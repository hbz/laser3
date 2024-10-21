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
        else s
    }

    Map<String, Object> getIssueEntitlements(Map configMap) {
        Map<String, Object> parameterGenerics = getParameterGenerics(configMap)
        Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                            identifierConfigMap = parameterGenerics.identifierConfigMap,
                            issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
        if(configMap.filter) {
            titleConfigMap.filter = configMap.filter
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
        if(configMap.identifier) {
            identifierConfigMap.identifier = "%${configMap.identifier.toLowerCase()}%"
            Set<Long> identifierMatches = TitleInstancePackagePlatform.executeQuery('select tipp.id from Identifier id join id.tipp tipp where tipp.pkg in (:packages) and lower(id.value) like :identifier and id.ns.ns in (:titleNS)', identifierConfigMap)
            tippIds = tippIds.intersect(identifierMatches)
        }
        //process here the issue entitlement-related parameters
        Map<String, Object> queryPart2 = filterService.getIssueEntitlementSubsetQuery(issueEntitlementConfigMap)

        tippIds.collate(65000).each { List<Long> subset ->
            queryPart2.queryParams.subset = subset
            ieIds.addAll(IssueEntitlement.executeQuery(queryPart2.query, queryPart2.queryParams))
        }
        SortedSet<IssueEntitlement> result = new TreeSet<IssueEntitlement>()
        Set<Long> ieSubset = ieIds.drop(configMap.offset).take(configMap.max)
        result.addAll(IssueEntitlement.findAllByIdInList(ieSubset))
        //test B: test for export
        /*
        Set<IssueEntitlement> result = []
        ieIds.eachWithIndex { Long ieId, int i ->
            println "processing entry ${i}"
            result << IssueEntitlement.get(ieId)
        }
        */
        if(configMap.containsKey('fileformat') || configMap.containsKey('exportKBart')) {
            [entIDs: ieSubset]
        }
        else [entitlements: result, num_ies_rows: ieIds.size()]
    }

    Map<String, Object> getParameterGenerics(configMap) {
        Map<String, Object> titleConfigMap = [packages: configMap.packages, platforms: configMap.platforms, ddcs: configMap.ddcs, languages: configMap.languages,
                                              subject_references: configMap.subject_references, series_names: configMap.series_names, summaryOfContent: configMap.summaryOfContent,
                                              ebookFirstAutorOrFirstEditor: configMap.ebookFirstAutorOrFirstEditor, dateFirstOnlineFrom: configMap.dateFirstOnlineFrom,
                                              dateFirstOnlineTo: configMap.dateFirstOnlineFrom, yearsFirstOnline: configMap.yearsFirstOnline, publishers: configMap.publishers,
                                              coverageDepth: configMap.coverageDepth, title_types: configMap.title_types, medium: configMap.medium],
                            identifierConfigMap = [packages: configMap.packages, titleNS: IdentifierNamespace.CORE_TITLE_NS],
                            issueEntitlementConfigMap = [subscription: configMap.subscription, asAt: configMap.asAt, hasPerpetualAccess: configMap.hasPerpetualAccess, titleGroup: configMap.titleGroup]
        [titleConfigMap: titleConfigMap, identifierConfigMap: identifierConfigMap, issueEntitlementConfigMap: issueEntitlementConfigMap]
    }
}
