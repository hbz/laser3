package de.laser.system

import de.laser.ReaderNumber
import de.laser.storage.BeanStore

import java.time.Year

class SystemMessageCondition {

    static enum CONFIG {
        ERMS_6121   ('ERMS_6121', 'Nutzerzahlen überprüfen', SystemMessage.TYPE_DASHBOARD)

        CONFIG (String key, String description, String systemMessageType) {
            this.key = key
            this.description = description
            this.systemMessageType = systemMessageType
        }
        public String key
        public String description
        public String systemMessageType
    }

    static mapWith = 'none'

    static boolean isDone(CONFIG type) {
        boolean result = false

        if (type == CONFIG.ERMS_6121) {
            if (BeanStore.getContextService().getOrg().isCustomerType_Inst() && BeanStore.getContextService().isInstEditor()) {
                //check if there are reader numbers from current year
                int now = Year.now().value
                Set<ReaderNumber> check = ReaderNumber.executeQuery('select rn from ReaderNumber rn where rn.org = :contextOrg and year(rn.lastUpdated) = :currYear', [contextOrg: BeanStore.getContextService().getOrg(), currYear: now])
                result = check.size() > 0
            }
            else {
                result = true
            }
        }

        result
    }
}
