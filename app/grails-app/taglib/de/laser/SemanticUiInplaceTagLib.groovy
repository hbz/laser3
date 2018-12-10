package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue

class SemanticUiInplaceTagLib {

    def genericOIDService

    static namespace = "semui"

    /**
    * Attributes:
    *   owner - Object
    *   field - property
    *   type - type of input
    *   id [optional] -
    *   class [optional] - additional classes
    */
    def xEditable = { attrs, body ->

        // TODO: data-type="combodate" data-value="1984-05-15" data-format="YYYY-MM-DD" data-viewformat="DD/MM/YYYY" data-template="D / MMM / YYYY"

        boolean editable = (request.getAttribute('editable') || attrs.overwriteEditable)

        if (editable == true) {

            def oid           = "${attrs.owner.class.name}:${attrs.owner.id}"
            def id            = attrs.id ?: "${oid}:${attrs.field}"
            def default_empty = message(code:'default.button.edit.label')
            def data_link     = null

            out << "<a href=\"#\" id=\"${id}\" class=\"xEditableValue ${attrs.class ?: ''}\""

            if (attrs.type == "date") {
                out << " data-type=\"text\"" // combodate | date
                def df = "${message(code:'default.date.format.notime', default:'yyyy-mm-dd').toUpperCase()}"
                out << " data-format=\"${df}\""
                out << " data-viewformat=\"${df}\""
                out << " data-template=\"${df}\""

                default_empty = message(code:'default.date.format.notime.normal')

            } else {
                out << " data-type=\"${attrs.type?:'text'}\""
            }
            out << " data-pk=\"${oid}\""
            out << " data-name=\"${attrs.field}\""

            switch (attrs.type) {
                case 'date':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'date', format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"]).encodeAsHTML()
                break
                case 'url':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'url']).encodeAsHTML()
                break
                case 'date':
                default:
                    data_link = createLink(controller:'ajax', action: 'editableSetValue').encodeAsHTML()
                break
            }

            if (attrs?.emptytext)
                out << " data-emptytext=\"${attrs.emptytext}\""
            else {
                out << " data-emptytext=\"${default_empty}\""
            }

            if (attrs.type == "date" && attrs.language) {
                out << "data-datepicker=\"{ 'language': '${attrs.language}' }\" language=\"${attrs.language}\" "
            }

            out << " data-url=\"${data_link}\""

