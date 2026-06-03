# Unit 1: Play State Engine — NFR Design

## Security Patterns

### Authentication Interceptor (reuse existing)
```
play-state-interceptor-chain:
  [check-auth, validate-character-ownership, validate-play-input, handle-play-action]
```

- `check-auth`: Existing JWT interceptor (already in routes.clj)
- `validate-character-ownership`: New — queries Datomic to confirm character's `::se/owner` matches JWT username
- `validate-play-input`: New — schema-based validation per action type (spec or manual)
- `handle-play-action`: Route handler

### Input Validation Schema
```clojure
;; Per-endpoint validation
{:take-damage    {:amount (s/and int? #(<= -999 % 999))}
 :heal           {:amount (s/and int? pos? #(<= % 999))}
 :set-temp-hp    {:amount (s/and int? #(<= 0 % 999))}
 :use-spell-slot {:level (s/and int? #(<= 1 % 9))}
 :use-resource   {:resource-key keyword?}
 :toggle-equip   {:item-key keyword?, :equipped? boolean?}
 :short-rest     {:hit-dice [{:die-size #{4 6 8 10 12}, :count pos-int?}]}
 :set-condition  {:condition keyword?, :active? boolean?}
 :death-save     {:roll (s/and int? #(<= 1 % 20))}
 :activate-rite  {:weapon-key keyword?, :rite-key keyword?}
 :blood-curse    {:curse-key keyword?, :amplify? boolean?}}
```

### Rate Limiting
- 60 requests/minute per character per user (covers rapid combat turns)
- Use existing `orcpub.security` atom-based approach (per-character key)

### Structured Logging
```clojure
{:timestamp (instant)
 :level :info
 :action :play/take-damage
 :user "username"
 :character-id 12345
 :params {:amount 8}
 :result {:current-hp 15}}
```

## PBT Architecture

### Generator Definitions (`test/cljc/orcpub/dnd/e5/play_generators.cljc`)
```clojure
;; Reusable generators for play state
gen-play-state     ; valid play state map
gen-hp-delta       ; bounded damage/heal values
gen-spell-level    ; 1-9
gen-resource-key   ; from a set of valid keys
gen-condition      ; from standard-conditions set
gen-dice-notation  ; valid XdY+Z
gen-equipment-key  ; from valid item keys
gen-play-operation ; random play state operation (for stateful PBT)
```

### Test Structure
```
test/cljc/orcpub/dnd/e5/
├── play_test.cljc           ; Example-based tests for all business rules
├── play_properties_test.cljc ; PBT: invariants, round-trips, idempotence
└── play_stateful_test.cljc   ; PBT: random operation sequences
```

### Stateful PBT Model (PBT-06)
```
Model State: {hp, max-hp, temp-hp, slots-used, resources-used, conditions, death-saves}
Commands: [take-damage, heal, set-temp-hp, use-slot, restore-slot, use-resource,
           short-rest, long-rest, set-condition, death-save, activate-rite, deactivate-rite]
Postconditions: All invariants from business-rules.md checked after each command
```

## Performance Design

### Optimistic UI Flow
```
User clicks "Take 8 Damage":
  1. [Instant] Dispatch ::play/take-damage → update app-db locally
  2. [Instant] Subscriptions recompute → UI reflects new HP
  3. [Async]   POST /api/play/:id/damage {amount: 8}
  4. [On success] No-op (state already correct)
  5. [On failure] Dispatch ::play/revert-state → restore previous state, show error toast
```

### Entity Rebuild Optimization
- Equipment toggle triggers full `entity/build` — existing 500ms debounce protects against rapid toggles
- Play state attributes (HP, slots, conditions) do NOT trigger rebuild — they're tracked separately
- Only equipment/rite/prepared-spell changes trigger rebuild (modifier-affecting changes)

## Reliability Patterns

### Atomic Persistence
```clojure
;; All play state changes in a single Datomic transaction
(d/transact conn
  [{:db/id character-id
    ::play/current-hp new-hp
    ::play/temp-hp new-temp}])
;; Either all attributes update or none do
```

### Error Handling Chain
```
Interceptor chain catches exceptions:
  ValidationError → 400 {error: "Invalid input", details: {...}}
  AuthorizationError → 403 {error: "Not authorized"}
  DatomicError → 500 {error: "Server error"} (log full details, don't expose)
  Unexpected → 500 {error: "Server error"} (global handler, log + alert)
```

## Responsive Design (CSS)

### Play Mode Breakpoints
```css
/* Mobile-first: single column, touch-friendly */
@media (max-width: 767px) {
  .play-mode { flex-direction: column; }
  .play-button { min-height: 44px; min-width: 44px; }
  .hp-display { font-size: 2rem; }
}

/* Tablet+: two column layout */
@media (min-width: 768px) {
  .play-mode { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }
}
```

### Critical Mobile Requirements
- HP tracker visible without scrolling (sticky or top-positioned)
- Dice roller accessible with one tap (floating action button on mobile)
- Touch targets minimum 44px
- Spell slot circles large enough to tap individually
