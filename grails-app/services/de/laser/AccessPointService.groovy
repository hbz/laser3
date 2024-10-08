package de.laser


import de.laser.storage.RDStore
import de.laser.oap.OrgAccessPoint
import de.laser.oap.OrgAccessPointLink
import de.laser.utils.LocaleUtils
import de.laser.wekb.Platform
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.context.MessageSource

/**
 * This service manages access point manipulations and exports
 */
@Transactional
class AccessPointService {

    ExportService exportService
    ExportClickMeService exportClickMeService
    EscapeService escapeService
    MessageSource messageSource

    /**
     * Deletes the given accessPointData
     * @param accessPointData to delete
     */
    void deleteAccessPointData(AccessPointData accessPointData) {
        accessPointData.delete()
    }

    /**
     * Links the given platform to the given access point if no link exists between them
     * @return an empty {@link Map} in case of success; an error message otherwise
     */
    Map<String,Object> linkPlatform(GrailsParameterMap params) {
        Map<String,Object> result = [:]
        OrgAccessPoint accessPoint = OrgAccessPoint.get(params.accessPointId)
        OrgAccessPointLink oapl = new OrgAccessPointLink()
        oapl.active = true
        oapl.oap = accessPoint
        if (params.platforms) {
            Platform platform = Platform.get(params.platforms)
            oapl.platform = platform
            String hql = "select oap from OrgAccessPoint oap join oap.oapp as oapl where oapl.active = true and oapl.platform.id =${platform.id} and oapl.oap=:oap and oapl.subPkg is null"
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

    /**
     * Deactivates the given access point link
     * @result error if a such occurs
     */
    Map<String,Object> unlinkPlatform(OrgAccessPointLink aoplInstance) {
        Map<String,Object> result = [:]
        aoplInstance.delete()
        result
    }

    /**
     * Retrieves a list of access points for the given institution with the platform and subscription link counts for each of the access point
     * @param org the institution whose data should be retrieved
     * @return a map of structure
     * [
     *      oap: the access point
     *      platformLinkCount: link counts to platforms
     *      subscriptionLinkCount: link counts to subscriptions
     * ]
     */
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

    /**
     * Exports the given list of access points in the format specified
     * @param accessPoints the access points to export
     * @param format {@link ExportClickMeService.FORMAT} output format enum constant
     * @return an Excel worksheet containing the access point methods and properties
     */
    def exportAccessPoints(List<OrgAccessPoint> accessPoints, ExportClickMeService.FORMAT format) {
        List titles = []

        Locale locale = LocaleUtils.getCurrentLocale()
        titles.addAll([messageSource.getMessage('default.name.label', null, locale),
                messageSource.getMessage('accessMethod.label', null, locale),
                       messageSource.getMessage('accessRule.plural', null, locale)])

        Map sheetData = [:]
        List accessPointData = []
        accessPoints.sort {it.accessMethod.value}.each { accessPoint ->
            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_IP) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each { String ipRangeString ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, ipRangeString))
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each { String ipRangeString ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, ipRangeString))
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each { String ipRangeString ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' ', accessPointUrl = accessPoint.url ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, ipRangeString))
                    row.add(exportClickMeService.createTableCell(format, accessPointUrl))
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each { String ipRangeString ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' ', accessPointUrl = accessPoint.url ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, ipRangeString))
                    row.add(exportClickMeService.createTableCell(format, accessPointUrl))
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_PROXY) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each { String ipRangeString ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, ipRangeString))
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each { String ipRangeString ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, ipRangeString))
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_SHIBBOLETH) {
                List row = []
                String accessPointName = accessPoint.name ?: ' ', entityId = accessPoint.entityId ?: ' '
                row.add(exportClickMeService.createTableCell(format, accessPointName))
                row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                row.add(exportClickMeService.createTableCell(format, entityId))
                accessPointData.add(row)
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_OA) {
                List row = []
                String accessPointName = accessPoint.name ?: ' ', entityId = accessPoint.entityId ?: ' '
                row.add(exportClickMeService.createTableCell(format, accessPointName))
                row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                row.add(exportClickMeService.createTableCell(format, entityId))
                accessPointData.add(row)
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_MAIL_DOMAIN) {
                accessPoint.accessPointData.each { AccessPointData apd ->
                    List row = []
                    String accessPointName = accessPoint.name ?: ' ', data = apd.data ?: ' '
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessPoint.accessMethod.getI10n('value')))
                    row.add(exportClickMeService.createTableCell(format, data))
                    accessPointData.add(row)
                }
            }


            //sheetData.put(escapeService.escapeString(accessPoint.name), [titleRow: titles, columnData: accessPointData])
        }
        sheetData.put(escapeService.escapeString(messageSource.getMessage('org.accessPoints.export', null, locale),), [titleRow: titles, columnData: accessPointData])
        return exportService.generateXLSXWorkbook(sheetData)
    }

    /**
     * Exports the IP ranges of the given institutions
     * @param orgs the institutions whose data should be exported
     * @param onlyMap output the raw map or an Excel worksheet?
     * @return a map containing the results if onlyMap is true, an Excel worksheet with the IP ranges otherwise
     */
    def exportIPsOfOrgs(List<Org> orgs, ExportClickMeService.FORMAT format, boolean onlyMap = false) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('accessPoint.ip.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.range',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.cidr',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { OrgAccessPoint accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_IP) {

                    Map<String, Object> accessPointDataList = accessPoint.getAccessPointIpRanges()

                    accessPointDataList.ipv4Ranges.each {
                        row = []
                        String sortname = org.sortname ?: ' ', name = it.name ?: ' ', ipRange = it.ipRange ?: ' ', ipCidr = it.ipCidr ?: ' '
                        row.add(exportClickMeService.createTableCell(format, sortname))
                        row.add(exportClickMeService.createTableCell(format, org.name))
                        row.add(exportClickMeService.createTableCell(format, name))
                        row.add(exportClickMeService.createTableCell(format, 'IPv4'))
                        row.add(exportClickMeService.createTableCell(format, ipRange))
                        row.add(exportClickMeService.createTableCell(format, ipCidr))
                        accessPointData.add(row)
                    }

                    accessPointDataList.ipv6Ranges.each {
                        row = []
                        String sortname = org.sortname ?: ' ', name = it.name ?: ' ', ipRange = it.ipRange ?: ' ', ipCidr = it.ipCidr ?: ' '
                        row.add(exportClickMeService.createTableCell(format, sortname))
                        row.add(exportClickMeService.createTableCell(format, org.name))
                        row.add(exportClickMeService.createTableCell(format, name))
                        row.add(exportClickMeService.createTableCell(format, 'IPv6'))
                        row.add(exportClickMeService.createTableCell(format, ipRange))
                        row.add(exportClickMeService.createTableCell(format, ipCidr))
                        accessPointData.add(row)
                    }
                }
            }
            //accessPointData.add([exportClickMeService.createTableCell(format, ' ')])

        }

        if(onlyMap){
            return [titleRow: titles, columnData: accessPointData]
        }else {
            return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportIPs.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
        }
    }

    /**
     * Exports the proxy configurations of the given institutions
     * @param orgs the institutions whose data should be exported
     * @param onlyMap output the raw map or an Excel worksheet?
     * @return a map containing the results if onlyMap is true, an Excel worksheet with the proxy configurations otherwise
     */
    def exportProxysOfOrgs(List<Org> orgs, ExportClickMeService.FORMAT format, boolean onlyMap = false) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('accessPoint.ip.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.range',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.cidr',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_PROXY) {

                    Map<String, Object> accessPointDataList = accessPoint.getAccessPointIpRanges()

                    accessPointDataList.ipv4Ranges.each {
                        row = []
                        String sortname = org.sortname ?: ' ', name = it.name ?: ' ', ipRange = it.ipRange ?: ' ', ipCidr = it.ipCidr ?: ' '
                        row.add(exportClickMeService.createTableCell(format, sortname))
                        row.add(exportClickMeService.createTableCell(format, org.name))
                        row.add(exportClickMeService.createTableCell(format, name))
                        row.add(exportClickMeService.createTableCell(format, 'IPv4'))
                        row.add(exportClickMeService.createTableCell(format, ipRange))
                        row.add(exportClickMeService.createTableCell(format, ipCidr))
                        accessPointData.add(row)
                    }

                    accessPointDataList.ipv6Ranges.each {
                        row = []
                        String sortname = org.sortname ?: ' ', name = it.name ?: ' ', ipRange = it.ipRange ?: ' ', ipCidr = it.ipCidr ?: ' '
                        row.add(exportClickMeService.createTableCell(format, sortname))
                        row.add(exportClickMeService.createTableCell(format, org.name))
                        row.add(exportClickMeService.createTableCell(format, name))
                        row.add(exportClickMeService.createTableCell(format, 'IPv6'))
                        row.add(exportClickMeService.createTableCell(format, ipRange))
                        row.add(exportClickMeService.createTableCell(format, ipCidr))
                        accessPointData.add(row)
                    }
                }
            }
            //accessPointData.add([[field: '', style: null]])

        }
        if(onlyMap){
            return [titleRow: titles, columnData: accessPointData]
        }else {
            return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportProxys.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
        }
    }

    /**
     * Exports the EZProxy configurations of the given institutions
     * @param orgs the institutions whose data should be exported
     * @param onlyMap output the raw map or an Excel worksheet?
     * @return a map containing the results if onlyMap is true, an Excel worksheet with the EZProxy ranges otherwise
     */
    def exportEZProxysOfOrgs(List<Org> orgs, ExportClickMeService.FORMAT format, boolean onlyMap = false) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('accessPoint.ezproxy.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.range',null, locale),
                       messageSource.getMessage('accessPoint.ip.format.cidr',null, locale),
                       messageSource.getMessage('accessPoint.url',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY) {

                    Map<String, Object> accessPointDataList = accessPoint.getAccessPointIpRanges()

                    accessPointDataList.ipv4Ranges.each {
                        row = []
                        String sortname = org.sortname ?: ' ', name = it.name ?: ' ', ipRange = it.ipRange ?: ' ', ipCidr = it.ipCidr ?: ' ', accessPointUrl = accessPoint.url ?: ''
                        row.add(exportClickMeService.createTableCell(format, sortname))
                        row.add(exportClickMeService.createTableCell(format, org.name))
                        row.add(exportClickMeService.createTableCell(format, name))
                        row.add(exportClickMeService.createTableCell(format, 'IPv4'))
                        row.add(exportClickMeService.createTableCell(format, ipRange))
                        row.add(exportClickMeService.createTableCell(format, ipCidr))
                        row.add(exportClickMeService.createTableCell(format, accessPointUrl))
                        accessPointData.add(row)
                    }

                    accessPointDataList.ipv6Ranges.each {
                        row = []
                        String sortname = org.sortname ?: ' ', name = it.name ?: ' ', ipRange = it.ipRange ?: ' ', ipCidr = it.ipCidr ?: ' ', accessPointUrl = accessPoint.url ?: ''
                        row.add(exportClickMeService.createTableCell(format, sortname))
                        row.add(exportClickMeService.createTableCell(format, org.name))
                        row.add(exportClickMeService.createTableCell(format, name))
                        row.add(exportClickMeService.createTableCell(format, 'IPv6'))
                        row.add(exportClickMeService.createTableCell(format, ipRange))
                        row.add(exportClickMeService.createTableCell(format, ipCidr))
                        row.add(exportClickMeService.createTableCell(format, accessPointUrl))
                        accessPointData.add(row)
                    }
                }
            }
            //accessPointData.add([[field: '', style: null]])

        }

        if(onlyMap){
            return [titleRow: titles, columnData: accessPointData]
        }else {
            return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportEZProxys.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
        }
    }

    /**
     * Exports the Shibboleth settings of the given institutions
     * @param orgs the institutions whose data should be exported
     * @param onlyMap output the raw map or an Excel worksheet?
     * @return a map containing the results if onlyMap is true, an Excel worksheet with the Shibboleth settings otherwise
     */
    def exportShibbolethsOfOrgs(List<Org> orgs, ExportClickMeService.FORMAT format, boolean onlyMap = false) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('accessPoint.shibboleth.name.label',null, locale),
                       messageSource.getMessage('accessMethod.label',null, locale),
                       messageSource.getMessage('accessPoint.entitiyId.label',null, locale)
        ])

        List accessPointData = []
        orgs.each { Org org ->
            List row = []

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { accessPoint ->

                if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_SHIBBOLETH) {
                    String sortname = org.sortname ?: ' ', name = org.name ?: ' ', accessPointName = accessPoint.name ?: ' ', accessMethod = accessPoint.accessMethod ? accessPoint.accessMethod.getI10n('value') : ' ', entityId = accessPoint.entityId ?: ' '
                    row.add(exportClickMeService.createTableCell(format, sortname))
                    row.add(exportClickMeService.createTableCell(format, name))
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, accessMethod))
                    row.add(exportClickMeService.createTableCell(format, entityId))
                    accessPointData.add(row)
                }
            }
            
            //accessPointData.add([[field: '', style: null]])


        }

        if(onlyMap){
            return [titleRow: titles, columnData: accessPointData]
        }else {
            return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportShibboleths.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
        }

    }

    /**
     * Exports the mail domain settings of the given institutions
     * @param orgs the institutions whose data should be exported
     * @param onlyMap output the raw map or an Excel worksheet?
     * @return a map containing the results if onlyMap is true, an Excel worksheet with the Shibboleth settings otherwise
     */
    def exportMailDomainsOfOrgs(List<Org> orgs, ExportClickMeService.FORMAT format, boolean onlyMap = false) {

        List titles = []
        Locale locale = LocaleUtils.getCurrentLocale()

        titles.addAll([messageSource.getMessage('org.sortname.label',null, locale),
                       'Name',
                       messageSource.getMessage('accessPoint.mailDomain.name.label',null, locale),
                       'Mail-Domain'
        ])

        List accessPointData = []
        orgs.each { Org org ->

            List<OrgAccessPoint> accessPoints = OrgAccessPoint.findAllByOrg(org, [sort: ["name": 'asc', "accessMethod": 'asc']])
            accessPoints.each { OrgAccessPoint accessPoint ->
                accessPoint.getAccessPointMailDomains().accessPointMailDomains.each { Map apd ->
                    List row = []
                    String sortname = org.sortname ?: ' ', accessPointName = accessPoint.name ?: ' '
                    row.add(exportClickMeService.createTableCell(format, sortname))
                    row.add(exportClickMeService.createTableCell(format, org.name))
                    row.add(exportClickMeService.createTableCell(format, accessPointName))
                    row.add(exportClickMeService.createTableCell(format, apd.mailDomain))
                    accessPointData.add(row)
                }
            }
        }

        if(onlyMap){
            return [titleRow: titles, columnData: accessPointData]
        }else {
            return exportService.generateXLSXWorkbook(["${messageSource.getMessage('subscriptionDetails.members.exportMailDomains.fileName',null, locale)}": [titleRow: titles, columnData: accessPointData]])
        }

    }

    /**
     * Check for existing name in all supported locales and return available suggestions for IP Access Method
     * A simpler solution would be nice
     * @param org the institution whose data should be queried
     * @return a list of available access options
     */
    List availableOptions(Org org) {

        List availableLanguageKeys = ['accessPoint.option.remoteAccess', 'accessPoint.option.woRemoteAccess', 'accessPoint.option.vpn']
        Locale locale = LocaleUtils.getCurrentLocale()
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

    /**
     * Retrieves the platforms to the given access point
     * @param params the parameter map containing sorting parameters of the result list
     * @param orgAccessPoint the access point to which linked platforms should be retrieved
     * @return a list of platforms linked to the access point
     * @see Platform
     * @see OrgAccessPoint
     */
    List getLinkedPlatforms(GrailsParameterMap params, OrgAccessPoint orgAccessPoint) {
        String sort = params.sort ?: "LOWER(p.name)"
        String order = params.order ?: "ASC"
        String qry1 = "select new map(p as platform,oapl as aplink) from Platform p join p.oapp as oapl where oapl.active = true and oapl.oap=${orgAccessPoint.id} and oapl.subPkg is null order by ${sort} ${order}"
        Platform.executeQuery(qry1)
    }
}
