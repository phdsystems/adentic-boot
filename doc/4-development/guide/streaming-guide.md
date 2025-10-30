# ADE Agent Streaming

Streaming result support for ADE Agent using progressive callbacks.

**Example Code:** [StreamingExample.java](../../examples/src/main/java/com/phdsystems/agent/examples/StreamingExample.java)

## Overview

This module enables agents to produce results incrementally, providing:
- Real-time feedback for long-running tasks
- Progressive rendering for UI applications
- Lower latency to first result
- Memory-efficient processing of large outputs

## Installation

```xml
<dependency>
    <groupId>com.phdsystems</groupId>
    <artifactId>ade-streaming</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Quick Start

### Basic Streaming

```java
import com.phdsystems.agent.streaming.StreamingAgent;

StreamingAgent agent = new MyStreamingAgent();

agent.executeTaskStreaming(
  request,
  chunk -> System.out.print(chunk),   // Handle each chunk
  error -> System.err.println(error), // Handle errors
  () -> System.out.println("Done")    // Handle completion
);
```

### Buffer Streaming Results

```java
import com.phdsystems.agent.streaming.StreamBuffer;

StreamBuffer buffer = new StreamBuffer();

agent.executeTaskStreaming(
  request,
  buffer::append,
  buffer::setError,
  buffer::complete
);

// Get complete result
String fullResult = buffer.getResult();

// Or get individual chunks
List<String> chunks = buffer.getChunks();
```

### Simplified API

```java
// When you only care about chunks (ignore errors/completion)
agent.executeTaskStreaming(
  request,
  chunk -> handleChunk(chunk)
);
```

## Features

- **Callback-based API** - Simple consumer callbacks for chunks, errors, completion
- **StreamBuffer utility** - Accumulate streaming results into complete output
- **Backward compatible** - StreamingAgent extends Agent interface
- **Flexible error handling** - Separate callbacks for success and error paths
- **Memory efficient** - Process chunks as they arrive

## API Reference

### StreamingAgent Interface

```java
public interface StreamingAgent extends Agent {
    void executeTaskStreaming(
        TaskRequest request,
        Consumer<String> onChunk,
        Consumer<Throwable> onError,
        Runnable onComplete
    );

    void executeTaskStreaming(
        TaskRequest request,
        Consumer<String> onChunk
    );
}
```

### StreamBuffer Class

```java
public final class StreamBuffer {
    void append(String chunk);
    void setError(Throwable error);
    void complete();

    String getResult();
    List<String> getChunks();
    boolean hasError();
    Throwable getError();
    boolean isCompleted();
    void clear();
}
```

## Examples

### LLM Text Generation

```java
public class LLMStreamingAgent implements StreamingAgent {

  @Override
  public void executeTaskStreaming(
    TaskRequest request,
    Consumer<String> onChunk,
    Consumer<Throwable> onError,
    Runnable onComplete
  ) {
    try {
      OpenAI.ChatCompletionRequest req = // ... build request

      OpenAI.streamChatCompletion(req, response -> {
        String chunk = response.choices().get(0).delta().content();
        if (chunk != null) {
          onChunk.accept(chunk);
        }
      });

      onComplete.run();
    } catch (Exception e) {
      onError.accept(e);
    }
  }

  // ... implement other Agent methods
}
```

### File Processing

```java
public class FileProcessingAgent implements StreamingAgent {

  @Override
  public void executeTaskStreaming(
    TaskRequest request,
    Consumer<String> onChunk,
    Consumer<Throwable> onError,
    Runnable onComplete
  ) {
    try {
      Path file = Paths.get(request.task());

      Files.lines(file).forEach(line -> {
        String processed = processLine(line);
        onChunk.accept(processed + "\n");
      });

      onComplete.run();
    } catch (IOException e) {
      onError.accept(e);
    }
  }

  private String processLine(String line) {
    // Process individual line
    return line.toUpperCase();
  }
}
```

### Progress Reporting

```java
StreamBuffer buffer = new StreamBuffer();
List<String> progressUpdates = new ArrayList<>();

agent.executeTaskStreaming(
  request,
  chunk -> {
    buffer.append(chunk);
    progressUpdates.add("Received " + buffer.getChunks().size() + " chunks");
  },
  error -> System.err.println("Error: " + error.getMessage()),
  () -> System.out.println("Complete: " + buffer.getResult().length() + " characters")
);
```

## Use Cases

- **LLM text generation** - Stream tokens as they're generated
- **File processing** - Process large files line by line
- **API responses** - Stream results from paginated APIs
- **Progress reporting** - Provide real-time progress updates
- **Incremental computation** - Return partial results while computing

## Best Practices

1. **Handle errors** - Always provide error callback for production code
2. **Use StreamBuffer for testing** - Easier to verify complete results
3. **Keep chunks small** - Emit chunks frequently for better responsiveness
4. **Signal completion** - Always call onComplete() when done
5. **Thread safety** - Use StreamBuffer for thread-safe accumulation

## Future Enhancements

- Reactive Streams (Publisher/Subscriber) support
- Backpressure handling
- Chunk transformation operators
- Stream multiplexing (fan-out to multiple consumers)
- Timeout and cancellation support

## License

Apache License 2.0
