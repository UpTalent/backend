# UpTalent
backend team repository for UpTalent project
The UpTalent app that is a professional networking platform that also allows content creators to monetize their content.

## Getting Started

To get started with the application, you will need to have JDK 17+. You can clone the repository and build the application.

### Prerequisites - Required software
* Java JDK version 17+ should be installed in the system.

Check it here https://www.oracle.com/java/technologies/downloads/#java17
or here https://adoptium.net/temurin/releases/ 

## How to Build:
To build the application execute the following commands in the project folder (where pom.xml and mvnw are located): 

```bash
./mvnw clean package # this will build the project
```
For the first time it will download and install Maven version configured in the project settings (`v.3.9.0`)
Next time the cached version will be used without redownloading.

After the build is completed, the folder `/target` will be created with a compiled `.jar` ready to be launched.

## How to Run:
Now you can launch the server at the default port `8080`
(if the option `--server.port={PORT}` is not provided):
```bash
java -jar ./target/*.jar 
```
It may take up to around 15 sec for the server to start. This will start the application and you can access the application by navigating to http://localhost:8080 in your web browser.
### Configuring the application
The application can be configured using the `application.properties` file. This file is located in the `src/main/resources` directory. Here, you can configure properties such as the server port, database settings, and logging.

### Adding new features
If you want to add new features to the application, you can do so by creating new controllers, services, and repositories. You can also add new dependencies to the `pom.xml` file.

