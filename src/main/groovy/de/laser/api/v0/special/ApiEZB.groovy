package de.laser.api.v0.special

import de.laser.*
import de.laser.api.v0.*
import de.laser.exceptions.NativeSqlException
import de.laser.storage.Constants
import de.laser.traces.DeletedObject
import de.laser.utils.DateUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.utils.SwissKnife
import de.laser.wekb.Platform
import grails.converters.JSON
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

import java.sql.Connection
import java.sql.Timestamp
import java.text.SimpleDateFormat

/**
 * This class is an endpoint implemented for the Electronic Journals Library (Elektronische Zeitschriftenbibliothek) of the <a href="https://ezb.uni-regensburg.de/index.phtml?bibid=AAAAA&colors=7&lang=en">Regensburg university library</a>.
 */
@Slf4j
class ApiEZB {

    /**
     * Checks EZB_SERVER_ACCESS, i.e. if the given institution authorised access to its data for the EZB endpoint
     * @param org the institution ({@link Org}) whose data should be accessed
     * @return true if access is granted, false otherwise
     */
    static boolean calculateAccess(Org org) {

        def resultSetting = OrgSetting.get(org, OrgSetting.KEYS.EZB_SERVER_ACCESS)
        resultSetting != OrgSetting.SETTING_NOT_FOUND && resultSetting.getValue()?.value == 'Yes'
        /*Identifier hasEZBID = Identifier.findByOrgAndNs(org, IdentifierNamespace.EZB_ORG_ID)
        //if(hasEZBID) {
            return true
        }
        else {
            return false
        }
        */
    }

    /**
     * Checks if the given subscription is accessible.
     * Checks implicitly EZB_SERVER_ACCESS, i.e. if the requested institution is among those who authorised access to the EZB endpoint
     * @param sub the {@link Subscription} to which access is requested
     * @return true if access is granted, false otherwise
     */
    static boolean calculateAccess(Subscription sub) {

        boolean hasAccess = false

        if (! sub.isPublicForApi) {
            hasAccess = false
        }
        else {
            List<Org> orgs = getAccessibleOrgs()

            if (orgs) {
                List<OrgRole> valid = OrgRole.executeQuery(
                        "select oo from OrgRole oo join oo.sub sub join oo.org org " +
                        "where sub = :sub and org in (:orgs) and oo.roleType in (:roles) ", [
                            sub  : sub,
                            orgs : orgs,
                            roles: [RDStore.OR_SUBSCRIPTION_CONSORTIUM, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]
                        ]
                )
                hasAccess = ! valid.isEmpty()
            }
        }

        hasAccess
    }

    /**
     * Checks if the given license is accessible.
     * Checks implicitly EZB_SERVER_ACCESS, i.e. if the requested institution is among those who authorised access to the EZB endpoint
     * @param lic the {@link License} to which access is requested
     * @return true if access is granted, false otherwise
     */
    static boolean calculateAccess(License lic) {

        boolean hasAccess = false

        if (! lic.isPublicForApi) {
            hasAccess = false
        }
        else {
            List<Org> orgs = getAccessibleOrgs()

            if (orgs) {
                List<OrgRole> valid = OrgRole.executeQuery(
                        "select oo from OrgRole oo join oo.lic lic join oo.org org " +
                        "where lic = :lic and org in (:orgs) and oo.roleType in (:roles) ", [
                            lic  : lic,
                            orgs : orgs,
                            roles: [RDStore.OR_LICENSING_CONSORTIUM, RDStore.OR_LICENSEE_CONS, RDStore.OR_LICENSEE]
                        ]
                )
                hasAccess = ! valid.isEmpty()
            }
        }

        hasAccess
    }

    /**
     * Retrieves all institutions which have given access to the EZB.
     * Checks EZB_SERVER_ACCESS; here those which have granted access to their data for the EZB
     */
    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSetting.executeQuery(
                "select o from OrgSetting os join os.org o where os.key = :key and os.rdValue = :rdValue and o.archiveDate is null", [
                key    : OrgSetting.KEYS.EZB_SERVER_ACCESS,
                rdValue: RDStore.YN_YES
        ])

