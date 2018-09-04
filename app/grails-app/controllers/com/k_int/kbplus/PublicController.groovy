package com.k_int.kbplus
import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinition
import grails.plugin.cache.Cacheable;
import grails.plugin.springsecurity.annotation.Secured;

@Secured(['permitAll'])
class PublicController {

    def springSecurityService
    def genericOIDService

    @Cacheable('laser_experimental')
    @Secured(['permitAll'])
    def index() {
    }

    @Secured(['permitAll'])
    def gasco() {
        def result = [:]

        result.allConsortia = Org.findAllByOrgType(
                RefdataValue.getByValueAndCategory('Consortium', 'OrgType')
        ).sort{ it.getDesignation() }

        if (! params.subTypes && ! params.consortia && ! params.q) {
            // init filter with checkboxes checked
            result.initQuery = 'true'
        }
        else {

            def query = "from Subscription as s where ("
            query += "      lower(s.status.value) = 'current'"
            query += "      and lower(s.type.value) != 'local licence'"
            query += "      and exists (select scp from s.customProperties as scp where scp.type = :gasco and lower(scp.refValue.value) = 'yes')"
            query += " )"

            def queryParams = [gasco: PropertyDefinition.findByName('GASCO Entry')]

            def q = params.q?.trim()
            if (q) {
                query += " and ((lower(s.name) like :q) or exists ("
                query += "    select ogr from s.orgRelations as ogr where ("
                query += "          lower(ogr.org.name) like :q or lower(ogr.org.shortname) like :q or lower(ogr.org.sortname) like :q"
                query += "      ) and ogr.roleType.value = 'Provider'"
                query += "    )"
                query += " )"

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

            if (q || consortia || subTypes) {
                result.subscriptionsCount = Subscription.executeQuery("select count(s) " + query, queryParams)[0]
                result.subscriptions = Subscription.executeQuery("select s ${query} order by lower(s.name) asc", queryParams)
            }
        }

        result
    }

    @Secured(['permitAll'])
    def gascoDetails() {
        def result = [:]

        result.tipps = []

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
                    RefdataValue.getByValueAndCategory('Yes', 'YN')
            )

            if (scp) {
                result.subscription = sub

                def query = "SELECT tipp FROM TitleInstancePackagePlatform as tipp WHERE tipp.pkg = :pkg "
                def queryParams = [pkg: pkg]

                result.tippsCount = TitleInstancePackagePlatform.executeQuery(query, queryParams).size()

                def q = params.q?.trim()
                if (q) {
                    query += " AND ( LOWER(tipp.title.title) LIKE :q ) "

                    queryParams.put('q', '%' + q.toLowerCase() + '%')
                }

                def idv = params.idv?.trim()
                if (idv) {
                    query += " AND ( EXISTS ( " +
                        " SELECT io FROM IdentifierOccurrence AS io " +
                        " WHERE io.ti = tipp.title AND io.identifier.value LIKE :idv "

                    if (params.idns) {
                        query += " AND io.identifier.ns = :idns "

                        queryParams.put('idns', IdentifierNamespace.get(params.idns))
                    }

                    query += " ) ) "

                    queryParams.put('idv', '%' + idv.toLowerCase() + '%')
                }

                result.tipps = TitleInstancePackagePlatform.executeQuery(query, queryParams)
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
    def deleted_ie = RefdataCategory.lookupOrCreate('Entitlement Issue Status','Deleted');
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
