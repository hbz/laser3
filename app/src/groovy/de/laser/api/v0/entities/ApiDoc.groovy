package de.laser.api.v0.entities

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.License
import com.k_int.kbplus.Org
import com.k_int.kbplus.auth.User
import de.laser.domain.Constants
import de.laser.api.v0.ApiReader
import groovy.util.logging.Log4j

@Log4j
class ApiDoc {

    /**
     * @return Doc | BAD_REQUEST | PRECONDITION_FAILED
     */
    static findDocumentBy(String query, String value) {
        def result

        switch(query) {
            case 'id':
                result = Doc.findAllWhere(id: Long.parseLong(value))
                break
            case 'uuid':
                result = Doc.findAllWhere(uuid: value)
                break
            default:
                return Constants.HTTP_BAD_REQUEST
                break
        }
        if (result) {
            result = result.size() == 1 ? result.get(0) : Constants.HTTP_PRECONDITION_FAILED
        }
        result
    }

    /**
     * @return Doc | FORBIDDEN
     */
    static getDocument(Doc doc, User user, Org context){
        def hasAccess = ApiReader.isDataManager(user)

        if (! hasAccess) {
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
        }

        return (hasAccess ? doc : Constants.HTTP_FORBIDDEN)
    }

    /**
     * @return Doc | FORBIDDEN | null
     */
    static getOnixPlDocument(License license, User user, Org context){
        def doc = license.onixplLicense?.doc
        if (! doc) {
            return null // not found
        }

        def hasAccess = ApiReader.isDataManager(user)

        if (! hasAccess) {
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
        }
        return (hasAccess ? doc : Constants.HTTP_FORBIDDEN)
    }
}
