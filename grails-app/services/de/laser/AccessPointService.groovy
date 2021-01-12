package de.laser

import com.k_int.kbplus.ExportService
import de.laser.helper.RDStore
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

@Transactional
class AccessPointService {

    ExportService exportService
    EscapeService escapeService
    MessageSource messageSource

    void deleteIpRange(AccessPointData accessPointData) {
        accessPointData.delete()
    }

    Map<String,Object> linkPlatform(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        OrgAccessPoint accessPoint = OrgAccessPoint.get(params.accessPointId)
        OrgAccessPointLink oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = accessPoint
        if (params.platforms) {
            oapl.platform = Platform.get(params.platforms)
            String hql = "select oap from OrgAccessPoint oap join oap.oapp as oapl where oapl.active = true and oapl.platform.id =${accessPoint.id} and oapl.oap=:oap and oapl.subPkg is null order by LOWER(oap.name)"
            Set<OrgAccessPoint> existingActiveAP = OrgAccessPoint.executeQuery(hql, ['oap' : accessPoint])
            if (! existingActiveAP.isEmpty()){
                result.error = "Existing active AccessPoint for platform"
            }
            if (! oapl.save()) {
                result.error = "Could not link AccessPoint to Platform"
            }
        }
        result
    }

    Map<String,Object> unlinkPlatform(OrgAccessPointLink aoplInstance) {
        Map<String,Object> result = [:]
        aoplInstance.active = false
        if (! aoplInstance.save()) {
            log.debug(aoplInstance.errors.toString())
            result.error = "Error updating AccessPoint for platform"
        }
        result
    }

    List<Map> getOapListWithLinkCounts(Org org) {
        List<Map> oapListWithLinkCounts = []
        List oapList = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
        oapList.each {
            Map tmpMap = [:]
            tmpMap['oap'] = it
            tmpMap['platformLinkCount'] = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNull(true, it)
            tmpMap['subscriptionLinkCount'] = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNotNull(true, it)
            oapListWithLinkCounts.add(tmpMap)
        }
        return oapListWithLinkCounts
    }

