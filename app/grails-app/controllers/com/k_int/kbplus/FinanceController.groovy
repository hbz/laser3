package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.converters.JSON;
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.hssf.usermodel.HSSFSheet
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Cell
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Secured(['IS_AUTHENTICATED_FULLY'])
class FinanceController extends AbstractDebugController {

    def springSecurityService
    def accessService
    def contextService
    def genericOIDService

    private final def ci_count        = 'select distinct count(ci.id) from CostItem as ci '
    private final def ci_select       = 'select distinct ci from CostItem as ci '
    private final def user_role        = Role.findByAuthority('INST_USER')
    private final def defaultCurrency = RefdataValue.getByValueAndCategory('EUR', 'Currency')

    final static MODE_OWNER          = 'MODE_OWNER'
    final static MODE_CONS           = 'MODE_CONS'
    final static MODE_CONS_AT_SUBSCR = 'MODE_CONS_AT_SUBSCR'
    final static MODE_SUBSCR         = 'MODE_SUBSCR'

    private boolean userCertified(User user, Org institution)
    {
        if (!user.getAuthorizedOrgs().id.contains(institution.id))
        {
            log.error("User ${user.id} trying to access financial Org information not privy to ${institution.name}")
            return false
        } else
            return true
    }

    private boolean isFinanceAuthorised(Org org, User user) {

        accessService.checkMinUserOrgRole(user, org, user_role)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def index() {

        log.debug("FinanceController::index() ${params}");

        def user =  User.get(springSecurityService.principal.id)
        def dateTimeFormat  = new java.text.SimpleDateFormat(message(code:'default.date.format')) {{setLenient(false)}}
        def result = [:]

      try {
        result.contextOrg = contextService.getOrg()
        result.institution = contextService.getOrg()

        if (! isFinanceAuthorised(result.institution, user)) {
            log.error("Sending 401 - forbidden");
            flash.error=message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A'])
            response.sendError(401)
        }

        /*
        this is the switch for page call: if the controller has been called with the param sub (for subscription), then this flag
        is being set.
         */
        result.inSubMode   = params.sub ? true : false

          result.queryMode = MODE_OWNER
          def orgRoleCons, orgRoleSubscr

        //this is the switch for the cost item fill
        if (result.inSubMode) {
            log.info("call from /subscriptionDetails/${params.sub}/finance")
            params.subscriptionFilter = "${params.sub}"

            result.fixedSubscription = params.int('sub')? Subscription.get(params.sub) : null

            result.navPrevSubscription = result.fixedSubscription.previousSubscription ?: null
            result.navNextSubscription = Subscription.findByPreviousSubscription(result.fixedSubscription) ?: null

            if (! result.fixedSubscription) {
                log.error("Financials in FIXED subscription mode, sent incorrect subscription ID: ${params?.sub}")
                response.sendError(400, "No relevant subscription, please report this error to an administrator")
            }

            // own costs
            def tmp = financialData(result, params, user, MODE_OWNER)
            result.foundMatches    = tmp.foundMatches
            result.cost_items      = tmp.cost_items
            result.cost_item_count = tmp.cost_item_count

            orgRoleCons = OrgRole.findBySubAndOrgAndRoleType(
                    result.fixedSubscription,
                    result.institution,
                    RDStore.OR_SUBSCRIPTION_CONSORTIA
            )

            orgRoleSubscr = OrgRole.findBySubAndRoleType(
                    result.fixedSubscription,
                    RDStore.OR_SUBSCRIBER_CONS
            )

            if (orgRoleCons) {
                // show consortial subscription, but member costs
                if (! orgRoleSubscr) {
                    result.queryMode = MODE_CONS
                    tmp = financialData(result, params, user, MODE_CONS)

                    result.foundMatches_CS = tmp.foundMatches

                    result.cost_items_CS = tmp.cost_items.sort{ x, y ->
                        def xx = OrgRole.findBySubAndRoleType(x.sub, RDStore.OR_SUBSCRIBER_CONS)
                        def yy = OrgRole.findBySubAndRoleType(y.sub, RDStore.OR_SUBSCRIBER_CONS)
                        xx?.org?.sortname <=> yy?.org?.sortname
                    }
                    result.cost_item_count_CS = tmp.cost_item_count
                }
                // show member subscription as consortia
                else {
                    result.queryMode = MODE_CONS_AT_SUBSCR
                    tmp = financialData(result, params, user, MODE_OWNER)

                    result.foundMatches_CS = tmp.foundMatches
                    result.cost_items_CS = tmp.cost_items
                    result.cost_item_count_CS = tmp.cost_item_count
                }
            }
            // show subscription as a member, but viewable costs
            else if (orgRoleSubscr) {
                result.queryMode = MODE_SUBSCR
                tmp = financialData(result, params, user, MODE_SUBSCR)

                result.foundMatches_SUBSCR = tmp.foundMatches
                result.cost_items_SUBSCR = tmp.cost_items
                result.cost_item_count_SUBSCR = tmp.cost_item_count
            }
        }
        else {
            log.info("call from /myInstitution/finance")
            def tmp = financialData(result, params, user, MODE_OWNER)
            result.foundMatches    = tmp.foundMatches
            result.cost_items      = tmp.cost_items
            result.cost_item_count = tmp.cost_item_count
            orgRoleCons = OrgRole.findByOrgAndRoleType(
                    result.institution,
                    RDStore.OR_SUBSCRIPTION_CONSORTIA
            )
            orgRoleSubscr = OrgRole.findByRoleType(
                    RDStore.OR_SUBSCRIBER_CONS
            )
            if (orgRoleCons) {
                // show consortial subscription, but member costs
                    result.queryMode = MODE_CONS
                    tmp = financialData(result, params, user, MODE_CONS)

                    result.foundMatches_CS = tmp.foundMatches

                    result.cost_items_CS = tmp.cost_items.sort{ x, y ->
                        def xx = OrgRole.findBySubAndRoleType(x.sub, RDStore.OR_SUBSCRIBER_CONS)
                        def yy = OrgRole.findBySubAndRoleType(y.sub, RDStore.OR_SUBSCRIBER_CONS)
                        xx?.org?.sortname <=> yy?.org?.sortname
                    }
                    result.cost_item_count_CS = tmp.cost_item_count
            }
            // show subscription as a member, but viewable costs
            else if (orgRoleSubscr) {
                result.queryMode = MODE_SUBSCR
                tmp = financialData(result, params, user, MODE_SUBSCR)

                result.foundMatches_SUBSCR = tmp.foundMatches
                result.cost_items_SUBSCR = tmp.cost_items
                result.cost_item_count_SUBSCR = tmp.cost_item_count
            }
        }

            flash.error = null
            flash.message = null

            // TODO: review as of ticket ERMS-761 and ERMS-823
            if (result.foundMatches || result.foundMatches_CS || result.foundMatches_SUBSCR) {
                flash.message = "Die aktuelle Filtereinstellung liefert potentielle Treffer. Ggfs. müssen Sie einzelne Felder noch anpassen."
            }
            else if (params.get('submit')) {
                flash.error = "Keine Treffer. Der Filter wird zurückgesetzt."
            }

          // prepare filter dropdowns
          //def myCostItems = result.fixedSubscription ?
          //          CostItem.findAllWhere(owner: result.institution, sub: result.fixedSubscription)
          //        : CostItem.findAllWhere(owner: result.institution)

          //TODO: Nochmal überdenken
          def myCostItems = CostItem.findAllWhere(owner: result.institution)
          switch (result.queryMode)
          {
              //own costs
              case MODE_OWNER:
                  myCostItems = result.cost_items
                  break
              //consortium viewing the subscription from the point of view of subscriber
              case MODE_CONS_AT_SUBSCR:
                  myCostItems = result.cost_items_SUBSCR
                  break
              //consortium viewing the overall consortium costs
              case MODE_CONS:
                  myCostItems = result.cost_items_CS
                  break
          }

          result.allCIInvoiceNumbers = (myCostItems.collect{ it -> it?.invoice?.invoiceNumber }).findAll{ it }.unique().sort()
          result.allCIOrderNumbers   = (myCostItems.collect{ it -> it?.order?.orderNumber }).findAll{ it }.unique().sort()
          result.allCIBudgetCodes    = (myCostItems.collect{ it -> it?.getBudgetcodes()?.value }).flatten().unique().sort()

          result.allCISPkgs = (myCostItems.collect{ it -> it?.subPkg }).findAll{ it }.unique().sort()
          if(result.queryMode == MODE_CONS)
              result.allCISubs  = (myCostItems.findAll{it?.sub?.status == RefdataValue.getByValueAndCategory('Current','Subscription Status')}.collect{ it -> it?.sub?.instanceOf }).findAll{ it }.unique().sort()
          else
              result.allCISubs  = (myCostItems.findAll{it?.sub?.status == RefdataValue.getByValueAndCategory('Current','Subscription Status')}.collect{ it -> it?.sub }).findAll{ it }.unique().sort()

          result.isXHR = request.isXhr()
        //Other than first run, request will always be AJAX...
        if (result.isXHR) {
            log.debug("XHR Request");
            render (template: "filter", model: result)
        }
        else
        {
            log.debug("HTML Request");
            //First run, make a date for recently updated costs AJAX operation
            use(groovy.time.TimeCategory) {
                result.from = dateTimeFormat.format(new Date() - 3.days)
            }
        }
      }
      catch ( Exception e ) {
        log.error("Error processing index",e);
      }
      finally {
        log.debug("finance::index returning");
      }

      result.tab = 'owner'
      if(params.tab) {
          result.tab = params.tab
      }
      else if(!params.tab) {
          if(result.queryMode == MODE_CONS)
              result.tab = 'sc'
      }

      //result.tab = params.tab ?: result.queryMode == MODE_CONS ? 'sc' : 'owner'

      result
    }

    /**
     * Sets up the financial data parameters and performs the relevant DB search
     * @param result - LinkedHashMap for model data
     * @param params - Qry data sent from view
     * @param user   - Currently logged in user object
     * @return Cost Item count & data / view information for pagination, sorting, etc
     *
     * Note - Requests DB requests are cached ONLY if non-hardcoded values are used
     */
    private def financialData(result, params, user, queryMode) {
        def tmp = [:]

        result.editable    =  accessService.checkMinUserOrgRole(user, result.institution, user_role)
        params.orgId       =  result.institution.id

        request.setAttribute("editable", result.editable) //editable Taglib doesn't pick up AJAX request, REQUIRED!
        result.info        =  [] as List
        //params.max         =  params.max && params.int('max') ? Math.min(params.int('max'),200) : (user?.defaultPageSize? maxAllowedVals.min{(it-user.defaultPageSize).abs()} : 10)
        //result.max         =  params.max
        //result.offset      =  params.int('offset',0)?: 0

        // WORKAROUND: erms-517 (deactivated as erms-802)

        params.max = params.max ? params.max : user.getDefaultPageSizeTMP()
        params.offset = params.offset ? params.offset : 0
        /*
        result.max = 5000
        result.offset = 0
        */
        /*
        result.max = params.max ? Integer.parseInt(params.max) : ;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        */
        //Query setup options, ordering, joins, param query data....
        def order = "id"
        def gspOrder = "Cost Item#"

        result.order = gspOrder
        result.sort =  ["desc","asc"].contains(params.sort)? params.sort : "desc" //defaults to sort & order of desc id

        def cost_item_qry_params =  [owner: result.institution]
        def cost_item_qry        =  " where ci.owner = :owner "
        def orderAndSortBy = " ORDER BY ci.${order} ${result.sort}"

        if (MODE_CONS == queryMode) {
            //ticket ERMS-802: switch for site call - consortia costs should be displayed also when not in fixed subscription mode
            def queryParams = ['roleType':RDStore.OR_SUBSCRIPTION_CONSORTIA, 'activeInst':result.institution, 'status':RefdataValue.getByValueAndCategory('Current','Subscription Status')]
            def memberSubs
            if(result.fixedSubscription)
                memberSubs = Subscription.findAllByInstanceOf(result.fixedSubscription)
            else
                memberSubs = Subscription.executeQuery("select s from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) ) AND ( s.instanceOf is not null AND s.status = :status ) ",queryParams)

            cost_item_qry_params = [subs: memberSubs, owner: result.institution]
            cost_item_qry        = ' WHERE ci.sub IN ( :subs ) AND ci.owner = :owner '
            //orderAndSortBy       = orderAndSortBy
        }

        if (MODE_OWNER == queryMode) {
            if(params.sub) {
                cost_item_qry_params = [sub: result.fixedSubscription, owner: result.institution]
                cost_item_qry        = ' WHERE ci.sub = :sub AND ci.owner = :owner '
                //orderAndSortBy       = orderAndSortBy
            }
            else if(! params.sub){
                //check if active institution has consortial subscriptions
                def orgRoleCheck = OrgRole.countByOrgAndRoleType(result.institution,RDStore.OR_SUBSCRIPTION_CONSORTIA)
                //there are consortial subscriptions for the given institution
                if(orgRoleCheck > 0) {
                    def queryParams = ['activeInst':result.institution, 'roleType': RDStore.OR_SUBSCRIPTION_CONSORTIA, 'consortialSubscription':RefdataValue.getByValueAndCategory('Consortial Licence','Subscription Type'), 'status':RefdataValue.getByValueAndCategory('Current','Subscription Status')]
                    //it may be that the condition whether only not-consortial subscriptions are considered when not as subscription consortia has to be reconsidered! Expect Daniel/Micha about that!
                    def instSubs = Subscription.executeQuery("select s from Subscription as s where ( (exists ( select o from s.orgRelations as o where o.org = :activeInst and o.roleType = :roleType ) AND s.instanceOf IS NULL) OR (exists (select o from s.orgRelations as o where o.org = :activeInst) AND s.type != :consortialSubscription ) ) AND s.status = :status",queryParams)
                    if(instSubs.size() > 0) {
                        cost_item_qry_params = [subs: instSubs, owner: result.institution]
                        cost_item_qry = ' WHERE (ci.sub IS NULL OR ci.sub IN ( :subs )) AND ci.owner = :owner '
                    }
                    else {
                        cost_item_qry_params = [owner: result.institution]
                        cost_item_qry = ' WHERE ci.sub IS NULL AND ci.owner = :owner '
                    }
                }
                //we are without consortial subscriptions e.g. the institution is not a consortium whose cost items are going to be checked
                else {
                    def queryParams = ['activeInst':result.institution, 'status':RefdataValue.getByValueAndCategory('Current','Subscription Status')]
                    def instSubs = Subscription.executeQuery("select s from Subscription as s where exists ( select o from s.orgRelations as o where o.org = :activeInst ) AND s.status = :status",queryParams)
                    if(instSubs.size() > 0) {
                        cost_item_qry_params = [subs: instSubs, owner: result.institution]
                        cost_item_qry = ' WHERE (ci.sub IS NULL OR ci.sub IN ( :subs )) AND ci.owner = :owner '
                    }
                    else {
                        cost_item_qry_params = [owner: result.institution]
                        cost_item_qry = ' WHERE ci.sub IS NULL AND ci.owner = :owner '
                    }
                }
            }
        }

        // OVERWRITE
        if (MODE_SUBSCR == queryMode) {

            if(result.fixedSubscription) {
                // TODO FLAG isVisibleForSubscriber
                cost_item_qry_params =  [sub: result.fixedSubscription, owner: result.institution]
                cost_item_qry        = ' , OrgRole as ogr WHERE ci.sub = :sub AND ogr.org = :owner AND ci.isVisibleForSubscriber is true ' // (join)? "LEFT OUTER JOIN ${join} AS j WHERE ci.owner = :owner " :"  where ci.owner = :owner "
                //orderAndSortBy       = orderAndSortBy
            }
            else if (!result.fixedSubscription) {
                def queryParams = ['activeInst':result.institution, 'status':RefdataValue.getByValueAndCategory('Current','Subscription Status')]
                def instSubs = Subscription.executeQuery("select s from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where o.org = :activeInst ) ) ) AND s.status = :status ",queryParams)
                if(instSubs.size() > 0) {
                  cost_item_qry_params = [subs: instSubs, owner: result.institution]
                  cost_item_qry = ' , OrgRole as ogr WHERE ci.sub IN ( :subs ) AND ogr.org = :owner AND ci.isVisibleForSubscriber is true '
                }
            }

       }

        //Filter processing...

        log.debug("index(${queryMode})  -- Performing filtering processing")
        def qryOutput = filterQuery(result, params, true, queryMode)

        cost_item_qry_params   << qryOutput.fqParams

        //println ci_select + cost_item_qry + qryOutput.qry_string + orderAndSortBy
        //println cost_item_qry_params

        tmp.foundMatches    =  cost_item_qry_params.size() > 1 // [owner:default] ; used for flash
        tmp.cost_items      =  CostItem.executeQuery(ci_select + cost_item_qry + qryOutput.qry_string + orderAndSortBy, cost_item_qry_params, params);
        tmp.cost_item_count =  CostItem.executeQuery(ci_select + cost_item_qry + qryOutput.qry_string + orderAndSortBy, cost_item_qry_params).size()

        log.debug("index(${queryMode})  -- Performed filtering process ${tmp.cost_item_count} result(s) found")

        tmp
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def financialsExport()  {
        log.debug("Financial Export :: ${params}")

            def result = [:]
            result.institution =  contextService.getOrg()
            result.fixedSubscription = params.int('sub')? Subscription.get(params.sub) : null
            def user           =  User.get(springSecurityService.principal.id)

            if (!isFinanceAuthorised(result.institution, user)) {
                flash.error=message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A'])
                response.sendError(403)
                return
            }
            //may kill the server, but I must override the pagination ... is very ugly! And hotfix!
            params.max = 5000
            params.offset = 0


            // I need the consortial data as well ...
            def orgRoleCons = OrgRole.findByOrgAndRoleType(
                    result.institution,
                    RDStore.OR_SUBSCRIPTION_CONSORTIA
            )
            def orgRoleSubscr = OrgRole.findByRoleType(
                    RDStore.OR_SUBSCRIBER_CONS
            )
            def tmp = financialData(result, params, user, MODE_OWNER) //Grab the financials!
            result.cost_item_tabs = [owner:tmp.cost_items]
            if(orgRoleCons) {
                tmp = financialData(result,params,user,MODE_CONS)
                result.cost_item_tabs["cons"] = tmp.cost_items
            }
            else if(orgRoleSubscr) {
                tmp = financialData(result,params,user,MODE_SUBSCR)
                result.cost_item_tabs["subscr"] = tmp.cost_items
            }
            def workbook = processFinancialXLS(result) //use always header, no batch processing intended

            def filename = result.institution.name
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}_financialExport.xls\"")
            response.contentType = "application/vnd.ms-excel"
            workbook.write(response.outputStream)
            response.outputStream.flush()
    }

