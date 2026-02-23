# ADR-001: Java 21 as Minimum JDK Version

## Status
Accepted

## Context
Kronflow uses virtual threads for job execution concurrency.

## Decision
JDK 21 is the minimum supported version.

## Consequences
Teams on JDK 17 or below cannot use Kronflow without upgrading.

## Alternatives Considered
JDK 17 with platform threads -- rejected because thread pool sizing
becomes a user-facing configuration burden.
