package com.k_int.kbplus

import de.laser.helper.RefdataAnnotation
import de.laser.traits.ShareableTrait
import org.hibernate.event.PostUpdateEvent // Hibernate 3
//import org.hibernate.event.spi.PostUpdateEvent // to Hibernate 4

import javax.persistence.Transient

class DocContext implements ShareableTrait {

    @Transient
    def shareService

    static belongsTo = [
        owner:          Doc,
        license:        License,
        subscription:   Subscription,
        pkg:            Package,
        link:           Links,
        org:            Org,
        /* sharedFrom:     DocContext, */ // self-referential GORM problem
        surveyConfig:   SurveyConfig
  ]

    @RefdataAnnotation(cat = 'Document Context Status')
    RefdataValue status
    @RefdataAnnotation(cat = 'Document Type')
    RefdataValue doctype
    @RefdataAnnotation(cat = 'Share Configuration')
    RefdataValue shareConf
    Org targetOrg

    Boolean globannounce = false
    DocContext sharedFrom
    Boolean isShared

  // We may attach a note to a particular column, in which case, we set domain here as a discriminator
  String domain

  static mapping = {
               id column:'dc_id'
          version column:'dc_version'
            owner column:'dc_doc_fk', sort:'title', order:'asc'
          doctype column:'dc_rv_doctype_fk'
          license column:'dc_lic_fk'
     subscription column:'dc_sub_fk'
              pkg column:'dc_pkg_fk'
              org column:'dc_org_fk'
             link column:'dc_link_fk'
     globannounce column:'dc_is_global'
           status column:'dc_status_fk'
       sharedFrom column:'dc_shared_from_fk'
         isShared column:'dc_is_shared'
        shareConf column:'dc_share_conf_fk'
        targetOrg column:'dc_target_org_fk'
     surveyConfig column: 'dc_survey_config_fk'
  }

  static constraints = {
    doctype(nullable:true, blank:false)
    license(nullable:true, blank:false)
    subscription(nullable:true, blank:false)
    pkg(nullable:true, blank:false)
    org(nullable: true,blank: false)
    link(nullable:true, blank:false)
    domain(nullable:true, blank:false)
    status(nullable:true, blank:false)
    globannounce(nullable:true, blank:true)
      sharedFrom(nullable:true, blank:true)
      isShared(nullable:true, blank:false, default:false)
      shareConf(nullable: true,blank: false)
      targetOrg(nullable: true, blank: false)
      surveyConfig (nullable: true,blank: false)
  }

    void afterUpdate(PostUpdateEvent event) {
        log.debug('afterUpdate')

        if (status?.value?.equalsIgnoreCase('Deleted')) {
            deleteShare_trait()
        }
    }

    void beforeDelete(PostUpdateEvent event) {
        log.debug('beforeDelete')
        deleteShare_trait()
    }
}
