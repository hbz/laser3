<%@ page import="de.laser.Contact;de.laser.Person;de.laser.helper.RDConstants;de.laser.RefdataCategory;de.laser.Org" %>



<div class="field ${hasErrors(bean: contactInstance, field: 'contentType', 'error')} ">
	<label for="contentType">
		<g:message code="contact.contentType.label" />
		
	</label>
	<laser:select class="ui dropdown" id="contentType" name="contentType.id"
		from="${Contact.getAllRefdataValues(RDConstants.CONTACT_CONTENT_TYPE)}"
    	optionKey="id"
    	optionValue="value"
    	value="${contactInstance?.contentType?.id}"
        required=""/>

</div>

<div class="field ${hasErrors(bean: contactInstance, field: 'content', 'error')} ">
	<label for="content">
		<g:message code="contact.content.label" />
		
	</label>
	<g:textField id="content" name="content" value="${contactInstance?.content}"/>

</div>

<div class="field ${hasErrors(bean: contactInstance, field: 'type', 'error')} ">
	<label for="type">
		${RefdataCategory.getByDesc(RDConstants.CONTACT_TYPE).getI10n('desc')}
		
	</label>
	<laser:select class="ui dropdown" id="type" name="type.id"
		from="${Contact.getAllRefdataValues(RDConstants.CONTACT_TYPE)}"
    	optionKey="id"
    	optionValue="value"
    	value="${contactInstance?.type?.id}"
        required=""/>
</div>

<div class="field ${hasErrors(bean: contactInstance, field: 'prs', 'error')} ">
	<label for="prs">
		<g:message code="contact.prs.label" />
		
	</label>
	<g:select id="prs" name="prs.id" from="${Person.list()}" optionKey="id" value="${contactInstance?.prs?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

<div class="field ${hasErrors(bean: contactInstance, field: 'org', 'error')} ">
	<label for="org">
		<g:message code="contact.org.label" />
		
	</label>
	<g:select id="org" name="org.id" from="${Org.list()}" optionKey="id" value="${contactInstance?.org?.id}" class="many-to-one" noSelection="['null': '']"/>

</div>

