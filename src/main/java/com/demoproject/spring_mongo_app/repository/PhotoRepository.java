package com.demoproject.spring_mongo_app.repository;

import com.demoproject.spring_mongo_app.collection.Photo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhotoRepository extends MongoRepository<Photo,String> {
}
