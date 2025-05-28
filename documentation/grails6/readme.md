
## Upgrade to Grails 6.2.3

#### Updated core and dependencies

- Spring Framework 5.3.39
- Spring Boot 2.7.18
- Micronaut 3.10.4 / for Spring 4.5.1
- Hibernate 5.6.15.Final / GORM 8.1.1
- Ehcache 2.10.9.2
- Elasticsearch 7.17.28
- Logback 1.2.12
- Gradle 8.12.1
- Groovy 3.0.23
- Java 17
- PostgreSQL 16+

See [details.md](./details.md) for detailed information.

### Setup with SDKMAN

    sdk i grails 6.2.3
    sdk i groovy 3.0.23
    sdk i java 17.0.12-oracle

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
