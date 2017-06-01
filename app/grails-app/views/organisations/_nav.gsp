<ul class="nav nav-pills">
	<li <%='show'== actionName ? ' class="active"' : '' %>>
		<g:link controller="organisations" action="show" params="${[id:params.id]}">${message(code:'org.nav.details', default:'Details')}</g:link>
	</li>
	<li <%='users'== actionName ? ' class="active"' : '' %>>
		<g:link controller="organisations" action="users" params="${[id:params.id]}">${message(code:'org.nav.users', default:'Users')}</g:link>
	</li>
	<li <%='config'== actionName ? ' class="active"' : '' %>>
		<g:link controller="organisations" action="config" params="${[id:params.id]}">${message(code:'org.nav.options', default:'Options')}</g:link>
	</li>
	<li <%='addressbook'== actionName ? ' class="active"' : '' %>>
		<g:link controller="organisations" action="addressbook" params="${[id:params.id]}">${message(code:'menu.institutions.addressbook', default:'Addressbook')}</g:link>
	</li>
	<li <%='properties'== actionName ? ' class="active"' : '' %>>
		<g:link controller="organisations" action="properties" params="${[id:params.id]}">Properties</g:link>
	</li>
</ul>
