# Database Tool

**Version:** 0.1.0
**Category:** Database Tools
**Status:** Architecture Implemented (Provider Stubs)
**Date:** 2025-10-25

---

## TL;DR

**Tool Provider for database operations with pluggable backends**. Uses provider/service pattern supporting PostgreSQL (production SQL), MySQL (web apps), SQLite (embedded), MongoDB (NoSQL documents), and H2 (testing). **Benefits**: Choose best database for each use case, switch providers at runtime, consistent API across all backends. **Use cases**: Data persistence, querying, CRUD operations, transactions, schema management.

**Quick start:**

```java
@Inject
private DatabaseTool db;

// Use H2 for testing (default)
db.connect().block();
db.executeQuery("SELECT * FROM users").block();

// Switch to PostgreSQL for production
db.setConfig(DatabaseConfig.postgresql("localhost", 5432, "mydb", "user", "pass"));

// Insert data
db.insert("users", Map.of("name", "John", "age", 30)).block();
```

---

## Table of Contents

- [Prerequisites](#prerequisites)
- [Overview](#overview)
- [Provider/Service Pattern](#providerservice-pattern)
- [Providers](#providers)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Operations](#operations)
- [Use Cases](#use-cases)
- [Implementation Status](#implementation-status)

---

## Prerequisites

**Status:** ⚠️ Architecture implemented, provider dependencies required

The Database Tool architecture is complete with five provider options, but **you must add dependencies for the provider(s) you want to use**. Currently, all providers are stub implementations awaiting their respective dependencies.

### Core Framework Requirements (Already Satisfied)

- ✅ **Java 21** - Language requirement
- ✅ **Project Reactor** - For reactive Mono/Flux API (provided by adentic-core)
- ✅ **SLF4J + Logback** - For logging (provided by adentic-boot)

### Provider-Specific Dependencies

#### Option 1: PostgreSQL (Recommended for Production) ⭐

**Status:** Stub - requires dependency

**Best for:** Production applications, complex queries, ACID compliance

**Add to pom.xml:**

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.0</version>
</dependency>
```

**For Reactive (Optional):**

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>r2dbc-postgresql</artifactId>
    <version>1.0.2.RELEASE</version>
</dependency>
```

---

#### Option 2: MySQL

**Status:** Stub - requires dependency

**Best for:** Web applications, read-heavy workloads, wide compatibility

**Add to pom.xml:**

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>
```

---

#### Option 3: SQLite (Recommended for Embedded) ⭐

**Status:** Stub - requires dependency

**Best for:** Local storage, mobile apps, testing, single-user apps

**Add to pom.xml:**

```xml
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>
```

---

#### Option 4: MongoDB

**Status:** Stub - requires dependency

**Best for:** Flexible schemas, JSON-like documents, scalability

**Add to pom.xml:**

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-reactivestreams</artifactId>
    <version>4.11.1</version>
</dependency>
```

---

#### Option 5: H2 (Recommended for Testing) ⭐

**Status:** Stub - requires dependency

**Best for:** Testing, development, temporary data, in-memory

**Add to pom.xml:**

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>
```

---

### Connection Pool (Recommended for Production)

For production SQL databases, add HikariCP:

```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

---

## Overview

The Database Tool is a **Tool Provider** implementation using a **provider/service pattern** that enables AI agents to perform database operations with multiple backend providers.

**Purpose:** Provide flexible, powerful database capabilities:
- Execute SQL/NoSQL queries
- Perform CRUD operations (Create, Read, Update, Delete)
- Manage transactions
- Introspect schemas
- Switch databases at runtime

**Key Benefits:**
- ✅ **Provider/Service Pattern** - Pluggable backends
- ✅ **Five Providers** - PostgreSQL, MySQL, SQLite, MongoDB, H2
- ✅ **Runtime Switching** - Change provider dynamically
- ✅ **Consistent API** - Same interface across all providers
- ✅ **Async/Reactive** - Mono-based API
- ✅ **Agent-Ready** - `@Tool` annotation
- ✅ **Security** - Parameterized queries (SQL injection prevention)
- ✅ **Configuration Presets** - Quick setup for common scenarios

---

## Provider/Service Pattern

### Architecture

```
DatabaseTool (@Tool)
    │
    ├─→ DatabaseProvider (Interface)
    │       ├─→ PostgreSQLDatabaseProvider
    │       ├─→ MySQLDatabaseProvider
    │       ├─→ SQLiteDatabaseProvider
    │       ├─→ MongoDBDatabaseProvider
    │       └─→ H2DatabaseProvider
    │
    └─→ DatabaseConfig (Provider Selection)
```

---

## Providers

### PostgreSQL (Production SQL)

**Technology:** Advanced open-source RDBMS
**Status:** Stub Implementation

**Strengths:**
- ✅ ACID compliant
- ✅ Advanced features (JSON, full-text search, geospatial)
- ✅ Excellent performance
- ✅ Strong data integrity

**Configuration:**

```java
db.setConfig(DatabaseConfig.postgresql("localhost", 5432, "mydb", "user", "pass"));
```

---

### MySQL (Web Application Database)

**Technology:** Popular open-source RDBMS
**Status:** Stub Implementation

**Strengths:**
- ✅ Widely used and supported
- ✅ Good for read-heavy workloads
- ✅ Easy replication
- ✅ Large ecosystem

**Configuration:**

```java
db.setConfig(DatabaseConfig.mysql("localhost", 3306, "mydb", "root", "pass"));
```

---

### SQLite (Embedded Database)

**Technology:** File-based SQL database
**Status:** Stub Implementation

**Strengths:**
- ✅ Zero configuration
- ✅ Single file database
- ✅ Perfect for embedded use
- ✅ Cross-platform

**Configuration:**

```java
db.setConfig(DatabaseConfig.sqlite("/path/to/database.db"));
// Or in-memory:
db.setConfig(DatabaseConfig.sqliteMemory());
```

---

### MongoDB (Document Database)

**Technology:** Document-oriented NoSQL
**Status:** Stub Implementation

**Strengths:**
- ✅ Flexible schema
- ✅ JSON-like documents
- ✅ Horizontal scalability
- ✅ Rich query language

**Configuration:**

```java
db.setConfig(DatabaseConfig.mongodb("localhost", 27017, "mydb", "user", "pass"));
```

---

### H2 (In-Memory Testing)

**Technology:** Fast in-memory SQL database
**Status:** ✅ **Fully Implemented** (with PreparedStatement for SQL injection prevention)

**Strengths:**
- ✅ Extremely fast
- ✅ Perfect for testing
- ✅ SQL compatible
- ✅ No setup required
- ✅ Real JDBC connections
- ✅ PreparedStatement for SQL injection prevention

**Configuration:**

```java
db.setConfig(DatabaseConfig.h2Memory());
// Or file-based:
db.setConfig(DatabaseConfig.h2File("/path/to/db"));
```

---

## Quick Start

### 1. Inject the Tool

```java
import dev.adeengineer.adentic.tool.database.DatabaseTool;

@Component
public class MyDataService {
    @Inject
    private DatabaseTool db;
}
```

### 2. Connect and Query

```java
// H2 in-memory (default - perfect for testing)
db.connect().block();

// Execute query
QueryResult result = db.executeQuery(
    "SELECT * FROM users WHERE age > ?",
    18
).block();

// Process results
result.getRows().forEach(row -> {
    System.out.println(row.get("name"));
});
```

### 3. Insert Data

```java
// Insert single record
Map<String, Object> user = Map.of(
    "name", "John Doe",
    "age", 30,
    "email", "john@example.com"
);

Object userId = db.insert("users", user).block();

// Insert multiple records
List<Map<String, Object>> users = List.of(
    Map.of("name", "Alice", "age", 25),
    Map.of("name", "Bob", "age", 35)
);

Long inserted = db.insertBatch("users", users).block();
```

### 4. Update and Delete

```java
// Update by ID
db.update("users", userId, Map.of("age", 31)).block();

// Update with criteria
db.updateWhere(
    "users",
    Map.of("age", 25),  // criteria
    Map.of("status", "active")  // updates
).block();

// Delete by ID
db.delete("users", userId).block();

// Delete with criteria
db.deleteWhere("users", Map.of("status", "inactive")).block();
```

### 5. Switch Providers

```java
// Use PostgreSQL for production
db.setConfig(DatabaseConfig.production(
    "db.example.com",
    "prod_db",
    "app_user",
    "secret_password"
));
db.connect().block();

// Use SQLite for local development
db.setConfig(DatabaseConfig.sqlite("./local.db"));
db.connect().block();
```

---

## Configuration

### Configuration Presets

```java
// Default (H2 in-memory)
DatabaseConfig.defaults()

// PostgreSQL
DatabaseConfig.postgresql()
DatabaseConfig.postgresql("host", 5432, "db", "user", "pass")

// MySQL
DatabaseConfig.mysql()
DatabaseConfig.mysql("host", 3306, "db", "user", "pass")

// SQLite
DatabaseConfig.sqlite("/path/to/db.sqlite")
DatabaseConfig.sqliteMemory()

// MongoDB
DatabaseConfig.mongodb()
DatabaseConfig.mongodb("host", 27017, "db", "user", "pass")

// H2
DatabaseConfig.h2Memory()
DatabaseConfig.h2File("/path/to/db")

// Development
DatabaseConfig.development()

// Production (with SSL)
DatabaseConfig.production("host", "db", "user", "pass")

// Testing
DatabaseConfig.testing()
```

### Custom Configuration

```java
DatabaseConfig config = DatabaseConfig.builder()
    .provider(DatabaseProvider.POSTGRESQL)
    .host("localhost")
    .port(5432)
    .database("myapp")
    .username("appuser")
    .password("password")
    .poolSize(20)
    .connectionTimeout(10000)
    .useSSL(true)
    .enableQueryLogging(true)
    .build();

db.setConfig(config);
```

---

## Operations

### Query Operations

- `executeQuery(sql)` - Execute SELECT query
- `executeQuery(sql, params)` - Parameterized query
- `executeUpdate(sql)` - Execute UPDATE/DELETE
- `findAll(table)` - Get all records
- `findById(table, id)` - Get by ID
- `findWhere(table, criteria)` - Find with criteria
- `count(table)` - Count records
- `countWhere(table, criteria)` - Count with criteria

### Data Modification

- `insert(table, data)` - Insert single record
- `insertBatch(table, records)` - Batch insert
- `update(table, id, updates)` - Update by ID
- `updateWhere(table, criteria, updates)` - Update with criteria
- `delete(table, id)` - Delete by ID
- `deleteWhere(table, criteria)` - Delete with criteria

### Transactions

- `executeTransaction(operations)` - Execute in transaction
- `beginTransaction()` - Begin transaction
- `commit()` - Commit transaction
- `rollback()` - Rollback transaction

### Schema Operations

- `listTables()` - List all tables
- `getSchema(table)` - Get table schema
- `tableExists(table)` - Check if table exists

---

## Use Cases

### 1. Testing with H2

```java
@Test
public void testUserService() {
    DatabaseTool db = new DatabaseTool(DatabaseConfig.h2Memory());
    db.connect().block();

    // Create test table
    db.executeUpdate(
        "CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100))"
    ).block();

    // Test insert
    db.insert("users", Map.of("id", 1, "name", "Test")).block();

    // Test query
    QueryResult result = db.findById("users", 1).block();
    assertEquals("Test", result.get("name"));
}
```

### 2. Production Application

```java
@Service
public class UserRepository {
    @Inject private DatabaseTool db;

    @PostConstruct
    public void init() {
        db.setConfig(DatabaseConfig.production(
            System.getenv("DB_HOST"),
            System.getenv("DB_NAME"),
            System.getenv("DB_USER"),
            System.getenv("DB_PASSWORD")
        ));
        db.connect().block();
    }

    public List<Map<String, Object>> findActiveUsers() {
        return db.findWhere("users", Map.of("active", true))
            .map(QueryResult::getRows)
            .block();
    }
}
```

---

## Implementation Status

### ✅ Completed

**Architecture:**
- ✅ Provider/Service pattern architecture
- ✅ DatabaseProvider interface (30+ methods)
- ✅ Base provider with common functionality
- ✅ DatabaseConfig with 10+ presets
- ✅ DatabaseTool main class with routing
- ✅ Models: QueryResult, TransactionResult, ConnectionInfo
- ✅ Async/Reactive API (Mono-based)

**H2DatabaseProvider (Fully Implemented):**
- ✅ H2 JDBC driver integrated
- ✅ Real JDBC connections (DriverManager)
- ✅ PreparedStatement for SQL injection prevention
- ✅ All CRUD operations (insert, update, delete, select)
- ✅ Transaction support (begin, commit, rollback)
- ✅ Schema introspection (listTables, getSchema, tableExists)
- ✅ Connection management
- ✅ Parameterized queries (positional and named)
- ✅ Batch operations
- ✅ Query metadata (column info, execution time)

### 🚧 TODO (Full Implementation)

**PostgreSQLDatabaseProvider:**
- Add PostgreSQL JDBC driver
- Connection pool with HikariCP
- Prepared statement handling
- Transaction management
- Schema introspection

**MySQLDatabaseProvider:**
- Add MySQL Connector/J
- Connection management
- Query execution
- Transaction support

**SQLiteDatabaseProvider:**
- Add SQLite JDBC driver
- File-based connection
- In-memory mode support

**MongoDBDatabaseProvider:**
- Add MongoDB Reactive Streams driver
- Document operations
- Query DSL mapping

---

## Provider Comparison

|     Feature     |  PostgreSQL   |  MySQL   |    SQLite     |    MongoDB     |       H2       |
|-----------------|---------------|----------|---------------|----------------|----------------|
| **Type**        | SQL           | SQL      | SQL           | NoSQL          | SQL            |
| **Setup**       | Moderate      | Moderate | None          | Moderate       | None           |
| **Speed**       | ⚡⚡⚡ Fast      | ⚡⚡⚡ Fast | ⚡⚡ Moderate   | ⚡⚡⚡ Fast       | ⚡⚡⚡⚡ Very Fast |
| **ACID**        | ✅ Full        | ✅ Full   | ✅ Full        | ⚠️ Eventual    | ✅ Full         |
| **Scalability** | ⚡⚡⚡ Excellent | ⚡⚡ Good  | ❌ Single user | ⚡⚡⚡⚡ Excellent | ❌ Single user  |
| **Features**    | Advanced      | Standard | Basic         | Flexible       | Standard       |
| **Best For**    | Production    | Web apps | Embedded      | Documents      | Testing        |
| **Recommended** | ⭐ Production  | ✅ Yes    | ⭐ Embedded    | ✅ NoSQL        | ⭐ Testing      |

---

## Dependencies (TODO)

Add dependencies for desired providers:

```xml
<!-- PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.0</version>
</dependency>

<!-- MySQL -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.2.0</version>
</dependency>

<!-- SQLite -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.44.1.0</version>
</dependency>

<!-- MongoDB -->
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-reactivestreams</artifactId>
    <version>4.11.1</version>
</dependency>

<!-- H2 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
</dependency>

<!-- Connection Pool (Recommended) -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>5.1.0</version>
</dependency>
```

---

## License

Part of the Adentic Framework.
See main project [LICENSE](../../../LICENSE) for details.

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
*Status: Architecture Implemented (Provider Stubs)*
