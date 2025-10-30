# Database Tool - LLM Integration Guide

**Version:** 0.1.0
**Category:** Tool Integration
**Status:** Architecture Complete (H2 Fully Implemented)
**Date:** 2025-10-25

---

## TL;DR

**Database Tool enables LLMs to query and manipulate databases through natural language**. Agents analyze user requests, select appropriate database operations (query, insert, update, delete), extract parameters, and execute SQL safely with PreparedStatement. **Benefits**: Natural language → SQL, SQL injection prevention, multi-database support, transaction handling. **Use cases**: Data retrieval, CRUD operations, analytics, reporting.

---

## Table of Contents

- [Overview](#overview)
- [Integration Architecture](#integration-architecture)
- [Tool Registration](#tool-registration)
- [LLM Workflow Examples](#llm-workflow-examples)
- [Tool Descriptor Format](#tool-descriptor-format)
- [Parameter Mapping](#parameter-mapping)
- [Error Handling](#error-handling)
- [Security Considerations](#security-considerations)
- [Use Cases](#use-cases)
- [Best Practices](#best-practices)

---

## Overview

The Database Tool integrates with LLM-based agents to provide database access through natural language. The tool handles the complexity of SQL generation, parameter binding, and result formatting while the LLM focuses on understanding user intent.

### Integration Flow

```
┌──────────────────────────────────────────────────────────────┐
│                         User Input                            │
│     "Find all users over 30 who live in New York"            │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM/AI Agent                             │
│  • Analyzes intent: "query users table"                      │
│  • Identifies filters: age > 30, city = "New York"           │
│  • Selects tool: database                                    │
│  • Extracts parameters: table, criteria                      │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                    DatabaseTool                               │
│  • Method: findWhere(table, criteria)                        │
│  • Provider: H2DatabaseProvider                              │
│  • Execution: PreparedStatement (SQL injection safe)         │
│  • SQL: SELECT * FROM users WHERE age > ? AND city = ?       │
│  • Params: [30, "New York"]                                  │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     QueryResult                               │
│  • success: true                                             │
│  • rows: [{id:1, name:"John", age:35, city:"New York"}, ...] │
│  • rowCount: 5                                               │
│  • executionTimeMs: 45                                       │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                      LLM Response                             │
│  "I found 5 users over 30 in New York:                       │
│   1. John (35 years old)                                     │
│   2. Sarah (42 years old)                                    │
│   ..."                                                        │
└──────────────────────────────────────────────────────────────┘
```

---

## Integration Architecture

### Component Roles

**1. LLM/AI Agent**
- Understands natural language queries
- Selects appropriate database operation
- Extracts parameters from user intent
- Formats results for user presentation

**2. DatabaseTool (@Tool)**
- Provides consistent API for database operations
- Routes to appropriate provider (H2, PostgreSQL, etc.)
- Returns structured results (QueryResult, TransactionResult)

**3. DatabaseProvider (H2, PostgreSQL, etc.)**
- Executes SQL with PreparedStatement (SQL injection prevention)
- Manages connections and transactions
- Handles provider-specific details

### Tool Registration

The Database Tool is registered via the `@Tool` annotation:

```java
@Tool(name = "database")
public class DatabaseTool {
    // 30+ methods for database operations
}
```

This makes it discoverable by the ToolProvider system for LLM agents.

---

## LLM Workflow Examples

### Example 1: Query - Find Records

**User Request:**

```
"Show me all products that cost more than $50"
```

**LLM Analysis:**

```json
{
  "intent": "query_database",
  "operation": "findWhere",
  "parameters": {
    "table": "products",
    "criteria": {
      "price": {">": 50}
    }
  }
}
```

**Agent Code:**

```java
// LLM invokes via ToolProvider
QueryResult result = databaseTool.findWhere(
    "products",
    Map.of("price", 50)  // Criteria mapping
).block();

// Format for LLM
String response = formatQueryResult(result);
// "Found 12 products over $50: Laptop ($899), Monitor ($299), ..."
```

**SQL Executed (with PreparedStatement):**

```sql
SELECT * FROM products WHERE price > ?
-- Parameter: [50]
```

### Example 2: Insert - Add Record

**User Request:**

```
"Add a new user named Alice with email alice@example.com, age 28"
```

**LLM Analysis:**

```json
{
  "intent": "insert_record",
  "operation": "insert",
  "parameters": {
    "table": "users",
    "data": {
      "name": "Alice",
      "email": "alice@example.com",
      "age": 28
    }
  }
}
```

**Agent Code:**

```java
Object userId = databaseTool.insert(
    "users",
    Map.of(
        "name", "Alice",
        "email", "alice@example.com",
        "age", 28
    )
).block();

// Response to LLM
// "Successfully added user Alice with ID: 42"
```

**SQL Executed:**

```sql
INSERT INTO users (name, email, age) VALUES (?, ?, ?)
-- Parameters: ["Alice", "alice@example.com", 28]
```

### Example 3: Update - Modify Record

**User Request:**

```
"Update John's age to 31"
```

**LLM Analysis:**

```json
{
  "intent": "update_record",
  "operation": "updateWhere",
  "parameters": {
    "table": "users",
    "criteria": {"name": "John"},
    "updates": {"age": 31}
  }
}
```

**Agent Code:**

```java
Long updated = databaseTool.updateWhere(
    "users",
    Map.of("name", "John"),  // Criteria
    Map.of("age", 31)         // Updates
).block();

// Response: "Updated 1 user named John"
```

**SQL Executed:**

```sql
UPDATE users SET age = ? WHERE name = ?
-- Parameters: [31, "John"]
```

### Example 4: Complex Query with Aggregation

**User Request:**

```
"What's the average age of users in each city?"
```

**LLM Analysis:**

```json
{
  "intent": "complex_query",
  "operation": "executeQuery",
  "parameters": {
    "sql": "SELECT city, AVG(age) as avg_age FROM users GROUP BY city",
    "params": []
  }
}
```

**Agent Code:**

```java
QueryResult result = databaseTool.executeQuery(
    "SELECT city, AVG(age) as avg_age FROM users GROUP BY city"
).block();

// Format for LLM presentation
for (Map<String, Object> row : result.getRows()) {
    // "New York: 35.2 years"
    // "San Francisco: 29.8 years"
}
```

### Example 5: Transaction - Multi-Step Operation

**User Request:**

```
"Transfer $100 from Alice's account to Bob's account"
```

**LLM Analysis:**

```json
{
  "intent": "transaction",
  "operation": "executeTransaction",
  "steps": [
    {"operation": "updateWhere", "table": "accounts", "criteria": {"user": "Alice"}, "updates": {"balance": "balance - 100"}},
    {"operation": "updateWhere", "table": "accounts", "criteria": {"user": "Bob"}, "updates": {"balance": "balance + 100"}}
  ]
}
```

**Agent Code:**

```java
TransactionResult txResult = databaseTool.executeTransaction(provider -> {
    // Step 1: Debit Alice
    provider.executeUpdate(
        "UPDATE accounts SET balance = balance - ? WHERE user = ?",
        100, "Alice"
    ).block();

    // Step 2: Credit Bob
    provider.executeUpdate(
        "UPDATE accounts SET balance = balance + ? WHERE user = ?",
        100, "Bob"
    ).block();

    return Mono.empty();
}).block();

// Response: "Transfer completed: $100 from Alice to Bob"
```

---

## Tool Descriptor Format

LLMs use tool descriptors to understand available functions. Here's the OpenAI function calling format for Database Tool:

### Query Operations

```json
{
  "name": "database_find_where",
  "description": "Find records in a database table matching criteria",
  "parameters": {
    "type": "object",
    "properties": {
      "table": {
        "type": "string",
        "description": "Table name to query"
      },
      "criteria": {
        "type": "object",
        "description": "Search criteria as key-value pairs",
        "additionalProperties": true
      }
    },
    "required": ["table", "criteria"]
  }
}
```

### Insert Operation

```json
{
  "name": "database_insert",
  "description": "Insert a new record into a database table",
  "parameters": {
    "type": "object",
    "properties": {
      "table": {
        "type": "string",
        "description": "Table name for insertion"
      },
      "data": {
        "type": "object",
        "description": "Record data as key-value pairs",
        "additionalProperties": true
      }
    },
    "required": ["table", "data"]
  }
}
```

### Update Operation

```json
{
  "name": "database_update_where",
  "description": "Update records matching criteria",
  "parameters": {
    "type": "object",
    "properties": {
      "table": {
        "type": "string",
        "description": "Table name to update"
      },
      "criteria": {
        "type": "object",
        "description": "Criteria to match records",
        "additionalProperties": true
      },
      "updates": {
        "type": "object",
        "description": "Fields to update with new values",
        "additionalProperties": true
      }
    },
    "required": ["table", "criteria", "updates"]
  }
}
```

### Custom Query

```json
{
  "name": "database_execute_query",
  "description": "Execute a custom SQL SELECT query with parameters",
  "parameters": {
    "type": "object",
    "properties": {
      "query": {
        "type": "string",
        "description": "SQL SELECT query with ? placeholders for parameters"
      },
      "parameters": {
        "type": "array",
        "description": "Parameter values for ? placeholders",
        "items": {
          "type": ["string", "number", "boolean", "null"]
        }
      }
    },
    "required": ["query"]
  }
}
```

---

## Parameter Mapping

### Criteria Mapping (Complex Queries)

LLMs can express complex criteria that map to SQL operators:

```java
// LLM provides
{
  "age": {">": 30},
  "city": "New York",
  "status": {"in": ["active", "pending"]}
}

// Maps to SQL
WHERE age > ? AND city = ? AND status IN (?, ?)
// Parameters: [30, "New York", "active", "pending"]
```

### Supported Criteria Operators

|           LLM Syntax           | SQL Operator |                          Example                          |
|--------------------------------|--------------|-----------------------------------------------------------|
| `{"field": value}`             | `=`          | `{"age": 30}` → `age = 30`                                |
| `{"field": {">": value}}`      | `>`          | `{"age": {">": 30}}` → `age > 30`                         |
| `{"field": {">=": value}}`     | `>=`         | `{"age": {">=": 30}}` → `age >= 30`                       |
| `{"field": {"<": value}}`      | `<`          | `{"age": {"<": 30}}` → `age < 30`                         |
| `{"field": {"<=": value}}`     | `<=`         | `{"age": {"<=": 30}}` → `age <= 30`                       |
| `{"field": {"!=": value}}`     | `<>`         | `{"status": {"!=": "inactive"}}` → `status <> 'inactive'` |
| `{"field": {"in": [...]}}`     | `IN`         | `{"city": {"in": ["NY", "LA"]}}` → `city IN ('NY', 'LA')` |
| `{"field": {"like": pattern}}` | `LIKE`       | `{"name": {"like": "John%"}}` → `name LIKE 'John%'`       |

---

## Error Handling

### LLM-Friendly Error Messages

The Database Tool provides structured error responses for LLMs:

```java
// SQL Error
QueryResult result = QueryResult.builder()
    .success(false)
    .errorMessage("Table 'users' does not exist")
    .sqlState("42S02")
    .build();

// LLM receives structured error and can:
// 1. Suggest creating the table
// 2. List available tables
// 3. Ask user for clarification
```

### Error Handling Patterns

**1. Table Not Found**

```
LLM: "The table 'userz' doesn't exist. Did you mean 'users'?
      Available tables: users, products, orders"
```

**2. Column Not Found**

```
LLM: "Column 'emal' not found in 'users' table.
      Did you mean 'email'? Available columns: id, name, email, age"
```

**3. Type Mismatch**

```
LLM: "Cannot compare 'age' (integer) with 'thirty' (string).
      Please provide a number for age."
```

**4. Permission Denied**

```
LLM: "You don't have permission to DELETE from 'users' table.
      Contact your administrator for access."
```

---

## Security Considerations

### SQL Injection Prevention

**✅ SAFE: All database operations use PreparedStatement**

```java
// User input: "'; DROP TABLE users; --"
// LLM extracts: criteria = {"name": "'; DROP TABLE users; --"}

// Executed safely with PreparedStatement:
SELECT * FROM users WHERE name = ?
// Parameter: ["'; DROP TABLE users; --"]
// Treated as literal string value, not SQL code
```

### LLM Security Responsibilities

**1. Input Validation**
- LLM should validate user intent before database access
- Confirm destructive operations (DELETE, DROP)
- Require explicit permission for sensitive data

**2. Access Control**
- LLM checks user permissions before database operations
- Restrict table access based on user role
- Log all database operations with user context

**3. Data Masking**
- LLM masks sensitive data (SSN, credit cards) in responses
- Applies field-level security rules
- Redacts PII in logs

---

## Use Cases

### 1. Natural Language Analytics

**User:** "What are our top 5 products by revenue this month?"

**Agent Workflow:**
1. Parse date range ("this month")
2. Identify metric (revenue)
3. Apply grouping (by product)
4. Add sorting and limit
5. Execute query with Database Tool
6. Format results as natural language

### 2. Data Entry Assistant

**User:** "Add a new customer: Jane Smith, jane@example.com, phone 555-1234"

**Agent Workflow:**
1. Extract entity type (customer)
2. Parse structured data
3. Validate format (email, phone)
4. Insert via Database Tool
5. Confirm with customer ID

### 3. Report Generation

**User:** "Generate a sales report for Q4 2024"

**Agent Workflow:**
1. Execute multiple queries (sales, returns, customers)
2. Aggregate data
3. Calculate metrics
4. Format as structured report
5. Offer export options (CSV, PDF)

### 4. Database Administration

**User:** "Show me the schema for the orders table"

**Agent Workflow:**
1. Call `getSchema("orders")`
2. Format column information
3. Show relationships
4. Suggest optimizations

---

## Best Practices

### For LLM Developers

1. **Validate Before Execute**
   - Confirm user intent for destructive operations
   - Validate data types and formats
   - Check business rules
2. **Use Parameterized Queries**
   - Always use `executeQuery(sql, params)` with parameters
   - Never concatenate user input into SQL
   - Let PreparedStatement handle escaping
3. **Handle Errors Gracefully**
   - Parse error messages for user-friendly explanations
   - Suggest corrections for typos
   - Provide context from schema information
4. **Optimize for Performance**
   - Use `findWhere()` for simple queries
   - Add LIMIT clauses for large result sets
   - Consider pagination for data-heavy operations
5. **Maintain Context**
   - Remember previous queries in conversation
   - Build on prior results
   - Reference tables/columns by context

### For Agent Implementers

1. **Schema Awareness**

   ```java
   // Get available tables
   List<String> tables = databaseTool.listTables().block();

   // Get table structure
   Map<String, Object> schema = databaseTool.getSchema("users").block();
   ```
2. **Connection Management**

   ```java
   // Ensure connection before operations
   databaseTool.connect().block();

   // Clean up when done
   databaseTool.disconnect().block();
   ```
3. **Transaction Handling**

   ```java
   // Use transactions for multi-step operations
   databaseTool.executeTransaction(provider -> {
       // Multiple operations atomically
       return Mono.empty();
   }).block();
   ```

---

## Integration Example

Complete example showing LLM agent using Database Tool:

```java
@Component
public class DatabaseAgent implements Agent {

    @Inject
    private DatabaseTool databaseTool;

    @Override
    public TaskResult executeTask(TaskRequest request) {
        String task = request.task().toLowerCase();

        // Connect if not already connected
        if (!databaseTool.isConnected()) {
            databaseTool.connect().block();
        }

        try {
            if (task.contains("find") || task.contains("show")) {
                return handleQuery(task);
            } else if (task.contains("add") || task.contains("create")) {
                return handleInsert(task);
            } else if (task.contains("update") || task.contains("change")) {
                return handleUpdate(task);
            } else if (task.contains("delete") || task.contains("remove")) {
                return handleDelete(task);
            }

            return TaskResult.failure(getName(), task, "Unknown database operation");

        } catch (Exception e) {
            return TaskResult.failureWithException(getName(), task, e);
        }
    }

    private TaskResult handleQuery(String task) {
        // Extract table and criteria from natural language
        // This is where LLM processing happens
        QueryResult result = databaseTool.findWhere(
            extractTable(task),
            extractCriteria(task)
        ).block();

        if (result.isSuccess()) {
            String output = formatResults(result);
            return TaskResult.success(getName(), task, output);
        } else {
            return TaskResult.failure(getName(), task, result.getErrorMessage());
        }
    }

    // Additional methods...
}
```

---

## Conclusion

The Database Tool provides LLM agents with powerful, secure database access through natural language. By using PreparedStatement for all operations, it ensures SQL injection prevention while maintaining flexibility across multiple database providers (H2, PostgreSQL, MySQL, SQLite, MongoDB).

**Key Takeaways:**
- ✅ Natural language → SQL via LLM intent analysis
- ✅ SQL injection prevention with PreparedStatement
- ✅ Multi-provider support (switch databases at runtime)
- ✅ Reactive/async API for LLM workflows
- ✅ Structured error handling for LLM feedback
- ✅ Transaction support for complex operations

---

*Last Updated: 2025-10-25*
*Version: 0.1.0*
*Status: H2 Fully Implemented, Other Providers Stub*
