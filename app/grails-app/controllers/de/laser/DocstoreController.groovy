package de.laser


import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.User
import de.laser.controller.AbstractDebugController
import de.laser.helper.AppUtils
import de.laser.helper.ConfigUtils
import de.laser.helper.DebugAnnotation
import de.laser.helper.RDConstants
import grails.plugin.springsecurity.annotation.Secured
import org.codehaus.groovy.grails.commons.GrailsClass
import org.springframework.context.i18n.LocaleContextHolder

@Secured(['IS_AUTHENTICATED_FULLY'])
class DocstoreController extends AbstractDebugController {

    def springSecurityService
    def genericOIDService
    def docstoreService
    def contextService

    @Secured(['ROLE_USER'])
    def index() {
        Doc doc = Doc.findByUuid(params.id)

        if (doc) {
            String filename = doc.filename ?: messageSource.getMessage(
                    'template.documents.missing',
                    null,
                    LocaleContextHolder.getLocale()
            )

            switch (doc.contentType) {
                case Doc.CONTENT_TYPE_STRING:
                    break
            //case Doc.CONTENT_TYPE_DOCSTORE:
            //    docstoreService.retrieve(params.id, response, doc.mimeType, filename)
            //    break
                case Doc.CONTENT_TYPE_FILE:
                    doc.render(response, filename)
                    break
            }
        }
    }

    @Secured(['ROLE_USER'])
    def uploadDocument() {
        log.debug("upload document....");

        def input_file = request.getFile("upload_file")
        if (input_file.size == 0) {
            flash.error = message(code: 'template.emptyDocument.file')
            redirect(url: request.getHeader('referer'))
            return
        }
        def input_stream = input_file?.inputStream
        String original_filename = request.getFile("upload_file")?.originalFilename

        User user = User.get(springSecurityService.principal.id)
        GrailsClass domain_class = AppUtils.getDomainClass( params.ownerclass )

        if (domain_class) {
            def instance = domain_class.getClazz().get(params.ownerid)
            if (instance) {
                log.debug("Got owner instance ${instance}")

                if (input_stream) {
                    // def docstore_uuid = docstoreService.uploadStream(input_stream, original_filename, params.upload_title)
                    // log.debug("Docstore uuid is ${docstore_uuid}");

                    Doc doc_content = new Doc(
                            contentType: Doc.CONTENT_TYPE_FILE,
                            filename: original_filename,
                            mimeType: request.getFile("upload_file")?.contentType,
                            title: params.upload_title ?: original_filename,
                            type: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE),
                            creator: user,
                            owner: contextService.getOrg())

                    doc_content.save(flush:true)

                    File new_File
                    try {
                        String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'
                        String fName = doc_content.uuid

                        File folder = new File("${fPath}")
                        if (!folder.exists()) {
                            folder.mkdirs()
                        }
                        new_File = new File("${fPath}/${fName}")

                        input_file.transferTo(new_File)
                    }
                    catch (Exception e) {
                        log.error(e)
                    }

                    DocContext doc_context = new DocContext(
                            "${params.ownertp}": instance,
                            owner: doc_content,
                            doctype: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                    )
                    doc_context.shareConf = genericOIDService.resolveOID(params.shareConf) ?: null
                    doc_context.targetOrg = params.targetOrg ? Org.get(params.targetOrg) : null

                    doc_context.save(flush: true)

                    //docForAllSurveyConfigs
                    if (instance instanceof SurveyConfig && params.docForAllSurveyConfigs) {
                        instance.surveyInfo.surveyConfigs.each { config ->

                            if (instance != config) {

                                Doc doc_content2 = new Doc(
                                        contentType: Doc.CONTENT_TYPE_FILE,
                                        filename: doc_content.filename,
                                        mimeType: doc_content.mimeType,
                                        title: doc_content.title,
                                        type: doc_content.type,
                                        owner: contextService.getOrg())

                                doc_content2.save(flush:true)

                                log.debug( doc_content2.toString() )

                                try {
                                    String fPath = ConfigUtils.getDocumentStorageLocation() ?: '/tmp/laser'
                                    String fName = doc_content2.uuid

                                    File folder = new File("${fPath}")
                                    if (!folder.exists()) {
                                        folder.mkdirs()
                                    }
                                    File dst = new File("${fPath}/${fName}")
                                    dst << new_File.text
                                }
                                catch (Exception e) {
                                    log.error(e)
                                }

                                DocContext doc_context2 = new DocContext(
                                        "${params.ownertp}": config,
                                        owner: doc_content2,
                                        doctype: doc_context.doctype
                                )
                                //doc_context2.shareConf = genericOIDService.resolveOID(params.shareConf)
                                doc_context2.save(flush: true)
                            }
                        }
                    }
                    log.debug("Doc created and new doc context set on ${params.ownertp} for ${params.ownerid}")
                }
            }
            else {
                log.error("Unable to locate document owner instance for class ${params.ownerclass}:${params.ownerid}")
            }
        }
        else {
            log.warn("Unable to locate domain class when processing generic doc upload. ownerclass was ${params.ownerclass}")
        }

        redirect(url: request.getHeader('referer'))
    }

    @DebugAnnotation(test = 'hasAffiliation("INST_EDITOR")')
    @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        log.debug("edit document....")

        GrailsClass domain_class = AppUtils.getDomainClass( params.ownerclass )

        if (domain_class) {
            def instance = domain_class.getClazz().get(params.ownerid)
            if (instance) {
                log.debug("Got owner instance ${instance}")

                DocContext doc_context = DocContext.get(params.docctx)
                Doc doc_content = doc_context.owner
                doc_content.title = params.upload_title ?: doc_content.filename
                doc_content.type = RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                doc_content.owner = contextService.org
                doc_content.save(flush:true)
                doc_context.doctype = RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                if(params.targetOrg)
                    doc_context.targetOrg = Org.get(params.targetOrg)
                doc_context.shareConf = genericOIDService.resolveOID(params.shareConf)
                doc_context.save(flush: true)

                log.debug("Doc updated and new doc context updated on ${params.ownertp} for ${params.ownerid}")
            }
            else {
                log.error("Unable to locate document owner instance for class ${params.ownerclass}:${params.ownerid}")
            }
        }
        else {
            log.warn("Unable to locate domain class when processing generic doc upload. ownerclass was ${params.ownerclass}")
        }

        redirect(url: request.getHeader('referer'))
    }
}
