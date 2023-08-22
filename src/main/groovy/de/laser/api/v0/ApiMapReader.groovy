package de.laser.api.v0


import de.laser.Org
import de.laser.Person
import de.laser.TitleInstancePackagePlatform
import groovy.sql.GroovyRowResult

class ApiMapReader {

    /**
     * Assembles the given person details into a {@link Map}. The schema may be viewed in schemas.gsp
     * @param prs the {@link Person} subject of output
     * @param allowedContactTypes the types of contacts which can be returned
     * @param allowedAddressTypes the types of addresses which can be returned
     * @param context the requesting institution ({@link Org}) whose perspective is going to be taken during checks
     * @return a {@link Map} reflecting the person details for API output
     */
    static Map<String, Object> getPersonMap(Person prs, allowedContactTypes, allowedAddressTypes, Org context) {
        Map<String, Object> result = [:]

        if (prs) {
            result.globalUID       = prs.globalUID
            result.firstName       = prs.first_name
            result.middleName      = prs.middle_name
            result.lastName        = prs.last_name
            result.title           = prs.title
            result.lastUpdated     = ApiToolkit.formatInternalDate(prs._getCalculatedLastUpdated())

            // RefdataValues
            result.gender          = prs.gender?.value
            result.isPublic        = prs.isPublic ? 'Yes' : 'No'
            result.contactType     = prs.contactType?.value

            // References
            result.contacts     = ApiCollectionReader.getContactCollection(prs.contacts, allowedContactTypes) // de.laser.Contact
            //result.addresses    = [:]//ApiCollectionReader.getAddressCollection(prs.addresses, allowedAddressTypes) // de.laser.Address - obsolete for 3.2
            result.properties   = ApiCollectionReader.getPrivatePropertyCollection(prs.propertySet, context) // de.laser.PersonPrivateProperty
        }
        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Assembles the given title details into a {@link Map}. The schema may be viewed in schemas.gsp.
     * Access rights due wrapping object. Some relations may be blocked
     * @param tipp the {@link TitleInstancePackagePlatform} subject of output
     * @param ignoreRelation which relations should be blocked
     * @param context the institution ({@link Org}) requesting
     * @return Map<String, Object>
     */
    static Map<String, Object> getTippMap(TitleInstancePackagePlatform tipp, def ignoreRelation, Org context) {
        Map<String, Object> result = [:]

        if (! tipp) {
            return null
        }

        result.globalUID         = tipp.globalUID
        result.gokbId            = tipp.gokbId
        result.name             = tipp.name
        result.medium           = tipp.medium?.value
        result.status           = tipp.status?.value
        result.coverages        = ApiCollectionReader.getCoverageCollection(tipp.coverages) //de.laser.TIPPCoverage
        result.priceItems       = ApiCollectionReader.getPriceItemCollection(tipp.priceItems) //de.laser.finance.PriceItem with pi.tipp != null
        result.altnames          = ApiCollectionReader.getAlternativeNameCollection(tipp.altnames)
        result.firstAuthor       = tipp.firstAuthor
        result.firstEditor       = tipp.firstEditor
        result.editionStatement  = tipp.editionStatement
        result.publisherName     = tipp.publisherName
        result.hostPlatformURL   = tipp.hostPlatformURL
        result.dateFirstInPrint  = tipp.dateFirstInPrint ? ApiToolkit.formatInternalDate(tipp.dateFirstInPrint) : null
        result.dateFirstOnline   = tipp.dateFirstOnline ? ApiToolkit.formatInternalDate(tipp.dateFirstOnline) : null
        result.seriesName        = tipp.seriesName
        result.subjectReference  = tipp.subjectReference
        result.titleType         = tipp.titleType
        result.volume            = tipp.volume
        result.lastUpdated       = ApiToolkit.formatInternalDate(tipp.lastUpdated)

        // RefdataValues
        result.accessType        = tipp.accessType?.value
        result.openAccess        = tipp.openAccess?.value

        // References
        result.identifiers          = ApiCollectionReader.getIdentifierCollection(tipp.ids)       // de.laser.Identifier
        //result.platform             = ApiUnsecuredMapReader.getPlatformStubMap(tipp.platform) // de.laser.Platform
        result.ddcs                 = ApiCollectionReader.getDeweyDecimalCollection(tipp.ddcs)  //de.laser.DeweyDecimalClassification
        result.languages            = ApiCollectionReader.getLanguageCollection(tipp.languages) //de.laser.Language
        //unsure construction; remains open u.f.n.
        //result.titleHistory         = ApiCollectionReader.getTitleHistoryCollection(tipp.historyEvents) //de.laser.titles.TitleHistoryEvent

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiUnsecuredMapReader.getPackageStubMap(tipp.pkg) // de.laser.Package
            }
            result.providers        = ApiCollectionReader.getOrgLinkCollection(tipp.orgs, ApiReader.IGNORE_TIPP, context) //de.laser.OrgRole
        }
        if (!(ignoreRelation in [ApiReader.IGNORE_SUBSCRIPTION, ApiReader.IGNORE_SUBSCRIPTION_AND_PACKAGE])) {
            //list here every property which may differ on entitlement level (= GlobalSourceSyncService's controlled properties, see getTippDiff() for the properties to be excluded here)
            result.accessStartDate  = tipp.accessStartDate ? ApiToolkit.formatInternalDate(tipp.accessStartDate) : null
            result.accessEndDate    = tipp.accessEndDate ? ApiToolkit.formatInternalDate(tipp.accessEndDate) : null
        }


