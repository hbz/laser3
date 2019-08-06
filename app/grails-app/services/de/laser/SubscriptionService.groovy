package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.exceptions.EntitlementCreationException
import de.laser.helper.DebugAnnotation
import org.springframework.web.multipart.commons.CommonsMultipartFile
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import org.codehaus.groovy.runtime.InvokerHelper

import static de.laser.helper.RDStore.*

class SubscriptionService {
    def contextService
    def accessService
    def subscriptionsQueryService
    def docstoreService
    def messageSource
    def escapeService
    def refdataService
    def locale

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
    }

    List getMySubscriptions_readRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
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
        result
    }

    List getMySubscriptions_writeRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
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
        result
    }

    //Konsortiallizenzen
    private List getSubscriptionsConsortiaQuery() {
        Map params = [:]
//        params.status = SUBSCRIPTION_CURRENT.id
        params.showParentsAndChildsSubs = false
//        params.showParentsAndChildsSubs = 'true'
        params.orgRole = OR_SUBSCRIPTION_CONSORTIA.value
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    //Teilnehmerlizenzen
    private List getSubscriptionsConsortialLicenseQuery() {
        Map params = [:]
//        params.status = SUBSCRIPTION_CURRENT.id
        params.orgRole = OR_SUBSCRIBER.value
        params.subTypes = SUBSCRIPTION_TYPE_CONSORTIAL.id
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    //Lokallizenzen
    private List getSubscriptionsLocalLicenseQuery() {
        Map params = [:]
//        params.status = SUBSCRIPTION_CURRENT.id
        params.orgRole = OR_SUBSCRIBER.value
        params.subTypes = SUBSCRIPTION_TYPE_LOCAL.id
        subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
    }

    List getValidSubChilds(Subscription subscription) {
        def validSubChilds = Subscription.findAllByInstanceOfAndStatusNotEqual(
                subscription,
                SUBSCRIPTION_DELETED
        )
        validSubChilds = validSubChilds?.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa?.sortname ?: sa?.name ?: "")?.compareTo((sb?.sortname ?: sb?.name ?: ""))
        }
        validSubChilds
    }

    List getIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status <> :del",
                        [sub: subscription, del: TIPP_STATUS_DELETED])
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deleteOwner(Subscription targetSub, def flash) {
        targetSub.owner = null
        return save(targetSub, flash)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyOwner(Subscription sourceSub, Subscription targetSub, def flash) {
        //Vertrag/License
        targetSub.owner = sourceSub.owner ?: null
        return save(targetSub, flash)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Subscription targetSub, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetSub, roleTypes: [OR_SUBSCRIPTION_CONSORTIA, OR_SUBSCRIBER_CONS, OR_SUBSCRIBER]]
        )
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyOrgRelations(List<OrgRole> toCopyOrgRelations, Subscription sourceSub, Subscription targetSub, def flash) {
        sourceSub.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
                if (targetSub.orgRelations?.find { it.roleTypeId == or.roleTypeId && it.orgId == or.orgId }) {
                    Object[] args = [or?.roleType?.getI10n("value") + " " + or?.org?.name]
                    flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deletePackages(List<SubscriptionPackage> packagesToDelete, Subscription targetSub, def flash) {
        //alle IEs löschen, die zu den zu löschenden Packages gehören
//        targetSub.issueEntitlements.each{ ie ->
        getIssueEntitlements(targetSub).each{ ie ->
            if (packagesToDelete.find { subPkg -> subPkg?.pkg?.id == ie?.tipp?.pkg?.id } ) {
                ie.status = TIPP_STATUS_DELETED
                save(ie, flash)
            }
        }

        //alle zugeordneten Packages löschen
        if (packagesToDelete) {
            SubscriptionPackage.executeUpdate(
                    "delete from SubscriptionPackage sp where sp in (:packagesToDelete) and sp.subscription = :sub ",
                    [packagesToDelete: packagesToDelete, sub: targetSub])
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyPackages(List<Package> packagesToTake, Subscription targetSub, def flash) {
        packagesToTake?.each { pkg ->
            if (targetSub.packages?.find { it.pkg?.id == pkg?.id }) {
                Object[] args = [pkg.name]
                flash.error += messageSource.getMessage('subscription.err.packageAlreadyExistsInTargetSub', args, locale)
            } else {
                SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                newSubscriptionPackage.subscription = targetSub
                newSubscriptionPackage.pkg = pkg
                save(newSubscriptionPackage, flash)
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deleteEntitlements(List<IssueEntitlement> entitlementsToDelete, Subscription targetSub, def flash) {
        entitlementsToDelete.each {
            it.status = TIPP_STATUS_DELETED
            save(it, flash)
        }
//        IssueEntitlement.executeUpdate(
//                "delete from IssueEntitlement ie where ie in (:entitlementsToDelete) and ie.subscription = :sub ",
//                [entitlementsToDelete: entitlementsToDelete, sub: targetSub])
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyEntitlements(List<IssueEntitlement> entitlementsToTake, Subscription targetSub, def flash) {
        entitlementsToTake.each { ieToTake ->
            if (ieToTake.status != TIPP_STATUS_DELETED) {
                def list = getIssueEntitlements(targetSub).findAll{it.tipp.id == ieToTake.tipp.id && it.status != TIPP_STATUS_DELETED}
                if (list?.size() > 0) {
                    // mich gibts schon! Fehlermeldung ausgeben!
                    Object[] args = [ieToTake.tipp.title.title]
                    flash.error += messageSource.getMessage('subscription.err.titleAlreadyExistsInTargetSub', args, locale)
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
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    void copySubscriber(List<Subscription> subscriptionToTake, Subscription targetSub, def flash) {
        subscriptionToTake.each { subMember ->
            //Gibt es mich schon in der Ziellizenz?
            def found = null
            getValidSubChilds(targetSub).each{
                it.getAllSubscribers().each {ts ->
                    subMember.getAllSubscribers().each { subM ->
                        if (subM.id == ts.id){
                            found = ts
                        }
                    }
                }
            }

            if (found) {
                // mich gibts schon! Fehlermeldung ausgeben!
                Object[] args = [found.sortname ?: found.sortname]
                flash.error += messageSource.getMessage('subscription.err.subscriberAlreadyExistsInTargetSub', args, locale)
//                diffs.add(message(code:'pendingChange.message_CI01',args:[costTitle,g.createLink(mapping:'subfinance',controller:'subscription',action:'index',params:[sub:cci.sub.id]),cci.sub.name,cci.costInBillingCurrency,newCostItem
            } else {
                //ChildSub Exist
//                ArrayList<Links> prevLinks = Links.findAllByDestinationAndLinkTypeAndObjectType(subMember.id, LINKTYPE_FOLLOWS, Subscription.class.name)
//                if (prevLinks.size() == 0) {

                    /* Subscription.executeQuery("select s from Subscription as s join s.orgRelations as sor where s.instanceOf = ? and sor.org.id = ?",
                            [result.subscriptionInstance, it.id])*/

                    def newSubscription = new Subscription(
                            type: subMember.type,
                            status: targetSub.status,
                            name: subMember.name,
                            startDate: targetSub.startDate,
                            endDate: targetSub.endDate,
                            manualRenewalDate: subMember.manualRenewalDate,
                            /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                            identifier: java.util.UUID.randomUUID().toString(),
                            instanceOf: targetSub?.id,
                            //previousSubscription: subMember?.id,
                            isSlaved: subMember.isSlaved,
                            isPublic: subMember.isPublic,
                            impId: java.util.UUID.randomUUID().toString(),
                            owner: targetSub.owner?.id ? subMember.owner?.id : null,
                            resource: targetSub.resource ?: null,
                            form: targetSub.form ?: null
                    )
                    newSubscription.save(flush: true)
                    //ERMS-892: insert preceding relation in new data model
                    if (subMember) {
                        Links prevLink = new Links(source: newSubscription.id, destination: subMember.id, linkType: LINKTYPE_FOLLOWS, objectType: Subscription.class.name, owner: contextService.org)
                        if (!prevLink.save()) {
                            log.error("Subscription linking failed, please check: ${prevLink.errors}")
                        }
                    }

                    if (subMember.customProperties) {
                        //customProperties
                        for (prop in subMember.customProperties) {
                            def copiedProp = new SubscriptionCustomProperty(type: prop.type, owner: newSubscription)
                            copiedProp = prop.copyInto(copiedProp)
                            copiedProp.save(flush: true)
                            //newSubscription.addToCustomProperties(copiedProp) // ERROR Hibernate: Found two representations of same collection
                        }
                    }
                    if (subMember.privateProperties) {
                        //privatProperties
                        List tenantOrgs = OrgRole.executeQuery('select o.org from OrgRole as o where o.sub = :sub and o.roleType in (:roleType)', [sub: subMember, roleType: [OR_SUBSCRIBER_CONS, OR_SUBSCRIPTION_CONSORTIA]]).collect {
                            it -> it.id
                        }
                        subMember.privateProperties?.each { prop ->
                            if (tenantOrgs.indexOf(prop.type?.tenant?.id) > -1) {
                                def copiedProp = new SubscriptionPrivateProperty(type: prop.type, owner: newSubscription)
                                copiedProp = prop.copyInto(copiedProp)
                                copiedProp.save(flush: true)
                                //newSubscription.addToPrivateProperties(copiedProp)  // ERROR Hibernate: Found two representations of same collection
                            }
                        }
                    }

                    if (subMember.packages && targetSub.packages) {
                        //Package
                        subMember.packages?.each { pkg ->
                            SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                            InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                            newSubscriptionPackage.subscription = newSubscription
                            newSubscriptionPackage.save(flush: true)
                        }
                    }
                    if (subMember.issueEntitlements && targetSub.issueEntitlements) {
                        subMember.issueEntitlements?.each { ie ->
                            if (ie.status != RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')) {
                                def ieProperties = ie.properties
                                ieProperties.globalUID = null

                                IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                                newIssueEntitlement.subscription = newSubscription
                                newIssueEntitlement.save(flush: true)
                            }
                        }
                    }

                    //OrgRole
                    subMember.orgRelations?.each { or ->
                        if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType.value in ['Subscriber', 'Subscriber_Consortial']) || (targetSub.orgRelations.size() >= 1)) {
                            OrgRole newOrgRole = new OrgRole()
                            InvokerHelper.setProperties(newOrgRole, or.properties)
                            newOrgRole.sub = newSubscription
                            newOrgRole.save(flush: true)
                        }
                    }

                    if (subMember.prsLinks && targetSub.prsLinks) {
                        //PersonRole
                        subMember.prsLinks?.each { prsLink ->
                            PersonRole newPersonRole = new PersonRole()
                            InvokerHelper.setProperties(newPersonRole, prsLink.properties)
                            newPersonRole.sub = newSubscription
                            newPersonRole.save(flush: true)
                        }
                    }
//                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deleteTasks(List<Long> toDeleteTasks, Subscription targetSub, def flash) {
        boolean isInstAdm = contextService.getUser().hasAffiliation("INST_ADM")
        def userId = contextService.user.id
        toDeleteTasks.each { deleteTaskId ->
            def dTask = Task.get(deleteTaskId)
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyTasks(Subscription sourceSub, def toCopyTasks, Subscription targetSub, def flash) {
        toCopyTasks.each { tsk ->
            def task = Task.findBySubscriptionAndId(sourceSub, tsk)
            if (task) {
                if (task.status != TASK_STATUS_DONE) {
                    Task newTask = new Task()
                    InvokerHelper.setProperties(newTask, task.properties)
                    newTask.subscription = targetSub
                    save(newTask, flash)
                }
            }
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
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

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteAnnouncements(List<Long> toDeleteAnnouncements, Subscription targetSub, def flash) {
        targetSub.documents.each {
            if (toDeleteAnnouncements.contains(it.id) && it.owner?.contentType == Doc.CONTENT_TYPE_STRING  && !(it.domain)){
                Map params = [deleteId: it.id]
                log.debug("deleteDocuments ${params}");
                docstoreService.unifiedDeleteDocuments(params)
            }
        }
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deleteDates(Subscription targetSub, def flash){
        targetSub.startDate = null
        targetSub.endDate = null
        return save(targetSub, flash)
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyDates(Subscription sourceSub, Subscription targetSub, def flash) {
        targetSub.setStartDate(sourceSub.getStartDate())
        targetSub.setEndDate(sourceSub.getEndDate())
        return save(targetSub, flash)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteDocs(List<Long> toDeleteDocs, Subscription targetSub, def flash) {
        log.debug("toDeleteDocCtxIds: " + toDeleteDocs)
        def updated = DocContext.executeUpdate("UPDATE DocContext set status = :del where id in (:ids)",
        [del: DOC_DELETED, ids: toDeleteDocs])
        log.debug("Number of deleted (per Flag) DocCtxs: " + updated)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyDocs(Subscription sourceSub, def toCopyDocs, Subscription targetSub, def flash) {
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
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean copyProperties(List<AbstractProperty> properties, Subscription targetSub, def flash){
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
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    boolean deleteProperties(List<AbstractProperty> properties, Subscription targetSub, def flash){
        int anzCP = SubscriptionCustomProperty.executeUpdate("delete from SubscriptionCustomProperty p where p in (:properties)",[properties: properties])
        int anzPP = SubscriptionPrivateProperty.executeUpdate("delete from SubscriptionPrivateProperty p where p in (:properties)",[properties: properties])
    }

    private boolean delete(obj, flash) {
        if (obj) {
            obj.delete(flush: true)
            log.debug("Delete ${obj} ok")
        } else {
            flash.error += messageSource.getMessage('default.delete.error.message', null, locale)
        }
    }

    private boolean save(obj, flash){
        if (obj.save(flush: true)){
            log.debug("Save ${obj} ok")
            return true
        } else {
            log.error("Problem saving ${obj.errors}")
            Object[] args = [obj]
            flash.error += messageSource.getMessage('default.save.error.message', args, locale)
            return false
        }
    }

    Map regroupSubscriptionProperties(List<Subscription> subsToCompare) {
        LinkedHashMap result = [groupedProperties:[:],orphanedProperties:[:],privateProperties:[:]]
        subsToCompare.each{ sub ->
            Map allPropDefGroups = sub.getCalculatedPropDefGroups(org)
            allPropDefGroups.entrySet().each { propDefGroupWrapper ->
                //group group level
                //There are: global, local, member (consortium@subscriber) property *groups* and orphaned *properties* which is ONE group
                String wrapperKey = propDefGroupWrapper.getKey()
                if(wrapperKey.equals("orphanedProperties")) {
                    TreeMap orphanedProperties = result.orphanedProperties
                    orphanedProperties = comparisonService.buildComparisonTree(orphanedProperties,sub,propDefGroupWrapper.getValue())
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
                                    groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,null,sub))
                                break
                            case "local":
                                try {
                                    groupKey = (PropertyDefinitionGroup) propDefGroup.get(0)
                                    groupBinding = (PropertyDefinitionGroupBinding) propDefGroup.get(1)
                                    if(groupBinding.isVisible) {
                                        groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,groupBinding,sub))
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
                                        groupedProperties.put(groupKey,comparisonService.getGroupedPropertyTrees(groupedProperties,groupKey,groupBinding,sub))
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
            privateProperties = comparisonService.buildComparisonTree(privateProperties,sub,sub.privateProperties)
            result.privateProperties = privateProperties
        }
        result
    }

    boolean addEntitlement(sub, gokbId) throws EntitlementCreationException {
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(gokbId)
        if (tipp == null) {
            throw new EntitlementCreationException("Unable to tipp ${gokbId}")
            return false
        } else {
            def new_ie = new IssueEntitlement(status: TIPP_STATUS_CURRENT,
                    subscription: sub,
                    tipp: tipp,
                    accessStartDate: tipp.accessStartDate,
                    accessEndDate: tipp.accessEndDate,
                    startDate: tipp.startDate,
                    startVolume: tipp.startVolume,
                    startIssue: tipp.startIssue,
                    endDate: tipp.endDate,
                    endVolume: tipp.endVolume,
                    endIssue: tipp.endIssue,
                    embargo: tipp.embargo,
                    coverageDepth: tipp.coverageDepth,
                    coverageNote: tipp.coverageNote,
                    ieReason: 'Manually Added by User')
            if (new_ie.save()) {
                return true
            } else {
                throw new EntitlementCreationException(new_ie.errors)
                return false
            }
        }
    }

    Map subscriptionImport(CommonsMultipartFile tsvFile) {
        Org contextOrg = contextService.org
        RefdataValue comboType
        String[] parentSubType
        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            comboType = COMBO_TYPE_CONSORTIUM
            parentSubType = [SUBSCRIPTION_TYPE_CONSORTIAL.getI10n('value')]
        }
        else if(accessService.checkPerm("ORG_INST_COLLECTIVE")) {
            comboType = COMBO_TYPE_DEPARTMENT
            parentSubType = [SUBSCRIPTION_TYPE_COLLECTIVE.getI10n('value')]
        }
        Map colMap = [:]
        Map<String, Map<String, Integer>> propMap = [:]
        Map candidates = [:]
        InputStream fileContent = tsvFile.getInputStream()
        List<String> rows = fileContent.text.split('\n')
        List<String> ignoredColHeads = [], multiplePropDefs = []
        rows[0].split('\t').eachWithIndex { String s, int c ->
            String headerCol = s.trim()
            //important: when processing column headers, grab those which are reserved; default case: check if it is a name of a property definition; if there is no result as well, reject.
            switch(headerCol.toLowerCase()) {
                case "name": colMap.name = c
                    break
                case "member":
                case "teilnehmer": colMap.member = c
                    break
                case "vertrag":
                case "license": colMap.owner = c
                    break
                case "elternlizenz":
                case "konsortiallizenz":
                case "kollektivlizenz":
                case "parent subscription":
                case "consortial subscription":
                case "collective subscription": colMap.instanceOf = c
                    break
                case "status": colMap.status = c
                    break
                case "startdatum":
                case "start date": colMap.startDate = c
                    break
                case "enddatum":
                case "end date": colMap.endDate = c
                    break
                case "kündigungsdatum":
                case "cancellation date":
                case "manual cancellation date": colMap.manualCancellationDate = c
                    break
                case "lizenztyp":
                case "subscription type":
                case "type": colMap.type = c
                    break
                case "lizenzform":
                case "subscription form":
                case "form": colMap.form = c
                    break
                case "ressourcentyp":
                case "subscription resource":
                case "resource": colMap.resource = c
                    break
                default:
                    //check if property definition
                    Map queryParams = [propDef:"${headerCol.toLowerCase()}",pdClass:PropertyDefinition.class.name,contextOrg:contextOrg]
                    List<PropertyDefinition> posiblePropDefs = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd, I10nTranslation i where i.referenceId = pd.id and i.referenceClass = :pdClass and (lower(i.valueDe) = :propDef or lower(i.valueEn) = :propDef) and (pd.tenant = :contextOrg or pd.tenant = null)",queryParams)
                    if(posiblePropDefs.size() == 1) {
                        PropertyDefinition propDef = posiblePropDefs[0]
                        String refCategory = ""
                        if(propDef.type == RefdataValue.toString()) {
                            refCategory = propDef.refdataCategory
                        }
                        Map<String,Integer> defPair = [colno:c,refCategory:refCategory]
                        propMap[propDef.class.name+':'+propDef.id] = defPair
                    }
                    else if(posiblePropDefs.size() > 1)
                        multiplePropDefs << headerCol
                    else
                        ignoredColHeads << headerCol
                    break
            }
        }
        Set<String> globalErrors = []
        if(ignoredColHeads)
            globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.colHeaderIgnored',[ignoredColHeads.join('</li><li>')].toArray(),locale)
        if(multiplePropDefs)
            globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.multiplePropDefs',[multiplePropDefs.join('</li><li>')].toArray(),locale)
        rows.remove(0)
        rows.each { row ->
            Map mappingErrorBag = [:], candidate = [properties: [:]]
            List<String> cols = row.split('\t')
            //check if we have some mandatory properties ...
            //status(nullable:false, blank:false) -> to status, defaults to status not set
            if(colMap.status != null) {
                String statusKey = cols[colMap.status]
                if(statusKey) {
                    String status = refdataService.retrieveRefdataValueOID(statusKey,'Subscription Status')
                    if(status) {
                        candidate.status = status
                    }
                    else {
                        //missing case one: invalid status key
                        //default to subscription not set
                        candidate.status = "${SUBSCRIPTION_NO_STATUS.class.name}:${SUBSCRIPTION_NO_STATUS.id}"
                        mappingErrorBag.noValidStatus = statusKey
                    }
                }
                else {
                    //missing case two: no status key set
                    //default to subscription not set
                    candidate.status = "${SUBSCRIPTION_NO_STATUS.class.name}:${SUBSCRIPTION_NO_STATUS.id}"
                    mappingErrorBag.statusNotSet = true
                }
            }
            else {
                //missing case three: the entire column is missing
                //default to subscription not set
                candidate.status = "${SUBSCRIPTION_NO_STATUS.class.name}:${SUBSCRIPTION_NO_STATUS.id}"
                mappingErrorBag.statusNotSet = true
            }
            //moving on to optional attributes
            //name(nullable:true, blank:false) -> to name
            if(colMap.name != null) {
                String name = cols[colMap.name]
                if(name)
                    candidate.name = name
            }
            //owner(nullable:true, blank:false) -> to license
            if(colMap.owner != null) {
                String ownerKey = cols[colMap.owner]
                if(ownerKey) {
                    List<License> licCandidates = License.executeQuery("select oo.lic from OrgRole oo join oo.lic l where :idCandidate in (cast(l.id as string),l.globalUID) and oo.roleType in :roleTypes and oo.org = :contextOrg",[idCandidate:ownerKey,roleTypes:[OR_LICENSEE_CONS,OR_LICENSING_CONSORTIUM,OR_LICENSEE],contextOrg:contextOrg])
                    if(licCandidates.size() == 1) {
                        License owner = licCandidates[0]
                        candidate.owner = "${owner.class.name}:${owner.id}"
                    }
                    else if(licCandidates.size() > 1)
                        mappingErrorBag.multipleLicenseError = ownerKey
                    else
                        mappingErrorBag.noValidLicense = ownerKey
                }
            }
            //type(nullable:true, blank:false) -> to type
            if(colMap.type != null) {
                String typeKey = cols[colMap.type]
                if(typeKey) {
                    String type = refdataService.retrieveRefdataValueOID(typeKey,'Subscription Type')
                    if(type) {
                        candidate.type = type
                    }
                    else {
                        mappingErrorBag.noValidType = typeKey
                    }
                }
            }
            //form(nullable:true, blank:false) -> to form
            if(colMap.form != null) {
                String formKey = cols[colMap.form]
                if(formKey) {
                    String form = refdataService.retrieveRefdataValueOID(formKey,'Subscription Form')
                    if(form) {
                        candidate.form = form
                    }
                    else {
                        mappingErrorBag.noValidForm = formKey
                    }
                }
            }
            //resource(nullable:true, blank:false) -> to resource
            if(colMap.resource != null) {
                String resourceKey = cols[colMap.resource]
                if(resourceKey) {
                    String resource = refdataService.retrieveRefdataValueOID(resourceKey,'Subscription Resource')
                    if(resource) {
                        candidate.resource = resource
                    }
                    else {
                        mappingErrorBag.noValidResource = resourceKey
                    }
                }
            }
            /*
            startDate(nullable:true, blank:false, validator: { val, obj ->
                if(obj.startDate != null && obj.endDate != null) {
                    if(obj.startDate > obj.endDate) return ['startDateAfterEndDate']
                }
            }) -> to startDate
            */
            Date startDate
            if(colMap.startDate != null) {
                startDate = escapeService.parseDate(cols[colMap.startDate])
            }
            /*
            endDate(nullable:true, blank:false, validator: { val, obj ->
                if(obj.startDate != null && obj.endDate != null) {
                    if(obj.startDate > obj.endDate) return ['endDateBeforeStartDate']
                }
            }) -> to endDate
            */
            Date endDate
            if(colMap.endDate != null) {
                endDate = escapeService.parseDate(cols[colMap.endDate])
            }
            if(startDate && endDate) {
                if(startDate <= endDate) {
                    candidate.startDate = startDate
                    candidate.endDate = endDate
                }
                else {
                    mappingErrorBag.startDateBeforeEndDate = true
                }
            }
            else if(startDate && !endDate)
                candidate.startDate = startDate
            else if(!startDate && endDate)
                candidate.endDate = endDate
            //manualCancellationDate(nullable:true, blank:false)
            if(colMap.manualCancellationDate != null) {
                Date manualCancellationDate = escapeService.parseDate(cols[colMap.manualCancellationDate])
                if(manualCancellationDate)
                    candidate.manualCancellationDate = manualCancellationDate
            }
            //instanceOf(nullable:true, blank:false)
            if(colMap.instanceOf != null && colMap.member != null) {
                String idCandidate = cols[colMap.instanceOf]
                String memberIdCandidate = cols[colMap.member]
                if(idCandidate && memberIdCandidate) {
                    List<Subscription> parentSubs = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :contextOrg and oo.roleType in :roleTypes and :idCandidate in (cast(oo.sub.id as string),oo.sub.globalUID)",[contextOrg: contextOrg, roleTypes: [OR_SUBSCRIPTION_CONSORTIA, OR_SUBSCRIPTION_COLLECTIVE], idCandidate: idCandidate])
                    List<Org> possibleOrgs = Org.executeQuery("select distinct idOcc.org from IdentifierOccurrence idOcc, Combo c join idOcc.identifier id where c.fromOrg = idOcc.org and :idCandidate in (cast(idOcc.org.id as string),idOcc.org.globalUID) or (id.value = :idCandidate and id.ns = :wibid) and c.toOrg = :contextOrg and c.type = :type",[idCandidate:memberIdCandidate,wibid:IdentifierNamespace.findByNs('wibid'),contextOrg: contextOrg,type: comboType])
                    if(parentSubs.size() == 1) {
                        Subscription instanceOf = parentSubs[0]
                        candidate.instanceOf = "${instanceOf.class.name}:${instanceOf.id}"
                        if(!candidate.name)
                            candidate.name = instanceOf.name
                    }
                    else {
                        mappingErrorBag.noValidSubscription = idCandidate
                    }
                    if(possibleOrgs.size() == 1) {
                        //further check needed: is the subscriber linked per combo to the organisation?
                        Org member = possibleOrgs[0]
                        candidate.member = "${member.class.name}:${member.id}"
                    }
                    else if(possibleOrgs.size() > 1) {
                        mappingErrorBag.multipleOrgsError = possibleOrgs.collect { org -> org.sortname ?: org.name }
                    }
                    else {
                        mappingErrorBag.noValidOrg = memberIdCandidate
                    }
                }
                else {
                    if(!idCandidate && memberIdCandidate)
                        mappingErrorBag.instanceOfWithoutMember = true
                    if(idCandidate && !memberIdCandidate)
                        mappingErrorBag.memberWithoutInstanceOf = true
                }
            }
            else {
                if(colMap.instanceOf == null && colMap.member != null)
                    globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.memberWithoutInstanceOf',null,locale)
                if(colMap.instanceOf != null && colMap.member == null)
                    globalErrors << messageSource.getMessage('myinst.subscriptionImport.post.globalErrors.instanceOfWithoutMember',null,locale)
            }
            //properties -> propMap
            propMap.each { String k, Map defPair ->
                if(cols[defPair.colno].trim()) {
                    def v
                    if(defPair.refCategory) {
                        v = refdataService.retrieveRefdataValueOID(cols[defPair.colno].trim(),defPair.refCategory)
                        if(!v) {
                            mappingErrorBag.propValNotInRefdataValueSet = [cols[defPair.colno].trim(),defPair.refCategory]
                        }
                    }
                    else v = cols[defPair.colno]
                    candidate.properties[k] = v
                }
            }
            candidates.put(candidate,mappingErrorBag)
        }
        [candidates: candidates, globalErrors: globalErrors, parentSubType: parentSubType]
    }

}