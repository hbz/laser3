package de.laser

import de.laser.config.ConfigDefaults
import de.laser.interfaces.ShareSupport
import de.laser.storage.RDStore
import de.laser.utils.AppUtils
import de.laser.utils.CodeUtils
import de.laser.config.ConfigMapper
import de.laser.annotations.DebugInfo
import de.laser.storage.RDConstants
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

    AccessService accessService
    ContextService contextService
    CryptoService cryptoService
    MessageSource messageSource

    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def index() {
        redirect(action: 'downloadDocument', params: params)
//        response.sendError(HttpStatus.SC_FORBIDDEN)
    }

    /**
     * Called by /documents/_table and /documents/_card
     * Retrieves a document by its uuid
     * @return the document, null otherwise
     */
    @DebugInfo(isInstUser = [])
    @Secured(closure = {
        ctx.contextService.isInstUser()
    })
    def downloadDocument() {
        Doc doc = Doc.findByUuidAndContentType(params.id, Doc.CONTENT_TYPE_FILE)
        if (doc) {
            boolean check = false

            DocContext.findAllByOwner(doc).each{dctx -> check = check || accessService.hasAccessToDocument(dctx, AccessService.READ) }
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
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC], withTransaction = 1)
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def uploadDocument() {
        log.debug('upload document ..')

        def uploadFile = request.getFile('upload_file')
        if (uploadFile.size == 0) {
            flash.error = message(code: 'template.emptyDocument.file') as String
            redirect(url: request.getHeader('referer'))
            return
        }

        Doc.withTransaction { TransactionStatus ts ->
            String ownerClass = params.ownerclass
            String ownerId    = params.ownerid
            String ownerTp    = params.ownertp

            Class dc = CodeUtils.getDomainClass(ownerClass)
            if (dc) {
                def instance = dc.get(ownerId)
                if (instance) {
                    log.debug("Got owner instance ${instance}")

                    if (uploadFile.inputStream) {
                        Doc doc = new Doc(
                                contentType:        Doc.CONTENT_TYPE_FILE,
                                confidentiality:    params.confidentiality ? RefdataValue.getByValueAndCategory(params.confidentiality, RDConstants.DOCUMENT_CONFIDENTIALITY) : null,
                                filename:   uploadFile.originalFilename,
                                mimeType:   uploadFile.contentType,
                                title:      params.upload_title ?: uploadFile.originalFilename,
                                type:       RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE),
                                creator:    contextService.getUser(),
                                owner:      contextService.getOrg(),
                                server:     AppUtils.getCurrentServer(),
                                ckey:       cryptoService.generateCKey()
                        )
                        doc.save()

                        //move the uploaded file to its actual destination (= store the file)
                        File targetFile
                        try {
                            String tfPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
                            String tfName = doc.uuid

                            File folder = new File("${tfPath}")
                            if (!folder.exists()) {
                                folder.mkdirs()
                            }
                            targetFile = new File("${tfPath}/${tfName}")
                            uploadFile.transferTo(targetFile)

                            cryptoService.encryptRawFile(targetFile, doc)
                        }
                        catch (Exception e) {
                            e.printStackTrace()
                        }

                        //link the document to the target object
                        DocContext docctx = new DocContext(
                                "${ownerTp}": instance,
                                owner: doc
                        )
                        //set sharing settings (counts iff document is linked to an Org, are null otherwise)
                        docctx.shareConf = RefdataValue.get(params.shareConf) ?: null
                        docctx.targetOrg = params.targetOrg ? Org.get(params.targetOrg) : null
                        //set sharing setting for license or subscription documents
                        if(params.setSharing) {
                            docctx.isShared = true
                        }
                        docctx.save()
                        if(docctx.isShared) {
                            ((ShareSupport) instance).updateShare(docctx)
                        }

                        log.debug('FileUpload: Created doc and docContext -> #' + doc.id + ', #' + docctx.id)
                    }
                    else {
                        log.error('FileUpload: Unable to open inputStream -> ' + uploadFile.originalFilename)
                    }
                }
                else {
                    log.error('FileUpload: Document owner not found -> ' + ownerClass + ':' + ownerId)
                }
            }
            else {
                log.warn('FileUpload: Domain class (owner) not found -> ' + ownerClass)
            }
        }
        redirect(url: request.getHeader('referer'))
    }

    /**
     * Call for editing an existing document. Redirects back to the referer where result may be shown in case of an error
     */
    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def editDocument() {
        log.debug("editDocument: ${params}")

        DocContext docctx = DocContext.get(params.docctx)
        if (accessService.hasAccessToDocument(docctx, AccessService.WRITE)) {
            // moved from DocumentControllerService.editDocument()

            Class dc = CodeUtils.getDomainClass(params.ownerclass)
            if (dc) {

                def instance = dc.get(params.ownerid)
                if (instance) {
                    Doc doc = docctx.owner

                    doc.title = params.upload_title ?: doc.filename
                    doc.confidentiality = params.confidentiality ? RefdataValue.getByValueAndCategory(params.confidentiality, RDConstants.DOCUMENT_CONFIDENTIALITY) : null
                    doc.type = params.doctype ? RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE) : null
                    doc.owner = contextService.getOrg()
                    doc.save()

                    // 4644 docctx.doctype = params.doctype ? RefdataValue.getByValueAndCategory(params.doctype, RDConstants.DOCUMENT_TYPE) : null
                    if(params.targetOrg)
                        docctx.targetOrg = Org.get(params.targetOrg)
                    docctx.shareConf = RefdataValue.get(params.shareConf) ?: null
                    docctx.save()

                    log.debug("Doc updated and new doc context updated on ${params.ownertp} for ${params.ownerid}")
                }
                else {
                    flash.error = message(code:'template.documents.edit.error')
                    log.error("Unable to locate document owner instance for class ${params.ownerclass}:${params.ownerid}")
                }
            }
            else {
                flash.error = message(code:'template.documents.edit.error')
                log.warn("Unable to locate domain class when processing generic doc upload. ownerclass was ${params.ownerclass}")
            }
        }
        else {
            flash.error = message(code: 'default.noPermissions')
        }
        redirect(url: request.getHeader('referer'))
    }

    @DebugInfo(isInstEditor = [CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC])
    @Secured(closure = {
        ctx.contextService.isInstEditor(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)
    })
    def deleteDocument() {
        log.debug("deleteDocument: ${params}")

        if (params.deleteId) {
            DocContext docctx = DocContext.get(params.deleteId)

            if (accessService.hasAccessToDocument(docctx, AccessService.WRITE)) {
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
        }
        else {
            redirect controller: params.redirectController, action: params.redirectAction, id: params.instanceId
        }
    }
}
