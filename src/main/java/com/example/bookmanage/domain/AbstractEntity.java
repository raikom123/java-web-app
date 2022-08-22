package com.example.bookmanage.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Version;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.Getter;
import lombok.Setter;

/**
 * Entityクラスの共通クラス<br />
 * 
 * 共通項目の定義と更新処理を実装している。
 */
@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity {

    /**
     * 作成ユーザ
     */
    @Column(name = "created_user")
    private String createdUser;

    /**
     * 作成日時
     */
    @Column(name = "created_date_time")
    private LocalDateTime createdDateTime;

    /**
     * 更新ユーザ
     */
    @Column(name = "updated_user")
    private String updatedUser;

    /**
     * 更新日時
     */
    @Column(name = "updated_date_time")
    private LocalDateTime updatedDateTime;

    /**
     * バージョン
     */
    @Version
    private long version;

    /**
     * 新規登録時に呼び出される前処理
     */
    @PrePersist
    public void prePersist() {
        LocalDateTime datetime = LocalDateTime.now();
        createdDateTime = datetime;
        updatedDateTime = datetime;

        SecurityContext context = SecurityContextHolder.getContext();
        String userName = context.getAuthentication().getName();
        createdUser = userName;
        updatedUser = userName;
    }

    /**
     * 更新時に呼び出される前処理
     */
    @PreUpdate
    public void preUpdate() {
        updatedDateTime = LocalDateTime.now();

        SecurityContext context = SecurityContextHolder.getContext();
        updatedUser = context.getAuthentication().getName();
    }

}
