package de.laser.traits

import de.laser.I10nTranslation
import org.springframework.context.i18n.LocaleContextHolder

trait I10nTrait {
    public static final LOCALE_DE = Locale.GERMAN.toString()
    public static final LOCALE_EN = Locale.ENGLISH.toString()

    private i10nStorage = [:]

    def getI10n(String property) {
        getI10n(property, LocaleContextHolder.getLocale().toString()) // current locale
    }

    def getI10n(String property, Locale locale) {
        getI10n(property, locale.toString())
    }

    def getI10n(String property, String locale) {
        String result
        locale = I10nTranslation.decodeLocale(locale)

        if (I10nTranslation.supportedLocales.contains(locale)) {
            String name = property + '_' + locale
            result = this."${name}"
        }
        else {
            result = "- requested locale ${locale} not supported -"
        }
        return (result != 'null') ? result : ''
    }

    // returning virtual property for template tags; laser:select
    def propertyMissing(String name) {
        if (! i10nStorage.containsKey(name)) {

            String[] parts = name.split("_")
            if (parts.size() == 2) {
                String fallback = this."${parts[0]}"
                String i10n = I10nTranslation.get(this, parts[0], parts[1])
                i10nStorage["${name}"] = (i10n ? i10n : "${fallback}")
            }
        }

        i10nStorage["${name}"]
    }
}
