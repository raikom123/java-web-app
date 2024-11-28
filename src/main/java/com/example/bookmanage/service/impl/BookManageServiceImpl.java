package com.example.bookmanage.service.impl;

import com.example.bookmanage.domain.Book;
import com.example.bookmanage.exception.BookNotFoundException;
import com.example.bookmanage.form.BookManagementForm;
import com.example.bookmanage.repository.BookRepository;
import com.example.bookmanage.service.BookManageService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 書籍管理システムのサービス
 */
@Service
public class BookManageServiceImpl implements BookManageService {

    /**
     * 書籍のリポジトリ
     */
    private final BookRepository bookRepository;

    /**
     * コンストラクタ
     *
     * @param bookRepository 書籍のリポジトリ
     */
    @Autowired
    public BookManageServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * フォーム情報の初期化を行う。
     * 
     * @return フォーム情報
     */
    @Override
    @Transactional(readOnly = true)
    public BookManagementForm initForm() {
        // 一覧を取得する
        List<Book> books = bookRepository.findAll();
        return new BookManagementForm(true, books);
    }

    /**
     * 指定したIDに該当する書籍を取得し、フォーム情報を返却する。
     *
     * @param id 書籍のID
     * @return フォーム情報
     * @throws BookNotFoundException 書籍が取得できない場合に発生する
     */
    @Override
    @Transactional(readOnly = true)
    public BookManagementForm readOneBook(long id) throws BookNotFoundException {
        // IDでエンティティを取得する
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        // 一覧を取得する
        List<Book> books = bookRepository.findAll();
        BookManagementForm form = new BookManagementForm(false, books);

        // エンティティの内容をフォームに反映する
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(book, form);

        return form;
    }

    /**
     * 指定したIDに該当する書籍をフォーム情報の内容に更新する。
     *
     * @param id 書籍のID
     * @param form フォーム情報
     * @return 更新後の書籍
     * @throws BookNotFoundException 書籍が取得できない場合に発生する
     */
    @Override
    @Transactional
    public Book updateBook(long id, BookManagementForm form) throws BookNotFoundException {
        // IDでエンティティを取得する
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        // 楽観排他
        if (book.getVersion() != form.getVersion()) {
            throw new ObjectOptimisticLockingFailureException(Book.class, id);
        }

        // フォームの内容をエンティティに更新する
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(form, book);

        // エンティティの更新
        return bookRepository.save(book);
    }

    /**
     * フォーム情報から書籍を新規作成する
     * 
     * @param form フォーム情報
     * @return 新規作成した書籍
     */
    @Override
    @Transactional
    public Book createBook(BookManagementForm form) {
        // フォーム情報を使って、エンティティを生成する
        ModelMapper modelMapper = new ModelMapper();
        Book book = modelMapper.map(form, Book.class);

        // エンティティを登録する
        return bookRepository.save(book);
    }

    /**
     * 指定したIDに該当する書籍を削除する。
     *
     * @param id 書籍のID
     * @throws BookNotFoundException 書籍が取得できない場合に発生する
     */
    @Override
    @Transactional
    public void deleteBook(long id) throws BookNotFoundException {
        if (bookRepository.existsById(id)) {
            bookRepository.deleteById(id);
        } else {
            throw new BookNotFoundException(id);
        }
    }

}
