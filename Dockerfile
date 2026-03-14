FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

# Copy gradle wrapper and config first (for caching)
COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle build.gradle
COPY settings.gradle settings.gradle

# Make gradlew executable
RUN chmod +x gradlew

# Copy local libs
COPY libs/ libs/

# Copy source code
COPY api/ api/
COPY common/ common/
COPY nms/ nms/

# Build the plugin
RUN ./gradlew shadowJar --no-daemon --stacktrace

# Output stage - extract the built JAR
FROM scratch AS output
COPY --from=build /app/build/libs/*-all.jar /AxMinions.jar
