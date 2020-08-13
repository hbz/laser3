package de.laser

import com.k_int.kbplus.*
import com.k_int.properties.PropertyDefinition
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.exceptions.CreationException
import de.laser.helper.ConfigUtils
import de.laser.helper.FactoryResult
import de.laser.helper.RDStore
import de.laser.interfaces.ShareSupport
import grails.transaction.Transactional
import grails.util.Holders
import org.codehaus.groovy.grails.web.util.WebUtils
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.MessageSource

import java.nio.file.Files
import java.nio.file.Path

@Transactional
class CopyElementsService {


    GenericOIDService genericOIDService
    ComparisonService comparisonService
    TaskService taskService
    SubscriptionService subscriptionService
    ContextService contextService
    MessageSource messageSource
    Locale locale
    DocstoreService docstoreService
    FormService formService

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
    }

    List allowedProperties(Object obj){
        List result = []
        switch (obj.class.simpleName) {
            case License.class.simpleName:
                result = ['startDate', 'endDate', 'licenseUrl', 'licenseCategory', 'status', 'type', 'openEnded', 'isPublicForApi']
                break
            case Subscription.class.simpleName:
                result = ['startDate', 'endDate', 'manualCancellationDate', 'status', 'kind', 'form', 'resource', 'isPublicForApi', 'hasPerpetualAccess']
                break
            case SurveyInfo.class.simpleName:
                result = ['startDate', 'endDate', 'comment']
                break
            case SurveyConfig.class.simpleName:
                result = ['scheduledStartDate', 'scheduledEndDate', 'comment', 'internalComment', 'url', 'url2', 'url3']
                break
        }
        result
    }

    Map loadDataFor_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceObjectId ?: params.id)
        Subscription newSub = params.targetObjectId ? Subscription.get(params.targetObjectId) : null

        result.sourceIdentifiers = baseSub.ids?.sort { x, y ->
            if (x.ns?.ns?.toLowerCase() == y.ns?.ns?.toLowerCase()){
                x.value <=> y.value
            } else {
                x.ns?.ns?.toLowerCase() <=> y.ns?.ns?.toLowerCase()
            }
        }
        result.targetIdentifiers = newSub?.ids?.sort { x, y ->
            if (x.ns?.ns?.toLowerCase() == y.ns?.ns?.toLowerCase()){
                x.value <=> y.value
            } else {
                x.ns?.ns?.toLowerCase() <=> y.ns?.ns?.toLowerCase()
            }
        }

        String sourceLicensesQuery = "select l from License l where concat('${License.class.name}:',l.id) in (select li.source from Links li where li.destination = :sub and li.linkType = :linkType) order by l.sortableReference asc"
        result.sourceLicenses = License.executeQuery(sourceLicensesQuery,[sub: GenericOIDService.getOID(baseSub), linkType: RDStore.LINKTYPE_LICENSE])
        if(newSub) {
            String targetLicensesQuery = "select l from License l where concat('${License.class.name}:',l.id) in (select li.source from Links li where li.destination = :sub and li.linkType = :linkType) order by l.sortableReference asc"
            result.targetLicenses = License.executeQuery(targetLicensesQuery,[sub:GenericOIDService.getOID(newSub),linkType:RDStore.LINKTYPE_LICENSE])
        }

        // restrict visible for templates/links/orgLinksAsList
        result.source_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(baseSub)
        result.target_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(newSub)
        result
    }

    Map loadDataFor_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceObjectId ? Long.parseLong(params.sourceObjectId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetObjectId) {
            newSub = Subscription.get(Long.parseLong(params.targetObjectId))
        }

        result.sourceObject = baseSub
        result.targetObject = newSub
        result.sourceTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.sourceObject)
        result.targetTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.targetObject)
        result
    }

    Map loadDataFor_Subscriber(Map params) {
        Map<String, Object> result = [:]
        result.sourceObject = Subscription.get(params.sourceObjectId ? Long.parseLong(params.sourceObjectId): Long.parseLong(params.id))
        result.validSourceSubChilds = subscriptionService.getValidSubChilds(result.sourceObject)
        if (params.targetObjectId) {
            result.targetObject = Subscription.get(Long.parseLong(params.targetObjectId))
            result.validTargetSubChilds = subscriptionService.getValidSubChilds(result.targetObject)
        }
        result
    }

    Map loadDataFor_Properties(Map params){
        LinkedHashMap result = [customProperties:[:],privateProperties:[:]]
        Subscription baseSub = Subscription.get(params.sourceObjectId ?: params.id)
        Subscription newSub = null
        List<Subscription> subsToCompare = [baseSub]
        if (params.targetObjectId) {
            newSub = Subscription.get(params.targetObjectId)
            subsToCompare.add(newSub)
        }

        if (newSub) {
            result.newSub = newSub.refresh()
        }
        Org contextOrg = contextService.org
        subsToCompare.each{ Subscription sub ->
            Map customProperties = result.customProperties
            customProperties = comparisonService.buildComparisonTree(customProperties,sub,sub.propertySet.findAll{it.type.tenant == null && (it.tenant?.id == contextOrg.id || (it.tenant?.id != contextOrg.id && it.isPublic))}.sort{it.type.getI10n('name')})
            result.customProperties = customProperties
            Map privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties,sub,sub.propertySet.findAll{it.type.tenant?.id == contextOrg.id}.sort{it.type.getI10n('name')})
            result.privateProperties = privateProperties
        }
        result
    }

    Map loadDataFor_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceObjectId ?: params.id)
        Subscription newSub = params.targetObjectId ? Subscription.get(params.targetObjectId) : null
        result.sourceIEs = subscriptionService.getIssueEntitlements(baseSub)
        result.targetIEs = subscriptionService.getIssueEntitlements(newSub)
        result.newSub = newSub
        result.subscription = baseSub
        result
    }

    void copySubscriber(List<Subscription> subscriptionToTake, Subscription targetSub, def flash) {
        targetSub.refresh()
        List<Subscription> targetChildSubs = subscriptionService.getValidSubChilds(targetSub)
        subscriptionToTake.each { Subscription subMember ->
            //Gibt es mich schon in der Ziellizenz?
            Org found = targetChildSubs?.find { Subscription targetSubChild -> targetSubChild.getSubscriber() == subMember.getSubscriber() }?.getSubscriber()

            if (found) {
                // mich gibts schon! Fehlermeldung ausgeben!
                Object[] args = [found.sortname ?: found.name]
                flash.error += messageSource.getMessage('subscription.err.subscriberAlreadyExistsInTargetSub', args, locale)
//                diffs.add(message(code:'pendingChange.message_CI01',args:[costTitle,g.createLink(mapping:'subfinance',controller:'subscription',action:'index',params:[sub:cci.sub.id]),cci.sub.name,cci.costInBillingCurrency,newCostItem
            } else {
                //ChildSub Exist
//                ArrayList<Links> prevLinks = Links.findAllByDestinationAndLinkTypeAndObjectType(subMember.id, RDStore.LINKTYPE_FOLLOWS, Subscription.class.name)
//                if (prevLinks.size() == 0) {

                /* Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
                        [result.subscriptionInstance, it.id])*/

                def newSubscription = new Subscription(
                        isMultiYear: subMember.isMultiYear,
                        type: subMember.type,
                        kind: subMember.kind,
                        status: targetSub.status,
                        name: subMember.name,
                        startDate: subMember.isMultiYear ? subMember.startDate : targetSub.startDate,
                        endDate: subMember.isMultiYear ? subMember.endDate : targetSub.endDate,
                        manualRenewalDate: subMember.manualRenewalDate,
                        /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                        identifier: UUID.randomUUID().toString(),
                        instanceOf: targetSub,
                        //previousSubscription: subMember?.id,
                        isSlaved: subMember.isSlaved,
                        //owner: targetSub.owner ? subMember.owner : null,
                        resource: targetSub.resource ?: null,
                        form: targetSub.form ?: null,
                        isPublicForApi: targetSub.isPublicForApi,
                        hasPerpetualAccess: targetSub.hasPerpetualAccess
                )
                newSubscription.save(flush:true)
                //ERMS-892: insert preceding relation in new data model
                if (subMember) {
                    try {
                        Links.construct([source: GenericOIDService.getOID(newSubscription), destination: GenericOIDService.getOID(subMember), linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org])
                        Set<Links> precedingLicenses = Links.findAllByDestinationAndLinkType(GenericOIDService.getOID(subMember),RDStore.LINKTYPE_LICENSE)
                        precedingLicenses.each { Links link ->
                            Map<String,Object> successorLink = [source:link.source,destination:GenericOIDService.getOID(newSubscription),linkType:RDStore.LINKTYPE_LICENSE,owner:contextService.org]
                            Links.construct(successorLink)
                        }
                    }
                    catch (CreationException e) {
                        log.error("Subscription linking failed, please check: ${e.stackTrace}")
                    }
                }

                if (subMember.propertySet) {
                    //customProperties
                    for (prop in subMember.propertySet) {
                        SubscriptionProperty copiedProp = new SubscriptionProperty(type: prop.type, owner: newSubscription, isPublic: prop.isPublic, tenant: prop.tenant)
                        copiedProp = prop.copyInto(copiedProp)
                        copiedProp.save()
                        //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                    }
                }
                /*
                if (subMember.privateProperties) {
                    //privatProperties
                    List tenantOrgs = OrgRole.executeQuery('select o.org from OrgRole as o where o.sub = :sub and o.roleType in (:roleType)', [sub: subMember, roleType: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIPTION_CONSORTIA]]).collect {
                        it -> it.id
                    }
                    subMember.privateProperties?.each { prop ->
                        if (tenantOrgs.indexOf(prop.type?.tenant?.id) > -1) {
                            def copiedProp = new SubscriptionProperty(type: prop.type, owner: newSubscription)
                            copiedProp = prop.copyInto(copiedProp)
                            copiedProp.save()
                            //newSubscription.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
                        }
                    }
                }
                */

                if (subMember.packages && targetSub.packages) {
                    //Package
                    subMember.packages?.each { pkg ->
                        def pkgOapls = pkg.oapls
                        pkg.properties.oapls = null
                        pkg.properties.pendingChangeConfig = null
                        SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                        InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                        newSubscriptionPackage.subscription = newSubscription

                        if(newSubscriptionPackage.save()){
                            pkgOapls.each{ oapl ->

                                def oaplProperties = oapl.properties
                                oaplProperties.globalUID = null
                                OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                                InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                                newOrgAccessPointLink.subPkg = newSubscriptionPackage
                                newOrgAccessPointLink.save()
                            }
                        }
                    }
                }
                if (subMember.issueEntitlements && targetSub.issueEntitlements) {
                    subMember.issueEntitlements?.each { ie ->
                        if (ie.status != RDStore.TIPP_STATUS_DELETED) {
                            def ieProperties = ie.properties
                            ieProperties.globalUID = null

                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                            InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                            newIssueEntitlement.coverages = null
                            newIssueEntitlement.ieGroups = null
                            newIssueEntitlement.subscription = newSubscription

                            if(save(newIssueEntitlement, flash)){
                                ie.properties.coverages.each{ coverage ->

                                    def coverageProperties = coverage.properties
                                    IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                                    InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                                    newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement
                                    newIssueEntitlementCoverage.save()
                                }
                            }
                        }
                    }
                }

                //OrgRole
                subMember.orgRelations?.each { or ->
                    if ((or.org.id == contextService.getOrg().id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIPTION_COLLECTIVE]) || (targetSub.orgRelations.size() >= 1)) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = newSubscription
                        newOrgRole.save(flush:true)
                        log.debug("new org role set: ${newOrgRole.sub} for ${newOrgRole.org.sortname}")
                    }
                }

                if (subMember.prsLinks && targetSub.prsLinks) {
                    //PersonRole
                    subMember.prsLinks?.each { prsLink ->
                        PersonRole newPersonRole = new PersonRole()
                        InvokerHelper.setProperties(newPersonRole, prsLink.properties)
                        newPersonRole.sub = newSubscription
                        newPersonRole.save()
                    }
                }
//                }
            }
        }
    }

    Map copySubElements_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Subscription baseSub = Subscription.get(params.sourceObjectId ?: params.id)
        Subscription newSub = params.targetObjectId ? Subscription.get(params.targetObjectId) : null

        if (formService.validateToken(params)) {

            params.list('object.take').each { takeProperty ->
                if (takeProperty in allowedProperties(baseSub)) {
                    copyObjectProperty(baseSub, newSub, flash, takeProperty)
                }
            }

            allowedProperties(baseSub).each { allowedProperty ->
                if (allowedProperty in params.list('object.take')) {
                    if (allowedProperty in params.list('object.toggleAudit')) {
                        toggleAuditObjectProperty(baseSub, newSub, flash, allowedProperty)
                    } else {
                        removeToggleAuditObjectProperty(newSub, flash, allowedProperty)
                    }
                }

            }

            if (params.subscription?.deleteLicenses && isBothSubscriptionsSet(baseSub, newSub)) {
                List<License> toDeleteLicenses = params.list('subscription.deleteLicenses').collect { genericOIDService.resolveOID(it) }
                deleteLicenses(toDeleteLicenses, newSub, flash)
            } else if (params.subscription?.takeLicenses && isBothSubscriptionsSet(baseSub, newSub)) {
                List<License> toCopyLicenses = params.list('subscription.takeLicenses').collect { genericOIDService.resolveOID(it) }
                copyLicenses(toCopyLicenses, newSub, flash)
            }

            if (params.subscription?.deleteOrgRelations && isBothSubscriptionsSet(baseSub, newSub)) {
                List<OrgRole> toDeleteOrgRelations = params.list('subscription.deleteOrgRelations').collect { genericOIDService.resolveOID(it) }
                deleteOrgRelations(toDeleteOrgRelations, newSub, flash)
                //isTargetSubChanged = true
            }
            if (params.subscription?.takeOrgRelations && isBothSubscriptionsSet(baseSub, newSub)) {
                List<OrgRole> toCopyOrgRelations = params.list('subscription.takeOrgRelations').collect { genericOIDService.resolveOID(it) }
                copyOrgRelations(toCopyOrgRelations, baseSub, newSub, flash)
                //isTargetSubChanged = true

                List<OrgRole> toggleShareOrgRoles = params.list('toggleShareOrgRoles').collect {
                    genericOIDService.resolveOID(it)
                }

                newSub = newSub.refresh()
                newSub.orgRelations.each { newSubOrgRole ->

                    if (newSubOrgRole.org in toggleShareOrgRoles.org) {
                        newSubOrgRole.isShared = true
                        newSubOrgRole.save(flush: true)
                        ((ShareSupport) newSub).updateShare(newSubOrgRole)
                    }
                }
            }

            if (params.subscription?.deleteSpecificSubscriptionEditors && isBothSubscriptionsSet(baseSub, newSub)) {
                List<PersonRole> toDeleteSpecificSubscriptionEditors = params.list('subscription.deleteSpecificSubscriptionEditors').collect { genericOIDService.resolveOID(it) }
                deleteSpecificSubscriptionEditors(toDeleteSpecificSubscriptionEditors, newSub, flash)
                //isTargetSubChanged = true
            }
            if (params.subscription?.takeSpecificSubscriptionEditors && isBothSubscriptionsSet(baseSub, newSub)) {
                List<PersonRole> toCopySpecificSubscriptionEditors = params.list('subscription.takeSpecificSubscriptionEditors').collect { genericOIDService.resolveOID(it) }
                copySpecificSubscriptionEditors(toCopySpecificSubscriptionEditors, baseSub, newSub, flash)
                //isTargetSubChanged = true
            }

            if (params.subscription?.deleteIdentifierIds && isBothSubscriptionsSet(baseSub, newSub)) {
                def toDeleteIdentifiers = []
                params.list('subscription.deleteIdentifierIds').each { identifier -> toDeleteIdentifiers << Long.valueOf(identifier) }
                deleteIdentifiers(toDeleteIdentifiers, newSub, flash)
                //isTargetSubChanged = true
            }

            if (params.subscription?.takeIdentifierIds && isBothSubscriptionsSet(baseSub, newSub)) {
                def toCopyIdentifiers = []
                params.list('subscription.takeIdentifierIds').each { identifier -> toCopyIdentifiers << Long.valueOf(identifier) }
                copyIdentifiers(baseSub, toCopyIdentifiers, newSub, flash)
                //isTargetSubChanged = true
            }
        }

        /*if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }*/
        result.subscription = baseSub
        result.newSub = newSub
        result.targetObject = newSub
        result
    }

    Map copySubElements_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Subscription baseSub = Subscription.get(params.sourceObjectId ? Long.parseLong(params.sourceObjectId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetObjectId) {
            newSub = Subscription.get(Long.parseLong(params.targetObjectId))
        }
        boolean isTargetSubChanged = false
        if (params.subscription?.deleteDocIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteDocs = []
            params.list('subscription.deleteDocIds').each { doc -> toDeleteDocs << Long.valueOf(doc) }
            deleteDocs(toDeleteDocs, newSub, flash)
            isTargetSubChanged = true
        }

        if (params.subscription?.takeDocIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyDocs = []
            params.list('subscription.takeDocIds').each { doc -> toCopyDocs << Long.valueOf(doc) }
            copyDocs(baseSub, toCopyDocs, newSub, flash)
            isTargetSubChanged = true
        }

        if (params.subscription?.deleteAnnouncementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteAnnouncements = []
            params.list('subscription.deleteAnnouncementIds').each { announcement -> toDeleteAnnouncements << Long.valueOf(announcement) }
            deleteAnnouncements(toDeleteAnnouncements, newSub, flash)
            isTargetSubChanged = true
        }

        if (params.subscription?.takeAnnouncementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyAnnouncements = []
            params.list('subscription.takeAnnouncementIds').each { announcement -> toCopyAnnouncements << Long.valueOf(announcement) }
            copyAnnouncements(baseSub, toCopyAnnouncements, newSub, flash)
            isTargetSubChanged = true
        }

        if (params.subscription?.deleteTaskIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteTasks =  []
            params.list('subscription.deleteTaskIds').each{ tsk -> toDeleteTasks << Long.valueOf(tsk) }
            deleteTasks(toDeleteTasks, newSub, flash)
            isTargetSubChanged = true
        }

        if (params.subscription?.takeTaskIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyTasks =  []
            params.list('subscription.takeTaskIds').each{ tsk -> toCopyTasks << Long.valueOf(tsk) }
            copyTasks(baseSub, toCopyTasks, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }

        result.sourceObject = baseSub
        result.targetObject = newSub
        result
    }

    Map copySubElements_Identifiers(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Subscription baseSub = Subscription.get(params.sourceObjectId ? Long.parseLong(params.sourceObjectId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetObjectId) {
            newSub = Subscription.get(Long.parseLong(params.targetObjectId))
        }
        boolean isTargetSubChanged = false

        if (params.subscription?.deleteIdentifierIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteIdentifiers =  []
            params.list('subscription.deleteIdentifierIds').each{ identifier -> toDeleteIdentifiers << Long.valueOf(identifier) }
            deleteIdentifiers(toDeleteIdentifiers, newSub, flash)
            isTargetSubChanged = true
        }

        if (params.subscription?.takeIdentifierIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyIdentifiers =  []
            params.list('subscription.takeIdentifierIds').each{ identifier -> toCopyIdentifiers << Long.valueOf(identifier) }
            copyIdentifiers(baseSub, toCopyIdentifiers, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }

        result.flash = flash
        result.sourceObject = baseSub
        result.targetObject = newSub
        result
    }

    Map copySubElements_Subscriber(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Subscription baseSub = Subscription.get(params.sourceObjectId ? Long.parseLong(params.sourceObjectId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetObjectId) {
            newSub = Subscription.get(Long.parseLong(params.targetObjectId))
        }
        if (params.subscription?.copySubscriber && isBothSubscriptionsSet(baseSub, newSub)) {
            List<Subscription> toCopySubs = params.list('subscription.copySubscriber').collect { genericOIDService.resolveOID(it) }
            copySubscriber(toCopySubs, newSub, flash)
        }

        result.sourceObject = baseSub
        result.targetObject = newSub
        result
    }

    Map copySubElements_Properties(Map params){
        LinkedHashMap result = [customProperties:[:],privateProperties:[:]]
        Subscription baseSub = Subscription.get(params.sourceObjectId ?: params.id)
        boolean isRenewSub = params.isRenewSub ? true : false

        Subscription newSub = null
        List auditProperties = params.list('auditProperties')
        List<Subscription> subsToCompare = [baseSub]
        if (params.targetObjectId) {
            newSub = Subscription.get(params.targetObjectId)
            subsToCompare.add(newSub)
        }
        List<AbstractPropertyWithCalculatedLastUpdated> propertiesToTake = params.list('subscription.takeProperty').collect{ genericOIDService.resolveOID(it)}
        if (propertiesToTake && isBothSubscriptionsSet(baseSub, newSub)) {
            copyProperties(propertiesToTake, newSub, isRenewSub, flash, auditProperties)
        }

        List<AbstractPropertyWithCalculatedLastUpdated> propertiesToDelete = params.list('subscription.deleteProperty').collect{ genericOIDService.resolveOID(it)}
        if (propertiesToDelete && isBothSubscriptionsSet(baseSub, newSub)) {
            deleteProperties(propertiesToDelete, newSub, isRenewSub, flash, auditProperties)
        }

        if (newSub) {
            result.newSub = newSub.refresh()
        }
        result
    }

    Map copySubElements_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Subscription baseSub = Subscription.get(params.sourceObjectId ?: params.id)
        Subscription newSub = params.targetObjectId ? Subscription.get(params.targetObjectId) : null

        boolean isTargetSubChanged = false
        if (params.subscription?.deletePackageIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<SubscriptionPackage> packagesToDelete = params.list('subscription.deletePackageIds').collect{ genericOIDService.resolveOID(it)}
            deletePackages(packagesToDelete, newSub, flash)
            isTargetSubChanged = true
        }
        if (params.subscription?.takePackageIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<SubscriptionPackage> packagesToTake = params.list('subscription.takePackageIds').collect{ genericOIDService.resolveOID(it)}
            copyPackages(packagesToTake, newSub, flash)
            isTargetSubChanged = true
        }

        if(params.subscription?.deletePackageSettings && isBothSubscriptionsSet(baseSub, newSub)) {
            List<SubscriptionPackage> packageSettingsToDelete = params.list('subscription.deletePackageSettings').collect {
                genericOIDService.resolveOID(it)
            }
            packageSettingsToDelete.each { SubscriptionPackage toDelete ->
                PendingChangeConfiguration.SETTING_KEYS.each { String setting ->
                    if(AuditConfig.getConfig(toDelete.subscription,setting))
                        AuditConfig.removeConfig(toDelete.subscription,setting)
                }
                PendingChangeConfiguration.executeUpdate('delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage = :sp',[sp:toDelete])
            }
            isTargetSubChanged = true
        }
        if(params.subscription?.takePackageSettings && isBothSubscriptionsSet(baseSub, newSub)) {
            List<SubscriptionPackage> packageSettingsToTake = params.list('subscription.takePackageSettings').collect {
                genericOIDService.resolveOID(it)
            }
            packageSettingsToTake.each { SubscriptionPackage sp ->
                //explicit loading of service needed because lazy initialisation gives null
                copyPendingChangeConfiguration(PendingChangeConfiguration.findAllBySubscriptionPackage(sp),SubscriptionPackage.findBySubscriptionAndPkg(newSub,sp.pkg))
            }
            isTargetSubChanged = true
        }

        if (params.subscription?.deleteEntitlementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<IssueEntitlement> entitlementsToDelete = params.list('subscription.deleteEntitlementIds').collect{ genericOIDService.resolveOID(it)}
            deleteEntitlements(entitlementsToDelete, newSub, flash)
            isTargetSubChanged = true
        }
        if (params.subscription?.takeEntitlementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<IssueEntitlement> entitlementsToTake = params.list('subscription.takeEntitlementIds').collect{ genericOIDService.resolveOID(it)}
            copyEntitlements(entitlementsToTake, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }
        result.newSub = newSub
        result.subscription = baseSub
        result
    }

    boolean deleteTasks(List<Long> toDeleteTasks, Subscription targetSub, def flash) {
        boolean isInstAdm = contextService.getUser().hasAffiliation("INST_ADM")
        def userId = contextService.user.id
        toDeleteTasks.each { deleteTaskId ->
            Task dTask = Task.get(deleteTaskId)
            if (dTask) {
                if (dTask.creator.id == userId || isInstAdm) {
                    delete(dTask, flash)
                } else {
                    Object[] args = [messageSource.getMessage('task.label', null, locale), deleteTaskId]
                    flash.error += messageSource.getMessage('default.not.deleted.notAutorized.message', args, locale)
                }
            } else {
                Object[] args = [deleteTaskId]
                flash.error += messageSource.getMessage('subscription.err.taskDoesNotExist', args, locale)
            }
        }
    }

    boolean copyTasks(Subscription sourceSub, def toCopyTasks, Subscription targetSub, def flash) {
        toCopyTasks.each { tsk ->
            def task = Task.findBySubscriptionAndId(sourceSub, tsk)
            if (task) {
                if (task.status != RDStore.TASK_STATUS_DONE) {
                    Task newTask = new Task()
                    InvokerHelper.setProperties(newTask, task.properties)
                    newTask.systemCreateDate = new Date()
                    newTask.subscription = targetSub
                    save(newTask, flash)
                }
            }
        }
    }

    boolean copyAnnouncements(Subscription sourceSub, def toCopyAnnouncements, Subscription targetSub, def flash) {
        sourceSub.documents?.each { dctx ->
            if (dctx.id in toCopyAnnouncements) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                    Doc newDoc = new Doc()
                    InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                    save(newDoc, flash)
                    DocContext newDocContext = new DocContext()
                    InvokerHelper.setProperties(newDocContext, dctx.properties)
                    newDocContext.subscription = targetSub
                    newDocContext.owner = newDoc
                    save(newDocContext, flash)
                }
            }
        }
    }

    def deleteAnnouncements(List<Long> toDeleteAnnouncements, Subscription targetSub, def flash) {
        targetSub.documents.each {
            if (toDeleteAnnouncements.contains(it.id) && it.owner?.contentType == Doc.CONTENT_TYPE_STRING  && !(it.domain)){
                Map params = [deleteId: it.id]
                log.debug("deleteDocuments ${params}");
                docstoreService.unifiedDeleteDocuments(params)
            }
        }
    }

    boolean deleteDates(Subscription targetSub, def flash){
        targetSub.startDate = null
        targetSub.endDate = null
        return save(targetSub, flash)
    }

    boolean copyDates(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.setStartDate(sourceSub.getStartDate())
        targetSub.setEndDate(sourceSub.getEndDate())
        return save(targetSub, flash)
    }

    void copyIdentifiers(Subscription baseSub, List<String> toCopyIdentifiers, Subscription newSub, def flash) {
        toCopyIdentifiers.each{ identifierId ->
            def ownerSub = newSub
            Identifier sourceIdentifier = Identifier.get(identifierId)
            IdentifierNamespace namespace = sourceIdentifier.ns
            String value = sourceIdentifier.value

            if (ownerSub && namespace && value) {
                FactoryResult factoryResult = Identifier.constructWithFactoryResult([value: value, reference: ownerSub, namespace: namespace])

                factoryResult.setFlashScopeByStatus(flash)
            }
        }
    }

    void deleteIdentifiers(List<String> toDeleteIdentifiers, Subscription newSub, def flash) {
        int countDeleted = Identifier.executeUpdate('delete from Identifier i where i.id in (:toDeleteIdentifiers) and i.sub = :sub',
                [toDeleteIdentifiers: toDeleteIdentifiers, sub: newSub])
        Object[] args = [countDeleted]
        flash.message += messageSource.getMessage('identifier.delete.success', args, locale)
    }

    def deleteDocs(List<Long> toDeleteDocs, Subscription targetSub, def flash) {
        log.debug("toDeleteDocCtxIds: " + toDeleteDocs)
        def updated = DocContext.executeUpdate("UPDATE DocContext set status = :del where id in (:ids)",
                [del: RDStore.DOC_CTX_STATUS_DELETED, ids: toDeleteDocs])
        log.debug("Number of deleted (per Flag) DocCtxs: " + updated)
    }

    boolean copyDocs(Subscription sourceSub, def toCopyDocs, Subscription targetSub, def flash) {
        sourceSub.documents?.each { dctx ->
            if (dctx.id in toCopyDocs) {
                if (((dctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (dctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (dctx.status?.value != 'Deleted')) {
                    try {

                        Doc newDoc = new Doc()
                        InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                        save(newDoc, flash)

                        DocContext newDocContext = new DocContext()
                        InvokerHelper.setProperties(newDocContext, dctx.properties)
                        newDocContext.subscription = targetSub
                        newDocContext.owner = newDoc
                        save(newDocContext, flash)

                        String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'

                        Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                        Path target = new File("${fPath}/${newDoc.uuid}").toPath()
                        Files.copy(source, target)

                    }
                    catch (Exception e) {
                        log.error("Problem by Saving Doc in documentStorageLocation (Doc ID: ${dctx.owner.id} -> ${e})")
                    }
                }
            }
        }
    }

    boolean copyProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        SubscriptionProperty targetProp


        properties.each { AbstractPropertyWithCalculatedLastUpdated sourceProp ->
            targetProp = targetSub.propertySet.find { it.typeId == sourceProp.typeId && sourceProp.tenant.id == sourceProp.tenant }
            boolean isAddNewProp = sourceProp.type?.multipleOccurrence
            if ( (! targetProp) || isAddNewProp) {
                targetProp = new SubscriptionProperty(type: sourceProp.type, owner: targetSub, tenant: sourceProp.tenant)
                targetProp = sourceProp.copyInto(targetProp)
                targetProp.isPublic = sourceProp.isPublic //provisoric, should be moved into copyInto once migration is complete
                save(targetProp, flash)
                if (((sourceProp.id.toString() in auditProperties)) && targetProp.isPublic) {
                    //copy audit
                    if (!AuditConfig.getConfig(targetProp, AuditConfig.COMPLETE_OBJECT)) {

                        Subscription.findAllByInstanceOf(targetSub).each { Subscription member ->

                            def existingProp = SubscriptionProperty.findByOwnerAndInstanceOf(member, targetProp)
                            if (! existingProp) {

                                // multi occurrence props; add one additional with backref
                                if (sourceProp.type.multipleOccurrence) {
                                    def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, targetProp.type)
                                    additionalProp = targetProp.copyInto(additionalProp)
                                    additionalProp.instanceOf = targetProp
                                    additionalProp.save(flush: true)
                                }
                                else {
                                    def matchingProps = SubscriptionProperty.findByOwnerAndType(member, targetProp.type)
                                    // unbound prop found with matching type, set backref
                                    if (matchingProps) {
                                        matchingProps.each { memberProp ->
                                            memberProp.instanceOf = targetProp
                                            memberProp.save(flush: true)
                                        }
                                    }
                                    else {
                                        // no match found, creating new prop with backref
                                        def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, targetProp.type)
                                        newProp = targetProp.copyInto(newProp)
                                        newProp.instanceOf = targetProp
                                        newProp.save(flush: true)
                                    }
                                }
                            }
                        }

                        def auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(SubscriptionProperty.class.name, sourceProp.id)
                        auditConfigs.each {
                            AuditConfig ac ->
                                //All ReferenceFields were copied!
                                AuditConfig.addConfig(targetProp, ac.referenceField)
                        }
                        if (!auditConfigs) {
                            AuditConfig.addConfig(targetProp, AuditConfig.COMPLETE_OBJECT)
                        }
                    }
                }
            } else {
                Object[] args = [sourceProp.type.getI10n("name") ?: sourceProp.class.getSimpleName()]
                flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
            }
        }
    }

    boolean deleteProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        if (true){
            properties.each { AbstractPropertyWithCalculatedLastUpdated prop ->
                AuditConfig.removeAllConfigs(prop)
            }
        }
        int anzCP = SubscriptionProperty.executeUpdate("delete from SubscriptionProperty p where p in (:properties) and p.tenant = :org and p.isPublic = true",[properties: properties, org: contextService.org])
        int anzPP = SubscriptionProperty.executeUpdate("delete from SubscriptionProperty p where p in (:properties) and p.tenant = :org and p.isPublic = false",[properties: properties, org: contextService.org])
    }

    boolean deleteStatus(Subscription targetSub, def flash) {
        targetSub.status = RDStore.SUBSCRIPTION_NO_STATUS
        return save(targetSub, flash)
    }

    boolean copyStatus(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.status = sourceSub.status ?: null
        return save(targetSub, flash)
    }

    boolean deleteKind(Subscription targetSub, def flash) {
        targetSub.kind = null
        return save(targetSub, flash)
    }

    boolean copyKind(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.kind = sourceSub.kind ?: null
        return save(targetSub, flash)
    }

    boolean deleteForm(Subscription targetSub, def flash) {
        targetSub.form = null
        return save(targetSub, flash)
    }

    boolean copyForm(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.form = sourceSub.form ?: null
        return save(targetSub, flash)
    }

    boolean deleteResource(Subscription targetSub, def flash) {
        targetSub.resource = null
        return save(targetSub, flash)
    }

    boolean copyResource(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.resource = sourceSub.resource ?: null
        return save(targetSub, flash)
    }

    boolean deletePublicForApi(Subscription targetSub, def flash) {
        targetSub.isPublicForApi = false
        return save(targetSub, flash)
    }

    boolean copyPublicForApi(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.isPublicForApi = sourceSub.isPublicForApi
        return save(targetSub, flash)
    }

    boolean deletePerpetualAccess(Subscription targetSub, def flash) {
        targetSub.hasPerpetualAccess = false
        return save(targetSub, flash)
    }

    boolean copyPerpetualAccess(Subscription sourceSub, Subscription targetSub, def flash) {
        //Vertrag/License
        targetSub.hasPerpetualAccess = sourceSub.hasPerpetualAccess
        return save(targetSub, flash)
    }

    boolean copyObjectProperty(Object sourceObject, Object targetObject, def flash, String propertyName) {

        if(sourceObject.getClass() == targetObject.getClass())
        {
            if (sourceObject.hasProperty(propertyName)) {
                targetObject[propertyName] = sourceObject."$propertyName"
                return save(targetObject, flash)
            }
        }

    }

    boolean deleteObjectProperty(Object targetObject, def flash, String propertyName) {

        if (targetObject.hasProperty(propertyName)) {
            if(targetObject[propertyName] instanceof Boolean){
                targetObject[propertyName] = false
            }else {
                targetObject[propertyName] = null
            }
            return save(targetObject, flash)
        }

    }

    boolean toggleAuditObjectProperty(Object sourceObject, Object targetObject, def flash, String propertyName) {

        if(sourceObject.getClass() == targetObject.getClass())
        {
            if (sourceObject.hasProperty(propertyName) && !AuditConfig.getConfig(targetObject,propertyName)) {
                AuditConfig.addConfig(targetObject,propertyName)
            }
        }

    }

    boolean removeToggleAuditObjectProperty(Object targetObject, def flash, String propertyName) {
            if (targetObject.hasProperty(propertyName) && AuditConfig.getConfig(targetObject, propertyName)) {
                    AuditConfig.removeConfig(targetObject, propertyName)
            }
    }

    boolean deleteLicenses(List<License> toDeleteLicenses, Subscription targetSub, def flash) {
        toDeleteLicenses.each { License lic ->
            subscriptionService.setOrgLicRole(targetSub,lic,true)
        }
    }

    boolean copyLicenses(List<License> toCopyLicenses, Subscription targetSub, def flash) {
        toCopyLicenses.each { License lic ->
            subscriptionService.setOrgLicRole(targetSub,lic,false)
        }
    }

    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Subscription targetSub, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetSub, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
    }

    boolean deleteSpecificSubscriptionEditors(List<PersonRole> toDeletePersonRoles, Subscription targetSub, def flash) {
        PersonRole.executeUpdate(
                "delete from PersonRole pr where pr in (:personRoles) and pr.sub = :sub",
                [personRoles: toDeletePersonRoles, sub: targetSub]
        )
    }

    boolean copyOrgRelations(List<OrgRole> toCopyOrgRelations, Subscription sourceSub, Subscription targetSub, def flash) {
        sourceSub.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
                if (targetSub.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                    Object[] args = [or?.roleType?.getI10n("value") + " " + or?.org?.name]
                    flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
                } else {
                    def newProperties = or.properties

                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, newProperties)
                    //Vererbung ausschalten
                    newOrgRole.sharedFrom  = null
                    newOrgRole.isShared = false
                    newOrgRole.sub = targetSub
                    save(newOrgRole, flash)
                }
            }
        }
    }

    boolean copySpecificSubscriptionEditors(List<PersonRole> toCopyPersonRoles, Subscription sourceSub, Subscription targetSub, def flash) {

        toCopyPersonRoles.each { prRole ->
            if(!(prRole.org in targetSub.orgRelations.org) && (prRole.org in sourceSub.orgRelations.org)){
                OrgRole or = OrgRole.findByOrgAndSub(prRole.org, sourceSub)
                def newProperties = or.properties

                OrgRole newOrgRole = new OrgRole()
                InvokerHelper.setProperties(newOrgRole, newProperties)
                //Vererbung ausschalten
                newOrgRole.sharedFrom  = null
                newOrgRole.isShared = false
                newOrgRole.sub = targetSub
            }

            if((prRole.org in targetSub.orgRelations.org) && !PersonRole.findWhere(prs: prRole.prs, org: prRole.org, responsibilityType: prRole.responsibilityType, sub: targetSub)){
                PersonRole newPrsRole = new PersonRole(prs: prRole.prs, org: prRole.org, sub: targetSub, responsibilityType: prRole.responsibilityType)
                save(newPrsRole, flash)
            }
        }

    }

    boolean deletePackages(List<SubscriptionPackage> packagesToDelete, Subscription targetSub, def flash) {
        //alle IEs löschen, die zu den zu löschenden Packages gehören
//        targetSub.issueEntitlements.each{ ie ->
        subscriptionService.getIssueEntitlements(targetSub).each{ ie ->
            if (packagesToDelete.find { subPkg -> subPkg?.pkg?.id == ie?.tipp?.pkg?.id } ) {
                ie.status = RDStore.TIPP_STATUS_DELETED
                save(ie, flash)
            }
        }

        //alle zugeordneten Packages löschen
        if (packagesToDelete) {

            packagesToDelete.each { subPkg ->
                OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg=?", [subPkg])
                PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp",[sp:subPkg])

                CostItem.findAllBySubPkg(subPkg).each { costItem ->
                    costItem.subPkg = null
                    if(!costItem.sub){
                        costItem.sub = subPkg.subscription
                    }
                    costItem.save(flush: true)
                }
            }

            SubscriptionPackage.executeUpdate(
                    "delete from SubscriptionPackage sp where sp in (:packagesToDelete) and sp.subscription = :sub ",
                    [packagesToDelete: packagesToDelete, sub: targetSub])
        }
    }

    boolean copyPackages(List<SubscriptionPackage> packagesToTake, Subscription targetSub, def flash) {
        packagesToTake.each { subscriptionPackage ->
            if (targetSub.packages?.find { it.pkg?.id == subscriptionPackage.pkg?.id }) {
                Object[] args = [subscriptionPackage.pkg.name]
                flash.error += messageSource.getMessage('subscription.err.packageAlreadyExistsInTargetSub', args, locale)
            }
            else {

                List<OrgAccessPointLink> pkgOapls = OrgAccessPointLink.findAllByIdInList(subscriptionPackage.oapls.id)
                subscriptionPackage.properties.oapls = null
                subscriptionPackage.properties.pendingChangeConfig = null //copied in next step
                SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                InvokerHelper.setProperties(newSubscriptionPackage, subscriptionPackage.properties)
                newSubscriptionPackage.subscription = targetSub

                if(save(newSubscriptionPackage, flash)){
                    pkgOapls.each{ oapl ->

                        def oaplProperties = oapl.properties
                        oaplProperties.globalUID = null
                        OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                        InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                        newOrgAccessPointLink.subPkg = newSubscriptionPackage
                        newOrgAccessPointLink.save()
                    }
                }
            }
        }
    }

    boolean copyPendingChangeConfiguration(Collection<PendingChangeConfiguration> configs, SubscriptionPackage target) {
        configs.each { PendingChangeConfiguration config ->
            Map<String,Object> configSettings = [subscriptionPackage:target,settingValue:config.settingValue,settingKey:config.settingKey,withNotification:config.withNotification]
            PendingChangeConfiguration newPcc = PendingChangeConfiguration.construct(configSettings)
            if(newPcc) {
                if(AuditConfig.getConfig(config.subscriptionPackage.subscription,config.settingKey) && !AuditConfig.getConfig(target.subscription,config.settingKey))
                    AuditConfig.addConfig(target.subscription,config.settingKey)
                else if(!AuditConfig.getConfig(config.subscriptionPackage.subscription,config.settingKey) && AuditConfig.getConfig(target.subscription,config.settingKey))
                    AuditConfig.removeConfig(target.subscription,config.settingKey)
            }
        }
    }

    boolean deleteEntitlements(List<IssueEntitlement> entitlementsToDelete, Subscription targetSub, def flash) {
        entitlementsToDelete.each {
            it.status = RDStore.TIPP_STATUS_DELETED
            save(it, flash)
        }
//        IssueEntitlement.executeUpdate(
//                "delete from IssueEntitlement ie where ie in (:entitlementsToDelete) and ie.subscription = :sub ",
//                [entitlementsToDelete: entitlementsToDelete, sub: targetSub])
    }

    boolean copyEntitlements(List<IssueEntitlement> entitlementsToTake, Subscription targetSub, def flash) {
        entitlementsToTake.each { ieToTake ->
            if (ieToTake.status != RDStore.TIPP_STATUS_DELETED) {
                def list = subscriptionService.getIssueEntitlements(targetSub).findAll{it.tipp.id == ieToTake.tipp.id && it.status != RDStore.TIPP_STATUS_DELETED}
                if (list?.size() > 0) {
                    // mich gibts schon! Fehlermeldung ausgeben!
                    Object[] args = [ieToTake.tipp.title.title]
                    flash.error += messageSource.getMessage('subscription.err.titleAlreadyExistsInTargetSub', args, locale)
                } else {
                    def properties = ieToTake.properties
                    properties.globalUID = null
                    IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                    InvokerHelper.setProperties(newIssueEntitlement, properties)
                    newIssueEntitlement.coverages = null
                    newIssueEntitlement.ieGroups = null
                    newIssueEntitlement.subscription = targetSub

                    if(save(newIssueEntitlement, flash)){
                        ieToTake.properties.coverages.each{ coverage ->

                            def coverageProperties = coverage.properties
                            IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                            InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                            newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement
                            newIssueEntitlementCoverage.save(flush: true)
                        }
                    }
                }
            }
        }
    }

    private boolean save(obj, flash){
        if (obj.save(flush:true)){
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, locale)
            return false
        }
    }

    private boolean delete(obj, flash) {
        if (obj) {
            obj.delete(flush: true)
            log.debug("Delete ${obj} ok")
        } else {
            flash.error += messageSource.getMessage('default.delete.error.general.message', null, locale)
        }
    }

    boolean isBothSubscriptionsSet(Subscription baseSub, Subscription newSub) {
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        if (! baseSub || !newSub) {
            if (!baseSub) flash.error += messageSource.getMessage('subscription.details.copyElementsIntoSubscription.noSubscriptionSource', null, locale) + '<br />'
            if (!newSub)  flash.error += messageSource.getMessage('subscription.details.copyElementsIntoSubscription.noSubscriptionTarget', null, locale) + '<br />'
            return false
        }
        return true
    }

}
