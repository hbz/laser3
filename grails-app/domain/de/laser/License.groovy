package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.CustomerTypeService
import de.laser.interfaces.CalculatedType
import de.laser.interfaces.Permissions
import de.laser.interfaces.ShareSupport
import de.laser.properties.LicenseProperty
import de.laser.properties.PropertyDefinitionGroup
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.storage.RDStore
import de.laser.traits.ShareableTrait
import de.laser.utils.DateUtils
import de.laser.utils.LocaleUtils
import grails.plugins.orm.auditable.Auditable
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

import java.text.Normalizer
import java.text.SimpleDateFormat

/**
 * <p>One of the central domains in LAS:eR; only {@link Subscription} is more essential.</p>
 * <p>A license is the entity which retains the framing conditions for one or more subscriptions. Like with subscriptions, there are consortial and local objects.
 * Consortial objects have two levels: a parent and a child level whereas local objects have only one level. Former are used by consortia; their child objects are
 * assigned to basic members or single users. Latter ones are used by single users. The main difference between consortia and local objects (counts for subscriptions as well!) is
 * that consortia control both levels and domain attributes and properties ({@link LicenseProperty}) may be passed from the parent to the child object just as documents (the {@link DocContext}
 * linking is being shared for that; technically, we multiply the pointers to the same document when we share one). On intellectual level, the passing of parental attributes to a child is
 * named two different ways: domain attributes and properties are inherited to child objects, documents are shared among the children.</p>
 * <p>The child-parent relation is represented by the {@link #instanceOf} field; a child is instance of a parent.</p>
 */
