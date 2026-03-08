##### Dit project is een quizapp met:

##### \- een Angular frontend

##### \- een Spring Boot backend


##### Wat heb je nodig?

##### 

##### Voor lokaal draaien 

##### \- Node.js

##### \- npm

##### \- Java 25

##### 

##### Voor draaien met Docker

##### \- Docker

##### \- Docker Compose

##### 

##### 1\) Lokaal runnen

##### 

##### Stap 1: backend starten

##### 

##### Open een terminal:

##### 

##### cd backend

##### ./gradlew bootRun

##### 

##### 

##### De backend draait dan op:

##### \- http://localhost:8080

##### 

##### Stap 2: frontend starten

##### 

##### Open een tweede terminal:

##### 

##### 

##### cd frontend

##### npm install

##### npm start

##### 

##### 

##### De frontend draait dan op:

##### \- http://localhost:4200

##### 

##### De frontend gebruikt in development automatisch `http://localhost:8080` als API URL.

##### 

##### 

##### 2\) Runnen met Docker

##### 

##### Wil je alles in containers draaien? Gebruik dan:

##### 

##### docker compose up --build

##### 

##### Open daarna:

##### \- http://localhost

##### 

##### Stoppen

##### 

##### docker compose down

##### 

##### 

##### 

##### 

