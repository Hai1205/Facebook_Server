# Facebook Clone

## Overview

**Facebook Clone** is a full-featured social networking web application inspired by Facebook. It supports features like posting statuses, stories, user and post management, and more.

- 📚 Admin dashboard for management  
- 🔍 Search for users  
- ❤️ Like, share, and comment on posts  
- 👤 User profiles  
- 🔐 Full authentication system  
- 💬 Real-time chat between users  
- 📱 Responsive UI design  

---

## Technologies Used

## Backend Dependencies

### Core Frameworks
- **Spring Boot Starter Web**: A comprehensive framework for creating web applications with Spring Boot, including embedded servers and RESTful APIs.
- **Spring Boot DevTools**: Provides additional development-time features like hot reload, automatic restart, and live reload for easier debugging.
- **Spring Boot Starter Security**: Adds built-in security features, including authentication and authorization to your application.
- **Spring Boot Starter Validation**: Provides validation support for JavaBeans using JSR-303/JSR-380 annotations.
- **Spring Boot Starter Mail**: Adds email sending capabilities to the application with Spring Boot.
- **Spring Boot Starter Thymeleaf**: Provides integration for Thymeleaf, a templating engine for web applications.
- **Spring Boot Starter WebSocket**: Adds WebSocket support to your application for real-time bi-directional communication.
- **Spring Boot Starter OAuth2 Client**: Simplifies the implementation of OAuth2 authentication in client applications.

### Data Persistence & Storage
- **Spring Boot Starter Data MongoDB**: Integrates MongoDB with Spring Boot for easy data persistence.
- **Jakarta Persistence API**: A specification for working with relational databases in Java, previously known as JPA.
- **Hibernate Core**: A popular ORM (Object-Relational Mapping) framework used for persistence in Java applications.
- **AWS Java SDK S3**: The official SDK for working with AWS S3 storage services.

### Security & Authentication
- **Google API Client**: A Java client library for interacting with Google APIs, such as OAuth2 for Google login.
- **Google Auth Library OAuth2 HTTP**: Provides OAuth2 authentication features for Google services.
- **JJWT Jackson**: A library for working with JSON Web Tokens (JWT) using Jackson for serialization/deserialization.
- **JJWT Impl**: Implements the logic for creating and parsing JSON Web Tokens (JWT).
- **JJWT API**: Provides the JWT API for creating and verifying tokens.
- **Spring Security Test**: A set of tools for testing Spring Security configurations.

### Development & Testing
- **Lombok**: A Java library that helps reduce boilerplate code like getters, setters, and constructors.
- **Spring Boot Starter Test**: Provides testing utilities and dependencies for unit and integration tests.
- **Spring Boot Configuration Processor**: Processes configuration properties and generates metadata for Spring Boot applications.
- **Spring Dotenv**: Loads environment variables from a `.env` file for easier configuration management.

### Real-time Communication
- **Netty SocketIO**: An implementation of the Socket.IO protocol for handling WebSocket connections.

---

## Installation

### Prerequisites

- Java JDK 21+  
- MongoDB  
- AWS account (ask the Indian teammates 😄)  

### Setup Steps

1. Clone the repository and navigate to the project directory:
```bash
git clone https://github.com/Hai1205/Facebook_Server.git
cd Facebook_Server
```

2. Configure the required environment variables:

```properties
spring.application.name=Facebook_Server
server.port=${PORT}

client.url=${CLIENT_URL_3000}

spring.data.mongodb.uri=${FACEBOOK_MONGODB_URI}
spring.data.mongodb.auto-index-creation=true

aws.s3.bucket.name=facebook-server
aws.s3.access.key=${S3_ACCESS_KEY}
aws.s3.secret.key=${S3_SECRET_KEY}

spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=50MB

spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USER}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.ssl.trust=smtp.gmail.com

spring.mvc.async.request-timeout=30000
spring.websocket.max-text-message-size=8192
spring.websocket.max-binary-message-size=65536
socketio.host=localhost
socketio.port=4041

gemini.api.key=${GEMINI_API_KEY}
gemini.api.url=${GEMINI_API_URL}
```

---

## Team Members

| Student ID     | Full Name           |
|----------------|---------------------|
| 3122410095     | Nguyen Hoang Hai    |
| 3122410117     | Nguyen Van Hieu     |
| 3122410365     | Doan Tuan Tai       |