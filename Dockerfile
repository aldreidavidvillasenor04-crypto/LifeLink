
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw

COPY src src
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Import TiDB Cloud's CA certificate into Java's truststore
COPY certs/isrgrootx1.pem /tmp/isrgrootx1.pem
RUN keytool -importcert -noprompt -trustcacerts \
    -alias tidb-isrg-root-x1 \
    -file /tmp/isrgrootx1.pem \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    && rm /tmp/isrgrootx1.pem

RUN useradd --system --uid 10001 --home-dir /app lifelink

COPY --from=build --chown=lifelink:lifelink /workspace/target/lifelink-0.0.1-SNAPSHOT.jar /app/lifelink.jar

USER lifelink

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=10000

ENTRYPOINT ["java", "-jar", "/app/lifelink.jar"]
