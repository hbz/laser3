package de.laser

import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.helper.ConfigUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import de.laser.interfaces.CalculatedType
import de.laser.oap.OrgAccessPointLink
import de.laser.properties.SubscriptionProperty
import de.laser.system.SystemEvent
import grails.gorm.transactions.Transactional
import groovy.time.TimeCategory
import org.codehaus.groovy.runtime.InvokerHelper

import java.nio.file.Files
import java.nio.file.Path

/**
 * This service handles the automatic renewal of subscriptions
 */
@Transactional
class RenewSubscriptionService extends AbstractLockableService {

    def contextService

    /**
     * Triggered by cronjob
     * Checks whether there are local subscriptions due to renewal, if there is a successor and if it has been flagged for automatic renewal.
     * If so, a successor for the next year ring will be automatically generated
     * @return true if no instance was running and the method could run through, false otherwise
     */
    boolean subscriptionRenewCheck() {
        if (!running) {
            running = true
            println "processing all current local subscriptions with annually periode to renew ..."
            Date currentDate = new Date()

            List renewSuccessSubIds = []
            List renewFailSubIds = []

            // CURRENT -> EXPIRED

            Set<Long> currentSubsIds = Subscription.executeQuery('select s.id from Subscription s where s.status = :status and s.startDate < :currentDate and (s.endDate != null and s.endDate <= :currentDate) and s.type = :type and s.isAutomaticRenewAnnually = true',
                    [status: RDStore.SUBSCRIPTION_CURRENT, currentDate: currentDate, type: RDStore.SUBSCRIPTION_TYPE_LOCAL])

            log.info("Current subscriptions reached end date and are now (${currentDate}) to renew: " + currentSubsIds)

            if (currentSubsIds) {
                Subscription.withTransaction {
                    currentSubsIds.each { Long id ->
                        Subscription subscription = Subscription.get(id)
                        if ((subscription._getCalculatedType() == CalculatedType.TYPE_LOCAL) && subscription.isAllowToAutomaticRenewAnnually() && !subscription._getCalculatedSuccessor()) {
                            boolean fail = false
                            Org org = subscription.getSubscriber()

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
                            copySub.prsLinks = null
                            copySub.derivedSubscriptions = null
                            copySub.pendingChanges = null
                            copySub.propertySet = null
                            copySub.costItems = null
                            copySub.ieGroups = null

                            use(TimeCategory) {
                                copySub.startDate = subscription.startDate + 1.year
                                copySub.endDate = subscription.endDate + 1.year
                            }

                            if (copySub.save()) {

                                //link to previous subscription
                                Links prevLink = Links.construct([source: copySub, destination: subscription, linkType: RDStore.LINKTYPE_FOLLOWS, owner: org])
                                if (!prevLink) {
                                    log.error("Problem linking to previous subscription: ${prevLink.errors}")
                                    fail = true
                                }

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
                                    if (ie.status != RDStore.TIPP_STATUS_DELETED) {
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
                                                def coverageProperties = coverage.properties
                                                IssueEntitlementCoverage newIssueEntitlementCoverage = new IssueEntitlementCoverage()
                                                InvokerHelper.setProperties(newIssueEntitlementCoverage, coverageProperties)
                                                newIssueEntitlementCoverage.issueEntitlement = newIssueEntitlement

                                                if (!newIssueEntitlementCoverage.save()) {
                                                    log.error("Problem saving IssueEntitlementCoverage ${newIssueEntitlementCoverage.errors}")
                                                    fail = true
                                                }
                                            }

                                            ie.priceItems.each { PriceItem priceItem ->
                                                def priceItemProperties = priceItem.properties

                                                PriceItem newPriceItem = new PriceItem()
                                                InvokerHelper.setProperties(newPriceItem, priceItemProperties)
                                                newPriceItem.issueEntitlement = newIssueEntitlement
                                                newPriceItem.globalUID = null

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
                                            IssueEntitlement ie = IssueEntitlement.findBySubscriptionAndTippAndStatusNotEqual(copySub, ieGroupItem.ie.tipp, RDStore.TIPP_STATUS_DELETED)
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

                                //Documents
                                subscription.documents.each { DocContext dctx ->
                                Doc newDoc = new Doc()
                                InvokerHelper.setProperties(newDoc, dctx.owner.properties)

                                if (newDoc.save()) {
                                    DocContext newDocContext = new DocContext()
                                    InvokerHelper.setProperties(newDocContext, dctx.properties)
                                    newDocContext.subscription = copySub
                                    newDocContext.owner = newDoc

                                    if (!newDocContext.save()) {
                                        log.error("Problem saving DocContext ${newDocContext.errors}")
                                        fail = true
                                    } else {

                                        if ((dctx.owner?.contentType == Doc.CONTENT_TYPE_FILE) && (dctx.status?.value != 'Deleted')) {
                                            try {
                                                String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'

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

                                //CostItems
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

                                Doc docContent = new Doc(contentType: Doc.CONTENT_TYPE_STRING, content: 'Diese Lizenz ist eine Kopie der vorherigen Lizenz. Es wurde automatisch vom System erstellt, da in der vorherigen Lizenz das Flag "Automatisch um ein Jahr verlängern" gesetzt war. (This subscription is a copy of the previous subscription. It was created automatically by the system because the flag "Automatic renew annually" was set in the previous subscription.)', title: 'Automatisch um ein Jahr verlängert (Automatic renew annually)', type: RefdataValue.getByValueAndCategory('Note', RDConstants.DOCUMENT_TYPE), owner: org, user: null)
                                if(docContent.save()) {
                                    DocContext dc = new DocContext(subscription: copySub, owner: docContent, doctype: RDStore.DOC_TYPE_NOTE)
                                    dc.save()
                                }

                            } else {
                                log.error("Problem saving Subscription ${copySub.errors}")
                                fail = true
                            }

                            if (fail) {
                                renewFailSubIds << copySub.id
                            } else {
                                renewSuccessSubIds << copySub.id
                            }
                        }
                    }
                }
            }

            SystemEvent.createEvent('SUB_RENEW_SERVICE_PROCESSING', ["renew Subscription Fail (${renewFailSubIds.size()})": renewFailSubIds, "renew Subscription Success (${renewSuccessSubIds.size()})": renewSuccessSubIds])
            running = false

            return true
        } else {
            log.warn("Subscription renew check already running ... not starting again.")
            return false
        }
    }

}
