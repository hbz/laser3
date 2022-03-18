
## Database Migration

- http://grails-plugins.github.io/grails-database-migration/4.0.x

### Configuration

**build.gradle**

    buildscript {
        dependencies {
            classpath "org.grails.plugins:database-migration:4.0.0"
        }
    }
    
    dependencies {
        compile 'org.grails.plugins:database-migration:4.0.0'
        compile 'org.liquibase:liquibase-core:4.6.2'
    }

    sourceSets {
        main {
            resources {
                srcDir 'grails-app/migrations'
            }
        }
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

    grails (prod) dbm-gorm-diff changelog-<currentDate>.groovy --add
  
