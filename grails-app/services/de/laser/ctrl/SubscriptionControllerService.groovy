package de.laser.ctrl

import de.laser.AccessService
import de.laser.AuditConfig
import de.laser.FilterService
import de.laser.FormService
import de.laser.License
import de.laser.Org
import de.laser.OrgRole
import de.laser.Package
import de.laser.PendingChangeConfiguration
import de.laser.Person
import de.laser.RefdataValue
import de.laser.Subscription
import de.laser.SubscriptionController
import de.laser.SubscriptionPackage
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import de.laser.interfaces.CalculatedType
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.transaction.TransactionStatus

import java.text.SimpleDateFormat

@Transactional
class SubscriptionControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    AccessService accessService
    FilterService filterService
    FormService formService
    MessageSource messageSource

    Map<String,Object> emptySubscription(SubscriptionController controller, GrailsParameterMap params) {
        Map<String, Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if(result.editable) {
            Calendar cal = GregorianCalendar.getInstance()
            SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
            cal.setTimeInMillis(System.currentTimeMillis())
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            result.defaultStartYear = sdf.format(cal.getTime())
            cal.set(Calendar.MONTH, Calendar.DECEMBER)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            result.defaultEndYear = sdf.format(cal.getTime())
            if(accessService.checkPerm("ORG_CONSORTIUM")) {
                params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
                Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
                result.members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            }
            [result:result,status:STATUS_OK]
        }
        else {
            result.errorMessage = 'default.notAuthorized.message'
            [result:result,status:STATUS_ERROR]
        }
    }

    Map<String,Object> processEmptySubscription(SubscriptionController controller, GrailsParameterMap params) {
        log.debug( params.toMapString() )
        Map<String, Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)

        RefdataValue role_sub = RDStore.OR_SUBSCRIBER
        RefdataValue role_sub_cons = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_cons_hidden = RDStore.OR_SUBSCRIBER_CONS_HIDDEN
        RefdataValue role_cons = RDStore.OR_SUBSCRIPTION_CONSORTIA

        RefdataValue orgRole
        RefdataValue memberRole
        RefdataValue subType = RefdataValue.get(params.type)

        switch(subType) {
            case RDStore.SUBSCRIPTION_TYPE_CONSORTIAL:
            case RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE:
                orgRole = role_cons
                memberRole = role_sub_cons
                break
            default:
                orgRole = role_sub
                if (! subType)
                    subType = RDStore.SUBSCRIPTION_TYPE_LOCAL
                break
        }

        //result may be null, change is TODO
        if (result?.editable) {

            SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
            Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
            Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
            RefdataValue status = RefdataValue.get(params.status)

            //beware: at this place, we cannot calculate the subscription type because essential data for the calculation is not persisted/available yet!
            boolean administrative = false
            if(subType == RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE)
                administrative = true

            Subscription new_sub = new Subscription(
                    type: subType,
                    kind: (subType == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL) ? RDStore.SUBSCRIPTION_KIND_CONSORTIAL : null,
                    name: params.newEmptySubName,
                    startDate: startDate,
                    endDate: endDate,
                    status: status,
                    administrative: administrative,
                    identifier: UUID.randomUUID().toString())

            if (new_sub.save()) {
                new OrgRole(org: result.institution, sub: new_sub, roleType: orgRole).save()

                if (result.consortialView){
                    List<Org> cons_members = []

                    params.list('selectedOrgs').each{ it ->
                        cons_members << Org.get(it)
                    }

                        //def cons_members = Combo.executeQuery("select c.fromOrg from Combo as c where c.toOrg = ?", [result.institution])

                    cons_members.each { cm ->
                        Subscription cons_sub = new Subscription(
                                type: subType,
                                kind: (subType == RDStore.SUBSCRIPTION_TYPE_CONSORTIAL) ? RDStore.SUBSCRIPTION_KIND_CONSORTIAL : null,
                                name: params.newEmptySubName,
                                startDate: startDate,
                                endDate: endDate,
                                identifier: UUID.randomUUID().toString(),
                                status: status,
                                administrative: administrative,
                                instanceOf: new_sub,
                                isSlaved: true)
                        if (new_sub.administrative) {
                            new OrgRole(org: cm, sub: cons_sub, roleType: role_sub_cons_hidden).save()
                        }
                        else {
                            new OrgRole(org: cm, sub: cons_sub, roleType: memberRole).save()
                        }
                        new OrgRole(org: result.institution, sub: cons_sub, roleType: orgRole).save()
                    }
                }
                result.newSub = new_sub
                [result:result,status:STATUS_OK]
            } else {
                new_sub.errors.each { e ->
                    log.debug("Problem creating new sub: ${e}");
                }
                result.errorMessage = new_sub.errors
                [result:result,status:STATUS_ERROR]
            }
        }
        else {
            [result:result,status:STATUS_ERROR]
        }
    }

    Map<String,Object> members(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW)
        if(!result)
            [result:null,status:STATUS_ERROR]
        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(result.institution)
        Set<RefdataValue> subscriberRoleTypes = [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN, RDStore.OR_SUBSCRIBER_COLLECTIVE]
        result.validSubChilds = Subscription.executeQuery('select s from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes order by oo.org.sortname asc, oo.org.name asc',[parent:result.subscription,subscriberRoleTypes:subscriberRoleTypes])

        ArrayList<Long> filteredOrgIds = controller.getOrgIdsForFilter()
        result.filteredSubChilds = []
        result.validSubChilds.each { Subscription sub ->
            List<Org> subscr = sub.getAllSubscribers()
            Set<Org> filteredSubscr = []
            subscr.each { Org subOrg ->
                if (filteredOrgIds.contains(subOrg.id)) {
                    filteredSubscr << subOrg
                }
            }
            if (filteredSubscr) {
                if (params.subRunTimeMultiYear || params.subRunTime) {

                    if (params.subRunTimeMultiYear && !params.subRunTime) {
                        if(sub.isMultiYear) {
                            result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                        }
                    }else if (!params.subRunTimeMultiYear && params.subRunTime){
                        if(!sub.isMultiYear) {
                            result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                        }
                    }
                    else {
                        result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                    }
                }
                else {
                    result.filteredSubChilds << [sub: sub, orgs: filteredSubscr]
                }
            }
        }
        result.filterSet = params.filterSet ? true : false
        Set<Map<String,Object>> orgs = []
        if (params.exportXLS || params.format) {
            Map allContacts = Person.getPublicAndPrivateEmailByFunc('General contact person',result.institution)
            Map publicContacts = allContacts.publicContacts
            Map privateContacts = allContacts.privateContacts
            result.filteredSubChilds.each { row ->
                Subscription subChild = (Subscription) row.sub
                row.orgs.each { Org subscr ->
                    Map<String,Object> org = [:]
                    org.name = subscr.name
                    org.sortname = subscr.sortname
                    org.shortname = subscr.shortname
                    org.globalUID = subChild.globalUID
                    org.libraryType = subscr.libraryType
                    org.libraryNetwork = subscr.libraryNetwork
                    org.funderType = subscr.funderType
                    org.region = subscr.region
                    org.country = subscr.country
                    org.startDate = subChild.startDate ? subChild.startDate.format(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale())) : ''
                    org.endDate = subChild.endDate ? subChild.endDate.format(messageSource.getMessage('default.date.format.notime',null,LocaleContextHolder.getLocale())) : ''
                    org.isPublicForApi = subChild.isPublicForApi ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")
                    org.hasPerpetualAccess = subChild.hasPerpetualAccess ? RDStore.YN_YES.getI10n("value") : RDStore.YN_NO.getI10n("value")
                    org.status = subChild.status
                    org.customProperties = subscr.propertySet.findAll{ OrgProperty op -> op.type.tenant == null && ((op.tenant?.id == result.institution.id && op.isPublic) || op.tenant == null) }
                    org.privateProperties = subscr.propertySet.findAll{ OrgProperty op -> op.type.tenant?.id == result.institution.id }
                    Set generalContacts = []
                    if (publicContacts.get(subscr))
                        generalContacts.addAll(publicContacts.get(subscr))
                    if (privateContacts.get(subscr))
                        generalContacts.addAll(privateContacts.get(subscr))
                    org.generalContacts = generalContacts.join("; ")
                    orgs << org
                }
            }
            result.orgs = orgs
        }
        [result:result,status:STATUS_OK]
    }

    Map<String,Object> addMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if(!result)
            [result:null,status:STATUS_ERROR]
        else {
            Locale locale = LocaleContextHolder.getLocale()
            params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
            //the following two are arguments for a g.message-call on the view which expects an Object[]
            result.superOrgType = [messageSource.getMessage('consortium.superOrgType',null,locale)]
            result.memberType = [messageSource.getMessage('consortium.subscriber',null,locale)]
            Map<String,Object> fsq = filterService.getOrgComboQuery(params, result.institution)
            result.members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            result.members_disabled = Subscription.executeQuery("select oo.org.id from OrgRole oo join oo.sub s where s.instanceOf = :io",[io: result.subscription])
            result.validPackages = result.subscription.packages?.sort { it.pkg.name }
            result.memberLicenses = License.executeQuery("select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType",[subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE])

            [result:result,status:STATUS_OK]
        }
    }

    Map<String,Object> processAddMembers(SubscriptionController controller, GrailsParameterMap params) {
        Map<String,Object> result = controller.setResultGenericsAndCheckAccess(AccessService.CHECK_VIEW_AND_EDIT)
        if (!result) {
            [result:null,status:STATUS_ERROR]
        }
        else {
            if(formService.validateToken(params)) {
                RefdataValue subStatus = RefdataValue.get(params.subStatus) ?: RDStore.SUBSCRIPTION_CURRENT
                RefdataValue role_sub       = RDStore.OR_SUBSCRIBER_CONS
                RefdataValue role_sub_cons  = RDStore.OR_SUBSCRIPTION_CONSORTIA
                RefdataValue role_sub_hidden = RDStore.OR_SUBSCRIBER_CONS_HIDDEN

                if (result.editable) {
                    List<Org> members = []
                    License licenseCopy
                    params.list('selectedOrgs').each { it ->
                        members << Org.findById(Long.valueOf(it))
                    }
                    List<Subscription> synShareTargetList = []
                    List<License> licensesToProcess = []
                    Set<Package> packagesToProcess = []
                    //copy package data
                    if(params.linkAllPackages) {
                        result.subscription.packages.each { SubscriptionPackage sp ->
                            packagesToProcess << sp.pkg
                        }
                    }
                    else if(params.packageSelection) {
                        List packageIds = params.list("packageSelection")
                        packageIds.each { spId ->
                            packagesToProcess << SubscriptionPackage.get(spId).pkg
                        }
                    }
                    if(params.generateSlavedLics == "all") {
                        String query = "select li.sourceLicense from Links li where li.destinationSubscription = :subscription and li.linkType = :linkType"
                        licensesToProcess.addAll(License.executeQuery(query, [subscription:result.subscription, linkType:RDStore.LINKTYPE_LICENSE]))
                    }
                    else if(params.generateSlavedLics == "partial") {
                        List<String> licenseKeys = params.list("generateSlavedLicsReference")
                        licenseKeys.each { String licenseKey ->
                            licensesToProcess << genericOIDService.resolveOID(licenseKey)
                        }
                    }
                    Set<AuditConfig> inheritedAttributes = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldNotInList(Subscription.class.name,result.subscription.id, PendingChangeConfiguration.SETTING_KEYS)
                    members.each { Org cm ->
                        log.debug("Generating separate slaved instances for members")
                        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
                        Date startDate = params.valid_from ? sdf.parse(params.valid_from) : null
                        Date endDate = params.valid_to ? sdf.parse(params.valid_to) : null
                        Subscription memberSub = new Subscription(
                                type: result.subscription.type ?: null,
                                kind: result.subscription.kind ?: null,
                                status: subStatus,
                                name: result.subscription.name,
                                //name: result.subscription.name + " (" + (cm.get(0).shortname ?: cm.get(0).name) + ")",
                                startDate: startDate,
                                endDate: endDate,
                                administrative: result.subscription._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE,
                                manualRenewalDate: result.subscription.manualRenewalDate,
                                /* manualCancellationDate: result.subscription.manualCancellationDate, */
                                identifier: UUID.randomUUID().toString(),
                                instanceOf: result.subscription,
                                isSlaved: true,
                                resource: result.subscription.resource ?: null,
                                form: result.subscription.form ?: null,
                                isMultiYear: params.checkSubRunTimeMultiYear ?: false
                        )
                        inheritedAttributes.each { attr ->
                            memberSub[attr.referenceField] = result.subscription[attr.referenceField]
                        }
                        if (!memberSub.save()) {
                            memberSub.errors.each { e ->
                                log.debug("Problem creating new sub: ${e}")
                            }
                            flash.error = memberSub.errors
                        }
                        if (memberSub) {
                            if(result.subscription._getCalculatedType() == CalculatedType.TYPE_ADMINISTRATIVE) {
                                new OrgRole(org: cm, sub: memberSub, roleType: role_sub_hidden).save()
                            }
                            else {
                                new OrgRole(org: cm, sub: memberSub, roleType: role_sub).save()
                            }
                            new OrgRole(org: result.institution, sub: memberSub, roleType: role_sub_cons).save()
                            synShareTargetList.add(memberSub)
                            SubscriptionProperty.findAllByOwner(result.subscription).each { SubscriptionProperty sp ->
                                AuditConfig ac = AuditConfig.getConfig(sp)

                                        if (ac) {
                                            // multi occurrence props; add one additional with backref
                                            if (sp.type.multipleOccurrence) {
                                                SubscriptionProperty additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, sp.type, sp.tenant)
                                                additionalProp = sp.copyInto(additionalProp)
                                                additionalProp.instanceOf = sp
                                                additionalProp.save()
                                            }
                                            else {
                                                // no match found, creating new prop with backref
                                                SubscriptionProperty newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, sp.type, sp.tenant)
                                                newProp = sp.copyInto(newProp)
                                                newProp.instanceOf = sp
                                                newProp.save()
                                            }
                                        }
                                    }

                                    memberSub.refresh()

                                    packagesToProcess.each { Package pkg ->
                                        if(params.linkWithEntitlements)
                                            pkg.addToSubscriptionCurrentStock(memberSub, result.subscription)
                                        else
                                            pkg.addToSubscription(memberSub, false)
                                    }

                                    licensesToProcess.each { License lic ->
                                        subscriptionService.setOrgLicRole(memberSub,lic,false)
                                    }

                                }
                                //}
                            }

                        result.subscription.syncAllShares(synShareTargetList)
                } else {
                    [result:result,status:STATUS_ERROR]
                }
            }
            else {
                [result:result,status:STATUS_ERROR]
            }
            [result:result,status:STATUS_OK]
        }
    }

}
