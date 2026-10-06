#!/usr/bin/env python3
"""Test the packaged SPA against an owned, limited, disposable PostgreSQL.

Run the serial Gradle release first. Requires Java 25, Docker and installed Chrome.
No external target/database is accepted. HTTP is restricted to the loopback dev
fixture; production TLS validation remains in tools/validation/run.py.
"""
import os
import pathlib
import signal
import socket
import subprocess
import tempfile
import time
import urllib.error
import urllib.request
import uuid

ROOT = pathlib.Path(__file__).resolve().parents[2]
WEBSITE = ROOT / 'authorization-website'
NAME = 'auth-browser-' + uuid.uuid4().hex[:12]
OUT = pathlib.Path(tempfile.mkdtemp(prefix=NAME + '-'))
container = None
processes = []
owned = {}
handles = []


def command(args, **kwargs):
    return subprocess.run(args, check=True, text=True, timeout=kwargs.pop('timeout', 30), **kwargs)


def snapshot():
    rows = command(['ps', '-axo', 'pid=,ppid=,args='], capture_output=True).stdout.splitlines()
    result = {}
    for row in rows:
        fields = row.strip().split(None, 2)
        if len(fields) == 3:
            result[int(fields[0])] = (int(fields[1]), fields[2])
    return result


def track_children():
    rows = snapshot()
    parents = {p.pid for p in processes if p.poll() is None} | {
        pid for pid, args in owned.items() if pid in rows and rows[pid][1] == args
    }
    for _ in range(len(rows)):
        children = {pid for pid, (parent, _) in rows.items() if parent in parents}
        if children <= parents:
            break
        parents |= children
    for pid in parents:
        if pid in rows:
            owned[pid] = rows[pid][1]
    return rows


def stop_owned():
    for sig, seconds in [(signal.SIGTERM, 10), (signal.SIGKILL, 5)]:
        rows = track_children()
        for pid, expected in list(owned.items()):
            if pid in rows and rows[pid][1] == expected:
                try:
                    os.kill(pid, sig)
                except ProcessLookupError:
                    pass
        deadline = time.monotonic() + seconds
        while time.monotonic() < deadline:
            for process in processes:
                process.poll()
            rows = track_children()
            remaining = [pid for pid, args in owned.items() if pid in rows and rows[pid][1] == args]
            if not remaining:
                return
            time.sleep(0.1)
    raise RuntimeError('Owned processes did not exit: ' + str(remaining))


def start(args, path, env, cwd):
    handle = path.open('w')
    path.chmod(0o600)
    handles.append(handle)
    process = subprocess.Popen(args, cwd=cwd, env=env, stdout=handle, stderr=subprocess.STDOUT, start_new_session=True)
    processes.append(process)
    track_children()
    return process


def interrupted(_signum, _frame):
    raise KeyboardInterrupt()


