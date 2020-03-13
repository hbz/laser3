package de.laser

import com.k_int.kbplus.Org
import com.k_int.kbplus.OrgAccessPoint
import com.k_int.kbplus.OrgAccessPointLink
import grails.transaction.Transactional

@Transactional
class AccessPointService {

    List<Map> getOapListWithLinkCounts(Org org)
    {
        List<Map> oapListWithLinkCounts = []
        List oapList = OrgAccessPoint.findAllByOrg(org,  [sort: ["name": 'asc', "accessMethod" : 'asc']])
        oapList.each {
            Map tmpMap = [:]
            tmpMap['oap'] = it
            tmpMap['platformLinkCount'] = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNull(true, it)
            tmpMap['subscriptionLinkCount'] = OrgAccessPointLink.countByActiveAndOapAndSubPkgIsNotNull(true, it)
            oapListWithLinkCounts.add(tmpMap)
        }
        return oapListWithLinkCounts
    }
}
