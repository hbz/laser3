package com.k_int.kbplus


import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinition
import de.laser.helper.RDStore
import grails.plugin.cache.Cacheable
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsApplication

@Secured(['permitAll'])
class PublicController {
    GrailsApplication grailsApplication
    def springSecurityService
    def genericOIDService
    def mailService

    @Cacheable('laser_static_pages')
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
                from grailsApplication.config.notifications.email.from
                subject grailsApplication.config.laserSystemId + ' - ' + 'Feedback-Mechanismus Barrierefreiheit'
                body (view: '/mailTemplates/text/wcagFeedback', model: [name:params.name, email:params.email,url:params.url, comment:params.comment])

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
    def gasco() {
        Map<String, Object> result = [:]

        result.allConsortia = Org.executeQuery(
                """select o from Org o, OrgSettings os_gs, OrgSettings os_ct where 
                        os_gs.org = o and os_gs.key = 'GASCO_ENTRY' and os_gs.rdValue.value = 'Yes' and 
                        os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and 
                        os_ct.roleValue in (select r from Role r where authority in ('ORG_CONSORTIUM_SURVEY', 'ORG_CONSORTIUM'))
                        order by lower(o.name)"""
        )


        if (! params.subTypes && ! params.consortia && ! params.q) {
            // init filter with checkboxes checked
            result.initQuery = 'true'
        }
        else {

            def q = params.q?.trim()
            def queryParams = [:]

            String query = "from Subscription as s where ("
            query += "      lower(s.status.value) = 'current'"
            query += "      and lower(s.type.value) != 'local licence'"
            query += "      and exists "
            query += "          ( select scp from s.customProperties as scp where "
            query += "               scp.type = :gasco and lower(scp.refValue.value) = 'yes'"
            query += "           )"
            queryParams.put('gasco', PropertyDefinition.getByNameAndDescr('GASCO Entry', PropertyDefinition.SUB_PROP))
            query += "        ) "

            query += " and exists ( select ogr from OrgRole ogr where ogr.sub = s and ogr.org in (:validOrgs) )"
            queryParams.put('validOrgs', result.allConsortia)

            if (q) {
                query += " and ("
                query += "    exists "
                query += "          ( select scp1 from s.customProperties as scp1 where "
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

            def subTypes = []
            if (params.containsKey('subTypes')) {
                params.list('subTypes').each{
                    subTypes.add(Long.parseLong(it))
                }
                if (subTypes) {
                    query += " and s.type.id in (:subTypes) "
                    queryParams.put('subTypes', subTypes)
                }
            }

/*
            def subTypes = params.list('subTypes')
            if (subTypes) {
                subTypes = subTypes.collect { it as Long }
                query += " and s.type.id in (:subTypes) "

                queryParams.put('subTypes', subTypes)
            }
            else {
                query += " and s.type.id in (:subTypes) "
                // fake negative result for query without checked subTypes
                queryParams.put('subTypes', [0.longValue()])
            }
*/
            result.subscriptions = []

            if (result.allConsortia && (q || consortia || subTypes)) {
                result.subscriptions = Subscription.executeQuery("select s ${query} order by lower(s.name) asc", queryParams)
            }
            result.subscriptionsCount = result.subscriptions.size()
        }

        result
    }

//    @Secured(['permitAll'])
//    def gascoDetails() {
//        Map<String, Object> result = [:]
//
//        result.tipps = []
//
//        result.idnsPreset = IdentifierNamespace.findAll {
//            ns in ['eissn', 'issn', 'zdb', 'ezb']
//        }
//
//        if (params.id) {
//            def sp  = SubscriptionPackage.get(params.long('id'))
//            def sub = sp?.subscription
//            def pkg = sp?.pkg
//            def scp = SubscriptionCustomProperty.findByOwnerAndTypeAndRefValue(
//                    sub,
//                    PropertyDefinition.findByDescrAndName('Subscription Property', 'GASCO Entry'),
//                    RefdataValue.getByValueAndCategory('Yes', RDConstants.Y_N)
//            )
//
//            if (scp) {
//                result.subscription = sub
//
//                def query = "SELECT tipp FROM TitleInstancePackagePlatform as tipp WHERE tipp.pkg = :pkg and tipp.status.value != 'Deleted'"
//                def queryParams = [pkg: pkg]
//
//                result.tippsCount = TitleInstancePackagePlatform.executeQuery(query, queryParams).size()
//
//                def q = params.q?.trim()
//                if (q) {
//                    query += " AND ( LOWER(tipp.title.title) LIKE :q ) "
//
//                    queryParams.put('q', '%' + q.toLowerCase() + '%')
//                }
//
//                def idv = params.idv?.trim()
//                if (idv) {
//                    query += " AND ( EXISTS ( " +
//                        " SELECT io FROM IdentifierOccurrence AS io " +
//                        " WHERE io.ti = tipp.title AND io.identifier.value LIKE :idv "
//
//                    if (params.idns) {
//                        query += " AND io.identifier.ns = :idns "
//
//                        queryParams.put('idns', IdentifierNamespace.get(params.idns))
//                    }
//
//                    query += " ) ) "
//
//                    queryParams.put('idv', '%' + idv.toLowerCase() + '%')
//                }
//
//                result.tipps = TitleInstancePackagePlatform.executeQuery(query, queryParams)
//            }
//            else {
//                redirect controller: 'public', action: 'gasco'
//            }
//        }
//
//        result
//    }

    @Secured(['permitAll'])
    def gascoDetailsIssueEntitlements() {
        Map<String, Object> result = [:]

        result.issueEntitlements = []

        result.idnsPreset = IdentifierNamespace.findAll {
            ns in ['eissn', 'issn', 'zdb', 'ezb']
        }

        if (params.id) {
            def sp  = SubscriptionPackage.get(params.long('id'))
            def sub = sp?.subscription
            def pkg = sp?.pkg
            def scp = SubscriptionCustomProperty.findByOwnerAndTypeAndRefValue(
                    sub,
                    PropertyDefinition.getByNameAndDescr('GASCO Entry', 'Subscription Property'),
                    RDStore.YN_YES
            )

            if (scp) {
                result.subscription = sub

                def base_query = " FROM IssueEntitlement as ie WHERE ie.subscription = :sub and (ie.status.value != 'Deleted' and ie.status.value != 'Retired')"+
                        " and exists (SELECT tipp FROM TitleInstancePackagePlatform as tipp WHERE ie.tipp = tipp and tipp.pkg = :pkg )"
                def queryParams = [sub: sub, pkg: pkg]

                result.issueEntitlementsCount = IssueEntitlement.executeQuery("select ie.id " + base_query, queryParams).size()

                def query = "SELECT ie " + base_query

                def q = params.q?.trim()
                if (q) {
                    query += " AND ( LOWER(tipp.title.title) LIKE :q ) "

                    queryParams.put('q', '%' + q.toLowerCase() + '%')
                }

                def idv = params.idv?.trim()
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

  private def checkUserAccessToOrg(user, org, org_access) {
    boolean hasAccess = false
    def org_access_rights = org_access?.getValue() ? org_access.getValue().split(",") : []
    org_access_rights = org_access_rights.collect{it.toLowerCase()}
    if(org_access_rights.contains("public")) return true;
    if(org_access_rights == []){
      //When no rights specified, users affiliated with the org should have access
      if(com.k_int.kbplus.auth.UserOrg.findAllByUserAndOrg(user,org)) return true;
    }
    if(user){
      def userRole = com.k_int.kbplus.auth.UserOrg.findAllByUserAndOrg(user,org)
      hasAccess = userRole.any{
        if(org_access_rights.contains(it.formalRole.authority.toLowerCase()) || org_access_rights.contains(it.formalRole.roleType.toLowerCase())) {
          return true;
        }
          
      }
    }
    return hasAccess
  }

  private def generateIELicenseMap(ies, result) {
    log.debug("generateIELicenseMap")
    def comparisonMap = [:]
    def licIEMap = new TreeMap()
    //See if we got IEs under the same license, and list them together
    ies.each{ ie->
      def lic = ie.subscription.owner
      if(licIEMap.containsKey(lic)){
        licIEMap.get(lic).add(ie)
      }else{
        licIEMap.put(lic,[ie])
      }
    }
    licIEMap.each{
      def lic = it.getKey()
          lic.customProperties.each{prop ->
            def point = [:]
            if(prop.getValue()|| prop.getNote()){
              point.put(lic.reference,prop)
              if(comparisonMap.containsKey(prop.type.name)){
                comparisonMap[prop.type.name].putAll(point)
              }else{
                comparisonMap.put(prop.type.name,point)
              }
            }
          }
    }
    result.licIEMap = licIEMap
    result.comparisonMap = comparisonMap
    log.debug("Processed: "+result)
  }

  @Deprecated
  private def retrieveIssueEntitlements(ti, org, result) {
    log.debug("retrieveIssueEntitlements")
    def issueEntitlements = []
    def deleted_ie = RDStore.TIPP_DELETED
    def today = new Date()

    String ie_query = "select ie from IssueEntitlement as ie join ie.subscription as sub where ie.tipp.title=(:journal) and exists ( select orgs from sub.orgRelations orgs where orgs.org = (:org) AND orgs.roleType.value = 'Subscriber' ) and ie.status != (:deleted_ie) and ie.subscription.owner is not null"
    ti.each{
      def queryParams = [org:org,journal:it,deleted_ie:deleted_ie]
      def query_results = IssueEntitlement.executeQuery(ie_query,queryParams)
      //Check that items are current based on dates
      query_results.each{ ie ->
        def current_ie = ie.accessEndDate > today || ie.accessEndDate == null
        def current_sub = ie.subscription.endDate > today || ie.subscription.endDate == null
        def current_license = ie.subscription.owner.endDate > today || ie.subscription.owner.endDate == null
        if(current_ie && current_sub && current_license){
          issueEntitlements.add(ie)
        }else{
          log.debug("${ie} is not current")
        }
      }
    }
    return issueEntitlements
  } 
}
