package de.laser.titles

import de.laser.IssueEntitlement
import de.laser.Org
import de.laser.TitleInstancePackagePlatform
import de.laser.Identifier
import de.laser.OrgRole
import de.laser.RefdataValue
import de.laser.base.AbstractBaseWithCalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import de.laser.annotations.RefdataInfo
import org.apache.commons.lang3.StringUtils

import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * Deprecated but kept intact because of an eventual reinstauration of the Global Open Knowledge Base (GOKb)
 * This class is reflecting a bibliographic record. Every title instance may occur in different package or platform contexts; as this may extremely vary, even the title's name, this central instance is unusable as such in
 * subscription context. Everything reflected by this class is being kept track in the {@link TitleInstancePackagePlatform} class. Title instances are specified by their title type; they were represented in inheriting classes:
 * <ul>
 *     <li>{@link BookInstance}</li>
 *     <li>{@link DatabaseInstance}</li>
 *     <li>{@link JournalInstance}</li>
 * </ul>
 * @see TitleInstancePackagePlatform
 */
@Deprecated
class TitleInstance extends AbstractBaseWithCalculatedLastUpdated {

    // AuditableTrait
    //static auditable = true
    //static controlledProperties = ['title']

  static final Pattern alphanum = Pattern.compile("\\p{Punct}|\\p{Cntrl}");

  String title
  String normTitle
  String keyTitle
  String sortTitle
  String gokbId
  String seriesName
  String subjectReference

  @RefdataInfo(cat = RDConstants.TITLE_STATUS)
  RefdataValue status

  @RefdataInfo(cat = RDConstants.TITLE_MEDIUM)
  RefdataValue medium

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

/*
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
                  prsLinks: PersonRole
                    ]
*/

    static transients = ['publisher'] // mark read-only accessor methods

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
           status column:'ti_status_rv_fk'
           medium column:'ti_medium_rv_fk'
            //tipps sort:'startDate', order: 'asc', batchSize: 10
      dateCreated column: 'ti_date_created'
      lastUpdated column: 'ti_last_updated'
      lastUpdatedCascading column: 'ti_last_updated_cascading'
      sortTitle column:'ti_sort_title', type:'text'

