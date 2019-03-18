package com.k_int.kbplus

class SurveyProperty {

    String name
    String type

    RefdataCategory category
    Org owner

    Date dateCreated
    Date lastUpdated

    static constraints = {
    }

    static mapping = {
        id column: 'surPro_id'
        version column: 'surPro_version'

        name column: 'surPro_name'
        type column: 'surPro_type', type: 'text'

        category column: 'surPro_refdataCa_fk'
        owner column: 'surPro_org_fk'

        dateCreated column: 'surPro_dateCreated'
        lastUpdated column: 'surPro_lastUpdated'
    }
}
