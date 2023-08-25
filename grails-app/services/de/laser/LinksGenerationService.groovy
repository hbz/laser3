package de.laser


import de.laser.auth.User
import de.laser.exceptions.CreationException
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service handles the display of hyperlinks between two linked objects and the
 * underlying linking of objects. Currently subscriptions and licenses may be target objects
 * @see Subscription
 * @see License
 * @see Links
 */
@Transactional
class LinksGenerationService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    GenericOIDService genericOIDService
    MessageSource messageSource

    /**
     * Generates a map for the navigation between two linked objects
     * @param context the context object whose perspective is going to be taken; may be a subscription or a license
     * @param fullObject should the full object be retrieved or only an identifier?
     * @return a map containing the next and previous objects
     */
    LinkedHashMap<String,List> generateNavigation(context, boolean fullObject = true) {
        List prevLink = [], nextLink = []
        String id = !fullObject ? ".id" : ""
        if(context instanceof Subscription) {
            prevLink.addAll(Links.executeQuery('select li.destinationSubscription'+id+' from Links li where li.sourceSubscription = :context and li.linkType = :linkType',[context:context,linkType:RDStore.LINKTYPE_FOLLOWS]))
            nextLink.addAll(Links.executeQuery('select li.sourceSubscription'+id+' from Links li where li.destinationSubscription = :context and li.linkType = :linkType',[context:context,linkType:RDStore.LINKTYPE_FOLLOWS]))
        }
        else if(context instanceof License) {
            prevLink.addAll(Links.executeQuery('select li.destinationLicense'+id+' from Links li where li.sourceLicense = :context and li.linkType = :linkType',[context:context,linkType:RDStore.LINKTYPE_FOLLOWS]))
            nextLink.addAll(Links.executeQuery('select li.sourceLicense'+id+' from Links li where li.destinationLicense = :context and li.linkType = :linkType',[context:context,linkType:RDStore.LINKTYPE_FOLLOWS]))
        }
        else if(context instanceof Org) {
            prevLink.addAll(Combo.executeQuery('select c.toOrg'+id+' from Combo c where c.fromOrg = :context and c.type = :type',[context:context,type:RDStore.COMBO_TYPE_FOLLOWS]))
            nextLink.addAll(Combo.executeQuery('select c.fromOrg'+id+' from Combo c where c.toOrg = :context and c.type = :type',[context:context,type:RDStore.COMBO_TYPE_FOLLOWS]))
        }
        return [prevLink:prevLink,nextLink:nextLink]
    }

    /**
     * Substitution call for list all kinds of link sources and destinations
     * @param obj the object whose perspective should be taken
     * @param user the user to check its access rights
     * @return the result of {@link #getSourcesAndDestinations(java.lang.Object, de.laser.auth.User, java.util.List)} with all link types as argument
     */
    Map<String,Object> getSourcesAndDestinations(obj,User user) {
        getSourcesAndDestinations(obj,user,RefdataCategory.getAllRefdataValues(RDConstants.LINK_TYPE))
    }

    /**
     * Retrieves all outgoing links from the given context object
     * @param obj the object whose perspective should be taken
     * @param user the user to check its access rights
     * @param linkTypes the list of link type reference values of which types the links may be
     * @return
     */
    Map<String,Object> getSourcesAndDestinations(obj,User user,List<RefdataValue> linkTypes) {
        Map<String,Set<Links>> links = [:]
        // links
        Set<Links> sources = [], destinations = []
        if(obj instanceof Subscription) {
            sources.addAll(Links.executeQuery('select li from Links li where :context = li.sourceSubscription and linkType in (:linkTypes)',[context:obj,linkTypes:linkTypes]))
            destinations.addAll(Links.executeQuery('select li from Links li where :context = li.destinationSubscription and linkType in (:linkTypes)',[context:obj,linkTypes: linkTypes]))
        }
        else if(obj instanceof License) {
            sources.addAll(Links.executeQuery('select li from Links li where :context = li.sourceLicense and linkType in (:linkTypes)',[context:obj,linkTypes:linkTypes]))
            destinations.addAll(Links.executeQuery('select li from Links li where :context = li.destinationLicense and linkType in (:linkTypes)',[context:obj,linkTypes: linkTypes]))
        }
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

    @Deprecated
    boolean checkPrevious(checkFor) {
        Map<String,Object> queryParams = [follows: RDStore.LINKTYPE_FOLLOWS, check: checkFor]
        if(checkFor instanceof License)
            Links.executeQuery('select li.sourceLicense.id from Links li where li.linkType = :follows and li.destinationLicense = :check',queryParams).size() > 0
        else if(checkFor instanceof Subscription)
            Links.executeQuery('select li.sourceSubscription.id from Links li where li.linkType = :follows and li.destinationSubscription = :check',queryParams).size() > 0
        false
    }

    /**
     * Gets all linked subscriptions for the given subscription list
     * @param ownerSubscriptions the subscriptions whose links should be retrieved
     * @param user the user to check the access rights to the target objects
     * @return a list of subscriptions linked to the given list
     */
    List<Subscription> getAllLinkedSubscriptions(List<Subscription> ownerSubscriptions, User user) {
        Set<Links> sources
        Set<Links> destinations
        List<Subscription> result = []

        // links
        if (ownerSubscriptions) {
            String srcQuery = "from Links where sourceSubscription in (:subs) and destinationSubscription != null"
            String dstQuery = "from Links where destinationSubscription in (:subs) and sourceSubscription != null"
            sources = Links.executeQuery( srcQuery, [subs: ownerSubscriptions])
            destinations = Links.executeQuery( dstQuery, [subs: ownerSubscriptions])

            //IN is from the point of view of the context object (= obj)

            sources.each { Links link ->
                Subscription destination = link.destinationSubscription
                if (destination.isVisibleBy(user)) {
                    result.add(destination)
                }
            }
            destinations.each { Links link ->
                Subscription source = link.sourceSubscription
                if (source.isVisibleBy(user)) {
                    result.add(source)
                }
            }
        }
        result
    }

    /**
     * Gets all linked subscriptions for the given subscription list for dropdown display
     * @param ownerSubscriptions the subscriptions whose links should be retrieved
     * @return a list of subscriptions linked to the given list
     */
    List<Subscription> getAllLinkedSubscriptionsForDropdown(Set<Long> ownerSubscriptions) {
        Set sources
        Set destinations
        List result = []

        // links
        if (ownerSubscriptions) {
            log.debug(ownerSubscriptions.toListString())
            //IN is from the point of view of the context object (= obj)
            String srcQuery = "select s.id, s.name, s.startDate, s.endDate, s.status, so.org, so.roleType, s.instanceOf.id from Links li join li.destinationSubscription s right join s.orgRelations so where li.sourceSubscription.id in (:subs)"
            String dstQuery = "select s.id, s.name, s.startDate, s.endDate, s.status, so.org, so.roleType, s.instanceOf.id from Links li join li.sourceSubscription s right join s.orgRelations so where li.destinationSubscription.id in (:subs)"
            sources = Subscription.executeQuery( srcQuery, [subs: ownerSubscriptions])
            destinations = Subscription.executeQuery( dstQuery, [subs: ownerSubscriptions])

            result.addAll(sources)
            result.addAll(destinations)
        }
        result
    }

    /**
     * Builds a set of subscriptions ordered in a succession chain
     * @param startingPoint the object from where to depart
     * @param position the direction to take
     * @return a {@link Set} of objects preceding of following the given one
     */
    Set getSuccessionChain(startingPoint, String position) {
        Set chain = []
        Set first = _getRecursiveNext([startingPoint].toSet(),position)
        Set next
        while(first.size() > 0 && chain.addAll(first)) {
            first.each { row ->
                chain << row
            }
            next = _getRecursiveNext(first,position)
            first = next
        }
        if(startingPoint instanceof Subscription)
            chain.sort{ a,b -> a.startDate <=> b.startDate }
        else chain
    }

    /**
     * Gets the respective neighboring subscriptions from the given starting point
     * @param points the set of objects following or preceding the given subscription
     * @param position the direction where to go from the starting point
     * @return a {@link Set} of {@link Subscription}s directly following or preceding the given one
     */
    private Set<Subscription> _getRecursiveNext(Set points, String position) {
        String pair
        if(position == 'sourceSubscription')
            pair = 'destinationSubscription'
        else if(position == 'destinationSubscription')
            pair = 'sourceSubscription'
        Subscription.executeQuery('select li.'+pair+' from Links li where li.'+position+' in (:points) and li.linkType = :linkType',[points:points,linkType:RDStore.LINKTYPE_FOLLOWS])
    }

    /**
     * Connects the context object with the given pair
     * @params the request parameter map
     * @return result map OK if manipulation was successful, ERROR with a string describing the error otherwise
     */
    Map<String,Object> createOrUpdateLink(GrailsParameterMap params) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String,Object> result = [institution:contextService.getOrg()]
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
                RefdataValue typeNote = RDStore.DOC_TYPE_NOTE
                // 4644 DocContext commentContext = DocContext.findByDoctypeAndLink(typeNote, link)
                DocContext commentContext = DocContext.findByLink(link)
                if(commentContext) {
                    linkComment = commentContext.owner
                }
                else {
                    linkComment = new Doc([type: typeNote])
                    // 4644 commentContext = new DocContext([doctype: typeNote, link: link, owner: linkComment])
                    commentContext = new DocContext([link: link, owner: linkComment])
                    commentContext.save()
                }
                linkComment.content = configMap.commentContent
                if(!linkComment.save()) {
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

    /**
     * Deletes the given link between two objects, unlinking them
     * @param oid the OID of the link to delete
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteLink(String oid) {
        Links obj = (Links) genericOIDService.resolveOID(oid)
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

    /**
     * Gets the organisations connected to the given organisation
     * @param org the {@link Org} whose connections should be retrieved
     * @return a {@link Set} of connections from or to this {@link Org}
     */
    Set<Combo> getOrgLinks(Org org) {
        Combo.executeQuery('select c from Combo c where (c.fromOrg = :context or c.toOrg = :context) and c.type = :follows', [follows: RDStore.COMBO_TYPE_FOLLOWS, context: org])
    }
}
