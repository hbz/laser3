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

    Map getDocuments(User user, Org org, Map params) {
        Map filterQuery = filterService.getDocumentQuery(params)
        Set<DocContext> allDocuments = []
        allDocuments.addAll(DocContext.executeQuery('select dc from DocContext dc join dc.owner d where (d.owner = :org or dc.targetOrg = :org)'+filterQuery.query,[org:org]+filterQuery.queryParams))
        //get documents shared for consortia
        if(!org.getallOrgTypeIds().contains(RDStore.OT_CONSORTIUM.id) && org.getallOrgTypeIds().contains(RDStore.OT_INSTITUTION.id)) {
            List consortia = Combo.findAllByFromOrgAndType(org, RefdataValue.getByValueAndCategory('Consortium','Combo Type')).collect {
                combo -> combo.toOrg
            }
            allDocuments.addAll(DocContext.executeQuery('select dc from DocContext dc join dc.owner d where d.owner in :consortia'+filterQuery.query,[consortia:consortia]+filterQuery.queryParams))
        }
        org.documents = []
        allDocuments.each { docctx ->

            boolean inOwnerOrg = false
            boolean isCreator = false

            if(docctx.owner.owner.id == contextService.org.id)
                inOwnerOrg = true
            if(docctx.owner.creator.id == user.id)
                isCreator = true

            boolean visible = false

            switch(docctx.shareConf) {
                case RDStore.SHARE_CONF_CREATOR: if(isCreator) visible = true
                    break
                case RDStore.SHARE_CONF_UPLOADER_ORG: if(inOwnerOrg) visible = true
                    break
                case RDStore.SHARE_CONF_UPLOADER_AND_TARGET: if(inOwnerOrg || contextService.org.id == docctx.targetOrg.id) visible = true
                    break
                case RDStore.SHARE_CONF_CONSORTIUM:
                case RDStore.SHARE_CONF_ALL: visible = true //definition says that everyone with "access" to target org. How are such access roles defined and where?
                    break
                default:
                    if(docctx.shareConf) log.debug(docctx.shareConf)
                    else visible = true
                    break
            }

            if(visible)
                org.documents.add(docctx)
        }
        List allUploaders = Doc.executeQuery('select dc.shareConf,d.creator from DocContext dc join dc.owner d where d.owner = :org order by d.creator.display asc',[org:org])
        Set availableUsers = []
        allUploaders.each { row ->
            if(row[0] == RDStore.SHARE_CONF_CREATOR) {
                if(row[1] == user)
                    availableUsers.add(row[1])
            }
            else availableUsers.add(row[1])
        }
        return [user:user,org:org,availableUsers:availableUsers]
    }
}
