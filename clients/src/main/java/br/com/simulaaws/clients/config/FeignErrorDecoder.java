package br.com.simulaaws.clients.config;

import feign.FeignException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error on method: {}, status: {}, reason: {}", methodKey, response.status(), response.reason());
        return FeignException.errorStatus(methodKey, response);
    }
}

