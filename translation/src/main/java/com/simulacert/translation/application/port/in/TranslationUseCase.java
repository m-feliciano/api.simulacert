package com.simulacert.translation.application.port.in;

import com.simulacert.translation.application.dto.TranslateFieldCommand;
import com.simulacert.translation.application.dto.TranslationResponse;

public interface TranslationUseCase {
    TranslationResponse translateField(TranslateFieldCommand command);
}

