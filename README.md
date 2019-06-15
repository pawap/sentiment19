# ba-service-backend

This project contains a web service based on the Spring Boot framework.
It features RESTful endpoints.


### local setup

- configure and run mysql-server
- copy /src/main/resources/application.properties.dist to /src/main/resources/application.properties
- configure your db-Connection-Settings in the new file. Never commit this file! It should be ignored by git, anyhow.
- if you encounter any problems: ask someone. If you don't, you will probably fail. 
- Check out and run the ApplicationController 
(you might need to install Java 1.8 and configure the project to use it since Spring Boot seems to dislike later Java versions.
It's best to use Maven, though, it'll take care automatically)

### Port
- default port is 8080
- for deployment, port can be edited in src/main/resources/application.properties (on the server)

### Local Import
- put an unpacked text file (UTF-8 !!!) containing one json object per line into the directory configured in application.json
- call the endpoint "/import" (browser or cli)
- your local db should be filled with a bunch of tweets

### Check with cURL

- check with "curl localhost:8080/sentiments?tweet=IBM&format=json"
- for textual output: "curl localhost:8080/sentiments?tweet=IBM&format=text"

### Check using html-tester
- URL: "localhost:8081/html"


