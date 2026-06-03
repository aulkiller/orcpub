# User Stories

## Epic 1: Interactive Play Mode

### PLAY-01: Enter Play Mode
**As a** Player, **I want to** switch my character to Play Mode **so that** I can track live session state without modifying my build.

**Acceptance Criteria**:
- Play Mode is accessible from any saved character
- No campaign membership required
- Character's computed stats display correctly
- Build Mode selections are read-only in Play Mode
- Switching back to Build Mode preserves play state

### PLAY-02: Track Hit Points
**As a** Player, **I want to** increment and decrement my HP **so that** I can track damage and healing during combat.

**Acceptance Criteria**:
- Current HP, max HP, and temporary HP displayed
- Increment/decrement buttons adjust current HP
- HP cannot exceed max HP (unless temporary HP)
- Temporary HP tracked separately (depletes first)
- HP changes persist to server (Datomic)
- Reducing to 0 HP triggers death save tracking state

### PLAY-03: Track Spell Slots
**As a** Player, **I want to** mark spell slots as used **so that** I know which slots remain available.

**Acceptance Criteria**:
- Spell slots displayed per level (1-9)
- Each slot toggleable between available/used
- Slot counts match character build (class levels + multiclass rules)
- Warlock pact slots displayed separately (short rest recovery)
- State persists to server

### PLAY-04: Roll Dice
**As a** Player, **I want to** roll any combination of dice **so that** I can resolve actions without physical dice.

**Acceptance Criteria**:
- Input accepts XdY+Z notation (e.g., 2d6+3)
- Common rolls available as quick buttons (d20, d4-d12)
- Roll result displayed with individual die values
- Roll history maintained for the session
- Advantage/disadvantage roll (2d20 keep highest/lowest)

### PLAY-05: Track Class Resources
**As a** Player, **I want to** track class-specific resources (Ki, Sorcery Points, Rage, etc.) **so that** I know my remaining uses.

**Acceptance Criteria**:
- Resources display with current/max counts
- Decrement on use, increment on recovery
- Resource names and max values derived from character build (modifier engine)
- Resources grouped by recovery type (short rest, long rest, per turn)
- State persists to server

### PLAY-06: Toggle Equipment
**As a** Player, **I want to** equip/unequip items **so that** my stats auto-recalculate (AC, attack bonuses, etc.).

**Acceptance Criteria**:
- Equipment list shows all owned items with equipped/unequipped toggle
- Equipping a shield recalculates AC via modifier engine
- Equipping a magic weapon updates attack/damage bonuses
- Unequipping removes the modifier effects
- Attunement slots tracked (max 3)
- Changes persist and trigger rebuilt character

### PLAY-07: Short Rest
**As a** Player, **I want to** take a short rest **so that** appropriate resources reset.

**Acceptance Criteria**:
- "Short Rest" button available in Play Mode
- Hit dice spending UI (choose how many to spend, roll for healing)
- Short-rest resources reset to max (Ki, Warlock slots, etc.)
- Long-rest-only resources remain unchanged
- Hit dice pool decrements by number spent
- Confirmation before executing (shows what will reset)

### PLAY-08: Long Rest
**As a** Player, **I want to** take a long rest **so that** HP, spell slots, and resources fully recover.

**Acceptance Criteria**:
- "Long Rest" button available in Play Mode
- HP restored to maximum
- All spell slots restored
- All long-rest resources reset
- Half hit dice recovered (round down)
- Death saving throws reset
- Confirmation before executing

### PLAY-09: Apply Conditions
**As a** Player, **I want to** apply/remove conditions **so that** my character state reflects active effects.

**Acceptance Criteria**:
- Standard conditions available (blinded, charmed, deafened, frightened, etc.)
- Toggle on/off per condition
- Active conditions visually indicated on character display
- Advantage/disadvantage markers derived from conditions where applicable
- Custom conditions can be added (text label)

### PLAY-10: Death Saving Throws
**As a** Player, **I want to** track death saving throws **so that** I know my stabilization progress.

**Acceptance Criteria**:
- Death save tracker appears when HP reaches 0
- Track successes (0-3) and failures (0-3)
- 3 successes = stabilized (reset tracker, set HP to 0 stable)
- 3 failures = dead indicator
- Natural 20 = regain 1 HP, reset tracker
- Natural 1 = 2 failures
- Receiving healing resets tracker and sets HP

