package com.loopers.config.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(value = "datasource.redis")
public class RedisProperties {

    private int database;
    private RedisNodeInfo master;
    private List<RedisNodeInfo> replicas;

}
