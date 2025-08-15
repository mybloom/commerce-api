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
public class ProductV1Controller {

    private final ProductListUseCase productListUseCase;
    private final ProductDetailUseCase productDetailUseCase;

    @GetMapping
    public ApiResponse<ProductV1Dto.ListViewResponse> retrieveListView(
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
    public ApiResponse<ProductV1Dto.DetailViewResponse> retrieveDetailView(@PathVariable("productId") Long productId) {
        ProductQueryResult.CatalogDetailResult result = productDetailUseCase.findDetail(productId);
        return ApiResponse.success(ProductV1Dto.DetailViewResponse.from(result));
    }
}
