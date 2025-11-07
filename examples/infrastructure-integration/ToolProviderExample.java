package examples.infrastructure.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.GetMapping;
import dev.adeengineer.adentic.boot.annotations.PostMapping;
import dev.adeengineer.adentic.boot.annotations.RequestBody;
import dev.adeengineer.adentic.boot.annotations.RequestParam;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.provider.tools.MavenToolProvider;
import dev.adeengineer.adentic.provider.tools.SimpleToolProvider;
import dev.adeengineer.tool.model.Tool;
import dev.adeengineer.tool.model.ToolInvocation;
import dev.adeengineer.tool.model.ToolResult;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating Tool Provider integration with AgenticBoot.
 *
 * <p>This example shows:
 *
 * <ul>
 *   <li>SimpleToolProvider - Function calling with built-in tools (echo, calculator, timestamp)
 *   <li>MavenToolProvider - Maven operations (compile, test, package, etc.)
 *   <li>Dynamic tool registration
 *   <li>Custom tool creation
 *   <li>Tool invocation and result handling
 * </ul>
 *
 * <h2>Run</h2>
 *
 * <pre>{@code
 * mvn exec:java -Dexec.mainClass="examples.infrastructure.integration.ToolProviderExample"
 * }</pre>
 *
 * <h2>Test Endpoints</h2>
 *
 * <pre>{@code
 * # Simple Tool Operations
 * curl -X POST "http://localhost:8080/api/tools/simple/echo" \
 *   -H "Content-Type: application/json" \
 *   -d '{"text":"Hello, World!"}'
 *
 * curl -X POST "http://localhost:8080/api/tools/simple/calculator" \
 *   -H "Content-Type: application/json" \
 *   -d '{"operation":"add","a":10,"b":5}'
 *
 * curl "http://localhost:8080/api/tools/simple/timestamp"
 *
 * curl "http://localhost:8080/api/tools/simple/list"
 *
 * # Maven Tool Operations
 * curl -X POST "http://localhost:8080/api/tools/maven/compile"
 *
 * curl -X POST "http://localhost:8080/api/tools/maven/test"
 *
 * curl -X POST "http://localhost:8080/api/tools/maven/project-info"
 *
 * curl "http://localhost:8080/api/tools/maven/list"
 * }</pre>
 */
@AgenticBootApplication(port = 8080, scanBasePackages = "examples.infrastructure.integration")
public class ToolProviderExample {

  public static void main(String[] args) {
    AgenticApplication.run(ToolProviderExample.class, args);
  }

  /** REST controller for Simple Tool operations. */
  @Slf4j
  @RestController
  public static class SimpleToolController {

    @Inject private ProviderRegistry registry;