      ids           batchSize: 10
      orgs          batchSize: 10
      historyEvents batchSize: 10
      prsLinks      batchSize: 10
  }

    static constraints = {
        globalUID(blank:false, unique:true, maxSize:255)
        status(nullable:true)
        medium(nullable:true)
        title(nullable:true, blank:false,maxSize:2048)
        normTitle(nullable:true, blank:false,maxSize:2048)
        sortTitle(nullable:true, blank:false,maxSize:2048)
        keyTitle(nullable:true, blank:false,maxSize:2048)
        gokbId (blank:false, unique: true, maxSize:511)
        seriesName(nullable:true, blank:false)
        subjectReference(nullable:true, blank:false)
        lastUpdatedCascading (nullable: true)
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
        super.beforeInsertHandler()
    }
    @Override
    def beforeUpdate() {
        super.beforeUpdateHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Gets the value of a title instance's identifier of the given type
     * @param idtype the identifier namespace string to be queried
     * @return the identifier value or null if not found
     */
  String getIdentifierValue(String idtype) {
    String result
    ids?.each { id ->
      if (id.ns?.ns?.toLowerCase() == idtype.toLowerCase())
        result = id.value
    }
    result
  }

    /**
     * Retrieves among the {@link OrgRole}s the title's publisher
     * @return the publisher {@link Org}
     */
  Org getPublisher() {
    Org result
    orgs.each { o ->
      if ( o.roleType?.value == 'Publisher' ) {
        result = o.org
      }
    }
    result
  }

    /**
     * Gets the first, the last and a list of {@link IssueEntitlement}s of the given institution
     * @param institution the {@link Org} whose entitlements should be retrieved
     * @param dateformat the format to output the retrieved dates
     * @return a {@link Map} containing the earliest and the latest issue entitlement date and the {@link List} of {@link IssueEntitlement}s retrieved
     */
    def getInstitutionalCoverageSummary(institution, dateformat) {
        getInstitutionalCoverageSummary(institution, dateformat, null)
    }

    /**
     * Gets the first, the last and a list of {@link IssueEntitlement}s of the given institution within a given date range
     * @param institution the {@link Org} whose entitlements should be retrieved
     * @param dateformat the format to output the retrieved dates
     * @param date_restriction the date range within which the {@link IssueEntitlement} {@link de.laser.Subscription}'s start/end date have to be
     * @return a {@link Map} containing the earliest and the latest issue entitlement date and the {@link List} of {@link IssueEntitlement}s retrieved
     */
    def getInstitutionalCoverageSummary(institution, dateformat, date_restriction) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateformat)
        String qry = """
select ie from IssueEntitlement as ie JOIN ie.subscription.orgRelations as o 
  where ie.tipp.title = :title and o.org = :institution 
  AND (o.roleType.value = 'Subscriber' OR o.roleType.value = 'Subscriber_Consortial' OR o.roleType.value = 'Subscription Consortia') 
  AND ie.status.value != 'Deleted'
"""
        Map<String, Object> qry_params = [title:this, institution:institution]

        if ( date_restriction ) {
            qry += " AND (ie.subscription.startDate <= :date_restriction OR ie.subscription.startDate = null) AND (ie.subscription.endDate >= :date_restriction OR ie.subscription.endDate = null) "
            qry_params.date_restriction = date_restriction
        }

        def ies = IssueEntitlement.executeQuery(qry,qry_params)
        def earliest = null
        def latest = null
        boolean open = false

        [
                earliest:earliest?sdf.format(earliest):'',
                latest: open ? '': (latest?sdf.format(latest):''),
                ies:ies
        ]
    }

    /**
     * Copied into {@link TitleInstancePackagePlatform}
     * Generates a normalized sort title string from the given input title
     * @param input_title the input title string to normalize
     * @return the normalized title
     */
  static String generateSortTitle(String input_title) {
    if ( ! input_title ) return null;

    String s1 = Normalizer.normalize(input_title, Normalizer.Form.NFKD).trim().toLowerCase()
    s1 = s1.replaceFirst('^copy of ','')
    s1 = s1.replaceFirst('^the ','')
    s1 = s1.replaceFirst('^a ','')
    s1 = s1.replaceFirst('^der ','')
    
    return  s1.trim()  
  }

    /**
     * Copied into {@link TitleInstancePackagePlatform}
     * Similar to generateSortTitle(), this generates a normalized string representation of the input title but according to different rules
     * @param input_title the input title string to normalize
     * @return the normalized title
     */
  static String generateNormTitle(String input_title) {
    if (!input_title) return null;

    String result = input_title.replaceAll('&',' and ');
    result = result.trim();
    result = result.toLowerCase();
    result = alphanum.matcher(result).replaceAll("");
    result = result.replaceAll("\\s+", " ");
   
    return asciify(result)
  }

    /**
     * Not used
     * Should generate (probably?) another way of a sortable title string ... again - different from generateSortTile() and generateNormTitle()
     * @param s the input string to process
     * @return the normalized title
     */
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

    /**
     * Converts the given string into a stream of ASCII charts to avoid encoding issues - does work only for latin-1!
     * @param s the input string to process
     * @return the "asciified" string
     */
    protected static String asciify(String s) {
        char[] c = s.toCharArray();
        StringBuffer b = new StringBuffer();
        for (char element : c) {
            b.append( TitleInstancePackagePlatform.translateChar(element) )
        }
        return b.toString();
    }

    /**
     * Abstract method to retrieve the title instance type as icon string
     * @return
     */
    String printTitleType() {

    }
}
