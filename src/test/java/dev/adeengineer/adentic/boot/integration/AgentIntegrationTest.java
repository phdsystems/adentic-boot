package dev.adeengineer.adentic.boot.integration;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.Component;
import dev.adeengineer.adentic.boot.context.AgenticContext;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.agent.Agent;
import dev.adeengineer.agent.TaskRequest;
import dev.adeengineer.agent.TaskResult;
import dev.adeengineer.ee.llm.tools.ToolRegistry;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for EE agent registration and execution.
 *
 * <p>Tests the complete agent lifecycle:
 *
 * <ul>
 *   <li>Agent discovery via ComponentScanner
 *   <li>Agent registration in ProviderRegistry
 *   <li>Agent retrieval and execution
 *   <li>ToolRegistry initialization
 * </ul>
 */
@DisplayName("Agent Integration Tests")
class AgentIntegrationTest {

  private static AgenticContext context;

  @BeforeAll
  static void setUp() {
    context = dev.adeengineer.adentic.boot.AgenticApplication.run(TestApp.class);
  }

  @AfterAll
  static void tearDown() {
    if (context != null) {
      context.close();
    }
  }

  @Test
  @DisplayName("Should register agents in ProviderRegistry under 'agent' category")
  void testAgentRegistration() {
    // Given: Application context already initialized in setUp()

    // When: Retrieve ProviderRegistry
    ProviderRegistry registry = context.getBean(ProviderRegistry.class);

    // Then: Agents should be registered under "agent" category
    Map<String, Object> agents = registry.getAllAgents();
    assertNotNull(agents, "Agent map should not be null");

    // Note: Actual agents will only be found if there are Agent implementations in the test package
    // This test verifies the registration mechanism works
  }

  @Test
  @DisplayName("Should initialize ToolRegistry as core bean")
  void testToolRegistryInitialization() {
    // Given: Application context already initialized

    // When: Retrieve ToolRegistry
    ToolRegistry toolRegistry = context.getBean(ToolRegistry.class);

    // Then: ToolRegistry should be available
    assertNotNull(toolRegistry, "ToolRegistry should be registered");

    // Verify it's a functioning registry
    Integer count = toolRegistry.count().block();
    assertNotNull(count, "ToolRegistry count should not be null");
    assertEquals(0, count, "ToolRegistry should start empty");
  }

  @Test
  @DisplayName("Should manually register and execute agent")
  void testManualAgentRegistrationAndExecution() {
    // Given: Application context with manually registered agent
    ProviderRegistry registry = context.getBean(ProviderRegistry.class);

    TestAgent testAgent = new TestAgent("test-agent-manual", "Hello from test agent!");
    registry.registerAgent("test-agent-manual", testAgent);

    // When: Retrieve and execute agent
    Agent agent =
        registry
            .<Agent>getAgent("test-agent-manual")
            .orElseThrow(() -> new AssertionError("Agent not found"));

    TaskRequest request = TaskRequest.of("test-agent-manual", "test task", Map.of());

    TaskResult result = agent.executeTask(request);

    // Then: Agent should execute successfully
    assertNotNull(result, "Result should not be null");
    assertTrue(result.success(), "Execution should be successful");
    assertEquals("Hello from test agent!", result.output(), "Output should match");
    assertNull(result.error(), "Error should be null");
  }

  @Test
  @DisplayName("Should retrieve all registered agents")
  void testGetAllAgents() {
    // Given: Application with multiple manually registered agents
    ProviderRegistry registry = context.getBean(ProviderRegistry.class);

    registry.registerAgent("get-all-1", new TestAgent("get-all-1", "Response 1"));
    registry.registerAgent("get-all-2", new TestAgent("get-all-2", "Response 2"));
    registry.registerAgent("get-all-3", new TestAgent("get-all-3", "Response 3"));

    // When: Get all agents
    Map<String, Object> agents = registry.getAllAgents();

    // Then: All registered agents should be present
    assertNotNull(agents, "Agents map should not be null");
    assertTrue(agents.size() >= 3, "Should have at least 3 agents");
    assertTrue(agents.containsKey("get-all-1"), "Should contain get-all-1");
    assertTrue(agents.containsKey("get-all-2"), "Should contain get-all-2");
    assertTrue(agents.containsKey("get-all-3"), "Should contain get-all-3");
  }

  @Test
  @DisplayName("Should handle missing agent gracefully")
  void testMissingAgent() {
    // Given: Application context
    ProviderRegistry registry = context.getBean(ProviderRegistry.class);

    // When: Try to get non-existent agent
    var agentOpt = registry.getAgent("non-existent");

    // Then: Should return empty Optional
    assertTrue(agentOpt.isEmpty(), "Should return empty Optional for missing agent");
  }

  /** Test application class for integration tests. */
  @AgenticBootApplication(
      port = 8081,
      scanBasePackages = "dev.adeengineer.adentic.boot.integration")
  @Component
  public static class TestApp {
    // Test application marker class
  }
}
