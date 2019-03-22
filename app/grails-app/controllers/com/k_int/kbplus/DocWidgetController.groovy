package com.k_int.kbplus

import de.laser.controller.AbstractDebugController
import de.laser.helper.DebugAnnotation
import grails.plugin.springsecurity.annotation.Secured
import com.k_int.kbplus.auth.*;

@Secured(['IS_AUTHENTICATED_FULLY'])
class DocWidgetController extends AbstractDebugController {

  def springSecurityService
  def genericOIDService
  def docstoreService
  def contextService

  @Secured(['ROLE_USER'])
  def createNote() { 
    log.debug("Create note referer was ${request.getHeader('referer')} or ${request.request.RequestURL}");
    def user = User.get(springSecurityService.principal.id)
    def domain_class = grailsApplication.getArtefact('Domain', params.ownerclass)

    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.ownerid)
      if ( instance ) {
        log.debug("Got owner instance ${instance}");

        def doc_content = new Doc(contentType:Doc.CONTENT_TYPE_STRING,
                                  title:    params.licenseNoteTitle,
                                  content:  params.licenseNote,
                                  type:     RefdataValue.getByValueAndCategory('Note', 'Document Type'),
                                  user:     user).save()


        log.debug("Setting new context type to ${params.ownertp}..");
        def doc_context = new DocContext("${params.ownertp}":instance,
                                         owner:doc_content,
                                         doctype:RefdataCategory.lookupOrCreate('Document Type',params.doctype)).save(flush:true);
      }
      else {
        log.debug("no instance");
      }
    }
    else {
      log.debug("no type");
    }

    redirect(url: request.getHeader('referer'))
    // redirect(url: request.request.RequestURL)
    // request.request.RequestURL
    // request.getHeader('referer') 
  }

  @Secured(['ROLE_USER'])
  def uploadDocument() {
    log.debug("upload document....");

    def input_file = request.getFile("upload_file")
    if(input_file.size == 0)
        {
            flash.error = message(code: 'template.emptyDocument.file')
            redirect(url: request.getHeader('referer'))
            return
        }
    def input_stream = input_file?.inputStream
    def original_filename = request.getFile("upload_file")?.originalFilename

    def user = User.get(springSecurityService.principal.id)
    def domain_class=grailsApplication.getArtefact('Domain',params.ownerclass)

    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.ownerid)
      if ( instance ) {
        log.debug("Got owner instance ${instance}");

        if ( input_stream ) {
          // def docstore_uuid = docstoreService.uploadStream(input_stream, original_filename, params.upload_title)
          // log.debug("Docstore uuid is ${docstore_uuid}");
    
          Doc doc_content = new Doc(contentType:Doc.CONTENT_TYPE_BLOB,
                                    filename: original_filename,
                                    mimeType: request.getFile("upload_file")?.contentType,
                                    title: params.upload_title,
                                    type:RefdataCategory.lookupOrCreate('Document Type',params.doctype),
                                    creator: user)
          if(instance instanceof Org)
            doc_content.owner = contextService.org
          // erms-790
          //doc_content.setBlobData(input_stream, input_file.size)
          doc_content.save()

          try {
              def fPath = grailsApplication.config.documentStorageLocation ?: '/tmp/laser'
              def fName = doc_content.uuid

              File folder = new File("${fPath}")
              if (!folder.exists()) {
                  folder.mkdirs()
              }
              input_file.transferTo(new File("${fPath}/${fName}"))
          }
          catch(Exception e) {
              // fallback
              doc_content.setBlobData(input_stream, input_file.size)
              doc_content.save()
          }

          DocContext doc_context = new DocContext(
                  "${params.ownertp}": instance,
                  owner:               doc_content,
                  doctype:             RefdataCategory.lookupOrCreate('Document Type',params.doctype)
          )
          if(instance instanceof Org) {
            doc_context.shareConf = genericOIDService.resolveOID(params.shareConf)
          }
          doc_context.save(flush:true)

          log.debug("Doc created and new doc context set on ${params.ownertp} for ${params.ownerid}");
        }
        
      }
      else {
        log.error("Unable to locate document owner instance for class ${params.ownerclass}:${params.ownerid}");
      }
    }
    else {
      log.warn("Unable to locate domain class when processing generic doc upload. ownerclass was ${params.ownerclass}");
    }

    redirect(url: request.getHeader('referer'))
  }

  @DebugAnnotation(test='hasAffiliation("INST_EDITOR")')
  @Secured(closure = { ctx.springSecurityService.getCurrentUser()?.hasAffiliation("INST_EDITOR") })
  def editDocument() {
    log.debug("edit document....");

    def domain_class=grailsApplication.getArtefact('Domain',params.ownerclass)

    if ( domain_class ) {
      def instance = domain_class.getClazz().get(params.ownerid)
      if ( instance ) {
        log.debug("Got owner instance ${instance}");
        DocContext doc_context = DocContext.get(params.docctx)
        Doc doc_content = doc_context.owner
        doc_content.title = params.upload_title
        doc_content.type = RefdataValue.getByValueAndCategory(params.doctype,'Document Type')
        if(instance instanceof Org)
          doc_content.owner = instance
        doc_content.save()
        doc_context.doctype = RefdataValue.getByValueAndCategory(params.doctype,'Document Type')
        if(instance instanceof Org) {
          doc_context.shareConf = genericOIDService.resolveOID(params.shareConf)
          doc_context.org = params.targetOrg ? Org.get(Long.parseLong(params.targetOrg)) : null
        }
        doc_context.save(flush:true)
        log.debug("Doc updated and new doc context updated on ${params.ownertp} for ${params.ownerid}");
      }
      else {
        log.error("Unable to locate document owner instance for class ${params.ownerclass}:${params.ownerid}");
      }
    }
    else {
      log.warn("Unable to locate domain class when processing generic doc upload. ownerclass was ${params.ownerclass}");
    }

    redirect(url: request.getHeader('referer'))
  }
}
