package de.laser

import com.k_int.kbplus.Combo
import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.Org
import com.k_int.kbplus.RefdataValue
import com.k_int.kbplus.auth.User
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class OrgDocumentService {

    def contextService
    def filterService

    Set getAllDocuments(Org org, Map params) {
        Map filterQuery = filterService.getDocumentQuery(params)
        Set<DocContext> allDocuments = []
        allDocuments.addAll(DocContext.executeQuery('select dc from DocContext dc join dc.owner d where (d.owner = :org or dc.org = :org) and dc.status = null and d.contentType in (1,3)'+filterQuery.query,[org:org]+filterQuery.queryParams))
        //get documents shared for consortia
        if(!org.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id) && org.getallOrgTypeIds().contains(RDStore.OT_INSTITUTION.id)) {
            List consortia = Combo.findAllByFromOrgAndType(org, RefdataValue.getByValueAndCategory('Consortium','Combo Type')).collect {
                combo -> combo.toOrg
            }
            allDocuments.addAll(DocContext.executeQuery('select dc from DocContext dc join dc.owner d where d.owner in :consortia and dc.status = null and d.contentType in (1,3)'+filterQuery.query,[consortia:consortia]+filterQuery.queryParams))
        }
        allDocuments
    }

    Set getAvailableUploaders(User user) {
        List allUploaders = Doc.executeQuery('select dc.shareConf,d.creator from DocContext dc join dc.owner d where d.owner = :org order by d.creator.display asc',[org:contextService.org])
        Set availableUsers = []
        allUploaders.each { row ->
            if(row[0] == RDStore.SHARE_CONF_CREATOR) {
                if(row[1] == user)
                    availableUsers.add(row[1])
            }
            else availableUsers.add(row[1])
        }
        availableUsers
    }

}
