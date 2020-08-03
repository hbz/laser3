<%@page import="com.k_int.kbplus.RefdataValue; de.laser.helper.RDStore" %>
<laser:serviceInjection />

<g:set var="contextOrg" value="${contextService.getOrg()}" />
<g:if test="${actionName == 'currentSubscriptions'}">
    <semui:actionsDropdown>
        <g:if test="${accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")}">
            <semui:actionsDropdownItem controller="myInstitution" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <semui:actionsDropdownItem controller="myInstitution" action="subscriptionImport" message="menu.institutions.subscriptionImport" />
            <div class="divider"></div>
        </g:if>
        <semui:actionsDropdownItemDisabled controller="subscription" action="compare" message="menu.my.comp_sub" />
    </semui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['currentLicenses']}">
    <g:if test="${accessService.checkPermAffiliation("ORG_INST,ORG_CONSORTIUM","INST_EDITOR")}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="myInstitution" action="emptyLicense" message="license.add.blank" />
        </semui:actionsDropdown>
    </g:if>

        %{--<semui:actionsDropdownItem controller="licenseCompare" action="index" message="menu.my.comp_lic" />
        <semui:actionsDropdownItem controller="myInstitution" action="addLicense" message="license.copy" />--}%
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
            <g:elseif test="${comboType != null && comboType == RDStore.COMBO_TYPE_DEPARTMENT}">
                <semui:actionsDropdownItem controller="organisation" action="findOrganisationMatches" message="org.create_new_department.label"/>
            </g:elseif>
            <g:if test="${actionName in ['manageMembers']}">
                <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses.button"/>
            </g:if>
        </semui:actionsDropdown>
    </g:if>
</g:if>
<g:if test="${actionName in ['addressbook']}">
    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="person.create_new.contactPerson.label" data-semui="modal" href="#personFormModal" />
        </semui:actionsDropdown>
    </g:if>
</g:if>

<g:if test="${actionName in ['documents']}">
    <g:if test="${editable}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
        </semui:actionsDropdown>
    </g:if>
    <g:render template="/templates/documents/modal" model="${[ownobj: contextOrg, owntp: 'org', inContextOrg: true]}"/>
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
            <g:link controller="myInstitution" action="managePropertyGroups" params="${[cmd:'new']}" class="ui button trigger-modal">
                ${message(code:'propertyDefinitionGroup.create_new.label')}
            </g:link>
        </semui:actionsDropdown>
    </g:if>
</g:if>