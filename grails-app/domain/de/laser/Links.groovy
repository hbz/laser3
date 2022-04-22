package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.exceptions.CreationException
import de.laser.storage.BeanStorage
import de.laser.storage.RDConstants
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This class represents links between subscriptions and licenses.
 * It is similar to {@link Combo} but with different functionality. The types of links see {@link RDConstants#LINK_TYPE}
 */
class Links {

    Long id
    Subscription sourceSubscription
    Subscription destinationSubscription
    License sourceLicense
    License destinationLicense
    @RefdataInfo(cat = RDConstants.LINK_TYPE)
    RefdataValue linkType
    Org     owner
    Date    dateCreated
    Date    lastUpdated

    static hasOne = [
        document: DocContext
    ]

    static mapping = {
        id                      column: 'l_id'
        sourceSubscription      column: 'l_source_sub_fk', index: 'l_source_sub_idx'
        destinationSubscription column: 'l_dest_sub_fk', index: 'l_dest_sub_idx'
        sourceLicense           column: 'l_source_lic_fk', index: 'l_source_lic_idx'
        destinationLicense      column: 'l_dest_lic_fk', index: 'l_dest_lic_idx'
        linkType         column: 'l_link_type_rv_fk'
        owner            column: 'l_owner_fk'
        dateCreated      column: 'l_date_created'
        autoTimestamp true
    }

    static constraints = {
        sourceSubscription      (nullable: true)
        destinationSubscription (nullable: true)
        sourceLicense           (nullable: true)
        destinationLicense      (nullable: true)
        document                (nullable: true)
        // Nullable is true, because values are already in the database
        dateCreated             (nullable: true)
    }

    /**
     * Constructor for a Subscription/License linking. Parameters are specified in a {@link Map}.
     * @param configMap contains the parameters. The source and destination are expected on source and destination; the determination of object type is done in the setter {@link #setSourceAndDestination setSourceAndDestination()}
     * @return the persisted linking object
     * @throws CreationException
     */
    static Links construct(Map<String, Object> configMap) throws CreationException {
        Links links = new Links(owner: configMap.owner, linkType: configMap.linkType)
        links.setSourceAndDestination(configMap.source,configMap.destination)
        if (links.save())
            links
        else {
            throw new CreationException(links.errors)
        }
    }

    /**
     * Sets the two ends of this link. A link points from source to destination; the perspective taken is that from the source towards the destination.
     * Connectable objects are {@link License} and {@link Subscription}
     * @param source the starting point; from where we look
     * @param destination the ending point; to where we look
     */
    void setSourceAndDestination(source, destination) {
        if(source instanceof Subscription)
            sourceSubscription = source
        else if(source instanceof License)
            sourceLicense = source
        if(destination instanceof Subscription)
            destinationSubscription = destination
        else if(destination instanceof License)
            destinationLicense = destination
    }

    /**
     * Determines the pair of the object in this link
     * @param key the object whose pair should be retrieved - may be a {@link License}, a {@link Subscription} or a license/subscription OID
     * @return the link pair of the given object
     */
    def getOther(key) {
        def context
        if(key instanceof Subscription || key instanceof License) {
            context = key
        }
        else if(key instanceof GString || key instanceof String) {
            context = BeanStorage.getGenericOIDService().resolveOID(key)
        }
        else {
            log.error("No context key!")
            return null
        }

        if(context) {
            if(context.id in [sourceSubscription?.id,sourceLicense?.id]) {
                determineDestination()
            }
            else if(context.id in [destinationSubscription?.id,destinationLicense?.id]) {
                determineSource()
            }
        }
        else null
    }

    /**
     * Gets the source of this link
     * @return the source {@link Subscription} or {@link License}
     */
    def determineSource() {
        if(sourceSubscription)
            (Subscription) GrailsHibernateUtil.unwrapIfProxy(sourceSubscription)
        else if(sourceLicense)
            (License) GrailsHibernateUtil.unwrapIfProxy(sourceLicense)
    }

    /**
     * Gets the destination of this link
     * @return the destination {@link Subscription} or {@link License}
     */
    def determineDestination() {
        if(destinationSubscription)
            (Subscription) GrailsHibernateUtil.unwrapIfProxy(destinationSubscription)
        else if(destinationLicense)
            (License) GrailsHibernateUtil.unwrapIfProxy(destinationLicense)
    }

}
