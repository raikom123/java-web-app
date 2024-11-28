package com.example.bookmanage.service;

import com.example.bookmanage.domain.Book;
import com.example.bookmanage.exception.BookNotFoundException;
import com.example.bookmanage.form.BookManagementForm;

/**
 * 書籍管理システムのサービス
 */
public interface BookManageService {

    /**
     * フォーム情報の初期化を行う。
     * 
     * @return フォーム情報
     */
    BookManagementForm initForm();

    /**
     * 指定したIDに該当する書籍を取得し、フォーム情報を返却する。
     *
     * @param id 書籍のID
     * @return フォーム情報
     * @throws BookNotFoundException 書籍が取得できない場合に発生する
     */
    BookManagementForm readOneBook(long id) throws BookNotFoundException;

    /**
     * 指定したIDに該当する書籍をフォーム情報の内容に更新する。
     *
     * @param id 書籍のID
     * @param form フォーム情報
     * @return 更新後の書籍
     * @throws BookNotFoundException 書籍が取得できない場合に発生する
     */
    Book updateBook(long id, BookManagementForm form) throws BookNotFoundException;

    /**
     * フォーム情報から書籍を新規作成する
     * 
     * @param form フォーム情報
     * @return 新規作成した書籍
     */
    Book createBook(BookManagementForm form);

    /**
     * 指定したIDに該当する書籍を削除する。
     *
     * @param id 書籍のID
     * @throws BookNotFoundException 書籍が取得できない場合に発生する
     */
    void deleteBook(long id) throws BookNotFoundException;

}
