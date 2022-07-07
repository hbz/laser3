package de.laser.api.v0

import de.laser.ExportService
import de.laser.AlternativeName
import de.laser.DeweyDecimalClassification
import de.laser.DocContext
import de.laser.GlobalService
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.IssueEntitlement
import de.laser.Language
import de.laser.Org
import de.laser.OrgRole
import de.laser.PersonRole
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.TitleInstancePackagePlatform
import de.laser.base.AbstractCoverage
import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.Address
import de.laser.Contact
import de.laser.oap.OrgAccessPoint
import de.laser.api.v0.entities.ApiDoc
import de.laser.api.v0.entities.ApiIssueEntitlement
import de.laser.storage.RDStore
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

/**
 * This class delivers given lists as maps of stubs or full objects
 */
@Slf4j
class ApiCollectionReader {

    /**
     * Processes a collection of addresses and builds a collection of entries for API output
     * @param list the {@link Collection} of {@link Address}es to output
     * @param allowedTypes the conditions which permit output of a value
     * @return a {@link Collection} of address details
     */
    static Collection<Object> getAddressCollection(Collection<Address> list, allowedTypes) {
        Collection<Object> result = []

        list.each { it ->   // de.laser.Address
            Map<String, Object> tmp = [:]

            tmp.street1         = it.street_1
            tmp.street2         = it.street_2
            tmp.pob             = it.pob
            tmp.pobZipcode      = it.pobZipcode
            tmp.pobCity         = it.pobCity
            tmp.zipcode         = it.zipcode
            tmp.city            = it.city
            tmp.name            = it.name
            tmp.additionFirst   = it.additionFirst
            tmp.additionSecond  = it.additionSecond
            //tmp.lastUpdated     = ApiToolkit.formatInternalDate(it.lastUpdated) updated nowhere?

            // RefdataValues
            tmp.region       = it.region?.value
            tmp.country     = it.country?.value
            tmp.type        = it.type?.collect{ it.value }

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(ApiReader.NO_CONSTRAINT == allowedTypes || (allowedTypes.findAll{it in it.type?.value} != [])) {
                result << tmp
            }
        }
        result
    }

    static Collection<Object> getAlternativeNameCollection(Collection<AlternativeName> list) {
        Collection<Object> result = []

        result.addAll(list.collect { AlternativeName altName -> altName.name })

        return result
    }

    /**
     * Processes a collection of contacts and builds a collection of entries for API output
     * @param list the {@link Collection} of {@link Contact}s to output
     * @param allowedTypes the conditions which permit output of a value
     * @return a {@link Collection} of contact details
     */
    static Collection<Object> getContactCollection(Collection<Contact> list, allowedTypes) {
        Collection<Object> result = []

        list.each { it ->       // de.laser.Contact
            Map<String, Object> tmp = [:]

            tmp.content         = it.content
            //tmp.lastUpdated     = ApiToolkit.formatInternalDate(it.lastUpdated) updated nowhere?

            // RefdataValues
            tmp.category        = it.contentType?.value
            tmp.language        = it.language?.value

            tmp = ApiToolkit.cleanUp(tmp, true, false)

            if(ApiReader.NO_CONSTRAINT == allowedTypes || allowedTypes.contains(it.type?.value)) {
                result << tmp
            }
        }
        result
    }

