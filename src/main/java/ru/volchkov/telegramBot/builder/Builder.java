package ru.volchkov.telegramBot.builder;

import ru.volchkov.telegramBot.buttons.KeyboardButton;


public interface Builder {

    KeyboardButtonBuilder addButton(String setText, String setCallbackData);

    KeyboardButtonBuilder addRowInline();

    KeyboardButtonBuilder setKeyboard();

    KeyboardButton build();
}
