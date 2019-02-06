package de.laser

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.Org
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
     * Retrieves a list of subscriptions owned by the context organisation matching given
     * parameters
     * @param params - eventual request params
     * @return a list of subscriptions, an empty one if no subscriptions match the filter
     */
    Map getSubscriptions(GrailsParameterMap params) {
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale()))
        Org org = contextService.getOrg()
        LinkedHashMap result = [values:[]]
        String ownQueryString = 'select new map(s as sub,orgRoles.org as org) from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in ( :orgRoles ) and s.status != :deleted'
        String consQueryString = 'select new map(s as sub,orgRoles.org as org) from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType = :orgRole  and s.status != :deleted and s.instanceOf = null'
        String childQueryString = 'select new map(s as sub,roleM.org as org) from Subscription s join s.instanceOf subC join subC.orgRelations roleC join s.orgRelations roleMC join s.orgRelations roleM where roleC.org = :org and roleC.roleType = :consType and roleMC.roleType = :consType and roleM.roleType = :subscrType and s.status != :deleted and subC.status != :deleted'
        LinkedHashMap filter = [org:org,deleted:RDStore.SUBSCRIPTION_DELETED]
        //may be generalised later - here it is where to expand the query filter
        if(params.q.length() > 0) {
            filter.put("query","%"+params.q+"%")
            ownQueryString += " and s.name like :query"
            consQueryString += " and s.name like :query"
            childQueryString += " and s.name like :query"
        }
        if(params.ctx) {
            Subscription ctx = genericOIDService.resolveOID(params.ctx)
            filter.ctx = ctx
            ownQueryString += " and s != :ctx"
            consQueryString += " and s != :ctx"
            childQueryString += " and s != :ctx"
        }
        List<Map> ownSubscriptions = Subscription.executeQuery(ownQueryString,filter+[orgRoles:[RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS]])
        List<Map> consSubscriptions = Subscription.executeQuery(consQueryString,filter+[orgRole:RDStore.OR_SUBSCRIPTION_CONSORTIA])
        List<Map> childSubscriptions = Subscription.executeQuery(childQueryString,filter+[consType:RDStore.OR_SUBSCRIPTION_CONSORTIA,subscrType:RDStore.OR_SUBSCRIBER_CONS])
        List subscriptions = []
        subscriptions.addAll(ownSubscriptions)
        subscriptions.addAll(consSubscriptions)
        subscriptions.addAll(childSubscriptions)
        subscriptions.each { entry ->
            Subscription s = entry.sub
            Org owner = entry.org
            if ((params.checkView && s.isVisibleBy(contextService.getUser())) || !params.checkView) {
                String dateString = ", "
                if (s.startDate)
                    dateString += sdf.format(s.startDate) + "-"
                if (s.endDate)
                    dateString += sdf.format(s.endDate)
                result.values.add([id:s.class.name + ":" + s.id,text:"${s.name} (${owner.name}${dateString})"])
            }
        }
        result.values.sort { x,y -> x.text.compareToIgnoreCase y.text }
        result
    }


    LinkedHashMap getIssueEntitlements(GrailsParameterMap params) {
        Org org = contextService.getOrg()
        LinkedHashMap issueEntitlements = [values:[]]
        List<IssueEntitlement> result = []
        //build up set of subscriptions which are owned by the current organisation or instances of such - or filter for a given subscription
        String subFilter = 'in ( select s from Subscription as s where s in (select o.sub from OrgRole as o where o.org = :org and o.roleType in ( :orgRoles ) ) and s.status = :current )'
        LinkedHashMap filterParams = ['org':org, 'orgRoles': [RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS], 'current':RDStore.SUBSCRIPTION_CURRENT]
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

}
