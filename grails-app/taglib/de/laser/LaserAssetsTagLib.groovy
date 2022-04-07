package de.laser

import de.laser.helper.AjaxUtils
import de.laser.helper.AppUtils
import grails.util.Environment
import org.grails.io.support.GrailsResourceUtils
import org.grails.web.servlet.mvc.GrailsWebRequest

class LaserAssetsTagLib {

    static namespace = 'laser'

    def javascript = {final attrs ->
        out << asset.javascript(attrs).toString().replace(' type="text/javascript" ', ' data-type="external" ')
    }

    def script = { attrs, body ->

        if (AjaxUtils.isXHR(request)) {
            out << "\n<script data-type=\"xhr\">"
            out << "\n\$(function() {"
            out << "\n " + body()
            out << "\n});</script>"
        }
        else {
            Map<String, Object> map = [:]

            if (AppUtils.getCurrentServer() != AppUtils.PROD) {
                if (attrs.file) {
                    map = [file: GrailsResourceUtils.getPathFromBaseDir(attrs.file)]
                }
                else {
                    map = [uri: request.getRequestURI()]
                }
            }
            asset.script(map, body())
        }
    }

    // adaption of AssetsTagLib.deferredScripts ..

    def scriptBlock = {attrs ->
        def assetBlocks = request.getAttribute('assetScriptBlocks')
        if (!assetBlocks) {
            return
        }

        out << "\n<script data-type=\"scriptBlock\">"
        out << "\n\$(function() {"

        assetBlocks.each {assetBlock ->
            out << "\n//-> asset: ${assetBlock.attrs ?: ''}"
            out << "\n ${assetBlock.body}"
        }

        out << "\n});</script>"
    }

    // render override for dev environment

    def render = { attrs ->

        if (Environment.isDevelopmentMode()) {
            GrailsWebRequest webRequest = getWebRequest()
            String uri = webRequest.getAttributes().getTemplateUri(attrs.template as String, webRequest.getRequest())

            if (attrs.get('model')) {
                out << '<!-- [template: ' + uri + '], [model: ' + (attrs.get('model') as Map).keySet().join(',') + '], START -->'
            } else {
                out << '<!-- [template: ' + uri + '], START -->'
            }

            out << g.render(attrs)
            out << '<!-- [template: ' + uri + '], END -->'
        }
        else {
            out << g.render(attrs)
        }
    }
}