<semui:subNav actionName="${actionName}">
	<semui:subNavItem controller="person" action="show" params="${[id:params.id]}" text="Übersicht" />
	<semui:subNavItem controller="person" action="properties" params="${[id:params.id]}" message="person.privateProperties.label" />
</semui:subNav>