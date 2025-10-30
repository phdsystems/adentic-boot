# Examples - Documentation Link Verifier Tool

**Date:** 2025-10-25
**Version:** 0.3.0

---

## Table of Contents

- [Overview](#overview)
- [Basic Examples](#basic-examples)
- [Advanced Examples](#advanced-examples)
- [Agent Integration Examples](#agent-integration-examples)
- [CI/CD Integration Examples](#cicd-integration-examples)
- [Spring Boot Integration Examples](#spring-boot-integration-examples)
- [Quarkus Integration Examples](#quarkus-integration-examples)
- [Error Handling Examples](#error-handling-examples)
- [Custom Configuration Examples](#custom-configuration-examples)
- [Complete Application Examples](#complete-application-examples)

---

## Overview

This document provides **copy-paste ready** examples for common use cases of the Documentation Link Verifier Tool.

**Example Categories:**
- Basic usage patterns
- Advanced verification scenarios
- Agent integration
- CI/CD pipelines
- Framework-specific integration
- Error handling strategies
- Custom configurations
- Complete applications

---

## Basic Examples

### Example 1: Verify a Single File

```java
import dev.adeengineer.adentic.tool.DocumentationLinkVerifierTool;
import dev.adeengineer.adentic.tool.model.LinkVerificationResult;
import jakarta.inject.Inject;
import org.springframework.stereotype.Component;

@Component
public class SimpleVerification {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void verifyReadme() {
        LinkVerificationResult result = verifier
            .verifyFile("README.md")
            .block();

        if (result.isValid()) {
            System.out.println("✅ All links in README.md are valid!");
        } else {
            System.out.println("❌ Found " + result.getBrokenLinks().size() + " broken links");
            result.getBrokenLinks().forEach(broken -> {
                System.out.println("  - Line " + broken.getLink().getLineNumber() + ": " + broken.getReason());
            });
        }
    }
}
```

**Output:**

```
✅ All links in README.md are valid!
```

---

### Example 2: Verify a Directory

```java
import dev.adeengineer.adentic.tool.model.DirectoryVerificationResult;

@Component
public class DirectoryVerification {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void verifyDocs() {
        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)  // recursive
            .block();

        // Print summary
        System.out.println(result.getSummary());

        // Print detailed report if there are broken links
        if (!result.isValid()) {
            System.out.println("\n" + result.getDetailedReport());
        }
    }
}
```

**Output:**

```
✅ Verified 47 files (124 links total) - All valid! [2341ms]
```

---

### Example 3: Extract Links from Content

```java
import dev.adeengineer.adentic.tool.model.MarkdownLink;
import java.util.List;

@Component
public class LinkExtraction {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void extractLinksExample() {
        String markdown = """
            # Documentation

            See [Architecture](architecture.md) for design.
            Visit [Homepage](https://example.com) for details.
            Jump to [Overview](#overview) section.
            """;

        List<MarkdownLink> links = verifier
            .extractLinks(markdown)
            .block();

        links.forEach(link -> {
            System.out.println(link.getType() + ": " + link.getTarget());
        });
    }
}
```

**Output:**

```
FILE: architecture.md
HTTP: https://example.com
ANCHOR: #overview
```

---

### Example 4: Validate a Single Link

```java
@Component
public class SingleLinkValidation {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void validateLinkExample() {
        boolean isValid = verifier
            .validateLink("doc/README.md", "architecture.md")
            .block();

        if (isValid) {
            System.out.println("✅ Link is valid");
        } else {
            System.out.println("❌ Link is broken");
        }
    }
}
```

---

## Advanced Examples

### Example 5: Fast Pre-Commit Verification

```java
import dev.adeengineer.adentic.tool.config.LinkVerifierConfig;

@Component
public class PreCommitHook {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    /**
     * Fast verification for pre-commit hooks.
     * Skips HTTP verification to ensure speed.
     */
    public boolean preCommitCheck() {
        // Configure for speed
        verifier.setConfig(LinkVerifierConfig.fast());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        if (!result.isValid()) {
            System.err.println("╔════════════════════════════════════════╗");
            System.err.println("║  ❌ COMMIT BLOCKED: BROKEN LINKS       ║");
            System.err.println("╚════════════════════════════════════════╝");
            System.err.println();
            System.err.println(result.getDetailedReport());
            System.err.println();
            System.err.println("Fix broken links before committing.");
            return false;
        }

        System.out.println("✅ Documentation links verified successfully");
        return true;
    }
}
```

**Usage in Git Hook:**

```bash
#!/bin/bash
# .git/hooks/pre-commit

java -jar adentic-boot.jar verify-docs || exit 1
```

---

### Example 6: Thorough CI/CD Verification

```java
@Component
public class CIPipeline {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    /**
     * Comprehensive verification for CI/CD pipelines.
     * Includes HTTP verification with retries.
     */
    public void nightlyDocumentationCheck() {
        // Configure for thoroughness
        verifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        // Log detailed statistics
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Documentation Verification Report");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Total Files:   " + result.getTotalFiles());
        System.out.println("Total Links:   " + result.getTotalLinks());
        System.out.println("Broken Links:  " + result.getTotalBrokenLinks());
        System.out.println("Status:        " + (result.isValid() ? "✅ PASS" : "❌ FAIL"));
        System.out.println("═══════════════════════════════════════");

        if (!result.isValid()) {
            System.out.println("\nBroken Links by File:");
            result.getFilesWithBrokenLinks().forEach(file -> {
                System.out.println("\n📄 " + file);
                result.getFileResults().stream()
                    .filter(r -> r.getFilePath().equals(file))
                    .findFirst()
                    .ifPresent(r -> {
                        r.getBrokenLinks().forEach(broken -> {
                            System.out.println("  ❌ Line " + broken.getLink().getLineNumber() + ": " + broken.getReason());
                            if (broken.getSuggestion() != null) {
                                System.out.println("     💡 " + broken.getSuggestion());
                            }
                        });
                    });
            });

            throw new RuntimeException("Documentation has broken links!");
        }
    }
}
```

**Output:**

```
═══════════════════════════════════════
  Documentation Verification Report
═══════════════════════════════════════
Total Files:   47
Total Links:   124
Broken Links:  0
Status:        ✅ PASS
═══════════════════════════════════════
```

---

### Example 7: Parallel Verification

```java
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ParallelVerification {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    /**
     * Verify multiple documentation sets in parallel.
     */
    public void verifyMultipleSets() {
        List<String> docPaths = List.of(
            "doc/1-planning/",
            "doc/2-analysis/",
            "doc/3-design/",
            "doc/4-development/"
        );

        List<DirectoryVerificationResult> results = Flux.fromIterable(docPaths)
            .flatMap(path -> verifier.verifyDirectory(path, false))
            .collectList()
            .block();

        // Aggregate results
        int totalFiles = results.stream().mapToInt(DirectoryVerificationResult::getTotalFiles).sum();
        int totalLinks = results.stream().mapToInt(DirectoryVerificationResult::getTotalLinks).sum();
        int totalBroken = results.stream().mapToInt(DirectoryVerificationResult::getTotalBrokenLinks).sum();

        System.out.println("Verified " + totalFiles + " files with " + totalLinks + " links");
        System.out.println("Found " + totalBroken + " broken links");
    }
}
```

---

### Example 8: Conditional Verification

```java
@Component
public class ConditionalVerification {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    /**
     * Different verification strategies based on environment.
     */
    public void smartVerification(String environment) {
        LinkVerifierConfig config = switch (environment) {
            case "dev" -> LinkVerifierConfig.fast();
            case "staging" -> LinkVerifierConfig.defaults();
            case "production" -> LinkVerifierConfig.thorough();
            default -> LinkVerifierConfig.filesOnly();
        };

        verifier.setConfig(config);
        System.out.println("Using " + environment + " verification configuration");

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        System.out.println(result.getSummary());
    }
}
```

---

## Agent Integration Examples

### Example 9: Documentation Maintenance Agent

```java
import dev.adeengineer.adentic.annotation.AgentService;
import dev.adeengineer.adentic.annotation.LLM;
import dev.adeengineer.adentic.provider.TextGenerationProvider;

@AgentService(
    name = "doc-maintenance-agent",
    description = "Validates and suggests fixes for broken documentation links"
)
public class DocumentationMaintenanceAgent {

    @Inject
    private DocumentationLinkVerifierTool linkVerifier;

    @Inject
    @LLM(name = "claude")
    private TextGenerationProvider llm;

    /**
     * Validates documentation and suggests fixes for broken links.
     */
    public Mono<AgentResult> validateAndFixDocumentation(String docsPath) {
        return linkVerifier.verifyDirectory(docsPath, true)
            .flatMap(result -> {
                if (result.isValid()) {
                    return Mono.just(AgentResult.success(
                        "✅ All documentation links are valid! " +
                        "Verified " + result.getTotalLinks() + " links across " +
                        result.getTotalFiles() + " files."
                    ));
                }

                // Build prompt for LLM to suggest fixes
                String prompt = buildFixPrompt(result);

                return llm.generate(prompt)
                    .map(suggestions -> AgentResult.builder()
                        .status("broken_links_found")
                        .summary("Found " + result.getTotalBrokenLinks() + " broken links")
                        .suggestions(suggestions)
                        .brokenLinks(result.getFilesWithBrokenLinks())
                        .build());
            });
    }

    private String buildFixPrompt(DirectoryVerificationResult result) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("I found broken documentation links. Please suggest fixes:\n\n");

        result.getFileResults().stream()
            .filter(r -> !r.isValid())
            .forEach(fileResult -> {
                prompt.append("File: ").append(fileResult.getFilePath()).append("\n");
                fileResult.getBrokenLinks().forEach(broken -> {
                    prompt.append("  - Line ").append(broken.getLink().getLineNumber())
                          .append(": [").append(broken.getLink().getText()).append("](")
                          .append(broken.getLink().getTarget()).append(")\n");
                    prompt.append("    Reason: ").append(broken.getReason()).append("\n");
                });
                prompt.append("\n");
            });

        return prompt.toString();
    }
}
```

**Usage:**

```java
@Inject
private DocumentationMaintenanceAgent agent;

public void maintainDocs() {
    AgentResult result = agent
        .validateAndFixDocumentation("doc/")
        .block();

    System.out.println(result.getSummary());
    if (result.hasSuggestions()) {
        System.out.println("\nSuggested Fixes:");
        System.out.println(result.getSuggestions());
    }
}
```

---

### Example 10: Scheduled Documentation Validation Agent

```java
import io.quarkus.scheduler.Scheduled;

@AgentService(name = "scheduled-doc-validator")
public class ScheduledDocumentationValidator {

    @Inject
    private DocumentationLinkVerifierTool linkVerifier;

    @Inject
    private NotificationService notificationService;

    /**
     * Runs every night at 2 AM to validate documentation.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void nightlyValidation() {
        linkVerifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = linkVerifier
            .verifyDirectory("doc/", true)
            .block();

        if (!result.isValid()) {
            // Send notification
            notificationService.sendAlert(
                "Documentation Broken Links",
                "Found " + result.getTotalBrokenLinks() + " broken links in documentation.\n\n" +
                result.getDetailedReport()
            );
        }
    }
}
```

---

### Example 11: Interactive Documentation Assistant Agent

```java
@AgentService(name = "doc-assistant")
public class InteractiveDocumentationAssistant {

    @Inject
    private DocumentationLinkVerifierTool linkVerifier;

    @Inject
    @LLM
    private TextGenerationProvider llm;

    /**
     * Answers questions about documentation link health.
     */
    public Mono<String> answerQuestion(String question) {
        return linkVerifier.verifyDirectory("doc/", true)
            .flatMap(result -> {
                String context = String.format(
                    "Documentation Statistics:\n" +
                    "- Total Files: %d\n" +
                    "- Total Links: %d\n" +
                    "- Broken Links: %d\n" +
                    "- Status: %s\n\n" +
                    "User Question: %s",
                    result.getTotalFiles(),
                    result.getTotalLinks(),
                    result.getTotalBrokenLinks(),
                    result.isValid() ? "Healthy" : "Has Issues",
                    question
                );

                return llm.generate(context);
            });
    }
}
```

**Usage:**

```java
agent.answerQuestion("Are there any broken links in the design documentation?")
     .subscribe(answer -> System.out.println(answer));
```

---

## CI/CD Integration Examples

### Example 12: GitHub Actions Integration

```yaml
# .github/workflows/verify-docs.yml
name: Verify Documentation Links

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  verify-links:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'

      - name: Build project
        run: ./mvnw clean package -DskipTests

      - name: Verify documentation links
        run: |
          java -jar target/adentic-boot.jar verify-docs || exit 1

      - name: Upload verification report
        if: failure()
        uses: actions/upload-artifact@v3
        with:
          name: link-verification-report
          path: link-verification-report.txt
```

**Java CLI Implementation:**

```java
@Command(name = "verify-docs", description = "Verify documentation links")
public class VerifyDocsCommand implements Runnable {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    @Override
    public void run() {
        verifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        // Write report to file
        try {
            Files.writeString(
                Path.of("link-verification-report.txt"),
                result.getDetailedReport()
            );
        } catch (IOException e) {
            System.err.println("Failed to write report: " + e.getMessage());
        }

        // Exit with appropriate code
        if (!result.isValid()) {
            System.err.println("❌ Documentation verification failed");
            System.exit(1);
        } else {
            System.out.println("✅ Documentation verification passed");
            System.exit(0);
        }
    }
}
```

---

### Example 13: GitLab CI Integration

```yaml
# .gitlab-ci.yml
verify-docs:
  stage: test
  image: maven:3.9-eclipse-temurin-21
  script:
    - mvn clean package -DskipTests
    - java -jar target/adentic-boot.jar verify-docs
  artifacts:
    when: on_failure
    paths:
      - link-verification-report.txt
    expire_in: 1 week
  rules:
    - if: '$CI_PIPELINE_SOURCE == "merge_request_event"'
    - if: '$CI_COMMIT_BRANCH == "main"'
```

---

### Example 14: Jenkins Pipeline Integration

```groovy
// Jenkinsfile
pipeline {
    agent any

    stages {
        stage('Build') {
            steps {
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Verify Documentation') {
            steps {
                script {
                    def result = sh(
                        script: 'java -jar target/adentic-boot.jar verify-docs',
                        returnStatus: true
                    )

                    if (result != 0) {
                        error "Documentation has broken links"
                    }
                }
            }
        }
    }

    post {
        failure {
            archiveArtifacts artifacts: 'link-verification-report.txt'
        }
    }
}
```

---

## Spring Boot Integration Examples

### Example 15: Spring Boot Configuration

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {

    @Bean
    public DocumentationLinkVerifierTool linkVerifier() {
        DocumentationLinkVerifierTool tool = new DocumentationLinkVerifierTool();
        tool.setConfig(LinkVerifierConfig.defaults());
        return tool;
    }

    @Bean
    public DocumentationVerificationService verificationService(
            DocumentationLinkVerifierTool linkVerifier) {
        return new DocumentationVerificationService(linkVerifier);
    }
}
```

---

### Example 16: Spring Boot REST Endpoint

```java
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/docs")
public class DocumentationVerificationController {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    @GetMapping("/verify")
    public Mono<VerificationResponse> verifyDocumentation(
            @RequestParam(defaultValue = "doc/") String path,
            @RequestParam(defaultValue = "true") boolean recursive) {

        return verifier.verifyDirectory(path, recursive)
            .map(result -> VerificationResponse.builder()
                .valid(result.isValid())
                .totalFiles(result.getTotalFiles())
                .totalLinks(result.getTotalLinks())
                .brokenLinks(result.getTotalBrokenLinks())
                .files(result.getFilesWithBrokenLinks())
                .build());
    }

    @PostMapping("/verify-file")
    public Mono<LinkVerificationResult> verifyFile(@RequestBody FileVerificationRequest request) {
        return verifier.verifyFile(request.getFilePath());
    }
}
```

**Usage:**

```bash
# Verify all documentation
curl http://localhost:8080/api/docs/verify

# Verify specific directory
curl http://localhost:8080/api/docs/verify?path=doc/3-design&recursive=false

# Verify single file
curl -X POST http://localhost:8080/api/docs/verify-file \
  -H "Content-Type: application/json" \
  -d '{"filePath": "doc/architecture.md"}'
```

---

### Example 17: Spring Boot Scheduled Task

```java
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledDocumentationVerification {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    @Inject
    private EmailService emailService;

    /**
     * Runs every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyVerification() {
        verifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        if (!result.isValid()) {
            emailService.sendEmail(
                "team@example.com",
                "Documentation Broken Links Alert",
                result.getDetailedReport()
            );
        }
    }
}
```

---

## Quarkus Integration Examples

### Example 18: Quarkus Configuration

```java
import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public class QuarkusDocumentationConfig {

    @Produces
    @ApplicationScoped
    public DocumentationLinkVerifierTool linkVerifier() {
        DocumentationLinkVerifierTool tool = new DocumentationLinkVerifierTool();
        tool.setConfig(LinkVerifierConfig.defaults());
        return tool;
    }

    @Startup
    public void onStartup() {
        System.out.println("Documentation Link Verifier Tool initialized");
    }
}
```

---

### Example 19: Quarkus REST Endpoint

```java
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import io.smallrye.mutiny.Uni;

@Path("/api/docs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class QuarkusDocumentationResource {

    @Inject
    DocumentationLinkVerifierTool verifier;

    @GET
    @Path("/verify")
    public Uni<VerificationResponse> verifyDocumentation(
            @QueryParam("path") @DefaultValue("doc/") String path,
            @QueryParam("recursive") @DefaultValue("true") boolean recursive) {

        return Uni.createFrom().item(() -> verifier
            .verifyDirectory(path, recursive)
            .block())
            .map(result -> new VerificationResponse(result));
    }

    @POST
    @Path("/verify-file")
    public Uni<LinkVerificationResult> verifyFile(FileVerificationRequest request) {
        return Uni.createFrom().item(() -> verifier
            .verifyFile(request.getFilePath())
            .block());
    }
}
```

---

### Example 20: Quarkus Scheduled Task

```java
import io.quarkus.scheduler.Scheduled;

@ApplicationScoped
public class QuarkusScheduledVerification {

    @Inject
    DocumentationLinkVerifierTool verifier;

    @Scheduled(cron = "0 0 2 * * ?")
    public void nightlyVerification() {
        verifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .block();

        if (!result.isValid()) {
            System.err.println("❌ Documentation has " + result.getTotalBrokenLinks() + " broken links");
            System.err.println(result.getDetailedReport());
        } else {
            System.out.println("✅ Documentation is healthy");
        }
    }
}
```

---

## Error Handling Examples

### Example 21: Comprehensive Error Handling

```java
import dev.adeengineer.adentic.tool.model.BrokenLink;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ErrorHandlingExample {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void verifyWithErrorHandling() {
        try {
            DirectoryVerificationResult result = verifier
                .verifyDirectory("doc/", true)
                .block();

            if (!result.isValid()) {
                handleBrokenLinks(result);
            } else {
                System.out.println("✅ All links valid");
            }

        } catch (Exception e) {
            System.err.println("❌ Verification failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleBrokenLinks(DirectoryVerificationResult result) {
        // Group broken links by type
        Map<String, List<BrokenLink>> brokenByType = result.getFileResults().stream()
            .flatMap(r -> r.getBrokenLinks().stream())
            .collect(Collectors.groupingBy(broken -> {
                if (broken.getHttpStatusCode() != null) {
                    return "HTTP_ERROR";
                } else if (broken.getReason().contains("not found")) {
                    return "FILE_NOT_FOUND";
                } else if (broken.getReason().contains("anchor")) {
                    return "ANCHOR_NOT_FOUND";
                } else {
                    return "OTHER";
                }
            }));

        // Handle each type differently
        handleHttpErrors(brokenByType.getOrDefault("HTTP_ERROR", List.of()));
        handleFileNotFound(brokenByType.getOrDefault("FILE_NOT_FOUND", List.of()));
        handleAnchorNotFound(brokenByType.getOrDefault("ANCHOR_NOT_FOUND", List.of()));
        handleOtherErrors(brokenByType.getOrDefault("OTHER", List.of()));
    }

    private void handleHttpErrors(List<BrokenLink> httpErrors) {
        if (httpErrors.isEmpty()) return;

        System.out.println("\n🌐 HTTP Errors:");
        httpErrors.forEach(broken -> {
            System.out.println("  ❌ " + broken.getLink().getTarget() +
                             " (HTTP " + broken.getHttpStatusCode() + ")");
            System.out.println("     Line: " + broken.getLink().getLineNumber());
            System.out.println("     💡 " + broken.getSuggestion());
        });
    }

    private void handleFileNotFound(List<BrokenLink> fileErrors) {
        if (fileErrors.isEmpty()) return;

        System.out.println("\n📁 File Not Found Errors:");
        fileErrors.forEach(broken -> {
            System.out.println("  ❌ " + broken.getLink().getTarget());
            System.out.println("     Line: " + broken.getLink().getLineNumber());
            System.out.println("     💡 " + broken.getSuggestion());
        });
    }

    private void handleAnchorNotFound(List<BrokenLink> anchorErrors) {
        if (anchorErrors.isEmpty()) return;

        System.out.println("\n🔗 Anchor Not Found Errors:");
        anchorErrors.forEach(broken -> {
            System.out.println("  ❌ " + broken.getLink().getTarget());
            System.out.println("     Line: " + broken.getLink().getLineNumber());
            System.out.println("     💡 " + broken.getSuggestion());
        });
    }

    private void handleOtherErrors(List<BrokenLink> otherErrors) {
        if (otherErrors.isEmpty()) return;

        System.out.println("\n⚠️  Other Errors:");
        otherErrors.forEach(broken -> {
            System.out.println("  ❌ " + broken.getLink().getTarget());
            System.out.println("     Reason: " + broken.getReason());
        });
    }
}
```

---

### Example 22: Retry Logic for Transient Errors

```java
import reactor.util.retry.Retry;
import java.time.Duration;

@Component
public class RetryExample {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void verifyWithRetry() {
        verifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                .filter(throwable -> throwable instanceof IOException)
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                    new RuntimeException("Verification failed after 3 retries", retrySignal.failure())
                ))
            .block();

        System.out.println(result.getSummary());
    }
}
```

---

### Example 23: Fallback Configuration

```java
@Component
public class FallbackExample {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void verifyWithFallback() {
        // Try thorough verification first
        verifier.setConfig(LinkVerifierConfig.thorough());

        DirectoryVerificationResult result = verifier
            .verifyDirectory("doc/", true)
            .timeout(Duration.ofSeconds(30))
            .onErrorResume(throwable -> {
                // Fallback to fast verification if timeout
                System.out.println("⚠️  Thorough verification timed out, falling back to fast mode");
                verifier.setConfig(LinkVerifierConfig.fast());
                return verifier.verifyDirectory("doc/", true);
            })
            .block();

        System.out.println(result.getSummary());
    }
}
```

---

## Custom Configuration Examples

### Example 24: Custom Timeout Configuration

```java
@Component
public class CustomTimeoutExample {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    public void configureCustomTimeout() {
        LinkVerifierConfig config = LinkVerifierConfig.builder()
            .httpTimeoutMs(15000)  // 15 seconds for slow external sites
            .httpRetries(3)
            .verifyHttpLinks(true)
            .verifyFileLinks(true)
            .verifyAnchorLinks(true)
            .build();

        verifier.setConfig(config);

        LinkVerificationResult result = verifier
            .verifyFile("doc/external-resources.md")
            .block();

        System.out.println(result.getSummary());
    }
}
```

---

### Example 25: Environment-Specific Configuration

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;

@Component
public class EnvironmentSpecificConfig {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    @Value("${app.environment:dev}")
    private String environment;

    @PostConstruct
    public void configure() {
        LinkVerifierConfig config = switch (environment) {
            case "dev" -> LinkVerifierConfig.builder()
                .httpTimeoutMs(2000)
                .verifyHttpLinks(false)
                .cacheHttpResults(true)
                .build();

            case "staging" -> LinkVerifierConfig.builder()
                .httpTimeoutMs(5000)
                .httpRetries(2)
                .verifyHttpLinks(true)
                .build();

            case "production" -> LinkVerifierConfig.builder()
                .httpTimeoutMs(10000)
                .httpRetries(3)
                .verifyHttpLinks(true)
                .followRedirects(true)
                .build();

            default -> LinkVerifierConfig.defaults();
        };

        verifier.setConfig(config);
        System.out.println("Configured link verifier for environment: " + environment);
    }
}
```

---

### Example 26: Multi-Configuration Service

```java
@Component
public class MultiConfigService {

    private final DocumentationLinkVerifierTool fastVerifier;
    private final DocumentationLinkVerifierTool thoroughVerifier;

    public MultiConfigService() {
        // Create two instances with different configs
        this.fastVerifier = new DocumentationLinkVerifierTool();
        this.fastVerifier.setConfig(LinkVerifierConfig.fast());

        this.thoroughVerifier = new DocumentationLinkVerifierTool();
        this.thoroughVerifier.setConfig(LinkVerifierConfig.thorough());
    }

    public void quickCheck(String path) {
        DirectoryVerificationResult result = fastVerifier
            .verifyDirectory(path, true)
            .block();

        System.out.println("Quick check: " + result.getSummary());
    }

    public void comprehensiveCheck(String path) {
        DirectoryVerificationResult result = thoroughVerifier
            .verifyDirectory(path, true)
            .block();

        System.out.println("Comprehensive check: " + result.getSummary());
    }
}
```

---

## Complete Application Examples

### Example 27: CLI Application

```java
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
    name = "doc-verifier",
    description = "Documentation Link Verification Tool",
    mixinStandardHelpOptions = true,
    version = "1.0"
)
public class DocumentationVerifierCLI implements Runnable {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    @Parameters(
        index = "0",
        description = "Path to documentation directory or file"
    )
    private String path;

    @Option(
        names = {"-r", "--recursive"},
        description = "Verify directory recursively"
    )
    private boolean recursive = true;

    @Option(
        names = {"-m", "--mode"},
        description = "Verification mode: fast, default, thorough, files-only"
    )
    private String mode = "default";

    @Option(
        names = {"-o", "--output"},
        description = "Output file for detailed report"
    )
    private String outputFile;

    @Override
    public void run() {
        // Configure based on mode
        LinkVerifierConfig config = switch (mode) {
            case "fast" -> LinkVerifierConfig.fast();
            case "thorough" -> LinkVerifierConfig.thorough();
            case "files-only" -> LinkVerifierConfig.filesOnly();
            default -> LinkVerifierConfig.defaults();
        };

        verifier.setConfig(config);

        // Determine if path is file or directory
        Path filePath = Path.of(path);
        if (Files.isDirectory(filePath)) {
            verifyDirectory();
        } else {
            verifyFile();
        }
    }

    private void verifyFile() {
        LinkVerificationResult result = verifier
            .verifyFile(path)
            .block();

        printFileSummary(result);

        if (outputFile != null) {
            writeReport(result.getDetailedReport());
        }

        System.exit(result.isValid() ? 0 : 1);
    }

    private void verifyDirectory() {
        DirectoryVerificationResult result = verifier
            .verifyDirectory(path, recursive)
            .block();

        printDirectorySummary(result);

        if (outputFile != null) {
            writeReport(result.getDetailedReport());
        }

        System.exit(result.isValid() ? 0 : 1);
    }

    private void printFileSummary(LinkVerificationResult result) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  File Verification: " + result.getFilePath());
        System.out.println("═══════════════════════════════════════");
        System.out.println("Total Links:   " + result.getTotalLinks());
        System.out.println("Broken Links:  " + result.getBrokenLinks().size());
        System.out.println("Status:        " + (result.isValid() ? "✅ PASS" : "❌ FAIL"));
        System.out.println("═══════════════════════════════════════");

        if (!result.isValid()) {
            System.out.println("\nBroken Links:");
            result.getBrokenLinks().forEach(broken -> {
                System.out.println("  ❌ Line " + broken.getLink().getLineNumber() + ": " + broken.getReason());
            });
        }
    }

    private void printDirectorySummary(DirectoryVerificationResult result) {
        System.out.println("═══════════════════════════════════════");
        System.out.println("  Directory Verification: " + path);
        System.out.println("═══════════════════════════════════════");
        System.out.println("Total Files:   " + result.getTotalFiles());
        System.out.println("Total Links:   " + result.getTotalLinks());
        System.out.println("Broken Links:  " + result.getTotalBrokenLinks());
        System.out.println("Status:        " + (result.isValid() ? "✅ PASS" : "❌ FAIL"));
        System.out.println("═══════════════════════════════════════");

        if (!result.isValid()) {
            System.out.println("\nFiles with Broken Links:");
            result.getFilesWithBrokenLinks().forEach(file -> {
                System.out.println("  📄 " + file);
            });
        }
    }

    private void writeReport(String report) {
        try {
            Files.writeString(Path.of(outputFile), report);
            System.out.println("\n✅ Detailed report written to: " + outputFile);
        } catch (IOException e) {
            System.err.println("❌ Failed to write report: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new DocumentationVerifierCLI()).execute(args);
        System.exit(exitCode);
    }
}
```

**Usage:**

```bash
# Verify directory (default mode)
doc-verifier doc/

# Verify file
doc-verifier doc/architecture.md

# Fast mode
doc-verifier doc/ --mode fast

# Thorough mode with output
doc-verifier doc/ --mode thorough --output report.txt

# Non-recursive
doc-verifier doc/3-design/ --no-recursive
```

---

### Example 28: Web Dashboard Application

```java
@RestController
@RequestMapping("/api/verification")
public class VerificationDashboardController {

    @Inject
    private DocumentationLinkVerifierTool verifier;

    @GetMapping("/status")
    public Mono<DashboardStatus> getStatus() {
        return verifier.verifyDirectory("doc/", true)
            .map(result -> DashboardStatus.builder()
                .healthy(result.isValid())
                .totalFiles(result.getTotalFiles())
                .totalLinks(result.getTotalLinks())
                .brokenLinks(result.getTotalBrokenLinks())
                .lastChecked(Instant.now())
                .build());
    }

    @GetMapping("/history")
    public List<VerificationHistory> getHistory() {
        // Return historical verification results
        // (requires persistence layer)
        return verificationHistoryRepository.findAll();
    }

    @PostMapping("/verify-now")
    public Mono<DirectoryVerificationResult> verifyNow() {
        verifier.setConfig(LinkVerifierConfig.thorough());
        return verifier.verifyDirectory("doc/", true)
            .doOnSuccess(this::saveVerificationResult);
    }

    private void saveVerificationResult(DirectoryVerificationResult result) {
        VerificationHistory history = VerificationHistory.builder()
            .timestamp(Instant.now())
            .totalFiles(result.getTotalFiles())
            .totalLinks(result.getTotalLinks())
            .brokenLinks(result.getTotalBrokenLinks())
            .valid(result.isValid())
            .build();

        verificationHistoryRepository.save(history);
    }
}
```

**Frontend (React):**

```typescript
// Dashboard.tsx
import React, { useEffect, useState } from 'react';

interface DashboardStatus {
  healthy: boolean;
  totalFiles: number;
  totalLinks: number;
  brokenLinks: number;
  lastChecked: string;
}

export const DocumentationDashboard: React.FC = () => {
  const [status, setStatus] = useState<DashboardStatus | null>(null);

  useEffect(() => {
    fetch('/api/verification/status')
      .then(res => res.json())
      .then(data => setStatus(data));
  }, []);

  if (!status) return <div>Loading...</div>;

  return (
    <div className="dashboard">
      <h1>Documentation Health Dashboard</h1>

      <div className={`status-card ${status.healthy ? 'healthy' : 'unhealthy'}`}>
        <h2>{status.healthy ? '✅ Healthy' : '❌ Issues Found'}</h2>
        <p>Last checked: {new Date(status.lastChecked).toLocaleString()}</p>
      </div>

      <div className="stats">
        <div className="stat">
          <h3>Total Files</h3>
          <p>{status.totalFiles}</p>
        </div>
        <div className="stat">
          <h3>Total Links</h3>
          <p>{status.totalLinks}</p>
        </div>
        <div className="stat">
          <h3>Broken Links</h3>
          <p>{status.brokenLinks}</p>
        </div>
      </div>

      <button onClick={() => fetch('/api/verification/verify-now', { method: 'POST' })}>
        Verify Now
      </button>
    </div>
  );
};
```

---

*Last Updated: 2025-10-25*
*Version: 0.3.0*
