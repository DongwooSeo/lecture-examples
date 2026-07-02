package com.growmighty.lectures.firstday.tangledmonolith.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/*
    Presentation 계층에서 응답 형식을 통일시키기 위해 공통 응답 객체 생성
    - 데이터 있는 성공 응답
    - 데이터 없는 성공 응답
    - 실패 응답
    - ApiError : 오류 코드와 예외 처리에서 작성한 메시지를 묶어줌.
*/

// null인 필드는 JSON에서 제외하라.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, T data, ApiError error) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ApiError error) {
        return new ApiResponse<>(false, null, error);
    }

    public record ApiError(String code, String message){}
}
