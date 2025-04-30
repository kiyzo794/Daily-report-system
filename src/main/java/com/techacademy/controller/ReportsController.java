package com.techacademy.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.constants.ErrorMessage;
import com.techacademy.entity.Reports;
import com.techacademy.service.ReportsService;
import com.techacademy.service.UserDetail;

@Controller
@RequestMapping("reports")
public class ReportsController {

    private final ReportsService reportsService;

    @Autowired
    public ReportsController(ReportsService reportsService) {
        this.reportsService = reportsService;
    }

    // 日報一覧
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {
        List<Reports> reportsList = reportsService.getReportsList(userDetail);
        model.addAttribute("reportsList", reportsList);
        model.addAttribute("listSize", reportsList.size());
        return "reports/list";
    }

    // 日報詳細
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model) {
        Reports reports = reportsService.findById(id);
        if (reports == null) {
            return "redirect:/reports";
        }
        model.addAttribute("reports", reports);
        return "reports/detail";
    }

    // 日報新規登録画面
    @GetMapping("/add")
    public String create(Reports reports, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        reports = new Reports();
        reports.setEmployee(userDetail.getEmployee());
        model.addAttribute("reports", reports);
        model.addAttribute("employeeName", userDetail.getEmployee().getName());
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping("/add")
    public String create(@Valid Reports reports, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        reports.setEmployee(userDetail.getEmployee());

        // 入力チェック
        if (reports.getTitle() != null && reports.getTitle().length() > 100) {
            res.rejectValue("title", ErrorMessage.getErrorName(ErrorKinds.TITLE_LENGTH_ERROR), ErrorMessage.getErrorValue(ErrorKinds.TITLE_LENGTH_ERROR));
        }

        if (reports.getContent() != null && reports.getContent().length() > 600) {
            res.rejectValue("content", ErrorMessage.getErrorName(ErrorKinds.CONTENT_LENGTH_ERROR), ErrorMessage.getErrorValue(ErrorKinds.CONTENT_LENGTH_ERROR));
        }

        if (res.hasErrors()) {
            model.addAttribute("reports", reports);
            return "reports/new";
        }
        // 日報重複エラー
        try {
            ErrorKinds result = reportsService.save(reports, userDetail);
            if (result == ErrorKinds.DATECHECK_ERROR) {
                model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", reports);
                return "reports/new";
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_ERROR));
            model.addAttribute("reports", reports);
            return "reports/new";
        }

        return "redirect:/reports";
    }

    // 日報更新画面
    @GetMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, Model model) {
        Reports reports = reportsService.findById(id);
        if (reports == null) {
            return "redirect:/reports";
        }
        if (reports.getEmployee() != null) {
            reports.getEmployee().getName();
        }
        model.addAttribute("reports", reports);
        return "reports/update";
    }

    // 日報更新処理
    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") Integer id, @Valid @ModelAttribute("reports") Reports reports, BindingResult res,@AuthenticationPrincipal UserDetail userDetail, Model model) {

        if (reports.getTitle() != null && reports.getTitle().length() > 100) {
            res.rejectValue("title", ErrorMessage.getErrorName(ErrorKinds.TITLE_LENGTH_ERROR), ErrorMessage.getErrorValue(ErrorKinds.TITLE_LENGTH_ERROR));
        }

        if (reports.getContent() != null && reports.getContent().length() > 600) {
            res.rejectValue("content", ErrorMessage.getErrorName(ErrorKinds.CONTENT_LENGTH_ERROR), ErrorMessage.getErrorValue(ErrorKinds.CONTENT_LENGTH_ERROR));
        }

        if (res.hasErrors()) {
            reports.setEmployee(userDetail.getEmployee());


            model.addAttribute("reports", reports);
            return "reports/update";
        }

         // パスIDとオブジェクトIDの整合性チェック
        if (reports.getId() == null || !reports.getId().equals(id)) {
            return "redirect:/reports";
        }

        reports.setEmployee(userDetail.getEmployee());

        // 日報重複エラー
        ErrorKinds result = reportsService.update(reports, userDetail);
        if (result == ErrorKinds.DATECHECK_ERROR) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("reports", reports);
            return "reports/update";
        }
        if (result != ErrorKinds.SUCCESS) {
            model.addAttribute("reports", reports);
            model.addAttribute("updateError", ErrorMessage.getErrorValue(result));
            return "reports/update";
        }

        return "redirect:/reports";
    }

    // 日報削除処理
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Integer id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = reportsService.deleteReport(id, userDetail);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("reports", reportsService.findById(id));
            return detail(id, model);
        }
        return "redirect:/reports";
    }
}
