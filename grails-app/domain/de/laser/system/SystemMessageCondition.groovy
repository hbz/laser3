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
                Calendar now = GregorianCalendar.getInstance(), timeCheck = GregorianCalendar.getInstance()
                ReaderNumber check = ReaderNumber.findByOrgAndSemesterIsNotNull(BeanStore.getContextService().getOrg(), [sort: 'semester', order: 'desc'])
                if(check) {
                    timeCheck.setTime(check.lastUpdated)
                    result = now.get(Calendar.YEAR) == timeCheck.get(Calendar.YEAR)
                }
                else {
                    check = ReaderNumber.findByOrgAndYearIsNotNull(BeanStore.getContextService().getOrg(), [sort: 'year', order: 'desc'])
                    if(check) {
                        timeCheck.setTime(check.lastUpdated)
                        result = now.get(Calendar.YEAR) == timeCheck.get(Calendar.YEAR)
                    }
                }
            }
            else {
                result = true
            }
        }

        result
    }
}
