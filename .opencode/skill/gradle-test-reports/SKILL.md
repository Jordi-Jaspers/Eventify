---
name: gradle-test-reports
description: Analyze Gradle test and build failures by reading HTML/XML reports in build directory instead of parsing console output
license: MIT
compatibility: opencode
metadata:
  language: java
  build-tool: gradle
---

## What I do

When running backend tests (`./gradlew test`) or clean builds (`./gradlew clean build`), analyze failures by reading the structured reports in the build directory rather than parsing console output.

## Why use this approach

- Console output is verbose and consumes excessive context window tokens
- HTML/XML reports contain structured, detailed failure information
- Stack traces and assertion messages are properly formatted in reports
- Avoids grep/regex parsing of unstructured console text

## Report locations

After running tests, check these directories in order:

1. **Test Results XML**: `server/build/test-results/test/*.xml`
   - Contains machine-readable test results
   - Each failed test has detailed failure message and stack trace

2. **Test Reports HTML**: `server/build/reports/tests/test/index.html`
   - Human-readable summary
   - Navigate to `classes/*.html` for per-class details

3. **Quality Reports** (for build failures):
   - Checkstyle: `server/build/reports/checkstyle/`
   - PMD: `server/build/reports/pmd/`
   - SpotBugs: `server/build/reports/spotbugs/`

## Workflow

1. Run the Gradle command without capturing output:
   ```bash
   cd server && ./gradlew test --no-daemon
   ```

2. Check exit code to determine if failures occurred

3. If failures exist, read the XML test results:
   ```bash
   # Find failed tests
   ls server/build/test-results/test/
   ```

4. Read specific XML files for failure details:
   - Look for `<failure>` or `<error>` elements
   - Extract the `message` attribute and nested stack trace

5. For quality check failures, read the corresponding report files

## When to use me

Use this skill when:
- Running `./gradlew test`, `./gradlew build`, or `./gradlew clean build`
- Debugging test failures in the backend
- The test run produces failures that need analysis
- You want to minimize token usage and avoid grepping console output

## Example: Reading a failed test

```xml
<!-- server/build/test-results/test/TEST-com.example.MyTest.xml -->
<testsuite name="com.example.MyTest" tests="3" failures="1">
  <testcase name="shouldValidateInput" classname="com.example.MyTest">
    <failure message="expected: true but was: false">
      org.opentest4j.AssertionFailedError: expected: true but was: false
        at com.example.MyTest.shouldValidateInput(MyTest.java:42)
    </failure>
  </testcase>
</testsuite>
```

Read the XML file directly to get the failure message and line number, then navigate to the source file to fix the issue.
