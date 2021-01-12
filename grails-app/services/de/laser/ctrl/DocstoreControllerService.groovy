package de.laser.ctrl

import com.k_int.kbplus.GenericOIDService
import de.laser.*
import de.laser.helper.AppUtils
import de.laser.helper.RDConstants
import grails.core.GrailsClass
import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class DocstoreControllerService {

    static final int STATUS_OK = 0
    static final int STATUS_ERROR = 1

    ContextService contextService
    GenericOIDService genericOIDService

    Map<String,Object> editDocument(GrailsParameterMap params) {
        GrailsClass domain_class = AppUtils.getDomainClass( params.ownerclass )
        if (domain_class) {
            def instance = domain_class.getClazz().get(params.ownerid)
            if (instance) {
                log.debug("Got owner instance ${instance}")

                DocContext doc_context = DocContext.get(params.docctx)
                Doc doc_content = doc_context.owner
                doc_content.title = params.upload_title ?: doc_content.filename
                doc_content.type = RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                doc_content.owner = contextService.getOrg()
                doc_content.save()
                doc_context.doctype = RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                if(params.targetOrg)
                    doc_context.targetOrg = Org.get(params.targetOrg)
                doc_context.shareConf = genericOIDService.resolveOID(params.shareConf)
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
