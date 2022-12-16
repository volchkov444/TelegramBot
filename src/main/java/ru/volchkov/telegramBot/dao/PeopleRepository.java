package ru.volchkov.telegramBot.dao;

import org.springframework.stereotype.Component;
import ru.volchkov.telegramBot.model.People;
import ru.volchkov.telegramBot.model.PeopleStatus;

import java.util.ArrayList;
import java.util.List;

@Component
public class PeopleRepository {
    private  final List<People> peopleList = new ArrayList<>();

    public List<People> index() {
        return peopleList;
    }

    public People findPeople(long id) {
        return peopleList.stream().filter(a -> a.getId() == id).findAny().orElse(null);
    }

    public long findPeople(String name) {
        long id = 0;
        for (People people : peopleList) {
            if (people.findBook(name) != null) {
                id = people.getId();
            }
        }
        return id;
    }

    public void addPeople(long id, String name, int age, PeopleStatus peopleStatus) {
        People people = new People();
        people.setId(id);
        people.setName(name);
        people.setAge(age);
        people.setPeopleStatus(peopleStatus);
        peopleList.add(people);
    }
}
