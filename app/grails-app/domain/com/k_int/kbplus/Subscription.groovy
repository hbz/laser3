package com.k_int.kbplus


import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.*
import de.laser.traits.AuditableTrait
import de.laser.traits.ShareableTrait
import grails.util.Holders
import org.apache.lucene.index.DocIDMerger
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.dao.TransientDataAccessResourceException

import javax.persistence.Transient
import java.text.SimpleDateFormat

class Subscription
        extends AbstractBaseDomain
        implements TemplateSupport, Permissions, ShareSupport,
                AuditableTrait {

    // AuditableTrait
    static auditable            = [ ignore: ['version', 'lastUpdated', 'pendingChanges'] ]
    static controlledProperties = [ 'name', 'startDate', 'endDate', 'manualCancellationDate', 'status', 'type', 'form', 'resource' ]

    @Transient
    def grailsApplication
    @Transient
    def contextService
    @Transient
    def messageSource
    @Transient
    def pendingChangeService
    @Transient
    def changeNotificationService
    @Transient
    def springSecurityService
    @Transient
    def accessService
    @Transient
    def propertyService

    @RefdataAnnotation(cat = 'Subscription Status')
    RefdataValue status

    @RefdataAnnotation(cat = 'Subscription Type')
    RefdataValue type

    @RefdataAnnotation(cat = 'Subscription Form')
    RefdataValue form

    @RefdataAnnotation(cat = 'Subscription Resource')
    RefdataValue resource

    // If a subscription is slaved then any changes to instanceOf will automatically be applied to this subscription
    boolean isSlaved
	boolean isPublic

    boolean isMultiYear

  String name
  String identifier
  String impId
  Date startDate
  Date endDate
  Date manualRenewalDate
  Date manualCancellationDate
  String cancellationAllowances

  Subscription instanceOf
  Subscription previousSubscription //deleted as ERMS-800
  // If a subscription is administrative, subscription members will not see it resp. there is a toggle which en-/disables visibility
  boolean administrative

  String noticePeriod
  Date dateCreated
  Date lastUpdated

  License owner
  SortedSet issueEntitlements

  static transients = [ 'subscriber', 'providers', 'agencies', 'consortia' ]

  static hasMany = [
                     ids: IdentifierOccurrence,
                     packages : SubscriptionPackage,
                     issueEntitlements: IssueEntitlement,
                     documents: DocContext,
                     orgRelations: OrgRole,
                     prsLinks: PersonRole,
                     derivedSubscriptions: Subscription,
                     pendingChanges: PendingChange,
                     customProperties: SubscriptionCustomProperty,
                     privateProperties: SubscriptionPrivateProperty,
                     costItems: CostItem,
                     oapl: OrgAccessPointLink
  ]

  static mappedBy = [
                      ids: 'sub',
                      packages: 'subscription',
                      issueEntitlements: 'subscription',
                      documents: 'subscription',
                      orgRelations: 'sub',
                      prsLinks: 'sub',
                      derivedSubscriptions: 'instanceOf',
                      pendingChanges: 'subscription',
                      costItems: 'sub',
                      customProperties: 'owner',
                      privateProperties: 'owner',
                      oapl: 'subscription'
                      ]

    static mapping = {
        sort name: 'asc'
        id          column:'sub_id'
        version     column:'sub_version'
        globalUID   column:'sub_guid'
        status      column:'sub_status_rv_fk'
        type        column:'sub_type_rv_fk',        index: 'sub_type_idx'
        owner       column:'sub_owner_license_fk',  index: 'sub_owner_idx'
        form        column:'sub_form_fk'
        resource    column:'sub_resource_fk'
        name        column:'sub_name'
        identifier  column:'sub_identifier'
        impId       column:'sub_imp_id', index:'sub_imp_id_idx'
        startDate   column:'sub_start_date',        index: 'sub_dates_idx'
        endDate     column:'sub_end_date',          index: 'sub_dates_idx'
        manualRenewalDate       column:'sub_manual_renewal_date'
        manualCancellationDate  column:'sub_manual_cancellation_date'
        instanceOf              column:'sub_parent_sub_fk', index:'sub_parent_idx'
        administrative          column:'sub_is_administrative'
        previousSubscription    column:'sub_previous_subscription_fk' //-> see Links, deleted as ERMS-800
        isSlaved        column:'sub_is_slaved'
        isPublic        column:'sub_is_public'
        noticePeriod    column:'sub_notice_period'
        isMultiYear column: 'sub_is_multi_year'
        pendingChanges  sort: 'ts', order: 'asc', batchSize: 10

        ids                 batchSize: 10
        packages            batchSize: 10
        issueEntitlements   batchSize: 10
        documents           batchSize: 10
        orgRelations        batchSize: 10
        prsLinks            batchSize: 10
        derivedSubscriptions    batchSize: 10
        customProperties    batchSize: 10
        privateProperties   batchSize: 10
        costItems           batchSize: 10
        oapl                batchSize: 10
    }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        status(nullable:false, blank:false)
        type(nullable:true, blank:false)
        owner(nullable:true, blank:false)
        form        (nullable:true, blank:false)
        resource    (nullable:true, blank:false)
        impId(nullable:true, blank:false)
        startDate(nullable:true, blank:false, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['startDateAfterEndDate']
            }
        })
        endDate(nullable:true, blank:false, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['endDateBeforeStartDate']
            }
        })
        manualRenewalDate(nullable:true, blank:false)
        manualCancellationDate(nullable:true, blank:false)
        instanceOf(nullable:true, blank:false)
        administrative(nullable:false, blank:false, default: false)
        previousSubscription(nullable:true, blank:false) //-> see Links, deleted as ERMS-800
        isSlaved    (nullable:false, blank:false)
        noticePeriod(nullable:true, blank:true)
        isPublic    (nullable:false, blank:false)
        cancellationAllowances(nullable:true, blank:true)
        lastUpdated(nullable: true, blank: true)
        isMultiYear(nullable: true, blank: false, default: false)
    }

    // TODO: implement
    @Override
    boolean isTemplate() {
        return false
    }

    // TODO: implement
    @Override
    boolean hasTemplate() {
        return false
    }

    @Override
    boolean checkSharePreconditions(ShareableTrait sharedObject) {
        // needed to differentiate OrgRoles
        if (sharedObject instanceof OrgRole) {
            if (showUIShareButton() && sharedObject.roleType.value in ['Provider', 'Agency']) {
                return true
            }
        }
        false
    }

    @Override
    boolean showUIShareButton() {
        getCalculatedType() in [TemplateSupport.CALCULATED_TYPE_CONSORTIAL,TemplateSupport.CALCULATED_TYPE_COLLECTIVE]
    }

    @Override
    void updateShare(ShareableTrait sharedObject) {
        log.debug('updateShare: ' + sharedObject)

        if (sharedObject instanceof DocContext) {
            if (sharedObject.isShared) {
                List<Subscription> newTargets = Subscription.findAllByInstanceOfAndStatusNotEqual(this, RDStore.SUBSCRIPTION_DELETED)
                log.debug('found targets: ' + newTargets)

                newTargets.each{ sub ->
                    log.debug('adding for: ' + sub)
                    sharedObject.addShareForTarget_trait(sub)
                }
            }
            else {
                sharedObject.deleteShare_trait()
            }
        }
        if (sharedObject instanceof OrgRole) {
            if (sharedObject.isShared) {
                List<Subscription> newTargets = Subscription.findAllByInstanceOfAndStatusNotEqual(this, RDStore.SUBSCRIPTION_DELETED)
                log.debug('found targets: ' + newTargets)

                newTargets.each{ sub ->
                    log.debug('adding for: ' + sub)

                    // ERMS-1185
                    if (sharedObject.roleType in [RDStore.OR_AGENCY, RDStore.OR_PROVIDER]) {
                        List<OrgRole> existingOrgRoles = OrgRole.findAll{
                            sub == sub && roleType == sharedObject.roleType && org == sharedObject.org
                        }
                        if (existingOrgRoles) {
                            log.debug('found existing orgRoles, deleting: ' + existingOrgRoles)
                            existingOrgRoles.each{ tmp -> tmp.delete() }
                        }
                    }
                    sharedObject.addShareForTarget_trait(sub)
                }
            }
            else {
                sharedObject.deleteShare_trait()
            }
        }
    }

    @Override
    void syncAllShares(List<ShareSupport> targets) {
        log.debug('synAllShares: ' + targets)

        documents.each{ sharedObject ->
            targets.each{ sub ->
                if (sharedObject.isShared) {
                    log.debug('adding for: ' + sub)
                    sharedObject.addShareForTarget_trait(sub)
                }
                else {
                    log.debug('deleting all shares')
                    sharedObject.deleteShare_trait()
                }
            }
        }

        orgRelations.each{ sharedObject ->
            targets.each{ sub ->
                if (sharedObject.isShared) {
                    log.debug('adding for: ' + sub)
                    sharedObject.addShareForTarget_trait(sub)
                }
                else {
                    log.debug('deleting all shares')
                    sharedObject.deleteShare_trait()
                }
            }
        }
    }

    @Override
    String getCalculatedType() {
        def result = CALCULATED_TYPE_UNKOWN

        if (isTemplate()) {
            result = CALCULATED_TYPE_TEMPLATE
        }
        else if(administrative) {
            result = CALCULATED_TYPE_ADMINISTRATIVE
        }
        else if(getCollective() && getConsortia() && instanceOf) {
            result = CALCULATED_TYPE_PARTICIPATION_AS_COLLECTIVE
        }
        else if(getCollective() && !getAllSubscribers() && !instanceOf) {
            result = CALCULATED_TYPE_COLLECTIVE
        }
        else if(getConsortia() && !getAllSubscribers() && !instanceOf) {
            result = CALCULATED_TYPE_CONSORTIAL
        }
        else if((getCollective() || getConsortia()) && instanceOf) {
            result = CALCULATED_TYPE_PARTICIPATION
        }
        // TODO remove type_local
        else if(getAllSubscribers() && !instanceOf) {
            result = CALCULATED_TYPE_LOCAL
        }
        result
    }

    /*
    @Override
    String getCalculatedType() {
        def result = CALCULATED_TYPE_UNKOWN

        if (isTemplate()) {
            result = CALCULATED_TYPE_TEMPLATE
        }
        else if(getCollective() && ! getAllSubscribers() && !instanceOf) {
            result = CALCULATED_TYPE_COLLECTIVE
        }
        else if(getCollective() && instanceOf) {
            result = CALCULATED_TYPE_PARTICIPATION
        }
        else if(getConsortia() && ! getAllSubscribers() && ! instanceOf) {
            if(administrative)
                result = CALCULATED_TYPE_ADMINISTRATIVE
            else
                result = CALCULATED_TYPE_CONSORTIAL
        }
        else if(getConsortia() && instanceOf) {
            if(administrative)
                result = CALCULATED_TYPE_ADMINISTRATIVE
            else
                result = CALCULATED_TYPE_PARTICIPATION
        }
        else if(! getConsortia() && getAllSubscribers() && ! instanceOf) {
            result = CALCULATED_TYPE_LOCAL
        }
        result
    }
    */

    List<Org> getProviders() {
        Org.executeQuery("select og.org from OrgRole og where og.sub =:sub and og.roleType = :provider",
            [sub: this, provider: RDStore.OR_PROVIDER])
    }

    List<Org> getAgencies() {
        Org.executeQuery("select og.org from OrgRole og where og.sub =:sub and og.roleType = :agency",
                [sub: this, agency: RDStore.OR_AGENCY])
    }

    // used for views and dropdowns
    def getNameConcatenated() {
        def cons = getConsortia()
        def subscr = getAllSubscribers()
        if (subscr) {
            "${name} (" + subscr.join(', ') + ")"
        }
        else if (cons){
            "${name} (${cons})"
        }
        else {
            name
        }
    }

  def getIsSlavedAsString() {
    isSlaved ? "Yes" : "No"
  }

  Org getSubscriber() {
    Org result = null
    Org cons = null
    Org coll = null
    
    orgRelations.each { or ->
      if ( or?.roleType?.id in [RDStore.OR_SUBSCRIBER.id, RDStore.OR_SUBSCRIBER_CONS.id, RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id, RDStore.OR_SUBSCRIBER_COLLECTIVE.id] )
        result = or.org
        
      if ( or?.roleType?.id == RDStore.OR_SUBSCRIPTION_CONSORTIA.id )
        cons = or.org
      else if(or?.roleType?.id == RDStore.OR_SUBSCRIPTION_COLLECTIVE.id)
        coll = or.org
    }
    
    if ( !result && cons ) {
      result = cons
    }
    else if(!result && coll) {
        result = coll
    }
    
    result
  }

  def getAllSubscribers() {
    def result = [];
    orgRelations.each { or ->
      if ( or?.roleType?.value in ['Subscriber', 'Subscriber_Consortial', 'Subscriber_Consortial_Hidden', 'Subscriber_Collective'] )
        result.add(or.org)
    }
    result
  }
  
  def getProvider() {
    def result = null;
    orgRelations.each { or ->
      if ( or?.roleType?.value=='Content Provider' )
        result = or.org;
    }
    result
  }

    def getConsortia() {
        def result = null;
        orgRelations.each { or ->
            if ( or?.roleType?.value=='Subscription Consortia' )
                result = or.org
        }
        result
    }

    Org getCollective() {
        Org result = null
        result = orgRelations.find { OrgRole or ->
            or.roleType.id == RDStore.OR_SUBSCRIPTION_COLLECTIVE.id
        }?.org
        result
    }

    // TODO: rename
    def getDerivedSubscribers() {
        def result = []

        Subscription.findAllByInstanceOf(this).each { s ->
            def ors = OrgRole.findAllWhere( sub: s )
            ors.each { or ->
                if (or.roleType?.value in ['Subscriber', 'Subscriber_Consortial']) {
                    result << or.org
                }
            }
        }
        result = result.sort {it.name}
    }

    Subscription getCalculatedPrevious() {
        Links match = Links.findWhere(
                source: this.id,
                objectType: Subscription.class.name,
                linkType: RDStore.LINKTYPE_FOLLOWS
        )
        return match ? Subscription.get(match?.destination) : null
    }

    Subscription getCalculatedSuccessor() {
        Links match = Links.findWhere(
                destination: this.id,
                objectType: Subscription.class.name,
                linkType: RDStore.LINKTYPE_FOLLOWS
        )
        return match ? Subscription.get(match?.source) : null
    }

    boolean isMultiYearSubscription()
    {
        if(this.startDate && this.endDate && (this.endDate.minus(this.startDate) > 366))
        {
            return true
        }else {
            return false
        }
    }

    boolean isCurrentMultiYearSubscription()
    {
        def currentDate = new Date(System.currentTimeMillis())
        //println(this.endDate.minus(currentDate))
        if(this.isMultiYearSubscription() && this.endDate && (this.endDate.minus(currentDate) > 366))
        {
            return true
        }else {
            return false
        }
    }

    boolean isEditableBy(user) {
        hasPerm('edit', user)
    }

    boolean isVisibleBy(user) {
        hasPerm('view', user)
    }

    boolean hasPerm(perm, user) {
        if (perm == 'view' && this.isPublic) {
            return true
        }
        def adm = Role.findByAuthority('ROLE_ADMIN')
        def yda = Role.findByAuthority('ROLE_YODA')

        if (user.getAuthorities().contains(adm) || user.getAuthorities().contains(yda)) {
            return true
        }

        if (user.getAuthorizedOrgsIds().contains(contextService.getOrg()?.id)) {

            OrgRole cons = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIPTION_CONSORTIA
            )
            OrgRole coll = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIPTION_COLLECTIVE
            )
            OrgRole subscrCons = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIBER_CONS
            )
            OrgRole subscrColl = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIBER_COLLECTIVE
            )
            OrgRole subscr = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIBER
            )

            if (perm == 'view') {
                return cons || subscrCons || coll || subscrColl || subscr
            }
            if (perm == 'edit') {
                if(accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN'))
                    return cons || coll || subscr
            }
        }

        return false
    }

  @Override
  def beforeInsert() {
    if (impId == null) {
      impId = java.util.UUID.randomUUID().toString();
    }
    super.beforeInsert()
  }

    @Transient
    def notifyDependencies_trait(changeDocument) {
        log.debug("notifyDependencies_trait(${changeDocument})")

        def slavedPendingChanges = []
        def derived_subscriptions = getNonDeletedDerivedSubscriptions()

        derived_subscriptions.each { ds ->

            log.debug("Send pending change to ${ds.id}")

            def locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
            def description = messageSource.getMessage('default.accept.placeholder',null, locale)

            def definedType = 'text'
            if (this."${changeDocument.prop}" instanceof RefdataValue) {
                definedType = 'rdv'
            }
            else if (this."${changeDocument.prop}" instanceof Date) {
                definedType = 'date'
            }

            def msgParams = [
                    definedType,
                    "${changeDocument.prop}",
                    "${changeDocument.old}",
                    "${changeDocument.new}",
                    "${description}"
            ]

            def newPendingChange = changeNotificationService.registerPendingChange(
                    PendingChange.PROP_SUBSCRIPTION,
                    ds,
                    ds.getSubscriber(),
                    [
                            changeTarget:"com.k_int.kbplus.Subscription:${ds.id}",
                            changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                            changeDoc:changeDocument
                    ],
                    PendingChange.MSG_SU01,
                    msgParams,
                    "<b>${changeDocument.prop}</b> hat sich von <b>\"${changeDocument.oldLabel?:changeDocument.old}\"</b> zu <b>\"${changeDocument.newLabel?:changeDocument.new}\"</b> von der Lizenzvorlage geändert. " + description
            )

            if (newPendingChange && ds.isSlaved) {
                slavedPendingChanges << newPendingChange
            }
        }

        slavedPendingChanges.each { spc ->
            log.debug('autoAccept! performing: ' + spc)
            def user = null
            pendingChangeService.performAccept(spc.getId(), user)
        }
    }

    def getNonDeletedDerivedSubscriptions() {
        Subscription.where { instanceOf == this }
    }

    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        def result = [ 'global':[], 'local':[], 'member':[], 'orphanedProperties':[]]

        // ALL type depending groups without checking tenants or bindings
        def groups = PropertyDefinitionGroup.findAllByOwnerType(Subscription.class.name)
        groups.each{ it ->

            // cons_members
            if (this.instanceOf && ! this.instanceOf.isTemplate()) {
                def binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndSub(it, this.instanceOf)

                // global groups
                if (it.tenant == null) {
                    if (binding) {
                        result.member << [it, binding]
                    } else {
                        result.global << it
                    }
                }
                // consortium @ member; getting group by tenant and instanceOf.binding
                if (it.tenant?.id == contextOrg?.id) {
                    if (binding) {
                        result.member << [it, binding]
                    }
                }
                // subscriber consortial; getting group by consortia and instanceOf.binding
                else if (it.tenant?.id == this.instanceOf.getConsortia()?.id) {
                    if (binding) {
                        result.member << [it, binding]
                    }
                }
            }
            // consortium or locals
            else {
                def binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndSub(it, this)

                if (it.tenant == null || it.tenant?.id == contextOrg?.id) {
                    if (binding) {
                        result.local << [it, binding]
                    } else {
                        result.global << it
                    }
                }
            }
        }

        // storing properties without groups
        result.orphanedProperties = propertyService.getOrphanedProperties(this, result.global, result.local, result.member)

        result
    }

  public String toString() {
      name ? "${name}" : "Subscription ${id}"
  }

  // JSON definition of the subscription object
  // see http://manbuildswebsite.com/2010/02/15/rendering-json-in-grails-part-3-customise-your-json-with-object-marshallers/
  // Also http://jwicz.wordpress.com/2011/07/11/grails-custom-xml-marshaller/
  // Also http://lucy-michael.klenk.ch/index.php/informatik/grails/c/
  static {
    grails.converters.JSON.registerObjectMarshaller(User) {
      // you can filter here the key-value pairs to output:
      return it.properties.findAll {k,v -> k != 'passwd'}
    }
  }

  // XML.registerObjectMarshaller Facility, { facility, xml ->
  //    xml.attribute 'id', facility.id
  //               xml.build {
  //      name(facility.name)
  //    }
  //  }

  public Date getRenewalDate() {
    manualRenewalDate ? manualRenewalDate : null
  }
  /**
  * OPTIONS: startDate, endDate, hideIdent, inclSubStartDate, hideDeleted, accessibleToUser,inst_shortcode
  **/
  @Transient
  static def refdataFind(params) {
    def result = [];

    def hqlString = "select sub from Subscription sub where lower(sub.name) like :name "
    def hqlParams = [name: ((params.q ? params.q.toLowerCase() : '' ) + "%")]
    def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")
    def cons_role = RDStore.OR_SUBSCRIPTION_CONSORTIA
    def subscr_role = RDStore.OR_SUBSCRIBER
    def subscr_cons_role = RDStore.OR_SUBSCRIBER_CONS
    def viableRoles = [cons_role, subscr_role, subscr_cons_role]
    
    hqlParams.put('viableRoles', viableRoles)

    if(params.hasDate ){

      def startDate = params.startDate.length() > 1 ? sdf.parse(params.startDate) : null
      def endDate = params.endDate.length() > 1 ? sdf.parse(params.endDate)  : null

      if(startDate){
          hqlString += " AND sub.startDate >= :startDate "
          hqlParams.put('startDate', startDate)
      }
      if(endDate){
        hqlString += " AND sub.endDate <= :endDate "
        hqlParams.put('endDate', endDate)
        }
    }

    if(params.inst_shortcode && params.inst_shortcode.length() > 1){
      hqlString += " AND exists ( select orgs from sub.orgRelations orgs where orgs.org.shortcode = :inst AND orgs.roleType IN (:viableRoles) ) "
      hqlParams.put('inst', params.inst_shortcode)
    }


    def results = Subscription.executeQuery(hqlString,hqlParams)

    if(params.accessibleToUser){
      for(int i=0;i<results.size();i++){
        if(! results.get(i).checkPermissionsNew("view",User.get(params.accessibleToUser))){
          results.remove(i)
        }
      }
    }

    results?.each { t ->
      def resultText = t.name
      def date = t.startDate ? " (${sdf.format(t.startDate)})" : ""
      resultText = params.inclSubStartDate == "true"? resultText + date : resultText
      resultText = params.hideIdent == "true"? resultText : resultText + " (${t.identifier})"
      result.add([id:"${t.class.name}:${t.id}",text:resultText])
    }

    result
  }

  def setInstitution(inst) {
      log.debug("Set institution ${inst}")

    def subrole = RDStore.OR_SUBSCRIBER
    def or = new OrgRole(org:inst, roleType:subrole, sub:this)
    if ( this.orgRelations == null)
      this.orgRelations = []
    this.orgRelations.add(or)
  }

  def addNamespacedIdentifier(ns,value) {
      log.debug("Add Namespaced identifier ${ns}:${value}")

    def canonical_id = Identifier.lookupOrCreateCanonicalIdentifier(ns, value);
    if ( this.ids == null)
      this.ids = []
    this.ids.add(new IdentifierOccurrence(sub:this,identifier:canonical_id))

  }

  def getCommaSeperatedPackagesIsilList() {
      def result = []
      packages.each { it ->
          def identifierValue = it.pkg.getIdentifierByType('isil')?.value ?: null
          if (identifierValue) {
              result += identifierValue
          }
      }
      result.join(',')
  }

  def hasOrgWithUsageSupplierId() {
      def hasUsageSupplier = false
      packages.each { it ->
          def hql = "select count(orole.id) from OrgRole as orole "+
                  "join orole.pkg as pa "+
                  "join orole.roleType as roletype "+
                  "where pa.id = :package_id and roletype.value='Content Provider' "+
                  "and exists (select oid from orole.org.ids as oid where oid.identifier.ns.ns='statssid')"
          def queryResult = OrgRole.executeQuery(hql, ['package_id':it.pkg.id])
          if (queryResult[0] > 0){
              hasUsageSupplier = true
          }
      }
      return hasUsageSupplier
  }

  def getHoldingTypes() {
      def types = issueEntitlements?.tipp?.title?.type?.unique()
      types
  }

  def dropdownNamingConvention() {
      return dropdownNamingConvention(contextService.org)
  }

  def dropdownNamingConvention(contextOrg){
       def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
       SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))
       String period = startDate ? sdf.format(startDate)  : ''

       period = endDate ? period + ' - ' + sdf.format(endDate)  : ''

       period = period ? '('+period+')' : ''

       String statusString = status ? status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

       if(instanceOf) {
           def additionalInfo
           Map<Long,Org> orgRelationsMap = [:]
           orgRelations.each { or ->
               orgRelationsMap.put(or.roleType.id,or.org)
           }
           if(orgRelationsMap.get(RDStore.OR_SUBSCRIPTION_CONSORTIA.id)?.id == contextOrg.id) {
               if(orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS.id))
                   additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS.id)?.sortname
               else if(orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id))
                   additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id)?.sortname
           }
           else if(orgRelationsMap.get(RDStore.OR_SUBSCRIPTION_COLLECTIVE.id)?.id == contextOrg.id) {
               additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_COLLECTIVE.id)?.sortname
           }
           else{
               additionalInfo = messageSource.getMessage('gasco.filter.consortialLicence',null, LocaleContextHolder.getLocale())
           }


           return name + ' - ' + statusString + ' ' +period + ' - ' + additionalInfo

       } else {

           return name + ' - ' + statusString + ' ' +period
       }
  }
  static dropdownNamingConvention(long subId, SimpleDateFormat sdf){
       def list = Subscription.executeQuery('select name from Subscription where id = :subId', [subId: subId])
      def sub = list[0]
      def name = sub[1]
//      def instanceOf = sub[2]
      print name
//      def startDate = sub[3]
//      def endDate = sub[4]
//      def status = sub[5]
//       String period = startDate ? sdf.format(startDate)  : ''
//
//       period = endDate ? period + ' - ' + sdf.format(endDate)  : ''
//
//       period = period ? '('+period+')' : ''
//
//       String statusString = status ? status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')

//       if(instanceOf) {
//           def additionalInfo
//           Map<Long,Org> orgRelationsMap = [:]
//           orgRelations.each { or ->
//               orgRelationsMap.put(or.roleType.id,or.org)
//           }
//           if(orgRelationsMap.get(RDStore.OR_SUBSCRIPTION_CONSORTIA.id)?.id == contextOrg.id) {
//               if(orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS.id))
//                   additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS.id)?.sortname
//               else if(orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id))
//                   additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_CONS_HIDDEN.id)?.sortname
//           }
//           else if(orgRelationsMap.get(RDStore.OR_SUBSCRIPTION_COLLECTIVE.id)?.id == contextOrg.id) {
//               additionalInfo =  orgRelationsMap.get(RDStore.OR_SUBSCRIBER_COLLECTIVE.id)?.sortname
//           }
//           else{
//               additionalInfo = messageSource.getMessage('gasco.filter.consortialLicence',null, LocaleContextHolder.getLocale())
//           }
//
//
//           return name + ' - ' + statusString + ' ' +period + ' - ' + additionalInfo
//
//       } else {

//           return name + ' - ' + statusString + ' ' +period
//       }
      return '...'
  }

    def dropdownNamingConventionWithoutOrg() {
        return dropdownNamingConventionWithoutOrg(contextService.org)
    }

    def dropdownNamingConventionWithoutOrg(contextOrg){
        def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))
        String period = startDate ? sdf.format(startDate)  : ''

        period = endDate ? period + ' - ' + sdf.format(endDate)  : ''

        period = period ? '('+period+')' : ''

        String statusString = status ? status.getI10n('value') : RDStore.SUBSCRIPTION_NO_STATUS.getI10n('value')


        return name + ' - ' + statusString + ' ' +period

    }

    def getDerivedSubscriptionBySubscribers(Org org) {
        def result

        Subscription.findAllByInstanceOf(this).each { s ->
            def ors = OrgRole.findAllWhere( sub: s )
            ors.each { or ->
                if (or.roleType?.value in ['Subscriber', 'Subscriber_Consortial'] && or.org.id == org.id) {
                    result = or.sub
                }
            }
        }
        result
    }

}
