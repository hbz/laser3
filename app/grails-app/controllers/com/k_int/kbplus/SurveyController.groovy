package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import de.laser.helper.DateUtil
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.dao.DataIntegrityViolationException

import javax.servlet.ServletOutputStream
import java.text.DateFormat
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

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def currentSurveysConsortia() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;


        DateFormat sdFormat = new DateUtil().getSimpleDateFormat_NoTime()
        def fsq = filterService.getSurveyQueryConsortia(params, sdFormat, result.institution)

        result.surveys = SurveyInfo.findAll(fsq.query, fsq.queryParams, params)
        result.countSurvey = SurveyInfo.executeQuery("select si.id ${fsq.query}", fsq.queryParams).size()

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def createSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def processCreateSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

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
                status: RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung']),
                comment: params.comment ?: null
        )

        if (!(surveyInfo.save(flush: true))) {
            flash.error = g.message(code: "createSurvey.create.fail")
            redirect(url: request.getHeader('referer'))
        }
        flash.message = g.message(code: "createSurvey.create.successfull")
        redirect action: 'show', id: surveyInfo.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def show() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.editable = (result.surveyInfo && result.surveyInfo?.status != RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])) ? false : true

        result.surveyConfigs = result.surveyInfo?.surveyConfigs?.sort { it?.configOrder }

        result

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def surveyConfigs() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyProperties = SurveyProperty.findAllByOwner(result.institution)

        result.properties = getSurveyProperties(result.institution)

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.editable = (result.surveyInfo.status != RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])) ? false : true

        result.surveyConfigs = result.surveyInfo.surveyConfigs.sort { it?.configOrder }

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def allSubscriptions() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP()
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0

        def date_restriction = null;
        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        if (params.validOn == null || params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        if (!params.status) {
            if (params.isSiteReloaded != "yes") {
                params.status = RDStore.SUBSCRIPTION_CURRENT.id
                result.defaultSet = true
            } else {
                params.status = 'FETCH_ALL'
            }
        }

        List<Org> providers = orgTypeService.getCurrentProviders( contextService.getOrg())
        List<Org> agencies   = orgTypeService.getCurrentAgencies( contextService.getOrg())

        providers.addAll(agencies)
        List orgIds = providers.unique().collect{ it2 -> it2.id }

        result.providers = Org.findAllByIdInList(orgIds).sort{it?.name}

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

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def surveyConfigDocs() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }


        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result.surveyConfigs = result.surveyInfo.surveyConfigs?.sort { it?.configOrder }

        params.surveyConfigID = params.surveyConfigID ?: result.surveyConfigs[0]?.id?.toString()

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def surveyParticipants() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
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
        result.consortiaMembers = Org.executeQuery(fsq.query, fsq.queryParams, params)
        result.consortiaMembersCount = Org.executeQuery(fsq.query, fsq.queryParams).size()

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.editable = (result.surveyInfo.status != RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])) ? false : true

        result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

        params.surveyConfigID = params.surveyConfigID ?: result?.surveyConfigs[0]?.id?.toString()

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        result.surveyConfigSubOrgs = com.k_int.kbplus.Subscription.get(result.surveyConfig?.subscription?.id)?.getDerivedSubscribers()

        result.surveyConfigOrgs = Org.findAllByIdInList(SurveyConfig.get(params.surveyConfigID)?.orgIDs)

        result.selectedParticipants = Org.findAllByIdInList(SurveyConfig.get(params.surveyConfigID)?.orgIDs) - result.surveyConfigSubOrgs
        result.selectedSubParticipants = Org.findAllByIdInList(SurveyConfig.get(params.surveyConfigID)?.orgIDs) - result.selectedParticipants

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def surveyEvaluation() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        params.tab = params.tab ?: 'selectedSubParticipants'

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.editable = (result.surveyInfo.status != RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])) ? false : true

        result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

        params.surveyConfigID = params.surveyConfigID ?: result?.surveyConfigs[0]?.id?.toString()

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        result.surveyConfigSubOrgs = com.k_int.kbplus.Subscription.get(result.surveyConfig?.subscription?.id)?.getDerivedSubscribers()

        result.surveyConfigOrgs = Org.findAllByIdInList(SurveyConfig.get(params.surveyConfigID)?.orgIDs)

        result.selectedParticipants = Org.findAllByIdInList(SurveyConfig.get(params.surveyConfigID)?.orgIDs) - result.surveyConfigSubOrgs
        result.selectedSubParticipants = Org.findAllByIdInList(SurveyConfig.get(params.surveyConfigID)?.orgIDs) - result.selectedParticipants

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def allSurveyProperties() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyProperties = SurveyProperty.findAllByOwner(result.institution)

        result.properties = getSurveyProperties(result.institution)

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        result.addSurveyConfigs = params.addSurveyConfigs ?: false

        result

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def surveyConfigsInfo() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyProperties = SurveyProperty.findAllByOwner(result.institution)

        result.properties = getSurveyProperties(result.institution)

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.surveyConfig = SurveyConfig.get(params.surveyConfigID)

        result

    }


    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def addSurveyConfig() {

        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

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
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def addSurveyConfigs() {

        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyInfo = SurveyInfo.get(params.id) ?: null

        if (surveyInfo) {

            if (params.selectedProperty) {

                params.list('selectedProperty').each { propertyID ->

                    if (propertyID) {
                        def property = SurveyProperty.get(Long.parseLong(propertyID))
                        //Config is Sub
                        if(params.surveyConfigID) {
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
                            redirect(url: request.getHeader('referer'))
                        }
                        else {
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
                            redirect action: 'surveyConfigs', id: surveyInfo.id
                        }
                    }

                }

            }

        } else {
            redirect action: 'currentSurveysConsortia'
        }
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def deleteSurveyConfig() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyConfig = SurveyConfig.get(params.id)
        //def surveyInfo = surveyConfig.surveyInfo
        //surveyInfo.removeFromSurveyConfigs(surveyConfig)


        try {

            SurveyConfigProperties.findAllBySurveyConfig(surveyConfig).each {
                it.delete(flush: true)
            }
            surveyConfig.delete(flush: true)
            flash.message = g.message(code: "default.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
        }
        catch (DataIntegrityViolationException e) {
            flash.error = g.message(code: "default.not.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def deleteSurveyPropfromSub() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyConfigProp = SurveyConfigProperties.get(params.id)

        try {
            surveyConfigProp.delete(flush: true)
            flash.message = g.message(code: "default.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
        }
        catch (DataIntegrityViolationException e) {
            flash.error = g.message(code: "default.not.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
        }

        redirect(url: request.getHeader('referer'))

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def addSurveyProperty() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

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

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def addSurveyParticipants() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if (params.selectedOrgs) {


            def surveyConfig = SurveyConfig.get(params.surveyConfigID)
            surveyConfig.orgIDs = surveyConfig.orgIDs ?: new ArrayList()

            params.list('selectedOrgs').each { soId ->
                if (!(soId in surveyConfig.orgIDs)) {
                    surveyConfig.orgIDs?.add(Long.parseLong(soId))
                    flash.message = g.message(code: "surveyParticipants.add.successfully")
                }
            }
            surveyConfig.save(flush: true)

        }

        redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID, tab: 'selectedParticipants']

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def openSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result.editable = (result.surveyInfo.status != RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])) ? false : true

        result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

        result
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def processOpenSurvey() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result.surveyConfigs = result.surveyInfo?.surveyConfigs.sort { it?.configOrder }

        result.surveyConfigs.each { config ->

            if (config?.type == 'Subscription') {

                config.orgIDs?.each { orgID ->

                    config?.surveyProperties?.each { property ->

                        def surveyResult = new SurveyResult(
                                owner: result.institution,
                                participant: Org.get(orgID) ?: null,
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
                config.orgIDs?.each { orgID ->

                    def surveyResult = new SurveyResult(
                            owner: result.institution,
                            participant: Org.get(orgID) ?: null,
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

        result.surveyInfo.status = RefdataValue.loc('Survey Status', [en: 'Ready', de: 'Bereit'])
        result.surveyInfo.save(flush: true)
        flash.message = g.message(code: "openSurvey.successfully")

        redirect action: 'surveyEvaluation', id: params.id

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def deleteSurveyParticipants() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        if (params.selectedOrgs) {


            def surveyConfig = SurveyConfig.get(params.surveyConfigID)
            surveyConfig.orgIDs = surveyConfig.orgIDs ?: new ArrayList()

            params.list('selectedOrgs').each { soId ->
                if (Long.parseLong(soId) in surveyConfig.orgIDs) {
                    surveyConfig.orgIDs?.remove(Long.parseLong(soId))
                    flash.message = g.message(code: "surveyParticipants.delete.successfully")
                }
            }
            surveyConfig.save(flush: true)

        }

        redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID]

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def addSubMembers() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyConfig = SurveyConfig.get(params.surveyConfigID)

        def orgs = com.k_int.kbplus.Subscription.get(surveyConfig.subscription?.id)?.getDerivedSubscribers()

        if (orgs) {

            surveyConfig.orgIDs = surveyConfig.orgIDs ?: new ArrayList()

            orgs.each { org ->
                if (!(org.id in surveyConfig.orgIDs)) {
                    surveyConfig.orgIDs?.add(org.id)
                }
            }
            surveyConfig.save(flush: true)

        }

        redirect action: 'surveyParticipants', id: params.id, params: [surveyConfigID: params.surveyConfigID, tab: 'selectedSubParticipants']

    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def deleteDocuments() {

        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect action: 'surveyConfigDocs', id: SurveyConfig.get(params.instanceId).surveyInfo.id, params: [surveyConfigID: params.instanceId]
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def deleteSurveyInfo() {

        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        def surveyInfo = SurveyInfo.get(params.id)

        surveyInfo.surveyConfigs.each { config ->

            config.documents.each {

                it.delete()

            }
            it.delete()
        }

        redirect action: 'currentSurveysConsortia'
    }


    private getSurveyProperties(Org contextOrg) {
        def properties = []

        //private Property
        SurveyProperty.findAllByOwner(contextOrg).each { prop ->
            properties << prop

        }

        //global Property
        SurveyProperty.findAllByOwnerIsNull().each { prop ->
            properties << prop

        }

        properties.sort { a, b -> a.getI10n('name').compareToIgnoreCase b.getI10n('name') }

        return properties
    }

    @DebugAnnotation(perm = "ORG_CONSORTIUM_SURVEY", affil = "INST_ADM", specRole = "ROLE_ADMIN")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliationX("ORG_CONSORTIUM_SURVEY", "INST_ADM", "ROLE_ADMIN")
    })
    def toggleSurveySub() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect(url: request.getHeader('referer'))
        }

        switch(params.direction) {
            case 'add':
                def surveyInfo = SurveyInfo.get(params.id) ?: null

                if (surveyInfo) {
                    if (params.sub) {
                        def subscription = Subscription.get(Long.parseLong(params.sub))
                        def surveyConfig = subscription ? SurveyConfig.findAllBySubscriptionAndSurveyInfo(subscription, surveyInfo) : null
                        if (!surveyConfig && subscription) {
                            surveyConfig = new SurveyConfig(
                                    subscription: subscription,
                                    configOrder: surveyInfo.surveyConfigs.size() + 1,
                                    type: 'Subscription',
                                    surveyInfo: surveyInfo

                            )

                            surveyConfig.save(flush: true)

                            flash.message = g.message(code: "survey.toggleSurveySub.add.success", args: [subscription.name])
                        } else {
                            flash.error = g.message(code: "survey.toggleSurveySub.add.fail", args: [subscription.name])
                        }
                    }
                }
                break
            case 'remove':
                def surveyInfo = SurveyInfo.get(params.id) ?: null

                if (surveyInfo) {
                    def subscription = Subscription.get(Long.parseLong(params.sub))
                    def surveyConfig = subscription ? SurveyConfig.findBySubscriptionAndSurveyInfo(subscription, surveyInfo) : null
                    if (surveyConfig && subscription) {
                        try {

                            SurveyConfigProperties.findAllBySurveyConfig(surveyConfig).each {
                                it.delete(flush: true)
                            }
                            surveyConfig.delete(flush: true)
                            flash.message = g.message(code: "survey.toggleSurveySub.remove.success", args: [subscription.name])
                        }
                        catch (DataIntegrityViolationException e) {
                            flash.error = g.message(code: "survey.toggleSurveySub.remove.fail", args: [subscription.name])
                        }
                    }
                }
                break
        }
        redirect action: 'allSubscriptions', id: params.id, params: params
    }


}
