package de.laser


import de.laser.base.AbstractCoverage
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.remote.ApiSource
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
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.springframework.context.MessageSource
import org.hibernate.Session

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.time.Year

/**
 * This service handles the export configuration: via a modal, the user checks the settings which should be exported and
 * after submit, the export will contain only the selected fields. This procedure is called ClickMe-Export. For every place
 * where ClickMe-exports are being offered a static configuration map is being defined where the settings are being defined
 */
@Transactional
class ExportClickMeService {

    AccessPointService accessPointService
    ContextService contextService
    CustomerTypeService customerTypeService
    ExportService exportService
    FinanceService financeService
    GokbService gokbService

    MessageSource messageSource

    static enum FORMAT {
        XLS, CSV, TSV
    }

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
                                    'participant.exportMailDomains' : [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
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
                                    'subscription.referenceYear'                : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                                    'subscription.status'                       : [field: 'sub.status', label: 'Status', message: 'subscription.status.label'],
                                    'subscription.kind'                         : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label'],
                                    'subscription.form'                         : [field: 'sub.form', label: 'Form', message: 'subscription.form.label'],
                                    'subscription.resource'                     : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                                    'subscription.hasPerpetualAccess'           : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                                    'subscription.hasPublishComponent'          : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                                    'subscription.holdingSelection'             : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
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
                            'subscription.referenceYear'                : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label', defaultChecked: 'true'],
                            'subscription.status'                       : [field: 'sub.status', label: 'Status', message: 'subscription.status.label', defaultChecked: 'true'],
                            'subscription.kind'                         : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label', defaultChecked: 'true'],
                            'subscription.form'                         : [field: 'sub.form', label: 'Form', message: 'subscription.form.label', defaultChecked: 'true'],
                            'subscription.resource'                     : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.holdingSelection'             : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
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
                            /*
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
                            'participant.eInvoice'          : [field: 'orgs.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'participant.eInvoicePortal'    : [field: 'orgs.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'participant.linkResolverBaseURL'    : [field: 'orgs.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers']
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
                            'participant.exportMailDomains' : [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
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
                            'subscription.referenceYear'                : [field: 'referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label', defaultChecked: 'true'],
                            //'subscription.isAutomaticRenewAnnually'     : [field: 'isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'], //to be shown for PRO users only!
                            'subscription.status'                       : [field: 'status', label: 'Status', message: 'subscription.status.label', defaultChecked: 'true'],
                            'subscription.kind'                         : [field: 'kind', label: 'Kind', message: 'subscription.kind.label', defaultChecked: 'true'],
                            'subscription.form'                         : [field: 'form', label: 'Form', message: 'subscription.form.label', defaultChecked: 'true'],
                            'subscription.resource'                     : [field: 'resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.holdingSelection'             : [field: 'holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
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

            packages : [
                    label: 'Packages',
                    message: 'subscription.packages.label',
                    fields: [
                            'package.name' : [field: 'package.name', label: 'Name (Package)', message: 'exportClickMe.package.name'],
                            'platform.name' : [field: 'package.nominalPlatform.name', label: 'Name (Platform)', message: 'exportClickMe.platform.name']
                    ]
            ],

            institutions: [
                    label: 'Consortium members',
                    message: 'consortium.member.plural',
                    fields: ['memberCount': [field: null, label: 'Count', message: 'default.count.label'],
                             'multiYearCount': [field: null, label: 'Count multi-year', message: 'default.count.multiYear.label']
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
                    label: 'Public properties',
                    message: 'default.properties',
                    fields: [:]
            ],

            mySubProperties : [
                    label: 'My properties',
                    message: 'default.properties.my',
                    fields: [:]
            ],

            subCostItems : [
                    label: 'Cost Items',
                    message: 'subscription.costItems.label',
                    subTabs: [],
                    fields: [
                            'costItemsElements' : [:],
                    /*
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
                            'costItem.orderNumber'                      : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number']
                    */
                    ]
            ]
    ]

    static Map<String, Object> EXPORT_CONSORTIA_CONFIG = [
            consortium : [
                    label: 'Consortium',
                    message: 'consortium.label',
                    fields: [
                            'consortium.sortname'          : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'consortium.name'              : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'consortium.funderType'        : [field: 'funderType', label: 'Funder Type', message: 'org.funderType.label'],
                            'consortium.funderHskType'     : [field: 'funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                            'consortium.libraryType'       : [field: 'libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                            /*
                            'consortium.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'consortium.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'consortium.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'consortium.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            */
                            'consortium.eInvoice'          : [field: 'eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'consortium.eInvoicePortal'    : [field: 'eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'consortium.linkResolverBaseURL'    : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                            'consortium.uuid'              : [field: 'globalUID', label: 'Laser-UUID',  message: null],
                    ]
            ],
            consortiumIdentifiers : [
                    label: 'Identifiers',
                    message: 'exportClickMe.consortiumIdentifiers',
                    fields: [:]
            ],
            consortiumProperties : [
                    label: 'Properties',
                    message: 'default.properties',
                    fields: [:]
            ],
            myConsortiumProperties : [
                    label: 'Properties',
                    message: 'default.properties.my',
                    fields: [:]
            ]
            //customer identifiers: sense for consortia?
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
                            'subscription.referenceYear'                : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                            'subscription.isAutomaticRenewAnnually'     : [field: 'sub.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'],
                            'subscription.status'                       : [field: 'sub.status', label: 'Status', message: 'subscription.status.label'],
                            'subscription.kind'                         : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label'],
                            'subscription.form'                         : [field: 'sub.form', label: 'Form', message: 'subscription.form.label'],
                            'subscription.resource'                     : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.holdingSelection'             : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
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
                            /*
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
                            'participant.eInvoice'          : [field: 'eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                            'participant.eInvoicePortal'    : [field: 'eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                            'participant.linkResolverBaseURL'    : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                            'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
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
                            'participant.exportMailDomains' : [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
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
            participantContacts : [
                    label: 'Contacts',
                    message: 'org.contacts.label',
                    fields: [:]
            ],
            participantProperties : [
                    label: 'Properties',
                    message: 'default.properties',
                    fields: [:]
            ],
            myParticipantProperties : [
                    label: 'Properties',
                    message: 'default.properties.my',
                    fields: [:]
            ]
    ]

    static Map<String, Object> EXPORT_PROVIDER_CONFIG = [
            provider : [
                    label: 'Provider',
                    message: 'default.ProviderAgency.singular',
                    fields: [
                            'provider.name'                  : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                            'provider.sortname'              : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                            'provider.altnames'              : [field: 'altnames', label: 'Alternative names', message: 'org.altname.label', defaultChecked: 'true' ],
                            'provider.status'                : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: true],
                            'provider.homepage'              : [field: 'homepage', label: 'Homepage URL', message: 'org.homepage.label', defaultChecked: true],
                            'provider.metadataDownloaderURL' : [field: 'metadataDownloaderURL', label: 'Metadata Downloader URL', message: 'org.metadataDownloaderURL.label', defaultChecked: true],
                            'provider.kbartDownloaderURL'    : [field: 'kbartDownloaderURL', label: 'KBART Downloader URL', message: 'org.KBARTDownloaderURL.label', defaultChecked: true],
                            'provider.roles'                 : [field: 'roles', label: 'Roles', message: 'org.orgRole.label', defaultChecked: true]
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
            providerContacts : [
                    label: 'Contacts',
                    message: 'org.contacts.label',
                    fields: [:]
            ],
            providerProperties : [
                    label: 'Properties',
                    message: 'default.properties',
                    fields: [:]
            ],
            myProviderProperties : [
                    label: 'Properties',
                    message: 'default.properties.my',
                    fields: [:]
            ]
    ]

    static Map<String, Object> EXPORT_ADDRESS_CONFIG = [
            contact : [
                    label: 'Contact',
                    message: 'contact.label',
                    fields: [
                            'organisation': [field: 'organisation', label: 'Organisation', message: 'address.org.label', defaultChecked: 'true'],
                            'receiver' : [field: 'receiver', label: 'Receiver', message: 'address.receiver.label', defaultChecked: 'true'],
                            'language': [field: 'language', label: 'Language', message: 'contact.language.label', defaultChecked: 'true'],
                            'email': [field: 'email', label: 'Email', message: 'contact.icon.label.email', defaultChecked: 'true'],
                            'fax': [field: 'fax', label: 'Fax', message: 'contact.icon.label.fax', defaultChecked: 'true'],
                            'url': [field: 'url', label: 'URL', message: 'contact.icon.label.url', defaultChecked: 'true'],
                            'phone': [field: 'phone', label: 'Phone', message: 'contact.icon.label.phone', defaultChecked: 'true']
                    ]
            ],
            address : [
                    label: 'Address',
                    message: 'address.label',
                    fields: [
                            'organisation': [field: 'organisation', label: 'Organisation', message: 'address.org.label'],
                            'receiver' : [field: 'receiver', label: 'Receiver', message: 'address.receiver.label'],
                            'additionFirst': [field: 'additionFirst', label: 'First Addition', message: 'address.additionFirst.label'],
                            'additionSecond': [field: 'additionSecond', label: 'Second Addition', message: 'address.additionSecond.label'],
                            'street_1': [field: 'street_1', label: 'Street', message: 'address.street_1.label'],
                            'street_2': [field: 'street_2', label: 'Number', message: 'address.street_2.label'],
                            'zipcode': [field: 'zipcode', label: 'Postcode', message: 'address.zipcode.label'],
                            'city': [field: 'city', label: 'City', message: 'address.city.label'],
                            'pob': [field: 'pob', label: 'Postal box', message: 'address.pob.label'],
                            'pobZipcode': [field: 'pobZipcode', label: 'Postal box zip code', message: 'address.pobZipcode.label'],
                            'pobCity': [field: 'pobCity', label: 'Postal box city', message: 'address.pobCity.label'],
                            'country': [field: 'country', label: 'Country', message: 'address.country.label'],
                            'region': [field: 'region', label: 'Region', message: 'address.region.label'],
                    ]
            ]
    ]

    static Map<String, Object> EXPORT_ADDRESS_FILTER = [
            function : [
                    label: 'Function',
                    message: 'person.function.label',
                    fields: [:]
            ],
            position : [
                    label: 'Position',
                    message: 'person.position.label',
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
                            /*
                            'participant.generalContact'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participant.billingContact'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
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
                            'subscription.referenceYear'                : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                            'subscription.status'                       : [field: 'sub.status', label: 'Status', message: 'subscription.status.label'],
                            'subscription.kind'                         : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label'],
                            'subscription.form'                         : [field: 'sub.form', label: 'Form', message: 'subscription.form.label'],
                            'subscription.resource'                     : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.holdingSelection'             : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
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
                            'subscription.referenceYear'                : [field: 'subscription.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                            'subscription.isAutomaticRenewAnnually'     : [field: 'subscription.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'],
                            'subscription.status'                       : [field: 'subscription.status', label: 'Status', message: 'subscription.status.label'],
                            'subscription.kind'                         : [field: 'subscription.kind', label: 'Kind', message: 'subscription.kind.label'],
                            'subscription.form'                         : [field: 'subscription.form', label: 'Form', message: 'subscription.form.label'],
                            'subscription.resource'                     : [field: 'subscription.resource', label: 'Resource', message: 'subscription.resource.label'],
                            'subscription.hasPerpetualAccess'           : [field: 'subscription.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                            'subscription.hasPublishComponent'          : [field: 'subscription.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                            'subscription.holdingSelection'             : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
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

        if(!(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in surveyConfig.surveyProperties.surveyProperty.id) && !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in surveyConfig.surveyProperties.surveyProperty.id) && !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in surveyConfig.surveyProperties.surveyProperty.id) && !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in surveyConfig.surveyProperties.surveyProperty.id)){
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

        if(!(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_2.id in surveyConfig.surveyProperties.surveyProperty.id) &&  !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_3.id in surveyConfig.surveyProperties.surveyProperty.id) &&  !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_4.id in surveyConfig.surveyProperties.surveyProperty.id) &&  !(PropertyStore.SURVEY_PROPERTY_MULTI_YEAR_5.id in surveyConfig.surveyProperties.surveyProperty.id)){
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
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: institution]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: institution]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: institution]))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.value, [field: null, label: contactType.getI10n('value')])
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
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription) and ci.value != null order by plat.name',[subscription:subscription]).each { Platform plat ->
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

        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: institution]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: institution]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: institution]))

        contactTypes.each { RefdataValue contactType ->
            fields.participant.fields.put("participantContact.${contactType.value}", [field: null, label: contactType.getI10n('value')])
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

        EXPORT_SUBSCRIPTION_CONFIG.keySet().each { String key ->
            if(key == 'institutions') {
                if(customerTypeService.isConsortium(institution.getCustomerType())) {
                    EXPORT_SUBSCRIPTION_CONFIG.get(key).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }
            }
            else {
                EXPORT_SUBSCRIPTION_CONFIG.get(key).fields.each {
                    exportFields.put(it.key, it.value)
                }
            }
        }

        if(institution.getCustomerType() == CustomerTypeService.ORG_INST_PRO) {
            exportFields.put('subscription.isAutomaticRenewAnnually', [field: 'isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
        }

        //determine field configuration based on customer type
        Set<String> fieldKeyPrefixes = []
        switch(institution.getCustomerType()) {
        //cases one to three
            case CustomerTypeService.ORG_CONSORTIUM_BASIC:
            case CustomerTypeService.ORG_CONSORTIUM_PRO: fieldKeyPrefixes.addAll(['own', 'cons'])
                break
                //cases four and five
            case CustomerTypeService.ORG_INST_PRO: fieldKeyPrefixes.addAll(['own', 'subscr'])
                break
                //cases six: basic member
            case CustomerTypeService.ORG_INST_BASIC: fieldKeyPrefixes << 'subscr'
                break
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        String consortiaFilter = ''
        if(institution.isCustomerType_Consortium())
            consortiaFilter = ' and s.instanceOf = null '
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx '+consortiaFilter+')', [ctx: institution]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }
        IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_PACKAGE, [sort: 'ns']).each { IdentifierNamespace idns ->
            exportFields.put("packageIdentifiers."+idns.id, [field: null, label: idns.ns + "(${messageSource.getMessage('package', null, locale)})"])
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:institution,availableTypes:[PropertyDefinition.SUB_PROP]])


        propList.each { PropertyDefinition propertyDefinition ->
            exportFields.put("subProperty." + propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == institution.id)])
        }

        Set<RefdataValue> elements = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.COST_ITEM_ELEMENT)
        elements.each { RefdataValue refdataValue ->
            fieldKeyPrefixes.each { String fieldKeyPrefix ->
                exportFields.put("subCostItem." +fieldKeyPrefix+"."+ refdataValue.id, [field: null, label: refdataValue."${localizedValue}"])
            }
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
        if(!customerTypeService.isConsortium(institution.getCustomerType())) {
            fields.remove('institutions')
        }
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
        fields.mySubProperties.fields.clear()
        fields.subCostItems.fields.costItemsElements.clear()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        String consortiaFilter = ''
        if(institution.isCustomerType_Consortium())
            consortiaFilter = ' and s.instanceOf = null '
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx '+consortiaFilter+') order by plat.name', [ctx:institution]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_PACKAGE, [sort: 'ns']).each { IdentifierNamespace idns ->
            fields.packages.fields << ["packageIdentifiers.${idns.id}":[field: null, label: idns.ns]]
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc", [ctx:institution,availableTypes:[PropertyDefinition.SUB_PROP]])

        propList.each { PropertyDefinition propertyDefinition ->
            //the proxies again ...
            if(propertyDefinition.tenant?.id == institution.id)
                fields.mySubProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
            else
                fields.subProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
        }

        //determine tabs configuration based on customer type
        switch(institution.getCustomerType()) {
            //cases one to three
            case CustomerTypeService.ORG_CONSORTIUM_BASIC:
            case CustomerTypeService.ORG_CONSORTIUM_PRO:
                fields.subCostItems.subTabs = [[view: 'own', label: 'financials.tab.ownCosts'],[view: 'cons', label: 'financials.tab.consCosts']]
                fields.subCostItems.subTabActive = 'cons'
                break
                //cases four and five
            case CustomerTypeService.ORG_INST_PRO:
                fields.subCostItems.subTabs = [[view: 'own', label: 'financials.tab.ownCosts'],[view: 'subscr', label: 'financials.tab.subscrCosts']]
                fields.subCostItems.subTabActive = 'subscr'
                break
                //cases six: basic member
            case CustomerTypeService.ORG_INST_BASIC:
                fields.subCostItems.subTabs = [[view: 'subscr', label: 'financials.tab.subscrCosts']]
                fields.subCostItems.subTabActive = 'subscr'
                break
        }

        Set<RefdataValue> elements = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.COST_ITEM_ELEMENT)
        elements.each { RefdataValue refdataValue ->
            fields.subCostItems.subTabs.view.each { String fieldKeyPrefix ->
                fields.subCostItems.fields << [("subCostItem." +fieldKeyPrefix+"."+ refdataValue.id): [field: null, label: refdataValue."${localizedValue}"]]
            }
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

        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))

        switch(config) {
            case 'consortium':
                EXPORT_CONSORTIA_CONFIG.keySet().each {
                    EXPORT_CONSORTIA_CONFIG.get(it).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }

                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    exportFields.put("consortiumIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
                }

                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    exportFields.put("consortiumProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
                }
                contactTypes.each { RefdataValue contactType ->
                    exportFields.put("consortiumContact."+contactType.value, [field: null, label: contactType.getI10n('value')])
                }
                break
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
                    exportFields.put("participantProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
                }
                contactTypes.each { RefdataValue contactType ->
                    exportFields.put("participantContact."+contactType.value, [field: null, label: contactType.getI10n('value')])
                }
                break
            case 'provider':
                EXPORT_PROVIDER_CONFIG.keySet().each {
                    EXPORT_PROVIDER_CONFIG.get(it).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }

                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_PROVIDER_NS).each {
                    exportFields.put("providerIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
                }

                Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
                    exportFields.put("providerCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
                }

                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    exportFields.put("providerProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
                }
                contactTypes.each { RefdataValue contactType ->
                    exportFields.put("providerContact."+contactType.value, [field: null, label: contactType.getI10n('value')])
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

        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields, contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))

        switch(orgType) {
            case 'consortium': fields = EXPORT_CONSORTIA_CONFIG as Map
                fields.consortiumIdentifiers.fields.clear()
                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    fields.consortiumIdentifiers.fields << ["consortiumIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
                }
                fields.consortiumProperties.fields.clear()
                fields.myConsortiumProperties.fields.clear()
                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextOrg).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    if(propertyDefinition.tenant?.id == contextOrg.id)
                        fields.myConsortiumProperties.fields << ["consortiumProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
                    else
                        fields.consortiumProperties.fields << ["consortiumProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
                }
                contactTypes.each { RefdataValue contactType ->
                    fields.consortium.fields.put("consortiumContact.${contactType.value}", [field: null, label: contactType.getI10n('value')])
                }
                break
            case 'institution': fields = EXPORT_ORG_CONFIG as Map
                fields.participantIdentifiers.fields.clear()
                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
                }
                fields.participantIdentifiers.fields << ['participant.uuid':[field: 'globalUID', label: 'Laser-UUID',  message: null]]
                fields.participantCustomerIdentifiers.fields.clear()
                Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx) order by plat.name', contextParams).each { Platform plat ->
                    fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
                }
                fields.participantProperties.fields.clear()
                fields.myParticipantProperties.fields.clear()
                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextOrg).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    if(propertyDefinition.tenant?.id == contextOrg.id)
                        fields.myParticipantProperties.fields << ["participantProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
                    else
                        fields.participantProperties.fields << ["participantProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
                }
                fields.participantContacts.fields.clear()
                contactTypes.each { RefdataValue contactType ->
                    fields.participantContacts.fields.put("participantContact.${contactType.value}", [field: null, label: contactType.getI10n('value')])
                }
                break
            case 'provider': fields = EXPORT_PROVIDER_CONFIG as Map
                fields.providerIdentifiers.fields.clear()
                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_PROVIDER_NS, [sort: 'ns']).each {
                    fields.providerIdentifiers.fields << ["providerIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
                }
                fields.providerIdentifiers.fields << ['provider.uuid':[field: 'globalUID', label: 'Laser-UUID',  message: null]]
                fields.providerCustomerIdentifiers.fields.clear()
                Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx) order by plat.name', contextParams).each { Platform plat ->
                    fields.providerCustomerIdentifiers.fields << ["providerCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
                }
                fields.providerProperties.fields.clear()
                fields.myProviderProperties.fields.clear()
                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextOrg).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    if(propertyDefinition.tenant?.id == contextOrg.id)
                        fields.myProviderProperties.fields << ["providerProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
                    else
                        fields.providerProperties.fields << ["providerProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
                }
                fields.providerContacts.fields.clear()
                contactTypes.each { RefdataValue contactType ->
                    fields.providerContacts.fields.put("providerContact.${contactType.value}",[field: null, label: contactType.getI10n('value')])
                }
                break
            default: fields = [:]
                break
        }

        fields
    }

    Map<String, Object> getExportAddressFields() {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        EXPORT_ADDRESS_CONFIG.keySet().each { String k ->
            EXPORT_ADDRESS_CONFIG.get(k).fields.each { key, value ->
                exportFields.put(key, value)
            }
        }

        /*
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
        */

        exportFields
    }

    Map<String, Object> getExportAddressFieldsForUI() {

        Map<String, Object> fields = EXPORT_ADDRESS_CONFIG as Map, filterFields = EXPORT_ADDRESS_FILTER as Map
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        Org institution = contextService.getOrg()
        String i10nAttr = LocaleUtils.getLocalizedAttributeName('value')
        Set<RefdataValue> functionTypes = PersonRole.executeQuery('select ft from PersonRole pr join pr.functionType ft join pr.prs p where p.tenant = :contextOrg order by ft.'+i10nAttr, [contextOrg: institution])
        Set<RefdataValue> positionTypes = PersonRole.executeQuery('select pt from PersonRole pr join pr.positionType pt join pr.prs p where p.tenant = :contextOrg order by pt.'+i10nAttr, [contextOrg: institution])

        functionTypes.each { RefdataValue functionType ->
            filterFields.function.fields.put('function.'+functionType.id, [field: null, label: functionType.getI10n('value')])
        }
        positionTypes.each { RefdataValue positionType ->
            filterFields.position.fields.put('position.'+positionType.id, [field: null, label: positionType.getI10n('value')])
        }

        /*
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
        */

        [exportFields: fields, filterFields: filterFields]
    }

    /**
     * Gets the survey evaluation fields for export
     * @param surveyConfig the survey whose evaluation should be exported
     * @return the configuration map for the survey evaluation export
     */
    Map<String, Object> getExportSurveyEvaluationFields(SurveyConfig surveyConfig) {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Org contextOrg = contextService.getOrg()

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
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.value, [field: null, label: contactType.getI10n('value')])
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
        Org contextOrg = contextService.getOrg()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (:participants) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [participants: surveyConfig.orgs.org, subscription: surveyConfig.subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where p.tenant = :ctx', [ctx: contextOrg]))
        contactTypes.each { RefdataValue contactType ->
            fields.participant.fields.put("participantContact.${contactType.value}", [field: null, label: contactType.getI10n('value')])
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
                                'subscription.holdingSelection'             : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
                                'subscription.uuid'                         : [field: 'sub.globalUID', label: 'LAS:eR-UUID', message: null]
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
    def exportRenewalResult(Map renewalResult, Map<String, Object> selectedFields, FORMAT format) {
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

        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.continuetoSubscription.label', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})", 'positive')])

        renewalResult.orgsContinuetoSubscription.sort { it.participant.sortname }.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.withMultiYearTermSub.label', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})", 'positive')])


        renewalResult.orgsWithMultiYearTermSub.sort{it.getSubscriber().sortname}.each { sub ->

            _setRenewalRow([participant: sub.getSubscriber(), sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, multiYearTermFourSurvey: renewalResult.multiYearTermFourSurvey, multiYearTermFiveSurvey: renewalResult.multiYearTermFiveSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)

        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.orgsWithParticipationInParentSuccessor.label', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})", 'positive')])


        renewalResult.orgsWithParticipationInParentSuccessor.each { sub ->
            sub.getAllSubscribers().sort{it.sortname}.each{ subscriberOrg ->
                _setRenewalRow([participant: subscriberOrg, sub: sub, multiYearTermTwoSurvey: renewalResult.multiYearTermTwoSurvey, multiYearTermThreeSurvey: renewalResult.multiYearTermThreeSurvey, multiYearTermFourSurvey: renewalResult.multiYearTermFourSurvey, multiYearTermFiveSurvey: renewalResult.multiYearTermFiveSurvey, properties: renewalResult.properties], selectedExportFields, renewalData, true, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)
            }
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.newOrgstoSubscription.label', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})", 'positive')])


        renewalResult.newOrgsContinuetoSubscription.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.withTermination.label', null, locale) + " (${renewalResult.orgsWithTermination.size()})", 'negative')])


        renewalResult.orgsWithTermination.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('surveys.tabs.termination', null, locale) + " (${renewalResult.orgsWithoutResult.size()})", 'negative')])


        renewalResult.orgsWithoutResult.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.orgInsertedItself.label', null, locale) + " (${renewalResult.orgInsertedItself.size()})", 'negative')])


        renewalResult.orgInsertedItself.sort{it.participant.sortname}.each { participantResult ->
            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, renewalResult.multiYearTermTwoSurvey, renewalResult.multiYearTermThreeSurvey, renewalResult.multiYearTermFourSurvey, renewalResult.multiYearTermFiveSurvey, format)
        }


        Map sheetData = [:]
        sheetData[messageSource.getMessage('renewalexport.renewals', null, locale)] = [titleRow: titles, columnData: renewalData]

        if (renewalResult.orgsContinuetoSubscription) {
            sheetData = _exportAccessPoints(renewalResult.orgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 1", format)
        }

        if (renewalResult.orgsWithMultiYearTermSub) {
            sheetData = _exportAccessPoints(renewalResult.orgsWithMultiYearTermSub.collect { it.getAllSubscribers() }, sheetData, selectedExportFields, locale, " - 2", format)
        }

        if (renewalResult.orgsWithParticipationInParentSuccessor) {
            sheetData = _exportAccessPoints(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getAllSubscribers() }, sheetData, selectedExportFields, locale, " - 3", format)
        }

        if (renewalResult.newOrgsContinuetoSubscription) {
            sheetData = _exportAccessPoints(renewalResult.newOrgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 4", format)
        }

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(sheetData.titleRow, sheetData.columnData, ',')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(sheetData.titleRow, sheetData.columnData, '\t')
        }
    }

    /**
     * Exports the selected fields about the members of the given subscription for the given institution
     * @param result the subscription members to export
     * @param selectedFields the fields which should appear
     * @param subscription the subscription as reference for the fields
     * @param institution the institution as reference for the fields
     * @return an Excel worksheet containing the export
     */
    def exportSubscriptionMembers(List result, Map<String, Object> selectedFields, Subscription subscription, Org institution, Set<String> contactSwitch, FORMAT format) {
       Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionMembersFields(subscription, institution)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        Map<String, List<RefdataValue>> selectedCostItemElements = [all: []]
        List<String> removeSelectedCostItemElements = []
        selectedExportFields.keySet().findAll {it.startsWith('participantSubCostItem.')}.each {
            selectedCostItemElements.all << RefdataValue.get(Long.parseLong(it.split("\\.")[1]))
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

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements, contactSwitch)

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        List exportData = []
        List orgList = []
        result.each { memberResult ->
            _setSubRow(memberResult, selectedExportFields, exportData, localizedName, selectedCostItemElements, selectedCostItemFields, format, contactSwitch)
            orgList << memberResult.orgs
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('subscriptionDetails.members.members', null, locale)] = [titleRow: titles, columnData: exportData]

        sheetData =  _exportAccessPoints(orgList, sheetData, selectedExportFields, locale, "", format)
        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, ',')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }

    }

    /**
     * Exports the given fields from the given subscriptions
     * @param result the subscription set to export
     * @param selectedFields the fields which should appear
     * @param institution the institution as reference
     * @return an Excel worksheet containing the export
     */
    def exportSubscriptions(ArrayList<Subscription> result, Map<String, Object> selectedFields, Org institution, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionFields(institution)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        Map<String, List<RefdataValue>> selectedCostItemElements = [:]
        List<String> removeSelectedCostItemElements = []
        selectedExportFields.keySet().findAll {it.startsWith('subCostItem.')}.each { String key ->
            List<String> keyParts = key.split("\\.")
            if(!selectedCostItemElements.containsKey(keyParts[1]))
                selectedCostItemElements.put(keyParts[1], [])
            selectedCostItemElements[keyParts[1]] << RefdataValue.get(Long.parseLong(keyParts[2]))
            removeSelectedCostItemElements << key
        }
        Map selectedCostItemFields = [:]
        if(selectedCostItemElements){
            /*
            selectedExportFields.keySet().findAll {it.startsWith('costItem.')}.each {
                selectedCostItemFields.put(it, selectedExportFields.get(it))
            }
            selectedCostItemFields.each {
                selectedExportFields.remove(it.key)
            }
            */
            removeSelectedCostItemElements.each {
                selectedExportFields.remove(it)
            }
            selectedExportFields.put('subCostItem', [:])
        }

        Integer maxCostItemsElements = 0

        maxCostItemsElements = CostItem.executeQuery('select count(id) as countCostItems from CostItem where sub in (:subs) group by costItemElement, sub order by countCostItems desc', [subs: result])[0]

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements, null, selectedCostItemElements)

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        List exportData = []
        result.each { Subscription subscription ->
            _setSubRow(subscription, selectedExportFields, exportData, localizedName, selectedCostItemElements, selectedCostItemFields, format)
        }

        switch(format) {
            case FORMAT.XLS:
            Map sheetData = [:]
            sheetData[messageSource.getMessage('myinst.currentSubscriptions.label', null, locale)] = [titleRow: titles, columnData: exportData]

            return exportService.generateXLSXWorkbook(sheetData)
        case FORMAT.CSV:
            return exportService.generateSeparatorTableString(titles, exportData, ',')
        case FORMAT.TSV:
            return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }
    }

    /**
     * Exports the given fields from the given cost items
     * @param result the cost item set to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportCostItems(Map result, Map<String, Object> selectedFields, FORMAT format) {
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
                    _setCostItemRow(ci, selectedExportFields, exportData, format)
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
    def exportOrgs(List<Org> result, Map<String, Object> selectedFields, String config, FORMAT format, Set<String> contactSources = []) {
        Locale locale = LocaleUtils.getCurrentLocale()

        String sheetTitle
        Map wekbRecords = [:]

        switch(config) {
            case 'consortium':
                sheetTitle = messageSource.getMessage('consortium.label', null, locale)
                break
            case 'institution':
                sheetTitle = messageSource.getMessage('default.institution', null, locale)
                break
            case 'member':
                sheetTitle = messageSource.getMessage('subscription.details.consortiaMembers.label', null, locale)
                break
            case 'provider':
                sheetTitle = messageSource.getMessage('default.ProviderAgency.export.label', null, locale)
                ApiSource apiSource = ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)
                Map queryResult = gokbService.queryElasticsearch(apiSource.baseUrl + apiSource.fixToken + "/searchApi", [componentType: 'Org' , max: 10000])
                if (queryResult.warning) {
                    List records = queryResult.warning.result
                    records.each { Map providerRecord ->
                        wekbRecords.put(providerRecord.uuid, providerRecord)
                    }
                }
                break
        }

        Map<String, Object> selectedExportFields = [:], configFields = getExportOrgFields(config)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSources)

        List exportData = []
        result.each { Org org ->
            _setOrgRow(org, selectedExportFields, exportData, wekbRecords, format, contactSources)
        }

        Map sheetData = [:]
        sheetData[sheetTitle] = [titleRow: titles, columnData: exportData]

        sheetData =  _exportAccessPoints(result, sheetData, selectedExportFields, locale, "", format)

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, ',')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }
    }

    def exportAddresses(List visiblePersons, Map<String, Object> selectedFields, withInstData, withProvData, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> configFields = getExportAddressFields(), selectedExportFields = [:], sheetData = [:]
        List instData = [], provData = []

        selectedFields.keySet().each { String key ->
            selectedExportFields.put(key, configFields.get(key))
        }

        List titleRow = [messageSource.getMessage('contact.contentType.label', null, locale)]
        titleRow.addAll(_exportTitles(selectedExportFields, locale))

        Map<Person, Map<String, Map<String, String>>> addressesContacts = [:]
        visiblePersons.each { Person p ->
            //lang: contactData
            Map<String, Map<String, String>> contactData = addressesContacts.get(p)
            if(!contactData)
                contactData = [:]
            p.contacts.each { Contact c ->
                String langKey
                if(c.language)
                    langKey = c.language.getI10n('value')
                else langKey = Contact.PRIMARY
                Map<String, String> contact = contactData.get(langKey)
                if(!contact)
                    contact = [:]
                switch(c.contentType) {
                    case RDStore.CCT_EMAIL: contact.email = c.content
                        break
                    case RDStore.CCT_FAX: contact.fax = c.content
                        break
                    case RDStore.CCT_PHONE: contact.phone = c.content
                        break
                    case RDStore.CCT_URL: contact.url = c.content
                        break
                }
                contactData.put(langKey, contact)
            }
            addressesContacts.put(p, contactData)
        }
        addressesContacts.each { Person p, Map<String, Map<String, String>> contactData ->
            for(int addressRow = 0; addressRow < contactData.size(); addressRow++) {
                String contactType = ''
                PersonRole orgLink = p.roleLinks.find { PersonRole pr -> pr.org != null }
                if(orgLink.functionType)
                    contactType = orgLink.functionType.getI10n('value')
                else if(orgLink.positionType)
                    contactType = orgLink.positionType.getI10n('value')
                List row = [createTableCell(format, contactType)]
                Map.Entry<String, Map<String, String>> contact = contactData.entrySet()[addressRow]
                //Address a = p.addresses[addressRow]
                selectedExportFields.each { String fieldKey, Map mapSelectedFields ->
                    String field = mapSelectedFields.field
                    if (field == 'organisation') {
                        row.add(createTableCell(format, orgLink.org.name))
                    }
                    else if (field == 'receiver') {
                        row.add(createTableCell(format, p.toString()))
                    }
                    else if (field == 'language')
                        contact?.key == Contact.PRIMARY ? row.add(createTableCell(format, ' ')) : row.add(createTableCell(format, contact.key))
                    else if (field in ['email', 'fax', 'phone', 'url'])
                        row.add(createTableCell(format, contact?.value?.get(fieldKey)))
                    /*else {
                        if (a && a.hasProperty(field)) {
                            if (a[field] instanceof RefdataValue)
                                row.add([field: a[field].getI10n("value"), style: null])
                            else row.add([field: a[field], style: null])
                        }
                    }*/
                }
                if(orgLink.org.getCustomerType())
                    instData << row
                else provData << row
            }
        }

        if(withInstData)
            sheetData[messageSource.getMessage('org.institution.plural', null, locale)] = [titleRow: titleRow, columnData: instData]
        if(withProvData)
            sheetData[messageSource.getMessage('default.agency.provider.plural.label', null, locale)] = [titleRow: titleRow, columnData: provData]
        if(sheetData.size() == 0)
            sheetData[messageSource.getMessage('org.institution.plural', null, locale)] = [titleRow: titleRow, columnData: []]
        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields of the given survey evaluation
     * @param result the survey evaluation which should be exported
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportSurveyEvaluation(Map result, Map<String, Object> selectedFields, Set<String> contactSwitch, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSurveyEvaluationFields(result.surveyConfig)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSwitch)

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

        //List<SurveyOrg> participantsNotFinish = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfigAndOrgInsertedItself(result.surveyConfig, false)
        //List<SurveyOrg> participantsNotFinishInsertedItself = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfigAndOrgInsertedItself(result.surveyConfig, true)

        List exportData = []

        exportData.add([createTableCell(format, messageSource.getMessage('surveyEvaluation.participantsViewAllFinish', null, locale) + " (${participantsFinish.size()})", 'positive')])

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

            _setSurveyEvaluationRow(participantResult, selectedExportFields, exportData, selectedCostItemFields, format, contactSwitch)
        }

        exportData.add([createTableCell(format, ' ')])
        exportData.add([createTableCell(format, ' ')])
        exportData.add([createTableCell(format, ' ')])
        exportData.add([createTableCell(format, messageSource.getMessage('surveyEvaluation.participantsViewAllNotFinish', null, locale) + " (${participantsNotFinish.size()})", 'negative')])


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

            _setSurveyEvaluationRow(participantResult, selectedExportFields, exportData, selectedCostItemFields, format, contactSwitch)
        }

        /*exportData.add([[field: '', style: null]])
        exportData.add([[field: '', style: null]])
        exportData.add([[field: '', style: null]])

        exportData.add([[field: messageSource.getMessage('renewalEvaluation.orgInsertedItself.label', null, locale) + " (${participantsNotFinishInsertedItself.size()})", style: '']])

        participantsNotFinishInsertedItself.sort { it.org.sortname }.each { SurveyOrg surveyOrg ->
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
        }*/




        Map sheetData = [:]
        sheetData[messageSource.getMessage('surveyInfo.evaluation', null, locale)] = [titleRow: titles, columnData: exportData]

        if (participantsFinish) {
            sheetData = _exportAccessPoints(participantsFinish.org, sheetData, selectedExportFields, locale, " - 1", format)
        }

        if (participantsNotFinish) {
            sheetData = _exportAccessPoints(participantsNotFinish.org, sheetData, selectedExportFields, locale, " - 2", format)
        }

        switch(format) {
            case FORMAT.XLS: return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV: return exportService.generateSeparatorTableString(titles, exportData, ',')
            case FORMAT.TSV: return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }
    }

    /**
     * Exports the given fields from the given issue entitlements
     * @param result the issue entitlements set to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportIssueEntitlements(ArrayList<IssueEntitlement> result, Map<String, Object> selectedFields, FORMAT format) {
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
                    _setIeRow(rowData, selectedExportFields, exportData, format)

                }
                log.debug("flushing after ${offset} ...")
                sess.flush()
            }
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('title.plural', null, locale)] = [titleRow: titles, columnData: exportData]
        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, ',')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }
    }

    /**
     * Exports the given fields from the given titles
     * @param result the titles set to export
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportTipps(Collection result, Map<String, Object> selectedFields, FORMAT format) {
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

        int max = result[0] instanceof Long ? 50000 : 500
        TitleInstancePackagePlatform.withSession { Session sess ->
            for(int offset = 0; offset < result.size(); offset+=max) {
                List allRows = []
                Set<TitleInstancePackagePlatform> tipps = []
                if(result[0] instanceof TitleInstancePackagePlatform) {
                    //this double structure is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
                    tipps = result.drop(offset).take(max)
                }
                else if(result[0] instanceof Long) {
                    tipps = TitleInstancePackagePlatform.findAllByIdInList(result.drop(offset).take(max), [sort: 'sortname'])
                }
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

                allRows.eachWithIndex { rowData, int i ->
                    long start = System.currentTimeMillis()
                    _setTippRow(rowData, selectedExportFields, exportData, format)
                    log.debug("used time for record ${i}: ${System.currentTimeMillis()-start}")
                }
                log.debug("flushing after ${offset} ...")
                sess.flush()
            }
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('title.plural', null, locale)] = [titleRow: titles, columnData: exportData]

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }
    }

    /**
     * Fills a row for the renewal export
     * @param participantResult the participant's result for the row
     * @param selectedFields the fields which should appear
     * @param renewalData the output list containing the rows
     * @param onlySubscription should only subscription-related parameters appear?
     * @param multiYearTermTwoSurvey should two years running times appear?
     * @param multiYearTermThreeSurvey should three years running times appear?
     * @param format the format to use for export
     */
    private void _setRenewalRow(Map participantResult, Map<String, Object> selectedFields, List renewalData, boolean onlySubscription, PropertyDefinition multiYearTermTwoSurvey, PropertyDefinition multiYearTermThreeSurvey, PropertyDefinition multiYearTermFourSurvey, PropertyDefinition multiYearTermFiveSurvey, FORMAT format){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('surveyProperty.')) {
                    if (onlySubscription) {
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
                    } else {
                        Long id = Long.parseLong(fieldKey.split("\\.")[1])
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, PropertyDefinition.get(id))
                        String resultStr = participantResultProperty.getResult() ?: " ", comment = participantResultProperty.comment ?: " "
                        row.add(createTableCell(format, resultStr))
                        row.add(createTableCell(format, comment))
                    }
                } else if (fieldKey == 'survey.period') {
                    String period = ""
                    if (multiYearTermTwoSurvey) {
                        period = participantResult.newSubPeriodTwoStartDate ? sdf.format(participantResult.newSubPeriodTwoStartDate) : " "
                        period = participantResult.newSubPeriodTwoEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodTwoEndDate) : " "
                    }

                    if (multiYearTermThreeSurvey) {
                        period = participantResult.newSubPeriodThreeStartDate ? sdf.format(participantResult.newSubPeriodThreeStartDate) : " "
                        period = participantResult.newSubPeriodThreeEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodThreeEndDate) : " "
                    }

                    if (multiYearTermFourSurvey) {
                        period = participantResult.newSubPeriodFourStartDate ? sdf.format(participantResult.newSubPeriodFourStartDate) : " "
                        period = participantResult.newSubPeriodFourEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodFourEndDate) : " "
                    }

                    if (multiYearTermFiveSurvey) {
                        period = participantResult.newSubPeriodFiveStartDate ? sdf.format(participantResult.newSubPeriodFiveStartDate) : " "
                        period = participantResult.newSubPeriodFiveEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodFiveEndDate) : " "
                    }

                    row.add(createTableCell(format, period))
                } else if (fieldKey == 'survey.costPeriod') {
                    String period = ""
                    if (participantResult.resultOfParticipation && participantResult.resultOfParticipation.costItem) {
                        period = participantResult.resultOfParticipation.costItem.startDate ? sdf.format(participantResult.resultOfParticipation.costItem.startDate) : " "
                        period = participantResult.resultOfParticipation.costItem.endDate ? period + " - " + sdf.format(participantResult.resultOfParticipation.costItem.endDate) : " "
                    }

                    row.add(createTableCell(format, period))
                }
                else if (fieldKey == 'survey.periodComment') {
                    String twoComment = participantResult.participantPropertyTwoComment ?: ' '
                    String threeComment = participantResult.participantPropertyThreeComment ?: ' '
                    String fourComment = participantResult.participantPropertyFourComment ?: ' '
                    String fiveComment = participantResult.participantPropertyFiveComment ?: ' '
                    if (multiYearTermTwoSurvey) {
                        row.add(createTableCell(format, twoComment))
                    }

                    if (multiYearTermThreeSurvey) {
                        row.add(createTableCell(format, threeComment))
                    }

                    if (multiYearTermFourSurvey) {
                        row.add(createTableCell(format, fourComment))
                    }

                    if (multiYearTermFiveSurvey) {
                        row.add(createTableCell(format, fiveComment))
                    }

                    if (!multiYearTermTwoSurvey && !multiYearTermThreeSurvey && !multiYearTermFourSurvey && !multiYearTermFiveSurvey) {
                        row.add(createTableCell(format, ' '))
                    }
                }else if (fieldKey == 'participant.generalContact') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, participantResult.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else {
                    if (onlySubscription) {
                        if (fieldKey.startsWith('subscription.') || fieldKey.startsWith('participant.')) {
                            def fieldValue = _getFieldValue(participantResult, field, sdf)
                            row.add(createTableCell(format, fieldValue))
                        } else {
                            row.add(createTableCell(format, ' '))
                        }

                    } else {
                        def fieldValue = _getFieldValue(participantResult, field, sdf)
                        row.add(createTableCell(format, fieldValue))
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
    private void _setSubRow(def result, Map<String, Object> selectedFields, List exportData, String localizedName, Map selectedCostItemElements, Map selectedCostItemFields, FORMAT format, Set<String> contactSources = []){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()
        Org org, contextOrg = contextService.getOrg()
        Subscription subscription
        if(result instanceof Subscription) {
            subscription = result
            org = subscription.getSubscriber()
        }
        else {
            subscription = result.sub
            org = result.orgs
        }


        List costItems
        Map<String, Object> costItemSums = [:]
        //in order to distinguish between sums and entire items
        if(selectedCostItemElements.containsKey('all')){
           costItems = CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqual(subscription, selectedCostItemElements.all, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
        }
        else if(selectedCostItemElements) {
            selectedCostItemElements.each { String key, List<RefdataValue> costItemElements ->
                Map<String, Object> costSumSubMap = [:]
                costItemElements.each { RefdataValue cie ->
                    //determine cost items configuration based on customer type
                    switch(contextOrg.getCustomerType()) {
                        //cases one to three
                        case CustomerTypeService.ORG_CONSORTIUM_BASIC:
                        case CustomerTypeService.ORG_CONSORTIUM_PRO:
                            costItems = CostItem.executeQuery('select ci.id from CostItem ci join ci.sub sub where ci.owner = :contextOrg and (sub = :sub or sub.instanceOf = :sub) and ci.costItemElement = :cie and ci.costItemStatus != :deleted', [contextOrg: contextOrg, sub: subscription, cie: cie, deleted: RDStore.COST_ITEM_DELETED])
                            break
                            //cases four and five
                        case CustomerTypeService.ORG_INST_PRO:
                            if(key == 'own')
                                costItems = CostItem.executeQuery('select ci.id from CostItem ci join ci.sub sub where ci.owner = :contextOrg and sub = :sub and ci.costItemElement = :cie and ci.costItemStatus != :deleted', [contextOrg: contextOrg, sub: subscription, cie: cie, deleted: RDStore.COST_ITEM_DELETED])
                            else if(key == 'subscr')
                                costItems = CostItem.executeQuery('select ci.id from CostItem ci join ci.sub sub where ci.isVisibleForSubscriber = true and sub = :sub and ci.costItemElement = :cie and ci.costItemStatus != :deleted', [sub: subscription, cie: cie, deleted: RDStore.COST_ITEM_DELETED])
                            break
                            //cases six: basic member
                        case CustomerTypeService.ORG_INST_BASIC: costItems = CostItem.executeQuery('select ci.id from CostItem ci join ci.sub sub where ci.isVisibleForSubscriber = true and sub = :sub and ci.costItemElement = :cie and ci.costItemStatus != :deleted', [sub: subscription, cie: cie, deleted: RDStore.COST_ITEM_DELETED])
                            break
                    }
                    costSumSubMap.put(cie, financeService.calculateResults(costItems))
                }
                costItemSums.put(key, costSumSubMap)
            }
        }

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(org, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(org, row, fieldKey, format, null, 'public')
                }
                /*
                else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                */
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format, subscription)
                }
                else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if(fieldKey == 'package.name') {
                    row.add(createTableCell(format, subscription.packages.pkg.name.join('; ')))
                }
                else if(fieldKey == 'platform.name') {
                    row.add(createTableCell(format, Platform.executeQuery('select distinct(plat.name) from SubscriptionPackage sp join sp.pkg pkg join pkg.nominalPlatform plat where sp.subscription = :sub', [sub: subscription]).join('; ')))
                }
                else if(fieldKey.startsWith('packageIdentifiers.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    List<String> identifierList = Identifier.executeQuery("select ident.value from Identifier ident where ident.pkg in (select sp.pkg from SubscriptionPackage sp where sp.subscription = :sub) and ident.ns.id = :namespace and ident.value != :unknown and ident.value != ''", [sub: subscription, namespace: id, unknown: IdentifierNamespace.UNKNOWN])
                    if (identifierList) {
                        row.add(createTableCell(format, identifierList.join("; ")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.startsWith('participantSubProperty.') || fieldKey.startsWith('subProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    String query = "select prop from SubscriptionProperty prop where (prop.owner = :sub and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :sub and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg) order by prop.type.${localizedName} asc"
                    List<SubscriptionProperty> subscriptionProperties = SubscriptionProperty.executeQuery(query,[sub:subscription, propertyDefs:[id], contextOrg: contextService.getOrg()])
                    if(subscriptionProperties){
                        row.add(createTableCell(format, subscriptionProperties.collect { it.getValueInI10n()}.join(";")))
                    }else{
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey == 'memberCount') {
                    int count = Subscription.countByInstanceOf(subscription)
                    row.add(createTableCell(format, count))
                }
                else if (fieldKey == 'multiYearCount') {
                    int count = Subscription.countByInstanceOfAndIsMultiYear(subscription, true)
                    row.add(createTableCell(format, count))
                }
                else if ((fieldKey == 'participantSubCostItem' || fieldKey == 'subCostItem')) {
                    if(costItemSums) {
                        costItemSums.each { String view, Map costSumSubMap ->
                            costSumSubMap.each { RefdataValue cie, Map sums ->
                                List<String> cellValue = []
                                sums.billingSums.each { Map currencyMap ->
                                    cellValue << "${messageSource.getMessage('financials.newCosts.billingSum', null, locale)} (${currencyMap.currency}): ${BigDecimal.valueOf(currencyMap.billingSumAfterTax).setScale(2, RoundingMode.HALF_UP)}"
                                    cellValue << "${messageSource.getMessage('financials.newCosts.finalSum', null, locale)}: ${BigDecimal.valueOf(currencyMap.localSumAfterTax).setScale(2, RoundingMode.HALF_UP)}"
                                }
                                row.add(createTableCell(format, cellValue.join('\n')))
                            }
                        }
                    }
                    else if(costItems && selectedCostItemFields.size() > 0){
                        costItems.each { CostItem costItem ->
                            String cieVal = costItem.costItemElement ? costItem.costItemElement.getI10n('value') : ''
                            row.add(createTableCell(format, cieVal))
                            selectedCostItemFields.each {
                                def fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                row.add(createTableCell(format, fieldValue))
                            }
                        }
                    }
                    else if(selectedCostItemFields.size() > 0) {
                            row.add(createTableCell(format, ' '))
                            selectedCostItemFields.each {
                                row.add(createTableCell(format, ' '))
                            }
                    }
                }
                else {
                    def fieldValue = _getFieldValue(result, field, sdf)
                    String strVal = ' '
                    if(fieldValue instanceof Year) {
                        strVal = fieldValue != null ? fieldValue.toString() : ' '
                    }
                    else {
                        strVal = fieldValue != null ? fieldValue : ' '
                    }
                    row.add(createTableCell(format, strVal))
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
    private void _setCostItemRow(CostItem costItem, Map<String, Object> selectedFields, List exportData, FORMAT format){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        Org org = costItem.sub ? costItem.sub.getSubscriber() : null

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey == 'participant.generalContact') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }else if (fieldKey == 'participant.billingContact') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format, costItem.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else {
                    def fieldValue = _getFieldValue(costItem, field, sdf)
                    row.add(createTableCell(format, fieldValue))
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
    private void _setOrgRow(Org result, Map<String, Object> selectedFields, List exportData, Map wekbRecords, FORMAT format, Set<String> contactSources = []){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(result, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(result, row, fieldKey, format, null, 'public')
                }
                /*else if (fieldKey.contains('billingContact')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(result, row, fieldKey, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(result, row, fieldKey, null, 'public')
                }
                else if (fieldKey.contains('billingAdress')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(result, row, fieldKey, null, contactSwitch)
                        }
                    }
                    else
                        _setOrgFurtherInformation(result, row, fieldKey, null)
                }
                else if (fieldKey.contains('postAdress')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(result, row, fieldKey, null, contactSwitch)
                        }
                    }
                    else
                        _setOrgFurtherInformation(result, row, fieldKey, null)
                }*/
                else if (fieldKey.contains('altnames')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantIdentifiers.') || fieldKey.startsWith('providerIdentifiers.')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantCustomerIdentifiers.') || fieldKey.startsWith('providerCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantProperty.') || fieldKey.startsWith('providerProperty.')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.split('\\.')[1] in Org.WEKB_PROPERTIES) {
                    if(result.gokbId != null) {
                        def fieldValue = wekbRecords.containsKey(result.gokbId) && wekbRecords.get(result.gokbId)[field] != null ? wekbRecords.get(result.gokbId)[field] : ' '

                        if(fieldValue instanceof List)
                            row.add(createTableCell(format, fieldValue.join(', ').replaceAll('"', '')))
                        else row.add(createTableCell(format, fieldValue))
                    }
                    else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else {
                    def fieldValue = field && result[field] != null ? result[field] : ' '

                    if(fieldValue instanceof RefdataValue){
                        fieldValue = fieldValue.getI10n('value')
                    }

                    if(fieldValue instanceof Boolean){
                        fieldValue = (fieldValue == true ? RDStore.YN_YES.getI10n('value') : (fieldValue == false ? RDStore.YN_NO.getI10n('value') : ''))
                    }

                    if(fieldValue instanceof Date){
                        fieldValue = sdf.format(fieldValue)
                    }
                    row.add(createTableCell(format, fieldValue))
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
    private void _setSurveyEvaluationRow(Map participantResult, Map<String, Object> selectedFields, List exportData, Map selectedCostItemFields, FORMAT format, Set<String> contactSources = []){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('surveyProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, PropertyDefinition.get(id))
                    String result = participantResultProperty.getResult() ?: " ", comment = participantResultProperty.comment ?: " "
                    row.add(createTableCell(format, result))
                    row.add(createTableCell(format, comment))
                } else if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, 'public')
                }
                /*
                else if (fieldKey == 'participant.billingContact') {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, null, 'public')
                }else if (fieldKey == 'participant.billingAdress') {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, null, 'public')
                }else if (fieldKey == 'participant.postAdress') {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, null, 'public')
                }
                */
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, participantResult.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participantSurveyCostItem') {
                    if(participantResult.surveyCostItem){
                            selectedCostItemFields.each {
                                def fieldValue = _getFieldValue(participantResult.surveyCostItem, it.value.field.replace('costItem.', ''), sdf)
                                row.add(createTableCell(format, fieldValue))
                            }
                    }else {
                        selectedCostItemFields.each {
                            row.add(createTableCell(format, ' '))
                        }
                    }
                }else {
                        def fieldValue = _getFieldValue(participantResult, field, sdf)
                        row.add(createTableCell(format, fieldValue))
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
    private void _setIeRow(def result, Map<String, Object> selectedFields, List exportData, FORMAT format){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        result = exportService.getIssueEntitlement(result)

        DecimalFormat df = new DecimalFormat("###,##0.00")
        df.decimalFormatSymbols = new DecimalFormatSymbols(LocaleUtils.getCurrentLocale())
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('issueEntitlementIdentifiers.')) {
                        if (result) {
                            Long id = Long.parseLong(fieldKey.split("\\.")[1])
                            List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.tipp = :tipp and ident.ns.id in (:namespaces)", [tipp: result.tipp, namespaces: [id]])
                            if (identifierList) {
                                row.add(createTableCell(format, identifierList.value.join(";")))
                            } else {
                                row.add(createTableCell(format, ' '))
                            }
                        } else {
                            row.add(createTableCell(format, ' '))
                        }
                }
                else if (fieldKey.contains('tipp.ddcs')) {
                    row.add(createTableCell(format, result.tipp.ddcs.collect {"${it.ddc.value} - ${it.ddc.getI10n("value")}"}.join(";")))
                }
                else if (fieldKey.contains('tipp.languages')) {
                    row.add(createTableCell(format, result.tipp.languages.collect {"${it.language.getI10n("value")}"}.join(";")))
                }else if (fieldKey.contains('perpetualAccessBySub')) {
                    String perpetualAccessBySub = result.perpetualAccessBySub ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')
                    row.add(createTableCell(format, perpetualAccessBySub))
                }
                else if (fieldKey.startsWith('coverage.')) {
                    AbstractCoverage covStmt = exportService.getCoverageStatement(result)
                    String coverageField = fieldKey.split("\\.")[1]

                    def fieldValue = covStmt ? _getFieldValue(covStmt, coverageField, sdf) : null
                    String fieldValStr = fieldValue != null ? fieldValue : ' '
                    row.add(createTableCell(format, fieldValStr))
                }
                else if (fieldKey.contains('listPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.listPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('listPriceGBP')) {
                    PriceItem priceItem = result.priceItems.find { it.listCurrency == RDStore.CURRENCY_GBP }

                    if (priceItem) {
                        row.add(createTableCell(format, df.format(priceItem.listPrice)))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('listPriceUSD')) {
                    PriceItem priceItem = result.priceItems.find { it.listCurrency == RDStore.CURRENCY_USD }

                    if (priceItem) {
                        row.add(createTableCell(format, df.format(priceItem.listPrice)))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('localPriceEUR')) {
                    PriceItem priceItem = result.priceItems.find { it.localCurrency == RDStore.CURRENCY_EUR }

                    if (priceItem) {
                        row.add(createTableCell(format, df.format(priceItem.localPrice)))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('localPriceGBP')) {
                    PriceItem priceItem = result.priceItems.find { it.localCurrency == RDStore.CURRENCY_GBP }

                    if (priceItem) {
                        row.add(createTableCell(format, df.format(priceItem.localPrice)))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('localPriceUSD')) {
                    PriceItem priceItem = result.priceItems.find { it.localCurrency == RDStore.CURRENCY_USD }

                    if (priceItem) {
                        row.add(createTableCell(format, df.format(priceItem.localPrice)))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else {
                    def fieldValue = _getFieldValue(result, field, sdf)
                    String fieldValStr = fieldValue != null ? fieldValue : ' '
                    row.add(createTableCell(format, fieldValStr))
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
    private void _setTippRow(def result, Map<String, Object> selectedFields, List exportData, FORMAT format){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        result = exportService.getTipp(result)

        DecimalFormat df = new DecimalFormat("###,##0.00")
        df.decimalFormatSymbols = new DecimalFormatSymbols(LocaleUtils.getCurrentLocale())
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('tippIdentifiers.')) {
                    if (result) {
                        Long id = Long.parseLong(fieldKey.split("\\.")[1])
                        List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.tipp = :tipp and ident.ns.id in (:namespaces)", [tipp: result.tipp, namespaces: [id]])
                        if (identifierList) {
                            row.add(createTableCell(format, identifierList.value.join(";")))
                        } else {
                            row.add(createTableCell(format, ' '))
                        }
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('ddcs')) {
                    row.add(createTableCell(format, result.ddcs.collect {"${it.ddc.value} - ${it.ddc.getI10n("value")}"}.join(";")))
                }
                else if (fieldKey.contains('languages')) {
                    row.add(createTableCell(format, result.languages.collect { "${it.language.getI10n("value")}" }.join(";")))
                }
                else if (fieldKey.startsWith('coverage.')) {
                    AbstractCoverage covStmt = exportService.getCoverageStatement(result)
                    String coverageField = fieldKey.split("\\.")[1]

                    def fieldValue = covStmt ? _getFieldValue(covStmt, coverageField, sdf) : null
                    row.add(createTableCell(format, fieldValue))
                }
                else if (fieldKey.contains('listPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.listPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('listPriceGBP')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_GBP }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.listPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('listPriceUSD')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.listCurrency == RDStore.CURRENCY_USD }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.listPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('localPriceEUR')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_EUR }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.localPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('localPriceGBP')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_GBP }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.localPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.contains('localPriceUSD')) {
                    LinkedHashSet<PriceItem> priceItemsList = result.priceItems.findAll { it.localCurrency == RDStore.CURRENCY_USD }

                    if (priceItemsList) {
                        row.add(createTableCell(format, priceItemsList.collect {df.format(it.localPrice)}.join(";")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else {
                    def fieldValue = _getFieldValue(result, field, sdf)
                    row.add(createTableCell(format, fieldValue))
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
        def fieldValue = ' '
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
            fieldValue = (fieldValue == true ? RDStore.YN_YES.getI10n('value') : (fieldValue == false ? RDStore.YN_NO.getI10n('value') : ' '))
        }

        if(fieldValue instanceof Date){
            fieldValue = sdf.format(fieldValue)
        }

        if(fieldValue instanceof List){
            fieldValue = fieldValue.join('; ')
        }

        return fieldValue
    }

    def createTableCell(FORMAT format, def value, String style = null) {
        if(format == FORMAT.XLS)
            [field: value, style: style]
        else if(format == FORMAT.CSV)
            value.replaceAll('\n', ';')
        else if(format == FORMAT.TSV)
            value.replaceAll('\n', ',')
        else value
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
    private Map _exportAccessPoints(List<Org> orgList, Map sheetData, LinkedHashMap selectedExportFields, Locale locale, String sheetNameAddition, FORMAT format) {

        Map export = [:]
        String sheetName = ''

        if ('participant.exportIPs' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportIPsOfOrgs(orgList, format, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }
        }

        if ('participant.exportProxys' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportProxysOfOrgs(orgList, format, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportEZProxys' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportEZProxysOfOrgs(orgList, format, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportShibboleths' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportShibbolethsOfOrgs(orgList, format, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        if ('participant.exportMailDomains' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportMailDomainsOfOrgs(orgList, format, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportMailDomains.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }

        return sheetData
    }

    private void _setOrgFurtherInformation(Org org, List row, String fieldKey, FORMAT format, Subscription subscription = null, String contactSwitch = 'public'){

        boolean isPublic = contactSwitch == 'public'
        String tenantFilter = '', contactTypeFilter = ''
        if (fieldKey.contains('Contact.')) {
            if (org) {
                Map<String, Object> queryParams = [org: org, type: RDStore.CCT_EMAIL, isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                RefdataValue contactType = RefdataValue.getByCategoriesDescAndValue([RDConstants.PERSON_POSITION, RDConstants.PERSON_FUNCTION, RDConstants.PERSON_RESPONSIBILITY], fieldKey.split('\\.')[1])
                switch(contactType.owner.desc) {
                    case RDConstants.PERSON_FUNCTION: contactTypeFilter = 'pr.functionType = :functionType'
                        queryParams.functionType = contactType
                        break
                    case RDConstants.PERSON_POSITION: contactTypeFilter = 'pr.positionType = :positionType'
                        queryParams.positionType = contactType
                        break
                    case RDConstants.PERSON_RESPONSIBILITY: contactTypeFilter = 'pr.responsibilityType = :responsibilityType'
                        queryParams.responsibilityType = contactType
                        break
                }
                List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.org = :org and "+contactTypeFilter+" and c.contentType = :type and p.isPublic = :isPublic"+tenantFilter, queryParams)

                if (contactList) {
                    row.add(createTableCell(format, contactList.content.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }

        }
        /*else if (fieldKey.endsWith('.billingContact')) {
            if (org) {
                Map<String, Object> queryParams = [org: org, functionTypes: [RDStore.PRS_FUNC_FC_BILLING_ADDRESS], type: RDStore.CCT_EMAIL, isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.org = :org and pr.functionType in (:functionTypes) and c.contentType = :type and p.isPublic = :isPublic"+tenantFilter, queryParams)

                if (contactList) {
                    row.add([field: contactList.content.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        } else if (fieldKey.endsWith('.billingAdress')) {
            if (org) {
                RefdataValue billingAddress = RDStore.ADDRESS_TYPE_BILLING
                Map<String, Object> queryParams = [org: org, type: billingAddress], personRoleParams = [isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    personRoleParams.ctx = contextService.getOrg()
                }
                //LinkedHashSet<Address> addressList = org.addresses.findAll { Address adress -> adress.type.findAll { it == billingAdress } }
                Set<Address> addressList = Address.executeQuery("select a from PersonRole pr join pr.prs p join p.addresses a join a.type type where pr.org = :org and type = :type and p.isPublic = :isPublic"+tenantFilter, queryParams+personRoleParams)
                if(isPublic)
                    addressList.addAll(Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org", queryParams))

                if (addressList) {
                    row.add([field: addressList.collect { Address address -> _getAddress(address, org)}.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        } else if (fieldKey.endsWith('.postAdress')) {
            if (org) {
                RefdataValue postAddress = RDStore.ADDRESS_TYPE_POSTAL
                Map<String, Object> queryParams = [org: org, type: postAddress], personRoleParams = [isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    personRoleParams.ctx = contextService.getOrg()
                }
                //LinkedHashSet<Address> addressList = org.addresses.findAll { Address adress -> adress.type.findAll { it == postAdress } }
                Set<Address> addressList = Address.executeQuery("select a from PersonRole pr join pr.prs p join p.addresses a join a.type type where pr.org = :org and type = :type and p.isPublic = :isPublic"+tenantFilter, queryParams+personRoleParams)
                if(isPublic)
                    addressList.addAll(Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org", queryParams))

                if (addressList) {
                    row.add([field: addressList.collect { Address address -> _getAddress(address, org)}.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        }*/
        else if (fieldKey.contains('altnames')) {
            if (org) {
                if(org.altnames) {
                    row.add(createTableCell(format, org.altnames.collect { AlternativeName alt -> alt.name }.join('\n')))
                }
                else row.add(createTableCell(format, ' '))
            }
            else {
                row.add(createTableCell(format, ' '))
            }
        } else if (fieldKey.startsWith('participantIdentifiers.') || fieldKey.startsWith('providerIdentifiers.')) {
            if (org) {
                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.id in (:namespaces) and ident.value != :unknown and ident.value != ''", [org: org, namespaces: [id], unknown: IdentifierNamespace.UNKNOWN])
                if (identifierList) {
                    row.add(createTableCell(format, identifierList.value.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        } else if (fieldKey.startsWith('participantCustomerIdentifiers.') || fieldKey.startsWith('providerCustomerIdentifiers.')) {
            if (org) {
                CustomerIdentifier customerIdentifier = CustomerIdentifier.findByCustomerAndPlatform(org, Platform.get(fieldKey.split("\\.")[1]))
                if (customerIdentifier) {
                    row.add(createTableCell(format, customerIdentifier.value))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        } else if (fieldKey.startsWith('participantProperty.') || fieldKey.startsWith('providerProperty.')) {
            if (org) {

                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<OrgProperty> orgProperties = OrgProperty.executeQuery("select prop from OrgProperty prop where (prop.owner = :org and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :org and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg)", [org: org, propertyDefs: [id], contextOrg: contextService.getOrg()])
                if (orgProperties) {
                    List<String> propValues = [], propAnnotations = []
                    orgProperties.each { OrgProperty prop ->
                        propValues << prop.getValueInI10n()
                        if(prop.note)
                            propAnnotations << prop.note
                    }
                    row.add(createTableCell(format, propValues.join('\n')))
                    row.add(createTableCell(format, propAnnotations.join('\n')))
                } else {
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        }else if (fieldKey == 'participant.readerNumbers') {
            if (org) {
                ReaderNumber readerNumberStudents
                ReaderNumber readerNumberStaff
                ReaderNumber readerNumberFTE

                //ReaderNumber readerNumberPeoplewithDueDate = ReaderNumber.findByReferenceGroupAndOrgAndDueDateIsNotNull(RDStore.READER_NUMBER_PEOPLE.value_de, org, [sort: 'dueDate', order: 'desc'])
                ReaderNumber readerNumberStaffwithDueDate = ReaderNumber.findByReferenceGroupAndOrgAndDueDateIsNotNull(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, org, [sort: 'dueDate', order: 'desc'])
                if(readerNumberStaffwithDueDate){
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, readerNumberStaffwithDueDate.value))
                    row.add(createTableCell(format, ' '))
                }else{
                    RefdataValue currentSemester = RefdataValue.getCurrentSemester()

                    readerNumberStudents = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_STUDENTS, org, currentSemester)
                    readerNumberStaff = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, org, currentSemester)
                    readerNumberFTE = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_FTE, org, currentSemester)

                    if(readerNumberStudents || readerNumberStaff || readerNumberFTE){
                        row.add(createTableCell(format, currentSemester.getI10n('value')))
                        BigDecimal studentsStr = readerNumberStudents ? readerNumberStudents.value : null,
                        staffStr = readerNumberStaff ? readerNumberStaff.value : null,
                        fteStr = readerNumberFTE ? readerNumberFTE.value : null
                        row.add(createTableCell(format, studentsStr))
                        row.add(createTableCell(format, staffStr))
                        row.add(createTableCell(format, fteStr))
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
                                    BigDecimal studentsStr = readerNumberStudents ? readerNumberStudents.value : null,
                                           staffStr = readerNumberStaff ? readerNumberStaff.value : null,
                                           fteStr = readerNumberFTE ? readerNumberFTE.value : null
                                    row.add(createTableCell(format, refdataValueList[count].getI10n('value')))
                                    row.add(createTableCell(format, studentsStr))
                                    row.add(createTableCell(format, staffStr))
                                    row.add(createTableCell(format, fteStr))
                                    break
                                }
                            }
                        }
                        if(!readerNumberStudents && !readerNumberStaff && !readerNumberFTE){
                            row.add(createTableCell(format, null))
                            row.add(createTableCell(format, null))
                            row.add(createTableCell(format, null))
                            row.add(createTableCell(format, null))
                        }
                    }
                }

                ReaderNumber readerNumberPeople = ReaderNumber.findByReferenceGroupAndOrg(RDStore.READER_NUMBER_PEOPLE, org, [sort: 'dueDate', order: 'desc'])
                ReaderNumber readerNumberUser = ReaderNumber.findByReferenceGroupAndOrg(RDStore.READER_NUMBER_USER, org, [sort: 'dueDate', order: 'desc'])

                BigDecimal peopleStr = readerNumberUser ? readerNumberUser.value : null, userStr = readerNumberPeople ? readerNumberPeople.value : null

                row.add(createTableCell(format, peopleStr))
                row.add(createTableCell(format, userStr))

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

                row.add(createTableCell(format, sum))

                String note = readerNumberStudents ? readerNumberStudents.dateGroupNote : (readerNumberPeople ? readerNumberPeople.dateGroupNote : (readerNumberUser ? readerNumberUser.dateGroupNote : (readerNumberStaffwithDueDate ? readerNumberStaffwithDueDate.dateGroupNote : '')))

                row.add(createTableCell(format, note))

            } else {
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
            }
        }
    }

    private List _exportTitles(Map<String, Object> selectedExportFields, Locale locale, Map selectedCostItemFields = null, Integer maxCostItemsElements = null, Set<String> contactSources = [], Map selectedCostElements = [:]){
        List titles = []

        String localizedValue = LocaleUtils.getLocalizedAttributeName('value')
        Map<String, String> colHeaderMap = [own: messageSource.getMessage('financials.tab.ownCosts', null, locale),
                                            cons: messageSource.getMessage('financials.tab.consCosts', null, locale),
                                            subscr: messageSource.getMessage('financials.tab.subscrCosts', null, locale)]

        selectedExportFields.keySet().each {String fieldKey ->
            Map fields = selectedExportFields.get(fieldKey)
            if(!fields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    RefdataValue contactType = RefdataValue.findByValue(fieldKey.split('\\.')[1])
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles << "${contactType.getI10n('value')} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"
                        }
                    }
                    else
                        titles << contactType.getI10n('value')
                }
                /*else if (fieldKey.endsWith('.billingContact')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles << "${RDStore.PRS_FUNC_FC_BILLING_ADDRESS."${localizedValue}"} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"
                        }
                    }
                    else
                        titles << RDStore.PRS_FUNC_FC_BILLING_ADDRESS."${localizedValue}"
                }
                else if (fieldKey.endsWith('.billingAdress')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles << "${RDStore.ADDRESS_TYPE_BILLING."${localizedValue}"} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"
                        }
                    }
                    else
                        titles << RDStore.ADDRESS_TYPE_BILLING."${localizedValue}"
                }
                else if (fieldKey.endsWith('.postAdress')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles << "${RDStore.ADDRESS_TYPE_POSTAL."${localizedValue}"} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"
                        }
                    }
                    else
                        titles << RDStore.ADDRESS_TYPE_POSTAL."${localizedValue}"
                }*/
                else if (fieldKey == 'participant.readerNumbers') {
                    titles << messageSource.getMessage('readerNumber.semester.label', null, locale)
                    titles << RDStore.READER_NUMBER_STUDENTS."${localizedValue}"
                    titles << RDStore.READER_NUMBER_SCIENTIFIC_STAFF."${localizedValue}"
                    titles << RDStore.READER_NUMBER_FTE."${localizedValue}"
                    titles << RDStore.READER_NUMBER_PEOPLE."${localizedValue}"
                    titles << RDStore.READER_NUMBER_USER."${localizedValue}"
                    titles << messageSource.getMessage('readerNumber.sum.label', null, locale)
                    titles << messageSource.getMessage('readerNumber.note.label', null, locale)
                }
                else if ((fieldKey == 'participantSubCostItem' || fieldKey == 'subCostItem') && maxCostItemsElements > 0 && selectedCostItemFields.size() > 0) {
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
                else if(fieldKey == 'subCostItem') {
                    selectedCostElements.each { String titleSuffix, List<RefdataValue> costItemElements ->
                        costItemElements.each { RefdataValue cie ->
                            titles << "${cie.getI10n('value')} (${colHeaderMap.get(titleSuffix)})"
                        }
                    }
                }
                else {
                    String label = (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label)
                    if(fields.privateProperty == true)
                        label += ' (Meine Merkmale)'
                    else if(fields.privateProperty == false)
                        label += ' (Allgemeine Merkmale)'
                    titles << label
                    if (fieldKey.contains('Property')) {
                        titles << "${label} ${messageSource.getMessage('default.notes.plural', null, locale)}"
                    }
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
