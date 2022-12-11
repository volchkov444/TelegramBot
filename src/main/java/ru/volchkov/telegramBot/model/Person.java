package ru.volchkov.telegramBot.model;

import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "person")
public class Person {
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "age")
    private int age;
    @Enumerated(EnumType.STRING)
    private PersonStatus personStatus;

    public Person() {
    }

    @OneToMany(mappedBy = "person", fetch = FetchType.EAGER)
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private List<Book> booksOfPerson = new ArrayList<>();
}
