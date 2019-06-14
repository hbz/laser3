package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang.StringUtils
import org.apache.poi.POIXMLProperties
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.xssf.streaming.SXSSFSheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFColor
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.context.i18n.LocaleContextHolder

import javax.servlet.ServletOutputStream
import java.awt.Color
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Year

@Secured(['IS_AUTHENTICATED_FULLY'])
class FinanceController extends AbstractDebugController {

    def springSecurityService
    def accessService
    def contextService
    def genericOIDService
    def navigationGenerationService
    def filterService
    def financeService
    def escapeService
    def exportService

    private final RefdataValue defaultCurrency = RefdataValue.getByValueAndCategory('EUR', 'Currency')

    private final static MODE_OWNER          = 'MODE_OWNER'
    private final static MODE_CONS           = 'MODE_CONS'
    private final static MODE_CONS_AT_SUBSCR = 'MODE_CONS_AT_SUBSCR'
    private final static MODE_SUBSCR         = 'MODE_SUBSCR'

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def index() {
        log.debug("FinanceController::index() ${params}")
        LinkedHashMap result = setResultGenerics()
        result.editable = accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN')
        result.max = params.max ? Long.parseLong(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0
        switch(params.view) {
            case "own": result.ownOffset = result.offset
                break
            case "cons": result.consOffset = result.offset
                break
            case "subscr": result.subscrOffset = result.offset
                break
            default: log.info("unhandled view: ${params.view}")
                break
        }
        result.financialData = financeService.getCostItems(params,result.max)
        //replaces the mode check MODE_CONS vs. MODE_SUBSCR
        if(accessService.checkPermAffiliation("ORG_CONSORTIUM","INST_USER")) {
            result.showView = "cons"
            params.comboType = 'Consortium'
            def fsq = filterService.getOrgComboQuery(params,result.institution)
            result.subscriptionParticipants = OrgRole.executeQuery(fsq.query,fsq.queryParams)
        }
        else result.showView = "subscr"
        if(params.ownSort)
            result.view = "own"
        else if(params.consSort)
            result.view = "cons"
        else if(params.subscrSort)
            result.view = "subscr"
        else result.view = params.view ? params.view : result.showView
        result.filterPresets = result.financialData.filterPresets
        result.allCIElements = CostItemElementConfiguration.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org',[org:result.institution])
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def subFinancialData() {
        log.debug("FinanceController::subFinancialData() ${params}")
        LinkedHashMap result = setResultGenerics()
        result.editable = accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN')
        result.max = params.max ? Long.parseLong(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0
        if(result.subscription.instanceOf && result.institution.id == result.subscription.getConsortia().id)
            params.view = "consAtSubscr"
        switch(params.view) {
            case "own": result.ownOffset = result.offset
                break
            case "cons":
            case "consAtSubscr": result.consOffset = result.offset
                break
            case "subscr": result.subscrOffset = result.offset
                break
            default: log.info("unhandled view: ${params.view}")
                break
        }
        result.financialData = financeService.getCostItemsForSubscription(result.subscription,params,result.max,result.offset)
        if(OrgRole.findBySubAndOrgAndRoleType(result.subscription,result.institution,RDStore.OR_SUBSCRIPTION_CONSORTIA)) {
            result.showView = "cons"
            if(params.view.equals("consAtSubscr"))
                result.showView = "consAtSubscr"
            params.comboType = "Consortium"
            Map fsq = filterService.getOrgComboQuery(params,result.institution)
            result.subscriptionParticipants = OrgRole.executeQuery(fsq.query,fsq.queryParams)
        }
        else if(OrgRole.findBySubAndOrgAndRoleType(result.subscription,result.institution,RDStore.OR_SUBSCRIBER_CONS))
            result.showView = "subscr"
        else if(accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_USER"))
            result.showView = "own"
        if(params.ownSort)
            result.view = "own"
        else if(params.consSort) {
            result.view = "cons"
            if(params.view == "consAtSubscr")
                result.view = "consAtSubscr"
        }
        else if(params.subscrSort)
            result.view = "subscr"
        else result.view = params.view ? params.view : result.showView
        result.filterPresets = result.financialData.filterPresets
        result.allCIElements = CostItemElementConfiguration.executeQuery('select ciec.costItemElement from CostItemElementConfiguration ciec where ciec.forOrganisation = :org',[org:result.institution])
        Map navigation = navigationGenerationService.generateNavigation(Subscription.class.name,result.subscription.id)
        result.navNextSubscription = navigation.nextLink
        result.navPrevSubscription = navigation.prevLink
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def financialsExport()  {
        log.debug("Financial Export :: ${params}")
        Map result = setResultGenerics()
        if (!accessService.checkMinUserOrgRole(result.user,result.institution,"INST_USER")) {
            flash.error=message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A'])
            response.sendError(403)
            return
        }
        // I need the consortial data as well ...
        def orgRoleCons = accessService.checkPerm('ORG_CONSORTIUM')
        def orgRoleSubscr = OrgRole.findByRoleType(RDStore.OR_SUBSCRIBER_CONS)
        Map financialData = result.subscription ? financeService.getCostItemsForSubscription(result.subscription,params,Long.MAX_VALUE,0) : financeService.getCostItems(params,Long.MAX_VALUE)
        result.cost_item_tabs = [:]
        if(accessService.checkPerm('ORG_INST,ORG_CONSORTIUM')) {
            result.cost_item_tabs["own"] = financialData.own
        }
        if(orgRoleCons) {
            result.cost_item_tabs["cons"] = financialData.cons
        }
        else if(orgRoleSubscr) {
            result.cost_item_tabs["subscr"] = financialData.subscr
        }
        SXSSFWorkbook workbook = processFinancialXLSX(result)
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code:'default.date.format.notimenopoint'))
        String filename = result.subscription ? escapeService.escapeString(result.subscription.name)+"_financialExport" : escapeService.escapeString(result.institution.name)+"_financialExport"
        if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date(System.currentTimeMillis()))}_${filename}.xlsx\"")
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
            titles.addAll([message(code: 'financials.forSubscription'), message(code:'subscription.startDate.label'), message(code: 'subscription.endDate.label'),
                           message(code: 'financials.costItemConfiguration'), message(code: 'package'), message(code: 'issueEntitlement.label'),
                           message(code: 'financials.datePaid'), message(code: 'financials.dateFrom'), message(code: 'financials.dateTo'),
                           message(code: 'financials.costItemStatus'), message(code: 'financials.billingCurrency'), message(code: 'financials.costInBillingCurrency'),"EUR",
                           message(code: 'financials.costInLocalCurrency')])
            if(["own","cons"].indexOf(viewMode) > -1)
                titles.addAll(message(code: 'financials.taxRate'), [message(code:'financials.billingCurrency'),message(code: 'financials.costInBillingCurrencyAfterTax'),"EUR",message(code: 'financials.costInLocalCurrencyAfterTax')])
            titles.addAll([message(code: 'financials.costItemElement'),message(code: 'financials.newCosts.description'),
                           message(code: 'financials.newCosts.constsReferenceOn'), message(code: 'financials.budgetCode'),
                           message(code: 'financials.invoice_number'), message(code: 'financials.order_number')])
            SimpleDateFormat dateFormat = new SimpleDateFormat(message(code: 'default.date.format.notime', default: 'dd.MM.yyyy'))
            LinkedHashMap<Subscription,List<Org>> subscribers = [:]
            LinkedHashMap<Subscription,List<Org>> providers = [:]
            LinkedHashMap<Subscription,BudgetCode> costItemGroups = [:]
            OrgRole.findAllByRoleType(RDStore.OR_SUBSCRIBER_CONS).each { it ->
                List<Org> orgs = subscribers.get(it.sub)
                if(orgs == null)
                    orgs = [it.org]
                else orgs.add(it.org)
                subscribers.put(it.sub,orgs)
            }
            OrgRole.findAllByRoleType(RDStore.OR_PROVIDER).each { it ->
                List<Org> orgs = providers.get(it.sub)
                if(orgs == null)
                    orgs = [it.org]
                else orgs.add(it.org)
                providers.put(it.sub,orgs)
            }
            CostItemGroup.findAll().each{ cig -> costItemGroups.put(cig.costItem,cig.budgetCode) }
            withFormat {
                csv {
                    response.setHeader("Content-disposition", "attachment; filename=\"${sdf.format(new Date(System.currentTimeMillis()))}_${filename}_${viewMode}.csv\"")
                    response.contentType = "text/csv"
                    ServletOutputStream out = response.outputStream
                    out.withWriter { writer ->
                        ArrayList rowData = []
                        if(financialData[viewMode].count > 0) {
                            ArrayList row
                            financialData[viewMode].costItems.each { ci ->
                                BudgetCode codes = costItemGroups.get(ci)
                                String start_date   = ci.startDate ? dateFormat.format(ci?.startDate) : ''
                                String end_date     = ci.endDate ? dateFormat.format(ci?.endDate) : ''
                                String paid_date    = ci.datePaid ? dateFormat.format(ci?.datePaid) : ''
                                row = []
                                int cellnum = 0
                                if(viewMode == "cons") {
                                    if(ci.sub) {
                                        List<Org> orgRoles = subscribers.get(ci.sub)
                                        //participants (visible?)
                                        String cellValueA = ""
                                        String cellValueB = ""
                                        orgRoles.each { or ->
                                            cellValueA += or.sortname.replace(',',':')
                                            cellValueB += or.name.replace(',','')
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
                                row.add(ci.costTitle ? ci.costTitle.replaceAll(',','') : '')
                                if(viewMode == "cons") {
                                    //provider
                                    cellnum++
                                    if(ci.sub) {
                                        List<Org> orgRoles = providers.get(ci.sub)
                                        String cellValue = ""
                                        orgRoles.each { or ->
                                            cellValue += or.name.replace(',','')
                                        }
                                        row.add(cellValue)
                                    }
                                    else row.add(" ")
                                }
                                //subscription
                                cellnum++
                                row.add(ci.sub ? ci.sub.name.replaceAll(',','') : "")
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
                                row.add(ci?.subPkg ? ci.subPkg.pkg.name:'')
                                //issue entitlement
                                cellnum++
                                row.add(ci?.issueEntitlement ? ci.issueEntitlement?.tipp?.title?.title:'')
                                //date paid
                                cellnum++
                                row.add(paid_date ?: '')
                                //date from
                                cellnum++
                                row.add(start_date ?: '')
                                //date to
                                cellnum++
                                row.add(end_date ?: '')
                                //for the sum title
                                sumTitleCell = cellnum
                                //cost item status
                                cellnum++
                                row.add(ci?.costItemStatus ? ci.costItemStatus.getI10n("value"):'')
                                if(["own","cons"].indexOf(viewMode) > -1) {
                                    sumCurrencyCell = cellnum
                                    cellnum++
                                    //billing currency and value
                                    row.add(ci?.billingCurrency ? ci.billingCurrency.value : '')
                                    cellnum++
                                    row.add(ci?.costInBillingCurrency ? ci.costInBillingCurrency : 0.0)
                                    sumcell = cellnum
                                    //local currency and value
                                    cellnum++
                                    row.add("EUR")
                                    cellnum++
                                    row.add(ci?.costInLocalCurrency ? ci.costInLocalCurrency : 0.0)
                                    sumCurrencyAfterTaxCell = cellnum
                                    //tax rate
                                    cellnum++
                                    row.add("${ci.taxRate ?: 0} %")
                                }
                                if(["own","cons"].indexOf(viewMode) < 0)
                                    sumCurrencyAfterTaxCell = cellnum
                                //billing currency and value
                                cellnum++
                                row.add(ci?.billingCurrency ? ci.billingCurrency.value : '')
                                if(["own","cons"].indexOf(viewMode) > -1)
                                    sumcellAfterTax = cellnum
                                cellnum++
                                row.add(ci?.costInBillingCurrencyAfterTax ? ci.costInBillingCurrencyAfterTax : 0.0)
                                if(["own","cons"].indexOf(viewMode) < 0)
                                    sumcellAfterTax = cellnum
                                //local currency and value
                                cellnum++
                                row.add("EUR")
                                cellnum++
                                row.add(ci?.costInLocalCurrencyAfterTax ? ci.costInLocalCurrencyAfterTax : 0.0)
                                //cost item element
                                cellnum++
                                row.add(ci?.costItemElement?ci.costItemElement.getI10n("value") : '')
                                //cost item description
                                cellnum++
                                row.add(ci?.costDescription?: '')
                                //reference
                                cellnum++
                                row.add(ci?.reference?:'')
                                //budget codes
                                cellnum++
                                row.add(codes ? codes.value : '')
                                //invoice number
                                cellnum++
                                row.add(ci?.invoice ? ci.invoice.invoiceNumber : "")
                                //order number
                                cellnum++
                                row.add(ci?.order ? ci.order.orderNumber : "")
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
                                sumRow.add(financialData[viewMode].sums.localSums.localSum)
                            }
                            for(h;h < sumcellAfterTax;h++) {
                                sumRow.add(" ")
                            }
                            sumRow.add(financialData[viewMode].sums.localSums.localSumAfterTax)
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
                                    sumRow.add(entry.billingSum)
                                }
                                for(i;i < sumCurrencyAfterTaxCell;i++) {
                                    sumRow.add(" ")
                                }
                                sumRow.add(entry.billingSumAfterTax)
                                rowData.add(sumRow)
                            }
                            writer.write(exportService.generateSeparatorTableString(titles,rowData,','))
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
     * Make a XLSX export of cost item results
     * @param result - passed from index
     * @return
     */
    SXSSFWorkbook processFinancialXLSX(result) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(message(code: 'default.date.format.notime', default: 'dd.MM.yyyy'))
        XSSFWorkbook workbook = new XSSFWorkbook()
        POIXMLProperties xmlProps = workbook.getProperties()
        POIXMLProperties.CoreProperties coreProps = xmlProps.getCoreProperties()
        coreProps.setCreator(message(code:'laser'))
        LinkedHashMap<Subscription,List<Org>> subscribers = [:]
        LinkedHashMap<Subscription,List<Org>> providers = [:]
        LinkedHashMap<Subscription,BudgetCode> costItemGroups = [:]
        OrgRole.findAllByRoleType(RDStore.OR_SUBSCRIBER_CONS).each { it ->
            List<Org> orgs = subscribers.get(it.sub)
            if(orgs == null)
                orgs = [it.org]
            else orgs.add(it.org)
            subscribers.put(it.sub,orgs)
        }
        OrgRole.findAllByRoleType(RDStore.OR_PROVIDER).each { it ->
            List<Org> orgs = providers.get(it.sub)
            if(orgs == null)
                orgs = [it.org]
            else orgs.add(it.org)
            providers.put(it.sub,orgs)
        }
        XSSFCellStyle csPositive = workbook.createCellStyle()
        csPositive.setFillForegroundColor(new XSSFColor(new Color(198,239,206)))
        csPositive.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        XSSFCellStyle csNegative = workbook.createCellStyle()
        csNegative.setFillForegroundColor(new XSSFColor(new Color(255,199,206)))
        csNegative.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        XSSFCellStyle csNeutral = workbook.createCellStyle()
        csNeutral.setFillForegroundColor(new XSSFColor(new Color(255,235,156)))
        csNeutral.setFillPattern(FillPatternType.SOLID_FOREGROUND)
        SXSSFWorkbook wb = new SXSSFWorkbook(workbook,50)
        wb.setCompressTempFiles(true)
        CostItemGroup.findAll().each{ cig -> costItemGroups.put(cig.costItem,cig.budgetCode) }
        result.cost_item_tabs.entrySet().each { cit ->
            String sheettitle
            String viewMode = cit.getKey()
            switch(viewMode) {
                case "own": sheettitle = message(code:'financials.header.ownCosts')
                break
                case "cons": sheettitle = message(code:'financials.header.consortialCosts')
                break
                case "subscr": sheettitle = message(code:'financials.header.subscriptionCosts')
                break
            }
            SXSSFSheet sheet = wb.createSheet(sheettitle)
            sheet.flushRows(10)
            sheet.setAutobreaks(true)
            Row headerRow = sheet.createRow(0)
            headerRow.setHeightInPoints(16.75f)
            ArrayList titles = [message(code: 'sidewide.number')]
            if(viewMode == "cons")
                titles.addAll([message(code:'org.sortName.label'),message(code:'financials.newCosts.costParticipants'),message(code:'financials.isVisibleForSubscriber')])
            titles.add(message(code: 'financials.newCosts.costTitle'))
            if(viewMode == "cons")
                titles.add(message(code:'default.provider.label'))
            titles.addAll([message(code: 'financials.forSubscription'), message(code:'subscription.startDate.label'), message(code: 'subscription.endDate.label'),
                           message(code: 'financials.costItemConfiguration'), message(code: 'package'), message(code: 'issueEntitlement.label'),
                           message(code: 'financials.datePaid'), message(code: 'financials.dateFrom'), message(code: 'financials.dateTo'),
                           message(code: 'financials.costItemStatus'), message(code: 'financials.billingCurrency'), message(code: 'financials.costInBillingCurrency'),"EUR",
                           message(code: 'financials.costInLocalCurrency')])
            if(["own","cons"].indexOf(viewMode) > -1)
                titles.addAll([message(code: 'financials.taxRate'), message(code:'financials.billingCurrency'),message(code: 'financials.costInBillingCurrencyAfterTax'),"EUR",message(code: 'financials.costInLocalCurrencyAfterTax')])
            titles.addAll([message(code: 'financials.costItemElement'),message(code: 'financials.newCosts.description'),
                           message(code: 'financials.newCosts.constsReferenceOn'), message(code: 'financials.budgetCode'),
                           message(code: 'financials.invoice_number'), message(code: 'financials.order_number')])
            titles.eachWithIndex { titleName, int i ->
                Cell cell = headerRow.createCell(i)
                cell.setCellValue(titleName)
            }
            sheet.createFreezePane(0, 1)
            Row row
            Cell cell
            int rownum = 1
            int sumcell = -1
            int sumcellAfterTax = -1
            int sumTitleCell = -1
            int sumCurrencyCell = -1
            int sumCurrencyAfterTaxCell = -1
            HashSet<String> currencies = new HashSet<String>()
            if(cit.getValue().count > 0) {
                cit.getValue().costItems.each { ci ->
                    BudgetCode codes = costItemGroups.get(ci)
                    String start_date   = ci.startDate ? dateFormat.format(ci?.startDate) : ''
                    String end_date     = ci.endDate ? dateFormat.format(ci?.endDate) : ''
                    String paid_date    = ci.datePaid ? dateFormat.format(ci?.datePaid) : ''
                    int cellnum = 0
                    row = sheet.createRow(rownum)
                    //sidewide number
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(rownum)
                    if(viewMode == "cons") {
                        if(ci.sub) {
                            List<Org> orgRoles = subscribers.get(ci.sub)
                            //participants (visible?)
                            Cell cellA = row.createCell(cellnum++)
                            Cell cellB = row.createCell(cellnum++)
                            String cellValueA = ""
                            String cellValueB = ""
                            orgRoles.each { or ->
                                cellValueA += or.sortname
                                cellValueB += or.name
                            }
                            cellA.setCellValue(cellValueA)
                            cellB.setCellValue(cellValueB)
                            cell = row.createCell(cellnum++)
                            cell.setCellValue(ci.isVisibleForSubscriber ? message(code:'financials.isVisibleForSubscriber') : "")
                        }
                    }
                    //cost title
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci.costTitle ?: '')
                    if(viewMode == "cons") {
                        //provider
                        cell = row.createCell(cellnum++)
                        if(ci.sub) {
                            List<Org> orgRoles = providers.get(ci.sub)
                            String cellValue = ""
                            orgRoles.each { or ->
                                cellValue += or.name
                            }
                            cell.setCellValue(cellValue)
                        }
                        else cell.setCellValue("")
                    }
                    //cell.setCellValue(ci.sub ? ci.sub"")
                    //subscription
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci.sub ? ci.sub.name : "")
                    //dates from-to
                    Cell fromCell = row.createCell(cellnum++)
                    Cell toCell = row.createCell(cellnum++)
                    if(ci.sub) {
                        if(ci.sub.startDate)
                            fromCell.setCellValue(dateFormat.format(ci.sub.startDate))
                        else
                            fromCell.setCellValue("")
                        if(ci.sub.endDate)
                            toCell.setCellValue(dateFormat.format(ci.sub.endDate))
                        else
                            toCell.setCellValue("")
                    }
                    //cost sign
                    cell = row.createCell(cellnum++)
                    if(ci.costItemElementConfiguration) {
                        cell.setCellValue(ci.costItemElementConfiguration.getI10n("value"))
                    }
                    else
                        cell.setCellValue(message(code:'financials.costItemConfiguration.notSet'))
                    //subscription package
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.subPkg ? ci.subPkg.pkg.name:'')
                    //issue entitlement
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.issueEntitlement ? ci.issueEntitlement?.tipp?.title?.title:'')
                    //date paid
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(paid_date ?: '')
                    //date from
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(start_date ?: '')
                    //date to
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(end_date ?: '')
                    /*cost item category
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costItemCategory ? ci.costItemCategory.value:'')
                    */
                    //for the sum title
                    sumTitleCell = cellnum
                    //cost item status
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costItemStatus ? ci.costItemStatus.getI10n("value"):'')
                    if(["own","cons"].indexOf(viewMode) > -1) {
                        //billing currency and value
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(ci?.billingCurrency ? ci.billingCurrency.value : '')
                        sumCurrencyCell = cellnum
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(ci?.costInBillingCurrency ? ci.costInBillingCurrency : 0.0)
                        if(ci.costItemElementConfiguration) {
                            switch(ci.costItemElementConfiguration) {
                                case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
                                break
                                case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
                                break
                                case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
                                break
                            }
                        }
                        //local currency and value
                        cell = row.createCell(cellnum++)
                        cell.setCellValue("EUR")
                        sumcell = cellnum
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(ci?.costInLocalCurrency ? ci.costInLocalCurrency : 0.0)
                        if(ci.costItemElementConfiguration) {
                            switch(ci.costItemElementConfiguration) {
                                case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
                                break
                                case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
                                break
                                case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
                                break
                            }
                        }
                        //tax rate
                        cell = row.createCell(cellnum++)
                        cell.setCellValue("${ci.taxRate ?: 0} %")
                    }
                    //billing currency and value
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.billingCurrency ? ci.billingCurrency.value : '')
                    sumCurrencyAfterTaxCell = cellnum
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costInBillingCurrencyAfterTax ? ci.costInBillingCurrencyAfterTax : 0.0)
                    if(ci.costItemElementConfiguration) {
                        switch(ci.costItemElementConfiguration) {
                            case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
                            break
                            case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
                            break
                            case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
                            break
                        }
                    }
                    //local currency and value
                    cell = row.createCell(cellnum++)
                    cell.setCellValue("EUR")
                    sumcellAfterTax = cellnum
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costInLocalCurrencyAfterTax ? ci.costInLocalCurrencyAfterTax : 0.0)
                    if(ci.costItemElementConfiguration) {
                        switch(ci.costItemElementConfiguration) {
                            case RDStore.CIEC_POSITIVE: cell.setCellStyle(csPositive)
                            break
                            case RDStore.CIEC_NEGATIVE: cell.setCellStyle(csNegative)
                            break
                            case RDStore.CIEC_NEUTRAL: cell.setCellStyle(csNeutral)
                            break
                        }
                    }
                    //cost item element
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costItemElement?ci.costItemElement.getI10n("value") : '')
                    //cost item description
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costDescription?: '')
                    //reference
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.reference?:'')
                    //budget codes
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(codes ? codes.value : '')
                    //invoice number
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.invoice ? ci.invoice.invoiceNumber : "")
                    //order number
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.order ? ci.order.orderNumber : "")
                    rownum++
                }
                rownum++
                sheet.createRow(rownum)
                Row sumRow = sheet.createRow(rownum)
                cell = sumRow.createCell(sumTitleCell)
                cell.setCellValue(message(code:'financials.export.sums'))
                if(sumcell > 0) {
                    cell = sumRow.createCell(sumcell)
                    cell.setCellValue(cit.getValue().sums.localSums.localSum)
                }
                cell = sumRow.createCell(sumcellAfterTax)
                cell.setCellValue(cit.getValue().sums.localSums.localSumAfterTax)
                rownum++
                cit.getValue().sums.billingSums.each { entry ->
                    sumRow = sheet.createRow(rownum)
                    cell = sumRow.createCell(sumTitleCell)
                    cell.setCellValue(entry.currency)
                    if(sumCurrencyCell > 0) {
                        cell = sumRow.createCell(sumCurrencyCell)
                        cell.setCellValue(entry.billingSum)
                    }
                    cell = sumRow.createCell(sumCurrencyAfterTaxCell)
                    cell.setCellValue(entry.billingSumAfterTax)
                    rownum++
                }
            }
            else {
                row = sheet.createRow(rownum)
                cell = row.createCell(0)
                cell.setCellValue(message(code:"finance.export.empty"))
            }

            for(int i = 0; i < titles.size(); i++) {
                try {
                    sheet.autoSizeColumn(i)
                }
                catch(NullPointerException e) {
                    log.error("Null value in column ${i}")
                }
            }
        }

        wb
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def editCostItem() {
        def result = [:]
        def costItemElementConfigurations = []
        def orgConfigurations = []
        result.tab = params.tab

        if(!params.sub.isEmpty() && StringUtils.isNumeric(params.sub))
            result.sub = Subscription.get(Long.parseLong(params.sub))
        result.costItem = CostItem.findById(params.id)
        //format for dropdown: (o)id:value
        def ciecs = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Cost configuration'))
        ciecs.each { ciec ->
            costItemElementConfigurations.add([id:ciec.class.name+":"+ciec.id,value:ciec.getI10n('value')])
        }
        def orgConf = CostItemElementConfiguration.findAllByForOrganisation(contextService.org)
        orgConf.each { oc ->
            orgConfigurations.add([id:oc.costItemElement.id,value:oc.elementSign.class.name+":"+oc.elementSign.id])
        }

        result.costItemElementConfigurations = costItemElementConfigurations
        result.orgConfigurations = orgConfigurations
        result.formUrl = g.createLink(controller:'finance', action:'newCostItem', params:[tab:result.tab,mode:"edit"])
        result.mode = "edit"
        render(template: "/finance/ajaxModal", model: result)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def copyCostItem() {
        def result = [:]

        result.id = params.id

        result.tab = params.tab
        if(params.sub != null && StringUtils.isNumeric(params.sub)) {
            result.sub = Subscription.get(Long.parseLong(params.sub))
        }

        def ci = CostItem.findById(params.id)
        result.costItem = ci
        List costItemElementConfigurations = []
        List orgConfigurations = []
        //format for dropdown: (o)id:value
        def ciecs = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Cost configuration'))
        ciecs.each { ciec ->
            costItemElementConfigurations.add([id:ciec.class.name+":"+ciec.id,value:ciec.getI10n('value')])
        }
        def orgConf = CostItemElementConfiguration.findAllByForOrganisation(contextService.org)
        orgConf.each { oc ->
            orgConfigurations.add([id:oc.costItemElement.id,value:oc.elementSign.class.name+":"+oc.elementSign.id])
        }
        result.costItemElementConfigurations = costItemElementConfigurations
        result.orgConfigurations = orgConfigurations
        result.formUrl = g.createLink(controller:"finance",action:"newCostItem",params:[tab:result.tab,mode:"copy"])
        result.mode = "copy"
        if(result.sub.getConsortia()?.id != contextService.org.id)
            result.consCostTransfer = true
        render(template: "/finance/ajaxModal", model: result)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def deleteCostItem() {
        def result = [:]

        def user = User.get(springSecurityService.principal.id)
        def institution = contextService.getOrg()
        if (!accessService.checkMinUserOrgRole(user,institution,"INST_EDITOR")) {
            response.sendError(403)
            return
        }

        def ci = CostItem.findByIdAndOwner(params.id, institution)
        if (ci) {
            def cigs = CostItemGroup.findAllByCostItem(ci)

            cigs.each { item ->
                item.delete()
                log.debug("deleting CostItemGroup: " + item)
            }
            log.debug("deleting CostItem: " + ci)
            ci.delete()
        }
        //redirect(controller: 'myInstitution', action: 'finance')

        result.tab = params.tab

        redirect(uri: request.getHeader('referer').replaceAll('(#|\\?).*', ''), params: [tab: result.tab])
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def newCostItem() {

        def dateFormat      = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        def result =  [showView:params.tab]
        CostItem newCostItem = null

      try {
        log.debug("FinanceController::newCostItem() ${params}");

        result.institution  =  contextService.getOrg()
        def user            =  User.get(springSecurityService.principal.id)
        result.error        =  [] as List

        if (!accessService.checkMinUserOrgRole(user,result.institution,"INST_EDITOR"))
        {
            result.error=message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A'])
            response.sendError(403)
        }

        def order = null
        if (params.newOrderNumber)
            order = Order.findByOrderNumberAndOwner(params.newOrderNumber, result.institution) ?: new Order(orderNumber: params.newOrderNumber, owner: result.institution).save(flush: true);

        def invoice = null
        if (params.newInvoiceNumber)
            invoice = Invoice.findByInvoiceNumberAndOwner(params.newInvoiceNumber, result.institution) ?: new Invoice(invoiceNumber: params.newInvoiceNumber, owner: result.institution).save(flush: true);

        def subsToDo = []
        if (params.newSubscription?.contains("com.k_int.kbplus.Subscription:"))
        {
            try {
                subsToDo << genericOIDService.resolveOID(params.newSubscription)
            } catch (Exception e) {
                log.error("Non-valid subscription sent ${params.newSubscription}",e)
            }
        }

          switch (params.newLicenseeTarget) {

              case 'com.k_int.kbplus.Subscription:forConsortia':
                  // keep current
                  break
              case 'com.k_int.kbplus.Subscription:forAllSubscribers':
                  // iterate over members
                  subsToDo = Subscription.findAllByInstanceOfAndStatusNotEqual(
                          genericOIDService.resolveOID(params.newSubscription),
                          RefdataValue.getByValueAndCategory('Deleted', 'Subscription Status')
                  )
                  break
              default:
                  if (params.newLicenseeTarget) {
                      subsToDo = genericOIDService.resolveOID(params.newLicenseeTarget)
                  }
                  break
          }

        def pkg = null;
        if (params.newPackage?.contains("com.k_int.kbplus.SubscriptionPackage:"))
        {
            try {
                if (params.newPackage.split(":")[1] != 'null') {
                    pkg = SubscriptionPackage.load(params.newPackage.split(":")[1])
                }
            } catch (Exception e) {
                log.error("Non-valid sub-package sent ${params.newPackage}",e)
            }
        }

        Closure newDate = { param, format ->
            Date date
            try {
                date = dateFormat.parse(param)
            } catch (Exception e) {
                log.debug("Unable to parse date : ${param} in format ${format}")
            }
            date
        }

        def datePaid    = newDate(params.newDatePaid,  dateFormat.toPattern())
        def startDate   = newDate(params.newStartDate, dateFormat.toPattern())
        def endDate     = newDate(params.newEndDate,   dateFormat.toPattern())
        def invoiceDate = newDate(params.newInvoiceDate,    dateFormat.toPattern())
        Year financialYear = params.newFinancialYear ? Year.parse(params.newFinancialYear) : null

        def ie = null
        if(params.newIE)
        {
            try {
                ie = IssueEntitlement.load(params.newIE.split(":")[1])
            } catch (Exception e) {
                log.error("Non-valid IssueEntitlement sent ${params.newIE}",e)
            }
        }

        def billing_currency = null
        if (params.long('newCostCurrency')) //GBP,etc
        {
            billing_currency = RefdataValue.get(params.newCostCurrency)
            if (! billing_currency)
                billing_currency = defaultCurrency
        }

        //def tempCurrencyVal       = params.newCostCurrencyRate?      params.double('newCostCurrencyRate',1.00) : 1.00//def cost_local_currency   = params.newCostInLocalCurrency?   params.double('newCostInLocalCurrency', cost_billing_currency * tempCurrencyVal) : 0.00
          def cost_item_status      = params.newCostItemStatus ?       (RefdataValue.get(params.long('newCostItemStatus'))) : null;    //estimate, commitment, etc
          def cost_item_element     = params.newCostItemElement ?      (RefdataValue.get(params.long('newCostItemElement'))): null    //admin fee, platform, etc
          //moved to TAX_TYPES
          //def cost_tax_type         = params.newCostTaxType ?          (RefdataValue.get(params.long('newCostTaxType'))) : null           //on invoice, self declared, etc

          def cost_item_category    = params.newCostItemCategory ?     (RefdataValue.get(params.long('newCostItemCategory'))): null  //price, bank charge, etc

          NumberFormat format = NumberFormat.getInstance(LocaleContextHolder.getLocale())
          def cost_billing_currency = params.newCostInBillingCurrency? format.parse(params.newCostInBillingCurrency).doubleValue() : 0.00
          def cost_currency_rate    = params.newCostCurrencyRate?      params.double('newCostCurrencyRate', 1.00) : 1.00
          def cost_local_currency   = params.newCostInLocalCurrency?   format.parse(params.newCostInLocalCurrency).doubleValue() : 0.00

          def cost_billing_currency_after_tax   = params.newCostInBillingCurrencyAfterTax ? format.parse(params.newCostInBillingCurrencyAfterTax).doubleValue() : cost_billing_currency
          def cost_local_currency_after_tax     = params.newCostInLocalCurrencyAfterTax ? format.parse(params.newCostInLocalCurrencyAfterTax).doubleValue() : cost_local_currency
          //moved to TAX_TYPES
          //def new_tax_rate                      = params.newTaxRate ? params.int( 'newTaxRate' ) : 0
          def tax_key = null
          if(!params.newTaxRate.contains("null")) {
              String[] newTaxRate = params.newTaxRate.split("§")
              RefdataValue taxType = genericOIDService.resolveOID(newTaxRate[0])
              int taxRate = Integer.parseInt(newTaxRate[1])
              switch(taxType.id) {
                  case RefdataValue.getByValueAndCategory("taxable","TaxType").id:
                      switch(taxRate) {
                          case 7: tax_key = CostItem.TAX_TYPES.TAXABLE_7
                              break
                          case 19: tax_key = CostItem.TAX_TYPES.TAXABLE_19
                              break
                      }
                      break
                  case RefdataValue.getByValueAndCategory("taxable tax-exempt","TaxType").id:
                      tax_key = CostItem.TAX_TYPES.TAX_EXEMPT
                      break
                  case RefdataValue.getByValueAndCategory("not taxable","TaxType").id:
                      tax_key = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                      break
                  case RefdataValue.getByValueAndCategory("not applicable","TaxType").id:
                      tax_key = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                      break
              }
          }
          def cost_item_element_configuration   = params.ciec ? genericOIDService.resolveOID(params.ciec) : null

          def cost_item_isVisibleForSubscriber = (params.newIsVisibleForSubscriber ? (RefdataValue.get(params.newIsVisibleForSubscriber)?.value == 'Yes') : false)

          if (! subsToDo) {
              subsToDo << null // Fallback for editing cost items via myInstitution/finance // TODO: ugly
          }
          subsToDo.each { sub ->

              List<CostItem> copiedCostItems = []

              if (params.oldCostItem && genericOIDService.resolveOID(params.oldCostItem)) {
                  newCostItem = (CostItem) genericOIDService.resolveOID(params.oldCostItem)
                  //get copied cost items
                  copiedCostItems = CostItem.findAllByCopyBase(newCostItem)
              }
              else {
                  newCostItem = new CostItem()
              }

              newCostItem.owner = result.institution
              newCostItem.sub = sub
              newCostItem.subPkg = SubscriptionPackage.findBySubscriptionAndPkg(sub,pkg?.pkg) ?: null
              newCostItem.issueEntitlement = IssueEntitlement.findBySubscriptionAndTipp(sub,ie?.tipp) ?: null
              newCostItem.order = order
              newCostItem.invoice = invoice
              //continue here: test, if visibility is set to false, check visibility settings of other consortial subscriptions, check then the financial data query whether the costs will be displayed or not!
              newCostItem.isVisibleForSubscriber = sub.administrative ? false : cost_item_isVisibleForSubscriber
              newCostItem.costItemCategory = cost_item_category
              newCostItem.costItemElement = cost_item_element
              newCostItem.costItemStatus = cost_item_status
              newCostItem.billingCurrency = billing_currency //Not specified default to GDP
              //newCostItem.taxCode = cost_tax_type -> to taxKey
              newCostItem.costDescription = params.newDescription ? params.newDescription.trim() : null
              newCostItem.costTitle = params.newCostTitle ?: null
              newCostItem.costInBillingCurrency = cost_billing_currency as Double
              newCostItem.costInLocalCurrency = cost_local_currency as Double

              newCostItem.finalCostRounding = params.newFinalCostRounding ? true : false
              newCostItem.costInBillingCurrencyAfterTax = cost_billing_currency_after_tax as Double
              newCostItem.costInLocalCurrencyAfterTax = cost_local_currency_after_tax as Double
              newCostItem.currencyRate = cost_currency_rate as Double
              //newCostItem.taxRate = new_tax_rate as Integer -> to taxKey
              newCostItem.taxKey = tax_key
              newCostItem.costItemElementConfiguration = cost_item_element_configuration

              newCostItem.datePaid = datePaid
              newCostItem.startDate = startDate
              newCostItem.endDate = endDate
              newCostItem.invoiceDate = invoiceDate
              newCostItem.financialYear = financialYear
              newCostItem.copyBase = params.copyBase ? genericOIDService.resolveOID(params.copyBase) : null

              newCostItem.includeInSubscription = null //todo Discussion needed, nobody is quite sure of the functionality behind this...
              newCostItem.reference = params.newReference ? params.newReference.trim() : null


              if (! newCostItem.validate())
              {
                  result.error = newCostItem.errors.allErrors.collect {
                      log.error("Field: ${it.properties.field}, user input: ${it.properties.rejectedValue}, Reason! ${it.properties.code}")
                      message(code:'finance.addNew.error', args:[it.properties.field])
                  }
              }
              else
              {
                  if (newCostItem.save(flush: true)) {
                      def newBcObjs = []

                      params.list('newBudgetCodes')?.each { newbc ->
                          def bc = genericOIDService.resolveOID(newbc)
                          if (bc) {
                              newBcObjs << bc
                              if (! CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )) {
                                  new CostItemGroup(costItem: newCostItem, budgetCode: bc).save(flush: true)
                              }
                          }
                      }

                      def toDelete = newCostItem.getBudgetcodes().minus(newBcObjs)
                      toDelete.each{ bc ->
                          def cig = CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )
                          if (cig) {
                              log.debug('deleting ' + cig)
                              cig.delete()
                          }
                      }

                      //notify cost items copied from this cost item
                      copiedCostItems.each { cci ->
                          List diffs = []
                          String costTitle = cci.costTitle ?: ''
                          if(newCostItem.costInBillingCurrencyAfterTax != cci.costInBillingCurrency) {
                              diffs.add(message(code:'pendingChange.message_CI01',args:[costTitle,g.createLink(mapping:'subfinance',controller:'subscription',action:'index',params:[sub:cci.sub.id]),cci.sub.name,cci.costInBillingCurrency,newCostItem.costInBillingCurrencyAfterTax]))
                          }
                          if(newCostItem.costInLocalCurrencyAfterTax != cci.costInLocalCurrency) {
                              diffs.add(message(code:'pendingChange.message_CI02',args:[costTitle,g.createLink(mapping:'subfinance',controller:'subscription',action:'index',params:[sub:cci.sub.id]),cci.sub.name,cci.costInLocalCurrency,newCostItem.costInLocalCurrencyAfterTax]))
                          }
                          diffs.each { diff ->
                              PendingChange change = new PendingChange(costItem: cci, owner: cci.owner,desc: diff, ts: new Date())
                              if(!change.save(flush: true))
                                  log.error(change.errors)
                              //continue here: a) remove pending change again if a button has been clicked or after a certain time, b) check if everything works
                          }
                      }

                  } else {
                      result.error = "Unable to save!"
                  }
              }
          } // subsToDo.each



      }
      catch ( Exception e ) {
        log.error("Problem in add cost item", e);
      }

      params.remove("Add")
      // render ([newCostItem:newCostItem.id, error:result.error]) as JSON


        redirect(uri: request.getHeader('referer').replaceAll('(#|\\?).*', ''), params: [view: result.showView])
    }

    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = { ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR") })
    def acknowledgeChange() {
        PendingChange changeAccepted = PendingChange.get(params.id)
        if(changeAccepted)
            changeAccepted.delete()
        redirect(uri:request.getHeader('referer'))
    }

    @Deprecated
    private def createBudgetCodes(CostItem costItem, String budgetcodes, Org budgetOwner)
    {
        def result = []
        if(budgetcodes && budgetOwner && costItem) {
            budgetcodes.split(",").each { c ->
                def bc = null
                if (c.startsWith("-1")) { //New code option from select2 UI
                    bc = new BudgetCode(owner: budgetOwner, value: c.substring(2)).save(flush: true)
                }
                else {
                    bc = BudgetCode.get(c)
                }

                if (bc != null) {
                    // WORKAROUND ERMS-337: only support ONE budgetcode per costitem
                    def existing = CostItemGroup.executeQuery(
                            "SELECT DISTINCT cig.id FROM CostItemGroup AS cig JOIN cig.costItem AS ci JOIN cig.budgetCode AS bc WHERE ci = ? AND bc.owner = ? AND bc != ?",
                            [costItem, budgetOwner, bc] );
                    existing.each { id ->
                        CostItemGroup.get(id).delete()
                    }
                    if (! CostItemGroup.findByCostItemAndBudgetCode(costItem, bc)) {
                        result.add(new CostItemGroup(costItem: costItem, budgetCode: bc).save(flush: true))
                    }
                }
            }
        }

        result
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def getRecentCostItems() {
        def dateTimeFormat     = new java.text.SimpleDateFormat(message(code:'default.date.format')) {{setLenient(false)}}
        def  institution       = contextService.getOrg()
        def  result            = [:]
        def  recentParams      = [max:10, order:'desc', sort:'lastUpdated']
        result.to              = new Date()
        result.from            = params.from? dateTimeFormat.parse(params.from): new Date()
        result.recentlyUpdated = CostItem.findAllByOwnerAndLastUpdatedBetween(institution,result.from,result.to,recentParams)
        result.from            = dateTimeFormat.format(result.from)
        result.to              = dateTimeFormat.format(result.to)
        log.debug("FinanceController - getRecentCostItems, rendering template with model: ${result}")

        render(template: "/finance/recentlyAddedModal", model: result)
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def newCostItemsPresent() {
        def institution = contextService.getOrg()
        def dateTimeFormat  = new java.text.SimpleDateFormat(message(code:'default.date.format')) {{setLenient(false)}}
        Date dateTo

        try {
            dateTo = dateTimeFormat.parse(params.to)
        } catch(Exception e) {
            dateTo = new Date()
        }
        int counter     = CostItem.countByOwnerAndLastUpdatedGreaterThan(institution, dateTo)

        def builder = new groovy.json.JsonBuilder()
        def root    = builder {
            count counter
            to dateTimeFormat.format(dateTo)
        }
        log.debug("Finance - newCostItemsPresent ? params: ${params} JSON output: ${builder.toString()}")
        render(text: builder.toString(), contentType: "text/json", encoding: "UTF-8")
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def delete() {
        log.debug("FinanceController::delete() ${params}");

        def results        =  [:]
        results.successful =  []
        results.failures   =  []
        results.message    =  null
        results.sentIDs    =  JSON.parse(params.del) //comma seperated list
        def user           =  User.get(springSecurityService.principal.id)
        def institution    =  contextService.getOrg()
        if (!accessService.checkMinUserOrgRole(user,institution,"INST_EDITOR"))
        {
            response.sendError(403)
            return
        }

        if (results.sentIDs && institution)
        {
            def _costItem = null
            def _props

            results.sentIDs.each { id ->
                _costItem = CostItem.findByIdAndOwner(id,institution)
                if (_costItem)
                {
                    try {
                        _props = _costItem.properties
                        CostItemGroup.deleteAll(CostItemGroup.findAllByCostItem(_costItem))
                        // TODO delete BudgetCode
                        _costItem.delete(flush: true)
                        results.successful.add(id)
                        log.debug("User: ${user.username} deleted cost item with properties ${_props}")
                    } catch (Exception e)
                    {
                        log.error("FinanceController::delete() : Delete Exception",e)
                        results.failures.add(id)
                    }
                }
                else
                    results.failures.add(id)
            }

            if (results.successful.size() > 0 && results.failures.isEmpty())
                results.message = "All ${results.successful.size()} Cost Items completed successfully : ${results.successful}"
            else if (results.successful.isEmpty() && results.failures.size() > 0)
                results.message = "All ${results.failures.size()} failed, unable to delete, have they been deleted already? : ${results.failures}"
            else
                results.message = "Success completed ${results.successful.size()} out of ${results.sentIDs.size()}  Failures as follows : ${results.failures}"

        } else
            results.message = "Incorrect parameters sent, not able to process the following : ${results.sentIDs.size()==0? 'Empty, no IDs present' : results.sentIDs}"

        render results as JSON
    }


    @Secured(['ROLE_USER'])
    def financialRef() {
        log.debug("Financials :: financialRef - Params: ${params}")

        def result      = [:]
        result.error    = [] as List
        def institution = Org.get(params.orgId)
        def owner       = refData(params.owner)
        log.debug("Financials :: financialRef - Owner instance returned: ${owner.obj}")

        //check in reset mode e.g. subscription changed, meaning IE & SubPkg will have to be reset
        boolean resetMode = params.boolean('resetMode',false) && params.fields
        boolean wasReset  = false


        if (owner) {
            if (resetMode)
                wasReset = refReset(owner,params.fields,result)


            if (resetMode && !wasReset)
                log.debug("Field(s): ${params.fields} should have been reset as relation: ${params.relation} has been changed")
            else {
                //continue happy path...
                def relation = refData(params.relation)
                log.debug("Financials :: financialRef - relation obj or stub returned " + relation)

                if (relation) {
                    log.debug("Financials :: financialRef - Relation needs creating: " + relation.create)
                    if (relation.create) {
                        if (relation.obj.hasProperty(params.relationField)) {
                            relation.obj."${params.relationField}" = params.val
                            relation.obj.owner = institution
                            log.debug("Financials :: financialRef -Creating Relation val:${params.val} field:${params.relationField} org:${institution.name}")
                            if (relation.obj.save())
                                log.debug("Financials :: financialRef - Saved the new relational inst ${relation.obj}")
                            else
                                result.error.add([status: "FAILED: Creating ${params.ownerField}", msg: "Invalid data received to retrieve from DB"])
                        } else
                            result.error.add([status: "FAILED: Setting value", msg: "The data you are trying to set does not exist"])
                    }

                    if (owner.obj.hasProperty(params.ownerField)) {
                        log.debug("Using owner instance field of ${params.ownerField} to set new instance of ${relation.obj.class} with ID ${relation.obj.id}")
                        owner.obj."${params.ownerField}" = relation.obj
                        result.relation = ['class':relation.obj.id, 'id':relation.obj.id] //avoid excess data leakage
                    }
                } else
                    result.error.add([status: "FAILED: Related Cost Item Data", msg: "Invalid data received to retrieve from DB"])
            }
        } else
            result.error.add([status: "FAILED: Cost Item", msg: "Invalid data received to retrieve from DB"])



        render result as JSON
    }

    /**
     *
     * @param costItem - The owner instance passed from financialRef
     * @param fields - comma seperated list of fields to reset, has to be in allowed list (see below)
     * @param result - LinkedHashMap from financialRef
     * @return
     */
    def private refReset(costItem, String fields, result) {
        log.debug("Attempting to reset a reference for cost item data ${costItem} for field(s) ${fields}")
        def wasResetCounter = 0
        def f               = fields?.split(',')
        def allowed         = ["sub", "issueEntitlement", "subPkg", "invoice", "order"]
        boolean validFields = false

        if (f)
        {
            validFields = f.every { allowed.contains(it) && costItem.obj.hasProperty(it) }

            if (validFields)
                f.each { field ->
                    costItem.obj."${field}" = null
                    if (costItem.obj."${field}" == null)
                        wasResetCounter++
                    else
                        result.error.add([status: "FAILED: Cost Item", msg: "Problem resetting data for field ${field}"])
                }
            else
                result.error.add([status: "FAILED: Cost Item", msg: "Problem resetting data, invalid fields received"])
        }
        else
            result.error.add([status: "FAILED: Cost Item", msg: "Invalid data received"])

        return validFields && wasResetCounter == f.size()
    }

    def private refData(String oid) {
        def result         = [:]
        result.create      = false
        def oid_components = oid.split(':');
        def dynamic_class  = grailsApplication.getArtefact('Domain',oid_components[0]).getClazz()
        if ( dynamic_class)
        {
            if (oid_components[1].equals("create"))
            {
                result.obj    = dynamic_class.newInstance()
                result.create = true
            }
            else
                result.obj = dynamic_class.get(oid_components[1])
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def removeBC() {
        log.debug("Financials :: remove budget code - Params: ${params}")
        def result      = [:]
        result.success  = [status:  "Success: Deleted code", msg: "Deleted instance of the budget code for the specified cost item"]
        def user        = User.get(springSecurityService.principal.id)
        def institution = contextService.getOrg()

        if (!user.getAuthorizedOrgs().id.contains(institution.id))
        {
            log.error("User ${user.id} has tried to delete budget code information for Org not privy to ${institution.name}")
            response.sendError(403)
            return
        }
        def ids = params.bcci ? params.bcci.split("_")[1..2] : null
        if (ids && ids.size()==2)
        {
            def bc  = BudgetCode.get(ids[0])
            def ci  = CostItem.get(ids[1])
            def cig = CostItemGroup.findByBudgetCodeAndCostItem(bc, ci)

            if (bc && ci && cig)
            {
                if (cig.costItem == ci) {
                    cig.delete(flush: true)
                    // TODO delete budgetcode
                }
                else {
                    result.error = [status: "FAILED: Deleting budget code", msg: "Budget code is not linked with the cost item"]
                }
            }
        } else
            result.error = [status: "FAILED: Deleting budget code", msg: "Incorrect parameter information sent"]

        render result as JSON
    }

    @Deprecated
    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def createCode() {
        def result      = [:]
        def user        = springSecurityService.currentUser
        def institution = contextService.getOrg()

        if (! userCertified(user, institution))
        {
            response.sendError(403)
            return
        }
        def code  = params.code?.trim()
        def ci    = CostItem.findByIdAndOwner(params.id, institution)

        if (code && ci)
        {
            def cig_codes = createBudgetCodes(ci, code, institution)
            if (cig_codes.isEmpty())
                result.error = "Unable to create budget code(s): ${code}"
            else
            {
                result.success = "${cig_codes.size()} new code(s) added to cost item"
                result.codes   = cig_codes.collect {
                    "<span class='budgetCode'>${it.budgetCode.value}</span><a id='bcci_${it.id}_${it.costItem.id}' class='badge budgetCode'>x</a>"
                }
            }
        } else
            result.error = "Invalid data received for code creation"

        render result as JSON
    }

    //ex SubscriptionDetailsController
    private Map setResultGenerics() {
        LinkedHashMap result = [:]
        result.user         = User.get(springSecurityService.principal.id)
        result.subscription = Subscription.get(params.sub)
        if(params.sub)
            result.editable = result.subscription.isEditableBy(result.user)
        else
            result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')
        result.institution  = contextService.getOrg()
        result
    }

}
