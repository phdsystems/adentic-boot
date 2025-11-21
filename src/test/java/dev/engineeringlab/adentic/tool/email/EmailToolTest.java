package dev.engineeringlab.adentic.tool.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import dev.engineeringlab.adentic.tool.email.config.EmailConfig;
import dev.engineeringlab.adentic.tool.email.config.EmailConfig.EmailProvider;
import dev.engineeringlab.adentic.tool.email.model.EmailMessage;
import dev.engineeringlab.adentic.tool.email.model.EmailResult;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Comprehensive tests for EmailTool covering:
 *
 * <ul>
 *   <li>SMTP provider operations
 *   <li>SendGrid provider (placeholder)
 *   <li>Mailgun provider (placeholder)
 *   <li>Message creation and validation
 *   <li>Convenience methods
 *   <li>Configuration handling
 *   <li>Error handling
 * </ul>
 */
@DisplayName("EmailTool Tests")
class EmailToolTest {

  private EmailTool emailTool;

  @BeforeEach
  void setUp() {
    EmailConfig config =
        EmailConfig.builder()
            .provider(EmailProvider.SMTP)
            .smtpHost("smtp.example.com")
            .smtpPort(587)
            .username("test@example.com")
            .password("test-password")
            .build();
    emailTool = new EmailTool(config);
  }

  @Nested
  @DisplayName("SMTP Provider Tests")
  class SmtpProviderTests {

    @Test
    @DisplayName("Should send email via SMTP (placeholder)")
    void testSendViaSmtp() {
      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .subject("Test Subject")
              .body("Test Body")
              .build();

      EmailResult result = emailTool.send(message).block();

      assertNotNull(result);
      // SMTP is not fully implemented, so it returns an error
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("SMTP not implemented");
    }

    @Test
    @DisplayName("Should fail when SMTP credentials are missing")
    void testSmtpMissingCredentials() {
      EmailConfig config =
          EmailConfig.builder().provider(EmailProvider.SMTP).smtpHost("smtp.example.com").build();
      EmailTool tool = new EmailTool(config);

      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .subject("Test")
              .body("Test")
              .build();

      EmailResult result = tool.send(message).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("SMTP credentials not configured");
    }
  }

  @Nested
  @DisplayName("SendGrid Provider Tests")
  class SendGridProviderTests {

    @Test
    @DisplayName("Should return error for SendGrid (not implemented)")
    void testSendViaSendGrid() {
      EmailConfig config =
          EmailConfig.builder().provider(EmailProvider.SENDGRID).apiKey("test-key").build();
      EmailTool tool = new EmailTool(config);

      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .subject("Test")
              .body("Test")
              .build();

      EmailResult result = tool.send(message).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("SendGrid not implemented");
    }
  }

  @Nested
  @DisplayName("Mailgun Provider Tests")
  class MailgunProviderTests {

