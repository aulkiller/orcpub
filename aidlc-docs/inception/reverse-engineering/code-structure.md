# Code Structure

## Build System
- **Type**: Leiningen (project.clj)
- **CLJS Build**: figwheel-main (dev hot-reload + prod builds)
- **CSS**: Garden (Clojure DSL → CSS compilation)
- **Production**: `lein build` → clean + fig:prod + uberjar

## Source Organization

```
src/
├── clj/orcpub/          # Server-only (JVM)
│   ├── server.clj       # Entrypoint (-main)
│   ├── system.clj       # Component system (Datomic + Pedestal)
│   ├── config.clj       # Env var configuration
│   ├── datomic.clj      # Datomic connection lifecycle
│   ├── pedestal.clj     # HTTP server + interceptors
│   ├── routes.clj       # ALL route handlers (63KB, monolithic)
│   ├── routes/party.clj # Party CRUD handlers
│   ├── routes/folder.clj# Folder CRUD handlers
│   ├── pdf.clj          # PDFBox PDF generation
│   ├── email.clj        # SMTP email sending
│   ├── security.clj     # Rate limiting
│   ├── privacy.clj      # Legal page rendering
│   ├── csp.clj          # Content Security Policy
│   ├── index.clj        # SPA HTML shell
│   ├── time.clj         # Date/time utilities
│   ├── favicon.clj      # Favicon link tags
│   ├── oauth.clj        # OAuth URL helpers
│   ├── db/schema.clj    # Datomic schema definition
│   ├── dnd/e5/modifier_macros.clj  # JVM-only macros
│   ├── styles/core.clj  # Garden CSS
│   ├── tools/orcbrew.clj# CLI homebrew inspector
│   └── fork/            # Fork-configurable modules
│       ├── branding.clj # App name, logos, limits
│       ├── auth.clj     # JWT config
│       ├── integrations.clj  # 3rd-party hooks (no-op)
│       ├── user_data.clj     # User enrichment (no-op)
│       └── privacy_content.clj  # Privacy policy text
│
├── cljc/orcpub/         # Shared (JVM + Browser)
│   ├── entity.cljc      # CORE: Entity build engine (25KB)
│   ├── entity_spec.cljc # Modifier macro DSL
│   ├── entity/strict.cljc  # Datomic serialization format
│   ├── modifiers.cljc   # Modifier system (apply, order)
│   ├── template.cljc    # Template spec + utilities
│   ├── dice.cljc        # Dice rolling
│   ├── common.cljc      # Shared utilities
│   ├── constants.cljc   # App constants
│   ├── components.cljc  # Reusable UI helpers
│   ├── route_map.cljc   # Bidi route definitions
│   ├── registration.cljc# Registration validation
│   ├── errors.cljc      # Error response helpers
│   ├── views_aux.cljc   # View auxiliary functions
│   ├── pdf_spec.cljc    # PDF field specifications
│   └── dnd/e5/          # D&D 5e domain
│       ├── character.cljc   # Character accessors + serialization
│       ├── character_props.cljc  # Character property keys
│       ├── modifiers.cljc   # D&D modifier factories
│       ├── modifier_macros.cljc  # Modifier macro definitions
│       ├── template.cljc    # D&D template construction
│       ├── template_base.cljc   # Base template data
│       ├── compute.cljc     # Computation utilities
│       ├── combat.cljc      # Combat specs
│       ├── encounters.cljc  # Encounter specs
│       ├── spells.cljc      # Spell database
│       ├── spell_lists.cljc # Class spell lists
│       ├── equipment.cljc   # Equipment data
│       ├── weapons.cljc     # Weapon properties
│       ├── armor.cljc       # Armor data
│       ├── magic_items.cljc # Magic item specs
│       ├── monsters.cljc    # Monster database
│       ├── races.cljc       # Race definitions
│       ├── classes.cljc     # Class definitions
│       ├── backgrounds.cljc # Background definitions
│       ├── feats.cljc       # Feat definitions
│       ├── skills.cljc      # Skill definitions
│       ├── languages.cljc   # Language data
│       ├── damage_types.cljc# Damage type data
│       ├── selections.cljc  # Selection helpers
│       ├── options.cljc     # Option utilities
│       ├── party.cljc       # Party spec
│       ├── folder.cljc      # Folder spec
│       ├── units.cljc       # Unit conversion
│       ├── display.cljc     # Display formatting
│       ├── common.cljc      # D&D common utilities
│       ├── char_filter.cljc # Character filtering
│       ├── char_decision_tree.cljc  # Decision tree
│       ├── event_handlers.cljc  # Shared event logic
│       ├── event_utils.cljc     # Event utilities
│       ├── views_2.cljc     # Splash page view
│       ├── character/
│       │   ├── equipment.cljc   # Equipment logic
│       │   └── random.cljc      # Random generation
│       └── templates/       # Content source templates
│           ├── ua_*.cljc    # Unearthed Arcana content
│           └── scag.cljc    # SCAG content
│
├── cljs/orcpub/         # Client-only (Browser)
│   ├── character_builder.cljs  # Main builder UI (86KB)
│   ├── user_agent.cljs  # Browser detection
│   ├── ver.cljc         # Version info
│   └── dnd/e5/
│       ├── views.cljs       # Page components
│       ├── events.cljs      # re-frame events (369 handlers)
│       ├── subs.cljs        # re-frame subscriptions (184 subs)
│       ├── db.cljs          # App state default
│       ├── autosave_fx.cljs # Autosave effect
│       ├── spell_subs.cljs  # Spell-specific subs
│       ├── equipment_subs.cljs  # Equipment subs
│       ├── content_reconciliation.cljs  # Import conflict
│       ├── import_validation.cljs  # Import validation
│       ├── views/
│       │   ├── import_log.cljs      # Import log overlay
│       │   └── conflict_resolution.cljs  # Conflict UI
│       └── fork/
│           ├── branding.cljs    # Client branding
│           ├── integrations.cljs# Client integrations
│           └── user_tier.cljs   # User tier logic
│
web/cljs/orcpub/
└── core.cljs            # App entry point (React 18 createRoot)

dev/
└── user.clj             # REPL helpers + CLI tooling
```

