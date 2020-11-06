package de.laser


import com.k_int.kbplus.GenericOIDService
import de.laser.auth.User
import de.laser.exceptions.CreationException
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class LinksGenerationService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    GenericOIDService genericOIDService
    MessageSource messageSource
    ContextService contextService

    LinkedHashMap<String,List> generateNavigation(context) {
        List prevLink = []
        List nextLink = []
        List previous = Links.executeQuery('select li from Links li where :context in (li.sourceLicense,li.sourceSubscription) and linkType = :linkType',[context:context,linkType:RDStore.LINKTYPE_FOLLOWS])
        List next = Links.executeQuery('select li from Links li where :context in (li.destinationLicense,li.destinationSubscription) and linkType = :linkType',[context:context,linkType:RDStore.LINKTYPE_FOLLOWS])
        if(previous.size() > 0) {
            previous.each { Links li ->
                def obj
                if(li.destinationLicense)
                    obj = li.destinationLicense
                else if(li.destinationSubscription)
                    obj = li.destinationSubscription
                prevLink.add(obj)
            }
        }
        if(next.size() > 0) {
            next.each { Links li ->
                def obj
                if(li.sourceLicense)
                    obj = li.sourceLicense
                else if(li.sourceSubscription)
                    obj = li.sourceSubscription
                nextLink.add(obj)
            }
        }
        return [prevLink:prevLink,nextLink:nextLink]
    }

    Map<String,Object> getSourcesAndDestinations(obj,user) {
        Map<String,Set<Links>> links = [:]
        // links
        Set<Links> sources = Links.executeQuery('select li from Links li where :context in (li.sourceSubscription,li.sourceLicense)',[context:obj])
        Set<Links> destinations = Links.executeQuery('select li from Links li where :context in (li.destinationSubscription,li.destinationLicense)',[context:obj])
        //IN is from the point of view of the context object (= obj)

        sources.each { Links link ->
            def destination
            if(link.destinationSubscription)
                destination = link.destinationSubscription
            else if(link.destinationLicense)
                destination = link.destinationLicense
            if (destination.respondsTo("isVisibleBy") && destination.isVisibleBy(user)) {
                String index = genericOIDService.getOID(link.linkType)
                if (links[index] == null) {
                    links[index] = []
                }
                links[index].add(link)
            }
        }
        destinations.each { Links link ->
            def source
            if(link.sourceSubscription)
                source = link.sourceSubscription
            else if(link.sourceLicense)
                source = link.sourceLicense
            if (source.respondsTo("isVisibleBy") && source.isVisibleBy(user)) {
                String index = genericOIDService.getOID(link.linkType)
                if (links[index] == null) {
                    links[index] = []
                }
                links[index].add(link)
            }
        }
        links
    }


    List<Subscription> getAllLinkedSubscriptions(List<Subscription> ownerSubscriptions, User user) {
        Set<Links> sources
        Set<Links> destinations
        List<Subscription> result = []

        // links
        List oIDs = ownerSubscriptions.collect {genericOIDService.getOID(it)}
        if (oIDs) {
            String srcQuery = "from Links where source in (:oIDs) and destination like '${Subscription.class.name}%'"
            String dstQuery = "from Links where destination in (:oIDs) and source like '${Subscription.class.name}%'"
            sources = Links.executeQuery( srcQuery, [oIDs: oIDs])
            destinations = Links.executeQuery( dstQuery, [oIDs: oIDs])

            //IN is from the point of view of the context object (= obj)

            sources.each { Links link ->
                Subscription destination = genericOIDService.resolveOID(link.destination)
                if (destination.isVisibleBy(user)) {
                    result.add(destination)
                }
            }
            destinations.each { Links link ->
                Subscription source = genericOIDService.resolveOID(link.source)
                if (source.isVisibleBy(user)) {
                    result.add(source)
                }
            }
        }
        result
    }


    Set getSuccessionChain(startingPoint, String position) {
        Set chain = []
        Set first = getRecursiveNext([startingPoint].toSet(),position)
        Set next
        while(first.size() > 0) {
            first.each { row ->
                chain << row
            }
            next = getRecursiveNext(first,position)
            first = next
        }
        if(startingPoint instanceof Subscription)
            chain.sort{ a,b -> a.startDate <=> b.startDate }
        else chain
    }

    private Set<Subscription> getRecursiveNext(Set points, String position) {
        String pair
        if(position == 'sourceSubscription')
            pair = 'destinationSubscription'
        else if(position == 'destinationSubscription')
            pair = 'sourceSubscription'
        Subscription.executeQuery('select li.'+pair+' from Links li where li.'+position+' in (:points) and li.linkType = :linkType',[points:points,linkType:RDStore.LINKTYPE_FOLLOWS])
    }

    /**
     * connects the context object with the given pair.
     *
     * @return false if manipulation was successful, a string describing the error otherwise
     */
    Map<String,Object> createOrUpdateLink(GrailsParameterMap params) {
        Locale locale = LocaleContextHolder.getLocale()
        Map<String,Object> result = [institution:contextService.org]
        //error when no pair is given!
        params.keySet().each {
            if(it.contains("pair_")) {
                def pairCheck = params.get(it)
                if(!pairCheck) {
                    result.error = messageSource.getMessage('default.linking.noLinkError',null,locale)
                    [result:result,status:STATUS_ERROR]
                }
            }
        }
        //distinct between insert and update - if a link id exists, then proceed with edit, else create new instance
        Map<String,Object> configMap = [owner:result.institution]
        //perspectiveIndex 0: source -> dest, 1: dest -> source
        if(params.link) {
            configMap.link = genericOIDService.resolveOID(params.link)
            if(params.commentID)
                configMap.comment = genericOIDService.resolveOID(params.commentID)
            if(params["linkType_${configMap.link.id}"]) {
                String linkTypeString = params["linkType_${configMap.link.id}"].split("§")[0]
                int perspectiveIndex = Integer.parseInt(params["linkType_${configMap.link.id}"].split("§")[1])
                configMap.linkType = genericOIDService.resolveOID(linkTypeString)
                configMap.commentContent = params["linkComment_${configMap.link.id}"].trim()
                if(perspectiveIndex == 0) {
                    configMap.source = genericOIDService.resolveOID(params.context)
                    configMap.destination = genericOIDService.resolveOID(params["pair_${configMap.link.id}"])
                }
                else if(perspectiveIndex == 1) {
                    configMap.source = genericOIDService.resolveOID(params["pair_${configMap.link.id}"])
                    configMap.destination = genericOIDService.resolveOID(params.context)
                }
            }
            else if(!params["linkType_${configMap.link.id}"]) {
                result.error = messageSource.getMessage('default.linking.linkTypeError',null,locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        else {
            if(params["linkType_new"]) {
                String linkTypeString = params["linkType_new"].split("§")[0]
                int perspectiveIndex = Integer.parseInt(params["linkType_new"].split("§")[1])
                configMap.linkType = genericOIDService.resolveOID(linkTypeString)
                configMap.commentContent = params.linkComment_new
                if(perspectiveIndex == 0) {
                    configMap.source = genericOIDService.resolveOID(params.context)
                    configMap.destination = genericOIDService.resolveOID(params.pair_new)
                }
                else if(perspectiveIndex == 1) {
                    configMap.source = genericOIDService.resolveOID(params.pair_new)
                    configMap.destination = genericOIDService.resolveOID(params.context)
                }
                def currentObject = genericOIDService.resolveOID(params.context)
                List childInstances = currentObject.getClass().findAllByInstanceOf(currentObject)
                if(childInstances) {
                    configMap.contextInstances = childInstances
                    def pairObject = genericOIDService.resolveOID(params.pair_new)
                    configMap.pairInstances = pairObject.getClass().findAllByInstanceOf(pairObject)
                }
            }
            else if(params["linkType_sl_new"]) {
                String linkTypeString = params["linkType_sl_new"].split("§")[0]
                configMap.linkType = genericOIDService.resolveOID(linkTypeString)
                configMap.commentContent = params.linkComment_sl_new
                configMap.source = genericOIDService.resolveOID(params.pair_sl_new)
                configMap.destination = genericOIDService.resolveOID(params.context)
            }
            else if(!params["linkType_new"] && !params["linkType_sl_new"]) {
                result.error = messageSource.getMessage('default.linking.linkTypeError',null,locale)
                [result:result,status:STATUS_ERROR]
            }
        }
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
                    def sourceObj = configMap.source, destObj = configMap.destination
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
                                childConfigMap.source = contextInstance
                                childConfigMap.destination = pairChild
                            }
                            else if(contextInstance.instanceOf == destObj) {
                                childConfigMap.source = pairChild
                                childConfigMap.destination = contextInstance
                            }
                            Links.construct(childConfigMap)
                        }
                    }
                }
            }
            catch (CreationException e) {
                log.error( e.toString() )
                result.error = messageSource.getMessage('default.linking.savingError',null, locale)
                [result:result,status:STATUS_ERROR]
            }
        }
        if(link) {
            link.linkType = (RefdataValue) configMap.linkType
            link.setSourceAndDestination(configMap.source, configMap.destination)
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
                    log.error(linkComment.errors.toString())
                    result.error = messageSource.getMessage('default.linking.savingError',null, locale)
                    [result:result,status:STATUS_ERROR]
                }
            }
        }
        else if(link && link.errors) {
            log.error(link.errors.toString())
            result.error = messageSource.getMessage('default.linking.savingError',null, locale)
            [result:result,status:STATUS_ERROR]
        }
        [result:result,status:STATUS_OK]
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
            def source = obj.determineSource(), destination = obj.determineDestination()
            Set sourceChildren = source.getClass().findAllByInstanceOf(source), destinationChildren = destination.getClass().findAllByInstanceOf(destination)
            sourceChildren.each { sourceChild ->
                def destinationChild
                if(sourceChild instanceof Subscription)
                    destinationChild = destinationChildren.find { dest -> dest.getSubscriber() == sourceChild.getSubscriber() }
                else if(sourceChild instanceof License)
                    destinationChild = destinationChildren.find { dest -> dest.getLicensee() == sourceChild.getLicensee() }
                if(destinationChild)
                    Links.executeUpdate('delete from Links li where :source in (li.sourceSubscription,li.sourceLicense) and :destination in (li.destinationSubscription,li.destinationLicense) and li.linkType = :linkType and li.owner = :owner',[source:sourceChild, destination:destinationChild, linkType:obj.linkType, owner:obj.owner])
            }
            obj.delete()
            true
        }
        else false
    }

}
