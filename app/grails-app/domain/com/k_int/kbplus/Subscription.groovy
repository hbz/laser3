package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.DeleteFlag
import de.laser.domain.AbstractBaseDomain
import de.laser.interfaces.Permissions
import de.laser.interfaces.ShareSupport
import de.laser.interfaces.TemplateSupport
import de.laser.traits.AuditableTrait
import de.laser.traits.ShareableTrait
import grails.util.Holders
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.SimpleDateFormat

class Subscription
        extends AbstractBaseDomain
        implements TemplateSupport, DeleteFlag, Permissions, ShareSupport,
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

    @RefdataAnnotation(cat = 'Subscription Status')
    RefdataValue status

    @RefdataAnnotation(cat = 'Subscription Type')
    RefdataValue type

    @RefdataAnnotation(cat = 'Subscription Form')
    RefdataValue form

    @RefdataAnnotation(cat = 'Subscription Resource')
    RefdataValue resource

    @RefdataAnnotation(cat = 'YN')
    RefdataValue isPublic

    // If a subscription is slaved then any changes to instanceOf will automatically be applied to this subscription
    @RefdataAnnotation(cat = 'YN')
    RefdataValue isSlaved

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
        type        column:'sub_type_rv_fk'
        owner       column:'sub_owner_license_fk'
        form        column:'sub_form_fk'
        resource    column:'sub_resource_fk'
        name        column:'sub_name'
        identifier  column:'sub_identifier'
        impId       column:'sub_imp_id', index:'sub_imp_id_idx'
        startDate   column:'sub_start_date'
        endDate     column:'sub_end_date'
        manualRenewalDate       column:'sub_manual_renewal_date'
        manualCancellationDate  column:'sub_manual_cancellation_date'
        instanceOf              column:'sub_parent_sub_fk', index:'sub_parent_idx'
        previousSubscription    column:'sub_previous_subscription_fk' //-> see Links, deleted as ERMS-800
        isSlaved        column:'sub_is_slaved'
        noticePeriod    column:'sub_notice_period'
        isPublic        column:'sub_is_public'
        pendingChanges  sort: 'ts', order: 'asc'
    }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        status(nullable:true, blank:false)
        type(nullable:true, blank:false)
        owner(nullable:true, blank:false)
        form        (nullable:true, blank:false)
        resource    (nullable:true, blank:false)
        impId(nullable:true, blank:false)
        startDate(nullable:true, blank:false)
        endDate(nullable:true, blank:false)
        manualRenewalDate(nullable:true, blank:false)
        manualCancellationDate(nullable:true, blank:false)
        instanceOf(nullable:true, blank:false)
        previousSubscription(nullable:true, blank:false) //-> see Links, deleted as ERMS-800
        isSlaved(nullable:true, blank:false)
        noticePeriod(nullable:true, blank:true)
        isPublic(nullable:true, blank:true)
        cancellationAllowances(nullable:true, blank:true)
        lastUpdated(nullable: true, blank: true)
    }

    @Override
    boolean isDeleted() {
        return RDStore.SUBSCRIPTION_DELETED.id == status?.id
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
        getCalculatedType() == TemplateSupport.CALCULATED_TYPE_CONSORTIAL
    }

    @Override
    void updateShare(ShareableTrait sharedObject) {
        log.debug('updateShare: ' + sharedObject)

        if (sharedObject instanceof DocContext || sharedObject instanceof OrgRole) {
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
        def result = TemplateSupport.CALCULATED_TYPE_UNKOWN

        if (isTemplate()) {
            result = TemplateSupport.CALCULATED_TYPE_TEMPLATE
        }
        else if(getConsortia() && ! getAllSubscribers() && ! instanceOf) {
            result = TemplateSupport.CALCULATED_TYPE_CONSORTIAL
        }
        else if(getConsortia() /* && getAllSubscribers() */ && instanceOf) {
            // current and deleted member subscriptions
            result = TemplateSupport.CALCULATED_TYPE_PARTICIPATION
        }
        else if(! getConsortia() && getAllSubscribers() && ! instanceOf) {
            result = TemplateSupport.CALCULATED_TYPE_LOCAL
        }
        result
    }

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
    isSlaved?.value == "Yes" ? "Yes" : "No"
  }

  def getSubscriber() {
    def result = null;
    def cons = null;
    
    orgRelations.each { or ->
      if ( or?.roleType?.value in ['Subscriber', 'Subscriber_Consortial'] )
        result = or.org;
        
      if ( or?.roleType?.value=='Subscription Consortia' )
        cons = or.org;
    }
    
    if ( !result && cons ) {
      result = cons
    }
    
    result
  }

  def getAllSubscribers() {
    def result = [];
    orgRelations.each { or ->
      if ( or?.roleType?.value in ['Subscriber', 'Subscriber_Consortial'] )
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

    boolean isEditableBy(user) {
        hasPerm('edit', user)
    }

    boolean isVisibleBy(user) {
        hasPerm('view', user)
    }

    boolean hasPerm(perm, user) {
        if (perm == 'view' && this.isPublic?.value == 'Yes') {
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
            OrgRole subscrCons = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIBER_CONS
            )
            OrgRole subscr = OrgRole.findBySubAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_SUBSCRIBER
            )

            if (perm == 'view') {
                return cons || subscrCons || subscr
            }
            if (perm == 'edit') {
                return cons || subscr
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

            if (newPendingChange && ds.isSlaved?.value == "Yes") {
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
        Subscription.where{ instanceOf == this && (status == null || status.value != 'Deleted') }
    }

    def getCalculatedPropDefGroups(Org contextOrg) {
        def result = [ 'global':[], 'local':[], 'member':[], 'fallback': true, 'orphanedProperties':[]]

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

        result.fallback = (result.global.size() == 0 && result.local.size() == 0 && result.member.size() == 0)

        // storing properties without groups

        def orph = customProperties.id

        result.global.each{ gl -> orph.removeAll(gl.getCurrentProperties(this).id) }
        result.local.each{ lc  -> orph.removeAll(lc[0].getCurrentProperties(this).id) }
        result.member.each{ m  -> orph.removeAll(m[0].getCurrentProperties(this).id) }

        result.orphanedProperties = SubscriptionCustomProperty.findAllByIdInList(orph)

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

    if(params.hideDeleted == 'true'){
      hqlString += " AND sub.status.value != 'Deleted' "
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
      def types = issueEntitlements?.tipp.title.type.unique()
      types
  }

   def dropdownNamingConvention(){

       def contextOrg = contextService.getOrg()
       def messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
       SimpleDateFormat sdf = new SimpleDateFormat(messageSource.getMessage('default.date.format.notime',null, LocaleContextHolder.getLocale()))

       String period = startDate ? sdf.format(startDate)  : ''

       period = endDate ? period + ' - ' + sdf.format(endDate)  : ''

       period = period ? '('+period+')' : ''



       if(instanceOf)
       {
           def additionalInfo

           if(contextOrg.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id))
           {
               additionalInfo = (getSubscriber() ? getSubscriber()?.sortname : '')
           }else{
               additionalInfo = messageSource.getMessage('gasco.filter.consortialLicence',null, LocaleContextHolder.getLocale())
           }

           return name + ' - ' + status.getI10n('value') + ' ' +period + ' - ' + additionalInfo

       }else {

           return name + ' - ' + status.getI10n('value') + ' ' +period
       }
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
