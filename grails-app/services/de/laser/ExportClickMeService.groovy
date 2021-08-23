package de.laser

import com.k_int.kbplus.ExportService
import de.laser.helper.DateUtils
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import grails.gorm.transactions.Transactional
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class ExportClickMeService {

    def messageSource
    ExportService exportService
    AccessPointService accessPointService
    ContextService contextService

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
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            'participant.eInvoice'          : [field: 'participant.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'participant.eInvoicePortal'    : [field: 'participant.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'participant.linkResolverBaseURL'    : [field: 'participant.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                    ]
                    ],
                    participantAccessPoints : [
                            label: 'Participants Access Points',
                            message: 'exportClickMe.participantAccessPoints',
                            fields: [
                                    'participant.exportIPs'         : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                                    'participant.exportProxys'      : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                                    'participant.exportEZProxys'    : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                                    'participant.exportShibboleths' : [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                            ]
                    ],
                    participantIdentifiersCustomerIdentifier : [
                            label: 'Identifiers/Customer Identifier',
                            message: 'exportClickMe.participantIdentifiersCustomerIdentifier',
                            fields: [
                                    'participant.customerIdentifier' : [field: null, label: 'customerIdentifier', message: 'org.customerIdentifier.plural'],
                            ],

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

    static Map<String, Object> EXPORT_SUBSCRIPTION_CONFIG = [
            subscription: [
                    label: 'Subscription',
                    message: 'subscription.label',
                    fields: [
                            'subscription.name'                         : [field: 'sub.name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                            'subscription.startDate'                    : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label', defaultChecked: 'true'],
                            'subscription.endDate'                      : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label', defaultChecked: 'true'],
                            'subscription.manualCancellationDate'       : [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                            'subscription.isMultiYear'                  : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label', defaultChecked: 'true'],
                            'subscription.status'                       : [field: 'sub.status', label: 'Status', message: 'subscription.status.label', defaultChecked: 'true'],
                            'subscription.kind'                         : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label', defaultChecked: 'true'],
                            'subscription.form'                         : [field: 'sub.form', label: 'Form', message: 'subscription.form.label', defaultChecked: 'true'],
                            'subscription.resource'                     : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                    ]
            ],
            participant : [
                    label: 'Participant',
                    message: 'surveyParticipants.label',
                    fields: [
                            'participant.sortname'          : [field: 'orgs.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'participant.name'              : [field: 'orgs.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'participant.funderType'        : [field: 'orgs.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'participant.funderHskType'     : [field: 'orgs.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'participant.libraryType'       : [field: 'orgs.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            'participant.eInvoice'          : [field: 'orgs.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'participant.eInvoicePortal'    : [field: 'orgs.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'participant.linkResolverBaseURL'    : [field: 'orgs.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                    ]
            ],
            participantAccessPoints : [
                    label: 'Participants Access Points',
                    message: 'exportClickMe.participantAccessPoints',
                    fields: [
                            'participant.exportIPs'         : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                            'participant.exportProxys'      : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                            'participant.exportEZProxys'    : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                            'participant.exportShibboleths' : [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                    ]
            ],
            participantIdentifiersCustomerIdentifier : [
                    label: 'Identifiers/Customer Identifier',
                    message: 'exportClickMe.participantIdentifiersCustomerIdentifier',
                    fields: [
                            'participant.customerIdentifier' : [field: null, label: 'customerIdentifier', message: 'org.customerIdentifier.plural'],
                    ]
            ],

            participantSubProperties : [
                    label: 'Properties',
                    message: 'exportClickMe.participantSubProperties',
                    fields: [:]
            ],

    ]

    static Map<String, Object> EXPORT_ORG_CONFIG = [
            participant : [
                    label: 'Participant',
                    message: 'surveyParticipants.label',
                    fields: [
                            'participant.sortname'          : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'participant.name'              : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'participant.funderType'        : [field: 'funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'participant.funderHskType'     : [field: 'funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'participant.libraryType'       : [field: 'libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            'participant.eInvoice'          : [field: 'eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'participant.eInvoicePortal'    : [field: 'eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'participant.linkResolverBaseURL'    : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                    ]
            ],
            participantAccessPoints : [
                    label: 'Participants Access Points',
                    message: 'exportClickMe.participantAccessPoints',
                    fields: [
                            'participant.exportIPs'         : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                            'participant.exportProxys'      : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                            'participant.exportEZProxys'    : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                            'participant.exportShibboleths' : [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                    ]
            ],
            participantIdentifiersCustomerIdentifier : [
                    label: 'Identifiers/Customer Identifier',
                    message: 'exportClickMe.participantIdentifiersCustomerIdentifier',
                    fields: [:]
            ],
            participantProperties : [
                    label: 'Properties',
                    message: 'propertyDefinition.plural',
                    fields: [:]
            ],

    ]

    Map<String, Object> getExportRenewalFields() {

        Map<String, Object> exportFields = [:]

        EXPORT_RENEWAL_CONFIG.keySet().each {
            EXPORT_RENEWAL_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns').each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it.getI10n('name') ?: it.ns])
        }

        exportFields
    }

    Map<String, Object> getExportRenewalFieldsForUI() {

        Map<String, Object> fields = EXPORT_RENEWAL_CONFIG as Map

        IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns').each {
            fields.participantIdentifiersCustomerIdentifier.fields << ["participantIdentifiers.${it.id}":[field: null, label: it.getI10n('name') ?: it.ns]]
        }

        fields
    }

    Map<String, Object> getExportSubscriptionFields(Subscription subscription, Org institution) {

        Map<String, Object> exportFields = [:]

        EXPORT_SUBSCRIPTION_CONFIG.keySet().each {
            EXPORT_SUBSCRIPTION_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns').each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it.getI10n('name') ?: it.ns])
        }
        List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
        if(childSubs) {
            String localizedName
            switch (LocaleContextHolder.getLocale()) {
                case Locale.GERMANY:
                case Locale.GERMAN: localizedName = "name_de"
                    break
                default: localizedName = "name_en"
                    break
            }
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: institution])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                exportFields.put("participantSubProperty."+propertyDefinition.id, [field: null, label: propertyDefinition.getI10n('name')])
            }
        }

        exportFields
    }

    Map<String, Object> getExportSubscriptionFieldsForUI(Subscription subscription, Org institution) {

        Map<String, Object> fields = EXPORT_SUBSCRIPTION_CONFIG as Map

        IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns').each {
            fields.participantIdentifiersCustomerIdentifier.fields << ["participantIdentifiers.${it.id}":[field: null, label: it.getI10n('name') ?: it.ns]]
        }

        fields.participantSubProperties.fields.clear()

        List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
        if(childSubs) {
            String localizedName
            switch (LocaleContextHolder.getLocale()) {
                case Locale.GERMANY:
                case Locale.GERMAN: localizedName = "name_de"
                    break
                default: localizedName = "name_en"
                    break
            }
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: institution])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                fields.participantSubProperties.fields << ["participantSubProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition.getI10n('name'), privateProperty: (propertyDefinition.tenant != null)]]
            }
        }

        fields
    }

    Map<String, Object> getExportOrgFields() {

        Map<String, Object> exportFields = [:]

        EXPORT_ORG_CONFIG.keySet().each {
            EXPORT_ORG_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns').each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it.getI10n('name') ?: it.ns])
        }

        PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).each { PropertyDefinition propertyDefinition ->
            exportFields.put("participantProperty."+propertyDefinition.id, [field: null, label: propertyDefinition.getI10n('name')])

        }

        exportFields
    }

    Map<String, Object> getExportOrgFieldsForUI() {

        Map<String, Object> fields = EXPORT_ORG_CONFIG as Map

        IdentifierNamespace.where{(nsType == Org.class.name)}
                .list(sort: 'ns').each {
            fields.participantIdentifiersCustomerIdentifier.fields << ["participantIdentifiers.${it.id}":[field: null, label: it.getI10n('name') ?: it.ns]]
        }

        fields.participantProperties.fields.clear()

        PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).each { PropertyDefinition propertyDefinition ->
            fields.participantProperties.fields << ["participantProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition.getI10n('name'), privateProperty: (propertyDefinition.tenant != null)]]
        }

        fields
    }


    def exportRenewalResult(Map renewalResult, Map<String, Object> selectedFields) {
        Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportRenewalFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = exportTitles(selectedExportFields, renewalResult.properties, locale)

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
                setRenewalRow([participant: subscriberOrg, sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermTwoSurvey)

            }
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.orgsWithParticipationInParentSuccessor.label', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})", style: 'positive']])


        renewalResult.orgsWithParticipationInParentSuccessor.each { sub ->
            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->
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

        if (renewalResult.orgsContinuetoSubscription) {
            sheetData = exportAccessPoints(renewalResult.orgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale)
        }

        if (renewalResult.orgsWithMultiYearTermSub) {
            sheetData = exportAccessPoints(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, sheetData, selectedExportFields, locale)
        }

        if (renewalResult.orgsWithParticipationInParentSuccessor) {
            sheetData = exportAccessPoints(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, sheetData, selectedExportFields, locale)
        }

        if (renewalResult.newOrgsContinuetoSubscription) {
            sheetData = exportAccessPoints(renewalResult.newOrgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale)
        }


        return exportService.generateXLSXWorkbook(sheetData)
    }

    def exportSubscriptions(List result, Map<String, Object> selectedFields, Subscription subscription, Org institution) {
       Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionFields(subscription, institution)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = exportTitles(selectedExportFields, null, locale)

        List exportData = []
        List orgList = []
        result.each { memberResult ->
            setSubRow(memberResult, selectedExportFields, exportData)
            orgList << memberResult.orgs
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('subscriptionDetails.members.members', null, locale)] = [titleRow: titles, columnData: exportData]

        sheetData =  exportAccessPoints(orgList, sheetData, selectedExportFields, locale)

        return exportService.generateXLSXWorkbook(sheetData)
    }

    def exportOrgs(List<Org> result, Map<String, Object> selectedFields) {
        Locale locale = LocaleContextHolder.getLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportOrgFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = exportTitles(selectedExportFields, null, locale)

        List exportData = []
        result.each { Org org ->
            setOrgRow(org, selectedExportFields, exportData)
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('subscription.details.consortiaMembers.label', null, locale)] = [titleRow: titles, columnData: exportData]

        sheetData =  exportAccessPoints(result, sheetData, selectedExportFields, locale)

        return exportService.generateXLSXWorkbook(sheetData)
    }

    private void setRenewalRow(Map participantResult, Map<String, Object> selectedFields, List renewalData, boolean onlySubscription, PropertyDefinition multiYearTermTwoSurvey, PropertyDefinition multiYearTermThreeSurvey){
        List row = []
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'survey.allOtherProperties') {
                        if (onlySubscription) {
                            participantResult.properties?.sort { it.name }.each { PropertyDefinition propertyDefinition ->
                                row.add([field: '', style: null])
                                row.add([field: '', style: null])
                            }
                        } else {
                            participantResult.properties?.sort { it.type.name }.each { SurveyResult participantResultProperty ->
                                row.add([field: participantResultProperty.getResult() ?: "", style: null])
                                row.add([field: participantResultProperty.comment ?: "", style: null])
                            }
                        }
                } else if (fieldKey == 'survey.period') {
                    String period = ""
                    if (multiYearTermTwoSurvey) {
                        period = participantResult.newSubPeriodTwoStartDate ? sdf.format(participantResult.newSubPeriodTwoStartDate) : ""
                        period = participantResult.newSubPeriodTwoEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodTwoEndDate) : ""
                    }

                    if (multiYearTermThreeSurvey) {
                        period = participantResult.newSubPeriodThreeStartDate ? sdf.format(participantResult.newSubPeriodThreeStartDate) : ""
                        period = participantResult.newSubPeriodThreeEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodThreeEndDate) : ""
                    }
                    row.add([field: period ?: '', style: null])
                } else if (fieldKey == 'survey.periodComment') {
                    if (multiYearTermTwoSurvey) {
                        row.add([field: participantResult.participantPropertyTwoComment ?: '', style: null])
                    }

                    if (multiYearTermThreeSurvey) {
                        row.add([field: participantResult.participantPropertyThreeComment ?: '', style: null])
                    }

                    if (!multiYearTermTwoSurvey && !multiYearTermThreeSurvey) {
                        row.add([field: '', style: null])
                    }
                }else if (fieldKey == 'participant.generalContact') {
                    setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingAdress') {
                    setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.postAdress') {
                    setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }
                else if (fieldKey == 'participant.customerIdentifier') {
                    setOrgFurtherInformation(participantResult.participant, row, fieldKey, participantResult.sub)
                }else if (fieldKey.startsWith('participantIdentifiers.')) {
                    setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else {
                    if (onlySubscription) {
                        if (fieldKey.startsWith('subscription.') || fieldKey.startsWith('participant.')) {
                            def fieldValue = getFieldValue(participantResult, field, sdf)
                            row.add([field: fieldValue ?: '', style: null])
                        } else {
                            row.add([field: '', style: null])
                        }

                    } else {
                        def fieldValue = getFieldValue(participantResult, field, sdf)
                        row.add([field: fieldValue ?: '', style: null])
                    }
                }
            }
        }
        renewalData.add(row)

    }

    private void setSubRow(Map result, Map<String, Object> selectedFields, List exportData){
        List row = []
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    setOrgFurtherInformation(result.orgs, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    setOrgFurtherInformation(result.orgs, row, fieldKey)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    setOrgFurtherInformation(result.orgs, row, fieldKey)
                }
                else if (fieldKey == 'participant.postAdress') {
                    setOrgFurtherInformation(result.orgs, row, fieldKey)
                }
                else if (fieldKey == 'participant.customerIdentifier') {
                    setOrgFurtherInformation(result.orgs, row, fieldKey, result.sub)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    setOrgFurtherInformation(result.orgs, row, fieldKey)
                }else if (fieldKey.startsWith('participantSubProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    List<SubscriptionProperty> subscriptionProperties = SubscriptionProperty.executeQuery("select prop from SubscriptionProperty prop where (prop.owner = :sub and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :sub and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg)",[sub:result.sub, propertyDefs:[id], contextOrg: contextService.getOrg()])
                    if(subscriptionProperties){
                        row.add([field:  subscriptionProperties.collect { it.getValueInI10n()}.join(";") , style: null])
                    }else{
                        row.add([field:  '' , style: null])
                    }
                }
                else {
                        def fieldValue = getFieldValue(result, field, sdf)
                        row.add([field: fieldValue ?: '', style: null])
                    }
                }
            }
        exportData.add(row)

    }

    private void setOrgRow(Org result, Map<String, Object> selectedFields, List exportData){
        List row = []
        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    setOrgFurtherInformation(result, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey == 'participant.postAdress') {
                    setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    setOrgFurtherInformation(result, row, fieldKey)
                }else if (fieldKey.startsWith('participantProperty.')) {
                    setOrgFurtherInformation(result, row, fieldKey)
                }
                else {
                    def fieldValue = field ? result[field] : null

                    if(fieldValue instanceof RefdataValue){
                        fieldValue = fieldValue.getI10n('value')
                    }

                    if(fieldValue instanceof Boolean){
                        fieldValue = (fieldValue == true ? RDStore.YN_YES.getI10n('value') : (fieldValue == false ? RDStore.YN_NO.getI10n('value') : ''))
                    }

                    if(fieldValue instanceof Date){
                        fieldValue = sdf.format(fieldValue)
                    }
                    row.add([field: fieldValue ?: '', style: null])
                }
            }
        }
        exportData.add(row)

    }

    private def getFieldValue(Map map, String field, SimpleDateFormat sdf){
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

    private Map exportAccessPoints(List<Org> orgList, Map sheetData, LinkedHashMap selectedExportFields, Locale locale) {

        Map export = [:]
        String sheetName = ''

        if ('participant.exportIPs' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportIPsOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${orgList.size()})"
                sheetData[sheetName] = export
            }
        }

        if ('participant.exportProxys' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportProxysOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${orgList.size()})"
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportEZProxys' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportEZProxysOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${orgList.size()})"
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportShibboleths' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportShibbolethsOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${orgList.size()})"
                sheetData[sheetName] = export
            }

        }

        return sheetData
    }

    private void setOrgFurtherInformation(Org org, List row, String fieldKey, Subscription subscription = null){

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

        }else if (fieldKey == 'participant.billingAdress') {
            RefdataValue billingAdress = RDStore.ADRESS_TYPE_BILLING
            LinkedHashSet<Address> adressList = org.addresses.findAll {Address adress -> adress.type.findAll {it == billingAdress}}

            if(adressList){
                row.add([field:  adressList.collect {Address address -> address.zipcode + ' ' + address.city + ', ' + address.street_1 + ' ' + address.street_2}.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }

        }else if (fieldKey == 'participant.postAdress') {
            RefdataValue postAdress = RDStore.ADRESS_TYPE_POSTAL
            LinkedHashSet<Address> adressList = org.addresses.findAll {Address adress -> adress.type.findAll {it == postAdress}}

            if(adressList){
                row.add([field:  adressList.collect {it.zipcode + ' ' + it.city + ', ' + it.street_1 + ' ' + it.street_2}.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }

        }
        else if (fieldKey.startsWith('participantIdentifiers.')) {
            Long id = Long.parseLong(fieldKey.split("\\.")[1])
            List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.id in (:namespaces)",[org:org,namespaces:[id]])
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
        else if (fieldKey.startsWith('participantProperty.')) {
            Long id = Long.parseLong(fieldKey.split("\\.")[1])
            List<OrgProperty> orgProperties = OrgProperty.executeQuery("select prop from OrgProperty prop where (prop.owner = :org and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :org and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg)",[org:org, propertyDefs:[id], contextOrg: contextService.getOrg()])
            if(orgProperties){
                row.add([field:  orgProperties.collect { it.getValueInI10n()}.join(";") , style: null])
            }else{
                row.add([field:  '' , style: null])
            }
        }
    }

    private List exportTitles(Map<String, Object> selectedExportFields, List<PropertyDefinition> propertyDefinitionList, Locale locale){
        List titles = []
        RefdataValue generalContact = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
        RefdataValue billingContact = RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS

        RefdataValue billingAdress =RDStore.ADRESS_TYPE_BILLING
        RefdataValue postAdress =RDStore.ADRESS_TYPE_POSTAL

        selectedExportFields.keySet().each {String fieldKey ->
            Map fields = selectedExportFields.get(fieldKey)
            if(!fields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    titles << generalContact.getI10n('value')
                }else if (fieldKey == 'participant.billingContact') {
                    titles << billingContact.getI10n('value')
                }
                else if (fieldKey == 'participant.billingAdress') {
                    titles << billingAdress.getI10n('value')
                }else if (fieldKey == 'participant.postAdress') {
                    titles << postAdress.getI10n('value')
                }
                else if (fieldKey != 'survey.allOtherProperties') {
                    titles << (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label)
                } else {
                    propertyDefinitionList.each { surveyProperty ->
                        titles << (surveyProperty?.getI10n('name'))
                        titles << (messageSource.getMessage('surveyResult.participantComment', null, locale) + " " + messageSource.getMessage('renewalEvaluation.exportRenewal.to', null, locale) + " " + surveyProperty?.getI10n('name'))
                    }
                }
            }
        }

        titles
    }
}
