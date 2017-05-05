<ul>
	<g:each in="${existingPrsLinks}" var="link">
		<li class="ui-delete">
		
			<strong>${link.functionType?.value}${link.responsibilityType?.value}</strong>
						
			<g:if test="${link.cluster}">
				for <g:link controller="${linkController}" action="show" id="${link.cluster.id}">${link.cluster.name}</g:link>
			</g:if>
			<g:if test="${link.lic}">
				for <g:link controller="${linkController}" action="show" id="${link.lic.id}">${link.lic.reference}</g:link>
			</g:if>
			<g:if test="${link.pkg}">
				for <g:link controller="${linkController}" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
			</g:if>
			<g:if test="${link.sub}">
				for <g:link controller="${linkController}" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
			</g:if>
			<g:if test="${link.title}">
		    	for <g:link controller="${linkController}" action="show" id="${link.title.id}">${link.title.normTitle}</g:link>
		    </g:if>
		    
		    at <g:link controller="organisations" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
		    
		    <br />
		    <g:checkBox name="personRoleDeleteIds.${link?.id}" value="${link?.id}" checked="false" /> Delete
		</li>
	</g:each>
</ul>


