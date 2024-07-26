package de.laser

class SymbolTagLib {

    // -- simple static icons
    // -- no size, no color, ..

    static namespace = 'icon'

    // Layout helper / mostly wrapped with class="hidden"
    def placeholder = { attrs, body ->
        out << '<i class="fake icon"></i>'
    }

    def arrow = { attrs, body ->
        out << '<i class="arrow right icon"></i>'
    }
}
