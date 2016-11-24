<%@ page import="com.k_int.kbplus.Group" %>



<div class="fieldcontain ${hasErrors(bean: groupInstance, field: 'owner', 'error')} required">
	<label for="owner">
		<g:message code="group.owner.label" default="Owner" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="owner" name="owner.id" from="${com.k_int.kbplus.Org.list()}" optionKey="id" required="" value="${groupInstance?.owner?.id}" class="many-to-one"/>

</div>

<div class="fieldcontain ${hasErrors(bean: groupInstance, field: 'definition', 'error')} required">
	<label for="definition">
		<g:message code="group.definition.label" default="Definition" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="definition" required="" value="${groupInstance?.definition}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: groupInstance, field: 'name', 'error')} required">
	<label for="name">
		<g:message code="group.name.label" default="Name" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="name" required="" value="${groupInstance?.name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: groupInstance, field: 'type', 'error')} required">
	<label for="type">
		<g:message code="group.type.label" default="Type" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="type" name="type.id" from="${com.k_int.kbplus.RefdataValue.list()}" optionKey="id" required="" value="${groupInstance?.type?.id}" class="many-to-one"/>

</div>

