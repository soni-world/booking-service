package com.assignment.bookingservice.controller;


import com.assignment.bookingservice.dto.request.BookingCreateRequest;
import com.assignment.bookingservice.dto.response.BookingResponse;
import com.assignment.bookingservice.service.BookingFacade;
import com.assignment.bookingservice.service.BookingService;
import io.swagger.v3.oas.annotations.OpenAPI31;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Create, Update and manage bookings")
public class BookingController {

    private final BookingFacade bookingFacade;
    private final BookingService bookingService;

    @PostMapping
    @Operation(
            summary = "Create a booking",
            description = "Creates a new booking with the specified number of professionals from the same vehicle."
    )
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request){
        BookingResponse response = bookingFacade.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
