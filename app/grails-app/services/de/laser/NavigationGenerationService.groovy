package de.laser

import com.k_int.kbplus.Links
import com.k_int.kbplus.Subscription
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
        if(previous.size() > 0) {
            previous.each { it ->
                def obj = domainClass.getClazz().get(it.destination)
                if(obj instanceof Subscription) {
                    if(!obj.status.equals(RDStore.SUBSCRIPTION_DELETED))
                        prevLink.add(obj)
                }
            }
        }
        else prevLink = null
        if(next.size() > 0) {
            next.each { it ->
                def obj = domainClass.getClazz().get(it.source)
                if(obj instanceof Subscription) {
                    if(!obj.status.equals(RDStore.SUBSCRIPTION_DELETED))
                        nextLink.add(obj)
                }
            }
        }
        else nextLink = null
        return [prevLink:prevLink,nextLink:nextLink]
    }

    LinkedHashMap<String, List> generateNavigation(String contextClassName, String contextID) {
        return generateNavigation(contextClassName,Long.parseLong(contextID))
    }
}
