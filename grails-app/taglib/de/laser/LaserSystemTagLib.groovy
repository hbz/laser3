package de.laser


import de.laser.helper.AjaxUtils
import de.laser.helper.ServerUtils
import org.grails.io.support.GrailsResourceUtils

class LaserSystemTagLib {

    static namespace = 'laser'

    def script = { attrs, body ->

        if (AjaxUtils.isXHR(request)) {
            out << "\n<script data-type=\"xhr\">"
            out << "\n\$(function() {"
            out << "\n " + body()
            out << "\n});</script>"
        }
        else {
            Map<String, Object> map = [:]

            if (ServerUtils.getCurrentServer() != ServerUtils.SERVER_PROD) {
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
            out << "\n//-> new asset: ${assetBlock.attrs ?: ''}"
            out << "\n ${assetBlock.body}"
        }

        out << "\n});</script>"
    }
}