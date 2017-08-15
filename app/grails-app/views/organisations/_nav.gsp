<laser:subNav actionName="${actionName}">
	<laser:subNavItem controller="organisations" action="show" params="${[id:params.id]}" message="org.nav.details" />
	<laser:subNavItem controller="organisations" action="users" params="${[id:params.id]}" message="org.nav.users" />
	<laser:subNavItem controller="organisations" action="config" params="${[id:params.id]}" message="org.nav.options" />
	<laser:subNavItem controller="organisations" action="addressbook" params="${[id:params.id]}" message="menu.institutions.addressbook" />
	<laser:subNavItem controller="organisations" action="properties" params="${[id:params.id]}" message="org.privateProperties.label" />
</laser:subNav>