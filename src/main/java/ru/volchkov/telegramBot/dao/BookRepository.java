package ru.volchkov.telegramBot.dao;

import org.springframework.stereotype.Component;
import ru.volchkov.telegramBot.model.Book;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookRepository {

    List<Book> listOfBooks = new ArrayList<>();

    {
        listOfBooks.add(newBook("Атлант расправил плечи", "Айн Рэнд", 1957, true));
        listOfBooks.add(newBook("Человек который смеется", "Виктор Гюго", 1869, true));
        listOfBooks.add(newBook("Война и мир", "Лев Толстой", 1865, true));
        listOfBooks.add(newBook("Евгений Онегин", "Александр Пушкин", 1833, true));
        listOfBooks.add(newBook("Преступление и наказание", "Фёдор Достоевский", 1866, true));

    }

    public List<Book> index() {
        return listOfBooks;
    }

    public Book findBook(String name) {
        return listOfBooks.stream().filter(a -> a.getName().equals(name)).findAny().orElse(null);
    }

    public Book newBook(String name, String author, int yearOfRelease, boolean bookStatus) {
        Book book = new Book();
        book.setName(name);
        book.setBookStatus(bookStatus);
        book.setAuthor(author);
        book.setYearOfRelease(yearOfRelease);
        return book;
    }
}
