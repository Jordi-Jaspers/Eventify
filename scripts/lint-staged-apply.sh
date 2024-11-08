#!/bin/bash
set -euo pipefail

pop_stash() {
  if [ "$previous_stash" != "$new_stash" ]; then
    echo "Popping stash and restoring unstaged changes..."
    git stash pop --quiet
    git restore --quiet --staged .
  fi
}

echo "Temporarily commiting staged changes..."
git commit --quiet --no-verify --message 'Temporary commit for Gradle code quality checks'

echo "Temporarily stashing unstaged changes including new files..."
git add -A
previous_stash=$(git rev-parse -q --verify refs/stash)
git stash push --quiet --staged --message 'Unstaged changes'
new_stash=$(git rev-parse -q --verify refs/stash)

echo "Reverting temporary commit to bring back staged changes..."
git reset --soft HEAD^

echo "Moving to project root directory..."
cd "$(git rev-parse --show-toplevel)"

echo "Moving to server directory..."
cd server

echo "Current directory: $(pwd)"
echo "Running Gradle './gradlew spotlessCheck checkQualityMain -xcyclonedxBom' task on staged changes..."
if ! ./gradlew spotlessApply checkQualityMain -xcyclonedxBom; then
  echo "Gradle code quality checks failed. Please fix the issues and try again."

  echo "Temporarily commiting staged changes..."
  git commit --quiet --no-verify --message 'Stash before Gradle code quality checks'

  pop_stash

  echo "Reverting temporary commit to bring back staged changes..."
  git reset --soft HEAD^

  exit 1
fi

echo "Staging formatting changes (if any) and temporarily commiting staged and formatted changes..."
git add --all
git commit --quiet --no-verify --message 'Staged and formatted changes'

pop_stash

echo "Reverting temporary commit to bring back staged and formatted changes..."
git reset --soft HEAD^
