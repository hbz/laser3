package com.k_int.kbplus

import de.laser.AuditService
import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.Package
import de.laser.PendingChange
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractCoverage
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.exceptions.ChangeAcceptException
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.properties.PropertyDefinition
import de.laser.AuditConfig
import de.laser.SubscriptionService
import de.laser.IssueEntitlementCoverage
import de.laser.PendingChangeConfiguration
import de.laser.TIPPCoverage
import de.laser.helper.AppUtils
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.core.GrailsClass
import grails.web.databinding.DataBindingUtils
import net.sf.json.JSONObject
import org.grails.web.json.JSONElement
import grails.web.mapping.LinkGenerator
import org.hibernate.Session
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.transaction.TransactionStatus

import java.text.SimpleDateFormat
import java.time.Duration

@Transactional
class PendingChangeService extends AbstractLockableService {

    def genericOIDService
    SubscriptionService subscriptionService
    AuditService auditService
    LinkGenerator grailsLinkGenerator
    def messageSource


    final static EVENT_OBJECT_NEW = 'New Object'
    final static EVENT_OBJECT_UPDATE = 'Update Object'

    final static EVENT_TIPP_ADD = 'TIPPAdd'
    final static EVENT_TIPP_EDIT = 'TIPPEdit'
    final static EVENT_TIPP_DELETE = 'TIPPDeleted'

    final static EVENT_COVERAGE_ADD = 'CoverageAdd'
    final static EVENT_COVERAGE_UPDATE = 'CoverageUpdate'
    final static EVENT_COVERAGE_DELETE = 'CoverageDeleted'

    final static EVENT_PROPERTY_CHANGE = 'PropertyChange'

    /*boolean performMultipleAcceptsForJob(List<PendingChange> subscriptionChanges, List<PendingChange> licenseChanges) {
        log.debug('performMultipleAcceptsFromJob()')

        if (!running) {
            running = true

            subscriptionChanges.each {
                pendingChangeService.performAccept(it)
            }
            licenseChanges.each {
                pendingChangeService.performAccept(it)
            }

            running = false
            return true
        }
        else {
            return false
        }
    }*/

