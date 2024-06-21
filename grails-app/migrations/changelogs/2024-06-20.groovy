package changelogs

import de.laser.RefdataCategory
import de.laser.RefdataValue

databaseChangeLog = {

    changeSet(author: "klober (generated)", id: "1718880478502-1") {
        dropForeignKeyConstraint(baseTableName: "org", constraintName: "fk1aee448a545b3")
    }

    changeSet(author: "klober (generated)", id: "1718880478502-2") {
        dropColumn(columnName: "org_sector_rv_fk", tableName: "org")
    }

    changeSet(author: "klober (modified)", id: "1718880478502-3") {
        grailsChange {
            change {
                RefdataCategory os = RefdataCategory.findByDesc('org.sector')
                if (os) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :os', [os: os])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: os.id])
                }
            }
            rollback {}
        }
    }
}
