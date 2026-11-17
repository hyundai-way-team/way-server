package com.hyundai.way.global.exception;

import com.hyundai.way.global.exception.ErrorResponse.FieldErrorDetail;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public @Nullable ResponseEntity<Object> handleBusinessException(BusinessException exception, WebRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        return handleExceptionInternal(
                exception, ErrorResponse.from(errorCode),
                new HttpHeaders(),
                errorCode.getStatus(), request);
    }

    @ExceptionHandler(Exception.class)
    public @Nullable ResponseEntity<Object> handleUnexpectedException(Exception exception, WebRequest request) {
        return handleExceptionInternal(
                exception, null, new HttpHeaders(),
                CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus(), request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        if (fieldErrors.stream().anyMatch(FieldError::isBindingFailure)) {
            return handleExceptionInternal(exception, null, headers, status, request);
        }

        List<FieldErrorDetail> errors = fieldErrors.stream()
                .map(error -> fieldError(error.getField(), error))
                .toList();

        return handleExceptionInternal(exception, validationFailure(errors), headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        if (exception.isForReturnValue()) {
            return handleExceptionInternal(exception, null, headers, status, request);
        }

        List<FieldErrorDetail> errors = new ArrayList<>();
        for (var result : exception.getParameterValidationResults()) {
            if (result instanceof ParameterErrors parameterErrors) {
                List<FieldError> fieldErrors = parameterErrors.getFieldErrors();
                if (fieldErrors.stream().anyMatch(FieldError::isBindingFailure)) {
                    return handleExceptionInternal(exception, null, headers, status, request);
                }
                fieldErrors.forEach(error -> errors.add(fieldError(error.getField(), error)));
            } else {
                String field = parameterName(result.getMethodParameter());
                result.getResolvableErrors().forEach(error -> errors.add(fieldError(field, error)));
            }
        }

        return handleExceptionInternal(exception, validationFailure(errors), headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ErrorResponse body = new ErrorResponse(CommonErrorCode.BAD_REQUEST.getCode(), "요청 본문을 확인해 주세요.");
        return handleExceptionInternal(exception, body, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(
            Exception exception, @Nullable Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("요청 처리 중 오류가 발생했습니다.", exception);
        }
        return super.handleExceptionInternal(exception, body, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> createResponseEntity(
            @Nullable Object body, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ErrorResponse response = body instanceof ErrorResponse errorResponse ? errorResponse : commonFailure(status);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.putAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(response, responseHeaders, status);
    }

    private ErrorResponse validationFailure(List<FieldErrorDetail> errors) {
        ErrorCode errorCode = CommonErrorCode.BAD_REQUEST;
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors);
    }

    private ErrorResponse commonFailure(HttpStatusCode status) {
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            if (errorCode.getStatus().value() == status.value()) {
                return ErrorResponse.from(errorCode);
            }
        }
        String code = String.format(Locale.ROOT, "COMMON-%04d", status.value());
        String message = status.is5xxServerError() ? CommonErrorCode.INTERNAL_SERVER_ERROR.getMessage()
                : "요청을 처리할 수 없습니다.";
        return new ErrorResponse(code, message);
    }

    private FieldErrorDetail fieldError(String field, MessageSourceResolvable error) {
        String message = error.getDefaultMessage();
        return new FieldErrorDetail(field, message != null ? message : CommonErrorCode.BAD_REQUEST.getMessage());
    }

    private String parameterName(MethodParameter parameter) {
        for (var annotation : parameter.getParameterAnnotations()) {
            String name = switch (annotation) {
                case RequestParam value -> value.name().isBlank() ? value.value() : value.name();
                case PathVariable value -> value.name().isBlank() ? value.value() : value.name();
                case RequestHeader value -> value.name().isBlank() ? value.value() : value.name();
                case CookieValue value -> value.name().isBlank() ? value.value() : value.name();
                default -> "";
            };
            if (!name.isBlank()) {
                return name;
            }
        }
        String name = parameter.getParameterName();
        return name != null ? name : "arg" + parameter.getParameterIndex();
    }
}
