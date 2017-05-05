package com.k_int.kbplus

class Address {
    
    String street_1
    String street_2
    String pob
    String zipcode
    String city
    String state
    String country
    RefdataValue type       // 'AddressType'
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
        state    (nullable:false, blank:false)
        country  (nullable:false, blank:false)
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
}
