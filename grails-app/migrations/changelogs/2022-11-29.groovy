package changelogs

import de.laser.BootStrapService
import grails.util.Holders

BootStrapService bootStrapService = Holders.grailsApplication.mainContext.getBean('bootStrapService')

databaseChangeLog = {

    changeSet(author: "galffy (hand-coded)", id: "1669710461273-1") {
        grailsChange {
            change {
                //needed because of a new property definition
                bootStrapService.setupPropertyDefinitions()
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669710461273-2") {
        grailsChange {
            change {
                sql.execute("update subscription_property set sp_type_fk = (select pd_id from property_definition where pd_name = 'Due date for title selection') where sp_type_fk = (select pd_id from property_definition where pd_name = 'adc8ce37-8954-4787-91d5-004db2847476')")
            }
            rollback {}
        }
    }

    changeSet(author: "galffy (hand-coded)", id: "1669710461273-3") {
        grailsChange {
            change {
                sql.execute("delete from property_definition where pd_name = 'adc8ce37-8954-4787-91d5-004db2847476'")
            }
            rollback {}
        }
    }

}
