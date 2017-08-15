<g:set var="licenseId" value="${com.k_int.kbplus.OnixplLicense.get(params.id).license.id}"/>

<laser:subNav actionName="${actionName}">
    <laser:subNavItem controller="onixplLicenseDetails" action="index" params="${[id:params.id]}" text="License Details" />
    <laser:subNavItem controller="onixplLicenseDetails" action="documents" params="${[id:params.id]}" text="Document" />
    <laser:subNavItem controller="onixplLicenseDetails" action="notes" params="${[id:params.id]}" text="Notes" />
    <laser:subNavItem controller="onixplLicenseDetails" action="history" params="${[id:params.id]}" text="History" />
    <laser:subNavItem controller="onixplLicenseDetails" action="additionalInfo" params="${[id:params.id]}" text="Additional Information" />
</laser:subNav>

