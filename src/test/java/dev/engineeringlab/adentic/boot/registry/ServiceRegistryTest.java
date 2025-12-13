package dev.engineeringlab.adentic.boot.registry;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for ServiceRegistry.
 *
 * <p>Tests service registration, retrieval, and category management.
 */
@DisplayName("ServiceRegistry Tests")
class ServiceRegistryTest {

  private ServiceRegistry registry;

  @BeforeEach
  void setUp() {
    registry = new ServiceRegistry();
  }

  @Test
  @DisplayName("Should register service by category and name")
  void shouldRegisterService() {
    Object service = new TestService("openai");

    registry.register("llm", "openai", service);

    Optional<Object> retrieved = registry.get("llm", "openai");
    assertThat(retrieved).isPresent();
    assertThat(retrieved.get()).isSameAs(service);
  }

  @Test
  @DisplayName("Should return empty optional for non-existent service")
  void shouldReturnEmptyForNonExistentService() {
    Optional<Object> result = registry.get("llm", "nonexistent");

    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("Should get all services in category")
  void shouldGetServicesByCategory() {
    Object openai = new TestService("openai");
    Object anthropic = new TestService("anthropic");

    registry.register("llm", "openai", openai);
    registry.register("llm", "anthropic", anthropic);

    Map<String, Object> llmServices = registry.getAll("llm");

    assertThat(llmServices).hasSize(2);
    assertThat(llmServices.get("openai")).isSameAs(openai);
    assertThat(llmServices.get("anthropic")).isSameAs(anthropic);
  }

  @Test
  @DisplayName("Should return empty map for category with no services")
  void shouldReturnEmptyMapForEmptyCategory() {
    Map<String, Object> services = registry.getAll("nonexistent");

    assertThat(services).isEmpty();
  }

  @Test
  @DisplayName("Should get service count")
  void shouldGetServiceCount() {
    registry.register("llm", "openai", new TestService("openai"));
    registry.register("llm", "anthropic", new TestService("anthropic"));
    registry.register("messaging", "kafka", new TestService("kafka"));

    assertThat(registry.count("llm")).isEqualTo(2);
    assertThat(registry.count("messaging")).isEqualTo(1);
    assertThat(registry.count("storage")).isZero();
  }

  @Test
  @DisplayName("Should get total service count")
  void shouldGetTotalServiceCount() {
    registry.register("llm", "openai", new TestService("openai"));
    registry.register("messaging", "kafka", new TestService("kafka"));
    registry.register("queue", "redis", new TestService("redis"));

    assertThat(registry.totalCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("Should check if service exists")
  void shouldCheckServiceExists() {
    registry.register("llm", "openai", new TestService("openai"));

    assertThat(registry.has("llm", "openai")).isTrue();
    assertThat(registry.has("llm", "anthropic")).isFalse();
    assertThat(registry.has("messaging", "kafka")).isFalse();
  }

  @Test
  @DisplayName("Should create category dynamically on first registration")
  void shouldCreateCategoryDynamically() {
    assertThat(registry.getCategories()).isEmpty();

    registry.register("custom-category", "service1", new TestService("s1"));

    assertThat(registry.getCategories()).contains("custom-category");
    assertThat(registry.has("custom-category", "service1")).isTrue();
  }

  @Nested
  @DisplayName("Multiple Service Support Tests")
  class MultipleServiceTests {

    @Test
    @DisplayName("Should handle multiple services in same category")
    void shouldHandleMultipleServicesInCategory() {
      registry.register("llm", "openai", new TestService("openai"));
      registry.register("llm", "anthropic", new TestService("anthropic"));
      registry.register("llm", "cohere", new TestService("cohere"));

      assertThat(registry.count("llm")).isEqualTo(3);
      assertThat(registry.has("llm", "openai")).isTrue();
      assertThat(registry.has("llm", "anthropic")).isTrue();
      assertThat(registry.has("llm", "cohere")).isTrue();
    }

    @Test
    @DisplayName("Should replace service with same name")
    void shouldReplaceServiceWithSameName() {
      Object service1 = new TestService("s1");
      Object service2 = new TestService("s2");

      registry.register("llm", "test", service1);
      registry.register("llm", "test", service2);

      Optional<Object> retrieved = registry.get("llm", "test");
      assertThat(retrieved).isPresent();
      assertThat(retrieved.get()).isSameAs(service2);
      assertThat(registry.count("llm")).isEqualTo(1);
    }

    @Test
    @DisplayName("Should maintain service isolation across categories")
    void shouldMaintainServiceIsolationAcrossCategories() {
      Object llmService = new TestService("llm");
      Object messagingService = new TestService("messaging");

      registry.register("llm", "test", llmService);
      registry.register("messaging", "test", messagingService);

      Optional<Object> llmResult = registry.get("llm", "test");
      Optional<Object> messagingResult = registry.get("messaging", "test");

      assertThat(llmResult).isPresent();
      assertThat(messagingResult).isPresent();
      assertThat(llmResult.get()).isSameAs(llmService);
      assertThat(messagingResult.get()).isSameAs(messagingService);
      assertThat(llmResult.get()).isNotSameAs(messagingResult.get());
    }
  }

  @Nested
  @DisplayName("Clear Tests")
  class ClearTests {

    @Test
    @DisplayName("Should clear category")
    void shouldClearCategory() {
      registry.register("llm", "openai", new TestService("openai"));
      registry.register("llm", "anthropic", new TestService("anthropic"));

      assertThat(registry.count("llm")).isEqualTo(2);

      registry.clear("llm");

      assertThat(registry.count("llm")).isZero();
      assertThat(registry.getAll("llm")).isEmpty();
    }

    @Test
    @DisplayName("Should clear all categories")
    void shouldClearAllCategories() {
      registry.register("llm", "openai", new TestService("openai"));
      registry.register("messaging", "kafka", new TestService("kafka"));

      assertThat(registry.totalCount()).isEqualTo(2);

      registry.clearAll();

      assertThat(registry.totalCount()).isZero();
      assertThat(registry.getCategories()).isEmpty();
    }
  }

  static class TestService {
    private final String name;

    TestService(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "TestService{name='" + name + "'}";
    }
  }
}
