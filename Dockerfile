# Dockerfile

# --- СТАДИЯ 1: Сборщик ---
FROM eclipse-temurin:17-jdk as builder
WORKDIR /workspace
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline
COPY src ./src
RUN ./mvnw package -DskipTests

# --- СТАДИЯ 2: Исполнитель ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /workspace/target/account-service-0.0.1-SNAPSHOT.jar account.jar
COPY --from=builder /workspace/wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh

ENTRYPOINT ["/wait-for-it.sh", "business-db:5432", "--", "/wait-for-it.sh", "security-db:5432", "--", "/wait-for-it.sh", "fintech-redis:6379", "--", "java", "-jar", "account.jar"]
