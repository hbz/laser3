<laser:serviceInjection/>

<g:if test="${accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN,ROLE_ORG_EDITOR')}">
    <semui:actionsDropdown>
        <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
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
                <semui:actionsDropdownItem data-semui="modal" href="#modalCreateTask" message="task.create.new"/>
                <semui:actionsDropdownItem data-semui="modal" href="#modalCreateDocument" message="template.documents.add"/>
                <semui:actionsDropdownItem data-semui="modal" href="#modalCreateNote" message="template.notes.add"/>
                <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />
                <g:set var="createModal" value="${true}"/>
            </g:if>
            <g:if test="${actionName == 'ids'}">
                <g:if test="${editable_identifier}">
                    <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
                </g:if>
                <g:else>
                    <semui:subNavItem message="identifier.create.new" disabled="disabled" />
                </g:else>
                <g:if test="${hasAccessToCustomeridentifier}">
                    <g:if test="${editable_customeridentifier}">
                        <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
                    </g:if>
                    <g:else>
                        <semui:subNavItem message="org.customerIdentifier.create.new" disabled="disabled" />
                    </g:else>
                </g:if>
            </g:if>
            <g:if test="${actionName == 'users'}">
                <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
            </g:if>

            <g:if test="${actionName == 'readerNumber'}">
                <semui:actionsDropdownItem data-semui="modal" href="#newForUni" message="readerNumber.createForUni.label" />
                <semui:actionsDropdownItem data-semui="modal" href="#newForPublic" message="readerNumber.createForPublic.label" />
                <semui:actionsDropdownItem data-semui="modal" href="#newForState" message="readerNumber.createForState.label" />
                <semui:actionsDropdownItem data-semui="modal" href="#newForResearchInstitute" message="readerNumber.createForResearchInstitute.label" />
                <semui:actionsDropdownItem data-semui="modal" href="#newForScientificLibrary" message="readerNumber.createForScientificLibrary.label" />
            </g:if>

        </g:if>
        <g:if test="${actionName == 'tasks'}">
            <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask"/>
            <g:set var="createModal" value="${true}"/>
        </g:if>
        <g:if test="${actionName == 'documents'}">
            <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument"/>
            <g:set var="createModal" value="${true}"/>
        </g:if>
        <g:if test="${actionName == 'notes'}">
            <semui:actionsDropdownItem message="template.notes.add" data-semui="modal" href="#modalCreateNote"/>
            <g:set var="createModal" value="${true}"/>
        </g:if>
        <g:if test="${actionName == 'show'}">
            <sec:ifAnyGranted roles="ROLE_ORG_EDITOR,ROLE_ADMIN">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.org')}</g:link>
            </sec:ifAnyGranted>
        </g:if>
    </semui:actionsDropdown>
</g:if>
<g:elseif test="${accessService.checkPermAffiliationX('ORG_BASIC_MEMBER','INST_EDITOR','ROLE_ADMIN,ROLE_ORG_EDITOR')}">
    <g:if test="${actionName in ['show','notes']}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="template.notes.add" data-semui="modal" href="#modalCreateNote"/>
            <g:set var="createModal" value="${true}"/>
        </semui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'ids'}">
        <semui:actionsDropdown>
            <g:if test="${editable_identifier}">
                <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
            </g:if>
            <g:else>
                <semui:actionsDropdownItem message="identifier.create.new" disabled="disabled" />
            </g:else>
            <g:if test="${hasAccessToCustomeridentifier}">
                <g:if test="${editable_customeridentifier}">
                    <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
                </g:if>
                <g:else>
                    <semui:actionsDropdownItem message="org.customerIdentifier.create.new" disabled="disabled" />
                </g:else>
            </g:if>
        </semui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'users'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
        </semui:actionsDropdown>
    </g:if>

    <g:if test="${actionName == 'readerNumber'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem data-semui="modal" href="#newForUni" message="readerNumber.createForUni.label" />
            <semui:actionsDropdownItem data-semui="modal" href="#newForPublic" message="readerNumber.createForPublic.label" />
            <semui:actionsDropdownItem data-semui="modal" href="#newForState" message="readerNumber.createForState.label" />
            <semui:actionsDropdownItem data-semui="modal" href="#newForResearchInstitute" message="readerNumber.createForResearchInstitute.label" />
            <semui:actionsDropdownItem data-semui="modal" href="#newForScientificLibrary" message="readerNumber.createForScientificLibrary.label" />
        </semui:actionsDropdown>
    </g:if>

</g:elseif>
<%-- secure against listInstitution, where no orgId is given --%>
<g:if test="${createModal}">
    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <laser:render template="/templates/tasks/modal_create" model="${[ownobj: orgInstance, owntp: 'org', validResponsibleUsers: taskService.getUserDropdown(institution)]}"/>
        <laser:render template="/templates/documents/modal" model="${[ownobj: orgInstance, institution: institution, owntp: 'org']}"/>
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user,institution,'INST_EDITOR')}">
        <laser:render template="/templates/notes/modal_create" model="${[ownobj: orgInstance, owntp: 'org']}"/>
    </g:if>
</g:if>

