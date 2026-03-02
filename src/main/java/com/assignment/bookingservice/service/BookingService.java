package com.assignment.bookingservice.service;

import com.assignment.bookingservice.dto.request.BookingCreateRequest;
import com.assignment.bookingservice.dto.request.BookingUpdateRequest;
import com.assignment.bookingservice.dto.response.BookingResponse;
import com.assignment.bookingservice.entity.Booking;
import com.assignment.bookingservice.entity.Professional;
import com.assignment.bookingservice.exception.BookingNotFoundException;
import com.assignment.bookingservice.exception.NoAvailabilityException;
import com.assignment.bookingservice.exception.NoAvailabilityProfessionalException;
import com.assignment.bookingservice.repository.BookingRepository;
import com.assignment.bookingservice.repository.ProfessionalRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

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
        booking.setProfessionals(new ArrayList<>(professionals));

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

    @Transactional
    public BookingResponse updateBooking(Long bookingId, BookingUpdateRequest request) {
        Booking existing = bookingRepository.findById(bookingId).orElseThrow(
                () -> new BookingNotFoundException("Booking not found for given ID: " + bookingId));
        availabilityService.validateDate(request.getDate());
        LocalTime newStartTime = request.getStartTime();
        LocalTime newEndTime = newStartTime.plusHours(existing.getDuration());
        availabilityService.validateTimeWindow(newStartTime, newEndTime);

        int requiredCount = existing.getProfessionals().size();
        Long originalVehicleId = existing.getProfessionals().get(0).getVehicle().getId();

        // Get all bookings for the new date, EXCLUDING current booking
        // (solving the same as new booking flow so the professional is free)
        List<Booking> newDayBooking = bookingRepository.findByDate(request.getDate())
                .stream().filter(b -> !b.getId().equals(bookingId)).toList();
        Map<Long, List<Booking>> bookingMap = availabilityService.buildBookingMap(newDayBooking);

        List<Professional> professionals = availabilityService.findAvailableProfessionalsAcrossVehicles(
                newStartTime, newEndTime, bookingMap, requiredCount, originalVehicleId
        );

        if (professionals.isEmpty()) {
            throw new NoAvailabilityException("No vehicle has " + requiredCount + " available professionals for the requested time");
        }

        List<Professional> oldProfessionals = new ArrayList<>(existing.getProfessionals());

        // Update booking
        existing.setDate(request.getDate());
        existing.setStartTime(newStartTime);
        existing.setEndTime(newEndTime);
        existing.setProfessionals(new ArrayList<>(professionals));;

        bookingRepository.save(existing);

        // process of updating the version of selected professional
        Set<Long> versionIds = new HashSet<>();

        for (Professional p : oldProfessionals) {
            versionIds.add(p.getId());
            professionalRepository.save(p);
        }

        for (Professional p : professionals) {
            if (!versionIds.contains(p.getId())) {
                professionalRepository.save(p);
            }

        }

        return toResponse(existing);
    }

    public BookingResponse getBooking(Long bookingId) {
        Booking booking = bookingRepository.findByIdWithProfessionals(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with id: " + bookingId));
        return toResponse(booking);
    }
}
