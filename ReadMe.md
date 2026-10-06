# Overview

## The Authorization Hub for "Downtown Bakeries"

Within this file I'd like to explain the approach taken in the folder structure
and architecture of the System.

There's a design document in the design-docs folder.

### authorization-service Module

This module includes all the **code** for setting up the Java-Server.
The idea of the Server is to have a very basic Authentitcation and Authorization Mechanism
for the **Downtown Bakeries**.

### authorization-service-shared-lib Module

Module that represents the Shared Requests. However, it seems that this folder
might get removed in later releases. The main idea was to use it as part of the
Functional-Tests - a way of having something shared between FunctionalTests and
the actual service but since we're using [swagger](https://swagger.io/) for
documenting our API, it might seem a good idea just to remove the models.

### functional-tests Module

Simple module that includes all the required tests
for our Aplication.

## Technologies Used

* Spring Boot
* Lombok
* Spring Security
* OAuth-2.0 Specification
* Docker
* Heroku for hosting our Application
* CloudFlare for helping us setting up TLS/SSL Certificates.

## Application input contract

Application creation requires a `redirectUrl` containing an absolute HTTP or HTTPS
origin. A root slash and a valid port are allowed; other paths, queries, fragments,
credentials and relative URLs are rejected with HTTP 400. Localhost origins are
allowed for local development. Creation and explicit URL updates use the same rule.
Existing stored URLs are preserved when an update omits `redirectUrl`.

The optional `appType` accepts exactly `Service` or `WebService`. Creation defaults
to `Service`; updates preserve the stored type when omitted. Migration V6 backfills
existing applications as `Service` and constrains stored values to these two types.

Example application payload:

```json
{
  "application": {
    "appName": "Example service",
    "shortDescription": "Service registration",
    "redirectUrl": "https://example.com",
    "appType": "WebService"
  }
}
```

## Service-to-service authorization

1. [Issue and rotate application client credentials](docs/client-credentials.md).
2. [Register receiving endpoints and actions](docs/application-endpoints.md).
3. [Grant directed access between applications](docs/directed-grants.md).
4. [Issue scoped OAuth tokens and enforce them in a receiving service](docs/oauth-client-credentials.md).

Production requires an explicit `OAUTH_ISSUER` and HTTPS. The development profile
allows HTTP only on loopback; see the OAuth deployment configuration.

## Runtime and audit

Use the [supported Java/Spring build and deployment configuration](docs/runtime-upgrade.md).
The [original-design and security audit](docs/security-audit-2026-10-05.md) records implemented behavior, reproduced defects and remaining user-role capabilities.
