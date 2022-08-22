package com.example.bookmanage.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import com.example.bookmanage.BookmanageApplication;
import com.example.bookmanage.form.BookManageForm;

@SpringBootTest(classes = BookmanageApplication.class)
class BookManageControllerIntegrationTests {

    private static final String TEST_TITLE = "タイトル";
    private static final String TEST_AUTHOR = "著者";

    @Autowired
    private WebApplicationContext context;

    /**
     * Httpリクエスト・レスポンスを扱うためのMockオブジェクト
     */
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() throws Exception {
        // MVCモックを生成
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .alwaysDo(log())
                .build();
    }

    @Test
    void login処理でログインに成功した場合の確認() throws Exception {
        // ログイン処理を行う
        mockMvc.perform(formLogin("/authenticate").user("user").password("user"))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/books")); // /booksにリダイレクトするか否か
    }

    @Test
    void login処理でログインに失敗した場合の確認() throws Exception {
        // ログイン処理を行う
        mockMvc.perform(formLogin("/authenticate").user("user").password("xxxxx"))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/loginfailure")); // /loginfailureにリダイレクトするか否か
    }

    @Test
    @WithMockUser(username = "user", password="user", authorities = "ROLE_USER")
    void 認証ありでgetリクエストでbooksにアクセスする場合のステータスとビューとモデルの確認() throws Exception {
        // getリクエストでbooksを指定する
        MvcResult result = this.mockMvc.perform(get("/books"))
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
    }

    @Test
    @WithMockUser(username = "user", password="user", authorities = "ROLE_USER")
    public void 認証ありでpostリクエストでbooksにアクセスする場合のステータスとリダイレクトURLの確認() throws Exception {
        // テストデータ作成
        BookManageForm inputForm = BookManageForm.builder()
                .title(TEST_TITLE)
                .author(TEST_AUTHOR)
                .newBook(true)
                .version(0)
                .build();

        // postリクエストでbooksを指定する
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("title", inputForm.getTitle());
        params.add("author", inputForm.getAuthor());
        params.add("newBook", String.valueOf(inputForm.isNewBook()));
        params.add("version", String.valueOf(inputForm.getVersion()));
        // csrfを設定しないとセッションが無効になる
        mockMvc.perform(post("/books").with(csrf()).params(params))
                .andDo(print())
                .andExpect(status().is3xxRedirection()) // HTTPステータスが3xxか否か(リダイレクト)
                .andExpect(redirectedUrl("/books")); // /booksにリダイレクトするか否か
    }

    @Test
    void 認証なしでbooksにアクセスしようとした場合の確認() throws Exception {
        // getリクエストでbooksにアクセス
        mockMvc.perform(get("/books"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @WithMockUser(username = "admin", password="admin", authorities = "ROLE_ADMIN")
    void 管理者権限があるユーザでadminにアクセスしようとした場合の確認() throws Exception {
        // getリクエストでadminを指定する
        MvcResult result = this.mockMvc.perform(get("/admin"))
                .andDo(print())
                .andExpect(status().isOk()) // HTTPステータスが200か否か
                .andExpect(view().name("admin")) // ビュー名が"admin"か否か
                .andReturn();

        // モデルからformを取得する
        BookManageForm form = (BookManageForm) result.getModelAndView().getModel().get("bookManageForm");

        // 変数を評価する
        assertNull(form.getTitle());
        assertNull(form.getAuthor());
        assertEquals(form.isNewBook(), true);
        assertEquals(form.getVersion(), 0);
        assertNotNull(form.getBooks());
    }

    @Test
    @WithMockUser(username = "user", password="user", authorities = "ROLE_USER")
    void 管理者権限がないユーザでadminにアクセスしようとした場合の確認() throws Exception {
        // getリクエストでadminにアクセス
        mockMvc.perform(get("/admin").with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andDo(print())
                .andExpect(status().isForbidden()); // クライアントエラー(403)
    }

    @Test
    void ログアウトした場合の確認() throws Exception {
        // getリクエストでlogoutにアクセス
        mockMvc.perform(get("/logout"))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/logoutsuccess"));
    }

}
