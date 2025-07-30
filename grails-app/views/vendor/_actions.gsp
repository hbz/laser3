<%@ page import="de.laser.ui.Icon; de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<ui:actionsDropdown>
    <g:if test="${contextService.isInstEditor( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )}">
        <laser:render template="/templates/sidebar/actions" />
        <g:if test="${editable}">
            <g:if test="${actionName == 'addressbook'}">
                <div class="divider"></div>
                <g:if test="${editable}">
                    <a href="#createPersonModal" class="item" data-ui="modal" onclick="JSPC.app.personCreate('contactPersonForVendor', ${vendor.id});"><g:message code="person.create_new.contactPersonForVendor.label"/></a>
                    <a href="#addressFormModal" class="item" onclick="JSPC.app.addressCreate('addressForVendor', ${vendor.id});"><g:message code="address.add.addressForVendor.label"/></a>
                </g:if>
                <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
        </g:if>
    </g:if>
    <g:elseif test="${contextService.isInstEditor( CustomerTypeService.ORG_INST_BASIC )}">
        <ui:actionsDropdownItem message="template.notes.add" data-ui="modal" href="#modalCreateNote"/>

        <g:if test="${actionName == 'addressbook'}">
            <div class="divider"></div>
            <g:if test="${editable}">
                <a href="#createPersonModal" class="item" data-ui="modal"
                   onclick="JSPC.app.personCreate('contactPersonForAgency', ${vendor.id});"><g:message code="person.create_new.contactPersonForVendor.label"/></a>
            </g:if>
            <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </g:if>
    </g:elseif>
    <g:else>
        <g:if test="${actionName == 'addressbook'}">
            <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </g:if>
    </g:else>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <g:if test="${actionName == 'show'}">
            <g:if test="${!vendor.gokbId}">
                <div class="divider"></div>
                <g:link action="delete" id="${vendor.id}" class="item"><i class="${Icon.CMD.DELETE}"></i>
                    ${message(code: 'deletion.vendor')}
                </g:link>
            </g:if>
        </g:if>
    </sec:ifAnyGranted>
</ui:actionsDropdown>

<g:if test="${contextService.isInstEditor()}">
    <laser:render template="/templates/sidebar/modals" model="${[tmplConfig: [ownobj: vendor, owntp: 'vendor']]}" />
</g:if>


