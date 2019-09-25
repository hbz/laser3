<g:set var="springSecurityService" bean="springSecurityService" />

<g:if test="${actionName == 'list'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" />
    </semui:actionsDropdown>
</g:if>
<g:elseif test="${actionName == 'userList'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="myInstitution" action="userCreate" message="user.create_new.label" />
    </semui:actionsDropdown>
</g:elseif>
<g:elseif test="${actionName == 'users'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="organisation" action="userCreate" message="user.create_new.label" params="${[id:params.id]}"/>
    </semui:actionsDropdown>
</g:elseif>
<g:if test="${actionName == 'edit'}">
    <g:if test="${springSecurityService.getCurrentUser().hasRole('ROLE_ADMIN') || springSecurityService.getCurrentUser().hasAffiliation("INST_ADM")}">
        <semui:actionsDropdown>
            <g:link class="item" action="_delete" id="${params.id}"><i class="trash alternate icon"></i> Nutzer löschen</g:link>
        </semui:actionsDropdown>
    </g:if>
</g:if>
