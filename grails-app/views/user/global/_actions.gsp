<%@ page import="de.laser.CustomerTypeService; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

<g:if test="${actionName == 'list'}">%{-- /user/list --}%
    <ui:actionsDropdown>
        <ui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" />
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${actionName == 'edit'}">%{-- /user/edit --}%
    <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
        <ui:actionsDropdown>
            <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.user')}</g:link>
        </ui:actionsDropdown>
    </g:if>
</g:elseif>
<g:elseif test="${actionName == 'users'}">
    <g:if test="${controllerName == 'myInstitution'}">%{-- /myInstitution/users --}%
        <ui:actionsDropdown>%{-- todo -- move to template --}%
            <g:set var="createNTDWModals" value="${true}"/>
            <g:if test="${contextService.isInstEditor_or_ROLEADMIN()}">
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateNote" message="template.notes.add"/>
            </g:if>
            <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}">
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateTask" message="task.create.new"/>
            </g:if>
            <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateDocument" message="template.documents.add"/>
            </g:if>
            <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}"><!-- TODO: workflows-permissions -->
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateWorkflow" message="workflow.instantiate"/>
            </g:if>

            <g:if test="${contextService.isInstAdm_or_ROLEADMIN()}">
                <div class="divider"></div>
                <ui:actionsDropdownItem controller="myInstitution" action="createUser" message="user.create_new.label" />
            </g:if>
        </ui:actionsDropdown>
    </g:if>
    <g:elseif test="${controllerName == 'organisation'}">%{-- organisation/users - TODO: isComboInstAdminOf --}%
        <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
            <ui:actionsDropdown>
                <ui:actionsDropdownItem controller="organisation" action="createUser" message="user.create_new.label" params="${[id:params.id]}"/>
            </ui:actionsDropdown>
        </g:if>
    </g:elseif>
</g:elseif>
<g:elseif test="${actionName == 'editUser'}">
    <g:if test="${controllerName == 'myInstitution'}">%{-- /myInstitution/editUser --}%
        <g:if test="${contextService.isInstAdm_or_ROLEADMIN()}">
            <ui:actionsDropdown>
                <g:link class="item" action="deleteUser" params="${[uoid: params.uoid]}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.user')}</g:link>
            </ui:actionsDropdown>
        </g:if>
    </g:if>
    <g:elseif test="${controllerName == 'organisation'}">%{-- /organisation/editUser - TODO: isComboInstAdminOf --}%
        <g:if test="${SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')}">
            <ui:actionsDropdown>
                <g:link class="item" action="deleteUser" params="${[id:params.id, uoid: params.uoid]}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.user')}</g:link>
            </ui:actionsDropdown>
        </g:if>
    </g:elseif>
</g:elseif>

<g:if test="${createNTDWModals}">
    <g:if test="${contextService.isInstEditor_or_ROLEADMIN()}">
        <laser:render template="/templates/notes/modal_create" model="${[ownobj: orgInstance, owntp: 'org']}"/>
    </g:if>
    <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}">
        <laser:render template="/templates/tasks/modal_create" model="${[ownobj: orgInstance, owntp: 'org']}"/>
    </g:if>
    <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC)}">
        <laser:render template="/templates/documents/modal" model="${[ownobj: orgInstance, institution: institution, owntp: 'org']}"/>
    </g:if>
    <g:if test="${contextService.hasPermAsInstEditor_or_ROLEADMIN(CustomerTypeService.PERMS_PRO)}"><!-- TODO: workflows-permissions -->
        <laser:render template="/templates/workflow/instantiate" model="${[target: orgInstance]}"/>
    </g:if>
</g:if>