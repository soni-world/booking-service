package com.assignment.bookingservice.repository;

import com.assignment.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDate(LocalDate date);
}
