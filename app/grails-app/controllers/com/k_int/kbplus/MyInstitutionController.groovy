package com.k_int.kbplus

import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import de.laser.helper.DebugAnnotation
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.poi.hslf.model.*
import org.apache.poi.hssf.usermodel.*
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel.*
import com.k_int.properties.PropertyDefinition

// import org.json.simple.JSONArray;
// import org.json.simple.JSONObject;
import java.text.SimpleDateFormat
import groovy.sql.Sql

@Secured(['IS_AUTHENTICATED_FULLY'])
class MyInstitutionController {
    def dataSource
    def springSecurityService
    def ESSearchService
    def gazetteerService
    def alertsService
    def genericOIDService
    def factService
    def zenDeskSyncService
    def exportService
    def transformerService
    def institutionsService
    def docstoreService
    def tsvSuperlifterService
    def accessService
    def contextService
    def taskService
    def filterService
    def propertyService

    // copied from
    static String INSTITUTIONAL_LICENSES_QUERY      =
            " from License as l where exists ( select ol from OrgRole as ol where ol.lic = l AND ol.org = :lic_org and ol.roleType IN (:org_roles) ) AND (l.status!=:lic_status or l.status=null ) "

    // copied from
    static String INSTITUTIONAL_SUBSCRIPTION_QUERY  =
            " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) AND ( s.status.value != 'Deleted' ) "

    // Map the parameter names we use in the webapp with the ES fields
    def renewals_reversemap = ['subject': 'subject', 'provider': 'provid', 'pkgname': 'tokname']
    def reversemap = ['subject': 'subject', 'provider': 'provid', 'studyMode': 'presentations.studyMode', 'qualification': 'qual.type', 'level': 'qual.level']

    def possible_date_formats = [
            new SimpleDateFormat('yyyy/MM/dd'),
            new SimpleDateFormat('dd.MM.yyyy'),
            new SimpleDateFormat('dd/MM/yyyy'),
            new SimpleDateFormat('dd/MM/yy'),
            new SimpleDateFormat('yyyy/MM'),
            new SimpleDateFormat('yyyy')
    ];

    @DebugAnnotation(test='hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def index() {
        // Work out what orgs this user has admin level access to
        def result = [:]
        result.institution  = contextService.getOrg()
        result.user = User.get(springSecurityService.principal.id)
        def currentOrg = contextService.getOrg()
        log.debug("index for user with id ${springSecurityService.principal.id} :: ${result.user}");

        if ( result.user ) {
          result.userAlerts = alertsService.getAllVisibleAlerts(result.user);
          //result.staticAlerts = alertsService.getStaticAlerts(request);

          if ((result.user.affiliations == null) || (result.user.affiliations.size() == 0)) {
              redirect controller: 'profile', action: 'index'
          } else {
          }
        }
        else {
          log.error("Failed to find user in database");
        }

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def tipview() {
        log.debug("admin::tipview ${params}")
        def result = [:]

        result.user = User.get(springSecurityService.principal.id)
        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        def current_inst = contextService.getOrg()
        //if(params.shortcode) current_inst = Org.findByShortcode(params.shortcode);
        //Parameters needed for criteria searching
        def (tip_property, property_field) = (params.sort ?: 'title-title').split("-")
        def list_order = params.order ?: 'asc'

        if (current_inst && ! accessService.checkUserIsMember(result.user, current_inst)) {
            flash.error = message(code:'myinst.error.noMember', args:[current_inst.name]);
            response.sendError(401)
            return;
        }

        def criteria = TitleInstitutionProvider.createCriteria();
        def results = criteria.list(max: result.max, offset:result.offset) {
              //if (params.shortcode){
              if (current_inst){
                institution{
                    idEq(current_inst.id)
                }
              }
              if (params.search_for == "institution") {
                institution {
                  ilike("name", "%${params.search_str}%")
                }
              }
             if (params.search_for == "provider") {
                provider {
                  ilike("name", "%${params.search_str}%")
                }
             }
             if (params.search_for == "title") {
                title {
                  ilike("title", "%${params.search_str}%")
                }
             }
             if(params.filter == "core" || !params.filter){
               isNotEmpty('coreDates')
             }else if(params.filter=='not'){
                isEmpty('coreDates')
             }
             "${tip_property}"{
                order(property_field,list_order)
             }
        }

        result.tips = results
        result.institution = current_inst
        result.editable = accessService.checkMinUserOrgRole(result.user, current_inst, 'INST_EDITOR')
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    @Deprecated
    def dashboard_OLD() {
        // Work out what orgs this user has admin level access to
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.userAlerts = alertsService.getAllVisibleAlerts(result.user);
        result.staticAlerts = alertsService.getStaticAlerts(request);

        if ((result.user.affiliations == null) || (result.user.affiliations.size() == 0)) {
            redirect controller: 'profile', action: 'index'
        } else {
        }
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def manageAffiliationRequests() {
        def result = [:]
        result.institution        = contextService.getOrg()
        result.user               = User.get(springSecurityService.principal.id)
        result.editable           = true // inherit
        result.pendingRequestsOrg = UserOrg.findAllByStatusAndOrg(UserOrg.STATUS_PENDING, contextService.getOrg(), [sort:'dateRequested'])

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def actionLicenses() {
        log.debug("actionLicenses :: ${params}")
        if (params['copy-license']) {
            newLicense_DEPR(params)
        } else if (params['delete-license']) {
            deleteLicense(params)
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def currentLicenses() {
        def result = setResultGenerics()
        result.transforms = grailsApplication.config.licenseTransforms

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')
        result.editable      = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        def date_restriction = null;
        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        if (params.validOn == null) {
            result.validOn = sdf.format(new Date(System.currentTimeMillis()))
            date_restriction = sdf.parse(result.validOn)
        } else if (params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        result.propList =
                PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList and pd.tenant is null", [
                        defList: [PropertyDefinition.LIC_PROP, PropertyDefinition.LIC_OA_PROP, PropertyDefinition.LIC_ARC_PROP],
                    ] // public properties
                ) +
                        PropertyDefinition.findAll( "from PropertyDefinition as pd where pd.descr in :defList and pd.tenant = :tenant", [
                                defList: [PropertyDefinition.LIC_PROP, PropertyDefinition.LIC_OA_PROP, PropertyDefinition.LIC_ARC_PROP],
                                tenant: contextService.getOrg()
                            ]// private properties
                        )

        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        result.max = params.format ? 10000 : result.max
        result.offset = params.format? 0 : result.offset

        def licensee_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee');
        def licensee_cons_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee_Consortial');
        def lic_cons_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensing Consortium');

        def template_license_type = RefdataCategory.lookupOrCreate('License Type', 'Template');
        def license_status = RefdataCategory.lookupOrCreate('License Status', 'Deleted')

        def base_qry
        def qry_params

        @Deprecated
        def qry = INSTITUTIONAL_LICENSES_QUERY

        if (! params.orgRole) {
            if (result.institution?.orgType?.value == 'Consortium') {
                params.orgRole = 'Licensing Consortium'
            }
            else {
                params.orgRole = 'Licensee'
            }
        }

        if (params.orgRole == 'Licensee') {

            base_qry = """
from License as l where (
    exists ( select o from l.orgLinks as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :lic_org ) ) 
    AND ( l.status != :deleted OR l.status = null )
    AND ( l.type != :template )
)
"""
            qry_params = [roleType1:licensee_role, roleType2:licensee_cons_role, lic_org:result.institution, deleted:license_status, template: template_license_type]
        }

        if (params.orgRole == 'Licensing Consortium') {

            base_qry = """
from License as l where (
    exists ( select o from l.orgLinks as o where ( 
            ( o.roleType = :roleTypeC 
                AND o.org = :lic_org 
                AND NOT exists (
                    select o2 from l.orgLinks as o2 where o2.roleType = :roleTypeL
                )
            )
        )) 
    AND ( l.status != :deleted OR l.status = null )
    AND ( l.type != :template )
)
"""
            qry_params = [roleTypeC:lic_cons_role, roleTypeL:licensee_cons_role, lic_org:result.institution, deleted:license_status, template:template_license_type]
        }

        if ((params['keyword-search'] != null) && (params['keyword-search'].trim().length() > 0)) {
            base_qry += " and lower(l.reference) like :ref"
            qry_params += [ref:"%${params['keyword-search'].toLowerCase()}%"]
            result.keyWord = params['keyword-search'].toLowerCase()
        }

        // eval property filter

        if (params.filterPropDef) {
            (base_qry, qry_params) = propertyService.evalFilterQuery(params, base_qry, 'l', qry_params)
        }

        if (date_restriction) {
            base_qry += " and ( ( l.startDate <= :date_restr and l.endDate >= :date_restr ) OR l.startDate is null OR l.endDate is null OR l.startDate >= :date_restr  ) "
            qry_params += [date_restr: date_restriction]
            qry_params += [date_restr: date_restriction]
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += " order by l.${params.sort} ${params.order}"
        } else {
            base_qry += " order by l.reference asc"
        }

        log.debug("query = ${base_qry}");
        log.debug("params = ${qry_params}");

        result.licenseCount = License.executeQuery("select count(l) ${base_qry}", qry_params)[0];
        result.licenses = License.executeQuery("select l ${base_qry}", qry_params, [max: result.max, offset: result.offset]);
        def filename = "${result.institution.name}_licenses"
        withFormat {
            html result

            json {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.json\"")
                response.contentType = "application/json"
                render (result as JSON)
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.csv\"")
                response.contentType = "text/csv"

                def out = response.outputStream
                result.searchHeader = true
                exportService.StreamOutLicenseCSV(out, result,result.licenses)
                out.close()
            }
            xml {
                def doc = exportService.buildDocXML("Licences")

                if ((params.transformId) && (result.transforms[params.transformId] != null)) {
                    switch(params.transformId) {
                      case "sub_ie":
                        exportService.addLicenseSubPkgTitleXML(doc, doc.getDocumentElement(),result.licenses)
                      break;
                      case "sub_pkg":
                        exportService.addLicenseSubPkgXML(doc, doc.getDocumentElement(),result.licenses)
                        break;
                    }
                    String xml = exportService.streamOutXML(doc, new StringWriter()).getWriter().toString();
                    transformerService.triggerTransform(result.user, filename, result.transforms[params.transformId], xml, response)
                }else{
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xml\"")
                    response.contentType = "text/xml"
                    exportService.streamOutXML(doc, response.outputStream)
                }
            }
        }
    }

    private buildPropertySearchQuery(params,propDef) {
        def result = [:]

        def query = " and exists ( select cp from l.customProperties as cp where cp.type.name = :prop_filter_name and  "
        def queryParam = [prop_filter_name:params.propertyFilterType];
        switch (propDef.type){
            case Integer.toString():
                query += "cp.intValue = :filter_val "
                def value;
                try{
                 value =Integer.parseInt(params.propertyFilter)
                }catch(Exception e){
                    log.error("Exception parsing search value: ${e}")
                    value = 0
                }
                queryParam += [filter_val:value]
                break;
            case BigDecimal.toString():
                query += "cp.decValue = :filter_val "
                try{
                 value = new BigDecimal(params.propertyFilter)
                }catch(Exception e){
                    log.error("Exception parsing search value: ${e}")
                    value = 0.0
                }
                queryParam += [filter_val:value]
                break;
            case String.toString():
                query += "cp.stringValue like :filter_val "
                queryParam += [filter_val:params.propertyFilter]
                break;
            case RefdataValue.toString():
                query += "cp.refValue.value like :filter_val "
                queryParam += [filter_val:params.propertyFilter]
                break;
            default:
                log.error("Error executing buildPropertySearchQuery. Definition type ${propDef.type} case not found. ")
        }
        query += ")"

        result.query = query
        result.queryParam = queryParam
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def emptyLicense() {
        def result = setResultGenerics()

        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            return;
        }

        result.orgType = result.institution.orgType

        def cal = new java.util.GregorianCalendar()
        def sdf = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        cal.setTimeInMillis(System.currentTimeMillis())
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)

        result.defaultStartYear = sdf.format(cal.getTime())

        cal.set(Calendar.MONTH, Calendar.DECEMBER)
        cal.set(Calendar.DAY_OF_MONTH, 31)

        result.defaultEndYear = sdf.format(cal.getTime())

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        def template_license_type = RefdataCategory.lookupOrCreate('License Type', 'Template');
        def qparams = [template_license_type]
        def public_flag = RefdataCategory.lookupOrCreate('YN', 'No');

       // This query used to allow institutions to copy their own licenses - now users only want to copy template licenses
        // (OS License specs)
        // def qry = "from License as l where ( ( l.type = ? ) OR ( exists ( select ol from OrgRole as ol where ol.lic = l AND ol.org = ? and ol.roleType = ? ) ) OR ( l.isPublic=? ) ) AND l.status.value != 'Deleted'"

        def query = "from License as l where l.type = ? AND l.status.value != 'Deleted'"

        if (params.filter) {
            query += " and lower(l.reference) like ?"
            qparams.add("%${params.filter.toLowerCase()}%")
        }

        //separately select all licenses that are not public or are null, to test access rights.
        // For some reason that I could track, l.isPublic != 'public-yes' returns different results.
        def non_public_query = query + " and ( l.isPublic = ? or l.isPublic is null) "

        if ((params.sort != null) && (params.sort.length() > 0)) {
            query += " order by l.${params.sort} ${params.order}"
        } else {
            query += " order by sortableReference asc"
        }

        println qparams
        result.numLicenses = License.executeQuery("select count(l) ${query}", qparams)[0]
        result.licenses = License.executeQuery("select l ${query}", qparams,[max: result.max, offset: result.offset])

        //We do the following to remove any licenses the user does not have access rights
        qparams += public_flag

        def nonPublic = License.executeQuery("select l ${non_public_query}", qparams)
        def no_access = nonPublic.findAll{ ! it.hasPerm("view", result.user)  }

        result.licenses = result.licenses - no_access
        result.numLicenses = result.numLicenses - no_access.size()

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def currentProviders() {
        def result = setResultGenerics()

        def role_sub            = RefdataValue.getByValueAndCategory('Subscriber','Organisational Role')
        def role_sub_cons       = RefdataValue.getByValueAndCategory('Subscriber_Consortial','Organisational Role')
        def role_sub_consortia  = RefdataValue.getByValueAndCategory('Subscription Consortia','Organisational Role')

        def mySubs = Subscription.executeQuery( """
            select s from Subscription as s join s.orgRelations as ogr where 
                ( s.status.value != 'Deleted' ) and
                ( s = ogr.sub and ogr.org = :subOrg ) and
                ( ogr.roleType = (:roleSub) or ogr.roleType = (:roleSubCons) or ogr.roleType = (:roleSubConsortia) )
        """, [subOrg: contextService.getOrg(), roleSub: role_sub, roleSubCons: role_sub_cons, roleSubConsortia: role_sub_consortia])

        result.orgList = []
        mySubs.each { sub ->
            def provider = OrgRole.findWhere(
                    sub: sub,
                    roleType: RefdataValue.getByValueAndCategory('Provider','Organisational Role')
            )
            if (provider && ! result.orgList.contains(provider.org)) {
                result.orgList << provider.org
            }
        }
        result.orgList.sort{a, b -> a.name.compareToIgnoreCase b.name}
        result.test = mySubs
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def currentSubscriptions() {
        def result = setResultGenerics()

        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        result.availableConsortia = Combo.executeQuery("select c.toOrg from Combo as c where c.fromOrg = ?", [result.institution])

        def viableOrgs = []

        if ( result.availableConsortia ){
          result.availableConsortia.each {
            viableOrgs.add(it)
          }
        }

        viableOrgs.add(result.institution)

        def date_restriction = null;
        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        if (params.validOn == null) {
            result.validOn = sdf.format(new Date(System.currentTimeMillis()))
            date_restriction = sdf.parse(result.validOn)
        } else if (params.validOn.trim() == '') {
            result.validOn = ""
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        /*
        def dateBeforeFilter = null;
        def dateBeforeFilterVal = null;
        if(params.dateBeforeFilter && params.dateBeforeVal){
            if(params.dateBeforeFilter == "renewalDate"){
                dateBeforeFilter = " and s.manualRenewalDate < :date_before"
                dateBeforeFilterVal =sdf.parse(params.dateBeforeVal)
            }else if (params.dateBeforeFilter == "endDate"){
                dateBeforeFilter = " and s.endDate < :date_before"
                dateBeforeFilterVal =sdf.parse(params.dateBeforeVal)
            }else{
              result.remove('dateBeforeFilterVal')
              result.remove('dateBeforeFilter')
            }

        }
        */

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')



        def role_sub            = RefdataValue.getByValueAndCategory('Subscriber','Organisational Role')
        def role_subCons        = RefdataValue.getByValueAndCategory('Subscriber_Consortial','Organisational Role')
        def role_sub_consortia  = RefdataValue.getByValueAndCategory('Subscription Consortia','Organisational Role')
        def roleTypes = [role_sub, role_sub_consortia]
        def role_provider        = RefdataValue.getByValueAndCategory('Provider','Organisational Role')
        def role_agency         = RefdataValue.getByValueAndCategory('Agency','Organisational Role')

        // ORG: def base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType IN (:roleTypes) AND o.org = :activeInst ) ) ) ) AND ( s.status.value != 'Deleted' ) "
        // ORG: def qry_params = ['roleTypes':roleTypes, 'activeInst':result.institution]

        def base_qry
        def qry_params

        if (! params.orgRole) {
            if (result.institution?.orgType?.value == 'Consortium') {
                params.orgRole = 'Subscription Consortia'
            }
            else {
                params.orgRole = 'Subscriber'
            }
        }

        if (params.orgRole == 'Subscriber') {

            base_qry = """
from Subscription as s where (
    exists ( select o from s.orgRelations as o where ( ( o.roleType = :roleType1 or o.roleType = :roleType2 ) AND o.org = :activeInst ) ) 
    AND ( s.status.value != 'Deleted' )
    AND (
        ( not exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) )
        or
        ( ( exists ( select o from s.orgRelations as o where o.roleType = :scRoleType ) ) AND ( s.instanceOf is not null) )
    )
)
"""
            qry_params = ['roleType1':role_sub, 'roleType2':role_subCons, 'activeInst':result.institution, 'scRoleType':role_sub_consortia]
        }

        if (params.orgRole == 'Subscription Consortia') {

            base_qry = " from Subscription as s where  ( ( exists ( select o from s.orgRelations as o where ( o.roleType = :roleType AND o.org = :activeInst ) ) ) ) AND ( s.instanceOf is null AND s.status.value != 'Deleted' ) "
            qry_params = ['roleType':role_sub_consortia, 'activeInst':result.institution]
        }

        if (params.q?.length() > 0) {
            base_qry += (
                    " and ( lower(s.name) like :name_filter " // filter by subscription
                + " or exists ( select sp from SubscriptionPackage as sp where sp.subscription = s and ( lower(sp.pkg.name) like :name_filter ) ) " // filter by pkg
                + " or exists ( select lic from License as lic where s.owner = lic and ( lower(lic.reference) like :name_filter ) ) " // filter by license
                + " or exists ( select orgR from OrgRole as orgR where orgR.sub = s and ( lower(orgR.org.name) like :name_filter"
                            + " or lower(orgR.org.shortname) like :name_filter or lower(orgR.org.sortname) like :name_filter) ) " // filter by Anbieter, Konsortium, Agency
                +  " ) "
            )

            qry_params.put('name_filter', "%${params.q.trim().toLowerCase()}%");
        }

        // eval property filter

        if (params.filterPropDef) {
            (base_qry, qry_params) = propertyService.evalFilterQuery(params, base_qry, 's', qry_params)
        }

        if (date_restriction) {
            base_qry += " and s.startDate <= :date_restr and (s.endDate >= :date_restr or s.endDate is null or s.endDate ='' )"
            qry_params.put('date_restr', date_restriction)
        }

        /* if(dateBeforeFilter ){
            base_qry += dateBeforeFilter
            qry_params.put('date_before', dateBeforeFilterVal)
        } */

        def subTypes = params.list('subTypes')
        if (subTypes) {
            subTypes = subTypes.collect{it as Long}
            base_qry += " and s.type.id in (:subTypes) "
            qry_params.put('subTypes', subTypes)
        }

        if (params.status) {
            base_qry += " and s.status.id = :status "
            qry_params.put('status', (params.status as Long))
        }

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += (params.sort=="s.name") ? " order by LOWER(${params.sort}) ${params.order}":" order by ${params.sort} ${params.order}"
        } else {
            base_qry += " order by LOWER(s.name) asc"
        }

        //log.debug("query: ${base_qry} && params: ${qry_params}")

        result.num_sub_rows = Subscription.executeQuery("select count(s) " + base_qry, qry_params)[0]
        result.subscriptions = Subscription.executeQuery("select s ${base_qry}", qry_params, [max: result.max, offset: result.offset]);
        result.date_restriction = date_restriction;

        result.propList =
                PropertyDefinition.findAllWhere(
                    descr: PropertyDefinition.SUB_PROP,
                    tenant: null // public properties
                ) +
                PropertyDefinition.findAllWhere(
                    descr: PropertyDefinition.SUB_PROP,
                    tenant: contextService.getOrg() // private properties
                )

        if (OrgCustomProperty.findByTypeAndOwner(PropertyDefinition.findByName("RequestorID"), result.institution)) {
            result.statsWibid = result.institution.getIdentifierByType('wibid')?.value
            result.usageMode = (result.institution.orgType?.value == 'Consortium') ? 'package' : 'institution'
        }

        if ( params.exportXLS=='yes' ) {
            def subscriptions = Subscription.executeQuery("select s ${base_qry}", qry_params);
            exportcurrentSubscription(subscriptions)
            return
        }

        withFormat {
            html {
                result
            }
        }
    }

    private def exportcurrentSubscription(subscriptions) {
        try {
            String[] titles = [
                    'Name', 'Vertrag', 'Verknuepfte Pakete', 'Konsortium', 'Anbieter', 'Agentur', 'Anfangsdatum', 'Enddatum', 'Status', 'Typ' ]

            def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'));
            def datetoday = sdf.format(new Date(System.currentTimeMillis()))

            HSSFWorkbook wb = new HSSFWorkbook();

            HSSFSheet sheet = wb.createSheet(g.message(code: "myinst.currentSubscriptions.label", default: "Current Subscriptions"));

            //the following three statements are required only for HSSF
            sheet.setAutobreaks(true);

            //the header row: centered text in 48pt font
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(16.75f);
            titles.eachWithIndex { titlesName, index ->
                Cell cell = headerRow.createCell(index);
                cell.setCellValue(titlesName);
            }

            //freeze the first row
            sheet.createFreezePane(0, 1);

            Row row;
            Cell cell;
            int rownum = 1;

            subscriptions.sort{it.name}
            subscriptions.each{  sub ->
                int cellnum = 0;
                row = sheet.createRow(rownum);

                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(sub.name));

                cell = row.createCell(cellnum++);
                //sub.owner.sort{it.reference}
                sub.owner.each{
                    cell.setCellValue(new HSSFRichTextString(it.reference));
                }
                cell = row.createCell(cellnum++);
                sub.packages.sort{it.pkg.name}
                def packages =""
                sub.packages.each{
                    packages += (it == sub.packages.last()) ? it.pkg.name : it.pkg.name+', ';
                }
                cell.setCellValue(new HSSFRichTextString(packages));

                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(sub.getConsortia()?.name));

                cell = row.createCell(cellnum++);
                def providername = ""
                def provider = OrgRole.findAllBySubAndRoleType(sub, RefdataValue.getByValueAndCategory('Provider', 'Organisational Role'))
                       provider.each {
                       providername += (it == provider.last()) ? it.org.name : it.org.name+", "
                }
                cell.setCellValue(new HSSFRichTextString(providername));
                cell = row.createCell(cellnum++);
                def agencyname = ""
                def agency = OrgRole.findAllBySubAndRoleType(sub, RefdataValue.getByValueAndCategory('Agency', 'Organisational Role'))
                agency.each {
                    agencyname += (it == agency.last()) ? it.org.name : it.org.name+", "
                }
                cell.setCellValue(new HSSFRichTextString(agencyname));

                cell = row.createCell(cellnum++);
                if (sub.startDate) {
                    cell.setCellValue(new HSSFRichTextString(sdf.format(sub.startDate)));
                }

                cell = row.createCell(cellnum++);
                if (sub.endDate) {
                    cell.setCellValue(new HSSFRichTextString(sdf.format(sub.endDate)));
                }

                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(sub.status?.getI10n("value")));

                cell = row.createCell(cellnum++);
                cell.setCellValue(new HSSFRichTextString(sub.type?.getI10n("value")));

                rownum++
            }

            for (int i = 0; i < 22; i++) {
                sheet.autoSizeColumn(i);
            }
            // Write the output to a file
            String file = g.message(code: "myinst.currentSubscriptions.label", default: "Current Subscriptions")+"_${datetoday}.xls";
            //if(wb instanceof XSSFWorkbook) file += "x";

            response.setHeader "Content-disposition", "attachment; filename=\"${file}\""
            // response.contentType = 'application/xls'
            response.contentType = 'application/vnd.ms-excel'
            wb.write(response.outputStream)
            response.outputStream.flush()

        }
        catch ( Exception e ) {
            log.error("Problem",e);
            response.sendError(500)
        }
    }

