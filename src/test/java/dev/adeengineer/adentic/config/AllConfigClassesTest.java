package dev.adeengineer.adentic.config;

import static org.junit.jupiter.api.Assertions.*;

import dev.adeengineer.adentic.tool.calculator.config.CalculatorConfig;
import dev.adeengineer.adentic.tool.database.config.DatabaseConfig;
import dev.adeengineer.adentic.tool.database.model.DatabaseProvider;
import dev.adeengineer.adentic.tool.datetime.config.DateTimeConfig;
import dev.adeengineer.adentic.tool.email.config.EmailConfig;
import dev.adeengineer.adentic.tool.email.config.EmailConfig.EmailProvider;
import dev.adeengineer.adentic.tool.filesystem.config.FileSystemConfig;
import dev.adeengineer.adentic.tool.http.config.HttpConfig;
import dev.adeengineer.adentic.tool.marketdata.config.MarketDataConfig;
import dev.adeengineer.adentic.tool.marketdata.model.DataProvider;
import dev.adeengineer.adentic.tool.websearch.config.WebSearchConfig;
import dev.adeengineer.adentic.tool.websearch.model.SearchProvider;
import dev.adeengineer.adentic.tool.websearch.model.SearchRequest.SafeSearch;
import dev.adeengineer.adentic.tool.webtest.config.WebTestConfig;
import dev.adeengineer.adentic.tool.webtest.model.BrowserType;
import dev.adeengineer.adentic.tool.webtest.model.TestProvider;
import java.math.MathContext;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Comprehensive tests for all configuration classes. */
@DisplayName("All Config Classes Tests")
class AllConfigClassesTest {

  @Nested
  @DisplayName("CalculatorConfig Tests")
  class CalculatorConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      CalculatorConfig config =
          CalculatorConfig.builder()
              .defaultPrecision(5)
              .maxPrecision(20)
              .roundingMode(RoundingMode.HALF_EVEN)
              .mathContext(MathContext.DECIMAL64)
              .enableScientific(false)
              .enableFinancial(false)
              .enableStatistical(false)
              .maxOperands(500)
              .build();

      assertEquals(5, config.getDefaultPrecision());
      assertEquals(20, config.getMaxPrecision());
      assertEquals(RoundingMode.HALF_EVEN, config.getRoundingMode());
      assertEquals(MathContext.DECIMAL64, config.getMathContext());
      assertFalse(config.isEnableScientific());
      assertFalse(config.isEnableFinancial());
      assertFalse(config.isEnableStatistical());
      assertEquals(500, config.getMaxOperands());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      CalculatorConfig config = CalculatorConfig.defaults();

