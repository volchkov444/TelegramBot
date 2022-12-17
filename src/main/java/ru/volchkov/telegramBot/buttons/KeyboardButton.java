package ru.volchkov.telegramBot.buttons;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class KeyboardButton {
    private final InlineKeyboardMarkup inlineKeyboardMarkup;


    public KeyboardButton(InlineKeyboardMarkup inlineKeyboardMarkup, List<List<InlineKeyboardButton>> rowsInline, List<InlineKeyboardButton> rowInline) {
        this.inlineKeyboardMarkup = inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return inlineKeyboardMarkup;
    }
}
