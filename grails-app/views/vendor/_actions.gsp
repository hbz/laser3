<%@ page import="de.laser.CustomerTypeService; de.laser.utils.AppUtils; de.laser.storage.RDStore" %>
<laser:serviceInjection/>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC )}">
    <ui:actionsDropdown>
        <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionDropdownItems: true]]}" />
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
    </ui:actionsDropdown>
</g:if>
<g:elseif test="${contextService.isInstEditor_or_ROLEADMIN( CustomerTypeService.ORG_INST_BASIC )}">
    <ui:actionsDropdown>
        <ui:actionsDropdownItem message="template.notes.add" data-ui="modal" href="#modalCreateNote"/>

        <g:if test="${actionName == 'addressbook'}">
            <div class="divider"></div>
            <g:if test="${editable}">
                <a href="#createPersonModal" class="item" data-ui="modal"
                   onclick="JSPC.app.personCreate('contactPersonForAgency', ${vendor.id});"><g:message code="person.create_new.contactPersonForVendor.label"/></a>
            </g:if>
            <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </g:if>
    </ui:actionsDropdown>
</g:elseif>
<g:else>
    <g:if test="${actionName == 'addressbook'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem notActive="true" data-ui="modal" href="#copyFilteredEmailAddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </ui:actionsDropdown>
    </g:if>
</g:else>

<g:if test="${contextService.isInstEditor_or_ROLEADMIN()}">
    <laser:render template="/templates/sidebar/helper" model="${[tmplConfig: [addActionModals: true, ownobj: vendor, owntp: 'vendor', institution: institution]]}" />
</g:if>


