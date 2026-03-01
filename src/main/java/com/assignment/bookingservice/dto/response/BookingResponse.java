package com.assignment.bookingservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private Long bookingId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer duration;
    private String status;
    private Long vehicleId;
    private String vehicleName;
    private List<ProfessionalInfo> professionals;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessionalInfo {
        private Long id;
        private String name;
    }

}
