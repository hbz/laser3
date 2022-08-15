package de.laser


import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.exceptions.CreationException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.helper.FactoryResult
import de.laser.storage.RDStore
import de.laser.interfaces.ShareSupport
import de.laser.oap.OrgAccessPointLink
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyInfo
import de.laser.survey.SurveyOrg
import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import grails.web.mvc.FlashScope
import groovy.sql.Sql
import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource

import javax.servlet.http.HttpServletRequest
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ExecutorService

/**
 * This service holds the complex element copy functionality methods which enable to copy
 * elements between all kinds of objects
 */
@Transactional
class CopyElementsService {

    ExecutorService executorService
    GenericOIDService genericOIDService
    ComparisonService comparisonService
    TaskService taskService
    SubscriptionService subscriptionService
    ContextService contextService
    MessageSource messageSource
    DocstoreService docstoreService
    FormService formService
    LicenseService licenseService
    CompareService compareService
    PackageService packageService

    static final String WORKFLOW_DATES_OWNER_RELATIONS = '1'
    static final String WORKFLOW_PACKAGES_ENTITLEMENTS = '5'
    static final String WORKFLOW_DOCS_ANNOUNCEMENT_TASKS = '2'
    static final String WORKFLOW_SUBSCRIBER = '3'
    static final String WORKFLOW_PROPERTIES = '4'
    static final String WORKFLOW_END = '6'

    /**
     * Gets a list of base attributes for the given object type
     * @param obj the object type which should be copied
     * @return a list of base attributes for the given object type
     */
    List<String> allowedProperties(Object obj) {
        List<String> result = []
        switch (obj.class.simpleName) {
            case License.class.simpleName:
                result = ['startDate', 'endDate', 'status', 'licenseCategory', 'openEnded', 'isPublicForApi']
                break
            case Subscription.class.simpleName:
                result = ['startDate', 'endDate', 'manualCancellationDate', 'status', 'kind', 'form', 'resource', 'isPublicForApi', 'hasPerpetualAccess', 'hasPublishComponent']
                break
            case SurveyInfo.class.simpleName:
                result = ['startDate', 'endDate', 'comment']
                break
            case SurveyConfig.class.simpleName:
                if(obj.subSurveyUseForTransfer) {
                    result = ['scheduledStartDate', 'scheduledEndDate', 'comment', 'internalComment', 'url', 'urlComment', 'url2', 'urlComment2', 'url3', 'urlComment3']
                }else {
                    result = ['comment', 'internalComment', 'url', 'urlComment', 'url2', 'urlComment2', 'url3', 'urlComment3']
                }
                break
        }
        result
    }

    /**
     * Loads the data for the subscription dates, license, organisational relations and links for the given subscriptions
     * @param params the request parameter map
     * @return the related objects each for both source and target objects
     */
    Map loadDataFor_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null

        if(sourceObject.hasProperty('ids')) {
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
        }

        Org contextOrg = contextService.getOrg()
        if (sourceObject instanceof Subscription) {
            result.sourceLicenses = License.executeQuery("select li.sourceLicense from Links li where li.destinationSubscription = :sub and li.linkType = :linkType order by li.sourceLicense.sortableReference asc", [sub: sourceObject, linkType: RDStore.LINKTYPE_LICENSE])
            if (targetObject) {
                result.targetLicenses = License.executeQuery("select li.sourceLicense from Links li where li.destinationSubscription = :sub and li.linkType = :linkType order by li.sourceLicense.sortableReference asc", [sub: targetObject, linkType: RDStore.LINKTYPE_LICENSE])
            }

            // restrict visible for templates/links/orgLinksAsList
            result.source_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(sourceObject)
            result.target_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(targetObject)

            Set<RefdataValue> excludes = [RDStore.LINKTYPE_LICENSE]
            if(params.isRenewSub)
                excludes << RDStore.LINKTYPE_FOLLOWS
            result.sourceLinks = Links.executeQuery("select li from Links li where :sub in (li.sourceSubscription,li.destinationSubscription) and li.linkType not in (:linkTypes) and owner = :context", [sub: sourceObject, linkTypes: excludes, context: contextOrg])
            if(targetObject) {
                result.targetLinks = Links.executeQuery("select li from Links li where :sub in (li.sourceSubscription,li.destinationSubscription) and li.linkType not in (:linkTypes) and owner = :context", [sub: targetObject, linkTypes: excludes, context: contextOrg])
            }
        }

        if (sourceObject instanceof License) {
            // restrict visible for templates/links/orgLinksAsList
            result.source_visibleOrgRelations = licenseService.getVisibleOrgRelations(sourceObject)
            result.target_visibleOrgRelations = licenseService.getVisibleOrgRelations(targetObject)

            Set<RefdataValue> excludes = [RDStore.LINKTYPE_LICENSE]
            result.sourceLinks = Links.executeQuery("select li from Links li where :lic in (li.sourceLicense,li.destinationLicense) and li.linkType not in (:linkTypes) and owner = :context", [lic: sourceObject, linkTypes: excludes, context: contextOrg])
            if(targetObject) {
                result.targetLinks = Links.executeQuery("select li from Links li where :lic in (li.sourceLicense,li.destinationLicense) and li.linkType not in (:linkTypes) and owner = :context", [lic: targetObject, linkTypes: excludes, context: contextOrg])
            }
        }

