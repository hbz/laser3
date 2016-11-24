<%@ page import="com.k_int.kbplus.Cluster" %>



<div class="fieldcontain ${hasErrors(bean: clusterInstance, field: 'owner', 'error')} required">
	<label for="owner">
		<g:message code="cluster.owner.label" default="Owner" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="owner" name="owner.id" from="${com.k_int.kbplus.Org.list()}" optionKey="id" required="" value="${clusterInstance?.owner?.id}" class="many-to-one"/>

</div>

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
	<g:select id="type" name="type.id" from="${com.k_int.kbplus.RefdataValue.list()}" optionKey="id" required="" value="${clusterInstance?.type?.id}" class="many-to-one"/>

</div>

