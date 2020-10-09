package de.laser


import de.laser.titles.TitleInstance
import de.laser.base.AbstractI10n
import de.laser.interfaces.CalculatedLastUpdated
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class IdentifierNamespace extends AbstractI10n implements CalculatedLastUpdated {

    def cascadingUpdateService

    static Log static_logger = LogFactory.getLog(IdentifierNamespace)

    public static final String UNKNOWN    = "Unknown"

    public static final NS_ORGANISATION = Org.class.name
    public static final NS_LICENSE      = License.class.name
    public static final NS_SUBSCRIPTION = Subscription.class.name
    public static final NS_PACKAGE      = Package.class.name
    public static final NS_TITLE        = TitleInstance.class.name

    public static final String ISIL       = "ISIL"
    public static final String WIBID      = "wibid"
    public static final String GND_ORG_NR = "gnd_org_nr"
    public static final String EZB_ORG_ID = "ezb_org_id"
    public static final String GRID_ID    = "GRID ID"
    public static final String DBS_ID     = "DBS-ID"
    public static final String VAT        = "VAT"

    //title identifier namespaces
    public static final String ZDB        = 'zdb'
    public static final String ZDB_PPN    = 'zdb_ppn'
    public static final String DOI        = 'doi'
    public static final String ISSN       = 'issn'
    public static final String EISSN      = 'eissn'
    public static final String PISBN      = 'pisbn'
    public static final String ISBN       = 'isbn'

    //subscription identifier namespaces
    public static final String EZB_ANCHOR = 'EZB anchor'
    public static final String EZB_COLLECTION_ID = 'ezb_collection_id'
    public static final String ISIL_PAKETSIGEL = 'ISIL_Paketsigel'
    public static final String ISCI = 'ISCI'

    final static List<String> CORE_ORG_NS = [
            ISIL,
            WIBID,
            GND_ORG_NR,
            EZB_ORG_ID,
            GRID_ID,
            DBS_ID,
            VAT
    ]

    final static List<String> CORE_TITLE_NS = [
            ZDB, ZDB_PPN, DOI, ISSN, EISSN, PISBN, ISBN, ISIL_PAKETSIGEL, ISCI, EZB_ANCHOR, EZB_COLLECTION_ID
    ]

    final static String[] AVAILABLE_NSTYPES = [
            NS_ORGANISATION,
            NS_LICENSE,
            NS_SUBSCRIPTION,
            NS_PACKAGE,
            NS_TITLE
    ]

    String ns
    String nsType
    String family
    String urlPrefix
    String validationRegex

    String name_de
    String name_en
    String description_de
    String description_en

    Boolean isHidden = false
    Boolean isUnique = false

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static transients = ['coreOrgNamespace'] // mark read-only accessor methods

    static mapping = {
        id              column:'idns_id'

        ns              column:'idns_ns'
        nsType          column:'idns_type'
        family          column:'idns_family'
        urlPrefix       column:'idns_url_prefix'
        validationRegex column:'idns_val_regex'

        name_de         column:'idns_name_de'
        name_en         column:'idns_name_en'
        description_de  column:'idns_description_de',   type: 'text'
        description_en  column:'idns_description_en',   type: 'text'

        isHidden        column:'idns_is_hidden'
        isUnique        column:'idns_is_unique'

        dateCreated column: 'idns_date_created'
        lastUpdated column: 'idns_last_updated'
        lastUpdatedCascading column: 'idns_last_updated_cascading'
    }

    static constraints = {
        ns              (blank:false) // TODO: constraint
        nsType          (nullable:true, blank:false)
        family          (nullable:true, blank:false)
        urlPrefix       (nullable:true, blank:false)
        validationRegex (nullable:true, blank:false)

        name_de         (nullable:true, blank:false)
        name_en         (nullable:true, blank:false)
        description_de  (nullable:true, blank:false)
        description_en  (nullable:true, blank:false)

        // Nullable is true, because values are already in the database
        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    def afterInsert() {
        static_logger.debug("afterInsert")
        cascadingUpdateService.update(this, dateCreated)
    }
    def afterUpdate() {
        static_logger.debug("afterUpdate")
        cascadingUpdateService.update(this, lastUpdated)
    }
    def afterDelete() {
        static_logger.debug("afterDelete")
        cascadingUpdateService.update(this, new Date())
    }

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    boolean isCoreOrgNamespace(){
        this.ns in CORE_ORG_NS
    }
}