    /**
     * Echo tool - returns input text unchanged.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/simple/echo" -H
     * "Content-Type: application/json" -d '{"text":"Hello, World!"}'}
     */
    @PostMapping("/api/tools/simple/echo")
    public Mono<Map<String, Object>> echo(@RequestBody Map<String, Object> params) {
      log.info("Invoking echo tool with params: {}", params);

      return getSimpleToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("echo", params, Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Calculator tool - performs basic arithmetic.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/simple/calculator" -H
     * "Content-Type: application/json" -d '{"operation":"add","a":10,"b":5}'}
     */
    @PostMapping("/api/tools/simple/calculator")
    public Mono<Map<String, Object>> calculator(@RequestBody Map<String, Object> params) {
      log.info("Invoking calculator tool with params: {}", params);

      return getSimpleToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("calculator", params, Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Timestamp tool - returns current Unix timestamp.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/tools/simple/timestamp"}
     */
    @GetMapping("/api/tools/simple/timestamp")
    public Mono<Map<String, Object>> timestamp() {
      log.info("Invoking timestamp tool");

      return getSimpleToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("get_timestamp", Map.of(), Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * List all available tools.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/tools/simple/list"}
     */
    @GetMapping("/api/tools/simple/list")
    public Flux<Tool> listTools() {
      log.info("Listing all simple tools");

      return getSimpleToolProvider().flatMapMany(SimpleToolProvider::getAvailableTools);
    }

    /**
     * Register a custom tool.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/simple/register" -H
     * "Content-Type: application/json" -d '{"name":"uppercase","description":"Converts text to
     * uppercase","parameters":{"text":{"type":"string","description":"Text to
     * convert","required":true}},"category":"text"}'}
     */
    @PostMapping("/api/tools/simple/register")
    public Mono<Tool> registerTool(@RequestBody Tool tool) {
      log.info("Registering custom tool: {}", tool.name());

      return getSimpleToolProvider()
          .flatMap(
              provider -> {
                // Register tool with a simple implementation
                return provider.registerTool(
                    tool,
                    params -> {
                      // Simple uppercase implementation
                      String text = (String) params.get("text");
                      return text != null ? text.toUpperCase() : "";
                    });
              });
    }

    private Mono<SimpleToolProvider> getSimpleToolProvider() {
      return Mono.justOrEmpty(registry.getProvider("tool", "simple"))
          .switchIfEmpty(Mono.error(new RuntimeException("SimpleToolProvider not found")));
    }

    private Map<String, Object> formatToolResult(ToolResult result) {
      return Map.of(
          "success", result.success(),
          "output", result.output() != null ? result.output() : "",
          "error", result.error() != null ? result.error() : "",
          "executionTimeMs", result.executionTimeMs(),
          "timestamp", result.timestamp());
    }
  }

  /** REST controller for Maven Tool operations. */
  @Slf4j
  @RestController
  public static class MavenToolController {

    @Inject private ProviderRegistry registry;

    /**
     * Compile project sources.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/maven/compile"}
     */
    @PostMapping("/api/tools/maven/compile")
    public Mono<Map<String, Object>> compile(
        @RequestParam(value = "flags", required = false) String flags) {
      log.info("Compiling project with flags: {}", flags);

      Map<String, Object> params = flags != null ? Map.of("flags", flags) : Map.of();

      return getMavenToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("mvn_compile", params, Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Run unit tests.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/maven/test"}
     */
    @PostMapping("/api/tools/maven/test")
    public Mono<Map<String, Object>> test(
        @RequestParam(value = "flags", required = false) String flags) {
      log.info("Running tests with flags: {}", flags);

      Map<String, Object> params = flags != null ? Map.of("flags", flags) : Map.of();

      return getMavenToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("mvn_test", params, Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Package the project.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/maven/package"}
     */
    @PostMapping("/api/tools/maven/package")
    public Mono<Map<String, Object>> packageProject(
        @RequestParam(value = "flags", required = false) String flags) {
      log.info("Packaging project with flags: {}", flags);

      Map<String, Object> params = flags != null ? Map.of("flags", flags) : Map.of();

      return getMavenToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("mvn_package", params, Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Clean build artifacts.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/maven/clean"}
     */
    @PostMapping("/api/tools/maven/clean")
    public Mono<Map<String, Object>> clean() {
      log.info("Cleaning project");

      return getMavenToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("mvn_clean", Map.of(), Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Get project information.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/maven/project-info"}
     */
    @PostMapping("/api/tools/maven/project-info")
    public Mono<Map<String, Object>> projectInfo() {
      log.info("Getting project information");

      return getMavenToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation =
                    new ToolInvocation("mvn_project_info", Map.of(), Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * Execute custom Maven command.
     *
     * <p>Example: {@code curl -X POST "http://localhost:8080/api/tools/maven/exec" -H
     * "Content-Type: application/json" -d '{"command":"clean verify -DskipTests"}'}
     */
    @PostMapping("/api/tools/maven/exec")
    public Mono<Map<String, Object>> exec(@RequestBody Map<String, Object> params) {
      log.info("Executing custom Maven command with params: {}", params);

      return getMavenToolProvider()
          .flatMap(
              provider -> {
                ToolInvocation invocation = new ToolInvocation("mvn_exec", params, Map.of());
                return provider.invoke(invocation);
              })
          .map(this::formatToolResult);
    }

    /**
     * List all available Maven tools.
     *
     * <p>Example: {@code curl "http://localhost:8080/api/tools/maven/list"}
     */
    @GetMapping("/api/tools/maven/list")
    public Flux<Tool> listTools() {
      log.info("Listing all Maven tools");

      return getMavenToolProvider().flatMapMany(MavenToolProvider::getAvailableTools);
    }

    private Mono<MavenToolProvider> getMavenToolProvider() {
      return Mono.justOrEmpty(registry.getProvider("tool", "maven"))
          .switchIfEmpty(Mono.error(new RuntimeException("MavenToolProvider not found")));
    }

    private Map<String, Object> formatToolResult(ToolResult result) {
      return Map.of(
          "success", result.success(),
          "output", result.output() != null ? result.output() : "",
          "error", result.error() != null ? result.error() : "",
          "executionTimeMs", result.executionTimeMs(),
          "timestamp", result.timestamp());
    }
  }
}
