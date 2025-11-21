package dev.engineeringlab.adentic.boot.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.engineeringlab.adentic.boot.annotations.RestController;
import dev.engineeringlab.adentic.boot.web.annotations.GetMapping;
import dev.engineeringlab.adentic.boot.web.annotations.PathVariable;
import dev.engineeringlab.adentic.boot.web.annotations.PostMapping;
import dev.engineeringlab.adentic.boot.web.annotations.RequestBody;
import dev.engineeringlab.adentic.boot.web.annotations.RequestMapping;
import io.javalin.Javalin;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for AgenticServer.
 *
 * <p>Tests REST controller registration, HTTP routing, parameter binding, and JSON serialization.
 */
@DisplayName("AgenticServer Tests")
class AgenticServerTest {

  private AgenticServer server;
  private HttpClient httpClient;
  private ObjectMapper objectMapper;
  private static final int TEST_PORT = 8765;
  private static final String BASE_URL = "http://localhost:" + TEST_PORT;

  @BeforeEach
  void setUp() {
    server = new AgenticServer();
    httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    objectMapper = new ObjectMapper();
  }

  @AfterEach
  void tearDown() {
    if (server != null) {
      server.close();
    }
  }

  // Constructor Tests

  @Test
  @DisplayName("Should create server with default configuration")
  void shouldCreateServerWithDefaultConfiguration() {
    assertThat(server).isNotNull();
    assertThat(server.getApp()).isNotNull();
  }

  @Test
  @DisplayName("Should have health check endpoint")
  void shouldHaveHealthCheckEndpoint() throws Exception {
    server.start(TEST_PORT);

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/health")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("status").contains("UP");
  }

  // RegisterController Tests

  @Test
  @DisplayName("Should register REST controller")
  void shouldRegisterRestController() {
    TestController controller = new TestController();

    server.registerController(controller);
    server.start(TEST_PORT);

    // Verify controller is registered (controller can handle requests)
    assertThat(server).isNotNull();
  }

  @Test
  @DisplayName("Should skip non-RestController classes")
  void shouldSkipNonRestControllerClasses() {
    NotAController notController = new NotAController();

    // Should not throw exception, just log warning
    server.registerController(notController);
    server.start(TEST_PORT);

    assertThat(server).isNotNull();
  }

  @Test
  @DisplayName("Should register GET mapping")
  void shouldRegisterGetMapping() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/test/hello")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("Hello, World!");
  }

  @Test
  @DisplayName("Should register POST mapping")
  void shouldRegisterPostMapping() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    TestRequest requestBody = new TestRequest("test-data");
    String jsonBody = objectMapper.writeValueAsString(requestBody);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/test/echo"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("test-data");
  }

  @Test
  @DisplayName("Should handle path variables")
  void shouldHandlePathVariables() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/test/user/123")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("123");
  }

  @Test
  @DisplayName("Should handle ResponseEntity return type")
  void shouldHandleResponseEntityReturnType() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/test/created")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(201);
    assertThat(response.body()).contains("resource created");
  }

  @Test
  @DisplayName("Should handle ResponseEntity without body")
  void shouldHandleResponseEntityWithoutBody() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/test/not-found")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(404);
  }

  @Test
  @DisplayName("Should build path from base and method paths")
  void shouldBuildPathFromBaseAndMethodPaths() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/test/hello")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
  }

  @Test
  @DisplayName("Should handle controller without base path")
  void shouldHandleControllerWithoutBasePath() throws Exception {
    NoBasePathController controller = new NoBasePathController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/ping")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    assertThat(response.statusCode()).isEqualTo(200);
    assertThat(response.body()).contains("pong");
  }

  @Test
  @DisplayName("Should deserialize request body")
  void shouldDeserializeRequestBody() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    TestRequest requestBody = new TestRequest("deserialize-test");
    String jsonBody = objectMapper.writeValueAsString(requestBody);

    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/test/echo"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    TestResponse responseObj = objectMapper.readValue(response.body(), TestResponse.class);

    assertThat(responseObj.getMessage()).isEqualTo("deserialize-test");
  }

  @Test
  @DisplayName("Should serialize response as JSON")
  void shouldSerializeResponseAsJson() throws Exception {
    TestController controller = new TestController();
    server.registerController(controller);
    server.start(TEST_PORT);

    HttpRequest request =
        HttpRequest.newBuilder().uri(URI.create(BASE_URL + "/test/hello")).build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

    TestResponse responseObj = objectMapper.readValue(response.body(), TestResponse.class);

    assertThat(responseObj.getMessage()).isEqualTo("Hello, World!");
  }

  // Start and Stop Tests

  @Test
  @DisplayName("Should start server on specified port")
  void shouldStartServerOnSpecifiedPort() {
    server.start(TEST_PORT);

    assertThat(server.getApp().port()).isEqualTo(TEST_PORT);
  }

  @Test
  @DisplayName("Should stop server")
  void shouldStopServer() {
    server.start(TEST_PORT);
    server.stop();

    // Server should be stopped (no exception thrown)
    assertThat(server).isNotNull();
  }

  // GetApp Tests

  @Test
  @DisplayName("Should get underlying Javalin app")
  void shouldGetUnderlyingJavalinApp() {
    Javalin app = server.getApp();

    assertThat(app).isNotNull();
    assertThat(app).isInstanceOf(Javalin.class);
  }

  // Close Tests

  @Test
  @DisplayName("Should close server")
  void shouldCloseServer() {
    server.start(TEST_PORT);
    server.close();

    // Server should be closed (no exception thrown)
    assertThat(server).isNotNull();
  }

  // Test Controllers

  @RestController
  @RequestMapping("/test")
  static class TestController {

    @GetMapping("/hello")
    public TestResponse hello() {
      return new TestResponse("Hello, World!");
    }

    @PostMapping("/echo")
    public TestResponse echo(@RequestBody TestRequest request) {
      return new TestResponse(request.getData());
    }

    @GetMapping("/user/{id}")
    public TestResponse getUserById(@PathVariable("id") String id) {
      return new TestResponse("User ID: " + id);
    }

    @GetMapping("/created")
    public ResponseEntity<TestResponse> created() {
      return ResponseEntity.created().body(new TestResponse("resource created"));
    }

    @GetMapping("/not-found")
    public ResponseEntity<Void> notFound() {
      return ResponseEntity.notFound().build();
    }
  }

  @RestController
  static class NoBasePathController {

    @GetMapping("/ping")
    public TestResponse ping() {
      return new TestResponse("pong");
    }
  }

  static class NotAController {
    public String someMethod() {
      return "not a controller";
    }
  }

  // Test Data Classes

  static class TestRequest {
    private String data;

    public TestRequest() {}

    public TestRequest(String data) {
      this.data = data;
    }

    public String getData() {
      return data;
    }

    public void setData(String data) {
      this.data = data;
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
