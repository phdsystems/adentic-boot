# Python AgenticBoot Equivalent - Design Reference

**Quick Reference Guide for Building Python Version**

---

## Core Components to Implement

### 1. Dependency Injection Container
```python
class AgenticContext:
    """Lightweight DI container matching Java's AgenticContext"""
    
    - register_singleton(type, instance)
    - register_named_singleton(name, instance)
    - register_factory(type, factory_fn)
    - register_bean(bean_class)
    - get_bean(type) -> T
    - get_bean_by_name(name, type) -> T
    - contains_bean(type) -> bool
    - close()
    
    # Features:
    - Constructor injection via type hints
    - Circular dependency detection
    - Lazy factory instantiation
    - Thread-safe (thread pool for factories)
```

### 2. Component Scanner
```python
class ComponentScanner:
    """Auto-discovery of annotated components and providers"""
    
    - scan(base_package: str) -> Set[Type]
    - scan_for_annotation(annotation_type) -> Set[Type]
    - scan_providers() -> Dict[str, Set[Type]]
    
    # Supported annotations:
    @component, @service, @rest_controller
    @llm, @infrastructure, @storage, @messaging
    @orchestration, @memory, @queue, @tool, @evaluation
```

### 3. Provider Registry
```python
class ProviderRegistry:
    """Category-based provider management"""
    
    - register_provider(category, name, instance)
    - register_provider_from_class(provider_class, instance)
    - get_provider(category, name) -> Optional[Object]
    - get_providers_by_category(category) -> Dict[str, Object]
    - get_categories() -> Set[str]
    - get_provider_count(category) -> int
    - has_provider(category, name) -> bool
    
    # Categories:
    llm, infrastructure, storage, messaging, orchestration
    memory, queue, tool, evaluation
```

### 4. Event Bus
```python
class EventBus:
    """Type-safe pub/sub event system"""
    
    - subscribe(event_type: Type[T], listener: Callable[[T], None])
    - subscribe_async(event_type: Type[T], listener: Callable[[T], None])
    - publish(event: T)
    - unsubscribe(event_type, listener)
    - unsubscribe_all(event_type)
    - get_listener_count(event_type) -> int
    - close()
    
    # Features:
    - Synchronous delivery (blocking)
    - Asynchronous delivery (thread pool, 10 default threads)
    - Type-safe via type hints
    - Error handling for listener exceptions
```

### 5. HTTP Server
```python
class AgenticServer:
    """REST controller auto-registration and HTTP routing"""
    
    - register_controller(controller_instance)
    - start(port: int)
    - close()
    
    # Features:
    - Auto-discovery of @rest_controller classes
    - Automatic route registration from @get_mapping, @post_mapping
    - Parameter injection (@path_variable, @request_body, @request_param)
    - JSON serialization (pydantic models)
    - CORS enabled by default
    - Health check endpoint (/health)
```

### 6. Application Bootstrap
```python
class AgenticApplication:
    """Main application entry point"""
    
    @staticmethod
    def run(app_class: Type, *args) -> AgenticContext:
        """
        1. Print banner
        2. Create AgenticContext
        3. Register core beans
        4. Get config from @agenic_boot_app
        5. Scan components
        6. Register components
        7. Scan and register providers
        8. Register REST controllers
        9. Start HTTP server
        10. Add shutdown hook
        """
```

---

## Decorator System

### Core Decorators
```python
@agenic_boot_app(port=8080, scan_base_packages="my.package")
class MyApp:
    pass

@component
class MyComponent:
    pass

@service
class MyService:
    pass

@rest_controller
@request_mapping("/api/users")
class UserController:
    @get_mapping("/{id}")
    def get_user(self, user_id: str) -> Dict:
        return {"id": user_id}

    @post_mapping("/")
    def create_user(self, user: UserRequest) -> User:
        return user
```

### Provider Decorators
```python
@llm_provider(name="openai")
class OpenAIProvider:
    pass

@storage_provider(name="s3")
class S3StorageProvider:
    pass

@database_provider(name="postgres")
class PostgresProvider:
    pass
```

### DI Decorators
```python
class UserService:
    def __init__(self, db: DatabaseProvider):
        # Type hint drives injection
        self.db = db

# Alternative with explicit @inject if needed:
@service
class OrderService:
    @inject
    def __init__(self, user_svc: UserService):
        self.user = user_svc
```

---

## Configuration

### Zero-Config Pattern
```python
# Minimal app - everything from decorators
@agenic_boot_app
class App:
    pass

if __name__ == "__main__":
    AgenticApplication.run(App)
    # Defaults:
    # - port: 8080
    # - host: 0.0.0.0
    # - base package: app's package
```

### Config Levels
1. **Decorator-based**: @agenic_boot_app(port=9000, ...)
2. **Programmatic**: AgenticApplication.run(App, config={...})
3. **Properties file**: application.properties (optional)

