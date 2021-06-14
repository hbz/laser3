package de.laser.custom

import de.laser.helper.ConfigUtils
import org.grails.plugins.wkhtmltopdf.PartialView
import org.grails.plugins.wkhtmltopdf.WkhtmltoxException
import org.grails.plugins.wkhtmltopdf.WkhtmltoxWrapper

class CustomWkhtmltoxService /* extends WkhtmltoxService */ {

    static transactional = false

    def mailMessageContentRenderer
    def grailsApplication

    byte[] makePdf(config) {

        WkhtmltoxWrapper wrapper = new WkhtmltoxWrapper()

        def view = config.remove("view")
        def model = config.remove("model")
        def plugin = config.remove("plugin")
        def header = config.remove("header")
        def footer = config.remove("footer")

        config.encoding = config.encoding ?: "UTF-8"

        PartialView contentPartial = new PartialView(view, model, plugin)
        PartialView headerPartial
        PartialView footerPartial

        if (header) {
            headerPartial = new PartialView(header, model, plugin)
        }
        if (footer) {
            footerPartial = new PartialView(footer, model, plugin)
        }

        config.each { key, value ->
            wrapper."$key" = value
        }

        return makePdf(wrapper, contentPartial, headerPartial, footerPartial)
    }

    byte[] makePdf(WkhtmltoxWrapper wrapper, contentPartial, headerPartial = null, footerPartial = null) {

        String htmlBodyContent = renderMailView(contentPartial)

        File headerFile
        if (headerPartial) {
            headerFile = makePartialViewFile(headerPartial)
            //We don't need "file://" prefix. See https://github.com/wkhtmltopdf/wkhtmltopdf/issues/1645.  It doesn't work on windows
            wrapper.headerHtml = headerFile.absolutePath
        }
        File footerFile
        if (footerPartial) {
            footerFile = makePartialViewFile(footerPartial)
            wrapper.footerHtml = footerFile.absolutePath
        }

        def wkhtmltopdfConfig = grailsApplication.config.grails.plugin.wkhtmltopdf
        def xcvbRunCmd = ConfigUtils.getWkhtmltopdfXcvbRunCmd()

        String binaryFilePath = wkhtmltopdfConfig.binary.toString()
        if (!(new File(binaryFilePath)).exists()) {
            throw new WkhtmltoxException("Cannot find wkhtmltopdf executable at $binaryFilePath")
        }
        xcvbRunCmd = xcvbRunCmd ? xcvbRunCmd.toString() : null

        byte[] pdfData = new CustomWkhtmltoxExecutor(binaryFilePath, xcvbRunCmd, wrapper).generatePdf(htmlBodyContent)
        try {
            if (headerFile) {
                headerFile.delete()
            }
            if (footerFile) {
                footerFile.delete()
            }
        } catch (SecurityException e) {
            log.error("Error deleting temp file: ${e.message}", e)
        }
        return pdfData
    }

    protected String renderMailView(PartialView partialView) {
        return mailMessageContentRenderer.render(new StringWriter(), partialView.viewName, partialView.model, null, partialView.pluginName).out.toString()
    }

    File makePartialViewFile(PartialView pv) {
        String content = renderMailView(pv)
        File tempFile = File.createTempFile("/wkhtmltopdf", ".html")
        tempFile.withWriter("UTF8") {
            it.write(content)
            it.close()
        }
        tempFile.setReadable(true, true)
        tempFile.setWritable(true, true)
        return tempFile
    }
}
