# SpotifyApp

## Overview

> *[Add a brief description of the project here — what it does, its main features, and the problem it solves]*

SpotifyApp is a Java desktop application that connects to a MySQL database to manage and display Spotify-related data. It leverages HikariCP for efficient database connection pooling and uses SLF4J for logging.

---

## Features

- Connects securely to a MySQL database using connection pooling (HikariCP)
- Reads database configuration from an external properties file for better security
- Simple Swing-based GUI interface for user login and navigation
- Uses SLF4J API with Simple implementation for logging
- Built and managed with Maven for dependency management and build automation

---

## Prerequisites

- Java Development Kit (JDK) version 15 or above  
  *(Project uses Java features such as Text Blocks introduced in Java 15)*
- Maven 3.6 or higher
- MySQL server running with a database named `spotify`
- MySQL Connector/J driver (managed automatically by Maven)

---

## Setup

### 1. Clone the repository

```bash
git clone [repository-url]
cd spotifyapp
```

### 2. Configure Database Credentials

Create a `config.properties` file under `src/main/resources` with the following content:

```properties
jdbc.url=jdbc:mysql://localhost:3306/spotify
jdbc.username=root
jdbc.password=your_password_here
```
Replace your_password_here with your actual database password.

---

## How to Run

Build and run the application using Maven:
```bash
mvn exec:java -Dexec.mainClass="com.kulacolin.spotifyapp.Main"
```
Maven will compile the project (if needed) and launch the application.

---

## Important Notes

- The application expects the `config.properties` file to be present in the classpath (`src/main/resources`). If the file is missing, the app will fail to start.
- Sensitive credentials such as database passwords are **not hardcoded**, but are loaded from the properties file to improve security.
- The project uses the **Java Text Blocks** feature; therefore, **Java 15 or above** is required for compiling and running the code.

---

## Project Structure

```
spotifyapp/
├── pom.xml                  # Maven build and dependencies
├── src/
│   ├── main/
│   │   ├── java/            # Java source code
│   │   └── resources/       # Contains config.properties (database config)
│   └── test/                # Unit tests
└── target/                  # Compiled classes and packaged JAR
```

---

## Dependencies

- [**HikariCP 5.0.1**](https://github.com/brettwooldridge/HikariCP) – Efficient JDBC connection pooling  
- [**MySQL Connector/J 9.1.0**](https://dev.mysql.com/downloads/connector/j/) – MySQL database driver  
- [**SLF4J API 2.0.16**](http://www.slf4j.org/) and [**slf4j-simple 2.0.16**](http://www.slf4j.org/) – Logging framework  
- [**JUnit 3.8.1**](https://junit.org/junit3/) – Unit testing framework (test scope)  

All dependencies are managed via **Maven**.

---

## Troubleshooting

### `config.properties not found` error
- Ensure `config.properties` is located inside `src/main/resources` so it is included in the classpath during runtime.

### Java feature 'Text Blocks' compilation error
- Confirm your Java compiler version is set to **15 or higher** in your IDE and Maven build.

### Database connection issues
- Verify your **MySQL server is running**, credentials in `config.properties` are correct, and the **JDBC URL** matches your setup.

---

## Future Improvements

*To be added.*

---

## Contact

For questions or support, please contact:

**Colin Kula**  
Email: *your-email@example.com*  
Phone: **224-212-0475**
