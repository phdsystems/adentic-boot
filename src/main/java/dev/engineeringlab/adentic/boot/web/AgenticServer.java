package dev.engineeringlab.adentic.boot.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.web.annotations.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Embedded HTTP server using Javalin with automatic REST controller registration.
 *
 * <p>Scans for @RestController beans and automatically registers all @GetMapping, @PostMapping,
 * etc. annotated methods as HTTP routes.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * AgenticServer server = new AgenticServer();
 * server.registerController(new AgentController());
 * server.start(8080);
 * }</pre>
 */
@Slf4j
public class AgenticServer implements AutoCloseable {

  private final Javalin app;
  private final ObjectMapper objectMapper;
  private final Map<String, Object> controllers = new HashMap<>();

  /** Creates a new AgenticServer with default configuration. */
  public AgenticServer() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.registerModule(new JavaTimeModule());

    this.app =
        Javalin.create(
            config -> {
              config.http.defaultContentType = "application/json";
              config.bundledPlugins.enableCors(
                  cors -> {
                    cors.addRule(it -> it.anyHost());
                  });
            });

    // Register health check endpoint
    app.get(
        "/health",
        ctx -> {
          ctx.json(Map.of("status", "UP"));
        });
  }

  /**
   * Register a REST controller.
   *
   * <p>Scans the controller for @GetMapping, @PostMapping, etc. and automatically registers HTTP
   * routes.
   *
   * @param controller the controller instance
   */
  public void registerController(final Object controller) {
    Class<?> controllerClass = controller.getClass();

    if (!controllerClass.isAnnotationPresent(RestController.class)) {
      log.warn(
          "Class {} is not annotated with @RestController, skipping",
          controllerClass.getSimpleName());
      return;
    }

    String basePath = "";
    if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
      basePath = controllerClass.getAnnotation(RequestMapping.class).value();
    }

    controllers.put(controllerClass.getName(), controller);

    // Register all HTTP mapping methods
    for (Method method : controllerClass.getDeclaredMethods()) {
      registerMethodRoutes(controller, method, basePath);
    }

    log.info("Registered REST controller: {}", controllerClass.getSimpleName());
  }

  /**
   * Register routes for a controller method.
   *
   * @param controller the controller instance
   * @param method the method
   * @param basePath base path from class-level @RequestMapping
   */
  private void registerMethodRoutes(
      final Object controller, final Method method, final String basePath) {

    String methodPath = "";

    if (method.isAnnotationPresent(GetMapping.class)) {
      methodPath = method.getAnnotation(GetMapping.class).value();
      String fullPath = buildPath(basePath, methodPath);
      app.get(fullPath, ctx -> invokeControllerMethod(controller, method, ctx));
      log.debug("Registered GET {}", fullPath);
    } else if (method.isAnnotationPresent(PostMapping.class)) {
      methodPath = method.getAnnotation(PostMapping.class).value();
      String fullPath = buildPath(basePath, methodPath);
      app.post(fullPath, ctx -> invokeControllerMethod(controller, method, ctx));
      log.debug("Registered POST {}", fullPath);
    }
    // Add more mappings (PUT, DELETE, PATCH) as needed
  }

  /**
   * Build full path from base path and method path.
   *
   * @param basePath base path
   * @param methodPath method path
   * @return full path
   */
  private String buildPath(final String basePath, final String methodPath) {
    String base = basePath.startsWith("/") ? basePath : "/" + basePath;
    String method = methodPath.startsWith("/") ? methodPath : "/" + methodPath;

    if (basePath.isEmpty()) {
      return method;
    }
    if (methodPath.isEmpty()) {
      return base;
    }

    return base + method;
  }

  /**
   * Invoke a controller method with parameter injection.
   *
   * @param controller the controller instance
   * @param method the method to invoke
   * @param ctx Javalin context
   * @throws Exception if invocation fails
   */
  private void invokeControllerMethod(
      final Object controller, final Method method, final Context ctx) throws Exception {

    method.setAccessible(true);

    Parameter[] parameters = method.getParameters();
    Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {
      Parameter param = parameters[i];

      if (param.isAnnotationPresent(PathVariable.class)) {
        // Extract path variable
        String varName = param.getAnnotation(PathVariable.class).value();
        if (varName.isEmpty()) {
          varName = param.getName();
        }
        args[i] = ctx.pathParam(varName);

      } else if (param.isAnnotationPresent(RequestBody.class)) {
        // Deserialize request body
        String body = ctx.body();
        args[i] = objectMapper.readValue(body, param.getType());

      } else {
        // Unsupported parameter type
        throw new IllegalArgumentException(
            "Unsupported parameter type: " + param.getType() + " in method " + method.getName());
      }
    }

    // Invoke method
    Object result = method.invoke(controller, args);

    // Handle response
    if (result instanceof ResponseEntity) {
      ResponseEntity<?> response = (ResponseEntity<?>) result;
      ctx.status(response.getStatusCode());
      if (response.getBody() != null) {
        ctx.json(response.getBody());
      }
    } else if (result != null) {
      // Direct return value - serialize as JSON
      ctx.json(result);
    }
  }

  /**
   * Start the HTTP server.
   *
   * @param port the port to listen on
   */
  public void start(final int port) {
    app.start(port);
    log.info("AgenticServer started on port {}", port);
    log.info("Health check available at: http://localhost:{}/health", port);
  }

  /** Stop the HTTP server. */
  public void stop() {
    app.stop();
    log.info("AgenticServer stopped");
  }

  /**
   * Get the underlying Javalin app for custom configuration.
   *
   * @return Javalin app
   */
  public Javalin getApp() {
    return app;
  }

  @Override
  public void close() {
    stop();
  }
}
