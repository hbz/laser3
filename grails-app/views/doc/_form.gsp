<%@ page import="de.laser.Doc;de.laser.RefdataValue" %>



<div class="field ${hasErrors(bean: docInstance, field: 'status', 'error')} ">
	<label for="status">
		<g:message code="default.status.label" />
		
	</label>
	<g:select id="status" name="status.id" from="${RefdataValue.list()}" optionKey="id" value="${docInstance?.status?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: docInstance, field: 'type', 'error')} ">
	<label for="type">
		<g:message code="default.type.label" />
		
	</label>
	<g:select id="type" name="type.id" from="${RefdataValue.list()}" optionKey="id" value="${docInstance?.type?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="field ${hasErrors(bean: docInstance, field: 'content', 'error')} ">
	<label for="content">
		<g:message code="default.content.label" />
		
	</label>
	<g:textField name="content" value="${docInstance?.content}"/>
</div>

<div class="field ${hasErrors(bean: docInstance, field: 'uuid', 'error')} ">
	<label for="uuid">
		<g:message code="default.uuid.label" />
		
	</label>
	<g:textField name="uuid" value="${docInstance?.uuid}"/>
</div>

<div class="field ${hasErrors(bean: docInstance, field: 'contentType', 'error')} ">
	<label for="contentType">
		<g:message code="default.contentType.label" />
		
	</label>
	<g:field type="number" name="contentType" value="${docInstance.contentType}"/>
</div>

<div class="field ${hasErrors(bean: docInstance, field: 'title', 'error')} ">
	<label for="title">
		<g:message code="default.title.label" />
		
	</label>
	<g:textField name="title" value="${docInstance?.title}"/>
</div>

<div class="field ${hasErrors(bean: docInstance, field: 'filename', 'error')} ">
	<label for="filename">
		<g:message code="default.fileName.label" />
		
	</label>
	<g:textField name="filename" value="${docInstance?.filename}"/>
</div>

