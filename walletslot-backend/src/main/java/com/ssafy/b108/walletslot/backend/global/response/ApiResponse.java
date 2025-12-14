// global/response/ApiResponse.java
package com.ssafy.b108.walletslot.backend.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponse<T> {
    private final boolean success;
    @JsonInclude(JsonInclude.Include.NON_NULL) private final T data;
    @JsonInclude(JsonInclude.Include.NON_NULL) private final Error error;

    public static <T> ApiResponse<T> ok(T data){ return ApiResponse.<T>builder().success(true).data(data).build(); }
    public static ApiResponse<Void> ok(){ return ApiResponse.<Void>builder().success(true).build(); }
    public static ApiResponse<?> error(String code, String message){
        return ApiResponse.builder().success(false).error(new Error(code, message)).build();
    }

    @Getter @AllArgsConstructor
    public static class Error { private final String code; private final String message; }
}