            if (! body) {
                def oldValue = ''
                if (attrs.owner[attrs.field] && attrs.type=='date') {
                    def sdf = new java.text.SimpleDateFormat(attrs.format?: message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
                    oldValue = sdf.format(attrs.owner[attrs.field])
                }
                else {
                    if ((attrs.owner[attrs.field] == null) || (attrs.owner[attrs.field].toString().length()==0)) {
                    }
                    else {
                        oldValue = attrs.owner[attrs.field].encodeAsHTML()
                    }
                }
                out << " data-oldvalue=\"${oldValue}\">"
                out << oldValue
            }
            else {
                out << ">"
                out << body()
            }
            out << "</a>"
        }
        // !editable
        else {
            if ( body ) {
                out << body()
            }
            else {
                if (attrs.owner[attrs.field] && attrs.type=='date') {
                    def sdf = new java.text.SimpleDateFormat(attrs.format?: message(code:'default.date.format.notime', default:'yyyy-MM-dd'))
                    out << sdf.format(attrs.owner[attrs.field])
                }
                else {
                    if ((attrs.owner[attrs.field] == null) || (attrs.owner[attrs.field].toString().length()==0)) {
                    }
                    else {
                        out << attrs.owner[attrs.field]
                    }
                }
            }
        }
    }

    def xEditableRefData = { attrs, body ->
        try {
            boolean editable = (request.getAttribute('editable') || attrs.overwriteEditable)

            if ( editable == true ) {

                def oid = "${attrs.owner.class.name}:${attrs.owner.id}"
                def dataController = attrs.dataController ?: 'ajax'
                def dataAction = attrs.dataAction ?: 'sel2RefdataSearch'
                def data_link = createLink(controller:dataController, action: dataAction, params:[id:attrs.config,format:'json',oid:oid]).encodeAsHTML()
                def update_link = createLink(controller:'ajax', action: 'genericSetRel').encodeAsHTML()
                def id = attrs.id ?: "${oid}:${attrs.field}"
                def default_empty = message(code:'default.button.edit.label')
                def emptyText = attrs?.emptytext ? " data-emptytext=\"${attrs.emptytext}\"" : " data-emptytext=\"${default_empty}\""

                out << "<span>"

                def dataValue = ""
                def obj = genericOIDService.resolveOID(oid)

                if (obj && obj."${attrs.field}") {
                    def tmpId = obj."${attrs.field}".id
                    dataValue = " data-value=\"com.k_int.kbplus.RefdataValue:${tmpId}\" "
                }

                // Output an editable link
                out << "<a href=\"#\" id=\"${id}\" class=\"xEditableManyToOne\" " + dataValue +
                        "data-pk=\"${oid}\" data-type=\"select\" data-name=\"${attrs.field}\" data-source=\"${data_link}\" data-url=\"${update_link}\" ${emptyText}>"

                // Here we can register different ways of presenting object references. The most pressing need to be
                // outputting a a containing an icon for refdata fields.

                out << renderObjectValue(attrs.owner[attrs.field])

                out << "</a></span>"
            }
            else {
                out << renderObjectValue(attrs.owner[attrs.field])
            }
        }
        catch ( Throwable e ) {
            log.error("Problem processing editable refdata ${attrs}",e)
        }
    }

    def simpleHiddenValue = { attrs, body ->
        def default_empty = message(code:'default.button.edit.label')

        if (attrs.type == "date") {
            out << '<div class="ui calendar datepicker">'
        }

        out << "<a href=\"#\" class=\"simpleHiddenRefdata ${attrs.class?:''}\""

        if (attrs.type == "date") {
            out << " data-type=\"text\"" // combodate | date
            def df = "${message(code:'default.date.format.notime', default:'yyyy-mm-dd').toUpperCase()}"
            out << " data-format=\"${df}\""
            out << " data-viewformat=\"${df}\""
            out << " data-template=\"${df}\""

            default_empty = message(code:'default.date.format.notime.normal')

            if (attrs.language) {
                out << " data-datepicker=\"{ 'language': '${attrs.language}' }\" language=\"${attrs.language}\""
            }
        } else {
            out << " data-type=\"${attrs.type?:'text'}\" "
        }

        def emptyText = attrs?.emptytext ? " data-emptytext=\"${attrs.emptytext}\"" : " data-emptytext=\"${default_empty}\""

        out << "data-hidden-id=\"${attrs.name}\" ${emptyText} >${attrs.value?:''}</a>"
        out << "<input type=\"hidden\" id=\"${attrs.id}\" name=\"${attrs.name}\" value=\"${attrs.value?:''}\"/>"

        if (attrs.type == "date") {
            out << '</div>'
        }
    }

    private renderObjectValue(value) {
        def result=''
        def not_set = message(code:'refdata.notSet')

        if ( value ) {
            switch ( value.class ) {
                case com.k_int.kbplus.RefdataValue.class:

                    if ( value.icon != null ) {
                        result="<span class=\"select-icon ${value.icon}\"></span>";
                        result += value.value ? value.getI10n('value') : not_set
                    }
                    else {
                        result = value.value ? value.getI10n('value') : not_set
                    }
                    break;
                default:
                    if(value instanceof String){

                    }else{
                        value = value.toString()
                    }
                    def no_ws = value.replaceAll(' ','')

                    result = message(code:"refdata.${no_ws}", default:"${value ?: not_set}")
            }
        }
        result;
    }
}
