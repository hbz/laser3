package de.laser


import de.laser.base.AbstractBase
import de.laser.finance.PriceItem
import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDStore
import de.laser.titles.TitleHistoryEvent
import de.laser.titles.TitleHistoryEventParticipant
import groovy.time.TimeCategory
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.regex.Pattern

/**
 * A title instance. Title instances in LAS:eR and we:kb are mandatorily linked to a {@link Package} and a {@link Platform}. Titles may be (list is not exhaustive):
 * <ul>
 *     <li>(E)Books</li>
 *     <li>Databases</li>
 *     <li>Journals</li>
 *     <li>Films</li>
 *     <li>...</li>
 * </ul>
 * Title instance records may have an access start / access end date; those are set by the provider and define from when to when this title is available in the given package context.
 * The package context defines the subscribability of the title; usually, titles are subscribed within a package and those packages are then linked to a subscription.
 * This class represents the global entitlement level, i.e. the title which counts for the package provided by the provider and is independent from negotiatory differences which may vary
 * from subscription to subscription. See {@link IssueEntitlement} for the local holding level. Local means for the institution subscribing the title within a certain subscription context.
 * This class is moreover a mirror of the we:kb TitleInstancePackagePlatform implementation <a href="https://github.com/hbz/wekb/blob/wekb-dev/server/gokbg3/grails-app/domain/org/gokb/cred/TitleInstancePackagePlatform.groovy">(see TitleInstancePackagePlatform in we:kb)</a>
 * and generally a reflection of a KBART record (see <a href="https://groups.niso.org/apps/group_public/download.php/16900/RP-9-2014_KBART.pdf">KBART specification</a>)
 * @see Package
 * @see SubscriptionPackage
 * @see Subscription
 * @see Platform
 * @see IssueEntitlement
 */
class TitleInstancePackagePlatform extends AbstractBase /*implements AuditableTrait*/ {

  @Transient
  def messageSource

    // AuditableTrait
    //static auditable = true
    static controlledProperties = ['status', 'platform','accessStartDate','accessEndDate','coverages']

    Date accessStartDate
    Date accessEndDate
  //Date coreStatusStart
  //Date coreStatusEnd
    String name
    String sortname
    String normName
    String seriesName
    String subjectReference
    String imprint
    String titleType
    @RefdataAnnotation(cat = RDConstants.TITLE_MEDIUM)
    RefdataValue medium
    Date dateFirstInPrint
    Date dateFirstOnline
    String summaryOfContent
    String volume

    String firstAuthor
    String firstEditor
    String publisherName

    Integer editionNumber
    String  editionStatement
    String editionDifferentiator
  //String rectype="so"
    String gokbId
  //TitleInstance title

    @RefdataAnnotation(cat = RDConstants.TIPP_STATUS)
    RefdataValue status

    @RefdataAnnotation(cat = '?')
    RefdataValue option

    @RefdataAnnotation(cat = '?')
    RefdataValue delayedOA

    @RefdataAnnotation(cat = '?')
    RefdataValue hybridOA

    @RefdataAnnotation(cat = '?')
    RefdataValue statusReason

    @RefdataAnnotation(cat = '?')
    RefdataValue payment

    @RefdataAnnotation(cat = RDConstants.TIPP_ACCESS_TYPE)
    RefdataValue accessType

    @RefdataAnnotation(cat = RDConstants.LICENSE_OA_TYPE)
    RefdataValue openAccess

    String hostPlatformURL

    Date dateCreated
    Date lastUpdated
    SortedSet ids
    SortedSet ddcs
    SortedSet languages
    SortedSet altnames

  static mappedBy = [ids: 'tipp',
                     ddcs: 'tipp',
                     languages: 'tipp',
                     orgs: 'tipp',
                     prsLinks: 'tipp',
                     priceItems: 'tipp',
                     historyEvents: 'tipp',
                     altnames: 'tipp']
  static hasMany = [ids: Identifier,
                    ddcs: DeweyDecimalClassification,
                    languages: Language,
                    coverages: TIPPCoverage,
                    orgs: OrgRole,
                    historyEvents: TitleHistoryEvent,
                    prsLinks: PersonRole,
                    priceItems: PriceItem,
                    altnames   : AlternativeName]

  static belongsTo = [
    pkg:Package,
    platform:Platform
  ]

    static transients = [
            'derivedAccessStartDate', 'derivedAccessEndDate',
            'availabilityStatus', 'availabilityStatusAsString', 'availabilityStatusExplanation'
    ] // mark read-only accessor methods

