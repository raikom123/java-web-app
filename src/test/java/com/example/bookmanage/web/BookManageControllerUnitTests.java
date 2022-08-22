package com.example.bookmanage.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.example.bookmanage.BookmanageApplication;
import com.example.bookmanage.domain.Book;
import com.example.bookmanage.exception.BookNotFoundException;
import com.example.bookmanage.form.BookManageForm;
import com.example.bookmanage.service.BookManageService;

/**
 * BookManageControllerの単体テストプログラム
 */
@SpringBootTest(classes = BookmanageApplication.class)
public class BookManageControllerUnitTests {

    /**
     * テストデータのID
     */
    private static final long TEST_ID = 1;

    /**
     * 不正なテストデータのID
     */
    private static final long INVALID_TEST_ID = 2;

    /**
     * テストデータのタイトル
     */
    private static final String TEST_TITLE = "書籍のタイトル";

    /**
     * テストデータのタイトル(エラー)
     */
    private static final String TEST_TITLE_ERROR = "書籍のタイトル(エラー)";

    /**
     * テストデータの著者名
     */
    private static final String TEST_AUTHOR = "書籍の著者名";

    /**
     * テストデータの著者名(エラー)
     */
    private static final String TEST_AUTHOR_ERROR = "書籍の著者名(エラー)";

    /**
     * テストデータのバージョン
     */
    private static final long TEST_VERSION = 2;

    /**
     * テスト用のメッセージ
     */
    private static final String TEST_MESSAGE = "test message";

    /**
     * テストデータの書籍
     */
    private Book testBook;

    /**
     * 書籍管理システムのController
     */
    @InjectMocks
    private BookManageController controller;

    /**
     * 書籍管理システムのサービス
     */
    @Mock
    private BookManageService service;

    /**
     * メッセージソースのモック
     */
    @Mock
    private MessageSource mockMessageSource;

    /**
     * メッセージソースのコードを確認するためのCaptor
     */
    @Captor
    private ArgumentCaptor<String> messageCode;

