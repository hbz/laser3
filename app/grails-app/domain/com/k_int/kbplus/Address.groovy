package com.k_int.kbplus

import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory
import groovy.util.logging.*

@Log4j
class Address {
    
    String street_1
    String street_2
    String pob
    String zipcode
    String city
    String state
    String country
    RefdataValue type
    Person prs
    Org    org
    
    static mapping = {
        id       column:'adr_id'
        version  column:'adr_version'
        street_1 column:'adr_street_1'
        street_2 column:'adr_street_2'
        pob      column:'adr_pob'
        zipcode  column:'adr_zipcode'
        city     column:'adr_city'
        state    column:'adr_state'
        country  column:'adr_country'
        type     column:'adr_type_rv_fk'
        prs      column:'adr_prs_fk'
        org      column:'adr_org_fk'
    }
    
    static constraints = {
        street_1 (nullable:false, blank:false)
        street_2 (nullable:true,  blank:true)
        pob      (nullable:true,  blank:true)
        zipcode  (nullable:false, blank:false)
        city     (nullable:false, blank:false)
        state    (nullable:false, blank:true)
        country  (nullable:false, blank:true)
        type     (nullable:false)
        prs      (nullable:true)
        org      (nullable:true)
    }
    
    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('AddressType')
    }
    
    @Override
    String toString() {
        zipcode + ' ' + city + ', ' + street_1 + ' ' + street_2 + ' (' + id + ')'
    }
    
    static def lookupOrCreate(street1, street2, postbox, zipcode, city, state, country, type, person, organisation) {
        
        def result = null
        
        street1      = street1 ? street1 : ''
        street2      = street2 ? street2 : ''
        postbox      = postbox ? postbox : ''
        zipcode      = zipcode ? zipcode : ''
        city         = city    ? city    : ''
        state        = state   ? state   : ''
        country      = country ? country : ''
        type         = type    ? type : null
        person       = person  ? person : null
        organisation = organisation ? organisation : null
        
        def a = Address.executeQuery(
            "from Address a where a.zipcode = ? and lower(a.city) = ? and ((lower(a.street_1) = ? and a.street_2 = ?) or a.pob = ?)",
            [zipcode, city.toLowerCase(), street1.toLowerCase(), street2, postbox]
            )
       
        if(!a && type){
            LogFactory.getLog(this).debug('trying to save new address')
            
            result = new Address(
                street_1: street1,
                street_2: street2,
                pob:      postbox,
                zipcode:  zipcode,
                city:     city,
                state:    state,
                country:  country,
                type:     type,
                prs:      person,
                org:      organisation
                )
                
            if(!result.save()){
                result.errors.each{ println it }
            }
        }
        else {
            result = a
        }
        
        result
       
    }
}
