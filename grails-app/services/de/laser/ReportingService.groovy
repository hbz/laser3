package de.laser

import de.laser.finance.CostItem
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import grails.converters.JSON
import grails.gorm.transactions.Transactional

import java.math.RoundingMode

@Transactional
class ReportingService {

    LinksGenerationService linksGenerationService
    SubscriptionService subscriptionService

    final static String CONFIG_LIBRARY_TYPE = 'libraryType'
    final static String CONFIG_SUBJECT_GROUP = 'subjectGroup'
    final static String CONFIG_REGION = 'region'
    final static String CONFIG_LIBRARY_NETWORK = 'libraryNetwork'
    final static String CONFIG_ORG_PROPERTY = 'property'

    //--------------------------------- general entry point -------------------------------------

    Map<String,Object> generateGrowth(Map<String,Object> configMap) {
        Map<String,Object> result = [:], params = [context:configMap.institution]
        String instanceFilter = ""
        if(configMap.institution.getCustomerType() == "ORG_CONSORTIUM"){
            params.roleTypes = [RDStore.OR_SUBSCRIPTION_CONSORTIA]
            instanceFilter += " and oo.sub.instanceOf = null"
        }
        else if(configMap.institution.getCustomerType() == "ORG_INST") {
            params.roleTypes = [RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER]
        }
        configMap.allSubscriptions = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.roleType in (:roleTypes) and oo.org = :context"+instanceFilter,params)
        configMap.roleTypes = params.roleTypes
        //log.debug(configMap.allSubscriptions.collect{ it.id })
        switch(configMap.requestParam) {
            case "members": result.putAll(generateGrowthForMembers(configMap))
                break
            case "costs": result.putAll(generateGrowthForCosts(configMap))
                break
            case "subscriptions": result.putAll(generateGrowthForSubscriptions(configMap))
                break
            default: log.info("unimplemented general request param")
                break
        }
        result
    }

    Map<String,JSON> generateGrowthForMembers(Map<String,Object> configMap) {
        Map<String,Object> queryParams = [consortiaSubscriptions:configMap.allSubscriptions,roleType:RDStore.OR_SUBSCRIBER_CONS]
        Map<String,JSON> result = [:]
        configMap.groupOptions.each { String groupCol ->
            List rows
            PropertyDefinition propDef
            if(groupCol == CONFIG_ORG_PROPERTY && configMap.propDef) {
                propDef = (PropertyDefinition) configMap.propDef
                queryParams.propDef = propDef
                Map<String,String> propDefData = setPropDefData(propDef)
                if(propDefData.containsKey("valueCol"))
                    rows = OrgRole.executeQuery("select year(sub.startDate),"+propDefData.valueCol+",count(distinct org) from OrgRole oo join oo.sub sub join oo.org org join org.propertySet op join op.type propDef "+propDefData.joinRdv+" where oo.roleType = :roleType and sub.instanceOf in (:consortiaSubscriptions) and propDef = :propDef group by year(sub.startDate),"+propDefData.valueCol+" order by year(sub.startDate) asc", queryParams)
            }
            else if(groupCol != CONFIG_ORG_PROPERTY) {
                rows = OrgRole.executeQuery("select year(sub.startDate)," + groupCol + ",count(distinct org) from OrgRole oo join oo.sub sub join oo.org org join org." + groupCol + " " + groupCol + " where oo.roleType = :roleType and sub.instanceOf in (:consortiaSubscriptions) group by year(sub.startDate)," + groupCol + " order by year(sub.startDate) asc", queryParams)
            }
            if(rows) {
                String key = "members${groupCol}"
                if(propDef)
                    key += propDef.id
                result.put(groupCol, collectCounts(rows))
            }
        }
        result
    }

