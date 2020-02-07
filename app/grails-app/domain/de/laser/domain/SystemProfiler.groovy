package de.laser.domain

import com.k_int.kbplus.Org
import de.laser.ContextService
import grails.util.Holders

import javax.persistence.Transient

class SystemProfiler {

    final static long THRESHOLD_MS = 1000

    String  uri
    String  params
    Org     context
    Integer ms

    Date dateCreated

    static mapping = {
        //table (name: 'debug_profiler')

        id          column:'sp_id'
        version     column:'sp_version'
        uri         column:'sp_uri',        index: 'sp_uri_idx'
        params      column:'sp_params',     type: 'text'
        context     column:'sp_context_fk'
        ms          column:'sp_ms'

        dateCreated column:'sp_created'
    }

    static constraints = {
        uri     (nullable:false, blank:false)
        params  (nullable:true,  blank:true)
        context (nullable:true,  blank:false)
        ms      (nullable:true,  blank:false)
    }

    // triggerd via AjaxController.notifyProfiler()

    static void update(long delta, String actionUri) {

        ContextService contextService = (ContextService) Holders.grailsApplication.mainContext.getBean('contextService')

        // store current request
        if (delta && delta > 0) {
            (new SystemProfiler(
                    uri: actionUri,
                    context: contextService?.getOrg(),
                    ms: delta
            )).save(flush: true)
        }
    }
}
