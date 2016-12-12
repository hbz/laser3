<%@ page import="com.k_int.kbplus.Contact" %>



<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'mail', 'error')} ">
	<label for="mail">
		<g:message code="contact.mail.label" default="Mail" />
		
	</label>
	<g:textField name="mail" value="${contactInstance?.mail}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'phone', 'error')} ">
	<label for="phone">
		<g:message code="contact.phone.label" default="Phone" />
		
	</label>
	<g:textField name="phone" value="${contactInstance?.phone}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: contactInstance, field: 'type', 'error')} ">
	<label for="type">
		<g:message code="contact.type.label" default="Type" />
		
	</label>
	<g:select id="type" name="type.id" 
		from="${com.k_int.kbplus.Contact.getAllRefdataValues()}"
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

