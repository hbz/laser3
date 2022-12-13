package de.laser

import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.ctrl.SubscriptionControllerService
import de.laser.exceptions.EntitlementCreationException
import de.laser.interfaces.CalculatedType
import de.laser.remote.ApiSource
import de.laser.stats.Counter4Report
import de.laser.stats.Counter5Report
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyOrg
import de.laser.utils.DateUtils
import de.laser.utils.SwissKnife
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat

/**
 * This controller is responsible for the subscription handling. Many of the controller calls do
 * also data manipulation; they thus needed to be wrapped in a transactional service for that database
 * actions are being persisted
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class SubscriptionController {

    AccessPointService accessPointService
    AccessService accessService
    ContextService contextService
    CopyElementsService copyElementsService
    DeletionService deletionService
    DocstoreService docstoreService
    EscapeService escapeService
    ExportClickMeService exportClickMeService
    ExportService exportService
    GenericOIDService genericOIDService
    GokbService gokbService
    LinksGenerationService linksGenerationService
    ManagementService managementService
    SubscriptionControllerService subscriptionControllerService
    SubscriptionService subscriptionService
    SurveyService surveyService

    //-----

    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'myInstitution/currentSubscriptions' : 'myinst.currentSubscriptions.label'
    ]

    //-------------------------------------- general or ungroupable section -------------------------------------------

    /**
     * Call to show the details of the given subscription
     * @return the subscription details view
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def show() {
        Map<String,Object> ctrlResult = subscriptionControllerService.show(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else ctrlResult.result
    }

    /**
     * Call to list the tasks related to this subscription
     * @return the task listing for this subscription
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER") })
    @Check404()
    def tasks() {
        Map<String,Object> ctrlResult = subscriptionControllerService.tasks(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
                ctrlResult.result
            }
    }

    @Deprecated
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def changes() {
        Map<String,Object> ctrlResult = subscriptionControllerService.changes(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else ctrlResult.result
    }

    /**
     * Call to prepare the usage data form for the given subscription
     * @return the filter for the given subscription
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', wtc = DebugInfo.IN_BETWEEN)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def stats() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
        result.flagContentGokb = true // gokbService.queryElasticsearch
        Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: result.subscription])
        if(!subscribedPlatforms) {
            subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription", [subscription: result.subscription])
        }
        Set<Subscription> refSubs = [result.subscription, result.subscription.instanceOf]
        result.platformInstanceRecords = [:]
        result.platforms = subscribedPlatforms
        result.platformsJSON = subscribedPlatforms.globalUID as JSON
        subscribedPlatforms.each { Platform platformInstance ->
            Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/find?uuid=${platformInstance.gokbId}")
            if (queryResult.error && queryResult.error == 404) {
                result.wekbServerUnavailable = message(code: 'wekb.error.404')
            }
            else if (queryResult.warning) {
                List records = queryResult.warning.records
                if(records[0]) {
                    records[0].lastRun = platformInstance.counter5LastRun ?: platformInstance.counter4LastRun
                    result.platformInstanceRecords[platformInstance.gokbId] = records[0]
                }
            }
        }
        if(result.subscription._getCalculatedType() != CalculatedType.TYPE_CONSORTIAL) {
            //Set<String> tippUIDs = subscriptionControllerService.fetchTitles(params, refSubs, 'uids')
            Map<String, Object> queryParamsBound = [customer: result.subscription.getSubscriber().globalUID, platforms: subscribedPlatforms.globalUID],
                                dateRangeParams = subscriptionControllerService.getDateRange(params, result.subscription)
            if(dateRangeParams.dateRange.length() > 0) {
                queryParamsBound.startDate = dateRangeParams.startDate
                queryParamsBound.endDate = dateRangeParams.endDate
            }
            Counter5Report.withTransaction {
                Set allAvailableReports = []
                allAvailableReports.addAll(Counter5Report.executeQuery('select new map(lower(r.reportType) as reportType, r.accessType as accessType, r.metricType as metricType, r.accessMethod as accessMethod) from Counter5Report r where r.reportInstitutionUID = :customer and r.platformUID in (:platforms) '+dateRangeParams.dateRange+' group by r.reportType, r.accessType, r.metricType, r.accessMethod', queryParamsBound))
                if(allAvailableReports.size() > 0) {
                    Set<String> reportTypes = [], metricTypes = [], accessTypes = [], accessMethods = []
                    allAvailableReports.each { row ->
                        if(!params.loadFor || (params.loadFor && !(row.reportType in Counter5Report.COUNTER_5_PLATFORM_REPORTS))) {
                            if (row.reportType)
                                reportTypes << row.reportType
                            if (row.metricType)
                                metricTypes << row.metricType
                            if (row.accessMethod)
                                accessMethods << row.accessMethod
                            if (row.accessType)
                                accessTypes << row.accessType
                        }
                    }
                    result.reportTypes = reportTypes
                    result.metricTypes = metricTypes
                    result.accessTypes = accessTypes
                    result.accessMethods = accessMethods
                    result.revision = 'counter5'
                }
                else {
                    allAvailableReports.addAll(Counter4Report.executeQuery('select new map(r.reportType as reportType, r.metricType as metricType) from Counter4Report r where r.reportInstitutionUID = :customer and r.platformUID in (:platforms) '+dateRangeParams.dateRange+' group by r.reportType, r.metricType order by r.reportType', queryParamsBound))
                    Set<String> reportTypes = [], metricTypes = []
                    allAvailableReports.each { row ->
                        if(!params.loadFor || (params.loadFor && row.reportType != Counter4Report.PLATFORM_REPORT_1)) {
                            if (row.reportType)
                                reportTypes << row.reportType
                            if (row.metricType)
                                metricTypes << row.metricType
                        }
                    }
                    result.reportTypes = reportTypes
                    result.metricTypes = metricTypes
                    result.revision = 'counter4'
                }
            }
        }
        result
    }

    /**
     * Call to fetch the usage data for the given subscription
     * @return the (filtered) usage data view for the given subscription, rendered as Excel worksheet
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def generateReport() {
        SXSSFWorkbook wb = exportService.generateReport(params)
        if(!wb) {
            response.sendError(401)
            return
        }
        else {
            Date dateRun = new Date()
            response.setHeader "Content-disposition", "attachment; filename=report_${DateUtils.getSDF_yyyyMMdd().format(dateRun)}.xlsx"
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
    }

    /*@DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })*/
    @Deprecated
    @Secured(['ROLE_ADMIN'])
    def compare() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)

        result
    }

    /**
     * Call to unlink the given subscription from the given license
     * @return a redirect back to the referer
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def unlinkLicense() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            response.sendError(401)
            return
        }
        else {
            subscriptionService.setOrgLicRole(result.subscription,License.get(params.license),true)
            redirect(url: request.getHeader('referer'))
        }
    }

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

    /**
     * Call to create a new subscription
     * @return the empty subscription form or the list of subscriptions in case of an error
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")})
    def emptySubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.emptySubscription(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = message(code: ctrlResult.messageToken) as String
            redirect action: 'currentSubscriptions'
            return
        }
        else
            ctrlResult.result
    }

    /**
     * Call to process the given input and to create a new subscription instance
     * @return the new subscription's details view in case of success, the subscription list view otherwise
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR") })
    def processEmptySubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processEmptySubscription(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.errorMessage
            redirect controller: 'myInstitution', action: 'currentSubscriptions' //temp
            return
        } else {
            redirect action: 'show', id: ctrlResult.result.newSub.id
            return
        }
    }

    /**
     * Call to delete the given subscription instance. If confirmed, the deletion is executed
     * @return the result of {@link DeletionService#deleteSubscription(de.laser.Subscription, boolean)}
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(result.subscription.instanceOf)
            result.parentId = result.subscription.instanceOf.id
        else if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_CONSORTIAL, CalculatedType.TYPE_ADMINISTRATIVE])
            result.parentId = result.subscription.id

        if (params.process  && result.editable) {
            result.licenses.each { License l ->
                subscriptionService.setOrgLicRole(result.subscription,l,true)
            }
            result.delResult = deletionService.deleteSubscription(result.subscription, false)
        }
        else {
            result.delResult = deletionService.deleteSubscription(result.subscription, DeletionService.DRY_RUN)
        }

        result
    }

    //--------------------------------------------- document section ----------------------------------------------

    /**
     * Call to list the notes attached to the given subscription
     * @return the table view of notes for the given subscription
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def notes() {
        Map<String,Object> ctrlResult = subscriptionControllerService.notes(this, params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }
        else ctrlResult.result
    }

    /**
     * Call to list the documents attached to the given subscription
     * @return the table view of documents for the given subscription
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER") })
    @Check404()
    def documents() {
        Map<String,Object> ctrlResult = subscriptionControllerService.documents(this, params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }
        else {
            if (params.bulk_op) {
                docstoreService.bulkDocOperation(params, ctrlResult.result as Map, flash)
            }

            ctrlResult.result
        }
    }

    /**
     * Call to edit the metadata of the given document
     * @return opens the document editing modal
     */
    @DebugInfo(test='hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map<String,Object> result = [user: contextService.getUser(), institution: contextService.getOrg()]
        result.ownobj = Subscription.get(params.instanceId)
        result.owntp = 'subscription'
        if(params.id) {
            result.docctx = DocContext.get(params.id)
            result.doc = result.docctx.owner
        }

        render template: "/templates/documents/modal", model: result
    }

    /**
     * Call to delete the given document attached to a subscription
     * @return a redirect, specified in the request parameters
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def deleteDocuments() {
        docstoreService.unifiedDeleteDocuments(params)
        redirect controller: 'subscription', action: params.redirectAction, id: params.instanceId
    }

    //--------------------------------- consortia members section ----------------------------------------------

    /**
     * Call to list the members of the given consortial subscription. The list may be rendered as direct HTML output
     * or exported as (configurable) Excel worksheet
     * @return a (filtered) view of the consortium members, either as HTML output or as Excel worksheet
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def members() {
        Map<String,Object> ctrlResult = subscriptionControllerService.members(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }
        else {
            SimpleDateFormat sdf = DateUtils.getSDF_yyyyMMdd()
            String datetoday = sdf.format(new Date())
            String filename = escapeService.escapeString(ctrlResult.result.subscription.name) + "_" + message(code:'subscriptionDetails.members.members') + "_" + datetoday
            if(params.exportXLS || params.exportShibboleths || params.exportEZProxys || params.exportProxys || params.exportIPs || params.exportClickMeExcel) {
                SXSSFWorkbook wb
                if(params.exportXLS) {
                    wb = (SXSSFWorkbook) exportService.exportOrg(ctrlResult.result.orgs, filename, true, 'xlsx')
                }
                if(params.exportClickMeExcel) {
                    if (params.filename) {
                        filename =params.filename
                    }

                    Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                    Map<String, Object> selectedFields = [:]
                    selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

                    wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptionMembers(ctrlResult.result.filteredSubChilds, selectedFields, ctrlResult.result.subscription, ctrlResult.result.institution)
                }
                else if (params.exportIPs) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportIPs.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportIPsOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }else if (params.exportProxys) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportProxys.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportProxysOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }else if (params.exportEZProxys) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportEZProxys.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportEZProxysOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }else if (params.exportShibboleths) {
                    filename = "${datetoday}_" + escapeService.escapeString(message(code: 'subscriptionDetails.members.exportShibboleths.fileName'))
                    wb = (SXSSFWorkbook) accessPointService.exportShibbolethsOfOrgs(ctrlResult.result.filteredSubChilds.orgs.flatten())
                }
                if(wb) {
                    response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                    response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    wb.write(response.outputStream)
                    response.outputStream.flush()
                    response.outputStream.close()
                    wb.dispose()
                    return
                }
            }
            else {
                withFormat {
                    html {
                        ctrlResult.result
                    }
                    csv {
                        response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                        response.contentType = "text/csv"
                        ServletOutputStream out = response.outputStream
                        out.withWriter { writer ->
                            writer.write((String) exportService.exportOrg(ctrlResult.result.orgs, filename, true, "csv"))
                        }
                        out.close()
                    }
                }
            }
        }
    }

    /**
     * Call to list potential member institutions to add to this subscription
     * @return a list view of member institutions
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    @Check404
    def addMembers() {
        log.debug("addMembers ..")
        Map<String,Object> ctrlResult = subscriptionControllerService.addMembers(this,params)

        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }
        else {
            ctrlResult.result
        }
    }

    /**
     * Call to process the given input data and create member subscription instances for the given consortial subscription
     * @return a redirect to the subscription members view in case of success or details view or to the member adding form otherwise
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processAddMembers(this,params)
        if (ctrlResult.error == SubscriptionControllerService.STATUS_ERROR) {
            if (ctrlResult.result) {
                redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.subscription.id]
                return
            } else {
            response.sendError(401)
                return
            }
        }
        else {
            redirect controller: 'subscription', action: 'members', params: [id: ctrlResult.result.subscription.id]
            return
        }
    }

    /**
     * Call to insert a succession link between two member subscriptions
     * @return the members view
     */
    @DebugInfo(perm="ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def linkNextPrevMemberSub() {
        Map<String,Object> ctrlResult = subscriptionControllerService.linkNextPrevMemberSub(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else redirect(url: request.getHeader('referer'))
        }
        else {
            redirect(action: 'members', id: params.id)
        }
    }

    /**
     * Call to a bulk operation view on member instances
     * @return the requested tab view
     */
    @DebugInfo(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR") })
    def membersSubscriptionsManagement() {
        def input_file

        if(params.tab == 'documents' && params.processOption == 'newDoc') {
            input_file = request.getFile("upload_file")
            if (input_file.size == 0) {
                flash.error = message(code: 'template.emptyDocument.file') as String
                redirect(url: request.getHeader('referer'))
                return
            }
            params.original_filename = input_file.originalFilename
            params.mimeType = input_file.contentType
        }

        Map<String, Object> ctrlResult = subscriptionControllerService.membersSubscriptionsManagement(this, params, input_file)

        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            params.tab = params.tab ?: 'generalProperties'
            if(ctrlResult.result.tabPlat && !params.tabPlat)
                params.tabPlat = ctrlResult.result.tabPlat.toString()

        }
        ctrlResult.result
    }

    /**
     * Call to unset the given customer identifier
     * @return the customer identifier tabs view
     */
    @DebugInfo(perm = "ORG_CONSORTIUM", affil = "INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_EDITOR")
    })
    def deleteCustomerIdentifier() {
        managementService.deleteCustomerIdentifier(params.long("deleteCI"))
        redirect(url: request.getHeader("referer"))
    }

    //-------------------------------- survey section --------------------------------------

    /**
     * Call to list surveys linked to a member subscription
     * @return a table view of surveys from the member's point of view
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def surveys() {
        Map<String,Object> ctrlResult = subscriptionControllerService.surveys(this, params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }
        else ctrlResult.result
    }

    /**
     * Call to list surveys linked to a consortial subscription
     * @return a table view of surveys from the consortium's point of view
     */
    @DebugInfo(perm = "ORG_CONSORTIUM", affil = "INST_USER", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER") })
    @Check404()
    def surveysConsortia() {
        Map<String,Object> ctrlResult = subscriptionControllerService.surveysConsortia(this, params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }
        else ctrlResult.result
    }

    //------------------------------------- packages section -------------------------------------------

    /**
     * Call to list the potential package candidates for linking
     * @return a list view of the packages in the we:kb ElasticSearch index or a redirect to an title list view
     * if a package UUID has been submitted with the call
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def linkPackage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.linkPackage(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            if(params.addUUID) {
                switch(params.addType) {
                    case "With": flash.message = message(code:'subscription.details.link.processingWithEntitlements') as String
                        redirect action: 'index', params: [id: params.id, gokbId: params.addUUID]
                        return
                        break
                    case "Without": flash.message = message(code:'subscription.details.link.processingWithoutEntitlements') as String
                        redirect action: 'addEntitlements', params: [id: params.id, packageLinkPreselect: params.addUUID, preselectedName: ctrlResult.result.packageName]
                        return
                        break
                }
            }
            else {
                flash.message = ctrlResult.result.message
                ctrlResult.result
            }
        }
    }

    /**
     * Call to process the submitted input and to link the given package to the given package(s)
     * @return a redirect, either to the title selection view or to the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processLinkPackage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processLinkPackage(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            if(params.addUUID) {
                switch(params.addType) {
                    case "With": flash.message = message(code:'subscription.details.link.processingWithEntitlements') as String
                        redirect action: 'index', params: [id: params.id, gokbId: params.addUUID]
                        return
                        break
                    case "Without": flash.message = message(code:'subscription.details.link.processingWithoutEntitlements') as String
                        redirect action: 'addEntitlements', params: [id: params.id, packageLinkPreselect: params.addUUID, preselectedName: ctrlResult.result.packageName]
                        return
                        break
                }
            }
        }
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Call to unlink the given package from the given subscription
     * @return the list of conflicts, if no confirm has been submitted; the redirect to the subscription details page if confirm has been sent
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def unlinkPackage() {
        Map<String, Object> ctrlResult = subscriptionControllerService.unlinkPackage(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                redirect(url: request.getHeader('referer'))
            }
        }
        else {
            if (params.confirmed) {
                flash.message = ctrlResult.result.message
                redirect(url: request.getHeader('referer'))
            }
            else {

                render(template: "unlinkPackageModal", model: [pkg: ctrlResult.result.package, subscription: ctrlResult.result.subscription, conflicts_list: ctrlResult.result.conflict_list])
            }
        }
    }

    //-------------------------------- issue entitlements holding --------------------------------------

    /**
     * Call to list the current title holding of the subscription. The list may be displayed as HTML table
     * or be exported as KBART or Excel worksheet
     * @return a list of the current subscription stock; either as HTML output or as KBART / Excel table
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def index() {
        Map<String,Object> ctrlResult = subscriptionControllerService.index(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            String filename = "${escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention())}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
            if (params.exportKBart) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
                response.contentType = "text/tab-separated-values"
                ServletOutputStream out = response.outputStream
                Map<String, Object> configMap = [:]
                configMap.putAll(params)
                configMap.sub = ctrlResult.result.subscription
                configMap.pkgIds = ctrlResult.result.subscription.packages?.pkg?.id //GORM sometimes does not initialise the sorted set
                Map<String, List> tableData = exportService.generateTitleExportKBART(configMap, IssueEntitlement.class.name)
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
                }
                out.flush()
                out.close()
            }
            else if(params.exportXLSX) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Map<String, Object> configMap = [:]
                configMap.putAll(params)
                configMap.sub = ctrlResult.result.subscription
                configMap.pkgIds = ctrlResult.result.subscription.packages?.pkg?.id //GORM sometimes does not initialise the sorted set
                Map<String,List> export = exportService.generateTitleExportCustom(configMap, IssueEntitlement.class.name) //subscription given, all packages
                Map sheetData = [:]
                sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
                SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
                workbook.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                workbook.dispose()
                return
            }
            else if(params.exportClickMeExcel) {
                if (params.filename) {
                    filename =params.filename
                }

                ArrayList<IssueEntitlement> issueEntitlements = ctrlResult.result.entitlementIDs ? IssueEntitlement.findAllByIdInList(ctrlResult.result.entitlementIDs.id,[sort:'tipp.sortname']) : [:]

                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                Map<String, Object> selectedFields = [:]
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportIssueEntitlements(issueEntitlements, selectedFields)
                response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            }
            else {
                withFormat {
                    html {
                        flash.message = ctrlResult.result.message
                        ctrlResult.result
                    }
                    csv {
                        response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                        response.contentType = "text/csv"
                        ServletOutputStream out = response.outputStream
                        Map<String,List> tableData = exportService.generateTitleExportCSV(ctrlResult.result.entitlementIDs.id,IssueEntitlement.class.name)
                        out.withWriter { writer ->
                            writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.rows,';'))
                        }
                        out.close()
                    }
                }
            }
        }
    }

    /**
     * Call to load the applied or pending changes to the given subscription
     * @return the called tab with the changes of the given event type
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    @Check404()
    def entitlementChanges() {
        Map<String,Object> ctrlResult = subscriptionControllerService.entitlementChanges(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            ctrlResult.result
        }
    }

    /**
     * Call to list those titles of the package which have not been added to the subscription yet. The view
     * may be exportes as KBART or Excel worksheet as well. The view contains also enrichment functionalities
     * such as preselection of titles based on identifiers or adding locally negotiated prices or coverage statements
     * @return the list view of entitlements, either as HTML table or KBART / Excel worksheet export
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def addEntitlements() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addEntitlements(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            String filename = "${escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention())}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
            Map<String, Object> configMap = [:]
            configMap.putAll(params)
            configMap.remove("subscription")
            configMap.pkgIds = ctrlResult.result.subscription.packages?.pkg?.id //GORM sometimes does not initialise the sorted set
            if(params.exportKBart) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
                response.contentType = "text/tsv"
                ServletOutputStream out = response.outputStream
                Map<String,List> tableData = exportService.generateTitleExportKBART(configMap, TitleInstancePackagePlatform.class.name)
                out.withWriter { writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.columnData,'\t'))
                }
                out.flush()
                out.close()
            }
            else if(params.exportXLSX) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Map<String,List> export = exportService.generateTitleExportCustom(configMap, TitleInstancePackagePlatform.class.name) //subscription given
                Map sheetData = [:]
                sheetData[message(code:'menu.my.titles')] = [titleRow:export.titles,columnData:export.rows]
                SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
                workbook.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                workbook.dispose()
                return
            }else if(params.exportClickMeExcel) {
                if (params.filename) {
                    filename =params.filename
                }

                ArrayList<IssueEntitlement> tipps = ctrlResult.result.tipps ? TitleInstancePackagePlatform.findAllByIdInList(ctrlResult.result.tipps,[sort:'tipp.sortname']) : [:]

                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                Map<String, Object> selectedFields = [:]
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportTipps(tipps, selectedFields)
                response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            }
            withFormat {
                html {
                    flash.message = ctrlResult.result.message
                    flash.error = ctrlResult.result.error
                    ctrlResult.result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    Map<String,List> tableData = exportService.generateTitleExportCSV(ctrlResult.result.tipps,TitleInstancePackagePlatform.class.name)
                    out.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(tableData.titleRow,tableData.rows,';'))
                    }
                    out.flush()
                    out.close()
                }
            }
        }
    }

    /**
     * Call to remove the given issue entitlement from the subscription's holding
     * @return the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeEntitlement() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeEntitlement(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.delete.error.general.message') as String
        else {
            Object[] args = [message(code:'issueEntitlement.label'),params.ieid]
            flash.message = message(code: 'default.deleted.message',args: args) as String
        }
        redirect action: 'index', id: params.sub
    }

    /**
     * Call to remove an issue entitlement along with his title group record
     * @return the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeEntitlementWithIEGroups() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeEntitlementWithIEGroups(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.delete.error.general.message') as String
        else {
            Object[] args = [message(code:'issueEntitlement.label'),params.ieid]
            flash.message = message(code: 'default.deleted.message',args: args) as String
        }
        redirect action: 'index', id: params.sub
    }

    /**
     * Call to persist the cached data and create the issue entitlement holding based on that data
     * @return the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddEntitlements() {

        Map<String,Object> ctrlResult = subscriptionControllerService.processAddEntitlements(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            flash.error = ctrlResult.result.error
            flash.message = ctrlResult.result.message
        }
        redirect action: 'index', id: ctrlResult.result.subscription.id
    }

    /**
     * Call to delete the given entitlement record from the given renewal
     * @return the entitlement renewal view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRemoveEntitlements() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401)
            return
        }
        if(subscriptionService.deleteEntitlement(result.subscription,params.singleTitle))
            log.debug("Deleted tipp ${params.singleTitle} from sub ${result.subscription.id}")
        redirect action: 'index', id: result.subscription.id
    }

    /**
     * Call to pick the given title to the following year's holding. Technically, it adds the title to the
     * subscription's holding, but it is not fixed as the holding is under negotiation
     * @return a redirect to the referer
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processAddIssueEntitlementsSurvey() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableSurvey(result.institution, result.surveyConfig.surveyInfo)
        if (result.subscription) {
            if(params.singleTitle) {
                IssueEntitlement ie = IssueEntitlement.get(params.singleTitle)
                TitleInstancePackagePlatform tipp = ie.tipp

                boolean tippExistsInParentSub = false

                if(IssueEntitlement.findByTippAndSubscriptionAndStatus(tipp, result.surveyConfig.subscription, RDStore.TIPP_STATUS_CURRENT)) {
                    tippExistsInParentSub = true
                }else {
                   List<TitleInstancePackagePlatform> titleInstancePackagePlatformList = TitleInstancePackagePlatform.findAllByHostPlatformURLAndStatus(tipp.hostPlatformURL, RDStore.TIPP_STATUS_CURRENT)
                    titleInstancePackagePlatformList.each { TitleInstancePackagePlatform titleInstancePackagePlatform ->
                        if(IssueEntitlement.findByTippAndSubscriptionAndStatus(titleInstancePackagePlatform, result.surveyConfig.subscription, RDStore.TIPP_STATUS_CURRENT)) {
                            tippExistsInParentSub = true
                            tipp = titleInstancePackagePlatform
                        }
                    }
                }

                if(tippExistsInParentSub) {
                    try {

                        RefdataValue acceptStatus = RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION

                        if(result.contextOrg.id == result.surveyConfig.surveyInfo.owner.id && SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, result.subscription.subscriber).finishDate != null){
                            acceptStatus = RDStore.IE_ACCEPT_STATUS_UNDER_NEGOTIATION
                        }

                        if (subscriptionService.addEntitlement(result.subscription, tipp.gokbId, ie, (ie.priceItems != null), acceptStatus, result.surveyConfig.pickAndChoosePerpetualAccess)) {
                            flash.message = message(code: 'subscription.details.addEntitlements.titleAddToSub', args: [tipp.name]) as String
                        }
                    }
                    catch (EntitlementCreationException e) {
                        flash.error = e.getMessage()
                    }
                }
            }
        } else {
            log.error("Unable to locate subscription instance")
        }

        redirect(url: request.getHeader("referer"))

    }

    /**
     * Call to remove the given title from the picked titles
     * @return a redirect to the referer
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRemoveIssueEntitlementsSurvey() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableSurvey(result.institution, result.surveyConfig.surveyInfo)
        if(subscriptionService.deleteEntitlementbyID(result.subscription,params.singleTitle))
            log.debug("Deleted ie ${params.singleTitle} from sub ${result.subscription.id}")
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Call to trigger the revertion of holding status to the end of the subscription's year ring
     * @return a redirect to the referer
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR")})
    def resetHoldingToSubEnd() {
        Map<String, Object> ctrlResult = subscriptionControllerService.resetHoldingToSubEnd(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code: ctrlResult.result.errMess) as String
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Call for a batch update on the given subscription's holding
     * @return the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def subscriptionBatchUpdate() {
        Map<String,Object> ctrlResult = subscriptionControllerService.subscriptionBatchUpdate(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = message(code:'default.save.error.general.message') as String
                redirect action: 'index', params: [id: ctrlResult.result.subscription.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max, status: params.status]
                return
            }
        }
        else {
            redirect action: 'index', params: [id: ctrlResult.result.subscription.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max, status: params.status]
            return
        }
    }

    /**
     * Call to add a new price item to the issue entitlement
     * @return the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def addEmptyPriceItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addEmptyPriceItem(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'index', id: params.id
    }

    /**
     * Call to remove a price item from the issue entitlement
     * @return the issue entitlement holding view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removePriceItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removePriceItem(params)
        Object[] args = [message(code:'tipp.price'), params.priceItem]
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = message(code: 'default.not.found.message', args: args) as String
        }
        else
        {
            flash.message = message(code:'default.deleted.message', args: args) as String
        }
        redirect action: 'index', id: params.id
    }

    /**
     * Call to add a new coverage statement to the issue entitlement
     * @return the issue entitlement holding view
     */
    @DebugInfo(test='hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def addCoverage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.addCoverage(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
            redirect action: 'index', id: ctrlResult.result.subId, params: params
    }

    /**
     * Call to remove a coverage statement from the issue entitlement
     * @return the issue entitlement holding view
     */
    @DebugInfo(test='hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeCoverage() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeCoverage(params)
        Object[] args = [message(code:'tipp.coverage'), params.ieCoverage]
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = message(code: 'default.not.found.message', args: args) as String
        }
        else
        {
            flash.message = message(code:'default.deleted.message', args: args) as String
        }
        redirect action: 'index', id: params.id, params: params
    }

    /**
     * Call to list the current title groups of the subscription
     * @return the list of title groups for the given subscription
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def manageEntitlementGroup() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.titleGroups = result.subscription.ieGroups
        result
    }

    /**
     * Call to edit the given title group
     * @return either the edit view or the index view, when form data has been submitted
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def editEntitlementGroupItem() {
        Map<String,Object> ctrlResult = subscriptionControllerService.editEntitlementGroupItem(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            redirect action: 'index', id: params.id
            return
        }
        else {
            if(params.cmd == 'edit') {
                render template: 'editEntitlementGroupItem', model: ctrlResult.result
            }
            else {
                redirect action: 'index', id: params.id
                return
            }
        }
    }

    /**
     * Call to create the given title group for the given subscription
     * @return the title group view for the given subscription
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processCreateEntitlementGroup() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processCreateEntitlementGroup(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        redirect action: 'manageEntitlementGroup', id: params.id
    }

    /**
     * Call to remove the given title group from the given subscription
     * @return the title group view for the given subscription
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def removeEntitlementGroup() {
        Map<String, Object> ctrlResult = subscriptionControllerService.removeEntitlementGroup(params)
        Object[] args = [message(code:'issueEntitlementGroup.label'),params.titleGroup]
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            flash.error = message(code: 'default.not.found.message', args: args) as String
        }
        else
        {
            flash.message = message(code:'default.deleted.message', args: args) as String
        }
        redirect action: 'manageEntitlementGroup', id: params.sub
    }

    @Deprecated
    @Secured(['ROLE_ADMIN'])
    Map renewEntitlements() {
        params.id = params.targetObjectId
        params.sourceObjectId = genericOIDService.resolveOID(params.targetObjectId)?.instanceOf?.id
        //Map result = copyElementsService.loadDataFor_PackagesEntitlements()
        //result.comparisonMap = comparisonService.buildTIPPComparisonMap(result.sourceIEs+result.targetIEs)
        result
    }

    @Deprecated
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRenewEntitlements() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processRenewEntitlements(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
            }
        }
        else {
            flash.message = ctrlResult.result.message
        }
        redirect action: 'index', id: params.id
    }

    @Deprecated
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def showEntitlementsRenewWithSurvey() {
        Map<String,Object> result = [user: contextService.getUser()]//subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.contextOrg = contextService.getOrg()
        result.institution = result.contextOrg
        SwissKnife.setPaginationParams(result,params,result.user)
        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo
        result.subscription =  result.surveyConfig.subscription
        result.newSub = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result.institution)
        result.subscriber = result.newSub.getSubscriber()
        result.ieIDs = subscriptionService.getIssueEntitlementIDsNotFixed(result.newSub)
        result.ies = result.ieIDs ? IssueEntitlement.findAllByIdInList(result.ieIDs.drop(result.offset).take(result.max)) : []
        result.filename = "renewEntitlements_${escapeService.escapeString(result.subscription.dropdownNamingConvention())}"
        if (params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${result.filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, List> tableData = exportService.generateTitleExportKBART([sub: result.newSub, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT, pkgIds: result.newSub.packages?.pkg?.id], IssueEntitlement.class.name)
            out.withWriter { Writer writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
        }
        else if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=${result.filename}.xlsx")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            Map<String,List> export = exportService.generateTitleExportCustom([sub: result.newSub, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT, pkgIds: result.newSub.packages?.pkg?.id],IssueEntitlement.class.name)
            Map sheetData = [:]
            sheetData[g.message(code:'subscription.details.renewEntitlements.label')] = [titleRow:export.titles,columnData:export.rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }
        else {
            withFormat {
                html {
                    result
                }
            }
        }
    }

    /**
     * Call to load the selection list for the title renewal. The list may be exported as a (configurable) Excel table with usage data for each title
     * @return the title list for selection; either as HTML table or as Excel export, configured with the given parameters
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def renewEntitlementsWithSurvey() {
        Map<String,Object> ctrlResult
        params.statsForSurvey = true
        SXSSFWorkbook wb
        if(params.exportXLSStats) {
            params.tab = params.tabStat
            wb = exportService.generateReport(params, true,  true, true)
        }
        else {
            ctrlResult = subscriptionControllerService.renewEntitlementsWithSurvey(this, params)
        }
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                ctrlResult.result
            }
        }
        else {
            Map queryMap = [:]
            String filename
            if(params.tab == 'allIEs') {
                queryMap = [sub: ctrlResult.result.subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT, pkgIds: ctrlResult.result.subscription.packages?.pkg?.id]
                filename = escapeService.escapeString(message(code: 'renewEntitlementsWithSurvey.selectableTitles') + '_' + ctrlResult.result.subscription.dropdownNamingConvention())
            }
            if(params.tab == 'selectedIEs') {
                queryMap = [sub: ctrlResult.result.newSub, ieAcceptStatusNotFixed: true, ieStatus: RDStore.TIPP_STATUS_CURRENT, pkgIds: ctrlResult.result.subscription.packages?.pkg?.id]
                filename = escapeService.escapeString(message(code: 'renewEntitlementsWithSurvey.currentEntitlements') + '_' + ctrlResult.result.newSub.dropdownNamingConvention())
            }

            if(params.tab == 'currentIEs' && ctrlResult.result.previousSubscription) {
                Set<Subscription> subscriptions = []
                Set<Long> packageIds = []
                if(ctrlResult.result.surveyConfig.pickAndChoosePerpetualAccess) {
                    subscriptions = linksGenerationService.getSuccessionChain(ctrlResult.result.newSub, 'sourceSubscription')
                    subscriptions.each {
                        packageIds.addAll(it.packages?.pkg?.id)
                    }
                    subscriptions << ctrlResult.result.newSub
                    packageIds.addAll(ctrlResult.result.newSub.packages?.pkg?.id)
                }else {
                    subscriptions << ctrlResult.result.previousSubscription
                    packageIds.addAll(ctrlResult.result.previousSubscription.packages?.pkg?.id)
                }

                queryMap = [subscriptions: subscriptions, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT, pkgIds: packageIds]
                filename = escapeService.escapeString(message(code: 'renewEntitlementsWithSurvey.currentTitles') + '_' + ctrlResult.result.previousSubscription.dropdownNamingConvention())
            }

            List<Long> toBeSelectedTippIDs = []
            if(params.tab == 'toBeSelectedIEs') {
                List<Long> allTippIDs = IssueEntitlement.executeQuery("select ie.tipp.id from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus", [sub: ctrlResult.result.subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])

                List<Long> selectedTippIDs =  IssueEntitlement.executeQuery("select ie.tipp.id from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus ", [sub: ctrlResult.result.newSub, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                toBeSelectedTippIDs.addAll(allTippIDs - selectedTippIDs)
                if(toBeSelectedTippIDs.size()) {
                    queryMap = [sub: ctrlResult.result.subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT, tippIds: toBeSelectedTippIDs, pkgIds: ctrlResult.result.subscription.packages?.pkg?.id]
                }
                filename = escapeService.escapeString(message(code: 'renewEntitlementsWithSurvey.toBeSelectedIEs') + '_' + ctrlResult.result.newSub.dropdownNamingConvention())
            }

            if (params.exportKBart) {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
                response.contentType = "text/tsv"
                ServletOutputStream out = response.outputStream
                Map<String, List> tableData = exportService.generateTitleExportKBART(queryMap, IssueEntitlement.class.name)
                out.withWriter { Writer writer ->
                    writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
                }
                out.flush()
                out.close()
            }
            if (params.exportForImport) {

                List monthsInRing = []
                if(ctrlResult.result.showStatisticByParticipant) {
                    Calendar startTime = GregorianCalendar.getInstance(), endTime = GregorianCalendar.getInstance()
                    if (ctrlResult.result.newSub.startDate && ctrlResult.result.newSub.endDate) {
                        startTime.setTime(ctrlResult.result.newSub.startDate)
                        if (ctrlResult.result.newSub.endDate < new Date())
                            endTime.setTime(ctrlResult.result.newSub.endDate)
                    } else if (ctrlResult.result.newSub.startDate) {
                        startTime.setTime(ctrlResult.result.newSub.startDate)
                        endTime.setTime(new Date())
                    }
                    while (startTime.before(endTime)) {
                        monthsInRing << startTime.getTime()
                        startTime.add(Calendar.MONTH, 1)
                    }
                }
                List<String> perpetuallyPurchasedTitleURLs = TitleInstancePackagePlatform.executeQuery('select tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (select oo.sub from OrgRole oo where oo.org = :org and oo.roleType in (:roleTypes)) and ie.acceptStatus = :acceptStatus and tipp.status = :tippStatus and ie.status = :tippStatus and ie.perpetualAccessBySub is not null',
                [org: ctrlResult.result.subscriber, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED, tippStatus: RDStore.TIPP_STATUS_CURRENT, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS]])

                response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Map<String, List> export = exportService.generateTitleExportCustom(queryMap, IssueEntitlement.class.name, monthsInRing.sort { Date monthA, Date monthB -> monthA <=> monthB }, ctrlResult.result.subscriber, perpetuallyPurchasedTitleURLs)
                export.titles << "Pick"

                Map sheetData = [:]
                sheetData[g.message(code: 'renewEntitlementsWithSurvey.selectableTitles')] = [titleRow: export.titles, columnData: export.rows]
                wb = exportService.generateXLSXWorkbook(sheetData)
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            }
            else if (params.exportXLS) {
                List<String> perpetuallyPurchasedTitleURLs = TitleInstancePackagePlatform.executeQuery('select tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (select oo.sub from OrgRole oo where oo.org = :org and oo.roleType in (:roleTypes)) and ie.acceptStatus = :acceptStatus and tipp.status = :tippStatus and ie.status = :tippStatus and ie.perpetualAccessBySub is not null',
                        [org: ctrlResult.result.subscriber, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED, tippStatus: RDStore.TIPP_STATUS_CURRENT, roleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS]])
                response.setHeader("Content-disposition", "attachment; filename=${filename}.xlsx")
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                Map<String, List> export = exportService.generateTitleExportCustom(queryMap, IssueEntitlement.class.name, [], null, perpetuallyPurchasedTitleURLs)
                Map sheetData = [:]
                sheetData[g.message(code: 'renewEntitlementsWithSurvey.selectableTitles')] = [titleRow: export.titles, columnData: export.rows]
                wb = exportService.generateXLSXWorkbook(sheetData)
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            } else if (params.exportXLSStats) {
                    if(wb) {
                        response.setHeader "Content-disposition", "attachment; filename=report_${DateUtils.getSDF_yyyyMMdd().format(ctrlResult.result.dateRun)}.xlsx"
                        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        wb.write(response.outputStream)
                        response.outputStream.flush()
                        response.outputStream.close()
                        wb.dispose()
                        return
                    }
            }else {

                if(params.tab in ['allIEsStats', 'holdingIEsStats']) {
                    params.metricType = ctrlResult.result.metricType
                    params.reportType = ctrlResult.result.reportType
                }
                ctrlResult.result
            }
        }
    }

    /**
     * Call to process the title selection with the given input parameters
     * @return a redirect to the referer
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def processRenewEntitlementsWithSurvey() {
        Map<String, Object> ctrlResult = subscriptionControllerService.processRenewEntitlementsWithSurvey(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else flash.error = ctrlResult.result.error
        }
        else {
            flash.message = ctrlResult.result.message
        }
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Takes the given configuration map and updates the pending change behavior for the given subscription package
     * @return the (updated) subscription details view
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    @Check404()
    def setupPendingChangeConfiguration() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(!result) {
            response.sendError(403)
                return
        }
        log.debug("Received params: ${params}")
        subscriptionService.addPendingChangeConfiguration(result.subscription, Package.get(params.pkg), params.clone())
        redirect(action:'show', params:[id:params.id])
    }

    /* TODO Cost per use tab, still needed?
    @DebugInfo(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def costPerUse() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        // Can we remove this block?
        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
            result.institutional_usage_identifier = OrgSetting.get(result.institution, OrgSetting.KEYS.NATSTAT_SERVER_REQUESTOR_ID)
        }

        // Get a unique list of invoices
        // select inv, sum(cost) from costItem as ci where ci.sub = x
        log.debug("Get all invoices for sub ${result.subscription}");
        result.costItems = []
        CostItem.executeQuery(INVOICES_FOR_SUB_HQL, [sub: result.subscription]).each {

            log.debug(it);

            def cost_row = [invoice: it[0], total: it[2]]

            cost_row.total_cost_for_sub = it[2];

            if (it && (it[3]?.startDate) && (it[3]?.endDate)) {

                log.debug("Total costs for sub : ${cost_row.total_cost_for_sub} period will be ${it[3]?.startDate} to ${it[3]?.endDate}");

                def usage_str = Fact.executeQuery(TOTAL_USAGE_FOR_SUB_IN_PERIOD, [
                        start   : it[3].startDate,
                        end     : it[3].endDate,
                        sub     : result.subscription,
                        factType: 'STATS:JR1'])[0]

                if (usage_str && usage_str.trim().length() > 0) {
                    cost_row.total_usage_for_sub = Double.parseDouble(usage_str);
                    if (cost_row.total_usage_for_sub > 0) {
                        cost_row.overall_cost_per_use = cost_row.total_cost_for_sub / cost_row.total_usage_for_sub;
                    } else {
                        cost_row.overall_cost_per_use = 0;
                    }
                } else {
                    cost_row.total_usage_for_sub = Double.parseDouble('0');
                    cost_row.overall_cost_per_use = cost_row.total_usage_for_sub
                }

                // Work out what cost items appear under this subscription in the period given
                cost_row.usage = Fact.executeQuery(USAGE_FOR_SUB_IN_PERIOD, [start: it[3].startDate, end: it[3].endDate, sub: result.subscription, jr1a: 'STATS:JR1'])
                cost_row.billingCurrency = it[3].billingCurrency.value.take(3)
                result.costItems.add(cost_row);
            } else {
                log.error("Invoice ${it} had no start or end date");
            }
        }

        result
    }*/

    //--------------------------------------------- renewal section ---------------------------------------------

    /**
     * Call for manual renewal of a given subscription, i.e. without performing a renewal survey before
     * @return the starting page of the subscription renewal process
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def renewSubscription() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Subscription subscription = Subscription.get(params.baseSubscription ?: params.id)
        result.subscription = subscription
        SimpleDateFormat sdf = DateUtils.getSDF_ddMMyyyy()
        Date newStartDate
        Date newEndDate
        use(TimeCategory) {
            newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
            newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
        }
        result.isRenewSub = true
        result.permissionInfo = [sub_startDate: newStartDate ? sdf.format(newStartDate) : null,
                                 sub_endDate  : newEndDate ? sdf.format(newEndDate) : null,
                                 sub_name     : subscription.name,
                                 sub_id       : subscription.id,
                                 sub_status   : RDStore.SUBSCRIPTION_INTENDED.id.toString()]
        result
    }

    /**
     * Takes the given base data, creates the successor subscription instance and initialises elements
     * copying process
     * @return the first page of the element copy processing
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processRenewSubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processRenewSubscription(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = ctrlResult.result.error
                if(ctrlResult.result.newSub)
                    ctrlResult.result.newSub
                redirect action: 'renewSubscription', params: [id: ctrlResult.result.subscription.id]
            }
        }
        else {
            redirect controller: 'subscription',
                    action: 'copyElementsIntoSubscription',
                    params: [sourceObjectId: genericOIDService.getOID(ctrlResult.result.subscription), targetObjectId: genericOIDService.getOID(ctrlResult.result.newSub), isRenewSub: true]
            return
        }
    }

    //------------------------------------------------ copy section ---------------------------------------------

    /**
     * Call to load the given section of subscription copying procedure
     * @return the view with the given copy parameters, depending on the tab queried
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copySubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.copySubscription(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else flash.error = ctrlResult.result.error
        }
        else {
            switch (params.workFlowPart) {
                case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                    if(ctrlResult.result.targetObject) {
                        params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                    }
                    ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    break
                case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                    ctrlResult.result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                    params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                    ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    break
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                    if (ctrlResult.result.isConsortialObjects && accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER")){
                        params.workFlowPart = CopyElementsService.WORKFLOW_SUBSCRIBER
                        ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                    } else {
                        params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                        ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_SUBSCRIBER:
                    ctrlResult.result << copyElementsService.copyObjectElements_Subscriber(params)
                    params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                    ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    break
                case CopyElementsService.WORKFLOW_END:
                    ctrlResult.result << copyElementsService.copyObjectElements_Properties(params)
                    if (ctrlResult.result.targetObject){
                        redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.targetObject.id]
                        return
                    }
                    break
                default:
                    ctrlResult.result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                    break
            }
            ctrlResult.result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
            ctrlResult.result
        }
    }

    /**
     * Call to load data for the given step (by browsing in the copySubscription() tabs or by submitting values and eventually
     * turning to the next page); if data has been submitted, it will be processed
     * @return the copy parameters for the given (or its following) procedure section
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyElementsIntoSubscription() {
        Map<String,Object> ctrlResult = subscriptionControllerService.copyElementsIntoSubscription(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else flash.error = ctrlResult.result.error
        }
        else {
            if(ctrlResult.result.transferIntoMember && params.workFlowPart in [CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS, CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS])
                params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
            switch (params.workFlowPart) {
                case CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DatesOwnerRelations(params)
                    if (params.isRenewSub){
                        params.workFlowPart = CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                        ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                    ctrlResult.result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                    if (params.isRenewSub){
                        params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                        ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                    if (params.isRenewSub){
                        if (!params.fromSurvey && ctrlResult.result.isSubscriberVisible){
                            params.workFlowPart = CopyElementsService.WORKFLOW_SUBSCRIBER
                            ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                        } else {
                            params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                            ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                        }
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_SUBSCRIBER:
                    ctrlResult.result << copyElementsService.copyObjectElements_Subscriber(params)
                    if (params.isRenewSub) {
                        params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                        ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_PROPERTIES:
                    ctrlResult.result << copyElementsService.copyObjectElements_Properties(params)
                    if(!(params.isRenewSub && ctrlResult.result.targetObject)) {
                        ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_END:
                    ctrlResult.result << copyElementsService.copyObjectElements_Properties(params)
                    break
                default:
                    if(ctrlResult.result.transferIntoMember)
                        ctrlResult.result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    else
                        ctrlResult.result << copyElementsService.loadDataFor_DatesOwnerRelations(params)
                    break
            }
            ctrlResult.result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DATES_OWNER_RELATIONS
            ctrlResult.result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
            if (params.isRenewSub) {
                ctrlResult.result.isRenewSub = params.isRenewSub
            }
            if(params.workFlowPart == CopyElementsService.WORKFLOW_END && ctrlResult.result.targetObject) {
                SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(ctrlResult.result.sourceObject, true)
                if (surveyConfig && ctrlResult.result.fromSurvey) {
                    redirect controller: 'survey', action: 'compareMembersOfTwoSubs', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id]
                    return
                }
                else {
                    redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.targetObject.id]
                    return
                }
            }
            else ctrlResult.result
        }
    }

    /**
     * Call for a single user to copy private properties from a consortial member subscription into its successor instance
     * @return the reduced subscription element copy view
     */
    @DebugInfo(perm = "ORG_INST", affil = "INST_EDITOR", specRole = "ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyMyElements() {
        Map<String, Object> result = subscriptionControllerService.setCopyResultGenerics(params+[copyMyElements: true])
        if (!result) {
            response.sendError(401)
                return
        }
        else {
            result.allObjects_readRights = subscriptionService.getMySubscriptionsWithMyElements_readRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
            result.allObjects_writeRights = subscriptionService.getMySubscriptionsWithMyElements_writeRights([status: RDStore.SUBSCRIPTION_CURRENT.id])
            switch (params.workFlowPart) {
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    result << copyElementsService.copyObjectElements_DocsAnnouncementsTasks(params)
                    result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    break
                case CopyElementsService.WORKFLOW_PROPERTIES:
                    result << copyElementsService.copyObjectElements_Properties(params)
                    result << copyElementsService.loadDataFor_Properties(params)
                    break
                case CopyElementsService.WORKFLOW_END:
                    result << copyElementsService.copyObjectElements_Properties(params)
                    if (result.targetObject){
                        flash.error = ""
                        flash.message = ""
                        redirect controller: 'subscription', action: 'show', params: [id: result.targetObject.id]
                        return
                    }
                    break
                default:
                    result << copyElementsService.loadDataFor_DocsAnnouncementsTasks(params)
                    break
            }
            if (params.targetObjectId) {
                result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
            }
            result.workFlowPart = params.workFlowPart ?: CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
            result.workFlowPartNext = params.workFlowPartNext ?: CopyElementsService.WORKFLOW_PROPERTIES
            result
        }
    }

    //----------------------------------------- subscription import section -----------------------------------------

    /**
     * Processes the given subscription candidates and creates subscription instances based on the submitted data
     * @return the subscription list view in case of success, the import starting page otherwise
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSubscriptions() {
        def candidates = JSON.parse(params.candidates)
        List errors = subscriptionService.addSubscriptions(candidates,params)
        if(errors.size() > 0) {
            flash.error = errors.join("<br/>")
            redirect controller: 'myInstitution', action: 'subscriptionImport'
            return
        }
        else {
            redirect controller: 'myInstitution', action: 'currentSubscriptions'
            return
        }
    }

    //--------------------------------------------- reporting -------------------------------------------------

    /**
     * Call for the reporting view for the given subscription
     * @return the reporting index for the subscription
     */
    @DebugInfo(perm="ORG_CONSORTIUM,ORG_INST", affil="INST_USER")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM,ORG_INST", "INST_USER") })
    @Check404()
    def reporting() {
        if (! params.token) {
            params.token = 'static#' + params.id
        }
        Map<String,Object> ctrlResult = subscriptionControllerService.reporting( params )

        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
                return
        }
        else {
            render view: 'reporting/index', model: ctrlResult.result
        }
    }

    //--------------------------------------------- workflows -------------------------------------------------

    /**
     * Call for the workflows related to this subscription
     * @return the workflow landing page for the given subscription
     */
    @DebugInfo(perm="ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_USER") })
    @Check404()
    def workflows() {
        Map<String,Object> ctrlResult = subscriptionControllerService.workflows( params )

        render view: 'workflows', model: ctrlResult.result
    }

    //--------------------------------------------- helper section -------------------------------------------------

}