    /**
     * Builds a map of cost item API records from the filtered list of {@link CostItem}s
     * @param filteredList a pre-filtered and cleared list of public {@link CostItem}s
     * @return a {@link Collection} of {@link Map}s of cost item records for API output
     */
    static Collection<Object> getCostItemCollection(Collection<CostItem> filteredList, Org context) {
        Collection<Object> result = []

        filteredList.each { it ->

            Map<String, Object> tmp     = [:]

            tmp.globalUID               = it.globalUID
            tmp.isVisibleForSubscriber  = it.isVisibleForSubscriber ? "Yes" : "No"
            tmp.costInBillingCurrency   = it.costInBillingCurrency
            tmp.costInLocalCurrency     = it.costInLocalCurrency
            tmp.currencyRate            = it.currencyRate
            tmp.costTitle               = it.costTitle
            tmp.costDescription         = it.costDescription
            //tmp.includeInSubscription   = it.includeInSubscription
            tmp.reference               = it.reference

            tmp.costInLocalCurrencyAfterTax     = it.getCostInLocalCurrencyAfterTax()
            tmp.costInBillingCurrencyAfterTax   = it.getCostInBillingCurrencyAfterTax()
            tmp.billingSumRounding     = it.billingSumRounding ? "Yes" : "No"
            tmp.finalCostRounding      = it.finalCostRounding ? "Yes" : "No"

            tmp.calculatedType      = it._getCalculatedType()
            tmp.datePaid            = ApiToolkit.formatInternalDate(it.datePaid)
            tmp.invoiceDate         = ApiToolkit.formatInternalDate(it.invoiceDate)
            tmp.financialYear       = it.financialYear?.value
            tmp.startDate           = ApiToolkit.formatInternalDate(it.startDate)
            tmp.endDate             = ApiToolkit.formatInternalDate(it.endDate)
            tmp.dateCreated         = ApiToolkit.formatInternalDate(it.dateCreated)
            tmp.lastUpdated         = ApiToolkit.formatInternalDate(it.lastUpdated)
            tmp.taxRate             = it.taxKey?.taxRate

            // RefdataValues
            tmp.billingCurrency     = it.billingCurrency?.value
            tmp.costItemCategory    = it.costItemCategory?.value
            tmp.costItemElement     = it.costItemElement?.value
            tmp.costItemElementConfiguration = it.costItemElementConfiguration?.value
            tmp.costItemStatus      = it.costItemStatus?.value
            tmp.taxCode             = it.taxKey?.taxType?.value

            // References
            //def context = null // TODO: use context
            tmp.budgetCodes         = it.budgetcodes.collect{ BudgetCode bc -> bc.value }.unique()
            tmp.copyBase            = it.copyBase?.globalUID
            tmp.invoiceNumber       = it.invoice?.invoiceNumber // retrieveInvoiceMap(it.invoice) // de.laser.finance.Invoice
            // tmp.issueEntitlement    = ApiIssueEntitlement.retrieveIssueEntitlementMap(it.issueEntitlement, ApiReader.IGNORE_ALL, context) // de.laser.IssueEntitlement
            tmp.orderNumber         = it.order?.orderNumber // retrieveOrderMap(it.order) // de.laser.finance.Order
            // tmp.owner               = ApiStubReader.retrieveOrganisationStubMap(it.owner, context) // de.laser.Org
            // tmp.sub                 = ApiStubReader.requestSubscriptionStub(it.sub, context) // de.laser.Subscription // RECURSION ???
            // tmp.package             = ApiStubReader.requestSubscriptionPackageStubMixed(it.subPkg, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.SubscriptionPackage
            //tmp.surveyOrg
            //tmp.subPkg

            result << ApiToolkit.cleanUp(tmp, true, true)
        }

        result
    }

    /**
     * Builds a collection of maps reflecting the given {@link AbstractCoverage} collection for API output
     * @param list a {@link Collection} of {@link AbstractCoverage}s to be processed for API output
     * @return a {@link Collection} of {@link Map}s reflecting the issue entitlement coverage collection
     */
    static Collection<Object> getCoverageCollection(Collection<AbstractCoverage> list) {
        Collection<Object> result = []

        //hasMany-relation generated sets may be not initialised; null check thus necessary!
        list?.each { AbstractCoverage covStmt -> // de.laser.AbstractCoverage
            result << ApiUnsecuredMapReader.getAbstractCoverageMap(covStmt)
        }

        result
    }

