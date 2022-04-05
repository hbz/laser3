databaseChangeLog = {

    changeSet(author: "agalffy (modified)", id: "1616051304447-1") {
        grailsChange{
            change {
                sql.execute("update identifier_namespace set idns_name_de = 'EZB Kollektions_ID' where idns_ns = 'ezb_collection_id'")
            }
            rollback {

            }
        }
    }

    changeSet(author: "agalffy (generated)", id: "1616051304447-2") {
        addColumn(tableName: "title_instance_package_platform") {
            column(name: "tipp_publisher_name", type: "text")
        }
    }

}
