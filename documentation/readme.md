
## Upgrade to Grails 6.1.2

#### Updated core and dependencies

- Spring Framework 5.3.31
- Spring Boot 2.7.18
- Micronaut 3.10.3 / for Spring 4.5.1
- Hibernate 5.6.15.Final / GORM 8.0.2
- Elasticsearch 7.17.14
- Logback 1.2.12
- Gradle 7.6.3
- Groovy 3.0.11
- Java 11
- PostgreSQL 13+

See [details.md](./details.md) for detailed information.

### Setup with SDKMAN

    sdk i grails 6.1.2
    sdk i groovy 3.0.11
    sdk i java 11.0.12-open

#### Configuration files

- [build.gradle](../build.gradle)
- [gradle.properties](../gradle.properties)
- [application.yml](../grails-app/conf/application.yml)
- [application.groovy](../grails-app/conf/application.groovy)
- [logback.groovy](../grails-app/conf/logback.groovy)

#### Local configuration file

- [laser3-config.groovy.example](../files/server/laser3-config.groovy.example)

### Plugins 

- Database migration: [database-migration.md](./database-migration.md)
- PDF generation: [pdf-generation.md](./pdf-generation.md)
