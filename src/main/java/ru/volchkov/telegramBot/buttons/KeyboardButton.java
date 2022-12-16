package ru.volchkov.telegramBot.buttons;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardButton {
    private InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    private List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
    private List<InlineKeyboardButton> rowInline = new ArrayList<>();


    public KeyboardButton(InlineKeyboardMarkup inlineKeyboardMarkup, List<List<InlineKeyboardButton>> rowsInline, List<InlineKeyboardButton> rowInline) {
        this.inlineKeyboardMarkup = inlineKeyboardMarkup;
        this.rowsInline = rowsInline;
        this.rowInline = rowInline;
    }

    public InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return inlineKeyboardMarkup;
    }
}
