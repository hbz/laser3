<%@ page import="com.k_int.kbplus.Org; com.k_int.kbplus.Person; com.k_int.kbplus.PersonRole" %>



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
	<laser:select id="gender" name="gender"
		from="${com.k_int.kbplus.Person.getAllRefdataValues('Gender')}"
    	optionKey="id"
    	optionValue="${{it.getI10n('value')}}"
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
        <!--
		<li class="add">
            <g:link controller="contact" action="create" params="['prs.id': personInstance?.id]">
                ${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}
            </g:link>
        </li>
        -->
        <input class="ui primary button" value="${message(code: 'default.add.label', args: [message(code: 'contact.label', default: 'Contact')])}"
               data-toggle="modal" href="#addContactModal" type="submit">
	</ul>

    <div id="addContactModal" class="modal hide">
        <g:formRemote name="remoteContactForm"
                      update="updateMeContact"
                      url="[controller: 'contact', action: 'create']">
            <g:render template="/contact/formModal" model="['prsId': personInstance?.id]"/>
        </g:formRemote>
    </div>

    <div id="updateMeContact"></div>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'addresses', 'error')} ">
	<label for="contacts">
		<g:message code="person.addresses.label" default="Addresses" />
	</label>
	<ul class="one-to-many">
		<g:each in="${personInstance?.addresses?}" var="a">
		    <li><g:link controller="address" action="show" id="${a.id}">${a?.encodeAsHTML()}</g:link></li>
		</g:each>
        <!--
		<li class="add">
            <g:link controller="address" action="create" params="['prs.id': personInstance?.id]">
                ${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}
            </g:link>
        </li>
        -->
        <input class="ui primary button" value="${message(code: 'default.add.label', args: [message(code: 'address.label', default: 'Address')])}"
               data-toggle="modal" href="#addAddressModal" type="submit">
	</ul>

    <div id="addAddressModal" class="modal hide">
        <g:formRemote name="remoteAddressForm"
                      update="updateMeAddress"
                      url="[controller: 'address', action: 'create']">
            <g:render template="/address/formModal" model="['prsId': personInstance?.id]"/>
        </g:formRemote>
    </div>

    <div id="updateMeAddress"></div>
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'tenant', 'error')} required">
	<label for="org">
		<g:message code="person.tenant.label" default="Tenant (Permissions to edit this person and depending addresses and contacts)" />
		<span class="required-indicator">*</span>		
	</label>
	<g:select id="tenant" name="tenant.id" from="${userMemberships}" 
		optionKey="id" value="${personInstance?.tenant?.id}" />
</div>

<div class="fieldcontain ${hasErrors(bean: personInstance, field: 'isPublic', 'error')} required">
	<label for="isPublic">
		<g:message code="person.isPublic.label" default="IsPublic" />
		<span class="required-indicator">*</span>	
	</label>
	<laser:select id="isPublic" name="isPublic"
		from="${com.k_int.kbplus.Person.getAllRefdataValues('YN')}"
    	optionKey="id"
    	optionValue="${{it.getI10n('value')}}"
        value="${personInstance?.isPublic?.id}" />
</div>

<div id="person-role-manager">

	<div class="person-role-function-manager">
		<h3>Functions</h3>
		
		<laser:select class="values"
			name="ignore-functionType-selector"
		    from="${PersonRole.getAllRefdataValues('Person Function')}" 
		    optionKey="id" 
		    optionValue="${{it.getI10n('value')}}" /> 
		    
		<button class="add-person-role" type="button">Add</button>
		
		<div class="workspace">
			<h4>* New</h4>
			<div class="adding"></div>
			<h4>Existing</h4>
			<div class="existing"></div>			
		</div>
	</div>
	
	
	<div class="person-role-responsibility-manager">
		<h3>Responsibilities</h3>
		
		<laser:select class="values"
			name="ignore-responsibilityType-selector"
		    from="${PersonRole.getAllRefdataValues('Person Responsibility')}" 
		    optionKey="id" 
		    optionValue="${{it.getI10n('value')}}" /> 
		    
		<button class="add-person-role" type="button">Add</button>

		<div class="workspace">
			<h4>* New</h4>
			<div class="adding"></div>
			<h4>Existing</h4>
			<div class="existing"></div>			
		</div>
	</div>
	
	<script>
		$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&roleType=func').done(function(data){
			$('.person-role-function-manager .workspace .existing').append(data);
		});
		$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=list&roleType=resp').done(function(data){
			$('.person-role-responsibility-manager .workspace .existing').append(data);
		});		


		$('.person-role-function-manager .add-person-role').click(function(){		
			var tt = $('.person-role-function-manager .values').val()
			
			$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&roleType=func&roleTypeId=' + tt + '&org=${params?.org?.id}').done(function(data){
				$('.person-role-function-manager .workspace .adding').append(data);
			});
		})
		
		$('.person-role-responsibility-manager .add-person-role').click(function(){
			var tt = $('.person-role-responsibility-manager .values').val()
			
			$.get('${webRequest.baseUrl}/person/ajax/${personInstance?.id}?cmd=add&roleType=resp&roleTypeId=' + tt + '&org=${params?.org?.id}').done(function(data){
				$('.person-role-responsibility-manager .workspace .adding').append(data);
			});
		})
	</script>

</div>
