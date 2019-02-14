package de.laser

import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.License
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
     * Retrieves a list of subscriptions owned by the context organisation matching given parameters
     * @param params - eventual request params
     * @return a map containing a sorted list of subscriptions, an empty one if no subscriptions match the filter
     * The map is necessary for the Select2 processing afterwards
     */
    Map getSubscriptions(Map params) {
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale()))
        Org org = contextService.getOrg()
        LinkedHashMap result = [values:[]]
        String ownQueryString = 'select new map(s as sub,orgRoles.org as org) from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType in ( :orgRoles ) and s.status != :deleted'
        String consQueryString = 'select new map(s as sub,orgRoles.org as org) from Subscription s join s.orgRelations orgRoles where orgRoles.org = :org and orgRoles.roleType = :orgRole  and s.status != :deleted and s.instanceOf = null'
        String childQueryString = 'select new map(s as sub,roleM.org as org) from Subscription s join s.instanceOf subC join subC.orgRelations roleC join s.orgRelations roleMC join s.orgRelations roleM where roleC.org = :org and roleC.roleType = :consType and roleMC.roleType = :consType and roleM.roleType = :subscrType and s.status != :deleted and subC.status != :deleted'
        LinkedHashMap filter = [org:org,deleted:RDStore.SUBSCRIPTION_DELETED]
        //may be generalised later - here it is where to expand the query filter
        if(params.q && params.q.length() > 0) {
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

    /**
     * Builds from the given list of objects a formatted list
     * @param objects - a list of objects to retrieve details from
     * @return a list which is formatted for dropdown menus
     */
    List buildSelectList(Collection objects,String className) {
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale()))
        List result = []
        objects.each { entry ->
            Subscription sub
            String packageString = ""
            String key
            switch(className) {
                case Subscription.class.name:
                    sub = (Subscription) entry
                    key = className + ":" + sub.id
                    break
                case SubscriptionPackage.class.name:
                    SubscriptionPackage sp = (SubscriptionPackage) entry
                    sub = (Subscription) sp.subscription
                    packageString = sp.pkg.name+"/"
                    key = className + ":" + sp.id
                    break
            }
            RefdataValue ownerType
            switch(sub.getCalculatedType()) {
                case TemplateSupport.CALCULATED_TYPE_CONSORTIAL: ownerType = RDStore.OR_SUBSCRIPTION_CONSORTIA
                    break
                case TemplateSupport.CALCULATED_TYPE_PARTICIPATION: ownerType = RDStore.OR_SUBSCRIBER_CONS
                    break
                default: ownerType = RDStore.OR_SUBSCRIBER
                    break
            }
            OrgRole owner = sub.orgRelations.find {org -> org.roleType.equals(ownerType)}
            String dateString = ", "
            if (sub.startDate)
                dateString += sdf.format(sub.startDate) + "-"
            if (sub.endDate)
                dateString += sdf.format(sub.endDate)
            result.add([id:key,text:packageString+sub.name+" ("+owner.org.name+dateString+")"])
            //log.info("${packageString}${sub.name} (${owner.org.name}${dateString})")
        }
        result.sort { x,y -> x.text.compareToIgnoreCase y.text }
        //result
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
