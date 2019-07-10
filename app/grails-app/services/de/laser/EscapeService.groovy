package de.laser

import grails.transaction.Transactional
import org.apache.commons.lang.StringUtils

@Transactional
class EscapeService {

    String escapeString(String input) {
        String output = input.replaceAll(' ','_')
        String[] escapingChars = ['ä','ö','ü','ß','Ä','Ö','Ü']
        String[] replacement = ['ae','oe','ue','ss','Ae','Oe','Ue']
        output = StringUtils.replaceEach(output,escapingChars,replacement).replaceAll('[-,\\\\./;:]','')
        output
    }

    void detectEncoding() {
        /*
            TODO: PHP to translate
            $encodings = array('ASCII', 'UTF-8', 'ISO-8859-15');
            mb_detect_order(implode(', ', $encodings));
            $detectedEncoding = mb_detect_encoding($data);
            if($detectedEncoding != 'ASCII' && $detectedEncoding != 'UTF-8') {
                $data = mb_convert_encoding($data, 'UTF-8', $detectedEncoding);
            }
         */
    }

}
