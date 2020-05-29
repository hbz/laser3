package de.laser

import grails.transaction.Transactional

@Transactional
class NormalizeService {

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
}
