<%@page import="de.laser.CustomerTypeService; de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:set var="contextOrg" value="${contextService.getOrg()}" />

<g:if test="${contextService.isInstEditor_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <g:if test="${actionName == 'currentSubscriptions'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="subscription" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <ui:actionsDropdownItem controller="myInstitution" action="subscriptionImport" message="menu.institutions.subscriptionImport" />
        </ui:actionsDropdown>
    </g:if>

    <g:if test="${actionName in ['currentLicenses']}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="myInstitution" action="emptyLicense" message="license.add.blank" />
        </ui:actionsDropdown>
    </g:if>

    <g:if test="${actionName in ['manageMembers', 'addMembers']}">
        <ui:actionsDropdown>
            <g:if test="${comboType != null && comboType == RDStore.COMBO_TYPE_CONSORTIUM}"> %{-- todo --}%
                <ui:actionsDropdownItem controller="myInstitution" action="addMembers" message="menu.institutions.add_consortia_members" />
            </g:if>
            <g:if test="${actionName in ['manageMembers']}">
                <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
        </ui:actionsDropdown>
    </g:if>
</g:if>
<g:elseif test="${contextService.isInstUser_or_ROLEADMIN(CustomerTypeService.ORG_SUPPORT)}">
    <g:if test="${actionName == 'manageMembers'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem data-ui="modal" id="copyMailAddresses" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
        </ui:actionsDropdown>
    </g:if>
</g:elseif>

<g:if test="${editable}">
    <g:if test="${actionName in ['documents']}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="template.documents.add" data-ui="modal" href="#modalCreateDocument" />
        </ui:actionsDropdown>
    </g:if>

    <g:if test="${actionName == 'tasks'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="task.create.new" data-ui="modal" href="#modalCreateTask" />
        </ui:actionsDropdown>
    </g:if>

    <g:if test="${actionName == 'managePrivatePropertyDefinitions'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="menu.institutions.manage_props.create_new" data-ui="modal" href="#addPropertyDefinitionModal" />
        </ui:actionsDropdown>
    </g:if>

    <g:if test="${actionName == 'managePropertyGroups'}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="propertyDefinitionGroup.create_new.label" controller="myInstitution" action="managePropertyGroups" params="${[cmd:'new']}" class="trigger-modal" notActive="true" />
        </ui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName in ['documents']}">
    <laser:render template="/templates/documents/modal" model="${[ownobj: contextOrg, owntp: 'org', inContextOrg: true]}"/>
</g:if>