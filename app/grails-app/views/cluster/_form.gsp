<%@ page import="com.k_int.kbplus.Cluster" %>



<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'definition', 'error')} required">
	<label for="definition">
		<g:message code="cluster.definition.label" default="Definition" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="definition" required="" value="${clusterInstance?.definition}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="cluster.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${clusterInstance?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'type', 'error')} required">
	<label for="type">
		<g:message code="cluster.type.label" default="Type" />
		<span class="required-indicator">*</span>
	</label>
	<laser:select id="type" name="type.id"
		from="${com.k_int.kbplus.Cluster.getAllRefdataValues()}"
    	optionKey="id"
    	optionValue="value"
    	required=""
    	value="${clusterInstance?.type?.id}"
        class="many-to-one" />
</div>

<!--
<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'orgs', 'error')} ">
	<label for="orgs">
		<g:message code="cluster.orgs.label" default="Orgs" />
		
	</label>
	
	<ul class="one-to-many">
	<g:each in="${clusterInstance?.orgs?}" var="o">
	    <li>
	    	<g:link controller="orgRole" action="show" id="${o.id}">${o?.encodeAsHTML()}</g:link>
	    </li>
	</g:each>
	<li class="add">
	<g:link controller="orgRole" action="create" params="['cluster.id': clusterInstance?.id]">${message(code: 'default.add.label', args: [message(code: 'orgRole.label', default: 'OrgRole')])}</g:link>
	</li>
	</ul>
</div>
-->

<div id="ui-placeholder">
	<g:if test="${clusterInstance?.id != null}">
		<script>
			$.get("${webRequest.baseUrl}/cluster/ajax/${clusterInstance?.id}").done(function(data){
					$("#ui-placeholder").append(data);
			});
		</script>
	</g:if>
</div>



