package de.laser

import com.k_int.kbplus.*
import com.k_int.kbplus.auth.User
import grails.util.Holders
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

class DataConsistencyService {

    def springSecurityService
    def g = Holders.grailsApplication.mainContext.getBean(ApplicationTagLib)

    def checkImportIds() {
        Map result = [
                Org: [],
                Package: [],
                Platform: [],
                TitleInstance: [],
                TitleInstancePackagePlatform: []
        ]

        result.Org = Org.executeQuery(
                'SELECT org.impId as impId, count(org.impId) as cnt FROM Org org GROUP BY org.impId ORDER BY org.impId'
        ).findAll{ it -> it[1] > 1}

        result.Package = Package.executeQuery(
                'SELECT pkg.impId as impId, count(pkg.impId) as cnt FROM Package pkg GROUP BY pkg.impId ORDER BY pkg.impId'
        ).findAll{ it -> it[1] > 1}

        result.Platform = Platform.executeQuery(
                'SELECT pf.impId as impId, count(pf.impId) as cnt FROM Platform pf GROUP BY pf.impId ORDER BY pf.impId'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstance = TitleInstance.executeQuery(
                'SELECT ti.impId as impId, count(ti.impId) as cnt FROM TitleInstance ti GROUP BY ti.impId ORDER BY ti.impId'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstancePackagePlatform = TitleInstancePackagePlatform.executeQuery(
                'SELECT tipp.impId as impId, count(tipp.impId) as cnt FROM TitleInstancePackagePlatform tipp GROUP BY tipp.impId ORDER BY tipp.impId'
        ).findAll{ it -> it[1] > 1}

        result
    }

    def checkTitles() {
        Map result = [
                Org: [:],
                Package: [:],
                Platform: [:]
                // TitleInstances: [:],
                // Tipps: [:]
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
        /*
        result.Platform.primaryUrl = Platform.executeQuery(
                'SELECT pf.primaryUrl as primaryUrl, count(pf.primaryUrl) as cnt FROM Platform pf GROUP BY pf.primaryUrl ORDER BY pf.primaryUrl'
        ).findAll{ it -> it[1] > 1}
        */

        // TitleInstance

        /*
        result.TitleInstance.title = TitleInstance.executeQuery(
                'SELECT ti.title as title, count(ti.title) as cnt FROM TitleInstance ti GROUP BY ti.title ORDER By ti.title'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstance.normTitle = TitleInstance.executeQuery(
                'SELECT ti.normTitle as normTitle, count(ti.normTitle) as cnt FROM TitleInstance ti GROUP BY ti.normTitle ORDER BY ti.normTitle'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstance.keyTitle = TitleInstance.executeQuery(
                'SELECT ti.keyTitle as keyTitle, count(ti.keyTitle) as cnt FROM TitleInstance ti GROUP BY ti.keyTitle ORDER BY ti.keyTitle'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstance.sortTitle = TitleInstance.executeQuery(
                'SELECT ti.sortTitle as sortTitle, count(ti.sortTitle) as cnt FROM TitleInstance ti GROUP BY ti.sortTitle ORDER BY ti.sortTitle'
        ).findAll{ it -> it[1] > 1}
        */

        // TitleInstancePackagePlatform
        /*
        result.TitleInstancePackagePlatform.hostPlatformURL = TitleInstancePackagePlatform.executeQuery(
                'SELECT tipp.hostPlatformURL as hostPlatformURL, count(tipp.hostPlatformURL) as cnt FROM TitleInstancePackagePlatform tipp GROUP BY tipp.hostPlatformURL ORDER BY tipp.hostPlatformURL'
        ).findAll{ it -> it[1] > 1}
        */
        
        result
    }

    def ajaxQuery(String key1, String key2, String value) {

        def result = []

        if (key1 == 'Org') {
            result = Org.findAllWhere( "${key2}": value ).collect{ it ->
                [id: it.id, name: it.name, link: g.createLink(controller:'organisations', action:'show', id: it.id)]
            }
        }
        if (key1 == 'Package') {
            result = Package.findAllWhere( "${key2}": value ).collect{ it ->
                [id: it.id, name: it.name, link: g.createLink(controller:'packageDetails', action:'show', id: it.id)]
            }
        }
        if (key1 == 'Platform') {
            result = Platform.findAllWhere( "${key2}": value ).collect{ it ->
                [id: it.id, name: it.name, link: g.createLink(controller:'platform', action:'show', id: it.id)]
            }
        }
        if (key1 == 'TitleInstance') {
            result = TitleInstance.findAllWhere( "${key2}": value ).collect{ it ->
                [id: it.id, name: it.title, link: g.createLink(controller:'title', action:'show', id: it.id)]
            }
        }
        if (key1 == 'TitleInstancePackagePlatform') {
            result = TitleInstancePackagePlatform.findAllWhere( "${key2}": value ).collect{ it ->
                [id: it.id, name: 'TitleInstancePackagePlatform', link: g.createLink(controller:'tipp', action:'show', id: it.id)]
            }
        }

        result
    }
}
