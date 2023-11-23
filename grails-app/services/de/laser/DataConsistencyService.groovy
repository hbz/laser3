package de.laser

import de.laser.storage.BeanStore
import de.laser.utils.CodeUtils
import de.laser.utils.DateUtils
import de.laser.utils.SwissKnife
import grails.gorm.transactions.Transactional
import org.grails.core.io.support.GrailsFactoriesLoader
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.grails.validation.discovery.ConstrainedDiscovery

import java.text.SimpleDateFormat

/**
 * This service ensures data consistency
 */
@Transactional
class DataConsistencyService {

    DeletionService deletionService
    ApplicationTagLib g = BeanStore.getApplicationTagLib()

    /**
     * Checks the duplicates of organisation, package and platform names
     * @return a map containing for each of the object types the counts per name
     */
    Map<String, Object> checkDuplicates() {
        Map result = [
                Org: [:],
                Package: [:],
                Platform: [:]
        ]

        // Orgs

        result.Org.name = Org.executeQuery(
                'SELECT org.name as name, count(org.name) as cnt FROM Org org GROUP BY org.name ORDER BY org.name'
        ).findAll{ it -> it[1] > 1}

        result.Org.shortcode = Org.executeQuery(
                'SELECT org.shortcode as shortcode, count(org.shortcode) as cnt FROM Org org GROUP BY org.shortcode ORDER BY org.shortcode'
        ).findAll{ it -> it[1] > 1}

        result.Org.sortname = Org.executeQuery(
                'SELECT org.sortname as sortname, count(org.sortname) as cnt FROM Org org GROUP BY org.sortname ORDER BY org.sortname'
        ).findAll{ it -> it[1] > 1}

        // Packages

        result.Package.name = Package.executeQuery(
                'SELECT pkg.name as name, count(pkg.name) as cnt FROM Package pkg GROUP BY pkg.name ORDER BY pkg.name'
        ).findAll{ it -> it[1] > 1}

        result.Package.sortName = Package.executeQuery(
                'SELECT pkg.sortname as sortname, count(pkg.sortname) as cnt FROM Package pkg GROUP BY pkg.sortname ORDER BY pkg.sortname'
        ).findAll{ it -> it[1] > 1}

        // Platforms

        result.Platform.name = Platform.executeQuery(
                'SELECT pf.name as name, count(pf.name) as cnt FROM Platform pf GROUP BY pf.name ORDER BY pf.name'
        ).findAll{ it -> it[1] > 1}

        result.Platform.normname = Platform.executeQuery(
                'SELECT pf.normname as normname, count(pf.normname) as cnt FROM Platform pf GROUP BY pf.normname ORDER BY pf.normname'
        ).findAll{ it -> it[1] > 1}

        result
    }

    /**
     * Checks per AJAX whether an identical object exists to the given property:value
     * @param key1 the object type
     * @param key2 the property key
     * @param value the property value
     * @return a list of potential object candidates
     */
    List<Object> ajaxQuery(String key1, String key2, String value) {

        List<Object> result = []
        SimpleDateFormat sdfA = DateUtils.getLocalizedSDF_noTime()
        SimpleDateFormat sdfB = DateUtils.getLocalizedSDF_noZ()

        if (key1 == 'Org') {
            result = Org.findAllWhere( "${key2}": value ).collect{ it ->
                Map<String, Object> dryRunInfo = deletionService.deleteOrganisation(it, null, deletionService.DRY_RUN)

                [
                    id: it.id,
                    name: it.name,
                    class: it.class.simpleName,
                    link: g.createLink(controller:'organisation', action:'show', id: it.id),
                    created: sdfA.format( it.dateCreated ),
                    updated: sdfB.format( it.lastUpdated ),
                    deletable: dryRunInfo.deletable,
                    mergeable: dryRunInfo.mergeable
                ]
            }
        }
        if (key1 == 'Package') {
            result = Package.findAllWhere( "${key2}": value ).collect{ it -> [
                    id: it.id,
                    name: it.name,
                    link: g.createLink(controller:'package', action:'show', id: it.id),
                    created: sdfA.format( it.dateCreated ),
                    updated: sdfB.format( it.lastUpdated )
                ]
            }
        }
        if (key1 == 'Platform') {
            result = Platform.findAllWhere( "${key2}": value ).collect{ it -> [
                    id: it.id,
                    name: it.name,
                    link: g.createLink(controller:'platform', action:'show', id: it.id),
                    created: sdfA.format( it.dateCreated ),
                    updated: sdfB.format( it.lastUpdated )
                ]
            }
        }

        result
    }

    @Deprecated
    def checkBooleanFields() {

        List<String> candidates = []
        List<String> statements = []

        CodeUtils.getAllDomainClasses().each { cls ->

            PersistentEntity pe = CodeUtils.getPersistentEntity( cls.name )
            Collection bools    = pe ? pe.persistentProperties.findAll {it.type in [boolean, java.lang.Boolean]} : []
            Map constraints     = GrailsFactoriesLoader.loadFactory(ConstrainedDiscovery.class).findConstrainedProperties(pe)

            if (! bools.isEmpty()) {
                Map<String, Boolean> props = [:]

                bools.each { it ->
                    props.putAt( it.name, constraints[ it.name ].isNullable() )
                }
                props.each{ k,v ->
                    if (v.equals(true)) {
                        candidates.add( "${cls.simpleName}.${k} -> ${v}" )

                        String tableName = SwissKnife.toSnakeCase(cls.simpleName)
                        String columnName = SwissKnife.toSnakeCase(k)
                        String sql = "update ${tableName} set ${columnName} = false where ${columnName} is null;"

                        statements.add( sql )
                    }
                }
            }
        }

        log.debug "___ found candidates: "
        candidates.each { log.debug it }
        log.debug "___ generated pseudo statements: "
        statements.each { log.debug it }
    }
}
