package com.example.bookmanage.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

/**
 * 書籍管理の例外を処理する
 */
@Slf4j
@ControllerAdvice
public class BookManageExceptionHandler {

    /**
     * 例外を処理する。<br />
     * 例外をログ出力し、エラー画面のHTML名を返却する。
     * 
     * @param e 例外
     * @return エラー画面のHTML名
     */
    @ExceptionHandler(value = {Exception.class})
    public String handleException(Exception e) {
        log.error("system error!", e);
        return "error";
    }

}
