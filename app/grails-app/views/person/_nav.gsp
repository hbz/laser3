<ul class="nav nav-pills">
	<li <%='show'== actionName ? ' class="active"' : '' %>>
		<g:link controller="person" action="show" params="${[id:params.id]}">Details</g:link>
	</li>
	<li <%='additionalInfo'== actionName ? ' class="active"' : '' %>>
		<g:link controller="person" action="additionalInfo" params="${[id:params.id]}">Additional Information</g:link>
	</li>
</ul>