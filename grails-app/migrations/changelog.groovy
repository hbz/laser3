databaseChangeLog = {

    changeSet(author: "kloberd (generated)", id: "laser") {}

    include file: 'changelogs/2025-05-06.groovy'
    include file: 'changelogs/2025-05-08.groovy'
    include file: 'changelogs/2025-05-09.groovy'
    include file: 'changelogs/2025-06-13.groovy'
    include file: 'changelogs/2025-06-24.groovy'
    include file: 'changelogs/2025-07-01.groovy'
    include file: 'changelogs/2025-07-11.groovy'
    include file: 'changelogs/2025-06-12.groovy' // moved here - due to domain class changes @ 2025-07-11.groovy
    include file: 'changelogs/2025-07-22.groovy'
    include file: 'changelogs/2025-07-31.groovy'
}
