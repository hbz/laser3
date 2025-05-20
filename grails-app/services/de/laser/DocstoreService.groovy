package de.laser

import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.utils.LocaleUtils
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource


@Transactional
class DocstoreService {

    AccessService accessService
    ContextService contextService
    MessageSource messageSource

    /**
     * Deletes a document with the given parameter map.
     * Used in:
     * <ul>
     *     <li>{@link de.laser.MyInstitutionController}</li>
     *     <li>{@link de.laser.SurveyController}</li>
     * </ul>
     * Will be replaced. Try to use {@link DocumentController#deleteDocument()}
     * @param params the parameter map, coming from one of the controllers specified in the list above
     */
    @Deprecated
    def deleteDocument(params) {

        // myInstitution > currentSubscriptionsTransfer
        // myInstitution > currentSubscriptionsTransfer_support
        // myInstitution > subscriptionsManagement
        // survey > cardDocuments
        // survey > evaluationParticipantsView
        // survey > surveyConfigDocs

        log.debug("deleteDocument (DEPRECATED): ${params}")

        if (params.deleteId) {
            DocContext docctx = DocContext.get(params.deleteId)

            if (accessService.hasAccessToDocument(docctx, AccessService.WRITE)) {
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save()
            }
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
            if(contextOrg.id == instance.getConsortium()?.id && instance.instanceOf) {
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
     * @param obj the object whose notes should be retrieved
     * @param docOwner the owner institution ({@link Org}) who owns the notes
     * @return a {@link List} of {@link DocContext}s pointing from the given object to respective notes
     */
    List<DocContext> getNotes(def obj, Org docOwner) {
        _getQueryResult(obj, docOwner, Doc.CONTENT_TYPE_STRING, false)
    }

    /**
     * Counts the notes attached to the given object and owned by the given institution.
     * Notes are technically {@link Doc}s with the type {@link Doc#CONTENT_TYPE_STRING}
     * @param obj the object, one of {@link Subscription}, {@link License}, {@link Org} or {@link SurveyConfig}, to which the notes are attached
     * @param docOwner the institution {@link Org} whose notes should be counted
     * @return the count of matching notes
     */
    int getNotesCount(def obj, Org docOwner) {
        _getQueryResult(obj, docOwner, Doc.CONTENT_TYPE_STRING, true)[0]
    }

    /**
     * Counts the documents ({@link Doc}s with {@link Doc#contentType} other than note) attached to the given object and owned by the given institution
     * @param obj the object, one of {@link Subscription}, {@link License}, {@link Org} or {@link SurveyConfig}, to which the documents are attached
     * @param docOwner the institution {@link Org} whose documents should be counted
     * @return the count of matching documents
     */
    int getDocsCount(def obj, Org docOwner) {
        _getQueryResult(obj, docOwner, Doc.CONTENT_TYPE_FILE, true)[0]
    }

    private List _getQueryResult(def obj, Org docOwner, def contentType, boolean countOnly = false) {

        Map queryParams = [
                obj         : obj,
                docOwner    : docOwner,
                contentType : contentType,
                delStatus   : RDStore.DOC_CTX_STATUS_DELETED,
        ]
        String query =  'and dc.owner = d and d.contentType = :contentType ' +
                        'and (dc.status is null or dc.status != :delStatus) ' +
                        'and (dc.sharedFrom is not null or (dc.sharedFrom is null and d.owner =: docOwner)) '

        if (obj instanceof Subscription) {
            query = 'dc.subscription = :obj ' + query
        }
        else if (obj instanceof License) {
            query = 'dc.license = :obj ' + query
        }
        else if (obj instanceof Org) {
            query = 'dc.org = :obj ' + query
        }
        else if (obj instanceof Provider) {
            query = 'dc.provider = :obj ' + query
        }
        else if (obj instanceof Vendor) {
            query = 'dc.vendor = :obj ' + query
        }
        else if (obj instanceof SurveyConfig) {
            query = 'dc.surveyConfig = :obj ' + query
        }
        else {
            return []
        }

        if (countOnly) {
            Doc.executeQuery('select count(*) from DocContext dc, Doc d where ' + query, queryParams)
        }
        else {
            Doc.executeQuery('select dc from DocContext dc, Doc d where ' + query + ' order by d.lastUpdated desc, d.dateCreated desc', queryParams)
        }
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
                        if (doc.owner.id == contextService.getOrg().id) {
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

    @Deprecated
    boolean fileCheck (String pathname) {
        try {
            File test = new File(pathname)
            if (test.exists() && test.isFile()) {
                return true
            }
        }
        catch (Exception e) {}
        false
    }
}
