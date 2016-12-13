<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'orgs', 'error')} ">
	<label for="orgs">
		<g:message code="cluster.orgs.label" default="Orgs" />
	</label>
	
	<p><strong>Warning:</strong> These changes will immediately affect the database.</p>
	
	<ul class="one-to-many">
		<g:each in="${clusterInstance?.orgs?}" var="o">
		    <li class="ui-delete">
		    	${o.roleType?.value} - <g:link controller="org" action="show" id="${o.org?.id}">${o.org?.name}</g:link>
		    	<!-- <button type="button" name="delete" value="${o.encodeAsHTML()}">Delete</button>-->
		    	<button type="button" name="delete" value="${o.id}">Delete</button>
		    </li>
		</g:each>
	</ul>
	
	<ul>	
		<li class="add ui-adding">
			<g:select name="orgRole_role"
			    from="${roles}" 
			    optionKey="id" 
			    optionValue="value"/> 
			<g:select name="orgRole_org"
			    from="${orgs}" 
			    optionKey="id" 
			    optionValue="name"/>
			  
			<button type="button" name="add">Add</button>
		</li>
	</ul>

	<script>
		$('#ui-placeholder button[name=add]').click(function(){
			var org = $("#ui-placeholder [name=orgRole_org] option:selected").attr("value");
			var role = $("#ui-placeholder [name=orgRole_role] option:selected").attr("value");
			$.post("${webRequest.baseUrl}/cluster/ajax", {
				op: "add",
				id: "${clusterInstance.id}",
				org:  org,
				role: role
			}).done(function(data){
				$("#ui-placeholder").html(data);
			})
		})
		$('#ui-placeholder button[name=delete]').click(function(){
			var orgRole = $(this).attr("value");
			$.post("${webRequest.baseUrl}/cluster/ajax", {
				op: "delete",
				id: "${clusterInstance.id}",
				orgRole: orgRole
			}).done(function(data){
				$("#ui-placeholder").html(data);
			})
		})
	</script>
</div>