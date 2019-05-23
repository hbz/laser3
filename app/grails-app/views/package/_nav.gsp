<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="package" action="show" params="${[id:params.id]}" message="package.show.nav.details" />

    <semui:subNavItem controller="package" action="current" params="${[id:params.id]}" message="package.show.nav.titles" />

    <semui:subNavItem controller="package" action="expected" params="${[id:params.id]}" message="package.show.nav.expected" />

    <semui:subNavItem controller="package" action="previous" params="${[id:params.id]}" message="package.show.nav.previous" />

    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="package" action="tasks" params="${[id:params.id]}" message="task.plural" />

    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="package" action="documents" params="${[id:params.id]}" message="package.show.nav.docs" />

    <semui:subNavItem controller="package" action="notes" params="${[id:params.id]}" message="package.show.nav.notes" />

    <g:if test="${false && editable}">
        <g:if test="${packageInstance?.consortia != null}">
            <semui:subNavItem controller="package" action="consortia" params="${[id:params.id]}" message="package.show.nav.consortia" />
        </g:if>
    </g:if>

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="package" action="history" params="${[id:params.id]}" class="la-role-admin" message="package.show.nav.history" />
    </sec:ifAnyGranted>
</semui:subNav>