    @Deprecated
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def addSubscription() {
        def result = setResultGenerics()

        def date_restriction = null;
        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        if (params.validOn == null) {
            result.validOn = sdf.format(new Date(System.currentTimeMillis()))
            date_restriction = sdf.parse(result.validOn)
        } else if (params.validOn.trim() == '') {
            result.validOn = "" 
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
        }

        // if ( ! permissionHelperService.hasUserWithRole(result.user, result.institution, 'INST_ADM') ) {
        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.currentSubscriptions.no_permission', args:[result.institution.name]);
            response.sendError(401)
            result.is_inst_admin = false;
            // render(status: '401', text:"You do not have permission to add subscriptions to ${result.institution.name}. Please request editor access on the profile page");
            return;
        }

        result.is_inst_admin = result.user?.hasAffiliation('INST_ADM')

        def public_flag = RefdataCategory.lookupOrCreate('YN', 'Yes');

        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        // def base_qry = " from Subscription as s where s.type.value = 'Subscription Offered' and s.isPublic=?"
        def qry_params = []
        def base_qry = " from Package as p where lower(p.name) like ?"

        if (params.q == null) {
            qry_params.add("%");
        } else {
            qry_params.add("%${params.q.trim().toLowerCase()}%");
        }

        if (date_restriction) {
            base_qry += " and p.startDate <= ? and p.endDate >= ? "
            qry_params.add(date_restriction)
            qry_params.add(date_restriction)
        }

        // Only list subscriptions where the user has view perms against the org
        // base_qry += "and ( ( exists select or from OrgRole where or.org =? and or.user = ? and or.perms.contains'view' ) "

        // Or the user is a member of an org who has a consortial membership that has view perms
        // base_qry += " or ( 2==2 ) )"

        if ((params.sort != null) && (params.sort.length() > 0)) {
            base_qry += " order by ${params.sort} ${params.order}"
        } else {
            base_qry += " order by p.name asc"
        }

