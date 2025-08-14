package de.laser.api.v0

import de.laser.AuditConfig
import de.laser.AlternativeName
import de.laser.RefdataValue
import de.laser.wekb.DeweyDecimalClassification
import de.laser.DocContext
import de.laser.Identifier
import de.laser.IdentifierNamespace
import de.laser.IssueEntitlement
import de.laser.wekb.Language
import de.laser.Org
import de.laser.OrgRole
import de.laser.addressbook.PersonRole
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.Subscription
import de.laser.SubscriptionPackage
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.base.AbstractCoverage
import de.laser.finance.BudgetCode
import de.laser.finance.CostItem
import de.laser.finance.PriceItem
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.addressbook.Address
import de.laser.addressbook.Contact
import de.laser.oap.OrgAccessPoint
import de.laser.api.v0.entities.ApiDoc
import de.laser.api.v0.entities.ApiIssueEntitlement
import de.laser.storage.RDStore
import groovy.json.JsonSlurper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.sql.Connection

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

        list.each { it ->   // de.laser.addressbook.Address
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

    /**
     * Processes a collection of alternative names and build a collection of entries for API output
     * @param list the {@link Collection} of {@link AlternativeName}s to output
     * @return a {@link Collection} of alternative names
     */
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

        list.each { it ->       // de.laser.addressbook.Contact
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

        filteredList.each { CostItem ci ->

            Map<String, Object> tmp     = [:]

            tmp.laserID                 = ci.globalUID
            tmp.isVisibleForSubscriber  = ci.isVisibleForSubscriber ? "Yes" : "No"
            tmp.costInBillingCurrency   = ci.costInBillingCurrency
            tmp.costInLocalCurrency     = ci.costInLocalCurrency
            tmp.currencyRate            = ci.currencyRate
            tmp.costTitle               = ci.costTitle
            tmp.costDescription         = ci.costDescription
            //tmp.includeInSubscription   = ci.includeInSubscription
            tmp.reference               = ci.reference

            tmp.costInLocalCurrencyAfterTax     = ci.getCostInLocalCurrencyAfterTax()
            tmp.costInBillingCurrencyAfterTax   = ci.getCostInBillingCurrencyAfterTax()
            tmp.billingSumRounding     = ci.billingSumRounding ? "Yes" : "No"
            tmp.finalCostRounding      = ci.finalCostRounding ? "Yes" : "No"

            tmp.calculatedType      = ci._getCalculatedType()
            tmp.datePaid            = ApiToolkit.formatInternalDate(ci.datePaid)
            tmp.invoiceDate         = ApiToolkit.formatInternalDate(ci.invoiceDate)
            tmp.financialYear       = ci.financialYear?.value
            tmp.startDate           = ApiToolkit.formatInternalDate(ci.startDate)
            tmp.endDate             = ApiToolkit.formatInternalDate(ci.endDate)
            tmp.dateCreated         = ApiToolkit.formatInternalDate(ci.dateCreated)
            tmp.lastUpdated         = ApiToolkit.formatInternalDate(ci.lastUpdated)
            tmp.taxRate             = ci.taxKey?.taxRate

            // RefdataValues
            tmp.billingCurrency     = ci.billingCurrency?.value
            tmp.costItemCategory    = ci.costItemCategory?.value
            tmp.costItemElement     = ci.costItemElement?.value
            tmp.costItemElementConfiguration = ci.costItemElementConfiguration?.value
            tmp.costItemStatus      = ci.costItemStatus?.value
            tmp.taxCode             = ci.taxKey?.taxType?.value

            // References
            //def context = null // TODO: use context
            tmp.budgetCodes         = ci.budgetcodes.collect{ BudgetCode bc -> bc.value }.unique()
            tmp.copyBase            = ci.copyBase?.globalUID
            tmp.invoiceNumber       = ci.invoice?.invoiceNumber // retrieveInvoiceMap(ci.invoice) // de.laser.finance.Invoice
            // tmp.issueEntitlement    = ApiIssueEntitlement.retrieveIssueEntitlementMap(ci.issueEntitlement, ApiReader.IGNORE_ALL, context) // de.laser.IssueEntitlement
            tmp.orderNumber         = ci.order?.orderNumber // retrieveOrderMap(ci.order) // de.laser.finance.Order
            if(ci.costInformationDefinition) {
                tmp.costInformation = [
                        token: ci.costInformationDefinition.getI10n('name'),
                        type: ci.costInformationDefinition.validTypes[ci.costInformationDefinition.type]['en'],
                        value: ci.getCostInformationValue()
                ]
                if(ci.costInformationDefinition.type == RefdataValue.class.name) {
                    tmp.costInformation.refdataCategory = ci.costInformationDefinition.refdataCategory
                }
            }
            // tmp.owner               = ApiStubReader.retrieveOrganisationStubMap(ci.owner, context) // de.laser.Org
            // tmp.sub                 = ApiStubReader.requestSubscriptionStub(ci.sub, context) // de.laser.Subscription // RECURSION ???
            // tmp.pkg             = ApiStubReader.requestSubscriptionPackageStubMixed(ci.subPkg, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.SubscriptionPackage
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
     * @param generic the object (one of {@link de.laser.Subscription}, {@link de.laser.License}, {@link Org}, {@link de.laser.addressbook.Person} or {@link de.laser.wekb.Platform})
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
                tmp.value   = (it.stringValue ?: (it.longValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
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
                        ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status != :statusIe',
                [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_REMOVED, statusIe: RDStore.TIPP_STATUS_REMOVED]
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
    static Collection<Object> getIssueEntitlementCollectionWithSQL(SubscriptionPackage subPkg, ignoreRelation, Org context, Sql sql, int max, int offset){
        Collection<Object> result = []
        /*
        List<IssueEntitlement> ieList = IssueEntitlement.executeQuery(
                'select ie from IssueEntitlement ie join ie.tipp tipp join ie.subscription sub join tipp.pkg pkg ' +
                        ' where sub = :sub and pkg = :pkg and tipp.status != :statusTipp and ie.status != :statusIe',
                [sub: subPkg.subscription, pkg: subPkg.pkg, statusTipp: RDStore.TIPP_STATUS_REMOVED, statusIe: RDStore.TIPP_STATUS_REMOVED]
        )
        */
        Subscription targetSub
        if(subPkg.subscription.instanceOf && (AuditConfig.getConfig(subPkg.subscription.instanceOf, 'holdingSelection')))
            targetSub = subPkg.subscription.instanceOf
        else targetSub = subPkg.subscription
        Map<String, Object> pkgParams = [pkgId: subPkg.pkg.id], ieParams = [sub: targetSub.id, pkg: subPkg.pkg.id]
        List<GroovyRowResult> ieRows = []
        //for(int i = 0; i < ieCount; i += limit) {
            ieRows.addAll(sql.rows("select ie_id, ie_laser_id, ie_access_start_date, ie_access_end_date, ie_last_updated, (select rdv_value from refdata_value where rdv_id = ie_status_rv_fk) as ie_status, (select rdv_value from refdata_value where rdv_id = tipp_medium_rv_fk) as tipp_medium, ie_perpetual_access_by_sub_fk, " +
                    "tipp_laser_id, tipp_name, tipp_host_platform_url, tipp_gokb_id, tipp_pkg_fk, tipp_date_first_in_print, tipp_date_first_online, tipp_first_author, tipp_first_editor, " +
                    "tipp_publisher_name, tipp_imprint, tipp_volume, tipp_edition_number, tipp_last_updated, tipp_series_name, tipp_subject_reference, (select rdv_value from refdata_value where rdv_id = tipp_access_type_rv_fk) as tipp_access_type, (select rdv_value from refdata_value where rdv_id = tipp_open_access_rv_fk) as tipp_open_access, " +
                    "tipp_last_updated, tipp_id, (select rdv_value from refdata_value where rdv_id = tipp_status_rv_fk) as tipp_status, " +
                    "tipp_title_type as title_type " +
                    "from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id " +
                    "where ie_subscription_fk = :sub and tipp_pkg_fk = :pkg order by tipp_sort_name limit :limit offset :offset",
                    ieParams+[limit: max, offset: offset]))
        //}
        JsonSlurper slurper = new JsonSlurper()
        log.debug("now fetching additional params ...")
        Set<Object> tippIDSet = [], ieIDSet = []
        tippIDSet.addAll(ieRows['tipp_id'])
        ieIDSet.addAll(ieRows['ie_id'])
        Object[] tippIDs = tippIDSet.toArray(), ieIDs = ieIDSet.toArray()
        Connection conn = sql.getDataSource().getConnection()
        Map<String, Object> tippSubParams = [tippIDs: conn.createArrayOf('bigint', tippIDs)], ieSubParams = [ieIDs: conn.createArrayOf('bigint', ieIDs)]
        List<GroovyRowResult> listPriceItemRows = [], localPriceItemRows = [], idRows = [], ddcRows = [], langRows = [], coverageRows = []
        //for(int i = 0; i < ieCount; i += limit) {
            listPriceItemRows.addAll(sql.rows("select pi_tipp_fk, json_agg(json_build_object('listCurrency', (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk), 'listPrice', pi_list_price)) as price_items from price_item where pi_tipp_fk = any(:tippIDs) group by pi_tipp_fk", tippSubParams))
            localPriceItemRows.addAll(sql.rows("select pi_ie_fk, json_agg(json_build_object('localCurrency', (select rdv_value from refdata_value where rdv_id = pi_local_currency_rv_fk), 'localPrice', pi_local_price)) as price_items from price_item where pi_ie_fk = any(:ieIDs) group by pi_ie_fk", ieSubParams))
            idRows.addAll(sql.rows("select id_tipp_fk, json_agg(json_build_object('namespace', idns_ns, 'value', id_value)) as identifiers from identifier join identifier_namespace on id_ns_fk = idns_id where id_value != '' and id_value != 'Unknown' and id_tipp_fk = any(:tippIDs) group by id_tipp_fk", tippSubParams))
            ddcRows.addAll(sql.rows("select ddc_tipp_fk, json_agg(json_build_object('value', rdv_value, 'value_de', rdv_value_de, 'value_en', rdv_value_en)) as ddcs from dewey_decimal_classification join refdata_value on ddc_rv_fk = rdv_id where ddc_tipp_fk = any(:tippIDs) group by ddc_tipp_fk", tippSubParams))
            langRows.addAll(sql.rows("select lang_tipp_fk, json_agg(json_build_object('value', rdv_value, 'value_de', rdv_value_de, 'value_en', rdv_value_en)) as languages from language join refdata_value on lang_rv_fk = rdv_id where lang_tipp_fk = any(:tippIDs) group by lang_tipp_fk", tippSubParams))
            coverageRows.addAll(sql.rows("select tc_tipp_fk, json_agg(json_build_object('startDate', coalesce(to_char(tc_start_date,'"+ApiToolkit.DATE_TIME_PATTERN_SQL+"'),''), 'startIssue', tc_start_issue, 'startVolume', tc_start_volume, 'endDate', coalesce(to_char(tc_end_date,'"+ApiToolkit.DATE_TIME_PATTERN_SQL+"'),''), 'endIssue', tc_end_issue, 'endVolume', tc_end_volume, 'coverageDepth', tc_coverage_depth, 'coverageNote', tc_coverage_note, 'embargo', tc_embargo, 'lastUpdated', tc_last_updated)) as coverages from tippcoverage where tc_tipp_fk = any(:tippIDs) group by tc_tipp_fk, tc_start_date, tc_start_volume, tc_start_issue", tippSubParams))
        //}
        List<GroovyRowResult> altNameRows = sql.rows("select altname_name from alternative_name where altname_tipp_fk = any(:tippIDs)", tippSubParams),
        //platformsOfSubscription = sql.rows('select plat_id, plat_gokb_id, plat_name, plat_laser_id, plat_primary_url, (select rdv_value from refdata_value where rdv_id = plat_status_rv_fk) as plat_status from platform join title_instance_package_platform on tipp_plat_fk = plat_id join issue_entitlement on ie_tipp_fk = tipp_id where ie_subscription_fk = :subId', subParams),
        packageOfSubscription = sql.rows("select pkg_laser_id, pkg_gokb_id, pkg_name, (select rdv_value from refdata_value where rdv_id = pkg_status_rv_fk) as pkg_status from package where pkg_id = :pkgId", pkgParams),
        packageIDs = sql.rows("select idns_ns, id_value from identifier join identifier_namespace on id_ns_fk = idns_id join package on pkg_id = id_pkg_fk where pkg_id = :pkgId", pkgParams),
        packageAltNames = sql.rows("select altname_name from alternative_name where altname_pkg_fk = :pkgId", pkgParams)
        Map<Long, Map> priceItemMap = listPriceItemRows.collectEntries { GroovyRowResult row -> [row['pi_tipp_fk'], slurper.parseText(row['price_items'].toString())] },
        identifierMap = idRows.collectEntries { GroovyRowResult row -> [row['id_tipp_fk'], slurper.parseText(row['identifiers'].toString())] },
        coverageMap = coverageRows.collectEntries { GroovyRowResult row -> [row['tc_tipp_fk'], slurper.parseText(row['coverages'].toString())] },
        ddcMap = ddcRows.collectEntries { GroovyRowResult row -> [row['ddc_tipp_fk'], slurper.parseText(row['ddcs'].toString())] },
        languageMap = langRows.collectEntries { GroovyRowResult row -> [row['lang_tipp_fk'], slurper.parseText(row['languages'].toString())] }
        //platformMap = ExportService.preprocessRows(platformsOfSubscription, 'plat_id'),
        //publisherMap = titlePublishers.collectEntries { GroovyRowResult row -> [row['or_tipp_fk'], slurper.parseText(row['publishers'].toString())] }
        //Map<Long, List<GroovyRowResult>> altNameMap = ExportService.preprocessRows(altNameRows, 'altname_tipp_fk')
        Map<String, Object> pkgData = packageOfSubscription.get(0)
        pkgData.ids = packageIDs
        pkgData.altnames = packageAltNames
        //GParsPool.withPool(8) {
            //ieRows.eachWithIndexParallel{ GroovyRowResult row, int i ->
            ieRows.eachWithIndex { GroovyRowResult row, int i ->
                //Subscription.withTransaction {
                    //println "now processing row ${i}"
                    //result << ApiIssueEntitlement.getIssueEntitlementMap(ie, ignoreRelation, context) // de.laser.IssueEntitlement
                    Map<String, Object> ie = [laserID: row['ie_laser_id']]
                    //ie.name = row['ie_name']
                    ie.accessStartDate = row['ie_access_start_date'] ? ApiToolkit.formatInternalDate(row['ie_access_start_date']) : null
                    ie.accessEndDate = row['ie_access_end_date'] ? ApiToolkit.formatInternalDate(row['ie_access_end_date']) : null
                    ie.lastUpdated = row['ie_last_updated'] ? ApiToolkit.formatInternalDate(row['ie_last_updated']) : null
                    //RefdataValues - both removed as of API version 2.0
                    //ie.medium = row['ie_medium']
                    ie.status = row['ie_status']
                    ie.perpetualAccessBySub = ApiStubReader.requestSubscriptionStub(Subscription.get(row['ie_perpetual_access_by_sub_fk']), context, false)
                    ie.coverages = coverageMap.containsKey(row['tipp_id']) ? coverageMap.get(row['tipp_id']) : []
                    ie.priceItems = priceItemMap.containsKey(row['tipp_id']) ? priceItemMap.get(row['tipp_id']) : []
                    //References
                    row.ids = identifierMap.containsKey(row['tipp_id']) ? identifierMap.get(row['tipp_id']) : []
                    row.ddcs = ddcMap.containsKey(row['tipp_id']) ? ddcMap.get(row['tipp_id']) : []
                    row.languages = languageMap.containsKey(row['tipp_id']) ? languageMap.get(row['tipp_id']) : []
                    row.altnames = altNameRows
                    row.publishers = [] //publisherMap.containsKey(row['tipp_id']) ? publisherMap.get(row['tipp_id']) : []
                    if(ignoreRelation != ApiReader.IGNORE_ALL) {
                        //println "processing references"
                        if(ignoreRelation == ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE) {
                            //row.platform = platformMap.get(row['tipp_plat_fk'])[0]
                            row.pkg = pkgData
                            ie.tipp = ApiMapReader.getTippMapWithSQL(row, ApiReader.IGNORE_ALL, context) // de.laser.wekb.TitleInstancePackagePlatform
                        }
                        else {
                            if(ignoreRelation != ApiReader.IGNORE_TIPP) {
                                //row.platform = platformMap.get(row['tipp_plat_fk'])[0]
                                row.pkg = pkgData
                                ie.tipp = ApiMapReader.getTippMapWithSQL(row, ApiReader.IGNORE_SUBSCRIPTION, context) // de.laser.wekb.TitleInstancePackagePlatform
                            }
                            if(ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                                ie.subscription = ApiStubReader.requestSubscriptionStub(subPkg.subscription, context) // de.laser.wekb.TitleInstancePackagePlatform
                            }
                        }
                    }
                    //println "processing finished"
                    result << ApiToolkit.cleanUp(ie, true, true)
                //}
            //}
            }
        //}
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Processes a collection of languages and build a collection of entries for API output
     * @param languages the {@link SortedSet} of {@link Language}s to output
     * @return a {@link Collection} of language entries
     */
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
            if (it.sub && (ApiReader.IGNORE_SUBSCRIPTION != ignoreRelationType)) {
                tmp.subscription = ApiStubReader.requestSubscriptionStub(it.sub, context) // de.laser.Subscription
            }

            result << ApiToolkit.cleanUp(tmp, true, false)
        }
        result
    }

    /**
     * Collects the information on the other ends of the outgoing provider relations list and returns the stub map
     * of information about the objects linked
     * @param list a {@link List} of {@link Provider}s to process
     * @return a {@link Collection} of map entries reflecting the information about the outgoing relations
     */
    static Collection<Object> getProviderCollection(Collection<Provider> list) {
        Collection<Object> result = []

        list.each { Provider p ->
            Map<String, Object> tmp = ApiUnsecuredMapReader.getProviderStubMap(p)
            result << ApiToolkit.cleanUp(tmp, true, false)
        }

        result
    }

    /**
     * Collects the information on the other ends of the outgoing library supplier relations list and returns the stub map
     * of information about the objects linked
     * @param list a {@link List} of library suppliers ({@link Vendor}s) to process
     * @return a {@link Collection} of map entries reflecting the information about the outgoing relations
     */
    static Collection<Object> getLibrarySuppliers(Collection<Vendor> list) {
        Collection<Object> result = []

        list.each { Vendor v ->
            Map<String, Object> tmp = ApiUnsecuredMapReader.getLibrarySupplierStubMap(v)
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
     * Delivers a package stub map without titles
     * @param list the {@link Collection} of {@link de.laser.wekb.Package}s which should be returned
     * @return a {@link Collection<Object>} reflecting the packages
     */
    static Collection<Object> getPackageCollection(Collection<de.laser.wekb.Package> list) {
        Collection<Object> result = []

        list.each { pkg ->
            Map<String, Object> pkgMap = ApiUnsecuredMapReader.getPackageStubMap(pkg) // de.laser.wekb.Package
            result << pkgMap
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    /**
     * Delivers a package stub map with the issue entitlements (!) belonging to each package subscribed
     * @param list the {@link Collection} of {@link SubscriptionPackage}s which should be returned along with the respective holdings
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during the checks
     * @return a {@link Collection<Object>} reflecting the packages and holdings
     */
    static Collection<Object> getPackageWithIssueEntitlementsCollection(Collection<SubscriptionPackage> list, Org context, int max, int offset, Sql sql = null) {  // TODO - TODO - TODO
        Collection<Object> result = []

        list.each { SubscriptionPackage subPkg ->
            Map<String, Object> pkg = ApiUnsecuredMapReader.getPackageStubMap(subPkg.pkg) // de.laser.wekb.Package
            int ieCount = sql.rows("select count(*) from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id where ie_subscription_fk = :sub and tipp_pkg_fk = :pkg", [sub: subPkg.subscription.id, pkg: subPkg.pkg.id])[0]["count"]


            //if (pkg != Constants.HTTP_FORBIDDEN) {
            //IGNORE_ALL -> IGNORE_SUBSCRIPTION_AND_PACKAGE (bottleneck one)
            if(offset <= ieCount) {
                pkg.issueEntitlements = getIssueEntitlementCollectionWithSQL(subPkg, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE, context, sql, max, offset)
            }
            pkg.recordTotalCount = ieCount
            pkg.recordCount = pkg.issueEntitlements ? pkg.issueEntitlements.size() : 0
            pkg.offset = offset
            pkg.max = max
            pkg.currentPage = (offset/max)+1
            pkg.totalPage = Math.ceil(ieCount/max)
            result << pkg
            //}
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    /**
     * Delivers a package stub map without titles
     * @param list the {@link Collection} of {@link Platform}s which should be returned
     * @return a {@link Collection<Object>} reflecting the packages
     */
    static Collection<Object> getPlatformCollection(Collection<Platform> list) {
        Collection<Object> result = []

        list.each { plat ->
            Map<String, Object> platformMap = ApiUnsecuredMapReader.getPlatformStubMap(plat) // de.laser.wekb.Platform
            result << platformMap
        }

        return ApiToolkit.cleanUp(result, true, false)
    }

    /**
     * Processes a collection of price items and build a collection of entries for API output
     * @param list a {@link Set} of {@link PriceItem}s to output
     * @return a {@link Collection} of price items
     */
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
                tmp.value   = (it.stringValue ?: (it.longValue ?: (it.decValue ?: (it.refValue?.value ?: (it.urlValue ?: null))))) // RefdataValue
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
     * @param generic the object (one of {@link de.laser.Subscription}, {@link de.laser.License}, {@link Org}, {@link de.laser.wekb.Package} or {@link de.laser.wekb.Platform})
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
     * @see de.laser.addressbook.Person
     */
    static Collection<Object> getPrsLinkCollection(Collection<PersonRole> list, allowedAddressTypes, allowedContactTypes, Org context) {  // TODO check context
        List result = []
        List tmp = []

        list.each { it ->

            // nested prs
            if(it.prs) {
                String x = it.prs.laserID
                def person = tmp.find {it.laserID == x}

                if(!person) {
                    person = ApiMapReader.getPersonMap(it.prs, allowedAddressTypes, allowedContactTypes, context) // de.laser.addressbook.Person

                    // export public
                    if(it.prs.isPublic) {
                        tmp << person
                    }
                    // or private if tenant = context
                    else {
                        if(it.prs.tenant?.id == context.id) {
                            tmp << person
                        }
                    }
                }

                Map<String, Object> role    = [:] // de.laser.addressbook.PersonRole
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
                        role.package = ApiStubReader.resolvePackageStub(it.pkg, context) // de.laser.wekb.Package
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

    static Collection<Object> getTippCollectionWithSQL(Sql sql, Collection<Long> list, def ignoreRelation) {
        Set<String> fieldClauses = []
        fieldClauses << "tipp_laser_id as \"laserID\""
        fieldClauses << "tipp_gokb_id as \"wekbId\""
        fieldClauses << "tipp_name as name"
        fieldClauses << "(select rdv_value from refdata_value where rdv_id = tipp_medium_rv_fk) as medium"
        fieldClauses << "(select rdv_value from refdata_value where rdv_id = tipp_status_rv_fk) as status"
        fieldClauses << "(select array_agg(altname_name) from alternative_name where altname_tipp_fk = tipp_id) as altnames"
        fieldClauses << "tipp_first_author as \"firstAuthor\""
        fieldClauses << "tipp_first_editor as \"firstEditor\""
        fieldClauses << "tipp_edition_statement as \"editionStatement\""
        fieldClauses << "tipp_publisher_name as \"publisherName\""
        fieldClauses << "tipp_host_platform_url as \"hostPlatformURL\""
        fieldClauses << "to_char(tipp_date_first_in_print, '${ApiToolkit.DATE_TIME_PATTERN_SQL}') as \"dateFirstInPrint\""
        fieldClauses << "to_char(tipp_date_first_online, '${ApiToolkit.DATE_TIME_PATTERN_SQL}') as \"dateFirstOnline\""
        fieldClauses << "tipp_series_name as \"seriesName\""
        fieldClauses << "tipp_subject_reference as \"subjectReference\""
        fieldClauses << "tipp_title_type as \"titleType\""
        fieldClauses << "tipp_volume as volume"
        fieldClauses << "to_char(tipp_last_updated, '${ApiToolkit.DATE_TIME_PATTERN_SQL}') as \"lastUpdated\""
        fieldClauses << "(select rdv_value from refdata_value where rdv_id = tipp_access_type_rv_fk) as \"accessType\""
        fieldClauses << "(select rdv_value from refdata_value where rdv_id = tipp_open_access_rv_fk) as \"openAccess\""

        // References
        fieldClauses << "(select json_agg(json_build_object('startDate', coalesce(to_char(tc_start_date,'${ApiToolkit.DATE_TIME_PATTERN_SQL}'),''), 'startIssue', tc_start_issue, 'startVolume', tc_start_volume, 'endDate', coalesce(to_char(tc_end_date,'${ApiToolkit.DATE_TIME_PATTERN_SQL}'),''), 'endIssue', tc_end_issue, 'endVolume', tc_end_volume, 'coverageDepth', tc_coverage_depth, 'coverageNote', tc_coverage_note, 'embargo', tc_embargo, 'lastUpdated', tc_last_updated)) from tippcoverage where tc_tipp_fk = tipp_id) as coverages" //de.laser.wekb.TIPPCoverage
        fieldClauses << "(select json_agg(json_build_object('listCurrency', (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk), 'listPrice', pi_list_price)) from price_item where pi_tipp_fk = tipp_id) as price_items" //de.laser.finance.PriceItem with pi.tipp != null
        fieldClauses << "(select json_agg(json_build_object('namespace', idns_ns, 'value', id_value)) from identifier join identifier_namespace on id_ns_fk = idns_id where id_value != '' and id_value != 'Unknown' and id_tipp_fk = tipp_id) as identifiers" // de.laser.Identifier
        fieldClauses << "(select json_agg(json_build_object('value', rdv_value, 'value_de', rdv_value_de, 'value_en', rdv_value_en)) from dewey_decimal_classification join refdata_value on ddc_rv_fk = rdv_id where ddc_tipp_fk = tipp_id) as ddcs"
        fieldClauses << "(select json_agg(json_build_object('value', rdv_value, 'value_de', rdv_value_de, 'value_en', rdv_value_en)) from language join refdata_value on lang_rv_fk = rdv_id where lang_tipp_fk = tipp_id) as languages"

        /*
        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiUnsecuredMapReader.getPackageStubMap(tipp.pkg) // de.laser.wekb.Package
            }
        }
        */
        if (!(ignoreRelation in [ApiReader.IGNORE_SUBSCRIPTION, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE])) {
            //list here every property which may differ on entitlement level (= GlobalSourceSyncService's controlled properties, see getTippDiff() for the properties to be excluded here)
            fieldClauses << "to_char(tipp_access_start_date, '${ApiToolkit.DATE_TIME_PATTERN_SQL}') as \"accessStartDate\""
            fieldClauses << "to_char(tipp_access_end_date, '${ApiToolkit.DATE_TIME_PATTERN_SQL}') as \"accessEndDate\""
        }

        String baseQuery = "select ${fieldClauses.join(',')} from title_instance_package_platform where tipp_id = any(:tippIDs)"
        Collection<Object> result = []
        sql.withTransaction { Connection c ->
            Map<String, Object> baseQueryParams = [tippIDs: c.createArrayOf('bigint', list.toArray())]
            sql.rows(baseQuery, baseQueryParams).each { GroovyRowResult row ->
                JsonSlurper slurper = new JsonSlurper()
                row.coverages = slurper.parseText(row['coverages'].toString())
                row.identifiers = slurper.parseText(row['identifiers'].toString())
                row.priceItems = slurper.parseText(row.remove('price_items').toString())
                row.ddcs = slurper.parseText(row['ddcs'].toString())
                row.languages = slurper.parseText(row['languages'].toString())
                result << row
            }
        }

        return ApiToolkit.cleanUp(result, true, true)
    }
}
