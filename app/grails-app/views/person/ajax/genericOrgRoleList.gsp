<div>
	<label for="orgs"><strong>${type}</strong></label>
	
	<p><strong>Warning:</strong> These changes will immediately affect the database.</p>
	
	<ul class="one-to-many">
		<g:each in="${prsLinks}" var="link">
			<li class="ui-delete">
				${link.roleType} - 
				<g:link controller="org" action="show" id="${link.org?.id}">${link.org?.name}</g:link> -
				
				<g:if test="${link.cluster}">
					<g:link controller="${linkController}" action="show" id="${link.cluster.id}">${link.cluster.name}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.lic}">
					<g:link controller="${linkController}" action="show" id="${link.lic.id}">${link.lic.reference}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.pkg}">
					<g:link controller="${linkController}" action="show" id="${link.pkg.id}">${link.pkg.name}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.sub}">
					<g:link controller="${linkController}" action="show" id="${link.sub.id}">${link.sub.name}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
				</g:if>
				<g:if test="${link.title}">
			    	<g:link controller="${linkController}" action="show" id="${link.title.id}">${link.title.normTitle}</g:link>
			    	<button type="button" name="delete" value="${link.id}">Delete</button>
			    </g:if>
			</li>
		</g:each>
	</ul>

	<ul>	
		<li class="add ui-adding">

			<g:select name="prsRole_org"
			    from="${orgs}" 
			    optionKey="id" 
			    optionValue=""/> 
			    
			<g:select name="prsRole_target"
			    from="${targets}" 
			    optionKey="id" 
			    optionValue="${targetOptionValue}"/> 
			    
			<g:select name="prsRole_role"
			    from="${roles}" 
			    optionKey="id" 
			    optionValue="value"/> 
			  
			<button type="button" name="add">Add</button>
		</li>
	</ul>

	<script>
		$('#ui-placeholder-${type} button[name=add]').click(function(){
			var target = $("#ui-placeholder-${type} [name=prsRole_target] option:selected").attr("value");
			var org    = $("#ui-placeholder-${type} [name=prsRole_org] option:selected").attr("value");
			var role   = $("#ui-placeholder-${type} [name=prsRole_role] option:selected").attr("value");

			$.post("/demo/person/ajax", {
				op:     "add",
				id:     "${personInstance.id}",
				org:	org,
				type:   "${type}",
				target: target,
				role:   role
			}).done(function(data){
				$("#ui-placeholder-${type}").html(data);
			})
		})
		$('#ui-placeholder-${type} button[name=delete]').click(function(){
			var prsRole = $(this).attr("value");

			$.post("/demo/person/ajax", {
				op:      "delete",
				id:      "${personInstance.id}",
				type:    "${type}",
				prsRole: prsRole
			}).done(function(data){
				$("#ui-placeholder-${type}").html(data);
			})
		})
	</script>
	
</div>


