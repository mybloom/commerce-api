package com.loopers.domain.commonvo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LikeCountConverter implements AttributeConverter<LikeCount, Integer> {

    @Override
    public Integer convertToDatabaseColumn(LikeCount attribute) {
        return attribute != null ? attribute.getValue() : null;
    }

    @Override
    public LikeCount convertToEntityAttribute(Integer dbData) {
        return dbData != null ? LikeCount.from(dbData) : null;
    }
}
