package de.laser


import de.laser.properties.SubscriptionProperty
import de.laser.properties.PropertyDefinition
import de.laser.helper.ConfigUtils
import de.laser.helper.RDStore
import grails.plugin.cache.Cacheable
import grails.plugin.springsecurity.annotation.Secured

@Secured(['permitAll'])
class PublicController {

    def springSecurityService
    def genericOIDService
    def mailService
    EscapeService escapeService

    @Secured(['permitAll'])
    def wcagStatement() {
    }
    @Secured(['permitAll'])
    def wcagFeedbackForm() {
    }
    @Secured(['permitAll'])
    def sendFeedbackForm() {

        try {

            mailService.sendMail {
                to 'barrierefreiheitsbelange@hbz-nrw.de'
                from ConfigUtils.getNotificationsEmailFrom()
                subject ConfigUtils.getLaserSystemId() + ' - Feedback-Mechanismus Barrierefreiheit'
                body (view: '/mailTemplates/text/wcagFeedback', model: [name:params.name, email:params.email,url:params.url, comment:escapeService.replaceUmlaute(params.comment)])

            }
        }
        catch (Exception e) {
            println "Unable to perform email due to exception ${e.message}"
        }
//        redirect controller: 'public', action: 'sendFeedbackForm', params: params, id: copyLicense.id
    }
    @Secured(['permitAll'])
    def wcagEasyLanguage() {
    }
    @Secured(['permitAll'])
    def index() {
    }
    @Secured(['permitAll'])
    def wcagTest() {
    }
    @Secured(['permitAll'])
    def gasco() {
        Map<String, Object> result = [:]

        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSetting os_gs, OrgSetting os_ct where 
                        os_gs.org = o and os_gs.key = 'GASCO_ENTRY' and os_gs.rdValue.value = 'Yes' and 
                        os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and 
                        os_ct.roleValue in (select r from Role r where authority  = 'ORG_CONSORTIUM')
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
            query += "      and lower(s.type.value) != 'local licence'"
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

        result
    }

    @Secured(['permitAll'])
    def gascoDetailsIssueEntitlements() {
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
                    query += " AND ( LOWER(tipp.title.title) LIKE :q ) "

                    queryParams.put('q', '%' + q.toLowerCase() + '%')
                }

                String idv = params.idv?.trim()
                if (idv) {
                    query += " AND ( EXISTS ( " +
                        " SELECT ident FROM Identifier AS ident " +
                        " WHERE ident.ti = ie.tipp.title AND ident.value LIKE :idv "

                    if (params.idns) {
                        query += " AND io.identifier.ns = :idns "

                        queryParams.put('idns', IdentifierNamespace.get(params.idns))
                    }
                    query += " ) ) "

                    queryParams.put('idv', '%' + idv.toLowerCase() + '%')
                }
                query += " order by LOWER(tipp.title.title)"
                result.issueEntitlements = IssueEntitlement.executeQuery(query, queryParams)
            }
            else {
                redirect controller: 'public', action: 'gasco'
            }
        }
        result
    }
}