### PLAY-11: Prepared Spell Management
**As a** Player, **I want to** change my prepared spells **so that** I can adjust my spell list after a long rest.

**Acceptance Criteria**:
- Available only for prepared casters (Cleric, Wizard, Druid, Paladin, Artificer)
- Shows all class spells eligible for preparation
- Max prepared count displayed (ability mod + class level)
- Toggling prepared triggers modifier recalculation (passive spell effects)
- Known-spell casters (Bard, Sorcerer, Warlock) see fixed list (not editable in Play Mode)

### PLAY-12: HP-Cost Activations (Crimson Rite)
**As a** Player, **I want to** activate Crimson Rite on a weapon **so that** it deals bonus elemental damage at the cost of my HP.

**Acceptance Criteria**:
- Rite activation toggle per weapon
- Activating deducts hemocraft die from max HP (not current HP per RAW — reduces max)
- Active rite adds damage type to weapon display
- Modifier engine recalculates affected stats
- Deactivation restores max HP
- Multiple rites on different weapons supported (at higher levels)

### PLAY-13: Amplifiable Abilities (Blood Curses)
**As a** Player, **I want to** use Blood Curses with optional amplification **so that** I can choose between a normal or empowered effect at HP cost.

**Acceptance Criteria**:
- Blood Curse uses displayed (current/max per rest)
- "Use" button decrements count
- "Amplify" option available (deducts HP in addition to use)
- Amplification HP cost displayed before confirmation
- Uses reset on appropriate rest type

### PLAY-14: In-Play Rules Lookup
**As a** Player, **I want to** tap a spell/ability/feature in Play Mode **so that** I can read its full rules text without leaving the view.

**Acceptance Criteria**:
- Spells, features, traits, and abilities are tappable
- Tap opens overlay/modal with full rules text from compendium
- Includes: description, casting time, range, components, duration (for spells)
- Works for class features, racial traits, feat descriptions
- Dismiss overlay returns to play view
- Works for Blood Hunter features (Crimson Rites, Blood Curses)

### PLAY-15: Play State Persistence
**As a** Player, **I want to** close the app and return later with my play state intact **so that** I can resume mid-session.

**Acceptance Criteria**:
- All play state (HP, slots, resources, conditions, equipped items) persists in Datomic
- Reopening character in Play Mode shows last saved state
- Works across devices (same account)
- No manual save button needed (auto-persists on change)

---

## Epic 2: Real-time Sync

### SYNC-01: WebSocket Connection
**As a** Player, **I want to** have my character state sync in real-time **so that** my DM can see my updates without me reporting them verbally.

**Acceptance Criteria**:
- WebSocket connection established when in a campaign
- Connection authenticated via JWT
- Auto-reconnect on disconnect
- Connection status indicator visible in UI

### SYNC-02: DM Full Party View
**As a** DM, **I want to** see all player characters in my campaign in real-time **so that** I can track the party's state without asking.

**Acceptance Criteria**:
- Dashboard shows all campaign characters
- Each character displays: name, class, level, current/max HP, active conditions, spell slots remaining
- Updates push from players within best-effort latency
- No manual refresh needed

### SYNC-03: Player Party Summary
**As a** Player, **I want to** see a summary of my party members **so that** I have tactical awareness during combat.

**Acceptance Criteria**:
- Party sidebar shows other campaign members
- Summary per member: name, HP percentage, active conditions
- Does not expose full character details (spells, resources) of other players
- Updates in real-time via WebSocket

### SYNC-04: Disconnect/Reconnect
**As a** Player, **I want to** automatically reconnect after losing connection **so that** I don't lose sync with the campaign.

**Acceptance Criteria**:
- Auto-reconnect with exponential backoff
- On reconnect, full state resync (no missed updates)
- Offline changes queue and push on reconnect
- Visual indicator during disconnected state

### SYNC-05: DM Initiative Broadcast
**As a** DM, **I want to** broadcast initiative order to all players **so that** everyone knows the turn sequence.

**Acceptance Criteria**:
- DM sets initiative order (from encounter tracker)
- Initiative order pushes to all campaign members via WebSocket
- Players see "whose turn it is" indicator
- DM can advance turn (next in order highlighted)

---

## Epic 3: Content Compendium

### COMP-01: Search Spells
**As a** Player, **I want to** search spells by name, level, school, or class **so that** I can find spell information quickly.

