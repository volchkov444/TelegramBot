package ru.volchkov.telegramBot.builder;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.volchkov.telegramBot.buttons.KeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class KeyboardButtonBuilder implements Builder {

    final private InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
    final private List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
    final private List<InlineKeyboardButton> rowInline = new ArrayList<>();

    public KeyboardButtonBuilder() {
        super();
    }

    @Override
    public KeyboardButtonBuilder addButton(String setText, String setCallbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(setText);
        button.setCallbackData(setCallbackData);
        rowInline.add(button);
        return this;
    }

    @Override
    public KeyboardButtonBuilder addRowInline() {
        rowsInline.add(rowInline);
        return this;
    }

    @Override
    public KeyboardButtonBuilder setKeyboard() {
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return this;
    }

    @Override
    public KeyboardButton build() {
        return new KeyboardButton(inlineKeyboardMarkup, rowsInline, rowInline);
    }
}
