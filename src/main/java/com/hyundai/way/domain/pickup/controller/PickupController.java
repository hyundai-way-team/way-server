package com.hyundai.way.domain.pickup.controller;

import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.request.PickupCreateRequest;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResponse;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.service.PickupService;
import com.hyundai.way.global.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(PickupController.PICKUPS_PATH)
@RequiredArgsConstructor
public class PickupController {

    static final String PICKUPS_PATH = "/api/v1/pickups";

    private final PickupService pickupService;

    @Operation(
            summary = "픽업 생성 및 초대 링크 발급",
            description = """
                    차량 앱이 동승자 정보와 운전자가 확정한 픽업 지점을 보내 픽업 한 건과 초대 링크를 생성합니다.
                    사전 등록된 차량의 `VIN`을 헤더에 보냅니다.

                    - 생성 응답을 받지 못하면 같은 `VIN`, `requestId`, 요청 본문으로 다시 요청합니다.
                      서버는 `VIN`과 `requestId`가 같은 요청을 하나의 출발 시도로 처리합니다.
                      초대 링크가 메모리에 남아 있고 유효하면 동일한 `pickupId`와 `invitationWebURL`을 `201`로 반환합니다.
                    - 초대 링크는 최초 발급 후 30분간 유효합니다. 링크가 만료되었거나 서버 재시작 등으로
                      사라졌다면 `409 INVITATION_LINK_UNAVAILABLE`을 반환합니다.
                    - 차량 앱은 응답받은 `invitationWebURL`을 `#code`까지 포함해 그대로 문자로 보냅니다.
                      문자 발송에 실패하면 같은 링크로 발송을 다시 시도합니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "201", description = "새 픽업과 초대 링크를 생성했거나 기존 결과를 반환했습니다.",
                            headers = @Header(name = "Location", description = "픽업 조회 경로",
                                    schema = @Schema(type = "string", example = "/api/v1/pickups/123")),
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = PickupCreateResponse.class))),
                    @ApiResponse(responseCode = "400", description = """
                            `INVALID_REQUEST` — `VIN` 헤더 누락·공백, 읽을 수 없는 JSON, 필수값 누락 또는 입력값 검증 실패입니다.
                            필드 검증에 실패하면 `errors`에 필드 경로와 메시지를 반환합니다.
                            """,
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(name = "좌표 범위 초과", value = """
                                            {
                                              "code": "INVALID_REQUEST",
                                              "message": "입력값을 확인해 주세요.",
                                              "errors": [{"field": "pickupLocation.latitude", "message": "위도는 -90에서 90 사이여야 합니다."}]
                                            }
                                            """))),
                    @ApiResponse(responseCode = "403", description = "`VEHICLE_NOT_REGISTERED` — 등록되지 않은 차량의 VIN입니다.",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {"code": "VEHICLE_NOT_REGISTERED", "message": "등록되지 않은 차량입니다."}
                                            """))),
                    @ApiResponse(responseCode = "409", description = "`INVITATION_LINK_UNAVAILABLE` — 기존 픽업의 초대 링크를 복구할 수 없습니다.",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ErrorResponse.class),
                                    examples = @ExampleObject(value = """
                                            {"code": "INVITATION_LINK_UNAVAILABLE", "message": "기존 초대 링크를 복구할 수 없습니다."}
                                            """)))
            }
    )
    @PostMapping
    public ResponseEntity<PickupCreateResponse> create(
            @Parameter(description = "Pleos Vehicle SDK에서 읽은 차량 식별값입니다. 로컬 테스트에서는 예시 VIN을 사용합니다.",
                    example = "DEMO-VIN-001")
            @RequestHeader("VIN") @NotBlank String vin,
            @Valid @RequestBody PickupCreateRequest request) {
        PickupCreateCommand command = request.toCommand();
        PickupCreateResult result = pickupService.createPickup(command, vin);

        URI location = URI.create(PICKUPS_PATH + "/" + result.pickupId());
        return ResponseEntity.created(location)
                .body(PickupCreateResponse.create(result.pickupId(), result.invitationWebURL()));
    }
}
