package com.k_int.kbplus

import java.util.List

import groovy.util.logging.Log4j

import org.apache.commons.logging.LogFactory

import groovy.util.logging.*

@Log4j
class Address {

    String street_1
    String street_2
    String zipcode
    String city

    String pob
    String pobZipcode
    String pobCity

    RefdataValue state      // RefdataCategory 'Federal State'
    RefdataValue country    // RefdataCategory 'Country'
    RefdataValue type       // RefdataCategory 'AddressType'

    String name
    String additionFirst
    String additionSecond

    Person prs              // person related contact; exclusive with org
    Org    org              // org related contact; exclusive with prs
    
    static mapping = {
        id       column:'adr_id'
        version  column:'adr_version'
        street_1 column:'adr_street_1'
        street_2 column:'adr_street_2'
        pob      column:'adr_pob'
        pobZipcode   column:'adr_pob_zipcode'
        pobCity      column:'adr_pob_city'
        zipcode  column:'adr_zipcode'
        city     column:'adr_city'
        state    column:'adr_state_rv_fk'
        country  column:'adr_country_rv_fk'
        name     column:'adr_name'
        additionFirst   column:'adr_addition_first'
        additionSecond  column:'adr_addition_second'
        type     column:'adr_type_rv_fk'
        prs      column:'adr_prs_fk'
        org      column:'adr_org_fk'
    }
    
    static constraints = {
        street_1 (nullable:true,  blank:false)
        street_2 (nullable:true,  blank:false)
        pob         (nullable:true,  blank:false)
        pobZipcode  (nullable:true,  blank:false)
        pobCity     (nullable:true,  blank:false)
        zipcode  (nullable:true, blank:false)
        city     (nullable:true, blank:false)
        state    (nullable:true,  blank:false)
        country  (nullable:true,  blank:false)
        name            (nullable:true,  blank:false)
        additionFirst   (nullable:true,  blank:false)
        additionSecond  (nullable:true,  blank:false)
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

    static def lookup(
            name,
            street1,
            street2,
            zipcode,
            city,
            state,
            country,
            postbox,
            pobZipcode,
            pobCity,
            type,
            person,
            organisation) {

        def address
        def check = Address.findAllWhere(
                name:           name ?: null,
                street_1: street1 ?: null,
                street_2: street2 ?: null,
                zipcode:  zipcode ?: null,
                city:     city ?: null,
                state:    state ?: null,
                country:  country ?: null,
                pob:            postbox ?: null,
                pobZipcode:     pobZipcode ?: null,
                pobCity:        pobCity ?: null,
                type:     type ?: null,
                prs:      person,
                org:      organisation
        ).sort({id: 'asc'})

        if (check.size() > 0) {
            address = check.get(0)
        }
        address
    }

    static def lookupOrCreate(
            name,
            street1,
            street2,
            zipcode,
            city,
            state,
            country,
            postbox,
            pobZipcode,
            pobCity,
            type,
            person,
            organisation) {
        
        def info   = "saving new address: ${type}"
        def result = null
        
        if (person && organisation) {
            type = RefdataValue.findByValue("Job-related")
        }

        def check = Address.lookup(name, street1, street2, zipcode, city, state, country, postbox, pobZipcode, pobCity, type, person, organisation)
        if (check) {
            result = check
            info += " > ignored; duplicate found"
        }
        else {
            result = new Address(
                name:     name,
                street_1: street1,
                street_2: street2,
                zipcode:  zipcode,
                city:     city,
                state:    state,
                country:  country,
                pob:      postbox,
                pobZipcode: pobZipcode,
                pobCity:  pobCity,
                type:     type,
                prs:      person,
                org:      organisation
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
}
