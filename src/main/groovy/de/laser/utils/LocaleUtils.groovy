package de.laser.utils

import groovy.transform.CompileStatic
import org.springframework.context.i18n.LocaleContextHolder

@CompileStatic
class LocaleUtils {

    /**
     * Gets the current locale
     * @return the {@link Locale} of the running server environment
     */
    static Locale getCurrentLocale() {
        LocaleContextHolder.getLocale()
    }

    /**
     * Gets the language suffix of the current language
     * @return the result of {@link #decodeLocale(java.util.Locale)} with {@link #getCurrentLocale()}
     */
    static String getCurrentLang() {
        decodeLocale(getCurrentLocale())
    }

    /**
     * Decodes the given {@link Locale} constant to determine the translation field to look for
     * @param locale the locale constant (as {@link Locale}) to decipher
     * @return the decoded locale string to use in queries
     */
    static String decodeLocale(Locale locale) {
        decodeLocale(locale.toString())
    }

    /**
     * Decodes the given locale string to determine the translation field to look for
     * Examples: de => de, de-DE => de, de_DE => de
     * @param locale the locale constant as string to decipher
     * @return the decoded locale part to use in queries
     */
    static String decodeLocale(String locale) {
        if (locale?.contains("-")) {
            return locale.split("-").first().toLowerCase()
        }
        else if(locale?.contains("_")) {
            return locale.split("_").first().toLowerCase()
        }
        else {
            return locale
        }
    }

    /**
     * Gets the localised attribute name of the given attribute
     * @param attribute the attribute which should be localised
     * @return the attribute name with the current language suffix
     */
    static String getLocalizedAttributeName(String attribute) {
        String localizedName

        switch (getCurrentLocale()) {
            case [Locale.GERMANY, Locale.GERMAN]:
                localizedName = attribute + '_de'
                break
            default:
                localizedName = attribute + '_en'
                break
        }
        localizedName
    }

    /**
     * Gets the German locale
     * @return the de-DE {@link Locale}
     */
    static Locale getLocaleDE() {
        Locale.forLanguageTag('de-DE')
    }

    /**
     * Gets the English locale
     * @return the en-EN {@link Locale}
     */
    static Locale getLocaleEN() {
        Locale.forLanguageTag('en-EN')
    }
}
