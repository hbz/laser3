
## Database Migration

- http://grails-plugins.github.io/grails-database-migration/4.2.x

### Configuration

**build.gradle**

    buildscript {
        dependencies {
            classpath "org.grails.plugins:database-migration:4.2.1"
        }
    }

    sourceSets {
        main {
            resources {
                srcDir 'grails-app/migrations'
            }
        }
    }

    dependencies {
        implementation 'org.grails.plugins:database-migration:4.2.1', {
            exclude module: 'spring-boot-cli'
        }
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

  