    @Deprecated
    boolean performAccept(PendingChange pendingChange) {

        log.debug('performAccept(): ' + pendingChange)
        boolean result = true

        //does not work for inheritance - ERMS-2335
        //PendingChange.withNewTransaction { TransactionStatus status ->
            boolean saveWithoutError = false

            try {
                JSONElement payload = JSON.parse(pendingChange.payload)
                log.debug("Process change ${payload}");
                switch ( payload.changeType ) {

                    case EVENT_TIPP_DELETE :
                        // "changeType":"TIPPDeleted","tippId":"${TitleInstancePackagePlatform.class.name}:6482"}
                        def sub_to_change = pendingChange.subscription
                        def tipp = genericOIDService.resolveOID(payload.tippId)
                        def ie_to_update = IssueEntitlement.findBySubscriptionAndTipp(sub_to_change,tipp)
                        if ( ie_to_update != null ) {
                            ie_to_update.status = RDStore.TIPP_STATUS_DELETED

                            if( ie_to_update.save()){
                                saveWithoutError = true
                            }
                        }
                        break
                    case EVENT_TIPP_ADD :
                        TitleInstancePackagePlatform underlyingTIPP = (TitleInstancePackagePlatform) genericOIDService.resolveOID(payload.changeDoc.OID)
                        Subscription subConcerned = pendingChange.subscription
                        subscriptionService.addEntitlement(subConcerned,underlyingTIPP.gokbId,null,false,RDStore.IE_ACCEPT_STATUS_FIXED)
                        saveWithoutError = true
                        break

                    case EVENT_PROPERTY_CHANGE :  // Generic property change
                        // TODO [ticket=1894]
                        // if ( ( payload.changeTarget != null ) && ( payload.changeTarget.length() > 0 ) ) {
                        if ( pendingChange.payloadChangeTargetOid?.length() > 0 || payload.changeTarget?.length() > 0) {
                            String targetOID = pendingChange.payloadChangeTargetOid ?: payload.changeTarget
                            //def target_object = genericOIDService.resolveOID(payload.changeTarget);
                            def target_object = genericOIDService.resolveOID(targetOID.replace('Custom','').replace('Private',''))
                            if ( target_object ) {
                                target_object.refresh()
                                // Work out if parsed_change_info.changeDoc.prop is an association - If so we will need to resolve the OID in the value
                                GrailsClass domain_class = AppUtils.getDomainClass( target_object.class.name )
                                def prop_info = domain_class.getPersistentProperty(payload.changeDoc.prop)
                                if(prop_info == null){
                                    log.debug("We are dealing with custom properties: ${payload}")
                                    //processCustomPropertyChange(payload)
                                    processCustomPropertyChange(pendingChange, payload) // TODO [ticket=1894]
                                }
                                else if ( prop_info.name == 'status' ) {
                                    RefdataValue oldStatus = (RefdataValue) genericOIDService.resolveOID(payload.changeDoc.old)
                                    RefdataValue newStatus = (RefdataValue) genericOIDService.resolveOID(payload.changeDoc.new)
                                    log.debug("Updating status from ${oldStatus.getI10n('value')} to ${newStatus.getI10n('value')}")
                                    target_object.status = newStatus
                                }
                                else if ( prop_info.isAssociation() ) {
                                    log.debug("Setting association for ${payload.changeDoc.prop} to ${payload.changeDoc.new}");
                                    target_object[payload.changeDoc.prop] = genericOIDService.resolveOID(payload.changeDoc.new)
                                }
                                else if ( prop_info.getType() == java.util.Date ) {
                                    log.debug("Date processing.... parse \"${payload.changeDoc.new}\"");
                                    if ( ( payload.changeDoc.new != null ) && ( payload.changeDoc.new.toString() != 'null' ) ) {
                                        //if ( ( parsed_change_info.changeDoc.new != null ) && ( parsed_change_info.changeDoc.new != 'null' ) ) {
                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // yyyy-MM-dd'T'HH:mm:ss.SSSZ 2013-08-31T23:00:00Z
                                        Date d = df.parse(payload.changeDoc.new)
                                        target_object[payload.changeDoc.prop] = d
                                    }
                                    else {
                                        target_object[payload.changeDoc.prop] = null
                                    }
                                }
                                else {
                                    log.debug("Setting value for ${payload.changeDoc.prop} to ${payload.changeDoc.new}");
                                    target_object[payload.changeDoc.prop] = payload.changeDoc.new
                                }

                                if(target_object.save()) {
                                    saveWithoutError = true
                                }

                                //FIXME: is this needed anywhere?
                                /*def change_audit_object = null
                                if ( change?.license ) change_audit_object = pendingChange?.license;
                                if ( change?.subscription ) change_audit_object = pendingChange?.subscription;
                                if ( change?.pkg ) change_audit_object = pendingChange?.pkg;
                                def change_audit_id = change_audit_object.id
                                def change_audit_class_name = change_audit_object.class.name*/
                            }
                        }
                        break

                    case EVENT_TIPP_EDIT :
                        // A tipp was edited, the user wants their change applied to the IE
                        break

                    case EVENT_OBJECT_NEW :
                        GrailsClass new_domain_class = AppUtils.getDomainClass( payload.newObjectClass )
                        if ( new_domain_class != null ) {
                            def new_instance = new_domain_class.getClazz().newInstance()
                            // like bindData(destination, map), that only exists in controllers

                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
                            if(payload.changeDoc?.startDate || payload.changeDoc?.endDate)
                            {
                                payload.changeDoc?.startDate = ((payload.changeDoc?.startDate != null) && (payload.changeDoc?.startDate.length() > 0)) ? sdf.parse(payload.changeDoc?.startDate) : null
                                payload.changeDoc?.endDate = ((payload.changeDoc?.endDate != null) && (payload.changeDoc?.endDate.length() > 0)) ? sdf.parse(payload.changeDoc?.endDate) : null
                            }
                            if(payload.changeDoc?.accessStartDate || payload.changeDoc?.accessEndDate) {
                                payload.changeDoc?.accessStartDate = ((payload.changeDoc?.accessStartDate != null) && (payload.changeDoc?.accessStartDate.length() > 0)) ? sdf.parse(payload.changeDoc?.accessStartDate) : null
                                payload.changeDoc?.accessEndDate = ((payload.changeDoc?.accessEndDate != null) && (payload.changeDoc?.accessEndDate.length() > 0)) ? sdf.parse(payload.changeDoc?.accessEndDate) : null
                            }


                            DataBindingUtils.bindObjectToInstance(new_instance, payload.changeDoc)
                            if(new_instance.save()) {
                                saveWithoutError = true
                            }
                            else {
                                log.error(new_instance.errors.toString())
                            }
                        }
                        break

                    case EVENT_OBJECT_UPDATE :
                        // TODO [ticket=1894]
                        //if ( ( payload.changeTarget != null ) && ( payload.changeTarget.length() > 0 ) ) {
                        if ( pendingChange.payloadChangeTargetOid?.length() > 0 ) {
                            //def target_object = genericOIDService.resolveOID(payload.changeTarget);
                            def target_object = genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
                            if ( target_object ) {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                if(payload.changeDoc?.startDate || payload.changeDoc?.endDate)
                                {
                                    payload.changeDoc?.startDate = ((payload.changeDoc?.startDate != null) && (payload.changeDoc?.startDate.length() > 0)) ? sdf.parse(payload.changeDoc?.startDate) : null
                                    payload.changeDoc?.endDate = ((payload.changeDoc?.endDate != null) && (payload.changeDoc?.endDate.length() > 0)) ? sdf.parse(payload.changeDoc?.endDate) : null
                                }
                                if(payload.changeDoc?.accessStartDate || payload.changeDoc?.accessEndDate) {
                                    payload.changeDoc?.accessStartDate = ((payload.changeDoc?.accessStartDate != null) && (payload.changeDoc?.accessStartDate.length() > 0)) ? sdf.parse(payload.changeDoc?.accessStartDate) : null
                                    payload.changeDoc?.accessEndDate = ((payload.changeDoc?.accessEndDate != null) && (payload.changeDoc?.accessEndDate.length() > 0)) ? sdf.parse(payload.changeDoc?.accessEndDate) : null
                                }

                                if(payload.changeDoc?.status)
                                {
                                    payload.changeDoc?.status = payload.changeDoc?.status?.id
                                }

                                DataBindingUtils.bindObjectToInstance(target_object, payload.changeDoc)

                                if(target_object.save()) {
                                    saveWithoutError = true
                                }
                                else {
                                    log.error(target_object.errors.toString())
                                }
                            }
                        }
                        break

                    case EVENT_COVERAGE_ADD:
                        // TODO [ticket=1894]
                        //IssueEntitlement target = genericOIDService.resolveOID(payload.changeTarget)
                        IssueEntitlement target = (IssueEntitlement) genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
                        if(target) {
                            Map newCovData = payload.changeDoc
                            IssueEntitlementCoverage cov = new IssueEntitlementCoverage(newCovData)
                            cov.issueEntitlement = target
                            if(cov.save()) {
                                saveWithoutError = true
                            }
                            else {
                                log.error(cov.errors.toString())
                            }
                        }
                        else {
                            log.error("Target issue entitlement with OID ${pendingChange.payloadChangeTargetOid} not found")
                        }
                        break

                    case EVENT_COVERAGE_UPDATE:
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        // TODO [ticket=1894]
                        //IssueEntitlementCoverage target = genericOIDService.resolveOID(payload.changeTarget)
                        IssueEntitlementCoverage target = (IssueEntitlementCoverage) genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
                        Map changeAttrs = payload.changeDoc
                        if(target) {
                            if(changeAttrs.prop in ['startDate','endDate']) {
                                target[changeAttrs.prop] = sdf.parse(changeAttrs.newValue)
                            }
                            else {
                                target[changeAttrs.prop] = changeAttrs.newValue
                            }
                            if(target.save()) {
                                saveWithoutError = true
                            }
                            else {
                                log.error(target.errors.toString())
                            }
                        }
                        else log.error("Target coverage object does not exist! The erroneous OID is: ${pendingChange.payloadChangeTargetOid}")
                        break

                    case EVENT_COVERAGE_DELETE:
                        // TODO [ticket=1894]
                        //IssueEntitlementCoverage cov = genericOIDService.resolveOID(payload.changeTarget)
                        IssueEntitlementCoverage cov = (IssueEntitlementCoverage) genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
                        if(cov) {
                            if(cov.delete()) {
                                saveWithoutError = true
                            }
                            else {
                                log.error("Error on deleting issue entitlement coverage statement with id ${cov.id}")
                            }
                        }
                        else {
                            log.error("Target coverage object does not exist! The erroneous OID is: ${pendingChange.payloadChangeTargetOid}")
                        }
                        break

                    default:
                        log.error("Unhandled change type : ${pc.payload}");
                        break;
                }

                if (saveWithoutError && pendingChange instanceof PendingChange) {
                    pendingChange.status = RefdataValue.getByValueAndCategory("Accepted", RDConstants.PENDING_CHANGE_STATUS)
                    pendingChange.actionDate = new Date()
                    pendingChange.save()  // ERMS-2184 // called implicit somewhere
                    log.debug("Pending change accepted and saved")
                }
            }
            catch ( Exception e ) {
                log.error("Problem accepting change",e)
                result = false
            }
            //status.flush()
            return result
        //}
    }

