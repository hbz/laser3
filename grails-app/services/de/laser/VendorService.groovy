package de.laser

import de.laser.auth.User
import de.laser.remote.ApiSource
import de.laser.storage.BeanStore
import de.laser.storage.RDStore
import de.laser.utils.SwissKnife
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class VendorService {

    ContextService contextService
    GokbService gokbService

    /**
     * Gets the contact persons; optionally, a function type may be given as filter. Moreover, the request may be limited to public contacts only
     * @param vendor the {@link Vendor} for which the contacts should be retrieved
     * @param onlyPublic retrieve only public contacts?
     * @param functionType the function type of the contacts to be requested
     * @param exWekb should only contacts being retrieved which come from the provider itself (i.e. from we:kb)?
     * @return a {@link List} of {@link Person}s matching to the function type
     */
    List<Person> getContactPersonsByFunctionType(Vendor vendor, Org contextOrg, boolean onlyPublic, RefdataValue functionType = null, boolean exWekb = false) {
        Map<String, Object> queryParams = [vendor: vendor]
        String functionTypeFilter = ''
        if(functionType) {
            functionTypeFilter = 'and pr.functionType = :functionType'
            queryParams.functionType = functionType
        }
        if (onlyPublic) {
            if(exWekb) {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor '+functionTypeFilter+' and p.tenant = null',
                        queryParams
                )
            }
            else {
                Person.executeQuery(
                        'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor and p.isPublic = true and p.tenant != null '+functionTypeFilter,
                        queryParams
                )
            }
        }
        else {
            queryParams.ctx = contextOrg
            Person.executeQuery(
                    'select distinct p from Person as p inner join p.roleLinks pr where pr.vendor = :vendor ' + functionTypeFilter +
                            ' and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )',
                    queryParams
            )
        }
    }

    /**
     * Gets a (filtered) map of provider records from the we:kb
     * @param params the request parameters
     * @param result a result generics map, containing also configuration params for the request
     * @return a {@link Map} of structure [providerUUID: providerRecord] containing the request results
     */
    Map<String, Map> getWekbVendorRecords(GrailsParameterMap params, Map result) {
        Map<String, Map> records = [:], queryParams = [componentType: 'Vendor']

        if(params.containsKey('orgNameContains'))
            queryParams.q = params.orgNameContains

        if(params.containsKey('curatoryGroup'))
            queryParams.curatoryGroupExact = params.curatoryGroup.replaceAll('&','ampersand').replaceAll('\\+','%2B').replaceAll(' ','%20')


        if(params.containsKey('venStatus')) {
            queryParams.status = RefdataValue.get(params.venStatus).value
        }
        else if(!params.containsKey('venStatus')) {
            queryParams.status = "Current"
            params.venStatus = RDStore.VENDOR_STATUS_CURRENT.id
        }

        Set<String> directMappings = ['curatoryGroupType', 'qp_supportedLibrarySystems', 'qp_electronicBillings', 'qp_invoiceDispatchs']
        directMappings.each { String mapping ->
            if(params.containsKey(mapping))
                queryParams.put(mapping,params.get('mapping'))
        }

        Map<String, Object> wekbResult = gokbService.doQuery(result, [max: 10000, offset: 0], queryParams)
        if(wekbResult.recordsCount > 0)
            records.putAll(wekbResult.records.collectEntries { Map wekbRecord -> [wekbRecord.uuid, wekbRecord] })
        records
    }

    boolean isMyVendor(Vendor vendor, Org contextOrg) {
        int count = VendorRole.executeQuery('select count(*) from OrgRole oo, VendorRole vr where (vr.subscription = oo.sub or vr.license = oo.lic) and oo.org = :context and vr.vendor = :vendor', [vendor: vendor, context: contextOrg])[0]
        count > 0
    }

    Set<Platform> getSubscribedPlatforms(Vendor vendor, Org contextOrg) {
        String instanceFilter = ''
        if(contextOrg.isCustomerType_Consortium())
            instanceFilter = 'and s.instanceOf = null'
        Platform.executeQuery('select pkg.nominalPlatform from PackageVendor pv, VendorRole vr, OrgRole oo join oo.sub s join pv.pkg pkg where pv.vendor = :vendor and pv.vendor = vr.vendor and vr.subscription = s and s.status = :current and oo.org = :contextOrg '+instanceFilter, [vendor: vendor, current: RDStore.SUBSCRIPTION_CURRENT, contextOrg: contextOrg])
    }

    Map<String, Object> getResultGenerics(GrailsParameterMap params) {
        Org contextOrg = contextService.getOrg()
        User contextUser = contextService.getUser()
        Map<String, Object> result = [user: contextUser,
                                      institution: contextOrg,
                                      wekbApi: ApiSource.findByTypAndActive(ApiSource.ApiTyp.GOKBAPI, true)]
        SwissKnife.setPaginationParams(result, params, contextUser)
        result
    }

}
