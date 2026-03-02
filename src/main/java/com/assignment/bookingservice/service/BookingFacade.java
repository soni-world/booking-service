package com.assignment.bookingservice.service;

import com.assignment.bookingservice.dto.request.BookingCreateRequest;
import com.assignment.bookingservice.dto.request.BookingUpdateRequest;
import com.assignment.bookingservice.dto.response.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingFacade {

    private final BookingService bookingService;

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public BookingResponse createBooking(BookingCreateRequest request) {
        return bookingService.createBooking(request);
    }

    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public BookingResponse updateBooking(Long bookingId, BookingUpdateRequest request) {
        return bookingService.updateBooking(bookingId, request);
    }
}