        orgs
    }

    /**
     * Lists the details of all institutions which have granted access to the EZB endpoint.
     * Checks implicit EZB_SERVER_ACCESS, i.e. if the requested institution is among those which gave permission to EZB
     * @return a {@link JSON} containing a list of the organisation stubs
     */
    static JSON getAllOrgs() {
        Collection<Object> result = []

        List<Org> orgs = getAccessibleOrgs(), orgsWithEZBPerm = ApiToolkit.getOrgsWithSpecialAPIAccess(ApiToolkit.API_LEVEL_EZB)
        orgs.each { o ->
            result << ApiUnsecuredMapReader.getOrganisationStubMap(o)
        }
        DeletedObject.withTransaction {
            DeletedObject.executeQuery('select do from DeletedObject do join do.combos delc where do.oldObjectType = :org and delc.accessibleOrg in (:orgsWithOAPerm)', [org: Org.class.name, orgsWithEZBPerm: orgsWithEZBPerm]).each { DeletedObject delObj ->
                result.addAll(ApiUnsecuredMapReader.getDeletedObjectStubMap(delObj))
            }
        }

        return result ? new JSON(result) : null
    }

    /**
     * Retrieves a list of all accessible subscriptions for the EZB harvester
     * @param changedFrom a timestamp from which recent subscriptions should be retrieved
     * @param targetInstitution the institution for which data should be requested
     * @return JSON | FORBIDDEN
     */
    static JSON getAllSubscriptions(Date changedFrom = null, Org targetInstitution = null) {
        Collection<Object> result = []

        List<Org> orgs = []
        if(targetInstitution) {
            orgs << targetInstitution
        }
        else {
            orgs = getAccessibleOrgs()
        }
        orgs.each { Org org ->
            Map<String, Object> orgStubMap = ApiUnsecuredMapReader.getOrganisationStubMap(org)
            orgStubMap.subscriptions = []
            String queryString = 'SELECT DISTINCT(sub) FROM Subscription sub JOIN sub.orgRelations oo WHERE oo.org = :owner AND oo.roleType in (:roles) AND sub.isPublicForApi = true AND (sub.status = :current OR (sub.status in (:otherAccessible) AND sub.hasPerpetualAccess = true))' //as of November 24th, '22, per Miriam's suggestion, only current subscriptions should be made available for EZB yellow switch
            Map<String, Object> queryParams = [owner: org, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIUM], current: RDStore.SUBSCRIPTION_CURRENT, otherAccessible: [RDStore.SUBSCRIPTION_EXPIRED, RDStore.SUBSCRIPTION_TEST_ACCESS]] //set to OR_SUBSCRIBER_CONS
            if(changedFrom) {
                queryString += ' AND sub.lastUpdatedCascading >= :changedFrom'
                queryParams.changedFrom = changedFrom
            }
            List<Subscription> available = Subscription.executeQuery(queryString, queryParams) as List<Subscription>

            println "${available.size()} available subscriptions found .."

            available.each { Subscription sub ->
                Map<String, Object> subscriptionStubMap = ApiUnsecuredMapReader.getSubscriptionStubMap(sub)
                //not needed, we regard bottommost level, consortium does not want to care about permissions
                /*
                Set<OrgRole> availableMembers = OrgRole.executeQuery('select oo from OrgRole oo where oo.sub.instanceOf = :parent and oo.roleType = :roleType and exists(select os from OrgSetting os where os.org = oo.org and os.key = :ezbAccess and os.rdValue = :yes)', [parent: sub, roleType: RDStore.OR_SUBSCRIBER_CONS, ezbAccess: OrgSetting.KEYS.EZB_SERVER_ACCESS, yes: RDStore.YN_YES])
                subscriptionStubMap.members = ApiCollectionReader.getOrgLinkCollection(availableMembers, ApiReader.IGNORE_SUBSCRIPTION, contextOrg)
                 */
                orgStubMap.subscriptions.add(subscriptionStubMap)
            }
            if(available.size() > 0)
                result << orgStubMap
        }

        return (result ? new JSON(result) : null)
    }

    /**
     * Requests the given subscription and returns a TSV table containing the requested subscription's details if the requesting institution has access to the details.
     * The table is in the KBART format, see the <a href="https://www.niso.org/standards-committees/kbart">KBART specification</a>.
     * @param sub the {@link Subscription} to be retrieved
     * @return TSV | FORBIDDEN
     * @see Subscription
     */
    static requestSubscription(Subscription sub, Date changedFrom = null) {
        Map<String, List> export

        boolean hasAccess = calculateAccess(sub)
        if (hasAccess) {
            long start = System.currentTimeMillis()
            List<Platform> platCheck = Platform.executeQuery('select plat.titleNamespace from SubscriptionPackage sp join sp.pkg pkg join pkg.nominalPlatform plat where sp.subscription = :sub', [sub: sub])
            String titleNS = null
            if(platCheck) {
                titleNS = platCheck[0]
            }
            else {
                log.error("No platform available! Continue without proprietary namespace!")
            }
            String dateFilter = ""
            Map<String, Object> queryParams = [sub: sub, removed: RDStore.TIPP_STATUS_REMOVED]
            if(changedFrom) {
                dateFilter = " and ie_last_updated >= :changedFrom "
                queryParams.changedFrom = new Timestamp(changedFrom.getTime())
            }
            Set<IdentifierNamespace> otherTitleIdentifierNamespaces = IdentifierNamespace.executeQuery('select idns from Identifier id join id.ns idns where idns.ns not in (:coreTitleNS) and id.tipp in (select tipp from TitleInstancePackagePlatform tipp where tipp.pkg in (select sp.pkg from SubscriptionPackage sp where sp.subscription = :sub))', [sub: sub, coreTitleNS: IdentifierNamespace.CORE_TITLE_NS])
            Object[] printIDNS = [IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISBN, IdentifierNamespace.NS_TITLE).id, IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISSN, IdentifierNamespace.NS_TITLE).id],
                     onlineIDNS = [IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISBN, IdentifierNamespace.NS_TITLE).id, IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EISSN, IdentifierNamespace.NS_TITLE).id]
            Set<Long> ieIDs = IssueEntitlement.executeQuery('select ie.id from IssueEntitlement ie where ie.subscription = :sub and ie.status != :removed', queryParams)
            Long pkgIDNS = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.PKG_ID, IdentifierNamespace.NS_PACKAGE).id,
                    zdb = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ZDB, IdentifierNamespace.NS_TITLE).id,
                    doi = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.DOI, IdentifierNamespace.NS_TITLE).id,
                    ezb = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB, IdentifierNamespace.NS_TITLE).id,
                    isci = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ISCI, IdentifierNamespace.NS_PACKAGE).id,
                    isil = IdentifierNamespace.findByNs(IdentifierNamespace.ISIL_PAKETSIGEL).id,
                    //package_ezb_anchor = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB_ANCHOR, IdentifierNamespace.NS_PACKAGE).id,
                    zdb_ppn = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.ZDB_PPN, IdentifierNamespace.NS_TITLE).id,
                    ezb_collection = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB_COLLECTION_ID, IdentifierNamespace.NS_SUBSCRIPTION).id,
                    ezb_anchor = IdentifierNamespace.findByNsAndNsType(IdentifierNamespace.EZB_ANCHOR, IdentifierNamespace.NS_SUBSCRIPTION).id
            try {
                Sql sql = GlobalService.obtainSqlConnection()
                try {
                    String prefix = "create_cell('kbart', ", suffix = ", null)"
                    Map<String, String> kbartCols = [publication_title: "tipp_name",
                                                     print_identifier: "(select id_value from identifier where id_ns_fk = any(:printIDNS) and id_tipp_fk = tipp_id)",
                                                     online_identifier: "(select id_value from identifier where id_ns_fk = any(:onlineIDNS) and id_tipp_fk = tipp_id)",
                                                     date_first_issue_online: "to_char(coalesce(ic_start_date, tc_start_date),'yyyy-MM-dd')",
                                                     num_first_volume_online: "coalesce(ic_start_volume, tc_start_volume)",
                                                     num_first_issue_online: "coalesce(ic_start_issue, tc_start_issue)",
                                                     date_last_issue_online: "to_char(coalesce(ic_end_date, tc_end_date),'yyyy-MM-dd')",
                                                     num_last_volume_online: "coalesce(ic_end_volume, tc_end_volume)",
                                                     num_last_issue_online: "coalesce(ic_end_issue, tc_end_issue)",
                                                     title_url: "tipp_host_platform_url",
                                                     first_author: "tipp_first_author",
                                                     title_id: "(select id_value from identifier where id_ns_fk = :titleNS and id_tipp_fk = tipp_id)",
                                                     embargo_information: "coalesce(ic_embargo, tc_embargo)",
                                                     coverage_depth: "coalesce(ic_coverage_depth, tc_coverage_depth)",
                                                     notes: "coalesce(ic_coverage_note, tc_coverage_note)",
                                                     publication_type: "tipp_title_type",
                                                     publisher_name: "tipp_publisher_name",
                                                     date_monograph_published_print: "to_char(tipp_date_first_in_print, 'yyyy-MM-dd')",
                                                     date_monograph_published_online: "to_char(tipp_date_first_online, 'yyyy-MM-dd')",
                                                     monograph_volume: "tipp_volume",
                                                     monograph_edition: "tipp_edition_number::text",
                                                     first_editor: "tipp_first_editor",
                                                     parent_publication_title_id: "' '", //no value yet, await we:kb
                                                     preceding_publication_title_id: "' '", //no value yet, await we:kb
                                                     package_name: "(select pkg_name from package where pkg_id = tipp_pkg_fk)",
                                                     package_id: "(select string_agg(id_value,',') from identifier where id_pkg_fk = tipp_pkg_fk and id_ns_fk = :pkgID)",
                                                     tipp_last_updated: "to_char(tipp_last_updated, 'yyyy-MM-dd')",
                                                     access_start_date: "to_char(tipp_access_start_date, 'yyyy-MM-dd')",
                                                     access_end_date: "to_char(tipp_access_end_date, 'yyyy-MM-dd')",
                                                     medium: "(select rdv_value from refdata_value where rdv_id = tipp_medium_rv_fk)",
                                                     zdb_id: "(select id_value from identifier where id_ns_fk = :zdb and id_tipp_fk = tipp_id)",
                                                     doi_identifier: "(select id_value from identifier where id_ns_fk = :doi and id_tipp_fk = tipp_id)",
                                                     ezb_id: "(select id_value from identifier where id_ns_fk = :ezb and id_tipp_fk = tipp_id)",
                                                     package_isci: "(select id_value from identifier where id_ns_fk = :isci and id_pkg_fk = tipp_pkg_fk)",
                                                     package_isil: "(select id_value from identifier where id_ns_fk = :isil and id_pkg_fk = tipp_pkg_fk)",
                                                     package_ezb_anchor: "' '",
                                                     ill_indicator: "' '",
                                                     superseding_publication_title_id: "' '",
                                                     monograph_parent_collection_title: "tipp_series_name",
                                                     subject_area: "tipp_subject_reference",
                                                     status: "(select rdv_value from refdata_value where rdv_id = ie_status_rv_fk)",
                                                     access_type: "case when tipp_access_type_rv_fk = :free then 'F' when tipp_access_type_rv_fk = :paid then 'P' else ' ' end",
                                                     oa_type: "(select rdv_value from refdata_value where rdv_id = tipp_open_access_rv_fk)",
                                                     zdb_ppn: "(select id_value from identifier where id_ns_fk = :zdb_ppn and id_tipp_fk = tipp_id)",
                                                     ezb_anchor: "(select id_value from identifier where id_ns_fk = :ezb_anchor and id_sub_fk = ie_subscription_fk)",
                                                     ezb_collection_id: "(select id_value from identifier where id_ns_fk = :ezb_collection and id_sub_fk = ie_subscription_fk)",
                                                     list_price_eur: "(select trim(to_char(pi_list_price, '999999999.99')) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :eur order by pi_date_created desc limit 1)",
                                                     list_price_gbp: "(select trim(to_char(pi_list_price, '999999999.99')) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :gbp order by pi_date_created desc limit 1)",
                                                     list_price_usd: "(select trim(to_char(pi_list_price, '999999999.99')) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :usd order by pi_date_created desc limit 1)",
                                                     local_price_eur: "(select trim(to_char(pi_local_price, '999999999.99')) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :eur order by pi_date_created desc limit 1)",
                                                     local_price_gbp: "(select trim(to_char(pi_local_price, '999999999.99')) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :gbp order by pi_date_created desc limit 1)",
                                                     local_price_usd: "(select trim(to_char(pi_local_price, '999999999.99')) from price_item where pi_tipp_fk = tipp_id and pi_list_currency_rv_fk = :usd order by pi_date_created desc limit 1)"
                    ]
                    Set<String> titleHeaders = getBaseTitleHeaders()
                    Set<String> cells = kbartCols.collect { String colHeader, String col -> "${prefix}${col}${suffix} as ${colHeader}" }
                    log.debug("Begin generateTitleExportKBARTSQL")
                    sql.withTransaction { Connection c ->
                        export = [titleRow:titleHeaders,columnData:[]]
                        Map<String, Object> rowQueryParams = [printIDNS: c.createArrayOf('bigint', printIDNS),
                                                              onlineIDNS: c.createArrayOf('bigint', onlineIDNS),
                                                              titleNS: titleNS,
                                                              pkgIDNS: pkgIDNS,
                                                              free: RDStore.TIPP_PAYMENT_FREE.id,
                                                              paid: RDStore.TIPP_PAYMENT_PAID.id,
                                                              eur: RDStore.CURRENCY_EUR.id,
                                                              gbp: RDStore.CURRENCY_GBP.id,
                                                              usd: RDStore.CURRENCY_USD.id,
                                                              zdb: zdb,
                                                              doi: doi,
                                                              ezb: ezb,
                                                              isci: isci,
                                                              isil: isil,
                                                              zdb_ppn: zdb_ppn,
                                                              ezb_collection: ezb_collection,
                                                              ezb_anchor: ezb_anchor]
                        otherTitleIdentifierNamespaces.each { IdentifierNamespace idNs ->
                            String idNsName = SwissKnife.toSnakeCase(idNs.ns)
                            titleHeaders.add(idNsName)
                            kbartCols.put(idNsName, "(select id_value from identifier where id_ns_fk = :${idNsName} and id_tipp_fk = tipp_id)")
                            rowQueryParams.put(idNsName, idNs.id)
                        }
                        int current = 0, pageSize = 20000
                        ieIDs.collate(pageSize).each { List subList ->
                            log.debug("processing rows ${current}-${current+pageSize} at ${System.currentTimeMillis()-start} msecs")
                            rowQueryParams.subList = c.createArrayOf('bigint', subList.toArray())
                            List<GroovyRowResult> entitlementRows = sql.rows("select ${cells.join(',')} " +
                                    "from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id left join issue_entitlement_coverage on ie_id = ic_ie_fk left join tippcoverage on tipp_id = tc_tipp_fk " +
                                    "where ie_id = any(:subList) order by tipp_sort_name",rowQueryParams)
                            export.columnData.addAll(entitlementRows.collect { GroovyRowResult row -> row.values() })
                            current += pageSize
                        }
                        /*
                        if(packageData.size() > 0) {
                            entitlementRows.eachWithIndex { GroovyRowResult row, int i ->
                                //this double-structure is needed because KBART standard foresees an extra row for each coverage statement
                                List<GroovyRowResult> coverageRows = sql.rows('select ic_start_date, ic_start_issue, ic_start_volume, ic_end_date, ic_end_issue, ic_end_volume, ic_coverage_depth, ic_coverage_note, ic_embargo from issue_entitlement_coverage where ic_ie_fk = :entitlement order by ic_start_date, ic_start_volume, ic_start_issue', [entitlement: row['ie_id']])
                                row.putAll(packageData.find { GroovyRowResult pkgRow -> pkgRow['pkg_id'] == row['tipp_pkg_fk'] })
                                List<GroovyRowResult> currPkgIds = packageIDs.findAll { GroovyRowResult pkgIdRow -> pkgIdRow['id_pkg_fk'] == row['tipp_pkg_fk'] }
                                if(coverageRows) {
                                    coverageRows.each { GroovyRowResult innerRow ->
                                        row.putAll(innerRow)
                                        export.columnData.add(buildRow(sql, row, currPkgIds, titleNS, otherTitleIdentifierNamespaces, priceItems))
                                    }
                                }
                                else
                                    export.columnData.add(buildRow(sql, row, currPkgIds, titleNS, otherTitleIdentifierNamespaces, priceItems))
                            }
                        }
                        */
                    }
                }
                finally {
                    sql.close()
                }
                //export.columnData = export.columnData.take(1000) //for debug purposes
            }
            catch (NativeSqlException e) {
                log.error(e.getMessage())
                return Constants.HTTP_SERVICE_UNAVAILABLE
            }
            log.debug("End generateTitleExportKBARTSQL")
            //ApiToolkit.cleanUp(result, true, true)
        }

        return (hasAccess ? export : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Requests the ILL indicators (interlibrary loan indicators) for the given license and outputs them as a set of {@link LicenseProperty} records in a JSON array
     * @param lic the license whose interlibrary loan
     * @return JSON | FORBIDDEN
     * @see License
     */
    static requestIllIndicators(License lic) {
        List<Map<String, Object>> result = []
        if(!lic) {
            return null
        }
        boolean hasAccess = calculateAccess(lic)
        if(hasAccess) {
            Set<PropertyDefinition> illIndicators = PropertyDefinition.findAllByNameInListAndDescr(['Ill ZETA code', 'Ill ZETA electronic forbidden', 'Ill ZETA inland only'], PropertyDefinition.LIC_PROP)
            lic.propertySet.findAll { LicenseProperty lp -> lp.type.id in illIndicators.id }?.each { LicenseProperty lp ->
                Map<String, Object> out = [:]
                out.token           = lp.type.name
                out.scope           = lp.type.descr
                out.note            = lp.note
                out.isPublic        = lp.isPublic ? RDStore.YN_YES.value : RDStore.YN_NO.value
                out.value           = lp.refValue ? lp.refValue.value : null
                out.type            = PropertyDefinition.validTypes[lp.type.type]['en']
                out.refdataCategory = lp.type.refdataCategory
                out.paragraph       = lp.paragraph
                result << ApiToolkit.cleanUp(out, true, true)
            }
        }
        return (hasAccess ? (result ? new JSON(result) : null) : Constants.HTTP_FORBIDDEN)
    }

    //-------------------------------------- helper methods -------------------------------------------

    /**
     * Builds a row for the KBART export table, assembling the data contained in the output
     * @param sql the {@link Sql} connection
     * @param row the base database row
     * @param packageIDs the list of package identifiers
     * @param titleNS the proprietary identifier namespace of the provider
     * @param otherTitleIdentifierNamespaces other identifier namespaces apart from the core title identifier namespaces
     * @param allPriceItems the {@link de.laser.finance.PriceItem} map for the given holding
     * @return a {@link List} containing the columns for the next row of the export table
     * @see IdentifierNamespace
     */
    @Deprecated
    static List buildRow(Sql sql, GroovyRowResult row, List<GroovyRowResult> packageIDs, String titleNS, List<GroovyRowResult> otherTitleIdentifierNamespaces, Map<Long, Map<RefdataValue, GroovyRowResult>> allPriceItems) {
        SimpleDateFormat formatter = DateUtils.getSDF_yyyyMMdd()
        List<GroovyRowResult> identifiers = sql.rows('select id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = :tipp', [tipp: row['tipp_id']])
        Map<RefdataValue, GroovyRowResult> priceItems = allPriceItems.get(row['ie_id'])
        List outRow = []
        //log.debug("processing ${tipp.name}")
        //publication_title
        outRow.add(row['tipp_name'])
        GroovyRowResult printIdentifier = identifiers.find { GroovyRowResult idRow -> idRow['idns_ns'] in ['isbn', 'issn'] },
                        onlineIdentifier = identifiers.find { GroovyRowResult idRow -> idRow['idns_ns'] in ['eisbn', 'eissn'] }
        //print_identifier - namespace pISBN is proprietary for LAS:eR because no eISBN is existing and ISBN is used for eBooks as well
        if (printIdentifier)
            outRow.add(printIdentifier['id_value'])
        else outRow.add(' ')
        //online_identifier
        if (onlineIdentifier)
            outRow.add(onlineIdentifier['id_value'])
        else outRow.add(' ')
        //date_first_issue_online
        outRow.add(row.containsKey('ic_start_date') && row['ic_start_date'] ? formatter.format(row['ic_start_date']) : ' ')
        //num_first_volume_online
        outRow.add(row.containsKey('ic_start_volume') ? row['ic_start_volume'] : ' ')
        //num_first_issue_online
        outRow.add(row.containsKey('ic_start_issue') ? row['ic_start_issue'] : ' ')
        //date_last_issue_online
        outRow.add(row.containsKey('ic_end_date') && row['ic_end_date'] ? formatter.format(row['ic_end_date']) : ' ')
        //num_last_volume_online
        outRow.add(row.containsKey('ic_end_volume') ? row['ic_end_volume'] : ' ')
        //num_last_issue_online
        outRow.add(row.containsKey('ic_end_issue') ? row['ic_end_issue'] : ' ')
        //title_url
        outRow.add(row['tipp_host_platform_url'] ?: ' ')
        //first_author (no value?)
        outRow.add(row['tipp_first_author'] ?: ' ')
        //title_id (no value?)
        if(titleNS) {
            String titleId = identifiers.find { GroovyRowResult idRow -> idRow['idns_ns'] == titleNS }?.get('id_value')
            outRow.add(titleId ?: ' ')
        }
        else outRow.add(' ')
        //embargo_information
        outRow.add(row.containsKey('ic_embargo') ? row['ic_embargo'] : ' ')
        //coverage_depth
        outRow.add(row.containsKey('ic_coverage_depth') ? row['ic_coverage_depth'] : ' ')
        //notes
        outRow.add(row.containsKey('ic_coverage_note') ? row['ic_coverage_note'] : ' ')
        //publication_type
        outRow.add(row['title_type'])
        //publisher_name
        outRow.add(row['tipp_publisher_name'] ?: ' ')
        //date_monograph_published_print (no value unless BookInstance)
        outRow.add(row['tipp_date_first_in_print'] ? formatter.format(row['tipp_date_first_in_print']) : ' ')
        //date_monograph_published_online (no value unless BookInstance)
        outRow.add(row['tipp_date_first_online'] ? formatter.format(row['tipp_date_first_online']) : ' ')
        //monograph_volume (no value unless BookInstance)
        outRow.add(row['tipp_volume'] ?: ' ')
        //monograph_edition (no value unless BookInstance)
        outRow.add(row['tipp_edition_number'] ?: ' ')
        //first_editor (no value unless BookInstance)
        outRow.add(row['tipp_first_editor'] ?: ' ')
        //parent_publication_title_id (no values defined for LAS:eR, must await we:kb)
        outRow.add(' ')
        //preceding_publication_title_id (no values defined for LAS:eR, must await we:kb)
        outRow.add(' ')
        //package_name
        outRow.add(row['pkg_name'] ?: ' ')
        //package_id
        outRow.add(joinIdentifiers(packageIDs, IdentifierNamespace.PKG_ID, ','))
        //last_changed
        outRow.add(row['tipp_last_updated'] ? formatter.format(row['tipp_last_updated']) : ' ')
        //access_start_date
        outRow.add(row['access_start_date'] ? formatter.format(row['access_start_date']) : ' ')
        //access_end_date
        outRow.add(row['access_end_date'] ? formatter.format(row['access_end_date']) : ' ')
        //medium
        outRow.add(row['tipp_medium_rv_fk'] ? RefdataValue.get(row['tipp_medium_rv_fk'])?.value : ' ')
        //zdb_id
        outRow.add(joinIdentifiers(identifiers, IdentifierNamespace.ZDB, ','))
        //doi_identifier
        outRow.add(joinIdentifiers(identifiers, IdentifierNamespace.DOI, ','))
        //ezb_id
        outRow.add(joinIdentifiers(identifiers, IdentifierNamespace.EZB, ','))
        //package_isci
        outRow.add(joinIdentifiers(packageIDs, IdentifierNamespace.ISCI, ','))
        //package_isil
        outRow.add(joinIdentifiers(packageIDs, IdentifierNamespace.ISIL_PAKETSIGEL, ','))
        //package_ezb_anchor
        outRow.add(joinIdentifiers(packageIDs, IdentifierNamespace.EZB_ANCHOR, ','))
        //ill_indicator
        outRow.add(' ')
        //superseding_publication_title_id
        outRow.add(' ')
        //monograph_parent_collection_title
        outRow.add(row['tipp_series_name'] ?: '')
        //subject_area
        outRow.add(row['tipp_subject_reference'] ?: '')
        //status
        outRow.add(row['ie_status_rv_fk'] ? RefdataValue.get(row['ie_status_rv_fk'])?.value : '')
        //access_type (no values defined for LAS:eR, must await we:kb)
        if(row['tipp_access_type_rv_fk']) {
            if(RefdataValue.get(row['tipp_access_type_rv_fk']) == RDStore.TIPP_PAYMENT_FREE)
                outRow.add('F')
            else if(RefdataValue.get(row['tipp_access_type_rv_fk']) == RDStore.TIPP_PAYMENT_PAID)
                outRow.add('P')
            else outRow.add('')
        }
        else
        //oa_type
        outRow.add(row['tipp_open_access_rv_fk'] ? RefdataValue.get(row['tipp_open_access_rv_fk'])?.value : '')
        //zdb_ppn
        outRow.add(joinIdentifiers(identifiers, IdentifierNamespace.ZDB_PPN, ','))
        //ezb_anchor
        outRow.add(joinIdentifiers(identifiers,IdentifierNamespace.EZB_ANCHOR,','))
        //ezb_collection_id
        outRow.add(joinIdentifiers(identifiers,IdentifierNamespace.EZB_COLLECTION_ID,','))
        //subscription_isil
        outRow.add(joinIdentifiers(identifiers,IdentifierNamespace.ISIL_PAKETSIGEL,','))
        //subscription_isci
        outRow.add(joinIdentifiers(identifiers,IdentifierNamespace.ISCI,','))
        //listprice_eur
        outRow.add(priceItems?.get(RDStore.CURRENCY_EUR.value)?.get('pi_list_price') ?: ' ')
        //listprice_gbp
        outRow.add(priceItems?.get(RDStore.CURRENCY_GBP.value)?.get('pi_list_price') ?: ' ')
        //listprice_usd
        outRow.add(priceItems?.get(RDStore.CURRENCY_USD.value)?.get('pi_list_price') ?: ' ')
        //localprice_eur
        outRow.add(priceItems?.get(RDStore.CURRENCY_EUR.value)?.get('pi_local_price') ?: ' ')
        //localprice_gbp
        outRow.add(priceItems?.get(RDStore.CURRENCY_GBP.value)?.get('pi_local_price') ?: ' ')
        //localprice_usd
        outRow.add(priceItems?.get(RDStore.CURRENCY_USD.value)?.get('pi_local_price') ?: ' ')
        //other identifier namespaces
        otherTitleIdentifierNamespaces.each { GroovyRowResult ns ->
            outRow.add(joinIdentifiers(identifiers, ns['idns_ns'], ','))
        }
        outRow
    }

    /**
     * Returns the base title headers for the KBART table
     * @return a {@link List} of title headers; they are specified according to the KBART standard (<a href="https://groups.niso.org/higherlogic/ws/public/download/16900/RP-9-2014_KBART.pdf">see here, section 6.6</a>)
     */
    static List<String> getBaseTitleHeaders() {
        ['publication_title',
         'print_identifier',
         'online_identifier',
         'date_first_issue_online',
         'num_first_vol_online',
         'num_first_issue_online',
         'date_last_issue_online',
         'num_last_vol_online',
         'num_last_issue_online',
         'title_url',
         'first_author',
         'title_id',
         'embargo_info',
         'coverage_depth',
         'notes',
         'publication_type',
         'publisher_name',
         'date_monograph_published_print',
         'date_monograph_published_online',
         'monograph_volume',
         'monograph_edition',
         'first_editor',
         'parent_publication_title_id',
         'preceding_publication_title_id',
         'package_name',
         'package_id',
         'last_changed',
         'access_start_date',
         'access_end_date',
         'medium',
         'zdb_id',
         'doi_identifier',
         'ezb_id',
         'package_isci',
         'package_isil',
         'package_ezb_anchor',
         'ill_indicator',
         'superseding_publication_title_id',
         'monograph_parent_collection_title',
         'subject_area',
         'status',
         'access_type',
         'oa_type',
         'zdb_ppn',
         'ezb_anchor',
         'ezb_collection_id',
         'listprice_eur',
         'listprice_gbp',
         'listprice_usd',
         'localprice_eur',
         'localprice_gbp',
         'localprice_usd']
    }

    /**
     * Concatenates the given list of identifiers of a given namespace to a character-separated string enumeration
     * @param rows the list of identifier records
     * @param namespace the namespace within which the concatenating identifiers are
     * @param separator the character separating the entries
     * @return the concatenated enumeration string
     */
    static String joinIdentifiers(List<GroovyRowResult> rows, String namespace, String separator) {
        String joined = ' '
        List values = []
        rows.each { GroovyRowResult idRow ->
            if(idRow['idns_ns'] == namespace) {
                values.add(idRow['id_value'])
            }
        }
        if(values)
            joined = values.join(separator)
        joined
    }
}
