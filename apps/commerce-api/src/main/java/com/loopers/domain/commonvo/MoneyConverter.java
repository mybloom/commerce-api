package com.loopers.domain.commonvo;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, Long> {

    @Override
    public Long convertToDatabaseColumn(Money money) {
        return money != null ? money.getAmount() : null;
    }

    @Override
    public Money convertToEntityAttribute(Long dbData) {
        return dbData != null ? new Money(dbData) : null;
    }
}
