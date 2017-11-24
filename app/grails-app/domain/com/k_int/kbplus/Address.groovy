package com.k_int.kbplus

import java.util.List

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
    RefdataValue state      // RefdataCategory 'Federal State'
    RefdataValue country    // RefdataCategory 'Country'
    RefdataValue type       // RefdataCategory 'AddressType'
    Person prs          // person related contact; exclusive with org
    Org    org          // org related contact; exclusive with prs
    
    static mapping = {
        id       column:'adr_id'
        version  column:'adr_version'
        street_1 column:'adr_street_1'
        street_2 column:'adr_street_2'
        pob      column:'adr_pob'
        zipcode  column:'adr_zipcode'
        city     column:'adr_city'
        state    column:'adr_state_rv_fk'
        country  column:'adr_country_rv_fk'
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
        state    (nullable:true,  blank:true)
        country  (nullable:true,  blank:true)
        type     (nullable:false)
        prs      (nullable:true)
        org      (nullable:true)
    }
    
    static getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues('AddressType')
    }
    
    @Override
    String toString() {
        zipcode + ' ' + city + ', ' + street_1 + ' ' + street_2 + ' (' + id + '); ' + type?.value
    }
    
    // TODO implement existing check (lookup)
    static def lookupOrCreate(street1, street2, postbox, zipcode, city, state, country, type, person, organisation) {
        
        def info   = "saving new address: ${type}"
        def result = null
        
        if(person && organisation){
            type = RefdataValue.findByValue("Job-related")
        }

        def check = Address.findAllWhere(
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
        ).sort({id: 'asc'})
            
        if(check.size()>0){
            result = check.get(0)
            info += " > ignored/duplicate"
        }
        else{
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
            else {
                info += " > ok"
            }
        }
             
        LogFactory.getLog(this).debug(info)
        result   
    }
}
