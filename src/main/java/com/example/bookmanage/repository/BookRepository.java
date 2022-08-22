package com.example.bookmanage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.bookmanage.domain.Book;

/**
 * 書籍のリポジトリ
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
