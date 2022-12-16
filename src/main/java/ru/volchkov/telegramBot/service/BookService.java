package ru.volchkov.telegramBot.service;

import com.vdurmont.emoji.EmojiParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.volchkov.telegramBot.builder.ButtonsBuilder;
import ru.volchkov.telegramBot.builder.ButtonBuilder;
import ru.volchkov.telegramBot.model.Person;
import ru.volchkov.telegramBot.repository.BookRepository;
import ru.volchkov.telegramBot.model.Book;
import ru.volchkov.telegramBot.repository.PersonRepository;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BookService {
    private final PersonRepository personRepository;
    private final BookRepository bookRepository;

    public List<SendMessage> allBooks(String chatId) {
        List<Book> books = bookRepository.findAll();
        List<SendMessage> messages = new ArrayList<>();
        messages.add(new SendMessage(chatId, EmojiParser.parseToUnicode("Всего книг в библиотеке: " +
                books.size() + "\n" + ":white_check_mark: - свободных " +
                books.stream().filter(a -> a.getPerson() == null).toArray().length + "\n" + ":o: - читают " +
                books.stream().filter(a -> a.getPerson() != null).toArray().length)));
        for (final Book book : bookRepository.findAll()) {
            String bookName = book.getName();
            String bookAuthor = book.getAuthor();
            int yearOfRelease = book.getYearOfRelease();
            String text;
            String buttonText;
            if (book.getPerson() == null) {
                text = EmojiParser.parseToUnicode(":white_check_mark:  " + bookName + ", " + bookAuthor + " " +
                        yearOfRelease + "  :white_check_mark:");
            } else {
                text = EmojiParser.parseToUnicode(":o:  " + bookName + ", " + bookAuthor + " " + yearOfRelease +
                        "  :o: \nКнигу взял(а)-" + book.getPerson().getName());
            }
            if (book.getPerson() == null) {
                buttonText = "Взять книгу";
            } else {
                buttonText = "Сдать книгу";
            }
            ButtonsBuilder builder = new ButtonBuilder().addButton(buttonText, bookName).addRowInline().setKeyboard();
            SendMessage sendMessage = createMessage(chatId, text);
            sendMessage.setReplyMarkup(builder.build().getInlineKeyboardMarkup());
            messages.add(sendMessage);
        }
        return messages;
    }

    public EditMessageText takeBook(long peopleId, String callbackData, String chatId, long messageId) {
        Book book = bookRepository.findBookByName(callbackData).orElseThrow();
        Person person = personRepository.findPersonById(peopleId).orElseThrow();
        book.setPerson(person);
        bookRepository.save(book);
        String text = EmojiParser.parseToUnicode("Вы взяли книгу-" + book.getName() + ",\n" + "отличный выбор:open_book:");
        return createEditMessage(chatId, text, messageId);
    }

    public EditMessageText giveBook(String callbackData, Update update, String chatId, long messageId) {
        Book book = bookRepository.findBookByName(callbackData).orElseThrow();
        book.setPerson(null);
        bookRepository.save(book);
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
