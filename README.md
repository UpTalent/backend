# UpTalent
backend team repository for UpTalent project
The UpTalent app that is a professional networking platform that also allows content creators to monetize their content.

## Getting Started

To get started with the application, you will need to have Java and Maven installed on your system. Once you have these installed, you can clone the repository and build the application using Maven.

### Prerequisites - Required software
* JDK 17 or higher
* Spring framework 6/Spring boot 3
* Maven 3.1.1 or higher

### Installation

- Clone this repository to your local machine using:

```bash
  git clone https://github.com/Team-Alexander/backend.git
```

- Go to the project directory:

```bash
  cd backend
```

- Install all dependencies, use Maven to build the server:

```bash
  mvn clean install
```

### Running the application
To run the application, you can use the Maven Spring Boot plugin. Run the following command from the root directory of the application:
In the project directory, you can run:

```bash
  mvn spring-boot:run
```

This will start the application on the default port (8080) and you can access the application by navigating to http://localhost:8080 in your web browser. 

### Configuring the application
The application can be configured using the `application.properties` file. This file is located in the `src/main/resources` directory. Here, you can configure properties such as the server port, database settings, and logging.

### Adding new features
If you want to add new features to the application, you can do so by creating new controllers, services, and repositories. You can also add new dependencies to the `pom.xml` file.

### Testing the application
To test the application, you can run the tests using Maven:

``` bash
  mvn test
```

This will run all the tests in the `src/test` directory. You can add your own tests here to ensure that your application is working correctly
