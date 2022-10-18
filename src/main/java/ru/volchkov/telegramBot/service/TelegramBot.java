package ru.volchkov.telegramBot.service;


import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.volchkov.telegramBot.config.BotConfig;
import ru.volchkov.telegramBot.dao.BookDAO;
import ru.volchkov.telegramBot.dao.PersonDAO;
import ru.volchkov.telegramBot.model.People;
import ru.volchkov.telegramBot.model.PeopleStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    final BotConfig config;
    final BookDAO bookDAO;
    final PersonDAO personDAO;
    String regex = "\\d+";
    static final String HELP_TEXT = "1. Добавление, изменение и удаление книг\n" + "2. Страница со списком всех людей\n" + "3. Страница со списком всех книг\n" + "4. Страница человека, на которой показаны значения его полей и список книг которые он взял.\n" + "6. Возможность освободить книгу";
    static final String INFO_TEXT = EmojiParser.parseToUnicode("Бот взят за основу разработки API приложения, чтобы реализовать" + " бизнес-логику проекта без html кода.\n                                                     " + ":heavy_check_mark:Задача:heavy_check_mark:\n" + "В местной библиотеке хотят перейти на цифровой учет книг. Нам " + "было необходимо реализовать приложение для них. Библиотекари " + "должны иметь возможность регистрировать читателей, выдавать им " + "книги и освобождать книги (после того, как читатель возвращает " + "книгу обратно в библиотеку).");
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";


    public TelegramBot(BotConfig config, BookDAO bookDAO, PersonDAO personDAO) {
        this.bookDAO = bookDAO;
        this.personDAO = personDAO;
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "получить начальную информацию"));
        listOfCommands.add(new BotCommand("/register", "регистрация нового пользователя"));
        listOfCommands.add(new BotCommand("/info", "описание проекта"));
        listOfCommands.add(new BotCommand("/help", "описание функционала"));
        listOfCommands.add(new BotCommand("/listOfUsers", "вывести всех зарегестрированных людей"));
        listOfCommands.add(new BotCommand("/addBook", "добавить книгу в библиотеку"));
        listOfCommands.add(new BotCommand("/takeBook", "посмотреть список книг, взять книгу"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot's command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && personDAO.collect(update.getMessage().getChat().getId()) == null) {
            People people = new People();
            people.setId(update.getMessage().getChat().getId());
            people.setName("Guest");
            people.setAge(1);
            people.setPeopleStatus(PeopleStatus.GUEST);
            personDAO.addPeople(people);
            prepareAndSendMessage(update.getMessage().getChatId(), EmojiParser.parseToUnicode("Добро пожаловать, вы вошли как гость," +
                    " пройдите регистрацию чтобы пользоваться нашим функционалом /register :smiling_face_with_hearts:"));
        }
        if (update.hasMessage() && update.getMessage().getText().equals("/register") &&
                personDAO.collect(update.getMessage().getChat().getId()).getPeopleStatus().equals(PeopleStatus.GUEST)) {
            long chatId = update.getMessage().getChatId();
            register(chatId);
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long peopleId = update.getCallbackQuery().getMessage().getChat().getId();
            if (bookDAO.findBook(callbackData) != null && personDAO.collect(update.getCallbackQuery().getMessage().getChat().getId()).getPeopleStatus().equals(PeopleStatus.USER) &&
                    bookDAO.findBook(callbackData).getBookStatus()) {
                bookDAO.findBook(callbackData).setBookStatus(false);
                personDAO.collect(peopleId).getBooksOfPeople().add(bookDAO.findBook(callbackData));
                String text = EmojiParser.parseToUnicode("Вы взяли книгу-" + bookDAO.findBook(callbackData).getName() + ",\n" +
                        "отличный выбор:open_book:");
                executeEditMessageText(text, chatId, messageId);
            } else if (bookDAO.findBook(callbackData) != null && !bookDAO.findBook(callbackData).getBookStatus() &&
                    personDAO.collect(update.getCallbackQuery().getMessage().getChat().getId()).getPeopleStatus().equals(PeopleStatus.USER) &&
                    update.getCallbackQuery().getMessage().getChat().getId().equals(personDAO.findPeople(callbackData))) {

                personDAO.collect(personDAO.findPeople(callbackData)).getBooksOfPeople().remove(bookDAO.findBook(callbackData));
                bookDAO.findBook(callbackData).setBookStatus(true);
                String text = update.getCallbackQuery().getMessage().getChat().getFirstName() + ", Вы сдали книгу обратно в библиотеку";
                executeEditMessageText(text, chatId, messageId);
            }


            if (callbackData.equals(YES_BUTTON)) {
                executeEditMessageText("Сколько вам лет?", chatId, messageId);

            }
            if (callbackData.equals(NO_BUTTON)) {
                String text = EmojiParser.parseToUnicode("Вы нажали на кнопку NO:cry:");
                executeEditMessageText(text, chatId, messageId);
            }
        }
        if (update.hasMessage() && update.getMessage().getText().matches(regex) &&
                Integer.parseInt(update.getMessage().getText()) <= 100
                && Integer.parseInt(update.getMessage().getText()) > 0 &&
                personDAO.collect(update.getMessage().getChat().getId()).getPeopleStatus().equals(PeopleStatus.GUEST)) {
            String name = update.getMessage().getChat().getFirstName();
            long chatId = update.getMessage().getChatId();
            long id = update.getMessage().getChat().getId();
            int age = Integer.parseInt(update.getMessage().getText());
            personDAO.collect(id).setAge(age);
            personDAO.collect(id).setName(name);
            personDAO.collect(id).setPeopleStatus(PeopleStatus.USER);

            String text = EmojiParser.parseToUnicode(name + " ,поздравляем,вы успешно зарегистрировались.:sparkles:\n /start Чтобы начать работу.");
            prepareAndSendMessage(chatId, text);
        }
        if (update.hasMessage() && personDAO.collect(update.getMessage().getChat().getId()).getPeopleStatus().equals(PeopleStatus.USER) &&
                !update.getMessage().getText().matches(regex)) {
            String message = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (message) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/info":
                    prepareAndSendMessage(chatId, INFO_TEXT);
                    break;
                case "/help":
                    prepareAndSendMessage(chatId, HELP_TEXT);
                    break;
                case "/listOfUsers":
                    listOfUsers(chatId);
                    break;
                case "/takeBook":
                    takeBook(chatId, personDAO);
                    break;
                default:
                    prepareAndSendMessage(chatId, EmojiParser.parseToUnicode(update.getMessage().getChat().getFirstName()
                            + ",Вы ввели неверную команду,попробуйте еще раз :unamused:"));
            }
        }
    }

    private void takeBook(long chatId, PersonDAO personDAO) {
        prepareAndSendMessage(chatId, EmojiParser.parseToUnicode("Всего книг в библиотеке: " + bookDAO.index().size() + "\n" +
                ":white_check_mark: - свободна \n" +
                ":o: - читают"));
        for (int i = 0; i < bookDAO.index().size(); i++) {
            String bookName = bookDAO.index().get(i).getName();
            String bookAuthor = bookDAO.index().get(i).getAuthor();
            int yearOfRelease = bookDAO.index().get(i).getYearOfRelease();
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            if (bookDAO.index().get(i).getBookStatus()) {
                message.setText(EmojiParser.parseToUnicode(":white_check_mark:  " + bookName + ", " + bookAuthor + " " + yearOfRelease + "  :white_check_mark:"));
            } else {
                message.setText(EmojiParser.parseToUnicode(":o:  " + bookName + ", " + bookAuthor + " " + yearOfRelease + "  :o: \nКнигу взял(а)-" + personDAO.collect(personDAO.findPeople(bookName)).getName()));
            }
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            var takeButton = new InlineKeyboardButton();
            if (bookDAO.index().get(i).getBookStatus()) {
                takeButton.setText("Взять книгу");
            } else {
                takeButton.setText("Сдать книгу");
            }
            takeButton.setCallbackData(bookName);
            rowInline.add(takeButton);
            rowsInline.add(rowInline);
            inlineKeyboardMarkup.setKeyboard(rowsInline);
            message.setReplyMarkup(inlineKeyboardMarkup);
            executeMessage(message);

        }

    }

    private void listOfUsers(long chatId) {
        prepareAndSendMessage(chatId, "Колличество пользователей:" + personDAO.index().size() + "\n");
        for (int i = 0; i < personDAO.index().size(); i++) {
            String name = personDAO.index().get(i).getName();
            long id = personDAO.index().get(i).getId();
            int age = personDAO.index().get(i).getAge();
            prepareAndSendMessage(chatId, (i + 1) + ". ID:" + id + ", имя:" + name + ", возраст:" + age);
        }


    }

    private void startCommandReceived(long chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + ", добро пожаловать в библиотеку с цифровым учетом книг " + ":blush: . \n" + "Для описания фунционала нажмите на команду /help\n" + "Для информации о проекте нажмите /info");
        log.info("Replied to user " + name);
        prepareAndSendMessage(chatId, answer);
    }

    /*private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("weather");
        row.add("get random joke");
        keyboardRows.add(row);
        row = new KeyboardRow();
        row.add("register");
        row.add("get my data");
        row.add("delete my data");
        keyboardRows.add(row);
        keyboardMarkup.setKeyboard(keyboardRows);
        message.setReplyMarkup(keyboardMarkup);
        executeMessage(message);
    }*/

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Регистрация пройдет автоматически.\n" + "Вы серьезно хотите зарегестрироваться?");
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData(YES_BUTTON);
        var noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData(NO_BUTTON);
        rowInline.add(yesButton);
        rowInline.add(noButton);
        rowsInline.add(rowInline);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        message.setReplyMarkup(inlineKeyboardMarkup);
        executeMessage(message);
    }

    private void executeEditMessageText(String text, long chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.setMessageId((int) messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
        }
    }

    private void prepareAndSendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);
        executeMessage(message);

    }
}
