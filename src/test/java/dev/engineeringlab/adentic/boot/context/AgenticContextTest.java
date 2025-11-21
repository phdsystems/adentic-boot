package dev.engineeringlab.adentic.boot.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.engineeringlab.adentic.boot.annotations.Inject;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for AgenticContext.
 *
 * <p>Tests dependency injection, bean registration, factory methods, and circular dependency
 * detection.
 */
@DisplayName("AgenticContext Tests")
class AgenticContextTest {

  private AgenticContext context;

  @BeforeEach
  void setUp() {
    context = new AgenticContext();
  }

  // RegisterSingleton Tests (by type)

  @Test
  @DisplayName("Should register singleton by type")
  void shouldRegisterSingletonByType() {
    TestService service = new TestService("test");

    context.registerSingleton(TestService.class, service);

    assertThat(context.containsBean(TestService.class)).isTrue();
  }

  @Test
  @DisplayName("Should retrieve registered singleton by type")
  void shouldRetrieveRegisteredSingletonByType() {
    TestService service = new TestService("test");
    context.registerSingleton(TestService.class, service);

    TestService retrieved = context.getBean(TestService.class);

    assertThat(retrieved).isSameAs(service);
  }

  @Test
  @DisplayName("Should overwrite existing singleton when registering same type")
  void shouldOverwriteExistingSingleton() {
    TestService service1 = new TestService("first");
    TestService service2 = new TestService("second");

    context.registerSingleton(TestService.class, service1);
    context.registerSingleton(TestService.class, service2);

    TestService retrieved = context.getBean(TestService.class);

    assertThat(retrieved).isSameAs(service2);
    assertThat(retrieved.getName()).isEqualTo("second");
  }

  // RegisterSingleton Tests (by name)

  @Test
  @DisplayName("Should register named singleton")
  void shouldRegisterNamedSingleton() {
    TestService service = new TestService("named");

    context.registerSingleton("myService", service);

    assertThat(context.containsBean("myService")).isTrue();
  }

  @Test
  @DisplayName("Should retrieve named singleton")
  void shouldRetrieveNamedSingleton() {
    TestService service = new TestService("named");
    context.registerSingleton("myService", service);

    TestService retrieved = context.getBean("myService", TestService.class);

    assertThat(retrieved).isSameAs(service);
  }

  @Test
  @DisplayName("Should overwrite existing named bean")
  void shouldOverwriteExistingNamedBean() {
    TestService service1 = new TestService("first");
    TestService service2 = new TestService("second");

    context.registerSingleton("myService", service1);
    context.registerSingleton("myService", service2);

    TestService retrieved = context.getBean("myService", TestService.class);

    assertThat(retrieved).isSameAs(service2);
  }

