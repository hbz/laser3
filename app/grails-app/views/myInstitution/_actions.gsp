<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<g:set var="contextOrg" value="${contextService.getOrg()}" />

<g:if test="${actionName == 'currentSubscriptions'}">
    <semui:actionsDropdown>
        <sec:ifAnyGranted roles="INST_USER">
            <semui:actionsDropdownItem controller="myInstitution" action="emptySubscription" message="menu.institutions.emptySubscription" />
            <div class="divider"></div>
        </sec:ifAnyGranted>

        <semui:actionsDropdownItem controller="subscriptionDetails" action="compare" message="menu.institutions.comp_sub" />

    %{--<semui:actionsDropdownItem controller="myInstitution" action="renewalsSearch" message="menu.institutions.gen_renewals" />--}%
    %{--<semui:actionsDropdownItem controller="myInstitution" action="renewalsUpload" message="menu.institutions.imp_renew" />--}%

        <semui:actionsDropdownItem controller="subscriptionImport" action="generateImportWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.sub_work" />
        <semui:actionsDropdownItem controller="subscriptionImport" action="importSubscriptionWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.imp_sub_work" />
    </semui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['currentLicenses']}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="licenseCompare" action="index" message="menu.institutions.comp_lic" />
        <semui:actionsDropdownItem controller="myInstitution" action="addLicense" message="license.copy" />
        <g:if test="${is_inst_admin}">
            <semui:actionsDropdownItem controller="myInstitution" action="cleanLicense" message="license.add.blank" />
        </g:if>
    </semui:actionsDropdown>
</g:if>