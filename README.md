# Ride Sharing Backend

A Spring Boot based backend system for a ride sharing / carpool application.

This project implements user registration, ride publishing, booking management,
and full integration + unit testing using H2 in-memory database.

---

## TECH STACK

- Java 17
- Spring Boot 3.2.1
- Spring Web (REST APIs)
- Spring Data JPA
- Hibernate ORM
- H2 In-Memory Database
- Spring Validation
- Spring Security Crypto (BCrypt)
- Maven
- JUnit 5
- TestRestTemplate

---

## PROJECT STRUCTURE

backend
├── src
│ ├── main
│ │ ├── java
│ │ │ └── com
│ │ │ └── carpool
│ │ │ ├── config
│ │ │ │ ├── SecurityConfig.java
│ │ │ │ └── GlobalExceptionHandler.java
│ │ │ │
│ │ │ ├── controller
│ │ │ │ ├── UserController.java
│ │ │ │ ├── RideController.java
│ │ │ │ └── BookingController.java
│ │ │ │
│ │ │ ├── dto
│ │ │ │ └── RegisterRequest.java
│ │ │ │
│ │ │ ├── model
│ │ │ │ ├── User.java
│ │ │ │ ├── Ride.java
│ │ │ │ ├── Booking.java
│ │ │ │ ├── UserRole.java
│ │ │ │ ├── RideStatus.java
│ │ │ │ └── BookingStatus.java
│ │ │ │
│ │ │ ├── repository
│ │ │ │ ├── UserRepository.java
│ │ │ │ ├── RideRepository.java
│ │ │ │ └── BookingRepository.java
│ │ │ │
│ │ │ ├── service
│ │ │ │ ├── UserService.java
│ │ │ │ ├── RideService.java
│ │ │ │ └── BookingService.java
│ │ │ │
│ │ │ ├── service
│ │ │ │ └── impl
│ │ │ │ ├── UserServiceImpl.java
│ │ │ │ ├── RideServiceImpl.java
│ │ │ │ └── BookingServiceImpl.java
│ │ │ │
│ │ │ └── RideSharingApplication.java
│ │ │
│ │ └── resources
│ │ └── application.yml
│ │
│ └── test
│ └── java
│ └── com
│ └── carpool
│ ├── integration
│ │ ├── UserControllerIntegrationTest.java
│ │ └── BookingIntegrationTest.java
│ │
│ ├── system
│ │ └── SystemFlowTest.java
│ │
│ └── unit
│ ├── UserServiceTest.java
│ └── FareStrategyTest.java
│
├── pom.xml
└── README.md

---

## HOW TO RUN THE PROJECT

1. Clone the repository

   git clone <your-repository-url>
   cd backend

2. Make sure Java 17 is installed

   java -version

3. Run the application

   mvn spring-boot:run

OR

mvn clean package
java -jar target/backend-0.0.1-SNAPSHOT.jar

Application will start on:

http://localhost:8080

---

## H2 DATABASE CONSOLE

URL:
http://localhost:8080/h2-console

JDBC URL:
jdbc:h2:mem:testdb

Username:
sa

Password:
(leave empty)

---

## HOW TO RUN TESTS

Run all tests:

mvn clean test

Run full verification:

mvn clean verify

Test reports location:

target/surefire-reports/

---

## BUILD COMMANDS EXPLAINED

mvn clean

- Deletes target directory

mvn test

- Compiles and runs tests

mvn verify

- Runs full build lifecycle including tests

mvn spring-boot:run

- Runs application directly

---

## API ENDPOINTS

USER APIs

POST /api/users/register
POST /api/users/login
PUT /api/users/{id}

RIDE APIs

POST /api/rides/{driverId}
GET /api/rides/search

BOOKING APIs

POST /api/bookings
PUT /api/bookings/{bookingId}/cancel

---

## DATABASE CONFIGURATION (application.yml)

spring:
datasource:
url: jdbc:h2:mem:testdb
driver-class-name: org.h2.Driver
username: sa
password:

jpa:
hibernate:
ddl-auto: update
show-sql: true

h2:
console:
enabled: true

---

## TESTING TYPES IMPLEMENTED

1. Unit Tests
   - UserServiceTest
   - FareStrategyTest

2. Integration Tests
   - UserControllerIntegrationTest
   - BookingIntegrationTest

3. System Test
   - SystemFlowTest

---

## HOW TO GENERATE TEST REPORTS

After running:

mvn clean test

Reports will be generated in:

target/surefire-reports/

Each test class will have:

- .txt report
- .xml report

---

## PROJECT STATUS

- User registration with validation
- BCrypt password hashing
- Ride publishing
- Ride search
- Seat booking
- Booking cancellation
- Optimistic locking on Ride entity
- Clean exception handling
- Integration + Unit testing
- H2 database integration

---

End of Documentation
