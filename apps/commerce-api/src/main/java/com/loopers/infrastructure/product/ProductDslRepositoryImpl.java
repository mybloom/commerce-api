package com.loopers.infrastructure.product;

import com.loopers.domain.brand.BrandStatus;
import com.loopers.domain.brand.QBrand;
import com.loopers.domain.product.ProductListProjection;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.product.QProduct;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Slf4j
@RequiredArgsConstructor
@Repository
public class ProductDslRepositoryImpl implements ProductDslRepository {
    private static final QProduct product = QProduct.product;
    private static final QBrand brand = QBrand.brand;

    private final JPAQueryFactory queryFactory;


    @Override
    public Page<ProductListProjection> findAllForListView(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = pageable.getSort().stream()
            .map(order -> getOrderSpecifier(order, product, brand))
            .collect(Collectors.toList());

        List<ProductListProjection> content = queryFactory
            .select(Projections.constructor(ProductListProjection.class,
                product.id,
                product.name,
                product.price,
                product.likeCount,
                product.status,
                product.saleStartDate,
                product.createdAt,
                brand.id,
                brand.name
            ))
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id)) //연관관계 없이 조인
            .where(
                brand.status.eq(BrandStatus.ACTIVE)
                    .and(product.status.eq(ProductStatus.AVAILABLE))
            )
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id))
            .where(
                brand.status.eq(BrandStatus.ACTIVE)
                    .and(product.status.eq(ProductStatus.AVAILABLE))
            )
            .fetchOne(); // fetchOne()은 결과가 없으면 null 반환

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public Page<ProductListProjection> findAllForListViewByBrand(Long brandId, Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = pageable.getSort().stream()
            .map(order -> getOrderSpecifier(order, product, brand))
            .collect(Collectors.toList());

        List<ProductListProjection> content = queryFactory
            .select(Projections.constructor(ProductListProjection.class,
                product.id,
                product.name,
                product.price,
                product.likeCount,
                product.status,
                product.saleStartDate,
                product.createdAt,
                brand.id,
                brand.name
            ))
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id))
            .where(
                brand.id.eq(brandId)
                    .and(brand.status.eq(BrandStatus.ACTIVE))
                    .and(product.status.eq(ProductStatus.AVAILABLE))
            )
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(product.count())
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id))
            .where(
                brand.id.eq(brandId)
                    .and(brand.status.eq(BrandStatus.ACTIVE))
                    .and(product.status.eq(ProductStatus.AVAILABLE))
            )
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private OrderSpecifier<?> getOrderSpecifier(Sort.Order order, QProduct product, QBrand brand) {
        String property = order.getProperty();
        System.out.println("정렬 필드: " + property);

        // product 기준 정렬
        PathBuilder<?> entity = new PathBuilder<>(product.getType(), product.getMetadata());

        return new OrderSpecifier<>(
            order.isAscending() ? Order.ASC : Order.DESC,
            entity.getComparable(property, Comparable.class)
        );
    }

    @Override
    public boolean existsListViewableByBrandId(Long brandId) {
        Long count = queryFactory
            .select(product.count())
            .from(product)
            .join(brand).on(product.brandId.eq(brand.id))
            .where(
                brand.id.eq(brandId),
                brand.status.eq(BrandStatus.ACTIVE),
                product.status.eq(ProductStatus.AVAILABLE)
            )
            .fetchOne();

        return count != null && count > 0;
    }
}
