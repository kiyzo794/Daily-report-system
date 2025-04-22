package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Reports;

public interface ReportsRepository extends JpaRepository<Reports, Integer> {

    Optional<Reports> findById(int reportId);

    List<Reports> findByEmployeeCode(String employeeCode);

    List<Reports> findByDeleteFlgFalse();

    List<Reports> findByEmployeeCodeAndDeleteFlgFalse(String employeeCode);

    boolean existsByEmployeeCodeAndReportDate(String code, LocalDate reportDate);

    boolean existsByEmployeeCodeAndReportDateAndIdNot(String code, LocalDate reportDate, int id);
}