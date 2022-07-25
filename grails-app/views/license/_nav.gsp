<laser:serviceInjection />

<g:set var="license" value="${de.laser.License.get(params.id)}"/>

<ui:subNav actionName="${actionName}">
    <ui:subNavItem controller="license" action="show" params="${[id:params.id]}" message="license.nav.details" />
    <ui:subNavItem controller="license" action="linkedSubs" params="${[id:params.id]}" message="subscription.plural"/>
    <g:if test="${license._getCalculatedType() == de.laser.interfaces.CalculatedType.TYPE_CONSORTIAL}">
        <ui:subNavItem controller="license" action="members" params="${[id:params.id]}" text="${message(code:'license.details.incoming.childs')}"/>
        <sec:ifAnyGranted roles="ROLE_ADMIN">
            <ui:subNavItem controller="license" action="pendingChanges" params="${[id:params.id]}" message="pendingChange.plural" />
        </sec:ifAnyGranted>
    </g:if>
    <ui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="license" action="tasks" params="${[id:params.id]}" message="task.plural" />
    <ui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="license" action="documents" params="${[id:params.id]}" message="license.nav.docs" />
    <ui:subNavItem controller="license" action="notes" params="${[id:params.id]}" message="license.nav.notes" />

    <ui:subNavItem controller="license" action="changes" params="${[id:params.id]}" message="license.nav.todo_history" />

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <ui:subNavItem controller="license" action="history" params="${[id:params.id]}" class="la-role-admin" message="license.nav.edit_history" />
    </sec:ifAnyGranted>

</ui:subNav>