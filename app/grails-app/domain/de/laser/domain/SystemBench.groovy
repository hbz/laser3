package de.laser.domain

import com.k_int.kbplus.Org

class SystemBench {

    final static THRESHOLD_MS = 1500;

    String  uri
    String  params
    Integer ms
    Org     context

    Date dateCreated

    static mapping = {
        id          column:'sb_id'
        version     column:'sb_version'
        uri         column:'sb_uri',    index: 'sb_uri_idx'
        params      column:'sb_params', type: 'text'
        ms          column:'sb_ms'
        context     column:'sb_context_fk'
        dateCreated column:'sb_created'
    }

    static constraints = {
        uri     (nullable:false, blank:false)
        params  (nullable:true,  blank:true)
        ms      (nullable:false, blank:false)
        context (nullable:true,  blank:false)
    }
}
