# Docker Image Build & Push to GHCR

**Epic**: CI/CD & Deployment Pipeline
**Status**: Ready for Dev
**Estimate**: M
**Created Date**: 2026-01-06

## 1. User Story
**As a** developer
**I want** the CI pipeline to automatically build and push Docker images on every push to `develop`
**So that** my test environment automatically receives the latest changes via Watchtower

## 2. Business Context & Value
Currently, the CI workflow builds and tests the application but does not produce deployable artifacts. By adding Docker image builds and pushing to GitHub Container Registry (ghcr.io), we enable continuous deployment to the test environment. Watchtower (already configured) will automatically pull new images when they appear.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Backend image is built and pushed on develop branch
    *   Given a commit is pushed to the `develop` branch
    *   When the CI workflow completes successfully
    *   Then a Docker image `ghcr.io/jordijaspers/eventify-server:latest-dev` is pushed to GHCR
    *   And the image is tagged with the short commit SHA (e.g., `ghcr.io/jordijaspers/eventify-server:dev-abc1234`)

*   [ ] **Scenario 2**: Frontend image is built and pushed on develop branch
    *   Given a commit is pushed to the `develop` branch
    *   When the CI workflow completes successfully
    *   Then a Docker image `ghcr.io/jordijaspers/eventify-client:latest-dev` is pushed to GHCR
    *   And the image is tagged with the short commit SHA

*   [ ] **Scenario 3**: Images are NOT pushed on pull requests
    *   Given a pull request is opened or updated
    *   When the CI workflow runs
    *   Then Docker images are built (to verify they work)
    *   But images are NOT pushed to GHCR

*   [ ] **Scenario 4**: Tagged releases produce versioned images
    *   Given a tag matching `*.*.*` is pushed (e.g., `1.2.3`)
    *   When the CI workflow completes successfully
    *   Then images are pushed with the version tag (e.g., `eventify-server:1.2.3`)
    *   And images are also tagged as `latest`

*   [ ] **Scenario 5**: Multi-platform builds (optional but recommended)
    *   Given the home server may be x64 or ARM
    *   When images are built
    *   Then they support `linux/amd64` platform (ARM can be added later if needed)

## 4. Technical Requirements

### API Changes
*   N/A - This is infrastructure only

### Workflow Changes
*   Modify `.github/workflows/ci.yml` (or create new `cd.yml`)
*   Add jobs for building and pushing Docker images
*   Use `docker/build-push-action` for efficient layer caching
*   Use `docker/login-action` for GHCR authentication

### GitHub Configuration
*   Repository must have `packages: write` permission (already present in ci.yml)
*   No additional secrets needed - `GITHUB_TOKEN` works for ghcr.io within same repo

### Image Naming Convention
```
ghcr.io/jordijaspers/eventify-server:latest-dev    # Latest develop build
ghcr.io/jordijaspers/eventify-server:dev-<sha>     # Specific commit
ghcr.io/jordijaspers/eventify-server:1.2.3         # Release version
ghcr.io/jordijaspers/eventify-server:latest        # Latest release

ghcr.io/jordijaspers/eventify-client:latest-dev    # Latest develop build
ghcr.io/jordijaspers/eventify-client:dev-<sha>     # Specific commit
ghcr.io/jordijaspers/eventify-client:1.2.3         # Release version
ghcr.io/jordijaspers/eventify-client:latest        # Latest release
```

### Security
*   Images should be public (or configure Watchtower with GHCR auth)
*   No secrets baked into images - all config via environment variables

### Performance
*   Use GitHub Actions cache for Docker layers
*   Build images in parallel (backend and frontend simultaneously)
*   Only build images after tests pass

## 5. Design & UI/UX
*   N/A - Infrastructure only

## 6. Implementation Notes / Research

### Existing CI Workflow Analysis
*   File: `.github/workflows/ci.yml`
*   Already has `packages: write` permission
*   Builds backend with Gradle, frontend with Bun
*   Need to add Docker build steps after existing build steps

### Backend Dockerfile
*   File: `server/Dockerfile`
*   Well-optimized with slim JRE (jlink)
*   Expects `build/libs/eventify*.jar` to exist (built by Gradle)
*   Note: Dockerfile uses Java 21, but `.tool-versions` says Java 25 - verify compatibility

### Frontend Dockerfile
*   Does not exist yet - see CICD-002
*   Will need to be created for SvelteKit with Bun adapter

### Suggested Workflow Structure
```yaml
jobs:
  build:
    # Existing build job - builds and tests
    ...
  
  docker-server:
    needs: build
    if: github.ref == 'refs/heads/develop' || startsWith(github.ref, 'refs/tags/')
    # Build and push server image
    
  docker-client:
    needs: build
    if: github.ref == 'refs/heads/develop' || startsWith(github.ref, 'refs/tags/')
    # Build and push client image
```

### Dependencies
*   **Depends on**: CICD-002 (Frontend Dockerfile must exist first)
*   **Blocks**: Actual deployment to test environment

### References
*   [docker/build-push-action](https://github.com/docker/build-push-action)
*   [GitHub Container Registry docs](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-container-registry)
