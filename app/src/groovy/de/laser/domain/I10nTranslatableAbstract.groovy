package de.laser.domain

import org.springframework.context.i18n.LocaleContextHolder

abstract class I10nTranslatableAbstract {

    protected i10nStorage = [:]

    // get translation; current locale
    def getI10n(String property) {
        getI10n(property, LocaleContextHolder.getLocale().toString())
    }

    // get translation
    def getI10n(String property, String locale) {
        def result
        locale = locale.split("-").first()

        if (I10nTranslation.supportedLocales.contains(locale.toLowerCase())) {
            result = this."${property}_${locale.toLowerCase()}"
        }
        else {
            result = "- requested locale not supported -"
        }
        result
    }

    // returning virtual property for template tags
    def propertyMissing(String name) {
        if (! i10nStorage.containsKey(name)) {

            def parts = name.split("_")
            if (parts.size() == 2) {
                def fallback = this."${parts[0]}"
                def i10n = I10nTranslation.get(this, parts[0], parts[1])
                this."${name}" = (i10n ? i10n : "${fallback} °") // TODO: remove ° ; indicated fallback
            }
        }

        i10nStorage["${name}"]
    }

    // setting virtual property
    def propertyMissing(String name, def value) {
        i10nStorage["${name}"] = value
    }
}
