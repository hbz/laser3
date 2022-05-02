<%@page import="de.laser.RefdataValue; de.laser.storage.RDStore" %>
<laser:serviceInjection />

<g:set var="contextOrg" value="${contextService.getOrg()}" />
<g:if test="${actionName == 'currentSubscriptions'}">
    <semui:actionsDropdown>
        <g:if test="${accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")}">
            <semui:actionsDropdownItem controller="subscription" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <semui:actionsDropdownItem controller="myInstitution" action="subscriptionImport" message="menu.institutions.subscriptionImport" />
            <div class="divider"></div>
        </g:if>
        <semui:actionsDropdownItem notActive="true" controller="myInstitution" action="currentSubscriptions" params="${[compare: true]+params}" message="menu.my.comp_sub" />
    </semui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['currentLicenses']}">
    <g:if test="${accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="myInstitution" action="emptyLicense" message="license.add.blank" />

            <div class="divider"></div>
            <semui:actionsDropdownItem notActive="true" controller="myInstitution" action="currentLicenses" params="${[compare: true]+params}" message="menu.my.comp_lic" />
        </semui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName in ['manageMembers', 'addMembers']}">
    <g:if test="${accessService.checkPermAffiliation("ORG_CONSORTIUM","INST_EDITOR")}">
        <semui:actionsDropdown>
            <g:if test="${comboType != null && comboType == RDStore.COMBO_TYPE_CONSORTIUM}">
                <semui:actionsDropdownItem controller="myInstitution" action="addMembers" message="menu.institutions.add_consortia_members" />
                <%-- leave for eventual reconsideration on behalf of users
                <semui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_institution.label"/>
                --%>
            </g:if>
            <g:if test="${actionName in ['manageMembers']}">
                <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
        </semui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName in ['documents']}">
    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
        </semui:actionsDropdown>
    </g:if>
    <laser:render template="/templates/documents/modal" model="${[ownobj: contextOrg, owntp: 'org', inContextOrg: true]}"/>
</g:if>

<g:if test="${actionName == 'tasks'}">
    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="task.create.new" data-semui="modal" href="#modalCreateTask" />
        </semui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName == 'managePrivatePropertyDefinitions'}">
    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="menu.institutions.manage_props.create_new" data-semui="modal" href="#addPropertyDefinitionModal" />
        </semui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName == 'managePropertyGroups'}">
    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="propertyDefinitionGroup.create_new.label" controller="myInstitution" action="managePropertyGroups" params="${[cmd:'new']}" class="trigger-modal" notActive="true" />
        </semui:actionsDropdown>
    </g:if>
</g:if>