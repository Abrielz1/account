package ru.example.account.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import ru.example.account.security.entity.ActiveSessionCache;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.security.repository.dao.RedisRepositoryDao;
import java.util.UUID;

@Configuration
public class RedisConfig {
    // "РАБОТЯГА", ЗАТОЧЕННЫЙ ПОД "ЧЕРНЫЙ СПИСОК"
    @Bean
    // Бин, который умеет работать с ЛЮБЫМИ ключами и значениями.
    // Для простоты, здесь реализация без дженериков, а в самом DAO - да.
    public RedisRepository redisRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        return new RedisRepositoryDao<>(redisTemplate, objectMapper, String.class); // <<<--- Создаем наш "универсальный DAO"
    }
    // "РАБОТЯГА", ЗАТОЧЕННЫЙ ПОД "БЕЛЫЙ СПИСОК"
    @Bean
    public RedisRepository<UUID, ActiveSessionCache> redisActiveSessionRepository(
            StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        // Мы ЯВНО говорим: "Работай с ActiveSessionCache!"
        // Упс, ключ у нас тоже должен быть String, это мой косяк в дизайне DAO
        // Правильно будет так:
        return new RedisRepositoryDao<>(redisTemplate, objectMapper, ActiveSessionCache.class);
    }
}
