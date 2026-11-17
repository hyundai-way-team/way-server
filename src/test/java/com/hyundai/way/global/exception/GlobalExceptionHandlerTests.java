package com.hyundai.way.global.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GlobalExceptionHandlerTests.TestController.class)
@Import(GlobalExceptionHandlerTests.TestController.class)
class GlobalExceptionHandlerTests {

    private final MockMvc mockMvc;

    @Autowired
    GlobalExceptionHandlerTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void preservesDomainErrorStatusAndBody() throws Exception {
        mockMvc.perform(get("/test/domain-error"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"INVITATION_UNAVAILABLE","message":"사용할 수 없는 초대입니다."}
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void includesNestedFieldValidationErrors() throws Exception {
        mockMvc.perform(post("/test/pickups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pickupLocation":{"latitude":91}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {
                          "code":"INVALID_REQUEST",
                          "message":"입력값을 확인해 주세요.",
                          "errors":[{"field":"pickupLocation.latitude","message":"위도는 90 이하여야 합니다."}]
                        }
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void returnsRequestBodyGuidanceForMalformedJson() throws Exception {
        mockMvc.perform(post("/test/pickups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"INVALID_REQUEST","message":"요청 본문을 확인해 주세요."}
                        """, JsonCompareMode.STRICT));
    }

    @Test
    void usesRequestParameterNameForMethodValidationErrors() throws Exception {
        mockMvc.perform(get("/test/quantity").param("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors.length()").value(1))
                .andExpect(jsonPath("$.errors[0].field").value("quantity"))
                .andExpect(jsonPath("$.errors[0].message").value("1 이상이어야 합니다."));
    }

    @Test
    void omitsFieldErrorsForTypeMismatch() throws Exception {
        mockMvc.perform(get("/test/quantity").param("quantity", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors").doesNotHaveJsonPath());
    }

    @Test
    void preservesAllowHeaderForUnsupportedMethod() throws Exception {
        mockMvc.perform(get("/test/pickups"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().string(HttpHeaders.ALLOW, containsString("POST")))
                .andExpect(jsonPath("$.code").value("COMMON-0405"))
                .andExpect(jsonPath("$.errors").doesNotHaveJsonPath());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/test/unexpected", "/test/invalid-return"})
    void hidesInternalDetailsInServerErrorResponses(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"code":"COMMON-0500","message":"요청 처리 중 오류가 발생했습니다. 다시 시도해 주세요."}
                        """, JsonCompareMode.STRICT));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/domain-error")
        void domainError() {
            throw new TestBusinessException();
        }

        @PostMapping("/test/pickups")
        void createPickup(@Valid @RequestBody PickupRequest request) {
        }

        @GetMapping("/test/quantity")
        int quantity(@RequestParam("quantity") @Min(value = 1, message = "1 이상이어야 합니다.") int count) {
            return count;
        }

        @GetMapping("/test/unexpected")
        void unexpected() {
            throw new IllegalStateException("internal exception detail");
        }

        @GetMapping("/test/invalid-return")
        @NotBlank(message = "internal validation detail")
        String invalidReturn() {
            return "";
        }
    }

    record PickupRequest(@Valid PickupLocation pickupLocation) {
    }

    record PickupLocation(@DecimalMax(value = "90", message = "위도는 90 이하여야 합니다.") double latitude) {
    }

    private static class TestBusinessException extends BusinessException {

        TestBusinessException() {
            super(new ErrorCode() {
                @Override
                public HttpStatus getStatus() {
                    return HttpStatus.FORBIDDEN;
                }

                @Override
                public String getCode() {
                    return "INVITATION_UNAVAILABLE";
                }

                @Override
                public String getMessage() {
                    return "사용할 수 없는 초대입니다.";
                }
            });
        }
    }
}
