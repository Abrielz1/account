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
            throw new SerializationException("Failed to process key for lookup", e);
        }

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

        try {

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

        return objectMapper.writeValueAsString(key);
    }

    private String serializeValue(V value) throws JsonProcessingException {
        if (value == null) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }

        return objectMapper.writeValueAsString(value);
    }

    private V deserializeValue(String jsonValue) throws IOException {

        if (!StringUtils.hasText(jsonValue)) {
            log.trace(MESSAGE);
            throw new IllegalArgumentException(MESSAGE);
        }

        return objectMapper.readValue(jsonValue, valueType);
    }
}
