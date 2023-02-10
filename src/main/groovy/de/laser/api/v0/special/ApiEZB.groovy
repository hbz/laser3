package de.laser.api.v0.special

import de.laser.*
import de.laser.api.v0.*
import de.laser.storage.Constants
import de.laser.utils.DateUtils
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import grails.converters.JSON
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Slf4j

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
                            roles: [RDStore.OR_SUBSCRIPTION_CONSORTIA, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER]
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
                "select o from OrgSetting os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                key    : OrgSetting.KEYS.EZB_SERVER_ACCESS,
                rdValue: RDStore.YN_YES,
                deleted: RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS)
        ])
        //List<Org> orgs = Org.executeQuery('select id.org from Identifier id where id.ns.ns = :ezb', [ezb: IdentifierNamespace.EZB_ORG_ID])

        orgs
    }

    /**
     * Lists the details of all institutions which have granted access to the EZB endpoint.
     * Checks implicit EZB_SERVER_ACCESS, i.e. if the requested institution is among those which gave permission to EZB
     * @return a {@link JSON} containing a list of the organisation stubs
     */
    static JSON getAllOrgs() {
        Collection<Object> result = []

        List<Org> orgs = getAccessibleOrgs()
        orgs.each { o ->
            result << ApiUnsecuredMapReader.getOrganisationStubMap(o)
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
            Map<String, Object> queryParams = [owner: org, roles: [RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIPTION_CONSORTIA], current: RDStore.SUBSCRIPTION_CURRENT, otherAccessible: [RDStore.SUBSCRIPTION_EXPIRED, RDStore.SUBSCRIPTION_TEST_ACCESS]] //set to OR_SUBSCRIBER_CONS
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
            Platform plat
            List<Platform> platCheck = Platform.executeQuery('select pkg.nominalPlatform from SubscriptionPackage sp join sp.pkg pkg where sp.subscription = :sub', [sub: sub])
            if(!platCheck)
                platCheck = Platform.executeQuery('select tipp.platform from IssueEntitlement ie join ie.tipp tipp where ie.subscription = :sub', [sub: sub], [max: 1])
            if(platCheck)
                plat = platCheck[0]
            String titleNS = null
            if(plat) {
                titleNS = plat.titleNamespace
            }
            else {
                log.error("No platform available! Continue without proprietary namespace!")
            }
            Sql sql = GlobalService.obtainSqlConnection()
            String dateFilter = ""
            Map<String, Object> genericFilter = [subId: sub.id]
            if(changedFrom) {
                dateFilter = " and ie_last_updated >= :changedFrom "
                genericFilter.changedFrom = new Timestamp(changedFrom.getTime())
            }
            log.debug("Begin generateTitleExportKBARTSQL")
            sql.withTransaction {
                List<String> titleHeaders = getBaseTitleHeaders()
                List<GroovyRowResult> entitlementRows = sql.rows("select ie_id, ie_name, ie_sortname, ie_access_start_date, ie_access_end_date, ie_medium_rv_fk, ie_status_rv_fk, " +
                        "tipp_id, tipp_pkg_fk, tipp_host_platform_url, tipp_date_first_in_print, tipp_date_first_online, tipp_first_author, tipp_first_editor, " +
                        "tipp_publisher_name, tipp_volume, tipp_edition_number, tipp_last_updated, tipp_series_name, tipp_subject_reference, tipp_access_type_rv_fk, tipp_open_access_rv_fk, " +
                        "case tipp_title_type when 'Journal' then 'serial' when 'Book' then 'monograph' when 'Database' then 'database' else 'other' end as title_type, " +
                        "case ie_access_start_date when null then tipp_access_start_date else ie_access_start_date end as access_start_date, " +
                        "case ie_access_end_date when null then tipp_access_end_date else ie_access_end_date end as access_end_date " +
                        "from issue_entitlement join title_instance_package_platform on ie_tipp_fk = tipp_id " +
                        "where ie_subscription_fk = :subId and ie_status_rv_fk != :removed ${dateFilter} order by ie_sortname, ie_name", genericFilter+[removed: RDStore.TIPP_STATUS_REMOVED.id])
                List<GroovyRowResult> packageData = sql.rows('select pkg_id, pkg_name from subscription_package join package on sp_pkg_fk = pkg_id where sp_sub_fk = :subId', [subId: sub.id])
                List<GroovyRowResult> packageIDs = sql.rows('select id_pkg_fk, id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id join subscription_package on id_pkg_fk = sp_pkg_fk where sp_sub_fk = :subId', [subId: sub.id])
                //log.debug("select id_pkg_fk, id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id join subscription_package on id_pkg_fk = sp_pkg_fk where sp_sub_fk = ${sub.id}")
                List<GroovyRowResult> otherTitleIdentifierNamespaces = sql.rows('select distinct(idns_ns) from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id join issue_entitlement on tipp_id = ie_tipp_fk where ie_subscription_fk = :subId and lower(idns_ns) != any(:coreTitleNS)', [subId: sub.id, coreTitleNS: sql.connection.createArrayOf('varchar', IdentifierNamespace.CORE_TITLE_NS as Object[])])
                //log.debug("select distinct(idns_ns) from identifier_namespace join identifier on id_ns_fk = idns_id join title_instance_package_platform on id_tipp_fk = tipp_id join issue_entitlement on tipp_id = ie_tipp_fk where ie_subscription_fk = :subId and lower(idns_ns) != any(${IdentifierNamespace.CORE_TITLE_NS.toListString()})")
                List<GroovyRowResult> priceItemRows = sql.rows('select pi_id, pi_ie_fk, (select rdv_value from refdata_value where rdv_id = pi_list_currency_rv_fk) as pi_list_currency, pi_list_price, (select rdv_value from refdata_value where rdv_id = pi_local_currency_rv_fk) as pi_local_currency, pi_local_price from price_item join issue_entitlement on pi_ie_fk = ie_id where ie_subscription_fk = :subId'+dateFilter, genericFilter)
                Map<Long, Map<String, GroovyRowResult>> priceItems = ExportService.preprocessPriceItemRows(priceItemRows, 'pi_ie_fk')
                titleHeaders.addAll(otherTitleIdentifierNamespaces.collect { GroovyRowResult ns -> "${ns['idns_ns']}_identifier"})
                export = [titleRow:titleHeaders,columnData:[]]
                long start = System.currentTimeMillis()
                entitlementRows.eachWithIndex { GroovyRowResult row, int i ->
                    log.debug("processing row ${i} at ${System.currentTimeMillis()-start} msecs")
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
                //export.columnData = export.columnData.take(1000) //for debug purposes
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
    static List buildRow(Sql sql, GroovyRowResult row, List<GroovyRowResult> packageIDs, String titleNS, List<GroovyRowResult> otherTitleIdentifierNamespaces, Map<Long, Map<RefdataValue, GroovyRowResult>> allPriceItems) {
        SimpleDateFormat formatter = DateUtils.getSDF_yyyyMMdd()
        List<GroovyRowResult> identifiers = sql.rows('select id_value, idns_ns from identifier join identifier_namespace on id_ns_fk = idns_id where id_tipp_fk = :tipp', [tipp: row['tipp_id']])
        Map<RefdataValue, GroovyRowResult> priceItems = allPriceItems.get(row['ie_id'])
        List outRow = []
        //log.debug("processing ${tipp.name}")
        //publication_title
        outRow.add(row['ie_name'])
        GroovyRowResult printIdentifier = identifiers.find { GroovyRowResult idRow -> idRow['idns_ns'] in ['pisbn', 'issn'] },
                        onlineIdentifier = identifiers.find { GroovyRowResult idRow -> idRow['idns_ns'] in ['isbn', 'eissn'] }
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
        outRow.add(row['ie_medium_rv_fk'] ? RefdataValue.get(row['ie_medium_rv_fk'])?.value : ' ')
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
        outRow.add(row['tipp_access_type_rv_fk'] ? RefdataValue.get(row['tipp_access_type_rv_fk'])?.value : '')
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
         'subscription_isil',
         'subscription_isci',
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