    Map<String,JSON> generateGrowthForCosts(Map<String,Object> configMap) {
        Map<String,Object> queryParams = [allSubscriptions:configMap.allSubscriptions,roleTypes:[],element:RDStore.COST_ITEM_ELEMENT_CONSORTIAL_PRICE]
        Map<String,JSON> result = [:]
        String subscriptionSet = ""
        if(configMap.institution.getCustomerType() == "ORG_CONSORTIUM") {
            subscriptionSet += "and sub.instanceOf in (:allSubscriptions)"
            queryParams.roleTypes << RDStore.OR_SUBSCRIBER_CONS
        }
        else if(configMap.institution.getCustomerType() == "ORG_INST") {
            subscriptionSet += "and sub in (:allSubscriptions)"
            queryParams.roleTypes.addAll([RDStore.OR_SUBSCRIBER,RDStore.OR_SUBSCRIBER_CONS])
        }
        configMap.groupOptions.each { String groupCol ->
            Map<String,Map<String,BigDecimal>> graph = [:]
            List rows
            PropertyDefinition propDef
            //for this next batch, we should consider only consortial price. Other elements need different tables, otherwise, it would explode the whole view
            if(groupCol == CONFIG_ORG_PROPERTY) {
                propDef = (PropertyDefinition) configMap.propDef
                queryParams.propDef = propDef
                Map<String,String> propDefData = setPropDefData(propDef)
                if(propDefData.containsKey("valueCol"))
                    rows = CostItem.executeQuery("select year(sub.startDate),"+propDefData.valueCol+",sum(ci.costInLocalCurrency) from CostItem ci join ci.sub sub join sub.orgRelations oo join oo.org org join org.propertySet op join op.type propDef "+propDefData.joinRdv+" where oo.roleType in (:roleTypes) and ci.costItemElement = :element "+subscriptionSet+" and propDef = :propDef group by year(sub.startDate),"+propDefData.valueCol+" order by year(sub.startDate) asc",queryParams)
            }
            else {
                rows = CostItem.executeQuery("select year(sub.startDate),"+groupCol+",sum(ci.costInLocalCurrency) from CostItem ci join ci.sub sub join sub.orgRelations oo join oo.org org join org."+groupCol+" "+groupCol+" where oo.roleType in (:roleTypes) and ci.costItemElement = :element "+subscriptionSet+" group by year(sub.startDate),"+groupCol+" order by year(sub.startDate) asc",queryParams)
            }
            if(rows) {
                Set allGroups = []
                rows.each { row ->
                    if(row[1] instanceof RefdataValue) {
                        RefdataValue rdv = (RefdataValue) row[1]
                        allGroups << rdv
                        Map<String,Integer> groupCounts = graph.get(row[0].toString())
                        if(!groupCounts)
                            groupCounts = [:]
                        String group = rdv ? rdv.getI10n("value") : "n/a"
                        BigDecimal count = groupCounts.get(group) ?: new BigDecimal(0)
                        count.setScale(2,RoundingMode.HALF_EVEN)
                        count += row[2]
                        groupCounts.put(group,count)
                        graph.put(row[0].toString(),groupCounts)
                    }
                    if(row[1] instanceof OrgSubjectGroup) {
                        OrgSubjectGroup osg = (OrgSubjectGroup) row[1]
                        allGroups << osg.subjectGroup
                        Map<String,BigDecimal> groupCounts = graph.get(row[0].toString())
                        if(!groupCounts)
                            groupCounts = [:]
                        String group = osg ? osg.subjectGroup.getI10n("value") : "n/a"
                        BigDecimal count = groupCounts.get(group) ?: new BigDecimal(0)
                        count.setScale(2,RoundingMode.HALF_EVEN)
                        count += row[2]
                        groupCounts.put(group,count)
                        graph.put(row[0].toString(),groupCounts)
                    }
                }
                //fill up null values
                graph.each { String k, Map<String,Integer> v ->
                    allGroups.each { group ->
                        if(!v.containsKey(group.getI10n("value")))
                            v.put(group.getI10n("value"),0)
                    }
                }
                String key = "costs${groupCol}"
                if(propDef)
                    key += propDef.id
                result.put(groupCol,graph as JSON)
            }
        }
        result
    }

