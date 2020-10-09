package de.laser

import com.k_int.kbplus.License
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import de.laser.titles.TitleInstance
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation
import de.laser.traits.ShareableTrait
import org.hibernate.event.PostUpdateEvent

import javax.persistence.Transient

class OrgRole implements ShareableTrait {

    def shareService

    static belongsTo = [
        org: Org,
        /* sharedFrom: OrgRole */ // self-referential GORM problem
    ]

    @RefdataAnnotation(cat = RDConstants.ORGANISATIONAL_ROLE)
    RefdataValue roleType

    Package       pkg
    Subscription  sub
    License       lic
    TitleInstance title
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
       title column:'or_title_fk'
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
    title       (nullable:true)
    startDate   (nullable:true)
    endDate     (nullable:true)
    sharedFrom  (nullable:true)

    // Nullable is true, because values are already in the database
    lastUpdated (nullable: true)
    dateCreated (nullable: true)
  }

    /**
     * Generic setter
     */
    void setReference(def owner) {
        org     = owner instanceof Org ? owner : org
        pkg     = owner instanceof Package ? owner : pkg
        lic     = owner instanceof License ? owner : lic
        sub     = owner instanceof Subscription ? owner : sub
        title   = owner instanceof TitleInstance ? owner : title
    }

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
        if (title) {
            return title
        }
    }

    // dynamic binding for hql queries
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
        if (title) {
            return title.getStatus()
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
