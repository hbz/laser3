<ul>
	<g:each in="${prsLinks}" var="link">
		<li class="ui-delete">
			${link.roleType} - 
						
			<g:if test="${link.cluster}">
				<g:link controller="${linkController}" action="show" id="${link.cluster.id}">${link.cluster.name}</g:link>
			</g:if>
			<g:if test="${link.lic}">
				<g:link controller="${linkController}" action="show" id="${link.lic.id}">${link.lic.reference}</g:link>
			</g:if>
			<g:if test="${link.pkg}">
				<g:link controller="${linkController}" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
			</g:if>
			<g:if test="${link.sub}">
				<g:link controller="${linkController}" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
			</g:if>
			<g:if test="${link.title}">
		    	<g:link controller="${linkController}" action="show" id="${link.title.id}">${link.title.normTitle}</g:link>
		    </g:if>
		    
		    -
		    
		    <g:link controller="org" action="show" id="${link.org?.id}">${link.org?.name}</g:link>
		    
		    <g:checkBox name="personRoleDeleteIds.${link?.id}" value="${link?.id}" checked="false" /> Delete
		</li>
	</g:each>
</ul>


