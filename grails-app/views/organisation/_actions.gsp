<%@ page import="de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<g:if test="${accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ADMIN')}">
    <ui:actionsDropdown>
        <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
            <g:if test="${actionName == 'list'}">
                <ui:actionsDropdownItem controller="organisation" action="create" message="org.create_new.label"/>
            </g:if>
            <g:if test="${actionName == 'listInstitution'}">
                <ui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_institution.label"/>
            </g:if>
            <g:if test="${actionName == 'listProvider'}">
                <ui:actionsDropdownItem controller="organisation" action="findProviderMatches" message="org.create_new_provider.label"/>
            </g:if>
            <g:if test="${actionName == 'show'}">
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateTask" message="task.create.new"/>
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateDocument" message="template.documents.add"/>
                <ui:actionsDropdownItem data-ui="modal" href="#modalCreateNote" message="template.notes.add"/>

                <g:if test="${workflowService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                    <g:if test="${inContextOrg || isProviderOrAgency}">
                        <div class="divider"></div>
                        <ui:actionsDropdownItem message="workflow.instantiate" data-ui="modal" href="#modalInstantiateWorkflow" />
                    </g:if>
                </g:if>

%{--                <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />--}% %{-- erms-4798 --}%
                <g:set var="createModal" value="${true}"/>
            </g:if>
            <g:if test="${actionName == 'ids'}">
                <g:if test="${editable_identifier}">
                    <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
                </g:if>
                <g:else>
                    <ui:subNavItem message="identifier.create.new" disabled="disabled" />
                </g:else>
                <g:if test="${hasAccessToCustomeridentifier}">
                    <g:if test="${editable_customeridentifier}">
                        <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
                    </g:if>
                    <g:else>
                        <ui:subNavItem message="org.customerIdentifier.create.new" disabled="disabled" />
                    </g:else>
                </g:if>
            </g:if>
            <g:if test="${actionName == 'users'}">
                <ui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
            </g:if>
            <g:if test="${actionName == 'workflows'}">
                <g:if test="${workflowService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
                    <g:if test="${inContextOrg || isProviderOrAgency}">
                        <ui:actionsDropdownItem message="workflow.instantiate" data-ui="modal" href="#modalInstantiateWorkflow" />
                    </g:if>
                </g:if>
            </g:if>
            <g:if test="${actionName == 'readerNumber'}">
                <ui:actionsDropdownItem data-ui="modal" href="#newForUni" message="readerNumber.createForUni.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForPublic" message="readerNumber.createForPublic.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForState" message="readerNumber.createForState.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForResearchInstitute" message="readerNumber.createForResearchInstitute.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForScientificLibrary" message="readerNumber.createForScientificLibrary.label" />
            </g:if>

        </g:if>
        <g:if test="${actionName == 'tasks'}">
            <ui:actionsDropdownItem message="task.create.new" data-ui="modal" href="#modalCreateTask"/>
            <g:set var="createModal" value="${true}"/>
        </g:if>
        <g:if test="${actionName == 'documents'}">
            <ui:actionsDropdownItem message="template.documents.add" data-ui="modal" href="#modalCreateDocument"/>
            <g:set var="createModal" value="${true}"/>
        </g:if>
        <g:if test="${actionName == 'notes'}">
            <ui:actionsDropdownItem message="template.notes.add" data-ui="modal" href="#modalCreateNote"/>
            <g:set var="createModal" value="${true}"/>
        </g:if>
        <g:if test="${actionName == 'show'}">
            <sec:ifAnyGranted roles="ROLE_ADMIN">
                <div class="divider"></div>
                <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.org')}</g:link>
            </sec:ifAnyGranted>
        </g:if>
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${accessService.checkPermAffiliationX('ORG_BASIC_MEMBER','INST_EDITOR','ROLE_ADMIN')}">
    <g:if test="${actionName in ['show','notes']}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="template.notes.add" data-ui="modal" href="#modalCreateNote"/>
            <g:set var="createModal" value="${true}"/>
        </ui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'ids'}">
        <ui:actionsDropdown>
            <g:if test="${editable_identifier}">
                <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
            </g:if>
            <g:else>
                <ui:actionsDropdownItem message="identifier.create.new" disabled="disabled" />
            </g:else>
            <g:if test="${hasAccessToCustomeridentifier}">
                <g:if test="${editable_customeridentifier}">
                    <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
                </g:if>
                <g:else>
                    <ui:actionsDropdownItem message="org.customerIdentifier.create.new" disabled="disabled" />
                </g:else>
            </g:if>
        </ui:actionsDropdown>
    </g:if>
    <g:if test="${actionName == 'users'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
        </ui:actionsDropdown>
    </g:if>

    <g:if test="${actionName == 'readerNumber'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem data-ui="modal" href="#newForUni" message="readerNumber.createForUni.label" />
            <ui:actionsDropdownItem data-ui="modal" href="#newForPublic" message="readerNumber.createForPublic.label" />
            <ui:actionsDropdownItem data-ui="modal" href="#newForState" message="readerNumber.createForState.label" />
            <ui:actionsDropdownItem data-ui="modal" href="#newForResearchInstitute" message="readerNumber.createForResearchInstitute.label" />
            <ui:actionsDropdownItem data-ui="modal" href="#newForScientificLibrary" message="readerNumber.createForScientificLibrary.label" />
        </ui:actionsDropdown>
    </g:if>

</g:elseif>
<%-- secure against listInstitution, where no orgId is given --%>
<g:if test="${createModal}">
    <g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
        <laser:render template="/templates/tasks/modal_create" model="${[ownobj: orgInstance, owntp: 'org']}"/>
        <laser:render template="/templates/documents/modal" model="${[ownobj: orgInstance, institution: institution, owntp: 'org']}"/>
    </g:if>
    <g:if test="${accessService.checkMinUserOrgRole(user,institution,'INST_EDITOR')}">
        <laser:render template="/templates/notes/modal_create" model="${[ownobj: orgInstance, owntp: 'org']}"/>
    </g:if>
</g:if>

<g:if test="${workflowService.hasUserPerm_init()}"><!-- TODO: workflows-permissions -->
    <g:if test="${inContextOrg}">
        <laser:render template="/templates/workflow/instantiate" model="${[cmd: RDStore.WF_WORKFLOW_TARGET_TYPE_INSTITUTION, target: orgInstance]}"/>
    </g:if>
    <g:if test="${isProviderOrAgency}">
        <laser:render template="/templates/workflow/instantiate" model="${[cmd: RDStore.WF_WORKFLOW_TARGET_TYPE_PROVIDER, target: orgInstance]}"/>
    </g:if>
</g:if>

