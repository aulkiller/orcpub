# Technology Stack

## Programming Languages
| Language | Version | Usage |
|----------|---------|-------|
| Clojure | 1.12.4 | Backend server, build scripts, CSS generation |
| ClojureScript | 1.12.134 | Frontend SPA, shared domain logic |
| Java | 21 (OpenJDK) | JVM runtime |
| JavaScript | ES2020+ | React interop, figwheel bridge |

## Frameworks
| Framework | Version | Purpose |
|-----------|---------|---------|
| Pedestal | 0.7.0 | HTTP server, routing, interceptors |
| Reagent | 2.0.1 | React wrapper for ClojureScript |
| re-frame | 1.4.4 | State management (events, subscriptions, effects) |
| React | 18.3.1 | DOM rendering (Concurrent Mode via Reagent 2.0) |
| Stuart Sierra Component | 1.2.0 | Server lifecycle management |

## Infrastructure
| Service | Purpose |
|---------|---------|
| Datomic Pro | 1.0.7482 — Immutable database with entity model |
| Jetty | 11 (via Pedestal) — HTTP server |
| nginx | Reverse proxy, SSL termination |
| Docker Compose | Container orchestration (3 containers) |

## Build Tools
| Tool | Version | Purpose |
|------|---------|---------|
| Leiningen | 2.9+ | Build tool, dependency management, project configuration |
| figwheel-main | 0.2.20 | CLJS hot-reload dev server + production builds |
| Garden | 1.9.606 | CSS generation from Clojure DSL |
| Google Closure Compiler | (via ClojureScript) | JS optimization + dead code elimination |

## Key Libraries
| Library | Version | Purpose |
|---------|---------|---------|
| Buddy Auth | 3.0.323 | JWT-based authentication |
| Buddy Hashers | 2.0.167 | Bcrypt password hashing |
| PDFBox | 3.0.6 | PDF form filling + image embedding |
| bidi | 2.1.6 | Bidirectional routing (server + client) |
| postal | 2.0.5 | SMTP email sending |
| cljs-http | 0.1.49 | HTTP client (ClojureScript) |
| transit-cljs | 0.8.280 | EDN-like data format for API communication |
| clj-http | 3.13.1 | HTTP client (Clojure) |
| hiccup | 2.0.0 | HTML templating |
| environ | 1.2.0 | Environment variable access |
| core.async | 1.8.741 | Async channels |
| data.json | 2.5.0 | JSON parsing |
| datomock | 0.2.2-favila1 | Datomic mocking for tests |

## Testing Tools
| Tool | Version | Purpose |
|------|---------|---------|
| clojure.test | (built-in) | JVM unit testing |
| test.check | 1.1.1 | Generative/property-based testing |
| clj-kondo | 2026.01.19 | Linting (CLJ + CLJS) |
| cljfmt | 0.16.0 | Code formatting |
| kibit | 0.1.11 | Idiomatic Clojure suggestions |
| datomock | 0.2.2 | In-memory Datomic for tests |

## Development Tools
| Tool | Purpose |
|------|---------|
| nREPL | 1.3.0 — Network REPL protocol |
| Piggieback | 0.5.3 — ClojureScript REPL over nREPL |
| devtools | 1.0.7 — Chrome DevTools custom formatters |
| re-frame-10x | 1.11.0 — re-frame debugging dashboard |
| rebel-readline-cljs | 0.1.4 — Enhanced CLJS REPL readline |

## CI/CD
| Tool | Purpose |
|------|---------|
| GitHub Actions | CI pipeline (test + lint) |
| Docker Hub | Image registry (orcpub/orcpub) |
