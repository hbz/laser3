package de.laser.system

import de.laser.utils.AppUtils

/**
 * This class lets configure some system-wide settings (e.g. MaintenanceMode, MailSending). Setting can be changed only by ROLE_YODA users
 */
class MuleCache {

    static enum CFG {
        SYSTEM_STARTUP     ('SYSTEM_STARTUP', 'dateValue', 'ALL'),
        SYSTEM_HEARTBEAT   ('SYSTEM_HEARTBEAT', 'dateValue', 'ALL')

        CFG (String id, String type, String server) {
            this.id   = id
            this.type   = type
            this.server = server
        }

        public String id
        public String type
        public String server

        static get(String id) {
            CFG.grep{ it.id == id }
        }
    }

    String  stringValue
    Date    dateValue

    CFG     cfg
    String  server

    Date dateCreated
    Date lastUpdated

    static mapping = {
             id column:'mc_id'
        version column:'mc_version'
    stringValue column:'mc_string_value', type: 'text'
      dateValue column:'mc_date_value'
            cfg column:'mc_cfg'
         server column:'mc_server'

        dateCreated column: 'mc_date_created'
        lastUpdated column: 'mc_last_updated'
    }

    static constraints = {
        stringValue (nullable:true, blank:true)
        dateValue   (nullable:true, blank:true)
    }

    static void addEntry(CFG cfg, def data) {
        String server = AppUtils.getCurrentServer()

        if (cfg && ['ALL', server].contains(cfg.server)) {
            MuleCache mc = new MuleCache(cfg: cfg, server: server)
            mc[cfg.type] = data
            mc.save()
        }
    }

    static void updateEntry(CFG cfg, def data) {
        String server = AppUtils.getCurrentServer()

        if (cfg && ['ALL', server].contains(cfg.server)) {
            MuleCache mc = findByCfgAndServer(cfg, server)
            if (mc) {
                mc[cfg.type] = data
                mc.save()
            }
            else {
                addEntry(cfg, data)
            }
        }
    }

    static MuleCache getEntry(CFG cfg, String server) {
        findByCfgAndServer(cfg, server)
    }

    static MuleCache getEntry(CFG cfg) {
        getEntry(cfg, AppUtils.getCurrentServer())
    }
}
