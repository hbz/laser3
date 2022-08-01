package de.laser.utils

import groovy.transform.CompileStatic
import org.springframework.context.i18n.LocaleContextHolder

@CompileStatic
class LocaleUtils {

    static Locale getCurrentLocale() {
        LocaleContextHolder.getLocale()
    }

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

    static Locale getLocaleDE() {
        Locale.forLanguageTag('de-DE')
    }
    static Locale getLocaleEN() {
        Locale.forLanguageTag('en-EN')
    }
}
