package de.laser


import de.laser.auth.User
import de.laser.ctrl.DocumentControllerService
import de.laser.config.ConfigDefaults
import de.laser.interfaces.ShareSupport
import de.laser.storage.RDStore
import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import de.laser.config.ConfigMapper
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
import de.laser.survey.SurveyConfig
import de.laser.utils.LocaleUtils
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.HttpStatus
import org.springframework.context.MessageSource
import org.springframework.transaction.TransactionStatus

/**
 * This is the actual controller to handle uploaded documents. Handling notes is done in {@link NoteController}
 * @see DocContext
 * @see Doc
 */
@Secured(['IS_AUTHENTICATED_FULLY'])
class DocumentController {

    ContextService contextService
    DocumentControllerService documentControllerService
    MessageSource messageSource
    AccessService accessService

    @Secured(['ROLE_USER'])
    def index() {
        redirect(action: 'downloadDocument', params: params)
//        response.sendError(HttpStatus.SC_FORBIDDEN)
    }

    /**
     * Called by /documents/_table and /documents/_card
     * Retrieves a document by its uuid
     * @return the document, null otherwise
     */
    @DebugInfo(isInstUser_or_ROLEADMIN = [])
    @Secured(closure = {
        ctx.contextService.isInstUser_or_ROLEADMIN()
    })
    def downloadDocument() {
        Doc doc = Doc.findByUuidAndContentType(params.id, Doc.CONTENT_TYPE_FILE)
        if (doc) {
            boolean check = false

            DocContext.findAllByOwner(doc).each{dctx -> check = check || accessService.hasAccessToDocument(dctx) }  // TODO
            if (check) {
                String filename = doc.filename ?: messageSource.getMessage('template.documents.missing', null, LocaleUtils.getCurrentLocale())
                doc.render(response, filename)
            }
            else {
                response.sendError(HttpStatus.SC_FORBIDDEN)
            }
        }
        else {
            response.sendError(HttpStatus.SC_NOT_FOUND)
        }
    }

    /**
     * Uploads a new document, specified by the upload form parameters, and sets the entered metadata to the new {@link DocContext} object
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def uploadDocument() {
        log.debug('upload document ..')

        //process file
        def input_file = request.getFile("upload_file")
        if (input_file.size == 0) {
            flash.error = message(code: 'template.emptyDocument.file') as String
            redirect(url: request.getHeader('referer'))
            return
        }

        Doc.withTransaction { TransactionStatus ts ->
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
                                owner: contextService.getOrg(),
                                server: AppUtils.getCurrentServer()
                        )

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
                                            owner: contextService.getOrg(),
                                            server: AppUtils.getCurrentServer()
                                    )

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
     * Call for editing an existing document, see {@link DocumentControllerService#editDocument()} for the editing implementation. Redirects back to the referer where result may be shown in case of an error
     */
    @DebugInfo(isInstEditor_or_ROLEADMIN = [], ctrlService = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def editDocument() {
        Map<String,Object> ctrlResult = documentControllerService.editDocument(params)
        if(ctrlResult.status == DocumentControllerService.STATUS_ERROR) {
            flash.error = message(code:'template.documents.edit.error') as String
        }
        redirect(url: request.getHeader('referer'))
    }

    @Secured(closure = {
        ctx.contextService.isInstEditor_or_ROLEADMIN()
    })
    def deleteDocument() {
        log.debug("deleteDocument: ${params}")

        if (params.deleteId) {
            DocContext docctx = DocContext.get(params.deleteId)

            if (accessService.hasAccessToDocument(docctx)) {
                docctx.status = RDStore.DOC_CTX_STATUS_DELETED
                docctx.save()
                flash.message = message(code: 'default.deleted.general.message')
            }
            else {
                flash.error = message(code: 'default.noPermissions')
            }
        }
        if (params.redirectTab) {
            redirect controller: params.redirectController, action: params.redirectAction, id: params.instanceId, params: [tab: params.redirectTab] // subscription.membersSubscriptionsManagement
        } else {
            redirect controller: params.redirectController, action: params.redirectAction, id: params.instanceId
        }
    }
}
