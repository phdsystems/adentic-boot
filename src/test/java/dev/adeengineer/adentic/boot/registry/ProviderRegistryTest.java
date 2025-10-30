package dev.adeengineer.adentic.boot.registry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.adeengineer.adentic.boot.annotations.provider.LLM;
import dev.adeengineer.adentic.boot.annotations.provider.Messaging;
import dev.adeengineer.adentic.boot.annotations.provider.Queue;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
            "evaluation");
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

  // Test provider classes

  @LLM(name = "openai", type = "text-generation")
  static class TestOpenAIProvider {}

  @LLM(name = "anthropic", type = "text-generation")
  static class TestAnthropicProvider {}

  @Messaging(name = "kafka", type = "distributed")
  static class TestKafkaProvider {}

  @Queue(name = "redis", type = "distributed")
  static class TestRedisQueueProvider {}

  @Messaging
  static class TestProviderWithoutName {}
}
