# Unit 1: Play State Engine — Infrastructure Design

## Overview

Unit 1 requires **no new infrastructure components** — it extends the existing stack:
- Same Pedestal server (new routes added)
- Same Datomic database (new schema attributes)
- Same Docker containers (no new services)
- Same nginx (no proxy changes needed for REST)

## Datomic Schema Extension

New attributes added via schema transact (additive — no breaking changes):

```clojure
;; Play State Schema (appended to existing db/schema.clj)
[;; Hit Points
 {:db/ident ::play/current-hp
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one
  :db/doc "Current hit points in play mode"}
 {:db/ident ::play/temp-hp
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one}
 {:db/ident ::play/max-hp-modifier
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one
  :db/doc "Max HP adjustment (negative for Crimson Rite cost)"}
 
 ;; Spell Slots
 {:db/ident ::play/spell-slots-used
  :db/valueType :db.type/string  ; EDN-encoded map {1 2, 3 1} (level→used)
  :db/cardinality :db.cardinality/one}
 {:db/ident ::play/pact-slots-used
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one}
 
 ;; Resources
 {:db/ident ::play/resources-used
  :db/valueType :db.type/string  ; EDN-encoded [{:key :ki, :used 3}]
  :db/cardinality :db.cardinality/one}
 
 ;; Conditions
 {:db/ident ::play/conditions
  :db/valueType :db.type/string  ; EDN-encoded set #{:poisoned :frightened}
  :db/cardinality :db.cardinality/one}
 {:db/ident ::play/custom-conditions
  :db/valueType :db.type/string  ; EDN-encoded ["Cursed" "Hexed"]
  :db/cardinality :db.cardinality/one}
 
 ;; Death Saves
 {:db/ident ::play/death-save-successes
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one}
 {:db/ident ::play/death-save-failures
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one}
 
 ;; Equipment State
 {:db/ident ::play/equipped-items
  :db/valueType :db.type/string  ; EDN-encoded set #{:longsword :shield}
  :db/cardinality :db.cardinality/one}
 {:db/ident ::play/attuned-items
  :db/valueType :db.type/string  ; EDN-encoded set #{:flame-tongue}
  :db/cardinality :db.cardinality/one}
 
 ;; Crimson Rite
 {:db/ident ::play/active-rites
  :db/valueType :db.type/string  ; EDN-encoded [{:weapon-key :x :rite-key :y :hp-cost 4}]
  :db/cardinality :db.cardinality/one}
 
 ;; Blood Curses
 {:db/ident ::play/blood-curses-used
  :db/valueType :db.type/long
  :db/cardinality :db.cardinality/one}
 
 ;; Prepared Spells
 {:db/ident ::play/prepared-spells
  :db/valueType :db.type/string  ; EDN-encoded [{:class-key :cleric :spell-keys [...]}]
  :db/cardinality :db.cardinality/one}
 
 ;; Hit Dice
 {:db/ident ::play/hit-dice-used
  :db/valueType :db.type/string  ; EDN-encoded [{:die-size 8 :used 2}]
  :db/cardinality :db.cardinality/one}
 
 ;; Mode
 {:db/ident ::play/mode
  :db/valueType :db.type/keyword
  :db/cardinality :db.cardinality/one
  :db/doc ":build or :play"}]
```

**Design note**: Complex nested data (maps, vectors of maps) stored as EDN strings in single attributes rather than as component entities. This is simpler for play state that's always read/written atomically and avoids schema explosion. Trade-off: no individual field queries on nested data — acceptable since play state is always loaded as a unit.

## Route Registration

New routes added to existing Pedestal route table:

```clojure
;; Added to orcpub.routes (or new orcpub.routes.play namespace)
["/api/play/:character-id/damage"       :post play/take-damage]
["/api/play/:character-id/heal"         :post play/heal]
["/api/play/:character-id/temp-hp"      :post play/set-temp-hp]
["/api/play/:character-id/spell-slot"   :post play/use-spell-slot]
["/api/play/:character-id/resource"     :post play/use-resource]
["/api/play/:character-id/equipment"    :post play/toggle-equipment]
["/api/play/:character-id/short-rest"   :post play/short-rest]
["/api/play/:character-id/long-rest"    :post play/long-rest]
["/api/play/:character-id/condition"    :post play/set-condition]
["/api/play/:character-id/death-save"   :post play/record-death-save]
["/api/play/:character-id/prepared"     :post play/set-prepared-spells]
["/api/play/:character-id/rite"         :post play/toggle-rite]
["/api/play/:character-id/blood-curse"  :post play/use-blood-curse]
["/api/play/:character-id/state"        :get  play/get-play-state]
```

All routes use interceptor chain: `[check-auth, validate-ownership, validate-input, handler]`

## Backward Compatibility

- All new attributes are optional (no default required)
- Existing characters have nil play state — Play Mode initializes defaults on first entry:
  - `current-hp` = built character's max HP
  - All `*-used` = 0
  - `conditions` = empty set
  - `equipped-items` = derived from current build selections
  - `mode` = :build (existing behavior unchanged)
- No schema migration needed — Datomic schema is additive
- Existing API endpoints unchanged

## Docker — No Changes

Unit 1 requires no Docker/infrastructure changes:
- Same `docker-compose.yaml`
- Same container count (datomic + app + nginx)
- Schema applied automatically on app startup (existing `orcpub.datomic` component)
