package ru.example.account.app.security.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import ru.example.account.app.entity.Account;
import ru.example.account.app.entity.EmailData;
import ru.example.account.app.entity.PhoneData;
import ru.example.account.app.entity.User;

import java.time.Duration;
import java.util.Set;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.expiryOfCashDuration}")
    private Duration expiry;

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.addMixIn(User.class, UserMixin.class);
        mapper.addMixIn(Account.class, AccountMixin.class);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(expiry)
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJackson2JsonRedisSerializer(mapper)
                ));
    }

    abstract class UserMixin {
        @JsonIgnore
        abstract Set<PhoneData> getUserPhones();
        @JsonIgnore
        abstract Set<EmailData> getUserEmails();
    }

    abstract class AccountMixin {
        @JsonIgnore
        abstract User getUser();
    }
}
