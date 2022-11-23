package de.laser


import de.laser.base.AbstractCoverage
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.storage.PropertyStore
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.SubscriptionProperty
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyConfigProperties
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import grails.gorm.transactions.Transactional
import org.springframework.context.MessageSource
import org.hibernate.Session

import java.text.SimpleDateFormat

/**
 * This service handles the export configuration: via a modal, the user checks the settings which should be exported and
 * after submit, the export will contain only the selected fields. This procedure is called ClickMe-Export. For every place
 * where ClickMe-exports are being offered a static configuration map is being defined where the settings are being defined
 */
@Transactional
class ExportClickMeService {

    MessageSource messageSource
    ExportService exportService
    AccessPointService accessPointService
    ContextService contextService

    static Map<String, Object> EXPORT_RENEWAL_CONFIG = [
            //Wichtig: Hier bei dieser Config bitte drauf achten, welche Feld Bezeichnung gesetzt ist, 
            // weil die Felder von einer zusammengesetzten Map kommen. siehe SurveyControllerService -> renewalEvaluation
                    survey      : [
                            label: 'Survey',
                            message: 'survey.label',
                            fields: [
                                    'participant.sortname'        : [field: 'participant.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                    'participant.name'            : [field: 'participant.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                                    'survey.period'               : [field: null, label: 'Period', message: 'renewalEvaluation.period', defaultChecked: 'true'],
                                    'survey.periodComment'        : [field: null, label: 'Period Comment', message: 'renewalEvaluation.periodComment', defaultChecked: 'true'],
                                    'survey.costBeforeTax'        : [field: 'resultOfParticipation.costItem.costInBillingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.costBeforeTax', defaultChecked: 'true'],
                                    'survey.costAfterTax'         : [field: 'resultOfParticipation.costItem.costInBillingCurrencyAfterTax', label: 'Cost After Tax', message: 'renewalEvaluation.costAfterTax', defaultChecked: 'true'],
                                    'survey.costTax'              : [field: 'resultOfParticipation.costItem.taxKey.taxRate', label: 'Cost Tax', message: 'renewalEvaluation.costTax', defaultChecked: 'true'],
                                    'survey.currency'             : [field: 'resultOfParticipation.costItem.billingCurrency', label: 'Cost Before Tax', message: 'default.currency.label', defaultChecked: 'true'],
                                    'survey.costPeriod'           : [field: 'resultOfParticipation.costPeriod', label: 'Cost Period', message: 'renewalEvaluation.costPeriod', defaultChecked: 'true'],
                            ]
                    ],

                    participant : [
                            label: 'Participant',
                            message: 'surveyParticipants.label',
                            fields: [
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
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                            'participant.uuid'              : [field: 'participant.globalUID', label: 'Laser-UUID',  message: null],
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
                    participantIdentifiers : [
                            label: 'Identifiers',
                            message: 'exportClickMe.participantIdentifiers',
                            fields: [:],

                    ],
                    participantCustomerIdentifiers : [
                            label: 'Customer Identifier',
                            message: 'exportClickMe.participantCustomerIdentifiers',
                            fields: [:],

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
                                    'subscription.uuid'                         : [field: 'sub.globalUID', label: 'Laser-UUID',  message: null],
                                    ]
                    ]
    ]

    static Map<String, Object> EXPORT_SUBSCRIPTION_MEMBERS_CONFIG = [
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
                            'subscription.uuid'                         : [field: 'sub.globalUID', label: 'Laser-UUID',  message: null],
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
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                            'participant.uuid'              : [field: 'orgs.globalUID', label: 'Laser-UUID',  message: null],
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
            participantIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.participantIdentifiers',
                    fields: [:]
            ],
            participantCustomerIdentifiers : [
                    label: 'Customer Identifiers',
                    message: 'exportClickMe.participantCustomerIdentifiers',
                    fields: [:]
            ],

            participantSubProperties : [
                    label: 'Properties',
                    message: 'exportClickMe.participantSubProperties',
                    fields: [:]
            ],

            participantSubCostItems : [
                    label: 'Cost Items',
                    message: 'subscription.costItems.label',
                    fields: [
                            'costItemsElements' : [:],
                            'costItem.costTitle'                        : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                            'costItem.reference'                        : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                            'costItem.budgetCodes'                      : [field: 'costItem.budgetcodes', label: 'Budget Code', message: 'financials.budgetCode'],
                            'costItem.costItemElementConfiguration'     : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                            'costItem.costItemStatus'                   : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                            'costItem.costInBillingCurrency'            : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                            'costItem.billingCurrency'                  : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                            'costItem.costInBillingCurrencyAfterTax'    : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                            'costItem.currencyRate'                     : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                            'costItem.taxType'                          : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                            'costItem.taxRate'                          : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                            'costItem.costInLocalCurrency'              : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                            'costItem.costInLocalCurrencyAfterTax'      : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                            'costItem.datePaid'                         : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                            'costItem.financialYear'                    : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                            'costItem.invoiceDate'                      : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                            'costItem.startDate'                        : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                            'costItem.endDate'                          : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                            'costItem.costDescription'                  : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                            'costItem.invoiceNumber'                    : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                            'costItem.orderNumber'                      : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                    ]
            ],

    ]

    static Map<String, Object> EXPORT_SUBSCRIPTION_CONFIG = [
            subscription: [
                    label: 'Subscription',
                    message: 'subscription.label',
                    fields: [
                            'subscription.name'                         : [field: 'name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                            'subscription.startDate'                    : [field: 'startDate', label: 'Start Date', message: 'subscription.startDate.label', defaultChecked: 'true'],
                            'subscription.endDate'                      : [field: 'endDate', label: 'End Date', message: 'subscription.endDate.label', defaultChecked: 'true'],
                            'subscription.manualCancellationDate'       : [field: 'manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                            'subscription.isMultiYear'                  : [field: 'isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label', defaultChecked: 'true'],
                            'subscription.status'                       : [field: 'status', label: 'Status', message: 'subscription.status.label', defaultChecked: 'true'],
                            'subscription.kind'                         : [field: 'kind', label: 'Kind', message: 'subscription.kind.label', defaultChecked: 'true'],
                            'subscription.form'                         : [field: 'form', label: 'Form', message: 'subscription.form.label', defaultChecked: 'true'],
                            'subscription.resource'                     : [field: 'resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.uuid'                         : [field: 'globalUID', label: 'Laser-UUID',  message: null],
                    ]
            ],

            licenses: [
                    label: 'License',
                    message: 'license.label',
                    fields: [
                            'license.name'            : [field: 'licenses.reference', label: 'Name', message: 'exportClickMe.license.name', defaultChecked: 'true' ],
                            'license.status'          : [field: 'licenses.status', label: 'Status', message: 'exportClickMe.license.status'],
                            'license.licenseCategory' : [field: 'licenses.licenseCategory', label: 'License Category', message: 'license.licenseCategory.label'],
                            'license.startDate'       : [field: 'licenses.startDate', label: 'Start Date', message: 'exportClickMe.license.startDate'],
                            'license.endDate'         : [field: 'licenses.endDate', label: 'End Date', message: 'exportClickMe.license.endDate'],
                            'license.openEnded'         : [field: 'licenses.openEnded', label: 'Open Ended', message: 'license.openEnded.label'],
                            'license.uuid'            : [field: 'licenses.globalUID', label: 'Laser-UUID',  message: null],
                    ]
            ],

            providers: [
                    label: 'Provider',
                    message: 'default.provider.label',
                    fields: [
                            'provider.sortname'          : [field: 'providers.sortname', label: 'Sortname', message: 'exportClickMe.provider.sortname'],
                            'provider.name'              : [field: 'providers.name', label: 'Name', message: 'exportClickMe.provider.name', defaultChecked: 'true' ],
                            'provider.altnames'          : [field: 'providers.altnames.name', label: 'Alt Name', message: 'exportClickMe.provider.altnames'],
                            'provider.url'               : [field: 'providers.url', label: 'Url', message: 'exportClickMe.provider.url'],
                            'provider.platforms'         : [field: 'providers.platforms.name', label: 'Platform', message: 'org.platforms.label'],
                            'provider.platforms.url'         : [field: 'providers.platforms.primaryUrl', label: 'Primary URL', message: 'platform.primaryURL'],
                    ]
            ],

            agencies: [
                    label: 'Agency',
                    message: 'default.agency.label',
                    fields: [
                            'agency.sortname'          : [field: 'agencies.sortname', label: 'Sortname', message: 'exportClickMe.agency.sortname'],
                            'agency.name'              : [field: 'agencies.name', label: 'Name', message: 'exportClickMe.agency.name', defaultChecked: 'true' ],
                            'agency.altnames'          : [field: 'agencies.altnames.name', label: 'Alt Name', message: 'exportClickMe.agency.altnames'],
                            'agency.url'               : [field: 'agencies.url', label: 'Url', message: 'exportClickMe.agency.url'],
                    ]
            ],

            participantIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.participantIdentifiers',
                    fields: [:],

            ],
            participantCustomerIdentifiers : [
                    label: 'Customer Identifier',
                    message: 'exportClickMe.participantCustomerIdentifiers',
                    fields: [:],

            ],

            subProperties : [
                    label: 'Properties',
                    message: 'default.object.properties',
                    fields: [:]
            ],

            subCostItems : [
                    label: 'Cost Items',
                    message: 'subscription.costItems.label',
                    fields: [
                            'costItemsElements' : [:],
                            'costItem.costTitle'                        : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                            'costItem.reference'                        : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                            'costItem.budgetCodes'                      : [field: 'costItem.budgetcodes', label: 'Budget Code', message: 'financials.budgetCode'],
                            'costItem.costItemElementConfiguration'     : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                            'costItem.costItemStatus'                   : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                            'costItem.costInBillingCurrency'            : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                            'costItem.billingCurrency'                  : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                            'costItem.costInBillingCurrencyAfterTax'    : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                            'costItem.currencyRate'                     : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                            'costItem.taxType'                          : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                            'costItem.taxRate'                          : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                            'costItem.costInLocalCurrency'              : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                            'costItem.costInLocalCurrencyAfterTax'      : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                            'costItem.datePaid'                         : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                            'costItem.financialYear'                    : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                            'costItem.invoiceDate'                      : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                            'costItem.startDate'                        : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                            'costItem.endDate'                          : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                            'costItem.costDescription'                  : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                            'costItem.invoiceNumber'                    : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                            'costItem.orderNumber'                      : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                    ]
            ],

    ]

    static Map<String, Object> EXPORT_COST_ITEM_CONFIG = [
            costItem : [
                    label: 'Cost Item',
                    message: 'costItem.label',
                    fields: [
                            'costItem.costItemElement'                  : [field: 'costItemElement', label: 'Cost Item Element', message: 'financials.costItemElement', defaultChecked: 'true'],
                            'costItem.costTitle'                        : [field: 'costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle', defaultChecked: 'true'],
                            'costItem.reference'                        : [field: 'reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                            'costItem.budgetCodes'                      : [field: 'budgetcodes', label: 'Budget Code', message: 'financials.budgetCode'],
                            'costItem.costItemElementConfiguration'     : [field: 'costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration', defaultChecked: 'true'],
                            'costItem.costItemStatus'                   : [field: 'costItemStatus', label: 'Status', message: 'default.status.label', defaultChecked: 'true'],
                            'costItem.costInBillingCurrency'            : [field: 'costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total', defaultChecked: 'true'],
                            'costItem.billingCurrency'                  : [field: 'billingCurrency', label: 'Billing Currency', message: 'default.currency.label', defaultChecked: 'true'],
                            'costItem.costInBillingCurrencyAfterTax'    : [field: 'costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount', defaultChecked: 'true'],
                            'costItem.currencyRate'                     : [field: 'currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                            'costItem.taxType'                          : [field: 'taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType', defaultChecked: 'true'],
                            'costItem.taxRate'                          : [field: 'taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate', defaultChecked: 'true'],
                            'costItem.costInLocalCurrency'              : [field: 'costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency', defaultChecked: 'true'],
                            'costItem.costInLocalCurrencyAfterTax'      : [field: 'costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax', defaultChecked: 'true'],

                            'costItem.datePaid'                         : [field: 'datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                            'costItem.financialYear'                    : [field: 'financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                            'costItem.invoiceDate'                      : [field: 'invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                            'costItem.startDate'                        : [field: 'startDate', label: 'Date From', message: 'financials.dateFrom'],
                            'costItem.endDate'                          : [field: 'endDate', label: 'Date To', message: 'financials.dateTo'],

                            'costItem.costDescription'                  : [field: 'costDescription', label: 'Description', message: 'default.description.label'],
                            'costItem.invoiceNumber'                    : [field: 'invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                            'costItem.orderNumber'                      : [field: 'order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                    ]
            ],

            org : [
                    label: 'Organisation',
                    message: 'org.label',
                    fields: [
                            'participant.sortname'          : [field: 'sub.subscriber.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'participant.name'              : [field: 'sub.subscriber.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'participant.funderType'        : [field: 'sub.subscriber.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'participant.funderHskType'     : [field: 'sub.subscriber.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'participant.libraryType'       : [field: 'sub.subscriber.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            'participant.eInvoice'          : [field: 'sub.subscriber.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'participant.eInvoicePortal'    : [field: 'sub.subscriber.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'participant.linkResolverBaseURL'    : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                            'participant.uuid'              : [field: 'sub.subscriber.globalUID', label: 'Laser-UUID',  message: null],
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
                            'subscription.uuid'                         : [field: 'sub.globalUID', label: 'Laser-UUID',  message: null],
                    ]
            ],
            participantIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.participantIdentifiers',
                    fields: [:]
            ],
            participantCustomerIdentifiers : [
                    label: 'Customer Identifiers',
                    message: 'exportClickMe.participantCustomerIdentifiers',
                    fields: [:]
            ]
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
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                            'participant.uuid'              : [field: 'globalUID', label: 'Laser-UUID',  message: null],
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
            participantIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.participantIdentifiers',
                    fields: [:]
            ],
            participantCustomerIdentifiers : [
                    label: 'Customer Identifiers',
                    message: 'exportClickMe.participantCustomerIdentifiers',
                    fields: [:]
            ],
            participantProperties : [
                    label: 'Properties',
                    message: 'propertyDefinition.plural',
                    fields: [:]
            ],

    ]

    static Map<String, Object> EXPORT_PROVIDER_CONFIG = [
            provider : [
                    label: 'Provider',
                    message: 'default.provider.label',
                    fields: [
                            'provider.sortname'            : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'provider.name'                : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'provider.altnames'            : [field: 'altnames', label: 'Alternative names', message: 'org.altname.label', defaultChecked: 'true' ],
                            'provider.funderType'          : [field: 'funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'provider.funderHskType'       : [field: 'funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'provider.generalContact'      : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'provider.billingContact'      : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'provider.postAdress'          : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'provider.billingAdress'       : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            'provider.linkResolverBaseURL' : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label']
                    ]
            ],
            providerIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.participantIdentifiers',
                    fields: [:]
            ],
            providerCustomerIdentifiers : [
                    label: 'Customer Identifiers',
                    message: 'exportClickMe.participantCustomerIdentifiers',
                    fields: [:]
            ],
            providerProperties : [
                    label: 'Properties',
                    message: 'propertyDefinition.plural',
                    fields: [:]
            ]

    ]

    static Map<String, Object> EXPORT_SURVEY_EVALUATION = [
            //Wichtig: Hier bei dieser Config bitte drauf achten, welche Feld Bezeichnung gesetzt ist,
            // weil die Felder von einer zusammengesetzten Map kommen. siehe ExportClickMeService -> exportSurveyEvaluation
            survey      : [
                    label: 'Survey',
                    message: 'survey.label',
                    fields: [
                            'participant.sortname'        : [field: 'participant.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'participant.name'            : [field: 'participant.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                    ]
            ],

            participant : [
                    label: 'Participant',
                    message: 'surveyParticipants.label',
                    fields: [
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
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                            'participant.uuid'              : [field: 'participant.globalUID', label: 'Laser-UUID',  message: null],
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
            participantIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.participantIdentifiers',
                    fields: [:],

            ],
            participantCustomerIdentifiers : [
                    label: 'Customer Identifiers',
                    message: 'exportClickMe.participantCustomerIdentifiers',
                    fields: [:],

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
                            'subscription.uuid'                         : [field: 'sub.globalUID', label: 'Laser-UUID',  message: null],
                    ]
            ],

            participantSurveyCostItems : [
                    label: 'Cost Items',
                    message: 'surveyCostItems.label',
                    fields: [
                            'costItem.costItemElement'                  : [field: 'costItemElement', label: 'Cost Item Element', message: 'financials.costItemElement'],
                            'costItem.costTitle'                        : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                            'costItem.reference'                        : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                            'costItem.budgetCodes'                      : [field: 'costItem.budgetcodes', label: 'Budget Code', message: 'financials.budgetCode'],
                            'costItem.costItemElementConfiguration'     : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                            'costItem.costItemStatus'                   : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                            'costItem.costInBillingCurrency'            : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                            'costItem.billingCurrency'                  : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                            'costItem.costInBillingCurrencyAfterTax'    : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                            'costItem.currencyRate'                     : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                            'costItem.taxType'                          : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                            'costItem.taxRate'                          : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                            'costItem.costInLocalCurrency'              : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                            'costItem.costInLocalCurrencyAfterTax'      : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                            'costItem.datePaid'                         : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                            'costItem.financialYear'                    : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                            'costItem.invoiceDate'                      : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                            'costItem.startDate'                        : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                            'costItem.endDate'                          : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                            'costItem.costDescription'                  : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                            'costItem.invoiceNumber'                    : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                            'costItem.orderNumber'                      : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                    ]
            ],

    ]

    static Map<String, Object> EXPORT_ISSUE_ENTITLEMENT_CONFIG = [
            issueEntitlement      : [
                    label: 'IssueEntitlement',
                    message: 'issueEntitlement.label',
                    fields: [
                            'issueEntitlement.name'                 : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'issueEntitlement.status'               : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: 'true'],
                            'issueEntitlement.medium'               : [field: 'medium', label: 'Status', message: 'tipp.medium', defaultChecked: 'true'],
                            'issueEntitlement.accessStartDate'      : [field: 'accessStartDate', label: 'Access Start Date', message: 'subscription.details.access_start', defaultChecked: 'true'],
                            'issueEntitlement.accessEndDate'        : [field: 'accessEndDate', label: 'Access End Date', message: 'subscription.details.access_end', defaultChecked: 'true'],
                            'issueEntitlement.tipp.titleType'       : [field: 'tipp.titleType', label: 'Cost After Tax', message: 'tipp.titleType', defaultChecked: 'true'],
                            'issueEntitlement.tipp.pkg'             : [field: 'tipp.pkg.name', label: 'Package', message: 'package.label', defaultChecked: 'true'],
                            'issueEntitlement.tipp.platform.name'   : [field: 'tipp.platform.name', label: 'Platform', message: 'tipp.platform', defaultChecked: 'true'],
                            'issueEntitlement.tipp.ieGroup.name'   : [field: 'ieGroups.ieGroup.name', label: 'Group', message: 'issueEntitlementGroup.label', defaultChecked: 'true'],
                            'issueEntitlement.tipp.perpetualAccessBySub'   : [field: 'perpetualAccessBySub', label: 'Perpetual Access', message: 'issueEntitlement.perpetualAccessBySub.label', defaultChecked: 'true'],
                    ]
            ],
            titleDetails      : [
                    label: 'Title Details',
                    message: 'title.details',
                    fields: [
                            'issueEntitlement.tipp.hostPlatformURL' : [field: 'tipp.hostPlatformURL', label: 'Url', message: null],
                            'issueEntitlement.tipp.dateFirstOnline' : [field: 'tipp.dateFirstOnline', label: 'Date first online', message: 'tipp.dateFirstOnline'],
                            'issueEntitlement.tipp.dateFirstInPrint' : [field: 'tipp.dateFirstInPrint', label: 'Date first in print', message: 'tipp.dateFirstInPrint'],
                            'issueEntitlement.tipp.firstAuthor'     : [field: 'tipp.firstAuthor', label: 'First Author', message: 'tipp.firstAuthor'],
                            'issueEntitlement.tipp.firstEditor'     : [field: 'tipp.firstEditor', label: 'First Editor', message: 'tipp.firstEditor'],
                            'issueEntitlement.tipp.volume'          : [field: 'tipp.volume', label: 'Volume', message: 'tipp.volume'],
                            'issueEntitlement.tipp.editionStatement': [field: 'tipp.editionStatement', label: 'Edition Statement', message: 'title.editionStatement.label'],
                            'issueEntitlement.tipp.editionNumber'   : [field: 'tipp.editionNumber', label: 'Edition Number', message: 'tipp.editionNumber'],
                            'issueEntitlement.tipp.summaryOfContent': [field: 'tipp.summaryOfContent', label: 'Summary of Content', message: 'title.summaryOfContent.label'],
                            'issueEntitlement.tipp.seriesName'      : [field: 'tipp.seriesName', label: 'Series Name', message: 'tipp.seriesName'],
                            'issueEntitlement.tipp.subjectReference': [field: 'tipp.subjectReference', label: 'Subject Reference', message: 'tipp.subjectReference'],
                            'issueEntitlement.tipp.delayedOA'       : [field: 'tipp.delayedOA', label: 'Delayed OA', message: 'tipp.delayedOA'],
                            'issueEntitlement.tipp.hybridOA'        : [field: 'tipp.hybridOA', label: 'Hybrid OA', message: 'tipp.hybridOA'],
                            'issueEntitlement.tipp.publisherName'   : [field: 'tipp.publisherName', label: 'Publisher', message: 'tipp.publisher'],
                            'issueEntitlement.tipp.accessType'      : [field: 'tipp.accessType', label: 'Access Type', message: 'tipp.accessType'],
                            'issueEntitlement.tipp.openAccess'      : [field: 'tipp.openAccess', label: 'Open Access', message: 'tipp.openAccess'],
                            'issueEntitlement.tipp.ddcs'            : [field: 'tipp.ddcs', label: 'DDCs', message: 'tipp.ddc'],
                            'issueEntitlement.tipp.languages'       : [field: 'tipp.languages', label: 'Languages', message: 'tipp.language'],
                            'issueEntitlement.tipp.publishers'       : [field: 'tipp.publishers', label: 'Publishers', message: 'tipp.provider']
                    ]
            ],
            coverage: [
                    label: 'Coverage',
                    message: 'tipp.coverage',
                    fields: [
                            'coverage.startDate'        : [field: 'startDate', label: 'Start Date', message: 'tipp.startDate'],
                            'coverage.startVolume'      : [field: 'startVolume', label: 'Start Volume', message: 'tipp.startVolume'],
                            'coverage.startIssue'       : [field: 'startIssue', label: 'Start Issue', message: 'tipp.startIssue'],
                            'coverage.endDate'          : [field: 'endDate', label: 'End Date', message: 'tipp.endDate'],
                            'coverage.endVolume'        : [field: 'endVolume', label: 'End Volume', message: 'tipp.endVolume'],
                            'coverage.endIssue'         : [field: 'endIssue', label: 'End Issue', message: 'tipp.endIssue'],
                            'coverage.coverageNote'     : [field: 'coverageNote', label: 'Coverage Note', message: 'default.note.label'],
                            'coverage.coverageDepth'    : [field: 'coverageDepth', label: 'Coverage Depth', message: 'tipp.coverageDepth'],
                            'coverage.embargo'          : [field: 'embargo', label: 'Embargo', message: 'tipp.embargo']
                    ]
            ],
            priceItem: [
                    label: 'Price Item',
                    message: 'costItem.label',
                    fields: [
                            'listPriceEUR'    : [field: null, label: 'List Price EUR', message: 'tipp.listprice_eur'],
                            'listPriceGBP'    : [field: null, label: 'List Price GBP', message: 'tipp.listprice_gbp'],
                            'listPriceUSD'    : [field: null, label: 'List Price USD', message: 'tipp.listprice_usd'],
                            'localPriceEUR'   : [field: null, label: 'Local Price EUR', message: 'tipp.localprice_eur'],
                            'localPriceGBP'   : [field: null, label: 'Local Price GBP', message: 'tipp.localprice_gbp'],
                            'localPriceUSD'   : [field: null, label: 'Local Price USD', message: 'tipp.localprice_usd']
                    ]
            ],
            issueEntitlementIdentifiers : [
                    label: 'Identifiers',
                    fields: [:],

            ],

            subscription: [
                    label: 'Subscription',
                    message: 'subscription.label',
                    fields: [
                            'subscription.name'                         : [field: 'subscription.name', label: 'Name', message: 'subscription.name.label'],
                            'subscription.startDate'                    : [field: 'subscription.startDate', label: 'Start Date', message: 'subscription.startDate.label'],
                            'subscription.endDate'                      : [field: 'subscription.endDate', label: 'End Date', message: 'subscription.endDate.label'],
                            'subscription.manualCancellationDate'       : [field: 'subscription.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                            'subscription.isMultiYear'                  : [field: 'subscription.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label'],
                            'subscription.status'                       : [field: 'subscription.status', label: 'Status', message: 'subscription.status.label'],
                            'subscription.kind'                         : [field: 'subscription.kind', label: 'Kind', message: 'subscription.kind.label'],
                            'subscription.form'                         : [field: 'subscription.form', label: 'Form', message: 'subscription.form.label'],
                            'subscription.resource'                     : [field: 'subscription.resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'subscription.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'subscription.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.uuid'                         : [field: 'subscription.globalUID', label: 'Laser-UUID',  message: null],
                    ]
            ],
    ]

    static Map<String, Object> EXPORT_TIPP_CONFIG = [
            tipp      : [
                    label: 'Title',
                    message: 'default.title.label',
                    fields: [
                            'tipp.name'            : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'tipp.status'          : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: 'true'],
                            'tipp.medium'          : [field: 'medium', label: 'Status', message: 'tipp.medium', defaultChecked: 'true'],
                            'tipp.titleType'       : [field: 'titleType', label: 'Cost After Tax', message: 'tipp.titleType', defaultChecked: 'true'],
                            'tipp.pkg'             : [field: 'pkg.name', label: 'Package', message: 'package.label', defaultChecked: 'true'],
                            'tipp.platform.name'   : [field: 'platform.name', label: 'Platform', message: 'tipp.platform', defaultChecked: 'true'],
                    ]
            ],
            titleDetails      : [
                    label: 'Title Details',
                    message: 'title.details',
                    fields: [
                            'tipp.hostPlatformURL' : [field: 'hostPlatformURL', label: 'Url', message: null],
                            'tipp.dateFirstOnline' : [field: 'dateFirstOnline', label: 'Date first online', message: 'tipp.dateFirstOnline'],
                            'tipp.dateFirstInPrint' : [field: 'dateFirstInPrint', label: 'Date first in print', message: 'tipp.dateFirstInPrint'],
                            'tipp.firstAuthor'     : [field: 'firstAuthor', label: 'First Author', message: 'tipp.firstAuthor'],
                            'tipp.firstEditor'     : [field: 'firstEditor', label: 'First Editor', message: 'tipp.firstEditor'],
                            'tipp.volume'          : [field: 'volume', label: 'Volume', message: 'tipp.volume'],
                            'tipp.editionStatement': [field: 'editionStatement', label: 'Edition Statement', message: 'title.editionStatement.label'],
                            'tipp.editionNumber'   : [field: 'editionNumber', label: 'Edition Number', message: 'tipp.editionNumber'],
                            'tipp.summaryOfContent': [field: 'summaryOfContent', label: 'Summary of Content', message: 'title.summaryOfContent.label'],
                            'tipp.seriesName'      : [field: 'seriesName', label: 'Series Name', message: 'tipp.seriesName'],
                            'tipp.subjectReference': [field: 'subjectReference', label: 'Subject Reference', message: 'tipp.subjectReference'],
                            'tipp.delayedOA'       : [field: 'delayedOA', label: 'Delayed OA', message: 'tipp.delayedOA'],
                            'tipp.hybridOA'        : [field: 'hybridOA', label: 'Hybrid OA', message: 'tipp.hybridOA'],
                            'tipp.publisherName'   : [field: 'publisherName', label: 'Publisher', message: 'tipp.publisher'],
                            'tipp.accessType'      : [field: 'accessType', label: 'Access Type', message: 'tipp.accessType'],
                            'tipp.openAccess'      : [field: 'openAccess', label: 'Open Access', message: 'tipp.openAccess'],
                            'tipp.ddcs'            : [field: 'ddcs', label: 'DDCs', message: 'tipp.ddc'],
                            'tipp.languages'       : [field: 'languages', label: 'Languages', message: 'tipp.language'],
                            'tipp.publishers'       : [field: 'publishers', label: 'Publishers', message: 'tipp.provider']
                    ]
            ],
            coverage: [
                    label: 'Coverage',
                    message: 'tipp.coverage',
                    fields: [
                            'coverage.startDate'        : [field: 'startDate', label: 'Start Date', message: 'tipp.startDate'],
                            'coverage.startVolume'      : [field: 'startVolume', label: 'Start Volume', message: 'tipp.startVolume'],
                            'coverage.startIssue'       : [field: 'startIssue', label: 'Start Issue', message: 'tipp.startIssue'],
                            'coverage.endDate'          : [field: 'endDate', label: 'End Date', message: 'tipp.endDate'],
                            'coverage.endVolume'        : [field: 'endVolume', label: 'End Volume', message: 'tipp.endVolume'],
                            'coverage.endIssue'         : [field: 'endIssue', label: 'End Issue', message: 'tipp.endIssue'],
                            'coverage.coverageNote'     : [field: 'coverageNote', label: 'Coverage Note', message: 'default.note.label'],
                            'coverage.coverageDepth'    : [field: 'coverageDepth', label: 'Coverage Depth', message: 'tipp.coverageDepth'],
                            'coverage.embargo'          : [field: 'embargo', label: 'Embargo', message: 'tipp.embargo']
                    ]
            ],
            priceItem: [
                    label: 'Price Item',
                    message: 'costItem.label',
                    fields: [
                            'listPriceEUR'    : [field: null, label: 'List Price EUR', message: 'tipp.listprice_eur'],
                            'listPriceGBP'    : [field: null, label: 'List Price GBP', message: 'tipp.listprice_gbp'],
                            'listPriceUSD'    : [field: null, label: 'List Price USD', message: 'tipp.listprice_usd'],
                            'localPriceEUR'   : [field: null, label: 'Local Price EUR', message: 'tipp.localprice_eur'],
                            'localPriceGBP'   : [field: null, label: 'Local Price GBP', message: 'tipp.localprice_gbp'],
                            'localPriceUSD'   : [field: null, label: 'Local Price USD', message: 'tipp.localprice_usd']
                    ]
            ],
            tippIdentifiers : [
                    label: 'Identifiers',
                    fields: [:],

            ],
    ]

    /**
     * Gets the fields for the subscription renewal for the given survey for processing
     * @param surveyConfig the survey to which the renewal fields should be generated
     * @return the configuration map for the survey
     */
    Map<String, Object> getExportRenewalFields(SurveyConfig surveyConfig) {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_RENEWAL_CONFIG.keySet().each {
            EXPORT_RENEWAL_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [subscription: surveyConfig.subscription]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        def removeSurveyProperties = exportFields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            exportFields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            exportFields.put("surveyProperty."+surveyConfigProperties.surveyProperty.id, [field: null, label: "${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true'])
        }

        if(!(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in surveyConfig.surveyProperties.surveyProperty.id) && !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in surveyConfig.surveyProperties.surveyProperty.id)){
            exportFields.remove('survey.period')
            exportFields.remove('survey.periodComment')
        }else{
            if(!exportFields.containsKey('survey.period')){
                exportFields.put('survey.period', [field: null, label: 'Period', message: 'renewalEvaluation.period', defaultChecked: 'true'])
            }
            if(!exportFields.containsKey('survey.periodComment')){
                exportFields.put('survey.periodComment', [field: null, label: 'Period Comment', message: 'renewalEvaluation.periodComment', defaultChecked: 'true'] )
            }
        }

        exportFields
    }

    /**
     * Called from _individuallyExportRenewModal.gsp
     * Gets the fields for the subscription renewal for the given survey and prepares them for the UI
     * @param surveyConfig the survey to which the renewal fields should be generated
     * @return the configuration map for the survey for the modal
     */
    Map<String, Object> getExportRenewalFieldsForUI(SurveyConfig surveyConfig) {

        Map<String, Object> fields = EXPORT_RENEWAL_CONFIG as Map
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        fields.participantIdentifiers.fields.clear()
        fields.participantCustomerIdentifiers.fields.clear()
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [subscription: surveyConfig.subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }

        def removeSurveyProperties = fields.survey.fields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            fields.survey.fields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            fields.survey.fields << ["surveyProperty.${surveyConfigProperties.surveyProperty.id}": [field: null, label: "${messageSource.getMessage('surveyProperty.label', null, locale)}: ${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true']]
        }

        if(!(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in surveyConfig.surveyProperties.surveyProperty.id) &&  !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in surveyConfig.surveyProperties.surveyProperty.id)){
            fields.survey.fields.remove('survey.period')
            fields.survey.fields.remove('survey.periodComment')
        }else{
            if(!fields.survey.fields.containsKey('survey.period')){
                fields.survey.fields <<['survey.period': [field: null, label: 'Period', message: 'renewalEvaluation.period', defaultChecked: 'true']]
            }
            if(!fields.survey.fields.containsKey('survey.periodComment')){
                fields.survey.fields << ['survey.periodComment': [field: null, label: 'Period Comment', message: 'renewalEvaluation.periodComment', defaultChecked: 'true']]
            }
        }

        fields
    }

    /**
     * Gets the subscription member export fields for the given subscription and institution for processing
     * @param subscription the subscription whose members should be exported
     * @param institution the context institution
     * @return the configuration map for the subscription member export
     */
    Map<String, Object> getExportSubscriptionMembersFields(Subscription subscription, Org institution) {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_SUBSCRIPTION_MEMBERS_CONFIG.keySet().each {
            EXPORT_SUBSCRIPTION_MEMBERS_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat = (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription) and ci.value != null', [subscription: subscription]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }
        List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
        if(childSubs) {
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: institution])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                exportFields.put("participantSubProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}"])
            }

            CostItem.findAllBySubInListAndCostItemStatusNotEqualAndCostItemElementIsNotNull(childSubs, RDStore.COST_ITEM_DELETED).groupBy {it.costItemElement.id}.each {
                exportFields.put("participantSubCostItem."+it.key, [field: null, label: RefdataValue.get(it.key).getI10n('value')])
            }
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the subscription member export fields for the given subscription and institution and prepares them for the UI
     * @param subscription the subscription whose members should be exported
     * @param institution the context institution
     * @return the configuration map for the subscription member export for the UI
     */
    Map<String, Object> getExportSubscriptionMembersFieldsForUI(Subscription subscription, Org institution) {

        Map<String, Object> fields = EXPORT_SUBSCRIPTION_MEMBERS_CONFIG as Map
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription) = plat and ci.value != null order by plat.name',[subscription:subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }

        fields.participantSubProperties.fields.clear()
        fields.participantSubCostItems.fields.costItemsElements.clear()

        List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
        if(childSubs) {
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: institution])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                fields.participantSubProperties.fields << ["participantSubProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)]]
            }

            CostItem.findAllBySubInListAndCostItemStatusNotEqualAndCostItemElementIsNotNull(childSubs, RDStore.COST_ITEM_DELETED).groupBy {it.costItemElement.id}.each {
                fields.participantSubCostItems.fields.costItemsElements << ["participantSubCostItem.${it.key}":[field: null, label: RefdataValue.get(it.key).getI10n('value')]]
            }
        }

        fields
    }

    /**
     * Gets the subscription fields for the given institution
     * @param institution the context institution whose perspective should be taken for the export
     * @return the configuration map for the subscription export
     */
    Map<String, Object> getExportSubscriptionFields(Org institution) {

        Map<String, Object> exportFields = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName
        String localizedValue
        switch (locale) {
            case Locale.GERMANY:
            case Locale.GERMAN: localizedName = "name_de"
                localizedValue = "value_de"
                break
            default: localizedName = "name_en"
                localizedValue = "value_en"
                break
        }

        EXPORT_SUBSCRIPTION_CONFIG.keySet().each {
            EXPORT_SUBSCRIPTION_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        String consortiaFilter = ''
        if(institution.getCustomerType() == 'ORG_CONSORTIUM')
            consortiaFilter = ' and s.instanceOf = null '
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat = in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx '+consortiaFilter+')', [ctx: institution]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:institution,availableTypes:[PropertyDefinition.SUB_PROP]])


        propList.each { PropertyDefinition propertyDefinition ->
            exportFields.put("subProperty." + propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}"])
        }

        String query = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv." + localizedValue
        RefdataValue.executeQuery(query, [category: RDConstants.COST_ITEM_ELEMENT]).each { RefdataValue refdataValue ->
            exportFields.put("subCostItem." + refdataValue.id, [field: null, label: refdataValue."${localizedValue}"])
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the subscription fields for the given institution for the UI
     * @param institution the context institution whose perspective should be taken for the export
     * @return the configuration map for the subscription export for the UI
     */
    Map<String, Object> getExportSubscriptionFieldsForUI(Org institution) {

        Map<String, Object> fields = EXPORT_SUBSCRIPTION_CONFIG as Map
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName
        String localizedValue
        switch (locale) {
            case Locale.GERMANY:
            case Locale.GERMAN: localizedName = "name_de"
                localizedValue = "value_de"
                break
            default: localizedName = "name_en"
                localizedValue = "value_en"
                break
        }

        fields.subProperties.fields.clear()
        fields.subCostItems.fields.costItemsElements.clear()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        String consortiaFilter = ''
        if(institution.getCustomerType() == 'ORG_CONSORTIUM')
            consortiaFilter = ' and s.instanceOf = null '
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx '+consortiaFilter+') order by plat.name', [ctx:institution]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:institution,availableTypes:[PropertyDefinition.SUB_PROP]])


        propList.each { PropertyDefinition propertyDefinition ->
            fields.subProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)]]
        }

        String query = "select rdv from RefdataValue as rdv where rdv.owner.desc=:category order by rdv.order, rdv." + localizedValue
        RefdataValue.executeQuery(query, [category: RDConstants.COST_ITEM_ELEMENT]).each { RefdataValue refdataValue ->
            fields.subCostItems.fields.costItemsElements << ["subCostItem.${refdataValue.id}": [field: null, label: refdataValue."${localizedValue}"]]
        }

        fields
    }

    /**
     * Gets the cost item fields for the given institution
     * @return the configuration map for the cost item export
     */
    Map<String, Object> getExportCostItemFields(Subscription sub = null) {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_COST_ITEM_CONFIG.keySet().each {
            EXPORT_COST_ITEM_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        String subquery
        Map<String, Object> queryParams = [ctx: contextService.getOrg()]
        if(sub) {
            subquery = '(select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :s)'
            queryParams.s = sub
        }
        else {
            subquery = '(select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx)'
        }
        Set<Platform> platforms = Platform.executeQuery('select plat from CustomerIdentifier ci join ci.platform plat where plat in '+subquery+' and ci.value != null and ci.customer = :ctx order by plat.name', queryParams)
        platforms.each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        exportFields
    }

    /**
     * Gets the cost item fields for the given institution
     * @return the configuration map for the cost item export for UI
     */
    Map<String, Object> getExportCostItemFieldsForUI(Subscription sub = null) {

        Map<String, Object> fields = EXPORT_COST_ITEM_CONFIG as Map
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        fields.participantIdentifiers.fields.clear()
        fields.participantCustomerIdentifiers.fields.clear()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        String subquery
        Map<String, Object> queryParams = [ctx: contextService.getOrg()]
        if(sub) {
            subquery = '(select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :s)'
            queryParams.s = sub
        }
        else {
            subquery = '(select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx)'
        }
        Set<Platform> platforms = Platform.executeQuery('select plat from CustomerIdentifier ci join ci.platform plat where plat in '+subquery+' and ci.value != null and ci.customer = :ctx order by plat.name', queryParams)
        platforms.each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }

        fields
    }

    /**
     * Gets the organisation fields for the given perspective configuration
     * @param config the organisation type to be exported
     * @return the configuration map for the organisation export
     */
    Map<String, Object> getExportOrgFields(String config) {

        Map<String, Object> exportFields = [:], contextParams = [ctx: contextService.getOrg()]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        switch(config) {
            case 'institution':
            case 'member':
                EXPORT_ORG_CONFIG.keySet().each {
                    EXPORT_ORG_CONFIG.get(it).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }

                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
                }

                Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
                    exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
                }

                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    exportFields.put("participantProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}"])
                }
                break
            case 'provider':
                EXPORT_PROVIDER_CONFIG.keySet().each {
                    EXPORT_PROVIDER_CONFIG.get(it).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }

                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    exportFields.put("providerIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
                }

                Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
                    exportFields.put("providerCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
                }

                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    exportFields.put("providerProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}"])
                }
                break
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the organisation fields for the given perspective configuration for the UI
     * @param orgType the organisation type to be exported
     * @return the configuration map for the organisation export for UI
     */
    Map<String, Object> getExportOrgFieldsForUI(String orgType) {

        Map<String, Object> fields, contextParams = [ctx: contextService.getOrg()]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        switch(orgType) {
            case 'institution': fields = EXPORT_ORG_CONFIG as Map
                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
                }
                Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx) order by plat.name', contextParams).each { Platform plat ->
                    fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
                }
                fields.participantProperties.fields.clear()
                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    fields.participantProperties.fields << ["participantProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)]]
                }
                break
            case 'provider': fields = EXPORT_PROVIDER_CONFIG as Map
                IdentifierNamespace.findAllByNsType(Org.class.name, [sort: 'ns']).each {
                    fields.providerIdentifiers.fields << ["providerIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
                }
                Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx) order by plat.name', contextParams).each { Platform plat ->
                    fields.providerCustomerIdentifiers.fields << ["providerCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
                }
                fields.providerProperties.fields.clear()
                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    fields.providerProperties.fields << ["providerProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)]]
                }
                break
            default: fields = [:]
                break
        }

