package ru.volchkov.telegramBot.builder;

import ru.volchkov.telegramBot.buttons.KeyboardButton;


public interface ButtonsBuilder {

    ButtonBuilder addButton(String setText, String setCallbackData);

    ButtonBuilder addRowInline();

    ButtonBuilder setKeyboard();

    KeyboardButton build();
}
