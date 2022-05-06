package de.laser.ajax

import de.laser.annotations.DebugAnnotation
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserRole
import de.laser.*
import de.laser.base.AbstractI10n
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.helper.*
import de.laser.interfaces.ShareSupport
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import de.laser.traits.I10nTrait
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import grails.core.GrailsClass
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils
import de.laser.exceptions.ChangeAcceptException
import com.k_int.kbplus.PendingChangeService

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * This controller manages AJAX calls which result in object manipulation and / or do not deliver clearly either HTML or JSON.
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxController {

    def genericOIDService
    def contextService
    def accessService
    def escapeService
    def formService
    def dashboardDueDatesService
    IdentifierService identifierService
    FilterService filterService
    PendingChangeService pendingChangeService
    PropertyService propertyService

    def refdata_config = [
    "ContentProvider" : [
      domain:'Org',
      countQry:"select count(o) from Org as o where exists (select roletype from o.orgType as roletype where roletype.value = 'Provider' ) and lower(o.name) like ? and (o.status is null or o.status != ?)",
      rowQry:"select o from Org as o where exists (select roletype from o.orgType as roletype where roletype.value = 'Provider' ) and lower(o.name) like ? and (o.status is null or o.status != ?) order by o.name asc",
      qryParams:[
              [
                param:'sSearch',
                clos:{ value ->
                    String result = '%'
                    if ( value && ( value.length() > 0 ) )
                        result = "%${value.trim().toLowerCase()}%"
                    result
                }
              ]
      ],
      cols:['name'],
      format:'map'
    ],
    "Licenses" : [
      domain:'License',
      countQry:"select count(l) from License as l",
      rowQry:"select l from License as l",
      qryParams:[],
      cols:['reference'],
      format:'simple'
    ],
    'Currency' : [
      domain:'RefdataValue',
      countQry:"select count(rdv) from RefdataValue as rdv where rdv.owner.desc='" + RDConstants.CURRENCY + "'",
      rowQry:"select rdv from RefdataValue as rdv where rdv.owner.desc='" + RDConstants.CURRENCY + "'",
      qryParams:[
                   [
                      param:'iDisplayLength',
                      value: 200
                   ]
      ],
      cols:['value'],
      format:'simple'
    ],
    "allOrgs" : [
            domain:'Org',
            countQry:"select count(o) from Org as o where lower(o.name) like ? and (o.status is null or o.status != ?)",
            rowQry:"select o from Org as o where lower(o.name) like ? and (o.status is null or o.status != ?) order by o.name asc",
            qryParams:[
                    [
                            param:'sSearch',
                            clos:{ value ->
                                String result = '%'
                                if ( value && ( value.length() > 0 ) )
                                    result = "%${value.trim().toLowerCase()}%"
                                result
                            }
                    ]
            ],
            cols:['name'],
            format:'map'
    ],
    "CommercialOrgs" : [
            domain:'Org',
            countQry:"select count(o) from Org as o where (o.sector.value = 'Publisher') and lower(o.name) like ? and (o.status is null or o.status != ?)",
            rowQry:"select o from Org as o where (o.sector.value = 'Publisher') and lower(o.name) like ? and (o.status is null or o.status != ?) order by o.name asc",
            qryParams:[
                    [
                            param:'sSearch',
                            clos:{ value ->
                                String result = '%'
                                if ( value && ( value.length() > 0 ) )
                                    result = "%${value.trim().toLowerCase()}%"
                                result
                            }
                    ]
            ],
            cols:['name'],
            format:'map'
    ]
  ]

    /**
     * Test call
     * @return the string 'test()'
     */
    def test() {
        render 'test()'
    }

    /**
     * Renders the given dialog message template
     * @return the template responding the given parameter
     */
    def genericDialogMessage() {

        if (params.template) {
            render template: "/templates/ajax/${params.template}", model: [a: 1, b: 2, c: 3]
        }
        else {
            render '<p>invalid call</p>'
        }
    }

    /**
     * Updates the user session cache for the given cache map key (formed by a key prefix and an uri) with the given value
     * @return void (an empty map)
     */
    def updateSessionCache() {
        if (contextService.getUser()) {
            SessionCacheWrapper cache = contextService.getSessionCache()

            if (params.key == UserSetting.KEYS.SHOW_EXTENDED_FILTER.toString()) {

                if (params.uri) {
                    cache.put("${params.key}/${params.uri}", params.value)
                    log.debug("update session based user setting: [${params.key}/${params.uri} -> ${params.value}]")
                }
            }
        }

        if (params.redirect) {
            redirect(url: request.getHeader('referer'))
        }
        Map<String, Object> result = [:]
        render result as JSON
    }

    /**
     * This is the call route for processing an xEditable reference data or role change
     * @return the new value for display update in the xEditable field
     * @see SemanticUiInplaceTagLib#xEditableRole
     * @see SemanticUiInplaceTagLib#xEditableRefData
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def genericSetRel() {
        String result = ''

        try {
            String[] target_components = params.pk.split(":")

            def target = genericOIDService.resolveOID(params.pk)
            if (target) {
                if (params.value == '') {
                    // Allow user to set a rel to null be calling set rel ''
                    target[params.name] = null
                    if (!target.save()) {
                        Map r = [status: "error", msg: message(code: 'default.save.error.general.message')]
                        render r as JSON
                        return
                    }
                } else {
                    String[] value_components = params.value.split(":")
                    def value = genericOIDService.resolveOID(params.value)

                    if (target && value) {
                        if (target instanceof UserSetting) {
                            target.setValue(value)
                        } else {
                            def binding_properties = ["${params.name}": value]
                            bindData(target, binding_properties)
                        }
                        if (target instanceof Org) {
                            if(params.name == "status" && value == RDStore.ORG_STATUS_RETIRED) {
                                target.retirementDate = new Date()
                            }
                        }

                        if (!target.save()) {
                            Map r = [status: "error", msg: message(code: 'default.save.error.general.message')]
                            render r as JSON
                            return
                        }

                        if (target instanceof SurveyResult) {
                            Org org = contextService.getOrg()
                            SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(target.surveyConfig, target.participant)

                            //If Survey Owner set Value then set FinishDate
                            if (org?.id == target.owner.id && (target.type == RDStore.SURVEY_PROPERTY_PARTICIPATION) && surveyOrg.finishDate == null) {
                                String property = target.type.getImplClassValueProperty()

                                if (target[property] != null) {
                                    log.debug("Set/Save FinishDate of SurveyOrg (${surveyOrg.id})")
                                    surveyOrg.finishDate = new Date()
                                    surveyOrg.save()
                                }
                            }
                        }

                        // We should clear the session values for a user if this is a user to force reload of the parameters.
                        if (target instanceof User) {
                            session.userPereferences = null
                        }

                        if (target instanceof UserSetting) {
                            if (target.key.toString() == 'LANGUAGE') {
                                Locale newLocale = new Locale(value.value, value.value.toUpperCase())
                                log.debug("UserSetting: LANGUAGE changed to: " + newLocale)

                                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request)
                                localeResolver.setLocale(request, response, newLocale)
                            }
                        }

                        if (params.resultProp) {
                            result = value[params.resultProp]
                        } else {
                            if (value) {
                                result = renderObjectValue(value)
                            }
                        }
                    } else {
                        log.debug("no value (target=${target_components}, value=${value_components}");
                    }
                }
            } else {
                log.error("no target (target=${target_components}");
            }

        } catch (Exception e) {
            log.error("@ genericSetRel()")
            log.error(e.toString())
        }

        def resp = [newValue: result]
        log.debug("genericSetRel() returns ${resp as JSON}")
        render resp as JSON
    }

  @Deprecated
  def refdataSearch() {
      // TODO: refactoring - only used by /templates/_orgLinksModal.gsp

    //log.debug("refdataSearch params: ${params}");
    
    Map<String, Object> result = [:]
    //we call toString in case we got a GString
    def config = refdata_config.get(params.id?.toString())

    if ( config == null ) {
        String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())
        // If we werent able to locate a specific config override, assume the ID is just a refdata key
      config = [
        domain:'RefdataValue',
        countQry:"select count(rdv) from RefdataValue as rdv where rdv.owner.desc='${params.id}'",
        rowQry:"select rdv from RefdataValue as rdv where rdv.owner.desc='${params.id}' order by rdv.order, rdv.value_" + locale,
        qryParams:[],
        cols:['value'],
        format:'simple'
      ]
    }

    if ( config ) {

      // result.config = config

      def query_params = []
      config.qryParams.each { qp ->
        log.debug("Processing query param ${qp} value will be ${params[qp.param]}");
        if ( qp.clos ) {
          query_params.add(qp.clos(params[qp.param]?:''));
        }
        else {
          query_params.add(params[qp.param]);
        }
      }

        if (config.domain == 'Org') {
            // new added param for org queries in this->refdata_config
            query_params.add(RefdataValue.getByValueAndCategory('Deleted', RDConstants.ORG_STATUS))
        }

        //log.debug("Row qry: ${config.rowQry}");
        //log.debug("Params: ${query_params}");
        //log.debug("Count qry: ${config.countQry}");

      def cq = Org.executeQuery(config.countQry,query_params);    

      def rq = Org.executeQuery(config.rowQry,
                                query_params,
                                [max:params.iDisplayLength?:1000,offset:params.iDisplayStart?:0]);

      if ( config.format=='map' ) {
        result.aaData = []
        result.sEcho = params.sEcho
        result.iTotalRecords = cq[0]
        result.iTotalDisplayRecords = cq[0]
    
        rq.each { it ->
          def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
          int ctr = 0;
          def row = [:]
          config.cols.each { cd ->
            // log.debug("Processing result col ${cd} pos ${ctr}");
            row["${ctr++}"] = rowobj[cd]
          }
          row["DT_RowId"] = "${rowobj.class.name}:${rowobj.id}"
          result.aaData.add(row)
        }
      }
      else {
        rq.each { it ->
          def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)
          result["${rowobj.class.name}:${rowobj.id}"] = rowobj[config.cols[0]];
        }
      }
    }

    // log.debug("refdataSearch returning ${result as JSON}");
    withFormat {
      html {
        result
      }
      json {
        render result as JSON
      }
    }
  }

    /**
     * Retrieves lists for a reference data value dropdown. The config parameter is used to build the underlying query
     * @return a {@link List} of {@link Map}s of structure [value: oid, text: text] to be used in dropdowns; the list may be returned purely or as JSON
     */
    @Secured(['ROLE_USER'])
    def sel2RefdataSearch() {

        log.debug("sel2RefdataSearch params: ${params}");
    
        List result = []
        Map<String, Object> config = refdata_config.get(params.id?.toString()) //we call toString in case we got a GString
        boolean defaultOrder = true

        if (config == null) {
            String locale = I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())
            defaultOrder = false
            // If we werent able to locate a specific config override, assume the ID is just a refdata key
            config = [
                domain      :'RefdataValue',
                countQry    :"select count(rdv) from RefdataValue as rdv where rdv.owner.desc='" + params.id + "'",
                rowQry      :"select rdv from RefdataValue as rdv where rdv.owner.desc='" + params.id + "' order by rdv.order asc, rdv.value_" + locale,
                qryParams   :[],
                cols        :['value'],
                format      :'simple'
            ]
        }

    if ( config ) {

      List query_params = []
      config.qryParams.each { qp ->
        if ( qp?.clos) {
          query_params.add(qp.clos(params[qp.param]?:''));
        }
        else if(qp?.value) {
            params."${qp.param}" = qp?.value
        }
        else {
          query_params.add(params[qp.param]);
        }
      }

      def cq = RefdataValue.executeQuery(config.countQry,query_params);
      def rq = RefdataValue.executeQuery(config.rowQry,
                                query_params,
                                [max:params.iDisplayLength?:1000,offset:params.iDisplayStart?:0]);

      rq.each { it ->
        def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)

          // handle custom constraint(s) ..
          if (it.value.equalsIgnoreCase('deleted') && params.constraint?.contains('removeValue_deleted')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          if (it.value.equalsIgnoreCase('administrative subscription') && params.constraint?.contains('removeValue_administrativeSubscription')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          //value is correct incorrectly translated!
          if (it.value.equalsIgnoreCase('local subscription') && accessService.checkPerm("ORG_CONSORTIUM") && params.constraint?.contains('removeValue_localSubscription')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          // default ..
          else {
              if (it instanceof I10nTrait) {
                  result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n(config.cols[0])}"])
              }
              else if (it instanceof AbstractI10n) {
                  result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n(config.cols[0])}"])
              }
              else {
                  def objTest = rowobj[config.cols[0]]
                  if (objTest) {
                      def no_ws = objTest.replaceAll(' ', '');
                      def local_text = message(code: "refdata.${no_ws}", default: "${objTest}");
                      result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${local_text}"])
                  }
              }
          }
      }
    }
    else {
      log.error("No config for refdata search ${params.id}");
    }

      if (result && defaultOrder) {
          result.sort{ x,y -> x.text.compareToIgnoreCase y.text  }
      }

        withFormat {
            html {
                result
            }
            json {
                render result as JSON
            }
        }
    }

    /**
     * This method is used for the property distribution view at manageProperties and controls which objects have been selected for later processing.
     * The caching ensures that the selections remain also when the page of results is being changed; this method updates a respective cache entry or (de-)selects the whole selection
     * @return a {@link Map} reflecting the success status
     */
    @Secured(['ROLE_USER'])
    def updatePropertiesSelection() {
        Map success = [success: false]
        EhcacheWrapper cache = contextService.getCache("/manageProperties", contextService.USER_SCOPE)
        List<String> checkedProperties = cache.get(params.table) ?: []
        boolean check = Boolean.valueOf(params.checked)
        if(params.key == "all") {
            if(check) {
                PropertyDefinition propDef = genericOIDService.resolveOID(params.propDef)
                Map<String, Object> propertyData = propertyService.getAvailableProperties(propDef, contextService.getOrg(), params)
                switch (params.table) {
                    case "with": checkedProperties.addAll(propertyData.withProp.collect { o -> o.id })
                        break
                    case "without":
                    case "audit": checkedProperties.addAll(propertyData.withoutProp.collect { o -> o.id })
                        break
                }
            }
            else {
                checkedProperties.clear()
            }
        }
        else {
            if(check)
                checkedProperties << params.key
            else checkedProperties.remove(params.key)
        }
        if(cache.put(params.table,checkedProperties))
            success.success = true
        render success as JSON
    }

    /**
     * This method is used for the title selection views at addEntitlement and controls which entitlements have been selected for later processing.
     * The caching ensures that the selections remain also when the page of results is being changed; this method updates a respective cache entry or (de-)selects the whole selection
     * @return a {@link Map} reflecting the success status and the number of changes performed
     */
  @Secured(['ROLE_USER'])
  def updateChecked() {
      Map success = [success:false]
      SessionCacheWrapper sessionCache = contextService.getSessionCache()
      String sub = params.sub ?: params.id
      Map<String,Object> cache = sessionCache.get("/subscription/${params.referer}/${sub}")

      if(!cache) {
          sessionCache.put("/subscription/${params.referer}/${sub}",["checked":[:]])
          cache = sessionCache.get("/subscription/${params.referer}/${sub}")
      }

      Map checked = cache.get('checked')

      if(params.index == 'all') {
          Map<String, Object> filterParams = JSON.parse(params.filterParams) as Map<String, Object>
		  Map<String, String> newChecked = checked ?: [:]
          if(params.referer == 'renewEntitlementsWithSurvey'){

              Subscription baseSub = Subscription.get(params.baseSubID)
              Subscription newSub = Subscription.get(params.newSubID)
              Set<Subscription> previousSubscription = newSub._getCalculatedPrevious()

              List<Long> sourceTipps

              Map query2 = filterService.getIssueEntitlementQuery(params+[ieAcceptStatusNotFixed: true], newSub)
              List<Long> selectedIETipps = IssueEntitlement.executeQuery("select ie.tipp.id " + query2.query, query2.queryParams)

              Map query3 = filterService.getIssueEntitlementQuery(params+[ieAcceptStatusFixed: true], newSub)
              List<Long> targetIETipps = IssueEntitlement.executeQuery("select ie.tipp.id " + query3.query, query3.queryParams)

              List<IssueEntitlement> sourceIEs

              if(params.tab == 'currentIEs') {
                  Map query = filterService.getIssueEntitlementQuery(params+[ieAcceptStatusFixed: true], previousSubscription)
                  List<IssueEntitlement> previousTipps = previousSubscription ? IssueEntitlement.executeQuery("select ie.tipp.id " + query.query, query.queryParams) : []
                  sourceIEs = previousTipps ? IssueEntitlement.findAllByTippInListAndSubscriptionAndStatusNotEqual(TitleInstancePackagePlatform.findAllByIdInList(previousTipps), previousSubscription, RDStore.TIPP_STATUS_DELETED) : []
                  sourceIEs = sourceIEs + (sourceTipps ? IssueEntitlement.findAllByTippInListAndSubscriptionAndStatusNotEqual(TitleInstancePackagePlatform.findAllByIdInList(targetIETipps), newSub, RDStore.TIPP_STATUS_DELETED) : [])

              }

              if(params.tab == 'allIEs') {
                  Map query = filterService.getIssueEntitlementQuery(params, baseSub)
                  List<Long> allIETipps = IssueEntitlement.executeQuery("select ie.tipp.id " + query.query, query.queryParams)
                  sourceTipps = allIETipps
                  sourceTipps = sourceTipps.minus(selectedIETipps)
                  sourceTipps = sourceTipps.minus(targetIETipps)
                  sourceIEs = sourceTipps ? IssueEntitlement.findAllByTippInListAndSubscriptionAndStatus(TitleInstancePackagePlatform.findAllByIdInList(sourceTipps), baseSub, RDStore.TIPP_STATUS_CURRENT) : []
              }
              if(params.tab == 'selectedIEs') {
                  sourceTipps = selectedIETipps
                  sourceTipps = sourceTipps.minus(targetIETipps)
                  sourceIEs = sourceTipps ? IssueEntitlement.findAllByTippInListAndSubscriptionAndStatusNotEqual(TitleInstancePackagePlatform.findAllByIdInList(sourceTipps), newSub, RDStore.TIPP_STATUS_DELETED) : []
              }

              sourceIEs.each { IssueEntitlement ie ->
                  newChecked[ie.id.toString()] = params.checked == 'true' ? 'checked' : null
              }

          }
          else {
              Set<Package> pkgFilter = []
              if (filterParams.pkgFilter)
                  pkgFilter << Package.get(filterParams.pkgFilter)
              else pkgFilter.addAll(SubscriptionPackage.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription.id = :sub', [sub: params.long("sub")]))
              Map<String, Object> tippFilter = filterService.getTippQuery(filterParams, pkgFilter.toList())
              String query = tippFilter.query.replace("select tipp.id", "select tipp.gokbId").replace("order by lower(tipp.sortname) asc", "")
              Set<String> tippUUIDs = TitleInstancePackagePlatform.executeQuery(query+" and not exists (select ie.id from IssueEntitlement ie join ie.tipp tipp2 where ie.subscription.id = :sub and tipp.id = tipp2.id and ie.status = tipp2.status)", tippFilter.queryParams+[sub: params.long("sub")])
              tippUUIDs.each { String e ->
                  newChecked[e] = params.checked == 'true' ? 'checked' : null
              }
          }
          cache.put('checked',newChecked)
          success.checkedCount = params.checked == 'true' ? newChecked.size() : 0
	  }
	  else {
          Map<String, String> newChecked = checked ?: [:]
		  newChecked[params.index] = params.checked == 'true' ? 'checked' : null
		  if(cache.put('checked',newChecked)){
              success.success = true
          }
          success.checkedCount = newChecked.findAll {it.value == 'checked'}.size()

	  }
      render success as JSON
  }

    /**
     * This method is used by the addEntitlements view and updates the cache for the entitlement candidates which should be added to the local subscription holding;
     * when the entitlements are being processed, the data from the cache is being applied to the entitlements
     * @return a {@link Map} reflecting the success status
     */
  @Secured(['ROLE_USER'])
  def updateIssueEntitlementOverwrite() {
      Map success = [success:false]
      EhcacheWrapper cache = contextService.getCache("/subscription/${params.referer}/${params.sub}", contextService.USER_SCOPE)
      Map issueEntitlementCandidates = cache.get('issueEntitlementCandidates')
      if(!issueEntitlementCandidates)
          issueEntitlementCandidates = [:]
      def ieCandidate = issueEntitlementCandidates.get(params.key)
      if(!ieCandidate)
          ieCandidate = [:]
      if(params.coverage != 'false') {
          def ieCoverage
          Pattern pattern = Pattern.compile("(\\w+)(\\d+)")
          Matcher matcher = pattern.matcher(params.prop)
          if(matcher.find()) {
              String prop = matcher.group(1)
              int covStmtKey = Integer.parseInt(matcher.group(2))
              if(!ieCandidate.coverages){
                  ieCandidate.coverages = []
                  ieCoverage = [:]
              }
              else
                  ieCoverage = ieCandidate.coverages[covStmtKey]
              if(prop in ['startDate','endDate']) {
                  SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                  ieCoverage[prop] = sdf.parse(params.propValue)
              }
              else {
                  ieCoverage[prop] = params.propValue
              }
              ieCandidate.coverages[covStmtKey] = ieCoverage
          }
          else {
              log.error("something wrong with the regex matching ...")
          }
      }
      else {
          ieCandidate[params.prop] = params.propValue
      }
      issueEntitlementCandidates.put(params.key,ieCandidate)
      if(cache.put('issueEntitlementCandidates',issueEntitlementCandidates))
          success.success = true
      render success as JSON
  }

    /**
     * Adds a relation link from a given object to an {@link Org}. The org may be an institution or an other organisation like a provider
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addOrgRole() {
        def owner  = genericOIDService.resolveOID(params.parent)
        RefdataValue rel = RefdataValue.get(params.orm_orgRole)

        def orgIds = params.list('orm_orgOid')
        orgIds.each{ oid ->
            Org org_to_link = (Org) genericOIDService.resolveOID(oid)
            boolean duplicateOrgRole = false

            if(params.recip_prop == 'sub') {
                duplicateOrgRole = OrgRole.findAllBySubAndRoleTypeAndOrg(owner, rel, org_to_link) ? true : false
            }
            else if(params.recip_prop == 'pkg') {
                duplicateOrgRole = OrgRole.findAllByPkgAndRoleTypeAndOrg(owner, rel, org_to_link) ? true : false
            }
            else if(params.recip_prop == 'lic') {
                duplicateOrgRole = OrgRole.findAllByLicAndRoleTypeAndOrg(owner, rel, org_to_link) ? true : false
            }
            else if(params.recip_prop == 'title') {
                duplicateOrgRole = OrgRole.findAllByTitleAndRoleTypeAndOrg(owner, rel, org_to_link) ? true : false
            }

            if(! duplicateOrgRole) {
                OrgRole new_link = new OrgRole(org: org_to_link, roleType: rel)
                new_link[params.recip_prop] = owner

                if (new_link.save()) {
                    // log.debug("Org link added")
                    if (owner instanceof ShareSupport && owner.checkSharePreconditions(new_link)) {
                        new_link.isShared = true
                        new_link.save()

                        owner.updateShare(new_link)
                    }
                } else {
                    log.error("Problem saving new org link ..")
                    new_link.errors.each { e ->
                        log.error( e.toString() )
                    }
                }
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Deletes the given relation link between an {@link Org} its target
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def delOrgRole() {
        OrgRole or = OrgRole.get(params.id)

        def owner = or.getOwner()
        if (owner instanceof ShareSupport && or.isShared) {
            or.isShared = false
            owner.updateShare(or)
        }
        or.delete()

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Adds a relation link from a given object to a {@link Person}
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addPrsRole() {
        Org org             = (Org) genericOIDService.resolveOID(params.org)
        def parent          = genericOIDService.resolveOID(params.parent)
        Person person       = (Person) genericOIDService.resolveOID(params.person)
        RefdataValue role   = (RefdataValue) genericOIDService.resolveOID(params.role)

        PersonRole newPrsRole
        PersonRole existingPrsRole

        if (org && person && role) {
            newPrsRole = new PersonRole(prs: person, org: org)
            if (parent) {
                newPrsRole.responsibilityType = role
                newPrsRole.setReference(parent)

                String[] ref = newPrsRole.getReference().split(":")
                existingPrsRole = PersonRole.findWhere(prs:person, org: org, responsibilityType: role, "${ref[0]}": parent)
            }
            else {
                newPrsRole.functionType = role
                existingPrsRole = PersonRole.findWhere(prs:person, org: org, functionType: role)
            }
        }

        if (! existingPrsRole && newPrsRole && newPrsRole.save()) {
            //flash.message = message(code: 'default.success')
        }
        else {
            log.error("Problem saving new person role ..")
            //flash.error = message(code: 'default.error')
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Deletes the given relation link between a {@link Person} its target
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def delPrsRole() {
        PersonRole prsRole = PersonRole.get(params.id)

        if (prsRole && prsRole.delete()) {
        }
        else {
            log.error("Problem deleting person role ..")
            //flash.error = message(code: 'default.error')
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Inserts a new reference data value. Beware: the inserted reference data value does not survive database resets nor is that available throughout the instances;
     * this has to be considered when running this webapp on multiple instances!
     * If you wish to insert a reference data value which persists and is available on different instances, enter the parameters in RefdataValue.csv. This resource file is
     * (currently, as of November 18th, '21) located at /src/main/webapp/setup
     */
    @Secured(['ROLE_USER'])
    def addRefdataValue() {

        RefdataValue newRefdataValue
        String error
        String msg

        RefdataCategory rdc = RefdataCategory.findById(params.refdata_category_id)

        if (RefdataValue.getByValueAndCategory(params.refdata_value, rdc.desc)) {
            error = message(code: "refdataValue.create_new.unique")
            log.debug(error)
        }
        else {
            Map<String, Object> map = [
                    token   : params.refdata_value,
                    rdc     : rdc.desc,
                    hardData: false,
                    i10n    : [value_de: params.refdata_value, value_en: params.refdata_value]
            ]

            newRefdataValue = RefdataValue.construct(map)

            if (newRefdataValue?.hasErrors()) {
                log.error(newRefdataValue.errors.toString())
                error = message(code: 'default.error')
            }
            else {
                msg = message(code: 'refdataValue.created', args: [newRefdataValue.value])
            }
        }

        if (params.reloadReferer) {
            flash.newRefdataValue = newRefdataValue
            flash.error   = error
            flash.message = msg
            redirect(url: params.reloadReferer)
        }
    }

    /**
     * Inserts a new reference data category. Beware: the inserted reference data category does not survive database resets nor is that available throughout the instances;
     * this has to be considered when running this webapp on multiple instances!
     * If you wish to insert a reference data category which persists and is available on different instances, enter the parameters in RefdataCategory.csv. This resource file is
     * (currently, as of November 18th, '21) located at /src/main/webapp/setup
     */
    @Secured(['ROLE_USER'])
    def addRefdataCategory() {

        RefdataCategory newRefdataCategory
        String error
        String msg

        RefdataCategory rdc = RefdataCategory.getByDesc(params.refdata_category)
        if (rdc) {
            error = message(code: 'refdataCategory.create_new.unique')
            log.debug(error)
        }
        else {
            Map<String, Object> map = [
                    token   : params.refdata_category,
                    hardData: false,
                    i10n    : [desc_de: params.refdata_category, desc_en: params.refdata_category]
            ]

            newRefdataCategory = RefdataCategory.construct(map)

            if (newRefdataCategory?.hasErrors()) {
                log.error(newRefdataCategory.errors.toString())
                error = message(code: 'default.error')
            }
            else {
                msg = message(code: 'refdataCategory.created', args: [newRefdataCategory.desc])
            }
        }

        if (params.reloadReferer) {
            flash.newRefdataCategory = newRefdataCategory
            flash.error   = error
            flash.message = msg
            redirect(url: params.reloadReferer)
        }
    }

    /**
     * Inserts a new custom property definition, i.e. a type of property which is usable by every institution.
     * Beware: the inserted reference data category does not survive database resets nor is that available throughout the instances;
     * this has to be considered when running this webapp on multiple instances!
     * If you wish to insert a reference data category which persists and is available on different instances, enter the parameters in PropertyDefinition.csv. This resource file is
     * (currently, as of November 18th, '21) located at /src/main/webapp/setup.
     * Note the global usability of this property definition; see {@link MyInstitutionController#managePrivatePropertyDefinitions()} with params.cmd == add for property types which
     * are for an institution's internal usage only
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addCustomPropertyType() {
        def newProp
        def error
        def msg
        def ownerClass = params.ownerClass // we might need this for addCustomPropertyValue
        def owner      = AppUtils.getDomainClass( ownerClass )?.getClazz()?.get(params.ownerId)

        // TODO ownerClass
        if (PropertyDefinition.findByNameAndDescrAndTenantIsNull(params.cust_prop_name, params.cust_prop_desc)) {
            error = message(code: 'propertyDefinition.name.unique')
        }
        else {
            if (params.cust_prop_type.equals(RefdataValue.class.name)) {
                if (params.refdatacategory) {

                    Map<String, Object> map = [
                            token       : params.cust_prop_name,
                            category    : params.cust_prop_desc,
                            type        : params.cust_prop_type,
                            rdc         : RefdataCategory.get(params.refdatacategory)?.getDesc(),
                            multiple    : (params.cust_prop_multiple_occurence == 'on'),
                            i10n        : [
                                    name_de: params.cust_prop_name?.trim(),
                                    name_en: params.cust_prop_name?.trim(),
                                    expl_de: params.cust_prop_expl?.trim(),
                                    expl_en: params.cust_prop_expl?.trim()
                            ]
                    ]

                    newProp = PropertyDefinition.construct(map)
                }
                else {
                    error = message(code: 'ajax.addCustPropertyType.error')
                }
            }
            else {
                    Map<String, Object> map = [
                            token       : params.cust_prop_name,
                            category    : params.cust_prop_desc,
                            type        : params.cust_prop_type,
                            multiple    : (params.cust_prop_multiple_occurence == 'on'),
                            i10n        : [
                                    name_de: params.cust_prop_name?.trim(),
                                    name_en: params.cust_prop_name?.trim(),
                                    expl_de: params.cust_prop_expl?.trim(),
                                    expl_en: params.cust_prop_expl?.trim()
                            ]
                    ]

                    newProp = PropertyDefinition.construct(map)
            }

            if (newProp?.hasErrors()) {
                log.error(newProp.errors.toString())
                error = message(code: 'default.error')
            }
            else {
                msg = message(code: 'ajax.addCustPropertyType.success')
                //newProp.softData = true
                newProp.save()

                if (params.autoAdd == "on" && newProp) {
                    params.propIdent = newProp.id.toString()
                    chain(action: "addCustomPropertyValue", params: params)
                }
            }
        }

        request.setAttribute("editable", params.editable == "true")

        if (params.reloadReferer) {
            flash.newProp = newProp
            flash.error = error
            flash.message = msg
            redirect(url: params.reloadReferer)
        }
        else if (params.redirect) {
            flash.newProp = newProp
            flash.error = error
            flash.message = msg
            redirect(controller:"propertyDefinition", action:"create")
        }
        else {
            Map<String, Object> allPropDefGroups = owner.getCalculatedPropDefGroups(contextService.getOrg())

            render(template: "/templates/properties/custom", model: [
                    ownobj: owner,
                    customProperties: owner.propertySet,
                    newProp: newProp,
                    error: error,
                    message: msg,
                    orphanedProperties: allPropDefGroups.orphanedProperties
            ])
        }
    }

    /**
     * Adds a value to a custom property and updates the property enumeration fragment
     */
  @Secured(['ROLE_USER'])
  def addCustomPropertyValue(){
    if(params.propIdent.length() > 0) {
      def error
      def newProp
      def owner = AppUtils.getDomainClass( params.ownerClass )?.getClazz()?.get(params.ownerId)
      def type = PropertyDefinition.get(params.propIdent.toLong())
      Org contextOrg = contextService.getOrg()
      def existingProp = owner.propertySet.find { it.type.name == type.name && it.tenant?.id == contextOrg.id }

      if (existingProp == null || type.multipleOccurrence) {
        String propDefConst = type.tenant ? PropertyDefinition.PRIVATE_PROPERTY : PropertyDefinition.CUSTOM_PROPERTY
        newProp = PropertyDefinition.createGenericProperty(propDefConst, owner, type, contextOrg )
        if (newProp.hasErrors()) {
          log.error(newProp.errors.toString())
        } else {
          log.debug("New custom property created: " + newProp.type.name)
        }
      } else {
        error = message(code: 'ajax.addCustomPropertyValue.error')
      }

      owner.refresh()

      request.setAttribute("editable", params.editable == "true")
      boolean showConsortiaFunctions = Boolean.parseBoolean(params.showConsortiaFunctions)
        if(Boolean.valueOf(params.withoutRender)){
            if(params.url){
                redirect(url: params.url)
            }else {
                redirect(url: request.getHeader('referer'))
            }
        }
        else
        {
              if (params.propDefGroup) {
                render(template: "/templates/properties/group", model: [
                        ownobj          : owner,
                        contextOrg      : contextOrg,
                        newProp         : newProp,
                        error           : error,
                        showConsortiaFunctions: showConsortiaFunctions,
                        propDefGroup    : genericOIDService.resolveOID(params.propDefGroup),
                        propDefGroupBinding : genericOIDService.resolveOID(params.propDefGroupBinding),
                        custom_props_div: "${params.custom_props_div}", // JS markup id
                        prop_desc       : type.descr // form data
                ])
              }
              else {
                  Map<String, Object> allPropDefGroups = owner.getCalculatedPropDefGroups(contextService.getOrg())

                  Map<String, Object> modelMap =  [
                          ownobj                : owner,
                          contextOrg            : contextOrg,
                          newProp               : newProp,
                          showConsortiaFunctions: showConsortiaFunctions,
                          error                 : error,
                          custom_props_div      : "${params.custom_props_div}", // JS markup id
                          prop_desc             : type.descr, // form data
                          orphanedProperties    : allPropDefGroups.orphanedProperties
                  ]


                      render(template: "/templates/properties/custom", model: modelMap)
                  }
        }
    }
    else {
      log.error("Form submitted with missing values")
    }
  }

    /**
     * Adds a custom property group binding, i.e. sets the configuration of a property type group for a given object and updates the group bindings view fragment
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addCustomPropertyGroupBinding() {

        def ownobj              = genericOIDService.resolveOID(params.ownobj)
        def propDefGroup        = genericOIDService.resolveOID(params.propDefGroup)
        List<PropertyDefinitionGroup> availPropDefGroups  = PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), ownobj.class.name)

        if (ownobj && propDefGroup) {
            if (params.isVisible in ['Yes', 'No']) {
                PropertyDefinitionGroupBinding gb = new PropertyDefinitionGroupBinding(
                        propDefGroup: propDefGroup,
                        isVisible: (params.isVisible == 'Yes')
                )
                if (ownobj.class.name == License.class.name) {
                    gb.lic = ownobj
                }
                else if (ownobj.class.name == Org.class.name) {
                    gb.org = ownobj
                }
                else if (ownobj.class.name == Subscription.class.name) {
                    gb.sub = ownobj
                }
                gb.save()
            }
        }

        render(template: "/templates/properties/groupBindings", model:[
                propDefGroup: propDefGroup,
                ownobj: ownobj,
                availPropDefGroups: availPropDefGroups,
                editable: params.editable,
                showConsortiaFunctions: params.showConsortiaFunctions
        ])
    }

    /**
     * Unsets a configuration for a property definition group for a given object and updates the property group view
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def deleteCustomPropertyGroupBinding() {
        def ownobj              = genericOIDService.resolveOID(params.ownobj)
        def propDefGroup        = genericOIDService.resolveOID(params.propDefGroup)
        def binding             = genericOIDService.resolveOID(params.propDefGroupBinding)
        List<PropertyDefinitionGroup> availPropDefGroups  = PropertyDefinitionGroup.getAvailableGroups(contextService.getOrg(), ownobj.class.name)

        if (ownobj && propDefGroup && binding) {
            binding.delete()
        }

        render(template: "/templates/properties/groupBindings", model:[
                propDefGroup: propDefGroup,
                ownobj: ownobj,
                availPropDefGroups: availPropDefGroups,
                editable: params.editable,
                showConsortiaFunctions: params.showConsortiaFunctions
        ])
    }

    /**
    * Adds a value to a domain specific private property and updates the property enumeration fragment
    */
    @Secured(['ROLE_USER'])
    def addPrivatePropertyValue(){
      if(params.propIdent.length() > 0) {
        def error
        def newProp
        Org tenant = Org.get(params.tenantId)
          def owner  = AppUtils.getDomainClass( params.ownerClass )?.getClazz()?.get(params.ownerId)
          PropertyDefinition type   = PropertyDefinition.get(params.propIdent.toLong())

        if (! type) { // new property via select2; tmp deactivated
          error = message(code:'propertyDefinition.private.deactivated')
        }
        else {
            Set<AbstractPropertyWithCalculatedLastUpdated> existingProps
            if(owner.hasProperty("privateProperties")) {
                existingProps = owner.propertySet.findAll {
                    it.owner.id == owner.id && it.type.id == type.id // this sucks due lazy proxy problem
                }
            }
            else {
                existingProps = owner.propertySet.findAll { AbstractPropertyWithCalculatedLastUpdated prop ->
                    prop.owner.id == owner.id && prop.type.id == type.id && prop.tenant.id == tenant.id && !prop.isPublic
                }
            }
          existingProps.removeAll { it.type.name != type.name } // dubious fix


          if (existingProps.size() == 0 || type.multipleOccurrence) {
            newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.PRIVATE_PROPERTY, owner, type, contextService.getOrg())
            if (newProp.hasErrors()) {
              log.error(newProp.errors.toString())
            } else {
              log.debug("New private property created: " + newProp.type.name)
            }
          } else {
            error = message(code: 'ajax.addCustomPropertyValue.error')
          }
        }

        owner.refresh()

        request.setAttribute("editable", params.editable == "true")
          if(Boolean.valueOf(params.withoutRender)){
              if(params.url){
                  redirect(url: params.url)
              }else {
                  redirect(url: request.getHeader('referer'))
              }
          }else {
              render(template: "/templates/properties/private", model: [
                      ownobj          : owner,
                      tenant          : tenant,
                      newProp         : newProp,
                      error           : error,
                      contextOrg      : contextService.getOrg(),
                      propertyWrapper: "private-property-wrapper-${tenant.id}", // JS markup id
                      prop_desc       : type.descr // form data
              ])
          }
      }
      else  {
        log.error("Form submitted with missing values")
      }
    }

    /**
     * Toggles the sharing of the given object, i.e. de-/activates its visiblity in member objects
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def toggleShare() {
        def owner = genericOIDService.resolveOID( params.owner )
        def sharedObject = genericOIDService.resolveOID( params.sharedObject )

        if (! sharedObject.isShared) {
            sharedObject.isShared = true
        } else {
            sharedObject.isShared = false
        }
        sharedObject.save()

        ((ShareSupport) owner).updateShare(sharedObject)

        if (params.tmpl) {
            if (params.tmpl == 'documents') {
                render(template: '/templates/documents/card', model: [ownobj: owner, editable: true, ajaxCallController: params.ajaxCallController, ajaxCallAction: params.ajaxCallAction]) // TODO editable from owner
            }
            else if (params.tmpl == 'notes') {
                render(template: '/templates/notes/card', model: [ownobj: owner, editable: true, ajaxCallController: params.ajaxCallController, ajaxCallAction: params.ajaxCallAction]) // TODO editable from owner
            }
        }
        else {
            redirect(url: request.getHeader('referer'))
        }
    }

    /**
     * Switches between the member subscription visiblity; is applicable for administrative subscriptions only. A SUBCRIBER_CONS_HIDDEN cannot see nor access the given subscription
     * @see de.laser.interfaces.CalculatedType#TYPE_ADMINISTRATIVE
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def toggleOrgRole() {
        OrgRole oo = OrgRole.executeQuery('select oo from OrgRole oo where oo.sub = :sub and oo.roleType in :roleTypes',[sub:Subscription.get(params.id),roleTypes:[RDStore.OR_SUBSCRIBER_CONS,RDStore.OR_SUBSCRIBER_CONS_HIDDEN]])[0]
        if (oo) {
            if(oo.roleType == RDStore.OR_SUBSCRIBER_CONS)
                oo.roleType = RDStore.OR_SUBSCRIBER_CONS_HIDDEN
            else if(oo.roleType == RDStore.OR_SUBSCRIBER_CONS_HIDDEN)
                oo.roleType = RDStore.OR_SUBSCRIBER_CONS
        }
        oo.save()
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Toggles the inheritance of the given object, i.e. passes or retires a consortial parent object to or from member objects.
     * The change is being reflected via a {@link PendingChange} because the inheritance means a change on the object itself and each consortium may decide
     * whether the changes should be automatically applied or only after confirmation
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def toggleAudit() {
        //String referer = request.getHeader('referer')
        if(formService.validateToken(params)) {
            def owner = genericOIDService.resolveOID(params.owner)
            if (owner) {
                def members = owner.getClass().findAllByInstanceOf(owner)
                def objProps = owner.getLogIncluded()
                def prop = params.property

                if (prop in objProps) {
                    if (! AuditConfig.getConfig(owner, prop)) {
                        AuditConfig.addConfig(owner, prop)

                        members.each { m ->
                            m.setProperty(prop, owner.getProperty(prop))
                            m.save()
                        }
                    }
                    else {
                        AuditConfig.removeConfig(owner, prop)

                        if (! params.keep) {
                            members.each { m ->
                                if(m[prop] instanceof Boolean)
                                    m.setProperty(prop, false)
                                else {
                                    if(m[prop] instanceof RefdataValue) {
                                        if(m[prop].owner.desc == RDConstants.Y_N_U)
                                            m.setProperty(prop, RDStore.YNU_UNKNOWN)
                                    }
                                    else
                                        m.setProperty(prop, null)
                                }
                                m.save()
                            }
                        }

                        // delete pending changes
                        // e.g. PendingChange.changeDoc = {changeTarget, changeType, changeDoc:{OID,  event}}
                        members.each { m ->
                            List<PendingChange> openPD = PendingChange.executeQuery("select pc from PendingChange as pc where pc.status is null and pc.costItem is null and pc.oid = :objectID",
                                    [objectID: "${m.class.name}:${m.id}"])

                            openPD?.each { pc ->
                                def payload = JSON.parse(pc?.payload)
                                if (payload && payload?.changeDoc) {
                                    def eventObj = genericOIDService.resolveOID(payload.changeDoc?.OID)
                                    def eventProp = payload.changeDoc?.prop
                                    if (eventObj?.id == owner?.id && eventProp.equalsIgnoreCase(prop)) {
                                        pc.delete()
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Toggles inheritance of the given identifier object, i.e. passes or retires an identifier to or from member objects
     * This change is being reflected via a {@link PendingChange} because the inheritance means a change on the object itself and each consortium may decide
     * whether the changes should be automatically applied or only after confirmation
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def toggleIdentifierAuditConfig() {
        def owner = AppUtils.getDomainClass( params.ownerClass )?.getClazz()?.get(params.ownerId)
        if(formService.validateToken(params)) {
            Identifier identifier  = Identifier.get(params.id)

            Org contextOrg = contextService.getOrg()
            if (AuditConfig.getConfig(identifier, AuditConfig.COMPLETE_OBJECT)) {
                AuditConfig.removeAllConfigs(identifier)

                Identifier.findAllByInstanceOf(identifier).each{ Identifier id ->
                    id.delete()
                }
            }
            else {
                String memberType
                    if(owner instanceof Subscription)
                        memberType = 'sub'
                    else if(owner instanceof License)
                        memberType = 'lic'
                if(memberType) {
                    owner.getClass().findAllByInstanceOf(owner).each { member ->
                        Identifier existingIdentifier = Identifier.executeQuery('select id from Identifier id where id.'+memberType+' = :member and id.instanceOf = :id', [member: member, id: identifier])[0]
                        if (! existingIdentifier) {
                            //List<Identifier> matchingProps = Identifier.findAllByOwnerAndTypeAndTenant(member, property.type, contextOrg)
                            List<Identifier> matchingIds = Identifier.executeQuery('select id from Identifier id where id.'+memberType+' = :member and id.value = :value and id.ns = :ns',[member: member, value: identifier.value, ns: identifier.ns])
                            // unbound prop found with matching type, set backref
                            if (matchingIds) {
                                matchingIds.each { Identifier memberId ->
                                    memberId.instanceOf = identifier
                                    memberId.save()
                                }
                            }
                            else {
                                // no match found, creating new prop with backref
                                Identifier.constructWithFactoryResult([value: identifier.value, note: identifier.note, parent: identifier, reference: member, namespace: identifier.ns])
                            }
                        }
                    }
                    AuditConfig.addConfig(identifier, AuditConfig.COMPLETE_OBJECT)
                }
            }
        }
        render template: "/templates/meta/identifierList", model: identifierService.prepareIDsForTable(owner)
    }

    /**
     * Enables or disables the visibility of a custom property
     * Visibility is important in a context where several institutions may see the property, those are
     * <ul>
     *     <li>member subscriptions</li>
     *     <li>organisations resp. institutions ({@link Org}s)</li>
     *     <li>platforms</li>
     * </ul>
     * If isPublic is set to false, only that institution may see the property who created it
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def togglePropertyIsPublic() {
        if(formService.validateToken(params)) {
            AbstractPropertyWithCalculatedLastUpdated property = genericOIDService.resolveOID(params.oid)
            property.isPublic = !property.isPublic
            property.save()
            Org contextOrg = contextService.getOrg()
            request.setAttribute("editable", params.editable == "true")
            if(params.propDefGroup) {
                render(template: "/templates/properties/group", model: [
                        ownobj          : property.owner,
                        newProp         : property,
                        contextOrg      : contextOrg,
                        showConsortiaFunctions: params.showConsortiaFunctions == "true",
                        propDefGroup    : genericOIDService.resolveOID(params.propDefGroup),
                        custom_props_div: "${params.custom_props_div}", // JS markup id
                        prop_desc       : property.type.descr // form data
                ])
            }
            else {
                Map<String, Object>  allPropDefGroups = property.owner.getCalculatedPropDefGroups(contextOrg)

                Map<String, Object> modelMap =  [
                        ownobj                : property.owner,
                        newProp               : property,
                        contextOrg            : contextOrg,
                        showConsortiaFunctions: params.showConsortiaFunctions == "true",
                        custom_props_div      : "${params.custom_props_div}", // JS markup id
                        prop_desc             : property.type.descr, // form data
                        orphanedProperties    : allPropDefGroups.orphanedProperties
                ]
                render(template: "/templates/properties/custom", model: modelMap)
            }
        }
    }

    /**
     * Toggles inheritance of the given property, i.e. passes or retires a property to or from member objects
     * This change is being reflected via a {@link PendingChange} because the inheritance means a change on the object itself and each consortium may decide
     * whether the changes should be automatically applied or only after confirmation.
     * If a property change is being applied, the following rules hold:
     * <ul>
     *     <li>if it does not exist in the given member object, it will be created</li>
     *     <li>if a property of the same definition exists and multiple occurrence is active for the property definition, it will be created</li>
     *     <li>if a property of the same definition exists and multiple occurrence is inactive, the current value will be overwritten</li>
     *     <li>if the property exists in the member object, it will be deleted</li>
     * </ul>
     * The property enumeration fragment is being updated after the process
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def togglePropertyAuditConfig() {
        def className = params.propClass.split(" ")[1]
        def propClass = Class.forName(className)
        def owner     = AppUtils.getDomainClass( params.ownerClass )?.getClazz()?.get(params.ownerId)
        def property  = propClass.get(params.id)
        def prop_desc = property.getType().getDescr()
        Org contextOrg = contextService.getOrg()

        if (AuditConfig.getConfig(property, AuditConfig.COMPLETE_OBJECT)) {
            AuditConfig.removeAllConfigs(property)

            property.getClass().findAllByInstanceOf(property).each{ prop ->
                prop.delete()
            }
        }
        else {
            owner.getClass().findAllByInstanceOf(owner).each { member ->

                def existingProp = property.getClass().findByOwnerAndInstanceOf(member, property)
                if (! existingProp) {

                    // multi occurrence props; add one additional with backref
                    if (property.type.multipleOccurrence) {
                        AbstractPropertyWithCalculatedLastUpdated additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, property.type, contextOrg)
                        additionalProp = property.copyInto(additionalProp)
                        additionalProp.instanceOf = property
                        additionalProp.isPublic = true
                        additionalProp.save()
                    }
                    else {
                        List<AbstractPropertyWithCalculatedLastUpdated> matchingProps = property.getClass().findAllByOwnerAndTypeAndTenant(member, property.type, contextOrg)
                        // unbound prop found with matching type, set backref
                        if (matchingProps) {
                            matchingProps.each { AbstractPropertyWithCalculatedLastUpdated memberProp ->
                                memberProp.instanceOf = property
                                memberProp.isPublic = true
                                memberProp.save()
                            }
                        }
                        else {
                            // no match found, creating new prop with backref
                            AbstractPropertyWithCalculatedLastUpdated newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, property.type, contextOrg)
                            newProp = property.copyInto(newProp)
                            newProp.instanceOf = property
                            newProp.isPublic = true
                            newProp.save()
                        }
                    }
                }
            }

            AuditConfig.addConfig(property, AuditConfig.COMPLETE_OBJECT)
        }

        request.setAttribute("editable", params.editable == "true")
        if (params.propDefGroup) {
          render(template: "/templates/properties/group", model: [
                  ownobj          : owner,
                  newProp         : property,
                  showConsortiaFunctions: params.showConsortiaFunctions,
                  propDefGroup    : genericOIDService.resolveOID(params.propDefGroup),
                  contextOrg      : contextOrg,
                  custom_props_div: "${params.custom_props_div}", // JS markup id
                  prop_desc       : prop_desc // form data
          ])
        }
        else {
            Map<String, Object>  allPropDefGroups = owner.getCalculatedPropDefGroups(contextService.getOrg())

            Map<String, Object> modelMap =  [
                    ownobj                : owner,
                    newProp               : property,
                    showConsortiaFunctions: params.showConsortiaFunctions,
                    custom_props_div      : "${params.custom_props_div}", // JS markup id
                    prop_desc             : prop_desc, // form data
                    contextOrg            : contextOrg,
                    orphanedProperties    : allPropDefGroups.orphanedProperties
            ]
            render(template: "/templates/properties/custom", model: modelMap)
        }
    }

    /**
     * Removes the given custom property from the object
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def deleteCustomProperty() {
        def className = params.propClass.split(" ")[1]
        def propClass = Class.forName(className)
        def owner     = AppUtils.getDomainClass( params.ownerClass )?.getClazz()?.get(params.ownerId)
        def property  = propClass.get(params.id)
        def prop_desc = property.getType().getDescr()
        Org contextOrg = contextService.getOrg()

        AuditConfig.removeAllConfigs(property)

        owner.propertySet.remove(property)

        try {
            property.delete()
        } catch (Exception e) {
            log.error(" TODO: fix property.delete() when instanceOf ")
        }


        if(property.hasErrors()) {
            log.error(property.errors.toString())
        }
        else {
            log.debug("Deleted custom property: " + property.type.name)
        }
        request.setAttribute("editable", params.editable == "true")
        boolean showConsortiaFunctions = Boolean.parseBoolean(params.showConsortiaFunctions)
        if(params.propDefGroup) {
          render(template: "/templates/properties/group", model: [
                  ownobj          : owner,
                  newProp         : property,
                  showConsortiaFunctions: showConsortiaFunctions,
                  contextOrg      : contextOrg,
                  propDefGroup    : genericOIDService.resolveOID(params.propDefGroup),
                  propDefGroupBinding : genericOIDService.resolveOID(params.propDefGroupBinding),
                  custom_props_div: "${params.custom_props_div}", // JS markup id
                  prop_desc       : prop_desc // form data
          ])
        }
        else {
            Map<String, Object> allPropDefGroups = owner.getCalculatedPropDefGroups(contextOrg)
            Map<String, Object> modelMap =  [
                    ownobj                : owner,
                    newProp               : property,
                    showConsortiaFunctions: showConsortiaFunctions,
                    contextOrg            : contextOrg,
                    custom_props_div      : "${params.custom_props_div}", // JS markup id
                    prop_desc             : prop_desc, // form data
                    orphanedProperties    : allPropDefGroups.orphanedProperties
            ]

            render(template: "/templates/properties/custom", model: modelMap)
        }
    }

  /**
    * Deletes the given domain specific private property from the object
    */
  @Secured(['ROLE_USER'])
  @Transactional
  def deletePrivateProperty(){
    def className = params.propClass.split(" ")[1]
    def propClass = Class.forName(className)
    def property  = propClass.get(params.id)
    def tenant    = property.type.tenant
    def owner     = AppUtils.getDomainClass( params.ownerClass )?.getClazz()?.get(params.ownerId)
    def prop_desc = property.getType().getDescr()

    owner.propertySet.remove(property)
    property.delete()

    if(property.hasErrors()){
      log.error(property.errors.toString())
    } else{
      log.debug("Deleted private property: " + property.type.name)
    }
    request.setAttribute("editable", params.editable == "true")
    render(template: "/templates/properties/private", model:[
            ownobj: owner,
            tenant: tenant,
            newProp: property,
            contextOrg: contextService.getOrg(),
            propertyWrapper: "private-property-wrapper-${tenant.id}",  // JS markup id
            prop_desc: prop_desc // form data
    ])
  }

    /**
     * Hides the given dashboard due date
     */
    @Secured(['ROLE_USER'])
    def hideDashboardDueDate(){
        setDashboardDueDateIsHidden(true)
    }

    /**
     * Shows the given dashboard due date
     */
    @Secured(['ROLE_USER'])
    def showDashboardDueDate(){
        setDashboardDueDateIsHidden(false)
    }

    /**
     * Shows or hides the given dashboard due date
     * @param isHidden is the dashboard due date hidden?
     */
    @Secured(['ROLE_USER'])
    @Transactional
    private setDashboardDueDateIsHidden(boolean isHidden){
        log.debug("Hide/Show Dashboard DueDate - isHidden="+isHidden)

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        flash.error = ''

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to access ${contextService.getOrg().name} pages. Please request access on the profile page"
            response.sendError(401)
            return;
        }

        if (params.owner) {
            DashboardDueDate dueDate = (DashboardDueDate) genericOIDService.resolveOID(params.owner)
            if (dueDate){
                dueDate.isHidden = isHidden
                dueDate.save()
            } else {
                if (isHidden)   flash.error += message(code:'dashboardDueDate.err.toHide.doesNotExist')
                else            flash.error += message(code:'dashboardDueDate.err.toShow.doesNotExist')
            }
        } else {
            if (isHidden)   flash.error += message(code:'dashboardDueDate.err.toHide.doesNotExist')
            else            flash.error += message(code:'dashboardDueDate.err.toShow.doesNotExist')
        }

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')
        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.dashboardDueDatesOffset = result.offset

        result.dueDates = dashboardDueDatesService.getDashboardDueDates(contextService.getUser(), contextService.getOrg(), false, false, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.getDashboardDueDates(contextService.getUser(), contextService.getOrg(), false, false).size()

        render (template: "/user/tableDueDates", model: [dueDates: result.dueDates, dueDatesCount: result.dueDatesCount, max: result.max, offset: result.offset])
    }

    /**
     * Marks the given due date as completed
     */
    @Secured(['ROLE_USER'])
    def dashboardDueDateSetIsDone() {
       setDashboardDueDateIsDone(true)
    }

    /**
     * Marks the given due date as undone
     */
    @Secured(['ROLE_USER'])
    def dashboardDueDateSetIsUndone() {
       setDashboardDueDateIsDone(false)
    }

    /**
     * Marks the given due date as done or undone
     * @param isDone is the due date completed or not?
     */
    @Secured(['ROLE_USER'])
    @Transactional
    private setDashboardDueDateIsDone(boolean isDone){
        log.debug("Done/Undone Dashboard DueDate - isDone="+isDone)

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        flash.error = ''

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to access ${contextService.getOrg().name} pages. Please request access on the profile page"
            response.sendError(401)
            return
        }


        if (params.owner) {
            DueDateObject dueDateObject = (DueDateObject) genericOIDService.resolveOID(params.owner)
            if (dueDateObject){
                Object obj = genericOIDService.resolveOID(dueDateObject.oid)
                if (obj instanceof Task && isDone){
                    Task dueTask = (Task)obj
                    dueTask.setStatus(RDStore.TASK_STATUS_DONE)
                    dueTask.save()
                }
                dueDateObject.isDone = isDone
                dueDateObject.save()
            } else {
                if (isDone)   flash.error += message(code:'dashboardDueDate.err.toSetDone.doesNotExist')
                else          flash.error += message(code:'dashboardDueDate.err.toSetUndone.doesNotExist')
            }
        } else {
            if (isDone)   flash.error += message(code:'dashboardDueDate.err.toSetDone.doesNotExist')
            else          flash.error += message(code:'dashboardDueDate.err.toSetUndone.doesNotExist')
        }

        result.is_inst_admin = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_ADM')
        result.editable = accessService.checkMinUserOrgRole(result.user, result.institution, 'INST_EDITOR')

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.dashboardDueDatesOffset = result.offset

        result.dueDates = dashboardDueDatesService.getDashboardDueDates(contextService.getUser(), contextService.getOrg(), false, false, result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.getDashboardDueDates(contextService.getUser(), contextService.getOrg(), false, false).size()

        render (template: "/user/tableDueDates", model: [dueDates: result.dueDates, dueDatesCount: result.dueDatesCount, max: result.max, offset: result.offset])
    }

    /**
     * Deletes a person (contact)-object-relation. Is a substitution call for {@link #deletePersonRole()}
     * @see Person
     * @see PersonRole
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def delete() {
      switch(params.cmd) {
        case 'deletePersonRole': deletePersonRole()
        break
        default: def obj = genericOIDService.resolveOID(params.oid)
          if (obj) {
            obj.delete()
          }
        break
      }
      redirect(url: request.getHeader('referer'))
    }

    /**
     * Deletes a person (contact)-object-relation
     * @see Person
     * @see PersonRole
     */
    @Secured(['ROLE_ORG_EDITOR'])
    @Transactional
    def deletePersonRole(){
        def obj = genericOIDService.resolveOID(params.oid)
        if (obj) {
            obj.delete()
        }
    }

    /**
     * De-/activates the editing mode in certain views. Viewing mode prevents editing of values in those views despite
     * the context user has editing rights to the object
     * @return the changed view
     */
    @Transactional
    @Secured(['ROLE_USER'])
    def toggleEditMode() {
        log.debug ('toggleEditMode()')

        User user = contextService.getUser()
        def show = params.showEditMode

        if (show) {
            def setting = user.getSetting(UserSetting.KEYS.SHOW_EDIT_MODE, RDStore.YN_YES)

            if (show == 'true') {
                setting.setValue(RDStore.YN_YES)
            }
            else if (show == 'false') {
                setting.setValue(RDStore.YN_NO)
            }
        }
        render show
    }

    /**
     * Adds an identifier to the given owner object
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addIdentifier() {
        log.debug("AjaxController::addIdentifier ${params}")
        def owner = genericOIDService.resolveOID(params.owner)
        def namespace = genericOIDService.resolveOID(params.namespace)
        String value = params.value?.trim()

        if (owner && namespace && value) {
            FactoryResult fr = Identifier.constructWithFactoryResult([value: value, reference: owner, note: params.note.trim(), namespace: namespace])

            fr.setFlashScopeByStatus(flash)
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Removes the given identifier from the given owner object
     */
    @Secured(['ROLE_USER'])
    def deleteIdentifier() {
        identifierService.deleteIdentifier(params.owner,params.target)
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Adds a new object to a given collection. Currently only used for UserController.edit()
     */
    @Transactional
    @Secured(['ROLE_USER'])
  def addToCollection() {
    log.debug("AjaxController::addToCollection ${params}");

    def contextObj = resolveOID2(params.__context)
    GrailsClass domain_class = AppUtils.getDomainClass( params.__newObjectClass )
    if ( domain_class ) {

      if ( contextObj ) {
        // log.debug("Create a new instance of ${params.__newObjectClass}");

        def new_obj = domain_class.getClazz().newInstance();

        domain_class.getPersistentProperties().each { p -> // list of GrailsDomainClassProperty
          // log.debug("${p.name} (assoc=${p.isAssociation()}) (oneToMany=${p.isOneToMany()}) (ManyToOne=${p.isManyToOne()}) (OneToOne=${p.isOneToOne()})");
          if ( params[p.name] ) {
            if ( p.isAssociation() ) {
              if ( p.isManyToOne() || p.isOneToOne() ) {
                // Set ref property
                // log.debug("set assoc ${p.name} to lookup of OID ${params[p.name]}");
                // if ( key == __new__ then we need to create a new instance )
                def new_assoc = resolveOID2(params[p.name])
                if(new_assoc){
                  new_obj[p.name] = new_assoc
                }
              }
              else {
                // Add to collection
                // log.debug("add to collection ${p.name} for OID ${params[p.name]}");
                new_obj[p.name].add(resolveOID2(params[p.name]))
              }
            }
            else {
              // log.debug("Set simple prop ${p.name} = ${params[p.name]}");
              new_obj[p.name] = params[p.name]
            }
          }
        }

        if ( params.__recip ) {
          // log.debug("Set reciprocal property ${params.__recip} to ${contextObj}");
          new_obj[params.__recip] = contextObj
        }

        // log.debug("Saving ${new_obj}");
        try{
          if ( new_obj.save() ) {
            log.debug("Saved OK");
          }
          else {
            flash.domainError = new_obj
            new_obj.errors.each { e ->
              log.debug("Problem ${e}");
            }
          }
        }catch(Exception ex){

            flash.domainError = new_obj
            new_obj.errors.each { e ->
            log.debug("Problem ${e}");
            }
        }
      }
      else {
        log.debug("Unable to locate instance of context class with oid ${params.__context}");
      }
    }
    else {
      log.error("Unable to lookup domain class ${params.__newObjectClass}");
    }
    redirect(url: request.getHeader('referer'))
  }

    /**
     * Resolves the given oid and returns the object if found
     * @param oid the oid key to resolve
     * @return the object matching to the given oid, null otherwise
     */
  def resolveOID2(String oid) {
    String[] oid_components = oid.split(':')
    def result

    GrailsClass domain_class = AppUtils.getDomainClass(oid_components[0])
    if (domain_class) {
      if (oid_components[1] == '__new__') {
        result = domain_class.getClazz().refdataCreate(oid_components)
        // log.debug("Result of create ${oid} is ${result?.id}");
      }
      else {
        result = domain_class.getClazz().get(oid_components[1])
      }
    }
    else {
      log.error("resolve OID failed to identify a domain class. Input was ${oid_components}");
    }
    result
  }

    /**
     * Removes a given object from the given owner and refreshes the owner's collection
     */
    @Transactional
    @Secured(['ROLE_USER'])
  def deleteThrough() {
    // log.debug("deleteThrough(${params})");
    def context_object = resolveOID2(params.contextOid)
    def target_object = resolveOID2(params.targetOid)
    if ( context_object."${params.contextProperty}".contains(target_object) ) {
      def otr = context_object."${params.contextProperty}".remove(target_object)
      target_object.delete()
      context_object.save()
    }
    redirect(url: request.getHeader('referer'))

  }

    /**
     * This is the call route for processing an xEditable change other than reference data or role
     * @return the new value for display update in the xEditable field
     * @see SemanticUiInplaceTagLib#xEditable
     * @see SemanticUiInplaceTagLib#xEditableAsIcon
     * @see SemanticUiInplaceTagLib#xEditableBoolean
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def editableSetValue() {
        log.debug("editableSetValue ${params}")

        def result = null
        def target_object = resolveOID2(params.pk)

        try {
            if (target_object) {
                switch(params.type) {
                    case 'date':
                        SimpleDateFormat sdf = DateUtils.getSDF_NoTime()
                        def backup = target_object."${params.name}"

                        try {
                            if (params.value && params.value.size() > 0) {
                                // parse new date
                                def parsed_date = sdf.parse(params.value)
                                target_object."${params.name}" = parsed_date
                            } else {
                                // delete existing date
                                target_object."${params.name}" = null
                            }
                            target_object.save(failOnError: true)
                        }
                        catch (Exception e) {
                            target_object."${params.name}" = backup
                            log.error(e.toString())
                        }
                        finally {
                            if (target_object."${params.name}") {
                                result = (target_object."${params.name}").format(message(code: 'default.date.format.notime'))
                            }
                        }
                        break
                    case 'url':
                        def backup = target_object."${params.name}"

                        try {
                            if (params.value && params.value.size() > 0) {
                                target_object."${params.name}" = new URL(params.value)
                            } else {
                                // delete existing url
                                target_object."${params.name}" = null
                            }
                            target_object.save(failOnError: true)
                        }
                        catch (Exception e) {
                            target_object."${params.name}" = backup
                            log.error(e.toString())
                        }
                        finally {
                            if (target_object."${params.name}") {
                                result = target_object."${params.name}"
                            }
                        }
                        break
                    case 'readerNumber':
                        if(target_object.semester)
                            ReaderNumber.executeUpdate('update ReaderNumber rn set rn.dateGroupNote = :note where rn.org = :org and rn.semester = :semester',[org: target_object.org, semester: target_object.semester, note: params.value])
                        else if(target_object.dueDate)
                            ReaderNumber.executeUpdate('update ReaderNumber rn set rn.dateGroupNote = :note where rn.org = :org and rn.dueDate = :dueDate',[org: target_object.org, dueDate: target_object.dueDate, note: params.value])
                        result = params.value
                        break
                    default:
                        Map binding_properties = [:]

                        if (target_object."${params.name}" instanceof BigDecimal) {
                            params.value = escapeService.parseFinancialValue(params.value)
                        }
                        if (target_object."${params.name}" instanceof Boolean) {
                            params.value = params.value?.equals("1")
                        }
                        if (params.value instanceof String) {
                            String value = params.value.startsWith('www.') ? ('http://' + params.value) : params.value
                            binding_properties[params.name] = value
                        } else {
                            binding_properties[params.name] = params.value
                        }
                        bindData(target_object, binding_properties)

                        target_object.save(failOnError: true)


                        if (target_object."${params.name}" instanceof BigDecimal) {
                            result = NumberFormat.getInstance(LocaleContextHolder.getLocale()).format(target_object."${params.name}")
                            //is for that German users do not cry about comma-dot-change
                        } else {
                            result = target_object."${params.name}"
                        }
                        break
                }

                if (target_object instanceof SurveyResult) {

                    Org org = contextService.getOrg()
                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(target_object.surveyConfig, target_object.participant)

                    //If Survey Owner set Value then set FinishDate
                    if (org?.id == target_object.owner.id && (target_object.type == RDStore.SURVEY_PROPERTY_PARTICIPATION) && surveyOrg.finishDate == null) {
                        String property = target_object.type.getImplClassValueProperty()

                        if (target_object[property] != null) {
                            log.debug("Set/Save FinishDate of SurveyOrg (${surveyOrg.id})")
                            surveyOrg.finishDate = new Date()
                            surveyOrg.save()
                        }
                    }
                }

            }

        } catch (Exception e) {
            log.error("@ editableSetValue()")
            log.error(e.toString())
        }

        log.debug("editableSetValue() returns ${result}")

        response.setContentType('text/plain')

        def outs = response.outputStream
        outs << result
        outs.flush()
        outs.close()
    }

    /**
     * Revokes the given role from the given user
     */
    @Secured(['ROLE_USER'])
    def removeUserRole() {
        User user = resolveOID2(params.user)
        Role role = resolveOID2(params.role)
        if (user && role) {
            UserRole.remove(user, role)
        }
        redirect(url: request.getHeader('referer'))
    }

  @Deprecated
  def renderObjectValue(value) {
    def result=''
    def not_set = message(code:'refdata.notSet')

    if ( value ) {
      switch ( value.class ) {
        case RefdataValue.class:

          if ( value.icon != null ) {
            result="<span class=\"select-icon ${value.icon}\"></span>";
            result += value.value ? value.getI10n('value') : not_set
          }
          else {
            result = value.value ? value.getI10n('value') : not_set
          }
          break
        default:
          if(value instanceof String){

          }else{
            value = value.toString()
          }
          def no_ws = value.replaceAll(' ','')

          result = message(code:"refdata.${no_ws}", default:"${value ?: not_set}")
      }
    }
    // log.debug("Result of render: ${value} : ${result}");
    result
  }

    /**
     * Deletes the given task
     */
    @DebugAnnotation(perm="ORG_INST,ORG_CONSORTIUM", affil="INST_EDITOR")
    @Secured(closure = {
        ctx.accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM", "INST_EDITOR")
    })
    def deleteTask() {

        if (params.deleteId) {
            Task.withTransaction {
                Task dTask = Task.get(params.deleteId)
                if (dTask && dTask.creator.id == contextService.getUser().id) {
                    try {
                        flash.message = message(code: 'default.deleted.message', args: [message(code: 'task.label'), dTask.title])
                        dTask.delete()
                    }
                    catch (Exception e) {
                        log.error(e)
                        flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'task.label'), dTask.title])
                    }
                } else {
                    if (!dTask) {
                        flash.error = message(code: 'default.not.found.message', args: [message(code: 'task.label'), params.deleteId])
                    } else {
                        flash.error = message(code: 'default.noPermissions')
                    }
                }
            }
        }
        if(params.returnToShow) {
            redirect action: 'show', id: params.id, controller: params.returnToShow
            return
        }
        else {
            redirect(url: request.getHeader('referer'))
            return
        }
    }

    @Secured(['ROLE_USER'])
    def dashboardChangesSetAccept() {
        setDashboardChangesStatus(RDStore.PENDING_CHANGE_ACCEPTED)
    }

    @Secured(['ROLE_USER'])
    def dashboardChangesSetReject() {
        setDashboardChangesStatus(RDStore.PENDING_CHANGE_REJECTED)
    }

    @Secured(['ROLE_USER'])
    @Transactional
    private setDashboardChangesStatus(RefdataValue refdataValue){
        log.debug("DsetDashboardChangesStatus - refdataValue="+refdataValue.value)

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        flash.error = ''

        if (! accessService.checkUserIsMember(result.user, result.institution)) {
            flash.error = "You do not have permission to access ${contextService.getOrg().name} pages. Please request access on the profile page"
            response.sendError(401)
            return
        }

        if (params.id) {
            PendingChange pc = PendingChange.get(params.long('id'))
            if (pc){
                pc.status = refdataValue
                pc.actionDate = new Date()
                if(!pc.save()) {
                    throw new ChangeAcceptException("problems when submitting new pending change status: ${pc.errors}")
                }
            } else {
                flash.error += message(code:'dashboardChanges.err.toChangeStatus.doesNotExist')
            }
        } else {
            flash.error += message(code:'dashboardChanges.err.toChangeStatus.doesNotExist')
        }

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.acceptedOffset = params.acceptedOffset ? params.int("acceptedOffset") : result.offset
        result.pendingOffset = params.pendingOffset ? params.int("pendingOffset") : result.offset
        def periodInDays = result.user.getSettingsValue(UserSetting.KEYS.DASHBOARD_ITEMS_TIME_WINDOW, 14)
        Map<String, Object> pendingChangeConfigMap = [contextOrg:result.institution,consortialView:accessService.checkPerm(result.institution,"ORG_CONSORTIUM"),periodInDays:periodInDays,max:result.max,acceptedOffset:result.acceptedOffset, pendingOffset: result.pendingOffset]
        Map<String, Object> changes = pendingChangeService.getChanges(pendingChangeConfigMap)
        changes.max = result.max
        changes.editable = result.editable
        render template: '/myInstitution/changesWrapper', model: changes
    }
}
