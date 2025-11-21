package dev.engineeringlab.adentic.tool.http.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for HTTP model classes. */
@DisplayName("HTTP Models Tests")
class HttpModelsTest {

  @Nested
  @DisplayName("HttpMethod Tests")
  class HttpMethodTests {

    @Test
    @DisplayName("Should have all standard HTTP methods")
    void testHttpMethods() {
      HttpMethod[] methods = HttpMethod.values();
      assertEquals(7, methods.length);
      assertNotNull(HttpMethod.valueOf("GET"));
      assertNotNull(HttpMethod.valueOf("POST"));
      assertNotNull(HttpMethod.valueOf("PUT"));
      assertNotNull(HttpMethod.valueOf("PATCH"));
      assertNotNull(HttpMethod.valueOf("DELETE"));
      assertNotNull(HttpMethod.valueOf("HEAD"));
      assertNotNull(HttpMethod.valueOf("OPTIONS"));
    }

    @Test
    @DisplayName("Should throw exception for invalid method")
    void testInvalidMethod() {
      assertThrows(IllegalArgumentException.class, () -> HttpMethod.valueOf("INVALID"));
    }

    @Test
    @DisplayName("Should return correct name")
    void testMethodName() {
      assertEquals("GET", HttpMethod.GET.name());
      assertEquals("POST", HttpMethod.POST.name());
    }
  }

  @Nested
  @DisplayName("HttpRequest Tests")
  class HttpRequestTests {

    @Test
    @DisplayName("Should create request with builder")
    void testBuilder() {
      Map<String, String> headers = Map.of("Authorization", "Bearer token");
      Map<String, String> queryParams = Map.of("key", "value");

      HttpRequest request =
          HttpRequest.builder()
              .method(HttpMethod.POST)
              .url("https://api.example.com")
              .headers(headers)
              .body("{\"data\": \"test\"}")
              .queryParams(queryParams)
              .timeoutMs(5000)
              .followRedirects(false)
              .contentType("application/json")
              .build();

      assertEquals(HttpMethod.POST, request.getMethod());
      assertEquals("https://api.example.com", request.getUrl());
      assertEquals(headers, request.getHeaders());
      assertEquals("{\"data\": \"test\"}", request.getBody());
      assertEquals(queryParams, request.getQueryParams());
      assertEquals(5000, request.getTimeoutMs());
      assertFalse(request.isFollowRedirects());
      assertEquals("application/json", request.getContentType());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      HttpRequest request =
          HttpRequest.builder().method(HttpMethod.GET).url("http://test.com").build();

      assertEquals(Map.of(), request.getHeaders());
      assertNull(request.getBody());
      assertEquals(Map.of(), request.getQueryParams());
      assertEquals(30000, request.getTimeoutMs());
      assertTrue(request.isFollowRedirects());
      assertEquals("application/json", request.getContentType());
    }

    @Test
    @DisplayName("Should create GET request")
    void testGetFactory() {
      HttpRequest request = HttpRequest.get("http://test.com");

      assertEquals(HttpMethod.GET, request.getMethod());
      assertEquals("http://test.com", request.getUrl());
    }

    @Test
    @DisplayName("Should create POST request")
    void testPostFactory() {
      HttpRequest request = HttpRequest.post("http://test.com", "{\"key\": \"value\"}");

      assertEquals(HttpMethod.POST, request.getMethod());
      assertEquals("http://test.com", request.getUrl());
      assertEquals("{\"key\": \"value\"}", request.getBody());
    }

    @Test
    @DisplayName("Should create PUT request")
    void testPutFactory() {
      HttpRequest request = HttpRequest.put("http://test.com", "{\"key\": \"value\"}");

      assertEquals(HttpMethod.PUT, request.getMethod());
      assertEquals("http://test.com", request.getUrl());
      assertEquals("{\"key\": \"value\"}", request.getBody());
    }

