package com.techacademy.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.data.repository.CrudRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.techacademy.constants.ErrorKinds;
import com.techacademy.entity.Employee;
import com.techacademy.entity.Reports;
import com.techacademy.repository.EmployeeRepository;
import com.techacademy.repository.ReportsRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReportsRepository reportsRepository;
    private final ReportsService reportsService;

    public EmployeeService(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder,ReportsRepository reportsRepository,ReportsService reportsService) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.reportsRepository = reportsRepository;
        this. reportsService = reportsService;
    }

    // 従業員更新
    @Transactional
    public ErrorKinds update(Employee employee) {
        // 1. 既存の従業員情報を取得
        Employee existingEmployee = findByCode(employee.getCode());
        if (existingEmployee == null) {
            return ErrorKinds.NOT_FOUND_ERROR;
        }
        // 2.パスワードが入力されていた場合はチェック&更新
        if (employee.getPassword() != null && !employee.getPassword().isEmpty()) {
            // パスワードチェック
            ErrorKinds result = employeePasswordCheck(employee);
            if (ErrorKinds.CHECK_OK != result) {
                return result;
            }

            // パスワードをセット（ハッシュ化済みが前提）
            existingEmployee.setPassword(employee.getPassword());
        }

        // 3.氏名・権限を更新
             existingEmployee.setName(employee.getName());
             existingEmployee.setRole(employee.getRole());

        // 4.更新日時を更新
             existingEmployee.setUpdatedAt(LocalDateTime.now());

       // 5.保存
             employeeRepository.save(existingEmployee);
             return ErrorKinds.SUCCESS;
            }

    // 従業員保存
    @Transactional
    public ErrorKinds save(Employee employee) {

        // パスワードチェック
        ErrorKinds result = employeePasswordCheck(employee);
        if (ErrorKinds.CHECK_OK != result) {
            return result;
        }

        // 従業員番号重複チェック
        if (findByCode(employee.getCode()) != null) {
            return ErrorKinds.DUPLICATE_ERROR;
        }

        employee.setDeleteFlg(false);

        LocalDateTime now = LocalDateTime.now();
        employee.setCreatedAt(now);
        employee.setUpdatedAt(now);

        employeeRepository.save(employee);
        return ErrorKinds.SUCCESS;
    }
    // 従業員削除
    @Transactional
    public ErrorKinds delete(String code, UserDetail userDetail) {

        // 自分を削除しようとした場合はエラーメッセージを表示
        if (code.equals(userDetail.getEmployee().getCode())) {
            return ErrorKinds.LOGINCHECK_ERROR;
        }
        Employee employee = findByCode(code);
        LocalDateTime now = LocalDateTime.now();
        employee.setUpdatedAt(now);
        employee.setDeleteFlg(true);
        employeeRepository.save(employee); // DBへ保存
        // 削除対象の従業員（employee）に紐づいている、日報のリスト（reportList）を取得
        List<Reports> reportList = reportsRepository.findByEmployee(employee);

        // 日報のリスト（reportList）を拡張for文を使って繰り返し
        for (Reports report : reportList) {
            // 日報（report）のIDを指定して、日報情報を削除
            reportsService.deleteReport(report.getId(),userDetail);
        // }
       }
        /* 削除対象の従業員に紐づいている日報情報の削除：ここまで */
        return ErrorKinds.SUCCESS;
    }
    // 従業員一覧表示処理
    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    // 1件を検索
    public Employee findByCode(String code) {
        // findByIdで検索
        Optional<Employee> option = employeeRepository.findById(code);
        // 取得できなかった場合はnullを返す
        Employee employee = option.orElse(null);
        return employee;
    }

    // 従業員パスワードチェック
    private ErrorKinds employeePasswordCheck(Employee employee) {

        // 従業員パスワードの半角英数字チェック処理
        if (isHalfSizeCheckError(employee)) {

            return ErrorKinds.HALFSIZE_ERROR;
        }

        // 従業員パスワードの8文字～16文字チェック処理
        if (isOutOfRangePassword(employee)) {

            return ErrorKinds.RANGECHECK_ERROR;
        }

        employee.setPassword(passwordEncoder.encode(employee.getPassword()));

        return ErrorKinds.CHECK_OK;
    }

    // 従業員パスワードの半角英数字チェック処理
    private boolean isHalfSizeCheckError(Employee employee) {

        // 半角英数字チェック
        Pattern pattern = Pattern.compile("^[A-Za-z0-9]+$");
        Matcher matcher = pattern.matcher(employee.getPassword());
        return !matcher.matches();
    }

    // 従業員パスワードの8文字～16文字チェック処理
    public boolean isOutOfRangePassword(Employee employee) {

        // 桁数チェック
        int passwordLength = employee.getPassword().length();
        return passwordLength < 8 || 16 < passwordLength;
    }

}

