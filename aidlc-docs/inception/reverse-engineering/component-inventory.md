# Component Inventory

## Application Packages

### Backend (JVM-only)
- `orcpub.server` — Production entrypoint
- `orcpub.system` — Component system assembly
- `orcpub.pedestal` — HTTP server + interceptors
- `orcpub.routes` — All HTTP route handlers (monolithic)
- `orcpub.routes.party` — Party CRUD handlers
- `orcpub.routes.folder` — Folder CRUD handlers
- `orcpub.pdf` — PDF character sheet generation
- `orcpub.email` — Transactional email sending
- `orcpub.security` — Login rate limiting
- `orcpub.config` — Environment configuration
- `orcpub.datomic` — Database connection lifecycle
- `orcpub.csp` — Content Security Policy
- `orcpub.index` — SPA HTML shell rendering
- `orcpub.privacy` — Legal pages
- `orcpub.time` — Date/time utilities
- `orcpub.styles.core` — Garden CSS generation
- `orcpub.dnd.e5.modifier-macros` — JVM macro definitions

### Frontend (Browser-only)
- `orcpub.core` — App entry point (React 18 createRoot)
- `orcpub.character-builder` — Main character builder UI
- `orcpub.dnd.e5.views` — Page-level view components
- `orcpub.dnd.e5.events` — re-frame event handlers (369)
- `orcpub.dnd.e5.subs` — re-frame subscriptions (184)
- `orcpub.dnd.e5.db` — App state defaults
- `orcpub.dnd.e5.autosave-fx` — Autosave effect handler
- `orcpub.dnd.e5.spell-subs` — Spell subscriptions
- `orcpub.dnd.e5.equipment-subs` — Equipment subscriptions
- `orcpub.dnd.e5.content-reconciliation` — Import conflict handling
- `orcpub.dnd.e5.import-validation` — Homebrew validation
- `orcpub.dnd.e5.views.import-log` — Import log overlay
- `orcpub.dnd.e5.views.conflict-resolution` — Conflict resolution UI

### Shared (JVM + Browser)
- `orcpub.entity` — Entity build engine (CORE)
- `orcpub.entity-spec` — Modifier macro DSL
- `orcpub.entity.strict` — Datomic serialization
- `orcpub.modifiers` — Modifier system
- `orcpub.template` — Template specification
- `orcpub.dice` — Dice rolling utilities
- `orcpub.common` — Shared utilities
- `orcpub.route-map` — URL route definitions
- `orcpub.registration` — Registration validation
- `orcpub.errors` — Error helpers
- `orcpub.views-aux` — View auxiliary functions
- `orcpub.pdf-spec` — PDF field specs
- `orcpub.components` — Reusable UI helpers

### D&D 5e Domain (Shared)
- `orcpub.dnd.e5.character` — Character model + accessors
- `orcpub.dnd.e5.modifiers` — D&D modifier factories
- `orcpub.dnd.e5.template` — D&D template construction
- `orcpub.dnd.e5.spells` — Spell database
- `orcpub.dnd.e5.spell-lists` — Class spell lists
- `orcpub.dnd.e5.monsters` — Monster database
- `orcpub.dnd.e5.equipment` — Equipment data
- `orcpub.dnd.e5.weapons` — Weapon properties
- `orcpub.dnd.e5.armor` — Armor data
- `orcpub.dnd.e5.magic-items` — Magic item specs
- `orcpub.dnd.e5.races` — Race definitions
- `orcpub.dnd.e5.classes` — Class definitions
- `orcpub.dnd.e5.backgrounds` — Background definitions
- `orcpub.dnd.e5.feats` — Feat definitions
- `orcpub.dnd.e5.skills` — Skill definitions
- `orcpub.dnd.e5.combat` — Combat specs
- `orcpub.dnd.e5.encounters` — Encounter specs
- `orcpub.dnd.e5.party` — Party specs
- `orcpub.dnd.e5.selections` — Selection helpers
- `orcpub.dnd.e5.options` — Option utilities
- `orcpub.dnd.e5.display` — Display formatting
- `orcpub.dnd.e5.character.equipment` — Equipment logic
- `orcpub.dnd.e5.character.random` — Random generation
- `orcpub.dnd.e5.templates.ua_*` — Unearthed Arcana content (8 modules)

### Fork/Configuration
- `orcpub.fork.branding` (clj + cljs) — App name, logos, limits
- `orcpub.fork.auth` (clj) — JWT config
- `orcpub.fork.integrations` (clj + cljs) — 3rd-party hooks
- `orcpub.fork.user-data` (clj) — User enrichment
- `orcpub.fork.privacy-content` (clj) — Privacy text
- `orcpub.fork.user-tier` (cljs) — User tier logic
- `orcpub.fork.splash` (cljc) — Splash page content

## Infrastructure

- `docker-compose.yaml` — 3-container deployment (datomic + orcpub + nginx)
- `docker/Dockerfile` — Multi-stage Java 21 build
- `deploy/nginx.conf.template` — Reverse proxy + SSL
- `deploy/start.sh` — Container entrypoint
- `.devcontainer/` — VS Code devcontainer config
- `.github/workflows/` — CI pipeline

## Test Packages
- `test/clj/` — Backend unit tests (210 tests, 963 assertions)
- `test/cljc/` — Shared logic tests
- `test/cljs/` — Frontend tests (minimal)
- `test/docker/` — Docker integration tests

## Dev Tooling
- `dev/user.clj` — REPL helpers + CLI entrypoint
- `scripts/` — Shell scripts (start, stop, migrate, setup)
- `menu` — Interactive service launcher

## Total Count
- **Total Source Files**: 113
- **Backend (clj)**: 26
- **Shared (cljc)**: 55
- **Frontend (cljs)**: 18
- **Frontend entry (web/)**: 1
- **Dev**: 1
- **Infrastructure**: 6 config files
- **Test**: 12+ test files
