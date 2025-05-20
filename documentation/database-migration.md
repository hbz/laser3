
## Database Migration

- https://github.com/grails/grails-database-migration
- https://grails.github.io/grails-database-migration/5.0.x/index.html

### Configuration

**build.gradle**

    buildscript {
        dependencies {
            classpath 'org.grails.plugins:database-migration:5.0.0'
            classpath 'org.grails:grails-shell:6.1.2'    
        }
    }

    sourceSets {
        main {
            resources {
                srcDir 'grails-app/migrations'
            }
        }
    }

    configurations {
        developmentOnly
        runtimeClasspath {
            exclude group: 'org.grails', module: 'grails-shell'
        }
    }

    dependencies {
        implementation 'org.grails.plugins:database-migration:5.0.0', {
            exclude module: 'spring-boot-cli'
        }
        implementation 'org.grails:grails-shell:6.1.2'
        implementation 'org.liquibase:liquibase-core:4.19.0'
    }

**grails-app/conf/application.yml**

    grails:
        plugin:
            databasemigration:
                updateOnStart: true

    environments:
        development|test|production:
            dataSource:
                dbCreate: none
    
### Usage
#### Adding changes to changelog

    ./gradlew dbmGormDiff -Pargs='changelogs/<currentDate>.groovy --add' -Dgrails.env=prod

  
