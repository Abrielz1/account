FROM openjdk:17-jdk-full
LABEL authors="Abriel"
WORKDIR /app
COPY target/subscriptions-0.0.1-SNAPSHOT.jar account.jar
COPY wait-for-it.sh /wait-for-it.sh
RUN chmod +x /wait-for-it.sh
ENTRYPOINT ["/wait-for-it.sh", "account_db:5432", "--", "java", "-jar", "account.jar"]
