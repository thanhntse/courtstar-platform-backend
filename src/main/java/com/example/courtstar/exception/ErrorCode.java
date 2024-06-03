package com.example.courtstar.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCOUNT_EXIST(1001,"account already exist",HttpStatus.BAD_REQUEST),
    EMAIL_INVALID(1002,"email is invalid",HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1003,"password must be at least 6 characters",HttpStatus.BAD_REQUEST),
    PHONE_INVALID(1004,"phone number is invalid",HttpStatus.BAD_REQUEST),
    NOT_FOUND_USER(1005,"user not found",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006,"unauthenticated",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007,"unauthorized",HttpStatus.FORBIDDEN),
    NOT_DELETE(1008,"not delete",HttpStatus.NOT_ACCEPTABLE),
    KEY_INVALID(1009,"key is invalid",HttpStatus.BAD_REQUEST),
    OTP_ERROR(1010,"otp error",HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

    int code;
    String message;
    HttpStatusCode httpStatusCode;
}
