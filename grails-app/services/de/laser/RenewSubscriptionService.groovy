package de.laser

import de.laser.addressbook.PersonRole
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.storage.RDStore
import de.laser.base.AbstractLockableService
import de.laser.interfaces.CalculatedType
import de.laser.oap.OrgAccessPointLink
import de.laser.properties.SubscriptionProperty
import de.laser.system.SystemEvent
import de.laser.wekb.ProviderRole
import de.laser.wekb.VendorRole
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.gorm.events.AutoTimestampEventListener

import java.nio.file.Files
import java.nio.file.Path

/**
 * This service handles the automatic renewal of subscriptions
 */
@Transactional
class RenewSubscriptionService extends AbstractLockableService {

    ContextService contextService
    AutoTimestampEventListener autoTimestampEventListener


    static final String AUTOMATIC_RENEW_ANNUALLY_DOC_TITLE = 'Automatisch um ein Jahr verlängert (Automatic renew annually)'

    /**
     * Triggered by cronjob
     * Checks whether there are local subscriptions due to renewal, if there is a successor and if it has been flagged for automatic renewal.
     * If so, a successor for the next year ring will be automatically generated
     * @return true if no instance was running and the method could run through, false otherwise
     */
    boolean subscriptionRenewCheck() {
        if (!running) {
            running = true
            log.debug "processing all current local subscriptions with annually periode to renew ..."
            Date currentDate = new Date()

            List renewSuccessSubIds = []
            List renewFailSubIds = []

            // CURRENT -> EXPIRED

            Set<Long> currentSubsIds = Subscription.executeQuery('select s.id from Subscription s where s.status = :status and s.startDate < :currentDate and (s.endDate != null and s.endDate <= :currentDate) and s.type = :type and s.isAutomaticRenewAnnually = true',
                    [status: RDStore.SUBSCRIPTION_CURRENT, currentDate: currentDate, type: RDStore.SUBSCRIPTION_TYPE_LOCAL])

            log.info("Current subscriptions reached end date and are now (${currentDate}) to renew: " + currentSubsIds)

            if (currentSubsIds) {
                    currentSubsIds.each { Long id ->
                        Subscription subscription = Subscription.get(id)
                        if ((subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL) && subscription.isAllowToAutomaticRenewAnnually() && !subscription._getCalculatedSuccessor()) {
                            boolean fail = false
                            Org org = subscription.getSubscriberRespConsortia()

                            def newProperties = subscription.properties
                            Subscription copySub = new Subscription()
                            InvokerHelper.setProperties(copySub, newProperties)
                            copySub.id = null
                            copySub.globalUID = null
                            copySub.ids = null
                            copySub.packages = null
                            copySub.issueEntitlements = null
                            copySub.documents = null
                            copySub.orgRelations = null
                            copySub.providerRelations = null
                            copySub.vendorRelations = null
                            copySub.prsLinks = null
                            copySub.derivedSubscriptions = null
                            copySub.propertySet = null
                            copySub.costItems = null
                            copySub.ieGroups = null
                            copySub.discountScales = null
                            copySub.altnames = null

                            use(TimeCategory) {
                                copySub.startDate = subscription.endDate + 1.day
                                copySub.endDate = subscription.endDate + 1.year
                            }
                            copySub.referenceYear = copySub.referenceYear ? subscription.referenceYear.plusYears(1) : null

                            if (copySub.save()) {

                                //link to previous subscription
                                Links prevLink = Links.construct([source: copySub, destination: subscription, linkType: RDStore.LINKTYPE_FOLLOWS, owner: org])
                                if (!prevLink) {
                                    log.error("Problem linking to previous subscription: ${prevLink.errors}")
                                    fail = true
                                }

                                subscription.status = RDStore.SUBSCRIPTION_EXPIRED
                                subscription.save()

                                //link to license
                                Set<Links> precedingLicenses = Links.findAllByDestinationSubscriptionAndLinkType(subscription, RDStore.LINKTYPE_LICENSE)
                                precedingLicenses.each { Links link ->
                                    Map<String, Object> successorLink = [source: link.sourceLicense, destination: copySub, linkType: RDStore.LINKTYPE_LICENSE, owner: org]
                                    Links.construct(successorLink)
                                }

                                //OrgRoles
                                subscription.orgRelations.each { OrgRole or ->
                                    def newOrgRoleProperties = or.properties
                                    OrgRole newOrgRole = new OrgRole()
                                    InvokerHelper.setProperties(newOrgRole, newOrgRoleProperties)
                                    //Vererbung ausschalten
                                    //newOrgRole.sharedFrom = null
                                    //newOrgRole.isShared = false
                                    newOrgRole.sub = copySub
                                    newOrgRole.id = null

                                    if (!newOrgRole.save()) {
                                        log.error("Problem saving OrgRole ${newOrgRole.errors}")
                                        fail = true
                                    }
                                }

                                //ProviderRoles
                                subscription.providerRelations.each { ProviderRole pvr ->
                                    def newProviderRoleProperties = pvr.properties
                                    ProviderRole newProviderRole = new ProviderRole()
                                    InvokerHelper.setProperties(newProviderRole, newProviderRoleProperties)
                                    //Vererbung ausschalten
                                    //newProviderRole.sharedFrom = null
                                    //newProviderRole.isShared = false
                                    newProviderRole.subscription = copySub
                                    newProviderRole.id = null

                                    if (!newProviderRole.save()) {
                                        log.error("Problem saving ProviderRole ${newProviderRole.errors}")
                                        fail = true
                                    }
                                }

                                //VendorRoles
                                subscription.vendorRelations.each { VendorRole vr ->
                                    def newVendorRoleProperties = vr.properties
                                    VendorRole newVendorRole = new VendorRole()
                                    InvokerHelper.setProperties(newVendorRole, newVendorRoleProperties)
                                    //Vererbung ausschalten
                                    //newVendorRole.sharedFrom = null
                                    //newVendorRole.isShared = false
                                    newVendorRole.subscription = copySub
                                    newVendorRole.id = null

                                    if (!newVendorRole.save()) {
                                        log.error("Problem saving VendorRole ${newVendorRole.errors}")
                                        fail = true
                                    }
                                }

                                //Identifiers
                                subscription.ids.each { Identifier identifier ->
                                    Identifier.constructWithFactoryResult([value: identifier.value, parent: identifier.instanceOf, reference: copySub, namespace: identifier.ns, note: identifier.note])
                                }

                                //Packages
                                subscription.packages.each { SubscriptionPackage subscriptionPackage ->
                                    def pkgOapls = subscriptionPackage.oapls
                                    def pkgPcc = subscriptionPackage.pendingChangeConfig
                                    SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                                    InvokerHelper.setProperties(newSubscriptionPackage, subscriptionPackage.properties)
                                    newSubscriptionPackage.subscription = copySub
                                    newSubscriptionPackage.oapls = null
                                    newSubscriptionPackage.pendingChangeConfig = null

                                    if (newSubscriptionPackage.save()) {
                                        pkgOapls.each { OrgAccessPointLink oapl ->

                                            def oaplProperties = oapl.properties
                                            OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                                            InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                                            newOrgAccessPointLink.subPkg = newSubscriptionPackage
                                            newOrgAccessPointLink.globalUID = null

                                            if (!newOrgAccessPointLink.save()) {
                                                log.error("Problem saving OrgAccessPointLink ${newOrgAccessPointLink.errors}")
                                                fail = true
                                            }
                                        }

                                        pkgPcc.each { PendingChangeConfiguration pcc ->

                                            def pccProperties = pcc.properties
                                            PendingChangeConfiguration newPendingChangeConfiguration = new PendingChangeConfiguration()
                                            InvokerHelper.setProperties(newPendingChangeConfiguration, pccProperties)
                                            newPendingChangeConfiguration.subscriptionPackage = newSubscriptionPackage

                                            if (!newPendingChangeConfiguration.save()) {
                                                log.error("Problem saving PendingChangeConfiguration ${newPendingChangeConfiguration.errors}")
                                                fail = true
                                            }
                                        }
                                    } else {
                                        log.error("Problem saving SubscriptionPackage ${newSubscriptionPackage.errors}")
                                        fail = true
                                    }

                                }

                                //IssueEntitlements
                                subscription.issueEntitlements.each { IssueEntitlement ie ->
                                    if (ie.status != RDStore.TIPP_STATUS_REMOVED) {
                                        def ieProperties = ie.properties

                                        IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                        InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                                        newIssueEntitlement.coverages = null
                                        newIssueEntitlement.ieGroups = null
                                        newIssueEntitlement.priceItems = null
                                        newIssueEntitlement.globalUID = null
                                        newIssueEntitlement.subscription = copySub

                                        if (newIssueEntitlement.save()) {
                                            ie.coverages.each { IssueEntitlementCoverage coverage ->
                                                IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage(issueEntitlement: newIssueEntitlement)
                                                newIssueEntitlementCoverage.startDate = coverage.startDate
                                                newIssueEntitlementCoverage.startVolume = coverage.startVolume
                                                newIssueEntitlementCoverage.startIssue = coverage.startIssue
                                                newIssueEntitlementCoverage.endDate = coverage.endDate
                                                newIssueEntitlementCoverage.endVolume = coverage.endVolume
                                                newIssueEntitlementCoverage.endIssue = coverage.endIssue
                                                newIssueEntitlementCoverage.coverageDepth = coverage.coverageDepth
                                                newIssueEntitlementCoverage.coverageNote = coverage.coverageNote
                                                newIssueEntitlementCoverage.embargo = coverage.embargo

                                                if (!newIssueEntitlementCoverage.save()) {
                                                    log.error("Problem saving IssueEntitlementCoverage ${newIssueEntitlementCoverage.errors}")
                                                    fail = true
                                                }
                                            }

                                            ie.priceItems.each { PriceItem priceItem ->
                                                PriceItem newPriceItem = new PriceItem(issueEntitlement: newIssueEntitlement)
                                                newPriceItem.startDate = priceItem.startDate
                                                newPriceItem.endDate = priceItem.endDate
                                                newPriceItem.listPrice = priceItem.listPrice
                                                newPriceItem.listCurrency = priceItem.listCurrency
                                                newPriceItem.localPrice = priceItem.localPrice
                                                newPriceItem.localCurrency = priceItem.localCurrency
                                                newPriceItem.setGlobalUID()


                                                if (!newPriceItem.save()) {
                                                    log.error("Problem saving PriceItem ${newPriceItem.errors}")
                                                    fail = true
                                                }
                                            }
                                        } else {
                                            log.error("Problem saving IssueEntitlement ${newIssueEntitlement.errors}")
                                            fail = true
                                        }
                                    }
                                }

                                //IEGroups
                                subscription.ieGroups.each { IssueEntitlementGroup ieGroup ->

                                    def issueEntitlementGroupProperties = ieGroup.properties

                                    IssueEntitlementGroup newIssueEntitlementGroup = new IssueEntitlementGroup()
                                    InvokerHelper.setProperties(newIssueEntitlementGroup, issueEntitlementGroupProperties)
                                    newIssueEntitlementGroup.sub = copySub
                                    newIssueEntitlementGroup.items = null


                                    if (newIssueEntitlementGroup.save()) {

                                        ieGroup.items.each { IssueEntitlementGroup ieGroupItem ->
                                            IssueEntitlement ie = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(copySub, ieGroupItem.ie.tipp, RDStore.TIPP_STATUS_REMOVED)
                                            if (ie && !IssueEntitlementGroupItem.findByIe(ie)) {
                                                IssueEntitlementGroupItem issueEntitlementGroupItem = new IssueEntitlementGroupItem(
                                                        ie: ie,
                                                        ieGroup: newIssueEntitlementGroup)

                                                if (!issueEntitlementGroupItem.save()) {
                                                    log.error("Problem saving IssueEntitlementGroupItem ${issueEntitlementGroupItem.errors}")
                                                    fail = true
                                                }
                                            }
                                        }
                                    } else {
                                        log.error("Problem saving IssueEntitlementGroup ${newIssueEntitlementGroup.errors}")
                                        fail = true
                                    }
                                }

                                //DiscountScales
                                subscription.discountScales.each { SubscriptionDiscountScale sds ->

                                    def discountScaleProperties = sds.properties

                                    SubscriptionDiscountScale newSubscriptionDiscountScale = new SubscriptionDiscountScale()
                                    InvokerHelper.setProperties(newSubscriptionDiscountScale, discountScaleProperties)
                                    newSubscriptionDiscountScale.subscription = copySub


                                    if (!newSubscriptionDiscountScale.save()) {
                                        log.error("Problem saving SubscriptionDiscountScale ${newSubscriptionDiscountScale.errors}")
                                        fail = true
                                    }
                                }

                                //Altnames
                                subscription.altnames.each { AlternativeName altname ->
                                    def altnameProperties = altname.properties
                                    AlternativeName newAltName = new AlternativeName()
                                    InvokerHelper.setProperties(newAltName, altnameProperties)
                                    newAltName.subscription = copySub

                                    if(!newAltName.save()) {
                                        log.error("Problem saving AlternativeName ${newAltName.errors}")
                                        fail = true
                                    }
                                }

                                //Documents
                                subscription.documents.each { DocContext dctx ->
                                    if (dctx.owner.title != AUTOMATIC_RENEW_ANNUALLY_DOC_TITLE) {
                                        //Because of autoTimestampEventListener.withoutTimestamps closure the old dateCreated and lastUpdated of doc are not overwritten (see gorm doc ->  Automatic timestamping)
                                        //Because the timestamp handling is only disabled for the duration of the closure, you must flush the session during the closure execution!
                                        //autoTimestampEventListener.withoutTimestamps {
                                            Doc newDoc = new Doc()
                                            InvokerHelper.setProperties(newDoc, dctx.owner.properties)

                                            if (newDoc.save()) {
                                                DocContext newDocContext = new DocContext()
                                                InvokerHelper.setProperties(newDocContext, dctx.properties)
                                                newDocContext.subscription = copySub
                                                newDocContext.owner = newDoc
                                                //newDocContext.dateCreated = new Date()
                                                //newDocContext.lastUpdated = new Date()

                                                if (!newDocContext.save()) {
                                                    log.error("Problem saving DocContext ${newDocContext.errors}")
                                                    fail = true
                                                } else {

                                                    if (dctx.isDocAFile() && (dctx.status?.value != 'Deleted')) {
                                                        try {
                                                            String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK

                                                            Path source = new File("${fPath}/${dctx.owner.uuid}").toPath()
                                                            Path target = new File("${fPath}/${newDoc.uuid}").toPath()
                                                            Files.copy(source, target)

                                                        }
                                                        catch (Exception e) {
                                                            log.error("Problem by Saving Doc in documentStorageLocation (Doc ID: ${dctx.owner.id} -> ${e})")
                                                            fail = true
                                                        }
                                                    }
                                                }
                                            } else {
                                                log.error("Problem saving Doc ${newDoc.errors}")
                                                fail = true
                                            }
                                        //}
                                    }
                                }

                                //PersonRole
                                subscription.prsLinks.each { PersonRole prsLink ->
                                    PersonRole newPersonRole = new PersonRole()
                                    InvokerHelper.setProperties(newPersonRole, prsLink.properties)
                                    newPersonRole.sub = copySub

                                    if (!newPersonRole.save()) {
                                        log.error("Problem saving PersonRole ${newPersonRole.errors}")
                                        fail = true
                                    }

                                }

                                //SubscriptionProperty
                                //customProperties of Subscriber && privateProperties of Subscriber
                                subscription.propertySet.each { SubscriptionProperty subProp ->
                                    if ((subProp.type.tenant == null && (subProp.tenant?.id == org.id || subProp.tenant == null)) || subProp.type.tenant?.id == org.id) {
                                        SubscriptionProperty copiedProp = new SubscriptionProperty(type: subProp.type, owner: copySub, isPublic: subProp.isPublic, tenant: subProp.tenant)
                                        copiedProp = subProp.copyInto(copiedProp)

                                        if (!copiedProp.save()) {
                                            log.error("Problem saving SubscriptionProperty ${copiedProp.errors}")
                                            fail = true
                                        }
                                    }
                                }

                                //CostItems - deactivated as of ERMS-6062
                                /*
                                subscription.costItems.each { CostItem costItem ->
                                    def costItemProperties = costItem.properties

                                    CostItem newCostItem = new CostItem()
                                    InvokerHelper.setProperties(newCostItem, costItemProperties)
                                    newCostItem.sub = copySub
                                    newCostItem.globalUID = null

                                    if (!newCostItem.save()) {
                                        log.error("Problem saving CostItem ${newCostItem.errors}")
                                        fail = true
                                    }
                                }
                                */

                                Doc docContent = new Doc(contentType: Doc.CONTENT_TYPE_STRING, content: 'Diese Lizenz ist eine Kopie der vorherigen Lizenz. Es wurde automatisch vom System erstellt, da in der vorherigen Lizenz das Flag "Automatisch um ein Jahr verlängern" gesetzt war. (This subscription is a copy of the previous subscription. It was created automatically by the system because the flag "Automatic renew annually" was set in the previous subscription.)', title: AUTOMATIC_RENEW_ANNUALLY_DOC_TITLE, type: RDStore.DOC_TYPE_NOTE, owner: org, user: null)
                                if(docContent.save()) {
                                    DocContext dc = new DocContext(subscription: copySub, owner: docContent)
                                    dc.save()
                                }

                            } else {
                                log.error("Problem saving Subscription ${copySub.errors}")
                                fail = true
                            }

                            if (fail) {
                                renewFailSubIds << copySub.id
                            } else {
                                PendingChange.construct([target: copySub, oid: "${copySub.getClass().getName()}:${copySub.id}", msgToken: "pendingChange.message_SU_NEW_02", status: RDStore.PENDING_CHANGE_PENDING, owner: org])
                                renewSuccessSubIds << copySub.id
                            }
                        }
                    }
            }

            if (renewFailSubIds.size() > 0 || renewSuccessSubIds.size() > 0 ) {
                SystemEvent.createEvent('SUB_RENEW_SERVICE_PROCESSING', ["renew Subscription Fail (${renewFailSubIds.size()})": renewFailSubIds, "renew Subscription Success (${renewSuccessSubIds.size()})": renewSuccessSubIds])
            }
            running = false

            return true
        } else {
            log.warn("Subscription renew check already running ... not starting again.")
            return false
        }
    }

}