**Acceptance Criteria**:
- Full-text search on spell names and descriptions
- Filter by: level (0-9), school, class, casting time, concentration
- Results show: name, level, school, casting time, one-line summary
- Clicking a result shows full spell details

### COMP-02: Search Monsters
**As a** DM, **I want to** search monsters by name, CR, type, or size **so that** I can find stat blocks for encounter building.

**Acceptance Criteria**:
- Full-text search on monster names
- Filter by: CR range, type (aberration, beast, etc.), size, environment
- Results show: name, CR, type, size, HP
- Clicking a result shows full stat block

### COMP-03: Search Items
**As a** Player, **I want to** search items (weapons, armor, magic items) **so that** I can look up item properties.

**Acceptance Criteria**:
- Full-text search on item names
- Filter by: type (weapon/armor/wondrous/potion), rarity, attunement required
- Results show: name, type, rarity
- Clicking a result shows full item description and properties

### COMP-04: Bundled SRD Data
**As a** Player, **I want to** access spell/monster/item data without internet **so that** the self-hosted instance works offline.

**Acceptance Criteria**:
- SRD data ships with the application in Datomic
- Available immediately after fresh install (no external fetch needed)
- Covers: all SRD spells, monsters, items, class features

### COMP-05: Open5e Sync
**As a** DM, **I want to** optionally sync content from Open5e **so that** I get updated/additional content.

**Acceptance Criteria**:
- Admin/user-triggered sync (not automatic)
- Imports: spells, monsters, items from Open5e API
- Merges with existing data (no duplicates)
- Sync status/progress displayed
- Works incrementally (only fetch new/changed)

### COMP-06: Non-SRD Content
**As a** Player, **I want to** add Blood Hunter and other non-SRD content to the compendium **so that** all my class features are available for lookup.

**Acceptance Criteria**:
- Homebrew/custom content can be added to compendium
- Same search/filter applies to all content regardless of source
- Content source labeled (SRD, Open5e, Homebrew, Custom)
- Existing .orcbrew import populates compendium entries

### COMP-07: Link to Character Builder
**As a** Player, **I want to** add a spell/item from the compendium to my character **so that** I don't have to find it again in the builder.

**Acceptance Criteria**:
- "Add to character" action available on compendium entries
- Opens character builder with the item/spell pre-selected
- Works for: spells (add to known/prepared), items (add to inventory)

---

## Epic 4: Campaign Management

### CAMP-01: Create Campaign
**As a** DM, **I want to** create a campaign **so that** I can organize a group of players.

**Acceptance Criteria**:
- Campaign has a name
- Creator is automatically the DM (owner)
- Campaign persists in Datomic
- Appears in DM's campaign list

### CAMP-02: Generate Invite
**As a** DM, **I want to** generate an invite code/link **so that** players can join my campaign.

**Acceptance Criteria**:
- Generate unique invite code (short, shareable)
- Code has optional expiry
- DM can revoke/regenerate codes
- Link format: `/join/<code>`

### CAMP-03: Join Campaign
**As a** Player, **I want to** join a campaign with an invite code **so that** my character syncs with the DM.

**Acceptance Criteria**:
- Enter invite code or follow invite link
- Select which character to assign to the campaign
- Character appears in DM's campaign view
- Player can only have one character per campaign

### CAMP-04: View Campaign Members
**As a** DM, **I want to** see all players and their assigned characters **so that** I know who's in the campaign.

**Acceptance Criteria**:
- List all members with: username, character name, class, level
- Show online/offline status
- Show character HP/conditions summary

### CAMP-05: Remove Player
**As a** DM, **I want to** remove a player from the campaign **so that** I can manage my group.

**Acceptance Criteria**:
- DM can remove any member
- Removed player loses real-time sync access
- Their character is unlinked from campaign
- Confirmation required before removal

### CAMP-06: Leave Campaign
**As a** Player, **I want to** leave a campaign voluntarily **so that** I can disconnect from a group.

**Acceptance Criteria**:
- "Leave" option available in campaign view
- Character unlinked from campaign
- WebSocket subscription ends
- DM sees player removed from roster

---

## Epic 5: Encounter Builder

### ENC-01: Create Encounter
**As a** DM, **I want to** create an encounter by selecting monsters **so that** I can prepare combat.

**Acceptance Criteria**:
- Search/select monsters from compendium
- Set quantity per monster type
- Encounter saved with a name
- Optionally tied to a campaign

