<laser:serviceInjection />

<g:set var="license" value="${de.laser.License.get(params.id)}"/>

<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="license" action="show" params="${[id:params.id]}" message="license.nav.details" />
    <semui:subNavItem controller="license" action="linkedSubs" params="${[id:params.id]}" message="subscription.plural"/>
    <g:if test="${license._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}">
        <semui:subNavItem controller="license" action="members" params="${[id:params.id]}" text="${message(code:'license.details.incoming.childs')}"/>
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <semui:subNavItem controller="license" action="pendingChanges" params="${[id:params.id]}" message="pendingChange.plural" />
        </sec:ifAnyGranted>
    </g:if>
    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="license" action="tasks" params="${[id:params.id]}" message="task.plural" />
    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="license" action="documents" params="${[id:params.id]}" message="license.nav.docs" />
    <semui:subNavItem controller="license" action="notes" params="${[id:params.id]}" message="license.nav.notes" />

    <semui:subNavItem controller="license" action="changes" params="${[id:params.id]}" message="license.nav.todo_history" />

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="license" action="history" params="${[id:params.id]}" class="la-role-admin" message="license.nav.edit_history" />
    </sec:ifAnyGranted>

</semui:subNav>