package de.laser

import com.k_int.kbplus.ExportService
import de.laser.helper.DateUtils
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ExportClickMeService {

    def messageSource
    ExportService exportService

    static Map<String, Object> EXPORT_RENEWAL_CONFIG = [
            //Wichtig: Hier bei dieser Config bitte drauf achten, welche Feld Bezeichnung gesetzt ist, 
            // weil die Felder von einer zusammengesetzten Map kommen. siehe SurveyControllerService -> renewalEvaltion
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
                            'survey.period'               : [field: null, label: 'Period', message: 'renewalEvaluation.period'],
                            'survey.periodComment'        : [field: null, label: 'Period Comment', message: 'renewalEvaluation.periodComment'],
                            'survey.costBeforeTax'        : [field: 'resultOfParticipation.costItem.costInBillingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.costBeforeTax'],
                            'survey.costAfterTax'         : [field: 'resultOfParticipation.costItem.costInBillingCurrencyAfterTax', label: 'Cost After Tax', message: 'renewalEvaluation.costAfterTax'],
                            'survey.costTax'              : [field: 'resultOfParticipation.costItem.taxKey.taxRate', label: 'Cost Tax', message: 'renewalEvaluation.costTax'],
                            'survey.currency'             : [field: 'resultOfParticipation.costItem.billingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.currency'],
                            'survey.allOtherProperties'   : [field: null, label: 'All other Properties', message: 'renewalEvaluation.allOtherProperties'],
                                    ]
                    ],

                    subscription: [
                            label: 'Subscription',
                            message: 'subscription.label',
                            fields: [
                                    'subscription.name'                         : [field: 'sub.name', label: 'Name', message: 'subscription.name.label'],
                                    'subscription.startDate'                    : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label'],
                                    'subscription.endDate'                      : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label'],
                                    'subscription.manualCancellationDate'       : [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                    'subscription.isMultiYear'                  : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label'],
                                    'subscription.status'                       : [field: 'sub.status', label: 'Status', message: 'subscription.status.label'],
                                    'subscription.kind'                         : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label'],
                                    'subscription.form'                         : [field: 'sub.form', label: 'Form', message: 'subscription.form.label'],
                                    'subscription.resource'                     : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                                    'subscription.hasPerpetualAccess'           : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                                    'subscription.hasPublishComponent'          : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
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

        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.continuetoSubscription.label', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})", style: 'positive']])

        renewalResult.orgsContinuetoSubscription.sort { it.participant.sortname }.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.withMultiYearTermSub.label', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})", style: 'positive']])


        renewalResult.orgsWithMultiYearTermSub.each { sub ->

            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->

               /* row.add([field: subscriberOrg.sortname ?: '', style: null])
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
                }*/
                setRenewalRow([participant: subscriberOrg, sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)

            }
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.orgsWithParticipationInParentSuccessor.label', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})", style: 'positive']])


        renewalResult.orgsWithParticipationInParentSuccessor.each { sub ->
            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->
/*
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
                }*/

                setRenewalRow([participant: subscriberOrg, sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)
            }
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.newOrgstoSubscription.label', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})", style: 'positive']])


        renewalResult.newOrgsContinuetoSubscription.sort{it.participant.sortname}.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.withTermination.label', null, locale) + " (${renewalResult.orgsWithTermination.size()})", style: 'negative']])


        renewalResult.orgsWithTermination.sort{it.participant.sortname}.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('surveys.tabs.termination', null, locale) + " (${renewalResult.orgsWithoutResult.size()})", style: 'negative']])


        renewalResult.orgsWithoutResult.sort{it.participant.sortname}.each { participantResult ->
            setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)
        }


        Map sheetData = [:]
        sheetData[messageSource.getMessage('renewalexport.renewals', null, locale)] = [titleRow: titles, columnData: renewalData]
        return exportService.generateXLSXWorkbook(sheetData)
    }

    private void setRenewalRow(Map renewalResult, Map<String, Object> selectedFields, List renewalData, boolean onlySubscription, PropertyDefinition multiYearTermTwoSurvey, PropertyDefinition multiYearTermThreeSurvey){
        List row = []
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        selectedFields.keySet().each { String fieldKey ->
            String field = selectedFields.get(fieldKey).field
            if (fieldKey == 'survey.allOtherProperties') {
                renewalResult.properties?.sort { it.type.name }.each { participantResultProperty ->
                    if(onlySubscription){
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                    }else{
                        row.add([field: participantResultProperty.getResult() ?: "", style: null])
                        row.add([field: participantResultProperty.comment ?: "", style: null])
                    }

                }
            } else if (fieldKey == 'survey.period') {
                String period = ""
                if (multiYearTermTwoSurvey) {
                    period = renewalResult.newSubPeriodTwoStartDate ? sdf.format(renewalResult.newSubPeriodTwoStartDate) : ""
                    period = renewalResult.newSubPeriodTwoEndDate ? period + " - " + sdf.format(renewalResult.newSubPeriodTwoEndDate) : ""
                }

                if (multiYearTermThreeSurvey) {
                    period = renewalResult.newSubPeriodThreeStartDate ? sdf.format(renewalResult.newSubPeriodThreeStartDate) : ""
                    period = renewalResult.newSubPeriodThreeEndDate ? period + " - " + sdf.format(renewalResult.newSubPeriodThreeEndDate) : ""
                }
                row.add([field: period ?: '', style: null])
            } else if (fieldKey == 'survey.periodComment') {
                if (multiYearTermTwoSurvey) {
                    row.add([field: renewalResult.participantPropertyTwoComment ?: '', style: null])
                }

                if (multiYearTermThreeSurvey) {
                    row.add([field: renewalResult.participantPropertyThreeComment ?: '', style: null])
                }

                if(!multiYearTermTwoSurvey && !multiYearTermThreeSurvey){
                    row.add([field: '', style: null])
                }
            } else {
                if(onlySubscription){
                    if(fieldKey.startsWith('subscription.') || fieldKey.startsWith('participant.')){
                        def fieldValue = getFieldValue(renewalResult, field, sdf)
                        row.add([field: fieldValue ?: '', style: null])
                    }else {
                        row.add([field: '', style: null])
                    }

                }
                else {
                    def fieldValue = getFieldValue(renewalResult, field, sdf)
                    row.add([field: fieldValue ?: '', style: null])
                }
            }
        }
        renewalData.add(row)

    }

    def getFieldValue(Map map, String field, SimpleDateFormat sdf){
        def fieldValue
        field.split('\\.').eachWithIndex { Object entry, int i ->

            if(i == 0) {
                fieldValue = map[entry]
            }else {
                fieldValue = fieldValue ? fieldValue[entry] : null
            }
        }

        if(fieldValue instanceof RefdataValue){
            fieldValue = fieldValue.getI10n('value')
        }

        if(fieldValue instanceof Boolean){
            fieldValue = (fieldValue == true ? RDStore.YN_YES.getI10n('value') : (fieldValue == false ? RDStore.YN_NO.getI10n('value') : ''))
        }

        if(fieldValue instanceof Date){
            fieldValue = sdf.format(fieldValue)
        }

        return fieldValue
    }
}
