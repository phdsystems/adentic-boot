plugins {
    java
    jacoco
}

group = "dev.adeengineer"
version = "0.2.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Import Adentic BOMs
    implementation(platform("dev.adeengineer.ee:adentic-ee-bom:1.0.0-SNAPSHOT"))

    // Adentic Core - Managed by BOM
    implementation("dev.adeengineer:adentic-core")

    // Javalin - Lightweight HTTP server
    implementation("io.javalin:javalin:6.1.3")

    // Jackson - JSON serialization (versions managed by BOM)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // SLF4J + Logback - Logging
    implementation("org.slf4j:slf4j-api")
    implementation("ch.qos.logback:logback-classic:1.5.6")

    // Lombok - Code generation
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    // Project Reactor - Reactive streams (version managed by BOM)
    implementation("io.projectreactor:reactor-core")

    // H2 Database - In-memory SQL database for testing and development
    implementation("com.h2database:h2:2.2.224")

    // Testing (versions managed by BOM)
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// JaCoCo Test Coverage Configuration
jacoco {
    toolVersion = "0.8.11"
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.60".toBigDecimal()
            }
            excludes = listOf(
                "dev.adeengineer.adentic.boot.AgenticApplication"
            )
        }
    }
}
