package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.properties.PropertyDefinition
import de.laser.AccessService
import de.laser.DeletionService
import de.laser.controller.AbstractDebugController
import de.laser.helper.DateUtil
import de.laser.helper.DebugAnnotation
import de.laser.helper.DebugUtil
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import de.laser.oai.OaiClientLaser
import grails.doc.internal.StringEscapeCategory
import grails.plugin.springsecurity.annotation.Secured
import de.laser.AuditConfig

// 2.0
import grails.converters.*
import com.k_int.kbplus.auth.*
import groovy.time.TimeCategory
import org.apache.poi.POIXMLProperties
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.groovy.grails.plugins.orm.auditable.AuditLogEvent
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.web.multipart.commons.CommonsMultipartFile

import javax.servlet.ServletOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat

//For Transform

@Mixin(com.k_int.kbplus.mixins.PendingChangeMixin)
@Secured(['IS_AUTHENTICATED_FULLY'])
class SubscriptionController extends AbstractDebugController {

    def springSecurityService
    def contextService
    def addressbookService
    def taskService
    def genericOIDService
    def transformerService
    def exportService
    def grailsApplication
    def pendingChangeService
    def institutionsService
    def ESSearchService
    def executorWrapperService
    def renewals_reversemap = ['subject': 'subject', 'provider': 'provid', 'pkgname': 'tokname']
    def accessService
    def filterService
    def propertyService
    def factService
    def docstoreService
    def ESWrapperService
    def globalSourceSyncService
    def dataloadService
    def GOKbService
    def navigationGenerationService
    def financeService
    def orgTypeService
    def subscriptionsQueryService
    def subscriptionService
    def comparisonService
    def titleStreamService
    def escapeService
    def deletionService
    def auditService

    public static final String COPY = "COPY"
    public static final String REPLACE = "REPLACE"
    public static final String DO_NOTHING = "DO_NOTHING"

    public static final String WORKFLOW_NEXT_DATES_OWNER_RELATIONS = "WORKFLOW_NEXT_DATES_OWNER_RELATIONS"//1
    public static final String WORKFLOW_NEXT_PACKAGES_ENTITLEMENTS = "WORKFLOW_NEXT_PACKAGES_ENTITLEMENTS"//5
    public static final String WORKFLOW_NEXT_DOCS_ANNOUNCEMENT_TASKS = "WORKFLOW_NEXT_DOCS_ANNOUNCEMENT_TASKS"//2
    public static final String WORKFLOW_NEXT_3 = "WORKFLOW_NEXT_3"//3
    public static final String WORKFLOW_NEXT_PROPERTIES = "WORKFLOW_NEXT_PROPERTIES"//4
    public static final String WORKFLOW_DATES_OWNER_RELATIONS = '1'
    public static final String WORKFLOW_PACKAGES_ENTITLEMENTS = '5'
    public static final String WORKFLOW_DOCS_ANNOUNCEMENT_TASKS = '2'
    public static final String WORKFLOW_SUBSCRIBER = '3'
    public static final String WORKFLOW_PROPERTIES = '4'
    public static final String WORKFLOW_END = '6'

    def possible_date_formats = [
            new SimpleDateFormat('yyyy/MM/dd'),
            new SimpleDateFormat('dd.MM.yyyy'),
            new SimpleDateFormat('dd/MM/yyyy'),
            new SimpleDateFormat('dd/MM/yy'),
            new SimpleDateFormat('yyyy/MM'),
            new SimpleDateFormat('yyyy')
    ]

    private static String INVOICES_FOR_SUB_HQL =
            'select co.invoice, sum(co.costInLocalCurrency), sum(co.costInBillingCurrency), co from CostItem as co where co.sub = :sub group by co.invoice order by min(co.invoice.startDate) desc';

    // TODO Used in Cost per use tab, still needed?
    private static String USAGE_FOR_SUB_IN_PERIOD =
            'select f.reportingYear, f.reportingMonth+1, sum(factValue) ' +
                    'from Fact as f ' +
                    'where f.factFrom >= :start and f.factTo <= :end and f.factType.value=:jr1a and exists ' +
                    '( select ie.tipp.title from IssueEntitlement as ie where ie.subscription = :sub and ie.tipp.title = f.relatedTitle)' +
                    'group by f.reportingYear, f.reportingMonth order by f.reportingYear desc, f.reportingMonth desc';

    // TODO Used in Cost per use tab, still needed?
    private static String TOTAL_USAGE_FOR_SUB_IN_PERIOD =
            'select sum(factValue) ' +
                    'from Fact as f ' +
                    'where f.factFrom >= :start and f.factTo <= :end and f.factType.value=:factType and exists ' +
                    '(select 1 from IssueEntitlement as ie INNER JOIN ie.tipp as tipp ' +
                    'where ie.subscription= :sub  and tipp.title = f.relatedTitle)'

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def index() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()
        def verystarttime = exportService.printStart("subscription")

        log.debug("subscription id:${params.id} format=${response.format}");

        result.transforms = grailsApplication.config.subscriptionTransforms

        result.max = params.max ? Integer.parseInt(params.max) : ((response.format && response.format != "html" && response.format != "all") ? 10000 : result.user.getDefaultPageSizeTMP());
        result.offset = (params.offset && response.format && response.format != "html") ? Integer.parseInt(params.offset) : 0;

        log.debug("max = ${result.max}");

        def pending_change_pending_status = RefdataValue.getByValueAndCategory('Pending', 'PendingChangeStatus')
        def pendingChanges = PendingChange.executeQuery("select pc.id from PendingChange as pc where subscription=? and ( pc.status is null or pc.status = ? ) order by ts desc", [result.subscriptionInstance, pending_change_pending_status]);

        if (result.subscriptionInstance?.isSlaved?.value == "Yes" && pendingChanges) {
            log.debug("Slaved subscription, auto-accept pending changes")
            def changesDesc = []
            pendingChanges.each { change ->
                if (!pendingChangeService.performAccept(change, result.user)) {
                    log.debug("Auto-accepting pending change has failed.")
                } else {
                    changesDesc.add(PendingChange.get(change).desc)
                }
            }
            flash.message = changesDesc
        } else {
            result.pendingChanges = pendingChanges.collect { PendingChange.get(it) }
        }

