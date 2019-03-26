<%@page import="com.k_int.kbplus.RefdataValue" %>
<laser:serviceInjection />

<g:set var="contextOrg" value="${contextService.getOrg()}" />
<semui:actionsDropdown>
    <g:if test="${actionName == 'currentSubscriptions'}">
        <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
            <semui:actionsDropdownItem controller="myInstitution" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <div class="divider"></div>
        </g:if>

        <semui:actionsDropdownItem controller="subscriptionDetails" action="compare" message="menu.institutions.comp_sub" />
        <semui:actionsDropdownItem controller="subscriptionImport" action="generateImportWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.sub_work" />
        <semui:actionsDropdownItem controller="subscriptionImport" action="importSubscriptionWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.imp_sub_work" />
    </g:if>

    <g:if test="${actionName in ['currentLicenses']}">
        <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
            <semui:actionsDropdownItem controller="myInstitution" action="emptyLicense" message="license.add.blank" />
            <div class="divider"></div>
        </g:if>

        <semui:actionsDropdownItem controller="licenseCompare" action="index" message="menu.institutions.comp_lic" />
        %{--<semui:actionsDropdownItem controller="myInstitution" action="addLicense" message="license.copy" />--}%
    </g:if>

    <g:if test="${actionName in ['manageConsortia', 'addConsortiaMembers']}">
        <g:if test="${springSecurityService.getCurrentUser().hasAffiliation("INST_ADM") && (RefdataValue.getByValueAndCategory('Consortium', 'OrgRoleType')?.id in  contextOrg.getallOrgTypeIds())}">
            <semui:actionsDropdownItem controller="myInstitution" action="addConsortiaMembers" message="menu.institutions.add_consortia_members" />
            <g:if test="${actionName in ['manageConsortia']}">
                <semui:actionsDropdownItem data-semui="modal" href="#copyEmailaddresses_ajaxModal" message="menu.institutions.copy_emailaddresses"/>
            </g:if>
        </g:if>
    </g:if>

    <g:if test="${actionName in ['addressbook']}">
        <g:if test="${editable}">
            <div class="item"  href="#personFormModal" data-semui="modal" data-value="1">${message(code: 'person.create_new.contactPerson.label')}</div>
        </g:if>
    </g:if>

    <g:if test="${actionName in ['documents']}">
        <g:if test="${editable}">
            <semui:actionsDropdownItem message="template.documents.add" data-semui="modal" href="#modalCreateDocument" />
        </g:if>
    </g:if>
</semui:actionsDropdown>
<%-- continue here: ask whether it is OK to map documents without target as documents attached to contextOrg? --%>
<g:render template="/templates/documents/modal" model="${[ownobj: contextOrg, owntp: 'org']}"/>