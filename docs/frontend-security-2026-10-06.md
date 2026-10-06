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

## Build and browser dependency correction layer

Replace CRA with Vite 8.3.3, Vitest 5.0.3 and Storybook 10.6.1. Gradle pins Node
24.21.0 LTS and npm 11.21.0; npm ci resolves strict peer dependencies without
legacy-peer-deps, force fixes or audit exclusions. React 18.3.1 and patched Router
7.18.4 retain the existing routes. TypeScript and hook lint checks run before the
production build. The complete fresh npm audit reports zero findings across all
severities, including development and build dependencies. Release now checks this
full tree; CI also builds Storybook independently.

The website remains packaged in Spring's jar. Vite assets use /static/assets so
they retain the public Spring Security resource boundary. Development binds to
loopback and GET /login loads the current SPA, while POST /login reaches Spring.
An emitted-module inventory is written to build-reports/browser-packages.json
outside public assets. The inspected production inventory contains Axios browser
modules and no Node HTTP adapter, follow-redirects or form-data modules. Storybook
builds do not overwrite this inventory or enter the deployed jar.

React createRoot revealed a registration bug: StrictMode's setup/cleanup/setup
cycle left the hook's mounted ref permanently false. A regression test reproduced
the completed registration response being ignored. Effect setup now restores the
ref; the test verifies completion and one API call for duplicate submissions.
The suite has 24 tests and six reviewed Vitest snapshots. Snapshot changes retain
the form labels/routes and account for the compatible MUI update and router link
attributes. Serial release validates 195 backend tests, frontend checks and jar
packaging; the unchanged local IPv6 test override described above still applies.

The main app chunk is approximately 505 kB minified / 165 kB gzip. The build keeps
its size warning visible; route splitting remains a separate performance change.
Packaged-browser registration, login and session checks are recorded in the next
layer rather than inferred from unit test or compilation results.

## Server-derived browser session layer

The old ProfileContext parsed ProfileKey at module initialization and mutated a
plain object after login. A Chrome run against the actual packaged jar reproduced
three defects: a forged stored identity appeared while GET /api/profile returned
401; malformed JSON left an empty page; and successful login did not refresh the
toolbar. These are UI session/state defects, not demonstrated backend authorization
bypasses. The server denied the forged profile throughout the reproduction.

ProfileProvider now hydrates in-memory identity from the cookie-authenticated
GET /api/profile, removes the legacy key without parsing it and revalidates on
window focus. Probes have a five-second request timeout and one active request;
unmount and successful login abort older probes. Revision/lifecycle guards stop
late responses from replacing a newer login. Probe failures clear the displayed
identity. No profile data or authentication token is written to browser storage.
The login effect depends on the profile and stable setter, so context updates
render the toolbar without causing a setter/render loop.

Validation of this layer:

- Serial release: 195 backend tests without failures/errors/skips, 32 frontend
  tests, six snapshots, TypeScript/hook lint, jar packaging and Storybook passed.
  The unchanged macOS IPv6 test override remains local only.
- Full fresh npm audit, including Playwright 1.63.0: zero findings at all severities.
- Four actual Chrome scenarios passed: forged and malformed storage; complete
  registration/login/retry/reload/profile edit/logout; and exact CORS allowlist.
  Missing CSRF returned 403; fresh CSRF allowed mutations. Logout returned 204,
  the following profile read returned 401 and the UI cleared the old identity.
  The allowed cross-origin preflight returned its exact allow-origin and credential
  headers; denied preflight/real requests returned 403. The observed browser fetch
  sent the unconfigured Origin and could not read the response.
- The disposable database confirmed exactly one registered account. Runtime DDL
  was denied. The fixture removed its browser, JVM descendants and container after
  successful and failed runs. CI now runs this fixture serially after retention.

Reproduce with the pinned Gradle runtime and tools/validation/browser.py after
release; see authorization-website/README.md. PostgreSQL is limited to 1 CPU,
512 MiB, no additional swap, 128 PIDs and 20 connections. The host JVM uses a
256 MiB maximum heap and ActiveProcessorCount=2 (a JVM hint, not an OS CPU quota).
The fixture accepts no existing database/target and binds dev HTTP to loopback.
It does not measure production TLS, sustained/mixed load or multi-instance session
sharing. Those are separate checks from this frontend correction. The main app
chunk still produces the documented size warning.
