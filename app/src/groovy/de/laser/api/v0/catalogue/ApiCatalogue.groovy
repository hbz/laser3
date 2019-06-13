package de.laser.api.v0.catalogue

import de.laser.api.v0.ApiReader
import grails.converters.JSON
import groovy.util.logging.Log4j

@Log4j
class ApiCatalogue {

    /**
     * @return JSON
     */
    static JSON getAllRefdatas() {
        Collection<Object> result = ApiReader.retrieveRefdataCollection()

        return (result ? new JSON(result) : null)
    }

    /**
     * @return []
     */
    static getDummy() {
        def result = ['dummy']
        result
    }

}
