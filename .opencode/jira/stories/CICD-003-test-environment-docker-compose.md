# Update Test Environment Docker Compose for Two-Container Architecture

**Epic**: CI/CD & Deployment Pipeline
**Status**: Ready for Dev
**Estimate**: S
**Created Date**: 2026-01-06

## 1. User Story
**As a** developer
**I want** the test environment to run separate frontend and backend containers
**So that** each service can be deployed, scaled, and debugged independently

## 2. Business Context & Value
The current eventify-tst docker-compose only defines a single container. With the new architecture (SvelteKit frontend with SSR + Spring Boot backend), we need two separate containers. Traefik will route requests appropriately: API calls to the backend, everything else to the frontend.

## 3. Acceptance Criteria
*   [ ] **Scenario 1**: Both containers start successfully
    *   Given the docker-compose.yml is updated
    *   When `docker-compose up -d` is run
    *   Then both `eventify-client` and `eventify-server` containers start
    *   And both report healthy status

*   [ ] **Scenario 2**: Frontend is accessible via browser
    *   Given both containers are running
    *   When navigating to `https://eventify-tst.jordijaspers.dev/`
    *   Then the SvelteKit frontend loads correctly
    *   And the login page is displayed for unauthenticated users

*   [ ] **Scenario 3**: API requests are routed to backend
    *   Given both containers are running
    *   When the frontend makes a request to `/api/v1/auth/login`
    *   Then the request is routed to the backend container
    *   And the response is returned correctly

*   [ ] **Scenario 4**: Watchtower updates both containers
    *   Given new images are pushed to GHCR
    *   When Watchtower polls the registry (every 60 seconds)
    *   Then both containers are updated to the new images
    *   And a Telegram notification is sent

*   [ ] **Scenario 5**: Backend connects to shared PostgreSQL
    *   Given the `nuc_postgres` container is running
    *   When the backend container starts
    *   Then it successfully connects to the database
    *   And Liquibase migrations run on startup

*   [ ] **Scenario 6**: Containers can communicate internally
    *   Given both containers are on the `internal` network
    *   When the frontend needs to make server-side API calls
    *   Then it can reach the backend via `eventify-server:8080`

## 4. Technical Requirements

### File Location
*   `/opt/projects/server-config/nuc/applications/eventify-tst/docker-compose.yml`
*   (Note: This is on the home server, not in the repo. Provide as documentation/reference.)

### Container Architecture
```
┌─────────────────────────────────────────────────────────────┐
│                         Traefik                              │
│                  (external reverse proxy)                    │
└─────────────────┬───────────────────────┬───────────────────┘
                  │                       │
                  │ /*                    │ /api/*
                  ▼                       ▼
        ┌─────────────────┐     ┌─────────────────┐
        │ eventify-client │     │ eventify-server │
        │   (Bun:3000)    │────▶│  (Java:8080)    │
        └─────────────────┘     └────────┬────────┘
                                         │
                                         ▼
                                ┌─────────────────┐
                                │   nuc_postgres  │
                                │   (PG:5432)     │
                                └─────────────────┘
```

### Traefik Routing Rules
*   **Frontend**: `Host(`eventify-tst.jordijaspers.dev`)` - default route
*   **Backend**: `Host(`eventify-tst.jordijaspers.dev`) && PathPrefix(`/api`)` - higher priority

### Network Configuration
*   `proxy`: External network for Traefik communication
*   `internal`: Internal network for container-to-container and database access

### Environment Variables (Backend)
```yaml
environment:
  - TZ=Europe/Amsterdam
  - SPRING_PROFILES_ACTIVE=dev
  - SPRING_DATASOURCE_URL=jdbc:postgresql://nuc_postgres:5432/eventify
  - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
  - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
  # Add more as needed (OAuth, email, etc.)
```

### Environment Variables (Frontend)
```yaml
environment:
  - TZ=Europe/Amsterdam
  - ORIGIN=https://eventify-tst.jordijaspers.dev
  - PUBLIC_API_URL=https://eventify-tst.jordijaspers.dev
```

### Watchtower Configuration
*   Scope: `eventify-tst` (already configured)
*   Both containers should have label: `com.centurylinklabs.watchtower.scope=eventify-tst`
*   Poll interval: 60 seconds (already configured)

### Database
*   Use existing `nuc_postgres` container
*   Create `eventify` database (if not exists)
*   Connection via `internal` network

## 5. Design & UI/UX
*   N/A - Infrastructure only

## 6. Implementation Notes / Research

