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

    List getTargettedDocuments(Org org) {
        List<DocContext> furtherDocs = DocContext.findAllByTargetOrgAndShareConf(org,RDStore.SHARE_CONF_UPLOADER_AND_TARGET)
        furtherDocs
    }

}
