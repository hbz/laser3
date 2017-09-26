package de.laser

import org.springframework.context.i18n.LocaleContextHolder

class I10nTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "laser"

    // <laser:select optionValue="field" />  ==> <laser:select optionValue="field_(de|en|fr)" />

    def select = { attrs, body ->
        attrs.optionValue = attrs.optionValue + "_" + LocaleContextHolder.getLocale().toString().split("-").first()
        out << g.select(attrs)
    }
}