  static mapping = {
                id column:'tipp_id'
         globalUID column:'tipp_guid'
           version column:'tipp_version'
               pkg column:'tipp_pkg_fk',    index: 'tipp_idx, tipp_pkg_idx'
          platform column:'tipp_plat_fk',   index: 'tipp_idx, tipp_plat_idx'
          // title column:'tipp_ti_fk',     index: 'tipp_idx'
         titleType column:'tipp_title_type'
            medium column:'tipp_medium_rv_fk', index: 'tipp_medium_idx'
              name column:'tipp_name', type: 'text'
          sortname column:'tipp_sort_name', type: 'text'
          normName column:'tipp_norm_name', type: 'text'
     publisherName column:'tipp_publisher_name', type: 'text'
        seriesName column:'tipp_series_name', type: 'text'
           imprint column:'tipp_imprint', type: 'text'
  subjectReference column:'tipp_subject_reference', type: 'text'
            gokbId column:'tipp_gokb_id'
            status column:'tipp_status_rv_fk', index: 'tipp_status_idx'
         delayedOA column:'tipp_delayedoa_rv_fk'
          hybridOA column:'tipp_hybridoa_rv_fk'
      statusReason column:'tipp_status_reason_rv_fk'
           payment column:'tipp_payment_rv_fk'
            option column:'tipp_option_rv_fk'
   hostPlatformURL column:'tipp_host_platform_url', type: 'text'
      accessStartDate column:'tipp_access_start_date'
      accessEndDate column:'tipp_access_end_date'
      accessType column:'tipp_access_type_rv_fk'
      openAccess column:'tipp_open_access_rv_fk'
      dateFirstInPrint column:'tipp_date_first_in_print'
      dateFirstOnline column:'tipp_date_first_online'
      summaryOfContent column:'tipp_summary_of_content'
      volume column:'tipp_volume'
      firstEditor column: 'tipp_first_editor', type: 'text'
      firstAuthor column: 'tipp_first_author', type: 'text'
      editionNumber column: 'tipp_edition_number'
      editionStatement column: 'tipp_edition_statement', type: 'text'
      editionDifferentiator column: 'tipp_edition_differentiator', type: 'text'
      dateCreated column: 'tipp_date_created'
      lastUpdated column: 'tipp_last_updated'

      ids                   batchSize: 10, sort: 'ns'
      ddcs                  batchSize: 10
      languages             batchSize: 10
      coverages             batchSize: 10, sort: 'startDate', order: 'asc'
      priceItems            batchSize: 10, sort: 'startDate', order: 'asc'
  }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        gokbId (blank:false, unique: true, maxSize:511)
        status      (nullable:true)
        name        (nullable:true)
        sortname    (nullable:true)
        normName    (nullable:true)
        seriesName  (nullable:true)
        imprint     (nullable:true)
       publisherName(nullable:true)
    subjectReference(nullable:true)
        titleType   (nullable:true)
        medium      (nullable:true)
        delayedOA   (nullable:true)
        hybridOA    (nullable:true)
        statusReason(nullable:true)
        payment     (nullable:true)
        option      (nullable:true)
        hostPlatformURL(nullable:true, blank:true, maxSize:2048)
        accessStartDate (nullable:true)
        accessEndDate (nullable:true)
        accessType (nullable:true)
        openAccess (nullable:true)
        dateFirstInPrint(nullable:true)
        dateFirstOnline(nullable:true)
        summaryOfContent(nullable:true, blank:false)
        volume(nullable:true, blank:false)
        firstAuthor (nullable:true, blank:false)
        firstEditor (nullable:true, blank:false)
        editionDifferentiator (nullable:true, blank:false)
        editionNumber       (nullable:true)
        editionStatement (nullable:true, blank:false)
        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    static final Pattern alphanum = Pattern.compile("\\p{Punct}|\\p{Cntrl}")

    @Override
    def beforeUpdate(){
        touchPkgLastUpdated()
        super.beforeUpdateHandler()
    }
    @Override
    def beforeInsert() {
        touchPkgLastUpdated()
        generateSortTitle()
        generateNormTitle()
        super.beforeInsertHandler()
    }
    @Override
    def beforeDelete() {
        super.beforeDeleteHandler()
    }

    /**
     * Cascade-trigger: if a title is updated, the whole package should be done likewise. This is necessary for APIs pulling data incrementally from a package endpoint; the OAI-based sync between LAS:eR and we:kb worked likewise
     * before it was changed to JSON and ElasticSearch harvesting
     */
  @Transient
  void touchPkgLastUpdated() {
    if(pkg!=null){
      use(TimeCategory) {
        pkg.lastUpdated += 1.seconds
      }
      pkg.save(failOnError:true)
    }
  }

