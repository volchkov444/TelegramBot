package ru.volchkov.telegramBot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class People {
    private long id;
    private String name;
    private int age;
    private PeopleStatus peopleStatus;
    private List<Book> booksOfPeople = new ArrayList<>();

    public Book findBook(String name) {
        return booksOfPeople.stream().filter(a -> a.getName().equals(name)).findAny().orElse(null);
    }
}
