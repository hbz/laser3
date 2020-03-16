package com.k_int.kbplus


import com.k_int.kbplus.auth.Perm
import com.k_int.kbplus.auth.PermGrant
import com.k_int.kbplus.auth.Role
import com.k_int.kbplus.auth.User
import com.k_int.kbplus.auth.UserOrg
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.DeleteFlag
import grails.util.Holders
import groovy.util.logging.Log4j
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.web.util.WebUtils
import org.hibernate.AssertionFailure

import javax.persistence.Transient
import java.text.SimpleDateFormat

@Log4j
class Org
        extends AbstractBaseDomain
        implements DeleteFlag {

    @Transient
    def sessionFactory // TODO: ugliest HOTFIX ever
    @Transient
    def contextService
    def organisationService
    @Transient
    def accessService
	@Transient
	def propertyService
    @Transient
    def deletionService

    String name
    String shortname
    String shortcode            // Used to generate friendly semantic URLs
    String sortname
    String legalPatronName
    String url
    String urlGov
    //URL originEditUrl

    String importSource         // "nationallizenzen.de", "edb des hbz"
    Date lastImportDate

    String gokbId
    String comment
    String ipRange
    String scope
    Date dateCreated
    Date lastUpdated
    Org createdBy
    Org legallyObligedBy
    String categoryId

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

    @RefdataAnnotation(cat = RDConstants.FEDERAL_STATE)
    RefdataValue federalState

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

    Set ids = []

    static mappedBy = [
        ids:                'org',
        outgoingCombos:     'fromOrg',
        incomingCombos:     'toOrg',
        links:              'org',
        prsLinks:           'org',
        contacts:           'org',
        addresses:          'org',
        affiliations:       'org',
        customProperties:   'owner',
        privateProperties:  'owner',
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
        customProperties:   OrgCustomProperty,
        privateProperties:  OrgPrivateProperty,
        orgType:            RefdataValue,
        documents:          DocContext,
        platforms:          Platform,
        hasCreated:         Org,
        hasLegallyObliged:  Org
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
      subjectGroup          column:'org_subject_group'
   //originEditUrl          column:'org_origin_edit_url'
           comment          column:'org_comment'
           ipRange          column:'org_ip_range'
         shortcode          column:'org_shortcode', index:'org_shortcode_idx'
             scope          column:'org_scope'
        categoryId          column:'org_cat'
        gokbId              column:'org_gokb_id', type:'text'
            sector          column:'org_sector_rv_fk', lazy: false
            status          column:'org_status_rv_fk'
        membership          column:'org_membership'
           country          column:'org_country_rv_fk'
            region          column:'org_region_rv_fk'
      federalState          column:'org_federal_state_rv_fk'
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
        customProperties    batchSize: 10
        privateProperties   batchSize: 10
        documents           batchSize: 10
        platforms           batchSize: 10
        hasCreated          batchSize: 10
        hasLegallyObliged   batchSize: 10
    }

    static constraints = {
           globalUID(nullable:true, blank:false, unique:true, maxSize:255)
                name(nullable:false, blank:false, maxSize:255)
           shortname(nullable:true, blank:true, maxSize:255)
            sortname(nullable:true, blank:true, maxSize:255)
     legalPatronName(nullable:true, blank:true, maxSize:255)
                 url(nullable:true, blank:true, maxSize:512)
              urlGov(nullable:true, blank:true, maxSize:512)
        subjectGroup(nullable:true, blank: true)
     //originEditUrl(nullable:true, blank:false)
             comment(nullable:true, blank:true, maxSize:2048)
             ipRange(nullable:true, blank:true, maxSize:1024)
              sector(nullable:true, blank:true)
           shortcode(nullable:true, blank:true, maxSize:128)
               scope(nullable:true, blank:true, maxSize:128)
          categoryId(nullable:true, blank:true, maxSize:128)
              status(nullable:true, blank:true)
          membership(nullable:true, blank:true, maxSize:128)
             country(nullable:true, blank:true)
              region(nullable:true, blank:true)
//        , validator: {RefdataValue val, Org obj, errors ->
//                  if ( ! val.owner.desc.endsWith(obj.country.toString().toLowerCase())){
//                      errors.rejectValue('region', 'regionDoesNotBelongToSelectedCountry')
//                      return false
//                  }
//              })
        federalState(nullable:true, blank:true)
      libraryNetwork(nullable:true, blank:true)
          funderType(nullable:true, blank:true)
       funderHskType(nullable:true, blank:true)
         libraryType(nullable:true, blank:true)
        importSource(nullable:true, blank:true)
      lastImportDate(nullable:true, blank:true)
           createdBy(nullable:true, blank:true)
    legallyObligedBy(nullable:true, blank:true)
      costConfigurationPreset(nullable:true, blank:false)
             orgType(nullable:true, blank:true)
             gokbId (nullable:true, blank:true)
    }

    /*
    // ERMS-1497
    List<Combo> getIncomingCombos() {
        Combo.executeQuery('SELECT c FROM Combo c WHERE c.toOrg = :org AND c.status = :active',
                [org: this, active: COMBO_STATUS_ACTIVE])
    }

    // ERMS-1497
    List<Combo> getOutgoingCombos() {
        Combo.executeQuery('SELECT c FROM Combo c WHERE c.fromOrg = :org AND c.status = :active',
                [org: this, active: COMBO_STATUS_ACTIVE])
    }
    */

    def afterDelete() {
        deletionService.deleteDocumentFromIndex(this.globalUID)
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

        super.beforeInsert()
    }

    boolean setDefaultCustomerType() {
        def oss = OrgSettings.get(this, OrgSettings.KEYS.CUSTOMER_TYPE)

        if (oss == OrgSettings.SETTING_NOT_FOUND) {
            log.debug ('Setting default customer type for org: ' + this.id)
            OrgSettings.add(this, OrgSettings.KEYS.CUSTOMER_TYPE, Role.findByAuthorityAndRoleType('ORG_BASIC_MEMBER', 'org'))
            return true
        }

        false
    }

    String getCustomerType() {
        String result

        def oss = OrgSettings.get(this, OrgSettings.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSettings.SETTING_NOT_FOUND) {
            result = oss.roleValue?.authority
        }
        result
    }
    String getCustomerTypeI10n() {
        String result

        def oss = OrgSettings.get(this, OrgSettings.KEYS.CUSTOMER_TYPE)

        if (oss != OrgSettings.SETTING_NOT_FOUND) {
            result = oss.roleValue?.getI10n('authority')
        }
        result
    }

    /*
	    gets OrgSettings
	    creating new one (with value) if not existing
     */
    def getSetting(OrgSettings.KEYS key, def defaultValue) {
        def os = OrgSettings.get(this, key)
        (os == OrgSettings.SETTING_NOT_FOUND) ? OrgSettings.add(this, key, defaultValue) : os
    }

    /*
        gets VALUE of OrgSettings
        creating new OrgSettings (with value) if not existing
     */
    def getSettingsValue(OrgSettings.KEYS key, def defaultValue) {
        def setting = getSetting(key, defaultValue)
        setting.getValue()
    }

    /*
        gets VALUE of OrgSettings
        creating new OrgSettings if not existing
     */
    def getSettingsValue(OrgSettings.KEYS key) {
        getSettingsValue(key, null)
    }

    @Override
    def beforeUpdate() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name);
        }
        super.beforeUpdate()
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

    static def lookupByIdentifierString(idstr) {
        LogFactory.getLog(this).debug("lookupByIdentifierString(${idstr})")

        def result = null
        def qr = Identifier.lookupObjectsByIdentifierString(new Org(), idstr)

        if (qr && (qr.size() == 1)) {
            //result = qr.get(0);
            result = GrailsHibernateUtil.unwrapIfProxy( qr.get(0) ) // fix: unwrap proxy
        }
        result
    }

    Map<String, Object> getCalculatedPropDefGroups(Org contextOrg) {
        Map<String, Object> result = [ 'global':[], 'local':[], 'orphanedProperties':[] ]

        // ALL type depending groups without checking tenants or bindings
        List<PropertyDefinitionGroup> groups = PropertyDefinitionGroup.findAllByOwnerType(Org.class.name)
        groups.each{ it ->

            PropertyDefinitionGroupBinding binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(it, this)

            if (it.tenant == null || it.tenant?.id == contextOrg?.id) {
                if (binding) {
                    result.local << [it, binding]
                } else {
                    result.global << it
                }
            }
        }

        // storing properties without groups
        result.orphanedProperties = propertyService.getOrphanedProperties(this, result.global, result.local, [])

        result
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

  static def refdataFind(params) {
    def result = [];
    List<Org> ql = Org.findAllByNameIlike("%${params.q}%",params)

    if ( ql ) {
      ql.each { id ->
        result.add([id:"${id.class.name}:${id.id}",text:"${id.name}"])
      }
    }

    result
  }

    // called from AjaxController.resolveOID2()
  static Org refdataCreate(value) {
    return new Org(name:value)
  }

    /*
  static def lookup(name, identifiers, def uuid = null) {

      def result = []

      if (uuid?.size() > 0) {
        def oldUuid_match = Org.findByImpId(uuid)

        def newUuid_match = Org.findByGokbId(uuid)

        if(newUuid_match) {
          result << newUuid_match
        }else {
            if(oldUuid_match) {
                result << oldUuid_match
            }
        }
      }

      if(!result) {
        // SUPPORT MULTIPLE IDENTIFIERS
        if (identifiers instanceof ArrayList) {
            identifiers.each { it ->
                it.each { k, v ->
                    if (v != null) {
                        def o = Org.executeQuery("select o from Org as o join o.ids as ident where ident.ns.ns = ? and ident.value = ?", [k, v])

                        if (o.size() > 0) result << o[0]
                    }
                }
            }
        }
        // DEFAULT LOGIC
        else {
            // See if we can uniquely match on any of the identifiers
            identifiers.each { k, v ->
                if (v != null) {
                    def o = Org.executeQuery("select o from Org as o join o.ids as ident where ident.ns.ns = ? and ident.value = ?", [k, v])

                    if (o.size() > 0) result << o[0]
                }
            }
        }
      }

      // No match by identifier, try and match by name
      if (! result) {
          // log.debug("Match by name ${name}");
          def o = Org.executeQuery("select o from Org as o where lower(o.name) = ?", [name.toLowerCase()])

          if (o.size() > 0) result << o[0]
      }

      result.isEmpty() ? null : result.get(0)
    }
     */

    /*
    static def lookupOrCreate(name, sector, consortium, identifiers, iprange, def imp_uuid = null) {
        lookupOrCreate2(name, sector, consortium, identifiers, iprange, null, imp_uuid)
    }*/

    /*
    static def lookupOrCreate2(name, sector, consortium, identifiers, iprange, orgRoleTyp, def imp_uuid = null) {

        if(imp_uuid?.size() == 0) {
          imp_uuid = null
        }

        println "before org lookup"
        def result = Org.lookup(name, identifiers, imp_uuid)

        if ( result == null ) {
          println "Create new entry for ${name}";
          if (sector instanceof String){
            sector = RefdataValue.getByValueAndCategory(sector,RDConstants.ORG_SECTOR)
          }

          if (orgRoleTyp instanceof String) {
             orgRoleTyp = RefdataValue.getByValueAndCategory(orgRoleTyp, RDConstants.ORG_TYPE)
          }
            println "creating new org"
          result = new Org(
                           name:name,
                           sector:sector,
                           ipRange:iprange,
                           impId: null,
                           gokbId: imp_uuid?.length() > 0 ? imp_uuid : null).save()
          if(orgRoleTyp) {
              result.addToOrgType(orgRoleTyp).save()
          }

            // SUPPORT MULTIPLE IDENTIFIERS
            if (identifiers instanceof ArrayList) {
                identifiers.each{ it ->
                    it.each { k, v ->
                        // TODO [ticket=1789]
                        if(k.toLowerCase() != 'originediturl') {
                            //def io = new IdentifierOccurrence(org: result, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                            Identifier ident = Identifier.construct([value: v, reference: result, namespace: k])
                        }
                        else println "org identifier ${v} is deprecated namespace originEditUrl .. ignoring"
                    }
                }
            }
            // DEFAULT LOGIC
            else {
                identifiers.each { k, v ->
                    // TODO [ticket=1789]
                    if(k.toLowerCase() != 'originediturl') {
                        //def io = new IdentifierOccurrence(org: result, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                        Identifier ident = Identifier.construct([value: v, reference: result, namespace: k])
                    }
                    else println "org identifier ${v} is deprecated namespace originEditUrl .. ignoring"
                }
            }

          if ( ( consortium != null ) && ( consortium.length() > 0 ) ) {
            def db_consortium = Org.lookupOrCreate(consortium, null, null, [:], null)
            def consLink = new Combo(fromOrg:result,
                                     toOrg:db_consortium,
                                     status:null,
                                     type: RDStore.COMBO_TYPE_CONSORTIUM
            ).save()
          }
        } else if (Holders.config.globalDataSync.replaceLocalImpIds.Org && result && imp_uuid && imp_uuid != result.gokbId){
          result.gokbId = imp_uuid
          result.impId = imp_uuid
          result.save()
        }
        else {
            result.name = name
            result.save()
        }
        println "org lookup end"
        result
    }*/

  @Transient
  static def oaiConfig = [
    id:'orgs',
    textDescription:'Org repository for KBPlus',
    query:" from Org as o ",
    pageSize:20
  ]

  /**
   *  Render this title as OAI_dc
   */
  @Transient
  def toOaiDcXml(builder, attr) {
    builder.'dc'(attr) {
      'dc:title' (name)
    }
  }

  /**
   *  Render this Title as KBPlusXML
   */
  @Transient
  def toKBPlus(builder, attr) {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    def pub = getPublisher()

    try {
      builder.'kbplus' (attr) {
        builder.'org' (['id':(id)]) {
          builder.'name' (name)
        }
        builder.'identifiers' () {
          ids?.each { id_oc ->
            builder.identifier([namespace:id_oc.ns.ns, value:id_oc.value])
          }
        }
      }
    }
    catch ( Exception e ) {
      log.error(e);
    }

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

    def getallOrgType()
    {
        def result = []
        getallOrgTypeIds()?.each { it ->
                result << RefdataValue.get(it)
        }
        result
    }

    List getallOrgTypeIds()
    {
        orgType.findAll{it}.collect{it.id}
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
            Identifier ident = Identifier.construct([value: value, reference: this, namespace: ns])
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
            def oss = OrgSettings.get(this, OrgSettings.KEYS.CUSTOMER_TYPE)
            if (oss != OrgSettings.SETTING_NOT_FOUND) {
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

    String dropdownNamingConvention(Org contextOrg){
        String result = ''
        if (RDStore.OT_INSTITUTION == contextOrg?.getCustomerType()){
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

}
