package com.example.bookmanage.form;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.example.bookmanage.domain.Book;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 書籍管理システムの画面のフォーム情報
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookManageForm {

    /**
     * タイトル
     */
    @NotBlank
    @Size(max = 30, message="{validation.max-size}")
    private String title;

    /**
     * 著者
     */
    @NotBlank
    @Size(max = 20, message="{validation.max-size}")
    private String author;

    /**
     * 新規登録か否か
     */
    private boolean newBook;
    
    /**
     * バージョン
     */
    private long version;

    /**
     * 書籍の一覧
     */
    private List<Book> books;

    /**
     * コンストラクタ
     * 
     * @param newBook 新規登録か否か
     * @param books 書籍の一覧
     */
    public BookManageForm(boolean newBook, List<Book> books) {
        this.newBook = newBook;
        this.books = books;
    }

}
