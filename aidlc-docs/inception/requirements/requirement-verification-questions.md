# Requirements Verification Questions

Please answer the following questions to help clarify the requirements for evolving OrcPub into a D&D Beyond-like platform.

---

## Question 1: First Milestone Scope
Given the 6 target features, how much should the FIRST milestone (v1.0) include?

A) Interactive play mode only (dice, HP/resource tracking, spell slots, equipment toggle, rests) — tightly scoped, shippable fast
B) Interactive play mode + real-time sync (play mode + WebSocket DM view) — higher impact but more infrastructure work
C) Interactive play mode + content compendium (play mode + searchable SRD database) — broader but independent features
D) All of features 1-3 together (play mode + sync + compendium) — larger first release
X) Other (please describe after [Answer]: tag below)

[Answer]: D

---

## Question 2: Interactive Play Mode — Character State Persistence
When a player tracks HP, spell slots, and resources during a session, where should that state live?

A) Server-side only (Datomic) — state persists across devices, requires connectivity
B) Client-side only (localStorage/app-db) — works offline, no server roundtrip, but single-device
C) Client-first with server sync (optimistic local updates, background sync to Datomic) — best UX, most complexity
D) Configurable per-campaign (DM chooses: local-only casual mode vs. synced persistent mode)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 3: Real-time Sync — Architecture
For WebSocket-based live updates, what communication pattern do you prefer?

A) Broadcast model: any character change pushes to all party members (simple, chatty)
B) Subscription model: DM subscribes to specific characters; players see only their own (efficient, privacy-respecting)
C) Room-based: campaign "rooms" where all members see all updates within that room (Discord-like)
D) Hybrid: subscription-based with DM having full-party visibility, players seeing party-level summary only
X) Other (please describe after [Answer]: tag below)

[Answer]: D

---

## Question 4: Content Compendium — Data Source
For the searchable spell/monster/item database, which data source strategy?

A) Bundle SRD data locally (ship with app, no external API dependency, works offline)
B) Fetch from Open5e API at runtime (always current, requires internet, rate limits)
C) Hybrid: bundle SRD baseline, allow optional Open5e sync for updates
D) Import-based: provide a tool to import Open5e data into Datomic on setup (one-time fetch, self-hosted data)
X) Other (please describe after [Answer]: tag below)

[Answer]: C

---

## Question 5: Multi-user Authorization Model
The current system has simple ownership (your characters, your parties). With DM/player collaboration, what's the permission model?

A) Campaign-based: DM creates campaign, invites players. DM sees all characters in campaign; players see their own + party summary
B) Share-link based: players generate share links for their characters; DM collects links (simpler, no formal campaign entity)
C) Role-based: explicit DM/Player/Spectator roles within a campaign with granular permissions
D) Trust-based: any party member can view any character in the party (no permission granularity)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 6: Mobile-Responsive UI — Approach
For the mobile UI overhaul, what's the target?

A) Responsive CSS only — same SPA, reflows for small screens (least effort, adequate for most use)
B) Mobile-first redesign of play mode only — builder stays desktop-focused, play mode optimized for phone-at-table
C) Full progressive web app (PWA) — responsive + service worker + offline + installable
D) Responsive + separate simplified mobile view for play mode (dual-mode UI)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 7: Encounter Builder — CR Calculation Scope
The encounter builder needs CR calculations. How sophisticated?

A) Basic XP thresholds (easy/medium/hard/deadly per DMG tables) — straightforward
B) XP thresholds + adjusted XP for monster count multipliers
C) Full encounter balancing including party composition analysis (levels, damage output, healing capacity)
D) XP thresholds + optional "encounter difficulty" simulation (Monte Carlo or heuristic)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 8: Backward Compatibility
Existing users have characters stored in Datomic. How strict is backward compatibility?

A) Must be seamless — all existing characters work without user action
B) One-time automated migration acceptable (schema migration on upgrade, no user action needed)
C) Migration with user notification acceptable (users see "upgrade character" prompt)
D) Breaking changes acceptable for major version (existing deployments can stay on old version)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 9: Performance Targets
For the interactive play mode with real-time sync, what are acceptable latency targets?

A) Best-effort — whatever Datomic + WebSocket latency gives us is fine for tabletop play
B) Sub-second: character state changes must reflect in DM view within 1 second
C) Real-time feel: <200ms for local updates, <500ms for remote sync
D) Optimistic UI: instant local feedback, eventual consistency for sync (no hard latency target)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 10: Security Baseline Extension
Should security extension rules be enforced for this project?

A) Yes — enforce all security rules as blocking constraints (recommended for production-grade applications with user auth and multi-user features)
B) No — skip security rules (suitable for PoCs, prototypes, and experimental projects)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---

## Question 11: Property-Based Testing Extension
Should property-based testing (PBT) rules be enforced for this project?

A) Yes — enforce all PBT rules as blocking constraints (recommended for the modifier engine which has complex business logic, data transformations, and stateful components)
B) Partial — enforce PBT rules only for pure functions and serialization round-trips (suitable for testing the entity/modifier engine without requiring PBT for UI code)
C) No — skip all PBT rules (suitable for simple CRUD applications)
X) Other (please describe after [Answer]: tag below)

[Answer]: A

---
