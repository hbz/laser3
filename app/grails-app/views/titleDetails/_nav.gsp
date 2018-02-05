<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="titleDetails" action="show" params="${[id:params.id]}" message="title.nav.details" />

    <g:if test="${user.hasRole('ROLE_ADMIN')}">
        <semui:subNavItem controller="titleDetails"  action="history" params="${[id:params.id]}" message="title.nav.history" />
    </g:if>
</semui:subNav>
