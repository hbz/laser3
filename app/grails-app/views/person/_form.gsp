<%@ page
import="com.k_int.kbplus.Org"  
import="com.k_int.kbplus.Person" 
import="com.k_int.kbplus.PersonRole" 
%>



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

<div id="person-role-manager">

	<div class="person-role-existing-functions-and-responsibilites">
		<h3>Functions and Responsibilities</h3>
		<div class="workspace">
			<h4>Existing</h4>
			<div class="existing"></div>
		</div>
	</div>
	
	<div class="person-role-function-manager">
		<h3>Functions</h3>
		
		<select class="values">
			<option value="org">Function only</option>
		</select>
	
		<button class="add-person-role" type="button">Add</button>
		
		<div class="workspace">
			<!--<h4>Existing</h4>
			<div class="existing"></div>-->
			
			<h4>Adding new</h4>
			<div class="adding"></div>
		</div>
	</div>
	
	
	<div class="person-role-responsibility-manager">
		<h3>Responsibilities</h3>
		
		<select class="values">
			<option value="cluster">Responsibility for cluster</option>
			<option value="lic">Responsibility for license</option>
			<option value="pkg">Responsibility for package</option>
			<option value="sub">Responsibility for subscription</option>
			<option value="title">Responsibility for title</option>
		</select>
	
		<button class="add-person-role" type="button">Add</button>

		<div class="workspace">
			<!--<h4>Existing</h4>
			<div class="existing"></div>-->
			
			<h4>Adding new</h4>
			<div class="adding"></div>
		</div>
	</div>
	
	<script>
		// TODO; fallback
		$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list').done(function(data){
			$('.person-role-existing-functions-and-responsibilites .workspace .existing').append(data);
		});
		
		/* $.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&type=functions').done(function(data){
			$('.person-role-function-manager .workspace .existing').append(data);
		});	*/
		$('.person-role-function-manager .add-person-role').click(function(){
			var tt = $('.person-role-function-manager .values').val()
			
			$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&org=${params?.org?.id}&type=' + tt).done(function(data){
				$('.person-role-function-manager .workspace .adding').append(data);
			});
		})
		
		/* $.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&type=responsibilities').done(function(data){
			$('.person-role-responsibility-manager .workspace .existing').append(data);
		}); */		
		$('.person-role-responsibility-manager .add-person-role').click(function(){
			var tt = $('.person-role-responsibility-manager .values').val()
			
			$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&org=${params?.org?.id}&type=' + tt).done(function(data){
				$('.person-role-responsibility-manager .workspace .adding').append(data);
			});
		})
	</script>
	
</div>
