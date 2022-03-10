
## Database Migration

http://grails-plugins.github.io/grails-database-migration/3.0.x

#### Configuration

**build.gradle**

    buildscript {
        // ..
        dependencies {
            // ..
            classpath "org.grails.plugins:database-migration:3.0.3"
        }
    }
    
    dependencies {
        // ..
        compile 'org.grails.plugins:database-migration:3.0.3'
        compile 'org.liquibase:liquibase-core:3.5.5'
    }

    // database-migration
    sourceSets {
        main {
            resources {
                srcDir 'grails-app/migrations'
            }
        }
    }

**grails-app/conf/application.yml**
    
    environments:
        development|test|production:
            dataSource:
                dbCreate: none
        
**grails-app/conf/application.groovy**

    // database migration plugin
    grails.plugin.databasemigration.updateOnStart = true
    
#### Adding changes to changelog

    grails (prod) dbm-gorm-diff changelog-<currentDate>.groovy --add
  
