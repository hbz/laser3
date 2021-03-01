<%@ page import="de.laser.TitleInstancePackagePlatform; de.laser.helper.RDStore;" %>
<semui:subNav actionName="${actionName}">

    <semui:subNavItem controller="package" action="show" params="${[id:params.id]}" message="package.show.nav.details" />

    <semui:subNavItem controller="package" action="current" counts="${currentTippsCounts}" params="${[id:params.id]}" message="package.show.nav.current" />

    <semui:subNavItem controller="package" action="planned" counts="${plannedTippsCounts}" params="${[id:params.id]}" message="package.show.nav.planned" />

    <semui:subNavItem controller="package" action="expired" counts="${expiredTippsCounts}" params="${[id:params.id]}" message="package.show.nav.expired" />

    <semui:subNavItem controller="package" action="deleted" counts="${deletedTippsCounts}" params="${[id:params.id]}" message="package.show.nav.deleted" />

    <semui:subNavItem controller="package" action="tippChanges" params="${[id:params.id]}" message="package.show.nav.tippChanges" />
%{--    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="package" action="tasks" params="${[id:params.id]}" message="task.plural" />

    <semui:securedSubNavItem orgPerm="ORG_INST,ORG_CONSORTIUM" controller="package" action="documents" params="${[id:params.id]}" message="package.show.nav.docs" />

    <semui:subNavItem controller="package" action="notes" params="${[id:params.id]}" message="package.show.nav.notes" />--}%

    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <semui:subNavItem controller="package" action="history" params="${[id:params.id]}" class="la-role-admin" message="package.show.nav.history" />
    </sec:ifAnyGranted>

</semui:subNav>
