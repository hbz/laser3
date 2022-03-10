
# Database Migrations

http://grails-plugins.github.io/grails-database-migration/1.4.0

### configuration

**DataSource.groovy**
    
    # deactivate grails/gorm schema update
    production {
        dataSource {
            dbCreate = "none"
        }
    }
        
**config.groovy**

    # activate automatic database migration
    grails.plugin.databasemigration.updateOnStart = false
    grails.plugin.databasemigration.updateOnStartFileNames = [ 'changelog.groovy' ]

#### create initial changelog

    cd /app
    grails (prod) dbm-create-changelog
    grails (prod) dbm-generate-changelog --add changelog-0.groovy
    grails (prod) dbm-changelog-sync
    
#### add changes to changelog

    grails (prod) dbm-gorm-diff --add changelog-x.groovy

#### apply changes

    # automatic (if updateOnStart = true)
    grails run-app
    # manually
    grails (prod) dbm-update
     
