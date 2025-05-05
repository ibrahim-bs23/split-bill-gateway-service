package com.brainstation.ib.gateway.configuration;

import com.brainstation.ib.gateway.domain.dto.ApiResponseBody;
import com.brainstation.ib.gateway.util.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Order(-100)
@Configuration
public class ErrorHandlerConfig implements ErrorWebExceptionHandler {

    private Locale locale(ServerWebExchange exchange) {
        Locale targetLocale = Locale.getDefault();
        final String language1 = exchange.getRequest().getQueryParams().getFirst("language");
        final String language2 = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        if (language1 != null && !language1.isEmpty()) {
            targetLocale = Locale.forLanguageTag(language1);
        } else if (language2 != null && !language2.isEmpty()) {
            targetLocale = Locale.forLanguageTag(language2);
        }
        return targetLocale;
    }

    @Override
    @org.jetbrains.annotations.NotNull
    public Mono<Void> handle(@Nullable ServerWebExchange exchange, @Nullable Throwable ex) {
        assert ex != null;
        assert exchange != null;
        return this.errorHandle(exchange, ex);
    }

    private Mono<Void> errorHandle(ServerWebExchange serverWebExchange, Throwable ex) {
        log.error(ex.getMessage());
        final var statusCode = Objects.requireNonNullElse(serverWebExchange.getResponse().getRawStatusCode(), 500);
        final var apiResponse = new ApiResponseBody<>("EU" + statusCode, null, null);
        if (ex instanceof DataAccessResourceFailureException) {
            apiResponse.setResponseCode("EU" + 500).setResponseMessage("Gateway db connection refused");
        } else if (ex instanceof HttpMessageNotReadableException) {
            apiResponse.setResponseCode("EU" + 400).setResponseMessage("Failed parse request body content");
        } else if (ex instanceof MethodArgumentNotValidException) {
            apiResponse.setResponseCode("EU" + 400).setResponseMessage(getMessageCode(ex));
        } else if (ex instanceof WebExchangeBindException) {
            apiResponse.setResponseCode("EU" + 400).setResponseMessage(getMessageCode(ex));
        } else if (ex instanceof ServerWebInputException) {
            apiResponse.setResponseCode("EU" + 409).setResponseMessage("Failed parse input data");
        } else if (ex instanceof BadSqlGrammarException) {
            apiResponse.setResponseCode("EU" + 500).setResponseMessage("Gateway bad sql message");
        } else if (ex instanceof ResponseStatusException exception) {
            if (exception.getStatusCode().value() == 503) {
                apiResponse.setResponseCode("EU" + exception.getStatusCode().value()).setResponseMessage("Unable to process your request. Please try again later");
            } else {
                apiResponse.setResponseCode("EU" + exception.getStatusCode().value()).setResponseMessage(exception.getReason());
            }
        } else if (ex.getMessage() != null) {
            if (ex.getMessage().contains("conflicted with the FOREIGN KEY")) {
                apiResponse.setResponseCode("EU" + 409).setResponseMessage("Gateway DB conflicted relational value");
            } else if (ex.getMessage().contains("duplicate key")) {
                apiResponse.setResponseCode("EU" + 409).setResponseMessage("Duplicate value not allow");
            }
        } else {
            apiResponse.setResponseMessage("Internal service error");
        }

        serverWebExchange.getResponse().setStatusCode(HttpStatus.OK);
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        final byte[] jsonByte = Objects.requireNonNull(JacksonUtil.objectToJson(apiResponse)).getBytes();
        DataBuffer dataBuffer = serverWebExchange.getResponse().bufferFactory().wrap(jsonByte);
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    private String getMessageCode(Throwable exception) {
        final Map<String, String> errors = new HashMap<>();
        if (exception instanceof MethodArgumentNotValidException ex) {
            ex.getBindingResult().getAllErrors().forEach((error) -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));
        } else if (exception instanceof WebExchangeBindException ex) {
            ex.getBindingResult().getAllErrors().forEach((error) -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));
        }
        for (Map.Entry<String, String> entry : errors.entrySet()) {
            return errors.get(entry.getValue());
        }
        return "Gateway invalid data";
    }
}
