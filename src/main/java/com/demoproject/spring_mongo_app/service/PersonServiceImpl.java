package com.demoproject.spring_mongo_app.service;

import com.demoproject.spring_mongo_app.collection.Person;
import com.demoproject.spring_mongo_app.repository.PersonRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PersonServiceImpl implements PersonService{

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public String save(Person person) {
        String personId = personRepository.save(person).getPersonId();
        log.info("person saved with id " + personId);
        return personId;
    }

    @Override
    public List<Person> getPersonStartWith(String name) {
        return personRepository.findByFirstNameStartsWith(name);
    }

    @Override
    public void deleteById(String id) {
        personRepository.deleteById(id);
    }

    @Override
    public List<Person> getPersonByAge(Integer minAge, Integer maxAge) {
        return personRepository.findPersonByAgeBetween(minAge,maxAge);
    }

    @Override
    public Page<Person> searchPerson(String name, Integer minAge, Integer maxAge, String city, Pageable pageable) {
        Query query = new Query().with(pageable);

        List<Criteria> criteria = new ArrayList<>();
        if(name!=null && !name.isEmpty()){
            criteria.add(Criteria.where("firstName").regex(name,"i"));
        }

        if(minAge!=null && maxAge!=null){
            criteria.add(Criteria.where("age").gte(minAge).lte(maxAge));
        }

        if(city!=null && !city.isEmpty()){
            criteria.add(Criteria.where("addresses.city").is(city));
        }
        if(!criteria.isEmpty()){
            query.addCriteria(
                    new Criteria()
                            .andOperator(criteria.toArray(new Criteria[0]))
            );
        }

        Page<Person> people = PageableExecutionUtils.getPage(
                mongoTemplate.find(query, Person.class
                ), pageable, () -> mongoTemplate.count(query.skip(0).limit(0),Person.class));
        return people;
    }

    @Override
    public List<Document> getOldestPerson() {

        UnwindOperation unwindOperation
                = Aggregation.unwind("addresses");

        SortOperation sortOperation
                = Aggregation.sort(Sort.Direction.DESC, "age");

        GroupOperation groupOperation
                =Aggregation
                .group("addresses.city")
                .first(Aggregation.ROOT)
                .as("oldestPerson");

        Aggregation aggregation
                = Aggregation.newAggregation(unwindOperation,sortOperation,groupOperation);

        List<Document> person
                = mongoTemplate.aggregate(aggregation, Person.class, Document.class).getMappedResults();

        return person;

    }

    @Override
    public List<Document> getPopulationByCity() {

        UnwindOperation unwindOperation
                = Aggregation.unwind("addresses");

        GroupOperation groupOperation
                = Aggregation.group("addresses.city")
                .count().as("popCount");

        SortOperation sortOperation
                = Aggregation.sort(Sort.Direction.DESC, "popCount");

        //what all fields to be displayed, use projection
        ProjectionOperation projectionOperation
                = Aggregation.project()
                .andExpression("_id").as("city")
                .andExpression("popCount").as("count")
                .andExclude("_id");

        Aggregation aggregation
                = Aggregation.newAggregation(unwindOperation, groupOperation, sortOperation, projectionOperation);

        List<Document> person = mongoTemplate.aggregate(aggregation,Person.class, Document.class).getMappedResults();

        return person;


    }
}
