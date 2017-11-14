<div>
	<label for="orgs"><strong>${type}</strong></label>
	
	<p><strong>Warning:</strong> These changes will immediately affect the database.</p>
	
	<ul class="one-to-many">
		<g:each in="${orgLinks}" var="link">
			<li class="ui-delete">
				${link.roleType} - 
				<g:if test="${link.cluster}">
					<g:link controller="${linkController}" action="show" id="${link.id}">${link.cluster.name}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.lic}">
					<g:link controller="${linkController}" action="show" id="${link.id}">${link.lic.reference}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.pkg}">
					<g:link controller="${linkController}" action="show" id="${link.id}">${link.pkg.name}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.sub}">
					<g:link controller="${linkController}" action="show" id="${link.id}">${link.sub.name}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.title}">
			    	<g:link controller="${linkController}" action="show" id="${link.id}">${link.title.normTitle}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
			    </g:if>
			</li>
		</g:each>
	</ul>

	<ul>	
		<li class="add ui-adding">
			<g:select name="orgRole_target"
			    from="${targets}" 
			    optionKey="id" 
			    optionValue="${targetOptionValue}"/> 
			    
			<g:select name="orgRole_role"
			    from="${roles}" 
			    optionKey="id" 
			    optionValue="${{it.getI10n('value')}}"/> 
			  
			<button type="button" name="add">Add</button>
		</li>
	</ul>

	<script>
		$('#ui-placeholder-${type} button[name=add]').click(function(){
			var target = $("#ui-placeholder-${type} [name=orgRole_target] option:selected").attr("value");
			var role   = $("#ui-placeholder-${type} [name=orgRole_role] option:selected").attr("value");
			$.post("${webRequest.baseUrl}/org/ajax", {
				op:     "add",
				id:     "${orgInstance.id}",
				type:   "${type}",
				target: target,
				role:   role
			}).done(function(data){
				$("#ui-placeholder-${type}").html(data);
			})
		})
		$('#ui-placeholder-${type} button[name=delete]').click(function(){
			var orgRole = $(this).attr("value");
			$.post("${webRequest.baseUrl}/org/ajax", {
				op:      "delete",
				id:      "${orgInstance.id}",
				type:    "${type}",
				orgRole: orgRole
			}).done(function(data){
				$("#ui-placeholder-${type}").html(data);
			})
		})
	</script>
	
</div>


