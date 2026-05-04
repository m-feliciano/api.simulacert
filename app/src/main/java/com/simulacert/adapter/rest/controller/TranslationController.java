package com.simulacert.adapter.rest.controller;

import com.simulacert.translation.application.dto.TranslateFieldCommand;
import com.simulacert.translation.application.dto.TranslationResponse;
import com.simulacert.translation.application.port.in.TranslationUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/translations")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationUseCase useCase;

    @PostMapping("/translate")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.OK)
    public TranslationResponse translate(@Valid @RequestBody TranslateFieldCommand command) {
        return useCase.translateField(command);
    }
}

