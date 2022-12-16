package ru.volchkov.telegramBot.model;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class Book {
    private String Name;
    private String Author;
    private String YearOfRelease;
    private boolean BookStatus;
}
