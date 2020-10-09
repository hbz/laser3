package com.k_int.kbplus

import com.k_int.kbplus.auth.Role
import de.laser.Doc
import de.laser.DocContext
import de.laser.Identifier
import de.laser.Org
import de.laser.OrgRole
import de.laser.Package
import de.laser.PendingChange
import de.laser.PersonRole
import de.laser.RefdataValue
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
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

class License extends AbstractBaseWithCalculatedLastUpdated
        implements AuditableSupport, CalculatedType, Permissions, ShareSupport, Comparable<License> {

    def grailsApplication
    def contextService
    def accessService
    def genericOIDService
    def messageSource
    def pendingChangeService
    def changeNotificationService
    def propertyService
    def deletionService
    def auditService

    static Log static_logger = LogFactory.getLog(License)

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

    Date startDate
    Date endDate

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static transients = ['referenceConcatenated', 'licensingConsortium', 'licensor', 'licensee', 'genericLabel', 'nonDeletedDerivedLicenses'] // mark read-only accessor methods

  static hasMany = [
          ids            : Identifier,
          pkgs           :         Package,
          //subscriptions:Subscription,
          documents      :    DocContext,
          orgRelations       :     OrgRole,
          prsLinks       :     PersonRole,
          derivedLicenses:    License,
          pendingChanges :     PendingChange,
          propertySet    :   LicenseProperty,
          //privateProperties:  LicensePrivateProperty
  ]

  static mappedBy = [
          ids:           'lic',
          pkgs:          'license',
          //subscriptions: 'owner',
          documents:     'license',
          orgRelations:      'lic',
          prsLinks:      'lic',
          derivedLicenses: 'instanceOf',
          pendingChanges:  'license',
          propertySet:  'owner',
          //privateProperties: 'owner'
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
              documents sort:'owner.id', order:'desc', batchSize: 10
        licenseCategory column: 'lic_category_rdv_fk'
              startDate column: 'lic_start_date',   index: 'lic_dates_idx'
                endDate column: 'lic_end_date',     index: 'lic_dates_idx'
      lastUpdatedCascading column: 'lic_last_updated_cascading'

       propertySet sort:'type', order:'desc', batchSize: 10
    //privateProperties sort:'type', order:'desc', batchSize: 10
         pendingChanges sort: 'ts', order: 'asc', batchSize: 10

              ids               batchSize: 10
              pkgs              batchSize: 10
              //subscriptions     sort:'name',order:'asc', batchSize: 10
              orgRelations          batchSize: 10
              prsLinks          batchSize: 10
              derivedLicenses   batchSize: 10
  }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        type        (nullable:true)
        reference(blank:false)
        sortableReference(nullable:true, blank:true) // !! because otherwise, the beforeInsert() method which generates a value is not executed
        noticePeriod(nullable:true, blank:true)
        licenseUrl(nullable:true, blank:true)
        instanceOf  (nullable:true)
        licenseCategory (nullable: true)
        startDate(nullable: true, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['startDateAfterEndDate']
            }
        })
        endDate(nullable: true, validator: { val, obj ->
            if(obj.startDate != null && obj.endDate != null) {
                if(obj.startDate > obj.endDate) return ['endDateBeforeStartDate']
            }
        })
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    @Override
    Collection<String> getLogIncluded() {
        [ 'startDate', 'endDate', 'licenseUrl', 'licenseCategory', 'status', 'type', 'openEnded', 'isPublicForApi' ]
    }
    @Override
    Collection<String> getLogExcluded() {
        [ 'version', 'lastUpdated', 'lastUpdatedCascading', 'pendingChanges' ]
    }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        deletionService.deleteDocumentFromIndex(this.globalUID)
    }
    @Override
    def afterInsert() {
        super.afterInsertHandler()
    }
    @Override
    def afterUpdate() {
        super.afterUpdateHandler()
    }

    @Override
    def beforeInsert() {
        if ( reference != null && !sortableReference) {
            sortableReference = generateSortableReference(reference)
        }
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        if ( reference != null && !sortableReference) {
            sortableReference = generateSortableReference(reference)
        }
        Map<String, Object> changes = super.beforeUpdateHandler()
        log.debug ("beforeUpdate() " + changes.toMapString())

        auditService.beforeUpdateHandler(this, changes.oldMap, changes.newMap)
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
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
        _getCalculatedType() == CalculatedType.TYPE_CONSORTIAL
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
    String _getCalculatedType() {
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

        License.findAllByInstanceOf(this).each { License l ->
            List<OrgRole> ors = OrgRole.findAllWhere( lic: l )
            ors.each { OrgRole or ->
                if (or.roleType in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS]) {
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
        orgRelations.each { OrgRole or ->
            if ( or.roleType == RDStore.OR_LICENSING_CONSORTIUM )
                result = or.org
            }
        result
    }

    Org getLicensor() {
        Org result
        orgRelations.each { OrgRole or ->
            if ( or.roleType == RDStore.OR_LICENSOR )
                result = or.org
        }
        result
    }

    Org getLicensee() {
        Org result
        orgRelations.each { OrgRole or ->
            if ( or.roleType in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS] )
                result = or.org
        }
        result
    }
    List<Org> getAllLicensee() {
        List<Org> result = []
        orgRelations.each { OrgRole or ->
            if ( or.roleType in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS] )
                result << or.org
        }
        result
  }

    DocContext getNote(String domain) {
        DocContext.findByLicenseAndDomain(this, domain)
    }

  void setNote(String domain, String note_content) {
      withTransaction {
          DocContext note = DocContext.findByLicenseAndDomain(this, domain)
          if (note) {
              log.debug("update existing note...");
              if (note_content == '') {
                  log.debug("Delete note doc ctx...");
                  note.delete()
                  note.owner.delete()
              } else {
                  note.owner.content = note_content
                  note.owner.save()
              }
          } else {
              log.debug("Create new note...");
              if ((note_content) && (note_content.trim().length() > 0)) {
                  Doc doc = new Doc(content: note_content, lastUpdated: new Date(), dateCreated: new Date())
                  DocContext newctx = new DocContext(license: this, owner: doc, domain: domain)
                  doc.save()
                  newctx.save()
              }
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
    def notifyDependencies(changeDocument) {
        log.debug("notifyDependencies(${changeDocument})")

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
                                changeTarget:"${License.class.name}:${dl.id}",
                                changeType:PendingChangeService.EVENT_PROPERTY_CHANGE,
                                changeDoc:changeDocument
                              ],
                        PendingChange.MSG_LI01,
                        msgParams,
                    "<strong>${changeDocument.prop}</strong> hat sich von <strong>\"${changeDocument.oldLabel?:changeDocument.old}\"</strong> zu <strong>\"${changeDocument.newLabel?:changeDocument.new}\"</strong> von der Vertragsvorlage geändert. " + description
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

    Map<String, Object> _getCalculatedPropDefGroups(Org contextOrg) {
        Map<String, Object> result = [ 'sorted':[], 'global':[], 'local':[], 'member':[], 'orphanedProperties':[]]

        // ALL type depending groups without checking tenants or bindings
        List<PropertyDefinitionGroup> groups = PropertyDefinitionGroup.findAllByOwnerType(License.class.name, [sort:'name', order:'asc'])
        groups.each{ it ->

            // cons_members
            if (this.instanceOf) {
                Long licId
                if(this.getLicensingConsortium().id == contextOrg.id)
                    licId = this.instanceOf.id
                else licId = this.id
                List<PropertyDefinitionGroupBinding> bindings = PropertyDefinitionGroupBinding.executeQuery('select b from PropertyDefinitionGroupBinding b where b.propDefGroup = :pdg and b.lic.id = :id and b.propDefGroup.tenant = :ctxOrg',[pdg:it, id: licId,ctxOrg:contextOrg])
                PropertyDefinitionGroupBinding binding = null
                if(bindings)
                    binding = bindings.get(0)

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
                if (it.tenant?.id == contextOrg.id) {
                    if (binding) {
                        if(contextOrg.id == this.getLicensingConsortium().id) {
                            result.member << [it, binding] // TODO: remove
                            result.sorted << ['member', it, binding]
                        }
                        else {
                            result.local << [it, binding] // TODO: remove
                            result.sorted << ['local', it, binding]
                        }
                    }
                    else {
                        result.global << it // TODO: remove
                        result.sorted << ['global', it, null]
                    }
                }
                // licensee consortial; getting group by consortia and instanceOf.binding
                else if (it.tenant?.id == this.getLicensingConsortium().id) {
                    if (binding) {
                        result.member << [it, binding] // TODO: remove
                        result.sorted << ['member', it, binding]
                    }
                }
            }
            // consortium or locals
            else {
                PropertyDefinitionGroupBinding binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndLic(it, this)

                if (it.tenant == null || it.tenant?.id == contextOrg.id) {
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

    String dropdownNamingConvention() {
        String statusString = "" + status ? status.getI10n('value') : RDStore.LICENSE_NO_STATUS.getI10n('value')

        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()
        String period = startDate ? sdf.format(startDate) : ''
        period = endDate ? period + ' - ' + sdf.format(endDate) : ''
        period = period ? '(' + period + ')' : ''

        String result = ''
        result += reference + " - " + statusString + " " + period
        if (CalculatedType.TYPE_PARTICIPATION == _getCalculatedType()) {
            result += " - " + messageSource.getMessage('license.member', null, LocaleContextHolder.getLocale())
        }

        return result
    }

    Set<Subscription> getSubscriptions() {
        Set<Subscription> result = Subscription.executeQuery("select li.destinationSubscription from Links li where li.sourceLicense = :license and li.linkType = :linkType",[license:this,linkType:RDStore.LINKTYPE_LICENSE])
        /*Links.findAllBySourceAndSourceTypeAndDestinationTypeAndLinkType(genericOIDService.getOID(this),RDStore.LINKTYPE_LICENSE).each { l ->
            result << genericOIDService.resolveOID(l.destination)
        }*/
        result
    }
}
