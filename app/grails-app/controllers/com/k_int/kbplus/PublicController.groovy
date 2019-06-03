package com.k_int.kbplus
import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import de.laser.controller.AbstractDebugController
import de.laser.helper.RDStore
import grails.plugin.cache.Cacheable;
import grails.plugin.springsecurity.annotation.Secured;

@Secured(['permitAll'])
class PublicController {

    def springSecurityService
    def genericOIDService

    @Cacheable('laser_static_pages')
    @Secured(['permitAll'])
    def index() {
    }
    @Secured(['permitAll'])
    def gasco() {
        def result = [:]

        def consRoles = Role.findAll { authority == 'ORG_CONSORTIUM_SURVEY' || authority == 'ORG_CONSORTIUM' }

        result.allConsortia = Org.executeQuery(
                "select o from Org o, OrgSettings os_gs, OrgSettings os_ct where " +
                        "os_gs.org = o and os_gs.key = 'GASCO_ENTRY' and os_gs.rdValue.value = 'Yes' and " +
                        "os_ct.org = o and os_ct.key = 'CUSTOMER_TYPE' and os_ct.roleValue in (:roles) ",
                    [roles: consRoles]
        ).sort { it.name?.toLowerCase() }


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
            queryParams.put('gasco', PropertyDefinition.findByName('GASCO Entry'))
            query += "        ) "

            query += " and exists ( select ogr from OrgRole ogr where ogr.sub = s and ogr.org in (:validOrgs) )"
            queryParams.put('validOrgs', result.allConsortia)

            if (q) {
                query += " and ("
                query += "    exists "
                query += "          ( select scp1 from s.customProperties as scp1 where "
                query += "               scp1.type = :gascoAnzeigenname and lower(scp1.stringValue) like :q"
                query += "           )"

                query += " or "
                query += "    ((lower(s.name) like :q) "

                query += " or exists ("
                query += "    select ogr from s.orgRelations as ogr where ("
                query += "          lower(ogr.org.name) like :q or lower(ogr.org.shortname) like :q or lower(ogr.org.sortname) like :q"
                query += "      ) and ogr.roleType.value = 'Provider'"
                query += "    )"
                query += " ))"

                queryParams.put('gascoAnzeigenname', PropertyDefinition.findByDescrAndName(PropertyDefinition.SUB_PROP, 'GASCO-Anzeigename'))
                queryParams.put('q', '%' + q.toLowerCase() + '%')
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
//        def result = [:]
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
//                    RefdataValue.getByValueAndCategory('Yes', 'YN')
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
        def result = [:]

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
                    PropertyDefinition.findByDescrAndName('Subscription Property', 'GASCO Entry'),
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
                        " SELECT io FROM IdentifierOccurrence AS io " +
                        " WHERE io.ti = ie.tipp.title AND io.identifier.value LIKE :idv "

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

    @Deprecated
    @Secured(['ROLE_YODA'])
  def journalLicenses(){
    log.debug("journalLicenses :: ${params}")
    def result = [:]

    if(params.journal && params.org){
      if(springSecurityService.principal != "anonymousUser"){
          result.user = User.get(springSecurityService.principal.id)
      }else{
        result.user = null
      }
      def ti = null
      def org = null
      if(params.journal.contains(":")){
        def (ns,id) = params.journal.split(":")
        if(ns=="kb"){
          try {
            ti = TitleInstance.get(id)
          } catch (NumberFormatException) {
            flash.error="Entering ns and id e.g :${params.journal} is not permitted, instead it should be ns:identifer number e.g. kb:123"
            log.error("Namespace & ID error for public journalLicenses: ns:${ns} id:${id} (expected integer)")
          }
        }else{
          ti = TitleInstance.lookupByIdentifierString(params.journal)
        }
      }else{
        ti = TitleInstance.findAllByTitleIlike("${params.journal}%")
      }

      if(params.org.isLong()){
        org = Org.get(params.org.toLong())
      }else{
        org = Org.findByNameIlike("${params.org}%")
      }

      log.debug("${ti} and ${org}")
      if(ti && org){
        def access_prop =  grails.util.Holders.config.customProperties.org.journalAccess
        def org_access = org.customProperties.find{it.type.name == access_prop.name}
        if(checkUserAccessToOrg(result.user,org,org_access)){
          def ies = retrieveIssueEntitlements(ti,org,result)
          log.debug("Retrieved ies: ${ies}")
          if(ies) generateIELicenseMap(ies,result);
        }else{
          flash.error = "${org.name} does not provide public access to this service."
        }
      }
    }
    result.journal = params.journal
    result.org = params.org

    result
  }

  private def checkUserAccessToOrg(user, org, org_access) {
    def hasAccess = false
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
    def deleted_ie = RefdataValue.getByValueAndCategory('Deleted', 'Entitlement Issue Status')
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
