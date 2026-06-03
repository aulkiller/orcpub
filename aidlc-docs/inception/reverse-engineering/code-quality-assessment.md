# Code Quality Assessment

## Test Coverage
- **Overall**: Good for backend, minimal for frontend
- **Unit Tests**: 210 tests, 963 assertions (backend + shared logic)
- **Integration Tests**: Docker integration tests present
- **Frontend Tests**: Minimal (test infrastructure exists but few tests)
- **Property-Based**: test.check dependency present but usage is minimal

## Code Quality Indicators
- **Linting**: Configured (clj-kondo with project-specific config in `.clj-kondo/config.edn`)
- **Code Style**: Consistent within modules; some variance between original and fork additions
- **Formatting**: cljfmt configured
- **Documentation**: Good project-level docs (README, GETTING-STARTED, migration guides); inline code comments are sparse
- **CI/CD**: GitHub Actions runs tests + lint on push

## Architectural Strengths

### Entity/Template/Modifier Engine
- Elegant separation of concerns: choices (entity) vs. available options (template) vs. computation (modifiers)
- Topological sort ensures correct evaluation order regardless of declaration order
- Shared code (cljc) means client and server compute identically
- No data migration needed when templates change (entities store choices, not computed values)
- Homebrew content "just works" by injecting into templates

### Component System
- Clean lifecycle management for stateful resources
- Easy to test in isolation (DatomicComponent can be mocked)
- Dev/prod configuration cleanly separated

### Fork Pattern
- Allows white-label deployments without forking code
- All customization via env vars or module replacement
- Public repo has clean no-op stubs

## Technical Debt

### High Priority
| Issue | Location | Impact |
|-------|----------|--------|
| Monolithic routes file | `routes.clj` (63KB) | Hard to navigate, test, and modify |
| Monolithic character builder | `character_builder.cljs` (86KB) | Same issue on frontend |
| No WebSocket support | Server architecture | Blocks real-time features |
| No frontend test coverage | `test/cljs/` | Regressions undetected in UI |
| API subs as side-effects | `reg-sub-raw` in subs.cljs | Blurs query/command; hard to reason about |

### Medium Priority
| Issue | Location | Impact |
|-------|----------|--------|
| No code splitting | CLJS bundle | Large initial download |
| Full entity rebuild on every change | `entity/build` | Performance for complex characters |
| localStorage-only homebrew | Frontend plugins | Data loss risk, no sync across devices |
| Inline styles mixed with CSS | character_builder.cljs | Inconsistent styling approach |
| No undo/redo | Events system | User experience gap |
| Stale session handling | Auth flow | API calls can fail before token refresh |

### Low Priority
| Issue | Location | Impact |
|-------|----------|--------|
| Legacy React Native code | `env/`, `native/` dirs | Dead code |
| Commented-out features | Various | Confusion |
| Magic numbers in PDF generation | `pdf.clj` | Maintenance burden |

## Patterns and Anti-patterns

### Good Patterns
- **Declarative computation**: Modifier engine avoids imperative state mutation
- **Shared code via cljc**: Single source of truth for domain logic
- **Interceptor chain**: Clean separation of cross-cutting concerns
- **Component lifecycle**: Proper resource management
- **Fork/white-label**: Clean extension points
- **Debounced subscriptions**: Performance optimization for rapid changes
- **Rate limiting**: Security-conscious auth system
- **Error email throttling**: Prevents admin notification storms

### Anti-patterns
- **God file**: `routes.clj` and `character_builder.cljs` do too much
- **Side-effecting subscriptions**: `reg-sub-raw` subs trigger HTTP requests
- **Implicit dependencies**: Some namespaces rely on eval-order for macro expansion
- **Global atoms for security state**: Rate limiting uses in-memory atoms (lost on restart, not multi-instance safe)
- **PDF template coupling**: Hard-coded field names tied to specific PDF templates

## Readiness for Evolution

### Favorable Factors
- Clean domain engine architecture (entity/template/modifier) is highly extensible
- Shared cljc code means new features automatically work client and server
- re-frame event/subscription pattern scales well with proper namespacing
- Component system makes it easy to add new server-side components (e.g., WebSocket server)
- Existing party/folder systems provide foundation for campaign tools
- Dice module already exists (needs extension for interactive rolling)
- Combat/encounter specs exist (need implementation)

### Challenges
- Monolithic files need refactoring before adding features
- No real-time infrastructure (WebSockets, pub/sub)
- Frontend performance may degrade with interactive play mode (more frequent builds)
- No service worker or offline capability
- State management needs extension for multi-user scenarios
