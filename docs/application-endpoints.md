# Endpoint registration contract

The authenticated owner manages `/api/applications/{appId}/endpoints`:
POST creates, GET lists active and inactive records, PUT `/{endpointId}` changes
`description`, DELETE `/{endpointId}` deactivates. Other users receive 403 and
missing applications/endpoints receive 404. Writes require an active application.

Identity is `(applicationId, uppercase HTTP method, application-relative path)`;
a lowercase named `action` is also unique within the application and becomes its
OAuth scope. Supported methods: GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS.
Paths use literal segments or named placeholders such as `/orders/{id}`. Query
strings, fragments, percent escapes, wildcards, traversal and external URLs are
rejected. The receiving application is responsible for mapping its actual routes
to this registered identity; the catalog never calls or proxies endpoints.

Example POST body: `{"httpMethod":"GET","path":"/orders/{id}","action":"orders.read","description":"Read one order"}`.
The server assigns IDs and active status; body application IDs cannot transfer
ownership. Description updates are bounded to 1024 characters. Identity updates
are rejected: deactivate the old record and register a new identity instead, so
existing permissions cannot silently change meaning. Deactivation is permanent;
method/path and action names remain reserved, including inactive rows. Conflicts
return 409. Migration V8 creates an empty catalog without changing applications.

These defaults implement the model/API decisions in #38 under Miguel's request
to complete all remaining issues. Directed grants and OAuth build on these IDs;
scheduling and endpoint execution are outside this contract.

## Catalog bounds

GET returns at most `limit` records (default 100, supported 1..100). For the next page,
pass `afterId` equal to the last returned `endpointId`; cursors are nonnegative.
The response remains a JSON array. Clients must paginate rather than assuming a
complete catalog in one response. Creation allows at most 1,000 endpoint identities
per application, including inactive entries, and serializes quota checks with writes.
At the quota, register another application or design an explicit archival process.
