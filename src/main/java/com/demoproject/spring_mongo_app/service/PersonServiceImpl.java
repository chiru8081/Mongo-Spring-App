package com.demoproject.spring_mongo_app.service;

import com.demoproject.spring_mongo_app.collection.Person;
import com.demoproject.spring_mongo_app.repository.PersonRepository;
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
public class PersonServiceImpl implements PersonService{

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Override
    public String save(Person person) {
        return personRepository.save(person).getPersonId();
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

        //Query is a class from Spring Data MongoDB that represents a MongoDB query.
        // It allows you to define query criteria, sorting, and pagination.

        //note:MongoDB stores the data in the binary json format

        //Pageable is an interface in Spring Data that provides pagination information.
        //It includes information such as the page number, the size of the page

        //Create a New Query Instance:
        //Apply Pagination:This method sets the page number and the page size for the query.
        // It also applies any sorting instructions defined in the Pageable object.
        //advantage: This allows you to retrieve a specific page of results from the database, which is useful.
        Query query = new Query().with(pageable);

        List<Criteria> criteria = new ArrayList<>();

        //firstName should match with passed name and it should be case-sensitive ("i")
        if(name!=null && !name.isEmpty()){
            criteria.add(Criteria.where("firstName").regex(name,"i"));
        }

        if(minAge!=null && maxAge!=null){
            criteria.add(Criteria.where("age").gte(minAge).lte(maxAge));
        }

        if(city!=null && !city.isEmpty()){
            criteria.add(Criteria.where("addresses.city").is(city));
        }

        //It handles the situation where multiple criteria need to be combined into a single query
        // using an AND logical operator and converts list of criteria to array
        if(!criteria.isEmpty()){
            query.addCriteria(
                    new Criteria()
                            .andOperator(criteria.toArray(new Criteria[0]))
            );
        }

        //MongoTemplate is a part of the Spring Data MongoDB library,
        // and it interacts MongoDB in a Spring application
        //perform various operations such as CRUD operations, query execution, and aggregation
        //Why Use MongoTemplate?
        //MongoTemplate abstracts much of the boilerplate code required to interact with MongoDB
        //For custom queries that are too complex for the repository method naming conventions,
        // MongoTemplate can be used to directly execute these queries.
        //MongoTemplate offers a comprehensive set of APIs that cover most of the functionality
        //and provides aggregation operations

        //steps:
        //1-PageableExecutionUtils.getPage: Helps to create page object
        //2-mongoTemplate.find(query, Person.class):This executes the MongoDB query and
        // retrieves a list of Person objects
        // based on the specified query
        //3-pageable: that contains pagination information, such as the page number,
        // the size of the page, and sorting information.
        //4-mongoTemplate.count(query.skip(0).limit(0), Person.class):This executes a count query to get the total number of
        // Person documents that match the specified query
        //The skip(0).limit(0) part ensures that the count query does not skip or limit any documents,
        // effectively counting all documents that match the query.

        Page<Person> people = PageableExecutionUtils.getPage(
                mongoTemplate.find(query, Person.class
                ), pageable, () -> mongoTemplate.count(query.skip(0).limit(0),Person.class));
        return people;
    }

    @Override
    public List<Document> getOldestPerson() {

        //first we are going to flatten the address,
        //as we the oldest person in the city and city is avaliable in the address pojo
        //So to flatten we do unwind operation - first step

        UnwindOperation unwindOperation
                = Aggregation.unwind("addresses");

        //2nd step(Sorting) - lets sort based on age

        SortOperation sortOperation
                = Aggregation.sort(Sort.Direction.DESC, "age");

        //3rd step is grouping
        //lets group by city and get the first document out of it by providing the document name as "oldestPerson

        GroupOperation groupOperation
                =Aggregation
                .group("addresses.city")
                .first(Aggregation.ROOT)
                .as("oldestPerson");

        Aggregation aggregation
                = Aggregation.newAggregation(unwindOperation,sortOperation,groupOperation);


        //we pass these aggregation to mongoTemplate to get the data back as document
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
