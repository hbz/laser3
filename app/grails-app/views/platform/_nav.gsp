<semui:subNav actionName="${actionName}">
	<semui:subNavItem controller="platform" action="show" params="${[id:params.id]}" message="platform.nav.details" />
	<semui:subNavItem controller="platform" action="platformTipps" params="${[id:params.id]}" message="platform.nav.platformTipps" />
</semui:subNav>