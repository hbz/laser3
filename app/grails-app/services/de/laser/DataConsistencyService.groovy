package de.laser

import com.k_int.kbplus.*

class DataConsistencyService {

    def springSecurityService

    def checkImportIds() {
        Map result = [
                Orgs: [],
                Packages: [],
                Platforms: [],
                TitleInstances: [],
                Tipps: []
        ]

        result.Orgs = Org.executeQuery(
                'SELECT org.impId as impId, count(org.impId) as cnt FROM Org org GROUP BY org.impId ORDER BY org.impId'
        ).findAll{ it -> it[1] > 1}

        result.Packages = Package.executeQuery(
                'SELECT pkg.impId as impId, count(pkg.impId) as cnt FROM Package pkg GROUP BY pkg.impId ORDER BY pkg.impId'
        ).findAll{ it -> it[1] > 1}

        result.Platforms = Platform.executeQuery(
                'SELECT pf.impId as impId, count(pf.impId) as cnt FROM Platform pf GROUP BY pf.impId ORDER BY pf.impId'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstances = TitleInstance.executeQuery(
                'SELECT ti.impId as impId, count(ti.impId) as cnt FROM TitleInstance ti GROUP BY ti.impId ORDER BY ti.impId'
        ).findAll{ it -> it[1] > 1}

        result.Tipps = TitleInstancePackagePlatform.executeQuery(
                'SELECT tipp.impId as impId, count(tipp.impId) as cnt FROM TitleInstancePackagePlatform tipp GROUP BY tipp.impId ORDER BY tipp.impId'
        ).findAll{ it -> it[1] > 1}

        result
    }

    def checkTitles() {
        Map result = [
                Orgs: [:],
                Packages: [:],
                Platforms: [:]
                // TitleInstances: [:],
                // Tipps: [:]
        ]

        // Orgs

        result.Orgs.name = Org.executeQuery(
                'SELECT org.name as name, count(org.name) as cnt FROM Org org GROUP BY org.name ORDER BY org.name'
        ).findAll{ it -> it[1] > 1}

        result.Orgs.shortname = Org.executeQuery(
                'SELECT org.shortname as shortname, count(org.shortname) as cnt FROM Org org GROUP BY org.shortname ORDER BY org.shortname'
        ).findAll{ it -> it[1] > 1}

        result.Orgs.shortcode = Org.executeQuery(
                'SELECT org.shortcode as shortcode, count(org.shortcode) as cnt FROM Org org GROUP BY org.shortcode ORDER BY org.shortcode'
        ).findAll{ it -> it[1] > 1}

        result.Orgs.sortname = Org.executeQuery(
                'SELECT org.sortname as sortname, count(org.sortname) as cnt FROM Org org GROUP BY org.sortname ORDER BY org.sortname'
        ).findAll{ it -> it[1] > 1}

        // Packages

        result.Packages.name = Package.executeQuery(
                'SELECT pkg.name as name, count(pkg.name) as cnt FROM Package pkg GROUP BY pkg.name ORDER BY pkg.name'
        ).findAll{ it -> it[1] > 1}

        result.Packages.sortName = Package.executeQuery(
                'SELECT pkg.sortName as sortName, count(pkg.sortName) as cnt FROM Package pkg GROUP BY pkg.sortName ORDER BY pkg.sortName'
        ).findAll{ it -> it[1] > 1}

        // Platforms

        result.Platforms.name = Platform.executeQuery(
                'SELECT pf.name as name, count(pf.name) as cnt FROM Platform pf GROUP BY pf.name ORDER BY pf.name'
        ).findAll{ it -> it[1] > 1}

        result.Platforms.normname = Platform.executeQuery(
                'SELECT pf.normname as normname, count(pf.normname) as cnt FROM Platform pf GROUP BY pf.normname ORDER BY pf.normname'
        ).findAll{ it -> it[1] > 1}
        /*
        result.Platforms.primaryUrl = Platform.executeQuery(
                'SELECT pf.primaryUrl as primaryUrl, count(pf.primaryUrl) as cnt FROM Platform pf GROUP BY pf.primaryUrl ORDER BY pf.primaryUrl'
        ).findAll{ it -> it[1] > 1}
        */

        // TitleInstances

        /*
        result.TitleInstances.title = TitleInstance.executeQuery(
                'SELECT ti.title as title, count(ti.title) as cnt FROM TitleInstance ti GROUP BY ti.title ORDER By ti.title'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstances.normTitle = TitleInstance.executeQuery(
                'SELECT ti.normTitle as normTitle, count(ti.normTitle) as cnt FROM TitleInstance ti GROUP BY ti.normTitle ORDER BY ti.normTitle'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstances.keyTitle = TitleInstance.executeQuery(
                'SELECT ti.keyTitle as keyTitle, count(ti.keyTitle) as cnt FROM TitleInstance ti GROUP BY ti.keyTitle ORDER BY ti.keyTitle'
        ).findAll{ it -> it[1] > 1}

        result.TitleInstances.sortTitle = TitleInstance.executeQuery(
                'SELECT ti.sortTitle as sortTitle, count(ti.sortTitle) as cnt FROM TitleInstance ti GROUP BY ti.sortTitle ORDER BY ti.sortTitle'
        ).findAll{ it -> it[1] > 1}
        */

        // TitleInstancePackagePlatforms
        /*
        result.Tipps.hostPlatformURL = TitleInstancePackagePlatform.executeQuery(
                'SELECT tipp.hostPlatformURL as hostPlatformURL, count(tipp.hostPlatformURL) as cnt FROM TitleInstancePackagePlatform tipp GROUP BY tipp.hostPlatformURL ORDER BY tipp.hostPlatformURL'
        ).findAll{ it -> it[1] > 1}
        */
        
        result
    }
}
