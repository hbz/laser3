package de.laser

import com.k_int.kbplus.License
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class LicenseService {

    AccessService accessService
    ContextService contextService

    List<License> getMyLicenses_readRights(){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getLicensesConsortiaQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesLocalLicenseQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result
    }

    List<License> getMyLicenses_writeRights(){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getLicensesConsortiaQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesLocalLicenseQuery()
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result.sort {it.dropdownNamingConvention()}
    }


    //Konsortialverträge
    private List getLicensesConsortiaQuery() {
        String base_qry = """from License as l where (
                    exists ( select o from l.orgRelations as o where ( 
                    ( o.roleType = :roleTypeC 
                        AND o.org = :lic_org 
                        AND l.instanceOf is null
                        AND NOT exists (
                        select o2 from l.orgRelations as o2 where o2.roleType = :roleTypeL
                    )
                )
            )))"""

        return [ base_qry, [roleTypeC: RDStore.OR_LICENSING_CONSORTIUM, roleTypeL: RDStore.OR_LICENSEE_CONS, lic_org: contextService.org] ]
    }

    //Teilnehmerverträge
    private List getLicensesConsortialLicenseQuery() {
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType  AND o.org = :lic_org ) ) 
            )"""

        return [ base_qry, [roleType: RDStore.OR_LICENSEE_CONS, lic_org: contextService.org] ]
    }

    //Lokalverträge
    private List getLicensesLocalLicenseQuery() {
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""

        return [ base_qry, [roleType: RDStore.OR_LICENSEE, lic_org: contextService.org] ]
    }

    List getVisibleOrgRelations(License license) {
        List visibleOrgRelations = []
        license?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

}
