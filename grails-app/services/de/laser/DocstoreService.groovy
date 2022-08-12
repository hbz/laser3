package de.laser

import de.laser.storage.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.survey.SurveyConfig
import grails.gorm.transactions.Transactional

/**
 * This service is one step behind {@link de.laser.ctrl.DocstoreControllerService} and contains helper methods for document retrieval
 */
@Transactional
class DocstoreService {

    /**
     * Deletes a document with the given parameter map.
     * Used in:
     * <ul>
     *     <li>{@link de.laser.LicenseController}</li>
     *     <li>{@link de.laser.MyInstitutionController}</li>
     *     <li>{@link de.laser.PackageController}</li>
     *     <li>{@link de.laser.SubscriptionController}</li>
     * </ul>
     * @param params the parameter map, coming from one of the controllers specified in the list above
     */
    def unifiedDeleteDocuments(params) {

        params.each { p ->
            if (p.key.startsWith('_deleteflag.') ) {
                String docctx_to_delete = p.key.substring(12)
                log.debug("Looking up docctx ${docctx_to_delete} for delete")

                DocContext docctx = DocContext.get(docctx_to_delete)
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save()
            }
            if (p.key.startsWith('_deleteflag"@.') ) { // PackageController
                String docctx_to_delete = p.key.substring(12);
                log.debug("Looking up docctx ${docctx_to_delete} for delete")

                DocContext docctx = DocContext.get(docctx_to_delete)
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save()
            }
        }

        if (params.deleteId) {
            String docctx_to_delete = params.deleteId
            log.debug("Looking up docctx ${docctx_to_delete} for delete")

            DocContext docctx = DocContext.get(docctx_to_delete)
            docctx.status = RDStore.DOC_CTX_STATUS_DELETED
            docctx.save()
        }
    }

    /**
     * Retrieves all documents which have been attached to the given organisation and are visible by the same organisation
     * @param org the {@link Org} which has been marked as target
     * @return a {@link List} of matching documents
     */
    List getTargettedDocuments(Org org) {
        List<DocContext> furtherDocs = DocContext.findAllByTargetOrgAndShareConf(org,RDStore.SHARE_CONF_UPLOADER_AND_TARGET)
        furtherDocs
    }

    /**
     * Retrieves and orders the documents attached to an instance of a given org for an export view. Currently used by the license PDF export, but should be extended
     * @param contextOrg the {@link Org} whose documents should be retrieved
     * @param instance the owner object ({@link Subscription}/{@link License}/{@link Org}/{@link SurveyConfig}) whose attached documents should be retrieved
     * @return a {@link Map} with a {@link SortedSet} of {@link DocContext}s containing links to the documents attached to the owner object
     */
    //this method may serve as document view refactoring base
    Map<String, SortedSet<DocContext>> getDocumentsForExport(Org contextOrg, instance) {
        boolean parentAtChild = false
        Set<DocContext> documentSet = instance.documents
        SortedSet<DocContext> filteredDocuments = new TreeSet<DocContext>(), sharedItems = new TreeSet<DocContext>()
        if(instance instanceof Subscription) {
            if(contextOrg.id == instance.getConsortia()?.id && instance.instanceOf) {
                if(instance._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)
                    parentAtChild = true
            }
        }
        else if(instance instanceof License) {
            if(contextOrg.id == instance.getLicensingConsortium()?.id && instance.instanceOf) {
                parentAtChild = true
            }
        }
        else if(instance instanceof Org && instance.id == contextOrg.id) {
            documentSet.addAll(getTargettedDocuments((Org) instance))
        }
        documentSet.each { DocContext docctx ->
            boolean visible = false
            boolean inOwnerOrg = docctx.owner.owner?.id == contextOrg.id
            boolean inTargetOrg = contextOrg.id == docctx.targetOrg?.id

            if(docctx.org) {
                switch(docctx.shareConf) {
                    case RDStore.SHARE_CONF_UPLOADER_ORG: visible = inOwnerOrg
                        break
                    case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: visible = inOwnerOrg || inTargetOrg
                        break
                    case [ RDStore.SHARE_CONF_CONSORTIUM, RDStore.SHARE_CONF_ALL ]: visible = true //this remark still counts - definition says that everyone with "access" to target org. How are such access roles defined and where?
                        break
                    default:
                        if(docctx.shareConf)
                            log.debug(docctx.shareConf)
                        else visible = true
                        break
                }
            }
            else if(inOwnerOrg || docctx.sharedFrom)
                visible = true
            else {
                if((parentAtChild && docctx.sharedFrom) || !parentAtChild && docctx.owner?.owner?.id == contextOrg.id)
                    visible = true
            }
            if(docctx.owner?.contentType == Doc.CONTENT_TYPE_FILE && docctx.status != RDStore.DOC_CTX_STATUS_DELETED) {
                if((docctx.sharedFrom || inTargetOrg) && visible)
                    sharedItems.add(docctx)
                else if(visible)
                    filteredDocuments.add(docctx)
            }
        }
        [filteredDocuments: filteredDocuments, sharedItems: sharedItems]
    }

