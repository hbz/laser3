
## Upgrade to Grails 5.1.7

#### Updated core and dependencies

- Spring Framework 5.3.19
- Spring Boot 2.6.6
- Micronaut 3.3.4
- Gradle 7.4.2
- Hibernate 5.6.9.Final / GORM 7.2.1
- Groovy 3.0.7
- Java 11

See [upgrade.md](./upgrade.md) for detailed information.

### Setup

    sdk i grails 5.1.7 
    sdk i groovy 3.0.7
    sdk i java 11.0.12

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
