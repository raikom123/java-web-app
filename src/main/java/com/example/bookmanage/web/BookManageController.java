package com.example.bookmanage.web;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.bookmanage.exception.BookManageValidationException;
import com.example.bookmanage.exception.BookNotFoundException;
import com.example.bookmanage.form.BookManageForm;
import com.example.bookmanage.service.BookManageService;

import lombok.extern.slf4j.Slf4j;

/**
 * 書籍管理システムのMVCコントローラ
 */
@Slf4j
@Controller
public class BookManageController {

    /**
     * ログイン画面のビュー名
     */
    private static final String LOGIN = "login";

    /**
     * 書籍管理システムのビュー名
     */
    private static final String BOOKS = "books";

    /**
     * リダイレクトのURL
     */
    private static final String REDIRECT_TO_BOOKS = "redirect:/" + BOOKS;

    /**
     * 書籍管理システムのサービス
     */
    private BookManageService service;

    /**
     * メッセージソース
     */
    private MessageSource messageSource;

    /**
     * コンストラクタ
     * 
     * @param service 書籍管理システムのサービス
     */
    @Autowired
    public BookManageController(BookManageService service, MessageSource messageSource) {
        this.service = service;
        this.messageSource = messageSource;
    }

    /**
     * ルートURLにアクセスする。
     * 
     * @return "/books"へのリダイレクト
     */
    @GetMapping(value = "/")
    public ModelAndView root() {
        return new ModelAndView(REDIRECT_TO_BOOKS);
    }

    // ------------------------------------------------------------------------
    // ログイン処理
    // ------------------------------------------------------------------------

    /**
     * ログイン画面にアクセスする。
     * 
     * @return モデルビュー
     */
    @GetMapping(value = "/login")
    public ModelAndView login(Principal principal) {
        //TODO セッションがあれば、booksにリダイレクト
        return new ModelAndView(LOGIN);
    }

    /**
     * ログインに失敗した時の処理。
     * 
     * @return モデルビュー
     */
    @GetMapping(value = "/loginfailure")
    public ModelAndView loginfailure() {
        ModelAndView modelAndView = new ModelAndView(LOGIN);
        modelAndView.addObject("loginFailure", true);
        return modelAndView;
    }

    // ------------------------------------------------------------------------
    // ログアウト処理
    // ------------------------------------------------------------------------

    /**
     * ログアウトが成功した時の処理。
     *
     * @return モデルビュー
     */
    @GetMapping(value = "/logoutsuccess")
    public ModelAndView logoutSuccess() {
        ModelAndView modelAndView = new ModelAndView(LOGIN);
        modelAndView.addObject("logout", true);
        return modelAndView;
    }

    // ------------------------------------------------------------------------
    // 書籍管理機能処理
    // ------------------------------------------------------------------------

    /**
     * 書籍一覧を読み込む。
     * 
     * @return モデルビュー
     */
    @GetMapping(value = "/books")
    public ModelAndView readBooks(Principal principal) {
        // 認証情報を取得
        Authentication authentication = (Authentication) principal;
        String userName = authentication.getName();

        BookManageForm form = service.initForm();
        ModelAndView modelAndView = toBookPages();
        modelAndView.addObject("bookManageForm", form);
        modelAndView.addObject("userName", userName);
        return modelAndView;
    }

    /**
     * ビュー名を設定したモデルビューを返却する。
     * 
     * @return return ビュー名を設定したモデルビュー
     */
    private ModelAndView toBookPages() {
        return new ModelAndView(BOOKS);
    }

    /**
     * 指定したIDに該当する書籍を読み込む。
     *
     * @param id 書籍のID
     * @return モデルビュー
     * @throws Throwable ビジネス例外以外の例外が発生した場合、throwされる
     */
    @GetMapping(value = "/books/{id}")
    public ModelAndView readOneBook(@PathVariable long id) throws Throwable {
        ModelAndView modelAndView = toBookPages();
        try {
            BookManageForm form = service.readOneBook(id);
            modelAndView.addObject("bookId", id);
            modelAndView.addObject("bookManageForm", form);
            return modelAndView;
        } catch (Throwable t) {
            return handleException(t);
        }
    }

    /**
     * フォーム情報から書籍を新規登録する。
     *
     * @param form フォーム情報
     * @param result Validatorの結果
     * @return モデルビュー
     * @throws Throwable ビジネス例外以外の例外が発生した場合、throwされる
     */
    @PostMapping(value = "/books")
    public ModelAndView createOneBook(@Validated @ModelAttribute BookManageForm form, BindingResult result)
            throws Throwable {
        try {
            validateInputFormData(form, result);

            service.createBook(form);
        } catch (Throwable t) {
            return handleException(form, t);
        }
        return new ModelAndView(REDIRECT_TO_BOOKS);
    }

