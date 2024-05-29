package com.demoproject.spring_mongo_app.repository;

import com.demoproject.spring_mongo_app.collection.Person;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface PersonRepository extends MongoRepository<Person,String> {

    List<Person> findByFirstNameStartsWith(String name);

    //@Query(value = "{ 'age' : { $gt : ?0, $lt : ?1}}",
    //           fields = "{addresses:  0}")

    //And we can ignore fields, if we dont want that field give it as 0, or else give it as 1
    //$gt - greater than first parameter which we have passed as min
    //&lt - lesser than last parameter which we have passed as max
    @Query(value = "{'age' : { $gt : ?0, $lt : ?1}}",
            fields = "{addresses: 0}")
    List<Person> findPersonByAgeBetween(Integer min, Integer max);


}
