package de.laser


import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class OrgDocumentService {

    List getTargettedDocuments(Org org) {
        List<DocContext> furtherDocs = DocContext.findAllByTargetOrgAndShareConf(org,RDStore.SHARE_CONF_UPLOADER_AND_TARGET)
        furtherDocs
    }

}
