package de.laser

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.IssueEntitlement
import com.k_int.kbplus.OrgRole
import com.k_int.kbplus.Package
import com.k_int.kbplus.Subscription
import com.k_int.kbplus.SubscriptionCustomProperty
import com.k_int.kbplus.SubscriptionPackage
import com.k_int.kbplus.SubscriptionPrivateProperty
import com.k_int.kbplus.Task
import com.k_int.kbplus.TitleInstancePackagePlatform
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import de.laser.helper.RDStore
import org.codehaus.groovy.runtime.InvokerHelper

class SubscriptionService {
    def contextService
    def subscriptionsQueryService

    public static final String COPY = "COPY"
    public static final String REPLACE = "REPLACE"
    public static final String DO_NOTHING = "DO_NOTHING"

    List getMySubscriptions_readRights(){
        List result = []
        List tmpQ

        if(contextService.org.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id)) {
            tmpQ = getSubscriptionsConsortiaQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

        } else {
            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        }
        result.sort{it.name?.toLowerCase()}
    }

    List getMySubscriptions_writeRights(){
        List result = []
        List tmpQ

        if(contextService.org.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id)) {
            tmpQ = getSubscriptionsConsortiaQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

        } else {
            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        }
        result.sort{it.name?.toLowerCase()}
    }

    //Konsortiallizenzen
    private List getSubscriptionsConsortiaQuery() {
        Map params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.showParentsAndChildsSubs = 'true'
        params.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    //Teilnehmerlizenzen
    private List getSubscriptionsConsortialLicenseQuery() {
        Map params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        params.subTypes = RDStore.SUBSCRIPTION_TYPE_CONSORTIAL_LICENSE.id
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    //Lokallizenzen
    private List getSubscriptionsLocalLicenseQuery() {
        Map params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        params.subTypes = RDStore.SUBSCRIPTION_TYPE_LOCAL_LICENSE.id
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    List getValidSubChilds(Subscription subscription) {
        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                subscription,
                RDStore.SUBSCRIPTION_DELETED
        )

        validSubChilds = validSubChilds.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa.sortname ?: sa.name).compareTo((sb.sortname ?: sb.name))
        }
        validSubChilds
    }

    List getIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status <> :del",
                        [sub: subscription, del: RDStore.IE_DELETED])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getVisibleOrgRelationsWithoutConsortia(Subscription subscription) {
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

    List getVisibleOrgRelations(Subscription subscription) {
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

    boolean deleteOwner(Subscription targetSub, def flash) {
        targetSub.owner = null
        return save(targetSub, flash)
    }

    boolean takeOwner(Subscription sourceSub, Subscription targetSub, def flash) {
        //Vertrag/License
        targetSub.owner = sourceSub.owner ?: null
        return save(targetSub, flash)
    }

    boolean deleteOrgRelations(Subscription targetSub, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.roleType not in (:roleTypes)",
                [orgRelations: targetSub.orgRelations, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
    }

    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Subscription targetSub, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetSub, roleTypes: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]]
        )
    }

    boolean takeOrgRelations(List<OrgRole> toCopyOrgRelations, Subscription sourceSub, Subscription targetSub, def flash) {
        sourceSub.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
                if (targetSub.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                    flash.error += or?.roleType?.getI10n("value") + " " + or?.org?.name + " wurde nicht hinzugefügt, weil er in der Ziellizenz schon existiert. <br />"
                } else {
                    def newProperties = or.properties
                    //Vererbung ausschalten
                    newProperties.sharedFrom = null
                    newProperties.isShared = false
                    OrgRole newOrgRole = new OrgRole()
                    InvokerHelper.setProperties(newOrgRole, newProperties)
                    newOrgRole.sub = targetSub
                    save(newOrgRole, flash)
                }
            }
        }
    }

    boolean takeOrgRelations(String aktion, Subscription sourceSub, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion) && targetSub?.orgRelations?.size() > 0) {
            deleteOrgRelations(targetSub, flash)
        }
        switch (aktion) {
            case REPLACE:
            case COPY:
                getVisibleOrgRelationsWithoutConsortia(sourceSub)?.each { or ->
                    if (targetSub.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                        flash.error += or?.roleType?.getI10n("value") + " " + or?.org?.name + " wurde nicht hinzugefügt, weil er in der Ziellizenz schon existiert. <br />"
                    } else {
                        def newProperties = or.properties
                        //Vererbung ausschalten
                        newProperties.sharedFrom = null
                        newProperties.isShared = false
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, newProperties)
                        newOrgRole.sub = targetSub
                        save(newOrgRole, flash)
                    }
                }
                break;

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takePackages(String aktion, List<Package> packagesToTake, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
            //alle IEs löschen, die zu den zu löschenden Packages gehören
            targetSub.issueEntitlements.each{ ie ->
                TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(ie.tipp.id)
                if (targetSub.packages.find { p -> p.pkg.id == tipp.pkg.id } ) {
                    ie.status = RDStore.IE_DELETED
                    save(ie, flash)
                }
            }

            //alle zugeordneten Packages löschen
            SubscriptionPackage.executeUpdate(
                    "delete from SubscriptionPackage sp where sp.subscription = :sub ",
                    [sub: targetSub])
        }

        switch (aktion) {
            case REPLACE:
            case COPY:
                packagesToTake?.each { pkg ->
                    if (targetSub.packages?.find { it.pkg?.id == pkg?.id }) {
                        flash.error = "Das Paket " + pkg.name + " wurde nicht hinzugefügt, weil es in der Ziellizenz schon existiert."
                    } else {
                        SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                        newSubscriptionPackage.subscription = targetSub
                        newSubscriptionPackage.pkg = pkg
                        save(newSubscriptionPackage, flash)
                    }
                }
                break;

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takeEntitlements(String aktion, List<IssueEntitlement> entitlementsToTake, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
//            targetSub.issueEntitlements.each {
            getIssueEntitlements(targetSub).each {
                it.status = RDStore.IE_DELETED
                save(it, flash)
            }
        }

        switch (aktion) {
            case REPLACE:
            case COPY:
                entitlementsToTake.each { ieToTake ->
                    if (ieToTake.status != RDStore.IE_DELETED) {
                        def list = getIssueEntitlements(targetSub).findAll{it.tipp.id == ieToTake.tipp.id && it.status != RDStore.IE_DELETED}
                        if (list?.size() > 0) {
                            // mich gibts schon! Fehlermeldung ausgeben!
                            flash.error = ieToTake.tipp.title.title + " wurde nicht hinzugefügt, weil es in der Ziellizenz schon existiert."
                        } else {
                            def properties = ieToTake.properties
                            properties.globalUID = null
                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                            InvokerHelper.setProperties(newIssueEntitlement, properties)
                            newIssueEntitlement.subscription = targetSub
                            save(newIssueEntitlement, flash)
                        }
                    }
                }
                break;

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takeTasks(String aktion, Subscription sourceSub, def toCopyTasks, Subscription targetSub, def flash) {
        switch (aktion) {
            case COPY:
                toCopyTasks.each { tsk ->
                    def task = Task.findBySubscriptionAndId(sourceSub, tsk)
                    if (task) {
                        if (task.status != RDStore.TASK_STATUS_DONE) {
                            Task newTask = new Task()
                            InvokerHelper.setProperties(newTask, task.properties)
                            newTask.subscription = targetSub
                            save(newTask, flash)
                        }
                    }
                }
                break;

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takeAnnouncements(String aktion, Subscription sourceSub, def toCopyAnnouncements, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
            targetSub.documents.each {
                if ((it.owner?.contentType == Doc.CONTENT_TYPE_STRING  && !(it.domain))){
                    it.status = RDStore.IE_DELETED
                    save(it, flash)
                }
            }
        }

        switch (aktion) {
            case COPY:
            case REPLACE:
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
                break

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }
    boolean deleteDates(Subscription targetSub, def flash){
        targetSub.startDate = null
        targetSub.endDate = null
        return save(targetSub, flash)
    }


    boolean takeDates(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.setStartDate(sourceSub.getStartDate())
        targetSub.setEndDate(sourceSub.getEndDate())
        return save(targetSub, flash)
    }

    boolean takeDoks(String aktion, Subscription sourceSub, def toCopyDocs, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
            targetSub.documents.each {
                if ((it.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (it.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) {
                    it.status = RDStore.IE_DELETED
                    save(it, flash)
                }
            }
        }

        switch (aktion) {
            case REPLACE:
            case COPY:
                sourceSub.documents?.each { dctx ->
                    if (dctx.id in toCopyDocs) {
                        if (((dctx.owner?.contentType == Doc.CONTENT_TYPE_DOCSTORE) || (dctx.owner?.contentType == Doc.CONTENT_TYPE_BLOB)) && (dctx.status?.value != 'Deleted')) {
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
                break

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takeProperties(String aktion, List<AbstractProperty> properties, Subscription targetSub, def flash){
        switch (aktion) {
            case COPY:
                def contextOrg = contextService.getOrg()
                def targetProp

                properties?.each { sourceProp ->
                    if (sourceProp instanceof CustomProperty) {
                        targetProp = targetSub.customProperties.find { it.typeId == sourceProp.typeId }
                    }
                    if (sourceProp instanceof PrivateProperty && sourceProp.type?.tenant?.id == contextOrg?.id) {
                        targetProp = targetSub.privateProperties.find { it.typeId == sourceProp.typeId }
                    }
                    boolean isAddNewProp = sourceProp.type?.multipleOccurrence
                    if ( (! targetProp) || isAddNewProp) {
                        if (sourceProp instanceof CustomProperty) {
                            targetProp = new SubscriptionCustomProperty(type: sourceProp.type, owner: targetSub)
                        } else {
                            targetProp = new SubscriptionPrivateProperty(type: sourceProp.type, owner: targetSub)
                        }
                    }
                    targetProp = sourceProp.copyInto(targetProp)
                    save(targetProp, flash)
                }
                break;

            default:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean deleteProperties(List<AbstractProperty> properties, Subscription targetSub, def flash){
        int anzCP = SubscriptionCustomProperty.executeUpdate("delete from SubscriptionCustomProperty p where p in (:properties)",[properties: properties])
        int anzPP = SubscriptionPrivateProperty.executeUpdate("delete from SubscriptionPrivateProperty p where p in (:properties)",[properties: properties])
        if (properties.size() == anzCP + anzPP) {
            log.debug("Delete ok: " + properties)
        } else {
            log.error("Problem deleting properties: ${properties}. Number of Properties to delete: ${properties.size()}. Number of CustomProperties deleted: ${anzCP}. Number of PrivateProperties deleted: ${anzPP} ")
            flash.error += "Es ist ein Fehler beim Löschen aufgetreten."
        }
    }

    private boolean save(obj, flash){
        if (obj.save(flush: true)){
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving property ${obj.errors}")
            flash.error += "Es ist ein Fehler beim Speichern von ${obj.value} aufgetreten."
            return false
        }
    }
}