class License extends AbstractBaseWithCalculatedLastUpdated
        implements Auditable, CalculatedType, Permissions, ShareSupport, Comparable<License> {

    License instanceOf

    /**
     * If a license is slaved then any changes to instanceOf will automatically be applied to this license
     */
    boolean isSlaved = false
    boolean isPublicForApi = false

    @RefdataInfo(cat = RDConstants.LICENSE_STATUS, i18n = 'license.status.label')
    RefdataValue status

    @RefdataInfo(cat = RDConstants.LICENSE_CATEGORY, i18n = 'license.category.label')
    RefdataValue licenseCategory

    /**
     * the actual name of the license
     */
    String reference
    /**
     * the sortable name of the license
     */
    String sortableReference

    String noticePeriod
    String licenseUrl

    @RefdataInfo(cat = RDConstants.Y_N_U)
    RefdataValue openEnded

    Date startDate
    Date endDate

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    SortedSet ids

    static transients = [
            'referenceConcatenated', 'licensingConsortium', 'licensor', 'licensee',
            'calculatedPropDefGroups', 'genericLabel', 'nonDeletedDerivedLicenses'
    ] // mark read-only accessor methods

  static hasMany = [
          ids            : Identifier,
          //pkgs           :         Package,
          //subscriptions:Subscription,
          documents      :    DocContext,
          orgRelations       :     OrgRole,
          prsLinks       :     PersonRole,
          derivedLicenses:    License,
          propertySet    :   LicenseProperty
  ]

  static mappedBy = [
          ids:           'lic',
          //pkgs:          'license',
          //subscriptions: 'owner',
          documents:     'license',
          orgRelations:      'lic',
          prsLinks:      'lic',
          derivedLicenses: 'instanceOf',
          propertySet:  'owner'
  ]

  static mapping = {
                    sort sortableReference: 'asc'
                     id column:'lic_id'
                version column:'lic_version'
              globalUID column:'lic_guid'
                 status column:'lic_status_rv_fk'
              reference column:'lic_ref'
      sortableReference column:'lic_sortable_ref'
           noticePeriod column:'lic_notice_period'
             licenseUrl column:'lic_license_url'
             instanceOf column:'lic_parent_lic_fk', index:'lic_parent_idx'
         isPublicForApi column:'lic_is_public_for_api'
               isSlaved column:'lic_is_slaved'
              openEnded column:'lic_open_ended_rv_fk'
              documents batchSize: 10
        licenseCategory column: 'lic_category_rdv_fk'
              startDate column: 'lic_start_date',   index: 'lic_dates_idx'
                endDate column: 'lic_end_date',     index: 'lic_dates_idx'

           dateCreated     column: 'lic_date_created'
           lastUpdated     column: 'lic_last_updated'
      lastUpdatedCascading column: 'lic_last_updated_cascading'

       propertySet sort:'type', order:'desc', batchSize: 10

              ids               sort: 'ns', batchSize: 10
              //pkgs            batchSize: 10
              //subscriptions   sort:'name',order:'asc', batchSize: 10
              orgRelations      batchSize: 10
              prsLinks          batchSize: 10
              derivedLicenses   batchSize: 10
  }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
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
        [ 'startDate', 'endDate', 'licenseUrl', 'licenseCategory', 'status', 'openEnded', 'isPublicForApi' ]
    }
    @Override
    Collection<String> getLogExcluded() {
        [ 'version', 'lastUpdated', 'lastUpdatedCascading', 'pendingChanges' ]
    }

    @Override
    def afterDelete() {
        super.afterDeleteHandler()

        BeanStore.getDeletionService().deleteDocumentFromIndex(this.globalUID, this.class.simpleName)
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

        BeanStore.getAuditService().beforeUpdateHandler(this, changes.oldMap, changes.newMap)
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Checks if the license is a consortial parent license and if the licensor relation (license <-> provider) is being shared
     * @param sharedObject the object to be shared
     * @return true if the conditions are met, false otherwise
     */
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

    /**
     * Checks whether this license is a consortial parent license
     * @return true if the license is a consortial parent license, false otherwise
     */
    boolean showUIShareButton() {
        _getCalculatedType() == CalculatedType.TYPE_CONSORTIAL
    }

    /**
     * Toggles the sharing of a {@link DocContext} or {@link OrgRole}
     * @param sharedObject the object which should be shared or not
     */
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

    /**
     * Toggles the sharing of all {@link DocContext}s and {@link OrgRole}s of this license with the given list of targets
     * @param targets the {@link List} of {@link License}s whose documents and org relations should be updated
     */
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

    /**
     * Gets all members (!) of this (consortial parent) license
     * @return a {@link List} of subscriber institutions ({@link Org})
     */
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

    /**
     * Gets a concatenated string representation of the license's name.
     * Used for views and dropdowns
     * @return a concatenated string showing the license's name and consortium or subscriber's list
     */
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

    /**
     * Retrieves the consortium which holds this license
     * @return the licensing consortium {@link Org}
     */
    Org getLicensingConsortium() {
        orgRelations.find { OrgRole or ->
            or.roleType == RDStore.OR_LICENSING_CONSORTIUM
        }?.org
    }

    /**
     * Retrieves the provider for this license
     * @return the licensor provider {@link Org}
     */
    Org getLicensor() {
        orgRelations.find { OrgRole or ->
            or.roleType == RDStore.OR_LICENSOR
        }?.org
    }

    /**
     * Retrieves the licensee org of this license. Is intended to use for licenses where only one licensee is linked (e.g. local licenses);
     * if there are more than one licensees to this license (this is the case if members share one member license instance), the FIRST one is being returned
     * (which may be random)
     * @return the or one licensee {@link Org}
     */
    Org getLicensee() {
        orgRelations.find { OrgRole or ->
            or.roleType in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS]
        }?.org
    }

    /**
     * Retrieves all licensee institutions of this license; this is the method to be used to get all licensees of a member license
     * @return a {@link List} of member {@link Org}s who are linked to this license as members
     */
    List<Org> getAllLicensee() {
        orgRelations.findAll { OrgRole or ->
            or.roleType in [RDStore.OR_LICENSEE, RDStore.OR_LICENSEE_CONS]
        }?.collect { OrgRole or -> or.org }
  }

    @Deprecated
    String getGenericLabel() {
        return reference
    }

    /**
     * Checks if the given user may edit this license
     * @param user the {@link de.laser.auth.User} whose rights should be verified
     * @return true if the user has editing permissions on the license, false otherwise
     */
    boolean isEditableBy(User user) {
        hasPerm("edit", user)
    }

    /**
     * Checks if the given user may view this license
     * @param user the {@link de.laser.auth.User} whose rights should be verified
     * @return true if the user has viewing permissions on the license, false otherwise
     */
    boolean isVisibleBy(User user) {
      hasPerm('view', user)
    }

    /**
     * Checks if the given user has the given permission granted
     * @param perm the permission to check
     * @param user the {@link de.laser.auth.User} whose grant should be verified
     * @return true if the grant for the user is given, false otherwise
     */
    boolean hasPerm(String perm, User user) {
        ContextService contextService = BeanStore.getContextService()
        Role adm = Role.findByAuthority('ROLE_ADMIN')
        Role yda = Role.findByAuthority('ROLE_YODA')

        if (user.getAuthorities().contains(adm) || user.getAuthorities().contains(yda)) {
            return true
        }

        if (user.getAffiliationOrgsIdList().contains(contextService.getOrg().id)) {

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
                if(BeanStore.getAccessService().is_ROLE_ADMIN_or_INST_EDITOR_with_PERMS( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC ))
                    return cons || licsee
            }
        }

        return false
    }

    /**
     * Equity check by the database id
     * @param o the license to comapre with
     * @return true if the database ids match, false otherwise
     */
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

    /**
     * Gets a string representation of this license
     * @return the license's name, if it is set, the string "License" and the database id otherwise
     */
  @Override
  String toString() {
    reference ? "${reference}" : "License ${id}"
  }

    /**
     * Compares this license to another license by the database id
     * @param other the license to compare with
     * @return the comparison result (-1, 0 or 1), if the other instance lacks an id, -1 is being returned
     */
  @Override
  int compareTo(License other){
      return other.id? other.id.compareTo(this.id) : -1
  }

    /**
     * Gets all member licenses of this consortia license
     * @return a {@link List} of licenses who are children of this license
     */
    def getNonDeletedDerivedLicenses() {
        License.where{ instanceOf == this }
    }

    /**
     * Retrieves all property definition groups that the given institution has defined for this license
     * @param contextOrg the {@link Org} whose property definition groups should be retrieved
     * @return a {@link Map} of {@link PropertyDefinitionGroup}s; ordered by sorted, global, local or orphaned ones
     */
    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        BeanStore.getPropertyService().getCalculatedPropDefGroups(this, contextOrg)
    }

    /**
     * Normalises the given name (i.e. removal of stopwords, elimination of special characters etc.) to make the license sortable
     * @param input_title the name to normalise
     * @return the normalised name string
     */
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

    /**
     * Concatenates the license record for dropdown menu entries
     * @return a concatenated string containing license name, status, running period and consortium or member
     */
    String dropdownNamingConvention() {
        String statusString = "" + status ? status.getI10n('value') : RDStore.LICENSE_NO_STATUS.getI10n('value')

        SimpleDateFormat sdf = DateUtils.getLocalizedSDF_noTime()
        String period = startDate ? sdf.format(startDate) : ''
        period = endDate ? period + ' - ' + sdf.format(endDate) : ''
        period = period ? '(' + period + ')' : ''

        String result = ''
        result += reference + " - " + statusString + " " + period
        if (CalculatedType.TYPE_PARTICIPATION == _getCalculatedType()) {
            result += " - " + BeanStore.getMessageSource().getMessage('license.member', null, LocaleUtils.getCurrentLocale())
        }

        return result
    }

    /**
     * Retrieves all linked subscriptions to this license to which the context institution has access
     * @return a {@link Set} of {@link Subscription}s connected to this license
     */
    Set<Subscription> getSubscriptions(Org institution) {
        Set<Subscription> result = Subscription.executeQuery("select s from Links li join li.destinationSubscription s join s.orgRelations oo where li.sourceLicense = :license and li.linkType = :linkType and oo.org = :institution",[institution: institution, license:this,linkType:RDStore.LINKTYPE_LICENSE])
        /*Links.findAllBySourceAndSourceTypeAndDestinationTypeAndLinkType(genericOIDService.getOID(this),RDStore.LINKTYPE_LICENSE).each { l ->
            result << genericOIDService.resolveOID(l.destination)
        }*/
        result
    }
}
