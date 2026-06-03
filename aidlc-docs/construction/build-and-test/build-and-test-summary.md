# Build & Test Instructions

## Prerequisites
- Java 21 (OpenJDK)
- Leiningen 2.9+
- Docker + Docker Compose (for integration/deployment testing)
- Datomic Pro transactor running (for backend integration tests)

---

## 1. Build Instructions

### Backend (JVM) compilation check
```bash
lein compile
```

### Frontend (ClojureScript) compilation check
```bash
lein fig:build
```

### Production build (CLJS + uberjar)
```bash
lein build
```

### CSS compilation
```bash
lein garden once
```

### Docker image (multi-arch)
```bash
docker buildx build --platform linux/amd64,linux/arm64 -t yourregistry/orcpub:latest docker/
```

---

## 2. Unit Test Execution

### All backend + shared tests
```bash
lein test
```
**Expected**: 234+ tests, 1030+ assertions, 0 failures

### Play State tests only
```bash
lein test :only orcpub.dnd.e5.play-test
```

### Property-based tests only
```bash
lein test :only orcpub.dnd.e5.play-properties-test
```

### Encounter tests only
```bash
lein test :only orcpub.dnd.e5.encounter-test
```

### Lint
```bash
lein lint
```
**Expected**: Exit 0 (no errors)

---

## 3. Integration Tests

### WebSocket integration (manual)
```bash
# 1. Start the full stack
./scripts/start.sh datomic
lein repl
# In REPL:
(start-server)

# 2. In another terminal, test WS connection:
# Install wscat: npm install -g wscat
wscat -c "ws://localhost:8890/ws?token=<valid-jwt>&campaign=<campaign-id>&role=player"

# Expected: receives {:type :connected :username "..."}
# Send: {:type :ping}
# Expected: receives {:type :pong}
```

### Campaign API integration (manual)
```bash
# Create campaign (authenticated)
curl -X POST http://localhost:8890/api/campaigns \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/edn" \
  -d '{:name "Test Campaign"}'

# Expected: {:db/id <number> :invite-code "XXXXXX"}

# Join campaign
curl -X POST http://localhost:8890/api/campaigns/join \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/edn" \
  -d '{:code "XXXXXX" :character-id <char-id>}'
```

### Play State API integration (manual)
```bash
# Get play state
curl http://localhost:8890/api/play/<char-id>/state \
  -H "Authorization: Bearer <jwt>"

# Take damage
curl -X POST http://localhost:8890/api/play/<char-id>/damage \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/edn" \
  -d '{:amount 10}'

# Long rest
curl -X POST http://localhost:8890/api/play/<char-id>/long-rest \
  -H "Authorization: Bearer <jwt>" \
  -H "Content-Type: application/edn" \
  -d '{:resource-defs []}'
```

### Compendium API integration (manual)
```bash
# Search spells
curl -X POST http://localhost:8890/api/compendium/search \
  -H "Content-Type: application/edn" \
  -d '{:query "fireball" :content-type :spell :filters {}}'
```

---

## 4. Docker Deployment Test

```bash
# Build and start full stack
docker compose up --build -d

# Verify all containers healthy
docker compose ps

# Create test user
./docker-user.sh create testuser test@test.com testpass

# Verify web accessible
curl -k https://localhost/health
# Expected: "OK"

# Verify WebSocket upgrade through nginx
wscat -c "wss://localhost/ws?token=<jwt>&campaign=test&role=dm" --no-check
# Expected: connection established (or 4001 if token invalid)
```

### Multi-arch verification
```bash
# Check image manifests
docker manifest inspect yourregistry/orcpub:latest
# Expected: shows both linux/amd64 and linux/arm64 platforms
```

---

## 5. CI/CD Setup

### Required GitHub Secrets
| Secret | Value |
|--------|-------|
| `DOCKERHUB_USERNAME` | Your DockerHub username |
| `DOCKERHUB_TOKEN` | DockerHub access token (not password) |

### Optional GitHub Variables
| Variable | Default | Purpose |
|----------|---------|---------|
| `DOCKERHUB_REGISTRY` | `orcpub` | Registry/org name |
| `DOCKERHUB_IMAGE` | `orcpub` | Image name |

### Trigger
- Push to `main` or `develop` → runs test + builds + pushes multi-arch images
- Manual: Actions tab → "Build and Push Multi-Arch" → Run workflow

---

## 6. Test Coverage Summary

| Category | Tests | Assertions | Coverage |
|----------|-------|------------|----------|
| Existing (character builder, routes, PDF, etc.) | 210 | 963 | All existing functionality |
| Play State (example-based) | 13 | 48 | All 13 business rule groups |
| Play State (property-based) | 8 | 8 | 750 generated cases: invariants, round-trips, idempotence |
| Encounter (CR calculation) | 3 | 11 | Party thresholds, difficulty rating, initiative sort |
| **Total** | **234** | **1030** | |

### PBT Properties Verified
- HP never below 0 after damage
- HP never exceeds max after heal
- Spell slots used never exceeds max
- Death saves bounded to 0-3
- Long rest is idempotent
- Crimson Rite activate/deactivate round-trip restores max HP
- Dice results within [1, sides]
- Attunement capped at 3 items

---

## 7. Known Limitations (v1.0)

- WebSocket does not persist message queue (missed messages on reconnect require full state resync)
- Compendium search is Datomic fulltext (adequate, not Elasticsearch-level)
- Open5e sync is manual trigger (no auto-scheduled sync)
- Frontend compendium subscriptions (`[:compendium-results]`, `[:compendium-loading?]`) need registration in `subs.cljs`
- Lycan Hybrid Transformation AC/claws are permanent modifiers (not toggle-gated in build mode — Play Mode conditions panel used for activation tracking)

## 8. Blood Hunter Class

Fully mechanical builder class with:
- Crimson Rite / Esoteric Rite selections (choose elements)
- Blood Curse selections (choose curses at 1, 6, 10, 14)
- Fighting Style selection at level 2
- All 4 subclasses (Ghostslayer, Lycan, Mutant, Profane Soul)
- Profane Soul pact magic spellcasting (Int-based, short rest recharge)
- Mutagen formula selections (Mutant)
- Mechanical modifiers: AC, speed, darkvision, resistances, immunities, save bonuses, attacks

## 9. Auto-Seeding

On first startup (empty compendium), the app automatically seeds:
- ~322 SRD spells
- ~325 SRD monsters  
- 55 Blood Hunter entries (features, rites, curses, all subclasses)

Manual re-seed: `lein with-profile init-db run -m user seed-compendium`
