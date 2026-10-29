package com.clinic.appointment.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID id);

    List<Doctor> findBySpecializationIgnoreCaseOrderByNameAsc(String specialization);

    List<Doctor> findAllByOrderByNameAsc();
}
