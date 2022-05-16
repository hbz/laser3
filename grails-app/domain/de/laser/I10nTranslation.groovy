package de.laser

import de.laser.helper.LocaleHelper
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * This class provides internationalisation for names and reference values; supported locales are German, English and French (whereas French is never used).
 * The reference object is being stored as follows (note the OID notation how to store the object parameters): {@link #referenceClass}:{@link #referenceId}.{@link #referenceField};
 * the internationalised strings are in value{locale}
 */
class I10nTranslation {

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

    /**
     *
     * @param reference the reference object to look for
     * @param referenceField the field to which internationalisation should be retrieved
     * @return the internationalisation object for the given object's field
     */
    static I10nTranslation get(Object reference, String referenceField) {
        reference = GrailsHibernateUtil.unwrapIfProxy(reference)

        I10nTranslation i10n = findByReferenceClassAndReferenceIdAndReferenceField(reference.getClass().getCanonicalName(), reference.getId(), referenceField)

        i10n
    }

    /**
     * Gets the translation object for the given reference object's field in the given locale
     * @param reference the reference object to look for
     * @param referenceField the field to which internationalisation should be retrieved
     * @param locale the locale to retrieve
     * @return the value string in the given language
     */
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

    /**
     * Sets the translation values for the given object's field
     * @param reference the object which should receive the translations
     * @param referenceField the field about to be translated
     * @param values the {@link Map} of translations provided
     * @return the new internationalisation object
     */
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

    /**
     * Used in Bootstrap ({@link BootStrapService}): creates or updates the translations for the given object's field; these values are hard-coded in the source files which are as follows:
     * <ul>
     *  <li>RefdataValue.csv for the {@link RefdataValue} class</li>
     *  <li>RefdataCategory.csv for the {@link RefdataCategory} class</li>
     *  <li>PropertyDefinition.csv for the {@link de.laser.properties.PropertyDefinition} class</li>
     *  <li>{@link BootStrapService#setIdentifierNamespace()} for the {@link IdentifierNamespace} class</li>
     * </ul>
     * @param reference the object to set the translation
     * @param referenceField the field which should be translated
     * @param translations the {@link Map} (in structure [locale: translation]) of translation strings
     * @return the new internationalisation object
     */
    static I10nTranslation createOrUpdateI10n(Object reference, String referenceField, Map translations) {

        Map<String, Object> values = [:] // no effect in set()

        translations['en'] ? (values << ['en':translations['en']]) : null
        translations['de'] ? (values << ['de':translations['de']]) : null
        translations['fr'] ? (values << ['fr':translations['fr']]) : null

        return set(reference, referenceField, values)
    }

    // -- helper --

    static String getRefdataValueColumn(Locale locale) {
        switch(LocaleHelper.decodeLocale(locale.toString())) {
            case 'de': 'rdv_value_de'
                break
            case 'fr': 'rdv_value_fr'
                break
            default: 'rdv_value_en'
                break
        }
    }

}