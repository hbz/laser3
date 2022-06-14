package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import groovy.util.logging.Slf4j

/**
 * A container class to retain Dewey decimal classifications of titles and packages.
 * The structure is the same as in {@link Language} just as the purpose is
 * @see TitleInstancePackagePlatform
 * @see Package
 * @see Language
 */
@Slf4j
class DeweyDecimalClassification implements CalculatedLastUpdated, Comparable{

    Long id
    Long version
    @RefdataInfo(cat = RDConstants.DDC)
    RefdataValue ddc
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static belongsTo = [
        tipp: TitleInstancePackagePlatform,
        pkg: Package
    ]

    static constraints = {
        tipp (nullable: true)
        pkg  (nullable: true)
        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static mapping = {
        id                    column: 'ddc_id'
        version               column: 'ddc_version'
        ddc                   column: 'ddc_rv_fk'
        tipp                  column: 'ddc_tipp_fk'
        pkg                   column: 'ddc_pkg_fk'
        dateCreated           column: 'ddc_date_created'
        lastUpdated           column: 'ddc_last_updated'
        lastUpdatedCascading  column: 'ddc_last_updated_cascading'
    }

    /**
     * Compares two entries against their underlying reference value
     * @param o the other entry to compare with
     * @return the comparison result of the {@link RefdataValue}s
     */
    @Override
    int compareTo(Object o) {
        DeweyDecimalClassification ddc2 = (DeweyDecimalClassification) o
        ddc <=> ddc2.ddc
    }

    @Override
    def afterInsert() {
        log.debug("afterInsert")
        BeanStore.getCascadingUpdateService().update(this, dateCreated)
    }

    @Override
    def afterUpdate() {
        log.debug("afterUpdate")
        BeanStore.getCascadingUpdateService().update(this, lastUpdated)
    }

    @Override
    def afterDelete() {
        log.debug("afterDelete")
        BeanStore.getCascadingUpdateService().update(this, new Date())
    }

    @Override
    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    /**
     * Constructor to set up a new DDC entry with the given config parameters
     * @param configMap the {@link Map} containing the configuration parameters
     * @return the new DDC instance or null on failure
     */
    static DeweyDecimalClassification construct(Map<String, Object> configMap) {
        if(configMap.tipp || configMap.pkg) {
            DeweyDecimalClassification ddc = new DeweyDecimalClassification(ddc: configMap.ddc)
            if(configMap.tipp)
                ddc.tipp = configMap.tipp
            else if(configMap.pkg)
                ddc.pkg = configMap.pkg
            if(!ddc.save()) {
                log.error("error on creating ddc: ${ddc.getErrors().getAllErrors().toListString()}")
            }
            ddc
        }
        else {
            log.error("No reference object specified for DDC!")
            null
        }
    }
}
