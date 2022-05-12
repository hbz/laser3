
## Upgrade to Grails 5.1.3

#### Updated core and dependencies

- Spring Framework 5.3.16
- Spring Boot 2.6.4
- Micronaut 3.2.7
- Gradle 7.4.2
- Hibernate 5.6.8.Final / GORM 7.2.1
- Groovy 3.0.7
- Java 11

See [upgrade.md](./upgrade.md) for detailed information.

### Setup

    sdk i grails 5.1.3 
    sdk i groovy 3.0.7
    sdk i java 11.0.12

#### Configuration files

- [build.gradle](../build.gradle)
- [gradle.properties](../gradle.properties)
- [application.yml](../grails-app/conf/application.yml)
- [application.groovy](../grails-app/conf/application.groovy)

#### Local configuration file

- [laser3-config.groovy.example](../files/server/laser3-config.groovy.example)

### Plugins 

- Database migration: [database-migration.md](./database-migration.md)
- PDF generation: [pdf-generation.md](./pdf-generation.md)
