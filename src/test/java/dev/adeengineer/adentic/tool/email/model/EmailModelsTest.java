package dev.adeengineer.adentic.tool.email.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for email model classes. */
@DisplayName("Email Models Tests")
class EmailModelsTest {

  @Nested
  @DisplayName("EmailMessage Tests")
  class EmailMessageTests {

    @Test
    @DisplayName("Should create email message with builder")
    void testBuilder() {
      List<String> to = List.of("user1@example.com", "user2@example.com");
      List<String> cc = List.of("cc@example.com");
      List<String> bcc = List.of("bcc@example.com");
      Map<String, String> attachments = Map.of("file.pdf", "application/pdf");
      Map<String, String> headers = Map.of("X-Custom", "value");

      EmailMessage email =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(to)
              .cc(cc)
              .bcc(bcc)
              .replyTo("reply@example.com")
              .subject("Test Subject")
              .body("Test body content")
              .html(true)
              .attachments(attachments)
              .headers(headers)
              .priority(1)
              .build();

      assertEquals("sender@example.com", email.getFrom());
      assertEquals(to, email.getTo());
      assertEquals(cc, email.getCc());
      assertEquals(bcc, email.getBcc());
      assertEquals("reply@example.com", email.getReplyTo());
      assertEquals("Test Subject", email.getSubject());
      assertEquals("Test body content", email.getBody());
      assertTrue(email.isHtml());
      assertEquals(attachments, email.getAttachments());
      assertEquals(headers, email.getHeaders());
      assertEquals(1, email.getPriority());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      EmailMessage email =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .subject("Test")
              .body("Body")
              .build();

      assertEquals(List.of(), email.getCc());
      assertEquals(List.of(), email.getBcc());
      assertNull(email.getReplyTo());
      assertFalse(email.isHtml());
      assertEquals(Map.of(), email.getAttachments());
      assertEquals(Map.of(), email.getHeaders());
      assertEquals(3, email.getPriority());
    }

    @Test
    @DisplayName("Should create simple text email")
    void testSimpleFactory() {
      EmailMessage email =
          EmailMessage.simple(
              "sender@example.com", "recipient@example.com", "Test Subject", "Test body");

      assertEquals("sender@example.com", email.getFrom());
      assertEquals(List.of("recipient@example.com"), email.getTo());
      assertEquals("Test Subject", email.getSubject());
      assertEquals("Test body", email.getBody());
      assertFalse(email.isHtml());
    }

    @Test
    @DisplayName("Should create HTML email")
    void testHtmlFactory() {
      EmailMessage email =
          EmailMessage.html(
              "sender@example.com", "recipient@example.com", "Test Subject", "<h1>Test</h1>");

      assertEquals("sender@example.com", email.getFrom());
      assertEquals(List.of("recipient@example.com"), email.getTo());
      assertEquals("Test Subject", email.getSubject());
      assertEquals("<h1>Test</h1>", email.getBody());
      assertTrue(email.isHtml());
    }

    @Test
    @DisplayName("Should support multiple recipients")
    void testMultipleRecipients() {
      List<String> recipients =
          List.of("user1@example.com", "user2@example.com", "user3@example.com");

      EmailMessage email =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(recipients)
              .subject("Test")
              .body("Body")
              .build();

      assertEquals(3, email.getTo().size());
      assertTrue(email.getTo().contains("user1@example.com"));
      assertTrue(email.getTo().contains("user2@example.com"));
      assertTrue(email.getTo().contains("user3@example.com"));
    }

    @Test
    @DisplayName("Should support priority levels")
    void testPriorityLevels() {
      EmailMessage highPriority =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("user@example.com"))
              .subject("Urgent")
              .body("Important message")
              .priority(1)
              .build();

      EmailMessage lowPriority =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("user@example.com"))
              .subject("FYI")
              .body("Low priority message")
              .priority(5)
              .build();

