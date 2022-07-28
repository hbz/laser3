package de.laser

import de.laser.utils.SwissKnife

class UiAjaxTagLib {

    static namespace = 'ui'

    def remoteLink = {attrs, body ->

        String cssClass = attrs.remove('class')
        String role = attrs.remove('role')
        String ariaLabel = attrs.remove('ariaLabel')

        Closure switchEntries = { keys ->
            Map<String, Object> map = [:]

            keys.each { key ->
                map.put(key, attrs.get(key))
                attrs.remove(key)
            }
            map
        }

        Map<String, Object> hrefMap = switchEntries(['controller', 'action', 'id', 'params']) as Map

        String href = g.createLink(hrefMap)

        out << '<a aria-label="' + ariaLabel + '" role="' + role + '" class="' + cssClass + ' la-js-remoteLink"  href="' + href + '"'

        attrs.each { k,v ->
            out << ' ' << k << '="' << v << '"'
        }

        out << '>'
        out << body()
        out << '</a>'
    }

    def remoteForm = { attrs, body ->

        attrs.class = ((attrs.class ?: '') + ' la-js-remoteForm')

        def url = attrs.url
        if (!(url instanceof CharSequence)) {
            url = SwissKnife.deepClone(attrs.url)
        }
        attrs.remove('url')

        Map params = [
                method: (attrs.method? attrs.method : 'post'),
                action: (attrs.action? attrs.action : url instanceof CharSequence ? url.toString() : createLink(url))
        ]
        params.putAll(attrs)

        if (params.name && !params.id) {
            params.id = params.name
        }
        params.remove 'name'

        out << withTag(name:'form', attrs:params) {
            out << body()
        }
    }

    def remoteJsOnChangeHandler = { attrs, body ->

        String href   = g.createLink([controller: attrs.controller, action: attrs.action])
        String data   = attrs.data ?: '{}'
        String update = attrs.update
        String updateOnFailure = attrs.updateOnFailure ?: update

        out << "jQuery.ajax({type:'POST'"
        out << ", data:" + data
        out << ", url:'" + href + "'"
        out << ", success:function(data,textStatus){jQuery('" + update + "').html(data);}"
        out << ", error:function(XMLHttpRequest,textStatus,errorThrown){jQuery('" + updateOnFailure + "').html(XMLHttpRequest.responseText)}"
        out <<"});"
    }
}