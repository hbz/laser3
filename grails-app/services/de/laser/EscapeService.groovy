package de.laser

import grails.gorm.transactions.Transactional
import org.apache.commons.lang.StringUtils
import org.springframework.context.i18n.LocaleContextHolder

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.NumberFormat

@Transactional
class EscapeService {

    String escapeString(String input) {
        String output = input.replaceAll(' ','_')
        String[] escapingChars = ['ä','ö','ü','ß','Ä','Ö','Ü']
        String[] replacement = ['ae','oe','ue','ss','Ae','Oe','Ue']
        output = StringUtils.replaceEach(output,escapingChars,replacement).replaceAll('[\'-,\\\\./;:?!]','')
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

        result
    }

    BigDecimal parseFinancialValue(String input) {
        String uniformedThousandSeparator = input.replaceAll("[',.](/d{3})",'$1')
        BigDecimal output = new BigDecimal(uniformedThousandSeparator.replaceAll(",","."))
        output
    }

    String outputFinancialValue(input) {
        //workaround for the correct currency output but without currency symbol, according to https://stackoverflow.com/questions/8658205/format-currency-without-currency-symbol
        NumberFormat nf = NumberFormat.getCurrencyInstance(LocaleContextHolder.getLocale())
        DecimalFormatSymbols dcf = ((DecimalFormat) nf).getDecimalFormatSymbols()
        dcf.setCurrencySymbol("")
        ((DecimalFormat) nf).setDecimalFormatSymbols(dcf)
        nf.format(input)
    }

    String generateSortTitle(String name) {
        //group all sortname generators here
        String sortname = Normalizer.normalize(name, Normalizer.Form.NFKD).trim().toLowerCase()
        sortname = sortname.replaceFirst('^copy of ', '')
        sortname = sortname.replaceFirst('^the ', '')
        sortname = sortname.replaceFirst('^a ', '')
        sortname = sortname.replaceFirst('^der ', '')
        sortname = sortname.replaceFirst('^die ', '')
        sortname = sortname.replaceFirst('^das ', '')
        sortname
    }
}
