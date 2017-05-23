<ul class="nav nav-pills">
	<li <%='show'== actionName ? ' class="active"' : '' %>>
		<g:link controller="person" action="show" params="${[id:params.id]}">Details</g:link>
	</li>
	<li <%='properties'== actionName ? ' class="active"' : '' %>>
		<g:link controller="person" action="properties" params="${[id:params.id]}">Properties</g:link>
	</li>
</ul>