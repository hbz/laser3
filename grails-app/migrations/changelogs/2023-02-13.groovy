package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1676287970555-1") {
        grailsChange {
            change {
                sql.execute('update address set adr_prs_fk = null, adr_org_fk = pr_org_fk from person_role where adr_prs_fk is not null and adr_prs_fk = pr_prs_fk;')
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (generated)", id: "1676287970555-2") {
        dropForeignKeyConstraint(baseTableName: "address", constraintName: "fkbb979bf4b01cf0b5")
    }

    changeSet(author: "galffy (generated)", id: "1676287970555-3") {
        dropColumn(columnName: "adr_prs_fk", tableName: "address")
    }

}
