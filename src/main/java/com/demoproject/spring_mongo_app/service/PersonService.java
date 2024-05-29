package com.demoproject.spring_mongo_app.service;

import com.demoproject.spring_mongo_app.collection.Person;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PersonService {

    String save(Person person);

    List<Person> getPersonStartWith(String name);

    void deleteById(String id);

    List<Person> getPersonByAge(Integer minAge, Integer maxAge);

    Page<Person> searchPerson(String name, Integer minAge, Integer maxAge, String city, Pageable pageable);

    List<Document> getOldestPerson();

    List<Document> getPopulationByCity();
}
