package de.laser.api.v0.catalogue

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.Identifier
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.Package
import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import de.laser.api.v0.ApiReader
import de.laser.helper.Constants
import groovy.util.logging.Log4j

@Log4j
class ApiRefdatas {

    /**
     * @return []
     */
    static getAllRefdatas() {
        def result = ApiReader.exportRefdatas()
        result
    }

}
