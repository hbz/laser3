<%@ page import="de.laser.wekb.TitleInstancePackagePlatform; de.laser.storage.RDStore;" %>
<ui:subNav actionName="${actionName}">

    <ui:subNavItem controller="package" action="show" params="${[id:params.id]}" message="package.show.nav.details" />

    <ui:subNavItem controller="package" action="current" counts="${currentTippsCounts}" params="${[id:params.id]}" message="package.show.nav.current" />

    <ui:subNavItem controller="package" action="planned" counts="${plannedTippsCounts}" params="${[id:params.id]}" message="package.show.nav.planned" />

    <ui:subNavItem controller="package" action="expired" counts="${expiredTippsCounts}" params="${[id:params.id]}" message="package.show.nav.expired" />

    <ui:subNavItem controller="package" action="deleted" counts="${deletedTippsCounts}" params="${[id:params.id]}" message="package.show.nav.deleted" />

    <ui:subNavItem controller="package" action="linkedSubscriptions" counts="${subscriptionCounts}" params="${[id:params.id]}" message="package.show.nav.linkedSubscriptions" />

</ui:subNav>
