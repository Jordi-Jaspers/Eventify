#!/bin/bash
set -euo pipefail

echo "Moving to project root directory..."
cd "$(git rev-parse --show-toplevel)"

echo "Moving to server directory..."
cd server

echo "Current directory: $(pwd)"
echo "Running Gradle 'spotless, checkstyle, PMD and spotbugs' tasks..."
if ! ./gradlew spotlessCheck checkQualityMain -xcyclonedxBom -xspotbugsMain; then
  echo "Gradle code quality checks failed. Please fix the issues and try again."
  exit 1
fi

echo "Gradle code quality checks passed."
