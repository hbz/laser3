package com.k_int.kbplus

import de.laser.domain.AbstractBaseDomainWithCalculatedLastUpdated
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.helper.RefdataAnnotation
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient
import java.text.Normalizer
import java.util.regex.Pattern

@Log4j
class TitleInstance extends AbstractBaseDomainWithCalculatedLastUpdated {

    @Transient
    def grailsApplication
    @Transient
    def deletionService
    @Transient
    def cascadingUpdateService

    // AuditableTrait
    //static auditable = true
    //static controlledProperties = ['title']

  static Log static_logger = LogFactory.getLog(TitleInstance)

  static final Pattern alphanum = Pattern.compile("\\p{Punct}|\\p{Cntrl}");

  String title
  String normTitle
  String keyTitle
  String sortTitle
  String gokbId
  //URL originEditUrl

  String seriesName
  String subjectReference

  @RefdataAnnotation(cat = RDConstants.TITLE_STATUS)
  RefdataValue status

  //@RefdataAnnotation(cat = 'Title Type')
  //RefdataValue type

  @RefdataAnnotation(cat = RDConstants.TITLE_MEDIUM)
  RefdataValue medium

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

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
      seriesName  column:'ti_series_name', type:'text'
      subjectReference column:'ti_subject_reference', type:'text'
           gokbId column:'ti_gokb_id', index:'ti_gokb_id_idx'
    //originEditUrl column:'ti_origin_edit_url'
           status column:'ti_status_rv_fk'
      // type column:'ti_type_rv_fk' -> existing values should be moved to medium
           medium column:'ti_medium_rv_fk'
            //tipps sort:'startDate', order: 'asc', batchSize: 10
      lastUpdatedCascading column: 'ti_last_updated_cascading'
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
        gokbId (nullable:false, blank:false, unique: true, maxSize:511)
        seriesName(nullable:true, blank:false)
        subjectReference(nullable:true, blank:false)
        //originEditUrl(nullable:true, blank:false)
        lastUpdatedCascading (nullable: true, blank: false)
    }

    @Override
    def afterDelete() {
        static_logger.debug("afterDelete")
        cascadingUpdateService.update(this, new Date())

        deletionService.deleteDocumentFromIndex(this.globalUID)
    }

  String getIdentifierValue(String idtype) {
    String result
    ids?.each { id ->
      if (id.ns?.ns?.toLowerCase() == idtype.toLowerCase())
        result = id.value
    }
    result
  }


  Org getPublisher() {
    Org result
    orgs.each { o ->
      if ( o.roleType?.value == 'Publisher' ) {
        result = o.org
      }
    }
    result
  }

  @Deprecated
  static TitleInstance lookupByIdentifierString(idstr) {

      static_logger.debug("lookupByIdentifierString(${idstr})")

      TitleInstance result
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

    def getInstitutionalCoverageSummary(institution, dateformat) {
        getInstitutionalCoverageSummary(institution, dateformat, null);
    }

    def getInstitutionalCoverageSummary(institution, dateformat, date_restriction) {
        def sdf = new java.text.SimpleDateFormat(dateformat)
        def qry = """
select ie from IssueEntitlement as ie JOIN ie.subscription.orgRelations as o 
  where ie.tipp.title = :title and o.org = :institution 
  AND (o.roleType.value = 'Subscriber' OR o.roleType.value = 'Subscriber_Consortial' OR o.roleType.value = 'Subscription Consortia') 
  AND ie.status.value != 'Deleted'
"""
        def qry_params = ['title':this, institution:institution]

        if ( date_restriction ) {
            qry += " AND (ie.subscription.startDate <= :date_restriction OR ie.subscription.startDate = null) AND (ie.subscription.endDate >= :date_restriction OR ie.subscription.endDate = null) "
            qry_params.date_restriction = date_restriction
        }

        def ies = IssueEntitlement.executeQuery(qry,qry_params)
        def earliest = null
        def latest = null
        boolean open = false

        /*
        TODO: BUG ERMS-1638
        ies.each { ie ->
          if ( earliest == null ) { earliest = ie.startDate } else { if ( ie.startDate < earliest ) { earliest = ie.startDate } }
          if ( latest == null ) { latest = ie.endDate } else { if ( ie.endDate > latest ) { latest = ie.endDate } }
          if ( ie.endDate == null ) open = true;
        }
        */

        [
                earliest:earliest?sdf.format(earliest):'',
                latest: open ? '': (latest?sdf.format(latest):''),
                ies:ies
        ]
    }

  static String generateSortTitle(String input_title) {
    if ( ! input_title ) return null;

    def s1 = Normalizer.normalize(input_title, Normalizer.Form.NFKD).trim().toLowerCase()
    s1 = s1.replaceFirst('^copy of ','')
    s1 = s1.replaceFirst('^the ','')
    s1 = s1.replaceFirst('^a ','')
    s1 = s1.replaceFirst('^der ','')
    
    return  s1.trim()  
  }

  static String generateNormTitle(String input_title) {
    if (!input_title) return null;

    def result = input_title.replaceAll('&',' and ');
    result = result.trim();
    result = result.toLowerCase();
    result = alphanum.matcher(result).replaceAll("");
    result = result.replaceAll("\\s+", " ");
   
    return asciify(result)
  }

  static String generateKeyTitle(String s) {
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

    protected static String asciify(String s) {
        char[] c = s.toCharArray();
        StringBuffer b = new StringBuffer();
        for (char element : c) {
            b.append(translate(element));
        }
        return b.toString();
    }

    /**
     * Translate the given unicode char in the closest ASCII representation
     * NOTE: this function deals only with latin-1 supplement and latin-1 extended code charts
     */
    private static char translate(char c) {
        switch(c) {
            case '\u00C0':
            case '\u00C1':
            case '\u00C2':
            case '\u00C3':
            case '\u00C4':
            case '\u00C5':
            case '\u00E0':
            case '\u00E1':
            case '\u00E2':
            case '\u00E3':
            case '\u00E4':
            case '\u00E5':
            case '\u0100':
            case '\u0101':
            case '\u0102':
            case '\u0103':
            case '\u0104':
            case '\u0105':
                return 'a';
            case '\u00C7':
            case '\u00E7':
            case '\u0106':
            case '\u0107':
            case '\u0108':
            case '\u0109':
            case '\u010A':
            case '\u010B':
            case '\u010C':
            case '\u010D':
                return 'c';
            case '\u00D0':
            case '\u00F0':
            case '\u010E':
            case '\u010F':
            case '\u0110':
            case '\u0111':
                return 'd';
            case '\u00C8':
            case '\u00C9':
            case '\u00CA':
            case '\u00CB':
            case '\u00E8':
            case '\u00E9':
            case '\u00EA':
            case '\u00EB':
            case '\u0112':
            case '\u0113':
            case '\u0114':
            case '\u0115':
            case '\u0116':
            case '\u0117':
            case '\u0118':
            case '\u0119':
            case '\u011A':
            case '\u011B':
                return 'e';
            case '\u011C':
            case '\u011D':
            case '\u011E':
            case '\u011F':
            case '\u0120':
            case '\u0121':
            case '\u0122':
            case '\u0123':
                return 'g';
            case '\u0124':
            case '\u0125':
            case '\u0126':
            case '\u0127':
                return 'h';
            case '\u00CC':
            case '\u00CD':
            case '\u00CE':
            case '\u00CF':
            case '\u00EC':
            case '\u00ED':
            case '\u00EE':
            case '\u00EF':
            case '\u0128':
            case '\u0129':
            case '\u012A':
            case '\u012B':
            case '\u012C':
            case '\u012D':
            case '\u012E':
            case '\u012F':
            case '\u0130':
            case '\u0131':
                return 'i';
            case '\u0134':
            case '\u0135':
                return 'j';
            case '\u0136':
            case '\u0137':
            case '\u0138':
                return 'k';
            case '\u0139':
            case '\u013A':
            case '\u013B':
            case '\u013C':
            case '\u013D':
            case '\u013E':
            case '\u013F':
            case '\u0140':
            case '\u0141':
            case '\u0142':
                return 'l';
            case '\u00D1':
            case '\u00F1':
            case '\u0143':
            case '\u0144':
            case '\u0145':
            case '\u0146':
            case '\u0147':
            case '\u0148':
            case '\u0149':
            case '\u014A':
            case '\u014B':
                return 'n';
            case '\u00D2':
            case '\u00D3':
            case '\u00D4':
            case '\u00D5':
            case '\u00D6':
            case '\u00D8':
            case '\u00F2':
            case '\u00F3':
            case '\u00F4':
            case '\u00F5':
            case '\u00F6':
            case '\u00F8':
            case '\u014C':
            case '\u014D':
            case '\u014E':
            case '\u014F':
            case '\u0150':
            case '\u0151':
                return 'o';
            case '\u0154':
            case '\u0155':
            case '\u0156':
            case '\u0157':
            case '\u0158':
            case '\u0159':
                return 'r';
            case '\u015A':
            case '\u015B':
            case '\u015C':
            case '\u015D':
            case '\u015E':
            case '\u015F':
            case '\u0160':
            case '\u0161':
            case '\u017F':
                return 's';
            case '\u0162':
            case '\u0163':
            case '\u0164':
            case '\u0165':
            case '\u0166':
            case '\u0167':
                return 't';
            case '\u00D9':
            case '\u00DA':
            case '\u00DB':
            case '\u00DC':
            case '\u00F9':
            case '\u00FA':
            case '\u00FB':
            case '\u00FC':
            case '\u0168':
            case '\u0169':
            case '\u016A':
            case '\u016B':
            case '\u016C':
            case '\u016D':
            case '\u016E':
            case '\u016F':
            case '\u0170':
            case '\u0171':
            case '\u0172':
            case '\u0173':
                return 'u';
            case '\u0174':
            case '\u0175':
                return 'w';
            case '\u00DD':
            case '\u00FD':
            case '\u00FF':
            case '\u0176':
            case '\u0177':
            case '\u0178':
                return 'y';
            case '\u0179':
            case '\u017A':
            case '\u017B':
            case '\u017C':
            case '\u017D':
            case '\u017E':
                return 'z';
        }
        return c;
    }

    String printTitleType() {

    }
}
