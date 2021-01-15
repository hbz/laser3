package de.laser

import com.k_int.kbplus.GenericOIDService
import de.laser.auth.Perm
import de.laser.auth.PermGrant
import de.laser.auth.Role
import de.laser.auth.User
import de.laser.auth.UserOrg
import de.laser.finance.CostItem
import de.laser.properties.OrgProperty
import de.laser.properties.PropertyDefinitionGroup
import de.laser.properties.PropertyDefinitionGroupBinding
import de.laser.oap.OrgAccessPoint
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.annotations.RefdataAnnotation
import de.laser.interfaces.DeleteFlag
import grails.util.Holders
import groovy.util.logging.Slf4j
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import grails.web.servlet.mvc.GrailsParameterMap

@Slf4j
class Org extends AbstractBaseWithCalculatedLastUpdated
        implements DeleteFlag {

    def sessionFactory // TODO: ugliest HOTFIX ever
    def contextService
    def organisationService
    def accessService
	def propertyService
    def deletionService

    static Log static_logger = LogFactory.getLog(Org)

    String name
    String shortname
    String shortcode            // Used to generate friendly semantic URLs
    String sortname
    String legalPatronName
    String url
    String urlGov
    SortedSet subjectGroup

    String importSource         // "nationallizenzen.de", "edb des hbz"
    Date lastImportDate

    String gokbId
    String comment
    String ipRange
    String scope

    Org createdBy
    Org legallyObligedBy
    String categoryId

    boolean eInvoice = false

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    @RefdataAnnotation(cat = RDConstants.ORG_SECTOR)
    RefdataValue sector

    @RefdataAnnotation(cat = RDConstants.ORG_STATUS)
    RefdataValue status

    @RefdataAnnotation(cat = '?')
    RefdataValue membership

    @RefdataAnnotation(cat = RDConstants.COUNTRY)
    RefdataValue country

    @RefdataAnnotation(cat = '?')
    RefdataValue region

    @RefdataAnnotation(cat = RDConstants.LIBRARY_NETWORK)
    RefdataValue libraryNetwork

    @RefdataAnnotation(cat = RDConstants.FUNDER_TYPE)
    RefdataValue funderType

    @RefdataAnnotation(cat = RDConstants.FUNDER_HSK_TYPE)
    RefdataValue funderHskType

    @RefdataAnnotation(cat = RDConstants.LIBRARY_TYPE)
    RefdataValue libraryType

    @RefdataAnnotation(cat = RDConstants.COST_CONFIGURATION)
    RefdataValue costConfigurationPreset

    @RefdataAnnotation(cat = RDConstants.E_INVOICE_PORTAL)
    RefdataValue eInvoicePortal

    Set ids = []

    static transients = [
            'deleted', 'customerType', 'customerTypeI10n', 'designation',
            'calculatedPropDefGroups', 'empty', 'consortiaMember', 'department'
    ] // mark read-only accessor methods

    static mappedBy = [
        ids:                'org',
        outgoingCombos:     'fromOrg',
        incomingCombos:     'toOrg',
        links:              'org',
        prsLinks:           'org',
        contacts:           'org',
        addresses:          'org',
        affiliations:       'org',
        propertySet:        'owner',
        //privateProperties:  'owner',
        documents:          'org',
        hasCreated:         'createdBy',
        hasLegallyObliged:  'legallyObligedBy'
    ]

    static hasMany = [
        ids:                Identifier,
        subjectGroup:       OrgSubjectGroup,
        outgoingCombos:     Combo,
        incomingCombos:     Combo,
        links:              OrgRole,
        prsLinks:           PersonRole,
        contacts:           Contact,
        addresses:          Address,
        affiliations:       UserOrg,
        propertySet:        OrgProperty,
        //privateProperties:  OrgPrivateProperty,
        orgType:            RefdataValue,
        documents:          DocContext,
        platforms:          Platform,
        hasCreated:         Org,
        hasLegallyObliged:  Org,
        accessPoints:   OrgAccessPoint
    ]

    static mapping = {
                cache true
                sort 'sortname'
                id          column:'org_id'
           version          column:'org_version'
         globalUID          column:'org_guid'
              name          column:'org_name',      index:'org_name_idx'
         shortname          column:'org_shortname', index:'org_shortname_idx'
          sortname          column:'org_sortname',  index:'org_sortname_idx'
   legalPatronName          column:'org_legal_patronname'
               url          column:'org_url'
            urlGov          column:'org_url_gov'
    //subjectGroup          column:'org_subject_group'
   //originEditUrl          column:'org_origin_edit_url'
           comment          column:'org_comment'
           ipRange          column:'org_ip_range'
         shortcode          column:'org_shortcode', index:'org_shortcode_idx'
             scope          column:'org_scope'
        categoryId          column:'org_cat'
        eInvoice            column:'org_e_invoice'
        eInvoicePortal      column:'org_e_invoice_portal_fk'
        gokbId              column:'org_gokb_id', type:'text'
            sector          column:'org_sector_rv_fk', lazy: false
            status          column:'org_status_rv_fk'
        membership          column:'org_membership'
           country          column:'org_country_rv_fk'
            region          column:'org_region_rv_fk'
    libraryNetwork          column:'org_library_network_rv_fk'
        funderType          column:'org_funder_type_rv_fk'
     funderHskType          column:'org_funder_hsk_type_rv_fk'
       libraryType          column:'org_library_type_rv_fk'
      importSource          column:'org_import_source'
    lastImportDate          column:'org_last_import_date'
       dateCreated          column:'org_date_created'
       lastUpdated          column:'org_last_updated'
        createdBy           column:'org_created_by_fk'
        legallyObligedBy    column:'org_legally_obliged_by_fk'
    costConfigurationPreset column:'org_config_preset_rv_fk'
       lastUpdatedCascading column:'org_last_updated_cascading'

        orgType             joinTable: [
                name:   'org_type',
                key:    'org_id',
                column: 'refdata_value_id', type:   'BIGINT'
        ], lazy: false

        ids                 batchSize: 10
        outgoingCombos      batchSize: 10
        incomingCombos      batchSize: 10
        links               batchSize: 10
        prsLinks            batchSize: 10
        affiliations        batchSize: 10
        propertySet    batchSize: 10
        //privateProperties   batchSize: 10
        documents           batchSize: 10
        platforms           sort:'name', order:'asc', batchSize: 10
        hasCreated          batchSize: 10
        hasLegallyObliged   batchSize: 10
    }

    static constraints = {
           globalUID(nullable:true, blank:false, unique:true, maxSize:255)
                name(blank:false, maxSize:255)
           shortname(nullable:true, blank:true, maxSize:255)
            sortname(nullable:true, blank:true, maxSize:255)
     legalPatronName(nullable:true, blank:true, maxSize:255)
                 url(nullable:true, blank:true, maxSize:512)
              urlGov(nullable:true, blank:true, maxSize:512)
      //subjectGroup(nullable:true)
     //originEditUrl(nullable:true, blank:false)
             comment(nullable:true, blank:true, maxSize:2048)
             ipRange(nullable:true, blank:true, maxSize:1024)
              sector(nullable:true)
           shortcode(nullable:true, blank:true, maxSize:128)
               scope(nullable:true, blank:true, maxSize:128)
          categoryId(nullable:true, blank:true, maxSize:128)
              status(nullable:true)
          membership(nullable:true)
             country(nullable:true)
              region(nullable:true)
            eInvoicePortal(nullable:true)
//        , validator: {RefdataValue val, Org obj, errors ->
//                  if ( ! val.owner.desc.endsWith(obj.country.toString().toLowerCase())){
//                      errors.rejectValue('region', 'regionDoesNotBelongToSelectedCountry')
//                      return false
//                  }
//              })
      libraryNetwork(nullable:true)
          funderType(nullable:true)
       funderHskType(nullable:true)
         libraryType(nullable:true)
        importSource(nullable:true, blank:true)
      lastImportDate(nullable:true)
           createdBy(nullable:true)
    legallyObligedBy(nullable:true)
      costConfigurationPreset(nullable:true)
             orgType(nullable:true)
             gokbId (nullable:true, blank:true)
        lastUpdatedCascading (nullable: true)
    }

    @Override
    boolean isDeleted() {
        return RDStore.ORG_STATUS_DELETED.id == status?.id
    }

    @Override
    def beforeInsert() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name);
        }

        //ugliest HOTFIX ever #2
        if(!Thread.currentThread().name.contains("Sync")) {
            if (contextService.getOrg()) {
                createdBy = contextService.getOrg()
            }
        }

        super.beforeInsertHandler()
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

    boolean setDefaultCustomerType() {
        def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)

        if (oss == OrgSetting.SETTING_NOT_FOUND) {
            log.debug ('Setting default customer type for org: ' + this.id)
            OrgSetting.add(this, OrgSetting.KEYS.CUSTOMER_TYPE, Role.findByAuthorityAndRoleType('ORG_BASIC_MEMBER', 'org'))
            return true
        }

        false
    }

    String getCustomerType() {
        String result

        def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSetting.SETTING_NOT_FOUND) {
            result = oss.roleValue?.authority
        }
        result
    }
    String getCustomerTypeI10n() {
        String result

        def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSetting.SETTING_NOT_FOUND) {
            result = oss.roleValue?.getI10n('authority')
        }
        result
    }

    /*
	    gets OrgSetting
	    creating new one (with value) if not existing
     */
    OrgSetting getSetting(OrgSetting.KEYS key, def defaultValue) {
        def os = OrgSetting.get(this, key)
        (os == OrgSetting.SETTING_NOT_FOUND) ? OrgSetting.add(this, key, defaultValue) : (OrgSetting) os
    }

    /*
        gets VALUE of OrgSetting
        creating new OrgSetting (with value) if not existing
     */
    def getSettingsValue(OrgSetting.KEYS key, def defaultValue) {
        OrgSetting setting = getSetting(key, defaultValue)
        setting.getValue()
    }

    /*
        gets VALUE of OrgSetting
        creating new OrgSetting if not existing
     */
    def getSettingsValue(OrgSetting.KEYS key) {
        getSettingsValue(key, null)
    }

    @Override
    def beforeUpdate() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name);
        }
        super.beforeUpdateHandler()
    }

    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    static String generateShortcodeFunction(name) {
        return StringUtils.left(name.trim().replaceAll(" ","_"), 128) // FIX
    }

    String generateShortcode(name) {
        String candidate = Org.generateShortcodeFunction(name)
        return incUntilUnique(candidate);
    }

    String incUntilUnique(name) {
        String result = name
        if ( Org.findByShortcode(result) ) {
            // There is already a shortcode for that identfier
            int i = 2;
            while ( Org.findByShortcode("${name}_${i}") ) {
                i++
            }
            result = "${name}_${i}"
        }
        result
    }

    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        propertyService.getCalculatedPropDefGroups(this, contextOrg)
    }

    Identifier getIdentifierByType(String idtype) {
        Identifier result

        List<Identifier> test = getIdentifiersByType(idtype)
        if (test.size() > 0) {
            result = test.get(0)  // TODO refactoring: multiple occurrences
        }
        result
    }

    List<User> getAllValidInstAdmins() {
        List<User> admins = User.executeQuery(
                "select u from User u join u.affiliations uo where " +
                        "uo.org = :org and uo.formalRole = :role and uo.status = :approved and u.enabled = true",
                [
                        org: this,
                        role: Role.findByAuthority('INST_ADM'),
                        approved: UserOrg.STATUS_APPROVED
                ]
        )
        admins
    }

    List<Identifier> getIdentifiersByType(String idtype) {

        Identifier.executeQuery(
                'select id from Identifier id join id.ns ns where id.org = :org and lower(ns.ns) = :idtype',
                [org: this, idtype: idtype.toLowerCase()]
        )
    }

    static def refdataFind(GrailsParameterMap params) {
        GenericOIDService genericOIDService = (GenericOIDService) Holders.grailsApplication.mainContext.getBean('genericOIDService')

        genericOIDService.getOIDMapList( Org.findAllByNameIlike("%${params.q}%", params), 'name' )
    }

    // called from AjaxController.resolveOID2()
  static Org refdataCreate(value) {
    return new Org(name:value)
  }

    String getDesignation() {
        String ret = ""
        Org hasDept = Combo.findByFromOrgAndType(this,RDStore.COMBO_TYPE_DEPARTMENT)?.toOrg
        if(hasDept)
            ret = "${hasDept.shortname?:(hasDept.sortname?: hasDept.name)} – "
        ret += shortname?:(sortname?:(name?:(globalUID?:id)))
        return ret
    }

    boolean isEmpty() {
        Map deptParams = [department:this,current:RDStore.SUBSCRIPTION_CURRENT]
        //verification a: check if department has cost items
        List costItems = CostItem.executeQuery('select ci from CostItem ci join ci.sub sub join sub.orgRelations orgRoles where orgRoles.org = :department and sub.status = :current',deptParams)
        if(costItems)
            return false
        //verification b: check if department has current subscriptions
        List currentSubscriptions = Subscription.executeQuery('select s from Subscription s join s.orgRelations orgRoles where orgRoles.org = :department and s.status = :current',deptParams)
        if(currentSubscriptions)
            return false
        //verification c: check if department has active users
        UserOrg activeUsers = UserOrg.findByOrg(this)
        if(activeUsers)
            return false
        return true
    }

    @Override
    String toString() {
        //sector ? name + ', ' + sector?.getI10n('value') : "${name}"
        name
    }

    List<Person> getGeneralContactPersons(boolean onlyPublic) {

        if (onlyPublic) {
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org and pr.functionType = :gcp",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
            )
        }
        else {
            Org ctxOrg = contextService.getOrg()
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where pr.org = :org and pr.functionType = :gcp " +
                    " and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, ctx: ctxOrg]
            )
        }
    }

    List<Person> getPublicPersons() {
        Person.executeQuery(
                "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org",
                [org: this]
        )
    }

    List<Person> getContactPersonsByFunctionType(boolean onlyPublic, RefdataValue functionType) {

        if (onlyPublic) {
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic = true and pr.org = :org and pr.functionType = :functionType",
                    [org: this, functionType: functionType]
            )
        }
        else {
            Org ctxOrg = contextService.getOrg()
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where pr.org = :org and pr.functionType = :functionType " +
                            " and ( (p.isPublic = false and p.tenant = :ctx) or (p.isPublic = true) )",
                    [org: this, functionType: functionType, ctx: ctxOrg]
            )
        }
    }

    List<RefdataValue> getAllOrgTypes() {
        RefdataValue.executeQuery("select ot from Org org join org.orgType ot where org = :org", [org: this])
    }

    List getAllOrgTypeIds() {
        RefdataValue.executeQuery("select ot.id from Org org join org.orgType ot where org = :org", [org: this])
    }

    boolean isInComboOfType(RefdataValue comboType) {
        if(Combo.findByFromOrgAndType(this, comboType))
            return true
        return false
    }

    boolean isConsortiaMember() {
        isInComboOfType(RDStore.COMBO_TYPE_CONSORTIUM)
    }

    boolean isDepartment() {
        isInComboOfType(RDStore.COMBO_TYPE_DEPARTMENT) && !hasPerm("ORG_INST")
    }
    void createCoreIdentifiersIfNotExist(){
        if(!Combo.findByFromOrgAndType(this, RDStore.COMBO_TYPE_DEPARTMENT) && !(RDStore.OT_PROVIDER.id in this.getAllOrgTypeIds())){

            boolean isChanged = false
            IdentifierNamespace.CORE_ORG_NS.each{ coreNs ->
                if ( ! ids.find {it.ns?.ns == coreNs}){
                    addOnlySpecialIdentifiers(coreNs, IdentifierNamespace.UNKNOWN)
                    isChanged = true
                }
            }
            if (isChanged) refresh()
        }
    }
    // Only for ISIL, EZB, WIBID
    void addOnlySpecialIdentifiers(String ns, String value) {
        boolean found = false
        this.ids.each {
            if ( it?.ns?.ns == ns && it.value == value ) {
                found = true
            }
        }

        if ( !found && value != '') {
            value = value?.trim()
            ns = ns?.trim()
            //def namespace = IdentifierNamespace.findByNsIlike(ns) ?: new IdentifierNamespace(ns:ns).save()
            // TODO [ticket=1789]
            Identifier ident = Identifier.construct([value: value, reference: this, namespace: ns, nsType: Org.class.name])
            //def id = new Identifier(ns:namespace, value:value).save()
            //new IdentifierOccurrence(identifier: id, org: this).save()
            log.debug("Create new identifier: ${ident.getId()} ns:${ns} value:${value}")
        }
    }

    //Only INST_ADM
    boolean hasAccessOrg(){
        if (UserOrg.findAllByOrgAndStatusAndFormalRole(this, UserOrg.STATUS_APPROVED, Role.findByAuthority('INST_ADM'))) {
            return true
        }
        else {
            return false
        }
    }

    Map<String, Object> hasAccessOrgListUser(){

        Map<String, Object> result = [:]

        result.instAdms = UserOrg.findAllByOrgAndStatusAndFormalRole(this, UserOrg.STATUS_APPROVED, Role.findByAuthority('INST_ADM'))
        result.instEditors = UserOrg.findAllByOrgAndStatusAndFormalRole(this, UserOrg.STATUS_APPROVED, Role.findByAuthority('INST_EDITOR'))
        result.instUsers = UserOrg.findAllByOrgAndStatusAndFormalRole(this, UserOrg.STATUS_APPROVED, Role.findByAuthority('INST_USER'))

        return result
    }

    // copied from AccessService
    // private boolean checkOrgPerm(String[] orgPerms) {}
    boolean hasPerm(String perms) {
        boolean check = false

        if (perms) {
            def oss = OrgSetting.get(this, OrgSetting.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSetting.SETTING_NOT_FOUND) {
                perms.split(',').each { perm ->
                    check = check || PermGrant.findByPermAndRole(Perm.findByCode(perm.toLowerCase()?.trim()), (Role) oss.getValue())
                }
            }
        }
        else {
            check = true
        }
        check
    }

    String dropdownNamingConvention() {
        return dropdownNamingConvention(contextService.getOrg())
    }

    String dropdownNamingConvention(Org contextOrg){
        String result = ''
        if (contextOrg.getCustomerType() in ['ORG_BASIC_MEMBER','ORG_INST','ORG_INST_COLLECTIVE']){
            if (name) {
                result += name
            }
            if (shortname){
                result += ' (' + shortname + ')'
            }
        } else {
            if (sortname) {
                result += sortname
            }
            if (name) {
                result += ' (' + name + ')'
            }
        }
        result
    }

    Identifier getLeitID() {
        return Identifier.findByOrgAndNs(this, IdentifierNamespace.findByNs(IdentifierNamespace.LEIT_ID))
    }

}
