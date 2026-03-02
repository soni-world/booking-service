package com.assignment.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalAvailability {

    private Long professionalId;
    private String professionalName;
    private Long vehicleId;
    private String vehicleName;
    private List<TimeSlot> availableSlots;
}
