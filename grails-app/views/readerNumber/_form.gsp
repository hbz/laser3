<%@ page import="de.laser.ReaderNumber;de.laser.Org" %>



<div class="${hasErrors(bean: numbersInstance, field: 'type', 'error')} required">
	<label for="type">
		<g:message code="readerNumber.referenceGroup.label" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="type" name="type.id" from="${de.laser.RefdataValue.list()}" optionKey="id" required="" value="${numbersInstance?.type?.id}" class="many-to-one"/>

</div>

<div class="${hasErrors(bean: numbersInstance, field: 'number', 'error')} ">
	<label for="number">
		<g:message code="readerNumber.number.label" />
		
	</label>
	<g:field id="number" name="number" type="number" value="${numbersInstance.number}"/>

</div>

<div class="${hasErrors(bean: numbersInstance, field: 'startDate', 'error')} required">
	<label for="startDate">
		<g:message code="default.startDate.label" />
		<span class="required-indicator">*</span>
	</label>
	<g:datePicker id="startDate" name="startDate" precision="day"  value="${numbersInstance?.startDate}"  />

</div>

<div class="${hasErrors(bean: numbersInstance, field: 'endDate', 'error')} ">
	<label for="endDate">
		<g:message code="default.endDate.label" />
		
	</label>
	<g:datePicker id="endDate" name="endDate" precision="day"  value="${numbersInstance?.endDate}" default="none" noSelection="['': '']" />

</div>

<div class="${hasErrors(bean: numbersInstance, field: 'org', 'error')} required">
	<label for="org">
		<g:message code="numbers.org.label" default="Org" />
		<span class="required-indicator">*</span>
	</label>
	<g:select id="org" name="org.id" from="${Org.list()}" optionKey="id" required="" value="${numbersInstance?.org?.id}" class="many-to-one"/>

</div>

