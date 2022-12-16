package ru.volchkov.telegramBot.service;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.volchkov.telegramBot.builder.Builder;
import ru.volchkov.telegramBot.builder.KeyboardButtonBuilder;
import ru.volchkov.telegramBot.dao.BookRepository;
import ru.volchkov.telegramBot.dao.PeopleRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookService {
    public List<SendMessage> allBooks(String chatId, PeopleRepository personDAO, BookRepository bookDAO) {
        List<SendMessage> messages = new ArrayList<>();
        messages.add(new SendMessage(chatId, EmojiParser.parseToUnicode("Всего книг в библиотеке: " + bookDAO.index().size() + "\n" +
                ":white_check_mark: - свободна \n" +
                ":o: - читают")));
        for (int i = 0; i < bookDAO.index().size(); i++) {
            String bookName = bookDAO.index().get(i).getName();
            String bookAuthor = bookDAO.index().get(i).getAuthor();
            int yearOfRelease = bookDAO.index().get(i).getYearOfRelease();
            String text;
            String buttonText;
            if (bookDAO.index().get(i).getBookStatus()) {
                text = EmojiParser.parseToUnicode(":white_check_mark:  " + bookName + ", " + bookAuthor + " " + yearOfRelease + "  :white_check_mark:");
            } else {
                text = EmojiParser.parseToUnicode(":o:  " + bookName + ", " + bookAuthor + " " + yearOfRelease + "  :o: \nКнигу взял(а)-" + personDAO.findPeople(personDAO.findPeople(bookName)).getName());
            }
            if (bookDAO.index().get(i).getBookStatus()) {
                buttonText = "Взять книгу";
            } else {
                buttonText = "Сдать книгу";
            }
            Builder builder = new KeyboardButtonBuilder().addButton(buttonText, bookName).addRowInline().setKeyboard();
            SendMessage sendMessage = createMessage(chatId, text);
            sendMessage.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
            messages.add(sendMessage);
        }
        return messages;
    }

    public EditMessageText takeBook(PeopleRepository peopleDAO, long peopleId, BookRepository bookDAO, String callbackData, String chatId, long messageId) {
        bookDAO.findBook(callbackData).setBookStatus(false);
        peopleDAO.findPeople(peopleId).getBooksOfPeople().add(bookDAO.findBook(callbackData));
        String text = EmojiParser.parseToUnicode("Вы взяли книгу-" + bookDAO.findBook(callbackData).getName() + ",\n" +
                "отличный выбор:open_book:");
        return createEditMessage(chatId, text, messageId);
    }

    public EditMessageText giveBook(PeopleRepository peopleDAO, BookRepository bookDAO, String callbackData, Update update, String chatId, long messageId) {
        peopleDAO.findPeople(peopleDAO.findPeople(callbackData)).getBooksOfPeople().remove(bookDAO.findBook(callbackData));
        bookDAO.findBook(callbackData).setBookStatus(true);
        String text = update.getCallbackQuery().getMessage().getChat().getFirstName() + ", Вы сдали книгу обратно в библиотеку";
        return createEditMessage(chatId, text, messageId);
    }

    public SendMessage createMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    public EditMessageText createEditMessage(String chatId, String text, long messageId) {
        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setText(text);
        message.setMessageId((int) messageId);
        return message;
    }
}
