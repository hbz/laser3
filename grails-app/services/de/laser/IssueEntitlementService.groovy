package de.laser

import de.laser.helper.Params
import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional

@Transactional
class IssueEntitlementService {

    FilterService filterService

    Subscription getTargetSubscription(Subscription s) {
        if(s.instanceOf && s.holdingSelection == RDStore.SUBSCRIPTION_HOLDING_ENTIRE)
            s.instanceOf
        s
    }

    Set<IssueEntitlement> getIssueEntitlements(Map configMap) {
        Map<String, Object> titleConfigMap = [packages: configMap.packages],
                       identifierConfigMap = [packages: configMap.packages],
                issueEntitlementConfigMap = [subscription: configMap.subscription]
        if(configMap.filter) {
            titleConfigMap.filter = configMap.filter
        }
        if(configMap.identifier) {
            identifierConfigMap.identifier = configMap.identifier
        }
        if(!configMap.containsKey('status')) {
            //titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT //activate if needed
            issueEntitlementConfigMap.ieStatus = RDStore.TIPP_STATUS_CURRENT
        }
        else {
            issueEntitlementConfigMap.ieStatus = Params.getRefdataList(configMap, 'status')
        }
        //process here the title-related parameters
        Map<String, Object> queryPart1 = filterService.getTippSubsetQuery(titleConfigMap)
        Set<Long> tippIds = TitleInstancePackagePlatform.executeQuery(queryPart1.query, queryPart1.queryParams), ieIds = []
        if(configMap.identifier)
            tippIds.addAll(TitleInstancePackagePlatform.executeQuery('select tipp.id from Identifier id join id.tipp tipp where tipp.pkg in (:packages) and lower(id.value) like :identifier', identifierConfigMap))
        //process here the issue entitlement-related parameters
        Map<String, Object> queryPart3 = filterService.getIssueEntitlementSubsetQuery(issueEntitlementConfigMap)

        tippIds.collate(65000).each { List<Long> subset ->
            queryPart3.queryParams.subset = subset
            ieIds.addAll(IssueEntitlement.executeQuery(queryPart3.query, queryPart3.queryParams))
        }
        //test A
        SortedSet<IssueEntitlement> result = new TreeSet<IssueEntitlement>()
        Set<Long> ieSubset = ieIds.drop(0).take(10)
        result.addAll(IssueEntitlement.findAllByIdInList(ieSubset))
        //test B: test for export
        /*
        Set<IssueEntitlement> result = []
        ieIds.eachWithIndex { Long ieId, int i ->
            println "processing entry ${i}"
            result << IssueEntitlement.get(ieId)
        }
        */
        result
    }
}
