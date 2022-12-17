package ru.volchkov.telegramBot.service;

import org.springframework.stereotype.Component;

@Component
public class WordService {
    public String firstUpperCase(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
