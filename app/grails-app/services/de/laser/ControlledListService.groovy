package de.laser

import com.k_int.kbplus.BudgetCode
import com.k_int.kbplus.CostItem
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.Invoice
import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.License
import com.k_int.kbplus.Order
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionPackage
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import grails.transaction.Transactional
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ControlledListService {

    def contextService
    def genericOIDService
    def messageSource

    /**
     * Retrieves a list of providers
     * @param params - eventual request params
     * @return a map containing a sorted list of providers, an empty one if no providers match the filter
     */
    Map getProviders(Map params) {
        LinkedHashMap result = [results:[]]
        Org org = contextService.getOrg()
        if(params.forFinanceView) {
            List subscriptions = Subscription.executeQuery('select s from CostItem ci join ci.sub s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in (:orgRoles)',[org:org,orgRoles:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIPTION_CONSORTIA]])
            Map filter = [provider: RDStore.OR_PROVIDER,subscriptions:subscriptions]
            String filterString = " "
            if(params.query && params.query.length() > 0) {
                filter.put("query",'%'+params.query+'%')
                filterString += "and lower(oo.name) like lower(:query) "
            }
            List providers = Org.executeQuery('select distinct oo.org, oo.org.name from OrgRole oo where oo.sub in (:subscriptions) and oo.roleType = :provider'+filterString+'order by oo.org.name asc',filter)
            providers.each { p ->
                result.results.add([name:p[1],value:p[0].class.name + ":" + p[0].id])
            }
        }
        else {
            String queryString = 'select o from Org o where o.type = :provider '
            LinkedHashMap filter = [provider:RDStore.OT_PROVIDER]
            if(params.query && params.query.length() > 0) {
                filter.put("query",'%'+params.query+'%')
                queryString += " and lower(o.name) like lower(:query) "
            }
            List providers = Org.executeQuery(queryString+" order by o.sortname asc",filter)
            providers.each { p ->
                result.results.add([name:p.name,value:p.class.name + ":" + p.id])
            }
        }
        result
    }

    /**
     * Retrieves a list of subscriptions owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a sorted list of subscriptions, an empty one if no subscriptions match the filter
     */
    Map getSubscriptions(Map params) {
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale()))
        Org org = contextService.getOrg()
        LinkedHashMap result = [results:[]]
        String queryString = 'select distinct s, orgRoles.org.sortname from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in ( :orgRoles ) and s.status != :deleted'
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],deleted:RDStore.SUBSCRIPTION_DELETED]
        //may be generalised later - here it is where to expand the query filter
        if(params.query && params.query.length() > 0) {
            filter.put("query",'%'+params.query+'%')
            queryString += " and (lower(s.name) like lower(:query) or lower(orgRoles.org.sortname) like lower(:query)) "
        }
        if(params.ctx) {
            Subscription ctx = genericOIDService.resolveOID(params.ctx)
            filter.ctx = ctx
            queryString += " and s != :ctx "
        }
        if(params.status) {
            filter.status = params.status
            queryString += " and s.status = :status "
        }
        List subscriptions = Subscription.executeQuery(queryString+" order by s.name asc, s.startDate asc, s.endDate asc, orgRoles.org.sortname asc",filter)
        subscriptions.each { row ->
            Subscription s = (Subscription) row[0]
            String tenant
            if(s.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_PARTICIPATION && s.getConsortia().id == org.id) {
                try {
                    tenant = s.getAllSubscribers().get(0).name
                }
                catch (IndexOutOfBoundsException e) {
                    log.debug("Please check subscription #${s.id}")
                }
            }
            else {
                tenant = org.name
            }
            if (((params.checkView && s.isVisibleBy(contextService.getUser())) || !params.checkView) && tenant != null) {
                String dateString = ", "
                if (s.startDate)
                    dateString += sdf.format(s.startDate) + "-"
                if (s.endDate)
                    dateString += sdf.format(s.endDate)
                result.results.add([name:"${s.name} (${tenant}${dateString})",value:s.class.name + ":" + s.id])
            }
        }
        result
    }

    /**
     * Retrieves a list of issue entitlements owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a list of issue entitlements, an empty one if no issue entitlements match the filter
     */
    Map getIssueEntitlements(Map params) {
        Org org = contextService.getOrg()
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))
        LinkedHashMap issueEntitlements = [results:[]]
        //build up set of subscriptions which are owned by the current organisation or instances of such - or filter for a given subscription
        String subFilter = 'in (select distinct o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) and o.sub.status = :current ) '
        LinkedHashMap filterParams = [org:org, orgRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS], current:RDStore.SUBSCRIPTION_CURRENT]
        if(params.sub) {
            subFilter = '= :sub'
            filterParams = ['sub':genericOIDService.resolveOID(params.sub)]
        }
        filterParams.put('query','%'+params.query+'%')
        List result = IssueEntitlement.executeQuery('select ie from IssueEntitlement as ie where ie.subscription '+subFilter+' and lower(ie.tipp.title.title) like lower(:query) order by ie.tipp.title.title asc, ie.subscription asc, ie.subscription.startDate asc, ie.subscription.endDate asc',filterParams)
        if(result.size() > 0) {
            log.debug("issue entitlements found")
            result.each { res ->
                Subscription s = (Subscription) res.subscription
                String tenant
                if(s.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_PARTICIPATION && s.getConsortia().id == org.id) {
                    try {
                        tenant = s.getAllSubscribers().get(0).name
                    }
                    catch (IndexOutOfBoundsException e) {
                        log.debug("Please check subscription #${s.id}")
                    }
                }
                else {
                    tenant = org.name
                }
                if(tenant) {
                    String dateString = ", "
                    if (s.startDate)
                        dateString += sdf.format(s.startDate) + "-"
                    if (s.endDate)
                        dateString += sdf.format(s.endDate)
                    issueEntitlements.results.add([name:"${res.tipp.title.title} (${tenant}${dateString})",value:res.class.name+":"+res.id])
                }
            }
        }
        issueEntitlements
    }

    /**
     * Retrieves a list of licenses owned by the context organisation matching given parameters
     * @param params - eventual request params (currently not in use, handed for an eventual extension)
     * @return a map containing licenses, an empty one if no licenses match the filter
     */
    Map getLicenses(Map params) {
        Org org = contextService.getOrg()
        LinkedHashMap licenses = [results:[]]
        List<License> result = []
        String licFilter = ''
        LinkedHashMap filterParams = [org:org,orgRoles:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE,RDStore.OR_LICENSEE_CONS]]
        if(params.query && params.query.length() > 0) {
            licFilter = ' and l.reference like :query'
            filterParams.put('query',"%"+params.query+"%")
        }
        result = License.executeQuery('select l from License as l join l.orgLinks ol where ol.org = :org and ol.roleType in (:orgRoles)'+licFilter+" order by l.reference asc",filterParams)
        if(result.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))
            log.debug("licenses found")
            result.each { res ->
                licenses.results.add([name:"${res.reference} (${res.startDate ? sdf.format(res.startDate) : '???'} - ${res.endDate ? sdf.format(res.endDate) : ''})",value:res.class.name+":"+res.id])
            }
        }
        licenses
    }

    /**
     * Retrieves a list of issue entitlements owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a sorted list of issue entitlements, an empty one if no issue entitlements match the filter
     */
    Map getSubscriptionPackages(Map params) {
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale()))
        Org org = contextService.getOrg()
        LinkedHashMap result = [results:[]]
        String queryString = 'select distinct s, orgRoles.org.sortname from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in ( :orgRoles ) and s.status != :deleted'
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],deleted:RDStore.SUBSCRIPTION_DELETED]
        //may be generalised later - here it is where to expand the query filter
        if(params.query && params.query.length() > 0) {
            filter.put("query","%"+params.query+"%")
            queryString += " and (lower(s.name) like lower(:query) or lower(orgRoles.org.sortname) like lower(:query)) "
        }
        if(params.ctx) {
            Subscription ctx = genericOIDService.resolveOID(params.ctx)
            filter.ctx = ctx
            queryString += " and s = :ctx"
        }
        if(params.status) {
            filter.status = params.status
            queryString += " and s.status = :status "
        }
        List subscriptions = Subscription.executeQuery(queryString+" order by s.name asc, orgRoles.org.sortname asc, s.startDate asc, s.endDate asc",filter)
        subscriptions.each { row ->
            Subscription s = (Subscription) row[0]
            String tenant
            if(s.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_PARTICIPATION && s.getConsortia().id == org.id) {
                try {
                    tenant = s.getAllSubscribers().get(0).name
                }
                catch (IndexOutOfBoundsException e) {
                    log.debug("Please check subscription #${s.id}")
                }
            }
            else {
                tenant = org.name
            }
            if (((params.checkView && s.isVisibleBy(contextService.getUser())) || !params.checkView) && tenant != null) {
                s.packages.each { sp ->
                    String dateString = ", "
                    if (s.startDate)
                        dateString += sdf.format(s.startDate) + "-"
                    if (s.endDate)
                        dateString += sdf.format(s.endDate)
                    result.results.add([name:"${sp.pkg.name}/${s.name} (${tenant}${dateString})",value:sp.class.name + ":" + sp.id])
                }
            }
        }
        result
    }

    Map getBudgetCodes(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select bc from BudgetCode bc where bc.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",'%'+params.query+'%')
            queryString += " and lower(bc.value) like lower(:query) "
        }
        List budgetCodes = BudgetCode.executeQuery(queryString,filter)
        budgetCodes.each { bc ->
            result.results.add([name:bc.value,value:bc.id])
        }
        result
    }

    Map getInvoiceNumbers(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select i from Invoice i where i.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",'%'+params.query+'%')
            queryString += " and i.invoiceNumber like :query "
        }
        List invoiceNumbers = Invoice.executeQuery(queryString,filter)
        invoiceNumbers.each { inv ->
            result.results.add([name:inv.invoiceNumber,value:inv.invoiceNumber])
        }
        result
    }

    Map getOrderNumbers(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select ord from Order ord where ord.owner = :owner'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",'%'+params.query+'%')
            queryString += " and ord.orderNumber like :query "
        }
        List orderNumbers = Order.executeQuery(queryString,filter)
        orderNumbers.each { ord ->
            result.results.add([name:ord.orderNumber,value:ord.orderNumber])
        }
        result
    }

    Map getReferences(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        String queryString = 'select ci from CostItem ci where ci.owner = :owner and ci.reference != null'
        LinkedHashMap filter = [owner:org]
        if(params.query && params.query.length() > 0) {
            filter.put("query",'%'+params.query+'%')
            queryString += " and lower(ci.reference) like lower(:query) "
        }
        List references = CostItem.executeQuery(queryString,filter)
        references.each { r ->
            log.debug(r)
            result.results.add([name:r.reference,value:r.reference])
        }
        result
    }

    Map getElements(Map params) {
        Map result = [results:[]]
        Org org = contextService.getOrg()
        if(params.org == "true") {
            List allOrgs = DocContext.executeQuery('select dc.org from DocContext dc where dc.owner.owner = :ctxOrg and dc.org != null and (lower(dc.org.name) like lower(:query) or lower(dc.org.sortname) like lower(:query)) order by dc.org.sortname asc',[ctxOrg:org,query:"%${params.query}%"])
            allOrgs.each { it ->
                result.results.add([name:"(${messageSource.getMessage('spotlight.organisation',null,LocaleContextHolder.locale)}) ${it.name}",value:"${it.class.name}:${it.id}"])
            }
        }
        if(params.license == "true") {
            List allLicenses = DocContext.executeQuery('select dc.license from DocContext dc where dc.owner.owner = :ctxOrg and dc.license != null and lower(dc.license.reference) like lower(:query) order by dc.license.reference asc',[ctxOrg:org,query:"%${params.query}%"])
            allLicenses.each { it ->
                result.results.add([name:"(${messageSource.getMessage('spotlight.license',null,LocaleContextHolder.locale)}) ${it.reference}",value:"${it.class.name}:${it.id}"])
            }
        }
        if(params.subscription == "true") {
            List allSubscriptions = DocContext.executeQuery('select dc.subscription from DocContext dc where dc.owner.owner = :ctxOrg and dc.subscription != null and lower(dc.subscription.name) like lower(:query) order by dc.subscription.name asc',[ctxOrg:org,query:"%${params.query}%"])
            allSubscriptions.each { it ->
                result.results.add([name:"(${messageSource.getMessage('spotlight.subscription',null,LocaleContextHolder.locale)}) ${it.name}",value:"${it.class.name}:${it.id}"])
            }
        }
        if(params.pkg == "true") {
            List allPackages = DocContext.executeQuery('select dc.pkg from DocContext dc where dc.owner.owner = :ctxOrg and dc.pkg != null and lower(dc.pkg.name) like lower(:query) order by dc.pkg.name asc', [ctxOrg: org, query: "%${params.query}%"])
            allPackages.each { it ->
                result.results.add([name: "(${messageSource.getMessage('spotlight.package', null, LocaleContextHolder.locale)}) ${it.name}", value: "${it.class.name}:${it.id}"])
            }
        }
        result
    }
}
