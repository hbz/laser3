
## Upgrade to Grails 5.2.5

#### Updated core and dependencies

- Spring Framework 5.3.23
- Spring Boot 2.7.5
- Micronaut 3.5.4 / for Spring 4.2.2
- Hibernate 5.6.14.Final / GORM 7.3.2
- Logback 1.2.11
- Gradle 7.5.1
- Groovy 3.0.11
- Java 11
- PostgreSQL 12+
- Elasticsearch 7.16.3

See [details.md](./details.md) for detailed information.

### Setup with SDKMAN

    sdk i grails 5.2.5
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
