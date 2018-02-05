<% def contextService = grailsApplication.mainContext.getBean("contextService") %>
<g:set var="contextOrg" value="${contextService.getOrg()}" />

<g:if test="${actionName == 'currentSubscriptions'}">
    <semui:actionsDropdown>
        <sec:ifAnyGranted roles="INST_USER">
            <semui:actionsDropdownItem controller="myInstitutions" action="emptySubscription" params="${[shortcode:contextOrg?.shortcode]}" message="menu.institutions.emptySubscription" />
            <div class="divider"></div>
        </sec:ifAnyGranted>

        <semui:actionsDropdownItem controller="subscriptionDetails" action="compare" params="${[shortcode:contextOrg?.shortcode]}" message="menu.institutions.comp_sub" />

        <semui:actionsDropdownItem controller="myInstitutions" action="renewalsSearch" params="${[shortcode:contextOrg?.shortcode]}" message="menu.institutions.gen_renewals" />
        <semui:actionsDropdownItem controller="myInstitutions" action="renewalsUpload" params="${[shortcode:contextOrg?.shortcode]}" message="menu.institutions.imp_renew" />

        <semui:actionsDropdownItem controller="subscriptionImport" action="generateImportWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.sub_work" />
        <semui:actionsDropdownItem controller="subscriptionImport" action="importSubscriptionWorksheet" params="${[id:contextOrg?.id]}" message="menu.institutions.imp_sub_work" />
    </semui:actionsDropdown>
</g:if>

<g:if test="${actionName in ['currentLicenses']}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="licenseCompare" action="index" params="${[shortcode:params.shortcode]}" message="menu.institutions.comp_lic" />
        <semui:actionsDropdownItem controller="myInstitutions" action="addLicense" params="${[shortcode:params.shortcode]}" message="license.copy" />
        <g:if test="${is_inst_admin}">
            <semui:actionsDropdownItem controller="myInstitutions" action="cleanLicense" params="${[shortcode:params.shortcode]}" message="license.add.blank" />
        </g:if>
    </semui:actionsDropdown>
</g:if>