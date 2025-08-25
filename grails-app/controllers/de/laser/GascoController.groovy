package de.laser

import de.laser.properties.SubscriptionProperty
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class GascoController {

    ContextService contextService
    GenericOIDService genericOIDService
    SpringSecurityService springSecurityService

    @Secured(['permitAll'])
    def members() {
        Map<String, Object> result = [:]
        render view: '/gasco/members', model: result
    }

    @Secured(['permitAll'])
    def about() {
        Map<String, Object> result = [:]
        render view: '/gasco/about', model: result
    }

    /**
     * Displays the GASCO page. GASCO stands for German-Austrian-Swiss Consortia and means the consortium institutions
     * in the German-speaking area
     * @return a form to select the consortium to display or if the filter has been submitted, a list of
     * subscriptions hold by the given consortium and which are enabled by subscription property GASCO display
     * to appear on the GASCO list
     * @see de.laser.properties.SubscriptionProperty
     */
    @Secured(['permitAll'])
    def monitor() {
        Map<String, Object> result = [:]

        try {

            result.allConsortia = Org.executeQuery(
                    """select o from Org o, OrgSetting os_ct where 
                        os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and 
                        os_ct.roleValue in (select r from Role r where authority in ('ORG_CONSORTIUM_PRO','ORG_CONSORTIUM_BASIC'))
                        order by lower(o.name)"""
            )

            if (! params.subKinds && ! params.consortia && ! params.q) {
                // init filter with checkboxes checked
                result.initQuery = 'true'
            }
            else {
                String q = params.q?.trim()
                Map<String, Object> queryParams = [:]

                String query = "from Subscription as s where ("
                query += "      lower(s.status.value) = 'current'"
                query += "      and lower(s.type.value) != 'local subscription'"
                query += "      and exists "
                query += "          ( select scp from s.propertySet as scp where "
                query += "               scp.type = :gasco and lower(scp.refValue.value) = 'yes'"
                query += "           )"
                queryParams.put('gasco', PropertyStore.SUB_PROP_GASCO_ENTRY)
                query += "        ) "

                query += " and exists ( select ogr from OrgRole ogr where ogr.sub = s and ogr.org in (:validOrgs) )"
                queryParams.put('validOrgs', result.allConsortia)

                if (q) {
                    query += " and ("
                    query += "    exists "
                    query += "          ( select scp1 from s.propertySet as scp1 where "
                    query += "               scp1.type = :gascoAnzeigenname and genfunc_filter_matcher(scp1.stringValue, :q) = true"
                    query += "           )"

                    query += " or "
                    query += "    ( genfunc_filter_matcher(s.name, :q) = true  "

                    query += " or exists ("
                    query += "    select pvr from s.providerRelations as pvr where genfunc_filter_matcher(pvr.provider.name, :q) = true or genfunc_filter_matcher(pvr.provider.abbreviatedName, :q) = true "
                    query += "    )"
                    query += " ))"

                    queryParams.put('gascoAnzeigenname', PropertyStore.SUB_PROP_GASCO_DISPLAY_NAME)
                    queryParams.put('q', q)
                }

                def consortia = params.consortia ? genericOIDService.resolveOID(params.consortia) : null
                if (consortia) {
                    query += " and exists ("
                    query += "    select cr from s.orgRelations as cr where lower(cr.roleType.value) = 'subscription consortia'"
                    query += "       and cr.org = :consortia"
                    query += " )"

                    queryParams.put('consortia', consortia)
                }

                def subKinds = []
                if (params.containsKey('subKinds')) {
                    params.list('subKinds').each{
                        subKinds.add(Long.parseLong(it))
                    }
                    if (subKinds) {
                        query += " and s.kind.id in (:subKinds) "
                        queryParams.put('subKinds', subKinds)
                    }
                }
                result.subscriptions = []

                if (result.allConsortia && (q || consortia || subKinds)) {
                    result.subscriptions = Subscription.executeQuery("select s " + query + " order by lower(s.name) asc", queryParams)
                }
                result.subscriptionsCount = result.subscriptions.size()
            }

        } catch (Exception e) {
            log.warn 'gasco: exception caused by ' + request.getRemoteAddr()
            throw e
        }

        render view: '/gasco/monitor', model: result
    }

    /**
     * Displays the issue entitlement details of the selected title
     * @see IssueEntitlement
     */
    @Secured(['permitAll'])
    def monitorDetails() {
        Map<String, Object> result = [
                isPublic_gascoDetails : ! springSecurityService.isLoggedIn()
        ]

        result.issueEntitlements = []

        result.max    = params.max    ? Integer.parseInt(params.max.toString()) : 25
        result.offset = params.offset ? Integer.parseInt(params.offset.toString()) : 0

        result.idnsPreset = IdentifierNamespace.findAll {
            ns in ['doi', 'eissn', 'issn', 'zdb', 'ezb', 'eisbn', 'isbn', 'title_id'] && nsType == TitleInstancePackagePlatform.class.name
        }

        if (params.id) {
            SubscriptionPackage sp  = SubscriptionPackage.get(params.long('id'))
            Subscription sub = sp?.subscription
            Package pkg = sp?.pkg
            SubscriptionProperty scp = SubscriptionProperty.findByOwnerAndTypeAndRefValue( sub, PropertyStore.SUB_PROP_GASCO_ENTRY, RDStore.YN_YES )

            if (scp) {
                result.subscription = sub

                String base_query = " FROM IssueEntitlement as ie WHERE ie.subscription = :sub and (ie.status = :current or ie.status = :expected)"+
                        " and ie.tipp.pkg = :pkg "
                Map<String, Object> queryParams = [sub: sub, pkg: pkg, current: RDStore.TIPP_STATUS_CURRENT, expected: RDStore.TIPP_STATUS_EXPECTED]

                result.issueEntitlementsCount = IssueEntitlement.executeQuery("select count(*) " + base_query, queryParams)[0]

                String query = "SELECT ie " + base_query

                String q = params.q?.trim()
                if (q) {
                    query += " AND ( LOWER(tipp.name) LIKE :q ) "

                    queryParams.put('q', '%' + q.toLowerCase() + '%')
                }

                String idv = params.idv?.trim()
                if (idv) {
                    query += " AND ( EXISTS ( " +
                            " SELECT ident FROM Identifier AS ident " +
                            " WHERE ident.tipp = ie.tipp AND ident.value LIKE :idv "

                    if (params.idns) {
                        query += " AND ident.ns = :idns "

                        queryParams.put('idns', IdentifierNamespace.get(params.idns))
                    }
                    query += " ) ) "

                    queryParams.put('idv', '%' + idv.toLowerCase() + '%')
                }
                query += " order by LOWER(ie.tipp.sortname)"

                result.issueEntitlements = IssueEntitlement.executeQuery(query, queryParams, [max: result.max, offset: result.offset])
                result.issueEntitlementsFilterCount = IssueEntitlement.executeQuery("select count(*) " + base_query, queryParams)[0]
            }
            else {
                redirect controller: 'gasco', action: 'monitor'
                return
            }
        }
        render view: '/gasco/monitorDetails', model: result
    }

    /**
     * Call to open the flyout containing the library types and regions participating at the given consortium (= consortial subscription)
     * @return a list of regions and library types, rendered in the flyout
     * @see Org#libraryType
     * @see Org#region
     */
    @Secured(['permitAll'])
    def monitorData() {
        Map<String, Object> result = [
                title: '?',
                data: []
        ]

        if (params.key) {
            Subscription sub = Subscription.get(params.key)

            if (sub && SubscriptionProperty.findByOwnerAndTypeAndRefValue( sub, PropertyStore.SUB_PROP_GASCO_ENTRY, RDStore.YN_YES )) {
                List<Org> participants =  Subscription.executeQuery(
                        'select distinct oo.org from Subscription s join s.orgRelations oo where s.instanceOf = :parent and oo.roleType in :subscriberRoleTypes',
                        [parent: sub, subscriberRoleTypes: [RDStore.OR_SUBSCRIBER, RDStore.OR_SUBSCRIBER_CONS, RDStore.OR_SUBSCRIBER_CONS_HIDDEN]]
                )
                result.title = sub.name
                result.info  = SubscriptionProperty.findByOwnerAndType(sub, PropertyStore.SUB_PROP_GASCO_GENERAL_INFORMATION)?.getValue()

                if (participants) {
                    List libraryTypes = Org.executeQuery('select lt, count(lt) from Org o join o.libraryType lt where o in (:oList) group by lt', [oList: participants])
                    result.data.add([
                            'key'   : 'libraryType',
                            'title' : message(code:'org.libraryType.label') + ' der Einrichtungen',
                            'data'  : libraryTypes.collect{ [id: it[0].id, name: it[0].getI10n('value'), value: it[1]] }
                    ])

                    List regions = Org.executeQuery('select r, count(r) from Org o join o.region r where o in (:oList) group by r', [oList: participants])
                    result.data.add([
                            'key'   : 'federalState',
                            'title' : message(code:'org.federalState.label') + ' der Einrichtungen',
                            'data'  : regions.collect{ [id: it[0].id, name: it[0].getI10n('value'), value: it[1]] }
                    ])

                }
            }
        }
        render result as JSON
    }
}
