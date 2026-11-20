package com.hyundai.way.domain.pickup.controller;

import com.hyundai.way.domain.pickup.domain.Pickup;
import com.hyundai.way.domain.pickup.repository.PickupRepository;
import com.hyundai.way.domain.pickup.service.PickupService;
import com.hyundai.way.domain.vehicle.domain.Vehicle;
import com.hyundai.way.domain.vehicle.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PickupController.class)
@Import(PickupService.class)
class PickupControllerTests {

    private static final String REGISTERED_VIN = "DEMO-VIN-001";
    private static final String REQUEST_BODY = """
            {
              "requestId": "request-1",
              "passenger": {"name": "테스트 동승자"},
              "pickupLocation": {"address": "테스트 장소", "latitude": 37.5, "longitude": 127.0}
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VehicleRepository vehicleRepository;

    @MockitoBean
    private PickupRepository pickupRepository;

    @Test
    void createsPickupForExistingVehicle() throws Exception {
        Vehicle vehicle = Vehicle.create(REGISTERED_VIN);
        when(vehicleRepository.findByVehicleKey(REGISTERED_VIN)).thenReturn(Optional.of(vehicle));
        when(pickupRepository.save(any(Pickup.class))).thenAnswer(invocation -> {
            Pickup pickup = invocation.getArgument(0);
            ReflectionTestUtils.setField(pickup, "id", 101L);
            return pickup;
        });

        mockMvc.perform(post("/api/v1/pickups")
                        .header("VIN", REGISTERED_VIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pickupId").value(101));

        ArgumentCaptor<Pickup> savedPickup = ArgumentCaptor.forClass(Pickup.class);
        verify(pickupRepository).save(savedPickup.capture());
        assertThat(savedPickup.getValue().getVehicle()).isSameAs(vehicle);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    void rejectsMissingOrBlankVin(String vin) throws Exception {
        var request = post("/api/v1/pickups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(REQUEST_BODY);
        if (vin != null) {
            request.header("VIN", vin);
        }

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        verifyNoInteractions(vehicleRepository, pickupRepository);
    }

    @Test
    void rejectsUnregisteredVin() throws Exception {
        String vin = "DEMO-VIN-002";
        when(vehicleRepository.findByVehicleKey(vin)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/pickups")
                        .header("VIN", vin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("VEHICLE_NOT_REGISTERED"));

        verifyNoInteractions(pickupRepository);
    }

    @Test
    void rejectsVinWhenDatabaseLookupReturnsOnlyACaseInsensitiveMatch() throws Exception {
        String vin = "demo-vin-001";
        when(vehicleRepository.findByVehicleKey(vin)).thenReturn(Optional.of(Vehicle.create(REGISTERED_VIN)));

        mockMvc.perform(post("/api/v1/pickups")
                        .header("VIN", vin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REQUEST_BODY))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("VEHICLE_NOT_REGISTERED"));

        verifyNoInteractions(pickupRepository);
    }
}
