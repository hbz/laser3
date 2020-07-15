package de.laser.base

import de.laser.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

abstract class AbstractI10n {
    public static final LOCALE_DE = Locale.GERMAN.toString()
    public static final LOCALE_EN = Locale.ENGLISH.toString()

    // get translation; current locale
    def getI10n(String property) {
        getI10n(property, LocaleContextHolder.getLocale().toString())
    }

    // get translation
    def getI10n(String property, Locale locale) {
        getI10n(property, locale.toString())
    }
    // get translation
    def getI10n(String property, String locale) {
        def result
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
