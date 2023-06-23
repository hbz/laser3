<%@page import="de.laser.CustomerTypeService; de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:set var="contextOrg" value="${contextService.getOrg()}" />
<g:if test="${actionName == 'currentSubscriptions'}">
    <ui:actionsDropdown>
        <g:if test="${contextService.hasAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')}">
            <ui:actionsDropdownItem controller="subscription" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <ui:actionsDropdownItem controller="myInstitution" action="subscriptionImport" message="menu.institutions.subscriptionImport" />
            <div class="divider"></div>
        </g:if>
        <ui:actionsDropdownItem notActive="true" controller="myInstitution" action="currentSubscriptions" params="${[compare: true]+params}" message="menu.my.comp_sub" />
    </ui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['currentLicenses']}">
    <g:if test="${contextService.hasAffiliation(CustomerTypeService.PERMS_INST_PRO_CONSORTIUM_BASIC, 'INST_EDITOR')}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem controller="myInstitution" action="emptyLicense" message="license.add.blank" />

            <div class="divider"></div>
            <ui:actionsDropdownItem notActive="true" controller="myInstitution" action="currentLicenses" params="${[compare: true]+params}" message="menu.my.comp_lic" />
        </ui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName in ['manageMembers', 'addMembers']}">
    <g:if test="${contextService.hasAffiliation(CustomerTypeService.ORG_CONSORTIUM_BASIC, 'INST_EDITOR')}">
        <ui:actionsDropdown>
            <g:if test="${comboType != null && comboType == RDStore.COMBO_TYPE_CONSORTIUM}">
                <ui:actionsDropdownItem controller="myInstitution" action="addMembers" message="menu.institutions.add_consortia_members" />
                <%-- leave for eventual reconsideration on behalf of users
                <ui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_institution.label"/>
                --%>
            </g:if>
            <g:if test="${actionName in ['manageMembers']}">
                <ui:actionsDropdownItem data-ui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
        </ui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName in ['documents']}">
    <g:if test="${editable}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="template.documents.add" data-ui="modal" href="#modalCreateDocument" />
        </ui:actionsDropdown>
    </g:if>
    <laser:render template="/templates/documents/modal" model="${[ownobj: contextOrg, owntp: 'org', inContextOrg: true]}"/>
</g:if>

<g:if test="${actionName == 'tasks'}">
    <g:if test="${editable}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="task.create.new" data-ui="modal" href="#modalCreateTask" />
        </ui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName == 'managePrivatePropertyDefinitions'}">
    <g:if test="${editable}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="menu.institutions.manage_props.create_new" data-ui="modal" href="#addPropertyDefinitionModal" />
        </ui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName == 'managePropertyGroups'}">
    <g:if test="${editable}">
        <ui:actionsDropdown>
            <ui:actionsDropdownItem message="propertyDefinitionGroup.create_new.label" controller="myInstitution" action="managePropertyGroups" params="${[cmd:'new']}" class="trigger-modal" notActive="true" />
        </ui:actionsDropdown>
    </g:if>
</g:if>