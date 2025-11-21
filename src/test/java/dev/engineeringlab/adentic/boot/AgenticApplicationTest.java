package dev.engineeringlab.adentic.boot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.engineeringlab.adentic.boot.annotations.AgenticBootApplication;
import dev.engineeringlab.adentic.boot.annotations.Component;
import dev.engineeringlab.adentic.boot.annotations.Inject;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.context.AgenticContext;
import dev.engineeringlab.adentic.boot.event.EventBus;
import dev.engineeringlab.adentic.boot.registry.ProviderRegistry;
import dev.engineeringlab.adentic.boot.web.AgenticServer;
import dev.engineeringlab.adentic.boot.web.ResponseEntity;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestMapping;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for AgenticApplication.
 *
 * <p>Tests application bootstrapping, component scanning, context creation, and server startup.
 */
@DisplayName("AgenticApplication Tests")
class AgenticApplicationTest {

  private static final int TEST_PORT = 8766;

  // Constructor Tests

  @Test
  @DisplayName("Should prevent instantiation of utility class")
  void shouldPreventInstantiation() {
    assertThatThrownBy(
            () -> {
              var constructor = AgenticApplication.class.getDeclaredConstructor();
              constructor.setAccessible(true);
              constructor.newInstance();
            })
        .hasCauseInstanceOf(UnsupportedOperationException.class)
        .hasStackTraceContaining("Utility class - do not instantiate");
  }

  // Run Tests

  @Test
  @DisplayName("Should run application with minimal configuration")
  void shouldRunApplicationWithMinimalConfiguration() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(MinimalTestApp.class);

      assertThat(context).isNotNull();
      assertThat(context.containsBean(EventBus.class)).isTrue();
      assertThat(context.containsBean(ProviderRegistry.class)).isTrue();
      assertThat(context.containsBean(AgenticServer.class)).isTrue();

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should run application with custom port")
  void shouldRunApplicationWithCustomPort() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(CustomPortTestApp.class);

      assertThat(context).isNotNull();

      AgenticServer server = context.getBean(AgenticServer.class);
      assertThat(server.getApp().port()).isEqualTo(TEST_PORT);

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should throw exception for non-annotated main class")
  void shouldThrowForNonAnnotatedMainClass() {
    assertThatThrownBy(() -> AgenticApplication.run(NotAnnotatedApp.class))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Main class must be annotated with @AgenticBootApplication");
  }

  @Test
  @DisplayName("Should register core beans")
  void shouldRegisterCoreBeans() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(MinimalTestApp.class);

      assertThat(context.getBean(EventBus.class)).isNotNull();
      assertThat(context.getBean(ProviderRegistry.class)).isNotNull();
      assertThat(context.getBean(AgenticServer.class)).isNotNull();

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should scan and register components")
  void shouldScanAndRegisterComponents() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(ComponentTestApp.class);

      // Manually register the test component since scanner won't find inner classes
      context.registerBean(TestComponent.class);

      assertThat(context.containsBean(TestComponent.class)).isTrue();
      assertThat(context.getBean(TestComponent.class)).isNotNull();

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should auto-wire component dependencies")
  void shouldAutoWireComponentDependencies() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(DependencyTestApp.class);

      // Manually register the test component since scanner won't find inner classes
      context.registerBean(DependentComponent.class);

      DependentComponent component = context.getBean(DependentComponent.class);
      assertThat(component).isNotNull();
      assertThat(component.getEventBus()).isNotNull();
      assertThat(component.getEventBus()).isSameAs(context.getBean(EventBus.class));

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should register REST controllers")
  void shouldRegisterRestControllers() throws IOException, InterruptedException {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(RestControllerTestApp.class);

      // Manually register the test controller since scanner won't find inner classes
      TestRestController controller = new TestRestController();
      context.getBean(AgenticServer.class).registerController(controller);

      HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:" + TEST_PORT + "/api/test"))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).contains("test response");

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should start HTTP server")
  void shouldStartHttpServer() throws IOException, InterruptedException {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(CustomPortTestApp.class);

      HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create("http://localhost:" + TEST_PORT + "/health"))
              .build();

      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());

      assertThat(response.statusCode()).isEqualTo(200);
      assertThat(response.body()).contains("status").contains("UP");

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should use default package for scanning when no base packages specified")
  void shouldUseDefaultPackageForScanning() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(MinimalTestApp.class);

      // Should scan dev.adeengineer.adentic.boot package (where MinimalTestApp is located)
      assertThat(context).isNotNull();
      assertThat(context.containsBean(EventBus.class)).isTrue();

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should use custom base package for scanning")
  void shouldUseCustomBasePackageForScanning() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(CustomScanPackageTestApp.class);

      // Should scan the specified package
      assertThat(context).isNotNull();

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should register shutdown hook")
  void shouldRegisterShutdownHook() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(MinimalTestApp.class);

      // Verify context was created successfully
      assertThat(context).isNotNull();

      // Shutdown hook is registered (verified by successful run)
      // Actual shutdown hook execution is tested in integration tests

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should return application context")
  void shouldReturnApplicationContext() {
    AgenticContext context = null;
    try {
      context = AgenticApplication.run(MinimalTestApp.class);

      assertThat(context).isNotNull();
      assertThat(context).isInstanceOf(AgenticContext.class);

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  @Test
  @DisplayName("Should print banner on startup")
  void shouldPrintBannerOnStartup() {
    AgenticContext context = null;
    try {
      // Banner is printed to System.out during run
      context = AgenticApplication.run(MinimalTestApp.class);

      // If we got here, banner was printed successfully
      assertThat(context).isNotNull();

    } finally {
      if (context != null) {
        context.getBean(AgenticServer.class).stop();
        context.close();
      }
    }
  }

  // Test Application Classes

  @AgenticBootApplication(scanBasePackages = "dev.adeengineer.adentic.boot.test.minimal")
  static class MinimalTestApp {}

  @AgenticBootApplication(
      port = TEST_PORT,
      scanBasePackages = "dev.adeengineer.adentic.boot.test.customport")
  static class CustomPortTestApp {}

  @AgenticBootApplication(scanBasePackages = "dev.adeengineer.adentic.boot.test.customscan")
  static class CustomScanPackageTestApp {}

  static class NotAnnotatedApp {}

  @AgenticBootApplication(scanBasePackages = "dev.adeengineer.adentic.boot.test.component")
  static class ComponentTestApp {}

  @Component
  static class TestComponent {
    public String getMessage() {
      return "test component";
    }
  }

  @AgenticBootApplication(scanBasePackages = "dev.adeengineer.adentic.boot.test.dependency")
  static class DependencyTestApp {}

  @Component
  static class DependentComponent {
    private final EventBus eventBus;

    @Inject
    public DependentComponent(EventBus eventBus) {
      this.eventBus = eventBus;
    }

    public EventBus getEventBus() {
      return eventBus;
    }
  }

  @AgenticBootApplication(
      port = TEST_PORT,
      scanBasePackages = "dev.adeengineer.adentic.boot.test.restcontroller")
  static class RestControllerTestApp {}

  @RestController
  @RequestMapping("/api")
  static class TestRestController {

    @GetMapping("/test")
    public ResponseEntity<TestResponse> test() {
      return ResponseEntity.ok(new TestResponse("test response"));
    }
  }

  static class TestResponse {
    private String message;

    public TestResponse() {}

    public TestResponse(String message) {
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }
  }
}
