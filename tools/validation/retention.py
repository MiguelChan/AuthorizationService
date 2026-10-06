#!/usr/bin/env python3
"""Run retention tests against an owned disposable PostgreSQL (1 CPU / 512 MiB).

No existing database or target URL is accepted. Requires Docker and Java 25.
The runtime role has DML privileges only; the fixture role applies migrations.
"""
import os
import pathlib
import signal
import subprocess
import time
import uuid

ROOT = pathlib.Path(__file__).resolve().parents[2]
NAME = "auth-retention-" + uuid.uuid4().hex[:12]
container = None
build = None
owned_processes = {}


def command(args, **kwargs):
    return subprocess.run(args, check=True, text=True, timeout=kwargs.pop("timeout", 30), **kwargs)


def process_snapshot():
    rows = command(["ps", "-axo", "pid=,ppid=,args="], capture_output=True).stdout.splitlines()
    return {int(p): (int(parent), args) for row in rows if len(parts := row.strip().split(None, 2)) == 3 for p, parent, args in [parts]}


def track_children():
    rows = process_snapshot()
    if build is None:
        return rows
    parents = {build.pid, *owned_processes}
    for _ in range(len(rows)):
        added = False
        for pid, (parent, args) in rows.items():
            if pid in parents or parent in parents:
                if pid not in parents:
                    parents.add(pid)
                    added = True
                if (pid == build.pid and build.poll() is None) or parent in parents:
                    owned_processes[pid] = args
                else:
                    owned_processes.setdefault(pid, args)
        if not added:
            break
    return rows


def stop_owned_processes():
    for sig, seconds in [(signal.SIGTERM, 10), (signal.SIGKILL, 5)]:
        deadline = time.monotonic() + seconds
        rows = track_children()
        for pid, expected in owned_processes.items():
            if pid in rows and rows[pid][1] == expected:
                try:
                    os.kill(pid, sig)
                except ProcessLookupError:
                    pass
        while time.monotonic() < deadline:
            if build is not None:
                build.poll()  # Reap the wrapper, which otherwise remains a zombie.
            rows = track_children()
            remaining = [pid for pid, args in owned_processes.items() if pid in rows and rows[pid][1] == args]
            if not remaining:
                return
            time.sleep(0.1)
    raise RuntimeError("Owned build processes did not exit: " + str(remaining))


def interrupted(_signum, _frame):
    raise KeyboardInterrupt()


signal.signal(signal.SIGTERM, interrupted)
try:
    container = command([
        "docker", "run", "-d", "--name", NAME, "--label", "codex.task=" + NAME,
        "--publish", "127.0.0.1::5432", "--cpus", "1", "--memory", "512m",
        "--memory-swap", "512m", "--pids-limit", "128",
        "--tmpfs", "/var/lib/postgresql/data", "-e", "POSTGRES_USER=probe",
        "-e", "POSTGRES_PASSWORD=fixture-only", "-e", "POSTGRES_DB=retention_probe",
        "postgres:16-alpine", "-c", "max_connections=20", "-c", "shared_buffers=128MB",
    ], capture_output=True).stdout.strip()
    for _ in range(60):
        ready = subprocess.run([
            "docker", "exec", container, "psql", "-U", "probe", "-d", "retention_probe", "-c", "SELECT 1"
        ], capture_output=True, timeout=5)
        if ready.returncode == 0:
            break
        time.sleep(0.5)
    else:
        raise RuntimeError("Disposable database startup timed out")
    migrations = sorted(
        (ROOT / "authorization-service/src/main/resources/db/migration").glob("V*.sql"),
        key=lambda p: int(p.name.split("__")[0][1:]),
    )
    setup = "CREATE SCHEMA auth_db;\n" + "\n".join(p.read_text() for p in migrations)
    setup += """
CREATE ROLE auth_runtime LOGIN PASSWORD 'runtime-fixture-only' NOSUPERUSER NOCREATEDB NOCREATEROLE;
GRANT USAGE ON SCHEMA auth_db TO auth_runtime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA auth_db TO auth_runtime;
GRANT USAGE,SELECT ON ALL SEQUENCES IN SCHEMA auth_db TO auth_runtime;
"""
    command(["docker", "exec", "-i", container, "psql", "-v", "ON_ERROR_STOP=1", "-U", "probe", "-d", "retention_probe"],
            input=setup, capture_output=True)
    denied = subprocess.run([
        "docker", "exec", container, "psql", "-v", "ON_ERROR_STOP=1", "-U", "probe", "-d", "retention_probe",
        "-c", "SET ROLE auth_runtime; CREATE TABLE auth_db.forbidden_fixture(x integer);"
    ], capture_output=True, text=True, timeout=10)
    assert denied.returncode != 0 and "permission denied" in denied.stderr.lower()
    port = command(["docker", "port", container, "5432"], capture_output=True).stdout.strip().split(":")[-1]
    env = os.environ.copy()
    env["RETENTION_TEST_URL"] = "jdbc:postgresql://127.0.0.1:" + port + "/retention_probe"
    print("Owned PostgreSQL: " + NAME + "; runtime DDL denied; V1–V11 applied", flush=True)
    build = subprocess.Popen([
        str(ROOT / "gradlew"), ":authorization-service:retentionTest", "--no-daemon", "--max-workers=1"
    ], cwd=ROOT, env=env, start_new_session=True)
    deadline = time.monotonic() + 300
    while build.poll() is None:
        track_children()
        if time.monotonic() > deadline:
            raise RuntimeError("Retention test deadline exceeded")
        time.sleep(0.5)
    result = build.returncode
    if result != 0:
        raise RuntimeError("Retention tests failed with exit code " + str(result))
finally:
    process_cleanup_error = None
    try:
        stop_owned_processes()
    except RuntimeError as error:
        process_cleanup_error = error
    if container is not None:
        command(["docker", "stop", "--time", "5", container], capture_output=True)
        command(["docker", "rm", container], capture_output=True)
    remaining = command(["docker", "ps", "-a", "--filter", "label=codex.task=" + NAME, "--format", "{{.ID}}"], capture_output=True)
    assert remaining.stdout.strip() == "", "Owned container cleanup failed"
    if process_cleanup_error is not None:
        raise process_cleanup_error
    print("Owned retention test resources removed", flush=True)
