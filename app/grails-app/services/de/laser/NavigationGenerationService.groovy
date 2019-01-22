package de.laser

import com.k_int.kbplus.Links
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class NavigationGenerationService {

    def grailsApplication

    LinkedHashMap<String,List> generateNavigation(String contextClassName, Long contextID) {
        List prevLink = []
        List nextLink = []
        List previous = Links.findAllBySourceAndLinkTypeAndObjectType(contextID, RDStore.LINKTYPE_FOLLOWS,contextClassName)
        List next = Links.findAllByDestinationAndLinkTypeAndObjectType(contextID,RDStore.LINKTYPE_FOLLOWS,contextClassName)
        def domainClass = grailsApplication.getArtefact('Domain', contextClassName)
        prevLink = previous ? previous.collect { it -> domainClass.getClazz().get(it.destination) } : null
        nextLink = next ? next.collect { it -> domainClass.getClazz().get(it.source)} : null
        return [prevLink:prevLink,nextLink:nextLink]
    }

    LinkedHashMap<String, List> generateNavigation(String contextClassName, String contextID) {
        return generateNavigation(contextClassName,Long.parseLong(contextID))
    }
}
