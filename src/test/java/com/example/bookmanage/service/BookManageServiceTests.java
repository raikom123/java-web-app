package com.example.bookmanage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.example.bookmanage.BookmanageApplication;
import com.example.bookmanage.domain.Book;
import com.example.bookmanage.exception.BookNotFoundException;
import com.example.bookmanage.form.BookManageForm;
import com.example.bookmanage.repository.BookRepository;

/**
 * BookManageServiceのテストプログラム
 */
@SpringBootTest(classes = {BookmanageApplication.class})
class BookManageServiceTests {

    /**
     * テストデータのID
     */
    private static final long TEST_ID = 1;

    /**
     * テストデータのタイトル
     */
    private static final String TEST_TITLE = "testタイトル";

    /**
     * テストデータの著者名
     */
    private static final String TEST_AUTHOR = "test著者名";

    /**
     * テストデータのバージョン
     */
    private static final long TEST_VERSION = 2;

    /**
     * 不正なテストデータのバージョン
     */
    private static final long INVALID_TEST_VERSION = 3;

    /**
     * 書籍管理システムのサービス
     */
    @InjectMocks
    private BookManageService service;

    /**
     * 書籍管理システムのリポジトリ
     */
    @Mock
    private BookRepository repository;

    /**
     * テストデータの書籍
     */
    private Book testBook;

    @BeforeEach
    void setup() {
        // テストデータの生成
        testBook = Book.builder()
                .id(TEST_ID)
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .build();
        testBook.setVersion(TEST_VERSION);
    }

    @Test
    void initForm_戻り値の変数とメソッドの呼び出しの確認() {
        // モック
        when(repository.findAll()).thenReturn(Arrays.asList(testBook));

        // initFormの呼び出し
        BookManageForm form = service.initForm();

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // booksにrepository.findAllの結果が設定されているか評価する
        Book book = form.getBooks().get(0);
        assertEquals(book.getTitle(), TEST_TITLE);
        assertEquals(book.getAuthor(), TEST_AUTHOR);
        assertEquals(book.getId(), TEST_ID);
        assertEquals(book.getVersion(), TEST_VERSION);

        // repositoryのメソッドの呼び出しを確認
        verify(repository, times(1)).findAll();
    }

    @Test
    void readOneBook_戻り値とメソッドの呼び出しの確認() {
        // モック
        when(repository.findById(TEST_ID)).thenReturn(Optional.of(testBook));
        when(repository.findAll()).thenReturn(Arrays.asList(testBook));

        try {
            // readOneBookを呼び出す
            BookManageForm form = service.readOneBook(TEST_ID);

            // 変数を評価する
            assertEquals(form.getTitle(), TEST_TITLE);
            assertEquals(form.getAuthor(), TEST_AUTHOR);
            assertEquals(form.isNewBook(), false);
            assertEquals(form.getVersion(), TEST_VERSION);
            assertNotNull(form.getBooks());
            assertEquals(form.getBooks().size(), 1);

            // repositoryのメソッドの呼び出しを確認
            verify(repository, times(1)).findAll();
            verify(repository, times(1)).findById(TEST_ID);
        } catch (BookNotFoundException e) {
            // Exceptionが発生したらエラー
            fail();
        }
    }

    @Test
    void readOneBook_指定したIDのデータが取得できない場合_例外が発生することの確認() {
        // モック
        when(repository.findById(TEST_ID)).thenReturn(Optional.ofNullable(null));
        when(repository.findAll()).thenReturn(Arrays.asList());

        try {
            // readOneBookを呼び出す
            service.readOneBook(TEST_ID);

            // Exceptionが発生しない場合、エラー
            fail();
        } catch (BookNotFoundException e) { }
    }

    @Test
    void updateBook_戻り値と保存処理の呼び出しの確認() {
        // モック
        when(repository.findById(TEST_ID)).thenReturn(Optional.of(testBook));
        when(repository.save(testBook)).thenReturn(testBook);

        // updateBookを呼び出す
        BookManageForm form = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .version(TEST_VERSION)
                .build();

        try {
            // updateBookを呼び出す
            Book book = service.updateBook(TEST_ID, form);

            // 戻り値と同じ値か否かを評価
            assertThat(book).isEqualTo(testBook);

            // saveが呼び出されることを確認
            verify(repository, times(1)).save(testBook);
        } catch (BookNotFoundException e) {
            // Exceptionが発生したら、エラー
            fail();
        }
    }

    @Test
    void updateBook_DBのバージョンと異なるバージョンを指定した場合_例外が発生することの確認() {
        // モック
        when(repository.findById(TEST_ID)).thenReturn(Optional.of(testBook));

        // updateBookを呼び出す
        BookManageForm form = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .version(INVALID_TEST_VERSION)
                .build();

        try {
            // updateBookを呼び出す
            service.updateBook(TEST_ID, form);

            // Exceptionが発生しない場合、エラー
            fail();
        } catch (BookNotFoundException e) {
            // データが存在しない場合、エラー
            fail();
        } catch (ObjectOptimisticLockingFailureException e) {
            // 楽観排他の場合、正常
        }
    }

    @Test
    void updateBook_指定したIDでデータが取得できない場合_例外が発生することの確認() {
        // モック
        when(repository.findById(TEST_ID)).thenReturn(Optional.ofNullable(null));

        // updateBookを呼び出す
        BookManageForm form = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .version(TEST_VERSION)
                .build();

        try {
            // updateBookを呼び出す
            service.updateBook(TEST_ID, form);

            // Exceptionが発生しない場合、エラー
            fail();
        } catch (BookNotFoundException e) { }
    }

    @Test
    void createBook_戻り値と保存処理の呼び出しを確認() {
        // 引数を作成
        BookManageForm form = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .build();
        Book inputBook = new ModelMapper().map(form, Book.class);

        // モック
        when(repository.save(inputBook)).thenReturn(testBook);

        // createBookを呼び出す
        Book book = service.createBook(form);

        // 戻り値と同じ値か否かを評価
        assertThat(book).isEqualTo(testBook);

        // saveが呼び出されることを確認
        verify(repository, times(1)).save(inputBook);
    }

    @Test
    void deleteBook_削除処理の呼び出しの確認() {
        // モック
        when(repository.existsById(TEST_ID)).thenReturn(true);

        try {
            // deleteBookを呼び出す
            service.deleteBook(TEST_ID);

            // deleteByIdが呼び出されることを確認
            verify(repository, times(1)).deleteById(TEST_ID);
        } catch (BookNotFoundException e) {
            // Exceptionが発生したら、エラー
            fail();
        }
    }

    @Test
    void deleteBook_指定したIDのデータが存在しない場合_例外が発生することの確認() {
        // モック
        when(repository.existsById(TEST_ID)).thenReturn(false);

        try {
            // deleteBookを呼び出す
            service.deleteBook(TEST_ID);

            // Exceptionが発生しないとエラー
            fail();
        } catch (BookNotFoundException e) { }
    }

}
