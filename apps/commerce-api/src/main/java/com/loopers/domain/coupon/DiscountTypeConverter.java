package com.loopers.domain.coupon;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class DiscountTypeConverter implements AttributeConverter<DiscountType, String> {

    @Override
    public String convertToDatabaseColumn(DiscountType attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public DiscountType convertToEntityAttribute(String dbData) {
        return dbData != null ? DiscountType.valueOf(dbData) : null;
    }
}
