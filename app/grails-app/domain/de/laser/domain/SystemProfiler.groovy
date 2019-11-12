package de.laser.domain

import com.k_int.kbplus.Org
import de.laser.ContextService
import grails.util.Holders

import javax.persistence.Transient

class SystemProfiler {

    final static long THRESHOLD_MS = 1000

    String  uri
    String  params
    Integer ms
    Org     context

    Date dateCreated

    static mapping = {
        //table (name: 'debug_profiler')

        id          column:'sp_id'
        version     column:'sp_version'
        uri         column:'sp_uri',        index: 'sp_uri_idx'
        params      column:'sp_params',     type: 'text'
        ms          column:'sp_ms'
        context     column:'sp_context_fk'
        dateCreated column:'sp_created'
    }

    static constraints = {
        uri     (nullable:false, blank:false)
        params  (nullable:true,  blank:true)
        ms      (nullable:false, blank:false)
        context (nullable:true,  blank:false)
    }

    // triggerd via AjaxController.notifyProfiler()

    static void update(long delta, String actionUri) {

        //println "updateSystemProfiler() delta: ${delta}, actionUri: ${actionUri}"
        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        if (delta >= SystemProfiler.THRESHOLD_MS) {
            (new SystemProfiler(
                    uri:      actionUri,
                    ms:       delta,
                    context:  contextService?.getOrg()
            )).save(flush: true)
        }

        // update global counts
        SystemProfiler.withTransaction { status ->
            SystemProfiler global = SystemProfiler.findWhere(uri: actionUri, context: null, params: null)

            if (! global) {
                global = new SystemProfiler(uri: actionUri, ms: 1)
                global.save(flush:true)
            }
            else {
                SystemProfiler.executeUpdate('UPDATE SystemProfiler SET ms =:newValue WHERE id =:spId',
                        [newValue: Integer.valueOf(global.ms) + 1, spId: global.id])
            }
        }
    }
}
