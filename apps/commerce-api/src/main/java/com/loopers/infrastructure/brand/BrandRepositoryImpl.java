package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class BrandRepositoryImpl implements BrandRepository {

    private final BrandJpaRepository brandJpaRepository;

    @Override
    public Optional<Brand> findById(Long brandId) {
        return brandJpaRepository.findById(brandId);
    }

    @Override
    public Brand save(Brand brand) {
        return brandJpaRepository.save(brand);
    }

    @Override
    public boolean existsById(Long brandId) {
        return brandJpaRepository.existsById(brandId);
    }

    @Override
    public List<Brand> findAllById(List<Long> brandIds) {
        return brandJpaRepository.findAllById(brandIds);
    }
}
