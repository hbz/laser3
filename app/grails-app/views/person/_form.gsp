<%@ page import="com.k_int.kbplus.Person" %>



<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'first_name', 'error')} required">
	<label for="first_name">
		<g:message code="person.first_name.label" default="Firstname" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="first_name" required="" value="${personInstance?.first_name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'middle_name', 'error')} ">
	<label for="middle_name">
		<g:message code="person.middle_name.label" default="Middlename" />
		
	</label>
	<g:textField name="middle_name" value="${personInstance?.middle_name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'last_name', 'error')} required">
	<label for="last_name">
		<g:message code="person.last_name.label" default="Lastname" />
		<span class="required-indicator">*</span>
	</label>
	<g:textField name="last_name" required="" value="${personInstance?.last_name}"/>

</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'gender', 'error')} ">
	<label for="gender">
		<g:message code="person.gender.label" default="Gender" />
	</label>
	<g:select id="gender" name="gender" 
		from="${com.k_int.kbplus.Person.getAllRefdataValues()}"
    	optionKey="id"
    	optionValue="value"
        value="${personInstance?.gender?.id}"
        noSelection="['': '']"/>
</div>