        // If transformer check user has access to it
        if (params.transforms && !transformerService.hasTransformId(result.user, params.transforms)) {
            flash.error = "It looks like you are trying to use an unvalid transformer or one you don't have access to!"
            params.remove("transforms")
            params.remove("format")
            redirect action: 'currentTitles', params: params
        }

        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
            result.institutional_usage_identifier =
                    OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)
        }

        if (params.mode == "advanced") {
            params.asAt = null
        }

        def base_qry = null;

        def deleted_ie = RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')
        def qry_params = [result.subscriptionInstance]

        def date_filter
        if (params.asAt && params.asAt.length() > 0) {
            def sdf = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'));
            date_filter = sdf.parse(params.asAt)
            result.as_at_date = date_filter;
            result.editable = false;
        } else {
            date_filter = new Date()
            result.as_at_date = date_filter;
        }
        // We dont want this filter to reach SQL query as it will break it.
        def core_status_filter = params.sort == 'core_status'
        if (core_status_filter) params.remove('sort');

        if (params.filter) {
            base_qry = " from IssueEntitlement as ie where ie.subscription = ? "
            if (params.mode != 'advanced') {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                // qry_params.add(deleted_ie);
                base_qry += "and (( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                qry_params.add(date_filter)
                qry_params.add(date_filter)
            }
            base_qry += "and ( ( lower(ie.tipp.title.title) like ? ) or ( exists ( from IdentifierOccurrence io where io.ti.id = ie.tipp.title.id and io.identifier.value like ? ) ) ) "
            qry_params.add("%${params.filter.trim().toLowerCase()}%")
            qry_params.add("%${params.filter}%")
        } else {
            base_qry = " from IssueEntitlement as ie where ie.subscription = ? "
            if (params.mode != 'advanced') {
                // If we are not in advanced mode, hide IEs that are not current, otherwise filter

                base_qry += " and (( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) ) "
                qry_params.add(date_filter)
                qry_params.add(date_filter)
            }
        }

        base_qry += " and ie.status <> ? "
        qry_params.add(deleted_ie);

        if (params.pkgfilter && (params.pkgfilter != '')) {
            base_qry += " and ie.tipp.pkg.id = ? "
            qry_params.add(Long.parseLong(params.pkgfilter));
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += "order by ie.${params.sort} ${params.order} "
        } else {
            base_qry += "order by lower(ie.tipp.title.title) asc"
        }

        result.num_sub_rows = IssueEntitlement.executeQuery("select ie.id " + base_qry, qry_params).size()

        if ((params.format == 'html' || params.format == null) && !params.exportKBart) {
            result.entitlements = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params, [max: result.max, offset: result.offset]);
        } else {
            result.entitlements = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params);
        }

        // Now we add back the sort so that the sortable column will recognize asc/desc
        // Ignore the sorting if we are doing an export
        if (core_status_filter) {
            params.put('sort', 'core_status');
            if (params.format == 'html' || params.format == null) sortOnCoreStatus(result, params);
        }

        exportService.printDuration(verystarttime, "Querying")

        log.debug("subscriptionInstance returning... ${result.num_sub_rows} rows ");
        def filename = "subscription_${escapeService.escapeString(result.subscriptionInstance.dropdownNamingConvention())}"


        if (executorWrapperService.hasRunningProcess(result.subscriptionInstance)) {
            result.processingpc = true
        }

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        if (params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, List> tableData = titleStreamService.generateTitleExportList(result.entitlements)
            out.withWriter { writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
        } else {
            withFormat {
                html result
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    //exportService.StreamOutSubsCSV(out, result.subscriptionInstance, result.entitlements, header)
                    out.close()
                    exportService.printDuration(verystarttime, "Overall Time")
                }
                json {
                    def starttime = exportService.printStart("Building Map")
                    def map = exportService.getSubscriptionMap(result.subscriptionInstance, result.entitlements)
                    exportService.printDuration(starttime, "Building Map")

                    starttime = exportService.printStart("Create JSON")
                    def json = map as JSON
                    exportService.printDuration(starttime, "Create JSON")

                    if (params.transforms) {
                        transformerService.triggerTransform(result.user, filename, params.transforms, json, response)
                    } else {
                        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.json\"")
                        response.contentType = "application/json"
                        render json
                    }
                    exportService.printDuration(verystarttime, "Overall Time")
                }
                xml {
                    def starttime = exportService.printStart("Building XML Doc")
                    def doc = exportService.buildDocXML("Subscriptions")
                    exportService.addSubIntoXML(doc, doc.getDocumentElement(), result.subscriptionInstance, result.entitlements)
                    exportService.printDuration(starttime, "Building XML Doc")

                    if ((params.transformId) && (result.transforms[params.transformId] != null)) {
                        String xml = exportService.streamOutXML(doc, new StringWriter()).getWriter().toString();
                        transformerService.triggerTransform(result.user, filename, result.transforms[params.transformId], xml, response)
                    } else {
                        response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xml\"")
                        response.contentType = "text/xml"
                        starttime = exportService.printStart("Sending XML")
                        exportService.streamOutXML(doc, response.outputStream)
                        exportService.printDuration(starttime, "Sending XML")
                    }
                    exportService.printDuration(verystarttime, "Overall Time")
                }
            }
        }

    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)

        if (params.process  && result.editable) {
            result.result = deletionService.deleteSubscription(result.subscription, false)
        }
        else {
            result.dryRun = deletionService.deleteSubscription(result.subscription, DeletionService.DRY_RUN)
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def unlinkPackage() {
        log.debug("unlinkPackage :: ${params}")
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscription = Subscription.get(params.subscription.toLong())
        result.package = Package.get(params.package.toLong())
        def query = "from IssueEntitlement ie, Package pkg where ie.subscription =:sub and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
        def queryParams = [sub: result.subscription, pkg_id: result.package.id]

        if (result.subscription.isEditableBy(result.user)) {
            result.editable = true
            if (params.confirmed) {
                //delete matches
                IssueEntitlement.withTransaction { status ->
                    removePackagePendingChanges(result.package.id, result.subscription.id, params.confirmed)
                    def deleteIdList = IssueEntitlement.executeQuery("select ie.id ${query}", queryParams)
                    if (deleteIdList) IssueEntitlement.executeUpdate("delete from IssueEntitlement ie where ie.id in (:delList)", [delList: deleteIdList]);
                    SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg=? and sp.subscription=? ", [result.package, result.subscription])

                    flash.message = message(code: 'subscription.details.unlink.successfully')

                }
            } else {
                def numOfPCs = removePackagePendingChanges(result.package.id, result.subscription.id, params.confirmed)

                def numOfIEs = IssueEntitlement.executeQuery("select ie.id ${query}", queryParams).size()
                def conflict_item_pkg = [name: "${g.message(code: "subscription.details.unlink.linkedPackage")}", details: [['link': createLink(controller: 'package', action: 'show', id: result.package.id), 'text': result.package.name]], action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.linkedPackage.action")}"]]
                def conflicts_list = [conflict_item_pkg]

                if (numOfIEs > 0) {
                    def conflict_item_ie = [name: "${g.message(code: "subscription.details.unlink.packageIEs")}", details: [['text': "${g.message(code: "subscription.details.unlink.packageIEs.numbers")} " + numOfIEs]], action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.packageIEs.action")}"]]
                    conflicts_list += conflict_item_ie
                }
                if (numOfPCs > 0) {
                    def conflict_item_pc = [name: "${g.message(code: "subscription.details.unlink.pendingChanges")}", details: [['text': "${g.message(code: "subscription.details.unlink.pendingChanges.numbers")} " + numOfPCs]], action: [actionRequired: false, text: "${g.message(code: "subscription.details.unlink.pendingChanges.numbers.action")}"]]
                    conflicts_list += conflict_item_pc
                }

                return render(template: "unlinkPackageModal", model: [pkg: result.package, subscription: result.subscription, conflicts_list: conflicts_list])
            }
        } else {
            result.editable = false
        }


        redirect(url: request.getHeader('referer'))

    }

    private def removePackagePendingChanges(pkg_id, sub_id, confirmed) {

        def tipp_class = TitleInstancePackagePlatform.class.getName()
        def tipp_id_query = "from TitleInstancePackagePlatform tipp where tipp.pkg.id = ?"
        def change_doc_query = "from PendingChange pc where pc.subscription.id = ? "
        def tipp_ids = TitleInstancePackagePlatform.executeQuery("select tipp.id ${tipp_id_query}", [pkg_id])
        def pendingChanges = PendingChange.executeQuery("select pc.id, pc.changeDoc ${change_doc_query}", [sub_id])

        def pc_to_delete = []
        pendingChanges.each { pc ->
            def parsed_change_info = JSON.parse(pc[1])
            if (parsed_change_info.tippID) {
                pc_to_delete += pc[0]
            } else if (parsed_change_info.changeDoc) {
                def (oid_class, ident) = parsed_change_info.changeDoc.OID.split(":")
                if (oid_class == tipp_class && tipp_ids.contains(ident.toLong())) {
                    pc_to_delete += pc[0]
                }
            } else {
                log.error("Could not decide if we should delete the pending change id:${pc[0]} - ${parsed_change_info}")
            }
        }
        if (confirmed && pc_to_delete) {
            log.debug("Deleting Pending Changes: ${pc_to_delete}")
            def del_pc_query = "delete from PendingChange where id in (:del_list) "
            PendingChange.executeUpdate(del_pc_query, [del_list: pc_to_delete])
        } else {
            return pc_to_delete.size()
        }
    }

    private def sortOnCoreStatus(result, params) {
        result.entitlements.sort { it.getTIP()?.coreStatus(null) }
        if (params.order == 'desc') result.entitlements.reverse(true);
        result.entitlements = result.entitlements.subList(result.offset, (result.offset + result.max).intValue())
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_USER")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_USER")
    })
    def compare() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)

        result
        /*
        result.unionList = []

        result.user = User.get(springSecurityService.principal.id)
        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        if (params.subA?.length() > 0 && params.subB?.length() > 0) {
            log.debug("Subscriptions submitted for comparison ${params.subA} and ${params.subB}.")
            log.debug("Dates submited are ${params.dateA} and ${params.dateB}")

            result.subInsts = []
            result.subDates = []

            def listA
            def listB
            try {
                listA = createCompareList(params.subA, params.dateA, params, result)
                listB = createCompareList(params.subB, params.dateB, params, result)
                if (!params.countA) {
                    def countQuery = "select count(elements(sub.issueEntitlements)) from Subscription sub where sub.id = ?"
                    params.countA = Subscription.executeQuery(countQuery, [result.subInsts.get(0).id])
                    params.countB = Subscription.executeQuery(countQuery, [result.subInsts.get(1).id])
                }
            } catch (IllegalArgumentException e) {
                request.message = e.getMessage()
                return
            }

            result.listACount = listA.size()
            result.listBCount = listB.size()

            def mapA = listA.collectEntries { [it.tipp.title.title, it] }
            def mapB = listB.collectEntries { [it.tipp.title.title, it] }

            //FIXME: It should be possible to optimize the following lines - it is. The whole code can be optimised as it is legacy
            def unionList = mapA.keySet().plus(mapB.keySet()).toList()
            unionList = unionList.unique()
            result.unionListSize = unionList.size()
            unionList.sort()

            def filterRules = [params.insrt ? true : false, params.dlt ? true : false, params.updt ? true : false, params.nochng ? true : false]
            withFormat {
                html {
                    def toIndex = result.offset + result.max < unionList.size() ? result.offset + result.max : unionList.size()
                    result.comparisonMap =
                            institutionsService.generateComparisonMap(unionList, mapA, mapB, result.offset, toIndex.intValue(), filterRules)
                    log.debug("Comparison Map" + result.comparisonMap)
                    result
                }
                csv {
                    try {
                        log.debug("Create CSV Response")
                        def comparisonMap =
                                institutionsService.generateComparisonMap(unionList, mapA, mapB, 0, unionList.size(), filterRules)
                        def dateFormatter = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))

                        response.setHeader("Content-disposition", "attachment; filename=\"subscriptionComparison.csv\"")
                        response.contentType = "text/csv"
                        def out = response.outputStream
                        out.withWriter { writer ->
                            writer.write("${result.subInsts[0].name} on ${params.dateA}, ${result.subInsts[1].name} on ${params.dateB}\n")
                            writer.write('IE Title, pISSN, eISSN, Start Date A, Start Date B, Start Volume A, Start Volume B, Start Issue A, Start Issue B, End Date A, End Date B, End Volume A, End Volume B, End Issue A, End Issue B, Coverage Note A, Coverage Note B, ColorCode\n');
                            log.debug("UnionList size is ${unionList.size}")
                            comparisonMap.each { title, values ->
                                def ieA = values[0]
                                def ieB = values[1]
                                def colorCode = values[2]
                                def pissn = ieA ? ieA.tipp.title.getIdentifierValue('issn') : ieB.tipp.title.getIdentifierValue('issn');
                                def eissn = ieA ? ieA.tipp.title.getIdentifierValue('eISSN') : ieB.tipp.title.getIdentifierValue('eISSN')

                                writer.write("\"${title}\",\"${pissn ?: ''}\",\"${eissn ?: ''}\",\"${formatDateOrNull(dateFormatter, ieA?.startDate)}\",\"${formatDateOrNull(dateFormatter, ieB?.startDate)}\",\"${ieA?.startVolume ?: ''}\",\"${ieB?.startVolume ?: ''}\",\"${ieA?.startIssue ?: ''}\",\"${ieB?.startIssue ?: ''}\",\"${formatDateOrNull(dateFormatter, ieA?.endDate)}\",\"${formatDateOrNull(dateFormatter, ieB?.endDate)}\",\"${ieA?.endVolume ?: ''}\",\"${ieB?.endVolume ?: ''}\",\"${ieA?.endIssue ?: ''}\",\"${ieB?.endIssue ?: ''}\",\"${ieA?.coverageNote ?: ''}\",\"${ieB?.coverageNote ?: ''}\",\"${colorCode}\"\n")
                            }
                            writer.write("END");
                            writer.flush();
                            writer.close();
                        }
                        out.close()

                    } catch (Exception e) {
                        log.error("An Exception was thrown here", e)
                    }
                }
            }
        } else {
            def currentDate = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd')).format(new Date())
            params.dateA = currentDate
            params.dateB = currentDate
            params.insrt = "Y"
            params.dlt = "Y"
            params.updt = "Y"

            if (contextService.getOrg()) {
                result.institutionName = contextService.getOrg().getName()
                log.debug("FIND ORG NAME ${result.institutionName}")
            }
            flash.message = message(code: 'subscription.compare.note', default: "Please select two subscriptions for comparison")
        }
        */
    }

    def formatDateOrNull(formatter, date) {
        def result;
        if (date) {
            result = formatter.format(date)
        } else {
            result = ''
        }
        return result
    }

    def createCompareList(sub, dateStr, params, result) {
        def returnVals = [:]
        def sdf = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))
        def date = dateStr ? sdf.parse(dateStr) : new Date()
        def subId = sub.substring(sub.indexOf(":") + 1)

        def subInst = Subscription.get(subId)
        if (subInst.startDate > date || subInst.endDate < date) {
            def errorMsg = "${subInst.name} start date is: ${sdf.format(subInst.startDate)} and end date is: ${sdf.format(subInst.endDate)}. You have selected to compare it on date ${sdf.format(date)}."
            throw new IllegalArgumentException(errorMsg)
        }

        result.subInsts.add(subInst)

        result.subDates.add(sdf.format(date))

        def queryParams = [subInst]
        def query = generateIEQuery(params, queryParams, true, date)

        def list = IssueEntitlement.executeQuery("select ie " + query, queryParams);
        list

    }

    private def generateIEQuery(params, qry_params, showDeletedTipps, asAt) {

        def base_qry = "from IssueEntitlement as ie where ie.subscription = ? and ie.tipp.title.status.value != 'Deleted' "

        if (showDeletedTipps == false) {
            base_qry += "and ie.tipp.status.value != 'Deleted' "
        }

        if (params.filter) {
            base_qry += " and ( ( lower(ie.tipp.title.title) like ? ) or ( exists ( from IdentifierOccurrence io where io.ti.id = ie.tipp.title.id and io.identifier.value like ? ) ) )"
            qry_params.add("%${params.filter.trim().toLowerCase()}%")
            qry_params.add("%${params.filter}%")
        }

        if (params.startsBefore && params.startsBefore.length() > 0) {
            def sdf = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'));
            def d = sdf.parse(params.startsBefore)
            base_qry += " and ie.startDate <= ?"
            qry_params.add(d)
        }

        if (asAt != null) {
            base_qry += " and ( ( ? >= coalesce(ie.tipp.accessStartDate, ie.startDate) ) and ( ( ? <= ie.tipp.accessEndDate ) or ( ie.tipp.accessEndDate is null ) ) ) "
            qry_params.add(asAt);
            qry_params.add(asAt);
        }

        return base_qry
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def subscriptionBatchUpdate() {

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        // def formatter = new java.text.SimpleDateFormat("MM/dd/yyyy")
        SimpleDateFormat formatter = new SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))

        // def subscriptionInstance = Subscription.get(params.id)
        // def user = User.get(springSecurityService.principal.id)
        // userAccessCheck(subscriptionInstance, user, 'edit')

        log.debug("subscriptionBatchUpdate ${params}");

        params.each { p ->
            if (p.key.startsWith('_bulkflag.') && (p.value == 'on')) {
                def ie_to_edit = p.key.substring(10);

                def ie = IssueEntitlement.get(ie_to_edit)

                if (params.bulkOperation == "edit") {

                    if (params.bulk_start_date && (params.bulk_start_date.trim().length() > 0)) {
                        ie.startDate = formatter.parse(params.bulk_start_date)
                    }

                    if (params.bulk_end_date && (params.bulk_end_date.trim().length() > 0)) {
                        ie.endDate = formatter.parse(params.bulk_end_date)
                    }

                    if (params.bulk_access_start_date && (params.bulk_access_start_date.trim().length() > 0)) {
                        ie.accessStartDate = formatter.parse(params.bulk_access_start_date)
                    }

                    if (params.bulk_access_end_date && (params.bulk_access_end_date.trim().length() > 0)) {
                        ie.accessEndDate = formatter.parse(params.bulk_access_end_date)
                    }

                    if (params.bulk_embargo && (params.bulk_embargo.trim().length() > 0)) {
                        ie.embargo = params.bulk_embargo
                    }

                    if (params.bulk_medium.trim().length() > 0) {
                        def selected_refdata = genericOIDService.resolveOID(params.bulk_medium.trim())
                        log.debug("Selected medium is ${selected_refdata}");
                        ie.medium = selected_refdata
                    }

                    if (params.bulk_coverage && (params.bulk_coverage.trim().length() > 0)) {
                        ie.coverageDepth = params.bulk_coverage
                    }

                    if (!ie.save(flush: true)) {
                        log.error("Problem saving ${ie.errors}")
                    }
                } else if (params.bulkOperation == "remove") {
                    log.debug("Updating ie ${ie.id} status to deleted");
                    def deleted_ie = RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')
                    ie.status = deleted_ie;
                    if (!ie.save(flush: true)) {
                        log.error("Problem saving ${ie.errors}")
                    }
                }
            }
        }

        redirect action: 'index', params: [id: result.subscriptionInstance?.id, sort: params.sort, order: params.order, offset: params.offset, max: params.max]
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def addEntitlements() {
        log.debug("addEntitlements .. params: ${params}")

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        result.max = params.max ? Integer.parseInt(params.max) : request.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        def tipp_deleted = RefdataCategory.lookupOrCreate(RefdataCategory.TIPP_STATUS, 'Deleted');
        def ie_deleted = RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')

        log.debug("filter: \"${params.filter}\"");

        List errorList = []
        if (result.subscriptionInstance) {
            // We need all issue entitlements from the parent subscription where no row exists in the current subscription for that item.
            def basequery = null;
            def qry_params = [result.subscriptionInstance, tipp_deleted, result.subscriptionInstance, ie_deleted]

            if (params.filter) {
                log.debug("Filtering....");
                basequery = "from TitleInstancePackagePlatform tipp where tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = ? ) and tipp.status != ? and ( not exists ( select ie from IssueEntitlement ie where ie.subscription = ? and ie.tipp.id = tipp.id and ie.status != ? ) ) and ( ( lower(tipp.title.title) like ? ) OR ( exists ( select io from IdentifierOccurrence io where io.ti.id = tipp.title.id and io.identifier.value like ? ) ) ) "
                qry_params.add("%${params.filter.trim().toLowerCase()}%")
                qry_params.add("%${params.filter}%")
            } else {
                basequery = "from TitleInstancePackagePlatform tipp where tipp.pkg in ( select pkg from SubscriptionPackage sp where sp.subscription = ? ) and tipp.status != ? and ( not exists ( select ie from IssueEntitlement ie where ie.subscription = ? and ie.tipp.id = tipp.id and ie.status != ? ) )"
            }

            if (params.endsAfter && params.endsAfter.length() > 0) {
                def sdf = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'));
                def d = sdf.parse(params.endsAfter)
                basequery += " and tipp.endDate >= ?"
                qry_params.add(d)
            }

            if (params.startsBefore && params.startsBefore.length() > 0) {
                def sdf = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'));
                def d = sdf.parse(params.startsBefore)
                basequery += " and tipp.startDate <= ?"
                qry_params.add(d)
            }


            if (params.pkgfilter && (params.pkgfilter != '')) {
                basequery += " and tipp.pkg.id = ? "
                qry_params.add(Long.parseLong(params.pkgfilter));
            }


            if ((params.sort != null) && (params.sort.length() > 0)) {
                basequery += " order by tipp.${params.sort} ${params.order} "
            } else {
                basequery += " order by tipp.title.title asc "
            }

            log.debug("Query ${basequery} ${qry_params}");

            result.num_tipp_rows = IssueEntitlement.executeQuery("select tipp.id " + basequery, qry_params).size()
            result.tipps = IssueEntitlement.executeQuery("select tipp ${basequery}", qry_params, [max: result.max, offset: result.offset])
            LinkedHashMap identifiers = [zdbIds:[],onlineIds:[],printIds:[],unidentified:[]]

            if(params.kbartPreselect && !params.pagination) {
                CommonsMultipartFile kbartFile = params.kbartPreselect
                identifiers.filename = kbartFile.originalFilename
                InputStream stream = kbartFile.getInputStream()
                ArrayList<String> rows = stream.text.split('\n')
                int zdbCol = -1, onlineIdentifierCol = -1, printIdentifierCol = -1
                //read off first line of KBART file
                rows[0].split('\t').eachWithIndex { headerCol, int c ->
                    switch(headerCol.toLowerCase()) {
                        case "zdb_id": zdbCol = c
                            break
                        case "print_identifier": printIdentifierCol = c
                            break
                        case "online_identifier": onlineIdentifierCol = c
                            break
                    }
                }
                //after having read off the header row, pop the first row
                rows.remove(0)
                //now, assemble the identifiers available to highlight
                rows.each { row ->
                    ArrayList<String> cols = row.split('\t')
                    List idCandidates = []
                    if(zdbCol >= 0 && cols[zdbCol]) {
                        identifiers.zdbIds.add(cols[zdbCol])
                        idCandidates.add([namespace:'zdb',value:cols[zdbCol]])
                    }
                    if(onlineIdentifierCol >= 0 && cols[onlineIdentifierCol]) {
                        identifiers.onlineIds.add(cols[onlineIdentifierCol])
                        idCandidates.add([namespace:'eissn',value:cols[onlineIdentifierCol]])
                        idCandidates.add([namespace:'isbn',value:cols[onlineIdentifierCol]])
                    }
                    if(printIdentifierCol >= 0 && cols[printIdentifierCol]) {
                        identifiers.printIds.add(cols[printIdentifierCol])
                        idCandidates.add([namespace:'issn',value:cols[printIdentifierCol]])
                        idCandidates.add([namespace:'pisbn',value:cols[printIdentifierCol]])
                    }
                    if(((zdbCol >= 0 && cols[zdbCol].trim().isEmpty()) || zdbCol < 0) &&
                       ((onlineIdentifierCol >= 0 && cols[onlineIdentifierCol].trim().isEmpty()) || onlineIdentifierCol < 0) &&
                       ((printIdentifierCol >= 0 && cols[printIdentifierCol].trim().isEmpty()) || printIdentifierCol < 0)) {
                        identifiers.unidentified.add('"'+cols[0]+'"')
                    }
                    else {
                        //make checks ...
                        //is title in LAS:eR?
                        //List tiObj = TitleInstancePackagePlatform.executeQuery('select tipp from TitleInstancePackagePlatform tipp join tipp.title ti join ti.ids identifiers where identifiers.identifier.value in :idCandidates',[idCandidates:idCandidates])
                        //log.debug(idCandidates)
                        def tiObj = TitleInstance.findByIdentifier(idCandidates)
                        if(tiObj) {
                            //is title already added?
                            List issueEntitlement = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.tipp in :tipp and ie.subscription = :sub',[tipp:tiObj,sub:result.subscriptionInstance])
                            if(issueEntitlement) {
                                errorList.add("${cols[0]}&#9;${cols[zdbCol] ?: " "}&#9;${cols[onlineIdentifierCol] ?: " "}&#9;${cols[printIdentifierCol] ?: " "}&#9;${message(code:'subscription.details.addEntitlements.titleAlreadyAdded')}")
                            }
                            /*else if(!issueEntitlement) {
                                errors += g.message([code:'subscription.details.addEntitlements.titleNotMatched',args:cols[0]])
                            }*/
                        }
                        else if(!tiObj) {
                            errorList.add("${cols[0]}&#9;${cols[zdbCol] ?: " "}&#9;${cols[onlineIdentifierCol] ?: " "}&#9;${cols[printIdentifierCol] ?: " "}&#9;${message(code:'subscription.details.addEntitlements.titleNotInERMS')}")
                        }
                    }
                }
                result.identifiers = identifiers
                params.remove("kbartPreselct")
            }
            else if(params.identifiers) {
                result.identifiers = JSON.parse(params.identifiers)
            }
            result.checked = []
            result.tipps.eachWithIndex { tipp, int t ->
                String serial
                String electronicSerial
                String checked = ""
                if(tipp.title.type.equals(RDStore.TITLE_TYPE_EBOOK)) {
                    serial = tipp.title.getIdentifierValue('pISBN')
                    electronicSerial = tipp?.title?.getIdentifierValue('ISBN')
                }
                else if(tipp.title.type.equals(RDStore.TITLE_TYPE_JOURNAL)) {
                    serial = tipp?.title?.getIdentifierValue('ISSN')
                    electronicSerial = tipp?.title?.getIdentifierValue('eISSN')
                }
                if(result.identifiers?.zdbIds?.indexOf(tipp.title.getIdentifierValue('zdb')) > -1) {
                    checked = "checked"
                }
                else if(result.identifiers?.onlineIds?.indexOf(electronicSerial) > -1) {
                    checked = "checked"
                }
                else if(result.identifiers?.printIds?.indexOf(serial) > -1) {
                    checked = "checked"
                }
                result.checked[t] = checked
            }
            if(result.identifiers && result.identifiers.unidentified.size() > 0) {
                String unidentifiedTitles = result.identifiers.unidentified.join(", ")
                String escapedFileName
                try {
                    escapedFileName = StringEscapeCategory.encodeAsHtml(result.identifiers.filename)
                }
                catch (Exception | Error e) {
                    log.error(e.printStackTrace())
                    escapedFileName = result.identifiers.filename
                }
                errorList.add(g.message(code:'subscription.details.addEntitlements.unidentified',args:[escapedFileName, unidentifiedTitles]))
            }
        } else {
            result.num_sub_rows = 0;
            result.tipps = []
        }
        if(errorList)
            flash.error = "<pre style='font-family:Lato,Arial,Helvetica,sans-serif;'>"+errorList.join("\n")+"</pre>"
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def previous() {
        previousAndExpected(params, 'previous');
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def members() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
//        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
//        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.org)

        //if (params.showDeleted == 'Y') {

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.subscriptionInstance,
                RDStore.SUBSCRIPTION_DELETED
        )
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }
        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

