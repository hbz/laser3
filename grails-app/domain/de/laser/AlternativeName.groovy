package de.laser

import de.laser.interfaces.CalculatedLastUpdated
import de.laser.storage.BeanStore
import groovy.util.logging.Slf4j

/**
 * A container class to retain alternative names of an entity; a such entity may be
 * <ul>
 *     <li>{@link TitleInstancePackagePlatform}</li>
 *     <li>{@link Package}</li>
 *     <li>{@link Platform}</li>
 *     <li>{@link Org}</li>
 * </ul>
 */
@Slf4j
class AlternativeName implements CalculatedLastUpdated, Comparable<AlternativeName> {

    Long id
    Long version
    String name
    Date dateCreated
    Date lastUpdated
    Date lastUpdatedCascading

    static belongsTo = [
            license: License,
            org: Org,
            pkg: Package,
            platform: Platform,
            provider: Provider,
            subscription: Subscription,
            tipp: TitleInstancePackagePlatform,
            vendor: Vendor
    ]

    static constraints = {
        license (nullable: true)
        org (nullable: true)
        pkg  (nullable: true)
        platform (nullable: true)
        provider (nullable: true)
        subscription (nullable: true)
        tipp (nullable: true)
        vendor (nullable: true)
        lastUpdated (nullable: true)
        lastUpdatedCascading (nullable: true)
    }

    static mapping = {
        id                    column: 'altname_id'
        version               column: 'altname_version'
        name                  column: 'altname_name', type: "text"
        license               column: 'altname_lic_fk'
        org                   column: 'altname_org_fk'
        pkg                   column: 'altname_pkg_fk'
        platform              column: 'altname_plat_fk'
        provider              column: 'altname_prov_fk'
        subscription          column: 'altname_sub_fk'
        tipp                  column: 'altname_tipp_fk'
        vendor                column: 'altname_vendor_fk'
        dateCreated           column: 'altname_date_created'
        lastUpdated           column: 'altname_last_updated'
        lastUpdatedCascading  column: 'altname_last_updated_cascading'
    }

    /**
     * Compares this name to a given alternative name
     * @param o the alternative name to compare against
     * @return the name comparison result (-1, 0, 1)
     */
    @Override
    int compareTo(AlternativeName altName2) {
        int result = name <=> altName2.name
        if(!result)
            result = id <=> altName2.id
        result
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
     * Factory constructor method to set up an alternative name for an entity
     * @param configMap the map containing the new entry's parameters
     * @return the new alternative name, null if no reference object has been specified
     */
    static AlternativeName construct(Map<String, Object> configMap) {
        if(configMap.license || configMap.org || configMap.pkg || configMap.platform || configMap.provider || configMap.subscription || configMap.tipp || configMap.vendor) {
            AlternativeName altName = new AlternativeName(name: configMap.name)
            if(configMap.license)
                altName.license = configMap.license
            else if(configMap.org)
                altName.org = configMap.org
            else if(configMap.pkg)
                altName.pkg = configMap.pkg
            else if(configMap.platform)
                altName.platform = configMap.platform
            else if(configMap.provider)
                altName.provider = configMap.provider
            else if(configMap.subscription)
                altName.subscription = configMap.subscription
            else if(configMap.tipp)
                altName.tipp = configMap.tipp
            else if(configMap.vendor)
                altName.vendor = configMap.vendor
            if(!altName.save()) {
                log.error("error on creating alternative name: ${altName.getErrors().getAllErrors().toListString()}")
            }
            altName
        }
        else {
            log.error("No reference object specified for AlternativeName!")
            null
        }
    }
}
