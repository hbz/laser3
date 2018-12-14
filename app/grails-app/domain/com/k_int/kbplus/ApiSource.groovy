package com.k_int.kbplus

class ApiSource {

    enum ApiTyp
    {
        GOKBAPI
    }

    String identifier
    String name
    String baseUrl
    String fixToken
    String variableToken
    ApiTyp typ

    String principal
    String credentials

    String apikey
    String apisecret

    Boolean active
    Date lastUpdatedwithApi

    Date dateCreated
    Date lastUpdated

    static constraints = {
        identifier(nullable:true, blank:false)
        name(nullable:true, blank:false, maxSize:2048)
        baseUrl(nullable:true, blank:false)
        lastUpdatedwithApi(nullable:true, blank:false)
        fixToken(nullable:true, blank:false)
        variableToken(nullable:true, blank:false)
        typ(nullable:true, blank:false)
        principal(nullable:true, blank:false)
        credentials(nullable:true, blank:false)
        active(nullable:true, blank:false)
        apikey (nullable:true, blank:false)
        apisecret (nullable:true, blank:false)
    }

    static mapping = {
        id  column:'as_id'
        version column:'as_version'

        identifier column:'as_identifier'
        name column:'as_name', type:'text'
        lastUpdatedwithApi column:'as_lastUpdated_with_Api'
        fixToken column:'as_fixToken'
        variableToken column:'as_variableToken'
        typ column:'as_typ'
        principal column:'as_principal'
        credentials column:'as_creds'
        active column:'as_active'
        baseUrl column: 'as_baseUrl'
        apikey column: 'as_apikey'
        apisecret column: 'as_apisecret'

        dateCreated column: 'as_dateCreated'
        lastUpdated column: 'as_lastUpdated'

    }
}
