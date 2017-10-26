<%@ page import="de.laser.PermissionHelperService" %>
<g:set var="license" value="${com.k_int.kbplus.License.get(params.id)}"/>

<laser:subNav actionName="${actionName}">
    <laser:subNavItem controller="licenseDetails" action="index" params="${[id:params.id]}" message="license.nav.details" />
    <laser:subNavItem controller="licenseDetails" action="documents" params="${[id:params.id]}" message="license.nav.docs" />
    <laser:subNavItem controller="licenseDetails" action="notes" params="${[id:params.id]}" message="license.nav.notes" />
    <laser:subNavItem controller="licenseDetails" action="todo_history" params="${[id:params.id]}" message="license.nav.todo_history" />
    <laser:subNavItem controller="licenseDetails" action="edit_history" params="${[id:params.id]}" message="license.nav.edit_history" />
    <laser:subNavItem controller="licenseDetails" action="additionalInfo" params="${[id:params.id]}" message="license.nav.additionalInfo" />
    <laser:subNavItem controller="licenseDetails" action="properties" params="${[id:params.id]}" message="license.nav.privateProperties" />

    <g:if test="${license.orgLinks?.find{it.roleType?.value == 'Licensing Consortium' && permissonHelperService.hasUserWithRole(user, it?.org, 'INST_ADM') && license.licenseType == 'Template'}}">
        <laser:subNavItem controller="licenseDetails" action="consortia" params="${[id:params.id]}" message="consortium.plural" />
    </g:if>
</laser:subNav>