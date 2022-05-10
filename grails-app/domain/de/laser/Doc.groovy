package de.laser


import de.laser.helper.ConfigMapper
import de.laser.storage.RDConstants
import de.laser.annotations.RefdataInfo

/**
 * A document object representation. The document may be an uploaded file with annotations or a note without file. As legacy, automatised messages / announcements were stored as docs as well. This type is specified by the
 * {@link #contentType} (content has to be understood in terms of technical content, not the intellectual content). This latter is specified by {@link #type} controlled list, see the reference data category {@link RDConstants#DOCUMENT_TYPE} for that.
 * Moreover, a document has an owner {@link Org} which may restrict its visiblity to other {@link Org}s. See {@link DocContext} for the visibility and sharing handling
 */
class Doc {

    static final CONTENT_TYPE_STRING              = 0
    @Deprecated
    static final CONTENT_TYPE_UPDATE_NOTIFICATION = 2
    static final CONTENT_TYPE_FILE                = 3

    @RefdataInfo(cat = RDConstants.DOCUMENT_TYPE)
    RefdataValue type

  String title
  String filename
  String mimeType
  Integer contentType = CONTENT_TYPE_STRING
  String content
  String uuid 
  Date dateCreated
  Date lastUpdated
  Org owner         //the context org of the user uploading a document
  String migrated

  static mapping = {
                id column:'doc_id'
           version column:'doc_version'
              type column:'doc_type_rv_fk', index:'doc_type_idx'
       contentType column:'doc_content_type'
              uuid column:'doc_docstore_uuid', index:'doc_uuid_idx'
             title column:'doc_title'
          filename column:'doc_filename'
           content column:'doc_content', type:'text'
          mimeType column:'doc_mime_type'
             owner column:'doc_owner_fk'
  }

  static constraints = {
    type      (nullable:true)
    content   (nullable:true, blank:false)
    uuid      (nullable:true, blank:false)
    contentType(nullable:true)
    title     (nullable:true, blank:false)
    filename  (nullable:true, blank:false)
    mimeType  (nullable:true, blank:false)
    owner     (nullable:true)
    migrated  (nullable:true, blank:false, maxSize:1)
  }

    /**
     * Delivers the document for download
     * @param response the response output stream to flush the document out into
     * @param filename the name of file to retrieve
     */
    def render(def response, def filename) {
        // erms-790
        def output
        def contentLength

        try {
            String fPath = ConfigMapper.getDocumentStorageLocation() ?: '/tmp/laser'
            File file = new File("${fPath}/${uuid}")
            output = file.getBytes()
            contentLength = output.length
        } catch(Exception e) {
            log.error(e)
        }

        response.setContentType(mimeType)
        response.addHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
        response.setHeader('Content-Length', "${contentLength}")

        response.outputStream << output
    }

    // erms-790
    def beforeInsert = {
        if (contentType == CONTENT_TYPE_FILE) {
            uuid = java.util.UUID.randomUUID().toString()
            log.info('generating new uuid: '+ uuid)
        }
    }
}
