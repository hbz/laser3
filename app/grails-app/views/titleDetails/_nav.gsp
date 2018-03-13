<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="titleDetails" action="show" params="${[id:params.id]}" message="title.nav.details" />

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="titleDetails"  action="history" params="${[id:params.id]}" message="title.nav.history" />
    </sec:ifAnyGranted>
</semui:subNav>