//        def deletedSubChilds = Subscription.findAllByInstanceOfAndStatus(
//                result.subscriptionInstance,
//                RDStore.SUBSCRIPTION_DELETED
//          )
//        result.deletedSubChilds = deletedSubChilds
        //}
        //else {
        //    result.subscriptionChildren = Subscription.executeQuery(
        //           "select sub from Subscription as sub where sub.instanceOf = ? and sub.status.value != 'Deleted'",
        //            [result.subscriptionInstance]
        //    )
        //}

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        result.filterSet = params.filterSet ? true : false

        def sdf = new SimpleDateFormat('yyyy-MM-dd')
        def datetoday = sdf.format(new Date(System.currentTimeMillis()))
        def message = escapeService.escapeString(result.subscription.name) + "_" + g.message(code: 'subscriptionDetails.members.members') + "_" + datetoday
        def orgs = []
        if (params.exportXLS || params.format) {
            Map allContacts = Person.getPublicAndPrivateEmailByFunc('General contact person')
            Map publicContacts = allContacts.publicContacts
            Map privateContacts = allContacts.privateContacts
            result.filteredSubChilds.each { row ->
                Subscription subChild = (Subscription) row.sub
                row.orgs.each { subscr ->
                    def org = [:]
                    org.name = subscr.name
                    org.sortname = subscr.sortname
                    org.shortname = subscr.shortname
                    org.libraryType = subscr.libraryType
                    org.libraryNetwork = subscr.libraryNetwork
                    org.funderType = subscr.funderType
                    org.federalState = subscr.federalState
                    org.country = subscr.country
                    org.startDate = subChild.startDate
                    org.endDate = subChild.endDate
                    org.status = subChild.status
                    org.customProperties = subscr.customProperties
                    org.privateProperties = subscr.privateProperties
                    String generalContacts = ""
                    if (publicContacts.get(subscr))
                        generalContacts += publicContacts.get(subscr).join("; ") + "; "
                    if (privateContacts.get(subscr))
                        generalContacts += privateContacts.get(subscr).join("; ")
                    org.generalContacts = generalContacts
                    orgs << org
                }
            }
        }

        if (params.exportXLS) {
            exportOrg(orgs, message, true, 'xlsx')
            return
        } else {
            withFormat {
                html {
                    result
                }
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${message}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        writer.write((String) exportOrg(orgs, message, true, "csv"))
                    }
                    out.close()
                }
            }
        }
    }
    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def linkLicenseConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentLicense = result.parentSub.owner

        result.validLicenses = []
        if(result.parentLicense) {
            result.validLicenses << result.parentLicense
        }

        def childLicenses = License.where {
            (instanceOf == result.parentLicense) && (status.value != 'Deleted')
        }

        childLicenses?.each {
            result.validLicenses << it
        }


        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID =  params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processLinkLicenseConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentLicense = result.parentSub.owner

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )


        def changeAccepted = []
        if (params.license_All) {
            def lic = License.get(params.license_All)
            validSubChilds.each { subChild ->
                def sub = Subscription.get(subChild.id)
                sub.owner = lic
                if (sub.save(flush: true)) {
                    changeAccepted << subChild?.dropdownNamingConvention(result.institution)
                }
            }
            if (changeAccepted) {
                flash.message = message(code: 'subscription.linkLicenseConsortium.changeAcceptedAll', args: [changeAccepted.join(', ')])
            }


        } else {
            validSubChilds.each {
                if (params."license_${it.id}") {
                    def newLicense = License.get(params."license_${it.id}")
                    if (it.owner != newLicense) {
                        def sub = Subscription.get(it.id)
                        sub.owner = newLicense
                        if (sub.save(flush: true)) {
                            changeAccepted << it?.dropdownNamingConvention(result.institution)
                        }
                    }
                }
            }
            if (changeAccepted) {
                flash.message = message(code: 'subscription.linkLicenseConsortium.changeAcceptedAll', args: [changeAccepted.join('')])
            }

        }


        redirect(action: 'linkLicenseConsortia', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processUnLinkLicenseConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentLicense = result.parentSub.owner

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )

        def removeLic = []
        validSubChilds.each { subChild ->
                def sub = Subscription.get(subChild.id)
                sub.owner = null
                if (sub.save(flush: true)) {
                    removeLic << subChild?.dropdownNamingConvention(result.institution)
                }
            }
            if (removeLic) {
                flash.message = message(code: 'subscription.linkLicenseConsortium.removeAcceptedAll', args: [removeLic.join(', ')])
            }


        redirect(action: 'linkLicenseConsortia', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def linkPackagesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentPackages = result.parentSub.packages.sort { it.pkg.name }

        result.validPackages
        result.validPackages = result.parentPackages

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID = params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processLinkPackagesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentPackages = result.parentSub.packages.sort { it.pkg.name }

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )


        def changeAccepted = []
        def changeAcceptedwithIE = []
        def changeFailed = []


        if (params.package_All) {
            def pkg_to_link = SubscriptionPackage.get(params.package_All).pkg

            validSubChilds.each { subChild ->

                if (!(pkg_to_link in subChild.packages.pkg)) {

                    if (params.withIssueEntitlements) {

                        pkg_to_link.addToSubscription(subChild, true)
                        changeAcceptedwithIE << subChild?.dropdownNamingConvention(result.institution)

                    } else {
                        pkg_to_link.addToSubscription(subChild, false)
                        changeAccepted << subChild?.dropdownNamingConvention(result.institution)

                    }
                } else {
                    changeFailed << subChild?.dropdownNamingConvention(result.institution)
                }

            }

            if (changeAccepted) {
                flash.message = message(code: 'subscription.linkPackagesConsortium.changeAcceptedAll', args: [pkg_to_link.name, changeAccepted.join(", ")])
            }
            if (changeAcceptedwithIE) {
                flash.message = message(code: 'subscription.linkPackagesConsortium.changeAcceptedIEAll', args: [pkg_to_link.name, changeAcceptedwithIE.join(", ")])
            }


        } else {
            validSubChilds.each { subChild ->

                if (params."package_${subChild.id}") {
                    def pkg_to_link = SubscriptionPackage.get(params."package_${subChild.id}").pkg

                    if (!(pkg_to_link in subChild.packages.pkg)) {

                        if (params.withIssueEntitlements) {

                            pkg_to_link.addToSubscription(subChild, true)
                            changeAcceptedwithIE << subChild?.dropdownNamingConvention(result.institution)

                        } else {
                            pkg_to_link.addToSubscription(subChild, false)
                            changeAccepted << subChild?.dropdownNamingConvention(result.institution)

                        }
                    } else {
                        changeFailed << subChild?.dropdownNamingConvention(result.institution)
                    }
                }
            }
            if (changeAccepted) {
                flash.message = message(code: 'subscription.linkPackagesConsortium.changeAcceptedAll', args: [changeAccepted.join(" ")])
            }
            if (changeAcceptedwithIE) {
                flash.message = message(code: 'subscription.linkPackagesConsortium.changeAcceptedIEAll', args: [changeAcceptedwithIE.join(" ")])
            }
            if (changeFailed) {
                flash.error = message(code: 'subscription.linkPackagesConsortium.changeFailedAll', args: [changeFailed.join(" ")])
            }
        }


        redirect(action: 'linkPackagesConsortia', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processUnLinkPackagesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        result.parentPackages = result.parentSub.packages.sort { it.pkg.name }

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )

        validSubChilds.each { subChild ->

            subChild.packages.pkg.each { pkg ->

                def query = "from IssueEntitlement ie, Package pkg where ie.subscription =:sub and pkg.id =:pkg_id and ie.tipp in ( select tipp from TitleInstancePackagePlatform tipp where tipp.pkg.id = :pkg_id ) "
                def queryParams = [sub: subChild, pkg_id: pkg.id]



                if (subChild.isEditableBy(result.user)) {
                    result.editable = true
                    if (params.withIE) {
                        //delete matches
                        IssueEntitlement.withTransaction { status ->
                            removePackagePendingChanges(pkg.id, subChild.id, params.withIE)
                            def deleteIdList = IssueEntitlement.executeQuery("select ie.id ${query}", queryParams)
                            if (deleteIdList) IssueEntitlement.executeUpdate("delete from IssueEntitlement ie where ie.id in (:delList)", [delList: deleteIdList]);
                            SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg=? and sp.subscription=? ", [pkg, subChild])

                            flash.message = message(code: 'subscription.linkPackagesConsortium.unlinkInfo.withIE.successful')
                        }
                    } else {
                        SubscriptionPackage.executeUpdate("delete from SubscriptionPackage sp where sp.pkg=? and sp.subscription=? ", [pkg, subChild])

                        flash.message = message(code: 'subscription.linkPackagesConsortium.unlinkInfo.onlyPackage.successful')
                    }
                }
            }
        }


        redirect(action: 'linkPackagesConsortia', id: params.id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def propertiesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

        params.remove('filterPropDef')


        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        //result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.org)
        result.propList = result.parentSub.privateProperties.type + result.parentSub.customProperties.type


        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID = params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def subscriptionPropertiesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )
        //Sortieren
        result.validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        def oldID = params.id
        params.id = result.parentSub.id

        ArrayList<Long> filteredOrgIds = getOrgIdsForFilter()
        result.filteredSubChilds = new ArrayList<Subscription>()
        result.validSubChilds.each { sub ->
            List<Org> subscr = sub.getAllSubscribers()
            def filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
            }
        }

        params.id = oldID

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processPropertiesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )

        if (params.filterPropDef && params.filterPropValue) {

            def filterPropDef = params.filterPropDef
            def propDef = genericOIDService.resolveOID(filterPropDef.replace(" ", ""))


            def newProperties = 0
            def changeProperties = 0

            if (propDef) {
                validSubChilds.each { subChild ->

                    if (propDef?.tenant != null) {
                        //private Property
                        def owner = subChild

                        def existingProps = owner.privateProperties.findAll {
                            it.owner.id == owner.id &&  it.type.id == propDef.id
                        }
                        existingProps.removeAll { it.type.name != propDef.name } // dubious fix

                        if (existingProps.size() == 0 || propDef.multipleOccurrence) {
                            def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, owner, propDef)
                            if (newProp.hasErrors()) {
                                log.error(newProp.errors)
                            } else {
                                log.debug("New private property created: " + newProp.type.name)

                                newProperties++
                                def prop = setProperty(newProp, params.filterPropValue)
                            }
                        }

                        if (existingProps?.size() == 1){
                            def privateProp = SubscriptionPrivateProperty.get(existingProps[0].id)
                            changeProperties++
                            def prop = setProperty(privateProp, params.filterPropValue)

                        }

                    } else {
                        //custom Property
                        def owner = subChild

                        def existingProp = owner.customProperties.find {
                            it.type.id == propDef.id && it.owner.id == owner.id
                        }

                        if (existingProp == null || propDef.multipleOccurrence) {
                            def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, owner, propDef)
                            if (newProp.hasErrors()) {
                                log.error(newProp.errors)
                            } else {
                                log.debug("New custom property created: " + newProp.type.name)
                                newProperties++
                                def prop = setProperty(newProp, params.filterPropValue)
                            }
                        }

                        if (existingProp){
                            def customProp = SubscriptionCustomProperty.get(existingProp.id)
                            changeProperties++
                            def prop = setProperty(customProp, params.filterPropValue)

                        }
                    }

                }
                flash.message = message(code: 'subscription.propertiesConsortia.successful', args: [newProperties, changeProperties])
            }

        }

        def filterPropDef = params.filterPropDef
        def id = params.id
        redirect(action: 'propertiesConsortia', id: id, params: [filterPropDef: filterPropDef])
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processSubscriptionPropertiesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )
        def change = []
                validSubChilds.each { subChild ->

                    def sdf = new DateUtil().getSimpleDateFormat_NoTime()
                    def startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                    def endDate = params.valid_to ? sdf.parse(params.valid_to) : null


                    if(startDate && !auditService.getAuditConfig(subChild?.instanceOf, 'startDate'))
                    {
                        subChild?.startDate = startDate
                        change << message(code: 'default.startDate.label')
                    }

                    if(endDate && !auditService.getAuditConfig(subChild?.instanceOf, 'endDate'))
                    {
                        subChild?.endDate = endDate
                        change << message(code: 'default.endDate.label')
                    }


                    if(params.status && !auditService.getAuditConfig(subChild?.instanceOf, 'status'))
                    {
                        subChild?.status = RefdataValue.get(params.status) ?: subChild?.status
                        change << message(code: 'subscription.status.label')
                    }

                    if(params.form && !auditService.getAuditConfig(subChild?.instanceOf, 'form'))
                    {
                        subChild?.form = RefdataValue.get(params.form) ?: subChild?.form
                        change << message(code: 'subscription.form.label')
                    }

                    if(params.resource && !auditService.getAuditConfig(subChild?.instanceOf, 'resource'))
                    {
                        subChild?.resource = RefdataValue.get(params.resource) ?: subChild?.resource
                        change << message(code: 'subscription.resource.label')
                    }

                    if (subChild?.isDirty()) {
                        subChild?.save(flush: true)
                    }
                }
        if(change){
            flash.message = message(code: 'subscription.subscriptionPropertiesConsortia.changes', args: [change?.unique { a, b -> a <=> b }.join(', ').toString()])
        }

        def id = params.id
        redirect(action: 'subscriptionPropertiesConsortia', id: id)
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM", affil = "INST_ADM")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_CONSORTIUM", "INST_ADM")
    })
    def processDeletePropertiesConsortia() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.filterPropDef = params.filterPropDef ? genericOIDService.resolveOID(params.filterPropDef.replace(" ", "")) : null

        result.parentSub = result.subscriptionInstance.instanceOf ? result.subscriptionInstance.instanceOf : result.subscriptionInstance

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.parentSub,
                RDStore.SUBSCRIPTION_DELETED
        )

        if (params.filterPropDef) {

            def filterPropDef = params.filterPropDef
            def propDef = genericOIDService.resolveOID(filterPropDef.replace(" ", ""))

            def deletedProperties = 0

            if (propDef) {
                validSubChilds.each { subChild ->

                    if (propDef?.tenant != null) {
                        //private Property
                        def owner = subChild

                        def existingProps = owner.privateProperties.findAll {
                            it.owner.id == owner.id &&  it.type.id == propDef.id
                        }
                        existingProps.removeAll { it.type.name != propDef.name } // dubious fix


                        if (existingProps?.size() == 1 ){
                            def privateProp = SubscriptionPrivateProperty.get(existingProps[0].id)

                            try {
                                owner.privateProperties.remove(privateProp)
                                owner.refresh()
                                privateProp?.delete(failOnError: true, flush: true)
                                deletedProperties++
                            } catch (Exception e)
                            {
                                log.error(e)
                            }

                        }

                    } else {
                        //custom Property
                        def owner = subChild

                        def existingProp = owner.customProperties.find {
                            it.type.id == propDef.id && it.owner.id == owner.id
                        }


                        if (existingProp && !(existingProp.hasProperty('instanceOf') && existingProp.instanceOf && AuditConfig.getConfig(existingProp.instanceOf))){
                            def customProp = SubscriptionCustomProperty.get(existingProp.id)

                            try {
                                customProp?.owner = null
                                customProp.save()
                                owner.customProperties.remove(customProp)
                                customProp?.delete(failOnError: true, flush: true)
                                owner.refresh()
                                deletedProperties++
                            } catch (Exception e){
                                log.error(e)
                            }

                        }
                    }

                }
                flash.message = message(code: 'subscription.propertiesConsortia.deletedProperties', args: [deletedProperties])
            }

        }

        def filterPropDef = params.filterPropDef
        def id = params.id
        redirect(action: 'propertiesConsortia', id: id, params: [filterPropDef: filterPropDef])
    }

    private ArrayList<Long> getOrgIdsForFilter() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        ArrayList<Long> resultOrgIds
        def tmpParams = params.clone()
        tmpParams.remove("max")
        tmpParams.remove("offset")
        if (accessService.checkPerm("ORG_CONSORTIUM"))
            tmpParams.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        else if (accessService.checkPerm("ORG_INST_COLLECTIVE"))
            tmpParams.comboType = RDStore.COMBO_TYPE_DEPARTMENT.value
        def fsq = filterService.getOrgComboQuery(tmpParams, result.institution)

        if (tmpParams.filterPropDef) {
            fsq = propertyService.evalFilterQuery(tmpParams, fsq.query, 'o', fsq.queryParams)
        }
        fsq.query = fsq.query.replaceFirst("select o from ", "select o.id from ")
        Org.executeQuery(fsq.query, fsq.queryParams, tmpParams)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def addMembers() {
        log.debug("addMembers ..")

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        if (accessService.checkPerm('ORG_CONSORTIUM')) {
            params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
            def fsq = filterService.getOrgComboQuery(params, result.institution)
            result.cons_members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            result.cons_members_disabled = []
            result.cons_members.each { it ->
                if (Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
                        [result.subscriptionInstance, it.id])
                ) {
                    result.cons_members_disabled << it.id
                }
            }
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processAddMembers() {
        log.debug(params)

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        def orgType = [com.k_int.kbplus.RefdataValue.getByValueAndCategory('Institution', 'OrgRoleType')?.id.toString()]
        if ((com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in result.institution?.getallOrgTypeIds())) {
            orgType = [com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id.toString()]
        }

        def subStatus = RefdataValue.get(params.subStatus) ?: RefdataCategory.lookupOrCreate('Subscription Status', 'Current')

        def role_sub = RDStore.OR_SUBSCRIBER_CONS
        def role_sub_cons = RDStore.OR_SUBSCRIPTION_CONSORTIA
        def role_sub_hidden = RDStore.OR_SUBSCRIBER_CONS_HIDDEN
        def role_lic = RDStore.OR_LICENSEE_CONS
        def role_lic_cons = RDStore.OR_LICENSING_CONSORTIUM

        def role_provider = RefdataValue.getByValueAndCategory('Provider', 'Organisational Role')
        def role_agency = RefdataValue.getByValueAndCategory('Agency', 'Organisational Role')


        if (accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')) {

            if ((com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in result.institution?.getallOrgTypeIds())) {
                def cons_members = []
                def licenseCopy

                params.list('selectedOrgs').each { it ->
                    def fo = Org.findById(Long.valueOf(it))
                    cons_members << Combo.executeQuery("select c.fromOrg from Combo as c where c.toOrg = ? and c.fromOrg = ?", [result.institution, fo])
                }

                def subLicense = result.subscriptionInstance.owner

                List<Subscription> synShareTargetList = []

                cons_members.each { cm ->

                    def postfix = (cons_members.size() > 1) ? 'Teilnehmervertrag' : (cm.get(0).shortname ?: cm.get(0).name)

                    if (subLicense) {
                        def subLicenseParams = [
                                lic_name     : "${subLicense.reference} (${postfix})",
                                isSlaved     : params.isSlaved,
                                asOrgType: orgType,
                                copyStartEnd : true
                        ]

                        if (params.generateSlavedLics == 'explicit') {
                            licenseCopy = institutionsService.copyLicense(
                                    subLicense, subLicenseParams, InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                        }
                        else if (params.generateSlavedLics == 'shared' && !licenseCopy) {
                            licenseCopy = institutionsService.copyLicense(
                                    subLicense, subLicenseParams, InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                        }
                        else if (params.generateSlavedLics == 'reference' && !licenseCopy) {
                            licenseCopy = genericOIDService.resolveOID(params.generateSlavedLicsReference)
                        }

                        if (licenseCopy) {
                            new OrgRole(org: cm, lic: licenseCopy, roleType: role_lic).save()
                        }
                    }

                    //ERMS-1155
                    if (true) {
                        log.debug("Generating seperate slaved instances for consortia members")

                        def sdf = new DateUtil().getSimpleDateFormat_NoTime()
                        def startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                        def endDate = params.valid_to ? sdf.parse(params.valid_to) : null

                        def cons_sub = new Subscription(
                                type: result.subscriptionInstance.type ?: "",
                                status: subStatus,
                                name: result.subscriptionInstance.name,
                                //name: result.subscriptionInstance.name + " (" + (cm.get(0).shortname ?: cm.get(0).name) + ")",
                                startDate: startDate,
                                endDate: endDate,
                                administrative: result.subscriptionInstance.administrative,
                                manualRenewalDate: result.subscriptionInstance.manualRenewalDate,
                                /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                                identifier: java.util.UUID.randomUUID().toString(),
                                instanceOf: result.subscriptionInstance,
                                isSlaved: RefdataValue.getByValueAndCategory('Yes', 'YN'),
                                isPublic: result.subscriptionInstance.isPublic,
                                impId: java.util.UUID.randomUUID().toString(),
                                owner: licenseCopy,
                                resource: result.subscriptionInstance.resource ?: null,
                                form: result.subscriptionInstance.form ?: null
                        )

                        if (!cons_sub.save()) {
                            cons_sub?.errors.each { e ->
                                log.debug("Problem creating new sub: ${e}")
                            }
                            flash.error = cons_sub.errors
                        }


                        if (cons_sub) {

                            if(cons_sub.administrative)
                                new OrgRole(org: cm, sub: cons_sub, roleType: role_sub_hidden).save()
                            else
                                new OrgRole(org: cm, sub: cons_sub, roleType: role_sub).save()
                            new OrgRole(org: result.institution, sub: cons_sub, roleType: role_sub_cons).save()

                            synShareTargetList.add(cons_sub)
                        }

                        if (cons_sub) {

                            SubscriptionCustomProperty.findAllByOwner(result.subscriptionInstance).each { scp ->
                                AuditConfig ac = AuditConfig.getConfig(scp)

                                if (ac) {
                                    // multi occurrence props; add one additional with backref
                                    if (scp.type.multipleOccurrence) {
                                        def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, cons_sub, scp.type)
                                        additionalProp = scp.copyInto(additionalProp)
                                        additionalProp.instanceOf = scp
                                        additionalProp.save(flush: true)
                                    }
                                    else {
                                        // no match found, creating new prop with backref
                                        def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, cons_sub, scp.type)
                                        newProp = scp.copyInto(newProp)
                                        newProp.instanceOf = scp
                                        newProp.save(flush: true)
                                    }
                                }
                            }
                        }
                    }
                }

                result.subscriptionInstance.syncAllShares(synShareTargetList)

                redirect controller: 'subscription', action: 'members', params: [id: result.subscriptionInstance?.id]
            } else {
                redirect controller: 'subscription', action: 'show', params: [id: result.subscriptionInstance?.id]
            }
        } else {
            redirect controller: 'subscription', action: 'show', params: [id: result.subscriptionInstance?.id]
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    @Deprecated
    def deleteMember() {
        log.debug(params)

        return
        /*
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        def delSubscription = genericOIDService.resolveOID(params.target)
        def delInstitution = delSubscription?.getSubscriber()

        def deletedStatus = RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')

        if (delSubscription?.hasPerm("edit", result.user)) {
            def derived_subs = Subscription.findByInstanceOfAndStatusNot(delSubscription, deletedStatus)

            if (!derived_subs) {

                if (!CostItem.findAllBySub(delSubscription)) {
                    // sync shares
                    delSubscription.instanceOf.syncAllShares([delSubscription])

                    if (delSubscription.getConsortia() && delSubscription.getConsortia() != delInstitution) {
                        OrgRole.executeUpdate("delete from OrgRole where sub = ? and org = ?", [delSubscription, delInstitution])
                    }
                    delSubscription.status = deletedStatus
                    delSubscription.save(flush: true)
                } else {
                    flash.error = message(code: 'subscription.delete.existingCostItems')
                }

            } else {
                flash.error = message(code: 'myinst.actionCurrentSubscriptions.error', default: 'Unable to delete - The selected license has attached subscriptions')
            }
        } else {
            log.warn("${result.user} attempted to delete subscription ${delSubscription} without perms")
            flash.message = message(code: 'subscription.delete.norights')
        }

        redirect action: 'members', params: [id: params.id], model: result
        */
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def pendingChanges() {
        log.debug("subscription id:${params.id}");

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                result.subscriptionInstance,
                RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
        )

        validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }

        result.pendingChanges = [:]

        validSubChilds.each { member ->

            if (executorWrapperService.hasRunningProcess(member)) {
                log.debug("PendingChange processing in progress")
                result.processingpc = true
            } else {
                def pending_change_pending_status = RefdataValue.getByValueAndCategory('Pending', 'PendingChangeStatus')
                def pendingChanges = PendingChange.executeQuery("select pc.id from PendingChange as pc where subscription.id=? and ( pc.status is null or pc.status = ? ) order by pc.ts desc", [member.id, pending_change_pending_status])

                result.pendingChanges << ["${member.id}": pendingChanges.collect { PendingChange.get(it) }]
            }
        }

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def expected() {
        previousAndExpected(params, 'expected');
    }

    private def previousAndExpected(params, screen) {
        log.debug("previousAndExpected ${params}");

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (!result.subscriptionInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'package.label', default: 'Subscription'), params.id])
            redirect action: 'list'
            return
        }

        result.max = params.max ? Integer.parseInt(params.max) : request.user.getDefaultPageSizeTMP()
        params.max = result.max
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        def limits = (!params.format || params.format.equals("html")) ? [max: result.max, offset: result.offset] : [offset: 0]

        def qry_params = [result.subscriptionInstance]
        def date_filter = new Date();

        def base_qry = "from IssueEntitlement as ie where ie.subscription = ? "
        base_qry += "and ie.status.value != 'Deleted' "
        if (date_filter != null) {
            if (screen.equals('previous')) {
                base_qry += " and ( ie.accessEndDate <= ? ) "
            } else {
                base_qry += " and (ie.accessStartDate > ? )"
            }
            qry_params.add(date_filter);
        }

        log.debug("Base qry: ${base_qry}, params: ${qry_params}, result:${result}");
        result.titlesList = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params, limits);
        result.num_ie_rows = IssueEntitlement.executeQuery("select ie.id " + base_qry, qry_params).size()

        result.lastie = result.offset + result.max > result.num_ie_rows ? result.num_ie_rows : result.offset + result.max;

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processAddEntitlements() {
        log.debug("addEntitlements....");

        params.id = params.siid // TODO refactoring frontend siid -> id
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        //result.user = User.get(springSecurityService.principal.id)
        //result.subscriptionInstance = Subscription.get(params.siid)
        //result.institution = result.subscriptionInstance?.subscriber
        //userAccessCheck(result.subscriptionInstance, result.user, 'edit')

        if (result.subscriptionInstance) {
            params.each { p ->
                if (p.key.startsWith('_bulkflag.')) {
                    def tipp_id = p.key.substring(10);
                    // def ie = IssueEntitlement.get(ie_to_edit)
                    def tipp = TitleInstancePackagePlatform.get(tipp_id)

                    if (tipp == null) {
                        log.error("Unable to tipp ${tipp_id}");
                        flash.error("Unable to tipp ${tipp_id}");
                    } else {
                        def ie_current = RefdataValue.getByValueAndCategory('Current', 'Entitlement Issue Status')

                        def new_ie = new IssueEntitlement(status: ie_current,
                                subscription: result.subscriptionInstance,
                                tipp: tipp,
                                accessStartDate: tipp.accessStartDate,
                                accessEndDate: tipp.accessEndDate,
                                startDate: tipp.startDate,
                                startVolume: tipp.startVolume,
                                startIssue: tipp.startIssue,
                                endDate: tipp.endDate,
                                endVolume: tipp.endVolume,
                                endIssue: tipp.endIssue,
                                embargo: tipp.embargo,
                                coverageDepth: tipp.coverageDepth,
                                coverageNote: tipp.coverageNote,
                                ieReason: 'Manually Added by User')
                        if (new_ie.save(flush: true)) {
                            log.debug("Added tipp ${tipp_id} to sub ${params.siid}");
                        } else {
                            new_ie.errors.each { e ->
                                log.error(e);
                            }
                            flash.error = new_ie.errors
                        }
                    }
                }
            }
        } else {
            log.error("Unable to locate subscription instance");
        }

        redirect action: 'index', id: result.subscriptionInstance?.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def removeEntitlement() {
        log.debug("removeEntitlement....");
        def ie = IssueEntitlement.get(params.ieid)
        def deleted_ie = RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')
        ie.status = deleted_ie;

        redirect action: 'index', id: params.sub
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def notes() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()
        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
        }
        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink
        result
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def documents() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()
        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
        }

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map result = [user:springSecurityService.getCurrentUser(),institution:contextService.org]
        result.ownobj = Subscription.get(params.instanceId)
        result.owntp = 'subscription'
        if(params.id) {
            result.docctx = DocContext.get(params.id)
            result.doc = result.docctx.owner
        }

        render template: "/templates/documents/modal", model: result
    }

    @DebugAnnotation(perm="ORG_INST", affil="INST_USER")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST", "INST_USER") })
    def tasks() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()

        if (params.deleteId) {
            def dTask = Task.get(params.deleteId)
            if (dTask && dTask.creator.id == result.user.id) {
                try {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label', default: 'Task'), dTask.title])
                    dTask.delete(flush: true)
                }
                catch (Exception e) {
                    flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'task.label', default: 'Task'), params.deleteId])
                }
            }
        }

        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
        }
        result.taskInstanceList = taskService.getTasksByResponsiblesAndObject(result.user, contextService.getOrg(), result.subscriptionInstance, params)

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        log.debug(result.taskInstanceList)
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewals() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteDocuments() {
        def ctxlist = []

        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'subscription', action: params.redirectAction, id: params.instanceId
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def permissionInfo() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()
        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def launchRenewalsProcess() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)
        result.institution = result.subscriptionInstance.subscriber

        def shopping_basket = UserFolder.findByUserAndShortcode(result.user, 'RenewalsBasket') ?: new UserFolder(user: result.user, shortcode: 'RenewalsBasket').save(flush: true);

        log.debug("Clear basket....");
        shopping_basket.items?.clear();
        shopping_basket.save(flush: true)

        def oid = "com.k_int.kbplus.Subscription:${params.id}"
        shopping_basket.addIfNotPresent(oid)
        Subscription.get(params.id).packages.each {
            oid = "com.k_int.kbplus.Package:${it.pkg.id}"
            shopping_basket.addIfNotPresent(oid)
        }

        redirect controller: 'myInstitution', action: 'renewalsSearch'
    }

    /*
    @Deprecated
    private def userAccessCheck(sub, user, role_str) {
        if ((sub == null || user == null) || (!sub.hasPerm(role_str, user))) {
            response.sendError(401);
            return
        }
    }
    */

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def acceptChange() {
        processAcceptChange(params, Subscription.get(params.id), genericOIDService)
        redirect controller: 'subscription', action: 'index', id: params.id
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def rejectChange() {
        processRejectChange(params, Subscription.get(params.id))
        redirect controller: 'subscription', action: 'index', id: params.id
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def possibleLicensesForSubscription() {
        def result = []

        def subscription = genericOIDService.resolveOID(params.oid)
        def subscriber = subscription.getSubscriber();
        def consortia = subscription.getConsortia();

        result.add([value: '', text: 'None']);

        if (subscriber || consortia) {

            def licensee_role = RDStore.OR_LICENSEE
            def licensee_cons_role = RDStore.OR_LICENSING_CONSORTIUM

            def template_license_type = RefdataValue.getByValueAndCategory('Template', 'License Type')

            def qry_params = [(subscription.instanceOf ? consortia : subscriber), licensee_role, licensee_cons_role]

            def qry = ""

            if (subscription.instanceOf) {
                qry = """
select l from License as l 
where exists ( select ol from OrgRole as ol where ol.lic = l AND ol.org = ? and ( ol.roleType = ? or ol.roleType = ?) ) 
AND l.status.value != 'Deleted' AND (l.instanceOf is not null) order by LOWER(l.reference)
"""
            } else {
                qry = """
select l from License as l 
where exists ( select ol from OrgRole as ol where ol.lic = l AND ol.org = ? and ( ol.roleType = ? or ol.roleType = ?) ) 
AND l.status.value != 'Deleted' order by LOWER(l.reference)
"""
            }
            if (subscriber == consortia) {
                qry_params = [consortia, licensee_cons_role]
                qry = """
select l from License as l 
where exists ( select ol from OrgRole as ol where ol.lic = l AND ol.org = ? and ( ol.roleType = ?) ) 
AND l.status.value != 'Deleted' AND (l.instanceOf is null) order by LOWER(l.reference)
"""
            }

            def license_list = License.executeQuery(qry, qry_params);
            license_list.each { l ->
                result.add([value: "${l.class.name}:${l.id}", text: l.reference ?: "No reference - license ${l.id}"]);
            }
        }
        render result as JSON
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def linkPackage() {
        log.debug("Link package, params: ${params} ");

        //Wenn die Subsc schon ein Anbieter hat. Soll nur Pakete von diesem Anbieter angezeigt werden als erstes
        /*if(params.size() == 3) {
            def subInstance = Subscription.get(params.id)
                    subInstance.orgRelations?.each { or ->
           if (or.roleType.value == "Provider") {
                params.put("cpname", or.org.name)
                }
            }
        }*/

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            response.sendError(401); return
        }

        if ((com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in result.institution?.getallOrgTypeIds())) {

        }

        params.gokbApi = false

        if (ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)) {
            params.gokbApi = true
        }
        params.sort = "name"

        //to be deployed in prallel thread - let's make a test!
        if (params.addType && (params.addType != '')) {
            if (params.gokbApi) {
                def gri = params.impId ? GlobalRecordInfo.findByUuid(params.impId) : null

                if (!gri) {
                    gri = GlobalRecordInfo.findByIdentifier(params.addId)
                }

                if (!gri) {
                    OaiClientLaser oaiClient = new OaiClientLaser()
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    GlobalRecordSource grs = GlobalRecordSource.findByUri(params.source+'/gokb/oai/packages')
                    def rec = oaiClient.getRecord(params.source+'/gokb/oai/','packages',params.impId) //alright, we rely on fixToken to remain as is!!!
                    def parsedRec = globalSourceSyncService.packageConv(rec.metadata,grs)
                    def kbplusCompliant = globalSourceSyncService.testPackageCompliance(parsedRec.parsed_rec)
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()
                    ObjectOutputStream out = new ObjectOutputStream(baos)
                    out.writeObject(parsedRec.parsedRec)
                    out.close()
                    gri = new GlobalRecordInfo(
                            ts: sdf.parse(rec.header.datestamp.text()),
                            name: parsedRec.title,
                            identifier: rec.header.identifier.text(),
                            uuid: rec.header.uuid?.text() ?: null,
                            desc: "${parsedRec.title}",
                            source: grs,
                            rectype: grs.rectype,
                            record: baos.toByteArray(),
                            kbplusCompliant: kbplusCompliant,
                            globalRecordInfoStatus: RefdataValue.getByValueAndCategory(parsedRec.parsed_rec.status,"Package Status")
                    )
                    flash.message = message(code:'subscription.details.link.no_package_yet')
                    gri.save(flush: true)
                }
                def grt = GlobalRecordTracker.findByOwner(gri)
                if (!grt) {
                    def new_tracker_id = UUID.randomUUID().toString()

                    grt = new GlobalRecordTracker(
                            owner: gri,
                            identifier: new_tracker_id,
                            name: gri.name,
                            autoAcceptTippAddition: params.autoAcceptTippAddition == 'on' ? true : false,
                            autoAcceptTippDelete: params.autoAcceptTippDelete == 'on' ? true : false,
                            autoAcceptTippUpdate: params.autoAcceptTippUpdate == 'on' ? true : false,
                            autoAcceptPackageUpdate: params.autoAcceptPackageChange == 'on' ? true : false)
                    if (!grt.save()) {
                        log.error(grt.errors)
                    }
                }

                //if(Package.findByGokbId(grt.owner.uuid)) {
                String addType = params.addType
                    executorWrapperService.processClosure({
                        globalSourceSyncService.initialiseTracker(grt)
                        //Update INDEX ES
                        dataloadService.updateFTIndexes()

                        def pkg_to_link = Package.findByGokbId(grt.owner.uuid)
                        def sub_instances = Subscription.executeQuery("select s from Subscription as s where s.instanceOf = ? ", [result.subscriptionInstance])
                        println "Add package ${addType} to subscription ${result.subscriptionInstance}"

                        if (addType == 'With') {
                            pkg_to_link.addToSubscription(result.subscriptionInstance, true)

                            sub_instances.each {
                                pkg_to_link.addToSubscription(it, true)
                            }
                        } else if (addType == 'Without') {
                            pkg_to_link.addToSubscription(result.subscriptionInstance, false)

                            sub_instances.each {
                                pkg_to_link.addToSubscription(it, false)
                            }
                        }
                    },gri)
                /*}
                else {
                    //setup new
                    globalSourceSyncService.initialiseTracker(grt)
                    //Update INDEX ES
                    dataloadService.updateFTIndexes()
                }*/
                switch(params.addType) {
                    case "With": flash.message = message(code:'subscription.details.link.processingWithEntitlements')
                        redirect action: 'index', id: params.id
                        break
                    case "Without": flash.message = message(code:'subscription.details.link.processingWithoutEntitlements')
                        redirect action: 'addEntitlements', id: params.id
                        break
                }

            } else {
                def pkg_to_link = Package.get(params.addId)
                def sub_instances = Subscription.executeQuery("select s from Subscription as s where s.instanceOf = ? ", [result.subscriptionInstance])
                log.debug("Add package ${params.addType} to subscription ${params}");

                if (params.addType == 'With') {
                    pkg_to_link.addToSubscription(result.subscriptionInstance, true)

                    sub_instances.each {
                        pkg_to_link.addToSubscription(it, true)
                    }

                    redirect action: 'index', id: params.id
                } else if (params.addType == 'Without') {
                    pkg_to_link.addToSubscription(result.subscriptionInstance, false)

                    sub_instances.each {
                        pkg_to_link.addToSubscription(it, false)
                    }

                    redirect action: 'addEntitlements', id: params.id
                }
            }
        }

        if (result.subscriptionInstance.packages) {
            result.pkgs = []
            if (params.gokbApi) {
                result.subscriptionInstance.packages.each { sp ->
                    log.debug("Existing package ${sp.pkg.name} (Adding ImpID: ${sp.pkg.gokbId})")
                    result.pkgs.add(sp.pkg.gokbId)
                }
            } else {
                result.subscriptionInstance.packages.each { sp ->
                    log.debug("Existing package ${sp.pkg.name} (Adding ID: ${sp.pkg.id})")
                    result.pkgs.add(sp.pkg.id)
                }
            }
        } else {
            log.debug("Subscription has no linked packages yet")
        }

        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
            result.institutional_usage_identifier =
                    OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)
        }
        log.debug("Going for GOKB API")
        User user = springSecurityService.getCurrentUser()
        params.max = user?.getDefaultPageSizeTMP() ?: 25

        if (params.gokbApi) {
            def gokbRecords = []

            ApiSource.findAllByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true).each { api ->
                gokbRecords << GOKbService.getPackagesMap(api, params.q, false).records
            }

            params.sort = params.sort ?: 'name'
            params.order = params.order ?: 'asc'

            result.records = gokbRecords ? gokbRecords.flatten().sort() : null

            result.records?.sort { x, y ->
                if (params.order == 'desc') {
                    y."${params.sort}".toString().compareToIgnoreCase x."${params.sort}".toString()
                } else {
                    x."${params.sort}".toString().compareToIgnoreCase y."${params.sort}".toString()
                }
            }
            result.resultsTotal = result.records?.size()

            Integer start = params.offset ? params.int('offset') : 0
            Integer end = params.offset ? params.int('max') + params.int('offset') : params.int('max')
            end = (end > result.records?.size()) ? result.records?.size() : end

            result.hits = result.records?.subList(start, end)

        } else {
            params.rectype = "Package"
            result.putAll(ESSearchService.search(params))
        }

        result
    }

    def buildRenewalsQuery(params) {
        log.debug("BuildQuery...");

        StringWriter sw = new StringWriter()

        // sw.write("subtype:'Subscription Offered'")
        sw.write("rectype:'Package'")

        renewals_reversemap.each { mapping ->

            // log.debug("testing ${mapping.key}");

            if (params[mapping.key] != null) {
                if (params[mapping.key].class == java.util.ArrayList) {
                    params[mapping.key].each { p ->
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${p}\"")
                    }
                } else {
                    // Only add the param if it's length is > 0 or we end up with really ugly URLs
                    // II : Changed to only do this if the value is NOT an *
                    if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${params[mapping.key]}\"")
                    }
                }
            }
        }


        def result = sw.toString();
        result;
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def history() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        result.contextOrg = contextService.getOrg()
        result.max = params.max ?: result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ?: 0;

        def qry_params = [result.subscription.class.name, "${result.subscription.id}"]
        result.historyLines = AuditLogEvent.executeQuery("select e from AuditLogEvent as e where className=? and persistedObjectId=? order by id desc", qry_params, [max: result.max, offset: result.offset]);
        result.historyLinesTotal = AuditLogEvent.executeQuery("select e.id from AuditLogEvent as e where className=? and persistedObjectId=?", qry_params).size()

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def changes() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        result.contextOrg = contextService.getOrg()
        result.max = params.max ?: result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ?: 0;

        def baseQuery = "select pc from PendingChange as pc where pc.subscription = :sub and pc.subscription.status.value != 'Deleted' and pc.status.value in (:stats)"
        def baseParams = [sub: result.subscription, stats: ['Accepted', 'Rejected']]

        result.todoHistoryLines = PendingChange.executeQuery(
                baseQuery + " order by pc.ts desc",
                baseParams,
                [max: result.max, offset: result.offset]
        )

        result.todoHistoryLinesTotal = PendingChange.executeQuery(
                baseQuery,
                baseParams
        )[0]

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        result
    }

    // TODO Cost per use tab, still needed?
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def costPerUse() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
            result.institutional_usage_identifier =
                    OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)
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
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def show() {

        DebugUtil du = new DebugUtil()
        du.setBenchMark('1')

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        du.setBenchMark('this-n-that')

        // unlink license

        if (params.cmd?.equalsIgnoreCase('unlinkLicense')) {
            if (result.subscriptionInstance.owner) {
                result.subscriptionInstance.setOwner(null)
                params.remove('cmd')
                redirect(url: request.getHeader('referer'))
                return
            }
        }

        //if (!result.institution) {
        //    result.institution = result.subscriptionInstance.subscriber ?: result.subscriptionInstance.consortia
        //}
        if (result.institution) {
            result.subscriber_shortcode = result.institution.shortcode
            result.institutional_usage_identifier =
                    OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)
        }

        du.setBenchMark('links')

        LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(Subscription.class.name, result.subscription.id)
        result.navPrevSubscription = links.prevLink
        result.navNextSubscription = links.nextLink

        // links
        def key = result.subscription.id
        def sources = Links.executeQuery('select l from Links as l where l.source = :source and l.objectType = :objectType', [source: key, objectType: Subscription.class.name])
        def destinations = Links.executeQuery('select l from Links as l where l.destination = :destination and l.objectType = :objectType', [destination: key, objectType: Subscription.class.name])
        //IN is from the point of view of the context subscription (= params.id)
        result.links = [:]

        sources.each { link ->
            Subscription destination = Subscription.get(link.destination)
            if (destination.isVisibleBy(result.user) && destination.status != RDStore.SUBSCRIPTION_DELETED) {
                def index = link.linkType.getI10n("value")?.split("\\|")[0]
                if (result.links[index] == null) {
                    result.links[index] = [link]
                } else result.links[index].add(link)
            }
        }
        destinations.each { link ->
            Subscription source = Subscription.get(link.source)
            if (source.isVisibleBy(result.user) && source.status != RDStore.SUBSCRIPTION_DELETED) {
                def index = link.linkType.getI10n("value")?.split("\\|")[1]
                if (result.links[index] == null) {
                    result.links[index] = [link]
                } else result.links[index].add(link)
            }
        }


        du.setBenchMark('pending changes')

        // ---- pendingChanges : start

        if (executorWrapperService.hasRunningProcess(result.subscriptionInstance)) {
            log.debug("PendingChange processing in progress")
            result.processingpc = true
        } else {

            def pending_change_pending_status = RefdataValue.getByValueAndCategory('Pending', 'PendingChangeStatus')
            def pendingChanges = PendingChange.executeQuery("select pc.id from PendingChange as pc where subscription=? and ( pc.status is null or pc.status = ? ) order by pc.ts desc", [result.subscription, pending_change_pending_status])

            log.debug("pc result is ${result.pendingChanges}")

            if (result.subscription.isSlaved?.value == "Yes" && pendingChanges) {
                log.debug("Slaved subscription, auto-accept pending changes")
                def changesDesc = []
                pendingChanges.each { change ->
                    if (!pendingChangeService.performAccept(change, result.user)) {
                        log.debug("Auto-accepting pending change has failed.")
                    } else {
                        changesDesc.add(PendingChange.get(change).desc)
                    }
                }
                flash.message = changesDesc
            } else {
                result.pendingChanges = pendingChanges.collect { PendingChange.get(it) }
            }
        }

        // ---- pendingChanges : end

        du.setBenchMark('tasks')

        // TODO: experimental asynchronous task
        //def task_tasks = task {

            // tasks

            def contextOrg = contextService.getOrg()
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, contextOrg, result.subscriptionInstance)
            def preCon = taskService.getPreconditions(contextOrg)
            result << preCon

            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = []
            result.subscriptionInstance.orgRelations?.each { or ->
                if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                    result.visibleOrgRelations << or
                }
            }
            result.visibleOrgRelations.sort { it.org.sortname }
        //}

        du.setBenchMark('properties')

        // TODO: experimental asynchronous task
        //def task_properties = task {

            // -- private properties

            result.authorizedOrgs = result.user?.authorizedOrgs
            result.contextOrg = contextService.getOrg()

            // create mandatory OrgPrivateProperties if not existing

            def mandatories = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Subscription Property", true, result.contextOrg)

            mandatories.each { pd ->
                if (!SubscriptionPrivateProperty.findWhere(owner: result.subscriptionInstance, type: pd)) {
                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.subscriptionInstance, pd)

                    if (newProp.hasErrors()) {
                        log.error(newProp.errors)
                    } else {
                        log.debug("New subscription private property created via mandatory: " + newProp.type.name)
                    }
                }
            }

            // -- private properties

            result.modalPrsLinkRole = RefdataValue.findByValue('Specific subscription editor')
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(contextService.getOrg())

            result.visiblePrsLinks = []

            result.subscriptionInstance.prsLinks.each { pl ->
                if (!result.visiblePrsLinks.contains(pl.prs)) {
                    if (pl.prs.isPublic?.value != 'No') {
                        result.visiblePrsLinks << pl
                    } else {
                        // nasty lazy loading fix
                        result.user.authorizedOrgs.each { ao ->
                            if (ao.getId() == pl.prs.tenant.getId()) {
                                result.visiblePrsLinks << pl
                            }
                        }
                    }
                }
            }
        //}

        du.setBenchMark('usage')

        // TODO: experimental asynchronous task
        //def task_usage = task {

            // usage
            def suppliers = result.subscriptionInstance.issueEntitlements?.tipp.pkg.contentProvider?.id.unique()

            if (suppliers) {
                if (suppliers.size() > 1) {
                    log.debug('Found different content providers, cannot show usage')
                } else {
                    def supplier_id = suppliers[0]
                    result.natStatSupplierId = Org.get(supplier_id).getIdentifierByType('statssid')?.value
                    result.institutional_usage_identifier =
                            OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)
                    if (result.institutional_usage_identifier) {

                        def fsresult = factService.generateUsageData(result.institution.id, supplier_id, result.subscriptionInstance)
                        def fsLicenseResult = factService.generateUsageDataForSubscriptionPeriod(result.institution.id, supplier_id, result.subscriptionInstance)

                        def holdingTypes = result.subscriptionInstance.getHoldingTypes() ?: null
                        if (!holdingTypes) {
                            log.debug('No types found, maybe there are no issue entitlements linked to subscription')
                        } else if (holdingTypes.size() > 1) {
                            log.info('Different content type for this license, cannot calculate Cost Per Use.')
                        } else if (!fsLicenseResult.isEmpty() && result.subscriptionInstance.startDate) {
                            def existingReportMetrics = fsLicenseResult.y_axis_labels*.split(':')*.last()
                            def costPerUseMetricValuePair = factService.getTotalCostPerUse(result.subscriptionInstance, holdingTypes.first(), existingReportMetrics)
                            if (costPerUseMetricValuePair) {
                                result.costPerUseMetric = costPerUseMetricValuePair[0]
                                result.totalCostPerUse = costPerUseMetricValuePair[1]
                                result.currencyCode = NumberFormat.getCurrencyInstance().getCurrency().currencyCode
                            }
                        }

                        result.statsWibid = result.institution.getIdentifierByType('wibid')?.value
                        result.usageMode = ((com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in result.institution?.getallOrgTypeIds())) ? 'package' : 'institution'
                        result.usage = fsresult?.usage
                        result.x_axis_labels = fsresult?.x_axis_labels
                        result.y_axis_labels = fsresult?.y_axis_labels

                        result.lusage = fsLicenseResult?.usage
                        result.l_x_axis_labels = fsLicenseResult?.x_axis_labels
                        result.l_y_axis_labels = fsLicenseResult?.y_axis_labels
                    }
                }
            }
        //}

        du.setBenchMark('costs')

        //determine org role
        if (result.subscription.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_CONSORTIAL))
            params.view = "cons"
        else if (result.subscription.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_PARTICIPATION) && result.subscription.getConsortia().equals(result.institution))
            params.view = "consAtSubscr"
        else if (result.subscription.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_PARTICIPATION) && !result.subscription.getConsortia().equals(result.institution))
            params.view = "subscr"
        //cost items
        //params.forExport = true
        LinkedHashMap costItems = financeService.getCostItemsForSubscription(result.subscription, params, 10, 0)
        result.costItemSums = [:]
        if (costItems.own.count > 0) {
            result.costItemSums.ownCosts = costItems.own.sums
        }
        if (costItems.cons.count > 0) {
            result.costItemSums.consCosts = costItems.cons.sums
        }
        if (costItems.subscr.count > 0) {
            result.costItemSums.subscrCosts = costItems.subscr.sums
        }

        du.setBenchMark('provider & agency filter')

        // TODO: experimental asynchronous task
        //def task_providerFilter = task {

            result.availableProviderList = orgTypeService.getOrgsForTypeProvider().minus(
                    OrgRole.executeQuery(
                            "select o from OrgRole oo join oo.org o where oo.sub.id = :sub and oo.roleType.value = 'Provider'",
                            [sub: result.subscriptionInstance.id]
                    ))
            result.existingProviderIdList = []
            // performance problems: orgTypeService.getCurrentProviders(contextService.getOrg()).collect { it -> it.id }

            result.availableAgencyList = orgTypeService.getOrgsForTypeAgency().minus(
                    OrgRole.executeQuery(
                            "select o from OrgRole oo join oo.org o where oo.sub.id = :sub and oo.roleType.value = 'Agency'",
                            [sub: result.subscriptionInstance.id]
                    ))
            result.existingAgencyIdList = []
            // performance problems: orgTypeService.getCurrentAgencies(contextService.getOrg()).collect { it -> it.id }

        //}

        List bm = du.stopBenchMark()
        result.benchMark = bm

        // TODO: experimental asynchronous task
        //waitAll(task_tasks, task_properties, task_usage, task_providerFilter)

        result
    }
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewSubscription_Local() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)

        if (!accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code: 'myinst.error.noMember', args: [result.institution.name]);
            response.sendError(401)
            return;
        } else if (!accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code: 'myinst.renewalUpload.error.noAdmin')
            response.sendError(401)
            return;
        }
        def prevSubs = Links.findByLinkTypeAndObjectTypeAndDestination(RDStore.LINKTYPE_FOLLOWS, Subscription.class.name, params.id)
        if (prevSubs){
            flash.error = message(code: 'subscription.renewSubExist')
            response.sendError(401)
            return;
        }

        def sdf = new SimpleDateFormat('dd.MM.yyyy')

        def subscription = Subscription.get(params.id)

        result.errors = []
        def newStartDate
        def newEndDate
        use(TimeCategory) {
            newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
            newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
        }

        result.isRenewSub = true
        result.permissionInfo = [sub_startDate: newStartDate? sdf.format(newStartDate) : null,
                                 sub_endDate: newEndDate? sdf.format(newEndDate) : null,
                                 sub_name: subscription.name,
                                 sub_id: subscription.id,
                                 sub_license: subscription?.owner?.reference?:'',
                                 sub_status: RDStore.SUBSCRIPTION_INTENDED]

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processSimpleRenewal_Local() {
        log.debug("-> renewalsUpload params: ${params}");
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            return;
        }

        def sub_startDate = params.subscription?.start_date ? parseDate(params.subscription?.start_date, possible_date_formats) : null
        def sub_endDate = params.subscription?.end_date ? parseDate(params.subscription?.end_date, possible_date_formats): null
        def sub_status = params.subStatus
        def old_subOID = params.subscription.old_subid
        def new_subname = params.subscription.name

        def new_subscription = new Subscription(
                identifier: java.util.UUID.randomUUID().toString(),
                status: sub_status,
                impId: java.util.UUID.randomUUID().toString(),
                name: new_subname,
                startDate: sub_startDate,
                endDate: sub_endDate,
                type: Subscription.get(old_subOID)?.type ?: null,
                isPublic: RDStore.YN_NO,
                owner: params.subscription.copyLicense ? (Subscription.get(old_subOID)?.owner) : null,
                resource: Subscription.get(old_subOID)?.resource ?: null,
                form: Subscription.get(old_subOID)?.form ?: null
        )
        log.debug("New Sub: ${new_subscription.startDate}  - ${new_subscription.endDate}")

        if (new_subscription.save()) {
            // assert an org-role
            def org_link = new OrgRole(org: result.institution,
                    sub: new_subscription,
                    roleType: RDStore.OR_SUBSCRIBER
            ).save();

            if(old_subOID) {
                Links prevLink = new Links(source: new_subscription.id, destination: old_subOID, objectType: Subscription.class.name, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org)
                prevLink.save()
            } else { log.error("Problem linking new subscription, ${prevLink.errors}") }
        } else {
            log.error("Problem saving new subscription, ${new_subscription.errors}");
        }

        new_subscription.save(flush: true);

        if (params?.targetSubscriptionId == "null") params.remove("targetSubscriptionId")
        redirect controller: 'subscription',
                 action: 'copyElementsIntoSubscription',
                 id: old_subOID,
                 params: [sourceSubscriptionId: old_subOID, targetSubscriptionId: new_subscription.id, isRenewSub: true]
    }



    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewSubscriptionConsortia() {

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!(result || (com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in contextService.getOrg()?.getallOrgTypeIds()))) {
            response.sendError(401); return
        }

        if ((com.k_int.kbplus.RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in result.institution?.getallOrgTypeIds())) {
            def baseSub = Subscription.get(params.baseSubscription ?: params.id)

            use(TimeCategory) {
                result.newStartDate = baseSub.startDate ? baseSub.startDate + 1.year : null
                result.newEndDate = baseSub.endDate ? baseSub.endDate + 1.year : null
            }

            if (params.workFlowPart == '3') {

                def newSubConsortia = Subscription.get(params.newSubscription)

                def subMembers = []

                params.list('selectedSubs').each { it ->
                    subMembers << Long.valueOf(it)
                }


                subMembers.each { sub ->

                    def subMember = Subscription.findById(sub)

                    //ChildSub Exist
                    ArrayList<Links> prevLinks = Links.findAllByDestinationAndLinkTypeAndObjectType(subMember.id, RDStore.LINKTYPE_FOLLOWS, Subscription.class.name)
                    if (prevLinks.size() == 0) {

                        /* Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
                            [result.subscriptionInstance, it.id])*/

                        def newSubscription = new Subscription(
                                type: subMember.type,
                                status: newSubConsortia.status,
                                name: subMember.name,
                                startDate: newSubConsortia.startDate,
                                endDate: newSubConsortia.endDate,
                                manualRenewalDate: subMember.manualRenewalDate,
                                /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                                identifier: java.util.UUID.randomUUID().toString(),
                                instanceOf: newSubConsortia?.id,
                                //previousSubscription: subMember?.id,
                                isSlaved: subMember.isSlaved,
                                isPublic: subMember.isPublic,
                                impId: java.util.UUID.randomUUID().toString(),
                                owner: newSubConsortia.owner?.id ? subMember.owner?.id : null,
                                resource: newSubConsortia.resource ?: null,
                                form: newSubConsortia.form ?: null
                        )
                        newSubscription.save(flush: true)
                        //ERMS-892: insert preceding relation in new data model
                        if (subMember) {
                            Links prevLink = new Links(source: newSubscription.id, destination: subMember.id, linkType: RDStore.LINKTYPE_FOLLOWS, objectType: Subscription.class.name, owner: contextService.org)
                            if (!prevLink.save()) {
                                log.error("Subscription linking failed, please check: ${prevLink.errors}")
                            }
                        }

                        if (subMember.customProperties) {
                            //customProperties
                            for (prop in subMember.customProperties) {
                                def copiedProp = new SubscriptionCustomProperty(type: prop.type, owner: newSubscription)
                                copiedProp = prop.copyInto(copiedProp)
                                copiedProp.instanceOf = null
                                copiedProp.save(flush: true)
                                //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                            }
                        }
                        if (subMember.privateProperties) {
                            //privatProperties

                            List tenantOrgs = OrgRole.executeQuery('select o.org from OrgRole as o where o.sub = :sub and o.roleType in (:roleType)', [sub: subMember, roleType: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]]).collect {
                                it -> it.id
                            }
                            subMember.privateProperties?.each { prop ->
                                if (tenantOrgs.indexOf(prop.type?.tenant?.id) > -1) {
                                    def copiedProp = new SubscriptionPrivateProperty(type: prop.type, owner: newSubscription)
                                    copiedProp = prop.copyInto(copiedProp)
                                    copiedProp.save(flush: true)
                                    //newSubscription.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
                                }
                            }
                        }

                        if (subMember.packages && newSubConsortia.packages) {
                            //Package
                            subMember.packages?.each { pkg ->
                                SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                                InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                                newSubscriptionPackage.subscription = newSubscription
                                newSubscriptionPackage.save(flush: true)
                            }
                        }
                        if (subMember.issueEntitlements && newSubConsortia.issueEntitlements) {

                            subMember.issueEntitlements?.each { ie ->

                                if (ie.status != RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')) {
                                    def ieProperties = ie.properties
                                    ieProperties.globalUID = null

                                    IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                    InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                                    newIssueEntitlement.subscription = newSubscription
                                    newIssueEntitlement.save(flush: true)
                                }
                            }

                        }

                        //OrgRole
                        subMember.orgRelations?.each { or ->
                            if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType.value in ['Subscriber', 'Subscriber_Consortial']) || (newSubConsortia.orgRelations.size() >= 1)) {
                                OrgRole newOrgRole = new OrgRole()
                                InvokerHelper.setProperties(newOrgRole, or.properties)
                                newOrgRole.sub = newSubscription
                                newOrgRole.save(flush: true)
                            }
                        }



                        if (subMember.prsLinks && newSubConsortia.prsLinks) {
                            //PersonRole
                            subMember.prsLinks?.each { prsLink ->
                                PersonRole newPersonRole = new PersonRole()
                                InvokerHelper.setProperties(newPersonRole, prsLink.properties)
                                newPersonRole.sub = newSubscription
                                newPersonRole.save(flush: true)
                            }
                        }
                    }

                }


                redirect controller: 'subscription', action: 'show', params: [id: newSubConsortia?.id]

            }

            if (params.workFlowPart == '2') {
                def newSub2 = Subscription.get(params.newSubscription)

                //Copy Docs
                def toCopyDocs = []
                params.list('subscription.takeDocs').each { doc ->

                    toCopyDocs << Long.valueOf(doc)
                }

                //Copy Announcements
                def toCopyAnnouncements = []
                params.list('subscription.takeAnnouncements').each { announcement ->

                    toCopyAnnouncements << Long.valueOf(announcement)
                }
                if (newSub2.documents.size() == 0) {
                    baseSub.documents?.each { dctx ->

                        //Copy Docs
                        if (dctx.id in toCopyDocs) {
                            if (((dctx.owner?.contentType == 1) || (dctx.owner?.contentType == 3)) && (dctx.status?.value != 'Deleted')) {

                                Doc newDoc = new Doc()
                                InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                                newDoc.save(flush: true)

                                DocContext newDocContext = new DocContext()
                                InvokerHelper.setProperties(newDocContext, dctx.properties)
                                newDocContext.subscription = newSub2
                                newDocContext.owner = newDoc
                                newDocContext.save(flush: true)
                            }
                        }
                        //Copy Announcements
                        if (dctx.id in toCopyAnnouncements) {
                            if ((dctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                                Doc newDoc = new Doc()
                                InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                                newDoc.save(flush: true)

                                DocContext newDocContext = new DocContext()
                                InvokerHelper.setProperties(newDocContext, dctx.properties)
                                newDocContext.subscription = newSub2
                                newDocContext.owner = newDoc
                                newDocContext.save(flush: true)
                            }
                        }
                    }
                }
                if (!Task.findAllBySubscription(newSub2)) {
                    //Copy Tasks
                    params.list('subscription.takeTasks').each { tsk ->

                        def task = Task.findBySubscriptionAndId(baseSub, Long.valueOf(tsk))
                        if (task) {
                            if (task.status != RefdataValue.loc('Task Status', [en: 'Done', de: 'Erledigt'])) {
                                Task newTask = new Task()
                                InvokerHelper.setProperties(newTask, task.properties)
                                newTask.subscription = newSub2
                                newTask.save(flush: true)
                            }

                        }
                    }
                }
                params.workFlowPart = 3
                params.workFlowPartNext = 4
                result.newSub = newSub2

                def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                        baseSub,
                        RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
                )

                result.validSubChilds = validSubChilds.sort { a, b ->
                    def sa = a.getSubscriber()
                    def sb = b.getSubscriber()
                    (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
                }

            }


            if (params.workFlowPart == '1') {

                if (params.baseSubscription) {

                    ArrayList<Links> previousSubscriptions = Links.findAllByDestinationAndObjectTypeAndLinkType(baseSub.id, Subscription.class.name, RDStore.LINKTYPE_FOLLOWS)
                    if (previousSubscriptions.size() > 0) {
                        flash.error = message(code: 'subscription.renewSubExist', default: 'The Subscription is already renewed!')
                    } else {


                        def newSub = new Subscription(
                                name: baseSub.name,
                                startDate: result.newStartDate,
                                endDate: result.newEndDate,
                                //previousSubscription: baseSub.id, overhauled as ERMS-800/ERMS-892
                                identifier: java.util.UUID.randomUUID().toString(),
                                isPublic: baseSub.isPublic,
                                isSlaved: baseSub.isSlaved,
                                type: baseSub.type,
                                status: RefdataValue.loc('Subscription Status', [en: 'Intended', de: 'Geplant']),
                                resource: baseSub.resource ?: null,
                                form: baseSub.form ?: null
                        )

                        if (params.subscription.takeLinks) {
                            //License
                            newSub.owner = baseSub.owner ?: null
                        }

                        if (!newSub.save(flush: true)) {
                            log.error("Problem saving subscription ${newSub.errors}");
                            return newSub
                        } else {
                            log.debug("Save ok");
                            //Copy References
                            //OrgRole
                            baseSub.orgRelations?.each { or ->

                                if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType.value in ['Subscriber', 'Subscriber_Consortial']) || params.subscription.takeLinks) {
                                    OrgRole newOrgRole = new OrgRole()
                                    InvokerHelper.setProperties(newOrgRole, or.properties)
                                    newOrgRole.sub = newSub
                                    newOrgRole.save(flush: true)
                                }
                            }
                            //link to previous subscription
                            Links prevLink = new Links(source: newSub.id, destination: baseSub.id, objectType: Subscription.class.name, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org)
                            if (!prevLink.save(flush: true)) {
                                log.error("Problem linking to previous subscription: ${prevLink.errors}")
                            }
                            if (params.subscription.takeLinks) {
                                //Package
                                baseSub.packages?.each { pkg ->
                                    SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                                    InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                                    newSubscriptionPackage.subscription = newSub
                                    newSubscriptionPackage.save(flush: true)
                                }
                                // fixed hibernate error: java.util.ConcurrentModificationException
                                // change owner before first save
                                //License
                                //newSub.owner = baseSub.owner ?: null
                                //newSub.save(flush: true)
                            }

                            if (params.subscription.takeEntitlements) {

                                baseSub.issueEntitlements.each { ie ->

                                    if (ie.status != RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')) {
                                        def properties = ie.properties
                                        properties.globalUID = null

                                        IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                        InvokerHelper.setProperties(newIssueEntitlement, properties)
                                        newIssueEntitlement.subscription = newSub
                                        newIssueEntitlement.save(flush: true)
                                    }
                                }

                            }


                            if (params.subscription.takeCustomProperties) {
                                //customProperties
                                for (prop in baseSub.customProperties) {
                                    def copiedProp = new SubscriptionCustomProperty(type: prop.type, owner: newSub)
                                    copiedProp = prop.copyInto(copiedProp)
                                    copiedProp.instanceOf = null
                                    copiedProp.save(flush: true)
                                    //newSub.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                                }
                            }
                            if (params.subscription.takePrivateProperties) {
                                //privatProperties
                                def contextOrg = contextService.getOrg()

                                baseSub.privateProperties.each { prop ->
                                    if (prop.type?.tenant?.id == contextOrg?.id) {
                                        def copiedProp = new SubscriptionPrivateProperty(type: prop.type, owner: newSub)
                                        copiedProp = prop.copyInto(copiedProp)
                                        copiedProp.save(flush: true)
                                        //newSub.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
                                    }
                                }
                            }

                            params.workFlowPart = 2
                            params.workFlowPartNext = 3
                            result.newSub = newSub
                        }
                    }
                }
            }

            LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(result.subscriptionInstance.class.name, result.subscriptionInstance.id)
            result.navPrevSubscription = links.prevLink
            result.navNextSubscription = links.nextLink

            // tasks
            def contextOrg = contextService.getOrg()
            result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, contextOrg, result.subscriptionInstance)

            result.contextOrg = contextOrg
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = []
            result.subscriptionInstance.orgRelations?.each { or ->
                if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                    result.visibleOrgRelations << or
                }
            }
            result.visibleOrgRelations.sort { it.org.sortname }
            result.visibleOrgRelations =  subscriptionService.getVisibleOrgRelations(result.subscriptionInstance)

            result.modalPrsLinkRole = RefdataValue.findByValue('Specific subscription editor')
            result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(contextService.getOrg())

            result.visiblePrsLinks = []

            result.subscriptionInstance.prsLinks.each { pl ->
                if (!result.visiblePrsLinks.contains(pl.prs)) {
                    if (pl.prs.isPublic?.value != 'No') {
                        result.visiblePrsLinks << pl
                    } else {
                        // nasty lazy loading fix
                        result.user.authorizedOrgs.each { ao ->
                            if (ao.getId() == pl.prs.tenant.getId()) {
                                result.visiblePrsLinks << pl
                            }
                        }
                    }
                }
            }

            result.workFlowPart = params.workFlowPart ?: 1
            result.workFlowPartNext = params.workFlowPartNext ?: 2

        }
        result

    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processSimpleRenewal_Consortia() {

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!(result || (RDStore.OT_CONSORTIUM?.id in contextService.getOrg()?.getallOrgTypeIds()))) {
            response.sendError(401); return
        }

        if ((RDStore.OT_CONSORTIUM?.id in result.institution?.getallOrgTypeIds())) {
            def baseSub = Subscription.get(params.baseSubscription ?: params.id)

            ArrayList<Links> previousSubscriptions = Links.findAllByDestinationAndObjectTypeAndLinkType(baseSub.id, Subscription.class.name, RDStore.LINKTYPE_FOLLOWS)
            if (previousSubscriptions.size() > 0) {
                flash.error = message(code: 'subscription.renewSubExist', default: 'The Subscription is already renewed!')
            } else {
                def sub_startDate = params.subscription?.start_date ? parseDate(params.subscription?.start_date, possible_date_formats) : null
                def sub_endDate = params.subscription?.end_date ? parseDate(params.subscription?.end_date, possible_date_formats): null
                def sub_status = params.subStatus
                def old_subOID = params.subscription.old_subid
                def new_subname = params.subscription.name

                def newSub = new Subscription(
                        name: new_subname,
                        startDate: sub_startDate,
                        endDate: sub_endDate,
                        identifier: java.util.UUID.randomUUID().toString(),
                        isPublic: baseSub.isPublic,
                        isSlaved: baseSub.isSlaved,
                        type: Subscription.get(old_subOID)?.type ?: null,
                        status: sub_status,
                        resource: baseSub.resource ?: null,
                        form: baseSub.form ?: null
                )

                if (!newSub.save(flush: true)) {
                    log.error("Problem saving subscription ${newSub.errors}");
                    return newSub
                } else {
                    log.debug("Save ok");
                    //link to previous subscription
                    Links prevLink = new Links(source: newSub.id, destination: baseSub.id, objectType: Subscription.class.name, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org)
                    if (!prevLink.save(flush: true)) {
                        log.error("Problem linking to previous subscription: ${prevLink.errors}")
                    }
                    result.newSub = newSub

                    LinkedHashMap<String, List> links = navigationGenerationService.generateNavigation(result.subscriptionInstance.class.name, result.subscriptionInstance.id)

                    if (params?.targetSubscriptionId == "null") params.remove("targetSubscriptionId")
                    result.isRenewSub = true
                    redirect controller: 'subscription',
                             action: 'copyElementsIntoSubscription',
                             id: old_subOID,
                             params: [sourceSubscriptionId: old_subOID, targetSubscriptionId: newSub.id, isRenewSub: true]
                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewSubscription_Consortia() {

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!(result || (RDStore.OT_CONSORTIUM?.id in contextService.getOrg()?.getallOrgTypeIds()))) {
            response.sendError(401); return
        }

        if ((RDStore.OT_CONSORTIUM?.id in result.institution?.getallOrgTypeIds())) {
            def subscription = Subscription.get(params.baseSubscription ?: params.id)

            def sdf = new SimpleDateFormat('dd.MM.yyyy')

            result.errors = []
            def newStartDate
            def newEndDate
            use(TimeCategory) {
                newStartDate = subscription.endDate ? (subscription.endDate + 1.day) : null
                newEndDate = subscription.endDate ? (subscription.endDate + 1.year) : null
            }

            result.isRenewSub = true
            result.permissionInfo = [sub_startDate: newStartDate ? sdf.format(newStartDate) : null,
                                     sub_endDate  : newEndDate ? sdf.format(newEndDate) : null,
                                     sub_name     : subscription.name,
                                     sub_id       : subscription.id,
                                     sub_license  : subscription?.owner?.reference ?: '',
                                     sub_status   : RDStore.SUBSCRIPTION_INTENDED]
        }
        result
    }

    private getMySubscriptions_readRights(){
        def params = [:]
        List result
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        result
    }
    private getMySubscriptions_writeRights(){
        List result
        Map params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        params.subTypes = "${RDStore.SUBSCRIPTION_TYPE_LOCAL.id}"
        tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        result
    }


    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def copyElementsIntoSubscription() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        flash.error = ""
        flash.info = ""
        if (params?.sourceSubscriptionId == "null") params.remove("sourceSubscriptionId")
        result.sourceSubscriptionId = params?.sourceSubscriptionId ?: params?.id
        result.sourceSubscription = Subscription.get(Long.parseLong(params?.sourceSubscriptionId ?: params?.id))

        if (params?.targetSubscriptionId == "null") params.remove("targetSubscriptionId")
        if (params?.targetSubscriptionId) {
            result.targetSubscriptionId = params?.targetSubscriptionId
            result.targetSubscription = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }

        result.isRenewSub = params.isRenewSub
        result.allSubscriptions_readRights = subscriptionService.getMySubscriptions_readRights()
        result.allSubscriptions_writeRights = subscriptionService.getMySubscriptions_writeRights()

        switch (params.workFlowPart) {
            case WORKFLOW_DATES_OWNER_RELATIONS:
                result << copySubElements_DatesOwnerRelations();
                break;
            case WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copySubElements_DocsAnnouncementsTasks();
                break;
            case WORKFLOW_SUBSCRIBER:
                result << copySubElements_Subscriber();
                break;
            case WORKFLOW_PROPERTIES:
                result << copySubElements_Properties();
                if (params?.targetSubscriptionId){
                    redirect controller: 'subscription', action: 'show', params: [id: params?.targetSubscriptionId]
                }
                break;
            case WORKFLOW_PACKAGES_ENTITLEMENTS:
                result << copySubElements_PackagesEntitlements();
                break;
            default:
                result << loadDataFor_DatesOwnerRelations()
                break;
        }

//        TODO: Unuseded Entf?
//        LinkedHashMap<String,List> links = navigationGenerationService.generateNavigation(result.subscriptionInstance.class.name, result.subscriptionInstance.id)
//        result.navPrevSubscription = links.prevLink
//        result.navNextSubscription = links.nextLink
//        result.modalPrsLinkRole = RDStore.PRS_RESP_SPEC_SUB_EDITOR
//        result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(contextService.getOrg())
//        result.visiblePrsLinks = []
//        result.subscriptionInstance.prsLinks.each { pl ->
//            if (!result.visiblePrsLinks.contains(pl.prs)) {
//                if (pl.prs.isPublic?.value != 'No') {
//                    result.visiblePrsLinks << pl
//                } else {
//                    // nasty lazy loading fix
//                    result.user.authorizedOrgs.each { ao ->
//                        if (ao.getId() == pl.prs.tenant.getId()) {
//                            result.visiblePrsLinks << pl
//                        }
//                    }
//                }
//            }
//        }
        if (params?.targetSubscriptionId) {
            result.targetSubscription = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }
        result.workFlowPart = params?.workFlowPart ?: WORKFLOW_DATES_OWNER_RELATIONS
        result.workFlowPartNext = params?.workFlowPartNext ?: WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        result.isRenewSub = params?.isRenewSub
        result
    }

    private copySubElements_DatesOwnerRelations() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        boolean isTargetSubChanged = false
        if (params?.subscription?.deleteDates && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.deleteDates(newSub, flash)
            isTargetSubChanged = true
        }else if (params?.subscription?.takeDates && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.copyDates(baseSub, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteOwner && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.deleteOwner(newSub, flash)
            isTargetSubChanged = true
        }else if (params?.subscription?.takeOwner && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.copyOwner(baseSub, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteOrgRelations && isBothSubscriptionsSet(baseSub, newSub)) {
            List<OrgRole> toDeleteOrgRelations = params.list('subscription.deleteOrgRelations').collect { genericOIDService.resolveOID(it) }
            subscriptionService.deleteOrgRelations(toDeleteOrgRelations, newSub, flash)
            isTargetSubChanged = true
        }
        if (params?.subscription?.takeOrgRelations && isBothSubscriptionsSet(baseSub, newSub)) {
            List<OrgRole> toCopyOrgRelations = params.list('subscription.takeOrgRelations').collect { genericOIDService.resolveOID(it) }
            subscriptionService.copyOrgRelations(toCopyOrgRelations, baseSub, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub.refresh()
        }

        result.sourceIEs = subscriptionService.getIssueEntitlements(baseSub)
        result.targetIEs = subscriptionService.getIssueEntitlements(newSub)

        // restrict visible for templates/links/orgLinksAsList
        result.source_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(baseSub)
        result.target_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(newSub)

        params?.workFlowPart = WORKFLOW_PACKAGES_ENTITLEMENTS
        params?.workFlowPartNext = WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        result.subscription = baseSub
        result.newSub = newSub
        result.targetSubscription = newSub
        result
    }
    private loadDataFor_DatesOwnerRelations(){
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        result.sourceIEs = subscriptionService.getIssueEntitlements(baseSub)
        result.targetIEs = subscriptionService.getIssueEntitlements(newSub)

        // restrict visible for templates/links/orgLinksAsList
        result.source_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(baseSub)
        result.target_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(newSub)
        params?.workFlowPart = WORKFLOW_DATES_OWNER_RELATIONS
        params?.workFlowPartNext = WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        result
    }

    private copySubElements_DocsAnnouncementsTasks() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }

        if (params?.subscription?.deleteDocIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteDocs = []
            params.list('subscription.deleteDocIds').each { doc -> toDeleteDocs << Long.valueOf(doc) }
            subscriptionService.deleteDocs(toDeleteDocs, newSub, flash)
        }

        if (params?.subscription?.takeDocIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyDocs = []
            params.list('subscription.takeDocIds').each { doc -> toCopyDocs << Long.valueOf(doc) }
            subscriptionService.copyDocs(baseSub, toCopyDocs, newSub, flash)
        }

        if (params?.subscription?.deleteAnnouncementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteAnnouncements = []
            params.list('subscription.deleteAnnouncementIds').each { announcement -> toDeleteAnnouncements << Long.valueOf(announcement) }
            subscriptionService.deleteAnnouncements(toDeleteAnnouncements, newSub, flash)
        }

        if (params?.subscription?.takeAnnouncementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyAnnouncements = []
            params.list('subscription.takeAnnouncementIds').each { announcement -> toCopyAnnouncements << Long.valueOf(announcement) }
            subscriptionService.copyAnnouncements(baseSub, toCopyAnnouncements, newSub, flash)
        }

        if (params?.subscription?.deleteTaskIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteTasks =  []
            params.list('subscription.deleteTaskIds').each{ tsk -> toDeleteTasks << Long.valueOf(tsk) }
            subscriptionService.deleteTasks(toDeleteTasks, newSub, flash)
        }

        if (params?.subscription?.takeTaskIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyTasks =  []
            params.list('subscription.takeTaskIds').each{ tsk -> toCopyTasks << Long.valueOf(tsk) }
            subscriptionService.copyTasks(baseSub, toCopyTasks, newSub, flash)
        }

        result.sourceSubscription = baseSub
        result.targetSubscription = newSub?.refresh()
        result.sourceTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.sourceSubscription)
        result.targetTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.targetSubscription)
