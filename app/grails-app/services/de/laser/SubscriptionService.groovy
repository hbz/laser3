package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.abstract_domain.CustomProperty
import com.k_int.kbplus.abstract_domain.PrivateProperty
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.domain.IssueEntitlementCoverage
import de.laser.domain.PendingChangeConfiguration
import de.laser.domain.PriceItem
import de.laser.domain.TIPPCoverage
import de.laser.exceptions.EntitlementCreationException
import de.laser.helper.DateUtil
import de.laser.helper.FactoryResult
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import grails.util.Holders
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.web.multipart.commons.CommonsMultipartFile

import java.nio.file.Files
import java.nio.file.Path
import java.text.SimpleDateFormat

import static de.laser.helper.RDStore.*

class SubscriptionService {
    def genericOIDService
    def contextService
    def accessService
    def subscriptionsQueryService
    def docstoreService
    def messageSource
    def escapeService
    def refdataService
    Locale locale
    def grailsApplication

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
           /* tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))*/

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

    List getMySubscriptionsWithMyElements_readRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_INST")) {

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

            tmpQ = getSubscriptionsLocalLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

        }
        result
    }

    List getMySubscriptionsWithMyElements_writeRights(){
        List result = []
        List tmpQ

        if(accessService.checkPerm("ORG_INST")) {

            tmpQ = getSubscriptionsConsortialLicenseQuery()
            result.addAll(Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1]))

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
        def validSubChilds = Subscription.findAllByInstanceOf(subscription)
        validSubChilds = validSubChilds?.sort { a, b ->
            def sa = a.getSubscriber()
            def sb = b.getSubscriber()
            (sa?.sortname ?: sa?.name ?: "")?.compareTo((sb?.sortname ?: sb?.name ?: ""))
        }
        validSubChilds
    }

    List getCurrentValidSubChilds(Subscription subscription) {
        def validSubChilds = Subscription.findAllByInstanceOfAndStatus(
                subscription,
                SUBSCRIPTION_CURRENT
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

    List getIssueEntitlementsWithFilter(Subscription subscription, params) {

        if(subscription) {
            String base_qry = null
            Map<String,Object> qry_params = [subscription: subscription]

            def date_filter
            if (params.asAt && params?.asAt?.length() > 0) {
                SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
                date_filter = sdf.parse(params.asAt)
                /*result.as_at_date = date_filter
                result.editable = false;*/
            } else {
                date_filter = new Date()
               /* result.as_at_date = date_filter*/
            }
            // We dont want this filter to reach SQL query as it will break it.
            def core_status_filter = params.sort == 'core_status'
            if (core_status_filter) params.remove('sort');

            if (params.filter) {
                base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
                if (params.mode != 'advanced') {
                    // If we are not in advanced mode, hide IEs that are not current, otherwise filter
                    // base_qry += "and ie.status <> ? and ( ? >= coalesce(ie.accessStartDate,subscription.startDate) ) and ( ( ? <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                    // qry_params.add(deleted_ie);
                    base_qry += "and (( :startDate >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( :endDate <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) )  "
                    qry_params.startDate = date_filter
                    qry_params.endDate = date_filter
                }
                base_qry += "and ( ( lower(ie.tipp.title.title) like :title ) or ( exists ( from Identifier ident where ident.ti.id = ie.tipp.title.id and ident.value like :identifier ) ) ) "
                qry_params.title = "%${params.filter.trim().toLowerCase()}%"
                qry_params.identifier = "%${params.filter}%"
            } else {
                base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
                if (params.mode != 'advanced') {
                    // If we are not in advanced mode, hide IEs that are not current, otherwise filter

                    base_qry += " and (( :startDate >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( :endDate <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) ) "
                    qry_params.startDate = date_filter
                    qry_params.endDate = date_filter
                }
            }

            if(params.mode != 'advanced') {
                base_qry += " and ie.status = :current "
                qry_params.current = TIPP_STATUS_CURRENT
            }
            else {
                base_qry += " and ie.status != :deleted "
                qry_params.deleted = TIPP_STATUS_DELETED
            }

            if(params.ieAcceptStatusFixed) {
                base_qry += " and ie.acceptStatus = :ieAcceptStatus "
                qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
            }

            if(params.ieAcceptStatusNotFixed) {
                base_qry += " and ie.acceptStatus != :ieAcceptStatus "
                qry_params.ieAcceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
            }

            if(params.summaryOfContent) {
                base_qry += " and lower(ie.tipp.title.summaryOfContent) like :summaryOfContent "
                qry_params.summaryOfContent = "%${params.summaryOfContent.trim().toLowerCase()}%"
            }

            if(params.ebookFirstAutorOrFirstEditor) {
                base_qry += " and (lower(ie.tipp.title.firstAuthor) like :ebookFirstAutorOrFirstEditor or lower(ie.tipp.title.firstEditor) like :ebookFirstAutorOrFirstEditor) "
                qry_params.ebookFirstAutorOrFirstEditor = "%${params.ebookFirstAutorOrFirstEditor.trim().toLowerCase()}%"
            }

            if (params.pkgfilter && (params.pkgfilter != '')) {
                base_qry += " and ie.tipp.pkg.id = :pkgId "
                qry_params.pkgId = Long.parseLong(params.pkgfilter)
            }

            if ((params.sort != null) && (params.sort.length() > 0)) {
                base_qry += "order by ie.${params.sort} ${params.order} "
            } else {
                base_qry += "order by lower(ie.tipp.title.title) asc"
            }

            List<IssueEntitlement> ies = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params, [max: params.max, offset: params.offset])



            ies.sort { it.tipp.title.title }
            ies
        }else{
            List<IssueEntitlement> ies = []
            ies
        }
    }


    // Entscheidung steht aus
    List getIssueEntitlementsUnderConsideration(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }
    //In Verhandlung
    List getIssueEntitlementsUnderNegotiation(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_UNDER_NEGOTIATION, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getIssueEntitlementsNotFixed(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getSelectedIssueEntitlementsBySurvey(Subscription subscription, SurveyInfo surveyInfo) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus != :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    List getIssueEntitlementsFixed(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.acceptStatus = :acceptStat and ie.status = :ieStatus",
                        [sub: subscription, acceptStat: RDStore.IE_ACCEPT_STATUS_FIXED, ieStatus: RDStore.TIPP_STATUS_CURRENT])
                : []
        ies.sort {it.tipp.title.title}
        ies
    }

    Set<String> getSubjects(List titleIDs) {
        //println(titleIDs)
        Set<String> subjects = []

        if(titleIDs){
            subjects = BookInstance.executeQuery("select distinct(summaryOfContent) from BookInstance where summaryOfContent is not null and id in (:titleIDs)", [titleIDs: titleIDs])
            //println("Moe!")
            //println(BookInstance.executeQuery("select bk.summaryOfContent from BookInstance as bk where bk.id in (15415, 15368, 15838)"))
        }

        //println(subjects)
        subjects

    }

    List getCurrentIssueEntitlements(Subscription subscription) {
        List<IssueEntitlement> ies = subscription?
                IssueEntitlement.executeQuery("select ie from IssueEntitlement as ie where ie.subscription = :sub and ie.status = :cur",
                        [sub: subscription, cur: RDStore.TIPP_STATUS_CURRENT])
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


    boolean copyOwner(Subscription sourceSub, Subscription targetSub, def flash) {
        //Vertrag/License
        targetSub.owner = sourceSub.owner ?: null
        return save(targetSub, flash)
    }


    boolean deleteOrgRelations(List<OrgRole> toDeleteOrgRelations, Subscription targetSub, def flash) {
        OrgRole.executeUpdate(
                "delete from OrgRole o where o in (:orgRelations) and o.sub = :sub and o.roleType not in (:roleTypes)",
                [orgRelations: toDeleteOrgRelations, sub: targetSub, roleTypes: [OR_SUBSCRIPTION_CONSORTIA, OR_SUBSCRIBER_CONS, OR_SUBSCRIBER]]
        )
    }


    boolean copyOrgRelations(List<OrgRole> toCopyOrgRelations, Subscription sourceSub, Subscription targetSub, def flash) {
        sourceSub.orgRelations?.each { or ->
            if (or in toCopyOrgRelations && !(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial', 'Subscription Consortia'])) {
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

            packagesToDelete.each { subPkg ->
                OrgAccessPointLink.executeUpdate("delete from OrgAccessPointLink oapl where oapl.subPkg=?", [subPkg])
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
            } else {

                List<OrgAccessPointLink> pkgOapls = OrgAccessPointLink.findAllByIdInList(subscriptionPackage.oapls.id)
                Set<PendingChangeConfiguration> pkgPendingChangeConfig = PendingChangeConfiguration.findAllByIdInList(subscriptionPackage.pendingChangeConfig.id)
                subscriptionPackage.properties.oapls = null
                subscriptionPackage.properties.pendingChangeConfig = null
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
                    pkgPendingChangeConfig.each { PendingChangeConfiguration config ->
                        Map<String,Object> configSettings = [subscriptionPackage:newSubscriptionPackage,settingValue:config.settingValue,settingKey:config.settingKey,withNotification:config.withNotification]
                        PendingChangeConfiguration newPcc = PendingChangeConfiguration.construct(configSettings)
                        if(newPcc) {
                            Set<AuditConfig> auditables = AuditConfig.findAllByReferenceClassAndReferenceIdAndReferenceFieldInList(subscriptionPackage.subscription.class.name,subscriptionPackage.subscription.id,PendingChangeConfiguration.settingKeys)
                            auditables.each { audit ->
                                AuditConfig.addConfig(targetSub,audit.referenceField)
                            }
                        }
                    }
                }
            }
        }
    }


    boolean deleteEntitlements(List<IssueEntitlement> entitlementsToDelete, Subscription targetSub, def flash) {
        entitlementsToDelete.each {
            it.status = TIPP_STATUS_DELETED
            save(it, flash)
        }
//        IssueEntitlement.executeUpdate(
//                "delete from IssueEntitlement ie where ie in (:entitlementsToDelete) and ie.subscription = :sub ",
//                [entitlementsToDelete: entitlementsToDelete, sub: targetSub])
    }


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
                    newIssueEntitlement.coverages = null
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
                            isMultiYear: subMember.isMultiYear,
                            type: subMember.type,
                            kind: subMember.kind,
                            status: targetSub.status,
                            name: subMember.name,
                            startDate: subMember.isMultiYear ? subMember.startDate : targetSub.startDate,
                            endDate: subMember.isMultiYear ? subMember.endDate : targetSub.endDate,
                            manualRenewalDate: subMember.manualRenewalDate,
                            /* manualCancellationDate: result.subscriptionInstance.manualCancellationDate, */
                            identifier: java.util.UUID.randomUUID().toString(),
                            instanceOf: targetSub?.id,
                            //previousSubscription: subMember?.id,
                            isSlaved: subMember.isSlaved,
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
                            def pkgOapls = pkg.oapls
                            pkg.properties.oapls = null
                            pkg.properties.pendingChangeConfig = null
                            SubscriptionPackage newSubscriptionPackage = new SubscriptionPackage()
                            InvokerHelper.setProperties(newSubscriptionPackage, pkg.properties)
                            newSubscriptionPackage.subscription = newSubscription

                            if(newSubscriptionPackage.save()){
                                pkgOapls.each{ oapl ->

                                    def oaplProperties = oapl.properties
                                    oaplProperties.globalUID = null
                                    OrgAccessPointLink newOrgAccessPointLink = new OrgAccessPointLink()
                                    InvokerHelper.setProperties(newOrgAccessPointLink, oaplProperties)
                                    newOrgAccessPointLink.subPkg = newSubscriptionPackage
                                    newOrgAccessPointLink.save(flush: true)
                                }
                            }
                        }
                    }
                    if (subMember.issueEntitlements && targetSub.issueEntitlements) {
                        subMember.issueEntitlements?.each { ie ->
                            if (ie.status != RDStore.TIPP_STATUS_DELETED) {
                                def ieProperties = ie.properties
                                ieProperties.globalUID = null

                                IssueEntitlement newIssueEntitlement = new IssueEntitlement()
                                InvokerHelper.setProperties(newIssueEntitlement, ieProperties)
                                newIssueEntitlement.coverages = null
                                newIssueEntitlement.subscription = newSubscription

                                if(save(newIssueEntitlement, flash)){
                                    ie.properties.coverages.each{ coverage ->

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

                    //OrgRole
                    subMember.orgRelations?.each { or ->
                        if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType in [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]) || (targetSub.orgRelations.size() >= 1)) {
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
                if (task.status != TASK_STATUS_DONE) {
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
        [del: DOC_CTX_STATUS_DELETED, ids: toDeleteDocs])
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

                        String fPath = grailsApplication.config.documentStorageLocation ?: '/tmp/laser'

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


    boolean copyProperties(List<AbstractProperty> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        Org contextOrg = contextService.getOrg()
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
                targetProp = sourceProp.copyInto(targetProp)
                save(targetProp, flash)
                if (((sourceProp.id.toString() in auditProperties)) && targetProp instanceof CustomProperty) {
                    //copy audit
                    if (!AuditConfig.getConfig(targetProp, AuditConfig.COMPLETE_OBJECT)) {
                        def auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(SubscriptionCustomProperty.class.name, sourceProp.id)
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
                Object[] args = [sourceProp?.type?.getI10n("name") ?: sourceProp.class.getSimpleName()]
                flash.error += messageSource.getMessage('subscription.err.alreadyExistsInTargetSub', args, locale)
            }
        }
    }


    boolean deleteProperties(List<AbstractProperty> properties, Subscription targetSub, boolean isRenewSub, def flash, List auditProperties){
        if (true){
            properties.each { AbstractProperty prop ->
                AuditConfig.removeAllConfigs(prop)
            }
        }
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
        if (obj.save()){
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

    boolean addEntitlement(sub, gokbId, issueEntitlementOverwrite, withPriceData, acceptStatus) throws EntitlementCreationException {
        TitleInstancePackagePlatform tipp = TitleInstancePackagePlatform.findByGokbId(gokbId)
        if (tipp == null) {
            throw new EntitlementCreationException("Unable to tipp ${gokbId}")
        }
        else if(IssueEntitlement.findAllBySubscriptionAndTippAndStatus(sub, tipp, TIPP_STATUS_CURRENT))
            {
                throw new EntitlementCreationException("Unable to create IssueEntitlement because IssueEntitlement exist with tipp ${gokbId}")
        } else {
            IssueEntitlement new_ie = new IssueEntitlement(
					status: tipp.status,
                    subscription: sub,
                    tipp: tipp,
                    accessStartDate: issueEntitlementOverwrite?.accessStartDate ? DateUtil.parseDateGeneric(issueEntitlementOverwrite.accessStartDate) : tipp.accessStartDate,
                    accessEndDate: issueEntitlementOverwrite?.accessEndDate ? DateUtil.parseDateGeneric(issueEntitlementOverwrite.accessEndDate) : tipp.accessEndDate,
                    ieReason: 'Manually Added by User',
                    acceptStatus: acceptStatus)
            if (new_ie.save()) {
                Set coverageStatements
                Set fallback = tipp.coverages
                if(issueEntitlementOverwrite?.coverages) {
                    coverageStatements = issueEntitlementOverwrite.coverages
                }
                else {
                    coverageStatements = fallback
                }
                coverageStatements.eachWithIndex { covStmt, int c ->
                    IssueEntitlementCoverage ieCoverage = new IssueEntitlementCoverage(
                            startDate: covStmt.startDate ?: fallback[c]?.startDate,
                            startVolume: covStmt.startVolume ?: fallback[c]?.startVolume,
                            startIssue: covStmt.startIssue ?: fallback[c]?.startIssue,
                            endDate: covStmt.endDate ?: fallback[c]?.endDate,
                            endVolume: covStmt.endVolume ?: fallback[c]?.endVolume,
                            endIssue: covStmt.endIssue ?: fallback[c]?.endIssue,
                            coverageDepth: covStmt.coverageDepth ?: fallback[c]?.coverageDepth,
                            coverageNote: covStmt.coverageNote ?: fallback[c]?.coverageNote,
                            embargo: covStmt.embargo ?: fallback[c]?.embargo,
                            issueEntitlement: new_ie
                    )
                    if(!ieCoverage.save()) {
                        throw new EntitlementCreationException(ieCoverage.getErrors())
                    }
                }
                if(withPriceData && issueEntitlementOverwrite) {
                    if(issueEntitlementOverwrite instanceof IssueEntitlement && issueEntitlementOverwrite.priceItem) {
                        PriceItem pi = new PriceItem(priceDate: issueEntitlementOverwrite.priceItem.priceDate ?: null,
                                listPrice: issueEntitlementOverwrite.priceItem.listPrice ?: null,
                                listCurrency: issueEntitlementOverwrite.priceItem.listCurrency ?: null,
                                localPrice: issueEntitlementOverwrite.priceItem.localPrice ?: null,
                                localCurrency: issueEntitlementOverwrite.priceItem.localCurrency ?: null,
                                issueEntitlement: new_ie
                        )
                        pi.setGlobalUID()
                        if(pi.save())
                            return true
                        else {
                            throw new EntitlementCreationException(pi.errors)
                        }

                    }
                    else {

                        PriceItem pi = new PriceItem(priceDate: DateUtil.parseDateGeneric(issueEntitlementOverwrite.priceDate),
                                listPrice: issueEntitlementOverwrite.listPrice,
                                listCurrency: RefdataValue.getByValueAndCategory(issueEntitlementOverwrite.listCurrency, 'Currency'),
                                localPrice: issueEntitlementOverwrite.localPrice,
                                localCurrency: RefdataValue.getByValueAndCategory(issueEntitlementOverwrite.localCurrency, 'Currency'),
                                issueEntitlement: new_ie
                        )
                        pi.setGlobalUID()
                        if(pi.save())
                            return true
                        else {
                            throw new EntitlementCreationException(pi.errors)
                        }
                    }

                }
                else return true
            } else {
                throw new EntitlementCreationException(new_ie.errors)
            }
        }
    }

    boolean deleteEntitlement(sub, gokbId) {
        IssueEntitlement ie = IssueEntitlement.findWhere(tipp: TitleInstancePackagePlatform.findByGokbId(gokbId), subscription: sub)
        if(ie == null) {
            return false
        }
        else {
            ie.status = TIPP_STATUS_DELETED
            if(ie.save()) {
                return true
            }
            else{
                return false
            }
        }
    }

    boolean deleteEntitlementbyID(sub, id) {
        IssueEntitlement ie = IssueEntitlement.findWhere(id: Long.parseLong(id), subscription: sub)
        if(ie == null) {
            return false
        }
        else {
            ie.status = TIPP_STATUS_DELETED
            if(ie.save()) {
                return true
            }
            else{
                return false
            }
        }
    }

    static boolean rebaseSubscriptions(TitleInstancePackagePlatform tipp, TitleInstancePackagePlatform replacement) {
        try {
            Map<String,TitleInstancePackagePlatform> rebasingParams = [old:tipp,replacement:replacement]
            IssueEntitlement.executeUpdate('update IssueEntitlement ie set ie.tipp = :replacement where ie.tipp = :old',rebasingParams)
            Identifier.executeUpdate('update Identifier i set i.tipp = :replacement where i.tipp = :old',rebasingParams)
            TIPPCoverage.executeUpdate('update TIPPCoverage tc set tc.tipp = :replacement where tc.tipp = :old',rebasingParams)
            return true
        }
        catch (Exception e) {
            println 'error while rebasing TIPP ... rollback!'
            println e.message
            return false
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
            parentSubType = [SUBSCRIPTION_TYPE_LOCAL.getI10n('value')]
        }
        Map colMap = [:]
        Map<String, Map> propMap = [:]
        Map candidates = [:]
        InputStream fileContent = tsvFile.getInputStream()
        List<String> rows = fileContent.text.split('\n')
        List<String> ignoredColHeads = [], multiplePropDefs = []
        rows[0].split('\t').eachWithIndex { String s, int c ->
            String headerCol = s.trim()
            if(headerCol.startsWith("\uFEFF"))
                headerCol = headerCol.substring(1)
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
                case "parent subscription":
                case "consortial subscription":
                    if(accessService.checkPerm("ORG_INST_COLLECTIVE, ORG_CONSORTIUM"))
                        colMap.instanceOf = c
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
                case "anbieter":
                case "provider:": colMap.provider = c
                    break
                case "lieferant":
                case "agency": colMap.agency = c
                    break
                case "anmerkungen":
                case "notes": colMap.notes = c
                    break
                default:
                    //check if property definition
                    boolean isNotesCol = false
                    String propDefString = headerCol.toLowerCase()
                    if(headerCol.contains('$$notes')) {
                        isNotesCol = true
                        propDefString = headerCol.split('\\$\\$')[0].toLowerCase()
                    }
                    Map queryParams = [propDef:propDefString,pdClass:PropertyDefinition.class.name,contextOrg:contextOrg]
                    List<PropertyDefinition> possiblePropDefs = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd, I10nTranslation i where i.referenceId = pd.id and i.referenceClass = :pdClass and (lower(i.valueDe) = :propDef or lower(i.valueEn) = :propDef) and (pd.tenant = :contextOrg or pd.tenant = null)",queryParams)
                    if(possiblePropDefs.size() == 1) {
                        PropertyDefinition propDef = possiblePropDefs[0]
                        if(isNotesCol) {
                            propMap[propDef.class.name+':'+propDef.id].notesColno = c
                        }
                        else {
                            String refCategory = ""
                            if(propDef.type == RefdataValue.toString()) {
                                refCategory = propDef.refdataCategory
                            }
                            Map<String,Integer> defPair = [colno:c,refCategory:refCategory]
                            propMap[propDef.class.name+':'+propDef.id] = [definition:defPair]
                        }
                    }
                    else if(possiblePropDefs.size() > 1)
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
                String statusKey = cols[colMap.status].trim()
                if(statusKey) {
                    String status = refdataService.retrieveRefdataValueOID(statusKey, RDConstants.SUBSCRIPTION_STATUS)
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
                String name = cols[colMap.name].trim()
                if(name)
                    candidate.name = name
            }
            //owner(nullable:true, blank:false) -> to license
            if(colMap.owner != null) {
                String ownerKey = cols[colMap.owner].trim()
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
                String typeKey = cols[colMap.type].trim()
                if(typeKey) {
                    String type = refdataService.retrieveRefdataValueOID(typeKey, RDConstants.SUBSCRIPTION_TYPE)
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
                String formKey = cols[colMap.form].trim()
                if(formKey) {
                    String form = refdataService.retrieveRefdataValueOID(formKey, RDConstants.SUBSCRIPTION_FORM)
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
                String resourceKey = cols[colMap.resource].trim()
                if(resourceKey) {
                    String resource = refdataService.retrieveRefdataValueOID(resourceKey,RDConstants.SUBSCRIPTION_RESOURCE)
                    if(resource) {
                        candidate.resource = resource
                    }
                    else {
                        mappingErrorBag.noValidResource = resourceKey
                    }
                }
            }
            //provider
            if(colMap.provider != null) {
                String providerIdCandidate = cols[colMap.provider]?.trim()
                if(providerIdCandidate) {
                    Long idCandidate = providerIdCandidate.isLong() ? Long.parseLong(providerIdCandidate) : null
                    Org provider = Org.findByIdOrGlobalUID(idCandidate,providerIdCandidate)
                    if(provider)
                        candidate.provider = "${provider.class.name}:${provider.id}"
                    else {
                        mappingErrorBag.noValidOrg = providerIdCandidate
                    }
                }
            }
            //agency
            if(colMap.agency != null) {
                String agencyIdCandidate = cols[colMap.agency]?.trim()
                if(agencyIdCandidate) {
                    Long idCandidate = agencyIdCandidate.isLong() ? Long.parseLong(agencyIdCandidate) : null
                    Org agency = Org.findByIdOrGlobalUID(idCandidate,agencyIdCandidate)
                    if(agency)
                        candidate.agency = "${agency.class.name}:${agency.id}"
                    else {
                        mappingErrorBag.noValidOrg = agencyIdCandidate
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
                startDate = DateUtil.parseDateGeneric(cols[colMap.startDate].trim())
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
                endDate = DateUtil.parseDateGeneric(cols[colMap.endDate].trim())
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
                Date manualCancellationDate = DateUtil.parseDateGeneric(cols[colMap.manualCancellationDate])
                if(manualCancellationDate)
                    candidate.manualCancellationDate = manualCancellationDate
            }
            //instanceOf(nullable:true, blank:false)
            if(colMap.instanceOf != null && colMap.member != null) {
                String idCandidate = cols[colMap.instanceOf].trim()
                String memberIdCandidate = cols[colMap.member].trim()
                if(idCandidate && memberIdCandidate) {
                    List<Subscription> parentSubs = Subscription.executeQuery("select oo.sub from OrgRole oo where oo.org = :contextOrg and oo.roleType in :roleTypes and :idCandidate in (cast(oo.sub.id as string),oo.sub.globalUID)",[contextOrg: contextOrg, roleTypes: [OR_SUBSCRIPTION_CONSORTIA, OR_SUBSCRIPTION_COLLECTIVE], idCandidate: idCandidate])
                    // TODO [ticket=1789]
                    //  List<Org> possibleOrgs = Org.executeQuery("select distinct idOcc.org from IdentifierOccurrence idOcc, Combo c join idOcc.identifier id where c.fromOrg = idOcc.org and :idCandidate in (cast(idOcc.org.id as string),idOcc.org.globalUID) or (id.value = :idCandidate and id.ns = :wibid) and c.toOrg = :contextOrg and c.type = :type",[idCandidate:memberIdCandidate,wibid:IdentifierNamespace.findByNs('wibid'),contextOrg: contextOrg,type: comboType])
                    List<Org> possibleOrgs = Org.executeQuery("select distinct ident.org from Identifier ident, Combo c where c.fromOrg = ident.org and :idCandidate in (cast(ident.org.id as string), ident.org.globalUID) or (ident.value = :idCandidate and ident.ns = :wibid) and c.toOrg = :contextOrg and c.type = :type", [idCandidate:memberIdCandidate,wibid:IdentifierNamespace.findByNs('wibid'),contextOrg: contextOrg,type: comboType])
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
            propMap.each { String k, Map propInput ->
                Map defPair = propInput.definition
                Map propData = [:]
                if(cols[defPair.colno]) {
                    def v
                    if(defPair.refCategory) {
                        v = refdataService.retrieveRefdataValueOID(cols[defPair.colno].trim(),defPair.refCategory)
                        if(!v) {
                            mappingErrorBag.propValNotInRefdataValueSet = [cols[defPair.colno].trim(),defPair.refCategory]
                        }
                    }
                    else v = cols[defPair.colno]
                    propData.propValue = v
                }
                if(propInput.notesColno)
                    propData.propNote = cols[propInput.notesColno].trim()
                candidate.properties[k] = propData
            }
            //notes
            if(colMap.notes != null && cols[colMap.notes].trim()) {
                candidate.notes = cols[colMap.notes].trim()
            }
            candidates.put(candidate,mappingErrorBag)
        }
        [candidates: candidates, globalErrors: globalErrors, parentSubType: parentSubType]
    }

}