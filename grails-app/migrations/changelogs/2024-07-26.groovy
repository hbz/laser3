package changelogs

import de.laser.RefdataCategory
import de.laser.RefdataValue

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1721974751152-1") {
        grailsChange {
            change {
                RefdataCategory wct = RefdataCategory.findByDesc('workflow.condition.type')
                if (wct) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: wct])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: wct.id])
                }

                RefdataCategory wcs = RefdataCategory.findByDesc('workflow.condition.status')
                if (wcs) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: wcs])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: wcs.id])
                }

                RefdataCategory wws = RefdataCategory.findByDesc('workflow.workflow.state')
                if (wws) {
                    RefdataValue.executeUpdate('delete from RefdataValue where owner = :obj', [obj: wws])
                    RefdataCategory.executeUpdate('delete from RefdataCategory where id = :id', [id: wws.id])
                }
            }
            rollback {}
        }
    }
}
