package de.laser.base

import de.laser.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

/**
 * Abstract class to attach to translatable / internationalisable classes.
 * It provides a generic getter method to retrieve to localised string of a value.
 * Currently only German and English locales are supported
 */
abstract class AbstractI10n {
    public static final LOCALE_DE = Locale.GERMAN.toString()
    public static final LOCALE_EN = Locale.ENGLISH.toString()

    /**
     * Gets the translation for the given property.
     * The current locale is being assumed
     * @param property the property to which the localised string should be retrieved
     */
    String getI10n(String property) {
        getI10n(property, LocaleContextHolder.getLocale().toString())
    }

    /**
     * Gets the translation for the given property and locale
     * @param property the property to which the localised string should be retrieved
     * @param locale the language as {@link Locale} constant in which the translation is defined
     * @return the translated string for the given property and locale
     */
    String getI10n(String property, Locale locale) {
        getI10n(property, locale.toString())
    }

    /**
     * Gets the translation for the given property and locale
     * @param property the property to which the localised string should be retrieved
     * @param locale the language as string in which the translation is defined
     * @return the translated string for the given property and locale
     */
    String getI10n(String property, String locale) {
        String result
        locale = I10nTranslation.decodeLocale(locale)

        if (I10nTranslation.supportedLocales.contains(locale)) {
            result = this."${property}_${locale}"
        }
        else {
            result = "- requested locale ${locale} not supported -"
        }
        result = (result != 'null') ? result : ''
    }
}
