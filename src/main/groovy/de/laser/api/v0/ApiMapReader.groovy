package de.laser.api.v0


import de.laser.Org
import de.laser.Person
import de.laser.TitleInstancePackagePlatform
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
        result.option           = tipp.option?.value
        result.delayedOA        = tipp.delayedOA?.value
        result.hybridOA         = tipp.hybridOA?.value
        result.statusReason     = tipp.statusReason?.value
        result.payment          = tipp.payment?.value

        // References
        //result.additionalPlatforms  = getPlatformTippCollection(tipp.additionalPlatforms) // com.k_int.kbplus.PlatformTIPP
        result.identifiers          = ApiCollectionReader.getIdentifierCollection(tipp.ids)       // de.laser.Identifier
        result.platform             = ApiUnsecuredMapReader.getPlatformStubMap(tipp.platform) // com.k_int.kbplus.Platform
        result.title                = ApiUnsecuredMapReader.getTitleStubMap(tipp)       // de.laser.titles.TitleInstance

        if (ignoreRelation != ApiReader.IGNORE_ALL) {
            if (ignoreRelation != ApiReader.IGNORE_PACKAGE) {
                result.package = ApiUnsecuredMapReader.getPackageStubMap(tipp.pkg) // com.k_int.kbplus.Package
            }
            if (ignoreRelation != ApiReader.IGNORE_SUBSCRIPTION) {
                result.subscription = ApiStubReader.requestSubscriptionStub(tipp.sub, context) // com.k_int.kbplus.Subscription
            }
        }
        //result.derivedFrom      = ApiStubReader.resolveTippStub(tipp.derivedFrom)  // de.laser.TitleInstancePackagePlatform
        //result.masterTipp       = ApiStubReader.resolveTippStub(tipp.masterTipp)   // de.laser.TitleInstancePackagePlatform

        return ApiToolkit.cleanUp(result, true, true)
    }

    /*

    // not used ??
    def resolveLink(Link link) {
        Map<String, Object> result = [:]
        if (!link) {
            return null
        }
        result.id   = link.id

        // RefdataValues
        result.status   = link.status?.value
        result.type     = link.type?.value
        result.isSlaved = link.isSlaved?.value

        def context = null // TODO: use context
        result.fromLic  = ApiStubReader.resolveLicenseStub(link.fromLic, context) // com.k_int.kbplus.License
        result.toLic    = ApiStubReader.resolveLicenseStub(link.toLic, context) // com.k_int.kbplus.License

        return ApiToolkit.cleanUp(result, true, true)
    }

    // not used ??
    def resolveLinks(list) {
        def result = []
        if(list) {
            list.each { it -> // com.k_int.kbplus.Link
                result << resolveLink(it)
            }
        }
        result
    }

    */
}
