package ru.example.account.security.repository;

import java.time.Duration;
import java.util.Optional;

/**
 * БАЗОВЫЙ, "универсальный" контракт для работы с ЛЮБЫМИ объектами в Redis.
 * Полностью параметризован типами Ключа (K) и Значения (V).
 */
public interface RedisRepository<K, V> {

    /**
     * Сохраняет ЛЮБОЙ объект в Redis по ключу.
     * @param key   Ключ (типа K)
     * @param value Объект для сохранения (типа V)
     * @param ttl   Время жизни
     */
    void save(K key, V value, Duration ttl);

    /**
     * Находит ЛЮБОЙ объект в Redis по ключу.
     * @param key Ключ (типа K)
     * @return Optional, содержащий найденный и десериализованный объект.
     */
    Optional<V> findByKey(K key);

    void delete(K key);

    boolean exists(K key);

    default V findByKeyOrDefault(K key, V defaultValue) {
        return findByKey(key).orElse(defaultValue);
    }
}