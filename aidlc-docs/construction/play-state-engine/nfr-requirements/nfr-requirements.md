# Unit 1: Play State Engine — NFR Requirements

## Security Requirements (SECURITY extension — blocking)

| ID | Requirement | SECURITY Rule |
|----|-------------|---------------|
| SEC-1.1 | All play state REST endpoints require JWT authentication | SECURITY-08 |
| SEC-1.2 | Object-level auth: player can only mutate their own character's play state | SECURITY-08 |
| SEC-1.3 | Input validation on all play state endpoints (HP delta bounds, slot level range, resource keys exist) | SECURITY-05 |
| SEC-1.4 | Error responses must not expose internals (stack traces, DB details) | SECURITY-09, SECURITY-15 |
| SEC-1.5 | Structured logging for play state mutations (timestamp, user, character-id, action, values) | SECURITY-03 |
| SEC-1.6 | Rate limiting on play state endpoints (prevent abuse via rapid mutations) | SECURITY-11 |
| SEC-1.7 | Fail closed: on auth/validation failure, deny mutation and return 4xx | SECURITY-15 |

## Testing Requirements (PBT extension — blocking)

| ID | Requirement | PBT Rule |
|----|-------------|----------|
| PBT-1.1 | Round-trip: `to-strict(from-strict(play-state)) = play-state` for serialization | PBT-02 |
| PBT-1.2 | Invariant: `0 <= current-hp <= max-hp + max-hp-modifier` after any operation | PBT-03 |
| PBT-1.3 | Invariant: `slots-used[level] <= slots-max[level]` after any operation | PBT-03 |
| PBT-1.4 | Invariant: `resource.used <= resource.max` after any operation | PBT-03 |
| PBT-1.5 | Idempotence: `long-rest(long-rest(state)) = long-rest(state)` | PBT-04 |
| PBT-1.6 | Round-trip: `deactivate-rite(activate-rite(state, w, r))` restores max-hp | PBT-02 |
| PBT-1.7 | Invariant: `death-save-successes <= 3 AND failures <= 3` | PBT-03 |
| PBT-1.8 | Invariant: dice results in `[1, sides]` for all rolls | PBT-03 |
| PBT-1.9 | Framework: clojure.test.check (already in deps) with custom generators for play state | PBT-09 |
| PBT-1.10 | Stateful PBT: random sequence of play operations maintains all invariants | PBT-06 |

## Performance Requirements

| ID | Requirement |
|----|-------------|
| PERF-1.1 | Play state mutations complete within 100ms server-side (Datomic transact + response) |
| PERF-1.2 | Entity rebuild after equipment toggle completes within 200ms (existing build performance) |
| PERF-1.3 | Dice rolling is client-side only (zero network latency) |
| PERF-1.4 | Play Mode initial load: fetch play state + build character within 500ms |

## Reliability Requirements

| ID | Requirement |
|----|-------------|
| REL-1.1 | Optimistic UI: local state updates instantly, server confirms asynchronously |
| REL-1.2 | On server failure: revert local state, show error message, retry available |
| REL-1.3 | Play state never partially persisted (atomic Datomic transactions) |
| REL-1.4 | Global error handler catches all unhandled exceptions in play state operations |

## Tech Stack (for this unit — no new dependencies)

| Component | Technology | Rationale |
|-----------|-----------|-----------|
| Backend | Pedestal interceptors + Datomic | Existing stack, additive routes |
| Shared logic | Pure cljc functions | Runs on both client and server |
| Frontend state | re-frame events + subscriptions | Existing pattern |
| Dice RNG | `Math/random` (cljs) / `java.util.Random` (clj) | Standard, adequate for tabletop |
| Testing | clojure.test + test.check | Already in project deps |
