<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<% def securityService = grailsApplication.mainContext.getBean("springSecurityService") %>
<g:set var="contextOrg" value="${contextService.getOrg()}" />

<g:if test="${actionName == 'currentSubscriptions'}">
    <semui:actionsDropdown>
        <g:if test="${securityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
            <semui:actionsDropdownItem controller="myInstitution" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <div class="divider"></div>
        </g:if>

        <semui:actionsDropdownItem controller="subscriptionDetails" action="compare" message="menu.institutions.comp_sub" />
        <semui:actionsDropdownItem controller="subscriptionImport" action="generateImportWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.sub_work" />
        <semui:actionsDropdownItem controller="subscriptionImport" action="importSubscriptionWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.imp_sub_work" />
    </semui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['currentLicenses']}">
    <semui:actionsDropdown>
        <g:if test="${securityService.getCurrentUser().hasAffiliation("INST_EDITOR")}">
            <semui:actionsDropdownItem controller="myInstitution" action="emptyLicense" message="license.add.blank" />
            <div class="divider"></div>
        </g:if>

        <semui:actionsDropdownItem controller="licenseCompare" action="index" message="menu.institutions.comp_lic" />
        %{--<semui:actionsDropdownItem controller="myInstitution" action="addLicense" message="license.copy" />--}%
    </semui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['manageConsortia', 'addConsortiaMembers']}">
    <g:if test="${securityService.getCurrentUser().hasAffiliation("INST_ADM") && contextService.getOrg().orgType?.value == 'Consortium'}">
        <semui:actionsDropdown>
            <semui:actionsDropdownItem controller="myInstitution" action="addConsortiaMembers" message="menu.institutions.add_consortia_members" />
        </semui:actionsDropdown>
    </g:if>
</g:if>