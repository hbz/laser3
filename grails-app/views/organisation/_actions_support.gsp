<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionDropdownItems: true]]}" />

        <g:if test="${editable}">
            <g:if test="${actionName == 'show'}">
%{--                <ui:actionsDropdownItem data-ui="modal" href="#propDefGroupBindings" message="menu.institutions.configure_prop_groups" />--}%%{-- --}%%{-- erms-4798 --}%
            </g:if>
            <g:elseif test="${actionName == 'ids'}">
                <g:if test="${editable_identifier || (hasAccessToCustomeridentifier && editable_customeridentifier)}">
                    <div class="divider"></div>
                </g:if>
                <g:if test="${editable_identifier}">
                    <a class="item" onclick="JSPC.app.IdContoller.createIdentifier(${orgInstance.id});">${message(code: 'identifier.create.new')}</a>
                </g:if>
                <g:if test="${hasAccessToCustomeridentifier && editable_customeridentifier}">
                        <a class="item" onclick="JSPC.app.IdContoller.createCustomerIdentifier(${orgInstance.id});">${message(code: 'org.customerIdentifier.create.new')}</a>
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
                <g:if test="${editable && !isProviderOrAgency}">
                    <a href="#createPersonModal" class="item" data-ui="modal" onclick="JSPC.app.personCreate('contactPersonForInstitution', ${orgInstance.id});"><g:message code="person.create_new.contactPersonForInstitution.label"/></a>
                    <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForInstitution', ${orgInstance.id});"><g:message code="address.add.addressForInstitution.label"/></a>
                </g:if>
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:elseif>
            <g:elseif test="${actionName == 'contacts'}">
                <div class="divider"></div>
                <g:if test="${editable}">
                    <a href="#createPersonModal" class="item" onclick="JSPC.app.personCreate('contactPersonForPublic');"><g:message code="person.create_new.contactPerson.label"/></a>
                    <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForPublic');"><g:message code="address.add.addressForPublic.label"/></a>
                </g:if>
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:elseif>
        </g:if>

        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <g:if test="${actionName in ['show']}">
                <div class="divider"></div>
                <g:link class="item js-open-confirm-modal la-popup-tooltip la-delay" action="disableAllUsers" id="${params.id}"
                        data-confirm-tokenMsg="${message(code: "confirm.dialog.disable.allInstUsers")}" data-confirm-term-how="ok">
                    <i class="user lock icon"></i> ${message(code:'org.disableAllUsers.label')}
                </g:link>
                <g:link class="item" action="delete" id="${params.id}"><i class="${Icon.CMD.DELETE}"></i> ${message(code:'deletion.org')}</g:link>
            </g:if>
        </sec:ifAnyGranted>
    </ui:actionsDropdown>
</g:if>

%{--<!--}%
%{--    orgInstance (org to show) : ${orgInstance}--}%
%{--    institution (context org) : ${institution}--}%
%{----!>--}%

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionModals: true, ownobj: orgInstance, owntp: 'org', institution: institution]]}" />
</g:if>


