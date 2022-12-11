package ru.volchkov.telegramBot.service;


import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.volchkov.telegramBot.builder.ButtonsBuilder;
import ru.volchkov.telegramBot.builder.ButtonBuilder;
import ru.volchkov.telegramBot.model.Book;
import ru.volchkov.telegramBot.model.Person;
import ru.volchkov.telegramBot.model.PersonStatus;
import ru.volchkov.telegramBot.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PersonService {
    private final PersonRepository personRepository;
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";

    public SendMessage startCommand(Update update) {
        Person person = new Person();
        person.setId(update.getMessage().getChat().getId());
        person.setName("Guest");
        person.setAge(1);
        person.setPersonStatus(PersonStatus.GUEST);
        personRepository.save(person);
        String chatId = String.valueOf(update.getMessage().getChatId());
        String text = EmojiParser.parseToUnicode("Добро пожаловать, вы вошли как гость," + " пройдите регистрацию чтобы пользоваться нашим функционалом /register :smiling_face_with_hearts:");
        return createMessage(chatId, text);
    }

    public void setNewUserStatus(PersonStatus peopleStatus, Update update) {
        String name = update.getMessage().getChat().getFirstName();
        long id = update.getMessage().getChat().getId();
        int age = Integer.parseInt(update.getMessage().getText());
        Person person = personRepository.findPersonById(id).orElseThrow();
        person.setPersonStatus(peopleStatus);
        person.setAge(age);
        person.setName(name);
        personRepository.save(person);
    }

    public SendMessage register(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String text = "Регистрация пройдет автоматически.\n" + "Вы серьезно хотите зарегестрироваться?";
        ButtonsBuilder builder = new ButtonBuilder().addButton("Yes", YES_BUTTON).addButton("No", NO_BUTTON).addRowInline().setKeyboard();
        SendMessage message = createMessage(chatId, text);
        message.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
        return message;
    }

    public List<SendMessage> listOfUsers(String chatId, PersonRepository personRepository) {
        List<SendMessage> messages = new ArrayList<>();
        for (Person person : personRepository.findAll()) {
            String name = person.getName();
            long id = person.getId();
            int i = 0;
            SendMessage message = createMessage(chatId, (i + 1) + ".   ID:" + id + "     имя:" + name);
            ButtonsBuilder builder = new ButtonBuilder().addButton("Вся информация", name).addRowInline().setKeyboard();
            message.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
            messages.add(message);
        }
        return messages;
    }

    public SendMessage listOfUser(String chatId, String name) {
        long id = personRepository.findPersonByName(name).orElseThrow().getId();
        SendMessage message = createMessage(chatId, "   ID:" + id + "     имя:" + name);
        ButtonsBuilder builder = new ButtonBuilder().addButton("Вся информация", name).addRowInline().setKeyboard();
        message.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
        return message;
    }

    public List<SendMessage> userMenu(String chatId, PersonRepository personRepository) {
        List<SendMessage> messages = new ArrayList<>();
        ButtonsBuilder builder = new ButtonBuilder().addButton("Список всех пользователей", "listOfUsers").addRowInline().setKeyboard();
        SendMessage message = createMessage(chatId, "Колличество пользователей:" + personRepository.findAll().size() + "\n");
        message.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
        messages.add(message);
        messages.add(new SendMessage(chatId, EmojiParser.parseToUnicode(":arrow_down:Воспользутесь  поиском ,написав имя в чат:arrow_down:")));
        return messages;
    }

    public List<SendMessage> userInfo(long peopleId, String chatId) {
        List<SendMessage> messages = new ArrayList<>();
        Person person = personRepository.findPersonById(peopleId).orElseThrow();
        messages.add(new SendMessage(chatId, "Имя пользователя: " + person.getName() + "\n" + "Статус: " + person.getPersonStatus() + "\n" + "ID: " + person.getId() + "\n" + "Возраст: " + person.getAge()));
        if (person.getBooksOfPerson().isEmpty()) {
            messages.add(new SendMessage(chatId, "Пользователь не читает ни одной книги."));
        } else if (person.getBooksOfPerson().size() > 0) {
            for (Book book : person.getBooksOfPerson()) {
                String bookName = book.getName();
                String bookAuthor = book.getAuthor();
                int yearOfRelease = book.getYearOfRelease();
                ButtonsBuilder builder = new ButtonBuilder().addButton("Сдать книгу", book.getName()).addRowInline().setKeyboard();
                SendMessage message = createMessage(chatId, EmojiParser.parseToUnicode(":o:  " + bookName + ", " + bookAuthor + " " + yearOfRelease + "  :o:"));
                message.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
                messages.add(message);
            }
        }
        return messages;
    }

    public SendMessage setStatusUSER(Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        setNewUserStatus(PersonStatus.USER, update);
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
