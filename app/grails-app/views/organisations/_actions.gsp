<%@page import="de.laser.helper.RDStore" %>
<laser:serviceInjection/>
<semui:actionsDropdown>
    <g:if test="${editable}">
        <g:if test="${actionName == 'list'}">
            <semui:actionsDropdownItem controller="organisations" action="create" message="org.create_new.label"/>
        </g:if>
        <g:if test="${actionName == 'listInstitution'}">
            <semui:actionsDropdownItem controller="organisations" action="findInstitutionMatches" message="org.create_new_Institution.label"/>
        </g:if>
        <g:if test="${actionName == 'listProvider'}">
            <semui:actionsDropdownItem controller="organisations" action="findProviderMatches" message="org.create_new_Provider.label"/>
        </g:if>
        <g:if test="${actionName == 'show'}">
            <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalgruppen konfigurieren" />
            </g:if>
        </g:if>
        <g:if test="${actionName == 'users'}">
            <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
        </g:if>
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user, contextService.org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR')}">
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument"/>
    </g:if>
</semui:actionsDropdown>
<g:render template="/templates/documents/modal" model="${[ownobj: org, institution: contextService.org, owntp: 'org']}"/>