package de.laser

import com.k_int.kbplus.*
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import com.k_int.properties.PropertyDefinition
import de.laser.helper.ConfigUtils
import de.laser.helper.FactoryResult
import de.laser.helper.RDStore
import de.laser.interfaces.ShareSupport
import grails.transaction.Transactional
import org.codehaus.groovy.runtime.InvokerHelper

import java.nio.file.Files
import java.nio.file.Path

@Transactional
class CopyElementsService {


    GenericOIDService genericOIDService
    ComparisonService comparisonService
    TaskService taskService

    Map copySubElements_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        //boolean isTargetSubChanged = false
        if (params.subscription?.deleteDates && isBothSubscriptionsSet(baseSub, newSub)) {
            deleteDates(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takeDates && isBothSubscriptionsSet(baseSub, newSub)) {
            copyDates(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleShareStartDate)
            AuditConfig.addConfig(newSub,'startDate')
        else if(params.toggleShareStartDate == false && AuditConfig.getConfig(newSub, 'startDate'))
            AuditConfig.removeConfig(newSub, 'startDate')
        if(params.toggleShareEndDate)
            AuditConfig.addConfig(newSub,'endDate')
        else if(params.toggleShareEndDate == false && AuditConfig.getConfig(newSub, 'endDate'))
            AuditConfig.removeConfig(newSub, 'endDate')

        if (params.subscription?.deleteStatus && isBothSubscriptionsSet(baseSub, newSub)) {
            deleteStatus(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takeStatus && isBothSubscriptionsSet(baseSub, newSub)) {
            copyStatus(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleShareStatus)
            AuditConfig.addConfig(newSub,'status')
        else if(!params.toggleShareStatus && AuditConfig.getConfig(newSub, 'status'))
            AuditConfig.removeConfig(newSub, 'status')

        if (params.subscription?.deleteKind && isBothSubscriptionsSet(baseSub, newSub)) {
            deleteKind(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takeKind && isBothSubscriptionsSet(baseSub, newSub)) {
            copyKind(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleShareKind)
            AuditConfig.addConfig(newSub,'kind')
        else if(!params.toggleShareKind && AuditConfig.getConfig(newSub, 'kind'))
            AuditConfig.removeConfig(newSub, 'kind')

        if (params.subscription?.deleteForm && isBothSubscriptionsSet(baseSub, newSub)) {
            deleteForm(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takeForm && isBothSubscriptionsSet(baseSub, newSub)) {
            copyForm(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleShareForm)
            AuditConfig.addConfig(newSub,'form')
        else if(!params.toggleShareForm && AuditConfig.getConfig(newSub, 'form'))
            AuditConfig.removeConfig(newSub, 'form')

        if (params.subscription?.deleteResource && isBothSubscriptionsSet(baseSub, newSub)) {
            deleteResource(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takeResource && isBothSubscriptionsSet(baseSub, newSub)) {
            copyResource(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleShareResource)
            AuditConfig.addConfig(newSub,'resource')
        else if(!params.toggleShareResource && AuditConfig.getConfig(newSub, 'resource'))
            AuditConfig.removeConfig(newSub, 'resource')

        if (params.subscription?.deletePublicForApi && isBothSubscriptionsSet(baseSub, newSub)) {
            deletePublicForApi(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takePublicForApi && isBothSubscriptionsSet(baseSub, newSub)) {
            copyPublicForApi(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleSharePublicForApi)
            AuditConfig.addConfig(newSub,'isPublicForApi')
        else if(!params.toggleSharePublicForApi && AuditConfig.getConfig(newSub, 'isPublicForApi'))
            AuditConfig.removeConfig(newSub, 'isPublicForApi')

        if (params.subscription?.deletePerpetualAccess && isBothSubscriptionsSet(baseSub, newSub)) {
            deletePerpetualAccess(newSub, flash)
            //isTargetSubChanged = true
        }else if (params.subscription?.takePerpetualAccess && isBothSubscriptionsSet(baseSub, newSub)) {
            copyPerpetualAccess(baseSub, newSub, flash)
            //isTargetSubChanged = true
        }
        if(params.toggleSharePerpetualAccess)
            AuditConfig.addConfig(newSub,'hasPerpetualAccess')
        else if(!params.toggleSharePerpetualAccess && AuditConfig.getConfig(newSub, 'hasPerpetualAccess'))
            AuditConfig.removeConfig(newSub, 'hasPerpetualAccess')

        if (params.subscription?.deleteLicenses && isBothSubscriptionsSet(baseSub, newSub)) {
            List<License> toDeleteLicenses = params.list('subscription.deleteLicenses').collect { genericOIDService.resolveOID(it) }
            deleteLicenses(toDeleteLicenses, newSub, flash)
        }else if (params.subscription?.takeLicenses && isBothSubscriptionsSet(baseSub, newSub)) {
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
            newSub.orgRelations.each {newSubOrgRole ->

                if(newSubOrgRole.org in toggleShareOrgRoles.org) {
                    newSubOrgRole.isShared = true
                    newSubOrgRole.save(flush:true)
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
            def toDeleteIdentifiers =  []
            params.list('subscription.deleteIdentifierIds').each{ identifier -> toDeleteIdentifiers << Long.valueOf(identifier) }
            deleteIdentifiers(toDeleteIdentifiers, newSub, flash)
            //isTargetSubChanged = true
        }

        if (params.subscription?.takeIdentifierIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyIdentifiers =  []
            params.list('subscription.takeIdentifierIds').each{ identifier -> toCopyIdentifiers << Long.valueOf(identifier) }
            copyIdentifiers(baseSub, toCopyIdentifiers, newSub, flash)
            //isTargetSubChanged = true
        }

        /*if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }*/
        result.subscription = baseSub
        result.newSub = newSub
        result.targetSubscription = newSub
        result
    }
    Map loadDataFor_DatesOwnerRelations(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

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
        result.source_visibleOrgRelations = getVisibleOrgRelations(baseSub)
        result.target_visibleOrgRelations = getVisibleOrgRelations(newSub)
        result
    }

    Map copySubElements_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
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

        result.sourceSubscription = baseSub
        result.targetSubscription = newSub
        result
    }
    Map copySubElements_Identifiers(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
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
        result.sourceSubscription = baseSub
        result.targetSubscription = newSub
        result
    }

    Map loadDataFor_DocsAnnouncementsTasks(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }

        result.sourceSubscription = baseSub
        result.targetSubscription = newSub
        result.sourceTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.sourceSubscription)
        result.targetTasks = taskService.getTasksByResponsiblesAndObject(result.user, contextService.org, result.targetSubscription)
        result
    }

    Map copySubElements_Subscriber(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }
        if (params.subscription?.copySubscriber && isBothSubscriptionsSet(baseSub, newSub)) {
            List<Subscription> toCopySubs = params.list('subscription.copySubscriber').collect { genericOIDService.resolveOID(it) }
            copySubscriber(toCopySubs, newSub, flash)
        }

        result.sourceSubscription = baseSub
        result.targetSubscription = newSub
        result
    }

    Map loadDataFor_Subscriber(Map params) {
        Map<String, Object> result = [:]
        result.sourceSubscription = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId): Long.parseLong(params.id))
        result.validSourceSubChilds = getValidSubChilds(result.sourceSubscription)
        if (params.targetSubscriptionId) {
            result.targetSubscription = Subscription.get(Long.parseLong(params.targetSubscriptionId))
            result.validTargetSubChilds = getValidSubChilds(result.targetSubscription)
        }
        result
    }


    Map copySubElements_Properties(Map params){
        LinkedHashMap result = [customProperties:[:],privateProperties:[:]]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        boolean isRenewSub = params.isRenewSub ? true : false

        Subscription newSub = null
        List auditProperties = params.list('auditProperties')
        List<Subscription> subsToCompare = [baseSub]
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(params.targetSubscriptionId)
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

    Map loadDataFor_Properties(Map params){
        LinkedHashMap result = [customProperties:[:],privateProperties:[:]]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = null
        List<Subscription> subsToCompare = [baseSub]
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(params.targetSubscriptionId)
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

    Map copySubElements_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

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

    Map loadDataFor_PackagesEntitlements(Map params) {
        Map<String, Object> result = [:]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        result.sourceIEs = getIssueEntitlements(baseSub)
        result.targetIEs = getIssueEntitlements(newSub)
        result.newSub = newSub
        result.subscription = baseSub
        result
    }

    boolean isBothSubscriptionsSet(Subscription baseSub, Subscription newSub) {
        if (! baseSub || !newSub) {
            if (!baseSub) flash.error += message(code: 'subscription.details.copyElementsIntoSubscription.noSubscriptionSource') + '<br />'
            if (!newSub)  flash.error += message(code: 'subscription.details.copyElementsIntoSubscription.noSubscriptionTarget') + '<br />'
            return false
        }
        return true
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

    private boolean delete(obj, flash) {
        if (obj) {
            obj.delete(flush: true)
            log.debug("Delete ${obj} ok")
        } else {
            flash.error += messageSource.getMessage('default.delete.error.general.message', null, locale)
        }
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


    boolean deleteLicenses(List<License> toDeleteLicenses, Subscription targetSub, def flash) {
        toDeleteLicenses.each { License lic ->
            setOrgLicRole(targetSub,lic,true)
        }
    }


    boolean copyLicenses(List<License> toCopyLicenses, Subscription targetSub, def flash) {
        toCopyLicenses.each { License lic ->
            setOrgLicRole(targetSub,lic,false)
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
        getIssueEntitlements(targetSub).each{ ie ->
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
                def list = getIssueEntitlements(targetSub).findAll{it.tipp.id == ieToTake.tipp.id && it.status != RDStore.TIPP_STATUS_DELETED}
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
}
