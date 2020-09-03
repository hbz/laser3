package com.k_int.kbplus

import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation
import de.laser.traits.ShareableTrait
import org.hibernate.event.PostUpdateEvent

import javax.persistence.Transient
import java.text.SimpleDateFormat

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
    def setReference(def owner) {
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

    static def assertOrgTitleLink(porg, ptitle, prole, pstart, pend) {
    // def link = OrgRole.findByTitleAndOrgAndRoleType(ptitle, porg, prole) ?: new OrgRole(title:ptitle, org:porg, roleType:prole).save();

    if ( porg && ptitle && prole ) {

      def link = OrgRole.find{ title==ptitle && org==porg && roleType==prole }
      if ( ! link ) {

        link = new OrgRole(title:ptitle, org:porg, roleType:prole)

          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        if(pstart){
          if(pstart instanceof Date){

          }else{
            pstart = sdf.parse(pstart)
          }
          link.startDate = pstart
        }
        if(pend){
          if(pend instanceof Date){

          }else{
            pend = sdf.parse(pend)
          }
          link.endDate = pend
        }

        if ( !porg.links )
          porg.links = [link]
        else
          porg.links.add(link)
  
        porg.save(flush:true, failOnError:true);
      }
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
