package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.interfaces.CalculatedLastUpdated
import de.laser.storage.BeanStore
import de.laser.storage.RDConstants
import groovy.util.logging.Slf4j

/**
 * A container class to retain language classifications of titles and packages.
 * The structure is the same as in {@link DeweyDecimalClassification} just as the purpose is
 * @see TitleInstancePackagePlatform
 * @see Package
 * @see DeweyDecimalClassification
 */
@Slf4j
class Language implements CalculatedLastUpdated, Comparable {

    Long id
    Long version
    @RefdataInfo(cat = RDConstants.LANGUAGE_ISO)
    RefdataValue language
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static belongsTo = [
        tipp: TitleInstancePackagePlatform,
        pkg: Package
    ]

    static constraints = {
        tipp (nullable: true)
        pkg  (nullable:true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static mapping = {
        id                    column: 'lang_id'
        version               column: 'lang_version'
        language              column: 'lang_rv_fk', index: 'lang_rv_idx, lang_tipp_rv_idx'
        tipp                  column: 'lang_tipp_fk', index: 'lang_tipp_idx, lang_tipp_rv_idx'
        pkg                   column: 'lang_pkg_fk', index: 'lang_pkg_idx'
        dateCreated           column: 'lang_date_created'
        lastUpdated           column: 'lang_last_updated'
        lastUpdatedCascading  column: 'lang_last_updated_cascading'
    }

    /**
     * Compares two entries against their underlying reference value
     * @param o the other entry to compare with
     * @return the comparison result of the {@link RefdataValue}s
     */
    @Override
    int compareTo(Object o) {
        Language lang2 = (Language) o
        language <=> lang2.language
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
     * Constructor to set up a new language entry with the given config parameters
     * @param configMap the {@link Map} containing the configuration parameters
     * @return the new language instance or null on failure
     */
    static Language construct(Map<String, Object> configMap) {
        if(configMap.tipp || configMap.pkg) {
            Language lang = new Language(language: configMap.language)
            if(configMap.tipp)
                lang.tipp = configMap.tipp
            else if(configMap.pkg)
                lang.pkg = configMap.pkg
            if(!lang.save()) {
                log.error("error on creating lang: ${lang.getErrors().getAllErrors().toListString()}")
                null
            }
            else lang
        }
        else {
            log.error("No reference object specified for Language!")
            null
        }
    }
}
