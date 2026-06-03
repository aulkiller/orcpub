# Unit 1: Play State Engine — Business Rules

## BR-1: Hit Points

| Rule | Logic |
|------|-------|
| BR-1.1 | `current-hp` cannot exceed `max-hp + max-hp-modifier` |
| BR-1.2 | `current-hp` can go below 0 only as "massive damage" indicator; clamp to 0 for display |
| BR-1.3 | `temp-hp` depletes before `current-hp` on damage |
| BR-1.4 | `temp-hp` does not stack — new temp HP replaces if higher |
| BR-1.5 | Healing cannot restore temp HP — only current HP |
| BR-1.6 | Reaching 0 HP activates death save tracking (if not already dead) |
| BR-1.7 | Receiving healing while at 0 HP resets death save counters and sets HP to healed amount |

## BR-2: Spell Slots

| Rule | Logic |
|------|-------|
| BR-2.1 | Slot counts derived from built character (class level tables + multiclass rules) |
| BR-2.2 | `slots-used[level]` cannot exceed `slots-max[level]` |
| BR-2.3 | Warlock pact slots tracked separately (different count, short-rest recovery) |
| BR-2.4 | Using a slot: increment `slots-used[level]` |
| BR-2.5 | Short rest: reset pact slots only (`pact-slots-used = 0`) |
| BR-2.6 | Long rest: reset all spell slots (`all slots-used = 0`) |

## BR-3: Class Resources

| Rule | Logic |
|------|-------|
| BR-3.1 | Resources defined by modifier engine (`:resource` modifier type with `:max`, `:reset-on`) |
| BR-3.2 | `used` cannot exceed `max` |
| BR-3.3 | Short rest: reset all resources where `reset-on ∈ #{:short-rest}` |
| BR-3.4 | Long rest: reset all resources where `reset-on ∈ #{:short-rest :long-rest}` |
| BR-3.5 | Some resources have `hp-cost` (e.g., Blood Curse amplification) — deduct HP on use |

## BR-4: Equipment Toggle

| Rule | Logic |
|------|-------|
| BR-4.1 | Equipping item adds its key to `::play/equipped-items` |
| BR-4.2 | Unequipping removes the key |
| BR-4.3 | Equipment change triggers `entity/build` with equipment state affecting modifier conditions |
| BR-4.4 | Attuned items tracked separately — max 3 attuned at once |
| BR-4.5 | Unattuning removes both attunement and equipped state |
| BR-4.6 | Modifiers with condition `(equipped? item-key)` activate/deactivate based on `equipped-items` set |

## BR-5: Short Rest

| Rule | Logic |
|------|-------|
| BR-5.1 | Player chooses how many hit dice to spend (0 to available) |
| BR-5.2 | Each hit die spent: roll hit die + CON modifier = HP healed |
| BR-5.3 | Available hit dice = total hit dice - hit-dice-used (per die size) |
| BR-5.4 | Reset: pact slots, short-rest resources |
| BR-5.5 | Do NOT reset: regular spell slots, long-rest resources, HP (beyond hit die healing) |

## BR-6: Long Rest

| Rule | Logic |
|------|-------|
| BR-6.1 | Restore `current-hp` to `max-hp + max-hp-modifier` |
| BR-6.2 | Restore all spell slots (all `slots-used` = 0) |
| BR-6.3 | Restore all resources (both short-rest and long-rest types) |
| BR-6.4 | Recover `floor(total-hit-dice / 2)` hit dice (minimum 1) |
| BR-6.5 | Reset death save counters |
| BR-6.6 | Reset `blood-curses-used` |
| BR-6.7 | Do NOT reset: conditions (DM manages manually), equipped items, active rites |

## BR-7: Conditions

| Rule | Logic |
|------|-------|
| BR-7.1 | Standard conditions are a fixed set (toggle on/off) |
| BR-7.2 | Custom conditions are free-text labels |
| BR-7.3 | Unconscious auto-applied when HP reaches 0 (if death saves triggered) |
| BR-7.4 | Unconscious auto-removed when HP restored above 0 |
| BR-7.5 | Conditions are informational — no auto-mechanical effects on rolls (DM adjudicates) |

## BR-8: Death Saving Throws

