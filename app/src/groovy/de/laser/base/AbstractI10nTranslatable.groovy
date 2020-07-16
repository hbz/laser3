package de.laser.base

import de.laser.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

abstract class AbstractI10nTranslatable {
    public static final LOCALE_DE = Locale.GERMAN.toString()
    public static final LOCALE_EN = Locale.ENGLISH.toString()

    protected i10nStorage = [:]

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
        String result
        locale = I10nTranslation.decodeLocale(locale)

        if (I10nTranslation.supportedLocales.contains(locale)) {
            String name = property + '_' + locale
            if (this.hasProperty(name)) {
                result = this."${name}"
            }
            else {
                if (! i10nStorage.containsKey(name)) {
                    String i10n = I10nTranslation.get(this, property, locale)
                    String fallback = this."${property}"
                    i10nStorage["${name}"] = (i10n ?: fallback)
                }

                return i10nStorage["${name}"]
            }
        }
        else {
            result = "- requested locale ${locale} not supported -"
        }
        return (result != 'null') ? result : ''
    }

    /** Suffix for DB Tables. Right now, there are only German and English Columns */
    static String getLanguageSuffix(){
        String languageSuffix = 'en'
        if (LocaleContextHolder.getLocale().getLanguage() == Locale.GERMAN.getLanguage()){
            languageSuffix = 'de'
        }
        languageSuffix
    }
}
