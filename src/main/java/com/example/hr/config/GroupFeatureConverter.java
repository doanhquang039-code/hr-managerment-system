package com.example.hr.config;

import com.example.hr.enums.GroupFeature;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GroupFeatureConverter implements AttributeConverter<GroupFeature, String> {

    @Override
    public String convertToDatabaseColumn(GroupFeature attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public GroupFeature convertToEntityAttribute(String dbData) {
        return dbData != null ? GroupFeature.valueOf(dbData) : null;
    }
}
