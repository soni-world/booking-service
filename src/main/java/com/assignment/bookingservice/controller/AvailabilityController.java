package com.assignment.bookingservice.controller;

import com.assignment.bookingservice.dto.response.AvailabilityResponse;
import com.assignment.bookingservice.service.AvailabilityService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/v1/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "check for the professional who are available")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @Parameter(description = "Date of service", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "Start time (HH:mm)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,

            @Parameter(description = "Duration in hours (2 or 4)")
            @RequestParam(required = false) Integer duration){

        AvailabilityResponse response = availabilityService.getAvailableProfessionals(date, startTime, duration);
        return ResponseEntity.ok(response);

    }

}
