#!/usr/bin/env python3
"""Validate an owned disposable TLS stack, then measure authenticated bounded load.

Requires Java 25, Go, Docker and a built bootJar. No production target is accepted.
All containers, child processes and secret fixtures are cleaned on success/failure.
"""
import argparse
import base64
import contextlib
import hashlib
import hmac
import http.client
import http.cookiejar
import json
import os
import pathlib
import signal
import socket
import ssl
import subprocess
import sys
import tempfile
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument(
    "--seconds",
    type=int,
    default=60,
    help="Sustained 1000-rps measurement duration, 1..300",
)
parser.add_argument(
    "--host-app",
    action="store_true",
    help="Run the JVM on the host; same bounded heap, no OS CPU/RAM quota",
)
parser.add_argument("--port", type=int, default=18443, help="Owned loopback HTTPS port")
parser.add_argument(
    "--diagnose-pool",
    action="store_true",
    help="Enable diagnostic pool logging; it can affect latency",
)
parser.add_argument("--app-cpus", type=int, default=2, choices=range(1, 9))
parser.add_argument("--app-memory-mib", type=int, default=768, choices=range(512, 4097))
parser.add_argument("--db-cpus", type=int, default=1, choices=range(1, 5))
args = parser.parse_args()
if not 1 <= args.seconds <= 300 or not 1024 <= args.port <= 65535:
    parser.error("Duration/port outside supported bounds")
root = pathlib.Path(__file__).resolve().parents[2]
out = pathlib.Path(tempfile.mkdtemp(prefix="authorization-validation-"))
print("Evidence directory:", out, flush=True)
run_id = uuid.uuid4().hex[:12]
label = "authorization-validation-" + run_id
network_name = "auth-capacity-net-" + run_id
db_name = "auth-capacity-db-" + run_id
app_name = "auth-capacity-app-" + run_id
receiver = None
server = None
server_log = None
load_failures = []
cleanup = {}


def interrupted(_signum, _frame):
    raise KeyboardInterrupt()


signal.signal(signal.SIGTERM, interrupted)

db = None
app = None
network = None
results = []
fixtures = []
tokens = []
samples = None
base = "https://localhost:" + str(args.port)
password = "Aa1!SecurityProbe"


def run(args, **kwargs):
    return subprocess.run(
        args, check=True, text=True, timeout=kwargs.pop("timeout", 30), **kwargs
    )


def sql(query):
    return run(
        [
            "docker",
            "exec",
            "-i",
            db,
            "psql",
            "-At",
            "-v",
            "ON_ERROR_STOP=1",
            "-U",
            "probe",
            "-d",
            "probe",
        ],
        input=query,
        capture_output=True,
    ).stdout.strip()


def request(path, method="GET", body=None, auth=None, headers=None, opener=None):
    h = dict(headers or {})
    if auth:
        pair = (
            [urllib.parse.quote_plus(x) for x in auth]
            if path.startswith("/oauth/")
            else auth
        )
        h["Authorization"] = (
            "Basic " + base64.b64encode((":".join(pair)).encode()).decode()
        )
    if isinstance(body, dict):
        body = json.dumps(body).encode()
        h.setdefault("Content-Type", "application/json")
    req = urllib.request.Request(base + path, data=body, headers=h, method=method)
    try:
        r = (
            opener
            or urllib.request.build_opener(urllib.request.HTTPSHandler(context=context))
        ).open(req, timeout=10)
    except urllib.error.HTTPError as e:
        r = e
    with r:
        raw = r.read(65537)
    if len(raw) > 65536:
        raise RuntimeError("Response exceeds the validation body bound")
    data = (
        json.loads(raw)
        if raw and r.headers.get("Content-Type", "").startswith("application/json")
        else None
    )
    return r.status, data, dict(r.headers)


def check(name, status, expected):
    assert status == expected, (name, status, expected)
    results.append({"check": name, "status": status})
    print(name, status, flush=True)


def oauth(path, client, params, expected=200, name="oauth"):
    status, data, headers = request(
        "/oauth/" + path,
        "POST",
        urllib.parse.urlencode(params).encode(),
        auth=(client["clientId"], client["clientSecret"]),
        headers={"Content-Type": "application/x-www-form-urlencoded"},
    )
    check(name, status, expected)
    assert headers.get("Cache-Control") == "no-store"
    return data


def basic(user):
    return ("capacity" + str(user) + "@example.com", password)


def cleanup_command(command, timeout):
    try:
        completed = subprocess.run(
            command, capture_output=True, text=True, timeout=timeout
        )
        if completed.returncode:
            cleanup.setdefault("errors", []).append(
                command[1] + ": " + completed.stderr[:256]
            )
        return completed.returncode == 0
    except (OSError, subprocess.TimeoutExpired) as error:
        cleanup.setdefault("errors", []).append(command[1] + ": " + str(error)[:256])
        return False


