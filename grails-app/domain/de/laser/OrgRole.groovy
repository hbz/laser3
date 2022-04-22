package de.laser


import de.laser.storage.RDConstants
import de.laser.annotations.RefdataInfo
import de.laser.traits.ShareableTrait
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

import javax.persistence.Transient

/**
 * This domain links organisations (institutions and other ones) to other objects. The objects may be one of:
 * <ul>
 *     <li>{@link Package}</li>
 *     <li>{@link TitleInstancePackagePlatform}</li>
 *     <li>{@link License}</li>
 *     <li>{@link Subscription}</li>
 * </ul>
 * A link is specified by the roleType attribute; the role type is also essential for checking the organisation type. Providers are usually linked with provider role types (Provider, Content Provider); agencies as
 * Agency while institutions will never take such role types. Institutions are linked by subscription or licensee role types, those may be:
 * <ul>
 *     <li>Subscriber (for local subscriptions; used mainly by single users)</li>
 *     <li>Subscriber_Consortial (consortial membership)</li>
 *     <li>Subscription Consortia (consortial parenthood)</li>
 * </ul>
 * and for licenses:
 * <ul>
 *     <li>Licensee (for local licenses; used mainly by single users)</li>
 *     <li>Licensee_Consortial (consortial membershipt)</li>
 *     <li>Licensing Consortium (consortial parenthood)</li>
 * </ul>
 * The role types listed above will never be taken by providers, agencies or similar; the linking of an {@link Org} to other objects permits thus determination of the organisation type itself. This is useful if the
 * organisation type ({@link Org#orgType}) set is empty because no one assigned a type.
 * Moreover, an organisation link may be inherited from a consortial parent object to its member children
 * @see Org
 * @see Package
 * @see TitleInstancePackagePlatform
 * @see License
 * @see Subscription
 */
class OrgRole implements ShareableTrait {

    static belongsTo = [
        org: Org,
        /* sharedFrom: OrgRole */ // self-referential GORM problem
    ]

    @RefdataInfo(cat = RDConstants.ORGANISATIONAL_ROLE)
    RefdataValue roleType

    Package       pkg
    Subscription  sub
    License       lic
    TitleInstancePackagePlatform tipp
    Date          startDate
    Date          endDate

    OrgRole sharedFrom
    Boolean isShared = false //workaround, default value is not set!

    Date dateCreated
    Date lastUpdated

    // dynamic binding for hql queries
    @Transient
    ownerStatus

    static transients = ['owner'] // mark read-only accessor methods

  static mapping = {
          id column:'or_id'
     version column:'or_version'
         org column:'or_org_fk',        index:'or_org_rt_idx'
    roleType column:'or_roletype_fk',   index:'or_org_rt_idx'
         pkg column:'or_pkg_fk',        index:'or_pkg_idx'
         sub column:'or_sub_fk',        index:'or_sub_idx'
         lic column:'or_lic_fk',        index:'or_lic_idx'
        tipp column:'or_tipp_fk',       index:'or_tipp_idx'
   startDate column:'or_start_date'
     endDate column:'or_end_date'
    isShared column:'or_is_shared'
  sharedFrom column:'or_shared_from_fk'
         org sort: 'name', order: 'asc'

      dateCreated column: 'or_date_created'
      lastUpdated column: 'or_last_updated'
  }

  static constraints = {
    roleType    (nullable:true)
    pkg         (nullable:true)
    sub         (nullable:true)
    lic         (nullable:true)
    tipp        (nullable:true)
    startDate   (nullable:true)
    endDate     (nullable:true)
    sharedFrom  (nullable:true)

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true)
    dateCreated (nullable: true)
  }

    /**
     * Generic setter
     * @param owner the destination to set for this link
     */
    void setReference(def owner) {
        org     = owner instanceof Org ? owner : org
        pkg     = owner instanceof Package ? owner : pkg
        lic     = owner instanceof License ? owner : lic
        sub     = owner instanceof Subscription ? owner : sub
        tipp    = owner instanceof TitleInstancePackagePlatform ? owner : tipp
    }

    /**
     * Gets the destination of this link
     * @return the destination, depending of its object type ({@link Package}, {@link Subscription}, {@link License} or {@link TitleInstancePackagePlatform})
     */
    def getOwner() {
        if (pkg) {
            return pkg
        }
        if (sub) {
            return sub
        }
        if (lic) {
            return lic
        }
        if (tipp) {
            return tipp
        }
    }

    /**
     * Gets the status of the destination object
     * Used for dynamic binding for hql queries
     * @return the status of the destination, depending on its class
     */
    def getOwnerStatus() {
        if (pkg) {
            return pkg.getPackageStatus()
        }
        if (sub) {
            return sub.getStatus()
        }
        if (lic) {
            return lic.getStatus()
        }
        if (tipp) {
            return tipp.getStatus()
        }
    }

    void afterUpdate(PostUpdateEvent event) {
        log.debug('afterUpdate')
    }

    void beforeDelete(PostUpdateEvent event) {
        log.debug('beforeDelete')
        deleteShare_trait()
    }
}
