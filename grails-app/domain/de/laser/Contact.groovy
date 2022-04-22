package de.laser


import de.laser.storage.RDConstants
import de.laser.annotations.RefdataInfo
import groovy.util.logging.Slf4j
import org.apache.commons.logging.LogFactory

/**
 * A contact address for a given {@link Person} or {@link Org}. Note that physical addresses are stored as {@link Address}es;
 * the more appropriate name for this domain would be ContactDetails because the Person domain class represents the actual contact
 * @see Address
 * @see Person
 */
@Slf4j
class Contact implements Comparable<Contact>{

    private static final String REFDATA_PHONE = "Phone"
    private static final String REFDATA_FAX =   "Fax"
    private static final String REFDATA_MAIL =  "Mail"
    private static final String REFDATA_EMAIL = "E-Mail"
    private static final String REFDATA_URL =   "Url"

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
     * Looks up a contact with the given arguments
     * @param content the contact data (phone number, email address, fax number) to look for
     * @param contentType the type of contact data (one of the {@link RefdataValue}s of the category CONTACT_CONTENT_TYPE)
     * @param type the type of the contact itself
     * @param person the {@link Person} to which the contact is linked to
     * @param organisation the {@link Org} to which the contact is linked to
     * @return the contact or null if not found
     */
    static Contact lookup(String content, RefdataValue contentType, RefdataValue type, Person person, Org organisation) {

        Contact contact
        List<Contact>  check = Contact.findAllWhere(
                content: content ?: null,
                contentType: contentType,
                type: type,
                prs: person,
                org: organisation
        ).sort({id: 'asc'})

        if (check.size() > 0) {
            contact = check.get(0)
        }
        contact
    }

    /**
     * Looks up a contact with the given arguments or creates if it does not exist
     * @param content the contact data (phone number, email address, fax number) to look for
     * @param contentType the type of contact data (one of the {@link RefdataValue}s of the category CONTACT_CONTENT_TYPE)
     * @param type the type of the contact itself
     * @param person the {@link Person} to which the contact is linked to
     * @param organisation the {@link Org} to which the contact is linked to
     * @return the contact
     */
    static Contact lookupOrCreate(String content, RefdataValue contentType, RefdataValue type, Person person, Org organisation) {

        withTransaction {
            Contact result
            String info = "saving new contact: ${content} ${contentType} ${type}"

            if (!content) {
                LogFactory.getLog(this).debug(info + " > ignored; empty content")
                return
            }

            if (person && organisation) {
                type = RefdataValue.getByValue("Job-related")
            }

            Contact check = Contact.lookup(content, contentType, type, person, organisation)
            if (check) {
                result = check
                info += " > ignored/duplicate"
            }
            else {
                result = new Contact(
                        content: content,
                        contentType: contentType,
                        type: type,
                        prs: person,
                        org: organisation
                )

                if (! result.save()) {
                    result.errors.each { println it }
                }
                else {
                    info += " > OK"
                }
            }

            LogFactory.getLog(this).debug(info)
            result
        }
    }

    /**
     * Compares this contact to the given contact
     * @param contact the contact to compare against
     * @return the comparison result (-1, 0, 1), based on the contact type, then, on content
     */
    @Override
    int compareTo(Contact contact) {
        int result = getCompareOrderValueForType(this).compareTo(getCompareOrderValueForType(contact))
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
    private int getCompareOrderValueForType(Contact contact){
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