        result.num_pkg_rows = Package.executeQuery("select count(p) " + base_qry, qry_params)[0]
        result.packages = Package.executeQuery("select p ${base_qry}", qry_params, [max: result.max, offset: result.offset]);

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def emptySubscription() {
        def result = setResultGenerics()
        result.orgType = result.institution.orgType
        
        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        if (result.editable) {
            def cal = new java.util.GregorianCalendar()
            def sdf = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

            cal.setTimeInMillis(System.currentTimeMillis())
            cal.set(Calendar.MONTH, Calendar.JANUARY)
            cal.set(Calendar.DAY_OF_MONTH, 1)
            result.defaultStartYear = sdf.format(cal.getTime())
            cal.set(Calendar.MONTH, Calendar.DECEMBER)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            result.defaultEndYear = sdf.format(cal.getTime())
            result.defaultSubIdentifier = java.util.UUID.randomUUID().toString()

            if(result.orgType?.value == 'Consortium') {
                def fsq = filterService.getOrgComboQuery(params, result.institution)
                result.cons_members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            }

            result
        } else {
            redirect action: 'currentSubscriptions'
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processEmptySubscription() {
        log.debug(params)
        def result = setResultGenerics()
        result.orgType = RefdataValue.get(params.asOrgType)

        def role_sub = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber')
        def role_sub_cons = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber_Consortial')
        def role_cons = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscription Consortia')
        
        def orgRole = null
        def subType = null
        
        log.debug("found orgType ${result.orgType}")
        
        if(result.orgType?.value == 'Consortium') {
            orgRole = role_cons
            subType = RefdataValue.getByValueAndCategory('Consortial Licence', 'Subscription Type')
        }
        else {
            orgRole = role_sub
            subType = RefdataValue.getByValueAndCategory('Local Licence', 'Subscription Type')
        }

        if (accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')) {

            def sdf = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
            def startDate = sdf.parse(params.valid_from)
            def endDate = params.valid_to ? sdf.parse(params.valid_to) : null


            def new_sub = new Subscription(
                    type: subType,
                    status: RefdataCategory.lookupOrCreate('Subscription Status', 'Current'),
                    name: params.newEmptySubName,
                    startDate: startDate,
                    endDate: endDate,
                    identifier: params.newEmptySubId,
                    isPublic: RefdataCategory.lookupOrCreate('YN', 'No'),
                    impId: java.util.UUID.randomUUID().toString())

            if (new_sub.save()) {
                def new_sub_link = new OrgRole(org: result.institution,
                        sub: new_sub,
                        roleType: orgRole).save();
                        
                // if(result.orgType?.value == 'Consortium' && params.linkToAll == "Y"){ // old code

                if(result.orgType?.value == 'Consortium') {
                    
                    def cons_members = []

                    params.list('selectedOrgs').each{ it ->
                        def fo =  Org.findById(Long.valueOf(it))
                        cons_members << Combo.executeQuery(
                                "select c.fromOrg from Combo as c where c.toOrg = ? and c.fromOrg = ?",
                                [result.institution, fo] )
                    }

                    //def cons_members = Combo.executeQuery("select c.fromOrg from Combo as c where c.toOrg = ?", [result.institution])

                    cons_members.each { cm ->

                    if (params.generateSlavedSubs == "Y") {
                        log.debug("Generating seperate slaved instances for consortia members")
                        def postfix = cm.get(0).shortname ?: cm.get(0).name

                        def cons_sub = new Subscription(
                                            // type: RefdataValue.findByValue("Subscription Taken"),
                                          type: subType,
                                          status: RefdataCategory.lookupOrCreate('Subscription Status', 'Current'),
                                          name: params.newEmptySubName + " (${postfix})",
                                          startDate: startDate,
                                          endDate: endDate,
                                          identifier: java.util.UUID.randomUUID().toString(),
                                          instanceOf: new_sub,
                                          isSlaved: RefdataCategory.lookupOrCreate('YN', 'Yes'),
                                          isPublic: RefdataCategory.lookupOrCreate('YN', 'No'),
                                          impId: java.util.UUID.randomUUID().toString()).save()
                                          
                        new OrgRole(org: cm,
                            sub: cons_sub,
                            roleType: role_sub_cons).save();

                        new OrgRole(org: result.institution,
                            sub: cons_sub,
                            roleType: role_cons).save();
                    }
                    else {
                        new OrgRole(org: cm,
                            sub: new_sub,
                            roleType: role_sub_cons).save();
                    }
                  }
                }

                if (params.newEmptySubId) {
                  def sub_id_components = params.newEmptySubId.split(':');
                  if ( sub_id_components.length == 2 ) {
                    def sub_identifier = Identifier.lookupOrCreateCanonicalIdentifier(sub_id_components[0],sub_id_components[1]);
                    new_sub.ids.add(sub_identifier);
                  }
                  else {
                    def sub_identifier = Identifier.lookupOrCreateCanonicalIdentifier('Unknown',params.newEmptySubId);
                    new_sub.ids.add(sub_identifier);
                  }
                }

                redirect controller: 'subscriptionDetails', action: 'show', id: new_sub.id
            } else {
                new_sub.errors.each { e ->
                    log.debug("Problem creating new sub: ${e}");
                }
                flash.error = new_sub.errors
                redirect action: 'emptySubscription'
            }
        } else {
            redirect action: 'currentSubscriptions'
        }
    }

    @Deprecated
    def buildQuery(params) {
        log.debug("BuildQuery...");

        StringWriter sw = new StringWriter()

        if (params?.q?.length() > 0)
            sw.write(params.q)
        else
            sw.write("*:*")

        reversemap.each { mapping ->

            // log.debug("testing ${mapping.key}");

            if (params[mapping.key] != null) {
                if (params[mapping.key].class == java.util.ArrayList) {
                    params[mapping.key].each { p ->
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${p}\"")
                    }
                } else {
                    // Only add the param if it's length is > 0 or we end up with really ugly URLs
                    // II : Changed to only do this if the value is NOT an *
                    if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${params[mapping.key]}\"")
                    }
                }
            }
        }

