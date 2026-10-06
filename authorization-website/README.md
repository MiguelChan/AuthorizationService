# Authorization website

The React SPA uses Vite, Vitest and Storybook. Gradle downloads pinned Node 24.21.0
LTS/npm 11.21.0 and runs npm ci with strict peer resolution. Use those versions
when running npm commands directly.

From the repository root:

```sh
./gradlew release -Pskip-functional-tests --no-daemon --max-workers=1
./gradlew :authorization-website:buildStorybook --no-daemon --max-workers=1
python3 tools/validation/browser.py
```

The release checks TypeScript, React hook lint, the full dependency audit and tests,
then packages build/ into the Spring jar. Browser tests require Java 25, Docker
and installed Google Chrome. They use the built jar, a disposable PostgreSQL with
1 CPU/512 MiB and a DML-only runtime role. The JVM has a 256 MiB maximum heap and
an ActiveProcessorCount hint of 2; it has no host OS CPU/RAM quota. The fixture
accepts no existing target/database and removes its processes/browser/container on
success or failure. Evidence logs are retained in the printed temporary directory.
Loopback dev HTTP is used here; production TLS validation is separate in
../tools/validation/run.py.

From this directory with the pinned Node/npm available:

```sh
npm ci
npm start
npm test
npm run test:watch
npm run test:coverage
npm run build
npm run audit
npm run storybook
npm run build-storybook
```

Development binds to 127.0.0.1:3000 and proxies fixed API/OAuth/login routes to the
development backend at localhost:8094. GET /login serves the current SPA; POST
/login reaches Spring. Storybook uses localhost:6006. Neither server runs in the
deployed jar. Compiled resources retain /static/** URLs and source maps are not
published. build-reports/browser-packages.json inventories emitted app modules
outside the public assets; Storybook builds do not replace that report.

Profile identity is kept in memory and hydrated from GET /api/profile using the
server cookie session. Startup and focus probes have a five-second timeout, allow
one active request and abort on unmount/new login. The legacy ProfileKey is removed
without parsing it; no profile data or authentication token is persisted in browser
storage. Session failures clear the displayed profile. Backend authorization remains
the boundary for protected operations. Login/registration obtain fresh CSRF headers.

See ../docs/frontend-security-2026-10-06.md for dependency reachability, reproduced
bugs, validation evidence and the remaining main-chunk size warning.
