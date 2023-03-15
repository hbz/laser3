package de.laser


import de.laser.ctrl.FinanceControllerService
import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.CostItemElementConfiguration
import de.laser.finance.CostItemGroup
import de.laser.exceptions.FinancialDataException
import de.laser.utils.DateUtils
import de.laser.annotations.DebugInfo
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.workflow.WfWorkflow
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.HttpStatus
import org.apache.poi.xssf.streaming.SXSSFWorkbook

import javax.servlet.ServletOutputStream
import java.math.RoundingMode
import java.text.SimpleDateFormat

/**
 * This is one of the more complex controllers in the app. It is responsible for managing the financial display and cost manipulation
 * calls.
 * This controller belongs to those which have received a service mirror to capsulate the complex data manipulation functions defined
 * directly in the controller in Grails 2
 * @see CostItem
 * @see FinanceControllerService
 * @see FinanceService
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class FinanceController  {

    AccessService accessService
    EscapeService escapeService
    ExportClickMeService exportClickMeService
    ExportService exportService
    FinanceControllerService financeControllerService
    FinanceService financeService
    GenericOIDService genericOIDService
    PendingChangeService pendingChangeService
    WorkflowService workflowService

    /**
     * Returns the financial overview page for the context institution. The number of visible tabs and
     * the cost items listed in them depends on the perspective taken and specified in the parameter map.
     * To see the decision tree, view {@link FinanceControllerService#getResultGenerics(grails.web.servlet.mvc.GrailsParameterMap)}
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def index() {
        log.debug("FinanceController::index() ${params}")
        try {
            Map<String,Object> result = financeControllerService.getResultGenerics(params)
            result.financialData = financeService.getCostItems(params,result)
            result.ciTitles = result.financialData.ciTitles
            result.budgetCodes = result.financialData.budgetCodes
            result.filterPresets = result.financialData.filterPresets
            result.filterSet = result.financialData.filterSet
            result.benchMark = result.financialData.benchMark
            result.allCIElements = CostItemElementConfiguration.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org',[org:result.institution])
            result.idSuffix = 'bulk'
            result
        }
        catch(FinancialDataException e) {
            flash.error = e.getMessage()
            redirect controller: "home"
            return
        }
    }

    /**
     * Returns the financial details view for the given subscription. The display depends upon the perspective taken
     * and specified in the parameter map, see {@link FinanceControllerService#getResultGenerics(grails.web.servlet.mvc.GrailsParameterMap)} for
     * the decision tree
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def subFinancialData() {
        log.debug("FinanceController::subFinancialData() ${params}")
        try {
            Map<String,Object> result = financeControllerService.getResultGenerics(params)
            result.financialData = financeService.getCostItemsForSubscription(params,result)
            result.currentTitlesCounts = IssueEntitlement.executeQuery("select count(ie.id) from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :status and ie.acceptStatus = :acceptStatus ", [sub: result.subscription, status: RDStore.TIPP_STATUS_CURRENT, acceptStatus: RDStore.IE_ACCEPT_STATUS_FIXED])[0]
            if (result.institution.isCustomerType_Consortium()) {
                if(result.subscription.instanceOf){
                    result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                            [sub: result.subscription.instanceOf,
                             org: result.subscription.getSubscriber(),
                             invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
                    result.currentCostItemCounts = result.financialData.subscr ? result.financialData.subscr.count : result.financialData.cons.count
                }else{
                    result.currentSurveysCounts = SurveyConfig.findAllBySubscription(result.subscription).size()
                    result.currentCostItemCounts = "${result.financialData.own.count}/${result.financialData.cons.count}"
                }
                result.currentMembersCounts =  Subscription.executeQuery('select count(s) from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',[parent: result.subscription, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])[0]
            }else{
                result.currentSurveysCounts = SurveyConfig.executeQuery("from SurveyConfig as surConfig where surConfig.subscription = :sub and surConfig.surveyInfo.status not in (:invalidStatuses) and (exists (select surOrg from SurveyOrg surOrg where surOrg.surveyConfig = surConfig AND surOrg.org = :org))",
                        [sub: result.subscription.instanceOf,
                         org: result.subscription.getSubscriber(),
                         invalidStatuses: [RDStore.SURVEY_IN_PROCESSING, RDStore.SURVEY_READY]]).size()
                if(result.institution.getCustomerType() == "ORG_INST") {
                    if(result.subscription.instanceOf)
                        result.currentCostItemCounts = "${result.financialData.own.count}/${result.financialData.subscr.count}"
                    else
                        result.currentCostItemCounts = result.financialData.own.count
                }
                else {
                    result.currentCostItemCounts = result.financialData.subscr.count
                }
            }
            result.workflowCount = workflowService.getWorkflowCount(result.subscription, result.contextOrg)
//            result.workflowCount = WfWorkflow.executeQuery(
//                    'select count(wf) from WfWorkflow wf where wf.subscription = :sub and wf.owner = :ctxOrg',
//                    [sub: result.subscription, ctxOrg: result.contextOrg]
//            )[0]
            result.ciTitles = result.financialData.ciTitles
            result.budgetCodes = result.financialData.budgetCodes
            result.filterPresets = result.financialData.filterPresets
            result.filterSet = result.financialData.filterSet
            result.allCIElements = CostItemElementConfiguration.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org',[org:result.institution])
            result.idSuffix = 'bulk'
            result
        }
        catch (FinancialDataException e) {
            flash.error = e.getMessage()
            redirect controller: 'myInstitution', action: 'currentSubscriptions'
            return
        }
    }

    /**
     * Exports the given financial data. Beware that multi-tab view is only possible in Excel; bare text exports
     * can only display the currently visible (= active) tab!
     * @return the financial data tab(s), as Excel worksheet or CSV export file
     */
    @DebugInfo(test = 'hasAffiliation("INST_USER")', ctrlService = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_USER") })
    def financialsExport()  {
        log.debug("Financial Export :: ${params}")
        Map<String, Object> result = financeControllerService.getResultGenerics(params+[forExport:true])
        if (!accessService.checkMinUserOrgRole(result.user,result.institution,"INST_USER")) {
            flash.error = message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A']) as String
            response.sendError(HttpStatus.SC_FORBIDDEN)
            return
        }
        Map financialData = result.subscription ? financeService.getCostItemsForSubscription(params,result) : financeService.getCostItems(params,result)
        result.cost_item_tabs = [:]
        if(result.dataToDisplay.contains("own")) {
            result.cost_item_tabs["own"] = financialData.own
        }
        if(result.dataToDisplay.contains("cons")) {
            result.cost_item_tabs["cons"] = financialData.cons
        }
        if(result.dataToDisplay.any { d -> ["subscr","consAtSubscr"].contains(d) }) {
            result.cost_item_tabs["subscr"] = financialData.subscr
        }
        SimpleDateFormat sdf = DateUtils.getSDF_noTimeNoPoint()
        String filename = result.subscription ? escapeService.escapeString(result.subscription.name)+"_financialExport" : escapeService.escapeString(result.institution.name)+"_financialExport"
        if(params.exportXLS) {
            SXSSFWorkbook workbook = exportService.processFinancialXLSX(result)
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            try {
                workbook.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                workbook.dispose()
            }
            catch (IOException e) {
                log.error("A request was started before the started one was terminated")
            }
        }
        else if(params.exportClickMeExcel) {
            if (params.filename) {
                filename =params.filename
            }

            Map<String, Object> selectedFieldsRaw = params.findAll{ it -> it.toString().startsWith('iex:') }
            Map<String, Object> selectedFields = [:]
            selectedFieldsRaw.each { it -> selectedFields.put( it.key.replaceFirst('iex:', ''), it.value ) }

            SXSSFWorkbook wb = (SXSSFWorkbook) exportClickMeService.exportCostItems(result, selectedFields)

            response.setHeader "Content-disposition", "attachment; filename=${filename}.xlsx"
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()
            return
        }
        else {
            ArrayList titles = []
            String viewMode = params.showView
            int sumcell = -1
            int sumcellAfterTax = -1
            int sumTitleCell = -1
            int sumCurrencyCell = -1
            int sumCurrencyAfterTaxCell = -1
            if(viewMode == "cons")
                titles.addAll([message(code:'org.sortName.label'),message(code:'financials.newCosts.costParticipants'),message(code:'financials.isVisibleForSubscriber')])
            titles.add(message(code: 'financials.newCosts.costTitle'))
            if(viewMode == "cons")
                titles.add(message(code:'default.provider.label'))
            titles.addAll([message(code: 'default.subscription.label'), message(code:'subscription.startDate.label'), message(code: 'subscription.endDate.label'),
                           message(code: 'financials.costItemConfiguration'), message(code: 'package.label'), message(code: 'issueEntitlement.label'),
                           message(code: 'financials.datePaid'), message(code: 'financials.dateFrom'), message(code: 'financials.dateTo'), message(code:'financials.financialYear'),
                           message(code: 'default.status.label'), message(code: 'default.currency.label'), message(code: 'financials.costInBillingCurrency'),"EUR",
                           message(code: 'financials.costInLocalCurrency')])
            if(["own","cons"].indexOf(viewMode) > -1)
                titles.addAll(message(code: 'financials.taxRate'), [message(code:'default.currency.label'),message(code: 'financials.costInBillingCurrencyAfterTax'),"EUR",message(code: 'financials.costInLocalCurrencyAfterTax')])
            titles.addAll([message(code: 'financials.costItemElement'), message(code: 'default.description.label'),
                           message(code: 'financials.newCosts.costsReferenceOn'), message(code: 'financials.budgetCode'),
                           message(code: 'financials.invoice_number'), message(code: 'financials.order_number')])
            SimpleDateFormat dateFormat = DateUtils.getLocalizedSDF_noTime()
            //LinkedHashMap<Subscription,List<Org>> subscribers = [:]
            //LinkedHashMap<Subscription,Set<Org>> providers = [:]
            LinkedHashMap<Subscription,BudgetCode> costItemGroups = [:]
            /*OrgRole.findAllByRoleTypeInList([RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]).each { it ->
                List<Org> orgs = subscribers.get(it.sub)
                if(orgs == null)
                    orgs = [it.org]
                else orgs.add(it.org)
                subscribers.put(it.sub,orgs)
            }
            OrgRole.findAllByRoleTypeInList([RDStore.OR_PROVIDER,RDStore.OR_AGENCY]).each { it ->
                Set<Org> orgs = providers.get(it.sub)
                if(orgs == null)
                    orgs = [it.org]
                else orgs.add(it.org)
                providers.put(it.sub,orgs)
            }*/
            CostItemGroup.findAll().each{ cig -> costItemGroups.put(cig.costItem,cig.budgetCode) }
            withFormat {
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date())}_${filename}_${viewMode}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        ArrayList rowData = []
                        if(financialData[viewMode].count > 0) {
                            ArrayList row
                            financialData[viewMode].costItems.each { CostItem ci ->
                                BudgetCode codes = costItemGroups.get(ci)
                                String start_date   = ci.startDate ? dateFormat.format(ci?.startDate) : ''
                                String end_date     = ci.endDate ? dateFormat.format(ci?.endDate) : ''
                                String paid_date    = ci.datePaid ? dateFormat.format(ci?.datePaid) : ''
                                row = []
                                int cellnum = 0
                                if(viewMode == "cons") {
                                    if(ci.sub) {
                                        List<Org> orgRoles = ci.sub.orgRelations.findAll { OrgRole oo -> oo.roleType in [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN] }.collect { it.org }
                                        //participants (visible?)
                                        String cellValueA = ""
                                        String cellValueB = ""
                                        orgRoles.each { or ->
                                            cellValueA += or.sortname
                                            cellValueB += or.name
                                        }
                                        cellnum++
                                        row.add(cellValueA)
                                        cellnum++
                                        row.add(cellValueB)
                                        cellnum++
                                        row.add(ci.isVisibleForSubscriber ? message(code:'financials.isVisibleForSubscriber') : " ")
                                    }
                                }
                                //cost title
                                cellnum++
                                row.add(ci.costTitle ?: '')
                                if(viewMode == "cons") {
                                    //provider
                                    cellnum++
                                    if(ci.sub) {
                                        Set<Org> orgRoles = ci.sub.orgRelations.findAll { OrgRole oo -> oo.roleType in [RDStore.OR_PROVIDER,RDStore.OR_AGENCY] }.collect { it.org }
                                        String cellValue = ""
                                        orgRoles.each { or ->
                                            cellValue += or.name
                                        }
                                        row.add(cellValue)
                                    }
                                    else row.add(" ")
                                }
                                //subscription
                                cellnum++
                                row.add(ci.sub ? ci.sub.name : "")
                                //dates from-to
                                if(ci.sub) {
                                    cellnum++
                                    if(ci.sub.startDate)
                                        row.add(dateFormat.format(ci.sub.startDate))
                                    else
                                        row.add("")
                                    cellnum++
                                    if(ci.sub.endDate)
                                        row.add(dateFormat.format(ci.sub.endDate))
                                    else
                                        row.add("")
                                }
                                //cost sign
                                cellnum++
                                if(ci.costItemElementConfiguration) {
                                    row.add(ci.costItemElementConfiguration.getI10n("value"))
                                }
                                else
                                    row.add(message(code:'financials.costItemConfiguration.notSet'))
                                //subscription package
                                cellnum++
                                row.add(ci.subPkg ? ci.subPkg.pkg.name:'')
                                //issue entitlement
                                cellnum++
                                row.add(ci.issueEntitlement ? ci.issueEntitlement?.tipp?.name:'')
                                //date paid
                                cellnum++
                                row.add(paid_date ?: '')
                                //date from
                                cellnum++
                                row.add(start_date ?: '')
                                //date to
                                cellnum++
                                row.add(end_date ?: '')
                                //financial year
                                cellnum++
                                row.add(ci.financialYear ? ci.financialYear.toString() : '')
                                //for the sum title
                                sumTitleCell = cellnum
                                //cost item status
                                cellnum++
                                row.add(ci.costItemStatus ? ci.costItemStatus.getI10n("value"):'')
                                if(["own","cons"].indexOf(viewMode) > -1) {
                                    sumCurrencyCell = cellnum
                                    cellnum++
                                    //billing currency and value
                                    row.add(ci.billingCurrency ? ci.billingCurrency.value : '')
                                    cellnum++
                                    row.add(ci.costInBillingCurrency ? ci.costInBillingCurrency : 0.0)
                                    sumcell = cellnum
                                    //local currency and value
                                    cellnum++
                                    row.add("EUR")
                                    cellnum++
                                    row.add(ci.costInLocalCurrency ? ci.costInLocalCurrency : 0.0)
                                    sumCurrencyAfterTaxCell = cellnum
                                    //tax rate
                                    cellnum++
                                    String taxString
                                    if(ci.taxKey && ci.taxKey.display) {
                                        taxString = "${ci.taxKey.taxType.getI10n('value')} (${ci.taxKey.taxRate} %)"
                                    }
                                    else if(ci.taxKey in [CostItem.TAX_TYPES.TAX_CONTAINED_7, CostItem.TAX_TYPES.TAX_CONTAINED_19, CostItem.TAX_TYPES.TAX_REVERSE_CHARGE]) {
                                        taxString = "${ci.taxKey.taxType.getI10n('value')}"
                                    }
                                    else taxString = message(code:'financials.taxRate.notSet')
                                    row.add(taxString)
                                }
                                if(["own","cons"].indexOf(viewMode) < 0)
                                    sumCurrencyAfterTaxCell = cellnum
                                //billing currency and value
                                cellnum++
                                row.add(ci.billingCurrency ? ci.billingCurrency.value : '')
                                if(["own","cons"].indexOf(viewMode) > -1)
                                    sumcellAfterTax = cellnum
                                cellnum++
                                row.add(ci.costInBillingCurrencyAfterTax ? ci.costInBillingCurrencyAfterTax : 0.0)
                                if(["own","cons"].indexOf(viewMode) < 0)
                                    sumcellAfterTax = cellnum
                                //local currency and value
                                cellnum++
                                row.add("EUR")
                                cellnum++
                                row.add(ci.costInLocalCurrencyAfterTax ? ci.costInLocalCurrencyAfterTax : 0.0)
                                //cost item element
                                cellnum++
                                row.add(ci.costItemElement?ci.costItemElement.getI10n("value") : '')
                                //cost item description
                                cellnum++
                                row.add(ci.costDescription?: '')
                                //reference
                                cellnum++
                                row.add(ci.reference?:'')
                                //budget codes
                                cellnum++
                                row.add(codes ? codes.value : '')
                                //invoice number
                                cellnum++
                                row.add(ci.invoice ? ci.invoice.invoiceNumber : "")
                                //order number
                                cellnum++
                                row.add(ci.order ? ci.order.orderNumber : "")
                                //rownum++
                                rowData.add(row)
                            }
                            rowData.add([])
                            List sumRow = []
                            int h = 0
                            for(h;h < sumTitleCell;h++) {
                                sumRow.add(" ")
                            }
                            sumRow.add(message(code:'financials.export.sums'))
                            if(sumcell > 0) {
                                for(h;h < sumcell;h++) {
                                    sumRow.add(" ")
                                }
                                BigDecimal localSum = BigDecimal.valueOf(financialData[viewMode].sums.localSums.localSum)
                                sumRow.add(localSum.setScale(2,RoundingMode.HALF_UP))
                            }
                            for(h;h < sumcellAfterTax;h++) {
                                sumRow.add(" ")
                            }
                            BigDecimal localSumAfterTax = BigDecimal.valueOf(financialData[viewMode].sums.localSums.localSumAfterTax)
                            sumRow.add(localSumAfterTax.setScale(2,RoundingMode.HALF_UP))
                            rowData.add(sumRow)
                            rowData.add([])
                            financialData[viewMode].sums.billingSums.each { entry ->
                                int i = 0
                                sumRow = []
                                for(i;i < sumTitleCell;i++) {
                                    sumRow.add(" ")
                                }
                                sumRow.add(entry.currency)
                                if(sumCurrencyCell > 0) {
                                    for(i;i < sumCurrencyCell;i++) {
                                        sumRow.add(" ")
                                    }
                                    BigDecimal billingSum = BigDecimal.valueOf(entry.billingSum)
                                    sumRow.add(billingSum.setScale(2,RoundingMode.HALF_UP))
                                }
                                for(i;i < sumCurrencyAfterTaxCell;i++) {
                                    sumRow.add(" ")
                                }
                                BigDecimal billingSumAfterTax = BigDecimal.valueOf(entry.billingSumAfterTax)
                                sumRow.add(billingSumAfterTax.setScale(2,RoundingMode.HALF_UP))
                                rowData.add(sumRow)
                            }
                            writer.write(exportService.generateSeparatorTableString(titles,rowData,';'))
                        }
                        else {
                            writer.write(message(code:'finance.export.empty'))
                        }
                    }
                    out.close()
                }
            }
        }
    }

    /**
     * Calls the cost item creation modal and sets the edit parameters
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    Object newCostItem() {
        Map<String, Object> result = financeControllerService.getResultGenerics(params)
        result.modalText = message(code:'financials.addNewCost')
        result.submitButtonLabel = message(code:'default.button.create_new.label')
        result.formUrl = g.createLink(controller:'finance', action:'createOrUpdateCostItem', params:[showView: params.showView, offset: params.offset])
        Set<String> pickedSubscriptions = []
        JSON.parse(params.preselectedSubscriptions).each { String ciId ->
            CostItem ci = CostItem.get(Long.parseLong(ciId))
            pickedSubscriptions << "'${genericOIDService.getOID(ci.sub)}'"
        }
        result.pickedSubscriptions = pickedSubscriptions
        result.idSuffix = "new"
        render(template: "/finance/ajaxModal", model: result)
    }

    /**
     * Calls the cost item creation modal, sets the edit parameters and prefills the form values with the existing cost item data
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    Object editCostItem() {
        Map<String, Object> result = financeControllerService.getResultGenerics(params)
        result.costItem = CostItem.get(params.id)
        if(result.costItem.taxKey)
            result.taxKey = result.costItem.taxKey
        result.modalText = message(code: 'financials.editCost')
        result.submitButtonLabel = message(code:'default.button.save.label')
        result.formUrl = g.createLink(controller:'finance', action:'createOrUpdateCostItem', params:[showView: params.showView, offset: params.offset])
        result.idSuffix = "edit_${params.id}"
        render(template: "/finance/ajaxModal", model: result)
    }

    /**
     * Calls the cost item creation modal, sets the editing parameters and prefills the form values with the copy base data.
     * After submitting the form, a new cost item will be created which has the current one as base, taking those values
     * submitted in the modal
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")',ctrlService = DebugInfo.NOT_TRANSACTIONAL)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    Object copyCostItem() {
        Map<String, Object> result = financeControllerService.getResultGenerics(params)
        result.costItem = CostItem.get(params.id)
        params.status = [result.costItem.sub.status.id.toString()]
        result.modalText = message(code: 'financials.costItem.copy.tooltip')
        result.submitButtonLabel = message(code:'default.button.copy.label')
        result.copyCostsFromConsortia = result.costItem.owner == result.costItem.sub?.getConsortia() && result.institution.id != result.costItem.sub?.getConsortia().id
        result.copyToOtherSub =  !result.copyCostsFromConsortia && result.costItem.owner.id == result.institution.id && result.institution.getCustomerType() == 'ORG_INST'
        result.taxKey = result.costItem.taxKey
        result.formUrl = createLink(controller:"finance",action:"createOrUpdateCostItem",params:[showView:params.showView, mode:"copy", offset: params.offset])
        result.mode = "copy"
        result.idSuffix = "copy_${params.id}"
        render template: "/finance/ajaxModal", model: result
    }

    /**
     * Call to delete a given cost item
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def deleteCostItem() {
        Map<String,Object> ctrlResult = financeService.deleteCostItem(params)
        if(ctrlResult.error == FinanceService.STATUS_ERROR)
            flash.error = ctrlResult.result.error
        redirect(uri: request.getHeader('referer').replaceAll('(#|\\?).*', ''), params: [showView: ctrlResult.result.showView, offset: params.offset])
    }

    /**
     * Call to process the submitted form values in order to create or update a cost item
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")', ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def createOrUpdateCostItem() {
        Map<String,Object> ctrlResult = financeService.createOrUpdateCostItem(params)
        if(ctrlResult.error == FinanceService.STATUS_ERROR) {
            flash.error = ctrlResult.result.error
        }
        else {
            params.remove("Add")
        }
        redirect(uri: request.getHeader('referer').replaceAll('(#|\\?).*', ''), params: [showView: ctrlResult.result.showView, offset: params.offset])
    }

    /**
     * Call to import cost items submitted from the import post processing view
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM_BASIC", affil="INST_EDITOR", specRole="ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_INST,ORG_CONSORTIUM_BASIC", "INST_EDITOR", "ROLE_ADMIN")
    })
    def importCostItems() {
        Map<String,Object> ctrlResult = financeService.importCostItems(params)
        if(ctrlResult.status == FinanceService.STATUS_ERROR) {
            redirect controller: 'myInstitution', action: 'financeImport'
            return
        }
        else {
            redirect action: 'index'
            return
        }
    }

    /**
     * Marks a change done by the consortium as acknowledged by the single user who copied the given cost item
     */
    @DebugInfo(perm="ORG_INST,ORG_CONSORTIUM_BASIC", affil="INST_EDITOR", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM_BASIC", "INST_EDITOR") })
    def acknowledgeChange() {
        PendingChange changeAccepted = PendingChange.get(params.id)
        if(changeAccepted)
            pendingChangeService.acknowledgeChange(changeAccepted)
        redirect(uri:request.getHeader('referer'))
    }

    /**
     * Call to process the data in the bulk editing form and to apply the changes to the picked cost items
     */
    @DebugInfo(perm = "ORG_CONSORTIUM_BASIC", affil = "INST_EDITOR", specRole = "ROLE_ADMIN", ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_BASIC", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processCostItemsBulk() {
        Map<String,Object> ctrlResult = financeService.processCostItemsBulk(params)
        if(ctrlResult.result.failures) {
            flash.error = message(code: 'financials.bulkCostItems.noCostItems', args: [ctrlResult.result.failures.join(', ')])
        }
        redirect(url: request.getHeader('referer'))
    }
}
