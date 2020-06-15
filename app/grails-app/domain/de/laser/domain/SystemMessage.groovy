package de.laser.domain

class SystemMessage {

    static final String TYPE_OVERLAY        = "TYPE_OVERLAY"
    static final String TYPE_STARTPAGE_NEWS = "TYPE_STARTPAGE_NEWS"

    String content // text
    String type
    boolean isActive = false // showNow = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
        id column: 'sm_id'
        version column: 'sm_version'

        content     column: 'sm_content'
        type        column: 'sm_type'
        isActive    column: 'sm_is_active'
        dateCreated column: 'sm_date_created'
        lastUpdated column: 'sm_last_updated'
    }

    static constraints = {
        content (nullable:true,  blank:true)
        type    (nullable:false, blank:false)
        isActive(nullable:false, blank:false)
    }

    static getTypes() {
        [TYPE_OVERLAY, TYPE_STARTPAGE_NEWS]
    }
}
