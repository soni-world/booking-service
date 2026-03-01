package com.assignment.bookingservice.repository;

import com.assignment.bookingservice.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDate(LocalDate date);

    @Query("SELECT b FROM Booking b JOIN FETCH b.professionals p JOIN FETCH p.vehicle WHERE b.id = :id")
    Optional<Booking> findByIdWithProfessionals(@Param("id") Long id);
}
