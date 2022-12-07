package de.laser


import de.laser.auth.User
import de.laser.ctrl.DocstoreControllerService
import de.laser.config.ConfigDefaults
import de.laser.interfaces.ShareSupport
import de.laser.utils.CodeUtils
import de.laser.config.ConfigMapper
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.survey.SurveyConfig
import de.laser.utils.LocaleUtils
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

/**
 * This is the actual controller to handle uploaded documents. Handling notes is done in {@link DocController}
 * @see DocContext
 * @see Doc
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DocstoreController  {

    ContextService contextService
    DocstoreControllerService docstoreControllerService
    MessageSource messageSource

    /**
     * Called by /documents/_table and /documents/_card
     * Retrieves a document by its uuid
     * @return the document, null otherwise
     */
    @Secured(['ROLE_USER'])
    def index() {
        Doc doc = Doc.findByUuid(params.id)

        if (doc) {
            String filename = doc.filename ?: messageSource.getMessage('template.documents.missing', null, LocaleUtils.getCurrentLocale())

            switch (doc.contentType) {
                case Doc.CONTENT_TYPE_STRING:
                    break
                case Doc.CONTENT_TYPE_FILE:
                    doc.render(response, filename)
                    break
            }
        }
    }

    /**
     * Uploads a new document, specified by the upload form parameters, and sets the entered metadata to the new {@link DocContext} object
     */
    @DebugInfo(wtc = DebugInfo.WITH_TRANSACTION)
    @Secured(['ROLE_USER'])
    def uploadDocument() {
        Doc.withTransaction { TransactionStatus ts ->
            log.debug("upload document....");

            //process file
            def input_file = request.getFile("upload_file")
            if (input_file.size == 0) {
                flash.error = message(code: 'template.emptyDocument.file') as String
                redirect(url: request.getHeader('referer'))
                return
            }
            //get input stream of file object
            def input_stream = input_file?.inputStream
            String original_filename = request.getFile("upload_file")?.originalFilename

            //retrieve uploading user and owner class
            User user = contextService.getUser()
            Class dc = CodeUtils.getDomainClass( params.ownerclass )

            if (dc) {
                def instance = dc.get(params.ownerid)
                if (instance) {
                    log.debug("Got owner instance ${instance}")

                    if (input_stream) {
                        // def docstore_uuid = docstoreService.uploadStream(input_stream, original_filename, params.upload_title)
                        // log.debug("Docstore uuid is ${docstore_uuid}");

                        //create new document
                        //TODO [ticket=2393] this must be replaced by the new entry in the document management system
                        Doc doc_content = new Doc(
                                contentType: Doc.CONTENT_TYPE_FILE,
                                confidentiality: params.confidentiality ? RefdataValue.getByValueAndCategory(params.confidentiality, RDConstants.DOCUMENT_CONFIDENTIALITY) : null,
                                filename: original_filename,
                                mimeType: request.getFile("upload_file")?.contentType,
                                title: params.upload_title ?: original_filename,
                                type: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE),
                                creator: user,
                                owner: contextService.getOrg())

                        doc_content.save()

                        //move the uploaded file to its actual destination (= store the file)
                        File new_File
                        try {
                            String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                            String fName = doc_content.uuid

                            File folder = new File("${fPath}")
                            if (!folder.exists()) {
                                folder.mkdirs()
                            }
                            new_File = new File("${fPath}/${fName}")

                            input_file.transferTo(new_File)
                        }
                        catch (Exception e) {
                            e.printStackTrace()
                        }

                        //link the document to the target object
                        DocContext doc_context = new DocContext(
                                "${params.ownertp}": instance,
                                owner: doc_content
                                // 4644 doctype: RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE)
                        )
                        //set sharing settings (counts iff document is linked to an Org, are null otherwise)
                        doc_context.shareConf = RefdataValue.get(params.shareConf) ?: null
                        doc_context.targetOrg = params.targetOrg ? Org.get(params.targetOrg) : null
                        //set sharing setting for license or subscription documents
                        if(params.setSharing) {
                            doc_context.isShared = true
                        }
                        doc_context.save()
                        if(doc_context.isShared) {
                            ((ShareSupport) instance).updateShare(doc_context)
                        }

                        //attach document to all survey participants (= SurveyConfigs)
                        //docForAllSurveyConfigs
                        if (instance instanceof SurveyConfig && params.docForAllSurveyConfigs) {
                            instance.surveyInfo.surveyConfigs.each { config ->

                                if (instance != config) {

                                    //create document objects for each file
                                    Doc doc_content2 = new Doc(
                                            contentType: Doc.CONTENT_TYPE_FILE,
                                            confidentiality: doc_content.confidentiality, // todo ?
                                            filename: doc_content.filename,
                                            mimeType: doc_content.mimeType,
                                            title: doc_content.title,
                                            type: doc_content.type,
                                            owner: contextService.getOrg())

                                    doc_content2.save()

                                    log.debug( doc_content2.toString() )

                                    //store copies of the uploaded document files
                                    try {
                                        String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                                        String fName = doc_content2.uuid

                                        File folder = new File("${fPath}")
                                        if (!folder.exists()) {
                                            folder.mkdirs()
                                        }
                                        File dst = new File("${fPath}/${fName}")
                                        dst << new_File.text
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace()
                                    }

                                    //create attachment of each document to the survey config
                                    DocContext doc_context2 = new DocContext(
                                            "${params.ownertp}": config,
                                            owner: doc_content2
                                            // 4644 doctype: doc_context.doctype
                                    )
                                    //doc_context2.shareConf = genericOIDService.resolveOID(params.shareConf)
                                    doc_context2.save()
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
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call for editing an existing document, see {@link DocstoreControllerService#editDocument()} for the editing implementation. Redirects back to the referer where result may be shown in case of an error
     */
    @DebugInfo(test = 'hasAffiliation("INST_EDITOR")',ctrlService = DebugInfo.WITH_TRANSACTION)
    @Secured(closure = { ctx.contextService.getUser()?.hasAffiliation("INST_EDITOR") })
    def editDocument() {
        Map<String,Object> ctrlResult = docstoreControllerService.editDocument(params)
        if(ctrlResult.status == DocstoreControllerService.STATUS_ERROR) {
            flash.error = message(code:'template.documents.edit.error') as String
        }
        redirect(url: request.getHeader('referer'))
    }
}
