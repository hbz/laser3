<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="packageDetails" action="show" params="${[id:params.id]}" message="package.show.nav.details" />

    <semui:subNavItem controller="packageDetails" action="current" params="${[id:params.id]}" message="package.show.nav.titles" />

    <semui:subNavItem controller="packageDetails" action="expected" params="${[id:params.id]}" message="package.show.nav.expected" />

    <semui:subNavItem controller="packageDetails" action="previous" params="${[id:params.id]}" message="package.show.nav.previous" />

    <semui:subNavItem controller="packageDetails" action="documents" params="${[id:params.id]}" message="package.show.nav.docs" />

    <semui:subNavItem controller="packageDetails" action="notes" params="${[id:params.id]}" message="package.show.nav.notes" />

    <semui:subNavItem controller="packageDetails" action="history" params="${[id:params.id]}" message="package.show.nav.history" />

    <g:if test="${editable}">
        <g:if test="${packageInstance?.consortia != null}">
            <semui:subNavItem controller="packageDetails" action="consortia" params="${[id:params.id]}" message="package.show.nav.consortia" />
        </g:if>
    </g:if>
</semui:subNav>
