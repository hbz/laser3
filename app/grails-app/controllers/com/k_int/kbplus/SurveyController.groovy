package com.k_int.kbplus

import com.k_int.kbplus.abstract_domain.AbstractProperty
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import de.laser.AccessService
import de.laser.AuditConfig
import de.laser.helper.DateUtil
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import de.laser.interfaces.TemplateSupport
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.codehaus.groovy.runtime.InvokerHelper
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.ServletOutputStream
import java.text.DateFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat


@Secured(['IS_AUTHENTICATED_FULLY'])
class SurveyController {

    def springSecurityService
    def accessService
    def contextService
    def subscriptionsQueryService
    def filterService
    def docstoreService
    def orgTypeService
    def genericOIDService
    def surveyService
    def financeService
    def exportService
    def taskService
    def subscriptionService
    def comparisonService
    def surveyUpdateService
    def escapeService
    def titleStreamService
    def institutionsService

    public static final String WORKFLOW_DATES_OWNER_RELATIONS = '1'
    public static final String WORKFLOW_PACKAGES_ENTITLEMENTS = '5'
    public static final String WORKFLOW_DOCS_ANNOUNCEMENT_TASKS = '2'
    public static final String WORKFLOW_SUBSCRIBER = '3'
    public static final String WORKFLOW_PROPERTIES = '4'
    public static final String WORKFLOW_END = '6'

    def possible_date_formats = [
            new SimpleDateFormat('yyyy/MM/dd'),
            new SimpleDateFormat('dd.MM.yyyy'),
            new SimpleDateFormat('dd/MM/yyyy'),
            new SimpleDateFormat('dd/MM/yy'),
            new SimpleDateFormat('yyyy/MM'),
            new SimpleDateFormat('yyyy')
    ]

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def currentSurveysConsortia() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        params.max = result.max
        params.offset = result.offset

        params.tab = params.tab ?: 'created'

        DateFormat sdFormat = new DateUtil().getSimpleDateFormat_NoTime()
        def fsq = filterService.getSurveyConfigQueryConsortia(params, sdFormat, result.institution)

        result.surveys = SurveyInfo.executeQuery(fsq.query, fsq.queryParams, params)
        result.countSurveyConfigs = getSurveyConfigCounts()

        result
    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def createGeneralSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processCreateGeneralSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()
        def surveyInfo = new SurveyInfo(
                name: params.name,
                startDate: params.startDate ? sdf.parse(params.startDate) : null,
                endDate: params.endDate ? sdf.parse(params.endDate) : null,
                type: params.type,
                owner: contextService.getOrg(),
                status: RDStore.SURVEY_IN_PROCESSING,
                comment: params.comment ?: null,
                isSubscriptionSurvey: false
        )

        if (!(surveyInfo.save(flush: true))) {
            flash.error = g.message(code: "createGeneralSurvey.create.fail")
            redirect(url: request.getHeader('referer'))
        }
        flash.message = g.message(code: "createGeneralSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def createSubscriptionSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        def date_restriction = null;
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        List<Org> providers = orgTypeService.getCurrentProviders(contextService.getOrg())
        List<Org> agencies = orgTypeService.getCurrentAgencies(contextService.getOrg())

        providers.addAll(agencies)
        List orgIds = providers.unique().collect { it2 -> it2.id }

        result.providers = Org.findAllByIdInList(orgIds).sort { it?.name }

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result.filterSet = tmpQ[2]
        List subscriptions = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        //,[max: result.max, offset: result.offset]

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.org)

        if (params.sort && params.sort.indexOf("§") >= 0) {
            switch (params.sort) {
                case "orgRole§provider":
                    subscriptions.sort { x, y ->
                        String a = x.getProviders().size() > 0 ? x.getProviders().first().name : ''
                        String b = y.getProviders().size() > 0 ? y.getProviders().first().name : ''
                        a.compareToIgnoreCase b
                    }
                    if (params.order.equals("desc"))
                        subscriptions.reverse(true)
                    break
            }
        }
        result.num_sub_rows = subscriptions.size()
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def createIssueEntitlementsSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        def date_restriction = null;
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_INTENDED.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        List<Org> providers = orgTypeService.getCurrentProviders(contextService.getOrg())
        List<Org> agencies = orgTypeService.getCurrentAgencies(contextService.getOrg())

        providers.addAll(agencies)
        List orgIds = providers.unique().collect { it2 -> it2.id }

        result.providers = Org.findAllByIdInList(orgIds).sort { it?.name }

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        result.filterSet = tmpQ[2]
        List subscriptions = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        //,[max: result.max, offset: result.offset]

