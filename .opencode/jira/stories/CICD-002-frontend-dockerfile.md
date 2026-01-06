# Frontend Dockerfile for SvelteKit/Bun

**Epic**: CI/CD & Deployment Pipeline
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-01-06

## 1. User Story
**As a** developer
**I want** a Dockerfile for the SvelteKit frontend application
**So that** the frontend can be containerized and deployed alongside the backend

## 2. Business Context & Value
The frontend uses SvelteKit with the Bun adapter, which requires a runtime server (not static files) to support SSR features like authentication guards and server-side data loading. A Dockerfile is needed to containerize this application for deployment to the test environment.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Dockerfile builds successfully
    *   Given the `client/Dockerfile` exists
    *   When `docker build -t eventify-client ./client` is run from the repo root
    *   Then the image builds without errors
    *   And the final image size is reasonable (< 200MB)

*   [ ] **Scenario 2**: Container starts and serves the application
    *   Given the Docker image is built
    *   When the container is started with `docker run -p 3000:3000 eventify-client`
    *   Then the application is accessible at `http://localhost:3000`
    *   And SSR routes work correctly (authentication redirects function)

*   [ ] **Scenario 3**: Environment variables are configurable
    *   Given the container is running
    *   When environment variables like `PUBLIC_API_URL` are provided
    *   Then the application uses those values for API requests

*   [ ] **Scenario 4**: Health check is configured
    *   Given the container is running
    *   When Docker performs a health check
    *   Then the container reports healthy when the app is responding

*   [ ] **Scenario 5**: Non-root user for security
    *   Given the container is running
    *   When inspecting the running process
    *   Then the application runs as a non-root user

## 4. Technical Requirements

### File Location
*   `client/Dockerfile`

### Base Image
*   Use official `oven/bun:1.3-alpine` or `oven/bun:1.3-slim` for minimal size
*   Multi-stage build: build stage + runtime stage

### Build Process
1.  **Stage 1 (build)**: Install dependencies, run `bun run build`
2.  **Stage 2 (runtime)**: Copy only the built output, minimal runtime

### SvelteKit Bun Adapter Output
*   The `svelte-adapter-bun` produces a `build/` directory
*   Entry point: `build/index.js` (run with `bun run build/index.js`)
*   Static assets: `build/client/` directory

### Environment Variables
```dockerfile
ENV HOST=0.0.0.0
ENV PORT=3000
ENV ORIGIN=http://localhost:3000
# API URL should be configurable at runtime
ENV PUBLIC_API_URL=http://eventify-server:8080
```

### Exposed Ports
*   Port `3000` (configurable via `PORT` env var)

### Health Check
```dockerfile
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000/health || exit 1
```
Note: May need to add a `/health` endpoint or check the root path.

### Security
*   Run as non-root user
*   No secrets in the image
*   Use `.dockerignore` to exclude unnecessary files

## 5. Design & UI/UX
*   N/A - Infrastructure only

## 6. Implementation Notes / Research

### Current Frontend Setup
*   **Package manager**: Bun 1.3.0+
*   **Framework**: SvelteKit 2.x with Svelte 5
*   **Adapter**: `svelte-adapter-bun` v1.0.1
*   **Build command**: `bun run build` (via Vite)

### SSR Features in Use (from research)
*   `hooks.server.ts`: Auth guard checking cookies, redirecting unauthenticated users
*   `+layout.server.ts`: Reading sidebar state from cookies
*   These require a running server, NOT static file serving

### API Proxying Consideration
The frontend needs to communicate with the backend. Options:
1.  **Direct API calls**: Frontend calls `eventify-server:8080` directly (container networking)
2.  **Traefik routing**: Frontend calls same domain, Traefik routes `/api/*` to backend

Recommendation: Use Traefik routing (simpler for browser requests, avoids CORS)

### Suggested Dockerfile Structure
```dockerfile
# Build stage
FROM oven/bun:1.3-alpine AS builder
WORKDIR /app
COPY package.json bun.lock ./
RUN bun install --frozen-lockfile
COPY . .
RUN bun run build

# Runtime stage
FROM oven/bun:1.3-alpine AS runtime
WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 -S eventify && \
    adduser -S eventify -u 1001 -G eventify

# Copy built application
COPY --from=builder --chown=eventify:eventify /app/build ./build
COPY --from=builder --chown=eventify:eventify /app/package.json ./

USER eventify

ENV HOST=0.0.0.0
ENV PORT=3000

EXPOSE 3000

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

CMD ["bun", "run", "build/index.js"]
```

### .dockerignore File
Create `client/.dockerignore`:
```
node_modules
.svelte-kit
build
.env
.env.*
*.log
.git
```

### Dependencies
*   **Depends on**: None
*   **Blocks**: CICD-001 (Docker Image Build & Push)

### Testing Locally
```bash
cd client
docker build -t eventify-client .
docker run -p 3000:3000 -e PUBLIC_API_URL=http://localhost:8080 eventify-client
```

### References
*   [svelte-adapter-bun documentation](https://github.com/gornostay25/svelte-adapter-bun)
*   [Bun Docker images](https://hub.docker.com/r/oven/bun)
*   [SvelteKit deployment docs](https://kit.svelte.dev/docs/adapters)
