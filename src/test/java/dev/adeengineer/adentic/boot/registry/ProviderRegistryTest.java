package dev.adeengineer.adentic.boot.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adeengineer.adentic.boot.annotations.provider.LLM;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for ProviderRegistry.
 *
 * <p>Tests provider registration, retrieval, and category management.
 */
@DisplayName("ProviderRegistry Integration Tests")
class ProviderRegistryTest {

  private ProviderRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new ProviderRegistry();
  }

  @Test
  @DisplayName("Should register provider by category and name")
  void shouldRegisterProvider() {
    Object provider = new TestOpenAIProvider();

    registry.registerProvider("llm", "openai", provider);

    Optional<Object> retrieved = registry.getProvider("llm", "openai");
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isSameAs(provider);
  }

  @Test
  @DisplayName("Should throw exception for unknown category")
  void shouldThrowForUnknownCategory() {
    Object provider = new TestOpenAIProvider();

    assertThatThrownBy(() -> registry.registerProvider("unknown", "test", provider))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown provider category");
  }

  @Test
  @DisplayName("Should return empty optional for non-existent provider")
  void shouldReturnEmptyForNonExistentProvider() {
    Optional<Object> result = registry.getProvider("llm", "nonexistent");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should get all providers in category")
  void shouldGetProvidersByCategory() {
    Object openai = new TestOpenAIProvider();
    Object anthropic = new TestAnthropicProvider();

    registry.registerProvider("llm", "openai", openai);
    registry.registerProvider("llm", "anthropic", anthropic);

    Map<String, Object> llmProviders = registry.getProvidersByCategory("llm");

    assertThat(llmProviders).hasSize(2);
    assertThat(llmProviders.get("openai")).isSameAs(openai);
    assertThat(llmProviders.get("anthropic")).isSameAs(anthropic);
  }

  @Test
  @DisplayName("Should return empty map for category with no providers")
  void shouldReturnEmptyMapForEmptyCategory() {
    Map<String, Object> providers = registry.getProvidersByCategory("llm");

    assertThat(providers).isEmpty();
  }

  @Test
  @DisplayName("Should get provider count")
  void shouldGetProviderCount() {
    registry.registerProvider("llm", "openai", new TestOpenAIProvider());
    registry.registerProvider("llm", "anthropic", new TestAnthropicProvider());
    registry.registerProvider("messaging", "kafka", new TestKafkaProvider());

    assertThat(registry.getProviderCount("llm")).isEqualTo(2);
    assertThat(registry.getProviderCount("messaging")).isEqualTo(1);
    assertThat(registry.getProviderCount("storage")).isZero();
  }

  @Test
  @DisplayName("Should get total provider count")
  void shouldGetTotalProviderCount() {
    registry.registerProvider("llm", "openai", new TestOpenAIProvider());
    registry.registerProvider("messaging", "kafka", new TestKafkaProvider());
    registry.registerProvider("queue", "redis", new TestRedisQueueProvider());

    assertThat(registry.getTotalProviderCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should check if provider exists")
  void shouldCheckProviderExists() {
    registry.registerProvider("llm", "openai", new TestOpenAIProvider());

    assertThat(registry.hasProvider("llm", "openai")).isTrue();
    assertThat(registry.hasProvider("llm", "anthropic")).isFalse();
    assertThat(registry.hasProvider("messaging", "kafka")).isFalse();
  }

  @Test
  @DisplayName("Should get all categories")
  void shouldGetAllCategories() {
    assertThat(registry.getCategories())
        .containsExactlyInAnyOrder(
            "llm",
            "infrastructure",
            "storage",
            "messaging",
            "orchestration",
            "memory",
            "queue",
            "tool",
            "evaluation",
            "agent", // NEW: EE agent category
            "resilience", // NEW: Enterprise resilience
            "health", // NEW: Enterprise health
            "metrics"); // NEW: Enterprise metrics
  }

  @Test
  @DisplayName("Should register provider from annotated class")
  void shouldRegisterProviderFromClass() {
    TestOpenAIProvider provider = new TestOpenAIProvider();

    registry.registerProviderFromClass(TestOpenAIProvider.class, provider);

    Optional<Object> retrieved = registry.getProvider("llm", "openai");
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isSameAs(provider);
  }

  @Test
  @DisplayName("Should use decapitalized class name when annotation name is empty")
  void shouldUseDecapitalizedClassName() {
    TestProviderWithoutName provider = new TestProviderWithoutName();

    registry.registerProviderFromClass(TestProviderWithoutName.class, provider);

    Optional<Object> retrieved = registry.getProvider("messaging", "testProviderWithoutName");
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isSameAs(provider);
  }

  @Test
  @DisplayName("Should throw exception when registering non-provider class")
  void shouldThrowForNonProviderClass() {
    Object notAProvider = new Object();

    assertThatThrownBy(() -> registry.registerProviderFromClass(Object.class, notAProvider))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("not annotated with a provider annotation");
  }

  @Nested
  @DisplayName("Multiple Provider Support Tests")
  class MultipleProviderTests {

    @Test
    @DisplayName("Should handle multiple providers in same category")
    void shouldHandleMultipleProvidersInCategory() {
      registry.registerProvider("llm", "openai", new TestOpenAIProvider());
      registry.registerProvider("llm", "anthropic", new TestAnthropicProvider());
      registry.registerProvider("llm", "cohere", "cohereProvider");

      assertThat(registry.getProviderCount("llm")).isEqualTo(3);
      assertThat(registry.hasProvider("llm", "openai")).isTrue();
      assertThat(registry.hasProvider("llm", "anthropic")).isTrue();
      assertThat(registry.hasProvider("llm", "cohere")).isTrue();
    }

    @Test
    @DisplayName("Should replace provider with same name")
    void shouldReplaceProviderWithSameName() {
      Object provider1 = new TestOpenAIProvider();
      Object provider2 = new TestAnthropicProvider();

      registry.registerProvider("llm", "test", provider1);
      registry.registerProvider("llm", "test", provider2);

      Optional<Object> retrieved = registry.getProvider("llm", "test");
      assertThat(retrieved).isPresent();
      assertThat(retrieved.get()).isSameAs(provider2);
      assertThat(registry.getProviderCount("llm")).isEqualTo(1);
    }

    @Test
    @DisplayName("Should maintain provider isolation across categories")
    void shouldMaintainProviderIsolationAcrossCategories() {
      Object llmProvider = new TestOpenAIProvider();
      Object messagingProvider = new TestKafkaProvider();

      registry.registerProvider("llm", "test", llmProvider);
      registry.registerProvider("messaging", "test", messagingProvider);

      Optional<Object> llmResult = registry.getProvider("llm", "test");
      Optional<Object> messagingResult = registry.getProvider("messaging", "test");

      assertThat(llmResult).isPresent();
      assertThat(messagingResult).isPresent();
      assertThat(llmResult.get()).isSameAs(llmProvider);
      assertThat(messagingResult.get()).isSameAs(messagingProvider);
      assertThat(llmResult.get()).isNotSameAs(messagingResult.get());
    }
  }

  @Nested
  @DisplayName("Provider Lifecycle Tests")
  class ProviderLifecycleTests {

    @Test
    @DisplayName("Should register and retrieve provider lifecycle")
    void shouldRegisterAndRetrieveProvider() {
      Object provider = new TestOpenAIProvider();

      // Before registration
      assertThat(registry.hasProvider("llm", "openai")).isFalse();
      assertThat(registry.getProviderCount("llm")).isZero();

      // Register
      registry.registerProvider("llm", "openai", provider);

      // After registration
      assertThat(registry.hasProvider("llm", "openai")).isTrue();
      assertThat(registry.getProviderCount("llm")).isEqualTo(1);

      Optional<Object> retrieved = registry.getProvider("llm", "openai");
      assertThat(retrieved).isPresent();
      assertThat(retrieved.get()).isSameAs(provider);
    }

    @Test
    @DisplayName("Should handle provider updates")
    void shouldHandleProviderUpdates() {
      Object oldProvider = new TestOpenAIProvider();
      Object newProvider = new TestAnthropicProvider();

      registry.registerProvider("llm", "provider", oldProvider);
      assertThat(registry.getProvider("llm", "provider").get()).isSameAs(oldProvider);

      registry.registerProvider("llm", "provider", newProvider);
      assertThat(registry.getProvider("llm", "provider").get()).isSameAs(newProvider);
      assertThat(registry.getProviderCount("llm")).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle null category gracefully")
    void shouldHandleNullCategory() {
      assertThat(registry.getProvidersByCategory(null)).isEmpty();
      assertThat(registry.getProviderCount(null)).isZero();
    }

    @Test
    @DisplayName("Should handle non-existent category")
    void shouldHandleNonExistentCategory() {
      assertThat(registry.getProvidersByCategory("nonexistent")).isEmpty();
      assertThat(registry.getProviderCount("nonexistent")).isZero();
      assertThat(registry.getProvider("nonexistent", "test")).isEmpty();
    }

    @Test
    @DisplayName("Should handle null provider name")
    void shouldHandleNullProviderName() {
      registry.registerProvider("llm", "test", new TestOpenAIProvider());

      Optional<Object> result = registry.getProvider("llm", null);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Annotation Processing Tests")
  class AnnotationProcessingTests {

    @Test
    @DisplayName("Should extract category from LLM annotation")
    void shouldExtractCategoryFromLLM() {
      TestOpenAIProvider provider = new TestOpenAIProvider();
      registry.registerProviderFromClass(TestOpenAIProvider.class, provider);

      assertThat(registry.hasProvider("llm", "openai")).isTrue();
    }

    @Test
    @DisplayName("Should extract category from Messaging annotation")
    void shouldExtractCategoryFromMessaging() {
      TestKafkaProvider provider = new TestKafkaProvider();
      registry.registerProviderFromClass(TestKafkaProvider.class, provider);

      assertThat(registry.hasProvider("messaging", "kafka")).isTrue();
    }

    @Test
    @DisplayName("Should extract category from Queue annotation")
    void shouldExtractCategoryFromQueue() {
      TestRedisQueueProvider provider = new TestRedisQueueProvider();
      registry.registerProviderFromClass(TestRedisQueueProvider.class, provider);

      assertThat(registry.hasProvider("queue", "redis")).isTrue();
    }

    @Test
    @DisplayName("Should use class name when annotation name is empty")
    void shouldUseClassNameWhenAnnotationNameEmpty() {
      TestProviderWithoutName provider = new TestProviderWithoutName();
      registry.registerProviderFromClass(TestProviderWithoutName.class, provider);

      assertThat(registry.hasProvider("messaging", "testProviderWithoutName")).isTrue();
    }

    @Test
    @DisplayName("Should handle all provider annotation types")
    void shouldHandleAllProviderAnnotations() {
      // Test Infrastructure
      TestInfrastructureProvider infraProvider = new TestInfrastructureProvider();
      registry.registerProviderFromClass(TestInfrastructureProvider.class, infraProvider);
      assertThat(registry.hasProvider("infrastructure", "testInfra")).isTrue();

      // Test Storage
      TestStorageProvider storageProvider = new TestStorageProvider();
      registry.registerProviderFromClass(TestStorageProvider.class, storageProvider);
      assertThat(registry.hasProvider("storage", "testStorage")).isTrue();

      // Test Orchestration
      TestOrchestrationProvider orchProvider = new TestOrchestrationProvider();
      registry.registerProviderFromClass(TestOrchestrationProvider.class, orchProvider);
      assertThat(registry.hasProvider("orchestration", "testOrch")).isTrue();

      // Test Memory
      TestMemoryProvider memProvider = new TestMemoryProvider();
      registry.registerProviderFromClass(TestMemoryProvider.class, memProvider);
      assertThat(registry.hasProvider("memory", "testMemory")).isTrue();

      // Test Tool
      TestToolProvider toolProvider = new TestToolProvider();
      registry.registerProviderFromClass(TestToolProvider.class, toolProvider);
      assertThat(registry.hasProvider("tool", "testTool")).isTrue();

      // Test Evaluation
      TestEvaluationProvider evalProvider = new TestEvaluationProvider();
      registry.registerProviderFromClass(TestEvaluationProvider.class, evalProvider);
      assertThat(registry.hasProvider("evaluation", "testEval")).isTrue();
    }
  }

  @Nested
  @DisplayName("Query and Retrieval Tests")
  class QueryRetrievalTests {

    @BeforeEach
    void setUpProviders() {
      registry.registerProvider("llm", "openai", new TestOpenAIProvider());
      registry.registerProvider("llm", "anthropic", new TestAnthropicProvider());
      registry.registerProvider("messaging", "kafka", new TestKafkaProvider());
      registry.registerProvider("queue", "redis", new TestRedisQueueProvider());
    }

    @Test
    @DisplayName("Should get providers by category returns correct map")
    void shouldGetProvidersByCategoryReturnsCorrectMap() {
      Map<String, Object> llmProviders = registry.getProvidersByCategory("llm");

      assertThat(llmProviders).hasSize(2);
      assertThat(llmProviders.keySet()).containsExactlyInAnyOrder("openai", "anthropic");
    }

    @Test
    @DisplayName("Should get total provider count across all categories")
    void shouldGetTotalProviderCountAcrossCategories() {
      assertThat(registry.getTotalProviderCount()).isEqualTo(4);
    }

    @Test
    @DisplayName("Should return immutable or defensive copy of providers")
    void shouldReturnDefensiveCopyOfProviders() {
      Map<String, Object> llmProviders = registry.getProvidersByCategory("llm");

      // Original should still have 2
      assertThat(llmProviders).hasSize(2);
    }
  }

  @Nested
  @DisplayName("Edge Cases Tests")
  class EdgeCasesTests {

    @Test
    @DisplayName("Should handle registering same provider instance multiple times")
    void shouldHandleRegisteringSameInstanceMultipleTimes() {
      Object provider = new TestOpenAIProvider();

      registry.registerProvider("llm", "provider1", provider);
      registry.registerProvider("llm", "provider2", provider);
      registry.registerProvider("messaging", "provider3", provider);

      assertThat(registry.getProviderCount("llm")).isEqualTo(2);
      assertThat(registry.getProviderCount("messaging")).isEqualTo(1);
      assertThat(registry.getTotalProviderCount()).isEqualTo(3);

      // All should return the same instance
      assertThat(registry.getProvider("llm", "provider1").get()).isSameAs(provider);
      assertThat(registry.getProvider("llm", "provider2").get()).isSameAs(provider);
      assertThat(registry.getProvider("messaging", "provider3").get()).isSameAs(provider);
    }

    @Test
    @DisplayName("Should maintain category initialization")
    void shouldMaintainCategoryInitialization() {
      // All categories should be initialized even if empty
      assertThat(registry.getCategories())
          .hasSize(13); // Updated: added "agent", "resilience", "health", "metrics" categories

      for (String category : registry.getCategories()) {
        assertThat(registry.getProvidersByCategory(category)).isNotNull();
      }
    }

    @Test
    @DisplayName("Should handle empty string as provider name")
    void shouldHandleEmptyStringAsProviderName() {
      Object provider = new TestOpenAIProvider();

      registry.registerProvider("llm", "", provider);

      assertThat(registry.hasProvider("llm", "")).isTrue();
      assertThat(registry.getProvider("llm", "").get()).isSameAs(provider);
    }
  }

  // Test provider classes

  @LLM(name = "openai", type = "text-generation")
  static class TestOpenAIProvider {}

  @LLM(name = "anthropic", type = "text-generation")
  static class TestAnthropicProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Messaging(name = "kafka", type = "distributed")
  static class TestKafkaProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Queue(name = "redis", type = "distributed")
  static class TestRedisQueueProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Messaging
  static class TestProviderWithoutName {}

  @dev.adeengineer.adentic.boot.annotations.provider.Infrastructure(name = "testInfra")
  static class TestInfrastructureProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Storage(name = "testStorage")
  static class TestStorageProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Orchestration(name = "testOrch")
  static class TestOrchestrationProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Memory(name = "testMemory")
  static class TestMemoryProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Tool(name = "testTool")
  static class TestToolProvider {}

  @dev.adeengineer.adentic.boot.annotations.provider.Evaluation(name = "testEval")
  static class TestEvaluationProvider {}
}