signal.signal(signal.SIGTERM, interrupted)
print('Evidence directory: ' + str(OUT), flush=True)
try:
    jar = ROOT / 'authorization-service/build/libs/authorization-service-0.0.1-SNAPSHOT.jar'
    nodes = list((WEBSITE / '.gradle/nodejs').glob('node-v24.21.0-*/bin/node'))
    if not jar.exists() or len(nodes) != 1:
        raise RuntimeError('Run ./gradlew release -Pskip-functional-tests first')
    java = str(pathlib.Path(os.environ['JAVA_HOME']) / 'bin/java') if 'JAVA_HOME' in os.environ else 'java'
    node = str(nodes[0])
    container = command([
        'docker', 'run', '-d', '--name', NAME, '--label', 'codex.task=' + NAME,
        '--publish', '127.0.0.1::5432', '--cpus', '1', '--memory', '512m',
        '--memory-swap', '512m', '--pids-limit', '128', '--tmpfs', '/var/lib/postgresql/data',
        '-e', 'POSTGRES_USER=probe', '-e', 'POSTGRES_PASSWORD=fixture-only', '-e', 'POSTGRES_DB=browser_probe',
        'postgres:16-alpine', '-c', 'max_connections=20', '-c', 'shared_buffers=128MB',
    ], capture_output=True).stdout.strip()
    for _ in range(60):
        ready = subprocess.run(['docker', 'exec', container, 'psql', '-U', 'probe', '-d', 'browser_probe', '-c', 'SELECT 1'], capture_output=True, timeout=5)
        if ready.returncode == 0:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError('Disposable PostgreSQL startup timed out')
    migrations = sorted((ROOT / 'authorization-service/src/main/resources/db/migration').glob('V*.sql'), key=lambda p: int(p.name.split('__')[0][1:]))
    sql = 'CREATE SCHEMA auth_db;\n' + '\n'.join(p.read_text() for p in migrations) + '''
CREATE ROLE auth_runtime LOGIN PASSWORD 'runtime-fixture-only' NOSUPERUSER NOCREATEDB NOCREATEROLE;
GRANT USAGE ON SCHEMA auth_db TO auth_runtime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA auth_db TO auth_runtime;
GRANT USAGE,SELECT ON ALL SEQUENCES IN SCHEMA auth_db TO auth_runtime;
'''
    command(['docker', 'exec', '-i', container, 'psql', '-v', 'ON_ERROR_STOP=1', '-U', 'probe', '-d', 'browser_probe'], input=sql, capture_output=True)
    denied = subprocess.run(['docker', 'exec', container, 'psql', '-v', 'ON_ERROR_STOP=1', '-U', 'probe', '-d', 'browser_probe', '-c', 'SET ROLE auth_runtime; CREATE TABLE auth_db.forbidden_fixture(x integer);'], capture_output=True, text=True, timeout=10)
    if denied.returncode == 0 or 'permission denied' not in denied.stderr.lower():
        raise RuntimeError('Runtime DDL boundary failed')
    db_port = command(['docker', 'port', container, '5432'], capture_output=True).stdout.strip().split(':')[-1]
    with socket.socket() as sock:
        sock.bind(('127.0.0.1', 0))
        port = sock.getsockname()[1]
    base = 'http://127.0.0.1:' + str(port)
    # Only OS runtime paths are inherited; app/credential/config environment is discarded.
    env = {key: os.environ[key] for key in ('PATH', 'HOME', 'TMPDIR', 'JAVA_HOME') if key in os.environ}
    server = start([
        java, '-XX:+UseG1GC', '-Xms128m', '-Xmx256m', '-XX:ActiveProcessorCount=2', '-jar', str(jar),
        '--spring.profiles.active=dev', '--server.address=127.0.0.1', '--server.port=' + str(port),
        '--spring.datasource.url=jdbc:postgresql://127.0.0.1:' + db_port + '/browser_probe',
        '--spring.datasource.username=auth_runtime', '--spring.datasource.password=runtime-fixture-only',
        '--spring.datasource.hikari.maximum-pool-size=4', '--spring.datasource.hikari.minimum-idle=4',
        '--app.pepper.value=owned-browser-fixture', '--app.bcrypt.iterations=11',
        '--app.oauth.issuer=' + base, '--app.security.allowed-origins=http://localhost:3000',
        '--app.retention.enabled=false',
    ], OUT / 'server.log', env, ROOT)
    deadline = time.monotonic() + 60
    while time.monotonic() < deadline:
        track_children()
        if server.poll() is not None:
            raise RuntimeError('Packaged server exited; inspect ' + str(OUT / 'server.log'))
        try:
            with urllib.request.urlopen(base + '/api/deep_ping', timeout=1) as response:
                if response.status == 200:
                    break
        except (OSError, urllib.error.URLError):
            time.sleep(0.2)
    else:
        raise RuntimeError('Packaged server readiness timed out')
    print('Owned PostgreSQL: ' + NAME + '; runtime DDL denied; packaged jar ready', flush=True)
    env['AUTH_BROWSER_BASE_URL'] = base
    browser = start([node, str(WEBSITE / 'node_modules/@playwright/test/cli.js'), 'test'], OUT / 'browser.log', env, WEBSITE)
    deadline = time.monotonic() + 150
    while browser.poll() is None:
        track_children()
        if server.poll() is not None or time.monotonic() > deadline:
            raise RuntimeError('Browser test/server deadline exceeded')
        time.sleep(0.2)
    print((OUT / 'browser.log').read_text(), flush=True)
    if browser.returncode != 0:
        raise RuntimeError('Packaged-browser regression failed')
    accounts = command(['docker', 'exec', container, 'psql', '-At', '-U', 'probe', '-d', 'browser_probe', '-c', 'SELECT count(*) FROM auth_db.accounts;'], capture_output=True).stdout.strip()
    if accounts != '1':
        raise RuntimeError('Expected one actual registered account; found ' + accounts)
    print('Database confirms one registered account', flush=True)
finally:
    errors = []
    try:
        stop_owned()
    except Exception as error:
        errors.append(str(error))
    for handle in handles:
        handle.close()
    if container is not None:
        for args in (['docker', 'stop', '--time', '5', container], ['docker', 'rm', container]):
            try:
                command(args, capture_output=True)
            except Exception as error:
                errors.append(str(error))
    remaining = command(['docker', 'ps', '-a', '--filter', 'label=codex.task=' + NAME, '--format', '{{.ID}}'], capture_output=True)
    if remaining.stdout.strip():
        errors.append('Owned container remains: ' + remaining.stdout.strip())
    if errors:
        raise RuntimeError('Cleanup failed: ' + '; '.join(errors))
    print('Owned browser/server/container resources removed', flush=True)
