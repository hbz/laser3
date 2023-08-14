package de.laser

import de.laser.annotations.RefdataInfo
import de.laser.config.ConfigDefaults
import de.laser.config.ConfigMapper
import de.laser.storage.RDConstants
import org.apache.http.HttpStatus

/**
 * A document object representation. The document may be an uploaded file with annotations or a note without file. As legacy, automatised messages / announcements were stored as docs as well. This type is specified by the
 * {@link #contentType} (content has to be understood in terms of technical content, not the intellectual content). This latter is specified by {@link #type} controlled list, see the reference data category {@link RDConstants#DOCUMENT_TYPE} for that.
 * Moreover, a document has an owner {@link Org} which may restrict its visiblity to other {@link Org}s. See {@link DocContext} for the visibility and sharing handling
 */
class Doc {

    static final CONTENT_TYPE_STRING              = 0
    static final CONTENT_TYPE_FILE                = 3

    @RefdataInfo(cat = RDConstants.DOCUMENT_TYPE)
    RefdataValue type

    @RefdataInfo(cat = RDConstants.DOCUMENT_CONFIDENTIALITY)
    RefdataValue confidentiality

  String title
  String filename
  String mimeType
  Integer contentType = CONTENT_TYPE_STRING
  String content
  String uuid 
  Date dateCreated
  Date lastUpdated
  Org owner         //the context org of the user uploading a document
  String server
  String migrated

  static mapping = {
                id column:'doc_id'
           version column:'doc_version'
              type column:'doc_type_rv_fk', index:'doc_type_idx'
   confidentiality column:'doc_confidentiality_rv_fk'
       contentType column:'doc_content_type'
              uuid column:'doc_docstore_uuid', index:'doc_uuid_idx'
             title column:'doc_title'
          filename column:'doc_filename'
          migrated column:'doc_migrated'
           content column:'doc_content', type:'text'
          mimeType column:'doc_mime_type'
             owner column:'doc_owner_fk'
            server column:'doc_server'
       dateCreated column:'doc_date_created'
       lastUpdated column:'doc_last_updated'
  }

  static constraints = {
            type      (nullable:true)
      confidentiality (nullable:true)
    content   (nullable:true, blank:false)
    uuid      (nullable:true, blank:false)
    contentType(nullable:true)
    title     (nullable:true, blank:false)
    filename  (nullable:true, blank:false)
    mimeType  (nullable:true, blank:false)
    owner     (nullable:true)
    server    (nullable:true, blank:false)
    migrated  (nullable:true, blank:false, maxSize:1)
  }

    /**
     * Delivers the document for download
     * @param response the response output stream to flush the document out into
     * @param filename the name of file to retrieve
     */
    def render(def response, String filename) {
        byte[] output = []
        try {
            String fPath = ConfigMapper.getDocumentStorageLocation() ?: ConfigDefaults.DOCSTORE_LOCATION_FALLBACK
            File file = new File("${fPath}/${uuid}")
            output = file.getBytes()

            response.setContentType(mimeType)
            response.addHeader("Content-Disposition", "attachment; filename=\"${filename}\"")
            response.setHeader('Content-Length', "${output.length}")

            response.outputStream << output
        } catch(Exception e) {
            log.error(e.getMessage())

            response.sendError(HttpStatus.SC_NOT_FOUND)
        }
    }

    /**
     * used where? And should this not be a method actually?
     */
    def beforeInsert = {
        if (contentType == CONTENT_TYPE_FILE) {
            uuid = java.util.UUID.randomUUID().toString()
            log.info('generating new uuid: '+ uuid)
        }
    }

    /**
     * Gets a map of MIME types and their respective encoding (encode or raw output)
     * @return a {@link Map} of MIME types and their output way
     */
    static Map<String, String> getPreviewMimeTypes() {
        String raw      = 'raw'
        String encode   = 'encode'

        return [
                'application/json'          : encode,
                'application/pdf'       : raw,
                //nope 'application/rtf'   : encode,
                'application/xml'           : encode,
                'application/x-javascript'  : encode,
                'image/gif'             : raw,
                'image/jpeg'            : raw,
                'image/png'             : raw,
                'image/svg+xml'             : encode,
                'text/calendar'             : encode,
                'text/csv'                  : encode,
                'text/html'                 : encode,
                'text/javascript'           : encode,
                'text/markdown'             : encode,
                'text/plain'                : encode,
                //nope 'text/richtext'     : encode,
                //nope 'text/rtf'          : encode,
                'text/tab-separated-values' : encode,
                'text/xml'                  : encode
        ]
    }
}
