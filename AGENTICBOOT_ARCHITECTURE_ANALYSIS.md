# AgenticBoot Framework Architecture & Design Summary

**Created:** November 4, 2025
**Source:** /home/developer/adentic-boot (Java 21)
**Purpose:** Analysis for Python equivalent design

---

## Executive Summary

AgenticBoot is a lightweight, Spring-free application framework designed for Java 21 that provides:
- Dependency Injection (constructor-based, type-safe)
- HTTP server with REST mapping (Javalin-based)
- Event bus (synchronous & asynchronous pub/sub)
- Provider registry system (9 categories of providers)
- Component scanning and auto-discovery
- Zero-configuration bootstrap

**Key Stats:**
- Framework Core: 1,574 lines of code
- JAR Size: 8.7KB (99.8% smaller than Spring Boot)
- Startup Time: <1 second
- Test Coverage: 25/25 tests passing (100%)
- Build Quality: Spotless (Google Java Format), Checkstyle, JaCoCo (10% minimum)

---

## 1. CORE ARCHITECTURAL COMPONENTS

### 1.1 4-Layer Modular Architecture

```
Layer 1: adentic-se (Contracts)
  ↓ Pure interfaces - zero dependencies

Layer 2: adentic-se-annotation (Annotations - 8.7KB)
  ↓ Domain-specific annotations (@LLM, @Storage, etc.)

Layer 3: adentic-core (Implementations)
  ↓ 20+ provider implementations

Layer 4: adentic-boot (Framework Runtime)
  ↓ DI, HTTP server, event bus, component scanning
```

**Key Design Principle:** Strict separation of concerns
- Layer 1 has NO dependencies
- Layer 2 depends only on Layer 1
- Layer 3 depends on Layers 1-2
- Layer 4 depends on Layers 1-3

### 1.2 Core Framework Classes

