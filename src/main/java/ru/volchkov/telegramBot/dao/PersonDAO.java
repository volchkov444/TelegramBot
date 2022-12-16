package ru.volchkov.telegramBot.dao;

import org.springframework.stereotype.Component;
import ru.volchkov.telegramBot.model.People;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersonDAO {
    List<People> peopleList = new ArrayList<>();

    public List<People> index() {
        return peopleList;
    }

    public People collect(long id) {
        return peopleList.stream().filter(a -> a.getId() == id).findAny().orElse(null);
    }

    public void addPeople(People people) {
        peopleList.add(people);
    }
    public long findPeople(String name){
        long id=0;
        for (People people : peopleList) {
            if (people.findBook(name) != null) {
                id=people.getId();
            }
        }
        return id;
    }
}