        result
    }

    /**
     * Loads the data for the subscription documents, notes and tasks for the given subscriptions
     * @param params the request parameter map
     * @return the related objects each for both source and target objects
     */
    Map loadDataFor_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result.sourceDocuments = sourceObject.documents.sort { it.owner?.title?.toLowerCase()}
        result.targetDocuments = targetObject?.documents?.sort { it.owner?.title?.toLowerCase()} //null check needed because targetObject may not necessarily exist at that point and GORM does not always initialises sets
        result.sourceTasks = taskService.getTasksByObject(result.sourceObject)
        result.targetTasks = taskService.getTasksByObject(result.targetObject)
        result
    }

    /**
     * Loads the subscribers to copy or delete
     * @param params the request parameter map
     * @return the subscribers each for both source and target objects
     */
    Map loadDataFor_Subscriber(Map params) {
        Map<String, Object> result = [:]
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if(result.sourceObject instanceof Subscription) {
            result.validSourceSubChilds = subscriptionService.getValidSubChilds(result.sourceObject)
            if (params.targetObjectId) {
                result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
                result.validTargetSubChilds = subscriptionService.getValidSubChilds(result.targetObject)
            }
        }
        result
    }

    /**
     * Loads the properties to copy or delete
     * @param params the request parameter map
     * @return the public and private properties each for both source and target objects
     */
    Map loadDataFor_Properties(Map params) {
        LinkedHashMap result = [customProperties: [:], privateProperties: [:]]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        List<Object> objectsToCompare = [sourceObject]
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
            objectsToCompare.add(targetObject)
        }

        if(sourceObject instanceof Subscription || sourceObject instanceof License) {
            result = regroupObjectProperties(objectsToCompare)
        }

        if(sourceObject instanceof SurveyConfig) {
            Org contextOrg = contextService.getOrg()
            objectsToCompare.each { Object obj ->
                Map customProperties = result.customProperties
                customProperties = comparisonService.buildComparisonTreePropertyDefintion(customProperties, obj, obj.surveyProperties.surveyProperty.findAll { it.tenant == null }.sort { it.getI10n('name') })
                result.customProperties = customProperties
                Map privateProperties = result.privateProperties
                privateProperties = comparisonService.buildComparisonTreePropertyDefintion(privateProperties, obj, obj.surveyProperties.surveyProperty.findAll { it.tenant?.id == contextOrg.id }.sort { it.getI10n('name') })
                result.privateProperties = privateProperties
            }
        }

        if (targetObject) {
            result.targetObject = targetObject.refresh()
        }
        result
    }

    /**
     * Loads the subscription holdings to copy or delete
     * @param params the request parameter map
     * @return the subscription holdings each for both source and target objects
     */
    Map loadDataFor_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null
        //result.sourceIEs = subscriptionService.getIssueEntitlements(sourceObject)
        //result.targetIEs = subscriptionService.getIssueEntitlements(targetObject)
        result.targetObject = targetObject
        result.sourceObject = sourceObject
        result
    }

    /**
     * Copies the subscribers from the source into the target subscription
     * @param subscriptionToTake the subscribers to take
     * @param targetObject the target object into which the subscribers should be copied
     * @param flash the message container
     */
    void copySubscriber(List<Subscription> subscriptionToTake, Object targetObject, def flash) {
        Locale locale = LocaleUtils.getCurrentLocale()
        targetObject.refresh()
        List<Subscription> targetChildSubs = subscriptionService.getValidSubChilds(targetObject), memberHoldingsToTransfer = []
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
//                ArrayList<Links> prevLinks = Links.findAllByDestinationSubscriptionAndLinkType(subMember.id, RDStore.LINKTYPE_FOLLOWS)
//                if (prevLinks.size() == 0) {

                /* Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
                        [result.subscription, it.id])*/

                Subscription newSubscription = new Subscription(
                        isMultiYear: subMember.isMultiYear,
                        type: subMember.type,
                        kind: subMember.kind,
                        status: targetObject.status,
                        name: targetObject.name,
                        startDate: subMember.isMultiYear ? subMember.startDate : targetObject.startDate,
                        endDate: subMember.isMultiYear ? subMember.endDate : targetObject.endDate,
                        manualRenewalDate: subMember.manualRenewalDate,
                        /* manualCancellationDate: result.subscription.manualCancellationDate, */
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
                        Links.construct([source: newSubscription, destination: subMember, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.getOrg()])

                        if(Links.findAllByDestinationSubscriptionAndLinkType(targetObject, RDStore.LINKTYPE_LICENSE).size() > 0) {
                            Set<Links> precedingLicenses = Links.findAllByDestinationSubscriptionAndLinkType(subMember, RDStore.LINKTYPE_LICENSE)
                            precedingLicenses.each { Links link ->
                                Map<String, Object> successorLink = [source: link.sourceLicense, destination: newSubscription, linkType: RDStore.LINKTYPE_LICENSE, owner: contextService.getOrg()]
                                Links.construct(successorLink)
                            }
                        }
                    }
                    catch (CreationException e) {
                        log.error("Subscription linking failed, please check: ${e.stackTrace}")
                    }
                }

                //only the bare properties should be transferred
                if (subMember.propertySet) {
                    Org org = contextService.getOrg()
                    //customProperties of ContextOrg && privateProperties of ContextOrg
                    subMember.propertySet.each {subProp ->
                        if(((subProp.type.tenant == null && (subProp.tenant?.id == org.id || subProp.tenant == null)) || subProp.type.tenant?.id == org.id) && !(subProp.hasProperty('instanceOf') && subProp.instanceOf && AuditConfig.getConfig(subProp.instanceOf)))
                        {
                            SubscriptionProperty copiedProp = new SubscriptionProperty(type: subProp.type, owner: newSubscription, isPublic: subProp.isPublic, tenant: subProp.tenant)
                            copiedProp = subProp.copyInto(copiedProp)
                            copiedProp.save()
                        }
                    }
                    /*
                    for (prop in subMember.propertySet) {
                        SubscriptionProperty copiedProp = new SubscriptionProperty(type: prop.type, owner: newSubscription, isPublic: prop.isPublic, tenant: prop.tenant)
                        copiedProp = prop.copyInto(copiedProp)
                        copiedProp.save()
                        //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                    }*/
                }

                if (subMember.ids) {
                    subMember.ids.each { Identifier id ->
                        Identifier.constructWithFactoryResult([value: id.value, parent: id.instanceOf, reference: newSubscription, namespace: id.ns])
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

                if (subMember.packages && targetObject.packages) {
                    //Package
                    subMember.packages?.each { pkg ->
                        def pkgOapls = pkg.oapls
                        pkg.properties.oapls = null
                        pkg.properties.pendingChangeConfig = null
                        SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                        //InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                        newSubscriptionPackage.subscription = newSubscription
                        newSubscriptionPackage.pkg = pkg.pkg
                        if (newSubscriptionPackage.save()) {
                            pkgOapls.each { oapl ->

                                //oapl.globalUID = null
                                OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                                //InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                                newOrgAccessPointLink.platform = oapl.platform
                                newOrgAccessPointLink.oap = oapl.oap
                                newOrgAccessPointLink.active = oapl.active
                                newOrgAccessPointLink.subPkg = newSubscriptionPackage
                                newOrgAccessPointLink.save()
                            }
                        }
                    }
                }
                if (IssueEntitlement.executeQuery('select count(ie.id) from IssueEntitlement ie where ie.subscription = :member', [member: subMember])[0] > 0 && IssueEntitlement.executeQuery('select count(ie.id) from IssueEntitlement ie where ie.subscription = :target', [target: targetObject])[0] > 0) {
                    memberHoldingsToTransfer << newSubscription
                    //Sql sql = GlobalService.obtainSqlConnection()
                    //List sourceHolding = sql.rows("select * from title_instance_package_platform join issue_entitlement on tipp_id = ie_tipp_fk where ie_subscription_fk = :source and ie_status_rv_fk = :current", [source: subMember.id, current: RDStore.TIPP_STATUS_CURRENT.id])
                    //packageService.bulkAddHolding(sql, newSubscription.id, sourceHolding, subMember.hasPerpetualAccess)
                    /*subMember.issueEntitlements?.each { ie ->
                        if (ie.status != RDStore.TIPP_STATUS_REMOVED) {
                            def ieProperties = ie.properties
                            ieProperties.globalUID = null

                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                            InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                            newIssueEntitlement.coverages = null
                            newIssueEntitlement.priceItems = null
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

                                ie.properties.priceItems.each { priceItem ->
                                    def priceItemProperties = priceItem.properties
                                    PriceItem newPriceItem = new PriceItem()
                                    InvokerHelper.setProperties(newPriceItem, priceItemProperties)
                                    newPriceItem.issueEntitlement = newIssueEntitlement
                                    newPriceItem.save()
                                }
                            }
                        }
                    }*/
                }

                //OrgRole
                subMember.orgRelations?.each { or ->
                    if ((or.org.id == contextService.getOrg().id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]) || (targetObject.orgRelations.size() >= 1)) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = newSubscription
                        newOrgRole.save()
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
        if(memberHoldingsToTransfer) {
            targetObject.packages.each { SubscriptionPackage targetPkg ->
                subscriptionService.addToMemberSubscription(targetObject, memberHoldingsToTransfer, targetPkg.pkg, true)
            }
        }
    }

    /**
     * Copies the participants from the source into the target survey
     * @param orgToTake the participants to take
     * @param targetObject the target object into which the participants should be copied
     * @param flash the message container
     */
    void copySurveyParticipants(List<Org> orgToTake, Object targetObject, def flash) {
        targetObject.refresh()

        orgToTake.each { Org org ->

            if(!SurveyOrg.findBySurveyConfigAndOrg(targetObject, org))
            {
                new SurveyOrg(surveyConfig: targetObject, org: org).save()
            }
        }
    }

    /**
     * Deletes the given participants from the target object
     * @param orgToTake the participants to remove
     * @param targetObject the target object from which the participants should be removed
     * @param flash the message container
     */
    void deleteSurveyParticipants(List<Org> orgToDelete, Object targetObject, def flash) {
        targetObject.refresh()

        orgToDelete.each { Org org ->
            SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(targetObject, org)
            if(surveyOrg)
            {
                surveyOrg.delete()
            }
        }
    }

    /**
     * Processes the transfer of given base properties
     * @param params the request parameters
     * @return the source and target objects for the next copy step
     */
    Map copyObjectElements_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        FlashScope flash = getCurrentFlashScope()

        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null

        if (formService.validateToken(params)) {

            List<String> allowedProperties = allowedProperties(sourceObject)
            List takeProperties = params.list('copyObject.take')
            List takeAudit = params.list('copyObject.toggleAudit')

            takeProperties.each { takeProperty ->
                if (takeProperty in allowedProperties) {
                    copyObjectProperty(sourceObject, targetObject, flash, takeProperty)
                }
            }

            allowedProperties.each { String allowedProperty ->
                if (allowedProperty in takeProperties || params.isRenewSub) {
                    if (allowedProperty in takeAudit) {
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

                //targetObject = targetObject.refresh()
                targetObject.orgRelations.each { newSubOrgRole ->

                    if (newSubOrgRole.org in toggleShareOrgRoles.org) {
                        newSubOrgRole.isShared = true
                        newSubOrgRole.save()
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
                List<Identifier> toDeleteIdentifiers = params.list('copyObject.deleteIdentifierIds').collect { genericOIDService.resolveOID(it) }
                deleteIdentifiers(toDeleteIdentifiers, targetObject, flash)
                //isTargetSubChanged = true
            }

            if (params.list('copyObject.takeIdentifierIds') && isBothObjectsSet(sourceObject, targetObject)) {
                List<Identifier> toCopyIdentifiers = params.list('copyObject.takeIdentifierIds').collect { genericOIDService.resolveOID(it) }
                copyIdentifiers(sourceObject, toCopyIdentifiers, targetObject, takeAudit, flash)
                //isTargetSubChanged = true
            }

            if (params.list('copyObject.deleteLinks') && isBothObjectsSet(sourceObject, targetObject)) {
                List<Links> toDeleteLinks = params.list('copyObject.deleteLinks').collect { genericOIDService.resolveOID(it) }
                deleteLinks(toDeleteLinks, flash)
                //isTargetSubChanged = true
            }

            if (params.list('copyObject.takeLinks') && isBothObjectsSet(sourceObject, targetObject)) {
                List<Links> toCopyLinks = params.list('copyObject.takeLinks').collect { genericOIDService.resolveOID(it) }
                copyLinks(sourceObject, toCopyLinks, targetObject, flash)
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

    /**
     * Processes the given documents / notes / tasks transfer
     * @param params the request parameters
     * @return the source and target objects for the next copy step
     */
    Map copyObjectElements_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        FlashScope flash = getCurrentFlashScope()

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
                def toCopyDocs = [], toShare = []
                params.list('copyObject.takeDocIds').each { doc -> toCopyDocs << Long.valueOf(doc) }
                params.list('copyObject.toggleShare').each { doc -> toShare << Long.valueOf(doc) }
                copyDocs(sourceObject, toCopyDocs, targetObject, flash, toShare)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.deleteAnnouncementIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toDeleteAnnouncements = []
                params.list('copyObject.deleteAnnouncementIds').each { announcement -> toDeleteAnnouncements << Long.valueOf(announcement) }
                deleteAnnouncements(toDeleteAnnouncements, targetObject, flash)
                isTargetSubChanged = true
            }

            if (params.list('copyObject.takeAnnouncementIds') && isBothObjectsSet(sourceObject, targetObject)) {
                def toCopyAnnouncements = [], toShare = []
                params.list('copyObject.takeAnnouncementIds').each { announcement -> toCopyAnnouncements << Long.valueOf(announcement) }
                params.list('copyObject.toggleShare').each { doc -> toShare << Long.valueOf(doc) }
                copyAnnouncements(sourceObject, toCopyAnnouncements, targetObject, flash, toShare)
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

            /*if (isTargetSubChanged) {
                targetObject = targetObject.refresh()
            }*/
        }

        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    @Deprecated
    Map copyObjectElements_Identifiers(Map params) {
        Map<String, Object> result = [:]
        FlashScope flash = getCurrentFlashScope()

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
            copyIdentifiers(sourceObject, toCopyIdentifiers, targetObject, [], flash) //this method is not used, no idea where to fetch audit information?
            isTargetSubChanged = true
        }

        /*if (isTargetSubChanged) {
            targetObject = targetObject.refresh()
        }*/

        result.flash = flash
        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    /**
     * Processes the transfer of the subscribers
     * @param params the request parameters
     * @return the source and target objects for the next copy step
     */
    Map copyObjectElements_Subscriber(Map params) {
        Map<String, Object> result = [:]
        FlashScope flash = getCurrentFlashScope()

        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        if (formService.validateToken(params)) {

            if(sourceObject instanceof SurveyConfig) {
                if (params.copyObject?.deleteParticipants && isBothObjectsSet(sourceObject, targetObject)) {
                    List<Org> toDeleteOrgs = params.list('copyObject.deleteParticipants').collect { genericOIDService.resolveOID(it) }
                    deleteSurveyParticipants(toDeleteOrgs, targetObject, flash)
                }
            }


            if(sourceObject instanceof Subscription){
                if (params.copyObject?.copySubscriber && isBothObjectsSet(sourceObject, targetObject)) {
                        List<Subscription> toCopySubs = params.list('copyObject.copySubscriber').collect { genericOIDService.resolveOID(it) }
                        copySubscriber(toCopySubs, targetObject, flash)
                }
            }
            if(sourceObject instanceof SurveyConfig) {
                if (params.copyObject?.copyParticipants && isBothObjectsSet(sourceObject, targetObject)) {
                    List<Org> toCopyOrgs = params.list('copyObject.copyParticipants').collect { genericOIDService.resolveOID(it) }
                    copySurveyParticipants(toCopyOrgs, targetObject, flash)
                }
            }
        }

        result.sourceObject = sourceObject
        result.targetObject = targetObject
        result
    }

    /**
     * Processes the transfer of the properties
     * @param params the request parameters
     * @return the source and target objects for the next copy step
     */
    Map copyObjectElements_Properties(Map params) {
        LinkedHashMap result = [:]
        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        boolean isRenewSub = params.isRenewSub ? true : false

        FlashScope flash = getCurrentFlashScope()
        Object targetObject = null
        if (params.targetObjectId) {
            targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        List auditProperties = params.list('auditProperties')

        if(sourceObject instanceof SurveyConfig){
            List<PropertyDefinition> propertiesToDelete = params.list('copyObject.deleteProperty').collect { genericOIDService.resolveOID(it) }
            if (propertiesToDelete && isBothObjectsSet(sourceObject, targetObject)) {
                deleteSurveyProperties(propertiesToDelete, targetObject)
            }

            List<PropertyDefinition> propertiesToTake = params.list('copyObject.takeProperty').collect { genericOIDService.resolveOID(it) }
            if (propertiesToTake && isBothObjectsSet(sourceObject, targetObject)) {
                copySurveyProperties(propertiesToTake, sourceObject, targetObject)
            }
        }

        if(sourceObject instanceof Subscription || sourceObject instanceof License) {
            List<AbstractPropertyWithCalculatedLastUpdated> propertiesToDelete = params.list('copyObject.deleteProperty').collect { genericOIDService.resolveOID(it) }
            if (propertiesToDelete && isBothObjectsSet(sourceObject, targetObject)) {
                deleteProperties(propertiesToDelete)
            }

            List<AbstractPropertyWithCalculatedLastUpdated> propertiesToTake = params.list('copyObject.takeProperty').collect { genericOIDService.resolveOID(it) }
            if (propertiesToTake && isBothObjectsSet(sourceObject, targetObject)) {
                copyProperties(propertiesToTake, targetObject, isRenewSub, flash, auditProperties)
            }
        }

        if (targetObject) {
            result.targetObject = targetObject
        }
        result
    }

    /**
     * Processes the transfer of the subscription holding.
     * As this process may take much time, it is deployed onto a parallel thread
     * @param params the request parameters
     * @return the source and target objects for the next copy step
     */
    Map copyObjectElements_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        FlashScope flash = getCurrentFlashScope()

        Object sourceObject = genericOIDService.resolveOID(params.sourceObjectId)
        Object targetObject = params.targetObjectId ? genericOIDService.resolveOID(params.targetObjectId) : null

        if (formService.validateToken(params)) {
            boolean bulkOperationRunning = false
            Set<Thread> threadSet = Thread.getAllStackTraces().keySet()
            Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()])
            threadArray.each {
                if (it.name == 'PackageTransfer_'+targetObject?.id) {
                    bulkOperationRunning = true
                }
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
            }

            log.debug(params.toMapString())
            if(!bulkOperationRunning && isBothObjectsSet(sourceObject, targetObject, flash) && params.copyElementsSubmit) {
                flash.message = messageSource.getMessage('subscription.details.linkPackage.thread.running',null, LocaleUtils.getCurrentLocale())
                executorService.execute({
                    try {
                        Thread.currentThread().setName("PackageTransfer_${targetObject.id}")
                        if (params.subscription?.deletePackageIds) {
                            List<SubscriptionPackage> packagesToDelete = params.list('subscription.deletePackageIds').collect { genericOIDService.resolveOID(it) }
                            deletePackages(packagesToDelete, targetObject, flash)
                        }
                        if (params.subscription?.takePackageIds) {
                            List<SubscriptionPackage> packagesToTake = params.list('subscription.takePackageIds').collect { genericOIDService.resolveOID(it) }
                            copyPackages(packagesToTake, targetObject, flash)
                        }

                        if (params.subscription?.takePackageSettings) {
                            List takePackageNotifications = params.list('subscription.takePackageNotifications'),
                            takePackageSettingAudit = params.list('subscription.takePackageSettingAudit'),
                            takePackageNotificationAudit = params.list('subscription.takePackageNotificationAudit')
                            params.list('subscription.takePackageSettings').each { String val ->
                                String[] setting = val.split('§')
                                boolean withNotification = takePackageNotifications.findIndexOf { String notification -> notification.split('§')[1] == setting[1]} > -1,
                                settingAudit = takePackageSettingAudit.findIndexOf { String settingAudit -> settingAudit.split('§')[1] == setting[1]} > -1,
                                notificationAudit = takePackageNotificationAudit.findIndexOf { String notificationAudit -> notificationAudit.split('§')[1] == setting[1]} > -1
                                SubscriptionPackage sourcePackage = genericOIDService.resolveOID(setting[0])
                                SubscriptionPackage targetPackage = SubscriptionPackage.findBySubscriptionAndPkg(targetObject, sourcePackage.pkg)
                                if(setting[2] != 'null' && targetPackage) {
                                    Map<String, Object> configSettings = [subscriptionPackage: targetPackage, settingValue: RefdataValue.get(setting[2]), settingKey: setting[1], withNotification: withNotification]
                                    PendingChangeConfiguration newPcc = PendingChangeConfiguration.construct(configSettings)
                                    if (newPcc) {
                                        if (settingAudit && !AuditConfig.getConfig(targetObject, setting[1]))
                                            AuditConfig.addConfig(targetObject, setting[1])
                                        else if (!settingAudit && AuditConfig.getConfig(targetObject, setting[1]))
                                            AuditConfig.removeConfig(targetObject, setting[1])
                                        if (notificationAudit && !AuditConfig.getConfig(targetObject, setting[1]+PendingChangeConfiguration.NOTIFICATION_SUFFIX))
                                            AuditConfig.addConfig(targetObject, setting[1]+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                                        else if (!notificationAudit && AuditConfig.getConfig(targetObject, setting[1]+PendingChangeConfiguration.NOTIFICATION_SUFFIX))
                                            AuditConfig.removeConfig(targetObject, setting[1]+PendingChangeConfiguration.NOTIFICATION_SUFFIX)
                                    }
                                }
                            }
                        }

                        if (params.subscription?.takeTitleGroups) {
                            List<IssueEntitlementGroup> takeTitleGroups = params.list('subscription.takeTitleGroups').collect { genericOIDService.resolveOID(it) }
                            copyIssueEntitlementGroupItem(takeTitleGroups, targetObject)
                        }

                        if (params.subscription?.deleteTitleGroups) {
                            List<IssueEntitlementGroup> deleteTitleGroups = params.list('subscription.deleteTitleGroups').collect { genericOIDService.resolveOID(it) }
                            deleteIssueEntitlementGroupItem(deleteTitleGroups)
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace()
                    }
                })
            }
            else if(bulkOperationRunning) {
                flash.message = messageSource.getMessage('subscription.details.linkPackage.thread.running',null, LocaleUtils.getCurrentLocale())
            }
            /*if (params.subscription?.deleteEntitlementIds && isBothObjectsSet(sourceObject, targetObject)) {
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
                targetObject = targetObject
            }*/
        }
        result.targetObject = targetObject
        result.sourceObject = sourceObject
        result
    }

    /**
     * Deletes the given tasks from the target object
     * @param toDeleteTasks the tasks which should be deleted
     * @param targetObject the target object from which the tasks should be deleted
     * @param flash the message container
     * @return true if the deletion was successful, false otherwise
     */
    boolean deleteTasks(List<Long> toDeleteTasks, Object targetObject, def flash) {
        Locale locale = LocaleUtils.getCurrentLocale()
        boolean isInstAdm = contextService.getUser().hasAffiliation("INST_ADM")
        def userId = contextService.getUser().id
        toDeleteTasks.each { deleteTaskId ->
            Task dTask = Task.get(deleteTaskId)
            if (dTask) {
                if (dTask.creator.id == userId || isInstAdm) {
                    _delete(dTask, flash)
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

    /**
     * Copies the given task list into the target object
     * @param sourceObject the object from which the tasks should be taken
     * @param toCopyTasks the task IDs to be copied
     * @param targetObject the target object into which the tasks should be copied
     * @param flash the message container
     * @return true if the transfer was successful, false otherwise
     */
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
                    _save(newTask, flash)
                }
            }
        }
    }

    /**
     * Copies the given notes into the target object
     * @param sourceObject the object from which the tasks should be taken
     * @param toCopyAnnouncements the note IDs to be copied
     * @param targetObject the target object into which the tasks should be copied
     * @param flash the message container
     * @return true if the transfer was successful, false otherwise
     */
    boolean copyAnnouncements(Object sourceObject, def toCopyAnnouncements, Object targetObject, def flash, def toShare = []) {
        sourceObject.documents?.each { dctx ->
            if (dctx.id in toCopyAnnouncements) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                    Doc newDoc = new Doc()
                    InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                    _save(newDoc, flash)
                    DocContext newDocContext = new DocContext()
                    InvokerHelper.setProperties(newDocContext, dctx.properties)
                    if(dctx.id in toShare)
                        newDocContext.isShared = true
                    else newDocContext.isShared = false
                    newDocContext."${targetObject.getClass().getSimpleName().toLowerCase()}" = targetObject
                    newDocContext.owner = newDoc
                    _save(newDocContext, flash)
                }
            }
        }
    }

    /**
     * Deletes the given notes from the target object
     * @param toDeleteAnnouncements the notes which should be deleted
     * @param targetObject the target object from which the tasks should be deleted
     * @param flash unused
     */
    def deleteAnnouncements(List<Long> toDeleteAnnouncements, Object targetObject, def flash) {
        targetObject.documents.each {
            if (toDeleteAnnouncements.contains(it.id) && it.owner?.contentType == Doc.CONTENT_TYPE_STRING && !(it.domain)) {
                Map params = [deleteId: it.id]
                log.debug("deleteDocuments ${params}");
                docstoreService.unifiedDeleteDocuments(params)
            }
        }
    }

    /**
     * Copies the given identifiers into the target object
     * @param sourceObject unused
     * @param toCopyIdentifiers the identifiers to be copied
     * @param targetObject the target object into which the tasks should be copied
     * @param takeAudit which identifiers should be inherited?
     * @param flash unused
     */
    void copyIdentifiers(Object sourceObject, List<Identifier> toCopyIdentifiers, Object targetObject, List takeAudit, def flash) {
        toCopyIdentifiers.each { Identifier sourceIdentifier ->
            def owner = targetObject
            IdentifierNamespace namespace = sourceIdentifier.ns
            String value = sourceIdentifier.value

            if (owner && namespace && value) {
                FactoryResult factoryResult = Identifier.constructWithFactoryResult([value: value, reference: owner, namespace: namespace])
                if(genericOIDService.getOID(sourceIdentifier) in takeAudit) {
                    if(!AuditConfig.getConfig(factoryResult.result)) {
                        AuditConfig.addConfig(factoryResult.result, AuditConfig.COMPLETE_OBJECT)
                    }
                }

                //factoryResult.setFlashScopeByStatus(flash)
            }
        }
    }

    /**
     * Deletes the given identifiers from the target object
     * @param toDeleteIdentifiers the identifiers which should be deleted
     * @param targetObject the target object from which the tasks should be deleted
     * @param flash unused
     */
    void deleteIdentifiers(List<Identifier> toDeleteIdentifiers, Object targetObject, def flash) {
        String attr = Identifier.getAttributeName(targetObject)
        Identifier.executeUpdate('delete from Identifier i where i.instanceOf in (:toDeleteIdentifiers)')
        toDeleteIdentifiers.each { Identifier delId ->
            AuditConfig.removeConfig(delId)
        }
        int countDeleted = Identifier.executeUpdate('delete from Identifier i where i in (:toDeleteIdentifiers) and i.' + attr + ' = :reference',
                [toDeleteIdentifiers: toDeleteIdentifiers, reference: targetObject])
        Object[] args = [countDeleted]
    }

    /**
     * Copies the given list of subscription / license links into the target object
     * @param sourceObject the source object from which the links should be taken
     * @param toCopyLinks the list of links to transfer
     * @param targetObject the target object into which the links should be copied
     * @param flash unused
     */
    void copyLinks(Object sourceObject, List<Links> toCopyLinks, Object targetObject, def flash) {
        toCopyLinks.each { Links sourceLink ->
            Map<String, Object> configMap = [owner: sourceLink.owner, linkType: sourceLink.linkType]
            if(sourceObject == sourceLink.determineSource()) {
                configMap.source = targetObject
                configMap.destination = sourceLink.determineDestination()
            }
            if(sourceObject == sourceLink.determineDestination()) {
                configMap.source = sourceLink.determineSource()
                configMap.destination = targetObject
            }

            Links.construct(configMap)

            //factoryResult.setFlashScopeByStatus(flash)
        }
    }

    /**
     * Deletes the given links
     * @param toDeleteLinks the links to be deleted
     * @param flash unused
     */
    void deleteLinks(List<Links> toDeleteLinks, def flash) {
        int countDeleted = Identifier.executeUpdate('delete from Links li where li in (:toDeleteLinks)',
                [toDeleteLinks: toDeleteLinks])
        Object[] args = [countDeleted]
    }

    /**
     * Marks the given documents as deleted (from the given target)
     * @param toDeleteDocs the documents which should be marked as deleted
     * @param targetObject unused
     * @param flash unused
     * @return the count of entries marked as deleted
     */
    def deleteDocs(List<Long> toDeleteDocs, Object targetObject, def flash) {
        log.debug("toDeleteDocCtxIds: " + toDeleteDocs)
        int updated = DocContext.executeUpdate("UPDATE DocContext set status = :del where id in (:ids)",
                [del: RDStore.DOC_CTX_STATUS_DELETED, ids: toDeleteDocs])
        log.debug("Number of deleted (per Flag) DocCtxs: " + updated)
    }

    /**
     * Copies the given list of documents into the target object
     * @param sourceObject the source object from which the documents should be copied
     * @param toCopyDocs the list of document context IDs to copy
     * @param targetObject the target object into which the documents should be copied
     * @param flash the message container
     * @return true if the transfer was successful, false otherwise
     */
    boolean copyDocs(Object sourceObject, def toCopyDocs, Object targetObject, def flash, def toShare = []) {
        sourceObject.documents?.each { dctx ->
            if (dctx.id in toCopyDocs) {
                if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (dctx.status?.value != 'Deleted')) {
                    try {

                        Doc newDoc = new Doc()
                        InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                        _save(newDoc, flash)

                        DocContext newDocContext = new DocContext()
                        InvokerHelper.setProperties(newDocContext, dctx.properties)
                        if(dctx.id in toShare)
                            newDocContext.isShared = true
                        else newDocContext.isShared = false
                        newDocContext."${targetObject.getClass().getSimpleName().toLowerCase()}" = targetObject
                        newDocContext.owner = newDoc
                        _save(newDocContext, flash)

                        String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

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

    /**
     * Copies the given properties into the target object
     * @param properties the properties to be copied
     * @param targetObject the target object into which the properties should be copied
     * @param isRenewSub unused
     * @param flash the message container
     * @param auditProperties the properties which should be inhertied in the target object
     * @return true if the transfer was successful, false otherwise
     */
    boolean copyProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties, Object targetObject, boolean isRenewSub, def flash, List auditProperties) {
        String classString = targetObject.class.name
        String ownerClassName = classString.substring(classString.lastIndexOf(".") + 1)
        ownerClassName = "de.laser.properties.${ownerClassName}Property"
        def targetProp
        properties.each { AbstractPropertyWithCalculatedLastUpdated sourceProp ->
            targetProp = targetObject.propertySet.find { it.type.id == sourceProp.type.id && it.tenant == sourceProp.tenant }
            boolean isAddNewProp = sourceProp.type?.multipleOccurrence
            if ((!targetProp) || isAddNewProp) {
                targetProp = (new GroovyClassLoader()).loadClass(ownerClassName).newInstance(type: sourceProp.type, owner: targetObject, tenant: sourceProp.tenant)
                targetProp = sourceProp.copyInto(targetProp)
                targetProp.isPublic = sourceProp.isPublic
                //provisoric, should be moved into copyInto once migration is complete
                _save(targetProp, flash)
                if (sourceProp.id.toString() in auditProperties) {
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
                                    additionalProp.save()
                                } else {
                                    def matchingProps = targetProp.getClass().findAllByOwnerAndType(member, targetProp.type)
                                    // unbound prop found with matching type, set backref
                                    if (matchingProps) {
                                        matchingProps.each { memberProp ->
                                            memberProp.instanceOf = targetProp
                                            memberProp.save()
                                        }
                                    } else {
                                        // no match found, creating new prop with backref
                                        def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, targetProp.type, contextService.getOrg())
                                        newProp = targetProp.copyInto(newProp)
                                        newProp.instanceOf = targetProp
                                        newProp.save()
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
                //Replace
                targetProp = sourceProp.copyInto(targetProp)
                targetProp.save()
            }
        }
    }

    /**
     * Copies the given survey properties (i.e. survey questions) into the target survey
     * @param properties the properties to copy
     * @param sourceObject the source survey from which the questions should be taken
     * @param targetObject the target survey into which the questions should be copied
     * @return true if the transfer was successful, false otherwise
     */
    boolean copySurveyProperties(List<PropertyDefinition> properties, Object sourceObject, Object targetObject) {
        properties.each { PropertyDefinition prop ->
            SurveyConfigProperties sourceSurveyConfigProperty = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(sourceObject, prop)
            SurveyConfigProperties targetSurveyConfigProperty = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(targetObject, prop)
            if (sourceSurveyConfigProperty && !targetSurveyConfigProperty) {
                new SurveyConfigProperties(surveyConfig: targetObject, surveyProperty: prop).save()
            }
        }
    }

    /**
     * Deletes the given properties along with eventual inheritance settings
     * @param properties the properties to delete
     * @return true if deletion was succesful, false otherwise
     */
    boolean deleteProperties(List<AbstractPropertyWithCalculatedLastUpdated> properties) {
        properties.each { AbstractPropertyWithCalculatedLastUpdated prop ->
            def owner = prop.owner
            if (AuditConfig.getConfig(prop, AuditConfig.COMPLETE_OBJECT)) {
                AuditConfig.removeAllConfigs(prop)
                prop.getClass().findAllByInstanceOf(prop).each { prop2 ->
                    prop2.delete()
                }
            }
            owner.propertySet.remove(prop)
            prop.delete()
        }
    }

    /**
     * Deletes the given survey properties (i.e. survey questions) from the target survey
     * @param properties the properties to be deleted
     * @param targetObject the target survey into which the questions should be copied
     * @return true if the transfer was successful, false otherwise
     */
    boolean deleteSurveyProperties(List<PropertyDefinition> properties, Object targetObject) {
        properties.each { PropertyDefinition prop ->
            SurveyConfigProperties surveyConfigProperty = SurveyConfigProperties.findBySurveyConfigAndSurveyProperty(targetObject, prop)
            if (surveyConfigProperty) {
                surveyConfigProperty.delete()
            }
        }
    }

    /**
     * Transfers the given attribute from the source into the target object
     * @param sourceObject the source object from which the attribute should be taken
     * @param targetObject the target object into which the attribute should be copied
     * @param flash the message container
     * @param propertyName the attrbiute to be transferred
     * @return true if the transfer was successful, false otherwise
     */
    boolean copyObjectProperty(Object sourceObject, Object targetObject, def flash, String propertyName) {

        if (sourceObject.getClass() == targetObject.getClass()) {
            if (sourceObject.hasProperty(propertyName)) {
                targetObject[propertyName] = sourceObject."$propertyName"
                return _save(targetObject, flash)
            }
        }

    }

    @Deprecated
    boolean deleteObjectProperty(Object targetObject, def flash, String propertyName) {

        if (targetObject.hasProperty(propertyName)) {
            if (targetObject[propertyName] instanceof Boolean) {
                targetObject[propertyName] = false
            } else {
                targetObject[propertyName] = null
            }
            return _save(targetObject, flash)
        }

    }

    /**
     * Sets the inheritance flag for the given attribute
     * @param sourceObject the source object
     * @param targetObject the target object
     * @param flash unused
     * @param propertyName the attribute which should be inherited
     * @return true if the config could be set, false otherwise
     */
    boolean toggleAuditObjectProperty(Object sourceObject, Object targetObject, def flash, String propertyName) {

        if (sourceObject.getClass() == targetObject.getClass()) {
            if (sourceObject.hasProperty(propertyName) && !AuditConfig.getConfig(targetObject, propertyName)) {
                AuditConfig.addConfig(targetObject, propertyName)
            }
        }

    }

    /**
     * Removes the inheritance flag for the given attribute
     * @param targetObject the target object
     * @param flash unused
     * @param propertyName the attribute which should be inherited
     * @return true if the config could be removed, false otherwise
     */
    boolean removeToggleAuditObjectProperty(Object targetObject, def flash, String propertyName) {
        if (targetObject.hasProperty(propertyName) && AuditConfig.getConfig(targetObject, propertyName)) {
            AuditConfig.removeConfig(targetObject, propertyName)
        }
    }

    /**
     * Unlinks the given licenses from the target object
     * @param toDeleteLicenses the licenses to be unlinked
     * @param targetObject the target object from which the license should be unlinked
     * @param flash unused
     * @return true if the unlinking was successful, false otherwise
     */
    boolean deleteLicenses(List<License> toDeleteLicenses, Object targetObject, def flash) {
        toDeleteLicenses.each { License lic ->
            subscriptionService.setOrgLicRole(targetObject, lic, true)
        }
    }

    /**
     * Links the licenses to the target object
     * @param toCopyLicenses the licenses to be linked
     * @param targetObject the target object to which the licenses should be linked
     * @param flash unused
     * @return true if the linking was successful, false otherwise
     */
    boolean copyLicenses(List<License> toCopyLicenses, Object targetObject, def flash) {
        toCopyLicenses.each { License lic ->
            subscriptionService.setOrgLicRole(targetObject, lic, false)
        }
    }

    /**
     * Deletes the given organisational relations from the target object
     * @param toDeleteOrgRelations the relations to be deleted
     * @param targetObject the target object from which the relations should be removed
     * @param flash unused
     * @return true if the unlinking was successful, false otherwise
     */
    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Object targetObject, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetObject, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
    }

    /**
     * Unlinks the given persons from the target object
     * @param toDeletePersonRoles the person roles which should be unlinked
     * @param targetObject the target object from which the person contacts should be unlinked
     * @param flash unused
     * @return true if the unlinking was successful, false otherwise
     */
    boolean deleteSpecificSubscriptionEditors(List<PersonRole> toDeletePersonRoles, Object targetObject, def flash) {
        PersonRole.executeUpdate(
                "delete from PersonRole pr where pr in (:personRoles) and pr.sub = :sub",
                [personRoles: toDeletePersonRoles, sub: targetObject]
        )
    }

    /**
     * Links the given organisations to the target object
     * @param toDeleteOrgRelations the relations to be linked
     * @param sourceObject the source object from which the organisational relations should be taken
     * @param targetObject the target object to which the organisations should be linked
     * @param flash the message container
     * @return true if the linking was successful, false otherwise
     */
    boolean copyOrgRelations(List<OrgRole> toCopyOrgRelations, Object sourceObject, Object targetObject, def flash) {
        Locale locale = LocaleUtils.getCurrentLocale()
        if (!targetObject.orgRelations)
            targetObject.orgRelations = []
        //question mark may be necessary because of lazy loading (there were some NPEs here in the past)
        sourceObject.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg().id) && !(or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE, RDStore.OR_LICENSING_CONSORTIUM])) {
                if (targetObject.orgRelations.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
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
                    //this is a bit dangerous ...
                    if (_save(newOrgRole, flash))
                        targetObject.orgRelations << newOrgRole
                }
            }
        }
    }

    /**
     * Copies the given person contacts into the target object
     * @param toCopyPersonRoles the contacts to be copied
     * @param sourceObject the source object from which the contacts should be taken
     * @param targetObject the target object to which the contacts should be linked
     * @param flash the message container
     * @return true if the transfer was successful, false otherwise
     */
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
                _save(newPrsRole, flash)
            }
        }

    }

    /**
     * Unlinks the given packages from the target object. The issue entitlements are going to be marked
     * as deleted as well
     * @param packagesToDelete the packages which should be unlinked
     * @param targetObject the target object from which the package should be unlinked
     * @param flash the message container
     * @return true if the unlinking was successful, false otherwise
     */
    boolean deletePackages(List<SubscriptionPackage> packagesToDelete, Object targetObject, def flash) {
        //alle IEs löschen, die zu den zu löschenden Packages gehören
//        targetObject.issueEntitlements.each{ ie ->
        subscriptionService.getIssueEntitlements(targetObject).each { ie ->
            if (packagesToDelete.find { subPkg -> subPkg?.pkg?.id == ie?.tipp?.pkg?.id }) {
                ie.status = RDStore.TIPP_STATUS_REMOVED
                _save(ie, flash)
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
                    costItem.save()
                }
            }

            SubscriptionPackage.executeUpdate(
                    "delete from SubscriptionPackage sp where sp in (:packagesToDelete) and sp.subscription = :sub ",
                    [packagesToDelete: packagesToDelete, sub: targetObject])
        }
    }

    /**
     * Links the given packages to the target object and sets up the title holdings, access point links and pending
     * change configurations
     * @param packagesToTake the packages to be linked to the target object
     * @param targetObject the target object which should be linked to the packages
     * @param flash the message container
     * @return true if the linking was successful, false otherwise
     */
    boolean copyPackages(List<SubscriptionPackage> packagesToTake, Object targetObject, def flash) {
        Locale locale = LocaleUtils.getCurrentLocale()
        packagesToTake.each { SubscriptionPackage subscriptionPackage ->
            if (!SubscriptionPackage.findByPkgAndSubscription(subscriptionPackage.pkg, targetObject)) {
                List<OrgAccessPointLink> pkgOapls = []
                if(subscriptionPackage.oapls)
                    pkgOapls << OrgAccessPointLink.findAllByIdInList(subscriptionPackage.oapls.id)
                subscriptionPackage.oapls = null
                subscriptionPackage.pendingChangeConfig = null //copied in next step
                SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                newSubscriptionPackage.pkg = subscriptionPackage.pkg
                newSubscriptionPackage.subscription = targetObject
                newSubscriptionPackage.freezeHolding = subscriptionPackage.freezeHolding //may be subject of setting change
                if (_save(newSubscriptionPackage, flash)) {
                    pkgOapls.each { OrgAccessPointLink oapl ->

                        def oaplProperties = oapl.properties
                        oaplProperties.globalUID = null
                        OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                        InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                        newOrgAccessPointLink.subPkg = newSubscriptionPackage
                        newOrgAccessPointLink.save()
                    }
                    Sql sql = GlobalService.obtainSqlConnection()
                    //List subscriptionHolding = sql.rows("select * from title_instance_package_platform join issue_entitlement on tipp_id = ie_tipp_fk where tipp_pkg_fk = :pkgId and ie_subscription_fk = :source", [pkgId: newSubscriptionPackage.pkg.id, source: subscriptionPackage.subscription.id])
                    packageService.bulkAddHolding(sql, targetObject.id, newSubscriptionPackage.pkg.id, targetObject.hasPerpetualAccess)
                    /*
                    List<IssueEntitlement> targetIEs = subscriptionService.getIssueEntitlements(targetObject)
                            //.findAll { it.tipp.id == ie.tipp.id && it.status != RDStore.TIPP_STATUS_REMOVED }
                    subscriptionPackage.getIssueEntitlementsofPackage().each { ie ->
                        //deleted check on both levels here because there are issue entitlements pointing to TIPPs which have been removed from we:kb
                        if (ie.status != RDStore.TIPP_STATUS_REMOVED && ie.tipp.status != RDStore.TIPP_STATUS_REMOVED) {
                            boolean check = targetIEs.find { IssueEntitlement targetIE -> targetIE.tipp.id == ie.tipp.id && targetIE.status != RDStore.TIPP_STATUS_REMOVED }
                            if (check) {
                                // mich gibts schon! Da aber der Prozeß asynchron läuft, kann keine Fehlermeldung (mehr) ausgegeben werden!
                                Object[] args = [ie.name]
                                //flash.error += messageSource.getMessage('subscription.err.titleAlreadyExistsInTargetSub', args, locale)
                            } else {
                                def properties = ie.properties
                                properties.globalUID = null
                                IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                InvokerHelper.setProperties(newIssueEntitlement, properties)
                                newIssueEntitlement.coverages = null
                                newIssueEntitlement.priceItems = null
                                newIssueEntitlement.ieGroups = null
                                newIssueEntitlement.subscription = targetObject

                                if (save(newIssueEntitlement, flash)) {
                                    ie.properties.coverages.each { coverage ->
                                        def coverageProperties = coverage.properties
                                        IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                                        InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                                        newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement
                                        newIssueEntitlementCoverage.save()
                                    }
                                    ie.properties.priceItems.each { priceItem ->
                                        def priceItemProperties = priceItem.properties
                                        PriceItem newPriceItem = new PriceItem()
                                        InvokerHelper.setProperties(newPriceItem, priceItemProperties)
                                        newPriceItem.issueEntitlement = newIssueEntitlement
                                        newPriceItem.save()
                                    }
                                }
                            }
                        }
                    }
                     */
                }
            }
        }
    }

    /**
     * Transfers the pending change configuration into the target subscription-package
     * @param configs the configurations to take
     * @param target the target subscription package to which the configurations should be applied
     * @return true if the transfer was successful, false otherwise
     */
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

    /**
     * Marks the given titles as deleted
     * @param entitlementsToDelete the titles which should be marked as deleted
     * @param targetObject unused
     * @param flash the message container
     * @return true if the deletion was deleted
     */
    boolean deleteEntitlements(List<IssueEntitlement> entitlementsToDelete, Object targetObject, def flash) {
        entitlementsToDelete.each {
            it.status = RDStore.TIPP_STATUS_REMOVED
            _save(it, flash)
        }
//        IssueEntitlement.executeUpdate(
//                "delete from IssueEntitlement ie where ie in (:entitlementsToDelete) and ie.subscription = :sub ",
//                [entitlementsToDelete: entitlementsToDelete, sub: targetObject])
    }

    /**
     * Adds the given titles to the target object's holding
     * @param entitlementsToTake the titles which should be inserted
     * @param targetObject the target subscription to which the titles should be added
     * @param flash the message container
     * @return true if the entitlements were successfully transferred, false otherwise
     */
    boolean copyEntitlements(List<IssueEntitlement> entitlementsToTake, Object targetObject, def flash) {
        Locale locale = LocaleUtils.getCurrentLocale()
        entitlementsToTake.each { ieToTake ->
            if (ieToTake.status != RDStore.TIPP_STATUS_REMOVED) {
                def list = subscriptionService.getIssueEntitlements(targetObject).findAll { it.tipp.id == ieToTake.tipp.id && (it.status != RDStore.TIPP_STATUS_REMOVED) }
                if (list.size() > 0) {
                    // mich gibts schon! Fehlermeldung ausgeben!
                    Object[] args = [ieToTake.name]
                    flash.error += messageSource.getMessage('subscription.err.titleAlreadyExistsInTargetSub', args, locale)
                } else {
                    def properties = ieToTake.properties
                    properties.globalUID = null
                    IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                    InvokerHelper.setProperties(newIssueEntitlement, properties)
                    newIssueEntitlement.coverages = null
                    newIssueEntitlement.priceItems = null
                    newIssueEntitlement.ieGroups = null
                    newIssueEntitlement.subscription = targetObject

                    if (_save(newIssueEntitlement, flash)) {
                        ieToTake.properties.coverages.each { coverage ->

                            def coverageProperties = coverage.properties
                            IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                            InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                            newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement
                            newIssueEntitlementCoverage.save()
                        }

                        ieToTake.properties.priceItems.each { priceItem ->
                            def priceItemProperties = priceItem.properties
                            PriceItem newPriceItem = new PriceItem()
                            InvokerHelper.setProperties(newPriceItem, priceItemProperties)
                            newPriceItem.issueEntitlement = newIssueEntitlement
                            newPriceItem.save()
                        }
                    }
                }
            }
        }
    }

    /**
     * Saves the given object
     * @param obj the object to persist
     * @param flash the message container
     * @return true if the saving was successful, false otherwise
     */
    private boolean _save(obj, flash) {
        if (obj.save()) {
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, LocaleUtils.getCurrentLocale())
            return false
        }
    }

    /**
     * Deletes the given object
     * @param obj the object to be deleted
     * @param flash the message container
     * @return (actually) true if the deletion was successful
     */
    private boolean _delete(obj, flash) {
        if (obj) {
            obj.delete()
            log.debug("Delete ${obj} ok")
        } else {
            flash.error += messageSource.getMessage('default.delete.error.general.message', null, LocaleUtils.getCurrentLocale())
        }
    }

    /**
     * Checks if both source and target objects are loaded
     * @param sourceObject the source object
     * @param targetObject the target object
     * @param flash the message container
     * @return true if both objects are not null, false otherwise
     */
    boolean isBothObjectsSet(Object sourceObject, Object targetObject, FlashScope flash = getCurrentFlashScope()) {
        Locale locale = LocaleUtils.getCurrentLocale()

        if (!sourceObject || !targetObject) {
            Object[] args = [messageSource.getMessage("${sourceObject.getClass().getSimpleName().toLowerCase()}.label", null, locale)]
            if (!sourceObject) flash.error += messageSource.getMessage('copyElementsIntoObject.noSourceObject', args, locale) + '<br />'
            if (!targetObject) flash.error += messageSource.getMessage('copyElementsIntoObject.noTargetObject', args, locale) + '<br />'
            return false
        }
        return true
    }

    /**
     * Builds the comparison tree for the objects to be compared
     * @param objectsToCompare the objects whose property sets should be displayed
     * @return the inverted tree property-object map
     */
    Map regroupObjectProperties(List<Object> objectsToCompare) {
        compareService.compareProperties(objectsToCompare)
    }

    FlashScope getCurrentFlashScope() {
        GrailsWebRequest grailsWebRequest = WebUtils.retrieveGrailsWebRequest()
        HttpServletRequest request = grailsWebRequest.getCurrentRequest()

        grailsWebRequest.attributes.getFlashScope(request)
    }

    /**
     * Copies the given issue entitlement groups and their items
     * @param ieGroups the issue entitlement groups to take
     * @param targetObject the target into which the new records should be copied
     */
    boolean copyIssueEntitlementGroupItem(List<IssueEntitlementGroup> ieGroups, Object targetObject) {

            ieGroups.each { ieGroup ->

                IssueEntitlementGroup issueEntitlementGroup = new IssueEntitlementGroup(
                        name: ieGroup.name,
                        description: ieGroup.description,
                        sub: targetObject
                )
                if (issueEntitlementGroup.save()) {

                    ieGroup.items.each { ieGroupItem ->
                        IssueEntitlement ie = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(targetObject, ieGroupItem.ie.tipp, RDStore.TIPP_STATUS_REMOVED)
                        if (ie && !IssueEntitlementGroupItem.findByIe(ie)) {
                            IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(
                                    ie: ie,
                                    ieGroup: issueEntitlementGroup)

                            if (!issueEntitlementGroupItem.save()) {
                                log.error("Problem saving IssueEntitlementGroupItem ${issueEntitlementGroupItem.errors}")
                            }
                        }
                    }
                }
            }
    }

    /**
     * Deletes the given issue entitlement groups and their items
     * @param ieGroups the issue entitlement groups to clear
     */
    boolean deleteIssueEntitlementGroupItem(List<IssueEntitlementGroup> ieGroups) {

        ieGroups.each { ieGroup ->
                ieGroup.items.each { ieGroupItem ->
                    ieGroupItem.delete()
                }
            ieGroup.delete()
        }
    }
}