    /**
     * Removes stopwords from the title and generates a sortable title string.
     * @see Normalizer.Form#NFKD
     */
    void generateSortTitle() {
        if ( name ) {
            sortname = Normalizer.normalize(name, Normalizer.Form.NFKD).trim().toLowerCase()
            sortname = sortname.replaceFirst('^copy of ', '')
            sortname = sortname.replaceFirst('^the ', '')
            sortname = sortname.replaceFirst('^a ', '')
            sortname = sortname.replaceFirst('^der ', '')
            sortname = sortname.replaceFirst('^die ', '')
            sortname = sortname.replaceFirst('^das ', '')
        }
    }

    /**
     * Generates a normalised title, i.e. converts the title to lower case, replaces special characters and numbers
     */
    void generateNormTitle() {
        if (name) {
            normName = name.replaceAll('&',' and ')
            normName = normName.trim()
            normName = normName.toLowerCase()
            normName = alphanum.matcher(normName).replaceAll("")
            normName = normName.replaceAll("\\s+", " ")
            normName = asciify(normName)
        }
    }

    /**
     * Gets an identifier value of the given namespace
     * @param idtype the {@link IdentifierNamespace} to which the required identifier belongs to
     * @return the {@link Identifier}'s value; if multiple, the last identifier's value is being returned (no comment ...)
     */
  String getIdentifierValue(idtype) {
      String result
    ids?.each { ident ->
      if ( ident.ns?.ns?.toLowerCase() == idtype.toLowerCase() )
        result = ident.value
    }
    result
  }

    @Deprecated
    private String stringify(obj) {
      String result
    if ( obj != null ) {
      if ( obj instanceof Date ) {
          SimpleDateFormat df = new SimpleDateFormat('yyyy-MM-dd');
        result = df.format(obj);
      }
      else {
        result = obj.toString()
      }
    }
        result
    }

    /**
     * Gets the first author and / or the first editor of the book instance
     * @return a concatenated string of the first author / first editor of the book
     */
    String getEbookFirstAutorOrFirstEditor(){

        String label = messageSource.getMessage('title.firstAuthor.firstEditor.label',null, LocaleContextHolder.getLocale())
        if(firstEditor && firstAuthor) {
            return firstAuthor + ' ; ' + firstEditor + ' ' + label
        }
        else if(firstAuthor) {
            return firstAuthor
        }
        else if(firstEditor) {
            return firstEditor + ' ' + label
        }
        else return ""
    }

  @Deprecated
  Date getDerivedAccessStartDate() {
    accessStartDate ? accessStartDate : null
  }
  @Deprecated
  Date getDerivedAccessEndDate() {
    accessEndDate ? accessEndDate : null
  }
  @Deprecated
  RefdataValue getAvailabilityStatus() {
    return getAvailabilityStatus(new Date());
  }

  @Deprecated
  String getAvailabilityStatusAsString() {
	  String result
	  def loc = LocaleContextHolder.locale?.toString()
	  Date as_at = new Date()
      Date tipp_access_start_date = getDerivedAccessStartDate()
      Date tipp_access_end_date = getDerivedAccessEndDate()
	  
	  if ( tipp_access_end_date == null ) {
		result = RefdataValue.getByValueAndCategory("Current(*)", RDConstants.TIPP_ACCESS_STATUS).getI10n("value")
	  }
	  else if ( as_at < tipp_access_start_date ) {
		// expected
		result = RefdataValue.getByValueAndCategory("Expected", RDConstants.TIPP_ACCESS_STATUS).getI10n("value")
	  }
	  else if ( as_at > tipp_access_end_date ) {
		// expired
		result = RefdataValue.getByValueAndCategory("Expired", RDConstants.TIPP_ACCESS_STATUS).getI10n("value")
	  }
	  else {
		result = RefdataValue.getByValueAndCategory("Current", RDConstants.TIPP_ACCESS_STATUS).getI10n("value")
	  }
	  result
  }
  
