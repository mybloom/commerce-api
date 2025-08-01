package com.loopers.domain.brand;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BrandService {

    private final BrandRepository brandRepository;

    public Optional<Brand> retrieveById(Long brandId) {
        return brandRepository.findById(brandId);
    }

    public List<Brand> getBrandsOfProducts(List<Long> brandIds) {
        return brandRepository.findAllById(brandIds);
    }
}
