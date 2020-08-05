package de.laser

import de.laser.helper.SwissKnife
import org.springframework.web.util.HtmlUtils

class LaserAjaxTagLib {

    def springSecurityService

    static namespace = 'laser'

    def remoteLink = {attrs, body ->

        def cssClass = attrs.class
        attrs.remove('class')

        def role = attrs.role
        attrs.remove('role')

        Closure switchEntries = { keys ->
            Map<String, Object> map = [:]

            keys.each { key ->
                map.put(key, attrs.get(key))
                attrs.remove(key)
            }
            map
        }

        Map<String, Object> hrefMap = switchEntries(['controller', 'action', 'id', 'params'])

        String href = g.createLink(hrefMap)

        out << "<a role='${role}' class='${cssClass} la-js-remoteLink'  href='" + href + "'"

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

        def params = [
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
}
