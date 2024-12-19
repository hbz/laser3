package de.laser

import de.laser.helper.Params
import de.laser.interfaces.CalculatedType
import de.laser.storage.RDStore
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.apache.http.HttpStatus

/**
 * This service handles license specific matters
 * @see License
 */
@Transactional
class LicenseService {

    ContextService contextService
    GenericOIDService genericOIDService

    /**
     * Gets a (filtered) list of licenses to which the context institution has reading rights
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List<License> getMyLicenses_readRights(Map params){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
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

    /**
     * Gets a (filtered) list of licenses to which the context institution has writing rights
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List<License> getMyLicenses_writeRights(Map params){
        List<License> result = []
        List tmpQ // [String, Map<String, Object>]

        if(contextService.getOrg().isCustomerType_Consortium() || contextService.getOrg().isCustomerType_Support()) {
            tmpQ = getLicensesConsortiaQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

        } else {
            tmpQ = getLicensesConsortialLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))

            tmpQ = getLicensesLocalLicenseQuery(params)
            result.addAll(License.executeQuery("select l " + tmpQ[0], tmpQ[1]))
        }
        result.sort {it.dropdownNamingConvention()}
    }

    /**
     * Retrieves consortial parent licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent licenses matching the given filter
     */
    List getLicensesConsortiaQuery(Map params) {
        Map qry_params = [roleTypeC: RDStore.OR_LICENSING_CONSORTIUM, roleTypeL: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
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
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }

        return [base_qry, qry_params]
    }

    /**
     * Retrieves consortial member licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of consortial parent licenses matching the given filter
     */
    List getLicensesConsortialLicenseQuery(Map params) {
        Map qry_params = [roleType: RDStore.OR_LICENSEE_CONS, lic_org: contextService.getOrg()]
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType  AND o.org = :lic_org ) ) 
            )"""

        if (params.status) {
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }

        return [ base_qry, qry_params ]
    }

    /**
     * Retrieves local licenses matching the given filter
     * @param params the filter parameter map
     * @return a list of licenses matching the given filter
     */
    List getLicensesLocalLicenseQuery(Map params) {
        Map qry_params = [roleType: RDStore.OR_LICENSEE, lic_org: contextService.getOrg()]
        String base_qry = """from License as l where (
                exists ( select o from l.orgRelations as o where ( o.roleType = :roleType AND o.org = :lic_org ) ) 
            )"""

        if (params.status) {
            base_qry += " and l.status.id in (:status) "
            qry_params.put('status', Params.getLongList(params, 'status'))
        }

        return [ base_qry, qry_params ]
    }

    /**
     * Retrieves all visible organisational relationships for the given license, i.e. licensors, providers, agencies, etc.
     * @param license the license to retrieve the relations from
     * @return a sorted list of visible relations
     */
    List getVisibleOrgRelations(License license) {
        List visibleOrgRelations = []
        license?.orgRelations?.each { or ->
            if (!(or.org?.id == contextService.getOrg().id) && !(or.roleType.value in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS])) {
                visibleOrgRelations << or
            }
        }
        visibleOrgRelations.sort { it.org?.name?.toLowerCase() }
    }

    /**
     * Retrieves all visible vendor links for the given license
     * @param license the license to retrieve the relations from
     * @return a sorted set of visible relations
     */
    SortedSet<ProviderRole> getVisibleProviders(License license) {
        SortedSet<ProviderRole> visibleProviderRelations = new TreeSet<ProviderRole>()
        visibleProviderRelations.addAll(ProviderRole.executeQuery('select pr from ProviderRole pr join pr.provider p where pr.license = :license order by p.sortname', [license: license]))
        visibleProviderRelations
    }

    /**
     * Retrieves all visible vendor links for the given license
     * @param license the license to retrieve the relations from
     * @return a sorted set of visible relations
     */
    SortedSet<VendorRole> getVisibleVendors(License license) {
        SortedSet<VendorRole> visibleVendorRelations = new TreeSet<VendorRole>()
        visibleVendorRelations.addAll(VendorRole.executeQuery('select vr from VendorRole vr join vr.vendor v where vr.license = :license order by v.sortname', [license: license]))
        visibleVendorRelations
    }

    Map<String, Object> getCopyResultGenerics(GrailsParameterMap params) {
        Map<String, Object> result             = [:]
        result.user            = contextService.getUser()
        result.institution     = contextService.getOrg()
        result.contextOrg      = result.institution

        if (params.sourceObjectId == "null") params.remove("sourceObjectId")
        result.sourceObjectId = params.sourceObjectId ?: params.id
        result.sourceObject = genericOIDService.resolveOID(params.sourceObjectId)

        if (params.targetObjectId == "null") params.remove("targetObjectId")
        if (params.targetObjectId) {
            result.targetObjectId = params.targetObjectId
            result.targetObject = genericOIDService.resolveOID(params.targetObjectId)
        }

        if(params.containsKey('copyMyElements'))
            result.editable = contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_INST_PRO)
        else
            result.editable = result.sourceObject.isEditableBy(result.user)

        result.isConsortialObjects = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_CONSORTIAL) ?: false
        result.transferIntoMember = (result.sourceObject?._getCalculatedType() == CalculatedType.TYPE_LOCAL && result.targetObject?._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION)

        result.allObjects_readRights = getMyLicenses_readRights([status: RDStore.LICENSE_CURRENT.id])
        result.allObjects_writeRights = getMyLicenses_writeRights([status: RDStore.LICENSE_CURRENT.id])

        List<String> licTypSubscriberVisible = [CalculatedType.TYPE_CONSORTIAL,
                                                CalculatedType.TYPE_ADMINISTRATIVE]
        result.isSubscriberVisible = result.sourceObject && result.targetObject &&
                licTypSubscriberVisible.contains(result.sourceObject._getCalculatedType()) &&
                licTypSubscriberVisible.contains(result.targetObject._getCalculatedType())
        result
    }

}
