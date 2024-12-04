FROM openjdk:8
EXPOSE 8080
COPY ./target/spring-mongo-app.jar spring-mongo-app.jar
ENTRYPOINT ["java","-jar","/spring-mongo-app.jar"]