package de.laser.utils

import de.laser.custom.CustomWkhtmltoxService
import grails.util.Holders
import groovy.util.logging.Slf4j

@Slf4j
class PdfUtils {

    static Map<String, Object> getPageStruct_Landscape(int width) {

        Map<String, Object> pageStruct = [
                orientation : 'Landscape',
                pageSize    : '',
                width       : width * 15,
                height      : 35,
        ]

        if      (pageStruct.width > 85 * 4) { pageStruct.pageSize = 'A0' }
        else if (pageStruct.width > 85 * 3) { pageStruct.pageSize = 'A1' }
        else if (pageStruct.width > 85 * 2) { pageStruct.pageSize = 'A2' }
        else if (pageStruct.width > 85)     { pageStruct.pageSize = 'A3' }

        pageStruct
    }

    static byte[] getPdf(Map<String, Object> pdfOutput, String template) {
        log.debug('getPdf( ' + template + ' )')

        CustomWkhtmltoxService wkhtmltoxService = Holders.grailsApplication.mainContext.getBean('wkhtmltoxService') as CustomWkhtmltoxService

        Map<String, Object> pageStruct = getPageStruct_Landscape(pdfOutput.mainHeader.size())
        pdfOutput.struct               = [pageStruct.pageSize + ' ' + pageStruct.orientation]

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
}
