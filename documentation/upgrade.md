
## Upgrade from Grails 3.3.11 to 4.0.13

#### Updated core and dependencies

- Spring Framework 5.1.20
- Spring Boot 2.1.8
- Gradle 5.6.4
- Hibernate 5.4.33.Final / GORM 7.0.8.RELEASE
- Groovy 2.5.14
- Java 11

See [build.gradle](../build.gradle) for details on all plugins

### Setup

    sdk i grails 4.0.13 
    sdk i groovy 2.5.14
    sdk i java 11.0.12

#### Configuration files

- [build.gradle](../build.gradle)
- [gradle.properties](../gradle.properties)
- [application.yml](../grails-app/conf/application.yml)
- [application.groovy](../grails-app/conf/application.groovy)

#### Local configuration file

- [laser3-config.groovy.example](../files/server/laser3-config.groovy.example)

### Plugins

#### Database migration

- [database-migration.md](./database-migration.md)

#### PDF generation

- [pdf-generation.md](./pdf-generation.md)