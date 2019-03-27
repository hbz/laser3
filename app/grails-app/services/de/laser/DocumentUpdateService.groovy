package de.laser

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.Org
import de.laser.helper.RDStore
import grails.transaction.Transactional

@Transactional
class DocumentUpdateService {

    void updateShareConfigurations() {
        //dry run
        DocContext.executeQuery('select dc from DocContext dc where dc.owner.creator != null and dc.owner.owner = null and dc.sharedFrom = null').each { dcObj ->
            DocContext dc = (DocContext) dcObj
            Doc owner = dc.owner
            if(owner.creator.authorizedAffiliations.size() > 0) {
                owner.owner = owner.creator.authorizedAffiliations[0].org
                switch(owner.creator.id) {
                    case 99: owner.owner = Org.get(1)
                        break
                }
            }
            else {
                switch(owner.creator.id) {
                    case 9: owner.owner = Org.get(1)
                        break
                    default: log.debug(owner.creator.display)
                        break
                }
            }
            owner.save()
            dc.shareConf = RDStore.SHARE_CONF_UPLOADER_ORG
            dc.save()
        }
    }
}
