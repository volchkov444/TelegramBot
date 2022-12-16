package ru.volchkov.telegramBot.model;

import lombok.Data;

@Data
public class People {
    private long id;
    private String name;
    private int age;
    private PeopleStatus peopleStatus;
}
