package de.laser.api.v0.catalogue

import de.laser.api.v0.ApiReader
import groovy.util.logging.Log4j

@Log4j
class ApiCatalogue {

    /**
     * @return []
     */
    static getAllRefdatas() {
        def result = ApiReader.exportRefdatas()
        result
    }

}
