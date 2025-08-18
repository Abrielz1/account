package ru.example.account.security.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.Map;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = "ru.example.account.security.repository",
        entityManagerFactoryRef = "securityEntityManagerFactory",
        transactionManagerRef = "securityTransactionManager"
)
public class SecurityDbConfig {

    @Bean
    @Primary // Он будет "главным", если что
    @ConfigurationProperties("spring.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    // --- ШАГ 2: БИН ДЛЯ ПОЛУЧЕНИЯ "СВОЙСТВ HIBERNATE" ---
    @Bean
    public HibernateProperties hibernateProperties() {
        return new HibernateProperties();
    }

    // --- ШАГ 3: "РУЧНОЕ" СОЗДАНИЕ EntityManagerFactoryBuilder ---
    // А вот теперь, когда у нас есть все компоненты, мы можем СОЗДАТЬ билдер САМИ,
    // передав ему все нужные "запчасти".
    @Bean
    public EntityManagerFactoryBuilder securityEntityManagerFactoryBuilder(
            JpaProperties jpaProperties,
            HibernateProperties hibernateProperties
    ) {
        // Мы просим у Спринга настройки...
        Map<String, String> jpaProps = jpaProperties.getProperties();

        // ...и явно передаем их в конструктор Билдера!
        return new EntityManagerFactoryBuilder(
                new HibernateJpaVendorAdapter(),
                jpaProps,
                null
        );
    }

    // --- ШАГ 4: ТВОЙ, УЖЕ ИДЕАЛЬНЫЙ, БИН ФАБРИКИ ---
    // Теперь этот метод СРАБОТАЕТ, потому что бин "securityEntityManagerFactoryBuilder"
    // будет создан на предыдущем шаге.
    @Bean(name = "securityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean securityEntityManagerFactory(
            // Он найдет бин, созданный выше!
            @Qualifier("securityEntityManagerFactoryBuilder") EntityManagerFactoryBuilder builder,
            @Qualifier("securityDataSource") DataSource dataSource
    ) {
        return builder
                .dataSource(dataSource)
                .packages("ru.example.account.security.entity") // Я все еще помню про этот косяк
                .persistenceUnit("security")
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource-security")
    public DataSourceProperties securityDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "securityDataSource")
    public DataSource securityDataSource() {
        return securityDataSourceProperties().initializeDataSourceBuilder().build();
    }

//    @Bean(name = "securityEntityManagerFactory")
//    public LocalContainerEntityManagerFactoryBean securityEntityManagerFactory(
//            EntityManagerFactoryBuilder builder,
//            @Qualifier("securityDataSource") DataSource dataSource) {
//
//        return builder
//                .dataSource(dataSource)
//                .packages("ru.example.account.security.entity")
//                .persistenceUnit("security")
//                .build();
//    }

    @Bean(name = "securityTransactionManager")
    public PlatformTransactionManager securityTransactionManager(
            @Qualifier("securityEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(Objects.requireNonNull(factory.getObject()));
    }

    @Bean
    public Flyway securityFlyway(@Qualifier("securityDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/security")
                .schemas("security")
                .load();
    }

    @Bean
    public FlywayMigrationInitializer securityFlywayInitializer(@Qualifier("securityFlyway") Flyway flyway) {
        return new FlywayMigrationInitializer(flyway);
    }
}