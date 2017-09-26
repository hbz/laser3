package de.laser

// Bootstrap 4

class CardTagLib {
    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "laser"

    // <laser:card title="" class="some_css_class">
    //
    // <laser:card>


    def card = { attrs, body ->
        def title = attrs.title ? "${message(code: attrs.title)}" : ''

        out << '<div class="card ' + attrs.class + '">'
        out <<   '<div class="card-block">'
        if (title) {
            out << '<h4 class="card-title">' + title + '</h4>'
        }
        out <<     body()
        out <<   '</div>'
        out << '</div>'
    }
}
