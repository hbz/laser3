package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import de.laser.SubscriptionService
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.PendingChangeConfiguration
import de.laser.domain.TIPPCoverage
import de.laser.helper.DateUtil
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.interfaces.AbstractLockableService
import grails.converters.JSON
import org.codehaus.groovy.grails.web.binding.DataBindingUtils
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration

class PendingChangeService extends AbstractLockableService {

    def genericOIDService
    def grailsApplication
    def springSecurityService
    SubscriptionService subscriptionService
    LinkGenerator grailsLinkGenerator
    MessageSource messageSource

    final static EVENT_OBJECT_NEW = 'New Object'
    final static EVENT_OBJECT_UPDATE = 'Update Object'

    final static EVENT_TIPP_ADD = 'TIPPAdd'
    final static EVENT_TIPP_EDIT = 'TIPPEdit'
    final static EVENT_TIPP_DELETE = 'TIPPDeleted'

    final static EVENT_COVERAGE_ADD = 'CoverageAdd'
    final static EVENT_COVERAGE_UPDATE = 'CoverageUpdate'
    final static EVENT_COVERAGE_DELETE = 'CoverageDeleted'

    final static EVENT_PROPERTY_CHANGE = 'PropertyChange'

    boolean performMultipleAcceptsForJob(List<PendingChange> subscriptionChanges, List<PendingChange> licenseChanges) {
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
    }

