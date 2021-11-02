package de.laser

import de.laser.annotations.RefdataAnnotation
import de.laser.helper.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.GenericHelper
import org.apache.commons.lang3.RandomStringUtils

import java.lang.reflect.Field

class LaserReportingTagLib {

    static namespace = "laser"

    def numberToString = { attrs, body ->
        Map <String, String> map = [
            '0' : 'zero',
            '1' : 'one',
            '2' : 'two',
            '3' : 'three',
            '4' : 'four',
            '5' : 'five',
            '6' : 'six',
            '7' : 'seven',
            '8' : 'eight',
            '9' : 'nine',
            '10' : 'ten',
            '11' : 'eleven',
            '12' : 'twelve',
            '13' : 'thirteen',
            '14' : 'fourteen'
        ]
        String n = attrs.number

        if (attrs.min) {
            n = Integer.max(attrs.min as int, attrs.number as int).toString()
        }
        if (map.containsKey(n)) {
            out << map.get(n)
        }
        else {
            out << 'xyz'
        }
    }

    def reportFilterField = { attrs, body ->

        String fieldType = GenericHelper.getFieldType(attrs.config, attrs.field) // [ property, refdata ]
        //boolean fieldIsMultiple = GenericHelper.isFieldMultiple(attrs.config, attrs.field)

        if (fieldType == BaseConfig.FIELD_TYPE_PROPERTY) {
            out << laser.reportFilterProperty(config: attrs.config, property: attrs.field, key: attrs.key)
        }
        if (fieldType == BaseConfig.FIELD_TYPE_REFDATA) {
            out << laser.reportFilterRefdata(config: attrs.config, refdata: attrs.field, key: attrs.key)
        }
        if (fieldType == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
            out << laser.reportFilterRefdataRelTable(config: attrs.config, refdata: attrs.field, key: attrs.key)
        }
        if (fieldType == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
            out << laser.reportFilterCustomImpl(config: attrs.config, field: attrs.field, key: attrs.key)
        }
    }

    def reportFilterProperty = { attrs, body ->

        Field prop  = attrs.config.meta.class.getDeclaredField(attrs.property)

        String todo = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = message(code: todo + '.' + prop.getName() + '.label', default: prop.getName())
        String filterName     = 'filter:' + (attrs.key ? attrs.key : todo) + '_' + attrs.property
        Integer filterValue   = params.int(filterName)

        if (prop.getType() in [boolean, Boolean]) {

            out << '<div class="field">'
            out << '<label for="' + filterName + '">' + filterLabel + '</label>'

            out << laser.select([
                    class      : "ui fluid search dropdown",
                    name       : filterName,
                    id         : getUniqueId(filterName),
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
                    name       : filterName,
                    id         : getUniqueId(filterName),
                    placeholder: "filter.placeholder",
                    value      : filterValue,
                    modifiers       : true,
                    defaultModifier : prop.name == 'startDate' ? 'greater-equal' : ( prop.name == 'endDate' ? 'less-equal' : null )
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
        Integer filterValue   = params.int(filterName)

        out << '<div class="field">'
        out << '<label for="' + filterName + '">' + filterLabel + '</label>'

        out << laser.select([
                class      : "ui fluid search dropdown",
                name       : GenericHelper.isFieldVirtual(attrs.refdata) ? filterName + '_virtualFF' : filterName,
                id         : getUniqueId(filterName),
                from       : RefdataCategory.getAllRefdataValues(rdCat),
                optionKey  : "id",
                optionValue: "value",
                value      : filterValue,
                noSelection: ['': message(code: 'default.select.choose.label')]
        ])

        if ( GenericHelper.isFieldVirtual(attrs.refdata) ) {
            out << '<input type="hidden" name="' + filterName + '" value="' + (filterValue ?: '') + '" />'
        }
        out << '</div>'
    }

    def reportFilterRefdataRelTable = { attrs, body ->

        Map<String, Object> customRdv = BaseConfig.getCustomImplRefdata(attrs.refdata, attrs.config.meta.class) // propertyKey, propertyValue

        String todo     = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = customRdv.get('label')
        String filterName     = "filter:" + (attrs.key ? attrs.key : todo) + '_' + attrs.refdata

        out << '<div class="field">'
        out << '<label for="' + filterName + '">' + filterLabel + '</label>'

        Map<String, Object> map = [
            class      : 'ui fluid search dropdown',
            name       : filterName,
            id         : getUniqueId(filterName),
            from       : customRdv.get('from'),
            optionKey  : 'id',
            optionValue: 'value',
            noSelection: ['': message(code: 'default.select.choose.label')]
        ]
        if ( GenericHelper.isFieldMultiple(attrs.refdata) ) {  // TODO - other tags
            map.put('multiple', true)
            map.put('value', params.list(filterName).collect { Integer.parseInt(it) })
        }
        else {
            map.put('value', params.int(filterName))
        }
        out << laser.select( map )
        out << '</div>'
    }

    def reportFilterCustomImpl = { attrs, body ->

        //println '> reportFilterCustomImpl: ' + attrs.field
        out << laser.reportFilterRefdataRelTable(config: attrs.config, refdata: attrs.field, key: attrs.key)
    }

    static String getUniqueId(String id) {
        return id + '-' + RandomStringUtils.randomAlphanumeric(8).toLowerCase()
    }
}
