package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.SurveyConfig
import com.k_int.kbplus.SurveyInfo
import com.k_int.kbplus.SurveyResult
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class SurveyService {

    boolean isEditableSurvey(Org org, SurveyInfo surveyInfo) {

        if(surveyInfo.status != RDStore.SURVEY_SURVEY_STARTED)
        {
            return false
        }

        def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo?.surveyConfigs)

        if(surveyResults) {
           return surveyResults?.finishDate?.contains(null) ? true : false
        }else
        {
            return false
        }


    }

    def getParticipantConfigNavigation(Org org, SurveyInfo surveyInfo, SurveyConfig surveyConfig){
        def result = [:]
        def surveyResults = SurveyResult.findAllByParticipantAndSurveyConfigInList(org, surveyInfo?.surveyConfigs).sort {it.surveyConfig.configOrder}

        int currentOrder = surveyConfig.configOrder
        List<Integer> configOrders = SurveyConfig.findAllByIdInList(surveyResults.findAll {it.surveyConfig.type == 'Subscription'}.groupBy {it.surveyConfig.id}.keySet()).configOrder
        int currentOrderIndex = configOrders.indexOf(currentOrder)

        if(currentOrderIndex > 0) {
            result.prev = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :prev and sc.surveyInfo = :surInfo',[prev:configOrders.get(currentOrderIndex-1), surInfo: surveyInfo])[0]
        }
        if(currentOrderIndex < configOrders.size()-1) {
            result.next = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :next and sc.surveyInfo = :surInfo',[next:configOrders.get(currentOrderIndex+1), surInfo: surveyInfo])[0]
        }

        result.total = configOrders.size()

        return result

    }

    def getConfigNavigation(SurveyInfo surveyInfo, SurveyConfig surveyConfig){
        def result = [:]
        int currentOrder = surveyConfig?.configOrder
        List<Integer> configOrders = surveyInfo?.surveyConfigs?.sort{it.configOrder}.configOrder
        int currentOrderIndex = configOrders.indexOf(currentOrder)

        if(currentOrderIndex > 0) {
            result.prev = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :prev and sc.surveyInfo = :surInfo',[prev:configOrders.get(currentOrderIndex-1), surInfo: surveyInfo])[0]
        }
        if(currentOrderIndex < configOrders.size()-1) {
            result.next = SurveyConfig.executeQuery('select sc from SurveyConfig sc where sc.configOrder = :next and sc.surveyInfo = :surInfo',[next:configOrders.get(currentOrderIndex+1), surInfo: surveyInfo])[0]
        }

        result.total = configOrders.size()

        return result

    }
}
