package de.laser.api.v0.entities

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import de.laser.api.v0.ApiBox
import de.laser.api.v0.ApiToolkit
import de.laser.helper.Constants
import groovy.util.logging.Log4j

@Log4j
class ApiDoc {

    /**
     * @return ApiBox(obj: Doc | null, status: null | BAD_REQUEST | PRECONDITION_FAILED | NOT_FOUND)
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
                break
        }
        result.validatePrecondition_1()

        result
    }

    /**
     * @return Doc | FORBIDDEN
     */
    static requestDocument(Doc doc, Org context){

        boolean hasAccess = false

        DocContext.findAllByOwner(doc).each{ dc ->
            if(dc.license) {
                dc.getLicense().getOrgLinks().each { orgRole ->
                    // TODO check orgRole.roleType
                    if(orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                    }
                }
            }
            if(dc.pkg) {
                dc.getPkg().getOrgs().each { orgRole ->
                    // TODO check orgRole.roleType
                    if(orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                    }
                }
            }
            if(dc.subscription) {
                dc.getSubscription().getOrgRelations().each { orgRole ->
                    // TODO check orgRole.roleType
                    if(orgRole.getOrg().id == context?.id) {
                        hasAccess = true
                    }
                }
            }
        }

        return (hasAccess ? doc : Constants.HTTP_FORBIDDEN)
    }

    /**
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
                dc.getLicense().getOrgLinks().each { orgRole ->
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
