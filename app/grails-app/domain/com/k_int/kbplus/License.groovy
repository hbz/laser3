package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import com.k_int.properties.PropertyDefinition
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.traits.BaseTraitWithCalculatedLastUpdated
import de.laser.helper.DateUtil
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.AuditableSupport
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.Permissions
import de.laser.interfaces.ShareSupport
import de.laser.traits.ShareableTrait
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.Normalizer
import java.text.SimpleDateFormat

class License
        implements BaseTraitWithCalculatedLastUpdated, CalculatedType, Permissions, AuditableSupport, ShareSupport, Comparable<License> {

    static Log static_logger = LogFactory.getLog(License)

    @Transient
    def grailsApplication
    @Transient
    def contextService
    @Transient
    def accessService
    @Transient
    def genericOIDService
    @Transient
    def messageSource
    @Transient
    def pendingChangeService
    @Transient
    def changeNotificationService
    @Transient
    def propertyService
    @Transient
    def deletionService
    @Transient
    def auditService

    static auditable            = [ ignore: ['version', 'lastUpdated', 'lastUpdatedCascading', 'pendingChanges'] ]
    static controlledProperties = [ 'startDate', 'endDate', 'licenseUrl', 'licenseCategory', 'status', 'type', 'openEnded', 'isPublicForApi' ]

    License instanceOf

    // If a license is slaved then any changes to instanceOf will automatically be applied to this license
    boolean isSlaved = false
    boolean isPublicForApi = false

    @RefdataAnnotation(cat = RDConstants.LICENSE_STATUS)
    RefdataValue status

    @RefdataAnnotation(cat = RDConstants.LICENSE_TYPE)
    RefdataValue type

    @RefdataAnnotation(cat = RDConstants.LICENSE_CATEGORY)
    RefdataValue licenseCategory

    String reference
    String sortableReference

    String noticePeriod
    String licenseUrl

    @RefdataAnnotation(cat = RDConstants.Y_N_U)
    RefdataValue openEnded
  //String licenseStatus

    //long lastmod
    Date startDate
    Date endDate

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

  //static hasOne = [onixplLicense: OnixplLicense]

  static hasMany = [
          ids: Identifier,
          pkgs:         Package,
          //subscriptions:Subscription,
          documents:    DocContext,
          orgLinks:     OrgRole,
          prsLinks:     PersonRole,
          derivedLicenses:    License,
          pendingChanges:     PendingChange,
          customProperties:   LicenseCustomProperty,
          privateProperties:  LicensePrivateProperty
  ]

  static mappedBy = [
          ids:           'lic',
          pkgs:          'license',
          //subscriptions: 'owner',
          documents:     'license',
          orgLinks:      'lic',
          prsLinks:      'lic',
          derivedLicenses: 'instanceOf',
          pendingChanges:  'license',
          customProperties:  'owner',
          privateProperties: 'owner'
  ]

  static mapping = {
                    sort sortableReference: 'asc'
                     id column:'lic_id'
                version column:'lic_version'
              globalUID column:'lic_guid'
                 status column:'lic_status_rv_fk'
                   type column:'lic_type_rv_fk'
              reference column:'lic_ref'
      sortableReference column:'lic_sortable_ref'
           noticePeriod column:'lic_notice_period'
             licenseUrl column:'lic_license_url'
             instanceOf column:'lic_parent_lic_fk', index:'lic_parent_idx'
         isPublicForApi column:'lic_is_public_for_api'
               isSlaved column:'lic_is_slaved'
              openEnded column:'lic_open_ended_rv_fk'
        //licenseStatus column:'lic_license_status_str'
              //lastmod column:'lic_lastmod'
              documents sort:'owner.id', order:'desc', batchSize: 10
        //onixplLicense column: 'lic_opl_fk'
        licenseCategory column: 'lic_category_rdv_fk'
              startDate column: 'lic_start_date',   index: 'lic_dates_idx'
                endDate column: 'lic_end_date',     index: 'lic_dates_idx'
      lastUpdatedCascading column: 'lic_last_updated_cascading'

       customProperties sort:'type', order:'desc', batchSize: 10
      privateProperties sort:'type', order:'desc', batchSize: 10
         pendingChanges sort: 'ts', order: 'asc', batchSize: 10

              ids               batchSize: 10
              pkgs              batchSize: 10
              //subscriptions     sort:'name',order:'asc', batchSize: 10
              orgLinks          batchSize: 10
              prsLinks          batchSize: 10
              derivedLicenses   batchSize: 10
  }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        status(nullable:false, blank:false)
        type(nullable:true, blank:false)
        reference(nullable:false, blank:false)
        sortableReference(nullable:true, blank:true) // !! because otherwise, the beforeInsert() method which generates a value is not executed
        isPublicForApi (nullable:true, blank:true)
        noticePeriod(nullable:true, blank:true)
        licenseUrl(nullable:true, blank:true)
        instanceOf(nullable:true, blank:false)
        isSlaved    (nullable:false, blank:false)
        //licenseStatus(nullable:true, blank:true)
        //lastmod(nullable:true, blank:true)
        //onixplLicense(nullable: true, blank: true)
        licenseCategory(nullable: true, blank: true)
        startDate(nullable: true, blank: false, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['startDateAfterEndDate']
            }
        })
        endDate(nullable: true, blank: false, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['endDateBeforeStartDate']
            }
        })
        lastUpdated(nullable: true, blank: true)
        lastUpdatedCascading (nullable: true, blank: false)
    }

    @Override
    def afterDelete() {
        static_logger.debug("afterDelete")
        cascadingUpdateService.update(this, new Date())

        deletionService.deleteDocumentFromIndex(this.globalUID)
    }

    @Transient
    def onChange = { oldMap, newMap ->
        log.debug("onChange ${this}")
        auditService.onChangeHandler(this, oldMap, newMap)
    }

    @Override
    boolean checkSharePreconditions(ShareableTrait sharedObject) {
        // needed to differentiate OrgRoles
        if (sharedObject instanceof OrgRole) {
            if (showUIShareButton() && sharedObject.roleType.value == 'Licensor') {
                return true
            }
        }
        false
    }

    boolean showUIShareButton() {
        getCalculatedType() == CalculatedType.TYPE_CONSORTIAL
    }

    void updateShare(ShareableTrait sharedObject) {
        log.debug('updateShare: ' + sharedObject)

        if (sharedObject instanceof DocContext || sharedObject instanceof OrgRole) {
            if (sharedObject.isShared) {
                List<License> newTargets = License.findAllByInstanceOf(this)
                log.debug('found targets: ' + newTargets)

                newTargets.each{ lic ->
                    log.debug('adding for: ' + lic)
                    sharedObject.addShareForTarget_trait(lic)
                }
            }
            else {
                sharedObject.deleteShare_trait()
            }
        }
    }

    void syncAllShares(List<ShareSupport> targets) {
        log.debug('synAllShares: ' + targets)

        documents.each{ sharedObject ->
            targets.each{ lic ->
                if (sharedObject.isShared) {
                    log.debug('adding for: ' + lic)
                    sharedObject.addShareForTarget_trait(lic)
                }
                else {
                    log.debug('deleting all shares')
                    sharedObject.deleteShare_trait()
                }
            }
        }
        orgLinks.each{ sharedObject ->
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
        String result = CalculatedType.TYPE_UNKOWN

        if (getLicensingConsortium() && ! instanceOf) {
            result = CalculatedType.TYPE_CONSORTIAL
        }
        else if (getLicensingConsortium() /*&& getAllLicensee()*/ && instanceOf) {
            // current and deleted member licenses
            result = CalculatedType.TYPE_PARTICIPATION
        }
        else if (! getLicensingConsortium()) {
            result = CalculatedType.TYPE_LOCAL
        }
        result
    }

    List<Org> getDerivedLicensees() {
        List<Org> result = []

        License.findAllByInstanceOf(this).each { l ->
            List<OrgRole> ors = OrgRole.findAllWhere( lic: l )
            ors.each { or ->
                if (or.roleType?.value in ['Licensee', 'Licensee_Consortial']) {
                    result << or.org
                }
            }
        }
        result = result.sort {it.name}
    }

    // used for views and dropdowns
    String getReferenceConcatenated() {
        Org cons = getLicensingConsortium()
        List<Org> subscr = getAllLicensee()
        if (subscr) {
            "${reference} (" + subscr.join(', ') + ")"
        }
        else if (cons){
            "${reference} (${cons})"
        }
        else {
            reference
        }
    }

    Org getLicensingConsortium() {
        Org result
        orgLinks.each { or ->
            if ( or.roleType.value == 'Licensing Consortium' )
                result = or.org
            }
        result
    }

    Org getLicensor() {
        Org result
        orgLinks.each { or ->
            if ( or.roleType.value in ['Licensor'] )
                result = or.org;
        }
        result
    }

    Org getLicensee() {
        Org result
        orgLinks.each { or ->
            if ( or.roleType.value in ['Licensee', 'Licensee_Consortial'] )
                result = or.org;
        }
        result
    }
    List<Org> getAllLicensee() {
        List<Org> result = []
        orgLinks.each { or ->
            if ( or.roleType.value in ['Licensee', 'Licensee_Consortial'] )
                result << or.org
        }
        result
  }

  @Transient
  def getLicenseType() {
    return type?.value
  }

    DocContext getNote(domain) {
        DocContext.findByLicenseAndDomain(this, domain)
    }

  void setNote(domain, note_content) {
      DocContext note = DocContext.findByLicenseAndDomain(this, domain)
    if ( note ) {
      log.debug("update existing note...");
      if ( note_content == '' ) {
        log.debug("Delete note doc ctx...");
        note.delete();
        note.owner.delete(flush:true);
      }
      else {
        note.owner.content = note_content
        note.owner.save(flush:true);
      }
    }
    else {
      log.debug("Create new note...");
      if ( ( note_content ) && ( note_content.trim().length() > 0 ) ) {
          Doc doc = new Doc(content:note_content, lastUpdated:new Date(), dateCreated: new Date())
          DocContext newctx = new DocContext(license: this, owner: doc, domain:domain)
        doc.save();
        newctx.save(flush:true);
      }
    }
  }

    String getGenericLabel() {
        return reference
    }

    boolean isEditableBy(user) {
        hasPerm("edit", user)
    }

    boolean isVisibleBy(user) {
      hasPerm('view', user)
    }

    boolean hasPerm(perm, user) {
        Role adm = Role.findByAuthority('ROLE_ADMIN')
        Role yda = Role.findByAuthority('ROLE_YODA')

        if (user.getAuthorities().contains(adm) || user.getAuthorities().contains(yda)) {
            return true
        }

        if (user.getAuthorizedOrgsIds().contains(contextService.getOrg().id)) {

            OrgRole cons = OrgRole.findByLicAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_LICENSING_CONSORTIUM
            )
            OrgRole licseeCons = OrgRole.findByLicAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_LICENSEE_CONS
            )
            OrgRole licsee = OrgRole.findByLicAndOrgAndRoleType(
                    this, contextService.getOrg(), RDStore.OR_LICENSEE
            )

            if (perm == 'view') {
                return cons || licseeCons || licsee
            }
            if (perm == 'edit') {
                if(accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN'))
                    return cons || licsee
            }
        }

        return false
    }

  @Override
  boolean equals (Object o) {
    //def obj = ClassUtils.deproxy(o)
    def obj = GrailsHibernateUtil.unwrapIfProxy(o)
    if (obj != null) {
      if ( obj instanceof License ) {
        return obj.id == id
      }
    }
    return false
  }

  @Override
  String toString() {
    reference ? "${reference}" : "License ${id}"
  }
  
  @Override
  int compareTo(License other){
      return other.id? other.id.compareTo(this.id) : -1
  }


    @Transient
    def notifyDependencies_trait(changeDocument) {
        log.debug("notifyDependencies_trait(${changeDocument})")

        List<PendingChange> slavedPendingChanges = []
        // Find any licenses derived from this license
        // create a new pending change object
        //def derived_licenses = License.executeQuery('select l from License as l where exists ( select link from Link as link where link.toLic=l and link.fromLic=? )',this)
        def derived_licenses = getNonDeletedDerivedLicenses()

        derived_licenses.each { dl ->
            log.debug("Send pending change to ${dl.id}")

            Locale locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
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

            PendingChange newPendingChange = changeNotificationService.registerPendingChange(
                        PendingChange.PROP_LICENSE,
                        dl,
                        dl.getLicensee(),
                              [
                                changeTarget:"com.k_int.kbplus.License:${dl.id}",
                                changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                                changeDoc:changeDocument
                              ],
                        PendingChange.MSG_LI01,
                        msgParams,
                    "<b>${changeDocument.prop}</b> hat sich von <b>\"${changeDocument.oldLabel?:changeDocument.old}\"</b> zu <b>\"${changeDocument.newLabel?:changeDocument.new}\"</b> von der Vertragsvorlage geändert. " + description
            )

            if (newPendingChange && dl.isSlaved) {
                slavedPendingChanges << newPendingChange
            }
        }

        slavedPendingChanges.each { spc ->
            log.debug('autoAccept! performing: ' + spc)
            pendingChangeService.performAccept(spc)
        }
    }

    def getNonDeletedDerivedLicenses() {
        License.where{ instanceOf == this }
    }

    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        Map<String, Object> result = [ 'sorted':[], 'global':[], 'local':[], 'member':[], 'orphanedProperties':[]]

        // ALL type depending groups without checking tenants or bindings
        List<PropertyDefinitionGroup> groups = PropertyDefinitionGroup.findAllByOwnerType(License.class.name, [sort:'name', order:'asc'])
        groups.each{ it ->

            // cons_members
            if (this.instanceOf) {
                PropertyDefinitionGroupBinding binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndLic(it, this.instanceOf)

                // global groups
                if (it.tenant == null) {
                    if (binding) {
                        result.member << [it, binding] // TODO: remove
                        result.sorted << ['member', it, binding]
                    } else {
                        result.global << it // TODO: remove
                        result.sorted << ['global', it, null]
                    }
                }
                // consortium @ member; getting group by tenant and instanceOf.binding
                if (it.tenant?.id == contextOrg?.id) {
                    if (binding) {
                        result.member << [it, binding] // TODO: remove
                        result.sorted << ['member', it, binding]
                    }
                }
                // licensee consortial; getting group by consortia and instanceOf.binding
                else if (it.tenant?.id == this.instanceOf.getLicensingConsortium()?.id) {
                    if (binding) {
                        result.member << [it, binding] // TODO: remove
                        result.sorted << ['member', it, binding]
                    }
                }
            }
            // consortium or locals
            else {
                PropertyDefinitionGroupBinding binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndLic(it, this)

                if (it.tenant == null || it.tenant?.id == contextOrg?.id) {
                    if (binding) {
                        result.local << [it, binding] // TODO: remove
                        result.sorted << ['local', it, binding]
                    } else {
                        result.global << it // TODO: remove
                        result.sorted << ['global', it, null]
                    }
                }
            }
        }

        // storing properties without groups
        result.orphanedProperties = propertyService.getOrphanedProperties(this, result.sorted)

        result
    }

    @Override
    def beforeInsert() {
         if ( reference != null && !sortableReference) {
            sortableReference = generateSortableReference(reference)
        }
        super.beforeInsert()
    }

    @Override
    def beforeUpdate() {
        if ( reference != null && !sortableReference) {
            sortableReference = generateSortableReference(reference)
        }
        super.beforeUpdate()
    }


  static String generateSortableReference(String input_title) {
    String result = ''

    if (input_title) {
      String s1 = Normalizer.normalize(input_title, Normalizer.Form.NFKD).trim().toLowerCase()
      s1 = s1.replaceFirst('^copy of ','')
      s1 = s1.replaceFirst('^the ','')
      s1 = s1.replaceFirst('^a ','')
      s1 = s1.replaceFirst('^der ','')
      result = s1.trim()
    }
    result
  }

  /*
    Following getter methods were introduced to avoid making too many changes when custom properties 
    were introduced.
  */
  @Transient
  def getConcurrentUserCount(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Concurrent Users")
  }
  
  @Transient
  def setConcurrentUserCount(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Concurrent Users",newVal)
  }

  @Transient
  def getConcurrentUsers(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Concurrent Access")
  }  
    @Transient
  def setConcurrentUsers(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Concurrent Access",newVal)
  }
  
  @Transient
  def getRemoteAccess(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Remote Access")
  }
  
  @Transient
  def setRemoteAccess(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Remote Access",newVal)
  }
  
  @Transient
  def getWalkinAccess(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Walk In Access")
  }
  
  @Transient
  def setWalkinAccess(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Walk In Access",newVal)
  }
  
  @Transient
  def getMultisiteAccess(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Multi Site Access")
  }
  
  @Transient
  def setMultisiteAccess(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Multi Site Access",newVal)
  }
  
  @Transient
  def getPartnersAccess(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Partners Access")
  }
  
  @Transient
  def setPartnersAccess(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Partners Access",newVal)
  }
 
  @Transient
  def getAlumniAccess(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Alumni Access")
  }
 
  @Transient
  def setAlumniAccess(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Alumni Access",newVal)
  }
  @Transient
  def getIll(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("ILL - InterLibraryLoans")
  }

  @Transient
  def setIll(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("ILL - InterLibraryLoans",newVal)
  }
  @Transient
  def getCoursepack(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Include In Coursepacks")
  }

  @Transient
  def setCoursepack(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Include In Coursepacks",newVal)
  }
  
  @Transient
  def getVle(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Include in VLE")
  }
  
  @Transient
  def setVle(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Include in VLE",newVal)
  }

  @Transient
  def getEnterprise(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Enterprise Access")
  }
  @Transient
  def setEnterprise(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Enterprise Access",newVal)

  }

  @Transient
  def getPca(){
    log.error("called cust prop with deprecated method.Call should be replaced")
    return getCustomPropByName("Post Cancellation Access Entitlement")
  }

  @Transient
  def setPca(newVal){
    log.error("called cust prop with deprecated method.Call should be replaced")
    setReferencePropertyAsCustProp("Post Cancellation Access Entitlement",newVal)
  }

  @Transient
  def setReferencePropertyAsCustProp(custPropName, newVal) {
    def custProp = getCustomPropByName(custPropName)
    if(custProp == null){
      def type = PropertyDefinition.findWhere(name: custPropName, tenant: null)
      custProp = PropertyDefinition.createGenericProperty(PropertyDefinition.CUSTOM_PROPERTY, this, type)
    }

    if ( newVal != null ) {
      custProp.refValue = genericOIDService.resolveOID(newVal)
    }
    else {
      custProp.refValue = null;
    }

    custProp.save()
   
  }

  
  @Transient
  def getCustomPropByName(name){
    return customProperties.find{it.type.name == name}    
  }

  static def refdataFind(params) {

      String INSTITUTIONAL_LICENSES_QUERY = """
 FROM License AS l WHERE
( exists ( SELECT ol FROM OrgRole AS ol WHERE ol.lic = l AND ol.org.id =(:orgId) AND ol.roleType.id IN (:orgRoles)) )
AND lower(l.reference) LIKE (:ref)
"""
      def result = []
      def ql

        // TODO: ugly select2 fallback
      def roleTypes = []
      if (params.'roleTypes[]') {
          params.'roleTypes[]'.each{ x -> roleTypes << x.toLong() }
      } else {
          roleTypes << params.roleType?.toLong()
      }

      ql = License.executeQuery("select l ${INSTITUTIONAL_LICENSES_QUERY}",
        [orgId: params.inst?.toLong(), orgRoles: roleTypes, ref: "${params.q.toLowerCase()}"])


      if ( ql ) {
          ql.each { lic ->
              def type = lic.type?.value ?"(${lic.type.value})":""
              result.add([id:"${lic.reference}||${lic.id}",text:"${lic.reference}${type}"])
          }
      }
      result
  }

    def getBaseCopy() {

        def copy = new License(
                //globalUID: globalUID,
                status: status, // fk
                type: type, // fk
                reference: reference,
                sortableReference: sortableReference,
                licenseCategory: licenseCategory, // fk
                noticePeriod: noticePeriod,
                licenseUrl: licenseUrl,
                licenseType: licenseType,
                //licenseStatus: licenseStatus,
                //lastmod: lastmod,
                startDate: startDate,
                endDate: endDate,
                dateCreated: dateCreated,
                lastUpdated: lastUpdated
                //onixplLicense: onixplLicense // fk
        )

        copy
    }
    String dropdownNamingConvention() {
        String statusString = "" + status ? status.getI10n('value') : RDStore.LICENSE_NO_STATUS.getI10n('value')

        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
        String period = startDate ? sdf.format(startDate) : ''
        period = endDate ? period + ' - ' + sdf.format(endDate) : ''
        period = period ? '(' + period + ')' : ''

        String result = ''
        result += reference + " - " + statusString + " " + period
        if (CalculatedType.TYPE_PARTICIPATION == getCalculatedType()) {
            result += " - " + messageSource.getMessage('license.member', null, LocaleContextHolder.getLocale())
        }

        return result
    }

    Set<Subscription> getSubscriptions() {
        Set<Subscription> result = []
        Links.findAllBySourceAndLinkType(GenericOIDService.getOID(this),RDStore.LINKTYPE_LICENSE).each { l ->
            result << genericOIDService.resolveOID(l.destination)
        }
        result
    }
}
