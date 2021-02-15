package de.laser


import de.laser.base.AbstractBase
import de.laser.finance.PriceItem
import de.laser.helper.RDConstants
import de.laser.annotations.RefdataAnnotation
import de.laser.titles.TitleHistoryEvent
import de.laser.titles.TitleHistoryEventParticipant
import groovy.time.TimeCategory
import org.springframework.context.i18n.LocaleContextHolder

import javax.persistence.Transient
import java.text.Normalizer
import java.text.SimpleDateFormat
import java.util.regex.Pattern

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
    String sortName
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

    String hostPlatformURL

    Date dateCreated
    Date lastUpdated

  static mappedBy = [ids: 'tipp',
                     orgs: 'tipp',
                     prsLinks: 'tipp',
                     priceItems: 'tipp',
                     historyEvents: 'tipp']
  static hasMany = [ids: Identifier,
                    coverages: TIPPCoverage,
                    orgs: OrgRole,
                    historyEvents: TitleHistoryEvent,
                    prsLinks: PersonRole,
                    priceItems: PriceItem]

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
        // rectype column:'tipp_rectype'
           version column:'tipp_version'
               pkg column:'tipp_pkg_fk',    index: 'tipp_idx'
          platform column:'tipp_plat_fk',   index: 'tipp_idx'
          // title column:'tipp_ti_fk',     index: 'tipp_idx'
         titleType column:'tipp_title_type'
            medium column:'tipp_medium_rv_fk'
              name column:'tipp_name', type: 'text'
          sortName column:'tipp_sort_name', type: 'text'
          normName column:'tipp_norm_name', type: 'text'
        seriesName column:'tipp_series_name', type: 'text'
           imprint column:'tipp_imprint', type: 'text'
  subjectReference column:'tipp_subject_reference', type: 'text'
            gokbId column:'tipp_gokb_id'
            status column:'tipp_status_rv_fk'
         delayedOA column:'tipp_delayedoa_rv_fk'
          hybridOA column:'tipp_hybridoa_rv_fk'
      statusReason column:'tipp_status_reason_rv_fk'
           payment column:'tipp_payment_rv_fk'
            option column:'tipp_option_rv_fk'
   hostPlatformURL column:'tipp_host_platform_url', type: 'text'
      accessStartDate column:'tipp_access_start_date'
      accessEndDate column:'tipp_access_end_date'
    //coreStatusStart column:'tipp_core_status_start_date'
    //coreStatusEnd column:'tipp_core_status_end_date'
      dateFirstInPrint column:'tipp_date_first_in_print'
      dateFirstOnline column:'tipp_date_first_online'
      summaryOfContent column:'tipp_summary_of_content'
      volume column:'tipp_volume'
      firstEditor column: 'tipp_first_editor'
      firstAuthor column: 'tipp_first_author'
      editionNumber column: 'tipp_edition_number'
      editionStatement column: 'tipp_edition_statement'
      editionDifferentiator column: 'tipp_edition_differentiator'
      dateCreated column: 'tipp_date_created'
      lastUpdated column: 'tipp_last_updated'

      ids                   batchSize: 10
    //additionalPlatforms   batchSize: 10
      coverages             batchSize: 10, sort: 'startDate', order: 'asc'
      priceItems            batchSize: 10, sort: 'startDate', order: 'asc'
  }

    static constraints = {
        globalUID(nullable:true, blank:false, unique:true, maxSize:255)
        gokbId (blank:false, unique: true, maxSize:511)
        status      (nullable:true)
        name        (nullable:true)
        sortName    (nullable:true)
        normName    (nullable:true)
        seriesName  (nullable:true)
        imprint     (nullable:true)
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
      //coreStatusStart (nullable:true)
      //coreStatusEnd (nullable:true)
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

  @Transient
  void touchPkgLastUpdated() {
    if(pkg!=null){
      use(TimeCategory) {
        pkg.lastUpdated += 1.seconds
      }
      pkg.save(failOnError:true)
    }
  }

    void generateSortTitle() {
        if ( name ) {
            sortName = Normalizer.normalize(name, Normalizer.Form.NFKD).trim().toLowerCase()
            sortName = sortName.replaceFirst('^copy of ', '')
            sortName = sortName.replaceFirst('^the ', '')
            sortName = sortName.replaceFirst('^a ', '')
            sortName = sortName.replaceFirst('^der ', '')
            sortName = sortName.replaceFirst('^die ', '')
            sortName = sortName.replaceFirst('^das ', '')
        }
    }

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

  String getIdentifierValue(idtype) {
      String result
    ids?.each { ident ->
      if ( ident.ns?.ns?.toLowerCase() == idtype.toLowerCase() )
        result = ident.value
    }
    result
  }

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

  Date getDerivedAccessStartDate() {
    accessStartDate ? accessStartDate : pkg?.startDate
  }

  Date getDerivedAccessEndDate() {
    accessEndDate ? accessEndDate : pkg?.endDate
  }

  RefdataValue getAvailabilityStatus() {
    return getAvailabilityStatus(new Date());
  }
  
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

    String getAvailabilityStatusExplanation() {
        return getAvailabilityStatusExplanation(new Date());
    }

    String getAvailabilityStatusExplanation(Date as_at) {
        StringWriter sw = new StringWriter()
        sw.write("This tipp is ${getAvailabilityStatus(as_at).value} as at ${as_at} because the date specified was between the start date (${getDerivedAccessStartDate()} ${accessStartDate ? 'Set explicitly on this TIPP' : 'Defaulted from package start date'}) and the end date (${getDerivedAccessEndDate()} ${accessEndDate ? 'Set explicitly on this TIPP' : 'Defaulted from package end date'})");
        return sw.toString();
    }
  /**
   * Compare the controlledPropertie of two tipps
  **/
  int compare(TitleInstancePackagePlatform tippB){
      if(!tippB) return -1
      boolean noChange = true
      controlledProperties.each{ noChange &= this."${it}" == tippB."${it}" }
      
      if( noChange ) return 0
      return 1
      }

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

}

