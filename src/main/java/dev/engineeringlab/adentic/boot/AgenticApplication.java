package dev.engineeringlab.adentic.boot;

import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.event.EventBus;
import dev.engineeringlab.adentic.boot.registry.ServiceRegistry;
import dev.engineeringlab.adentic.boot.scanner.ComponentScanner;
import dev.engineeringlab.adentic.boot.web.AgenticServer;
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
   *   <li>Registers core beans (EventBus, ServiceRegistry, ToolRegistry, AgenticServer)
   *   <li>Scans for @Component classes and all provider annotations
   *   <li>Registers and instantiates all components
   *   <li>Scans and registers providers in {@link ServiceRegistry}
   *   <li>Scans and registers EE agents in {@link ServiceRegistry} under "agent" category
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

    // 7. Scan and register services
    ServiceRegistry serviceRegistry = context.getBean(ServiceRegistry.class);
    Map<String, Set<Class<?>>> services = scanner.scanProviders();
    int totalServices = 0;

    for (Map.Entry<String, Set<Class<?>>> entry : services.entrySet()) {
      String category = entry.getKey();
      Set<Class<?>> classes = entry.getValue();

      for (Class<?> clazz : classes) {
        Object instance = context.getBean(clazz);
        String name = extractProviderName(clazz);
        serviceRegistry.register(category, name, instance);
        totalServices++;
      }
    }

    if (totalServices > 0) {
      log.info("Registered {} services across {} categories", totalServices, services.size());
    }

    // 7.5. Scan and register EE agents
    int totalAgents = registerEEAgents(context, scanner, serviceRegistry);

    // 8. Register REST controllers with HTTP server
    AgenticServer server = context.getBean(AgenticServer.class);
    for (Class<?> component : components) {
      if (component.isAnnotationPresent(RestController.class)) {
        Object controller = context.getBean(component);
        server.registerController(controller);
      }
    }

    // 9. Start HTTP server
    server.start(config.port());

    long duration = System.currentTimeMillis() - startTime;
    log.info("AgenticBoot application started in {}ms", duration);
    log.info(
        "Application context: {} beans registered ({} services, {} agents)",
        components.size() + 4,
        totalServices,
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

    // ServiceRegistry
    ServiceRegistry serviceRegistry = new ServiceRegistry();
    context.registerSingleton(ServiceRegistry.class, serviceRegistry);
    log.debug("Registered core bean: ServiceRegistry");

    // ToolRegistry (for EE agents)
    ToolRegistry toolRegistry = new SimpleToolRegistry();
    context.registerSingleton(ToolRegistry.class, toolRegistry);
    log.debug("Registered core bean: ToolRegistry");

    // AgenticServer
    AgenticServer server = new AgenticServer();
    context.registerSingleton(AgenticServer.class, server);
    log.debug("Registered core bean: AgenticServer");

    // Note: LLM clients, infrastructure providers, messaging providers,
    // observability providers, and resilience providers are disabled in standalone mode.
    // They will be re-enabled when the external dependencies are available.
    log.info("Running in standalone mode - external providers disabled");
  }

  /**
   * Register EE agents discovered via component scanning.
   *
   * <p>This method:
   *
   * <ol>
   *   <li>Scans for classes implementing {@link Agent} interface
   *   <li>Instantiates each agent (if not already in context)
   *   <li>Registers agents in {@link ServiceRegistry} under "agent" category
   * </ol>
   *
   * @param context the application context
   * @param scanner the component scanner
   * @param serviceRegistry the service registry
   * @return number of agents registered
   */
  private static int registerEEAgents(
      final AgenticContext context,
      final ComponentScanner scanner,
      final ServiceRegistry serviceRegistry) {

    Set<Class<?>> agentClasses = scanner.scanAgents();
    if (agentClasses.isEmpty()) {
      log.debug("No EE agents found on classpath");
      return 0;
    }

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

        // Register in ServiceRegistry under "agent" category
        serviceRegistry.register("agent", agentName, agent);
        log.debug("Registered EE agent: {} ({})", agentName, agentClass.getSimpleName());

        agentCount++;
      } catch (Exception e) {
        log.warn("Failed to register agent: {}", agentClass.getName(), e);
      }
    }

    if (agentCount > 0) {
      log.info("Registered {} EE agents", agentCount);
    }

    return agentCount;
  }

  /**
   * Extract provider name from class annotations.
   *
   * <p>Looks for a 'name' attribute in provider annotations. Falls back to decapitalized class name
   * if not found.
   *
   * @param clazz the provider class
   * @return the provider name
   */
  private static String extractProviderName(final Class<?> clazz) {
    // Try to find name from any annotation with a 'name' method
    for (java.lang.annotation.Annotation annotation : clazz.getAnnotations()) {
      try {
        java.lang.reflect.Method nameMethod = annotation.annotationType().getMethod("name");
        String name = (String) nameMethod.invoke(annotation);
        if (name != null && !name.isEmpty()) {
          return name;
        }
      } catch (NoSuchMethodException
          | IllegalAccessException
          | java.lang.reflect.InvocationTargetException e) {
        // Annotation doesn't have 'name' method or failed to invoke, continue
      }
    }

    // Fallback: decapitalized simple class name
    String simpleName = clazz.getSimpleName();
    if (simpleName.isEmpty()) {
      return simpleName;
    }
    return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
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

        :: AgenticBoot ::        (v1.0.0-standalone)
        """;

    System.out.println(banner);
  }
}
