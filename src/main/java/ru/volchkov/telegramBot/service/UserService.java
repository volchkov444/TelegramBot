package ru.volchkov.telegramBot.service;


import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.volchkov.telegramBot.builder.Builder;
import ru.volchkov.telegramBot.builder.KeyboardButtonBuilder;
import ru.volchkov.telegramBot.dao.PeopleRepository;
import ru.volchkov.telegramBot.model.PeopleStatus;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";

    public SendMessage startCommand(PeopleRepository peopleDAO, Update update) {
        peopleDAO.addPeople(update.getMessage().getChat().getId(), "Guest", 1, PeopleStatus.GUEST);
        String chatId = String.valueOf(update.getMessage().getChatId());
        String text = EmojiParser.parseToUnicode("Добро пожаловать, вы вошли как гость," + " пройдите регистрацию чтобы пользоваться нашим функционалом /register :smiling_face_with_hearts:");
        return createMessage(chatId, text);
    }

    public void setNewUserStatus(PeopleStatus peopleStatus, Update update, PeopleRepository personDAO) {
        String name = update.getMessage().getChat().getFirstName();
        long id = update.getMessage().getChat().getId();
        int age = Integer.parseInt(update.getMessage().getText());
        personDAO.findPeople(update.getMessage().getChat().getId()).setPeopleStatus(peopleStatus);
        personDAO.findPeople(id).setAge(age);
        personDAO.findPeople(id).setName(name);
    }

    public SendMessage register(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String text = "Регистрация пройдет автоматически.\n" + "Вы серьезно хотите зарегестрироваться?";
        Builder builder = new KeyboardButtonBuilder().addButton("Yes", YES_BUTTON).addButton("No,", NO_BUTTON).addRowInline().setKeyboard();
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
        return message;
    }

    public List<SendMessage> listOfUsers(String chatId, PeopleRepository peopleDAO) {
        List<SendMessage> messages = new ArrayList<>();
        messages.add(new SendMessage(chatId, "Колличество пользователей:" + peopleDAO.index().size() + "\n"));
        for (int i = 0; i < peopleDAO.index().size(); i++) {
            String name = peopleDAO.index().get(i).getName();
            long id = peopleDAO.index().get(i).getId();
            int age = peopleDAO.index().get(i).getAge();
            messages.add(new SendMessage(chatId, (i + 1) + ". ID:" + id + ", имя:" + name + ", возраст:" + age));
        }
        return messages;
    }

    public SendMessage setStatusUSER(Update update, PeopleRepository peopleDAO) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        setNewUserStatus(PeopleStatus.USER, update, peopleDAO);
        String text = EmojiParser.parseToUnicode(update.getMessage().getChat().getFirstName() + " ,поздравляем,вы успешно зарегистрировались.:sparkles:\n /start Чтобы начать работу.");
        return createMessage(chatId, text);
    }

    public SendMessage createMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }
}
