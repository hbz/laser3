package changelogs

import de.laser.CustomerTypeService
import de.laser.Org
import de.laser.OrgSetting
import de.laser.api.v0.ApiToolkit

databaseChangeLog = {

    changeSet(author: "klober (modified)", id: "1682590379406-1") {
        grailsChange {
            change {
                List<Org> orgList = Org.executeQuery(
                        "select o from OrgSetting os join os.org o where os.key = 'CUSTOMER_TYPE' and os.roleValue.authority in (:proCtList) order by o.sortname, o.name",
                                    [proCtList: [CustomerTypeService.ORG_INST_PRO, CustomerTypeService.ORG_CONSORTIUM_PRO]]
                )
                orgList.each { Org o ->
                    if (OrgSetting.get(o, OrgSetting.KEYS.API_LEVEL) == OrgSetting.SETTING_NOT_FOUND) {
                        ApiToolkit.setApiLevel(o, ApiToolkit.API_LEVEL_READ)
                    }
                }
            }
            rollback {}
        }
    }
}
