package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

@Secured(['IS_AUTHENTICATED_FULLY'])
class SurveyController {

    def springSecurityService
    def accessService
    def contextService
    def subscriptionsQueryService

    @Secured(['ROLE_YODA'])
    def currentSurveys() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        result.surveys = SurveyInfo.findAll('from SurveyInfo as si where si.owner = :contextOrg ', [contextOrg: result.institution])

        result.countSurvey = result.surveys.size()

        result
    }
    @Secured(['ROLE_YODA'])
    def showSurveyInfo() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect action: 'currentSurveys'
        }

        result.surveyInfo = SurveyInfo.get(params.id) ?: null
        result


    }
    @Secured(['ROLE_YODA'])
    def locSurveyInfo() {

        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect action: 'currentSurveys'
        }


        def surveyInfo = SurveyInfo.get(params.id) ?: null

        def sdf = new DateUtil().getSimpleDateFormat_NoTime()

        if (surveyInfo) {
            surveyInfo.name = params.name
            surveyInfo.startDate = params.startDate ? sdf.parse(params.startDate) : null
            surveyInfo.endDate = params.endDate ? sdf.parse(params.endDate) : null
            //surveyInfo.type = RefdataValue.get(params.type)

            if (surveyInfo.isDirty()) {
                if(surveyInfo.save(flush: true)) {
                    flash.message = g.message(code: "showSurveyInfo.save.successfully")
                }else {
                    flash.error = g.message(code: "showSurveyInfo.save.fail")
                }
            }

        } else {

            surveyInfo = new SurveyInfo(
                    name: params.name,
                    startDate: params.startDate ? sdf.parse(params.startDate) : null,
                    endDate: params.endDate ? sdf.parse(params.endDate) : null,
                    type: params.type,
                    owner: contextService.getOrg(),
                    status: RefdataValue.loc('Survey Status', [en: 'In Processing', de: 'In Bearbeitung'])
            ).save(flush: true)

        }

        redirect action: 'showSurveyInfo', id: surveyInfo.id


    }
    @Secured(['ROLE_YODA'])
    def showSurveyConfig() {
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect action: 'currentSurveys'
        }


        result.surveyProperties = SurveyProperty.findAllByOwner(result.institution)
        result.subscriptions = SurveyProperty.findAllByOwner(result.institution)

        params.status = RDStore.SUBSCRIPTION_CURRENT.id

        def tmpQ = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery(params, contextService.org)
        List subscriptions = Subscription.executeQuery("select s ${tmpQ[0]}", tmpQ[1])
        result.subscriptions = subscriptions

        result.properties = getSurveyProperties(result.institution)

        result.surveyInfo = SurveyInfo.get(params.id) ?: null

        result.surveyConfigs = result.surveyInfo.surveyConfigs.sort{it?.configOrder}

        result

    }
    @Secured(['ROLE_YODA'])
    def addSurveyConfig() {

        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect action: 'currentSurveys'
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
                            type: 'Subscription'

                    )
                    surveyInfo.addToSurveyConfigs(surveyConfig)
                    surveyInfo.save(flush: true)

                    def configProperty = new SurveyConfigProperties(
                            surveyProperty: SurveyProperty.findByName('Continue to license'),
                            surveyConfig: surveyConfig).save(flush: true)

                    flash.message = g.message(code: "showSurveyConfig.add.successfully")

                } else {
                    flash.error = g.message(code: "showSurveyConfig.exists")
                }
            }
                if (params.property) {
                    def property = SurveyProperty.get(Long.parseLong(params.property))
                    def surveyConfigProp = property ? SurveyConfig.findAllBySurveyPropertyAndSurveyInfo(property, surveyInfo) : null
                    if (!surveyConfigProp && property) {
                        surveyConfigProp = new SurveyConfig(
                                surveyProperty: property,
                                configOrder: surveyInfo.surveyConfigs.size()+1,
                                type: 'SurveyProperty'

                        )
                        surveyInfo.addToSurveyConfigs(surveyConfigProp)
                        surveyInfo.save(flush: true)

                        flash.message = g.message(code: "showSurveyConfig.add.successfully")

                    }else {
                        flash.error = g.message(code: "showSurveyConfig.exists")
                    }
                }

            redirect action: 'showSurveyConfig', id: surveyInfo.id

        } else {
            redirect action: 'currentSurveys'
        }
    }

    def deleteSurveyConfig(){
        def result = [:]
        result.institution = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        if (!result.editable) {
            flash.error = g.message(code: "default.notAutorized.message")
            redirect action: 'currentSurveys'
        }

        def surveyConfig = SurveyConfig.get(params.id)
        //def surveyInfo = surveyConfig.surveyInfo
        //surveyInfo.removeFromSurveyConfigs(surveyConfig)


        try {
            surveyConfig.delete(flush: true)
            flash.message = g.message(code: "default.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
        }
        catch (DataIntegrityViolationException e) {
            flash.error = g.message(code: "default.not.deleted.message", args: [g.message(code: "surveyConfig.label"), ''])
        }

         redirect(url: request.getHeader('referer'))

    }

    private getSurveyProperties(Org contextOrg)
    {
        def properties = []

        //private Property
        SurveyProperty.findAllByOwner(contextOrg).each { prop ->
            properties << prop

        }

        //global Property
        SurveyProperty.findAllByOwnerIsNull().each { prop ->
            properties << prop

        }

        return properties
    }


}