        fields
    }

    /**
     * Gets the survey evaluation fields for export
     * @param surveyConfig the survey whose evaluation should be exported
     * @return the configuration map for the survey evaluation export
     */
    Map<String, Object> getExportSurveyEvaluationFields(SurveyConfig surveyConfig) {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_SURVEY_EVALUATION.keySet().each {
            EXPORT_SURVEY_EVALUATION.get(it).fields.each {

                if(!(surveyConfig.pickAndChoose || !surveyConfig.subscription) && it.key.startsWith('costItem')){
                    //do nothing
                }
                else if(!surveyConfig.subscription && it.key.startsWith('subscription')){
                    //do nothing
                }else {
                    exportFields.put(it.key, it.value)
                }
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (:participants) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [participants: surveyConfig.orgs.org, subscription: surveyConfig.subscription]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        def removeSurveyProperties = exportFields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            exportFields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            exportFields.put("surveyProperty."+surveyConfigProperties.surveyProperty.id, [field: null, label: "${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true'])
        }


        exportFields
    }

    /**
     * Generic call from views
     * Gets the survey evaluation fields for export for the UI
     * @param surveyConfig the survey whose evaluation should be exported
     * @return the configuration map for the survey evaluation export for UI
     */
    Map<String, Object> getExportSurveyEvaluationFieldsForUI(SurveyConfig surveyConfig) {

        Map<String, Object> fields = EXPORT_SURVEY_EVALUATION as Map
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (:participants) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [participants: surveyConfig.orgs.org, subscription: surveyConfig.subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }

        def removeSurveyProperties = fields.survey.fields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            fields.survey.fields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            fields.survey.fields << ["surveyProperty.${surveyConfigProperties.surveyProperty.id}": [field: null, label: "${messageSource.getMessage('surveyProperty.label', null, locale)}: ${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true']]
        }

        if(!surveyConfig.subscription){
            fields.remove('subscription')
        }else {
            if(!fields.containsKey('subscription')){
                fields.put('subscription', [
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
                ])
            }
        }

        if(surveyConfig.pickAndChoose || !surveyConfig.subscription){
            fields.remove('participantSurveyCostItems')
        }else {
            if(!fields.containsKey('participantSurveyCostItems')){
                fields.put('participantSurveyCostItems',[
                        label: 'Cost Items',
                        message: 'surveyCostItems.label',
                        fields: [
                                'costItem.costItemElement'                  : [field: 'costItemElement', label: 'Cost Item Element', message: 'financials.costItemElement'],
                                'costItem.costTitle'                        : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'costItem.reference'                        : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                                'costItem.budgetCodes'                      : [field: 'costItem.budgetcodes', label: 'Budget Code', message: 'financials.budgetCode'],
                                'costItem.costItemElementConfiguration'     : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                                'costItem.costItemStatus'                   : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                                'costItem.costInBillingCurrency'            : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                                'costItem.billingCurrency'                  : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                                'costItem.costInBillingCurrencyAfterTax'    : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                                'costItem.currencyRate'                     : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                                'costItem.taxType'                          : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                                'costItem.taxRate'                          : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                                'costItem.costInLocalCurrency'              : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                                'costItem.costInLocalCurrencyAfterTax'      : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                                'costItem.datePaid'                         : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                                'costItem.financialYear'                    : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                                'costItem.invoiceDate'                      : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                                'costItem.startDate'                        : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'costItem.endDate'                          : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                                'costItem.costDescription'                  : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                                'costItem.invoiceNumber'                    : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                                'costItem.orderNumber'                      : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                        ]
                ])
            }
        }

        fields
    }

    /**
     * Generic call from views
     * Gets the issue entitlement export fields and prepares them for the UI
     * @return the configuration map for the issue entitlement export for the UI
     */
    Map<String, Object> getExportIssueEntitlementFieldsForUI() {

        Map<String, Object> fields = EXPORT_ISSUE_ENTITLEMENT_CONFIG as Map
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        IdentifierNamespace.findAllByNsType(TitleInstancePackagePlatform.class.name, [sort: 'ns']).each {
            fields.issueEntitlementIdentifiers.fields << ["issueEntitlementIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }

        fields
    }

    /**
     * Gets the issue entitlement fields
     * @return the configuration map for the issue entitlement export
     */
    Map<String, Object> getExportIssueEntitlementFields() {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_ISSUE_ENTITLEMENT_CONFIG.keySet().each {
            EXPORT_ISSUE_ENTITLEMENT_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("issueEntitlementIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the title export fields and prepares them for the UI
     * @return the configuration map for the title export for the UI
     */
    Map<String, Object> getExportTippFieldsForUI() {

        Map<String, Object> fields = EXPORT_TIPP_CONFIG as Map
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        IdentifierNamespace.findAllByNsType(TitleInstancePackagePlatform.class.name, [sort: 'ns']).each {
            fields.tippIdentifiers.fields << ["tippIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }

        fields
    }

    /**
     * Gets the title fields
     * @return the configuration map for the title export
     */
    Map<String, Object> getExportTippFields() {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_TIPP_CONFIG.keySet().each {
            EXPORT_TIPP_CONFIG.get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("tippIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }

        exportFields
    }

    /**
     * Exports the selected fields of the given renewal result
     * @param renewalResult the result to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportRenewalResult(Map renewalResult, Map<String, Object> selectedFields) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportRenewalFields(renewalResult.surveyConfig)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale)

        List renewalData = []

        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.continuetoSubscription.label', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})", style: 'positive']])

        renewalResult.orgsContinuetoSubscription.sort { it.participant.sortname }.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.withMultiYearTermSub.label', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})", style: 'positive']])


        renewalResult.orgsWithMultiYearTermSub.sort{it.getSubscriber().sortname}.each { sub ->

            _setRenewalRow([participant: sub.getSubscriber(), sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey)

        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.orgsWithParticipationInParentSuccessor.label', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})", style: 'positive']])


        renewalResult.orgsWithParticipationInParentSuccessor.each { sub ->
            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->
                _setRenewalRow([participant: subscriberOrg, sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey)
            }
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.newOrgstoSubscription.label', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})", style: 'positive']])


        renewalResult.newOrgsContinuetoSubscription.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('renewalEvaluation.withTermination.label', null, locale) + " (${renewalResult.orgsWithTermination.size()})", style: 'negative']])


        renewalResult.orgsWithTermination.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey)
        }

        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: '', style: null]])
        renewalData.add([[field: messageSource.getMessage('surveys.tabs.termination', null, locale) + " (${renewalResult.orgsWithoutResult.size()})", style: 'negative']])


        renewalResult.orgsWithoutResult.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey)
        }


        Map sheetData = [:]
        sheetData[messageSource.getMessage('renewalexport.renewals', null, locale)] = [titleRow: titles, columnData: renewalData]

        if (renewalResult.orgsContinuetoSubscription) {
            sheetData = _exportAccessPoints(renewalResult.orgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 1")
        }

        if (renewalResult.orgsWithMultiYearTermSub) {
            sheetData = _exportAccessPoints(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, sheetData, selectedExportFields, locale, " - 2")
        }

        if (renewalResult.orgsWithParticipationInParentSuccessor) {
            sheetData = _exportAccessPoints(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, sheetData, selectedExportFields, locale, " - 3")
        }

        if (renewalResult.newOrgsContinuetoSubscription) {
            sheetData = _exportAccessPoints(renewalResult.newOrgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 4")
        }


        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the selected fields about the members of the given subscription for the given institution
     * @param result the subscription members to export
     * @param selectedFields the fields which should appear
     * @param subscription the subscription as reference for the fields
     * @param institution the institution as reference for the fields
     * @return an Excel worksheet containing the export
     */
    def exportSubscriptionMembers(List result, Map<String, Object> selectedFields, Subscription subscription, Org institution) {
       Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionMembersFields(subscription, institution)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List<RefdataValue> selectedCostItemElements = []
        List<String> removeSelectedCostItemElements = []
        selectedExportFields.keySet().findAll {it.startsWith('participantSubCostItem.')}.each {
            selectedCostItemElements << RefdataValue.get(Long.parseLong(it.split("\\.")[1]))
            removeSelectedCostItemElements << it
        }
        Map selectedCostItemFields = [:]
        if(selectedCostItemElements){
            selectedExportFields.keySet().findAll {it.startsWith('costItem.')}.each {
                selectedCostItemFields.put(it, selectedExportFields.get(it))
            }
            selectedCostItemFields.each {
                selectedExportFields.remove(it.key)
            }
            removeSelectedCostItemElements.each {
                selectedExportFields.remove(it)
            }
            selectedExportFields.put('participantSubCostItem', [:])
        }

        Integer maxCostItemsElements = 0

        List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
        if(childSubs) {
            maxCostItemsElements = CostItem.executeQuery('select count(id) as countCostItems from CostItem where sub in (:subs) group by costItemElement, sub order by countCostItems desc', [subs: childSubs])[0]
        }

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements)

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        List exportData = []
        List orgList = []
        result.each { memberResult ->
            _setSubRow(memberResult, selectedExportFields, exportData, localizedName, selectedCostItemElements, selectedCostItemFields)
            orgList << memberResult.orgs
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('subscriptionDetails.members.members', null, locale)] = [titleRow: titles, columnData: exportData]

        sheetData =  _exportAccessPoints(orgList, sheetData, selectedExportFields, locale, "")

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given subscriptions
     * @param result the subscription set to export
     * @param selectedFields the fields which should appear
     * @param institution the institution as reference
     * @return an Excel worksheet containing the export
     */
    def exportSubscriptions(ArrayList<Subscription> result, Map<String, Object> selectedFields, Org institution) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionFields(institution)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List<RefdataValue> selectedCostItemElements = []
        List<String> removeSelectedCostItemElements = []
        selectedExportFields.keySet().findAll {it.startsWith('subCostItem.')}.each {
            selectedCostItemElements << RefdataValue.get(Long.parseLong(it.split("\\.")[1]))
            removeSelectedCostItemElements << it
        }
        Map selectedCostItemFields = [:]
        if(selectedCostItemElements){
            selectedExportFields.keySet().findAll {it.startsWith('costItem.')}.each {
                selectedCostItemFields.put(it, selectedExportFields.get(it))
            }
            selectedCostItemFields.each {
                selectedExportFields.remove(it.key)
            }
            removeSelectedCostItemElements.each {
                selectedExportFields.remove(it)
            }
            selectedExportFields.put('subCostItem', [:])
        }

        Integer maxCostItemsElements = 0

        maxCostItemsElements = CostItem.executeQuery('select count(id) as countCostItems from CostItem where sub in (:subs) group by costItemElement, sub order by countCostItems desc', [subs: result])[0]

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements)

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        List exportData = []
        result.each { Subscription subscription ->
            _setSubRow(subscription, selectedExportFields, exportData, localizedName, selectedCostItemElements, selectedCostItemFields)
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('myinst.currentSubscriptions.label', null, locale)] = [titleRow: titles, columnData: exportData]

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given cost items
     * @param result the cost item set to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportCostItems(Map result, Map<String, Object> selectedFields) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportCostItemFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }
        Map sheetData = [:]

        List titles = _exportTitles(selectedExportFields, locale)

        result.cost_item_tabs.entrySet().each { cit ->
            String sheettitle
            String viewMode = cit.getKey()
            switch (viewMode) {
                case "own": sheettitle = messageSource.getMessage('financials.header.ownCosts', null, locale)
                    break
                case "cons": sheettitle = messageSource.getMessage('financials.header.consortialCosts', null, locale)
                    break
                case "subscr": sheettitle = messageSource.getMessage('financials.header.subscriptionCosts', null, locale)
                    break
            }

            List exportData = []

            if (cit.getValue().count > 0) {
                cit.getValue().costItems.eachWithIndex { ci, int i ->
                    _setCostItemRow(ci, selectedExportFields, exportData)
                }
            }
            sheetData[sheettitle] = [titleRow: titles, columnData: exportData]
        }

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given cost items
     * @param result the organisation set to export
     * @param selectedFields the fields which should appear in the export
     * @param config the organisation type to be exported
     * @return an Excel worksheet containing the output
     */
    def exportOrgs(List<Org> result, Map<String, Object> selectedFields, String config) {
        Locale locale = LocaleUtils.getCurrentLocale()

        String sheetTitle

        switch(config) {
            case 'institution':
                sheetTitle = messageSource.getMessage('default.institution', null, locale)
                break
            case 'member':
                sheetTitle = messageSource.getMessage('subscription.details.consortiaMembers.label', null, locale)
                break
            case 'provider':
                sheetTitle = messageSource.getMessage('default.ProviderAgency.export.label', null, locale)
                break
        }

        Map<String, Object> selectedExportFields = [:], configFields = getExportOrgFields(config)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale)

        List exportData = []
        result.each { Org org ->
            _setOrgRow(org, selectedExportFields, exportData)
        }

        Map sheetData = [:]
        sheetData[sheetTitle] = [titleRow: titles, columnData: exportData]

        sheetData =  _exportAccessPoints(result, sheetData, selectedExportFields, locale, "")

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields of the given survey evaluation
     * @param result the survey evaluation which should be exported
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportSurveyEvaluation(Map result, Map<String, Object> selectedFields) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSurveyEvaluationFields(result.surveyConfig)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale)

        Map selectedCostItemFields = [:]
        selectedExportFields.keySet().findAll { it.startsWith('costItem.') }.each {
            selectedCostItemFields.put(it, selectedExportFields.get(it))
        }
        selectedCostItemFields.each {
            selectedExportFields.remove(it.key)
        }

        selectedExportFields.put('participantSurveyCostItem', [:])


        List<SurveyOrg> participantsNotFinish = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfig(result.surveyConfig)
        List<SurveyOrg> participantsFinish = SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)

        List exportData = []

        exportData.add([[field: messageSource.getMessage('surveyEvaluation.participantsViewAllFinish', null, locale) + " (${participantsFinish.size()})", style: 'positive']])

        participantsFinish.sort { it.org.sortname }.each { SurveyOrg surveyOrg ->
            Map participantResult = [:]
            participantResult.properties = SurveyResult.findAllByParticipantAndSurveyConfig(surveyOrg.org, result.surveyConfig)

            participantResult.sub = [:]
            if(result.surveyConfig.subscription) {
                participantResult.sub = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(surveyOrg.org)
            }

            participantResult.participant = surveyOrg.org
            participantResult.surveyCostItem = CostItem.findBySurveyOrg(surveyOrg)
            participantResult.surveyConfig = result.surveyConfig

            _setSurveyEvaluationRow(participantResult, selectedExportFields, exportData, selectedCostItemFields)
        }

        exportData.add([[field: '', style: null]])
        exportData.add([[field: '', style: null]])
        exportData.add([[field: '', style: null]])
        exportData.add([[field: messageSource.getMessage('surveyEvaluation.participantsViewAllNotFinish', null, locale) + " (${participantsNotFinish.size()})", style: 'negative']])


        participantsNotFinish.sort { it.org.sortname }.each { SurveyOrg surveyOrg ->
            Map participantResult = [:]
            participantResult.properties = SurveyResult.findAllByParticipantAndSurveyConfig(surveyOrg.org, result.surveyConfig)

            participantResult.sub = [:]
            if(result.surveyConfig.subscription) {
                participantResult.sub = result.surveyConfig.subscription.getDerivedSubscriptionBySubscribers(surveyOrg.org)
            }

            participantResult.participant = surveyOrg.org
            participantResult.surveyCostItem = CostItem.findBySurveyOrg(surveyOrg)
            participantResult.surveyConfig = result.surveyConfig

            _setSurveyEvaluationRow(participantResult, selectedExportFields, exportData, selectedCostItemFields)
        }


        Map sheetData = [:]
        sheetData[messageSource.getMessage('surveyInfo.evaluation', null, locale)] = [titleRow: titles, columnData: exportData]

        if (participantsFinish) {
            sheetData = _exportAccessPoints(participantsFinish.org, sheetData, selectedExportFields, locale, " - 1")
        }

        if (participantsNotFinish) {
            sheetData = _exportAccessPoints(participantsNotFinish.org, sheetData, selectedExportFields, locale, " - 2")
        }

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given issue entitlements
     * @param result the issue entitlements set to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportIssueEntitlements(ArrayList<IssueEntitlement> result, Map<String, Object> selectedFields) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportIssueEntitlementFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale, null, null)

        List exportData = []

        int max = 500
        TitleInstancePackagePlatform.withSession { Session sess ->
            for(int offset = 0; offset < result.size(); offset+=max) {
                List allRows = []
                //this double structure is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
                    Set<IssueEntitlement> issueEntitlements = result.drop(offset).take(max)
                    issueEntitlements.each { IssueEntitlement entitlement ->
                        if(!entitlement.coverages && !entitlement.priceItems) {
                            allRows << entitlement
                        }
                        else if(entitlement.coverages.size() > 1){
                            entitlement.coverages.each { AbstractCoverage covStmt ->
                                allRows << covStmt
                            }
                        }
                        else {
                            allRows << entitlement
                        }
                    }

                allRows.each { rowData ->
                    _setIeRow(rowData, selectedExportFields, exportData)

                }
                log.debug("flushing after ${offset} ...")
                sess.flush()
            }
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('title.plural', null, locale)] = [titleRow: titles, columnData: exportData]

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given titles
     * @param result the titles set to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportTipps(ArrayList<TitleInstancePackagePlatform> result, Map<String, Object> selectedFields) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportTippFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale, null, null)

        List exportData = []

        int max = 500
        TitleInstancePackagePlatform.withSession { Session sess ->
            for(int offset = 0; offset < result.size(); offset+=max) {
                List allRows = []
                //this double structure is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
                Set<TitleInstancePackagePlatform> tipps = result.drop(offset).take(max)
                tipps.each { TitleInstancePackagePlatform tipp ->
                    if(!tipp.coverages && !tipp.priceItems) {
                        allRows << tipp
                    }
                    else if(tipp.coverages.size() > 1){
                        tipp.coverages.each { AbstractCoverage covStmt ->
                            allRows << covStmt
                        }
                    }
                    else {
                        allRows << tipp
                    }
                }

                allRows.each { rowData ->
                    _setTippRow(rowData, selectedExportFields, exportData)

                }
                log.debug("flushing after ${offset} ...")
                sess.flush()
            }
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('title.plural', null, locale)] = [titleRow: titles, columnData: exportData]

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Fills a row for the renewal export
     * @param participantResult the participant's result for the row
     * @param selectedFields the fields which should appear
     * @param renewalData the output list containing the rows
     * @param onlySubscription should only subscription-related parameters appear?
     * @param multiYearTermTwoSurvey should two years running times appear?
     * @param multiYearTermThreeSurvey should three years running times appear?
     */
    private void _setRenewalRow(Map participantResult, Map<String, Object> selectedFields, List renewalData, boolean onlySubscription, PropertyDefinition multiYearTermTwoSurvey, PropertyDefinition multiYearTermThreeSurvey){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('surveyProperty.')) {
                    if (onlySubscription) {
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                    } else {
                        Long id = Long.parseLong(fieldKey.split("\\.")[1])
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, PropertyDefinition.get(id))
                        row.add([field: participantResultProperty.getResult() ?: "", style: null])
                        row.add([field: participantResultProperty.comment ?: "", style: null])
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
                } else if (fieldKey == 'survey.costPeriod') {
                    String period = ""
                    if (participantResult.resultOfParticipation && participantResult.resultOfParticipation.costItem) {
                        period = participantResult.resultOfParticipation.costItem.startDate ? sdf.format(participantResult.resultOfParticipation.costItem.startDate) : ""
                        period = participantResult.resultOfParticipation.costItem.endDate ? period + " - " + sdf.format(participantResult.resultOfParticipation.costItem.endDate) : ""
                    }

                    row.add([field: period ?: '', style: null])
                }
                else if (fieldKey == 'survey.periodComment') {
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
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, participantResult.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else {
                    if (onlySubscription) {
                        if (fieldKey.startsWith('subscription.') || fieldKey.startsWith('participant.')) {
                            def fieldValue = _getFieldValue(participantResult, field, sdf)
                            row.add([field: fieldValue != null ? fieldValue : '', style: null])
                        } else {
                            row.add([field: '', style: null])
                        }

                    } else {
                        def fieldValue = _getFieldValue(participantResult, field, sdf)
                        row.add([field: fieldValue != null ? fieldValue : '', style: null])
                    }
                }
            }
        }
        renewalData.add(row)

    }

    /**
     * Fills a row for the subscription export
     * @param result the subscription to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     * @param localizedName the localised name of the property name
     * @param selectedCostItemElements the cost item elements to export
     * @param selectedCostItemFields the fields which should appear in the cost item export
     */
    private void _setSubRow(def result, Map<String, Object> selectedFields, List exportData, String localizedName, List<RefdataValue> selectedCostItemElements, Map selectedCostItemFields){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Org org
        Subscription subscription
        if(result instanceof Subscription) {
            subscription = result
            org = subscription.getSubscriber()
        }
        else {
            subscription = result.sub
            org = result.orgs
        }


        List<CostItem> costItems
        if(selectedCostItemElements){
           costItems = CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqual(subscription, selectedCostItemElements, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
        }

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, subscription)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }else if (fieldKey.startsWith('participantSubProperty.') || fieldKey.startsWith('subProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    String query = "select prop from SubscriptionProperty prop where (prop.owner = :sub and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :sub and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg) order by prop.type.${localizedName} asc"
                    List<SubscriptionProperty> subscriptionProperties = SubscriptionProperty.executeQuery(query,[sub:subscription, propertyDefs:[id], contextOrg: contextService.getOrg()])
                    if(subscriptionProperties){
                        row.add([field:  subscriptionProperties.collect { it.getValueInI10n()}.join(";") , style: null])
                    }else{
                        row.add([field:  '' , style: null])
                    }
                }
                else if (fieldKey == 'participantSubCostItem' || fieldKey == 'subCostItem') {
                    if(costItems){
                        costItems.each { CostItem costItem ->
                            row.add([field: costItem.costItemElement ? costItem.costItemElement.getI10n('value') : '', style: null])
                            selectedCostItemFields.each {
                                def fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                row.add([field: fieldValue != null ? fieldValue : '', style: null])
                            }
                        }
                    }else {
                            row.add([field:  '' , style: null])
                            selectedCostItemFields.each {
                                row.add([field:  '' , style: null])
                            }
                    }
                }
                else {
                        def fieldValue = _getFieldValue(result, field, sdf)
                        row.add([field: fieldValue != null ? fieldValue : '', style: null])
                    }
                }
            }
        exportData.add(row)

    }

    /**
     * Fills a row for cost item export
     * @param costItem the cost item to be exported
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the rows to export
     */
    private void _setCostItemRow(CostItem costItem, Map<String, Object> selectedFields, List exportData){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        Org org = costItem.sub ? costItem.sub.getSubscriber() : null

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, costItem.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else {
                    def fieldValue = _getFieldValue(costItem, field, sdf)
                    row.add([field: fieldValue != null ? fieldValue : '', style: null])
                }
            }
        }
        exportData.add(row)

    }

    /**
     * Fills a row for the organisation export
     * @param result the organisation to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     */
    private void _setOrgRow(Org result, Map<String, Object> selectedFields, List exportData){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('generalContact')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }else if (fieldKey.contains('billingContact')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey.contains('billingAdress')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey.contains('postAdress')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey.contains('altnames')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }
                else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }else if (fieldKey.startsWith('participantIdentifiers.') || fieldKey.startsWith('providerIdentifiers.')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }else if (fieldKey.startsWith('participantCustomerIdentifiers.') || fieldKey.startsWith('providerCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
                }else if (fieldKey.startsWith('participantProperty.') || fieldKey.startsWith('providerProperty.')) {
                    _setOrgFurtherInformation(result, row, fieldKey)
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

    /**
     * Fills a row for the survey evaluation
     * @param participantResult the evaluation of the participant to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the rows
     * @param selectedCostItemFields the cost item fields which should appear in export
     */
    private void _setSurveyEvaluationRow(Map participantResult, Map<String, Object> selectedFields, List exportData, Map selectedCostItemFields){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('surveyProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, PropertyDefinition.get(id))
                    row.add([field: participantResultProperty.getResult() ?: "", style: null])
                    row.add([field: participantResultProperty.comment ?: "", style: null])
                } else if (fieldKey == 'participant.generalContact') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, participantResult.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey)
                }else if (fieldKey == 'participantSurveyCostItem') {
                    if(participantResult.surveyCostItem){
                            selectedCostItemFields.each {
                                def fieldValue = _getFieldValue(participantResult.surveyCostItem, it.value.field.replace('costItem.', ''), sdf)
                                row.add([field: fieldValue != null ? fieldValue : '', style: null])
                            }
                    }else {
                        selectedCostItemFields.each {
                            row.add([field:  '' , style: null])
                        }
                    }
                }else {
                        def fieldValue = _getFieldValue(participantResult, field, sdf)
                        row.add([field: fieldValue != null ? fieldValue : '', style: null])
                }
            }
        }
        exportData.add(row)

    }

    /**
     * Fills a row for the issue entitlement export
     * @param result the issue entitlement to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     */
    private void _setIeRow(def result, Map<String, Object> selectedFields, List exportData){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        result = exportService.getIssueEntitlement(result)

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('issueEntitlementIdentifiers.')) {
                        if (result) {
                            Long id = Long.parseLong(fieldKey.split("\\.")[1])
                            List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.tipp = :tipp and ident.ns.id in (:namespaces)", [tipp: result.tipp, namespaces: [id]])
                            if (identifierList) {
                                row.add([field: identifierList.value.join(";"), style: null])
                            } else {
                                row.add([field: '', style: null])
                            }
                        } else {
                            row.add([field: '', style: null])
                        }
                }
                else if (fieldKey.contains('tipp.ddcs')) {
                    row.add([field: result.tipp.ddcs.collect {"${it.ddc.value} - ${it.ddc.getI10n("value")}"}.join(";"), style: null])
                }
                else if (fieldKey.contains('tipp.languages')) {
                    row.add([field: result.tipp.languages.collect {"${it.language.getI10n("value")}"}.join(";"), style: null])
                }else if (fieldKey.contains('perpetualAccessBySub')) {
                    row.add([field: result.perpetualAccessBySub ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value'), style: null])
                }
                else if (fieldKey.startsWith('coverage.')) {
                    AbstractCoverage covStmt = exportService.getCoverageStatement(result)
                    String coverageField = fieldKey.split("\\.")[1]

                    def fieldValue = covStmt ? _getFieldValue(covStmt, coverageField, sdf) : null
                    row.add([field: fieldValue != null ? fieldValue : '', style: null])
                }
                else if (fieldKey.contains('listPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.listPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('listPriceGBP')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_GBP }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.listPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('listPriceUSD')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_USD }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.listPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('localPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.localPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('localPriceGBP')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_GBP }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.localPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('localPriceUSD')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_USD }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.localPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else {
                    def fieldValue = _getFieldValue(result, field, sdf)
                    row.add([field: fieldValue != null ? fieldValue : '', style: null])
                }
            }
        }
        exportData.add(row)

    }

    /**
     * Fills a row for the title export
     * @param result the title to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     */
    private void _setTippRow(def result, Map<String, Object> selectedFields, List exportData){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        result = exportService.getTipp(result)

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('tippIdentifiers.')) {
                    if (result) {
                        Long id = Long.parseLong(fieldKey.split("\\.")[1])
                        List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.tipp = :tipp and ident.ns.id in (:namespaces)", [tipp: result.tipp, namespaces: [id]])
                        if (identifierList) {
                            row.add([field: identifierList.value.join(";"), style: null])
                        } else {
                            row.add([field: '', style: null])
                        }
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('ddcs')) {
                    row.add([field: result.tipp.ddcs.collect {"${it.ddc.value} - ${it.ddc.getI10n("value")}"}.join(";"), style: null])
                }
                else if (fieldKey.contains('languages')) {
                    row.add([field: result.tipp.languages.collect { "${it.language.getI10n("value")}" }.join(";"), style: null])
                }
                else if (fieldKey.startsWith('coverage.')) {
                    AbstractCoverage covStmt = exportService.getCoverageStatement(result)
                    String coverageField = fieldKey.split("\\.")[1]

                    def fieldValue = covStmt ? _getFieldValue(covStmt, coverageField, sdf) : null
                    row.add([field: fieldValue != null ? fieldValue : '', style: null])
                }
                else if (fieldKey.contains('listPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.listPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('listPriceGBP')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_GBP }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.listPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('listPriceUSD')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_USD }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.listPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('localPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.localPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('localPriceGBP')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_GBP }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.localPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else if (fieldKey.contains('localPriceUSD')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_USD }

                    if (priceItemsList) {
                        row.add([field: priceItemsList.collect {it.localPrice}.join(";"), style: null])
                    } else {
                        row.add([field: '', style: null])
                    }
                }
                else {
                    def fieldValue = _getFieldValue(result, field, sdf)
                    row.add([field: fieldValue != null ? fieldValue : '', style: null])
                }
            }
        }
        exportData.add(row)

    }


    /**
     * Returns the value for the given object
     * @param map the map whose field should be returned
     * @param field the field key to be displayed
     * @param sdf the {@link SimpleDateFormat} instance to format date fields
     * @return the string representation of the queried field
     */
    private def _getFieldValue(def map, String field, SimpleDateFormat sdf){
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

        if(fieldValue instanceof List){
            fieldValue = fieldValue.join('; ')
        }

        return fieldValue
    }

    /**
     * Exports access points of the given institutions
     * @param orgList the list of institutions whose access points should be exported
     * @param sheetData the worksheet containing the export
     * @param selectedExportFields the fields which should appear
     * @param locale the locale to use for message constants
     * @param sheetNameAddition an addition submitted to the sheet name
     * @return the updated export worksheet
     */
    private Map _exportAccessPoints(List<Org> orgList, Map sheetData, LinkedHashMap selectedExportFields, Locale locale, String sheetNameAddition) {

        Map export = [:]
        String sheetName = ''

        if ('participant.exportIPs' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportIPsOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }
        }

        if ('participant.exportProxys' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportProxysOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportEZProxys' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportEZProxysOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportShibboleths' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportShibbolethsOfOrgs(orgList, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        return sheetData
    }

    private void _setOrgFurtherInformation(Org org, List row, String fieldKey, Subscription subscription = null){

        if (fieldKey == 'participant.generalContact') {
            if (org) {
                RefdataValue generalContact = RDStore.PRS_FUNC_GENERAL_CONTACT_PRS
                List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.org = :org and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true", [org: org, functionTypes: [generalContact], type: RDStore.CCT_EMAIL])

                if (contactList) {
                    row.add([field: contactList.content.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        } else if (fieldKey == 'participant.billingContact') {
            if (org) {
                RefdataValue billingContact = RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS
                List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.org = :org and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = true", [org: org, functionTypes: [billingContact], type: RDStore.CCT_EMAIL])

                if (contactList) {
                    row.add([field: contactList.content.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        } else if (fieldKey == 'participant.billingAdress') {
            if (org) {
                RefdataValue billingAdress = RDStore.ADRESS_TYPE_BILLING
                LinkedHashSet<Address> adressList = org.addresses.findAll { Address adress -> adress.type.findAll { it == billingAdress } }

                if (adressList) {
                    row.add([field: adressList.collect { Address address -> _getAddress(address, org)}.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        } else if (fieldKey == 'participant.postAdress') {
            if (org) {

                RefdataValue postAdress = RDStore.ADRESS_TYPE_POSTAL
                LinkedHashSet<Address> adressList = org.addresses.findAll { Address adress -> adress.type.findAll { it == postAdress } }

                if (adressList) {
                    row.add([field: adressList.collect { Address address -> _getAddress(address, org)}.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        } else if (fieldKey.contains('altnames')) {
            if (org) {
                if(org.altnames)
                    row.add([field: org.altnames.collect { AlternativeName alt -> alt.name }.join('\n'), style: null])
                else row.add([field: '', style: null])
            }
            else {
                row.add([field: '', style: null])
            }
        } else if (fieldKey.startsWith('participantIdentifiers.') || fieldKey.startsWith('providerIdentifiers.')) {
            if (org) {
                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.id in (:namespaces) and ident.value != :unknown and ident.value != ''", [org: org, namespaces: [id], unknown: IdentifierNamespace.UNKNOWN])
                if (identifierList) {
                    row.add([field: identifierList.value.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }
        } else if (fieldKey.startsWith('participantCustomerIdentifiers.') || fieldKey.startsWith('providerCustomerIdentifiers.')) {
            if (org) {
                CustomerIdentifier customerIdentifier = CustomerIdentifier.findByCustomerAndPlatform(org, Platform.get(fieldKey.split("\\.")[1]))
                if (customerIdentifier) {
                    row.add([field: customerIdentifier.value, style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }
        } else if (fieldKey.startsWith('participantProperty.') || fieldKey.startsWith('providerProperty.')) {
            if (org) {

                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<OrgProperty> orgProperties = OrgProperty.executeQuery("select prop from OrgProperty prop where (prop.owner = :org and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :org and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg)", [org: org, propertyDefs: [id], contextOrg: contextService.getOrg()])
                if (orgProperties) {
                    row.add([field: orgProperties.collect { it.getValueInI10n() }.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }
        }else if (fieldKey == 'participant.readerNumbers') {
            if (org) {
                ReaderNumber readerNumberStudents
                ReaderNumber readerNumberStaff
                ReaderNumber readerNumberFTE

                //ReaderNumber readerNumberPeoplewithDueDate = ReaderNumber.findByReferenceGroupAndOrgAndDueDateIsNotNull(RDStore.READER_NUMBER_PEOPLE.value_de, org, [sort: 'dueDate', order: 'desc'])
                ReaderNumber readerNumberStaffwithDueDate = ReaderNumber.findByReferenceGroupAndOrgAndDueDateIsNotNull(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, org, [sort: 'dueDate', order: 'desc'])
                if(readerNumberStaffwithDueDate){
                    row.add([field: '', style: null])
                    row.add([field: '', style: null])
                    row.add([field: readerNumberStaffwithDueDate.value, style: null])
                    row.add([field: '', style: null])
                }else{
                    RefdataValue currentSemester = RefdataValue.getCurrentSemester()

                    readerNumberStudents = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_STUDENTS, org, currentSemester)
                    readerNumberStaff = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, org, currentSemester)
                    readerNumberFTE = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_FTE, org, currentSemester)

                    if(readerNumberStudents || readerNumberStaff || readerNumberFTE){
                        row.add([field: currentSemester.getI10n('value'), style: null])
                        row.add([field: readerNumberStudents ? readerNumberStudents.value : '', style: null])
                        row.add([field: readerNumberStaff ? readerNumberStaff.value : '', style: null])
                        row.add([field: readerNumberFTE ? readerNumberFTE.value : '', style: null])
                    }else{
                        boolean nextSemester = false

                        List<RefdataValue> refdataValueList = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.SEMESTER).reverse()
                        for(int count = 0; count < refdataValueList.size(); count = count + 1) {
                            if (refdataValueList[count] == currentSemester) {
                                nextSemester = true
                            }
                            if (nextSemester) {
                                readerNumberStaff = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, org, refdataValueList[count])
                                readerNumberStudents = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_STUDENTS, org, refdataValueList[count])
                                readerNumberFTE = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_FTE, org, refdataValueList[count])
                                if (readerNumberStudents || readerNumberStaff || readerNumberFTE) {
                                    row.add([field: refdataValueList[count].getI10n('value'), style: null])
                                    row.add([field: readerNumberStudents ? readerNumberStudents.value : '', style: null])
                                    row.add([field: readerNumberStaff ? readerNumberStaff.value : '', style: null])
                                    row.add([field: readerNumberFTE ? readerNumberFTE.value : '', style: null])
                                    break
                                }
                            }
                        }
                        if(!readerNumberStudents && !readerNumberStaff && !readerNumberFTE){
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                            row.add([field: '', style: null])
                        }
                    }
                }

                ReaderNumber readerNumberPeople = ReaderNumber.findByReferenceGroupAndOrg(RDStore.READER_NUMBER_PEOPLE, org, [sort: 'dueDate', order: 'desc'])
                ReaderNumber readerNumberUser = ReaderNumber.findByReferenceGroupAndOrg(RDStore.READER_NUMBER_USER, org, [sort: 'dueDate', order: 'desc'])


                row.add([field: readerNumberPeople ? readerNumberPeople.value : '', style: null])
                row.add([field: readerNumberUser ? readerNumberUser.value : '', style: null])

                BigDecimal sum = 0
                if(readerNumberStudents){
                    sum = sum + (readerNumberStudents.value != null ? readerNumberStudents.value : 0)
                }
                if(readerNumberStaff){
                    sum = sum + (readerNumberStaff.value != null ? readerNumberStaff.value : 0)
                }
                if(readerNumberFTE){
                    sum = sum + (readerNumberFTE.value != null ? readerNumberFTE.value : 0)
                }
                if(readerNumberPeople){
                    sum = sum + (readerNumberPeople.value != null ? readerNumberPeople.value : 0)
                }
                if(readerNumberUser){
                    sum = sum + (readerNumberUser.value != null ? readerNumberUser.value : 0)
                }

                if(readerNumberStaffwithDueDate){
                    sum = sum + (readerNumberStaffwithDueDate.value != null ? readerNumberStaffwithDueDate.value : 0)
                }

                row.add([field: sum, style: null])

                String note = readerNumberStudents ? readerNumberStudents.dateGroupNote : (readerNumberPeople ? readerNumberPeople.dateGroupNote : (readerNumberUser ? readerNumberUser.dateGroupNote : (readerNumberStaffwithDueDate ? readerNumberStaffwithDueDate.dateGroupNote : '')))

                row.add([field: note, style: null])

            } else {
                row.add([field: '', style: null])
                row.add([field: '', style: null])
                row.add([field: '', style: null])
                row.add([field: '', style: null])
                row.add([field: '', style: null])
                row.add([field: '', style: null])
                row.add([field: '', style: null])
            }
        }
    }

    private List _exportTitles(Map<String, Object> selectedExportFields, Locale locale, Map selectedCostItemFields = null, Integer maxCostItemsElements = null){
        List titles = []

        String localizedValue = LocaleUtils.getLocalizedAttributeName('value')

        selectedExportFields.keySet().each {String fieldKey ->
            Map fields = selectedExportFields.get(fieldKey)
            if(!fields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    titles << RDStore.PRS_FUNC_GENERAL_CONTACT_PRS."${localizedValue}"
                }else if (fieldKey == 'participant.billingContact') {
                    titles << RDStore.PRS_FUNC_FUNC_BILLING_ADDRESS."${localizedValue}"
                }
                else if (fieldKey == 'participant.billingAdress') {
                    titles << RDStore.ADRESS_TYPE_BILLING."${localizedValue}"
                }else if (fieldKey == 'participant.postAdress') {
                    titles << RDStore.ADRESS_TYPE_POSTAL."${localizedValue}"
                }else if (fieldKey == 'participant.readerNumbers') {
                    titles << messageSource.getMessage('readerNumber.semester.label', null, locale)
                    titles << RDStore.READER_NUMBER_STUDENTS."${localizedValue}"
                    titles << RDStore.READER_NUMBER_SCIENTIFIC_STAFF."${localizedValue}"
                    titles << RDStore.READER_NUMBER_FTE."${localizedValue}"
                    titles << RDStore.READER_NUMBER_PEOPLE."${localizedValue}"
                    titles << RDStore.READER_NUMBER_USER."${localizedValue}"
                    titles << messageSource.getMessage('readerNumber.sum.label', null, locale)
                    titles << messageSource.getMessage('readerNumber.note.label', null, locale)


                }
                else if (fieldKey == 'participantSubCostItem' || fieldKey == 'subCostItem') {
                            for(int i = 0; i < maxCostItemsElements; i++) {
                                titles << messageSource.getMessage("financials.costItemElement", null, locale)
                                selectedCostItemFields.each {
                                    titles << (it.value.message ? messageSource.getMessage("${it.value.message}", null, locale) : it.value.label)
                                }
                            }
                }
                else if (fieldKey == 'participantSurveyCostItem') {
                        selectedCostItemFields.each {
                            titles << (it.value.message ? messageSource.getMessage("${it.value.message}", null, locale) : it.value.label)
                        }
                }
                else {
                    titles << (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label)
                    if (fieldKey.startsWith('surveyProperty.')) {
                        titles << (messageSource.getMessage('surveyResult.participantComment', null, locale) + " " + messageSource.getMessage('renewalEvaluation.exportRenewal.to', null, locale) + " " + (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label))
                    }
                }
            }
        }

        titles
    }

    /**
     * Formats the given organisations address
     * @param address the address to format
     * @param org the organisation to which the address is belonging
     * @return the formatted address string
     */
    private String _getAddress(Address address, Org org){
        String addr= ""

        if(address.name){
            addr = address.name
        }else {
            addr = org.name
        }

        if(address.additionFirst || address.additionSecond) {
            addr += ', '
            if (address.additionFirst) {
                addr += address.additionFirst + ' '
            }
            if (address.additionSecond) {
                addr += address.additionSecond + ' '
            }
        }

        if(address.street_1 || address.street_2) {
            addr += ', '
            if (address.street_1) {
                addr += address.street_1 + ' '
            }
            if (address.street_2) {
                addr += address.street_2 + ' '
            }
        }

        if(address.zipcode || address.city) {
            addr += ', '
            if (address.zipcode) {
                addr += address.zipcode + ' '
            }
            if (address.city) {
                addr += address.city + ' '
            }
        }

        if (address.region) {
                addr += ', ' + address.region.getI10n('value') + ' '
        }

        if (address.country) {
                addr += ', ' + address.country.getI10n('value') + ' '
        }

        if(address.pob){
            addr += ', ' + address.pob + ' '
        }

        if(address.pobZipcode){
            addr += ', ' + address.pobZipcode + ' '
        }

        if(address.pobCity){
            addr += ', ' + address.pobCity + ' '
        }

        return addr
    }
}
