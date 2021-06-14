package de.laser.custom

import groovy.util.logging.Commons
import org.grails.plugins.wkhtmltopdf.WkhtmltoxException
import org.grails.plugins.wkhtmltopdf.WkhtmltoxWrapper

@Commons
class CustomWkhtmltoxExecutor /* extends WkhtmltoxExecutor */ {

    String binaryPath
    String xvfbRunCmd
    WkhtmltoxWrapper wrapper

    CustomWkhtmltoxExecutor(String binaryPath, String xvfbRunCmd, WkhtmltoxWrapper wrapper) {

        if (!(new File(binaryPath)).exists()) {
            throw new WkhtmltoxException("Could not locate Wkhtmltox binary.")
        }

        if (!wrapper) {
            throw new WkhtmltoxException("Wrapper must be set.")
        }

        this.binaryPath = binaryPath
        this.xvfbRunCmd = xvfbRunCmd
        this.wrapper = wrapper
    }

    byte[] generatePdf(String html) {
        def stderr
        try {
            def commandList = wrapper.toArgumentsList()
            commandList.add(0, binaryPath)
            commandList << "-q" << "-" << "-"

            if (xvfbRunCmd) {
                commandList.add(0, xvfbRunCmd)
            }

            log.info("Invoking wkhtml2pdf with command $commandList")
            log.trace "Following html will be converted to PDF: $html"
            def process = (commandList as String[]).execute()
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