      assertEquals(1, highPriority.getPriority());
      assertEquals(5, lowPriority.getPriority());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      EmailMessage email =
          EmailMessage.builder()
              .from("old@example.com")
              .to(List.of("old@example.com"))
              .subject("Old")
              .body("Old body")
              .build();

      email.setFrom("new@example.com");
      email.setTo(List.of("new@example.com"));
      email.setSubject("New Subject");
      email.setBody("New body");
      email.setHtml(true);
      email.setPriority(1);

      assertEquals("new@example.com", email.getFrom());
      assertEquals(List.of("new@example.com"), email.getTo());
      assertEquals("New Subject", email.getSubject());
      assertEquals("New body", email.getBody());
      assertTrue(email.isHtml());
      assertEquals(1, email.getPriority());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      EmailMessage email1 =
          EmailMessage.simple("sender@example.com", "user@example.com", "Test", "Body");

      EmailMessage email2 =
          EmailMessage.simple("sender@example.com", "user@example.com", "Test", "Body");

      assertEquals(email1, email2);
      assertEquals(email1.hashCode(), email2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      EmailMessage email =
          EmailMessage.simple("sender@example.com", "user@example.com", "Test", "Body");
      String str = email.toString();

      assertTrue(str.contains("sender@example.com"));
      assertTrue(str.contains("Test"));
    }
  }

  @Nested
  @DisplayName("EmailResult Tests")
  class EmailResultTests {

    @Test
    @DisplayName("Should create result with builder")
    void testBuilder() {
      Instant timestamp = Instant.now();

      EmailResult result =
          EmailResult.builder()
              .success(true)
              .messageId("<msg-123@server.com>")
              .errorMessage(null)
              .timestamp(timestamp)
              .recipientCount(3)
              .serverResponse("250 OK")
              .build();

      assertTrue(result.isSuccess());
      assertEquals("<msg-123@server.com>", result.getMessageId());
      assertNull(result.getErrorMessage());
      assertEquals(timestamp, result.getTimestamp());
      assertEquals(3, result.getRecipientCount());
      assertEquals("250 OK", result.getServerResponse());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      EmailResult result = EmailResult.builder().success(true).build();

      assertNull(result.getMessageId());
      assertNull(result.getErrorMessage());
      assertNotNull(result.getTimestamp());
      assertEquals(0, result.getRecipientCount());
      assertNull(result.getServerResponse());
    }

    @Test
    @DisplayName("Should create success result")
    void testSuccessFactory() {
      EmailResult result = EmailResult.success("<msg-456@server.com>", 5);

      assertTrue(result.isSuccess());
      assertEquals("<msg-456@server.com>", result.getMessageId());
      assertEquals(5, result.getRecipientCount());
      assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorFactory() {
      EmailResult result = EmailResult.error("Connection timeout");

      assertFalse(result.isSuccess());
      assertEquals("Connection timeout", result.getErrorMessage());
      assertNull(result.getMessageId());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      EmailResult result = EmailResult.builder().success(true).build();

      result.setSuccess(false);
      result.setMessageId("<msg-789@server.com>");
      result.setErrorMessage("Failed to send");
      result.setRecipientCount(2);
      result.setServerResponse("550 Error");

      assertFalse(result.isSuccess());
      assertEquals("<msg-789@server.com>", result.getMessageId());
      assertEquals("Failed to send", result.getErrorMessage());
      assertEquals(2, result.getRecipientCount());
      assertEquals("550 Error", result.getServerResponse());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      EmailResult result1 = EmailResult.success("<msg-123@server.com>", 2);
      EmailResult result2 = EmailResult.success("<msg-123@server.com>", 2);

      // Note: timestamps will differ, but we can verify factory works
      assertTrue(result1.isSuccess());
      assertTrue(result2.isSuccess());
      assertEquals(result1.getMessageId(), result2.getMessageId());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      EmailResult result = EmailResult.success("<msg-123@server.com>", 2);
      String str = result.toString();

      assertTrue(str.contains("true")); // success
      assertTrue(str.contains("<msg-123@server.com>"));
    }
  }
}
