package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured

@Secured(['IS_AUTHENTICATED_FULLY'])
class SurveyController {

    def springSecurityService
    def accessService
    def contextService


    def currentSurveys()
    {
        def result = [:]
        result.institution  = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.max = params.max ? Integer.parseInt(params.max) : result.user.getDefaultPageSizeTMP();
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        result.surveys = SurveyInfo.findAll('from SurveyInfo as si where si.owner = :contextOrg ', [contextOrg: result.institution])

        result.countSurvey = result.surveys.size()

        result
    }

    def emptySurvey()
    {
        def result = [:]
        result.institution  = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        params.steps = params.steps ?: 1

        if (result.editable) {



            if(params.valid_from)
            {
                def sdf = new DateUtil().getSimpleDateFormat_NoTime()
                params.valid_from = sdf.parse(params.valid_from)
            }
            if(params.valid_to)
            {
                def sdf = new DateUtil().getSimpleDateFormat_NoTime()
                params.valid_to = sdf.parse(params.valid_to)
            }

            result
        } else {
            redirect action: 'currentSurveys'
        }

    }

    def createSurvey(){

        def result = [:]
        result.institution  = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)

    }


}
