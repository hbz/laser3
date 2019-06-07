package de.laser.api.v0.special

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import de.laser.api.v0.ApiReaderHelper
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiOA2020 {

    static private List<Org> getAccessibleOrgs() {

        List<Org> orgs = OrgSettings.executeQuery(
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue " +
                        "and (o.status is null or o.status != :deleted)", [
                key    : OrgSettings.KEYS.OA2020_SERVER_ACCESS,
                rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN'),
                deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus')
        ])

        orgs
    }

    /**
     * @return JSON
     */
    static JSON getAllOrgs() {
        Collection<Object> result = []

        // if (requestingOrghasNoAccessDueSpecialFlag?) { return Constants.HTTP_FORBIDDEN }

        List<Org> orgs = getAccessibleOrgs()

        orgs.each{ o ->
            result << ApiReaderHelper.retrieveOrganisationStubMap(o, o)
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
