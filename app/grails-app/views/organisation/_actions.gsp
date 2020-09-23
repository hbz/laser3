<%@page import="de.laser.helper.RDStore; grails.plugin.springsecurity.SpringSecurityUtils" %>
<laser:serviceInjection/>

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
            <semui:actionsDropdownItem data-semui="modal" href="#propDefGroupBindings" text="Merkmalsgruppen konfigurieren" />
        </g:if>
        <g:if test="${actionName == 'ids'}">
            <g:if test="${editable_identifier}">
                <a class="item" onclick="IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
            </g:if>
            <g:else>
                <semui:subNavItem message="identifier.create.new" disabled="disabled" />
            </g:else>
            <g:if test="${hasAccessToCustomeridentifier}">
                <g:if test="${editable_customeridentifier}">
                    <a class="item" onclick="IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
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
        </g:if>

        <g:if test="${actionName == 'addressbook'}">
            <semui:actionsDropdownItem data-semui="modal"
                                       href="#personFormModal" message="person.create_new.contactPerson.label" />
        </g:if>

        <g:if test="${actionName == 'accessPoints'}">
            <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'ip']" message="accessPoint.create_ip"/>
            <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'proxy']" message="accessPoint.create_proxy"/>
            <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'ezproxy']" message="accessPoint.create_ezproxy"/>
            <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'oa']" message="accessPoint.create_openAthens"/>
            <semui:actionsDropdownItem controller="accessPoint" action="create" params="[id: orgInstance.id, accessMethod: 'shibboleth']" message="accessPoint.create_shibboleth"/>
        </g:if>


    </g:if>
    <g:if test="${actionName in ['documents','show'] && (editable || accessService.checkPermAffiliationX('ORG_INST,ORG_CONSORTIUM','INST_EDITOR','ROLE_ORG_EDITOR'))}">
        <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument"/>
    </g:if>
    <g:if test="${actionName == 'show'}">
        <sec:ifAnyGranted roles="ROLE_ORG_EDITOR,ROLE_ADMIN">
            <semui:actionsDropdownItem controller="organisation" action="setupBasicTestData" params="[id: orgInstance.id]" message="datamanager.setupBasicOrgData.label"/>

            <div class="divider"></div>
            <g:link class="item" action="_delete" id="${params.id}"><i class="trash alternate icon"></i> ${message(code:'deletion.org')}</g:link>
        </sec:ifAnyGranted>
    </g:if>
</semui:actionsDropdown>
<g:if test="${editable || accessService.checkPermAffiliation('ORG_INST,ORG_CONSORTIUM','INST_EDITOR')}">
    <g:render template="/templates/documents/modal" model="${[ownobj: orgInstance, institution: contextService.org, owntp: 'org']}"/>
</g:if>
