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
        def params = [:]
        List result
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        result.sort{it.name}
    }

    List getMySubscriptions_writeRights(){
        List result
        Map params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIPTION_CONSORTIA.value
        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        params = [:]
        params.status = RDStore.SUBSCRIPTION_CURRENT.id
        params.orgRole = RDStore.OR_SUBSCRIBER.value
        params.subTypes = "${RDStore.SUBSCRIPTION_TYPE_LOCAL_LICENSE.id}"
        tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))
        result.sort{it.name}
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

    List getVisibleOrgRelations(Subscription subscription) {
        // restrict visible for templates/links/orgLinksAsList
        List visibleOrgRelations = []
        subscription?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

    boolean takeOwner(String aktion, Subscription sourceSub, Subscription targetSub, def flash) {
        //Vertrag/License
        switch (aktion) {
            case REPLACE:
                targetSub.owner = sourceSub.owner ?: null
                targetSub.save(flush: true)
                break;
            case COPY:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takeOrgRelations(String aktion, Subscription sourceSub, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
            OrgRole.executeUpdate("delete from OrgRole o where o in (:orgRelations)",[orgRelations: targetSub.orgRelations])
        }
        switch (aktion) {
            case REPLACE:
            case COPY:
                sourceSub.orgRelations?.each { or ->
                    if (targetSub.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                        flash.error += or?.roleType?.getI10n("value") + " " + or?.org?.name + " wurde nicht hinzugefügt, weil er in der Ziellizenz schon existiert. <br />"
                    } else {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = targetSub
                        newOrgRole.save(flush: true)
                    }
//                    }
                }
                break;
        }
    }

    boolean takePackages(String aktion, List<Package> packagesToTake, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
            //alle IEs löschen, die zu den zu löschenden Packages gehören
            targetSub.issueEntitlements.each{ ie ->
                TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.get(ie.tipp.id)
                if (targetSub.packages.find { p -> p.pkg.id == tipp.pkg.id } ) {
                    ie.status = RDStore.IE_DELETED
                    ie.save(flush: true)
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
                        newSubscriptionPackage.save(flush: true)
                    }
                }
                // fixed hibernate error: java.util.ConcurrentModificationException
                // change owner before first save
                //License
                //newSub.owner = baseSub.owner ?: null
                //newSub.save(flush: true)
                break;
        }
    }

    boolean takeEntitlements(String aktion, List<IssueEntitlement> entitlementsToTake, Subscription targetSub, def flash) {
        if (REPLACE.equals(aktion)) {
//            targetSub.issueEntitlements.each {
            getIssueEntitlements(targetSub).each {
                it.status = RDStore.IE_DELETED
                it.save(flush: true)
            }
        }

        switch (aktion) {
            case REPLACE:
            case COPY:
                entitlementsToTake.each { ieToTake ->
                    if (ieToTake.status != RDStore.IE_DELETED) {
                        def list = getIssueEntitlements(targetSub).findAll{it.tipp.id == ieToTake.tipp.id && it.status != RDStore.IE_DELETED}
//                        def list = targetSub.issueEntitlements.findAll {it.tipp.id == ieToTake.tipp.id} && it.status != RDStore.IE_DELETED}
                        if (list?.size() > 0) {
                            // mich gibts schon! Fehlermeldung ausgeben!
                            flash.error = ieToTake.tipp.title.title + " wurde nicht hinzugefügt, weil es in der Ziellizenz schon existiert."
                        } else {
                            def properties = ieToTake.properties
                            properties.globalUID = null
                            IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                            InvokerHelper.setProperties(newIssueEntitlement, properties)
                            newIssueEntitlement.subscription = targetSub
                            newIssueEntitlement.save(flush: true)
                        }
                    }
                }
                break;
        }
    }

    boolean takeTasks(String aktion, Subscription sourceSub, def toCopyTasks, Subscription targetSub, def flash) {
        switch (aktion) {
            case COPY:
    //        if (!Task.findAllBySubscription(targetSub)) {
            //Copy Tasks
    //        params.list('subscription.takeTasks').each { tsk ->
            toCopyTasks.each { tsk ->

                def task = Task.findBySubscriptionAndId(sourceSub, tsk)
                if (task) {
                    if (task.status != RDStore.TASK_STATUS_DONE) {
                        Task newTask = new Task()
                        InvokerHelper.setProperties(newTask, task.properties)
                        newTask.subscription = targetSub
                        newTask.save(flush: true)
                    }

                }
            }
                break;
            case REPLACE:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
        //       }
    }

    boolean takeAnnouncements(String aktion, Subscription sourceSub, def toCopyAnnouncements, Subscription targetSub, def flash) {
        switch (aktion) {
            case COPY:
    //        def toCopyAnnouncements = []
    //        params.list('subscription.takeAnnouncements').each { announcement ->
    //            toCopyAnnouncements << Long.valueOf(announcement)
    //        }
            //        if (targetSub?.documents?.size() == 0) {//?
            sourceSub.documents?.each { dctx ->
                //Copy Announcements
                if (dctx.id in toCopyAnnouncements) {
                    if ((dctx.owner?.contentType == com.k_int.kbplus.Doc.CONTENT_TYPE_STRING) && !(dctx.domain) && (dctx.status?.value != 'Deleted')) {
                        Doc newDoc = new Doc()
                        InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                        newDoc.save(flush: true)
                        DocContext newDocContext = new DocContext()
                        InvokerHelper.setProperties(newDocContext, dctx.properties)
                        newDocContext.subscription = targetSub
                        newDocContext.owner = newDoc
                        newDocContext.save(flush: true)
                    }
                }
            }
                break
            case REPLACE:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
        //        }
    }

    boolean takeDates(String aktion, Subscription sourceSub, Subscription targetSub, def flash) {
        switch (aktion) {
            case REPLACE:
                targetSub.setStartDate(sourceSub.getStartDate())
                targetSub.setEndDate(sourceSub.getEndDate())
                if (targetSub.save(flush: true)) {
                    log.debug("Save ok")
                    return true
                } else {
                    log.error("Problem saving subscription ${targetSub.errors}")
                    flash.error("Es ist ein Fehler aufgetreten.")
                    return false
                }
                break;

            case COPY:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

    boolean takeDoks(String aktion, Subscription sourceSub, def toCopyDocs, Subscription targetSub, def flash) {
        switch (aktion) {
            case COPY:
    //        def toCopyDocs = []
    //        params.list('subscription.takeDocs').each { doc -> toCopyDocs << Long.valueOf(doc) }
    //
            //        if (targetSub?.documents?.size() == 0) {//?
            sourceSub.documents?.each { dctx ->
                //Copy Docs
                if (dctx.id in toCopyDocs) {
                    if (((dctx.owner?.contentType == 1) || (dctx.owner?.contentType == 3)) && (dctx.status?.value != 'Deleted')) {
                        Doc newDoc = new Doc()
                        InvokerHelper.setProperties(newDoc, dctx.owner.properties)
                        newDoc.save(flush: true)
                        DocContext newDocContext = new DocContext()
                        InvokerHelper.setProperties(newDocContext, dctx.properties)
                        newDocContext.subscription = targetSub
                        newDocContext.owner = newDoc
                        newDocContext.save(flush: true)
                    }
                }
            }
                break;
            case REPLACE:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
        //        }
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
                if (targetProp.save(flush: true)){
                    log.debug("Save ok")
                    return true
                } else {
                    log.error("Problem saving property ${targetProp.errors}")
                    flash.error += "Es ist ein Fehler beim Speichern von ${sourceProp.value} aufgetreten."
                    return false
                }
                //newSub.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
            }
                break;
            case REPLACE:
                throw new UnsupportedOperationException("Der Fall " + aktion + " ist nicht vorgesehen!")
        }
    }

}