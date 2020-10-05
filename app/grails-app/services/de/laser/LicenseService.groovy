package de.laser

import com.k_int.kbplus.License
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class LicenseService {

    AccessService accessService
    ContextService contextService

    List<License> getMyLicenses_readRights(Map params){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getLicensesConsortiaQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result
    }

    List<License> getMyLicenses_writeRights(Map params){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(accessService.checkPerm("ORG_CONSORTIUM")) {
            tmpQ = getLicensesConsortiaQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result.sort {it.dropdownNamingConvention()}
    }


    //Konsortialverträge
    private List getLicensesConsortiaQuery(Map params) {
        Map qry_params = [roleTypeC: RDStore.OR_LICENSING_CONSORTIUM, roleTypeL: RDStore.OR_LICENSEE_CONS, lic_org: contextService.org]
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

            if (params.status) {

                if(params.status instanceof List){
                    base_qry += " and l.status.id in (:status) "
                    qry_params.put('status', params.status.collect { it instanceof Long ? it : Long.parseLong(it) })
                }else {
                    base_qry += " and l.status.id = :status "
                    qry_params.put('status', (params.status as Long))
                }
            }

        return [base_qry, qry_params]
    }

    //Teilnehmerverträge
    private List getLicensesConsortialLicenseQuery(Map params) {
        Map qry_params = [roleType: RDStore.OR_LICENSEE_CONS, lic_org: contextService.org]
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType  AND o.org = :lic_org ) ) 
            )"""

        if (params.status) {

            if(params.status instanceof List){
                base_qry += " and l.status.id in (:status) "
                qry_params.put('status', params.status.collect { it instanceof Long ? it : Long.parseLong(it) })
            }else {
                base_qry += " and l.status.id = :status "
                qry_params.put('status', (params.status as Long))
            }
        }

        return [ base_qry, qry_params ]
    }

    //Lokalverträge
    private List getLicensesLocalLicenseQuery(Map params) {
        Map qry_params = [roleType: RDStore.OR_LICENSEE, lic_org: contextService.org]
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""

        if (params.status) {

            if(params.status instanceof List){
                base_qry += " and l.status.id in (:status) "
                qry_params.put('status', params.status.collect { it instanceof Long ? it : Long.parseLong(it) })
            }else {
                base_qry += " and l.status.id = :status "
                qry_params.put('status', (params.status as Long))
            }
        }

        return [ base_qry, qry_params ]
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