    /**
     * Retrieves and orders the notes attached to an instance of a given org for an export view. Currently used by the license PDF export, but should be extended
     * @param contextOrg the {@link Org} whose documents should be retrieved
     * @param instance the owner object ({@link Subscription}/{@link License}/{@link Org}/{@link SurveyConfig}) whose attached documents should be retrieved
     * @return a {@link Map} with a {@link SortedSet} of {@link DocContext}s containing notes attached to the owner object
     */
    Map<String, SortedSet<DocContext>> getNotesForExport(Org contextOrg, instance) {
        String instanceClause
        SortedSet<DocContext> filteredDocuments = new TreeSet<DocContext>(), sharedItems = new TreeSet<DocContext>()
        if(instance instanceof License)
            instanceClause = 'dc.license = :instance'
        else if(instance instanceof Subscription)
            instanceClause = 'dc.subscription = :instance'
        else if(instance instanceof Package)
            instanceClause = 'dc.pkg = :instance'
        else if(instance instanceof Links)
            instanceClause = 'dc.link = :instance'
        else if(instance instanceof Org)
            instanceClause = 'dc.org = :instance'
        else if(instance instanceof SurveyConfig)
            instanceClause = 'dc.surveyConfig = :instance'
        if(instanceClause) {
            Set<DocContext> documents = DocContext.executeQuery('select dc from DocContext dc join dc.owner doc where ' + instanceClause + ' and doc.contentType = :string and doc.status != :deleted and ((dc.sharedFrom = null and (doc.owner = :contextOrg or doc.owner = null)) or dc.sharedFrom != null) order by lower(doc.title)', [string: Doc.CONTENT_TYPE_STRING, deleted: RDStore.DOC_CTX_STATUS_DELETED, contextOrg: contextOrg, instance: instance])
            documents.each { DocContext dc ->
                if(dc.sharedFrom)
                    sharedItems << dc
                else filteredDocuments << dc
            }
        }
        [filteredDocuments: filteredDocuments, sharedItems: sharedItems]
    }

    List<DocContext> getNotes(def objInstance, Org docOwner) {

        Map queryParams = [instance: objInstance, del: RDStore.DOC_CTX_STATUS_DELETED, docOwner: docOwner]
        String query =  "and dc.owner = d and d.contentType = 0 and (dc.status is null or dc.status != :del) " +
                        "and (dc.sharedFrom is not null or (dc.sharedFrom is null and d.owner =: docOwner)) " +
                        "order by d.lastUpdated desc, d.dateCreated desc"

        if (objInstance instanceof Subscription) {
            query = "dc.subscription = :instance " + query
        }
        else if (objInstance instanceof License) {
            query = "dc.license = :instance " + query
        }
        else if (objInstance instanceof Org) {
            query = "dc.org = :instance " + query
        }
        else if (objInstance instanceof SurveyConfig) {
            query = "dc.surveyConfig = :instance " + query
        }
        else {
            return []
        }


        Doc.executeQuery("select dc from DocContext dc, Doc d where " + query, queryParams)
    }
}