        return ApiToolkit.cleanUp(result, true, true)
    }

    /**
     * Assembles the given title details into a {@link Map}. The schema may be viewed in schemas.gsp.
     * Access rights due wrapping object. Some relations may be blocked
     * @param tipp the {@link TitleInstancePackagePlatform} subject of output
     * @param ignoreRelation which relations should be blocked
     * @param context the institution ({@link Org}) requesting
     * @return Map<String, Object>
     */
    static Map<String, Object> getTippMapWithSQL(GroovyRowResult row, def ignoreRelation, Org context) {
        Map<String, Object> result = [:]

        if (! row) {
            return null
        }

        result.globalUID         = row['tipp_guid']
        result.gokbId            = row['tipp_gokb_id']
        result.name              = row['tipp_name']
        result.altnames          = row['altnames'].collect { GroovyRowResult altNameRow -> altNameRow['altname_name'] }
        result.firstAuthor       = row['tipp_first_author']
        result.firstEditor       = row['tipp_first_editor']
        result.editionStatement  = row['tipp_edition_number']
        result.publisherName     = row['tipp_publisher_name']
        result.hostPlatformURL   = row['tipp_host_platform_url']
        result.dateFirstInPrint  = row['tipp_date_first_in_print'] ? ApiToolkit.formatInternalDate(row['tipp_date_first_in_print']) : null
        result.dateFirstOnline   = row['tipp_date_first_online'] ? ApiToolkit.formatInternalDate(row['tipp_date_first_online']) : null
        result.imprint           = row['tipp_imprint']
        result.seriesName        = row['tipp_series_name']
        result.subjectReference  = row['tipp_subject_reference']
        result.titleType         = row['title_type']
        result.volume            = row['tipp_volume']
        result.lastUpdated      = ApiToolkit.formatInternalDate(row['tipp_last_updated'])

        // RefdataValues
        result.status           = row['tipp_status']
        result.accessType       = row['tipp_access_type']
        result.openAccess       = row['tipp_open_access']
        /*
        List<Map<String, Object>> ddcs = [], languages = []
        row['ddcs'].each { GroovyRowResult ddcRow ->
            ddcs << [value: ddcRow['rdv_value'], value_de: ddcRow['rdv_value_de'], value_en: ddcRow['rdv_value_en']]
        }
        row['languages'].each { GroovyRowResult langRow ->
            languages << [value: langRow['rdv_value'], value_de: langRow['rdv_value_de'], value_en: langRow['rdv_value_en']]
        }
        */
        result.ddcs             = row['ddcs']
        result.languages        = row['languages']
        result.medium           = row['tipp_medium']

        // References
        result.identifiers          = row['ids']       // de.laser.Identifier
        //result.platform             = ApiUnsecuredMapReader.getPlatformStubMapWithSQL(row['platform']) // de.laser.Platform
        result.publishers           = row['publishers']

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiUnsecuredMapReader.getPackageStubMapWithSQL(row['pkg']) // de.laser.Package
            }
        }

        return ApiToolkit.cleanUp(result, true, true)
    }
}