  @Test
  @DisplayName("Should throw exception for non-existent named bean")
  void shouldThrowForNonExistentNamedBean() {
    assertThatThrownBy(() -> context.getBean("nonexistent", TestService.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No bean found with name: nonexistent");
  }

  // RegisterFactory Tests

  @Test
  @DisplayName("Should register factory")
  void shouldRegisterFactory() {
    Supplier<TestService> factory = () -> new TestService("factory");

    context.registerFactory(TestService.class, factory);

    assertThat(context.containsBean(TestService.class)).isTrue();
  }

  @Test
  @DisplayName("Should lazily instantiate bean from factory")
  void shouldLazilyInstantiateBeanFromFactory() {
    context.registerFactory(TestService.class, () -> new TestService("lazy"));

    // Bean not created yet
    TestService bean = context.getBean(TestService.class);

    assertThat(bean).isNotNull();
    assertThat(bean.getName()).isEqualTo("lazy");
  }

  @Test
  @DisplayName("Should cache bean created from factory")
  void shouldCacheBeanCreatedFromFactory() {
    context.registerFactory(TestService.class, () -> new TestService("cached"));

    TestService bean1 = context.getBean(TestService.class);
    TestService bean2 = context.getBean(TestService.class);

    assertThat(bean1).isSameAs(bean2);
  }

  // RegisterBean Tests

  @Test
  @DisplayName("Should register bean class with no-arg constructor")
  void shouldRegisterBeanClassWithNoArgConstructor() {
    context.registerBean(NoArgConstructorBean.class);

    NoArgConstructorBean bean = context.getBean(NoArgConstructorBean.class);

    assertThat(bean).isNotNull();
  }

  @Test
  @DisplayName("Should register bean class with @Inject constructor")
  void shouldRegisterBeanClassWithInjectConstructor() {
    context.registerSingleton(TestService.class, new TestService("dependency"));
    context.registerBean(InjectConstructorBean.class);

    InjectConstructorBean bean = context.getBean(InjectConstructorBean.class);

    assertThat(bean).isNotNull();
    assertThat(bean.getService()).isNotNull();
    assertThat(bean.getService().getName()).isEqualTo("dependency");
  }

  @Test
  @DisplayName("Should auto-wire dependencies with constructor injection")
  void shouldAutoWireDependencies() {
    TestService service = new TestService("autowired");
    context.registerSingleton(TestService.class, service);
    context.registerBean(ServiceConsumer.class);

    ServiceConsumer consumer = context.getBean(ServiceConsumer.class);

    assertThat(consumer).isNotNull();
    assertThat(consumer.getService()).isSameAs(service);
  }

  @Test
  @DisplayName("Should resolve transitive dependencies")
  void shouldResolveTransitiveDependencies() {
    context.registerBean(TestService.class);
    context.registerBean(ServiceConsumer.class);
    context.registerBean(TransitiveConsumer.class);

    TransitiveConsumer consumer = context.getBean(TransitiveConsumer.class);

    assertThat(consumer).isNotNull();
    assertThat(consumer.getConsumer()).isNotNull();
    assertThat(consumer.getConsumer().getService()).isNotNull();
  }

  // GetBean Tests

  @Test
  @DisplayName("Should throw exception for non-existent bean")
  void shouldThrowForNonExistentBean() {
    assertThatThrownBy(() -> context.getBean(TestService.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No bean found for type");
  }

  @Test
  @DisplayName("Should retrieve singleton before factory")
  void shouldRetrieveSingletonBeforeFactory() {
    TestService singleton = new TestService("singleton");
    context.registerSingleton(TestService.class, singleton);
    context.registerFactory(TestService.class, () -> new TestService("factory"));

    TestService bean = context.getBean(TestService.class);

    assertThat(bean).isSameAs(singleton);
    assertThat(bean.getName()).isEqualTo("singleton");
  }

  // ContainsBean Tests

  @Test
  @DisplayName("Should check if bean exists by type")
  void shouldCheckIfBeanExistsByType() {
    assertThat(context.containsBean(TestService.class)).isFalse();

    context.registerSingleton(TestService.class, new TestService("test"));

    assertThat(context.containsBean(TestService.class)).isTrue();
  }

  @Test
  @DisplayName("Should check if bean exists by name")
  void shouldCheckIfBeanExistsByName() {
    assertThat(context.containsBean("myService")).isFalse();

    context.registerSingleton("myService", new TestService("test"));

    assertThat(context.containsBean("myService")).isTrue();
  }

  @Test
  @DisplayName("Should detect factory-registered beans")
  void shouldDetectFactoryRegisteredBeans() {
    context.registerFactory(TestService.class, () -> new TestService("factory"));

    assertThat(context.containsBean(TestService.class)).isTrue();
  }

  // Circular Dependency Detection Tests

  @Test
  @DisplayName("Should detect circular dependency between two beans")
  void shouldDetectCircularDependency() {
    context.registerBean(CircularA.class);
    context.registerBean(CircularB.class);

    assertThatThrownBy(() -> context.getBean(CircularA.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to instantiate bean")
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasRootCauseMessage("Circular dependency detected: CircularA -> CircularB -> CircularA");
  }

  @Test
  @DisplayName("Should detect circular dependency in chain")
  void shouldDetectCircularDependencyInChain() {
    context.registerBean(CircularX.class);
    context.registerBean(CircularY.class);
    context.registerBean(CircularZ.class);

    assertThatThrownBy(() -> context.getBean(CircularX.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to instantiate bean")
        .hasCauseInstanceOf(IllegalStateException.class)
        .hasRootCauseMessage(
            "Circular dependency detected: CircularX -> CircularY -> CircularZ -> CircularX");
  }

  // Constructor Resolution Tests

  @Test
  @DisplayName("Should use @Inject constructor when present")
  void shouldUseInjectConstructor() {
    context.registerSingleton(TestService.class, new TestService("injected"));
    context.registerBean(MultipleConstructorsBean.class);

    MultipleConstructorsBean bean = context.getBean(MultipleConstructorsBean.class);

    assertThat(bean).isNotNull();
    assertThat(bean.getService()).isNotNull();
    assertThat(bean.isInjectedConstructorUsed()).isTrue();
  }

  @Test
  @DisplayName("Should use single public constructor when no @Inject")
  void shouldUseSinglePublicConstructor() {
    context.registerSingleton(TestService.class, new TestService("single"));
    context.registerBean(SingleConstructorBean.class);

    SingleConstructorBean bean = context.getBean(SingleConstructorBean.class);

    assertThat(bean).isNotNull();
    assertThat(bean.getService()).isNotNull();
  }

  @Test
  @DisplayName("Should throw exception for bean with no suitable constructor")
  void shouldThrowForNoSuitableConstructor() {
    context.registerBean(NoSuitableConstructorBean.class);

    assertThatThrownBy(() -> context.getBean(NoSuitableConstructorBean.class))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Failed to instantiate bean");
  }

  // Close Tests

  @Test
  @DisplayName("Should close and clear all beans")
  void shouldCloseAndClearAllBeans() {
    context.registerSingleton(TestService.class, new TestService("test"));
    context.registerSingleton("named", new TestService("named"));
    context.registerFactory(NoArgConstructorBean.class, NoArgConstructorBean::new);

    context.close();

    assertThat(context.containsBean(TestService.class)).isFalse();
    assertThat(context.containsBean("named")).isFalse();
    assertThat(context.containsBean(NoArgConstructorBean.class)).isFalse();
  }

  // Test Classes

  static class TestService {
    private final String name;

    public TestService() {
      this.name = "default";
    }

    public TestService(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  static class NoArgConstructorBean {
    public NoArgConstructorBean() {}
  }

  static class InjectConstructorBean {
    private final TestService service;

    @Inject
    public InjectConstructorBean(TestService service) {
      this.service = service;
    }

    public TestService getService() {
      return service;
    }
  }

  static class ServiceConsumer {
    private final TestService service;

    public ServiceConsumer(TestService service) {
      this.service = service;
    }

    public TestService getService() {
      return service;
    }
  }

  static class TransitiveConsumer {
    private final ServiceConsumer consumer;

    public TransitiveConsumer(ServiceConsumer consumer) {
      this.consumer = consumer;
    }

    public ServiceConsumer getConsumer() {
      return consumer;
    }
  }

  static class CircularA {
    @Inject
    public CircularA(CircularB b) {}
  }

  static class CircularB {
    @Inject
    public CircularB(CircularA a) {}
  }

  static class CircularX {
    @Inject
    public CircularX(CircularY y) {}
  }

  static class CircularY {
    @Inject
    public CircularY(CircularZ z) {}
  }

  static class CircularZ {
    @Inject
    public CircularZ(CircularX x) {}
  }

  static class MultipleConstructorsBean {
    private final TestService service;
    private final boolean injectedConstructorUsed;

    public MultipleConstructorsBean() {
      this.service = null;
      this.injectedConstructorUsed = false;
    }

    @Inject
    public MultipleConstructorsBean(TestService service) {
      this.service = service;
      this.injectedConstructorUsed = true;
    }

    public TestService getService() {
      return service;
    }

    public boolean isInjectedConstructorUsed() {
      return injectedConstructorUsed;
    }
  }

  static class SingleConstructorBean {
    private final TestService service;

    public SingleConstructorBean(TestService service) {
      this.service = service;
    }

    public TestService getService() {
      return service;
    }
  }

  static class NoSuitableConstructorBean {
    private NoSuitableConstructorBean(String param1, int param2) {
      // Private constructor with unresolvable parameters
    }

    public NoSuitableConstructorBean(TestService service, String param) {
      // Multiple constructors without @Inject
    }
  }
}
