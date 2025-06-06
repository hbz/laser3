package de.laser

import de.laser.utils.RandomUtils

// new ui core components --- do NOT modify ---
// new ui core components --- do NOT modify ---
// new ui core components --- do NOT modify ---

class UiCoreTagLib {

    static namespace = 'cc'

    /**
     *  @param owner MUST
     *  @param field MUST
     *  @param overwriteEditable OPT
     *  @param label OPT
     */
    def toggle = { attrs, body ->

        if (!_checkOwner(attrs)) {
            out << '<span class="ui red label"> &lt;cc:toggle/> </span>'
        }
        else {
            boolean checked = 'yes'.equalsIgnoreCase(attrs.owner[attrs.field]?.value)
            String oid      = attrs.owner.class.name + ':' + attrs.owner.id
            String rndId    = RandomUtils.getHtmlID()

            out << '<span class="cc-element cc-toggle" id="cc-element-' + rndId + '">'
            out <<   '<span class="ui toggle checkbox">'

            if (_isEditable(request.getAttribute('editable'), attrs.overwriteEditable)) {
                out << '<input type="checkbox" name="cc-element-value-' + rndId + '"'
                out <<   ' ' + (checked ? 'checked="checked"' : '') + ' data-pk="' + oid + '" data-name="' + attrs.field + '" />'
            }
            else {
                out << '<input type="checkbox" disabled="disabled" ' + (checked ? 'checked="checked" />' : '/>')
            }

            if (attrs.label) { out << '<label>' + attrs.label + '</label>' }
            out <<   '</span>'
            out << '</span>'
        }
    }

    /**
     *  @param owner MUST
     *  @param field MUST
     *  @param overwriteEditable OPT
     *  @param label OPT
     */
    def boogle = { attrs, body ->

        if (!_checkOwner(attrs)) {
            out << '<span class="ui red label"> &lt;cc:boogle/> </span>'
        }
        else {
            boolean checked = true == attrs.owner[attrs.field]
            String oid      = attrs.owner.class.name + ':' + attrs.owner.id
            String rndId    = RandomUtils.getHtmlID()

            out << '<span class="cc-element cc-boogle" id="cc-element-' + rndId + '">'
            out <<   '<span class="ui toggle checkbox">'

            if (_isEditable(request.getAttribute('editable'), attrs.overwriteEditable)) {
                out << '<input type="checkbox" name="cc-element-value-' + rndId + '"'
                out <<   ' ' + (checked ? 'checked="checked"' : '') + ' data-pk="' + oid + '" data-name="' + attrs.field + '" />'
            }
            else {
                out << '<input type="checkbox" disabled="disabled" ' + (checked ? 'checked="checked" />' : '/>')
            }

            if (attrs.label) { out << '<label>' + attrs.label + '</label>' }
            out <<   '</span>'
            out << '</span>'
        }
    }

    private boolean _checkOwner(Map attrs) {
        Object o = attrs.owner
        String f = attrs.field

        o && f && o.hasProperty(f)
    }

    private boolean _isEditable(editable, overwrite) {
        boolean result = Boolean.valueOf(editable)

        if (overwrite in [true, 'true', 'True', 1]) {
            result = true
        }
        else if (overwrite in [false, 'false', 'False', 0]) {
            result = false
        }
        result
    }
}
