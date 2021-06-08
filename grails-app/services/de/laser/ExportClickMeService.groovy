package de.laser

import com.k_int.kbplus.ExportService
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.PropertyDefinition
import grails.gorm.transactions.Transactional
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ExportClickMeService {

    def messageSource
    ExportService exportService
    AccessPointService accessPointService

    static Map<String, Object> EXPORT_RENEWAL_CONFIG = [
            //Wichtig: Hier bei dieser Config bitte drauf achten, welche Feld Bezeichnung gesetzt ist, 
            // weil die Felder von einer zusammengesetzten Map kommen. siehe SurveyControllerService -> renewalEvaltion
                    survey      : [
                            label: 'Survey',
                            message: 'survey.label',
                            fields: [
                                    'survey.participantComment'   : [field: 'resultOfParticipation.comment', label: 'Participant Comment', message: 'surveyResult.participantComment', defaultChecked: 'true'],
                                    'survey.participationProperty': [field: 'resultOfParticipation.result', label: 'Participation', message: 'surveyResult.participationProperty', defaultChecked: 'true'],
                                    'survey.period'               : [field: null, label: 'Period', message: 'renewalEvaluation.period', defaultChecked: 'true'],
                                    'survey.periodComment'        : [field: null, label: 'Period Comment', message: 'renewalEvaluation.periodComment', defaultChecked: 'true'],
                                    'survey.costBeforeTax'        : [field: 'resultOfParticipation.costItem.costInBillingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.costBeforeTax', defaultChecked: 'true'],
                                    'survey.costAfterTax'         : [field: 'resultOfParticipation.costItem.costInBillingCurrencyAfterTax', label: 'Cost After Tax', message: 'renewalEvaluation.costAfterTax', defaultChecked: 'true'],
                                    'survey.costTax'              : [field: 'resultOfParticipation.costItem.taxKey.taxRate', label: 'Cost Tax', message: 'renewalEvaluation.costTax', defaultChecked: 'true'],
                                    'survey.currency'             : [field: 'resultOfParticipation.costItem.billingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.currency', defaultChecked: 'true'],
                                    'survey.allOtherProperties'   : [field: null, label: 'All other Properties', message: 'renewalEvaluation.allOtherProperties', defaultChecked: 'true'],
                            ]
                    ],

                    participant : [
                            label: 'Participant',
                            message: 'surveyParticipants.label',
                            fields: [
                            'participant.sortname'          : [field: 'participant.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'participant.name'              : [field: 'participant.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'participant.funderType'        : [field: 'participant.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'participant.funderHskType'     : [field: 'participant.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'participant.libraryType'       : [field: 'participant.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                            'participant.exportIPs'         : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                            'participant.exportProxys'      : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                            'participant.exportEZProxys'    : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                            'participant.exportShibboleths' : [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.ISIL'              : [field: null, label: 'ISIL'],
                            'participant.WIBID'              : [field: null, label: 'WIB-ID'],
                            'participant.EZBID'              : [field: null, label: 'EZB-ID'],
                            'participant.customerIdentifier' : [field: null, label: 'customerIdentifier', message: 'org.customerIdentifier.plural'],
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
        RefdataValue generalContact = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        RefdataValue billingContact = RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS

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

            if(!fields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    titles << generalContact.getI10n('value')
                }else if (fieldKey == 'participant.billingContact') {
                    titles << billingContact.getI10n('value')
                }else if (fieldKey == 'participant.ISIL') {
                    titles << 'ISIL'
                }
                else if (fieldKey == 'participant.WIBID') {
                    titles << 'WIB-ID'
                }
                else if (fieldKey == 'participant.EZBID') {
                    titles << 'EZB-ID'
                }
                else if (fieldKey != 'survey.allOtherProperties') {
                    titles << (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label)
                } else {
                    renewalResult.properties.each { surveyProperty ->
                        titles << (surveyProperty?.getI10n('name'))
                        titles << (messageSource.getMessage('surveyResult.participantComment', null, locale) + " " + messageSource.getMessage('renewalEvaluation.exportRenewal.to', null, locale) + " " + surveyProperty?.getI10n('name'))
                    }
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

        sheetData = exportAccessPoints(renewalResult, sheetData, selectedExportFields, locale)

        return exportService.generateXLSXWorkbook(sheetData)
    }

    private void setRenewalRow(Map renewalResult, Map<String, Object> selectedFields, List renewalData, boolean onlySubscription, PropertyDefinition multiYearTermTwoSurvey, PropertyDefinition multiYearTermThreeSurvey){
        List row = []
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'survey.allOtherProperties') {
                    renewalResult.properties?.sort { it.type.name }.each { participantResultProperty ->
                        if (onlySubscription) {
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                        } else {
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

                    if (!multiYearTermTwoSurvey && !multiYearTermThreeSurvey) {
                        row.add([field: '', style: null])
                    }
                }else if (fieldKey == 'participant.generalContact') {
                    setOrgFurtherInformation(renewalResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    setOrgFurtherInformation(renewalResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.ISIL') {
                    setOrgFurtherInformation(renewalResult.participant, row, fieldKey)
                }
                else if (fieldKey == 'participant.WIBID') {
                    setOrgFurtherInformation(renewalResult.participant, row, fieldKey)
                }
                else if (fieldKey == 'participant.EZBID') {
                    setOrgFurtherInformation(renewalResult.participant, row, fieldKey)
                }
                else if (fieldKey == 'participant.customerIdentifier') {
                    setOrgFurtherInformation(renewalResult.participant, row, fieldKey, renewalResult.sub)
                }else {
                    if (onlySubscription) {
                        if (fieldKey.startsWith('subscription.') || fieldKey.startsWith('participant.')) {
                            def fieldValue = getFieldValue(renewalResult, field, sdf)
                            row.add([field: fieldValue ?: '', style: null])
                        } else {
                            row.add([field: '', style: null])
                        }

                    } else {
                        def fieldValue = getFieldValue(renewalResult, field, sdf)
                        row.add([field: fieldValue ?: '', style: null])
                    }
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

    Map exportAccessPoints(Map renewalResult, Map sheetData, LinkedHashMap selectedExportFields, Locale locale) {

        Map export = [:]
        String sheetName = ''

        if ('participant.exportIPs' in selectedExportFields.keySet()) {
            if (renewalResult.orgsContinuetoSubscription) {

                export = accessPointService.exportIPsOfOrgs(renewalResult.orgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithMultiYearTermSub) {

                export = accessPointService.exportIPsOfOrgs(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithParticipationInParentSuccessor) {
                export = accessPointService.exportIPsOfOrgs(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.newOrgsContinuetoSubscription) {

                export = accessPointService.exportIPsOfOrgs(renewalResult.newOrgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportProxys' in selectedExportFields.keySet()) {
            if (renewalResult.orgsContinuetoSubscription) {

                export = accessPointService.exportProxysOfOrgs(renewalResult.orgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithMultiYearTermSub) {

                export = accessPointService.exportProxysOfOrgs(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithParticipationInParentSuccessor) {
                export = accessPointService.exportProxysOfOrgs(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.newOrgsContinuetoSubscription) {

                export = accessPointService.exportProxysOfOrgs(renewalResult.newOrgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportEZProxys' in selectedExportFields.keySet()) {
            if (renewalResult.orgsContinuetoSubscription) {

                export = accessPointService.exportEZProxysOfOrgs(renewalResult.orgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithMultiYearTermSub) {

                export = accessPointService.exportEZProxysOfOrgs(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithParticipationInParentSuccessor) {
                export = accessPointService.exportEZProxysOfOrgs(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.newOrgsContinuetoSubscription) {

                export = accessPointService.exportEZProxysOfOrgs(renewalResult.newOrgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportShibboleths' in selectedExportFields.keySet()) {
            if (renewalResult.orgsContinuetoSubscription) {

                export = accessPointService.exportShibbolethsOfOrgs(renewalResult.orgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithMultiYearTermSub) {

                export = accessPointService.exportShibbolethsOfOrgs(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.orgsWithParticipationInParentSuccessor) {
                export = accessPointService.exportShibbolethsOfOrgs(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})"
                sheetData[sheetName] = export
            }

            if (renewalResult.newOrgsContinuetoSubscription) {

                export = accessPointService.exportShibbolethsOfOrgs(renewalResult.newOrgsContinuetoSubscription.participant, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})"
                sheetData[sheetName] = export
            }

        }

        return sheetData
    }

    void setOrgFurtherInformation(Org org, List row, String fieldKey, Subscription subscription = null){

        if (fieldKey == 'participant.generalContact') {
            RefdataValue generalContact = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
            List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.org = :org and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true",[org:org, functionTypes:[generalContact], type: RDStore.CCT_EMAIL])

            if(contactList){
                row.add([field:  contactList.content.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }

        }else if (fieldKey == 'participant.billingContact') {
            RefdataValue billingContact = RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS
            List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.org = :org and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true",[org:org, functionTypes:[billingContact], type: RDStore.CCT_EMAIL])

            if(contactList){
                row.add([field:  contactList.content.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }

        }else if (fieldKey == 'participant.ISIL') {
            List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.ns in (:namespaces)",[org:org,namespaces:['ISIL']])

            if(identifierList){
                row.add([field:  identifierList.value.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }
        }
        else if (fieldKey == 'participant.WIBID') {
            List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.ns in (:namespaces)",[org:org,namespaces:['wibid']])
            if(identifierList){
                row.add([field:  identifierList.value.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }
        }
        else if (fieldKey == 'participant.EZBID') {
            List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.ns in (:namespaces)",[org:org,namespaces:['ezb']])
            if(identifierList){
                row.add([field:  identifierList.value.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }
        }
        else if (fieldKey == 'participant.customerIdentifier') {
            if(subscription && subscription.packages){
                List<Platform> platformList = Platform.executeQuery('select distinct tipp.platform from TitleInstancePackagePlatform tipp where tipp.pkg = :pkg',[pkg: subscription.packages.pkg])
                List<CustomerIdentifier> customerIdentifierList = CustomerIdentifier.findAllByCustomerAndIsPublicAndPlatformInList(org, false, platformList)
                if(customerIdentifierList){
                    row.add([field:  customerIdentifierList.value.join(";") , style: null])
                }else {
                    row.add([field:  '' , style: null])
                }
            }
            else{
                row.add([field:  '' , style: null])
            }
        }
    }
}
