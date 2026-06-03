# Business Overview

## Business Context

Dungeon Master's Vault (DMV) is a self-hosted, open-source web application for creating and managing Dungeons & Dragons 5th Edition characters. It serves as a community-driven alternative to commercial tools like D&D Beyond.

## Business Description

**Domain**: Tabletop role-playing game (TTRPG) digital tooling for D&D 5e.

**Core Value Proposition**: A free, self-hostable character builder that uses a declarative modifier engine to accurately compute all character attributes from hierarchical option selections — enabling homebrew extensibility without code changes.

**Target Users**:
- D&D 5e players building characters
- Dungeon Masters managing parties
- Homebrew content creators
- Self-hosting enthusiasts running private instances

## Business Transactions

| # | Transaction | Description |
|---|------------|-------------|
| 1 | **Character Creation** | User selects race, class, background, abilities, equipment, spells through hierarchical option selections. System auto-computes all derived attributes via modifier engine. |
| 2 | **Character Management** | Save, load, clone, delete characters. Organize into folders and parties. |
| 3 | **Character Export** | Generate printable PDF character sheet with all computed stats, spell cards, and optional portrait. |
| 4 | **Homebrew Content Creation** | Build custom races, classes, spells, monsters, items, backgrounds, feats, and encounters via in-app builders. |
| 5 | **Homebrew Import/Export** | Import/export `.orcbrew` files containing homebrew content. Handles conflict resolution for duplicate keys. |
| 6 | **Party Management** | Group characters into parties. View party composition. |
| 7 | **User Account Management** | Register, verify email, login, password reset, email change, follow other users, delete account. |
| 8 | **Character Sharing** | Share characters publicly. Follow users to see their shared characters. |
| 9 | **Content Search** | Search spells, monsters, items, and homebrew content via full-text search overlay (Orcacle). |
| 10 | **Random Character Generation** | Generate random characters with optional locked selections (user chooses what to keep). |

## Business Dictionary

| Term | Meaning |
|------|---------|
| **Entity** | A raw character stored as hierarchical option selections (choices only, no computed values). |
| **Template** | A declarative definition of all available character options and their modifiers. |
| **Modifier** | A function that transforms the character entity, with dependency tracking for ordering. |
| **Built Entity** | A computed character with all derived attributes resolved by applying modifiers in dependency order. |
| **Selection** | A choice point in the template (e.g., "Choose a race"). Has min/max constraints. |
| **Option** | A specific choice within a selection (e.g., "Elf" within the race selection). Carries modifiers and sub-selections. |
| **Plugin** | Homebrew content loaded as additional selections merged into the base template at runtime. |
| **Orcbrew** | The `.orcbrew` file format (EDN) for homebrew content packages. |
| **Deferred Modifier** | A modifier that requires user input at build time (e.g., ability score allocation, HP roll choice). |
| **Strict Entity** | The Datomic-storable normalized format of an entity (vs. runtime map format). |
| **SRD** | System Reference Document — freely licensed D&D 5e content from Wizards of the Coast. |
| **Party** | A group of characters belonging to the same game session/campaign. |
| **Folder** | An organizational container for characters (at-most-one-folder per character). |

## Component Level Business Descriptions

### Character Builder (Frontend)
- **Purpose**: Interactive character creation and editing interface
- **Responsibilities**: Render hierarchical option selections, display live character preview, manage autosave

### Entity/Modifier Engine (Shared)
- **Purpose**: Declarative character computation engine
- **Responsibilities**: Build characters from selections, resolve modifier dependencies via topological sort, apply modifiers in correct order

### API Server (Backend)
- **Purpose**: REST API for persistence and PDF generation
- **Responsibilities**: User authentication, character CRUD, PDF generation, email handling

### Database (Datomic)
- **Purpose**: Persistent storage for users, characters, parties, and items
- **Responsibilities**: Entity storage in strict format, schema enforcement, query support

### Homebrew System
- **Purpose**: Extensible content system for community-created D&D content
- **Responsibilities**: Content builders, import/export, conflict resolution, plugin injection into templates
