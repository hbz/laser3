package com.k_int.kbplus

import de.laser.exceptions.CreationException

import javax.persistence.Transient

class Links {


    @Transient
    def contextService
    @Transient
    def springSecurityService
    @Transient
    def genericOIDService

    Long id
    String source
    String destination
    RefdataValue linkType
    Org     owner
    Date    dateCreated
    Date    lastUpdated

    static mapping = {
        id          column: 'l_id'
        source      column: 'l_source_fk',      index: 'l_source_idx'
        destination column: 'l_destination_fk', index: 'l_dest_idx'
        //objectType  column: 'l_object'
        linkType    column: 'l_link_type_rv_fk'
        owner       column: 'l_owner_fk'
        autoTimestamp true

        dateCreated column: 'l_date_created'
    }

    static constraints = {
        source        (nullable: false, blank: false)
        destination   (nullable: false, blank: false)
        //objectType    (nullable: false, blank: false)
        linkType      (nullable: false, blank: false)
        owner         (nullable: false, blank: false)

        // Nullable is true, because values are already in the database
        dateCreated (nullable: true, blank: false)

    }

    static Links construct(Map<String, Object> configMap) throws CreationException {
        Links links = new Links(source:configMap.source,destination:configMap.destination,owner:configMap.owner,linkType:configMap.linkType)
        if(links.save())
            links
        else {
            throw new CreationException(links.errors)
        }
    }

    def getOther(key) {
        def context
        if(key instanceof Subscription || key instanceof License)
            context = GenericOIDService.getOID(key)
        else if(key instanceof GString || key instanceof String)
            context = key
        else {
            log.error("No context key!")
            return null
        }

        if(context == source)
            return genericOIDService.resolveOID(destination)
        else if(context == destination)
            return genericOIDService.resolveOID(source)
        else return null
    }
}