    /**
     * 指定したIDの書籍をフォーム情報の内容に更新する。
     *
     * @param id 書籍のID
     * @param form フォーム情報
     * @param result Validatorの結果
     * @return モデルビュー
     * @throws Throwable ビジネス例外以外の例外が発生した場合、throwされる
     */
    @PutMapping(value = "/books/{id}")
    public ModelAndView updateOneBook(@PathVariable long id, @Validated @ModelAttribute BookManageForm form,
            BindingResult result) throws Throwable {
        try {
            validateInputFormData(form, result);

            service.updateBook(id, form);
        } catch (Exception e) {
            ModelAndView mav = handleException(form, e);
            mav.addObject("bookId", id);
            return mav;
        }
        return new ModelAndView(REDIRECT_TO_BOOKS);
    }

    /**
     * 指定したIDの書籍を削除する。
     *
     * @param id 書籍のID
     * @return モデルビュー
     * @throws Throwable ビジネス例外以外の例外が発生した場合、throwされる
     */
    @DeleteMapping(value = "/books/{id}")
    public ModelAndView deleteOneBook(@PathVariable long id) throws Throwable {
        try {
            service.deleteBook(id);
        } catch (Throwable t) {
            return handleException(t);
        }
        return new ModelAndView(REDIRECT_TO_BOOKS);
    }

    // ------------------------------------------------------------------------
    // 管理者用処理
    // ------------------------------------------------------------------------

    /**
     * 管理者用画面へのアクセスした時の処理。
     *
     * @param principal 認証情報
     * @return モデルビュー
     */
    @GetMapping("/admin")
    public ModelAndView admin(Principal principal) {
        ModelAndView modelAndView = readBooks(principal);
        modelAndView.setViewName("admin");
        return modelAndView;
    }

    // ------------------------------------------------------------------------
    // エラー処理
    // ------------------------------------------------------------------------

    /**
     * セッションが無効になった時の処理。
     *
     * @return モデルビュー
     */
    @GetMapping("/invalidsession")
    public ModelAndView invalidSession() {
        ModelAndView modelAndView = new ModelAndView(LOGIN);
        modelAndView.addObject("sessionInvalid", true);
        return modelAndView;
    }

    /**
     * フォーム情報の入力内容が妥当か否かを検証する。<br />
     * フォーム情報の入力内容に不備がある場合、Exceptionをthrowする。
     *
     * @param form フォーム情報
     * @param result validationの結果
     * @throws BookManageValidationException 入力内容にエラーがあった場合、発生する
     */
    private void validateInputFormData(BookManageForm form, BindingResult result) throws BookManageValidationException {
        if (result.hasErrors()) {
            throw new BookManageValidationException(result);
        }
    }

    /**
     * 例外を処理する。<br />
     * ビジネス例外の場合、エラーメッセージを設定したモデルビューを返却する。
     *
     * @param t 例外
     * @return モデルビュー
     * @throws Throwable ビジネス例外以外の例外が発生した場合、throwされる
     */
    private ModelAndView handleException(Throwable t) throws Throwable {
        BookManageForm form = new BookManageForm();
        form.setNewBook(true);
        return handleException(form, t);
    }

    /**
     * 例外を処理する。<br />
     * ビジネス例外の場合、エラーメッセージを設定したモデルビューを返却する。
     *
     * @param form フォーム情報
     * @param t 例外
     * @return モデルビュー
     * @throws Throwable ビジネス例外以外の例外が発生した場合、throwされる
     */
    private ModelAndView handleException(BookManageForm form, Throwable t) throws Throwable {
        if (t instanceof BookNotFoundException) {
            // 書籍が取得出来ない場合
            String message = messageSource.getMessage("error.booknotfound", null, null);
            log.warn(message, t);
            return toBookPageForError(form, message);
        } else if (t instanceof ObjectOptimisticLockingFailureException) {
            // 楽観排他でエラーが発生した場合
            String message = messageSource.getMessage("error.optlockfailure", null, null);
            log.warn(message, t);
            return toBookPageForError(form, message);
        } else if (t instanceof BookManageValidationException) {
            // 入力内容のエラーが発生した場合
            String message = messageSource.getMessage("error.validation", null, null);
            log.warn(message, t);
            return toBookPageForError(form, message);
        }

        throw t;
    }

    /**
     * エラーメッセージを設定したモデルビューを返却する。<br />
     * 書籍一覧の設定も行う。
     *
     * @param form フォーム情報
     * @param errorMessage エラーメッセージ
     * @return モデルビュー
     */
    private ModelAndView toBookPageForError(BookManageForm form, String errorMessage) {
        // 書籍一覧を取得し直す
        BookManageForm initForm = service.initForm();
        form.setBooks(initForm.getBooks());
        ModelAndView modelAndView = toBookPages();
        modelAndView.addObject("bookManageForm", form);
        modelAndView.addObject("errorMessage", errorMessage);
        return modelAndView;
    }

}
