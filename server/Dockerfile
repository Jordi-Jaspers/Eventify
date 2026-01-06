# =============================================================================
# EVENTIFY SERVER - SPRING BOOT WITH SLIM JRE
# =============================================================================
# Multi-stage Dockerfile for the Spring Boot backend application
# Creates a minimal JRE using jlink for reduced image size
# =============================================================================

# =============================================================================
# STAGE 1: Create minimal JRE and extract JAR
# =============================================================================
FROM eclipse-temurin:25-jdk-noble AS create-slimjre

# Copy the generated jar file in a temporary directory
RUN mkdir /workspace
WORKDIR /workspace
ADD build/libs/Eventify*.jar /workspace/eventify.jar

# Create a minimal JRE for this application using jlink
RUN jlink \
    --no-header-files \
    --no-man-pages \
    --compress=zip-6 \
    --strip-debug \
    --add-modules java.base,java.compiler,java.desktop,jdk.crypto.cryptoki,java.xml,jdk.unsupported,java.sql,java.naming,java.logging,java.management,java.security.jgss,java.instrument,jdk.zipfs,jdk.attach \
    --output slimjre

# Extract the JAR file so it can be run directly from disk
RUN mkdir unpacked && cd unpacked && jar -xf /workspace/eventify.jar

# =============================================================================
# STAGE 2: Create production runtime image
# =============================================================================
FROM ubuntu:noble

LABEL maintainer="Jordi Jaspers"
LABEL github="https://www.github.com/jordi-jaspers"
LABEL description="Eventify Spring Boot API Server"

# Setup the Ubuntu environment
RUN export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        apt-utils \
        tzdata \
        fontconfig \
        locales \
        wget \
        curl \
        ca-certificates && \
    echo "en_US.UTF-8 UTF-8" >> /etc/locale.gen && \
    locale-gen en_US.UTF-8 && \
    apt-get dist-upgrade -y && \
    echo "dash dash/sh boolean false" | debconf-set-selections && \
    dpkg-reconfigure dash && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd -g 10001 eventify && \
    useradd --create-home -u 10000 -g 10001 eventify && \
    install -d -m 770 -o eventify -g eventify /opt/workspace/app /opt/workspace/config

# Set ownership of /opt/workspace
RUN chown eventify:eventify /opt/workspace

# From now on, run everything as non-root user
USER eventify

# Switch to our working directory
WORKDIR /opt/workspace

# Install the slim JRE in the container
COPY --from=create-slimjre --chown=eventify:eventify /workspace/slimjre /opt/workspace/slimjre/

# Default Spring profile
ARG SPRING_PROFILES_ACTIVE="dev"

# Default environment variables
ENV SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE \
    TZ="Europe/Amsterdam" \
    JAVA_TOOL_OPTIONS="-XX:+IgnoreUnrecognizedVMOptions -XX:+UseContainerSupport" \
    HEAP_SIZE_MIN="256m" \
    HEAP_SIZE="2048m" \
    METASPACE_SIZE_MIN="128m" \
    METASPACE_SIZE="512m"

# Container health check
HEALTHCHECK --interval=30s --timeout=5s --retries=3 --start-period=30s \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/v1/public/actuator/health || exit 1

# Expose HTTP port
EXPOSE 8080

# Install the unpacked Spring Boot application in the container
COPY --from=create-slimjre --chown=eventify:eventify /workspace/unpacked/BOOT-INF/lib /opt/workspace/app/lib
COPY --from=create-slimjre --chown=eventify:eventify /workspace/unpacked/META-INF /opt/workspace/app/META-INF
COPY --from=create-slimjre --chown=eventify:eventify /workspace/unpacked/BOOT-INF/classes /opt/workspace/app

# Start the Spring Boot process
CMD /opt/workspace/slimjre/bin/java \
    -Xms${HEAP_SIZE_MIN} -Xmx${HEAP_SIZE} \
    -XX:MetaspaceSize=${METASPACE_SIZE_MIN} -XX:MaxMetaspaceSize=${METASPACE_SIZE} \
    -XX:+AlwaysPreTouch \
    -XX:+UseG1GC \
    -Djava.security.egd=file:/dev/urandom \
    -Djava.awt.headless=true \
    -Djava.library.path=/opt/workspace/slimjre/lib/default:/opt/workspace/slimjre/lib:/usr/lib64:/usr/lib:/usr/lib/x86_64-linux-gnu \
    -cp /opt/workspace/app:/opt/workspace/app/lib/* io.github.eventify.Main \
    --spring.output.ansi.enabled=always
