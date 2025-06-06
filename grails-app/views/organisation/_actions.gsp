<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<g:if test="${contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/actions" />
%{--                <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />--}% %{-- erms-4798 --}%

        <g:if test="${editable}">
            <g:if test="${actionName == 'show'}">
%{--                <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />--}%%{-- --}%%{-- erms-4798 --}%
            </g:if>
            <g:elseif test="${actionName == 'ids'}">
                <g:if test="${editable_identifier}">
                    <div class="divider"></div>
                    <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
                </g:if>
                <g:if test="${hasAccessToCustomeridentifier}">
                    <g:if test="${editable_customeridentifier}">
                        <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
                    </g:if>
                </g:if>
            </g:elseif>
            <g:elseif test="${actionName == 'users'}">
                <div class="divider"></div>
                <ui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
            </g:elseif>
            <g:elseif test="${actionName == 'readerNumber'}">
                <g:if test="${editable}">
                    <div class="divider"></div>
                    <ui:actionsDropdownItem data-ui="modal" href="#newForUni" message="readerNumber.createForUni.label" />
                    <ui:actionsDropdownItem data-ui="modal" href="#newForPublic" message="readerNumber.createForPublic.label" />
                    <ui:actionsDropdownItem data-ui="modal" href="#newForState" message="readerNumber.createForState.label" />
                    <ui:actionsDropdownItem data-ui="modal" href="#newForResearchInstitute" message="readerNumber.createForResearchInstitute.label" />
                    <ui:actionsDropdownItem data-ui="modal" href="#newForScientificLibrary" message="readerNumber.createForScientificLibrary.label" />
                </g:if>
            </g:elseif>
            <g:elseif test="${actionName == 'addressbook'}">
                <div class="divider"></div>
                <g:if test="${editable}">
                    <a href="#createPersonModal" class="item" data-ui="modal" onclick="JSPC.app.personCreate('contactPersonForInstitution', ${orgInstance.id});"><g:message code="person.create_new.contactPersonForInstitution.label"/></a>
                    <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForInstitution', ${orgInstance.id});"><g:message code="address.add.addressForInstitution.label"/></a>
                </g:if>
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:elseif>
            <g:elseif test="${actionName == 'contacts'}">
                <div class="divider"></div>
                <g:if test="${editable}">
                    <a href="#createPersonModal" class="item" onclick="JSPC.app.personCreate('contactPersonForPublic');"><g:message code="person.create_new.contactPerson.label"/></a>
                    <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForPublic');"><g:message code="address.add.address.label"/></a>
                </g:if>
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:elseif>
        </g:if>

        <g:render template="actions_roleAdminOptions" />
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${contextService.isInstEditor( CustomerTypeService.ORG_INST_BASIC )}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem message="template.notes.add" data-ui="modal" href="#modalCreateNote"/>

        <g:if test="${actionName in ['show', 'notes']}">
        </g:if>
        <g:elseif test="${actionName == 'ids'}">
            <g:if test="${editable_identifier}">
                <div class="divider"></div>
                <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
            </g:if>
            <g:if test="${hasAccessToCustomeridentifier}">
                <g:if test="${editable_customeridentifier}">
                    <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
                </g:if>
            </g:if>
        </g:elseif>
        <g:elseif test="${actionName == 'users'}">
            <div class="divider"></div>
            <ui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" params="[org: orgInstance.id]" />
        </g:elseif>
        <g:elseif test="${actionName == 'readerNumber'}">
            <g:if test="${editable}">
                <div class="divider"></div>
                <ui:actionsDropdownItem data-ui="modal" href="#newForUni" message="readerNumber.createForUni.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForPublic" message="readerNumber.createForPublic.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForState" message="readerNumber.createForState.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForResearchInstitute" message="readerNumber.createForResearchInstitute.label" />
                <ui:actionsDropdownItem data-ui="modal" href="#newForScientificLibrary" message="readerNumber.createForScientificLibrary.label" />
            </g:if>
        </g:elseif>
        <g:elseif test="${actionName == 'addressbook'}">
            <div class="divider"></div>
            <g:if test="${editable}">
                <a href="#createPersonModal" class="item" data-ui="modal" onclick="JSPC.app.personCreate('contactPersonForInstitution', ${orgInstance.id});"><g:message code="person.create_new.contactPersonForInstitution.label"/></a>
            </g:if>
            <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </g:elseif>
        <g:elseif test="${actionName == 'contacts'}">
            <div class="divider"></div>
            <g:if test="${editable}">
                <a href="#createPersonModal" class="item" onclick="JSPC.app.personCreate('contactPersonForPublic');"><g:message code="person.create_new.contactPerson.label"/></a>
            </g:if>
            <g:if test="${editable}">
                <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForPublic');"><g:message code="address.add.addressForPublic.label"/></a>
            </g:if>
            <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </g:elseif>

        <g:render template="actions_roleAdminOptions" />
    </ui:actionsDropdown>
</g:elseif>
<g:else>
    <g:if test="${actionName in ['addressbook', 'contacts']}">
        <ui:actionsDropdown>
            <g:if test="${actionName == 'addressbook'}">
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
            <g:elseif test="${actionName == 'contacts'}">
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:elseif>
        </ui:actionsDropdown>
    </g:if>
</g:else>
<%-- secure against listInstitution, where no orgId is given --%>

%{--<!--}%
%{--    orgInstance (org to show) : ${orgInstance}--}%
%{--    institution (context org) : ${institution}--}%
%{----!>--}%

<g:if test="${contextService.isInstEditor()}">
    <laser:render template="/templates/sidebar/modals" model="${[tmplConfig: [ownobj: orgInstance, owntp: 'org']]}" />
</g:if>


