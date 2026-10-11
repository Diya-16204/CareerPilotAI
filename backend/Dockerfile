FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
RUN ./mvnw -B -ntp verify

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app
RUN groupadd --system --gid 10001 careerpilot \
    && useradd --system --uid 10001 --gid careerpilot --home-dir /app --shell /usr/sbin/nologin careerpilot \
    && mkdir -p /tmp/careerpilot/uploads \
    && chown -R careerpilot:careerpilot /app /tmp/careerpilot
COPY --from=build --chown=careerpilot:careerpilot /workspace/target/careerpilot-api-0.1.0-SNAPSHOT.jar /app/app.jar

ENV UPLOAD_DIR=/tmp/careerpilot/uploads
EXPOSE 10000
USER 10001:10001
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar"]
