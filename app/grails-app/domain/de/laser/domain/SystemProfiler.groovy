package de.laser.domain

import com.k_int.kbplus.Org

class SystemProfiler {

    final static THRESHOLD_MS = 1000;

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
}
