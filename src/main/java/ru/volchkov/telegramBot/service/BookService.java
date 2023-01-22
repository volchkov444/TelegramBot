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
import ru.volchkov.telegramBot.model.PersonStatus;
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
        for (final Book book : books) {
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
            ButtonsBuilder builder = new ButtonBuilder()
                    .addButton(buttonText, bookName)
                    .addRowInline()
                    .setKeyboard();
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

    public SendMessage addNewBook(long peopleId, String chatId) {
        Person person = personRepository.findPersonById(peopleId).orElseThrow();
        Book book = Book.builder()
                .name("test")
                .author("test")
                .yearOfRelease(1)
                .build();
        bookRepository.save(book);
        person.setPersonStatus(PersonStatus.TAPING_NAME_BOOK);
        personRepository.save(person);
        return new SendMessage(chatId, "Добавляем новую книгу в библиотеку:\n\n" + "Введите название книги:");
    }

    public SendMessage addBookName(String name, String chatId, Person person) {
        System.out.println();
        Book book = bookRepository.findBookByName("test").orElseThrow();
        book.setName(name);
        bookRepository.save(book);
        person.setPersonStatus(PersonStatus.TAPING_AUTHOR_BOOK);
        personRepository.save(person);
        return new SendMessage(chatId, "Вы успешно добавили название книги.\n\n" + "Введите автора:");
    }

    public SendMessage addBookAuthor(String name, String chatId, Person person) {
        Book book = bookRepository.findBookByAuthor("test").orElseThrow();
        book.setAuthor(name);
        bookRepository.save(book);
        person.setPersonStatus(PersonStatus.TAPING_YEAR_OF_BOOK);
        personRepository.save(person);
        return new SendMessage(chatId, "Вы успешно добавили автора.\n\n" + "Введите год выпуска книги:");
    }

    public SendMessage addBookYear(int year, String chatId, long Id) {
        Book book = bookRepository.findBookByYearOfRelease(1).orElseThrow();
        book.setYearOfRelease(year);
        Person person = personRepository.findPersonById(Id).orElseThrow();
        person.setPersonStatus(PersonStatus.USER);
        bookRepository.save(book);
        personRepository.save(person);
        return new SendMessage(chatId, "Вы успешно добавили год выпуска и создали книгу.\n\n" + "Теперь можете взять ее из библиотеки.");
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
        message.setMessageId((Math.toIntExact(messageId)));
        return message;
    }
}
