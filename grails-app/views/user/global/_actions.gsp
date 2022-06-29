<laser:serviceInjection/>

<g:if test="${actionName == 'list'}">%{-- /user/list --}%
    <semui:actionsDropdown>
        <semui:actionsDropdownItem controller="user" action="create" message="user.create_new.label" />
    </semui:actionsDropdown>
</g:if>
<g:elseif test="${actionName == 'edit'}">%{-- /user/edit --}%
    <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN')}">
        <semui:actionsDropdown>
            <g:link class="item" action="delete" id="${params.id}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.user')}</g:link>
        </semui:actionsDropdown>
    </g:if>
</g:elseif>
<g:elseif test="${actionName == 'users'}">
    <g:if test="${controllerName == 'myInstitution'}">%{-- /myInstitution/users --}%
        <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN') || contextService.getUser().hasAffiliation("INST_ADM")}">
            <semui:actionsDropdown>
                <semui:actionsDropdownItem controller="myInstitution" action="createUser" message="user.create_new.label" />
            </semui:actionsDropdown>
        </g:if>
    </g:if>
    <g:elseif test="${controllerName == 'organisation'}">%{-- organisation/users - TODO: hasComboInstAdminAffiliation --}%
        <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN')}">
            <semui:actionsDropdown>
                <semui:actionsDropdownItem controller="organisation" action="createUser" message="user.create_new.label" params="${[id:params.id]}"/>
            </semui:actionsDropdown>
        </g:if>
    </g:elseif>
</g:elseif>
<g:elseif test="${actionName == 'editUser'}">
    <g:if test="${controllerName == 'myInstitution'}">%{-- /myInstitution/editUser --}%
        <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN') || contextService.getUser().hasAffiliation("INST_ADM")}">
            <semui:actionsDropdown>
                <g:link class="item" action="deleteUser" params="${[uoid: params.uoid]}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.user')}</g:link>
            </semui:actionsDropdown>
        </g:if>
    </g:if>
    <g:elseif test="${controllerName == 'organisation'}">%{-- /organisation/editUser - TODO: hasComboInstAdminAffiliation --}%
        <g:if test="${contextService.getUser().hasRole('ROLE_ADMIN')}">
            <semui:actionsDropdown>
                <g:link class="item" action="deleteUser" params="${[id:params.id, uoid: params.uoid]}"><i class="trash alternate outline icon"></i> ${message(code:'deletion.user')}</g:link>
            </semui:actionsDropdown>
        </g:if>
    </g:elseif>
</g:elseif>
