package de.laser.custom

import groovy.util.logging.Commons
import org.grails.plugins.wkhtmltopdf.WkhtmltoxException
import org.grails.plugins.wkhtmltopdf.WkhtmltoxWrapper

/**
 * Class to implement the Wkhtmltox execution for PDF generation of a prepared file
 */
@Commons
class CustomWkhtmltoxExecutor /* extends WkhtmltoxExecutor */ {

    String binaryPath
    String xvfbRunner
    WkhtmltoxWrapper wrapper

    /**
     * Constructor to configure the binary paths of the output generators
     * @param binaryPath the path to the Wkhtmltox binary
     * @param xvfbRunner the path to the xvfbRunner binary
     * @param wrapper the wrapper instance performing the output generation
     */
    CustomWkhtmltoxExecutor(String binaryPath, String xvfbRunner, WkhtmltoxWrapper wrapper) {

        if (!(new File(binaryPath)).exists()) {
            throw new WkhtmltoxException("Could not locate Wkhtmltox binary.")
        }
        if (xvfbRunner && !(new File(xvfbRunner)).exists()) {
            throw new WkhtmltoxException("Could not locate xvfb-run binary.")
        }
        if (!wrapper) {
            throw new WkhtmltoxException("Wrapper must be set.")
        }

        wrapper.disableJavascript = true
        wrapper.disableLocalFileAccess = true

        this.binaryPath = binaryPath
        this.xvfbRunner = xvfbRunner
        this.wrapper = wrapper
    }

    /**
     * Generates a PDF file from the given HTML page
     * @param html the page with HTML markup, passed as string
     * @return a byte array containing the PDF file
     */
    byte[] generatePdf(String html) {
        def stderr
        try {
            def commandList = wrapper.toArgumentsList()
            commandList.add(0, binaryPath)
            commandList << "-q" << "-" << "-"

            if (xvfbRunner) {
               commandList.add(0, xvfbRunner)
            }

            log.info("Invoking wkhtml2pdf with command $commandList")
            log.trace "Following html will be converted to PDF: $html"

            //def process = (commandList as String[]).execute()
            def process = (commandList.join(' ')).execute()
            def stdout = new ByteArrayOutputStream()
            stderr = new ByteArrayOutputStream()
            OutputStreamWriter os = new OutputStreamWriter(process.outputStream, "UTF8")
            os.write(html)
            os.close()

            process.waitForProcessOutput(stdout, stderr)
            return stdout.toByteArray()
        }
        catch (e) {
            throw new WkhtmltoxException(e)
        }
        finally {
            if (stderr) {
                def bytes = stderr.toByteArray()
                if (bytes.length) {
                    log.error new String(bytes)
                }
            }
        }
    }
}
