# Frontend dependency security — 2026-10-06

The inspected baseline is mainline e0f1e8a. A fresh npm audit of the complete locked
tree reports 245 findings: 19 low, 132 moderate, 75 high and 19 critical. Counts are
packages in the npm report, not demonstrated vulnerabilities in the shipped app.
The deployment serves the compiled website inside the Spring jar; Node, Storybook,
test frameworks and build servers do not run in that deployed process.

## First correction layer

Pin Axios 1.20.0, React Router DOM 6.30.6 and the compatible Lodash 4.18.1 override.
Remove unused yup-phone and history direct dependencies. Pin the existing MUI lab
alpha instead of allowing an unconstrained upgrade during lockfile generation.
The resulting complete-tree report has 241 findings: 19 low, 131 moderate, 72 high
and 19 critical. Axios and Lodash no longer appear in that report. Remaining build
and router findings are retained for the next layer; they are not hidden by audit
flags or an unreviewed force fix.

| Package/path | Evidence and applicability | Action |
| --- | --- | --- |
| Axios | APIClient imports it; 29 baseline source-map modules are present. The XSRF advisory concerns cross-host cookie-header disclosure. Current API calls use fixed relative routes; no arbitrary-host caller path was found. Node adapter SSRF/proxy advisories do not describe the browser XHR path. | Patch the library and validate actual registration/login/CSRF behavior. |
| Lodash | 262 baseline source-map modules are present, including the Formik dependency. Specific attacker-controlled calls to the affected template/unset/omit operations were not demonstrated. | Apply the compatible patched release throughout the lockfile. |
| React Router | BrowserRouter and fixed local links are used. The remaining 6.x report includes backslash redirects and SSR hydration; no caller-supplied link destination or server hydration flow is used here. | Move to patched Router 7 with compatible React in the build layer. |
| yup-phone/google-libphonenumber | No imports and no emitted baseline modules; signup already validates ten digits with Yup. | Remove unused dependency without changing phone validation. |
| CRA/Storybook/test tooling | Compiler bootstrap code is distinct from a deployed build server. The old build/test dependency tree still contributes audit findings. | Replace CRA and old Storybook/test integrations, then audit the complete new tree. |

Source-map presence establishes delivery, not exploitability. This review does not
claim a production exploit or equate all nineteen critical npm entries with browser
execution. It also does not classify build dependencies as harmless: they execute
during development/CI and require the separate tooling correction.

## Validation and reproduction

Use the Java 25 serial release and the locked npm audit. The frontend has 23 existing
tests and six snapshots covering forms, hooks, API calls and registration. Actual
packaged-browser checks and the final tooling audit are recorded in the dependent
layers. Generated legacy TestNG tests remain compiled but skipped.

On this macOS run the real backend HTTP fixture listened on IPv6: an owned probe
received HTTP 200 over ::1 while its IPv4 request timed out. Five unchanged HTTP
tests failed under the default localhost address preference and passed when the
local test JVM preferred IPv6. The override is in a temporary Gradle init script;
no backend source or global network/JVM setting was changed. CI must still validate
the normal environment.

Primary references:

- [Axios XSRF advisory](https://github.com/advisories/GHSA-wf5p-g6vw-rhxx)
- [Router backslash redirect advisory](https://github.com/advisories/GHSA-wrjc-x8rr-h8h6)
- [Router hydration advisory](https://github.com/advisories/GHSA-337j-9hxr-rhxg)
- [Lodash array-path prototype pollution](https://github.com/advisories/GHSA-f23m-r3pf-42rh)
- [CRA deprecation](https://react.dev/blog/2025/02/14/sunsetting-create-react-app)
