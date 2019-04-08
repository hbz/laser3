package de.laser

import com.k_int.kbplus.RefdataCategory
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.SystemAdmin
import de.laser.helper.RefdataAnnotation

class RefdataService {

    def grailsApplication
    def genericOIDService

    Map<String, Object> getUsageDetails() {
        def detailsMap = [:]
        def usedRdvList = []

        def allDcs = [:]

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
            allDcs << ["${dc.clazz.simpleName}" : dcFields.sort()]
        }

        // inspect classes and fields

        allDcs.each { dcName, dcFields ->
            def dfMap = [:]

            dcFields.each { df ->
                def rdvs = SystemAdmin.executeQuery("SELECT DISTINCT ${df.name} FROM ${dcName}")

                dfMap << ["${df.name}": rdvs.collect { it -> "${it.id}:${it.value}" }.sort()]

                // ids of used refdata values
                rdvs.each { it ->
                    usedRdvList << it.id
                }
            }

            if (! dfMap.isEmpty()) {
                detailsMap << ["${dcName}": dfMap]
            }
        }

        [usedRdvList.unique().sort(), detailsMap.sort()]
    }

    int replaceRefdataValues(RefdataValue rdvFrom, RefdataValue rdvTo) {

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

    Map<String, Object> integrityCheck() {
        Map checkResult = [:]

        grailsApplication.getArtefacts("Domain").toList().each { dc ->
            def cls = dc.clazz

            // find all rdv_fk from superclasses
            while (cls != Object.class) {
                List tmp = []
                tmp.addAll( cls.getDeclaredFields()
                        .findAll { it -> !it.synthetic }
                        .findAll { it -> it.type.name == 'com.k_int.kbplus.RefdataValue' }
                        .collect { it ->

                            RefdataAnnotation anno = it.getAnnotation(RefdataAnnotation)
                            if (anno && ! [RefdataAnnotation.GENERIC, RefdataAnnotation.UNKOWN].contains(anno.cat()) ) {
                                List fieldCats = RefdataValue.executeQuery(
                                        "SELECT DISTINCT dummy.${it.name}, rdc FROM ${cls.simpleName} dummy JOIN dummy.${it.name}.owner rdc",
                                )
                                Map fieldCheck = [:]

                                fieldCats.each { it2 ->
                                    if (it2[1].id == RefdataCategory.findByDesc(anno.cat())?.id) {
                                        fieldCheck << ["${it2[0]}": true]
                                    }
                                    else {
                                        fieldCheck << ["${it2[0]}": it2[1]]
                                    }
                                }
                                return [field: it.name, cat: anno.cat(), rdc: RefdataCategory.findByDesc(anno.cat()), check: fieldCheck]
                            }
                            else {
                                return [field: it.name, cat: anno?.cat(), rdc: null, check: [:]]
                            }
                        }
                )

                if (tmp) {
                    checkResult << ["${cls.simpleName}": tmp]
                }
                cls = cls.getSuperclass()
            }
        }

        log.debug(checkResult)

        checkResult.sort()
    }

}

