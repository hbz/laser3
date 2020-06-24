package de.laser

import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.Links
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class LinksGenerationService {

    GenericOIDService genericOIDService

    LinkedHashMap<String,List> generateNavigation(String contextOID) {
        List prevLink = []
        List nextLink = []
        List previous = Links.findAllBySourceAndLinkType(contextOID,RDStore.LINKTYPE_FOLLOWS)
        List next = Links.findAllByDestinationAndLinkType(contextOID,RDStore.LINKTYPE_FOLLOWS)
        if(previous.size() > 0) {
            previous.each { it ->
                def obj = genericOIDService.resolveOID(it.destination)
                prevLink.add(obj)
            }
        }
        else prevLink = null
        if(next.size() > 0) {
            next.each { it ->
                def obj = genericOIDService.resolveOID(it.source)
                nextLink.add(obj)
            }
        }
        else nextLink = null
        return [prevLink:prevLink,nextLink:nextLink]
    }

    Map<String,Object> getSourcesAndDestinations(obj,user) {
        Map<String,Object> links = [:]
        // links
        Set<Links> sources = Links.findAllBySource(GenericOIDService.getOID(obj))
        Set<Links> destinations = Links.findAllByDestination(GenericOIDService.getOID(obj))
        //IN is from the point of view of the context object (= obj)

        sources.each { link ->
            def destination = genericOIDService.resolveOID(link.destination)
            if (destination.respondsTo("isVisibleBy") && destination.isVisibleBy(user)) {
                def index = link.linkType.getI10n("value")?.split("\\|")[0]
                if (links[index] == null) {
                    links[index] = []
                }
                links[index].add(link)
            }
        }
        destinations.each { link ->
            def source = genericOIDService.resolveOID(link.source)
            if (source.respondsTo("isVisibleBy") && source.isVisibleBy(user)) {
                def index = link.linkType.getI10n("value")?.split("\\|")[1]
                if (links[index] == null) {
                    links[index] = []
                }
                links[index].add(link)
            }
        }
        links
    }

}
