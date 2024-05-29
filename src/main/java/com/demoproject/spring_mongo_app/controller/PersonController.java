package com.demoproject.spring_mongo_app.controller;

import com.demoproject.spring_mongo_app.collection.Person;
import com.demoproject.spring_mongo_app.service.PersonService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/person")
public class PersonController {

    @Autowired
    private PersonService personService;

    @PostMapping
    private String save(@RequestBody Person person){
        return personService.save(person);
    }
    @GetMapping
    private List<Person> getPersonStartsWith(@RequestParam("name") String name){
        return personService.getPersonStartWith(name);
    }

    @DeleteMapping("/{id}")
    private void delete(@PathVariable String id){
        personService.deleteById(id);
    }

    @GetMapping("/age")
    private List<Person> getByPersonAge(@RequestParam Integer minAge,
                                        @RequestParam Integer maxAge){
        return personService.getPersonByAge(minAge, maxAge);
    }

    @GetMapping("/search")
    private Page<Person> searchPerson(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "5") Integer size
    ){
        Pageable pageable = PageRequest.of(page, size);

        return personService.searchPerson(name,minAge,maxAge,city,pageable);
    }

    @GetMapping("/oldestPerson")
    private List<Document> getOldestPerson(){
        return personService.getOldestPerson();
    }

    @GetMapping("/getPopulationByCity")
    private List<Document> getPopulationByCity(){
        return personService.getPopulationByCity();
    }
}
