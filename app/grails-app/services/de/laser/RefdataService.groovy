package de.laser

import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SystemAdmin
import com.k_int.kbplus.abstract_domain.AbstractProperty
import org.codehaus.groovy.grails.web.util.WebUtils

class RefdataService {

    def grailsApplication
    def genericOIDService

    def getUsageDetails() {
        def usedRdvList = []
        def detailsMap = [:]

        def fortytwo = [:]

        grailsApplication.getArtefacts("Domain").toList().each { dc ->
            def dcFields = []
            def cls = dc.clazz

            // find all rdv_fk from superclasses
            while (cls != Object.class) {
                dcFields.addAll( cls.getDeclaredFields()
                        .findAll { it -> !it.synthetic }
                        .findAll { it -> it.type.name == 'com.k_int.kbplus.RefdataValue' }
                )
                cls = cls.getSuperclass()
            }
            fortytwo << ["${dc.clazz.simpleName}" : dcFields.sort()]
        }

        fortytwo.each { dcName, dcFields ->
            def dcMap = [:]
            //log.debug("${dcName} : ${dcFields}")

            dcFields.each { df ->

                def query = "SELECT DISTINCT ${df.name} FROM ${dcName}"
                def rdvs = SystemAdmin.executeQuery(query)

                dcMap << ["${df.name}": rdvs.collect { it -> "${it.id}:${it.value}" }.sort()]

                // ids of used refdata values
                rdvs.each { it ->
                    usedRdvList << it.id
                }
            }

            if (! dcMap.isEmpty()) {
                detailsMap << ["${dcName}": dcMap]
            }
        }

        [usedRdvList.unique().sort(), detailsMap.sort()]
    }

    def replaceRefdataValues(RefdataValue rdvFrom, RefdataValue rdvTo) {

        log.debug("replacing: ${rdvFrom} with: ${rdvTo}")
        def count = 0
        def fortytwo = [:]

        grailsApplication.getArtefacts("Domain").toList().each { dc ->
            def dcFields = []
            def cls = dc.clazz

            // find all rdv_fk from superclasses
            while (cls != Object.class) {
                dcFields.addAll( cls.getDeclaredFields()
                        .findAll { it -> !it.synthetic }
                        .findAll { it -> it.type.name == 'com.k_int.kbplus.RefdataValue' }
                )
                cls = cls.getSuperclass()
            }
            fortytwo << ["${dc.clazz.simpleName}" : dcFields.sort()]
        }

        fortytwo.each { dcName, dcFields ->

            dcFields.each { df ->
                def hql = "select dummy from ${dcName} as dummy where dummy.${df.name} = :xfrom"
                def result = SystemAdmin.executeQuery(hql, [xfrom: rdvFrom])

                //log.debug(hql + " @ " + rdvFrom.id + " -> " + result)
                result.each { it ->
                    if (it."${df.name}" == rdvFrom) {
                        log.debug("exchange refdata value at: ${dcName}(${it.id}) from: ${rdvFrom}(${rdvFrom.id}) to: ${rdvTo}(${rdvTo.id})")
                        it."${df.name}" = rdvTo
                        it.save(flush: true)
                        count++
                    }
                }
            }
        }
        count
    }
}