    @Test
    @DisplayName("Should create DELETE request")
    void testDeleteFactory() {
      HttpRequest request = HttpRequest.delete("http://test.com");

      assertEquals(HttpMethod.DELETE, request.getMethod());
      assertEquals("http://test.com", request.getUrl());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      HttpRequest request =
          HttpRequest.builder().method(HttpMethod.GET).url("http://test.com").build();

      request.setMethod(HttpMethod.POST);
      request.setUrl("http://new-url.com");
      request.setBody("new body");
      request.setTimeoutMs(10000);
      request.setFollowRedirects(false);

      assertEquals(HttpMethod.POST, request.getMethod());
      assertEquals("http://new-url.com", request.getUrl());
      assertEquals("new body", request.getBody());
      assertEquals(10000, request.getTimeoutMs());
      assertFalse(request.isFollowRedirects());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      HttpRequest request1 = HttpRequest.get("http://test.com");
      HttpRequest request2 = HttpRequest.get("http://test.com");

      assertEquals(request1, request2);
      assertEquals(request1.hashCode(), request2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      HttpRequest request = HttpRequest.get("http://test.com");
      String str = request.toString();

      assertTrue(str.contains("GET"));
      assertTrue(str.contains("http://test.com"));
    }
  }

  @Nested
  @DisplayName("HttpResponse Tests")
  class HttpResponseTests {

    @Test
    @DisplayName("Should create response with builder")
    void testBuilder() {
      Map<String, String> headers = Map.of("Content-Type", "application/json");

      HttpResponse response =
          HttpResponse.builder()
              .statusCode(200)
              .body("{\"result\": \"ok\"}")
              .headers(headers)
              .success(true)
              .errorMessage(null)
              .duration(Duration.ofMillis(150))
              .contentType("application/json")
              .contentLength(20)
              .build();

      assertEquals(200, response.getStatusCode());
      assertEquals("{\"result\": \"ok\"}", response.getBody());
      assertEquals(headers, response.getHeaders());
      assertTrue(response.isSuccess());
      assertNull(response.getErrorMessage());
      assertEquals(Duration.ofMillis(150), response.getDuration());
      assertEquals("application/json", response.getContentType());
      assertEquals(20, response.getContentLength());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      HttpResponse response = HttpResponse.builder().statusCode(200).success(true).build();

      assertNull(response.getBody());
      assertEquals(Map.of(), response.getHeaders());
      assertNull(response.getErrorMessage());
      assertEquals(Duration.ZERO, response.getDuration());
      assertNull(response.getContentType());
      assertEquals(0, response.getContentLength());
    }

    @Test
    @DisplayName("Should create success response")
    void testSuccessFactory() {
      Map<String, String> headers = Map.of("Content-Type", "text/html");
      HttpResponse response = HttpResponse.success(200, "<html>test</html>", headers);

      assertEquals(200, response.getStatusCode());
      assertEquals("<html>test</html>", response.getBody());
      assertEquals(headers, response.getHeaders());
      assertTrue(response.isSuccess());
      assertEquals("text/html", response.getContentType());
      assertEquals(17, response.getContentLength());
    }

    @Test
    @DisplayName("Should create success response with null body")
    void testSuccessFactoryNullBody() {
      Map<String, String> headers = Map.of();
      HttpResponse response = HttpResponse.success(204, null, headers);

      assertEquals(204, response.getStatusCode());
      assertNull(response.getBody());
      assertEquals(0, response.getContentLength());
      assertEquals("text/plain", response.getContentType()); // default when no Content-Type header
    }

    @Test
    @DisplayName("Should create error response with status code")
    void testErrorFactoryWithStatusCode() {
      HttpResponse response = HttpResponse.error(404, "Not Found");

      assertEquals(404, response.getStatusCode());
      assertFalse(response.isSuccess());
      assertEquals("Not Found", response.getErrorMessage());
    }