    Map<String,JSON> generateGrowthForSubscriptions(Map<String,Object> configMap) {
        Map<String,Object> queryParams = [consortiaSubscriptions:configMap.allSubscriptions,roleType:RDStore.OR_SUBSCRIBER_CONS]
        Map<String,JSON> result = [:]
        List rows
        PropertyDefinition propDef
        configMap.groupOptions.each { String groupCol ->
            if(groupCol == CONFIG_ORG_PROPERTY) {
                propDef = (PropertyDefinition) configMap.propDef
                queryParams.propDef = propDef
                Map<String,String> propDefData = setPropDefData(propDef)
                if(propDefData.containsKey("valueCol"))
                    rows = OrgRole.executeQuery("select year(sub.startDate)," + propDefData.valueCol + ",count(sub) from OrgRole oo join oo.sub sub join oo.org org join org.propertySet op join op.type propDef " + propDefData.joinRdv + " where oo.roleType = :roleType and sub.instanceOf in (:consortiaSubscriptions) and propDef = :propDef group by year(sub.startDate)," + propDefData.valueCol + " order by year(sub.startDate) asc", queryParams)
            }
            else {
                rows = OrgRole.executeQuery("select year(sub.startDate)," + groupCol + ",count(sub) from OrgRole oo join oo.sub sub join oo.org org join org." + groupCol + " " + groupCol + " where oo.roleType = :roleType and sub.instanceOf in (:consortiaSubscriptions) group by year(sub.startDate)," + groupCol + " order by year(sub.startDate) asc", queryParams)
            }
            if(rows) {
                String key = "subs${groupCol}"
                if(propDef)
                    key += propDef.id
                result.put(groupCol, collectCounts(rows))
            }
        }
        result
    }

    //--------------------------------- subscription entry point -------------------------------------

    Map<String,Object> groupCostItemsBySubscription(Map<String,Object> configMap) {
        Map<String,Object> options = configMap.options, result = [:]
        Set<Subscription> precedingYearRings = linksGenerationService.getSuccessionChain(configMap.entry,'sourceSubscription'),
                          followingYearRings = linksGenerationService.getSuccessionChain(configMap.entry,'destinationSubscription'),
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

    //---------------------------------- helper section -------------------------------

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

    Map<String,String> setPropDefData(PropertyDefinition propDef) {
        String valueCol, joinRdv = ""
        if (propDef.isIntegerType()) {
            valueCol = "op.intValue"
        }
        else if (propDef.isStringType()) {
            valueCol = "op.stringValue"
        }
        else if (propDef.isBigDecimalType()) {
            valueCol = "op.decValue"
        }
        else if (propDef.isDateType()) {
            valueCol = "op.dateValue"
        }
        else if (propDef.isURLType()) {
            valueCol = "op.urlValue"
        }
        else if (propDef.isRefdataValueType()) {
            valueCol = "value"
            joinRdv += "join op.refValue value"
        }
        if(valueCol)
            [valueCol:valueCol,joinRdv:joinRdv]
        else [:]
    }

    JSON collectCounts(List rows) {
        Map<String,Map<String,Integer>> graph = [:]
        Set allGroups = []
        rows.each { row ->
            if(row[1] instanceof RefdataValue) {
                RefdataValue rdv = (RefdataValue) row[1]
                allGroups << rdv
                Map<String,Integer> groupCounts = graph.get(row[0].toString())
                if(!groupCounts)
                    groupCounts = [:]
                String group = rdv ? rdv.getI10n("value") : "n/a"
                Integer count = groupCounts.get(group) ?: 0
                count += row[2]
                groupCounts.put(group,count)
                graph.put(row[0].toString(),groupCounts)
            }
            if(row[1] instanceof OrgSubjectGroup) {
                OrgSubjectGroup osg = (OrgSubjectGroup) row[1]
                allGroups << osg.subjectGroup
                Map<String,Integer> groupCounts = graph.get(row[0].toString())
                if(!groupCounts)
                    groupCounts = [:]
                String group = osg ? osg.subjectGroup.getI10n("value") : "n/a"
                Integer count = groupCounts.get(group) ?: 0
                count += row[2]
                groupCounts.put(group,count)
                graph.put(row[0].toString(),groupCounts)
            }
        }
        //fill up null values
        graph.each { String k, Map<String,Integer> v ->
            allGroups.each { group ->
                if (!v.containsKey(group.getI10n("value")))
                    v.put(group.getI10n("value"), 0)
            }
        }
        graph as JSON
    }
}
