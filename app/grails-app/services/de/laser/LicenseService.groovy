package de.laser

import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.License
import com.k_int.kbplus.Subscription
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class LicenseService {

    AccessService accessService
    ContextService contextService

    List getMyLicenses_readRights(){
        List result = []
        List tmpQ

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

    List getMyLicenses_writeRights(){
        List result = []
        List tmpQ

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
        String base_qry
        Map qry_params

        RefdataValue licensee_cons_role      = RDStore.OR_LICENSEE_CONS
        RefdataValue lic_cons_role           = RDStore.OR_LICENSING_CONSORTIUM


        base_qry = """from License as l where (
                    exists ( select o from l.orgLinks as o where ( 
                    ( o.roleType = :roleTypeC 
                        AND o.org = :lic_org 
                        AND l.instanceOf is null
                        AND NOT exists (
                        select o2 from l.orgLinks as o2 where o2.roleType = :roleTypeL
                    )
                )
            )))"""
        qry_params = [roleTypeC: lic_cons_role, roleTypeL: licensee_cons_role, lic_org: contextService.org]

        return [base_qry, qry_params]

    }

    //Teilnehmerverträge
    private List getLicensesConsortialLicenseQuery() {
        String base_qry
        Map qry_params
        RefdataValue licensee_cons_role      = RDStore.OR_LICENSEE_CONS

        base_qry = """from License as l where (
                exists ( select o from l.orgLinks as o where ( o.roleType = :roleType  AND o.org = :lic_org ) ) 
            )"""
        qry_params = [roleType: licensee_cons_role, lic_org: contextService.org]

        return [base_qry, qry_params]
    }

    //Lokalverträge
    private List getLicensesLocalLicenseQuery() {
        String base_qry
        Map qry_params

        RefdataValue licensee_role           = RDStore.OR_LICENSEE

        base_qry = """from License as l where (
                exists ( select o from l.orgLinks as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""
        qry_params = [roleType: licensee_role, lic_org: contextService.org]

        return [base_qry, qry_params]

    }

    List getVisibleOrgRelations(License license) {
        List visibleOrgRelations = []
        license?.orgLinks?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name.toLowerCase() }
    }

}
