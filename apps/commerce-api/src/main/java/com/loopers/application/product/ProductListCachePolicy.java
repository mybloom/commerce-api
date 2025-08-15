package com.loopers.application.product;

import lombok.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

//todo: @ConfigurationProperties(prefix = "cache.product-list") 방식으로 수정
@Getter
@Component
public class ProductListCachePolicy {

    /** 캐시 이름 */
    private final String cacheName = "prd:list";
    /** 캐시 키 포맷 */
    private final String keyFormat = "brandId-%s:page-%s:size-%s:sort-%s:v1";
//    private final String keyFormat = "brandId-%d:page-%d:size-%d:sort-%s:v1";
    /** 정상 결과 TTL */
    private final Duration ttl = Duration.ofSeconds(60);
    /** null 결과 TTL */
    private final Duration nullTtl = Duration.ofSeconds(20);
}