try:
    docker_info = json.loads(
        run(["docker", "info", "--format", "{{json .}}"], capture_output=True).stdout
    )
    (out / "docker-host.json").write_text(
        json.dumps(
            {
                "cpus": docker_info["NCPU"],
                "memory_bytes": docker_info["MemTotal"],
                "os": docker_info["OperatingSystem"],
            },
            indent=2,
        )
        + "\n"
    )
    if args.db_cpus > docker_info["NCPU"] or (
        not args.host_app and args.app_cpus > docker_info["NCPU"]
    ):
        raise RuntimeError(
            "Requested container CPU limit exceeds Docker capacity; choose a smaller limit. Docker settings are not changed."
        )
    with socket.socket() as port_check:
        port_check.bind(("127.0.0.1", args.port))
    jar = (
        root
        / "authorization-service/build/libs/authorization-service-0.0.1-SNAPSHOT.jar"
    )
    if not jar.exists():
        raise RuntimeError("Build ./gradlew release -Pskip-functional-tests first")
    tls_dir = out / "tls"
    tls_dir.mkdir(mode=0o700)
    keytool = (
        str(pathlib.Path(os.environ["JAVA_HOME"]) / "bin/keytool")
        if "JAVA_HOME" in os.environ
        else "keytool"
    )
    run(
        [
            keytool,
            "-genkeypair",
            "-alias",
            "tomcat",
            "-keyalg",
            "RSA",
            "-keysize",
            "2048",
            "-storetype",
            "PKCS12",
            "-keystore",
            str(tls_dir / "local.p12"),
            "-storepass",
            "local-validation-only",
            "-dname",
            "CN=localhost",
            "-validity",
            "3",
            "-ext",
            "SAN=dns:localhost,ip:127.0.0.1",
        ],
        capture_output=True,
    )
    (tls_dir / "local.p12").chmod(0o600)
    run(
        [
            keytool,
            "-exportcert",
            "-rfc",
            "-alias",
            "tomcat",
            "-keystore",
            str(tls_dir / "local.p12"),
            "-storepass",
            "local-validation-only",
            "-file",
            str(tls_dir / "local-ca.pem"),
        ],
        capture_output=True,
    )
    context = ssl.create_default_context(cafile=str(tls_dir / "local-ca.pem"))
    run(
        ["go", "build", "-o", str(out / "load"), str(root / "tools/load/main.go")],
        cwd=root,
    )
    assert (
        run(
            [
                "docker",
                "ps",
                "-a",
                "--filter",
                "label=codex.task=" + label + "",
                "--format",
                "{{.ID}}",
            ],
            capture_output=True,
        ).stdout.strip()
        == ""
    )
    network = run(
        ["docker", "network", "create", "--label", "codex.task=" + label, network_name],
        capture_output=True,
    ).stdout.strip()
    db = run(
        [
            "docker",
            "run",
            "-d",
            "--name",
            db_name,
            "--label",
            "codex.task=" + label,
            "--network",
            network,
            "--network-alias",
            "auth-db",
            *(["--publish", "127.0.0.1::5432"] if args.host_app else []),
            "--cpus",
            str(args.db_cpus),
            "--memory",
            "512m",
            "--memory-swap",
            "512m",
            "--pids-limit",
            "128",
            "--tmpfs",
            "/var/lib/postgresql/data",
            "-e",
            "POSTGRES_USER=probe",
            "-e",
            "POSTGRES_PASSWORD=local-validation-only",
            "-e",
            "POSTGRES_DB=probe",
            "postgres:16-alpine",
            "-c",
            "max_connections=50",
            "-c",
            "shared_buffers=128MB",
            "-c",
            "work_mem=4MB",
        ],
        capture_output=True,
    ).stdout.strip()
    for _ in range(40):
        p = subprocess.run(
            ["docker", "exec", db, "pg_isready", "-U", "probe", "-d", "probe"],
            capture_output=True,
            timeout=5,
        )
        if p.returncode == 0:
            time.sleep(2)
            if (
                subprocess.run(
                    [
                        "docker",
                        "exec",
                        db,
                        "psql",
                        "-U",
                        "probe",
                        "-d",
                        "probe",
                        "-c",
                        "SELECT 1",
                    ],
                    capture_output=True,
                    timeout=5,
                ).returncode
                == 0
            ):
                break
        time.sleep(0.5)
    else:
        raise RuntimeError("Database startup timeout")
    migrations = sorted(
        (root / "authorization-service/src/main/resources/db/migration").glob("V*.sql"),
        key=lambda p: int(p.name.split("__")[0][1:]),
    )
    sql("CREATE SCHEMA auth_db;\n" + "\n".join(p.read_text() for p in migrations))
    sql(
        "CREATE ROLE auth_runtime LOGIN PASSWORD 'runtime-fixture-only' NOSUPERUSER NOCREATEDB NOCREATEROLE; GRANT USAGE ON SCHEMA auth_db TO auth_runtime; GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA auth_db TO auth_runtime; GRANT USAGE,SELECT ON ALL SEQUENCES IN SCHEMA auth_db TO auth_runtime;"
    )
    ddl = subprocess.run(
        [
            "docker",
            "exec",
            db,
            "psql",
            "-U",
            "probe",
            "-d",
            "probe",
            "-v",
            "ON_ERROR_STOP=1",
            "-c",
            "SET ROLE auth_runtime; CREATE TABLE auth_db.forbidden_fixture(x integer);",
        ],
        capture_output=True,
        text=True,
        timeout=10,
    )
    assert ddl.returncode != 0 and "permission denied" in ddl.stderr.lower()
    results.append({"check": "runtime_role_cannot_execute_ddl", "passed": True})
    jar = (
        root
        / "authorization-service/build/libs/authorization-service-0.0.1-SNAPSHOT.jar"
    )
    assert jar.exists()
    launch = [
        "docker",
        "run",
        "-d",
        "--name",
        app_name,
        "--label",
        "codex.task=" + label,
        "--network",
        network,
        "--publish",
        "127.0.0.1:" + str(args.port) + ":8443",
        "--cpus",
        str(args.app_cpus),
        "--memory",
        str(args.app_memory_mib) + "m",
        "--memory-swap",
        str(args.app_memory_mib) + "m",
        "--pids-limit",
        "128",
        "--read-only",
        "--cap-drop",
        "ALL",
        "--security-opt",
        "no-new-privileges",
        "--user",
        str(os.getuid()) + ":" + str(os.getgid()),
        "--tmpfs",
        "/tmp:rw,nosuid,size=128m",
        "--mount",
        f"type=bind,src={jar},dst=/app/service.jar,readonly",
        "--mount",
        f'type=bind,src={out / "tls/local.p12"},dst=/validation/local.p12,readonly',
        "eclipse-temurin:25-jre@sha256:fcd7fd7b387f94bb2ac461478a7436ad8e349924c374ea8313919624dceae636",
        "java",
        "-XX:+UseG1GC",
        "-XX:InitialRAMPercentage=60",
        "-XX:MaxRAMPercentage=60",
        "-XX:MaxGCPauseMillis=50",
        "-Xlog:gc*:file=/tmp/gc.log:time,level,tags:filecount=2,filesize=2m",
        "-XX:ActiveProcessorCount=" + str(args.app_cpus),
        "-jar",
        "/app/service.jar",
        "--spring.profiles.active=prod",
        *(
            ["--logging.level.com.zaxxer.hikari.pool.HikariPool=DEBUG"]
            if args.diagnose_pool
            else []
        ),
        "--server.port=8443",
        "--server.ssl.enabled=true",
        "--server.ssl.key-store=file:/validation/local.p12",
        "--server.ssl.key-store-type=PKCS12",
        "--server.ssl.key-store-password=local-validation-only",
        "--spring.datasource.url=jdbc:postgresql://auth-db:5432/probe",
        "--spring.datasource.username=auth_runtime",
        "--spring.datasource.password=runtime-fixture-only",
        "--app.pepper.value=LocalValidationPepper",
        "--app.bcrypt.iterations=11",
        "--app.oauth.issuer=" + base,
    ]
    if args.host_app:
        db_info = json.loads(
            run(["docker", "inspect", db], capture_output=True).stdout
        )[0]
        database_port = db_info["NetworkSettings"]["Ports"]["5432/tcp"][0]["HostPort"]
        java_args = launch[launch.index("java") + 1 :]
        java_args = [
            value
            for value in java_args
            if not value.startswith(
                ("-XX:InitialRAMPercentage", "-XX:MaxRAMPercentage")
            )
        ]
        heap_mib = int(args.app_memory_mib * 0.6)
        java_args = [
            value.replace("/app/service.jar", str(jar))
            .replace("file:/validation/local.p12", "file:" + str(tls_dir / "local.p12"))
            .replace(
                "jdbc:postgresql://auth-db:5432/probe",
                "jdbc:postgresql://127.0.0.1:" + database_port + "/probe",
            )
            .replace("--server.port=8443", "--server.port=" + str(args.port))
            .replace("file=/tmp/gc.log", "file=" + str(out / "gc.log"))
            for value in java_args
        ]
        java = (
            str(pathlib.Path(os.environ["JAVA_HOME"]) / "bin/java")
            if "JAVA_HOME" in os.environ
            else "java"
        )
        server_log = (out / "constrained-server.log").open("w")
        server = subprocess.Popen(
            [
                java,
                "-Xms" + str(heap_mib) + "m",
                "-Xmx" + str(heap_mib) + "m",
                *java_args,
                "--server.address=127.0.0.1",
            ],
            stdout=server_log,
            stderr=subprocess.STDOUT,
            cwd=root,
            start_new_session=True,
        )
    else:
        app = run(launch, capture_output=True).stdout.strip()
    (out / "constrained-ownership.json").write_text(
        json.dumps(
            {
                "app": app,
                "db": db,
                "network": network,
                "cwd": str(root),
                "server_pid": server.pid if server else None,
                "host_app": args.host_app,
            },
            indent=2,
        )
    )
    for _ in range(120):
        try:
            if request("/api/ping")[0] == 200:
                break
        except (urllib.error.URLError, TimeoutError):
            pass
        if server is not None:
            if server.poll() is not None:
                raise RuntimeError("Host backend exited; inspect server log")
        elif (
            run(
                ["docker", "inspect", "--format", "{{.State.Running}}", app],
                capture_output=True,
            ).stdout.strip()
            != "true"
        ):
            raise RuntimeError("Backend exited; inspect server log")
        time.sleep(0.5)
    else:
        raise RuntimeError("Backend startup timeout")
    for i in range(3):
        jar_cookies = http.cookiejar.CookieJar()
        opener = urllib.request.build_opener(
            urllib.request.HTTPSHandler(context=context),
            urllib.request.HTTPCookieProcessor(jar_cookies),
        )
        status, nonce, _ = request("/api/csrf", opener=opener)
        check("csrf_" + str(i), status, 200)
        check(
            "registration_" + str(i),
            request(
                "/api/sign-up",
                "POST",
                {
                    "firstName": "Capacity",
                    "lastName": "Fixture",
                    "emailAddress": basic(i)[0],
                    "phoneNumber": "1234567890",
                    "password": password,
                },
                headers={nonce["headerName"]: nonce["token"]},
                opener=opener,
            )[0],
            200,
        )
    for i in range(3):
        owner = 0 if i == 0 else 1
        status, data, _ = request(
            "/api/applications",
            "POST",
            {
                "application": {
                    "appName": "Capacity " + str(i),
                    "shortDescription": "Bounded disposable fixture",
                    "redirectUrl": "https://example.com",
                }
            },
            auth=basic(owner),
        )
        check("create_application_" + str(i), status, 200)
        data["owner"] = owner
        fixtures.append(data)
    source, target, other = fixtures
    status, endpoint, _ = request(
        f'/api/applications/{target["applicationId"]}/endpoints',
        "POST",
        {"httpMethod": "GET", "path": "/resource", "action": "resource.read"},
        auth=basic(1),
    )
    check("register_resource", status, 200)
    status, grant, _ = request(
        f'/api/applications/{target["applicationId"]}/grants',
        "POST",
        {
            "sourceApplicationId": source["applicationId"],
            "endpointId": endpoint["endpointId"],
        },
        auth=basic(1),
    )
    check("target_owner_grants", status, 200)
    check(
        "source_owner_cannot_manage_target",
        request(
            f'/api/applications/{target["applicationId"]}/endpoints', auth=basic(0)
        )[0],
        403,
    )
    check(
        "bounded_page_limit",
        request(
            f'/api/applications/{target["applicationId"]}/endpoints?limit=101',
            auth=basic(1),
        )[0],
        400,
    )
    check(
        "negative_page_cursor",
        request(
            f'/api/applications/{target["applicationId"]}/grants?afterId=-1',
            auth=basic(1),
        )[0],
        400,
    )
    issued = oauth(
        "token",
        source,
        {"grant_type": "client_credentials", "audience": target["clientId"]},
        name="scoped_token",
    )
    token = issued["access_token"]
    tokens.append(token)
    claims = oauth(
        "introspect", target, {"token": token}, name="recipient_introspection"
    )
    assert claims["active"] is True and claims["aud"] == target["clientId"]
    assert claims["permissions"][0]["endpoint_id"] == endpoint["endpointId"]
    assert oauth(
        "introspect", other, {"token": token}, name="wrong_recipient_no_metadata"
    ) == {"active": False}
    assert oauth(
        "introspect", target, {"token": "A" * 43}, name="tampered_token_inactive"
    ) == {"active": False}
    check(
        "service_bearer_cannot_become_admin",
        request("/api/profile", headers={"Authorization": "Bearer " + token})[0],
        401,
    )
    check(
        "metadata_limit",
        request(
            "/api/applications",
            "POST",
            {
                "application": {
                    "appName": "X" * 129,
                    "shortDescription": "Bounded",
                    "redirectUrl": "https://example.com",
                }
            },
            auth=basic(0),
        )[0],
        400,
    )
    before = int(sql("SELECT count(*) FROM auth_db.sessions"))
    for _ in range(10):
        check(
            "basic_read_no_audit_write", request("/api/profile", auth=basic(0))[0], 200
        )
    assert int(sql("SELECT count(*) FROM auth_db.sessions")) == before
    check(
        "oversized_oauth_body",
        request(
            "/oauth/introspect",
            "POST",
            b"token=" + b"A" * 20000,
            auth=(target["clientId"], target["clientSecret"]),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )[0],
        413,
    )
    # Cursor pages are complete and nonoverlapping.
    for i in range(3):
        check(
            "catalog_" + str(i),
            request(
                f'/api/applications/{target["applicationId"]}/endpoints',
                "POST",
                {
                    "httpMethod": "GET",
                    "path": "/page" + str(i),
                    "action": "page." + str(i),
                },
                auth=basic(1),
            )[0],
            200,
        )
    status, page, _ = request(
        f'/api/applications/{target["applicationId"]}/endpoints?limit=2', auth=basic(1)
    )
    check("first_keyset_page", status, 200)
    assert len(page) == 2
    status, page2, _ = request(
        f'/api/applications/{target["applicationId"]}/endpoints?limit=2&afterId={page[-1]["endpointId"]}',
        auth=basic(1),
    )
    check("second_keyset_page", status, 200)
    assert len(page2) == 2 and not {e["endpointId"] for e in page} & {
        e["endpointId"] for e in page2
    }
    assert (
        sql(
            "SELECT bool_and(secret_hash LIKE 'hmac-sha256$%') FROM auth_db.client_credentials"
        )
        == "t"
    )
    # Real browser session auditing and mutation defenses against the packaged TLS server.
    browser_cookies = http.cookiejar.CookieJar()
    browser = urllib.request.build_opener(
        urllib.request.HTTPSHandler(context=context),
        urllib.request.HTTPCookieProcessor(browser_cookies),
    )
    status, nonce, _ = request("/api/csrf", opener=browser)
    check("browser_csrf", status, 200)
    login = urllib.parse.urlencode(
        {"username": basic(1)[0], "password": password}
    ).encode()
    check(
        "browser_login_without_csrf",
        request(
            "/login",
            "POST",
            login,
            headers={"Content-Type": "application/x-www-form-urlencoded"},
            opener=browser,
        )[0],
        403,
    )
    check(
        "browser_login",
        request(
            "/login",
            "POST",
            login,
            headers={
                "Content-Type": "application/x-www-form-urlencoded",
                nonce["headerName"]: nonce["token"],
            },
            opener=browser,
        )[0],
        200,
    )
    assert int(sql("SELECT count(*) FROM auth_db.sessions")) == before + 1
    before += 1
    check("session_read", request("/api/profile", opener=browser)[0], 200)
    assert int(sql("SELECT count(*) FROM auth_db.sessions")) == before
    check(
        "cookie_mutation_without_csrf",
        request("/api/profile", "PUT", {"firstName": "Forged"}, opener=browser)[0],
        403,
    )
    check(
        "basic_plus_cookie_does_not_bypass_csrf",
        request(
            "/api/profile",
            "PUT",
            {"firstName": "Forged"},
            auth=basic(1),
            opener=browser,
        )[0],
        403,
    )
    status, _, headers = request(
        "/api/profile", headers={"Origin": "https://localhost:18096"}, opener=browser
    )
    check("same_site_attacker_cors", status, 403)
    assert "Access-Control-Allow-Origin" not in headers
    check(
        "sql_injection_username",
        request("/api/profile", auth=("x' OR '1'='1", password))[0],
        401,
    )
    for _ in range(5):
        check(
            "failed_human_login",
            request("/api/profile", auth=(basic(2)[0], "incorrect"))[0],
            401,
        )
    check(
        "five_failures_block_correct_password",
        request("/api/profile", auth=basic(2))[0],
        401,
    )
    # Exercise legacy BCrypt upgrade through PostgreSQL and the real OAuth path without token invalidation.

    peppered = base64.b64encode(
        hmac.new(
            b"LocalValidationPepper", target["clientSecret"].encode(), hashlib.sha1
        ).digest()
    ).decode()
    sql(
        "CREATE EXTENSION pgcrypto; UPDATE auth_db.client_credentials SET secret_hash=crypt('"
        + peppered
        + "',gen_salt('bf',11)) WHERE application_id="
        + str(target["applicationId"])
    )
    wrong = dict(target)
    wrong["clientSecret"] = "z" * 43
    oauth(
        "introspect",
        wrong,
        {"token": token},
        expected=401,
        name="incorrect_legacy_secret",
    )
    assert (
        sql(
            "SELECT secret_hash LIKE '$2%' FROM auth_db.client_credentials WHERE application_id="
            + str(target["applicationId"])
        )
        == "t"
    )
    assert (
        oauth(
            "introspect",
            target,
            {"token": token},
            name="legacy_secret_upgrade_preserves_token",
        )["active"]
        is True
    )
    assert (
        sql(
            "SELECT (secret_hash LIKE 'hmac-sha256$%') AND version=1 FROM auth_db.client_credentials WHERE application_id="
            + str(target["applicationId"])
        )
        == "t"
    )
    # Tomcat rejects duplicate credentials; the admission filter bounds unknown-length uploads.
    pair = (
        urllib.parse.quote_plus(target["clientId"])
        + ":"
        + urllib.parse.quote_plus(target["clientSecret"])
    )
    authorization = "Basic " + base64.b64encode(pair.encode()).decode()
    with contextlib.closing(
        http.client.HTTPSConnection("localhost", args.port, context=context, timeout=10)
    ) as connection:
        connection.request(
            "POST",
            "/oauth/introspect",
            body=iter([b"token=", b"A" * 20000]),
            headers={
                "Authorization": authorization,
                "Content-Type": "application/x-www-form-urlencoded",
            },
            encode_chunked=True,
        )
        response = connection.getresponse()
        check("chunked_oversized_body", response.status, 413)
        response.read()
    with contextlib.closing(
        http.client.HTTPSConnection("localhost", args.port, context=context, timeout=10)
    ) as connection:
        body = urllib.parse.urlencode({"token": token}).encode()
        connection.putrequest("POST", "/oauth/introspect")
        connection.putheader("Authorization", authorization)
        connection.putheader("Authorization", authorization)
        connection.putheader("Content-Type", "application/x-www-form-urlencoded")
        connection.putheader("Content-Length", str(len(body)))
        connection.endheaders(body)
        response = connection.getresponse()
        check("duplicate_authorization", response.status, 401)
        response.read()
    check(
        "duplicate_form_parameter",
        request(
            "/oauth/introspect",
            "POST",
            b"token=A&token=B",
            auth=(target["clientId"], target["clientSecret"]),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )[0],
        400,
    )
    oauth(
        "token",
        source,
        {"grant_type": "password", "audience": target["clientId"]},
        expected=400,
        name="unsupported_password_grant",
    )
    # Keep sending bytes within the socket timeout: the total upload deadline must still fire.
    with contextlib.closing(
        http.client.HTTPSConnection("localhost", args.port, context=context, timeout=5)
    ) as connection:
        connection.putrequest("POST", "/oauth/introspect")
        connection.putheader("Authorization", authorization)
        connection.putheader("Content-Type", "application/x-www-form-urlencoded")
        connection.putheader("Transfer-Encoding", "chunked")
        connection.endheaders()
        started = time.monotonic()
        for index in range(4):
            if index:
                time.sleep(0.75)
            connection.send(b"1\r\nt\r\n")
        response = connection.getresponse()
        check("trickled_upload_has_absolute_deadline", response.status, 408)
        response.read()
        assert (
            time.monotonic() - started < 3.5
        ), "Upload waited for a fresh per-read timeout instead of its absolute budget"
    # Owner catalog quotas include inactive entries; bounded pages still work at the limit.
    sql(
        "INSERT INTO auth_db.application_endpoints(application_id,http_method,path,action,description) SELECT "
        + str(target["applicationId"])
        + ",'GET','/quota'||n,'quota.'||n,'' FROM generate_series(1,996) n"
    )
    check(
        "endpoint_quota",
        request(
            f'/api/applications/{target["applicationId"]}/endpoints',
            "POST",
            {"httpMethod": "GET", "path": "/overflow", "action": "overflow"},
            auth=basic(1),
        )[0],
        400,
    )
    status, page, _ = request(
        f'/api/applications/{target["applicationId"]}/endpoints', auth=basic(1)
    )
    check("default_page_remains_bounded", status, 200)
    assert len(page) == 100
    # A real receiving process enforces bearer -> introspection -> exact route permission.
    with socket.socket() as receiver_port_check:
        receiver_port_check.bind(("127.0.0.1", 0))
        receiver_port = receiver_port_check.getsockname()[1]
    receiver_env = os.environ.copy()
    receiver_env.update(
        {
            "SSL_CERT_FILE": str(tls_dir / "local-ca.pem"),
            "AUTHORIZATION_URL": base,
            "OAUTH_ISSUER": base,
            "CLIENT_ID": target["clientId"],
            "CLIENT_SECRET": target["clientSecret"],
            "ENDPOINT_ID": str(endpoint["endpointId"]),
            "RESOURCE_PORT": str(receiver_port),
        }
    )
    receiver_log = (out / "receiver.log").open("w")
    receiver = subprocess.Popen(
        [sys.executable, str(root / "examples/python/opaque_token_resource.py")],
        env=receiver_env,
        stdout=receiver_log,
        stderr=subprocess.STDOUT,
        start_new_session=True,
    )
    (out / "receiver-ownership.json").write_text(
        json.dumps({"pid": receiver.pid, "pgid": receiver.pid, "port": receiver_port})
    )

    def resource(bearer=None):
        headers = {"Authorization": "Bearer " + bearer} if bearer else {}
        try:
            response = urllib.request.urlopen(
                urllib.request.Request(
                    "http://127.0.0.1:" + str(receiver_port) + "/resource",
                    headers=headers,
                ),
                timeout=5,
            )
        except urllib.error.HTTPError as error:
            response = error
        with response:
            return response.status, json.load(response)

    for _ in range(40):
        try:
            if resource()[0] == 401:
                break
        except urllib.error.URLError:
            pass
        if receiver.poll() is not None:
            raise RuntimeError("Receiving service exited")
        time.sleep(0.1)
    else:
        raise RuntimeError("Receiving service did not become ready")
    check("receiving_service_missing_bearer", resource()[0], 401)
    check("receiving_service_allows_exact_permission", resource(token)[0], 200)
    check("receiving_service_tampered_token", resource("A" * 43)[0], 401)
    fixture_path = out / "load-fixture.json"
    fixture_path.write_text(
        json.dumps(
            {
                "clientId": target["clientId"],
                "clientSecret": target["clientSecret"],
                "token": token,
            }
        )
    )
    fixture_path.chmod(0o600)
    # docker stats is bounded to the exact owned containers and is stopped in finally.
    stats_file = (out / "constrained-stats.log").open("w")
    samples = subprocess.Popen(
        [
            "docker",
            "stats",
            "--format",
            "{{json .}}",
            *[owned for owned in (app, db) if owned],
        ],
        stdout=stats_file,
        stderr=subprocess.STDOUT,
        start_new_session=True,
    )
    (out / "constrained-stats-ownership.json").write_text(
        json.dumps({"pid": samples.pid, "pgid": samples.pid, "containers": [app, db]})
    )
    for label, rate, seconds, concurrency, burst in [
        ("ramp-100", 100, 5, 200, False),
        ("ramp-500", 500, 10, 500, False),
        ("target-1000", 1000, args.seconds, 1000, False),
        ("burst-1000", 1000, 1, 1000, True),
    ]:
        command = [
            str(out / "load"),
            "-url",
            base + "/oauth/introspect",
            "-fixture",
            str(fixture_path),
            "-ca",
            str(out / "tls/local-ca.pem"),
            "-rate",
            str(rate),
            "-seconds",
            str(seconds),
            "-concurrency",
            str(concurrency),
        ]
        if burst:
            command += ["-burst"]
        result = subprocess.run(
            command, capture_output=True, text=True, timeout=seconds + 30
        )
        (out / (label + ".json")).write_text(result.stdout)
        if result.stdout:
            evidence = json.loads(result.stdout)
            print(label, json.dumps(evidence), flush=True)
        if result.returncode:
            load_failures.append(label)
    manifests = []
    for owned in [item for item in (app, db) if item]:
        info = json.loads(
            run(["docker", "inspect", owned], capture_output=True).stdout
        )[0]
        manifests.append(
            {
                "name": info["Name"],
                "image_id": info["Image"],
                "nano_cpus": info["HostConfig"]["NanoCpus"],
                "memory_bytes": info["HostConfig"]["Memory"],
                "pids_limit": info["HostConfig"]["PidsLimit"],
                "read_only_root": info["HostConfig"]["ReadonlyRootfs"],
            }
        )
    (out / "resource-manifest.json").write_text(json.dumps(manifests, indent=2) + "\n")
    # Live grant revocation must still take effect after the successful high-rate run.
    check(
        "revoke_after_load",
        request(
            f'/api/applications/{target["applicationId"]}/grants/{grant["grantId"]}',
            "DELETE",
            auth=basic(1),
        )[0],
        204,
    )
    assert oauth(
        "introspect", target, {"token": token}, name="revocation_after_load"
    ) == {"active": False}
    check("receiving_service_rejects_revoked_grant", resource(token)[0], 401)
    status, regrant, _ = request(
        f'/api/applications/{target["applicationId"]}/grants',
        "POST",
        {
            "sourceApplicationId": source["applicationId"],
            "endpointId": endpoint["endpointId"],
        },
        auth=basic(1),
    )
    check("regrant_increases_version", status, 200)
    assert regrant["version"] > grant["version"]
    assert oauth(
        "introspect",
        target,
        {"token": token},
        name="regrant_does_not_restore_old_token",
    ) == {"active": False}
    fresh = oauth(
        "token",
        source,
        {"grant_type": "client_credentials", "audience": target["clientId"]},
        name="fresh_token_after_regrant",
    )["access_token"]
    tokens.append(fresh)
    assert (
        oauth("introspect", target, {"token": fresh}, name="fresh_token_active")[
            "active"
        ]
        is True
    )
    oauth("revoke", other, {"token": fresh}, name="foreign_source_cannot_revoke")
    assert (
        oauth(
            "introspect",
            target,
            {"token": fresh},
            name="foreign_revocation_does_not_change_token",
        )["active"]
        is True
    )
    oauth("revoke", source, {"token": fresh}, name="source_revokes_token")
    assert oauth(
        "introspect", target, {"token": fresh}, name="token_revocation_is_live"
    ) == {"active": False}
    expired = oauth(
        "token",
        source,
        {"grant_type": "client_credentials", "audience": target["clientId"]},
        name="token_for_expiry_check",
    )["access_token"]
    tokens.append(expired)
    sql(
        "UPDATE auth_db.oauth_tokens SET issued_at=CURRENT_TIMESTAMP-INTERVAL '600 seconds',expires_at=CURRENT_TIMESTAMP-INTERVAL '1 second' WHERE token_hash='"
        + hashlib.sha256(expired.encode()).hexdigest()
        + "'"
    )
    assert oauth(
        "introspect", target, {"token": expired}, name="expired_token_inactive"
    ) == {"active": False}
    bound = oauth(
        "token",
        source,
        {"grant_type": "client_credentials", "audience": target["clientId"]},
        name="token_before_recipient_rotation",
    )["access_token"]
    tokens.append(bound)
    status, rotated_target, _ = request(
        f'/api/applications/{target["applicationId"]}/credentials/rotate',
        "POST",
        auth=basic(1),
    )
    check("recipient_credential_rotation", status, 200)
    assert (
        rotated_target["clientId"] == target["clientId"]
        and rotated_target["version"] == 2
    )
    fixtures.append(rotated_target)
    oauth(
        "introspect",
        target,
        {"token": bound},
        expected=401,
        name="old_recipient_secret_rejected",
    )
    assert oauth(
        "introspect",
        rotated_target,
        {"token": bound},
        name="recipient_rotation_invalidates_token",
    ) == {"active": False}
    bound = oauth(
        "token",
        source,
        {"grant_type": "client_credentials", "audience": target["clientId"]},
        name="token_before_source_rotation",
    )["access_token"]
    tokens.append(bound)
    assert (
        oauth(
            "introspect",
            rotated_target,
            {"token": bound},
            name="new_recipient_version_works",
        )["active"]
        is True
    )
    status, rotated_source, _ = request(
        f'/api/applications/{source["applicationId"]}/credentials/rotate',
        "POST",
        auth=basic(0),
    )
    check("source_credential_rotation", status, 200)
    fixtures.append(rotated_source)
    assert oauth(
        "introspect",
        rotated_target,
        {"token": bound},
        name="source_rotation_invalidates_token",
    ) == {"active": False}
    bound = oauth(
        "token",
        rotated_source,
        {"grant_type": "client_credentials", "audience": target["clientId"]},
        name="token_before_endpoint_deactivation",
    )["access_token"]
    tokens.append(bound)
    check(
        "endpoint_deactivation",
        request(
            f'/api/applications/{target["applicationId"]}/endpoints/{endpoint["endpointId"]}',
            "DELETE",
            auth=basic(1),
        )[0],
        204,
    )
    assert oauth(
        "introspect",
        rotated_target,
        {"token": bound},
        name="endpoint_deactivation_invalidates_token",
    ) == {"active": False}
    # Issuance is intentionally a separate, lower rate budget than introspection.
    for attempt in range(80):
        status, data, headers = request(
            "/oauth/token",
            "POST",
            urllib.parse.urlencode(
                {"grant_type": "client_credentials", "audience": target["clientId"]}
            ).encode(),
            auth=(other["clientId"], other["clientSecret"]),
            headers={"Content-Type": "application/x-www-form-urlencoded"},
        )
        # This source has no grant; even failed exchanges consume the public issuance budget.
        if status == 429:
            break
        assert status == 400
    else:
        raise AssertionError("Token issuance rate limit did not reject excess demand")
    check("issuance_budget_returns_retry_after", status, 429)
    assert headers.get("Retry-After") == "1"
    assert sql("SELECT count(*) FROM auth_db.sessions") == str(before)
    fixture_path.unlink()
    (out / "constrained-security-results.json").write_text(
        json.dumps(
            {
                "checks": results,
                "resources": {
                    "app_cpus": args.app_cpus,
                    "app_memory_mib": args.app_memory_mib,
                    "db_cpus": args.db_cpus,
                    "db_memory_mib": 512,
                    "bcrypt_cost": 11,
                    "https_verified": True,
                    "runtime_role_ddl": False,
                    "gc": "G1",
                    "host_app": args.host_app,
                    "app_os_quota_enforced": not args.host_app,
                    "initial_and_max_heap_ram_percent": 60,
                },
                "http_checks": len(results),
                "load_failures": load_failures,
            },
            indent=2,
        )
        + "\n"
    )
    print(
        "Security checks completed:",
        len(results),
        "Load phases outside SLO:",
        load_failures,
        flush=True,
    )
