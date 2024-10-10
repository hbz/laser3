package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.survey.SurveyConfig
import de.laser.traits.ShareableTrait
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import org.grails.datastore.mapping.engine.event.PostUpdateEvent

/**
 * When a user sees a document, this is what s/he sees actually: the connection between a {@link Doc} and the object the document is related to.
 * In LAS:eR, documents are mostly not on their own, they are always attached to an object. There is an exception (see /myInstitution/documents for that) when a document is for internal purposes only (which may be shared
 * occasionally); but even then, there is a link - in that case, the context org. Technically, this is the same result as attaching a document to the context org (may even be done there).
 */
class DocContext implements ShareableTrait, Comparable {

    static belongsTo = [
        owner:          Doc,
        license:        License,
        subscription:   Subscription,
        link:           Links,
        org:            Org,
        surveyConfig:   SurveyConfig,
        provider:       Provider,
        vendor:         Vendor
    ]

    @RefdataInfo(cat = RDConstants.DOCUMENT_CONTEXT_STATUS)
    RefdataValue status
    @RefdataInfo(cat = RDConstants.SHARE_CONFIGURATION)
    RefdataValue shareConf
    Org targetOrg

    DocContext sharedFrom
    Boolean isShared = false

    Date dateCreated
    Date lastUpdated

    static mapping = {
               id column:'dc_id'
          version column:'dc_version'
            owner column:'dc_doc_fk',              index: 'dc_doc_idx', sort:'title', order:'asc'
          license column:'dc_lic_fk',              index: 'dc_lic_idx'
     subscription column:'dc_sub_fk',              index: 'dc_sub_idx'
              org column:'dc_org_fk',              index: 'dc_org_idx'
         provider column:'dc_prov_fk',             index: 'dc_prov_idx'
           vendor column:'dc_ven_fk',              index: 'dc_ven_idx'
             link column:'dc_link_fk',             index: 'dc_link_idx'
           status column:'dc_status_fk',           index: 'dc_status_idx'
       sharedFrom column:'dc_shared_from_fk',      index: 'dc_shared_from_idx'
         isShared column:'dc_is_shared'
        shareConf column:'dc_share_conf_fk',       index: 'dc_share_conf_idx'
        targetOrg column:'dc_target_org_fk',       index: 'dc_target_org_idx'
     surveyConfig column: 'dc_survey_config_fk',   index: 'dc_survey_config_idx'

      dateCreated column: 'dc_date_created'
      lastUpdated column: 'dc_last_updated'

    }

    static constraints = {
        license     (nullable:true)
        subscription(nullable:true)
        org         (nullable:true)
        provider    (nullable:true)
        vendor      (nullable:true)
        link        (nullable:true)
        status      (nullable:true)
        sharedFrom    (nullable: true)
        shareConf     (nullable: true)
        targetOrg     (nullable: true)
        surveyConfig  (nullable: true)
        lastUpdated   (nullable: true)
    }

    /**
     * Triggers after the database removal of the document context also the ElasticSearch index removal
     */
    def afterDelete() {
        BeanStore.getDeletionService().deleteDocumentFromIndex(this.getClass().getSimpleName().toLowerCase()+":"+this.id, this.class.simpleName)
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
     * Gets the type of the document linked by this context
     * @return the {@link Doc#type} of the context document
     */
    RefdataValue getDocType() {
        owner?.type
    }

    /**
     * Gets the confidentiality of the document linked by this context
     * @return the {@link Doc#confidentiality} of the context document
     */
    RefdataValue getDocConfid() {
        owner?.confidentiality
    }

    /**
     * Checks whether the document is a note
     * @return true if the {@link Doc#contentType} is of type {@link Doc#CONTENT_TYPE_STRING} (= a note), false otherwise
     */
    boolean isDocANote() {
        owner?.contentType == Doc.CONTENT_TYPE_STRING // 0
    }

    /**
     * Checks whether the document is a file
     * @return true if the {@link Doc#contentType} is of type {@link Doc#CONTENT_TYPE_FILE} (= a document), false otherwise
     */
    boolean isDocAFile() {
        owner?.contentType == Doc.CONTENT_TYPE_FILE // 3
    }

    /**
     * Comparator method; the owner document's titles are being compared against each other.
     * If they are equal, the creation dates are being compared
     * @param o the {@link DocContext} to compare with
     * @return the comparison result (-1, 0 or 1)
     */
    int compareTo(Object o) {
        int result = 0
        DocContext dc = (DocContext) o
        if(owner && dc.owner) {
            result = owner.title?.toLowerCase()?.compareTo(dc.owner.title?.toLowerCase())
        }
        if(result == 0) {
            result = owner.dateCreated <=> dc.owner.dateCreated
        }
        result
    }
}