      assertEquals(2, config.getDefaultPrecision());
      assertEquals(10, config.getMaxPrecision());
      assertEquals(RoundingMode.HALF_UP, config.getRoundingMode());
      assertEquals(MathContext.DECIMAL128, config.getMathContext());
      assertTrue(config.isEnableScientific());
      assertTrue(config.isEnableFinancial());
      assertTrue(config.isEnableStatistical());
      assertEquals(1000, config.getMaxOperands());
    }

    @Test
    @DisplayName("Should create basic config")
    void testBasic() {
      CalculatorConfig config = CalculatorConfig.basic();

      assertFalse(config.isEnableScientific());
      assertFalse(config.isEnableFinancial());
      assertFalse(config.isEnableStatistical());
      assertEquals(2, config.getDefaultPrecision());
      assertEquals(10, config.getMaxPrecision());
    }

    @Test
    @DisplayName("Should create high precision config")
    void testHighPrecision() {
      CalculatorConfig config = CalculatorConfig.highPrecision();

      assertEquals(10, config.getDefaultPrecision());
      assertEquals(34, config.getMaxPrecision());
      assertEquals(MathContext.DECIMAL128, config.getMathContext());
      assertTrue(config.isEnableScientific());
      assertTrue(config.isEnableFinancial());
      assertTrue(config.isEnableStatistical());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      CalculatorConfig config = CalculatorConfig.defaults();

      config.setDefaultPrecision(8);
      config.setMaxPrecision(16);
      config.setRoundingMode(RoundingMode.CEILING);
      config.setMathContext(MathContext.DECIMAL32);
      config.setEnableScientific(false);
      config.setEnableFinancial(false);
      config.setEnableStatistical(false);
      config.setMaxOperands(2000);

      assertEquals(8, config.getDefaultPrecision());
      assertEquals(16, config.getMaxPrecision());
      assertEquals(RoundingMode.CEILING, config.getRoundingMode());
      assertEquals(MathContext.DECIMAL32, config.getMathContext());
      assertFalse(config.isEnableScientific());
      assertFalse(config.isEnableFinancial());
      assertFalse(config.isEnableStatistical());
      assertEquals(2000, config.getMaxOperands());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      CalculatorConfig config1 =
          CalculatorConfig.builder().defaultPrecision(5).maxPrecision(15).build();

      CalculatorConfig config2 =
          CalculatorConfig.builder().defaultPrecision(5).maxPrecision(15).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      CalculatorConfig config = CalculatorConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("defaultPrecision"));
      assertTrue(str.contains("maxPrecision"));
    }
  }

  @Nested
  @DisplayName("DateTimeConfig Tests")
  class DateTimeConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      ZoneId timezone = ZoneId.of("America/New_York");

      DateTimeConfig config =
          DateTimeConfig.builder()
              .defaultTimezone(timezone)
              .defaultDateFormat("MM/dd/yyyy")
              .defaultDateTimeFormat("MM/dd/yyyy HH:mm:ss")
              .defaultTimeFormat("HH:mm")
              .useUtcDefault(true)
              .includeTimezone(false)
              .build();

      assertEquals(timezone, config.getDefaultTimezone());
      assertEquals("MM/dd/yyyy", config.getDefaultDateFormat());
      assertEquals("MM/dd/yyyy HH:mm:ss", config.getDefaultDateTimeFormat());
      assertEquals("HH:mm", config.getDefaultTimeFormat());
      assertTrue(config.isUseUtcDefault());
      assertFalse(config.isIncludeTimezone());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      DateTimeConfig config = DateTimeConfig.defaults();

      assertEquals(ZoneId.systemDefault(), config.getDefaultTimezone());
      assertEquals("yyyy-MM-dd", config.getDefaultDateFormat());
      assertEquals("yyyy-MM-dd'T'HH:mm:ss", config.getDefaultDateTimeFormat());
      assertEquals("HH:mm:ss", config.getDefaultTimeFormat());
      assertFalse(config.isUseUtcDefault());
      assertTrue(config.isIncludeTimezone());
    }

    @Test
    @DisplayName("Should create UTC config")
    void testUtc() {
      DateTimeConfig config = DateTimeConfig.utc();

      assertEquals(ZoneId.of("UTC"), config.getDefaultTimezone());
      assertTrue(config.isUseUtcDefault());
    }

    @Test
    @DisplayName("Should create ISO 8601 config")
    void testIso8601() {
      DateTimeConfig config = DateTimeConfig.iso8601();

      assertTrue(config.isIncludeTimezone());
      assertNotNull(config.getDefaultDateTimeFormat());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      DateTimeConfig config = DateTimeConfig.defaults();
      ZoneId newZone = ZoneId.of("Europe/London");

      config.setDefaultTimezone(newZone);
      config.setDefaultDateFormat("dd-MM-yyyy");
      config.setDefaultDateTimeFormat("dd-MM-yyyy HH:mm:ss");
      config.setDefaultTimeFormat("HH:mm:ss.SSS");
      config.setUseUtcDefault(true);
      config.setIncludeTimezone(false);

      assertEquals(newZone, config.getDefaultTimezone());
      assertEquals("dd-MM-yyyy", config.getDefaultDateFormat());
      assertEquals("dd-MM-yyyy HH:mm:ss", config.getDefaultDateTimeFormat());
      assertEquals("HH:mm:ss.SSS", config.getDefaultTimeFormat());
      assertTrue(config.isUseUtcDefault());
      assertFalse(config.isIncludeTimezone());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      ZoneId zone = ZoneId.of("UTC");
      DateTimeConfig config1 = DateTimeConfig.builder().defaultTimezone(zone).build();

      DateTimeConfig config2 = DateTimeConfig.builder().defaultTimezone(zone).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      DateTimeConfig config = DateTimeConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("defaultTimezone"));
      assertTrue(str.contains("defaultDateFormat"));
    }
  }

  @Nested
  @DisplayName("EmailConfig Tests")
  class EmailConfigTests {

    @Test
    @DisplayName("Should test EmailProvider enum values")
    void testEmailProviderEnum() {
      EmailProvider[] providers = EmailProvider.values();
      assertEquals(3, providers.length);

      assertNotNull(EmailProvider.valueOf("SMTP"));
      assertNotNull(EmailProvider.valueOf("SENDGRID"));
      assertNotNull(EmailProvider.valueOf("MAILGUN"));
    }

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      EmailConfig config =
          EmailConfig.builder()
              .provider(EmailProvider.SENDGRID)
              .smtpHost("smtp.example.com")
              .smtpPort(465)
              .username("user@example.com")
              .password("secret")
              .useTls(false)
              .useSsl(true)
              .timeoutMs(60000)
              .apiKey("api-key-123")
              .apiDomain("example.com")
              .debug(true)
              .build();

      assertEquals(EmailProvider.SENDGRID, config.getProvider());
      assertEquals("smtp.example.com", config.getSmtpHost());
      assertEquals(465, config.getSmtpPort());
      assertEquals("user@example.com", config.getUsername());
      assertEquals("secret", config.getPassword());
      assertFalse(config.isUseTls());
      assertTrue(config.isUseSsl());
      assertEquals(60000, config.getTimeoutMs());
      assertEquals("api-key-123", config.getApiKey());
      assertEquals("example.com", config.getApiDomain());
      assertTrue(config.isDebug());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      EmailConfig config = EmailConfig.builder().build();

      assertEquals(EmailProvider.SMTP, config.getProvider());
      assertEquals("smtp.gmail.com", config.getSmtpHost());
      assertEquals(587, config.getSmtpPort());
      assertNull(config.getUsername());
      assertNull(config.getPassword());
      assertTrue(config.isUseTls());
      assertFalse(config.isUseSsl());
      assertEquals(30000, config.getTimeoutMs());
      assertNull(config.getApiKey());
      assertNull(config.getApiDomain());
      assertFalse(config.isDebug());
    }

    @Test
    @DisplayName("Should create Gmail config")
    void testGmail() {
      EmailConfig config = EmailConfig.gmail("test@gmail.com", "app-password");

      assertEquals(EmailProvider.SMTP, config.getProvider());
      assertEquals("smtp.gmail.com", config.getSmtpHost());
      assertEquals(587, config.getSmtpPort());
      assertEquals("test@gmail.com", config.getUsername());
      assertEquals("app-password", config.getPassword());
      assertTrue(config.isUseTls());
    }

    @Test
    @DisplayName("Should create SendGrid config")
    void testSendgrid() {
      EmailConfig config = EmailConfig.sendgrid("sg-api-key");

      assertEquals(EmailProvider.SENDGRID, config.getProvider());
      assertEquals("sg-api-key", config.getApiKey());
    }

    @Test
    @DisplayName("Should create Mailgun config")
    void testMailgun() {
      EmailConfig config = EmailConfig.mailgun("mg-api-key", "mail.example.com");

      assertEquals(EmailProvider.MAILGUN, config.getProvider());
      assertEquals("mg-api-key", config.getApiKey());
      assertEquals("mail.example.com", config.getApiDomain());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      EmailConfig config = EmailConfig.builder().build();

      config.setProvider(EmailProvider.MAILGUN);
      config.setSmtpHost("mail.example.com");
      config.setSmtpPort(25);
      config.setUsername("admin");
      config.setPassword("pass");
      config.setUseTls(false);
      config.setUseSsl(true);
      config.setTimeoutMs(45000);
      config.setApiKey("key");
      config.setApiDomain("domain");
      config.setDebug(true);

      assertEquals(EmailProvider.MAILGUN, config.getProvider());
      assertEquals("mail.example.com", config.getSmtpHost());
      assertEquals(25, config.getSmtpPort());
      assertEquals("admin", config.getUsername());
      assertEquals("pass", config.getPassword());
      assertFalse(config.isUseTls());
      assertTrue(config.isUseSsl());
      assertEquals(45000, config.getTimeoutMs());
      assertEquals("key", config.getApiKey());
      assertEquals("domain", config.getApiDomain());
      assertTrue(config.isDebug());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      EmailConfig config1 =
          EmailConfig.builder().username("user@test.com").password("pass").build();

      EmailConfig config2 =
          EmailConfig.builder().username("user@test.com").password("pass").build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      EmailConfig config = EmailConfig.builder().username("test@example.com").build();
      String str = config.toString();

      assertTrue(str.contains("provider"));
      assertTrue(str.contains("smtpHost"));
    }
  }

  @Nested
  @DisplayName("DatabaseConfig Tests")
  class DatabaseConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      DatabaseConfig config =
          DatabaseConfig.builder()
              .provider(DatabaseProvider.POSTGRESQL)
              .host("db.example.com")
              .port(5433)
              .database("mydb")
              .username("admin")
              .password("secret")
              .poolSize(20)
              .connectionTimeout(60000)
              .queryTimeout(120000)
              .maxResultRows(5000)
              .useSSL(true)
              .validateSSL(false)
              .enableQueryLogging(false)
              .autoCommit(false)
              .isolationLevel("SERIALIZABLE")
              .jdbcUrl("jdbc:postgresql://localhost/test")
              .mongoUri("mongodb://localhost:27017")
              .build();

      assertEquals(DatabaseProvider.POSTGRESQL, config.getProvider());
      assertEquals("db.example.com", config.getHost());
      assertEquals(5433, config.getPort());
      assertEquals("mydb", config.getDatabase());
      assertEquals("admin", config.getUsername());
      assertEquals("secret", config.getPassword());
      assertEquals(20, config.getPoolSize());
      assertEquals(60000, config.getConnectionTimeout());
      assertEquals(120000, config.getQueryTimeout());
      assertEquals(5000, config.getMaxResultRows());
      assertTrue(config.isUseSSL());
      assertFalse(config.isValidateSSL());
      assertFalse(config.isEnableQueryLogging());
      assertFalse(config.isAutoCommit());
      assertEquals("SERIALIZABLE", config.getIsolationLevel());
      assertEquals("jdbc:postgresql://localhost/test", config.getJdbcUrl());
      assertEquals("mongodb://localhost:27017", config.getMongoUri());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      DatabaseConfig config = DatabaseConfig.defaults();

      assertEquals(DatabaseProvider.H2, config.getProvider());
      assertEquals("mem:testdb", config.getDatabase());
      assertEquals("sa", config.getUsername());
      assertEquals("", config.getPassword());
      assertTrue(config.isAutoCommit());
    }

    @Test
    @DisplayName("Should create PostgreSQL config with parameters")
    void testPostgresqlWithParams() {
      DatabaseConfig config =
          DatabaseConfig.postgresql("db.example.com", 5433, "mydb", "user", "pass");

      assertEquals(DatabaseProvider.POSTGRESQL, config.getProvider());
      assertEquals("db.example.com", config.getHost());
      assertEquals(5433, config.getPort());
      assertEquals("mydb", config.getDatabase());
      assertEquals("user", config.getUsername());
      assertEquals("pass", config.getPassword());
    }

    @Test
    @DisplayName("Should create PostgreSQL config with defaults")
    void testPostgresqlDefaults() {
      DatabaseConfig config = DatabaseConfig.postgresql();

      assertEquals(DatabaseProvider.POSTGRESQL, config.getProvider());
      assertEquals("localhost", config.getHost());
      assertEquals(5432, config.getPort());
      assertEquals("postgres", config.getDatabase());
    }

    @Test
    @DisplayName("Should create MySQL config with parameters")
    void testMysqlWithParams() {
      DatabaseConfig config = DatabaseConfig.mysql("db.example.com", 3307, "mydb", "user", "pass");

      assertEquals(DatabaseProvider.MYSQL, config.getProvider());
      assertEquals("db.example.com", config.getHost());
      assertEquals(3307, config.getPort());
      assertEquals("mydb", config.getDatabase());
    }

    @Test
    @DisplayName("Should create MySQL config with defaults")
    void testMysqlDefaults() {
      DatabaseConfig config = DatabaseConfig.mysql();

      assertEquals(DatabaseProvider.MYSQL, config.getProvider());
      assertEquals("localhost", config.getHost());
      assertEquals(3306, config.getPort());
      assertEquals("mysql", config.getDatabase());
    }

    @Test
    @DisplayName("Should create SQLite config")
    void testSqlite() {
      DatabaseConfig config = DatabaseConfig.sqlite("/path/to/db.sqlite");

      assertEquals(DatabaseProvider.SQLITE, config.getProvider());
      assertEquals("/path/to/db.sqlite", config.getDatabase());
      assertNull(config.getHost());
      assertEquals(0, config.getPort());
    }

    @Test
    @DisplayName("Should create SQLite in-memory config")
    void testSqliteMemory() {
      DatabaseConfig config = DatabaseConfig.sqliteMemory();

      assertEquals(DatabaseProvider.SQLITE, config.getProvider());
      assertEquals(":memory:", config.getDatabase());
    }

    @Test
    @DisplayName("Should create MongoDB config with parameters")
    void testMongodbWithParams() {
      DatabaseConfig config =
          DatabaseConfig.mongodb("db.example.com", 27018, "mydb", "user", "pass");

      assertEquals(DatabaseProvider.MONGODB, config.getProvider());
      assertEquals("db.example.com", config.getHost());
      assertEquals(27018, config.getPort());
      assertEquals("mydb", config.getDatabase());
    }

    @Test
    @DisplayName("Should create MongoDB config with defaults")
    void testMongodbDefaults() {
      DatabaseConfig config = DatabaseConfig.mongodb();

      assertEquals(DatabaseProvider.MONGODB, config.getProvider());
      assertEquals("localhost", config.getHost());
      assertEquals(27017, config.getPort());
      assertEquals("test", config.getDatabase());
    }

    @Test
    @DisplayName("Should create H2 in-memory config")
    void testH2Memory() {
      DatabaseConfig config = DatabaseConfig.h2Memory();

      assertEquals(DatabaseProvider.H2, config.getProvider());
      assertEquals("mem:testdb", config.getDatabase());
      assertEquals("sa", config.getUsername());
      assertEquals("", config.getPassword());
      assertTrue(config.isAutoCommit());
    }

    @Test
    @DisplayName("Should create H2 file config")
    void testH2File() {
      DatabaseConfig config = DatabaseConfig.h2File("/path/to/db");

      assertEquals(DatabaseProvider.H2, config.getProvider());
      assertEquals("file:/path/to/db", config.getDatabase());
      assertEquals("sa", config.getUsername());
    }

    @Test
    @DisplayName("Should create development config")
    void testDevelopment() {
      DatabaseConfig config = DatabaseConfig.development();

      assertEquals(DatabaseProvider.POSTGRESQL, config.getProvider());
      assertEquals("localhost", config.getHost());
      assertEquals("dev_db", config.getDatabase());
      assertTrue(config.isEnableQueryLogging());
      assertFalse(config.isUseSSL());
    }

    @Test
    @DisplayName("Should create production config")
    void testProduction() {
      DatabaseConfig config =
          DatabaseConfig.production("prod.db.com", "proddb", "produser", "prodpass");

      assertEquals(DatabaseProvider.POSTGRESQL, config.getProvider());
      assertEquals("prod.db.com", config.getHost());
      assertEquals("proddb", config.getDatabase());
      assertEquals("produser", config.getUsername());
      assertTrue(config.isUseSSL());
      assertTrue(config.isValidateSSL());
      assertEquals(20, config.getPoolSize());
      assertFalse(config.isEnableQueryLogging());
    }

    @Test
    @DisplayName("Should create testing config")
    void testTesting() {
      DatabaseConfig config = DatabaseConfig.testing();

      assertEquals(DatabaseProvider.H2, config.getProvider());
      assertEquals("mem:testdb", config.getDatabase());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      DatabaseConfig config = DatabaseConfig.defaults();

      config.setProvider(DatabaseProvider.MYSQL);
      config.setHost("newhost");
      config.setPort(3307);
      config.setDatabase("newdb");
      config.setUsername("newuser");
      config.setPassword("newpass");
      config.setPoolSize(15);
      config.setConnectionTimeout(20000);
      config.setQueryTimeout(40000);
      config.setMaxResultRows(2000);
      config.setUseSSL(true);
      config.setValidateSSL(false);
      config.setEnableQueryLogging(false);
      config.setAutoCommit(false);
      config.setIsolationLevel("READ_COMMITTED");
      config.setJdbcUrl("jdbc:mysql://localhost/test");
      config.setMongoUri("mongodb://localhost");

      assertEquals(DatabaseProvider.MYSQL, config.getProvider());
      assertEquals("newhost", config.getHost());
      assertEquals(3307, config.getPort());
      assertEquals("newdb", config.getDatabase());
      assertEquals("newuser", config.getUsername());
      assertEquals("newpass", config.getPassword());
      assertEquals(15, config.getPoolSize());
      assertEquals(20000, config.getConnectionTimeout());
      assertEquals(40000, config.getQueryTimeout());
      assertEquals(2000, config.getMaxResultRows());
      assertTrue(config.isUseSSL());
      assertFalse(config.isValidateSSL());
      assertFalse(config.isEnableQueryLogging());
      assertFalse(config.isAutoCommit());
      assertEquals("READ_COMMITTED", config.getIsolationLevel());
      assertEquals("jdbc:mysql://localhost/test", config.getJdbcUrl());
      assertEquals("mongodb://localhost", config.getMongoUri());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      DatabaseConfig config1 = DatabaseConfig.builder().database("testdb").username("user").build();

      DatabaseConfig config2 = DatabaseConfig.builder().database("testdb").username("user").build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      DatabaseConfig config = DatabaseConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("provider"));
      assertTrue(str.contains("database"));
    }
  }

  @Nested
  @DisplayName("HttpConfig Tests")
  class HttpConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      HttpConfig config =
          HttpConfig.builder()
              .defaultTimeoutMs(60000)
              .connectTimeoutMs(20000)
              .followRedirects(false)
              .maxRedirects(10)
              .userAgent("CustomAgent/1.0")
              .validateSsl(false)
              .maxResponseSize(20 * 1024 * 1024)
              .enableLogging(false)
              .includeTimings(false)
              .build();

      assertEquals(60000, config.getDefaultTimeoutMs());
      assertEquals(20000, config.getConnectTimeoutMs());
      assertFalse(config.isFollowRedirects());
      assertEquals(10, config.getMaxRedirects());
      assertEquals("CustomAgent/1.0", config.getUserAgent());
      assertFalse(config.isValidateSsl());
      assertEquals(20 * 1024 * 1024, config.getMaxResponseSize());
      assertFalse(config.isEnableLogging());
      assertFalse(config.isIncludeTimings());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      HttpConfig config = HttpConfig.defaults();

      assertEquals(30000, config.getDefaultTimeoutMs());
      assertEquals(10000, config.getConnectTimeoutMs());
      assertTrue(config.isFollowRedirects());
      assertEquals(5, config.getMaxRedirects());
      assertEquals("Adentic-HttpClient/0.3.0", config.getUserAgent());
      assertTrue(config.isValidateSsl());
      assertEquals(10 * 1024 * 1024, config.getMaxResponseSize());
      assertTrue(config.isEnableLogging());
      assertTrue(config.isIncludeTimings());
    }

    @Test
    @DisplayName("Should create API testing config")
    void testApiTesting() {
      HttpConfig config = HttpConfig.apiTesting();

      assertEquals(60000, config.getDefaultTimeoutMs());
      assertTrue(config.isEnableLogging());
      assertTrue(config.isIncludeTimings());
    }

    @Test
    @DisplayName("Should create production config")
    void testProduction() {
      HttpConfig config = HttpConfig.production();

      assertEquals(15000, config.getDefaultTimeoutMs());
      assertTrue(config.isValidateSsl());
      assertFalse(config.isEnableLogging());
      assertEquals(5 * 1024 * 1024, config.getMaxResponseSize());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      HttpConfig config = HttpConfig.defaults();

      config.setDefaultTimeoutMs(45000);
      config.setConnectTimeoutMs(15000);
      config.setFollowRedirects(false);
      config.setMaxRedirects(3);
      config.setUserAgent("NewAgent/2.0");
      config.setValidateSsl(false);
      config.setMaxResponseSize(1024 * 1024);
      config.setEnableLogging(false);
      config.setIncludeTimings(false);

      assertEquals(45000, config.getDefaultTimeoutMs());
      assertEquals(15000, config.getConnectTimeoutMs());
      assertFalse(config.isFollowRedirects());
      assertEquals(3, config.getMaxRedirects());
      assertEquals("NewAgent/2.0", config.getUserAgent());
      assertFalse(config.isValidateSsl());
      assertEquals(1024 * 1024, config.getMaxResponseSize());
      assertFalse(config.isEnableLogging());
      assertFalse(config.isIncludeTimings());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      HttpConfig config1 = HttpConfig.builder().defaultTimeoutMs(30000).maxRedirects(5).build();

      HttpConfig config2 = HttpConfig.builder().defaultTimeoutMs(30000).maxRedirects(5).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      HttpConfig config = HttpConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("defaultTimeoutMs"));
      assertTrue(str.contains("userAgent"));
    }
  }

  @Nested
  @DisplayName("WebSearchConfig Tests")
  class WebSearchConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      WebSearchConfig config =
          WebSearchConfig.builder()
              .defaultProvider(SearchProvider.GOOGLE)
              .maxResults(20)
              .httpTimeoutMs(15000)
              .httpRetries(5)
              .cacheResults(false)
              .cacheTtlMs(600000)
              .defaultSafeSearch(SafeSearch.STRICT)
              .defaultRegion("en-GB")
              .userAgent("CustomBot/1.0")
              .googleApiKey("google-key")
              .googleCseId("cse-id")
              .bingApiKey("bing-key")
              .build();

      assertEquals(SearchProvider.GOOGLE, config.getDefaultProvider());
      assertEquals(20, config.getMaxResults());
      assertEquals(15000, config.getHttpTimeoutMs());
      assertEquals(5, config.getHttpRetries());
      assertFalse(config.isCacheResults());
      assertEquals(600000, config.getCacheTtlMs());
      assertEquals(SafeSearch.STRICT, config.getDefaultSafeSearch());
      assertEquals("en-GB", config.getDefaultRegion());
      assertEquals("CustomBot/1.0", config.getUserAgent());
      assertEquals("google-key", config.getGoogleApiKey());
      assertEquals("cse-id", config.getGoogleCseId());
      assertEquals("bing-key", config.getBingApiKey());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      WebSearchConfig config = WebSearchConfig.defaults();

      assertEquals(SearchProvider.DUCKDUCKGO, config.getDefaultProvider());
      assertEquals(10, config.getMaxResults());
      assertEquals(10000, config.getHttpTimeoutMs());
      assertEquals(2, config.getHttpRetries());
      assertTrue(config.isCacheResults());
      assertEquals(300000, config.getCacheTtlMs());
      assertEquals(SafeSearch.MODERATE, config.getDefaultSafeSearch());
      assertEquals("en-US", config.getDefaultRegion());
    }

    @Test
    @DisplayName("Should create fast config")
    void testFast() {
      WebSearchConfig config = WebSearchConfig.fast();

      assertEquals(5, config.getMaxResults());
      assertEquals(5000, config.getHttpTimeoutMs());
      assertEquals(1, config.getHttpRetries());
      assertTrue(config.isCacheResults());
    }

    @Test
    @DisplayName("Should create thorough config")
    void testThorough() {
      WebSearchConfig config = WebSearchConfig.thorough();

      assertEquals(20, config.getMaxResults());
      assertEquals(15000, config.getHttpTimeoutMs());
      assertEquals(3, config.getHttpRetries());
      assertTrue(config.isCacheResults());
    }

    @Test
    @DisplayName("Should create Google config")
    void testGoogle() {
      WebSearchConfig config = WebSearchConfig.google("api-key", "cse-id");

      assertEquals(SearchProvider.GOOGLE, config.getDefaultProvider());
      assertEquals("api-key", config.getGoogleApiKey());
      assertEquals("cse-id", config.getGoogleCseId());
    }

    @Test
    @DisplayName("Should create Bing config")
    void testBing() {
      WebSearchConfig config = WebSearchConfig.bing("bing-api-key");

      assertEquals(SearchProvider.BING, config.getDefaultProvider());
      assertEquals("bing-api-key", config.getBingApiKey());
    }

    @Test
    @DisplayName("Should create no-cache config")
    void testNoCache() {
      WebSearchConfig config = WebSearchConfig.noCache();

      assertFalse(config.isCacheResults());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      WebSearchConfig config = WebSearchConfig.defaults();

      config.setDefaultProvider(SearchProvider.BING);
      config.setMaxResults(15);
      config.setHttpTimeoutMs(12000);
      config.setHttpRetries(4);
      config.setCacheResults(false);
      config.setCacheTtlMs(180000);
      config.setDefaultSafeSearch(SafeSearch.OFF);
      config.setDefaultRegion("fr-FR");
      config.setUserAgent("TestBot/1.0");
      config.setGoogleApiKey("new-google-key");
      config.setGoogleCseId("new-cse-id");
      config.setBingApiKey("new-bing-key");

      assertEquals(SearchProvider.BING, config.getDefaultProvider());
      assertEquals(15, config.getMaxResults());
      assertEquals(12000, config.getHttpTimeoutMs());
      assertEquals(4, config.getHttpRetries());
      assertFalse(config.isCacheResults());
      assertEquals(180000, config.getCacheTtlMs());
      assertEquals(SafeSearch.OFF, config.getDefaultSafeSearch());
      assertEquals("fr-FR", config.getDefaultRegion());
      assertEquals("TestBot/1.0", config.getUserAgent());
      assertEquals("new-google-key", config.getGoogleApiKey());
      assertEquals("new-cse-id", config.getGoogleCseId());
      assertEquals("new-bing-key", config.getBingApiKey());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      WebSearchConfig config1 = WebSearchConfig.builder().maxResults(10).httpRetries(2).build();

      WebSearchConfig config2 = WebSearchConfig.builder().maxResults(10).httpRetries(2).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      WebSearchConfig config = WebSearchConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("defaultProvider"));
      assertTrue(str.contains("maxResults"));
    }
  }

  @Nested
  @DisplayName("FileSystemConfig Tests")
  class FileSystemConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      Set<Path> allowed = new HashSet<>();
      allowed.add(Paths.get("/home/user"));

      Set<Path> blocked = new HashSet<>();
      blocked.add(Paths.get("/etc"));

      Set<String> blockedExt = new HashSet<>();
      blockedExt.add(".exe");

      FileSystemConfig config =
          FileSystemConfig.builder()
              .allowedRoots(allowed)
              .blockedPaths(blocked)
              .allowRead(true)
              .allowWrite(false)
              .allowDelete(false)
              .allowCreateDirectory(false)
              .followSymlinks(true)
              .maxReadSize(5 * 1024 * 1024)
              .maxWriteSize(5 * 1024 * 1024)
              .maxResults(500)
              .maxDepth(5)
              .operationTimeoutMs(60000)
              .validatePaths(true)
              .calculateHashes(true)
              .defaultEncoding("UTF-16")
              .blockedExtensions(blockedExt)
              .build();

      assertEquals(allowed, config.getAllowedRoots());
      assertEquals(blocked, config.getBlockedPaths());
      assertTrue(config.isAllowRead());
      assertFalse(config.isAllowWrite());
      assertFalse(config.isAllowDelete());
      assertFalse(config.isAllowCreateDirectory());
      assertTrue(config.isFollowSymlinks());
      assertEquals(5 * 1024 * 1024, config.getMaxReadSize());
      assertEquals(5 * 1024 * 1024, config.getMaxWriteSize());
      assertEquals(500, config.getMaxResults());
      assertEquals(5, config.getMaxDepth());
      assertEquals(60000, config.getOperationTimeoutMs());
      assertTrue(config.isValidatePaths());
      assertTrue(config.isCalculateHashes());
      assertEquals("UTF-16", config.getDefaultEncoding());
      assertEquals(blockedExt, config.getBlockedExtensions());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      FileSystemConfig config = FileSystemConfig.defaults();

      assertTrue(config.getAllowedRoots().isEmpty());
      assertFalse(config.getBlockedPaths().isEmpty());
      assertTrue(config.isAllowRead());
      assertTrue(config.isAllowWrite());
      assertTrue(config.isAllowDelete());
      assertTrue(config.isAllowCreateDirectory());
      assertFalse(config.isFollowSymlinks());
      assertEquals(10 * 1024 * 1024, config.getMaxReadSize());
      assertEquals(10 * 1024 * 1024, config.getMaxWriteSize());
      assertEquals(1000, config.getMaxResults());
      assertEquals(10, config.getMaxDepth());
      assertEquals(30000, config.getOperationTimeoutMs());
      assertTrue(config.isValidatePaths());
      assertFalse(config.isCalculateHashes());
      assertEquals("UTF-8", config.getDefaultEncoding());
      assertTrue(config.getBlockedExtensions().isEmpty());
    }

    @Test
    @DisplayName("Should create read-only config")
    void testReadOnly() {
      FileSystemConfig config = FileSystemConfig.readOnly();

      assertTrue(config.isAllowRead());
      assertFalse(config.isAllowWrite());
      assertFalse(config.isAllowDelete());
      assertFalse(config.isAllowCreateDirectory());
    }

    @Test
    @DisplayName("Should create permissive config")
    void testPermissive() {
      FileSystemConfig config = FileSystemConfig.permissive();

      assertEquals(100 * 1024 * 1024, config.getMaxReadSize());
      assertEquals(100 * 1024 * 1024, config.getMaxWriteSize());
      assertEquals(10000, config.getMaxResults());
      assertEquals(20, config.getMaxDepth());
    }

    @Test
    @DisplayName("Should create sandboxed config")
    void testSandboxed() {
      FileSystemConfig config = FileSystemConfig.sandboxed("/home/sandbox");

      assertEquals(1, config.getAllowedRoots().size());
      assertTrue(config.getBlockedPaths().isEmpty());
    }

    @Test
    @DisplayName("Should create secure config")
    void testSecure() {
      FileSystemConfig config = FileSystemConfig.secure();

      assertFalse(config.isAllowWrite());
      assertFalse(config.isAllowDelete());
      assertFalse(config.isAllowCreateDirectory());
      assertFalse(config.isFollowSymlinks());
      assertEquals(1024 * 1024, config.getMaxReadSize());
      assertEquals(100, config.getMaxResults());
      assertEquals(5, config.getMaxDepth());
      assertTrue(config.isCalculateHashes());
    }

    @Test
    @DisplayName("Should create temp directory config")
    void testTempDirectory() {
      FileSystemConfig config = FileSystemConfig.tempDirectory();

      assertEquals(1, config.getAllowedRoots().size());
    }

    @Test
    @DisplayName("Should check if path is allowed")
    void testIsPathAllowed() {
      FileSystemConfig config = FileSystemConfig.sandboxed("/home/user");

      assertTrue(config.isPathAllowed(Paths.get("/home/user/file.txt")));
      assertFalse(config.isPathAllowed(Paths.get("/etc/passwd")));
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      FileSystemConfig config = FileSystemConfig.defaults();

      Set<Path> newAllowed = new HashSet<>();
      newAllowed.add(Paths.get("/tmp"));

      Set<Path> newBlocked = new HashSet<>();
      newBlocked.add(Paths.get("/root"));

      Set<String> newBlockedExt = new HashSet<>();
      newBlockedExt.add(".bat");

      config.setAllowedRoots(newAllowed);
      config.setBlockedPaths(newBlocked);
      config.setAllowRead(false);
      config.setAllowWrite(false);
      config.setAllowDelete(false);
      config.setAllowCreateDirectory(false);
      config.setFollowSymlinks(true);
      config.setMaxReadSize(2048);
      config.setMaxWriteSize(2048);
      config.setMaxResults(50);
      config.setMaxDepth(3);
      config.setOperationTimeoutMs(10000);
      config.setValidatePaths(false);
      config.setCalculateHashes(true);
      config.setDefaultEncoding("ISO-8859-1");
      config.setBlockedExtensions(newBlockedExt);

      assertEquals(newAllowed, config.getAllowedRoots());
      assertEquals(newBlocked, config.getBlockedPaths());
      assertFalse(config.isAllowRead());
      assertFalse(config.isAllowWrite());
      assertFalse(config.isAllowDelete());
      assertFalse(config.isAllowCreateDirectory());
      assertTrue(config.isFollowSymlinks());
      assertEquals(2048, config.getMaxReadSize());
      assertEquals(2048, config.getMaxWriteSize());
      assertEquals(50, config.getMaxResults());
      assertEquals(3, config.getMaxDepth());
      assertEquals(10000, config.getOperationTimeoutMs());
      assertFalse(config.isValidatePaths());
      assertTrue(config.isCalculateHashes());
      assertEquals("ISO-8859-1", config.getDefaultEncoding());
      assertEquals(newBlockedExt, config.getBlockedExtensions());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      FileSystemConfig config1 =
          FileSystemConfig.builder().maxReadSize(1024).maxResults(100).build();

      FileSystemConfig config2 =
          FileSystemConfig.builder().maxReadSize(1024).maxResults(100).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      FileSystemConfig config = FileSystemConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("allowRead"));
      assertTrue(str.contains("maxReadSize"));
    }
  }

  @Nested
  @DisplayName("WebTestConfig Tests")
  class WebTestConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      Set<String> allowed = new HashSet<>();
      allowed.add("example.com");

      Set<String> blocked = new HashSet<>();
      blocked.add("malicious.com");

      WebTestConfig config =
          WebTestConfig.builder()
              .provider(TestProvider.SELENIUM)
              .browser(BrowserType.FIREFOX)
              .headless(false)
              .timeout(60000)
              .pageLoadTimeout(60000)
              .scriptTimeout(60000)
              .viewportWidth(1920)
              .viewportHeight(1080)
              .allowedDomains(allowed)
              .blockedDomains(blocked)
              .userAgent("CustomAgent/1.0")
              .acceptInsecureCerts(true)
              .javascriptEnabled(false)
              .imagesEnabled(false)
              .slowMo(1000)
              .screenshotDir("/tmp/screenshots")
              .captureConsoleLogs(true)
              .captureNetwork(true)
              .maxScreenshots(200)
              .build();

      assertEquals(TestProvider.SELENIUM, config.getProvider());
      assertEquals(BrowserType.FIREFOX, config.getBrowser());
      assertFalse(config.isHeadless());
      assertEquals(60000, config.getTimeout());
      assertEquals(60000, config.getPageLoadTimeout());
      assertEquals(60000, config.getScriptTimeout());
      assertEquals(1920, config.getViewportWidth());
      assertEquals(1080, config.getViewportHeight());
      assertEquals(allowed, config.getAllowedDomains());
      assertEquals(blocked, config.getBlockedDomains());
      assertEquals("CustomAgent/1.0", config.getUserAgent());
      assertTrue(config.isAcceptInsecureCerts());
      assertFalse(config.isJavascriptEnabled());
      assertFalse(config.isImagesEnabled());
      assertEquals(1000, config.getSlowMo());
      assertEquals("/tmp/screenshots", config.getScreenshotDir());
      assertTrue(config.isCaptureConsoleLogs());
      assertTrue(config.isCaptureNetwork());
      assertEquals(200, config.getMaxScreenshots());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      WebTestConfig config = WebTestConfig.defaults();

      assertEquals(TestProvider.PLAYWRIGHT, config.getProvider());
      assertEquals(BrowserType.CHROMIUM, config.getBrowser());
      assertTrue(config.isHeadless());
      assertEquals(30000, config.getTimeout());
      assertEquals(1280, config.getViewportWidth());
      assertEquals(720, config.getViewportHeight());
      assertTrue(config.getAllowedDomains().isEmpty());
      assertTrue(config.getBlockedDomains().isEmpty());
      assertFalse(config.isAcceptInsecureCerts());
      assertTrue(config.isJavascriptEnabled());
      assertTrue(config.isImagesEnabled());
      assertEquals(0, config.getSlowMo());
      assertFalse(config.isCaptureConsoleLogs());
      assertFalse(config.isCaptureNetwork());
      assertEquals(100, config.getMaxScreenshots());
    }

    @Test
    @DisplayName("Should create Playwright config")
    void testPlaywright() {
      WebTestConfig config = WebTestConfig.playwright();

      assertEquals(TestProvider.PLAYWRIGHT, config.getProvider());
      assertEquals(BrowserType.CHROMIUM, config.getBrowser());
      assertTrue(config.isHeadless());
    }

    @Test
    @DisplayName("Should create Selenium config")
    void testSelenium() {
      WebTestConfig config = WebTestConfig.selenium();

      assertEquals(TestProvider.SELENIUM, config.getProvider());
      assertEquals(BrowserType.CHROME, config.getBrowser());
      assertTrue(config.isHeadless());
    }

    @Test
    @DisplayName("Should create HtmlUnit config")
    void testHtmlunit() {
      WebTestConfig config = WebTestConfig.htmlunit();

      assertEquals(TestProvider.HTMLUNIT, config.getProvider());
      assertEquals(BrowserType.NONE, config.getBrowser());
      assertEquals(10000, config.getTimeout());
      assertEquals(10000, config.getPageLoadTimeout());
    }

    @Test
    @DisplayName("Should create lightweight config")
    void testLightweight() {
      WebTestConfig config = WebTestConfig.lightweight();

      assertEquals(TestProvider.HTMLUNIT, config.getProvider());
      assertEquals(BrowserType.NONE, config.getBrowser());
    }

    @Test
    @DisplayName("Should create visible config")
    void testVisible() {
      WebTestConfig config = WebTestConfig.visible();

      assertEquals(TestProvider.PLAYWRIGHT, config.getProvider());
      assertEquals(BrowserType.CHROMIUM, config.getBrowser());
      assertFalse(config.isHeadless());
      assertEquals(500, config.getSlowMo());
    }

    @Test
    @DisplayName("Should create debug config")
    void testDebug() {
      WebTestConfig config = WebTestConfig.debug();

      assertEquals(TestProvider.PLAYWRIGHT, config.getProvider());
      assertFalse(config.isHeadless());
      assertEquals(1000, config.getSlowMo());
      assertTrue(config.isCaptureConsoleLogs());
      assertTrue(config.isCaptureNetwork());
    }

    @Test
    @DisplayName("Should create mobile config")
    void testMobile() {
      WebTestConfig config = WebTestConfig.mobile();

      assertEquals(390, config.getViewportWidth());
      assertEquals(844, config.getViewportHeight());
    }

    @Test
    @DisplayName("Should create desktop config")
    void testDesktop() {
      WebTestConfig config = WebTestConfig.desktop();

      assertEquals(1920, config.getViewportWidth());
      assertEquals(1080, config.getViewportHeight());
    }

    @Test
    @DisplayName("Should create fast config")
    void testFast() {
      WebTestConfig config = WebTestConfig.fast();

      assertEquals(TestProvider.HTMLUNIT, config.getProvider());
      assertEquals(10000, config.getTimeout());
      assertEquals(10000, config.getPageLoadTimeout());
      assertEquals(5000, config.getScriptTimeout());
      assertFalse(config.isImagesEnabled());
    }

    @Test
    @DisplayName("Should create cross-browser config")
    void testCrossBrowser() {
      WebTestConfig config = WebTestConfig.crossBrowser();

      assertEquals(TestProvider.SELENIUM, config.getProvider());
      assertEquals(BrowserType.FIREFOX, config.getBrowser());
      assertTrue(config.isHeadless());
    }

    @Test
    @DisplayName("Should create Puppeteer config")
    void testPuppeteer() {
      WebTestConfig config = WebTestConfig.puppeteer();

      assertEquals(TestProvider.PUPPETEER, config.getProvider());
      assertEquals(BrowserType.CHROMIUM, config.getBrowser());
      assertTrue(config.isHeadless());
    }

    @Test
    @DisplayName("Should create Cypress config")
    void testCypress() {
      WebTestConfig config = WebTestConfig.cypress();

      assertEquals(TestProvider.CYPRESS, config.getProvider());
      assertEquals(BrowserType.CHROMIUM, config.getBrowser());
      assertTrue(config.isHeadless());
    }

    @Test
    @DisplayName("Should check if domain is allowed")
    void testIsDomainAllowed() {
      Set<String> allowed = new HashSet<>();
      allowed.add("example.com");

      Set<String> blocked = new HashSet<>();
      blocked.add("blocked.com");

      WebTestConfig config =
          WebTestConfig.builder().allowedDomains(allowed).blockedDomains(blocked).build();

      assertTrue(config.isDomainAllowed("example.com"));
      assertFalse(config.isDomainAllowed("blocked.com"));
      assertFalse(config.isDomainAllowed("other.com"));
    }

    @Test
    @DisplayName("Should allow all domains when whitelist is empty")
    void testAllowAllDomains() {
      Set<String> blocked = new HashSet<>();
      blocked.add("blocked.com");

      WebTestConfig config = WebTestConfig.builder().blockedDomains(blocked).build();

      assertTrue(config.isDomainAllowed("example.com"));
      assertFalse(config.isDomainAllowed("blocked.com"));
      assertTrue(config.isDomainAllowed("other.com"));
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      WebTestConfig config = WebTestConfig.defaults();

      Set<String> newAllowed = new HashSet<>();
      newAllowed.add("test.com");

      Set<String> newBlocked = new HashSet<>();
      newBlocked.add("bad.com");

      config.setProvider(TestProvider.SELENIUM);
      config.setBrowser(BrowserType.FIREFOX);
      config.setHeadless(false);
      config.setTimeout(45000);
      config.setPageLoadTimeout(45000);
      config.setScriptTimeout(45000);
      config.setViewportWidth(800);
      config.setViewportHeight(600);
      config.setAllowedDomains(newAllowed);
      config.setBlockedDomains(newBlocked);
      config.setUserAgent("TestAgent/1.0");
      config.setAcceptInsecureCerts(true);
      config.setJavascriptEnabled(false);
      config.setImagesEnabled(false);
      config.setSlowMo(2000);
      config.setScreenshotDir("/var/screenshots");
      config.setCaptureConsoleLogs(true);
      config.setCaptureNetwork(true);
      config.setMaxScreenshots(50);

      assertEquals(TestProvider.SELENIUM, config.getProvider());
      assertEquals(BrowserType.FIREFOX, config.getBrowser());
      assertFalse(config.isHeadless());
      assertEquals(45000, config.getTimeout());
      assertEquals(45000, config.getPageLoadTimeout());
      assertEquals(45000, config.getScriptTimeout());
      assertEquals(800, config.getViewportWidth());
      assertEquals(600, config.getViewportHeight());
      assertEquals(newAllowed, config.getAllowedDomains());
      assertEquals(newBlocked, config.getBlockedDomains());
      assertEquals("TestAgent/1.0", config.getUserAgent());
      assertTrue(config.isAcceptInsecureCerts());
      assertFalse(config.isJavascriptEnabled());
      assertFalse(config.isImagesEnabled());
      assertEquals(2000, config.getSlowMo());
      assertEquals("/var/screenshots", config.getScreenshotDir());
      assertTrue(config.isCaptureConsoleLogs());
      assertTrue(config.isCaptureNetwork());
      assertEquals(50, config.getMaxScreenshots());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      WebTestConfig config1 =
          WebTestConfig.builder().viewportWidth(1280).viewportHeight(720).build();

      WebTestConfig config2 =
          WebTestConfig.builder().viewportWidth(1280).viewportHeight(720).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      WebTestConfig config = WebTestConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("provider"));
      assertTrue(str.contains("browser"));
    }
  }

  @Nested
  @DisplayName("MarketDataConfig Tests")
  class MarketDataConfigTests {

    @Test
    @DisplayName("Should create config with builder")
    void testBuilder() {
      MarketDataConfig config =
          MarketDataConfig.builder()
              .provider(DataProvider.YAHOO_FINANCE)
              .apiKey("test-key")
              .rateLimitPerMinute(10)
              .enableCaching(false)
              .cacheTTLSeconds(600)
              .maxBarsPerRequest(2000)
              .requestTimeout(60000)
              .enableRetry(false)
              .maxRetryAttempts(5)
              .enableLogging(false)
              .baseUrlOverride("https://custom.api.com")
              .build();

      assertEquals(DataProvider.YAHOO_FINANCE, config.getProvider());
      assertEquals("test-key", config.getApiKey());
      assertEquals(10, config.getRateLimitPerMinute());
      assertFalse(config.isEnableCaching());
      assertEquals(600, config.getCacheTTLSeconds());
      assertEquals(2000, config.getMaxBarsPerRequest());
      assertEquals(60000, config.getRequestTimeout());
      assertFalse(config.isEnableRetry());
      assertEquals(5, config.getMaxRetryAttempts());
      assertFalse(config.isEnableLogging());
      assertEquals("https://custom.api.com", config.getBaseUrlOverride());
    }

    @Test
    @DisplayName("Should use default values")
    void testDefaults() {
      MarketDataConfig config = MarketDataConfig.defaults();

      assertEquals(DataProvider.ALPHA_VANTAGE, config.getProvider());
      assertEquals("demo", config.getApiKey());
      assertEquals(5, config.getRateLimitPerMinute());
      assertTrue(config.isEnableCaching());
      assertEquals(900, config.getCacheTTLSeconds());
      assertEquals(1000, config.getMaxBarsPerRequest());
      assertEquals(30000, config.getRequestTimeout());
      assertTrue(config.isEnableRetry());
      assertEquals(3, config.getMaxRetryAttempts());
      assertTrue(config.isEnableLogging());
      assertNull(config.getBaseUrlOverride());
    }

    @Test
    @DisplayName("Should create Alpha Vantage config")
    void testAlphaVantage() {
      MarketDataConfig config = MarketDataConfig.alphaVantage("my-api-key");

      assertEquals(DataProvider.ALPHA_VANTAGE, config.getProvider());
      assertEquals("my-api-key", config.getApiKey());
    }

    @Test
    @DisplayName("Should create Yahoo Finance config")
    void testYahooFinance() {
      MarketDataConfig config = MarketDataConfig.yahooFinance();

      assertEquals(DataProvider.YAHOO_FINANCE, config.getProvider());
      assertEquals(60, config.getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should create Binance config")
    void testBinance() {
      MarketDataConfig config = MarketDataConfig.binance("binance-key");

      assertEquals(DataProvider.BINANCE, config.getProvider());
      assertEquals("binance-key", config.getApiKey());
      assertEquals(20, config.getRateLimitPerMinute());
    }

    @Test
    @DisplayName("Should create Mock config")
    void testMock() {
      MarketDataConfig config = MarketDataConfig.mock();

      assertEquals(DataProvider.MOCK, config.getProvider());
      assertFalse(config.isEnableCaching());
      assertTrue(config.isEnableLogging());
    }

    @Test
    @DisplayName("Should support setters")
    void testSetters() {
      MarketDataConfig config = MarketDataConfig.defaults();

      config.setProvider(DataProvider.BINANCE);
      config.setApiKey("new-key");
      config.setRateLimitPerMinute(15);
      config.setEnableCaching(false);
      config.setCacheTTLSeconds(1200);
      config.setMaxBarsPerRequest(500);
      config.setRequestTimeout(45000);
      config.setEnableRetry(false);
      config.setMaxRetryAttempts(2);
      config.setEnableLogging(false);
      config.setBaseUrlOverride("https://override.com");

      assertEquals(DataProvider.BINANCE, config.getProvider());
      assertEquals("new-key", config.getApiKey());
      assertEquals(15, config.getRateLimitPerMinute());
      assertFalse(config.isEnableCaching());
      assertEquals(1200, config.getCacheTTLSeconds());
      assertEquals(500, config.getMaxBarsPerRequest());
      assertEquals(45000, config.getRequestTimeout());
      assertFalse(config.isEnableRetry());
      assertEquals(2, config.getMaxRetryAttempts());
      assertFalse(config.isEnableLogging());
      assertEquals("https://override.com", config.getBaseUrlOverride());
    }

    @Test
    @DisplayName("Should implement equals and hashCode")
    void testEqualsAndHashCode() {
      MarketDataConfig config1 =
          MarketDataConfig.builder().apiKey("key").rateLimitPerMinute(5).build();

      MarketDataConfig config2 =
          MarketDataConfig.builder().apiKey("key").rateLimitPerMinute(5).build();

      assertEquals(config1, config2);
      assertEquals(config1.hashCode(), config2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void testToString() {
      MarketDataConfig config = MarketDataConfig.defaults();
      String str = config.toString();

      assertTrue(str.contains("provider"));
      assertTrue(str.contains("apiKey"));
    }
  }
}
