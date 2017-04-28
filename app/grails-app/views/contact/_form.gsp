<%@ page import="com.k_int.kbplus.Contact" %>



<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'contentType', 'error')} ">
	<label for="contentType">
		<g:message code="contact.contentType.label" default="ContentType" />
		
	</label>
	<g:select id="contentType" name="contentType.id" 
		from="${com.k_int.kbplus.Contact.getAllRefdataValues('ContactContentType')}"
    	optionKey="id"
    	optionValue="value"
    	value="${contactInstance?.contentType?.id}"
        required=""/>

</div>

<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'content', 'error')} ">
	<label for="content">
		<g:message code="contact.content.label" default="Content" />
		
	</label>
	<g:textField name="content" value="${contactInstance?.content}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'type', 'error')} ">
	<label for="type">
		<g:message code="contact.type.label" default="Type" />
		
	</label>
	<g:select id="type" name="type.id" 
		from="${com.k_int.kbplus.Contact.getAllRefdataValues('ContactType')}"
    	optionKey="id"
    	optionValue="value"
    	value="${contactInstance?.type?.id}"
        required=""/>
</div>

<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'prs', 'error')} ">
	<label for="prs">
		<g:message code="contact.prs.label" default="Prs" />
		
	</label>
	<g:select id="prs" name="prs.id" from="${com.k_int.kbplus.Person.list()}" optionKey="id" value="${contactInstance?.prs?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'org', 'error')} ">
	<label for="org">
		<g:message code="contact.org.label" default="Org" />
		
	</label>
	<g:select id="org" name="org.id" from="${com.k_int.kbplus.Org.list()}" optionKey="id" value="${contactInstance?.org?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

