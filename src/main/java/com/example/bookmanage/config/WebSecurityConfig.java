package com.example.bookmanage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 書籍管理システムのsecurityのconfiguration<br />
 * 
 * 以下を実装している。<br />
 * 認証が不要なURLと認証が必要なURLの設定。<br />
 * ログイン処理、ログアウト処理の設定。<br />
 * 認証できるユーザ情報の設定。
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
        (authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/", "/login", "/error", "/css/**", "/js/**", "img/**")
                                                        .permitAll()
                                                        // 認証済みでROLE_ADMIN権限を持っている場合のみ、アクセス可能
                                                        .requestMatchers("/admin")
                                                        .hasAuthority("ROLE_ADMIN")
                                                        // その他はアクセス権限が必要
                                                        .anyRequest()
                                                        .authenticated())
        .formLogin((formLogin) -> formLogin.loginPage("/login")
                                           // 認証処理
                                           .loginProcessingUrl("/authenticate")
                                           // ログイン成功
                                           .defaultSuccessUrl("/loginsuccess")
                                           // ログイン失敗
                                           .failureUrl("/loginfailure")
                                           // usernameのパラメータ名
                                           .usernameParameter("username")
                                           // passwordのパラメータ名
                                           .passwordParameter("password")
                                           .permitAll())
        .logout(logout -> logout
                                // ログアウト処理
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                // ログアウト成功時の遷移先
                                .logoutSuccessUrl("/logoutsuccess")
                                // ログアウト時に削除するクッキー名
                                .deleteCookies("JSESSIONID")
                                // ログアウト時のセッション破棄の有効化
                                .invalidateHttpSession(true)
                                .permitAll())
        .sessionManagement(sessionManagement -> sessionManagement
                                                                 // セッションが無効な時の遷移先
                                                                 .invalidSessionUrl("/invalidsession"));
    return http.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(PasswordEncoder passwordEncoder) {
    InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
    userDetailsService.createUser(User.withUsername("user")
                                      .password(passwordEncoder.encode("user"))
                                      .authorities("ROLE_USER")
                                      .build());
    userDetailsService.createUser(User.withUsername("admin")
                                      .password(passwordEncoder.encode("admin"))
                                      .authorities("ROLE_ADMIN")
                                      .build());
    var authenticationProvider = new DaoAuthenticationProvider();
    authenticationProvider.setUserDetailsService(userDetailsService);
    authenticationProvider.setPasswordEncoder(passwordEncoder);
    return new ProviderManager(authenticationProvider);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

}
