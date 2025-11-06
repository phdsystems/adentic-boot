package examples.ee.integration;

import dev.adeengineer.adentic.boot.AgenticApplication;
import dev.adeengineer.adentic.boot.annotations.AgenticBootApplication;
import dev.adeengineer.adentic.boot.annotations.Inject;
import dev.adeengineer.adentic.boot.annotations.RestController;
import dev.adeengineer.adentic.boot.annotations.Service;
import dev.adeengineer.adentic.boot.registry.ProviderRegistry;
import dev.adeengineer.adentic.boot.web.annotations.GetMapping;
import dev.adeengineer.adentic.boot.web.annotations.PostMapping;
import dev.adeengineer.adentic.boot.web.annotations.RequestBody;
import dev.adeengineer.adentic.boot.web.annotations.RequestMapping;
import dev.adeengineer.ai.client.LLMClient;
import dev.adeengineer.ai.client.config.LLMClientConfig;
import dev.adeengineer.ai.client.mock.MockLLMClient;
import dev.adeengineer.ee.llm.agent.Agent;
import dev.adeengineer.ee.llm.agent.SimpleAgent;
import dev.adeengineer.ee.llm.agent.config.AgentConfig;
import dev.adeengineer.ee.llm.agent.model.AgentRequest;
import dev.adeengineer.ee.llm.agent.model.AgentResult;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

/**
 * Example application demonstrating AgenticBoot integration with Adentic EE agents.
 *
 * <p>This example shows:
 * <ul>
 *   <li>Manual registration of SimpleAgent in ProviderRegistry
 *   <li>Dependency injection of agents into controllers
 *   <li>REST API for agent execution
 *   <li>Reactive agent responses using Project Reactor
 * </ul>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Run application
 * mvn clean compile exec:java -Dexec.mainClass="examples.ee.integration.SimpleAgentExample"
 *
 * // Test via curl
 * curl -X POST http://localhost:8080/api/agent/ask \
 *   -H "Content-Type: application/json" \
 *   -d '{"question":"What is 2+2?"}'
 * }</pre>
 *
 * @since 1.1.0
 */
@Slf4j
@AgenticBootApplication
public class SimpleAgentExample {

  public static void main(String[] args) {
    AgenticApplication.run(SimpleAgentExample.class, args);
  }
}

/**
 * Service for initializing and registering EE agents.
 *
 * <p>This service demonstrates how to:
 * <ul>
 *   <li>Create LLM client (using MockLLMClient for demo)
 *   <li>Configure AgentConfig
 *   <li>Instantiate SimpleAgent
 *   <li>Register agent in ProviderRegistry
 * </ul>
 */
@Service
@Slf4j
class AgentInitializationService {

  private final ProviderRegistry registry;

  @Inject
  public AgentInitializationService(ProviderRegistry registry) {
    this.registry = registry;
    initializeAgents();
  }

  private void initializeAgents() {
    log.info("Initializing EE agents...");

    try {
      // Create LLM client (using Mock for demo - replace with real client in production)
      LLMClient llmClient = new MockLLMClient(LLMClientConfig.mock());

      // Configure agent
      AgentConfig config = AgentConfig.builder()
          .model("gpt-3.5-turbo")
          .systemPrompt("You are a helpful assistant. Provide clear, concise answers.")
          .temperature(0.7)
          .maxTokens(500)
          .build();

      // Create SimpleAgent
      SimpleAgent simpleAgent = new SimpleAgent(llmClient, null, null, config);

      // Register in ProviderRegistry under "agent" category
      registry.registerAgent("simple", simpleAgent);

      log.info("Successfully registered SimpleAgent");

    } catch (Exception e) {
      log.error("Failed to initialize agents", e);
      throw new RuntimeException("Agent initialization failed", e);
    }
  }
}

/**
 * REST controller exposing agent execution endpoints.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET /api/agent/status - Check agent availability
 *   <li>POST /api/agent/ask - Ask a question to SimpleAgent
 * </ul>
 */
@RestController
@RequestMapping("/api/agent")
@Slf4j
class AgentController {

  private final ProviderRegistry registry;

  @Inject
  public AgentController(ProviderRegistry registry) {
    this.registry = registry;
  }

  /**
   * Health check endpoint.
   *
   * @return agent status
   */
  @GetMapping("/status")
  public AgentStatusResponse status() {
    boolean hasSimpleAgent = registry.getAgent("simple").isPresent();
    int agentCount = registry.getAllAgents().size();

    return AgentStatusResponse.builder()
        .available(hasSimpleAgent)
        .agentCount(agentCount)
        .message(hasSimpleAgent ? "SimpleAgent is ready" : "No agents registered")
        .build();
  }

  /**
   * Execute agent query.
   *
   * @param request the question request
   * @return agent response
   */
  @PostMapping("/ask")
  public Mono<AgentResponse> ask(@RequestBody QuestionRequest request) {
    log.info("Received question: {}", request.getQuestion());

    // Get SimpleAgent from registry
    Agent agent = registry.<Agent>getAgent("simple")
        .orElseThrow(() -> new RuntimeException("SimpleAgent not found in registry"));

    // Execute agent with question
    return agent.execute(AgentRequest.of(request.getQuestion()))
        .map(result -> AgentResponse.builder()
            .answer(result.getAnswer())
            .model(result.getMetadata().getModel())
            .tokensUsed(result.getMetadata().getTotalTokens())
            .success(true)
            .build())
        .onErrorResume(error -> {
          log.error("Agent execution failed", error);
          return Mono.just(AgentResponse.builder()
              .success(false)
              .error(error.getMessage())
              .build());
        });
  }
}

// === Request/Response DTOs ===

@Data
class QuestionRequest {
  private String question;
}

@Data
@lombok.Builder
class AgentResponse {
  private boolean success;
  private String answer;
  private String model;
  private Integer tokensUsed;
  private String error;
}

@Data
@lombok.Builder
class AgentStatusResponse {
  private boolean available;
  private int agentCount;
  private String message;
}
