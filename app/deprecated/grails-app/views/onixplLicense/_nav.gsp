<g:set var="licenseId" value="${com.k_int.kbplus.OnixplLicense.get(params.id).license.id}"/>

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="onixplLicense" action="index" params="${[id:params.id]}" text="License Details" />
    <semui:subNavItem controller="onixplLicense" action="documents" params="${[id:params.id]}" text="Document" />
    <semui:subNavItem controller="onixplLicense" action="notes" params="${[id:params.id]}" text="Notes" />
    <semui:subNavItem controller="onixplLicense" action="permissionInfo" params="${[id:params.id]}" text="Additional Information" />

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="onixplLicense" action="history" params="${[id:params.id]}" class="la-role-admin" text="History" />
    </sec:ifAnyGranted>
</semui:subNav>