        sw.write(" AND type:\"Subscription Offered\"");
        def result = sw.toString();
        result;
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processEmptyLicense() {
        def user = User.get(springSecurityService.principal.id)
        def org = contextService.getOrg()

        if (! accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR')) {
            flash.error = message(code:'myinst.error.noAdmin', args:[org.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${org.name}. Please request access on the profile page");
            return;
        }

        def baseLicense = params.baselicense ? License.get(params.baselicense) : null;
        //Nur wenn von Vorlage ist
        if (baseLicense) {
            if (!baseLicense?.hasPerm("view", user)) {
                log.debug("return 401....");
                flash.error = message(code: 'myinst.newLicense.error', default: 'You do not have permission to view the selected license. Please request access on the profile page');
                response.sendError(401)

            }
            else {
                def copyLicense = institutionsService.copyLicense(baseLicense, params)
                if (copyLicense.hasErrors()) {
                    log.error("Problem saving license ${copyLicense.errors}");
                    render view: 'editLicense', model: [licenseInstance: copyLicense]
                } else {
                    copyLicense.reference = params.licenseName
                    copyLicense.startDate = parseDate(params.licenseStartDate,possible_date_formats)
                    copyLicense.endDate = parseDate(params.licenseEndDate,possible_date_formats)

                    if (!copyLicense.save(flush: true)) {
                    }

                    flash.message = message(code: 'license.created.message')
                    redirect controller: 'licenseDetails', action: 'show', params: params, id: copyLicense.id
                    return
                }
            }
        }

        def license_type = RefdataCategory.lookupOrCreate('License Type', 'Actual')
        def license_status = RefdataCategory.lookupOrCreate('License Status', 'Current')

        def licenseInstance = new License(type: license_type, status: license_status, reference: params.licenseName ?:null,
                startDate:params.licenseStartDate ? parseDate(params.licenseStartDate,possible_date_formats) : null,
                endDate: params.licenseEndDate ? parseDate(params.licenseEndDate,possible_date_formats) : null,)

        if (!licenseInstance.save(flush: true)) {
        } else {
            log.debug("Save ok");
            def licensee_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee')
            def lic_cons_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensing Consortium')

            log.debug("adding org link to new license");

            if (params.asOrgType && RefdataValue.get(params.asOrgType)?.value == 'Consortium') {
                org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: lic_cons_role))
            } else {
                org.links.add(new OrgRole(lic: licenseInstance, org: org, roleType: licensee_role))
            }

            if (org.save(flush: true)) {
            } else {
                log.error("Problem saving org links to license ${org.errors}");
            }
            if(params.sub) {
                def subInstance = Subscription.get(params.sub)
                subInstance.owner = licenseInstance
                subInstance.save(flush: true)
            }

            flash.message = message(code: 'license.created.message')
            redirect controller: 'licenseDetails', action: 'show', params: params, id: licenseInstance.id
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    @Deprecated
    def newLicense_DEPR(params) {
        def user = User.get(springSecurityService.principal.id)
        def org = contextService.getOrg()

        if (! accessService.checkMinUserOrgRole(user, org, 'INST_EDITOR')) {
            flash.error = message(code:'myinst.error.noAdmin', args:[org.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${org.name}. Please request access on the profile page");
            return;
        }

        def baseLicense = params.baselicense ? License.get(params.baselicense) : null;

        if (! baseLicense?.hasPerm("view", user)) {
            log.debug("return 401....");
            flash.error = message(code:'myinst.newLicense.error', default:'You do not have permission to view the selected license. Please request access on the profile page');
            response.sendError(401)

        }
        else {
            def copyLicense = institutionsService.copyLicense(baseLicense, params)
            if (copyLicense.hasErrors() ) {
                log.error("Problem saving license ${copyLicense.errors}");
                render view: 'editLicense', model: [licenseInstance: copyLicense]
            }else{
                flash.message = message(code: 'license.created.message')
                redirect controller: 'licenseDetails', action: 'show', params: params, id: copyLicense.id
            }
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteLicense(params) {
        log.debug("deleteLicense ${params}");
        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${result.institution.name}. Please request access on the profile page");
            return;
        }

        def license = License.get(params.baselicense)


        if (license?.hasPerm("edit", result.user)) {
            def current_subscription_status = RefdataCategory.lookupOrCreate('Subscription Status', 'Current');

            def subs_using_this_license = Subscription.findAllByOwnerAndStatus(license, current_subscription_status)

            if (subs_using_this_license.size() == 0) {
                def deletedStatus = RefdataCategory.lookupOrCreate('License Status', 'Deleted');
                license.status = deletedStatus
                license.save(flush: true);
            } else {
                flash.error = message(code:'myinst.deleteLicense.error', default:'Unable to delete - The selected license has attached subscriptions marked as Current')
                redirect(url: request.getHeader('referer'))
                return
            }
        } else {
            log.warn("Attempt by ${result.user} to delete license ${result.license} without perms")
            flash.message = message(code: 'license.delete.norights', default: 'You do not have edit permission for the selected license.')
            redirect(url: request.getHeader('referer'))
            return
        }

        redirect action: 'currentLicenses'
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def deleteDocuments() {
        def ctxlist = []

        log.debug("deleteDocuments ${params}");

        docstoreService.unifiedDeleteDocuments(params)

        redirect controller: 'licenseDetails', action: 'show', id: params.licid /*, fragment: 'docstab' */
    }

    @Deprecated
    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processAddSubscription() {

        def user = User.get(springSecurityService.principal.id)
        def institution = contextService.getOrg()

        if (! accessService.checkUserIsMember(user, institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${institution.name}. Please request access on the profile page");
            return;
        }

        log.debug("processAddSubscription ${params}");

        def basePackage = Package.get(params.packageId);

        if (basePackage) {
            //
            def add_entitlements = (params.createSubAction == 'copy' ? true : false)

            def new_sub = basePackage.createSubscription("Subscription Taken",
                    "A New subscription....",
                    "A New subscription identifier.....",
                    basePackage.startDate,
                    basePackage.endDate,
                    basePackage.getConsortia(),
                    add_entitlements)

            def new_sub_link = new OrgRole(org: institution, sub: new_sub, roleType: RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber')).save();

            // This is done by basePackage.createSubscription
            // def new_sub_package = new SubscriptionPackage(subscription: new_sub, pkg: basePackage).save();

            flash.message = message(code: 'subscription.created.message', args: [message(code: 'subscription.label', default: 'Package'), basePackage.id])
            redirect controller: 'subscriptionDetails', action: 'index', params: params, id: new_sub.id
        } else {
            flash.message = message(code: 'subscription.unknown.message',default: "Subscription not found")
            redirect action: 'addSubscription', params: params
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def currentTitles() {
        // define if we're dealing with a HTML request or an Export (i.e. XML or HTML)
        boolean isHtmlOutput = !params.format || params.format.equals("html")

        def result = setResultGenerics()
        result.transforms = grailsApplication.config.titlelistTransforms
        
        result.availableConsortia = Combo.executeQuery("select c.toOrg from Combo as c where c.fromOrg = ?", [result.institution])
        
        def viableOrgs = []
        
        if(result.availableConsortia){
          result.availableConsortia.each {
            viableOrgs.add(it.id)
          }
        }
        
        viableOrgs.add(result.institution.id)
        
        log.debug("Viable Org-IDs are: ${viableOrgs}, active Inst is: ${result.institution.id}")

        // Set Date Restriction
        def date_restriction = null;

        def sdf = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'));
        if (params.validOn == null) {
            result.validOn = sdf.format(new Date(System.currentTimeMillis()))
            date_restriction = sdf.parse(result.validOn)
            log.debug("Getting titles as of ${date_restriction} (current)")
        } else if (params.validOn.trim() == '') {
            result.validOn = "" 
        } else {
            result.validOn = params.validOn
            date_restriction = sdf.parse(params.validOn)
            log.debug("Getting titles as of ${date_restriction} (given)")
        }
        
        // Set is_inst_admin
        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')

        // Set offset and max
        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        def filterSub = params.list("filterSub")
        if (filterSub.contains("all")) filterSub = null
        def filterPvd = params.list("filterPvd")
        if (filterPvd.contains("all")) filterPvd = null
        def filterHostPlat = params.list("filterHostPlat")
        if (filterHostPlat.contains("all")) filterHostPlat = null
        def filterOtherPlat = params.list("filterOtherPlat")
        if (filterOtherPlat.contains("all")) filterOtherPlat = null

        def limits = (isHtmlOutput) ? [readOnly:true,max: result.max, offset: result.offset] : [offset: 0]
        def del_sub = RefdataCategory.lookupOrCreate('Subscription Status', 'Deleted')
        def del_ie =  RefdataCategory.lookupOrCreate('Entitlement Issue Status','Deleted');

        def role_sub        = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber');
        def role_sub_cons   = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber_Consortial');

        def role_sub_consortia = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscription Consortia');
        def role_pkg_consortia = RefdataCategory.lookupOrCreate('Organisational Role', 'Package Consortia');
        def roles = [role_sub.id,role_sub_consortia.id,role_pkg_consortia.id]
        
        log.debug("viable roles are: ${roles}")
        log.debug("Using params: ${params}")
        
        def qry_params = [
                max:result.max,
                offset:result.offset,
                institution: result.institution.id,
                del_sub:del_sub.id,
                del_ie:del_ie.id,
                role_sub: role_sub.id,
                role_sub_cons: role_sub_cons.id,
                role_cons: role_sub_consortia.id]


        def sub_qry = "from issue_entitlement ie INNER JOIN subscription sub on ie.ie_subscription_fk=sub.sub_id inner join org_role orole on sub.sub_id=orole.or_sub_fk, title_instance_package_platform tipp inner join title_instance ti  on tipp.tipp_ti_fk=ti.ti_id cross join title_instance ti2 "
        if (filterOtherPlat) {
            sub_qry += "INNER JOIN platformtipp ap on ap.tipp_id = tipp.tipp_id "
        }
        if (filterPvd) {
            sub_qry += "INNER JOIN org_role orgrole on orgrole.or_pkg_fk=tipp.tipp_pkg_fk "
        }
        sub_qry += "WHERE ie.ie_tipp_fk=tipp.tipp_id and tipp.tipp_ti_fk=ti2.ti_id and ( orole.or_roletype_fk = :role_sub or orole.or_roletype_fk = :role_sub_cons or orole.or_roletype_fk = :role_cons ) "
        sub_qry += "AND orole.or_org_fk = :institution "
        sub_qry += "AND (sub.sub_status_rv_fk is null or sub.sub_status_rv_fk <> :del_sub) "
        sub_qry += "AND (ie.ie_status_rv_fk is null or ie.ie_status_rv_fk <> :del_ie ) "

        if (date_restriction) {
            sub_qry += " AND sub.sub_start_date <= :date_restriction AND sub.sub_end_date >= :date_restriction "
            qry_params.date_restriction = date_restriction
        }
        result.date_restriction = date_restriction;

        if ((params.filter) && (params.filter.length() > 0)) {
            log.debug("Adding title filter ${params.filter}");
            sub_qry += " AND LOWER(ti.ti_title) like :titlestr"
            qry_params.titlestr = "%${params.filter}%".toLowerCase();

        }

        if (filterSub) {
            sub_qry += " AND sub.sub_id in ( :subs ) "
            qry_params.subs = filterSub.join(", ")
        }

        if (filterOtherPlat) {
            sub_qry += " AND ap.id in ( :addplats )"
           qry_params.addplats = filterOtherPlat.join(", ")
        }

        if (filterHostPlat) {
            sub_qry += " AND tipp.tipp_plat_fk in ( :plats )"
            qry_params.plats = filterHostPlat.join(", ")

        }

        if (filterPvd) {
            def cp = RefdataCategory.lookupOrCreate('Organisational Role','Content Provider').id
            sub_qry += " AND orgrole.or_roletype_fk = :cprole  AND orgrole.or_org_fk IN (:provider) "
            qry_params.cprole = cp
            qry_params.provider = filterPvd.join(", ")
        }

        def having_clause = params.filterMultiIE ? 'having count(ie.ie_id) > 1' : ''
        def limits_clause = limits ? " limit :max offset :offset " : ""
        
        def order_by_clause = ''
        if (params.order == 'desc') {
            order_by_clause = 'order by ti.sort_title desc'
        } else {
            order_by_clause = 'order by ti.sort_title asc'
        }

        def sql = new Sql(dataSource)

        def queryStr = "tipp.tipp_ti_fk, count(ie.ie_id) ${sub_qry} group by tipp.tipp_ti_fk ${having_clause} ".toString()
        //If html return Titles and count
        if (isHtmlOutput) {

            result.titles = sql.rows("SELECT ${queryStr} ${order_by_clause} ${limits_clause} ".toString(),qry_params).collect{ TitleInstance.get(it.tipp_ti_fk)  }

            def queryCnt = "SELECT count(*) from (SELECT ${queryStr}) as ras".toString()
            result.num_ti_rows = sql.firstRow(queryCnt,qry_params)['count(*)']
            result = setFiltersLists(result, date_restriction)
        }else{
            //Else return IEs
            def exportQuery = "SELECT ie.ie_id, ${queryStr} order by ti.sort_title asc ".toString()
            result.entitlements = sql.rows(exportQuery,qry_params).collect { IssueEntitlement.get(it.ie_id) }
        } 


        def filename = "titles_listing_${result.institution.shortcode}"
        withFormat {
            html {
                result
            }
            csv {
                response.setHeader("Content-disposition", "attachment; filename=${filename}.csv")
                response.contentType = "text/csv"

                def out = response.outputStream
                exportService.StreamOutTitlesCSV(out, result.entitlements)
                out.close()
            }
            json {
                def map = [:]
                exportService.addTitlesToMap(map, result.entitlements)
                def content = map as JSON

                response.setHeader("Content-disposition", "attachment; filename=\"${filename}.json\"")
                response.contentType = "application/json"

                render content
            }
            xml {
                def doc = exportService.buildDocXML("TitleList")
                exportService.addTitleListXML(doc, doc.getDocumentElement(), result.entitlements)

                if ((params.transformId) && (result.transforms[params.transformId] != null)) {
                    String xml = exportService.streamOutXML(doc, new StringWriter()).getWriter().toString();
                    transformerService.triggerTransform(result.user, filename, result.transforms[params.transformId], xml, response)
                } else { // send the XML to the user
                    response.setHeader("Content-disposition", "attachment; filename=\"${filename}.xml\"")
                    response.contentType = "text/xml"
                    exportService.streamOutXML(doc, response.outputStream)
                }
            }
        }
    }

    /**
     * Add to the given result Map the list of values available for each filters.
     *
     * @param result - result Map which will be sent to the view page
     * @param date_restriction - 'Subscription valid on' date restriction as a String
     * @return the result Map with the added filter lists
     */
    private setFiltersLists(result, date_restriction) {
        // Query the list of Subscriptions
        def del_sub = RefdataCategory.lookupOrCreate('Subscription Status', 'Deleted')
        def del_ie =  RefdataCategory.lookupOrCreate('Entitlement Issue Status','Deleted');

        def role_sub            = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber');
        def role_sub_cons       = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber_Consortial');
        def role_sub_consortia  = RefdataCategory.lookupOrCreate('Organisational Role', 'Subscription Consortia');

        def cp = RefdataCategory.lookupOrCreate('Organisational Role','Content Provider')
        def role_consortia = RefdataCategory.lookupOrCreate('Organisational Role', 'Package Consortia');

        def roles = [role_sub, role_sub_cons, role_sub_consortia]
        
        def allInst = []
        if(result.availableConsortia){
          allInst = result.availableConsortia
        }
        
        allInst.add(result.institution)

        def sub_params = [institution: allInst,roles:roles,sub_del:del_sub]
        def sub_qry = """
Subscription AS s INNER JOIN s.orgRelations AS o
WHERE o.roleType IN (:roles)
AND o.org IN (:institution)
AND s.status !=:sub_del """
        if (date_restriction) {
            sub_qry += "\nAND s.startDate <= :date_restriction AND s.endDate >= :date_restriction "
            sub_params.date_restriction = date_restriction
        }
        result.subscriptions = Subscription.executeQuery("SELECT s FROM ${sub_qry} ORDER BY s.name", sub_params);

        // Query the list of Providers
        result.providers = Subscription.executeQuery("\
SELECT Distinct(role.org) FROM SubscriptionPackage sp INNER JOIN sp.pkg.orgs AS role \
WHERE EXISTS ( FROM ${sub_qry} AND sp.subscription = s ) \
AND role.roleType=:role_cp \
ORDER BY role.org.name", sub_params+[role_cp:cp]);

        // Query the list of Host Platforms
        result.hostplatforms = IssueEntitlement.executeQuery("""
SELECT distinct(ie.tipp.platform)
FROM IssueEntitlement AS ie, ${sub_qry}
AND s = ie.subscription
ORDER BY ie.tipp.platform.name""", sub_params);

        // Query the list of Other Platforms
        result.otherplatforms = IssueEntitlement.executeQuery("""
SELECT distinct(p.platform)
FROM IssueEntitlement AS ie
  INNER JOIN ie.tipp.additionalPlatforms as p,
  ${sub_qry}
AND s = ie.subscription
ORDER BY p.platform.name""", sub_params);

        return result
    }

    /**
     * This function will gather the different filters from the request parameters and
     * will build the base of the query to gather all the information needed for the view page
     * according to the requested filtering.
     *
     * @param institution - the {@link #com.k_int.kbplus.Org Org} object representing the institution we're looking at
     * @param date_restriction - 'Subscription valid on' date restriction as a String
     * @return a Map containing the base query as a String and a Map containing the parameters to run the query
     */
    private buildCurrentTitlesQuery(institution, date_restriction) {
        def qry_map = [:]

        // Put multi parameters for filtering into Lists
        // Set the variables to null if filter is equal to 'all'
        def filterSub = params.list("filterSub")
        if (filterSub.contains("all")) filterSub = null
        def filterPvd = params.list("filterPvd")
        if (filterPvd.contains("all")) filterPvd = null
        def filterHostPlat = params.list("filterHostPlat")
        if (filterHostPlat.contains("all")) filterHostPlat = null
        def filterOtherPlat = params.list("filterOtherPlat")
        if (filterOtherPlat.contains("all")) filterOtherPlat = null

        def qry_params = [:]

        StringBuilder title_query = new StringBuilder()
        title_query.append("FROM IssueEntitlement AS ie ")
        // Join with Org table if there are any Provider filters
        if (filterPvd) title_query.append("INNER JOIN ie.tipp.pkg.orgs AS role ")
        // Join with the Platform table if there are any Host Platform filters
        if (filterHostPlat) title_query.append("INNER JOIN ie.tipp.platform AS hplat ")
        title_query.append(", Subscription AS s INNER JOIN s.orgRelations AS o ")

        // Main query part
        title_query.append("\
  WHERE ( o.roleType.value = 'Subscriber' or o.roleType.value = 'Subscriber_Consortial' ) \
  AND o.org = :institution \
  AND s.status.value != 'Deleted' \
  AND s = ie.subscription ")
        qry_params.institution = institution

        // Subscription filtering
        if (filterSub) {
            title_query.append("\
  AND ( \
  ie.subscription.id IN (:subscriptions) \
  OR ( EXISTS ( FROM IssueEntitlement AS ie2 \
  WHERE ie2.tipp.title = ie.tipp.title \
  AND ie2.subscription.id IN (:subscriptions) \
  )))")
            qry_params.subscriptions = filterSub.collect(new ArrayList<Long>()) { Long.valueOf(it) }
        }

        // Title name filtering
        // Copied from SubscriptionDetailsController
        if (params.filter) {
            title_query.append("\
  AND ( ( Lower(ie.tipp.title.title) like :filterTrim ) \
  OR ( EXISTS ( FROM IdentifierOccurrence io \
  WHERE io.ti.id = ie.tipp.title.id \
  AND io.identifier.value like :filter ) ) )")
            qry_params.filterTrim = "%${params.filter.trim().toLowerCase()}%"
            qry_params.filter = "%${params.filter}%"
        }

        // Provider filtering
        if (filterPvd) {
            title_query.append("\
  AND role.roleType.value = 'Content Provider' \
  AND role.org.id IN (:provider) ")
            qry_params.provider = filterPvd.collect(new ArrayList<Long>()) { Long.valueOf(it) }
            //Long.valueOf(params.filterPvd)
        }
        // Host Platform filtering
        if (filterHostPlat) {
            title_query.append("AND hplat.id IN (:hostPlatform) ")
            qry_params.hostPlatform = filterHostPlat.collect(new ArrayList<Long>()) { Long.valueOf(it) }
            //Long.valueOf(params.filterHostPlat)
        }
        // Host Other filtering
        if (filterOtherPlat) {
            title_query.append("""
AND EXISTS (
  FROM IssueEntitlement ie2
  WHERE EXISTS (
    FROM ie2.tipp.additionalPlatforms AS ap
    WHERE ap.platform.id IN (:otherPlatform)
  )
  AND ie2.tipp.title = ie.tipp.title
) """)
            qry_params.otherPlatform = filterOtherPlat.collect(new ArrayList<Long>()) { Long.valueOf(it) }
            //Long.valueOf(params.filterOtherPlat)
        }
        // 'Subscription valid on' filtering
        if (date_restriction) {
            title_query.append(" AND ie.subscription.startDate <= :date_restriction AND ie.subscription.endDate >= :date_restriction ")
            qry_params.date_restriction = date_restriction
        }

        title_query.append("AND ( ie.status.value != 'Deleted' ) ")

        qry_map.query = title_query.toString()
        qry_map.parameters = qry_params
        return qry_map
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def availableLicenses() {
        // def sub = resolveOID(params.elementid);
        // OrgRole.findAllByOrgAndRoleType(result.institution, licensee_role).collect { it.lic }


        def user = User.get(springSecurityService.principal.id)
        def institution = contextService.getOrg()

        if (! accessService.checkUserIsMember(user, institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[institution.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${institution.name}. Please request access on the profile page");
            return;
        }

        def licensee_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee');
        def licensee_cons_role = RefdataCategory.lookupOrCreate('Organisational Role', 'Licensee_Consortial');

        // Find all licenses for this institution...
        def result = [:]
        OrgRole.findAllByOrg(institution).each { it ->
            if (it.roleType in [licensee_role, licensee_cons_role]) {
                if (it.lic?.status?.value != 'Deleted') {
                    result["License:${it.lic?.id}"] = it.lic?.reference
                }
            }
        }

        //log.debug("returning ${result} as available licenses");
        render result as JSON
    }

    def resolveOID(oid_components) {
        def result = null;
        def domain_class = grailsApplication.getArtefact('Domain', "com.k_int.kbplus.${oid_components[0]}")
        if (domain_class) {
            result = domain_class.getClazz().get(oid_components[1])
        }
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def actionCurrentSubscriptions() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        def subscription = Subscription.get(params.basesubscription)
        def inst = Org.get(params.curInst)
        def deletedStatus = RefdataCategory.lookupOrCreate('Subscription Status', 'Deleted');

        if (subscription.hasPerm("edit", result.user)) {
            def derived_subs = Subscription.findByInstanceOfAndStatusNot(subscription, deletedStatus)

            if (!derived_subs) {
              log.debug("Current Institution is ${inst}, sub has consortium ${subscription.consortia}")
              if( subscription.consortia && subscription.consortia != inst ) {
                OrgRole.executeUpdate("delete from OrgRole where sub = ? and org = ?",[subscription, inst])
              } else {
                subscription.status = deletedStatus
                subscription.save(flush: true);
              }
            } else {
                flash.error = message(code:'myinst.actionCurrentSubscriptions.error', default:'Unable to delete - The selected license has attached subscriptions')
            }
        } else {
            log.warn("${result.user} attempted to delete subscription ${result.subscription} without perms")
            flash.message = message(code: 'subscription.delete.norights')
        }

        redirect action: 'currentSubscriptions'
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewalsSearch() {

        log.debug("renewalsSearch : ${params}");
        log.debug("Start year filters: ${params.startYear}");


        // Be mindful that the behavior of this controller is strongly influenced by the schema setup in ES.
        // Specifically, see KBPlus/import/processing/processing/dbreset.sh for the mappings that control field type and analysers
        // Internal testing with http://localhost:9200/kbplus/_search?q=subtype:'Subscription%20Offered'
        def result = [:]

        result.institution = contextService.getOrg()
        result.user = springSecurityService.getCurrentUser()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[(result?.institution?.name?: message(code:'myinst.error.noMember.ph', default:'the selected institution'))]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${result.institution.name}. Please request access on the profile page");
            return;
        }

        if (params.searchreset) {
            params.remove("pkgname")
            params.remove("search")
        }

        def shopping_basket = UserFolder.findByUserAndShortcode(result.user, 'RenewalsBasket') ?: new UserFolder(user: result.user, shortcode: 'RenewalsBasket').save();

        shopping_basket.save(flush: true)

        if (params.addBtn) {
            log.debug("Add item ${params.addBtn} to basket");
            def oid = "com.k_int.kbplus.Package:${params.addBtn}"
            shopping_basket.addIfNotPresent(oid)
            shopping_basket.save(flush: true);
        } else if (params.clearBasket == 'yes') {
            log.debug("Clear basket....");
            shopping_basket.removePackageItems();
            shopping_basket.save(flush: true)
        } else if (params.clearOnlyoneitemBasket) {
            log.debug("Clear item ${params.clearOnlyoneitemBasket} from basket ");
            def oid = "com.k_int.kbplus.Package:${params.clearOnlyoneitemBasket}"
            shopping_basket.removeItem(oid)
            shopping_basket.save(flush: true)
        } else if (params.generate == 'yes') {
            log.debug("Generate");
            generate(materialiseFolder(shopping_basket.items), result.institution)
            return
        }

        result.basket = materialiseFolder(shopping_basket.items)


        //Following are the ES stuff
        try {
            StringWriter sw = new StringWriter()
            def fq = null;
            boolean has_filter = false
            //This handles the facets.
            params.each { p ->
                if (p.key.startsWith('fct:') && p.value.equals("on")) {
                    log.debug("start year ${p.key} : -${p.value}-");

                    if (!has_filter)
                        has_filter = true;
                    else
                        sw.append(" AND ");

                    String[] filter_components = p.key.split(':');
                    switch (filter_components[1]) {
                        case 'consortiaName':
                            sw.append('consortiaName')
                            break;
                        case 'startYear':
                            sw.append('startYear')
                            break;
                        case 'endYear':
                            sw.append('endYear')
                            break;
                        case 'cpname':
                            sw.append('cpname')
                            break;
                    }
                    if (filter_components[2].indexOf(' ') > 0) {
                        sw.append(":\"");
                        sw.append(filter_components[2])
                        sw.append("\"");
                    } else {
                        sw.append(":");
                        sw.append(filter_components[2])
                    }
                }
                if ((p.key.startsWith('fct:')) && p.value.equals("")) {
                    params.remove(p.key)
                }
            }

            if (has_filter) {
                fq = sw.toString();
                log.debug("Filter Query: ${fq}");
            }
            params.sort = "name"
            params.rectype = "Package" // Tells ESSearchService what to look for
            if(params.pkgname) params.q = params.pkgname
            if(fq){
                if(params.q) parms.q += " AND ";
                params.q += " (${fq}) ";
            }
            result += ESSearchService.search(params)
        }
        catch (Exception e) {
            log.error("problem", e);
        }

        result.basket.each
                {
                    if (it instanceof Subscription) result.sub_id = it.id
                }

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def selectPackages() {
        def result = [:]
        result.user = User.get(springSecurityService.principal.id)
        result.subscriptionInstance = Subscription.get(params.id)

        result.candidates = [:]
        def title_list = []
        def package_list = []

        result.titles_in_this_sub = result.subscriptionInstance.issueEntitlements.size();

        result.subscriptionInstance.issueEntitlements.each { e ->
            def title = e.tipp.title
            log.debug("Looking for packages offering title ${title.id} - ${title?.title}");

            title.tipps.each { t ->
                log.debug("  -> This title is provided by package ${t.pkg.id} on platform ${t.platform.id}");

                def title_idx = title_list.indexOf("${title.id}");
                def pkg_idx = package_list.indexOf("${t.pkg.id}:${t.platform.id}");

                if (title_idx == -1) {
                    // log.debug("  -> Adding title ${title.id} to matrix result");
                    title_list.add("${title.id}");
                    title_idx = title_list.size();
                }

                if (pkg_idx == -1) {
                    log.debug("  -> Adding package ${t.pkg.id} to matrix result");
                    package_list.add("${t.pkg.id}:${t.platform.id}");
                    pkg_idx = package_list.size();
                }

                log.debug("  -> title_idx is ${title_idx} pkg_idx is ${pkg_idx}");

                def candidate = result.candidates["${t.pkg.id}:${t.platform.id}"]
                if (!candidate) {
                    candidate = [:]
                    result.candidates["${t.pkg.id}:${t.platform.id}"] = candidate;
                    candidate.pkg = t.pkg.id
                    candidate.platform = t.platform
                    candidate.titlematch = 0
                    candidate.pkg = t.pkg
                    candidate.pkg_title_count = t.pkg.tipps.size();
                }
                candidate.titlematch++;
                log.debug("  -> updated candidate ${candidate}");
            }
        }

        log.debug("titles list ${title_list}");
        log.debug("package list ${package_list}");

        log.debug("titles list size ${title_list.size()}");
        log.debug("package list size ${package_list.size()}");
        result
    }

    private def buildRenewalsQuery(params) {
        log.debug("BuildQuery...");

        StringWriter sw = new StringWriter()

        // sw.write("subtype:'Subscription Offered'")
        sw.write("rectype:'Package'")

        renewals_reversemap.each { mapping ->

            // log.debug("testing ${mapping.key}");

            if (params[mapping.key] != null) {
                if (params[mapping.key].class == java.util.ArrayList) {
                    params[mapping.key].each { p ->
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${p}\"")
                    }
                } else {
                    // Only add the param if it's length is > 0 or we end up with really ugly URLs
                    // II : Changed to only do this if the value is NOT an *
                    if (params[mapping.key].length() > 0 && !(params[mapping.key].equalsIgnoreCase('*'))) {
                        sw.write(" AND ")
                        sw.write(mapping.value)
                        sw.write(":")
                        sw.write("\"${params[mapping.key]}\"")
                    }
                }
            }
        }


        def result = sw.toString();
        result;
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def materialiseFolder(f) {
        def result = []
        f.each {
            def item_to_add = genericOIDService.resolveOID(it.referencedOid)
            if (item_to_add) {
                result.add(item_to_add)
            } else {
                flash.message = message(code:'myinst.materialiseFolder.error', default:'Folder contains item that cannot be found');
            }
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def generate(plist, inst) {
        try {
            def m = generateMatrix(plist, inst)
            exportWorkbook(m, inst)
        }
        catch (Exception e) {
            log.error("Problem", e);
            response.sendError(500)
        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def generateMatrix(plist, inst) {

        def titleMap = [:]
        def subscriptionMap = [:]

        log.debug("pre-pre-process");

        boolean first = true;

        def formatter = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))

        // Add in JR1 and JR1a reports
        def c = new GregorianCalendar()
        c.setTime(new Date());
        def current_year = c.get(Calendar.YEAR)

        // Step one - Assemble a list of all titles and packages.. We aren't assembling the matrix
        // of titles x packages yet.. Just gathering the data for the X and Y axis
        plist.each { sub ->


            log.debug("pre-pre-process (Sub ${sub.id})");

            def sub_info = [
                    sub_idx : subscriptionMap.size(),
                    sub_name: sub.name,
                    sub_id : "${sub.class.name}:${sub.id}"
            ]

            subscriptionMap[sub.id] = sub_info

            // For each subscription in the shopping basket
            if (sub instanceof Subscription) {
                log.debug("Handling subscription: ${sub_info.sub_name}")
                sub_info.putAll([sub_startDate : sub.startDate ? formatter.format(sub.startDate):null,
                sub_endDate: sub.endDate ? formatter.format(sub.endDate) : null])
                sub.issueEntitlements.each { ie ->
                    // log.debug("IE");
                    if (!(ie.status?.value == 'Deleted')) {
                        def title_info = titleMap[ie.tipp.title.id]
                        if (!title_info) {
                            log.debug("Adding ie: ${ie}");
                            title_info = [:]
                            title_info.title_idx = titleMap.size()
                            title_info.id = ie.tipp.title.id;
                            title_info.issn = ie.tipp.title.getIdentifierValue('ISSN');
                            title_info.eissn = ie.tipp.title.getIdentifierValue('eISSN');
                            title_info.title = ie.tipp.title.title
                            if (first) {
                                if (ie.startDate)
                                    title_info.current_start_date = formatter.format(ie.startDate)
                                if (ie.endDate)
                                    title_info.current_end_date = formatter.format(ie.endDate)
                                title_info.current_embargo = ie.embargo
                                title_info.current_depth = ie.coverageDepth
                                title_info.current_coverage_note = ie.coverageNote
                                def test_coreStatus =ie.coreStatusOn(new Date())
                                def formatted_date = formatter.format(new Date())
                                title_info.core_status = test_coreStatus?"True(${formatted_date})": test_coreStatus==null?"False(Never)":"False(${formatted_date})"
                                title_info.core_status_on = formatted_date
                                title_info.core_medium = ie.coreStatus


                                /*try {
                                    log.debug("get jusp usage");
                                    title_info.jr1_last_4_years = factService.lastNYearsByType(title_info.id,
                                            inst.id,
                                            ie.tipp.pkg.contentProvider.id, 'STATS:JR1', 4, current_year)

                                    title_info.jr1a_last_4_years = factService.lastNYearsByType(title_info.id,
                                            inst.id,
                                            ie.tipp.pkg.contentProvider.id, 'STATS:JR1a', 4, current_year)
                                }
                                catch (Exception e) {
                                    log.error("Problem collating STATS report info for title ${title_info.id}", e);
                                }*/

                                // log.debug("added title info: ${title_info}");
                            }
                            titleMap[ie.tipp.title.id] = title_info;
                        }
                    }
                }
            } else if (sub instanceof Package) {
                log.debug("Adding package into renewals worksheet");
                sub.tipps.each { tipp ->
                    log.debug("Package tipp");
                    if (!(tipp.status?.value == 'Deleted')) {
                        def title_info = titleMap[tipp.title.id]
                        if (!title_info) {
                            // log.debug("Adding ie: ${ie}");
                            title_info = [:]
                            title_info.title_idx = titleMap.size()
                            title_info.id = tipp.title.id;
                            title_info.issn = tipp.title.getIdentifierValue('ISSN');
                            title_info.eissn = tipp.title.getIdentifierValue('eISSN');
                            title_info.title = tipp.title.title
                            titleMap[tipp.title.id] = title_info;
                        }
                    }
                }
            }

            first = false
        }

        log.debug("Result will be a matrix of size ${titleMap.size()} by ${subscriptionMap.size()}");

        // Object[][] result = new Object[subscriptionMap.size()+1][titleMap.size()+1]
        Object[][] ti_info_arr = new Object[titleMap.size()][subscriptionMap.size()]
        Object[] sub_info_arr = new Object[subscriptionMap.size()]
        Object[] title_info_arr = new Object[titleMap.size()]

        // Run through the list of packages, and set the X axis headers accordingly
        subscriptionMap.values().each { v ->
            sub_info_arr[v.sub_idx] = v
        }

        // Run through the titles and set the Y axis headers accordingly
        titleMap.values().each { v ->
            title_info_arr[v.title_idx] = v
        }

        // Fill out the matrix by looking through each sub/package and adding the appropriate cell info
        plist.each { sub ->
            def sub_info = subscriptionMap[sub.id]
            if (sub instanceof Subscription) {
                log.debug("Filling out renewal sheet column for an ST");
                sub.issueEntitlements.each { ie ->
                    if (!(ie.status?.value == 'Deleted')) {
                        def title_info = titleMap[ie.tipp.title.id]
                        def ie_info = [:]
                        // log.debug("Adding tipp info ${ie.tipp.startDate} ${ie.tipp.derivedFrom}");
                        ie_info.tipp_id = ie.tipp.id;
                        def test_coreStatus =ie.coreStatusOn(new Date())
                        def formatted_date = formatter.format(new Date())
                        ie_info.core_status = test_coreStatus?"True(${formatted_date})": test_coreStatus==null?"False(Never)":"False(${formatted_date})"
                        ie_info.core_status_on = formatted_date
                        ie_info.core_medium = ie.coreStatus
                        ie_info.startDate_d = ie.tipp.startDate ?: ie.tipp.derivedFrom?.startDate
                        ie_info.startDate = ie_info.startDate_d ? formatter.format(ie_info.startDate_d) : null
                        ie_info.startVolume = ie.tipp.startVolume ?: ie.tipp.derivedFrom?.startVolume
                        ie_info.startIssue = ie.tipp.startIssue ?: ie.tipp.derivedFrom?.startIssue
                        ie_info.endDate_d = ie.endDate ?: ie.tipp.derivedFrom?.endDate
                        ie_info.endDate = ie_info.endDate_d ? formatter.format(ie_info.endDate_d) : null
                        ie_info.endVolume = ie.endVolume ?: ie.tipp.derivedFrom?.endVolume
                        ie_info.endIssue = ie.endIssue ?: ie.tipp.derivedFrom?.endIssue

                        ti_info_arr[title_info.title_idx][sub_info.sub_idx] = ie_info
                    }
                }
            } else if (sub instanceof Package) {
                log.debug("Filling out renewal sheet column for a package");
                sub.tipps.each { tipp ->
                    if (!(tipp.status?.value == 'Deleted')) {
                        def title_info = titleMap[tipp.title.id]
                        def ie_info = [:]
                        // log.debug("Adding tipp info ${tipp.startDate} ${tipp.derivedFrom}");
                        ie_info.tipp_id = tipp.id;
                        ie_info.startDate_d = tipp.startDate
                        ie_info.startDate = ie_info.startDate_d ? formatter.format(ie_info.startDate_d) : null
                        ie_info.startVolume = tipp.startVolume
                        ie_info.startIssue = tipp.startIssue
                        ie_info.endDate_d = tipp.endDate
                        ie_info.endDate = ie_info.endDate_d ? formatter.format(ie_info.endDate_d) : null
                        ie_info.endVolume = tipp.endVolume ?: tipp.derivedFrom?.endVolume
                        ie_info.endIssue = tipp.endIssue ?: tipp.derivedFrom?.endIssue

                        ti_info_arr[title_info.title_idx][sub_info.sub_idx] = ie_info
                    }
                }
            }
        }

        log.debug("Completed.. returning final result");

        def final_result = [
                ti_info     : ti_info_arr,                      // A crosstab array of the packages where a title occours
                title_info  : title_info_arr,                // A list of the titles
                sub_info    : sub_info_arr,
                current_year: current_year]                  // The subscriptions offered (Packages)

        return final_result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def exportWorkbook(m, inst) {
        try {
            log.debug("export workbook");

            // read http://stackoverflow.com/questions/2824486/groovy-grails-how-do-you-stream-or-buffer-a-large-file-in-a-controllers-respon
            def date = new Date()
            def sdf = new SimpleDateFormat("dd.MM.yyyy")

            HSSFWorkbook workbook = new HSSFWorkbook();

            CreationHelper factory = workbook.getCreationHelper();

            //
            // Create two sheets in the excel document and name it First Sheet and
            // Second Sheet.
            //
            HSSFSheet firstSheet = workbook.createSheet(g.message(code: "renewalexport.renewalsworksheet", default: "Renewals Worksheet"));
            Drawing drawing = firstSheet.createDrawingPatriarch();

            // Cell style for a present TI
            HSSFCellStyle present_cell_style = workbook.createCellStyle();
            present_cell_style.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
            present_cell_style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            // Cell style for a core TI
            HSSFCellStyle core_cell_style = workbook.createCellStyle();
            core_cell_style.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
            core_cell_style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            //NOT CHANGE
            HSSFCellStyle notchange_cell_style = workbook.createCellStyle();
            notchange_cell_style.setFillForegroundColor(HSSFColor.RED.index);
            notchange_cell_style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

            int rc = 0;
            // header
            int cc = 0;
            HSSFRow row = null;
            HSSFCell cell = null;

            log.debug(m.sub_info.toString())

            // Blank rows
            row = firstSheet.createRow(rc++);
            row = firstSheet.createRow(rc++);
            cc = 0;
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.subscriberID", default: "Subscriber ID")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.subscribername", default: "Subscriber Name")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.subscriberShortcode", default: "Subscriber Shortcode")));

            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.subscriptionStartDate", default: "Subscription Startdate")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.subscriptionEndDate", default: "Subscription Enddate")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.copySubscriptionDoc", default: "Copy Subscription Doc")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.generated", default: "Generated at")));

            row = firstSheet.createRow(rc++);
            cc = 0;
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("${inst.id}"));
            cell.setCellStyle(notchange_cell_style);
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(inst.name));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(inst.shortcode));

            def subscription = m.sub_info.find{it.sub_startDate}
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("${subscription?.sub_startDate?:''}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("${subscription?.sub_endDate?:''}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("${subscription?.sub_id?:m.sub_info[0].sub_id}"));
            cell.setCellStyle(notchange_cell_style);
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("${sdf.format(date)}"));
            row = firstSheet.createRow(rc++);

            // Key
            row = firstSheet.createRow(rc++);
            cc = 0;
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.key", default: "Key")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.inSubscription", default: "In Subscription")));
            cell.setCellStyle(present_cell_style);
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.coreTitle", default: "Core Title")));
            cell.setCellStyle(core_cell_style);
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.notinSubscription", default: "Not in Subscription")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.notChange", default: "Not Change")));
            cell.setCellStyle(notchange_cell_style);
            cell = row.createCell(21);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.currentSub", default: "Current Subscription")));
            cell = row.createCell(22);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.candidates", default: "Candidates")));


            row = firstSheet.createRow(rc++);
            cc = 21
            m.sub_info.each { sub ->
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${sub.sub_id}"));
                cell.setCellStyle(notchange_cell_style);
            }

            // headings
            row = firstSheet.createRow(rc++);
            cc = 0;
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.titleID", default: "Title ID")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.title", default: "Title")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("ISSN"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("eISSN"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.currentStartDate", default: "Current Startdate")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.currentEndDate", default: "Current Enddate")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.currentCoverageDepth", default: "Current Coverage Depth")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.currentCoverageNote", default: "Current Coverage Note")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.coreStatus", default: "Core Status")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.coreStatusCheked", default: "Core Status Checked")));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString(g.message(code: "renewalexport.coreMedium", default: "Core Medium")));

            // USAGE History
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1\n${m.current_year - 4}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1a\n${m.current_year - 4}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1\n${m.current_year - 3}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1a\n${m.current_year - 3}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1\n${m.current_year - 2}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1a\n${m.current_year - 2}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1\n${m.current_year - 1}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1a\n${m.current_year - 1}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1\n${m.current_year}"));
            cell = row.createCell(cc++);
            cell.setCellValue(new HSSFRichTextString("Nationale Statistik JR1a\n${m.current_year}"));

            m.sub_info.each { sub ->
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${sub.sub_name}"));

                // Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_URL);
                // link.setAddress("http://poi.apache.org/");
                // cell.setHyperlink(link);
            }

            m.title_info.each { title ->

                row = firstSheet.createRow(rc++);
                cc = 0;

                // Internal title ID
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.id}"));
                // Title
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.title ?: ''}"));

                // ISSN
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.issn ?: ''}"));

                // eISSN
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.eissn ?: ''}"));

                // startDate
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.current_start_date ?: ''}"));

                // endDate
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.current_end_date ?: ''}"));

                // coverageDepth
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.current_depth ?: ''}"));

                // embargo
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.current_coverage_note ?: ''}"));

                // IsCore
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.core_status ?: ''}"));

                // Core Start Date
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.core_status_on ?: ''}"));

                // Core End Date
                cell = row.createCell(cc++);
                cell.setCellValue(new HSSFRichTextString("${title.core_medium ?: ''}"));

                // Usage Stats
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1_last_4_years[4] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1a_last_4_years[4] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1_last_4_years[3] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1a_last_4_years[3] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1_last_4_years[2] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1a_last_4_years[2] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1_last_4_years[1] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1a_last_4_years[1] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1_last_4_years[0] ?: '0'));
                cell = row.createCell(cc++);
                if (title.jr1_last_4_years)
                    cell.setCellValue(new HSSFRichTextString(title.jr1a_last_4_years[0] ?: '0'));

                m.sub_info.each { sub ->

                    cell = row.createCell(cc++);
                    def ie_info = m.ti_info[title.title_idx][sub.sub_idx]
                    if (ie_info) {
                        if ((ie_info.core_status) && (ie_info.core_status.contains("True"))) {
                            cell.setCellValue(new HSSFRichTextString(""));
                            cell.setCellStyle(core_cell_style);
                        } else {
                            cell.setCellValue(new HSSFRichTextString(""));
                            cell.setCellStyle(present_cell_style);
                        }
                        if (sub.sub_idx == 0) {
                            addCellComment(row, cell, "${title.title} ${g.message(code: "renewalexport.providedby", default: "provided by")} ${sub.sub_name}\n${g.message(code: "renewalexport.startDate", default: "Start Date")}:${ie_info.startDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startVolume", default: "Start Volume")}:${ie_info.startVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startIssue", default: "Start Issue")}:${ie_info.startIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endDate", default: "End Date")}:${ie_info.endDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endVolume", default: "End Volume")}:${ie_info.endVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endIssue", default: "End Issue")}:${ie_info.endIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n", drawing, factory);
                        } else {
                            addCellComment(row, cell, "${title.title} ${g.message(code: "renewalexport.providedby", default: "provided by")} ${sub.sub_name}\n${g.message(code: "renewalexport.startDate", default: "Start Date")}:${ie_info.startDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startVolume", default: "Start Volume")}:${ie_info.startVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.startIssue", default: "Start Issue")}:${ie_info.startIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endDate", default: "End Date")}:${ie_info.endDate ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endVolume", default: "End Volume")}:${ie_info.endVolume ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.endIssue", default: "End Issue")}:${ie_info.endIssue ?: g.message(code: "renewalexport.notSet", default: "Not set")}\n${g.message(code: "renewalexport.selectTitle", default: "Select Title by setting this cell to Y")}\n", drawing, factory);
                        }
                    }

                }
            }
            row = firstSheet.createRow(rc++);
            cell = row.createCell(0);
            cell.setCellValue(new HSSFRichTextString("END"));

            // firstSheet.autoSizeRow(6); //adjust width of row 6 (Headings for JUSP Stats)
