package com.techacademy.constants;

// エラーメッセージ定義
public enum ErrorKinds {

    // エラー内容
    // 空白チェックエラー
    BLANK_ERROR,
    // 半角英数字チェックエラー
    HALFSIZE_ERROR,
    // 桁数(8桁~16桁以外)チェックエラー
    RANGECHECK_ERROR,
    // タイトル文字数(100文字以上)チェックエラー
    TITLE_LENGTH_ERROR,
    // 内容文字数(600文字以上)チェックエラー
    CONTENT_LENGTH_ERROR,
    // 重複チェックエラー
    DUPLICATE_ERROR,
    // ログイン中削除チェックエラー
    LOGINCHECK_ERROR,
    // 日付チェックエラー
    DATECHECK_ERROR,
    // 更新チェックエラー
    NOT_FOUND_ERROR,
    // アクセス権限エラー
    ACCESS_DENIED_ERROR,
    // チェックOK
    CHECK_OK,
    // 正常終了
    SUCCESS,

}
