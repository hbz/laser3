package changelogs

import de.laser.Org
import de.laser.RefdataValue
import de.laser.storage.RDStore

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1737626994089-1") {
        grailsChange {
            change {
                String query = "delete from reader_number where num_semester_rv_fk in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'semester' and rdv_value ilike 's%')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1737626994089-2") {
        grailsChange {
            change {
                String query = "delete from refdata_value where rdv_id in (select rdv_id from refdata_value join refdata_category on rdv_owner = rdc_id where rdc_description = 'semester' and rdv_value ilike 's%')"
                int count = sql.executeUpdate(query)
                String info = "${query}: ${count}"
                confirm(info)
                changeSet.setComments(info)
            }
            rollback {}
        }
    }

    changeSet(author: "klober (modified)", id: "1737626994089-3") {
        grailsChange {
            change {
                RefdataValue rdv = RefdataValue.getByValueAndCategory('follows', 'combo.type')
                if (rdv) {
                    sql.execute("delete from combo where combo_type_rv_fk = :id", [id: rdv.id])
                    confirm("combo(type=follows) removed: ${sql.getUpdateCount()}")
                }
            }
        }
    }

    changeSet(author: "klober (modified)", id: "1737626994089-4") {
        grailsChange {
            change {
                sql.execute("delete from refdata_value where rdv_value='follows' and rdv_owner = (select rdc_id from refdata_category where rdc_description='combo.type')")
                confirm("refdata_value removed: ${sql.getUpdateCount()}")
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1737626994089-5") {
        addColumn(tableName: "org") {
            column(name: "org_archive_date", type: "TIMESTAMP WITHOUT TIME ZONE")
        }
    }

    changeSet(author: "klober (modified)", id: "1737626994089-6") {
        grailsChange {
            change {
                int c1 = sql.executeUpdate("update org set org_archive_date = org_last_updated where org_status_rv_fk in (select rdv_id from refdata_value join refdata_category ON rdv_owner = rdc_id where rdc_description = 'org.status' and rdv_value in ('Deleted', 'Removed'))")
                int c2 = sql.executeUpdate("update org set org_archive_date = org_retirement_date where org_retirement_date is not null")

                String cc = 'set archiveDate=lastUpdated: ' + c1 + ' / set archiveDate=retirementDate: ' + c2
                confirm(cc)
                changeSet.setComments(cc)
            }
        }
    }

    changeSet(author: "klober (generated)", id: "1737626994089-7") {
        dropNotNullConstraint(columnDataType: "bigint", columnName: "org_status_rv_fk", tableName: "org")
    }
}
