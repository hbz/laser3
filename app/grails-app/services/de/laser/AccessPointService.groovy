package de.laser


import com.k_int.kbplus.ExportService
import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgAccessPoint
import com.k_int.kbplus.OrgAccessPointLink
import com.k_int.kbplus.OrgRole
import de.laser.helper.DateUtil
import de.laser.helper.RDStore
import grails.transaction.Transactional
import grails.util.Holders
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder

import java.text.SimpleDateFormat

@Transactional
class AccessPointService {

    MessageSource messageSource
    ExportService exportService
    Locale locale
    EscapeService escapeService

    @javax.annotation.PostConstruct
    void init() {
        messageSource = Holders.grailsApplication.mainContext.getBean('messageSource')
        locale = org.springframework.context.i18n.LocaleContextHolder.getLocale()
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
        SimpleDateFormat sdf = DateUtil.getSDF_NoTime()

        List titles = []

        titles.addAll([messageSource.getMessage('accessMethod.label', null, LocaleContextHolder.getLocale()),
                       messageSource.getMessage('accessRule.plural', null, LocaleContextHolder.getLocale())])

        Map sheetData = [:]
        accessPoints.each { accessPoint ->
            List accessPointData = []

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_IP) {
                accessPoint.getIpRangeStrings('ipv4', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod.getI10n('value') ?: '', style: null])
                    row.add([field: it ?: '', style: null])
                    accessPointData.add(row)
                }

                accessPoint.getIpRangeStrings('ipv6', 'ranges').each {
                    List row = []
                    row.add([field: accessPoint.accessMethod.getI10n('value') ?: '', style: null])
                    row.add([field: it ?: '', style: null])
                    accessPointData.add(row)
                }
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_EZPROXY) {
                List row = []
                row.add([field: accessPoint.accessMethod.getI10n('value') ?: '', style: null])
                row.add([field: accessPoint.url ?: '', style: null])
                accessPointData.add(row)
            }

            if (accessPoint.accessMethod == RDStore.ACCESS_POINT_TYPE_SHIBBOLETH) {
                List row = []
                row.add([field: accessPoint.accessMethod.getI10n('value') ?: '', style: null])
                row.add([field: accessPoint.entityId ?: '', style: null])
                accessPointData.add(row)
            }
            
            sheetData.put(escapeService.escapeString(accessPoint.name), [titleRow: titles, columnData: accessPointData])
        }
        return exportService.generateXLSXWorkbook(sheetData)
    }
}
