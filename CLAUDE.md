# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Twirl** is a drop-in replacement for SPIN RDF that validates RDF data against SPIN constraints using plain SPARQL execution (CONSTRUCT queries only — ASK constraints are not supported). It does not depend on Jena internals. Constraint violations are returned as `spin:ConstraintViolation` triples.

## Common Commands

```bash
# Build
mvn clean package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=PlainOntModelConstraintTest

# Run a specific test method
mvn test -Dtest=PlainOntModelConstraintTest#methodName

# Release
bash release.sh
```

## Architecture

The library has three main layers:

**Constraint validation** (`constraints/`): `SPINConstraints` is the entry point — it inspects an OntModel for SPIN constraint definitions and executes CONSTRUCT SPARQL queries against RDF instances to produce `ConstraintViolation` objects. Property paths are represented by `ObjectPropertyPath` (forward) and `SubjectPropertyPath` (reverse).

**Model abstractions** (`model/`): Interfaces (`Template`, `Query`, `Command`, `Argument`, `TemplateCall`) and their `impl/` implementations wrap Jena resources to represent SPIN template/query constructs. `model/update/` handles SPARQL Update.

**Vocabulary** (`vocabulary/`): `SP`, `SPIN`, and `SPL` classes define RDF term constants and initialization logic. `SP.init()` must be called before using the library — tests do this in `@BeforeClass`.

**RDF vocabulary files** (`etc/`): Bundled `.ttl`/`.rdf` files for SPIN, SPL, SP, OWL, FOAF etc., referenced via Jena's location mapping (`etc/location-mapping.ttl`).

## Test Setup Pattern

Tests come in two variants for each area: `Plain` (OWL_MEM) and `Inf` (OWL_MEM_RDFS_INF with inference). The `@BeforeClass` in each test initializes the Jena location mapper from `etc/location-mapping.ttl` and calls `SP.init()`.

## Key Dependency

`org.apache.jena:jena-arq:6.0.0` — SPARQL/ARQ engine. Java 17 required.
