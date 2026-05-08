# =============================================================================
# EVENTIFY CLIENT - SVELTEKIT WITH BUN
# =============================================================================
# Multi-stage Dockerfile for the SvelteKit frontend application
# Uses svelte-adapter-bun for SSR support
# =============================================================================

# =============================================================================
# STAGE 1: Build the application
# =============================================================================
FROM oven/bun:alpine AS builder

WORKDIR /app

# Copy package files first for better layer caching
COPY package.json bun.lock ./

# Install dependencies
RUN bun install --frozen-lockfile

# Copy source code
COPY . .

# Build the application
RUN bun run build

# =============================================================================
# STAGE 2: Production runtime
# =============================================================================
FROM oven/bun:alpine AS runtime

LABEL maintainer="Jordi Jaspers"
LABEL github="https://www.github.com/jordi-jaspers"
LABEL description="Eventify SvelteKit Frontend Client"

WORKDIR /app

# Install wget for health checks (not included in alpine by default)
RUN apk add --no-cache wget

# Create non-root user for security
RUN addgroup -g 1001 -S eventify && \
    adduser -S eventify -u 1001 -G eventify

# Copy built application from builder stage
COPY --from=builder --chown=eventify:eventify /app/build ./build
COPY --from=builder --chown=eventify:eventify /app/package.json ./

# Switch to non-root user
USER eventify

# Environment variables
# HOST: Bind to all interfaces for container networking
# PORT: Application port
# ORIGIN: Required by SvelteKit for CSRF protection
ENV HOST=0.0.0.0 \
    PORT=3000 \
    ORIGIN=http://localhost:3000

# Expose application port
EXPOSE 3000

# Health check - verify the application is responding
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=10s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:3000 || exit 1

# Start the application
CMD ["bun", "run", "build/index.js"]
