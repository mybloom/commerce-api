package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "brand")
@Entity
public class Brand extends BaseEntity {

    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    private BrandStatus status;

    public static Brand from(String name, String description, BrandStatus status) {
        return Brand.builder()
            .name(name)
            .description(description)
            .status(status)
            .build();
    }
}
