package de.laser

import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.StringUtils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.NumberFormat

/**
 * This central service should contain string escaping methods
 */
@Transactional
class EscapeService {

    /**
     * Replaces special characters from the given input
     * @param input the string to purge
     * @return the sanitised output string
     */
    String escapeString(String input) {
        String output = input.replaceAll(' ','_')
        String[] escapingChars = ['ä','ö','ü','ß','Ä','Ö','Ü']
        String[] replacement = ['ae','oe','ue','ss','Ae','Oe','Ue']
        output = StringUtils.replaceEach(output,escapingChars,replacement).replaceAll('[\'-,\\\\./;:?!]','')
        output
    }

    /**
     * Replaces only the German Umlauts (but no other special characters) by their cross-word writing (the so-called Kreuzworträtselschreibweise)
     * @param input the string to purge
     * @return the sanitised output string
     */
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

    /**
     * Parses the given input sum and converts them into a BigDecimal representation
     * @param input the input sum to parse
     * @return the sum parsed as BigDecimal
     */
    BigDecimal parseFinancialValue(String input) {
        String uniformedThousandSeparator = input.replaceAll("[',.](/d{3})",'$1')
        BigDecimal output = new BigDecimal(uniformedThousandSeparator.replaceAll(",","."))
        output
    }

    /**
     * Called from cost table and cost input views
     * Returns the input sum in the localised format
     * @param input the sum value to format
     * @return the formatted output of the cost
     */
    String outputFinancialValue(input) {
        //workaround for the correct currency output but without currency symbol, according to https://stackoverflow.com/questions/8658205/format-currency-without-currency-symbol
        NumberFormat nf = NumberFormat.getCurrencyInstance( LocaleUtils.getCurrentLocale() )
        DecimalFormatSymbols dcf = ((DecimalFormat) nf).getDecimalFormatSymbols()
        dcf.setCurrencySymbol("")
        ((DecimalFormat) nf).setDecimalFormatSymbols(dcf)
        nf.format(input)
    }

    /**
     * Copy from IssueEntitlement and should replace the methods in the domain classes (see below)
     * Generates a normalised sort name from the given input
     * @param name the name to normalise
     * @return the normalised sort name string
     * @see IssueEntitlement#generateSortTitle()
     * @see TitleInstancePackagePlatform#generateSortTitle()
     * @see Package#generateSortName(java.lang.String)
     */
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
