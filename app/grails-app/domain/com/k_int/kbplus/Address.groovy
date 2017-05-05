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
        zipcode + ' ' + city + ', ' + street_1 + ' ' + street_2 + ' (' + id + '); ' + type?.value
    }
    
    static def lookupOrCreate(street1, street2, postbox, zipcode, city, state, country, type, person, organisation) {
        
        def result = null
        
        def hqlStreet1 = Address.hqlHelper(street1)
        def hqlStreet2 = Address.hqlHelper(street2)
        def hqlPostbox = Address.hqlHelper(postbox)
        def hqlZipcode = Address.hqlHelper(zipcode)
        def hqlCity    = Address.hqlHelper(city)
        def hqlState   = Address.hqlHelper(state)
        def hqlCountry = Address.hqlHelper(country)
        def hqlType    = Address.hqlHelper(type)
        def hqlPrs     = Address.hqlHelper(person)
        def hqlOrg     = Address.hqlHelper(organisation)
        
        def query = "from Address a where a.org ${hqlOrg[1]} and a.prs ${hqlPrs[1]} and a.zipcode ${hqlZipcode[1]} and lower(a.city) ${hqlCity[1]} "
        query = query + "and ((lower(a.street_1) ${hqlStreet1[1]} and a.street_2 ${hqlStreet2[1]}) or a.pob ${hqlPostbox[1]})"
        
        def queryParams = [hqlOrg[0], hqlPrs[0], hqlZipcode[0], hqlCity[0].toLowerCase(), hqlStreet1[0].toLowerCase(), hqlStreet2[0], hqlPostbox[0]]
        queryParams.removeAll([null, ''])
        
        log.debug(query)
        log.debug('@ ' + queryParams)
        
        def a = Address.executeQuery(query, queryParams)
       
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
            // TODO catch multiple results
            if(a.size() > 0){
                result = a[0]
            }
        }        
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
}
