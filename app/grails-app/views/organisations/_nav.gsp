<semui:subNav actionName="${actionName}">
	<semui:subNavItem controller="organisations" action="show" params="${[id:params.id]}" message="org.nav.details" />
	<semui:securedSubNavItem controller="organisations" action="users" params="${[id:params.id]}" message="org.nav.users" affiliation="INST_USER" affiliationOrg="${orgInstance}" />
	<semui:securedSubNavItem controller="organisations" action="config" params="${[id:params.id]}" message="org.nav.options" affiliation="INST_ADM" affiliationOrg="${orgInstance}" />
	<semui:subNavItem controller="organisations" action="addressbook" params="${[id:params.id]}" message="menu.institutions.addressbook" />
</semui:subNav>