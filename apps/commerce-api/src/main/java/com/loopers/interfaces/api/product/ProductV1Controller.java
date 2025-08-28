package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductDetailUseCase;
import com.loopers.application.product.ProductQueryResult;
import com.loopers.application.product.ProductQueryResult.ListViewResult;
import com.loopers.application.product.ProductListUseCase;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductV1Controller implements ProductV1ApiSpec {

    private final ProductListUseCase productListUseCase;
    private final ProductDetailUseCase productDetailUseCase;

    @GetMapping
    @Override
    public ApiResponse<ProductV1Dto.ListViewResponse> retrieveListView(
            @RequestHeader(name = "X-USER-ID", required = false) Long userId,
            @Valid @ModelAttribute ProductV1Dto.ListViewRequest request
    ) {
        ListViewResult listViewResult = productListUseCase.findList(
                Optional.ofNullable(request.brandId()),
                Optional.ofNullable(request.sortCondition()),
                Optional.ofNullable(request.pagingCondition())
        );
        return ApiResponse.success(ProductV1Dto.ListViewResponse.from(listViewResult));
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.DetailViewResponse> retrieveDetailView(
            @RequestHeader(name = "X-USER-ID", required = false) Long userId,
            @PathVariable("productId") Long productId) {
        ProductQueryResult.CatalogDetailResult result = productDetailUseCase.findDetail(productId);
        return ApiResponse.success(ProductV1Dto.DetailViewResponse.from(result));
    }
}
