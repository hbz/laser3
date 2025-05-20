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
        gdc2()
        gdc3()
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

    static void gdc2() {
        log.info 'ERMS6460 - gdc2'

        List changes = []

        List<DocContext> subList = DocContext.executeQuery(
                'select dc from DocContext dc where dc.subscription != null and dc.shareConf = :sc and dc.targetOrg = null',
                [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET]
        )
        subList.each { DocContext dc ->
            if (dc.subscription.getSubscriber()) {
                dc.targetOrg = dc.subscription.getSubscriber()
                dc.save()
                changes << ['SUB', dc.id, dc.targetOrg.id]
            }
        }

        if (changes) {
            SystemEvent.createEvent('GDC_INFO', [
                    server: AppUtils.getCurrentServer(),
                    op: 'ERMS6460 - gdc2',
                    count: changes.size(),
                    changes: changes
            ])

            log.info(changes.toListString())
        }
    }

    static void gdc3() {
        log.info 'ERMS6460 - gdc3'

        List changes = []
        List<DocContext> licList = DocContext.executeQuery(
                'select dc from DocContext dc where dc.license != null and dc.shareConf = :sc and dc.targetOrg = null',
                [sc: RDStore.SHARE_CONF_UPLOADER_AND_TARGET]
        )
        licList.each { DocContext dc ->
            dc.shareConf = RDStore.SHARE_CONF_ALL
            dc.save()
            changes << ['LIC', dc.id, 'SHARE_CONF_ALL']
        }

        if (changes) {
            SystemEvent.createEvent('GDC_INFO', [
                    server: AppUtils.getCurrentServer(),
                    op: 'ERMS6460 - gdc3',
                    count: changes.size(),
                    changes: changes
            ])

            log.info(changes.toListString())
        }
    }
}
