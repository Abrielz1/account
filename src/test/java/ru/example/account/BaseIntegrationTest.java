package ru.example.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Базовый абстрактный класс для всех интеграционных тестов.
 * <p>
 * Что он делает:
 * 1. {@link Testcontainers}: Интегрирует JUnit 5 с Testcontainers.
 * 2. {@link SpringBootTest}: Поднимает полный Spring-контекст приложения для теста.
 * 3. {@link ActiveProfiles()}: "test"  Активирует тестовый профиль, заставляя Spring читать `application-test.yml`.
 * 4. {@link AutoConfigureMockMvc}: Автоматически настраивает бин {@link MockMvc} для выполнения HTTP-запросов.
 * 5. {@link Container}: Объявляет контейнер PostgreSQL, который будет запущен перед тестами.
 * 6. {@link DynamicPropertySource}: Динамически подставляет свойства (URL, пароли) от запущенных контейнеров
 * в Spring-контекст перед его стартом.
 */
@Testcontainers
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // MOCK быстрее, т.к. не запускает реальный веб-сервер
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    // --- Инструменты для тестов, будут доступны в классах-наследниках ---

    @Autowired
    protected MockMvc mockMvc; // Для отправки HTTP-запросов

    @Autowired
    protected ObjectMapper objectMapper; // Для сериализации/десериализации JSON

    // --- Контейнеры ---

    // Объявляем контейнер с PostgreSQL.
    // 'static' означает, что контейнер запустится ОДИН РАЗ на все тесты во всех классах,
    // которые наследуются от BaseIntegrationTest. Это экономит десятки секунд на запуске тестов.
    @Container
    static final PostgreSQLContainer<?> postgresContainer =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:14.6-alpine"));

    // --- Динамическая конфигурация ---

    /**
     * Этот метод - ключевая магия. Он выполняется ДО старта Spring-контекста
     * и позволяет нам программно задать свойства, которые Spring будет использовать.
     * Эти свойства имеют наивысший приоритет.
     */
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {

        // --- Настройка подключения к PostgreSQL из контейнера ---
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // --- Настройка Flyway для тестовой базы ---
        // Убеждаемся, что Flyway включен и будет накатывать миграции на нашу чистую тестовую базу.
        registry.add("spring.flyway.enabled", () -> "true");

        // --- Настройка JPA/Hibernate для тестов ---
        // 'validate' - хороший выбор. Hibernate проверит, что миграции соответствуют сущностям.
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        // --- Безопасная настройка JWT-секрета для тестов ---
        // Задаем тестовый секрет прямо здесь. Он должен быть в валидном формате Base64,
        // как этого ожидает твой JwtUtils.
        String testJwtSecret = "dGhpc19pc19hX3Zlcnlfc2VjdXJlX3Rlc3Rfa2V5X2Zvcl9qd3RfYXV0aGVudGljYXRpb24K";
        registry.add("app.jwt.secret", () -> testJwtSecret);
    }
}