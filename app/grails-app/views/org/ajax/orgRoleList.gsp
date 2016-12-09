<div class="fieldcontain ${hasErrors(bean: orgInstance, field: 'orgs', 'error')} ">
	<label for="orgs">
		<g:message code="cluster.orgs.label" default="Cluster" />
	</label>
	
	<p><strong>Warning:</strong> These changes will immediately affect the database.</p>
	
	<ul class="one-to-many">
		<g:each in="${orgInstance?.links?}" var="l">
			<g:if test="${l.roleType?.owner?.desc == 'Cluster Role'}">
			    <li class="ui-delete">
			    	${l.roleType?.value} - <g:link controller="cluster" action="show" id="${l.cluster?.id}">${l.cluster?.name}</g:link>
			    	<!-- <button type="button" name="delete" value="${l.encodeAsHTML()}">Delete</button>-->
			    	<button type="button" name="delete" value="${l.id}">Delete</button>
			    </li>
			</g:if>
		</g:each>
<!-- 		
		<li class="add">
			<g:link controller="orgRole" action="create" params="['org.id': orgInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgRole.label', default: 'OrgRole')])}</g:link>
		</li>
-->
	</ul>
	
	<ul>	
		<li class="add ui-adding">
			<g:select name="orgRole_role"
			    from="${roles}" 
			    optionKey="id" 
			    optionValue="value"/> 
			<g:select name="orgRole_cluster"
			    from="${cluster}" 
			    optionKey="id" 
			    optionValue="name"/>
			  
			<button type="button" name="add">Add</button>
		</li>
	</ul>

	<script>
		$('#ui-placeholder button[name=add]').click(function(){
			var cluster = $("#ui-placeholder [name=orgRole_cluster] option:selected").attr("value");
			var role    = $("#ui-placeholder [name=orgRole_role] option:selected").attr("value");
			$.post("/demo/org/ajax", {
				op: "add",
				id: "${orgInstance.id}",
				cluster:  cluster,
				role: role
			}).done(function(data){
				$("#ui-placeholder").html(data);
			})
		})
		$('#ui-placeholder button[name=delete]').click(function(){
			var orgRole = $(this).attr("value");
			$.post("/demo/org/ajax", {
				op: "delete",
				id: "${orgInstance.id}",
				orgRole: orgRole
			}).done(function(data){
				$("#ui-placeholder").html(data);
			})
		})
	</script>
</div>


