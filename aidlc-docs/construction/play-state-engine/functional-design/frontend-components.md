# Unit 1: Play State Engine — Frontend Components

## Component Hierarchy

```
play-mode-view
├── play-header (character name, class, level, mode toggle)
├── hp-tracker
│   ├── hp-display (current/max/temp)
│   ├── hp-controls (damage/heal input + buttons)
│   └── death-save-tracker (successes/failures circles)
├── resource-panel
│   ├── spell-slot-tracker (per-level slot toggles)
│   ├── pact-slot-tracker (warlock-specific)
│   └── class-resource-list (Ki, Rage, etc. — current/max + decrement)
├── conditions-panel
│   ├── standard-condition-toggles
│   └── custom-condition-input
├── equipment-panel
│   ├── equipped-item-list (with toggle switches)
│   ├── attunement-slots (3 slots)
│   └── rite-activation (Blood Hunter — weapon + rite selector)
├── spells-panel
│   ├── prepared-spell-selector (prepared casters)
│   └── spell-list (with tap-for-detail)
├── dice-roller
│   ├── quick-dice-buttons (d4, d6, d8, d10, d12, d20)
│   ├── custom-roll-input (XdY+Z)
│   ├── advantage-toggle
│   └── roll-history (scrollable, last 50)
├── actions-panel
│   ├── short-rest-button (with hit-dice spend dialog)
│   ├── long-rest-button (with confirmation)
│   └── blood-curse-use (normal/amplify buttons)
└── rules-lookup-modal (overlay showing compendium entry)
```

## Key Component Specs

### `play-mode-view`
- **Props**: character-id (from route)
- **Subscriptions**: `::play/built-character`, `::play/play-state`, `::play/mode`
- **Layout**: Single-column scrollable on mobile, 2-column on desktop (left: actions/resources, right: character summary)

### `hp-tracker`
- **Subscriptions**: `::play/current-hp`, `::play/max-hp`, `::play/temp-hp`, `::play/death-saves`
- **Events**: `::play/take-damage`, `::play/heal`, `::play/set-temp-hp`
- **Behavior**: Shows death save tracker only when current-hp = 0

### `spell-slot-tracker`
- **Subscriptions**: `::play/spell-slots` (derived: max per level from built char, used from play state)
- **Events**: `::play/use-spell-slot`, `::play/restore-spell-slot`
- **Render**: Row per spell level (1-9), circles for each slot (filled=used, empty=available), tap to toggle

### `dice-roller`
- **State**: Local atom for input notation, app-db for roll history
- **Events**: `::play/roll-dice`
- **Render**: Quick buttons grid + input field + advantage/disadvantage toggle + results display

### `equipment-panel`
- **Subscriptions**: `::play/equipment-list` (all items with equipped? flag), `::play/attuned-items`
- **Events**: `::play/toggle-equipment`, `::play/toggle-attunement`
- **Behavior**: Toggle triggers entity rebuild → updated stats visible immediately

### `rules-lookup-modal`
- **Props**: entry-type, entry-key
- **Subscriptions**: `::compendium/entry` (fetches from compendium)
- **Behavior**: Overlay on tap of any spell/feature/trait. Dismissed on backdrop tap or X button.

## State Management (re-frame)

### New Subscriptions
```clojure
::play/play-state        ; raw play state from app-db
::play/current-hp        ; derived from play-state
::play/max-hp            ; from built-character
::play/temp-hp           ; from play-state
::play/spell-slots       ; merged: max from built-char + used from play-state
::play/resources         ; merged: definitions from built-char + used from play-state
::play/conditions        ; from play-state
::play/death-saves       ; from play-state
::play/equipment-list    ; all items with equipped? derived from play-state set
::play/active-rites      ; from play-state
::play/roll-history      ; from app-db (not persisted)
::play/is-play-mode?     ; boolean from play-state mode flag
```

### New Events
```clojure
::play/enter-play-mode     [character-id]
::play/exit-play-mode      [character-id]
::play/take-damage         [amount]
::play/heal                [amount]
::play/set-temp-hp         [amount]
::play/use-spell-slot      [level]
::play/restore-spell-slot  [level]
::play/use-resource        [resource-key]
::play/toggle-equipment    [item-key equipped?]
::play/toggle-attunement   [item-key attuned?]
::play/short-rest          [hit-dice-selections]
::play/long-rest           []
::play/set-condition       [condition active?]
::play/add-custom-condition [label]
::play/record-death-save   [roll-value]
::play/roll-dice           [notation advantage?]
::play/set-prepared-spells [class-key spell-keys]
::play/activate-rite       [weapon-key rite-key]
::play/deactivate-rite     [weapon-key]
::play/use-blood-curse     [curse-key amplify?]
::play/open-rules-lookup   [entry-type entry-key]
::play/close-rules-lookup  []
```

### Event Flow
```
User action → dispatch event → 
  1. Update local app-db (instant feedback)
  2. POST to /api/play/:character-id/:action (persist to Datomic)
  3. On success: no-op (already updated locally)
  4. On failure: revert local state, show error
```

This is **optimistic local update with server confirmation** — even though we chose server-side storage (Q2:A), the UX benefits from instant local feedback while the server persists asynchronously.