    @Test
    @DisplayName("Should return error for Mailgun (not implemented)")
    void testSendViaMailgun() {
      EmailConfig config =
          EmailConfig.builder()
              .provider(EmailProvider.MAILGUN)
              .apiKey("test-key")
              .apiDomain("example.com")
              .build();
      EmailTool tool = new EmailTool(config);

      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .subject("Test")
              .body("Test")
              .build();

      EmailResult result = tool.send(message).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).contains("Mailgun not implemented");
    }
  }

  @Nested
  @DisplayName("Email Message Tests")
  class EmailMessageTests {

    @Test
    @DisplayName("Should create simple email message")
    void testSimpleMessage() {
      EmailMessage message = EmailMessage.simple("from@test.com", "to@test.com", "Subject", "Body");

      assertNotNull(message);
      assertEquals("from@test.com", message.getFrom());
      assertEquals(1, message.getTo().size());
      assertEquals("to@test.com", message.getTo().get(0));
      assertEquals("Subject", message.getSubject());
      assertEquals("Body", message.getBody());
      assertFalse(message.isHtml());
    }

    @Test
    @DisplayName("Should create HTML email message")
    void testHtmlMessage() {
      EmailMessage message =
          EmailMessage.html("from@test.com", "to@test.com", "Subject", "<h1>HTML Body</h1>");

      assertNotNull(message);
      assertTrue(message.isHtml());
      assertThat(message.getBody()).contains("<h1>");
    }

    @Test
    @DisplayName("Should build message with multiple recipients")
    void testMultipleRecipients() {
      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient1@example.com", "recipient2@example.com"))
              .subject("Test")
              .body("Test")
              .build();

      assertNotNull(message);
      assertEquals(2, message.getTo().size());
    }

    @Test
    @DisplayName("Should build message with CC recipients")
    void testCcRecipients() {
      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .cc(List.of("cc@example.com"))
              .subject("Test")
              .body("Test")
              .build();

      assertNotNull(message);
      assertEquals(1, message.getCc().size());
    }

    @Test
    @DisplayName("Should build message with BCC recipients")
    void testBccRecipients() {
      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .bcc(List.of("bcc@example.com"))
              .subject("Test")
              .body("Test")
              .build();

      assertNotNull(message);
      assertEquals(1, message.getBcc().size());
    }

    @Test
    @DisplayName("Should build message with attachments")
    void testMessageWithAttachments() {
      EmailMessage message =
          EmailMessage.builder()
              .from("sender@example.com")
              .to(List.of("recipient@example.com"))
              .subject("Test")
              .body("Test")
              .attachments(Map.of("file1.pdf", "application/pdf", "file2.pdf", "application/pdf"))
              .build();

      assertNotNull(message);
      assertEquals(2, message.getAttachments().size());
    }
  }

  @Nested
  @DisplayName("Convenience Methods")
  class ConvenienceMethodsTests {

    @Test
    @DisplayName("Should send simple email using convenience method")
    void testSendSimple() {
      EmailResult result =
          emailTool.sendSimple("from@test.com", "to@test.com", "Subject", "Body").block();

      assertNotNull(result);
      // Will fail because SMTP is not implemented
      assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("Should send HTML email using convenience method")
    void testSendHtml() {
      EmailResult result =
          emailTool.sendHtml("from@test.com", "to@test.com", "Subject", "<h1>HTML</h1>").block();

      assertNotNull(result);
      // Will fail because SMTP is not implemented
      assertFalse(result.isSuccess());
    }
  }

  @Nested
  @DisplayName("Email Result Tests")
  class EmailResultTests {

    @Test
    @DisplayName("Should create success result")
    void testSuccessResult() {
      EmailResult result = EmailResult.success("message-id-123", 1);

      assertNotNull(result);
      assertTrue(result.isSuccess());
      assertEquals("message-id-123", result.getMessageId());
      assertEquals(1, result.getRecipientCount());
      assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("Should create error result")
    void testErrorResult() {
      EmailResult result = EmailResult.error("Connection failed");

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertEquals("Connection failed", result.getErrorMessage());
      assertNull(result.getMessageId());
    }

    @Test
    @DisplayName("Should build result with builder")
    void testBuilderPattern() {
      EmailResult result =
          EmailResult.builder().success(true).messageId("msg-123").recipientCount(3).build();

      assertTrue(result.isSuccess());
      assertEquals("msg-123", result.getMessageId());
      assertEquals(3, result.getRecipientCount());
    }
  }

  @Nested
  @DisplayName("Configuration Tests")
  class ConfigurationTests {

    @Test
    @DisplayName("Should create SMTP configuration")
    void testSmtpConfig() {
      EmailConfig config = EmailConfig.gmail("user@gmail.com", "password");

      assertNotNull(config);
      assertEquals(EmailProvider.SMTP, config.getProvider());
      assertEquals("smtp.gmail.com", config.getSmtpHost());
      assertEquals(587, config.getSmtpPort());
      assertTrue(config.isUseTls());
    }

    @Test
    @DisplayName("Should create SendGrid configuration")
    void testSendGridConfig() {
      EmailConfig config = EmailConfig.sendgrid("api-key");

      assertNotNull(config);
      assertEquals(EmailProvider.SENDGRID, config.getProvider());
      assertEquals("api-key", config.getApiKey());
    }

    @Test
    @DisplayName("Should create Mailgun configuration")
    void testMailgunConfig() {
      EmailConfig config = EmailConfig.mailgun("api-key", "example.com");

      assertNotNull(config);
      assertEquals(EmailProvider.MAILGUN, config.getProvider());
      assertEquals("api-key", config.getApiKey());
      assertEquals("example.com", config.getApiDomain());
    }

    @Test
    @DisplayName("Should use default configuration")
    void testDefaultConfig() {
      EmailConfig config = EmailConfig.builder().build();

      assertNotNull(config);
      assertEquals(EmailProvider.SMTP, config.getProvider());
    }
  }

  @Nested
  @DisplayName("Error Handling Tests")
  class ErrorHandlingTests {

    @Test
    @DisplayName("Should handle exceptions during send")
    void testExceptionHandling() {
      EmailMessage message =
          EmailMessage.builder()
              .from("invalid")
              .to(List.of("invalid"))
              .subject("Test")
              .body("Test")
              .build();

      EmailResult result = emailTool.send(message).block();

      assertNotNull(result);
      assertFalse(result.isSuccess());
      assertThat(result.getErrorMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle missing recipient")
    void testMissingRecipient() {
      EmailMessage message =
          EmailMessage.builder().from("sender@example.com").subject("Test").body("Test").build();

      EmailResult result = emailTool.send(message).block();

      assertNotNull(result);
      // Will fail due to implementation or validation
      assertFalse(result.isSuccess());
    }
  }
}
