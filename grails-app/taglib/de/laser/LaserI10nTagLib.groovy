package de.laser

import de.laser.helper.LocaleHelper
import org.springframework.context.i18n.LocaleContextHolder

class LaserI10nTagLib {

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]


    static namespace = "laser"

    // <laser:select optionValue="field" />  ==> <laser:select optionValue="field_(de|en|fr)" />

    def select = { attrs, body ->
        attrs.optionValue = attrs.optionValue + "_" + LocaleHelper.decodeLocale(LocaleContextHolder.getLocale())
        out << g.select(attrs)
    }
}
