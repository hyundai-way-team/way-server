package com.hyundai.way.domain.invitation.dto.request;

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
