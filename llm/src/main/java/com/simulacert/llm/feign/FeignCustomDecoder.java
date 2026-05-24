package com.simulacert.llm.feign;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignCustomDecoder implements ErrorDecoder {

    @Override
    public FeignException decode(String methodKey, Response response) {
        log.error("[ErrorDecoder] Method: {}, Status: {}, Reason: {}", methodKey, response.status(), response.reason());
        log.error("[ErrorDecoder] Headers: {}", response.headers());

        if (response.body() != null) {
            try (java.io.InputStream bodyStream = response.body().asInputStream()) {
                if (bodyStream != null) {
                    log.error("[ErrorDecoder] Body: {}", new String(bodyStream.readAllBytes()));
                } else {
                    log.error("[ErrorDecoder] Body stream is null");
                }
            } catch (Exception e) {
                log.error("[ErrorDecoder] Failed to read response body", e);
            }
        }

        return FeignException.errorStatus(methodKey, response);
    }
}