//        params.workFlowPart = WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
//        params.workFlowPartNext = WORKFLOW_SUBSCRIBER
        params.workFlowPart = WORKFLOW_PROPERTIES
        params.workFlowPartNext = WORKFLOW_END
        result
    }

    private copySubElements_Subscriber() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        result.validSourceSubChilds = subscriptionService.getValidSubChilds(baseSub)
        if (params.targetSubscriptionId) {
            Subscription newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
            result.validTargetSubChilds = subscriptionService.getValidSubChilds(newSub)
        }


//        def newSubConsortia = Subscription.get(params.newSubscription)
//        def subMembers = []
//
//        params.list('selectedSubs').each { it -> subMembers << Long.valueOf(it) }
//
//        subMembers.each { sub -> def subMember = Subscription.findById(sub)
//
//            //ChildSub Exist
//            ArrayList<Links> prevLinks = Links.findAllByDestinationAndLinkTypeAndObjectType(subMember.id,RDStore.LINKTYPE_FOLLOWS,Subscription.class.name)
//            if (prevLinks.size() == 0) {
//
//                /* Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
//                    [result.subscriptionInstance, it.id])*/
//
//                def newSubscription = new Subscription(
//                        type: subMember.type,
//                        status: newSubConsortia.status,
//                        name: subMember.name,
//                        startDate: newSubConsortia.startDate,
//                        endDate: newSubConsortia.endDate,
//                        manualRenewalDate: subMember.manualRenewalDate,
//                        /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
//                        identifier: java.util.UUID.randomUUID().toString(),
//                        instanceOf: newSubConsortia?.id,
//                        //previousSubscription: subMember?.id,
//                        isSlaved: subMember.isSlaved,
//                        isPublic: subMember.isPublic,
//                        impId: java.util.UUID.randomUUID().toString(),
//                        owner: newSubConsortia.owner?.id ? subMember.owner?.id : null,
//                        resource: newSubConsortia.resource ?: null,
//                        form: newSubConsortia.form ?: null
//                )
//                newSubscription.save(flush: true)
//                //ERMS-892: insert preceding relation in new data model
//                if(subMember) {
//                    Links prevLink = new Links(source:newSubscription.id,destination:subMember.id,linkType:RDStore.LINKTYPE_FOLLOWS,objectType:Subscription.class.name,owner:contextService.org)
//                    if(!prevLink.save()) {
//                        log.error("Subscription linking failed, please check: ${prevLink.errors}")
//                    }
//                }
//
//                if (subMember.customProperties) {
//                    //customProperties
//                    for (prop in subMember.customProperties) {
//                        def copiedProp = new SubscriptionCustomProperty(type: prop.type, owner: newSubscription)
//                        copiedProp = prop.copyInto(copiedProp)
//                        copiedProp.save(flush: true)
//                        //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
//                    }
//                }
//                if (subMember.privateProperties) {
//                    //privatProperties
//                    List tenantOrgs = OrgRole.executeQuery('select o.org from OrgRole as o where o.sub = :sub and o.roleType in (:roleType)',[sub:subMember,roleType:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIPTION_CONSORTIA]]).collect {
//                        it -> it.id
//                    }
//                    subMember.privateProperties?.each { prop ->
//                        if (tenantOrgs.indexOf(prop.type?.tenant?.id) > -1) {
//                            def copiedProp = new SubscriptionPrivateProperty(type: prop.type, owner: newSubscription)
//                            copiedProp = prop.copyInto(copiedProp)
//                            copiedProp.save(flush: true)
//                            //newSubscription.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
//                        }
//                    }
//                }
//
//                if (subMember.packages && newSubConsortia.packages) {
//                    //Package
//                    subMember.packages?.each { pkg ->
//                        SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
//                        InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
//                        newSubscriptionPackage.subscription = newSubscription
//                        newSubscriptionPackage.save(flush: true)
//                    }
//                }
//                if (subMember.issueEntitlements && newSubConsortia.issueEntitlements) {
//                    subMember.issueEntitlements?.each { ie ->
//                        if (ie.status != RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')) {
//                            def ieProperties = ie.properties
//                            ieProperties.globalUID = null
//
//                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
//                            InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
//                            newIssueEntitlement.subscription = newSubscription
//                            newIssueEntitlement.save(flush: true)
//                        }
//                    }
//                }
//
//                //OrgRole
//                subMember.orgRelations?.each { or ->
//                    if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType.value in ['Subscriber', 'Subscriber_Consortial']) || (newSubConsortia.orgRelations.size() >= 1)) {
//                        OrgRole newOrgRole = new OrgRole()
//                        InvokerHelper.setProperties(newOrgRole, or.properties)
//                        newOrgRole.sub = newSubscription
//                        newOrgRole.save(flush: true)
//                    }
//                }
//
//                if (subMember.prsLinks && newSubConsortia.prsLinks) {
//                    //PersonRole
//                    subMember.prsLinks?.each { prsLink ->
//                        PersonRole newPersonRole = new PersonRole()
//                        InvokerHelper.setProperties(newPersonRole, prsLink.properties)
//                        newPersonRole.sub = newSubscription
//                        newPersonRole.save(flush: true)
//                    }
//                }
//            }
//        }
//        redirect controller: 'subscription', action: 'show', params: [id: newSubConsortia?.id]#
        result
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    Map copySubElements_Properties(){
        LinkedHashMap result = [customProperties:[:],privateProperties:[:]]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = null
        List<Subscription> subsToCompare = [baseSub]
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(params.targetSubscriptionId)
            subsToCompare.add(newSub)
        }
        List<AbstractProperty> propertiesToTake = params?.list('subscription.takeProperty').collect{ genericOIDService.resolveOID(it)}
        if (propertiesToTake && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.copyProperties(propertiesToTake, newSub, flash)
        }

        List<AbstractProperty> propertiesToDelete = params?.list('subscription.deleteProperty').collect{ genericOIDService.resolveOID(it)}
        if (propertiesToDelete && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.deleteProperties(propertiesToDelete, newSub, flash)
        }

        if (newSub) {
            result.newSub = newSub.refresh()
        }
        subsToCompare.each{ sub ->
            TreeMap customProperties = result.customProperties
            customProperties = comparisonService.buildComparisonTree(customProperties,sub,sub.customProperties)
            result.customProperties = customProperties
            TreeMap privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties,sub,sub.privateProperties)
            result.privateProperties = privateProperties
        }
        result
    }


    private copySubElements_PackagesEntitlements() {
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        if (params?.subscription?.deletePackageIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<SubscriptionPackage> packagesToDelete = params?.list('subscription.deletePackageIds').collect{ genericOIDService.resolveOID(it)}
            subscriptionService.deletePackages(packagesToDelete, newSub, flash)
        }
        if (params?.subscription?.takePackageIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<Package> packagesToTake = params?.list('subscription.takePackageIds').collect{ genericOIDService.resolveOID(it)}
            subscriptionService.copyPackages(packagesToTake, newSub, flash)
        }

        if (params?.subscription?.deleteEntitlementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<IssueEntitlement> entitlementsToDelete = params?.list('subscription.deleteEntitlementIds').collect{ genericOIDService.resolveOID(it)}
            subscriptionService.deleteEntitlements(entitlementsToDelete, newSub, flash)
        }
        if (params?.subscription?.takeEntitlementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<IssueEntitlement> entitlementsToTake = params?.list('subscription.takeEntitlementIds').collect{ genericOIDService.resolveOID(it)}
            subscriptionService.copyEntitlements(entitlementsToTake, newSub, flash)
        }

//        params?.workFlowPart = WORKFLOW_PACKAGES_ENTITLEMENTS
//        params?.workFlowPartNext = WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        params?.workFlowPart = WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        params?.workFlowPartNext = WORKFLOW_PROPERTIES
        if (newSub) {
            newSub.refresh()
        }

        result.sourceIEs = subscriptionService.getIssueEntitlements(baseSub)
        result.targetIEs = subscriptionService.getIssueEntitlements(newSub)
        result.newSub = newSub
        result.subscription = baseSub
        result
    }


    private boolean isBothSubscriptionsSet(Subscription baseSub, Subscription newSub) {
        if (! baseSub || !newSub) {
            if (!baseSub) flash.error += message(code: 'subscription.details.copyElementsIntoSubscription.noSubscriptionSource') + '<br />'
            if (!newSub)  flash.error += message(code: 'subscription.details.copyElementsIntoSubscription.noSubscriptionTarget') + '<br />'
            return false
        }
        return true
    }

    def copySubscription() {

        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        // tasks
        def contextOrg = contextService.getOrg()
        result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, contextOrg, result.subscriptionInstance)
        def preCon = taskService.getPreconditions(contextOrg)
        result << preCon


        result.visibleOrgRelations = []
        result.subscriptionInstance.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                result.visibleOrgRelations << or
            }
        }

        // -- private properties

        result.authorizedOrgs = result.user?.authorizedOrgs
        result.contextOrg = contextService.getOrg()

        // create mandatory OrgPrivateProperties if not existing

        def mandatories = []
        result.user?.authorizedOrgs?.each { org ->
            def ppd = PropertyDefinition.findAllByDescrAndMandatoryAndTenant("Subscription Property", true, org)
            if (ppd) {
                mandatories << ppd
            }
        }
        mandatories.flatten().each { pd ->
            if (!SubscriptionPrivateProperty.findWhere(owner: result.subscriptionInstance, type: pd)) {
                def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, result.subscriptionInstance, pd)

                if (newProp.hasErrors()) {
                    log.error(newProp.errors)
                } else {
                    log.debug("New subscription private property created via mandatory: " + newProp.type.name)
                }
            }
        }

        // -- private properties

        result.modalPrsLinkRole = RefdataValue.findByValue('Specific subscription editor')
        result.modalVisiblePersons = addressbookService.getPrivatePersonsByTenant(contextService.getOrg())

        result.visiblePrsLinks = []

        result.subscriptionInstance.prsLinks.each { pl ->
            if (!result.visiblePrsLinks.contains(pl.prs)) {
                if (pl.prs.isPublic?.value != 'No') {
                    result.visiblePrsLinks << pl
                } else {
                    // nasty lazy loading fix
                    result.user.authorizedOrgs.each { ao ->
                        if (ao.getId() == pl.prs.tenant.getId()) {
                            result.visiblePrsLinks << pl
                        }
                    }
                }
            }
        }

        result

    }

    def processcopySubscription() {

        params.id = params.baseSubscription
        def result = setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }

        def baseSubscription = com.k_int.kbplus.Subscription.get(params.baseSubscription)

        if (baseSubscription) {

            def sub_name = params.sub_name ?: "Kopie von ${baseSubscription.name}"

            def newSubscriptionInstance = new Subscription(
                    name: sub_name,
                    status: baseSubscription.status,
                    type: baseSubscription.type,
                    identifier: java.util.UUID.randomUUID().toString(),
                    isPublic: baseSubscription.isPublic,
                    isSlaved: baseSubscription.isSlaved,
                    startDate: params.subscription.copyDates ? baseSubscription?.startDate : null,
                    endDate: params.subscription.copyDates ? baseSubscription?.endDate : null,
                    resource: baseSubscription.resource ?: null,
                    form: baseSubscription.form ?: null,
            )


            if (!newSubscriptionInstance.save(flush: true)) {
                log.error("Problem saving subscription ${newSubscriptionInstance.errors}");
                return newSubscriptionInstance
            } else {
                log.debug("Save ok");

                baseSubscription.documents?.each { dctx ->

                    //Copy Docs
                    if (params.subscription.copyDocs) {
                        if (((dctx.owner?.contentType == 1) || (dctx.owner?.contentType == 3)) && (dctx.status?.value != 'Deleted')) {
                            Doc clonedContents = new Doc(
                                    blobContent: dctx.owner.blobContent,
                                    status: dctx.owner.status,
                                    type: dctx.owner.type,
                                    content: dctx.owner.content,
                                    uuid: dctx.owner.uuid,
                                    contentType: dctx.owner.contentType,
                                    title: dctx.owner.title,
                                    creator: dctx.owner.creator,
                                    filename: dctx.owner.filename,
                                    mimeType: dctx.owner.mimeType,
                                    user: dctx.owner.user,
                                    migrated: dctx.owner.migrated
                            ).save()

                            DocContext ndc = new DocContext(
                                    owner: clonedContents,
                                    subscription: newSubscriptionInstance,
                                    domain: dctx.domain,
                                    status: dctx.status,
                                    doctype: dctx.doctype
                            ).save()
                        }
                    }
                    //Copy Announcements
                    if (params.subscription.copyAnnouncements) {
                        if ((dctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                            Doc clonedContents = new Doc(
                                    blobContent: dctx.owner.blobContent,
                                    status: dctx.owner.status,
                                    type: dctx.owner.type,
                                    content: dctx.owner.content,
                                    uuid: dctx.owner.uuid,
                                    contentType: dctx.owner.contentType,
                                    title: dctx.owner.title,
                                    creator: dctx.owner.creator,
                                    filename: dctx.owner.filename,
                                    mimeType: dctx.owner.mimeType,
                                    user: dctx.owner.user,
                                    migrated: dctx.owner.migrated
                            ).save()

                            DocContext ndc = new DocContext(
                                    owner: clonedContents,
                                    subscription: newSubscriptionInstance,
                                    domain: dctx.domain,
                                    status: dctx.status,
                                    doctype: dctx.doctype
                            ).save()
                        }
                    }
                }
                //Copy Tasks
                if (params.subscription.copyTasks) {

                    Task.findAllBySubscription(baseSubscription).each { task ->

                        Task newTask = new Task()
                        InvokerHelper.setProperties(newTask, task.properties)
                        newTask.subscription = newSubscriptionInstance
                        newTask.save(flush: true)
                    }

                }
                //Copy References
                baseSubscription.orgRelations?.each { or ->
                    if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType.value in ['Subscriber', 'Subscriber_Consortial']) || (params.subscription.copyLinks)) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = newSubscriptionInstance
                        newOrgRole.save(flush: true)

                    }

                }

                //Copy Package
                if (params.subscription.copyPackages) {
                    baseSubscription.packages?.each { pkg ->
                        SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                        InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                        newSubscriptionPackage.subscription = newSubscriptionInstance
                        newSubscriptionPackage.save(flush: true)
                    }
                }
                //Copy License
                if (params.subscription.copyLicense) {
                    newSubscriptionInstance.owner = baseSubscription.owner ?: null
                    newSubscriptionInstance.save(flush: true)
                }
                //Copy InstanceOf
                if (params.subscription.copylinktoSubscription) {
                    newSubscriptionInstance.instanceOf = baseSubscription?.instanceOf ?: null
                }

                if (params.subscription.copyEntitlements) {

                    baseSubscription.issueEntitlements.each { ie ->

                        if (ie.status != RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')) {
                            def properties = ie.properties
                            properties.globalUID = null

                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                            InvokerHelper.setProperties(newIssueEntitlement, properties)
                            newIssueEntitlement.subscription = newSubscriptionInstance
                            newIssueEntitlement.save(flush: true)
                        }
                    }

                }

                if (params.subscription.copyCustomProperties) {
                    //customProperties
                    for (prop in baseSubscription.customProperties) {
                        def copiedProp = new SubscriptionCustomProperty(type: prop.type, owner: newSubscriptionInstance)
                        copiedProp = prop.copyInto(copiedProp)
                        copiedProp.instanceOf = null
                        copiedProp.save(flush: true)
                        //newSubscriptionInstance.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                    }
                }
                if (params.subscription.copyPrivateProperties) {
                    //privatProperties
                    def contextOrg = contextService.getOrg()

                    baseSubscription.privateProperties.each { prop ->
                        if (prop.type?.tenant?.id == contextOrg?.id) {
                            def copiedProp = new SubscriptionPrivateProperty(type: prop.type, owner: newSubscriptionInstance)
                            copiedProp = prop.copyInto(copiedProp)
                            copiedProp.save(flush: true)
                            //newSubscriptionInstance.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
                        }
                    }
                }


                redirect controller: 'subscription', action: 'show', params: [id: newSubscriptionInstance.id]
            }
        }

    }

    private LinkedHashMap setResultGenericsAndCheckAccess(checkOption) {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)
        result.subscription = Subscription.get(params.id)
        result.institution = result.subscription?.subscriber

        result.showConsortiaFunctions = showConsortiaFunctions(contextService.getOrg(), result.subscription)

        if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (!result.subscriptionInstance?.isVisibleBy(result.user)) {
                log.debug("--- NOT VISIBLE ---")
                return null
            }
        }
        result.editable = result.subscriptionInstance?.isEditableBy(result.user)

        if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (!result.editable) {
                log.debug("--- NOT EDITABLE ---")
                return null
            }
        }

        result
    }

    static boolean showConsortiaFunctions(Org contextOrg, Subscription subscription) {
        return ((subscription?.getConsortia()?.id == contextOrg?.id) && !subscription.instanceOf)
    }

    private def exportOrg(orgs, message, addHigherEducationTitles, format) {
        def titles = [
            'Name', g.message(code: 'org.shortname.label'), g.message(code: 'org.sortname.label')]

        def orgSector = RefdataValue.getByValueAndCategory('Higher Education', 'OrgSector')
        def orgType = RefdataValue.getByValueAndCategory('Provider', 'OrgRoleType')


        if (addHigherEducationTitles) {
            titles.add(g.message(code: 'org.libraryType.label'))
            titles.add(g.message(code: 'org.libraryNetwork.label'))
            titles.add(g.message(code: 'org.funderType.label'))
            titles.add(g.message(code: 'org.federalState.label'))
            titles.add(g.message(code: 'org.country.label'))
        }

        titles.add(g.message(code: 'subscription.details.startDate'))
        titles.add(g.message(code: 'subscription.details.endDate'))
        titles.add(g.message(code: 'subscription.details.status'))
        titles.add(RefdataValue.getByValueAndCategory('General contact person','Person Function').getI10n('value'))
        //titles.add(RefdataValue.getByValueAndCategory('Functional contact','Person Contact Type').getI10n('value'))

        def propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg())

        propList.sort { a, b -> a.name.compareToIgnoreCase b.name }

        propList.each {
            titles.add(it.name)
        }

        orgs.sort { it.sortname } //see ERMS-1196. If someone finds out how to put order clauses into GORM domain class mappings which include a join, then OK. Otherwise, we must do sorting here.
        try {
            if(format == "xlsx") {

                XSSFWorkbook workbook = new XSSFWorkbook()
                POIXMLProperties xmlProps = workbook.getProperties()
                POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
                coreProps.setCreator(g.message(code:'laser'))
                SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50,true)

                Sheet sheet = wb.createSheet(message)

                //the following three statements are required only for HSSF
                sheet.setAutobreaks(true)

                //the header row: centered text in 48pt font
                Row headerRow = sheet.createRow(0)
                headerRow.setHeightInPoints(16.75f)
                titles.eachWithIndex { titlesName, index ->
                    Cell cell = headerRow.createCell(index)
                    cell.setCellValue(titlesName)
                }

                //freeze the first row
                sheet.createFreezePane(0, 1)

                Row row
                Cell cell
                int rownum = 1


                orgs.each { org ->
                    int cellnum = 0
                    row = sheet.createRow(rownum)

                    //Name
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.name ?: '')

                    //Shortname
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.shortname ?: '')

                    //Sortname
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.sortname ?: '')


                    if (addHigherEducationTitles) {

                        //libraryType
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(org.libraryType?.getI10n('value') ?: ' ')

                        //libraryNetwork
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(org.libraryNetwork?.getI10n('value') ?: ' ')

                        //funderType
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(org.funderType?.getI10n('value') ?: ' ')

                        //federalState
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(org.federalState?.getI10n('value') ?: ' ')

                        //country
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(org.country?.getI10n('value') ?: ' ')
                    }

                    cell = row.createCell(cellnum++)
                    cell.setCellValue("${org.startDate ?: ''}")

                    cell = row.createCell(cellnum++)
                    cell.setCellValue("${org.endDate ?: ''}")

                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.status?.getI10n('value') ?: ' ')

                    cell = row.createCell(cellnum++)
                    cell.setCellValue(org.generalContacts ?: '')

                    /*cell = row.createCell(cellnum++)
                    cell.setCellValue('')*/

                    propList.each { pd ->
                        def value = ''
                        org.customProperties.each { prop ->
                            if (prop.type.descr == pd.descr && prop.type == pd) {
                                if (prop.type.type == Integer.toString()) {
                                    value = prop.intValue.toString()
                                } else if (prop.type.type == String.toString()) {
                                    value = prop.stringValue ?: ''
                                } else if (prop.type.type == BigDecimal.toString()) {
                                    value = prop.decValue.toString()
                                } else if (prop.type.type == Date.toString()) {
                                    value = prop.dateValue.toString()
                                } else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }

                        org.privateProperties.each { prop ->
                            if (prop.type.descr == pd.descr && prop.type == pd) {
                                if (prop.type.type == Integer.toString()) {
                                    value = prop.intValue.toString()
                                } else if (prop.type.type == String.toString()) {
                                    value = prop.stringValue ?: ''
                                } else if (prop.type.type == BigDecimal.toString()) {
                                    value = prop.decValue.toString()
                                } else if (prop.type.type == Date.toString()) {
                                    value = prop.dateValue.toString()
                                } else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }

                            }
                        }
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(value)
                    }

                    rownum++
                }

                for (int i = 0; i < titles.size(); i++) {
                    sheet.autoSizeColumn(i)
                }
                // Write the output to a file
                String file = message + ".xlsx"
                response.setHeader "Content-disposition", "attachment; filename=\"${file}\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()
            }
            else if(format == 'csv') {
                List orgData = []
                orgs.each{  org ->
                    List row = []
                    //Name
                    row.add(org.name ? org.name.replaceAll(',','') : '')
                    //Shortname
                    row.add(org.shortname ? org.shortname.replaceAll(',','') : '')
                    //Sortname
                    row.add(org.sortname ? org.sortname.replaceAll(',','') : '')
                    if(addHigherEducationTitles) {
                        //libraryType
                        row.add(org.libraryType?.getI10n('value') ?: ' ')
                        //libraryNetwork
                        row.add(org.libraryNetwork?.getI10n('value') ?: ' ')
                        //funderType
                        row.add(org.funderType?.getI10n('value') ?: ' ')
                        //federalState
                        row.add(org.federalState?.getI10n('value') ?: ' ')
                        //country
                        row.add(org.country?.getI10n('value') ?: ' ')
                    }
                    //startDate
                    row.add(org.startDate ?: '')
                    //endDate
                    row.add(org.endDate ?: '')
                    //status
                    row.add(org.status?.getI10n('value') ?: ' ')
                    //generalContacts
                    row.add(org.generalContacts ?: '')
                    propList.each { pd ->
                        def value = ''
                        org.customProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
                                if(prop.type.type == Integer.toString()){
                                    value = prop.intValue.toString()
                                }
                                else if (prop.type.type == String.toString()){
                                    value = prop.stringValue ?: ''
                                }
                                else if (prop.type.type == BigDecimal.toString()){
                                    value = prop.decValue.toString()
                                }
                                else if (prop.type.type == Date.toString()){
                                    value = prop.dateValue.toString()
                                }
                                else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }
                        org.privateProperties.each{ prop ->
                            if(prop.type.descr == pd.descr && prop.type == pd) {
                                if(prop.type.type == Integer.toString()){
                                    value = prop.intValue.toString()
                                }
                                else if (prop.type.type == String.toString()){
                                    value = prop.stringValue ?: ''
                                }
                                else if (prop.type.type == BigDecimal.toString()){
                                    value = prop.decValue.toString()
                                }
                                else if (prop.type.type == Date.toString()){
                                    value = prop.dateValue.toString()
                                }
                                else if (prop.type.type == RefdataValue.toString()) {
                                    value = prop.refValue?.getI10n('value') ?: ''
                                }
                            }
                        }
                        row.add(value.replaceAll(',',';'))
                    }
                    orgData.add(row)
                }
                return exportService.generateSeparatorTableString(titles,orgData,',')
            }
        }
        catch (Exception e) {
            log.error("Problem", e);
            response.sendError(500)
        }
    }

    private def setProperty(def property, def value) {

        def field = null


        if(property instanceof SubscriptionCustomProperty || property instanceof SubscriptionPrivateProperty)
        {

        }

        if(property.type.type == Integer.toString()) {
            field = "intValue"
        }
        else if (property.type.type == String.toString())  {
            field = "stringValue"
        }
        else if (property.type.type == BigDecimal.toString())  {
            field = "decValue"
        }
        else if (property.type.type == Date.toString())  {
            field = "dateValue"
        }
        else if (property.type.type == URL.toString())  {
            field = "urlValue"
        }
        else if (property.type.type == RefdataValue.toString())  {
            field = "refValue"
        }

        //Wenn eine Vererbung vorhanden ist.
        if(field && property.hasProperty('instanceOf') && property.instanceOf && AuditConfig.getConfig(property.instanceOf)){
            if(property.instanceOf."${field}" == '' || property.instanceOf."${field}" == null)
            {
                value = property.instanceOf."${field}" ?: ''
            }else{
                //
                return
            }
        }

        if (value == '' && field) {
            // Allow user to set a rel to null be calling set rel ''
            property[field] = null
            property.save(flush: true);
        } else {

            if (property && value && field){

                if(field == "refValue") {
                    def binding_properties = ["${field}": value]
                    bindData(property, binding_properties)
                    //property.save()
                    if(!property.save(failOnError: true, flush: true))
                    {
                        println(property.error)
                    }
                } else if(field == "dateValue") {
                    def sdf = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))

                    def backup = property."${field}"
                    try {
                        if (value && value.size() > 0) {
                            // parse new date
                            def parsed_date = sdf.parse(value)
                            property."${field}" = parsed_date
                        } else {
                            // delete existing date
                            property."${field}" = null
                        }
                        property.save(failOnError: true, flush: true);
                    }
                    catch (Exception e) {
                        property."${field}" = backup
                        log.error(e)
                    }
                } else if(field == "urlValue") {

                    def backup = property."${field}"
                    try {
                        if (value && value.size() > 0) {
                            property."${field}" = new URL(value)
                        } else {
                            // delete existing url
                            property."${field}" = null
                        }
                        property.save(failOnError: true, flush: true)
                    }
                    catch (Exception e) {
                        property."${field}" = backup
                        log.error(e)
                    }
                } else {
                    def binding_properties = [:]
                    if (property."${field}" instanceof Double) {
                        value = Double.parseDouble(value)
                    }

                    binding_properties["${field}"] = value
                    bindData(property, binding_properties)

                    property.save(failOnError: true, flush: true)

                }

            }
        }

    }
    def parseDate(datestr, possible_formats) {
        def parsed_date = null;
        if (datestr && (datestr.toString().trim().length() > 0)) {
            for (Iterator i = possible_formats.iterator(); (i.hasNext() && (parsed_date == null));) {
                try {
                    parsed_date = i.next().parse(datestr.toString());
                }
                catch (Exception e) {
                }
            }
        }
        parsed_date
    }
}
