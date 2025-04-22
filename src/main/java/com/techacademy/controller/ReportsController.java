package com.techacademy.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    // 日報一覧画面
    @GetMapping
    public String list(@AuthenticationPrincipal UserDetail userDetail, Model model) {
        List<Reports> reportsList = reportsService.getReportsList(userDetail);
        model.addAttribute("reportsList", reportsList);
        model.addAttribute("listSize", reportsList.size());
        return "reports/list"; // 日報一覧画面に遷移
    }

    // 日報詳細画面
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") int id, Model model) {
        Reports reports = reportsService.findById(id);
        if (reports == null) {
            return "redirect:/reports"; // 日報が見つからない場合は一覧画面へ
        }
        model.addAttribute("reports", reports);
        return "reports/detail"; // 日報詳細画面に遷移
    }

    // 日報新規登録画面
    @GetMapping("/add")
    public String create( Reports reports, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        reports = new Reports();
        reports.setEmployee(userDetail.getEmployee());
        model.addAttribute("reports", reports);
        model.addAttribute("employeeName", userDetail.getEmployee().getName());
        return "reports/new";
    }

    // 日報新規登録処理
    @PostMapping("/add")
    public String create(@Validated Reports reports, BindingResult res, @AuthenticationPrincipal UserDetail userDetail, Model model) {
       //タイトルまたは内容が空白かチェック
        if ("".equals(reports.getTitle()) || "".equals(reports.getContent())) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.BLANK_ERROR),
            ErrorMessage.getErrorValue(ErrorKinds.BLANK_ERROR));
            model.addAttribute("reports", reports);
            return "reports/new";
        }

        // 入力チェック
        if (res.hasErrors()) {
            model.addAttribute("reports", reports);
            return "reports/new";
        }

        try {
            ErrorKinds result = reportsService.save(reports, userDetail);
            if (ErrorMessage.contains(result)) {
                model.addAttribute(ErrorMessage.getErrorName(result),ErrorMessage.getErrorValue(result));
                model.addAttribute("reports", reports);
                return "reports/new";
            }
        } catch (DataIntegrityViolationException e) {
            model.addAttribute(ErrorMessage.getErrorName(ErrorKinds.DUPLICATE_EXCEPTION_ERROR),
                    ErrorMessage.getErrorValue(ErrorKinds.DUPLICATE_EXCEPTION_ERROR));
            model.addAttribute("reports", reports);
            return "reports/new";
        }

        return "redirect:/reports";
    }

    // 日報更新画面
    @GetMapping("/{id}/update")
    public String update(@PathVariable("id") int id, Model model) {
        Reports reports = reportsService.findById(id);
        if (reports == null) {
            return "redirect:/reports";
        }
        model.addAttribute("reports", reports);
        return "reports/update";
        }

    // 日報更新処理
    @PostMapping("/{id}/update")
    public String update(@PathVariable("id") int id, @Validated @ModelAttribute("report") Reports reports, BindingResult res, Model model) {
        String title = reports.getTitle();
        String content = reports.getContent();

        // タイトル・内容の文字数チェック
        if (title != null && title.length() > 100) {
            res.rejectValue("title", null, "タイトルは100文字以下で入力してください");
        }
        if (content != null && content.length() > 600) {
            res.rejectValue("content", null, "内容は600文字以下で入力してください");
        }
        if (res.hasErrors()) {
            return"reports/update";
        }
        // パスIDとオブジェクトIDの整合性チェック
        if (reports.getId() == null || !reports.getId().equals(id)) {
            return "redirect:/reports";
        }
        // 更新処理
        ErrorKinds result = reportsService.update(reports, null);
        if (result != ErrorKinds.SUCCESS) {
            model.addAttribute("reports", reports);
            model.addAttribute("updateError",ErrorMessage.getErrorValue(result));
            return"reports/update";
        }
        return"redirect:/reports";
    }
    // 日報削除処理
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") int id, @AuthenticationPrincipal UserDetail userDetail, Model model) {
        ErrorKinds result = reportsService.deleteReport(id, userDetail);
        if (ErrorMessage.contains(result)) {
            model.addAttribute(ErrorMessage.getErrorName(result), ErrorMessage.getErrorValue(result));
            model.addAttribute("reports", reportsService.findById(id));
            return detail(id,model);
        }
        return"redirect:/reports";
    }
}
