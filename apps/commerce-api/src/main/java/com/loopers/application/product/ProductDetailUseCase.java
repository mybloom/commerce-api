package com.loopers.application.product;


import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.ProductQuery;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.UserBehaviorEvent;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductDetailUseCase {

    private final BrandService brandService;
    private final ProductService productService;
    private final ApplicationEventPublisher eventPublisher;
    private final RankingService rankingService;

    public ProductQueryResult.CatalogDetailResult findDetail(final Optional<Long> userId, final Long productId) {
        //상품 정보 조회
        ProductQuery.ProductDetailQuery productDetailQuery = productService.retrieveOneByCache(productId);
        if(productDetailQuery == null) {
            throw new CoreException(ErrorType.NOT_FOUND, "유효한 상품을 찾을 수 없습니다.");
        }

        //브랜드 정보 조회
        final Brand brand = brandService.retrieveById(productDetailQuery.brandId())
                .orElseThrow(() -> {
                    log.error("Brand with ID {} does not exist", productDetailQuery.brandId());
                    return new CoreException(ErrorType.BAD_REQUEST, "유효한 상품을 찾을 수 없습니다.");
                });

        //랭킹 정보 조회
        Long rank = rankingService.retrieveRankByDateAndProductId(java.time.LocalDate.now(), productId);

        //이벤트 발행
        UserBehaviorEvent.ProductView event = new UserBehaviorEvent.ProductView(userId, productId);
        eventPublisher.publishEvent(event);

        return ProductQueryResult.CatalogDetailResult.from(brand, productDetailQuery, rank);
    }
}