#### AgenticApplication (Bootstrap)
**File:** `AgenticApplication.java` (~195 lines)
**Purpose:** Application entry point (similar to Spring's SpringApplication)

**Workflow:**
1. Print banner
2. Create AgenticContext
3. Register core beans (EventBus, ProviderRegistry, AgenticServer)
4. Get configuration from @AgenticBootApplication annotation
5. Scan components from base package
6. Register components in context
7. Scan and register providers
8. Register REST controllers with HTTP server
9. Start HTTP server
10. Add shutdown hook for cleanup

**Usage:**
```java
@AgenticBootApplication
public class MyApp {
    public static void main(String[] args) {
        AgenticApplication.run(MyApp.class, args);
    }
}
```

#### AgenticContext (Dependency Injection)
**File:** `AgenticContext.java` (~267 lines)
**Purpose:** Lightweight DI container with constructor injection

**Features:**
- Register singletons by type or name
- Register factories for lazy instantiation
- Auto-instantiate beans from classes
- Constructor-based dependency injection
- Circular dependency detection
- Type-safe bean retrieval

**Key Methods:**
- `registerSingleton(Class<T>, T instance)` - Register singleton by type
- `registerSingleton(String name, Object instance)` - Register named singleton
- `registerFactory(Class<T>, Supplier<T> factory)` - Lazy instantiation
- `registerBean(Class<T> beanClass)` - Auto-discover and instantiate
- `getBean(Class<T> type)` - Retrieve bean
- `getBean(String name, Class<T> type)` - Retrieve named bean
- `containsBean(Class<?> type)` - Check existence
- `close()` - Cleanup resources

**DI Mechanism:**
- Scans class constructors for @Inject annotation
- Falls back to single public constructor
- Falls back to no-arg constructor
- Recursively resolves transitive dependencies
- Detects and prevents circular dependencies

#### ComponentScanner (Auto-discovery)
**File:** `ComponentScanner.java` (~283 lines)
**Purpose:** Classpath scanning for annotated components and providers

**Supported Annotations:**
- Core: @Component, @Service, @RestController
- Providers: @TextGenerationProvider, @InfrastructureProvider, @StorageProvider, @MessageBrokerProvider, @OrchestrationProvider, @MemoryProvider, @TaskQueueProvider, @ToolProvider, @EvaluationProvider, @WebSearchProvider, @WebTestProvider, @DatabaseProvider

**Features:**
- Recursive directory scanning
- Meta-annotation support (e.g., @Service is annotated with @Component)
- Provider categorization (12 categories)
- Configurable base package
- Custom class loader support

**Key Methods:**
- `scan()` - Scan all component types
- `scanForAnnotation(Class<? extends Annotation>)` - Scan specific annotation
- `scanProviders()` - Scan and group providers by category

#### ProviderRegistry (Provider Management)
**File:** `ProviderRegistry.java` (~265 lines)
**Purpose:** Centralized registry for managing providers by category

**Supported Categories:**
1. llm - Text generation providers
2. infrastructure - Infrastructure providers
3. storage - Storage providers
4. messaging - Message broker providers
5. orchestration - Orchestration providers
6. memory - Memory providers
7. queue - Task queue providers
8. tool - Tool providers
9. evaluation - Evaluation providers

**Features:**
- Category-based organization
- Provider lookup by category and name
- Annotation-driven registration
- Provider count tracking

**Key Methods:**
- `registerProvider(String category, String name, Object instance)`
- `registerProviderFromClass(Class<?> providerClass, Object instance)`
- `getProvider(String category, String name)` → Optional<Object>
- `getProvidersByCategory(String category)` → Map<String, Object>
- `hasProvider(String category, String name)` → boolean
- `getProviderCount(String category)` → int

#### EventBus (Pub/Sub Messaging)
**File:** `EventBus.java` (~186 lines)
**Purpose:** Type-safe event publishing and subscription

**Features:**
- Type-safe listeners (generics)
- Synchronous event delivery
- Asynchronous event delivery (thread pool)
- Listener registration/unregistration
- Error handling for listener exceptions
- Configurable thread executor

**Key Methods:**
- `subscribe(Class<T> eventType, Consumer<T> listener)` - Sync listener
- `subscribeAsync(Class<T> eventType, Consumer<T> listener)` - Async listener
- `publish(T event)` - Publish event
- `unsubscribe(Class<T> eventType, Consumer<T> listener)`
- `unsubscribeAll(Class<T> eventType)`
- `getListenerCount(Class<?> eventType)` → int

**Thread Pool:** Default 10 threads, daemon threads, named "event-bus-{threadId}"

#### AgenticServer (HTTP Server)
**File:** `AgenticServer.java` (~150+ lines)
**Purpose:** Javalin-based embedded HTTP server with automatic route registration

**Features:**
- REST controller auto-discovery and registration
- Automatic HTTP method mapping (@GetMapping, @PostMapping, etc.)
- Path variable and request body parameter extraction
- JSON serialization (Jackson with Java Time support)
- CORS enabled by default
- Health check endpoint (/health)

**Key Methods:**
- `registerController(Object controller)`
- `start(int port)`
- `close()`

**Supported HTTP Annotations:**
- @RequestMapping - Class-level path
- @GetMapping - GET requests
- @PostMapping - POST requests
- @PathVariable - Path parameters
- @RequestBody - Request body parsing
- @RequestParam - Query parameters

---

## 2. KEY FEATURES TO REPLICATE

### 2.1 Dependency Injection System

**Type:** Constructor-based, type-safe
**Scope:** Singletons only (no prototype scope)
**Features:**
- Automatic constructor detection
- @Inject annotation support
- Circular dependency detection
- Type-safe bean retrieval
- Named bean support
- Factory functions for lazy instantiation

**Pattern for Python:**
```python
@inject
class MyService:
    def __init__(self, dependency: OtherService):
        self.dependency = dependency
```

### 2.2 Component Scanning

**Mechanism:** Classpath reflection-based
**Annotations Supported:** 20+
**Behavior:**
- Recursive directory scanning
- Meta-annotation support
- Configurable base package
- Provider categorization

**Python Equivalent:** Module introspection + decorator-based discovery

### 2.3 Provider Registry Pattern

**Purpose:** Category-based provider management
**Categories:** 9 standard categories (LLM, Storage, Messaging, etc.)
**Features:**
- Annotation-driven categorization
- Provider lookup by name
- Category-wide queries
- Provider counting

### 2.4 Event Bus Pattern

**Type:** In-process pub/sub
**Delivery:** Sync and async modes
**Implementation:** Consumer-based listeners with thread pool

### 2.5 HTTP Server Integration

**Framework:** Javalin (lightweight, no Spring)
**Features:**
- Automatic controller registration
- Annotation-driven routing
- Parameter injection
- JSON serialization

---

## 3. ANNOTATION SYSTEM

### 3.1 Core Annotations

```java
@AgenticBootApplication       // Main app marker
@Component                     // Generic component
@Service                       // Service layer
@RestController               // HTTP controller
@Inject                        // Constructor injection
```

### 3.2 HTTP Annotations

```java
@RequestMapping("/path")      // Class-level mapping
@GetMapping("/path")          // GET handler
@PostMapping("/path")         // POST handler
@PutMapping("/path")          // PUT handler (planned)
@DeleteMapping("/path")       // DELETE handler (planned)
@PathVariable                 // Path parameter
@RequestBody                  // Request body
@RequestParam                 // Query parameter
```

### 3.3 Provider Annotations

```java
@LLM                          // Text generation providers
@Infrastructure               // Infrastructure providers
@Storage                       // Storage providers
@Messaging                     // Message brokers
@Orchestration                 // Orchestration providers
@Memory                        // Memory providers
@Queue                         // Task queue providers
@Tool                          // Tool providers
@Evaluation                    // Evaluation providers
@WebSearch                     // Web search providers
@WebTest                       // Web testing providers
@DatabaseProvider             // Database providers
```

**Naming Convention:** Short annotations, packages provide context
- NOT: @LLMProvider, @StorageProvider
- YES: @LLM, @Storage (in respective packages)

---

## 4. CONFIGURATION APPROACH

### 4.1 Zero Configuration Philosophy

**Core Principle:** Convention over configuration

**Configuration Levels:**
1. **Annotation-based:** @AgenticBootApplication(port=8080, scanBasePackages="com.example")
2. **Programmatic:** AgenticApplication constructor setters
3. **Properties File:** application.properties (optional)

### 4.2 Properties File (application.properties)

```properties
# Server configuration
server.port=8080
server.host=0.0.0.0

# Component scanning
component.scan.packages=com.example.app

# HTTP server
http.server.enabled=true
```

### 4.3 Programmatic Configuration

```java
@AgenticBootApplication
public class MyApp {
    public static void main(String[] args) {
        AgenticApplication app = new AgenticApplication(MyApp.class);
        // Configuration available via annotation
    }
}
```

---

## 5. TESTING PATTERNS & PRACTICES

### 5.1 Test Framework

**Framework:** JUnit 5 (Jupiter)
**Mocking:** Mockito
**Assertions:** AssertJ
**Coverage Tool:** JaCoCo (10% minimum requirement)
**Code Format:** Google Java Format (Spotless)
**Linting:** Checkstyle

### 5.2 Test Structure

**Test Files:** 45 test files
**Passing Tests:** 25/25 (100%)
**Coverage Requirement:** 10% minimum (enforced by build)

**Test Categories:**
1. Unit Tests - Individual components with mocks
2. Integration Tests - Real provider implementations
3. E2E Tests - Full workflows

### 5.3 Test Naming Conventions

**Pattern:** `XxxTest.java` or `XxxTests.java`
**Method Pattern:** `shouldXxxWhenYyy()`
**Annotations:**
- `@Test` - Test method
- `@BeforeEach` - Setup before each test
- `@DisplayName` - Human-readable test name
- `@ParameterizedTest` - Parametrized tests

### 5.4 Example Test Pattern

```java
@DisplayName("AgenticContext Tests")
class AgenticContextTest {
    private AgenticContext context;
    
    @BeforeEach
    void setUp() {
        context = new AgenticContext();
    }
    
    @Test
    @DisplayName("Should register singleton by type")
    void shouldRegisterSingletonByType() {
        TestService service = new TestService("test");
        context.registerSingleton(TestService.class, service);
        assertThat(context.containsBean(TestService.class)).isTrue();
    }
}
```

### 5.5 Test Coverage

**Key Areas:**
- Dependency injection (circular deps, autowiring)
- Component scanning (discovery, meta-annotations)
- Provider registry (registration, lookup)
- Event bus (sync/async, error handling)
- HTTP server (routing, parameter extraction)
- Tool implementations (Database, WebSearch, FileSystem, etc.)

---

## 6. BUILD & QUALITY CONTROL SYSTEM

### 6.1 Build Tool: Maven

**Configuration:** `pom.xml`
**Java Version:** 21
**Version Strategy:** 1.0.0-SNAPSHOT

### 6.2 Maven Plugins

| Plugin | Purpose | Configuration |
|--------|---------|---------------|
| maven-compiler-plugin | Java 21 compilation | Lombok annotation processor |
| maven-surefire-plugin | Test execution | JUnit 5 support |
| spotless-maven-plugin | Code formatting | Google Java Format (v1.19.2) |
| maven-checkstyle-plugin | Linting | Custom checkstyle.xml config |
| jacoco-maven-plugin | Code coverage | 10% minimum enforcement |

### 6.3 Build Phases

```bash
mvn clean install          # Full build + test + install
mvn test                   # Run tests only
mvn verify                 # Run tests + coverage check
mvn spotless:apply         # Format code
mvn checkstyle:check       # Lint check
```

### 6.4 Code Quality Gates

**Spotless (Google Java Format):**
- Runs in validate phase
- Auto-applies formatting on mvn clean
- Checks on verify phase (fails build if issues)

**Checkstyle:**
- Custom configuration (checkstyle.xml)
- Suppressions file (checkstyle-suppressions.xml)
- Runs in validate phase
- Fails build on violations

**JaCoCo:**
- Minimum 10% line coverage required
- Minimum 10% branch coverage required
- Runs in verify phase
- HTML report: target/site/jacoco/index.html

### 6.5 Dependency Management

**BOMs (Bill of Materials):**
- adentic-ee-bom (imports SE BOM)
- adentic-se-bom (manages core dependencies)

**Managed Dependencies:**
- Jackson (JSON serialization)
- SLF4J + Logback (logging)
- Project Reactor (reactive)
- JUnit 5, Mockito, AssertJ (testing)

**Direct Dependencies:**
- Javalin 6.1.3 (HTTP server)
- Lombok 1.18.34 (code generation)
- H2 2.2.224 (in-memory database)

---

## 7. PROJECT STRUCTURE

```
adentic-boot/
├── pom.xml                          # Maven configuration
├── README.md                        # Project overview
├── ARCHITECTURE_SPLIT_PLAN.md      # Modular design docs
├── checkstyle.xml                  # Code linting rules
├── checkstyle-suppressions.xml     # Linting exceptions
├── src/main/java/
│   └── dev/adeengineer/adentic/
│       ├── boot/                    # Framework core (1,574 LOC)
│       │   ├── AgenticApplication.java
│       │   ├── AgenticContext.java
│       │   ├── annotations/         # Core annotations
│       │   ├── context/             # DI container
│       │   ├── event/               # Event bus
│       │   ├── registry/            # Provider registry
│       │   ├── scanner/             # Component scanner
│       │   └── web/                 # HTTP server
│       └── tool/                    # 9 tool implementations
├── src/test/java/                   # 45 test files, 25/25 passing
└── doc/                             # Comprehensive SDLC documentation
    ├── overview.md                  # Documentation index
    ├── executive-summary.md
    ├── 1-planning/                  # Requirements, planning
    ├── 2-analysis/                  # Use cases, feasibility
    ├── 3-design/                    # Architecture, workflows
    ├── 4-development/               # Developer guides
    ├── 5-testing/                   # Test strategies
    ├── 6-deployment/                # Deployment guides
    └── 7-maintenance/               # Operations, troubleshooting
```

---

## 8. WORKFLOW & DATAFLOW

### 8.1 Application Startup Flow

```
@AgenticBootApplication class
        ↓
AgenticApplication.run()
        ↓
Create AgenticContext (DI container)
        ↓
Register core beans:
  - EventBus
  - ProviderRegistry
  - AgenticServer
        ↓
Get @AgenticBootApplication config
        ↓
Determine base package
        ↓
ComponentScanner.scan()
  - Find all @Component classes
  - Find all provider annotations
        ↓
Register components in context
        ↓
ProviderRegistry.registerProviderFromClass()
  - Categorize by annotation
  - Extract provider name
        ↓
AgenticServer.registerController()
  - Find all @RestController classes
  - Register routes
        ↓
AgenticServer.start(port)
        ↓
Log startup info
        ↓
Add shutdown hook
```

### 8.2 Dependency Injection Flow

```
AgenticContext.getBean(Class<T>)
        ↓
Check singletons cache
        ↓
Check factories
        ↓
If factory exists:
  - Call factory.get()
  - Cache as singleton
  - Return
        ↓
instantiate(Class<T>)
        ↓
Check circular dependencies
        ↓
Find constructor:
  1. @Inject marked
  2. Single public
  3. No-arg
        ↓
Resolve parameter types
        ↓
Recursively getBean(paramType)
        ↓
Invoke constructor
        ↓
Return instance
```

### 8.3 HTTP Request Flow

```
HTTP Request (GET /path)
        ↓
Javalin routes request
        ↓
AgenticServer finds matching route
        ↓
Extract parameters:
  - @PathVariable
  - @RequestBody
  - @RequestParam
        ↓
Invoke controller method
        ↓
Method returns response
        ↓
Jackson serializes to JSON
        ↓
HTTP Response (200 OK + JSON body)
```

### 8.4 Event Publishing Flow

```
Component calls: eventBus.publish(event)
        ↓
EventBus finds listeners for event type
        ↓
For each listener:
  ├─ Synchronous: Call immediately
  └─ Asynchronous: Submit to thread pool
        ↓
Listener receives event
        ↓
Error handling catches exceptions
        ↓
Publishing complete
```

---

## 9. DESIGN PATTERNS USED

| Pattern | Component | Purpose |
|---------|-----------|---------|
| **Dependency Injection** | AgenticContext | Invert control, decouple components |
| **Registry** | ProviderRegistry | Centralized provider management |
| **Scanner** | ComponentScanner | Auto-discovery via reflection |
| **Factory** | AgenticContext.registerFactory() | Lazy instantiation |
| **Observer/Pub-Sub** | EventBus | Event-driven architecture |
| **Singleton** | AgenticContext | Single instance per type |
| **Strategy** | Provider annotations | Category-based implementation selection |
| **Template Method** | AgenticApplication | Fixed startup sequence |
| **Decorator** | @Component, @Service, @RestController | Add behavior via annotations |

---

## 10. PYTHON EQUIVALENT DESIGN CONSIDERATIONS

### 10.1 DI Container Equivalent

**Java:** Constructor injection with type hints
**Python:** Decorator-based with type hints + dependency resolver

```python
@injectable
class MyService:
    @inject
    def __init__(self, other_service: OtherService):
        self.other = other_service
```

### 10.2 Component Scanning Equivalent

**Java:** Reflection + classpath scanning
**Python:** Module introspection + decorator discovery

```python
from adentic import component_scanner
components = component_scanner.scan("my.package")
```

### 10.3 Annotation System Equivalent

**Java:** @Component, @Service, @RestController
**Python:** Decorators: @component, @service, @rest_controller

```python
@service
class UserService:
    pass
```

### 10.4 Provider Registry Equivalent

**Java:** Annotation-driven categorization
**Python:** Decorator with category parameter

```python
@llm_provider(name="openai")
class OpenAIProvider:
    pass
```

### 10.5 Event Bus Equivalent

**Java:** Type-safe Consumer<T>
**Python:** Type hints + callable listeners

```python
event_bus.subscribe(AgentCompletedEvent, lambda e: print(e))
event_bus.publish(AgentCompletedEvent(...))
```

### 10.6 HTTP Server Equivalent

**Java:** Javalin + auto-routing
**Python:** Flask/FastAPI + decorator-based routing

```python
@rest_controller
@request_mapping("/users")
class UserController:
    @get_mapping("/{id}")
    def get_user(self, id: str):
        return {"id": id}
```

---

## 11. BEST PRACTICES & CONVENTIONS

### 11.1 Naming Conventions

**Packages:** Singular nouns
- `dev.adeengineer.adentic.boot.annotation` (not annotations)
- `dev.adeengineer.adentic.boot.provider` (not providers)
- `dev.adeengineer.adentic.boot.registry` (not registries)

**Annotations:** Short, let package provide context
- `@LLM` in `boot.annotation.provider` package
- `@Storage` in `boot.annotation.provider` package
- NOT: @LLMProvider, @StorageProvider

**Classes:**
- `Service` suffix for services
- `Provider` suffix for providers
- `Tool` suffix for tools
- `Test` suffix for tests

### 11.2 Code Quality Standards

**Coverage:** 10% minimum (enforced by JaCoCo)
**Formatting:** Google Java Format (Spotless)
**Linting:** Checkstyle with configuration
**Tests:** @DisplayName, descriptive names
**Documentation:** Javadoc for public APIs

### 11.3 Commit Message Format

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types:** feat, fix, docs, refactor, test, chore, perf, style, ci

### 11.4 No AI Attribution in Commits

- ✅ Clean commits reflecting human decisions
- ✅ Attribution in project documentation
- ✅ AI treated as tool, like IDE or compiler

---

## 12. SUMMARY TABLE: JAVA → PYTHON EQUIVALENTS

| Java Concept | Implementation | Python Equivalent |
|--------------|-----------------|-------------------|
| @Component | Annotation | @component decorator |
| @Inject | Constructor annotation | @inject decorator or constructor type hints |
| AgenticContext | DI container class | Context or Container class |
| ComponentScanner | Reflection-based | Module introspection + decorators |
| ProviderRegistry | Category-based registry | Dict-based registry with categories |
| EventBus | Observer pattern | Event emitter or callback system |
| @RestController | HTTP controller annotation | @rest_controller or Flask/FastAPI route |
| JUnit 5 + AssertJ | Testing framework | pytest + assertions |
| Maven + Spotless | Build system | setuptools/poetry + black/ruff |
| JaCoCo | Coverage tool | coverage.py or pytest-cov |
| Javalin | HTTP framework | Flask, FastAPI, or Starlette |
| Jackson | JSON serialization | json, pydantic, or marshmallow |
| Lombok | Code generation | dataclasses or attrs |

---

## 13. KEY ARCHITECTURAL INSIGHTS

1. **Minimal Dependencies:** Only 9 external dependencies (vs 100+ for Spring)
2. **Layered Design:** Clear separation between contracts, annotations, implementations, and runtime
3. **Type Safety:** Heavy use of Java generics and type hints
4. **Convention over Config:** Annotations drive behavior, few external configs
5. **Startup Performance:** <1 second startup due to minimal reflection
6. **Single Responsibility:** Each component has one job, decoupled from others
7. **Thread Safety:** ConcurrentHashMap, CopyOnWriteArrayList for concurrent access
8. **Error Handling:** Explicit exceptions, good error messages for debugging
9. **Testing:** Comprehensive tests with good coverage, parametrized tests
10. **Quality Gates:** Automated checks (format, lint, coverage) in build pipeline

---

**End of Summary**
