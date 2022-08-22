package com.example.bookmanage.exception;

import java.text.MessageFormat;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

/**
 * 入力内容に不正があった時の例外処理
 */
@SuppressWarnings("serial")
public class BookManageValidationException extends Exception {

    /**
     * コンストラクタ
     * 
     * @param result validatorの結果
     */
    public BookManageValidationException(BindingResult result) {
        super(createMessage(result));
    }

    /**
     * validatorの結果からエラーメッセージを生成します。
     * 
     * @param result validatorの結果
     * @return エラーメッセージ
     */
    private static String createMessage(BindingResult result) {
        return result.getFieldErrors().stream()
                .map(BookManageValidationException::createMessage)
                .reduce("validation error!", (s1, s2) -> { return s1 + System.lineSeparator() + s2; });
    }

    /**
     * フィールドエラーからエラーメッセージを生成します。
     *
     * @param e フィールドエラー
     * @return エラーメッセージ
     */
    private static String createMessage(FieldError e) {
        return MessageFormat.format(e.getDefaultMessage(), (Object[]) e.getCodes());
    }

}
