package de.laser.system

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

    static boolean isTrue(CONFIG type) {
        if (type == CONFIG.ERMS_6121) {
            // do something
            // true if all conditions are met
            // false otherwise
        }
        false
    }
}
