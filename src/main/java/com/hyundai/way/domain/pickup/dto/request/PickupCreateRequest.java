package com.hyundai.way.domain.pickup.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PickupCreateRequest(
        @Schema(description = "차량 앱이 출발 시도마다 한 번 생성하는 식별자입니다. 같은 시도를 재요청할 때 재사용합니다.",
                example = "pickup-request-example-1")
        @NotBlank @Size(max = 255) String requestId,
        @Schema(description = "동승자 웹 표시용 차량 번호입니다. 현재 생성 API는 이 값을 입력으로만 받습니다.",
                types = {"string", "null"}, example = "12가 3456")
        String vehicleNumber,
        @Schema(description = "초대할 동승자 한 명의 정보입니다.")
        @NotNull @Valid Passenger passenger,
        @Schema(description = "운전자가 마지막으로 확정한 픽업 지점입니다.")
        @NotNull @Valid PickupLocation pickupLocation,
        @Schema(description = "동승자가 탑승한 뒤의 최종 목적지 주소입니다. 입력하지 않으면 필드를 생략하거나 null을 보냅니다.",
                types = {"string", "null"}, example = "서울특별시 마포구 예시로 24")
        @Size(max = 255) String destination
) {

    public PickupCreateCommand toCommand() {
        return new PickupCreateCommand(
                requestId,
                vehicleNumber,
                new PickupCreateCommand.Passenger(passenger.name(), passenger.phoneNumber()),
                new PickupCreateCommand.PickupLocation(
                        pickupLocation.address(), pickupLocation.latitude(), pickupLocation.longitude()),
                destination
        );
    }

    public record Passenger(
            @Schema(description = "동승자 이름입니다.", example = "김서연")
            @NotBlank @Size(max = 255) String name,
            @Schema(description = "차량 앱의 문자 발송에 사용할 동승자 전화번호입니다. 생성 요청에서는 생략하거나 null을 보낼 수 있습니다.",
                    types = {"string", "null"})
            String phoneNumber
    ) {
    }

    public record PickupLocation(
            @Schema(description = "확정한 픽업 지점의 화면 표시용 도로명 주소입니다.", example = "서울특별시 서대문구 예시로 12")
            @NotBlank @Size(max = 255) String address,
            @Schema(description = "확정한 핀의 위도입니다. 단위는 도(°)입니다.", example = "37.55624")
            @NotNull
            @DecimalMin(value = "-90", message = "위도는 -90에서 90 사이여야 합니다.")
            @DecimalMax(value = "90", message = "위도는 -90에서 90 사이여야 합니다.")
            Double latitude,
            @Schema(description = "확정한 핀의 경도입니다. 단위는 도(°)입니다.", example = "126.93683")
            @NotNull
            @DecimalMin(value = "-180", message = "경도는 -180에서 180 사이여야 합니다.")
            @DecimalMax(value = "180", message = "경도는 -180에서 180 사이여야 합니다.")
            Double longitude
    ) {
    }
}
