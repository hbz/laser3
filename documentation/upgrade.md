
## Upgrade from Grails 2.5.6 to 3.3.11

#### Updated Dependencies

- Hibernate 5.1.5 (4 supported)
- Spring Framework 4.3.9
- Spring Boot 1.5.4
- Gradle 3.5
- Spock 1.1
- GORM 6.1

#### Important Files

- build.gradle (info.app.version)
- gradle.properties (info.app.grailsVersion)
- settings.gradle (info.app.name)
- conf/application.yml
- conf/application.groovy

### Setup

    sdk i grails 3.3.11  
    sdk i groovy 2.4.17
    
- Java OpenJDK 1.8+
- PostgreSQL 11/12
- Elasticsearch 7.5.0
- Apache Tomcat 9.0.0

#### Migration

    git remote add grails2 https://github.com/hbz/laser
    git remote set-url --push grails2 DISABLED
    git fetch grails2 grails-upgrade-source:grails2
    
    git checkout -b migration
    
    git merge -s ours --no-commit --allow-unrelated-histories grails2/grails-upgrade-source
    git read-tree --prefix=grails2/ -u grails2/grails-upgrade-source
    git commit -m '--- imported laser (grails2) ---'

### Docs

- https://grails-plugins.github.io/grails-spring-security-core/3.2.x/
- https://docs.gradle.org/3.5/userguide/userguide.html
- https://robertoschwald.github.io/grails-audit-logging-plugin/3.0.x/plugin.html
- http://logback.qos.ch/
- http://www.asset-pipeline.com/manual/
