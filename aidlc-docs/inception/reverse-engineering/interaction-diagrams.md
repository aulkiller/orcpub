# Interaction Diagrams

## Business Transaction Flows

### 1. Character Creation (Build Flow)

```mermaid
sequenceDiagram
    participant User
    participant UI as Builder UI
    participant Events as re-frame Events
    participant Subs as re-frame Subs
    participant Engine as Entity Engine
    participant LS as localStorage

    User->>UI: Opens character builder
    UI->>Events: dispatch [:initialize-db]
    Events->>LS: Load saved character (if any)
    Events-->>Subs: app-db updated

    loop For each selection
        User->>UI: Makes selection (e.g., race=Elf)
        UI->>Events: dispatch [:set-option path value]
        Events->>Events: Interceptors: spec-check, set-changed, ->local-store
        Events->>LS: Persist character to localStorage
        Subs->>Engine: entity/build(entity, built-template)
        Engine->>Engine: collect-modifiers-2 (flatten options, lookup template)
        Engine->>Engine: kahn-sort (topological sort by dependencies)
        Engine->>Engine: apply-modifiers (reduce over entity)
        Engine-->>Subs: Built character (computed attributes)
        Subs-->>UI: Reagent re-render with new values
        UI-->>User: Updated character preview
    end
```

### 2. Character Save (Persistence Flow)

```mermaid
sequenceDiagram
    participant User
    participant UI as Builder UI
    participant Events as re-frame Events
    participant API as Pedestal Server
    participant DB as Datomic

    User->>UI: Click Save
    UI->>Events: dispatch [:save-character built-char]
    Events->>Events: Validate ability scores
    Events->>Events: char5e/to-strict (serialize entity)
    Events->>Events: Generate summary (name, race, classes, level)
    Events->>API: POST /dnd/5e/characters (Transit EDN)
    API->>API: check-auth interceptor (verify JWT)
    API->>API: Validate entity structure
    API->>DB: d/transact (upsert entity)
    DB-->>API: tx-result with :db/id
    API-->>Events: 200 {db/id: 12345}
    Events->>Events: Update app-db with server ID
    Events->>Events: Clear dirty flag
    Events-->>UI: Save confirmed
    UI-->>User: Success indicator
```

### 3. PDF Export

```mermaid
sequenceDiagram
    participant User
    participant UI as Browser
    participant Server as Pedestal
    participant PDF as PDFBox
    participant FS as PDF Templates

    User->>UI: Click Export PDF
    UI->>UI: Build hidden form with EDN body
    UI->>Server: POST /character.pdf (form submit)
    Server->>Server: Parse EDN from form params
    Server->>Server: Determine template (spell count based)
    Server->>FS: Load fillable PDF template
    Server->>PDF: Open PDDocument
    Server->>PDF: write-fields! (fill all form fields)
    
    opt Has character image
        Server->>PDF: draw-image! (embed portrait)
    end
    
    opt Has spells
        Server->>PDF: print-spells (generate spell cards, 9/page)
    end
    
    PDF-->>Server: Completed PDF bytes
    Server-->>UI: Content-Disposition: attachment; filename=CharName.pdf
    UI-->>User: PDF download
```

### 4. User Authentication

```mermaid
sequenceDiagram
    participant User
    participant UI as Browser
    participant Server as Pedestal
    participant Security as Rate Limiter
    participant DB as Datomic
    participant Email as SMTP

    Note over User,Email: Registration
    User->>UI: Fill registration form
    UI->>Server: POST /register
    Server->>DB: Check username/email uniqueness
    Server->>DB: Create user (unverified, bcrypt hash)
    Server->>Email: Send verification email
    Server-->>UI: 200 OK
    
    Note over User,Email: Email Verification
    User->>Server: GET /verify?key=UUID
    Server->>DB: Lookup by verification-key
    Server->>DB: Mark verified=true
    Server-->>User: Redirect to success page
    
    Note over User,Email: Login
    User->>UI: Enter credentials
    UI->>Server: POST /login
    Server->>Security: Check rate limits
    Security-->>Server: OK (under threshold)
    Server->>DB: Lookup user, verify password
    Server->>Server: Create JWT (24h expiry)
    Server-->>UI: {token: "jwt...", user-data: {...}}
    UI->>UI: Store token in localStorage
```

### 5. Homebrew Import

```mermaid
sequenceDiagram
    participant User
    participant UI as Browser
    participant Validation as Import Validator
    participant Reconciler as Content Reconciler
    participant LS as localStorage

    User->>UI: Select .orcbrew file
    UI->>UI: Read file as EDN
    UI->>Validation: Validate structure + required fields
    Validation-->>UI: Validation result (errors/warnings)
    
    alt Has conflicts (duplicate keys)
        UI->>Reconciler: Detect conflicts with existing plugins
        Reconciler-->>UI: Conflict list
        UI-->>User: Show conflict resolution UI
        User->>UI: Choose resolution per conflict (keep/replace/rename)
        UI->>Reconciler: Apply resolutions
    end
    
    UI->>LS: Merge into plugins map
    UI->>UI: Rebuild template (inject new selections)
    UI-->>User: Import complete, content available in builder
```

### 6. Party Management

```mermaid
sequenceDiagram
    participant DM as Dungeon Master
    participant UI as Browser
    participant API as Server
    participant DB as Datomic

    DM->>UI: Create party "The Fellowship"
    UI->>API: POST /dnd/5e/parties {name: "The Fellowship"}
    API->>DB: Create party entity
    DB-->>API: {:db/id 789}
    API-->>UI: Party created

    loop Add characters
        DM->>UI: Add character to party
        UI->>API: POST /dnd/5e/parties/789/characters {character-id: 123}
        API->>DB: Add ref to party/character-ids
        API-->>UI: Character added
    end

    DM->>UI: View party
    UI->>API: GET /dnd/5e/parties
    API->>DB: Query parties for owner
    DB-->>API: Party with character refs
    API-->>UI: Party data with character IDs
```
