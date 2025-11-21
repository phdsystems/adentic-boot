package dev.engineeringlab.adentic.boot.integration;

import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.Inject;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.registry.ProviderRegistry;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PathVariable;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import dev.engineeringlab.agent.Agent;
import dev.engineeringlab.agent.TaskRequest;
import dev.engineeringlab.agent.TaskResult;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for agent REST API endpoints.
 *
 * <p>Tests complete request-response cycle:
 *
 * <ul>
 *   <li>REST controller with agent injection
 *   <li>HTTP GET/POST endpoints
 *   <li>Agent execution via HTTP
 *   <li>JSON serialization/deserialization
 * </ul>
 */
@DisplayName("Agent REST API Integration Tests")
class AgentRestApiIntegrationTest {

  private static AgenticContext context;
  private static HttpClient httpClient;
  private static final int TEST_PORT = 8082;
  private static final String BASE_URL = "http://localhost:" + TEST_PORT;

  @BeforeAll
  static void setUp() {
    // Start application with REST controller
    context = dev.adeengineer.adentic.boot.AgenticApplication.run(TestRestApp.class);

    // Register test agent
    ProviderRegistry registry = context.getBean(ProviderRegistry.class);
    registry.registerAgent("calculator", new TestAgent("calculator", "42"));

    // Create HTTP client
    httpClient = HttpClient.newHttpClient();

    // Wait for server to start
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @AfterAll
  static void tearDown() {
    if (context != null) {
      context.close();
    }
  }

  @Test
  @DisplayName("Should execute agent via GET endpoint")
  void testAgentExecutionViaGet() throws IOException, InterruptedException {
    // Given: GET request to /api/agent/execute/calculator
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/agent/execute/calculator"))
            .GET()
            .build();

    // When: Send request
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // Then: Should return agent result
    assertEquals(200, response.statusCode(), "Should return 200 OK");
    assertTrue(response.body().contains("42"), "Response should contain agent output");
  }

  @Test
  @DisplayName("Should execute agent via POST endpoint with JSON body")
  void testAgentExecutionViaPost() throws IOException, InterruptedException {
    // Given: POST request with JSON body
    String jsonBody = "{\"agentName\":\"calculator\",\"task\":\"2+2\"}";

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/agent/ask"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    // When: Send request
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // Then: Should return agent result
    assertEquals(200, response.statusCode(), "Should return 200 OK");
    assertTrue(response.body().contains("42"), "Response should contain agent output");
  }

  @Test
  @DisplayName("Should return agent status via GET endpoint")
  void testAgentStatus() throws IOException, InterruptedException {
    // Given: GET request to /api/agent/status
    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/api/agent/status")).GET().build();

    // When: Send request
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // Then: Should return status
    assertEquals(200, response.statusCode(), "Should return 200 OK");
    assertTrue(response.body().contains("calculator"), "Response should contain agent name");
  }

  @Test
  @DisplayName("Should handle missing agent gracefully")
  void testMissingAgent() throws IOException, InterruptedException {
    // Given: Request for non-existent agent
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/api/agent/execute/non-existent"))
            .GET()
            .build();

    // When: Send request
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    // Then: Should return error response
    assertTrue(response.statusCode() >= 400, "Should return error status code");
  }

  /** Test application with REST controller. */
  @AgenticBootApplication(
      port = TEST_PORT,
      scanBasePackages = "dev.adeengineer.adentic.boot.integration")
  public static class TestRestApp {
    // Test application marker class
  }

  /** Test REST controller for agent execution. */
  @RestController
  public static class AgentController {

    private final ProviderRegistry registry;

    @Inject
    public AgentController(final ProviderRegistry registry) {
      this.registry = registry;
    }

    @GetMapping("/api/agent/status")
    public Map<String, Object> getStatus() {
      return Map.of("status", "running", "agents", registry.getAllAgents().keySet());
    }

    @GetMapping("/api/agent/execute/{name}")
    public Map<String, Object> executeAgent(@PathVariable("name") final String agentName) {
      Agent agent =
          registry
              .<Agent>getAgent(agentName)
              .orElseThrow(() -> new RuntimeException("Agent not found: " + agentName));

      TaskRequest request = TaskRequest.of(agentName, "test", Map.of());

      TaskResult result = agent.executeTask(request);

      return Map.of(
          "success", result.success(),
          "output", result.output() != null ? result.output() : "",
          "error", result.error() != null ? result.error() : "");
    }

    @PostMapping("/api/agent/ask")
    public Map<String, Object> askAgent(@RequestBody final Map<String, String> body) {
      String agentName = body.get("agentName");
      String task = body.get("task");

      Agent agent =
          registry
              .<Agent>getAgent(agentName)
              .orElseThrow(() -> new RuntimeException("Agent not found: " + agentName));

      TaskRequest request = TaskRequest.of(agentName, task, Map.of());

      TaskResult result = agent.executeTask(request);

      return Map.of(
          "success", result.success(),
          "output", result.output() != null ? result.output() : "",
          "error", result.error() != null ? result.error() : "");
    }
  }
}
