package de.laser.addressbook

import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.storage.RDConstants
import de.laser.wekb.Provider
import de.laser.wekb.Vendor
import groovy.util.logging.Slf4j

/**
 * A physical address of an {@link Org}. Email-addresses and other contact possibilities than the physical address are represented by the {@link de.laser.addressbook.Contact} domain
 * @see de.laser.addressbook.Contact
 */
@Slf4j
class Address {

    String street_1
    String street_2
    String zipcode
    String city

    String pob
    String pobZipcode
    String pobCity

    Date dateCreated
    Date lastUpdated

    boolean preferredForSurvey  = false

    /**
     * The region may be located in Germany, Austria, Switzerland; since multi-annotation is not supported and the annotation is used by logic, the categories are being named only in Groovydoc
     * @see RDConstants#REGIONS_DE
     * @see RDConstants#REGIONS_AT
     * @see RDConstants#REGIONS_CH
     */
    @RefdataInfo(cat = '?', i18n = 'org.region.label')
    RefdataValue region

    @RefdataInfo(cat = RDConstants.COUNTRY)
    RefdataValue country

    String name
    String additionFirst
    String additionSecond

    Org    org              // org related contact; exclusive with vendor and provider
    Provider provider       // provider related contact; exclusive with org and vendor
    Vendor vendor           // vendor related contact; exclusive with org and provider
    Org    tenant           // enables private contacts

    @RefdataInfo(cat = RDConstants.ADDRESS_TYPE)
    static hasMany = [
            type: RefdataValue
    ]
    
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
        region    column:'adr_region_rv_fk'
        country  column:'adr_country_rv_fk'
        name     column:'adr_name'
        additionFirst   column:'adr_addition_first'
        additionSecond  column:'adr_addition_second'
        org      column:'adr_org_fk', index: 'adr_org_idx'
        provider column:'adr_provider_fk', index: 'adr_provider_idx'
        vendor   column:'adr_vendor_fk', index: 'adr_vendor_idx'
        tenant   column:'adr_tenant_fk', index: 'adr_tenant_idx'

        preferredForSurvey column: 'adr_preferred_for_survey'

        lastUpdated     column: 'adr_last_updated'
        dateCreated     column: 'adr_date_created'

        type            joinTable: [
                name:   'address_type',
                key:    'address_id',
                column: 'refdata_value_id', type:   'BIGINT'
        ], lazy: false
    }
    
    static constraints = {
        street_1 (nullable:true,  blank:false)
        street_2 (nullable:true,  blank:false)
        pob         (nullable:true,  blank:false)
        pobZipcode  (nullable:true,  blank:false)
        pobCity     (nullable:true,  blank:false)
        zipcode  (nullable:true, blank:false)
        city     (nullable:true, blank:false)
        region          (nullable:true)
        country         (nullable:true)
        name            (nullable:true,  blank:false)
        additionFirst   (nullable:true,  blank:false)
        additionSecond  (nullable:true,  blank:false)
        org      (nullable:true)
        provider (nullable:true)
        vendor   (nullable:true)
        tenant   (nullable:true)
        lastUpdated (nullable: true)
    }

    /**
     * Gets all address types
     * @return a {@link List} of {@link RefdataValue}s of category {@link RDConstants#ADDRESS_TYPE}
     */
    static List<RefdataValue> getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE)
    }

    /**
     * Outputs the address as human-readable string
     * @return the concatenated address string
     */
    @Override
    String toString() {
        zipcode + ' ' + city + ', ' + street_1 + ' ' + street_2 + ' (' + id + '); ' + type.each {it.value}.join(',')
    }

    @Deprecated
    String generateGoogleMapURL(){
        String url = ''
        if (this.name) url += this.name
        url += (url.length()>0 && this.street_1)? '+' : ''
        if (this.street_1) url += this.street_1
        url += (url.length()>0 && this.street_2)? '+' : ''
        if (this.street_2) url += this.street_2
        url += (url.length()>0 && this.zipcode)? '+' : ''
        if (this.zipcode) url += this.zipcode
        url += (url.length()>0 && this.city)? '+' : ''
        if (this.city) url += this.city
        url += (url.length()>0 && this.country)? '+' : ''
        if (this.country) url += this.country
        url = url.replace(' ', '+')
        //String encodedUrl = URLEncoder.encode(url)
        url = 'https://maps.google.com/?q='+url
        //url = 'https://www.google.com/maps/search/?api:1&query='+encodedUrl

        url
    }

    String getAddressForExport(){
        String addr= ""

        if(name){
            addr = name
        }else {
            addr = org.name
        }

        if(additionFirst || additionSecond) {
            addr += ', '
            if (additionFirst) {
                addr += additionFirst + ' '
            }
            if (additionSecond) {
                addr += additionSecond + ' '
            }
        }

        if(street_1 || street_2) {
            addr += ', '
            if (street_1) {
                addr += street_1 + ' '
            }
            if (street_2) {
                addr += street_2 + ' '
            }
        }

        if(zipcode || city) {
            addr += ', '
            if (zipcode) {
                addr += zipcode + ' '
            }
            if (city) {
                addr += city + ' '
            }
        }

        if (region) {
            addr += ', ' + region.getI10n('value') + ' '
        }

        if (country) {
            addr += ', ' + country.getI10n('value') + ' '
        }

        if(pob){
            addr += ', ' + pob + ' '
        }

        if(pobZipcode){
            addr += ', ' + pobZipcode + ' '
        }

        if(pobCity){
            addr += ', ' + pobCity + ' '
        }

        return addr
    }
}
