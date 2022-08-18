package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import groovy.util.logging.Slf4j

/**
 * A contact address for a given {@link Person} or {@link Org}. Note that physical addresses are stored as {@link Address}es;
 * the more appropriate name for this domain would be ContactDetails because the Person domain class represents the actual contact
 * @see Address
 * @see Person
 */
@Slf4j
class Contact implements Comparable<Contact>{

    public static final String REFDATA_PHONE = "Phone"
    public static final String REFDATA_FAX =   "Fax"
    public static final String REFDATA_MAIL =  "Mail"
    public static final String REFDATA_EMAIL = "E-Mail"
    public static final String REFDATA_URL =   "Url"

    String content
    Person prs            // person related contact; exclusive with org
    Org    org            // org related contact; exclusive with prs

    Date dateCreated
    Date lastUpdated

    @RefdataInfo(cat = RDConstants.CONTACT_CONTENT_TYPE)
    RefdataValue contentType

    @RefdataInfo(cat = RDConstants.CONTACT_TYPE)
    RefdataValue type

    @RefdataInfo(cat = RDConstants.LANGUAGE_ISO)
    RefdataValue language
    
    static mapping = {
        id          column:'ct_id'
        version     column:'ct_version'
        content     column:'ct_content'
        contentType column:'ct_content_type_rv_fk'
        type        column:'ct_type_rv_fk'
        language    column:'ct_language_rv_fk'
        prs         column:'ct_prs_fk', index: 'ct_prs_idx'
        org         column:'ct_org_fk', index: 'ct_org_idx'

        dateCreated column: 'ct_date_created'
        lastUpdated column: 'ct_last_updated'
    }
    
    static constraints = {
        content     (nullable:true)
        contentType (nullable:true)
        prs         (nullable:true)
        org         (nullable:true)

        // Nullable is true, because values are already in the database
        language    (nullable: true)
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }

    /**
     * Maps to {@link RefdataCategory#getAllRefdataValues()}
     * @param category the category (one of {@link RDConstants#CONTACT_CONTENT_TYPE} or {@link RDConstants#CONTACT_TYPE}) to look for
     * @return a {@link List} of all reference data values of the given category
     */
    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }

    /**
     * Outputs the contact address as human-readable string
     * @return the contact as concatenated string
     */
    @Override
    String toString() {
        contentType?.value + ', ' + content + ' (' + id + '); ' + type?.value
    }

    /**
     * Compares this contact to the given contact
     * @param contact the contact to compare against
     * @return the comparison result (-1, 0, 1), based on the contact type, then, on content
     */
    @Override
    int compareTo(Contact contact) {
        int result = _getCompareOrderValueForType(this).compareTo(_getCompareOrderValueForType(contact))
        if (result == 0) {
            String a = this.getContent() ?: ''
            String b = contact.getContent() ?: ''
            result = a.compareTo(b)
        }
        return result
    }

    /**
     * Gets to a given contact an integer comparison value of its type
     * @param contact the contact which should be compared
     * @return for REFDATA_EMAIL or REFDATA_MAIL: 1, REFDATA_URL: 2, REFDATA_PHONE: 3, REFDATA_FAX: 4, 5 otherwise
     */
    private int _getCompareOrderValueForType(Contact contact){
        switch (contact?.getContentType()?.getValue()){
            case [ REFDATA_MAIL, REFDATA_EMAIL ]:
                return 1;
            case REFDATA_URL:
                return 2;
            case REFDATA_PHONE:
                return 3;
            case REFDATA_FAX:
                return 4;
            default:
                return 5;
        }
    }

}
