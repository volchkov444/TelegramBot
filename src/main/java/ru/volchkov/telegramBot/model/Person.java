package ru.volchkov.telegramBot.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Builder(toBuilder = true)
@Data
@Entity
@Table(name = "person")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Person {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private Integer age;
    @Enumerated(EnumType.STRING)
    private PersonStatus personStatus;

    public Person() {
    }

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private List<Book> booksOfPerson = new ArrayList<>();
}
