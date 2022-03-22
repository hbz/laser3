package de.laser

import de.laser.annotations.RefdataAnnotation
import de.laser.helper.AppUtils
import grails.gorm.transactions.Transactional

/**
 * This service delivers generic reference data related information
 */
@Transactional
class RefdataService {

    /**
     * Retrieves the usage data for each reference data category and the values belonging to them
     * @return a list of reference value usages and where they are used
     */
    List getUsageDetails() {
        def detailsMap      = [:]
        def usedRdvList     = []
        def allDcs          = [:]

        List classes = AppUtils.getAllDomainClasses().findAll {
            ! it.clazz.toString().endsWith('CustomProperty') && ! it.clazz.toString().endsWith('PrivateProperty') // tmp
        }

        classes.each { dc ->
            def dcFields = []
            def cls = dc.clazz

            // find all rdv_fk from superclasses
            while (cls != Object.class) {
                dcFields.addAll( cls.getDeclaredFields()
                        .findAll { it -> !it.synthetic }
                        .findAll { it -> it.type.name == RefdataValue.class.name }
                )
                cls = cls.getSuperclass()
            }
            allDcs << ["${dc.clazz.simpleName}" : dcFields.sort()]
        }

        // inspect classes and fields

        allDcs.each { dcName, dcFields ->
            def dfMap = [:]

            dcFields.each { df ->
                Set<RefdataValue> rdvs = RefdataValue.executeQuery( "SELECT DISTINCT " + df.name + " FROM " + dcName )

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

    /**
     * Replaces a reference value by another
     * @param rdvFrom the reference value to be replaced
     * @param rdvTo the substitution value
     * @return the count of replaces performed
     */
    int replaceRefdataValues(RefdataValue rdvFrom, RefdataValue rdvTo) {

        log.debug("replacing: ${rdvFrom} with: ${rdvTo}")
        def count = 0
        def fortytwo = [:]

        AppUtils.getAllDomainClasses().each { dc ->
            def dcFields = []
            def cls = dc.clazz

            // find all rdv_fk from superclasses
            while (cls != Object.class) {
                dcFields.addAll( cls.getDeclaredFields()
                        .findAll { it -> !it.synthetic }
                        .findAll { it -> it.type.name == RefdataValue.class.name }
                )
                cls = cls.getSuperclass()
            }
            fortytwo << ["${dc.clazz.simpleName}" : dcFields.sort()]
        }

        fortytwo.each { dcName, dcFields ->

            dcFields.each { df ->
                String hql = "select dummy from ${dcName} as dummy where dummy.${df.name} = :xfrom"
                def result = RefdataValue.executeQuery(hql, [xfrom: rdvFrom])

                //log.debug(hql + " @ " + rdvFrom.id + " -> " + result)
                result.each { it ->
                    if (it."${df.name}" == rdvFrom) {
                        log.debug("exchange refdata value at: ${dcName}(${it.id}) from: ${rdvFrom}(${rdvFrom.id}) to: ${rdvTo}(${rdvTo.id})")
                        it."${df.name}" = rdvTo
                        it.save()
                        count++
                    }
                }
            }
        }
        count
    }

    /**
     * Checks the correctness of reference value definitions
     * @return a map containing for each reference data field defined whether it really belongs to the annotated reference data category
     */
    Map<String, Object> integrityCheck() {
        Map checkResult = [:]

        AppUtils.getAllDomainClasses().each { dc ->
            def cls = dc.clazz
            String dcClassName = cls.simpleName

            // find all rdv_fk from superclasses
            while (cls != Object.class) {
                List tmp = []
                tmp.addAll( cls.getDeclaredFields()
                        .findAll { it -> !it.synthetic }
                        .findAll { it -> it.type.name == RefdataValue.class.name }
                        .collect { it ->

                            RefdataAnnotation anno = it.getAnnotation(RefdataAnnotation)
                            if (anno && ! [RefdataAnnotation.GENERIC, RefdataAnnotation.UNKOWN].contains(anno.cat()) ) {
                                String query = "SELECT DISTINCT dummy.${it.name}.id, rdc.id FROM ${dcClassName} dummy JOIN dummy.${it.name}.owner rdc"
                                List fieldCats = RefdataValue.executeQuery( query )
                                Map fieldCheck = [:]

                                fieldCats.each { it2 ->
                                    RefdataValue rdv = RefdataValue.get(it2[0])
                                    if (it2[1] == RefdataCategory.getByDesc(anno.cat())?.id) {
                                        fieldCheck << ["${rdv}": true]
                                    }
                                    else {
                                        fieldCheck << ["${rdv}": RefdataCategory.get(it2[1])]
                                    }
                                }
                                return [field: it.name, cat: anno.cat(), rdc: RefdataCategory.getByDesc(anno.cat()), check: fieldCheck]
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

        log.debug( 'integrityCheck: ' + checkResult.size())

        checkResult.sort()
    }

    /**
     * Retrieves for a given reference data value key and category description the OID representation of the RefdataValue.
     * If not found, this method returns null
     * @param key the refdata value. May be an English or German string key
     * @param categoryDescription the {@link RefdataCategory}
     * @return the OID of the result {@link RefdataValue} or null if no result is found
     */
    String retrieveRefdataValueOID(String key,String categoryDescription) {
        RefdataValue result = RefdataValue.getByValueAndCategory(key,categoryDescription)
        if(!result) {
            result = RefdataValue.getByCategoryDescAndI10nValueDe(categoryDescription,key)
        }
        if(result)
            "${result.class.name}:${result.id}"
        else
            null
    }

}

