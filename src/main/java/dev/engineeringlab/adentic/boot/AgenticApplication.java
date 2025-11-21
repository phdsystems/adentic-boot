package dev.engineeringlab.adentic.boot;

import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.event.EventBus;
import dev.engineeringlab.adentic.boot.registry.ProviderRegistry;
import dev.engineeringlab.adentic.boot.scanner.ComponentScanner;
import dev.engineeringlab.agent.Agent;
import dev.engineeringlab.ee.llm.tools.SimpleToolRegistry;
import dev.engineeringlab.ee.llm.tools.ToolRegistry;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Bootstrap class for AgenticBoot applications.
 *
 * <p>Provides a {@code run()} method similar to Spring Boot's {@code SpringApplication.run()} that
 * initializes the application context, scans for components, and starts the HTTP server.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @AgenticBootApplication
 * public class SoftwareEngineerApplication {
 *     public static void main(String[] args) {
 *         AgenticApplication.run(SoftwareEngineerApplication.class, args);
 *     }
 * }
 * }</pre>
 */
@Slf4j
public final class AgenticApplication {

  /** Private constructor to prevent instantiation of utility class. */
  private AgenticApplication() {
    throw new UnsupportedOperationException("Utility class - do not instantiate");
  }

  /**
   * Run the AgenticBoot application.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Creates an {@link AgenticContext}
   *   <li>Registers core beans (EventBus, ProviderRegistry, ToolRegistry, AgenticServer)
   *   <li>Scans for @Component classes and all provider annotations
   *   <li>Registers and instantiates all components
   *   <li>Scans and registers providers in {@link ProviderRegistry}
   *   <li>Scans and registers EE agents (SimpleAgent, ReActAgent, etc.)
   *   <li>Registers REST controllers with HTTP server
   *   <li>Starts the HTTP server (if enabled)
   *   <li>Returns the application context
   * </ol>
   *
   * @param primarySource the main application class annotated with @AgenticBootApplication
   * @param args command-line arguments
   * @return the application context
   */
  public static AgenticContext run(final Class<?> primarySource, final String... args) {
    long startTime = System.currentTimeMillis();

    printBanner();

    log.info("Starting AgenticBoot application: {}", primarySource.getSimpleName());

    // 1. Create application context
    AgenticContext context = new AgenticContext();

    // 2. Register core beans
    registerCoreBeans(context);

    // 3. Get configuration from @AgenticBootApplication annotation
    AgenticBootApplication config = primarySource.getAnnotation(AgenticBootApplication.class);
    if (config == null) {
      throw new IllegalArgumentException(
          "Main class must be annotated with @AgenticBootApplication: " + primarySource.getName());
    }

    // 4. Determine base package for scanning
    String[] basePackages = config.scanBasePackages();
    String scanPackage =
        (basePackages.length > 0) ? basePackages[0] : primarySource.getPackageName();

    log.info("Base package for component scanning: {}", scanPackage);

    // 5. Scan for components
    ComponentScanner scanner = new ComponentScanner(scanPackage);
    Set<Class<?>> components = scanner.scan();

    // 6. Register components in context
    for (Class<?> component : components) {
      context.registerBean(component);
    }

    // 7. Scan and register providers
    ProviderRegistry providerRegistry = context.getBean(ProviderRegistry.class);
    Map<String, Set<Class<?>>> providers = scanner.scanProviders();
    int totalProviders = 0;

    for (Map.Entry<String, Set<Class<?>>> entry : providers.entrySet()) {
      String category = entry.getKey();
      Set<Class<?>> categoryProviders = entry.getValue();

      for (Class<?> providerClass : categoryProviders) {
        Object providerInstance = context.getBean(providerClass);
        providerRegistry.registerProviderFromClass(providerClass, providerInstance);
        totalProviders++;
      }
    }

    if (totalProviders > 0) {
      log.info("Registered {} providers across {} categories", totalProviders, providers.size());
    }

    // 7.5. Scan and register EE agents
    int totalAgents = registerEEAgents(context, scanner, providerRegistry);

    // 8. Register REST controllers with HTTP server
    dev.adeengineer.adentic.boot.web.AgenticServer server =
        context.getBean(dev.adeengineer.adentic.boot.web.AgenticServer.class);
    for (Class<?> component : components) {
      if (component.isAnnotationPresent(
          dev.adeengineer.adentic.boot.annotations.RestController.class)) {
        Object controller = context.getBean(component);
        server.registerController(controller);
      }
    }

    // 9. Start HTTP server
    server.start(config.port());

    long duration = System.currentTimeMillis() - startTime;
    log.info("AgenticBoot application started in {}ms", duration);
    log.info(
        "Application context: {} beans registered ({} providers, {} agents)",
        components.size() + 4,
        totalProviders,
        totalAgents);

    // Add shutdown hook
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread(
                () -> {
                  log.info("Shutting down AgenticBoot application");
                  context.close();
                }));

