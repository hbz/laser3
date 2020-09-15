package de.laser

import com.k_int.kbplus.*
import de.laser.helper.AppUtils
import de.laser.helper.DateUtil
import de.laser.helper.SwissKnife
import grails.transaction.Transactional
import grails.util.Holders
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

import java.text.SimpleDateFormat

@Transactional
class DataConsistencyService {

    def springSecurityService
    def deletionService
    def g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)

    Map<String, Object> checkTitles() {
        Map result = [
                Org: [:],
                Package: [:],
                Platform: [:]
        ]

        // Orgs

        result.Org.name = Org.executeQuery(
                'SELECT org.name as name, count(org.name) as cnt FROM Org org GROUP BY org.name ORDER BY org.name'
        ).findAll{ it -> it[1] > 1}

        result.Org.shortname = Org.executeQuery(
                'SELECT org.shortname as shortname, count(org.shortname) as cnt FROM Org org GROUP BY org.shortname ORDER BY org.shortname'
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
                'SELECT pkg.sortName as sortName, count(pkg.sortName) as cnt FROM Package pkg GROUP BY pkg.sortName ORDER BY pkg.sortName'
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

    List<Object> ajaxQuery(String key1, String key2, String value) {

        List<Object> result = []
        SimpleDateFormat sdfA = DateUtil.getSDF_NoTime()
        SimpleDateFormat sdfB = DateUtil.getSDF_NoZ()

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
        if (key1 == 'TitleInstance') {
            result = TitleInstance.findAllWhere( "${key2}": value ).collect{ it -> [
                    id: it.id,
                    name: it.title,
                    link: g.createLink(controller:'title', action:'show', id: it.id),
                    created: sdfA.format( it.dateCreated ),
                    updated: sdfB.format( it.lastUpdated )
                ]
            }
        }
        if (key1 == 'TitleInstancePackagePlatform') {
            result = TitleInstancePackagePlatform.findAllWhere( "${key2}": value ).collect{ it -> [
                    id: it.id,
                    name: 'TitleInstancePackagePlatform',
                    link: g.createLink(controller:'tipp', action:'show', id: it.id),
                    created: '',
                    updated: ''
                ]
            }
        }

        result
    }

    def checkBooleanFields() {

        List<String> candidates = []
        List<String> statements = []

        AppUtils.getAllDomainClasses().sort{ it.clazz.simpleName }.each { dc ->

            Collection bools = dc.persistentProperties.findAll {it.type in [boolean, java.lang.Boolean]}

            if (! bools.isEmpty()) {
                Map<String, Boolean> props = [:]

                bools.each { it ->
                    props.put( "${it.name}", dc.constraints[ it.name ].isNullable() )
                }

                // println " " + dc.clazz.simpleName
                props.each{ k,v ->
                    // String ctrl = "select count(o) from ${dc.clazz.simpleName} o where o.${k} is null"
                    // println "   nullable ? ${k} : ${v}, DB contains null values : " + Org.executeQuery(ctrl)

                    if (v.equals(true)) {
                        candidates.add( "${dc.clazz.simpleName}.${k} -> ${v}" )

                        String tableName = SwissKnife.toSnakeCase(dc.clazz.simpleName)
                        String columnName = SwissKnife.toSnakeCase(k)
                        String sql = "update ${tableName} set ${columnName} = false where ${columnName} is null;"

                        statements.add( sql )
                    }
                }
            }
        }

        println "___ found candidates: "
        candidates.each { println it }
        println "___ generated pseudo statements: "
        statements.each { println it }
    }
}
