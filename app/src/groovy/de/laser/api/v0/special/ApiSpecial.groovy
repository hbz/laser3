package de.laser.api.v0.special

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgSettings
import com.k_int.kbplus.RefdataValue
import de.laser.api.v0.ApiReader
import de.laser.api.v0.ApiReaderHelper
import de.laser.helper.Constants
import groovy.util.logging.Log4j
import org.springframework.http.HttpStatus

@Log4j
class ApiSpecial {

    /**
     * @return []
     */
    static getAllOA2020Orgs() {
        def result = []

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

        println result
        result
        //result ?: HttpStatus.NOT_FOUND.value()
    }

    /**
     * @return []
     */
    static getAllStatisticOrgs() {
        def result = []

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

        println result
        result
        //result ?: HttpStatus.NOT_FOUND.value()
    }

    /**
     * @return []
     */
    static getDummy() {
        def result = ['dummy']
        result
    }

}
