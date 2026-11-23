package rw.qtglobal.diary.config;

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.lettuce.core.ClientOptions;

import rw.qtglobal.diary.payload.EntryDto;
import rw.qtglobal.diary.payload.StatsDto;

/**
 * Redis cache (cache-aside via Spring's @Cacheable / @CacheEvict).
 * <ul>
 *   <li>{@value #ENTRY_STATS}: dashboard statistics per user, key = userId</li>
 *   <li>{@value #ENTRIES}: single diary entries, key = userId:entryId (the owner is part of the key)</li>
 * </ul>
 * Values are stored as readable JSON. Keys look like {@code mydiary:entryStats::42}.
 * If Redis is unavailable, cache errors are logged and the call falls through to PostgreSQL.
 */
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    public static final String ENTRY_STATS = "entryStats";
    public static final String ENTRIES = "entries";

    @Bean
    RedisCacheManagerBuilderCustomizer redisCaches(AppProperties props, ObjectMapper mapper) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .prefixCacheNameWith("mydiary:")
                .disableCachingNullValues();
        return builder -> builder
                .withCacheConfiguration(ENTRY_STATS, base
                        .entryTtl(props.cache().statsTtl())
                        .serializeValuesWith(SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(mapper, StatsDto.class))))
                .withCacheConfiguration(ENTRIES, base
                        .entryTtl(props.cache().entryTtl())
                        .serializeValuesWith(SerializationPair.fromSerializer(
                                new Jackson2JsonRedisSerializer<>(mapper, EntryDto.class))));
    }

    /**
     * While Redis is unreachable, reject cache commands immediately instead of queueing them until the
     * timeout, so an outage adds no latency. The client keeps reconnecting in the background.
     */
    @Bean
    LettuceClientConfigurationBuilderCustomizer failFastWhenDisconnected() {
        return builder -> builder.clientOptions(ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .build());
    }

    /** Never fail a request because of the cache: log the error and read from the database instead. */
    @Override
    public CacheErrorHandler errorHandler() {
        return new LoggingCacheErrorHandler();
    }
}
