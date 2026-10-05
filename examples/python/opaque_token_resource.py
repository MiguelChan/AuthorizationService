"""A minimal receiving service enforcing the online opaque-token contract.

Configure AUTHORIZATION_URL, OAUTH_ISSUER, CLIENT_ID, CLIENT_SECRET, ENDPOINT_ID
and optionally RESOURCE_PORT. /resource maps to the registered GET action resource.read.
The server binds to loopback for an explicit local example. Deployed services require HTTPS.
"""
import base64
import json
import math
import os
import re
import time
import urllib.error
import urllib.parse
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer


class InvalidToken(Exception):
    """The request does not carry a current token for this receiving service."""


class PermissionDenied(Exception):
    """The token does not grant this exact receiving endpoint and action."""


class AuthorizationUnavailable(Exception):
    """Introspection failed; the resource must not execute."""


def authorize(authorization, endpoint_id, action, method, issuer, audience, client_secret, server_url):
    """Authenticate the bearer, bind issuer/audience, and require the exact route permission."""
    if not authorization or not re.fullmatch(r"(?i:Bearer) [A-Za-z0-9_-]{43}", authorization):
        raise InvalidToken()
    token = authorization.split(" ", 1)[1]
    credentials = urllib.parse.quote_plus(audience) + ":" + urllib.parse.quote_plus(client_secret)
    basic = base64.b64encode(credentials.encode()).decode()
    request = urllib.request.Request(server_url.rstrip("/") + "/oauth/introspect",
        data=urllib.parse.urlencode({"token": token}).encode(),
        headers={"Authorization": "Basic " + basic, "Content-Type": "application/x-www-form-urlencoded", "Accept": "application/json"})
    try:
        with urllib.request.urlopen(request, timeout=3) as response:
            claims = json.load(response)
    except (urllib.error.URLError, TimeoutError, ValueError) as error:
        raise AuthorizationUnavailable() from error
    if not isinstance(claims, dict) or claims.get("active") is not True or claims.get("iss") != issuer or claims.get("aud") != audience:
        raise InvalidToken()
    expiry = claims.get("exp")
    if isinstance(expiry, bool) or not isinstance(expiry, (int, float)) or not math.isfinite(expiry) or expiry <= time.time():
        raise InvalidToken()
    scope = claims.get("scope")
    permissions = claims.get("permissions")
    if not isinstance(scope, str) or not isinstance(permissions, list):
        raise PermissionDenied()
    permitted = any(isinstance(entry, dict) and type(entry.get("endpoint_id")) is int and entry.get("endpoint_id") == endpoint_id
        and entry.get("action") == action and entry.get("http_method") == method for entry in permissions)
    if action not in scope.split() or not permitted:
        raise PermissionDenied()


def serve():
    """Run the example resource without storing or logging submitted bearer tokens."""
    endpoint_id = int(os.environ["ENDPOINT_ID"])
    issuer = os.environ["OAUTH_ISSUER"]
    audience = os.environ["CLIENT_ID"]
    secret = os.environ["CLIENT_SECRET"]
    server_url = os.environ["AUTHORIZATION_URL"]

    class Handler(BaseHTTPRequestHandler):
        def log_message(self, *_args):
            pass

        def do_GET(self):
            if self.path != "/resource":
                self.send_error(404)
                return
            headers = self.headers.get_all("Authorization", [])
            try:
                if len(headers) != 1:
                    raise InvalidToken()
                authorize(headers[0], endpoint_id, "resource.read", "GET", issuer, audience, secret, server_url)
                status, body, challenge = 200, {"resource": "allowed"}, None
            except InvalidToken:
                status, body, challenge = 401, {"error": "invalid_token"}, 'Bearer realm="resource", error="invalid_token"'
            except PermissionDenied:
                status, body, challenge = 403, {"error": "insufficient_scope"}, 'Bearer error="insufficient_scope", scope="resource.read"'
            except AuthorizationUnavailable:
                status, body, challenge = 503, {"error": "authorization_unavailable"}, None
            self.send_response(status)
            self.send_header("Content-Type", "application/json")
            self.send_header("Cache-Control", "no-store")
            if challenge:
                self.send_header("WWW-Authenticate", challenge)
            self.end_headers()
            self.wfile.write(json.dumps(body).encode())

    server = HTTPServer(("127.0.0.1", int(os.environ.get("RESOURCE_PORT", "18095"))), Handler)
    try:
        server.serve_forever()
    finally:
        server.server_close()


if __name__ == "__main__":
    serve()