    /**
     * Httpリクエスト・レスポンスを扱うためのMockオブジェクト
     */
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() throws Exception {
        // テストデータを生成
        testBook = Book.builder()
                .id(TEST_ID)
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .build();
        testBook.setVersion(TEST_VERSION);

        // [Circular view path]の例外が発生するため、ViewResolverを設定する
        String prefix = "/WEB-INF/pages/";
        String suffix = ".html";
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver(prefix, suffix);
        // MVCモックを生成
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new BookManageExceptionHandler())
                .setViewResolvers(viewResolver)
                .alwaysDo(log())
                .build();
    }

    /**
     * 登録データが0件の時にgetリクエストでbooksを指定し、
     * httpステータスとビュー名とモデルに設定されている変数で成否を判定
     * 
     * @throws Exception MockMvcのメソッド呼び出し時に発生する
     */
    @Test
    public void readBooks_データが登録されていない時のステータスとビューとモデルの確認() throws Exception {
        // モックを登録
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList())
                .build();
        when(service.initForm()).thenReturn(initForm);
        // 認証情報のモック
        Authentication mockPrincipal = mock(Authentication.class);
        when(mockPrincipal.getName()).thenReturn("user");

        // getリクエストでbooksを指定する
        MvcResult result = this.mockMvc.perform(get("/books").principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 0);
    }

    /**
     * 登録データが1件の時にgetリクエストでbooksを指定し、
     * httpステータスとビュー名とモデルに設定されている変数で成否を判定
     * 
     * @throws Exception MockMvcのメソッド呼び出し時に発生する
     */
    @Test
    public void readBooks_データが1件登録されている時のステータスとビューとモデルの確認() throws Exception {
        // モックを登録
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();
        when(service.initForm()).thenReturn(initForm);
        // 認証情報のモック
        Authentication mockPrincipal = mock(Authentication.class);
        when(mockPrincipal.getName()).thenReturn("user");

        // getリクエストでbooksを指定する
        MvcResult result = this.mockMvc.perform(get("/books").principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);
    }

    /**
     * getリクエストでbooks/{id}を指定し、存在しないidを指定した時のhttpステータスとビュー名とモデルに設定されている変数で成否を判定
     * 
     * @throws Exception MockMvcのメソッド呼び出し時に発生する
     */
    @Test
    public void readOneBook_データが存在するidを指定した時のステータスとビューとモデルの確認() throws Exception {
        // モックを登録
        BookManageForm readOneForm = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .newBook(false)
                .version(TEST_VERSION)
                .books(Arrays.asList(testBook))
                .build();
        when(service.readOneBook(TEST_ID)).thenReturn(readOneForm);

        // getリクエストでbooks/{id}を指定する
        MvcResult result = mockMvc.perform(get("/books/1")).andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertEquals(form.getTitle(), TEST_TITLE);
        assertEquals(form.getAuthor(), TEST_AUTHOR);
        assertEquals(form.isNewBook(), false);
        assertEquals(form.getVersion(), TEST_VERSION);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // モデルからメッセージを取得し、リソースファイルのメッセージと同じか評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertNull(message);
    }

    /**
     * getリクエストでbooks/{id}を指定し、存在しないidを指定した時のhttpステータスとビュー名とモデルに設定されている変数で成否を判定
     * 
     * @throws Exception MockMvcのメソッド呼び出し時に発生する
     */
    @Test
    public void readOneBook_データが存在しないidを指定した時のステータスとビューとモデルの確認() throws Exception {
        // モックを登録
        when(service.readOneBook(INVALID_TEST_ID)).thenThrow(new BookNotFoundException(INVALID_TEST_ID));
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();
        when(service.initForm()).thenReturn(initForm);
        when(mockMessageSource.getMessage(any(), any(), any())).thenReturn(TEST_MESSAGE);

        // getリクエストでbooks/{id}を指定する
        MvcResult result = mockMvc.perform(get("/books/2")).andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // モデルにメッセージが設定されているかを評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertEquals(message, TEST_MESSAGE);
        // メッセージソースの引数を確認する
        verify(mockMessageSource).getMessage(messageCode.capture(), any(), any());
        assertEquals(messageCode.getValue(), "error.booknotfound");
    }

    @Test
    public void createOneBook_正常に新規登録した場合のステータスとリダイレクトURLの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .newBook(true)
                .version(0)
                .build();

        // モックを登録
        when(service.createBook(inputForm)).thenReturn(testBook);

        // postリクエストでbooksを指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        mockMvc.perform(post("/books").params(params))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/books")); // /booksにリダイレクトするか否か
    }

    @Test
    public void createOneBook_入力エラーが発生した場合のステータスとビューとモデルの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .newBook(true)
                .version(0)
                .build();
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .version(0)
                .books(Arrays.asList())
                .build();

        // モックを登録
        when(service.initForm()).thenReturn(initForm);
        when(mockMessageSource.getMessage("error.validation", null, null)).thenReturn(TEST_MESSAGE);

        // postリクエストでbooksを指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        MvcResult result = mockMvc.perform(post("/books").params(params))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 0);

        // モデルにメッセージが設定されているかを評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertEquals(message, TEST_MESSAGE);
        // メッセージソースの引数を確認する
        verify(mockMessageSource).getMessage(messageCode.capture(), any(), any());
        assertEquals(messageCode.getValue(), "error.validation");

        //MEMO bindingResultの詳細な確認は、BookManageFormTestsで行う
        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.bookManageForm");
        assertEquals(bindingResult.getErrorCount(), 2);
    }

    @Test
    public void updateOneBook_正常に更新した場合のステータスとリダイレクトURLの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .newBook(false)
                .version(0)
                .build();

        // モックを登録
        when(service.updateBook(TEST_ID, inputForm)).thenReturn(testBook);

        // putリクエストでbooks/{id}を指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        mockMvc.perform(put("/books/1").params(params))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/books")); // /booksにリダイレクトするか否か
    }

    @Test
    public void updateOneBook_指定したIDのデータが存在しない場合のステータスとビューとモデルの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .title(TEST_TITLE_ERROR)
                .author(TEST_AUTHOR_ERROR)
                .newBook(false)
                .version(TEST_VERSION)
                .build();
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();

        // モックを登録
        when(service.updateBook(INVALID_TEST_ID, inputForm)).thenThrow(new BookNotFoundException(INVALID_TEST_ID));
        when(service.initForm()).thenReturn(initForm);
        when(mockMessageSource.getMessage("error.booknotfound", null, null)).thenReturn(TEST_MESSAGE);

        // putリクエストでbooks/{id}を指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        MvcResult result = mockMvc.perform(put("/books/2").params(params))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertEquals(form.getTitle(), inputForm.getTitle());
        assertEquals(form.getAuthor(), inputForm.getAuthor());
        assertEquals(form.isNewBook(), inputForm.isNewBook());
        assertEquals(form.getVersion(), inputForm.getVersion());
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // モデルにメッセージが設定されているかを評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertEquals(message, TEST_MESSAGE);
        // メッセージソースの引数を確認する
        verify(mockMessageSource).getMessage(messageCode.capture(), any(), any());
        assertEquals(messageCode.getValue(), "error.booknotfound");
    }

    @Test
    public void updateOneBook_バージョンが更新されている場合のステータスとビューとモデルの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .title(TEST_TITLE_ERROR)
                .author(TEST_AUTHOR_ERROR)
                .newBook(false)
                .version(TEST_VERSION)
                .build();
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();

        // モックを登録
        when(service.updateBook(INVALID_TEST_ID, inputForm)).thenThrow(new ObjectOptimisticLockingFailureException(Book.class, INVALID_TEST_ID));
        when(service.initForm()).thenReturn(initForm);
        when(mockMessageSource.getMessage(any(), any(), any())).thenReturn(TEST_MESSAGE);

        // putリクエストでbooks/{id}を指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        MvcResult result = mockMvc.perform(put("/books/2").params(params))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertEquals(form.getTitle(), inputForm.getTitle());
        assertEquals(form.getAuthor(), inputForm.getAuthor());
        assertEquals(form.isNewBook(), inputForm.isNewBook());
        assertEquals(form.getVersion(), inputForm.getVersion());
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // モデルにメッセージが設定されているかを評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertEquals(message, TEST_MESSAGE);
        // メッセージソースの引数を確認する
        verify(mockMessageSource).getMessage(messageCode.capture(), any(), any());
        assertEquals(messageCode.getValue(), "error.optlockfailure");
    }

    @Test
    public void updateOneBook_入力エラーが発生する場合のステータスとビューとモデルの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .title("")
                .author("")
                .newBook(false)
                .version(TEST_VERSION)
                .build();
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();

        // モックを登録
        when(service.initForm()).thenReturn(initForm);
        when(mockMessageSource.getMessage(any(), any(), any())).thenReturn(TEST_MESSAGE);

        // putリクエストでbooks/{id}を指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        MvcResult result = mockMvc.perform(put("/books/1").params(params))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertEquals(form.getTitle(), inputForm.getTitle());
        assertEquals(form.getAuthor(), inputForm.getAuthor());
        assertEquals(form.isNewBook(), inputForm.isNewBook());
        assertEquals(form.getVersion(), inputForm.getVersion());
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // モデルにメッセージが設定されているかを評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertEquals(message, TEST_MESSAGE);
        // メッセージソースの引数を確認する
        verify(mockMessageSource).getMessage(messageCode.capture(), any(), any());
        assertEquals(messageCode.getValue(), "error.validation");

        //MEMO bindingResultの詳細な確認は、BookManageFormTestsで行う
        BindingResult bindingResult = (BindingResult) result.getModelAndView().getModel().get("org.springframework.validation.BindingResult.bookManageForm");
        assertEquals(bindingResult.getErrorCount(), 2);
    }

    @Test
    public void deleteOneBook_正常に削除した場合のステータスとリダイレクトURLの確認() throws Exception {
        // モックを登録
        doNothing().when(service).deleteBook(TEST_ID);

        // deleteリクエストでbooksを指定する
        mockMvc.perform(delete("/books/1"))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/books")); // /booksにリダイレクトするか否か
    }

    @Test
    public void deleteOneBook_指定したIDのデータが存在しない場合のステータスとビューとモデルの確認() throws Exception {
        // モックを登録
        doThrow(new BookNotFoundException(INVALID_TEST_ID)).when(service).deleteBook(INVALID_TEST_ID);
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();
        when(service.initForm()).thenReturn(initForm);
        when(mockMessageSource.getMessage("error.booknotfound", null, null)).thenReturn(TEST_MESSAGE);

        // deleteリクエストでbooks/{id}を指定する
        MvcResult result = mockMvc.perform(delete("/books/2")).andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("books")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);

        // モデルにメッセージが設定されているかを評価する
        String message = (String) result.getModelAndView().getModel().get("errorMessage");
        assertEquals(message, TEST_MESSAGE);
        // メッセージソースの引数を確認する
        verify(mockMessageSource).getMessage(messageCode.capture(), any(), any());
        assertEquals(messageCode.getValue(), "error.booknotfound");
    }

    @Test
    void ルートURLを指定した場合のステータスとビュー名の確認() throws Exception {
        // getリクエストで"/"を指定する
        mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/books")); // /booksにリダイレクトするか否か
    }

    @Test
    void idに文字を指定した場合のステータスとビュー名の確認() throws Exception {
        // putリクエストでbooks/{id}を指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", TEST_TITLE);
        params.add("author", TEST_AUTHOR);
        params.add("newBook", String.valueOf(false));
        params.add("version", String.valueOf(0));
        mockMvc.perform(put("/books/a").params(params))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("error")); // ビュー名がerrorか否か
    }

    @Test
    void login_ログイン画面にアクセスした場合のステータスとビュー名とモデルの確認() throws Exception {
        mockMvc.perform(get("/login"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("loginFailure", "logout", "sessionInvalid"));
    }

    @Test
    void loginfailure_ログイン失敗時のステータスとビュー名とモデルの確認() throws Exception {
        mockMvc.perform(get("/loginfailure"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attribute("loginFailure", true))
                .andExpect(model().attributeDoesNotExist("logout", "sessionInvalid"));
    }

    @Test
    void invalidsession_セッションが無効になった場合のステータスとビュー名とモデルの確認() throws Exception {
        mockMvc.perform(get("/invalidsession"))
                 .andDo(print())
                 .andExpect(status().isOk())
                 .andExpect(view().name("login"))
                 .andExpect(model().attribute("sessionInvalid", true))
                 .andExpect(model().attributeDoesNotExist("logout", "loginFailure"));
    }

    @Test
    void logoutsuccess_ログアウトに成功した場合のステータスとビュー名とモデルの確認() throws Exception {
        mockMvc.perform(get("/logoutsuccess"))
                 .andDo(print())
                 .andExpect(status().isOk())
                 .andExpect(view().name("login"))
                 .andExpect(model().attribute("logout", true))
                 .andExpect(model().attributeDoesNotExist("sessionInvalid", "loginFailure"));
    }

    @Test
    void admin_管理者機能にアクセスした場合のステータスとビュー名とモデルの確認() throws Exception {
        // モックを登録
        BookManageForm initForm = BookManageForm.builder()
                .newBook(true)
                .books(Arrays.asList(testBook))
                .build();
        when(service.initForm()).thenReturn(initForm);
        // 認証情報のモック
        Authentication mockPrincipal = mock(Authentication.class);
        when(mockPrincipal.getName()).thenReturn("user");

        // getリクエストでbooksを指定する
        MvcResult result = this.mockMvc.perform(get("/admin").principal(mockPrincipal))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("admin")) // ビュー名が"books"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
        assertEquals(form.getBooks().size(), 1);
    }

}
