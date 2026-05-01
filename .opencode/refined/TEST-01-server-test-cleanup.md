---
epic: "TEST"
title: "Server Test Suite Cleanup — Remove Low-Value Tests & Extract Shared Builders"
estimate: M
status: ready
created: 2026-04-04
depends_on: [ ]
labels: [ backend, testing, tech-debt ]
priority: P2
claimed_by:
claimed_by_date:
---

## 1. User Story
**As a** developer\
**I want** a cleaner, faster test suite with shared test builders and no low-value tests\
**So that** I get faster feedback during development and CI, and consistent test patterns across the codebase\

## 2. Business Context & Value
The server test suite has 100+ test files taking ~5 minutes. A number of tests provide no meaningful coverage — trivial delegation tests (job tests that only verify a method calls another method), broken exception handling tests, context-loading smoke tests, and redundant field assertions already covered by comprehensive tests. Additionally, helper methods for creating test objects (channels, API keys, watchlists, page inputs) are duplicated across multiple files. This wastes developer time maintaining duplicates and makes the test suite slower than necessary. Cleaning up dead weight and extracting shared builders improves maintainability and sets the stage for parallel execution.

## 3. Acceptance Criteria

* [ ] **Delete trivial job delegation test files**: Job test files that only verify delegation to a service (call-through tests) are removed
    * Given job test files exist that follow the pattern: "should delegate to service", "should handle exception" (broken — creates exception but never throws), "should be idempotent" (calls twice, verifies called twice)
    * When the cleanup is applied
    * Then all such trivial job test files are deleted
    * And `./gradlew test` still passes

* [ ] **Remove low-value smoke tests**: Tests that only assert context loads or beans exist are removed
    * Given test methods exist that only verify `applicationContext is not null` or `someBean is not null` without testing behavior
    * When the cleanup is applied
    * Then those methods are removed
    * And tests in the same file that verify actual HTTP behavior or business logic are kept

* [ ] **Remove redundant field assertion tests**: Individual field assertion tests already covered by comprehensive tests are removed
    * Given test methods exist that assert a single field value (e.g., role, enabled, validated) where a comprehensive test in the same class already asserts all fields together
    * When the cleanup is applied
    * Then the redundant individual tests are removed
    * And the comprehensive test remains

* [ ] **Extract shared static builders for non-entity objects**: Reusable builders for request/response/input objects in a shared utility class
    * Given helper methods like `createDefaultPageInput()` are duplicated across multiple test files
    * When shared builders are extracted
    * Then a `TestBuilders` (or similar) utility class exists in the `support/` package with static methods
    * And each builder creates a **valid default object** (correct, complete) that tests can then adjust for their specific scenario
    * And all files with duplicates use the shared builder instead of local copies
    * And the class has no Spring dependency (usable from both unit and integration tests)

* [ ] **Extract entity builders to UnitTest base class**: Duplicated entity creation helpers are consolidated into the UnitTest base class
    * Given entity creation helpers (for Channel, ApiKey, Watchlist, etc.) are duplicated across multiple unit test files
    * When entity builders are extracted
    * Then `UnitTest.java` has protected methods that create valid default entities with all required fields set
    * And each creates a **valid default entity** — tests adjust only the field relevant to their scenario
    * And duplicate local methods are removed from individual test files
    * And all affected tests pass unchanged

* [ ] **Enable parallel execution for unit tests**: Unit tests run in parallel via Gradle
    * Given unit tests currently run sequentially (no `maxParallelForks` configured)
    * When parallel execution is configured in `build.gradle.kts`
    * Then `maxParallelForks` is set aggressively (`Runtime.getRuntime().availableProcessors()`)
    * And integration tests are not affected (they share a testcontainer and may need separate consideration)
    * And `./gradlew test` passes with no flaky failures

* [ ] **All tests pass**: Full suite green after all changes
    * Given all cleanup and refactoring is applied
    * When `./gradlew test` is run
    * Then all remaining tests pass

## 4. Technical Requirements
* **API Changes**: N/A
* **Database**: N/A
* **Security**: N/A
* **Performance**: Faster test execution via parallel unit tests and fewer tests overall. Target: measurable reduction from current ~5 min runtime.

## 5. Design & UI/UX
N/A — backend-only, no UI changes.

## 6. Implementation Notes

### Builder design principle
Every shared builder creates a **valid, correct default object**. Tests adjust only the field(s) relevant to their scenario. This avoids scattered object construction logic and makes tests self-documenting about what they're actually testing.

Example pattern:
```java
// In TestBuilders.java (static, no Spring)
public static SortablePageInput aPageInput() {
    SortablePageInput input = new SortablePageInput();
    input.setPageNumber(0);
    input.setPageSize(20);
    return input;
}

// In test:
SortablePageInput input = TestBuilders.aPageInput();
input.setPageSize(5); // only adjust what matters for this test
```

### Two builder locations:
1. **`TestBuilders.java`** (new, in `support/`) — static methods for non-entity objects (page inputs, request/response DTOs). No Spring dependency, usable everywhere.
2. **`UnitTest.java`** (existing base class) — protected methods for entity objects (Channel, ApiKey, Watchlist) specific to unit test mocking patterns.

### Investigation needed at implementation time:
- Scan for all duplicate helper methods across test files to find current state
- Identify which job test files match the trivial delegation pattern
- Check for any additional low-value tests beyond what was found during refinement
- Verify integration tests work correctly with parallel unit test execution

### Parallel execution consideration:
- Unit tests (Mockito, no Spring context) are safe to parallelize aggressively
- Integration tests share a TimescaleDB testcontainer — may need to remain sequential or use a separate Gradle task
- Consider splitting into `unitTest` and `integrationTest` tasks if needed

## 7. Test Impact Analysis

### Test modification policy:
- [ ] Existing test **files** MAY be deleted if they contain only low-value tests
- [ ] Existing test **methods** MAY be deleted if they are redundant or trivial
- [ ] Existing test **helpers** MAY be replaced with shared builders — no assertion changes
- [ ] No business logic test assertions should change — only the source of helper objects changes

### Investigation at implementation time:
The implementer should scan the test suite to identify the current state of:
1. Job test files matching the trivial delegation pattern
2. Context-loading / bean-existence-only test methods
3. Redundant field assertion tests covered by comprehensive tests
4. Duplicate helper methods across test files (especially entity and page input builders)
