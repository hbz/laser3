<g:set var="springSecurityService" bean="springSecurityService" />

<g:if test="${actionName == 'list'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" />
    </semui:actionsDropdown>
</g:if>
<g:if test="${actionName == 'show' || actionName == 'edit'}">
    <g:if test="${springSecurityService.getCurrentUser().hasRole('ROLE_ADMIN') || springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <semui:actionsDropdown>
            <g:link class="item" action="_delete" id="${params.id}"><i class="trash alternate icon"></i> Nutzer löschen</g:link>
        </semui:actionsDropdown>
    </g:if>
</g:if>
