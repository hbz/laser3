package de.laser

import de.laser.utils.RandomUtils

// new X components --- do NOT modify ---
// new X components --- do NOT modify ---
// new X components --- do NOT modify ---

class XTagLib {

    static namespace = 'ui'

    def xToggle = { attrs, body ->

        if (_isEditable(request.getAttribute('editable'), attrs.overwriteEditable)) {
            boolean checked = attrs.owner && attrs.field && attrs.owner[attrs.field]?.value == 'Yes'
            String rndId    = RandomUtils.getHtmlID()

            out << '<span class="x-toggle" id="x-toggle-' + rndId + '">'
            out <<   '<span class="ui toggle checkbox">'
            out <<     '<input type="checkbox" name="x-toggle-value-' + rndId + '" '
            if (checked) { out << 'checked="checked" ' }

            out <<        'data-pk="' + attrs.owner.class.name + ':' + attrs.owner.id + '" data-name="' + attrs.field + '" />'
            if (attrs.label) { out << '<label>' + attrs.label + '</label>' }

            out <<   '</span>'
            out << '</span>'
        }
        else {
            out << '<span>[xToggle blocked]</span>'
        }
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
}
