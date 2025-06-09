FROM eclipse-temurin:17-jdk-alpine
LABEL authors="Abriel"
WORKDIR /app
COPY target/subscriptions-0.0.1-SNAPSHOT.jar account.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
ENTRYPOINT ["/wait-for-it.sh", "account_db:5432", "--", "/wait-for-it.sh", "redis:6379", "--", "java", "-jar", "app.jar"]
