package de.laser

class SymbolTagLib {

    static namespace = 'ui'

    // Layout helper / mostly wrapped with class="hidden"
    def placeholder = { attrs, body ->
        out << '<i class="fake icon"></i>'
    }

    def arrow = { attrs, body ->
        out << '<i class="arrow right icon"></i>'
    }
}
