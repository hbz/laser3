package de.laser.domain

class I10nTranslation {

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
        referenceId    (nullable:false, blank:false)
        referenceClass (nullable:false, blank:false, maxSize:255)
        referenceField (nullable:false, blank:false, maxSize:255)
        valueDe     (nullable:true, blank:false)
        valueEn     (nullable:true, blank:false)
        valueFr     (nullable:true, blank:false)

        referenceId(unique: ['referenceClass', 'referenceField'])
    }

    static get(Object reference, String referenceField) {
        def i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getSimpleName(), reference.getId(), referenceField)

        i10n
    }

    static get(Object reference, String referenceField, String locale) {
        def i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getSimpleName(), reference.getId(), referenceField)

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
                    referenceClass: reference.getClass().getSimpleName(),
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

        i10n.save()
        i10n
    }

    static createOrUpdateI10n(def obj, String referenceField, Map translations) {
        def values = [:]

        translations['en'] ? (values << ['en':translations['en']]) : null
        translations['de'] ? (values << ['de':translations['de']]) : null
        translations['fr'] ? (values << ['fr':translations['fr']]) : null

        I10nTranslation.set(obj, referenceField, values)
    }
}
