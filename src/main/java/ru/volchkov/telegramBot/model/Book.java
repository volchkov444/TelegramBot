package ru.volchkov.telegramBot.model;

import lombok.Data;



@Data
public class Book {
    private  String name;
    private  String author;
    private  int yearOfRelease;
    private  boolean bookStatus;

    public boolean getBookStatus(){
        return bookStatus;
    }
}
