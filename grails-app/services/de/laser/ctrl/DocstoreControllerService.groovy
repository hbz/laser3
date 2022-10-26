package de.laser.ctrl

import de.laser.*
import de.laser.storage.RDConstants
import de.laser.utils.CodeUtils
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

/**
 * This contains the service methods for the {@link DocstoreController}
 */
@Transactional
class DocstoreControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService

    /**
     * Edits a document with the given parameter map. The editing is done if the attached object could be retrieved. The document context is given by params.docctx
     * @param params the parameter {@link Map}
     * @return a {@link Map} containing the edtiting result status
     */
    //TODO [ticket=2393] this is a process which should be deployed onto the DMS
    Map<String,Object> editDocument(GrailsParameterMap params) {
        Class dc = CodeUtils.getDomainClass( params.ownerclass )
        if (dc) {
            def instance = dc.get(params.ownerid)
            if (instance) {
                log.debug("Got owner instance ${instance}")

                DocContext doc_context = DocContext.get(params.docctx)

                Doc doc_content = doc_context.owner
                doc_content.title = params.upload_title ?: doc_content.filename
                doc_content.confidentiality = params.confidentiality ? RefdataValue.getByValueAndCategory(params.confidentiality, RDConstants.DOCUMENT_CONFIDENTIALITY) : null
                doc_content.type = RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                doc_content.owner = contextService.getOrg()
                doc_content.save()

                doc_context.doctype = RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                if(params.targetOrg)
                    doc_context.targetOrg = Org.get(params.targetOrg)
                doc_context.shareConf = RefdataValue.get(params.shareConf) ?: null
                doc_context.save()

                log.debug("Doc updated and new doc context updated on ${params.ownertp} for ${params.ownerid}")
                [result:null,status:STATUS_OK]
            }
            else {
                log.error("Unable to locate document owner instance for class ${params.ownerclass}:${params.ownerid}")
                [result:null,status:STATUS_ERROR]
            }
        }
        else {
            log.warn("Unable to locate domain class when processing generic doc upload. ownerclass was ${params.ownerclass}")
            [result:null,status:STATUS_ERROR]
        }
    }

}
