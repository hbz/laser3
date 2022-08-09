databaseChangeLog = {

    changeSet(author: "galffy (modified)", id: "changelog-stats") {}

    include file: 'changelogs/2022-08-08.groovy' // move to at least August 8th because of indexes which have to be created manually due to datasource discrepancy
}
