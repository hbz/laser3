package de.laser

import de.laser.addressbook.Contact
import de.laser.annotations.Check404
import de.laser.annotations.DebugInfo
import de.laser.auth.User
import de.laser.cache.EhcacheWrapper
import de.laser.config.ConfigMapper
import de.laser.ctrl.SubscriptionControllerService
import de.laser.helper.FilterLogic
import de.laser.helper.Profiler
import de.laser.interfaces.CalculatedType
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.remote.Wekb
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.utils.DateUtils
import de.laser.utils.PdfUtils
import de.laser.utils.RandomUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.TitleInstancePackagePlatform
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.apache.http.HttpStatus
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.mozilla.universalchardet.UniversalDetector
import org.springframework.web.multipart.MultipartFile

import javax.servlet.ServletOutputStream
import java.text.SimpleDateFormat
import java.time.Year
import java.util.concurrent.ExecutorService

/**
 * This controller is responsible for the subscription handling. Many of the controller calls do
 * also data manipulation; they thus needed to be wrapped in a transactional service for that database
 * actions are being persisted
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class SubscriptionController {

    CacheService cacheService
    ContextService contextService
    CopyElementsService copyElementsService
    CustomerTypeService customerTypeService
    DeletionService deletionService
    DocstoreService docstoreService
    EscapeService escapeService
    ExecutorService executorService
    ExportClickMeService exportClickMeService
    ExportService exportService
    FilterService filterService
    GenericOIDService genericOIDService
    GlobalService globalService
    GokbService gokbService
    IssueEntitlementService issueEntitlementService
    LinksGenerationService linksGenerationService
    SubscriptionControllerService subscriptionControllerService
    SubscriptionService subscriptionService
    SurveyService surveyService
    TaskService taskService
    TitleService titleService

    //-----

    /**
     * Map containing menu alternatives if an unexisting object has been called
     */
    public static final Map<String, String> CHECK404_ALTERNATIVES = [
            'myInstitution/currentSubscriptions' : 'myinst.currentSubscriptions.label'
    ]

    //-------------------------------------- general or ungroupable section -------------------------------------------

    /**
     * Call to show the details of the given subscription
     * @return the subscription details view
     */
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404()
    def show() {
        Map<String,Object> ctrlResult = subscriptionControllerService.show(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            if(params.export) {
                ctrlResult.result.availablePropDefGroups = PropertyDefinitionGroup.getAvailableGroups(ctrlResult.result.institution, Subscription.class.name)
                ctrlResult.result.allPropDefGroups = ctrlResult.result.subscription.getCalculatedPropDefGroups(ctrlResult.result.institution)
                ctrlResult.result.prop_desc = PropertyDefinition.SUB_PROP
                ctrlResult.result.memberSubscriptions = OrgRole.executeQuery('select sub from OrgRole oo join oo.sub sub join oo.org org where sub.instanceOf = :parent and oo.roleType = :subscriberCons order by org.sortname', [parent: ctrlResult.result.subscription, subscriberCons: RDStore.OR_SUBSCRIBER_CONS])
                ctrlResult.result.linkedLicenses = Subscription.executeQuery('select lic from Links li join li.sourceLicense lic join lic.orgRelations oo where li.destinationSubscription = :sub and li.linkType = :linkType and lic.status = :current and oo.org = :context', [sub: ctrlResult.result.subscription, linkType: RDStore.LINKTYPE_LICENSE, current: RDStore.LICENSE_CURRENT, context: ctrlResult.result.institution])
                ctrlResult.result.links = linksGenerationService.getSourcesAndDestinations(ctrlResult.result.subscription, ctrlResult.result.user, RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE)-RDStore.LINKTYPE_LICENSE)
                ctrlResult.result.entry = ctrlResult.result.subscription
                ctrlResult.result.tasks = taskService.getTasksForExport((User) ctrlResult.result.user, (Subscription) ctrlResult.result.subscription)
                ctrlResult.result.documents = docstoreService.getDocumentsForExport((Org) ctrlResult.result.institution, (Subscription) ctrlResult.result.subscription)
                ctrlResult.result.notes = docstoreService.getNotesForExport((Org) ctrlResult.result.institution, (Subscription) ctrlResult.result.subscription)

                byte[] pdf = PdfUtils.getPdf(
                        ctrlResult.result as Map<String, Object>,
                        PdfUtils.PORTRAIT_FIXED_A4,
                        customerTypeService.getCustomerTypeDependingView('/subscription/subscriptionPdf')
                )
                response.setHeader('Content-disposition', 'attachment; filename="'+ escapeService.escapeString(ctrlResult.result.subscription.dropdownNamingConvention()) +'.pdf"')
                response.setContentType('application/pdf')
                response.outputStream.withStream { it << pdf }
            }
            else {
                if(subscriptionService.checkThreadRunning('PackageTransfer_'+ctrlResult.result.subscription.id)) {
                    flash.message = message(code: 'subscription.details.linkPackage.thread.running.withPackage', args: [subscriptionService.getCachedPackageName('PackageTransfer_'+ctrlResult.result.subscription.id)] as Object[])
                }
                ctrlResult.result
            }
        }
    }

    /**
     * Call to open the subscription transfer steps to this subscription
     * @return the listing of the transfer steps for this subscription
     */
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
    @Check404()
    def subTransfer() {
        Map<String,Object> ctrlResult = subscriptionControllerService.subTransfer(params)
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
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
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

    /**
     * Call to prepare the usage data form for the given subscription
     * @return the filter for the given subscription
     */
    @DebugInfo(isInstUser_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    @Check404()
    def stats() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(params.error)
            result.error = params.error
        if(params.reportType)
            result.putAll(subscriptionControllerService.loadFilterList(params))
        result.flagContentGokb = true // gokbService.executeQuery
        Set<Platform> subscribedPlatforms = Platform.executeQuery("select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription", [subscription: result.subscription])
        /*
        if(!subscribedPlatforms) {
            subscribedPlatforms = Platform.executeQuery("select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :subscription", [subscription: result.subscription])
        }
        */
        Set<Subscription> refSubs = [result.subscription, result.subscription.instanceOf]
        result.platformInstanceRecords = [:]
        if(subscribedPlatforms) {
            result.platforms = subscribedPlatforms
            result.platformsJSON = subscribedPlatforms.laserID as JSON
            result.keyPairs = [:]
            if(!params.containsKey('tab'))
                params.tab = subscribedPlatforms[0].id.toString()
            result.subscription.packages.each { SubscriptionPackage sp ->
                Platform platformInstance = sp.pkg.nominalPlatform
                if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_LOCAL]) {
                    //create dummies for that they may be xEdited - OBSERVE BEHAVIOR for eventual performance loss!
                    CustomerIdentifier keyPair = CustomerIdentifier.findByPlatformAndCustomer(platformInstance, result.subscription.getSubscriberRespConsortia())
                    if(!keyPair) {
                        keyPair = new CustomerIdentifier(platform: platformInstance,
                                customer: result.subscription.getSubscriberRespConsortia(),
                                type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT,
                                owner: contextService.getOrg(),
                                isPublic: true)
                        if(!keyPair.save()) {
                            log.warn(keyPair.errors.getAllErrors().toListString())
                        }
                    }
                    result.keyPairs.put(platformInstance.gokbId, keyPair)
                }
                else {
                    result.subscription.getDerivedNonHiddenSubscribers().each { Org member ->
                        CustomerIdentifier keyPair = CustomerIdentifier.findByPlatformAndCustomer(platformInstance, member)
                        if(!keyPair) {
                            keyPair = new CustomerIdentifier(platform: platformInstance,
                                    customer: member,
                                    type: RDStore.CUSTOMER_IDENTIFIER_TYPE_DEFAULT,
                                    owner: contextService.getOrg(),
                                    isPublic: true)
                            if(!keyPair.save()) {
                                log.warn(keyPair.errors.getAllErrors().toListString())
                            }
                        }
                    }
                }
                Map queryResult = gokbService.executeQuery(Wekb.getSearchApiURL(), [uuid: platformInstance.gokbId])
                if (queryResult.error && queryResult.error == 404) {
                    result.wekbServerUnavailable = message(code: 'wekb.error.404')
                }
                else if (queryResult) {
                    List records = queryResult.result
                    if(records[0]) {
                        records[0].lastRun = platformInstance.counter5LastRun ?: platformInstance.counter4LastRun
                        records[0].id = platformInstance.id
                        result.platformInstanceRecords[platformInstance.gokbId] = records[0]
                        result.platformInstanceRecords[platformInstance.gokbId].wekbUrl = Wekb.getResourceShowURL() + "/${platformInstance.gokbId}"
                        if(records[0].statisticsFormat == 'COUNTER' && records[0].counterR4SushiServerUrl == null && records[0].counterR5SushiServerUrl == null) {
                            result.error = 'noSushiSource'
                            ArrayList<Object> errorArgs = ["${Wekb.getResourceShowURL()}/${platformInstance.gokbId}", platformInstance.name]
                            result.errorArgs = errorArgs.toArray()
                        }
                        else {
                            CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(result.subscription.getSubscriberRespConsortia(), platformInstance)
                            if(!ci?.value) {
                                if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_LOCAL])
                                    result.error = 'noCustomerId.local'
                                else
                                    result.error = 'noCustomerId'
                            }
                        }
                    }
                }
                if(result.subscription._getCalculatedType() != CalculatedType.TYPE_CONSORTIAL) {
                    //Set<String> tippUIDs = subscriptionControllerService.fetchTitles(params, refSubs, 'uids')
                    Map<String, Object> dateRangeParams = subscriptionControllerService.getDateRange(params, result.subscription)
                    result.reportTypes = []
                    CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(result.subscription.getSubscriberRespConsortia(), platformInstance)
                    if(ci?.value) {
                        Set allAvailableReports = subscriptionControllerService.getAvailableReports(result)
                        if(allAvailableReports)
                            result.reportTypes.addAll(allAvailableReports)
                        else {
                            result.error = 'noReportAvailable'
                        }
                    }
                    else if(!ci?.value) {
                        result.error = 'noCustomerId'
                    }
                }
            }
            result
        }
        else {
            flash.error = message(code: 'default.stats.error.noPlatformAvailable')
            redirect action: 'show', params: [id: params.id]
        }
    }

    /**
     * Call to process the given input data and create member subscription instances for the given consortial subscription
     * @return a redirect to the subscription members view in case of success or details view or to the member adding form otherwise
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def uploadRequestorIDs() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(!result) {
            response.sendError(401)
            return
        }
        MultipartFile importFile = params.requestorIDFile
        InputStream stream = importFile.getInputStream()
        result.putAll(subscriptionService.uploadRequestorIDs(Platform.get(params.platform), stream))
        if(result.truncatedRows){
            flash.message = message(code: 'subscription.details.addMembers.option.selectMembersWithFile.selectProcess.truncatedRows', args: [result.processCount, result.processRow, result.wrongOrgs, result.truncatedRows])
        }else if(result.wrongOrgs){
            flash.message = message(code: 'subscription.details.addMembers.option.selectMembersWithFile.selectProcess.wrongOrgs', args: [result.processCount, result.processRow, result.wrongOrgs])
        }else {
            flash.message = message(code: 'subscription.details.addMembers.option.selectMembersWithFile.selectProcess', args: [result.processCount, result.processRow])
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call to fetch the usage data for the given subscription
     * @return the (filtered) usage data view for the given subscription, rendered as Excel worksheet
     */
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def generateReport() {
        if(!params.reportType) {
            Map<String, Object> errorMap = [error: message(code: "default.stats.error.noReportSelected")]
            render template: '/templates/stats/usageReport', model: errorMap
        }
        else {
            Subscription sub = Subscription.get(params.id)
            String dateHash = "${params.startDate}${params.endDate}".encodeAsMD5()
            String token = "report_${params.reportType}_${params.platform}_${dateHash}_${sub.getSubscriberRespConsortia().id}_${sub.id}"
            if(params.metricType) {
                token += '_'+params.list('metricType').join('_')
            }
            if(params.accessType) {
                token += '_'+params.list('accessType').join('_')
            }
            if(params.accessMethod) {
                token += '_'+params.list('accessMethod').join('_')
            }
            String dir = ConfigMapper.getStatsReportSaveLocation() ?: '/usage'
            File folder = new File(dir)
            if (!folder.exists()) {
                folder.mkdir()
            }
            File f = new File(dir+'/'+token)
            Map<String, String> fileResult = [token: token]
            if(!f.exists()) {
                Map<String, Object> ctrlResult = exportService.generateReport(params)
                if(ctrlResult.containsKey('result')) {
                    SXSSFWorkbook wb = ctrlResult.result
                    /*
                    see DocumentController and https://stackoverflow.com/questions/24827571/how-to-convert-xssfworkbook-to-file
                     */
                    FileOutputStream fos = new FileOutputStream(f)
                    //--> to document
                    wb.write(fos)
                    fos.flush()
                    fos.close()
                    wb.dispose()
                    render template: '/templates/stats/usageReport', model: fileResult
                }
                else {
                    Map<String, Object> errorMap
                    if(ctrlResult.error.hasProperty('code'))
                        errorMap = [error: ctrlResult.error.code]
                    else
                        errorMap = [error: ctrlResult.error]
                    render template: '/templates/stats/usageReport', model: errorMap
                }
            }
            else {
                render template: '/templates/stats/usageReport', model: fileResult
            }
        }
    }

    /**
     * Call to fetch the usage data for the given subscription
     * @return the (filtered) usage data view for the given subscription, rendered as Excel worksheet
     */
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def downloadReport() {
        /*
        get file from token and offer to download with filename
         */
        byte[] output = []
        try {
            Date dateRun = new Date()
            String dir = ConfigMapper.getStatsReportSaveLocation() ?: '/usage'
            File f = new File(dir+'/'+params.token)
            output = f.getBytes()
            response.setHeader "Content-disposition", "attachment; filename=report_${DateUtils.getSDF_yyyyMMdd().format(dateRun)}.xlsx"
            response.setHeader("Content-Length", "${output.length}")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            response.outputStream << output
        }
        catch(Exception e) {
            log.error(e.getMessage())
            response.sendError(HttpStatus.SC_NOT_FOUND)
        }
    }

    /**
     * Call to unlink the given subscription from the given license
     * @return a redirect back to the referer
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def unlinkLicense() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            response.sendError(401)
            return
        }
        else {
            License lic = License.get(params.license)
            subscriptionService.setOrgLicRole(result.subscription,lic,true)
            if(params.unlinkWithChildren) {
                Subscription.findAllByInstanceOf(result.subscription).each { Subscription childSub ->
                    License.findAllByInstanceOf(lic).each { License childLic ->
                        subscriptionService.setOrgLicRole(childSub, childLic, true)
                    }
                    //eliminate stray connections - AWARE! It might cause blows later ...
                    Links.findAllByDestinationSubscriptionAndLinkTypeAndOwner(childSub, RDStore.LINKTYPE_LICENSE, contextService.getOrg()).each { Links li ->
                        li.delete()
                    }
                }
            }
            redirect(url: request.getHeader('referer'))
        }
    }

    /**
     * Call to unlink the given subscription from all linked license
     * @return a redirect back to the referer
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def unlinkAllLicenses() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if(!result) {
            response.sendError(401)
            return
        }
        else {
            Subscription.findAllByInstanceOf(result.subscription).each { Subscription childSub ->
                //eliminate stray connections - AWARE! It might cause blows later ...
                Links.findAllByDestinationSubscriptionAndLinkTypeAndOwner(childSub, RDStore.LINKTYPE_LICENSE, contextService.getOrg()).each { Links li ->
                    li.delete()
                }
            }
            redirect(url: request.getHeader('referer'))
        }
    }

    //--------------------------------------------- new subscription creation -----------------------------------------------------------

    /**
     * Call to create a new subscription
     * @return the empty subscription form or the list of subscriptions in case of an error
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
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
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
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
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def delete() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        if(result.subscription.instanceOf)
            result.parentId = result.subscription.instanceOf.id
        else if(result.subscription._getCalculatedType() in [CalculatedType.TYPE_PARTICIPATION, CalculatedType.TYPE_ADMINISTRATIVE])
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
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
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
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
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

    //--------------------------------- consortia members section ----------------------------------------------

    /**
     * Call to list the members of the given consortial subscription. The list may be rendered as direct HTML output
     * or exported as (configurable) Excel worksheet
     * @return a (filtered) view of the consortium members, either as HTML output or as Excel worksheet
     */
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
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
            Map<String, Object> selectedFields = [:]
            Set<String> contactSwitch = []
            if(params.fileformat) {
                if (params.filename) {
                    filename =params.filename
                }
                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
                contactSwitch.addAll(params.list("contactSwitch"))
                contactSwitch.addAll(params.list("addressSwitch"))
                SXSSFWorkbook wb
                switch(params.fileformat) {
                    case 'xlsx':
                        wb = (SXSSFWorkbook) exportClickMeService.exportSubscriptionMembers(ctrlResult.result.filteredSubChilds, selectedFields, ctrlResult.result.subscription, contactSwitch, ExportClickMeService.FORMAT.XLS)
                        response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        wb.write(response.outputStream)
                        response.outputStream.flush()
                        response.outputStream.close()
                        wb.dispose()
                        return
                    case 'pdf':
                        Map<String, Object> pdfOutput = exportClickMeService.exportSubscriptionMembers(ctrlResult.result.filteredSubChilds, selectedFields, ctrlResult.result.subscription, contactSwitch, ExportClickMeService.FORMAT.PDF)

                        byte[] pdf = PdfUtils.getPdf(pdfOutput, PdfUtils.LANDSCAPE_DYNAMIC, '/templates/export/_individuallyExportPdf')
                        response.setHeader('Content-disposition', 'attachment; filename="'+ filename +'.pdf"')
                        response.setContentType('application/pdf')
                        response.outputStream.withStream { it << pdf }
                        return
                    case 'csv':
                        response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                        response.contentType = "text/csv"
                        ServletOutputStream out = response.outputStream
                        out.withWriter { writer ->
                            writer.write((String) exportClickMeService.exportSubscriptionMembers(ctrlResult.result.filteredSubChilds, selectedFields, ctrlResult.result.subscription, contactSwitch, ExportClickMeService.FORMAT.CSV))
                        }
                        out.close()
                        return
                }
            }
            else {
                if(subscriptionService.checkThreadRunning("PackageTransfer_"+ctrlResult.result.subscription.id)) {
                    flash.message = message(code: 'subscription.details.linkPackage.thread.running.withPackage', args: [subscriptionService.getCachedPackageName('PackageTransfer_'+ctrlResult.result.subscription.id)] as Object[])
                }
                ctrlResult.result
            }
        }
    }

    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_BASIC], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport( CustomerTypeService.ORG_CONSORTIUM_BASIC )
    })
    def compareSubMemberCostItems() {
        Map<String,Object> ctrlResult = subscriptionControllerService.compareSubMemberCostItems(this,params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            response.sendError(401)
            return
        }

        ctrlResult.result
    }

    /**
     * Call to list potential member institutions to add to this subscription
     * @return a list view of member institutions
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    @Check404()
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

    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def templateForMembersBulkWithUpload() {
        log.debug("templateForMembersBulkWithUpload :: ${params}")
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)

        String filename = "template_sub_members_import"

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        FilterService.Result fsr = filterService.getOrgComboQuery(params, result.institution as Org)

        List<Org> consortiaMembers = Org.executeQuery(fsr.query, fsr.queryParams, params)


        ArrayList titles = ["Laser-ID", "WIB-ID", "ISIL", "ROR-ID", "GND-ID", "DEAL-ID", message(code: 'org.sortname.label'), message(code: 'default.name.label'), message(code: 'org.libraryType.label'), message(code: 'subscription.label')]

        ArrayList rowData = []
        ArrayList row
        consortiaMembers.each { Org org ->
            row = []
            String wibid = org.getIdentifierByType('wibid')?.value
            String isil = org.getIdentifierByType('ISIL')?.value
            String ror = org.getIdentifierByType('ROR ID')?.value
            String gnd = org.getIdentifierByType('gnd_org_nr')?.value
            String deal = org.getIdentifierByType('deal_id')?.value

            row.add(org.laserID)
            row.add((wibid != IdentifierNamespace.UNKNOWN && wibid != null) ? wibid : '')
            row.add((isil != IdentifierNamespace.UNKNOWN && isil != null) ? isil : '')
            row.add((ror != IdentifierNamespace.UNKNOWN && ror != null) ? ror : '')
            row.add((gnd != IdentifierNamespace.UNKNOWN && gnd != null) ? gnd : '')
            row.add((deal != IdentifierNamespace.UNKNOWN && deal != null) ? deal : '')

            row.add(org.sortname)
            row.add(org.name)
            row.add(org.libraryType?.getI10n('value'))
            Subscription subscription = result.subscription.getDerivedSubscriptionForNonHiddenSubscriber(org)
            if(subscription){
                row.add(subscription.getLabel())
            }

            rowData.add(row)
        }

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
        response.contentType = "text/csv"
        ServletOutputStream out = response.outputStream
        out.withWriter { writer ->
            writer.write(exportService.generateSeparatorTableString(titles, rowData, '\t'))
        }
        out.close()
        return

    }

    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def templateForRequestorIDUpload() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)

        String filename = "template_upload_requestor_ids"
        List<Org> consortiaMembers = result.subscription.getDerivedNonHiddenSubscribers()
        Platform platform = Platform.get(params.platform)

        ArrayList titles = ['Laser-ID', message(code: 'default.sortname.label'), 'Customer ID', 'Requestor ID']

        ArrayList rowData = [], row
        consortiaMembers.each { Org org ->
            CustomerIdentifier ci = CustomerIdentifier.findByCustomerAndPlatform(org, platform)
            row = [org.laserID, org.sortname]
            if(ci?.value)
                row << ci.value
            else row << ''
            if(ci?.requestorKey)
                row << ci.requestorKey
            else row << ''
            rowData.add(row)
        }

        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
        response.contentType = "text/csv"
        ServletOutputStream out = response.outputStream
        out.withWriter { writer ->
            writer.write(exportService.generateSeparatorTableString(titles, rowData, '\t'))
        }
        out.close()
        return

    }

    /**
     * Call to process the given input data and create member subscription instances for the given consortial subscription
     * @return a redirect to the subscription members view in case of success or details view or to the member adding form otherwise
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def processAddMembers() {
        Map<String,Object> ctrlResult = subscriptionControllerService.processAddMembers(params)
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
            if (ctrlResult.result.packagesToProcess && ctrlResult.result.memberSubscriptions) {
                Map<String, Object> configMap = params.clone()
                executorService.execute({
                    Thread.currentThread().setName("PackageTransfer_" + ctrlResult.result.parentSubscriptions[0].id)
                    ctrlResult.result.parentSubscriptions.each { Subscription currParent ->
                        if (ctrlResult.result.packagesToProcess.containsKey(currParent.id) && ctrlResult.result.memberSubscriptions.containsKey(currParent.id)) {
                            List<Subscription> updatedSubList = Subscription.findAllByIdInList(ctrlResult.result.memberSubscriptions.get(currParent.id))
                            Set<Package> packagesToProcessCurParent = ctrlResult.result.packagesToProcess.get(currParent.id)
                            if(updatedSubList && packagesToProcessCurParent) {
                                packagesToProcessCurParent.each { Package pkg ->
                                    subscriptionService.cachePackageName("PackageTransfer_" + ctrlResult.result.parentSubscriptions[0].id, pkg.name)
                                    subscriptionService.addToMemberSubscription(currParent, updatedSubList, pkg, configMap.get('linkWithEntitlements_' + currParent.id) == 'on')
                                    /*
                                        updatedSubList.each { Subscription currMember ->
                                        if(currParent.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_PARTIAL && !auditService.getAuditConfig(currParent, 'holdingSelection')) {
                                            }
                                            subscriptionService.addToSubscriptionCurrentStock(currMember, currParent, pkg, )
                                        }
                                    */
                                }
                            }
                        }
                    }
                })
            }

            if(ctrlResult.result.selectSubMembersWithImport){
                if(ctrlResult.result.selectSubMembersWithImport.truncatedRows){
                    flash.message = message(code: 'subscription.details.addMembers.option.selectMembersWithFile.selectProcess.truncatedRows', args: [ctrlResult.result.selectSubMembersWithImport.processCount, ctrlResult.result.selectSubMembersWithImport.processRow, ctrlResult.result.selectSubMembersWithImport.wrongOrgs, ctrlResult.result.selectSubMembersWithImport.truncatedRows])
                }else if(ctrlResult.result.selectSubMembersWithImport.wrongOrgs){
                    flash.message = message(code: 'subscription.details.addMembers.option.selectMembersWithFile.selectProcess.wrongOrgs', args: [ctrlResult.result.selectSubMembersWithImport.processCount, ctrlResult.result.selectSubMembersWithImport.processRow, ctrlResult.result.selectSubMembersWithImport.wrongOrgs])
                }else {
                    flash.message = message(code: 'subscription.details.addMembers.option.selectMembersWithFile.selectProcess', args: [ctrlResult.result.selectSubMembersWithImport.processCount, ctrlResult.result.selectSubMembersWithImport.processRow])
                }
            }

            redirect controller: 'subscription', action: 'members', params: [id: ctrlResult.result.subscription.id]
            return
        }
    }

    /**
     * Call to insert a succession link between two member subscriptions
     * @return the members view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_CONSORTIUM_BASIC )
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
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.ORG_CONSORTIUM_BASIC)
    })
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
     * @return redirects to the referer
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def unsetCustomerIdentifier() {
        subscriptionService.unsetCustomerIdentifier(params.long("deleteCI"))
        redirect(url: request.getHeader("referer"))
    }

    //-------------------------------- survey section --------------------------------------

    /**
     * Call to list surveys linked to a member subscription
     * @return a table view of surveys from the member's point of view
     */
    @DebugInfo(isInstUser_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.ORG_CONSORTIUM_PRO)
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
                if(params.createEntitlements == 'on') {
                    flash.message = message(code: 'subscription.details.link.processingWithEntitlements') as String
                    redirect action: 'index', params: [id: params.id, gokbId: params.addUUID]
                    return
                }
                else {
                    flash.message = message(code:'subscription.details.link.processingWithoutEntitlements') as String
                    redirect action: 'addEntitlements', params: [id: params.id, packageLinkPreselect: params.addUUID, preselectedName: ctrlResult.result.packageName]
                    return
                }
            }
        }
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Call to unlink the given package from the given subscription
     * @return the list of conflicts, if no confirm has been submitted; the redirect to the subscription details page if confirm has been sent
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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

    /**
     * Call to list the potential package candidates for single title linking
     * @return a list view of the packages in the we:kb ElasticSearch index or a redirect to an title list view
     * if a package UUID has been submitted with the call
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def linkTitle() {
        log.debug("linkTitle : ${params}")
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(result) {
            Profiler prf = new Profiler()
            prf.setBenchmark('init')
            Map<String, Object> configMap = [:]
            Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'Tipps')
            if (ttParams.status) { params.status = ttParams.status }
            if (ttParams.tab)    { params.tab = ttParams.tab }
            SwissKnife.setPaginationParams(result, params, contextService.getUser())
            if(params.containsKey('filterSet')) {
                params.each { key, value ->
                    if(value)
                        configMap.put(key, value)
                }
                prf.setBenchmark('getting keys')
                Set<Long> keys = titleService.getKeys(configMap)
                prf.setBenchmark('get title list')
                result.titlesList = keys ? TitleInstancePackagePlatform.findAllByIdInList(keys.drop(result.offset).take(result.max), [sort: params.sort?: 'sortname', order: params.order]) : []
                result.num_tipp_rows = keys.size()
                result.editable = contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
                result.tmplConfigShow = ['lineNumber', 'name', 'status', 'package', 'provider', 'platform', 'lastUpdatedDisplay', 'linkTitle']
            }
            result.benchMark = prf.stopBenchmark()
            result
        }
        else {
            response.sendError(401)
            return
        }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processLinkTitle() {
        Subscription subscription = (Subscription) genericOIDService.resolveOID(params.subscription)
        if(!subscription)
            subscription = Subscription.get(params.subscription)
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(params.tippID)
        if(subscription && tipp) {
            Package pkg = tipp.pkg
            subscription.holdingSelection = RDStore.SUBSCRIPTION_HOLDING_PARTIAL //in case it was null before
            subscription.save()
            subscriptionService.linkTitle(subscription, pkg, tipp, params.linkToChildren == 'on')
            redirect action: 'index', id: subscription.id
        }
        else {
            String error = ""
            if(!subscription)
                error += "<p>${message(code: 'default.not.found.message', args: [message(code: 'subscription'), params.subscription])}</p>"
            if(!tipp)
                error += "<p>${message(code: 'default.not.found.message', args: [message(code: 'title'), params.tippID])}</p>"
            flash.error = error
            redirect controller: 'title', action: 'index'
        }
    }

    //-------------------------------- issue entitlements holding --------------------------------------

    /**
     * Call to list the current title holding of the subscription. The list may be displayed as HTML table
     * or be exported as KBART or Excel worksheet
     * @return a list of the current subscription stock; either as HTML output or as KBART / Excel table
     */
    /*
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404()
    def index_old() {
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
            //ArrayList<IssueEntitlement> issueEntitlements = []
            Map<String, Object> selectedFields = [:]
            if(params.fileformat) {
                if (params.filename) {
                    filename = params.filename
                }
                //issueEntitlements.addAll(IssueEntitlement.findAllByIdInList(ctrlResult.result.entitlementIDs.id,[sort:'tipp.sortname']))
                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            }
            if (params.exportKBart) {
                String dir = GlobalService.obtainFileStorageLocation()
                File f = new File(dir+'/'+filename)
                if(!f.exists()) {
                    FileOutputStream fos = new FileOutputStream(f)
                    Map<String, Object> configMap = [:]
                    configMap.putAll(params)
                    configMap.sub = ctrlResult.result.subscription
                    configMap.pkgIds = ctrlResult.result.subscription.packages?.pkg?.id //GORM sometimes does not initialise the sorted set
                    Map<String, Collection> tableData = exportService.generateTitleExportKBART(configMap, IssueEntitlement.class.name)
                    fos.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
                    }
                    fos.flush()
                    fos.close()
                }
                Map fileResult = [token: filename, filenameDisplay: filename, fileformat: 'kbart']
                render template: '/templates/bulkItemDownload', model: fileResult
                return
            }
            else if(params.fileformat == 'xlsx') {
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportIssueEntitlements(ctrlResult.result.entitlementIDs.id, selectedFields, ExportClickMeService.FORMAT.XLS)
                response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            }
            else if(params.fileformat == 'csv') {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportIssueEntitlements(ctrlResult.result.entitlementIDs.id, selectedFields, ExportClickMeService.FORMAT.CSV))
                }
                out.close()
            }
            else {
                flash.message = ctrlResult.result.message
                ctrlResult.result
            }
        }
    }
    */

    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404()
    def index() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (result.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = result.error
                result
            }
        }
        else {
            SwissKnife.setPaginationParams(result, params, (User) result.user)
            if(result.subscription.packages) {
                Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
                Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
                threadArray.each {
                    if (it.name == 'PackageTransfer_'+result.subscription.id) {
                        result.message = message(code: 'subscription.details.linkPackage.thread.running.withPackage',args: [subscriptionService.getCachedPackageName(it.name)] as Object[])
                    }
                    else if (it.name == 'EntitlementEnrichment_'+result.subscription.id) {
                        result.message = message(code: 'subscription.details.addEntitlements.thread.running')
                    }
                }
                result.issueEntitlementEnrichment = params.issueEntitlementEnrichment
                EhcacheWrapper resultCache = cacheService.getTTL300Cache("${contextService.getUser()}/issueEntitlementResult")
                Map<String, Object> enrichmentResult = (Map<String, Object>) resultCache.get('enrichmentResult')
                enrichmentResult.each { String enrichmentResultKey, def value ->
                    result[enrichmentResultKey] = value
                }
                resultCache.clear()
                Map ttParams = FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'IEs')
                if (ttParams.status) { params.status = ttParams.status }
                if (ttParams.tab)    { params.tab = ttParams.tab }
                SwissKnife.setPaginationParams(result, params, (User) result.user)
                Subscription targetSub = issueEntitlementService.getTargetSubscription(result.subscription)
                result.editable = targetSub == result.subscription && result.editable
                Set<Package> targetPkg = targetSub.packages.pkg
                if(params.pkgFilter)
                    targetPkg = [Package.get(params.pkgFilter)]
                Map<String, Object> configMap = [subscription: targetSub, packages: targetPkg]
                configMap.putAll(params)
                if(params.titleGroup && params.titleGroup != 'notInGroups')
                    configMap.titleGroup = IssueEntitlementGroup.get(params.titleGroup)
                //overwrite with parsed ints
                configMap.offset = result.offset
                configMap.max = result.max
                if(!configMap.sort)
                    configMap.sort = "tipp.sortname"
                if(!configMap.order)
                    configMap.order = "asc"
                Map<String, Object> keys = issueEntitlementService.getKeys(configMap)
                result.putAll(issueEntitlementService.getCounts(configMap))
                Set<Long> ieSubset = keys.ieIDs.drop(configMap.offset).take(configMap.max)
                result.entitlements = IssueEntitlement.findAllByIdInList(ieSubset, [sort: configMap.sort, order: configMap.order])
                result.num_ies_rows = keys.ieIDs.size()
                Set<SubscriptionPackage> deletedSPs = result.subscription.packages.findAll { SubscriptionPackage sp -> sp.pkg.packageStatus in [RDStore.PACKAGE_STATUS_DELETED, RDStore.PACKAGE_STATUS_REMOVED] }
                if(deletedSPs) {
                    result.deletedSPs = []
                    deletedSPs.each { SubscriptionPackage sp ->
                        result.deletedSPs << [name:sp.pkg.name,uuid:sp.pkg.gokbId]
                    }
                }
            }
            result
        }
    }

    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    @Check404()
    def exportHolding() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        if (result.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = result.error
                result
            }
        }
        else {
            String exportTab
            switch(params.tab) {
                case 'currentIEs': exportTab = escapeService.escapeString(message(code: "package.show.nav.current"))
                    break
                case 'plannedIEs': exportTab = escapeService.escapeString(message(code: "package.show.nav.planned"))
                    break
                case 'expiredIEs': exportTab = escapeService.escapeString(message(code: "package.show.nav.expired"))
                    break
                case 'deletedIEs': exportTab = escapeService.escapeString(message(code: "package.show.nav.deleted"))
                    break
                case 'allIEs': exportTab = escapeService.escapeString(message(code: "menu.public.all_titles"))
                    break
                default: exportTab = escapeService.escapeString(message(code: "package.show.nav.current"))
                    break
            }
            String filename = "${escapeService.escapeString(result.subscription.dropdownNamingConvention())}_${exportTab}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
            Subscription targetSub = issueEntitlementService.getTargetSubscription(result.subscription)
            Set<Package> targetPkg = targetSub.packages.pkg
            if(params.pkgFilter)
                targetPkg = [Package.get(params.pkgFilter)]
            Map<String, Object> configMap = [subscription: targetSub, packages: targetPkg]
            configMap.putAll(FilterLogic.resolveTabAndStatusForTitleTabsMenu(params, 'IEs'))
            configMap.putAll(params)
            Map<String, Object> keys = issueEntitlementService.getKeys(configMap)
            Map<String, Object> selectedFields = [:]
            if(params.fileformat) {
                if (params.filename) {
                    filename = params.filename
                }
                //issueEntitlements.addAll(IssueEntitlement.findAllByIdInList(ctrlResult.result.entitlementIDs.id,[sort:'tipp.sortname']))
                Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }
            }
            if (params.exportKBart) {
                String dir = GlobalService.obtainTmpFileLocation()
                File f = new File(dir+'/'+filename)
                if(!f.exists()) {
                    FileOutputStream fos = new FileOutputStream(f)
                    Map<String, Object> tableData = exportService.generateTitleExport([format: ExportService.KBART, ieIDs: keys.ieIDs])
                    fos.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
                    }
                    fos.flush()
                    fos.close()
                }
                Map fileResult = [token: filename, filenameDisplay: filename, fileformat: ExportService.KBART]
                render template: '/templates/bulkItemDownload', model: fileResult
                return
            }
            else if(params.fileformat == 'xlsx') {
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportIssueEntitlements(keys.ieIDs, selectedFields, ExportClickMeService.FORMAT.XLS)
                response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            }
            else if(params.fileformat == 'csv') {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportIssueEntitlements(keys.ieIDs, selectedFields, ExportClickMeService.FORMAT.CSV))
                }
                out.close()
            }
        }
    }

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processIssueEntitlementEnrichment() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if (result.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = result.error
                result
            }
        }
        else {
            Map<String, Object> configMap = params.clone()
            result.putAll(params)
            MultipartFile kbartPreselect = params.kbartPreselect
            Set<Package> subPkgs = result.subscription.packages.pkg
            if(kbartPreselect && kbartPreselect.size > 0) {
                String encoding = UniversalDetector.detectCharset(kbartPreselect.getInputStream())
                if (encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                    result.remove('kbartPreselect')
                    result.remove("uploadCoverageDates")
                    result.remove("uploadPriceInfo")
                    if(params.withPick)
                        configMap.withPick = true //this way because I check the existence of the key
                    configMap.uploadCoverageDates = params.uploadCoverageDates == 'on'
                    configMap.uploadPriceInfo = params.uploadPriceInfo == 'on'
                    String progressCacheKey = result.remove('progressCacheKey')
                    configMap.putAll([subscription: result.subscription, subPkgs: subPkgs, encoding: encoding, issueEntitlementEnrichment: true, progressCacheKey: progressCacheKey, floor: 0, ceil: 50])
                    result.putAll(subscriptionService.issueEntitlementEnrichment(kbartPreselect, configMap))
                }
                else result.wrongCharset = true
            }
            else result.noFileSubmitted = true
            if (result.notAddedCount > 0) {
                //background of this procedure: the editor adding titles via KBART wishes to receive a "counter-KBART" which will then be sent to the provider for verification
                String dir = GlobalService.obtainTmpFileLocation(), filename = "${kbartPreselect.getOriginalFilename()}_matchingErrors"
                File f = new File(dir+'/'+filename)
                result.titleRow.add('found_in_package')
                Set<Contact> mailTo = Contact.executeQuery("select ct from PersonRole pr join pr.prs p join p.contacts ct where (p.isPublic = true or p.tenant = :context) and pr.functionType in (:functionTypes) and ct.contentType.value in (:contentTypes) and pr.provider in (:providers)",
                        [context: contextService.getOrg(), providers: subPkgs.provider, functionTypes: [RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS], contentTypes: [RDStore.CCT_EMAIL.value, 'Mail']])
                result.mailTo = mailTo
                Set<CustomerIdentifier> customerIdentifierSet = CustomerIdentifier.findAllByPlatformInListAndCustomer(subPkgs.nominalPlatform, contextService.getOrg())
                String customerIdentifier = "", notInPackageRows = "", productIDString = ""
                if(customerIdentifierSet)
                    customerIdentifier = customerIdentifierSet.value.join(',')
                if(result.notInPackage) {
                    result.notInPackage.each { Map<String, Object> row ->
                        notInPackageRows += "${row.values().join('\t')}\n"
                    }
                }
                Set<Identifier> productIDs = subPkgs.ids.findAll { id -> id.ns == IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PKG_ID, de.laser.wekb.Package.class.name) }
                if(productIDs)
                    productIDString = productIDs.join('/')
                result.mailBody = message(code: 'subscription.details.addEntitlements.matchingErrorMailBody', args: [contextService.getOrg().name, customerIdentifier, subPkgs.name.join(','), productIDString, notInPackageRows])
                String returnKBART = exportService.generateSeparatorTableString(result.titleRow, result.notAddedTitles, '\t')
                FileOutputStream fos = new FileOutputStream(f)
                fos.withWriter { Writer w ->
                    w.write(returnKBART)
                }
                fos.flush()
                fos.close()
                result.token = filename
            }
            EhcacheWrapper resultCache = cacheService.getTTL300Cache("${contextService.getUser()}/issueEntitlementResult")
            resultCache.put('enrichmentResult', [error: result.keySet().any {String errKey -> errKey in ['wrongCharset', 'noFileSubmitted', 'noValidSubscription', 'token', 'pickWithNoPick'] },
                                            pickWithNoPick: result.pickWithNoPick,
                                            success: result.success,
                                            wrongCharset: result.wrongCharset,
                                            noFileSubmitted: result.noFileSubmitted,
                                            mailTo: result.mailTo,
                                            processCount: result.processCount,
                                            toAddCount: result.toAddCount,
                                            notAddedCount: result.notAddedCount,
                                            notAddedTitles: result.notAddedTitles,
                                            notSubscribedCount: result.notSubscribedCount,
                                            notInPackageCount: result.notInPackageCount,
                                            processCountChangesCoverageDates: result.processCountChangesCoverageDates,
                                            processCountChangesPrice: result.processCountChangesPrice])
            redirect action: 'index', params: [id: result.id,
                                               issueEntitlementEnrichment: true]
        }
    }

    /**
     * Call to load the applied or pending changes to the given subscription
     * @return the called tab with the changes of the given event type
     */
    @DebugInfo(isInstUser_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
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
     * may be exported as KBART or Excel worksheet as well. The view contains also enrichment functionalities
     * such as preselection of titles based on identifiers or adding locally negotiated prices or coverage statements
     * @return the list view of entitlements, either as HTML table or KBART / Excel worksheet export
     */
    @DebugInfo(isInstUser_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def addEntitlements() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if (result.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = result.error
                result
            }
        }
        else if(result.subscription.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE || AuditConfig.getConfig(result.subscription.instanceOf, 'holdingSelection')) {
            flash.error = message(code: 'subscription.details.addEntitlements.holdingEntire')
            redirect controller: 'subscription', action: 'show', params: [id: params.id]
            return
        }
        else {
            Map<String, Object> configMap = params.clone()
            if(subscriptionService.checkThreadRunning('PackageSync_'+result.subscription.id)) {
                flash.message = message(code: 'subscription.details.linkPackage.thread.running')
            }
            else if (subscriptionService.checkThreadRunning('EntitlementEnrichment_'+result.subscription.id)) {
                flash.message = message(code: 'subscription.details.addEntitlements.thread.running')
                result.blockSubmit = true
            }
            SwissKnife.setPaginationParams(result, configMap, (User) result.user)
            if(params.packageLinkPreselect) {
                configMap.packages = [Package.findByGokbId(params.packageLinkPreselect)]
                if(configMap.packages)
                    params.pkgfilter = configMap.packages[0].id
            }
            else {
                configMap.packages = result.subscription.packages.pkg
            }
            configMap.subscription = result.subscription
            EhcacheWrapper userCache = contextService.getUserCache("/subscription/addEntitlements/${params.id}")
            Map checkedCache = userCache.get('selectedTitles')

            if (!checkedCache || !params.containsKey('pagination')) {
                checkedCache = [:]
            }
            result.checkedCache = checkedCache.get('checked')
            result.checkedCount = result.checkedCache.findAll { it.value == 'checked' }.size()
            result.countSelectedTipps = result.checkedCount
            if(configMap.packages) {
                configMap.newEntitlements = true
                Map<String, Object> keys = issueEntitlementService.getKeys(configMap)
                Set<Long> tippIDs = keys.tippIDs
                tippIDs.removeAll(keys.ieIDs)
                result.num_tipp_rows = tippIDs.size()
                result.tipps = []
                Map<TitleInstancePackagePlatform, Set<PermanentTitle>> permanentTitles = [:]
                TitleInstancePackagePlatform.findAllByIdInList(tippIDs.drop(result.offset).take(result.max), [sort: 'sortname', order: 'asc']).each { TitleInstancePackagePlatform tipp ->
                    result.tipps << tipp
                    permanentTitles.put(tipp, PermanentTitle.executeQuery('select pt from PermanentTitle pt where pt.owner = :org and (pt.tipp = :tipp or pt.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.hostPlatformURL = :hostPlatformURL and tipp.status != :tippStatus))', [tipp: tipp, hostPlatformURL: tipp.hostPlatformURL, tippStatus: RDStore.TIPP_STATUS_DELETED, org: result.subscription.getSubscriber()]))
                    permanentTitles.put(tipp, PermanentTitle.executeQuery("select pt from PermanentTitle pt where pt.subscription in (select s.instanceOf from OrgRole oo join oo.sub s where oo.org = :org and oo.roleType = :subscriberCons and s.instanceOf.id in (select ac.referenceId from AuditConfig ac where ac.referenceField = 'holdingSelection')) and (pt.tipp = :tipp or pt.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.hostPlatformURL = :hostPlatformURL and tipp.status != :tippStatus))", [tipp: tipp, hostPlatformURL: tipp.hostPlatformURL, tippStatus: RDStore.TIPP_STATUS_DELETED, org: result.subscription.getSubscriber(), subscriberCons: RDStore.OR_SUBSCRIBER_CONS]))
                }
                result.permanentTitles = permanentTitles
            }
            else {
                result.num_tipp_rows = 0
                result.tipps = []
                result.permanentTitles = []
            }
        }
        result
    }

    @DebugInfo(isInstUser_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport()
    })
    def exportPossibleEntitlements() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        if(result.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = result.result
                redirect action: 'addEntitlements', params: [id: params.id]
            }
        }
        else {
            String filename = "${escapeService.escapeString(result.subscription.dropdownNamingConvention())}_${DateUtils.getSDF_noTimeNoPoint().format(new Date())}"
            Map<String, Object> configMap = params.clone()
            configMap.packages = result.subscription.packages.pkg
            configMap.subscription = result.subscription
            Map<String, Object> keys = issueEntitlementService.getKeys(configMap)
            Set<Long> tippIDs = keys.tippIDs
            tippIDs.removeAll(keys.ieIDs)
            configMap.tippIDs = tippIDs
            Map<String, Object> selectedFields = [:]
            if (params.fileformat) {
                Map<String, Object> selectedFieldsRaw = params.findAll { it -> it.toString().startsWith('iex:') }
                selectedFieldsRaw.each { it -> selectedFields.put(it.key.replaceFirst('iex:', ''), it.value) }
            }
            if (params.exportKBart) {
                configMap.format = ExportService.KBART
                String dir = GlobalService.obtainTmpFileLocation()
                File f = new File(dir + '/' + filename)
                if (!f.exists()) {
                    FileOutputStream out = new FileOutputStream(f)
                    Map<String, Collection> tableData = exportService.generateTitleExport(configMap)
                    out.withWriter { writer ->
                        writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
                    }
                    out.flush()
                    out.close()
                }
                Map fileResult = [token: filename, filenameDisplay: filename, fileformat: 'kbart']
                render template: '/templates/bulkItemDownload', model: fileResult
                return
            } else if (params.fileformat == 'xlsx') {
                SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportTipps(tippIDs, selectedFields, ExportClickMeService.FORMAT.XLS)
                response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
                return
            } else if (params.fileformat == 'csv') {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"
                ServletOutputStream out = response.outputStream
                out.withWriter { writer ->
                    writer.write((String) exportClickMeService.exportTipps(tippIDs, selectedFields, ExportClickMeService.FORMAT.CSV))
                }
                out.flush()
                out.close()
            } else {
                flash.message = result.result.message
                flash.error = result.result.error
                result.result
            }
        }
    }

    /**
     * Call to remove the given issue entitlement from the subscription's holding
     * @return the issue entitlement holding view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def removeEntitlement() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeEntitlement(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.delete.error.general.message') as String
        else {
            Object[] args = [message(code:'issueEntitlement.label'),params.ieid]
            flash.message = message(code: 'default.deleted.message',args: args) as String
        }
        redirect action: 'index', id: params.sub, params: [tab: params.tab]
    }

    /**
     * Call to remove an issue entitlement along with his title group record
     * @return the issue entitlement holding view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def removeEntitlementWithIEGroups() {
        Map<String,Object> ctrlResult = subscriptionControllerService.removeEntitlementWithIEGroups(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR)
            flash.error = message(code:'default.delete.error.general.message') as String
        else {
            Object[] args = [message(code:'issueEntitlement.label'),params.ieid]
            flash.message = message(code: 'default.deleted.message',args: args) as String
        }
        redirect action: 'index', id: params.sub, params: [tab: params.tab]
    }

    /**
     * Call to preselect and add the selected entitlements via a KBART file
     * @return the issue entitlement holding view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def selectEntitlementsWithKBART() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_EDIT)
        MultipartFile kbartPreselect = request.getFile('kbartPreselect')
        //we may presume that package linking is given and checked at this place
        Set<Package> subPkgs = result.subscription.packages.pkg
        if(kbartPreselect && kbartPreselect.size > 0) {
            String encoding = UniversalDetector.detectCharset(kbartPreselect.getInputStream())
            if (encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                params.remove('kbartPreselect')
                result.progressCacheKey = params.remove('progressCacheKey')
                Map<String, Object> configMap = params.clone()
                configMap.putAll([subscription: result.subscription, subPkgs: subPkgs, encoding: encoding, progressCacheKey: result.progressCacheKey, floor: 0, ceil: 50])
                result.putAll(subscriptionService.selectEntitlementsWithKBART(kbartPreselect, configMap))
            }
            else result.wrongCharset = true
        }
        else result.noFileSubmitted = true
        if (result.notAddedCount > 0) {
            //background of this procedure: the editor adding titles via KBART wishes to receive a "counter-KBART" which will then be sent to the provider for verification
            String dir = GlobalService.obtainTmpFileLocation(), filename = "${kbartPreselect.getOriginalFilename()}_matchingErrors"
            File f = new File(dir+'/'+filename)
            result.titleRow.addAll(['found_in_package','already_purchased_at'])
            Set<Contact> mailTo = Contact.executeQuery("select ct from PersonRole pr join pr.prs p join p.contacts ct where (p.isPublic = true or p.tenant = :context) and pr.functionType in (:functionTypes) and ct.contentType.value in (:contentTypes) and pr.provider in (:providers)",
                    [context: contextService.getOrg(), providers: subPkgs.provider, functionTypes: [RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS], contentTypes: [RDStore.CCT_EMAIL.value, 'Mail']])
            result.mailTo = mailTo
            Set<CustomerIdentifier> customerIdentifierSet = CustomerIdentifier.findAllByPlatformInListAndCustomer(subPkgs.nominalPlatform, contextService.getOrg())
            String customerIdentifier = "", notInPackageRows = "", productIDString = ""
            if(customerIdentifierSet)
                customerIdentifier = customerIdentifierSet.value.join(',')
            if(result.notInPackage) {
                result.notInPackage.each { Map<String, Object> row ->
                    notInPackageRows += "${row.values().join('\t')}\n"
                }
            }
            Set<Identifier> productIDs = subPkgs.ids.findAll { id -> id.ns == IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PKG_ID, de.laser.wekb.Package.class.name) }
            if(productIDs)
                productIDString = productIDs.join('/')
            result.mailBody = message(code: 'subscription.details.addEntitlements.matchingErrorMailBody', args: [contextService.getOrg().name, customerIdentifier, subPkgs.name.join(','), productIDString, notInPackageRows])
            String returnKBART = exportService.generateSeparatorTableString(result.titleRow, result.notAddedTitles, '\t')
            FileOutputStream fos = new FileOutputStream(f)
            fos.withWriter { Writer w ->
                w.write(returnKBART)
            }
            fos.flush()
            fos.close()
            result.token = filename
            result.error = true
        }
        render template: 'entitlementProcessResult', model: result
    }

    /**
     * Call to preselect and add the selected entitlements via a KBART file
     * @return the issue entitlement holding view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def tippSelectForSurvey() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        MultipartFile kbartPreselect = request.getFile('kbartPreselect')
        //we may presume that package linking is given and checked at this place
        Set<Package> subPkgs = result.subscription.packages.pkg
        if(kbartPreselect && kbartPreselect.size > 0) {
            String encoding = UniversalDetector.detectCharset(kbartPreselect.getInputStream())
            if (encoding in ["US-ASCII", "UTF-8", "WINDOWS-1252"]) {
                params.remove('kbartPreselect')
                result.progressCacheKey = params.remove('progressCacheKey')
                Map<String, Object> configMap = params.clone()
                configMap.putAll([subscription: result.subscription, subPkgs: subPkgs, encoding: encoding, progressCacheKey: result.progressCacheKey, floor: 0, ceil: 50])
                result.putAll(subscriptionService.tippSelectForSurvey(kbartPreselect, configMap))
            }
            else result.wrongCharset = true
        }
        else result.noFileSubmitted = true
        if (result.notAddedCount > 0) {
            //background of this procedure: the editor adding titles via KBART wishes to receive a "counter-KBART" which will then be sent to the provider for verification
            String dir = GlobalService.obtainTmpFileLocation(), filename = "${kbartPreselect.getOriginalFilename()}_matchingErrors"
            File f = new File(dir+'/'+filename)
            if(!params.containsKey('withIDOnly'))
                result.titleRow.addAll(['found_in_package','already_purchased_at'])
            Set<Contact> mailTo = Contact.executeQuery("select ct from PersonRole pr join pr.prs p join p.contacts ct where (p.isPublic = true or p.tenant = :context) and pr.functionType in (:functionTypes) and ct.contentType.value in (:contentTypes) and pr.provider in (:providers)",
                    [context: contextService.getOrg(), providers: subPkgs.provider, functionTypes: [RDStore.PRS_FUNC_TECHNICAL_SUPPORT, RDStore.PRS_FUNC_SERVICE_SUPPORT, RDStore.PRS_FUNC_GENERAL_CONTACT_PRS], contentTypes: [RDStore.CCT_EMAIL.value, 'Mail']])
            result.mailTo = mailTo
            Set<CustomerIdentifier> customerIdentifierSet = CustomerIdentifier.findAllByPlatformInListAndCustomer(subPkgs.nominalPlatform, contextService.getOrg())
            String customerIdentifier = "", notInPackageRows = "", productIDString = ""
            if(customerIdentifierSet)
                customerIdentifier = customerIdentifierSet.value.join(',')
            if(result.notInPackage) {
                result.notInPackage.each { Map<String, Object> row ->
                    notInPackageRows += "${row.values().join('\t')}\n"
                }
            }
            Set<Identifier> productIDs = subPkgs.ids.findAll { id -> id.ns == IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PKG_ID, de.laser.wekb.Package.class.name) }
            if(productIDs)
                productIDString = productIDs.join('/')
            result.mailBody = message(code: 'subscription.details.addEntitlements.matchingErrorMailBody', args: [contextService.getOrg().name, customerIdentifier, subPkgs.name.join(','), productIDString, notInPackageRows])
            String returnKBART = exportService.generateSeparatorTableString(result.titleRow, result.notAddedTitles, '\t')
            FileOutputStream fos = new FileOutputStream(f)
            fos.withWriter { Writer w ->
                w.write(returnKBART)
            }
            fos.flush()
            fos.close()
            result.token = filename
            result.error = true
        }
        render template: 'entitlementProcessResult', model: result
    }

    /**
     * Call to persist the cached data and create the issue entitlement holding based on that data
     * @return the issue entitlement holding view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processAddEntitlements() {

        Map<String,Object> ctrlResult = subscriptionService.processAddEntitlements(params)
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processAddIssueEntitlementsSurvey() {
        Map<String, Object> result = subscriptionService.addSingleEntitlementSurvey(params)
        if(result.containsKey('message'))
            flash.message = result.message
        else if(result.containsKey('error'))
            flash.error = result.error
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Call to remove the given title from the picked titles
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processRemoveIssueEntitlementsSurvey() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)
        result.editable = surveyService.isEditableSurvey(result.institution, result.surveyConfig.surveyInfo)

        if(result.editable){
            IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
            if(IssueEntitlement.countById(params.long('singleTitle')) && issueEntitlementGroup) {
                if (subscriptionService.deleteEntitlementByID(result.subscription, params.singleTitle, issueEntitlementGroup))
                    log.debug("Deleted ie ${params.singleTitle} from sub ${result.subscription.id}")
            }
        }


        redirect(url: request.getHeader("referer"))
    }

    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def setPermanentTitlesByPackage() {
        Package pkg = Package.get(params.pkg)
        if(pkg) {
            subscriptionService.setPermanentTitlesByPackage(pkg)
        }
        redirect(url: request.getHeader("referer"))
    }

    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
    def removePermanentTitlesByPackage() {
        Package pkg = Package.get(params.pkg)
        if(pkg) {
            subscriptionService.removePermanentTitlesByPackage(pkg)
        }
        redirect(url: request.getHeader("referer"))
    }

    /**
     * Call to trigger the revertion of holding status to the end of the subscription's year ring
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor()
    })
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
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def subscriptionBatchUpdate() {
        Map<String,Object> ctrlResult = subscriptionControllerService.subscriptionBatchUpdate(this,params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else {
                flash.error = message(code:'default.save.error.general.message') as String
                redirect action: 'index', params: [id: ctrlResult.result.subscription.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max, status: params.list('status')]
                return
            }
        }
        else {
            redirect action: 'index', params: [id: ctrlResult.result.subscription.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max, status: params.list('status')]
            return
        }
    }

    /**
     * Call to add a new coverage statement to the issue entitlement
     * @return the issue entitlement holding view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def manageEntitlementGroup() {
        Map<String, Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW_AND_EDIT)
        result.titleGroups = result.subscription.ieGroups
        result
    }

    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], withTransaction = 0)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    Map<String,Object> copyDiscountScales() {
        Map<String, Object> ctrlResult = subscriptionControllerService.copyDiscountScales(this, params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else {
            if(ctrlResult.result.error) {
                flash.error = ctrlResult.result.error
            }

            ctrlResult.result
        }

    }

    /**
     * Call to list the current discount scales of the subscription
     * @return the list of discount scales for the given subscription
     */
    @DebugInfo(isInstEditor_denySupport = [CustomerTypeService.ORG_CONSORTIUM_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport( CustomerTypeService.ORG_CONSORTIUM_PRO )
    })
    def manageDiscountScale() {
        Map<String, Object> ctrlResult = subscriptionControllerService.manageDiscountScale(this, params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
        }
        else ctrlResult.result
    }


    /**
     * Call to edit the given title group
     * @return either the edit view or the index view, when form data has been submitted
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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
    @DebugInfo(isInstEditor_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processRenewEntitlementsWithSurvey_old() {
        /*
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
        */
    }

    /**
     * Call to load the selection list for the title renewal. The list may be exported as a (configurable) Excel table with usage data for each title
     * @return the title list for selection; either as HTML table or as Excel export, configured with the given parameters
     */
    @DebugInfo(isInstUser = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def renewEntitlementsWithSurvey() {
        Map<String,Object> ctrlResult = subscriptionService.renewEntitlementsWithSurvey(params)
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if (!ctrlResult.result) {
                response.sendError(401)
                return
            }
            else flash.error = ctrlResult.result.error
        }
        else {
            ctrlResult.result
        }
    }

    /**
     * Call to process the title selection with the given input parameters
     * @return a redirect to the referer
     */
    @DebugInfo(isInstEditor_denySupport = [])
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    def processRenewEntitlementsWithSurvey() {
        Map<String, Object> ctrlResult = subscriptionService.processRenewEntitlementsWithSurvey(params)
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

    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
    Map exportRenewalEntitlements() {
        Map<String, Object> ctrlResult = subscriptionService.exportRenewalEntitlements(params)
        Map<String, Object> fileResult = [:]
        if (ctrlResult.status == SubscriptionControllerService.STATUS_ERROR) {
            if(!ctrlResult.result) {
                fileResult.error = 401
            }
            else {
                fileResult.error = ctrlResult.result.error
            }
            render template: '/templates/bulkItemDownload', model: fileResult
        }
        else if(ctrlResult.result.containsKey('status202')) {
            fileResult.remove('token')
            fileResult.error = 202
            render template: '/templates/stats/usageReport', model: fileResult
        }
        else {
            fileResult = [token: ctrlResult.result.token, filenameDisplay: ctrlResult.result.filename, fileformat: params.exportConfig]
            if(params.tab == 'usage')
                render template: '/templates/stats/usageReport', model: fileResult
            else
                render template: '/templates/bulkItemDownload', model: fileResult
            return
        }
    }

    /**
     * Takes the given configuration map and updates the pending change behavior for the given subscription package
     * @return the (updated) subscription details view
     */
    @DebugInfo(isInstEditor_denySupport = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_denySupport()
    })
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

    //--------------------------------------------- renewal section ---------------------------------------------

    /**
     * Call for manual renewal of a given subscription, i.e. without performing a renewal survey before
     * @return the starting page of the subscription renewal process
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
    })
    def renewSubscription() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        Subscription subscription = Subscription.get(params.baseSubscription ?: params.id)
        result.subscription = subscription
        SimpleDateFormat sdf = DateUtils.getSDF_ddMMyyyy()
        Date newStartDate
        Date newEndDate
        Year newReferenceYear = subscription.referenceYear ? subscription.referenceYear.plusYears(1) : null
        use(TimeCategory) {
            newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
            newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
        }
        result.isRenewSub = true
        result.permissionInfo = [sub_startDate    : newStartDate ? sdf.format(newStartDate) : null,
                                 sub_endDate      : newEndDate ? sdf.format(newEndDate) : null,
                                 sub_referenceYear: newReferenceYear ?: null,
                                 sub_name         : subscription.name,
                                 sub_id           : subscription.id,
                                 sub_status       : RDStore.SUBSCRIPTION_INTENDED.id]
        result
    }

    /**
     * Takes the given base data, creates the successor subscription instance and initialises elements
     * copying process
     * @return the first page of the element copy processing
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
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
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
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
                    ctrlResult.result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
                    break
                case CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS:
                    ctrlResult.result << copyElementsService.copyObjectElements_PackagesEntitlements(params)
                    params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                    ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                    break
                case CopyElementsService.WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                    ctrlResult.result << copyElementsService.copyObjectElements_DocsTasksWorkflows(params)
                    if (ctrlResult.result.isConsortialObjects && contextService.isInstUser(CustomerTypeService.ORG_CONSORTIUM_BASIC)){
                        params.workFlowPart = CopyElementsService.WORKFLOW_SUBSCRIBER
                        ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                    } else {
                        params.workFlowPart = contextService.getOrg().isCustomerType_Support() ? CopyElementsService.WORKFLOW_PROPERTIES : CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                        ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_SUBSCRIBER:
                    ctrlResult.result << copyElementsService.copyObjectElements_Subscriber(params)
                    params.workFlowPart = contextService.getOrg().isCustomerType_Support() ? CopyElementsService.WORKFLOW_PROPERTIES : CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
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
//            ctrlResult.result
        }

        render view: customerTypeService.getCustomerTypeDependingView('copySubscription'), model: ctrlResult.result
    }

    /**
     * Call to load data for the given step (by browsing in the copySubscription() tabs or by submitting values and eventually
     * turning to the next page); if data has been submitted, it will be processed
     * @return the copy parameters for the given (or its following) procedure section
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
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
                        ctrlResult.result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
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
                    ctrlResult.result << copyElementsService.copyObjectElements_DocsTasksWorkflows(params)
                    if (params.isRenewSub){
                        if (!params.fromSurvey && ctrlResult.result.isSubscriberVisible){
                            params.workFlowPart = CopyElementsService.WORKFLOW_SUBSCRIBER
                            ctrlResult.result << copyElementsService.loadDataFor_Subscriber(params)
                        } else {
                            params.workFlowPart = contextService.getOrg().isCustomerType_Support() ? CopyElementsService.WORKFLOW_PROPERTIES : CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                            ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                        }
                    } else {
                        ctrlResult.result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
                    }
                    break
                case CopyElementsService.WORKFLOW_SUBSCRIBER:
                    ctrlResult.result << copyElementsService.copyObjectElements_Subscriber(params)
                    if (params.isRenewSub) {
                        if(contextService.getOrg().isCustomerType_Support()) {
                            params.workFlowPart = CopyElementsService.WORKFLOW_PROPERTIES
                            ctrlResult.result << copyElementsService.loadDataFor_Properties(params)
                        }
                        else {
                            params.workFlowPart = CopyElementsService.WORKFLOW_PACKAGES_ENTITLEMENTS
                            ctrlResult.result << copyElementsService.loadDataFor_PackagesEntitlements(params)
                        }
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
                        ctrlResult.result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
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
                SurveyConfig surveyConfig = ctrlResult.result.fromSurvey ? SurveyConfig.get(Long.valueOf(ctrlResult.result.fromSurvey)) : null
                if (surveyConfig) {
                    redirect controller: 'survey', action: 'compareMembersOfTwoSubs', params: [id: surveyConfig.surveyInfo.id, surveyConfigID: surveyConfig.id]
                    return
                }
                else {
                    redirect controller: 'subscription', action: 'show', params: [id: ctrlResult.result.targetObject.id]
                    return
                }
            }
//            else ctrlResult.result
        }

        render view: customerTypeService.getCustomerTypeDependingView('copyElementsIntoSubscription'), model: ctrlResult.result
    }

    /**
     * Call for a single user to copy private properties from a consortial member subscription into its successor instance
     * @return the reduced subscription element copy view
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.ORG_INST_PRO], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.ORG_INST_PRO )
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
                    result << copyElementsService.copyObjectElements_DocsTasksWorkflows(params)
                    result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
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
                    result << copyElementsService.loadDataFor_DocsTasksWorkflows(params)
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
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )
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
    @DebugInfo(isInstUser_denySupport = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser_denySupport(CustomerTypeService.PERMS_PRO)
    })
    @Check404()
    def reporting() {
        if (! params.token) {
//            params.token = 'static#' + params.id
            params.token = RandomUtils.getAlphaNumeric(16) + '#' + params.id
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
    @DebugInfo(isInstUser = [CustomerTypeService.PERMS_PRO])
    @Secured(closure = {
        ctx.contextService.isInstUser(CustomerTypeService.PERMS_PRO)
    })
    @Check404()
    def workflows() {
        Map<String,Object> ctrlResult = subscriptionControllerService.workflows( params )

        render view: 'workflows', model: ctrlResult.result
    }

    //--------------------------------------------- helper section -------------------------------------------------

    /**
     * Gets the filter for titles and issue entitlements
     * Is here because the template uses controllerName
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def getTippIeFilter() {
        Map<String,Object> result = subscriptionControllerService.getResultGenericsAndCheckAccess(params, AccessService.CHECK_VIEW)
        result.action = params.formAction
        //<laser:render template="/templates/filter/tipp_ieFilter"/>
        render template: '/templates/filter/tipp_ieFilter', model: result
    }
}
