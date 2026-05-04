package com.simulacert.adapter.rest.controller.param;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum ContentLanguage {
    PT_BR("pt_br"),
    EN("en");

    private final String headerValue;

    public String headerValue() {
        return headerValue;
    }

    public static ContentLanguage fromHeader(String raw) {
        if (raw == null || raw.isBlank()) {
            return PT_BR;
        }

        String normalized = raw.trim().toLowerCase();

        return Arrays.stream(values())
                .filter(v -> v.headerValue.equals(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid x-content-language. Allowed values: pt_br, en"
                ));
    }
}

