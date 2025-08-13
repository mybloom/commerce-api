package com.loopers.config.redis;

import io.lettuce.core.ReadFrom;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStaticMasterReplicaConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
@EnableConfigurationProperties(RedisProperties.class)
public class RedisConfig {

    private final RedisProperties redisProperties;

    public static final String CONNECTION_MASTER = "redisConnectionMaster";
    public static final String REDIS_TEMPLATE_MASTER = "redisTemplateMaster";

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Primary
    @Bean
    public LettuceConnectionFactory defaultRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();

        return lettuceConnectionFactory(database, master, replicas,
                builder -> builder.readFrom(ReadFrom.REPLICA_PREFERRED));
    }

    @Qualifier(CONNECTION_MASTER)
    @Bean
    public LettuceConnectionFactory masterRedisConnectionFactory() {
        int database = redisProperties.getDatabase();
        RedisNodeInfo master = redisProperties.getMaster();
        List<RedisNodeInfo> replicas = redisProperties.getReplicas();

        return lettuceConnectionFactory(database, master, replicas,
                builder -> builder.readFrom(ReadFrom.MASTER));
    }

    @Primary
    @Bean
    public RedisTemplate<String, String> defaultRedisTemplate(LettuceConnectionFactory lettuceConnectionFactory) {
        return defaultRedisTemplateInternal(new RedisTemplate<>(), lettuceConnectionFactory);
    }

    @Qualifier(REDIS_TEMPLATE_MASTER)
    @Bean
    public RedisTemplate<String, String> masterRedisTemplate(
            @Qualifier(CONNECTION_MASTER) LettuceConnectionFactory lettuceConnectionFactory) {
        return defaultRedisTemplateInternal(new RedisTemplate<>(), lettuceConnectionFactory);
    }

    private LettuceConnectionFactory lettuceConnectionFactory(
            int database,
            RedisNodeInfo master,
            List<RedisNodeInfo> replicas,
            java.util.function.Consumer<LettuceClientConfiguration.LettuceClientConfigurationBuilder> customizer
    ) {
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder();
        customizer.accept(builder);
        LettuceClientConfiguration lettuceClientConfiguration = builder.build();

        RedisStaticMasterReplicaConfiguration masterReplicaConfig =
                new RedisStaticMasterReplicaConfiguration(master.getHost(), master.getPort());
        masterReplicaConfig.setDatabase(database);

        for (RedisNodeInfo replica : replicas) {
            masterReplicaConfig.addNode(replica.getHost(), replica.getPort());
        }

        return new LettuceConnectionFactory(masterReplicaConfig, lettuceClientConfiguration);
    }

    private <K, V> RedisTemplate<K, V> defaultRedisTemplateInternal(
            RedisTemplate<K, V> template,
            LettuceConnectionFactory connectionFactory
    ) {
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.setConnectionFactory(connectionFactory);
        return template;
    }
}
