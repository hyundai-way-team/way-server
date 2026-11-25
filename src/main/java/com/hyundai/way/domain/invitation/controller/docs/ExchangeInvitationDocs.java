package com.hyundai.way.domain.invitation.controller.docs;

import com.hyundai.way.domain.invitation.dto.response.ExchangeInvitationResponse;
import com.hyundai.way.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(name = "access_token", in = ParameterIn.COOKIE, required = false,
        description = "최초 교환 시 생략할 수 있습니다. 사용된 코드로 재진입할 때는 같은 픽업에 대한 유효한 동승자 세션 쿠키가 필요합니다.",
        schema = @Schema(type = "string"))
@Operation(
        summary = "동승자 참여 코드 교환",
        description = """
                동승자가 '픽업 보기'를 누르면 문자 초대 링크의 `#code`에서 읽은 참여 코드를 요청 본문으로 보냅니다.
                유효한 미사용 코드로 처음 교환하면 동승자 세션을 발급합니다. 사용된 코드로 재진입할 때는
                브라우저가 보낸 `access_token` 쿠키를 확인한 뒤 기존 픽업 식별자를 반환합니다.
                """,
        responses = {
                @ApiResponse(
                        responseCode = "200",
                        description = """
                                최초 교환과 재진입 모두 `pickupId`를 반환합니다.
                                최초 교환에서만 `Set-Cookie`로 `access_token`을 발급합니다. 재진입 시에는 기존 쿠키를 사용합니다.
                                """,
                        headers = @Header(name = "Set-Cookie", description = "최초 교환 시 발급하는 동승자 세션 쿠키입니다.",
                                schema = @Schema(type = "string",
                                        example = "access_token={token}; Path=/; Secure; HttpOnly; SameSite=Lax")),
                        content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ExchangeInvitationResponse.class),
                                examples = {
                                        @ExampleObject(name = "최초 교환", description = "Set-Cookie와 함께 반환합니다.",
                                                value = "{\"pickupId\": \"123\"}"),
                                        @ExampleObject(name = "동승자 재진입", description = "기존 세션을 사용하며 Set-Cookie는 생략합니다.",
                                                value = "{\"pickupId\": \"123\"}")
                                })),
                @ApiResponse(
                        responseCode = "400",
                        description = """
                                `INVALID_REQUEST` — 요청 본문이 없거나 JSON을 읽을 수 없거나 `participationCode`에
                                숫자·불리언·배열·객체를 보내면 본문을 읽을 수 없다는 응답을 반환합니다.
                                `participationCode`가 누락되었거나 `null`이면 필드 검증에 실패합니다. 빈 문자열, 공백만 있는
                                문자열, 255자를 넘는 문자열도 필드 검증에 실패하며 `errors`에 필드 경로와 메시지를 반환합니다.
                                """,
                        content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = {
                                        @ExampleObject(name = "문자열이 아닌 참여 코드", value = """
                                                {"code": "INVALID_REQUEST", "message": "요청 본문을 확인해 주세요."}
                                                """),
                                        @ExampleObject(name = "빈 참여 코드", value = """
                                                {
                                                  "code": "INVALID_REQUEST",
                                                  "message": "입력값을 확인해 주세요.",
                                                  "errors": [{"field": "participationCode", "message": "공백일 수 없습니다"}]
                                                }
                                                """)
                                })),
                @ApiResponse(
                        responseCode = "403",
                        description = """
                                `INVITATION_UNAVAILABLE` — 등록되지 않은 코드, 만료된 미사용 코드, 연결된 픽업이 없거나 완료된 경우,
                                또는 사용된 코드에 해당하는 유효한 동승자 세션이 없는 경우에 반환합니다.
                                """,
                        content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                schema = @Schema(implementation = ErrorResponse.class),
                                examples = @ExampleObject(value = """
                                        {"code": "INVITATION_UNAVAILABLE", "message": "사용할 수 없는 초대입니다."}
                                        """)))
        }
)
public @interface ExchangeInvitationDocs {
}
