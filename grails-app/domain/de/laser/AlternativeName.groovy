package de.laser

import de.laser.interfaces.CalculatedLastUpdated
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class AlternativeName implements CalculatedLastUpdated, Comparable {

    def cascadingUpdateService

    Long id
    Long version
    String name
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static Log static_logger = LogFactory.getLog(AlternativeName)

    static belongsTo = [
        tipp: TitleInstancePackagePlatform,
        pkg: Package,
        platform: Platform,
        org: Org
    ]

    static constraints = {
        tipp (nullable: true)
        pkg  (nullable: true)
        platform (nullable: true)
        org (nullable: true)
        dateCreated (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static mapping = {
        id                    column: 'altname_id'
        version               column: 'altname_version'
        name                  column: 'altname_name', type: "text"
        tipp                  column: 'altname_tipp_fk'
        pkg                   column: 'altname_pkg_fk'
        platform              column: 'altname_plat_fk'
        org                   column: 'altname_org_fk'
        dateCreated           column: 'altname_date_created'
        lastUpdated           column: 'altname_last_updated'
        lastUpdatedCascading  column: 'altname_last_updated_cascading'
    }

    @Override
    int compareTo(Object o) {
        AlternativeName altName2 = (AlternativeName) o
        name <=> altName2.name
    }

    @Override
    def afterInsert() {
        static_logger.debug("afterInsert")
        cascadingUpdateService.update(this, dateCreated)
    }

    @Override
    def afterUpdate() {
        static_logger.debug("afterUpdate")
        cascadingUpdateService.update(this, lastUpdated)
    }

    @Override
    def afterDelete() {
        static_logger.debug("afterDelete")
        cascadingUpdateService.update(this, new Date())
    }

    @Override
    Date _getCalculatedLastUpdated() {
        (lastUpdatedCascading > lastUpdated) ? lastUpdatedCascading : lastUpdated
    }

    static AlternativeName construct(Map<String, Object> configMap) {
        if(configMap.tipp || configMap.pkg || configMap.platform || configMap.org) {
            AlternativeName altName = new AlternativeName(name: configMap.name)
            if(configMap.tipp)
                altName.tipp = configMap.tipp
            else if(configMap.pkg)
                altName.pkg = configMap.pkg
            else if(configMap.platform)
                altName.platform = configMap.platform
            else if(configMap.org)
                altName.org = configMap.org
            if(!altName.save()) {
                static_logger.error("error on creating alternative name: ${altName.getErrors().getAllErrors().toListString()}")
            }
            altName
        }
        else {
            static_logger.error("No reference object specified for AlternativeName!")
            null
        }
    }
}
