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