        result.propList = PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.SUB_PROP], contextService.org)

        if (params.sort && params.sort.indexOf("§") >= 0) {
            switch (params.sort) {
                case "orgRole§provider":
                    subscriptions.sort { x, y ->
                        String a = x.getProviders().size() > 0 ? x.getProviders().first().name : ''
                        String b = y.getProviders().size() > 0 ? y.getProviders().first().name : ''
                        a.compareToIgnoreCase b
                    }
                    if (params.order.equals("desc"))
                        subscriptions.reverse(true)
                    break
            }
        }
        result.num_sub_rows = subscriptions.size()
        result.subscriptions = subscriptions.drop((int) result.offset).take((int) result.max)

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSubtoSubscriptionSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.subscription = Subscription.get(Long.parseLong(params.sub))
        if (!result.subscription) {
            redirect action: 'createSubscriptionSurvey'
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSubtoIssueEntitlementsSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.subscription = Subscription.get(Long.parseLong(params.sub))
        result.pickAndChoose = true
        if (!result.subscription) {
            redirect action: 'createIssueEntitlementsSurvey'
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processCreateSubscriptionSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        def surveyInfo = new SurveyInfo(
                name: params.name,
                startDate: params.startDate ? sdf.parse(params.startDate) : null,
                endDate: params.endDate ? sdf.parse(params.endDate) : null,
                type: params.type,
                owner: contextService.getOrg(),
                status: RDStore.SURVEY_IN_PROCESSING,
                comment: params.comment ?: null,
                isSubscriptionSurvey: true
        )

        if (!(surveyInfo.save(flush: true))) {
            flash.error = g.message(code: "createSubscriptionSurvey.create.fail")
            redirect(url: request.getHeader('referer'))
        }

        def subscription = Subscription.get(Long.parseLong(params.sub))
        def surveyConfig = subscription ? SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo) : null
        if (!surveyConfig && subscription) {
            surveyConfig = new SurveyConfig(
                    subscription: subscription,
                    configOrder: surveyInfo?.surveyConfigs?.size() ? surveyInfo?.surveyConfigs?.size() + 1 : 1,
                    type: 'Subscription',
                    surveyInfo: surveyInfo,
                    isSubscriptionSurveyFix: SurveyConfig.findAllBySubscriptionAndIsSubscriptionSurveyFix(subscription, true) ? false : (params.isSubscriptionSurveyFix ? true : false)

            )

            surveyConfig.save(flush: true)

            //Wenn es eine Umfrage schon gibt, die als Übertrag dient. Dann ist es auch keine Lizenz Umfrage mit einem Teilname-Merkmal abfragt!
            if (!SurveyConfig.findAllBySubscriptionAndIsSubscriptionSurveyFix(subscription, true)) {
                def configProperty = new SurveyConfigProperties(
                        surveyProperty: SurveyProperty.findByName('Participation'),
                        surveyConfig: surveyConfig)
                if (configProperty.save(flush: true)) {
                    addSubMembers(surveyConfig)
                }
            } else {
                addSubMembers(surveyConfig)
            }


        } else {
            surveyInfo.delete(flush: true)
            flash.error = g.message(code: "createSubscriptionSurvey.create.fail")
            redirect(url: request.getHeader('referer'))
        }

        flash.message = g.message(code: "createSubscriptionSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processCreateIssueEntitlementsSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        def surveyInfo = new SurveyInfo(
                name: params.name,
                startDate: params.startDate ? sdf.parse(params.startDate) : null,
                endDate: params.endDate ? sdf.parse(params.endDate) : null,
                type: RefdataValue.getByValueAndCategory('selection','Survey Type'),
                owner: contextService.getOrg(),
                status: RDStore.SURVEY_IN_PROCESSING,
                comment: params.comment ?: null,
                isSubscriptionSurvey: true
        )

        if (!(surveyInfo.save(flush: true))) {
            flash.error = g.message(code: "createSubscriptionSurvey.create.fail")
            redirect(url: request.getHeader('referer'))
        }

        def subscription = Subscription.get(Long.parseLong(params.sub))
        def surveyConfig = subscription ? SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo) : null
        if (!surveyConfig && subscription) {
            surveyConfig = new SurveyConfig(
                    subscription: subscription,
                    configOrder: surveyInfo?.surveyConfigs?.size() ? surveyInfo?.surveyConfigs?.size() + 1 : 1,
                    type: 'Subscription',
                    surveyInfo: surveyInfo,
                    isSubscriptionSurveyFix: false,
                    pickAndChoose: true

            )

            surveyConfig.save(flush: true)

            addSubMembers(surveyConfig)

        } else {
            surveyInfo.delete(flush: true)
            flash.error = g.message(code: "createIssueEntitlementsSurvey.create.fail")
            redirect(url: request.getHeader('referer'))
        }

        flash.message = g.message(code: "createIssueEntitlementsSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def show() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyConfigs = result.surveyInfo?.surveyConfigs?.sort { it?.configOrder }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyTitles() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.contextOrg = contextService.getOrg()

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        String base_qry = null
        Map<String,Object> qry_params = [subscription: result.surveyConfig?.subscription]



        def date_filter
        date_filter = new Date()
        result.as_at_date = date_filter
        base_qry = " from IssueEntitlement as ie where ie.subscription = :subscription "
        base_qry += " and (( :startDate >= coalesce(ie.accessStartDate,subscription.startDate) ) OR ( ie.accessStartDate is null )) and ( ( :endDate <= coalesce(ie.accessEndDate,subscription.endDate) ) OR ( ie.accessEndDate is null ) ) "
        qry_params.startDate = date_filter
        qry_params.endDate = date_filter


        base_qry += " and ie.status = :current "
        qry_params.current = RDStore.TIPP_STATUS_CURRENT

        base_qry += "order by lower(ie.tipp.title.title) asc"

        result.num_sub_rows = IssueEntitlement.executeQuery("select ie.id " + base_qry, qry_params).size()

        result.entitlements = IssueEntitlement.executeQuery("select ie " + base_qry, qry_params, [max: result.max, offset: result.offset])

        result

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyConfigs() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyProperties = SurveyProperty.findAllByOwner(result.institution)

        result.properties = getSurveyProperties(result.institution)

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        result.surveyConfigs = result.surveyInfo.surveyConfigs.sort { it?.getConfigNameShort() }

        result

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyConfigDocs() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyParticipants() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        // new: filter preset
        params.orgType = RDStore.OT_INSTITUTION?.id?.toString()
        params.orgSector = RDStore.O_SECTOR_HIGHER_EDU?.id?.toString()

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.org)

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        def fsq = filterService.getOrgComboQuery(params, result.institution)
        def tmpQuery = "select o.id " + fsq.query.minus("select o ")
        def consortiaMemberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

        if (params.filterPropDef && consortiaMemberIds) {
            fsq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids)", 'o', [oids: consortiaMemberIds])
        }
        result.consortiaMembers = Org.executeQuery(fsq.query, fsq.queryParams, params)
        result.consortiaMembersCount = Org.executeQuery(fsq.query, fsq.queryParams).size()

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

        params.surveyConfigID = params.surveyConfigID ?: result?.surveyConfigs[0]?.id?.toString()

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        def surveyOrgs = result.surveyConfig?.getSurveyOrgsIDs()

        result.selectedParticipants = getfilteredSurveyOrgs(surveyOrgs.orgsWithoutSubIDs, fsq.query, fsq.queryParams, params)
        result.selectedSubParticipants = getfilteredSurveyOrgs(surveyOrgs.orgsWithSubIDs, fsq.query, fsq.queryParams, params)

        params.tab = params.tab ?: (result.surveyConfig.type == 'Subscription' ? 'selectedSubParticipants' : 'selectedParticipants')

        result

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyCostItems() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        params.tab = params.tab ?: 'selectedSubParticipants'

        // new: filter preset
        params.orgType = RDStore.OT_INSTITUTION?.id?.toString()
        params.orgSector = RDStore.O_SECTOR_HIGHER_EDU?.id?.toString()

        result.propList = PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.org)

        params.comboType = RDStore.COMBO_TYPE_CONSORTIUM.value
        def fsq = filterService.getOrgComboQuery(params, result.institution)
        def tmpQuery = "select o.id " + fsq.query.minus("select o ")
        def consortiaMemberIds = Org.executeQuery(tmpQuery, fsq.queryParams)

        if (params.filterPropDef && consortiaMemberIds) {
            fsq = propertyService.evalFilterQuery(params, "select o FROM Org o WHERE o.id IN (:oids)", 'o', [oids: consortiaMemberIds])
        }

        result.editable = (result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        //Only SurveyConfigs with Subscriptions
        result.surveyConfigs = result.surveyInfo?.surveyConfigs.findAll { it.subscription != null }?.sort {
            it?.configOrder
        }

        params.surveyConfigID = params.surveyConfigID ?: result?.surveyConfigs[0]?.id?.toString()

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        def surveyOrgs = result.surveyConfig?.getSurveyOrgsIDs()

        result.selectedParticipants = getfilteredSurveyOrgs(surveyOrgs.orgsWithoutSubIDs, fsq.query, fsq.queryParams, params)
        result.selectedSubParticipants = getfilteredSurveyOrgs(surveyOrgs.orgsWithSubIDs, fsq.query, fsq.queryParams, params)

        result.selectedCostItemElement = params.selectedCostItemElement ?: RefdataValue.getByValueAndCategory('price: consortial price', 'CostItemElement').id.toString()

        if (params.selectedCostItemElement) {
            params.remove('selectedCostItemElement')
        }
        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyConfigFinish() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyConfig.configFinish = params.configFinish ?: false
        if (result.surveyConfig.save(flush: true)) {
            flash.message = g.message(code: 'survey.change.successfull')
        } else {
            flash.error = g.message(code: 'survey.change.fail')
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyCostItemsFinish() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyConfig.costItemsFinish = params.costItemsFinish ?: false

        if (result.surveyConfig.save(flush: true)) {
            flash.message = g.message(code: 'survey.change.successfull')
        } else {
            flash.error = g.message(code: 'survey.change.fail')
        }

        redirect(url: request.getHeader('referer'))

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyEvaluation() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        params.tab = params.tab ?: 'surveyConfigsView'

        result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

        def orgs = result.surveyConfigs?.orgs.org.flatten().unique { a, b -> a.id <=> b.id }
        result.participants = orgs.sort { it.sortname }

        result.participantsNotFinish = SurveyResult.findAllBySurveyConfigInListAndFinishDateIsNull(result.surveyConfigs)?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }.sort {
            it.sortname
        }
        result.participantsFinish = SurveyResult.findAllBySurveyConfigInListAndFinishDateIsNotNull(result.surveyConfigs)?.participant?.flatten()?.unique { a, b -> a.id <=> b.id }.sort {
            it.sortname
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyTitlesEvaluation() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        def orgs = result.surveyConfig?.orgs.org.flatten().unique { a, b -> a.id <=> b.id }
        result.participants = orgs.sort { it.sortname }

        result.participantsNotFinish = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfig(result.surveyConfig)?.org.flatten().unique { a, b -> a.id <=> b.id }?.sort {
            it?.sortname
        }
        result.participantsFinish = SurveyOrg.findAllByFinishDateIsNotNullAndSurveyConfig(result.surveyConfig)?.org.flatten().unique { a, b -> a.id <=> b.id }?.sort {
            it?.sortname
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def showEntitlementsRenew() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)
        result.participant = params.participant ? Org.get(params.participant) : null

        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.editable = result.surveyInfo?.isEditable() ?: false

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

        result.subscriptionInstance =  result.surveyConfig?.subscription

        result.subscriptionParticipant = result.surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(result.participant)

        result.ies = subscriptionService.getCurrentIssueEntitlements(result.subscriptionParticipant)

        def filename = "renewEntitlements_${escapeService.escapeString(result.subscriptionInstance.dropdownNamingConvention(result.participant))}"

        if (params.exportKBart) {
            response.setHeader("Content-disposition", "attachment; filename=${filename}.tsv")
            response.contentType = "text/tsv"
            ServletOutputStream out = response.outputStream
            Map<String, List> tableData = titleStreamService.generateTitleExportList(result.ies)
            out.withWriter { writer ->
                writer.write(exportService.generateSeparatorTableString(tableData.titleRow, tableData.columnData, '\t'))
            }
            out.flush()
            out.close()
        }else if(params.exportXLS) {
            response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xlsx\"")
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            List titles = [
                    g.message(code:'title'),
                    g.message(code:'tipp.volume'),
                    g.message(code:'author.slash.editor'),
                    g.message(code:'title.editionStatement.label'),
                    g.message(code:'title.summaryOfContent.label'),
                    g.message(code:'identifier.label'),
                    g.message(code:'title.dateFirstInPrint.label'),
                    g.message(code:'title.dateFirstOnline.label'),
                    g.message(code:'default.status.label'),
                    g.message(code:'tipp.price')

            ]
            List rows = []
            result.ies?.each { ie ->
                List row = []
                row.add([field: ie?.tipp?.title?.title ?: '', style:null])
                row.add([field: ie?.tipp?.title?.volume ?: '', style:null])
                row.add([field: ie?.tipp?.title?.getEbookFirstAutorOrFirstEditor() ?: '', style:null])
                row.add([field: ie?.tipp?.title?.editionStatement ?: '', style:null])
                row.add([field: ie?.tipp?.title?.summaryOfContent ?: '', style:null])

                def identifiers = []
                ie?.tipp?.title?.ids?.sort { it?.identifier?.ns?.ns }.each{ id ->
                    identifiers << "${id.identifier.ns.ns}: ${id.identifier.value}"
                }
                row.add([field: identifiers ? identifiers.join(', ') : '', style:null])

                row.add([field: ie?.tipp?.title?.dateFirstInPrint ? g.formatDate(date: ie?.tipp?.title?.dateFirstInPrint, format: message(code: 'default.date.format.notime')): '', style:null])
                row.add([field: ie?.tipp?.title?.dateFirstOnline ? g.formatDate(date: ie?.tipp?.title?.dateFirstOnline, format: message(code: 'default.date.format.notime')): '', style:null])
                row.add([field: ie?.acceptStatus?.getI10n('value') ?: '', style:null])

                row.add([field: ie.priceItem?.listPrice ? g.formatNumber(number: ie?.priceItem?.listPrice, type: 'currency', currencySymbol: ie?.priceItem?.listCurrency, currencyCode: ie?.priceItem?.listCurrency) : '', style:null])
                row.add([field: ie.priceItem?.localPrice ? g.formatNumber(number: ie?.priceItem?.localPrice, type: 'currency', currencySymbol: ie?.priceItem?.localCurrency, currencyCode: ie?.priceItem?.localCurrency) : '', style:null])


                rows.add(row)
            }
            Map sheetData = [:]
            sheetData[g.message(code:'subscription.details.renewEntitlements.label')] = [titleRow:titles,columnData:rows]
            SXSSFWorkbook workbook = exportService.generateXLSXWorkbook(sheetData)
            workbook.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            workbook.dispose()
            return
        }
        else {
            withFormat {
                html result
            }
        }
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def openIssueEntitlementsSurveyAgain() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)
        result.participant = params.participant ? Org.get(params.participant) : null

        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.editable = result.surveyInfo?.isEditable() ?: false

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

        result.subscriptionInstance =  result.surveyConfig?.subscription

        def ies = subscriptionService.getIssueEntitlementsUnderNegotiation(result.surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(result.participant))

        ies?.each { ie ->
            ie.acceptStatus = RDStore.IE_ACCEPT_STATUS_UNDER_CONSIDERATION
            ie.save(flush: true)
        }

        surveyOrg.finishDate = null
        surveyOrg.save(flush: true)

        flash.message = message(code: 'openIssueEntitlementsSurveyAgain.info')

        redirect(action: 'showEntitlementsRenew', id: result.surveyConfig?.id, params:[participant: result.participant?.id])

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def completeIssueEntitlementsSurveyforParticipant() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)
        result.participant = params.participant ? Org.get(params.participant) : null

        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.editable = result.surveyInfo?.isEditable() ?: false

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def ies = subscriptionService.getIssueEntitlementsUnderNegotiation(result.surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(result.participant))

        ies?.each { ie ->
            ie.acceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
            ie.save(flush: true)
        }

        flash.message = message(code: 'completeIssueEntitlementsSurvey.forParticipant.info')

        redirect(action: 'showEntitlementsRenew', id: result.surveyConfig?.id, params:[participant: result.participant?.id])

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def completeIssueEntitlementsSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.editable = result.surveyInfo?.isEditable() ?: false

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def participantsFinish = SurveyOrg.findAllByFinishDateIsNotNullAndSurveyConfig(result.surveyConfig)?.org.flatten().unique { a, b -> a.id <=> b.id }

        participantsFinish?.each { org ->

            def ies = subscriptionService.getIssueEntitlementsUnderNegotiation(result.surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(org))

            ies?.each { ie ->
                ie.acceptStatus = RDStore.IE_ACCEPT_STATUS_FIXED
                ie.save(flush: true)
            }
        }

        flash.message = message(code: 'completeIssueEntitlementsSurvey.forFinishParticipant.info')

        redirect(action: 'surveyTitlesEvaluation', id: result.surveyInfo?.id, params:[surveyConfigID: result.surveyConfig?.id])

    }

    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def evaluateIssueEntitlementsSurvey() {
        def result = setResultGenericsAndCheckAccess()

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.participant = params.participant ? Org.get(params.participant) : null

        result.surveyConfig = SurveyConfig.get(params.id)
        result.surveyInfo = result.surveyConfig.surveyInfo

        result.surveyOrg = SurveyOrg.findByOrgAndSurveyConfig(result.participant, result.surveyConfig)

        result.subscriptionInstance =  result.surveyConfig?.subscription

        result.ies = subscriptionService.getCurrentIssueEntitlements(result.surveyConfig.subscription?.getDerivedSubscriptionBySubscribers(result.participant))

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def evaluationParticipantInfo() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        //params.tab = params.tab ?: 'surveyConfigsView'

        result.participant = Org.get(params.participant)

        result.surveyResult = SurveyResult.findAllByOwnerAndParticipantAndSurveyConfigInList(result.institution, result.participant, result.surveyInfo.surveyConfigs).sort {
            it?.surveyConfig?.getConfigNameShort()
        }.groupBy { it?.surveyConfig?.id }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def evaluationConfigsInfo() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.subscriptionInstance = result.surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(result.institution)


        result.surveyResult = SurveyResult.findAllByOwnerAndSurveyConfig(result.institution, result.surveyConfig).sort {
            it.participant?.sortname
        }


        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def evaluationConfigResult() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        //result.editable = (result.surveyInfo.status != RDStore.SURVEY_IN_PROCESSING) ? false : true

        result.surveyProperty = SurveyProperty.get(params.prop)

        result.surveyResult = SurveyResult.findAllByOwnerAndSurveyConfigAndType(result.institution, result.surveyConfig, result.surveyProperty).sort {
            it.participant?.sortname
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def allSurveyProperties() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.properties = getSurveyProperties(result.institution)

        result.addSurveyConfigs = params.addSurveyConfigs ?: false

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def surveyConfigsInfo() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyProperties = result.surveyConfig?.surveyProperties

        result.navigation = surveyService.getConfigNavigation(result.surveyInfo, result.surveyConfig)

        if (result.surveyConfig?.type == 'Subscription') {
            result.authorizedOrgs = result.user?.authorizedOrgs
            result.contextOrg = contextService.getOrg()
            // restrict visible for templates/links/orgLinksAsList
            result.visibleOrgRelations = []
            result.surveyConfig?.subscription?.orgRelations?.each { or ->
                if (!(or.org?.id == contextService.getOrg()?.id) && !(or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                    result.visibleOrgRelations << or
                }
            }
            result.visibleOrgRelations.sort { it.org.sortname }

            result.subscription = result.surveyConfig?.subscription ?: null
            result.subscriptionInstance = result.surveyConfig?.subscription ?: null

            //costs
            if (result.subscription.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_CONSORTIAL))
                params.view = "cons"
            else if (result.subscription.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_PARTICIPATION) && result.subscription.getConsortia().equals(result.institution))
                params.view = "consAtSubscr"
            else if (result.subscription.getCalculatedType().equals(TemplateSupport.CALCULATED_TYPE_PARTICIPATION) && !result.subscription.getConsortia().equals(result.institution))
                params.view = "subscr"
            //cost items
            //params.forExport = true
            LinkedHashMap costItems = financeService.getCostItemsForSubscription(result.subscription, params, 10, 0)
            result.costItemSums = [:]
            if (costItems.own.count > 0) {
                result.costItemSums.ownCosts = costItems.own.sums
            }
            if (costItems.cons.count > 0) {
                result.costItemSums.consCosts = costItems.cons.sums
            }
            if (costItems.subscr.count > 0) {
                result.costItemSums.subscrCosts = costItems.subscr.sums
            }
        }

        result.properties = []
        def allProperties = getSurveyProperties(result.institution)
        allProperties.each {

            if (!(it.id in result?.surveyProperties?.surveyProperty?.id)) {
                result.properties << it
            }
        }

        def contextOrg = contextService.getOrg()
        result.tasks = taskService.getTasksByResponsiblesAndObject(result.user, contextOrg, result.surveyConfig)
        def preCon = taskService.getPreconditionsWithoutTargets(contextOrg)
        result << preCon

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def changeConfigOrder() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        if (result.surveyInfo.surveyConfigs.size() > 0) {
            def surveyConfig = SurveyConfig.get(params.surveyConfigID)

            if (params.change == 'up') {

                def secoundSurveyConfig = SurveyConfig.findBySurveyInfoAndConfigOrder(result.surveyInfo, surveyConfig.configOrder - 1)
                secoundSurveyConfig.configOrder = surveyConfig.configOrder
                secoundSurveyConfig.save(flush: true)
                surveyConfig.configOrder = surveyConfig.configOrder - 1
                if (surveyConfig.save(flush: true)) {
                    flash.message = g.message(code: 'survey.change.successfull')
                } else {
                    flash.error = g.message(code: 'survey.change.fail')
                }

            }

            if (params.change == 'down') {
                def secoundSurveyConfig = SurveyConfig.findBySurveyInfoAndConfigOrder(result.surveyInfo, surveyConfig.configOrder + 1)
                secoundSurveyConfig.configOrder = surveyConfig.configOrder
                secoundSurveyConfig.save(flush: true)
                surveyConfig.configOrder = surveyConfig.configOrder + 1

                if (surveyConfig.save(flush: true)) {
                    flash.message = g.message(code: 'survey.change.successfull')
                } else {
                    flash.error = g.message(code: 'survey.change.fail')
                }
            }

        }
        redirect(url: request.getHeader('referer'))
    }

    /*@DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSurveyConfig() {

        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyInfo = SurveyInfo.get(params.id) ?: null

        if (surveyInfo) {
            if (params.subscription) {
                def subscription = Subscription.get(Long.parseLong(params.subscription))
                def surveyConfig = subscription ? SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo) : null
                if (!surveyConfig && subscription) {
                    surveyConfig = new SurveyConfig(
                            subscription: subscription,
                            configOrder: surveyInfo.surveyConfigs.size() + 1,
                            type: 'Subscription',
                            surveyInfo: surveyInfo

                    )
                    surveyConfig.save(flush: true)

                    def configProperty = new SurveyConfigProperties(
                            surveyProperty: SurveyProperty.findByName('Continue to license'),
                            surveyConfig: surveyConfig).save(flush: true)

                    flash.message = g.message(code: "surveyConfigs.add.successfully")

                } else {
                    flash.error = g.message(code: "surveyConfigs.exists")
                }
            }
            if (params.property && !params.addtoallSubs) {
                def property = SurveyProperty.get(Long.parseLong(params.property))
                def surveyConfigProp = property ? SurveyConfig.findAllBySurveyPropertyAndSurveyInfo(property, surveyInfo) : null
                if (!surveyConfigProp && property) {
                    surveyConfigProp = new SurveyConfig(
                            surveyProperty: property,
                            configOrder: surveyInfo.surveyConfigs.size() + 1,
                            type: 'SurveyProperty',
                            surveyInfo: surveyInfo

                    )
                    surveyConfigProp.save(flush: true)

                    flash.message = g.message(code: "surveyConfigs.add.successfully")

                } else {
                    flash.error = g.message(code: "surveyConfigs.exists")
                }
            }
            if (params.propertytoSub) {
                def property = SurveyProperty.get(Long.parseLong(params.propertytoSub))
                def surveyConfig = SurveyConfig.get(Long.parseLong(params.surveyConfig))

                def propertytoSub = property ? SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(property, surveyConfig) : null
                if (!propertytoSub && property && surveyConfig) {
                    propertytoSub = new SurveyConfigProperties(
                            surveyConfig: surveyConfig,
                            surveyProperty: property

                    )
                    propertytoSub.save(flush: true)

                    flash.message = g.message(code: "surveyConfigs.add.successfully")

                } else {
                    flash.error = g.message(code: "surveyConfigs.exists")
                }
            }

            if (params.property && params.addtoallSubs) {
                def property = SurveyProperty.get(Long.parseLong(params.property))

                surveyInfo.surveyConfigs.each { surveyConfig ->

                    if (surveyConfig.type == 'Subscription') {
                        def propertytoSub = property ? SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(property, surveyConfig) : null
                        if (!propertytoSub && property && surveyConfig) {
                            propertytoSub = new SurveyConfigProperties(
                                    surveyConfig: surveyConfig,
                                    surveyProperty: property

                            )
                            propertytoSub.save(flush: true)

                            flash.message = g.message(code: "surveyConfigs.add.successfully")

                        } else {
                            flash.error = g.message(code: "surveyConfigs.exists")
                        }
                    }
                }
            }


            redirect action: 'surveyConfigs', id: surveyInfo.id

        } else {
            redirect action: 'currentSurveysConsortia'
        }
    }*/

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSurveyConfigs() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }


        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.surveyInfo && result.editable) {

            if (params.selectedProperty) {

                params.list('selectedProperty').each { propertyID ->

                    if (propertyID) {
                        def property = SurveyProperty.get(Long.parseLong(propertyID))
                        //Config is Sub
                        if (params.surveyConfigID && !params.addtoallSubs) {
                            def surveyConfig = SurveyConfig.get(Long.parseLong(params.surveyConfigID))

                            def propertytoSub = property ? SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(property, surveyConfig) : null
                            if (!propertytoSub && property && surveyConfig) {
                                propertytoSub = new SurveyConfigProperties(
                                        surveyConfig: surveyConfig,
                                        surveyProperty: property

                                )
                                propertytoSub.save(flush: true)

                                flash.message = g.message(code: "surveyConfigs.add.successfully")

                            } else {
                                flash.error = g.message(code: "surveyConfigs.exists")
                            }
                        } else if (params.surveyConfigID && params.addtoallSubs) {

                            result.surveyInfo.surveyConfigs.each { surveyConfig ->

                                def propertytoSub = property ? SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(property, surveyConfig) : null
                                if (!propertytoSub && property && surveyConfig) {
                                    propertytoSub = new SurveyConfigProperties(
                                            surveyConfig: surveyConfig,
                                            surveyProperty: property

                                    )
                                    propertytoSub.save(flush: true)

                                    flash.message = g.message(code: "surveyConfigs.add.successfully")

                                } else {
                                    flash.error = g.message(code: "surveyConfigs.exists")
                                }
                            }
                        } else {
                            def surveyConfigProp = property ? SurveyConfig.findAllBySurveyPropertyAndSurveyInfo(property, result.surveyInfo) : null
                            if (!surveyConfigProp && property) {
                                surveyConfigProp = new SurveyConfig(
                                        surveyProperty: property,
                                        configOrder: result.surveyInfo.surveyConfigs.size() + 1,
                                        type: 'SurveyProperty',
                                        surveyInfo: result.surveyInfo

                                )
                                surveyConfigProp.save(flush: true)

                                flash.message = g.message(code: "surveyConfigs.add.successfully")

                            } else {
                                flash.error = g.message(code: "surveyConfigs.exists")
                            }

                        }
                    }


                }

                if (params.surveyConfigID) {
                    redirect action: 'surveyConfigsInfo', id: result.surveyInfo.id, params: [surveyConfigID: params.surveyConfigID]
                } else {
                    redirect action: 'surveyConfigs', id: result.surveyInfo.id
                }
            } else {
                redirect action: 'currentSurveysConsortia'
            }
        }
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSurveyConfig() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.surveyInfo && result.editable) {

            if (params.selectedProperty) {
                def property = genericOIDService.resolveOID(params.selectedProperty)
                //Config is Sub
                if (params.surveyConfigID && !params.addtoallSubs) {
                    def surveyConfig = SurveyConfig.get(Long.parseLong(params.surveyConfigID))

                    def propertytoSub = property ? SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(property, surveyConfig) : null
                    if (!propertytoSub && property && surveyConfig) {
                        propertytoSub = new SurveyConfigProperties(
                                surveyConfig: surveyConfig,
                                surveyProperty: property

                        )
                        propertytoSub.save(flush: true)

                        flash.message = g.message(code: "surveyConfigs.add.successfully")

                    } else {
                        flash.error = g.message(code: "surveyConfigs.exists")
                    }
                } else if (params.surveyConfigID && params.addtoallSubs) {

                    result.surveyInfo.surveyConfigs.each { surveyConfig ->

                        def propertytoSub = property ? SurveyConfigProperties.findAllBySurveyPropertyAndSurveyConfig(property, surveyConfig) : null
                        if (!propertytoSub && property && surveyConfig) {
                            propertytoSub = new SurveyConfigProperties(
                                    surveyConfig: surveyConfig,
                                    surveyProperty: property

                            )
                            propertytoSub.save(flush: true)

                            flash.message = g.message(code: "surveyConfigs.add.successfully")

                        } else {
                            flash.error = g.message(code: "surveyConfigs.exists")
                        }
                    }
                } else {
                    def surveyConfigProp = property ? SurveyConfig.findAllBySurveyPropertyAndSurveyInfo(property, result.surveyInfo) : null
                    if (!surveyConfigProp && property) {
                        surveyConfigProp = new SurveyConfig(
                                surveyProperty: property,
                                configOrder: result.surveyInfo.surveyConfigs.size() + 1,
                                type: 'SurveyProperty',
                                surveyInfo: result.surveyInfo

                        )
                        surveyConfigProp.save(flush: true)

                        flash.message = g.message(code: "surveyConfigs.add.successfully")

                    } else {
                        flash.error = g.message(code: "surveyConfigs.exists")
                    }

                }
            }

            if (params.surveyConfigID) {
                redirect action: 'surveyConfigsInfo', id: result.surveyInfo.id, params: [surveyConfigID: params.surveyConfigID]
            } else {
                redirect action: 'surveyConfigs', id: result.surveyInfo.id
            }
        } else {
            redirect action: 'currentSurveysConsortia'
        }
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteSurveyConfig() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyConfig = SurveyConfig.get(params.id)
        def surveyInfo = surveyConfig.surveyInfo
        //surveyInfo.removeFromSurveyConfigs(surveyConfig)

        result.editable = (surveyInfo && surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.editable) {
            try {

                SurveyConfigProperties.findAllBySurveyConfig(surveyConfig).each {
                    it.delete(flush: true)
                }

                SurveyOrg.findAllBySurveyConfig(surveyConfig).each {
                    it.delete(flush: true)
                }

                surveyConfig.delete(flush: true)
                flash.message = g.message(code: "default.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
            }
            catch (DataIntegrityViolationException e) {
                flash.error = g.message(code: "default.not.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteSurveyPropfromSub() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyConfigProp = SurveyConfigProperties.get(params.id)

        def surveyInfo = surveyConfigProp?.surveyConfig?.surveyInfo

        result.editable = (surveyInfo && surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.editable) {
            try {
                surveyConfigProp.delete(flush: true)
                flash.message = g.message(code: "default.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
            }
            catch (DataIntegrityViolationException e) {
                flash.error = g.message(code: "default.not.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
            }
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSurveyProperty() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyProperty = SurveyProperty.findWhere(
                name: params.name,
                type: params.type,
                owner: result.institution,
        )

        if ((!surveyProperty) && params.name && params.type) {
            def rdc
            if (params.refdatacategory) {
                rdc = RefdataCategory.findById(Long.parseLong(params.refdatacategory))
            }
            surveyProperty = SurveyProperty.loc(
                    params.name,
                    params.type,
                    rdc,
                    params.explain,
                    params.comment,
                    params.introduction,
                    result.institution
            )

            if (surveyProperty.save(flush: true)) {
                flash.message = message(code: 'surveyProperty.create.successfully', args: [surveyProperty.name])
            } else {
                flash.error = message(code: 'surveyProperty.create.fail')
            }
        } else if (surveyProperty) {
            flash.error = message(code: 'surveyProperty.create.exist')
        } else {
            flash.error = message(code: 'surveyProperty.create.fail')
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteSurveyProperty() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyProperty = SurveyProperty.findByIdAndOwner(params.deleteId, result.institution)

        if (surveyProperty.countUsages()==0 && surveyProperty?.owner?.id == result.institution?.id && surveyProperty.delete())
        {
            flash.message = message(code: 'default.deleted.message', args:[message(code: 'surveyProperty.label'), surveyProperty.getI10n('name')])
        }

        redirect(action: 'allSurveyProperties', id: params.id)

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def addSurveyParticipants() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyConfig = SurveyConfig.get(params.surveyConfigID)
        def surveyInfo = surveyConfig?.surveyInfo

        result.editable = (surveyInfo && surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (params.selectedOrgs && result.editable) {

            params.list('selectedOrgs').each { soId ->

                def org = Org.get(Long.parseLong(soId))

                if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org))) {
                    def surveyOrg = new SurveyOrg(
                            surveyConfig: surveyConfig,
                            org: org
                    )

                    if (!surveyOrg.save(flush: true)) {
                        log.debug("Error by add Org to SurveyOrg ${surveyOrg.errors}");
                    } else {
                        flash.message = g.message(code: "surveyParticipants.add.successfully")
                    }
                }
            }
            surveyConfig.save(flush: true)

        }

        redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID, tab: 'selectedParticipants']

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processOpenSurvey() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.editable) {

            result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

            result.surveyConfigs.each { config ->

                if(!config?.pickAndChoose) {
                    if (config?.type == 'Subscription') {

                        config.orgs?.org?.each { org ->

                            config?.surveyProperties?.each { property ->

                                def surveyResult = new SurveyResult(
                                        owner: result.institution,
                                        participant: org ?: null,
                                        startDate: result.surveyInfo.startDate,
                                        endDate: result.surveyInfo.endDate,
                                        type: property.surveyProperty,
                                        surveyConfig: config
                                )

                                if (surveyResult.save(flush: true)) {
                                    log.debug(surveyResult)
                                } else {
                                    log.debug(surveyResult)
                                }
                            }
                        }
                    } else {
                        config.orgs?.org?.each { org ->

                            def surveyResult = new SurveyResult(
                                    owner: result.institution,
                                    participant: org ?: null,
                                    startDate: result.surveyInfo.startDate,
                                    endDate: result.surveyInfo.endDate,
                                    type: config.surveyProperty,
                                    surveyConfig: config
                            )

                            if (surveyResult.save(flush: true)) {

                            }
                        }
                    }
                }
            }

            result.surveyInfo.status = RDStore.SURVEY_READY
            result.surveyInfo.save(flush: true)
            flash.message = g.message(code: "openSurvey.successfully")
        }

            redirect action: 'show', id: params.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processEndSurvey() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        if (result.editable) {

            result.surveyInfo.status = RDStore.SURVEY_SURVEY_COMPLETED
            result.surveyInfo.save(flush: true)
            flash.message = g.message(code: "endSurvey.successfully")
        }

        redirect action: 'show', id: params.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processOpenSurveyNow() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        def currentDate = new Date(System.currentTimeMillis())

        if (result.editable) {

            result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

            result.surveyConfigs.each { config ->
                if(!config?.pickAndChoose) {
                    if (config?.type == 'Subscription') {

                        config.orgs?.org?.each { org ->

                            config?.surveyProperties?.each { property ->

                                def surveyResult = new SurveyResult(
                                        owner: result.institution,
                                        participant: org ?: null,
                                        startDate: currentDate,
                                        endDate: result.surveyInfo.endDate,
                                        type: property.surveyProperty,
                                        surveyConfig: config
                                )

                                if (surveyResult.save(flush: true)) {
                                    log.debug(surveyResult)
                                } else {
                                    log.debug(surveyResult)
                                }
                            }
                        }
                    } else {
                        config.orgs?.org?.each { org ->

                            def surveyResult = new SurveyResult(
                                    owner: result.institution,
                                    participant: org ?: null,
                                    startDate: currentDate,
                                    endDate: result.surveyInfo.endDate,
                                    type: config.surveyProperty,
                                    surveyConfig: config
                            )

                            if (surveyResult.save(flush: true)) {

                            }
                        }
                    }
                }

            }

            result.surveyInfo.status = RDStore.SURVEY_SURVEY_STARTED
            result.surveyInfo.startDate = currentDate
            result.surveyInfo.save(flush: true)
            flash.message = g.message(code: "openSurveyNow.successfully")

            surveyUpdateService.emailsToSurveyUsers([result.surveyInfo?.id])

        }

            redirect action: 'show', id: params.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteSurveyParticipants() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (params.selectedOrgs && result.editable) {

            params.list('selectedOrgs').each { soId ->
                if (SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, Org.get(Long.parseLong(soId))).delete(flush: true)) {
                    flash.message = g.message(code: "surveyParticipants.delete.successfully")
                }
            }
        }

        redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID]

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteDocuments() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect action: 'surveyConfigDocs', id: result.surveyInfo.id, params: [surveyConfigID: result.surveyConfig.id]
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def deleteSurveyInfo() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RDStore.SURVEY_IN_PROCESSING) ? false : result.editable

        if (result.editable) {
            result.surveyInfo.surveyConfigs.each { config ->

                config.documents.each {

                    it.delete()

                }
                it.delete()
            }
        }

        redirect action: 'currentSurveysConsortia'
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def editSurveyCostItem() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }
        result.costItem = CostItem.findById(params.costItem)

        def costItemElementConfigurations = []
        def orgConfigurations = []

        def ciecs = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Cost configuration'))
        ciecs.each { ciec ->
            costItemElementConfigurations.add([id: ciec.class.name + ":" + ciec.id, value: ciec.getI10n('value')])
        }
        def orgConf = CostItemElementConfiguration.findAllByForOrganisation(contextService.org)
        orgConf.each { oc ->
            orgConfigurations.add([id: oc.costItemElement.id, value: oc.elementSign.class.name + ":" + oc.elementSign.id])
        }

        result.costItemElementConfigurations = costItemElementConfigurations
        result.orgConfigurations = orgConfigurations
        //result.selectedCostItemElement = params.selectedCostItemElement ?: RefdataValue.getByValueAndCategory('price: consortial price', 'CostItemElement').id.toString()

        result.participant = Org.get(params.participant)
        result.surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result.surveyConfig, result.participant)

        result.mode = result.costItem ? "edit" : ""
        render(template: "/survey/costItemModal", model: result)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def addForAllSurveyCostItem() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        def costItemElementConfigurations = []
        def orgConfigurations = []

        def ciecs = RefdataValue.findAllByOwner(RefdataCategory.findByDesc('Cost configuration'))
        ciecs.each { ciec ->
            costItemElementConfigurations.add([id: ciec.class.name + ":" + ciec.id, value: ciec.getI10n('value')])
        }
        def orgConf = CostItemElementConfiguration.findAllByForOrganisation(contextService.org)
        orgConf.each { oc ->
            orgConfigurations.add([id: oc.costItemElement.id, value: oc.elementSign.class.name + ":" + oc.elementSign.id])
        }

        result.costItemElementConfigurations = costItemElementConfigurations
        result.orgConfigurations = orgConfigurations
        //result.selectedCostItemElement = params.selectedCostItemElement ?: RefdataValue.getByValueAndCategory('price: consortial price', 'CostItemElement').id.toString()

        result.setting = 'bulkForAll'

        result.surveyOrgList = []

        if (params.get('orgsIDs')) {
            List idList = (params.get('orgsIDs')?.split(',')?.collect { Long.valueOf(it.trim()) }).toList()
            result.surveyOrgList = SurveyOrg.findAllByOrgInListAndSurveyConfig(Org.findAllByIdInList(idList), result.surveyConfig)
        }

        render(template: "/survey/costItemModal", model: result)
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def exportParticipantResult() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }


        def surveyResults

        if (result.surveyConfig) {
            surveyResults = SurveyResult.findAllByOwnerAndSurveyConfig(result.institution, result.surveyConfig).sort {
                it?.participant.sortname
            }

        } else {

            surveyResults = SurveyResult.findAllByOwnerAndSurveyConfigInList(result.institution, result.surveyInfo.surveyConfigs).sort {
                params.exportConfigs ? it?.surveyConfig?.configOrder : it?.participant.sortname
            }
        }


        if (params.exportXLS) {
            def sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notimenopoint'));
            String datetoday = sdf.format(new Date(System.currentTimeMillis()))
            String filename = "${datetoday}_" + g.message(code: "survey.label")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) ((params.surveyConfigID) ? exportSurveyParticipantResultMin(surveyResults, "xls", result.institution) : exportSurveyParticipantResult(surveyResults, "xls", result.institution))
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        } else {
            redirect(uri: request.getHeader('referer'))
        }

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def setInEvaluation() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyInfo.status = RDStore.SURVEY_IN_EVALUATION
        result.surveyInfo.save(flush: true)

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def setCompleteSurvey() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.surveyInfo.status = RDStore.SURVEY_SURVEY_COMPLETED
        result.surveyInfo.save(flush: true)

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def renewalwithSurvey() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }
        result.superOrgType = []
        if(accessService.checkPerm('ORG_CONSORTIUM')) {
            result.superOrgType << message(code:'consortium.superOrgType')
        }
        if(accessService.checkPerm('ORG_INST_COLLECTIVE')) {
            result.superOrgType << message(code:'collective.superOrgType')
        }

        result.parentSubscription = result.surveyConfig?.subscription
        result.parentSubChilds = subscriptionService.getCurrentValidSubChilds(result.parentSubscription)
        result.parentSuccessorSubscription = result.surveyConfig?.subscription?.getCalculatedSuccessor()
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.participationProperty = SurveyProperty.findByNameAndOwnerIsNull("Participation")

        result.properties = []
        result.properties.addAll(SurveyConfigProperties.findAllBySurveyPropertyNotEqualAndSurveyConfig(result.participationProperty, result.surveyConfig)?.surveyProperty?.sort {
            it?.getI10n('name')
        })


        result.multiYearTermThreeSurvey = null
        result.multiYearTermTwoSurvey = null

        if (SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 3 years")?.id in result.properties.id) {
            result.multiYearTermThreeSurvey = SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 3 years")
            result.properties.remove(result.multiYearTermThreeSurvey)
        }
        if (SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 2 years")?.id in result.properties.id) {
            result.multiYearTermTwoSurvey = SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 2 years")
            result.properties.remove(result.multiYearTermTwoSurvey)

        }

        def lateCommersProperty = PropertyDefinition.findByName("Späteinsteiger")
        def currentParticipantIDs = []
        result.orgsWithMultiYearTermSub = []
        result.orgsLateCommers = []
        def orgsWithMultiYearTermOrgsID = []
        def orgsLateCommersOrgsID = []
        result.parentSubChilds?.each { sub ->
            if (sub?.isCurrentMultiYearSubscription())
            {
                result.orgsWithMultiYearTermSub << sub
                sub?.getAllSubscribers()?.each { org ->
                    orgsWithMultiYearTermOrgsID << org?.id
                }
            }
            else if (lateCommersProperty && lateCommersProperty?.type == 'class com.k_int.kbplus.RefdataValue') {
                def subProp = sub?.customProperties?.find { it?.type?.id == lateCommersProperty?.id }
                if(subProp?.refValue == RefdataValue.getByValueAndCategory('Yes', lateCommersProperty?.refdataCategory))
                {
                    result.orgsLateCommers << sub
                    sub?.getAllSubscribers()?.each { org ->
                        orgsLateCommersOrgsID << org?.id
                    }

                } else
                {
                    sub?.getAllSubscribers()?.each { org ->
                        currentParticipantIDs << org?.id
                    }
                }
            }
            else
            {
                sub?.getAllSubscribers()?.each { org ->
                    currentParticipantIDs << org?.id
                }
            }
        }


        result.orgsWithParticipationInParentSuccessor = []
        result.parentSuccessorSubChilds?.each { sub ->
            sub?.getAllSubscribers()?.each { org ->
                if(! (org?.id in currentParticipantIDs)) {
                    result.orgsWithParticipationInParentSuccessor  << sub
                }
            }
        }

        result.orgsWithTermination = []

            //Orgs with termination there sub
            SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue  order by participant.sortname",
                    [
                     owner      : result.institution?.id,
                     surProperty: result.participationProperty?.id,
                     surConfig  : result.surveyConfig?.id,
                     refValue   : RDStore.YN_NO])?.each {
                def newSurveyResult = [:]
                newSurveyResult.participant = it?.participant
                newSurveyResult.resultOfParticipation = it
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.sub = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                        [parentSub  : result.parentSubscription,
                         participant: it?.participant
                        ])[0]
                newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(it?.participant, result.institution, result.surveyConfig, result.properties).sort {
                    it?.type?.getI10n('name')
                }

                result.orgsWithTermination << newSurveyResult

            }


        // Orgs that renew or new to Sub
        result.orgsContinuetoSubscription = []
        result.newOrgsContinuetoSubscription = []

            SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue order by participant.sortname",
                    [
                     owner      : result.institution?.id,
                     surProperty: result.participationProperty?.id,
                     surConfig  : result.surveyConfig?.id,
                     refValue   : RDStore.YN_YES])?.each {
                def newSurveyResult = [:]
                newSurveyResult.participant = it?.participant
                newSurveyResult.resultOfParticipation = it
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(it?.participant, result.institution, result.surveyConfig, result.properties).sort {
                    it?.type?.getI10n('name')
                }

                if (it?.participant?.id in currentParticipantIDs) {

                    newSurveyResult.sub = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                            [parentSub  : result.parentSubscription,
                             participant: it?.participant
                            ])[0]

                    //newSurveyResult.sub = result.parentSubscription?.getDerivedSubscriptionBySubscribers(it?.participant)

                    if (result.multiYearTermTwoSurvey) {

                        newSurveyResult.newSubPeriodTwoStartDate = null
                        newSurveyResult.newSubPeriodTwoEndDate = null

                        def participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it?.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                        if (participantPropertyTwo?.refValue?.id == RDStore.YN_YES?.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodTwoStartDate = newSurveyResult.sub?.startDate ? (newSurveyResult.sub?.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodTwoEndDate = newSurveyResult.sub?.endDate ? (newSurveyResult.sub?.endDate + 2.year) : null
                                newSurveyResult.participantPropertyTwoComment = participantPropertyTwo?.comment
                            }
                        }

                    }
                    if (result.multiYearTermThreeSurvey) {
                        newSurveyResult.newSubPeriodThreeStartDate = null
                        newSurveyResult.newSubPeriodThreeEndDate = null

                        def participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it?.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                        if (participantPropertyThree?.refValue?.id == RDStore.YN_YES?.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodThreeStartDate = newSurveyResult.sub?.startDate ? (newSurveyResult.sub?.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodThreeEndDate = newSurveyResult.sub?.endDate ? (newSurveyResult.sub?.endDate + 3.year) : null
                                newSurveyResult.participantPropertyThreeComment = participantPropertyThree?.comment
                            }
                        }
                    }

                    result.orgsContinuetoSubscription << newSurveyResult
                }
                if (!(it?.participant?.id in currentParticipantIDs) && !(it?.participant?.id in orgsLateCommersOrgsID) && !(it?.participant?.id in orgsWithMultiYearTermOrgsID)) {


                    if (result.multiYearTermTwoSurvey) {

                        newSurveyResult.newSubPeriodTwoStartDate = null
                        newSurveyResult.newSubPeriodTwoEndDate = null

                        def participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it?.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                        if (participantPropertyTwo?.refValue?.id == RDStore.YN_YES?.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodTwoStartDate = result.parentSubscription?.startDate ? (result.parentSubscription?.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodTwoEndDate = result.parentSubscription?.endDate ? (result.parentSubscription?.endDate + 2.year) : null
                                newSurveyResult.participantPropertyTwoComment = participantPropertyTwo?.comment
                            }
                        }

                    }
                    if (result.multiYearTermThreeSurvey) {
                        newSurveyResult.newSubPeriodThreeStartDate = null
                        newSurveyResult.newSubPeriodThreeEndDate = null

                        def participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it?.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                        if (participantPropertyThree?.refValue?.id == RDStore.YN_YES?.id) {
                            use(TimeCategory) {
                                newSurveyResult.newSubPeriodThreeStartDate = result.parentSubscription?.startDate ? (result.parentSubscription?.endDate + 1.day) : null
                                newSurveyResult.newSubPeriodThreeEndDate = result.parentSubscription?.endDate ? (result.parentSubscription?.endDate + 3.year) : null
                                newSurveyResult.participantPropertyThreeComment = participantPropertyThree?.comment
                            }
                        }
                    }

                    result.newOrgsContinuetoSubscription << newSurveyResult
                }


            }


        //Orgs without really result
        result.orgsWithoutResult = []

            SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue is null order by participant.sortname",
                    [
                     owner      : result.institution?.id,
                     surProperty: result.participationProperty?.id,
                     surConfig  : result.surveyConfig?.id])?.each {
                def newSurveyResult = [:]
                newSurveyResult.participant = it?.participant
                newSurveyResult.resultOfParticipation = it
                newSurveyResult.surveyConfig = result.surveyConfig
                newSurveyResult.properties = SurveyResult.findAllByParticipantAndOwnerAndSurveyConfigAndTypeInList(it?.participant, result.institution, result.surveyConfig, result.properties).sort {
                    it?.type?.getI10n('name')
                }

                if (it?.participant?.id in currentParticipantIDs) {
                    newSurveyResult.sub = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                            [parentSub  : result.parentSubscription,
                             participant: it?.participant
                            ])[0]
                    //newSurveyResult.sub = result.parentSubscription?.getDerivedSubscriptionBySubscribers(it?.participant)
                } else {
                    newSurveyResult.sub = null
                }
                result.orgsWithoutResult << newSurveyResult
            }


        //MultiYearTerm Subs
        def sumParticipantWithSub = ((result.orgsContinuetoSubscription?.groupBy {
            it?.participant.id
        }?.size()?:0) + (result.orgsWithTermination?.groupBy { it?.participant.id }?.size()?:0) + (result.orgsWithMultiYearTermSub?.size()?:0))

        if (sumParticipantWithSub < result.parentSubChilds?.size()?:0) {
            def property = PropertyDefinition.findByName("Mehrjahreslaufzeit ausgewählt")

            def removeSurveyResultOfOrg = []
            result.orgsWithoutResult?.each { surveyResult ->
                if (surveyResult?.participant?.id in currentParticipantIDs && surveyResult?.sub) {

                    if (property?.type == 'class com.k_int.kbplus.RefdataValue') {
                        if (surveyResult?.sub?.customProperties?.find {
                            it?.type?.id == property?.id
                        }?.refValue == RefdataValue.getByValueAndCategory('Yes', property?.refdataCategory)) {

                            result.orgsWithMultiYearTermSub << surveyResult?.sub
                            removeSurveyResultOfOrg << surveyResult
                        }
                    }
                }
            }
            removeSurveyResultOfOrg?.each{ it
                result.orgsWithoutResult?.remove(it)
            }

            result.orgsWithMultiYearTermSub = result.orgsWithMultiYearTermSub.sort{it.getAllSubscribers()[0]?.sortname}

        }


        def message = g.message(code: 'renewalexport.renewals')
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))
        String datetoday = sdf.format(new Date(System.currentTimeMillis()))
        String filename = message + "_" + result?.surveyConfig?.getSurveyName() +"_${datetoday}"
        if (params.exportXLS) {
            try {
                SXSSFWorkbook wb = (SXSSFWorkbook) exportRenewalResult(result)
                // Write the output to a file

                response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
                response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                wb.write(response.outputStream)
                response.outputStream.flush()
                response.outputStream.close()
                wb.dispose()

                return
            }
            catch (Exception e) {
                log.error("Problem", e);
                response.sendError(500)
            }
        }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def renewSubscriptionConsortiaWithSurvey() {

        def result = setResultGenericsAndCheckAccess()
        result.institution = contextService.org
        if (!(result || accessService.checkPerm("ORG_CONSORTIUM_SURVEY"))) {
            response.sendError(401); return
        }

        def subscription = Subscription.get(params.parentSub ?: null)

        def sdf = new SimpleDateFormat('dd.MM.yyyy')

        result.errors = []
        def newStartDate
        def newEndDate
        use(TimeCategory) {
            newStartDate = subscription?.endDate ? (subscription?.endDate + 1.day) : null
            newEndDate = subscription?.endDate ? (subscription?.endDate + 1.year) : null
        }
        params.surveyConfig = params.surveyConfig ?: null
        result.isRenewSub = true
        result.permissionInfo = [sub_startDate: newStartDate ? sdf.format(newStartDate) : null,
                                 sub_endDate  : newEndDate ? sdf.format(newEndDate) : null,
                                 sub_name     : subscription?.name,
                                 sub_id       : subscription?.id,
                                 sub_license  : subscription?.owner?.reference ?: '',
                                 sub_status   : RDStore.SUBSCRIPTION_INTENDED]

        result.subscription = subscription
        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processRenewalwithSurvey() {

        def result = setResultGenericsAndCheckAccess()
        if (!(result || accessService.checkPerm("ORG_CONSORTIUM_SURVEY"))) {
            response.sendError(401); return
        }

        def baseSub = Subscription.get(params.parentSub ?: null)

        ArrayList<Links> previousSubscriptions = Links.findAllByDestinationAndObjectTypeAndLinkType(baseSub?.id, Subscription.class.name, RDStore.LINKTYPE_FOLLOWS)
        if (previousSubscriptions.size() > 0) {
            flash.error = message(code: 'subscription.renewSubExist', default: 'The Subscription is already renewed!')
        } else {
            boolean isCopyAuditOn = params.subscription.isCopyAuditOn ? true : false
            def sub_startDate = null
            def sub_endDate = null
            def sub_status = null
            def old_subOID = null
            def new_subname = null
            def manualCancellationDate = null
            if (isCopyAuditOn) {
                use(TimeCategory) {
                    sub_startDate = baseSub?.endDate ? (baseSub?.endDate + 1.day) : null
                    sub_endDate = baseSub?.endDate ? (baseSub?.endDate + 1.year) : null
                }
                sub_status = RDStore.SUBSCRIPTION_INTENDED
                old_subOID = baseSub?.id
                new_subname = baseSub?.name
            } else {
                sub_startDate = params.subscription?.start_date ? parseDate(params.subscription?.start_date, possible_date_formats) : null
                sub_endDate = params.subscription?.end_date ? parseDate(params.subscription?.end_date, possible_date_formats) : null
                sub_status = params.subStatus
                old_subOID = params.subscription.old_subid
                new_subname = params.subscription.name
            }

            use(TimeCategory) {
                manualCancellationDate =  baseSub?.manualCancellationDate ? (baseSub?.manualCancellationDate + 1.year) : null
            }
            def newSub = new Subscription(
                    name: new_subname,
                    startDate: sub_startDate,
                    endDate: sub_endDate,
                    manualCancellationDate: manualCancellationDate,
                    identifier: java.util.UUID.randomUUID().toString(),
                    isPublic: baseSub?.isPublic,
                    isSlaved: baseSub?.isSlaved,
                    type: baseSub?.type ?: null,
                    status: sub_status,
                    resource: baseSub?.resource ?: null,
                    form: baseSub?.form ?: null
            )

            if (!newSub.save(flush: true)) {
                log.error("Problem saving subscription ${newSub.errors}");
                return newSub
            } else {
                log.debug("Save ok");
                if (isCopyAuditOn) {
                    //copy audit
                    def auditConfigs = AuditConfig.findAllByReferenceClassAndReferenceId(Subscription.class.name, baseSub?.id)
                    auditConfigs.each {
                        AuditConfig ac ->
                            //All ReferenceFields were copied!
                            //'name', 'startDate', 'endDate', 'manualCancellationDate', 'status', 'type', 'form', 'resource'
                            AuditConfig.addConfig(newSub, ac.referenceField)
                    }
                }
                //Copy References
                //OrgRole
                baseSub?.orgRelations?.each { or ->

                    if ((or.org?.id == contextService.getOrg()?.id) || (or.roleType.value in ['Subscriber', 'Subscriber_Consortial'])) {
                        OrgRole newOrgRole = new OrgRole()
                        InvokerHelper.setProperties(newOrgRole, or.properties)
                        newOrgRole.sub = newSub
                        newOrgRole.save(flush: true)
                    }
                }
                //link to previous subscription
                Links prevLink = new Links(source: newSub.id, destination: baseSub?.id, objectType: Subscription.class.name, linkType: RDStore.LINKTYPE_FOLLOWS, owner: contextService.org)
                if (!prevLink.save(flush: true)) {
                    log.error("Problem linking to previous subscription: ${prevLink.errors}")
                }
                result.newSub = newSub

                if (params?.targetSubscriptionId == "null") params.remove("targetSubscriptionId")
                result.isRenewSub = true
                if (isCopyAuditOn) {
                    redirect controller: 'survey',
                            action: 'copyElementsIntoRenewalSubscription',
                            id: old_subOID,
                            params: [sourceSubscriptionId: old_subOID, targetSubscriptionId: newSub.id, isRenewSub: true, isCopyAuditOn: true]
                } else {
                    redirect controller: 'survey',
                            action: 'copyElementsIntoRenewalSubscription',
                            id: old_subOID,
                            params: [sourceSubscriptionId: old_subOID, targetSubscriptionId: newSub.id, isRenewSub: true]
                }
            }
        }
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def copyElementsIntoRenewalSubscription() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        if (!result) {
            response.sendError(401); return
        }
        flash.error = ""
        flash.message = ""
        if (params?.sourceSubscriptionId == "null") params.remove("sourceSubscriptionId")
        result.sourceSubscriptionId = params?.sourceSubscriptionId ?: params?.id
        result.sourceSubscription = Subscription.get(Long.parseLong(params?.sourceSubscriptionId ?: params?.id))

        if (params?.targetSubscriptionId == "null") params.remove("targetSubscriptionId")
        if (params?.targetSubscriptionId) {
            result.targetSubscriptionId = params?.targetSubscriptionId
            result.targetSubscription = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }

        if (params?.isRenewSub) {
            result.isRenewSub = params?.isRenewSub
        }

        result.allSubscriptions_readRights = subscriptionService.getMySubscriptions_readRights()
        result.allSubscriptions_writeRights = subscriptionService.getMySubscriptions_writeRights()

        List<String> subTypSubscriberVisible = [RDStore.SUBSCRIPTION_TYPE_CONSORTIAL,
                                                RDStore.SUBSCRIPTION_TYPE_ADMINISTRATIVE,
                                                RDStore.SUBSCRIPTION_TYPE_ALLIANCE,
                                                RDStore.SUBSCRIPTION_TYPE_NATIONAL]
        result.isSubscriberVisible =
                result.sourceSubscription &&
                        result.targetSubscription &&
                        subTypSubscriberVisible.contains(result.sourceSubscription.type) &&
                        subTypSubscriberVisible.contains(result.targetSubscription.type)

        if (!result.isSubscriberVisible) {
            flash.message += message(code: 'subscription.info.subscriberNotAvailable')
        }

        switch (params.workFlowPart) {
            case WORKFLOW_DATES_OWNER_RELATIONS:
                result << copySubElements_DatesOwnerRelations();
                if (params.isRenewSub) {
                    params?.workFlowPart = WORKFLOW_PACKAGES_ENTITLEMENTS
                    result << loadDataFor_PackagesEntitlements()
                } else {
                    result << loadDataFor_DatesOwnerRelations()
                }
                break;
            case WORKFLOW_PACKAGES_ENTITLEMENTS:
                result << copySubElements_PackagesEntitlements();
                if (params.isRenewSub) {
                    params.workFlowPart = WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
                    result << loadDataFor_DocsAnnouncementsTasks()
                } else {
                    result << loadDataFor_PackagesEntitlements()
                }
                break;
            case WORKFLOW_DOCS_ANNOUNCEMENT_TASKS:
                result << copySubElements_DocsAnnouncementsTasks();
                if (params.isRenewSub) {
                    if (result.isSubscriberVisible) {
                        //params.workFlowPart = WORKFLOW_SUBSCRIBER
                        params.workFlowPart = WORKFLOW_PROPERTIES
                        result << loadDataFor_Subscriber()
                    } else {
                        params.workFlowPart = WORKFLOW_PROPERTIES
                        result << loadDataFor_Properties()
                    }
                } else {
                    result << loadDataFor_DocsAnnouncementsTasks()
                }
                break;
            case WORKFLOW_SUBSCRIBER:
                result << copySubElements_Subscriber();
                if (params.isRenewSub) {
                    params?.workFlowPart = WORKFLOW_PROPERTIES
                    result << loadDataFor_Properties()
                } else {
                    result << loadDataFor_Subscriber()
                }
                break;
            case WORKFLOW_PROPERTIES:
                result << copySubElements_Properties();
                if (params.isRenewSub && params.targetSubscriptionId) {
                    flash.error = ""
                    flash.message = ""
                    redirect controller: 'subscription', action: 'show', params: [id: params?.targetSubscriptionId]
                } else {
                    result << loadDataFor_Properties()
                }
                break;
            case WORKFLOW_END:
                result << copySubElements_Properties();
                if (params?.targetSubscriptionId) {
                    flash.error = ""
                    flash.message = ""
                    redirect controller: 'subscription', action: 'show', params: [id: params?.targetSubscriptionId]
                }
                break;
            default:
                result << loadDataFor_DatesOwnerRelations()
                break;
        }

        if (params?.targetSubscriptionId) {
            result.targetSubscription = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }
        result.workFlowPart = params?.workFlowPart ?: WORKFLOW_DATES_OWNER_RELATIONS
        result.workFlowPartNext = params?.workFlowPartNext ?: WORKFLOW_DOCS_ANNOUNCEMENT_TASKS
        if (params?.isCopyAuditOn) {
            result.isCopyAuditOn = params?.isCopyAuditOn
        }
        if (params?.isRenewSub) {
            result.isRenewSub = params?.isRenewSub
        }
        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    Map copySubElements_Properties() {
        LinkedHashMap result = [customProperties: [:], privateProperties: [:]]
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        boolean isRenewSub = params?.isRenewSub ? true : false
        boolean isCopyAuditOn = params?.isCopyAuditOn ? true : false
        Subscription newSub = null
        List<Subscription> subsToCompare = [baseSub]
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(params.targetSubscriptionId)
            subsToCompare.add(newSub)
        }
        List<AbstractProperty> propertiesToTake = params?.list('subscription.takeProperty').collect {
            genericOIDService.resolveOID(it)
        }
        if (propertiesToTake && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.copyProperties(propertiesToTake, newSub, isRenewSub, isCopyAuditOn, flash)
        }

        List<AbstractProperty> propertiesToDelete = params?.list('subscription.deleteProperty').collect {
            genericOIDService.resolveOID(it)
        }
        if (propertiesToDelete && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.deleteProperties(propertiesToDelete, newSub, isRenewSub, isCopyAuditOn, flash)
        }

        if (newSub) {
            result.newSub = newSub.refresh()
        }
        result
    }


    private loadDataFor_Properties() {
        LinkedHashMap result = [customProperties: [:], privateProperties: [:]]
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
        subsToCompare.each { sub ->
            TreeMap customProperties = result.customProperties
            customProperties = comparisonService.buildComparisonTree(customProperties, sub, sub.customProperties)
            result.customProperties = customProperties
            TreeMap privateProperties = result.privateProperties
            privateProperties = comparisonService.buildComparisonTree(privateProperties, sub, sub.privateProperties)
            result.privateProperties = privateProperties
        }
        result
    }

    private boolean isBothSubscriptionsSet(Subscription baseSub, Subscription newSub) {
        if (!baseSub || !newSub) {
            if (!baseSub) flash.error += message(code: 'subscription.details.copyElementsIntoSubscription.noSubscriptionSource') + '<br />'
            if (!newSub) flash.error += message(code: 'subscription.details.copyElementsIntoSubscription.noSubscriptionTarget') + '<br />'
            return false
        }
        return true
    }


    private copySubElements_PackagesEntitlements() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        boolean isTargetSubChanged = false
        if (params?.subscription?.deletePackageIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<SubscriptionPackage> packagesToDelete = params?.list('subscription.deletePackageIds').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.deletePackages(packagesToDelete, newSub, flash)
            isTargetSubChanged = true
        }
        if (params?.subscription?.takePackageIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<Package> packagesToTake = params?.list('subscription.takePackageIds').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.copyPackages(packagesToTake, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteEntitlementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<IssueEntitlement> entitlementsToDelete = params?.list('subscription.deleteEntitlementIds').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.deleteEntitlements(entitlementsToDelete, newSub, flash)
            isTargetSubChanged = true
        }
        if (params?.subscription?.takeEntitlementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            List<IssueEntitlement> entitlementsToTake = params?.list('subscription.takeEntitlementIds').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.copyEntitlements(entitlementsToTake, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }
        result.newSub = newSub
        result.subscription = baseSub
        result
    }

    private loadDataFor_PackagesEntitlements() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null
        result.sourceIEs = subscriptionService.getIssueEntitlements(baseSub)
        result.targetIEs = subscriptionService.getIssueEntitlements(newSub)
        result.newSub = newSub
        result.subscription = baseSub
        result
    }

    private copySubElements_DatesOwnerRelations() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        boolean isTargetSubChanged = false
        if (params?.subscription?.deleteDates && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.deleteDates(newSub, flash)
            isTargetSubChanged = true
        } else if (params?.subscription?.takeDates && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.copyDates(baseSub, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteOwner && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.deleteOwner(newSub, flash)
            isTargetSubChanged = true
        } else if (params?.subscription?.takeOwner && isBothSubscriptionsSet(baseSub, newSub)) {
            subscriptionService.copyOwner(baseSub, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteOrgRelations && isBothSubscriptionsSet(baseSub, newSub)) {
            List<OrgRole> toDeleteOrgRelations = params.list('subscription.deleteOrgRelations').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.deleteOrgRelations(toDeleteOrgRelations, newSub, flash)
            isTargetSubChanged = true
        }
        if (params?.subscription?.takeOrgRelations && isBothSubscriptionsSet(baseSub, newSub)) {
            List<OrgRole> toCopyOrgRelations = params.list('subscription.takeOrgRelations').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.copyOrgRelations(toCopyOrgRelations, baseSub, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }
        result.subscription = baseSub
        result.newSub = newSub
        result.targetSubscription = newSub
        result
    }

    private loadDataFor_DatesOwnerRelations() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ?: params.id)
        Subscription newSub = params.targetSubscriptionId ? Subscription.get(params.targetSubscriptionId) : null

        // restrict visible for templates/links/orgLinksAsList
        result.source_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(baseSub)
        result.target_visibleOrgRelations = subscriptionService.getVisibleOrgRelations(newSub)
        result
    }

    private copySubElements_DocsAnnouncementsTasks() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId) : Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }
        boolean isTargetSubChanged = false
        if (params?.subscription?.deleteDocIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteDocs = []
            params.list('subscription.deleteDocIds').each { doc -> toDeleteDocs << Long.valueOf(doc) }
            subscriptionService.deleteDocs(toDeleteDocs, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.takeDocIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyDocs = []
            params.list('subscription.takeDocIds').each { doc -> toCopyDocs << Long.valueOf(doc) }
            subscriptionService.copyDocs(baseSub, toCopyDocs, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteAnnouncementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteAnnouncements = []
            params.list('subscription.deleteAnnouncementIds').each { announcement -> toDeleteAnnouncements << Long.valueOf(announcement) }
            subscriptionService.deleteAnnouncements(toDeleteAnnouncements, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.takeAnnouncementIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyAnnouncements = []
            params.list('subscription.takeAnnouncementIds').each { announcement -> toCopyAnnouncements << Long.valueOf(announcement) }
            subscriptionService.copyAnnouncements(baseSub, toCopyAnnouncements, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.deleteTaskIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toDeleteTasks = []
            params.list('subscription.deleteTaskIds').each { tsk -> toDeleteTasks << Long.valueOf(tsk) }
            subscriptionService.deleteTasks(toDeleteTasks, newSub, flash)
            isTargetSubChanged = true
        }

        if (params?.subscription?.takeTaskIds && isBothSubscriptionsSet(baseSub, newSub)) {
            def toCopyTasks = []
            params.list('subscription.takeTaskIds').each { tsk -> toCopyTasks << Long.valueOf(tsk) }
            subscriptionService.copyTasks(baseSub, toCopyTasks, newSub, flash)
            isTargetSubChanged = true
        }

        if (isTargetSubChanged) {
            newSub = newSub.refresh()
        }

        result.sourceSubscription = baseSub
        result.targetSubscription = newSub
        result
    }

    private loadDataFor_DocsAnnouncementsTasks() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId) : Long.parseLong(params.id))
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

    private copySubElements_Subscriber() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        Subscription baseSub = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId) : Long.parseLong(params.id))
        Subscription newSub = null
        if (params.targetSubscriptionId) {
            newSub = Subscription.get(Long.parseLong(params.targetSubscriptionId))
        }

        if (params?.subscription?.copySubscriber && isBothSubscriptionsSet(baseSub, newSub)) {
            List<Subscription> toCopySubs = params.list('subscription.copySubscriber').collect {
                genericOIDService.resolveOID(it)
            }
            subscriptionService.copySubscriber(toCopySubs, newSub, flash)
        }

        result.sourceSubscription = baseSub
        result.targetSubscription = newSub
        result
    }

    private loadDataFor_Subscriber() {
        def result = setResultGenericsAndCheckAccessforSub(AccessService.CHECK_VIEW)
        result.sourceSubscription = Subscription.get(params.sourceSubscriptionId ? Long.parseLong(params.sourceSubscriptionId) : Long.parseLong(params.id))
        result.validSourceSubChilds = subscriptionService.getValidSubChilds(result.sourceSubscription)
        if (params.targetSubscriptionId) {
            result.targetSubscription = Subscription.get(Long.parseLong(params.targetSubscriptionId))
            result.validTargetSubChilds = subscriptionService.getValidSubChilds(result.targetSubscription)
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def exportSurCostItems() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        /*   def surveyInfo = SurveyInfo.findByIdAndOwner(params.id, result.institution) ?: null

           def surveyConfig = SurveyConfig.findByIdAndSurveyInfo(params.surveyConfigID, surveyInfo)*/

        if (params.exportXLS) {
            def sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notimenopoint'));
            String datetoday = sdf.format(new Date(System.currentTimeMillis()))
            String filename = "${datetoday}_" + g.message(code: "survey.label")
            //if(wb instanceof XSSFWorkbook) file += "x";
            response.setHeader "Content-disposition", "attachment; filename=\"${filename}.xlsx\""
            response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            SXSSFWorkbook wb = (SXSSFWorkbook) exportSurveyCostItems(result.surveyConfig, "xls", result.institution)
            wb.write(response.outputStream)
            response.outputStream.flush()
            response.outputStream.close()
            wb.dispose()

            return
        } else {
            redirect(uri: request.getHeader('referer'))
        }

    }


    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def copyEmailaddresses() {
        def result = [:]
        result.modalID = params.targetId
        result.orgList = []

        if (params.get('orgListIDs')) {
            List idList = (params.get('orgListIDs').split(',').collect { Long.valueOf(it.trim()) }).toList()
            result.orgList = Org.findAllByIdInList(idList)
        }

        render(template: "/survey/copyEmailaddresses", model: result)
    }


    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def newSurveyCostItem() {

        def dateFormat = new java.text.SimpleDateFormat(message(code: 'default.date.format.notime', default: 'yyyy-MM-dd'))

        def result = [:]
        def newCostItem = null

        try {
            log.debug("SurveyController::newCostItem() ${params}");

            result.institution = contextService.getOrg()
            def user = User.get(springSecurityService.principal.id)
            result.error = [] as List

            if (!accessService.checkMinUserOrgRole(user, result.institution, "INST_EDITOR")) {
                result.error = message(code: 'financials.permission.unauthorised', args: [result.institution ? result.institution.name : 'N/A'])
                response.sendError(403)
            }


            Closure newDate = { param, format ->
                Date date
                try {
                    date = dateFormat.parse(param)
                } catch (Exception e) {
                    log.debug("Unable to parse date : ${param} in format ${format}")
                }
                date
            }

            def startDate = newDate(params.newStartDate, dateFormat.toPattern())
            def endDate = newDate(params.newEndDate, dateFormat.toPattern())
            def billing_currency = null
            if (params.long('newCostCurrency')) //GBP,etc
            {
                billing_currency = RefdataValue.get(params.newCostCurrency)
                if (!billing_currency)
                    billing_currency = defaultCurrency
            }

            //def tempCurrencyVal       = params.newCostCurrencyRate?      params.double('newCostCurrencyRate',1.00) : 1.00//def cost_local_currency   = params.newCostInLocalCurrency?   params.double('newCostInLocalCurrency', cost_billing_currency * tempCurrencyVal) : 0.00
            def cost_item_status = params.newCostItemStatus ? (RefdataValue.get(params.long('newCostItemStatus'))) : null;
            //estimate, commitment, etc
            def cost_item_element = params.newCostItemElement ? (RefdataValue.get(params.long('newCostItemElement'))) : null
            //admin fee, platform, etc
            //moved to TAX_TYPES
            //def cost_tax_type         = params.newCostTaxType ?          (RefdataValue.get(params.long('newCostTaxType'))) : null           //on invoice, self declared, etc

            def cost_item_category = params.newCostItemCategory ? (RefdataValue.get(params.long('newCostItemCategory'))) : null
            //price, bank charge, etc

            NumberFormat format = NumberFormat.getInstance(LocaleContextHolder.getLocale())
            def cost_billing_currency = params.newCostInBillingCurrency ? format.parse(params.newCostInBillingCurrency).doubleValue() : 0.00
            def cost_currency_rate = params.newCostCurrencyRate ? params.double('newCostCurrencyRate', 1.00) : 1.00
            def cost_local_currency = params.newCostInLocalCurrency ? format.parse(params.newCostInLocalCurrency).doubleValue() : 0.00

            def cost_billing_currency_after_tax = params.newCostInBillingCurrencyAfterTax ? format.parse(params.newCostInBillingCurrencyAfterTax).doubleValue() : cost_billing_currency
            def cost_local_currency_after_tax = params.newCostInLocalCurrencyAfterTax ? format.parse(params.newCostInLocalCurrencyAfterTax).doubleValue() : cost_local_currency
            //moved to TAX_TYPES
            //def new_tax_rate                      = params.newTaxRate ? params.int( 'newTaxRate' ) : 0
            def tax_key = null
            if (!params.newTaxRate.contains("null")) {
                String[] newTaxRate = params.newTaxRate.split("§")
                RefdataValue taxType = genericOIDService.resolveOID(newTaxRate[0])
                int taxRate = Integer.parseInt(newTaxRate[1])
                switch (taxType.id) {
                    case RefdataValue.getByValueAndCategory("taxable", "TaxType").id:
                        switch (taxRate) {
                            case 7: tax_key = CostItem.TAX_TYPES.TAXABLE_7
                                break
                            case 19: tax_key = CostItem.TAX_TYPES.TAXABLE_19
                                break
                        }
                        break
                    case RefdataValue.getByValueAndCategory("taxable tax-exempt", "TaxType").id:
                        tax_key = CostItem.TAX_TYPES.TAX_EXEMPT
                        break
                    case RefdataValue.getByValueAndCategory("not taxable", "TaxType").id:
                        tax_key = CostItem.TAX_TYPES.TAX_NOT_TAXABLE
                        break
                    case RefdataValue.getByValueAndCategory("not applicable", "TaxType").id:
                        tax_key = CostItem.TAX_TYPES.TAX_NOT_APPLICABLE
                        break
                }
            }
            def cost_item_element_configuration = params.ciec ? genericOIDService.resolveOID(params.ciec) : null

            def cost_item_isVisibleForSubscriber = false
            // (params.newIsVisibleForSubscriber ? (RefdataValue.get(params.newIsVisibleForSubscriber)?.value == 'Yes') : false)

            def surveyOrgsDo = []

            if (params.surveyOrg) {
                try {
                    surveyOrgsDo << genericOIDService.resolveOID(params.surveyOrg)
                } catch (Exception e) {
                    log.error("Non-valid surveyOrg sent ${params.surveyOrg}", e)
                }
            }

            if (params.get('surveyOrgs')) {
                List surveyOrgs = (params.get('surveyOrgs')?.split(',')?.collect {
                    String.valueOf(it.replaceAll("\\s", ""))
                }).toList()
                surveyOrgs.each {
                    try {

                        def surveyOrg = genericOIDService.resolveOID(it)
                        if (!CostItem.findBySurveyOrg(surveyOrg)) {
                            surveyOrgsDo << surveyOrg
                        }
                    } catch (Exception e) {
                        log.error("Non-valid surveyOrg sent ${it}", e)
                    }
                }
            }

            /* if (params.surveyConfig) {
                 def surveyConfig = genericOIDService.resolveOID(params.surveyConfig)

                 surveyConfig?.orgs?.each {

                     if (!CostItem.findBySurveyOrg(it)) {
                         surveyOrgsDo << it
                     }
                 }
             }*/

            surveyOrgsDo?.each { surveyOrg ->

                if (!surveyOrg?.existsMultiYearTerm()) {

                    if (params.oldCostItem && genericOIDService.resolveOID(params.oldCostItem)) {
                        newCostItem = genericOIDService.resolveOID(params.oldCostItem)
                    } else {
                        newCostItem = new CostItem()
                    }

                    newCostItem.owner = result.institution
                    newCostItem.surveyOrg = newCostItem.surveyOrg ?: surveyOrg
                    newCostItem.isVisibleForSubscriber = cost_item_isVisibleForSubscriber
                    newCostItem.costItemCategory = cost_item_category
                    newCostItem.costItemElement = cost_item_element
                    newCostItem.costItemStatus = cost_item_status
                    newCostItem.billingCurrency = billing_currency //Not specified default to GDP
                    //newCostItem.taxCode = cost_tax_type -> to taxKey
                    newCostItem.costTitle = params.newCostTitle ?: null
                    newCostItem.costInBillingCurrency = cost_billing_currency as Double
                    newCostItem.costInLocalCurrency = cost_local_currency as Double

                    newCostItem.finalCostRounding = params.newFinalCostRounding ? true : false
                    newCostItem.costInBillingCurrencyAfterTax = cost_billing_currency_after_tax as Double
                    newCostItem.costInLocalCurrencyAfterTax = cost_local_currency_after_tax as Double
                    newCostItem.currencyRate = cost_currency_rate as Double
                    //newCostItem.taxRate = new_tax_rate as Integer -> to taxKey
                    newCostItem.taxKey = tax_key
                    newCostItem.costItemElementConfiguration = cost_item_element_configuration

                    newCostItem.costDescription = params.newDescription ? params.newDescription.trim() : null

                    newCostItem.startDate = startDate ?: null
                    newCostItem.endDate = endDate ?: null

                    newCostItem.includeInSubscription = null
                    //todo Discussion needed, nobody is quite sure of the functionality behind this...


                    if (!newCostItem.validate()) {
                        result.error = newCostItem.errors.allErrors.collect {
                            log.error("Field: ${it.properties.field}, user input: ${it.properties.rejectedValue}, Reason! ${it.properties.code}")
                            message(code: 'finance.addNew.error', args: [it.properties.field])
                        }
                    } else {
                        if (newCostItem.save(flush: true)) {
                            /* def newBcObjs = []

                         params.list('newBudgetCodes')?.each { newbc ->
                             def bc = genericOIDService.resolveOID(newbc)
                             if (bc) {
                                 newBcObjs << bc
                                 if (! CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )) {
                                     new CostItemGroup(costItem: newCostItem, budgetCode: bc).save(flush: true)
                                 }
                             }
                         }

                         def toDelete = newCostItem.getBudgetcodes().minus(newBcObjs)
                         toDelete.each{ bc ->
                             def cig = CostItemGroup.findByCostItemAndBudgetCode( newCostItem, bc )
                             if (cig) {
                                 log.debug('deleting ' + cig)
                                 cig.delete()
                             }
                         }*/

                        } else {
                            result.error = "Unable to save!"
                        }
                    }
                }
            } // subsToDo.each

        }
        catch (Exception e) {
            log.error("Problem in add cost item", e);
        }


        redirect(uri: request.getHeader('referer'))
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def transferParticipants() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.parentSubscription = result.surveyConfig?.subscription
        result.parentSubChilds = subscriptionService.getCurrentValidSubChilds(result.parentSubscription)
        result.parentSuccessorSubscription = result.surveyConfig?.subscription?.getCalculatedSuccessor()
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.participantsList = []

        result.parentParticipantsList = []
        result.parentSuccessortParticipantsList = []

        result.parentSubChilds.each { sub ->
            def org = sub.getSubscriber()
            result.participantsList << org
            result.parentParticipantsList << org

        }

        result.parentSuccessorSubChilds.each { sub ->
            def org = sub.getSubscriber()
            if(!org in result.participantsList) {
                result.participantsList << org
            }
            result.parentSuccessortParticipantsList << org

        }

        result.participantsList = result.participantsList.sort{it.sortname}


        result.participationProperty = SurveyProperty.findByNameAndOwnerIsNull("Participation")

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_EDITOR", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_EDITOR", "ROLE_ADMIN")
    })
    def processTransferParticipants() {
        def result = setResultGenericsAndCheckAccess()
        if (!result.editable) {
            response.sendError(401); return
        }

        result.parentSubscription = result.surveyConfig?.subscription
        result.parentSubChilds = subscriptionService.getCurrentValidSubChilds(result.parentSubscription)
        result.parentSuccessorSubscription = result.surveyConfig?.subscription?.getCalculatedSuccessor()
        result.parentSuccessorSubChilds = result.parentSuccessorSubscription ? subscriptionService.getValidSubChilds(result.parentSuccessorSubscription) : null

        result.participationProperty = SurveyProperty.findByNameAndOwnerIsNull("Participation")

        result.properties = []
        result.properties.addAll(SurveyConfigProperties.findAllBySurveyPropertyNotEqualAndSurveyConfig(result.participationProperty, result.surveyConfig)?.surveyProperty)

        result.multiYearTermThreeSurvey = null
        result.multiYearTermTwoSurvey = null

        if (SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 3 years")?.id in result.properties.id) {
            result.multiYearTermThreeSurvey = SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 3 years")
            result.properties.remove(result.multiYearTermThreeSurvey)
        }
        if (SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 2 years")?.id in result.properties.id) {
            result.multiYearTermTwoSurvey = SurveyProperty.findByNameAndOwnerIsNull("Multi-year term 2 years")
            result.properties.remove(result.multiYearTermTwoSurvey)

        }

        def lateCommersProperty = PropertyDefinition.findByName("Späteinsteiger")
        def currentParticipantIDs = []
        result.orgsWithMultiYearTermSub = []
        result.orgsLateCommers = []
        def orgsWithMultiYearTermOrgsID = []
        def orgsLateCommersOrgsID = []
        result.parentSubChilds?.each { sub ->
            if (sub?.isCurrentMultiYearSubscription())
            {
                result.orgsWithMultiYearTermSub << sub
                sub?.getAllSubscribers()?.each { org ->
                    orgsWithMultiYearTermOrgsID << org?.id
                }
            }
            else if (lateCommersProperty && lateCommersProperty?.type == 'class com.k_int.kbplus.RefdataValue') {
                def subProp = sub?.customProperties?.find { it?.type?.id == lateCommersProperty?.id }
                if(subProp?.refValue == RefdataValue.getByValueAndCategory('Yes', lateCommersProperty?.refdataCategory))
                {
                    result.orgsLateCommers << sub
                    sub?.getAllSubscribers()?.each { org ->
                        orgsLateCommersOrgsID << org?.id
                    }

                } else
                {
                    sub?.getAllSubscribers()?.each { org ->
                        currentParticipantIDs << org?.id
                    }
                }
            }
            else
            {
                sub?.getAllSubscribers()?.each { org ->
                    currentParticipantIDs << org?.id
                }
            }
        }


        result.parentSuccessortParticipantsList = []

        result.parentSuccessorSubChilds.each { sub ->
            def org = sub.getSubscriber()
            result.parentSuccessortParticipantsList << org

        }

        // Orgs that renew or new to Sub
        result.orgsContinuetoSubscription = []
        result.newOrgsContinuetoSubscription = []

        result.newSubs = []

        def countNewSubs = 0

        SurveyResult.executeQuery("from SurveyResult where owner.id = :owner and surveyConfig.id = :surConfig and type.id = :surProperty and refValue = :refValue order by participant.sortname",
                [
                        owner      : result.institution?.id,
                        surProperty: result.participationProperty?.id,
                        surConfig  : result.surveyConfig?.id,
                        refValue   : RDStore.YN_YES])?.each {

            // Keine Kindlizenz in der Nachfolgerlizenz vorhanden
            if(!(it?.participant in result.parentSuccessortParticipantsList)){

                def oldSubofParticipant = Subscription.executeQuery("Select s from Subscription s left join s.orgRelations orgR where s.instanceOf = :parentSub and orgR.org = :participant",
                        [parentSub  : result.parentSubscription,
                         participant: it?.participant
                        ])[0]


                if(!oldSubofParticipant)
                {
                    oldSubofParticipant = result.parentSubscription
                }

                def newStartDate = null
                def newEndDate = null

                //Umfrage-Merkmal MJL2
                if (result.multiYearTermTwoSurvey) {

                    def participantPropertyTwo = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it?.participant, result.institution, result.surveyConfig, result.multiYearTermTwoSurvey)

                    if (participantPropertyTwo?.refValue?.id == RDStore.YN_YES?.id) {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant?.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant?.endDate ? (oldSubofParticipant.endDate + 2.year) : null
                        }
                            countNewSubs++
                            result.newSubs << processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it?.participant, newStartDate, newEndDate, true, params)
                    }

                }
                //Umfrage-Merkmal MJL3
                else if (result.multiYearTermThreeSurvey) {

                    def participantPropertyThree = SurveyResult.findByParticipantAndOwnerAndSurveyConfigAndType(it?.participant, result.institution, result.surveyConfig, result.multiYearTermThreeSurvey)
                    if (participantPropertyThree?.refValue?.id == RDStore.YN_YES?.id) {
                        use(TimeCategory) {
                            newStartDate = oldSubofParticipant?.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                            newEndDate = oldSubofParticipant?.endDate ? (oldSubofParticipant.endDate + 3.year) : null
                        }
                        countNewSubs++
                        result.newSubs << processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it?.participant, newStartDate, newEndDate, true, params)
                    }
                }else {
                    use(TimeCategory) {
                        newStartDate = oldSubofParticipant?.startDate ? (oldSubofParticipant.endDate + 1.day) : null
                        newEndDate = oldSubofParticipant?.endDate ? (oldSubofParticipant.endDate + 1.year) : null
                    }
                    countNewSubs++
                    result.newSubs << processAddMember(((oldSubofParticipant != result.parentSubscription) ? oldSubofParticipant: null), result.parentSuccessorSubscription, it?.participant, newStartDate, newEndDate, false, params)
                }
            }
        }

        //Späteinsteiger

        //MultiYearTerm Subs  //Späteinsteiger
        result.parentSubChilds?.each { sub ->
            if (sub?.isCurrentMultiYearSubscriptionNew()){
                sub?.getAllSubscribers()?.each { org ->
                    if (!(org in result.parentSuccessortParticipantsList)) {

                        countNewSubs++
                        result.newSubs << processAddMember(sub, result.parentSuccessorSubscription, org, sub.startDate, sub.endDate, true, params)
                    }
                }
            }
            else if(sub.islateCommer()){
                sub?.getAllSubscribers()?.each { org ->
                    if (!(org in result.parentSuccessortParticipantsList)) {

                        countNewSubs++
                        result.newSubs << processAddMember(sub, result.parentSuccessorSubscription, org, sub.startDate, sub.endDate, false, params)
                    }
                }
            }

        }

        result.countNewSubs = countNewSubs
        if(result.newSubs) {
            result.parentSuccessorSubscription.syncAllShares(result.newSubs)
        }



        redirect(action: 'renewalwithSurvey', id: params.id, params: [surveyConfigID: result.surveyConfig?.id])


    }

    private def processAddMember(def oldSub, Subscription newParentSub, Org org, Date newStartDate, Date newEndDate, boolean multiYear, params) {

        List orgType = [RDStore.OT_INSTITUTION.id.toString()]
        if (accessService.checkPerm("ORG_CONSORTIUM")) {
            orgType = [RDStore.OT_CONSORTIUM.id.toString()]
        }

        def institution = contextService.getOrg()

        RefdataValue subStatus = multiYear ? RDStore.SUBSCRIPTION_INTENDED_PERENNIAL : RDStore.SUBSCRIPTION_INTENDED

        RefdataValue role_sub = RDStore.OR_SUBSCRIBER_CONS
        RefdataValue role_sub_cons = RDStore.OR_SUBSCRIPTION_CONSORTIA
        RefdataValue role_coll = RDStore.OR_SUBSCRIBER_COLLECTIVE
        RefdataValue role_sub_coll = RDStore.OR_SUBSCRIPTION_COLLECTIVE
        RefdataValue role_sub_hidden = RDStore.OR_SUBSCRIBER_CONS_HIDDEN
        RefdataValue role_lic = RDStore.OR_LICENSEE_CONS
        if(accessService.checkPerm("ORG_INST_COLLECTIVE")) {
            role_lic = RDStore.OR_LICENSEE_COLL
        }
        RefdataValue role_lic_cons = RDStore.OR_LICENSING_CONSORTIUM

        RefdataValue role_provider = RDStore.OR_PROVIDER
        RefdataValue role_agency = RDStore.OR_AGENCY

        if (accessService.checkPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM")) {

                License licenseCopy

                def subLicense = newParentSub.owner

                Set<Package> packagesToProcess = []

                //copy package data
                if(params.linkAllPackages) {
                    newParentSub.packages.each { sp ->
                        packagesToProcess << sp.pkg
                    }
                }else if(params.packageSelection) {
                    List packageIds = params.list("packageSelection")
                    packageIds.each { spId ->
                        packagesToProcess << SubscriptionPackage.get(spId).pkg
                    }
                }

                    if(accessService.checkPerm("ORG_INST_COLLECTIVE,ORG_CONSORTIUM")) {
                       // def postfix = (members.size() > 1) ? 'Teilnehmervertrag' : (cm.shortname ?: cm.name)
                        def postfix = 'Teilnehmervertrag'

                        if (subLicense) {
                            def subLicenseParams = [
                                    lic_name     : "${subLicense.reference} (${postfix})",
                                    isSlaved     : params.isSlaved,
                                    asOrgType: orgType,
                                    copyStartEnd : true
                            ]

                            if (params.generateSlavedLics == 'explicit') {
                                licenseCopy = institutionsService.copyLicense(
                                        subLicense, subLicenseParams, InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                            }
                            else if (params.generateSlavedLics == 'shared' && !licenseCopy) {
                                licenseCopy = institutionsService.copyLicense(
                                        subLicense, subLicenseParams, InstitutionsService.CUSTOM_PROPERTIES_ONLY_INHERITED)
                            }
                            else if ((params.generateSlavedLics == 'reference' || params.attachToParticipationLic == "true") && !licenseCopy) {
                                licenseCopy = genericOIDService.resolveOID(params.generateSlavedLicsReference)
                            }

                            if (licenseCopy) {
                                new OrgRole(org: org, lic: licenseCopy, roleType: role_lic).save()
                            }
                        }
                    }

                    log.debug("Generating seperate slaved instances for members")

                    SimpleDateFormat sdf = new DateUtil().getSimpleDateFormat_NoTime()
                    Date startDate = newStartDate ?: null
                    Date endDate = newEndDate ?: null

                    Subscription memberSub = new Subscription(
                            type: newParentSub.type ?: null,
                            status: subStatus,
                            name: newParentSub.name,
                            startDate: startDate,
                            endDate: endDate,
                            administrative: newParentSub.getCalculatedType() == TemplateSupport.CALCULATED_TYPE_ADMINISTRATIVE,
                            manualRenewalDate: newParentSub.manualRenewalDate,
                            identifier: UUID.randomUUID().toString(),
                            instanceOf: newParentSub,
                            isSlaved: true,
                            isPublic: newParentSub.isPublic,
                            impId: UUID.randomUUID().toString(),
                            owner: licenseCopy,
                            resource: newParentSub.resource ?: null,
                            form: newParentSub.form ?: null,
                            isMultiYear: multiYear ?: false
                    )

                    if (!memberSub.save()) {
                        memberSub?.errors.each { e ->
                            log.debug("Problem creating new sub: ${e}")
                        }
                    }

                    if (memberSub) {
                        if(accessService.checkPerm("ORG_CONSORTIUM")) {

                            new OrgRole(org: org, sub: memberSub, roleType: role_sub).save()
                            new OrgRole(org: institution, sub: memberSub, roleType: role_sub_cons).save()

                            if(params.transferProviderAgency) {
                                newParentSub.getProviders().each { provider ->
                                    new OrgRole(org: provider, sub: memberSub, roleType: role_provider).save()
                                }
                                newParentSub.getAgencies().each { provider ->
                                    new OrgRole(org: provider, sub: memberSub, roleType: role_agency).save()
                                }
                            }else if(params.providersSelection) {
                                List orgIds = params.list("providersSelection")
                                orgIds.each { orgID ->
                                    new OrgRole(org: Org.get(orgID), sub: memberSub, roleType: role_provider).save()
                                }
                            }else if(params.agenciesSelection) {
                                List orgIds = params.list("agenciesSelection")
                                orgIds.each { orgID ->
                                    new OrgRole(org: Org.get(orgID), sub: memberSub, roleType: role_agency).save()
                                }
                            }

                        }

                        SubscriptionCustomProperty.findAllByOwner(newParentSub).each { scp ->
                            AuditConfig ac = AuditConfig.getConfig(scp)

                            if (ac) {
                                // multi occurrence props; add one additional with backref
                                if (scp.type.multipleOccurrence) {
                                    def additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type)
                                    additionalProp = scp.copyInto(additionalProp)
                                    additionalProp.instanceOf = scp
                                    additionalProp.save(flush: true)
                                }
                                else {
                                    // no match found, creating new prop with backref
                                    def newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, memberSub, scp.type)
                                    newProp = scp.copyInto(newProp)
                                    newProp.instanceOf = scp
                                    newProp.save(flush: true)
                                }
                            }
                        }

                        packagesToProcess.each { pkg ->
                            if(params.linkWithEntitlements)
                                pkg.addToSubscriptionCurrentStock(memberSub, newParentSub)
                            else
                                pkg.addToSubscription(memberSub, false)
                        }

                        if(oldSub){
                            new Links(linkType: RDStore.LINKTYPE_FOLLOWS, source: memberSub.id, destination: oldSub.id, owner: contextService.getOrg(), objectType:Subscription.class.name).save(flush: true)
                        }

                        return memberSub
                    }
            }
    }

    private getSurveyProperties(Org contextOrg) {
        def props = []

        //private Property
        SurveyProperty.findAllByOwnerAndOwnerIsNotNull(contextOrg).each { it ->
            props << it

        }

        //global Property
        SurveyProperty.findAllByOwnerIsNull().each { it ->
            props << it

        }

        props.sort { a, b -> a.getI10n('name').compareToIgnoreCase b.getI10n('name') }

        return props
    }

    private def addSubMembers(SurveyConfig surveyConfig) {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = (accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR') && surveyConfig.surveyInfo?.owner?.id == contextService.getOrg()?.id)

        if (!result.editable) {
            return
        }

        def orgs = []
        def currentMembersSubs = subscriptionService.getCurrentValidSubChilds(surveyConfig.subscription)

        currentMembersSubs.each{ sub ->
            sub?.getAllSubscribers()?.each { org ->
                orgs << org
            }
        }

        if (orgs) {

            orgs.each { org ->

                if (!(SurveyOrg.findAllBySurveyConfigAndOrg(surveyConfig, org))) {

                    boolean existsMultiYearTerm = false
                    def sub = surveyConfig?.subscription
                    if (sub) {
                        def subChild = sub?.getDerivedSubscriptionBySubscribers(org)
                        def property = PropertyDefinition.findByName("Mehrjahreslaufzeit ausgewählt")

                        if (subChild?.isCurrentMultiYearSubscription()) {
                            existsMultiYearTerm = true
                        }

                        if (!existsMultiYearTerm && property?.type == 'class com.k_int.kbplus.RefdataValue') {
                            if (subChild?.customProperties?.find {
                                it?.type?.id == property?.id
                            }?.refValue == RefdataValue.getByValueAndCategory('Yes', property?.refdataCategory)) {
                                existsMultiYearTerm = true
                                return existsMultiYearTerm
                            }
                        }
                    }
                    if (!existsMultiYearTerm) {
                        def surveyOrg = new SurveyOrg(
                                surveyConfig: surveyConfig,
                                org: org
                        )

                        if (!surveyOrg.save(flush: true)) {
                            log.debug("Error by add Org to SurveyOrg ${surveyOrg.errors}");
                        }
                    }
                }
            }

        }
    }

    private def deleteSubMembers(SurveyConfig surveyConfig) {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = (accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR') && surveyConfig.surveyInfo?.owner?.id == contextService.getOrg()?.id)

        if (!result.editable) {
            return
        }

        def orgs = com.k_int.kbplus.Subscription.get(surveyConfig.subscription?.id)?.getDerivedSubscribers()

        if (orgs) {

            orgs.each { org ->
                CostItem.findBySurveyOrg(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, org))?.delete(flush: true)
                SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, org)?.delete(flush: true)
            }
        }
    }

    private static def getSubscriptionMembers(Subscription subscription) {
        def result = []

        Subscription.findAllByInstanceOf(subscription).each { s ->
            def ors = OrgRole.findAllWhere(sub: s)
            ors.each { or ->
                if (or.roleType?.value in ['Subscriber', 'Subscriber_Consortial']) {
                    result << or.org
                }
            }
        }
        result = result.sort { it.name }
    }

    private static def getfilteredSurveyOrgs(List orgIDs, String query, queryParams, params) {

        if (!(orgIDs?.size() > 0)) {
            return []
        }
        def tmpQuery = query
        tmpQuery = tmpQuery.replace("order by", "and o.id in (:orgIDs) order by")

        def tmpQueryParams = queryParams
        tmpQueryParams.put("orgIDs", orgIDs)

        return Org.executeQuery(tmpQuery, tmpQueryParams, params)
    }


    private def exportSurveyParticipantResult(List<SurveyResult> results, String format, Org org) {
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notime'))
        List titles = [g.message(code: 'surveyInfo.owner.label'),

                       g.message(code: 'surveyConfigsInfo.comment'),
                       g.message(code: 'surveyParticipants.label'),
                       g.message(code: 'org.shortname.label'),
                       g.message(code: 'surveyProperty.subName'),
                       g.message(code: 'surveyProperty.subProvider'),
                       g.message(code: 'surveyProperty.subAgency'),
                       g.message(code: 'subscription.owner.label'),
                       g.message(code: 'subscription.packages.label'),
                       g.message(code: 'subscription.details.status'),
                       g.message(code: 'subscription.details.type'),
                       g.message(code: 'subscription.form.label'),
                       g.message(code: 'subscription.resource.label'),

                       g.message(code: 'surveyConfigsInfo.newPrice'),
                       g.message(code: 'surveyConfigsInfo.newPrice.comment'),

                       g.message(code: 'surveyProperty.label'),
                       g.message(code: 'surveyProperty.type.label'),
                       g.message(code: 'surveyResult.result'),
                       g.message(code: 'surveyResult.comment'),
                       g.message(code: 'surveyResult.finishDate')]

        List surveyData = []
        results.findAll { it.surveyConfig.type == 'Subscription' }.each { result ->
            List row = []
            switch (format) {
                case "xls":
                case "xlsx":

                    def sub = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(result?.participant)

                    def surveyCostItem = CostItem.findBySurveyOrg(SurveyOrg.findBySurveyConfigAndOrg(result?.surveyConfig, result?.participant))

                    row.add([field: result?.owner?.name ?: '', style: null])
                    row.add([field: result?.surveyConfig?.comment ?: '', style: null])
                    row.add([field: result?.participant?.name ?: '', style: null])
                    row.add([field: result?.participant?.shortname ?: '', style: null])
                    row.add([field: sub?.name ?: "", style: null])


                    row.add([field: sub?.providers ? sub?.providers?.join(", ") : '', style: null])
                    row.add([field: sub?.agencies ? sub?.agencies?.join(", ") : '', style: null])

                    row.add([field: sub?.owner?.reference ?: '', style: null])
                    List packageNames = sub?.packages?.collect {
                        it.pkg.name
                    }
                    row.add([field: packageNames ? packageNames.join(", ") : '', style: null])
                    row.add([field: sub?.status?.getI10n("value") ?: '', style: null])
                    row.add([field: sub?.type?.getI10n("value") ?: '', style: null])
                    row.add([field: sub?.form?.getI10n("value") ?: '', style: null])
                    row.add([field: sub?.resource?.getI10n("value") ?: '', style: null])

                    row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ? surveyCostItem?.costInBillingCurrencyAfterTax : '', style: null])

                    row.add([field: surveyCostItem?.costDescription ?: '', style: null])

                    row.add([field: result.type?.getI10n('name') ?: '', style: null])
                    row.add([field: result.type?.getLocalizedType() ?: '', style: null])

                    def value = ""

                    if (result?.type?.type == Integer.toString()) {
                        value = result?.intValue ? result?.intValue.toString() : ""
                    } else if (result?.type?.type == String.toString()) {
                        value = result?.stringValue ?: ""
                    } else if (result?.type?.type == BigDecimal.toString()) {
                        value = result?.decValue ? result?.decValue.toString() : ""
                    } else if (result?.type?.type == Date.toString()) {
                        value = result?.dateValue ? sdf.format(result?.dateValue) : ""
                    } else if (result?.type?.type == URL.toString()) {
                        value = result?.urlValue ? result?.urlValue.toString() : ""
                    } else if (result?.type?.type == RefdataValue.toString()) {
                        value = result?.refValue ? result?.refValue.getI10n('value') : ""
                    }

                    def surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(result?.surveyConfig, result?.participant)

                    if (surveyOrg?.existsMultiYearTerm()) {
                        value = g.message(code: "surveyOrg.perennialTerm.available")
                    }

                    row.add([field: value ?: '', style: null])
                    row.add([field: result.comment ?: '', style: null])
                    row.add([field: result.finishDate ? sdf.format(result?.finishDate) : '', style: null])

                    surveyData.add(row)
                    break
            }
        }
        switch (format) {
            case 'xls':
            case 'xlsx':
                Map sheetData = [:]
                sheetData[message(code: 'menu.my.surveys')] = [titleRow: titles, columnData: surveyData]
                return exportService.generateXLSXWorkbook(sheetData)
        }
    }

    private def exportRenewalResult(Map renewalResult) {
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notime'))
        List titles = [g.message(code: 'org.sortname.label'),
                       g.message(code: 'org.name.label'),

                       renewalResult.participationProperty?.getI10n('name'),
                       g.message(code: 'surveyResult.participantComment') + " " + renewalResult.participationProperty?.getI10n('name')
        ]


        titles << g.message(code: 'renewalwithSurvey.period')

        if (renewalResult?.multiYearTermTwoSurvey || renewalResult?.multiYearTermThreeSurvey)
        {
            titles << g.message(code: 'renewalwithSurvey.periodComment')
        }

        renewalResult.properties.each { surveyProperty ->
            titles << surveyProperty?.getI10n('name')
            titles << g.message(code: 'surveyResult.participantComment') + " " + g.message(code: 'renewalwithSurvey.exportRenewal.to') +" " + surveyProperty?.getI10n('name')
        }
        titles << g.message(code: 'renewalwithSurvey.costBeforeTax')
        titles << g.message(code: 'renewalwithSurvey.costAfterTax')
        titles << g.message(code: 'renewalwithSurvey.costTax')
        titles << g.message(code: 'renewalwithSurvey.currency')

        List renewalData = []

        renewalData.add([[field: g.message(code: 'renewalwithSurvey.continuetoSubscription.label')+ " (${renewalResult.orgsContinuetoSubscription?.size() ?: 0})", style: 'positive']])

        renewalResult.orgsContinuetoSubscription.each { participantResult ->
            List row = []

            row.add([field: participantResult?.participant?.sortname ?: '', style: null])
            row.add([field: participantResult?.participant?.name ?: '', style: null])
            row.add([field: participantResult?.resultOfParticipation?.getResult() ?: '', style: null])

            row.add([field: participantResult?.resultOfParticipation?.comment ?: '', style: null])


            def period = ""
            if (renewalResult?.multiYearTermTwoSurvey) {
                period = participantResult?.newSubPeriodTwoStartDate ? sdf.format(participantResult?.newSubPeriodTwoStartDate) : ""
                period = participantResult?.newSubPeriodTwoEndDate ? period + " - " +sdf.format(participantResult?.newSubPeriodTwoEndDate) : ""
            }

            if (renewalResult?.multiYearTermThreeSurvey) {
                period = participantResult?.newSubPeriodThreeStartDate ? sdf.format(participantResult?.newSubPeriodThreeStartDate) : ""
                period = participantResult?.newSubPeriodThreeEndDate ? period + " - " +sdf.format(participantResult?.newSubPeriodThreeEndDate) : ""
            }

            row.add([field: period ?: '', style: null])

            if (renewalResult?.multiYearTermTwoSurvey) {
                row.add([field: participantResult?.participantPropertyTwoComment ?: '', style: null])
            }

            if (renewalResult?.multiYearTermThreeSurvey) {
                row.add([field: participantResult?.participantPropertyThreeComment ?: '', style: null])
            }

            participantResult?.properties.sort { it?.type?.name }.each { participantResultProperty ->
                row.add([field: participantResultProperty?.getResult() ?: "", style: null])

                row.add([field: participantResultProperty?.comment ?: "", style: null])

            }

            def costItem = CostItem.findBySurveyOrg(SurveyOrg.findBySurveyConfigAndOrg(participantResult?.resultOfParticipation?.surveyConfig, participantResult?.participant))

            row.add([field: costItem?.costInBillingCurrency ? costItem?.costInBillingCurrency : "", style: null])
            row.add([field: costItem?.costInBillingCurrencyAfterTax ? costItem?.costInBillingCurrencyAfterTax : "", style: null])
            row.add([field: costItem?.taxKey ? costItem?.taxKey?.taxRate+'%' : "", style: null])
            row.add([field: costItem?.billingCurrency ? costItem?.billingCurrency?.getI10n('value')?.split('-').first() : "", style: null])


            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: g.message(code: 'renewalwithSurvey.withMultiYearTermSub.label')+ " (${renewalResult.orgsWithMultiYearTermSub?.size() ?: 0})", style: 'positive']])


        renewalResult.orgsWithMultiYearTermSub.each { sub ->
            List row = []

            sub.getAllSubscribers().each{ subscriberOrg ->

                row.add([field: subscriberOrg?.sortname ?: '', style: null])
                row.add([field: subscriberOrg?.name ?: '', style: null])

                row.add([field: '', style: null])

                row.add([field: '', style: null])

                def period = ""

                period = sub?.startDate ? sdf.format(sub?.startDate) : ""
                period = sub?.endDate ? period + " - " +sdf.format(sub?.endDate) : ""

                row.add([field: period?: '', style: null])

                if (renewalResult?.multiYearTermTwoSurvey || renewalResult?.multiYearTermThreeSurvey)
                {
                    row.add([field: '', style: null])
                }

            }


            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: g.message(code: 'renewalwithSurvey.orgsWithParticipationInParentSuccessor.label')+ " (${renewalResult.orgsWithParticipationInParentSuccessor?.size() ?: 0})", style: 'positive']])


        renewalResult.orgsWithParticipationInParentSuccessor.each { sub ->
            List row = []

            sub.getAllSubscribers().each{ subscriberOrg ->

                row.add([field: subscriberOrg?.sortname ?: '', style: null])
                row.add([field: subscriberOrg?.name ?: '', style: null])

                row.add([field: '', style: null])

                row.add([field: '', style: null])

                def period = ""

                period = sub?.startDate ? sdf.format(sub?.startDate) : ""
                period = sub?.endDate ? period + " - " +sdf.format(sub?.endDate) : ""

                row.add([field: period?: '', style: null])

                if (renewalResult?.multiYearTermTwoSurvey || renewalResult?.multiYearTermThreeSurvey)
                {
                    row.add([field: '', style: null])
                }
            }


            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: g.message(code: 'renewalwithSurvey.orgsLateCommers.label')+ " (${renewalResult.orgsLateCommers?.size() ?: 0})", style: 'positive']])


        renewalResult.orgsLateCommers.each { sub ->
            List row = []

            sub.getAllSubscribers().each{ subscriberOrg ->

                row.add([field: subscriberOrg?.sortname ?: '', style: null])
                row.add([field: subscriberOrg?.name ?: '', style: null])

                row.add([field: '', style: null])

                row.add([field: '', style: null])

                def period = ""

                period = sub?.startDate ? sdf.format(sub?.startDate) : ""
                period = sub?.endDate ? period + " - " +sdf.format(sub?.endDate) : ""

                row.add([field: period?: '', style: null])

                if (renewalResult?.multiYearTermTwoSurvey || renewalResult?.multiYearTermThreeSurvey)
                {
                    row.add([field: '', style: null])
                }
            }


            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: g.message(code: 'renewalwithSurvey.newOrgstoSubscription.label')+ " (${renewalResult.newOrgstoSubscription?.size() ?: 0})", style: 'positive']])


        renewalResult.newOrgsContinuetoSubscription.each { participantResult ->
            List row = []

            row.add([field: participantResult?.participant?.sortname ?: '', style: null])
            row.add([field: participantResult?.participant?.name ?: '', style: null])

            row.add([field: participantResult?.resultOfParticipation?.getResult() ?: '', style: null])

            row.add([field: participantResult?.resultOfParticipation?.comment ?: '', style: null])


            def period = ""
            if (renewalResult?.multiYearTermTwoSurvey) {
                period = participantResult?.newSubPeriodTwoStartDate ? sdf.format(participantResult?.newSubPeriodTwoStartDate) : ""
                period = period + " - " + participantResult?.newSubPeriodTwoEndDate ? sdf.format(participantResult?.newSubPeriodTwoEndDate) : ""
            }
            period = ""
            if (renewalResult?.multiYearTermThreeSurvey) {
                period = participantResult?.newSubPeriodThreeStartDate ?: ""
                period = period + " - " + participantResult?.newSubPeriodThreeEndDate ?: ""
            }
            row.add([field: period ?: '', style: null])

            if (renewalResult?.multiYearTermTwoSurvey) {
                row.add([field: participantResult?.participantPropertyTwoComment ?: '', style: null])
            }

            if (renewalResult?.multiYearTermThreeSurvey) {
                row.add([field: participantResult?.participantPropertyThreeComment ?: '', style: null])
            }

            participantResult?.properties.sort {
                it?.type?.name
            }.each { participantResultProperty ->
                row.add([field: participantResultProperty?.getResult() ?: "", style: null])

                row.add([field: participantResultProperty?.comment ?: "", style: null])

            }

            def costItem = CostItem.findBySurveyOrg(SurveyOrg.findBySurveyConfigAndOrg(participantResult?.resultOfParticipation?.surveyConfig, participantResult?.participant))
            row.add([field: costItem?.costInBillingCurrency ? costItem?.costInBillingCurrency : "", style: null])
            row.add([field: costItem?.costInBillingCurrencyAfterTax ? costItem?.costInBillingCurrencyAfterTax : "", style: null])
            row.add([field: costItem?.taxKey ? costItem?.taxKey?.taxRate+'%' : "", style: null])
            row.add([field: costItem?.billingCurrency ? costItem?.billingCurrency?.getI10n('value')?.split('-').first() : "", style: null])

            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: g.message(code: 'renewalwithSurvey.withTermination.label')+ " (${renewalResult.orgsWithTermination?.size() ?: 0})", style: 'negative']])


        renewalResult.orgsWithTermination.each { participantResult ->
            List row = []

            row.add([field: participantResult?.participant?.sortname ?: '', style: null])
            row.add([field: participantResult?.participant?.name ?: '', style: null])

            row.add([field: participantResult?.resultOfParticipation?.getResult() ?: '', style: null])

            row.add([field: participantResult?.resultOfParticipation?.comment ?: '', style: null])

            row.add([field: '', style: null])

            if (renewalResult?.multiYearTermTwoSurvey || renewalResult?.multiYearTermThreeSurvey)
            {
                row.add([field: '', style: null])
            }

            participantResult?.properties.sort {
                it?.type?.name
            }.each { participantResultProperty ->
                row.add([field: participantResultProperty?.getResult() ?: "", style: null])

                row.add([field: participantResultProperty?.comment ?: "", style: null])

            }

            def costItem = CostItem.findBySurveyOrg(SurveyOrg.findBySurveyConfigAndOrg(participantResult?.resultOfParticipation?.surveyConfig, participantResult?.participant))

            row.add([field: costItem?.costInBillingCurrency ? costItem?.costInBillingCurrency : "", style: null])
            row.add([field: costItem?.costInBillingCurrencyAfterTax ? costItem?.costInBillingCurrencyAfterTax : "", style: null])
            row.add([field: costItem?.taxKey ? costItem?.taxKey?.taxRate+'%' : "", style: null])
            row.add([field: costItem?.billingCurrency ? costItem?.billingCurrency?.getI10n('value')?.split('-').first() : "", style: null])

            renewalData.add(row)
        }


        Map sheetData = [:]
        sheetData[message(code: 'renewalexport.renewals')] = [titleRow: titles, columnData: renewalData]
        return exportService.generateXLSXWorkbook(sheetData)
    }

    private def exportSurveyParticipantResultMin(List<SurveyResult> results, String format, Org org) {
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notime'))
        List titles = [
                g.message(code: 'org.shortname.label'),
                g.message(code: 'surveyParticipants.label'),

                g.message(code: 'surveyProperty.subName'),

                g.message(code: 'surveyConfigsInfo.newPrice'),
                g.message(code: 'surveyConfigsInfo.newPrice.comment'),
                g.message(code: 'surveyResult.finishDate')
        ]

        results.groupBy {
            it?.type.id
        }.sort { it?.value[0]?.type?.name }.each { property ->
            titles << SurveyProperty.get(property.key)?.getI10n('name')
            titles << g.message(code: 'surveyResult.participantComment')
        }

        List surveyData = []
        results.findAll { it.surveyConfig.type == 'Subscription' }.groupBy { it?.participant.id }.each { result ->
            List row = []
            switch (format) {
                case "xls":
                case "xlsx":

                    def participant = Org.get(result?.key)

                    row.add([field: participant?.shortname ?: '', style: null])
                    row.add([field: participant?.name ?: '', style: null])

                    row.add([field: result?.value[0]?.surveyConfig?.getConfigNameShort() ?: "", style: null])
                    def surveyCostItem = CostItem.findBySurveyOrg(SurveyOrg.findBySurveyConfigAndOrg(result?.value[0]?.surveyConfig, participant))

                    row.add([field: surveyCostItem?.costInBillingCurrencyAfterTax ? surveyCostItem?.costInBillingCurrencyAfterTax : '', style: null])

                    row.add([field: surveyCostItem?.costDescription ?: '', style: null])

                    row.add([field: result?.value[0]?.finishDate ? sdf.format(result?.value[0]?.finishDate) : '', style: null])

                    result.value.sort { it?.type?.name }.each { resultProperty ->

                        def surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(resultProperty?.surveyConfig, participant)

                        def value = ""

                        if (resultProperty?.type?.type == Integer.toString()) {
                            value = resultProperty?.intValue ? resultProperty?.intValue.toString() : ""
                        } else if (resultProperty?.type?.type == String.toString()) {
                            value = resultProperty?.stringValue ?: ""
                        } else if (resultProperty?.type?.type == BigDecimal.toString()) {
                            value = resultProperty?.decValue ? resultProperty?.decValue.toString() : ""
                        } else if (resultProperty?.type?.type == Date.toString()) {
                            value = resultProperty?.dateValue ? sdf.format(resultProperty?.dateValue) : ""
                        } else if (resultProperty?.type?.type == URL.toString()) {
                            value = resultProperty?.urlValue ? resultProperty?.urlValue.toString() : ""
                        } else if (resultProperty?.type?.type == RefdataValue.toString()) {
                            value = resultProperty?.refValue ? resultProperty?.refValue.getI10n('value') : ""
                        }

                        if (surveyOrg?.existsMultiYearTerm()) {
                            value = g.message(code: "surveyOrg.perennialTerm.available")
                        }

                        row.add([field: value ?: '', style: null])
                        row.add([field: resultProperty.comment ?: '', style: null])
                    }

                    surveyData.add(row)
                    break
            }
        }
        switch (format) {
            case 'xls':
            case 'xlsx':
                Map sheetData = [:]
                sheetData[message(code: 'menu.my.surveys')] = [titleRow: titles, columnData: surveyData]
                return exportService.generateXLSXWorkbook(sheetData)
        }
    }

    private def exportSurveyCostItems(SurveyConfig surveyConfig, String format, Org org) {
        SimpleDateFormat sdf = new SimpleDateFormat(g.message(code: 'default.date.format.notime'))
        List titles = ['Name',
                       '',
                       g.message(code: 'surveyConfig.type.label'),
                       g.message(code: 'surveyConfigsInfo.comment'),

                       g.message(code: 'surveyParticipants.label'),
                       g.message(code: 'org.shortname.label'),
                       g.message(code: 'org.libraryNetwork.label'),
                       g.message(code: 'surveyProperty.subName'),
                       g.message(code: 'surveyConfigsInfo.newPrice'),
                       g.message(code: 'surveyConfigsInfo.newPrice.comment')
        ]

        List surveyData = []

        def surveyOrgs = SurveyOrg.findAllBySurveyConfig(surveyConfig)

        surveyOrgs.each { surveyOrg ->
            List row = []

            row.add([field: surveyConfig?.surveyInfo?.name ?: '', style: null])

            row.add([field: surveyConfig?.getConfigName() ?: '', style: null])

            row.add([field: surveyConfig?.type == 'Subscription' ? com.k_int.kbplus.SurveyConfig.getLocalizedValue(surveyConfig?.type) : com.k_int.kbplus.SurveyConfig.getLocalizedValue(config?.type) + '(' + surveyConfig?.surveyProperty?.getLocalizedType() + ')', style: null])

            row.add([field: surveyConfig?.comment ?: '', style: null])

            row.add([field: surveyOrg?.org?.name ?: '', style: null])

            row.add([field: surveyOrg?.org?.shortname ?: '', style: null])

            row.add([field: surveyOrg?.org?.libraryType?.getI10n('value') ?: '', style: null])

            row.add([field: surveyConfig?.subscription?.getDerivedSubscriptionBySubscribers(surveyOrg?.org)?.name ?: '', style: null])


            def costItem = CostItem.findBySurveyOrg(surveyOrg)

            if (!surveyOrg?.existsMultiYearTerm()) {
                if (costItem) {
                    row.add([field: g.formatNumber(number: costItem?.costInBillingCurrencyAfterTax, minFractionDigits: 2, maxFractionDigits: 2, type: "number") + costItem?.billingCurrency?.getI10n('value').split('-').first(), style: null])
                }
            } else {
                row.add([field: g.message(code: "surveyOrg.perennialTerm.available"), style: null])
            }

            row.add([field: costItem.costDescription ?: '', style: null])

            surveyData.add(row)
        }

        switch (format) {
            case 'xls':
            case 'xlsx':
                Map sheetData = [:]
                sheetData[message(code: 'menu.my.surveys')] = [titleRow: titles, columnData: surveyData]
                return exportService.generateXLSXWorkbook(sheetData)
        }
    }

    private def getSurveyConfigCounts() {
        def result = [:]

        def contextOrg = contextService.getOrg()

        result.created = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and (surInfo.status = :status or surInfo.status = :status2)",
                [contextOrg: contextOrg, status: RDStore.SURVEY_READY, status2: RDStore.SURVEY_IN_PROCESSING]).size()

        result.active = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_SURVEY_STARTED]).size()

        result.finish = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_SURVEY_COMPLETED]).size()

        result.inEvaluation = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_IN_EVALUATION]).size()

        result.completed = SurveyConfig.executeQuery("from SurveyInfo surInfo left join surInfo.surveyConfigs surConfig where surInfo.owner = :contextOrg and surInfo.status = :status",
                [contextOrg: contextOrg, status: RDStore.SURVEY_COMPLETED]).size()

        return result
    }

    private LinkedHashMap setResultGenericsAndCheckAccess() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)
        result.surveyInfo = SurveyInfo.get(params.id)
        result.surveyConfig = SurveyConfig.get(params.surveyConfigID) ?: (result.surveyInfo?.isSubscriptionSurvey ? result.surveyInfo?.surveyConfigs[0] : result.surveyInfo?.surveyConfigs[0])

        result.editable = result.surveyInfo?.isEditable() ?: false


        result
    }

    private LinkedHashMap setResultGenericsAndCheckAccessforSub(checkOption) {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)
        result.subscription = Subscription.get(params.id)
        result.institution = result.subscription?.subscriber

        if (checkOption in [AccessService.CHECK_VIEW, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (!result.subscriptionInstance?.isVisibleBy(result.user)) {
                log.debug("--- NOT VISIBLE ---")
                return null
            }
        }
        result.editable = result.subscriptionInstance?.isEditableBy(result.user)

        if (checkOption in [AccessService.CHECK_EDIT, AccessService.CHECK_VIEW_AND_EDIT]) {
            if (!result.editable) {
                log.debug("--- NOT EDITABLE ---")
                return null
            }
        }

        result
    }
    def parseDate(datestr, possible_formats) {
        def parsed_date = null;
        if (datestr && (datestr.toString().trim().length() > 0)) {
            for (Iterator i = possible_formats.iterator(); (i.hasNext() && (parsed_date == null));) {
                try {
                    parsed_date = i.next().parse(datestr.toString());
                }
                catch (Exception e) {
                }
            }
        }
        parsed_date
    }

}
