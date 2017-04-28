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
		from="${com.k_int.kbplus.Person.getAllRefdataValues('Gender')}"
    	optionKey="id"
    	optionValue="value"
        value="${personInstance?.gender?.id}"
        noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'contacts', 'error')} ">
	<label for="contacts">
		<g:message code="person.contacts.label" default="Contacts" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.contacts?}" var="c">
		    <li><g:link controller="contact" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
		</g:each>
		<li class="add">
		<g:link controller="contact" action="create" params="['prs.id': personInstance?.id]">
			${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}
		</g:link>
		</li>
	</ul>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'addresses', 'error')} ">
	<label for="contacts">
		<g:message code="person.addresses.label" default="Addresses" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.addresses?}" var="a">
		    <li><g:link controller="address" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
		</g:each>
		<li class="add">
		<g:link controller="address" action="create" params="['prs.id': personInstance?.id]">
			${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}
		</g:link>
		</li>
	</ul>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'owner', 'error')} required">
	<label for="org">
		<g:message code="person.owner.label" default="Owner (Permissions to edit this person and depending addresses and contacts)" />
		<span class="required-indicator">*</span>		
	</label>
	<g:select id="owner" name="owner.id" from="${com.k_int.kbplus.Org.list()}" 
		optionKey="id" value="${personInstance?.owner?.id}" />

</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'isPublic', 'error')} required">
	<label for="isPublic">
		<g:message code="person.isPublic.label" default="IsPublic" />
		<span class="required-indicator">*</span>	
	</label>
	<g:select id="isPublic" name="isPublic" 
		from="${com.k_int.kbplus.Person.getAllRefdataValues('YN')}"
    	optionKey="id"
    	optionValue="value"
        value="${personInstance?.isPublic?.id}" />
</div>


<h3>Person-to-Org-and-X-with-Role (Demo only)</h3>

<div id="ui-placeholder-cluster" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-lic" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-pkg" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-sub" class="ui-ajax"></div><hr/>
<div id="ui-placeholder-title" class="ui-ajax"></div>

<g:if test="${personInstance?.id != null}">
	<script>
		$.get("${webRequest.baseUrl}/person/ajax/${personInstance.id}?type=cluster").done(function(data){
			$("#ui-placeholder-cluster").append(data);
		});
		$.get("${webRequest.baseUrl}/person/ajax/${personInstance.id}?type=lic").done(function(data){
			$("#ui-placeholder-lic").append(data);
		});
		$.get("${webRequest.baseUrl}/person/ajax/${personInstance.id}?type=pkg").done(function(data){
			$("#ui-placeholder-pkg").append(data);
		});
		$.get("${webRequest.baseUrl}/person/ajax/${personInstance.id}?type=sub").done(function(data){
			$("#ui-placeholder-sub").append(data);
		});
		$.get("${webRequest.baseUrl}/person/ajax/${personInstance.id}?type=title").done(function(data){
			$("#ui-placeholder-title").append(data);
		});
	</script>
</g:if>
