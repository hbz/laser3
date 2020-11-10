package de.laser.system

import de.laser.Org
import de.laser.ContextService
import grails.util.Holders

class SystemProfiler {

    final static long THRESHOLD_MS = 1000

    String  uri
    String  params
    Org     context
    Integer ms
    String  archive

    Date dateCreated

    static mapping = {
        id          column:'sp_id'
        version     false
        uri         column:'sp_uri',        index: 'sp_uri_idx'
        params      column:'sp_params',     type: 'text'
        context     column:'sp_context_fk'
        ms          column:'sp_ms'
        archive     column:'sp_archive'

        dateCreated column:'sp_created'
    }

    static constraints = {
        uri     (blank:false)
        params  (nullable:true, blank:true)
        context (nullable:true)
        ms      (nullable:true)
        archive (nullable:true, blank:false)
    }

    static String getCurrentArchive() {
        String av = Holders.grailsApplication.metadata['info.app.version'] ?: 'unkown'
        List<String> avList = av.split("\\.")
        if (avList.size() >= 2) {
            return "${avList[0]}.${avList[1]}"
        }

        return av
    }

    // triggerd via AjaxController.notifyProfiler()

    static void update(long delta, String actionUri) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        withTransaction {

            // store current request
            if (delta && delta > 0) {
                (new SystemProfiler(
                        uri: actionUri,
                        context: contextService?.getOrg(),
                        ms: delta,
                        archive: SystemProfiler.getCurrentArchive()
                )).save()
            }
        }
    }
}
