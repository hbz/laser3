package com.k_int.kbplus.api.v0

import com.k_int.kbplus.Doc
import com.k_int.kbplus.DocContext
import com.k_int.kbplus.Org
import com.k_int.kbplus.api.v0.out.OutService
import com.k_int.kbplus.auth.User
import groovy.util.logging.Log4j

@Log4j
class DocService {

    OutService outService

    /**
     * @return Doc | BAD_REQUEST
     */
    def findDocumentBy(String query, String value) {

        switch(query) {
            case 'id':
                return Doc.findWhere(id: Long.parseLong(value))
                break
            case 'uuid':
                return Doc.findWhere(uuid: value)
                break
            default:
                return MainService.BAD_REQUEST
                break
        }
    }

    /**
     * @return Doc | FORBIDDEN
     */
    def getDocument(Doc doc, User user, Org context){
        def hasAccess = false

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
        return (hasAccess ? doc : MainService.FORBIDDEN)
    }
}
