package ru.volchkov.telegramBot.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;

@Builder
@Data
@Entity
@Table(name = "book")
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Book {
    @Id
    @GeneratedValue
    private Long id;
    @Column(name = "name")
    private String name;
    @Column(name = "author")
    private String author;
    @Column(name = "yearOfRelease")
    private Integer yearOfRelease;
    @ManyToOne
    @JoinColumn(name = "person_id", referencedColumnName = "id")
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    private Person person;

    public Book(){}
}
