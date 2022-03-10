package de.laser


import org.springframework.context.i18n.LocaleContextHolder

class LaserI10nTagLib {

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]


    static namespace = "laser"

    // <laser:select optionValue="field" />  ==> <laser:select optionValue="field_(de|en|fr)" />

    def select = { attrs, body ->
        attrs.optionValue = attrs.optionValue + "_" + I10nTranslation.decodeLocale(LocaleContextHolder.getLocale())
        out << g.select(attrs)
    }
}
