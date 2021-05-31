package de.laser

import com.k_int.kbplus.ExportService
import de.laser.helper.DateUtils
import grails.gorm.transactions.Transactional
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ExportClickMeService {

    def messageSource
    ExportService exportService

    static Map<String, Object> EXPORT_RENEWAL_CONFIG = [

                    participant : [
                            label: 'Participant',
                            message: 'surveyParticipants.label',
                            fields: [
                            'participant.sortname'     : [field: 'participant.sortname', label: 'Sortname', message: 'org.sortname.label'],
                            'participant.name'         : [field: 'participant.name', label: 'Name', message: 'default.name.label'],
                            'participant.funderType'   : [field: 'participant.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'participant.funderHskType': [field: 'participant.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'participant.libraryType'  : [field: 'participant.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                    ]
                    ],

                    survey      : [
                            label: 'Survey',
                            message: 'survey.label',
                            fields: [
                            'survey.participantComment'   : [field: 'resultOfParticipation.comment', label: 'Participant Comment', message: 'surveyResult.participantComment'],
                            'survey.participationProperty': [field: 'resultOfParticipation.result', label: 'Participation', message: 'surveyResult.participationProperty'],
                            'survey.period'               : [field: '', label: 'Period', message: 'renewalEvaluation.period'],
                            'survey.periodComment'        : [field: '', label: 'Period Comment', message: 'renewalEvaluation.periodComment'],
                            'survey.costBeforeTax'        : [field: 'costItem.costInBillingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.costBeforeTax'],
                            'survey.costAfterTax'         : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Cost After Tax', message: 'renewalEvaluation.costAfterTax'],
                            'survey.costTax'              : [field: 'costItem.taxKey.taxRate', label: 'Cost Tax', message: 'renewalEvaluation.costTax'],
                            'survey.currency'             : [field: 'costItem.billingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.currency'],
                            'survey.allOtherProperties'   : [field: '', label: '', message: ''],
                                    ]
                    ],

                    subscription: [
                            label: 'Subscription',
                            message: 'subscription.label',
                            fields: [
                                    ]
                    ]

    ]

    Map<String, Object> getExportRenewalFields() {

        Map<String, Object> exportFields = [:]

        EXPORT_RENEWAL_CONFIG.keySet().each {
            EXPORT_RENEWAL_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        exportFields
    }

    Map<String, Object> getExportRenewalFieldsForUI() {

        Map<String, Object> fields = EXPORT_RENEWAL_CONFIG as Map

        fields
    }


    def exportRenewalResult(Map renewalResult, Map<String, Object> selectedFields) {
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()

        Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportRenewalFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = []


        selectedExportFields.keySet().each {String fieldKey ->
            Map fields = selectedExportFields.get(fieldKey)

            if(fieldKey != 'survey.allOtherProperties') {
                titles << (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label)
            }else {
                renewalResult.properties.each { surveyProperty ->
                    titles << (surveyProperty?.getI10n('name'))
                    titles << (messageSource.getMessage('surveyResult.participantComment', null, locale) + " " + messageSource.getMessage('renewalEvaluation.exportRenewal.to', null, locale) +" " + surveyProperty?.getI10n('name'))
                }
            }
        }

        List renewalData = []

        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.continuetoSubscription.label', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size() ?: 0})", style: 'positive']])

        renewalResult.orgsContinuetoSubscription.sort { it.participant.sortname }.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.withMultiYearTermSub.label', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size() ?: 0})", style: 'positive']])


        renewalResult.orgsWithMultiYearTermSub.each { sub ->
            List row = []

            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->

                row.add([field: subscriberOrg.sortname ?: '', style: null])
                row.add([field: subscriberOrg.name ?: '', style: null])

                row.add([field: '', style: null])

                row.add([field: '', style: null])

                String period = ""

                period = sub.startDate ? sdf.format(sub.startDate) : ""
                period = sub.endDate ? period + " - " +sdf.format(sub.endDate) : ""

                row.add([field: period?: '', style: null])

                if (renewalResult.multiYearTermTwoSurvey || renewalResult.multiYearTermThreeSurvey)
                {
                    row.add([field: '', style: null])
                }

            }


            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.orgsWithParticipationInParentSuccessor.label', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size() ?: 0})", style: 'positive']])


        renewalResult.orgsWithParticipationInParentSuccessor.each { sub ->
            List row = []

            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->

                row.add([field: subscriberOrg.sortname ?: '', style: null])
                row.add([field: subscriberOrg.name ?: '', style: null])

                row.add([field: '', style: null])

                row.add([field: '', style: null])

                String period = ""

                period = sub.startDate ? sdf.format(sub.startDate) : ""
                period = sub.endDate ? period + " - " +sdf.format(sub.endDate) : ""

                row.add([field: period?: '', style: null])

                if (renewalResult.multiYearTermTwoSurvey || renewalResult.multiYearTermThreeSurvey)
                {
                    row.add([field: '', style: null])
                }
            }


            renewalData.add(row)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.newOrgstoSubscription.label', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size() ?: 0})", style: 'positive']])


        renewalResult.newOrgsContinuetoSubscription.sort{it.participant.sortname}.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.withTermination.label', null, locale) + " (${renewalResult.orgsWithTermination.size() ?: 0})", style: 'negative']])


        renewalResult.orgsWithTermination.sort{it.participant.sortname}.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('surveys.tabs.termination', null, locale) + " (${renewalResult.orgsWithoutResult.size()})", style: 'negative']])


        renewalResult.orgsWithoutResult.sort{it.participant.sortname}.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData)
        }


        Map sheetData = [:]
        sheetData[messageSource.getMessage('renewalexport.renewals', null, locale)] = [titleRow: titles, columnData: renewalData]
        return exportService.generateXLSXWorkbook(sheetData)
    }

    private void setRenewalRow(Map renewalResult, Map<String, Object> selectedFields, List renewalData){
        List row = []
        selectedFields.keySet().each { String fieldKey ->
            Map fields = selectedFields.get(fieldKey)
            if (fieldKey == 'survey.allOtherProperties') {
                renewalResult.properties?.sort { it.type.name }.each { participantResultProperty ->
                    row.add([field: participantResultProperty.getResult() ?: "", style: null])
                    row.add([field: participantResultProperty.comment ?: "", style: null])
                }
            } else if (fieldKey == 'survey.period') {
                String period = ""
                if (renewalResult.multiYearTermTwoSurvey) {
                    period = renewalResult.newSubPeriodTwoStartDate ? sdf.format(renewalResult.newSubPeriodTwoStartDate) : ""
                    period = renewalResult.newSubPeriodTwoEndDate ? period + " - " + sdf.format(renewalResult.newSubPeriodTwoEndDate) : ""
                }

                if (renewalResult.multiYearTermThreeSurvey) {
                    period = renewalResult.newSubPeriodThreeStartDate ? sdf.format(renewalResult.newSubPeriodThreeStartDate) : ""
                    period = renewalResult.newSubPeriodThreeEndDate ? period + " - " + sdf.format(renewalResult.newSubPeriodThreeEndDate) : ""
                }
                row.add([field: period ?: '', style: null])
            } else if (fieldKey == 'survey.periodComment') {
                if (renewalResult.multiYearTermTwoSurvey) {
                    row.add([field: renewalResult.participantPropertyTwoComment ?: '', style: null])
                }

                if (renewalResult.multiYearTermThreeSurvey) {
                    row.add([field: renewalResult.participantPropertyThreeComment ?: '', style: null])
                }

                if(!renewalResult.multiYearTermTwoSurvey && !renewalResult.multiYearTermThreeSurvey){
                    row.add([field: '', style: null])
                }
            } else {

                def field

               fields.field.split('\\.').eachWithIndex { Object entry, int i ->

                    if(i == 0) {
                        field = renewalResult[entry]
                    }else {
                        field = field ? field[entry]: null
                    }
                }

                if(field instanceof RefdataValue){
                    field = field.getI10n('value')
                }

                row.add([field: field ?: '', style: null])
            }
        }
        renewalData.add(row)

    }

    private List setRenewalRowWithoutRenewalResult(Map renewalResult, Map<String, Object> selectedFields, List renewalData ){
        List row = []
        selectedFields.keySet().each { String fieldKey ->
            Map fields = selectedFields.get(fieldKey)
            if (fieldKey == 'survey.allOtherProperties') {
                renewalResult.properties?.sort { it.type.name }.each { participantResultProperty ->
                    row.add([field: "", style: null])
                    row.add([field: "", style: null])

                }
            } else if (fieldKey == 'survey.period') {
                String period = ""
                if (renewalResult.multiYearTermTwoSurvey) {
                    period = renewalResult.newSubPeriodTwoStartDate ? sdf.format(renewalResult.newSubPeriodTwoStartDate) : ""
                    period = renewalResult.newSubPeriodTwoEndDate ? period + " - " + sdf.format(renewalResult.newSubPeriodTwoEndDate) : ""
                }

                if (renewalResult.multiYearTermThreeSurvey) {
                    period = renewalResult.newSubPeriodThreeStartDate ? sdf.format(renewalResult.newSubPeriodThreeStartDate) : ""
                    period = renewalResult.newSubPeriodThreeEndDate ? period + " - " + sdf.format(renewalResult.newSubPeriodThreeEndDate) : ""
                }

                row.add([field: period ?: '', style: null])
            } else if (fieldKey == 'survey.periodComment') {
                if (renewalResult.multiYearTermTwoSurvey) {
                    row.add([field: renewalResult.participantPropertyTwoComment ?: '', style: null])
                }

                if (renewalResult.multiYearTermThreeSurvey) {
                    row.add([field: renewalResult.participantPropertyThreeComment ?: '', style: null])
                }
            } else {
                row.add([field: renewalResult."${fields.field}" ?: '', style: null])
            }
        }
        renewalData.add(row)
    }
}
