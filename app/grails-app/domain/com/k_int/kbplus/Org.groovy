package com.k_int.kbplus

import com.k_int.kbplus.auth.*
import de.laser.domain.BaseDomainComponent
import org.apache.commons.lang3.StringUtils
import org.apache.commons.logging.LogFactory
import groovy.util.logging.*
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

import javax.persistence.Transient

@Log4j
class Org extends BaseDomainComponent {

    String name
    String shortname
    String sortname
    String url

    String shortcode            // Used to generate friendly semantic URLs

    String impId
    String comment
    String ipRange
    String scope
    Date dateCreated
    Date lastUpdated
    String categoryId

    int fteStudents
    int fteStaff

    RefdataValue orgType
    RefdataValue sector
    RefdataValue status
    RefdataValue membership
    RefdataValue country        // RefdataCategory 'Country'
    RefdataValue federalState   // RefdataCategory 'Federal State'
    RefdataValue libraryNetwork // RefdataCategory 'Library Network'
    RefdataValue funderType     // RefdataCategory 'Funder Type'
    RefdataValue libraryType    // RefdataCategory 'Library Type'

    Set ids = []

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
        privateProperties:'owner'
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
        privateProperties:  OrgPrivateProperty
    ]

    static mapping = {
                id column:'org_id'
           version column:'org_version'
         globalUID column:'org_guid'
             impId column:'org_imp_id', index:'org_imp_id_idx'
              name column:'org_name', index:'org_name_idx'
         shortname column:'org_shortname', index:'org_shortname_idx'
          sortname column:'org_sortname', index:'org_sortname_idx'
               url column:'org_url'
       fteStudents column:'org_fte_students'
          fteStaff column:'org_fte_staff'
           comment column:'org_comment'
           ipRange column:'org_ip_range'
         shortcode column:'org_shortcode', index:'org_shortcode_idx'
             scope column:'org_scope'
        categoryId column:'org_cat'
           orgType column:'org_type_rv_fk'
            sector column:'org_sector_rv_fk'
            status column:'org_status_rv_fk'
        membership column:'org_membership'
           country column:'org_country_rv_fk'
      federalState column:'org_federal_state_rv_fk'
    libraryNetwork column:'org_library_network_rv_fk'
        funderType column:'org_funder_type_rv_fk'
       libraryType column:'org_library_type_rv_fk'
    }

    static constraints = {
           globalUID(nullable:true, blank:false, unique:true, maxSize:255)
                name(nullable:true, blank:false, maxSize:255)
           shortname(nullable:true, blank:true, maxSize:255)
            sortname(nullable:true, blank:true, maxSize:255)
                 url(nullable:true, blank:true, maxSize:512)
         fteStudents(nullable:true, blank:true)
            fteStaff(nullable:true, blank:true)
               impId(nullable:true, blank:true, maxSize:255)
             comment(nullable:true, blank:true, maxSize:2048)
             ipRange(nullable:true, blank:true, maxSize:1024)
              sector(nullable:true, blank:true)
           shortcode(nullable:true, blank:true, maxSize:128)
               scope(nullable:true, blank:true, maxSize:128)
          categoryId(nullable:true, blank:true, maxSize:128)
             orgType(nullable:true, blank:true, maxSize:128)
              status(nullable:true, blank:true)
          membership(nullable:true, blank:true, maxSize:128)
             country(nullable:true, blank:true)
        federalState(nullable:true, blank:true)
      libraryNetwork(nullable:true, blank:true)
          funderType(nullable:true, blank:true)
         libraryType(nullable:true, blank:true)
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

  def getIdentifierByType(idtype) {
    def result = null
    ids.each { id ->
      if ( id.identifier.ns.ns.equalsIgnoreCase(idtype) ) {
        result = id.identifier;
      }
    }
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

  static def lookupOrCreate(name, sector, consortium, identifiers, iprange) {

    def result = null;

    // See if we can uniquely match on any of the identifiers
    identifiers.each { k,v ->
      if ( v != null ) {
        def o = Org.executeQuery("select o from Org as o join o.ids as io where io.identifier.ns.ns = ? and io.identifier.value = ?",[k,v])
        if ( o.size() > 0 ) {
          result = o[0]
        }
      }
    }

    // No match by identifier, try and match by name
    if ( result == null ) {
      // log.debug("Match by name ${name}");
      def o = Org.executeQuery("select o from Org as o where lower(o.name) = ?",[name.toLowerCase()])
      if ( o.size() > 0 ) {
        result = o[0]
      }
    }

    if ( result == null ) {
      // log.debug("Create new entry for ${name}");
      if (sector instanceof String){
        sector = RefdataCategory.lookupOrCreate('OrgSector', sector)
      }

      result = new Org(
                       name:name, 
                       sector:sector,
                       ipRange:iprange,
                       impId:java.util.UUID.randomUUID().toString()).save()

      identifiers.each { k,v ->
          def io = new IdentifierOccurrence(org:result, identifier:Identifier.lookupOrCreateCanonicalIdentifier(k,v)).save()
      }

      if ( ( consortium != null ) && ( consortium.length() > 0 ) ) {
        def db_consortium = Org.lookupOrCreate(consortium, null, null, [:], null)
        def consLink = new Combo(fromOrg:result,
                                 toOrg:db_consortium,
                                 status:null,
                                 type: RefdataCategory.lookupOrCreate('Organisational Role', 'Package Consortia')).save()
      }
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
  
    @Override
    String toString() {
        name + ', ' + sector + ' (' + id + ')'
    }
}
