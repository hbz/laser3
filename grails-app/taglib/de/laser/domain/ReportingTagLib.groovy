package de.laser.domain

import de.laser.Org
import de.laser.RefdataCategory
import de.laser.RefdataValue
import de.laser.annotations.RefdataInfo
import de.laser.base.AbstractPropertyWithCalculatedLastUpdated
import de.laser.storage.RDConstants
import de.laser.reporting.report.myInstitution.base.BaseConfig
import de.laser.reporting.report.GenericHelper
import de.laser.reporting.report.myInstitution.base.BaseDetails
import de.laser.utils.SwissKnife
import org.apache.commons.lang3.RandomStringUtils

import java.lang.reflect.Field

class ReportingTagLib {

    static namespace = 'uiReporting'

    def numberToString = { attrs, body ->
        String n = attrs.number

        if (attrs.min) {
            n = Integer.max(attrs.min as int, attrs.number as int).toString()
        }
        if (SwissKnife.NUMBER_AS_STRING.containsKey(n)) {
            out << SwissKnife.NUMBER_AS_STRING.get(n)
        }
        else {
            out << 'xyz'
        }
    }

    def filterField = { attrs, body ->
        //println '<uiReporting:filterField>   ' + attrs.key + ' ' + attrs.field
        Map<String, Object> field = GenericHelper.getField(attrs.config, attrs.field)

        if (! field) {
            out << '[[ ' + attrs.field + ' ]]'
            return
        }
        else if (field.type == BaseConfig.FIELD_TYPE_PROPERTY) {
            out << uiReporting.filterProperty(config: attrs.config, key: attrs.key, property: attrs.field)
        }
        else if (field.type == BaseConfig.FIELD_TYPE_REFDATA) {
            out << uiReporting.filterRefdata(config: attrs.config, key: attrs.key, refdata: attrs.field)
        }
        else if (field.type == BaseConfig.FIELD_TYPE_REFDATA_JOINTABLE) {
            out << uiReporting.filterCustomImpl(config: attrs.config, key: attrs.key, refdata: attrs.field)
        }
        else if (field.type == BaseConfig.FIELD_TYPE_CUSTOM_IMPL) {
            if (field.customImplRdv) {
                out << uiReporting.filterCustomImpl(config: attrs.config, key: attrs.key, refdata: attrs.field, customImplRdv: field.customImplRdv)
            } else {
                out << uiReporting.filterCustomImpl(config: attrs.config, key: attrs.key, refdata: attrs.field)
            }
        }
        else if (field.type == BaseConfig.FIELD_TYPE_ELASTICSEARCH) {
            out << uiReporting.filterCustomImpl(config: attrs.config, key: attrs.key, refdata: attrs.field)
        }
    }

