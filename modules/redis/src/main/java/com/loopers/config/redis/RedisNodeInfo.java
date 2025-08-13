package com.loopers.config.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@NoArgsConstructor // 기본 생성자 (Spring 바인딩용)
@AllArgsConstructor
@Getter
public class RedisNodeInfo {

    private String host;
    private int port;

}
