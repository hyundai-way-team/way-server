package com.hyundai.way.domain.pickup.controller;

import com.hyundai.way.domain.pickup.dto.request.PickupCreateCommand;
import com.hyundai.way.domain.pickup.dto.response.PickupCreateResult;
import com.hyundai.way.domain.pickup.service.PickupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PickupController.class)
class PickupControllerTests {

    private static final String REGISTERED_VIN = "DEMO-VIN-001";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PickupService pickupService;

    @Test
    void returnsCreatedLocationAndStringPickupId() throws Exception {
        var command = new PickupCreateCommand("request-1", "12가 3456",
                new PickupCreateCommand.Passenger("테스트 동승자", null),
                new PickupCreateCommand.PickupLocation("테스트 장소", 90.0, -180.0), null);
        String invitationUrl = "https://example.test/p/join#code=sample";
        when(pickupService.createPickup(command, REGISTERED_VIN))
                .thenReturn(new PickupCreateResult(101L, invitationUrl));

        mockMvc.perform(post("/api/v1/pickups")
                        .header("VIN", REGISTERED_VIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "request-1",
                                  "vehicleNumber": "12가 3456",
                                  "passenger": {"name": "테스트 동승자"},
                                  "pickupLocation": {"address": "테스트 장소", "latitude": 90, "longitude": -180}
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, "/api/v1/pickups/101"))
                .andExpect(jsonPath("$.pickupId").isString())
                .andExpect(jsonPath("$.pickupId").value("101"))
                .andExpect(jsonPath("$.invitationWebURL").value(invitationUrl));
    }

    @Test
    void rejectsMissingPassengerBeforeCallingService() throws Exception {
        mockMvc.perform(post("/api/v1/pickups")
                        .header("VIN", REGISTERED_VIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "request-1",
                                  "pickupLocation": {"address": "테스트 장소", "latitude": 37.5, "longitude": 127.0}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[0].field").value("passenger"));

        verifyNoInteractions(pickupService);
    }

    @Test
    void returnsFieldPathsForInvalidNestedValues() throws Exception {
        mockMvc.perform(post("/api/v1/pickups")
                        .header("VIN", REGISTERED_VIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "requestId": "request-1",
                                  "passenger": {"name": " "},
                                  "pickupLocation": {"address": "테스트 장소", "latitude": 90.1, "longitude": -180.1}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.errors[*].field", containsInAnyOrder(
                        "passenger.name", "pickupLocation.latitude", "pickupLocation.longitude")));

        verifyNoInteractions(pickupService);
    }
}
