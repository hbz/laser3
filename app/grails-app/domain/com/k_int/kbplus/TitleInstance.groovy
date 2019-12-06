package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomain
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import de.laser.traits.AuditableTrait
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient
import java.text.Normalizer
import java.util.regex.Pattern

@Log4j
class TitleInstance extends AbstractBaseDomain implements AuditableTrait {

  @Transient
  def grailsApplication
  @Transient
  def deletionService

    // AuditableTrait
    static auditable = true
    static controlledProperties = ['title']

  static Log static_logger = LogFactory.getLog(TitleInstance)

  static final Pattern alphanum = Pattern.compile("\\p{Punct}|\\p{Cntrl}");

  String title
  String normTitle
  String keyTitle
  String sortTitle
  String gokbId
  //URL originEditUrl

  @RefdataAnnotation(cat = 'TitleInstanceStatus')
  RefdataValue status

  //@RefdataAnnotation(cat = 'Title Type')
  //RefdataValue type

  @RefdataAnnotation(cat = 'Title Type')
  RefdataValue medium

  Date dateCreated
  Date lastUpdated

  static mappedBy = [
                     tipps:     'title',
                     ids:       'ti',
                     orgs:      'title',
                     historyEvents: 'participant',
                     prsLinks:  'title'
                     ]
  static hasMany = [
                    tipps:  TitleInstancePackagePlatform,
                    ids:    Identifier,
                    orgs:   OrgRole,
                    historyEvents: TitleHistoryEventParticipant,
                    prsLinks: PersonRole,
                    creators: CreatorTitle
                    ]

  static mapping = {
               id column:'ti_id'
        globalUID column:'ti_guid'
            title column:'ti_title', type:'text'
        normTitle column:'ti_norm_title', type:'text'
         keyTitle column:'ti_key_title', type:'text'
          version column:'ti_version'
           gokbId column:'ti_gokb_id', index:'ti_gokb_id_idx'
    //originEditUrl column:'ti_origin_edit_url'
           status column:'ti_status_rv_fk'
      // type column:'ti_type_rv_fk' -> existing values should be moved to medium
           medium column:'ti_medium_rv_fk'
            //tipps sort:'startDate', order: 'asc', batchSize: 10
        sortTitle column:'sort_title', type:'text'

      ids           batchSize: 10
      orgs          batchSize: 10
      historyEvents batchSize: 10
      prsLinks      batchSize: 10
      creators      batchSize: 10
  }

    static constraints = {
        globalUID(nullable:false, blank:false, unique:true, maxSize:255)
        status(nullable:true, blank:false)
        //type(nullable:true, blank:false)
        medium(nullable:true, blank:false)
        title(nullable:true, blank:false,maxSize:2048)
        normTitle(nullable:true, blank:false,maxSize:2048)
        sortTitle(nullable:true, blank:false,maxSize:2048)
        keyTitle(nullable:true, blank:false,maxSize:2048)
        creators(nullable:true, blank:false)
        gokbId (nullable:false, blank:false, unique: true)
        //originEditUrl(nullable:true, blank:false)
    }

    def afterDelete() {
        deletionService.deleteDocumentFromIndex(this.class.name, this.globalUID)
    }

  String getIdentifierValue(idtype) {
    def result=null
    ids?.each { id ->
      if ( id.ns?.ns?.toLowerCase() == idtype.toLowerCase() )
        result = id.value
    }
    result
  }


  Org getPublisher() {
    def result = null;
    orgs.each { o ->
      if ( o.roleType?.value == 'Publisher' ) {
        result = o.org
      }
    }
    result
  }

    @Deprecated
  static def lookupByIdentifierString(idstr) {

      static_logger.debug("lookupByIdentifierString(${idstr})")

    def result = null;
    def qr = null;
    def idstr_components = idstr.split(':');

    switch ( idstr_components.size() ) {
      case 1:
        qr = executeQuery('select t from TitleInstance as t join t.ids as ident where ident.value = ?',[idstr_components[0]])
        break;
      case 2:
        qr = executeQuery('select t from TitleInstance as t join t.ids as ident where ident.value = ? and lower(ident.ns.ns) = ?',[idstr_components[1],idstr_components[0]?.toLowerCase()])
        break;
      default:
        // static_logger.debug("Unable to split");
        break;
    }

    // static_logger.debug("components: ${idstr_components} : ${qr}");

    if ( qr ) {
      switch ( qr.size() ) {
        case 0:
            static_logger.debug("No matches - trying to locate via identifier group");
          switch ( idstr_components.size() ) {
            case 1:
              qr = executeQuery('select t from TitleInstance as t join t.ids as ident where ident.value = ?', [idstr_components[0]])
              break;
            case 2:
              qr = executeQuery('select t from TitleInstance as t join t.ids as ident where ident.value = ? and ident.ns.ns = ?', [idstr_components[1], idstr_components[0]?.toLowerCase()])
              break;
            default:
              // static_logger.debug("Unable to split");
              break;
          }

          break;
        case 1:
          result = qr.get(0);
          break;
        default:
            static_logger.error("WARNING:: Identifier '${idstr}' matched multiple rows");
          break;
      }
    }

    result
  }