    /**
     * Make a XLS export of cost item results
     * @param result - passed from index
     * @return
     */
    def private processFinancialXLS(result) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(message(code: 'default.date.format.notime', default: 'dd.MM.yyyy'))
        HSSFWorkbook wb = new HSSFWorkbook()
        result.cost_item_tabs.entrySet().each { cit ->
            String sheettitle
            String viewMode = cit.getKey()
            switch(viewMode) {
                case "owner": sheettitle = message(code:'financials.header.ownCosts')
                break
                case "cons": sheettitle = message(code:'financials.header.consortialCosts')
                break
                case "subscr": sheettitle = message(code:'financials.header.subscriptionCosts')
                break
            }
            HSSFSheet sheet = wb.createSheet(sheettitle)
            sheet.setAutobreaks(true)
            Row headerRow = sheet.createRow(0)
            headerRow.setHeightInPoints(16.75f)
            ArrayList titles = [message(code: 'sidewide.number'), message(code: 'financials.invoice_number'), message(code: 'financials.order_number')]
            if(viewMode == "cons")
                titles.addAll(["${message(code:'financials.newCosts.costParticipants')} (${message(code:'financials.isVisibleForSubscriber')})"])
            titles.addAll([message(code: 'financials.newCosts.costTitle'), message(code: 'financials.newCosts.subscriptionHeader'),
                           message(code: 'package'), message(code: 'issueEntitlement.label'), message(code: 'financials.datePaid'), message(code: 'financials.dateFrom'),
                           message(code: 'financials.dateTo'), message(code: 'financials.addNew.costCategory'), message(code: 'financials.costItemStatus'),
                           message(code: 'financials.billingCurrency'),message(code: 'financials.costInBillingCurrency'),"EUR",message(code: 'financials.costInLocalCurrency'),
                           message(code: 'financials.taxRate')])
            if(["owner","cons"].indexOf(viewMode) > -1)
                titles.addAll([message(code:'financials.billingCurrency'),message(code: 'financials.costInBillingCurrencyAfterTax'),"EUR",message(code: 'financials.costInLocalCurrencyAfterTax')])
            titles.addAll([message(code: 'financials.costItemElement'),message(code: 'financials.newCosts.description'),
                           message(code: 'financials.newCosts.constsReferenceOn'), message(code: 'financials.budgetCode')])
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
            double localSum = 0.0
            double localSumAfterTax = 0.0
            def sumCounters = [:]
            def sumAfterTaxCounters = [:]
            HashSet<String> currencies = new HashSet<String>()
            if(cit.getValue().size() > 0) {
                cit.getValue().each { ci ->
                    def codes = CostItemGroup.findAllByCostItem(ci).collect { it?.budgetCode?.value }
                    def start_date   = ci.startDate ? dateFormat.format(ci?.startDate) : ''
                    def end_date     = ci.endDate ? dateFormat.format(ci?.endDate) : ''
                    def paid_date    = ci.datePaid ? dateFormat.format(ci?.datePaid) : ''
                    int cellnum = 0
                    row = sheet.createRow(rownum)
                    //sidewide number
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(rownum)
                    //invoice number
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.invoice ? ci.invoice.invoiceNumber : "")
                    //order number
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.order ? ci.order.orderNumber : "")
                    if(viewMode == "cons") {
                        if(ci.sub) {
                            def orgRoles = OrgRole.findBySubAndRoleType(ci.sub,RefdataValue.getByValueAndCategory('Subscriber_Consortial','Organisational Role'))
                            //participants (visible?)
                            cell = row.createCell(cellnum++)
                            orgRoles.each { or ->
                                String cellValue = or.org.getDesignation()
                                if(ci.isVisibleForSubscriber)
                                    cellValue += " (sichtbar)"
                                cell.setCellValue(cellValue)
                            }
                        }
                    }
                    //cost title
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci.costTitle)
                    //subscription with running time
                    cell = row.createCell(cellnum++)
                    String dateString = ""
                    if(ci.sub) {
                        dateString = "${ci.sub.name} (${dateFormat.format(ci.sub.startDate)}"
                        if(ci.sub.endDate)
                            dateString += " - ${dateFormat.format(ci.sub.endDate)})"
                    }
                    cell.setCellValue(dateString)
                    //subscription package
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.subPkg ? ci.subPkg.pkg.name:'')
                    //issue entitlement
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.issueEntitlement ? ci.issueEntitlement?.tipp?.title?.title:'')
                    //date paid
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(paid_date)
                    //date from
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(start_date)
                    //date to
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(end_date)
                    //cost item category
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costItemCategory ? ci.costItemCategory.value:'')
                    //for the sum title
                    sumTitleCell = cellnum
                    //cost item status
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costItemStatus ? ci.costItemStatus.getI10n("value"):'')
                    if(["owner","cons"].indexOf(viewMode) > -1) {
                        //billing currency and value
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(ci?.billingCurrency ? ci.billingCurrency.value : '')
                        if(currencies.add(ci?.billingCurrency?.value)) {
                            sumCounters[ci.billingCurrency.value] = 0.0
                            sumAfterTaxCounters[ci.billingCurrency.value] = 0.0
                        }
                        sumCurrencyCell = cellnum
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(ci?.costInBillingCurrency ? ci.costInBillingCurrency : 0.0)
                        sumCounters[ci.billingCurrency.value] += ci.costInBillingCurrency
                        //local currency and value
                        cell = row.createCell(cellnum++)
                        cell.setCellValue("EUR")
                        sumcell = cellnum
                        cell = row.createCell(cellnum++)
                        cell.setCellValue(ci?.costInLocalCurrency ? ci.costInLocalCurrency : 0.0)
                        localSum += ci.costInLocalCurrency
                    }
                    //tax rate
                    cell = row.createCell(cellnum++)
                    cell.setCellValue("${ci.taxRate ?: 0} %")
                    //billing currency and value
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.billingCurrency ? ci.billingCurrency.value : '')
                    if(currencies.add(ci?.billingCurrency?.value))
                        sumAfterTaxCounters[ci.billingCurrency.value] = 0.0
                    sumCurrencyAfterTaxCell = cellnum
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costInBillingCurrencyAfterTax ? ci.costInBillingCurrencyAfterTax : 0.0)
                    sumAfterTaxCounters[ci.billingCurrency.value] += ci.costInBillingCurrencyAfterTax
                    //local currency and value
                    cell = row.createCell(cellnum++)
                    cell.setCellValue("EUR")
                    sumcellAfterTax = cellnum
                    cell = row.createCell(cellnum++)
                    cell.setCellValue(ci?.costInLocalCurrencyAfterTax ? ci.costInLocalCurrencyAfterTax : 0.0)
                    localSumAfterTax += ci.costInLocalCurrencyAfterTax
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
                    cell.setCellValue(codes ? codes.toString() : '')
                    rownum++
                }
                rownum++
                sheet.createRow(rownum)
                Row sumRow = sheet.createRow(rownum)
                cell = sumRow.createCell(sumTitleCell)
                cell.setCellValue("Summen:")
                if(sumcell > 0) {
                    cell = sumRow.createCell(sumcell)
                    cell.setCellValue(localSum)
                }
                cell = sumRow.createCell(sumcellAfterTax)
                cell.setCellValue(localSumAfterTax)
                rownum++
                currencies.each { currency ->
                    sumRow = sheet.createRow(rownum)
                    cell = sumRow.createCell(sumTitleCell)
                    cell.setCellValue(currency)
                    if(sumCurrencyCell > 0) {
                        cell = sumRow.createCell(sumCurrencyCell)
                        cell.setCellValue(sumCounters.get(currency))
                    }
                    cell = sumRow.createCell(sumCurrencyAfterTaxCell)
                    cell.setCellValue(sumAfterTaxCounters.get(currency))
                    rownum++
                }
            }
            else {
                row = sheet.createRow(rownum)
                cell = row.createCell(0)
                cell.setCellValue(message(code:"finance.export.empty"))
            }

            for(int i = 0; i < titles.size(); i++) {
                sheet.autoSizeColumn(i)
            }
        }

        wb
    }

    /**
     * Method used by index to configure the HQL, check existence, and setup helpful messages
     * @param result
     * @param params
     * @param wildcard
     * @return
     */
    def private filterQuery(LinkedHashMap result, GrailsParameterMap params, boolean wildcard, queryMode) {
        def fqResult = [:]

        fqResult.qry_string = ""
        fqResult.fqParams = [owner: result.institution]

        def count =  ci_count + " where ci.owner = :owner "
        def countCheck = ci_count + " where ci.owner = :owner "

        def hqlCompare = (wildcard) ? " like " : " = "

        // usage:
        // filterParam = FORM.FIELD.name
        // hqlVar = DOMAINCLASS.attribute
        // opt = value | refdata | budgetcode | <generic>

        Closure filterBy = { filterParam, hqlVar, opt ->

            if (params.get(filterParam)) {
                def _query, _param

                // CostItem.attributes
                if (opt == 'value') {
                    _query = " AND ci.${hqlVar} ${hqlCompare} :${hqlVar} "
                    _param = (wildcard) ? "%${params.get(filterParam)}%" : params.get(filterParam)
                }
                // CostItem.refdataValues
                else if (opt == 'refdata') {
                    _query = " AND ci.${hqlVar} = :${hqlVar} "
                    _param = genericOIDService.resolveOID(params.get(filterParam))
                }
                // CostItem <- CostItemGroup -> Budgetcodes
                else if (opt == 'budgetCode') {
                    _query = " AND exists (select cig from CostItemGroup as cig where cig.costItem = ci and cig.budgetCode.value = :${hqlVar} ) "
                    _param = params.get(filterParam)
                }
                // CostItem.<generic>.attributes
                else {
                    //_query = " AND ci.${opt}.${hqlVar} ${hqlCompare} :${hqlVar} "
                    //_param = (wildcard) ? "%${params.get(filterParam)}%" : params.get(filterParam)
                    _query = " AND ci.${opt}.${hqlVar} = :${hqlVar} "
                    _param = params.get(filterParam)
                }

                def order = CostItem.executeQuery(count + _query, [owner: result.institution, (hqlVar): _param])

                if (order && order.first() > 0) {
                    fqResult.qry_string += _query
                    countCheck          += _query

                    fqResult.fqParams << [(hqlVar): _param]
                }
                else {
                    params.remove(filterParam)
                }
            }
        }

        filterBy( 'filterCITitle', 'costTitle', 'value' )
        filterBy( 'filterCIOrderNumber', 'orderNumber', 'order' )
        filterBy( 'filterCIInvoiceNumber', 'invoiceNumber', 'invoice' )

        //filterBy( 'filterCICategory', 'costItemCategory', 'refdata' )
        filterBy( 'filterCIElement', 'costItemElement', 'refdata' )
        filterBy( 'filterCIStatus', 'costItemStatus', 'refdata' )

        filterBy( 'filterCITaxType', 'taxCode', 'refdata' )
        filterBy( 'filterCIBudgetCode', 'budgetCode', 'budgetCode' )

        if (params.filterCISub) {
            if(params.filterCISub instanceof String) {
              def fSub = genericOIDService.resolveOID(params.filterCISub)
              if (fSub) {
                  if(queryMode == MODE_CONS) {
                      fqResult.qry_string += " AND ci.sub.id = :subId OR ci.sub.instanceOf = :subId "
                      countCheck          += " AND ci.sub.id = :subId OR ci.sub.instanceOf = :subId "
                  }
                  else {
                      fqResult.qry_string += " AND ci.sub.id = :subId "
                      countCheck          += " AND ci.sub.id = :subId "
                  }

                fqResult.fqParams << [subId: fSub.id]
              }
            }
            else if(params.filterCISub.getClass().isArray()) {
                ArrayList fSubs = new ArrayList()
                for(int i = 0;i < params.filterCISub.length; i++){
                    def fSub = genericOIDService.resolveOID(params.filterCISub[i])
                    if (fSub) {
                         fSubs.add(fSub.id)
                    }
                }
                if(queryMode == MODE_CONS) {
                    fqResult.qry_string += " AND ci.sub.id IN :subIds OR ci.sub.instanceOf IN :subIds "
                    countCheck          += " AND ci.sub.id IN :subIds OR ci.sub.instanceOf IN :subIds "
                }
                else {
                    fqResult.qry_string += " AND ci.sub.id IN :subIds "
                    countCheck          += " AND ci.sub.id IN :subIds "
                }

                fqResult.fqParams << [subIds: fSubs]
            }
        }

        if (params.filterCISPkg) {
            if(params.filterCISPkg instanceof String) {
                def fSPkg = genericOIDService.resolveOID(params.filterCISPkg)
                if (fSPkg) {
                    fqResult.qry_string += " AND ci.subPkg.pkg.id = :pkgId"
                    countCheck          += " AND ci.subPkg.pkg.id = :pkgId"

                    fqResult.fqParams << [pkgId: fSPkg.pkg.id]
                }
            }
            else if(params.filterCISPkg.getClass.isArray()) {
                ArrayList fSPkgs = new ArrayList()
                for(int i = 0;i < params.filterCISPkg.length; i++) {
                    def fSPkg = genericOIDService.resolveOID(params.filterCISPkg[i])
                    if (fSPkg) {
                        fSPkgs.add(fSPkg.pkg.id)
                    }
                }
                fqResult.qry_string += " AND ci.subPkg.pkg.id IN :pkgIds "
                countCheck          += " AND ci.subPkg.pkg.id IN :pkgIds "
                fqResult.fqParams << [pkgIds:fSPkgs]
            }
        }

        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        if (params.filterCIValidOn) {
            fqResult.qry_string += " AND (ci.startDate <= :validOn OR ci.startDate IS null) AND (ci.endDate >= :validOn OR ci.endDate IS null) "
            countCheck          += " AND (ci.startDate <= :validOn OR ci.startDate IS null) AND (ci.endDate >= :validOn OR ci.endDate IS null) "

            fqResult.fqParams << [validOn: sdf.parse(params.filterCIValidOn)]
        }

        if (params.filterCIInvoiceFrom) {
            // println sdf.parse(params.filterCIInvoiceFrom)

            fqResult.qry_string += " AND (ci.invoiceDate >= :invoiceDateFrom AND ci.invoiceDate IS NOT null) "
            countCheck          += " AND (ci.invoiceDate >= :invoiceDateFrom AND ci.invoiceDate IS NOT null) "

            fqResult.fqParams << [invoiceDateFrom: sdf.parse(params.filterCIInvoiceFrom)]
        }

        if (params.filterCIInvoiceTo) {
            // println sdf.parse(params.filterCIInvoiceTo)

            fqResult.qry_string += " AND (ci.invoiceDate <= :invoiceDateTo AND ci.invoiceDate IS NOT null) "
            countCheck          += " AND (ci.invoiceDate <= :invoiceDateTo AND ci.invoiceDate IS NOT null) "

            fqResult.fqParams << [invoiceDateTo: sdf.parse(params.filterCIInvoiceTo)]
        }

        if (params.filterCIPaidFrom) {
            // println sdf.parse(params.filterCIPaidFrom)

            fqResult.qry_string += " AND (ci.datePaid >= :datePaidFrom AND ci.datePaid IS NOT null) "
            countCheck          += " AND (ci.datePaid >= :datePaidFrom AND ci.datePaid IS NOT null) "

            fqResult.fqParams << [datePaidFrom: sdf.parse(params.filterCIPaidFrom)]
        }

        if (params.filterCIPaidTo) {
            // println sdf.parse(params.filterCIPaidTo)

            fqResult.qry_string += " AND (ci.datePaid <= :datePaidTo AND ci.datePaid IS NOT null) "
            countCheck          += " AND (ci.datePaid <= :datePaidTo AND ci.datePaid IS NOT null) "

            fqResult.fqParams << [datePaidTo: sdf.parse(params.filterCIPaidTo)]
        }

        fqResult.filterCount = CostItem.executeQuery(countCheck, fqResult.fqParams).first() // ?
        log.debug("filterQuery() ${fqResult.qry_string ?: 'NO QUERY BUILD!'}")

        return fqResult
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def editCostItem() {
        def result = [:]
        result.tab = params.tab

        result.inSubMode = params.fixedSub ? true : false
        if (result.inSubMode) {
            result.fixedSubscription = params.int('fixedSub') ? Subscription.get(params.fixedSub) : null
        }
        else {
            result.currentSubscription = params.int('currSub') ? Subscription.get(params.currSub) : null
        }
        result.costItem = CostItem.findById(params.id)

        result.formUrl = g.createLink(controller:'finance', action:'newCostItem', params:[tab:result.tab])

        render(template: "/finance/ajaxModal", model: result)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def copyCostItem() {
        def result = [:]

        result.id = params.id
        result.fixedSub = params.fixedSub
        result.currSub = params.currSub
        result.inSubMode = params.fixedSub ? true : false

        result.tab = params.tab

        if (result.inSubMode) {
            result.fixedSubscription = params.int('fixedSub') ? Subscription.get(params.fixedSub) : null
        }
        else {
            result.currentSubscription = params.int('currSub') ? Subscription.get(params.currSub) : null
        }

        def ci = CostItem.findById(params.id)
        result.costItem = ci

        if (ci && params.process) {
            params.list('newLicenseeTargets')?.each{ target ->

                def newSub = genericOIDService.resolveOID(target)

                CostItem newCostItem = new CostItem()
                InvokerHelper.setProperties(newCostItem, ci.properties)
                newCostItem.globalUID = null
                newCostItem.sub = newSub

                if (! newCostItem.validate())
                {
                    result.error = newCostItem.errors.allErrors.collect {
                        log.error("Field: ${it.properties.field}, user input: ${it.properties.rejectedValue}, Reason! ${it.properties.code}")
                        message(code:'finance.addNew.error', args:[it.properties.field])
                    }
                }
                else {
                    if ( newCostItem.save(flush: true) ) {
                        ci.getBudgetcodes().each{ bc ->
                            if (! CostItemGroup.findByCostItemAndBudgetCode(newCostItem, bc)) {
                                new CostItemGroup(costItem: newCostItem, budgetCode: bc).save(flush: true)
                            }
                        }
                    }
                }
            }

            redirect(uri: request.getHeader('referer').replaceAll('(#|\\?).*', ''), params: [tab: result.tab])
        }
        else {
            result.formUrl = g.createLink(mapping:"subfinanceCopyCI", params:[sub:result.sub, id:result.id, tab:result.tab])

            render(template: "/finance/copyModal", model: result)
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteCostItem() {
        def result = [:]

        def user = User.get(springSecurityService.principal.id)
        def institution = contextService.getOrg()
        if (!isFinanceAuthorised(institution, user)) {
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def newCostItem() {

        def dateFormat      = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        def result =  [:]
        def newCostItem = null

      try {
        log.debug("FinanceController::newCostItem() ${params}");

        result.institution  =  contextService.getOrg()
        def user            =  User.get(springSecurityService.principal.id)
        result.error        =  [] as List

        if (!isFinanceAuthorised(result.institution, user))
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

        def ie = null
        if(params.newIe)
        {
            try {
                ie = IssueEntitlement.load(params.newIe.split(":")[1])
            } catch (Exception e) {
                log.error("Non-valid IssueEntitlement sent ${params.newIe}",e)
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
          def cost_tax_type         = params.newCostTaxType ?          (RefdataValue.get(params.long('newCostTaxType'))) : null           //on invoice, self declared, etc
          def cost_item_category    = params.newCostItemCategory ?     (RefdataValue.get(params.long('newCostItemCategory'))): null  //price, bank charge, etc

          def cost_billing_currency = params.newCostInBillingCurrency? params.double('newCostInBillingCurrency',0.00) : 0.00
          def cost_currency_rate    = params.newCostCurrencyRate?      params.double('newCostCurrencyRate', 1.00) : 1.00
          def cost_local_currency   = params.newCostInLocalCurrency?   params.double('newCostInLocalCurrency', 0.00) : 0.00

          def cost_billing_currency_after_tax   = params.newCostInBillingCurrencyAfterTax ? params.double( 'newCostInBillingCurrencyAfterTax') : cost_billing_currency
          def cost_local_currency_after_tax     = params.newCostInLocalCurrencyAfterTax ? params.double( 'newCostInLocalCurrencyAfterTax') : cost_local_currency
          def new_tax_rate                      = params.newTaxRate ? params.int( 'newTaxRate' ) : 0

          def cost_item_isVisibleForSubscriber = (params.newIsVisibleForSubscriber ? (RefdataValue.get(params.newIsVisibleForSubscriber)?.value == 'Yes') : false)

          if (! subsToDo) {
              subsToDo << null // Fallback for editing cost items via myInstitution/finance // TODO: ugly
          }
          subsToDo.each { sub ->

              if (params.oldCostItem && genericOIDService.resolveOID(params.oldCostItem)) {
                  newCostItem = genericOIDService.resolveOID(params.oldCostItem)
              }
              else {
                  newCostItem = new CostItem()
              }

              newCostItem.owner = result.institution
              newCostItem.sub = sub
              newCostItem.subPkg = pkg
              newCostItem.issueEntitlement = ie
              newCostItem.order = order
              newCostItem.invoice = invoice
              newCostItem.isVisibleForSubscriber = cost_item_isVisibleForSubscriber
              newCostItem.costItemCategory = cost_item_category
              newCostItem.costItemElement = cost_item_element
              newCostItem.costItemStatus = cost_item_status
              newCostItem.billingCurrency = billing_currency //Not specified default to GDP
              newCostItem.taxCode = cost_tax_type
              newCostItem.costDescription = params.newDescription ? params.newDescription.trim() : null
              newCostItem.costTitle = params.newCostTitle ?: null
              newCostItem.costInBillingCurrency = cost_billing_currency as Double
              newCostItem.costInLocalCurrency = cost_local_currency as Double

              newCostItem.finalCostRounding = params.newFinalCostRounding ? true : false
              newCostItem.costInBillingCurrencyAfterTax = cost_billing_currency_after_tax as Double
              newCostItem.costInLocalCurrencyAfterTax = cost_local_currency_after_tax as Double
              newCostItem.currencyRate = cost_currency_rate as Double
              newCostItem.taxRate = new_tax_rate as Integer

              newCostItem.datePaid = datePaid
              newCostItem.startDate = startDate
              newCostItem.endDate = endDate
              newCostItem.invoiceDate = invoiceDate

              newCostItem.includeInSubscription = null //todo Discussion needed, nobody is quite sure of the functionality behind this...
              newCostItem.reference = params.newReference ? params.newReference.trim()?.toLowerCase() : null


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

        result.tab = params.tab

        redirect(uri: request.getHeader('referer').replaceAll('(#|\\?).*', ''), params: [tab: result.tab])
    }

    @Deprecated
    private def createBudgetCodes(CostItem costItem, String budgetcodes, Org budgetOwner)
    {
        def result = []
        if(budgetcodes && budgetOwner && costItem) {
            budgetcodes.split(",").each { c ->
                def bc = null
                if (c.startsWith("-1")) { //New code option from select2 UI
                    bc = new BudgetCode(owner: budgetOwner, value: c.substring(2).toLowerCase()).save(flush: true)
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def delete() {
        log.debug("FinanceController::delete() ${params}");

        def results        =  [:]
        results.successful =  []
        results.failures   =  []
        results.message    =  null
        results.sentIDs    =  JSON.parse(params.del) //comma seperated list
        def user           =  User.get(springSecurityService.principal.id)
        def institution    =  contextService.getOrg()
        if (!isFinanceAuthorised(institution, user))
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
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
    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
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
}
