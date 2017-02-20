# ba-service-backend

This project contains a web service based on the Spring Boot framework.
It features a RESTful endpoint that can retrieve word expansions.

- Check out and run the ApplicationController
- default port is 8080
- for deployment, port can be edited in /resources/application.properties
- check with "curl localhost:8080/expansions?word=IBM&format=json"
- for textual output: "curl localhost:8080/expansions?word=IBM&format=text"
