# ADR-002: Single-Row Heartbeat for Leader Election

## Status
Accepted

## Context
In a multi-pod deployment, only one k8s pod should run the scheduler poll loop to avoid duplicate job execution.

## Decision
A single-row leader_info table with leader_id, expiry_at, and leader_epoch provides lease-based leader election. Takeover condition: UPDATE WHERE expiry_at < NOW(). Affected rows = 1 means election won.

## Consequences
Zombie leader risk exists if a pod suffers a long GC pause. Mitigated by epoch-based fencing on execution writes.

## Alternatives Considered
ZooKeeper / etcd -- rejected for V1 (violates zero-infrastructure goal).
