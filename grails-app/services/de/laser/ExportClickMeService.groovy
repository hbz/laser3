package de.laser

import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.addressbook.Person
import de.laser.addressbook.PersonRole
import de.laser.finance.CostInformationDefinition
import de.laser.finance.CostItem
import de.laser.properties.LicenseProperty
import de.laser.properties.ProviderProperty
import de.laser.properties.VendorProperty
import de.laser.storage.BeanStore
import de.laser.storage.PropertyStore
import de.laser.survey.SurveyConfigPackage
import de.laser.survey.SurveyConfigVendor
import de.laser.survey.SurveyPackageResult
import de.laser.survey.SurveyPersonResult
import de.laser.survey.SurveyVendorResult
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
import de.laser.wekb.ElectronicBilling
import de.laser.wekb.ElectronicDeliveryDelayNotification
import de.laser.wekb.InvoiceDispatch
import de.laser.wekb.LibrarySystem
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import groovy.sql.GroovyRowResult
import groovy.xml.XmlSlurper
import groovy.xml.slurpersupport.GPathResult
import org.grails.web.util.WebUtils
import org.springframework.context.MessageSource
import org.xml.sax.SAXException

import java.math.RoundingMode
import java.text.NumberFormat
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
    BatchQueryService batchQueryService
    ContextService contextService
    DocstoreService docstoreService
    EscapeService escapeService
    ExportService exportService
    FilterService filterService
    FinanceService financeService
    SubscriptionsQueryService subscriptionsQueryService
    SurveyService surveyService
    SubscriptionService subscriptionService

    MessageSource messageSource

    /**
     * The currently supported formats: Excel, character- or tab-separated values, Portable Document Format (Adobe PDF)
     */
    static enum FORMAT {
        XLS, CSV, TSV, PDF
    }

    static final String ADDRESSBOOK = "addressbook"
    static final String CONSORTIAS = "consortias"
    static final String CONSORTIA_PARTICIPATIONS = "consortiaParticipations"
    static final String COST_ITEMS = "costItems"
    static final String ISSUE_ENTITLEMENTS = "issueEntitlements"
    static final String INSTITUTIONS = "institutions"
    static final String LICENSES = "licenses"
    static final String PROVIDERS = "providers"
    static final String SUBSCRIPTIONS = "subscriptions"
    static final String SUBSCRIPTIONS_MEMBERS = "subscriptionsMembers"
    static final String SUBSCRIPTIONS_TRANSFER = "subscriptionsTransfer"
    static final String SURVEY_EVALUATION = "surveyEvaluation"
    static final String SURVEY_RENEWAL_EVALUATION = "surveyRenewalEvaluation"
    static final String SURVEY_COST_ITEMS = "surveyCostItems"
    static final String TIPPS = "tipps"
    static final String VENDORS = "vendors"


    static List<String> CLICK_ME_TYPES = [ADDRESSBOOK, CONSORTIAS, CONSORTIA_PARTICIPATIONS,
                                        COST_ITEMS, ISSUE_ENTITLEMENTS, INSTITUTIONS,
                                        LICENSES, PROVIDERS, SUBSCRIPTIONS, SUBSCRIPTIONS_MEMBERS,
                                        SUBSCRIPTIONS_TRANSFER, SURVEY_EVALUATION,  SURVEY_RENEWAL_EVALUATION,
                                        SURVEY_COST_ITEMS, TIPPS, VENDORS
    ]

    Map<String, Object> getDefaultExportSurveyRenewalConfig() {
        return  [
                //Wichtig: Hier bei dieser Config bitte drauf achten, welche Feld Bezeichnung gesetzt ist,
                // weil die Felder von einer zusammengesetzten Map kommen. siehe SurveyControllerService -> renewalEvaluation
                survey                        : [
                        label  : 'Survey',
                        message: 'survey.label',
                        fields : [
                                'participant.sortname'                  : [field: 'participant.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'                      : [field: 'participant.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true'],
                                'survey.surveyPerson'                   : [field: null, label: 'Survey Contact', message: 'surveyOrg.surveyContacts', defaultChecked: 'true'],
                                'survey.period'                         : [field: null, label: 'Period', message: 'renewalEvaluation.period', defaultChecked: 'true'],
                                'survey.periodComment'                  : [field: null, label: 'Period Comment', message: 'renewalEvaluation.periodComment', defaultChecked: 'true'],
                                'costItem.costPeriod'                   : [field: null, label: 'Cost Period', message: 'renewalEvaluation.costPeriod', defaultChecked: 'true'],
                                'costItem.costInBillingCurrency'        : [field: 'costItem.costInBillingCurrency', label: 'Cost Before Tax', message: 'renewalEvaluation.costBeforeTax', defaultChecked: 'true'],
                                'costItem.costInBillingCurrencyAfterTax': [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Cost After Tax', message: 'renewalEvaluation.costAfterTax', defaultChecked: 'true'],
                                'costItem.taxRate'                      : [field: 'costItem.taxKey.taxRate', label: 'Cost Tax', message: 'renewalEvaluation.costTax', defaultChecked: 'true'],
                                'costItem.billingCurrency'              : [field: 'costItem.billingCurrency', label: 'Cost Before Tax', message: 'default.currency.label', defaultChecked: 'true'],
                                'costItem.costDescription'              : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                                'costItem.costTitle'                    : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'survey.ownerComment'                   : [field: null, label: 'Owner Comment', message: 'surveyResult.commentOnlyForOwner', defaultChecked: 'true'],
                        ]
                ],

                participant                   : [
                        label  : 'Participant',
                        message: 'surveyParticipants.label',
                        fields : [
                                'participant.funderType'              : [field: 'participant.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'           : [field: 'participant.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'             : [field: 'participant.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                'participant.url'                     : [field: 'participant.url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'         : [field: 'participant.legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'                  : [field: 'participant.urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                /*
                                'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                                'participantContact.Functional Contact Billing Adress'   : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                                'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                                'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                                */
                                'participant.eInvoice'                : [field: 'participant.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'          : [field: 'participant.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'     : [field: 'participant.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'           : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.discoverySystemsFrontend': [field: null, label: 'Discovery Systems: Frontend', message: 'org.discoverySystems.frontend.label'],
                                'participant.discoverySystemsIndex'   : [field: null, label: 'Discovery Systems: Index', message: 'org.discoverySystems.index.label'],
                                'participant.uuid'                    : [field: 'participant.globalUID', label: 'Laser-UUID', message: null],
                                'participant.libraryNetwork'          : [field: 'participant.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'                 : [field: 'participant.country', label: 'Country', message: 'org.country.label'],
                                'participant.region'                  : [field: 'participant.region', label: 'Region', message: 'org.region.label'],
                        ]
                ],
                participantContacts           : [
                        label  : 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields : [:]
                ],
                participantAddresses          : [
                        label  : 'Addresses',
                        message: 'org.addresses.label',
                        fields : [:]
                ],
                participantAccessPoints       : [
                        label  : 'Participants Access Points',
                        message: 'exportClickMe.participantAccessPoints',
                        fields : [
                                'participant.exportIPs'        : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                                'participant.exportProxys'     : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                                'participant.exportEZProxys'   : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                                'participant.exportShibboleths': [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                                'participant.exportMailDomains': [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
                        ]
                ],
                participantIdentifiers        : [
                        label  : 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields : [:],

                ],
                participantCustomerIdentifiers: [
                        label  : 'Customer Identifier',
                        message: 'exportClickMe.participantCustomerIdentifiers',
                        fields : [:],

                ],

                subscription                  : [
                        label  : 'Subscription',
                        message: 'subscription.label',
                        fields : [
                                'subscription.name'                  : [field: 'sub.name', label: 'Name', message: 'subscription.name.label'],
                                'subscription.altnames'              : [field: 'sub.altnames', label: 'Alternative names', message: 'altname.plural'],
                                'subscription.startDate'             : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label'],
                                'subscription.endDate'               : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label'],
                                'subscription.manualCancellationDate': [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                'subscription.isMultiYear'           : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label'],
                                'subscription.referenceYear'         : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                                'subscription.status'                : [field: 'sub.status', label: 'Status', message: 'subscription.status.label'],
                                'subscription.kind'                  : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label'],
                                'subscription.form'                  : [field: 'sub.form', label: 'Form', message: 'subscription.form.label'],
                                'subscription.resource'              : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                                'subscription.hasPerpetualAccess'    : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                                'subscription.hasPublishComponent'   : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                                'subscription.holdingSelection'      : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
                                'subscription.uuid'                  : [field: 'sub.globalUID', label: 'Laser-UUID', message: null],
                        ]
                ]
        ]
    }

    Map<String, Object> getDefaultExportSubscriptionMembersConfig() {
        return [
                subscription                  : [
                        label  : 'Subscription',
                        message: 'subscription.label',
                        fields : [
                                'subscription.name'                  : [field: 'sub.name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                                'subscription.altnames'              : [field: 'sub.altnames', label: 'Alternative names', message: 'altname.plural'],
                                'subscription.startDate'             : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label', defaultChecked: 'true'],
                                'subscription.endDate'               : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label', defaultChecked: 'true'],
                                'subscription.manualCancellationDate': [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                'subscription.isMultiYear'           : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label', defaultChecked: 'true'],
                                'subscription.referenceYear'         : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label', defaultChecked: 'true'],
                                'subscription.status'                : [field: 'sub.status', label: 'Status', message: 'subscription.status.label', defaultChecked: 'true'],
                                'subscription.kind'                  : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label', defaultChecked: 'true'],
                                'subscription.form'                  : [field: 'sub.form', label: 'Form', message: 'subscription.form.label', defaultChecked: 'true'],
                                'subscription.resource'              : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                                'subscription.hasPerpetualAccess'    : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                                'subscription.hasPublishComponent'   : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                                'subscription.holdingSelection'      : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
                                'subscription.notes'                 : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'subscription.notes.shared'          : [field: null, label: 'Shared notes', message: 'license.notes.shared'],
                                'subscription.uuid'                  : [field: 'sub.globalUID', label: 'LAS:eR-UUID (Institution subscription)', message: 'default.uuid.inst.sub.label'],
                        ]
                ],
                participant                   : [
                        label  : 'Participant',
                        message: 'surveyParticipants.label',
                        fields : [
                                'participant.sortname'                : [field: 'orgs.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'                    : [field: 'orgs.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true'],
                                'participant.funderType'              : [field: 'orgs.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'           : [field: 'orgs.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'             : [field: 'orgs.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                'participant.url'                     : [field: 'orgs.url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'         : [field: 'orgs.legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'                  : [field: 'orgs.urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                /*
                            'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
                                'participant.eInvoice'                : [field: 'orgs.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'          : [field: 'orgs.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'     : [field: 'orgs.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'           : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.discoverySystemsFrontend': [field: null, label: 'Discovery Systems: Frontend', message: 'org.discoverySystems.frontend.label'],
                                'participant.discoverySystemsIndex'   : [field: null, label: 'Discovery Systems: Index', message: 'org.discoverySystems.index.label'],
                                'participant.uuid'                   : [field: 'orgs.globalUID', label: 'LAS:eR-UUID (Institution)', message: 'default.uuid.inst.label'],
                                'participant.libraryNetwork'          : [field: 'orgs.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'                 : [field: 'orgs.country', label: 'Country', message: 'org.country.label'],
                                'participant.region'                  : [field: 'orgs.region', label: 'Region', message: 'org.region.label']
                        ]
                ],
                participantContacts           : [
                        label  : 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields : [:]
                ],
                participantAddresses          : [
                        label  : 'Addresses',
                        message: 'org.addresses.label',
                        fields : [:]
                ],
                participantAccessPoints       : [
                        label  : 'Participants Access Points',
                        message: 'exportClickMe.participantAccessPoints',
                        fields : [
                                'participant.exportIPs'        : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                                'participant.exportProxys'     : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                                'participant.exportEZProxys'   : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                                'participant.exportShibboleths': [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                                'participant.exportMailDomains': [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
                        ]
                ],
                participantIdentifiers        : [
                        label  : 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields : [:]
                ],
                participantCustomerIdentifiers: [
                        label  : 'Customer Identifiers',
                        message: 'exportClickMe.participantCustomerIdentifiers',
                        fields : [:]
                ],

                participantSubProperties      : [
                        label  : 'Properties',
                        message: 'exportClickMe.participantSubProperties',
                        fields : [:]
                ],

                participantSubCostItems       : [
                        label  : 'Cost Items',
                        message: 'subscription.costItems.label',
                        fields : [
                                'costItemsElements'                     : [:],
                                'costItem.costTitle'                    : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'costItem.reference'                    : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                                'costItem.budgetCodes'                  : [field: 'costItem.budgetcodes.value', label: 'Budget Code', message: 'financials.budgetCode'],
                                'costItem.costItemElementConfiguration' : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                                'costItem.costItemStatus'               : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                                'costItem.costInBillingCurrency'        : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                                'costItem.billingCurrency'              : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                                'costItem.costInBillingCurrencyAfterTax': [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                                'costItem.currencyRate'                 : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                                'costItem.taxType'                      : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                                'costItem.taxRate'                      : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                                'costItem.costInLocalCurrency'          : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                                'costItem.costInLocalCurrencyAfterTax'  : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                                'costItem.datePaid'                     : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                                'costItem.financialYear'                : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                                'costItem.invoiceDate'                  : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                                'costItem.startDate'                    : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'costItem.endDate'                      : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                                'costItem.costDescription'              : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                                'costItem.invoiceNumber'                : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                                'costItem.orderNumber'                  : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                                'costItem.pkg'                          : [field: 'costItem.pkg.name', label: 'Package Name', message: 'package.label'],
                                'costItem.issueEntitlement'             : [field: 'costItem.issueEntitlement.tipp.name', label: 'Title', message: 'issueEntitlement.label'],
                                'costItem.issueEntitlementGroup'        : [field: 'costItem.issueEntitlementGroup.name', label: 'Title Group Name', message: 'package.label'],
                        ]
                ],

                providers                     : [
                        label  : 'Provider',
                        message: 'provider.label',
                        fields : [
                                'provider.sortname': [field: 'sub.providers.sortname', label: 'Sortname', message: 'exportClickMe.provider.sortname'],
                                'provider.name'    : [field: 'sub.providers.name', label: 'Name', message: 'exportClickMe.provider.name', defaultChecked: 'true'],
                                'provider.altnames': [field: 'sub.providers.altnames.name', label: 'Alternative Name', message: 'exportClickMe.provider.altnames'],
                                'provider.url'     : [field: 'sub.providers.homepage', label: 'Homepage', message: 'exportClickMe.provider.url']
                        ]
                ],

                vendor                        : [
                        label  : 'Agency',
                        message: 'vendor.label',
                        fields : [
                                'vendor.sortname': [field: 'sub.vendors.sortname', label: 'Sortname', message: 'exportClickMe.vendor.sortname'],
                                'vendor.name'    : [field: 'sub.vendors.name', label: 'Name', message: 'exportClickMe.vendor.name', defaultChecked: 'true'],
                                'vendor.altnames': [field: 'sub.vendors.altnames.name', label: 'Alternative Name', message: 'exportClickMe.vendor.altnames'],
                                'vendor.url'     : [field: 'sub.vendors.homepage', label: 'Homepage', message: 'exportClickMe.vendor.url'],
                        ]
                ],

        ]
    }

    Map<String, Object> getDefaultExportSubscriptionConfig(){
        return [
                subscription: [
                        label: 'Subscription',
                        message: 'subscription.label',
                        fields: [
                                'subscription.name'                         : [field: 'name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                                'subscription.altnames'                     : [field: 'altnames', label: 'Alternative names', message: 'altname.plural'],
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
                                'subscription.notes'                        : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'subscription.notes.shared'                 : [field: null, label: 'Notes', message: 'license.notes.shared'],
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
                                'license.openEnded'       : [field: 'licenses.openEnded', label: 'Open Ended', message: 'license.openEnded.label'],
                                'license.notes'           : [field: null, label: 'Notes (License)', message: 'license.notes.export.label'],
                                'license.notes.shared'    : [field: null, label: 'Notes (License)', message: 'license.notes.export.shared'],
                                'license.uuid'            : [field: 'licenses.globalUID', label: 'Laser-UUID',  message: null],
                        ]
                ],

                identifiers: [
                        label  : 'Identifiers',
                        message: 'subscription.identifiers.label',
                        fields : [:]
                ],

                packages : [
                        label: 'Packages',
                        message: 'subscription.packages.label',
                        fields: [
                                'package.name' : [field: 'package.name', label: 'Name (Package)', message: 'exportClickMe.package.name'],
                                'platform.name' : [field: 'package.nominalPlatform.name', label: 'Name (Platform)', message: 'exportClickMe.platform.name'],
                                'platform.url'  : [field: 'providers.platforms.primaryUrl', label: 'Primary URL', message: 'platform.primaryURL'],
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
                        message: 'provider.label',
                        fields: [
                                'provider.sortname'          : [field: 'providers.sortname', label: 'Sortname', message: 'exportClickMe.provider.sortname'],
                                'provider.name'              : [field: 'providers.name', label: 'Name', message: 'exportClickMe.provider.name', defaultChecked: 'true' ],
                                'provider.altnames'          : [field: 'providers.altnames.name', label: 'Alt Name', message: 'exportClickMe.provider.altnames'],
                                'provider.url'               : [field: 'providers.homepage', label: 'Url', message: 'exportClickMe.provider.url']
                        ]
                ],

                vendors: [
                        label: 'Vendor',
                        message: 'vendor.label',
                        fields: [
                                'vendor.sortname'          : [field: 'vendors.sortname', label: 'Sortname', message: 'exportClickMe.vendor.sortname'],
                                'vendor.name'              : [field: 'vendors.name', label: 'Name', message: 'exportClickMe.vendor.name', defaultChecked: 'true' ],
                                'vendor.altnames'          : [field: 'vendors.altnames.name', label: 'Alt Name', message: 'exportClickMe.vendor.altnames'],
                                'vendor.url'               : [field: 'vendors.homepage', label: 'Url', message: 'exportClickMe.vendor.url'],
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
                                        'costItem.budgetCodes'                      : [field: 'costItem.budgetcodes.value', label: 'Budget Code', message: 'financials.budgetCode'],
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

    }

    Map<String, Object> getDefaultExportSubscriptionSupportConfig(){
        return [
                subscription: [
                        label: 'Subscription',
                        message: 'subscription.label',
                        fields: [
                                'subscription.name'                         : [field: 'name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                                'subscription.altnames'                     : [field: 'altnames', label: 'Alternative names', message: 'altname.plural'],
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
                                'subscription.notes'                        : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'subscription.notes.shared'                 : [field: null, label: 'Notes', message: 'license.notes.shared'],
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
                                'license.openEnded'       : [field: 'licenses.openEnded', label: 'Open Ended', message: 'license.openEnded.label'],
                                'license.notes'           : [field: null, label: 'Notes (License)', message: 'license.notes.export.label'],
                                'license.notes.shared'    : [field: null, label: 'Notes (License)', message: 'license.notes.export.shared'],
                                'license.uuid'            : [field: 'licenses.globalUID', label: 'Laser-UUID',  message: null],
                        ]
                ],

                institutions: [
                        label: 'Consortium members',
                        message: 'consortium.member.plural',
                        fields: [
                                'memberCount':      [field: null, label: 'Count', message: 'default.count.label'],
                                'multiYearCount':   [field: null, label: 'Count multi-year', message: 'default.count.multiYear.label']
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
                                        'costItem.budgetCodes'                      : [field: 'costItem.budgetcodes.value', label: 'Budget Code', message: 'financials.budgetCode'],
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
    }

    Map<String, Object> getDefaultExportConsortiaParticipationsConfig() {
        return [
                subscription: [
                        label: 'Subscription',
                        message: 'subscription.label',
                        fields: [
                                'subscription.name'                         : [field: 'sub.name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                                'subscription.altnames'                     : [field: 'sub.altnames', label: 'Alternative names', message: 'altname.plural'],
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
                                'subscription.notes'                        : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'subscription.notes.shared'                 : [field: null, label: 'Notes', message: 'license.notes.shared'],
                                'subscription.uuid'                         : [field: 'sub.globalUID', label: 'Laser-UUID',  message: null],
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
                                'license.openEnded'       : [field: 'licenses.openEnded', label: 'Open Ended', message: 'license.openEnded.label'],
                                'license.notes'           : [field: null, label: 'Notes (License)', message: 'license.notes.export.label'],
                                'license.notes.shared'    : [field: null, label: 'Notes (License)', message: 'license.notes.export.shared'],
                                'license.uuid'            : [field: 'licenses.globalUID', label: 'Laser-UUID',  message: null],
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
                                'participant.url'               : [field: 'orgs.url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'   : [field: 'orgs.legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'            : [field: 'orgs.urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                /*
                                'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                                'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                                'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                                'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                                 */
                                'participant.eInvoice'          : [field: 'orgs.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'    : [field: 'orgs.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'    : [field: 'orgs.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.discoverySystemsFrontend' : [field: null, label: 'Discovery Systems: Frontend', message: 'org.discoverySystems.frontend.label'],
                                'participant.discoverySystemsIndex' : [field: null, label: 'Discovery Systems: Index', message: 'org.discoverySystems.index.label'],
                                'participant.uuid'                   : [field: 'orgs.globalUID', label: 'LAS:eR-UUID (Institution)', message: 'default.uuid.inst.label'],
                                'participant.libraryNetwork'    : [field: 'orgs.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'           : [field: 'orgs.country', label: 'Country', message: 'org.country.label'],
                                'participant.region'            : [field: 'orgs.region', label: 'Region', message: 'org.region.label']
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

                packages : [
                        label: 'Packages',
                        message: 'subscription.packages.label',
                        fields: [
                                'package.name' : [field: 'package.name', label: 'Name (Package)', message: 'exportClickMe.package.name'],
                                'platform.name' : [field: 'package.nominalPlatform.name', label: 'Name (Platform)', message: 'exportClickMe.platform.name'],
                                'platform.url'  : [field: 'providers.platforms.primaryUrl', label: 'Primary URL', message: 'platform.primaryURL'],
                        ]
                ],

                providers: [
                        label: 'Provider',
                        message: 'provider.label',
                        fields: [
                                'provider.sortname'          : [field: 'providers.sortname', label: 'Sortname', message: 'exportClickMe.provider.sortname'],
                                'provider.name'              : [field: 'providers.name', label: 'Name', message: 'exportClickMe.provider.name', defaultChecked: 'true' ],
                                'provider.altnames'          : [field: 'providers.altnames.name', label: 'Alt Name', message: 'exportClickMe.provider.altnames'],
                                'provider.url'               : [field: 'providers.homepage', label: 'Url', message: 'exportClickMe.provider.url']
                        ]
                ],

                vendors: [
                        label: 'Vendor',
                        message: 'vendor.label',
                        fields: [
                                'vendor.sortname'          : [field: 'vendors.sortname', label: 'Sortname', message: 'exportClickMe.vendor.sortname'],
                                'vendor.name'              : [field: 'vendors.name', label: 'Name', message: 'exportClickMe.vendor.name', defaultChecked: 'true' ],
                                'vendor.url'               : [field: 'vendors.homepage', label: 'Url', message: 'exportClickMe.vendor.url'],
                        ]
                ],

                participantContacts : [
                        label: 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields: [:]
                ],

                participantAddresses : [
                        label: 'Addresses',
                        message: 'org.addresses.label',
                        fields: [:]
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
                                'costItemsElements' : [:]
                        ]
                ]
        ]
    }

    Map<String, Object> getDefaultExportConsortiaParticipationsSupportConfig() {
        return [
                subscription                  : [
                        label  : 'Subscription',
                        message: 'subscription.label',
                        fields : [
                                'subscription.name'                  : [field: 'sub.name', label: 'Name', message: 'subscription.name.label', defaultChecked: 'true'],
                                'subscription.altnames'              : [field: 'sub.altnames', label: 'Alternative names', message: 'altname.plural'],
                                'subscription.startDate'             : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label', defaultChecked: 'true'],
                                'subscription.endDate'               : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label', defaultChecked: 'true'],
                                'subscription.manualCancellationDate': [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                'subscription.isMultiYear'           : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label', defaultChecked: 'true'],
                                'subscription.referenceYear'         : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label', defaultChecked: 'true'],
                                'subscription.status'                : [field: 'sub.status', label: 'Status', message: 'subscription.status.label', defaultChecked: 'true'],
                                'subscription.kind'                  : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label', defaultChecked: 'true'],
                                'subscription.form'                  : [field: 'sub.form', label: 'Form', message: 'subscription.form.label', defaultChecked: 'true'],
                                'subscription.resource'              : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                                'subscription.notes'                 : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'subscription.notes.shared'          : [field: null, label: 'Notes', message: 'license.notes.shared'],
                                'subscription.uuid'                  : [field: 'sub.globalUID', label: 'Laser-UUID', message: null],
                        ]
                ],

                licenses                      : [
                        label  : 'License',
                        message: 'license.label',
                        fields : [
                                'license.name'           : [field: 'licenses.reference', label: 'Name', message: 'exportClickMe.license.name', defaultChecked: 'true'],
                                'license.status'         : [field: 'licenses.status', label: 'Status', message: 'exportClickMe.license.status'],
                                'license.licenseCategory': [field: 'licenses.licenseCategory', label: 'License Category', message: 'license.licenseCategory.label'],
                                'license.startDate'      : [field: 'licenses.startDate', label: 'Start Date', message: 'exportClickMe.license.startDate'],
                                'license.endDate'        : [field: 'licenses.endDate', label: 'End Date', message: 'exportClickMe.license.endDate'],
                                'license.openEnded'      : [field: 'licenses.openEnded', label: 'Open Ended', message: 'license.openEnded.label'],
                                'license.notes'          : [field: null, label: 'Notes (License)', message: 'license.notes.export.label'],
                                'license.notes.shared'   : [field: null, label: 'Notes (License)', message: 'license.notes.export.shared'],
                                'license.uuid'           : [field: 'licenses.globalUID', label: 'Laser-UUID', message: null],
                        ]
                ],

                participant                   : [
                        label  : 'Participant',
                        message: 'surveyParticipants.label',
                        fields : [
                                'participant.sortname'           : [field: 'orgs.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'               : [field: 'orgs.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true'],
                                'participant.funderType'         : [field: 'orgs.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'      : [field: 'orgs.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'        : [field: 'orgs.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                'participant.url'                : [field: 'orgs.url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'    : [field: 'orgs.legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'             : [field: 'orgs.urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                /*
                            'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
                                'participant.eInvoice'           : [field: 'orgs.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'     : [field: 'orgs.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL': [field: 'orgs.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'      : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.libraryNetwork'     : [field: 'orgs.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'            : [field: 'orgs.country', label: 'Country', message: 'org.country.label'],
                                'participant.region'             : [field: 'orgs.region', label: 'Region', message: 'org.region.label']
                        ]
                ],

                participantIdentifiers        : [
                        label  : 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields : [:],

                ],
                participantCustomerIdentifiers: [
                        label  : 'Customer Identifier',
                        message: 'exportClickMe.participantCustomerIdentifiers',
                        fields : [:],

                ],

                participantContacts           : [
                        label  : 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields : [:]
                ],

                participantAddresses          : [
                        label  : 'Addresses',
                        message: 'org.addresses.label',
                        fields : [:]
                ],

                subProperties                 : [
                        label  : 'Public properties',
                        message: 'default.properties',
                        fields : [:]
                ],

                mySubProperties               : [
                        label  : 'My properties',
                        message: 'default.properties.my',
                        fields : [:]
                ],

                subCostItems                  : [
                        label  : 'Cost Items',
                        message: 'subscription.costItems.label',
                        subTabs: [],
                        fields : [
                                'costItemsElements': [:]
                        ]
                ]
        ]
    }

    Map<String, Object> getDefaultExportSubscriptionTransferConfig(){
        return [
                subscriptionTransfer: [
                        label: 'Transfer',
                        message: 'subscription.details.subTransfer.label',
                        fields: [
                                'subscription.offerRequested'                : [field: 'offerRequested', label: 'Offer Requested', message: 'subscription.offerRequested.label', defaultChecked: 'true' ],
                                'subscription.offerRequestedDate'            : [field: 'offerRequestedDate', label: 'Offer Requested Date', message: 'subscription.offerRequestedDate.label', defaultChecked: 'true' ],
                                'subscription.offerNote'                     : [field: 'offerNote', label: 'Offer Note', message: 'subscription.offerNote.label', defaultChecked: 'true' ],
                                'subscription.offerAccepted'                 : [field: 'offerAccepted', label: 'Offer Accepted', message: 'subscription.offerAccepted.label', defaultChecked: 'true' ],
                                'subscription.priceIncreaseInfo'             : [field: 'priceIncreaseInfo', label: 'Price Increase Info', message: 'subscription.priceIncreaseInfo.label', defaultChecked: 'true' ],
                                'subscription.survey'                        : [field: null, label: 'Survey', message: 'survey.label', defaultChecked: 'true' ],
                                'subscription.survey.evaluation'             : [field: null, label: 'Evaluation', message: 'subscription.survey.evaluation.label', defaultChecked: 'true' ],
                                'subscription.survey.cancellation'           : [field: null, label: 'Cancellation', message: 'subscription.survey.cancellation.label', defaultChecked: 'true' ],
                                'subscription.discountScale'                 : [field: 'discountScale', label: 'Discount Scale', message: 'subscription.discountScale.label', defaultChecked: 'true' ],
                                'subscription.reminderSent'                  : [field: 'reminderSent', label: 'Reminder Sent', message: 'subscription.reminderSent.label', defaultChecked: 'true' ],
                                'subscription.reminderSentDate'              : [field: 'reminderSentDate', label: 'Reminder Sent Date', message: 'subscription.reminderSentDate.label', defaultChecked: 'true' ],
                                'subscription.renewalSent'                   : [field: 'renewalSent', label: 'Renewal Sent', message: 'subscription.renewalSent.label', defaultChecked: 'true' ],
                                'subscription.renewalSentDate'               : [field: 'renewalSentDate', label: 'Renewal Sent Date', message: 'subscription.renewalSentDate.label', defaultChecked: 'true' ],
                                'subscription.renewalChanges'               : [field: null, label: 'Renewal Changes', message: 'default.change.label', defaultChecked: 'true' ],
                                'subscription.participantTransferWithSurvey' : [field: 'participantTransferWithSurvey', label: 'Participant Transfe With Survey', message: 'subscription.participantTransferWithSurvey.label', defaultChecked: 'true' ],
                        ]
                ],
        ]
    }

    Map<String, Object> getDefaultExportLicenseConfig() {
        return [
                licenses              : [
                        label  : 'License',
                        message: 'license.label',
                        fields : [
                                'license.reference'      : [field: 'reference', label: 'Name', message: 'exportClickMe.license.name', defaultChecked: 'true'],
                                'license.altnames'       : [field: 'altnames', label: 'Alternative names', message: 'altname.plural'],
                                'license.status'         : [field: 'status', label: 'Status', message: 'exportClickMe.license.status', defaultChecked: 'true'],
                                'license.licenseCategory': [field: 'licenseCategory', label: 'License Category', message: 'license.licenseCategory.label', defaultChecked: 'true'],
                                'license.startDate'      : [field: 'startDate', label: 'Start Date', message: 'exportClickMe.license.startDate', defaultChecked: 'true'],
                                'license.endDate'        : [field: 'endDate', label: 'End Date', message: 'exportClickMe.license.endDate', defaultChecked: 'true'],
                                'license.openEnded'      : [field: 'openEnded', label: 'Open Ended', message: 'license.openEnded.label', defaultChecked: 'true'],
                                'license.notes'          : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'license.notes.shared'   : [field: null, label: 'Notes', message: 'license.notes.shared'],
                                'license.uuid'           : [field: 'globalUID', label: 'Laser-UUID', message: null],
                                'subscription.name'      : [field: 'subscription.name', label: 'Name', message: 'license.details.linked_subs', defaultChecked: 'true']
                        ]
                ],

                /*
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
            ],*/

                identifiers: [
                        label  : 'Identifiers',
                        message: 'license.identifiers.label',
                        fields : [:]
                ],

                providers             : [
                        label  : 'Provider',
                        message: 'provider.label',
                        fields : [
                                'provider.sortname': [field: 'providers.sortname', label: 'Sortname', message: 'exportClickMe.provider.sortname'],
                                'provider.name'    : [field: 'providers.name', label: 'Name', message: 'exportClickMe.provider.name', defaultChecked: 'true'],
                                'provider.altnames': [field: 'providers.altnames.name', label: 'Alt Name', message: 'exportClickMe.provider.altnames'],
                                'provider.url'     : [field: 'providers.homepage', label: 'Url', message: 'exportClickMe.provider.url']
                        ]
                ],

                vendors               : [
                        label  : 'Agency',
                        message: 'vendor.label',
                        fields : [
                                'vendor.sortname': [field: 'vendors.sortname', label: 'Sortname', message: 'exportClickMe.vendor.sortname'],
                                'vendor.name'    : [field: 'vendors.name', label: 'Name', message: 'exportClickMe.vendor.name', defaultChecked: 'true'],
                                'vendor.url'     : [field: 'vendors.homepage', label: 'Url', message: 'exportClickMe.vendor.url']
                        ]
                ],

                /*
                participantIdentifiers: [
                        label  : 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields : [:],

                ],
                */

                licProperties         : [
                        label  : 'Public properties',
                        message: 'default.properties',
                        fields : [:]
                ],

                myLicProperties       : [
                        label  : 'My properties',
                        message: 'default.properties.my',
                        fields : [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportLicenseSupportConfig(){
        return [
                licenses: [
                        label: 'License',
                        message: 'license.label',
                        fields: [
                                'license.reference'       : [field: 'reference', label: 'Name', message: 'exportClickMe.license.name', defaultChecked: 'true'],
                                'license.altnames'        : [field: 'altnames', label: 'Alternative names', message: 'altname.plural'],
                                'license.status'          : [field: 'status', label: 'Status', message: 'exportClickMe.license.status', defaultChecked: 'true'],
                                'license.licenseCategory' : [field: 'licenseCategory', label: 'License Category', message: 'license.licenseCategory.label', defaultChecked: 'true'],
                                'license.startDate'       : [field: 'startDate', label: 'Start Date', message: 'exportClickMe.license.startDate', defaultChecked: 'true'],
                                'license.endDate'         : [field: 'endDate', label: 'End Date', message: 'exportClickMe.license.endDate', defaultChecked: 'true'],
                                'license.openEnded'       : [field: 'openEnded', label: 'Open Ended', message: 'license.openEnded.label', defaultChecked: 'true'],
                                'license.notes'           : [field: null, label: 'Notes', message: 'default.notes.label'],
                                'license.notes.shared'    : [field: null, label: 'Notes', message: 'license.notes.shared'],
                                'license.uuid'            : [field: 'globalUID', label: 'Laser-UUID',  message: null],
                                'subscription.name'       : [field: 'subscription.name', label: 'Name', message: 'license.details.linked_subs', defaultChecked: 'true']
                        ]
                ],

                participantIdentifiers : [
                        label: 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields: [:],
                ],

                licProperties : [
                        label: 'Public properties',
                        message: 'default.properties',
                        fields: [:]
                ],

                myLicProperties : [
                        label: 'My properties',
                        message: 'default.properties.my',
                        fields: [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportConsortiaConfig () {
        return [
                consortium            : [
                        label  : 'Consortium',
                        message: 'consortium.label',
                        fields : [
                                'consortium.sortname'           : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'consortium.name'               : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true'],
                                'consortium.funderType'         : [field: 'funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'consortium.funderHskType'      : [field: 'funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'consortium.libraryType'        : [field: 'libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                /*
                            'consortiumContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'consortiumContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'consortium.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'consortium.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                            */
                                'consortium.eInvoice'           : [field: 'eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'consortium.eInvoicePortal'     : [field: 'eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'consortium.linkResolverBaseURL': [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'consortium.uuid'               : [field: 'globalUID', label: 'Laser-UUID', message: null],
                        ]
                ],
                consortiumContacts    : [
                        label  : 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields : [:]
                ],
                consortiumAddresses   : [
                        label  : 'Addresses',
                        message: 'org.addresses.label',
                        fields : [:]
                ],
                consortiumIdentifiers : [
                        label  : 'Identifiers',
                        message: 'exportClickMe.consortiumIdentifiers',
                        fields : [:]
                ],
                consortiumProperties  : [
                        label  : 'Properties',
                        message: 'default.properties',
                        fields : [:]
                ],
                myConsortiumProperties: [
                        label  : 'Properties',
                        message: 'default.properties.my',
                        fields : [:]
                ]
                //customer identifiers: sense for consortia?
        ]
    }

    Map<String, Object> getDefaultExportCostItemConfig() {
        return [
                costItem : [
                        label: 'Cost Item',
                        message: 'costItem.label',
                        fields: [
                                'costItem.costItemElement'                  : [field: 'costItemElement', label: 'Cost Item Element', message: 'financials.costItemElement', defaultChecked: 'true'],
                                'costItem.costTitle'                        : [field: 'costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle', defaultChecked: 'true'],
                                'costItem.reference'                        : [field: 'reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                                'costItem.budgetCodes'                      : [field: 'budgetcodes.value', label: 'Budget Code', message: 'financials.budgetCode'],
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

                                'costItem.datePaid'                         : [field: 'datePaid', label: 'Date Paid', message: 'financials.datePaid'],
                                'costItem.financialYear'                    : [field: 'financialYear', label: 'Financial Year', message: 'financials.financialYear'],
                                'costItem.invoiceDate'                      : [field: 'invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                                'costItem.startDate'                        : [field: 'startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'costItem.endDate'                          : [field: 'endDate', label: 'Date To', message: 'financials.dateTo'],

                                'costItem.costDescription'                  : [field: 'costDescription', label: 'Description', message: 'default.description.label'],
                                'costItem.invoiceNumber'                    : [field: 'invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                                'costItem.orderNumber'                      : [field: 'order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                                'costItem.pkg'                              : [field: 'pkg.name', label: 'Package Name', message: 'package.label'],
                                'costItem.issueEntitlement'                 : [field: 'issueEntitlement.tipp.name', label: 'Title', message: 'issueEntitlement.label'],
                                'costItem.issueEntitlementGroup'            : [field: 'issueEntitlementGroup.name', label: 'Title Group Name', message: 'issueEntitlementGroup.label'],
                        ]
                ],

                costInformation : [
                        label: 'Information budgets',
                        message: 'costInformationDefinition.financeView.label',
                        fields: [:]
                ],

                myCostInformation : [
                        label: 'My information budgets',
                        message: 'costInformationDefinition.financeView.my.label',
                        fields: [:]
                ],

                org : [
                        label: 'Organisation',
                        message: 'org.institution.label',
                        fields: [
                                'participant.sortname'          : [field: 'sub.subscriberRespConsortia.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'              : [field: 'sub.subscriberRespConsortia.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                                'participant.funderType'        : [field: 'sub.subscriberRespConsortia.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'     : [field: 'sub.subscriberRespConsortia.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'       : [field: 'sub.subscriberRespConsortia.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                'participant.url'               : [field: 'sub.subscriberRespConsortia.url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'   : [field: 'sub.subscriberRespConsortia.legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'            : [field: 'sub.subscriberRespConsortia.urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                /*
                                'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                                'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                                'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                                'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                                */
                                'participant.eInvoice'          : [field: 'sub.subscriberRespConsortia.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'    : [field: 'sub.subscriberRespConsortia.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'    : [field: 'sub.subscriberRespConsortia.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.discoverySystemsFrontend' : [field: null, label: 'Discovery Systems: Frontend', message: 'org.discoverySystems.frontend.label'],
                                'participant.discoverySystemsIndex' : [field: null, label: 'Discovery Systems: Index', message: 'org.discoverySystems.index.label'],
                                'participant.libraryNetwork'    : [field: 'sub.subscriberRespConsortia.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'           : [field: 'sub.subscriberRespConsortia.country', label: 'Country', message: 'org.country.label'],
                                'participant.region'            : [field: 'sub.subscriberRespConsortia.region', label: 'Region', message: 'org.region.label'],
                                'participant.uuid'              : [field: 'sub.subscriberRespConsortia.globalUID', label: 'Laser-UUID',  message: null],
                        ]
                ],

                subscription: [
                        label: 'Subscription',
                        message: 'subscription.label',
                        fields: [
                                'subscription.name'                         : [field: 'sub.name', label: 'Name', message: 'subscription.name.label', defaultChecked: true],
                                'subscription.startDate'                    : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label', defaultChecked: true],
                                'subscription.endDate'                      : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label', defaultChecked: true],
                                'subscription.manualCancellationDate'       : [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                'subscription.isMultiYear'                  : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label'],
                                'subscription.referenceYear'                : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                                //'subscription.isAutomaticRenewAnnually'     : [field: 'sub.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'],
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
                ],
                participantContacts : [
                        label: 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields: [:]
                ],
                participantAccessPoints       : [
                        label  : 'Participants Access Points',
                        message: 'exportClickMe.participantAccessPoints',
                        fields : [
                                'participant.exportIPs'        : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                                'participant.exportProxys'     : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                                'participant.exportEZProxys'   : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                                'participant.exportShibboleths': [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                                'participant.exportMailDomains': [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
                        ]
                ],
                participantAddresses : [
                        label: 'Addresses',
                        message: 'org.addresses.label',
                        fields: [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportSurveyCostItemConfig(){
        return [
                costItem : [
                        label: 'Cost Item',
                        message: 'costItem.label',
                        fields: [
                                'costItem.costItemElement'                  : [field: 'costItemElement', label: 'Cost Item Element', message: 'financials.costItemElement', defaultChecked: 'true'],
                                'costItem.costTitle'                        : [field: 'costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle', defaultChecked: 'true'],
                                'costItem.costItemElementConfiguration'     : [field: 'costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration', defaultChecked: 'true'],
                                'costItem.costItemStatus'                   : [field: 'costItemStatus', label: 'Status', message: 'default.status.label', defaultChecked: 'true'],
                                'costItem.costInBillingCurrency'            : [field: 'costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total', defaultChecked: 'true'],
                                'costItem.billingCurrency'                  : [field: 'billingCurrency', label: 'Billing Currency', message: 'default.currency.label', defaultChecked: 'true'],
                                'costItem.costInBillingCurrencyAfterTax'    : [field: 'costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount', defaultChecked: 'true'],
                                'costItem.taxType'                          : [field: 'taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType', defaultChecked: 'true'],
                                'costItem.taxRate'                          : [field: 'taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate', defaultChecked: 'true'],
                                'costItem.startDate'                        : [field: 'startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'costItem.endDate'                          : [field: 'endDate', label: 'Date To', message: 'financials.dateTo'],
                                'costItem.costDescription'                  : [field: 'costDescription', label: 'Description', message: 'default.description.label'],
                        ]
                ],

                org : [
                        label: 'Organisation',
                        message: 'org.institution.label',
                        fields: [
                                'participant.sortname'          : [field: 'surveyOrg.org.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'              : [field: 'surveyOrg.org.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                                'participant.funderType'        : [field: 'surveyOrg.org.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'     : [field: 'surveyOrg.org.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'       : [field: 'surveyOrg.org.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                'participant.libraryNetwork'    : [field: 'surveyOrg.org.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.region'            : [field: 'surveyOrg.org.region', label: 'Region', message: 'org.region.label'],
                                'participant.country'           : [field: 'surveyOrg.org.country', label: 'Country', message: 'org.country.label'],
                                'participant.eInvoice'          : [field: 'surveyOrg.org.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'    : [field: 'surveyOrg.org.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'    : [field: 'surveyOrg.org.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.discoverySystemsFrontend' : [field: null, label: 'Discovery Systems: Frontend', message: 'org.discoverySystems.frontend.label'],
                                'participant.discoverySystemsIndex' : [field: null, label: 'Discovery Systems: Index', message: 'org.discoverySystems.index.label'],
                                'participant.uuid'              : [field: 'surveyOrg.org.globalUID', label: 'Laser-UUID',  message: null],
                        ]
                ],

                participantIdentifiers : [
                        label: 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields: [:]
                ],
                participantContacts : [
                        label: 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields: [:]
                ],
                participantAddresses : [
                        label: 'Addresses',
                        message: 'org.addresses.label',
                        fields: [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportOrgConfig() {
        return [
                participant                   : [
                        label  : 'Participant',
                        message: 'surveyParticipants.label',
                        fields : [
                                'participant.sortname'                : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'                    : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true'],
                                'participant.url'                     : [field: 'url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'         : [field: 'legalPatronName', label: 'Name of Legal Patron', message: 'org.legalPatronName.label'],
                                'participant.urlGov'                  : [field: 'urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                'participant.funderType'              : [field: 'funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'           : [field: 'funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'             : [field: 'libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                /*
                            'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
                                'participant.eInvoice'                : [field: 'eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'          : [field: 'eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'     : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'           : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.discoverySystemsFrontend': [field: null, label: 'Discovery Systems: Frontend', message: 'org.discoverySystems.frontend.label'],
                                'participant.discoverySystemsIndex'   : [field: null, label: 'Discovery Systems: Index', message: 'org.discoverySystems.index.label'],
                                'participant.libraryNetwork'          : [field: 'libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'                 : [field: 'country', label: 'Country', message: 'org.country.label'],
                                'participant.region'                  : [field: 'region', label: 'Region', message: 'org.region.label']
                        ]
                ],
                participantAccessPoints       : [
                        label  : 'Participants Access Points',
                        message: 'exportClickMe.participantAccessPoints',
                        fields : [
                                'participant.exportIPs'        : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                                'participant.exportProxys'     : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                                'participant.exportEZProxys'   : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                                'participant.exportShibboleths': [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                                'participant.exportMailDomains': [field: null, label: 'Export Mail Domains', message: 'subscriptionDetails.members.exportMailDomains', separateSheet: 'true'],
                        ]
                ],
                participantIdentifiers        : [
                        label  : 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields : [:]
                ],
                participantCustomerIdentifiers: [
                        label  : 'Customer Identifiers',
                        message: 'exportClickMe.participantCustomerIdentifiers',
                        fields : [:]
                ],
                participantContacts           : [
                        label  : 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields : [:]
                ],
                participantAddresses          : [
                        label  : 'Addresses',
                        message: 'org.addresses.label',
                        fields : [:]
                ],
                participantProperties         : [
                        label  : 'Properties',
                        message: 'default.properties',
                        fields : [:]
                ],
                myParticipantProperties       : [
                        label  : 'Properties',
                        message: 'default.properties.my',
                        fields : [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportOrgSupportConfig(){
        return  [
                participant : [
                        label: 'Participant',
                        message: 'surveyParticipants.label',
                        fields: [
                                'participant.sortname'          : [field: 'sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'              : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                                'participant.url'               : [field: 'url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'   : [field: 'legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'            : [field: 'urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                'participant.funderType'        : [field: 'funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'     : [field: 'funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'       : [field: 'libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                /*
                                'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                                'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                                'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                                'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                                 */
                                'participant.eInvoice'          : [field: 'eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'    : [field: 'eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL'    : [field: 'linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'    : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.libraryNetwork'    : [field: 'libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'           : [field: 'country', label: 'Country', message: 'org.country.label'],
                                'participant.region'            : [field: 'region', label: 'Region', message: 'org.region.label']
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
                        subTabs: [],
                        fields: [:]
                ],
                participantAddresses : [
                        label: 'Addresses',
                        message: 'org.addresses.label',
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
    }

    Map<String, Object> getDefaultExportProviderConfig(){
        return [
                provider : [
                        label: 'Provider',
                        message: 'provider.label',
                        fields: [
                                'provider.name'                  : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                                'provider.sortname'              : [field: 'sortname', label: 'Sortname', message: 'default.shortname.label', defaultChecked: 'true'],
                                'provider.altnames'              : [field: 'altnames', label: 'Alternative names', message: 'altname.plural'],
                                'provider.status'                : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: true],
                                'provider.homepage'              : [field: 'homepage', label: 'Homepage URL', message: 'org.homepage.label', defaultChecked: true],
                                'provider.metadataDownloaderURL' : [field: 'metadataDownloaderURL', label: 'Metadata Downloader URL', message: 'org.metadataDownloaderURL.label', defaultChecked: true],
                                'provider.kbartDownloaderURL'    : [field: 'kbartDownloaderURL', label: 'KBART Downloader URL', message: 'org.KBARTDownloaderURL.label', defaultChecked: true],
                                'provider.packages'              : [field: null, label: 'Packages', message:'package.plural', defaultChecked: true],
                                'provider.platforms'             : [field: null, label: 'Platforms', message: 'org.platforms.label', defaultChecked: true],
                                'provider.subscriptions'         : [field: null, label: 'Subscriptions', message: 'subscription.plural', defaultChecked: true],
                                'provider.licenses'              : [field: null, label: 'Licenses', message: 'license.plural', defaultChecked: true]
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
                providerInvoicing : [
                        label: 'Invoicing',
                        message: 'vendor.invoicing.header',
                        fields: [
                                'vendor.electronicBillings'                 : [field: null, label: 'Electronic invoice formats', message: 'vendor.invoicing.formats.label'],
                                'vendor.invoiceDispatchs'                   : [field: null, label: 'Invoice dispatch via', message: 'vendor.invoicing.dispatch.label'],
                                'vendor.paperInvoice'                       : [field: 'paperInvoice', label: 'Paper invoice', message: 'vendor.invoicing.paperInvoice.label'],
                                'vendor.managementOfCredits'                : [field: 'managementOfCredits', label: 'Management of credits', message: 'vendor.invoicing.managementOfCredits.label'],
                                'vendor.processingOfCompensationPayments'   : [field: 'processingOfCompensationPayments', label: 'Processing of compensation payments (credits/subsequent debits)', message: 'vendor.invoicing.compensationPayments.label'],
                                'vendor.individualInvoiceDesign'            : [field: 'individualInvoiceDesign', label: 'Individual invoice design', message: 'vendor.invoicing.individualInvoiceDesign.label']
                        ]
                ],
                providerContacts : [
                        label: 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields: [:]
                ],
                providerAddresses : [
                        label: 'Addresses',
                        message: 'org.addresses.label',
                        fields: [:]
                ],
                myProviderProperties : [
                        label: 'Properties',
                        message: 'default.properties.my',
                        fields: [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportVendorConfig(){
        return [
                vendor : [
                        label: 'Vendor',
                        message: 'vendor.label',
                        fields: [
                                'vendor.name'                  : [field: 'name', label: 'Name', message: 'default.name.label', defaultChecked: 'true' ],
                                'vendor.sortname'              : [field: 'sortname', label: 'Sortname', message: 'default.shortname.label', defaultChecked: 'true'],
                                'vendor.altnames'              : [field: 'altnames', label: 'Alternative names', message: 'altname.plural'],
                                'vendor.status'                : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: true],
                                'vendor.homepage'              : [field: 'homepage', label: 'Homepage URL', message: 'org.homepage.label', defaultChecked: true],
                                'vendor.packages'              : [field: null, label: 'Packages', message:'package.plural', defaultChecked: true],
                                'vendor.platforms'             : [field: null, label: 'Platforms', message: 'org.platforms.label', defaultChecked: true],
                                'vendor.subscriptions'         : [field: null, label: 'Subscriptions', message: 'subscription.plural', defaultChecked: true],
                                'vendor.licenses'              : [field: null, label: 'Licenses', message: 'license.plural', defaultChecked: true]
                        ]
                ],
                vendorOrders : [
                        label: 'Ordering',
                        message: 'vendor.ordering.header',
                        fields: [
                                'vendor.webShopOrders'         : [field: 'webShopOrders', label: 'Order via Webshop', message: 'vendor.ordering.webshop.label'],
                                'vendor.xmlOrders'             : [field: 'xmlOrders', label: 'Order via XML', message: 'vendor.ordering.xml.label'],
                                'vendor.ediOrders'             : [field: 'ediOrders', label: 'Order via EDI', message: 'vendor.ordering.edi.label'],
                                'vendor.supportedLibrarySystems': [field: null, label: 'Supported library systems', message: 'vendor.ordering.supportedLibrarySystems.label'],
                                'vendor.electronicDeliveryDelays': [field: null, label: 'Electronic delivery delay notification via', message: 'vendor.ordering.electronicDeliveryDelayNotifications.label']
                        ]
                ],
                vendorInvoicing : [
                        label: 'Invoicing',
                        message: 'vendor.invoicing.header',
                        fields: [
                                'vendor.electronicBillings'                 : [field: null, label: 'Electronic invoice formats', message: 'vendor.invoicing.formats.label'],
                                'vendor.invoiceDispatchs'                   : [field: null, label: 'Invoice dispatch via', message: 'vendor.invoicing.dispatch.label'],
                                'vendor.paperInvoice'                       : [field: 'paperInvoice', label: 'Paper invoice', message: 'vendor.invoicing.paperInvoice.label'],
                                'vendor.managementOfCredits'                : [field: 'managementOfCredits', label: 'Management of credits', message: 'vendor.invoicing.managementOfCredits.label'],
                                'vendor.processingOfCompensationPayments'   : [field: 'processingOfCompensationPayments', label: 'Processing of compensation payments (credits/subsequent debits)', message: 'vendor.invoicing.compensationPayments.label'],
                                'vendor.individualInvoiceDesign'            : [field: 'individualInvoiceDesign', label: 'Individual invoice design', message: 'vendor.invoicing.individualInvoiceDesign.label']
                        ]
                ],
                vendorGeneralServices : [
                        label: 'General services',
                        message: 'vendor.general.header',
                        fields: [
                                'vendor.technicalSupport'                       : [field: 'technicalSupport', label: 'Technical support', message: 'vendor.general.technicalSupport.label'],
                                'vendor.shippingMetadata'                       : [field: 'shippingMetadata', label: 'Metadata (MARC records)', message: 'vendor.general.metadata.label'],
                                'vendor.forwardingUsageStatisticsFromPublisher' : [field: 'forwardingUsageStatisticsFromPublisher', label: 'Forwarding usage statistics from the publisher', message: 'vendor.general.usageStats.label'],
                                'vendor.activationForNewReleases'               : [field: 'activationForNewReleases', label: 'Update information about new releases within e-book packages', message: 'vendor.general.newReleaseInformation.label'],
                                'vendor.exchangeOfIndividualTitles'             : [field: 'exchangeOfIndividualTitles', label: 'Exchange of individual titles within e-book packages', message: 'vendor.general.exchangeIndividualTitles.label'],
                                'vendor.researchPlatformForEbooks'              : [field: 'researchPlatformForEbooks', label: 'Research platform for e-books', message: 'vendor.general.researchPlatform.label']
                        ]
                ],
                vendorSupplierInformation : [
                        label: 'Supplier information',
                        message: 'vendor.supplier.header',
                        fields: [
                                'vendor.prequalification'        : [field: 'prequalification', label: 'Prequalification', message: 'vendor.supplier.prequalification.label'],
                                'vendor.prequalificationInfo'    : [field: 'prequalificationInfo', label: 'Info to Prequalification', message: 'vendor.supplier.infoPrequalification.label']
                        ]
                ],
                vendorIdentifiers : [
                        label: 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields: [:]
                ],
                vendorCustomerIdentifiers : [
                        label: 'Customer Identifiers',
                        message: 'exportClickMe.participantCustomerIdentifiers',
                        fields: [:]
                ],
                vendorContacts : [
                        label: 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields: [:]
                ],
                vendorAddresses : [
                        label: 'Addresses',
                        message: 'org.addresses.label',
                        fields: [:]
                ],
                myVendorProperties : [
                        label: 'Properties',
                        message: 'default.properties.my',
                        fields: [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportAddressConfig(){
        return [
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
                                'organisation': [field: 'organisation', label: 'Organisation', message: 'address.org.label', defaultChecked: true],
                                'receiver' : [field: 'receiver', label: 'Receiver', message: 'address.receiver.label', defaultChecked: true],
                                'additionFirst': [field: 'additionFirst', label: 'First Addition', message: 'address.additionFirst.label'],
                                'additionSecond': [field: 'additionSecond', label: 'Second Addition', message: 'address.additionSecond.label'],
                                'street_1': [field: 'street_1', label: 'Street', message: 'address.street_1.label', defaultChecked: true],
                                'street_2': [field: 'street_2', label: 'Number', message: 'address.street_2.label', defaultChecked: true],
                                'zipcode': [field: 'zipcode', label: 'Postcode', message: 'address.zipcode.label', defaultChecked: true],
                                'city': [field: 'city', label: 'City', message: 'address.city.label', defaultChecked: true],
                                'pob': [field: 'pob', label: 'Postal box', message: 'address.pob.label'],
                                'pobZipcode': [field: 'pobZipcode', label: 'Postal box zip code', message: 'address.pobZipcode.label'],
                                'pobCity': [field: 'pobCity', label: 'Postal box city', message: 'address.pobCity.label'],
                                'country': [field: 'country', label: 'Country', message: 'address.country.label', defaultChecked: true],
                                'region': [field: 'region', label: 'Region', message: 'address.region.label'],
                        ]
                ]
        ]
    }

    Map<String, Object> getDefaultExportAddressFilter() {
        return [
                function: [
                        label  : 'Function',
                        message: 'person.function.label',
                        fields : [:]
                ],
                position: [
                        label  : 'Position',
                        message: 'person.position.label',
                        fields : [:]
                ],
                type    : [
                        label  : 'Type',
                        message: 'default.type.label',
                        fields : [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportSurveyEvaluation() {
        return [
                //Wichtig: Hier bei dieser Config bitte drauf achten, welche Feld Bezeichnung gesetzt ist,
                // weil die Felder von einer zusammengesetzten Map kommen. siehe ExportClickMeService -> exportSurveyEvaluation
                survey                        : [
                        label  : 'Survey',
                        message: 'survey.label',
                        fields : [
                                'participant.sortname'   : [field: 'participant.sortname', label: 'Sortname', message: 'org.sortname.label', defaultChecked: 'true'],
                                'participant.name'       : [field: 'participant.name', label: 'Name', message: 'default.name.label', defaultChecked: 'true'],
                                'survey.surveyPerson'    : [field: null, label: 'Survey Contact', message: 'surveyOrg.surveyContacts', defaultChecked: 'true'],
                                'survey.ownerComment'    : [field: null, label: 'Owner Comment', message: 'surveyResult.commentOnlyForOwner', defaultChecked: 'true'],
                                'survey.finishDate'      : [field: null, label: 'Finish Date', message: 'surveyInfo.finishedDate', defaultChecked: 'true'],
                                'survey.reminderMailDate': [field: null, label: 'Reminder Mail Date', message: 'surveyOrg.reminderMailDate'],
                        ]
                ],

                participantSurveyCostItems    : [
                        label  : 'Cost Items',
                        message: 'exportClickMe.survey.costItems',
                        fields : [
                                'costItemsElements'                                      : [:],
                                'participantSurveyCostItem.costTitle'                    : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'participantSurveyCostItem.costItemElementConfiguration' : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                                'participantSurveyCostItem.costItemStatus'               : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                                'participantSurveyCostItem.costInBillingCurrency'        : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                                'participantSurveyCostItem.billingCurrency'              : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                                'participantSurveyCostItem.costInBillingCurrencyAfterTax': [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                                'participantSurveyCostItem.taxType'                      : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                                'participantSurveyCostItem.taxRate'                      : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                                'participantSurveyCostItem.startDate'                    : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'participantSurveyCostItem.endDate'                      : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],
                                'participantSurveyCostItem.costDescription'              : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label']
                        ]
                ],

                participantSurveySubCostItems : [
                        label  : 'Cost Items',
                        message: 'exportClickMe.subscription.costItems',
                        fields : [
                                'costItemsElements'                                   : [:],
                                'participantSubCostItem.costTitle'                    : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'participantSubCostItem.reference'                    : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                                'participantSubCostItem.budgetCodes'                  : [field: 'costItem.budgetcodes.value', label: 'Budget Code', message: 'financials.budgetCode'],
                                'participantSubCostItem.costItemElementConfiguration' : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                                'participantSubCostItem.costItemStatus'               : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                                'participantSubCostItem.costInBillingCurrency'        : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                                'participantSubCostItem.billingCurrency'              : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                                'participantSubCostItem.costInBillingCurrencyAfterTax': [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                                'participantSubCostItem.currencyRate'                 : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                                'participantSubCostItem.taxType'                      : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                                'participantSubCostItem.taxRate'                      : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                                'participantSubCostItem.costInLocalCurrency'          : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                                'participantSubCostItem.costInLocalCurrencyAfterTax'  : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                                'participantSubCostItem.datePaid'                     : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                                'participantSubCostItem.financialYear'                : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                                'participantSubCostItem.invoiceDate'                  : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                                'participantSubCostItem.startDate'                    : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'participantSubCostItem.endDate'                      : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                                'participantSubCostItem.costDescription'              : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                                'participantSubCostItem.invoiceNumber'                : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                                'participantSubCostItem.orderNumber'                  : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
                                'participantSubCostItem.pkg'                          : [field: 'costItem.pkg.name', label: 'Package Name', message: 'package.label'],
                                'participantSubCostItem.issueEntitlement'             : [field: 'costItem.issueEntitlement.tipp.name', label: 'Title', message: 'issueEntitlement.label'],
                                'participantSubCostItem.issueEntitlementGroup'        : [field: 'costItem.issueEntitlementGroup.name', label: 'Title Group Name', message: 'package.label'],
                        ]
                ],

                participant                   : [
                        label  : 'Participant',
                        message: 'surveyParticipants.label',
                        fields : [
                                'participant.funderType'         : [field: 'participant.funderType', label: 'Funder Type', message: 'org.funderType.label'],
                                'participant.funderHskType'      : [field: 'participant.funderHskType', label: 'Funder Hsk Type', message: 'org.funderHSK.label'],
                                'participant.libraryType'        : [field: 'participant.libraryType', label: 'Library Type', message: 'org.libraryType.label'],
                                'participant.url'                : [field: 'participant.url', label: 'URL', message: 'default.url.label'],
                                'participant.legalPatronName'    : [field: 'participant.legalPatronName', label: 'Lagal Patron Name', message: 'org.legalPatronName.label'],
                                'participant.urlGov'             : [field: 'participant.urlGov', label: 'URL of governing institution', message: 'org.urlGov.label'],
                                /*
                            'participantContact.General contact person'    : [field: null, label: 'General Contact Person', message: 'org.mainContact.label'],
                            'participantContact.Functional Contact Billing Adress'    : [field: null, label: 'Functional Contact Billing Adress', message: 'org.functionalContactBillingAdress.label'],
                            'participant.postAdress'        : [field: null, label: 'Post Adress', message: 'addressFormModalPostalAddress'],
                            'participant.billingAdress'     : [field: null, label: 'Billing Adress', message: 'addressFormModalBillingAddress'],
                             */
                                'participant.eInvoice'           : [field: 'participant.eInvoice', label: 'eInvoice', message: 'org.eInvoice.label'],
                                'participant.eInvoicePortal'     : [field: 'participant.eInvoicePortal', label: 'eInvoice Portal', message: 'org.eInvoicePortal.label'],
                                'participant.linkResolverBaseURL': [field: 'participant.linkResolverBaseURL', label: 'Link Resolver Base URL', message: 'org.linkResolverBase.label'],
                                'participant.readerNumbers'      : [field: null, label: 'Reader Numbers', message: 'menu.institutions.readerNumbers'],
                                'participant.libraryNetwork'     : [field: 'participant.libraryNetwork', label: 'Library Network', message: 'org.libraryNetwork.label'],
                                'participant.country'            : [field: 'participant.country', label: 'Country', message: 'org.country.label'],
                                'participant.region'             : [field: 'participant.region', label: 'Region', message: 'org.region.label'],
                                'participant.uuid'               : [field: 'participant.globalUID', label: 'Laser-UUID', message: null],
                        ]
                ],
                participantAccessPoints       : [
                        label  : 'Participants Access Points',
                        message: 'exportClickMe.participantAccessPoints',
                        fields : [
                                'participant.exportIPs'        : [field: null, label: 'Export IPs', message: 'subscriptionDetails.members.exportIPs', separateSheet: 'true'],
                                'participant.exportProxys'     : [field: null, label: 'Export Proxys', message: 'subscriptionDetails.members.exportProxys', separateSheet: 'true'],
                                'participant.exportEZProxys'   : [field: null, label: 'Export EZProxys', message: 'subscriptionDetails.members.exportEZProxys', separateSheet: 'true'],
                                'participant.exportShibboleths': [field: null, label: 'Export Shibboleths', message: 'subscriptionDetails.members.exportShibboleths', separateSheet: 'true'],
                        ]
                ],
                participantIdentifiers        : [
                        label  : 'Identifiers',
                        message: 'exportClickMe.participantIdentifiers',
                        fields : [:],
                ],
                participantCustomerIdentifiers: [
                        label  : 'Customer Identifiers',
                        message: 'exportClickMe.participantCustomerIdentifiers',
                        fields : [:],
                ],
                participantContacts           : [
                        label  : 'Contacts',
                        message: 'org.contacts.label',
                        subTabs: [],
                        fields : [:]
                ],
                participantAddresses          : [
                        label  : 'Addresses',
                        message: 'org.addresses.label',
                        fields : [:]
                ],

                subscription                  : [
                        label  : 'Subscription',
                        message: 'subscription.label',
                        fields : [
                                'subscription.name'                  : [field: 'sub.name', label: 'Name', message: 'subscription.name.label'],
                                'subscription.startDate'             : [field: 'sub.startDate', label: 'Start Date', message: 'subscription.startDate.label'],
                                'subscription.endDate'               : [field: 'sub.endDate', label: 'End Date', message: 'subscription.endDate.label'],
                                'subscription.manualCancellationDate': [field: 'sub.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                'subscription.isMultiYear'           : [field: 'sub.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label'],
                                'subscription.referenceYear'         : [field: 'sub.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                                'subscription.status'                : [field: 'sub.status', label: 'Status', message: 'subscription.status.label'],
                                'subscription.kind'                  : [field: 'sub.kind', label: 'Kind', message: 'subscription.kind.label'],
                                'subscription.form'                  : [field: 'sub.form', label: 'Form', message: 'subscription.form.label'],
                                'subscription.resource'              : [field: 'sub.resource', label: 'Resource', message: 'subscription.resource.label'],
                                'subscription.hasPerpetualAccess'    : [field: 'sub.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                                'subscription.hasPublishComponent'   : [field: 'sub.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                                'subscription.holdingSelection'      : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
                                'subscription.uuid'                  : [field: 'sub.globalUID', label: 'Laser-UUID', message: null],
                        ]
                ],

        ]
    }

    Map<String, Object> getDefaultExportIssueEntitlementConfig() {
        return [
                issueEntitlement           : [
                        label  : 'IssueEntitlement',
                        message: 'issueEntitlement.label',
                        fields : [
                                'issueEntitlement.tipp.name'                : [field: 'tipp.name', label: 'Name', message: 'tipp.name', defaultChecked: 'true', sqlCol: 'tipp_name'],
                                'issueEntitlement.status'                   : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: 'true', sqlCol: 'ie_status_rv_fk'],
                                'issueEntitlement.tipp.medium'              : [field: 'tipp.medium', label: 'Status', message: 'tipp.medium', defaultChecked: 'true', sqlCol: 'tipp_medium_rv_fk'],
                                'issueEntitlement.accessStartDate'          : [field: 'accessStartDate', label: 'Access Start Date', message: 'tipp.accessStartDate', defaultChecked: 'true', sqlCol: 'ie_access_start_date'],
                                'issueEntitlement.accessEndDate'            : [field: 'accessEndDate', label: 'Access End Date', message: 'tipp.accessEndDate', defaultChecked: 'true', sqlCol: 'ie_access_end_date'],
                                'issueEntitlement.tipp.titleType'           : [field: 'tipp.titleType', label: 'Title Type', message: 'tipp.titleType', defaultChecked: 'true', sqlCol: 'tipp_title_type'],
                                'issueEntitlement.tipp.pkg'                 : [field: 'tipp.pkg.name', label: 'Package', message: 'package.label', defaultChecked: 'true', sqlCol: 'pkg_name'],
                                'issueEntitlement.tipp.platform.name'       : [field: 'tipp.platform.name', label: 'Platform', message: 'tipp.platform', defaultChecked: 'true', sqlCol: 'plat_name'],
                                'issueEntitlement.tipp.ieGroup.name'        : [field: 'ieGroups.ieGroup.name', label: 'Group', message: 'issueEntitlementGroup.label', defaultChecked: 'true', sqlCol: 'ig_name'],
                                'issueEntitlement.tipp.perpetualAccessBySub': [field: 'perpetualAccessBySub', label: 'Perpetual Access', message: 'issueEntitlement.perpetualAccessBySub.label', defaultChecked: 'true'],
                        ]
                ],
                titleDetails               : [
                        label  : 'Title Details',
                        message: 'title.details',
                        fields : [
                                'issueEntitlement.tipp.hostPlatformURL' : [field: 'tipp.hostPlatformURL', label: 'Url', message: null, sqlCol: 'tipp_host_platform_url'],
                                'issueEntitlement.tipp.dateFirstOnline' : [field: 'tipp.dateFirstOnline', label: 'Date first online', message: 'tipp.dateFirstOnline', sqlCol: 'tipp_date_first_online'],
                                'issueEntitlement.tipp.dateFirstInPrint': [field: 'tipp.dateFirstInPrint', label: 'Date first in print', message: 'tipp.dateFirstInPrint', sqlCol: 'tipp_date_first_in_print'],
                                'issueEntitlement.tipp.firstAuthor'     : [field: 'tipp.firstAuthor', label: 'First Author', message: 'tipp.firstAuthor', sqlCol: 'tipp_first_author'],
                                'issueEntitlement.tipp.firstEditor'     : [field: 'tipp.firstEditor', label: 'First Editor', message: 'tipp.firstEditor', sqlCol: 'tipp_first_editor'],
                                'issueEntitlement.tipp.volume'          : [field: 'tipp.volume', label: 'Volume', message: 'tipp.volume', sqlCol: 'tipp_volume'],
                                'issueEntitlement.tipp.editionStatement': [field: 'tipp.editionStatement', label: 'Edition Statement', message: 'title.editionStatement.label', sqlCol: 'tipp_edition_statement'],
                                'issueEntitlement.tipp.editionNumber'   : [field: 'tipp.editionNumber', label: 'Edition Number', message: 'tipp.editionNumber', sqlCol: 'tipp_edition_number'],
                                'issueEntitlement.tipp.summaryOfContent': [field: 'tipp.summaryOfContent', label: 'Summary of Content', message: 'title.summaryOfContent.label', sqlCol: 'tipp_summary_of_content'],
                                'issueEntitlement.tipp.seriesName'      : [field: 'tipp.seriesName', label: 'Series Name', message: 'tipp.seriesName', sqlCol: 'tipp_series_name'],
                                'issueEntitlement.tipp.subjectReference': [field: 'tipp.subjectReference', label: 'Subject Reference', message: 'tipp.subjectReference', sqlCol: 'tipp_subject_reference'],
                                'issueEntitlement.tipp.delayedOA'       : [field: 'tipp.delayedOA', label: 'Delayed OA', message: 'tipp.delayedOA', sqlCol: 'tipp_delayedoa_rv_fk'],
                                'issueEntitlement.tipp.hybridOA'        : [field: 'tipp.hybridOA', label: 'Hybrid OA', message: 'tipp.hybridOA', sqlCol: 'tipp_hybridoa_rv_fk'],
                                'issueEntitlement.tipp.publisherName'   : [field: 'tipp.publisherName', label: 'Publisher', message: 'tipp.publisher', sqlCol: 'tipp_publisher_name'],
                                'issueEntitlement.tipp.accessType'      : [field: 'tipp.accessType', label: 'Access Type', message: 'tipp.accessType', sqlCol: 'tipp_access_type_rv_fk'],
                                'issueEntitlement.tipp.openAccess'      : [field: 'tipp.openAccess', label: 'Open Access', message: 'tipp.openAccess', sqlCol: 'tipp_open_access_rv_fk'],
                                'issueEntitlement.tipp.ddcs'            : [field: 'tipp.ddcs', label: 'DDCs', message: 'tipp.ddc'],
                                'issueEntitlement.tipp.languages'       : [field: 'tipp.languages', label: 'Languages', message: 'tipp.language'],
                                'issueEntitlement.tipp.providers'       : [field: 'tipp.providers', label: 'Providers', message: 'tipp.provider']
                        ]
                ],
                coverage                   : [
                        label  : 'Coverage',
                        message: 'tipp.coverage',
                        fields : [
                                'coverage.startDate'    : [field: 'startDate', label: 'Start Date', message: 'tipp.startDate', sqlCol: 'tc_start_date'],
                                'coverage.startVolume'  : [field: 'startVolume', label: 'Start Volume', message: 'tipp.startVolume', sqlCol: 'tc_start_volume'],
                                'coverage.startIssue'   : [field: 'startIssue', label: 'Start Issue', message: 'tipp.startIssue', sqlCol: 'tc_start_issue'],
                                'coverage.endDate'      : [field: 'endDate', label: 'End Date', message: 'tipp.endDate', sqlCol: 'tc_end_date'],
                                'coverage.endVolume'    : [field: 'endVolume', label: 'End Volume', message: 'tipp.endVolume', sqlCol: 'tc_end_volume'],
                                'coverage.endIssue'     : [field: 'endIssue', label: 'End Issue', message: 'tipp.endIssue', sqlCol: 'tc_end_issue'],
                                'coverage.coverageNote' : [field: 'coverageNote', label: 'Coverage Note', message: 'default.note.label', sqlCol: 'tc_coverage_note'],
                                'coverage.coverageDepth': [field: 'coverageDepth', label: 'Coverage Depth', message: 'tipp.coverageDepth', sqlCol: 'tc_coverage_depth'],
                                'coverage.embargo'      : [field: 'embargo', label: 'Embargo', message: 'tipp.embargo', sqlCol: 'tc_embargo']
                        ]
                ],
                priceItem                  : [
                        label  : 'Price Item',
                        message: 'costItem.label',
                        fields : [
                                'listPriceEUR' : [field: null, label: 'List Price EUR', message: 'tipp.listprice_eur'],
                                'listPriceGBP' : [field: null, label: 'List Price GBP', message: 'tipp.listprice_gbp'],
                                'listPriceUSD' : [field: null, label: 'List Price USD', message: 'tipp.listprice_usd'],
                                'localPriceEUR': [field: null, label: 'Local Price EUR', message: 'tipp.localprice_eur'],
                                'localPriceGBP': [field: null, label: 'Local Price GBP', message: 'tipp.localprice_gbp'],
                                'localPriceUSD': [field: null, label: 'Local Price USD', message: 'tipp.localprice_usd']
                        ]
                ],
                issueEntitlementIdentifiers: [
                        label  : 'Identifiers',
                        message: 'identifier.plural',
                        fields : [:]
                ],

                subscription               : [
                        label  : 'Subscription',
                        message: 'subscription.label',
                        fields : [
                                'subscription.name'                  : [field: 'subscription.name', label: 'Name', message: 'subscription.name.label'],
                                'subscription.startDate'             : [field: 'subscription.startDate', label: 'Start Date', message: 'subscription.startDate.label'],
                                'subscription.endDate'               : [field: 'subscription.endDate', label: 'End Date', message: 'subscription.endDate.label'],
                                'subscription.manualCancellationDate': [field: 'subscription.manualCancellationDate', label: 'Manual Cancellation Date', message: 'subscription.manualCancellationDate.label'],
                                'subscription.isMultiYear'           : [field: 'subscription.isMultiYear', label: 'Multi Year', message: 'subscription.isMultiYear.label'],
                                'subscription.referenceYear'         : [field: 'subscription.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
                                'subscription.status'                : [field: 'subscription.status', label: 'Status', message: 'subscription.status.label'],
                                'subscription.kind'                  : [field: 'subscription.kind', label: 'Kind', message: 'subscription.kind.label'],
                                'subscription.form'                  : [field: 'subscription.form', label: 'Form', message: 'subscription.form.label'],
                                'subscription.resource'              : [field: 'subscription.resource', label: 'Resource', message: 'subscription.resource.label'],
                                'subscription.hasPerpetualAccess'    : [field: 'subscription.hasPerpetualAccess', label: 'Perpetual Access', message: 'subscription.hasPerpetualAccess.label'],
                                'subscription.hasPublishComponent'   : [field: 'subscription.hasPublishComponent', label: 'Publish Component', message: 'subscription.hasPublishComponent.label'],
                                'subscription.holdingSelection'      : [field: 'sub.holdingSelection', label: 'Holding Selection', message: 'subscription.holdingSelection.label'],
                                'subscription.uuid'                  : [field: 'subscription.globalUID', label: 'Laser-UUID', message: null],
                        ]
                ],
                subscriptionIdentifiers: [
                        label  : 'Subscription Identifiers',
                        message: 'subscription.identifiers.label',
                        fields : [:]
                ]
        ]
    }

    Map<String, Object> getDefaultExportTippConfig(){
        return [
                tipp      : [
                        label: 'Title',
                        message: 'default.title.label',
                        fields: [
                                'tipp.name'            : [field: 'name', label: 'Name', message: 'tipp.name', defaultChecked: 'true', sqlCol: 'tipp_name' ],
                                'tipp.status'          : [field: 'status', label: 'Status', message: 'default.status.label', defaultChecked: 'true', sqlCol: 'tipp_status_rv_fk'],
                                'tipp.medium'          : [field: 'medium', label: 'Status', message: 'tipp.medium', defaultChecked: 'true', sqlCol: 'tipp_medium_rv_fk'],
                                'tipp.titleType'       : [field: 'titleType', label: 'Title Type', message: 'tipp.titleType', defaultChecked: 'true', sqlCol: 'tipp_title_type'],
                                'tipp.pkg'             : [field: 'pkg.name', label: 'Package', message: 'package.label', defaultChecked: 'true', sqlCol: 'pkg_name'],
                                'tipp.platform.name'   : [field: 'platform.name', label: 'Platform', message: 'tipp.platform', defaultChecked: 'true', sqlCol: 'plat_name'],
                                'perpetualAccessBySub' : [field: null, label: 'Subscription', message: 'subscription', defaultChecked: 'true']
                        ]
                ],
                titleDetails      : [
                        label: 'Title Details',
                        message: 'title.details',
                        fields: [
                                'tipp.hostPlatformURL' : [field: 'hostPlatformURL', label: 'Url', message: null, sqlCol: 'tipp_host_platform_url'],
                                'tipp.dateFirstOnline' : [field: 'dateFirstOnline', label: 'Date first online', message: 'tipp.dateFirstOnline', sqlCol: 'tipp_date_first_online'],
                                'tipp.dateFirstInPrint' : [field: 'dateFirstInPrint', label: 'Date first in print', message: 'tipp.dateFirstInPrint', sqlCol: 'tipp_date_first_in_print'],
                                'tipp.firstAuthor'     : [field: 'firstAuthor', label: 'First Author', message: 'tipp.firstAuthor', sqlCol: 'tipp_first_author'],
                                'tipp.firstEditor'     : [field: 'firstEditor', label: 'First Editor', message: 'tipp.firstEditor', sqlCol: 'tipp_first_editor'],
                                'tipp.volume'          : [field: 'volume', label: 'Volume', message: 'tipp.volume', sqlCol: 'tipp_volume'],
                                'tipp.editionStatement': [field: 'editionStatement', label: 'Edition Statement', message: 'title.editionStatement.label', sqlCol: 'tipp_edition_statement'],
                                'tipp.editionNumber'   : [field: 'editionNumber', label: 'Edition Number', message: 'tipp.editionNumber', sqlCol: 'tipp_edition_number::text'],
                                'tipp.summaryOfContent': [field: 'summaryOfContent', label: 'Summary of Content', message: 'title.summaryOfContent.label', sqlCol: 'tipp_summary_of_content'],
                                'tipp.seriesName'      : [field: 'seriesName', label: 'Series Name', message: 'tipp.seriesName', sqlCol: 'tipp_series_name'],
                                'tipp.subjectReference': [field: 'subjectReference', label: 'Subject Reference', message: 'tipp.subjectReference', sqlCol: 'tipp_subject_reference'],
                                'tipp.delayedOA'       : [field: 'delayedOA', label: 'Delayed OA', message: 'tipp.delayedOA', sqlCol: 'tipp_delayedoa_rv_fk'],
                                'tipp.hybridOA'        : [field: 'hybridOA', label: 'Hybrid OA', message: 'tipp.hybridOA', sqlCol: 'tipp_hybridoa_rv_fk'],
                                'tipp.publisherName'   : [field: 'publisherName', label: 'Publisher', message: 'tipp.publisher', sqlCol: 'tipp_publisher_name'],
                                'tipp.accessType'      : [field: 'accessType', label: 'Access Type', message: 'tipp.accessType', sqlCol: 'tipp_access_type_rv_fk'],
                                'tipp.openAccess'      : [field: 'openAccess', label: 'Open Access', message: 'tipp.openAccess', sqlCol: 'tipp_open_access_rv_fk'],
                                'tipp.ddcs'            : [field: 'ddcs', label: 'DDCs', message: 'tipp.ddc', sqlCol: 'ddc'],
                                'tipp.languages'       : [field: 'languages', label: 'Languages', message: 'tipp.language', sqlCol: 'language']
                        ]
                ],
                coverage: [
                        label: 'Coverage',
                        message: 'tipp.coverage',
                        fields: [
                                'coverage.startDate'        : [field: 'startDate', label: 'Start Date', message: 'tipp.startDate', sqlCol: 'tc_start_date'],
                                'coverage.startVolume'      : [field: 'startVolume', label: 'Start Volume', message: 'tipp.startVolume', sqlCol:'tc_start_volume'],
                                'coverage.startIssue'       : [field: 'startIssue', label: 'Start Issue', message: 'tipp.startIssue', sqlCol:'tc_start_issue'],
                                'coverage.endDate'          : [field: 'endDate', label: 'End Date', message: 'tipp.endDate', sqlCol:'tc_end_date'],
                                'coverage.endVolume'        : [field: 'endVolume', label: 'End Volume', message: 'tipp.endVolume', sqlCol:'tc_end_volume'],
                                'coverage.endIssue'         : [field: 'endIssue', label: 'End Issue', message: 'tipp.endIssue', sqlCol:'tc_end_issue'],
                                'coverage.coverageNote'     : [field: 'coverageNote', label: 'Coverage Note', message: 'default.note.label', sqlCol:'tc_coverage_note'],
                                'coverage.coverageDepth'    : [field: 'coverageDepth', label: 'Coverage Depth', message: 'tipp.coverageDepth', sqlCol:'tc_coverage_depth'],
                                'coverage.embargo'          : [field: 'embargo', label: 'Embargo', message: 'tipp.embargo', sqlCol:'tc_embargo']
                        ]
                ],
                priceItem: [
                        label: 'Price Item',
                        message: 'costItem.label',
                        fields: [
                                'listPriceEUR'    : [field: null, label: 'List Price EUR', message: 'tipp.listprice_eur', sqlCol: 'list_price_eur'],
                                'listPriceGBP'    : [field: null, label: 'List Price GBP', message: 'tipp.listprice_gbp', sqlCol: 'list_price_gbp'],
                                'listPriceUSD'    : [field: null, label: 'List Price USD', message: 'tipp.listprice_usd', sqlCol: 'list_price_usd'],
                                /*'localPriceEUR'   : [field: null, label: 'Local Price EUR', message: 'tipp.localprice_eur', sqlCol: 'local_price_eur'],
                                'localPriceGBP'   : [field: null, label: 'Local Price GBP', message: 'tipp.localprice_gbp', sqlCol: 'local_price_gbp'],
                                'localPriceUSD'   : [field: null, label: 'Local Price USD', message: 'tipp.localprice_usd', sqlCol: 'local_price_usd']*/
                        ]
                ],
                tippIdentifiers : [
                        label: 'Identifiers',
                        message: 'identifier.plural',
                        fields: [:]
                ],
        ]
    }

    /**
     * Gets the fields for the subscription renewal for the given survey for processing
     * @param surveyConfig the survey to which the renewal fields should be generated
     * @return the configuration map for the survey
     */
    Map<String, Object> getExportRenewalFields(SurveyConfig surveyConfig) {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        getDefaultExportSurveyRenewalConfig().keySet().each {
            getDefaultExportSurveyRenewalConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns, defaultChecked:  it.ns == 'VAT' ? true : false])
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [subscription: surveyConfig.subscription]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }
        Org contextOrg = contextService.getOrg()
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        def removeSurveyProperties = exportFields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            exportFields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            exportFields.put("surveyProperty."+surveyConfigProperties.surveyProperty.id, [field: null, label: "${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true'])
        }

        exportFields.put("surveyPropertyParticipantComment", [field: null, label: "${messageSource.getMessage('surveyResult.participantComment.export', null, locale)}", defaultChecked: 'true'])
        exportFields.put("surveyPropertyCommentOnlyForOwner", [field: null, label: "${messageSource.getMessage('surveyResult.commentOnlyForOwner.export', null, locale)}", defaultChecked: 'true'])

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

/*        if(surveyConfig.subscription) {
            CostItem.executeQuery('from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig)', [status: RDStore.COST_ITEM_DELETED, surveyConfig: surveyConfig]).groupBy {it.costItemElement.id}.each {
                exportFields.put("renewalSurveyCostItem.${it.key}", [field: null, label: RefdataValue.get(it.key).getI10n('value')])
            }
        }*/

        if(surveyConfig.vendorSurvey){
            exportFields.put("vendorSurvey", [field: null, label: "${messageSource.getMessage('surveyconfig.vendorSurvey.label', null, locale)}", defaultChecked: 'true'])
        }
        if(surveyConfig.subscriptionSurvey){
            exportFields.put("subscriptionSurvey", [field: null, label: "${messageSource.getMessage('surveyconfig.subscriptionSurvey.label', null, locale)}", defaultChecked: 'true'])
        }
        if(surveyConfig.packageSurvey){
            exportFields.put("packageSurvey", [field: null, label: "${messageSource.getMessage('surveyconfig.packageSurvey.label', null, locale)}", defaultChecked: 'true', separateSheet: 'true'])
        }
        if(surveyConfig.invoicingInformation){
            exportFields.put('survey.person', [field: null, label: 'Selected billing contact', message: 'surveyOrg.person.selected', defaultChecked: 'true'])
            exportFields.put('survey.address', [field: null, label: 'Selected billing address', message: 'surveyOrg.address.selected', defaultChecked: 'true'])
            exportFields.put('survey.eInvoicePortal', [field: null, label: 'Invoice receipt platform', message: 'surveyOrg.eInvoicePortal.label', defaultChecked: 'true'])
            exportFields.put('survey.eInvoiceLeitwegId', [field: null, label: ' Leit ID', message: 'surveyOrg.eInvoiceLeitwegId.label', defaultChecked: 'true'])
            exportFields.put('survey.eInvoiceLeitkriterium', [field: null, label: 'Leitkriterium', message: 'surveyOrg.eInvoiceLeitkriterium.label', defaultChecked: 'true'])
        }

        exportFields
    }

    /**
     * Gets the fields for the subscription renewal for the given survey and prepares them for the UI
     * @param surveyConfig the survey to which the renewal fields should be generated
     * @return the configuration map for the survey for the modal
     */
    Map<String, Object> getExportRenewalFieldsForUI(SurveyConfig surveyConfig) {

        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportSurveyRenewalConfig())
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        fields.participantIdentifiers.fields.clear()
        fields.participantCustomerIdentifiers.fields.clear()
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns, defaultChecked:  it.ns == 'VAT' ? true : false]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [subscription: surveyConfig.subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name, defaultChecked: 'true']]
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        Org contextOrg = contextService.getOrg()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc
        fields.participantContacts.fields.clear()
        fields.participantAddresses.fields.clear()
        fields.participantContacts.subTabs = subTabs
        fields.participantContacts.subTabActive = subTabActive
        contactTypes.each { RefdataValue contactType ->
            fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        def removeSurveyProperties = fields.survey.fields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            fields.survey.fields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            fields.survey.fields << ["surveyProperty.${surveyConfigProperties.surveyProperty.id}": [field: null, label: "${messageSource.getMessage('surveyProperty.label', null, locale)}: ${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true']]
        }

        fields.survey.fields << ["surveyPropertyParticipantComment": [field: null, label: "${messageSource.getMessage('surveyResult.participantComment.export', null, locale)}", defaultChecked: 'true']]
        fields.survey.fields << ["surveyPropertyCommentOnlyForOwner": [field: null, label: "${messageSource.getMessage('surveyResult.commentOnlyForOwner.export', null, locale)}", defaultChecked: 'true']]


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

/*        if(surveyConfig.subscription) {
            CostItem.executeQuery('from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig)', [status: RDStore.COST_ITEM_DELETED, surveyConfig: surveyConfig]).groupBy {it.costItemElement.id}.each {
                    fields.survey.fields.costItemsElements << ["renewalSurveyCostItems.${it.key}": [field: null, label: RefdataValue.get(it.key).getI10n('value')]]
                }
        }*/

        if(surveyConfig.vendorSurvey){
            fields.survey.fields << ["vendorSurvey": [field: null, label: "${messageSource.getMessage('surveyconfig.vendorSurvey.label', null, locale)}", defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('vendorSurvey')) {
                fields.survey.fields.remove('vendorSurvey')
            }
        }

        if(surveyConfig.subscriptionSurvey){
            fields.survey.fields << ["subscriptionSurvey": [field: null, label: "${messageSource.getMessage('surveyconfig.subscriptionSurvey.label', null, locale)}", defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('subscriptionSurvey')) {
                fields.survey.fields.remove('subscriptionSurvey')
            }
        }


        if(surveyConfig.packageSurvey){
            fields.survey.fields << ["packageSurvey":  [field: null, label: "${messageSource.getMessage('surveyconfig.packageSurvey.label', null, locale)}", defaultChecked: 'true', separateSheet: 'true']]
        }else {
            if(fields.survey.fields.containsKey('packageSurvey')) {
                fields.survey.fields.remove('packageSurvey')
            }
        }


        if(surveyConfig.invoicingInformation){
            fields.survey.fields << ['survey.person': [field: null, label: 'Selected billing contact', message: 'surveyOrg.person.selected', defaultChecked: 'true']]
            fields.survey.fields << ['survey.address': [field: null, label: 'Selected billing address', message: 'surveyOrg.address.selected', defaultChecked: 'true']]
            fields.survey.fields << ['survey.eInvoicePortal': [field: null, label: 'Invoice receipt platform', message: 'surveyOrg.eInvoicePortal.label', defaultChecked: 'true']]
            fields.survey.fields << ['survey.eInvoiceLeitwegId': [field: null, label: ' Leit ID', message: 'surveyOrg.eInvoiceLeitwegId.label', defaultChecked: 'true']]
            fields.survey.fields << ['survey.eInvoiceLeitkriterium': [field: null, label: 'Leitkriterium', message: 'surveyOrg.eInvoiceLeitkriterium.label', defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('survey.person')) {
                fields.survey.fields.remove('survey.person')
            }
            if(fields.survey.fields.containsKey('survey.address')) {
                fields.survey.fields.remove('survey.address')
            }
            if(fields.survey.fields.containsKey('survey.eInvoicePortal')) {
                fields.survey.fields.remove('survey.eInvoicePortal')
            }
            if(fields.survey.fields.containsKey('survey.eInvoiceLeitwegId')) {
                fields.survey.fields.remove('survey.eInvoiceLeitwegId')
            }
            if(fields.survey.fields.containsKey('survey.eInvoiceLeitkriterium')) {
                fields.survey.fields.remove('survey.eInvoiceLeitkriterium')
            }
        }

        fields
    }

    /**
     * Gets the subscription member export fields for the given subscription and contextOrg for processing
     * @param subscription the subscription whose members should be exported
     * @return the configuration map for the subscription member export
     */
    Map<String, Object> getExportSubscriptionMembersFields(Subscription subscription) {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        getDefaultExportSubscriptionMembersConfig().keySet().each {
            getDefaultExportSubscriptionMembersConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }
        List<Subscription> childSubs = []

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription) and ci.value != null', [subscription: subscription]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }
        if(subscription)
            childSubs.addAll(subscription.getNonDeletedDerivedSubscriptions())
        if(childSubs) {
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: contextOrg])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                exportFields.put("participantSubProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}"])
            }

            CostItem.findAllBySubInListAndCostItemStatusNotEqualAndCostItemElementIsNotNull(childSubs, RDStore.COST_ITEM_DELETED).groupBy {it.costItemElement.id}.each {
                exportFields.put("participantSubCostItem."+it.key, [field: null, label: RefdataValue.get(it.key).getI10n('value')])
            }
        }
        else {
            String consortiaQuery = "select s from OrgRole oo join oo.sub s where oo.org = :context and s.instanceOf != null"
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (${consortiaQuery}) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [context: contextOrg])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                exportFields.put("participantSubProperty.${propertyDefinition.id}", [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)])
            }

            CostItem.executeQuery('select ci.costItemElement from CostItem ci where ci.sub in ('+consortiaQuery+') and ci.costItemStatus != :deleted and ci.costItemElement != null', [context: contextOrg, deleted: RDStore.COST_ITEM_DELETED]).each { RefdataValue cie ->
                exportFields.put("participantSubCostItem.${cie.id}", [field: null, label: cie.getI10n('value')])
            }
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the subscription member export fields for the given subscription and contextOrg and prepares them for the UI
     * @param subscription the subscription whose members should be exported
     * @return the configuration map for the subscription member export for the UI
     */
    Map<String, Object> getExportSubscriptionMembersFieldsForUI(Subscription subscription = null) {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportSubscriptionMembersConfig())
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        fields.participantIdentifiers.fields.clear()
        fields.participantCustomerIdentifiers.fields.clear()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription) and ci.value != null order by plat.name',[subscription:subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }

        fields.participantSubProperties.fields.clear()
        fields.participantSubCostItems.fields.costItemsElements.clear()

        if(subscription) {
            List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
            if(childSubs) {
                String query = "select sp.type from SubscriptionProperty sp where sp.owner in (:subscriptionSet) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
                Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [subscriptionSet: childSubs, context: contextOrg])

                memberProperties.each {PropertyDefinition propertyDefinition ->
                    fields.participantSubProperties.fields << ["participantSubProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)]]
                }

                CostItem.findAllBySubInListAndCostItemStatusNotEqualAndCostItemElementIsNotNull(childSubs, RDStore.COST_ITEM_DELETED).groupBy {it.costItemElement.id}.each {
                    fields.participantSubCostItems.fields.costItemsElements << ["participantSubCostItem.${it.key}":[field: null, label: RefdataValue.get(it.key).getI10n('value')]]
                }
            }
        }
        else {
            String consortiaQuery = "select s from OrgRole oo join oo.sub s where oo.org = :context and s.instanceOf != null"
            String query = "select sp.type from SubscriptionProperty sp where sp.owner in (${consortiaQuery}) and sp.tenant = :context and sp.instanceOf = null order by sp.type.${localizedName} asc"
            Set<PropertyDefinition> memberProperties = PropertyDefinition.executeQuery(query, [context: contextOrg])

            memberProperties.each {PropertyDefinition propertyDefinition ->
                fields.participantSubProperties.fields << ["participantSubProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant != null)]]
            }

            CostItem.executeQuery('select ci.costItemElement from CostItem ci where ci.sub in ('+consortiaQuery+') and ci.costItemStatus != :deleted and ci.costItemElement != null', [context: contextOrg, deleted: RDStore.COST_ITEM_DELETED]).each { RefdataValue cie ->
                fields.participantSubCostItems.fields.costItemsElements << ["participantSubCostItem.${cie.id}":[field: null, label: cie.getI10n('value')]]
            }
        }

        Set<RefdataValue> contactTypes = []
        SortedSet<RefdataValue> addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.functionType.'+LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.positionType.'+LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.responsibilityType.'+LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc

        fields.participantContacts.fields.clear()
        fields.participantContacts.subTabs = subTabs
        fields.participantContacts.subTabActive = subTabActive
        fields.participantAddresses.fields.clear()
        contactTypes.each { RefdataValue contactType ->
            fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        fields
    }

    /**
     * Gets the subscription fields for the given contextOrg
     * @param showTransferFields should the subscription transfer fields be displayed as well?
     * @return the configuration map for the subscription export
     */
    Map<String, Object> getExportSubscriptionFields(boolean showTransferFields = false) {
        Org contextOrg = contextService.getOrg()
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

        Map<String, Object> config = contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportSubscriptionSupportConfig() : getDefaultExportSubscriptionConfig()

        config.keySet().each { String key ->
            if(key == 'institutions') {
                if (contextOrg.isCustomerType_Consortium()) {
                    config.get(key).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }
            }
            else {
                config.get(key).fields.each {
                    exportFields.put(it.key, it.value)
                }
            }
        }

        if(showTransferFields){
            getDefaultExportSubscriptionTransferConfig().keySet().each { String key ->
                getDefaultExportSubscriptionTransferConfig().get(key).fields.each {
                    exportFields.put(it.key, it.value)
                }
            }
        }

        if(contextOrg.getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]) {
            if (contextOrg.getCustomerType() == CustomerTypeService.ORG_INST_PRO) {
                exportFields.put('subscription.isAutomaticRenewAnnually', [field: 'isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
            }
            exportFields.put('subscription.consortium', [field: null, label: 'Consortium', message: 'consortium.label'])
            exportFields.put('license.consortium', [field: null, label: 'Consortium', message: 'consortium.label'])
        }

        //determine field configuration based on customer type
        Set<String> fieldKeyPrefixes = []
        switch(contextOrg.getCustomerType()) {
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

        IdentifierNamespace.findAllByNsType(Subscription.class.name, [sort: 'ns']).each {
            exportFields.put("identifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        String consortiaFilter = ''
        if(contextOrg.isCustomerType_Consortium())
            consortiaFilter = ' and s.instanceOf = null '
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx '+consortiaFilter+')', [ctx: contextOrg]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }
        IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_PACKAGE, [sort: 'ns']).each { IdentifierNamespace idns ->
            exportFields.put("packageIdentifiers."+idns.id, [field: null, label: idns.ns + "(${messageSource.getMessage('package', null, locale)})"])
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:contextOrg,availableTypes:[PropertyDefinition.SUB_PROP]])


        propList.each { PropertyDefinition propertyDefinition ->
            exportFields.put("subProperty." + propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
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
     * Gets the subscription fields for the given contextOrg for the UI
     * @param showTransferFields should the subscription transfer fields be displayed as well?
     * @return the configuration map for the subscription export for the UI
     */
    Map<String, Object> getExportSubscriptionFieldsForUI(boolean showTransferFields = false) {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:]

        fields.putAll(contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportSubscriptionSupportConfig() : getDefaultExportSubscriptionConfig())

        if(contextOrg.getCustomerType() == CustomerTypeService.ORG_INST_PRO)
            fields.subscription.fields.put('subscription.isAutomaticRenewAnnually', [field: 'isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
        if (!contextOrg.isCustomerType_Consortium()) {
            fields.remove('institutions')
            fields.subscription.fields.put('subscription.consortium', [field: null, label: 'Consortium', message: 'consortium.label', defaultChecked: true])
            fields.licenses.fields.put('license.consortium', [field: null, label: 'Consortium', message: 'exportClickMe.license.consortium', defaultChecked: true])
        }

        if(showTransferFields){
            fields << getDefaultExportSubscriptionTransferConfig() as Map
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

        IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where idns.nsType = :nsType order by coalesce(idns.'+localizedName+', idns.ns)', [nsType: IdentifierNamespace.NS_SUBSCRIPTION]).each {
            fields.identifiers.fields << ["identifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        String consortiaFilter = ''
        if(contextOrg.isCustomerType_Consortium())
            consortiaFilter = ' and s.instanceOf = null '
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx '+consortiaFilter+') order by plat.name', [ctx:contextOrg]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        if (fields.packages) {
            IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_PACKAGE, [sort: 'ns']).each { IdentifierNamespace idns ->
                fields.packages.fields << ["packageIdentifiers.${idns.id}": [field: null, label: idns.ns]]
            }
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc", [ctx:contextOrg,availableTypes:[PropertyDefinition.SUB_PROP]])

        propList.each { PropertyDefinition propertyDefinition ->
            //the proxies again ...
            if(propertyDefinition.tenant?.id == contextOrg.id)
                fields.mySubProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
            else
                fields.subProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
        }

        //determine tabs configuration based on customer type
        switch(contextOrg.getCustomerType()) {
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
     * Gets the consortia participation fields for the given contextOrg
     * @return the configuration map for the subscription export
     */
    Map<String, Object> getExportConsortiaParticipationFields() {
        Org contextOrg = contextService.getOrg()
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

        Map<String, Object> config = contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportConsortiaParticipationsSupportConfig() : getDefaultExportConsortiaParticipationsConfig()

        config.keySet().each { String key ->
            config.get(key).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        //determine field configuration based on customer type
        Set<String> fieldKeyPrefixes = ['own', 'cons']

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx and s.instanceOf = null)', [ctx: contextOrg]).each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }
        IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_PACKAGE, [sort: 'ns']).each { IdentifierNamespace idns ->
            exportFields.put("packageIdentifiers."+idns.id, [field: null, label: idns.ns + "(${messageSource.getMessage('package', null, locale)})"])
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:contextOrg,availableTypes:[PropertyDefinition.SUB_PROP]])


        propList.each { PropertyDefinition propertyDefinition ->
            exportFields.put("subProperty." + propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
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
     * Gets the export fields for the given contextOrg for the UI
     * @return the configuration map for the participation export for the UI
     */
    Map<String, Object> getExportConsortiaParticipationFieldsForUI() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:]
        fields.putAll(contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportConsortiaParticipationsSupportConfig() : getDefaultExportConsortiaParticipationsConfig())

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
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx and s.instanceOf = null) order by plat.name', [ctx:contextOrg]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        if (fields.packages) {
            IdentifierNamespace.findAllByNsType(IdentifierNamespace.NS_PACKAGE, [sort: 'ns']).each { IdentifierNamespace idns ->
                fields.packages.fields << ["packageIdentifiers.${idns.id}": [field: null, label: idns.ns]]
            }
        }

        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc

        fields.participantContacts.fields.clear()
        fields.participantContacts.subTabs = subTabs
        fields.participantContacts.subTabActive = subTabActive
        fields.participantAddresses.fields.clear()
        contactTypes.each { RefdataValue contactType ->
            fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc", [ctx:contextOrg,availableTypes:[PropertyDefinition.SUB_PROP]])

        propList.each { PropertyDefinition propertyDefinition ->
            //the proxies again ...
            if(propertyDefinition.tenant?.id == contextOrg.id)
                fields.mySubProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
            else
                fields.subProperties.fields << ["subProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
        }

        //determine tabs configuration based on customer type
        fields.subCostItems.subTabs = [[view: 'own', label: 'financials.tab.ownCosts'],[view: 'cons', label: 'financials.tab.consCosts']]
        fields.subCostItems.subTabActive = 'cons'

        Set<RefdataValue> elements = RefdataCategory.getAllRefdataValuesWithOrder(RDConstants.COST_ITEM_ELEMENT)
        elements.each { RefdataValue refdataValue ->
            fields.subCostItems.subTabs.view.each { String fieldKeyPrefix ->
                fields.subCostItems.fields << [("subCostItem." +fieldKeyPrefix+"."+ refdataValue.id): [field: null, label: refdataValue."${localizedValue}"]]
            }
        }

        fields
    }

    /**
     * Gets the license fields for the given contextOrg
     * @return the configuration map for the license export
     */
    Map<String, Object> getExportLicenseFields() {
        Org contextOrg = contextService.getOrg()
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

        Map<String, Object> config = contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportLicenseSupportConfig() : getDefaultExportLicenseConfig()

        config.keySet().each { String key ->
            if(key == 'institutions') {
                if (contextOrg.isCustomerType_Consortium()) {
                    config.get(key).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }
            }
            else {
                config.get(key).fields.each {
                    exportFields.put(it.key, it.value)
                }
            }
        }

        if(contextOrg.getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]) {
            exportFields.put('consortium', [field: null, label: 'Consortium', message: 'consortium.label'])
        }

        IdentifierNamespace.findAllByNsType(License.class.name, [sort: 'ns']).each {
            exportFields.put("identifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }

        /*
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        */

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc",
                [ctx:contextOrg,availableTypes:[PropertyDefinition.LIC_PROP]])


        propList.each { PropertyDefinition propertyDefinition ->
            exportFields.put("licProperty." + propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the license fields for the given contextOrg for the UI
     * @return the configuration map for the subscription export for the UI
     */
    Map<String, Object> getExportLicenseFieldsForUI() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:]
        fields.putAll(contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportLicenseSupportConfig() : getDefaultExportLicenseConfig())

        if (!contextOrg.isCustomerType_Consortium()) {
            fields.remove('institutions')
            fields.licenses.fields.put('consortium', [field: null, label: 'Consortium', message: 'consortium.label', defaultChecked: true])
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

        fields.licProperties.fields.clear()
        fields.myLicProperties.fields.clear()


        IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where idns.nsType = :nsType order by coalesce(idns.'+localizedName+', idns.ns)', [nsType: IdentifierNamespace.NS_LICENSE]).each {
            fields.identifiers.fields << ["identifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        /*
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        */

        Set<PropertyDefinition> propList = PropertyDefinition.executeQuery("select pd from PropertyDefinition pd where pd.descr in (:availableTypes) and (pd.tenant = null or pd.tenant = :ctx) order by pd."+localizedName+" asc", [ctx:contextOrg,availableTypes:[PropertyDefinition.LIC_PROP]])

        propList.each { PropertyDefinition propertyDefinition ->
            //the proxies again ...
            if(propertyDefinition.tenant?.id == contextOrg.id)
                fields.myLicProperties.fields << ["licProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
            else
                fields.licProperties.fields << ["licProperty.${propertyDefinition.id}": [field: null, label: propertyDefinition."${localizedName}", privateProperty: false]]
        }



        fields
    }

    /**
     * Gets the cost item fields for the given contextOrg. The export may be restricted to packages of a certain subscription
     * @param sub the {@link Subscription} whose packages should be included in the export
     * @return the configuration map for the cost item export
     */
    Map<String, Object> getExportCostItemFields(Subscription sub = null) {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        getDefaultExportCostItemConfig().keySet().each {
            getDefaultExportCostItemConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }
        Set<CostInformationDefinition> cifList = CostInformationDefinition.executeQuery("select cif from CostInformationDefinition cif where (cif.tenant = :ctx or cif.tenant = null) order by cif."+localizedName+" asc", [ctx:contextOrg])

        cifList.each { CostInformationDefinition costInformationDefinition ->
            exportFields.put("costInformation." + costInformationDefinition.id, [field: null, label: costInformationDefinition."${localizedName}", privateProperty: (costInformationDefinition.tenant?.id == contextOrg.id)])
        }

        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        if(contextOrg.getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]) {
            if(contextOrg.getCustomerType() == CustomerTypeService.ORG_INST_PRO) {
                exportFields.put('subscription.isAutomaticRenewAnnually', [field: 'sub.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
            }
            exportFields.put('subscription.consortium', [field: null, label: 'Consortium', message: 'consortium.label'])
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        Set<Platform> platforms
        if(sub) {
            platforms = Platform.executeQuery('select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :s', [s: sub])
        }
        else {
            platforms = Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx and s.instanceOf = null) order by plat.name', [ctx: contextOrg])
        }
        platforms.each { Platform plat ->
            exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the cost item fields for the given contextOrg. The export may be restricted to packages of a certain subscription
     * @param sub the {@link Subscription} whose packages should be included in the export
     * @return the configuration map for the cost item export for UI
     */
    Map<String, Object> getExportCostItemFieldsForUI(Subscription sub = null) {
        Org contextOrg = contextService.getOrg()

        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportCostItemConfig())
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        if(contextOrg.getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]) {
            if(contextOrg.getCustomerType() == CustomerTypeService.ORG_INST_PRO) {
                fields.subscription.fields.put('subscription.isAutomaticRenewAnnually', [field: 'sub.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
            }
            fields.subscription.fields.put('subscription.consortium', [field: null, label: 'Consortium', message: 'consortium.label', defaultChecked: true])
        }

        fields.costInformation.fields.clear()
        fields.myCostInformation.fields.clear()

        Set<CostInformationDefinition> cifList = CostInformationDefinition.executeQuery("select cif from CostInformationDefinition cif where (cif.tenant = :ctx or cif.tenant = null) order by cif."+localizedName+" asc", [ctx:contextOrg])

        cifList.each { CostInformationDefinition costInformationDefinition ->
            //the proxies again ...
            if(costInformationDefinition.tenant?.id == contextOrg.id)
                fields.myCostInformation.fields << ["costInformation.${costInformationDefinition.id}": [field: null, label: costInformationDefinition."${localizedName}", privateProperty: true]]
            else
                fields.costInformation.fields << ["costInformation.${costInformationDefinition.id}": [field: null, label: costInformationDefinition."${localizedName}", privateProperty: false]]
        }

        fields.participantIdentifiers.fields.clear()
        fields.participantCustomerIdentifiers.fields.clear()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc
        fields.participantContacts.fields.clear()
        fields.participantAddresses.fields.clear()
        fields.participantContacts.subTabs = subTabs
        fields.participantContacts.subTabActive = subTabActive
        contactTypes.each { RefdataValue contactType ->
            fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        Set<Platform> platforms
        if(sub) {
            platforms = Platform.executeQuery('select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :s', [s: sub])
        }
        else {
            platforms = Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg join sp.subscription s join s.orgRelations oo where oo.org = :ctx and s.instanceOf = null) order by plat.name', [ctx: contextOrg])
        }
        platforms.each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }



        fields
    }

    /**
     * Gets the cost item fields for the given contextOrg
     * @return the configuration map for the cost item export
     */
    Map<String, Object> getExportSurveyCostItemFields() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        getDefaultExportSurveyCostItemConfig().keySet().each {
            getDefaultExportSurveyCostItemConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the cost item fields for the given contextOrg
     * @return the configuration map for the cost item export for UI
     */
    Map<String, Object> getExportSurveyCostItemFieldsForUI() {
        Org contextOrg = contextService.getOrg()

        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportSurveyCostItemConfig())
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()

        fields.participantIdentifiers.fields.clear()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc
        fields.participantContacts.fields.clear()
        fields.participantAddresses.fields.clear()
        fields.participantContacts.subTabs = subTabs
        fields.participantContacts.subTabActive = subTabActive
        contactTypes.each { RefdataValue contactType ->
            fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }



        fields
    }

    /**
     * Gets the organisation fields for the given perspective configuration
     * @param config the organisation type to be exported
     * @return the configuration map for the organisation export
     */
    Map<String, Object> getExportOrgFields(String config) {

//        println 'getExportOrgFields'
//        println config

        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))

        switch(config) {
            case 'consortium':
                getDefaultExportConsortiaConfig().keySet().each {
                    getDefaultExportConsortiaConfig().get(it).fields.each {
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
                    exportFields.put("consortiumContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
                }
                addressTypes.each { RefdataValue addressType ->
                    exportFields.put("consortiumAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
                    exportFields.put("consortiumAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
                }
                break
            case 'institution':
                Map<String, Object> config2 = contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportOrgSupportConfig() : getDefaultExportOrgConfig()

                config2.keySet().each {
                    config2.get(it).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }

                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
                }
                exportFields.put("participant.uuid", [field: 'globalUID', label: 'Laser-UUID',  message: null])

                Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
                    exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
                }

                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    exportFields.put("participantProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
                }
                contactTypes.each { RefdataValue contactType ->
                    exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
                }
                addressTypes.each { RefdataValue addressType ->
                    exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
                    exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
                }
                break
            case 'member':
                getDefaultExportOrgConfig().keySet().each {
                    getDefaultExportOrgConfig().get(it).fields.each {
                        exportFields.put(it.key, it.value)
                    }
                }
                exportFields.put('participant.subscriptions', [field: null, label: 'Current subscription count',  message: 'subscription.plural.current.count'])

                IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
                    exportFields.put("participantIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
                }
                exportFields.put("participant.uuid", [field: 'globalUID', label: 'Laser-UUID',  message: null])

                Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
                    exportFields.put("participantCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
                }

                PropertyDefinition.findAllPublicAndPrivateOrgProp(contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
                    exportFields.put("participantProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
                }
                contactTypes.each { RefdataValue contactType ->
                    exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
                }
                addressTypes.each { RefdataValue addressType ->
                    exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
                    exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
                }
                break
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the organisation fields for the given perspective configuration for the UI
     * @param customerType the organisation type to be exported
     * @return the configuration map for the organisation export for UI
     */
    Map<String, Object> getExportOrgFieldsForUI(String customerType) {

        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()
        Set<RefdataValue> contactTypes = []
        SortedSet<RefdataValue> addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.functionType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.positionType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.responsibilityType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc

        switch (customerType) {
            case 'consortium': fields.putAll(getDefaultExportConsortiaConfig())
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
                fields.consortiumContacts.fields.clear()
                fields.consortiumContacts.subTabs = subTabs
                fields.consortiumContacts.subTabActive = subTabActive
                fields.consortiumAddresses.fields.clear()
                contactTypes.each { RefdataValue contactType ->
                    fields.consortiumContacts.fields.put("consortiumContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
                }
                addressTypes.each { RefdataValue addressType ->
                    fields.consortiumAddresses.fields.put("consortiumAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
                    fields.consortiumAddresses.fields.put("consortiumAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
                }
                break
            case 'institution':
                fields.putAll(contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? getDefaultExportOrgSupportConfig() : getDefaultExportOrgConfig())

                fields.participant.fields << ['participant.subscriptions':[field: null, label: 'Current subscriptions count',  message: 'subscription.plural.current.count']]
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
                fields.participantContacts.subTabs = subTabs
                fields.participantContacts.subTabActive = subTabActive
                fields.participantAddresses.fields.clear()
                contactTypes.each { RefdataValue contactType ->
                    fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
                }
                addressTypes.each { RefdataValue addressType ->
                    fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
                    fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
                }
                break
            default: fields = [:]
                break
        }



        fields
    }

    Map<String, Object> getExportVendorFields() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))

        getDefaultExportVendorConfig().keySet().each {
            getDefaultExportVendorConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_PROVIDER_NS).each {
            exportFields.put("vendorIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        exportFields.put("vendor.uuid", [field: 'globalUID', label: 'Laser-UUID',  message: null])
        exportFields.put("vendor.wekbId", [field: 'gokbId', label: 'we:kb-ID',  message: null])

        Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
            exportFields.put("vendorCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.VEN_PROP], contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
            exportFields.put("vendorProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
        }
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("vendorContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("vendorAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("vendorAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        exportFields
    }

    Map<String, Object> getExportVendorFieldsForUI() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()
        Set<RefdataValue> contactTypes = []
        SortedSet<RefdataValue> addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.functionType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.positionType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.responsibilityType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc
        fields.putAll(getDefaultExportVendorConfig())
        fields.vendorIdentifiers.fields.clear()
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_PROVIDER_NS, [sort: 'ns']).each {
            fields.vendorIdentifiers.fields << ["vendorIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        fields.vendorIdentifiers.fields << ['vendor.uuid':[field: 'globalUID', label: 'Laser-UUID',  message: null]]
        fields.vendorIdentifiers.fields << ['vendor.wekbId':[field: 'gokbId', label: 'we:kb-ID',  message: null]]
        fields.vendorCustomerIdentifiers.fields.clear()
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx) order by plat.name', contextParams).each { Platform plat ->
            fields.vendorCustomerIdentifiers.fields << ["vendorCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        fields.myVendorProperties.fields.clear()
        PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.VEN_PROP], contextOrg).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
            if(propertyDefinition.tenant?.id == contextOrg.id)
                fields.myVendorProperties.fields << ["vendorProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
        }
        fields.vendorContacts.fields.clear()
        fields.vendorContacts.subTabs = subTabs
        fields.vendorContacts.subTabActive = subTabActive
        contactTypes.each { RefdataValue contactType ->
            fields.vendorContacts.fields.put("vendorContact.${contactType.owner.desc}.${contactType.value}",[field: null, label: contactType.getI10n('value')])
        }
        fields.vendorAddresses.fields.clear()
        addressTypes.each { RefdataValue addressType ->
            fields.vendorAddresses.fields.put("vendorAddress.${addressType.value}.address",[field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.vendorAddresses.fields.put("vendorAddress.${addressType.value}.pob",[field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }



        fields
    }

    Map<String, Object> getExportProviderFields() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> exportFields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))

        getDefaultExportProviderConfig().keySet().each {
            getDefaultExportProviderConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }


        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_PROVIDER_NS).each {
            exportFields.put("providerIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }
        exportFields.put("provider.uuid", [field: 'globalUID', label: 'Laser-UUID',  message: null])
        exportFields.put("provider.wekbId", [field: 'gokbId', label: 'we:kb-ID',  message: null])

        Platform.executeQuery('select distinct(ci.platform) from CustomerIdentifier ci where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx)', contextParams).each { Platform plat ->
            exportFields.put("providerCustomerIdentifiers."+plat.id, [field: null, label: plat.name])
        }

        PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.PRV_PROP], contextService.getOrg()).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
            exportFields.put("providerProperty."+propertyDefinition.id, [field: null, label: propertyDefinition."${localizedName}", privateProperty: (propertyDefinition.tenant?.id == contextOrg.id)])
        }
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("providerContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("providerAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("providerAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        exportFields
    }

    Map<String, Object> getExportProviderFieldsForUI() {
        Org contextOrg = contextService.getOrg()
        Map<String, Object> fields = [:], contextParams = [ctx: contextOrg]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Locale locale = LocaleUtils.getCurrentLocale()
        Set<RefdataValue> contactTypes = []
        SortedSet<RefdataValue> addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.functionType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.positionType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true) order by pr.responsibilityType.'+ LocaleUtils.getLocalizedAttributeName('value'), [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc
        fields.putAll(getDefaultExportProviderConfig())
        fields.providerIdentifiers.fields.clear()
        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_PROVIDER_NS, [sort: 'ns']).each {
            fields.providerIdentifiers.fields << ["providerIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        fields.providerIdentifiers.fields << ['provider.uuid':[field: 'globalUID', label: 'Laser-UUID',  message: null]]
        fields.providerIdentifiers.fields << ['provider.wekbId':[field: 'gokbId', label: 'we:kb-ID',  message: null]]
        fields.providerCustomerIdentifiers.fields.clear()
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (select c.fromOrg from Combo c where c.toOrg = :ctx) order by plat.name', contextParams).each { Platform plat ->
            fields.providerCustomerIdentifiers.fields << ["providerCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        fields.myProviderProperties.fields.clear()
        PropertyDefinition.findAllPublicAndPrivateProp([PropertyDefinition.PRV_PROP], contextOrg).sort {it."${localizedName}"}.each { PropertyDefinition propertyDefinition ->
            if(propertyDefinition.tenant?.id == contextOrg.id)
                fields.myProviderProperties.fields << ["providerProperty.${propertyDefinition.id}":[field: null, label: propertyDefinition."${localizedName}", privateProperty: true]]
        }
        fields.providerContacts.fields.clear()
        fields.providerContacts.subTabs = subTabs
        fields.providerContacts.subTabActive = subTabActive
        contactTypes.each { RefdataValue contactType ->
            fields.providerContacts.fields.put("providerContact.${contactType.owner.desc}.${contactType.value}",[field: null, label: contactType.getI10n('value')])
        }
        fields.providerAddresses.fields.clear()
        addressTypes.each { RefdataValue addressType ->
            fields.providerAddresses.fields.put("providerAddress.${addressType.value}.address",[field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.providerAddresses.fields.put("providerAddress.${addressType.value}.pob",[field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }



        fields
    }

    /**
     * Gets the address fields
     * @return the configuration map for the address export
     */
    Map<String, Object> getExportAddressFields() {

        Map<String, Object> exportFields = [contact:[:], address:[:]]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        getDefaultExportAddressConfig().keySet().each { String k ->
            getDefaultExportAddressConfig().get(k).fields.each { key, value ->
                exportFields.get(k).put(key, value)
            }
        }

        /*
        switch(config) {
            case 'institution':
            case 'member':
                getDefaultExportOrgConfig().keySet().each {
                    getDefaultExportOrgConfig().get(it).fields.each {
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
                getDefaultExportProviderConfig().keySet().each {
                    getDefaultExportProviderConfig().get(it).fields.each {
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

    /**
     * Generic call from views
     * Gets the address fields for the UI
     * @return the configuration map for the address export for UI
     */
    Map<String, Object> getExportAddressFieldsForUI() {

        Map<String, Object> fields = [:], filterFields = [:]
        fields.putAll(getDefaultExportAddressConfig())
        filterFields.putAll(getDefaultExportAddressFilter())
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        Org contextOrg = contextService.getOrg()
        String i10nAttr = LocaleUtils.getLocalizedAttributeName('value')
        Set<RefdataValue> functionTypes = PersonRole.executeQuery('select ft from PersonRole pr join pr.functionType ft join pr.prs p where (p.tenant = :contextOrg or p.isPublic = true) order by ft.'+i10nAttr, [contextOrg: contextOrg])
        Set<RefdataValue> positionTypes = PersonRole.executeQuery('select pt from PersonRole pr join pr.positionType pt join pr.prs p where (p.tenant = :contextOrg or p.isPublic = true) order by pt.'+i10nAttr, [contextOrg: contextOrg])
        Set<RefdataValue> addressTypes = RefdataValue.executeQuery('select at from Address a join a.type at where a.tenant = :contextOrg order by at.'+i10nAttr, [contextOrg: contextOrg])

        functionTypes.each { RefdataValue functionType ->
            filterFields.function.fields.put('function.'+functionType.id, [field: null, label: functionType.getI10n('value')])
        }
        positionTypes.each { RefdataValue positionType ->
            filterFields.position.fields.put('position.'+positionType.id, [field: null, label: positionType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            filterFields.type.fields.put('type.'+addressType.id, [field: null, label: addressType.getI10n('value')])
        }

        /*
        switch(orgType) {
            case 'institution': fields = getDefaultExportOrgConfig() as Map
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
            case 'provider': fields = getDefaultExportProviderConfig() as Map
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
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Org contextOrg = contextService.getOrg()

        getDefaultExportSurveyEvaluation().keySet().each {
            getDefaultExportSurveyEvaluation().get(it).fields.each {

                if((surveyConfig.pickAndChoose || !surveyConfig.subscription) && it.key.startsWith('costItem')){
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
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        contactTypes.each { RefdataValue contactType ->
            exportFields.put("participantContact."+contactType.owner.desc+"."+contactType.value, [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            exportFields.put("participantAddress."+addressType.value+".address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            exportFields.put("participantAddress."+addressType.value+".pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        def removeSurveyProperties = exportFields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            exportFields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            exportFields.put("surveyProperty."+surveyConfigProperties.surveyProperty.id, [field: null, label: "${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true'])
        }

        exportFields.put("surveyPropertyParticipantComment", [field: null, label: "${messageSource.getMessage('surveyResult.participantComment.export', null, locale)}", defaultChecked: 'true'])
        exportFields.put("surveyPropertyCommentOnlyForOwner", [field: null, label: "${messageSource.getMessage('surveyResult.commentOnlyForOwner.export', null, locale)}", defaultChecked: 'true'])

        if(surveyConfig.pickAndChoose){
            exportFields.put("pickAndChoose", [field: null, label: "${messageSource.getMessage('surveyEvaluation.titles.label', null, locale)}", defaultChecked: 'true'])
        }

        if(surveyConfig.subscription) {
            CostItem.executeQuery('from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig) and ct.costItemElement is not null', [status: RDStore.COST_ITEM_DELETED, surveyConfig: surveyConfig]).groupBy {it.costItemElement.id}.each {
                exportFields.put("costItemsElementSurveyCostItem.${it.key}", [field: null, label: RefdataValue.get(it.key).getI10n('value')])
            }

            List<Subscription> childSubs = surveyConfig.subscription.getNonDeletedDerivedSubscriptions()
            if(childSubs) {

                CostItem.findAllBySubInListAndCostItemStatusNotEqualAndCostItemElementIsNotNull(childSubs, RDStore.COST_ITEM_DELETED).groupBy {it.costItemElement.id}.each {
                    exportFields.put("costItemsElementSubCostItem.${it.key}", [field: null, label: RefdataValue.get(it.key).getI10n('value')])
                }
            }
        }

        if(surveyConfig.vendorSurvey){
            exportFields.put("vendorSurvey", [field: null, label: "${messageSource.getMessage('surveyconfig.vendorSurvey.label', null, locale)}", defaultChecked: 'true'])
        }
        if(surveyConfig.subscriptionSurvey){
            exportFields.put("subscriptionSurvey", [field: null, label: "${messageSource.getMessage('surveyconfig.subscriptionSurvey.label', null, locale)}", defaultChecked: 'true'])
        }
        if(surveyConfig.packageSurvey){
            exportFields.put("packageSurvey", [field: null, label: "${messageSource.getMessage('surveyconfig.packageSurvey.label', null, locale)}", defaultChecked: 'true', separateSheet: 'true'])
        }
        if(surveyConfig.invoicingInformation){
            exportFields.put('survey.person', [field: null, label: 'Selected billing contact', message: 'surveyOrg.person.selected', defaultChecked: 'true'])
            exportFields.put('survey.address', [field: null, label: 'Selected billing address', message: 'surveyOrg.address.selected', defaultChecked: 'true'])
            exportFields.put('survey.eInvoicePortal', [field: null, label: 'Invoice receipt platform', message: 'surveyOrg.eInvoicePortal.label', defaultChecked: 'true'])
            exportFields.put('survey.eInvoiceLeitwegId', [field: null, label: ' Leit ID', message: 'surveyOrg.eInvoiceLeitwegId.label', defaultChecked: 'true'])
            exportFields.put('survey.eInvoiceLeitkriterium', [field: null, label: 'Leitkriterium', message: 'surveyOrg.eInvoiceLeitkriterium.label', defaultChecked: 'true'])
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

        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportSurveyEvaluation())
        Locale locale = LocaleUtils.getCurrentLocale()
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')
        Org contextOrg = contextService.getOrg()

        IdentifierNamespace.findAllByNsInList(IdentifierNamespace.CORE_ORG_NS).each {
            fields.participantIdentifiers.fields << ["participantIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        Platform.executeQuery('select distinct(plat) from CustomerIdentifier ci join ci.platform plat where ci.value != null and ci.customer in (:participants) and plat in (select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :subscription)', [participants: surveyConfig.orgs.org, subscription: surveyConfig.subscription]).each { Platform plat ->
            fields.participantCustomerIdentifiers.fields << ["participantCustomerIdentifiers.${plat.id}":[field: null, label: plat.name]]
        }
        SortedSet<RefdataValue> contactTypes = new TreeSet<RefdataValue>(), addressTypes = new TreeSet<RefdataValue>()
        contactTypes.addAll(Person.executeQuery('select pr.functionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.positionType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        contactTypes.addAll(Person.executeQuery('select pr.responsibilityType from Person p join p.roleLinks pr where (p.tenant = :ctx or p.isPublic = true)', [ctx: contextOrg]))
        addressTypes.addAll(RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE))
        RefdataCategory funcType = RefdataCategory.getByDesc(RDConstants.PERSON_FUNCTION), posType = RefdataCategory.getByDesc(RDConstants.PERSON_POSITION), respType = RefdataCategory.getByDesc(RDConstants.PERSON_RESPONSIBILITY)
        List<Map> subTabs = [[view: funcType.desc, label: funcType.getI10n('desc')], [view: posType.desc, label: posType.getI10n('desc')], [view: respType.desc, label: respType.getI10n('desc')]]
        String subTabActive = funcType.desc
        fields.participantContacts.fields.clear()
        fields.participantAddresses.fields.clear()
        fields.participantContacts.subTabs = subTabs
        fields.participantContacts.subTabActive = subTabActive
        contactTypes.each { RefdataValue contactType ->
            fields.participantContacts.fields.put("participantContact.${contactType.owner.desc}.${contactType.value}", [field: null, label: contactType.getI10n('value')])
        }
        addressTypes.each { RefdataValue addressType ->
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.address", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.address.export.addition', null, locale)])
            fields.participantAddresses.fields.put("participantAddress.${addressType.value}.pob", [field: null, label: addressType.getI10n('value')+' '+messageSource.getMessage('default.pob.export.addition', null, locale)])
        }

        def removeSurveyProperties = fields.survey.fields.keySet().findAll { it.startsWith('surveyProperty.') }

        removeSurveyProperties.each {
            fields.survey.fields.remove(it)
        }

        surveyConfig.surveyProperties.sort { it.surveyProperty."${localizedName}" }.each {SurveyConfigProperties surveyConfigProperties ->
            fields.survey.fields << ["surveyProperty.${surveyConfigProperties.surveyProperty.id}": [field: null, label: "${messageSource.getMessage('surveyProperty.label', null, locale)}: ${surveyConfigProperties.surveyProperty."${localizedName}"}", defaultChecked: 'true']]
        }

        fields.survey.fields << ["surveyPropertyParticipantComment": [field: null, label: "${messageSource.getMessage('surveyResult.participantComment.export', null, locale)}", defaultChecked: 'true']]
        fields.survey.fields << ["surveyPropertyCommentOnlyForOwner": [field: null, label: "${messageSource.getMessage('surveyResult.commentOnlyForOwner.export', null, locale)}", defaultChecked: 'true']]

        if(surveyConfig.pickAndChoose){
            fields.survey.fields << ["pickAndChoose": [field: null, label: "${messageSource.getMessage('surveyEvaluation.titles.label', null, locale)}", defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('pickAndChoose')) {
                fields.survey.fields.remove('pickAndChoose')
            }
        }

        if(surveyConfig.vendorSurvey){
            fields.survey.fields << ["vendorSurvey": [field: null, label: "${messageSource.getMessage('surveyconfig.vendorSurvey.label', null, locale)}", defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('vendorSurvey')) {
                fields.survey.fields.remove('vendorSurvey')
            }
        }

        if(surveyConfig.subscriptionSurvey){
            fields.survey.fields << ["subscriptionSurvey": [field: null, label: "${messageSource.getMessage('surveyconfig.subscriptionSurvey.label', null, locale)}", defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('subscriptionSurvey')) {
                fields.survey.fields.remove('subscriptionSurvey')
            }
        }


        if(surveyConfig.packageSurvey){
            fields.survey.fields << ["packageSurvey":  [field: null, label: "${messageSource.getMessage('surveyconfig.packageSurvey.label', null, locale)}", defaultChecked: 'true', separateSheet: 'true']]
        }else {
            if(fields.survey.fields.containsKey('packageSurvey')) {
                fields.survey.fields.remove('packageSurvey')
            }
        }


        if(surveyConfig.invoicingInformation){
            fields.survey.fields << ['survey.person': [field: null, label: 'Selected billing contact', message: 'surveyOrg.person.selected', defaultChecked: 'true']]
            fields.survey.fields << ['survey.address': [field: null, label: 'Selected billing address', message: 'surveyOrg.address.selected', defaultChecked: 'true']]
            fields.survey.fields << ['survey.eInvoicePortal': [field: null, label: 'Invoice receipt platform', message: 'surveyOrg.eInvoicePortal.label', defaultChecked: 'true']]
            fields.survey.fields << ['survey.eInvoiceLeitwegId': [field: null, label: ' Leit ID', message: 'surveyOrg.eInvoiceLeitwegId.label', defaultChecked: 'true']]
            fields.survey.fields << ['survey.eInvoiceLeitkriterium': [field: null, label: 'Leitkriterium', message: 'surveyOrg.eInvoiceLeitkriterium.label', defaultChecked: 'true']]
        }else {
            if(fields.survey.fields.containsKey('survey.person')) {
                fields.survey.fields.remove('survey.person')
            }
            if(fields.survey.fields.containsKey('survey.address')) {
                fields.survey.fields.remove('survey.address')
            }
            if(fields.survey.fields.containsKey('survey.eInvoicePortal')) {
                fields.survey.fields.remove('survey.eInvoicePortal')
            }
            if(fields.survey.fields.containsKey('survey.eInvoiceLeitwegId')) {
                fields.survey.fields.remove('survey.eInvoiceLeitwegId')
            }
            if(fields.survey.fields.containsKey('survey.eInvoiceLeitkriterium')) {
                fields.survey.fields.remove('survey.eInvoiceLeitkriterium')
            }
        }

        fields.participantSurveyCostItems.fields.costItemsElements.clear()
        fields.participantSurveySubCostItems.fields.costItemsElements.clear()

        if(surveyConfig.subscription) {
            CostItem.executeQuery('from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig) and ct.costItemElement is not null', [status: RDStore.COST_ITEM_DELETED, surveyConfig: surveyConfig]).groupBy {it.costItemElement.id}.each {
                fields.participantSurveyCostItems.fields.costItemsElements << ["costItemsElementSurveyCostItem.${it.key}": [field: null, label: RefdataValue.get(it.key).getI10n('value')]]
            }

            List<Subscription> childSubs = surveyConfig.subscription.getNonDeletedDerivedSubscriptions()
            if(childSubs) {

                CostItem.findAllBySubInListAndCostItemStatusNotEqualAndCostItemElementIsNotNull(childSubs, RDStore.COST_ITEM_DELETED).groupBy {it.costItemElement.id}.each {
                    fields.participantSurveySubCostItems.fields.costItemsElements << ["costItemsElementSubCostItem.${it.key}":[field: null, label: RefdataValue.get(it.key).getI10n('value')]]
                }
            }
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
                                'subscription.referenceYear'                : [field: 'subscription.referenceYear', label: 'Reference Year', message: 'subscription.referenceYear.label'],
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
            fields.remove('participantSurveySubCostItems')
        }else {
            if(!fields.containsKey('participantSurveyCostItems')){
                fields.put('participantSurveyCostItems',[
                        label: 'Cost Items',
                        message: 'exportClickMe.survey.costItems',
                        fields: [
                                'costItemsElements' : [:],
                                'participantSurveyCostItems.costTitle'                        : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'participantSurveyCostItems.costItemElementConfiguration'     : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                                'participantSurveyCostItems.costItemStatus'                   : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                                'participantSurveyCostItems.costInBillingCurrency'            : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                                'participantSurveyCostItems.billingCurrency'                  : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                                'participantSurveyCostItems.costInBillingCurrencyAfterTax'    : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                                'participantSurveyCostItems.taxType'                          : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                                'participantSurveyCostItems.taxRate'                          : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                                'participantSurveyCostItems.startDate'                        : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'participantSurveyCostItems.endDate'                          : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],
                                'participantSurveyCostItems.costDescription'                  : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label']
                        ]
                ])
            }

            if(!fields.containsKey('participantSurveySubCostItems')){
                fields.put('participantSurveySubCostItems', [
                        label: 'Cost Items',
                        message: 'exportClickMe.subscription.costItems',
                        fields: [
                                'costItemsElements' : [:],
                                'participantSubCostItem.costTitle'                        : [field: 'costItem.costTitle', label: 'Cost Title', message: 'financials.newCosts.costTitle'],
                                'participantSubCostItem.reference'                        : [field: 'costItem.reference', label: 'Reference Codes', message: 'financials.referenceCodes'],
                                'participantSubCostItem.budgetCodes'                      : [field: 'costItem.budgetcodes.value', label: 'Budget Code', message: 'financials.budgetCode'],
                                'participantSubCostItem.costItemElementConfiguration'     : [field: 'costItem.costItemElementConfiguration', label: 'CostItem Configuration', message: 'financials.costItemConfiguration'],
                                'participantSubCostItem.costItemStatus'                   : [field: 'costItem.costItemStatus', label: 'Status', message: 'default.status.label'],
                                'participantSubCostItem.costInBillingCurrency'            : [field: 'costItem.costInBillingCurrency', label: 'Invoice Total', message: 'financials.invoice_total'],
                                'participantSubCostItem.billingCurrency'                  : [field: 'costItem.billingCurrency', label: 'Billing Currency', message: 'default.currency.label'],
                                'participantSubCostItem.costInBillingCurrencyAfterTax'    : [field: 'costItem.costInBillingCurrencyAfterTax', label: 'Total Amount', message: 'financials.newCosts.totalAmount'],
                                'participantSubCostItem.currencyRate'                     : [field: 'costItem.currencyRate', label: 'Exchange Rate', message: 'financials.newCosts.exchangeRate'],
                                'participantSubCostItem.taxType'                          : [field: 'costItem.taxKey.taxType', label: 'Tax Type', message: 'myinst.financeImport.taxType'],
                                'participantSubCostItem.taxRate'                          : [field: 'costItem.taxKey.taxRate', label: 'Tax Rate', message: 'myinst.financeImport.taxRate'],
                                'participantSubCostItem.costInLocalCurrency'              : [field: 'costItem.costInLocalCurrency', label: 'Cost In Local Currency', message: 'financials.costInLocalCurrency'],
                                'participantSubCostItem.costInLocalCurrencyAfterTax'      : [field: 'costItem.costInLocalCurrencyAfterTax', label: 'Cost in Local Currency after taxation', message: 'financials.costInLocalCurrencyAfterTax'],

                                'participantSubCostItem.datePaid'                         : [field: 'costItem.datePaid', label: 'Financial Year', message: 'financials.financialYear'],
                                'participantSubCostItem.financialYear'                    : [field: 'costItem.financialYear', label: 'Date Paid', message: 'financials.datePaid'],
                                'participantSubCostItem.invoiceDate'                      : [field: 'costItem.invoiceDate', label: 'Invoice Date', message: 'financials.invoiceDate'],
                                'participantSubCostItem.startDate'                        : [field: 'costItem.startDate', label: 'Date From', message: 'financials.dateFrom'],
                                'participantSubCostItem.endDate'                          : [field: 'costItem.endDate', label: 'Date To', message: 'financials.dateTo'],

                                'participantSubCostItem.costDescription'                  : [field: 'costItem.costDescription', label: 'Description', message: 'default.description.label'],
                                'participantSubCostItem.invoiceNumber'                    : [field: 'costItem.invoice.invoiceNumber', label: 'Invoice Number', message: 'financials.invoice_number'],
                                'participantSubCostItem.orderNumber'                      : [field: 'costItem.order.orderNumber', label: 'Order Number', message: 'financials.order_number'],
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

        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportIssueEntitlementConfig())
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        if(contextService.getOrg().getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]) {
            if(contextService.getOrg().getCustomerType() == CustomerTypeService.ORG_INST_PRO) {
                fields.subscription.fields.put('subscription.isAutomaticRenewAnnually', [field: 'subscription.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
            }
            fields.subscription.fields.put('subscription.consortium', [field: null, label: 'Consortium', message: 'consortium.label', defaultChecked: true])
        }

        IdentifierNamespace.findAllByNsType(TitleInstancePackagePlatform.class.name, [sort: 'ns']).each {
            fields.issueEntitlementIdentifiers.fields << ["issueEntitlementIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        IdentifierNamespace.executeQuery('select idns from IdentifierNamespace idns where idns.nsType = :nsType order by coalesce(idns.'+localizedName+', idns.ns)', [nsType: IdentifierNamespace.NS_SUBSCRIPTION]).each {
            fields.subscriptionIdentifiers.fields << ["subscriptionIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
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

        getDefaultExportIssueEntitlementConfig().keySet().each {
            getDefaultExportIssueEntitlementConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        if(contextService.getOrg().getCustomerType() in [CustomerTypeService.ORG_INST_BASIC, CustomerTypeService.ORG_INST_PRO]) {
            if(contextService.getOrg().getCustomerType() == CustomerTypeService.ORG_INST_PRO) {
                exportFields.put('subscription.isAutomaticRenewAnnually', [field: 'subscription.isAutomaticRenewAnnually', label: 'Automatic Renew Annually', message: 'subscription.isAutomaticRenewAnnually.label'])
            }
            exportFields.put('subscription.consortium', [field: null, label: 'Consortium', message: 'consortium.label'])
        }

        IdentifierNamespace.findAllByNsType(TitleInstancePackagePlatform.class.name, [sort: 'ns']).each {
            exportFields.put("issueEntitlementIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }

        IdentifierNamespace.findAllByNsType(Subscription.class.name, [sort: 'ns']).each {
            exportFields.put("subscriptionIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns])
        }

        exportFields
    }

    /**
     * Generic call from views
     * Gets the title export fields and prepares them for the UI
     * @return the configuration map for the title export for the UI
     */
    Map<String, Object> getExportTippFieldsForUI() {

        Map<String, Object> fields = [:]
        fields.putAll(getDefaultExportTippConfig())
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        IdentifierNamespace.findAllByNsType(TitleInstancePackagePlatform.class.name, [sort: 'ns']).each {
            fields.tippIdentifiers.fields << ["tippIdentifiers.${it.id}":[field: null, label: it."${localizedName}" ?: it.ns]]
        }
        fields.tippIdentifiers.fields << ['tipp.wekbId':[field: 'gokbId', label: 'we:kb-ID',  message: null]]

        fields
    }

    /**
     * Gets the title fields
     * @return the configuration map for the title export
     */
    Map<String, Object> getExportTippFields() {

        Map<String, Object> exportFields = [:]
        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        getDefaultExportTippConfig().keySet().each {
            getDefaultExportTippConfig().get(it).fields.each {
                exportFields.put(it.key, it.value)
            }
        }

        IdentifierNamespace.findAllByNsType(TitleInstancePackagePlatform.class.name, [sort: 'ns']).each {
            exportFields.put("tippIdentifiers."+it.id, [field: null, label: it."${localizedName}" ?: it.ns, sqlCol: it.ns])
        }
        exportFields.put('tipp.wekbId', [field: 'gokbId', label: 'we:kb-ID', sqlCol: 'tipp_gokb_id'])

        exportFields
    }

    /**
     * Exports the selected fields of the given renewal result
     * @param renewalResult the result to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @param contactSources the types of contact (public or private) to be exported
     * @return the output in the desired format
     */
    def exportRenewalResult(Map renewalResult, Map<String, Object> selectedFields, FORMAT format, Set<String> contactSources) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportRenewalFields(renewalResult.surveyConfig)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportSurveyRenewalConfig")

        Integer maxCostItemsElements = 0
        maxCostItemsElements = CostItem.executeQuery('select count(*) from CostItem where surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig) group by costItemElement', [surveyConfig: renewalResult.surveyConfig]).size()

/*        Map<String, List<RefdataValue>> selectedCostItemElements = [all: []]
        List<String> removeSelectedCostItemElements = []
        selectedExportFields.keySet().findAll {it.startsWith('renewalSurveyCostItem.')}.each {
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
            selectedExportFields.put('renewalSurveyCostItems', [:])
        }*/

        selectedExportFields.put('renewalSurveyCostItems', [:])

        Map selectedCostItemFields = [:]
        selectedExportFields.keySet().findAll { it.startsWith('costItem.') }.each {
            selectedCostItemFields.put(it, selectedExportFields.get(it))
        }
        selectedCostItemFields.each {
            selectedExportFields.remove(it.key)
        }

        List<RefdataValue> costItemsElements = CostItem.executeQuery('from CostItem ct where ct.costItemStatus != :status and ct.surveyOrg in (select surOrg from SurveyOrg as surOrg where surveyConfig = :surveyConfig)', [status: RDStore.COST_ITEM_DELETED, surveyConfig: renewalResult.surveyConfig]).groupBy {it.costItemElement}.collect {RefdataValue.findByValueAndOwner(it.key, RefdataCategory.findByDesc(RDConstants.COST_ITEM_ELEMENT))}

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements, contactSources, null, format)

        List renewalData = []

        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.continuetoSubscription.label', null, locale) + " (${renewalResult.orgsContinuetoSubscription.size()})", 'positive')])

        renewalResult.orgsContinuetoSubscription.sort { it.participant.sortname }.each { participantResult ->
            participantResult.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            participantResult.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            participantResult.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            participantResult.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey

            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, costItemsElements, selectedCostItemFields, format, contactSources)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.withMultiYearTermSub.label', null, locale) + " (${renewalResult.orgsWithMultiYearTermSub.size()})", 'positive')])

        Subscription successorSubscriptionParent = renewalResult.surveyConfig.subscription._getCalculatedSuccessorForSurvey()

        renewalResult.orgsWithMultiYearTermSub.sort{it.getSubscriberRespConsortia().sortname}.each { sub ->
            Map renewalMap = [:]

            renewalMap.sub = sub
            renewalMap.participant = sub.getSubscriberRespConsortia()
            renewalMap.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            renewalMap.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            renewalMap.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            renewalMap.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey
            renewalMap.properties = renewalResult.properties
            renewalMap.subForCostItems = successorSubscriptionParent ? successorSubscriptionParent.getDerivedSubscriptionForNonHiddenSubscriber(renewalMap.participant) : null
            renewalMap.surveyOwner = renewalResult.surveyConfig.surveyInfo.owner

            _setRenewalRow(renewalMap, selectedExportFields, renewalData, true, costItemsElements, selectedCostItemFields, format, contactSources)

        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.orgsWithParticipationInParentSuccessor.label', null, locale) + " (${renewalResult.orgsWithParticipationInParentSuccessor.size()})", 'positive')])


        renewalResult.orgsWithParticipationInParentSuccessor.sort{it.getSubscriberRespConsortia().sortname}.each { sub ->
            Org org = sub.getSubscriberRespConsortia()

            Map renewalMap = [:]

            renewalMap.sub = sub
            renewalMap.participant = org
            renewalMap.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            renewalMap.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            renewalMap.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            renewalMap.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey
            renewalMap.properties = renewalResult.properties
            renewalMap.subForCostItems = sub
            renewalMap.surveyOwner = renewalResult.surveyConfig.surveyInfo.owner

            _setRenewalRow(renewalMap, selectedExportFields, renewalData, true, costItemsElements, selectedCostItemFields, format, contactSources)

        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.newOrgstoSubscription.label', null, locale) + " (${renewalResult.newOrgsContinuetoSubscription.size()})", 'positive')])


        renewalResult.newOrgsContinuetoSubscription.sort{it.participant.sortname}.each { participantResult ->
            participantResult.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            participantResult.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            participantResult.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            participantResult.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey

            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, costItemsElements, selectedCostItemFields, format, contactSources)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.withTermination.label', null, locale) + " (${renewalResult.orgsWithTermination.size()})", 'negative')])


        renewalResult.orgsWithTermination.sort{it.participant.sortname}.each { participantResult ->
            participantResult.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            participantResult.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            participantResult.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            participantResult.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey

            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, costItemsElements, selectedCostItemFields, format, contactSources)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('surveys.tabs.termination', null, locale) + " (${renewalResult.orgsWithoutResult.size()})", 'negative')])


        renewalResult.orgsWithoutResult.sort{it.participant.sortname}.each { participantResult ->
            participantResult.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            participantResult.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            participantResult.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            participantResult.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey

            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, costItemsElements, selectedCostItemFields, format, contactSources)
        }

        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, ' ')])
        renewalData.add([createTableCell(format, messageSource.getMessage('renewalEvaluation.orgInsertedItself.label', null, locale) + " (${renewalResult.orgInsertedItself.size()})", 'negative')])


        renewalResult.orgInsertedItself.sort{it.participant.sortname}.each { participantResult ->
            participantResult.multiYearTermTwoSurvey = renewalResult.multiYearTermTwoSurvey
            participantResult.multiYearTermThreeSurvey = renewalResult.multiYearTermThreeSurvey
            participantResult.multiYearTermFourSurvey = renewalResult.multiYearTermFourSurvey
            participantResult.multiYearTermFiveSurvey = renewalResult.multiYearTermFiveSurvey

            _setRenewalRow(participantResult, selectedExportFields, renewalData, false, costItemsElements, selectedCostItemFields, format, contactSources)
        }


        Map sheetData = [:]
        sheetData[messageSource.getMessage('renewalexport.renewals', null, locale)] = [titleRow: titles, columnData: renewalData]

        if (renewalResult.orgsContinuetoSubscription) {
            sheetData = _exportAccessPoints(renewalResult.orgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 1", format)
        }

        if (renewalResult.orgsWithMultiYearTermSub) {
            sheetData = _exportAccessPoints(renewalResult.orgsWithMultiYearTermSub.collect { it.getSubscriberRespConsortia() }, sheetData, selectedExportFields, locale, " - 2", format)
        }

        if (renewalResult.orgsWithParticipationInParentSuccessor) {
            sheetData = _exportAccessPoints(renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getSubscriberRespConsortia() }, sheetData, selectedExportFields, locale, " - 3", format)
        }

        if (renewalResult.newOrgsContinuetoSubscription) {
            sheetData = _exportAccessPoints(renewalResult.newOrgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 4", format)
        }

        if (renewalResult.orgsContinuetoSubscription) {
            sheetData = _exportSurveyPackagesAndSurveyVendors(renewalResult.surveyConfig, renewalResult.orgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 5", format)
        }

        if (renewalResult.orgsWithMultiYearTermSub) {
            sheetData = _exportSurveyPackagesAndSurveyVendors(renewalResult.surveyConfig, renewalResult.orgsWithMultiYearTermSub.collect { it.getSubscriberRespConsortia() }, sheetData, selectedExportFields, locale, " - 6", format)
        }

        if (renewalResult.orgsWithParticipationInParentSuccessor) {
            sheetData = _exportSurveyPackagesAndSurveyVendors(renewalResult.surveyConfig, renewalResult.orgsWithParticipationInParentSuccessor.collect { it.getSubscriberRespConsortia() }, sheetData, selectedExportFields, locale, " - 7", format)
        }

        if (renewalResult.newOrgsContinuetoSubscription) {
            sheetData = _exportSurveyPackagesAndSurveyVendors(renewalResult.surveyConfig, renewalResult.newOrgsContinuetoSubscription.participant, sheetData, selectedExportFields, locale, " - 8", format)
        }

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(sheetData.titleRow, sheetData.columnData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(sheetData.titleRow, sheetData.columnData, '\t')
        }
    }

    /**
     * Exports the selected fields about the members of the given subscription for the given contextOrg
     * @param result the subscription members to export
     * @param selectedFields the fields which should appear
     * @param subscription the subscription as reference for the fields
     * @param contactSwitch which set of contacts should be considered (public or private)?
     * @param format the {@link FORMAT} to be exported
     * @return the output in the desired format
     */
    def exportSubscriptionMembers(Collection result, Map<String, Object> selectedFields, Subscription subscription, Set<String> contactSwitch, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionMembersFields(subscription)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportSubscriptionMembersConfig")

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

        if(subscription) {
            List<Subscription> childSubs = subscription.getNonDeletedDerivedSubscriptions()
            if(childSubs) {
                maxCostItemsElements = CostItem.executeQuery('select count(*) as countCostItems from CostItem where sub in (:subs) group by costItemElement, sub order by countCostItems desc', [subs: childSubs])[0]
            }
        }
        else maxCostItemsElements = 1

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements, contactSwitch, null, format)

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
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titles, pages: sheetData.values()]
        }

    }

    /**
     * Exports the given fields from the given subscriptions
     * @param result the subscription set or list to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @param showTransferFields should the subscription transfer fields be included in the export?
     * @return the output in the desired format
     */
    def exportSubscriptions(Collection<Subscription> result, Map<String, Object> selectedFields, FORMAT format, boolean showTransferFields = false) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Org contextOrg = contextService.getOrg()
        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSubscriptionFields(showTransferFields)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, contextOrg.getCustomerType() == CustomerTypeService.ORG_SUPPORT ? 'getDefaultExportConsortiaParticipationsSupportConfig' : 'getDefaultExportConsortiaParticipationsConfig')

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

        maxCostItemsElements = CostItem.executeQuery('select count(*) as countCostItems from CostItem where sub in (:subs) group by costItemElement, sub order by countCostItems desc', [subs: result])[0]

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements, null, selectedCostItemElements, format)

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
            return exportService.generateSeparatorTableString(titles, exportData, '|')
        case FORMAT.TSV:
            return exportService.generateSeparatorTableString(titles, exportData, '\t')
        case FORMAT.PDF:
            //structure: list of maps (each map is the content of a page)
            return [mainHeader: titles, pages: [[titleRow: titles, columnData: exportData]]]
        }
    }

    /**
     * Exports the given fields from the given consortia participations
     * @param result the subscription set to export
     * @param selectedFields the fields which should appear
     * @param contactSwitch which set of contacts should be considered (public or private)?
     * @param format the {@link FORMAT} to be exported
     * @return an Excel worksheet containing the export
     */
    def exportConsortiaParticipations(Set result, Map<String, Object> selectedFields, Set<String> contactSwitch, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportConsortiaParticipationFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportSubscriptionConfig")

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

        maxCostItemsElements = CostItem.executeQuery('select count(*) as countCostItems from CostItem where sub in (:subs) group by costItemElement, sub order by countCostItems desc', [subs: result.sub])[0]

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, maxCostItemsElements, contactSwitch, selectedCostItemElements, format)

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        List exportData = []
        result.each { memberResult ->
            _setSubRow(memberResult, selectedExportFields, exportData, localizedName, selectedCostItemElements, selectedCostItemFields, format, contactSwitch)
        }

        switch(format) {
            case FORMAT.XLS:
                Map sheetData = [:]
                sheetData[messageSource.getMessage('myinst.currentSubscriptions.label', null, locale)] = [titleRow: titles, columnData: exportData]
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titles, pages: [[titleRow: titles, columnData: exportData]]]
        }
    }

    /**
     * Exports the given fields from the given subscriptions
     * @param result the subscription set to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @return the output in the desired format
     */
    def exportLicenses(ArrayList<License> result, Map<String, Object> selectedFields, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportLicenseFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportLicenseConfig")

        List titles = _exportTitles(selectedExportFields, locale, null, null, null, null, format)

        String localizedName = LocaleUtils.getLocalizedAttributeName('name')

        List exportData = []
        result.each { License license ->
            _setLicRow(license, selectedExportFields, exportData, localizedName, format)
        }
        Map sheetData = [:]
        sheetData[messageSource.getMessage('menu.my.licenses', null, locale)] = [titleRow: titles, columnData: exportData]

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titles, pages: [[titleRow: titles, columnData: exportData]]]
        }
    }

    /**
     * Exports the given fields from the given cost items
     * @param result the cost item set to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which set of contacts should be considered (public or private)?
     * @return the output in the desired format
     */
    def exportCostItems(Map result, Map<String, Object> selectedFields, FORMAT format, Set<String> contactSources) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportCostItemFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportCostItemConfig")

        Map sheetData = [:]

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSources, null, format)
        Set<Org> orgSet = []

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
                    _setCostItemRow(ci, selectedExportFields, exportData, format, contactSources)
                    orgSet << ci.sub?.getSubscriberRespConsortia()
                }
            }
            sheetData[sheettitle] = [titleRow: titles, columnData: exportData]
        }

        sheetData =  _exportAccessPoints(orgSet.toList(), sheetData, selectedExportFields, locale, "", format)

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given survey cost items
     * @param result the cost item set to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which set of contacts should be considered (public or private)?
     * @return the output in the desired format
     */
    def exportSurveyCostItemsForOwner(SurveyConfig surveyConfig, Map<String, Object> selectedFields, FORMAT format, Set<String> contactSources) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSurveyCostItemFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportSurveyCostItemConfig")

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSources, null, format)

        List<CostItem> costItems = CostItem.findAllBySurveyOrgInListAndCostItemStatusNotEqualAndPkgIsNull(SurveyOrg.findAllBySurveyConfig(surveyConfig), RDStore.COST_ITEM_DELETED).sort {it.surveyOrg.org.sortname}

        List exportData = []
        costItems.each { CostItem costItem ->
            _setCostItemRow(costItem, selectedExportFields, exportData, format, contactSources)
        }

        Map sheetData = [:]
        sheetData[messageSource.getMessage('financials.costItem', null, locale)] = [titleRow: titles, columnData: exportData]

        if (surveyConfig.packageSurvey) {
            String sheetName = ''
            costItems = CostItem.findAllBySurveyOrgInListAndCostItemStatusNotEqualAndPkgIsNotNull(SurveyOrg.findAllBySurveyConfig(surveyConfig), RDStore.COST_ITEM_DELETED).sort { it.surveyOrg.org.sortname }
            selectedExportFields.put('pkg', [field: 'pkg.name', label: 'Package Name', message: 'package.label'])
            titles = _exportTitles(selectedExportFields, locale, null, null, contactSources, null, format)

            exportData = []
            costItems.each { CostItem costItem ->
                _setCostItemRow(costItem, selectedExportFields, exportData, format, contactSources)
            }

            sheetName = messageSource.getMessage('surveyCostItemsPackages.label', null, locale)
            sheetData[sheetName] = [titleRow: titles, columnData: exportData]
        }

        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the given fields from the given organisations
     * @param result the organisation set to export
     * @param selectedFields the fields which should appear in the export
     * @param config the organisation type to be exported
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which type of contacts should be taken? (public or private)
     * @param configMap filter parameters for further queries
     * @return the output in the desired format
     */
    def exportOrgs(List<Org> result, Map<String, Object> selectedFields, String config, FORMAT format, Set<String> contactSources = [], Map<String, Object> configMap = [:]) {
        Locale locale = LocaleUtils.getCurrentLocale()

        String sheetTitle

        String nameOfClickMeMap = ''

        switch(config) {
            case 'consortium':
                sheetTitle = messageSource.getMessage('consortium.label', null, locale)
                nameOfClickMeMap = 'getDefaultExportConsortiaConfig'
                break
            case 'institution':
                sheetTitle = messageSource.getMessage('default.institution', null, locale)
                nameOfClickMeMap = contextService.getOrg().getCustomerType() == CustomerTypeService.ORG_SUPPORT ? 'getDefaultExportOrgSupportConfig' : 'getDefaultExportOrgConfig'
                break
            case 'member':
                sheetTitle = messageSource.getMessage('subscription.details.consortiaMembers.label', null, locale)
                nameOfClickMeMap = 'getDefaultExportOrgConfig'
                break
        }

        Map<String, Object> selectedExportFields = [:], configFields = getExportOrgFields(config)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, nameOfClickMeMap)

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSources, null, format)

        List exportData = []
        result.each { Org org ->
            _setOrgRow(org, selectedExportFields, exportData, format, contactSources, configMap)
        }

        Map sheetData = [:]
        sheetData[sheetTitle] = [titleRow: titles, columnData: exportData]

        sheetData =  _exportAccessPoints(result, sheetData, selectedExportFields, locale, "", format)

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titles, pages: sheetData.values()]
        }
    }

    /**
     * Exports the given fields from the given cost items
     * @param result the {@link de.laser.wekb.Vendor} set to export
     * @param selectedFields the fields which should appear in the export
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which type of contacts should be taken? (public or private)
     * @param configMap filter parameters for further queries
     * @return the output in the desired format
     */
    def exportVendors(Set<Vendor> result, Map<String, Object> selectedFields, FORMAT format, Set<String> contactSources = []) {
        Locale locale = LocaleUtils.getCurrentLocale()

        String sheetTitle = messageSource.getMessage('default.vendor.export.label', null, locale)

        Map<String, Object> selectedExportFields = [:], configFields = getExportVendorFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, 'getDefaultExportVendorConfig')

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSources, null, format)

        List exportData = []
        result.each { Vendor vendor ->
            _setVendorRow(vendor, selectedExportFields, exportData, format, contactSources)
        }

        Map sheetData = [:]
        sheetData[sheetTitle] = [titleRow: titles, columnData: exportData]

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titles, pages: sheetData.values()]
        }
    }

    /**
     * Exports the given fields from the given cost items
     * @param result the {@link de.laser.wekb.Provider} set to export
     * @param selectedFields the fields which should appear in the export
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which type of contacts should be taken? (public or private)
     * @param configMap filter parameters for further queries
     * @return the output in the desired format
     */
    def exportProviders(Set<Provider> result, Map<String, Object> selectedFields, FORMAT format, Set<String> contactSources = []) {
        Locale locale = LocaleUtils.getCurrentLocale()

        String sheetTitle = messageSource.getMessage('default.provider.export.label', null, locale)

        Map<String, Object> selectedExportFields = [:], configFields = getExportProviderFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, 'getDefaultExportProviderConfig')

        List titles = _exportTitles(selectedExportFields, locale, null, null, contactSources, null, format)

        List exportData = []
        result.each { Provider provider ->
            _setProviderRow(provider, selectedExportFields, exportData, format, contactSources)
        }

        Map sheetData = [:]
        sheetData[sheetTitle] = [titleRow: titles, columnData: exportData]

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titles, pages: sheetData.values()]
        }
    }

    /**
     * Exports the given fields from the given person contacts or addresses in the given format
     * @param visiblePersons the contact set to export
     * @param visibleAddresses the address set to export
     * @param selectedFields the fields to be exported
     * @param withInstData should data from institutions be included?
     * @param withProvData should data from providers be included?
     * @param withVenData should data from vendors be included?
     * @param format the {@link FORMAT} to be exported
     * @return the output, rendered in the desired format
     */
    def exportAddresses(List visiblePersons, List visibleAddresses, Map<String, Object> selectedFields, withInstData, withProvData, withVenData, String tab, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map<String, Object> configFields = getExportAddressFields(), selectedExportContactFields = [:], selectedExportAddressFields = [:], sheetData = [:]
        List instData = [], provData = [], venData = [], instAddresses = [], provAddresses = [], venAddresses = []

        selectedFields.keySet().each { String key ->
            if(configFields.contact.containsKey(key))
                selectedExportContactFields.put(key, configFields.contact.get(key))
            if(configFields.address.containsKey(key))
                selectedExportAddressFields.put(key, configFields.address.get(key))
        }

        saveClickMeConfig(selectedFields, "getDefaultExportAddressConfig")

        List titleRow = [messageSource.getMessage('contact.contentType.label', null, locale)]
        titleRow.addAll(_exportTitles(selectedExportContactFields, locale, null, null, null, null, format))

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
                PersonRole prsLink = p.roleLinks.find { PersonRole pr -> pr.org != null || pr.provider != null || pr.vendor != null }
                if(prsLink.functionType)
                    contactType = prsLink.functionType.getI10n('value')
                else if(prsLink.positionType)
                    contactType = prsLink.positionType.getI10n('value')
                List row = [createTableCell(format, contactType)]
                Map.Entry<String, Map<String, String>> contact = contactData.entrySet()[addressRow]
                //Address a = p.addresses[addressRow]
                selectedExportContactFields.each { String fieldKey, Map mapSelectedFields ->
                    String field = mapSelectedFields.field
                    if (field == 'organisation') {
                        // ERMS-5869 - sufficient criteria?
                        if (prsLink.org) {
                            row.add(createTableCell(format, prsLink.org.name))
                        }
                        else if (prsLink.provider) {
                            row.add(createTableCell(format, prsLink.provider.name))
                        }
                        else if (prsLink.vendor) {
                            row.add(createTableCell(format, prsLink.vendor.name))
                        }
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
                if(prsLink.org)
                    instData << row
                else if(prsLink.provider)
                    provData << row
                else if(prsLink.vendor)
                    venData << row
            }
        }

        if(withInstData)
            sheetData[messageSource.getMessage('org.institution.plural', null, locale)] = [titleRow: titleRow, columnData: instData]
        if(withProvData)
            sheetData[messageSource.getMessage('provider.plural', null, locale)] = [titleRow: titleRow, columnData: provData]
        if(withVenData)
            sheetData[messageSource.getMessage('vendor.plural', null, locale)] = [titleRow: titleRow, columnData: venData] // ERMS-5869
        if(visibleAddresses || tab == 'addresses') {
            titleRow = [messageSource.getMessage('default.type.label', null, locale)]
            titleRow.addAll(_exportTitles(selectedExportAddressFields, locale, null, null, null, null, format))
            visibleAddresses.each { Address a ->
                a.type.each { RefdataValue type ->
                    List row = [createTableCell(format, type.getI10n('value'))]
                    selectedExportAddressFields.each { String fieldKey, Map mapSelectedFields ->
                        String field = mapSelectedFields.field
                        if (field == 'organisation') {
                            // ERMS-5869 - sufficient criteria?
                            if (a.org) {
                                row.add(createTableCell(format, a.org.name))
                            }
                            else if (a.provider) {
                                row.add(createTableCell(format, a.provider.name))
                            }
                            else if (a.vendor) {
                                row.add(createTableCell(format, a.vendor.name))
                            }
                        }
                        else if (field == 'receiver') {
                            row.add(createTableCell(format, a.name))
                        }
                        else {
                            if (a[field] instanceof RefdataValue)
                                row.add(createTableCell(format, a[field].getI10n("value")))
                            else row.add(createTableCell(format, a[field]))
                        }
                    }
                    if(a.org)
                        instAddresses << row
                    else if(a.provider)
                        provAddresses << row
                    else if(a.vendor)
                        venAddresses << row
                }
            }
            if(withInstData)
                sheetData[messageSource.getMessage('org.institution.address.label', null, locale)] = [titleRow: titleRow, columnData: instAddresses]
            if(withProvData)
                sheetData[messageSource.getMessage('default.provider.address.label', null, locale)] = [titleRow: titleRow, columnData: provAddresses]
            if(withVenData)
                sheetData[messageSource.getMessage('default.vendor.address.label', null, locale)] = [titleRow: titleRow, columnData: venAddresses]
        }
        if(sheetData.size() == 0) {
            sheetData[messageSource.getMessage('org.institution.plural', null, locale)] = [titleRow: titleRow, columnData: []]
        }
        switch(format) {
            case FORMAT.XLS: return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                List currData = []
                switch(tab) {
                    case 'addresses':
                        if(withInstData)
                            currData.addAll(instAddresses)
                        if(withProvData)
                            currData.addAll(provAddresses)
                        if(withProvData)
                            currData.addAll(venAddresses)
                        break
                    case 'contacts':
                        if(withInstData)
                            currData.addAll(instData)
                        if(withProvData)
                            currData.addAll(provData)
                        if(withVenData)
                            currData.addAll(venData)
                        break
                }
                return exportService.generateSeparatorTableString(titleRow, currData, '|')
            case FORMAT.PDF:
                //structure: list of maps (each map is the content of a page)
                return [mainHeader: titleRow, pages: sheetData.values()]
        }
    }

    /**
     * Exports the given fields of the given survey evaluation
     * @param result the survey evaluation which should be exported
     * @param selectedFields the fields which should appear
     * @return an Excel worksheet containing the export
     */
    def exportSurveyEvaluation(Map result, Map<String, Object> selectedFields, Set<String> contactSwitch, FORMAT format, String chartFilter) {
        Locale locale = LocaleUtils.getCurrentLocale()
        Map sheetData = [:]
        Map<String, Object> selectedExportFields = [:]

        Map<String, Object> configFields = getExportSurveyEvaluationFields(result.surveyConfig)

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportSurveyEvaluation")

        Map<String, List<RefdataValue>> selectedCostItemElements = [selectedCostItemElementsForSurveyCostItems: [], selectedCostItemElementsForSubCostItems: []]
        List<String> removeSelectedCostItemElements = []
        selectedExportFields.keySet().findAll {it.startsWith('costItemsElementSurveyCostItem.')}.each {
            selectedCostItemElements.selectedCostItemElementsForSurveyCostItems << RefdataValue.get(Long.parseLong(it.split("\\.")[1]))
            removeSelectedCostItemElements << it
        }

        selectedExportFields.keySet().findAll {it.startsWith('costItemsElementSubCostItem.')}.each {
            selectedCostItemElements.selectedCostItemElementsForSubCostItems << RefdataValue.get(Long.parseLong(it.split("\\.")[1]))
            removeSelectedCostItemElements << it
        }

        Map selectedCostItemFields = [forSurveyCostItems: [:], forSubCostItems: [:]]
        if(selectedCostItemElements){
            selectedExportFields.keySet().findAll {it.startsWith('participantSubCostItem.')}.each {
                selectedCostItemFields.forSubCostItems.put(it, selectedExportFields.get(it))
            }

            selectedExportFields.keySet().findAll {it.startsWith('participantSurveyCostItem.')}.each {
                selectedCostItemFields.forSurveyCostItems.put(it, selectedExportFields.get(it))
            }

            selectedCostItemFields.forSurveyCostItems.each {
                selectedExportFields.remove(it.key)
            }

            selectedCostItemFields.forSubCostItems.each {
                selectedExportFields.remove(it.key)
            }

            removeSelectedCostItemElements.each {
                selectedExportFields.remove(it)
            }

            selectedExportFields.put('participantSurveyCostItems', [:])
            selectedExportFields.put('participantSurveySubCostItems', [:])

        }

        List titles = _exportTitles(selectedExportFields, locale, selectedCostItemFields, null, contactSwitch, selectedCostItemElements, format)

        List exportData = []

        if(chartFilter){
            exportData.add([createTableCell(format, chartFilter + " (${result.participants.size()})", 'bold')])
            result.participants.sort { it.org.sortname }.each { SurveyOrg surveyOrg ->
                Map participantResult = [:]
                participantResult.properties = SurveyResult.findAllByParticipantAndSurveyConfig(surveyOrg.org, result.surveyConfig)

                participantResult.sub = [:]
                if (result.surveyConfig.subscription) {
                    participantResult.sub = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyOrg.org)
                }

                participantResult.participant = surveyOrg.org
                participantResult.surveyConfig = result.surveyConfig
                participantResult.surveyOwner = result.surveyConfig.surveyInfo.owner
                participantResult.subCostItems = participantResult.sub ? CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqualAndOwnerAndPkgIsNull(participantResult.sub, selectedCostItemElements.selectedCostItemElementsForSubCostItems, RDStore.COST_ITEM_DELETED, participantResult.surveyOwner, [sort: 'costItemElement']) : []
                participantResult.surveyCostItems = CostItem.findAllBySurveyOrgAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(surveyOrg, selectedCostItemElements.selectedCostItemElementsForSurveyCostItems, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
                participantResult.surveyOrg = surveyOrg
                participantResult.selectedCostItemElementsForSubCostItems = selectedCostItemElements.selectedCostItemElementsForSubCostItems
                participantResult.selectedCostItemElementsForSurveyCostItems = selectedCostItemElements.selectedCostItemElementsForSurveyCostItems

                _setSurveyEvaluationRow(participantResult, selectedExportFields, exportData, selectedCostItemFields, format, contactSwitch)
            }

            sheetData = [:]
            sheetData[messageSource.getMessage('surveyInfo.evaluation', null, locale)] = [titleRow: titles, columnData: exportData]

            if (result.participants) {
                sheetData = _exportAccessPoints(result.participants.org, sheetData, selectedExportFields, locale, " - 1", format)
            }

            if (result.participants) {
                sheetData = _exportSurveyPackagesAndSurveyVendors(result.surveyConfig, result.participants.org, sheetData, selectedExportFields, locale, " - 2", format)
            }

        }else {

            List<SurveyOrg> participantsNotFinish = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfig(result.surveyConfig)
            List<SurveyOrg> participantsFinish = SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(result.surveyConfig)

            //List<SurveyOrg> participantsNotFinish = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfigAndOrgInsertedItself(result.surveyConfig, false)
            //List<SurveyOrg> participantsNotFinishInsertedItself = SurveyOrg.findAllByFinishDateIsNullAndSurveyConfigAndOrgInsertedItself(result.surveyConfig, true)
            exportData.add([createTableCell(format, messageSource.getMessage('surveyEvaluation.participantsViewAllFinish', null, locale) + " (${participantsFinish.size()})", 'positive')])

            participantsFinish.sort { it.org.sortname }.each { SurveyOrg surveyOrg ->
                Map participantResult = [:]
                participantResult.properties = SurveyResult.findAllByParticipantAndSurveyConfig(surveyOrg.org, result.surveyConfig)

                participantResult.sub = [:]
                if (result.surveyConfig.subscription) {
                    participantResult.sub = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyOrg.org)
                }

                participantResult.participant = surveyOrg.org
                participantResult.surveyConfig = result.surveyConfig
                participantResult.surveyOwner = result.surveyConfig.surveyInfo.owner
                participantResult.subCostItems = participantResult.sub ? CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqualAndOwnerAndPkgIsNull(participantResult.sub, selectedCostItemElements.selectedCostItemElementsForSubCostItems, RDStore.COST_ITEM_DELETED, participantResult.surveyOwner, [sort: 'costItemElement']) : []
                participantResult.surveyCostItems = CostItem.findAllBySurveyOrgAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(surveyOrg, selectedCostItemElements.selectedCostItemElementsForSurveyCostItems, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
                participantResult.surveyOrg = surveyOrg
                participantResult.selectedCostItemElementsForSubCostItems = selectedCostItemElements.selectedCostItemElementsForSubCostItems
                participantResult.selectedCostItemElementsForSurveyCostItems = selectedCostItemElements.selectedCostItemElementsForSurveyCostItems

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
                if (result.surveyConfig.subscription) {
                    participantResult.sub = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyOrg.org)
                }

                participantResult.participant = surveyOrg.org
                participantResult.surveyConfig = result.surveyConfig
                participantResult.surveyOwner = result.surveyConfig.surveyInfo.owner
                participantResult.subCostItems = participantResult.sub ? CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqualAndOwnerAndPkgIsNull(participantResult.sub, selectedCostItemElements.selectedCostItemElementsForSubCostItems, RDStore.COST_ITEM_DELETED, participantResult.surveyOwner, [sort: 'costItemElement']) : []
                participantResult.surveyCostItems = CostItem.findAllBySurveyOrgAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(surveyOrg, selectedCostItemElements.selectedCostItemElementsForSurveyCostItems, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
                participantResult.surveyOrg = surveyOrg
                participantResult.selectedCostItemElementsForSubCostItems = selectedCostItemElements.selectedCostItemElementsForSubCostItems
                participantResult.selectedCostItemElementsForSurveyCostItems = selectedCostItemElements.selectedCostItemElementsForSurveyCostItems

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
                participantResult.sub = result.surveyConfig.subscription.getDerivedSubscriptionForNonHiddenSubscriber(surveyOrg.org)
            }

            participantResult.participant = surveyOrg.org
            participantResult.surveyConfig = result.surveyConfig
            participantResult.surveyOwner = result.surveyConfig.surveyInfo.owner
            participantResult.subCostItems = participantResult.sub ? CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqualAndOwnerAndPkgIsNull(participantResult.sub, selectedCostItemElements.selectedCostItemElementsForSubCostItems, RDStore.COST_ITEM_DELETED, participantResult.surveyOwner, [sort: 'costItemElement']) : []
            participantResult.surveyCostItems = CostItem.findAllBySurveyOrgAndCostItemElementAndCostItemStatusNotEqualAndPkgIsNull(surveyOrg, selectedCostItemElements.selectedCostItemElementsForSurveyCostItems, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
            participantResult.surveyOrg = surveyOrg
            participantResult.selectedCostItemElementsForSubCostItems = selectedCostItemElements.selectedCostItemElementsForSubCostItems
            participantResult.selectedCostItemElementsForSurveyCostItems = selectedCostItemElements.selectedCostItemElementsForSurveyCostItems

            _setSurveyEvaluationRow(participantResult, selectedExportFields, exportData, selectedCostItemFields, format, contactSwitch)
        }*/

            sheetData[messageSource.getMessage('surveyInfo.evaluation', null, locale)] = [titleRow: titles, columnData: exportData]

            if (participantsFinish) {
                sheetData = _exportAccessPoints(participantsFinish.org, sheetData, selectedExportFields, locale, " - 1", format)
            }

            if (participantsNotFinish) {
                sheetData = _exportAccessPoints(participantsNotFinish.org, sheetData, selectedExportFields, locale, " - 2", format)
            }

            if (participantsFinish) {
                sheetData = _exportSurveyPackagesAndSurveyVendors(result.surveyConfig, participantsFinish.org, sheetData, selectedExportFields, locale, " - 3", format)
            }

            if (participantsNotFinish) {
                sheetData = _exportSurveyPackagesAndSurveyVendors(result.surveyConfig, participantsNotFinish.org, sheetData, selectedExportFields, locale, " - 4", format)
            }

        }



        switch(format) {
            case FORMAT.XLS: return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV: return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV: return exportService.generateSeparatorTableString(titles, exportData, '\t')
        }
    }

    /**
     * Exports the given fields from the given issue entitlements
     * @param result the issue entitlements set to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @return the output in the desired format
     */
    def exportIssueEntitlements(Set<Long> result, Map<String, Object> selectedFields, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Map> selectedExportFields = [:]

        Map<String, Object> configFields = getExportIssueEntitlementFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportTippConfig")

        List titles = _exportTitles(selectedExportFields, locale, null, null, null, null, format)

        List exportData = buildIssueEntitlementRows(result, selectedExportFields, format)

        /*
        int max = 32500
        TitleInstancePackagePlatform.withSession { Session sess ->
            for(int offset = 0; offset < result.size(); offset+=max) {
                List allRows = []
                //this double structure is necessary because the KBART standard foresees for each coverageStatement an own row with the full data
                Set<IssueEntitlement> issueEntitlements = IssueEntitlement.findAllByIdInList(result.drop(offset).take(max), [sort: 'tipp.sortname'])
                //println "select this_.ie_id as ie_id1_35_1_, this_.ie_version as ie_versi2_35_1_, this_.ie_date_created as ie_date_3_35_1_, this_.ie_perpetual_access_by_sub_fk as ie_perpe4_35_1_, this_.ie_last_updated as ie_last_5_35_1_, this_.ie_notes as ie_notes6_35_1_, this_.ie_access_start_date as ie_acces7_35_1_, this_.ie_medium_rv_fk as ie_mediu8_35_1_, this_.ie_open_access_rv_fk as ie_open_9_35_1_, this_.ie_access_end_date as ie_acce10_35_1_, this_.ie_sortname as ie_sort11_35_1_, this_.ie_tipp_fk as ie_tipp12_35_1_, this_.ie_subscription_fk as ie_subs13_35_1_, this_.ie_name as ie_name14_35_1_, this_.ie_guid as ie_guid15_35_1_, this_.ie_access_type_rv_fk as ie_acce16_35_1_, this_.ie_status_rv_fk as ie_stat17_35_1_, titleinsta1_.tipp_id as tipp_id1_101_0_, titleinsta1_.tipp_version as tipp_ver2_101_0_, titleinsta1_.tipp_first_editor as tipp_fir3_101_0_, titleinsta1_.tipp_imprint as tipp_imp4_101_0_, titleinsta1_.tipp_gokb_id as tipp_gok5_101_0_, titleinsta1_.tipp_date_created as tipp_dat6_101_0_, titleinsta1_.tipp_last_updated as tipp_las7_101_0_, titleinsta1_.tipp_status_reason_rv_fk as tipp_sta8_101_0_, titleinsta1_.tipp_series_name as tipp_ser9_101_0_, titleinsta1_.tipp_plat_fk as tipp_pl10_101_0_, titleinsta1_.tipp_access_start_date as tipp_ac11_101_0_, titleinsta1_.tipp_medium_rv_fk as tipp_me12_101_0_, titleinsta1_.tipp_date_first_online as tipp_da13_101_0_, titleinsta1_.tipp_pkg_fk as tipp_pk14_101_0_, titleinsta1_.tipp_title_type as tipp_ti15_101_0_, titleinsta1_.tipp_edition_number as tipp_ed16_101_0_, titleinsta1_.tipp_open_access_rv_fk as tipp_op17_101_0_, titleinsta1_.tipp_access_end_date as tipp_ac18_101_0_, titleinsta1_.tipp_date_first_in_print as tipp_da19_101_0_, titleinsta1_.tipp_publisher_name as tipp_pu20_101_0_, titleinsta1_.tipp_sort_name as tipp_so21_101_0_, titleinsta1_.tipp_summary_of_content as tipp_su22_101_0_, titleinsta1_.tipp_volume as tipp_vo23_101_0_, titleinsta1_.tipp_norm_name as tipp_no24_101_0_, titleinsta1_.tipp_delayedoa_rv_fk as tipp_de25_101_0_, titleinsta1_.tipp_edition_statement as tipp_ed26_101_0_, titleinsta1_.tipp_host_platform_url as tipp_ho27_101_0_, titleinsta1_.tipp_hybridoa_rv_fk as tipp_hy28_101_0_, titleinsta1_.tipp_name as tipp_na29_101_0_, titleinsta1_.tipp_guid as tipp_gu30_101_0_, titleinsta1_.tipp_access_type_rv_fk as tipp_ac31_101_0_, titleinsta1_.tipp_status_rv_fk as tipp_st32_101_0_, titleinsta1_.tipp_edition_differentiator as tipp_ed33_101_0_, titleinsta1_.tipp_first_author as tipp_fi34_101_0_, titleinsta1_.tipp_subject_reference as tipp_su35_101_0_ from issue_entitlement this_ inner join title_instance_package_platform titleinsta1_ on this_.ie_tipp_fk=titleinsta1_.tipp_id where this_.ie_id in (${result.join(',')}) order by lower(titleinsta1_.tipp_sort_name) asc limit ${max} offset ${offset}"
                issueEntitlements.each { IssueEntitlement entitlement ->
                    if(!entitlement.coverages) {
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
        */

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
     * Exports the given fields from the given titles
     * @param result the titles set to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     * @return the output in the desired format
     */
    def exportTipps(Set<Long> result, Map<String, Object> selectedFields, FORMAT format) {
        Locale locale = LocaleUtils.getCurrentLocale()

        Map<String, Map> selectedExportFields = [:]

        Map<String, Object> configFields = getExportTippFields()

        configFields.keySet().each { String k ->
            if (k in selectedFields.keySet() ) {
                selectedExportFields.put(k, configFields.get(k))
            }
        }

        saveClickMeConfig(selectedExportFields, "getDefaultExportIssueEntitlementConfig")

        List titles = _exportTitles(selectedExportFields, locale, null, null, null, null, format)

        List exportData = buildTippRows(result, selectedExportFields, format)

        Map sheetData = [:]
        sheetData[messageSource.getMessage('title.plural', null, locale)] = [titleRow: titles, columnData: exportData]

        switch(format) {
            case FORMAT.XLS:
                return exportService.generateXLSXWorkbook(sheetData)
            case FORMAT.CSV:
                return exportService.generateSeparatorTableString(titles, exportData, '|')
            case FORMAT.TSV:
                return exportService.generateSeparatorTableString(titles, exportData, '\t')
            case FORMAT.PDF:
                return [mainHeader: titles, pages: [[titleRow: titles, columnData: exportData]]]
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
     * @param multiYearTermFourSurvey should four years running times appear?
     * @param multiYearTermFiveSurvey should five years running times appear?
     * @param format the format to use for export
     */
    private void _setRenewalRow(Map participantResult, Map<String, Object> selectedFields, List renewalData, boolean onlySubscription, List costItemElements, Map selectedCostItemFields, FORMAT format, Set<String> contactSources){
        SurveyOrg surveyOrg
        if (participantResult.surveyConfig && participantResult.participant) {
            surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(participantResult.surveyConfig, participantResult.participant)
        }

        List costItems
            if(costItemElements.size() > 0){
                if (onlySubscription && participantResult.subForCostItems) {
                    costItems = CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqualAndOwner(participantResult.subForCostItems, costItemElements, RDStore.COST_ITEM_DELETED, participantResult.surveyOwner, [sort: 'costItemElement'])
                }else{
                    if(surveyOrg){
                        costItems = CostItem.findAllBySurveyOrgAndCostItemElementInListAndCostItemStatusNotEqualAndPkgIsNullAndSurveyConfigSubscriptionIsNull(surveyOrg, costItemElements, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
                    }
                }
            }

        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('surveyProperty.')) {
                    if (onlySubscription) {
                        row.add(createTableCell(format, ' '))
                        if('surveyPropertyParticipantComment' in selectedFields.keySet()) {
                            row.add(createTableCell(format, ' '))
                        }
                        if('surveyPropertyCommentOnlyForOwner' in selectedFields.keySet()) {
                            row.add(createTableCell(format, ' '))
                        }
                    } else {
                        Long id = Long.parseLong(fieldKey.split("\\.")[1])
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, PropertyDefinition.get(id))
                        String resultStr = participantResultProperty.getResult() ?: " ", comment = participantResultProperty.comment ?: " ", ownerComment = participantResultProperty.ownerComment ?: " "

                        row.add(createTableCell(format, resultStr))
                        if('surveyPropertyParticipantComment' in selectedFields.keySet()) {
                            row.add(createTableCell(format, comment))
                        }
                        if('surveyPropertyCommentOnlyForOwner' in selectedFields.keySet()) {
                            row.add(createTableCell(format, ownerComment))
                        }
                    }
                } else if (fieldKey == 'survey.period') {
                    String period = ""
                    if (participantResult.multiYearTermTwoSurvey) {
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermTwoSurvey)
                        if (participantResultProperty && participantResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            period = participantResult.newSubPeriodTwoStartDate ? sdf.format(participantResult.newSubPeriodTwoStartDate) : " "
                            period = participantResult.newSubPeriodTwoEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodTwoEndDate) : " "
                        }
                    }

                    if (participantResult.multiYearTermThreeSurvey) {
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermThreeSurvey)
                        if (participantResultProperty && participantResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            period = participantResult.newSubPeriodThreeStartDate ? sdf.format(participantResult.newSubPeriodThreeStartDate) : " "
                            period = participantResult.newSubPeriodThreeEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodThreeEndDate) : " "
                        }
                    }

                    if (participantResult.multiYearTermFourSurvey) {
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermFourSurvey)
                        if (participantResultProperty && participantResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            period = participantResult.newSubPeriodFourStartDate ? sdf.format(participantResult.newSubPeriodFourStartDate) : " "
                            period = participantResult.newSubPeriodFourEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodFourEndDate) : " "
                        }
                    }

                    if (participantResult.multiYearTermFiveSurvey) {
                        SurveyResult participantResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermFiveSurvey)
                        if (participantResultProperty && participantResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            period = participantResult.newSubPeriodFiveStartDate ? sdf.format(participantResult.newSubPeriodFiveStartDate) : " "
                            period = participantResult.newSubPeriodFiveEndDate ? period + " - " + sdf.format(participantResult.newSubPeriodFiveEndDate) : " "
                        }
                    }

                    row.add(createTableCell(format, period))
                } else if (fieldKey == 'survey.ownerComment') {
                    String ownerComment = ""
                    if (surveyOrg) {
                        ownerComment = surveyOrg.ownerComment
                    }
                    row.add(createTableCell(format, ownerComment))
                }
                else if (fieldKey == 'vendorSurvey') {
                    SurveyVendorResult surveyVendorResult = SurveyVendorResult.findBySurveyConfigAndParticipant(participantResult.surveyConfig, participantResult.participant)
                    if (surveyVendorResult) {
                        row.add(createTableCell(format, surveyVendorResult.vendor.name))
                       /* row.add(createTableCell(format, surveyVendorResult.comment))
                        row.add(createTableCell(format, surveyVendorResult.ownerComment))*/
                    }else {
                        row.add(createTableCell(format, ''))
                       /* row.add(createTableCell(format, ''))
                        row.add(createTableCell(format, ''))*/
                    }
                }
                else if (fieldKey == 'survey.finishDate') {
                    String finishDate = ""
                    if (surveyOrg && surveyOrg.finishDate) {
                        finishDate = sdf.format(surveyOrg.finishDate)
                    }
                    row.add(createTableCell(format, finishDate))
                }
                else if (fieldKey == 'survey.reminderMailDate') {
                    String reminderMailDate = ""
                    if (surveyOrg && surveyOrg.reminderMailDate) {
                        reminderMailDate = sdf.format(surveyOrg.reminderMailDate)
                    }
                    row.add(createTableCell(format, reminderMailDate))
                }
                else if (fieldKey == 'survey.person') {
                    String personString = ""
                    if (surveyOrg) {
                        List emails = []
                        List<SurveyPersonResult> personResults = SurveyPersonResult.findAllByParticipantAndSurveyConfigAndBillingPerson(surveyOrg.org, surveyOrg.surveyConfig, true)
                        personResults.each { SurveyPersonResult personResult ->
                            personResult.person.contacts.each {
                                if (it.contentType == RDStore.CCT_EMAIL)
                                    emails << it.content
                            }
                        }
                        personString = emails.join('; ')
                    }
                    row.add(createTableCell(format, personString, surveyOrg && surveyService.modificationToContactInformation(surveyOrg) ? 'negative' : ''))
                }
                else if (fieldKey == 'survey.surveyPerson') {
                    String personString = ""
                    if (surveyOrg) {
                        List emails = []
                        List<SurveyPersonResult> personResults = SurveyPersonResult.findAllByParticipantAndSurveyConfigAndSurveyPerson(surveyOrg.org, surveyOrg.surveyConfig, true)
                        personResults.each { SurveyPersonResult personResult ->
                            personResult.person.contacts.each {
                                if (it.contentType == RDStore.CCT_EMAIL)
                                    emails << it.content
                            }
                        }
                        personString = emails.join('; ')
                    }
                    row.add(createTableCell(format, personString, surveyOrg && surveyService.modificationToContactInformation(surveyOrg) ? 'negative' : ''))
                }
                else if (fieldKey == 'survey.address') {
                    String address = ""
                    if (surveyOrg && surveyOrg.address) {
                        address = _getAddress(surveyOrg.address, surveyOrg.org)
                    }
                    row.add(createTableCell(format, address, surveyOrg && surveyService.modificationToContactInformation(surveyOrg) ? 'negative' : ''))
                }
                else if (fieldKey == 'survey.eInvoicePortal') {
                    String eInvoicePortal = ""
                    if (surveyOrg && surveyOrg.eInvoicePortal) {
                        eInvoicePortal = surveyOrg.eInvoicePortal.getI10n('value')
                    }
                    row.add(createTableCell(format, eInvoicePortal))
                }
                else if (fieldKey == 'survey.eInvoiceLeitwegId') {
                    String eInvoiceLeitwegId = ""
                    if (surveyOrg && surveyOrg.eInvoiceLeitwegId) {
                        eInvoiceLeitwegId = surveyOrg.eInvoiceLeitwegId
                    }
                    row.add(createTableCell(format, eInvoiceLeitwegId))
                }
                else if (fieldKey == 'survey.eInvoiceLeitkriterium') {
                    String eInvoiceLeitkriterium = ""
                    if (surveyOrg && surveyOrg.eInvoiceLeitkriterium) {
                        eInvoiceLeitkriterium = surveyOrg.eInvoiceLeitkriterium
                    }
                    row.add(createTableCell(format, eInvoiceLeitkriterium))
                }
                else if (fieldKey == 'survey.periodComment') {
                    String twoComment = participantResult.participantPropertyTwoComment ?: ' '
                    String threeComment = participantResult.participantPropertyThreeComment ?: ' '
                    String fourComment = participantResult.participantPropertyFourComment ?: ' '
                    String fiveComment = participantResult.participantPropertyFiveComment ?: ' '
                    String participantPropertyMultiYearComment = ' '
                    if (participantResult.multiYearTermTwoSurvey) {
                        SurveyResult participantMultiYearTermResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermTwoSurvey)
                        if (participantMultiYearTermResultProperty && participantMultiYearTermResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            participantPropertyMultiYearComment = twoComment
                        }
                    }

                    if (participantResult.multiYearTermThreeSurvey) {
                        SurveyResult participantMultiYearTermResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermThreeSurvey)
                        if (participantMultiYearTermResultProperty && participantMultiYearTermResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            participantPropertyMultiYearComment = threeComment
                        }
                    }

                    if (participantResult.multiYearTermFourSurvey) {
                        SurveyResult participantMultiYearTermResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermFourSurvey)
                        if (participantMultiYearTermResultProperty && participantMultiYearTermResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            participantPropertyMultiYearComment = fourComment
                        }
                    }

                    if (participantResult.multiYearTermFiveSurvey) {
                        SurveyResult participantMultiYearTermResultProperty = SurveyResult.findBySurveyConfigAndParticipantAndType(participantResult.surveyConfig, participantResult.participant, participantResult.multiYearTermFiveSurvey)
                        if (participantMultiYearTermResultProperty && participantMultiYearTermResultProperty.refValue?.id == RDStore.YN_YES.id) {
                            participantPropertyMultiYearComment = fiveComment
                        }
                    }

                    if (!participantResult.multiYearTermTwoSurvey && !participantResult.multiYearTermThreeSurvey && !participantResult.multiYearTermFourSurvey && !participantResult.multiYearTermFiveSurvey) {
                        row.add(createTableCell(format, ' '))
                    }else {
                        row.add(createTableCell(format, participantPropertyMultiYearComment))
                    }
                }
                else if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, 'publicAddress')
                }
                /*
                else if (fieldKey == 'participantContact.General contact person') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participantContact.Functional Contact Billing Adress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }
                */
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, participantResult.sub)
                }else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }
                else if (fieldKey.contains('discoverySystems')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                } else if (fieldKey == 'renewalSurveyCostItems') {
                    if (costItems && selectedCostItemFields.size() > 0) {
                        for (int c = 0; c < costItemElements.size(); c++) {
                            CostItem costItem
                            if (c < costItems.size())
                                costItem = costItems.get(c)
                            if (costItem) {
                                String cieVal = costItem.costItemElement ? costItem.costItemElement.getI10n('value') : ''
                                row.add(createTableCell(format, cieVal))
                                selectedCostItemFields.each {
                                    if (it.key == 'costItem.costPeriod') {
                                        String period = ""
                                        period = costItem.startDate ? sdf.format(costItem.startDate) : " "
                                        period = costItem.endDate ? period + " - " + sdf.format(costItem.endDate) : " "
                                        row.add(createTableCell(format, period))
                                    } else if (it.key == 'costItem.costInBillingCurrencyAfterTax') {
                                        def fieldValue
                                        if (costItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                            fieldValue = ' '
                                        else
                                            fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                        row.add(createTableCell(format, fieldValue))

                                    } else if (it.key == 'costItem.taxRate') {
                                        def fieldValue
                                        if (costItem.taxKey == CostItem.TAX_TYPES.TAX_REVERSE_CHARGE)
                                            fieldValue = RDStore.TAX_TYPE_REVERSE_CHARGE.getI10n('value')
                                        else
                                            fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                        row.add(createTableCell(format, fieldValue))
                                    } else {
                                        def fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                        row.add(createTableCell(format, fieldValue))
                                    }
                                }
                            } else {
                                row.add(createTableCell(format, ' '))
                                for (int e = 0; e < selectedCostItemFields.size(); e++) {
                                    row.add(createTableCell(format, ' '))
                                }
                            }
                        }
                    } else if (selectedCostItemFields.size() > 0) {
                        for (int c = 0; c < costItemElements.size(); c++) {
                            row.add(createTableCell(format, ' '))
                            selectedCostItemFields.each {
                                row.add(createTableCell(format, ' '))
                            }
                        }
                    }
                }
                else {
                    if (onlySubscription) {
                       if (fieldKey.startsWith('subscription.') || fieldKey.startsWith('participant.')) {
                            def fieldValue = _getFieldValue(participantResult, field, sdf)
                            row.add(createTableCell(format, fieldValue))
                        } else {
                            row.add(createTableCell(format, ' '))
                        }

                    } else {
                        if(fieldKey != 'surveyPropertyParticipantComment' && fieldKey != 'surveyPropertyCommentOnlyForOwner') {
                            def fieldValue = _getFieldValue(participantResult, field, sdf)
                            row.add(createTableCell(format, fieldValue))
                        }
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
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which set of contacts should be considered (public or private)?
     */
    private void _setSubRow(def result, Map<String, Object> selectedFields, List exportData, String localizedName, Map selectedCostItemElements, Map selectedCostItemFields, FORMAT format, Set<String> contactSources = []){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()
        Org org, contextOrg = contextService.getOrg()
        Subscription subscription
        boolean rowWithCost = false
        if(result instanceof Subscription) {
            subscription = result
            org = subscription.getSubscriberRespConsortia()
        }
        else {
            subscription = result.sub
            //rowWithCost = true  keep only while RC compatibility in order to be able to test current RC state
            if(result.containsKey('org'))
                org = result.org
            else org = result.orgs
        }


        List costItems
        Map<String, Object> costItemSums = [:]
        //in order to distinguish between sums and entire items
        if(!rowWithCost) {
            if(selectedCostItemElements.containsKey('all')){
                costItems = CostItem.findAllBySubAndCostItemElementInListAndCostItemStatusNotEqualAndPkgIsNull(subscription, selectedCostItemElements.all, RDStore.COST_ITEM_DELETED, [sort: 'costItemElement'])
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
        }

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(org, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(org, row, fieldKey, format, null, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(org, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(org, row, fieldKey, format, null, 'publicAddress')
                }
                /*
                else if (fieldKey == 'participantContact.Functional Contact Billing Adress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey)
                }
                */
                else if (fieldKey == 'subscription.consortium') {
                    row.add(createTableCell(format, subscription.getConsortium()?.name))
                }
                else if (fieldKey == 'license.consortium') {
                    row.add(createTableCell(format, subscription.getConsortium()?.name))
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format, subscription)
                }
                else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.contains('discoverySystems')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if(fieldKey.contains('subscription.notes')) { //subscription.notes and subscription.notes.shared
                    Map<String, Object> subNotes = _getNotesForObject(subscription)
                    if(fieldKey == 'subscription.notes')
                        row.add(createTableCell(format, subNotes.baseItems.join('\n')))
                    else if(fieldKey == 'subscription.notes.shared')
                        row.add(createTableCell(format, subNotes.sharedItems.join('\n')))
                }
                else if(fieldKey.contains('license.notes')) { //license.notes and license.notes.shared
                    Map<String, Object> licNotes = _getNotesForObject(subscription.licenses)
                    if(fieldKey == 'license.notes')
                        row.add(createTableCell(format, licNotes.baseItems.join('\n')))
                    else if(fieldKey == 'license.notes.shared')
                        row.add(createTableCell(format, licNotes.sharedItems.join('\n')))
                }
                else if(fieldKey == 'package.name') {
                    row.add(createTableCell(format, subscription.packages.pkg.name.join('; ')))
                }
                else if(fieldKey == 'platform.name') {
                    row.add(createTableCell(format, Platform.executeQuery('select distinct(plat.name) from SubscriptionPackage sp join sp.pkg pkg join pkg.nominalPlatform plat where sp.subscription = :sub', [sub: subscription]).join('; ')))
                }
                else if(fieldKey == 'platform.url') {
                    row.add(createTableCell(format, Platform.executeQuery('select distinct(plat.primaryUrl) from SubscriptionPackage sp join sp.pkg pkg join pkg.nominalPlatform plat where sp.subscription = :sub', [sub: subscription]).join('; ')))
                }
                else if(fieldKey.startsWith('identifiers.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    List<String> identifierList = Identifier.executeQuery("select ident.value from Identifier ident where ident.sub = :sub and ident.ns.id = :namespace and ident.value != :unknown and ident.value != ''", [sub: subscription, namespace: id, unknown: IdentifierNamespace.UNKNOWN])
                    if (identifierList) {
                        row.add(createTableCell(format, identifierList.join("; ")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
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
                else if (fieldKey == 'subscription.altnames') {
                    if(subscription.altnames) {
                        row.add(createTableCell(format, subscription.altnames.collect { AlternativeName alt -> alt.name }.join('\n')))
                    }
                    else row.add(createTableCell(format, ' '))
                }
                else if (fieldKey.startsWith('participantSubProperty.') || fieldKey.startsWith('subProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    String query = "select prop from SubscriptionProperty prop where prop.owner = :sub and prop.type.id in (:propertyDefs) and (prop.isPublic = true or prop.instanceOf != null or prop.tenant = :contextOrg) order by prop.type.${localizedName} asc"
                    List<SubscriptionProperty> subscriptionProperties = SubscriptionProperty.executeQuery(query,[sub:subscription, propertyDefs:[id], contextOrg: contextService.getOrg()])
                    if(subscriptionProperties){
                        List<String> values = [], notes = []
                        subscriptionProperties.each { SubscriptionProperty sp ->
                            values << sp.getValueInI10n()
                            notes << sp.note != null ? sp.note : ' '
                        }
                        row.add(createTableCell(format, values.join('; ')))
                        row.add(createTableCell(format, notes.join('; ')))
                    }else{
                        row.add(createTableCell(format, ' '))
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
                else if (fieldKey == 'subscription.survey') {
                    SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)
                    String dateString = ''
                    String style = ''

                    if(surveyConfig) {
                        dateString = (surveyConfig.surveyInfo.startDate ? sdf.format(surveyConfig.surveyInfo.startDate) : '') + ' - ' + (surveyConfig.surveyInfo.endDate ? sdf.format(surveyConfig.surveyInfo.endDate) : '')
                        style = surveyConfig.surveyInfo.status == RDStore.SURVEY_SURVEY_STARTED ? 'positive' : ''
                    }

                    row.add(createTableCell(format, dateString, style))
                }
                else if (fieldKey == 'subscription.survey.evaluation') {
                    SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)
                    String finishProcess = ''
                    String style = ''
                    NumberFormat formatNumber = NumberFormat.getNumberInstance()
                    formatNumber.maximumFractionDigits = 2

                    if(surveyConfig) {
                        int finish = SurveyOrg.findAllBySurveyConfigAndFinishDateIsNotNull(surveyConfig).size()
                        int total  = SurveyOrg.findAllBySurveyConfig(surveyConfig).size()
                        formatNumber.maximumFractionDigits = 2
                        finishProcess  = formatNumber.format((finish != 0 && total != 0) ? (finish / total) * 100 : 0)
                        style = finish == total ? 'positive' : ''
                    }
                    row.add(createTableCell(format, (surveyConfig ? ("${finishProcess}%") : ' '), style))
                }
                else if (fieldKey == 'subscription.survey.cancellation') {
                    SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)
                    int countOrgsWithTermination = 0
                    String style = ''
                    if(surveyConfig) {
                        countOrgsWithTermination = surveyConfig.countOrgsWithTermination()
                        style = countOrgsWithTermination > 10 ? 'negative' : ''
                    }

                    row.add(createTableCell(format, surveyConfig ? countOrgsWithTermination : ' ', style))
                }
                else if (fieldKey == 'subscription.renewalChanges') {
                    SurveyConfig surveyConfig = SurveyConfig.findBySubscriptionAndSubSurveyUseForTransfer(subscription, true)
                    int countModificationToContactInformationAfterRenewalDoc = surveyConfig ? surveyService.countModificationToContactInformationAfterRenewalDoc(subscription) : 0
                    String style = ''
                    if(surveyConfig) {
                        style = countModificationToContactInformationAfterRenewalDoc == 0 ? 'positive' : 'negative'
                    }
                    row.add(createTableCell(format, surveyConfig ? countModificationToContactInformationAfterRenewalDoc  : ' ', style))
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
                    else if(rowWithCost) {
                        //null checks because key cost *may* exist but filled up with null value
                        String cieVal = result.cost?.costItemElement ? result.cost.costItemElement.getI10n('value') : ' '
                        row.add(createTableCell(format, cieVal))
                        selectedCostItemFields.each {
                            if(result.cost) {
                                def fieldValue = _getFieldValue(result.cost, it.value.field.replace('costItem.', ''), sdf)
                                row.add(createTableCell(format, fieldValue))
                            }
                            else row.add(createTableCell(format, ' '))
                        }
                    }
                    else if(costItems && selectedCostItemFields.size() > 0){
                        for(int c = 0; c < selectedCostItemElements.all.size(); c++) {
                            CostItem costItem
                            if(c < costItems.size())
                                costItem = costItems.get(c)
                            if(costItem) {
                                String cieVal = costItem.costItemElement ? costItem.costItemElement.getI10n('value') : ''
                                row.add(createTableCell(format, cieVal))
                                selectedCostItemFields.each {
                                    def fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                    row.add(createTableCell(format, fieldValue))
                                }
                            }
                            else {
                                row.add(createTableCell(format, ' '))
                                for(int e = 0; e < selectedCostItemFields.size(); e++) {
                                    row.add(createTableCell(format, ' '))
                                }
                            }
                        }
                    }
                    else if(selectedCostItemFields.size() > 0) {
                        for(int c = 0; c < selectedCostItemElements.all.size(); c++) {
                            row.add(createTableCell(format, ' '))
                            selectedCostItemFields.each {
                                row.add(createTableCell(format, ' '))
                            }
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
     * Fills a row for the license export
     * @param result the license to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     * @param localizedName the localised name of the property name
     * @param format the {@link FORMAT} to use for export
     */
    private void _setLicRow(def result, Map<String, Object> selectedFields, List exportData, String localizedName, FORMAT format){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()
        Org org
        License license
        if(result instanceof License) {
            license = result
            org = license.getLicensee()
        }
        else {
            license = result.lic
            org = result.orgs
        }


        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('subscription.')) {
                    Set<String> linkedSubs = Subscription.executeQuery('select li.destinationSubscription.name from Links li where li.sourceLicense = :lic and li.linkType = :license', [lic: result, license: RDStore.LINKTYPE_LICENSE])
                    row.add(createTableCell(format, linkedSubs.join('; ')))
                }
                else if (fieldKey == 'consortium') {
                    row.add(createTableCell(format, license.getLicensingConsortium()?.name))
                }
                else if(fieldKey.contains('license.notes')) { //license.notes and license.notes.shared
                    Map<String, Object> licNotes = _getNotesForObject(license)
                    if(fieldKey == 'license.notes')
                        row.add(createTableCell(format, licNotes.baseItems.join('\n')))
                    else if(fieldKey == 'license.notes.shared')
                        row.add(createTableCell(format, licNotes.sharedItems.join('\n')))
                }
                else if (fieldKey.contains('altnames')) {
                    if(license.altnames) {
                        row.add(createTableCell(format, license.altnames.collect { AlternativeName alt -> alt.name }.join('\n')))
                    }
                    else row.add(createTableCell(format, ' '))
                }
                else if(fieldKey.startsWith('identifiers.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    List<String> identifierList = Identifier.executeQuery("select ident.value from Identifier ident where ident.lic = :license and ident.ns.id = :namespace and ident.value != :unknown and ident.value != ''", [lic: license, namespace: id, unknown: IdentifierNamespace.UNKNOWN])
                    if (identifierList) {
                        row.add(createTableCell(format, identifierList.join("; ")))
                    } else {
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey.startsWith('participantLicProperty.') || fieldKey.startsWith('licProperty.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    String query = "select prop from LicenseProperty prop where prop.owner = :lic and prop.type.id = :propertyDef and (prop.instanceOf != null or prop.isPublic = true or prop.tenant = :contextOrg) order by prop.type.${localizedName} asc"
                    List<LicenseProperty> licenseProperties = LicenseProperty.executeQuery(query,[lic:license, propertyDef: id, contextOrg: contextService.getOrg()])
                    if(licenseProperties){
                        List<String> values = [], notes = [], paragraphs = []
                        licenseProperties.each { LicenseProperty lp ->
                            if(lp.isVisibleExternally()) {
                                //ternary operator / elvis delivers strange results ...
                                if(lp.getValueInI10n() != null) {
                                    values << lp.getValueInI10n()
                                }
                                else values << ' '
                                if(lp.note != null) {
                                    notes << lp.note
                                }
                                else notes << ' '
                                if(lp.paragraph != null) {
                                    paragraphs << lp.paragraph
                                }
                                else paragraphs << ' '
                            }
                        }
                        row.add(createTableCell(format, values.join('; ')))
                        row.add(createTableCell(format, notes.join('; ')))
                        row.add(createTableCell(format, paragraphs.join('; ')))
                    }else{
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
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
     * @param format the {@link FORMAT} to be exported
     */
    private void _setCostItemRow(CostItem costItem, Map<String, Object> selectedFields, List exportData, FORMAT format, Set<String> contactSources){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()

        Org org = null

        if(costItem.sub)
            org = costItem.sub.getSubscriberRespConsortia()

        if(costItem.surveyOrg)
            org = costItem.surveyOrg.org

        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                /*
                if (fieldKey == 'participantContact.General contact person') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }else if (fieldKey == 'participantContact.Functional Contact Billing Adress') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey == 'participant.billingAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey == 'participant.postAdress') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                */
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(org, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(org, row, fieldKey, format, null, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(org, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(org, row, fieldKey, format, null, 'publicAddress')
                }
                else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format, costItem.sub)
                }
                else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.contains('discoverySystems')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(org, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('costInformation.')) {
                    Long id = Long.parseLong(fieldKey.split("\\.")[1])
                    if(costItem.costInformationDefinition?.id == id){
                        if(costItem.costInformationDefinition.type == RefdataValue.class.name)
                            row.add(createTableCell(format, costItem.costInformationRefValue.getI10n('value')))
                        else
                            row.add(createTableCell(format, costItem.costInformationStringValue))
                    }else{
                        row.add(createTableCell(format, ' '))
                    }
                }
                else if (fieldKey == 'subscription.consortium') {
                    row.add(createTableCell(format, costItem.sub?.getConsortium()?.name))
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
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which type of contacts should be considered (public or private)?
     * @param configMap filter parameters for further queries
     */
    private void _setOrgRow(Org result, Map<String, Object> selectedFields, List exportData, FORMAT format, Set<String> contactSources = [], Map<String, Object> configMap = [:]){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(result, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(result, row, fieldKey, format, null, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(result, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(result, row, fieldKey, format, null, 'publicAddress')
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
                else if (fieldKey.contains('altnames') || fieldKey.contains('discoverySystems')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey == 'participant.subscriptions') {
                    List<Long> subStatus = [RDStore.SUBSCRIPTION_CURRENT.id]
                    List subscriptionQueryParams
                    if(configMap.filterPvd && configMap.filterPvd != "" && filterService.listReaderWrapper(configMap, 'filterPvd')){
                        subscriptionQueryParams = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([count: true, org: result, actionName: configMap.action, status: subStatus, date_restr: configMap.subValidOn ? DateUtils.parseDateGeneric(configMap.subValidOn) : null, providers: filterService.listReaderWrapper(configMap, 'filterPvd')])
                    }else {
                        subscriptionQueryParams = subscriptionsQueryService.myInstitutionCurrentSubscriptionsBaseQuery([count: true, org: result, actionName: configMap.action, status: subStatus, date_restr: configMap.subValidOn ? DateUtils.parseDateGeneric(configMap.subValidOn) : null])
                    }
                    int countOfSubscriptions = Subscription.executeQuery("select count(s) " + subscriptionQueryParams[0], subscriptionQueryParams[1])[0]
                    row.add(createTableCell(format, countOfSubscriptions))
                }
                else if (fieldKey == 'participant.readerNumbers') {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantProperty.')) {
                    _setOrgFurtherInformation(result, row, fieldKey, format)
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
     * Fills a row for the vendor export
     * @param result the {@link Vendor} record to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which type of contacts should be considered (public or private)?
     * @param configMap filter parameters for further queries
     */
    private void _setVendorRow(Vendor result, Map<String, Object> selectedFields, List exportData, FORMAT format, Set<String> contactSources = []){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Locale locale = LocaleUtils.getCurrentLocale()
        Org context = contextService.getOrg()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setVendorFurtherInformation(result, row, fieldKey, format, contactSwitch)
                        }
                    }
                    else _setVendorFurtherInformation(result, row, fieldKey, format, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setVendorFurtherInformation(result, row, fieldKey, format, contactSwitch)
                        }
                    }
                    else _setVendorFurtherInformation(result, row, fieldKey, format, 'publicAddress')
                }
                else if (fieldKey.startsWith('vendorIdentifiers.')) {
                    _setVendorFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('vendorCustomerIdentifiers.')) {
                    _setVendorFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('vendorProperty.')) {
                    _setVendorFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.contains('altnames')) {
                    _setVendorFurtherInformation(result, row, fieldKey, format)
                }
                else {
                    switch(fieldKey) {
                        case 'vendor.electronicBillings':
                            row.add(createTableCell(format, result.electronicBillings.collect { ElectronicBilling eb -> eb.invoicingFormat.getI10n('value') }.join('; ')))
                            break
                        case 'vendor.electronicDeliveryDelays':
                            row.add(createTableCell(format, result.electronicDeliveryDelays.collect { ElectronicDeliveryDelayNotification eddn -> eddn.delayNotification.getI10n('value') }.join('; ')))
                            break
                        case 'vendor.invoiceDispatchs':
                            row.add(createTableCell(format, result.invoiceDispatchs.collect { InvoiceDispatch id -> id.invoiceDispatch.getI10n('value') }.join('; ')))
                            break
                        case 'vendor.licenses':
                            String consortiaFilter = ''
                            if(context.isCustomerType_Consortium())
                                consortiaFilter = ' and l.instanceOf = null'
                            List nameOfLicenses = Subscription.executeQuery('select l.reference from VendorRole vr join vr.license l, OrgRole oo where vr.license = oo.lic and vr.vendor = :vendor and l.status = :current and oo.org = :context'+consortiaFilter, [vendor: result, current: RDStore.LICENSE_CURRENT, context: context])
                            row.add(createTableCell(format, nameOfLicenses.join('\n')))
                            break
                        case 'vendor.packages':
                            Set<Package> distinctPackages = []
                            distinctPackages.addAll(result.packages.pkg)
                            String pkgString
                            Object[] count = [distinctPackages.size()-20]
                            if(distinctPackages.size() >= 20)
                                pkgString = "${distinctPackages.take(20).name.join('\n')}\n${messageSource.getMessage('default.export.furtherPackages', count, locale)}"
                            else pkgString = distinctPackages.name.join('\n')
                            row.add(createTableCell(format, pkgString))
                            break
                        case 'vendor.platforms':
                            SortedSet<Platform> distinctPlatforms = new TreeSet<Platform>()
                            distinctPlatforms.addAll(result.packages.pkg.nominalPlatform)
                            String platString
                            Object[] count = [distinctPlatforms.size()-20]
                            if(distinctPlatforms.size() >= 20)
                                platString = "${distinctPlatform.take(20).name.join('\n')}\n${messageSource.getMessage('default.export.furtherPlatforms', count, locale)}"
                            else platString = distinctPlatforms.name.join('\n')
                            row.add(createTableCell(format, platString))
                            break
                        case 'vendor.subscriptions':
                            String consortiaFilter = ''
                            if(context.isCustomerType_Consortium())
                                consortiaFilter = ' and (s.instanceOf = null or not exists(select vri from VendorRole vri where vri.subscription = s.instanceOf))'
                            List nameOfSubscriptions = Subscription.executeQuery('select s.name from VendorRole vr join vr.subscription s, OrgRole oo where s = oo.sub and vr.vendor = :vendor and s.status = :current and oo.org = :context'+consortiaFilter+' order by s.name', [vendor: result, current: RDStore.SUBSCRIPTION_CURRENT, context: context])
                            row.add(createTableCell(format, nameOfSubscriptions.join('; ')))
                            break
                        case 'vendor.supportedLibrarySystems':
                            row.add(createTableCell(format, result.supportedLibrarySystems.collect { LibrarySystem ls -> ls.librarySystem.getI10n('value') }.join('; ')))
                            break
                        default:
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
                            break
                    }
                }
            }
        }
        exportData.add(row)
    }

    /**
     * Fills a row for the provider export
     * @param result the {@link Provider} record to export
     * @param selectedFields the fields which should appear
     * @param exportData the list containing the export rows
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which type of contacts should be considered (public or private)?
     * @param configMap filter parameters for further queries
     */
    private void _setProviderRow(Provider result, Map<String, Object> selectedFields, List exportData, FORMAT format, Set<String> contactSources = []){
        List row = []
        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        Org context = contextService.getOrg()
        selectedFields.keySet().each { String fieldKey ->
            Map mapSelecetedFields = selectedFields.get(fieldKey)
            String field = mapSelecetedFields.field
            if(!mapSelecetedFields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setProviderFurtherInformation(result, row, fieldKey, format, contactSwitch)
                        }
                    }
                    else _setProviderFurtherInformation(result, row, fieldKey, format, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setProviderFurtherInformation(result, row, fieldKey, format, contactSwitch)
                        }
                    }
                    else _setProviderFurtherInformation(result, row, fieldKey, format, 'publicAddress')
                }
                else if (fieldKey.startsWith('providerIdentifiers.')) {
                    _setProviderFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('providerCustomerIdentifiers.')) {
                    _setProviderFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.startsWith('providerProperty.')) {
                    _setProviderFurtherInformation(result, row, fieldKey, format)
                }
                else if (fieldKey.contains('altnames')) {
                    _setProviderFurtherInformation(result, row, fieldKey, format)
                }
                else {
                    switch(fieldKey) {
                        case 'provider.electronicBillings':
                            row.add(createTableCell(format, result.electronicBillings.collect { ElectronicBilling eb -> eb.invoicingFormat.getI10n('value') }.join('; ')))
                            break
                        case 'provider.invoiceDispatchs':
                            row.add(createTableCell(format, result.invoiceDispatchs.collect { InvoiceDispatch id -> id.invoiceDispatch.getI10n('value') }.join('; ')))
                            break
                        case 'provider.licenses':
                            String consortiaFilter = ''
                            if(context.isCustomerType_Consortium())
                                consortiaFilter = ' and l.instanceOf = null'
                            List nameOfLicenses = Subscription.executeQuery('select l.reference from ProviderRole pvr join pvr.license l, OrgRole oo where pvr.license = oo.lic and pvr.provider = :provider and l.status = :current and oo.org = :context'+consortiaFilter, [provider: result, current: RDStore.LICENSE_CURRENT, context: context])
                            row.add(createTableCell(format, nameOfLicenses.join('\n')))
                            break
                        case 'provider.packages':
                            String packageNames
                            if(result.packages.size() > 10) {
                                Set<Package> packageSubSet = result.packages.take(10)
                                packageNames = "${packageSubSet.name.join('\n')} ${messageSource.getMessage('export.overflow', [result.packages.size()-10] as Object[], LocaleUtils.getCurrentLocale())}"
                            }
                            else packageNames = result.packages.name.join('\n')
                            row.add(createTableCell(format, packageNames))
                            break
                        case 'provider.platforms':
                            String platformNames
                            if(result.platforms.size() > 10) {
                                Set<Platform> platformSubSet = result.platforms.take(10)
                                platformNames = "${platformSubSet.name.join('\n')} ${messageSource.getMessage('export.overflow', [result.platforms.size()-10] as Object[], LocaleUtils.getCurrentLocale())}"
                            }
                            else platformNames = result.platforms.name.join('\n')
                            row.add(createTableCell(format, platformNames))
                            break
                        case 'provider.subscriptions':
                            String consortiaFilter = ''
                            if(context.isCustomerType_Consortium())
                                consortiaFilter = ' and s.instanceOf = null'
                            List nameOfSubscriptions = Subscription.executeQuery('select s.name from ProviderRole pvr join pvr.subscription s, OrgRole oo where pvr.subscription = oo.sub and pvr.provider = :provider and s.status = :current and oo.org = :context'+consortiaFilter, [provider: result, current: RDStore.SUBSCRIPTION_CURRENT, context: context])
                            row.add(createTableCell(format, nameOfSubscriptions.join('; ')))
                            break
                        default:
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
                            break
                    }
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
     * @param format the {@link FORMAT} to be exported
     * @param contactSources which set of contacts should be considered?
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

                    if(participantResultProperty) {
                        String result = participantResultProperty.getResult() ?: " ", comment = participantResultProperty.comment ?: " ", ownerComment = participantResultProperty.ownerComment ?: " "

                        row.add(createTableCell(format, result))
                        if('surveyPropertyParticipantComment' in selectedFields.keySet()) {
                            row.add(createTableCell(format, comment))
                        }
                        if('surveyPropertyCommentOnlyForOwner' in selectedFields.keySet()) {
                            row.add(createTableCell(format, ownerComment))
                        }
                    }else{
                        row.add(createTableCell(format, ' '))
                        if('surveyPropertyParticipantComment' in selectedFields.keySet()) {
                            row.add(createTableCell(format, ' '))
                        }
                        if('surveyPropertyCommentOnlyForOwner' in selectedFields.keySet()) {
                            row.add(createTableCell(format, ' '))
                        }
                    }
                }
                else if (fieldKey.contains('Contact.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, 'publicContact')
                }
                else if (fieldKey.contains('Address.')) {
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, contactSwitch)
                        }
                    }
                    else _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format, null, 'publicAddress')
                }
                /*
               else if (fieldKey == 'participant.billingAdress') {
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
                }else if (fieldKey.contains('discoverySystems')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
                    _setOrgFurtherInformation(participantResult.participant, row, fieldKey, format)
                }else if (fieldKey == 'participantSurveyCostItems') {
                    for(int c = 0; c < participantResult.selectedCostItemElementsForSurveyCostItems.size(); c++) {
                        CostItem costItem
                        if(c < participantResult.surveyCostItems.size())
                            costItem = participantResult.surveyCostItems.get(c)
                        if(costItem) {
                            String cieVal = costItem.costItemElement ? costItem.costItemElement.getI10n('value') : ''
                            row.add(createTableCell(format, cieVal))
                            selectedCostItemFields.forSurveyCostItems.each {
                                def fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                row.add(createTableCell(format, fieldValue))
                            }
                        }
                        else {
                            row.add(createTableCell(format, ' '))
                            for(int e = 0; e < selectedCostItemFields.size(); e++) {
                                row.add(createTableCell(format, ' '))
                            }
                        }
                    }
                }else if (fieldKey == 'participantSurveySubCostItems') {
                    for(int c = 0; c < participantResult.selectedCostItemElementsForSubCostItems.size(); c++) {
                        CostItem costItem
                        if(c < participantResult.subCostItems.size())
                            costItem = participantResult.subCostItems.get(c)
                        if(costItem) {
                            String cieVal = costItem.costItemElement ? costItem.costItemElement.getI10n('value') : ''
                            row.add(createTableCell(format, cieVal))
                            selectedCostItemFields.forSubCostItems.each {
                                def fieldValue = _getFieldValue(costItem, it.value.field.replace('costItem.', ''), sdf)
                                row.add(createTableCell(format, fieldValue))
                            }
                        }
                        else {
                            row.add(createTableCell(format, ' '))
                            for(int e = 0; e < selectedCostItemFields.size(); e++) {
                                row.add(createTableCell(format, ' '))
                            }
                        }
                    }
                } else if (fieldKey == 'survey.ownerComment') {
                    row.add(createTableCell(format, participantResult.surveyOrg.ownerComment))
                }else if (fieldKey == 'vendorSurvey') {
                    SurveyVendorResult surveyVendorResult = SurveyVendorResult.findBySurveyConfigAndParticipant(participantResult.surveyConfig, participantResult.participant)
                    if (surveyVendorResult) {
                        row.add(createTableCell(format, surveyVendorResult.vendor.name))
                       /* row.add(createTableCell(format, surveyVendorResult.comment))
                        row.add(createTableCell(format, surveyVendorResult.ownerComment))*/
                    }else {
                        row.add(createTableCell(format, ''))
                       /* row.add(createTableCell(format, ''))
                        row.add(createTableCell(format, ''))*/
                    }
                } else if (fieldKey == 'survey.finishDate') {
                    String finishDate = ""
                    if (participantResult.surveyOrg.finishDate) {
                        finishDate = sdf.format(participantResult.surveyOrg.finishDate)
                    }
                    row.add(createTableCell(format, finishDate))
                }
                else if (fieldKey == 'survey.reminderMailDate') {
                    String reminderMailDate = ""
                    if (participantResult.surveyOrg.reminderMailDate) {
                        reminderMailDate = sdf.format(participantResult.surveyOrg.reminderMailDate)
                    }
                    row.add(createTableCell(format, reminderMailDate))
                }
                else if (fieldKey == 'survey.person') {
                    String personString = ""
                    if (participantResult.surveyOrg) {
                        List emails = []
                        List<SurveyPersonResult> personResults = SurveyPersonResult.findAllByParticipantAndSurveyConfigAndBillingPerson(participantResult.surveyOrg.org, participantResult.surveyOrg.surveyConfig, true)
                        personResults.each { SurveyPersonResult personResult ->
                            personResult.person.contacts.each {
                                if (it.contentType == RDStore.CCT_EMAIL)
                                    emails << it.content
                            }
                        }
                        personString = emails.join('; ')
                    }
                    row.add(createTableCell(format, personString, participantResult.surveyOrg && surveyService.modificationToContactInformation(participantResult.surveyOrg) ? 'negative' : ''))
                }
                else if (fieldKey == 'survey.surveyPerson') {
                    String personString = ""
                    if (participantResult.surveyOrg) {
                        List emails = []
                        List<SurveyPersonResult> personResults = SurveyPersonResult.findAllByParticipantAndSurveyConfigAndSurveyPerson(participantResult.surveyOrg.org, participantResult.surveyOrg.surveyConfig, true)
                        personResults.each { SurveyPersonResult personResult ->
                            personResult.person.contacts.each {
                                if (it.contentType == RDStore.CCT_EMAIL)
                                    emails << it.content
                            }
                        }
                        personString = emails.join('; ')
                    }
                    row.add(createTableCell(format, personString, participantResult.surveyOrg && surveyService.modificationToContactInformation(participantResult.surveyOrg) ? 'negative' : ''))
                }
                else if (fieldKey == 'survey.address') {
                    String address = ""
                    if (participantResult.surveyOrg && participantResult.surveyOrg.address) {
                        address = _getAddress(participantResult.surveyOrg.address, participantResult.surveyOrg.org)
                    }
                    row.add(createTableCell(format, address, participantResult.surveyOrg && surveyService.modificationToContactInformation(participantResult.surveyOrg) ? 'negative' : ''))
                }
                else if (fieldKey == 'survey.eInvoicePortal') {
                    String eInvoicePortal = ""
                    if (participantResult.surveyOrg && participantResult.surveyOrg.eInvoicePortal) {
                        eInvoicePortal = participantResult.surveyOrg.eInvoicePortal.getI10n('value')
                    }
                    row.add(createTableCell(format, eInvoicePortal))
                }
                else if (fieldKey == 'survey.eInvoiceLeitwegId') {
                    String eInvoiceLeitwegId = ""
                    if (participantResult.surveyOrg && participantResult.surveyOrg.eInvoiceLeitwegId) {
                        eInvoiceLeitwegId = participantResult.surveyOrg.eInvoiceLeitwegId
                    }
                    row.add(createTableCell(format, eInvoiceLeitwegId))
                }
                else if (fieldKey == 'survey.eInvoiceLeitkriterium') {
                    String eInvoiceLeitkriterium = ""
                    if (participantResult.surveyOrg && participantResult.surveyOrg.eInvoiceLeitkriterium) {
                        eInvoiceLeitkriterium = participantResult.surveyOrg.eInvoiceLeitkriterium
                    }
                    row.add(createTableCell(format, eInvoiceLeitkriterium))
                }
                else if (fieldKey == 'pickAndChoose') {
                    double sumListPriceSelectedIEsEUR = surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(participantResult.sub, participantResult.surveyConfig, RDStore.CURRENCY_EUR)
                    double sumListPriceSelectedIEsUSD = surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(participantResult.sub, participantResult.surveyConfig, RDStore.CURRENCY_USD)
                    double sumListPriceSelectedIEsGBP = surveyService.sumListPriceInCurrencyOfIssueEntitlementsByIEGroup(participantResult.sub, participantResult.surveyConfig, RDStore.CURRENCY_GBP)

                    String titleCount = ""

                    IssueEntitlementGroup ieGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(participantResult.surveyConfig, participantResult.sub)

                    if (participantResult.surveyConfig.pickAndChoosePerpetualAccess) {
                        titleCount= "${surveyService.countPerpetualAccessTitlesBySubAndNotInIEGroup(participantResult.sub, participantResult.surveyConfig)} / ${surveyService.countIssueEntitlementsByIEGroup(participantResult.sub, participantResult.surveyConfig)}"
                    } else {
                        titleCount= "${(ieGroup ? subscriptionService.countCurrentIssueEntitlementsNotInIEGroup(participantResult.sub, ieGroup) : 0)} / ${surveyService.countIssueEntitlementsByIEGroup(participantResult.sub, participantResult.surveyConfig)}"
                    }

                    row.add(createTableCell(format, titleCount))

                    List<String> prices = []
                    if (sumListPriceSelectedIEsEUR > 0) {
                        prices << "${sumListPriceSelectedIEsEUR.round(2)}EUR"
                    }
                    if (sumListPriceSelectedIEsUSD > 0) {
                        prices << "${sumListPriceSelectedIEsUSD.round(2)}USD"
                    }
                    if (sumListPriceSelectedIEsGBP > 0) {
                        prices << "${sumListPriceSelectedIEsGBP.round(2)}GBP"
                    }

                    List<CostItem> costItemsBudget = CostItem.findAllBySubAndCostItemElementAndCostItemStatusNotEqualAndOwner(participantResult.sub, RDStore.COST_ITEM_ELEMENT_BUDGET_TITLE_PICK, RDStore.COST_ITEM_DELETED, participantResult.surveyOwner)
                    List<String> priceBudgets = []
                    List<String> priceDiffs = []
                    costItemsBudget.each { CostItem ct ->
                        if (ct.costInBillingCurrency > 0 && ct.billingCurrency == RDStore.CURRENCY_EUR) {
                            priceBudgets << "${ct.costInBillingCurrency}EUR"
                            priceDiffs << "${(ct.costInBillingCurrency - sumListPriceSelectedIEsEUR).round(2)}EUR"
                        }
                        if (ct.costInBillingCurrency > 0 && ct.billingCurrency == RDStore.CURRENCY_USD) {
                            priceBudgets << "${ct.costInBillingCurrency}USD"
                            priceDiffs << "${(ct.costInBillingCurrency - sumListPriceSelectedIEsUSD).round(2)}USD"
                        }
                        if (ct.costInBillingCurrency > 0 && ct.billingCurrency == RDStore.CURRENCY_GBP) {
                            priceBudgets << "${ct.costInBillingCurrency}GBP"
                            priceDiffs << "${(ct.costInBillingCurrency - sumListPriceSelectedIEsGBP).round(2)}GBP"
                        }

                    }

                    row.add(createTableCell(format, prices.join(';')))
                    row.add(createTableCell(format, priceBudgets.join(';')))
                    row.add(createTableCell(format, priceDiffs.join(';')))
                }else {
                    if(fieldKey != 'surveyPropertyParticipantComment' && fieldKey != 'surveyPropertyCommentOnlyForOwner') {
                        def fieldValue = _getFieldValue(participantResult, field, sdf)
                        row.add(createTableCell(format, fieldValue))
                    }
                }
            }
        }
        exportData.add(row)

    }

    /**
     * Fetches the selected issue entitlement fields via SQL query. The data is already prepared in JSON cells
     * @param ieIDs the issue entitlement set to export
     * @param selectedFields the fields which should appear
     * @param formatEnum the {@link FORMAT} to be exported
     */
    private List buildIssueEntitlementRows(Set<Long> ieIDs, Map<String, Map> selectedFields, FORMAT formatEnum){
        List result = []
        Set<String> queryCols = []
        Map<String, Object> queryArgs = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        String format = null, rdCol = I10nTranslation.getRefdataValueColumn(locale)
        if(formatEnum == FORMAT.XLS)
            format = ExportService.EXCEL
        selectedFields.eachWithIndex { String fieldKey, Map mapSelectedFields, int i ->
            String field = mapSelectedFields.field?.replaceAll("\\.", '_'), sqlCol = mapSelectedFields.sqlCol
            if(fieldKey.startsWith('issueEntitlementIdentifiers.')) {
                String argKey = "ns${i}"
                queryCols << "create_cell('${format}', (select string_agg(id_value,';') from identifier where id_tipp_fk = tipp_id and id_ns_fk = :${argKey}), null) as ${argKey}"
                queryArgs.put(argKey, Long.parseLong(fieldKey.split("\\.")[1]))
            }
            else if(fieldKey.startsWith('subscriptionIdentifiers.')) {
                String argKey = "ns${i}"
                queryCols << "create_cell('${format}', (select string_agg(id_value,';') from identifier where id_sub_fk = ie_subscription_fk and id_ns_fk = :${argKey}), null) as ${argKey}"
                queryArgs.put(argKey, Long.parseLong(fieldKey.split("\\.")[1]))
            }
            else if (fieldKey.contains('subscription.consortium')) {
                queryCols << "create_cell('${format}', (select org_name from org join org_role on org_id = or_org_fk where or_sub_fk = ie_subscription_fk and or_roletype_fk = :consortium), null) as consName"
                queryArgs.consortium = RDStore.OR_SUBSCRIPTION_CONSORTIUM.id
            }
            else if (fieldKey.contains('tipp.ddcs')) {
                queryCols << "create_cell('${format}', (select string_agg(rdv_id || ' - ' || ${rdCol}, ';') from dewey_decimal_classification join refdata_value on ddc_rv_fk = rdv_id where ddc_tipp_fk = tipp_id), null) as ddcs"
            }
            else if (fieldKey.contains('tipp.languages')) {
                queryCols << "create_cell('${format}', (select string_agg(rdv_id || ' - ' || ${rdCol}, ';') from language join refdata_value on lang_rv_fk = rdv_id where lang_tipp_fk = tipp_id), null) as languages"
            }
            else if (fieldKey.contains('tipp.providers')) {
                queryCols << "create_cell('${format}', (select string_agg(prov_name, ';') from package join provider on pkg_provider_fk = prov_id where pkg_id = tipp_pkg_fk), null) as providers"
            }
            else if (fieldKey.contains('pkg')) {
                queryCols << "create_cell('${format}', (select ${sqlCol} from package where pkg_id = tipp_pkg_fk), null) as ${field}"
            }
            else if (fieldKey.contains('platform')) {
                queryCols << "create_cell('${format}', (select ${sqlCol} from platform where plat_id = tipp_plat_fk), null) as ${field}"
            }
            else if (fieldKey.contains('ieGroup')) {
                queryCols << "create_cell('${format}', (select ${sqlCol} from issue_entitlement_group join issue_entitlement_group_item on igi_ie_group_fk = ig_id where igi_ie_fk = ie_id), null) as ${field}"
            }
            else if (fieldKey.contains('perpetualAccessBySub')) {
                queryCols << "create_cell('${format}', (select case when ie_perpetual_access_by_sub_fk is not null then '${RDStore.YN_YES.getI10n('value')}' else '${RDStore.YN_NO.getI10n('value')}' end), null) as perpetualAccessbySub"
            }
            else if (fieldKey.startsWith('coverage.')) {
                if(fieldKey.contains('startDate')) {
                    queryCols << "create_cell('${format}', to_char(coalesce(ic_start_date, tc_start_date), '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), null) as coverageStartDate"
                }
                else if(fieldKey.contains('startVolume')) {
                    queryCols << "create_cell('${format}', coalesce(ic_start_volume, tc_start_volume), null) as coverageStartVolume"
                }
                else if(fieldKey.contains('startIssue')) {
                    queryCols << "create_cell('${format}', coalesce(ic_start_issue, tc_start_issue), null) as coverageStartIssue"
                }
                else if(fieldKey.contains('endDate')) {
                    queryCols << "create_cell('${format}', to_char(coalesce(ic_end_date, tc_end_date), '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), null) as coverageEndDate"
                }
                else if(fieldKey.contains('endVolume')) {
                    queryCols << "create_cell('${format}', coalesce(ic_end_volume, tc_end_volume), null) as coverageEndVolume"
                }
                else if(fieldKey.contains('endIssue')) {
                    queryCols << "create_cell('${format}', coalesce(ic_end_issue, tc_end_issue), null) as coverageEndIssue"
                }
                else if(fieldKey.contains('coverageNote')) {
                    queryCols << "create_cell('${format}', coalesce(ic_coverage_note, tc_coverage_note), null) as coverageNote"
                }
                else if(fieldKey.contains('coverageDepth')) {
                    queryCols << "create_cell('${format}', coalesce(ic_coverage_depth, tc_coverage_depth), null) as coverageDepth"
                }
                else if(fieldKey.contains('embargo')) {
                    queryCols << "create_cell('${format}', coalesce(ic_embargo, tc_embargo), null) as embargo"
                }
            }
            else if (fieldKey.contains('listPriceEUR')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :euro order by pi_date_created desc limit 1), 'financial') as listPriceEUR"
                queryArgs.euro = RDStore.CURRENCY_EUR.id
            }
            else if (fieldKey.contains('listPriceGBP')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :gbp order by pi_date_created desc limit 1), 'financial') as listPriceGBP"
                queryArgs.gbp = RDStore.CURRENCY_GBP.id
            }
            else if (fieldKey.contains('listPriceUSD')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :usd order by pi_date_created desc limit 1), 'financial') as listPriceUSD"
                queryArgs.usd = RDStore.CURRENCY_USD.id
            }
            else if (fieldKey.contains('localPriceEUR')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_local_price')}) from price_item where pi_ie_fk = ie_id and pi_local_currency_rv_fk = :leuro order by pi_date_created desc limit 1), 'financial') as localPriceEUR"
                queryArgs.leuro = RDStore.CURRENCY_EUR.id
            }
            else if (fieldKey.contains('localPriceGBP')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_local_price')}) from price_item where pi_ie_fk = ie_id and pi_local_currency_rv_fk = :lgbp order by pi_date_created desc limit 1), 'financial') as localPriceGBP"
                queryArgs.lgbp = RDStore.CURRENCY_GBP.id
            }
            else if (fieldKey.contains('localPriceUSD')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_local_price')}) from price_item where pi_ie_fk = ie_id and pi_local_currency_rv_fk = :lusd order by pi_date_created desc limit 1), 'financial') as localPriceUSD"
                queryArgs.lusd = RDStore.CURRENCY_USD.id
            }
            else {
                if(sqlCol.contains('rv_fk')) {
                    queryCols << "create_cell('${format}', (select ${rdCol} from refdata_value where rdv_id = ${sqlCol}), null) as ${field}"
                }
                else if(sqlCol.containsIgnoreCase('date')) {
                    queryCols << "create_cell('${format}', to_char(${sqlCol}, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), null) as ${field}"
                }
                else {
                    queryCols << "create_cell('${format}', ${sqlCol}, null) as ${field}"
                }
            }
        }
        String query = "select ${queryCols.join(',')} from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id left join issue_entitlement_coverage on ic_ie_fk = ie_id left join tippcoverage on tc_tipp_fk = tipp_id where ie_id = any(:ieIDs) order by tipp_sort_name"
        result.addAll(batchQueryService.longArrayQuery(query, [ieIDs: ieIDs], queryArgs).collect { GroovyRowResult row -> row.values() })
        result
    }

    /**
     * Prepares the title data to be exported in JSON cells
     * @param tippIDs the title IDs to export
     * @param selectedFields the fields which should appear
     * @param format the {@link FORMAT} to be exported
     */
    private List buildTippRows(Set<Long> tippIDs, Map<String, Map> selectedFields, FORMAT formatEnum) {
        List result = []
        Set<String> queryCols = []
        Map<String, Object> queryArgs = [:]
        Locale locale = LocaleUtils.getCurrentLocale()
        String format = null, rdCol = I10nTranslation.getRefdataValueColumn(locale)
        if(formatEnum == FORMAT.XLS)
            format = ExportService.EXCEL
        selectedFields.eachWithIndex{ String fieldKey, Map mapSelectedFields, int i ->
            String field = mapSelectedFields.field?.replaceAll("\\.", '_'), sqlCol = mapSelectedFields.sqlCol
            if(fieldKey.startsWith('tippIdentifiers.')) {
                String argKey = "ns${i}"
                queryCols << "create_cell('${format}', (select string_agg(id_value,';') from identifier where id_tipp_fk = tipp_id and id_ns_fk = :${argKey}), null) as ${argKey}"
                queryArgs.put(argKey, Long.parseLong(fieldKey.split("\\.")[1]))
            }
            else if (fieldKey.contains('ddcs')) {
                queryCols << "create_cell('${format}', (select string_agg(rdv_id || ' - ' || ${rdCol}, ';') from dewey_decimal_classification join refdata_value on ddc_rv_fk = rdv_id where ddc_tipp_fk = tipp_id), null) as ddcs"
            }
            else if (fieldKey.contains('languages')) {
                queryCols << "create_cell('${format}', (select string_agg(rdv_id || ' - ' || ${rdCol}, ';') from language join refdata_value on lang_rv_fk = rdv_id where lang_tipp_fk = tipp_id), null) as languages"
            }
            else if (fieldKey.contains('pkg')) {
                queryCols << "create_cell('${format}', (select ${sqlCol} from package where pkg_id = tipp_pkg_fk), null) as ${field}"
            }
            else if (fieldKey.contains('platform')) {
                queryCols << "create_cell('${format}', (select ${sqlCol} from platform where plat_id = tipp_plat_fk), null) as ${field}"
            }
            else if (fieldKey == 'perpetualAccessBySub') {
                queryCols << "create_cell('${format}', (select string_agg(sub_name || ' (' || to_char(sub_start_date, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}') || '-' || to_char(sub_end_date, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}') || ')', ';') from permanent_title join subscription on pt_subscription_fk = sub_id where pt_tipp_fk = tipp_id and (pt_owner_fk = ${contextService.getOrg().id} or pt_subscription_fk in (select os.sub_parent_sub_fk from org_role join subscription as os on or_sub_fk = os.sub_id where or_org_fk = ${contextService.getOrg().id} and or_roletype_fk = ${RDStore.OR_SUBSCRIBER_CONS.id} and exists(select auc_id from audit_config where auc_reference_field = 'holdingSelection' and auc_reference_class = '${Subscription.class.name}' and auc_reference_id = os.sub_parent_sub_fk)))), null)"
            }
            else if (fieldKey.startsWith('coverage.')) {
                if(fieldKey.contains('startDate')) {
                    queryCols << "create_cell('${format}', to_char(tc_start_date, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), null) as coverageStartDate"
                }
                else if(fieldKey.contains('startVolume')) {
                    queryCols << "create_cell('${format}', tc_start_volume, null) as coverageStartVolume"
                }
                else if(fieldKey.contains('startIssue')) {
                    queryCols << "create_cell('${format}', tc_start_issue, null) as coverageStartIssue"
                }
                else if(fieldKey.contains('endDate')) {
                    queryCols << "create_cell('${format}', to_char(tc_end_date, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), null) as coverageEndDate"
                }
                else if(fieldKey.contains('endVolume')) {
                    queryCols << "create_cell('${format}', tc_end_volume, null) as coverageEndVolume"
                }
                else if(fieldKey.contains('endIssue')) {
                    queryCols << "create_cell('${format}', tc_end_issue, null) as coverageEndIssue"
                }
                else if(fieldKey.contains('coverageNote')) {
                    queryCols << "create_cell('${format}', tc_coverage_note, null) as coverageNote"
                }
                else if(fieldKey.contains('coverageDepth')) {
                    queryCols << "create_cell('${format}', tc_coverage_depth, null) as coverageDepth"
                }
                else if(fieldKey.contains('embargo')) {
                    queryCols << "create_cell('${format}', tc_embargo, null) as embargo"
                }
            }
            else if (fieldKey.contains('listPriceEUR')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :euro order by pi_date_created desc limit 1), 'financial') as listPriceEUR"
                queryArgs.euro = RDStore.CURRENCY_EUR.id
            }
            else if (fieldKey.contains('listPriceGBP')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :gbp order by pi_date_created desc limit 1), 'financial') as listPriceGBP"
                queryArgs.gbp = RDStore.CURRENCY_GBP.id
            }
            else if (fieldKey.contains('listPriceUSD')) {
                queryCols << "create_cell('${format}', (select trim(${escapeService.getFinancialOutputQuery('pi_list_price')}) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :usd order by pi_date_created desc limit 1), 'financial') as listPriceUSD"
                queryArgs.usd = RDStore.CURRENCY_USD.id
            }
            else {
                if(sqlCol.contains('rv_fk')) {
                    queryCols << "create_cell('${format}', (select ${rdCol} from refdata_value where rdv_id = ${sqlCol}), null) as ${field}"
                }
                else if(sqlCol.containsIgnoreCase('date')) {
                    queryCols << "create_cell('${format}', to_char(${sqlCol}, '${messageSource.getMessage(DateUtils.DATE_FORMAT_NOTIME,null,locale)}'), null) as ${field}"
                }
                else {
                    queryCols << "create_cell('${format}', ${sqlCol}, null) as ${field}"
                }
            }
        }
        String query = "select ${queryCols.join(',')} from title_instance_package_platform left join tippcoverage on tc_tipp_fk = tipp_id where tipp_id = any(:tippIDs) order by tipp_sort_name"
        result.addAll(batchQueryService.longArrayQuery(query, [tippIDs: tippIDs], queryArgs).collect { GroovyRowResult row -> row.values() })
        result
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

        if(fieldValue instanceof Year){
            fieldValue = fieldValue.toString()
        }

        if(fieldValue instanceof Collection){
            if(fieldValue[0] instanceof Collection)
                fieldValue = fieldValue[0].join('; ')
            else if(fieldValue[0] instanceof Date)
                fieldValue = fieldValue.collect { Date fv -> sdf.format(fv) }.join('; ')
            else if(fieldValue.find { val -> val != null })
                fieldValue = fieldValue.join('; ')
            else fieldValue = ' '
        }

        return fieldValue
    }

    /**
     * Renders the given value in a table cell in the given format. Lists are being escaped in the
     * appropriate way according to the format
     * @param format the {@link FORMAT} in which the cell should be rendered
     * @param value the value to be rendered
     * @param style the cell style for an Excel cell
     * @return the value rendered in a cell; either a {@link Map} or the value itself
     */
    def createTableCell(FORMAT format, def value, String style = null) {
        if(format == FORMAT.XLS)
            [field: value, style: style]
        else if(value instanceof String) {
            if (format == FORMAT.CSV)
                value.replaceAll('\n', ';')
            else if (format == FORMAT.TSV)
                value.replaceAll('\n', ',')
            else value
        }
        else {
            if(value == null && format in [FORMAT.CSV, FORMAT.TSV])
                ' '
            else value
        }
    }

    /**
     * Exports access points of the given institutions
     * @param orgList the list of institutions whose access points should be exported
     * @param sheetData the worksheet containing the export
     * @param selectedExportFields the fields which should appear
     * @param locale the locale to use for message constants
     * @param sheetNameAddition an addition submitted to the sheet name
     * @param format the {@link FORMAT} to be exported
     * @return the updated output in the desired format
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

    private Map _exportSurveyPackagesAndSurveyVendors(SurveyConfig surveyConfig, List<Org> orgList, Map sheetData, LinkedHashMap selectedExportFields, Locale locale, String sheetNameAddition, FORMAT format) {

        Map export = [:]
        String sheetName = ''

        if ('packageSurvey' in selectedExportFields.keySet()) {
            if (orgList) {

                export = _exportSurveyPackages(surveyConfig, orgList, format)
                sheetName = messageSource.getMessage('surveyconfig.packageSurvey.label', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }
        }

       /* if ('vendorSurvey' in selectedExportFields.keySet()) {
            if (orgList) {

                export = _exportSurveyVendors(surveyConfig, orgList, format)
                sheetName = messageSource.getMessage('surveyconfig.vendorSurvey.label', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }*/

       /* if ('packageSurveyCostItems' in selectedExportFields.keySet()) {
            if (orgList) {

                export = accessPointService.exportEZProxysOfOrgs(orgList, format, true)
                sheetName = messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName.short', null, locale) + " (${orgList.size()})" +sheetNameAddition
                sheetData[sheetName] = export
            }

        }*/

        return sheetData
    }

    /**
     * Enriches the organisation row by the given additional field in the given format
     * @param org the {@link Org} to be exported
     * @param row the export row for the organisation and which will be filled by this method
     * @param fieldKey the field to be fetched for the organisation
     * @param format the {@link FORMAT} to be used for the export
     * @param subscription unused
     * @param contactSwitch which set of contacts should be used (public or private)? Defaults to publicContact
     */
    private void _setOrgFurtherInformation(Org org, List row, String fieldKey, FORMAT format, Subscription subscription = null, String contactSwitch = 'publicContact'){

        boolean isPublic = contactSwitch == 'publicContact'
        String tenantFilter = '', addressTenantFilter = '', contactTypeFilter = '', postBoxFilter = ''
        if (fieldKey.contains('Contact.')) {
            if (org) {
                Map<String, Object> queryParams = [org: org, type: RDStore.CCT_EMAIL, isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                RefdataValue contactType = RefdataValue.getByCategoriesDescAndValue([RDConstants.PERSON_POSITION, RDConstants.PERSON_FUNCTION, RDConstants.PERSON_RESPONSIBILITY], fieldKey.split('\\.')[3])
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
        if (fieldKey.contains('Address.')) {
            if (org) {
                Map<String, Object> queryParams = [org: org, type: RefdataValue.getByValue(fieldKey.split('\\.')[1])]
                if(contactSwitch == 'privateAddress') {
                    addressTenantFilter = ' and a.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                else addressTenantFilter = ' and a.tenant = null'
                if(fieldKey.contains('.address')) {
                    postBoxFilter = ' and a.pob = null and a.pobZipcode = null and a.pobCity = null'
                }
                else if(fieldKey.contains('.pob')) {
                    postBoxFilter = ' and (a.pob is not null or a.pobZipcode is not null or a.pobCity is not null)'
                }
                Set<Address> addressList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org"+addressTenantFilter+postBoxFilter, queryParams)

                if (addressList) {
                    row.add(createTableCell(format, addressList.collect { Address address -> _getAddress(address, org)}.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }

        }
        /*else if (fieldKey.endsWith('.billingContact')) {
            if (org) {
                Map<String, Object> queryParams = [org: org, functionTypes: [RDStore.PRS_FUNC_INVOICING_CONTACT], type: RDStore.CCT_EMAIL, isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and (p.tenant = :ctx or p.isPublic = true)'
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

        }  */
        else if (fieldKey.endsWith('.billingAdress')) {
            if (org) {
                RefdataValue billingAddress = RDStore.ADDRESS_TYPE_BILLING
                Map<String, Object> queryParams = [org: org, type: billingAddress]

                //LinkedHashSet<Address> addressList = org.addresses.findAll { Address adress -> adress.type.findAll { it == billingAdress } }
                Set<Address> addressList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org", queryParams)

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
                Map<String, Object> queryParams = [org: org, type: postAddress]
                //LinkedHashSet<Address> addressList = org.addresses.findAll { Address adress -> adress.type.findAll { it == postAdress } }
                Set<Address> addressList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.org = :org", queryParams)

                if (addressList) {
                    row.add([field: addressList.collect { Address address -> _getAddress(address, org)}.join(";"), style: null])
                } else {
                    row.add([field: '', style: null])
                }
            } else {
                row.add([field: '', style: null])
            }

        }
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
        } else if (fieldKey.contains('discoverySystemsFrontend')) {
            if (org) {
                if(org.discoverySystemFrontends) {
                    row.add(createTableCell(format, org.discoverySystemFrontends.collect { DiscoverySystemFrontend dsf -> dsf.frontend.getI10n('value') }.join('\n')))
                }
                else row.add(createTableCell(format, ' '))
            }
            else {
                row.add(createTableCell(format, ' '))
            }
        } else if (fieldKey.contains('discoverySystemsIndex')) {
            if (org) {
                if(org.discoverySystemIndices) {
                    row.add(createTableCell(format, org.discoverySystemIndices.collect { DiscoverySystemIndex dsi -> dsi.index.getI10n('value') }.join('\n')))
                }
                else row.add(createTableCell(format, ' '))
            }
            else {
                row.add(createTableCell(format, ' '))
            }
        } else if (fieldKey.startsWith('participantIdentifiers.')) {
            if (org) {
                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.org = :org and ident.ns.id in (:namespaces) and ident.value != :unknown and ident.value != ''", [org: org, namespaces: [id], unknown: IdentifierNamespace.UNKNOWN])
                if (identifierList) {
                    row.add(createTableCell(format, identifierList.value.join('\n')))
                    List idNotes = []
                    identifierList.each { Identifier ident ->
                        if(ident.note)
                            idNotes << ident.note
                        else idNotes << ' '
                    }
                    row.add(createTableCell(format, idNotes.join('\n')))
                } else {
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
            }
        } else if (fieldKey.startsWith('participantCustomerIdentifiers.')) {
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
        } else if (fieldKey.startsWith('participantProperty.')) {
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
                ReaderNumber readerNumberPeople
                ReaderNumber readerNumberUser
                ReaderNumber readerNumberStudents
                ReaderNumber readerNumberStaff
                ReaderNumber readerNumberFTE
                ReaderNumber readerNumberTotalWithYear
                ReaderNumber readerNumberFTEWithYear
                List yearCheck = ReaderNumber.executeQuery('select rn.year from ReaderNumber rn where rn.org = :org and rn.year != null order by rn.year desc', [org: org])
                if(yearCheck) {
                    Year newestYear = (Year) yearCheck[0]
                    readerNumberPeople = ReaderNumber.findByReferenceGroupAndOrgAndYear(RDStore.READER_NUMBER_PEOPLE, org, newestYear)
                    readerNumberUser = ReaderNumber.findByReferenceGroupAndOrgAndYear(RDStore.READER_NUMBER_USER, org, newestYear)
                    readerNumberTotalWithYear = ReaderNumber.findByReferenceGroupAndOrgAndYear(RDStore.READER_NUMBER_FTE_TOTAL, org, newestYear)
                    readerNumberFTEWithYear = ReaderNumber.findByReferenceGroupAndOrgAndYear(RDStore.READER_NUMBER_FTE, org, newestYear)

                    if(readerNumberTotalWithYear || readerNumberFTEWithYear){
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, newestYear.value))
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
                        if(readerNumberTotalWithYear)
                            row.add(createTableCell(format, readerNumberTotalWithYear.value))
                        else row.add(createTableCell(format, ' '))
                        if(readerNumberFTEWithYear)
                            row.add(createTableCell(format, readerNumberFTEWithYear.value))
                        else row.add(createTableCell(format, ' '))
                    }
                    else if(readerNumberPeople || readerNumberUser){
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, newestYear.value))
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
                        row.add(createTableCell(format, ' '))
                    }
                }
                else {
                    RefdataValue currentSemester = RefdataValue.getCurrentSemester()

                    readerNumberStudents = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_STUDENTS, org, currentSemester)
                    readerNumberStaff = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_SCIENTIFIC_STAFF, org, currentSemester)
                    readerNumberFTE = ReaderNumber.findByReferenceGroupAndOrgAndSemester(RDStore.READER_NUMBER_FTE, org, currentSemester)

                    if(readerNumberStudents || readerNumberStaff || readerNumberFTE){
                        row.add(createTableCell(format, currentSemester.getI10n('value')))
                        row.add(createTableCell(format, ' '))
                        BigDecimal studentsStr = readerNumberStudents ? readerNumberStudents.value : null,
                        staffStr = readerNumberStaff ? readerNumberStaff.value : null,
                        fteStr = readerNumberFTE ? readerNumberFTE.value : null
                        row.add(createTableCell(format, studentsStr))
                        row.add(createTableCell(format, staffStr))
                        row.add(createTableCell(format, null))
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
                                    row.add(createTableCell(format, ' '))
                                    row.add(createTableCell(format, studentsStr))
                                    row.add(createTableCell(format, staffStr))
                                    row.add(createTableCell(format, null))
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
                            row.add(createTableCell(format, null))
                            row.add(createTableCell(format, null))
                        }
                    }
                }


                BigDecimal peopleStr = readerNumberUser ? readerNumberUser.value : null, userStr = readerNumberPeople ? readerNumberPeople.value : null

                row.add(createTableCell(format, peopleStr))
                row.add(createTableCell(format, userStr))

                BigDecimal sum = 0, sumStudFTE = 0, sumStudHeads = 0
                if(readerNumberStudents){
                    sum = sum + (readerNumberStudents.value != null ? readerNumberStudents.value : 0)
                    sumStudFTE += (readerNumberStudents.value != null ? readerNumberStudents.value : 0)
                    sumStudHeads += (readerNumberStudents.value != null ? readerNumberStudents.value : 0)
                }
                if(readerNumberStaff){
                    sum = sum + (readerNumberStaff.value != null ? readerNumberStaff.value : 0)
                    sumStudHeads += (readerNumberStaff.value != null ? readerNumberStaff.value : 0)
                }
                if(readerNumberFTE){
                    sum = sum + (readerNumberFTE.value != null ? readerNumberFTE.value : 0)
                    sumStudFTE += (readerNumberFTE.value != null ? readerNumberFTE.value : 0)
                }
                if(readerNumberPeople){
                    sum = sum + (readerNumberPeople.value != null ? readerNumberPeople.value : 0)
                }
                if(readerNumberUser){
                    sum = sum + (readerNumberUser.value != null ? readerNumberUser.value : 0)
                }

                if(readerNumberFTEWithYear){
                    sum = sum + (readerNumberFTEWithYear.value != null ? readerNumberFTEWithYear.value : 0)
                    sumStudFTE += (readerNumberFTEWithYear.value != null ? readerNumberFTEWithYear.value : 0)
                }

                row.add(createTableCell(format, sumStudFTE))
                row.add(createTableCell(format, sumStudHeads))
                if((readerNumberFTE?.value || readerNumberFTEWithYear?.value) && (readerNumberStaff?.value || readerNumberTotalWithYear?.value))
                    row.add(createTableCell(format, ' '))
                else
                    row.add(createTableCell(format, sum))

                String note = readerNumberStudents ? readerNumberStudents.dateGroupNote : (readerNumberPeople ? readerNumberPeople.dateGroupNote : (readerNumberUser ? readerNumberUser.dateGroupNote : (readerNumberTotalWithYear ? readerNumberTotalWithYear.dateGroupNote : (readerNumberFTEWithYear ? readerNumberFTEWithYear.dateGroupNote : ''))))

                row.add(createTableCell(format, note))

            } else {
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
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

    private void _setVendorFurtherInformation(Vendor vendor, List row, String fieldKey, FORMAT format, String contactSwitch = 'publicContact'){
        boolean isPublic = contactSwitch == 'publicContact'
        String tenantFilter = '', addressTenantFilter, contactTypeFilter = '', postBoxFilter = ''
        Org contextOrg = contextService.getOrg()
        if (fieldKey.contains('Contact.')) {
            if (vendor) {
                Map<String, Object> queryParams = [vendor: vendor, type: RDStore.CCT_EMAIL, isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                RefdataValue contactType = RefdataValue.getByCategoriesDescAndValue([RDConstants.PERSON_POSITION, RDConstants.PERSON_FUNCTION, RDConstants.PERSON_RESPONSIBILITY], fieldKey.split('\\.')[3])
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
                List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.vendor = :vendor and "+contactTypeFilter+" and c.contentType = :type and p.isPublic = :isPublic"+tenantFilter, queryParams)

                if (contactList) {
                    row.add(createTableCell(format, contactList.content.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }

        }
        if (fieldKey.contains('Address.')) {
            if (vendor) {
                Map<String, Object> queryParams = [org: org, type: RefdataValue.getByValue(fieldKey.split('\\.')[1])]
                if(contactSwitch == 'privateAddress') {
                    addressTenantFilter = ' and a.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                else addressTenantFilter = ' and a.tenant = null'
                if(fieldKey.contains('.address')) {
                    postBoxFilter = ' and a.pob = null and a.pobZipcode = null and a.pobCity = null'
                }
                else if(fieldKey.contains('.pob')) {
                    postBoxFilter = ' and (a.pob is not null or a.pobZipcode is not null or a.pobCity is not null)'
                }
                Set<Address> addressList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.vendor = :vendor"+addressTenantFilter+postBoxFilter, queryParams)

                if (addressList) {
                    row.add(createTableCell(format, addressList.collect { Address address -> _getAddress(address, vendor)}.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        }
        if (fieldKey.contains('altnames')) {
            if (vendor) {
                if(vendor.altnames) {
                    row.add(createTableCell(format, vendor.altnames.collect { AlternativeName alt -> alt.name }.join('\n')))
                }
                else row.add(createTableCell(format, ' '))
            }
            else {
                row.add(createTableCell(format, ' '))
            }
        }
        else if (fieldKey.startsWith('vendorIdentifiers.')) {
            if (vendor) {
                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.provider = :provider and ident.ns.id in (:namespaces) and ident.value != :unknown and ident.value != ''", [vendor: vendor, namespaces: [id], unknown: IdentifierNamespace.UNKNOWN])
                if (identifierList) {
                    row.add(createTableCell(format, identifierList.value.join('\n')))
                    List idNotes = []
                    identifierList.each { Identifier ident ->
                        if(ident.note)
                            idNotes << ident.note
                        else idNotes << ' '
                    }
                    row.add(createTableCell(format, idNotes.join('\n')))
                } else {
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
            }
        }
        else if (fieldKey.startsWith('vendorCustomerIdentifiers.')) {
            if (vendor) {
                Set<CustomerIdentifier> customerIdentifier = CustomerIdentifier.executeQuery('select ci from CustomerIdentifier ci join ci.platform plat where plat in (select pkg.nominalPlatform from PackageVendor pv join pv.pkg pkg where pkg.vendor = :vendor) and plat = :platform and ci.customer = :context', [context: contextOrg, vendor: vendor, platform: Platform.get(fieldKey.split("\\.")[1])])
                if (customerIdentifier) {
                    row.add(createTableCell(format, customerIdentifier[0].value))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        }
        else if (fieldKey.startsWith('vendorProperty.')) {
            if (vendor) {

                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<VendorProperty> vendorProperties = VendorProperty.executeQuery("select prop from VendorProperty prop where (prop.owner = :vendor and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :vendor and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg)", [vendor: vendor, propertyDefs: [id], contextOrg: contextOrg])
                if (vendorProperties) {
                    List<String> propValues = [], propAnnotations = []
                    vendorProperties.each { VendorProperty prop ->
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
        }
    }

    private void _setProviderFurtherInformation(Provider provider, List row, String fieldKey, FORMAT format, String contactSwitch = 'publicContact'){
        boolean isPublic = contactSwitch == 'publicContact'
        String tenantFilter = '', addressTenantFilter, contactTypeFilter = '', postBoxFilter = ''
        Org contextOrg = contextService.getOrg()
        if (fieldKey.contains('Contact.')) {
            if (provider) {
                Map<String, Object> queryParams = [provider: provider, type: RDStore.CCT_EMAIL, isPublic: isPublic]
                if(!isPublic) {
                    tenantFilter = ' and p.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                RefdataValue contactType = RefdataValue.getByCategoriesDescAndValue([RDConstants.PERSON_POSITION, RDConstants.PERSON_FUNCTION, RDConstants.PERSON_RESPONSIBILITY], fieldKey.split('\\.')[3])
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
                List<Contact> contactList = Contact.executeQuery("select c from PersonRole pr join pr.prs p join p.contacts c where pr.provider = :provider and "+contactTypeFilter+" and c.contentType = :type and p.isPublic = :isPublic"+tenantFilter, queryParams)

                if (contactList) {
                    row.add(createTableCell(format, contactList.content.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }

        }
        if (fieldKey.contains('Address.')) {
            if (provider) {
                Map<String, Object> queryParams = [provider: provider, type: RefdataValue.getByValue(fieldKey.split('\\.')[1])]
                if(contactSwitch == 'privateAddress') {
                    addressTenantFilter = ' and a.tenant = :ctx'
                    queryParams.ctx = contextService.getOrg()
                }
                else addressTenantFilter = ' and a.tenant = null'
                if(fieldKey.contains('.address')) {
                    postBoxFilter = ' and a.pob = null and a.pobZipcode = null and a.pobCity = null'
                }
                else if(fieldKey.contains('.pob')) {
                    postBoxFilter = ' and (a.pob is not null or a.pobZipcode is not null or a.pobCity is not null)'
                }
                Set<Address> addressList = Address.executeQuery("select a from Address a join a.type type where type = :type and a.provider = :provider"+addressTenantFilter+postBoxFilter, queryParams)

                if (addressList) {
                    row.add(createTableCell(format, addressList.collect { Address address -> _getAddress(address, provider)}.join(";")))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        }
        if (fieldKey.contains('altnames')) {
            if (provider) {
                if(provider.altnames) {
                    row.add(createTableCell(format, provider.altnames.collect { AlternativeName alt -> alt.name }.join('\n')))
                }
                else row.add(createTableCell(format, ' '))
            }
            else {
                row.add(createTableCell(format, ' '))
            }
        }
        else if (fieldKey.startsWith('providerIdentifiers.')) {
            if (provider) {
                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<Identifier> identifierList = Identifier.executeQuery("select ident from Identifier ident where ident.provider = :provider and ident.ns.id in (:namespaces) and ident.value != :unknown and ident.value != ''", [provider: provider, namespaces: [id], unknown: IdentifierNamespace.UNKNOWN])
                if (identifierList) {
                    row.add(createTableCell(format, identifierList.value.join('\n')))
                    List idNotes = []
                    identifierList.each { Identifier ident ->
                        if(ident.note)
                            idNotes << ident.note
                        else idNotes << ' '
                    }
                    row.add(createTableCell(format, idNotes.join('\n')))
                } else {
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
                row.add(createTableCell(format, ' '))
            }
        }
        else if (fieldKey.startsWith('providerCustomerIdentifiers.')) {
            if (provider) {
                Set<CustomerIdentifier> customerIdentifier = CustomerIdentifier.executeQuery('select ci from CustomerIdentifier ci join ci.platform plat where plat.provider = :provider and plat = :platform and ci.customer = :context', [context: contextOrg, provider: provider, platform: Platform.get(fieldKey.split("\\.")[1])])
                if (customerIdentifier) {
                    row.add(createTableCell(format, customerIdentifier[0].value))
                } else {
                    row.add(createTableCell(format, ' '))
                }
            } else {
                row.add(createTableCell(format, ' '))
            }
        }
        else if (fieldKey.startsWith('providerProperty.')) {
            if (provider) {

                Long id = Long.parseLong(fieldKey.split("\\.")[1])
                List<ProviderProperty> providerProperties = ProviderProperty.executeQuery("select prop from ProviderProperty prop where (prop.owner = :provider and prop.type.id in (:propertyDefs) and prop.isPublic = true) or (prop.owner = :provider and prop.type.id in (:propertyDefs) and prop.isPublic = false and prop.tenant = :contextOrg)", [provider: provider, propertyDefs: [id], contextOrg: contextOrg])
                if (providerProperties) {
                    List<String> propValues = [], propAnnotations = []
                    providerProperties.each { ProviderProperty prop ->
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
        }
    }

    /**
     * Builds the header row for the given export configuration
     * @param selectedExportFields the fields to be exported
     * @param locale the {@link Locale} to be used for the export
     * @param selectedCostItemFields the cost item fields to appear in the export; every field will appear for each cost item element selected
     * @param maxCostItemsElements the count of cost item elements figuring in the export
     * @param contactSources the set of contacts to be exported (public or private)
     * @param selectedCostElements the cost item elements to be included in the export; for each cost item element, an own field column is being generated
     * @return a {@link List} of column headers
     */
    private List _exportTitles(Map<String, Object> selectedExportFields, Locale locale, Map selectedCostItemFields = null, Integer maxCostItemsElements = null, Set<String> contactSources = [], Map selectedCostElements = [:], FORMAT format){
        List titles = []

        String localizedValue = LocaleUtils.getLocalizedAttributeName('value')
        Map<String, String> colHeaderMap = [own: messageSource.getMessage('financials.tab.ownCosts', null, locale),
                                            cons: messageSource.getMessage('financials.tab.consCosts', null, locale),
                                            subscr: messageSource.getMessage('financials.tab.subscrCosts', null, locale)]

        selectedExportFields.each { String fieldKey, Map fields ->
            if(!fields.separateSheet) {
                if (fieldKey.contains('Contact.')) {
                    RefdataValue contactType = RefdataValue.findByValue(fieldKey.split('\\.')[3])
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Contact') }.each { String contactSwitch ->
                            if(contactSwitch.contains('Contact'))
                                titles.add(createTableCell(format,  "${contactType.getI10n('value')} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"))
                        }
                    }
                    else
                        titles.add(createTableCell(format,  contactType.getI10n('value')))
                }
                else if (fieldKey.contains('Address.')) {
                    RefdataValue addressType = RefdataValue.findByValue(fieldKey.split('\\.')[1])
                    if(contactSources) {
                        contactSources.findAll{ String source -> source.contains('Address') }.each { String contactSwitch ->
                            if(contactSwitch.contains('Address')) {
                                if(fieldKey.contains('.address'))
                                    titles.add(createTableCell(format, "${addressType.getI10n('value')} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)} ${messageSource.getMessage('default.address.export.addition', null, locale)}"))
                                else if(fieldKey.contains('.pob'))
                                    titles.add(createTableCell(format, "${addressType.getI10n('value')} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)} ${messageSource.getMessage('default.pob.export.addition', null, locale)}"))
                            }
                        }
                    }
                    else {
                        if(fieldKey.contains('.address'))
                            titles.add(createTableCell(format, "${addressType.getI10n('value')} ${messageSource.getMessage('default.address.export.addition', null, locale)}"))
                        else if(fieldKey.contains('.pob'))
                            titles.add(createTableCell(format, "${addressType.getI10n('value')} ${messageSource.getMessage('default.pob.export.addition', null, locale)}"))
                    }
                }
                /*else if (fieldKey.endsWith('.billingContact')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles.add(createTableCell(format,  "${RDStore.PRS_FUNC_INVOICING_CONTACT."${localizedValue}"} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"))
                        }
                    }
                    else
                        titles.add(createTableCell(format,  RDStore.PRS_FUNC_INVOICING_CONTACT."${localizedValue}"))
                }
                else if (fieldKey.endsWith('.billingAdress')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles.add(createTableCell(format,  "${RDStore.ADDRESS_TYPE_BILLING."${localizedValue}"} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"))
                        }
                    }
                    else
                        titles.add(createTableCell(format,  RDStore.ADDRESS_TYPE_BILLING."${localizedValue}"))
                }
                else if (fieldKey.endsWith('.postAdress')) {
                    if(contactSources) {
                        contactSources.each { String contactSwitch ->
                            titles.add(createTableCell(format,  "${RDStore.ADDRESS_TYPE_POSTAL."${localizedValue}"} ${messageSource.getMessage("org.export.column.${contactSwitch}", null, locale)}"))
                        }
                    }
                    else
                        titles.add(createTableCell(format,  RDStore.ADDRESS_TYPE_POSTAL."${localizedValue}"))
                }*/
                else if (fieldKey == 'participant.readerNumbers') {
                    titles.add(createTableCell(format,  messageSource.getMessage('readerNumber.semester.label', null, locale)))
                    titles.add(createTableCell(format,  messageSource.getMessage('readerNumber.year.label', null, locale)))
                    titles.add(createTableCell(format,  RDStore.READER_NUMBER_STUDENTS."${localizedValue}"))
                    titles.add(createTableCell(format,  RDStore.READER_NUMBER_SCIENTIFIC_STAFF."${localizedValue}"))
                    titles.add(createTableCell(format,  RDStore.READER_NUMBER_FTE_TOTAL."${localizedValue}"))
                    titles.add(createTableCell(format,  RDStore.READER_NUMBER_FTE."${localizedValue}"))
                    titles.add(createTableCell(format,  RDStore.READER_NUMBER_USER."${localizedValue}"))
                    titles.add(createTableCell(format,  RDStore.READER_NUMBER_PEOPLE."${localizedValue}"))
                    titles.add(createTableCell(format,  messageSource.getMessage('readerNumber.sumStudFTE.label', null, locale)))
                    titles.add(createTableCell(format,  messageSource.getMessage('readerNumber.sumStudHeads.label', null, locale)))
                    titles.add(createTableCell(format,  messageSource.getMessage('readerNumber.sum.label', null, locale)))
                    titles.add(createTableCell(format,  messageSource.getMessage('readerNumber.note.label', null, locale)))
                }
                else if ((fieldKey == 'participantSubCostItem' || fieldKey == 'subCostItem' || fieldKey == 'renewalSurveyCostItems') && maxCostItemsElements > 0 && selectedCostItemFields.size() > 0) {
                    for(int i = 0; i < maxCostItemsElements; i++) {
                        titles.add(createTableCell(format,  messageSource.getMessage("financials.costItemElement", null, locale)))
                        selectedCostItemFields.each {
                            titles.add(createTableCell(format,  (it.value.message ? messageSource.getMessage("${it.value.message}", null, locale) : it.value.label)))
                        }
                    }
                }
                else if (fieldKey == 'participantSurveyCostItems' && selectedCostElements.selectedCostItemElementsForSurveyCostItems.size() > 0 && selectedCostItemFields.forSurveyCostItems.size() > 0) {
                    for(int i = 0; i < selectedCostElements.selectedCostItemElementsForSurveyCostItems.size(); i++) {
                        titles.add(createTableCell(format,  messageSource.getMessage("financials.costItemElement", null, locale)))
                        selectedCostItemFields.forSurveyCostItems.each {
                            titles.add(createTableCell(format,  (it.value.message ? messageSource.getMessage("${it.value.message}", null, locale) : it.value.label)))
                        }
                    }
                }
                else if (fieldKey == 'participantSurveySubCostItems' && selectedCostElements.selectedCostItemElementsForSubCostItems.size() > 0 && selectedCostItemFields.forSubCostItems.size() > 0) {
                    String style = 'neutral'
                    for(int i = 0; i < selectedCostElements.selectedCostItemElementsForSubCostItems.size(); i++) {
                        String oldPrideInfo = messageSource.getMessage("surveyConfigsInfo.oldPrice", null, locale)
                        titles.add(createTableCell(format,  oldPrideInfo + '-' + messageSource.getMessage("financials.costItemElement", null, locale), style))
                        selectedCostItemFields.forSubCostItems.each {
                            oldPrideInfo = oldPrideInfo + '-' + (it.value.message ? messageSource.getMessage("${it.value.message}", null, locale) : it.value.label)
                            titles.add(createTableCell(format,  oldPrideInfo, style))
                        }

                        if(style == 'neutral'){
                            style = 'neutral2'
                        }else {
                            style = 'neutral'
                        }
                    }
                }
                else if(fieldKey == 'pickAndChoose') {
                    titles.add(createTableCell(format,  "${messageSource.getMessage('surveyEvaluation.titles.currentAndFixedEntitlements', null, locale)}"))
                    titles.add(createTableCell(format,  "${messageSource.getMessage('tipp.price.plural', null, locale)}"))
                    titles.add(createTableCell(format,  "Budget"))
                    titles.add(createTableCell(format,  "Diff."))
                }
                else if(fieldKey == 'subCostItem') {
                    selectedCostElements.each { String titleSuffix, List<RefdataValue> costItemElements ->
                        costItemElements.each { RefdataValue cie ->
                            titles.add(createTableCell(format,  "${cie.getI10n('value')} (${colHeaderMap.get(titleSuffix)})"))
                        }
                    }
                }
                else if(fieldKey.contains('participantIdentifiers.') || fieldKey.contains('providerIdentifiers.')) {
                    titles.add(createTableCell(format,  fields.label))
                    titles.add(createTableCell(format,  "${fields.label} ${messageSource.getMessage('default.notes.plural', null, locale)}"))
                }
                else if(fieldKey.contains('vendorSurvey')) {
                    titles.add(createTableCell(format,  fields.label))
                    /*titles.add(createTableCell(format,  "${fields.label}: ${messageSource.getMessage('surveyResult.participantComment', null, locale)}"))
                    titles.add(createTableCell(format,  "${fields.label}: ${messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale)}"))*/
                }
                else {
                    String label = (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label)
                    if(fields.privateProperty == true)
                        label += ' (Meine Merkmale)'
                    else if(fields.privateProperty == false)
                        label += ' (Allgemeine Merkmale)'
                    if(label && (fieldKey != 'surveyPropertyParticipantComment' && fieldKey != 'surveyPropertyCommentOnlyForOwner'))
                        titles.add(createTableCell(format,  label))
                    if (fieldKey.startsWith('surveyProperty.')) {
                        if('surveyPropertyParticipantComment' in selectedExportFields) {
                            titles.add(createTableCell(format, (messageSource.getMessage('surveyResult.participantComment', null, locale) + " " + messageSource.getMessage('renewalEvaluation.exportRenewal.to', null, locale) + " " + (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label))))
                        }
                        if('surveyPropertyCommentOnlyForOwner' in selectedExportFields) {
                             titles.add(createTableCell(format, (messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale) + " " + messageSource.getMessage('renewalEvaluation.exportRenewal.to', null, locale) + " " + (fields.message ? messageSource.getMessage("${fields.message}", null, locale) : fields.label))))
                        }
                    }else if (fieldKey.contains('Property') && (fieldKey != 'surveyPropertyParticipantComment' && fieldKey != 'surveyPropertyCommentOnlyForOwner')) {
                        titles.add(createTableCell(format,  "${label} ${messageSource.getMessage('default.notes.plural', null, locale)}"))
                    }
                    if(fieldKey.contains('licProperty')) {
                        titles.add(createTableCell(format,  "${label} ${messageSource.getMessage('property.table.paragraph', null, locale)}"))
                    }
                }
            }
        }

        titles
    }

    /**
     * Formats the given organisations address
     * @param address the address to format
     * @param org the organisation/provider/vendor to which the address is belonging
     * @return the formatted address string
     */
    private String _getAddress(Address address, org){
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

    /**
     * Convenience method, the current implementation should be refactored by library.
     * Exports the notes for the given object, owned by the given contextOrg
     * @param objInstance the object of which the notes are subject
     * @return a {@link Map} containing the object's notes, owned by the contextOrg, of structure: [baseItems: notes directly attached to the object, sharedItems: items coming from a possible parent (if a parent exist at all)]
     */
    private Map<String, Object> _getNotesForObject(objInstance) {
        List<DocContext> baseItems = [], sharedItems = []
        Org contextOrg = contextService.getOrg()
        Set items = []
        if(!(objInstance instanceof Collection)) {
            items << objInstance
        }
        else items.addAll(objInstance)
        items.each { obj ->
            docstoreService.getNotes(obj, contextOrg).each { DocContext dc ->
                if(dc.status != RDStore.DOC_CTX_STATUS_DELETED) {
                    String noteContent = dc.owner.title
                    if(dc.owner.content != null) {
                        XmlSlurper parser = new XmlSlurper()
                        String inputText = dc.owner.content.replaceAll('&nbsp;', ' ')
                                .replaceAll('&', '&amp;')
                                .replaceAll('<(?!p|/|ul|li|strong|b|i|u|em|ol|a|h[1-6])', '&lt;')
                                .replaceAll('(?<!p|/|ul|li|strong|b|i|u|em|ol|a|h[1-6])>', '&gt;')
                        try {
                            GPathResult input = parser.parseText('<mkp>'+inputText+'</mkp>')
                            List<String> listEntries = []
                            input.'**'.findAll{ node -> node.childNodes().size() == 0 }.each { node ->
                                //log.debug("${node.childNodes().size()}")
                                //if(node.childNodes().size() == 0)
                                listEntries << node.text().trim()
                                /*
                                else {
                                    listEntries.addAll(_getRecursiveNodeText(node.childNodes()))
                                }
                                */
                            }
                            noteContent += '\n' + listEntries.join('\n')
                        }
                        catch (SAXException e) {
                            log.debug(inputText)
                        }
                    }
                    if(dc.sharedFrom) {
                        sharedItems << noteContent
                    }
                    else {
                        if(dc.owner.owner == contextOrg || dc.owner.owner == null) {
                            baseItems << noteContent
                        }
                    }
                }
            }
        }
        [baseItems: baseItems, sharedItems: sharedItems]
    }

    /**
     * Helper method to retrieve text in nested HTML
     * @param childNodes the child nodes to process
     * @return the text nodes as a {@link List}
     */
    private List<String> _getRecursiveNodeText(childNodes) {
        List<String> result = []
        childNodes.each { node ->
            if(node.childNodes().size() == 0)
                result << node.text()
            else {
                result.addAll(_getRecursiveNodeText(node.childNodes()))
            }
        }
        result
    }

    private void saveClickMeConfig(Map selectedExportFields, String nameOfClickMeMap){
        Org contextOrg = contextService.getOrg()
        def request = WebUtils.retrieveGrailsWebRequest().getCurrentRequest()
        GrailsParameterMap grailsParameterMap = new GrailsParameterMap(request)

        if(grailsParameterMap.saveClickMeConfig && BeanStore.getContextService().isInstEditor(CustomerTypeService.PERMS_PRO)){
            String jsonConfig = (new JSON(selectedExportFields)).toString()
            String clickMeConfigName = grailsParameterMap.clickMeConfigName
            String clickMeType = grailsParameterMap.clickMeType
            int countClickMeConfigs = ClickMeConfig.executeQuery('select count(*) from ClickMeConfig where contextOrg = :contextOrg and clickMeType = :clickMeType', [contextOrg: contextOrg, clickMeType: clickMeType])[0]

            ClickMeConfig clickMeConfig = new ClickMeConfig(name: clickMeConfigName,
                    contextOrg: contextOrg,
                    jsonConfig: jsonConfig,
                    contextUrl: "/${grailsParameterMap.exportController}/${grailsParameterMap.exportAction}",
                    nameOfClickMeMap: nameOfClickMeMap,
                    clickMeType: clickMeType,
                    configOrder: countClickMeConfigs+1,
                    note: grailsParameterMap.clickMeConfigNote
            ).save()

        }

    }

    Map getClickMeFields(ClickMeConfig clickMeConfig, Map fields){
        if (clickMeConfig){
            Map clickMeConfigMap = clickMeConfig.getClickMeConfigMap()
            Set clickMeConfigMapKeys = clickMeConfigMap.keySet()

            fields.each { def field ->
                field.value.fields.each{
                    if(it.value ){
                        if(it.value.containsKey('defaultChecked')) {
                            it.value.defaultChecked = it.key.toString() in clickMeConfigMapKeys ? true : false
                        }else {
                            if(it.key.toString() in clickMeConfigMapKeys){
                                it.value.defaultChecked = true
                            }
                        }
                    }
                }
            }
        }

        return fields
    }

    def _exportSurveyPackages(SurveyConfig surveyConfig, List<Org> orgs, ExportClickMeService.FORMAT format) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name'
        ])

        surveyConfig.surveyPackages.sort {it.pkg.name}.each {SurveyConfigPackage surveyConfigPackage ->
            titles << surveyConfigPackage.pkg.name
            titles << messageSource.getMessage('surveyResult.participantComment', null, locale)
            titles << messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale)

        }

        List surveyPackageData = []
        orgs.each { Org org ->
            List row = []
            String sortname = org.sortname ?: ' '
            row.add(createTableCell(format, sortname))
            row.add(createTableCell(format, org.name))

            surveyConfig.surveyPackages.sort {it.pkg.name}.each { SurveyConfigPackage surveyConfigPackage ->
                SurveyPackageResult surveyPackageResult = SurveyPackageResult.findBySurveyConfigAndPkgAndParticipant(surveyConfig, surveyConfigPackage.pkg, org)
                if(surveyPackageResult){
                    row.add(createTableCell(format, messageSource.getMessage('default.selected.label', null, locale), 'positive'))
                    row.add(createTableCell(format, surveyPackageResult.comment))
                    row.add(createTableCell(format, surveyPackageResult.ownerComment))
                }else{
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                }
            }

            surveyPackageData.add(row)
        }

        return [titleRow: titles, columnData: surveyPackageData]

    }

    def _exportSurveyCostItemPackages(SurveyConfig surveyConfig, List<Org> orgs, ExportClickMeService.FORMAT format) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name'
        ])

        surveyConfig.surveyPackages.sort {it.pkg.name}.each {SurveyConfigPackage surveyConfigPackage ->
            titles << surveyConfigPackage.pkg.name
            titles << messageSource.getMessage('surveyResult.participantComment', null, locale)
            titles << messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale)

        }

        List surveyPackageData = []
        orgs.each { Org org ->
            List row = []
            String sortname = org.sortname ?: ' '
            row.add(createTableCell(format, sortname))
            row.add(createTableCell(format, org.name))

            surveyConfig.surveyPackages.sort {it.pkg.name}.each { SurveyConfigPackage surveyConfigPackage ->
                SurveyPackageResult surveyPackageResult = SurveyPackageResult.findBySurveyConfigAndPkgAndParticipant(surveyConfig, surveyConfigPackage.pkg, org)
                List<CostItem> costItemList = CostItem.findAllBySurveyOrgAndPkg(SurveyOrg.findBySurveyConfigAndOrg(surveyConfig, org), surveyConfigPackage.pkg)
                if(surveyPackageResult && costItemList){
                    row.add(createTableCell(format, messageSource.getMessage('default.selected.label', null, locale), 'positive'))
                    row.add(createTableCell(format, surveyPackageResult.comment))
                    row.add(createTableCell(format, surveyPackageResult.ownerComment))
                }else{
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))
                }
            }

            surveyPackageData.add(row)
        }

        return [titleRow: titles, columnData: surveyPackageData]

    }


    def _exportSurveyVendors(SurveyConfig surveyConfig, List<Org> orgs, ExportClickMeService.FORMAT format) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name'
        ])

        surveyConfig.surveyVendors.sort {it.vendor.name}.each { SurveyConfigVendor surveyConfigVendor ->
            titles << surveyConfigVendor.vendor.name
            titles << messageSource.getMessage('surveyResult.participantComment', null, locale)
            titles << messageSource.getMessage('surveyResult.commentOnlyForOwner', null, locale)

        }

        List surveyVendorData = []
        orgs.each { Org org ->
            List row = []
            String sortname = org.sortname ?: ' '
            row.add(createTableCell(format, sortname))
            row.add(createTableCell(format, org.name))

            surveyConfig.surveyVendors.sort {it.vendor.name}.each { SurveyConfigVendor surveyConfigVendor ->
                SurveyVendorResult surveyVendorResult = SurveyVendorResult.findBySurveyConfigAndVendorAndParticipant(surveyConfig, surveyConfigVendor.vendor, org)
                if(surveyVendorResult){
                    row.add(createTableCell(format, messageSource.getMessage('default.selected.label', null, locale), 'positive'))
                   /* row.add(createTableCell(format, surveyVendorResult.comment))
                    row.add(createTableCell(format, surveyVendorResult.ownerComment))*/
                }else{
                    row.add(createTableCell(format, ' '))
                   /* row.add(createTableCell(format, ' '))
                    row.add(createTableCell(format, ' '))*/
                }
            }

            surveyVendorData.add(row)
        }

        return [titleRow: titles, columnData: surveyVendorData]

    }
}
