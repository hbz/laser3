<semui:subNav actionName="${actionName}">
    <semui:subNavItem controller="titleDetails" action="show" params="${[id:params.id]}" message="title.nav.details" />
    <semui:subNavItem controller="titleDetails"  action="history" params="${[id:params.id]}" message="title.nav.history" />
</semui:subNav>
