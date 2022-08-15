package de.laser.remote

/**
 * Represents an external Elasticsearch-API-Source. Currently in use to retain we:kb API connections which are not used for bulk operations;
 * the title synchronisation is being done via {@link GlobalRecordSource}s
 */
class ApiSource {

    /**
     * List all possible API types here
     */
    enum ApiTyp
    {
        /**
         * The we:kb is a fork of the GOKb
         */
        GOKBAPI
    }

    String identifier
    String name
    String baseUrl
    String editUrl
    String fixToken
    String variableToken
    ApiTyp typ

    String principal
    String credentials

    String apikey
    String apisecret

    Boolean active = false
    Date lastUpdatedwithApi

    Date dateCreated
    Date lastUpdated

    static constraints = {
        identifier(nullable:true, blank:false)
        name(nullable:true, blank:false, maxSize:2048)
        baseUrl(nullable:true, blank:false)
        editUrl(nullable:true, blank:false)
        lastUpdatedwithApi(nullable:true)
        fixToken(nullable:true, blank:false)
        variableToken(nullable:true, blank:false)
        typ         (nullable:true)
        principal(nullable:true, blank:false)
        credentials(nullable:true, blank:false)
        apikey      (nullable:true, blank:false)
        apisecret   (nullable:true, blank:false)
    }

    static mapping = {
        id  column:'as_id'
        version column:'as_version'
        identifier column:'as_identifier'
        name column:'as_name', type:'text'
        lastUpdatedwithApi column:'as_last_updated_with_api'
        fixToken column:'as_fix_token'
        variableToken column:'as_variable_token'
        typ column:'as_typ'
        principal column:'as_principal'
        credentials column:'as_creds'
        active column:'as_active'
        baseUrl column: 'as_base_url'
        editUrl column: 'as_edit_url'
        apikey column: 'as_apikey'
        apisecret column: 'as_apisecret'

        dateCreated column: 'as_date_created'
        lastUpdated column: 'as_last_updated'
    }
}
