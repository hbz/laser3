package com.k_int.kbplus

import de.laser.base.AbstractI10n
import de.laser.interfaces.CalculatedLastUpdated
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import javax.persistence.Transient

class IdentifierNamespace extends AbstractI10n implements CalculatedLastUpdated {

    @Transient
    def cascadingUpdateService

    static Log static_logger = LogFactory.getLog(IdentifierNamespace)

    @Transient
    public static final String UNKNOWN    = "Unknown"

    @Transient
    public static final NS_ORGANISATION = "com.k_int.kbplus.Org"
    @Transient
    public static final NS_LICENSE      = "com.k_int.kbplus.License"
    @Transient
    public static final NS_SUBSCRIPTION = "com.k_int.kbplus.Subscription"
    @Transient
    public static final NS_PACKAGE      = "com.k_int.kbplus.Package"
    @Transient
    public static final NS_TITLE        = "com.k_int.kbplus.TitleInstance"
    @Transient
    public static final NS_CREATOR      = "com.k_int.kbplus.Creator"

    @Transient
    public static final String ISIL       = "ISIL"
    @Transient
    public static final String WIBID      = "wibid"
    @Transient
    public static final String GND_ORG_NR = "gnd_org_nr"
    @Transient
    public static final String EZB_ORG_ID = "ezb_org_id"
    @Transient
    public static final String GRID_ID    = "GRID ID"
    @Transient
    public static final String DBS_ID     = "DBS-ID"
    @Transient
    public static final String VAT        = "VAT"

    //title identifier namespaces
    @Transient
    public static final String ZDB        = 'zdb'
    @Transient
    public static final String ZDB_PPN    = 'zdb_ppn'
    @Transient
    public static final String DOI        = 'doi'
    @Transient
    public static final String ISSN       = 'issn'
    @Transient
    public static final String EISSN      = 'eissn'
    @Transient
    public static final String PISBN      = 'pisbn'
    @Transient
    public static final String ISBN       = 'isbn'

    //subscription identifier namespaces
    @Transient
    public static final String EZB_ANCHOR = 'EZB anchor'
    @Transient
    public static final String EZB_COLLECTION_ID = 'ezb_collection_id'
    @Transient
    public static final String ISIL_PAKETSIGEL = 'ISIL_Paketsigel'
    @Transient
    public static final String ISCI = 'ISCI'

    @Transient
    final static List<String> CORE_ORG_NS = [
            ISIL,
            WIBID,
            GND_ORG_NR,
            EZB_ORG_ID,
            GRID_ID,
            DBS_ID,
            VAT
    ]

    @Transient
    final static List<String> CORE_TITLE_NS = [
            ZDB, ZDB_PPN, DOI, ISSN, EISSN, PISBN, ISBN, ISIL_PAKETSIGEL, ISCI, EZB_ANCHOR, EZB_COLLECTION_ID
    ]

    @Transient
    final static String[] AVAILABLE_NSTYPES = [
            NS_ORGANISATION,
            NS_LICENSE,
            NS_SUBSCRIPTION,
            NS_PACKAGE,
            NS_TITLE,
            NS_CREATOR
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

        isUnique        (blank:false)
        isHidden        (blank:false)

        // Nullable is true, because values are already in the database
        dateCreated (nullable: true, blank: false)
        lastUpdated (nullable: true, blank: false)
        lastUpdatedCascading (nullable: true, blank: false)
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

    Date getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    boolean isCoreOrgNamespace(){
        this.ns in CORE_ORG_NS
    }
}
