# API Documentation

## REST APIs

### Authentication

#### POST /register
- **Purpose**: Create new user account
- **Request**: `{:username, :email, :password, :first-and-last-name}`
- **Response**: 200 OK or 400 with error details
- **Side Effects**: Sends verification email

#### POST /login
- **Purpose**: Authenticate user, return JWT
- **Request**: `{:username-or-email, :password}`
- **Response**: `{:token <jwt>, :user-data {:username, :email, ...}}`
- **Errors**: 401 (bad credentials), 403 (unverified), 429 (rate limited)

#### GET /user
- **Auth**: Required (JWT)
- **Purpose**: Get current user profile
- **Response**: User data map

#### PUT /user
- **Auth**: Required
- **Purpose**: Update preferences (send-updates?)
- **Request**: `{:send-updates? boolean}`

#### DELETE /user
- **Auth**: Required
- **Purpose**: Delete own account and all associated data

#### PUT /user/email
- **Auth**: Required
- **Purpose**: Initiate email change
- **Request**: `{:new-email string}`
- **Side Effects**: Sends verification to new email

#### GET /verify?key=UUID
- **Purpose**: Verify email address
- **Response**: Redirect to success/failure page

#### GET /send-password-reset?email=X
- **Purpose**: Send password reset email
- **Response**: 200 always (security: no email enumeration)

#### POST /reset-password
- **Auth**: Temporary JWT (from cookie)
- **Purpose**: Set new password
- **Request**: `{:password string}`

### Characters

#### GET /dnd/5e/characters
- **Auth**: Required
- **Purpose**: List all user's characters
- **Response**: Vector of strict entities

#### POST /dnd/5e/characters
- **Auth**: Required
- **Purpose**: Create or update character
- **Request**: Strict entity (EDN in Transit format)
- **Response**: `{:db/id <entity-id>}`
- **Notes**: Upsert — if `:db/id` present, updates existing

#### GET /dnd/5e/characters/:id
- **Auth**: Optional
- **Purpose**: Get character by database ID
- **Response**: Strict entity or 404

#### DELETE /dnd/5e/characters/:id
- **Auth**: Required (owner only)
- **Purpose**: Delete character
- **Response**: 200 OK

#### GET /dnd/5e/character-summaries
- **Auth**: Required
- **Purpose**: List character summaries (own + followed users' shared characters)
- **Response**: Vector of `{:db/id, :summary, :owner}`

### PDF Export

#### POST /character.pdf
- **Auth**: Not required
- **Purpose**: Generate PDF character sheet
- **Request**: Form POST with EDN body (built character data)
- **Response**: `application/pdf` attachment
- **Notes**: Selects PDF template based on spell sheet count

### Parties

#### GET /dnd/5e/parties
- **Auth**: Required
- **Purpose**: List user's parties
- **Response**: Vector of party maps

#### POST /dnd/5e/parties
- **Auth**: Required
- **Purpose**: Create new party
- **Request**: `{:name string}`
- **Response**: `{:db/id <id>}`

#### DELETE /dnd/5e/parties/:id
- **Auth**: Required (owner only)
- **Purpose**: Delete party

#### PUT /dnd/5e/parties/:id/name
- **Auth**: Required (owner only)
- **Purpose**: Rename party
- **Request**: `{:name string}`

#### POST /dnd/5e/parties/:id/characters
- **Auth**: Required (owner only)
- **Purpose**: Add character to party
- **Request**: `{:character-id <db-id>}`

#### DELETE /dnd/5e/parties/:id/characters/:character-id
- **Auth**: Required (owner only)
- **Purpose**: Remove character from party

### Folders

#### GET /dnd/5e/folders
- **Auth**: Required
- **Purpose**: List user's folders

#### POST /dnd/5e/folders
- **Auth**: Required
- **Purpose**: Create folder
- **Request**: `{:name string}`

#### DELETE /dnd/5e/folders/:id
- **Auth**: Required (owner only)

#### PUT /dnd/5e/folders/:id/name
- **Auth**: Required (owner only)
- **Request**: `{:name string}`

#### POST /dnd/5e/folders/:id/characters
- **Auth**: Required (owner only)
- **Request**: `{:character-id <db-id>}`
- **Notes**: Auto-removes from previous folder (max 1 folder per character)

#### DELETE /dnd/5e/folders/:id/characters/:character-id
- **Auth**: Required (owner only)

### Magic Items

#### GET /dnd/5e/items
- **Auth**: Required
- **Purpose**: List user's custom magic items

#### POST /dnd/5e/items
- **Auth**: Required
- **Purpose**: Create/update custom magic item

#### DELETE /dnd/5e/items/:id
- **Auth**: Required (owner only)

### Social

#### POST /following/users/:username
- **Auth**: Required
- **Purpose**: Follow a user

#### DELETE /following/users/:username
- **Auth**: Required
- **Purpose**: Unfollow a user

### Utility

#### GET /health
- **Purpose**: Healthcheck endpoint
- **Response**: "OK" (200)

#### GET /check-email?email=X
- **Purpose**: Check if email already registered
- **Response**: `{:used true/false}`

#### GET /check-username?username=X
- **Purpose**: Check if username taken
- **Response**: `{:used true/false}`

## Data Models

### Character (Strict Entity Format)
```clojure
{:db/id <long>
 ::se/owner "username"
 ::se/type :character
 ::se/game :dnd
 ::se/game-version :e5
 ::se/selections [{::se/key :race
                   ::se/options [{::se/key :elf
                                  ::se/selections [...]}]}]
 ::se/values {::char5e/character-name "Name"
              ::char5e/str 15
              ::char5e/dex 14 ...}}
```

### User
```clojure
{:db/id <long>
 :orcpub.user/username "user"
 :orcpub.user/email "user@example.com"
 :orcpub.user/password "<bcrypt-hash>"
 :orcpub.user/verified? true
 :orcpub.user/created #inst "..."}
```

### Party
```clojure
{:db/id <long>
 ::party5e/owner "username"
 ::party5e/name "The Fellowship"
 ::party5e/character-ids [<db-id> <db-id> ...]}
```

### Folder
```clojure
{:db/id <long>
 ::folder5e/owner "username"
 ::folder5e/name "Campaign 1"
 ::folder5e/character-ids [<db-id> ...]}
```
