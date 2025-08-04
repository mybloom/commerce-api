package com.loopers.domain.like;

import com.loopers.application.like.LikeResult.LikeDetailResult;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LikeProductService {
    public List<LikeDetailResult> assembleLikeProductInfo(
        List<LikeHistory> likeHistories,
        List<Product> products,
        List<Brand> brands
    ) {
        Map<Long, Brand> brandMap = brands.stream()
            .collect(Collectors.toMap(Brand::getId, Function.identity()));

        Map<Long, Product> productMap = products.stream()
            .collect(Collectors.toMap(Product::getId, Function.identity()));

        return likeHistories.stream()
            .map(history -> {
                Product product = productMap.get(history.getProductId());
                Brand brand = brandMap.get(product.getBrandId());
                return LikeDetailResult.from(product, brand);
            })
            .toList();
    }

}
