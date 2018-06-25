package de.laser

import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SystemAdmin
import com.k_int.kbplus.abstract_domain.AbstractProperty

class RefdataService {

    def grailsApplication
    def genericOIDService

    def getUsageDetails() {
        def usedRdvList = []
        def detailsMap = [:]

        grailsApplication.getArtefacts("Domain").toList().each { dc ->
            def dcMap = [:]

            dc.clazz.declaredFields
                    .findAll { it -> !it.synthetic }
                    .findAll { it -> it.type.name == 'com.k_int.kbplus.RefdataValue' }
                    .sort()
                    .each { df ->
                def query = "SELECT DISTINCT ${df.name} FROM ${dc.name}"

                def rdvs = SystemAdmin.executeQuery(query)

                dcMap << ["${df.name}": rdvs.collect { it -> "${it.id}:${it.value}" }.sort()]

                // ids of used refdata values
                rdvs.each { it ->
                    usedRdvList << it.id
                }
            }
            if (!dcMap.isEmpty()) {
                detailsMap << ["${dc.clazz.name}": dcMap]
            }
        }

        [usedRdvList.unique().sort(), detailsMap.sort()]
    }
}

