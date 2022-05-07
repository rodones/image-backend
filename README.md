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

## Database

### Naming Convention

- Table:
    - Model: PascalCase (singular) (CompanyUser)
    - SQL:  snake_case (plural) (example: company_users)
- Column:
    - Model: camelCase (refreshToken)
    - SQL:  snake_case (example: refresh_token)




