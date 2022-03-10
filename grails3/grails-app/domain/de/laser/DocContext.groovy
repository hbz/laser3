package de.laser


import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation
import de.laser.traits.ShareableTrait
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

/**
 * When a user sees a document, this is what s/he sees actually: the connection between a {@link Doc} and the object the document is related to.
 * In LAS:eR, documents are mostly not on their own, they are always attached to an object. There is an exception (see /myInstitution/documents for that) when a document is for internal purposes only (which may be shared
 * occasionally); but even then, there is a link - in that case, the context org. Technically, this is the same result as attaching a document to the context org (may even be done there).
 */
class DocContext implements ShareableTrait, Comparable {

    def deletionService
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

    @RefdataAnnotation(cat = RDConstants.DOCUMENT_CONTEXT_STATUS)
    RefdataValue status
    @RefdataAnnotation(cat = RDConstants.DOCUMENT_TYPE)
    RefdataValue doctype
    @RefdataAnnotation(cat = RDConstants.SHARE_CONFIGURATION)
    RefdataValue shareConf
    Org targetOrg

    Boolean globannounce = false
    DocContext sharedFrom
    Boolean isShared = false

    Date dateCreated
    Date lastUpdated

    // We may attach a note to a particular column, in which case, we set domain here as a discriminator
    String domain

    static mapping = {
               id column:'dc_id'
          version column:'dc_version'
            owner column:'dc_doc_fk', sort:'title', order:'asc', index:'doc_owner_idx'
          doctype column:'dc_rv_doctype_fk'
          license column:'dc_lic_fk', index:'doc_lic_idx'
     subscription column:'dc_sub_fk', index:'doc_sub_idx'
              pkg column:'dc_pkg_fk'
              org column:'dc_org_fk', index:'doc_org_idx'
             link column:'dc_link_fk'
     globannounce column:'dc_is_global'
           status column:'dc_status_fk'
       sharedFrom column:'dc_shared_from_fk'
         isShared column:'dc_is_shared'
        shareConf column:'dc_share_conf_fk'
        targetOrg column:'dc_target_org_fk'
     surveyConfig column: 'dc_survey_config_fk'

      dateCreated column: 'dc_date_created'
      lastUpdated column: 'dc_last_updated'

    }

    static constraints = {
        doctype     (nullable:true)
        license     (nullable:true)
        subscription(nullable:true)
        pkg         (nullable:true)
        org         (nullable:true)
        link        (nullable:true)
        domain      (nullable:true, blank:false)
        status      (nullable:true)
        sharedFrom    (nullable: true)
        shareConf     (nullable: true)
        targetOrg     (nullable: true)
        surveyConfig  (nullable: true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    /**
     * Triggers after the database removal of the document context also the ElasticSearch index removal
     */
    def afterDelete() {
        deletionService.deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
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

    /**
     * Comparator method; the owner document's titles are being compared against each other
     * @param o the {@link DocContext} to compare with
     * @return the comparison result (-1, 0 or 1)
     */
    int compareTo(Object o) {
        int result = 0
        DocContext dc = (DocContext) o
        if(owner && dc.owner) {
            result = owner.title?.toLowerCase()?.compareTo(dc.owner.title?.toLowerCase())
        }
        result
    }
}