    @Deprecated
    void performReject(PendingChange change) {
        PendingChange.withNewTransaction { TransactionStatus status ->
            if (change) {
                change.license?.pendingChanges?.remove(change)
                change.license?.save()
                change.subscription?.pendingChanges?.remove(change)
                change.subscription?.save()
                change.actionDate = new Date()
                change.status = RefdataValue.getByValueAndCategory("Rejected",RDConstants.PENDING_CHANGE_STATUS)
                change.save()
            }
        }
    }

    private void processCustomPropertyChange(PendingChange pendingChange, JSONElement payload) {
        def changeDoc = payload.changeDoc

        // TODO [ticket=1894]
        //if ( ( payload.changeTarget != null ) && ( payload.changeTarget.length() > 0 ) ) {
        if (pendingChange.payloadChangeTargetOid?.length() > 0 || payload.changeTarget?.length() > 0) {
            //def changeTarget = genericOIDService.resolveOID(payload.changeTarget)
            String targetOID = pendingChange.payloadChangeTargetOid ?: payload.changeTarget
            def changeTarget = genericOIDService.resolveOID(targetOID.replace('Custom','').replace('Private',''))

            if (changeTarget) {
                if(! changeTarget.hasProperty('propertySet')) {
                    log.error("Custom property change, but owner doesnt have the custom props: ${payload}")
                    return
                }

                //def srcProperty = genericOIDService.resolveOID(changeDoc.propertyOID)
                //def srcObject = genericOIDService.resolveOID(changeDoc.OID)
                String srcOID = pendingChange.payloadChangeDocOid ?: payload.changeDoc.OID
                def srcObject = genericOIDService.resolveOID(srcOID.replace('Custom','').replace('Private',''))

                // A: get existing targetProperty by instanceOf
                def targetProperty = srcObject.getClass().findByOwnerAndInstanceOf(changeTarget, srcObject)

                def setInstanceOf

                // B: get existing targetProperty by name if not multiple allowed
                if (! targetProperty) {
                    if (! srcObject.type.multipleOccurrence) {
                        targetProperty = srcObject.getClass().findByOwnerAndType(changeTarget, srcObject.type)
                        setInstanceOf = true
                    }
                }
                // C: create new targetProperty
                if (! targetProperty) {
                    targetProperty = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, changeTarget, srcObject.type, srcObject.tenant)
                    setInstanceOf = true
                }

                //def updateProp = target_object.customProperties.find{it.type.name == changeDoc.name}
                if (targetProperty) {
                    // in case of C or B set instanceOf
                    if (setInstanceOf && targetProperty.hasProperty('instanceOf')) {
                        targetProperty.instanceOf = srcObject
                        targetProperty.save()
                    }

                    if (changeDoc.event.endsWith('Property.deleted')) {

                        log.debug("Deleting property ${targetProperty.type.name} from ${pendingChange.payloadChangeTargetOid}")
                        changeTarget.customProperties.remove(targetProperty)
                        targetProperty.delete()
                    }
                    else if (changeDoc.event.endsWith('Property.updated')) {

                        log.debug("Update custom property ${targetProperty.type.name}")

                        if (RefdataValue.class.name == changeDoc.type){

                            RefdataValue newProp = (RefdataValue) genericOIDService.resolveOID(
                                    changeDoc.new instanceof String ? changeDoc.new : (changeDoc.new.class + ':' + changeDoc.new.id)
                            )

                            // Backward compatible
                            if (! newProp) {
                                def propDef = targetProperty.type
                                newProp = RefdataValue.getByValueAndCategory(changeDoc.newLabel, propDef.refdataCategory)
                            }
                            targetProperty."${changeDoc.prop}" = newProp
                        }
                        else {
                            targetProperty."${changeDoc.prop}" = AbstractPropertyWithCalculatedLastUpdated.parseValue("${changeDoc.new}", changeDoc.type)
                        }

                        log.debug("Setting value for ${changeDoc.name}.${changeDoc.prop} to ${changeDoc.new}")
                        targetProperty.save()
                    }
                    else {
                        log.error("ChangeDoc event '${changeDoc.event}' not recognized.")
                    }
                }
                else {
                    log.error("Custom property changed, but no derived property found: ${payload}")
                }
            }
        }
    }

    Map<String,Object> getChanges(LinkedHashMap<String, Object> configMap) {
        Locale locale = LocaleContextHolder.getLocale()
        Date time = new Date(System.currentTimeMillis() - Duration.ofDays(configMap.periodInDays).toMillis())
        //package changes
        String subscribedPackagesQuery = 'select new map(sp as subPackage,pcc as config) from PendingChangeConfiguration pcc join pcc.subscriptionPackage sp join sp.subscription sub join sub.orgRelations oo where oo.org = :context and oo.roleType in (:roleTypes) and (pcc.settingValue = :prompt or pcc.withNotification = true)'
        Map<String,Object> spQueryParams = [context:configMap.contextOrg,roleTypes:[RDStore.OR_SUBSCRIPTION_CONSORTIA,RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER],prompt:RDStore.PENDING_CHANGE_CONFIG_PROMPT]
        if(configMap.contextOrg.getCustomerType() == "ORG_CONSORTIUM") {
            subscribedPackagesQuery += ' and sub.instanceOf = null'
        }
        List tokensWithNotifications = SubscriptionPackage.executeQuery(subscribedPackagesQuery,spQueryParams)
        Set<Package> subscribedPackages = []
        Map<SubscriptionPackage,String> packageSettingMap = [:]
        Set<String> withNotification = [], prompt = []
        tokensWithNotifications.each { row ->
            subscribedPackages << (Package) row.subPackage.pkg
            PendingChangeConfiguration pcc = (PendingChangeConfiguration) row.config
            String setting
            if(pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_PROMPT) {
                prompt << pcc.settingKey
                setting = "prompt"
            }
            else if(pcc.settingValue == RDStore.PENDING_CHANGE_CONFIG_ACCEPT && pcc.withNotification) {
                withNotification << pcc.settingKey
                setting = "notify"
            }
            if(setting) {
                packageSettingMap.put(row.subPackage, setting)
            }
        }
        List query1Clauses = [], query2Clauses = []
        String query1 = "select pc from PendingChange pc where pc.owner = :contextOrg and pc.status in (:status) and (pc.msgToken = :newSubscription or pc.costItem != null)",
        query2 = 'select pc.msgToken,pkg.id,count(pc.msgToken),\'pkg\' from PendingChange pc join pc.pkg pkg where pkg in (:packages) and pc.oid = null',
        query3 = 'select pc.msgToken,pkg.id,count(pc.msgToken),\'tipp.pkg\' from PendingChange pc join pc.tipp.pkg pkg where pkg in (:packages) and pc.oid = null',
        query4 = 'select pc.msgToken,pkg.id,count(pc.msgToken),\'tippCoverage.tipp.pkg\' from PendingChange pc join pc.tippCoverage.tipp.pkg pkg where pkg in (:packages) and pc.oid = null',
        query5 = 'select pc.msgToken,pkg.id,count(pc.msgToken),\'priceItem.tipp.pkg\' from PendingChange pc join pc.priceItem.tipp.pkg pkg where pkg in (:packages) and pc.oid = null'
        Map<String,Object> query1Params = [contextOrg:configMap.contextOrg, status:[RDStore.PENDING_CHANGE_PENDING,RDStore.PENDING_CHANGE_ACCEPTED], newSubscription: "pendingChange.message_SU_NEW_01"],
        query2Params = [packages:subscribedPackages]
        if(configMap.periodInDays) {
            query1Clauses << "pc.ts >= :time"
            query2Clauses << "((pc.ts >= :time and pc.msgToken in (:withNotification)) or pc.msgToken in (:prompt))"
            query1Params.time = time
            query2Params.time = time
            query2Params.withNotification = withNotification
            query2Params.prompt = prompt
        }
        if(query1Clauses) {
            query1 += ' and ' + query1Clauses.join(" and ")
        }
        if(query2Clauses) {
            query2 += ' and ' + query2Clauses.join(" and ")
            query3 += ' and ' + query2Clauses.join(" and ")
            query4 += ' and ' + query2Clauses.join(" and ")
            query5 += ' and ' + query2Clauses.join(" and ")
        }
        List<PendingChange> nonPackageChanges = PendingChange.executeQuery(query1,query1Params) //PendingChanges need to be refilled in maps
        List tokensOnly = [], pending = [], notifications = []
        if (subscribedPackages) {
            tokensOnly.addAll(PendingChange.executeQuery(query2 + ' group by pc.msgToken,pkg.id', query2Params))
            tokensOnly.addAll(PendingChange.executeQuery(query3 + ' group by pc.msgToken,pkg.id', query2Params))
            tokensOnly.addAll(PendingChange.executeQuery(query4 + ' group by pc.msgToken,pkg.id', query2Params))
            tokensOnly.addAll(PendingChange.executeQuery(query5 + ' group by pc.msgToken,pkg.id', query2Params))
            /*
               I need to summarize here:
               - the subscription package (I need both)
               - for package changes: the old and new value (there, I can just add the pc row as is)
               - for title and coverage changes: I just need to record that *something* happened and then, on the details page (method subscriptionControllerService.entitlementChanges()), to enumerate the actual changes
            */
            tokensOnly.each { row ->
                Set<SubscriptionPackage> spSet = tokensWithNotifications.findAll { tokenRow -> tokenRow.subPackage.pkg.id == row[1] }.subPackage
                spSet.each {SubscriptionPackage sp ->
                    String setting = packageSettingMap.get(sp)
                    Object[] args = [row[2]]
                    Map<String,Object> eventRow = [subPkg:sp,eventString:messageSource.getMessage(row[0],args,locale)]
                    if(setting == "prompt") {
                        if(!PendingChange.executeQuery('select pc.id from PendingChange pc where pc.'+row[3]+' = :package and pc.oid = :oid and pc.status = :accepted',[package:sp.pkg,oid:genericOIDService.getOID(sp.subscription),accepted:RDStore.PENDING_CHANGE_ACCEPTED]))
                            pending << eventRow
                    }
                    else if(setting == "notify") {
                        notifications << eventRow
                    }
                }
            }
        }
        nonPackageChanges.each { PendingChange pc ->
            Map<String,Object> eventRow = [event:pc.msgToken]
            if(pc.costItem) {
                eventRow.costItem = pc.costItem
                Object[] args = [pc.oldValue,pc.newValue]
                eventRow.eventString = messageSource.getMessage(pc.msgToken,args,locale)
                eventRow.changeId = pc.id
            }
            else {
                eventRow.eventString = messageSource.getMessage("${pc.msgToken}.eventString", null, locale)
                eventRow.changeId = pc.id
                eventRow.subscription = pc.subscription
            }
            if(pc.status == RDStore.PENDING_CHANGE_PENDING)
                pending << eventRow
            else if(pc.status == RDStore.PENDING_CHANGE_ACCEPTED) {
                notifications << eventRow
            }
        }
        //[changes:result,changesCount:result.size(),subscribedPackages:subscribedPackages]
        [notifications:notifications.drop(configMap.offset).take(configMap.max),pending:pending,acceptedOffset:configMap.offset]
    }

    //called from: dashboard.gsp
    Map<String,Object> printRow(PendingChange change) {
        Locale locale = LocaleContextHolder.getLocale()
        String eventIcon, instanceIcon, eventString
        List<Object> eventData
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        if(change.subscription && change.msgToken == "pendingChange.message_SU_NEW_01") {
            eventIcon = '<span data-tooltip="' + messageSource.getMessage("${change.msgToken}", null, locale) + '"><i class="yellow circle icon"></i></span>'
            instanceIcon = '<span data-tooltip="' + messageSource.getMessage('subscription', null, locale) + '"><i class="clipboard icon"></i></span>'
            eventString = messageSource.getMessage('pendingChange.message_SU_NEW_01.eventString', null, locale)
        }
        else if(change.costItem) {
            eventIcon = '<span data-tooltip="'+messageSource.getMessage('default.change.label',null,locale)+'"><i class="yellow circle icon"></i></span>'
            instanceIcon = '<span data-tooltip="'+messageSource.getMessage('financials.costItem',null,locale)+'"><i class="money bill icon"></i></span>'
            eventData = [change.oldValue,change.newValue]
        }
        else {
            switch(change.msgToken) {
                case PendingChangeConfiguration.PACKAGE_PROP:
                case PendingChangeConfiguration.PACKAGE_TIPP_COUNT_CHANGED:
                    eventIcon = '<span data-tooltip="'+messageSource.getMessage('default.change.label',null,locale)+'"><i class="yellow circle icon"></i></span>'
                    instanceIcon = '<span data-tooltip="'+messageSource.getMessage('package',null,locale)+'"><i class="gift icon"></i></span>'
                    eventData = [change.targetProperty,change.oldValue,change.newValue]
                    break
                case PendingChangeConfiguration.PACKAGE_DELETED:
                    eventIcon = '<span data-tooltip="'+messageSource.getMessage('subscription.packages.'+change.msgToken)+'"><i class="red minus icon"></i></span>'
                    instanceIcon = '<span data-tooltip="'+messageSource.getMessage('package',null,locale)+'"><i class="gift icon"></i></span>'
                    eventData = [change.pkg.name]
                    break
            }
        }
        if(eventString == null && eventData)
            eventString = messageSource.getMessage(change.msgToken,eventData.toArray(),locale)
        [instanceIcon:instanceIcon,eventIcon:eventIcon,eventString:eventString]
    }

    /**
     * Converts the given value according to the field type
     * @param key - the string value
     * @return the value as {@link Date} or {@link String}
     */
    def output(PendingChange change,String key) {
        Locale locale = LocaleContextHolder.getLocale()
        def retI
        if(change.targetProperty in PendingChange.DATE_FIELDS) {
            Date date = DateUtils.parseDateGeneric(change[key])
            if(date)
                ret = date.format(messageSource.getMessage('default.date.format.notime',null,locale))
            else ret = null
        }
        else if(change.targetProperty in PendingChange.REFDATA_FIELDS) {
            ret = RefdataValue.get(change[key])
        }
        else ret = change[key]
        ret
    }

    boolean accept(PendingChange pc) throws ChangeAcceptException {
        boolean done = false
        def target
        if(pc.oid)
            target = genericOIDService.resolveOID(pc.oid)
        else if(pc.costItem)
            target = pc.costItem
        def parsedNewValue
        if(pc.targetProperty in PendingChange.DATE_FIELDS)
            parsedNewValue = DateUtils.parseDateGeneric(pc.newValue)
        else if(pc.targetProperty in PendingChange.REFDATA_FIELDS) {
            if(pc.newValue)
                parsedNewValue = RefdataValue.get(Long.parseLong(pc.newValue))
            else reject() //i.e. do nothing, wrong value
        }
        else parsedNewValue = pc.newValue
        switch(pc.msgToken) {
        //pendingChange.message_TP01 (newTitle)
            case PendingChangeConfiguration.NEW_TITLE:
                if(target instanceof TitleInstancePackagePlatform) {
                    TitleInstancePackagePlatform tipp = (TitleInstancePackagePlatform) target
                    IssueEntitlement newTitle = IssueEntitlement.construct([subscription:pc.subscription,tipp:tipp,acceptStatus:RDStore.IE_ACCEPT_STATUS_FIXED,status:tipp.status])
                    if(newTitle) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${newTitle.errors}")
                }
                else if(target instanceof Subscription) {
                    IssueEntitlement newTitle = IssueEntitlement.construct([subscription:target,tipp:pc.tipp,acceptStatus:RDStore.IE_ACCEPT_STATUS_FIXED,status:pc.tipp.status])
                    if(newTitle) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${newTitle.errors}")
                }
                else throw new ChangeAcceptException("no instance of TitleInstancePackagePlatform stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TP02 (titleUpdated)
            case PendingChangeConfiguration.TITLE_UPDATED:
                IssueEntitlement targetTitle
                if(target instanceof IssueEntitlement) {
                    targetTitle = (IssueEntitlement) target
                }
                else if(target instanceof Subscription) {
                    targetTitle = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != :deleted',[target:target,tipp:pc.tipp,deleted:RDStore.TIPP_STATUS_DELETED])[0]
                }
                if(targetTitle) {
                    targetTitle[pc.targetProperty] = parsedNewValue
                    if(targetTitle.save()) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when updating entitlement - pending change not accepted: ${targetTitle.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TP03 (titleDeleted)
            case PendingChangeConfiguration.TITLE_DELETED:
                IssueEntitlement targetTitle
                if(target instanceof IssueEntitlement) {
                    targetTitle = (IssueEntitlement) target
                }
                else if(target instanceof Subscription) {
                    targetTitle = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != deleted',[target:target,tipp:pc.tipp,deleted:RDStore.TIPP_STATUS_DELETED])[0]
                }
                if(targetTitle) {
                    targetTitle.status = RDStore.TIPP_STATUS_DELETED
                    if(targetTitle.save()) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when deleting entitlement - pending change not accepted: ${targetTitle.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlement stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TC01 (coverageUpdated)
            case PendingChangeConfiguration.COVERAGE_UPDATED:
                IssueEntitlementCoverage targetCov
                if(target instanceof IssueEntitlementCoverage) {
                    targetCov = (IssueEntitlementCoverage) target
                }
                else if(target instanceof Subscription) {
                    IssueEntitlement ie = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != deleted',[target:target,tipp:pc.tippCoverage.tipp,deleted:RDStore.TIPP_STATUS_DELETED])[0]
                    targetCov = (IssueEntitlementCoverage) pc.tippCoverage.findEquivalent(ie.coverages)
                }
                if(targetCov) {
                    targetCov[pc.targetProperty] = parsedNewValue
                    if(targetCov.save()) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when updating coverage statement - pending change not accepted: ${targetCov.errors}")
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlementCoverage stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TC02 (newCoverage)
            case PendingChangeConfiguration.NEW_COVERAGE:
                IssueEntitlement owner
                TIPPCoverage tippCoverage
                if(target instanceof TIPPCoverage) {
                    tippCoverage = (TIPPCoverage) target
                    owner = IssueEntitlement.findBySubscriptionAndTipp(pc.subscription,tippCoverage.tipp)
                }
                else if(target instanceof Subscription) {
                    tippCoverage = pc.tippCoverage
                    owner = IssueEntitlement.findBySubscriptionAndTipp(target,tippCoverage.tipp)
                }
                if(tippCoverage && owner) {
                    Map<String,Object> configMap = [issueEntitlement:owner,
                                                    startDate: tippCoverage.startDate,
                                                    startIssue: tippCoverage.startIssue,
                                                    startVolume: tippCoverage.startVolume,
                                                    endDate: tippCoverage.endDate,
                                                    endIssue: tippCoverage.endIssue,
                                                    endVolume: tippCoverage.endVolume,
                                                    embargo: tippCoverage.embargo,
                                                    coverageDepth: tippCoverage.coverageDepth,
                                                    coverageNote: tippCoverage.coverageNote,
                    ]
                    IssueEntitlementCoverage ieCov = new IssueEntitlementCoverage(configMap)
                    if(ieCov.save()) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${ieCov.errors}")
                }
                else throw new ChangeAcceptException("no instance of TIPPCoverage stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TC03 (coverageDeleted)
            case PendingChangeConfiguration.COVERAGE_DELETED:
                IssueEntitlementCoverage targetCov
                if(target instanceof IssueEntitlementCoverage) {
                    targetCov = (IssueEntitlementCoverage) target
                }
                else if(target instanceof Subscription) {
                    JSONObject oldMap = JSON.parse(pc.oldValue) as JSONObject
                    IssueEntitlement ie = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != deleted',[target:target,tipp:oldMap.tipp,deleted:RDStore.TIPP_STATUS_DELETED])[0]
                    for (String k : AbstractCoverage.equivalencyProperties) {
                        targetCov = ie.coverages.find { IssueEntitlementCoverage iec -> iec[k] == oldMap[k] }
                        if(targetCov) {
                            break
                        }
                    }
                }
                if(targetCov) {
                    //no way to check whether object could actually be deleted or not
                    targetCov.delete()
                    done = true
                }
                else throw new ChangeAcceptException("no instance of IssueEntitlementCoverage stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_TR01 (priceUpdated)
            case PendingChangeConfiguration.PRICE_UPDATED:
                PriceItem targetPi
                if(target instanceof PriceItem && target.issueEntitlement) {
                    targetPi = (PriceItem) target
                }
                else if(target instanceof Subscription) {
                    IssueEntitlement ie = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != deleted',[target:target,tipp:priceItem.tipp,deleted:RDStore.TIPP_STATUS_DELETED])[0]
                    targetPi = priceItem.findEquivalent(ie.priceItems)
                }
                if(targetPi) {
                    targetPi[targetProperty] = parsedNewValue
                    if(targetPi.save()) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when updating price item - pending change not accepted: ${targetPi.errors}")
                }
                else throw new ChangeAcceptException("no instance of PriceItem stored: ${oid}! Pending change is void!")
                break
        //pendingChange.message_TR02 (newPrice)
            case PendingChangeConfiguration.NEW_PRICE:
                PriceItem tippPrice
                IssueEntitlement owner
                if(target instanceof PriceItem && target.tipp) {
                    tippPrice = (PriceItem) target
                    owner = IssueEntitlement.findBySubscriptionAndTipp(pc.subscription,tippPrice.tipp)
                }
                else if(target instanceof Subscription) {
                    tippPrice = pc.priceItem
                    owner = IssueEntitlement.findBySubscriptionAndTipp(target,tippPrice.tipp)
                }
                if(owner && tippPrice) {
                    Map<String,Object> configMap = [issueEntitlement:owner,
                                                    startDate: tippPrice.startDate,
                                                    endDate: tippPrice.endDate,
                                                    listPrice: tippPrice.listPrice,
                                                    listCurrency: tippPrice.listCurrency
                    ]
                    PriceItem iePrice = new PriceItem(configMap)
                    if(iePrice.save()) {
                        done = true
                    }
                    else throw new ChangeAcceptException("problems when creating new entitlement - pending change not accepted: ${iePrice.errors}")
                }
                else throw new ChangeAcceptException("no instance of PriceItem stored: ${oid}! Pending change is void!")
                break
        //pendingChange.message_TR03 (priceDeleted)
            case PendingChangeConfiguration.PRICE_DELETED:
                PriceItem targetPi
                if(target instanceof PriceItem && target.issueEntitlement) {
                    targetPi = (PriceItem) target
                }
                else if(target instanceof Subscription) {
                    JSONObject oldMap = JSON.parse(pc.oldValue) as JSONObject
                    IssueEntitlement ie = IssueEntitlement.executeQuery('select ie from IssueEntitlement ie where ie.subscription = :target and ie.tipp = :tipp and ie.status != deleted',[target:target,tipp:oldMap.tipp,deleted:RDStore.TIPP_STATUS_DELETED])[0]
                    for(String k : PriceItem.equivalencyProperties) {
                        targetPi = ie.priceItems.find { PriceItem pi -> pi[k] == oldMap[k] }
                        if(targetPi) {
                            break
                        }
                    }
                }
                if(targetPi) {
                    //no way to check whether object could actually be deleted or not
                    targetPi.delete()
                    done = true
                }
                else throw new ChangeAcceptException("no instance of PriceItem stored: ${pc.oid}! Pending change is void!")
                break
        //pendingChange.message_CI01 (billingSum)
            case PendingChangeConfiguration.BILLING_SUM_UPDATED:
                if(target instanceof CostItem) {
                    CostItem costItem = (CostItem) target
                    costItem.costInBillingCurrency = Double.parseDouble(pc.newValue)
                    if(costItem.save())
                        done = true
                    else throw new ChangeAcceptException("problems when updating billing sum - pending change not accepted: ${costItem.errors}")
                }
                break
        //pendingChange.message_CI02 (localSum)
            case PendingChangeConfiguration.LOCAL_SUM_UPDATED:
                if(target instanceof CostItem) {
                    CostItem costItem = (CostItem) target
                    costItem.costInLocalCurrency = Double.parseDouble(pc.newValue)
                    if(costItem.save())
                        done = true
                    else throw new ChangeAcceptException("problems when updating local sum - pending change not accepted: ${costItem.errors}")
                }
                break
        }
        if(done) {
            pc.status = RDStore.PENDING_CHANGE_ACCEPTED
            if(!pc.save()) {
                throw new ChangeAcceptException("problems when submitting new pending change status: ${errors}")
            }
        }
        done
    }

    boolean reject() {
        pc.status = RDStore.PENDING_CHANGE_REJECTED
        if(!pc.save()) {
            throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
        }
        true
    }

    void applyChangeForHolding(PendingChange newChange,SubscriptionPackage subPkg,Org contextOrg) {
            def target
            if(newChange.tipp)
                target = newChange.tipp
            else if(newChange.tippCoverage)
                target = newChange.tippCoverage
            else if(newChange.priceItem && newChange.priceItem.tipp)
                target = newChange.priceItem
            if(target) {
                PendingChange toApply = PendingChange.construct([target: target, oid: genericOIDService.getOID(subPkg.subscription), newValue: newChange.newValue, oldValue: newChange.oldValue, prop: newChange.targetProperty, msgToken: newChange.msgToken, status: RDStore.PENDING_CHANGE_PENDING, owner: contextOrg])
                if(accept(toApply)) {
                    Set<Subscription> childSubscriptions = Subscription.executeQuery('select sp.subscription from SubscriptionPackage sp join sp.subscription s where s.instanceOf = :parent',[parent:subPkg.subscription])
                    if(childSubscriptions) {
                        if(auditService.getAuditConfig(subPkg.subscription,newChange.msgToken)) {
                            childSubscriptions.each { Subscription child ->
                                log.debug("processing child ${child.id}")
                                PendingChange toApplyChild = PendingChange.construct([target: target, oid: genericOIDService.getOID(child), newValue: newChange.newValue, oldValue: newChange.oldValue, prop: newChange.targetProperty, msgToken: newChange.msgToken, status: RDStore.PENDING_CHANGE_PENDING, owner: contextOrg])
                                if (!accept(toApplyChild)) {
                                    log.error("Error when auto-accepting pending change ${toApplyChild}!")
                                }
                            }
                        }
                    }
                }
                else
                    log.error("Error when auto-accepting pending change ${toApply}!")
            }
            else log.error("Unable to determine target object! Ignoring change ${newChange}!")
    }

    void acknowledgeChange(PendingChange changeAccepted) {
        changeAccepted.delete()
    }

}
