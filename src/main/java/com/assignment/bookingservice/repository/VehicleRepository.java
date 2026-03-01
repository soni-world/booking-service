package com.assignment.bookingservice.repository;

import com.assignment.bookingservice.entity.Vehicle;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    @Cacheable("vehicleProfessionals")
    @Query("SELECT DISTINCT v FROM Vehicle v JOIN FETCH v.professionals")
    List<Vehicle> findAllWithProfessionals();
}