finally:
    cleanup = {}
    if server is not None:
        if server.poll() is None:
            os.killpg(server.pid, signal.SIGTERM)
            try:
                server.wait(timeout=15)
            except subprocess.TimeoutExpired:
                os.killpg(server.pid, signal.SIGKILL)
                server.wait(timeout=5)
        server_log.close()
        cleanup["server_exit_code"] = server.returncode
        logs = (out / "constrained-server.log").read_text()
        cleanup["logs_contain_credentials"] = any(
            marker in logs
            for marker in [password] + [x["clientSecret"] for x in fixtures] + tokens
        )
    if receiver is not None:
        if receiver.poll() is None:
            os.killpg(receiver.pid, signal.SIGTERM)
            try:
                receiver.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(receiver.pid, signal.SIGKILL)
                receiver.wait(timeout=5)
        receiver_log.close()
        cleanup["receiver_exit_code"] = receiver.returncode
    if samples is not None:
        if samples.poll() is None:
            os.killpg(samples.pid, signal.SIGTERM)
            try:
                samples.wait(timeout=10)
            except subprocess.TimeoutExpired:
                os.killpg(samples.pid, signal.SIGKILL)
                samples.wait(timeout=5)
        stats_file.close()
        cleanup["stats_exit_code"] = samples.returncode
    if app:
        try:
            logs = subprocess.run(
                ["docker", "logs", app], capture_output=True, text=True, timeout=15
            )
            (out / "constrained-server.log").write_text(logs.stdout + logs.stderr)
            cleanup["logs_contain_credentials"] = any(
                marker in logs.stdout + logs.stderr
                for marker in [password]
                + [x["clientSecret"] for x in fixtures]
                + tokens
            )
            gc_log = subprocess.run(
                ["docker", "exec", app, "cat", "/tmp/gc.log"],
                capture_output=True,
                text=True,
                timeout=15,
            )
            (out / "gc.log").write_text(gc_log.stdout)
            db_log = subprocess.run(
                ["docker", "logs", db], capture_output=True, text=True, timeout=15
            )
            (out / "database.log").write_text(db_log.stdout + db_log.stderr)
        except (OSError, subprocess.TimeoutExpired) as error:
            cleanup.setdefault("errors", []).append("log capture: " + str(error)[:256])
        cleanup["app_stopped"] = cleanup_command(
            ["docker", "stop", "--time", "15", app], 25
        )
        cleanup["app_removed"] = cleanup_command(["docker", "rm", "--force", app], 20)
    if db:
        cleanup["database_removed"] = cleanup_command(
            ["docker", "rm", "--force", db], 20
        )
    if network:
        cleanup["network_removed"] = cleanup_command(
            ["docker", "network", "rm", network], 20
        )
    (out / "load-fixture.json").unlink(missing_ok=True)
    (out / "tls/local.p12").unlink(missing_ok=True)
    (out / "constrained-cleanup.json").write_text(json.dumps(cleanup, indent=2) + "\n")
    print("Cleanup", json.dumps(cleanup), flush=True)
    if app and not cleanup.get("app_removed"):
        raise RuntimeError("App container cleanup failed")
    if db and not cleanup.get("database_removed"):
        raise RuntimeError("DB container cleanup failed")

    if cleanup.get("errors"):
        raise RuntimeError(
            "Validation cleanup reported errors; inspect cleanup evidence"
        )
    if cleanup.get("logs_contain_credentials"):
        raise RuntimeError("Disposable credential marker appeared in logs")

if load_failures:
    raise SystemExit(1)
