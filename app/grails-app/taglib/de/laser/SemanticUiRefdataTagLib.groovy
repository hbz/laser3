package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.User

// Semantic UI

class SemanticUiRefdataTagLib {

    def springSecurityService

    //static defaultEncodeAs = [taglib:'html']
    //static encodeAsForTags = [tagName: [taglib:'html'], otherTagName: [taglib:'none']]

    static namespace = "semui"

    // <semui:rdcSelector/>

    def rdcSelector = { attrs, body ->

        def name = 'rdc-selector' + (attrs.name ? '-' + attrs.name  :'')
        def rdCats = RefdataCategory.where{}.sort('desc')

        out << g.select( id: name, name: name,
                class: "ui dropdown search selection",
                from: rdCats,
                optionKey: { it -> RefdataCategory.class.name + ':' + it.id },
                optionValue: { it -> it.getI10n('desc') + " (${it.desc})" })

    }

    // <semui:rdvSelector category="2"/>

    def rdvSelector = { attrs, body ->

        def name = 'rdv-selector' + (attrs.name ? '-' + attrs.name  :'')
        def rdCat = RefdataCategory.findById((attrs.category) as Long)
        def rdValues = rdCat ? RefdataValue.findAllByOwner(rdCat) : []

        out << g.select( id: name, name: name,
                class: "ui dropdown search selection",
                from: rdValues,
                optionKey: { it -> RefdataValue.class.name + ':' + it.id },
                optionValue: { it -> it.getI10n('value') })

    }

    // <semui:rdvComboSelector category="2" value="5"/>

    def rdvComboSelector = { attrs, body ->
        print "!"
        def nameRdc = 'rdc-combo-selector' + (attrs.name ? '-' + attrs.name  :'')
        def nameRdv = 'rdv-combo-selector' + (attrs.name ? '-' + attrs.name  :'')
        def rdCats = RefdataCategory.where{}.sort('desc')
        def rdValues

        def rdcPreset
        if (attrs.category) {
            rdcPreset = RefdataCategory.findById((attrs.category) as Long)
            rdValues  = RefdataValue.findAllByOwner(rdcPreset)
        }
        out << g.select( id: nameRdc, name: nameRdc,
                class: "ui dropdown search selection",
                from: rdCats,
                value: (rdcPreset ? (rdcPreset.class.name + ":" + rdcPreset.id) : null),
                optionKey: { it -> RefdataCategory.class.name + ':' + it.id },
                optionValue: { it -> it.getI10n('desc') + " (${it.desc})" })

        def rdvPreset
        if (rdValues && attrs.value) {
            rdvPreset = RefdataCategory.findById((attrs.value) as Long)
        }
        out << g.select( id: nameRdv, name: nameRdv,
                class: "ui dropdown search selection",
                from: rdValues,
                value: (rdvPreset ? (rdvPreset.class.name + ":" + rdvPreset.id) : null),
                optionKey: { it -> RefdataValue.class.name + ':' + it.id },
                optionValue: { it -> it.getI10n('value') })

        out << "<script>"
        out << "    \$('#${nameRdc}').on('change', function(e) {"
        out << "        \$.ajax({ url: \"${g.createLink(controller:"ajax", action:"sel2RefdataSearchNew")}?oid=\" + \$(this).find(':selected').val() + \"&format=json\""
        out << "            ,"
        out << "            success: function(data){ "
        out << "                var result = [];"
        out << "                \$('#${nameRdv}').dropdown('clear');"
        out << "                result.push('<option></option>');"
        out << "                data.forEach(function(item){"
        out << "                      result.push('<option value=\"' + item.value + '\">' + item.text + '</option>');"
        out << "                });"
        out << "               \$('#${nameRdv}').empty().append(result.join());"
        out << "            }"
        out << "        })"
        out << "     });"
        out << "    \$('#${nameRdv}').on('change', function(e) {"
        out << "         console.log(\$(this).find(':selected').val());"
        out << "     });"
        out << "</script>"
    }

}
