package de.laser

import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

import javax.persistence.Transient

class I10nTranslation {

    def grailsApplication

    static supportedLocales = ['en', 'de', 'fr']
    static transients       = ['supportedLocales']

    Long   referenceId
    String referenceClass
    String referenceField
    String valueDe
    String valueEn
    String valueFr

    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache   true
        id              column:'i10n_id'
        version         column:'i10n_version'
        referenceId     column:'i10n_reference_id',    index: 'i10n_ref_idx'
        referenceClass  column:'i10n_reference_class', index: 'i10n_ref_idx'
        referenceField  column:'i10n_reference_field'
        valueDe         column:'i10n_value_de', type: 'text'
        valueEn         column:'i10n_value_en', type: 'text'
        valueFr         column:'i10n_value_fr', type: 'text'
        lastUpdated     column:'i10n_last_updated'
        dateCreated     column:'i10n_date_created'
    }

    static constraints = {
        referenceClass  (blank:false, maxSize:255)
        referenceField  (blank:false, maxSize:255)
        valueDe         (nullable:true,  blank:false)
        valueEn         (nullable:true,  blank:false)
        valueFr         (nullable:true,  blank:false)
        lastUpdated     (nullable:true)
        dateCreated     (nullable:true)

        referenceId(unique: ['referenceClass', 'referenceField'])
    }

    // -- getter and setter --

    static I10nTranslation get(Object reference, String referenceField) {
        reference = GrailsHibernateUtil.unwrapIfProxy(reference)

        I10nTranslation i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getCanonicalName(), reference.getId(), referenceField)

        i10n
    }

    static String get(Object reference, String referenceField, String locale) {
        reference = GrailsHibernateUtil.unwrapIfProxy(reference)

        I10nTranslation i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getCanonicalName(), reference.getId(), referenceField)

        switch(locale.toLowerCase()){
            case 'de':
                return i10n?.valueDe
                break
            case 'en':
                return i10n?.valueEn
                break
            case 'fr':
                return i10n?.valueFr
                break
        }
    }

    static I10nTranslation set(Object reference, String referenceField, Map values) {
        if (!reference || !referenceField)
            return

        withTransaction {

            reference = GrailsHibernateUtil.unwrapIfProxy(reference)
            I10nTranslation i10n = I10nTranslation.get(reference, referenceField)
            
            if (!i10n) {
                i10n = new I10nTranslation(
                        referenceClass: reference.getClass().getCanonicalName(),
                        referenceId: reference.getId(),
                        referenceField: referenceField
                )
            }

            values.each { k, v ->
                switch (k.toString().toLowerCase()) {
                    case 'de':
                        i10n.valueDe = v ? v.toString() : null
                        break
                    case 'en':
                        i10n.valueEn = v ? v.toString() : null
                        break
                    case 'fr':
                        i10n.valueFr = v ? v.toString() : null
                        break
                }
            }

            i10n.save()
            i10n
        }
    }

    // -- initializations --

    // used in bootstap
    static I10nTranslation createOrUpdateI10n(Object reference, String referenceField, Map translations) {

        Map<String, Object> values = [:] // no effect in set()

        translations['en'] ? (values << ['en':translations['en']]) : null
        translations['de'] ? (values << ['de':translations['de']]) : null
        translations['fr'] ? (values << ['fr':translations['fr']]) : null

        return set(reference, referenceField, values)
    }

    // -- helper --

    // de => de, de-DE => de, de_DE => de
    static String decodeLocale(String locale) {

        if(locale?.contains("-")) {
            return locale.split("-").first().toLowerCase()
        }
        else if(locale?.contains("_")) {
            return locale.split("_").first().toLowerCase()
        }
        else {
            return locale
        }
    }
    static String decodeLocale(Locale locale) {
        decodeLocale(locale.toString())
    }

}