    return context;
  }

  /**
   * Register core framework beans.
   *
   * @param context the application context
   */
  private static void registerCoreBeans(final AgenticContext context) {
    // EventBus
    EventBus eventBus = new EventBus();
    context.registerSingleton(EventBus.class, eventBus);
    log.debug("Registered core bean: EventBus");

    // ProviderRegistry
    ProviderRegistry providerRegistry = new ProviderRegistry();
    context.registerSingleton(ProviderRegistry.class, providerRegistry);
    log.debug("Registered core bean: ProviderRegistry");

    // ToolRegistry (for EE agents)
    ToolRegistry toolRegistry = new SimpleToolRegistry();
    context.registerSingleton(ToolRegistry.class, toolRegistry);
    log.debug("Registered core bean: ToolRegistry");

    // AgenticServer
    dev.adeengineer.adentic.boot.web.AgenticServer server =
        new dev.adeengineer.adentic.boot.web.AgenticServer();
    context.registerSingleton(dev.adeengineer.adentic.boot.web.AgenticServer.class, server);
    log.debug("Registered core bean: AgenticServer");

    // LLM Clients (from adentic-ai-client)
    registerLLMClients(providerRegistry);

    // Infrastructure Providers (from adentic-core)
    registerInfrastructureProviders(providerRegistry);

    // Enterprise Messaging (Kafka, RabbitMQ)
    registerMessagingProviders(providerRegistry);

    // Observability (Metrics, Health Checks)
    registerObservabilityProviders(providerRegistry);

    // Resilience Patterns (Circuit Breakers, Retry)
    registerResilienceProviders(providerRegistry);

    // TODO: Add ConfigurationLoader
    // TODO: Add AsyncExecutor
  }

  /**
   * Register LLM clients from adentic-ai-client.
   *
   * <p>Creates and registers OpenAI, Gemini, and other LLM clients with environment configuration.
   *
   * @param registry provider registry
   */
  private static void registerLLMClients(final ProviderRegistry registry) {
    // OpenAI client
    if (dev.adeengineer.adentic.boot.provider.LLMClientFactory.isOpenAIAvailable()) {
      dev.adeengineer.ai.openai.OpenAIClient openaiClient =
          dev.adeengineer.adentic.boot.provider.LLMClientFactory.createOpenAIClient();
      registry.registerProvider("llm", "openai", openaiClient);
      log.info("Registered OpenAI LLM client");
    } else {
      log.debug("OpenAI client not available (OPENAI_API_KEY not set)");
    }

    // Anthropic Claude client
    if (dev.adeengineer.adentic.boot.provider.LLMClientFactory.isAnthropicAvailable()) {
      dev.adeengineer.ai.anthropic.AnthropicClient anthropicClient =
          dev.adeengineer.adentic.boot.provider.LLMClientFactory.createAnthropicClient();
      registry.registerProvider("llm", "anthropic", anthropicClient);
      log.info("Registered Anthropic Claude LLM client");
    } else {
      log.debug("Anthropic client not available (ANTHROPIC_API_KEY not set)");
    }

    // Google Gemini client
    if (dev.adeengineer.adentic.boot.provider.LLMClientFactory.isGeminiAvailable()) {
      dev.adeengineer.ai.gemini.GeminiClient geminiClient =
          dev.adeengineer.adentic.boot.provider.LLMClientFactory.createGeminiClient();
      registry.registerProvider("llm", "gemini", geminiClient);
      log.info("Registered Google Gemini LLM client");
    } else {
      log.debug("Gemini client not available (GEMINI_API_KEY not set)");
    }

    // vLLM client
    if (dev.adeengineer.adentic.boot.provider.LLMClientFactory.isVLLMAvailable()) {
      dev.adeengineer.ai.runtime.vllm.VLLMClient vllmClient =
          dev.adeengineer.adentic.boot.provider.LLMClientFactory.createVLLMClient();
      registry.registerProvider("llm", "vllm", vllmClient);
      log.info("Registered vLLM (self-hosted) LLM client");
    } else {
      log.debug("vLLM client not available (VLLM_BASE_URL not set)");
    }

    // Ollama client (always available with default localhost)
    dev.adeengineer.ai.runtime.ollama.OllamaClient ollamaClient =
        dev.adeengineer.adentic.boot.provider.LLMClientFactory.createOllamaClient();
    registry.registerProvider("llm", "ollama", ollamaClient);
    log.info("Registered Ollama (local) LLM client");
  }

  /**
   * Register infrastructure providers from adentic-core.
   *
   * <p>Creates and registers infrastructure providers for task queues, orchestration, tools,
   * storage, messaging, and memory.
   *
   * @param registry provider registry
   */
  private static void registerInfrastructureProviders(final ProviderRegistry registry) {
    // Task Queue Provider
    dev.adeengineer.adentic.provider.queue.InMemoryTaskQueueProvider queueProvider =
        dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory
            .createTaskQueueProvider();
    registry.registerProvider("queue", "in-memory", queueProvider);
    log.info("Registered InMemory task queue provider");

    // Orchestration Provider
    dev.adeengineer.adentic.provider.orchestration.SimpleOrchestrationProvider
        orchestrationProvider =
            dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory
                .createOrchestrationProvider();
    registry.registerProvider("orchestration", "simple", orchestrationProvider);
    log.info("Registered Simple orchestration provider");

    // Tool Providers
    dev.adeengineer.adentic.provider.tools.SimpleToolProvider simpleToolProvider =
        dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory
            .createSimpleToolProvider();
    registry.registerProvider("tool", "simple", simpleToolProvider);
    log.info("Registered Simple tool provider");

    dev.adeengineer.adentic.tool.config.MavenToolConfig mavenConfig =
        dev.adeengineer.adentic.tool.config.MavenToolConfig.builder()
            .workingDirectory(System.getProperty("user.dir"))
            .timeoutSeconds(300)
            .autoInstallWrapper(true)
            .build();
    dev.adeengineer.adentic.provider.tools.MavenToolProvider mavenToolProvider =
        dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory.createMavenToolProvider(
            mavenConfig);
    registry.registerProvider("tool", "maven", mavenToolProvider);
    log.info("Registered Maven tool provider");

    // Storage Provider
    try {
      String storagePath = System.getProperty("adentic.storage.path", "./data/storage");
      com.fasterxml.jackson.databind.ObjectMapper objectMapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      dev.adeengineer.adentic.storage.local.LocalStorageProvider storageProvider =
          dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory
              .createLocalStorageProvider(storagePath, objectMapper);
      registry.registerProvider("storage", "local", storageProvider);
      log.info("Registered Local storage provider");
    } catch (Exception e) {
      log.error("Failed to register Local storage provider: {}", e.getMessage(), e);
    }

    // Messaging Provider
    dev.adeengineer.adentic.agent.coordination.InMemoryMessageBus messageBus =
        dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory.createMessageBus();
    registry.registerProvider("messaging", "in-memory", messageBus);
    log.info("Registered InMemory message bus");

    // Memory Provider (requires EmbeddingService)
    try {
      String embeddingApiKey =
          System.getProperty("openai.api.key", System.getenv("OPENAI_API_KEY"));
      if (embeddingApiKey != null && !embeddingApiKey.isBlank()) {
        String embeddingModel =
            System.getProperty("openai.embedding.model", "text-embedding-3-small");
        com.fasterxml.jackson.databind.ObjectMapper objectMapper =
            new com.fasterxml.jackson.databind.ObjectMapper();

        dev.adeengineer.rag.embedding.EmbeddingService embeddingService =
            dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory
                .createOpenAIEmbeddingService(embeddingApiKey, embeddingModel, objectMapper);

        dev.adeengineer.adentic.provider.memory.InMemoryMemoryProvider memoryProvider =
            dev.adeengineer.adentic.boot.provider.InfrastructureProviderFactory
                .createMemoryProvider(embeddingService);

        registry.registerProvider("memory", "in-memory", memoryProvider);
        log.info("Registered InMemory memory provider with OpenAI embeddings");
      } else {
        log.info("Memory provider not registered - OPENAI_API_KEY not configured");
      }
    } catch (Exception e) {
      log.error("Failed to register InMemory memory provider: {}", e.getMessage(), e);
    }
  }

  /**
   * Register EE agents discovered via component scanning.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Scans for classes implementing {@link Agent} interface
   *   <li>Instantiates each agent (if not already in context)
   *   <li>Registers agents in {@link ProviderRegistry} under "agent" category
   *   <li>Publishes agent lifecycle events to {@link EventBus}
   * </ol>
   *
   * @param context the application context
   * @param scanner the component scanner
   * @param providerRegistry the provider registry
   * @return number of agents registered
   */
  private static int registerEEAgents(
      final AgenticContext context,
      final ComponentScanner scanner,
      final ProviderRegistry providerRegistry) {

    Set<Class<?>> agentClasses = scanner.scanAgents();
    if (agentClasses.isEmpty()) {
      log.debug("No EE agents found on classpath");
      return 0;
    }

    // EventBus eventBus = context.getBean(EventBus.class); // TODO: Enable when event classes added
    int agentCount = 0;

    for (Class<?> agentClass : agentClasses) {
      try {
        // Get or create agent instance from context
        Object agentInstance;
        if (context.containsBean(agentClass)) {
          agentInstance = context.getBean(agentClass);
        } else {
          // Not registered as component, create instance manually
          agentInstance = agentClass.getDeclaredConstructor().newInstance();
        }

        // Cast to Agent interface
        Agent agent = (Agent) agentInstance;
        String agentName = agent.getName();

        // Register in ProviderRegistry under "agent" category
        providerRegistry.registerAgent(agentName, agent);
        log.debug("Registered EE agent: {} ({})", agentName, agentClass.getSimpleName());

        // TODO: Publish agent registered event to EventBus (requires AgentRegisteredEvent class)
        // eventBus.publish(new AgentRegisteredEvent(agent));

        agentCount++;
      } catch (Exception e) {
        log.warn("Failed to register agent: {}", agentClass.getName(), e);
      }
    }

    if (agentCount > 0) {
      log.info("Registered {} EE agents in ProviderRegistry", agentCount);
    }

    return agentCount;
  }

  /**
   * Register enterprise messaging providers (Kafka, RabbitMQ).
   *
   * @param registry provider registry
   */
  private static void registerMessagingProviders(final ProviderRegistry registry) {
    // Kafka message broker
    if (dev.adeengineer.adentic.boot.provider.MessagingProviderFactory.isKafkaAvailable()) {
      dev.adeengineer.adentic.messaging.kafka.KafkaMessageBroker kafkaBroker =
          dev.adeengineer.adentic.boot.provider.MessagingProviderFactory.createKafkaBroker();
      registry.registerProvider("messaging", "kafka", kafkaBroker);
      log.info("Registered Kafka message broker");
    } else {
      log.debug("Kafka broker not available (KAFKA_BOOTSTRAP_SERVERS not set)");
    }

    // RabbitMQ message broker
    if (dev.adeengineer.adentic.boot.provider.MessagingProviderFactory.isRabbitMQAvailable()) {
      dev.adeengineer.adentic.messaging.rabbitmq.RabbitMQMessageBroker rabbitmqBroker =
          dev.adeengineer.adentic.boot.provider.MessagingProviderFactory.createRabbitMQBroker();
      registry.registerProvider("messaging", "rabbitmq", rabbitmqBroker);
      log.info("Registered RabbitMQ message broker");
    } else {
      log.debug("RabbitMQ broker not available (RABBITMQ_HOST not set)");
    }
  }

  /**
   * Register observability providers (metrics, health checks).
   *
   * @param registry provider registry
   */
  private static void registerObservabilityProviders(final ProviderRegistry registry) {
    // Metrics collector
    dev.adeengineer.adentic.observability.monitoring.MetricsCollector metricsCollector =
        dev.adeengineer.adentic.boot.provider.ObservabilityProviderFactory.createMetricsCollector();
    registry.registerProvider("metrics", "default", metricsCollector);
    log.info("Registered default metrics collector");

    // Health check service
    dev.adeengineer.adentic.observability.monitoring.HealthCheckService healthService =
        dev.adeengineer.adentic.boot.provider.ObservabilityProviderFactory
            .createHealthCheckService();
    registry.registerProvider("health", "default", healthService);
    log.info("Registered health check service");

    // Prometheus metrics provider (if enabled)
    if (dev.adeengineer.adentic.boot.provider.ObservabilityProviderFactory
        .isPrometheusAvailable()) {
      dev.adeengineer.adentic.observability.providers.prometheus.PrometheusMetricsProvider
          prometheusProvider =
              dev.adeengineer.adentic.boot.provider.ObservabilityProviderFactory
                  .createPrometheusProvider();
      registry.registerProvider("prometheus", "metrics", prometheusProvider);
      log.info("Registered Prometheus metrics provider");
    } else {
      log.debug("Prometheus not enabled (PROMETHEUS_ENABLED not true)");
    }
  }

  /**
   * Register resilience providers (circuit breakers, retry, bulkhead).
   *
   * @param registry provider registry
   */
  private static void registerResilienceProviders(final ProviderRegistry registry) {
    // Resilience4j proxy factory
    dev.adeengineer.resilience4j.Resilience4jProxyFactory resilienceFactory =
        dev.adeengineer.adentic.boot.provider.ResilienceProviderFactory
            .createResilienceProxyFactory();
    registry.registerProvider("resilience", "resilience4j", resilienceFactory);
    log.info("Registered Resilience4j proxy factory (circuit breaker, retry, bulkhead)");
  }

  /** Print startup banner. */
  private static void printBanner() {
    String banner =
        """
         █████╗  ██████╗ ███████╗███╗   ██╗████████╗██╗ ██████╗
        ██╔══██╗██╔════╝ ██╔════╝████╗  ██║╚══██╔══╝██║██╔════╝
        ███████║██║  ███╗█████╗  ██╔██╗ ██║   ██║   ██║██║
        ██╔══██║██║   ██║██╔══╝  ██║╚██╗██║   ██║   ██║██║
        ██║  ██║╚██████╔╝███████╗██║ ╚████║   ██║   ██║╚██████╗
        ╚═╝  ╚═╝ ╚═════╝ ╚══════╝╚═╝  ╚═══╝   ╚═╝   ╚═╝ ╚═════╝
        ██████╗  ██████╗  ██████╗ ████████╗
        ██╔══██╗██╔═══██╗██╔═══██╗╚══██╔══╝
        ██████╔╝██║   ██║██║   ██║   ██║
        ██╔══██╗██║   ██║██║   ██║   ██║
        ██████╔╝╚██████╔╝╚██████╔╝   ██║
        ╚═════╝  ╚═════╝  ╚═════╝    ╚═╝

        :: AgenticBoot ::        (v1.0.0)
        """;

    System.out.println(banner);
  }
}