  @Deprecated
  RefdataValue getAvailabilityStatus(Date as_at) {
      RefdataValue result
    // If StartDate <= as_at <= EndDate - Current
    // if Date < StartDate - Expected
    // if Date > EndDate - Expired
      Date tipp_access_start_date = getDerivedAccessStartDate()
      Date tipp_access_end_date = getDerivedAccessEndDate()

    // if ( ( accessEndDate == null ) && ( as_at > tipp_access_end_date ) ) {
    if ( tipp_access_end_date == null ) {
      result = RefdataValue.getByValueAndCategory('Current(*)', RDConstants.TIPP_ACCESS_STATUS)
    }
    else if ( as_at < tipp_access_start_date ) {
      // expected
      result = RefdataValue.getByValueAndCategory('Expected', RDConstants.TIPP_ACCESS_STATUS)
    }
    else if ( as_at > tipp_access_end_date ) {
      // expired
      result = RefdataValue.getByValueAndCategory('Expired', RDConstants.TIPP_ACCESS_STATUS)
    }
    else {
      result = RefdataValue.getByValueAndCategory('Current', RDConstants.TIPP_ACCESS_STATUS)
    }
    result
  }
    @Deprecated
    String getAvailabilityStatusExplanation() {
        return getAvailabilityStatusExplanation(new Date());
    }
    @Deprecated
    String getAvailabilityStatusExplanation(Date as_at) {
        StringWriter sw = new StringWriter()
        sw.write("This tipp is ${getAvailabilityStatus(as_at).value} as at ${as_at} because the date specified was between the start date (${getDerivedAccessStartDate()} ${accessStartDate ? 'Set explicitly on this TIPP' : 'Defaulted from package start date'}) and the end date (${getDerivedAccessEndDate()} ${accessEndDate ? 'Set explicitly on this TIPP' : 'Defaulted from package end date'})");
        return sw.toString();
    }

    /**
     * Compares the controlled properties of two title records.
     * The controlled properties are defined at {@link #controlledProperties}
     * @param tippB the title record to compare with
     * @return the comparison result (-1, 0 or 1)
     */
  int compare(TitleInstancePackagePlatform tippB){
      if(!tippB) return -1
      boolean noChange = true
      controlledProperties.each{ noChange &= this."${it}" == tippB."${it}" }
      
      if( noChange ) return 0
      return 1
      }

    /**
     * Translates the given string into its ASCII representation, i.e. eliminates special chars; required for normalising
     * @param s the string to decode
     * @return the ASCII-decoded string
     */
    private static String asciify(String s) {
        char[] c = s.toCharArray()
        StringBuffer b = new StringBuffer()
        for (char element : c) {
            b.append(translate(element))
        }
        return b.toString()
    }

    /**
     * Translate the given unicode char in the closest ASCII representation
     * NOTE: this function deals only with latin-1 supplement and latin-1 extended code charts
     * @param c the character to translate
     * @return the ASCII representation of the char
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
                return 'a'
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
                return 'c'
            case '\u00D0':
            case '\u00F0':
            case '\u010E':
            case '\u010F':
            case '\u0110':
            case '\u0111':
                return 'd'
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
                return 'e'
            case '\u011C':
            case '\u011D':
            case '\u011E':
            case '\u011F':
            case '\u0120':
            case '\u0121':
            case '\u0122':
            case '\u0123':
                return 'g'
            case '\u0124':
            case '\u0125':
            case '\u0126':
            case '\u0127':
                return 'h'
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
                return 'i'
            case '\u0134':
            case '\u0135':
                return 'j'
            case '\u0136':
            case '\u0137':
            case '\u0138':
                return 'k'
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
                return 'l'
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
                return 'n'
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
                return 'o'
            case '\u0154':
            case '\u0155':
            case '\u0156':
            case '\u0157':
            case '\u0158':
            case '\u0159':
                return 'r'
            case '\u015A':
            case '\u015B':
            case '\u015C':
            case '\u015D':
            case '\u015E':
            case '\u015F':
            case '\u0160':
            case '\u0161':
            case '\u017F':
                return 's'
            case '\u0162':
            case '\u0163':
            case '\u0164':
            case '\u0165':
            case '\u0166':
            case '\u0167':
                return 't'
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
                return 'u'
            case '\u0174':
            case '\u0175':
                return 'w'
            case '\u00DD':
            case '\u00FD':
            case '\u00FF':
            case '\u0176':
            case '\u0177':
            case '\u0178':
                return 'y'
            case '\u0179':
            case '\u017A':
            case '\u017B':
            case '\u017C':
            case '\u017D':
            case '\u017E':
                return 'z'
        }
        return c
    }

    /**
     * Gets the publishers associated to this title
     * @return a {@link List} of publisher {@link Org}s
     */
    List<Org> getPublishers() {
        List<Org> result = []


        orgs.each { or ->
            if ( or.roleType.id in [RDStore.OR_PUBLISHER.id] )
                result << or.org

        }

        result
    }

}

