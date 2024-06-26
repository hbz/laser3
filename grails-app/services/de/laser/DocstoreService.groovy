package de.laser

import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service is one step behind {@link de.laser.ctrl.DocstoreControllerService} and contains helper methods for document retrieval
 */
@Transactional
class DocstoreService {

    MessageSource messageSource

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
                    case RDStore.SHARE_CONF_ALL: visible = true //this remark still counts - definition says that everyone with "access" to target org. How are such access roles defined and where?
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
            if(docctx.isDocAFile() && docctx.status != RDStore.DOC_CTX_STATUS_DELETED) {
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
        else if(instance instanceof Links)
            instanceClause = 'dc.link = :instance'
        else if(instance instanceof Org)
            instanceClause = 'dc.org = :instance'
        else if(instance instanceof Provider)
            instanceClause = 'dc.provider = :instance'
        else if(instance instanceof Vendor)
            instanceClause = 'dc.vendor = :instance'
        else if(instance instanceof SurveyConfig)
            instanceClause = 'dc.surveyConfig = :instance'
        if(instanceClause) {
            Set<DocContext> documents = DocContext.executeQuery('select dc from DocContext dc join dc.owner doc where ' + instanceClause + ' and doc.contentType = :string and (dc.status != :deleted or dc.status is null) and ((dc.sharedFrom = null and (doc.owner = :contextOrg or doc.owner = null)) or dc.sharedFrom != null) order by lower(doc.title)', [string: Doc.CONTENT_TYPE_STRING, deleted: RDStore.DOC_CTX_STATUS_DELETED, contextOrg: contextOrg, instance: instance])
            documents.each { DocContext dc ->
                if(dc.sharedFrom)
                    sharedItems << dc
                else filteredDocuments << dc
            }
        }
        [filteredDocuments: filteredDocuments, sharedItems: sharedItems]
    }

    /**
     * Gets the notes of the given owner institution for the given object
     * @param objInstance the object whose notes should be retrieved
     * @param docOwner the owner institution ({@link Org}) who owns the notes
     * @return a {@link List} of {@link DocContext}s pointing from the given object to respective notes
     */
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
        else if (objInstance instanceof Provider) {
            query = "dc.provider = :instance " + query
        }
        else if (objInstance instanceof Vendor) {
            query = "dc.vendor = :instance " + query
        }
        else if (objInstance instanceof SurveyConfig) {
            query = "dc.surveyConfig = :instance " + query
        }
        else {
            return []
        }


        Doc.executeQuery("select dc from DocContext dc, Doc d where " + query, queryParams)
    }

    /**
     * Counts the notes attached to the given object and owned by the given institution.
     * Notes are technically {@link Doc}s with the type {@link Doc#CONTENT_TYPE_STRING}
     * @param objInstance the object, one of {@link Subscription}, {@link License}, {@link Org} or {@link SurveyConfig}, to which the notes are attached
     * @param docOwner the institution {@link Org} whose notes should be counted
     * @return the count of matching notes
     */
    int getNotesCount(def objInstance, Org docOwner) {

        Map queryParams = [instance: objInstance, del: RDStore.DOC_CTX_STATUS_DELETED, docOwner: docOwner]
        String query =  "and dc.owner = d and d.contentType = 0 and (dc.status is null or dc.status != :del) " +
                "and (dc.sharedFrom is not null or (dc.sharedFrom is null and d.owner =: docOwner)) "

        if (objInstance instanceof Subscription) {
            query = "dc.subscription = :instance " + query
        }
        else if (objInstance instanceof License) {
            query = "dc.license = :instance " + query
        }
        else if (objInstance instanceof Org) {
            query = "dc.org = :instance " + query
        }
        else if (objInstance instanceof Provider) {
            query = "dc.provider = :instance " + query
        }
        else if (objInstance instanceof Vendor) {
            query = "dc.vendor = :instance " + query
        }
        else if (objInstance instanceof SurveyConfig) {
            query = "dc.surveyConfig = :instance " + query
        }
        else {
            return 0
        }


        Doc.executeQuery("select count(*) from DocContext dc, Doc d where " + query, queryParams)[0]
    }

    /**
     * Counts the documents ({@link Doc}s with {@link Doc#contentType} other than note) attached to the given object and owned by the given institution
     * @param objInstance the object, one of {@link Subscription}, {@link License}, {@link Org} or {@link SurveyConfig}, to which the documents are attached
     * @param docOwner the institution {@link Org} whose documents should be counted
     * @return the count of matching documents
     */
    int getDocsCount(def objInstance, Org docOwner) {

        Map queryParams = [instance: objInstance, del: RDStore.DOC_CTX_STATUS_DELETED, docOwner: docOwner]
        String query =  "and dc.owner = d and d.contentType = 3 and (dc.status is null or dc.status != :del) " +
                "and (dc.sharedFrom is not null or (dc.sharedFrom is null and d.owner =: docOwner)) "

        if (objInstance instanceof Subscription) {
            query = "dc.subscription = :instance " + query
        }
        else if (objInstance instanceof License) {
            query = "dc.license = :instance " + query
        }
        else if (objInstance instanceof Org) {
            query = "dc.org = :instance " + query
        }
        else if (objInstance instanceof Provider) {
            query = "dc.provider = :instance " + query
        }
        else if (objInstance instanceof Vendor) {
            query = "dc.vendor = :instance " + query
        }
        else if (objInstance instanceof SurveyConfig) {
            query = "dc.surveyConfig = :instance " + query
        }
        else {
            return 0
        }


        Doc.executeQuery("select count(*) from DocContext dc, Doc d where " + query, queryParams)[0]
    }

    /**
     * Performs the given bulk operation (params.bulk_op) on a list of {@link Doc}uments
     * @param params the request parameter map
     * @param result the result map containing generic data
     * @param flash the {@link grails.web.mvc.FlashScope} container for success or error messages
     */
    def bulkDocOperation (GrailsParameterMap params, Map result, def flash) {
        if (params.bulk_op == RDConstants.DOCUMENT_CONFIDENTIALITY) {
            if (params.bulk_docIdList) {
                Locale locale = LocaleUtils.getCurrentLocale()

                try {
                    RefdataValue dc = params.bulk_docConf ? RefdataValue.get(params.bulk_docConf) : null
                    List<Long> idList = Params.getLongList_forCommaSeparatedString(params, 'bulk_docIdList')
                    idList.each { id ->
                        Doc doc = Doc.get(id)
                        if (doc.owner.id == result.contextOrg.id) {
                            doc.confidentiality = dc
                            doc.save()
                        }
                    }
                    log.debug('set document.confidentiality = ' + dc + ' for ' + idList)
                    flash.message = messageSource.getMessage('default.updated.selection.message', null, locale)
                }
                catch (Exception e) {
                    log.debug(e.toString())
                    flash.error = messageSource.getMessage('default.not.updated.selection.message', null, locale)
                }
            }
        }
    }
}
