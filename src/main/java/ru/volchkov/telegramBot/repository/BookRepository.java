package ru.volchkov.telegramBot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volchkov.telegramBot.model.Book;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findBookByName(String name);

    Optional<Book> findBookByAuthor(String name);

    Optional<Book> findBookByYearOfRelease(int year);
}
