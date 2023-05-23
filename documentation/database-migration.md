
## Database Migration

- http://grails-plugins.github.io/grails-database-migration/4.2.x

### Configuration

**build.gradle**

    buildscript {
        dependencies {
            classpath "org.grails.plugins:database-migration:4.2.0"
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
        implementation 'org.grails.plugins:database-migration:4.2.0', {
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

    grails (prod) dbm-gorm-diff changelogs/<currentDate>.groovy --add
  
