package de.laser

import com.k_int.kbplus.*
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import com.k_int.properties.PropertyDefinitionGroupItem
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.exceptions.CreationException
import de.laser.helper.ConfigUtils
import de.laser.helper.FactoryResult
import de.laser.helper.RDStore
import de.laser.interfaces.ShareSupport
import grails.transaction.Transactional
import grails.util.Holders
import org.apache.lucene.index.DocIDMerger
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
    LicenseService licenseService

    static final String WORKFLOW_DATES_OWNER_RELATIONS = '1'
    static final String WORKFLOW_PACKAGES_ENTITLEMENTS = '5'
    static final String WORKFLOW_DOCS_ANNOUNCEMENT_TASKS = '2'
    static final String WORKFLOW_SUBSCRIBER = '3'
    static final String WORKFLOW_PROPERTIES = '4'
    static final String WORKFLOW_END = '6'

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
    }

    List allowedProperties(Object obj) {
        List result = []
        switch (obj.class.simpleName) {
            case License.class.simpleName:
                result = ['startDate', 'endDate', 'status', 'type', 'licenseUrl', 'licenseCategory', 'openEnded', 'isPublicForApi']
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
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null

        result.sourceIdentifiers = sourceObject.ids?.sort { x, y ->
            if (x.ns?.ns?.toLowerCase() == y.ns?.ns?.toLowerCase()) {
                x.value <=> y.value
            } else {
                x.ns?.ns?.toLowerCase() <=> y.ns?.ns?.toLowerCase()
            }
        }
        result.targetIdentifiers = targetObject?.ids?.sort { x, y ->
            if (x.ns?.ns?.toLowerCase() == y.ns?.ns?.toLowerCase()) {
                x.value <=> y.value
            } else {
                x.ns?.ns?.toLowerCase() <=> y.ns?.ns?.toLowerCase()
            }
        }

        if (sourceObject instanceof Subscription) {
            String sourceLicensesQuery = "select l from License l where concat('${License.class.name}:',l.id) in (select li.source from Links li where li.destination = :sub and li.linkType = :linkType) order by l.sortableReference asc"
            result.sourceLicenses = License.executeQuery(sourceLicensesQuery, [sub: GenericOIDService.getOID(sourceObject), linkType: RDStore.LINKTYPE_LICENSE])
            if (targetObject) {
                String targetLicensesQuery = "select l from License l where concat('${License.class.name}:',l.id) in (select li.source from Links li where li.destination = :sub and li.linkType = :linkType) order by l.sortableReference asc"
                result.targetLicenses = License.executeQuery(targetLicensesQuery, [sub: GenericOIDService.getOID(targetObject), linkType: RDStore.LINKTYPE_LICENSE])
            }

            // restrict visible for templates/links/orgLinksAsList
            result.source_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(sourceObject)
            result.target_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(targetObject)
        }

        if (sourceObject instanceof License) {
            // restrict visible for templates/links/orgLinksAsList
            result.source_visibleOrgRelations = licenseService.getVisibleOrgRelations(sourceObject)
            result.target_visibleOrgRelations = licenseService.getVisibleOrgRelations(targetObject)
        }

        result
    }

    Map loadDataFor_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result.sourceTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.sourceObject)
        result.targetTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.targetObject)
        result
    }

    Map loadDataFor_Subscriber(Map params) {
        Map<String, Object> result = [:]
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        result.validSourceSubChilds = subscriptionService.getValidSubChilds(result.sourceObject)
        if (params.targetObjectId) {
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
            result.validTargetSubChilds = subscriptionService.getValidSubChilds(result.targetObject)
        }
        result
    }

    Map loadDataFor_Properties(Map params) {
        LinkedHashMap result = [customProperties: [:], privateProperties: [:]]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        List<Object> objectsToCompare = [sourceObject]
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
            objectsToCompare.add(targetObject)
        }

        Org contextOrg = contextService.org

        result = regroupObjectProperties(objectsToCompare, contextOrg)

        if (targetObject) {
            result.targetObject = targetObject.refresh()
        }
        result
    }

    /*Map loadDataFor_MyProperties(Map params) {
        LinkedHashMap result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        List<Object> objectsToCompare = [sourceObject]
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
            objectsToCompare.add(targetObject)
        }


        Org contextOrg = contextService.org
        *//*objectsToCompare.each { Object obj ->
            Map customProperties = result.customProperties
            customProperties = comparisonService.buildComparisonTree(customProperties, obj, obj.propertySet.findAll { it.type.tenant == null && it.tenant?.id == contextOrg.id }.sort { it.type.getI10n('name') })
            result.customProperties = customProperties
            Map privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties, obj, obj.propertySet.findAll { it.type.tenant?.id == contextOrg.id }.sort { it.type.getI10n('name') })
            result.privateProperties = privateProperties
        }*//*

        result = regroupObjectProperties(objectsToCompare, contextOrg)

        if (targetObject) {
            result.targetObject = targetObject.refresh()
        }

        result
    }*/

    Map loadDataFor_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null
        result.sourceIEs = subscriptionService.getIssueEntitlements(sourceObject)
        result.targetIEs = subscriptionService.getIssueEntitlements(targetObject)
        result.targetObject = targetObject
        result.sourceObject = sourceObject
        result
    }

    void copySubscriber(List<Subscription> subscriptionToTake, Object targetObject, def flash) {
        targetObject.refresh()
        List<Subscription> targetChildSubs = subscriptionService.getValidSubChilds(targetObject)
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
                        status: targetObject.status,
                        name: subMember.name,
                        startDate: subMember.isMultiYear ? subMember.startDate : targetObject.startDate,
                        endDate: subMember.isMultiYear ? subMember.endDate : targetObject.endDate,
                        manualRenewalDate: subMember.manualRenewalDate,
                        /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                        identifier: UUID.randomUUID().toString(),
                        instanceOf: targetObject,
                        //previousSubscription: subMember?.id,
                        isSlaved: subMember.isSlaved,
                        resource: targetObject.resource ?: null,
                        form: targetObject.form ?: null,
                        isPublicForApi: targetObject.isPublicForApi,
                        hasPerpetualAccess: targetObject.hasPerpetualAccess,
                        administrative: subMember.administrative
                )
                newSubscription.save()
                //ERMS-892: insert preceding relation in new data model
                if (subMember) {
                    try {
                        Links.construct([source: GenericOIDService.getOID(newSubscription), destination: GenericOIDService.getOID(subMember), linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org])
                        Set<Links> precedingLicenses = Links.findAllByDestinationAndLinkType(GenericOIDService.getOID(subMember), RDStore.LINKTYPE_LICENSE)
                        precedingLicenses.each { Links link ->
                            Map<String, Object> successorLink = [source: link.source, destination: GenericOIDService.getOID(newSubscription), linkType: RDStore.LINKTYPE_LICENSE, owner: contextService.org]
                            Links.construct(successorLink)
                        }
                    }
                    catch (CreationException e) {
                        log.error("Subscription linking failed, please check: ${e.stackTrace}")
                    }
                }

                if (subMember.propertySet) {
                    Org org = contextService.getOrg()
                        //customProperties of ContextOrg && privateProperties of ContextOrg
                        subMember.propertySet.each {subProp ->
                            if((subProp.type.tenant == null && (subProp.tenant?.id == org.id || subProp.tenant == null)) || subProp.type.tenant?.id == org.id)
                            {
                                SubscriptionProperty copiedProp = new SubscriptionProperty(type: subProp.type, owner: newSubscription, isPublic: subProp.isPublic, tenant: subProp.tenant)
                                copiedProp = subProp.copyInto(copiedProp)
                                copiedProp.save()
                            }
                        }

                   /* for (prop in subMember.propertySet) {
                        SubscriptionProperty copiedProp = new SubscriptionProperty(type: prop.type, owner: newSubscription, isPublic: prop.isPublic, tenant: prop.tenant)
                        copiedProp = prop.copyInto(copiedProp)
                        copiedProp.save()
                        //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                    }*/
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

                if (subMember.packages && targetObject.packages) {
                    //Package
                    subMember.packages?.each { pkg ->
                        def pkgOapls = pkg.oapls
                        pkg.properties.oapls = null
                        pkg.properties.pendingChangeConfig = null
                        SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                        InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                        newSubscriptionPackage.subscription = newSubscription

                        if (newSubscriptionPackage.save()) {
                            pkgOapls.each { oapl ->

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
                if (subMember.issueEntitlements && targetObject.issueEntitlements) {
                    subMember.issueEntitlements?.each { ie ->
                        if (ie.status != RDStore.TIPP_STATUS_DELETED) {
                            def ieProperties = ie.properties
                            ieProperties.globalUID = null

                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                            InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                            newIssueEntitlement.coverages = null
                            newIssueEntitlement.ieGroups = null
                            newIssueEntitlement.subscription = newSubscription

                            if (save(newIssueEntitlement, flash)) {
                                ie.properties.coverages.each { coverage ->

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
                    if ((or.org.id == contextService.getOrg().id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIPTION_COLLECTIVE]) || (targetObject.orgRelations.size() >= 1)) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = newSubscription
                        newOrgRole.save(flush: true)
                        log.debug("new org role set: ${newOrgRole.sub} for ${newOrgRole.org.sortname}")
                    }
                }

                if (subMember.prsLinks && targetObject.prsLinks) {
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

    Map copyObjectElements_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null

        if (formService.validateToken(params)) {

            params.list('copyObject.take').each { takeProperty ->
                if (takeProperty in allowedProperties(sourceObject)) {
                    copyObjectProperty(sourceObject, targetObject, flash, takeProperty)
                }
            }

            allowedProperties(sourceObject).each { allowedProperty ->
                if (allowedProperty in params.list('copyObject.take')) {
                    if (allowedProperty in params.list('copyObject.toggleAudit')) {
                        toggleAuditObjectProperty(sourceObject, targetObject, flash, allowedProperty)
                    } else {
                        removeToggleAuditObjectProperty(targetObject, flash, allowedProperty)
                    }
                }

            }

            if (params.list('copyObject.deleteLicenses') && isBothObjectsSet(sourceObject, targetObject)) {
                List<License> toDeleteLicenses = params.list('copyObject.deleteLicenses').collect { genericOIDService.resolveOID(it) }
                deleteLicenses(toDeleteLicenses, targetObject, flash)
            } else if (params.list('copyObject.takeLicenses') && isBothObjectsSet(sourceObject, targetObject)) {
                List<License> toCopyLicenses = params.list('copyObject.takeLicenses').collect { genericOIDService.resolveOID(it) }
                copyLicenses(toCopyLicenses, targetObject, flash)
            }

            if (params.list('copyObject.deleteOrgRelations') && isBothObjectsSet(sourceObject, targetObject)) {
                List<OrgRole> toDeleteOrgRelations = params.list('copyObject.deleteOrgRelations').collect { genericOIDService.resolveOID(it) }
                deleteOrgRelations(toDeleteOrgRelations, targetObject, flash)
                //isTargetSubChanged = true
            }
            if (params.list('copyObject.takeOrgRelations') && isBothObjectsSet(sourceObject, targetObject)) {
                List<OrgRole> toCopyOrgRelations = params.list('copyObject.takeOrgRelations').collect { genericOIDService.resolveOID(it) }
                copyOrgRelations(toCopyOrgRelations, sourceObject, targetObject, flash)
                //isTargetSubChanged = true

                List<OrgRole> toggleShareOrgRoles = params.list('toggleShareOrgRoles').collect {
                    genericOIDService.resolveOID(it)
                }

                targetObject = targetObject.refresh()
                targetObject.orgRelations.each { newSubOrgRole ->

                    if (newSubOrgRole.org in toggleShareOrgRoles.org) {
                        newSubOrgRole.isShared = true
                        newSubOrgRole.save(flush: true)
                        ((ShareSupport) targetObject).updateShare(newSubOrgRole)
                    }
                }
            }

            if (params.list('subscription.deleteSpecificSubscriptionEditors') && isBothObjectsSet(sourceObject, targetObject)) {
                List<PersonRole> toDeleteSpecificSubscriptionEditors = params.list('subscription.deleteSpecificSubscriptionEditors').collect { genericOIDService.resolveOID(it) }
                deleteSpecificSubscriptionEditors(toDeleteSpecificSubscriptionEditors, targetObject, flash)
                //isTargetSubChanged = true
            }
            if (params.list('subscription.takeSpecificSubscriptionEditors') && isBothObjectsSet(sourceObject, targetObject)) {
                List<PersonRole> toCopySpecificSubscriptionEditors = params.list('subscription.takeSpecificSubscriptionEditors').collect { genericOIDService.resolveOID(it) }
                copySpecificSubscriptionEditors(toCopySpecificSubscriptionEditors, sourceObject, targetObject, flash)
                //isTargetSubChanged = true
            }

            if (params.list('copyObject.deleteIdentifierIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toDeleteIdentifiers = []
                params.list('copyObject.deleteIdentifierIds').each { identifier -> toDeleteIdentifiers << Long.valueOf(identifier) }
                deleteIdentifiers(toDeleteIdentifiers, targetObject, flash)
                //isTargetSubChanged = true
            }

            if (params.list('copyObject.takeIdentifierIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toCopyIdentifiers = []
                params.list('copyObject.takeIdentifierIds').each { identifier -> toCopyIdentifiers << Long.valueOf(identifier) }
                copyIdentifiers(sourceObject, toCopyIdentifiers, targetObject, flash)
                //isTargetSubChanged = true
            }
        }

        /*if (isTargetSubChanged) {
            targetObject = targetObject.refresh()
        }*/
        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    Map copyObjectElements_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        if (formService.validateToken(params)) {
            boolean isTargetSubChanged = false
            if (params.list('copyObject.deleteDocIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toDeleteDocs = []
                params.list('copyObject.deleteDocIds').each { doc -> toDeleteDocs << Long.valueOf(doc) }
                deleteDocs(toDeleteDocs, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.takeDocIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toCopyDocs = []
                params.list('copyObject.takeDocIds').each { doc -> toCopyDocs << Long.valueOf(doc) }
                copyDocs(sourceObject, toCopyDocs, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.deleteAnnouncementIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toDeleteAnnouncements = []
                params.list('copyObject.deleteAnnouncementIds').each { announcement -> toDeleteAnnouncements << Long.valueOf(announcement) }
                deleteAnnouncements(toDeleteAnnouncements, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.takeAnnouncementIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toCopyAnnouncements = []
                params.list('copyObject.takeAnnouncementIds').each { announcement -> toCopyAnnouncements << Long.valueOf(announcement) }
                copyAnnouncements(sourceObject, toCopyAnnouncements, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.deleteTaskIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toDeleteTasks = []
                params.list('copyObject.deleteTaskIds').each { tsk -> toDeleteTasks << Long.valueOf(tsk) }
                deleteTasks(toDeleteTasks, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.takeTaskIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toCopyTasks = []
                params.list('copyObject.takeTaskIds').each { tsk -> toCopyTasks << Long.valueOf(tsk) }
                copyTasks(sourceObject, toCopyTasks, targetObject, flash)
                isTargetSubChanged = true
            }

            if (isTargetSubChanged) {
                targetObject = targetObject.refresh()
            }
        }

        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    Map copyObjectElements_Identifiers(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }
        boolean isTargetSubChanged = false

        if (params.copyObject?.deleteIdentifierIds && isBothObjectsSet(sourceObject, targetObject)) {
            def toDeleteIdentifiers = []
            params.list('copyObject.deleteIdentifierIds').each { identifier -> toDeleteIdentifiers << Long.valueOf(identifier) }
            deleteIdentifiers(toDeleteIdentifiers, targetObject, flash)
            isTargetSubChanged = true
        }

        if (params.copyObject?.takeIdentifierIds && isBothObjectsSet(sourceObject, targetObject)) {
            def toCopyIdentifiers = []
            params.list('copyObject.takeIdentifierIds').each { identifier -> toCopyIdentifiers << Long.valueOf(identifier) }
            copyIdentifiers(sourceObject, toCopyIdentifiers, targetObject, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            targetObject = targetObject.refresh()
        }

        result.flash = flash
        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    Map copyObjectElements_Subscriber(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        if (formService.validateToken(params)) {

            if (params.copyObject?.copySubscriber && isBothObjectsSet(sourceObject, targetObject)) {
                List<Subscription> toCopySubs = params.list('copyObject.copySubscriber').collect { genericOIDService.resolveOID(it) }
                copySubscriber(toCopySubs, targetObject, flash)
            }
        }

        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    Map copyObjectElements_Properties(Map params) {
        LinkedHashMap result = [customProperties: [:], privateProperties: [:]]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        boolean isRenewSub = params.isRenewSub ? true : false

        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)

        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        List auditProperties = params.list('auditProperties')

        List<AbstractPropertyWithCalculatedLastUpdated> propertiesToDelete = params.list('copyObject.deleteProperty').collect { genericOIDService.resolveOID(it) }
        if (propertiesToDelete && isBothObjectsSet(sourceObject, targetObject)) {
            deleteProperties(propertiesToDelete, targetObject, isRenewSub, flash, auditProperties)
        }

        List<AbstractPropertyWithCalculatedLastUpdated> propertiesToTake = params.list('copyObject.takeProperty').collect { genericOIDService.resolveOID(it) }
        if (propertiesToTake && isBothObjectsSet(sourceObject, targetObject)) {
            copyProperties(propertiesToTake, targetObject, isRenewSub, flash, auditProperties)
        }

        if (targetObject) {
            result.targetObject = targetObject.refresh()
        }
        result
    }

    Map copyObjectElements_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null

        if (formService.validateToken(params)) {
            boolean isTargetSubChanged = false
            if (params.subscription?.deletePackageIds && isBothObjectsSet(sourceObject, targetObject)) {
                List<SubscriptionPackage> packagesToDelete = params.list('subscription.deletePackageIds').collect { genericOIDService.resolveOID(it) }
                deletePackages(packagesToDelete, targetObject, flash)
                isTargetSubChanged = true
            }
            if (params.subscription?.takePackageIds && isBothObjectsSet(sourceObject, targetObject)) {
                List<SubscriptionPackage> packagesToTake = params.list('subscription.takePackageIds').collect { genericOIDService.resolveOID(it) }
                copyPackages(packagesToTake, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.subscription?.deletePackageSettings && isBothObjectsSet(sourceObject, targetObject)) {
                List<SubscriptionPackage> packageSettingsToDelete = params.list('subscription.deletePackageSettings').collect {
                    genericOIDService.resolveOID(it)
                }
                packageSettingsToDelete.each { SubscriptionPackage toDelete ->
                    PendingChangeConfiguration.SETTING_KEYS.each { String setting ->
                        if (AuditConfig.getConfig(toDelete.subscription, setting))
                            AuditConfig.removeConfig(toDelete.subscription, setting)
                    }
                    PendingChangeConfiguration.executeUpdate('delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage = :sp', [sp: toDelete])
                }
                isTargetSubChanged = true
            }
            if (params.subscription?.takePackageSettings && isBothObjectsSet(sourceObject, targetObject)) {
                List<SubscriptionPackage> packageSettingsToTake = params.list('subscription.takePackageSettings').collect {
                    genericOIDService.resolveOID(it)
                }
                packageSettingsToTake.each { SubscriptionPackage sp ->
                    //explicit loading of service needed because lazy initialisation gives null
                    copyPendingChangeConfiguration(PendingChangeConfiguration.findAllBySubscriptionPackage(sp), SubscriptionPackage.findBySubscriptionAndPkg(targetObject, sp.pkg))
                }
                isTargetSubChanged = true
            }

            if (params.subscription?.deleteEntitlementIds && isBothObjectsSet(sourceObject, targetObject)) {
                List<IssueEntitlement> entitlementsToDelete = params.list('subscription.deleteEntitlementIds').collect { genericOIDService.resolveOID(it) }
                deleteEntitlements(entitlementsToDelete, targetObject, flash)
                isTargetSubChanged = true
            }
            if (params.subscription?.takeEntitlementIds && isBothObjectsSet(sourceObject, targetObject)) {
                List<IssueEntitlement> entitlementsToTake = params.list('subscription.takeEntitlementIds').collect { genericOIDService.resolveOID(it) }
                copyEntitlements(entitlementsToTake, targetObject, flash)
                isTargetSubChanged = true
            }

            if (isTargetSubChanged) {
                targetObject = targetObject.refresh()
            }
        }
        result.targetObject = targetObject
        result.sourceObject = sourceObject
        result
    }

    boolean deleteTasks(List<Long> toDeleteTasks, Object targetObject, def flash) {
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

    boolean copyTasks(Object sourceObject, def toCopyTasks, Object targetObject, def flash) {
        toCopyTasks.each { tsk ->
            def task

            if (sourceObject instanceof Subscription) {
                task = Task.findBySubscriptionAndId(sourceObject, tsk)
            } else if (sourceObject instanceof License) {
                task = Task.findByLicenseAndId(sourceObject, tsk)
            }
            if (task) {
                if (task.status != RDStore.TASK_STATUS_DONE) {
                    Task newTask = new Task()
                    InvokerHelper.setProperties(newTask, task.properties)
                    newTask.systemCreateDate = new Date()
                    newTask."${targetObject.getClass().getSimpleName().toLowerCase()}" = targetObject
                    save(newTask, flash)
                }
            }
        }
    }

    boolean copyAnnouncements(Object sourceObject, def toCopyAnnouncements, Object targetObject, def flash) {
        sourceObject.documents?.each { dctx ->
            if (dctx.id in toCopyAnnouncements) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                    Doc newDoc = new Doc()
                    InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                    save(newDoc, flash)
                    DocContext newDocContext = new DocContext()
                    InvokerHelper.setProperties(newDocContext, dctx.properties)
                    newDocContext."${targetObject.getClass().getSimpleName().toLowerCase()}" = targetObject
                    newDocContext.owner = newDoc
                    save(newDocContext, flash)
                }
            }
        }
    }

    def deleteAnnouncements(List<Long> toDeleteAnnouncements, Object targetObject, def flash) {
        targetObject.documents.each {
            if (toDeleteAnnouncements.contains(it.id) && it.owner?.contentType == Doc.CONTENT_TYPE_STRING && !(it.domain)) {
                Map params = [deleteId: it.id]
                log.debug("deleteDocuments ${params}");
                docstoreService.unifiedDeleteDocuments(params)
            }
        }
    }

    void copyIdentifiers(Object sourceObject, List<String> toCopyIdentifiers, Object targetObject, def flash) {
        toCopyIdentifiers.each { identifierId ->
            def owner = targetObject
            Identifier sourceIdentifier = Identifier.get(identifierId)
            IdentifierNamespace namespace = sourceIdentifier.ns
            String value = sourceIdentifier.value

            if (owner && namespace && value) {
                FactoryResult factoryResult = Identifier.constructWithFactoryResult([value: value, reference: owner, namespace: namespace])

                factoryResult.setFlashScopeByStatus(flash)
            }
        }
    }

    void deleteIdentifiers(List<String> toDeleteIdentifiers, Object targetObject, def flash) {
        int countDeleted = Identifier.executeUpdate('delete from Identifier i where i.id in (:toDeleteIdentifiers) and i.sub = :sub',
                [toDeleteIdentifiers: toDeleteIdentifiers, sub: targetObject])
        Object[] args = [countDeleted]
        flash.message += messageSource.getMessage('identifier.delete.success', args, locale)
    }

    def deleteDocs(List<Long> toDeleteDocs, Object targetObject, def flash) {
        log.debug("toDeleteDocCtxIds: " + toDeleteDocs)
        def updated = DocContext.executeUpdate("UPDATE DocContext set status = :del where id in (:ids)",
                [del: RDStore.DOC_CTX_STATUS_DELETED, ids: toDeleteDocs])
        log.debug("Number of deleted (per Flag) DocCtxs: " + updated)
    }

    boolean copyDocs(Object sourceObject, def toCopyDocs, Object targetObject, def flash) {
        sourceObject.documents?.each { dctx ->
            if (dctx.id in toCopyDocs) {
                if (((dctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (dctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (dctx.status?.value != 'Deleted')) {
                    try {

                        Doc newDoc = new Doc()
                        InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                        save(newDoc, flash)

                        DocContext newDocContext = new DocContext()
                        InvokerHelper.setProperties(newDocContext, dctx.properties)
                        newDocContext."${targetObject.getClass().getSimpleName().toLowerCase()}" = targetObject
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

    boolean copyProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Object targetObject, boolean isRenewSub, def flash, List auditProperties) {
        String classString = targetObject.getClass().toString()
        String ownerClassName = classString.substring(classString.lastIndexOf(".") + 1)
        ownerClassName = "com.k_int.kbplus.${ownerClassName}Property"
        def targetProp
        properties.each { AbstractPropertyWithCalculatedLastUpdated sourceProp ->
            targetProp = targetObject.propertySet.find { it.typeId == sourceProp.typeId && it.tenant == sourceProp.tenant }
            boolean isAddNewProp = sourceProp.type?.multipleOccurrence
            if ((!targetProp) || isAddNewProp) {
                targetProp = (new GroovyClassLoader()).loadClass(ownerClassName).newInstance(type: sourceProp.type, owner: targetObject, tenant: sourceProp.tenant)
                targetProp = sourceProp.copyInto(targetProp)
                targetProp.isPublic = sourceProp.isPublic
                //provisoric, should be moved into copyInto once migration is complete
                save(targetProp, flash)
                if (((sourceProp.id.toString() in auditProperties)) && targetProp.isPublic) {
                    //copy audit
                    if (!AuditConfig.getConfig(targetProp, AuditConfig.COMPLETE_OBJECT)) {

                        targetObject.getClass().findAllByInstanceOf(targetObject).each { Object member ->

                            def existingProp = targetProp.getClass().findByOwnerAndInstanceOf(member, targetProp)
                            if (!existingProp) {

                                // multi occurrence props; add one additional with backref
                                if (sourceProp.type.multipleOccurrence) {
                                    def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, targetProp.type, contextService.getOrg())
                                    additionalProp = targetProp.copyInto(additionalProp)
                                    additionalProp.instanceOf = targetProp
                                    additionalProp.save(flush: true)
                                } else {
                                    def matchingProps = targetProp.getClass().findAllByOwnerAndType(member, targetProp.type)
                                    // unbound prop found with matching type, set backref
                                    if (matchingProps) {
                                        matchingProps.each { memberProp ->
                                            memberProp.instanceOf = targetProp
                                            memberProp.save(flush: true)
                                        }
                                    } else {
                                        // no match found, creating new prop with backref
                                        def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, targetProp.type, contextService.getOrg())
                                        newProp = targetProp.copyInto(newProp)
                                        newProp.instanceOf = targetProp
                                        newProp.save(flush: true)
                                    }
                                }
                            }
                        }

                        def auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(targetProp.class.name, sourceProp.id)
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

    boolean deleteProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Object targetObject, boolean isRenewSub, def flash, List auditProperties) {
        properties.each { AbstractPropertyWithCalculatedLastUpdated prop ->
            if (AuditConfig.getConfig(prop, AuditConfig.COMPLETE_OBJECT)) {

                AuditConfig.removeAllConfigs(prop)

                prop.getClass().findAllByInstanceOf(prop).each { prop2 ->
                    prop2.delete(flush: true) //see ERMS-2049. Here, it is unavoidable because it affects the loading of orphaned properties - Hibernate tries to set up a list and encounters implicitely a SessionMismatch
                }
            }
            prop.delete(flush: true)
        }
    }


    boolean copyObjectProperty(Object sourceObject, Object targetObject, def flash, String propertyName) {

        if (sourceObject.getClass() == targetObject.getClass()) {
            if (sourceObject.hasProperty(propertyName)) {
                targetObject[propertyName] = sourceObject."$propertyName"
                return save(targetObject, flash)
            }
        }

    }

    boolean deleteObjectProperty(Object targetObject, def flash, String propertyName) {

        if (targetObject.hasProperty(propertyName)) {
            if (targetObject[propertyName] instanceof Boolean) {
                targetObject[propertyName] = false
            } else {
                targetObject[propertyName] = null
            }
            return save(targetObject, flash)
        }

    }

    boolean toggleAuditObjectProperty(Object sourceObject, Object targetObject, def flash, String propertyName) {

        if (sourceObject.getClass() == targetObject.getClass()) {
            if (sourceObject.hasProperty(propertyName) && !AuditConfig.getConfig(targetObject, propertyName)) {
                AuditConfig.addConfig(targetObject, propertyName)
            }
        }

    }

    boolean removeToggleAuditObjectProperty(Object targetObject, def flash, String propertyName) {
        if (targetObject.hasProperty(propertyName) && AuditConfig.getConfig(targetObject, propertyName)) {
            AuditConfig.removeConfig(targetObject, propertyName)
        }
    }

    boolean deleteLicenses(List<License> toDeleteLicenses, Object targetObject, def flash) {
        toDeleteLicenses.each { License lic ->
            subscriptionService.setOrgLicRole(targetObject, lic, true)
        }
    }

    boolean copyLicenses(List<License> toCopyLicenses, Object targetObject, def flash) {
        toCopyLicenses.each { License lic ->
            subscriptionService.setOrgLicRole(targetObject, lic, false)
        }
    }

    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Object targetObject, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetObject, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
    }

    boolean deleteSpecificSubscriptionEditors(List<PersonRole> toDeletePersonRoles, Object targetObject, def flash) {
        PersonRole.executeUpdate(
                "delete from PersonRole pr where pr in (:personRoles) and pr.sub = :sub",
                [personRoles: toDeletePersonRoles, sub: targetObject]
        )
    }

    boolean copyOrgRelations(List<OrgRole> toCopyOrgRelations, Object sourceObject, Object targetObject, def flash) {
        sourceObject.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg().id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE, RDStore.OR_LICENSING_CONSORTIUM])) {
                if (targetObject.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                    Object[] args = [or?.roleType?.getI10n("value") + " " + or?.org?.name]
                    flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
                } else {
                    def newProperties = or.properties

                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, newProperties)
                    //Vererbung ausschalten
                    newOrgRole.sharedFrom = null
                    newOrgRole.isShared = false
                    if (sourceObject instanceof Subscription) {
                        newOrgRole.sub = targetObject
                    }
                    if (sourceObject instanceof License) {
                        newOrgRole.lic = targetObject
                    }
                    save(newOrgRole, flash)
                }
            }
        }
    }

    boolean copySpecificSubscriptionEditors(List<PersonRole> toCopyPersonRoles, Object sourceObject, Object targetObject, def flash) {

        toCopyPersonRoles.each { prRole ->
            if (!(prRole.org in targetObject.orgRelations.org) && (prRole.org in sourceObject.orgRelations.org)) {
                OrgRole or = OrgRole.findByOrgAndSub(prRole.org, sourceObject)
                def newProperties = or.properties

                OrgRole newOrgRole = new OrgRole()
                InvokerHelper.setProperties(newOrgRole, newProperties)
                //Vererbung ausschalten
                newOrgRole.sharedFrom = null
                newOrgRole.isShared = false
                newOrgRole.sub = targetObject
            }

            if ((prRole.org in targetObject.orgRelations.org) && !PersonRole.findWhere(prs: prRole.prs, org: prRole.org, responsibilityType: prRole.responsibilityType, sub: targetObject)) {
                PersonRole newPrsRole = new PersonRole(prs: prRole.prs, org: prRole.org, sub: targetObject, responsibilityType: prRole.responsibilityType)
                save(newPrsRole, flash)
            }
        }

    }

    boolean deletePackages(List<SubscriptionPackage> packagesToDelete, Object targetObject, def flash) {
        //alle IEs löschen, die zu den zu löschenden Packages gehören
//        targetObject.issueEntitlements.each{ ie ->
        subscriptionService.getIssueEntitlements(targetObject).each { ie ->
            if (packagesToDelete.find { subPkg -> subPkg?.pkg?.id == ie?.tipp?.pkg?.id }) {
                ie.status = RDStore.TIPP_STATUS_DELETED
                save(ie, flash)
            }
        }

        //alle zugeordneten Packages löschen
        if (packagesToDelete) {

            packagesToDelete.each { subPkg ->
                OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg=?", [subPkg])
                PendingChangeConfiguration.executeUpdate("delete from PendingChangeConfiguration pcc where pcc.subscriptionPackage=:sp", [sp: subPkg])

                CostItem.findAllBySubPkg(subPkg).each { costItem ->
                    costItem.subPkg = null
                    if (!costItem.sub) {
                        costItem.sub = subPkg.subscription
                    }
                    costItem.save(flush: true)
                }
            }

            SubscriptionPackage.executeUpdate(
                    "delete from SubscriptionPackage sp where sp in (:packagesToDelete) and sp.subscription = :sub ",
                    [packagesToDelete: packagesToDelete, sub: targetObject])
        }
    }

    boolean copyPackages(List<SubscriptionPackage> packagesToTake, Object targetObject, def flash) {
        packagesToTake.each { subscriptionPackage ->
            if (targetObject.packages?.find { it.pkg?.id == subscriptionPackage.pkg?.id }) {
                Object[] args = [subscriptionPackage.pkg.name]
                flash.error += messageSource.getMessage('subscription.err.packageAlreadyExistsInTargetSub', args, locale)
            } else {

                List<OrgAccessPointLink> pkgOapls = OrgAccessPointLink.findAllByIdInList(subscriptionPackage.oapls.id)
                subscriptionPackage.properties.oapls = null
                subscriptionPackage.properties.pendingChangeConfig = null //copied in next step
                SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                InvokerHelper.setProperties(newSubscriptionPackage, subscriptionPackage.properties)
                newSubscriptionPackage.subscription = targetObject

                if (save(newSubscriptionPackage, flash)) {
                    pkgOapls.each { oapl ->

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
            Map<String, Object> configSettings = [subscriptionPackage: target, settingValue: config.settingValue, settingKey: config.settingKey, withNotification: config.withNotification]
            PendingChangeConfiguration newPcc = PendingChangeConfiguration.construct(configSettings)
            if (newPcc) {
                if (AuditConfig.getConfig(config.subscriptionPackage.subscription, config.settingKey) && !AuditConfig.getConfig(target.subscription, config.settingKey))
                    AuditConfig.addConfig(target.subscription, config.settingKey)
                else if (!AuditConfig.getConfig(config.subscriptionPackage.subscription, config.settingKey) && AuditConfig.getConfig(target.subscription, config.settingKey))
                    AuditConfig.removeConfig(target.subscription, config.settingKey)
            }
        }
    }

    boolean deleteEntitlements(List<IssueEntitlement> entitlementsToDelete, Object targetObject, def flash) {
        entitlementsToDelete.each {
            it.status = RDStore.TIPP_STATUS_DELETED
            save(it, flash)
        }
//        IssueEntitlement.executeUpdate(
//                "delete from IssueEntitlement ie where ie in (:entitlementsToDelete) and ie.subscription = :sub ",
//                [entitlementsToDelete: entitlementsToDelete, sub: targetObject])
    }

    boolean copyEntitlements(List<IssueEntitlement> entitlementsToTake, Object targetObject, def flash) {
        entitlementsToTake.each { ieToTake ->
            if (ieToTake.status != RDStore.TIPP_STATUS_DELETED) {
                def list = subscriptionService.getIssueEntitlements(targetObject).findAll { it.tipp.id == ieToTake.tipp.id && it.status != RDStore.TIPP_STATUS_DELETED }
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
                    newIssueEntitlement.subscription = targetObject

                    if (save(newIssueEntitlement, flash)) {
                        ieToTake.properties.coverages.each { coverage ->

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

    private boolean save(obj, flash) {
        if (obj.save(flush: true)) {
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

    boolean isBothObjectsSet(Object sourceObject, Object targetObject) {
        def grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        def request = grailsWebRequest.getCurrentRequest()
        def flash = grailsWebRequest.attributes.getFlashScope(request)
        if (!sourceObject || !targetObject) {
            Object[] args = [messageSource.getMessage("${sourceObject.getClass().getSimpleName().toLowerCase()}.label", null, locale)]
            if (!sourceObject) flash.error += messageSource.getMessage('copyElementsIntoObject.noSourceObject', args, locale) + '<br />'
            if (!targetObject) flash.error += messageSource.getMessage('copyElementsIntoObject.noTargetObject', args, locale) + '<br />'
            return false
        }
        return true
    }

    Map regroupObjectProperties(List<Object> objectsToCompare, Org org) {
        LinkedHashMap result = [groupedProperties:[:],orphanedProperties:[:],privateProperties:[:]]
        objectsToCompare.each{ object ->
            Map allPropDefGroups = object._getCalculatedPropDefGroups(org)
            allPropDefGroups.entrySet().each { propDefGroupWrapper ->
                //group group level
                //There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
                String wrapperKey = propDefGroupWrapper.getKey()
                if(wrapperKey.equals("orphanedProperties")) {
                    TreeMap orphanedProperties = result.orphanedProperties
                    orphanedProperties = comparisonService.buildComparisonTree(orphanedProperties, object, propDefGroupWrapper.getValue())
                    result.orphanedProperties = orphanedProperties
                }
                else {
                    LinkedHashMap groupedProperties = result.groupedProperties
                    //group level
                    //Each group may have different property groups
                    propDefGroupWrapper.getValue().each { propDefGroup ->
                        PropertyDefinitionGroup groupKey
                        PropertyDefinitionGroupBinding groupBinding
                        switch(wrapperKey) {
                            case "global":
                                groupKey = (PropertyDefinitionGroup) propDefGroup
                                if(groupKey.isVisible)
                                    groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTrees(groupedProperties, groupKey,null, object))
                                break
                            case "local":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if(groupBinding.isVisible) {
                                        groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTrees(groupedProperties, groupKey, groupBinding, object))
                                    }
                                }
                                catch (ClassCastException e) {
                                    log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                                    e.printStackTrace()
                                }
                                break
                            case "member":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if(groupBinding.isVisible && groupBinding.isVisibleForConsortiaMembers) {
                                        groupedProperties.put(groupKey, comparisonService.getGroupedPropertyTrees(groupedProperties, groupKey, groupBinding, object))
                                    }
                                }
                                catch (ClassCastException e) {
                                    log.error("Erroneous values in calculated property definition group! Stack trace as follows:")
                                    e.printStackTrace()
                                }
                                break
                        }
                    }
                    result.groupedProperties = groupedProperties
                }
            }
            TreeMap privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties, object, object.propertySet.findAll { it.type.tenant?.id == org.id })
            result.privateProperties = privateProperties
        }
        result
    }
}