    def filterProperty = { attrs, body ->

        Field prop  = attrs.config.meta.class.getDeclaredField(attrs.property)

        String todo           = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check
        String filterLabel    = message(code: todo + '.' + prop.getName() + '.label', default: prop.getName())
        String filterName     = 'filter:' + (attrs.key ? attrs.key : todo) + '_' + attrs.property
        Integer filterValue   = params.int(filterName)

        // println 'TMP - filterProperty: ' + prop + ' : ' + todo + ' > ' + todo + '.' + prop.getName() + '.label' + ' > ' + filterLabel

        if (prop.getType() in [boolean, Boolean]) {

            out << '<div class="field">'
            out << '<label for="' + filterName + '">' + filterLabel + '</label>'

            out << ui.select([
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
            out << ui.datepicker([
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

    def filterRefdata = { attrs, body ->

        Field refdata   = attrs.config.meta.class.getDeclaredField(attrs.refdata)
        def anno        = refdata.getAnnotationsByType(RefdataInfo).head()
        String rdCat    = anno.cat()
        String rdI18n   = anno.i18n()

        String todo     = attrs.config.meta.class.simpleName.uncapitalize() // TODO -> check

        String filterLabel    = rdI18n != 'n/a' ? message(code: rdI18n, default: rdCat) : message(code: rdCat + '.label', default: rdCat) // TODO -> @RefdataInfo
        String filterName     = "filter:" + (attrs.key ? attrs.key : todo) + '_' + attrs.refdata
        Integer filterValue   = params.int(filterName)

        // println 'TMP - filterRefdata: ' + rdCat + ' : ' + rdI18n + ' > ' + filterLabel

        out << '<div class="field">'
        out << '<label for="' + filterName + '">' + filterLabel + '</label>'

        out << ui.select([
                class      : "ui fluid search dropdown",
                name       : GenericHelper.isFieldVirtual(attrs.config, attrs.refdata) ? filterName + '_virtualFF' : filterName,
                id         : getUniqueId(filterName),
                from       : RefdataCategory.getAllRefdataValues(rdCat),
                optionKey  : "id",
                optionValue: "value",
                value      : filterValue,
                noSelection: ['': message(code: 'default.select.choose.label')]
        ])

        if ( GenericHelper.isFieldVirtual(attrs.config, attrs.refdata) ) {
            out << '<input type="hidden" name="' + filterName + '" value="' + (filterValue ?: '') + '" />'
        }
        out << '</div>'
    }

    def filterCustomImpl = { attrs, body ->
        //println '<uiReporting:filterCustomImpl>   ' + attrs.key + ' ' + attrs.refdata + ' ' + attrs.customImplRdv

        // TODO
        Map<String, Object> customRdv
        if (attrs.customImplRdv) {
            customRdv = BaseConfig.getCustomImplRefdata(attrs.customImplRdv, attrs.config.meta.class)
        } else {
            customRdv = BaseConfig.getCustomImplRefdata(attrs.refdata, attrs.config.meta.class)
        }
        if (! customRdv) {
            customRdv = BaseConfig.getElasticSearchRefdata(attrs.refdata) // TODO !!!!!!!!!!!!!
        }

        //println '||->' + attrs.config.meta.class + ' :: ' + attrs.config.meta.cfgKey + ' / ' + attrs.refdata

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
        if ( GenericHelper.isFieldMultiple(attrs.config, attrs.refdata) ) {
            map.put('multiple', true)
            map.put('value', params.list(filterName).collect { Long.parseLong(it) })
        }
        else {
            map.put('value', params.int(filterName))
        }
        out << ui.select( map )
        out << '</div>'
    }

    def objectProperties = { attrs, body ->

        Long pdId = attrs.propDefId as Long
        Org tenant = attrs.tenant as Org
        List<AbstractPropertyWithCalculatedLastUpdated> properties = BaseDetails.getPropertiesGeneric(attrs.owner, pdId, tenant) as List<AbstractPropertyWithCalculatedLastUpdated>

        List<String> props = properties.collect { prop ->
            String result = ''
            Map<String, List> tmp = [ tooltips:[], icons:[] ]
            if (prop.getType().isRefdataValueType()) {
                result += (prop.getRefValue() ? prop.getRefValue().getI10n('value') : '')
            } else {
                result += (prop.getValue() ?: '')
            }

            if (prop.type.tenant?.id == tenant.id) {
                tmp.tooltips.add( message(code: 'reporting.details.property.own') as String )
                tmp.icons.add( '<i class="icon shield alternate la-light-grey"></i>' )
            }
            if (!prop.isPublic && (prop.tenant && prop.tenant.id == tenant.id)) {
                tmp.tooltips.add( message(code: 'reporting.details.property.private') as String )
                tmp.icons.add( '<i class="icon eye slash alternate yellow"></i>' )
            }
            if (tmp.icons) {
                result = result + '&nbsp;&nbsp;&nbsp;'
                result = result + '<span class="la-popup-tooltip la-delay" data-content="' + tmp.tooltips.join(' / ') + '" data-position="top right">'
                result = result + tmp.icons.join('')
                result = result + '</span>'
            }
            result
        }.sort().findAll() // removing empty and null values

        out << props.join(' ,<br/>')
    }

    def detailsTableTD = { attrs, body ->
        //println '<uiReporting:detailsTableTD> ' + attrs
        Map<String, Boolean> config = attrs.config as Map

        if ( config.get( attrs.field )?.containsKey('dtc') ) {
            String markup = '<td data-column="dtc:' + attrs.field + '"'

            if (! config.get( attrs.field ).dtc) {
                markup = markup + ' class="hidden"'
            }
            out << markup + '>'
            out << body()
            out << '</td>'
        }
        else {
            out << '### ' + attrs.field + ' ###'
        }
    }

    def detailsTableEsValue = { attrs, body ->

        // TMP
        // TMP

        String key = attrs.key
        Long id = attrs.id as Long
        String field = attrs.field

        Map<String, Object> esRecords = attrs.records as Map
        Map<String, Map> esdConfig  = BaseConfig.getCurrentConfigElasticsearchData(key).get( key + '-' + field )

        Map<String, Object> record = esRecords.getAt(id as String) as Map
        if (record) {
            String value = record.get( field )
            // workaround - nested values
            if (esdConfig.mapping) {
                def tmp = record
                esdConfig.mapping.split('\\.').each{ m ->
                    if (tmp) { tmp = tmp.get(m) }
                }
                value = tmp
            }
            //String value = record.get( record.mapping ?: field )
            if (value) {
                RefdataValue rdv = RefdataValue.getByValueAndCategory(value, esdConfig.rdc as String)
                if (rdv) {
                    out << rdv.getI10n('value')
                }
                else {
                    out << GenericHelper.flagUnmatched( value )
                }
            }
        }
    }

    static String getUniqueId(String id) {
        return id + '-' + RandomStringUtils.randomAlphanumeric(8).toLowerCase()
    }
}
