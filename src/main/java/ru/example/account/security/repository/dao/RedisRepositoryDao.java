package ru.example.account.security.repository.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import ru.example.account.security.repository.RedisRepository;
import ru.example.account.shared.exception.exceptions.SerializationException;
import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class RedisRepositoryDao<K, V> implements RedisRepository<K, V> {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    private final Class<V> valueType;

    private static final String MESSAGE = "Key or Value MUST not be null";

    private static final String EXCEPTION_MESSAGE = "Key and Value MUST not be null";


    @Override
    public void save(K key, V value, Duration ttl) {

        if (key == null || value == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(EXCEPTION_MESSAGE);
        }

        try {
            redisTemplate.opsForValue().set(
                    serializeKey(key),
                    serializeValue(value),
                    ttl
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Redis data. Key: {}, Value: {}", key, value, e);
            throw new SerializationException("Failed to serialize cache data", e);
        }
    }

    @Override
    public Optional<V> findByKey(K key) {

        if (key == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }

        String stringKey;

        try {
            stringKey = this.serializeKey(key);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize key for Redis lookup. Key: {}", key, e);
            // Если мы даже ключ не можем собрать - дальше идти бессмысленно.
            // Это НЕ ошибка кеша. Это ОШИБКА ВХОДНЫХ ДАННЫХ.
            throw new SerializationException("Failed to process key for lookup", e);
        }


        // Это если в Redis есть наш объект по ключу и он распарсен в корректный Json
        String jsonValue = redisTemplate.opsForValue().get(stringKey);

        if (jsonValue == null) {
            log.trace(MESSAGE);
            return Optional.empty();
        }

        try {
            return Optional.of(this.deserializeValue(jsonValue));

        } catch (IOException e) {
            log.error("""
                               Failed to deserialize value from Redis.
                               Corrupted data will be deleted. Key: {}, Target type: {}
                            """,
                    stringKey, valueType.getSimpleName(), e);
            // На случай не корректных данных в редис, мы их того...
            // Удаляем "битые" данные из Redis и проводим...
            // --- "САМО-ИЗЛЕЧЕНИЕ" С "СЫРЫМ" КЛЮЧОМ! ---
            // Мы УВЕРЕНЫ, что "битые" данные лежат именно по этому ключу.
            try {
                redisTemplate.delete(stringKey);
                log.error("object was deleted with RAW string key: {}", stringKey);
            } catch (Exception deleteEx) {
                log.error("Failed to delete corrupted key: {}", key, deleteEx);
            }

            return Optional.empty();
        }
    }

    @Override
    public void delete(K key) {

        if (key == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }
        //если есть ключ и он не null удаляем по нему объект из Redis
        try {
            this.redisTemplate.delete(this.serializeKey(key));

        } catch (JsonProcessingException deleteEx) {
            log.error("Failed to serialize key for Redis delete. Key: {}", key, deleteEx);
            throw new SerializationException("Failed to process key for delete", deleteEx);
        }
    }

    @Override
    public boolean exists(K key) {

        if (key == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }
        //если есть ключ и он не null проверяем по нему объект из Redis
        try {
            // объект по ключу есть
            return Boolean.TRUE.equals(redisTemplate.hasKey(this.serializeKey(key)));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize key for Redis exists check. Key: {}", key, e);
            throw new SerializationException("Failed to process key for exists check. Key: %s, ex trace: %s".formatted(key, e));
        }
    }

    @Override
    public V findByKeyOrDefault(K key, V defaultValue) {
        return findByKey(key).orElse(defaultValue);
    }

    private String serializeKey(K key) throws JsonProcessingException {
        if (key == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }
        // Для простых типов (String, UUID) это сработает "как есть".
        // SIC! Для сложных ключей-объектов может понадобиться кастомный сериализатор. Помни об этом! ВАЖНО!
        return objectMapper.writeValueAsString(key);
    }

    private String serializeValue(V value) throws JsonProcessingException {
        if (value == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }
        // просто парсим значение в json как строку
        return objectMapper.writeValueAsString(value);
    }

    private V deserializeValue(String jsonValue) throws IOException {

        if (!StringUtils.hasText(jsonValue)) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }
        // а тут наоборот их строки собираем (парсим) объект Json
        return objectMapper.readValue(jsonValue, valueType);
    }
}