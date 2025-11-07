package dev.adeengineer.adentic.boot;

import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.context.AgenticContext;
import dev.adeengineer.adentic.boot.event.EventBus;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.boot.scanner.ComponentScanner;
import dev.adeengineer.agent.Agent;
import dev.adeengineer.ee.llm.tools.SimpleToolRegistry;
import dev.adeengineer.ee.llm.tools.ToolRegistry;
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
      registry.registerProvider("openai", "llm", openaiClient);
      log.info("Registered OpenAI LLM client");
    } else {
      log.debug("OpenAI client not available (OPENAI_API_KEY not set)");
    }

    // TODO: Add Gemini, vLLM, Anthropic clients
  }

  /**
   * Register infrastructure providers from adentic-core.
   *
   * <p>Creates and registers infrastructure providers for task queues, memory, and orchestration.
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

    // Memory Provider (note: requires EmbeddingService, will be added when available)
    // TODO: Add InMemoryMemoryProvider when EmbeddingService is configured
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
