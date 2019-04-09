package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation
import de.laser.traits.ShareableTrait
//import org.hibernate.event.PostUpdateEvent // Hibernate 3
import org.hibernate.event.spi.PostUpdateEvent // Hibernate 4

import javax.persistence.Column
import javax.persistence.Transient

class OrgRole implements ShareableTrait {

    @Transient
    def shareService

    static belongsTo = [
        org: Org,
        sharedFrom: OrgRole
    ]

    @RefdataAnnotation(cat = 'Organisational Role')
    RefdataValue roleType

    Package       pkg
    Subscription  sub
    License       lic
    Cluster       cluster
    TitleInstance title
    Date          startDate
    Date          endDate

    OrgRole sharedFrom
    Boolean isShared

    // dynamic binding for hql queries
    @Transient
    ownerStatus

  static mapping = {
          id column:'or_id'
     version column:'or_version'
         org column:'or_org_fk',        index:'or_org_rt_idx'
    roleType column:'or_roletype_fk',   index:'or_org_rt_idx'
         pkg column:'or_pkg_fk'
         sub column:'or_sub_fk',        index:'or_sub_idx'
         lic column:'or_lic_fk',        index:'or_lic_idx'
     cluster column:'or_cluster_fk'
       title column:'or_title_fk'
   startDate column:'or_start_date'
     endDate column:'or_end_date'
    isShared column:'or_is_shared'
  sharedFrom column:'or_shared_from_fk'
         org sort: 'name', order: 'asc'
  }

  static constraints = {
    roleType    (nullable:true, blank:false)
    pkg         (nullable:true, blank:false)
    sub         (nullable:true, blank:false)
    lic         (nullable:true, blank:false)
    cluster     (nullable:true, blank:false)
    title       (nullable:true, blank:false)
    startDate   (nullable:true, blank:false)
    endDate     (nullable:true, blank:false)
    isShared    (nullable:true, blank:false, default:false)
    sharedFrom  (nullable:true, blank:true)
  }

    /**
     * Generic setter
     */
    def setReference(def owner) {
        org     = owner instanceof Org ? owner : org
        pkg     = owner instanceof Package ? owner : pkg
        lic     = owner instanceof License ? owner : lic
        sub     = owner instanceof Subscription ? owner : sub
        cluster = owner instanceof Cluster ? owner : cluster
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

        def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

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

  static def assertOrgPackageLink(porg, ppkg, prole) {

    if ( porg && ppkg && prole ) {
      def link = OrgRole.find{ org==porg && pkg==ppkg && roleType==prole }
      if ( ! link ) {
        link = new OrgRole(pkg:ppkg, org:porg, roleType:prole)
        if ( !porg.links )
          porg.links = [link]
        else
          porg.links.add(link)

        porg.save();
      }
    }
  }

    void afterUpdate(PostUpdateEvent event) {
        log.debug('afterUpdate')
    }

    void beforeDelete(PostUpdateEvent event) {
        log.debug('beforeDelete')
    }
}
