# Code Generation Plan — Unit 1: Play State Engine

## Plan Overview
Implement the Play State Engine across 3 layers: shared logic (cljc), backend routes (clj), and frontend UI (cljs). Follows dependency order — shared first, then backend, then frontend.

---

## Step 1: Datomic Schema Extension
- [ ] Add play state attributes to `src/clj/orcpub/db/schema.clj`
- [ ] 17 new attributes (all optional, EDN-encoded for complex data)

## Step 2: Shared Play State Logic (cljc)
- [ ] Create `src/cljc/orcpub/dnd/e5/play.cljc`
  - Play state initialization (defaults from built character)
  - Damage/heal pipeline (temp HP, clamping, death save activation)
  - Short rest logic (hit dice spending, resource reset)
  - Long rest logic (full restore, hit dice recovery)
  - Resource tracking (use, check bounds)
  - Condition management (add/remove, auto unconscious)
  - Death save logic (success/fail/nat20/nat1 rules)
  - Crimson Rite activation/deactivation (max HP adjustment)
  - Blood Curse use (normal/amplify with HP cost)
  - Equipment state helpers (equipped? check for modifier conditions)
  - Prepared spell validation (max count, always-prepared filtering)

## Step 3: Dice Module Extension (cljc)
- [ ] Extend `src/cljc/orcpub/dice.cljc`
  - Add `parse-notation` (XdY+Z string → map)
  - Add `roll-full` (returns individual die results + total)
  - Add `roll-with-advantage` / `roll-with-disadvantage`

## Step 4: Backend Play Routes (clj)
- [ ] Create `src/clj/orcpub/routes/play.clj`
  - Ownership validation interceptor
  - Input validation (spec-based per action)
  - 14 route handlers (damage, heal, temp-hp, spell-slot, resource, equipment, short-rest, long-rest, condition, death-save, prepared, rite, blood-curse, get-state)
  - Each handler: validate → compute new state → transact to Datomic → return updated state
  - Structured logging on mutations

## Step 5: Route Registration
- [ ] Add play routes to Pedestal route table in `src/clj/orcpub/routes.clj`
- [ ] Add play interceptor chain (auth + ownership + validation)

## Step 6: Frontend — Play State Events (cljs)
- [ ] Create `src/cljs/orcpub/dnd/e5/play_events.cljs`
  - 21 re-frame event handlers
  - Optimistic local update pattern (update app-db → async POST → revert on failure)
  - Dice rolling (client-side only, no server call)

## Step 7: Frontend — Play State Subscriptions (cljs)
- [ ] Create `src/cljs/orcpub/dnd/e5/play_subs.cljs`
  - 12 subscriptions (current-hp, max-hp, temp-hp, spell-slots, resources, conditions, death-saves, equipment-list, active-rites, roll-history, is-play-mode?, play-state)

## Step 8: Frontend — Play Mode View (cljs)
- [ ] Create `src/cljs/orcpub/dnd/e5/views/play.cljs`
  - play-mode-view (main container, layout)
  - hp-tracker (display + controls + death saves)
  - spell-slot-tracker (per-level toggles)
  - class-resource-list (generic resource tracker)
  - conditions-panel (standard toggles + custom)
  - equipment-panel (toggle switches + attunement)
  - actions-panel (rest buttons, blood curse)
  - rite-activation (Blood Hunter weapon + rite picker)

## Step 9: Frontend — Dice Roller Component (cljs)
- [ ] Create `src/cljs/orcpub/dnd/e5/views/dice.cljs`
  - Quick-roll buttons (d4-d20)
  - Custom notation input
  - Advantage/disadvantage toggle
  - Roll result display (individual dice + total)
  - Roll history (scrollable, last 50)

## Step 10: Frontend — Rules Lookup Modal (cljs)
- [ ] Create `src/cljs/orcpub/dnd/e5/views/rules_lookup.cljs`
  - Modal overlay component
  - Fetches entry from compendium (or inline data for spells already loaded)
  - Dismiss on backdrop/X

## Step 11: Route/Navigation Integration
- [ ] Add Play Mode route to `src/cljc/orcpub/route_map.cljc`
- [ ] Add Play Mode page to views dispatch in `src/cljs/orcpub/dnd/e5/views.cljs`
- [ ] Add "Play Mode" button to character list/builder

## Step 12: CSS — Responsive Play Mode
- [ ] Add play mode styles to `src/clj/orcpub/styles/core.clj` (Garden)
  - Mobile-first single column
  - 44px touch targets
  - Sticky HP display
  - Tablet+ grid layout

## Step 13: Tests — Example-Based
- [ ] Create `test/cljc/orcpub/dnd/e5/play_test.cljc`
  - Tests for all 13 business rule groups
  - Edge cases: 0 HP transitions, max slot boundaries, rite activation limits

## Step 14: Tests — Property-Based
- [ ] Create `test/cljc/orcpub/dnd/e5/play_generators.cljc` (reusable generators)
- [ ] Create `test/cljc/orcpub/dnd/e5/play_properties_test.cljc`
  - Invariant properties (HP bounds, slot bounds, resource bounds)
  - Round-trip properties (rite activate/deactivate, serialization)
  - Idempotence (long rest)
- [ ] Create `test/cljc/orcpub/dnd/e5/play_stateful_test.cljc`
  - Random operation sequences maintaining all invariants

---

## Execution Notes
- Steps 1-3 first (foundation)
- Steps 4-5 next (backend)
- Steps 6-12 next (frontend)
- Steps 13-14 last (tests validate everything)
- All code follows existing project conventions (namespace style, indentation, patterns)