    def exportAccessPoints(List accessPoints, Org contextOrg) {
        List titles = []

        titles.addAll([messageSource.getMessage('accessMethod.label', null, LocaleContextHolder.getLocale()),
                       messageSource.getMessage('accessRule.plural', null, LocaleContextHolder.getLocale())])

        Map sheetData = [:]
        accessPoints.each { accessPoint ->
            List accessPointData = []

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_IP) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: it ?: '', style: null])
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: it ?: '', style: null])
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: it ?: '', style: null])
                    row.add([field: accessPoint.url ?: '', style: null])
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: it ?: '', style: null])
                    row.add([field: accessPoint.url ?: '', style: null])
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_PROXY) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: it ?: '', style: null])
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: it ?: '', style: null])
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_SHIBBOLETH) {
                List row = []
                row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                row.add([field: accessPoint.entityId ?: '', style: null])
                accessPointData.add(row)
            }
            
            sheetData.put(escapeService.escapeString(accessPoint.name), [titleRow: titles, columnData: accessPointData])
        }
        return exportService.generateXLSXWorkbook(sheetData)
    }
    def exportIPsOfOrgs(List<Org> orgs) {

        List titles = []
        Locale locale = LocaleContextHolder.getLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('org.shortname.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.range',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.cidr',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []
            row.add([field: org.sortname ?: '', style: null])
            row.add([field: org.name ?: '', style: null])
            row.add([field: org.shortname ?: '', style: null])
            accessPointData.add(row)

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_IP) {

                    Map<String, Object> accessPointDataList = accessPoint.getAccessPointIpRanges()

                    accessPointDataList.ipv4Ranges.each {
                        row = []
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: it.name ?: '', style: null])
                        row.add([field: 'IPv4', style: null])
                        row.add([field: it.ipRange ?: '', style: null])
                        row.add([field: it.ipCidr ?: '', style: null])
                        accessPointData.add(row)
                    }

                    accessPointDataList.ipv6Ranges.each {
                        row = []
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: it.name ?: '', style: null])
                        row.add([field: 'IPv6', style: null])
                        row.add([field: it.ipRange ?: '', style: null])
                        row.add([field: it.ipCidr ?: '', style: null])
                        accessPointData.add(row)
                    }
                }
            }
            accessPointData.add([[field: '', style: null]])

        }

        return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
    }

    def exportProxysOfOrgs(List<Org> orgs) {

        List titles = []
        Locale locale = LocaleContextHolder.getLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('org.shortname.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.range',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.cidr',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []
            row.add([field: org.sortname ?: '', style: null])
            row.add([field: org.name ?: '', style: null])
            row.add([field: org.shortname ?: '', style: null])
            accessPointData.add(row)

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_PROXY) {

                    Map<String, Object> accessPointDataList = accessPoint.getAccessPointIpRanges()

                    accessPointDataList.ipv4Ranges.each {
                        row = []
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: it.name ?: '', style: null])
                        row.add([field: 'IPv4', style: null])
                        row.add([field: it.ipRange ?: '', style: null])
                        row.add([field: it.ipCidr ?: '', style: null])
                        accessPointData.add(row)
                    }

                    accessPointDataList.ipv6Ranges.each {
                        row = []
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: it.name ?: '', style: null])
                        row.add([field: 'IPv6', style: null])
                        row.add([field: it.ipRange ?: '', style: null])
                        row.add([field: it.ipCidr ?: '', style: null])
                        accessPointData.add(row)
                    }
                }
            }
            accessPointData.add([[field: '', style: null]])

        }

        return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
    }
    def exportEZProxysOfOrgs(List<Org> orgs) {

        List titles = []
        Locale locale = LocaleContextHolder.getLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('org.shortname.label',null, locale),
                       messageSource.getMessage('accessPoint.ezproxy.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.range',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.cidr',null, locale),
                       messageSource.getMessage('accessPoint.url',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []
            row.add([field: org.sortname ?: '', style: null])
            row.add([field: org.name ?: '', style: null])
            row.add([field: org.shortname ?: '', style: null])
            accessPointData.add(row)

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY) {

                    Map<String, Object> accessPointDataList = accessPoint.getAccessPointIpRanges()

                    accessPointDataList.ipv4Ranges.each {
                        row = []
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: it.name ?: '', style: null])
                        row.add([field: 'IPv4', style: null])
                        row.add([field: it.ipRange ?: '', style: null])
                        row.add([field: it.ipCidr ?: '', style: null])
                        row.add([field: accessPoint.url ?: '', style: null])
                        accessPointData.add(row)
                    }

                    accessPointDataList.ipv6Ranges.each {
                        row = []
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: '', style: null])
                        row.add([field: it.name ?: '', style: null])
                        row.add([field: 'IPv6', style: null])
                        row.add([field: it.ipRange ?: '', style: null])
                        row.add([field: it.ipCidr ?: '', style: null])
                        row.add([field: accessPoint.url ?: '', style: null])
                        accessPointData.add(row)
                    }
                }
            }
            accessPointData.add([[field: '', style: null]])

        }

        return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
    }
    def exportShibbolethsOfOrgs(List<Org> orgs) {

        List titles = []
        Locale locale = LocaleContextHolder.getLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('org.shortname.label',null, locale),
                       messageSource.getMessage('accessPoint.shibboleth.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.entitiyId.label',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []
            row.add([field: org.sortname ?: '', style: null])
            row.add([field: org.name ?: '', style: null])
            row.add([field: org.shortname ?: '', style: null])

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_SHIBBOLETH) {

                    row.add([field: accessPoint.name ?: '', style: null])
                    row.add([field: accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : '', style: null])
                    row.add([field: accessPoint.entityId ?: '', style: null])
                }
            }
            accessPointData.add(row)
            accessPointData.add([[field: '', style: null]])


        }

        return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
    }

    /**
     * Check for existing name in all supported locales and return available suggestions for IP Access Method
     * A simpler solution would be nice
     */
    List availableOptions(Org org) {

        List availableLanguageKeys = ['accessPoint.option.remoteAccess', 'accessPoint.option.woRemoteAccess', 'accessPoint.option.vpn']
        Locale locale = LocaleContextHolder.getLocale()
        Map localizedAccessPointNameSuggestions = [:]
        availableLanguageKeys.each { key ->
            localizedAccessPointNameSuggestions[messageSource.getMessage(key, null,locale)] = key
        }
        List<OrgAccessPoint> existingOapIpInstances = OrgAccessPoint.findAllByOrgAndAccessMethod(org, RefdataValue.getByValue('ip'))

        if (existingOapIpInstances) {
            existingOapIpInstances.each { OrgAccessPoint oap ->
                if (localizedAccessPointNameSuggestions.keySet().contains(oap.name)){
                    if (localizedAccessPointNameSuggestions[oap.name]) {
                        availableLanguageKeys.removeAll {languageKey ->
                            languageKey == localizedAccessPointNameSuggestions[oap.name]
                        }
                    }
                }
            }
        }
        List resultList = []
        availableLanguageKeys.each { it ->
            resultList.add(["${it}" : messageSource.getMessage(it,null,locale)])
        }
        resultList.add(["accessPoint.option.customName" : ''])
        return resultList
    }

    List getLinkedPlatforms(GrailsParameterMap params, OrgAccessPoint orgAccessPoint) {
        String sort = params.sort ?: "LOWER(p.name)"
        String order = params.order ?: "ASC"
        String qry1 = "select new map(p as platform,oapl as aplink) from Platform p join p.oapp as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id} and oapl.subPkg is null order by ${sort} ${order}"
        Platform.executeQuery(qry1)
    }
}
