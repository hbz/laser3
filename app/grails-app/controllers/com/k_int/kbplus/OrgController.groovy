package com.k_int.kbplus

import grails.plugins.springsecurity.Secured
import com.k_int.kbplus.ajax.AjaxHandler

@Deprecated
class OrgController extends AjaxHandler {

    def springSecurityService

    static allowedMethods = [create: ['GET', 'POST'], edit: ['GET', 'POST'], delete: 'POST']

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def index() {
        redirect controller: 'organisations', action: 'index', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def list() {
        redirect controller: 'organisations', action: 'list', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def create() {
        redirect controller: 'organisations', action: 'create', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def show() {
        redirect controller: 'organisations', action: 'show', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def edit() {
        redirect controller: 'organisations', action: 'edit', params: params
    }

    @Secured(['ROLE_USER', 'IS_AUTHENTICATED_FULLY'])
    def delete() {
        redirect controller: 'organisations', action: 'delete', params: params
    }

    @Deprecated
    @Override
    def ajax() {
        // TODO: check permissions for operation
        
        switch(params.op){
            case 'add':
                ajaxAdd()
                return
            break;
            case 'delete':
                ajaxDelete()
                return
            break;
            default:
                ajaxList()
                return
            break;
        }
    }

    @Deprecated
    @Override
    def private ajaxList() {
        def valid               = true
        def org                 = Org.get(params.id)
        def type                = params.type
        def orgLinks            // only org depeding entities
        def targets             // 
        def roles               = RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Organisational Role'))
        def targetOptionValue   = "name"
        def linkController      
        def linkAction          // TODO
                
        def hqlPart = "from OrgRole as GOR where GOR.org = ${org.id}"
        
        if(type == "cluster") {
            targets             = Cluster.getAll()
            linkController      = "cluster"
            roles               = RefdataValue.findAllByOwner(com.k_int.kbplus.RefdataCategory.findByDesc('Cluster Role'))
            orgLinks            = OrgRole.findAll(hqlPart + " and GOR.cluster is not NULL")
        }
        else if(type == "lic") {
            targets             = License.getAll()
            targetOptionValue   = "reference"
            linkController      = "license"
            orgLinks            = OrgRole.findAll(hqlPart + " and GOR.lic is not NULL")
        }
        else if(type == "pkg") {
            targets             = Package.getAll()
            linkController      = "package"
            orgLinks            = OrgRole.findAll(hqlPart + " and GOR.pkg is not NULL")
        }
        else if(type == "sub") {
            targets             = Subscription.getAll()
            linkController      = "subscription"
            orgLinks            = OrgRole.findAll(hqlPart + " and GOR.sub is not NULL")
        }
        else if(type == "title") {
            targets             = TitleInstance.getAll()
            targetOptionValue   = "normTitle"
            linkController      = "titleInstance"
            orgLinks            = OrgRole.findAll(hqlPart + " and GOR.title is not NULL")
        }
        else {
            valid = false
        }

        render view: 'ajax/genericOrgRoleList', model: [
            orgInstance:        org,
            targets:            targets,   
            orgLinks:           orgLinks,
            type:               type,       
            roles:              roles,
            targetOptionValue:  targetOptionValue,
            linkController:     linkController
            ]
        return
    }

    @Deprecated
    @Override
    def private ajaxDelete() {

        log.debug(params)
        
        def orgRole = OrgRole.get(params.orgRole)
        // TODO: switch to resolveOID/resolveOID2 ?
        
        //def orgRole = AjaxController.resolveOID(params.orgRole[0])
        if(orgRole) {
            log.debug("deleting OrgRole ${orgRole}")
            orgRole.delete(flush:true);
        }
        ajaxList()
    }

    @Deprecated
    @Override
    def private ajaxAdd() {
        
        log.debug(params)
     
        def org         = Org.get(params.id)
        def orgRole     = RefdataValue.get(params.role)
        def type        = params.type
        def newOrgRole
        def target

        switch(type) {
            case "cluster":
                target      = Cluster.get(params.target)
                newOrgRole  = new OrgRole(org:org, roleType:orgRole, cluster:target)
            break;
            case"lic":
                target      = License.get(params.target)
                newOrgRole  = new OrgRole(org:org, roleType:orgRole, lic:target)
            break;
            case "pkg":
                target      = Package.get(params.target)
                newOrgRole  = new OrgRole(org:org, roleType:orgRole, pkg:target)
            break;
            case "sub":
                target      = Subscription.get(params.target)
                newOrgRole  = new OrgRole(org:org, roleType:orgRole, sub:target)
            break;
            case "title":
                target      = TitleInstance.get(params.target)
                newOrgRole  = new OrgRole(org:org, roleType:orgRole, title:target)
            break;
        }
        
        if(OrgRole.find("from OrgRole as GOR where GOR.org = ${org.id} and GOR.roleType = ${orgRole.id} and GOR.${type} = ${target.id}")) {
            log.debug("ignoring to add OrgRole because of existing duplicate")
        }
        else if(newOrgRole) {
            if(newOrgRole.save(flush:true)) {
                log.debug("adding OrgRole ${newOrgRole}")
            } else {
                log.error("problem saving new OrgRole ${newOrgRole}")
                if(newOrgRole)
                    newOrgRole.errors.each { e ->
                        log.error(e)
                    }
            }
        }
        else {
            log.debug("problem saving new OrgRole")
        }
        
        ajaxList()
    }
}
