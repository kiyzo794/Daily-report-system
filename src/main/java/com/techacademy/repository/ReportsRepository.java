package com.techacademy.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;

public interface ReportsRepository extends JpaRepository<Reports, Integer> {

    Optional<Reports> findById(Integer reportId);

    List<Reports> findByEmployeeCode(String employeeCode);

    List<Reports> findByEmployee(Employee employee);

    List<Reports> findByDeleteFlgFalse();

    List<Reports> findByEmployeeCodeAndDeleteFlgFalse(String employeeCode);
 // 従業員コードと日付で重複チェック
    boolean existsByEmployeeCodeAndReportDate(String code, LocalDate reportDate);
 // IDを除いて従業員コードと日付で重複チェック
    boolean existsByEmployeeCodeAndReportDateAndIdNot(String code, LocalDate reportDate, Integer id);
}