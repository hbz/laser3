package de.laser

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore
import grails.transaction.Transactional
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ControlledListService {

    def contextService
    def genericOIDService
    def messageSource

    /**
     * Retrieves a list of subscriptions owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a sorted list of subscriptions, an empty one if no subscriptions match the filter
     * The map is necessary for the Select2 processing afterwards
     */
    Map getSubscriptions(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap result = [values:[]]
        String queryString = "select s from Subscription as s where s in (select o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) )"
        LinkedHashMap filter = [org:org,orgRoles:[RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIPTION_CONSORTIA]]
        //may be generalised later - here it is where to expand the query filter
        if(params.q.length() > 0) {
            filter.put("query","%${params.q}%")
            queryString += " and s.name like :query"
        }
        List<Subscription> subscriptions = Subscription.executeQuery(queryString,filter)
        if(subscriptions.size() > 0) {
            log.info("subscriptions found")
            subscriptions.each { s ->
                if((params.checkView && s.isVisibleBy(contextService.getUser())) || !params.checkView) {
                    OrgRole owner = OrgRole.findBySub(s)
                    result.values.add([id:s.class.name+":"+s.id,sortKey:s.name,text:"#${s.id}: ${s.name} (${owner.org.name})"])
                }
            }
            result.values.sort{ x,y -> x.sortKey.compareToIgnoreCase y.sortKey }
        }
        result
    }

    /**
     * Retrieves a list of issue entitlements owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a list of issue entitlements, an empty one if no issue entitlements match the filter
     * The map is necessary for the Select2 processing afterwards
     */
    Map getIssueEntitlements(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap issueEntitlements = [values:[]]
        List<IssueEntitlement> result = []
        //build up set of subscriptions which are owned by the current organisation or instances of such - or filter for a given subscription
        String subFilter = 'in ( select s from Subscription as s where s in (select o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) ) and s.status = :current )'
        LinkedHashMap filterParams = [org:org, orgRoles: [RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS], current:RDStore.SUBSCRIPTION_CURRENT]
        if(params.sub) {
            subFilter = '= :sub'
            filterParams = ['sub':genericOIDService.resolveOID(params.sub)]
        }
        filterParams.put('query',params.q)
        result = IssueEntitlement.executeQuery('select ie from IssueEntitlement as ie where ie.subscription '+subFilter+' and ie.tipp.title.title like :query',filterParams)
        if(result.size() > 0) {
            log.debug("issue entitlements found")
            result.each { res ->
                issueEntitlements.values.add([id:res.class.name+":"+res.id,text:res.tipp.title.title])
            }
            issueEntitlements.values.sort{ x,y -> x.text.compareToIgnoreCase y.text  }
        }
        issueEntitlements
    }

    /**
     * Retrieves a list of licenses owned by the context organisation matching given parameters
     * @param params - eventual request params (currently not in use, handed for an eventual extension)
     * @return a map containing licenses, an empty one if no licenses match the filter
     * The map is necessary for the Select2 processing afterwards
     */
    Map getLicenses(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap licenses = [values:[]]
        List<License> result = []
        String licFilter = ''
        LinkedHashMap filterParams = [org:org,orgRoles:[RDStore.OR_LICENSING_CONSORTIUM,RDStore.OR_LICENSEE,RDStore.OR_LICENSEE_CONS]]
        if(params.q) {
            licFilter = ' and l.reference like :query'
            filterParams.put('query',"%"+params.q+"%")
        }
        result = License.executeQuery('select l from License as l where l in (select o.lic from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ))'+licFilter,filterParams)
        if(result.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))
            log.debug("licenses found")
            result.each { res ->
                licenses.values.add([id:res.class.name+":"+res.id,text:"${res.reference} (${res.startDate ? sdf.format(res.startDate) : '???'} - ${res.endDate ? sdf.format(res.endDate) : ''})"])
            }
            licenses.values.sort{x,y -> x.text.compareToIgnoreCase y.text }
        }
        licenses
    }

}