    /**
     * Builds a collection of custom (= general) properties for the given object and respecting the settings of the requestor institution
     * @param list the {@link Collection} of properties to enumerate
     * @param generic the object (one of {@link de.laser.Subscription}, {@link de.laser.License}, {@link Org}, {@link de.laser.Person} or {@link de.laser.Platform})
     * @param context the requestor institution ({@link Org})
     * @return a {@link Collection} of {@link Map}s containing property details for API output
     */
    static Collection<Object> getCustomPropertyCollection(Collection<Object> list, def generic, Org context) {
        Collection<Object> result = []

        if (generic.metaClass.getMetaMethod("getCalculatedPropDefGroups")) {
            def groups = generic.getCalculatedPropDefGroups(context)
            List tmp = []

            // [PropertyDefinitionGroup, ..]
            groups.global?.each { it ->
                if (it.isVisible) {
                    tmp.addAll(it.getCurrentProperties(generic))
                }
            }
            // [[PropertyDefinitionGroup, PropertyDefinitionGroupBinding], ..]
            groups.local?.each { it ->
                if (it[0].isVisible) {
                    tmp.addAll(it[0].getCurrentProperties(generic))
                }
            }
            // [[PropertyDefinitionGroup, PropertyDefinitionGroupBinding], ..]
            groups.member?.each { it ->
                if (it[1].isVisibleForConsortiaMembers) {
                    tmp.addAll(it[0].getCurrentProperties(generic))
                }
            }

            // [<x>CustomProperty, ..]
            if (groups.orphanedProperties) {
                tmp.addAll(groups.orphanedProperties)
            }

            list = tmp.unique()
        }

        list.each { it ->       // de.laser.<x>CustomProperty
            Map<String, Object> tmp = [:]

            tmp.token       = it.type?.name     // de.laser.properties.PropertyDefinition.String
            tmp.scope       = it.type?.descr    // de.laser.properties.PropertyDefinition.String
            tmp.note        = it.note
            tmp.isPublic    = "Yes" // derived to substitute private properties tentant

            if (it.dateValue) {
                tmp.value   = ApiToolkit.formatInternalDate(it.dateValue)
            }
            else {
                tmp.value   = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
            }

            tmp.type = PropertyDefinition.validTypes[it.type.type]['en']

            if (it.type.isRefdataValueType()) {
                tmp.refdataCategory = it.type.refdataCategory
            }

            if (it instanceof LicenseProperty) {
                tmp.paragraph = it.paragraph
            }
            tmp = ApiToolkit.cleanUp(tmp, true, false)
            result << tmp
        }
        result
    }

    static Collection<Object> getDeweyDecimalCollection(SortedSet<DeweyDecimalClassification> ddcs) {
        Collection<Object> result = []

        result.addAll(ddcs.collect { DeweyDecimalClassification ddc -> [value: ddc.ddc.value, value_de: ddc.ddc.value_de, value_en: ddc.ddc.value_en] })

        result
    }

    /**
     * Builds a collection of document records for API output
     * @param list a {@link List} of document relations outgoing from the given object
     * @return a {@link Collection} of document map stubs
     * @see DocContext
     * @see ApiDoc#getDocumentMap(de.laser.Doc)
     */
    static Collection<Object> getDocumentCollection(Collection<DocContext> list) {
        Collection<Object> result = []
        list.each { it -> // de.laser.DocContext
            result << ApiDoc.getDocumentMap(it.owner)
        }
        result
    }

    /**
     * Builds a collection of identifier records for API output
     * @param list a {@link List} of {@link Identifier}s
     * @return a {@link Collection} of identifier namespace:value pairs
     */
    static Collection<Object> getIdentifierCollection(Collection<Identifier> list) {
        Collection<Object> result = []
        list.each { it ->   // de.laser.Identifier
            if(it.value != IdentifierNamespace.UNKNOWN) {
                Map<String, Object> tmp = [:]

                tmp.put( 'namespace', it.ns?.ns )
                tmp.put( 'value', it.value )

                tmp = ApiToolkit.cleanUp(tmp, true, true)
                result << tmp
            }
        }
        result
    }

