package de.laser

import de.laser.annotations.RefdataAnnotation
import de.laser.storage.BeanStorage
import de.laser.storage.RDConstants
import de.laser.interfaces.CalculatedLastUpdated
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

/**
 * A container class to retain language classifications of titles and packages.
 * The structure is the same as in {@link DeweyDecimalClassification} just as the purpose is
 * @see TitleInstancePackagePlatform
 * @see Package
 * @see DeweyDecimalClassification
 */
class Language implements CalculatedLastUpdated, Comparable {

    Long id
    Long version
    @RefdataAnnotation(cat = RDConstants.LANGUAGE_ISO)
    RefdataValue language
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static Log static_logger = LogFactory.getLog(Language)

    static belongsTo = [
        tipp: TitleInstancePackagePlatform,
        pkg: Package
    ]

    static constraints = {
        tipp (nullable: true)
        pkg  (nullable:true)
        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static mapping = {
        id                    column: 'lang_id'
        version               column: 'lang_version'
        language              column: 'lang_rv_fk'
        tipp                  column: 'lang_tipp_fk'
        pkg                   column: 'lang_pkg_fk'
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
        static_logger.debug("afterInsert")
        BeanStorage.getCascadingUpdateService().update(this, dateCreated)
    }

    @Override
    def afterUpdate() {
        static_logger.debug("afterUpdate")
        BeanStorage.getCascadingUpdateService().update(this, lastUpdated)
    }

    @Override
    def afterDelete() {
        static_logger.debug("afterDelete")
        BeanStorage.getCascadingUpdateService().update(this, new Date())
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
                static_logger.error("error on creating lang: ${lang.getErrors().getAllErrors().toListString()}")
                null
            }
            else lang
        }
        else {
            static_logger.error("No reference object specified for Language!")
            null
        }
    }
}
