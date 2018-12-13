
# PostgreSQL Migration

- copy current MySQL database
- use dataCleanupMySQL.sql
- use exportFilesMySQL.sql
- create PostgreSQL database (set search_path to public)
- init database tables (starting app)
- migrate data with pgLoader using migrate2postgresql.load
- use dataCleanupPostgreSQL.sql
- init database migration


    //grails prod dbm-create-changelog --defaultSchema=public
    grails prod dbm-changelog-sync --defaultSchema=public
    grails prod dbm-gorm-diff --defaultSchema=public --add changelog-0.groovy

### TODO

#### /usage/index
FEHLER: Spalte »statstripl0_.id« muss in der GROUP-BY-Klausel erscheinen oder in einer Aggregatfunktion verwendet werden

#### /myInstitution/processEmptySubscription
java.util.ConcurrentModificationException
 
#### /issueEntitlement/show

@FACT