//            Row jusp_heads_row = firstSheet.getRow(6);
//            jusp_heads_row.setHeight((short) (jusp_heads_row.getHeight() * 2));

            for (int i = 0; i < 22; i++) {
                firstSheet.autoSizeColumn(i); //adjust width of the first column
            }
            for (int i = 0; i < m.sub_info.size(); i++) {
                firstSheet.autoSizeColumn(22 + i); //adjust width of the second column
            }

            response.setHeader "Content-disposition", "attachment; filename=\"${g.message(code: "renewalexport.renewals", default: "Renewals")}.xls\""
            // response.contentType = 'application/xls'
            response.contentType = 'application/vnd.ms-excel'
            workbook.write(response.outputStream)
            response.outputStream.flush()
        }
        catch (Exception e) {
            log.error("Problem", e);
            response.sendError(500)
        }
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewalsnoPackageChange() {
        def result = setResultGenerics()

        if (!accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code: 'myinst.error.noMember', args: [result.institution.name]);
            response.sendError(401)
            return;
        } else if (!accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code: 'myinst.renewalUpload.error.noAdmin', default: 'Renewals Upload screen is not available to read-only users.')
            response.sendError(401)
            return;
        }

        def sdf = new java.text.SimpleDateFormat('dd.MM.yyyy')

        def subscription = Subscription.get(params.sub_id)

        result.errors = []

        result.permissionInfo = [sub_startDate: (subscription.startDate ? sdf.format(subscription.startDate) : null), sub_endDate: (subscription.endDate ? sdf.format(subscription.endDate) : null), sub_name: subscription.name, sub_id: subscription.id, sub_license: subscription?.owner?.reference?:'']

        result.entitlements = subscription.issueEntitlements

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def renewalswithoutPackage() {
        def result = setResultGenerics()

        if (!accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code: 'myinst.error.noMember', args: [result.institution.name]);
            response.sendError(401)
            return;
        } else if (!accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code: 'myinst.renewalUpload.error.noAdmin', default: 'Renewals Upload screen is not available to read-only users.')
            response.sendError(401)
            return;
        }

        def sdf = new java.text.SimpleDateFormat('dd.MM.yyyy')

        def subscription = Subscription.get(params.sub_id)

        result.errors = []

        result.permissionInfo = [sub_startDate: (subscription.startDate ? sdf.format(subscription.startDate) : null), sub_endDate: (subscription.endDate ? sdf.format(subscription.endDate) : null), sub_name: subscription.name, sub_id: subscription.id, sub_license: subscription?.owner?.reference?:'']

        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def renewalsUpload() {
        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            return;
        } else if (! accessService.checkMinUserOrgRole(result.user, result.institution, "INST_EDITOR")) {
            flash.error = message(code:'myinst.renewalUpload.error.noAdmin', default:'Renewals Upload screen is not available to read-only users.')
            response.sendError(401)
            return;
        }

        result.errors = []

        log.debug("upload");

        if (request.method == 'POST') {
            def upload_mime_type = request.getFile("renewalsWorksheet")?.contentType
            def upload_filename = request.getFile("renewalsWorksheet")?.getOriginalFilename()
            log.debug("Uploaded worksheet type: ${upload_mime_type} filename was ${upload_filename}");
            def input_stream = request.getFile("renewalsWorksheet")?.inputStream
            if (input_stream.available() != 0) {
                processRenewalUpload(input_stream, upload_filename, result)
            } else {
                flash.error = message(code:'myinst.renewalUpload.error.noSelect');
            }
        }

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processRenewalUpload(input_stream, upload_filename, result) {
        int SO_START_COL = 22
        int SO_START_ROW = 7
        log.debug("processRenewalUpload - opening upload input stream as HSSFWorkbook");
        def user = User.get(springSecurityService.principal.id)

        if (input_stream) {
            HSSFWorkbook wb;
            try {
                wb = new HSSFWorkbook(input_stream);
            } catch (IOException e) {
                if (e.getMessage().contains("Invalid header signature")) {
                    flash.error = message(code:'myinst.processRenewalUpload.error.invHeader', default:'Error creating workbook. Possible causes: document format, corrupted file.')
                } else {
                    flash.error = message(code:'myinst.processRenewalUpload.error', default:'Error creating workbook')
                }
                log.debug("Error creating workbook from input stream. ", e)
                return result;
            }
            HSSFSheet firstSheet = wb.getSheetAt(0);

            def sdf = new java.text.SimpleDateFormat('dd.MM.yyyy')

            // Step 1 - Extract institution id, name and shortcode
            HSSFRow org_details_row = firstSheet.getRow(2)
            String org_name = org_details_row?.getCell(0)?.toString()
            String org_id = org_details_row?.getCell(1)?.toString()
            String org_shortcode = org_details_row?.getCell(2)?.toString()
            String sub_startDate = org_details_row?.getCell(3)?.toString()
            String sub_endDate = org_details_row?.getCell(4)?.toString()
            String original_sub_id = org_details_row?.getCell(5)?.toString()
            def original_sub = null
            if(original_sub_id){
                original_sub = genericOIDService.resolveOID(original_sub_id)
                if(!original_sub.hasPerm("view",user)){
                    original_sub = null;
                    flash.error = message(code:'myinst.processRenewalUpload.error.access')
                }
            }
            result.permissionInfo = [sub_startDate:sub_startDate,sub_endDate:sub_endDate,sub_name:original_sub?.name?:'',sub_id:original_sub?.id?:'', sub_license: original_sub?.owner?.reference?:'']


            log.debug("Worksheet upload on behalf of ${org_name}, ${org_id}, ${org_shortcode}");

            def sub_info = []
            // Step 2 - Row 5 (6, but 0 based) contains package identifiers starting in column 4(5)
            HSSFRow package_ids_row = firstSheet.getRow(5)
            for (int i = SO_START_COL; ((i < package_ids_row.getLastCellNum()) && (package_ids_row.getCell(i))); i++) {
                log.debug("Got package identifier: ${package_ids_row.getCell(i).toString()}");
                def sub_id = package_ids_row.getCell(i).toString()
                def sub_rec = genericOIDService.resolveOID(sub_id) // Subscription.get(sub_id);
                if (sub_rec) {
                    sub_info.add(sub_rec);
                } else {
                    log.error("Unable to resolve the package identifier in row 6 column ${i + 5}, please check");
                    return
                }
            }

            result.entitlements = []

            boolean processing = true
            // Step three, process each title row, starting at row 11(10)
            for (int i = SO_START_ROW; ((i < firstSheet.getLastRowNum()) && (processing)); i++) {
                // log.debug("processing row ${i}");

                HSSFRow title_row = firstSheet.getRow(i)
                // Title ID
                def title_id = title_row.getCell(0).toString()
                if (title_id == 'END') {
                    // log.debug("Encountered END title");
                    processing = false;
                } else {
                    // log.debug("Upload Process title: ${title_id}, num subs=${sub_info.size()}, last cell=${title_row.getLastCellNum()}");
                    def title_id_long = Long.parseLong(title_id)
                    def title_rec = TitleInstance.get(title_id_long);
                    for (int j = 0; (((j + SO_START_COL) < title_row.getLastCellNum()) && (j <= sub_info.size())); j++) {
                        def resp_cell = title_row.getCell(j + SO_START_COL)
                        if (resp_cell) {
                            // log.debug("  -> Testing col[${j+SO_START_COL}] val=${resp_cell.toString()}");

                            def subscribe = resp_cell.toString()

                            // log.debug("Entry : sub:${subscribe}");

                            if (subscribe == 'Y' || subscribe == 'y') {
                                // log.debug("Add an issue entitlement from subscription[${j}] for title ${title_id_long}");

                                def entitlement_info = [:]
                                entitlement_info.base_entitlement = extractEntitlement(sub_info[j], title_id_long)
                                if (entitlement_info.base_entitlement) {
                                    entitlement_info.title_id = title_id_long
                                    entitlement_info.subscribe = subscribe

                                    entitlement_info.start_date = title_row.getCell(4)
                                    entitlement_info.end_date = title_row.getCell(5)
                                    entitlement_info.coverage = title_row.getCell(6)
                                    entitlement_info.coverage_note = title_row.getCell(7)
                                    entitlement_info.core_status = title_row.getCell(10) // Moved from 8


                                    // log.debug("Added entitlement_info ${entitlement_info}");
                                    result.entitlements.add(entitlement_info)
                                } else {
                                    log.error("TIPP not found in package.");
                                    flash.error = message(code:'myinst.processRenewalUpload.error.tipp', args:[title_id_long]);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            log.error("Input stream is null");
        }
        log.debug("Done");

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def extractEntitlement(pkg, title_id) {
        def result = pkg.tipps.find { e -> e.title?.id == title_id }
        if (result == null) {
            log.error("Failed to look up title ${title_id} in package ${pkg.name}");
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def processRenewal() {
        log.debug("-> renewalsUpload params: ${params}");
        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = message(code:'myinst.error.noMember', args:[result.institution.name]);
            response.sendError(401)
            // render(status: '401', text:"You do not have permission to access ${result.institution.name}. Please request access on the profile page");
            return;
        }

        log.debug("entitlements...[${params.ecount}]");

        int ent_count = Integer.parseInt(params.ecount);
        Calendar now = Calendar.getInstance();

        def sub_startDate = params.subscription?.copyStart ? parseDate(params.subscription?.start_date,possible_date_formats) : null
        def sub_endDate = params.subscription?.copyEnd ? parseDate(params.subscription?.end_date,possible_date_formats): null
        def copy_documents = params.subscription?.copy_docs && params.subscription.copyDocs
        def old_subOID = params.subscription.copy_docs
        def old_subname = "${params.subscription.name} ${now.get(Calendar.YEAR)+1}"

        def new_subscription = new Subscription(
                identifier: java.util.UUID.randomUUID().toString(),
                status: RefdataCategory.lookupOrCreate('Subscription Status', 'Under Consideration'),
                impId: java.util.UUID.randomUUID().toString(),
                name: old_subname ?: "Unset: Generated by import",
                startDate: sub_startDate,
                endDate: sub_endDate,
                previousSubscription: old_subOID ?: null,
                type: Subscription.get(old_subOID)?.type ?: null,
                isPublic: RefdataCategory.lookupOrCreate('YN', 'No'),
                owner: params.subscription.copyLicense ? (Subscription.get(old_subOID)?.owner) : null)
        log.debug("New Sub: ${new_subscription.startDate}  - ${new_subscription.endDate}")
        def packages_referenced = []
        Date earliest_start_date = null
        Date latest_end_date = null

        if (new_subscription.save()) {
            // assert an org-role
            def org_link = new OrgRole(org: result.institution,
                    sub: new_subscription,
                    roleType: RefdataCategory.lookupOrCreate('Organisational Role', 'Subscriber')).save();

            // Copy any links from SO
            // db_sub.orgRelations.each { or ->
            //   if ( or.roleType?.value != 'Subscriber' ) {
            //     def new_or = new OrgRole(org: or.org, sub: new_subscription, roleType: or.roleType).save();
            //   }
            // }
        } else {
            log.error("Problem saving new subscription, ${new_subscription.errors}");
        }

        new_subscription.save(flush: true);
        if(copy_documents){
            String subOID =  params.subscription.copy_docs
            def sourceOID = "${new_subscription.getClass().getName()}:${subOID}"
            docstoreService.copyDocuments(sourceOID,"${new_subscription.getClass().getName()}:${new_subscription.id}")
        }

        if (!new_subscription.issueEntitlements) {
            new_subscription.issueEntitlements = new java.util.TreeSet()
        }

        for (int i = 0; i <= ent_count; i++) {
            def entitlement = params.entitlements."${i}";
            log.debug("process entitlement[${i}]: ${entitlement} - TIPP id is ${entitlement.tipp_id}");

            def dbtipp = TitleInstancePackagePlatform.get(entitlement.tipp_id)

            if (!packages_referenced.contains(dbtipp.pkg)) {
                packages_referenced.add(dbtipp.pkg)
                def new_package_link = new SubscriptionPackage(subscription: new_subscription, pkg: dbtipp.pkg).save();
                if ((earliest_start_date == null) || (dbtipp.pkg.startDate < earliest_start_date))
                    earliest_start_date = dbtipp.pkg.startDate
                if ((latest_end_date == null) || (dbtipp.pkg.endDate > latest_end_date))
                    latest_end_date = dbtipp.pkg.endDate
            }

            if (dbtipp) {
                def live_issue_entitlement = RefdataCategory.lookupOrCreate('Entitlement Issue Status', 'Live');
                def is_core = false

                def new_core_status = null;

                switch (entitlement.core_status?.toUpperCase()) {
                    case 'Y':
                    case 'YES':
                        new_core_status = RefdataCategory.lookupOrCreate('CoreStatus', 'Yes');
                        is_core = true;
                        break;
                    case 'P':
                    case 'PRINT':
                        new_core_status = RefdataCategory.lookupOrCreate('CoreStatus', 'Print');
                        is_core = true;
                        break;
                    case 'E':
                    case 'ELECTRONIC':
                        new_core_status = RefdataCategory.lookupOrCreate('CoreStatus', 'Electronic');
                        is_core = true;
                        break;
                    case 'P+E':
                    case 'E+P':
                    case 'PRINT+ELECTRONIC':
                    case 'ELECTRONIC+PRINT':
                        new_core_status = RefdataCategory.lookupOrCreate('CoreStatus', 'Print+Electronic');
                        is_core = true;
                        break;
                    default:
                        new_core_status = RefdataCategory.lookupOrCreate('CoreStatus', 'No');
                        break;
                }

                def new_start_date = entitlement.start_date ? parseDate(entitlement.start_date, possible_date_formats) : null
                def new_end_date = entitlement.end_date ? parseDate(entitlement.end_date, possible_date_formats) : null


                // entitlement.is_core
                def new_ie = new IssueEntitlement(subscription: new_subscription,
                        status: live_issue_entitlement,
                        tipp: dbtipp,
                        startDate: new_start_date ?: dbtipp.startDate,
                        startVolume: dbtipp.startVolume,
                        startIssue: dbtipp.startIssue,
                        endDate: new_end_date ?: dbtipp.endDate,
                        endVolume: dbtipp.endVolume,
                        endIssue: dbtipp.endIssue,
                        embargo: dbtipp.embargo,
                        coverageDepth: dbtipp.coverageDepth,
                        coverageNote: dbtipp.coverageNote,
                        coreStatus: new_core_status
                )

                if (new_ie.save()) {
                    log.debug("new ie saved");
                } else {
                    new_ie.errors.each { e ->
                        log.error("Problem saving new ie : ${e}");
                    }
                }
            } else {
                log.debug("Unable to locate tipp with id ${entitlement.tipp_id}");
            }
        }
        log.debug("done entitlements...");

        new_subscription.startDate = sub_startDate ?: earliest_start_date
        new_subscription.endDate = sub_endDate ?: latest_end_date
        new_subscription.save()

        if (new_subscription)
            redirect controller: 'subscriptionDetails', action: 'index', id: new_subscription.id
        else
            redirect action: 'renewalsUpload', params: params
    }

    def addCellComment(row, cell, comment_text, drawing, factory) {

        // When the comment box is visible, have it show in a 1x3 space
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + 9);
        anchor.setRow1(row.getRowNum());
        anchor.setRow2(row.getRowNum() + 11);

        // Create the comment and set the text+author
        def comment = drawing.createCellComment(anchor);
        RichTextString str = factory.createRichTextString(comment_text);
        comment.setString(str);
        comment.setAuthor("LAS:eR System");

        // Assign the comment to the cell
        cell.setCellComment(comment);
    }

    def parseDate(datestr, possible_formats) {
        def parsed_date = null;
        if (datestr && (datestr.toString().trim().length() > 0)) {
            for (Iterator i = possible_formats.iterator(); (i.hasNext() && (parsed_date == null));) {
                try {
                    parsed_date = i.next().parse(datestr.toString());
                }
                catch (Exception e) {
                }
            }
        }
        parsed_date
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def dashboard() {

        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to access ${result.institution.name} pages. Please request access on the profile page";
            response.sendError(401)
            return;
        }

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')
        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        def pending_change_pending_status = RefdataCategory.lookupOrCreate("PendingChangeStatus", "Pending")
        getTodoForInst(result)

        //.findAllByOwner(result.user,sort:'ts',order:'asc')

        // tasks

        def sdFormat    = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
        params.taskStatus = 'not done'
        def query       = filterService.getTaskQuery(params, sdFormat)
        def contextOrg  = contextService.getOrg()
        result.tasks    = taskService.getTasksByResponsibles(springSecurityService.getCurrentUser(), contextOrg, query)
        def preCon      = taskService.getPreconditions(contextOrg)
        result.enableMyInstFormFields = true // enable special form fields
        result << preCon

        def announcement_type = RefdataCategory.lookupOrCreate('Document Type', 'Announcement')
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: 10, sort: 'dateCreated', order: 'desc'])

        result.forumActivity = zenDeskSyncService.getLatestForumActivity()

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def getTodoForInst(result){
        def lic_del = RefdataCategory.lookupOrCreate('License Status', 'Deleted');
        def sub_del = RefdataCategory.lookupOrCreate('Subscription Status', 'Deleted');
        def pkg_del = RefdataCategory.lookupOrCreate( 'Package Status', 'Deleted' );
        def pc_status = RefdataCategory.lookupOrCreate("PendingChangeStatus", "Pending")
        result.num_todos = PendingChange.executeQuery("select count(distinct pc.oid) from PendingChange as pc left outer join pc.license as lic left outer join lic.status as lic_status left outer join pc.subscription as sub left outer join sub.status as sub_status left outer join pc.pkg as pkg left outer join pkg.packageStatus as pkg_status where pc.owner = ? and (pc.status = ? or pc.status is null) and ((lic_status is null or lic_status!=?) and (sub_status is null or sub_status!=?) and (pkg_status is null or pkg_status!=?))", [result.institution,pc_status, lic_del,sub_del,pkg_del])[0]

        log.debug("Count3=${result.num_todos}");

        def change_summary = PendingChange.executeQuery("select distinct(pc.oid), count(pc), min(pc.ts), max(pc.ts) from PendingChange as pc left outer join pc.license as lic left outer join lic.status as lic_status left outer join pc.subscription as sub left outer join sub.status as sub_status left outer join pc.pkg as pkg left outer join pkg.packageStatus as pkg_status where pc.owner = ? and (pc.status = ? or pc.status is null) and ((lic_status is null or lic_status!=?) and (sub_status is null or sub_status!=?) and (pkg_status is null or pkg_status!=?)) group by pc.oid", [result.institution,pc_status,lic_del,sub_del,pkg_del], [max: result.max?:100, offset: result.offset?:0]);
        result.todos = []

        change_summary.each { cs ->
            log.debug("Change summary row : ${cs}");
            def item_with_changes = genericOIDService.resolveOID(cs[0])
            result.todos.add([
                    item_with_changes: item_with_changes,
                    oid              : cs[0],
                    num_changes      : cs[1],
                    earliest         : cs[2],
                    latest           : cs[3],
            ]);
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def changes() {
        def result = setResultGenerics()

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to view ${result.institution.name}. Please request access on the profile page";
            response.sendError(401)
            return;
        }

        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        getTodoForInst(result)

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def announcements() {
        def result = setResultGenerics()

        result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
        result.offset = params.offset ? Integer.parseInt(params.offset) : 0;

        def announcement_type = RefdataCategory.lookupOrCreate('Document Type', 'Announcement')
        result.recentAnnouncements = Doc.findAllByType(announcement_type, [max: result.max, sort: 'dateCreated', order: 'desc'])
        result.num_announcements = result.recentAnnouncements.size()

        // result.num_sub_rows = Subscription.executeQuery("select count(s) "+base_qry, qry_params )[0]
        // result.subscriptions = Subscription.executeQuery("select s ${base_qry}", qry_params, [max:result.max, offset:result.offset]);


        result
    }

    @Secured(['ROLE_YODA'])
    def changeLog() {
        def result = setResultGenerics()

        def exporting = ( params.format == 'csv' ? true : false )

        result.institutional_objects = []

        if ( exporting ) {
          result.max = 1000000;
          result.offset = 0;
        }
        else {
          result.max = params.max ? Integer.parseInt(params.max) : result.user.defaultPageSize;
          result.offset = params.offset ? Integer.parseInt(params.offset) : 0;
        }

        PendingChange.executeQuery('select distinct(pc.license) from PendingChange as pc where pc.owner = ?',[result.institution]).each {
          result.institutional_objects.add(['com.k_int.kbplus.License:'+it.id,"${message(code:'license')}: "+it.reference]);
        }
        PendingChange.executeQuery('select distinct(pc.subscription) from PendingChange as pc where pc.owner = ?',[result.institution]).each {
          result.institutional_objects.add(['com.k_int.kbplus.Subscription:'+it.id,"${message(code:'subscription')}: "+it.name]);
        }

        if ( params.restrict == 'ALL' )
          params.restrict=null

        def base_query = " from PendingChange as pc where owner = ?";
        def qry_params = [result.institution]
        if ( ( params.restrict != null ) && ( params.restrict.trim().length() > 0 ) ) {
          def o =  genericOIDService.resolveOID(params.restrict)
          if ( o != null ) {
            if ( o instanceof License ) {
              base_query += ' and license = ?'
            }
            else {
              base_query += ' and subscription = ?'
            }
            qry_params.add(o)
          }
        }

        result.num_changes = PendingChange.executeQuery("select count(pc) "+base_query, qry_params)[0];


        withFormat {
            html {
            result.changes = PendingChange.executeQuery("select pc "+base_query+"  order by ts desc", qry_params, [max: result.max, offset:result.offset])
                result
            }
            csv {
                def dateFormat = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
                def changes = PendingChange.executeQuery("select pc "+base_query+"  order by ts desc", qry_params)
                response.setHeader("Content-disposition", "attachment; filename=\"${result.institution.name}_changes.csv\"")
                response.contentType = "text/csv"

                def out = response.outputStream
                out.withWriter { w ->
                  w.write('Date,ChangeId,Actor, SubscriptionId,LicenseId,Description\n')
                  changes.each { c ->
                    def line = "\"${dateFormat.format(c.ts)}\",\"${c.id}\",\"${c.user?.displayName?:''}\",\"${c.subscription?.id ?:''}\",\"${c.license?.id?:''}\",\"${c.desc}\"\n".toString()
                    w.write(line)
                  }
                }
                out.close()
            }

        }
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def financeImport() {
      def result = setResultGenerics()

      if (! accessService.checkUserIsMember(result.user, result.institution)) {
          flash.error = "You do not have permission to view ${result.institution.name}. Please request access on the profile page";
          response.sendError(401)
          return;
      }

      def defaults = [ 'owner':result.institution];

      if (request.method == 'POST'){
        def input_stream = request.getFile("tsvfile")?.inputStream
        result.loaderResult = tsvSuperlifterService.load(input_stream,
                                                         grailsApplication.config.financialImportTSVLoaderMappings,
                                                         params.dryRun=='Y'?true:false,
                                                         defaults)
      }
      result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def tip() {
      def result = setResultGenerics()

      log.debug("tip :: ${params}")
      result.tip = TitleInstitutionProvider.get(params.id)

      if (request.method == 'POST' && result.tip ){
        log.debug("Add usage ${params}")
        def sdf = new SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'));
        def usageDate = sdf.parse(params.usageDate);
        def cal = new GregorianCalendar()
        cal.setTime(usageDate)
        def fact = new Fact(
          relatedTitle:result.tip.title,
          supplier:result.tip.provider,
          inst:result.tip.institution,
          juspio:result.tip.title.getIdentifierValue('jusp'),
          factFrom:usageDate,
          factTo:usageDate,
          factValue:params.usageValue,
          factUid:java.util.UUID.randomUUID().toString(),
          reportingYear:cal.get(Calendar.YEAR),
          reportingMonth:cal.get(Calendar.MONTH),
          factType:RefdataValue.get(params.factType)
        ).save(flush:true, failOnError:true);

      }

      if ( result.tip ) {
        result.usage = Fact.findAllByRelatedTitleAndSupplierAndInst(result.tip.title,result.tip.provider,result.tip.institution)
      }
      result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def addressbook() {
        def result = setResultGenerics()

        def qParts = [
                'p.tenant = :tenant',
                'p.isPublic = :public'
        ]
        def qParams = [
                tenant: result.institution,
                public: RefdataValue.getByValueAndCategory('No', 'YN')
        ]

        if (params.prs) {
            qParts << "(LOWER(p.last_name) LIKE :prsName OR LOWER(p.middle_name) LIKE :prsName OR LOWER(p.first_name) LIKE :prsName)"
            qParams << [prsName: "%${params.prs.toLowerCase()}%"]
        }
        if (params.org) {
            qParts << """(EXISTS (
SELECT pr FROM p.roleLinks AS pr WHERE (LOWER(pr.org.name) LIKE :orgName OR LOWER(pr.org.shortname) LIKE :orgName OR LOWER(pr.org.sortname) LIKE :orgName)
))
"""
            qParams << [orgName: "%${params.org.toLowerCase()}%"]
        }

        def query = "SELECT p FROM Person AS p WHERE " + qParts.join(" AND ")

        if (params.filterPropDef) {
            (query, qParams) = propertyService.evalFilterQuery(params, query, 'p', qParams)
        }

        result.visiblePersons = Person.executeQuery(query + " ORDER BY p.last_name, p.first_name ASC", qParams)

        result.editable = accessService.checkMinUserOrgRole(result.user, contextService.getOrg(), 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        result.propList =
                PropertyDefinition.findAllWhere(
                        descr: PropertyDefinition.PRS_PROP,
                        tenant: null // public properties
                ) +
                        PropertyDefinition.findAllWhere(
                                descr: PropertyDefinition.PRS_PROP,
                                tenant: contextService.getOrg() // private properties
                        )

        result
      }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def budgetCodes() {
        def result = setResultGenerics()

        result.editable = accessService.checkMinUserOrgRole(result.user, contextService.getOrg(), 'INST_USER') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        if (result.editable) {

            flash.message = null
            flash.error = null

            if (params.cmd == "newBudgetCode") {
                if (params.bc) {
                    def bc = new BudgetCode(
                            owner: result.institution,
                            value: params.bc,
                            descr: params.descr
                    )
                    if (bc.save()) {
                        flash.message = "Neuer Budgetcode wurde angelegt."
                    }
                    else {
                        flash.error = "Der neue Budgetcode konnte nicht angelegt werden."
                    }

                }
            } else if (params.cmd == "deleteBudgetCode") {
                def bc = genericOIDService.resolveOID(params.bc)

                if (bc && bc.owner == result.institution) {
                    bc.delete()
                }
            }

        }
        result.budgetCodes = BudgetCode.findAllByOwner(result.institution, [sort: 'value'])

        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def tasks() {
        def result = setResultGenerics()

        if (params.deleteId) {
            def dTask = Task.get(params.deleteId)
            if (dTask && dTask.creator.id == result.user.id) {
                try {
                    dTask.delete(flush: true)
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label', default: 'Task'), params.deleteId])
                }
                catch (Exception e) {
                    flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'task.label', default: 'Task'), params.deleteId])
                }
            }
        }

        def sdFormat = new java.text.SimpleDateFormat(message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
        def query = filterService.getTaskQuery(params, sdFormat)
        result.taskInstanceList   = taskService.getTasksByResponsibles(result.user, result.institution, query)
        result.myTaskInstanceList = taskService.getTasksByCreator(result.user, null)

        result.editable = accessService.checkMinUserOrgRole(result.user, contextService.getOrg(), 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')

        log.debug(result.taskInstanceList)
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def addConsortiaMembers() {
        def result = setResultGenerics()

        // new: filter preset
        params.orgType   = RefdataValue.getByValueAndCategory('Institution', 'OrgType')?.id.toString()
        params.orgSector = RefdataValue.getByValueAndCategory('Higher Education', 'OrgSector')?.id.toString()

        if (params.selectedOrgs) {
            log.debug('adding orgs to consortia')

            params.list('selectedOrgs').each { soId ->
                Map map = [
                        toOrg: result.institution,
                        fromOrg: Org.findById( Long.parseLong(soId)),
                        type: RefdataValue.findByValue('Consortium')
                ]
                if (! Combo.findWhere(map)) {
                    def cmb = new Combo(map)
                    cmb.save()
                }
            }
        }

        def fsq = filterService.getOrgQuery(params)
        result.availableOrgs = Org.executeQuery(fsq.query, fsq.queryParams, params)

        result.consortiaMemberIds = []
        Combo.findAllWhere(
                toOrg: result.institution,
                type:    RefdataValue.findByValue('Consortium')
        ).each { cmb ->
            result.consortiaMemberIds << cmb.fromOrg.id
        }
        result
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def manageConsortia() {
        def result = setResultGenerics()

        // new: filter preset
        params.orgType   = RefdataValue.getByValueAndCategory('Institution', 'OrgType')?.id.toString()
        params.orgSector = RefdataValue.getByValueAndCategory('Higher Education', 'OrgSector')?.id.toString()

        if (params.selectedOrgs) {
            log.debug('remove orgs from consortia')

            params.list('selectedOrgs').each { soId ->
                def cmb = Combo.findWhere(
                        toOrg: result.institution,
                        fromOrg: Org.findById( Long.parseLong(soId)),
                        type: RefdataValue.findByValue('Consortium')
                )
                cmb.delete()
            }
        }

        def fsq = filterService.getOrgComboQuery(params, result.institution)
        result.consortiaMembers = Org.executeQuery(fsq.query, fsq.queryParams, params)

        result
    }

    /**
     * Display and manage PrivateProperties for this institution
     */
    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def managePrivateProperties() {
        def result = setResultGenerics()

        result.editable = true // true, because action is protected
        result.privatePropertyDefinitions = PropertyDefinition.findAllWhere([tenant: result.institution])

        if('add' == params.cmd) {
            flash.message = addPrivatePropertyDefinition(params)
            result.privatePropertyDefinitions = PropertyDefinition.findAllWhere([tenant: result.institution])
        }
        else if('delete' == params.cmd) {
            flash.message = deletePrivatePropertyDefinition(params)
            result.privatePropertyDefinitions = PropertyDefinition.findAllWhere([tenant: result.institution])
        }
        result
    }

    @Secured(['ROLE_USER'])
    def switchContext() {
        def user = User.get(springSecurityService.principal.id)
        def org  = genericOIDService.resolveOID(params.oid)

        if (user && org && org.id in user.getAuthorizedOrgsIds()) {
            log.debug('switched context to: ' + org)
            contextService.setOrg(org)
        }
        redirect action:'dashboard', params:params.remove('oid')
    }

    /**
     * Adding new PrivateProperty for this institution if not existing
     *
     * @param params
     * @return
     */

    private addPrivatePropertyDefinition(params) {
        log.debug("adding private property definition for institution: " + params)

        def tenant = contextService.getOrg()

        def privatePropDef = PropertyDefinition.findWhere(
                name:   params.pd_name,
                descr:  params.pd_descr,
               // type:   params.pd_type,
                tenant: tenant,
        )

        if(!privatePropDef){
            def rdc

            if (params.refdatacategory) {
                rdc = RefdataCategory.findById( Long.parseLong(params.refdatacategory) )
                rdc = rdc?.desc
            }
            privatePropDef = PropertyDefinition.lookupOrCreate(
                    params.pd_name,
                    params.pd_type,
                    params.pd_descr,
                    (params.pd_multiple_occurrence ? true : false),
                    (params.pd_mandatory ? true : false),
                    tenant
            )
            privatePropDef.softData = PropertyDefinition.TRUE
            privatePropDef.refdataCategory = rdc

            if (privatePropDef.save(flush: true)) {
                return message(code: 'default.created.message', args:[privatePropDef.descr, privatePropDef.name])
            }
            else {
                return message(code: 'default.not.created.message', args:[privatePropDef.descr, privatePropDef.name])
            }
        }
    }

    /**
     * Delete existing PrivateProperty for this institution
     *
     * @param params
     * @return
     */

    private deletePrivatePropertyDefinition(params) {
        log.debug("delete private property definition for institution: " + params)

        def messages  = []
        def tenant    = contextService.getOrg()
        def deleteIds = params.list('deleteIds')

        deleteIds.each { did ->
            def id = Long.parseLong(did)
            def privatePropDef = PropertyDefinition.findWhere(id: id, tenant: tenant)
            if (privatePropDef) {
                privatePropDef.delete()
                messages << message(code: 'default.deleted.message', args:[privatePropDef.descr, privatePropDef.name])
            }
        }
        messages
    }

    private setResultGenerics() {

        def result          = [:]
        result.user         = User.get(springSecurityService.principal.id)
        //result.institution  = Org.findByShortcode(params.shortcode)
        result.institution  = contextService.getOrg()
        result
    }

    @DebugAnnotation(test='hasAffiliation("INST_USER")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_USER") })
    def ajaxEmptySubscription() {

        def result = setResultGenerics()
        result.orgType = result.institution.orgType

        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')
        if (result.editable) {

            if(result.orgType?.value == 'Consortium') {
                def fsq = filterService.getOrgComboQuery(params, result.institution)
                result.cons_members = Org.executeQuery(fsq.query, fsq.queryParams, params)
            }

            result
        }
        render (template: "../templates/filter/orgFilterTable", model: [orgList: result.cons_members, tmplShowCheckbox: true])
    }

    @DebugAnnotation(test='hasAffiliation("INST_ADM")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_ADM") })
    def actionAffiliationRequestOrg() {
        log.debug("actionMembershipRequestOrg");
        def req = UserOrg.get(params.req);
        def user = User.get(springSecurityService.principal.id)
        def currentOrg = contextService.getOrg()
        if ( req != null && req.org == currentOrg) {
            switch(params.act) {
                case 'approve':
                    req.status = UserOrg.STATUS_APPROVED
                    break;
                case 'deny':
                    req.status = UserOrg.STATUS_REJECTED
                    break;
                default:
                    log.error("FLASH UNKNOWN CODE");
                    break;
            }
            req.dateActioned = System.currentTimeMillis();
            req.save(flush:true);
        }
        else {
            log.error("FLASH");
        }
        redirect(action: "manageAffiliationRequests")
    }
}
