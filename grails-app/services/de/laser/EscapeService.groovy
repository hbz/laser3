package de.laser

import de.laser.utils.LocaleUtils
import grails.gorm.transactions.Transactional
import org.apache.commons.lang3.StringUtils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.NumberFormat
import java.util.regex.Pattern

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
        output = StringUtils.replaceEach(output,escapingChars,replacement).replaceAll('[\'-,\\\\./;:?!# ]','')
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
        if(input.length() > 0) {
            String uniformedThousandSeparator = input.replaceAll("[',.](\\d{3})",'$1').trim()
            BigDecimal output = new BigDecimal(uniformedThousandSeparator.replaceAll(",","."))
            output
        }
        else null
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

    String getFinancialOutputQuery(String sqlCol) {
        switch (LocaleUtils.getCurrentLocale()) {
            case [Locale.GERMANY, Locale.GERMAN]:
                return "replace(to_char(${sqlCol}, '999999999.99'), '.', ',')"
            default:
                return "to_char(${sqlCol}, '999999999.99')"
        }
    }

    /**
     * Copy from IssueEntitlement and should replace the methods in the domain classes (see below)
     * Generates a normalised sort name from the given input
     * @param name the name to normalise
     * @return the normalised sort name string
     * @see de.laser.wekb.TitleInstancePackagePlatform#generateSortTitle()
     * @see de.laser.wekb.Package#generateSortName(java.lang.String)
     */
    String generateSortTitle(String name) {
        Pattern alphanum = Pattern.compile("\\p{Punct}|\\p{Cntrl}|( ?« ?)+|( ?» ?)+|[‹›¿¡‘’“”„‚‟§]")
        //group all sortname generators here
        String sortname = Normalizer.normalize(name, Normalizer.Form.NFKD).trim().toLowerCase()
        sortname = sortname.replaceAll('&',' and ')
        sortname = sortname.trim()
        sortname = sortname.toLowerCase()
        sortname = alphanum.matcher(sortname).replaceAll("")
        sortname = sortname.replaceAll("\\s+", " ").trim()
        sortname = asciify(sortname)
        sortname = sortname.replaceAll('°', ' ') //no trailing or leading °, so trim() is not needed
        sortname
    }

    /**
     * Converts the given stream of characters into their representation in ASCII
     * @param s the string to convert
     * @return the string whose special character letters have been converted
     */
    String asciify(String s) {
        char[] c = s.toCharArray()
        StringBuffer b = new StringBuffer()
        for (char element : c) {
            b.append( translateChar(element) )
        }
        return b.toString()
    }

    /**
     * Selects to the given special char its closest ASCII char
     * @param c the character to transform
     * @return the ASCII representation of the char
     */
    char translateChar(char c) {
        switch(c) {
            case ['\u00C0', '\u00C1', '\u00C2', '\u00C3', '\u00C4', '\u00C5', '\u00E0', '\u00E1', '\u00E2', '\u00E3', '\u00E4', '\u00E5', '\u0100', '\u0101', '\u0102', '\u0103', '\u0104', '\u0105']: return 'a'
            case ['\u00C7', '\u00E7', '\u0106', '\u0107', '\u0108', '\u0109', '\u010A', '\u010B', '\u010C', '\u010D']: return 'c'
            case ['\u00D0', '\u00F0', '\u010E', '\u010F', '\u0110', '\u0111']: return 'd'
            case ['\u00C8', '\u00C9', '\u00CA', '\u00CB', '\u00E8', '\u00E9', '\u00EA', '\u00EB', '\u0112', '\u0113', '\u0114', '\u0115', '\u0116', '\u0117', '\u0118', '\u0119', '\u011A', '\u011B']: return 'e'
            case ['\u011C', '\u011D', '\u011E', '\u011F', '\u0120', '\u0121', '\u0122', '\u0123']: return 'g'
            case ['\u0124', '\u0125', '\u0126', '\u0127']: return 'h'
            case ['\u00CC', '\u00CD', '\u00CE', '\u00CF', '\u00EC', '\u00ED', '\u00EE', '\u00EF', '\u0128', '\u0129', '\u012A', '\u012B', '\u012C', '\u012D', '\u012E', '\u012F', '\u0130', '\u0131']: return 'i'
            case ['\u0134', '\u0135']: return 'j'
            case ['\u0136', '\u0137', '\u0138']: return 'k'
            case ['\u0139', '\u013A', '\u013B', '\u013C', '\u013D', '\u013E', '\u013F', '\u0140', '\u0141', '\u0142']: return 'l'
            case ['\u00D1', '\u00F1', '\u0143', '\u0144', '\u0145', '\u0146', '\u0147', '\u0148', '\u0149', '\u014A', '\u014B']: return 'n'
            case ['\u00D2', '\u00D3', '\u00D4', '\u00D5', '\u00D6', '\u00D8', '\u00F2', '\u00F3', '\u00F4', '\u00F5', '\u00F6', '\u00F8', '\u014C', '\u014D', '\u014E', '\u014F', '\u0150', '\u0151']: return 'o'
            case ['\u0154', '\u0155', '\u0156', '\u0157', '\u0158', '\u0159']: return 'r'
            case ['\u015A', '\u015B', '\u015C', '\u015D', '\u015E', '\u015F', '\u0160', '\u0161', '\u017F']: return 's'
            case ['\u0162', '\u0163', '\u0164', '\u0165', '\u0166', '\u0167']: return 't'
            case ['\u00D9', '\u00DA', '\u00DB', '\u00DC', '\u00F9', '\u00FA', '\u00FB', '\u00FC', '\u0168', '\u0169', '\u016A', '\u016B', '\u016C', '\u016D', '\u016E', '\u016F', '\u0170', '\u0171', '\u0172', '\u0173']: return 'u'
            case ['\u0174', '\u0175']: return 'w'
            case ['\u00DD', '\u00FD', '\u00FF', '\u0176', '\u0177', '\u0178']: return 'y'
            case ['\u0179', '\u017A', '\u017B', '\u017C', '\u017D', '\u017E']: return 'z'
        }
        return c
    }
}