    @Test
    @DisplayName("Should create error response from exception")
    void testErrorFactoryWithException() {
      Exception ex = new RuntimeException("Connection timeout");
      HttpResponse response = HttpResponse.error(ex);

      assertEquals(0, response.getStatusCode());
      assertFalse(response.isSuccess());
      assertEquals("Connection timeout", response.getErrorMessage());
    }

    @Test
    @DisplayName("Should correctly identify 2xx success")
    void testIs2xxSuccess() {
      assertTrue(HttpResponse.builder().statusCode(200).success(true).build().is2xxSuccess());
      assertTrue(HttpResponse.builder().statusCode(201).success(true).build().is2xxSuccess());
      assertTrue(HttpResponse.builder().statusCode(204).success(true).build().is2xxSuccess());
      assertTrue(HttpResponse.builder().statusCode(299).success(true).build().is2xxSuccess());

      assertFalse(HttpResponse.builder().statusCode(199).success(false).build().is2xxSuccess());
      assertFalse(HttpResponse.builder().statusCode(300).success(false).build().is2xxSuccess());
      assertFalse(HttpResponse.builder().statusCode(404).success(false).build().is2xxSuccess());
    }

    @Test
    @DisplayName("Should correctly identify 4xx client errors")
    void testIs4xxClientError() {
      assertTrue(HttpResponse.builder().statusCode(400).success(false).build().is4xxClientError());
      assertTrue(HttpResponse.builder().statusCode(401).success(false).build().is4xxClientError());
      assertTrue(HttpResponse.builder().statusCode(404).success(false).build().is4xxClientError());
      assertTrue(HttpResponse.builder().statusCode(499).success(false).build().is4xxClientError());

      assertFalse(HttpResponse.builder().statusCode(399).success(false).build().is4xxClientError());
      assertFalse(HttpResponse.builder().statusCode(500).success(false).build().is4xxClientError());
      assertFalse(HttpResponse.builder().statusCode(200).success(true).build().is4xxClientError());
    }

    @Test
    @DisplayName("Should correctly identify 5xx server errors")
    void testIs5xxServerError() {
      assertTrue(HttpResponse.builder().statusCode(500).success(false).build().is5xxServerError());
      assertTrue(HttpResponse.builder().statusCode(502).success(false).build().is5xxServerError());
      assertTrue(HttpResponse.builder().statusCode(503).success(false).build().is5xxServerError());
      assertTrue(HttpResponse.builder().statusCode(599).success(false).build().is5xxServerError());

      assertFalse(HttpResponse.builder().statusCode(499).success(false).build().is5xxServerError());
      assertFalse(HttpResponse.builder().statusCode(600).success(false).build().is5xxServerError());
      assertFalse(HttpResponse.builder().statusCode(200).success(true).build().is5xxServerError());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      HttpResponse response = HttpResponse.builder().statusCode(200).success(true).build();

      response.setStatusCode(404);
      response.setBody("Not found");
      response.setSuccess(false);
      response.setErrorMessage("Resource not found");
      response.setDuration(Duration.ofMillis(100));
      response.setContentType("text/plain");
      response.setContentLength(100);

      assertEquals(404, response.getStatusCode());
      assertEquals("Not found", response.getBody());
      assertFalse(response.isSuccess());
      assertEquals("Resource not found", response.getErrorMessage());
      assertEquals(Duration.ofMillis(100), response.getDuration());
      assertEquals("text/plain", response.getContentType());
      assertEquals(100, response.getContentLength());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      HttpResponse response1 = HttpResponse.error(404, "Not Found");
      HttpResponse response2 = HttpResponse.error(404, "Not Found");

      assertEquals(response1, response2);
      assertEquals(response1.hashCode(), response2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      HttpResponse response = HttpResponse.success(200, "OK", Map.of());
      String str = response.toString();

      assertTrue(str.contains("200"));
      assertTrue(str.contains("true")); // success flag
    }
  }
}
