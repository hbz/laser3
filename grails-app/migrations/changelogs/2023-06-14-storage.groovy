package changelogs

import de.laser.finance.CostItem
import de.laser.storage.BeanStore
import de.laser.storage.RDStore

databaseChangeLog = {

    changeSet(author: "galffy (generated)", id: "1686720364944-1") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_calculated_type", type: "varchar(255)")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686720364944-2") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_end_date", type: "timestamp")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686720364944-3") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_name", type: "text")
        }
    }

    changeSet(author: "galffy (generated)", id: "1686720364944-4") {
        addColumn(tableName: "deleted_object") {
            column(name: "do_old_start_date", type: "timestamp")
        }
    }

    /*changeSet(author: "galffy (hand-coded)", id: "1686720364944-5") {
        grailsChange {
            change {
                int migrated = 0
                CostItem.withTransaction {
                    CostItem.findAllByCostItemStatus(RDStore.COST_ITEM_DELETED).each { CostItem ci ->
                        BeanStore.getDeletionService().deleteCostItem(ci)
                        migrated++
                    }
                }
                confirm("BeanStore.getDeletionService().deleteCostItem(ci): ${migrated}")
                changeSet.setComments("BeanStore.getDeletionService().deleteCostItem(ci): ${migrated}")
            }
            rollback {}
        }
    }*/
}
