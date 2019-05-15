package de.laser.api.v0.special

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import de.laser.api.v0.ApiReaderHelper
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiStatistic {

    /**
     * @return [] | HTTP_FORBIDDEN
     */
    static getAllOrgs() {
        def result = []

        // if (requestingOrghasNoAccess) { return Constants.HTTP_FORBIDDEN }

        List<Org> orgs = OrgSettings.executeQuery(
                //"select o from OrgSettings os join os.org o where o.status != :deleted and os.key = :key and os.rdValue = :rdValue",
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue",
                [
                  //  deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus'),
                    key: OrgSettings.KEYS.STATISTICS_SERVER_ACCESS,
                    rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN')
                ]
        )
        orgs.each{ o ->
            result << ApiReaderHelper.resolveOrganisationStub(o, o)
        }

        return (result ? new JSON(result) : null)
    }

    static getAllPackages() {
        def result = []

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue", [
                key: OrgSettings.KEYS.STATISTICS_SERVER_ACCESS,
                rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN') ] )
        println orgs
        List<Package> packages = com.k_int.kbplus.Package.executeQuery(
                "select sp.pkg from SubscriptionPackage sp " +
                        "join sp.subscription s join s.orgRelations ogr join ogr.org o " +
                        "where o in :orgs ", [orgs: orgs]
        )
        println packages
        packages.each{ p ->
            result << ApiReaderHelper.resolvePackageStub(p, null) // ? null
        }

        return (result ? new JSON(result) : null)
    }

    /**
     * @return []
     */
    static getDummy() {
        def result = ['dummy']
        result
    }

}
