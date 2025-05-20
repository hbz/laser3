package de.laser.remote

import javax.persistence.Transient

/**
 * This is a source entry to an ElasticSearch scroll endpoint; it is currently used for we:kb data mirroring but may be extended to other sources as well
 * A source may have a record type ({@link #rectype}):
 * <ul>
 *     <li>{@link de.laser.wekb.Package}</li>
 *     <li>{@link de.laser.wekb.Platform}</li>
 *     <li>{@link de.laser.Org}</li>
 *     <li>{@link de.laser.wekb.TitleInstancePackagePlatform}</li>
 * </ul>
 * and a source type ({@link #type}):
 * <ul>
 *     <li><s>OAI</s> (deprecated)</li>
 *     <li>JSON</li>
 * </ul>
 */
class GlobalRecordSource {

    String name
    String type
    Date haveUpTo
    Long rectype
    Boolean active = false

    Date dateCreated
    Date lastUpdated

    static transients = [ 'uri' ] // mark read-only accessor methods

    static mapping = {
                   id column:'grs_id'
              version column:'grs_version'
                 name column:'grs_name', type:'text'
             haveUpTo column:'grs_have_up_to'
                 type column:'grs_type'
              rectype column:'grs_rectype'
               active column:'grs_active'

        dateCreated column: 'grs_date_created'
      lastUpdated   column: 'grs_last_updated'
    }

    static constraints = {
           name(nullable:true, blank:false, maxSize:2048)
       haveUpTo(nullable:true)
           type(nullable:true, blank:false)
    lastUpdated(nullable: true)
    }

    @Deprecated
    @Transient
    String getUri() {
        Wekb.getURL() + '/api2'
    }
}