    /**
     * Puts together a collection of issue entitlement stubs which belong to the given subscription package
     * @param subPkg the {@link de.laser.SubscriptionPackage} to process
     * @param ignoreRelation should relations followed up (and stubs returned) or not?
     * @param context the requesting institution ({@link Org}) whose perspective should be taken
     * @return a {@link Collection<Object>} reflecting the result
     */
    static Collection<Object> getIssueEntitlementCollection(SubscriptionPackage subPkg, ignoreRelation, Org context){
        Collection<Object> result = []

        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status not in (:statusIe)',
                [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_DELETED, statusIe: [RDStore.TIPP_STATUS_DELETED, RDStore.TIPP_STATUS_REMOVED]]
        )
        ieList.each{ ie ->
            result << ApiIssueEntitlement.getIssueEntitlementMap(ie, ignoreRelation, context) // de.laser.IssueEntitlement
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Puts together a collection of issue entitlement stubs which belong to the given subscription package, using native SQL to retrieve data
     * @param subPkg the {@link de.laser.SubscriptionPackage} to process
     * @param ignoreRelation should relations followed up (and stubs returned) or not?
     * @param context the requesting institution ({@link Org}) whose perspective should be taken
     * @return a {@link Collection<Object>} reflecting the result
     */
    static Collection<Object> getIssueEntitlementCollectionWithSQL(SubscriptionPackage subPkg, ignoreRelation, Org context){
        Collection<Object> result = []
        Sql sql = GlobalService.obtainSqlConnection()
        /*
        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status != :statusIe',
                [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_DELETED, statusIe: RDStore.TIPP_STATUS_DELETED]
        )
        */
        //missing are names etc., verify output - continue here!
        List ieRows = sql.rows("select ie_id, ie_name, ie_guid, ie_access_start_date, ie_access_end_date, ie_last_updated, (select rdv_value from refdata_value where rdv_id = ie_status_rv_fk) as ie_status, (select rdv_value from refdata_value where (rdv_id = ie_medium_rv_fk and ie_medium_rv_fk is not null) or (ie_medium_rv_fk is null and rdv_id = tipp_medium_rv_fk)) as ie_medium, ie_perpetual_access_by_sub_fk, " +
                "tipp_guid, tipp_name, tipp_host_platform_url, tipp_gokb_id, tipp_pkg_fk, tipp_plat_fk, tipp_date_first_in_print, tipp_date_first_online, tipp_first_author, tipp_first_editor, " +
                "tipp_publisher_name, tipp_imprint, tipp_volume, tipp_edition_number, tipp_last_updated, tipp_series_name, tipp_subject_reference, (select rdv_value from refdata_value where rdv_id = tipp_access_type_rv_fk) as tipp_access_type, (select rdv_value from refdata_value where rdv_id = tipp_open_access_rv_fk) as tipp_open_access, " +
                "tipp_last_updated, tipp_id, (select rdv_value from refdata_value where rdv_id = tipp_status_rv_fk) as tipp_status, " +
                "case tipp_title_type when 'Journal' then 'serial' when 'Book' then 'monograph' when 'Database' then 'database' else 'other' end as title_type " +
                "from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id " +
                "where ie_subscription_fk = :sub and tipp_pkg_fk = :pkg and tipp_status_rv_fk != :statusTipp and ie_status_rv_fk not in (:statusIe) order by ie_sortname",
                [sub: subPkg.subscription.id, pkg: subPkg.pkg.id, statusTipp: RDStore.TIPP_STATUS_DELETED.id, statusIe: [RDStore.TIPP_STATUS_DELETED.id, RDStore.TIPP_STATUS_REMOVED.id]])
        log.debug("now fetching additional params ...")
        Map<String, Object> subParams = [subId: subPkg.subscription.id], pkgParams = [pkgId: subPkg.pkg.id]
        List<GroovyRowResult> priceItemRows = sql.rows('select pi_ie_fk, (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk) as pi_list_currency, pi_list_price, (select rdv_value from refdata_value where rdv_id = pi_local_currency_rv_fk) as pi_local_currency, pi_local_price from price_item join issue_entitlement on pi_ie_fk = ie_id where ie_subscription_fk = :subId', subParams),
        coverageRows = sql.rows('select ic_ie_fk, ic_start_date, ic_start_issue, ic_start_volume, ic_end_date, ic_end_issue, ic_end_volume, ic_coverage_depth, ic_coverage_note, ic_embargo, ic_last_updated from issue_entitlement_coverage join issue_entitlement on ic_ie_fk = ie_id where ie_subscription_fk = :subId order by ic_start_date, ic_start_volume, ic_start_issue', subParams),
        idRows = sql.rows('select idns_ns, id_value, id_tipp_fk from identifier join identifier_namespace on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId', pkgParams),
        ddcRows = sql.rows('select rdv_value, rdv_value_de, rdv_value_en, ddc_tipp_fk from dewey_decimal_classification join refdata_value on ddc_rv_fk = rdv_id join title_instance_package_platform on ddc_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId', pkgParams),
        langRows = sql.rows('select rdv_value, rdv_value_de, rdv_value_en, lang_tipp_fk from language join refdata_value on lang_rv_fk = rdv_id join title_instance_package_platform on lang_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId', pkgParams),
        altNameRows = sql.rows('select altname_name, altname_tipp_fk from alternative_name join title_instance_package_platform on altname_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId', pkgParams),
        platformsOfSubscription = sql.rows('select plat_id, plat_gokb_id, plat_name, plat_guid, plat_primary_url, (select rdv_value from refdata_value where rdv_id = plat_status_rv_fk) as plat_status from platform join title_instance_package_platform on tipp_plat_fk = plat_id join issue_entitlement on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId', subParams),
        packageOfSubscription = sql.rows('select pkg_guid, pkg_gokb_id, pkg_name, (select rdv_value from refdata_value where rdv_id = pkg_status_rv_fk) as pkg_status from package where pkg_id = :pkgId', pkgParams),
        packageIDs = sql.rows('select idns_ns, id_value from identifier join identifier_namespace on id_ns_fk = idns_id join package on pkg_id = id_pkg_fk where pkg_id = :pkgId', pkgParams),
        packageAltNames = sql.rows('select altname_name from alternative_name where altname_pkg_fk = :pkgId', pkgParams),
        titlePublishers = sql.rows('select rdv_value, org_guid, org_gokb_id, org_name, or_end_date, or_start_date, or_tipp_fk from org_role join refdata_value on or_roletype_fk = rdv_id join org on or_org_fk = org_id join title_instance_package_platform on or_tipp_fk = tipp_id where tipp_pkg_fk = :pkgId', pkgParams)
        Map<Long, Map<String, GroovyRowResult>> priceItemMap = ExportService.preprocessPriceItemRows(priceItemRows, 'pi_ie_fk')
        Map<Long, List<GroovyRowResult>> identifierMap = ExportService.preprocessRows(idRows, 'id_tipp_fk'),
        coverageMap = ExportService.preprocessRows(coverageRows, 'ic_ie_fk'),
        ddcMap = ExportService.preprocessRows(ddcRows, 'ddc_tipp_fk'),
        languageMap = ExportService.preprocessRows(langRows, 'lang_tipp_fk'),
        altNameMap = ExportService.preprocessRows(altNameRows, 'altname_tipp_fk'),
        publisherMap = ExportService.preprocessRows(titlePublishers, 'or_tipp_fk'),
        platformMap = ExportService.preprocessRows(platformsOfSubscription, 'plat_id')
        Map<String, Object> pkgData = packageOfSubscription.get(0)
        pkgData.ids = packageIDs
        pkgData.altnames = packageAltNames
        ieRows.eachWithIndex{ GroovyRowResult row, int i ->
            //println "now processing row ${i}"
            //result << ApiIssueEntitlement.getIssueEntitlementMap(ie, ignoreRelation, context) // de.laser.IssueEntitlement
            Map<String, Object> ie = [globalUID: row['ie_guid']]
            ie.name = row['ie_name']
            ie.accessStartDate = row['ie_access_start_date'] ? ApiToolkit.formatInternalDate(row['ie_access_start_date']) : null
            ie.accessEndDate = row['ie_access_end_date'] ? ApiToolkit.formatInternalDate(row['ie_access_end_date']) : null
            ie.lastUpdated = row['ie_last_updated'] ? ApiToolkit.formatInternalDate(row['ie_last_updated']) : null
            //RefdataValues
            ie.medium = row['ie_medium'] //fallback tipp_medium already in query
            ie.status = row['ie_status']
            ie.perpetualAccessBySub = ApiStubReader.requestSubscriptionStub(Subscription.get(row['ie_perpetual_access_by_sub_fk']), context, false)
            List coverages = [], priceItems = []
            coverageMap.get(row['ie_id']).eachWithIndex { GroovyRowResult covRow, int j ->
                //println "processing coverage ${j}"
                Map<String, Object> ieCov = [:]
                ieCov.startDate        = covRow.containsKey('ic_start_date') && covRow['ic_start_date'] != null ? ApiToolkit.formatInternalDate(covRow['ic_start_date']) : null
                ieCov.startVolume      = covRow['ic_start_volume']
                ieCov.startIssue       = covRow['ic_start_issue']
                ieCov.endDate          = covRow.containsKey('ic_end_date') && covRow['ic_end_date'] != null ? ApiToolkit.formatInternalDate(covRow['ic_end_date']) : null
                ieCov.endVolume        = covRow['ic_end_volume']
                ieCov.endIssue         = covRow['ic_end_issue']
                ieCov.embargo          = covRow['ic_embargo']
                ieCov.coverageDepth    = covRow['ic_coverage_depth']
                ieCov.coverageNote     = covRow['ic_coverage_note']
                ieCov.lastUpdated      = covRow['ic_last_updated'] ? ApiToolkit.formatInternalDate(covRow['ic_last_updated']) : null
                coverages << ieCov
            }
            ie.coverages = coverages
            priceItemMap.get(row['ie_id']).eachWithIndex { String currency, GroovyRowResult piRow, int p ->
                //println "processing price item ${p}"
                Map<String, Object> pi = [:]
                pi.listCurrency = piRow['pi_list_currency']
                pi.listPrice = piRow['pi_list_price']
                pi.localCurrency = piRow['pi_local_currency']
                pi.localPrice = piRow['pi_local_price']
                priceItems << pi
            }
            ie.priceItems = priceItems
            //References
            row.ids = identifierMap.get(row['tipp_id'])
            row.ddcs = ddcMap.get(row['tipp_id'])
            row.languages = languageMap.get(row['tipp_id'])
            row.altnames = altNameMap.get(row['tipp_id'])
            row.publishers = publisherMap.get(row['tipp_id'])
            if(ignoreRelation != ApiReader.IGNORE_ALL) {
                //println "processing references"
                if(ignoreRelation == ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE) {
                    row.platform = platformMap.get(row['tipp_plat_fk'])[0]
                    row.pkg = pkgData
                    ie.tipp = ApiMapReader.getTippMapWithSQL(row, ApiReader.IGNORE_ALL, context) // de.laser.TitleInstancePackagePlatform
                }
                else {
                    if(ignoreRelation != ApiReader.IGNORE_TIPP) {
                        row.platform = platformMap.get(row['tipp_plat_fk'])[0]
                        row.pkg = pkgData
                        ie.tipp = ApiMapReader.getTippMapWithSQL(row, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.TitleInstancePackagePlatform
                    }
                    if(ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                        ie.subscription = ApiStubReader.requestSubscriptionStub(subPkg.subscription, context) // de.laser.TitleInstancePackagePlatform
                    }
                }
            }
            //println "processing finished"
            result << ie
        }

        return ApiToolkit.cleanUp(result, true, true)
    }

    static Collection<Object> getLanguageCollection(SortedSet<Language> languages) {
        Collection<Object> result = []

        result.addAll(languages.collect { Language lang -> [value: lang.language.value] })

        result
    }

    /**
     * Collects the information on the other ends of the outgoing organisation relations list and returns the stub map
     * of information about the objects linked
     * @param list a {@link List} of {@link OrgRole}s to process
     * @param ignoreRelationType should futher relations be ignored?
     * @param context the requestor institution ({@link Org}) whose perspective is going to be taken during the checks
     * @return a {@link Collection} of map entries reflecting the information about the outgoing relations
     */
    static Collection<Object> getOrgLinkCollection(Collection<OrgRole> list, ignoreRelationType, Org context) { // TODO
        Collection<Object> result = []

        list.each { it ->   // de.laser.OrgRole
            Map<String, Object> tmp = [:]

            tmp.endDate     = ApiToolkit.formatInternalDate(it.endDate)
            tmp.startDate   = ApiToolkit.formatInternalDate(it.startDate)

            // RefdataValues
            tmp.roleType    = it.roleType?.value

            // References
            if (it.org && (ApiReader.IGNORE_ORGANISATION != ignoreRelationType)) {
                tmp.organisation = ApiUnsecuredMapReader.getOrganisationStubMap(it.org) // de.laser.Org
            }
            if (it.lic && (ApiReader.IGNORE_LICENSE != ignoreRelationType)) {
                tmp.license = ApiStubReader.requestLicenseStub(it.lic, context) // de.laser.License
            }
            if (it.pkg && (ApiReader.IGNORE_PACKAGE != ignoreRelationType)) {
                tmp.package = ApiUnsecuredMapReader.getPackageStubMap(it.pkg) // de.laser.Package
            }
            if (it.sub && (ApiReader.IGNORE_SUBSCRIPTION != ignoreRelationType)) {
                tmp.subscription = ApiStubReader.requestSubscriptionStub(it.sub, context) // de.laser.Subscription
            }
            if (it.tipp && (ApiReader.IGNORE_TIPP != ignoreRelationType)) {
                tmp.title = ApiMapReader.getTippMap(it.tipp, ApiReader.IGNORE_ALL, context) // de.laser.titles.TitleInstancePackagePlatform
            }

            result << ApiToolkit.cleanUp(tmp, true, false)
        }
        result
    }

    /**
     * Builds a collection of map entries reflecting the given {@link Collection} of {@link OrgAccessPoint}s
     * @param list a {@link Collection} of {@link OrgAccessPoint}s to process
     * @return a {@link Collection} of entries reflecting the access point records
     */
    static Collection<Object> getOrgAccessPointCollection(Collection<OrgAccessPoint> list) {
        Collection<Object> result = []
        list.each { it -> // de.laser.OrgAccessPoint
            result << ApiUnsecuredMapReader.getOrgAccessPointStubMap(it)
        }
        result
    }

    /**
     * Delivers a package stub map with the issue entitlements (!) belonging to each package subscribed
     * @param list the {@link Collection} of {@link SubscriptionPackage}s which should be returned along with the respective holdings
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during the checks
     * @return a {@link Collection<Object>} reflecting the packages and holdings
     */
    static Collection<Object> getPackageWithIssueEntitlementsCollection(Collection<SubscriptionPackage> list, Org context) {  // TODO - TODO - TODO
        Collection<Object> result = []

        list.each { subPkg ->
            Map<String, Object> pkg = ApiUnsecuredMapReader.getPackageStubMap(subPkg.pkg) // de.laser.Package
            result << pkg

            //if (pkg != Constants.HTTP_FORBIDDEN) {
            //IGNORE_ALL -> IGNORE_SUBSCRIPTION_AND_PACKAGE (bottleneck one)
            pkg.issueEntitlements = getIssueEntitlementCollectionWithSQL(subPkg, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, context)
            //}
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    static Collection<Object> getPriceItemCollection(Set<PriceItem> list) {
        Collection<Object> result = []

        //hasMany-relation generated sets may be not initialised; null check thus necessary!
        list?.each { PriceItem pi ->
            result << ApiUnsecuredMapReader.getPriceItemMap(pi)
        }

        result
    }

    /**
     * Builds a collection of private properties for the given object and respecting the settings of the requestor institution
     * @param list the {@link Collection} of properties to enumerate
     * @param context the requestor institution ({@link Org})
     * @return a {@link Collection} of {@link Map}s containing property details for API output
     */
    static Collection<Object> getPrivatePropertyCollection(Collection list, Org context) {
        Collection<Object> result = []

        list.findAll{ (it.owner.id == context.id || it.type.tenant?.id == context.id) && it.tenant?.id == context.id && it.isPublic == false }?.each { it ->       // de.laser.<x>PrivateProperty
            Map<String, Object> tmp = [:]

            tmp.token   = it.type.name     // de.laser.properties.PropertyDefinition.String
            tmp.scope   = it.type.descr    // de.laser.properties.PropertyDefinition.String
            tmp.note    = it.note
            //tmp.tenant          = ApiStubReader.resolveOrganisationStub(it.tenant, context) // de.laser.Org

            if (it.dateValue) {
                tmp.value   = ApiToolkit.formatInternalDate(it.dateValue)
            }
            else {
                tmp.value   = (it.stringValue ?: (it.intValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
            }

            tmp.type = PropertyDefinition.validTypes[it.type.type]['en']

            if (it.type.isRefdataValueType()) {
                tmp.refdataCategory = it.type.refdataCategory
            }

            //tmp.dateCreated = ApiToolkit.formatInternalDate(it.dateCreated)
            //tmp.lastUpdated = ApiToolkit.formatInternalDate(it._getCalculatedLastUpdated())

            if (it instanceof LicenseProperty) {
                tmp.paragraph = it.paragraph
            }

            if(it.type.tenant?.id == context.id) {
                tmp.isPublic    = "No" // derived to substitute tentant
                result << ApiToolkit.cleanUp(tmp, true, false)
            }
        }
        result
    }

    /**
     * Collects the properties (general and private) of the given object and outputs the collections
     * @param generic the object (one of {@link de.laser.Subscription}, {@link de.laser.License}, {@link Org}, {@link de.laser.Package} or {@link de.laser.Platform})
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @param ignoreFlag should certain properties being left out from output (private or custom)?
     * @return a {@link Collection} of both general and private properties
     */
    static Collection<Object> getPropertyCollection(Object generic, Org context, def ignoreFlag) {
        Collection<Object> cp = getCustomPropertyCollection(generic.propertySet, generic, context)
        Collection<Object> pp = getPrivatePropertyCollection(generic.propertySet, context)

        if (ignoreFlag == ApiReader.IGNORE_CUSTOM_PROPERTIES) {
            return pp
        }
        else if (ignoreFlag == ApiReader.IGNORE_PRIVATE_PROPERTIES) {
            return cp
        }

        pp.each { cp << it }
        cp
    }

    /**
     * Builds a list of entries showing person links from a certain object
     * @param list a {@link List} of links pointing to contact entities
     * @param allowedAddressTypes the types of addresses which can be returned
     * @param allowedContactTypes the types of contacts which can be returned
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @return a {@link List} of map entries reflecting the contact entity details
     * @see de.laser.Person
     */
    static Collection<Object> getPrsLinkCollection(Collection<PersonRole> list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        List result = []
        List tmp = []

        list.each { it ->

            // nested prs
            if(it.prs) {
                String x = it.prs.globalUID
                def person = tmp.find {it.globalUID == x}

                if(!person) {
                    person = ApiMapReader.getPersonMap(it.prs, allowedAddressTypes, allowedContactTypes, context) // de.laser.Person

                    // export public
                    if("No" != person.isPublic?.value?.toString()) {
                        tmp << person
                    }
                    // or private if tenant = context
                    else {
                        if(it.prs.tenant?.id == context.id) {
                            tmp << person
                        }
                    }
                }

                Map<String, Object> role    = [:] // de.laser.PersonRole
                role.startDate              = ApiToolkit.formatInternalDate(it.start_date)
                role.endDate                = ApiToolkit.formatInternalDate(it.end_date)

                // RefdataValues
                role.functionType           = it.functionType?.value
                role.positionType           = it.positionType?.value

                if(! person.roles) {
                    person.roles = []
                }
                if (role.functionType || role.positionType) {
                    person.roles << ApiToolkit.cleanUp(role, true, false)
                }


                // TODO responsibilityType
                /*if (role.responsibilityType) {
                    // References
                    //if (it.org) {
                    //    role.organisation = ApiStubReader.resolveOrganisationStub(it.org, context) // de.laser.Org
                    //}

                    if (it.lic) {
                        role.license = ApiStubReader.resolveLicenseStub(it.lic, context) // de.laser.License
                    }
                    if (it.pkg) {
                        role.package = ApiStubReader.resolvePackageStub(it.pkg, context) // de.laser.Package
                    }
                    if (it.sub) {
                        role.subscription = ApiStubReader.resolveSubscriptionStub(it.sub, context) // de.laser.Subscription
                    }
                    if (it.title) {
                        role.title = ApiStubReader.resolveTitleStub(it.title) // de.laser.titles.TitleInstance
                    }
                }*/
            }
        }

        // export only persons with valid roles
        tmp.each{ person ->
            if (! person.roles.isEmpty()) {
                result << person
            }
        }

        result
    }

    /**
     * Collects for the given list of subscription packages the stubs for API output
     * @param list a {@link List} of {@link SubscriptionPackage}s to be processed
     * @param ignoreRelation should further relations being followed up?
     * @param context the requesting institution ({@link Org}) whose prespective is going to be taken for checks
     * @return a {@link Collection} of maps reflecting the packages with their entitlements
     */
    static Collection<Object> getSubscriptionPackageStubCollection(Collection<SubscriptionPackage> list, def ignoreRelation, Org context) {
        Collection<Object> result = []

        if (! list) {
            return null
        }

        list.each { it -> // de.laser.SubscriptionPackage
            result << ApiStubReader.requestSubscriptionPackageStubMixed(it, ignoreRelation, context)
        }
        result
    }

    /**
     * Builds a collection of title entries for API output.
     * Access rights due wrapping object
     * @param list a {@link Collection} of {@link TitleInstancePackagePlatform}
     * @param ignoreRelation should further relations be followed up?
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @return a {@link Collection<Object>} reflecting the titles in the list
     */
    static Collection<Object> getTippCollection(Collection<TitleInstancePackagePlatform> list, def ignoreRelation, Org context) {
        Collection<Object> result = []

        list.each { TitleInstancePackagePlatform it -> // de.laser.TitleInstancePackagePlatform
            result << ApiMapReader.getTippMap(it, ignoreRelation, context)
        }

        result
    }
}
