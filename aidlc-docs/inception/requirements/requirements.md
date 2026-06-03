# Requirements — DMV Platform Evolution (v1.0)

## Intent Analysis Summary

- **User Request**: Evolve OrcPub/DMV from a character builder into a D&D Beyond-like platform with interactive play, real-time sync, and content compendium
- **Request Type**: New Feature (major platform expansion)
- **Scope Estimate**: System-wide — new components, new infrastructure (WebSockets), schema extensions, UI additions
- **Complexity Estimate**: Complex — multiple interacting features, real-time infrastructure, multi-user authorization
- **Requirements Depth**: Standard

---

## Functional Requirements

### FR-1: Interactive Play Mode

Characters must transition between **Build Mode** (existing) and **Play Mode** (new). Play Mode is fully standalone — no campaign membership required. It functions as an interactive character sheet for at-the-table play.

| ID | Requirement |
|----|-------------|
| FR-1.1 | System provides a "Play Mode" view for any saved character, distinct from the builder. No campaign membership required. |
| FR-1.2 | Play Mode displays current HP, max HP, temporary HP with increment/decrement controls |
| FR-1.3 | Play Mode tracks spell slot usage per level (used/available) with reset on rest |
| FR-1.4 | Play Mode provides dice rolling (any combination: XdY+Z) with roll history |
| FR-1.5 | Play Mode tracks class-specific resources (Ki points, sorcery points, rage uses, etc.) via the modifier engine's resource definitions |
| FR-1.6 | Equipment can be toggled equipped/unequipped in Play Mode, triggering modifier recalculation (e.g., equipping a shield recalculates AC) |
| FR-1.7 | Short Rest resets: hit dice spending (heal), short-rest resources |
| FR-1.8 | Long Rest resets: HP to max, all spell slots, all long-rest resources, recover half hit dice (round down) |
| FR-1.9 | Conditions can be applied/removed (advantage, disadvantage, standard conditions like frightened, poisoned) |
| FR-1.10 | Death saving throws tracked (successes/failures, auto-reset on healing) |
| FR-1.11 | All play state persists server-side (Datomic) — accessible across devices |
| FR-1.12 | Play Mode preserves full compatibility with Build Mode — switching between modes never loses data |
| FR-1.13 | Prepared spells can be changed in Play Mode (e.g., after long rest). Swapping prepared spells triggers modifier recalculation for any passive effects. Known-spell casters (Bard, Sorcerer, Warlock) are unaffected — their list is fixed from Build Mode. |
| FR-1.14 | HP-cost resource activations supported (e.g., Blood Hunter's Crimson Rite: toggle on weapon, deducts HP, adds damage type). Activating/deactivating triggers modifier recalculation. |
| FR-1.15 | Amplifiable limited-use abilities supported (e.g., Blood Curses: spend a use normally, or amplify at HP cost for enhanced effect). |
| FR-1.16 | Spells, abilities, curses, and features are tappable/clickable in Play Mode to show their full rules text (description, mechanics, duration, range, components). Content sourced from the compendium. |

### FR-2: Real-time Sync (WebSocket)

| ID | Requirement |
|----|-------------|
| FR-2.1 | Server exposes a WebSocket endpoint for real-time character state updates |
| FR-2.2 | DM has full visibility into all characters in their campaign (HP, spell slots, conditions, equipment state) |
| FR-2.3 | Players see their own full character + party-level summary (party member names, HP percentage, conditions) |
| FR-2.4 | Character state changes push to subscribed clients within best-effort latency |
| FR-2.5 | Connection handles disconnect/reconnect gracefully (resync on reconnect) |
| FR-2.6 | WebSocket connections are authenticated (JWT-based, same auth system as REST) |

### FR-3: Content Compendium

| ID | Requirement |
|----|-------------|
| FR-3.1 | Searchable database of SRD spells with filtering by level, school, class, casting time |
| FR-3.2 | Searchable database of SRD monsters with filtering by CR, type, size, environment |
| FR-3.3 | Searchable database of SRD items (weapons, armor, adventuring gear, magic items) |
| FR-3.4 | Full-text search across all content types |
| FR-3.5 | SRD baseline data bundled with the application (works offline/self-hosted without internet) |
| FR-3.6 | Optional sync mechanism to import updates from Open5e API into local database |
| FR-3.7 | Homebrew and non-SRD content (e.g., Blood Hunter, UA, third-party classes) can be added to the compendium — no content restrictions for personal/self-hosted use |
| FR-3.8 | Compendium entries link to the character builder (e.g., "Add this spell to my character") |

### FR-4: Campaign & Authorization

Campaigns are an **optional layer** on top of Play Mode. Play Mode works fully standalone. Campaigns add real-time sync visibility and shared party features.

| ID | Requirement |
|----|-------------|
| FR-4.1 | DM creates a campaign with a name |
| FR-4.2 | DM generates invite codes/links for players to join |
| FR-4.3 | Players join a campaign by entering invite code and assigning a character |
| FR-4.4 | DM sees all characters assigned to the campaign |
| FR-4.5 | Players see their own character + party summary within the campaign |
| FR-4.6 | DM can remove players from campaign |
| FR-4.7 | Players can leave campaigns voluntarily |
| FR-4.8 | Campaign entity persists in Datomic with owner (DM) and member list |

### FR-5: Encounter Builder (Basic)

| ID | Requirement |
|----|-------------|
| FR-5.1 | DM can create encounters by selecting monsters from the compendium |
| FR-5.2 | Encounter difficulty calculated using DMG XP thresholds (Easy/Medium/Hard/Deadly) |
| FR-5.3 | Party level composition used to determine thresholds |
| FR-5.4 | Initiative tracker: roll initiative for all participants, display ordered list |
| FR-5.5 | Monster HP tracking during encounter (decrement/increment) |
| FR-5.6 | Encounter state persists per campaign session |

### FR-6: Party Tools

| ID | Requirement |
|----|-------------|
| FR-6.1 | Shared party view showing all campaign members' characters (name, race, class, level, HP) |
| FR-6.2 | Session notes: DM and players can add timestamped notes to a campaign |
| FR-6.3 | Party overview accessible from campaign page |

---

## Non-Functional Requirements

### NFR-1: Backward Compatibility
- All existing characters must work without user action after upgrade
- New schema attributes added additively (no breaking changes to existing entities)
- Existing API endpoints remain unchanged; new features use new endpoints

### NFR-2: Performance
- Best-effort latency for WebSocket sync (no hard sub-second target)
- Entity build engine performance must not degrade with play state attributes
- Compendium search returns results within 500ms for typical queries

### NFR-3: Security (Extension Enabled — Full Enforcement)
- All SECURITY-01 through SECURITY-15 rules apply as blocking constraints
- WebSocket connections must be authenticated and authorized
- Campaign membership enforced server-side (no client-trust)
- Input validation on all new API endpoints

### NFR-4: Testing (PBT Extension Enabled — Full Enforcement)
- All PBT-01 through PBT-10 rules apply as blocking constraints
- Framework: `clojure.test.check` (already a dependency)
- Modifier engine round-trips, entity serialization, dice rolling, and CR calculation must have property-based tests

### NFR-5: Architecture Constraints
- Must preserve entity/template/modifier architecture
- Clojure/ClojureScript stack (no JS/TS rewrite)
- Self-hostable via Docker — single `docker-compose.yaml` runs the full stack (Datomic + app + nginx)
- CI/CD pipeline builds and pushes multi-arch images (linux/amd64 + linux/arm64) to user's own DockerHub registry
- No copyrighted content constraint removed — this is for personal/self-hosted use
- EPL-2.0 compatible dependencies only

### NFR-6: Mobile Responsiveness
- Responsive CSS approach (same SPA, reflows for small screens)
- Play Mode must be usable on mobile (phone-at-table scenario)
- Builder remains desktop-focused (no mobile redesign)

### NFR-7: Data Integrity
- Play state changes are atomic (no partial updates to HP + spell slots)
- WebSocket messages must not corrupt character state on concurrent edits
- Campaign membership changes must be consistent (no orphaned references)

---

## Architectural Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Play state storage | Server-side (Datomic) | Enables cross-device access and real-time sync; aligns with existing persistence model |
| Real-time protocol | WebSocket (Pedestal/Jetty) | Bidirectional, low-latency push; Jetty 11 supports JSR 356 WebSocket |
| Sync pattern | Hybrid subscription (DM=full, player=own+summary) | Balances privacy, bandwidth, and DM needs |
| Content data | Bundled SRD + optional Open5e sync | Self-hosted works offline; optional sync for updates |
| Authorization | Campaign-based (DM invites players) | Natural D&D table structure; clear ownership model |
| CR calculation | Basic DMG XP thresholds | Covers 90% of use cases; extensible later |
| Mobile strategy | Responsive CSS only | Lowest effort, adequate for play mode use case |
| Backward compat | Seamless (additive schema) | Existing users must not be disrupted |
| PBT framework | clojure.test.check 1.1.1 | Already in deps, mature, supports generators + shrinking |

---

## Milestone Scope: v1.0

The first release includes features 1-3 together:
1. **Interactive Play Mode** (FR-1)
2. **Real-time Sync** (FR-2)
3. **Content Compendium** (FR-3)

Plus the supporting infrastructure:
- **Campaign entity** (FR-4.1–4.8) — required for real-time sync authorization
- **Basic encounter builder** (FR-5) — leverages compendium data
- **Party tools** (FR-6) — lightweight addition on top of campaigns

This is a large milestone but the features are synergistic: campaigns enable sync, sync makes play mode collaborative, compendium feeds encounters.
