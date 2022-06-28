package de.laser.utils

import org.springframework.context.i18n.LocaleContextHolder

class LocaleUtils {

    static String getCurrentLang() {
        decodeLocale(LocaleContextHolder.getLocale())
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

        switch (LocaleContextHolder.getLocale()) {
            case [Locale.GERMANY, Locale.GERMAN]:
                localizedName = attribute + '_de'
                break
            default:
                localizedName = attribute + '_en'
                break
        }
        localizedName
    }
}
