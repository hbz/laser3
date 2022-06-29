
## Upgrade to Grails 5.2.0

#### Updated core and dependencies

- Spring Framework 5.3.20
- Spring Boot 2.7.0
- Micronaut 3.5.1
- Gradle 7.4.2
- Hibernate 5.6.9.Final / GORM 7.3.2
- Groovy 3.0.11
- Java 11

See [details.md](./details.md) for detailed information.

### Setup with SDKMAN

    sdk i grails 5.2.0
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
