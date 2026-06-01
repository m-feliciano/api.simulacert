package com.simulacert.rest.controller.param;

import org.springframework.core.convert.converter.Converter;

public class ContentLanguageConverter implements Converter<String, ContentLanguage> {

    @Override
    public ContentLanguage convert(String source) {
        return ContentLanguage.fromHeader(source);
    }
}

