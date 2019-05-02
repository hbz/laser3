<%@page import="de.laser.helper.RDStore; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

<semui:actionsDropdown>
    <g:if test="${editable}">
        <g:if test="${actionName == 'list'}">
            <semui:actionsDropdownItem controller="organisation" action="create" message="org.create_new.label"/>
        </g:if>
        <g:if test="${actionName == 'listInstitution'}">
            <semui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_institution.label"/>
        </g:if>
        <g:if test="${actionName == 'listProvider'}">
            <semui:actionsDropdownItem controller="organisation" action="findProviderMatches" message="org.create_new_provider.label"/>
        </g:if>
        <g:if test="${actionName == 'show'}">
            <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalgruppen konfigurieren" />
            </g:if>
        </g:if>
        <g:if test="${actionName == 'users'}">
            <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
        </g:if>

        <g:if test="${actionName == 'readerNumber'}">
            <semui:actionsDropdownItem data-semui="modal"
                                       href="#create_number" message="readerNumber.create.label" />
        </g:if>

        <g:if test="${actionName == 'addressbook'}">
            <semui:actionsDropdownItem data-semui="modal"
                                       href="#personFormModal" message="person.create_new.contactPerson.label" />
        </g:if>


    </g:if>
    <g:if test="${actionName in ['documents','show'] && (accessService.checkMinUserOrgRole(user, contextService.org, 'INST_EDITOR') || SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN,ROLE_ORG_EDITOR'))}">
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument"/>
    </g:if>
</semui:actionsDropdown>
<g:render template="/templates/documents/modal" model="${[ownobj: org, institution: contextService.org, owntp: 'org']}"/>