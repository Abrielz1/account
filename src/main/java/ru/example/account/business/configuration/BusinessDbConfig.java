package ru.example.account.business.configuration;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = {"ru.example.account.business.repository", "ru.example.account.user.repository"},
        entityManagerFactoryRef = "businessEntityManagerFactory",
        transactionManagerRef = "businessTransactionManager"
)
public class BusinessDbConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource-business")
    public DataSourceProperties businessDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "businessDataSource")
    @Primary
    public DataSource businessDataSource() {
        return businessDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name = "businessEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean businessEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("businessDataSource") DataSource dataSource) {

        return builder
                .dataSource(dataSource)
                .packages("ru.example.account.business.entity", "ru.example.account.user.entity")
                .persistenceUnit("business")
                .build();
    }

    @Bean(name = "businessTransactionManager")
    @Primary
    public PlatformTransactionManager businessTransactionManager(
            @Qualifier("businessEntityManagerFactory") LocalContainerEntityManagerFactoryBean factory) {
        return new JpaTransactionManager(Objects.requireNonNull(factory.getObject()));
    }

    @Bean
    @Primary
    public Flyway businessFlyway(@Qualifier("businessDataSource") DataSource dataSource) {
        return Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/business")
                .schemas("business")
                .load();
    }

    @Bean
    public FlywayMigrationInitializer businessFlywayInitializer(Flyway businessFlyway) {
        return new FlywayMigrationInitializer(businessFlyway);
    }
}