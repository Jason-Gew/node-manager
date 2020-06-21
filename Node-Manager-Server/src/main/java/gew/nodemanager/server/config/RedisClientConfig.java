package gew.nodemanager.server.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author Jason/GeW
 * @since 2020-03-24
 */
@Log4j2
@Configuration
@EnableRedisRepositories
public class RedisClientConfig {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final RedisSerializer<String> STRING_SERIALIZER = new StringRedisSerializer();

    private static Jackson2JsonRedisSerializer<Object> SERIALIZER = new Jackson2JsonRedisSerializer<>(Object.class);

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        OBJECT_MAPPER.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        SERIALIZER.setObjectMapper(OBJECT_MAPPER);

        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(STRING_SERIALIZER);
        redisTemplate.setValueSerializer(SERIALIZER);
        redisTemplate.setHashKeySerializer(STRING_SERIALIZER);
        redisTemplate.setHashValueSerializer(SERIALIZER);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }

    @Bean(name = "redisMsgListenContainer")
    public RedisMessageListenerContainer redisMsgListenContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        return container;
    }
}
