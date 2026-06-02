---
name: jframe-exception-handling
description: Exception handling patterns in Eventify using jFrame's ApiException, ApiErrorCode enum, and auto-configured global handler. Use when creating new exceptions, error codes, or understanding error response format.
metadata:
  skill-type: backend
  language: Java
  framework: JFrame
  build-tool: gradle
---

# jFrame Exception Handling Pattern

Eventify delegates all exception handling to jFrame. No custom `@ControllerAdvice` exists in Eventify — jFrame's `JFrameResponseEntityExceptionHandler` handles everything automatically.

## Architecture

```
Throw site (Service/Controller)
    → ApiException(ApiErrorCode)
        → JFrameResponseEntityExceptionHandler (@RestControllerAdvice, HIGHEST_PRECEDENCE)
            → ErrorResponseEntityBuilder + Enrichers
                → ApiErrorResponseResource (JSON response, HTTP 400)
```

## Key jFrame Classes

| Class | Package | Role |
|-------|---------|------|
| `ApiError` | `io.github.jframe.exception` | Interface: `getErrorCode()` + `getReason()` |
| `ApiException` | `io.github.jframe.exception` | Base exception, wraps `ApiError` |
| `HttpException` | `io.github.jframe.exception` | HTTP-status-based exception (dynamic status) |
| `JFrameResponseEntityExceptionHandler` | `io.github.jframe.exception.handler` | Global `@RestControllerAdvice` (auto-configured) |
| `ErrorResponseResource` | `io.github.jframe.exception.handler` | Base error response DTO |
| `ApiErrorResponseResource` | `io.github.jframe.exception.handler` | Extended DTO with `apiErrorCode` + `apiErrorReason` |

## HTTP Status Mapping

| Exception Type | HTTP Status |
|---|---|
| `ApiException` | 400 Bad Request |
| `HttpException` | Dynamic (from `Response.Status`) |
| `BadCredentialsException` (Spring) | 401 |
| `AccessDeniedException` (Spring) | 403 |
| `NoResourceFoundException` (Spring) | 404 |
| `RateLimitExceededException` | 429 |
| `Throwable` (fallback) | 500 |

## Adding a New Exception (Step-by-Step)

### 1. Add error code to `ApiErrorCode` enum

**File:** `server/src/main/java/io/github/eventify/common/exception/ApiErrorCode.java`

```java
MY_NEW_ERROR(
    "ERR-0063",
    "Human-readable reason explaining what went wrong."
),
```

> Increment the error number sequentially. Currently ends at ERR-0062.

### 2. Create exception class

**File:** `server/src/main/java/io/github/eventify/common/exception/MyNewException.java`

```java
package io.github.eventify.common.exception;

import io.github.jframe.exception.ApiException;

import java.io.Serial;

import static io.github.eventify.Main.SERIAL_VERSION_UID;

/**
 * Exception thrown when [describe scenario].
 */
public class MyNewException extends ApiException {

    @Serial
    private static final long serialVersionUID = SERIAL_VERSION_UID;

    /**
     * Constructor with error code.
     *
     * @param errorCode the error code
     */
    public MyNewException(final ApiErrorCode errorCode) {
        super(errorCode);
    }

    @Override
    public String getMessage() {
        return getApiError().getReason();
    }
}
```

### 3. Throw from service

```java
throw new MyNewException(ApiErrorCode.MY_NEW_ERROR);
```

That's it. jFrame handles the rest automatically.

## Error Response Format

### Standard API Error (HTTP 400)

```json
{
  "method": "POST",
  "uri": "/api/v1/resource",
  "query": "",
  "contentType": "application/json",
  "statusCode": 400,
  "statusMessage": "Bad Request",
  "errorMessage": "Human-readable reason explaining what went wrong.",
  "apiErrorCode": "ERR-0063",
  "apiErrorReason": "Human-readable reason explaining what went wrong.",
  "txId": "uuid",
  "traceId": "...",
  "spanId": "..."
}
```

## Conventions

- **One exception class per domain concern** (e.g., `InvalidApiKeyException`, `QuotaExceededException`)
- **Multiple error codes can share an exception class** — pass different `ApiErrorCode` values
- **Always override `getMessage()`** to return `getApiError().getReason()`
- **Use `SERIAL_VERSION_UID` from `Main`** for serialization
- **Constructor takes `ApiErrorCode`** (not raw strings)
- **Javadoc required** on class and constructor
- **Package:** `io.github.eventify.common.exception`

## Existing Exceptions (Reference)

| Exception | Typical Error Code |
|---|---|
| `InvalidApiKeyException` | `INVALID_API_KEY`, `API_KEY_REVOKED` |
| `NonExistingUserException` | `USER_NOT_FOUND` |
| `UserAlreadyExistsException` | `USER_ALREADY_EXISTS` |
| `QuotaExceededException` | `QUOTA_EXCEEDED` |
| `AuthorizationException` | `UNAUTHORIZED` |
| `InvalidTokenException` | `INVALID_TOKEN_ERROR` |
| `DuplicateChannelNameException` | `DUPLICATE_CHANNEL_NAME` |
| `OwnershipTransferException` | `OWNERSHIP_TRANSFER_*` |
| `InternalServerException` | `INTERNAL_SERVER_ERROR` |

## When to Use `HttpException` vs `ApiException`

- **`ApiException`** (via custom subclass): Business logic errors with a stable error code clients can programmatically handle
- **`HttpException`**: Raw HTTP errors without business semantics (rare in Eventify — prefer `ApiException`)
