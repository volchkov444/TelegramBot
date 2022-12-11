package ru.volchkov.telegramBot.model;

import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;


@Data
@Entity
@Table(name = "book")
public class Book {
    @Id
    private long id;
    @Column(name = "name")
    private String name;
    @Column(name = "author")
    private String author;
    @Column(name = "yearOfRelease")
    private int yearOfRelease;
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Person person;

    public Book() {
    }

}
