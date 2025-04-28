package com.techacademy.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;
import com.techacademy.repository.ReportsRepository;

@Service
public class ReportsService {

    private final ReportsRepository reportsRepository;

    public ReportsService(ReportsRepository reportsRepository) {
        this.reportsRepository = reportsRepository;
    }

    // 日報一覧取得
    @Transactional
    public List<Reports> getReportsList(UserDetail userDetail) {
        if (userDetail.getEmployee().getRole() == Employee.Role.ADMIN) {
            return reportsRepository.findAll();
        } else {
            return reportsRepository.findByEmployeeCode(userDetail.getEmployee().getCode());
        }
    }

    // 日報1件取得
    public Reports findById(Integer id) {
        Reports report = reportsRepository.findById(id).orElse(null);
        if (report != null && report.getEmployee() != null) {
            // Lazyロード対策
            report.getEmployee().getName();
        }
        return report;
    }

    // 日報保存
    @Transactional
    public ErrorKinds save(Reports report, UserDetail userDetail) {

        if (report.getReportDate() == null || report.getTitle() == null || report.getContent() == null) {
            return ErrorKinds.BLANK_ERROR;
        }

        // 手動で文字数制限チェック（タイトル）
        if (report.getTitle().length() > 100) {
            return ErrorKinds.TITLE_LENGTH_ERROR;
        }
     // 手動で文字数制限チェック（内容）
        if (report.getContent().length() > 600) {
            return ErrorKinds.CONTENT_LENGTH_ERROR;
        }


        // 同日・同社員の日報重複チェック
        LocalDate reportDateOnly = report.getReportDate(); // 時間部分を無視する
        if (reportsRepository.existsByEmployeeCodeAndReportDate(userDetail.getEmployee().getCode(), reportDateOnly)) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        LocalDateTime now = LocalDateTime.now();

        report.setEmployee(userDetail.getEmployee());
        report.setDeleteFlg(false);
        report.setCreatedAt(now);
        report.setUpdatedAt(now);

        reportsRepository.save(report);
        return ErrorKinds.SUCCESS;
    }

    // 日報更新
    @Transactional
    public ErrorKinds update(Reports updatedReport, UserDetail userDetail) {

        Reports existingReport = findById(updatedReport.getId());
        if (existingReport == null) {
            return ErrorKinds.NOT_FOUND_ERROR;
        }

        // 重複チェック
        if (reportsRepository.existsByEmployeeCodeAndReportDateAndIdNot(userDetail.getEmployee().getCode(),updatedReport.getReportDate(),updatedReport.getId())) {
            return ErrorKinds.DATECHECK_ERROR;
        }

        // 手動で文字数制限チェック（タイトル）
        if (updatedReport.getTitle().length() > 100) {
            return ErrorKinds.TITLE_LENGTH_ERROR;
        }
        // 手動で文字数制限チェック（content）
        if (updatedReport.getContent().length() > 600) {
            return ErrorKinds.CONTENT_LENGTH_ERROR;
        }

        existingReport.setReportDate(updatedReport.getReportDate());
        existingReport.setTitle(updatedReport.getTitle());
        existingReport.setContent(updatedReport.getContent());
        existingReport.setUpdatedAt(LocalDateTime.now());

        reportsRepository.save(existingReport);
        return ErrorKinds.SUCCESS;
    }

    // 日報削除
    @Transactional
    public ErrorKinds deleteReport(Integer reportId, UserDetail userDetail) {
        Reports report = findById(reportId);
        if (report == null) {
            return ErrorKinds.NOT_FOUND_ERROR;
        }

        if (!report.getEmployee().getCode().equals(userDetail.getEmployee().getCode())
                && userDetail.getEmployee().getRole() != Employee.Role.ADMIN) {
            return ErrorKinds.ACCESS_DENIED_ERROR;
        }

        report.setDeleteFlg(true);
        report.setUpdatedAt(LocalDateTime.now());
        reportsRepository.save(report);
        return ErrorKinds.SUCCESS;
    }
}
