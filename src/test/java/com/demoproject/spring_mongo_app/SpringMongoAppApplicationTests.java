package com.demoproject.spring_mongo_app;

import com.demoproject.spring_mongo_app.collection.Address;
import com.demoproject.spring_mongo_app.collection.Person;
import com.demoproject.spring_mongo_app.repository.PersonRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class SpringMongoAppApplicationTests {

	@Container
	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry dynamicPropertyRegistry){

		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
//		String uri = String.format("mongodb://%s:%s@%s:%d/%s?authSource=%s",
//				"admin",
//				"password",
//				mongoDBContainer.getHost(),
//				mongoDBContainer.getFirstMappedPort(),
//				"demo",
//				"admin");
//		dynamicPropertyRegistry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);

//		dynamicPropertyRegistry.add("spring.data.mongodb.authentication-database", () -> "admin");
//		dynamicPropertyRegistry.add("spring.data.mongodb.username", () -> "admin");
//		dynamicPropertyRegistry.add("spring.data.mongodb.password", () -> "password");
//		dynamicPropertyRegistry.add("spring.data.mongodb.database", () -> "demo");
//		dynamicPropertyRegistry.add("spring.data.mongodb.port", () -> mongoDBContainer.getFirstMappedPort());
//		dynamicPropertyRegistry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
	}
	@Test
	void shouldSavePerson() throws Exception {
		
		Person person = getPersonDetails();
		String objectPersonAsString = objectMapper.writeValueAsString(person);
		mockMvc.perform(MockMvcRequestBuilders.post("/person")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectPersonAsString))
				.andExpect(status().isCreated());

		Assertions.assertEquals(1,personRepository.findAll().size());

	}

	private Person getPersonDetails() {

		List<Address> addressess = getAddressList();
		List<String> hobbies = getHobbies();
		return Person.builder()
				.firstName("chiranjeevi")
				.lastName("M")
				.addresses(addressess)
				.hobbies(hobbies)
				.build();

	}

	private List<String> getHobbies() {

		List<String> hobbiesList = new ArrayList<>();

		List<String> list = Arrays.asList("cricket", "football", "indoor-games");
		hobbiesList.add(list.toString());
		return hobbiesList;
	}

	private List<Address> getAddressList() {

		List<Address> addressList = new ArrayList<>();

		Address address1 = Address.builder()
				.address1("mallathalli")
				.address2("same as above")
				.city("bangalore").build();

		Address address2 = Address.builder()
				.address1("shakthinagar")
				.address2("same as above")
				.city("raichur").build();

		addressList.add(address1);
		addressList.add(address2);

		return addressList;


	}


}