  /**
   * Attempt to look up a title instance which has any of the listed identifiers
   * @param candidate_identifiers A list of maps containing identifiers and namespaces [ { namespace:'ISSN', value:'Xnnnn-nnnn' }, {namespace:'ISSN', value:'Xnnnn-nnnn'} ]
   */
    @Deprecated
  static def findByIdentifier(candidate_identifiers) {
    def matched = []
    candidate_identifiers.each { i ->
      // TODO [ticket=1789]
      //List<IdentifierOccurrence> ioList = IdentifierOccurrence.executeQuery('select io from IdentifierOccurrence io join io.identifier id where id.ns = :namespace and id.value = :value',[namespace:i.namespace,value:i.value])
      List<Identifier> identifiers = Identifier.executeQuery('select ident from Identifier ident where ident.ns = :namespace and ident.value = :value', [namespace:i.namespace, value:i.value])
      if(identifiers.size() > 0) {
        Identifier ident = identifiers.get(0)
        if ( ( ident != null ) && ( ident.ti != null ) ) {
          if (! matched.contains(ident.ti) ) {
            matched.add(ident.ti)
          }
        }
      }
    }

    // Didn't match anything - see if we can match based on identifier without namespace [In case of duff supplier data - or duff code like this legacy shit...]
    if ( matched.size() == 0 ) {
      candidate_identifiers.each { i ->
        // TODO [ticket=1789]
        //def id1 = Identifier.executeQuery('Select io from IdentifierOccurrence as io where io.identifier.value = ?',[i.value]);
        def id1 = Identifier.executeQuery('select ident from Identifier as ident where ident.value = ?', [i.value])
        id1.each {
          if ( it.ti != null ) {
            if ( ! matched.contains(it.ti) ) {
              matched.add(it.ti)
            }
          }
        }
      }
    }

    def result = null;
    if ( matched.size() == 1 ) {
      result = matched.get(0);
    }
    else if ( matched.size() > 1 ) {
      throw new Exception("Identifier set ${candidate_identifiers} matched multiple titles");
    }

    return result;
  }

  public static String generateSortTitle(String input_title) {
    if ( ! input_title ) return null;

    def s1 = Normalizer.normalize(input_title, Normalizer.Form.NFKD).trim().toLowerCase()
    s1 = s1.replaceFirst('^copy of ','')
    s1 = s1.replaceFirst('^the ','')
    s1 = s1.replaceFirst('^a ','')
    s1 = s1.replaceFirst('^der ','')
    
    return  s1.trim()  
  }

  public static String generateNormTitle(String input_title) {
    if (!input_title) return null;

    def result = input_title.replaceAll('&',' and ');
    result = result.trim();
    result = result.toLowerCase();
    result = alphanum.matcher(result).replaceAll("");
    result = result.replaceAll("\\s+", " ");
   
    return asciify(result)
  }

  public static String generateKeyTitle(String s) {
    def result = null
    if ( s != null ) {
        s = s.replaceAll('&',' and ');
        s = s.trim(); // first off, remove whitespace around the string
        s = s.toLowerCase(); // then lowercase it
        s = alphanum.matcher(s)?.replaceAll(''); // then remove all punctuation and control chars
        s = s.replaceAll("\\s+", " ");
        String[] frags = StringUtils.split(s); // split by whitespace
        TreeSet<String> set = new TreeSet<String>();
        for (String ss : frags) {
            set.add(ss); // order fragments and dedupe
        }
        StringBuffer b = new StringBuffer();
        Iterator<String> i = set.iterator();
        while (i.hasNext()) {  // join ordered fragments back together
            b.append(i.next());
            if ( i.hasNext() )
              b.append(' ');
        }
        result = asciify(b.toString()); // find ASCII equivalent to characters
    }

    return result;
  }

    String printTitleType() {

    }
}
