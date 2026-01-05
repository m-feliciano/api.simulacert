package com.simulacert.llm.application.port.out;

import com.simulacert.llm.application.dto.LLMRequest;
import com.simulacert.llm.application.dto.LLMResult;

public interface ExplanationLLMPort {
    LLMResult generate(LLMRequest request);
}