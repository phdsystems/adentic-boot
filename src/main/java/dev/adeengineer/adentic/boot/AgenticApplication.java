package dev.adeengineer.adentic.boot;

import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.context.AgenticContext;
import dev.adeengineer.adentic.boot.event.EventBus;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.boot.scanner.ComponentScanner;
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
   *   <li>Registers core beans (EventBus, ProviderRegistry, AgenticServer)
   *   <li>Scans for @Component classes and all provider annotations
   *   <li>Registers and instantiates all components
   *   <li>Scans and registers providers in {@link ProviderRegistry}
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
        "Application context: {} beans registered ({} providers)",
        components.size() + 3,
        totalProviders);

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

    // AgenticServer
    dev.adeengineer.adentic.boot.web.AgenticServer server =
        new dev.adeengineer.adentic.boot.web.AgenticServer();
    context.registerSingleton(dev.adeengineer.adentic.boot.web.AgenticServer.class, server);
    log.debug("Registered core bean: AgenticServer");

    // TODO: Add ConfigurationLoader
    // TODO: Add AsyncExecutor
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
