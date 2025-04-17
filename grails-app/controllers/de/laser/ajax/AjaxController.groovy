package de.laser.ajax

import de.laser.addressbook.Address
import de.laser.addressbook.Person
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserRole
import de.laser.*
import de.laser.base.AbstractI10n
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.cache.EhcacheWrapper
import de.laser.cache.SessionCacheWrapper
import de.laser.convenience.Marker
import de.laser.ctrl.SubscriptionControllerService
import de.laser.helper.*
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.MarkerSupport
import de.laser.interfaces.ShareSupport
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinition
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import de.laser.properties.SubscriptionProperty
import de.laser.storage.PropertyStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.survey.SurveyConfig
import de.laser.survey.SurveyOrg
import de.laser.survey.SurveyResult
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import de.laser.utils.SwissKnife
import de.laser.wekb.Package
import de.laser.wekb.Platform
import de.laser.wekb.Provider
import de.laser.wekb.ProviderRole
import de.laser.wekb.TitleInstancePackagePlatform
import de.laser.wekb.Vendor
import de.laser.wekb.VendorRole
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.annotation.Secured
import groovy.sql.GroovyRowResult
import org.apache.http.HttpStatus
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.transaction.TransactionStatus
import org.springframework.web.servlet.LocaleResolver
import org.springframework.web.servlet.support.RequestContextUtils

import javax.servlet.ServletOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Year
import java.util.concurrent.ExecutorService

