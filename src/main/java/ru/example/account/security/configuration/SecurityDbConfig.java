package ru.example.account.security.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = "ru.example.account.security.repository",
        entityManagerFactoryRef = "securityEntityManagerFactory",
        transactionManagerRef = "securityTransactionManager"
)
public class SecurityDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource-security")
    public DataSourceProperties securityDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "securityDataSource")
    public DataSource securityDataSource() {
        return securityDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "securityEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean securityEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("securityDataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("ru.example.account.security.model")
                .persistenceUnit("security")
                .build();
    }

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