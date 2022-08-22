package com.example.bookmanage.exception;

/**
 * 書籍が存在しない場合の例外処理
 */
@SuppressWarnings("serial")
public class BookNotFoundException extends Exception {

    /**
     * メッセージのフォーマット
     */
    private static final String MESSAGE_FORMAT = "Book is not found. (id = %d)";

    /**
     * コンストラクタ
     * 
     * @param id 書籍のID
     */
    public BookNotFoundException(long id) {
        super(String.format(MESSAGE_FORMAT, id));
    }

}
