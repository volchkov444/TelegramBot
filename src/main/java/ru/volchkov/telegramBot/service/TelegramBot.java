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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.volchkov.telegramBot.config.BotConfig;
import ru.volchkov.telegramBot.dao.BookRepository;
import ru.volchkov.telegramBot.dao.PeopleRepository;
import ru.volchkov.telegramBot.model.PeopleStatus;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    BookService bookService;
    UserService userService;
    final BotConfig config;
    final BookRepository bookRepository;
    final PeopleRepository peopleRepository;
    String regex = "\\d+";
    static final String HELP_TEXT = "1. Добавление, изменение и удаление книг\n" + "2. Страница со списком всех людей\n" + "3. Страница со списком всех книг\n" + "4. Страница человека, на которой показаны значения его полей и список книг которые он взял.\n" + "6. Возможность освободить книгу";
    static final String INFO_TEXT = EmojiParser.parseToUnicode("Бот взят за основу разработки API приложения, чтобы реализовать" + " бизнес-логику проекта без html кода.\n                                                     " + ":heavy_check_mark:Задача:heavy_check_mark:\n" + "В местной библиотеке хотят перейти на цифровой учет книг. Нам " + "было необходимо реализовать приложение для них. Библиотекари " + "должны иметь возможность регистрировать читателей, выдавать им " + "книги и освобождать книги (после того, как читатель возвращает " + "книгу обратно в библиотеку).");
    static final String YES_BUTTON = "YES_BUTTON";
    static final String NO_BUTTON = "NO_BUTTON";


    public TelegramBot(BotConfig config, BookRepository bookRepository, PeopleRepository personRepository, UserService userService, BookService bookService) {
        this.bookService = bookService;
        this.userService = userService;
        this.bookRepository = bookRepository;
        this.peopleRepository = personRepository;
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "получить начальную информацию"));
        listOfCommands.add(new BotCommand("/register", "регистрация нового пользователя"));
        listOfCommands.add(new BotCommand("/info", "описание проекта"));
        listOfCommands.add(new BotCommand("/help", "описание функционала"));
        listOfCommands.add(new BotCommand("/listofusers", "вывести всех зарегестрированных людей"));
        listOfCommands.add(new BotCommand("/addbook", "добавить книгу в библиотеку"));
        listOfCommands.add(new BotCommand("/takebook", "посмотреть список книг, взять книгу"));
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
        long id = 0;
        String textFromUpdate = null;
        if (update.hasMessage()) {
            textFromUpdate = update.getMessage().getText();
            id = update.getMessage().getChat().getId();
        }
        if (update.hasMessage() && peopleRepository.findPeople(id) == null) {
            executeMessage(userService.startCommand(peopleRepository, update));
        }
        if (update.hasMessage() && textFromUpdate.equals("/register") &&
                peopleRepository.findPeople(id).getPeopleStatus().equals(PeopleStatus.GUEST)) {
            executeMessage(userService.register(update));
        }
        if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            String chatId = String.valueOf(update.getCallbackQuery().getMessage().getChatId());
            long peopleId = update.getCallbackQuery().getMessage().getChat().getId();
            long idQuery = update.getCallbackQuery().getMessage().getChat().getId();
            if (bookRepository.findBook(callbackData) != null && peopleRepository.findPeople(idQuery).getPeopleStatus().equals(PeopleStatus.USER) &&
                    bookRepository.findBook(callbackData).getBookStatus()) {
                executeMessage(bookService.takeBook(peopleRepository, peopleId, bookRepository, callbackData, chatId, messageId));

            } else if (bookRepository.findBook(callbackData) != null && !bookRepository.findBook(callbackData).getBookStatus() &&
                    peopleRepository.findPeople(idQuery).getPeopleStatus().equals(PeopleStatus.USER) &&
                    update.getCallbackQuery().getMessage().getChat().getId().equals(peopleRepository.findPeople(callbackData))) {
                executeMessage(bookService.giveBook(peopleRepository, bookRepository, callbackData, update, chatId, messageId));
            }
            if (callbackData.equals(YES_BUTTON)) {
                executeEditMessageText("Сколько вам лет?", chatId, messageId);

            }
            if (callbackData.equals(NO_BUTTON)) {
                String text = EmojiParser.parseToUnicode("Вы нажали на кнопку NO:cry:");
                executeEditMessageText(text, chatId, messageId);
            }
        }
        if (update.hasMessage() && textFromUpdate.matches(regex) &&
                Integer.parseInt(textFromUpdate) <= 100
                && Integer.parseInt(textFromUpdate) > 0 &&
                peopleRepository.findPeople(id).getPeopleStatus().equals(PeopleStatus.GUEST)) {
            executeMessage(userService.setStatusUSER(update, peopleRepository));
        }
        if (update.hasMessage() && peopleRepository.findPeople(id).getPeopleStatus().equals(PeopleStatus.USER) &&
                !textFromUpdate.matches(regex)) {
            String message = textFromUpdate;
            String chatId = String.valueOf(update.getMessage().getChatId());

            switch (message) {
                case "/start" -> startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                case "/info" -> prepareAndSendMessage(chatId, INFO_TEXT);
                case "/help" -> prepareAndSendMessage(chatId, HELP_TEXT);
                case "/listofusers" -> userService.listOfUsers(chatId, peopleRepository).
                        forEach(this::executeMessage);
                case "/takebook" -> bookService.allBooks(chatId, peopleRepository, bookRepository).
                        forEach(this::executeMessage);
                default ->
                        prepareAndSendMessage(chatId, EmojiParser.parseToUnicode(update.getMessage().getChat().getFirstName()
                                + ",Вы ввели неверную команду,попробуйте еще раз :unamused:"));
            }
        }
    }

    private void startCommandReceived(String chatId, String name) {
        String answer = EmojiParser.parseToUnicode("Привет, " + name + ", добро пожаловать в библиотеку с цифровым учетом книг " + ":blush: . \n" + "Для описания фунционала нажмите на команду /help\n" + "Для информации о проекте нажмите /info");
        log.info("Replied to user " + name);
        prepareAndSendMessage(chatId, answer);
    }

    private void executeEditMessageText(String text, String chatId, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId((int) messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message sending error" + e.getMessage());
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message sending error" + e.getMessage());
        }
    }

    private void executeMessage(EditMessageText message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Message sending error" + e.getMessage());
        }
    }

    private void prepareAndSendMessage(String chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        executeMessage(message);
    }

    private void sendMessage(String chatId, String textToSend, ReplyKeyboard keyboard) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (textToSend != null) {
            sendMessage.setText(textToSend);
        }
        if (keyboard != null) {
            sendMessage.setReplyMarkup(keyboard);
        }
        executeMessage(sendMessage);
    }
}
