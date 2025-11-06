# AgenticBoot + Adentic-EE Integration Design

**Date:** 2025-11-06
**Version:** 1.0
**Status:** Design Phase
**Target Release:** AgenticBoot v1.1.0

---

## TL;DR

**Integration goal**: Enable AgenticBoot to discover, manage, and orchestrate Adentic-EE agents and tools via dependency injection. **Key additions**: EE annotation support (@Agent, @Tool), agent/tool provider registry categories, automatic component scanning, REST API integration. **Benefits**: Developers can build intelligent agentic applications with DI, HTTP APIs, and event-driven architecture using ReActAgent, ChainOfThoughtAgent, and enterprise tools. **Pattern**: Framework-agnostic - works standalone or with Spring/Quarkus.

**Quick decision**: Need basic agents? → Use SimpleAgent + CalculatorTool. Need reasoning? → Use ReActAgent + ToolRegistry. Need orchestration? → Use AgentOrchestrator + EventBus.

---

## Table of Contents

1. [Executive Overview](#executive-overview)
2. [Architecture Design](#architecture-design)
3. [Component Changes](#component-changes)
4. [Annotation System](#annotation-system)
5. [Provider Registry Extensions](#provider-registry-extensions)
6. [Integration Patterns](#integration-patterns)
7. [Code Examples](#code-examples)
8. [Testing Strategy](#testing-strategy)
9. [Migration Guide](#migration-guide)
10. [Performance Considerations](#performance-considerations)
11. [Security Considerations](#security-considerations)
12. [References](#references)

---

## Executive Overview

### What This Integration Delivers

AgenticBoot currently provides:
- ✅ Dependency injection (constructor-based)
- ✅ HTTP server with REST controllers
- ✅ Event bus (sync/async)
- ✅ Component scanning
- ✅ Provider registry (9 categories)
- ✅ 9 built-in tools (Database, WebSearch, FileSystem, etc.)

**After Adentic-EE Integration, AgenticBoot adds:**
- ✅ **7 Enterprise LLM Agents** (SimpleAgent, ReActAgent, ChainOfThoughtAgent, TreeOfThoughtAgent, FunctionCallingAgent, AgentOrchestrator, LLMAgentRouter)
- ✅ **Enterprise Tools** (CalculatorTool, FileTool, ShellTool, ToolRegistry)
- ✅ **6 Database Providers** (H2, PostgreSQL, MySQL, SQLite, MongoDB, Base)
- ✅ **Web Search** (DuckDuckGoSearchProvider)
- ✅ **Memory Management** (Conversation memory, context)
- ✅ **Workflow Patterns** (via adentic-ee-core)

### Integration Goals

1. **Zero-Configuration Auto-Discovery**
   - Scan classpath for EE agents and tools
   - Auto-register in ProviderRegistry
   - No XML, no manual wiring

2. **Type-Safe DI Integration**
   - Inject agents/tools via @Inject
   - Type-safe retrieval from registry
   - Constructor injection support

3. **REST API Integration**
   - Expose agents via @RestController
   - JSON request/response handling
   - Streaming support (future)

4. **Event-Driven Architecture**
   - Publish agent events to EventBus
   - Subscribe to agent lifecycle events
   - Async agent orchestration

5. **Framework-Agnostic Design**
   - Works standalone (just AgenticBoot)
   - Works with Spring Boot (optional)
   - Works with Quarkus/Micronaut (optional)

### Success Criteria

- ✅ All EE agents discoverable via component scanning
- ✅ All EE tools registered in ToolRegistry
- ✅ Agents injectable via @Inject or ProviderRegistry
- ✅ REST API for agent execution
- ✅ Event bus integration for agent lifecycle
- ✅ 100% backward compatibility (existing code unaffected)
- ✅ Zero configuration required (convention over configuration)
- ✅ Comprehensive tests (unit, integration, E2E)

---

## Architecture Design

### Current AgenticBoot Architecture (Before Integration)

```
┌─────────────────────────────────────────────────────────┐
│ AgenticBoot Application Layer                          │
│ - @AgenticBootApplication                              │
│ - AgenticContext (DI Container)                        │
│ - AgenticServer (HTTP)                                 │
│ - EventBus (Pub/Sub)                                   │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┴────────────┐
        │                         │
┌───────▼──────────┐  ┌──────────▼────────┐
│ ComponentScanner │  │ ProviderRegistry  │
│ - @Component     │  │ - llm             │
│ - @Service       │  │ - infrastructure  │
│ - @RestController│  │ - storage         │
│ - @LLM           │  │ - messaging       │
│ - @Storage       │  │ - orchestration   │
│ - @Messaging     │  │ - memory          │
│ - @Tool          │  │ - queue           │
└──────────────────┘  │ - tool            │
                      │ - evaluation      │
                      └───────────────────┘
```

### Target Architecture (After Adentic-EE Integration)

```
┌─────────────────────────────────────────────────────────────────┐
│ AgenticBoot Application Layer                                  │
│ - @AgenticBootApplication (enhanced with EE support)           │
│ - AgenticContext (DI Container with agent/tool injection)      │
│ - AgenticServer (HTTP with agent endpoints)                    │
│ - EventBus (Agent lifecycle events)                            │
└──────────────────────────┬──────────────────────────────────────┘
                           │
             ┌─────────────┴─────────────┐
             │                           │
┌────────────▼─────────────┐  ┌─────────▼────────────────────────┐
│ ComponentScanner         │  │ ProviderRegistry                 │
│ (Enhanced)               │  │ (Extended)                       │
│                          │  │                                  │
│ Core Annotations:        │  │ Existing Categories:             │
│ - @Component             │  │ - llm, infrastructure, storage   │
│ - @Service               │  │ - messaging, orchestration       │
│ - @RestController        │  │ - memory, queue, evaluation      │
│                          │  │                                  │
│ NEW EE Annotations:      │  │ NEW EE Categories:               │
│ - @Agent                 │  │ - agent (NEW)                    │
│ - @Tool                  │  │   - simple, react, cot, tot      │
│ - @Memory                │  │   - function-calling, orchestrator│
│ - @Workflow              │  │ - tool (ENHANCED)                │
│                          │  │   - calculator, file, shell      │
└──────────────────────────┘  │ - database (ENHANCED)            │
                              │   - h2, postgres, mysql, sqlite  │
                              └──────────────────────────────────┘
                                          │
                     ┌────────────────────┴────────────────────┐
                     │                                         │
        ┌────────────▼─────────────┐          ┌───────────────▼─────────┐
        │ Adentic-EE-Core          │          │ Adentic-AI-Client       │
        │                          │          │                         │
        │ - 7 LLM Agents           │          │ - LLMClient (abstract)  │
        │ - ToolRegistry           │          │ - ChatMessage           │
        │ - AgenticTool            │          │ - ToolCall              │
        │ - DatabaseProviders (6)  │          │ - CompletionRequest     │
        │ - WebSearchProvider      │          │ - CompletionResult      │
        │ - Memory                 │          │ - Model abstractions    │
        └──────────────────────────┘          └─────────────────────────┘
```

### Dependency Flow

```
AgenticBoot (v1.1.0)
    ↓ imports via BOM
adentic-ee-bom (v1.0.0-SNAPSHOT)
    ↓ manages
adentic-ee-core (v1.0.0-SNAPSHOT)
    ↓ depends on
adentic-ee-api (v1.0.0-SNAPSHOT)
    ↓ depends on
adentic-ai-client (v1.0.0-SNAPSHOT)
    ↓ depends on
adentic-se (Standard Edition)
    ↓ provides
Pure interfaces (TextGenerationProvider, MessageBrokerProvider, etc.)
```

---

## Component Changes

### 1. ComponentScanner Enhancement

**File:** `src/main/java/dev/adeengineer/adentic/boot/scanner/ComponentScanner.java`

**Current Supported Annotations:**
```java
// Core
@Component, @Service, @RestController

// Providers (SE)
@LLM, @Infrastructure, @Storage, @Messaging
@Orchestration, @Memory, @Queue, @Tool, @Evaluation
@WebSearch, @WebTest, @DatabaseProvider
```

**NEW Annotations to Support (EE):**
```java
// Agent annotations (from adentic-ee-api)
@Agent                    // Generic agent marker
@ReActAgent              // Reasoning + Action agent
@ChainOfThoughtAgent     // Chain of thought reasoning
@TreeOfThoughtAgent      // Tree search reasoning
@FunctionCallingAgent    // Tool-calling agent
@AgentOrchestrator       // Multi-agent orchestrator

// Tool annotations (from adentic-ee-api)
@AgenticTool             // Generic tool marker
@CalculatorTool          // Math operations
@FileTool                // File operations
@ShellTool               // Shell execution

// Memory annotations
@MemoryProvider          // Conversation memory

// Workflow annotations
@WorkflowProvider        // Workflow patterns
```

**Implementation Changes:**

```java
public class ComponentScanner {
    // Existing code...

    // NEW: EE annotation support
    private static final List<Class<? extends Annotation>> EE_AGENT_ANNOTATIONS = List.of(
        Agent.class,
        ReActAgent.class,
        ChainOfThoughtAgent.class,
        TreeOfThoughtAgent.class,
        FunctionCallingAgent.class,
        AgentOrchestrator.class
    );

    private static final List<Class<? extends Annotation>> EE_TOOL_ANNOTATIONS = List.of(
        AgenticTool.class,
        CalculatorTool.class,
        FileTool.class,
        ShellTool.class
    );

    /**
     * Scan for EE agents and register in "agent" category
     */
    public Map<String, List<Class<?>>> scanEEAgents() {
        Map<String, List<Class<?>>> agents = new HashMap<>();

        for (Class<? extends Annotation> annotation : EE_AGENT_ANNOTATIONS) {
            List<Class<?>> found = scanForAnnotation(annotation);
            String category = extractCategoryName(annotation);
            agents.put(category, found);
        }

        return agents;
    }

    /**
     * Scan for EE tools and register in "tool" category
     */
    public Map<String, List<Class<?>>> scanEETools() {
        Map<String, List<Class<?>>> tools = new HashMap<>();

        for (Class<? extends Annotation> annotation : EE_TOOL_ANNOTATIONS) {
            List<Class<?>> found = scanForAnnotation(annotation);
            String category = extractCategoryName(annotation);
            tools.put(category, found);
        }

        return tools;
    }

    // Existing scan() method enhanced
    public ScanResult scan() {
        ScanResult result = new ScanResult();

        // Existing SE scanning
        result.setComponents(scanComponents());
        result.setProviders(scanProviders());

        // NEW: EE scanning
        result.setAgents(scanEEAgents());
        result.setTools(scanEETools());

        return result;
    }
}
```

### 2. ProviderRegistry Extension

**File:** `src/main/java/dev/adeengineer/adentic/boot/registry/ProviderRegistry.java`

**Current Categories:**
```java
llm, infrastructure, storage, messaging,
orchestration, memory, queue, tool, evaluation
```

**NEW Categories:**
```java
agent     // EE agents (SimpleAgent, ReActAgent, etc.)
tool      // ENHANCED with EE tools (CalculatorTool, FileTool, ShellTool)
database  // ENHANCED with EE database providers
```

**Implementation Changes:**

```java
public class ProviderRegistry {
    // Existing categories
    public static final String CATEGORY_LLM = "llm";
    public static final String CATEGORY_STORAGE = "storage";
    // ... existing categories

    // NEW: EE categories
    public static final String CATEGORY_AGENT = "agent";
    public static final String CATEGORY_TOOL = "tool";  // Already exists, enhanced
    public static final String CATEGORY_DATABASE = "database";

    /**
     * Register an EE agent in the "agent" category
     */
    public void registerAgent(String name, Object agentInstance) {
        registerProvider(CATEGORY_AGENT, name, agentInstance);
        log.info("Registered EE agent: {} in category: {}", name, CATEGORY_AGENT);
    }

    /**
     * Get an agent by name from "agent" category
     */
    public <T> Optional<T> getAgent(String name, Class<T> type) {
        return getProvider(CATEGORY_AGENT, name, type);
    }

    /**
     * Get all registered agents
     */
    public Map<String, Object> getAllAgents() {
        return getProvidersByCategory(CATEGORY_AGENT);
    }

    /**
     * Register an EE tool in "tool" category
     */
    public void registerTool(String name, Object toolInstance) {
        registerProvider(CATEGORY_TOOL, name, toolInstance);
        log.info("Registered EE tool: {} in category: {}", name, CATEGORY_TOOL);
    }

    /**
     * Get a tool by name
     */
    public <T> Optional<T> getTool(String name, Class<T> type) {
        return getProvider(CATEGORY_TOOL, name, type);
    }

    /**
     * Get all registered tools
     */
    public Map<String, Object> getAllTools() {
        return getProvidersByCategory(CATEGORY_TOOL);
    }
}
```

### 3. AgenticApplication Enhancement

**File:** `src/main/java/dev/adeengineer/adentic/boot/AgenticApplication.java`

**Current Startup Flow:**
```
1. Print banner
2. Create AgenticContext
3. Register core beans (EventBus, ProviderRegistry, AgenticServer)
4. Scan components
5. Register providers
6. Start HTTP server
7. Add shutdown hook
```

**Enhanced Startup Flow (with EE):**
```
1. Print banner
2. Create AgenticContext
3. Register core beans (EventBus, ProviderRegistry, AgenticServer)
4. Scan components (SE + EE)
5. Register SE providers
6. NEW: Register EE agents
7. NEW: Register EE tools
8. NEW: Initialize ToolRegistry with discovered tools
9. NEW: Inject dependencies into agents
10. Register REST controllers
11. Start HTTP server
12. NEW: Publish ApplicationStartedEvent
13. Add shutdown hook
```

**Implementation:**

```java
public class AgenticApplication {
    // Existing code...

    public void run(String[] args) {
        printBanner();

        AgenticContext context = new AgenticContext();
        registerCoreBeans(context);

        // Scan for all components (SE + EE)
        ComponentScanner scanner = new ComponentScanner(basePackage);
        ScanResult scanResult = scanner.scan();

        // Register SE components
        registerComponents(scanResult.getComponents(), context);
        registerProviders(scanResult.getProviders(), context);

        // NEW: Register EE agents
        registerEEAgents(scanResult.getAgents(), context);

        // NEW: Register EE tools
        registerEETools(scanResult.getTools(), context);

        // NEW: Initialize ToolRegistry
        initializeToolRegistry(context);

        // Register REST controllers
        registerControllers(scanResult.getControllers(), context);

        // Start HTTP server
        startServer(context);

        // NEW: Publish startup event
        publishStartupEvent(context);

        addShutdownHook(context);
    }

    private void registerEEAgents(Map<String, List<Class<?>>> agents, AgenticContext context) {
        ProviderRegistry registry = context.getBean(ProviderRegistry.class);

        for (Map.Entry<String, List<Class<?>>> entry : agents.entrySet()) {
            String category = entry.getKey();

            for (Class<?> agentClass : entry.getValue()) {
                try {
                    // Instantiate with DI
                    Object agentInstance = context.getBean(agentClass);

                    // Extract name from annotation or class name
                    String name = extractProviderName(agentClass);

                    // Register in "agent" category
                    registry.registerAgent(name, agentInstance);

                    log.info("Registered EE agent: {} ({})", name, agentClass.getSimpleName());
                } catch (Exception e) {
                    log.error("Failed to register agent: {}", agentClass, e);
                }
            }
        }
    }

    private void registerEETools(Map<String, List<Class<?>>> tools, AgenticContext context) {
        ProviderRegistry registry = context.getBean(ProviderRegistry.class);

        for (Map.Entry<String, List<Class<?>>> entry : tools.entrySet()) {
            String category = entry.getKey();

            for (Class<?> toolClass : entry.getValue()) {
                try {
                    Object toolInstance = context.getBean(toolClass);
                    String name = extractProviderName(toolClass);
                    registry.registerTool(name, toolInstance);

                    log.info("Registered EE tool: {} ({})", name, toolClass.getSimpleName());
                } catch (Exception e) {
                    log.error("Failed to register tool: {}", toolClass, e);
                }
            }
        }
    }

    private void initializeToolRegistry(AgenticContext context) {
        try {
            // Get or create ToolRegistry
            ToolRegistry toolRegistry = context.containsBean(ToolRegistry.class)
                ? context.getBean(ToolRegistry.class)
                : new SimpleToolRegistry();

            // Get all tools from ProviderRegistry
            ProviderRegistry providerRegistry = context.getBean(ProviderRegistry.class);
            Map<String, Object> tools = providerRegistry.getAllTools();

            // Register each tool in ToolRegistry
            for (Map.Entry<String, Object> entry : tools.entrySet()) {
                if (entry.getValue() instanceof Tool) {
                    Tool tool = (Tool) entry.getValue();
                    toolRegistry.register(tool).block();
                    log.info("Added tool to ToolRegistry: {}", entry.getKey());
                }
            }

            // Register ToolRegistry as singleton
            context.registerSingleton(ToolRegistry.class, toolRegistry);

        } catch (Exception e) {
            log.error("Failed to initialize ToolRegistry", e);
        }
    }

    private void publishStartupEvent(AgenticContext context) {
        try {
            EventBus eventBus = context.getBean(EventBus.class);
            eventBus.publish(new ApplicationStartedEvent(this));
        } catch (Exception e) {
            log.warn("Failed to publish startup event", e);
        }
    }
}
```

### 4. EventBus Integration

**New Event Types:**

```java
// Agent lifecycle events
public class AgentExecutionStartedEvent {
    private final String agentName;
    private final AgentRequest request;
    private final Instant timestamp;
}

public class AgentExecutionCompletedEvent {
    private final String agentName;
    private final AgentRequest request;
    private final AgentResult result;
    private final Duration duration;
    private final Instant timestamp;
}

public class AgentExecutionFailedEvent {
    private final String agentName;
    private final AgentRequest request;
    private final Throwable error;
    private final Instant timestamp;
}

// Tool execution events
public class ToolExecutedEvent {
    private final String toolName;
    private final Map<String, Object> parameters;
    private final Object result;
    private final Duration duration;
}

// Application lifecycle events
public class ApplicationStartedEvent {
    private final AgenticApplication application;
    private final Instant timestamp;
}
```

---

## Annotation System

### EE Annotations (from adentic-ee-api)

**Agent Annotations:**

```java
package dev.adeengineer.ee.llm.agent.annotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component  // Meta-annotation for component scanning
public @interface Agent {
    /**
     * Agent name (default: class name in kebab-case)
     */
    String value() default "";

    /**
     * Agent description
     */
    String description() default "";

    /**
     * Model to use (e.g., "gpt-4", "claude-3-5-sonnet")
     */
    String model() default "";

    /**
     * Enable tool calling
     */
    boolean enableTools() default false;

    /**
     * Max tool iterations
     */
    int maxToolIterations() default 5;
}
```

**Tool Annotations:**

```java
package dev.adeengineer.ee.llm.tools.annotation;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AgenticTool {
    /**
     * Tool name (default: class name in kebab-case)
     */
    String value() default "";

    /**
     * Tool description for LLM
     */
    String description();

    /**
     * Tool parameters (JSON schema)
     */
    String parameters() default "{}";
}
```

### Usage Examples

**Creating a Custom Agent:**

```java
package com.example.agents;

import dev.adeengineer.ee.llm.agent.annotation.Agent;
import dev.adeengineer.ee.llm.agent.BaseAgent;

@Agent(
    value = "customer-support",
    description = "Handles customer support inquiries",
    model = "gpt-4",
    enableTools = true,
    maxToolIterations = 10
)
public class CustomerSupportAgent extends BaseAgent {
    @Inject
    public CustomerSupportAgent(LLMClient client, Memory memory, ToolRegistry toolRegistry) {
        super(client, memory, toolRegistry, buildConfig());
    }

    private static AgentConfig buildConfig() {
        return AgentConfig.builder()
            .systemPrompt("You are a helpful customer support agent. Be empathetic and solution-oriented.")
            .temperature(0.7)
            .build();
    }
}
```

**Creating a Custom Tool:**

```java
package com.example.tools;

import dev.adeengineer.ee.llm.tools.annotation.AgenticTool;
import dev.adeengineer.ee.llm.tools.Tool;

@AgenticTool(
    value = "weather-lookup",
    description = "Get current weather for a location",
    parameters = """
    {
        "type": "object",
        "properties": {
            "location": {"type": "string", "description": "City name"},
            "units": {"type": "string", "enum": ["celsius", "fahrenheit"]}
        },
        "required": ["location"]
    }
    """
)
public class WeatherTool implements Tool {
    private final WeatherService weatherService;

    @Inject
    public WeatherTool(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public Mono<ToolResult> execute(Map<String, Object> parameters) {
        String location = (String) parameters.get("location");
        String units = (String) parameters.getOrDefault("units", "celsius");

        return weatherService.getCurrentWeather(location, units)
            .map(weather -> ToolResult.success(weather))
            .onErrorResume(e -> Mono.just(ToolResult.error(e.getMessage())));
    }
}
```

---

## Provider Registry Extensions

### Category Mapping

| Category | Provider Type | Examples |
|----------|--------------|----------|
| **agent** | LLM Agents | simple, react, cot, tot, function-calling, orchestrator |
| **tool** | Tools & Utilities | calculator, file, shell, weather, database |
| **database** | Database Providers | h2, postgres, mysql, sqlite, mongodb |
| **llm** | LLM Clients | openai, anthropic, groq, ollama |
| **memory** | Memory Providers | in-memory, redis, persistent |
| **workflow** | Workflow Patterns | saga, event-sourcing, cqrs |

### Registry API

```java
// Agent operations
registry.registerAgent("react", reactAgentInstance);
Optional<ReActAgent> agent = registry.getAgent("react", ReActAgent.class);
Map<String, Object> allAgents = registry.getAllAgents();

// Tool operations
registry.registerTool("calculator", calculatorToolInstance);
Optional<CalculatorTool> tool = registry.getTool("calculator", CalculatorTool.class);
Map<String, Object> allTools = registry.getAllTools();

// Generic provider operations (existing)
registry.registerProvider("llm", "openai", openAIClient);
Optional<LLMClient> llm = registry.getProvider("llm", "openai", LLMClient.class);
```

---

## Integration Patterns

### Pattern 1: Direct Injection

**Use Case:** Simple applications with known agents

```java
@RestController
@RequestMapping("/api/support")
public class SupportController {
    private final ReActAgent agent;

    @Inject
    public SupportController(ReActAgent agent) {
        this.agent = agent;
    }

    @PostMapping("/query")
    public Mono<AgentResult> handleQuery(@RequestBody String question) {
        return agent.execute(AgentRequest.of(question));
    }
}
```

### Pattern 2: Registry Lookup

**Use Case:** Dynamic agent selection at runtime

```java
@Service
public class AgentRouter {
    private final ProviderRegistry registry;

    @Inject
    public AgentRouter(ProviderRegistry registry) {
        this.registry = registry;
    }

    public Mono<AgentResult> routeRequest(String agentType, String question) {
        return registry.getAgent(agentType, Agent.class)
            .map(agent -> agent.execute(AgentRequest.of(question)))
            .orElse(Mono.error(new AgentNotFoundException(agentType)));
    }
}
```

### Pattern 3: AgentOrchestrator

**Use Case:** Multi-agent workflows

```java
@Service
public class MultiAgentWorkflow {
    private final AgentOrchestrator orchestrator;
    private final EventBus eventBus;

    @Inject
    public MultiAgentWorkflow(AgentOrchestrator orchestrator, EventBus eventBus) {
        this.orchestrator = orchestrator;
        this.eventBus = eventBus;

        // Subscribe to agent events
        eventBus.subscribe(AgentExecutionCompletedEvent.class, this::onAgentComplete);
    }

    public Mono<String> processComplexQuery(String query) {
        // Step 1: ReAct agent analyzes query
        return orchestrator.execute("react", AgentRequest.of(query))
            // Step 2: ChainOfThought agent refines answer
            .flatMap(result -> orchestrator.execute("cot", AgentRequest.of(result.getAnswer())))
            // Step 3: Extract final answer
            .map(AgentResult::getAnswer);
    }

    private void onAgentComplete(AgentExecutionCompletedEvent event) {
        log.info("Agent {} completed in {}ms",
            event.getAgentName(),
            event.getDuration().toMillis());
    }
}
```

### Pattern 4: Event-Driven Agent Chain

**Use Case:** Async agent pipelines

```java
@Service
public class EventDrivenPipeline {
    private final EventBus eventBus;
    private final ProviderRegistry registry;

    @Inject
    public EventDrivenPipeline(EventBus eventBus, ProviderRegistry registry) {
        this.eventBus = eventBus;
        this.registry = registry;

        // Chain: UserQuery → ReAct → ChainOfThought → Final Result
        eventBus.subscribe(UserQueryEvent.class, this::handleQuery);
        eventBus.subscribe(ReActCompleteEvent.class, this::runChainOfThought);
        eventBus.subscribe(ChainOfThoughtCompleteEvent.class, this::sendFinalResult);
    }

    private void handleQuery(UserQueryEvent event) {
        registry.getAgent("react", ReActAgent.class)
            .ifPresent(agent -> {
                agent.execute(AgentRequest.of(event.getQuery()))
                    .subscribe(result ->
                        eventBus.publish(new ReActCompleteEvent(result)));
            });
    }

    private void runChainOfThought(ReActCompleteEvent event) {
        registry.getAgent("cot", ChainOfThoughtAgent.class)
            .ifPresent(agent -> {
                agent.execute(AgentRequest.of(event.getResult().getAnswer()))
                    .subscribe(result ->
                        eventBus.publish(new ChainOfThoughtCompleteEvent(result)));
            });
    }

    private void sendFinalResult(ChainOfThoughtCompleteEvent event) {
        // Send to user via WebSocket, HTTP response, etc.
        log.info("Final answer: {}", event.getResult().getAnswer());
    }
}
```

---

## Code Examples

### Example 1: Simple Agent REST API

**Application:**

```java
@AgenticBootApplication
public class SimpleAgentApp {
    public static void main(String[] args) {
        AgenticApplication.run(SimpleAgentApp.class, args);
    }
}
```

**Controller:**

```java
@RestController
@RequestMapping("/api/agent")
public class AgentController {
    private final SimpleAgent agent;

    @Inject
    public AgentController(ProviderRegistry registry) {
        // Get SimpleAgent from registry
        this.agent = registry.getAgent("simple", SimpleAgent.class)
            .orElseThrow(() -> new IllegalStateException("SimpleAgent not found"));
    }

    @PostMapping("/ask")
    public Mono<ResponseEntity<String>> ask(@RequestBody QuestionRequest request) {
        return agent.execute(AgentRequest.of(request.getQuestion()))
            .map(result -> ResponseEntity.ok(result.getAnswer()))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(500).body("Error: " + e.getMessage())
            ));
    }
}

@Data
class QuestionRequest {
    private String question;
}
```

### Example 2: ReAct Agent with Tools

**Application:**

```java
@AgenticBootApplication
public class ReActAgentApp {
    public static void main(String[] args) {
        AgenticApplication.run(ReActAgentApp.class, args);
    }
}
```

**Agent Configuration:**

```java
@Component
public class ReActAgentConfig {

    @Bean
    public ReActAgent reactAgent(LLMClient llmClient, ToolRegistry toolRegistry) {
        AgentConfig config = AgentConfig.builder()
            .model("gpt-4")
            .systemPrompt("You are a helpful assistant. Use tools when needed.")
            .enableTools(true)
            .maxToolIterations(5)
            .build();

        return new ReActAgent(llmClient, null, toolRegistry, config);
    }

    @Bean
    public LLMClient llmClient() {
        return new OpenAIClient(
            LLMClientConfig.openAI(System.getenv("OPENAI_API_KEY"))
        );
    }
}
```

**Controller:**

```java
@RestController
@RequestMapping("/api/react")
public class ReActController {
    private final ReActAgent agent;

    @Inject
    public ReActController(ReActAgent agent) {
        this.agent = agent;
    }

    @PostMapping("/execute")
    public Mono<ResponseEntity<AgentResponse>> execute(@RequestBody AgentRequestDto request) {
        return agent.execute(AgentRequest.of(request.getQuestion()))
            .map(result -> ResponseEntity.ok(AgentResponse.from(result)))
            .onErrorResume(e -> Mono.just(
                ResponseEntity.status(500).body(AgentResponse.error(e.getMessage()))
            ));
    }
}

@Data
class AgentRequestDto {
    private String question;
    private Map<String, Object> context;
}

@Data
@Builder
class AgentResponse {
    private String answer;
    private List<String> toolsUsed;
    private int iterations;
    private String error;

    static AgentResponse from(AgentResult result) {
        return AgentResponse.builder()
            .answer(result.getAnswer())
            .toolsUsed(result.getToolCalls().stream()
                .map(ToolCall::getName)
                .toList())
            .iterations(result.getIterations())
            .build();
    }

    static AgentResponse error(String message) {
        return AgentResponse.builder()
            .error(message)
            .build();
    }
}
```

### Example 3: Multi-Agent Orchestration

```java
@RestController
@RequestMapping("/api/orchestrator")
public class OrchestratorController {
    private final AgentOrchestrator orchestrator;
    private final EventBus eventBus;

    @Inject
    public OrchestratorController(AgentOrchestrator orchestrator, EventBus eventBus) {
        this.orchestrator = orchestrator;
        this.eventBus = eventBus;
    }

    @PostMapping("/analyze")
    public Mono<ResponseEntity<AnalysisResult>> analyze(@RequestBody AnalysisRequest request) {
        // Orchestrate multiple agents
        return Flux.concat(
            // Step 1: ReAct agent gathers facts
            orchestrator.execute("react", AgentRequest.of(request.getQuestion())),

            // Step 2: ChainOfThought agent reasons
            orchestrator.execute("cot", AgentRequest.of(request.getQuestion())),

            // Step 3: TreeOfThought explores alternatives
            orchestrator.execute("tot", AgentRequest.of(request.getQuestion()))
        )
        .collectList()
        .map(results -> {
            AnalysisResult analysis = AnalysisResult.builder()
                .facts(results.get(0).getAnswer())
                .reasoning(results.get(1).getAnswer())
                .alternatives(results.get(2).getAnswer())
                .build();

            return ResponseEntity.ok(analysis);
        })
        .onErrorResume(e -> Mono.just(
            ResponseEntity.status(500).body(AnalysisResult.error(e.getMessage()))
        ));
    }
}

@Data
class AnalysisRequest {
    private String question;
}

@Data
@Builder
class AnalysisResult {
    private String facts;
    private String reasoning;
    private String alternatives;
    private String error;

    static AnalysisResult error(String message) {
        return AnalysisResult.builder().error(message).build();
    }
}
```

---

## Testing Strategy

### Unit Tests

**Test Agent Registration:**

```java
@Test
void shouldRegisterEEAgentInRegistry() {
    // Given
    ProviderRegistry registry = new ProviderRegistry();
    ReActAgent agent = mock(ReActAgent.class);

    // When
    registry.registerAgent("react", agent);

    // Then
    Optional<ReActAgent> retrieved = registry.getAgent("react", ReActAgent.class);
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isEqualTo(agent);
}
```

**Test Component Scanning:**

```java
@Test
void shouldScanAndDiscoverEEAgents() {
    // Given
    ComponentScanner scanner = new ComponentScanner("com.example.agents");

    // When
    Map<String, List<Class<?>>> agents = scanner.scanEEAgents();

    // Then
    assertThat(agents).containsKey("react");
    assertThat(agents.get("react")).contains(ReActAgent.class);
}
```

### Integration Tests

**Test End-to-End Agent Execution:**

```java
@AdenticBootTest
class AgentIntegrationTest {

    @Inject
    private ProviderRegistry registry;

    @Inject
    private AgenticContext context;

    @Test
    void shouldExecuteReActAgentViaRegistry() {
        // Given
        ReActAgent agent = registry.getAgent("react", ReActAgent.class)
            .orElseThrow();

        AgentRequest request = AgentRequest.of("What is 2 + 2?");

        // When
        AgentResult result = agent.execute(request).block();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAnswer()).contains("4");
    }
}
```

**Test REST API:**

```java
@AdenticBootTest
@WebTest
class AgentControllerTest {

    @Inject
    private AgenticServer server;

    @Test
    void shouldHandleAgentRequestViaHTTP() {
        // Given
        String question = "{\"question\": \"What is the capital of France?\"}";

        // When
        HttpResponse<String> response = HttpClient.newHttpClient()
            .send(
                HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/agent/ask"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(question))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            );

        // Then
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).contains("Paris");
    }
}
```

### E2E Tests

**Test Multi-Agent Workflow:**

```java
@AdenticBootTest
class MultiAgentWorkflowTest {

    @Inject
    private AgentOrchestrator orchestrator;

    @Inject
    private EventBus eventBus;

    @Test
    void shouldExecuteMultiAgentPipeline() {
        // Given
        String complexQuery = "Analyze the pros and cons of renewable energy";
        AtomicInteger eventsReceived = new AtomicInteger(0);

        // Subscribe to events
        eventBus.subscribe(AgentExecutionCompletedEvent.class,
            event -> eventsReceived.incrementAndGet());

        // When
        List<AgentResult> results = Flux.concat(
            orchestrator.execute("react", AgentRequest.of(complexQuery)),
            orchestrator.execute("cot", AgentRequest.of(complexQuery)),
            orchestrator.execute("tot", AgentRequest.of(complexQuery))
        ).collectList().block();

        // Then
        assertThat(results).hasSize(3);
        assertThat(eventsReceived.get()).isEqualTo(3);

        // Verify each agent contributed
        assertThat(results.get(0).getAnswer()).isNotEmpty();  // ReAct facts
        assertThat(results.get(1).getAnswer()).isNotEmpty();  // CoT reasoning
        assertThat(results.get(2).getAnswer()).isNotEmpty();  // ToT alternatives
    }
}
```

### Test Coverage Goals

| Component | Target Coverage | Priority |
|-----------|----------------|----------|
| ComponentScanner (EE scanning) | 80%+ | HIGH |
| ProviderRegistry (agent/tool methods) | 90%+ | HIGH |
| AgenticApplication (EE registration) | 70%+ | HIGH |
| Agent REST controllers | 85%+ | MEDIUM |
| Event bus integration | 75%+ | MEDIUM |
| Custom agents/tools | 60%+ | LOW |

---

## Migration Guide

### For Existing AgenticBoot Users

**Before (AgenticBoot v1.0):**

```java
@AgenticBootApplication
public class MyApp {
    public static void main(String[] args) {
        AgenticApplication.run(MyApp.class, args);
    }
}

@RestController
public class MyController {
    @Inject
    private WebSearchTool webSearch;

    @GetMapping("/search")
    public String search(@RequestParam String query) {
        return webSearch.search(query);
    }
}
```

**After (AgenticBoot v1.1 with EE):**

```java
// NO CHANGES REQUIRED - 100% backward compatible!

// But you can NOW use EE features:
@RestController
public class IntelligentController {
    @Inject
    private ReActAgent agent;  // NEW: Inject EE agents

    @Inject
    private ToolRegistry toolRegistry;  // NEW: Access all tools

    @PostMapping("/ask")
    public Mono<String> ask(@RequestBody String question) {
        return agent.execute(AgentRequest.of(question))
            .map(AgentResult::getAnswer);
    }
}
```

### Migration Checklist

- [ ] Update `pom.xml` to use `adentic-ee-bom`
- [ ] Add `adentic-ee-core` dependency
- [ ] No code changes required (100% backward compatible)
- [ ] Optional: Start using EE agents and tools
- [ ] Optional: Add custom agents with `@Agent`
- [ ] Optional: Add custom tools with `@AgenticTool`
- [ ] Run tests to verify integration
- [ ] Update documentation for new features

---

## Performance Considerations

### Startup Time Impact

**Baseline (AgenticBoot v1.0):**
- Startup time: <1 second
- Memory: 50-100MB
- JAR size: 8.7KB (annotation module)

**With EE Integration (AgenticBoot v1.1):**
- Startup time: <2 seconds (+1s for EE scanning)
- Memory: 100-150MB (+50MB for EE classes)
- JAR size: 8.7KB (annotation module) + ~10MB (adentic-ee-core)

**Optimization Strategies:**

1. **Lazy Loading** - Only load agents when first accessed
2. **Conditional Scanning** - Skip EE scanning if no EE classes on classpath
3. **Parallel Scanning** - Scan SE and EE components in parallel threads
4. **Class Caching** - Cache reflection metadata to avoid repeated scanning

```java
// Conditional EE scanning (only if EE classes present)
if (isEEAvailable()) {
    registerEEAgents(scanResult.getAgents(), context);
    registerEETools(scanResult.getTools(), context);
} else {
    log.info("Adentic EE not found on classpath, skipping EE integration");
}

private boolean isEEAvailable() {
    try {
        Class.forName("dev.adeengineer.ee.llm.agent.Agent");
        return true;
    } catch (ClassNotFoundException e) {
        return false;
    }
}
```

### Runtime Performance

**Agent Execution:**
- ReActAgent: 1-5 seconds (depends on LLM latency)
- ChainOfThoughtAgent: 2-10 seconds (multiple LLM calls)
- TreeOfThoughtAgent: 5-30 seconds (tree search)

**Optimization:**
- Use streaming responses for real-time feedback
- Cache agent results for common queries
- Implement circuit breakers for LLM failures
- Use async execution with EventBus for non-blocking ops

---

## Security Considerations

### 1. Tool Execution Security

**Risk:** Tools can execute arbitrary code (ShellTool, FileTool)

**Mitigation:**
```java
@Component
public class SecureShellTool implements Tool {
    private static final List<String> ALLOWED_COMMANDS = List.of(
        "ls", "pwd", "date", "echo"
    );

    @Override
    public Mono<ToolResult> execute(Map<String, Object> parameters) {
        String command = (String) parameters.get("command");

        // Validate command is in allowlist
        if (!isAllowed(command)) {
            return Mono.just(ToolResult.error("Command not allowed: " + command));
        }

        // Execute safely
        return executeCommand(command);
    }

    private boolean isAllowed(String command) {
        return ALLOWED_COMMANDS.stream()
            .anyMatch(allowed -> command.startsWith(allowed));
    }
}
```

### 2. Agent Input Validation

**Risk:** Malicious prompts could exploit LLM vulnerabilities

**Mitigation:**
```java
@Service
public class PromptSanitizer {
    private static final Pattern INJECTION_PATTERN =
        Pattern.compile("(ignore|forget|disregard) (previous|above|all) (instructions|rules)");

    public String sanitize(String prompt) {
        // Remove potential injection attempts
        if (INJECTION_PATTERN.matcher(prompt.toLowerCase()).find()) {
            throw new SecurityException("Potential prompt injection detected");
        }

        // Limit length
        if (prompt.length() > 10000) {
            throw new IllegalArgumentException("Prompt too long");
        }

        return prompt;
    }
}
```

### 3. API Key Protection

**Best Practices:**
- Store API keys in environment variables
- Never log API keys
- Use secrets management (HashiCorp Vault, AWS Secrets Manager)
- Rotate keys regularly

```java
@Component
public class SecureLLMClientConfig {

    @Bean
    public LLMClient llmClient() {
        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY not set");
        }

        // Validate key format (basic check)
        if (!apiKey.startsWith("sk-")) {
            throw new IllegalArgumentException("Invalid API key format");
        }

        return new OpenAIClient(LLMClientConfig.openAI(apiKey));
    }
}
```

### 4. Rate Limiting

**Prevent abuse of agent endpoints:**

```java
@Service
public class RateLimiter {
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 60;

    public boolean allowRequest(String clientId) {
        AtomicInteger count = requestCounts.computeIfAbsent(
            clientId,
            k -> new AtomicInteger(0)
        );

        // Reset counter every minute
        scheduleReset(clientId);

        return count.incrementAndGet() <= MAX_REQUESTS_PER_MINUTE;
    }
}

@RestController
public class RateLimitedController {
    @Inject
    private RateLimiter rateLimiter;

    @PostMapping("/api/agent/ask")
    public Mono<ResponseEntity<String>> ask(@RequestBody String question,
                                             @RequestHeader("X-Client-ID") String clientId) {
        if (!rateLimiter.allowRequest(clientId)) {
            return Mono.just(ResponseEntity.status(429).body("Rate limit exceeded"));
        }

        // Process request...
    }
}
```

---

## References

### Documentation

- [AgenticBoot Architecture](AGENTICBOOT_ARCHITECTURE_ANALYSIS.md)
- [Adentic Framework README](../adentic-framework/README.md)
- [Adentic EE README](../adentic-framework/adentic-ee/README.md)
- [Adentic AI Client](../adentic-framework/adentic-ai-client/README.md)

### Code Examples

- [AgenticBoot Examples](examples/)
- [Adentic Framework Examples](../adentic-framework/example/)

### API References

- [ComponentScanner JavaDoc](src/main/java/dev/adeengineer/adentic/boot/scanner/ComponentScanner.java)
- [ProviderRegistry JavaDoc](src/main/java/dev/adeengineer/adentic/boot/registry/ProviderRegistry.java)
- [AgenticApplication JavaDoc](src/main/java/dev/adeengineer/adentic/boot/AgenticApplication.java)

### External Resources

- [Adentic Framework GitHub](https://github.com/phdsystems/adentic-framework)
- [ReAct Pattern Paper](https://arxiv.org/abs/2210.03629)
- [Chain-of-Thought Paper](https://arxiv.org/abs/2201.11903)
- [Tree-of-Thought Paper](https://arxiv.org/abs/2305.10601)

---

## Appendix A: Complete Integration Checklist

### Phase 1: Foundation (Week 1)

- [x] Add `adentic-ee-core` to BOM
- [ ] Update `pom.xml` with EE dependency
- [ ] Verify dependency resolution
- [ ] Build adentic-framework successfully
- [ ] Build AgenticBoot with EE dependency

### Phase 2: Core Integration (Week 2)

- [ ] Update `ComponentScanner` with EE annotations
- [ ] Update `ProviderRegistry` with agent/tool categories
- [ ] Update `AgenticApplication` startup flow
- [ ] Add agent registration logic
- [ ] Add tool registration logic
- [ ] Add ToolRegistry initialization

### Phase 3: REST API Integration (Week 3)

- [ ] Create example REST controllers
- [ ] Add SimpleAgent endpoint
- [ ] Add ReActAgent endpoint
- [ ] Add ChainOfThoughtAgent endpoint
- [ ] Add AgentOrchestrator endpoint
- [ ] Test HTTP endpoints

### Phase 4: Event Integration (Week 4)

- [ ] Define agent lifecycle events
- [ ] Define tool execution events
- [ ] Add event publishing in agents
- [ ] Add event subscribers
- [ ] Test event-driven workflows

### Phase 5: Testing (Week 5)

- [ ] Write unit tests for ComponentScanner
- [ ] Write unit tests for ProviderRegistry
- [ ] Write integration tests for agents
- [ ] Write integration tests for tools
- [ ] Write E2E tests for workflows
- [ ] Achieve 80%+ test coverage

### Phase 6: Documentation (Week 6)

- [ ] Update README.md
- [ ] Create integration guide
- [ ] Add code examples
- [ ] Add troubleshooting guide
- [ ] Create migration guide
- [ ] Update JavaDoc comments

### Phase 7: Release (Week 7)

- [ ] Final testing
- [ ] Performance benchmarks
- [ ] Security audit
- [ ] Release notes
- [ ] Tag release v1.1.0
- [ ] Deploy to Maven Central

---

## Appendix B: Troubleshooting Guide

### Issue: Agent Not Found in Registry

**Symptom:**
```
Optional<ReActAgent> agent = registry.getAgent("react", ReActAgent.class);
// agent.isEmpty() == true
```

**Diagnosis:**
1. Check if agent class is on classpath
2. Verify agent is annotated with `@Agent`
3. Check component scanning base package
4. Enable debug logging for ComponentScanner

**Solution:**
```java
// Enable debug logging
logging.level.dev.adeengineer.adentic.boot.scanner=DEBUG

// Verify agent is scanned
ComponentScanner scanner = new ComponentScanner("com.example");
Map<String, List<Class<?>>> agents = scanner.scanEEAgents();
System.out.println("Found agents: " + agents);
```

### Issue: Tool Not Registered in ToolRegistry

**Symptom:**
```
toolRegistry.getTool("calculator"); // Returns empty
```

**Solution:**
```java
// Manual registration fallback
@Component
public class ToolRegistryInitializer {
    @Inject
    public ToolRegistryInitializer(ToolRegistry toolRegistry) {
        toolRegistry.register(new CalculatorTool()).block();
        toolRegistry.register(new FileTool()).block();
    }
}
```

### Issue: LLM API Key Not Found

**Symptom:**
```
java.lang.IllegalStateException: OPENAI_API_KEY not set
```

**Solution:**
```bash
# Set environment variable
export OPENAI_API_KEY=sk-your-key-here

# Or use .env file with dotenv library
echo "OPENAI_API_KEY=sk-your-key-here" > .env
```

---

**End of Integration Design Document**

*Last Updated: 2025-11-06*
*Version: 1.0*
*Author: PHD Systems Engineering Team*
