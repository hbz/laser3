package de.laser

import de.laser.finance.CostItem
import de.laser.helper.RDStore
import grails.converters.JSON
import grails.gorm.transactions.Transactional

import java.math.RoundingMode
import java.time.Year

@Transactional
class ReportingService {

    final static String CONFIG_LIBRARY_TYPE = 'libraryType'
    final static String CONFIG_SUBJECT_GROUP = 'subjectGroup'
    final static String CONFIG_REGION = 'region'
    final static String CONFIG_LIBRARY_NETWORK = 'libraryNetwork'

    //--------------------------------- general entry point -------------------------------------

    Map<String,Object> generateGrowth(Map<String,Object> configMap) {
        Map result = [:], params = [context:configMap.institution]
        String instanceFilter = ""
        if(configMap.institution.getCustomerType() == "ORG_CONSORTIUM"){
            params.roleTypes = [RDStore.OR_SUBSCRIPTION_CONSORTIA]
            instanceFilter += " and oo.sub.instanceOf = null"
        }
        else if(configMap.institution.getCustomerType() == "ORG_INST") {
            params.roleTypes = [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER]
        }
        configMap.allSubscriptions = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.roleType in (:roleTypes) and oo.org = :context"+instanceFilter,params)
        switch(configMap.requestParam) {
            case "members": result.putAll(generateGrowthForMembers(configMap))
                break
            default: log.info("unimplemented general request param")
                break
        }
        result
    }

    Map<Year,Map<String,Integer>> generateGrowthForMembers(Map<String,Object> configMap) {
        Map<String,Object> queryParams = [consortiaSubscriptions:configMap.allSubscriptions,roleType:RDStore.OR_SUBSCRIBER_CONS]
        Map<Year,Map<String,Integer>> result = [:]
        if(CONFIG_LIBRARY_TYPE in configMap.groupOptions) {

        }
        if(CONFIG_SUBJECT_GROUP in configMap.groupOptions) {

        }
        if(CONFIG_REGION in configMap.groupOptions) {
            List rows = OrgRole.executeQuery("select year(sub.startDate),region,count(org) from OrgRole oo join oo.sub sub join oo.org org join org.region region where oo.roleType = :roleType and sub.instanceOf in (:consortiaSubscriptions) group by year(sub.startDate),region order by year(sub.startDate) asc",queryParams)
            rows.each { row ->
                Map<String,Integer> regionCols = result.get(row[0].toString())
                if(!regionCols)
                    regionCols = [:]
                String region = row[1] ? row[1].getI10n("value") : "n/a"
                Integer count = regionCols.get(region) ?: 0
                count += row[2]
                regionCols.put(region,count)
                result.put(row[0].toString(),regionCols)
            }
        }
        if(CONFIG_LIBRARY_NETWORK in configMap.groupOptions) {

        }
        result
    }

    //--------------------------------- subscription entry point -------------------------------------

    Map<String,Object> groupCostItemsBySubscription(Map<String,Object> configMap) {
        Map<String,Object> options = configMap.options, result = [:]
        Set<Subscription> precedingYearRings = linksGenerationService.getSuccessionChain(configMap.entry,'source'),
                          followingYearRings = linksGenerationService.getSuccessionChain(configMap.entry,'destination'),
                          allYearRings = []
        allYearRings.addAll(precedingYearRings)
        allYearRings.add(configMap.entry)
        allYearRings.addAll(followingYearRings)
        Set<Org> allTimeSubscribers = subscriptionService.getAllTimeSubscribersForConsortiaSubscription(allYearRings)
        Map<String,Object> queryParams = [:]
        queryParams.subs = allYearRings
        queryParams.context = configMap.institution
        Set costItemRows = CostItem.executeQuery("select ci.sub,ci.costItemElementConfiguration,ci.costInBillingCurrency,ci.costItemElement from CostItem ci where ci.sub.instanceOf in (:subs) and ci.owner = :context",queryParams)
        Set<RefdataValue> allElements = RefdataValue.executeQuery("select ci.costItemElement from CostItem ci where ci.sub.instanceOf in (:subs) and ci.owner = :context",queryParams)
        options.displayConfiguration.each { String config ->
            Map<String,Object> yearRings = [:]
            if('subscriber' in options.group) {
                //the option subscriber as grouping option is only available for consortia
                allYearRings.each { Subscription parentSub ->
                    //level 1: Map<Subscription,Map>, subscription represents sub year ring
                    Set currentRingRows = costItemRows.findAll { row -> row[0].instanceOf.id == parentSub.id }
                    Map<String,Object> currentRing = [:]
                    allTimeSubscribers.each { Org subscriber ->
                        //calculate by: subscriber, subscription start date, element sign
                        String subSortName = subscriber.sortname
                        if(config == 'costItemDevelopment') {
                            def row = currentRingRows.find { row -> row[0].getSubscriber().id == subscriber.id }
                            BigDecimal costItemsSubscriber = currentRing.get(subSortName)
                            if(!costItemsSubscriber) {
                                costItemsSubscriber = new BigDecimal(0)
                                costItemsSubscriber.setScale(2, RoundingMode.HALF_EVEN)
                            }
                            if(row) {
                                switch(row[1]) {
                                    case RDStore.CIEC_POSITIVE: costItemsSubscriber += row[2]
                                        break
                                    case RDStore.CIEC_NEGATIVE: costItemsSubscriber -= row[2]
                                        break
                                    default: log.debug(row[1])
                                        break
                                }
                            }
                            currentRing.put(subSortName,costItemsSubscriber)
                        }
                        else if(config == 'costItemDivision') {
                            allElements.each { RefdataValue element ->
                                def row = currentRingRows.find { row -> row[0].getSubscriber().id == subscriber.id && row[3].id == element.id }
                                String elementName = element.getI10n("value")
                                Map costsForElement = currentRing.get(elementName)
                                if(!costsForElement)
                                    costsForElement = [:]
                                BigDecimal costForSubscriberWithElement = new BigDecimal(0)
                                costForSubscriberWithElement.setScale(2, RoundingMode.HALF_EVEN)
                                if(row) {
                                    costForSubscriberWithElement = row[2]
                                }
                                costsForElement.put(subSortName,costForSubscriberWithElement)
                                currentRing.put(elementName,costsForElement)
                            }
                        }
                    }
                    yearRings.put(parentSub.dropdownNamingConvention(configMap.institution),currentRing)
                }
            }
            result[config] = yearRings as JSON
        }
        result
    }

    BigDecimal calculateSum(Collection<CostItem> allCIs) {
        BigDecimal result = 0.0
        allCIs.each { CostItem ci ->
            switch(ci.costItemElementConfiguration) {
                case RDStore.CIEC_POSITIVE: result += ci.costInBillingCurrency
                    break
                case RDStore.CIEC_NEGATIVE: result -= ci.costInBillingCurrency
                    break
            }
        }
        result
    }
}
