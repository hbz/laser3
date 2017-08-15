<g:set var="licence" value="${com.k_int.kbplus.License.get(params.id)}"/>

<laser:subNav actionName="${actionName}">
    <laser:subNavItem controller="licenseDetails" action="index" params="${[id:params.id]}" message="licence.nav.details" />
    <laser:subNavItem controller="licenseDetails" action="documents" params="${[id:params.id]}" message="licence.nav.docs" />
    <laser:subNavItem controller="licenseDetails" action="notes" params="${[id:params.id]}" message="licence.nav.notes" />
    <laser:subNavItem controller="licenseDetails" action="todo_history" params="${[id:params.id]}" message="licence.nav.todo_history" />
    <laser:subNavItem controller="licenseDetails" action="edit_history" params="${[id:params.id]}" message="licence.nav.edit_history" />
    <laser:subNavItem controller="licenseDetails" action="additionalInfo" params="${[id:params.id]}" message="licence.nav.additionalInfo" />
    <laser:subNavItem controller="licenseDetails" action="properties" params="${[id:params.id]}" message="licence.nav.privateProperties" />

    <g:if test="${licence.orgLinks?.find{it.roleType?.value == 'Licensing Consortium' &&
            it?.org?.hasUserWithRole(user,'INST_ADM') && licence.licenseType == 'Template'}}">
        <laser:subNavItem controller="licenseDetails" action="consortia" params="${[id:params.id]}" message="consortium.plural" />
    </g:if>
</laser:subNav>