databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1644839560040-1") {
        addUniqueConstraint(columnNames: "sp_pkg_fk, sp_sub_fk", constraintName: "sub_package_unique", tableName: "subscription_package")
    }

}
