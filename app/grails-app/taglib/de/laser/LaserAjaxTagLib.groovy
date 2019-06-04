package de.laser

import org.springframework.web.util.HtmlUtils

class LaserAjaxTagLib {

    def springSecurityService

    static namespace = 'laser'

    def remoteLink = {attrs, body ->

        def cssClass = attrs.class
        def id = attrs.id

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

        out << "<a class='${cssClass} la-js-remoteLink'  href='" + href + "'"



        attrs.each { k,v ->
            out << ' ' << k << '="' << v << '"'
        }

        out << '>'

        out << body()

        out << '</a>'

        println attrs
    }
}
