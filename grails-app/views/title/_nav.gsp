<ui:subNav actionName="${actionName}">
    <ui:subNavItem controller="title" action="show" params="${[id:params.id]}" message="title.nav.details" />

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <ui:subNavItem controller="title"  action="history" params="${[id:params.id]}" class="la-role-admin" message="title.nav.history" />
    </sec:ifAnyGranted>
</ui:subNav>
