# Python AgenticBoot Framework - Comprehensive Design

**Design Date:** November 4, 2025
**Target Python:** 3.14+
**Package Manager:** uv (not pip)
**Build System:** Makefile
**Quality Control:** Built-in (ruff, mypy, black, pytest)

---

## TL;DR

**Python AgenticBoot** is a lightweight dependency injection and application framework inspired by the Java AgenticBoot implementation. **Key features**: Zero-config DI container, auto-discovery via decorators, type-safe provider registry, built-in HTTP server, event bus. **Target**: Sub-second startup, <50MB memory, <10 dependencies. **Structure**: Maven-like `src/main/python` and `src/test/python` layout with Makefile orchestration and mandatory quality gates (formatting, linting, type checking, 10% coverage minimum).

---

## Table of Contents

- [Executive Overview](#executive-overview)
- [Project Structure](#project-structure)
- [Core Architecture](#core-architecture)
- [Component Design](#component-design)
- [Decorator System](#decorator-system)
- [Build System & Quality Control](#build-system--quality-control)
- [Dependencies](#dependencies)
- [Testing Strategy](#testing-strategy)
- [Configuration System](#configuration-system)
- [Implementation Phases](#implementation-phases)
- [Code Examples](#code-examples)
- [Performance Targets](#performance-targets)
- [Comparison: Java vs Python](#comparison-java-vs-python)

---

## Executive Overview

### What is Python AgenticBoot?

A **lightweight application framework** for Python 3.14+ that provides:
- Constructor-based dependency injection
- Automatic component discovery
- Type-safe provider registry (9 categories)
- Built-in HTTP server with auto-routing
- In-process event bus (sync/async)
- Zero-configuration startup

### Design Goals

1. **Lightweight**: <10 dependencies, <50MB memory, <1s startup
2. **Type-Safe**: Full type hints, mypy strict mode
3. **Zero-Config**: Convention over configuration
4. **Quality-First**: Mandatory formatting, linting, type checking, 10% coverage
5. **Developer-Friendly**: Clear errors, comprehensive docs, easy debugging
6. **Production-Ready**: Thread-safe, error handling, observability

### Why Not Use Existing Frameworks?

| Framework | Issue | AgenticBoot Solution |
|-----------|-------|---------------------|
| **Django** | Heavy, opinionated ORM | Lightweight, provider-based |
| **FastAPI** | API-first, no DI | DI container + HTTP optional |
| **Flask** | No DI, manual wiring | Auto-discovery, DI built-in |
| **Spring Boot (Jython)** | Jython dead, no Python 3 | Native Python 3.14+ |

---

## Project Structure

### Directory Layout

```
adentic-boot-python/
├── Makefile                           # Build orchestration
├── pyproject.toml                     # uv configuration
├── uv.lock                            # Dependency lock file
├── README.md                          # Project overview
├── ARCHITECTURE.md                    # Architecture details
├── CONTRIBUTING.md                    # Contribution guide
│
├── src/
│   └── main/
│       └── python/
│           └── adentic_boot/
│               ├── __init__.py                    # Public API exports
│               ├── application.py                 # AgenticApplication (bootstrap)
│               ├── context.py                     # AgenticContext (DI container)
│               ├── annotations.py                 # Decorator system
│               ├── scanner.py                     # ComponentScanner
│               ├── registry.py                    # ProviderRegistry
│               ├── event_bus.py                   # EventBus (pub/sub)
│               ├── server.py                      # AgenticServer (HTTP)
│               ├── exceptions.py                  # Custom exceptions
│               └── types.py                       # Type definitions
│
├── src/
│   └── test/
│       └── python/
│           └── adentic_boot/
│               ├── __init__.py
│               ├── test_context.py               # DI container tests
│               ├── test_scanner.py               # Scanner tests
│               ├── test_registry.py              # Registry tests
│               ├── test_event_bus.py             # Event bus tests
│               ├── test_server.py                # HTTP server tests
│               ├── test_application.py           # Bootstrap tests
│               ├── test_integration.py           # End-to-end tests
│               └── fixtures/                     # Test fixtures
│
└── doc/                                          # SDLC documentation
    ├── overview.md                               # Master index
    ├── 0-ideation/
    ├── 1-planning/
    ├── 2-analysis/
    ├── 3-design/
    ├── 4-development/
    ├── 5-testing/
    ├── 6-deployment/
    └── 7-maintenance/
```

### Why Maven-Style Structure?

- **Clear separation** between source and test code
- **Explicit namespacing** (`src/main/python` vs `src/test/python`)
- **Tool compatibility** (pytest, mypy, coverage)
- **Professional standard** (used by Spring, Maven, Gradle)
- **Scales well** for large projects

---

## Core Architecture

### 4-Layer Modular Design

```
┌────────────────────────────────────────────┐
│  Layer 1: Contracts (types.py)             │
│  - Pure type definitions, protocols         │
│  - Zero runtime dependencies                │
└────────────────────────────────────────────┘
                    ↓
┌────────────────────────────────────────────┐
│  Layer 2: Annotations (annotations.py)     │
│  - Decorators (@component, @llm, etc.)     │
│  - No business logic                        │
└────────────────────────────────────────────┘
                    ↓
┌────────────────────────────────────────────┐
│  Layer 3: Core Framework                   │
│  - AgenticContext (DI)                     │
│  - ComponentScanner (discovery)            │
│  - ProviderRegistry (providers)            │
│  - EventBus (messaging)                    │
│  - AgenticServer (HTTP)                    │
└────────────────────────────────────────────┘
                    ↓
┌────────────────────────────────────────────┐
│  Layer 4: Bootstrap (application.py)       │
│  - AgenticApplication.run()                │
│  - Orchestrates startup sequence           │
└────────────────────────────────────────────┘
```

### Design Principles

1. **Separation of Concerns**: Each layer has a single responsibility
2. **No Circular Dependencies**: Strict layer hierarchy
3. **Type Safety**: Type hints throughout, mypy strict mode
4. **Explicit Over Implicit**: Clear errors, no magic
5. **Convention Over Configuration**: Sensible defaults
6. **Testability**: DI-first design, easy mocking

---

## Component Design

### 1. AgenticContext (Dependency Injection Container)

**Purpose:** Type-safe singleton container with constructor injection

**File:** `src/main/python/adentic_boot/context.py`

**Key Features:**
- Constructor-based dependency injection via type hints
- Singleton scope only (no prototype/request scope)
- Circular dependency detection with clear error messages
- Thread-safe bean registration and retrieval
- Factory function support for lazy instantiation
- Named bean support

**Core Methods:**

```python
class AgenticContext:
    def register_singleton(self, bean_type: Type[T], instance: T) -> None:
        """Register a pre-created singleton instance"""

    def register_named_singleton(self, name: str, instance: Any) -> None:
        """Register a named singleton (name-based lookup)"""

    def register_factory(self, bean_type: Type[T], factory: Callable[[], T]) -> None:
        """Register a factory function for lazy instantiation"""

    def register_bean(self, bean_class: Type[T]) -> None:
        """Register a class for auto-wiring (DI via __init__ type hints)"""

    def get_bean(self, bean_type: Type[T]) -> T:
        """Retrieve bean by type (raises BeanNotFoundError if missing)"""

    def get_bean_by_name(self, name: str, expected_type: Type[T]) -> T:
        """Retrieve bean by name with type checking"""

    def contains_bean(self, bean_type: Type[T]) -> bool:
        """Check if bean is registered"""

    def close(self) -> None:
        """Cleanup resources (call __del__ on closeable beans)"""
```

**Implementation Details:**

```python
from typing import TypeVar, Type, Dict, Any, Callable, Set, Optional
from threading import RLock
import inspect

T = TypeVar('T')

class AgenticContext:
    def __init__(self):
        self._singletons: Dict[Type, Any] = {}
        self._named_beans: Dict[str, Any] = {}
        self._factories: Dict[Type, Callable] = {}
        self._instantiating: Set[Type] = set()  # Circular dependency tracking
        self._lock = RLock()

    def register_bean(self, bean_class: Type[T]) -> None:
        """Auto-wire dependencies via __init__ type hints"""
        with self._lock:
            if bean_class in self._singletons:
                return  # Already registered

            # Check for circular dependencies
            if bean_class in self._instantiating:
                cycle = " -> ".join(str(t.__name__) for t in self._instantiating)
                raise CircularDependencyError(
                    f"Circular dependency detected: {cycle} -> {bean_class.__name__}"
                )

            self._instantiating.add(bean_class)
            try:
                # Get __init__ signature
                sig = inspect.signature(bean_class.__init__)
                params = sig.parameters

                # Resolve dependencies
                kwargs = {}
                for param_name, param in params.items():
                    if param_name == 'self':
                        continue

                    if param.annotation == inspect.Parameter.empty:
                        raise DependencyResolutionError(
                            f"Parameter '{param_name}' in {bean_class.__name__}.__init__ "
                            "has no type hint"
                        )

                    # Resolve dependency recursively
                    dependency = self.get_bean(param.annotation)
                    kwargs[param_name] = dependency

                # Instantiate bean
                instance = bean_class(**kwargs)
                self._singletons[bean_class] = instance

            finally:
                self._instantiating.discard(bean_class)

    def get_bean(self, bean_type: Type[T]) -> T:
        """Retrieve or create bean"""
        with self._lock:
            # Check if already instantiated
            if bean_type in self._singletons:
                return self._singletons[bean_type]

            # Check if factory exists
            if bean_type in self._factories:
                instance = self._factories[bean_type]()
                self._singletons[bean_type] = instance
                return instance

            # Try to auto-register and instantiate
            try:
                self.register_bean(bean_type)
                return self._singletons[bean_type]
            except Exception as e:
                raise BeanNotFoundError(
                    f"Bean of type {bean_type.__name__} not found and could not be created"
                ) from e
```

---

### 2. ComponentScanner (Auto-Discovery)

**Purpose:** Scan modules for decorated classes and register them

**File:** `src/main/python/adentic_boot/scanner.py`

**Key Features:**
- Module introspection for decorated classes
- Meta-decorator support (`@component`, `@service`, `@rest_controller`)
- Provider categorization (9 categories: llm, storage, messaging, etc.)
- Recursive package scanning
- Configurable base package

**Core Methods:**

```python
class ComponentScanner:
    def __init__(self, context: AgenticContext):
        self.context = context
        self.discovered_components: Set[Type] = set()
        self.discovered_providers: Dict[str, Set[Type]] = {}

    def scan(self, base_package: str) -> Set[Type]:
        """Scan package for @component, @service, @rest_controller classes"""

    def scan_providers(self, base_package: str) -> Dict[str, Set[Type]]:
        """Scan for provider annotations (@llm, @storage, etc.)"""

    def register_components(self) -> None:
        """Register all discovered components in context"""
```

**Implementation Strategy:**

```python
import importlib
import pkgutil
import sys
from pathlib import Path

class ComponentScanner:
    COMPONENT_DECORATORS = ['component', 'service', 'rest_controller']
    PROVIDER_CATEGORIES = ['llm', 'storage', 'messaging', 'infrastructure',
                          'orchestration', 'memory', 'queue', 'tool', 'evaluation']

    def scan(self, base_package: str) -> Set[Type]:
        """Walk package tree and find decorated classes"""
        components = set()

        # Import base package
        try:
            base_module = importlib.import_module(base_package)
        except ImportError as e:
            raise ScanError(f"Cannot import base package '{base_package}': {e}")

        # Get package path
        if hasattr(base_module, '__path__'):
            package_path = base_module.__path__
        else:
            raise ScanError(f"'{base_package}' is not a package")

        # Walk all modules
        for importer, modname, ispkg in pkgutil.walk_packages(
            package_path, prefix=f"{base_package}."
        ):
            try:
                module = importlib.import_module(modname)
                components.update(self._scan_module(module))
            except Exception as e:
                # Log warning but continue scanning
                print(f"Warning: Failed to scan module '{modname}': {e}")

        return components

    def _scan_module(self, module) -> Set[Type]:
        """Inspect module for decorated classes"""
        components = set()

        for name in dir(module):
            obj = getattr(module, name)

            # Check if it's a class
            if not isinstance(obj, type):
                continue

            # Check for component decorators
            if hasattr(obj, '_adentic_component'):
                components.add(obj)

        return components
```

---

### 3. ProviderRegistry (Provider Management)

**Purpose:** Category-based provider registration and lookup

**File:** `src/main/python/adentic_boot/registry.py`

**Key Features:**
- 9 built-in categories (llm, storage, messaging, etc.)
- Name-based provider lookup within categories
- Thread-safe registration and retrieval
- Provider metadata (name, category, instance)

**Core Methods:**

```python
class ProviderRegistry:
    def __init__(self):
        self._providers: Dict[str, Dict[str, Any]] = {}
        self._lock = RLock()

    def register_provider(self, category: str, name: str, instance: Any) -> None:
        """Register provider in category with name"""

    def register_provider_from_class(self, provider_class: Type) -> None:
        """Extract category/name from @llm/@storage decorator and register"""

    def get_provider(self, category: str, name: str) -> Optional[Any]:
        """Get provider by category and name"""

    def get_providers_by_category(self, category: str) -> Dict[str, Any]:
        """Get all providers in category"""

    def has_provider(self, category: str, name: str) -> bool:
        """Check if provider exists"""

    def get_categories(self) -> Set[str]:
        """Get all registered categories"""
```

**Implementation:**

```python
from typing import Dict, Any, Optional, Set, Type
from threading import RLock

class ProviderRegistry:
    # Category constants
    CATEGORY_LLM = "llm"
    CATEGORY_STORAGE = "storage"
    CATEGORY_MESSAGING = "messaging"
    CATEGORY_INFRASTRUCTURE = "infrastructure"
    CATEGORY_ORCHESTRATION = "orchestration"
    CATEGORY_MEMORY = "memory"
    CATEGORY_QUEUE = "queue"
    CATEGORY_TOOL = "tool"
    CATEGORY_EVALUATION = "evaluation"

    def __init__(self):
        self._providers: Dict[str, Dict[str, Any]] = {}
        self._lock = RLock()

    def register_provider(self, category: str, name: str, instance: Any) -> None:
        with self._lock:
            if category not in self._providers:
                self._providers[category] = {}

            if name in self._providers[category]:
                raise ProviderAlreadyRegisteredError(
                    f"Provider '{name}' already registered in category '{category}'"
                )

            self._providers[category][name] = instance

    def get_provider(self, category: str, name: str) -> Optional[Any]:
        with self._lock:
            if category not in self._providers:
                return None
            return self._providers[category].get(name)
```

---

### 4. EventBus (Pub/Sub Messaging)

**Purpose:** Type-safe in-process event system

**File:** `src/main/python/adentic_boot/event_bus.py`

**Key Features:**
- Type-safe event listeners (based on event class)
- Synchronous (blocking) and asynchronous (thread pool) delivery
- Error handling for listener exceptions
- Listener lifecycle management (subscribe/unsubscribe)

**Core Methods:**

```python
class EventBus:
    def __init__(self, max_workers: int = 10):
        self._listeners: Dict[Type, List[Callable]] = {}
        self._async_listeners: Dict[Type, List[Callable]] = {}
        self._executor = ThreadPoolExecutor(max_workers=max_workers)
        self._lock = RLock()

    def subscribe(self, event_type: Type[T], listener: Callable[[T], None]) -> None:
        """Subscribe synchronous listener"""

    def subscribe_async(self, event_type: Type[T], listener: Callable[[T], None]) -> None:
        """Subscribe asynchronous listener (thread pool)"""

    def publish(self, event: T) -> None:
        """Publish event to all listeners (sync first, then async)"""

    def unsubscribe(self, event_type: Type[T], listener: Callable) -> None:
        """Unsubscribe specific listener"""

    def unsubscribe_all(self, event_type: Type[T]) -> None:
        """Remove all listeners for event type"""

    def close(self) -> None:
        """Shutdown thread pool"""
```

---

### 5. AgenticServer (HTTP Server)

**Purpose:** REST controller auto-registration with parameter injection

**File:** `src/main/python/adentic_boot/server.py`

**Key Features:**
- Auto-routing from `@rest_controller` and `@get_mapping/@post_mapping`
- Parameter injection (`@path_variable`, `@request_body`, `@request_param`)
- JSON serialization (pydantic models)
- CORS enabled by default
- Health check endpoint (`/health`)

**HTTP Framework Choice:** **Starlette** (lightweight ASGI, 15KB, mature)

**Core Methods:**

```python
class AgenticServer:
    def __init__(self, context: AgenticContext, port: int = 8080, host: str = "0.0.0.0"):
        self.context = context
        self.port = port
        self.host = host
        self.app = Starlette()
        self._setup_routes()

    def register_controller(self, controller_instance: Any) -> None:
        """Register REST controller and auto-create routes"""

    def start(self) -> None:
        """Start HTTP server (blocking)"""

    def start_async(self) -> asyncio.Task:
        """Start HTTP server (non-blocking)"""

    def close(self) -> None:
        """Stop HTTP server"""
```

---

### 6. AgenticApplication (Bootstrap)

**Purpose:** Orchestrate application startup sequence

**File:** `src/main/python/adentic_boot/application.py`

**Startup Workflow (10 steps):**

```python
class AgenticApplication:
    @staticmethod
    def run(app_class: Type, port: int = 8080, host: str = "0.0.0.0") -> AgenticContext:
        """
        1. Print banner
        2. Create AgenticContext
        3. Register core beans (EventBus, ProviderRegistry, AgenticServer)
        4. Extract config from @agenic_boot_app decorator
        5. Create ComponentScanner
        6. Scan for components (@component, @service)
        7. Register components in context
        8. Scan for providers (@llm, @storage, etc.)
        9. Register providers in registry
        10. Scan for REST controllers
        11. Register controllers in server
        12. Start HTTP server
        13. Add shutdown hook (signal handlers)
        14. Return context
        """
```

---

## Decorator System

### Core Decorators

```python
# annotations.py

def agenic_boot_app(
    port: int = 8080,
    host: str = "0.0.0.0",
    scan_base_packages: Optional[str] = None
):
    """Mark main application class"""
    def decorator(cls):
        cls._adentic_app_config = {
            'port': port,
            'host': host,
            'scan_base_packages': scan_base_packages or cls.__module__.rsplit('.', 1)[0]
        }
        return cls
    return decorator

def component(cls):
    """Mark class as component (generic bean)"""
    cls._adentic_component = True
    cls._adentic_component_type = 'component'
    return cls

def service(cls):
    """Mark class as service (business logic)"""
    cls._adentic_component = True
    cls._adentic_component_type = 'service'
    return cls

def rest_controller(cls):
    """Mark class as REST controller"""
    cls._adentic_component = True
    cls._adentic_component_type = 'rest_controller'
    return cls
```

### Provider Decorators

```python
def llm_provider(name: str):
    """Mark class as LLM provider"""
    def decorator(cls):
        cls._adentic_provider = True
        cls._adentic_provider_category = 'llm'
        cls._adentic_provider_name = name
        return cls
    return decorator

def storage_provider(name: str):
    """Mark class as storage provider"""
    def decorator(cls):
        cls._adentic_provider = True
        cls._adentic_provider_category = 'storage'
        cls._adentic_provider_name = name
        return cls
    return decorator

# Similar for: messaging, infrastructure, orchestration, memory, queue, tool, evaluation
```

### HTTP Decorators

```python
def request_mapping(path: str):
    """Class-level path prefix"""
    def decorator(cls):
        cls._adentic_base_path = path
        return cls
    return decorator

def get_mapping(path: str):
    """Map method to GET request"""
    def decorator(func):
        func._adentic_http_method = 'GET'
        func._adentic_http_path = path
        return func
    return decorator

def post_mapping(path: str):
    """Map method to POST request"""
    def decorator(func):
        func._adentic_http_method = 'POST'
        func._adentic_http_path = path
        return func
    return decorator

# Similar for: put_mapping, delete_mapping, patch_mapping
```

### Parameter Injection Decorators

```python
def path_variable(name: str):
    """Extract path variable (e.g., /users/{id})"""
    def decorator(func):
        if not hasattr(func, '_adentic_param_bindings'):
            func._adentic_param_bindings = {}
        func._adentic_param_bindings[name] = {'type': 'path', 'name': name}
        return func
    return decorator

def request_body(cls: Type[T]):
    """Parse JSON body into pydantic model"""
    def decorator(func):
        func._adentic_request_body_type = cls
        return func
    return decorator

def request_param(name: str, required: bool = True, default: Any = None):
    """Extract query parameter"""
    def decorator(func):
        if not hasattr(func, '_adentic_query_params'):
            func._adentic_query_params = {}
        func._adentic_query_params[name] = {
            'required': required,
            'default': default
        }
        return func
    return decorator
```

---

## Build System & Quality Control

### Makefile

```makefile
# Makefile for Python AgenticBoot

PYTHON := python3.14
UV := uv
SRC_DIR := src/main/python
TEST_DIR := src/test/python
PACKAGE := adentic_boot

# Colors for output
GREEN := \033[0;32m
YELLOW := \033[0;33m
RED := \033[0;31m
NC := \033[0m # No Color

.PHONY: help
help:
	@echo "Python AgenticBoot - Build Commands"
	@echo ""
	@echo "  make install       - Install dependencies with uv"
	@echo "  make format        - Format code with black and ruff"
	@echo "  make format-check  - Check code formatting"
	@echo "  make lint          - Lint code with ruff"
	@echo "  make typecheck     - Type check with mypy"
	@echo "  make test          - Run tests with pytest"
	@echo "  make coverage      - Run tests with coverage report"
	@echo "  make quality       - Run all quality checks (format, lint, typecheck, test)"
	@echo "  make build         - Build distribution packages"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make dev           - Install package in editable mode"

.PHONY: install
install:
	@echo "$(GREEN)Installing dependencies with uv...$(NC)"
	$(UV) pip install -e .
	$(UV) pip install -r requirements-dev.txt

.PHONY: format
format:
	@echo "$(GREEN)Formatting code with black...$(NC)"
	black $(SRC_DIR) $(TEST_DIR)
	@echo "$(GREEN)Fixing import order with ruff...$(NC)"
	ruff check --select I --fix $(SRC_DIR) $(TEST_DIR)

.PHONY: format-check
format-check:
	@echo "$(YELLOW)Checking code formatting...$(NC)"
	black --check $(SRC_DIR) $(TEST_DIR)
	ruff check --select I $(SRC_DIR) $(TEST_DIR)

.PHONY: lint
lint:
	@echo "$(YELLOW)Linting code with ruff...$(NC)"
	ruff check $(SRC_DIR) $(TEST_DIR)

.PHONY: typecheck
typecheck:
	@echo "$(YELLOW)Type checking with mypy...$(NC)"
	mypy $(SRC_DIR)

.PHONY: test
test:
	@echo "$(YELLOW)Running tests with pytest...$(NC)"
	pytest $(TEST_DIR) -v --tb=short

.PHONY: coverage
coverage:
	@echo "$(YELLOW)Running tests with coverage...$(NC)"
	pytest $(TEST_DIR) \
		--cov=$(SRC_DIR)/$(PACKAGE) \
		--cov-report=html \
		--cov-report=term \
		--cov-fail-under=10
	@echo "$(GREEN)Coverage report: htmlcov/index.html$(NC)"

.PHONY: quality
quality: format-check lint typecheck coverage
	@echo "$(GREEN)✓ All quality checks passed!$(NC)"

.PHONY: build
build: quality
	@echo "$(GREEN)Building distribution packages...$(NC)"
	$(UV) pip install build
	$(PYTHON) -m build

.PHONY: clean
clean:
	@echo "$(YELLOW)Cleaning build artifacts...$(NC)"
	rm -rf dist/ build/ *.egg-info
	rm -rf htmlcov/ .coverage .pytest_cache .mypy_cache .ruff_cache
	find . -type d -name __pycache__ -exec rm -rf {} +
	find . -type f -name "*.pyc" -delete

.PHONY: dev
dev:
	@echo "$(GREEN)Installing package in editable mode...$(NC)"
	$(UV) pip install -e ".[dev]"

.PHONY: pre-commit
pre-commit: quality
	@echo "$(GREEN)✓ Pre-commit checks passed!$(NC)"
```

### pyproject.toml (uv configuration)

```toml
[project]
name = "adentic-boot"
version = "0.1.0"
description = "Lightweight dependency injection and application framework for Python 3.14+"
authors = [
    {name = "PHD Systems", email = "contact@phdsystems.com"}
]
readme = "README.md"
requires-python = ">=3.14"
license = {text = "MIT"}
keywords = ["dependency-injection", "di", "ioc", "framework", "http-server"]
classifiers = [
    "Development Status :: 3 - Alpha",
    "Intended Audience :: Developers",
    "License :: OSI Approved :: MIT License",
    "Programming Language :: Python :: 3.14",
    "Topic :: Software Development :: Libraries :: Application Frameworks",
]

dependencies = [
    "starlette>=0.37.0",      # HTTP server (ASGI)
    "uvicorn>=0.29.0",        # ASGI server
    "pydantic>=2.7.0",        # Data validation
]

[project.optional-dependencies]
dev = [
    "pytest>=8.0.0",
    "pytest-cov>=5.0.0",
    "pytest-asyncio>=0.23.0",
    "black>=24.0.0",
    "ruff>=0.4.0",
    "mypy>=1.10.0",
]

[project.urls]
Homepage = "https://github.com/phdsystems/adentic-boot-python"
Documentation = "https://adentic-boot.readthedocs.io"
Repository = "https://github.com/phdsystems/adentic-boot-python"
Issues = "https://github.com/phdsystems/adentic-boot-python/issues"

[build-system]
requires = ["setuptools>=68.0.0", "wheel"]
build-backend = "setuptools.build_meta"

[tool.setuptools]
package-dir = {"" = "src/main/python"}

[tool.setuptools.packages.find]
where = ["src/main/python"]

# Black configuration
[tool.black]
line-length = 100
target-version = ['py314']
include = '\.pyi?$'
extend-exclude = '''
/(
  \.git
  | \.mypy_cache
  | \.pytest_cache
  | \.ruff_cache
  | dist
  | build
)/
'''

# Ruff configuration
[tool.ruff]
line-length = 100
target-version = "py314"
src = ["src/main/python", "src/test/python"]

[tool.ruff.lint]
select = [
    "E",   # pycodestyle errors
    "W",   # pycodestyle warnings
    "F",   # pyflakes
    "I",   # isort
    "B",   # flake8-bugbear
    "C4",  # flake8-comprehensions
    "UP",  # pyupgrade
]
ignore = []

[tool.ruff.lint.per-file-ignores]
"__init__.py" = ["F401"]  # Allow unused imports in __init__.py

# Mypy configuration
[tool.mypy]
python_version = "3.14"
warn_return_any = true
warn_unused_configs = true
disallow_untyped_defs = true
disallow_incomplete_defs = true
check_untyped_defs = true
no_implicit_optional = true
warn_redundant_casts = true
warn_unused_ignores = true
warn_no_return = true
strict_equality = true

[[tool.mypy.overrides]]
module = "tests.*"
disallow_untyped_defs = false

# Pytest configuration
[tool.pytest.ini_options]
testpaths = ["src/test/python"]
python_files = ["test_*.py"]
python_classes = ["Test*"]
python_functions = ["test_*"]
addopts = [
    "-v",
    "--strict-markers",
    "--tb=short",
    "--cov-fail-under=10",
]

# Coverage configuration
[tool.coverage.run]
source = ["src/main/python"]
omit = ["*/tests/*", "*/__pycache__/*"]

[tool.coverage.report]
precision = 2
show_missing = true
skip_covered = false
```

---

## Dependencies

### Core Dependencies (3)

| Package | Version | Purpose | Size |
|---------|---------|---------|------|
| **starlette** | >=0.37.0 | ASGI HTTP framework | ~150KB |
| **uvicorn** | >=0.29.0 | ASGI server | ~200KB |
| **pydantic** | >=2.7.0 | Data validation | ~500KB |

**Total:** ~850KB (vs Java's 5MB+)

### Development Dependencies (5)

| Package | Purpose |
|---------|---------|
| **pytest** | Testing framework |
| **pytest-cov** | Coverage reporting |
| **pytest-asyncio** | Async test support |
| **black** | Code formatting |
| **ruff** | Linting and import sorting |
| **mypy** | Type checking |

---

## Testing Strategy

### Test Organization

```
src/test/python/adentic_boot/
├── test_context.py           # DI container tests
├── test_scanner.py           # Component scanner tests
├── test_registry.py          # Provider registry tests
├── test_event_bus.py         # Event bus tests
├── test_server.py            # HTTP server tests
├── test_application.py       # Bootstrap tests
├── test_integration.py       # End-to-end tests
└── fixtures/
    ├── sample_app/           # Test application
    ├── sample_components.py
    └── sample_providers.py
```

### Test Coverage Requirements

- **Minimum:** 10% (enforced by pytest-cov)
- **Target:** 80%+ for core components
- **Strategy:**
  - Unit tests for each component
  - Integration tests for end-to-end flows
  - Fixture-based testing for realistic scenarios

### Example Test

```python
# test_context.py

import pytest
from adentic_boot.context import AgenticContext
from adentic_boot.exceptions import CircularDependencyError, BeanNotFoundError

class TestAgenticContext:
    def setup_method(self):
        """Create fresh context for each test"""
        self.context = AgenticContext()

    def test_register_singleton_by_type(self):
        """Should register and retrieve singleton by type"""
        service = TestService("test")
        self.context.register_singleton(TestService, service)

        assert self.context.contains_bean(TestService)
        assert self.context.get_bean(TestService) is service

    def test_auto_wire_dependencies(self):
        """Should resolve dependencies via __init__ type hints"""
        other = OtherService()
        self.context.register_singleton(OtherService, other)
        self.context.register_bean(ServiceWithDeps)

        service = self.context.get_bean(ServiceWithDeps)
        assert service.other is other

    def test_detect_circular_dependencies(self):
        """Should raise CircularDependencyError"""
        self.context.register_bean(CircularA)
        self.context.register_bean(CircularB)

        with pytest.raises(CircularDependencyError) as exc_info:
            self.context.get_bean(CircularA)

        assert "Circular dependency detected" in str(exc_info.value)
        assert "CircularA -> CircularB -> CircularA" in str(exc_info.value)

# Test fixtures
class TestService:
    def __init__(self, name: str):
        self.name = name

class OtherService:
    pass

class ServiceWithDeps:
    def __init__(self, other: OtherService):
        self.other = other

class CircularA:
    def __init__(self, b: 'CircularB'):
        self.b = b

class CircularB:
    def __init__(self, a: CircularA):
        self.a = a
```

---

## Configuration System

### Zero-Config Approach

```python
# Minimal application (everything inferred)
from adentic_boot import AgenticApplication, agenic_boot_app

@agenic_boot_app
class MyApp:
    pass

if __name__ == "__main__":
    AgenticApplication.run(MyApp)
    # Defaults:
    # - port: 8080
    # - host: 0.0.0.0
    # - scan_base_packages: 'my_app' (auto-detected from MyApp.__module__)
```

### Decorator-Based Config

```python
@agenic_boot_app(
    port=9000,
    host="localhost",
    scan_base_packages="my_app.components"
)
class MyApp:
    pass

if __name__ == "__main__":
    AgenticApplication.run(MyApp)
```

### Programmatic Config

```python
if __name__ == "__main__":
    context = AgenticApplication.run(
        MyApp,
        port=9000,
        host="localhost",
        scan_base_packages="my_app.components"
    )
    # Access context for manual bean registration if needed
```

---

## Implementation Phases

### Phase 1: Core DI (Week 1-2)

**Goal:** Working DI container with auto-wiring

**Tasks:**
- [ ] Create project structure (src/main/python, src/test/python)
- [ ] Setup uv, pyproject.toml, Makefile
- [ ] Implement `AgenticContext` (register_singleton, register_bean, get_bean)
- [ ] Implement circular dependency detection
- [ ] Implement `@component`, `@service` decorators
- [ ] Write tests for `AgenticContext`
- [ ] Setup quality gates (black, ruff, mypy, pytest-cov)

**Deliverables:**
- `context.py` (250-300 lines)
- `annotations.py` (core decorators, 50-100 lines)
- `exceptions.py` (20-30 lines)
- `test_context.py` (200+ lines)

---

### Phase 2: Discovery & Registry (Week 3-4)

**Goal:** Component scanning and provider registry

**Tasks:**
- [ ] Implement `ComponentScanner` (module introspection)
- [ ] Implement `ProviderRegistry` (9 categories)
- [ ] Implement provider decorators (`@llm_provider`, etc.)
- [ ] Meta-decorator support
- [ ] Write tests for scanner and registry
- [ ] Integration tests (scan + register)

**Deliverables:**
- `scanner.py` (250-300 lines)
- `registry.py` (200-250 lines)
- `annotations.py` (provider decorators, +100 lines)
- `test_scanner.py` (150+ lines)
- `test_registry.py` (150+ lines)

---

### Phase 3: HTTP & Events (Week 5-6)

**Goal:** HTTP server and event bus

**Tasks:**
- [ ] Implement `EventBus` (sync/async listeners)
- [ ] Implement `AgenticServer` (Starlette integration)
- [ ] Implement HTTP decorators (`@get_mapping`, etc.)
- [ ] Implement parameter injection (`@path_variable`, `@request_body`)
- [ ] Write tests for event bus and server
- [ ] Integration tests (full REST controller flow)

**Deliverables:**
- `event_bus.py` (150-200 lines)
- `server.py` (300-400 lines)
- `annotations.py` (HTTP decorators, +150 lines)
- `test_event_bus.py` (150+ lines)
- `test_server.py` (200+ lines)
- `test_integration.py` (200+ lines)

---

### Phase 4: Bootstrap & Polish (Week 7-8)

**Goal:** Application bootstrap and production readiness

**Tasks:**
- [ ] Implement `AgenticApplication.run()` (10-step startup)
- [ ] Implement shutdown hooks (signal handlers)
- [ ] Configuration system (decorator + programmatic)
- [ ] Comprehensive documentation (SDLC structure)
- [ ] Example applications (hello world, REST API, providers)
- [ ] Performance benchmarking
- [ ] Security audit

**Deliverables:**
- `application.py` (200-250 lines)
- `test_application.py` (150+ lines)
- Complete `doc/` structure (SDLC template)
- `examples/` directory (3+ apps)
- `ARCHITECTURE.md`
- `CONTRIBUTING.md`

---

## Code Examples

### Example 1: Hello World Application

```python
# app.py
from adentic_boot import AgenticApplication, agenic_boot_app, rest_controller, get_mapping

@agenic_boot_app
class HelloWorldApp:
    pass

@rest_controller
class HelloController:
    @get_mapping("/hello")
    def hello(self):
        return {"message": "Hello, AgenticBoot!"}

if __name__ == "__main__":
    AgenticApplication.run(HelloWorldApp)
    # Server starts on http://0.0.0.0:8080
    # GET http://0.0.0.0:8080/hello -> {"message": "Hello, AgenticBoot!"}
```

---

### Example 2: REST API with Dependency Injection

```python
# models.py
from pydantic import BaseModel

class User(BaseModel):
    id: str
    name: str
    email: str

# services.py
from adentic_boot import service

@service
class UserService:
    def __init__(self):
        self.users = {}

    def create_user(self, user: User) -> User:
        self.users[user.id] = user
        return user

    def get_user(self, user_id: str) -> User | None:
        return self.users.get(user_id)

# controllers.py
from adentic_boot import rest_controller, request_mapping, get_mapping, post_mapping
from adentic_boot import path_variable, request_body

@rest_controller
@request_mapping("/api/users")
class UserController:
    def __init__(self, user_service: UserService):
        self.user_service = user_service  # Auto-injected

    @get_mapping("/{id}")
    def get_user(self, id: str):
        user = self.user_service.get_user(id)
        if not user:
            return {"error": "User not found"}, 404
        return user.model_dump()

    @post_mapping("/")
    def create_user(self, user: User):
        created = self.user_service.create_user(user)
        return created.model_dump(), 201

# app.py
from adentic_boot import AgenticApplication, agenic_boot_app

@agenic_boot_app(scan_base_packages="my_app")
class MyApp:
    pass

if __name__ == "__main__":
    AgenticApplication.run(MyApp)
```

---

### Example 3: Provider System

```python
# providers.py
from adentic_boot import llm_provider, storage_provider

@llm_provider(name="openai")
class OpenAIProvider:
    def generate(self, prompt: str) -> str:
        # Call OpenAI API
        return "Generated text..."

@storage_provider(name="s3")
class S3StorageProvider:
    def upload(self, key: str, data: bytes) -> str:
        # Upload to S3
        return f"s3://bucket/{key}"

# services.py
from adentic_boot import service, ProviderRegistry

@service
class DocumentService:
    def __init__(self, registry: ProviderRegistry):
        self.registry = registry

    def process_document(self, text: str):
        # Use LLM provider
        llm = self.registry.get_provider("llm", "openai")
        summary = llm.generate(f"Summarize: {text}")

        # Use storage provider
        storage = self.registry.get_provider("storage", "s3")
        url = storage.upload("summary.txt", summary.encode())

        return {"summary": summary, "url": url}
```

---

### Example 4: Event Bus

```python
# events.py
from dataclasses import dataclass

@dataclass
class UserCreatedEvent:
    user_id: str
    email: str

# listeners.py
from adentic_boot import component, EventBus

@component
class EmailListener:
    def __init__(self, event_bus: EventBus):
        event_bus.subscribe(UserCreatedEvent, self.on_user_created)

    def on_user_created(self, event: UserCreatedEvent):
        print(f"Sending welcome email to {event.email}")

# services.py
from adentic_boot import service, EventBus

@service
class UserService:
    def __init__(self, event_bus: EventBus):
        self.event_bus = event_bus

    def create_user(self, user: User) -> User:
        # Save user...

        # Publish event
        self.event_bus.publish(UserCreatedEvent(user.id, user.email))

        return user
```

---

## Performance Targets

### Startup Time

| Metric | Target | Measured |
|--------|--------|----------|
| **Cold Start** | <1 second | TBD |
| **Import Time** | <100ms | TBD |
| **Component Scan** | <200ms | TBD |
| **HTTP Server Start** | <100ms | TBD |

### Memory Usage

| Metric | Target | Measured |
|--------|--------|----------|
| **Baseline** | <30MB | TBD |
| **With 100 components** | <50MB | TBD |
| **Under Load** | <100MB | TBD |

### Throughput

| Metric | Target | Measured |
|--------|--------|----------|
| **Requests/sec** | 1000+ | TBD |
| **Latency (p50)** | <10ms | TBD |
| **Latency (p99)** | <50ms | TBD |

---

## Comparison: Java vs Python

### Feature Parity

| Feature | Java AgenticBoot | Python AgenticBoot | Status |
|---------|------------------|-------------------|--------|
| **DI Container** | ✅ AgenticContext | ✅ AgenticContext | Planned |
| **Component Scanning** | ✅ ComponentScanner | ✅ ComponentScanner | Planned |
| **Provider Registry** | ✅ ProviderRegistry | ✅ ProviderRegistry | Planned |
| **Event Bus** | ✅ EventBus | ✅ EventBus | Planned |
| **HTTP Server** | ✅ Javalin | ✅ Starlette | Planned |
| **Auto-Routing** | ✅ Annotations | ✅ Decorators | Planned |
| **Circular Dep Detection** | ✅ Stack tracking | ✅ Set tracking | Planned |
| **Type Safety** | ✅ Generics | ✅ Type hints | Planned |
| **Thread Safety** | ✅ ConcurrentHashMap | ✅ RLock + dict | Planned |

### Implementation Differences

| Aspect | Java | Python |
|--------|------|--------|
| **Annotations** | `@Component` | `@component` decorator |
| **Type System** | `Class<T>` | `Type[T]` |
| **Reflection** | Java Reflection API | `inspect` module |
| **Dependency Detection** | Constructor parameters | `__init__` type hints |
| **Concurrency** | `ExecutorService` | `ThreadPoolExecutor` |
| **HTTP Framework** | Javalin (Jetty) | Starlette (ASGI) |
| **JSON** | Jackson | Pydantic |
| **Testing** | JUnit 5 | pytest |
| **Build** | Maven | uv + Makefile |
| **Formatting** | Spotless (Google Java) | black |
| **Linting** | Checkstyle | ruff |
| **Type Checking** | javac | mypy |

---

## Next Steps

### Immediate Actions

1. **Create project structure**
   ```bash
   mkdir -p adentic-boot-python/{src/{main,test}/python/adentic_boot,doc}
   cd adentic-boot-python
   ```

2. **Initialize uv project**
   ```bash
   uv init
   # Edit pyproject.toml (use template above)
   ```

3. **Create Makefile**
   ```bash
   # Copy Makefile template above
   ```

4. **Start Phase 1 implementation**
   - Implement `context.py`
   - Implement core decorators in `annotations.py`
   - Implement `exceptions.py`
   - Write tests in `test_context.py`

### Questions to Resolve

1. **Async support**: Should HTTP handlers support `async def`?
2. **Multi-tenancy**: Support for multiple contexts in same process?
3. **Configuration**: Should we support `.env` files or only decorators?
4. **Monitoring**: Built-in metrics/tracing or leave to external tools?
5. **Native compilation**: Should we target PyPy or CPython only?

---

## References

### Internal Documentation

- **Java AgenticBoot Analysis**: `/home/developer/adentic-boot/AGENTICBOOT_ARCHITECTURE_ANALYSIS.md`
- **Executive Summary**: `/home/developer/adentic-boot/EXECUTIVE_SUMMARY.md`
- **Python Design Reference**: `/home/developer/adentic-boot/PYTHON_DESIGN_REFERENCE.md`

### External References

- **PEP 484** - Type Hints: https://peps.python.org/pep-0484/
- **PEP 526** - Variable Annotations: https://peps.python.org/pep-0526/
- **Starlette Documentation**: https://www.starlette.io/
- **Pydantic Documentation**: https://docs.pydantic.dev/
- **uv Documentation**: https://github.com/astral-sh/uv

---

**Last Updated:** 2025-11-04
**Version:** 1.0.0
**Status:** Design Complete - Ready for Implementation
