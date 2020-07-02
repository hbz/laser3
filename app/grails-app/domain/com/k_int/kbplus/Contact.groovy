package com.k_int.kbplus

import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation
import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory

@Log4j
class Contact implements Comparable<Contact>{
    private static final String REFDATA_PHONE = "Phone"
    private static final String REFDATA_FAX =   "Fax"
    private static final String REFDATA_MAIL =  "Mail"
    private static final String REFDATA_EMAIL = "E-Mail"
    private static final String REFDATA_URL =   "Url"

    String       content
    Person       prs            // person related contact; exclusive with org
    Org          org            // org related contact; exclusive with prs

    Date dateCreated
    Date lastUpdated

    @RefdataAnnotation(cat = RDConstants.CONTACT_CONTENT_TYPE)
    RefdataValue contentType

    @RefdataAnnotation(cat = RDConstants.CONTACT_TYPE)
    RefdataValue type
    
    static mapping = {
        cache  true
        id          column:'ct_id'
        version     column:'ct_version'
        content     column:'ct_content'
        contentType column:'ct_content_type_rv_fk'
        type        column:'ct_type_rv_fk'
        prs         column:'ct_prs_fk', index: 'ct_prs_idx'
        org         column:'ct_org_fk', index: 'ct_org_idx'

        dateCreated column: 'ct_date_created'
        lastUpdated column: 'ct_last_updated'
    }
    
    static constraints = {
        content     (nullable:true, blank:true)
        contentType (nullable:true, blank:true)
        type        ()
        prs         (nullable:true)
        org         (nullable:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true, blank: false)
        dateCreated (nullable: true, blank: false)
    }
    
    static List<RefdataValue> getAllRefdataValues(String category) {
        RefdataCategory.getAllRefdataValues(category)
    }
    
    @Override
    String toString() {
        contentType?.value + ', ' + content + ' (' + id + '); ' + type?.value
    }

    static Contact lookup(content, contentType, type, person, organisation) {

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

    static Contact lookupOrCreate(content, contentType, type, person, organisation) {

        Contact result
        String info = "saving new contact: ${content} ${contentType} ${type}"

        if (! content) {
            LogFactory.getLog(this).debug( info + " > ignored; empty content")
            return
        }

        if(person && organisation){
            type = RefdataValue.getByValue("Job-related")
        }
        
        Contact check = Contact.lookup(content, contentType, type, person, organisation)
        if (check) {
            result = check
            info += " > ignored/duplicate"
        }
        else{
            result = new Contact(
                content:     content,
                contentType: contentType,
                type:        type,
                prs:         person,
                org:         organisation
                )
                
            if(!result.save()){
                result.errors.each{ println it }
            }
            else {
                info += " > OK"
            }
        }
        
        LogFactory.getLog(this).debug(info)
        result  
    }
    
    /**
     *
     * @param obj
     * @return list with two elements for building hql query
     */
    static List hqlHelper(obj){
        
        def result = []
        result.add(obj ? obj : '')
        result.add(obj ? '= ?' : 'is null')
        
        result
    }

    @Override
    int compareTo(Contact contact) {
        int result
        result = getCompareOrderValueForType(this).compareTo(getCompareOrderValueForType(contact))
        if (result == 0) {
            String a = this.getContent() ?: ''
            String b = contact.getContent() ?: ''
            result = a.compareTo(b)
        }
        return result
    }

    private int getCompareOrderValueForType(Contact contact){
        switch (contact?.getContentType()?.getValue()){
            case REFDATA_EMAIL:
            case REFDATA_MAIL:
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
