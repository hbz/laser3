<g:set var="licenseId" value="${com.k_int.kbplus.OnixplLicense.get(params.id).license.id}"/>

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="onixplLicenseDetails" action="index" params="${[id:params.id]}" text="License Details" />
    <semui:subNavItem controller="onixplLicenseDetails" action="documents" params="${[id:params.id]}" text="Document" />
    <semui:subNavItem controller="onixplLicenseDetails" action="notes" params="${[id:params.id]}" text="Notes" />
    <semui:subNavItem controller="onixplLicenseDetails" action="permissionInfo" params="${[id:params.id]}" text="Additional Information" />

    <g:if test="${user.hasRole('ROLE_ADMIN')}">
        <semui:subNavItem controller="onixplLicenseDetails" action="history" params="${[id:params.id]}" text="History" />
    </g:if>
</semui:subNav>