### ENC-02: CR Difficulty Rating
**As a** DM, **I want to** see encounter difficulty rating **so that** I can balance combat.

**Acceptance Criteria**:
- Difficulty calculated using DMG XP thresholds (Easy/Medium/Hard/Deadly)
- Based on party size and levels (from campaign or manual input)
- Updates dynamically as monsters are added/removed
- Shows total XP and per-player threshold

### ENC-03: Initiative Tracker
**As a** DM, **I want to** track initiative order **so that** I can run combat turn-by-turn.

**Acceptance Criteria**:
- Roll initiative for all participants (players + monsters)
- Sorted list displayed (highest first)
- Current turn highlighted
- "Next turn" button advances
- Supports delay/ready actions (reorder)

### ENC-04: Monster HP Tracking
**As a** DM, **I want to** track monster HP during combat **so that** I know when they're defeated.

**Acceptance Criteria**:
- Each monster instance has current/max HP
- Decrement on damage, increment on healing
- 0 HP = defeated (visually indicated, optionally hidden)
- Supports multiple instances of same monster type

### ENC-05: Encounter State Persistence
**As a** DM, **I want to** save encounter state mid-combat **so that** I can resume next session.

**Acceptance Criteria**:
- Initiative order, monster HP, turn position persist
- Resumable from campaign page
- "End encounter" clears state

---

## Epic 6: Party Tools

### PARTY-01: Shared Party View
**As a** Player, **I want to** see my party members' basic info **so that** I have context during play.

**Acceptance Criteria**:
- Shows: name, race, class, level, HP (current/max) for each party member
- Accessible from campaign page
- Updates in real-time (if sync active)

### PARTY-02: Session Notes
**As a** DM, **I want to** take session notes within the campaign **so that** I have a record of events.

**Acceptance Criteria**:
- Text area for notes within campaign view
- Timestamped entries
- Both DM and players can add notes
- Notes persist per campaign
- Chronological display

### PARTY-03: Party Overview Dashboard
**As a** DM, **I want to** see a party overview dashboard **so that** I can quickly assess the group's status.

**Acceptance Criteria**:
- Total party level, average HP percentage
- Combined spell slot availability
- Active conditions across all members
- Quick link to each character's full Play Mode view

---

## Epic 7: Responsive UI

### UI-01: Responsive Play Mode
**As a** Player, **I want to** use Play Mode on my phone **so that** I can use it at the table.

**Acceptance Criteria**:
- Play Mode usable on 375px+ screen width
- Touch-friendly buttons (44px+ tap targets)
- Single-column layout on mobile
- Critical info (HP, conditions, resources) visible without scrolling
- Dice roller accessible with one tap

### UI-02: Responsive Builder
**As a** Player, **I want to** use the character builder on mobile **so that** I can make quick edits anywhere.

**Acceptance Criteria**:
- Existing 3-tab mobile layout improved
- Option selections navigable via touch
- Character preview accessible
- No horizontal scrolling required

### UI-03: Responsive Compendium
**As a** Player, **I want to** browse the compendium on mobile **so that** I can look things up at the table.

**Acceptance Criteria**:
- Search bar prominent at top
- Results list scrollable
- Detail view readable on small screens
- Filters collapsible (don't consume screen space by default)

---

## Epic 8: Deployment

### DEPLOY-01: Single Compose File
**As a** DM, **I want to** run the full stack with a single docker-compose command **so that** setup is simple.

**Acceptance Criteria**:
- `docker compose up` starts all services (Datomic + app + nginx)
- No manual steps required after initial setup
- Health checks configured for all containers
- Includes WebSocket support (no additional proxy config)

### DEPLOY-02: Multi-Arch Images
**As a** DM, **I want to** run on ARM or x86 **so that** I can use any server (Raspberry Pi, cloud VMs, etc.).

**Acceptance Criteria**:
- Docker images built for linux/amd64 and linux/arm64
- Same docker-compose.yaml works on both architectures
- CI builds multi-arch manifests via docker buildx

### DEPLOY-03: Push to Personal DockerHub
**As a** DM, **I want to** push built images to my own DockerHub **so that** I can pull from any machine.

**Acceptance Criteria**:
- CI/CD pipeline pushes to configurable DockerHub registry
- Registry name/org configurable via environment variable or secret
- Tags include version + `latest`
- Push triggered on main branch merge or manual workflow dispatch
