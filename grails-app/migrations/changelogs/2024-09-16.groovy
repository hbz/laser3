package changelogs

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1726483138229-1") {
        grailsChange {
            change {
                String query = "delete from address_type where address_id in (select adr_id from address where adr_tenant_fk is null and (adr_provider_fk is not null or adr_vendor_fk is not null))"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1726483138229-2") {
        grailsChange {
            change {
                String query = "delete from address where adr_tenant_fk is null and (adr_provider_fk is not null or adr_vendor_fk is not null)"
                sql.execute(query)
                String info = "${query}: ${sql.getUpdateCount()}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }
}
