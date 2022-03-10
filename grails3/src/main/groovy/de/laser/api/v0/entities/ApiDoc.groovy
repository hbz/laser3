package de.laser.api.v0.entities

import de.laser.Doc
import de.laser.DocContext
import de.laser.License
import de.laser.Org
import de.laser.api.v0.*
import de.laser.helper.Constants
import de.laser.helper.RDStore
import groovy.util.logging.Slf4j

/**
 * An API representation of a {@link Doc}
 */
@Slf4j
class ApiDoc {

    /**
     * Locates the given {@link Doc} and returns the object (or null if not found) and the request status for further processing
     * @param the field to look for the identifier, one of {id, uuid}
     * @param the identifier value
     * @return {@link ApiBox}(obj: Doc | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND)
     * @see ApiBox#validatePrecondition_1()
     */
    static ApiBox findDocumentBy(String query, String value) {
        ApiBox result = ApiBox.get()

        switch(query) {
            case 'id':
                result.obj = Doc.findAllWhere(id: Long.parseLong(value))
                break
            case 'uuid':
                result.obj = Doc.findAllWhere(uuid: value)
                break
            default:
                result.status = Constants.HTTP_BAD_REQUEST
                return result
                break
        }
        result.validatePrecondition_1()

        if (result.obj) {
            List<DocContext> contexts = DocContext.findAllByOwner(result.obj)
            int delCount = 0

            contexts.each { dc ->
                if (dc.status == RDStore.DOC_CTX_STATUS_DELETED) {
                    delCount++
                }
            }
            if (delCount == contexts.size()) {
                result.status = Constants.OBJECT_STATUS_DELETED
            }
        }

        result
    }

    /**
     * Checks if the given institution can access the given document. The cost item
     * is returned in case of success
     * @param doc the {@link Doc} whose details should be retrieved
     * @param context the institution ({@link Org}) requesting the cost item
     * @return Doc | FORBIDDEN
     */
    static requestDocument(Doc doc, Org context){

        boolean hasAccess = (doc.owner?.id == context.id)

        return (hasAccess ? doc : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Should return always null because ONIX PL is not in use in LAS:eR
     * @return Doc | FORBIDDEN | null
     */
    static requestOnixPlDocument(License license, Org context){
        def doc = license.onixplLicense?.doc
        if (! doc) {
            return null // not found
        }

        boolean hasAccess = false

        DocContext.findAllByOwner(doc).each { dc ->
            if (dc.license) {
                dc.getLicense().getOrgRelations().each { orgRole ->
                    // TODO check orgRole.roleType
                    if (orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                        doc = dc.getOwner()
                    }
                }
            }
        }

        return (hasAccess ? doc : Constants.HTTP_FORBIDDEN)
    }

    /**
     * Assembles the given document attributes into a {@link Map}. The schema of the map can be seen in
     * schemas.gsp
     * @param doc the {@link Doc} which should be output
     * @return Map<String, Object>
     */
    static Map<String, Object> getDocumentMap(Doc doc) {
        Map<String, Object> result = [:]

        if (doc) {
            result.content      = doc.content
            result.filename     = doc.filename
            result.mimeType     = doc.mimeType
            result.title        = doc.title
            result.uuid         = doc.uuid
            result.lastUpdated  = ApiToolkit.formatInternalDate(doc.lastUpdated)

            // RefdataValues
            result.type     = doc.type?.value
        }

        ApiToolkit.cleanUp(result, true, true)
    }
}
