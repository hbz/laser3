package de.laser

import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils
import org.springframework.context.i18n.LocaleContextHolder

import java.text.NumberFormat

@Transactional
class EscapeService {

    String escapeString(String input) {
        String output = input.replaceAll(' ','_')
        String[] escapingChars = ['ä','ö','ü','ß','Ä','Ö','Ü']
        String[] replacement = ['ae','oe','ue','ss','Ae','Oe','Ue']
        output = StringUtils.replaceEach(output,escapingChars,replacement).replaceAll('[\'-,\\\\./;:]','')
        output
    }

    String replaceUmlaute(String input) {

        String result = input.replaceAll("ä", "ae")
                .replaceAll("ö", "oe")
                .replaceAll("ü", "ue")
                .replaceAll("ß", "ss")

        result = result.replaceAll("Ä", "Ae")
                .replaceAll("Ö", "Oe")
                .replaceAll("Ü", "Ue")

        return result;

    }

    BigDecimal parseFinancialValue(String input) {
        String uniformedThousandSeparator = input.replaceAll("[',.]/d{3}","")
        BigDecimal output = new BigDecimal(uniformedThousandSeparator.replaceAll(",","."))
        output
    }

    String outputFinancialValue(input) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale())
        nf.format(input)
    }
}
