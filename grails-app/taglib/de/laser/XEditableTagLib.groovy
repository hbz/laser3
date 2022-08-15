package de.laser

import de.laser.auth.Role
import de.laser.storage.RDStore
import de.laser.survey.SurveyResult
import de.laser.utils.LocaleUtils

import java.text.NumberFormat
import java.text.SimpleDateFormat

class XEditableTagLib {

    GenericOIDService genericOIDService

    static namespace = 'ui'

    /**
     *   Attributes:
     *   owner - Object
     *   field - property
     *   type - type of input
     *   validation - trigger js validation
     *   id [optional] -
     *   class [optional] - additional classes
     *   overwriteEditable - if existing, value overwrites global editable
    */
    def xEditable = { attrs, body ->

        // TODO: data-type="combodate" data-value="1984-05-15" data-format="YYYY-MM-DD" data-viewformat="DD/MM/YYYY" data-template="D / MMM / YYYY"

        boolean editable = _isEditable(request.getAttribute('editable'), attrs.overwriteEditable)

        if ( editable ) {

            String oid           = "${attrs.owner.class.name}:${attrs.owner.id}"
            String id            = attrs.id ?: "${oid}:${attrs.field}"
            String default_empty = message(code:'default.button.edit.label')
            String data_link     = null

            out << '<a href="#" id="' +  id + '" class="xEditableValue ' + (attrs.class ?: '') + '"'

            if(attrs.owner instanceof SurveyResult){
                out << ' data-onblur="submit"'
            }else {
                out << ' data-onblur="ignore"'
            }

            if (attrs.type == "date") {
                out << ' data-type="text"' // combodate | date

                String df = "${message(code:'default.date.format.notime').toUpperCase()}"
                out << ' data-format="' + df + '" data-viewformat="' + df + '" data-template="' + df + '"'

                default_empty = message(code:'default.date.format.notime.normal')
            }
            else if(attrs.type == "readerNumber") {
                out << ' data-type="text"'
            }
            else {
                out << ' data-type="' + (attrs.type ?: 'text') + '"'
            }
            out << ' data-pk="' + oid + '" data-name="' + attrs.field + '"'

            if (attrs.validation) {
                out << ' data-validation="' + attrs.validation + '"'
            }
            if (attrs.maxlength) {
                out << ' data-maxlength="' + attrs.maxlength + '"'
            }

            switch (attrs.type) {
                case 'date':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'date', format:"${message(code:'default.date.format.notime')}"]).encodeAsHTML()
                break
                case 'url':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'url']).encodeAsHTML()
                break
                case 'readerNumber':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'readerNumber']).encodeAsHTML()
                break
                default:
                    data_link = createLink(controller:'ajax', action: 'editableSetValue').encodeAsHTML()
                break
            }
            if (data_link) {
                out << ' data-url="' + data_link + '"'
            }

            out << ' data-emptytext="' + (attrs.emptytext ?: default_empty) + '"'

            if (attrs.type == "date" && attrs.language) {
                out << "data-datepicker=\"{ 'language': '${attrs.language}' }\" language=\"${attrs.language}\" "
            }

            if (! body) {
                String oldValue = ''
                if (attrs.owner[attrs.field] && attrs.type=='date') {
                    SimpleDateFormat sdf = new SimpleDateFormat(attrs.format?: message(code:'default.date.format.notime'))
                    oldValue = sdf.format(attrs.owner[attrs.field])
                }
                else {
                    if ((attrs.owner[attrs.field] == null) || (attrs.owner[attrs.field].toString().length()==0)) {
                    }
                    else if(attrs.field in ['decValue','listPrice','localPrice'] || (attrs.field == 'value' && attrs.owner instanceof ReaderNumber)) {
                        NumberFormat nf = NumberFormat.getInstance(LocaleUtils.getCurrentLocale())
                        nf.setMinimumFractionDigits(2)
                        nf.setMaximumFractionDigits(2)
                        oldValue = nf.format(attrs.owner[attrs.field])
                    }
                    else {
                        oldValue = attrs.owner[attrs.field]
                    }
                }
                out << ' data-oldvalue="' + oldValue.encodeAsHTML() + '">'
                out << oldValue.encodeAsHTML()
            }
            else {
                out << '>'
                out << body()
            }
            out << '</a>'
        }
        // !editable
        else {
            out << "<span class=\"${attrs.class ?: ''}\">"
            if ( body ) {
                out << body()
            }
            else {
                if (attrs.owner[attrs.field] && attrs.type=='date') {
                    SimpleDateFormat sdf = new SimpleDateFormat(attrs.format?: message(code:'default.date.format.notime'))
                    out << sdf.format(attrs.owner[attrs.field])
                }
                else {
                    if ((attrs.owner[attrs.field] == null) || (attrs.owner[attrs.field].toString().length()==0)) {
                    }
                    else if(attrs.field == 'decValue') {
                        out << NumberFormat.getInstance(LocaleUtils.getCurrentLocale()).format(attrs.owner[attrs.field])
                    }
                    else {
                        out << attrs.owner[attrs.field]
                    }
                }
            }
            out << '</span>'
        }
    }


    /**
     *   Attributes:
     *   overwriteEditable - if existing, value overwrites global editable
     */
    def xEditableRefData = { attrs, body ->
        try {
            boolean editable = _isEditable(request.getAttribute('editable'), attrs.overwriteEditable)

            if ( editable ) {

                String oid = "${attrs.owner.class.name}:${attrs.owner.id}"
                String dataController = attrs.dataController ?: 'ajax'
                String dataAction = attrs.dataAction ?: 'select2RefdataSearch'

                Map<String, Object> params = [id:attrs.config, format:'json', oid:oid]

                if (attrs.constraint) {
                    params.put('constraint', attrs.constraint)
                }

                String data_link = createLink(
                        controller:dataController,
                        action: dataAction,
                        params: params
                ).encodeAsHTML()

                String update_link = createLink(controller:'ajax', action: 'genericSetData').encodeAsHTML()
                String id = attrs.id ?: "${oid}:${attrs.field}"
                String cssClass = attrs.cssClass
                String data_confirm_tokenMsg = attrs.data_confirm_tokenMsg
                String data_confirm_term_how = attrs.data_confirm_term_how
                String data_confirm_value = attrs.data_confirm_value
                String default_empty = message(code:'default.button.edit.label')
                String emptyText = attrs.emptytext ? " data-emptytext=\"${attrs.emptytext}\"" : " data-emptytext=\"${default_empty}\""

                out << "<span>"

                String dataValue = ""
                def obj = genericOIDService.resolveOID(oid)

                if (obj && obj."${attrs.field}") {
                    def tmpId = obj."${attrs.field}".id
                    dataValue = " data-value=\"${RefdataValue.class.name}:${tmpId}\" "
                }

                // Output an editable link
                out << "<a href=\"#\" id=\"${id}\" class=\"xEditableManyToOne ${cssClass}\" "
                if(attrs.owner instanceof SurveyResult){
                    out << "data-onblur=\"submit\" "
                }else {
                    out << "data-onblur=\"ignore\" "
                }
                out << dataValue + "data-pk=\"${oid}\"  "
                if(attrs.data_confirm_term_how) {
                    out << "data-confirm-term-how=\"${data_confirm_term_how}\" "
                }
                if(attrs.data_confirm_tokenMsg) {
                    out << "data-confirm-tokenmsg=\"${data_confirm_tokenMsg}\" "
                }
                if(attrs.data_confirm_value) {
                    out << "data-confirm-value=\"${data_confirm_value}\" "
                }
                out << "data-type=\"select\" data-name=\"${attrs.field}\" " +
                        "data-source=\"${data_link}\" data-url=\"${update_link}\" ${emptyText}>"

                // Here we can register different ways of presenting object references. The most pressing need to be
                // outputting a a containing an icon for refdata fields.

                out << _renderObjectValue(attrs.owner[attrs.field])

                out << "</a></span>"
            }
            else {
                out << _renderObjectValue(attrs.owner[attrs.field])
            }
        }
        catch ( Throwable e ) {
            log.error("Problem processing editable refdata ${attrs}",e)
        }
    }

    /**
     *   Attributes:
     *   owner - UserOrg
     *   type - Role.roleType
     *   overwriteEditable - if existing, value overwrites global editable
     */
    def xEditableRole = { attrs, body ->
        try {
            boolean editable = _isEditable(request.getAttribute('editable'), attrs.overwriteEditable)

            if ( editable ) {

                String oid = "${attrs.owner.class.name}:${attrs.owner.id}"
                String type = attrs.type ?: 'user'

                Map<String, Object> params = [type:type, oid:oid]

                if (attrs.constraint) {
                    params.put('constraint', attrs.constraint)
                }

                String data_link = createLink(
                        controller: 'ajaxJson',
                        action: 'lookupRoles',
                        params: params
                ).encodeAsHTML()

                String update_link = createLink(controller:'ajax', action: 'genericSetData').encodeAsHTML()
                String id = attrs.id ?: "${oid}:${attrs.field}"
                String default_empty = message(code:'default.button.edit.label')
                String emptyText = attrs.emptytext ? " data-emptytext=\"${attrs.emptytext}\"" : " data-emptytext=\"${default_empty}\""

                out << '<span>'

                String dataValue = ""
                def obj = genericOIDService.resolveOID(oid)

                if (obj && obj."${attrs.field}") {
                    def tmpId = obj."${attrs.field}".id
                    dataValue = " data-value=\"${RefdataValue.class.name}:${tmpId}\" "
                }

                // Output an editable link
                out << '<a href="#" id="' + id + '" class="xEditableManyToOne" '
                out << 'data-onblur="ignore" ' + dataValue + ' data-type="select" '
                out << 'data-pk="' + oid + '" data-name="' + attrs.field + '" '
                out << 'data-source="' + data_link + '" data-url="' + update_link + '" ' + emptyText + '>'

                out << _renderObjectValue(attrs.owner[attrs.field])
                out << '</a></span>'
            }
            else {
                out << _renderObjectValue(attrs.owner[attrs.field])
            }
        }
        catch ( Throwable e ) {
            log.error("Problem processing editable refdata ${attrs}",e)
        }
    }

    /**
     *   Attributes:
     *   overwriteEditable - if existing, value overwrites global editable
     */
    def xEditableBoolean = { attrs, body ->
        try {
            boolean editable = _isEditable(request.getAttribute('editable'), attrs.overwriteEditable)

            if ( editable ) {

                String oid 			= "${attrs.owner.class.name}:${attrs.owner.id}"
                String update_link 	= createLink(controller:'ajax', action: 'editableSetValue').encodeAsHTML()
                String data_link 	= createLink(controller:'ajaxJson', action: 'getBooleans').encodeAsHTML()
                String id 			= attrs.id ?: "${oid}:${attrs.field}"
                String default_empty = message(code:'default.button.edit.label')
                String emptyText    = attrs.emptytext ? " data-emptytext=\"${attrs.emptytext}\"" : " data-emptytext=\"${default_empty}\""

                out << "<span>"

                int intValue = attrs.owner[attrs.field] ? 1 : 0
                String strValue = intValue ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')

                // Output an editable link
                out << "<a href=\"#\" id=\"${id}\" class=\"xEditableManyToOne\" "
                if(attrs.owner instanceof SurveyResult){
                    out << "data-onblur=\"submit\" "
                }else {
                    out << "data-onblur=\"ignore\" "
                }
                out <<  " data-value=\"${intValue}\" data-pk=\"${oid}\" data-type=\"select\" " +
                        " data-name=\"${attrs.field}\" data-source=\"${data_link}\" data-url=\"${update_link}\" ${emptyText}>"

                out << "${strValue}</a></span>"
            }
            else {

                int intValue = attrs.owner[attrs.field] ? 1 : 0
                String strValue = intValue ? RDStore.YN_YES.getI10n('value') : RDStore.YN_NO.getI10n('value')
                out << strValue
            }
        }
        catch ( Throwable e ) {
            log.error("Problem processing editable boolean ${attrs}",e)
        }
    }

    def simpleHiddenValue = { attrs, body ->
        String default_empty = message(code:'default.button.edit.label')

        if (attrs.type == "date") {
            out << '<div class="ui calendar datepicker">'
        }
        out << "<a href=\"#\" class=\"simpleHiddenValue ${attrs.class?:''}\""

        if (attrs.type == "date") {
            out << " data-type=\"text\"" // combodate | date

            String df = "${message(code:'default.date.format.notime').toUpperCase()}"
            out << " data-format=\"${df}\""
            out << " data-viewformat=\"${df}\""
            out << " data-template=\"${df}\""

            default_empty = message(code:'default.date.format.notime.normal')

            if (attrs.language) {
                out << " data-datepicker=\"{ 'language': '${attrs.language}' }\" language=\"${attrs.language}\""
            }
        }
        else if (attrs.type == "refdata") {
            String data_link = createLink(controller: 'ajax', action: 'select2RefdataSearch', params:[id: attrs.category, format: 'json'])
            out << " data-type=\"select\" data-source=\"${data_link}\" "
        }
        else {
            out << " data-type=\"${attrs.type?:'text'}\" "
        }

        String emptyText = attrs.emptytext ? " data-emptytext=\"${attrs.emptytext}\"" : " data-emptytext=\"${default_empty}\""

        out << "data-hidden-id=\"${attrs.name}\" ${emptyText} >${attrs.value?:''}</a>"
        out << "<input type=\"hidden\" id=\"${attrs.id}\" name=\"${attrs.name}\" value=\"${attrs.value?:''}\"/>"

        if (attrs.type == "date") {
            out << '</div>'
        }
    }

    private String _renderObjectValue(value) {
        String result = ''
        String not_set = message(code:'refdata.notSet')

        if ( value ) {
            switch ( value.class ) {
                case Role.class:
                    result = message(code: "cv.roles." + value.authority)
                    break
                case RefdataValue.class:
                    result = value.value ? value.getI10n('value') : not_set
                    break
                default:
                    if (! (value instanceof String)){
                        value = value.toString()
                    }
                    String no_ws = value.replaceAll(' ','')
                    result = message(code: "refdata.${no_ws}", default: "${value ?: not_set}")
            }
        }
        result
    }

    private boolean _isEditable(editable, overwrite) {

        boolean result = Boolean.valueOf(editable)

        List positive = [true, 'true', 'True', 1]
        List negative = [false, 'false', 'False', 0]

        if (overwrite in positive) {
            result = true
        }
        else if (overwrite in negative) {
            result = false
        }

        result
    }

    /**
     *   Attributes:
     *   owner - Object
     *   field - property
     *   type - type of input
     *   validation - trigger js validation
     *   id [optional] -
     *   class [optional] - additional classes
     *   overwriteEditable - if existing, value overwrites global editable
     */
    def xEditableAsIcon = { attrs, body ->

        // TODO: data-type="combodate" data-value="1984-05-15" data-format="YYYY-MM-DD" data-viewformat="DD/MM/YYYY" data-template="D / MMM / YYYY"

        boolean editable = _isEditable(request.getAttribute('editable'), attrs.overwriteEditable)

        if (editable) {

            String oid           = "${attrs.owner.class.name}:${attrs.owner.id}"
            String id            = attrs.id ?: "${oid}:${attrs.field}"
            String default_empty = message(code:'default.button.edit.label')
            String data_link     = null

            out << "<a style=\"display: inline-block;\" href=\"#\" id=\"${id}\" class=\"xEditableValue ${attrs.class ?: ''}\""

            if(attrs.owner instanceof SurveyResult){
                out << " data-onblur=\"submit\""
            }else {
                out << " data-onblur=\"ignore\""
            }

            if (attrs.type == "date") {
                out << " data-type=\"text\"" // combodate | date

                String df = "${message(code:'default.date.format.notime').toUpperCase()}"
                out << " data-format=\"${df}\""
                out << " data-viewformat=\"${df}\""
                out << " data-template=\"${df}\""

                default_empty = message(code:'default.date.format.notime.normal')

            } else {
                out << " data-type=\"${attrs.type?:'text'}\""
            }
            out << " data-pk=\"${oid}\""
            out << " data-name=\"${attrs.field}\""


            if (attrs.validation) {
                out << " data-validation=\"${attrs.validation}\" "
            }

            switch (attrs.type) {
                case 'date':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'date', format:"${message(code:'default.date.format.notime')}"]).encodeAsHTML()
                    break
                case 'url':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'url']).encodeAsHTML()
                    break
                default:
                    data_link = createLink(controller:'ajax', action: 'editableSetValue').encodeAsHTML()
                    break
            }

            if (attrs.emptytext)
                out << " data-emptytext=\"${attrs.emptytext}\""
            else {
                out << " data-emptytext=\"${default_empty}\""
            }

            if (attrs.type == "date" && attrs.language) {
                out << "data-datepicker=\"{ 'language': '${attrs.language}' }\" language=\"${attrs.language}\" "
            }

            out << " data-url=\"${data_link}\""

                String oldValue = ''
                if (attrs.owner[attrs.field] && attrs.type=='date') {
                    SimpleDateFormat sdf = new SimpleDateFormat(attrs.format?: message(code:'default.date.format.notime'))
                    oldValue = sdf.format(attrs.owner[attrs.field])
                }
                else {
                    if ((attrs.owner[attrs.field] == null) || (attrs.owner[attrs.field].toString().length()==0)) {
                    }
                    else {
                        oldValue = attrs.owner[attrs.field].encodeAsHTML()
                    }
                }
            out << " data-oldvalue=\"${oldValue}\" "
            out << " data-value=\"${oldValue}\" data-autotext=\"never\">"
            out << "<span class=\"la-popup-tooltip la-delay \" data-position=\"\" data-content=\""
            out << oldValue
            out << "\"><i class=\"${attrs.iconClass ?: 'info'} ${oldValue ? 'green' : 'la-light-grey'} icon\"></i>"
            out << '</span>'

            out << "</a>"
        }
        // !editable
        else {

            if (attrs.owner[attrs.field]) {
                out << "<span class=\"la-popup-tooltip la-delay ui icon\" data-position=\"top right\" data-content=\""

                if (attrs.owner[attrs.field] && attrs.type == 'date') {
                    SimpleDateFormat sdf = new SimpleDateFormat(attrs.format ?: message(code: 'default.date.format.notime'))
                    out << sdf.format(attrs.owner[attrs.field])
                } else {
                    if ((attrs.owner[attrs.field] == null) || (attrs.owner[attrs.field].toString().length() == 0)) {
                    } else {
                        out << attrs.owner[attrs.field]
                    }
                }
                out << "\"><i class=\"${attrs.iconClass ?: 'info'} ${attrs.owner[attrs.field] ? 'green' : 'la-light-grey'} icon\"></i>"
                out << '</span>'
            }
        }
    }
}