/**
 * This controller manages AJAX calls which result in object manipulation and / or do not deliver clearly either HTML or JSON.
 * Methods defined here are accessible only with a registered account; general methods which require no authentication are defined in {@link AjaxOpenController}
 * @see AjaxOpenController
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class AjaxController {

    AccessService accessService
    BatchQueryService batchQueryService
    ContextService contextService
    DashboardDueDatesService dashboardDueDatesService
    EscapeService escapeService
    ExecutorService executorService
    FilterService filterService
    FormService formService
    GenericOIDService genericOIDService
    IdentifierService identifierService
    IssueEntitlementService issueEntitlementService
    LinksGenerationService linksGenerationService
    ManagementService managementService
    PackageService packageService
    PropertyService propertyService
    SubscriptionControllerService subscriptionControllerService
    SubscriptionService subscriptionService
    TitleService titleService

    def refdata_config = [
    "Licenses" : [
      domain:'License',
      countQry:"select count(*) from License as l",
      rowQry:"select l from License as l",
      qryParams:[],
      cols:['reference'],
      format:'simple'
    ],
    'Currency' : [
      domain:'RefdataValue',
      countQry:"select count(*) from RefdataValue as rdv where rdv.owner.desc='" + RDConstants.CURRENCY + "'",
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
            countQry:"select count(*) from Org as o where lower(o.name) like :oname",
            rowQry:"select o from Org as o where lower(o.name) like :oname order by o.name asc",
            qryParams:[
                    [
                            param:'sSearch',
                            onameClosure: { value ->
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
    @Secured(['ROLE_USER'])
    def test() {
        render 'test()'
    }

    /**
     * Renders the given dialog message template
     * @return the template responding the given parameter
     */
    @Secured(['ROLE_USER'])
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
    @Secured(['ROLE_USER'])
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
     * @see XEditableTagLib#xEditableRole
     * @see XEditableTagLib#xEditableRefData
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def genericSetData() {
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

                        if (!target.save()) {
                            Map r = [status: "error", msg: message(code: 'default.save.error.general.message')]
                            render r as JSON
                            return
                        }

                        if (target instanceof SurveyResult) {
                            Org org = contextService.getOrg()
                            SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(target.surveyConfig, target.participant)

                            //If Survey Owner set Value then set FinishDate
                            if (org?.id == target.owner.id && (target.type == PropertyStore.SURVEY_PROPERTY_PARTICIPATION) && surveyOrg.finishDate == null) {
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

                        if (target instanceof Subscription) {
                            if(params.name == 'holdingSelection') {
                                Org ctx = contextService.getOrg()
                                Subscription sub = (Subscription) target
                                Map<String, Object> configMap = [sub: sub, value: value]
                                subscriptionService.switchPackageHoldingInheritance(configMap)
                                List<Long> subChildIDs = sub.getDerivedSubscriptions().id
                                if(value == RDStore.SUBSCRIPTION_HOLDING_ENTIRE) {
                                    executorService.execute({
                                        String threadName = 'PackageUnlink_'+sub.id
                                        Thread.currentThread().setName(threadName)
                                        sub.packages.each { SubscriptionPackage sp ->
                                            if(!packageService.unlinkFromSubscription(sp.pkg, subChildIDs, ctx, false)){
                                                log.error('error on clearing issue entitlements when changing package holding selection')
                                            }
                                        }
                                    })
                                }
                            }
                        }

                        if (params.resultProp) {
                            result = value[params.resultProp]
                        } else {
                            if (value) {
                                result = _renderObjectValue(value)
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
            log.error("@ genericSetData()")
            log.error(e.toString())
        }

        Map resp = [newValue: result]
        log.debug("genericSetData() returns ${resp as JSON}")
        render resp as JSON
    }

    /**
     * Retrieves lists for a reference data value dropdown. The config parameter is used to build the underlying query
     * @return a {@link List} of {@link Map}s of structure [value: oid, text: text] to be used in dropdowns; the list may be returned purely or as JSON
     */
    @Secured(['ROLE_USER'])
    def remoteRefdataSearch() {
        log.debug("remoteRefdataSearch params: ${params}")

        List result = []
        Map<String, Object> config = refdata_config.get(params.id?.toString()) //we call toString in case we got a GString
        boolean defaultOrder = true
        def obj = genericOIDService.resolveOID(params.oid)

        if (config == null) {
            String lang = LocaleUtils.getCurrentLang()
            defaultOrder = false
            // If we werent able to locate a specific config override, assume the ID is just a refdata key
            config = [
                domain      :'RefdataValue',
                countQry    :"select count(*) from RefdataValue as rdv where rdv.owner.desc='" + params.id + "'",
                rowQry      :"select rdv from RefdataValue as rdv where rdv.owner.desc='" + params.id + "' order by rdv.order asc, rdv.value_" + lang,
                qryParams   :[],
                cols        :['value'],
                format      :'simple'
            ]
        }

    if ( config ) {

      Map<String, Object> query_params = [:]
      config.qryParams.each { qp ->
        if ( qp.onameClosure) {
          query_params.putAt('oname', qp.onameClosure(params[qp.param]?:''));
        }
        else if( qp.value ) {
            params."${qp.param}" = qp.value
        }
//        else {
//          query_params.add(params[qp.param]);
//        }
      }
      def rq = RefdataValue.executeQuery(config.rowQry,
                                query_params,
                                [max:params.iDisplayLength?:1000,offset:params.iDisplayStart?:0]);

      rq.each { it ->
        def rowobj = GrailsHibernateUtil.unwrapIfProxy(it)

          // handle custom constraint(s) ..
          if (it.value.equalsIgnoreCase('deleted') && params.constraint?.contains('removeValue_deleted')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          else if ((obj instanceof SubscriptionProperty || obj instanceof LicenseProperty) && obj.owner._getCalculatedType() != CalculatedType.TYPE_CONSORTIAL && it == RDStore.INVOICE_PROCESSING_PROVIDER_OR_VENDOR && params.constraint?.contains('removeValues_processingProvOrVendor')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          else if (it in [RDStore.INVOICE_PROCESSING_CONSORTIUM, RDStore.INVOICE_PROCESSING_NOT_SET, RDStore.INVOICE_PROCESSING_PROVIDER_OR_VENDOR] && params.constraint?.contains('removeValues_invoiceProcessing')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          else if (it.value.equalsIgnoreCase('administrative subscription') && params.constraint?.contains('removeValue_administrativeSubscription')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          //value is correct incorrectly translated!
          else if (it.value.equalsIgnoreCase('local subscription') && contextService.getOrg().isCustomerType_Consortium() && params.constraint?.contains('removeValue_localSubscription')) {
              log.debug('ignored value "' + it + '" from result because of constraint: '+ params.constraint)
          }
          // default ..
          else {
              if (it instanceof AbstractI10n) {
                  result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${it.getI10n(config.cols[0])}", order: it.order])
              }
              else {
                  def objTest = rowobj[config.cols[0]]
                  if (objTest) {
                      def no_ws = objTest.replaceAll(' ', '');
                      String local_text = message(code: "refdata.${no_ws}", default: "${objTest}") as String
                      result.add([value: "${rowobj.class.name}:${rowobj.id}", text: "${local_text}"])
                  }
              }
          }
      }
    }
    else {
      log.error("No config for refdata search ${params.id}");
    }

        if(params.id == 'Currency') {
            result.sort{ x,y -> x.order.compareTo y.order  }
        }
      else if (result && defaultOrder) {
          result.sort{ x,y -> x.text.compareToIgnoreCase y.text  }
      }

    render result as JSON
    }

    /**
     * This method is used for the property distribution view at manageProperties and controls which objects have been selected for later processing.
     * The caching ensures that the selections remain also when the page of results is being changed; this method updates a respective cache entry or (de-)selects the whole selection
     * @return a {@link Map} reflecting the success status
     */
    @Secured(['ROLE_USER'])
    def updatePropertiesSelection() {
        Map success = [success: false]
        EhcacheWrapper cache = contextService.getUserCache("/manageProperties")
        List<String> checkedProperties = cache.get(params.table) ?: []
        boolean check = Boolean.valueOf(params.checked)
        if(params.key == "all") {
            if(check) {
                PropertyDefinition propDef = genericOIDService.resolveOID(params.propDef)
                Map<String, Object> propertyData = propertyService.getAvailableProperties(propDef, contextService.getOrg(), params)
                switch (params.table) {
                    case "with":
                        checkedProperties.addAll(propertyData.withProp.collect { o -> o.id })
                        break
                    case [ "without", "audit" ]:
                        checkedProperties.addAll(propertyData.withoutProp.collect { o -> o.id })
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
      String sub = params.sub ?: params.id
      EhcacheWrapper userCache = contextService.getUserCache("/subscription/${params.referer}/${sub}")
      Map<String,Object> cache = userCache.get('selectedTitles')

      if(!cache) {
          cache = ["checked":[:]]
      }

      Map checked = cache.get('checked')

      if(params.index == 'all') {
          Map<String, Object> filterParams = [:]
          if(params.filterParams){
              JSON.parse(params.filterParams).each {
                  if(it.key in ['series_names', 'subject_references', 'ddcs', 'languages', 'yearsFirstOnline', 'medium', 'title_types', 'publishers', 'hasPerpetualAccess']){
                      if(it.value != '[]') {
                          filterParams[it.key] = []
                          it.value = it.value.replace('[','').replace(']','')
                          //Needed because of filterService -> Params.getLongList_forCommaSeparatedString()
                          if(it.key in ['ddcs', 'languages', 'yearsFirstOnline', 'medium']){
                              filterParams[it.key] = it.value
                          }else {
                              it.value.split(',').each { String paramsValue ->
                                  filterParams[it.key] << paramsValue
                              }
                          }
                      }
                  }else{
                      filterParams[it.key] = it.value
                  }
              }
          }
		  Map<String, String> newChecked = checked ?: [:]
          if(params.referer == 'renewEntitlementsWithSurvey'){
              Map<String, Object> result = subscriptionService.getRenewalGenerics(params)
              IssueEntitlementGroup issueEntitlementGroup = IssueEntitlementGroup.findBySurveyConfigAndSub(result.surveyConfig, result.subscription)
              if(issueEntitlementGroup) {
                  result.titleGroup = issueEntitlementGroup
              }
              else {
                  result.titleGroup = null
              }
              Map<String, Object> parameterGenerics = issueEntitlementService.getParameterGenerics(result.configMap)
              Map<String, Object> titleConfigMap = parameterGenerics.titleConfigMap,
                                  identifierConfigMap = parameterGenerics.identifierConfigMap,
                                  issueEntitlementConfigMap = parameterGenerics.issueEntitlementConfigMap
              //build up title data
              if(!result.configMap.containsKey('status')) {
                  titleConfigMap.tippStatus = RDStore.TIPP_STATUS_CURRENT.id
                  issueEntitlementConfigMap.ieStatus = RDStore.TIPP_STATUS_CURRENT.id
              }
              Map<String, Object> query = filterService.getTippSubsetQuery(titleConfigMap)
              Set<Long> tippIDs = TitleInstancePackagePlatform.executeQuery(query.query, query.queryParams)
              if(result.identifier) {
                  tippIDs = tippIDs.intersect(titleService.getTippsByIdentifier(identifierConfigMap, result.identifier))
              }
              switch (params.tab) {
                  case ['allTipps', 'selectableTipps']:
                      Set<Subscription> subscriptions = []
                      if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                          subscriptions = linksGenerationService.getSuccessionChain(result.subscription, 'sourceSubscription')
                      }
                      //else {
                          subscriptions << result.subscription
                      //}
                      if(subscriptions) {
                          Set rows
                          if(result.surveyConfig.pickAndChoosePerpetualAccess) {
                              rows = IssueEntitlement.executeQuery('select tipp.hostPlatformURL from IssueEntitlement ie join ie.tipp tipp where ie.subscription in (:subs) and ie.perpetualAccessBySub in (:subs) and ie.status = :ieStatus and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup)', [subs: subscriptions, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                          }
                          else {
                              rows = IssueEntitlement.executeQuery('select ie.tipp.hostPlatformURL from IssueEntitlement ie where ie.subscription = :sub and ie.status = :ieStatus and ie not in (select igi.ie from IssueEntitlementGroupItem as igi where igi.ieGroup = :ieGroup)', [sub: result.subscription, ieStatus: RDStore.TIPP_STATUS_CURRENT, ieGroup: issueEntitlementGroup])
                          }
                          rows.collate(65000).each { subSet ->
                              tippIDs.removeAll(TitleInstancePackagePlatform.executeQuery('select tipp.id from TitleInstancePackagePlatform tipp where tipp.hostPlatformURL in (:subSet) and tipp.pkg in (:currSubPkgs)', [subSet: subSet, currSubPkgs: result.subscription.packages.pkg]))
                          }
                      }
                      tippIDs.each { Long tippID ->
                          if(newChecked.containsKey(tippID.toString()))
                              newChecked.remove(tippID.toString())
                          else newChecked.put(tippID.toString(), 'checked')
                      }
                      break
                  case 'selectedIEs': issueEntitlementConfigMap.tippIDs = tippIDs
                      Map<String, Object> queryPart2 = filterService.getIssueEntitlementSubsetSQLQuery(issueEntitlementConfigMap)
                      List<GroovyRowResult> rows = batchQueryService.longArrayQuery(queryPart2.query, queryPart2.arrayParams, queryPart2.queryParams)
                      rows.each { GroovyRowResult row ->
                          if(newChecked.containsKey(row['ie_id']))
                              newChecked.remove(row['ie_id'])
                          else newChecked.put(row['ie_id'], 'checked')
                      }
                      break
              }
          }
          else {
              Set<Package> pkgFilter = []
              if (filterParams.pkgFilter)
                  pkgFilter << Package.get(filterParams.pkgFilter)
              else pkgFilter.addAll(SubscriptionPackage.executeQuery('select sp.pkg from SubscriptionPackage sp where sp.subscription.id = :sub', [sub: params.long("sub")]))
              Map<String, Object> tippFilter = filterService.getTippQuery(filterParams, pkgFilter.toList())
              String query = tippFilter.query.replace("select tipp.id", "select tipp.gokbId")
              query = query.replace("where ", "where not exists (select ie.id from IssueEntitlement ie join ie.tipp tipp2 where ie.subscription.id = :sub and tipp.id = tipp2.id and ie.status = tipp2.status) and ")
              Set<String> tippUUIDs = TitleInstancePackagePlatform.executeQuery(query, tippFilter.queryParams+[sub: params.long("sub")])
              tippUUIDs.each { String e ->
                  if(params.checked == 'true')
                    newChecked[e] = 'checked'
                  else newChecked.remove(e)
              }
          }
          cache.put('checked',newChecked)
          success.checkedCount = params.checked == 'true' ? newChecked.size() : 0
          success.success = success.checkedCount > 0
	  }
	  else {
          Map<String, String> newChecked = checked ?: [:]
          if(params.checked == 'true')
              newChecked[params.index] = 'checked'
          else newChecked.remove(params.index)
          cache.put('checked', newChecked)
          success.success = true
          success.checkedCount = newChecked.findAll {it.value == 'checked'}.size()
	  }
      userCache.put('selectedTitles', cache)
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

        def orgIds = params.list('selectedOrgs')
        orgIds.each{ orgId ->
            Org org_to_link = Org.get(orgId)
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
                duplicateOrgRole = OrgRole.findAllByTippAndRoleTypeAndOrg(owner, rel, org_to_link) ? true : false
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
     * Adds a relation link from a given object to a {@link de.laser.wekb.Vendor}
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addVendorRole() {
        def owner  = genericOIDService.resolveOID(params.parent)
        Set<Vendor> vendors = Vendor.findAllByIdInList(params.list('selectedVendors'))
        if(params.containsKey('takeSelectedSubs')) {
            Set<Subscription> subscriptions = managementService.loadSubscriptions(params, owner)
            if(subscriptions) {
                vendors.each { Vendor vendorToLink ->
                    List<VendorRole> duplicateVendorRoles = VendorRole.findAllBySubscriptionInListAndVendor(subscriptions, vendorToLink)
                    if(duplicateVendorRoles)
                        subscriptions.removeAll(duplicateVendorRoles.subscription)
                    subscriptions.each { Subscription subToLink ->
                        VendorRole new_link = new VendorRole(vendor: vendorToLink, subscription: subToLink)

                        if (new_link.save()) {
                            if (subToLink.checkSharePreconditions(new_link)) {
                                new_link.isShared = true
                                new_link.save()

                                subToLink.updateShare(new_link)
                            }
                        } else {
                            log.error("Problem saving new vendor link ..")
                            new_link.errors.each { e ->
                                log.error( e.toString() )
                            }
                        }
                    }
                }
                managementService.clearSubscriptionCache(params)
            }
            else flash.error = message(code: 'subscriptionsManagement.noSelectedSubscriptions')
        }
        else if(owner) {

            vendors.each{ Vendor vendorToLink ->
                boolean duplicateVendorRole = false

                if(params.recip_prop == 'subscription') {
                    duplicateVendorRole = VendorRole.findAllBySubscriptionAndVendor(owner, vendorToLink) ? true : false
                }
                else if(params.recip_prop == 'license') {
                    duplicateVendorRole = VendorRole.findAllByLicenseAndVendor(owner, vendorToLink) ? true : false
                }

                if(! duplicateVendorRole) {
                    VendorRole new_link = new VendorRole(vendor: vendorToLink)
                    new_link[params.recip_prop] = owner

                    if (new_link.save()) {
                        // log.debug("Org link added")
                        if (owner.checkSharePreconditions(new_link)) {
                            new_link.isShared = true
                            new_link.save()

                            owner.updateShare(new_link)
                        }
                    } else {
                        log.error("Problem saving new vendor link ..")
                        new_link.errors.each { e ->
                            log.error( e.toString() )
                        }
                    }
                }
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Deletes the given relation link between a {@link Vendor} and its target
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def delVendorRole() {
        VendorRole vr = VendorRole.get(params.id)

        def owner
        if(vr.subscription)
            owner = vr.subscription
        else if(vr.license)
            owner = vr.license
        if (owner instanceof ShareSupport && vr.isShared) {
            vr.isShared = false
            owner.updateShare(vr)
        }
        vr.delete()

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Adds a relation link from a given object to a {@link de.laser.wekb.Provider}
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def addProviderRole() {
        def owner  = genericOIDService.resolveOID(params.parent)

        Set<Provider> providers = Provider.findAllByIdInList(params.list('selectedProviders'))
        if(params.containsKey('takeSelectedSubs')) {
            Set<Subscription> subscriptions = managementService.loadSubscriptions(params, owner)
            if(subscriptions) {
                providers.each { Provider providerToLink ->
                    List<ProviderRole> duplicateProviderRoles = ProviderRole.findAllBySubscriptionInListAndProvider(subscriptions, providerToLink)
                    if(duplicateProviderRoles)
                        subscriptions.removeAll(duplicateProviderRoles.subscription)
                    subscriptions.each { Subscription subToLink ->
                        ProviderRole new_link = new ProviderRole(provider: providerToLink, subscription: subToLink)

                        if (new_link.save()) {
                            // log.debug("Org link added")
                            if (subToLink.checkSharePreconditions(new_link)) {
                                new_link.isShared = true
                                new_link.save()

                                subToLink.updateShare(new_link)
                            }
                        } else {
                            log.error("Problem saving new provider link ..")
                            new_link.errors.each { e ->
                                log.error( e.toString() )
                            }
                        }
                    }
                }
                managementService.clearSubscriptionCache(params)
            }
            else flash.error = message(code: 'subscriptionsManagement.noSelectedSubscriptions')
        }
        else if(owner) {
            providers.each{ Provider providerToLink ->
                boolean duplicateProviderRole = false

                if(params.recip_prop == 'subscription') {
                    duplicateProviderRole = ProviderRole.findAllBySubscriptionAndProvider(owner, providerToLink) ? true : false
                }
                else if(params.recip_prop == 'license') {
                    duplicateProviderRole = ProviderRole.findAllByLicenseAndProvider(owner, providerToLink) ? true : false
                }

                if(! duplicateProviderRole) {
                    ProviderRole new_link = new ProviderRole(provider: providerToLink)
                    new_link[params.recip_prop] = owner

                    if (new_link.save()) {
                        // log.debug("Org link added")
                        if (owner.checkSharePreconditions(new_link)) {
                            new_link.isShared = true
                            new_link.save()

                            owner.updateShare(new_link)
                        }
                    } else {
                        log.error("Problem saving new provider link ..")
                        new_link.errors.each { e ->
                            log.error( e.toString() )
                        }
                    }
                }
            }
        }

        redirect(url: request.getHeader('referer'))
    }

    /**
     * Deletes the given relation link between a {@link Provider} its target
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def delProviderRole() {
        ProviderRole pvr = ProviderRole.get(params.id)

        def owner
        if(pvr.subscription)
            owner = pvr.subscription
        else if(pvr.license)
            owner = pvr.license
        if (owner instanceof ShareSupport && pvr.isShared) {
            pvr.isShared = false
            owner.updateShare(pvr)
        }
        pvr.delete()

        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def delAllProviderRoles() {
        Subscription owner = genericOIDService.resolveOID(params.parent)
        Set<Subscription> subscriptions = managementService.loadSubscriptions(params, owner)
        Set<Provider> providers = Provider.findAllByIdInList(params.list('selectedProviders'))
        int sharedProviderRoles = 0
        if(subscriptions && providers) {
            List<ProviderRole> providerRolesToProcess = ProviderRole.executeQuery('select pvr from ProviderRole pvr where pvr.provider in (:providers) and pvr.subscription in (:subscriptions)', [providers: providers, subscriptions: subscriptions])
            providerRolesToProcess.each { ProviderRole pvr ->
                if (!pvr.sharedFrom) {
                    if (pvr.isShared) {
                        pvr.isShared = false
                        pvr.subscription.updateShare(pvr)
                    }
                    pvr.delete()
                }
                else sharedProviderRoles++
            }
        }
        managementService.clearSubscriptionCache(params)
        if(!subscriptions)
            flash.error = message(code: 'subscriptionsManagement.noSelectedSubscriptions')
        if(sharedProviderRoles > 0)
            flash.error = message(code: 'subscription.details.linkProvider.sharedLinks.error', args: [sharedProviderRoles])
        redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def delAllVendorRoles() {
        Subscription owner = genericOIDService.resolveOID(params.parent)
        Set<Subscription> subscriptions = managementService.loadSubscriptions(params, owner)
        Set<Vendor> vendors = Provider.findAllByIdInList(params.list('selectedVendors'))
        int sharedVendorRoles = 0
        if(subscriptions && vendors) {
            List<VendorRole> vendorRolesToProcess = VendorRole.executeQuery('select vr from VendorRole vr where vr.vendor in (:vendors) and vr.subscription in (:subscriptions)', [vendors: vendors, subscriptions: subscriptions])
            vendorRolesToProcess.each { VendorRole vr ->
                if (!vr.sharedFrom) {
                    if (vr.isShared) {
                        vr.isShared = false
                        vr.subscription.updateShare(vr)
                    }
                    vr.delete()
                }
                else sharedVendorRoles++
            }
        }
        managementService.clearSubscriptionCache(params)
        if(!subscriptions)
            flash.error = message(code: 'subscriptionsManagement.noSelectedSubscriptions')
        if(sharedVendorRoles > 0)
            flash.error = message(code: 'subscription.details.linkProvider.sharedLinks.error', args: [sharedVendorRoles])
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
     * (currently, as of August 14th, '23) located at /src/main/webapp/setup
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
     * Adds a value to a custom property and updates the property enumeration fragment
     */
  @Secured(['ROLE_USER'])
  def addCustomPropertyValue(){
    if(params.propIdent.length() > 0) {
      def error
      def newProp
      def owner = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
        PropertyDefinition type = PropertyDefinition.get(params.long('propIdent'))
      def existingProp = owner.propertySet.find { it.type.name == type.name && it.tenant?.id == contextService.getOrg().id }

      if (existingProp == null || type.multipleOccurrence) {
        String propDefConst = type.tenant ? PropertyDefinition.PRIVATE_PROPERTY : PropertyDefinition.CUSTOM_PROPERTY
        newProp = PropertyDefinition.createGenericProperty(propDefConst, owner, type, contextService.getOrg() )
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
                  Org consortium
                  boolean atSubscr
                  List propDefGroupItems = []
                  PropertyDefinitionGroup propDefGroup = genericOIDService.resolveOID(params.propDefGroup)
                  PropertyDefinitionGroupBinding propDefGroupBinding = genericOIDService.resolveOID(params.propDefGroupBinding)
                  boolean isGroupVisible = propDefGroup.isVisible || propDefGroupBinding?.isVisible
                  if (owner instanceof License) {
                      consortium = owner.getLicensingConsortium()
                      atSubscr = owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
                  } else if (owner instanceof Subscription) {
                      consortium = owner.getConsortium()
                      atSubscr = owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
                  }
                  if (isGroupVisible) {
                      propDefGroupItems = propDefGroup.getCurrentProperties(owner)
                  } else if (consortium != null) {
                      propDefGroupItems = propDefGroup.getCurrentPropertiesOfTenant(owner, consortium)
                  }
                  Map<String, Object> modelMap = [
                          isGroupVisible: isGroupVisible,
                          atSubscr: atSubscr,
                          consortium: consortium,
                          propDefGroupItems: propDefGroupItems,
                          ownobj          : owner,
                          newProp         : newProp,
                          error           : error,
                          showConsortiaFunctions: showConsortiaFunctions,
                          propDefGroup    : propDefGroup,
                          propDefGroupBinding : propDefGroupBinding,
                          custom_props_div: "${params.custom_props_div}", // JS markup id
                          prop_desc       : type.descr // form data
                  ]
                render(template: "/templates/properties/group", model: modelMap)
              }
              else {
                  Map<String, Object> allPropDefGroups = owner.getCalculatedPropDefGroups(contextService.getOrg())

                  Map<String, Object> modelMap =  [
                          ownobj                : owner,
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
            availPropDefGroups = availPropDefGroups - propDefGroup
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
          def owner  = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
          PropertyDefinition type = PropertyDefinition.get(params.long('propIdent'))

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
     * Toggles the state of a marker for a we:kb object, i.e. whether it is on the user's watchlist or not
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def toggleMarker() {

        MarkerSupport obj   = genericOIDService.resolveOID(params.oid) as MarkerSupport
        User user           = contextService.getUser()
        Marker.TYPE type    = params.type ? Marker.TYPE.get(params.type) : Marker.TYPE.UNKOWN

        Map attrs = [ type: type, ajax: true ]

        if (params.simple) { attrs.simple = true }

             if (obj instanceof Org)        { attrs.org = obj }
        else if (obj instanceof Package)    { attrs.package = obj }
        else if (obj instanceof Platform)   { attrs.platform = obj }
        else if (obj instanceof Provider)   { attrs.provider = obj }
        else if (obj instanceof Vendor)     { attrs.vendor = obj }
        else if (obj instanceof TitleInstancePackagePlatform) { attrs.tipp = obj }

        if (obj.isMarked(user, type)) {
            obj.removeMarker(user, type)
        }
        else {
            obj.setMarker(user, type)
        }

        render ui.cbItemMarkerAction(attrs, null)
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
                                        else m.setProperty(prop, null)
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
        if(Boolean.valueOf(params.returnSuccessAsJSON))
            render([success: true] as JSON)
        else
            redirect(url: request.getHeader('referer'))
    }

    @Secured(['ROLE_USER'])
    def switchPackageHoldingInheritance() {
        if(formService.validateToken(params)) {
            Subscription sub = Subscription.get(params.id)
            RefdataValue value = RefdataValue.get(params.value)
            Map<String, Object> configMap = [sub: sub, value: value]
            subscriptionService.switchPackageHoldingInheritance(configMap)
        }
        render([success: true] as JSON)
    }

    /**
     * Toggles inheritance of the given identifier object, i.e. passes or retires an identifier to or from member objects
     * This change is being reflected via a {@link PendingChange} because the inheritance means a change on the object itself and each consortium may decide
     * whether the changes should be automatically applied or only after confirmation
     */
    @Secured(['ROLE_USER'])
    def toggleIdentifierAuditConfig() {
        def owner = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
        if(formService.validateToken(params)) {
            Identifier identifier  = Identifier.get(params.id)
            subscriptionService.inheritIdentifier(owner, identifier)
        }
        render template: "/templates/meta/identifierList", model: identifierService.prepareIDsForTable(owner)
    }

    /**
     * Toggles inheritance of the given alternative name, i.e. passes or retires an alternative name to or from member objects
     */
    @Secured(['ROLE_USER'])
    def toggleAlternativeNameAuditConfig() {
        def owner = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
        if(formService.validateToken(params)) {
            AlternativeName altName  = AlternativeName.get(params.id)
            subscriptionService.inheritAlternativeName(owner, altName)
        }
        redirect(url: request.getHeader('referer'))
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
            request.setAttribute("editable", params.editable == "true")
            boolean showConsortiaFunctions = Boolean.parseBoolean(params.showConsortiaFunctions)
            if(params.propDefGroup) {
                Org consortium
                boolean atSubscr
                List propDefGroupItems = []
                PropertyDefinitionGroup propDefGroup = genericOIDService.resolveOID(params.propDefGroup)
                PropertyDefinitionGroupBinding propDefGroupBinding = genericOIDService.resolveOID(params.propDefGroupBinding)
                boolean isGroupVisible = propDefGroup.isVisible || propDefGroupBinding?.isVisible
                if (property.owner instanceof License) {
                    consortium = property.owner.getLicensingConsortium()
                    atSubscr = property.owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
                } else if (property.owner instanceof Subscription) {
                    consortium = property.owner.getConsortium()
                    atSubscr = property.owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
                }
                if (isGroupVisible) {
                    propDefGroupItems = propDefGroup.getCurrentProperties(property.owner)
                } else if (consortium != null) {
                    propDefGroupItems = propDefGroup.getCurrentPropertiesOfTenant(property.owner, consortium)
                }
                Map<String, Object> modelMap = [
                        isGroupVisible: isGroupVisible,
                        atSubscr: atSubscr,
                        consortium: consortium,
                        propDefGroupItems: propDefGroupItems,
                        ownobj          : property.owner,
                        newProp         : property,
                        showConsortiaFunctions: showConsortiaFunctions,
                        propDefGroup    : propDefGroup,
                        propDefGroupBinding : propDefGroupBinding,
                        custom_props_div: "${params.custom_props_div}", // JS markup id
                        prop_desc       : property.type.descr // form data
                ]
                render(template: "/templates/properties/group", model: modelMap)
            }
            else {
                Map<String, Object>  allPropDefGroups = property.owner.getCalculatedPropDefGroups(contextService.getOrg())

                Map<String, Object> modelMap =  [
                        ownobj                : property.owner,
                        newProp               : property,
                        showConsortiaFunctions: showConsortiaFunctions,
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
        String className = params.propClass.split(" ")[1]
        def propClass = Class.forName(className)
        def owner     = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
        def property  = propClass.get(params.id)
        def prop_desc = property.getType().getDescr()

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
                        AbstractPropertyWithCalculatedLastUpdated additionalProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, property.type, contextService.getOrg())
                        additionalProp = property.copyInto(additionalProp)
                        additionalProp.instanceOf = property
                        additionalProp.isPublic = true
                        additionalProp.save()
                    }
                    else {
                        List<AbstractPropertyWithCalculatedLastUpdated> matchingProps = property.getClass().findAllByOwnerAndTypeAndTenant(member, property.type, contextService.getOrg())
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
                            AbstractPropertyWithCalculatedLastUpdated newProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, member, property.type, contextService.getOrg())
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
        boolean showConsortiaFunctions = Boolean.parseBoolean(params.showConsortiaFunctions)
        if (params.propDefGroup) {
            Org consortium
            boolean atSubscr
            List propDefGroupItems = []
            PropertyDefinitionGroup propDefGroup = genericOIDService.resolveOID(params.propDefGroup)
            PropertyDefinitionGroupBinding propDefGroupBinding = genericOIDService.resolveOID(params.propDefGroupBinding)
            boolean isGroupVisible = propDefGroup.isVisible || propDefGroupBinding?.isVisible
            if (owner instanceof License) {
                consortium = owner.getLicensingConsortium()
                atSubscr = owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
            } else if (owner instanceof Subscription) {
                consortium = owner.getConsortium()
                atSubscr = owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
            }
            if (isGroupVisible) {
                propDefGroupItems = propDefGroup.getCurrentProperties(owner)
            } else if (consortium != null) {
                propDefGroupItems = propDefGroup.getCurrentPropertiesOfTenant(owner, consortium)
            }
            Map<String, Object> modelMap = [
                    isGroupVisible: isGroupVisible,
                    atSubscr: atSubscr,
                    consortium: consortium,
                    propDefGroupItems: propDefGroupItems,
                    ownobj          : owner,
                    newProp         : property,
                    showConsortiaFunctions: showConsortiaFunctions,
                    propDefGroup    : propDefGroup,
                    propDefGroupBinding : propDefGroupBinding,
                    custom_props_div: "${params.custom_props_div}", // JS markup id
                    prop_desc       : prop_desc // form data
            ]

          render(template: "/templates/properties/group", model: modelMap)
        }
        else {
            Map<String, Object>  allPropDefGroups = owner.getCalculatedPropDefGroups(contextService.getOrg())

            Map<String, Object> modelMap =  [
                    ownobj                : owner,
                    newProp               : property,
                    showConsortiaFunctions: showConsortiaFunctions,
                    custom_props_div      : "${params.custom_props_div}", // JS markup id
                    prop_desc             : prop_desc, // form data
                    orphanedProperties    : allPropDefGroups.orphanedProperties
            ]
            render(template: "/templates/properties/custom", model: modelMap)
        }
    }

    /**
     * Removes the given custom property from the object
     */
    @Secured(['ROLE_USER'])
    def deleteCustomProperty() {
        Subscription.withTransaction { TransactionStatus ts ->
            String className = params.propClass.split(" ")[1]
            def propClass = Class.forName(className)
            def owner     = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
            def property  = propClass.get(params.id)
            def prop_desc = property.getType().getDescr()

            AuditConfig.removeAllConfigs(property)

            owner.propertySet.remove(property)

            //try {
            property.delete() //cf. ERMS-5889; execution of delete done only after template has been called - with wrong values!
            ts.flush()
            //} catch (Exception e) {
            //log.error(" TODO: fix property.delete() when instanceOf ")
            //}


            if(property.hasErrors()) {
                log.error(property.errors.toString())
            }
            else {
                log.debug("Deleted custom property: " + property.type.name)
            }
            request.setAttribute("editable", params.editable == "true")
            boolean showConsortiaFunctions = Boolean.parseBoolean(params.showConsortiaFunctions)
            if(params.propDefGroup) {
                Org consortium
                boolean atSubscr
                List propDefGroupItems = []
                PropertyDefinitionGroup propDefGroup = genericOIDService.resolveOID(params.propDefGroup)
                PropertyDefinitionGroupBinding propDefGroupBinding = genericOIDService.resolveOID(params.propDefGroupBinding)
                boolean isGroupVisible = propDefGroup.isVisible || propDefGroupBinding?.isVisible
                if (owner instanceof License) {
                    consortium = owner.getLicensingConsortium()
                    atSubscr = owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
                } else if (owner instanceof Subscription) {
                    consortium = owner.getConsortium()
                    atSubscr = owner._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_PARTICIPATION
                }
                if (isGroupVisible) {
                    propDefGroupItems = propDefGroup.getCurrentProperties(owner)
                } else if (consortium != null) {
                    propDefGroupItems = propDefGroup.getCurrentPropertiesOfTenant(owner, consortium)
                }
                Map<String, Object> modelMap = [
                        isGroupVisible: isGroupVisible,
                        atSubscr: atSubscr,
                        consortium: consortium,
                        propDefGroupItems: propDefGroupItems,
                        ownobj          : owner,
                        newProp         : property,
                        showConsortiaFunctions: showConsortiaFunctions,
                        propDefGroup    : propDefGroup,
                        propDefGroupBinding : propDefGroupBinding,
                        custom_props_div: "${params.custom_props_div}", // JS markup id
                        prop_desc       : prop_desc // form data
                ]
                render(template: "/templates/properties/group", model: modelMap)
            }
            else {
                Map<String, Object> allPropDefGroups = owner.getCalculatedPropDefGroups(contextService.getOrg())
                Map<String, Object> modelMap =  [
                        ownobj                : owner,
                        newProp               : property,
                        showConsortiaFunctions: showConsortiaFunctions,
                        custom_props_div      : "${params.custom_props_div}", // JS markup id
                        prop_desc             : prop_desc, // form data
                        orphanedProperties    : allPropDefGroups.orphanedProperties
                ]

                render(template: "/templates/properties/custom", model: modelMap)
            }
        }
    }

  /**
    * Deletes the given domain specific private property from the object
    */
  @Secured(['ROLE_USER'])
  @Transactional
  def deletePrivateProperty(){
    String className = params.propClass.split(" ")[1]
    def propClass = Class.forName(className)
    def property  = propClass.get(params.id)
    def tenant    = property.type.tenant
    def owner     = CodeUtils.getDomainClass( params.ownerClass )?.get(params.ownerId)
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
            propertyWrapper: "private-property-wrapper-${tenant.id}",  // JS markup id
            prop_desc: prop_desc // form data
    ])
  }

    /**
     * Shows or hides the given dashboard due date
     * @param isHidden is the dashboard due date hidden?
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def setDashboardDueDateVisibility(){
        boolean isHidden = ! params.boolean('visibility')
        log.debug('Hide/Show DashboardDueDate - isHidden=' + isHidden)

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        flash.error = ''

        if (! (result.user as User).isFormal(contextService.getOrg())) {
            flash.error = "You do not have permission to access. Please request access on the profile page"
            response.sendError(HttpStatus.SC_FORBIDDEN)
            return
        }

        if (params.id) {
            DashboardDueDate dueDate = DashboardDueDate.get(params.id)
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

        result.editable = contextService.isInstEditor()

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.dashboardDueDatesOffset = result.offset

        result.dueDates = dashboardDueDatesService.getDashboardDueDates(contextService.getUser(), result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.countDashboardDueDates(contextService.getUser())

        render (template: "/user/tableDueDates", model: [dueDates: result.dueDates, dueDatesCount: result.dueDatesCount, max: result.max, offset: result.offset])
    }

    /**
     * Marks the given due date as done or undone
     * @param isDone is the due date completed or not?
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def setDueDateObjectStatus(){
        boolean isDone = params.boolean('done')
        log.debug('Done/Undone DueDateObject - isDone=' + isDone)

        Map<String, Object> result = [:]
        result.user = contextService.getUser()
        result.institution = contextService.getOrg()
        flash.error = ''

        if (! (result.user as User).isFormal(contextService.getOrg())) {
            flash.error = "You do not have permission to access. Please request access on the profile page"
            response.sendError(HttpStatus.SC_FORBIDDEN)
            return
        }

        if (params.id) {
            DueDateObject dueDateObject = DueDateObject.get(params.id)
            if (dueDateObject){
                Object obj = genericOIDService.resolveOID(dueDateObject.oid)
//                Object obj = dueDateObject.getObject() // TODO - ERMS-5862
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

        result.editable = contextService.isInstEditor()

        SwissKnife.setPaginationParams(result, params, (User) result.user)
        result.dashboardDueDatesOffset = result.offset

        result.dueDates = dashboardDueDatesService.getDashboardDueDates(contextService.getUser(), result.max, result.dashboardDueDatesOffset)
        result.dueDatesCount = dashboardDueDatesService.countDashboardDueDates(contextService.getUser())

        render (template: "/user/tableDueDates", model: [dueDates: result.dueDates, dueDatesCount: result.dueDatesCount, max: result.max, offset: result.offset])
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
            if(Boolean.valueOf(params.auditNewIdentifier)) {
                Identifier parentIdentifier = fr.result as Identifier
                subscriptionService.inheritIdentifier(owner, parentIdentifier)
            }
            fr.setFlashScopeByStatus(flash)
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Removes the given identifier from the given owner object
     */
    @Secured(['ROLE_USER'])
    def deleteIdentifier() {
        Identifier target = genericOIDService.resolveOID(params.target)
        identifierService.deleteIdentifier(params.owner,params.target)
        flash.message = message(code: 'identifier.delete.success', args: [target.ns.ns, target.value])
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Revokes the given affiliation from the given user to the given institution.
     * Expected is a structure userId:orgId:roleId
     * @return redirects to the referer
     */
    @Transactional
    @Secured(['ROLE_ADMIN'])
    def unsetAffiliation() {
        String[] keys = params.key.split(':')
        if (keys.size() == 3) {
            User u = User.get(keys[0])
            Org fo = Org.get(keys[1])
            Role fr = Role.get(keys[2])

            if (u && fo && fr) {
                if (u.formalOrg?.id == fo.id && u.formalRole?.id == fr.id) {
                    u.formalOrg = null
                    u.formalRole = null
                    u.save()
                }
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * This is the call route for processing an xEditable change other than reference data or role
     * @return the new value for display update in the xEditable field
     * @see XEditableTagLib#xEditable
     * @see XEditableTagLib#xEditableAsIcon
     * @see XEditableTagLib#xEditableBoolean
     */
    @Secured(['ROLE_USER'])
    @Transactional
    def editableSetValue() {
        log.debug("editableSetValue ${params}")

        def result = null
        def target_object = genericOIDService.resolveOID(params.pk)

        try {
            if (target_object) {
                switch(params.type) {
                    case 'date':
                        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                        def backup = target_object."${params.name}"

                        try {
                            if (params.value && params.value.size() > 0) {
                                // parse new date
                                Date parsed_date = sdf.parse(params.value)
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
                                result = DateUtils.getLocalizedSDF_noTime().format( target_object."${params.name}" )
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
                    case 'year':
                        def backup = target_object."${params.name}"

                        try {
                            if (params.value && params.value.size() > 0) {
                                // parse new year
                                Year parsed_year = Year.parse(params.value)
                                target_object."${params.name}" = parsed_year
                            } else {
                                // delete existing year
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
                    default:
                        Map binding_properties = [:]

                        if (target_object."${params.name}" instanceof BigDecimal) {
                            params.value = escapeService.parseFinancialValue(params.value)
                        }
                        else if (target_object."${params.name}" instanceof Long) {
                            if (params.long('value').toString() != params.value) {
                                params.value = null // Hotfix: ERMS-6274 - Todo: ERMS-6285
                            }
                        }
                        else if (target_object."${params.name}" instanceof Integer) {
                            if (params.int('value').toString() != params.value) {
                                params.value = null // Hotfix: ERMS-6274 - Todo: ERMS-6285
                            }
                        }
                        else if (target_object."${params.name}" instanceof Boolean) {
                            params.value = params.value?.equals("1")
                        }
                        if (target_object instanceof AlternativeName) {
                            if(!params.value)
                                binding_properties[params.name] = 'Unknown'
                            else binding_properties[params.name] = params.value
                        }
                        if (params.value instanceof String) {
                            String value = params.value.startsWith('www.') ? ('http://' + params.value) : params.value
                            binding_properties[params.name] = value
                        } else {
                            binding_properties[params.name] = params.value
                        }

                        if(target_object instanceof Subscription && params.name == 'hasPerpetualAccess'){
                            boolean packageProcess = false
                            for(SubscriptionPackage sp: target_object.packages) {
                                packageProcess = subscriptionService.checkThreadRunning('permanentTitlesProcess_' + sp.pkg.id + '_' + contextService.getOrg().id)
                                if(packageProcess)
                                    break
                            }
                            if(!subscriptionService.checkThreadRunning('permanentTitlesProcess_'+target_object.id) && !packageProcess) {
                                if (params.value == true && target_object.hasPerpetualAccess != params.value) {
                                    subscriptionService.setPermanentTitlesBySubscription(target_object)
                                }
                                if (params.value == false && target_object.hasPerpetualAccess != params.value) {
                                    subscriptionService.removePermanentTitlesBySubscription(target_object)
                                }
                                bindData(target_object, binding_properties)

                                target_object.save(failOnError: true)

                                if (target_object."${params.name}" instanceof BigDecimal) {
                                    result = NumberFormat.getInstance( LocaleUtils.getCurrentLocale() ).format(target_object."${params.name}")
                                    //is for that German users do not cry about comma-dot-change
                                } else {
                                    result = target_object."${params.name}"
                                }
                            }else {
                                result = [status: 'error', msg: "${message(code: 'subscription.details.permanentTitlesProcessRunning.info')}"]
                                render result as JSON
                                return
                            }
                        }
                        else { // -- default

                            bindData(target_object, binding_properties)

                            target_object.save(failOnError: true)

                            if (target_object."${params.name}" instanceof BigDecimal) {
                                result = NumberFormat.getInstance( LocaleUtils.getCurrentLocale() ).format(target_object."${params.name}")
                                //is for that German users do not cry about comma-dot-change
                            } else {
                                result = target_object."${params.name}"
                            }
                        }

                        break
                }

                if (target_object instanceof SurveyResult) {

                    Org org = contextService.getOrg()
                    SurveyOrg surveyOrg = SurveyOrg.findBySurveyConfigAndOrg(target_object.surveyConfig, target_object.participant)

                    //If Survey Owner set Value then set FinishDate
                    if (org?.id == target_object.owner.id && (target_object.type == PropertyStore.SURVEY_PROPERTY_PARTICIPATION) && surveyOrg.finishDate == null) {
                        String property = target_object.type.getImplClassValueProperty()

                        if (target_object[property] != null) {
                            log.debug("Set/Save FinishDate of SurveyOrg (${surveyOrg.id})")
                            surveyOrg.finishDate = new Date()
                            surveyOrg.save()
                        }
                    }
                }

            }

        } catch (NumberFormatException e) {
            log.error("NumberFormatException @ editableSetValue()")
            log.error(e.toString())
            result = target_object ? target_object[params.name] : null
        } catch (Exception e) {
            log.error("@ editableSetValue()")
            log.error(e.toString())
        }

        log.debug("editableSetValue() returns ${result}")

        response.setContentType('text/plain')

        ServletOutputStream outs = response.outputStream
        outs << result
        outs.flush()
        outs.close()
    }

    @Secured(['ROLE_ADMIN'])
    def addUserRole() {
        User user = User.get(params.long('user'))
        Role role = Role.get(params.long('role'))
        if (user && role) {
            UserRole ur = UserRole.create(user, role)

            if (ur.hasErrors()) {
                flash.error = "${message(code: 'default.save.error.general.message')}"
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Revokes the given role from the given user
     */
    @Secured(['ROLE_ADMIN'])
    def removeUserRole() {
        User user = User.get(params.long('user'))
        Role role = Role.get(params.long('role'))
        if (user && role) {
            UserRole.remove(user, role)
        }
        redirect(url: request.getHeader('referer'))
    }

  @Deprecated
  private def _renderObjectValue(value) {
    String result = ''
    String not_set = message(code:'refdata.notSet') as String

    if ( value ) {
      switch ( value.class ) {
        case RefdataValue.class:
            result = value.value ? value.getI10n('value') : not_set
          break
        default:
          if(value instanceof String){

          }else{
            value = value.toString()
          }
          String no_ws = value.replaceAll(' ','')

          result = message(code:"refdata.${no_ws}", default:"${value ?: not_set}")
      }
    }
    // log.debug("Result of render: ${value} : ${result}");
    result
  }

    /**
     * Method under development; concept of cost per use is not fully elaborated yet
     * Generates for the given subsciption and its holding a cost per use calculation, i.e. a cost analysis for the regarded COUNTER report
     * @return a table view of the cost analysis, depending on the given report(s)
     */
    @Secured(['ROLE_USER'])
    def generateCostPerUse() {
        Map<String, Object> ctrlResult = subscriptionControllerService.getStatsDataForCostPerUse(params)
        if(ctrlResult.status == SubscriptionControllerService.STATUS_OK) {
            if(ctrlResult.result.containsKey('alternatePeriodStart') && ctrlResult.result.containsKey('alternatePeriodEnd')) {
                SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
                ctrlResult.result.selectedPeriodNotCovered = message(code: 'default.stats.error.selectedPeriodNotCovered', args: [sdf.format(ctrlResult.result.alternatePeriodStart), sdf.format(ctrlResult.result.alternatePeriodEnd)] as Object[])
            }
            ctrlResult.result.costPerUse = [:]
            if(ctrlResult.result.subscription._getCalculatedType() == CalculatedType.TYPE_PARTICIPATION) {
                Map<String, Object> costPerUseConsortial = subscriptionControllerService.calculateCostPerUse(ctrlResult.result, "consortial")
                ctrlResult.result.costPerUse.consortialData = costPerUseConsortial.costPerMetric
                ctrlResult.result.consortialCosts = costPerUseConsortial.costsAllYears
                if (ctrlResult.result.contextOrg.isCustomerType_Inst_Pro()) {
                    Map<String, Object> costPerUseOwn = subscriptionControllerService.calculateCostPerUse(ctrlResult.result, "own")
                    ctrlResult.result.costPerUse.ownData = costPerUseOwn.costPerMetric
                    ctrlResult.result.ownCosts = costPerUseOwn.costsAllYears
                }
            }
            else {
                Map<String, Object> costPerUseOwn = subscriptionControllerService.calculateCostPerUse(ctrlResult.result, "own")
                ctrlResult.result.costPerUse.ownData = costPerUseOwn.costPerMetric
                ctrlResult.result.ownCosts = costPerUseOwn.costsAllYears
            }
            render template: "/templates/stats/costPerUse", model: ctrlResult.result
        }
        else [error: ctrlResult.error]
    }

    @Secured(['ROLE_USER'])
    @Transactional
    def editPreferredConcatsForSurvey() {
        Address  addressInstance = params.addressId ? Address.get(params.addressId) : null
        Person personInstance = params.personId ? Person.get(params.personId) : null

        if (addressInstance && accessService.hasAccessToAddress(addressInstance as Address, AccessService.WRITE)) {
            if (params.setPreferredAddress == 'false') {
                addressInstance.preferredForSurvey = false
                addressInstance.save()
            }
            if (params.setPreferredAddress == 'true') {
                if(Address.findAllByOrgAndTenantIsNullAndPreferredForSurvey(contextService.getOrg(), true))
                {
                    flash.error = message(code: 'address.preferredForSurvey.fail')
                }else {
                    addressInstance.preferredForSurvey = true
                    addressInstance.save()
                }
            }

        }

        if (personInstance && accessService.hasAccessToPerson(personInstance as Person, AccessService.WRITE)) {

            if(params.setPreferredBillingPerson) {
                if (params.setPreferredBillingPerson == 'false') {
                    personInstance.preferredBillingPerson = false
                    personInstance.save()
                }
                if (params.setPreferredBillingPerson == 'true') {
                    if(Person.findAllByTenantAndPreferredBillingPerson(contextService.getOrg(), true))
                    {
                        flash.error = message(code: 'person.preferredBillingPerson.fail')
                    }else {
                        personInstance.preferredBillingPerson = true
                        personInstance.save()
                    }
                }
            }

            if(params.setPreferredSurveyPerson) {
                if (params.setPreferredSurveyPerson == 'false') {
                    personInstance.preferredSurveyPerson = false
                }
                if (params.setPreferredSurveyPerson == 'true') {
                    personInstance.preferredSurveyPerson = true
                }
                personInstance.save()
            }
        }

        redirect(controller: 'organisation', action: 'contacts', id: params.id, params: [tab: addressInstance ? 'addresses' : 'contacts'])
    }
}
