<semui:subNav actionName="${actionName}">
	<semui:subNavItem controller="platform" action="show" params="${[id:params.id]}" message="platform.nav.details" />
	<semui:subNavItem controller="platform" action="platformTipps" params="${[id:params.id]}" message="platform.nav.platformTipps" />
	<semui:subNavItem controller="platform" action="accessMethods" params="${[id:params.id]}" message="platform.nav.accessMethods" />
	<semui:subNavItem controller="platform" action="link" params="${[id:params.id]}" message="org.nav.link" />
</semui:subNav>