| Rule | Logic |
|------|-------|
| BR-8.1 | Activated when `current-hp` reaches 0 |
| BR-8.2 | Track successes (0-3) and failures (0-3) independently |
| BR-8.3 | 3 successes → stabilized (HP stays at 0, tracker resets, unconscious remains) |
| BR-8.4 | 3 failures → dead (display indicator) |
| BR-8.5 | Natural 20 (roll=20) → regain 1 HP, reset tracker, remove unconscious |
| BR-8.6 | Natural 1 (roll=1) → count as 2 failures |
| BR-8.7 | Receiving any healing → reset tracker, set HP to healed amount, remove unconscious |
| BR-8.8 | Taking damage at 0 HP → add 1 failure (or 2 if critical hit) |

## BR-9: Prepared Spells

| Rule | Logic |
|------|-------|
| BR-9.1 | Only for prepared casters: Cleric, Wizard, Druid, Paladin, Artificer |
| BR-9.2 | Max prepared = ability modifier + class level (varies by class) |
| BR-9.3 | Changing prepared list triggers `entity/build` (passive spell effects recalculate) |
| BR-9.4 | Cannot prepare more than max |
| BR-9.5 | Domain/oath/circle spells are always-prepared (not counted toward limit, not removable) |

## BR-10: Crimson Rite (Blood Hunter)

| Rule | Logic |
|------|-------|
| BR-10.1 | Activate on a specific weapon — costs `hemocraft-die` from max HP (not current HP) |
| BR-10.2 | `max-hp-modifier` decreases by hemocraft die size when rite activated |
| BR-10.3 | Active rite adds damage type to weapon (modifier recalculation) |
| BR-10.4 | Deactivating restores max HP (`max-hp-modifier` increases back) |
| BR-10.5 | Hemocraft die size scales: d4 (1-4), d6 (5-10), d8 (11-16), d10 (17-20) |
| BR-10.6 | Max simultaneous rites: 1 (levels 1-10), 2 (11+) |
| BR-10.7 | If `current-hp > new max`, clamp current to new max |

## BR-11: Blood Curses

| Rule | Logic |
|------|-------|
| BR-11.1 | Uses per rest derived from class level (modifier engine) |
| BR-11.2 | Normal use: decrement `blood-curses-used` |
| BR-11.3 | Amplified use: decrement `blood-curses-used` AND deduct HP (hemocraft die) |
| BR-11.4 | Reset on short rest (class feature) |
| BR-11.5 | Cannot use if `blood-curses-used >= max` |

## BR-12: Dice Rolling

| Rule | Logic |
|------|-------|
| BR-12.1 | Parse XdY+Z notation (X dice of Y sides plus Z modifier) |
| BR-12.2 | Each die result: random integer in [1, Y] |
| BR-12.3 | Advantage: roll 2d20, keep highest |
| BR-12.4 | Disadvantage: roll 2d20, keep lowest |
| BR-12.5 | Roll history: append-only list (newest first), capped at 50 entries |
| BR-12.6 | History is session-only (frontend app-db, not persisted to Datomic) |

## BR-13: Play/Build Mode Transition

| Rule | Logic |
|------|-------|
| BR-13.1 | Switching to Play Mode: character must be saved (has `:db/id`) |
| BR-13.2 | Switching to Build Mode: play state preserved (can return to Play Mode) |
| BR-13.3 | Build Mode changes trigger full rebuild — play state remains but may become stale if max values change |
| BR-13.4 | After Build Mode edit + return to Play Mode: clamp `current-hp` if it now exceeds new max |

## Testable Properties (PBT-01)

| Property | Category | Description |
|----------|----------|-------------|
| Rest idempotence | Idempotence | `long-rest(long-rest(state)) = long-rest(state)` |
| HP clamping invariant | Invariant | `0 <= current-hp <= max-hp + max-hp-modifier` at all times |
| Spell slot invariant | Invariant | `slots-used[level] <= slots-max[level]` for all levels |
| Resource invariant | Invariant | `resource.used <= resource.max` for all resources |
| Equipment toggle round-trip | Round-trip | `unequip(equip(state, item)) = state` (modifiers cancel) |
| Rite HP round-trip | Round-trip | `deactivate-rite(activate-rite(state, w, r)) restores max-hp` |
| Death save terminal | Invariant | `successes <= 3 AND failures <= 3` |
| Dice bounds | Invariant | Every die result in `[1, sides]` |
| Prepared spells bound | Invariant | `count(prepared) <= max-prepared` |
