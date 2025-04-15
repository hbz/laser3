package de.laser.gdc

import de.laser.DocContext
import de.laser.RefdataValue
import de.laser.storage.RDStore
import de.laser.system.SystemEvent
import de.laser.utils.AppUtils
import groovy.util.logging.Slf4j

@Slf4j
class ERMS6460 {

    static void go() {
        gdc1()
    }

    static void gdc1() {
        log.info 'ERMS6460 - gdc1'

        List changes = []
        RefdataValue sc_up      = RDStore.SHARE_CONF_UPLOADER_ORG
        RefdataValue sc_target  = RDStore.SHARE_CONF_UPLOADER_AND_TARGET

        List<DocContext> provList = DocContext.executeQuery(
                'select dc from DocContext dc where dc.provider != null and dc.shareConf in :scList',
                [scList: [sc_up, sc_target]]
        )
        provList.each { DocContext dc ->
            dc.targetOrg = null
            dc.shareConf = null
            dc.save()
            changes << ['PROV', dc.id]
        }

        List<DocContext> venList = DocContext.executeQuery(
                'select dc from DocContext dc where dc.vendor != null and dc.shareConf in :scList',
                [scList: [sc_up, sc_target]]
        )
        venList.each { DocContext dc ->
            dc.targetOrg = null
            dc.shareConf = null
            dc.save()
            changes << ['VEN', dc.id]
        }

        if (changes) {
            SystemEvent.createEvent('GDC_INFO', [
                    server: AppUtils.getCurrentServer(),
                    op: 'ERMS6460 - gdc1',
                    count: changes.size(),
                    changes: changes
            ])

            log.info(changes.toListString())
        }
    }
}
