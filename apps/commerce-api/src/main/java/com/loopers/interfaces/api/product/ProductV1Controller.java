package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductQueryResult.ListViewResult;
import com.loopers.application.product.ProductUseCase;
import com.loopers.interfaces.api.ApiResponse;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
@RestController
public class ProductV1Controller {

    private final ProductUseCase productUseCase;

    @GetMapping
    public ApiResponse<ProductV1Dto.ListViewResponse> retrieveListView(
        @Valid @ModelAttribute ProductV1Dto.ListViewRequest request
    ) {
        ListViewResult listViewResult = productUseCase.findList(
            Optional.ofNullable(request.brandId()),
            Optional.ofNullable(request.sortCondition()),
            Optional.ofNullable(request.pagingCondition())
        );
        return ApiResponse.success(ProductV1Dto.ListViewResponse.from(listViewResult));
    }
}
