package de.laser.domain

abstract class I10nTranslatableAbstract {

    protected i10nStorage = [:]

    def getTranslation(String property, String locale) {
        def result

        if (I10nTranslation.supportedLocales.contains(locale.toLowerCase())) {
            result = this."${property}_${locale.toLowerCase()}"
        }
        else {
            result = "- requested locale not supported -"
        }
        result
    }

    def propertyMissing(String name) {
        if (! i10nStorage.containsKey(name)) {

            def parts = name.split("_")
            if (parts.size() == 2) {
                def fallback = this."${parts[0]}"
                def i10n = I10nTranslation.get(this, parts[0], parts[1])
                this."${name}" = (i10n ? i10n : "${fallback} *")
            }
        }

        i10nStorage["${name}"]
    }

    def propertyMissing(String name, value) {
        i10nStorage["${name}"] = value
    }
}
