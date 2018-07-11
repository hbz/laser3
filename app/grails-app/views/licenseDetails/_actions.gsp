<%-- TODO: FIX ACCESS: erms-470 --%>
<g:if test="${true || license?.getLicensor()?.id == contextService.getOrg()?.id}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="licenseDetails" action="links" params="${[id:license?.id]}" message="myinst.emptyLicense.child" />
    </semui:actionsDropdown>
</g:if>
