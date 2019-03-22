package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import com.k_int.properties.PropertyDefinitionGroup
import com.k_int.properties.PropertyDefinitionGroupBinding
import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.interfaces.DeleteFlag
import groovy.sql.Sql
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import groovy.util.logging.*
//import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.Criteria
import javax.persistence.Transient
import grails.util.Holders

@Log4j
class Org
        extends AbstractBaseDomain
        implements DeleteFlag {

    @Transient
    def sessionFactory // TODO: ugliest HOTFIX ever
    @Transient
    def contextService

    String name
    String shortname
    String shortcode            // Used to generate friendly semantic URLs
    String sortname
    String url
    String urlGov

    String importSource         // "nationallizenzen.de", "edb des hbz"
    Date lastImportDate

    String impId
    String gokbId
    String comment
    String ipRange
    String scope
    Date dateCreated
    Date lastUpdated
    String categoryId

    int fteStudents
    int fteStaff

    @RefdataAnnotation(cat = '?')
    RefdataValue sector

    @RefdataAnnotation(cat = 'OrgStatus')
    RefdataValue status

    @RefdataAnnotation(cat = '?')
    RefdataValue membership

    @RefdataAnnotation(cat = 'Country')
    RefdataValue country

    @RefdataAnnotation(cat = 'Federal State')
    RefdataValue federalState

    @RefdataAnnotation(cat = 'Library Network')
    RefdataValue libraryNetwork

    @RefdataAnnotation(cat = 'Funder Type')
    RefdataValue funderType

    @RefdataAnnotation(cat = 'Library Type')
    RefdataValue libraryType

    @RefdataAnnotation(cat = 'Cost configuration')
    RefdataValue costConfigurationPreset

    Set ids = []

    @Transient
    def documents

    static mappedBy = [
        ids:              'org',
        outgoingCombos:   'fromOrg',
        incomingCombos:   'toOrg',
        links:            'org',
        prsLinks:         'org',
        contacts:         'org',
        addresses:        'org',
        affiliations:     'org',
        customProperties: 'owner',
        privateProperties:'owner',
        documents:        'org'
    ]

    static hasMany = [
        ids:                IdentifierOccurrence,
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
        documents:          DocContext
    ]

    static mapping = {
                sort 'sortname'
                id          column:'org_id'
           version          column:'org_version'
         globalUID          column:'org_guid'
             impId          column:'org_imp_id', index:'org_imp_id_idx'
              name          column:'org_name', index:'org_name_idx'
         shortname          column:'org_shortname', index:'org_shortname_idx'
          sortname          column:'org_sortname', index:'org_sortname_idx'
               url          column:'org_url'
            urlGov          column:'org_url_gov'
       fteStudents          column:'org_fte_students'
          fteStaff          column:'org_fte_staff'
           comment          column:'org_comment'
           ipRange          column:'org_ip_range'
         shortcode          column:'org_shortcode', index:'org_shortcode_idx'
             scope          column:'org_scope'
        categoryId          column:'org_cat'
        gokbId              column:'org_gokb_id', type:'text'
            sector          column:'org_sector_rv_fk'
            status          column:'org_status_rv_fk'
        membership          column:'org_membership'
           country          column:'org_country_rv_fk'
      federalState          column:'org_federal_state_rv_fk'
    libraryNetwork          column:'org_library_network_rv_fk'
        funderType          column:'org_funder_type_rv_fk'
       libraryType          column:'org_library_type_rv_fk'
      importSource          column:'org_import_source'
    lastImportDate          column:'org_last_import_date'
    costConfigurationPreset column:'org_config_preset_rv_fk'

        orgType             joinTable: [
                name:   'org_roletype',
                key:    'org_id',
                column: 'refdata_value_id', type:   'BIGINT'
        ], lazy: false
        addresses   lazy: false
        contacts    lazy: false
    }

    static constraints = {
           globalUID(nullable:true, blank:false, unique:true, maxSize:255)
                name(nullable:true, blank:false, maxSize:255)
           shortname(nullable:true, blank:true, maxSize:255)
            sortname(nullable:true, blank:true, maxSize:255)
                 url(nullable:true, blank:true, maxSize:512)
              urlGov(nullable:true, blank:true, maxSize:512)
         fteStudents(nullable:true, blank:true)
            fteStaff(nullable:true, blank:true)
               impId(nullable:true, blank:true, maxSize:255)
             comment(nullable:true, blank:true, maxSize:2048)
             ipRange(nullable:true, blank:true, maxSize:1024)
              sector(nullable:true, blank:true)
           shortcode(nullable:true, blank:true, maxSize:128)
               scope(nullable:true, blank:true, maxSize:128)
          categoryId(nullable:true, blank:true, maxSize:128)
              status(nullable:true, blank:true)
          membership(nullable:true, blank:true, maxSize:128)
             country(nullable:true, blank:true)
        federalState(nullable:true, blank:true)
      libraryNetwork(nullable:true, blank:true)
          funderType(nullable:true, blank:true)
         libraryType(nullable:true, blank:true)
        importSource(nullable:true, blank:true)
      lastImportDate(nullable:true, blank:true)
      costConfigurationPreset(nullable:true, blank:false)
             orgType(nullable:true, blank:true)
             gokbId (nullable:true, blank:true)
    }

    @Override
    boolean isDeleted() {
        return RDStore.ORG_DELETED.id == status?.id
    }

    @Override
    def beforeInsert() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name);
        }
        
        if (impId == null) {
          impId = java.util.UUID.randomUUID().toString();
        }
        
        super.beforeInsert()
    }

    @Override
    def beforeUpdate() {
        if ( !shortcode ) {
            shortcode = generateShortcode(name);
        }
        super.beforeUpdate()
    }

    static generateShortcodeFunction(name) {
        return StringUtils.left(name.trim().replaceAll(" ","_"), 128) // FIX
    }

    def generateShortcode(name) {
        def candidate = Org.generateShortcodeFunction(name)
        return incUntilUnique(candidate);
    }

    def incUntilUnique(name) {
        def result = name;
        if ( Org.findByShortcode(result) ) {
            // There is already a shortcode for that identfier
            int i = 2;
            while ( Org.findByShortcode("${name}_${i}") ) {
                i++
            }
            result = "${name}_${i}"
        }
        result;
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

    def getCalculatedPropDefGroups(Org contextOrg) {
        def result = [ 'global':[], 'local':[], 'fallback': true, 'orphanedProperties':[] ]

        // ALL type depending groups without checking tenants or bindings
        def groups = PropertyDefinitionGroup.findAllByOwnerType(Org.class.name)
        groups.each{ it ->

            def binding = PropertyDefinitionGroupBinding.findByPropDefGroupAndOrg(it, this)

            if (it.tenant == null || it.tenant?.id == contextOrg?.id) {
                if (binding) {
                    result.local << [it, binding]
                } else {
                    result.global << it
                }
            }
        }

        result.fallback = (result.global.size() == 0 && result.local.size() == 0)

        // storing properties without groups

        def orph = customProperties.id

        result.global.each{ gl -> orph.removeAll(gl.getCurrentProperties(this).id) }
        result.local.each{ lc  -> orph.removeAll(lc[0].getCurrentProperties(this).id) }

        result.orphanedProperties = OrgCustomProperty.findAllByIdInList(orph)

        result
    }

    def getIdentifierByType(String idtype) {
        def result = null

        def test = getIdentifiersByType(idtype)
        if (test.size() > 0) {
            result = test.get(0)  // TODO refactoring: multiple occurrences
        }

      /*
      org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.k_int.kbplus.Org.ids, could not initialize proxy

    ids.each { id ->
      if ( id.identifier.ns.ns.equalsIgnoreCase(idtype) ) {
        result = id.identifier;
      }
    }
    */
    result
  }

    def getIdentifiersByType(String idtype) {
        /*
        org.hibernate.LazyInitializationException: failed to lazily initialize a collection of role: com.k_int.kbplus.Org.ids, could not initialize proxy -

        def result = []
        ids.each { id ->
            if ( id.identifier.ns.ns.equalsIgnoreCase(idtype) ) {
                result << id.identifier;
            }
        }
        */
        def result = Identifier.executeQuery(
                'select id from Identifier id join id.ns ns join id.occurrences oc where oc.org = :org and lower(ns.ns) = :idtype',
                [org: this, idtype: idtype.toLowerCase()]
        )

        result
    }

  static def refdataFind(params) {
    def result = [];
    def ql = null;
    ql = Org.findAllByNameIlike("%${params.q}%",params)

    if ( ql ) {
      ql.each { id ->
        result.add([id:"${id.class.name}:${id.id}",text:"${id.name}"])
      }
    }

    result
  }

  static def refdataCreate(value) {
    return new Org(name:value)
  }

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
                        def o = Org.executeQuery("select o from Org as o join o.ids as io where io.identifier.ns.ns = ? and io.identifier.value = ?", [k, v])

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
                    def o = Org.executeQuery("select o from Org as o join o.ids as io where io.identifier.ns.ns = ? and io.identifier.value = ?", [k, v])

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

    static def lookupOrCreate(name, sector, consortium, identifiers, iprange, def imp_uuid = null) {
        lookupOrCreate2(name, sector, consortium, identifiers, iprange, null, imp_uuid)
    }

    static def lookupOrCreate2(name, sector, consortium, identifiers, iprange, orgRoleTyp, def imp_uuid = null) {

        if(imp_uuid?.size() == 0) {
          imp_uuid = null
        }

        def result = Org.lookup(name, identifiers, imp_uuid)

        if ( result == null ) {
          // log.debug("Create new entry for ${name}");
          if (sector instanceof String){
            sector = RefdataValue.getByValueAndCategory(sector,'OrgSector')
          }

          if (orgRoleTyp instanceof String) {
             orgRoleTyp = RefdataValue.getByValueAndCategory(orgRoleTyp, 'OrgRoleType')
          }

          result = new Org(
                           name:name,
                           sector:sector,
                           ipRange:iprange,
                           impId: null,
                           gokbId: imp_uuid?.length() > 0 ? imp_uuid : null
          ).save()
          if(orgRoleTyp) {
              result.addToOrgType(orgRoleTyp).save()
          }

            // SUPPORT MULTIPLE IDENTIFIERS
            if (identifiers instanceof ArrayList) {
                identifiers.each{ it ->
                    it.each { k, v ->
                        def io = new IdentifierOccurrence(org: result, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                    }
                }
            }
            // DEFAULT LOGIC
            else {
                identifiers.each { k, v ->
                    def io = new IdentifierOccurrence(org: result, identifier: Identifier.lookupOrCreateCanonicalIdentifier(k, v)).save()
                }
            }

          if ( ( consortium != null ) && ( consortium.length() > 0 ) ) {
            def db_consortium = Org.lookupOrCreate(consortium, null, null, [:], null)
            def consLink = new Combo(fromOrg:result,
                                     toOrg:db_consortium,
                                     status:null,
                                     type: RefdataValue.getByValueAndCategory('Consortium','Combo Type')
            ).save()
          }
        } else if (Holders.config.globalDataSync.replaceLocalImpIds.Org && result && imp_uuid && imp_uuid != result.gokbId){
          result.gokbId = imp_uuid
          result.impId = imp_uuid
          result.save()
        }
 
        result
    }

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

    def sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    def pub = getPublisher()

    try {
      builder.'kbplus' (attr) {
        builder.'org' (['id':(id)]) {
          builder.'name' (name)
        }
        builder.'identifiers' () {
          ids?.each { id_oc ->
            builder.identifier([namespace:id_oc.identifier?.ns.ns, value:id_oc.identifier?.value])
          }
        }
      }
    }
    catch ( Exception e ) {
      log.error(e);
    }

  }

    def getDesignation() {
        return (shortname?:(sortname?:(name?:(globalUID?:id))))
    }


    @Override
    String toString() {
        //sector ? name + ', ' + sector?.getI10n('value') : "${name}"
        name
    }

    List<Person> getGeneralContactPersons(boolean onlyPublic) {

        if (onlyPublic) {
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic.value != 'No' and pr.org = :org and pr.functionType = :gcp",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS]
            )
        }
        else {
            Org ctxOrg = contextService.getOrg()
            Person.executeQuery(
                    "select distinct p from Person as p inner join p.roleLinks pr where pr.org = :org and pr.functionType = :gcp " +
                    " and ( (p.isPublic.value = 'No' and p.tenant = :ctx) or (p.isPublic.value != 'No') )",
                    [org: this, gcp: RDStore.PRS_FUNC_GENERAL_CONTACT_PRS, ctx: ctxOrg]
            )
        }
    }

    List<Person> getPublicPersons() {
        Person.executeQuery(
                "select distinct p from Person as p inner join p.roleLinks pr where p.isPublic.value != 'No' and pr.org = :org",
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

    def getallOrgTypeIds()
    {
        List result = []
        orgType.collect{ it -> result.add(it.id) }
        result

        /*
        // TODO: ugliest HOTFIX ever
        def hibernateSession = sessionFactory.currentSession

        String query = 'select refdata_value_id from org_roletype where org_id = ' + this.id
        def sqlQuery = hibernateSession.createSQLQuery(query)
        sqlQuery.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP)
        def result = sqlQuery.list()?.collect{ it.refdata_value_id as Long }
        //log.debug('getallOrgRoleTypeIds(): ' + result)
        result
        */
    }
}
