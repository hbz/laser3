package de.laser

import de.laser.annotations.RefdataAnnotation
import java.lang.reflect.Field

class LaserReportingTagLib {

    static namespace = "laser"

    def reportFilterProperty = { attrs, body ->

        Map<String, Object> config = attrs.config

        Field prop  = config.meta.class.getDeclaredField(attrs.property)

        String todo = config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = message(code: todo + '.' + prop.getName() + '.label', default: prop.getName())
        String filterName     = 'filter:' + (attrs.key ? attrs.key : todo) + '_' + attrs.property
        String filterValue    = params.get(filterName)

        if (prop.getType() == java.util.Date) {
            out << semui.datepicker([
                    label      : filterLabel,
                    id         : filterName,
                    name       : filterName,
                    placeholder: "filter.placeholder",
                    value      : filterValue
            ])
        }

        // println todo + '.' + prop.getName() + '.label' // DEBUG
    }

    def reportFilterRefdata = { attrs, body ->

        Map<String, Object> config = attrs.config

        Field refdata   = config.meta.class.getDeclaredField(attrs.refdata)
        def anno        = refdata.getAnnotationsByType(RefdataAnnotation).head()
        String rdCat    = anno.cat()
        String rdI18n   = anno.i18n()

        String todo     = config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = rdI18n != 'n/a' ? message(code: rdI18n, default: rdCat) : message(code: rdCat + '.label', default: rdCat) // TODO -> @RefdataAnnotation
        String filterName     = "filter:" + (attrs.key ? attrs.key : todo) + '_' + attrs.refdata
        String filterValue    = params.get(filterName)

        out << '<div class="field">'
        out << '<label for="' + filterName + '">' + filterLabel + '</label>'

        out << laser.select([
                class      : "ui fluid dropdown",
                name       : filterName,
                from       : RefdataCategory.getAllRefdataValues(rdCat),
                optionKey  : "id",
                optionValue: "value",
                value      : filterValue,
                noSelection: ['': message(code: 'default.select.choose.label')]
        ])
        out << '</div>'

        // println rdCat + '.label' // DEBUG
    }
}
