package de.laser

import de.laser.helper.LocaleHelper

class LaserI10nTagLib {

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]


    static namespace = "laser"

    // <laser:select optionValue="field" />  ==> <laser:select optionValue="field_(de|en|fr)" />

    def select = { attrs, body ->
        attrs.optionValue = attrs.optionValue + "_" + LocaleHelper.getCurrentLang()
        out << g.select(attrs)
    }
}
