package de.laser

import de.laser.base.AbstractI10n
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.wekb.Package
import de.laser.wekb.TitleInstancePackagePlatform
import groovy.util.logging.Slf4j

/**
 * This is the class to group identifiers by their vocabulary type. The namespace defines moreover the type of object the
 * identifiers are assigned; above that, the namespace configuration defines uniqueness of the identifier (i.e. only zero or one
 * identifier of the given namespace may be assigned to the same object). Identifier namespaces may come from external APIs (currently
 * only the we:kb serves as source for non-LAS:eR namespaces); the flag {@link #isFromLaser} indicates the origin of the namespace (and
 * usually also the identifiers belonging to this namespace). Alternatively, identifier namespaces may be soft-entered (that via the view
 * {@link AdminController#manageNamespaces()}) in the database or hard-coded in the method {@link BootStrapService#setIdentifierNamespace()}.
 * Identifier namespaces belong to the internationalisable classes; the name and description strings may be translated to German
 */
@Slf4j
class IdentifierNamespace extends AbstractI10n implements CalculatedLastUpdated {

    public static final String UNKNOWN    = "Unknown"

    public static final NS_ORGANISATION = Org.class.name
    public static final NS_LICENSE      = License.class.name
    public static final NS_SUBSCRIPTION = Subscription.class.name
    public static final NS_PACKAGE      = Package.class.name
    public static final NS_TITLE        = TitleInstancePackagePlatform.class.name

    //organisation identifier namespaces
    public static final String CROSSREF_FUNDER_ID = "crossref funder id"
    public static final String DBPEDIA     = "dbpedia"
    public static final String DBS_ID      = "DBS-ID"
    public static final String DBIS_ORG_ID = "dbis_org_id"
    public static final String DEAL_ID     = "deal_id"
    public static final String EZB_ORG_ID  = "ezb_org_id"
    public static final String GND_ORG_NR  = "gnd_org_nr"
    public static final String ISIL        = "ISIL"
    public static final String ISNI        = "isni"
    public static final String RINGGOLD_ID = "ringgold"
    public static final String ROR_ID      = "ROR ID"
    public static final String LEIT_ID     = "Leitweg-ID"
    public static final String LEIT_KR     = "Leitkriterium (intern)"
    public static final String LEIBNIZ_ID  = "leibniz"
    public static final String LOC_ID      = "loc id"
    public static final String PEPPOL_RECEIVER_ID     = "Peppol-Receiver-ID"
    public static final String VAT         = "VAT"
    public static final String VIAF        = "viaf"
    public static final String WIBID       = "wibid"
    public static final String WIKIDATA_ID = "wikidata id"

    //title identifier namespaces
    public static final String ZDB        = 'zdb'
    public static final String EZB        = 'ezb'
    public static final String ZDB_PPN    = 'zdb_ppn'
    public static final String DOI        = 'doi'
    public static final String ISSN       = 'issn'
    public static final String EISSN      = 'eissn'
    public static final String EISBN      = 'eisbn'
    public static final String ISBN       = 'isbn'
    public static final String TITLE_ID   = 'title_id'

    //package identifier namespaces
    public static final String PKG_ID        = 'Anbieter_Produkt_ID'

    //subscription identifier namespaces
    public static final String EZB_ANCHOR = 'ezb_anchor'
    public static final String EZB_COLLECTION_ID = 'ezb_collection_id'
    public static final String EZB_SUB_ID = 'ezb_sub_id'
    public static final String ISIL_PAKETSIGEL = 'package_isil'
    public static final String ISCI = 'ISCI'

    /**
     * Set of identifiers every institution (!) must have; this does not count for providers and agencies.
     * They get identifiers, too, but are set to empty value and are never shown
     */
    public static final List<String> CORE_ORG_NS = [
            ISIL,
            ISNI,
            WIBID,
            GND_ORG_NR,
            EZB_ORG_ID,
            ROR_ID,
            DBS_ID,
            DBIS_ORG_ID,
            DEAL_ID,
            VAT,
            RINGGOLD_ID,
            WIKIDATA_ID,
            LEIT_ID,
            LEIT_KR,
            LEIBNIZ_ID,
            PEPPOL_RECEIVER_ID
    ]

    /**
     * Set of identifiers every provider or agency (i.e. commercial organisation) must have
     */
    public static final List<String> CORE_PROVIDER_NS = [
            CROSSREF_FUNDER_ID,
            DBPEDIA,
            GND_ORG_NR,
            ISNI,
            LOC_ID,
            ROR_ID,
            VIAF,
            WIKIDATA_ID
    ]

    /**
     * Set of identifiers which are common to most {@link TitleInstancePackagePlatform}s; they are KBART headers and need all to be processed.
     * EBooks do not have ZDB IDs, EZB IDs nor ISSNs
     */
    public static final List<String> CORE_TITLE_NS = [
            ZDB, ZDB_PPN, DOI, ISSN, EISSN, EISBN, ISBN, ISIL_PAKETSIGEL, ISCI, EZB_ANCHOR, EZB_COLLECTION_ID
    ]

    public static final String[] AVAILABLE_NSTYPES = [
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

    Boolean isHidden    = false
    Boolean isUnique    = false
    Boolean isFromLaser = false
    Boolean isHardData  = false

    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static transients = ['coreOrgNamespace'] // mark read-only accessor methods

    static mapping = {
        id              column:'idns_id'
        version         column:'idns_version'

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
        isFromLaser     column:'idns_is_from_laser'
        isHardData      column:'idns_is_hard_data'

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

        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    def afterInsert() {
        log.debug("afterInsert")
        BeanStore.getCascadingUpdateService().update(this, dateCreated)
    }
    def afterUpdate() {
        log.debug("afterUpdate")
        BeanStore.getCascadingUpdateService().update(this, lastUpdated)
    }
    def afterDelete() {
        log.debug("afterDelete")
        BeanStore.getCascadingUpdateService().update(this, new Date())
    }

    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    /**
     * Sets up a new identifier namespace with the given parameter map
     * @param configMap the parameter {@link Map} defining the new namespace's properties
     * @return the new or updated identifier namespace
     */
    static IdentifierNamespace construct(Map<String, Object> configMap) {
        withTransaction {
            IdentifierNamespace idns
            if(configMap.nsType)
                idns = IdentifierNamespace.findByNsAndNsType(configMap.ns, configMap.nsType)
            else
                idns = IdentifierNamespace.findByNs(configMap.ns)
            if(!idns) {
                idns = new IdentifierNamespace([ns: configMap.ns, nsType: configMap.nsType, isFromLaser: configMap.isFromLaser] )
            }
            idns.name_de = configMap.name_de
            idns.description_de = configMap.description_de
            idns.name_en = configMap.name_en
            idns.description_en = configMap.description_en
            idns.isUnique = configMap.isUnique
            idns.isHidden = configMap.isHidden
            idns.isHardData = configMap.isHardData
            idns.validationRegex = configMap.validationRegex
            idns.urlPrefix = configMap.urlPrefix
            if(!idns.save())
                log.error(idns.getErrors().getAllErrors().toListString())
            idns
        }
    }

    /**
     * Checks whether this identifier namespace is among the core {@link Org} namespaces
     * @return is this identifier namespace a core organisation namespace?
     */
    boolean isCoreOrgNamespace(){
        this.ns in CORE_ORG_NS
    }
}
