package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import de.laser.helper.DebugAnnotation
import grails.converters.JSON;
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap

//todo Refactor aspects into service
//todo track state, opt 1: potential consideration of using get, opt 2: requests maybe use the #! stateful style syntax along with the history API or more appropriately history.js (cross-compatible, polyfill for HTML4)
//todo Change notifications integration maybe use : changeNotificationService with the onChange domain event action
//todo Refactor index separation of filter page (used for AJAX), too much content, slows DOM on render/binding of JS functionality
//todo Enable advanced searching, use configurable map, see filterQuery() 
@Secured(['IS_AUTHENTICATED_FULLY'])
class FinanceController {

    def springSecurityService
    def accessService
    def contextService
    def genericOIDService

    private final def ci_count        = 'select count(ci.id) from CostItem as ci '
    private final def ci_select       = 'select ci from CostItem as ci '
    private final def user_role        = Role.findByAuthority('INST_USER')
    private final def defaultCurrency = RefdataCategory.lookupOrCreate('Currency','EUR')
    private final def maxAllowedVals  = [10,20,50,100,200] //in case user has strange default list size, plays hell with UI
    //private final def defaultInclSub  = RefdataCategory.lookupOrCreate('YN','Yes') //Owen is to confirm this functionality

    final static MODE_OWNER       = 'MODE_OWNER'
    final static MODE_CONS_SUBSCR = 'MODE_CONS_SUBSCR'

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

        def dateTimeFormat  = new java.text.SimpleDateFormat(message(code:'default.date.format')) {{setLenient(false)}}
        def result = [:]

