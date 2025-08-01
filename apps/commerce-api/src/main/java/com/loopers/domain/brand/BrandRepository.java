package com.loopers.domain.brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {

    Optional<Brand> findById(Long brandId);

    Brand save(Brand brand);

    boolean existsById(Long brandId);

    List<Brand> findAllById(List<Long> brandIds);
}
