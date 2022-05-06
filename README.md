# image-backend

It is written in Java 11 using Spring Boot framework. It is deployed and published with AWS Elastic Beanstalk.

## Links
  - API Documentation: [Swagger](https://imba.eu-central-1.elasticbeanstalk.com/documentation)

<hr />

## Setup

### Environment 

- **IDE:** [IntelliJ Idea](https://www.jetbrains.com/idea/)
- **Plugins:**
    - [Save Actions](https://plugins.jetbrains.com/plugin/7642-save-actions)
    - [Rainbow Brackets](https://plugins.jetbrains.com/plugin/10080-rainbow-brackets)
- **Java SDK:** Amazon Corretto 11
- **Build tool:** Apache Maven
- **Database:** MySql
- **Migration Tool:** Flyway
- **Important Libraries:**
    - Spring Boot
    - Hibernate ORM
    - LogBack
- **Recommendations:**
    - [DBeaver (Database Viewer)](https://dbeaver.io/)
    - Make your Intellij Idea connected to database.

### Requirements

- Amazon Corretto 11
- MySql Database
- S3 Space

### Instructions

- Clone repository.
- Open project with Intellij Idea and let it install maven dependencies.

### Configuration and Profiles

#### Application Configuration File

*application.yml* is the only configuration file for application. All common configurations must be declared here.

The newly added configurations must be specified
in `src/main/resources/META-INF/additional-spring-configuration-metadata.json`.

#### Profiles

`dev` is for development/staging branch of the application and `prod` is master branch of the application.

There are two primary profiles named `dev` and `prod`. Profile specific variables can be found in folder named after
profile name in _src/main/resources/filters/_. These are called as maven filters. Maven replaces their occurrences in
all resources with their values.

The secondary profile is `local`. It must be activated during development on your computer.
**If you don't activate it, the application assumes it is running on server.**

## Messages, Templates, Static files etc.

### Messages

All resource bundles located in _src/main/resources/messages_ folder. Try to follow

## Database

### Naming Convention

- Table:
    - Model: PascalCase (singular) (CompanyUser)
    - SQL:  snake_case (plural) (example: company_users)
- Column:
    - Model: camelCase (refreshToken)
    - SQL:  snake_case (example: refresh_token)




