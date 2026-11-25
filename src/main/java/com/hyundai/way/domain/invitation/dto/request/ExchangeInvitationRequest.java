package com.hyundai.way.domain.invitation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;

public record ExchangeInvitationRequest(
        @Schema(description = "문자 초대 링크의 #code에서 읽은 최대 255자의 JSON 문자열입니다. 비어 있거나 공백으로만 이루어진 값은 허용되지 않습니다.",
                requiredMode = Schema.RequiredMode.REQUIRED,
                example = "aBcDeFgHiJkLmNoPqRsTuVwXyZ0123456789_-abcdE")
        @JsonDeserialize(using = ParticipationCodeDeserializer.class)
        @NotBlank @Size(max = 255) String participationCode
) {
    public ExchangeInvitationCommand toCommand(@Nullable String accessToken) {
        return new ExchangeInvitationCommand(accessToken, participationCode);
    }

    public static final class ParticipationCodeDeserializer extends ValueDeserializer<String> {

        @Override
        public String deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
            if (!parser.hasToken(JsonToken.VALUE_STRING)) {
                return context.reportInputMismatch(String.class, "participationCode는 JSON 문자열이어야 합니다.");
            }
            return parser.getString();
        }
    }

}
