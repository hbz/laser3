<laser:serviceInjection/>

<g:if test="${actionName == 'list'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" />
    </semui:actionsDropdown>
</g:if>
<g:elseif test="${actionName == 'userList'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="myInstitution" action="createUser" message="user.create_new.label" />
    </semui:actionsDropdown>
</g:elseif>
<g:elseif test="${actionName == 'users'}">
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="organisation" action="createUser" message="user.create_new.label" params="${[id:params.id]}"/>
    </semui:actionsDropdown>
</g:elseif>
<g:if test="${actionName == 'edit'}">
    <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN') || contextService.getUser().hasAffiliation("INST_ADM")}">
        <semui:actionsDropdown>
            <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate icon"></i> ${message(code:'deletion.user')}</g:link>
        </semui:actionsDropdown>
    </g:if>
</g:if>
