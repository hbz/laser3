package de.laser.utils

import de.laser.custom.CustomWkhtmltoxService
import grails.util.Holders
import groovy.util.logging.Slf4j

@Slf4j
class PdfUtils {

    public static final String LANDSCAPE_DYNAMIC   = 'LANDSCAPE_DYNAMIC'
    public static final String PORTRAIT_FIXED_A4   = 'PORTRAIT_FIXED_A4'


    static byte[] getPdf(Map<String, Object> pdfOutput, String format, String template) {
        log.debug('getPdf( ' + format + ', ' + template + ' )')

        CustomWkhtmltoxService wkhtmltoxService = Holders.grailsApplication.mainContext.getBean('wkhtmltoxService') as CustomWkhtmltoxService

        Map<String, Object> pageStruct = [:]

        if (format == LANDSCAPE_DYNAMIC) {
            pageStruct = [
                    orientation : 'Landscape',
                    pageSize    : '',
                    width       : pdfOutput.mainHeader.size() * 15,
                    height      : 35,
            ] as Map<String, Object>

            if      (pageStruct.width > 85 * 4) { pageStruct.pageSize = 'A0' }
            else if (pageStruct.width > 85 * 3) { pageStruct.pageSize = 'A1' }
            else if (pageStruct.width > 85 * 2) { pageStruct.pageSize = 'A2' }
            else if (pageStruct.width > 85)     { pageStruct.pageSize = 'A3' }

            pdfOutput.struct = [pageStruct.pageSize + ' ' + pageStruct.orientation]
        }
        else if (format == PORTRAIT_FIXED_A4) {
            pageStruct = [
                    orientation : 'Portrait',
                    pageSize    : 'A4',
                    width       : 85,
                    height      : 35,
            ] as Map<String, Object>

            pdfOutput.struct = [pageStruct.width, pageStruct.height, pageStruct.pageSize + ' ' + pageStruct.orientation]
        }

        if (pageStruct) {
            wkhtmltoxService.makePdf(
                    view            : template,
                    model           : pdfOutput,
                    pageSize        : pageStruct.pageSize,
                    orientation     : pageStruct.orientation,
                    marginLeft      : 10,
                    marginRight     : 10,
                    marginTop       : 15,
                    marginBottom    : 15
            )
        }
        else {
            log.debug ('no pageStruct @ getPdf()')
        }
    }
}