      try {
        result.institution = contextService.getOrg()

        def user =  User.get(springSecurityService.principal.id)
        if (!isFinanceAuthorised(result.institution, user)) {
            log.error("Sending 401 - forbidden");
            flash.error=message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A'])
            response.sendError(401)
        }

        //Accessed from Subscription page, 'hardcoded' set subscription 'hardcode' values
        //todo Once we know we are in sub only mode, make nessesary adjustments in setupQueryData()
        result.inSubMode   = params.sub ? true : false
        if (result.inSubMode)
        {
            params.subscriptionFilter = "${params.sub}"

            result.fixedSubscription = params.int('sub')? Subscription.get(params.sub) : null
            if (! result.fixedSubscription) {
                log.error("Financials in FIXED subscription mode, sent incorrect subscription ID: ${params?.sub}")
                response.sendError(400, "No relevant subscription, please report this error to an administrator")
            }
        }

          //Grab the financial data
          if (result.inSubMode
                  &&
                  OrgRole.findBySubAndOrgAndRoleType(
                      result.fixedSubscription,
                      result.institution,
                      RefdataValue.getByValueAndCategory('Subscription Consortia', 'Organisational Role')
                  )
                  &&
                  ! OrgRole.findBySubAndRoleType(
                      result.fixedSubscription,
                      RefdataValue.getByValueAndCategory('Subscriber_Consortial', 'Organisational Role')
                  )
          ) {
              result.queryMode = MODE_CONS_SUBSCR
              def tmp = financialData(result, params, user, MODE_CONS_SUBSCR)

              result.foundMatches_CS    = tmp.foundMatches
              result.cost_items_CS      = tmp.cost_items
              result.cost_item_count_CS = tmp.cost_item_count
          }
          else {
              result.queryMode = MODE_OWNER
          }

          def tmp = financialData(result, params, user, MODE_OWNER)
          result.foundMatches    = tmp.foundMatches
          result.cost_items      = tmp.cost_items
          result.cost_item_count = tmp.cost_item_count

            flash.error = null
            flash.message = null

            if (result.foundMatches || result.foundMatches_CS ) {
                flash.message = "Felder mit potentiellen Treffern bleiben gesetzt."
            }
            else if (params.get('submit')) {
                flash.error = "Keine Treffer. Der Filter wird zurückgesetzt."
            }

          // TODO : MODE_CHILDREN & MODE_ALL

          // prepare filter dropdowns
          def myCostItems = result.fixedSubscription ?
                    CostItem.findAllWhere(owner: result.institution, sub: result.fixedSubscription)
                  : CostItem.findAllWhere(owner: result.institution)

          result.allCIInvoiceNumbers = (myCostItems.collect{ it -> it?.invoice?.invoiceNumber }).findAll{ it }.unique().sort()
          result.allCIOrderNumbers   = (myCostItems.collect{ it -> it?.order?.orderNumber }).findAll{ it }.unique().sort()
          result.allCIBudgetCodes    = (myCostItems.collect{ it -> it?.budgetcodes?.value }).flatten().unique().sort()

          result.allCISPkgs = (myCostItems.collect{ it -> it?.subPkg }).findAll{ it }.unique().sort()
          result.allCISubs  = (myCostItems.collect{ it -> it?.sub }).findAll{ it }.unique().sort()

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

        //Setup using param data, returning back DB query info

        result.editable    =  accessService.checkMinUserOrgRole(user, result.institution, user_role)
        params.shortcode   =  result.institution.shortcode

        request.setAttribute("editable", result.editable) //editable Taglib doesn't pick up AJAX request, REQUIRED!
        result.info        =  [] as List
        //params.max         =  params.max && params.int('max') ? Math.min(params.int('max'),200) : (user?.defaultPageSize? maxAllowedVals.min{(it-user.defaultPageSize).abs()} : 10)
        //result.max         =  params.max
        //result.offset      =  params.int('offset',0)?: 0

        // WORKAROUND: erms-517
        params.max = 5000
        result.max = 5000
        result.offset = 0

        result.sort        =  ["desc","asc"].contains(params.sort)? params.sort : "desc" //defaults to sort & order of desc id
        result.isRelation  =  params.orderRelation? params.boolean('orderRelation',false) : false

        //result.wildcard    =  params.wildcard != 'off' ? 'on' : 'off' //defaulted to on

        // TODO fix:shortcode
        if (params.csvMode && request.getHeader('referer')?.endsWith("${params?.shortcode}/finance")) {
            params.max = -1 //Adjust so all results are returned, in regards to present user screen query
            log.debug("Making changes to query setup data for an export...")
        }
        //Query setup options, ordering, joins, param query data....
        def (order, join, gspOrder) = CostItem.orderingByCheck(params.order) //order = field, join = left join required or null, gsporder = to see which field is ordering by
        result.order = gspOrder

        //todo Add to query params and HQL query if we are in sub mode e.g. result.inSubMode, result.fixedSubscription
        def cost_item_qry_params =  [owner: result.institution]
        def cost_item_qry        = (join)? "LEFT OUTER JOIN ${join} AS j WHERE ci.owner = :owner " :"  where ci.owner = :owner "
        def orderAndSortBy       = (join)? "ORDER BY COALESCE(j.${order}, ${Integer.MAX_VALUE}) ${result.sort}, ci.id ASC" : " ORDER BY ci.${order} ${result.sort}"


        //Filter processing...

            log.debug("FinanceController::index()  -- Performing filtering processing...")
            def qryOutput = filterQuery(result, params, (/*result.wildcard != 'off'*/ true), queryMode)

            cost_item_qry_params   << qryOutput.fqParams
        tmp.foundMatches    =  cost_item_qry_params.size() > 1 // [owner:default] ; used for flash
        tmp.cost_items      =  CostItem.executeQuery(ci_select + cost_item_qry + qryOutput.qry_string + orderAndSortBy, cost_item_qry_params, params);
        tmp.cost_item_count =  CostItem.executeQuery(ci_count + cost_item_qry + qryOutput.qry_string, cost_item_qry_params).first();

        log.debug("FinanceController::index()  -- Performed filtering process... ${tmp.cost_item_count} result(s) found")

        tmp
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def financialsExport()  {
        log.debug("Financial Export :: ${params}")

        if (request.isPost() && params.format == "csv") {
            def result = [:]
            result.institution =  contextService.getOrg()
            def user           =  User.get(springSecurityService.principal.id)

            if (!isFinanceAuthorised(result.institution, user)) {
                flash.error=message(code: 'financials.permission.unauthorised', args: [result.institution? result.institution.name : 'N/A'])
                response.sendError(403)
                return
            }

            financialData(result, params, user, MODE_OWNER) //Grab the financials!
            def filename = result.institution.name
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}_financialExport.csv\"")
            response.contentType = "text/csv"
            def out = response.outputStream
            def useHeader = params.header? true : false //For batch processing...
            processFinancialCSV(out,result,useHeader)
            out.close()
        }
        else
        {
            response.sendError(400)
        }
    }

    /**
     * Make a CSV export of cost item results
     * @param out    - Output stream
     * @param result - passed from index
     * @param header - true or false
     * @return
     */
    //todo change for batch processing... don't want to kill the server, defaulting to all results presently!
    def private processFinancialCSV(out, result, header) {
        def dateFormat      = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        def generation_start = new Date()
        def processedCounter = 0

        switch (params.csvMode)
        {
            case "code":
                log.debug("Processing code mode... Estimated total ${params.estTotal?: 'Unknown'}")

                def categories = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('CostItemStatus')).collect {it.value.toString()} << "Unknown"

                def codeResult = [:].withDefault {
                    categories.collectEntries {
                        [(it): 0 as Double]
                    }
                }

                result.cost_items.each { c -> // TODO: CostItemGroup -> BudgetCode
                    if (!c.budgetcodes.isEmpty())
                    {
                        log.debug("${c.budgetcodes.size()} codes for Cost Item: ${c.id}")

                        def status = c?.costItemStatus?.value? c.costItemStatus.value.toString() : "Unknown"

                        c.budgetcodes.each {bc ->
                            if (! codeResult.containsKey(bc.value))
                                codeResult[bc.value] //sets up with default values

                            if (! codeResult.get(bc.value).containsKey(status))
                            {
                                log.warn("Status should exist in list already, unless additions have been made? Code:${bc} Status:${status}")
                                codeResult.get(bc.value).put(status, c?.costInLocalCurrency? c.costInLocalCurrency : 0.0)
                            }
                            else
                            {
                                codeResult[bc.value][status] += c?.costInLocalCurrency?: 0.0
                            }
                        }
                    }
                    else
                    {
                        log.debug("skipped cost item ${c.id} NO codes are present")
                    }
                }

                def catSize = categories.size()-1
                out.withWriter { writer ->
                    writer.write("\t" + categories.join("\t") + "\n") //Header

                    StringBuilder sb = new StringBuilder() //join map vals e.g. estimate : 123

                    codeResult.each {code, cat_statuses ->
                        sb.append(code).append("\t")
                        cat_statuses.eachWithIndex { status, amount, idx->
                            sb.append(amount)
                            if (idx < catSize)
                                sb.append("\t")
                        }
                        sb.append("\n")
                    }
                    writer.write(sb.toString())
                    writer.flush()
                    writer.close()
                }

                processedCounter = codeResult.size()
                break

            case "sub":
                log.debug("Processing subscription data mode... calculation of costs Estimated total ${params.estTotal?: 'Unknown'}")

                def categories = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('CostItemStatus')).collect {it.value} << "Unknown"

                def subResult = [:].withDefault {
                    categories.collectEntries {
                        [(it): 0 as Double]
                    }
                }

                def skipped = []

                result.cost_items.each { c ->
                    if (c?.sub)
                    {
                        def status = c?.costItemStatus?.value? c.costItemStatus.value.toString() : "Unknown"
                        def subID  = c.sub.name

                        if (!subResult.containsKey(subID))
                            subResult[subID] //1st time around for subscription, could 1..* cost items linked...

                        if (!subResult.get(subID).containsKey(status)) //This is here as a safety precaution, you're welcome :P
                        {
                            log.warn("Status should exist in list already, unless additions have been made? Sub:${subID} Status:${status}")
                            subResult.get(subID).put(status, c?.costInLocalCurrency? c.costInLocalCurrency : 0.0)
                        }
                        else
                        {
                            subResult[subID][status] += c?.costInLocalCurrency?: 0.0
                        }
                    }
                    else
                    {
                        skipped.add("${c.id}")
                    }
                }

                log.debug("Skipped ${skipped.size()} out of ${result.cost_items.size()} Cost Item's (NO subscription present) IDs : ${skipped} ")

                def catSize = categories.size()-1
                out.withWriter { writer ->
                    writer.write("\t" + categories.join("\t") + "\n") //Header

                    StringBuilder sb = new StringBuilder() //join map vals e.g. estimate : 123

                    subResult.each {sub, cat_statuses ->
                        sb.append(sub).append("\t")
                        cat_statuses.eachWithIndex { status, amount, idx->
                            sb.append(amount)
                            if (idx < catSize)
                                sb.append("\t")
                        }
                        sb.append("\n")
                    }
                    writer.write(sb.toString())
                    writer.flush()
                    writer.close()
                }

                processedCounter = subResult.size()
                break

            case "all":
            default:
                log.debug("Processing all mode... Estimated total ${params.estTotal?: 'Unknown'}")

                out.withWriter { writer ->

                    if ( header ) {
                        writer.write("Institution\tGenerated Date\tCost Item Count\n")
                        writer.write("${result.institution.name?:''}\t${dateFormat.format(generation_start)}\t${result.cost_item_count}\n")
                    }

                    // Output the body text
                    writer.write("cost_item_id\towner\tinvoice_no\torder_no\tsubscription_name\tsubscription_package\tissueEntitlement\tdate_paid\tdate_valid_from\t" +
                            "date_valid_to\tcost_Item_Category\tcost_Item_Status\tbilling_Currency\tcost_In_Billing_Currency\tcost_In_Local_Currency\ttax_Code\t" +
                            "cost_Item_Element\tcost_Description\treference\tcodes\tcreated_by\tdate_created\tedited_by\tdate_last_edited\n");

                    result.cost_items.each { ci ->

                        def codes = CostItemGroup.findAllByCostItem(ci).collect { it?.budgetCode?.value+'\t' }
                        // TODO budgetcodes

                        def start_date   = ci.startDate ? dateFormat.format(ci?.startDate) : ''
                        def end_date     = ci.endDate ? dateFormat.format(ci?.endDate) : ''
                        def paid_date    = ci.datePaid ? dateFormat.format(ci?.datePaid) : ''
                        def created_date = ci.dateCreated ? dateFormat.format(ci?.dateCreated) : ''
                        def edited_date  = ci.lastUpdated ? dateFormat.format(ci?.lastUpdated) : ''

                        writer.write("\"${ci.id}\"\t\"${ci?.owner?.name}\"\t\"${ci?.invoice?ci.invoice.invoiceNumber:''}\"\t${ci?.order? ci.order.orderNumber:''}\t" +
                                "${ci?.sub? ci.sub.name:''}\t${ci?.subPkg?ci.subPkg.pkg.name:''}\t${ci?.issueEntitlement?ci.issueEntitlement?.tipp?.title?.title:''}\t" +
                                "${paid_date}\t${start_date}\t\"${end_date}\"\t\"${ci?.costItemCategory?ci.costItemCategory.value:''}\"\t\"${ci?.costItemStatus?ci.costItemStatus.value:''}\"\t" +
                                "\"${ci?.billingCurrency.value?:''}\"\t\"${ci?.costInBillingCurrency?:''}\"\t\"${ci?.costInLocalCurrency?:''}\"\t\"${ci?.taxCode?ci.taxCode.value:''}\"\t" +
                                "\"${ci?.costItemElement?ci.costItemElement.value:''}\"\t\"${ci?.costDescription?:''}\"\t\"${ci?.reference?:''}\"\t\"${codes?codes.toString():''}\"\t" +
                                "\"${ci.createdBy.username}\"\t\"${created_date}\"\t\"${ci.lastUpdatedBy.username}\"\t\"${edited_date}\"\n")
                    }
                    writer.flush()
                    writer.close()
                }

                processedCounter = result.cost_items.size()
                break
        }
        groovy.time.TimeDuration duration = groovy.time.TimeCategory.minus(new Date(), generation_start)
        log.debug("CSV export operation for ${params.csvMode} mode -- Duration took to complete (${processedCounter} Rows of data) was: ${duration} --")
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
            def fSub = genericOIDService.resolveOID(params.filterCISub)
            if (fSub) {
                fqResult.qry_string += " AND ci.sub.id = :subId "
                countCheck          += " AND ci.sub.id = :subId "

                fqResult.fqParams << [subId: fSub.id]
            }
        }

        if (params.filterCISPkg) {
            def fSPkg = genericOIDService.resolveOID(params.filterCISPkg)
            if (fSPkg) {
                fqResult.qry_string += " AND ci.subPkg.pkg.id = :pkgId"
                countCheck          += " AND ci.subPkg.pkg.id = :pkgId"

                fqResult.fqParams << [pkgId: fSPkg.pkg.id]
            }
        }

        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        if (params.filterCIValidOn) {
            fqResult.qry_string += " AND (ci.startDate <= :validOn OR ci.startDate IS null) AND (ci.endDate >= :validOn OR ci.endDate IS null) "
            countCheck          += " AND (ci.startDate <= :validOn OR ci.startDate IS null) AND (ci.endDate >= :validOn OR ci.endDate IS null) "

            fqResult.fqParams << [validOn: sdf.parse(params.filterCIValidOn)]
        }

        if (params.filterCIInvoiceFrom) {
            println sdf.parse(params.filterCIInvoiceFrom)

            fqResult.qry_string += " AND (ci.invoiceDate >= :invoiceDateFrom AND ci.invoiceDate IS NOT null) "
            countCheck          += " AND (ci.invoiceDate >= :invoiceDateFrom AND ci.invoiceDate IS NOT null) "

            fqResult.fqParams << [invoiceDateFrom: sdf.parse(params.filterCIInvoiceFrom)]
        }

        if (params.filterCIInvoiceTo) {
            println sdf.parse(params.filterCIInvoiceTo)

            fqResult.qry_string += " AND (ci.invoiceDate <= :invoiceDateTo AND ci.invoiceDate IS NOT null) "
            countCheck          += " AND (ci.invoiceDate <= :invoiceDateTo AND ci.invoiceDate IS NOT null) "

            fqResult.fqParams << [invoiceDateTo: sdf.parse(params.filterCIInvoiceTo)]
        }

        if (MODE_CONS_SUBSCR == queryMode) {
            if (result.inSubMode && result.fixedSubscription) {
                def memberSubs = Subscription.findAllByInstanceOf(result.fixedSubscription)

                fqResult.qry_string += " AND ci_sub_fk IN (" + memberSubs.collect{ it.id }.join(',') + ") "
                countCheck          += " AND ci_sub_fk IN (" + memberSubs.collect{ it.id }.join(',') + ") "
            }
        }
        else if (MODE_OWNER == queryMode) {
            if (result.inSubMode && result.fixedSubscription) {
                fqResult.qry_string += " AND ci_sub_fk = " + result.fixedSubscription.id
                countCheck          += " AND ci_sub_fk = " + result.fixedSubscription.id
            }
        }

        fqResult.filterCount = CostItem.executeQuery(countCheck, fqResult.fqParams).first() // ?
        log.debug("Financials : filterQuery - Wildcard Searching active : ${wildcard} Query output : ${fqResult.qry_string? fqResult.qry_string:'qry failed!'}")

        return fqResult
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def editCostItem() {
        def result = [:]

        //TODO: copied from index()
        result.inSubMode = params.sub ? true : false
        if (result.inSubMode) {
            result.fixedSubscription = params.int('sub') ? Subscription.get(params.sub) : null
        }
        result.costItem = CostItem.findById(params.id)

        render(template: "/finance/ajaxModal", model: result)
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
        redirect(controller: 'myInstitution', action: 'finance')
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

        def sub = null;
        if (params.newSubscription?.contains("com.k_int.kbplus.Subscription:"))
        {
            try {
                sub = Subscription.get(params.newSubscription.split(":")[1]);
            } catch (Exception e) {
                log.error("Non-valid subscription sent ${params.newSubscription}",e)
            }

        }

          // NEW: create cost items for members
          // TODO
          if (params.newLicenseeTarget && (params.newLicenseeTarget != "com.k_int.kbplus.Subscription:null")) {
            sub = genericOIDService.resolveOID(params.newLicenseeTarget)
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

        //def inclSub = params.includeInSubscription? (RefdataValue.get(params.long('includeInSubscription'))): defaultInclSub //todo Speak with Owen, unknown behaviour

          if (params.oldCostItem && genericOIDService.resolveOID(params.oldCostItem)) {
              newCostItem = genericOIDService.resolveOID(params.oldCostItem)
          }
          else {
              newCostItem = new CostItem()
          }

            newCostItem.owner               = result.institution
            newCostItem.sub                 = sub
            newCostItem.subPkg              = pkg
            newCostItem.issueEntitlement    = ie
            newCostItem.order               = order
            newCostItem.invoice             = invoice
            newCostItem.costItemCategory    = cost_item_category
            newCostItem.costItemElement     = cost_item_element
            newCostItem.costItemStatus      = cost_item_status
            newCostItem.billingCurrency     = billing_currency //Not specified default to GDP
            newCostItem.taxCode             = cost_tax_type
            newCostItem.costDescription     = params.newDescription ? params.newDescription.trim() : null
            newCostItem.costTitle           = params.newCostTitle ?: null
            newCostItem.costInBillingCurrency = cost_billing_currency as Double
            newCostItem.costInLocalCurrency = cost_local_currency as Double

            newCostItem.finalCostRounding   = params.newFinalCostRounding ? true : false
            newCostItem.costInBillingCurrencyAfterTax = cost_billing_currency_after_tax as Double
            newCostItem.costInLocalCurrencyAfterTax   = cost_local_currency_after_tax as Double
            newCostItem.currencyRate                  = cost_currency_rate as Double
            newCostItem.taxRate                       = new_tax_rate as Integer

            newCostItem.datePaid            = datePaid
            newCostItem.startDate           = startDate
            newCostItem.endDate             = endDate
            newCostItem.invoiceDate         = invoiceDate

            newCostItem.includeInSubscription = null //todo Discussion needed, nobody is quite sure of the functionality behind this...
            newCostItem.reference           = params.newReference? params.newReference.trim()?.toLowerCase() : null

        if (!newCostItem.validate())
        {
            result.error = newCostItem.errors.allErrors.collect {
                log.error("Field: ${it.properties.field}, user input: ${it.properties.rejectedValue}, Reason! ${it.properties.code}")
                message(code:'finance.addNew.error',args:[it.properties.field])
            }
        }
        else
        {
            if (newCostItem.save(flush: true))
            {
                if (params.newBudgetCode)
                    createBudgetCodes(newCostItem, params.newBudgetCode?.trim()?.toLowerCase(), result.institution)
            } else {
                result.error = "Unable to save!"
            }
        }
      }
      catch ( Exception e ) {
        log.error("Problem in add cost item",e);
      }

      params.remove("Add")
      // render ([newCostItem:newCostItem.id, error:result.error]) as JSON

        redirect(uri: request.getHeader('referer') )
    }

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
                            "SELECT DISTINCT cig.id FROM CostItemGroup AS cig JOIN cig.costItem AS ci JOIN cig.budgetCode AS bc WHERE ci = ? AND bc.owner = ? AND bc IS NOT ?",
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
        def dateTimeFormat  = new java.text.SimpleDateFormat(message(code:'default.date.format')) {{setLenient(false)}}
        def institution = contextService.getOrg()
        Date dateTo     = params.to? dateTimeFormat.parse(params.to):new Date()//getFromToDate(params.to,"to")
        int counter     = CostItem.countByOwnerAndLastUpdatedGreaterThan(institution,dateTo)

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
        def institution = Org.findByShortcode(params.shortcode)
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
