package com.image.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.image.backend.enums.EErrorCode;
import io.swagger.annotations.ApiModelProperty;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;


public class Response<T> {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorObject error = null;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data = null;

    private Response(@NotNull T data) {
        this.data = data;
    }

    private Response(@NotNull String message, @NotNull EErrorCode code) {
        error = new ErrorObject(message, code);
    }

    public static <K> Response<K> ok(K data) {
        return new Response<>(data);
    }

    public static <K> Response<K> notOk(String message, EErrorCode code) {
        return new Response<>(message, code);
    }

    public static <K> Response<K> notOk(ErrorObject error) {
        return new Response<>(error.getMessage(), error.getCode());
    }

    @JsonIgnore
    public boolean isOk() {
        return error == null;
    }

    @JsonIgnore
    public boolean isNotOk() {
        return error != null;
    }

    public ErrorObject getError() {
        return error;
    }

    public T getData() {
        return data;
    }

    public ResponseEntity<Response<T>> toResponseEntity() {
        return isOk() ?
                ResponseEntity.ok(this) :
                ResponseEntity.status(error.code.httpStatusCode()).body(this);
    }

    private static class ErrorObject {

        @ApiModelProperty(example = "400")
        private final EErrorCode code;
        @ApiModelProperty(example = "Bad Request")
        private final String message;

        ErrorObject(String message, EErrorCode code) {
            this.code = code;
            this.message = message;
        }

        public EErrorCode getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
