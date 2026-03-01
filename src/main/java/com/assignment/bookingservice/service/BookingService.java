package com.assignment.bookingservice.service;

import com.assignment.bookingservice.dto.request.BookingCreateRequest;
import com.assignment.bookingservice.dto.request.BookingUpdateRequest;
import com.assignment.bookingservice.dto.response.BookingResponse;
import com.assignment.bookingservice.entity.Booking;
import com.assignment.bookingservice.entity.Professional;
import com.assignment.bookingservice.exception.NoAvailabilityProfessionalException;
import com.assignment.bookingservice.repository.BookingRepository;
import com.assignment.bookingservice.repository.ProfessionalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ProfessionalRepository professionalRepository;
    private final AvailabilityService availabilityService;

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request) {

        // all validation first
        availabilityService.validateDate(request.getDate());
        availabilityService.validateDuration(request.getDuration());
        availabilityService.validateProfessionalCount(request.getProfessionalCount());

        LocalTime startTime = request.getStartTime();
        LocalTime endTime = startTime.plusHours(request.getDuration());
        availabilityService.validateTimeWindow(startTime, endTime);

        //find bookings for that date and map professional and booking
        List<Booking> dayBookings = bookingRepository.findByDate(request.getDate());
        Map<Long, List<Booking>> bookingMap = availabilityService.buildBookingMap(dayBookings);

        // Find available professionals (no vehicle preference for creation)
        List<Professional> professionals = availabilityService.findAvailableProfessionalsAcrossVehicles(
                startTime, endTime, bookingMap, request.getProfessionalCount(), null
        );

        if (professionals.isEmpty()) {
            throw new NoAvailabilityProfessionalException(
                    "No vehicle has " + request.getProfessionalCount() +
                            " available professionals for the requested time"
            );
        }
        // Create booking
        Booking booking = new Booking();
        booking.setDate(request.getDate());
        booking.setStartTime(startTime);
        booking.setEndTime(endTime);
        booking.setDuration(request.getDuration());
        booking.setStatus("CONFIRMED");
        booking.setProfessionals(professionals);

        booking = bookingRepository.save(booking);

        // Bump version on assigned professionals (optimistic lock)
        for (Professional p : professionals) {
            professionalRepository.save(p);
        }

        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        List<BookingResponse.ProfessionalInfo> profInfos = booking.getProfessionals().stream()
                .map(p -> new BookingResponse.ProfessionalInfo(p.getId(), p.getName()))
                .toList();

        Professional firstProfessional = booking.getProfessionals().get(0);
        return new BookingResponse(
                booking.getId(),
                booking.getDate(),
                booking.getStartTime(),
                booking.getEndTime(),
                booking.getDuration(),
                booking.getStatus(),
                firstProfessional.getVehicle().getId(),
                firstProfessional.getVehicle().getName(),
                profInfos
        );
    }

    public BookingResponse updateBooking(Long bookingId, BookingUpdateRequest request) {
        return null;
    }
}
