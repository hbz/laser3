package de.laser.domain

import javax.persistence.Transient

class I10nTranslation {

    @Transient
    def grailsApplication

    static supportedLocales = ['en', 'de', 'fr']
    static transients       = ['supportedLocales']

    Long   referenceId
    String referenceClass
    String referenceField
    String valueDe
    String valueEn
    String valueFr

    static mapping = {
        id          column:'i10n_id'
        version     column:'i10n_version'
        referenceId column:'i10n_reference_id'
        referenceClass column:'i10n_reference_class'
        referenceField column:'i10n_reference_field'
        valueDe     column:'i10n_value_de'
        valueEn     column:'i10n_value_en'
        valueFr     column:'i10n_value_fr'
    }

    static constraints = {
        referenceId     (nullable:false, blank:false)
        referenceClass  (nullable:false, blank:false, maxSize:255)
        referenceField  (nullable:false, blank:false, maxSize:255)
        valueDe         (nullable:true,  blank:false)
        valueEn         (nullable:true,  blank:false)
        valueFr         (nullable:true,  blank:false)

        referenceId(unique: ['referenceClass', 'referenceField'])
    }

    // -- getter and setter --

    static get(Object reference, String referenceField) {
        def i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getCanonicalName(), reference.getId(), referenceField)

        i10n
    }

    static get(Object reference, String referenceField, String locale) {
        def i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getCanonicalName(), reference.getId(), referenceField)

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

    static set(Object reference, String referenceField, Map values) {
        if (!reference || !referenceField)
            return

        def i10n = get(reference, referenceField)
        if (!i10n) {
            i10n = new I10nTranslation(
                    referenceClass: reference.getClass().getCanonicalName(),
                    referenceId:    reference.getId(),
                    referenceField: referenceField
            )
        }

        values.each { k, v ->
            switch(k.toString().toLowerCase()){
                case 'de':
                    i10n.valueDe = v.toString()
                    break
                case 'en':
                    i10n.valueEn = v.toString()
                    break
                case 'fr':
                    i10n.valueFr = v.toString()
                    break
            }
        }

        i10n.save(flush: true)
        i10n
    }

    // -- initializations --

    // used in gsp to create translations for on-the-fly-created refdatas
    static createI10nIfNeeded(Object reference, String referenceField) {

        def values = [:] // no effect in set()

        if (! get(reference, referenceField)) { // set default values
            values = [
                    'en': reference."${referenceField}",
                    'de': reference."${referenceField}",
                    'fr': reference."${referenceField}"
            ]
        }

        return set(reference, referenceField, values)
    }

    // used in bootstap
    static createOrUpdateI10n(Object reference, String referenceField, Map translations) {

        def values = [:] // no effect in set()

        translations['en'] ? (values << ['en':translations['en']]) : null
        translations['de'] ? (values << ['de':translations['de']]) : null
        translations['fr'] ? (values << ['fr':translations['fr']]) : null

        return set(reference, referenceField, values)
    }

    // -- helper --

    static def refdataFindHelper(String referenceClass, String referenceField, String query, def locale) {

        def matches = []
        def result = []

        switch(locale.toString().toLowerCase()){
            case 'en':
                matches = I10nTranslation.findAllByReferenceClassAndReferenceFieldAndValueEnIlike(
                        referenceClass, referenceField, "${query}%"
                )
                break
            case 'de':
                matches = I10nTranslation.findAllByReferenceClassAndReferenceFieldAndValueDeIlike(
                        referenceClass, referenceField, "${query}%"
                )
                break
            case 'fr':
                matches = I10nTranslation.findAllByReferenceClassAndReferenceFieldAndValueFrIlike(
                        referenceClass, referenceField, "${query}%"
                )
                break
        }
        matches.each { it ->
            def obj = (new I10nTranslation().getDomainClass().grailsApplication.classLoader.loadClass(it.referenceClass)).findById(it.referenceId)
            if (obj) {
                result << obj
            }
        }

        return result
    }
}