## Design Patterns

### Entity/Template/Modifier (Core Architecture)
- **Location**: `src/cljc/orcpub/{entity,modifiers,template,entity_spec}.cljc`
- **Purpose**: Declarative character computation without centralized calculation functions
- **Implementation**: Entities store choices → Templates define options with modifiers → Build process collects, sorts (Kahn's topo-sort), and applies modifiers

### Component System (Stuart Sierra)
- **Location**: `src/clj/orcpub/{system,datomic,pedestal}.clj`
- **Purpose**: Lifecycle management for stateful components (DB, HTTP server)
- **Implementation**: `com.stuartsierra.component` with DatomicComponent and Pedestal components

### re-frame (Unidirectional Data Flow)
- **Location**: `src/cljs/orcpub/dnd/e5/{events,subs,db}.cljs`
- **Purpose**: Predictable state management with event-driven mutations
- **Implementation**: Events → Effects → DB mutations → Subscription reactions → View renders

### Fork Pattern (White-label Configuration)
- **Location**: `src/clj/orcpub/fork/`, `src/cljs/orcpub/fork/`
- **Purpose**: Allow different deployments to customize branding, auth, integrations without code changes
- **Implementation**: Centralized config modules with env-var overrides; public repo has no-op stubs

### Interceptor Chain (Pedestal)
- **Location**: `src/clj/orcpub/pedestal.clj`
- **Purpose**: Cross-cutting concerns (auth, CSP, caching, error handling)
- **Implementation**: Pedestal interceptors composed per-route

## Critical Dependencies

| Dependency | Version | Usage | Purpose |
|-----------|---------|-------|---------|
| Clojure | 1.12.4 | JVM runtime | Language |
| ClojureScript | 1.12.134 | Browser runtime | Language |
| Datomic Peer | 1.0.7482 | Database access | Data persistence |
| Pedestal | 0.7.0 | HTTP framework | Request handling |
| Reagent | 2.0.1 | React wrapper | UI rendering |
| re-frame | 1.4.4 | State management | App architecture |
| React | 18.3.1 | DOM rendering | UI foundation |
| PDFBox | 3.0.6 | PDF manipulation | Character sheets |
| Buddy Auth | 3.0.323 | JWT authentication | Security |
| Buddy Hashers | 2.0.167 | Password hashing | Security |
| figwheel-main | 0.2.20 | Dev server + CLJS builds | Development |
| Garden | 1.9.606 | CSS generation | Styling |
| bidi | 2.1.6 | Routing | URL dispatch |
| postal | 2.0.5 | SMTP client | Email |
