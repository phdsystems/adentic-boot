package dev.adeengineer.adentic.boot.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adeengineer.adentic.boot.annotations.Component;
import dev.adeengineer.adentic.boot.annotations.Service;
import dev.adeengineer.adentic.boot.annotations.provider.Evaluation;
import dev.adeengineer.adentic.boot.annotations.provider.Infrastructure;
import dev.adeengineer.adentic.boot.annotations.provider.LLM;
import dev.adeengineer.adentic.boot.annotations.provider.Memory;
import dev.adeengineer.adentic.boot.annotations.provider.Messaging;
import dev.adeengineer.adentic.boot.annotations.provider.Orchestration;
import dev.adeengineer.adentic.boot.annotations.provider.Queue;
import dev.adeengineer.adentic.boot.annotations.provider.Storage;
import dev.adeengineer.adentic.boot.annotations.provider.Tool;
import dev.adeengineer.adentic.boot.annotations.service.AgentService;
import dev.adeengineer.adentic.boot.annotations.service.DomainService;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ComponentScanner with all annotation types.
 *
 * <p>Tests component discovery for all 13 annotation types: core annotations (Component, Service),
 * provider annotations (9 types), and service annotations (DomainService, AgentService).
 */
@DisplayName("ComponentScanner Integration Tests")
class ComponentScannerTest {

  private ComponentScanner scanner;

  @BeforeEach
  void setUp() {
    scanner = new ComponentScanner("dev.adeengineer.adentic.boot.scanner");
  }

  @Test
  @DisplayName("Should scan all component types")
  void shouldScanAllComponentTypes() {
    Set<Class<?>> components = scanner.scan();

    // Verify all test components are found
    assertThat(components).isNotEmpty();
    assertThat(components).contains(TestComponent.class);
    assertThat(components).contains(TestService.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @LLM")
  void shouldScanLLMProviders() {
    Set<Class<?>> llmProviders = scanner.scanForAnnotation(LLM.class);

    assertThat(llmProviders).contains(TestOpenAIProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Infrastructure")
  void shouldScanInfrastructureProviders() {
    Set<Class<?>> infraProviders = scanner.scanForAnnotation(Infrastructure.class);

    assertThat(infraProviders).contains(TestDockerProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Storage")
  void shouldScanStorageProviders() {
    Set<Class<?>> storageProviders = scanner.scanForAnnotation(Storage.class);

    assertThat(storageProviders).contains(TestLocalStorageProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Messaging")
  void shouldScanMessagingProviders() {
    Set<Class<?>> messagingProviders = scanner.scanForAnnotation(Messaging.class);

    assertThat(messagingProviders).contains(TestKafkaProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Orchestration")
  void shouldScanOrchestrationProviders() {
    Set<Class<?>> orchestrationProviders = scanner.scanForAnnotation(Orchestration.class);

    assertThat(orchestrationProviders).contains(TestSimpleOrchestrator.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Memory")
  void shouldScanMemoryProviders() {
    Set<Class<?>> memoryProviders = scanner.scanForAnnotation(Memory.class);

    assertThat(memoryProviders).contains(TestInMemoryProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Queue")
  void shouldScanQueueProviders() {
    Set<Class<?>> queueProviders = scanner.scanForAnnotation(Queue.class);

    assertThat(queueProviders).contains(TestRedisQueueProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Tool")
  void shouldScanToolProviders() {
    Set<Class<?>> toolProviders = scanner.scanForAnnotation(Tool.class);

    assertThat(toolProviders).contains(TestSimpleToolProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @Evaluation")
  void shouldScanEvaluationProviders() {
    Set<Class<?>> evaluationProviders = scanner.scanForAnnotation(Evaluation.class);

    assertThat(evaluationProviders).contains(TestLLMEvaluator.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @DomainService")
  void shouldScanDomainServices() {
    Set<Class<?>> domainServices = scanner.scanForAnnotation(DomainService.class);

    assertThat(domainServices).contains(TestFinanceDomainService.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @AgentService")
  void shouldScanAgentServices() {
    Set<Class<?>> agentServices = scanner.scanForAnnotation(AgentService.class);

    assertThat(agentServices).contains(TestAgentRegistryService.class);
  }

  @Test
  @DisplayName("Should group providers by category")
  void shouldGroupProvidersByCategory() {
    Map<String, Set<Class<?>>> providers = scanner.scanProviders();

    assertThat(providers)
        .containsKeys(
            "llm",
            "infrastructure",
            "storage",
            "messaging",
            "orchestration",
            "memory",
            "queue",
            "tool",
            "evaluation");

    assertThat(providers.get("llm")).contains(TestOpenAIProvider.class);
    assertThat(providers.get("infrastructure")).contains(TestDockerProvider.class);
    assertThat(providers.get("storage")).contains(TestLocalStorageProvider.class);
    assertThat(providers.get("messaging")).contains(TestKafkaProvider.class);
    assertThat(providers.get("orchestration")).contains(TestSimpleOrchestrator.class);
    assertThat(providers.get("memory")).contains(TestInMemoryProvider.class);
    assertThat(providers.get("queue")).contains(TestRedisQueueProvider.class);
    assertThat(providers.get("tool")).contains(TestSimpleToolProvider.class);
    assertThat(providers.get("evaluation")).contains(TestLLMEvaluator.class);
  }

  // Test components
  @Component
  static class TestComponent {}

  @Service
  static class TestService {}

  @LLM(name = "openai", type = "text-generation")
  static class TestOpenAIProvider {}

  @Infrastructure(name = "docker", type = "container")
  static class TestDockerProvider {}

  @Storage(name = "local", type = "file-system")
  static class TestLocalStorageProvider {}

  @Messaging(name = "kafka", type = "distributed")
  static class TestKafkaProvider {}

  @Orchestration(name = "simple")
  static class TestSimpleOrchestrator {}

  @Memory(name = "in-memory", type = "short-term")
  static class TestInMemoryProvider {}

  @Queue(name = "redis", type = "distributed")
  static class TestRedisQueueProvider {}

  @Tool(name = "simple")
  static class TestSimpleToolProvider {}

  @Evaluation(name = "llm-evaluator")
  static class TestLLMEvaluator {}

  @DomainService(name = "finance-manager", domain = "finance")
  static class TestFinanceDomainService {}

  @AgentService(name = "agent-registry")
  static class TestAgentRegistryService {}
}
