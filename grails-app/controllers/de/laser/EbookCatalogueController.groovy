package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.properties.SubscriptionProperty
import de.laser.properties.PropertyDefinition
import de.laser.helper.RDStore
import grails.plugin.springsecurity.annotation.Secured
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession

/**
 * This controller is for managing calls to the ebooks catalogue
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class EbookCatalogueController {

    GenericOIDService genericOIDService

    /**
     * The landing page of the catalogue
     * @return either the selection filter or the list of filter results
     */
    @Secured(['ROLE_ADMIN'])
    def index() {

        Map<String, Object> result = _stats_TODO( false )

        if (! params.subKinds && ! params.consortia && ! params.q) {
            // init filter with checkboxes checked
            result.initQuery = 'true'
            result.queryHistory = _history_TODO( null, 0, false )
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
            queryParams.put('gasco', PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP))
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
                query += "    select ogr from s.orgRelations as ogr where ("
                query += "          genfunc_filter_matcher(ogr.org.name, :q) = true or genfunc_filter_matcher(ogr.org.shortname, :q) = true or genfunc_filter_matcher(ogr.org.sortname, :q) = true "
                query += "      ) and ogr.roleType.value = 'Provider'"
                query += "    )"
                query += " ))"

                queryParams.put('gascoAnzeigenname', PropertyDefinition.getByNameAndDescr('GASCO display name', PropertyDefinition.SUB_PROP))
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

            List subKinds = []
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

            result.queryHistory = _history_TODO( params, result.subscriptionsCount, false )
        }

        result
    }

    /**
     * Retrieves the details to an ebook
     * @return publicly visible title details
     */
    @Secured(['ROLE_ADMIN'])
    def details() {
        Map<String, Object> result = [:]

        result.issueEntitlements = []

        result.idnsPreset = IdentifierNamespace.findAll {
            ns in ['eissn', 'issn', 'zdb', 'ezb']
        }

        if (params.id) {
            SubscriptionPackage sp  = SubscriptionPackage.get(params.long('id'))
            Subscription sub = sp?.subscription
            Package pkg = sp?.pkg
            SubscriptionProperty scp = SubscriptionProperty.findByOwnerAndTypeAndRefValue(
                    sub,
                    PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP),
                    RDStore.YN_YES
            )

            if (scp) {
                result.subscription = sub

                String base_query = " FROM IssueEntitlement as ie WHERE ie.subscription = :sub and (ie.status.value != 'Deleted' and ie.status.value != 'Retired')"+
                        " and exists (SELECT tipp FROM TitleInstancePackagePlatform as tipp WHERE ie.tipp = tipp and tipp.pkg = :pkg )"
                Map<String, Object> queryParams = [sub: sub, pkg: pkg]

                result.issueEntitlementsCount = IssueEntitlement.executeQuery("select ie.id " + base_query, queryParams).size()

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
                        query += " AND io.identifier.ns = :idns "

                        queryParams.put('idns', IdentifierNamespace.get(params.idns))
                    }
                    query += " ) ) "

                    queryParams.put('idv', '%' + idv.toLowerCase() + '%')
                }
                query += " order by LOWER(tipp.sortname)"
                result.issueEntitlements = IssueEntitlement.executeQuery(query, queryParams)
            }
            else {
                redirect controller: 'public', action: 'gasco'
                return
            }
        }
        result
    }

    /**
     * method under development
     * @param reset
     * @return
     */
    private Map<String, Object> _stats_TODO(boolean reset) {

        Map<String, Object> result = [:]
        HttpSession session = RequestContextHolder.currentRequestAttributes().getSession()

        if (reset) {
            session.removeAttribute('ebc_allConsortia')
            session.removeAttribute('ebc_allSubscriptions')
            session.removeAttribute('ebc_allProvider')
            session.removeAttribute('ebc_allTitles')
        }

        // --- stats

        if (! session.getAttribute('ebc_allConsortia')) {
            session.setAttribute('ebc_allConsortia',
                    Org.executeQuery(
                    """select o from Org o, OrgSetting os_gs, OrgSetting os_ct where 
                        os_gs.org = o and os_gs.key = 'GASCO_ENTRY' and os_gs.rdValue.value = 'Yes' and 
                        os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and 
                        os_ct.roleValue in (select r from Role r where authority  = 'ORG_CONSORTIUM')
                        order by lower(o.name)"""
            ))
        }
        result.allConsortia = session.getAttribute('ebc_allConsortia')

        if (! session.getAttribute('ebc_allSubscriptions')) {
            session.setAttribute('ebc_allSubscriptions',
                    Subscription.executeQuery(
                    """select distinct s from Subscription as s where (
                            lower(s.status.value) = 'current' and lower(s.type.value) != 'local subscription'
                            and exists 
                                ( select scp from s.propertySet as scp where
                                    scp.type = :gasco and lower(scp.refValue.value) = 'yes' )
                            )
                            and exists 
                                ( select ogr from OrgRole ogr where ogr.sub = s and ogr.org in (:validOrgs) )""",
                    [
                    gasco    : PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP),
                    validOrgs: result.allConsortia
                    ]
            ))
        }
        result.allSubscriptions = session.getAttribute('ebc_allSubscriptions')

        if (! session.getAttribute('ebc_allProvider')) {
            session.setAttribute('ebc_allProvider',
                    Org.executeQuery(
                    """select distinct ogr.org from OrgRole ogr where 
                            ogr.roleType.value = 'Provider'
                            and ogr.sub in (:allSubscriptions)""",
                    [
                    allSubscriptions: result.allSubscriptions
                    ]
            ))
        }
        result.allProvider = session.getAttribute('ebc_allProvider')

        if (! session.getAttribute('ebc_allTitles')) {
            session.setAttribute('ebc_allTitles',
                    IssueEntitlement.executeQuery(
                            """select ie from IssueEntitlement ie where 
                            ie.subscription in :allSubscriptions 
                            and ie.status.value != 'Deleted' and ie.status.value != 'Retired'
                                """,
                            [
                            allSubscriptions: result.allSubscriptions
                            ]
            ))

        }
        result.allTitles = session.getAttribute('ebc_allTitles')

        // --- hitCounter

        int hitCounter = session.getAttribute('ebc_hitCounter') ?: 0
        result.hitCounter = ++hitCounter
        session.setAttribute('ebc_hitCounter', result.hitCounter)

        result
    }

    /**
     * method under development
     * @param params
     * @param subCount
     * @param reset
     * @return
     */
    private List _history_TODO(GrailsParameterMap params, int subCount, boolean reset) {

        HttpSession session = RequestContextHolder.currentRequestAttributes().getSession()

        if (reset) {
            session.removeAttribute('ebc_queryHistory')
        }

        // --- queryHistory

        if (! session.getAttribute('ebc_queryHistory')) {
            session.setAttribute('ebc_queryHistory', [])
        }

        List<String> query = []
        List<String> label = []

        if (params?.containsKey('q')) {
            query.add('q=' + params.get('q'))
            if (params.q) {
                label.add(params.q)
            }
        }
        if (params?.containsKey('consortia')) {
            query.add('consortia=' + params.get('consortia'))
            if (params.consortia) {
                label.add(genericOIDService.resolveOID(params.consortia).getName())
            }
        }
        if (params?.containsKey('subKinds') && params.subKinds) {
            query.add('subKinds=' + params.list('subKinds').join('&subKinds='))
            if (params.subKinds) {
                if (params.list('subKinds').size() == 5) {
                    label.add('alle Lizenztypen')
                } else {
                    label.add(params.list('subKinds').collect { RefdataValue.get(it).getI10n('value') }.join(','))
                }
            }
        }

        List history = (List) session.getAttribute('ebc_queryHistory')

        if (query) {
            history.add([label: label, matches: subCount, queryString: '?' + query.join('&')])

            if (history.size() > 10) {
                history.remove(0)
            }
            session.setAttribute('ebc_queryHistory', history)
        }

        history.reverse()
    }
}
