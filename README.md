# Sentiment19

This project contains a web service based on the Spring Boot framework.
It features RESTful endpoints.


### local setup

- configure and run mongo-server (no Authorization for now, please)
- copy ```/src/main/resources/application.properties.local``` to ```/src/main/resources/application.properties```
- configure your db-Connection-Settings in the new file. Never commit this file! It should be ignored by git, anyhow.
- if you encounter any problems: ask someone. If you don't, you will probably fail. 
- Check out and run the ApplicationController 
(you might need to install Java 1.11 and configure the project to use it since Spring Boot seems to dislike later Java versions.
It's best to use Maven, though, it'll take care automatically)

### Port
- default port is 8080
- for deployment, port can be edited in ```src/main/resources/application.properties``` (on the server)

### Context-Path
context path is set to "/sentiment19" to have the same paths on the server and locally
Therefore, all Endpoints have the structure ```§baseUrl/sentiment19/...```, where
```$baseUrl``` will be ```localhost:8080/``` or ```basecamp-demmos.inf...```  

### Backend-Security
All Endpoints starting with ```sentiment19/backend/``` are protected by password.

user: ```admin``` \
password: ```ppp```

### Local Import
- put an unpacked text file (UTF-8 !!!) containing one json object per line into the directory configured in application.json
- call the endpoint "sentiment19/backend/import" (browser or cli)
- your local db should be filled with a bunch of tweets

### Mongo-Fake-Data
You can tweek the imported data for testing purposes:
Go to the ```src/main/resources/mongoscripts``` directory and run the following commands:
````
mongo MongoDates.js
mongo MongoFakeLabels.js
````
(alternatively just run thoses scripts with mongo from anywhere. Warning may not work out of the box with Authorization on the DB)
- first one assigns new, nicely distributed dates to all tweets
- second one labels 1/3 of all tweets as offensive

### Check using frontend
- URL: "localhost:8080/sentiment19/"

