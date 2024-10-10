package de.laser


import de.laser.properties.SubscriptionProperty
import de.laser.config.ConfigMapper
import de.laser.storage.PropertyStore
import de.laser.storage.RDStore
import de.laser.utils.AppUtils
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.mail.MailService

/**
 * This controller contains all pages which are accessible without a user account, i.e. public access pages
 */
@Secured(['permitAll'])
class PublicController {

    EscapeService escapeService
    GenericOIDService genericOIDService
    MailService mailService
    SpringSecurityService springSecurityService

    /**
     * Displays the landing page
     */
    @Secured(['permitAll'])
    def index() {
    }

   /**
    * Displays the robots.txt preventing crawler access to instances other than the productive one
    */
    @Secured(['permitAll'])
    def robots() {
        String text = "User-agent: *\n"

        if (AppUtils.getCurrentServer() == AppUtils.PROD) {
            text += "Disallow: /tipp/ \n"                                   // TODO TMP
            text += "Disallow: /public/gascoDetailsIssueEntitlements/ \n"   // TODO TMP
            text += "Disallow: /gasco/details/ \n"
        }
        else {
            text += "Disallow: / \n"
        }
        render(text: text, contentType: "text/plain", encoding: "UTF-8")
    }

    /**
     * Displays the WCAG statement
     */
    @Secured(['permitAll'])
    def wcagStatement() {
    }

    /**
     * Displays the WCAG feedback form
     */
    @Secured(['permitAll'])
    def wcagFeedbackForm() {
    }

    /**
     * Takes the submitted message and sends a barrier-free feedback mail to an address responsible for
     * disability matters
     */
    @Secured(['permitAll'])
    def sendFeedbackForm() {

        try {
            mailService.sendMail {
                to 'barrierefreiheitsbelange@hbz-nrw.de'
                from ConfigMapper.getNotificationsEmailFrom()
                subject ConfigMapper.getLaserSystemId() + ' - Feedback-Mechanismus Barrierefreiheit'
                body (view: '/mailTemplates/text/wcagFeedback', model: [name:params.name, email:params.email,url:params.url, comment:escapeService.replaceUmlaute(params.comment)])
            }
        }
        catch (Exception e) {
            log.error "Unable to perform email due to exception ${e.message}"
        }
    }

    /**
     * Displays the site content in easy language
     */
    @Secured(['permitAll'])
    def wcagEasyLanguage() {
    }

    /**
     * Test page for check compatibility
     */
    @Secured(['permitAll'])
    def wcagTest() {
    }

    /**
     * Displays the GASCO page. GASCO stands for German-Austrian-Swiss Consortia and means the consortium institutions
     * in the German-speaking area
     * @return a form to select the consortium to display or if the filter has been submitted, a list of
     * subscriptions hold by the given consortium and which are enabled by subscription property GASCO display
     * to appear on the GASCO list
     * @see SubscriptionProperty
     */
    @Secured(['permitAll'])
    def gasco() {
        Map<String, Object> result = [:]

      try {

        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSetting os_gs, OrgSetting os_ct where 
                        os_gs.org = o and os_gs.key = 'GASCO_ENTRY' and os_gs.rdValue.value = 'Yes' and 
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
                query += "    select ogr from s.orgRelations as ogr where ("
                query += "          genfunc_filter_matcher(ogr.org.name, :q) = true or genfunc_filter_matcher(ogr.org.sortname, :q) = true "
                query += "      ) and ogr.roleType.value = 'Provider'"
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

        result
    }

    /**
     * Displays the issue entitlement details of the selected title
     * @see IssueEntitlement
     */
    @Secured(['permitAll'])
    def gascoDetails() {
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
                redirect controller: 'public', action: 'gasco'
                return
            }
        }
        result
    }

    /**
     * Call to open the flyout containing the library types and regions participating at the given consortium (= consortial subscription)
     * @return a list of regions and library types, rendered in the flyout
     * @see Org#libraryType
     * @see Org#region
     */
    @Secured(['permitAll'])
    def gascoJson() {
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

    /**
     * @return the frontend view with sample area for frontend developing and showcase
     */
    @Secured(['permitAll'])
    def licensingModel() {
        Map<String, Object> result = [:]
        result.mappingColsBasic = ["asService", "accessRights", "community", "wekb"]
        result.mappingColsPro = ["management", "organisation", "reporting", "api"]
        result
    }

    @Secured(['permitAll'])
    def faq() {
        Map<String, Object> result = [
                content : [
                        'various'               : ['Allgemein', 'General'],
                        'notifications'         : ['Benachrichtigungen', 'Notifications'],
                        'propertyDefinitions'   : ['Merkmale', 'Properties'],
                        'userManagement'        : ['Benutzer-Accounts', 'User accounts'],
                ], // todo
                topic   : 'various'
        ]
        if (params.id) {
            result.topic = params.id.toString()
        }
        result
    }

    @Secured(['permitAll'])
    def releaseNotes() {
        Map<String, Object> result = [
                history : ['3.2', '3.3', '3.4'] // todo
        ]

        String[] iap = AppUtils.getMeta('info.app.version').split('\\.')
        if (params.id) {
            iap = params.id.toString().split('\\.')
        }
        result.version = (iap.length >= 2) ? (iap[0] + '.' + iap[1]) : 'failed'
        result
    }
}