    @Deprecated
    boolean performAccept(PendingChange pendingChange) {

        log.debug('performAccept(): ' + pendingChange)
        boolean result = true

        PendingChange.withNewTransaction { TransactionStatus status ->
            boolean saveWithoutError = false

            try {
                JSONElement payload = JSON.parse(pendingChange.payload)
                log.debug("Process change ${payload}");
                switch ( payload.changeType ) {

                    case EVENT_TIPP_DELETE :
                        // "changeType":"TIPPDeleted","tippId":"com.k_int.kbplus.TitleInstancePackagePlatform:6482"}
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
                        TitleInstancePackagePlatform underlyingTIPP = genericOIDService.resolveOID(payload.changeDoc.OID)
                        Subscription subConcerned = pendingChange.subscription
                        subscriptionService.addEntitlement(subConcerned,underlyingTIPP.gokbId,null,false,RDStore.IE_ACCEPT_STATUS_FIXED)
                        saveWithoutError = true
                        break

                    case EVENT_PROPERTY_CHANGE :  // Generic property change
                        // TODO [ticket=1894]
                        // if ( ( payload.changeTarget != null ) && ( payload.changeTarget.length() > 0 ) ) {
                        if ( pendingChange.payloadChangeTargetOid?.length() > 0 ) {
                            //def target_object = genericOIDService.resolveOID(payload.changeTarget);
                            def target_object = genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
                            if ( target_object ) {
                                target_object.refresh()
                                // Work out if parsed_change_info.changeDoc.prop is an association - If so we will need to resolve the OID in the value
                                def domain_class = grailsApplication.getArtefact('Domain',target_object.class.name);
                                def prop_info = domain_class.getPersistentProperty(payload.changeDoc.prop)
                                if(prop_info == null){
                                    log.debug("We are dealing with custom properties: ${payload}")
                                    //processCustomPropertyChange(payload)
                                    processCustomPropertyChange(pendingChange, payload) // TODO [ticket=1894]
                                }
                                else if ( prop_info.name == 'status' ) {
                                    RefdataValue oldStatus = genericOIDService.resolveOID(payload.changeDoc.old)
                                    RefdataValue newStatus = genericOIDService.resolveOID(payload.changeDoc.new)
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
                        def new_domain_class = grailsApplication.getArtefact('Domain',payload.newObjectClass);
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
                                log.error(new_instance.errors)
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

                                if(payload.changeDoc?.status) //continue here: reset DB, perform everything, then check process at this line - status of retired TIPPs goes miraculously to null
                                {
                                    payload.changeDoc?.status = payload.changeDoc?.status?.id
                                }

                                DataBindingUtils.bindObjectToInstance(target_object, payload.changeDoc)

                                if(target_object.save()) {
                                    saveWithoutError = true
                                }
                                else {
                                    log.error(target_object.getErrors())
                                }
                            }
                        }
                        break

                    case EVENT_COVERAGE_ADD:
                        // TODO [ticket=1894]
                        //IssueEntitlement target = genericOIDService.resolveOID(payload.changeTarget)
                        IssueEntitlement target = genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
                        if(target) {
                            Map newCovData = payload.changeDoc
                            IssueEntitlementCoverage cov = new IssueEntitlementCoverage(newCovData)
                            cov.issueEntitlement = target
                            if(cov.save()) {
                                saveWithoutError = true
                            }
                            else {
                                log.error(cov.getErrors())
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
                        IssueEntitlementCoverage target = genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
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
                                log.error(target.getErrors())
                            }
                        }
                        else log.error("Target coverage object does not exist! The erroneous OID is: ${pendingChange.payloadChangeTargetOid}")
                        break

                    case EVENT_COVERAGE_DELETE:
                        // TODO [ticket=1894]
                        //IssueEntitlementCoverage cov = genericOIDService.resolveOID(payload.changeTarget)
                        IssueEntitlementCoverage cov = genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)
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
                    /*if(pendingChange.pkg?.pendingChanges) pendingChange.pkg?.pendingChanges?.remove(pendingChange)
                    pendingChange.pkg?.save();
                    if(pendingChange.license?.pendingChanges) pendingChange.license?.pendingChanges?.remove(pendingChange)
                    pendingChange.license?.save();
                    if(pendingChange.subscription?.pendingChanges) pendingChange.subscription?.pendingChanges?.remove(pendingChange)
                    pendingChange.subscription?.save();*/
                    pendingChange.status = RefdataValue.getByValueAndCategory("Accepted", RDConstants.PENDING_CHANGE_STATUS)
                    pendingChange.actionDate = new Date()
                    //pendingChange.save()  // ERMS-2184 // called implicit somewhere
                    log.debug("Pending change accepted and saved")
                }
            }
            catch ( Exception e ) {
                log.error("Problem accepting change",e)
                result = false
            }
            return result
        }
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
        if (pendingChange.payloadChangeTargetOid?.length() > 0) {
            //def changeTarget = genericOIDService.resolveOID(payload.changeTarget)
            def changeTarget = genericOIDService.resolveOID(pendingChange.payloadChangeTargetOid)

            if (changeTarget) {
                if(! changeTarget.hasProperty('customProperties')) {
                    log.error("Custom property change, but owner doesnt have the custom props: ${payload}")
                    return
                }

                //def srcProperty = genericOIDService.resolveOID(changeDoc.propertyOID)
                //def srcObject = genericOIDService.resolveOID(changeDoc.OID)
                def srcObject = genericOIDService.resolveOID(pendingChange.payloadChangeDocOid)

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
                    targetProperty = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, changeTarget, srcObject.type)
                    setInstanceOf = true
                }

                //def updateProp = target_object.customProperties.find{it.type.name == changeDoc.name}
                if (targetProperty) {
                    // in case of C or B set instanceOf
                    if (setInstanceOf && targetProperty.hasProperty('instanceOf')) {
                        targetProperty.instanceOf = srcObject
                        targetProperty.save(flush: true)
                    }

                    if (changeDoc.event.endsWith('CustomProperty.deleted')) {

                        log.debug("Deleting property ${targetProperty.type.name} from ${pendingChange.payloadChangeTargetOid}")
                        changeTarget.customProperties.remove(targetProperty)
                        targetProperty.delete()
                    }
                    else if (changeDoc.event.endsWith('CustomProperty.updated')) {

                        log.debug("Update custom property ${targetProperty.type.name}")

                        if (changeDoc.type == RefdataValue.toString()){
                            def newProp = genericOIDService.resolveOID(changeDoc.new instanceof String ?: (changeDoc.new.class + ':' + changeDoc.new.id))

                            // Backward compatible
                            if (!newProp) {
                                def propDef = targetProperty.type
                                newProp = RefdataValue.getByValueAndCategory(changeDoc.newLabel, propDef.refdataCategory)
                                // Fallback
                                if (! newProp) {
                                    // ERMS-2016: newProp = RefdataCategory.lookupOrCreate(propDef.refdataCategory, changeDoc.newLabel)
                                    // if value exists --> RefdataValue.getByValueAndCategory()

                                    newProp = RefdataValue.construct([
                                            token   : changeDoc.newLabel,
                                            rdc     : propDef.refdataCategory,
                                            hardData: false,
                                            i10n    : [value_en: changeDoc.newLabel, value_de: changeDoc.newLabel]
                                    ])
                                }
                            }
                            targetProperty."${changeDoc.prop}" = newProp
                        }
                        else {
                            targetProperty."${changeDoc.prop}" = targetProperty.parseValue("${changeDoc.new}", changeDoc.type)
                        }

                        log.debug("Setting value for ${changeDoc.name}.${changeDoc.prop} to ${changeDoc.new}")
                        targetProperty.save()
                    }
                    else {
                        log.error("ChangeDoc event '${changeDoc.event}'' not recognized.")
                    }
                }
                else {
                    log.error("Custom property changed, but no derived property found: ${payload}")
                }
            }
        }
    }

    Map<String,Object> getChanges(LinkedHashMap<String, Object> configMap) {
        Set<Map<String,Object>> pendingChanges = [], acceptedChanges = []
        String queryString = "select pc from PendingChange pc where pc.owner = :contextOrg and "
        List query = []
        Map<String,Object> queryParams = [contextOrg:configMap.contextOrg]
        if(configMap.periodInDays) {
            query << "pc.ts > :time"
            queryParams.time = new Date(System.currentTimeMillis() - Duration.ofDays(configMap.periodInDays).toMillis())
        }
        if(query)
            queryString += query.join(" and ")
        String pendingQuery = "${queryString} and pc.status = :pending", acceptedQuery = "${queryString} and pc.status = :accepted"
        Map<String,RefdataValue> pendingFilterParams = [pending:RDStore.PENDING_CHANGE_PENDING], acceptedFilterParams = [accepted:RDStore.PENDING_CHANGE_ACCEPTED]
        if(configMap.pending) {
            List<PendingChange> result = PendingChange.executeQuery(pendingQuery,queryParams+pendingFilterParams)
            if(configMap.offset && configMap.max)
                result = result.drop(configMap.offset).take(configMap.max)
            result.each { chObj ->
                if(chObj instanceof PendingChange) {
                    PendingChange change = chObj
                    if(change.subscription) {
                        if(change.subscription.instanceOf) {
                            Map<String,Object> entryOfParent = pendingChanges.find {it.target == change.subscription.instanceOf}
                            if(entryOfParent){
                                if(!entryOfParent.memberSubscriptions)
                                    entryOfParent.memberSubscriptions = [change]
                                else entryOfParent.memberSubscriptions << change

                            }
                            else pendingChanges << [target:change.subscription.instanceOf,memberSubscriptions:[change]]
                        }
                        else {
                            if(pendingChanges.find {it.target == change.subscription})
                                pendingChanges.find {it.target == change.subscription}.change = change
                            else pendingChanges << [target:change.subscription,change:change]
                        }
                    }
                    else if(change.license) {
                        pendingChanges << [target:change.license,change:change]
                    }
                    else if(change.costItem) {
                        pendingChanges << [target:change.costItem]
                    }
                }
            }
        }
        if(configMap.notifications) {
            List<PendingChange> result = PendingChange.executeQuery(acceptedQuery,queryParams+acceptedFilterParams)
            if(configMap.offset && configMap.max)
                result = result.drop(configMap.offset).take(configMap.max)
            result.each { resObj ->
                PendingChange change = (PendingChange) resObj
                //fetch pending change configuration for subscription package attached, see if notification should be generated; fallback is yes
                if(change.subscription) {
                    def target = genericOIDService.resolveOID(change.oid)
                    Package targetPkg
                    if(target instanceof TitleInstancePackagePlatform) {
                        targetPkg = target.pkg
                    }
                    else if(target instanceof IssueEntitlement || target instanceof TIPPCoverage) {
                        targetPkg = target.tipp.pkg
                    }
                    else if(target instanceof IssueEntitlementCoverage) {
                        targetPkg = target.issueEntitlement.tipp.pkg
                    }
                    else if(target instanceof Package) {
                        targetPkg = target
                    }
                    if(targetPkg){
                        SubscriptionPackage targetSp = SubscriptionPackage.findBySubscriptionAndPkg(change.subscription,targetPkg)
                        PendingChangeConfiguration pcc = PendingChangeConfiguration.findBySubscriptionPackageAndSettingKey(targetSp,change.msgToken)
                        if(!pcc) {
                            targetSp = SubscriptionPackage.findBySubscriptionAndPkg(change.subscription.instanceOf,targetPkg)
                            pcc = PendingChangeConfiguration.findBySubscriptionPackageAndSettingKey(targetSp,change.msgToken)
                        }
                        if(pcc && pcc.withNotification || !pcc) {
                            if(change.subscription.instanceOf) {
                                Map<String,Object> entryOfParent = acceptedChanges.find {it.target == change.subscription.instanceOf}
                                if(!entryOfParent.memberSubscriptions)
                                    entryOfParent.memberSubscriptions = [change]
                                else entryOfParent.memberSubscriptions << change
                            }
                            else {
                                acceptedChanges << [target:change.subscription,change:change]
                            }
                        }
                    }
                }
                else if(change.license) {
                    acceptedChanges << [target:change.license,change:change]
                }
                else if(change.costItem) {
                    acceptedChanges << [target:change.costItem,change:change]
                }
            }
        }
        [pending:pendingChanges,pendingCount:pendingChanges.size(),notifications:acceptedChanges,notificationsCount:acceptedChanges.size()]
    }

    //called from: dashboard.gsp, pendingChanges.gsp, accepetdChanges.gsp
    Map<String,Object> printRow(PendingChange change) {
        String eventIcon, instanceIcon, eventString, pkgLink, pkgName, titleLink, titleName, platformName, platformLink, holdingLink, coverageString
        List<Object> eventData
        Locale locale = Locale.getDefault()
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null,locale))
        if(change.oid) {
            if(change.oid.contains(IssueEntitlement.class.name)){
                IssueEntitlement target = (IssueEntitlement) genericOIDService.resolveOID(change.oid)
                holdingLink = grailsLinkGenerator.link(controller: 'subscription', action: 'index', id: target.subscription.id, params: [filter: target.tipp.title.title,pkgfilter: target.tipp.pkg.id])
                pkgName = target.tipp.pkg.name
                titleName = target.tipp.title.title
            }
            else if(change.oid.contains(TitleInstancePackagePlatform.class.name)) {
                TitleInstancePackagePlatform target = (TitleInstancePackagePlatform) genericOIDService.resolveOID(change.oid)
                pkgLink = grailsLinkGenerator.link(controller: 'package', action: 'current', id: target.pkg.id, params: [filter:target.title.title])
                pkgName = target.pkg.name
                titleLink = grailsLinkGenerator.link(controller: 'title', action: 'show', id: target.title.id)
                titleName = target.title.title
                platformLink = grailsLinkGenerator.link(controller: 'platform', action: 'show', id: target.platform.id)
                platformName = target.platform.name
            }
            else if(change.oid.contains(IssueEntitlementCoverage.class.name)) {
                IssueEntitlementCoverage target = (IssueEntitlementCoverage) genericOIDService.resolveOID(change.oid)
                IssueEntitlement ie = target.issueEntitlement
                String volume = messageSource.getMessage('tipp.volume',null,locale)
                String issue = messageSource.getMessage('tipp.issue',null,locale)
                holdingLink = grailsLinkGenerator.link(controller: 'subscription', action: 'index', id: ie.subscription.id, params: [filter: ie.tipp.title.title,pkgfilter: ie.tipp.pkg.id])
                titleName = ie.tipp.title.title
                pkgName = ie.tipp.pkg.name
                String startDate = target.startDate ? sdf.format(target.startDate) : ""
                String endDate = target.endDate ? sdf.format(target.endDate) : ""
                coverageString = "${startDate} (${volume} ${target.startVolume}, ${issue} ${target.startIssue}) – ${endDate} (${volume} ${target.endVolume}, ${issue} ${target.endIssue})"
            }
            else if(change.oid.contains(TIPPCoverage.class.name)) {
                TIPPCoverage target = (TIPPCoverage) genericOIDService.resolveOID(change.oid)
                pkgName = target.tipp.pkg.name
                titleName = target.tipp.title.title
                platformName = target.tipp.platform.name
                String volume = messageSource.getMessage('tipp.volume',null,locale)
                String issue = messageSource.getMessage('tipp.issue',null,locale)
                String startDate = target.startDate ? sdf.format(target.startDate) : ""
                String endDate = target.endDate ? sdf.format(target.endDate) : ""
                coverageString = "${startDate} (${volume} ${target.startVolume}, ${issue} ${target.startIssue}) – ${endDate} (${volume} ${target.endVolume}, ${issue} ${target.endIssue})"
            }
            switch(change.msgToken) {
            //pendingChange.message_TP01 (newTitle)
                case PendingChangeConfiguration.NEW_TITLE:
                    eventIcon = '<i class="green plus icon"></i>'
                    instanceIcon = '<i class="book icon"></i>'
                    if(pkgLink && pkgName && titleLink && titleName && platformLink && platformName)
                        eventData = [pkgLink,pkgName,titleLink,titleName,platformLink,platformName]
                    else eventString = messageSource.getMessage('pendingChange.invalidParameter',null,locale)
                    break
            //pendingChange.message_TP02 (titleUpdated)
                case PendingChangeConfiguration.TITLE_UPDATED:
                    eventIcon = '<i class="yellow circle outline icon"></i>'
                    instanceIcon = '<i class="book icon"></i>'
                    if(holdingLink && titleName && pkgName) {
                        eventData = [holdingLink,titleName,pkgName,messageSource.getMessage("tipp.${change.targetProperty}",null,locale),output(change,'oldValue')]
                        if(change.targetProperty in ['hostPlatformURL'])
                            eventData << "<a href='${output(change,'newValue')}'>${output(change,'newValue')}</a>"
                        else eventData << output(change,'newValue')
                    }
                    else eventString = messageSource.getMessage('pendingChange.invalidParameter',null,locale)
                    break
            //pendingChange.message_TP03 (titleDeleted)
                case PendingChangeConfiguration.TITLE_DELETED:
                    eventIcon = '<i class="red minus icon"></i>'
                    instanceIcon = '<i class="book icon"></i>'
                    if(pkgName && titleName && holdingLink)
                        eventData = [pkgName,titleName,holdingLink]
                    else eventString = messageSource.getMessage('pendingChange.invalidParameter',null,locale)
                    break
            //pendingChange.message_TC01 (coverageUpdated)
                case PendingChangeConfiguration.COVERAGE_UPDATED:
                    eventIcon = '<i class="yellow circle outline icon"></i>'
                    instanceIcon = '<i class="file alternate icon"></i>'
                    if(holdingLink && pkgName && coverageString && titleName) {
                        eventData = [holdingLink,pkgName,titleName,coverageString,messageSource.getMessage("tipp.${change.targetProperty}",null,locale),output(change,'oldValue'),output(change,'newValue')]
                    }
                    else eventString = messageSource.getMessage('pendingChange.invalidParameter',null,locale)
                    break
            //pendingChange.message_TC02 (newCoverage)
                case PendingChangeConfiguration.NEW_COVERAGE:
                    eventIcon = '<i class="green plus icon"></i>'
                    instanceIcon = '<i class="file alternate icon"></i>'
                    if(holdingLink && coverageString)
                        eventData = [coverageString,holdingLink]
                    else eventString = messageSource.getMessage('pendingChange.invalidParameter',null,locale)
                    break
            //pendingChange.message_TC03 (coverageDeleted)
                case PendingChangeConfiguration.COVERAGE_DELETED:
                    eventIcon = '<i class="red minus icon"></i>'
                    instanceIcon = '<i class="file alternate icon"></i>'
                    if(holdingLink && coverageString)
                        eventData = [coverageString,holdingLink]
                    else eventString = messageSource.getMessage('pendingChange.invalidParameter',null,locale)
                    break
            }
        }

        if(eventString == null)
            eventString = messageSource.getMessage(change.msgToken,eventData.toArray(),locale)
        [instanceIcon:instanceIcon,eventIcon:eventIcon,eventString:eventString]
    }

    /**
     * Converts the given value according to the field type
     * @param key - the string value
     * @return the value as {@link Date} or {@link String}
     */
    def output(PendingChange change,String key) {
        def ret
        if(change.targetProperty in PendingChange.DATE_FIELDS) {
            ret = DateUtil.parseDateGeneric(change[key])
        }
        else if(change.targetProperty in PendingChange.REFDATA_FIELDS) {
            ret = RefdataValue.get(change[key])
        }
        else ret = change[key]
        ret
    }

}
