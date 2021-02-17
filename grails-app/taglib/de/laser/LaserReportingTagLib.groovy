package de.laser

import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import de.laser.helper.RDStore
import de.laser.reporting.GenericConfig

import java.lang.reflect.Field

class LaserReportingTagLib {

    static namespace = "laser"

    def reportFilterField = { attrs, body ->

        String fieldType = GenericConfig.getFormFieldType(attrs.config, attrs.field) // [ property, refdata ]

        if (fieldType == GenericConfig.FORM_TYPE_PROPERTY) {
            out << laser.reportFilterProperty(config: attrs.config, property: attrs.field, key: attrs.key)
        }
        if (fieldType == GenericConfig.FORM_TYPE_REFDATA) {
            out << laser.reportFilterRefdata(config: attrs.config, refdata: attrs.field, key: attrs.key)
        }
        if (fieldType == GenericConfig.FORM_TYPE_REFDATA_RELTABLE) {
            out << laser.reportFilterRefdataRelTable(config: attrs.config, refdata: attrs.field, key: attrs.key)
        }
    }

    def reportFilterProperty = { attrs, body ->

        Field prop  = attrs.config.meta.class.getDeclaredField(attrs.property)

        String todo = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = message(code: todo + '.' + prop.getName() + '.label', default: prop.getName())
        String filterName     = 'filter:' + (attrs.key ? attrs.key : todo) + '_' + attrs.property
        String filterValue    = params.get(filterName)

        if (prop.getType() in [boolean, Boolean]) {

            out << '<div class="field">'
            out << '<label for="' + filterName + '">' + filterLabel + '</label>'

            out << laser.select([
                    class      : "ui fluid dropdown",
                    name       : filterName,
                    from       : RefdataCategory.getAllRefdataValues(RDConstants.Y_N),
                    optionKey  : "id",
                    optionValue: "value",
                    value      : filterValue,
                    noSelection: ['': message(code: 'default.select.choose.label')]
            ])
            out << '</div>'
        }
        else if (prop.getType() == Date) {
            out << semui.datepicker([
                    label      : filterLabel,
                    id         : filterName,
                    name       : filterName,
                    placeholder: "filter.placeholder",
                    value      : filterValue,
                    modifiers  : true
            ])
        }
    }

    def reportFilterRefdata = { attrs, body ->

        Field refdata   = attrs.config.meta.class.getDeclaredField(attrs.refdata)
        def anno        = refdata.getAnnotationsByType(RefdataAnnotation).head()
        String rdCat    = anno.cat()
        String rdI18n   = anno.i18n()

        String todo     = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check

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
    }

    def reportFilterRefdataRelTable = { attrs, body ->

        Map<String, Object> rdvInfo = GenericConfig.getRefdataRelTableInfo(attrs.refdata)

        String todo     = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = rdvInfo.get('label')
        String filterName     = "filter:" + (attrs.key ? attrs.key : todo) + '_' + attrs.refdata
        String filterValue    = params.get(filterName)

        out << '<div class="field">'
        out << '<label for="' + filterName + '">' + filterLabel + '</label>'

        out << laser.select([
                class      : "ui fluid dropdown",
                name       : filterName,
                from       : rdvInfo.get('from'),
                optionKey  : "id",
                optionValue: "value",
                value      : filterValue,
                noSelection: ['': message(code: 'default.select.choose.label')]
        ])
        out << '</div>'
    }
}
