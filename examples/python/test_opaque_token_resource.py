import io
import json
import time
import unittest
from unittest.mock import patch
from opaque_token_resource import authorize, InvalidToken, PermissionDenied, AuthorizationUnavailable


class ResourceAuthorizationTests(unittest.TestCase):
    def setUp(self):
        self.claims = {"active": True, "iss": "https://issuer.example", "aud": "target", "exp": time.time() + 300,
            "scope": "resource.read", "permissions": [{"endpoint_id": 7, "action": "resource.read", "http_method": "GET"}]}

    def check(self, claims=None):
        with patch("urllib.request.urlopen", return_value=io.BytesIO(json.dumps(self.claims if claims is None else claims).encode())):
            authorize("Bearer " + "a" * 43, 7, "resource.read", "GET", "https://issuer.example", "target", "secret", "https://issuer.example")

    def test_exact_permission_is_allowed(self):
        self.check()

    def test_wrong_issuer_and_audience_are_denied(self):
        for field in ("iss", "aud"):
            with self.assertRaises(InvalidToken):
                self.check({**self.claims, field: "wrong"})

    def test_inactive_and_expired_are_denied(self):
        with self.assertRaises(InvalidToken):
            self.check({"active": False})
        for expiry in (time.time() - 1, None, True, float("nan")):
            with self.assertRaises(InvalidToken):
                self.check({**self.claims, "exp": expiry})

    def test_missing_or_wrong_scope_endpoint_and_method_are_denied(self):
        for permission in ({"endpoint_id": 8, "action": "resource.read", "http_method": "GET"},
            {"endpoint_id": 7, "action": "resource.write", "http_method": "GET"},
            {"endpoint_id": 7, "action": "resource.read", "http_method": "POST"}):
            with self.assertRaises(PermissionDenied):
                self.check({**self.claims, "permissions": [permission]})
        with self.assertRaises(PermissionDenied):
            self.check({**self.claims, "scope": "resource.write"})

    def test_unavailable_introspection_fails_closed(self):
        with patch("urllib.request.urlopen", side_effect=TimeoutError()):
            with self.assertRaises(AuthorizationUnavailable):
                authorize("Bearer " + "a" * 43, 7, "resource.read", "GET", "https://issuer.example", "target", "secret", "https://issuer.example")


if __name__ == "__main__":
    unittest.main()