---

## Testing Patterns

```python
# Test class structure
class TestAgenticContext:
    """Using pytest"""
    
    def setup_method(self):
        self.context = AgenticContext()
    
    def test_should_register_singleton(self):
        service = TestService("test")
        self.context.register_singleton(TestService, service)
        
        assert self.context.contains_bean(TestService)
        assert self.context.get_bean(TestService) is service
    
    def test_should_auto_wire_dependencies(self):
        other = OtherService()
        self.context.register_singleton(OtherService, other)
        self.context.register_bean(ServiceWithDeps)
        
        service = self.context.get_bean(ServiceWithDeps)
        assert service.other is other
    
    def test_should_detect_circular_dependencies(self):
        self.context.register_bean(CircularA)
        self.context.register_bean(CircularB)
        
        with pytest.raises(CircularDependencyError):
            self.context.get_bean(CircularA)
```

---

## Build & Quality

### Build System
- **Tool**: setuptools + poetry (or just setuptools)
- **Testing**: pytest
- **Coverage**: coverage.py / pytest-cov
- **Formatting**: black
- **Linting**: ruff / pylint
- **Type checking**: mypy

### Quality Gates (Similar to Java)
```bash
# Format check
black --check src/

# Linting
ruff check src/

# Type checking
mypy src/

# Test with coverage
pytest --cov=adentic_boot --cov-report=html

# All checks
make quality  # or similar script
```

---

## Module Structure

```
adentic-boot-python/
├── pyproject.toml                    # Poetry/setuptools config
├── README.md
├── ARCHITECTURE.md
├── src/
│   └── adentic_boot/
│       ├── __init__.py
│       ├── application.py            # AgenticApplication
│       ├── context.py                # AgenticContext (DI)
│       ├── annotations.py            # Decorators
│       ├── scanner.py                # ComponentScanner
│       ├── registry.py               # ProviderRegistry
│       ├── event_bus.py              # EventBus
│       ├── server.py                 # AgenticServer
│       └── exceptions.py             # Custom exceptions
├── tests/
│   ├── test_context.py
│   ├── test_scanner.py
│   ├── test_registry.py
│   ├── test_event_bus.py
│   ├── test_server.py
│   └── test_application.py
└── doc/
    ├── overview.md
    ├── architecture.md
    └── ... (SDLC docs)
```

---

## Key Differences: Java → Python

| Java | Python |
|------|--------|
| @Component annotation | @component decorator |
| @Inject on constructor | Type hints in __init__ |
| Class-based reflection | Module introspection + decorators |
| Constructor overloading | Single __init__ with type hints |
| Generic<T> type safety | Type hints (typing module) |
| Circular dependency detection | Track instantiation stack |
| Thread pool (ExecutorService) | ThreadPoolExecutor |
| ConcurrentHashMap | threading.Lock + dict |
| Optional<T> | Optional[T] from typing |
| Interface implements | ABC + inheritance |
| Maven build | Poetry/setuptools |
| JUnit 5 | pytest |
| Spotless formatting | black |
| Checkstyle linting | ruff |

---

## Implementation Priority

1. **Phase 1 (Core)**
   - AgenticContext (DI container)
   - @component, @service decorators
   - Basic component scanning
   - Tests for DI

2. **Phase 2 (Registry & Discovery)**
   - ComponentScanner improvements
   - ProviderRegistry
   - Provider decorators
   - Meta-annotation support

3. **Phase 3 (HTTP & Events)**
   - EventBus implementation
   - AgenticServer (REST)
   - HTTP decorators
   - Integration tests

4. **Phase 4 (Polish)**
   - AgenticApplication bootstrap
   - Configuration system
   - Documentation
   - Quality gates

---

## Code Quality Standards

- **Coverage**: 10% minimum (like Java)
- **Type hints**: All public functions/classes
- **Docstrings**: Google style for all public APIs
- **Line length**: 100 chars (black default)
- **Naming**: snake_case for functions/variables, PascalCase for classes
- **Error messages**: Clear, actionable, same level as Java version
- **Thread safety**: Same guarantees as Java (concurrent access patterns)

---

## Key Insights from Java Implementation

1. **Simplicity First**: 1,574 LOC for core framework
2. **Strict Layering**: Clear separation between concerns
3. **Type Safety**: Use type hints throughout (like Java generics)
4. **No Reflection Magic**: Explicit is better than implicit
5. **Good Errors**: Circular dependency detection, clear messages
6. **Testability**: DI-first design, injectable dependencies
7. **Convention**: Let defaults work, allow override
8. **Quality**: Automated formatting, linting, coverage checks
9. **Documentation**: Comprehensive SDLC docs alongside code
10. **Single Responsibility**: Each component does one thing well

