package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue

class SemanticUiInplaceTagLib {

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

        boolean editable = request.getAttribute('editable')
        if (editable == true) {

            def oid           = "${attrs.owner.class.name}:${attrs.owner.id}"
            def id            = attrs.id ?: "${oid}:${attrs.field}"
            def default_empty = message(code:'default.button.edit.label')
            def data_link     = null

            out << "<span id=\"${id}\" class=\"xEditableValue ${attrs.class?:''}\""
            if (attrs.type == "date") {
                out << " data-type=\"combodate\""
                def df = "${message(code:'default.date.format.notime', default:'yyyy-mm-dd').toUpperCase()}"
                out << " data-format=\"${df}\""
                out << " data-viewformat=\"${df}\""
                out << " data-template=\"${df}\""

            } else {
                out << " data-type=\"${attrs.type?:'textarea'}\""
            }
            out << " data-pk=\"${oid}\""
            out << " data-name=\"${attrs.field}\""

            switch (attrs.type) {
                case 'date':
                    data_link = createLink(controller:'ajax', action: 'editableSetValue', params:[type:'date', format:"${message(code:'default.date.format.notime', default:'yyyy-MM-dd')}"]).encodeAsHTML()
                break
                case 'string':
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
            out << ">"

            if (body) {
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
                        out << attrs.owner[attrs.field].encodeAsHTML()
                    }
                }
            }
            out << "</span>"
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
}
