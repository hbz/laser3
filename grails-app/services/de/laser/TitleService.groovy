package de.laser

import de.laser.storage.RDStore
import de.laser.wekb.TitleInstancePackagePlatform
import grails.gorm.transactions.Transactional

@Transactional
class TitleService {

    FilterService filterService

    Map<String, Object> getCounts(Set<Long> tippIDs) {
        int currentTippCounts = 0, plannedTippCounts = 0, expiredTippCounts = 0, deletedTippCounts = 0, allTippCounts = 0
        tippIDs.collate(65000).each { List<Long> subset ->
            List counts = TitleInstancePackagePlatform.executeQuery('select count(*), rv.id from TitleInstancePackagePlatform tipp join tipp.status rv where tipp.id in (:subset) group by rv.id', [subset: subset])
            counts.each { row ->
                switch(row[1]) {
                    case RDStore.TIPP_STATUS_CURRENT.id: currentTippCounts += row[0]
                        break
                    case RDStore.TIPP_STATUS_EXPECTED.id: plannedTippCounts += row[0]
                        break
                    case RDStore.TIPP_STATUS_RETIRED.id: expiredTippCounts += row[0]
                        break
                    case RDStore.TIPP_STATUS_DELETED.id: deletedTippCounts += row[0]
                        break
                }
                allTippCounts += row[0]
            }
        }
        [currentTippCounts: currentTippCounts, plannedTippCounts: plannedTippCounts, expiredTippCounts: expiredTippCounts, deletedTippCounts: deletedTippCounts, allTippCounts: allTippCounts]
    }

    Set<Long> getKeys(Map configMap) {
        Map<String, Object> parameterGenerics = getParameterGenerics(configMap)
        Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                            identifierConfigMap = parameterGenerics.identifierConfigMap
        if(configMap.filter) {
            titleConfigMap.filter = configMap.filter
        }
        if(!configMap.containsKey('status') && !configMap.containsKey('forCount')) {
            titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT.id
        }
        else if(!configMap.containsKey('forCount')) {
            titleConfigMap.tippStatus = configMap.status
        }
        //process here the title-related parameters
        Map<String, Object> queryPart1 = filterService.getTippSubsetQuery(titleConfigMap)
        Set<Long> tippIDs = TitleInstancePackagePlatform.executeQuery(queryPart1.query, queryPart1.queryParams)
        if(configMap.identifier) {
            tippIDs = tippIDs.intersect(getTippsByIdentifier(identifierConfigMap, configMap.identifier))
        }
        tippIDs
    }

    Set<Long> getTippsByIdentifier(Map identifierConfigMap, String identifier) {
        identifierConfigMap.identifier = "%${identifier.toLowerCase()}%"
        TitleInstancePackagePlatform.executeQuery('select tipp.id from Identifier id join id.tipp tipp where tipp.pkg in (:packages) and lower(id.value) like :identifier and id.ns.ns in (:titleNS) and id.ns.nsType = :titleObj', identifierConfigMap)
    }

    Map<String, Object> getParameterGenerics(configMap) {
        String sort = configMap.containsKey('sort') && configMap.sort ? configMap.sort : 'tipp.sortname'
        String order = configMap.containsKey('order') && configMap.order ? configMap.order : 'asc'
        Map<String, Object> titleConfigMap = [filter: configMap.filter, packages: configMap.packages, platforms: configMap.platforms, ddcs: configMap.ddcs, languages: configMap.languages,
                                              subject_references: configMap.subject_references, series_names: configMap.series_names, summaryOfContent: configMap.summaryOfContent,
                                              ebookFirstAutorOrFirstEditor: configMap.ebookFirstAutorOrFirstEditor, dateFirstOnlineFrom: configMap.dateFirstOnlineFrom, openAccess: configMap.openAccess,
                                              dateFirstOnlineTo: configMap.dateFirstOnlineFrom, yearsFirstOnline: configMap.yearsFirstOnline, publishers: configMap.publishers,
                                              coverageDepth: configMap.coverageDepth, title_types: configMap.title_types, medium: configMap.medium, sort: sort, order: order],
                            identifierConfigMap = [identifier: configMap.identifier, packages: configMap.packages, titleNS: IdentifierNamespace.CORE_TITLE_NS, titleObj: IdentifierNamespace.NS_TITLE] //, sort: sort, order: order
        [titleConfigMap: titleConfigMap, identifierConfigMap: identifierConfigMap]
    }
}
