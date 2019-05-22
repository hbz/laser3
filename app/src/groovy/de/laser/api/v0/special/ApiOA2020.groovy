package de.laser.api.v0.special

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import de.laser.api.v0.ApiReaderHelper
import de.laser.helper.Constants
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiOA2020 {

    /**
     * @return [] | HTTP_FORBIDDEN
     */
    static getAllOrgs() {
        def result = []

        // if (requestingOrghasNoAccessDueSpecialFlag?) { return Constants.HTTP_FORBIDDEN }

        List<Org> orgs = OrgSettings.executeQuery(
                //"select o from OrgSettings os join os.org o where o.status != :deleted and os.key = :key and os.rdValue = :rdValue",
                "select o from OrgSettings os join os.org o where os.key = :key and os.rdValue = :rdValue",
                [
                   // deleted: RefdataValue.getByValueAndCategory('Deleted', 'OrgStatus'),
                    key: OrgSettings.KEYS.STATISTICS_SERVER_ACCESS,
                    rdValue: RefdataValue.getByValueAndCategory('Yes', 'YN')
                ]
        )
        orgs.each{ o ->
            result << ApiReaderHelper.resolveOrganisationStub(o, o)
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