### Current Configuration (from research)
```yaml
# Current - Single container
services:
  eventify:
    image: ghcr.io/jordijaspers/eventify:latest-dev
    container_name: eventify
    ...
```

### Proposed Configuration
```yaml
services:
  eventify-server:
    image: ghcr.io/jordijaspers/eventify-server:latest-dev
    container_name: eventify-server
    restart: unless-stopped
    networks:
      - proxy
      - internal
    environment:
      - TZ=Europe/Amsterdam
      - SPRING_PROFILES_ACTIVE=dev
      - SPRING_DATASOURCE_URL=jdbc:postgresql://nuc_postgres:5432/eventify
      - SPRING_DATASOURCE_USERNAME=${POSTGRES_USER}
      - SPRING_DATASOURCE_PASSWORD=${POSTGRES_PASSWORD}
    labels:
      - "traefik.enable=true"
      - "traefik.docker.network=proxy"
      # API routes - higher priority
      - "traefik.http.routers.eventify-api.rule=Host(`eventify-tst.jordijaspers.dev`) && PathPrefix(`/api`)"
      - "traefik.http.routers.eventify-api.entrypoints=https"
      - "traefik.http.routers.eventify-api.tls=true"
      - "traefik.http.routers.eventify-api.priority=100"
      - "traefik.http.routers.eventify-api.service=eventify-api"
      - "traefik.http.services.eventify-api.loadbalancer.server.port=8080"
      - "com.centurylinklabs.watchtower.enable=true"
      - "com.centurylinklabs.watchtower.scope=eventify-tst"
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:8080/api/v1/public/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3

  eventify-client:
    image: ghcr.io/jordijaspers/eventify-client:latest-dev
    container_name: eventify-client
    restart: unless-stopped
    networks:
      - proxy
      - internal
    environment:
      - TZ=Europe/Amsterdam
      - ORIGIN=https://eventify-tst.jordijaspers.dev
      - PUBLIC_API_URL=https://eventify-tst.jordijaspers.dev
    labels:
      - "traefik.enable=true"
      - "traefik.docker.network=proxy"
      # Frontend routes - lower priority (catch-all)
      - "traefik.http.routers.eventify-ui.rule=Host(`eventify-tst.jordijaspers.dev`)"
      - "traefik.http.routers.eventify-ui.entrypoints=https"
      - "traefik.http.routers.eventify-ui.tls=true"
      - "traefik.http.routers.eventify-ui.priority=50"
      - "traefik.http.routers.eventify-ui.service=eventify-ui"
      - "traefik.http.services.eventify-ui.loadbalancer.server.port=3000"
      - "com.centurylinklabs.watchtower.enable=true"
      - "com.centurylinklabs.watchtower.scope=eventify-tst"
    healthcheck:
      test: ["CMD", "wget", "-q", "--spider", "http://localhost:3000"]
      interval: 30s
      timeout: 5s
      retries: 3

  watchtower:
    image: containrrr/watchtower:latest
    container_name: eventify_watchtower
    restart: unless-stopped
    environment:
      - WATCHTOWER_POLL_INTERVAL=60
      - WATCHTOWER_SCOPE=eventify-tst
      - WATCHTOWER_CLEANUP=true
      - WATCHTOWER_NOTIFICATION_URL=${WATCHTOWER_NOTIFICATION_URL}
      - WATCHTOWER_NOTIFICATIONS=shoutrrr
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - /root/.docker/config.json:/config.json:ro  # For GHCR auth
    networks:
      - proxy

networks:
  proxy:
    external: true
  internal:
    external: true
```

### Database Setup (One-time)
Run on `nuc_postgres` to create the database:
```sql
CREATE DATABASE eventify;
```

### Environment File
Create/update `.env` in the eventify-tst directory:
```env
POSTGRES_USER=<from nuc .env>
POSTGRES_PASSWORD=<from nuc .env>
WATCHTOWER_NOTIFICATION_URL=<telegram shoutrrr URL>
```

### Dependencies
*   **Depends on**: CICD-001, CICD-002 (images must exist in GHCR)
*   **Depends on**: CICD-004 (Watchtower needs GHCR auth)

### Rollback Plan
If issues occur, revert to single container:
```bash
docker-compose down
git checkout HEAD~1 docker-compose.yml
docker-compose up -d
```

### References
*   [Traefik routing priority](https://doc.traefik.io/traefik/routing/routers/#priority)
*   [Watchtower scopes](https://containrrr.dev/watchtower/arguments/#scope)
