package de.laser

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.GenericOIDService
import com.k_int.kbplus.License
import com.k_int.kbplus.Links
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.Subscription
import de.laser.exceptions.CreationException
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.transaction.Transactional
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class LinksGenerationService {

    GenericOIDService genericOIDService
    def messageSource

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
        Map<String,Set<Links>> links = [:]
        // links
        Set<Links> sources = Links.findAllBySource(GenericOIDService.getOID(obj))
        Set<Links> destinations = Links.findAllByDestination(GenericOIDService.getOID(obj))
        //IN is from the point of view of the context object (= obj)

        sources.each { Links link ->
            def destination = genericOIDService.resolveOID(link.destination)
            if (destination.respondsTo("isVisibleBy") && destination.isVisibleBy(user)) {
                String index = GenericOIDService.getOID(link.linkType)
                if (links[index] == null) {
                    links[index] = []
                }
                links[index].add(link)
            }
        }
        destinations.each { Links link ->
            def source = genericOIDService.resolveOID(link.source)
            if (source.respondsTo("isVisibleBy") && source.isVisibleBy(user)) {
                String index = GenericOIDService.getOID(link.linkType)
                if (links[index] == null) {
                    links[index] = []
                }
                links[index].add(link)
            }
        }
        links
    }

    /**
     * connects the context object with the given pair.
     *
     * @return false if manipulation was successful, a string describing the error otherwise
     */
    def createOrUpdateLink(Map<String,Object> configMap) {
        def errors = false

        Doc linkComment
        if(configMap.comment instanceof Doc)
            linkComment = (Doc) configMap.comment
        Links link
        if(configMap.link instanceof Links)
            link = (Links) configMap.link
        else if(!configMap.link) {
            try {
                link = Links.construct(configMap)
                if(configMap.contextInstances) {
                    def sourceObj = genericOIDService.resolveOID(configMap.source), destObj = genericOIDService.resolveOID(configMap.destination)
                    configMap.contextInstances.each { contextInstance ->
                        def pairChild
                        if(contextInstance instanceof Subscription) {
                            pairChild = (Subscription) configMap.pairInstances.find { child -> child.getSubscriber() == contextInstance.getSubscriber() }
                        }
                        else if(contextInstance instanceof License) {
                            pairChild = (License) configMap.pairInstances.find { child -> child.getLicensee() == contextInstance.getLicensee() }
                        }
                        if(pairChild) {
                            Map<String,Object> childConfigMap = [linkType:configMap.linkType,owner:configMap.owner]
                            if(contextInstance.instanceOf == sourceObj) {
                                childConfigMap.source = GenericOIDService.getOID(contextInstance)
                                childConfigMap.destination = GenericOIDService.getOID(pairChild)
                            }
                            else if(contextInstance.instanceOf == destObj) {
                                childConfigMap.source = GenericOIDService.getOID(pairChild)
                                childConfigMap.destination = GenericOIDService.getOID(contextInstance)
                            }
                            Links.construct(childConfigMap)
                        }
                    }
                }
            }
            catch (CreationException e) {
                log.error( e.toString() )
                errors = messageSource.getMessage('default.linking.savingError',null, LocaleContextHolder.getLocale())
            }
        }

        if(link) {
            if(linkComment) {
                if(configMap.commentContent.length() > 0) {
                    linkComment.content = configMap.commentContent
                    linkComment.save()
                }
                else if(configMap.commentContent.length() == 0) {
                    DocContext commentContext = DocContext.findByOwner(linkComment)
                    if(commentContext.delete())
                        linkComment.delete()
                }
            }
            else if(!linkComment && configMap.commentContent.length() > 0) {
                RefdataValue typeNote = RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE)
                linkComment = new Doc([content:configMap.commentContent,type:typeNote])
                if(linkComment.save()) {
                    DocContext commentContext = new DocContext([doctype:typeNote,link:link,owner:linkComment])
                    commentContext.save()
                }
                else {
                    log.error(linkComment.errors)
                    errors = messageSource.getMessage('default.linking.savingError',null, LocaleContextHolder.getLocale())
                }
            }
        }
        else if(link && link.errors) {
            log.error(link.errors)
            errors = messageSource.getMessage('default.linking.savingError',null, LocaleContextHolder.getLocale())
        }
        errors
    }

    boolean deleteLink(String oid) {
        Links obj = genericOIDService.resolveOID(oid)
        if (obj) {
            DocContext comment = DocContext.findByLink(obj)
            if(comment) {
                Doc commentContent = comment.owner
                comment.delete()
                commentContent.delete()
            }
            def source = genericOIDService.resolveOID(obj.source), destination = genericOIDService.resolveOID(obj.destination)
            Set sourceChildren = source.getClass().findAllByInstanceOf(source), destinationChildren = destination.getClass().findAllByInstanceOf(destination)
            sourceChildren.each { sourceChild ->
                def destinationChild
                if(sourceChild instanceof Subscription)
                    destinationChild = destinationChildren.find { dest -> dest.getSubscriber() == sourceChild.getSubscriber() }
                else if(sourceChild instanceof License)
                    destinationChild = destinationChildren.find { dest -> dest.getLicensee() == sourceChild.getLicensee() }
                if(destinationChild)
                    Links.executeUpdate('delete from Links li where li.source = :source and li.destination = :destination and li.linkType = :linkType and li.owner = :owner',[source:GenericOIDService.getOID(sourceChild),destination:GenericOIDService.getOID(destinationChild),linkType:obj.linkType,owner:obj.owner])
            }
            obj.delete()
            true
        }
        else false
    }

}
