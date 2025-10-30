package dev.adeengineer.adentic.boot.scanner;

import static org.assertj.core.api.Assertions.assertThat;

import dev.adeengineer.adentic.boot.annotations.Component;
import dev.adeengineer.adentic.boot.annotations.Service;
import dev.adeengineer.annotation.provider.EvaluationProvider;
import dev.adeengineer.annotation.provider.InfrastructureProvider;
import dev.adeengineer.annotation.provider.MemoryProvider;
import dev.adeengineer.annotation.provider.MessageBrokerProvider;
import dev.adeengineer.annotation.provider.OrchestrationProvider;
import dev.adeengineer.annotation.provider.StorageProvider;
import dev.adeengineer.annotation.provider.TaskQueueProvider;
import dev.adeengineer.annotation.provider.TextGenerationProvider;
import dev.adeengineer.annotation.provider.ToolProvider;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ComponentScanner with all annotation types.
 *
 * <p>Tests component discovery for core annotations (Component, Service) and provider annotations
 * from adentic-annotation module.
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
  @DisplayName("Should scan specific annotation type - @TextGenerationProvider")
  void shouldScanTextGenerationProviders() {
    Set<Class<?>> providers = scanner.scanForAnnotation(TextGenerationProvider.class);

    assertThat(providers).contains(TestOpenAIProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @InfrastructureProvider")
  void shouldScanInfrastructureProviders() {
    Set<Class<?>> infraProviders = scanner.scanForAnnotation(InfrastructureProvider.class);

    assertThat(infraProviders).contains(TestDockerProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @StorageProvider")
  void shouldScanStorageProviders() {
    Set<Class<?>> storageProviders = scanner.scanForAnnotation(StorageProvider.class);

    assertThat(storageProviders).contains(TestLocalStorageProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @MessageBrokerProvider")
  void shouldScanMessagingProviders() {
    Set<Class<?>> messagingProviders = scanner.scanForAnnotation(MessageBrokerProvider.class);

    assertThat(messagingProviders).contains(TestKafkaProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @OrchestrationProvider")
  void shouldScanOrchestrationProviders() {
    Set<Class<?>> orchestrationProviders = scanner.scanForAnnotation(OrchestrationProvider.class);

    assertThat(orchestrationProviders).contains(TestSimpleOrchestrator.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @MemoryProvider")
  void shouldScanMemoryProviders() {
    Set<Class<?>> memoryProviders = scanner.scanForAnnotation(MemoryProvider.class);

    assertThat(memoryProviders).contains(TestInMemoryProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @TaskQueueProvider")
  void shouldScanQueueProviders() {
    Set<Class<?>> queueProviders = scanner.scanForAnnotation(TaskQueueProvider.class);

    assertThat(queueProviders).contains(TestRedisQueueProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @ToolProvider")
  void shouldScanToolProviders() {
    Set<Class<?>> toolProviders = scanner.scanForAnnotation(ToolProvider.class);

    assertThat(toolProviders).contains(TestSimpleToolProvider.class);
  }

  @Test
  @DisplayName("Should scan specific annotation type - @EvaluationProvider")
  void shouldScanEvaluationProviders() {
    Set<Class<?>> evaluationProviders = scanner.scanForAnnotation(EvaluationProvider.class);

    assertThat(evaluationProviders).contains(TestLLMEvaluator.class);
  }

  @Test
  @DisplayName("Should group providers by category")
  void shouldGroupProvidersByCategory() {
    Map<String, Set<Class<?>>> providers = scanner.scanProviders();

    assertThat(providers)
        .containsKeys(
            "text-generation",
            "infrastructure",
            "storage",
            "messaging",
            "orchestration",
            "memory",
            "queue",
            "tool",
            "evaluation",
            "web-search",
            "web-test",
            "database");

    assertThat(providers.get("text-generation")).contains(TestOpenAIProvider.class);
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

  @TextGenerationProvider(name = "openai", model = "gpt-4")
  static class TestOpenAIProvider {}

  @InfrastructureProvider(name = "docker")
  static class TestDockerProvider {}

  @StorageProvider(name = "local")
  static class TestLocalStorageProvider {}

  @MessageBrokerProvider(name = "kafka")
  static class TestKafkaProvider {}

  @OrchestrationProvider(name = "simple")
  static class TestSimpleOrchestrator {}

  @MemoryProvider(name = "in-memory")
  static class TestInMemoryProvider {}

  @TaskQueueProvider(name = "redis")
  static class TestRedisQueueProvider {}

  @ToolProvider(name = "simple")
  static class TestSimpleToolProvider {}

  @EvaluationProvider(name = "llm-evaluator")
  static class TestLLMEvaluator {}
}
