# Unit 1: Play State Engine — Domain Entities

## Play State (extends Character Entity)

New Datomic attributes added to the existing character entity:

```clojure
;; Hit Points
::play/current-hp          ; long — current hit points
::play/temp-hp             ; long — temporary hit points
::play/max-hp-modifier     ; long — max HP adjustment (e.g., Crimson Rite reduction)

;; Spell Slots (used counts per level)
::play/spell-slots-used    ; ref (component) → map of level→used-count
::play/pact-slots-used     ; long — warlock pact slots used

;; Class Resources (generic tracking)
::play/resources-used      ; ref (many, component) → [{:key :ki, :used 3}, ...]

;; Conditions
::play/conditions          ; keyword (many) — active conditions set
::play/custom-conditions   ; string (many) — custom condition labels

;; Death Saves
::play/death-save-successes ; long (0-3)
::play/death-save-failures  ; long (0-3)

;; Equipment State
::play/equipped-items      ; keyword (many) — keys of currently equipped items
::play/attuned-items       ; keyword (many) — keys of attuned items (max 3)

;; Crimson Rite
::play/active-rites        ; ref (many, component) → [{:weapon-key :longsword, :rite-key :rite-of-the-flame}]

;; Blood Curses
::play/blood-curses-used   ; long — uses expended this rest

;; Prepared Spells (override per class)
::play/prepared-spells     ; ref (many, component) → [{:class-key :cleric, :spell-keys [...]}]

;; Hit Dice
::play/hit-dice-used       ; ref (many, component) → [{:die-size 8, :used 2}]

;; Mode
::play/mode                ; keyword — :build or :play (UI state flag)

;; Dice Roll History (transient, not persisted — app-db only)
;; Roll history lives in frontend app-db, not Datomic
```

## Resource Definition (derived from built character)

Resources are **not stored** — they're derived from the built character via the modifier engine. The play state only tracks **used counts**.

```clojure
;; A resource definition (derived at build time via modifiers)
{:key        :ki-points        ; unique key
 :name       "Ki Points"       ; display name
 :max        10                ; max uses (from built character)
 :reset-on   :short-rest       ; :short-rest | :long-rest | :turn | :minute | :hour
 :hp-cost    nil               ; optional HP cost per use
 :amplify-hp nil}              ; optional HP cost for amplification

;; Play state tracks only:
{:key :ki-points, :used 3}     ; 3 of 10 used
```

## Condition Set (standard D&D 5e)

```clojure
(def standard-conditions
  #{:blinded :charmed :deafened :frightened :grappled
    :incapacitated :invisible :paralyzed :petrified
    :poisoned :prone :restrained :stunned :unconscious
    :exhaustion-1 :exhaustion-2 :exhaustion-3
    :exhaustion-4 :exhaustion-5 :exhaustion-6})
```

## Dice Roll (frontend only — app-db)

```clojure
{:id          (random-uuid)
 :notation    "2d6+3"
 :dice        [{:sides 6, :result 4} {:sides 6, :result 2}]
 :modifier    3
 :total       9
 :type        :damage           ; :attack | :damage | :save | :check | :custom
 :advantage?  false
 :timestamp   (js/Date.)}
```

## Crimson Rite Activation

```clojure
{:weapon-key  :longsword
 :rite-key    :rite-of-the-flame
 :damage-type :fire
 :hp-reduction (hemocraft-die-size level)}  ; reduces max HP
```

## Encounter Initiative Entry (used by encounter builder but referenced here for dice)

```clojure
{:name        "Gandalf"
 :type        :player           ; :player | :monster
 :initiative  18
 :dex-mod     2}
```
