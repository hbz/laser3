package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import de.laser.helper.RDConstants
import de.laser.helper.RefdataAnnotation
import groovy.util.logging.Log4j
import org.apache.commons.logging.LogFactory

@Log4j
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

    @RefdataAnnotation(cat = '?')
    RefdataValue region

    @RefdataAnnotation(cat = RDConstants.COUNTRY)
    RefdataValue country

    @RefdataAnnotation(cat = RDConstants.ADDRESS_TYPE)
    RefdataValue type

    String name
    String additionFirst
    String additionSecond

    Person prs              // person related contact; exclusive with org
    Org    org              // org related contact; exclusive with prs
    
    static mapping = {
        cache  true
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
        type     column:'adr_type_rv_fk'
        prs      column:'adr_prs_fk', index: 'adr_prs_idx'
        org      column:'adr_org_fk', index: 'adr_org_idx'

        lastUpdated     column: 'adr_last_updated'
        dateCreated     column: 'adr_date_created'

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
        prs      (nullable:true)
        org      (nullable:true)

        // Nullable is true, because values are already in the database
        lastUpdated (nullable: true)
        dateCreated (nullable: true)
    }
    
    static List<RefdataValue> getAllRefdataValues() {
        RefdataCategory.getAllRefdataValues(RDConstants.ADDRESS_TYPE)
    }
    
    @Override
    String toString() {
        zipcode + ' ' + city + ', ' + street_1 + ' ' + street_2 + ' (' + id + '); ' + type?.value
    }

    static Address lookup(
            String name,
            String street1,
            String street2,
            String zipcode,
            String city,
            RefdataValue region,
            RefdataValue country,
            String postbox,
            String pobZipcode,
            String pobCity,
            RefdataValue type,
            Person person,
            Org organisation) {

        Address address
        List<Address> check = Address.findAllWhere(
                name:           name ?: null,
                street_1: street1 ?: null,
                street_2: street2 ?: null,
                zipcode:  zipcode ?: null,
                city:     city ?: null,
                region:    region ?: null,
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

    static Address lookupOrCreate(
            String name,
            String street1,
            String street2,
            String zipcode,
            String city,
            RefdataValue region,
            RefdataValue country,
            String postbox,
            String pobZipcode,
            String pobCity,
            RefdataValue type,
            Person person,
            Org organisation) {

        withTransaction {
            Address result
            String info = "saving new address: ${type}"

            if (person && organisation) {
                type = RefdataValue.getByValue("Job-related")
            }

            Address check = Address.lookup(name, street1, street2, zipcode, city, region, country, postbox, pobZipcode,
                    pobCity, type, person, organisation)
            if (check) {
                result = check
                info += " > ignored; duplicate found"
            }
            else {
                result = new Address(
                        name: name,
                        street_1: street1,
                        street_2: street2,
                        zipcode: zipcode,
                        city: city,
                        region: region,
                        country: country,
                        pob: postbox,
                        pobZipcode: pobZipcode,
                        pobCity: pobCity,
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
}
