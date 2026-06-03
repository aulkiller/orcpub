# Application Design Plan

## Plan Overview
Define new components, their interfaces, and how they integrate with the existing entity/modifier architecture.

---

## Questions

### Question 1: WebSocket Library
For the WebSocket server component, which approach?

A) Use Jetty 11's native JSR 356 WebSocket support (already available via Pedestal 0.7's Jetty — zero new deps)
B) Add a dedicated WebSocket library like Sente (Clojure WebSocket/Ajax abstraction, popular in ClojureScript apps, adds dependency)
C) Use core.async channels over raw Jetty WebSocket (manual but full control)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2: Compendium Storage
Where should compendium content live?

A) In Datomic alongside characters (same DB, simple queries, existing infrastructure)
B) Separate read-only data file (EDN/JSON loaded into memory at startup — fast search, no schema pollution)
C) Datomic but separate database (dedicated compendium DB, clean separation, slightly more operational complexity)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3: Play State vs Build State Separation
How should play state (current HP, used spell slots, conditions) relate to the character entity?

A) Additional attributes on the same entity (simple, one Datomic entity = one character with both build + play state)
B) Separate "play session" entity linked to character (clean separation, supports multiple play sessions per character, more complex queries)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Execution Steps

- [x] Step 1: Define new backend components
- [x] Step 2: Define new shared (cljc) components
- [x] Step 3: Define new frontend components
- [x] Step 4: Define component methods/interfaces
- [x] Step 5: Define services and orchestration
- [x] Step 6: Define component dependencies
- [x] Step 7: Create consolidated application-design.md
