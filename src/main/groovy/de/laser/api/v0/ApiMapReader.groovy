package de.laser.api.v0


import de.laser.Org
import de.laser.Person
import de.laser.RefdataValue
import de.laser.TitleInstancePackagePlatform
import groovy.sql.GroovyRowResult
import groovy.util.logging.Slf4j

@Slf4j
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
            result.addresses    = ApiCollectionReader.getAddressCollection(prs.addresses, allowedAddressTypes) // de.laser.Address
            result.properties   = ApiCollectionReader.getPrivatePropertyCollection(prs.propertySet, context) // com.k_int.kbplus.PersonPrivateProperty
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

        result.globalUID        = tipp.globalUID
        result.hostPlatformURL  = tipp.hostPlatformURL
        result.gokbId           = tipp.gokbId
        result.lastUpdated      = ApiToolkit.formatInternalDate(tipp.lastUpdated)
        //result.rectype          = tipp.rectype    // legacy; not needed ?

        // RefdataValues
        result.status           = tipp.status?.value

        // References
        //result.additionalPlatforms  = getPlatformTippCollection(tipp.additionalPlatforms) // com.k_int.kbplus.PlatformTIPP
        result.identifiers          = ApiCollectionReader.getIdentifierCollection(tipp.ids)       // de.laser.Identifier
        result.platform             = ApiUnsecuredMapReader.getPlatformStubMap(tipp.platform) // com.k_int.kbplus.Platform

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiUnsecuredMapReader.getPackageStubMap(tipp.pkg) // com.k_int.kbplus.Package
            }
        }
        //result.derivedFrom      = ApiStubReader.resolveTippStub(tipp.derivedFrom)  // de.laser.TitleInstancePackagePlatform
        //result.masterTipp       = ApiStubReader.resolveTippStub(tipp.masterTipp)   // de.laser.TitleInstancePackagePlatform

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

        result.globalUID        = row['tipp_guid']
        result.hostPlatformURL  = row['tipp_host_platform_url']
        result.gokbId           = row['tipp_gokb_id']
        result.lastUpdated      = ApiToolkit.formatInternalDate(row['tipp_last_updated'])

        // RefdataValues
        result.status           = row['tipp_status']

        // References
        List<Map<String, Object>> identifiers = []
        row['ids'].each { idRow ->
            identifiers << [namespace: idRow['idns_ns'], value: idRow['id_value']]
        }
        result.identifiers          = identifiers       // de.laser.Identifier
        result.platform             = ApiUnsecuredMapReader.getPlatformStubMapWithSQL(row['platform']) // com.k_int.kbplus.Platform

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiUnsecuredMapReader.getPackageStubMapWithSQL(row['pkg']) // com.k_int.kbplus.Package
            }
        }

        return ApiToolkit.cleanUp(result, true, true)
    }
}
