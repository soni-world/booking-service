package com.assignment.bookingservice.service;

import com.assignment.bookingservice.dto.response.AvailabilityResponse;
import com.assignment.bookingservice.dto.response.ProfessionalAvailability;
import com.assignment.bookingservice.dto.response.TimeSlot;
import com.assignment.bookingservice.entity.Booking;
import com.assignment.bookingservice.entity.Professional;
import com.assignment.bookingservice.entity.Vehicle;
import com.assignment.bookingservice.exception.InvalidBookingRequestException;
import com.assignment.bookingservice.repository.BookingRepository;
import com.assignment.bookingservice.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    static final LocalTime WORK_START = LocalTime.of(8, 0);
    static final LocalTime WORK_END = LocalTime.of(22, 0);
    static final int BREAK_MINUTES = 30;

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;


    public AvailabilityResponse getAvailableProfessionals(LocalDate date, LocalTime startTime, Integer duration) {

        validateDate(date);
        List<Vehicle> vehicles = vehicleRepository.findAllWithProfessionals();
        List<Booking> dayBookings = bookingRepository.findByDate(date);

        Map<Long, List<Booking>> bookingsByProfessional = buildBookingMap(dayBookings);

        List<ProfessionalAvailability> result;

        if (startTime != null && duration != null) {
            validateTimeWindow(startTime, startTime.plusHours(duration));
            result = getAvailabilityForSlot(vehicles, bookingsByProfessional, startTime, duration);
        } else {
            result = getAvailabilityForDay(vehicles, bookingsByProfessional);
        }

        return new AvailabilityResponse(date, result);

    }

    private List<ProfessionalAvailability> getAvailabilityForDay(
            List<Vehicle> vehicles, Map<Long, List<Booking>> bookingsByProfessional) {
        List<ProfessionalAvailability> result = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            for (Professional professional : vehicle.getProfessionals()) {
                List<Booking> bookings = bookingsByProfessional
                        .getOrDefault(professional.getId(), List.of());
                List<TimeSlot> freeWindows = calculateFreeWindows(bookings);
                if (!freeWindows.isEmpty()) {
                    result.add(new ProfessionalAvailability(
                            professional.getId(),
                            professional.getName(),
                            vehicle.getId(),
                            vehicle.getName(),
                            freeWindows
                    ));
                }
            }
        }

        return result;
    }

    private List<TimeSlot> calculateFreeWindows(List<Booking> bookings) {

        if (bookings.isEmpty()) {
            return List.of(new TimeSlot(WORK_START, WORK_END));
        }

        List<Booking> sorted = bookings.stream()
                .sorted(Comparator.comparing(Booking::getStartTime))
                .toList();

        List<TimeSlot> freeSlots = new ArrayList<>();

        // Gap before first booking
        LocalTime firstStart = sorted.get(0).getStartTime();
        LocalTime freeUntil = firstStart.minusMinutes(BREAK_MINUTES);
        if (WORK_START.isBefore(freeUntil) &&
                Duration.between(WORK_START, freeUntil).toMinutes() >= 120) {
            freeSlots.add(new TimeSlot(WORK_START, freeUntil));
        }

        // Gaps between bookings
        for (int i = 0; i < sorted.size() - 1; i++) {
            LocalTime gapStart = sorted.get(i).getEndTime().plusMinutes(BREAK_MINUTES);
            LocalTime gapEnd = sorted.get(i + 1).getStartTime().minusMinutes(BREAK_MINUTES);
            if (gapStart.isBefore(gapEnd) &&
                    Duration.between(gapStart, gapEnd).toMinutes() >= 120) {
                freeSlots.add(new TimeSlot(gapStart, gapEnd));
            }
        }

        // Gap after last booking
        LocalTime lastEnd = sorted.get(sorted.size() - 1).getEndTime().plusMinutes(BREAK_MINUTES);
        if (lastEnd.isBefore(WORK_END) &&
                Duration.between(lastEnd, WORK_END).toMinutes() >= 120) {
            freeSlots.add(new TimeSlot(lastEnd, WORK_END));
        }

        return freeSlots;


    }

    private List<ProfessionalAvailability> getAvailabilityForSlot(
            List<Vehicle> vehicles,
            Map<Long, List<Booking>> bookingsByProfessional,
            LocalTime startTime,
            int duration) {
        LocalTime endTime = startTime.plusHours(duration);
        List<ProfessionalAvailability> result = new ArrayList<>();

        for (Vehicle vehicle : vehicles) {
            for (Professional professional : vehicle.getProfessionals()) {
                List<Booking> bookings = bookingsByProfessional
                        .getOrDefault(professional.getId(), List.of());
                if (isProfessionalAvailable(startTime, endTime, bookings)) {
                    result.add(new ProfessionalAvailability(
                            professional.getId(),
                            professional.getName(),
                            vehicle.getId(),
                            vehicle.getName(),
                            List.of(new TimeSlot(startTime, endTime))
                    ));
                }
            }
        }

        return result;

    }

    private boolean isProfessionalAvailable(
            LocalTime requestedStart, LocalTime requestedEnd, List<Booking> existingBookings) {
        if (requestedStart.isBefore(WORK_START) || requestedEnd.isAfter(WORK_END)) {
            return false;
        }

        for (Booking booking : existingBookings) {
            LocalTime blockedFrom = booking.getStartTime().minusMinutes(BREAK_MINUTES);
            LocalTime blockedUntil = booking.getEndTime().plusMinutes(BREAK_MINUTES);

            if (requestedStart.isBefore(blockedUntil) && requestedEnd.isAfter(blockedFrom)) {
                return false;
            }
        }
        return true;
    }

    public void validateTimeWindow(LocalTime start, LocalTime end) {

        if (start.isBefore(WORK_START)) {
            throw new InvalidBookingRequestException(
                    "Appointments cannot start before " + WORK_START);
        }
        if (end.isAfter(WORK_END)) {
            throw new InvalidBookingRequestException(
                    "Appointments must finish by " + WORK_END);
        }
    }

    public Map<Long, List<Booking>> buildBookingMap(List<Booking> dayBookings) {
        return dayBookings.stream()
                .flatMap(b -> b.getProfessionals().stream()
                        .map(p -> Map.entry(p.getId(), b)))
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())
                ));
    }

    public void validateDate(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            throw new InvalidBookingRequestException("Bookings are not allowed on Fridays");
        }
    }

    public void validateDuration(int duration) {
        if (duration != 2 && duration != 4) {
            throw new InvalidBookingRequestException("Duration must be 2 or 4 hours");
        }
    }

    public void validateProfessionalCount(int count) {
        if (count < 1 || count > 3) {
            throw new InvalidBookingRequestException("Professional count must be 1, 2, or 3");
        }
    }

    public List<Professional> findAvailableProfessionalsAcrossVehicles(
            LocalTime start,
            LocalTime end,
            Map<Long, List<Booking>> bookingsByProfessional,
            int count,
            Long preferredVehicleId
    ) {
        List<Vehicle> vehicles = vehicleRepository.findAllWithProfessionals();

        if (preferredVehicleId != null) {
            vehicles.sort((a, b) ->
                    a.getId().equals(preferredVehicleId) ? -1 :
                            b.getId().equals(preferredVehicleId) ? 1 : 0
            );
        }

        for (Vehicle vehicle : vehicles) {
            List<Professional> free = vehicle.getProfessionals().stream()
                    .filter(p -> isProfessionalAvailable(
                            start, end,
                            bookingsByProfessional.getOrDefault(p.getId(), List.of())
                    ))
                    .toList();

            if (free.size() >= count) {
                return free.subList(0, count);
            }
        }

        return List.of();
    }
}
