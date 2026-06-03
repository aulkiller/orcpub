# Units of Work

## Decomposition Strategy

8 units ordered by dependency chain. Each unit is independently testable and deployable (additive changes). The critical path is Unit 1 → Unit 2 → Units 4/5/6. Unit 3 is parallelizable with Units 1-2.

---

## Unit 1: Play State Engine

**Scope**: Core play mode logic + backend persistence + frontend UI

**Stories**: PLAY-01 through PLAY-15

**Components**:
- `orcpub.dnd.e5.play` (shared — rest mechanics, resource tracking, conditions, death saves, rite/curse logic)
- `orcpub.routes.play` (backend — REST endpoints for all play state mutations)
- `orcpub.dnd.e5.views.play` (frontend — Play Mode UI)
- `orcpub.dnd.e5.play-events` (frontend — re-frame events)
- `orcpub.dnd.e5.play-subs` (frontend — re-frame subscriptions)
- `orcpub.dnd.e5.views.dice` (frontend — dice roller component)
- Datomic schema extension (play state attributes on character entity)

**Dependencies**: Existing entity engine, existing modifier system, existing dice module

**Deliverable**: Standalone Play Mode that works without campaigns or WebSocket

---

## Unit 2: WebSocket Infrastructure

**Scope**: Real-time communication layer

**Stories**: SYNC-01 through SYNC-04

**Components**:
- `orcpub.websocket` (backend — Jetty 11 JSR 356 handler, connection management, broadcast)
- `orcpub.websocket-client` (frontend — connect, send, receive, reconnect with backoff)
- nginx config update (WebSocket proxy upgrade headers)

**Dependencies**: Unit 1 (play state changes are what gets broadcast), existing auth (JWT validation on WS connect)

**Deliverable**: Working WebSocket that pushes play state changes to subscribed clients

---

## Unit 3: Content Compendium

**Scope**: Searchable content database + UI

**Stories**: COMP-01 through COMP-07

**Components**:
- `orcpub.dnd.e5.compendium` (shared — entry specs, search model)
- `orcpub.routes.compendium` (backend — search/browse endpoints)
- `orcpub.compendium.sync` (backend — Open5e import client)
- `orcpub.dnd.e5.views.compendium` (frontend — search UI, detail view, filters)
- `orcpub.dnd.e5.compendium-events` (frontend — re-frame events)
- Datomic schema extension (compendium entities: spells, monsters, items with full text)
- Data seeding (bundle SRD data as initial Datomic transact)

**Dependencies**: Existing Datomic infrastructure only (no dependency on Units 1 or 2)

**Deliverable**: Searchable compendium with SRD data + Open5e sync + homebrew merge. Also enables FR-1.16 (in-play rules lookup from Unit 1).

---

## Unit 4: Campaign Management

**Scope**: Campaign CRUD + authorization

**Stories**: CAMP-01 through CAMP-06

**Components**:
- `orcpub.dnd.e5.campaign` (shared — campaign entity spec, membership model)
- `orcpub.routes.campaign` (backend — CRUD + invite + join/leave)
- `orcpub.campaign.auth` (backend — campaign membership interceptor)
- `orcpub.dnd.e5.views.campaign` (frontend — create, invite, members list, DM dashboard)
- `orcpub.dnd.e5.campaign-events` (frontend — re-frame events)
- Datomic schema extension (campaign entity, invite codes, membership refs)

**Dependencies**: Unit 2 (WebSocket subscribes to campaigns), existing auth

**Deliverable**: DM can create campaigns, invite players, see member list. Enables WebSocket scoping.

---

## Unit 5: Encounter Builder

**Scope**: Encounter creation + CR calculation + initiative + monster HP

**Stories**: ENC-01 through ENC-05, SYNC-05

**Components**:
- `orcpub.dnd.e5.encounter` (shared — CR calculation, initiative sort, encounter model)
- `orcpub.routes.encounter` (backend — CRUD + initiative + monster HP)
- `orcpub.dnd.e5.views.encounter` (frontend — builder UI, initiative tracker)
- Datomic schema extension (encounter entity, monster instances)

**Dependencies**: Unit 3 (monsters sourced from compendium), Unit 4 (encounters tied to campaigns), existing dice module

**Deliverable**: DM creates encounters, sees difficulty, runs initiative tracker with monster HP tracking

---

## Unit 6: Party Tools

**Scope**: Shared party view + session notes + DM dashboard

**Stories**: PARTY-01 through PARTY-03

**Components**:
- Frontend views within `orcpub.dnd.e5.views.campaign` (party overview, session notes)
- Datomic schema extension (session notes entity)
- WebSocket integration (party view updates in real-time)

**Dependencies**: Unit 4 (campaigns provide the party context), Unit 2 (real-time updates)

**Deliverable**: Party overview dashboard + session notes within campaign view

---

## Unit 7: Responsive UI

**Scope**: CSS overhaul for mobile responsiveness across all views

**Stories**: UI-01 through UI-03

**Components**:
- `orcpub.styles.core` (Garden CSS — responsive breakpoints, touch targets, layout reflow)
- Existing views (minor markup adjustments for responsive behavior)

**Dependencies**: Units 1, 3, 4, 5 (all views must exist before making them responsive)

**Deliverable**: All views (play mode, builder, compendium, campaign, encounter) usable on mobile

---

## Unit 8: Deployment & CI/CD

**Scope**: Multi-arch build + DockerHub push + compose file updates

**Stories**: DEPLOY-01 through DEPLOY-03

**Components**:
- `docker/Dockerfile` (multi-arch compatible, buildx)
- `docker-compose.yaml` (WebSocket nginx config, health checks)
- `.github/workflows/` (CI pipeline: test → build → push multi-arch to personal DockerHub)
- `deploy/nginx.conf.template` (WebSocket upgrade headers)

**Dependencies**: All other units (deploy the final product)

**Deliverable**: Single `docker compose up` runs everything, images pushed to personal DockerHub for amd64+arm64

---

## Execution Order & Parallelization

```
       ┌─── Unit 1: Play State Engine ───┐
       │                                   ├─── Unit 2: WebSocket ───┐
Start ─┤                                   │                          ├─── Unit 4: Campaign ───┬─── Unit 5: Encounter
       │                                   │                          │                         │
       └─── Unit 3: Compendium ───────────┘──────────────────────────┘                         ├─── Unit 6: Party Tools
                                                                                                │
                                                                                                └─── Unit 7: Responsive UI ─── Unit 8: Deployment
```

**Parallel tracks**:
- Track A: Unit 1 → Unit 2 → Unit 4 → Units 5+6
- Track B: Unit 3 (independent, can start immediately)
- Finishing: Unit 7 → Unit 8 (sequential, after all features)

**Integration points**:
- Unit 3 integrates with Unit 1 at FR-1.16 (in-play rules lookup)
- Unit 4 integrates with Unit 2 (WebSocket scoped by campaign)
- Unit 5 integrates with Units 3+4 (monsters from compendium, encounters in campaigns)
