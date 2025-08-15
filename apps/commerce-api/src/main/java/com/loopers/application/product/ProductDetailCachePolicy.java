package com.loopers.application.product;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.Duration;

//todo: @ConfigurationProperties(prefix = "cache.product-list") 방식으로 수정
@Getter
@Component
public class ProductDetailCachePolicy {

    /** 캐시 이름 */
    private final String cacheName = "prd:detail";
    /** 캐시 키 포맷 */
    private final String keyFormat = "productId-%s:v1";
    /** 정상 결과 TTL */
    private final Duration ttl = Duration.ofSeconds(20);
    /** null 결과 TTL */
    private final Duration nullTtl = Duration.ofSeconds(2);
}
