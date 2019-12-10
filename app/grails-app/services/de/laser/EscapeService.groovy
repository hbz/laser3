package de.laser

import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils
import java.text.SimpleDateFormat

@Transactional
class EscapeService {

    private List<SimpleDateFormat> possibleDateFormats = [
            new SimpleDateFormat('yyyy/MM/dd'),
            new SimpleDateFormat('dd.MM.yyyy'),
            new SimpleDateFormat('dd/MM/yyyy'),
            new SimpleDateFormat('dd/MM/yy'),
            new SimpleDateFormat('yyyy/MM'),
            new SimpleDateFormat('yyyy'),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S")
    ]

    String escapeString(String input) {
        String output = input.replaceAll(' ','_')
        String[] escapingChars = ['ä','ö','ü','ß','Ä','Ö','Ü']
        String[] replacement = ['ae','oe','ue','ss','Ae','Oe','Ue']
        output = StringUtils.replaceEach(output,escapingChars,replacement).replaceAll('[\'-,\\\\./;:]','')
        output
    }

    BigDecimal parseFinancialValue(String input) {
        String uniformedThousandSeparator = input.replaceAll("[',.]/d{3}","")
        BigDecimal output = new BigDecimal(uniformedThousandSeparator.replaceAll(",","."))
        output
    }

    Date parseDate(String datestr) {
        Date parsed_date = null
        if (datestr && (datestr.toString().trim().length() > 0)) {
            for (Iterator<SimpleDateFormat> i = possibleDateFormats.iterator(); (i.hasNext() && (parsed_date == null));) {
                SimpleDateFormat next = i.next()
                try {
                    parsed_date = next.parse(datestr.toString())
                }
                catch (Exception e) {
                    log.info("Invalid parser. Trying next one ...")
                }
            }
        }
        parsed_date
    }

}
